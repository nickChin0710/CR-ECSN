/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/09/21  V1.00.02  Alex       add check procDate                         *
* 109/11/17  V1.00.03  Alex       coding standard, rename field method & format*
* 110/02/02  V1.00.04  Alex       排除刪除卡特指								 *
******************************************************************************/

package Cca;

import com.BaseBatch;
import com.SqlParm;
import com.BaseBatch;

public class CcaR041 extends BaseBatch {
	private String PROGNAME = "每月特指/凍結檢視處理 109/11/17 V1.00.03";
	hdata.RskAcnoLog hAcnl = new hdata.RskAcnoLog();
// -----------------------------------------------------------------------------
	private String isProcYymm = "";
	private int tiBase1 = -1;
	private int tiBase2 = -1;
	private int tiAcct1 = -1;
	private int tiAcnologU = -1;

	public static void main(String[] args) {
		CcaR041 proc = new CcaR041();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit(0);
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(PROGNAME);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : CcaR041 [YYYYMM,batch_seq]");
			errExit(1);
		}
		dbConnect();
		if (liArg > 0) {
			if (args[0].length() == 6) {
				isProcYymm = args[0];
			}
			callBatchSeqno(args[liArg - 1]);
		}
		callBatch(0, 0, 0);

		if (empty(isProcYymm)) {
			isProcYymm =  commDate.dateAdd(hBusiDate, 0, -1, 0);
		}

		if (isProcYymm.length() == 8) {
			String procDate = "";
			procDate = commString.mid(isProcYymm, 6, 2);
			if (procDate.equals("01") == false) {
				showLogMessage("I", "", "執行日不是每月 1 日 , 不執行結束");
				endProgram();
				return;
			}
			isProcYymm = commString.mid(isProcYymm, 0, 6);
		}

		// -重新檢視-
		selectRskAcnolog1();
		// -檢視資料預備-
		selectRskAcnolog2();

		sqlCommit(1);
		endProgram();

	}

	void selectRskAcnolog1() throws Exception {

		sqlCmd = " select hex(rowid) as rowid , kind_flag , card_no , acno_p_seqno , acct_type , log_type , "
				+ " block_reason , block_reason2 , block_reason3 , block_reason4 , block_reason5 , spec_status , blk_review_flag "
				+ " from rsk_acnolog " + " where log_date like ?" + " and log_type in ('3','6')" +
//         " and log_mode = '1'"+
				" and blk_review_flag ='0' ";

		setString(1, isProcYymm + "%");
		daoTable = "rsk_acnolog-1";
		openCursor();

		while (fetchTable()) {
			hAcnl.rowid = colSs("rowid");
			hAcnl.kindFlag = colSs("kind_flag");
			hAcnl.cardNo = colSs("card_no");
			hAcnl.acnoPSeqno = colSs("acno_p_seqno");
			hAcnl.acctType = colSs("acct_type");
			hAcnl.logType = colSs("log_type");
			hAcnl.blockReason = colSs("block_reason");
			hAcnl.blockReason2 = colSs("block_reason2");
			hAcnl.blockReason3 = colSs("block_reason3");
			hAcnl.blockReason4 = colSs("block_reason4");
			hAcnl.blockReason5 = colSs("block_reason5");
			hAcnl.specStatus = colSs("spec_status");
			hAcnl.blkReviewFlag = colSs("blk_review_flag");

			totalCnt++;
//      if ((totalCnt%1000)==0) {
//         printf("-->acnolog-1 proc-row="+totalCnt);
//      }

			// --卡片特指: kind_flag='C' and log_type='6'
			if (hAcnl.cardNo.isEmpty() == false) {
				checkCardBase(hAcnl.cardNo);
			}
			// --帳戶凍結: kind_flag='A' and log_type='3'
			if(hAcnl.kindFlag.equals("A")) {
				checkCardAcct(hAcnl.acnoPSeqno, hAcnl.acctType);
			}			
			if (empty(hAcnl.blkReviewFlag))
				continue;

			updateAcnolog();
		}
		closeCursor();
	}

	void checkCardBase(String lsCard) throws Exception {
		if (empty(lsCard)) {
			hAcnl.blkReviewFlag = "9";
			return;
		}

		if (tiBase1 <= 0) {
			sqlCmd = " select A.card_no , A.debit_flag , A.spec_status , A.spec_del_date , "
					+ " decode(A.debit_flag,'Y',C.current_code,B.current_code) as current_code "
					+ " from cca_card_base A left join crd_card B on B.card_no=A.card_no and B.current_code='0' "
					+ " left join dbc_card C on C.card_no=A.card_no and C.current_code='0' " + " where A.card_no = ? ";
			tiBase1 = ppStmtCrt("tiBase1", "");
		}

		setString(1, lsCard);
		sqlSelect(tiBase1);

		// 無流通卡--
		if (sqlNrow <= 0) {
			hAcnl.blkReviewFlag = "1";
			return;
		}
		
		if(colEmpty("spec_status")) {
			//--排除刪除戶特指
			hAcnl.blkReviewFlag = "9";
			return ;
		}
		
		// -del_date到期-
		if (!colEq("spec_status", hAcnl.specStatus) || ssComp(colNvl("spec_del_date", "99991231"), sysDate) <= 0) {
			hAcnl.blkReviewFlag = "9"; // -review結束-
			return;
		}

	}

	void checkCardAcct(String lsPSeqno, String lsAcctType) throws Exception {
		if (empty(lsPSeqno)) {
			hAcnl.blkReviewFlag = "9";
			return;
		}

		if (tiBase2 <= 0) {
			sqlCmd = " select count(*) as xx_cnt"
					+ " from cca_card_base A left join crd_card B on B.card_no=A.card_no and B.current_code='0'"
					+ " left join dbc_card C on C.card_no=A.card_no and C.current_code='0'" + " where A.p_seqno = ?"
					+ " and A.acct_type = ?";
			tiBase2 = ppStmtCrt("tiBase2", "");
		}

		setString(1, lsPSeqno);
		setString(2, lsAcctType);
		sqlSelect(tiBase2);

		if (colNum("xx_cnt") <= 0) {
			hAcnl.blkReviewFlag = "1"; // 無流通卡
			return;
		}

		if (tiAcct1 <= 0) {
			sqlCmd = " select block_reason1, block_reason2, block_reason3, block_reason4, block_reason5 , spec_status, spec_del_date "
					+ " from cca_card_acct where p_seqno = ? and acct_type = ? ";

			tiAcct1 = ppStmtCrt("tiAcct1", "");
		}

		setString(1, lsPSeqno);
		setString(2, lsAcctType);
		sqlSelect(tiAcct1);

		if (sqlNrow <= 0) {
			hAcnl.blkReviewFlag = "9"; // review結束
			return;
		}

		if (!empty(hAcnl.blockReason2) && !eqIgno(hAcnl.blockReason2, colSs("block_reason2"))) {
			hAcnl.blkReviewFlag = "9";
			return;
		}

		if (!empty(hAcnl.blockReason3) && !eqIgno(hAcnl.blockReason3, colSs("block_reason3"))) {
			hAcnl.blkReviewFlag = "9";
			return;
		}

		if (!empty(hAcnl.blockReason4) && !eqIgno(hAcnl.blockReason4, colSs("block_reason4"))) {
			hAcnl.blkReviewFlag = "9";
			return;
		}

		if (!empty(hAcnl.blockReason5) && !eqIgno(hAcnl.blockReason5, colSs("block_reason5"))) {
			hAcnl.blkReviewFlag = "9";
			return;
		}

		if (!empty(hAcnl.specStatus) && !eqIgno(hAcnl.specStatus, colSs("spec_status"))) {
			hAcnl.blkReviewFlag = "9";
			return;
		}

		if (ssComp(colNvl("spec_del_date", "99991231"), sysDate) <= 0) {
			hAcnl.blkReviewFlag = "9";
			return;
		}
	}

	void selectRskAcnolog2() throws Exception {
		sqlCmd = " select hex(rowid) as rowid , kind_flag, card_no, acno_p_seqno, acct_type , log_type , "
				+ " block_reason, block_reason2, block_reason3, block_reason4, block_reason5 , spec_status "
				+ " from rsk_acnolog " + " where log_date like ?" + " and log_type in ('3','6')"
//         +" and log_mode ='1'"
				+ " and blk_review_flag =''";

		ppp(1, isProcYymm + "%");
		daoTable = "rsk_acnolog-2";
		openCursor();

		while (fetchTable()) {
			hAcnl.rowid = colSs("rowid");
			hAcnl.kindFlag = colSs("kind_flag");
			hAcnl.cardNo = colSs("card_no");
			hAcnl.acnoPSeqno = colSs("acno_p_seqno");
			hAcnl.acctType = colSs("acct_type");
			hAcnl.logType = colSs("log_type");
			hAcnl.blockReason = colSs("block_reason");
			hAcnl.blockReason2 = colSs("block_reason2");
			hAcnl.blockReason3 = colSs("block_reason3");
			hAcnl.blockReason4 = colSs("block_reason4");
			hAcnl.blockReason5 = colSs("block_reason5");
			hAcnl.specStatus = colSs("spec_status");

			totalCnt++;
//      if ((totalCnt%1000)==0) {
//         printf("-->acnolog-2 proc-row="+totalCnt);
//      }

			hAcnl.blkReviewFlag = "0";

			// 卡片特指: kind_flag='C' and log_type='6'
			if (hAcnl.cardNo.isEmpty() == false) {
				checkCardBase(hAcnl.cardNo);
			}
			// 帳戶凍結: kind_flag='A' and log_type='3'
			if(hAcnl.kindFlag.equals("A")) {
				checkCardAcct(hAcnl.acnoPSeqno, hAcnl.acctType);
			}			
			if (empty(hAcnl.blkReviewFlag))
				continue;

			updateAcnolog();
		}
		closeCursor();
	}

	void updateAcnolog() throws Exception {
		if (tiAcnologU <= 0) {
			sqlCmd = " update rsk_acnolog set " + " blk_review_flag = ?, " + " blk_review_date = ? "
					+ " where rowid = ? ";
			tiAcnologU = ppStmtCrt("rsk_acnolog-U", "");
		}

		setString(1, hAcnl.blkReviewFlag);
		if (eqIgno(hAcnl.blkReviewFlag, "0")) {
			setString(2, "");
		} else {
			setString(2,  commDate.sysDate());
		}
		setRowId(3, hAcnl.rowid);

		sqlExec(tiAcnologU);
		if (sqlNrow <= 0) {
			sqlerr("update rsk_acnolog error");
			errExit(1);
		}

		return;
	}

}
