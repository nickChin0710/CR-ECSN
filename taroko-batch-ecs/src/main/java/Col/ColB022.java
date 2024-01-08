/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/23  V1.00.00    phopho     program initial                          *
*  109/03/30  V1.00.01    phopho     fix update_act_debt SQL                  *
*  109/12/14  V1.00.02    shiyuqi       updated for project coding standard   *
*  112/07/24  V1.00.03    sunny      調整產生會計帳相關處理                                                 *
*  112/09/14  V1.00.04    sunny      增加平帳作業處理寫入act_jrnl                 *
*  112/10/23  V1.00.05    sunny      增加平帳作業處理轉呆時也要寫入act_jrnl          *
******************************************************************************/

package Col;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB022 extends AccessDAO {
	public final boolean debug = false; // debug用
	public final boolean debug1 = true; // debug用
	private String progname = "每日科目明細轉催呆戶處理程式 112/10/23  V1.00.05 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

	String rptName1 = "";
	int rptSeq1 = 0;
	String buf = "";
	String hCallBatchSeqno = "";
	String hBusiBusinessDate = "";

//    String h_system_vouch_type         = "";
//    String h_system_vouch_date         = "";
//    int    h_system_refno_seq          = 0;
	String hAcagAcctMonth = "";
	double hAcagPayAmt = 0;
	double hAcagModSeqno = 0;
	String hAcagRowid = "";
	double hAcctAcctJrnlBal = 0;
	double hAcctTempUnbillInterest;
	double hAcctMinPay = 0;
	double hAcctMinPayBal = 0;
	double hAcctRcMinPay = 0;
	double hAcctRcMinPayBal = 0;
	double hAcctRcMinPayM0 = 0;
	double hAcctAdjustDrAmt = 0;
	int hAcctAdjustDrCnt = 0;
	double hAcctTtlAmt = 0;
	double hAcctTtlAmtBal = 0;
	String hAcctRowid = "";
	String hAcnoPSeqno = "";
	String hAcnoAcctType = "";
	String hAcnoAcctKey = "";
	String hAcnoCorpPSeqno = "";
	String hAcnoCorpNo = "";
	String hAcnoCorpNoCode = "";
	String hAcnoAcctStatus = "";
	String hAcnoAcnoFlag = "";
	String hAcnoAcctSubStatus = "";
	String hAcnoStmtCycle = "";
	String hAcnoAcctHolderId = "";
	String hAcnoAcctHolderIdCode = "";
	String hAcnoIdPSeqno = "";
	String hAcnoNoDelinquentFlag = "";
	String hAcnoNoDelinquentSDate = "";
	String hAcnoNoDelinquentEDate = "";
	String hAcnoNoCollectionFlag = "";
	String hAcnoNoCollectionSDate = "";
	String hAcnoNoCollectionEDate = "";
	String hAcnoLegalDelayCode = "";
	String hAcnoLawsuitMark = "";
	String hAcnoLawsuitMarkDate = "";
	String hAcnoRecourseMark = "";
	String hAcnoCreditActNo = "";
	long hAcnoLineOfCreditAmt = 0;
	String hAcnoModUser = "";
	String hAcnoModPgm = "";
	double hAcnoModSeqno = 0;
	String hAcnoRowid = "";
	String hDebtReferenceSeq = "";
	String hDebtItemOrderNormal = "";
	String hDebtItemOrderBackDate = "";
	String hDebtItemOrderRefund = "";
	String hDebtItemClassNormal = "";
	String hDebtItemClassBackDate = "";
	String hDebtItemClassRefund = "";
	String hDebtAcctMonth = "";
	double hDebtEndBal = 0;
	double hDebtDAvailableBal = 0;
	String hDebtCardNo = "";
	String hDebtAcctCode = "";
	String hDebtAcctItemCname = "";
	String hDebtPurchaseDate = "";
	String hDebtPostDate = "";
	String hDebtOrgItemEname = "";
	String hDebtRowid = "";
	String hCdbtPSeqno = "";
//    String h_gsvh_curr                 = "";
//    String h_gsvh_memo1                = "";
//    String h_gsvh_memo2                = "";
//    String h_gsvh_mod_pgm              = "";
	double hAgenRevolvingInterest1 = 0;
	double hAgenRevolvingInterest2 = 0;
	double hAgenRevolvingInterest3 = 0;
	double hAgenRevolvingInterest4 = 0;
	double hAgenRevolvingInterest5 = 0;
	double hAgenRevolvingInterest6 = 0;
	String hWdayStmtCycle = "";
	String hWdayThisAcctMonth = "";
	String hWdayNextAcctMonth = "";
	String hWdayThisCloseDate = "";

	int totalCnt = 0;

	String hTempAcctCode = "";
	String hTempAcctStatus = "";
	String hTempAcctSubStatus = "";
	String hVouchCdKind = "";
	String hTAcNo = "";
	int hTSeqno = 0;
	String hTDbcr = "";
	String hTMemo3Kind = "";
	String hTMemo3Flag = "";
	String hTDrFlag = "";
	String hTCrFlag = "";
	String tMemo3 = "";

	String hTempLegalDelayCode = "";
	double hTempRate = 0;
	double hTempRateAmt = 0;

	int hTempMonth = 0;
	double cTotalAmt[] = new double[12];
	double dTotalAmt[] = new double[12];
	double nTotalAmt[] = new double[12];
	double tTotalAmt[] = new double[12];
	double t6TotalAmt[] = new double[12];

	String tmpstr = "";
	String tmpstr1 = "";
	String temstr = "";
	double amtNovouchAf = 0, amtNovouchCf = 0;
	double amtNovouchPf = 0, amtNovouchAi = 0;
	int inta;

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 1) {
				comc.errExit("Usage : ColB022 0", "");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.hCallBatchSeqno = hCallBatchSeqno;
			comcr.hCallRProgramCode = javaProgram;

			comcr.callbatch(0, 0, 0);

			hAcnoModUser = comc.commGetUserID();
			hAcnoModPgm = javaProgram;

			selectPtrBusinday();
			selectPtrActgeneral();
			selectColDebtT();

// 20230724 TCB取消
//            if ((amtNovouchAf > 0) || (amtNovouchCf > 0) || (amtNovouchPf > 0) || (amtNovouchAi > 0))
//                novouchReportRtn();

			showLogMessage("I", "", "程式執行結束");
			comcr.hCallErrorDesc = "程式執行結束";

			// ==============================================
			// 固定要做的
			comcr.callbatch(1, 0, 0);
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		sqlCmd = "select business_date ";
		sqlCmd += "from ptr_businday ";
		sqlCmd += "fetch first 1 row only ";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
		}
	}

	/*
	 * I001轉催會計帳 *******************************************************
	 * 
	 * I001 D 1 155410016 非放款轉列之催收款項-信用卡墊款轉入 每月不定期批次作業產生轉催收起帳 I001 C 2 130270032
	 * 應收信用卡款項-信用卡墊款 每月不定期批次作業產生轉催收起帳 I001 C 3 130270041 應收信用卡款項-循環信用息
	 * 每月不定期批次作業產生轉催收起帳 I001 C 4 130270024 應收信用卡款項-信用卡手續費 每月不定期批次作業產生轉催收起帳 I001 C 5
	 * 130270024 應收信用卡款項-信用卡手續費 每月不定期批次作業產生轉催收起帳
	 */

	/***********************************************************************/
	void selectColDebtT() throws Exception {
		sqlCmd = "select ";
		sqlCmd += "p_seqno ";
		sqlCmd += "from col_debt_t ";
		sqlCmd += "where aud_code ='2' ";
		sqlCmd += "and   apr_date <> '' ";
		sqlCmd += "group by p_seqno ";

		openCursor();
		while (fetchTable()) {
			hCdbtPSeqno = getValue("p_seqno");

			selectActAcno();
			selectActAcct();

			if (hAcnoAcctStatus.equals("3")) {
				for (inta = 0; inta < 10; inta++)
					nTotalAmt[inta] = 0;
				selectActDebt();
				hVouchCdKind = "I001"; // 舊C-01
//                tmpstr = String.format("%7d", comcr.str2int(hBusiBusinessDate) - 19110000);
//                if (hAcnoAcctHolderId.length() != 0) {
//                    tmpstr1 = String.format("ID %s 轉催收", hAcnoAcctHolderId);
//                } else {
//                    tmpstr1 = String.format("ID %s 轉催收", hAcnoCorpNo);
//                }

				tmpstr1 = String.format("%s-%s明細轉催", hAcnoAcctType, hAcnoAcctHolderId);

				if (!hAcnoAcnoFlag.equals("1")) {
					tmpstr1 = String.format("%s-%s明細轉催", hAcnoAcctType, hAcnoCorpNo);
				}

				// 違約金轉催時多一套會計帳
				if (t6TotalAmt[1] > 0) {
					hVouchCdKind = "I004";

					for (inta = 1; inta < 10; inta++) {
						tTotalAmt[inta] = t6TotalAmt[inta];
						showLogMessage("I", "", " 888-t6 get tTotalAmt[" + inta + "]=[" + nTotalAmt[inta] + "]");
					}

					tmpstr = "違約金轉循環息";
					comcr.hGsvhMemo1 = tmpstr1;
					comcr.hGsvhMemo2 = tmpstr;
					comcr.hGsvhModPgm = javaProgram;
					vouchRtn();
				}

				comcr.hGsvhMemo1 = tmpstr1;
//                comcr.hGsvhMemo2 = tmpstr1;
				comcr.hGsvhModPgm = javaProgram;

				showLogMessage("I", "", " 888 get AcnoFlag[" + hAcnoAcnoFlag + "],tmpstr1[" + tmpstr1 + "],Memo1["
						+ comcr.hGsvhMemo1 + "],Memo2[" + comcr.hGsvhMemo2 + "]");

				for (inta = 1; inta < 10; inta++)
					tTotalAmt[inta] = nTotalAmt[inta];
				vouchRtn();
			}

			if (hAcnoAcctStatus.equals("4")) {
				for (inta = 0; inta < 10; inta++)
					dTotalAmt[inta] = 0;
				selectActDebt1();
				hVouchCdKind = "I002"; // 舊C-10
//                if (hAcnoAcctHolderId.length() != 0) {
//                    tmpstr1 = String.format("ID %s 轉呆", hAcnoAcctHolderId);
//                } else {
//                    tmpstr1 = String.format("ID %s 轉呆", hAcnoCorpNo);
//                }

				tmpstr1 = String.format("%s-%s明細轉呆", hAcnoAcctType, hAcnoAcctHolderId);

				if (!hAcnoAcnoFlag.equals("1")) {
					tmpstr1 = String.format("%s-%s明細轉呆", hAcnoAcctType, hAcnoCorpNo);
				}

				comcr.hGsvhMemo1 = tmpstr1;
//                comcr.hGsvhMemo2 = comcr.hGsvhMemo1;
				comcr.hGsvhModPgm = javaProgram;
				for (inta = 1; inta < 10; inta++)
					tTotalAmt[inta] = dTotalAmt[inta];
				
				//明細轉呆加檔，sunny 暫時不起呆帳，應該不是起I002
				//vouchRtn();
			}

			if (hAcnoRecourseMark.equals("Y")) {
				hVouchCdKind = "C-04";
				if (hAcnoAcctHolderId.length() != 0) {
					tmpstr1 = String.format("ID %s 轉追索債權", hAcnoAcctHolderId);
				} else {
					tmpstr1 = String.format("ID %s 轉追索債權", hAcnoCorpNo);
				}
				comcr.hGsvhMemo1 = tmpstr1;
//                comcr.hGsvhMemo2 = comcr.hGsvhMemo1;
				comcr.hGsvhModPgm = javaProgram;
				tTotalAmt[1] = dTotalAmt[1] + dTotalAmt[2] + dTotalAmt[3] + dTotalAmt[4];
				tTotalAmt[2] = tTotalAmt[1];
				vouchRtn();
			}
			deleteColDebtT();
			updateActAcct();
			updateActAcctCurr();
		}
		closeCursor();
	}

	/***********************************************************************/
	void selectActAcno() throws Exception {
		hAcnoPSeqno = "";
		hAcnoAcctType = "";
		hAcnoAcctKey = "";
		hAcnoAcctHolderId = "";
		hAcnoAcctHolderIdCode = "";
		hAcnoIdPSeqno = "";
		hAcnoCorpNo = "";
		hAcnoCorpPSeqno = "";
		hAcnoCreditActNo = "";
		hAcnoLineOfCreditAmt = 0;
		hAcnoStmtCycle = "";
		hAcnoAcctStatus = "";
		hAcnoAcctSubStatus = "";
		hAcnoLegalDelayCode = "";
		hAcnoRecourseMark = "";
		hAcnoLawsuitMark = "";
		hAcnoLawsuitMarkDate = "";
		hAcnoNoDelinquentFlag = "";
		hAcnoNoDelinquentSDate = "";
		hAcnoNoDelinquentEDate = "";
		hAcnoNoCollectionFlag = "";
		hAcnoNoCollectionSDate = "";
		hAcnoNoCollectionEDate = "";
		hAcnoRowid = "";
		sqlCmd = "SELECT ";
//        sqlCmd += "a.p_seqno, ";
		sqlCmd += "a.acno_p_seqno, ";
		sqlCmd += "a.acct_type, ";
		sqlCmd += "a.acct_key, ";
		sqlCmd += "c.id_no acct_holder_id, ";
		sqlCmd += "c.id_no_code acct_holder_id_code, ";
		sqlCmd += "a.id_p_seqno, ";
		sqlCmd += "d.corp_no, ";
		sqlCmd += "a.corp_p_seqno, ";
		sqlCmd += "a.credit_act_no, ";
		sqlCmd += "a.line_of_credit_amt, ";
		sqlCmd += "a.stmt_cycle, ";
		sqlCmd += "a.acct_status, ";
		sqlCmd += "a.acno_flag, ";
//        sqlCmd += "a.acct_sub_status, ";  //no column
		sqlCmd += "a.legal_delay_code, ";
		sqlCmd += "decode(a.recourse_mark    ,'','N'       ,a.recourse_mark    ) recourse_mark, ";
		sqlCmd += "decode(a.lawsuit_mark     ,'','N'       ,a.lawsuit_mark     ) lawsuit_mark, ";
		sqlCmd += "decode(a.lawsuit_mark_date,'','30000101',a.lawsuit_mark_date) lawsuit_mark_date, ";
		sqlCmd += "a.rowid rowid  ";
		sqlCmd += "FROM    act_acno a, crd_idno c  ";
		sqlCmd += "  left join crd_corp d on d.corp_p_seqno = a.corp_p_seqno "; // find corp_no in crd_corp
//        sqlCmd += "WHERE   a.p_seqno = ? ";
		sqlCmd += "WHERE   a.acno_p_seqno = ? ";
		sqlCmd += "and     acno_flag <> 'Y'   ";
		sqlCmd += " and a.id_p_seqno = c.id_p_seqno  ";
		setString(1, hCdbtPSeqno);

		extendField = "act_acno.";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_act_acno not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
//            h_acno_p_seqno = getValue("p_seqno");
			hAcnoPSeqno = getValue("act_acno.acno_p_seqno");
			hAcnoAcctType = getValue("act_acno.acct_type");
			hAcnoAcctKey = getValue("act_acno.acct_key");
			hAcnoAcctHolderId = getValue("act_acno.acct_holder_id");
			hAcnoAcctHolderIdCode = getValue("act_acno.acct_holder_id_code");
			hAcnoIdPSeqno = getValue("act_acno.id_p_seqno");
			hAcnoCorpNo = getValue("act_acno.corp_no");
			hAcnoCorpPSeqno = getValue("act_acno.corp_p_seqno");
			hAcnoCreditActNo = getValue("act_acno.credit_act_no");
			hAcnoLineOfCreditAmt = getValueLong("act_acno.line_of_credit_amt");
			hAcnoStmtCycle = getValue("act_acno.stmt_cycle");
			hAcnoAcctStatus = getValue("act_acno.acct_status");
			hAcnoAcnoFlag = getValue("act_acno.acno_flag");
//          h_acno_acct_sub_status = getValue("acct_sub_status");  //no column
			hAcnoLegalDelayCode = getValue("act_acno.legal_delay_code");
			hAcnoRecourseMark = getValue("act_acno.recourse_mark");
			hAcnoLawsuitMark = getValue("act_acno.lawsuit_mark");
			hAcnoLawsuitMarkDate = getValue("act_acno.lawsuit_mark_date");
			hAcnoRowid = getValue("act_acno.rowid");
		}
	}

	/***********************************************************************/
	void updateColBadDebt() throws Exception {
		daoTable = "col_bad_debt";
		updateSQL = " src_amt = src_amt + ? ";
		whereStr = "where p_seqno = ? ";
		whereStr += "and   trans_date = (select max(trans_date) ";
		whereStr += "                     from  col_bad_debt ";
		whereStr += "                     where trans_type = ? ";
		whereStr += "                     and   p_seqno = ? )";
		whereStr += "and    trans_type = ? ";
		setDouble(1, hDebtEndBal);
		setString(2, hAcnoPSeqno);
		setString(3, hAcnoAcctStatus);
		setString(4, hAcnoPSeqno);
		setString(5, hAcnoAcctStatus);

		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_col_bad_debt not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void deleteColDebtT() throws Exception {
		daoTable = "col_debt_t";
		whereStr = "where p_seqno = ? ";
		whereStr += "and   aud_code='2' ";
		whereStr += "and   apr_date <> '' ";
		setString(1, hCdbtPSeqno);

		deleteTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("delete_col_debt_t not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void selectActAcct() throws Exception {
		hAcctAcctJrnlBal = 0;
		hAcctTempUnbillInterest = 0;
		hAcctMinPayBal = 0;
		hAcctRcMinPayM0 = 0;
		hAcctRcMinPayBal = 0;
		hAcctTtlAmtBal = 0;
		hAcctAdjustDrCnt = 0;
		hAcctAdjustDrAmt = 0;
		sqlCmd = "SELECT ";
		sqlCmd += "acct_jrnl_bal, ";
		sqlCmd += "temp_unbill_interest, ";
		sqlCmd += "min_pay_bal, ";
		sqlCmd += "rc_min_pay_m0, ";
		sqlCmd += "rc_min_pay_bal, ";
		sqlCmd += "ttl_amt_bal, ";
		sqlCmd += "adjust_dr_cnt, ";
		sqlCmd += "adjust_dr_amt, ";
		sqlCmd += "rowid as rowid ";
		sqlCmd += "FROM    act_acct ";
		sqlCmd += "WHERE   p_seqno = ? ";
		setString(1, hAcnoPSeqno);

		extendField = "act_acct.";

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hAcctAcctJrnlBal = getValueDouble("act_acct.acct_jrnl_bal");
			hAcctTempUnbillInterest = getValueDouble("act_acct.temp_unbill_interest");
			hAcctMinPayBal = getValueDouble("act_acct.min_pay_bal");
			hAcctRcMinPayM0 = getValueDouble("act_acct.rc_min_pay_m0");
			hAcctRcMinPayBal = getValueDouble("act_acct.rc_min_pay_bal");
			hAcctTtlAmtBal = getValueDouble("act_acct.ttl_amt_bal");
			hAcctAdjustDrCnt = getValueInt("act_acct.adjust_dr_cnt");
			hAcctAdjustDrAmt = getValueDouble("act_acct.adjust_dr_amt");
			hAcctRowid = getValue("act_acct.rowid");
		}
	}

	/***********************************************************************/
	void selectActDebt() throws Exception {
		long lAmt;
		double tempEndal, tempSumRateAmt = 0;

		selectPtrWorkday();

		sqlCmd = "select ";
		sqlCmd += "decode(a.acct_code,'BL','CB','CA','CB','IT','CB', ";
		sqlCmd += "'ID','CB','AO','CB','OT','CB','RI','CI','PN','CI', ";
		sqlCmd += "'LF','CC','SF','CC',a.acct_code) temp_item_ename, ";
		sqlCmd += "a.acct_code, ";
		sqlCmd += "a.acct_month, ";
		sqlCmd += "a.reference_no, ";
		sqlCmd += "a.end_bal, ";
		sqlCmd += "a.d_avail_bal, ";
		sqlCmd += "a.purchase_date, ";
		sqlCmd += "a.post_date, ";
		sqlCmd += "a.rowid as rowid ";
		sqlCmd += "FROM   act_debt a,col_debt_t b ";
		sqlCmd += "where  a.p_seqno = ? ";
		sqlCmd += "and    a.reference_no = b.reference_no ";
		sqlCmd += "and    a.acct_code != 'DP' ";
		sqlCmd += "and    a.acct_code != 'CB' ";
		sqlCmd += "and    a.acct_code != 'CC' ";
		sqlCmd += "and    a.acct_code != 'AI' ";
//        sqlCmd += "and    a.acct_code != 'AF' ";
//        sqlCmd += "and    a.acct_code != 'CF' ";
//        sqlCmd += "and    a.acct_code != 'PF' ";
		sqlCmd += "and    a.acct_code != 'CI' ";
		sqlCmd += "and    a.end_bal > 0  ";
		setString(1, hCdbtPSeqno);

		extendField = "act_debt.";

		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hTempAcctCode = getValue("act_debt.temp_item_ename", i);
			hDebtAcctCode = getValue("act_debt.acct_code", i);
			hDebtAcctMonth = getValue("act_debt.acct_month", i);
			hDebtReferenceSeq = getValue("act_debt.reference_no", i);
			hDebtEndBal = getValueDouble("act_debt.end_bal", i);
			hDebtDAvailableBal = getValueDouble("act_debt.d_avail_bal", i);
			hDebtPostDate = getValue("act_debt.post_date", i);
			hDebtPurchaseDate = getValue("act_debt.purchase_date", i);
			hDebtRowid = getValue("act_debt.rowid", i);

			
			if (debug1)
				showLogMessage("I", "", " 771[1]-明細平帳作業(selectActDebt) acct_code[" + hDebtAcctCode + "],post_date[" + hDebtPostDate + "]");
			
			// 本金
			if ((hDebtAcctCode.compareTo("BL") == 0) || (hDebtAcctCode.compareTo("CA") == 0)
					|| (hDebtAcctCode.compareTo("IT") == 0) || (hDebtAcctCode.compareTo("ID") == 0)
					|| (hDebtAcctCode.compareTo("OT") == 0) || (hDebtAcctCode.compareTo("AO") == 0)) {
//                nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal;
//                nTotalAmt[7] = nTotalAmt[7] + hDebtEndBal;
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // GEN_ACCT_M.dbcr_seq=1
				nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal; // GEN_ACCT_M.dbcr_seq=2

				hTempAcctCode = "CB";
				selectPtrActcode();

				/* 20230724 tcb先取消，計算cycle結帳日至轉催日之間的利息轉催收款利息 */
				/*
				 * if (hWdayThisCloseDate.compareTo(hDebtPurchaseDate) > 0) hDebtPurchaseDate =
				 * hWdayThisCloseDate;
				 * 
				 * hTempRateAmt = 0; //DB2 與 Oracle 對日期(時間)直接加減結果的表示方式不同, 語法須修正 phopho 2019.7.19
				 * // sqlCmd =
				 * "select ( ? * ? * to_char((to_date(?,'yyyymmdd'),'yyyymmdd') - to_char((to_date(?,'yyyymmdd'),'yyyymmdd')))*0.0001 rate_amt "
				 * ; // sqlCmd += "from dual "; //弱掃不過 Potential ReDoS phopho 2019.8.15 sqlCmd =
				 * "select ( ((?) * (?)) * (days(to_date(?,'yyyymmdd')) - days(to_date(?,'yyyymmdd')))) *0.0001 rate_amt "
				 * ; sqlCmd += "from dual "; setDouble(1, hTempRate); setDouble(2, hDebtEndBal);
				 * setString(3, hBusiBusinessDate); setString(4, hDebtPurchaseDate);
				 * 
				 * extendField = "temp_rate.";
				 * 
				 * int recordCnt1 = selectTable(); if (notFound.equals("Y")) {
				 * comcr.errRtn("select_compute rate_amt not found!", "", hCallBatchSeqno); } if
				 * (recordCnt1 > 0) { hTempRateAmt = getValueDouble("temp_rate.rate_amt"); }
				 * 
				 * if (hTempRateAmt < 0) hTempRateAmt = 0; lAmt = (long) hTempRateAmt;
				 * tempSumRateAmt = tempSumRateAmt + lAmt; nTotalAmt[3] = nTotalAmt[3] + lAmt;
				 * nTotalAmt[9] = nTotalAmt[9] + lAmt;
				 */
			}

			// 利息
			if (hDebtAcctCode.compareTo("RI") == 0) {
//                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
//                nTotalAmt[6] = nTotalAmt[6] + hDebtEndBal;
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // GEN_ACCT_M.dbcr_seq=1
				nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal; // GEN_ACCT_M.dbcr_seq=3
			}

			// 違約金
			if (hDebtAcctCode.compareTo("PN") == 0) {
//                nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal;
//                nTotalAmt[5] = nTotalAmt[5] + hDebtEndBal;
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // 催收款項(總和
//              nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;   	//掛費用130270024(營運科)--不用
				nTotalAmt[3] = nTotalAmt[3] + hDebtEndBal; // 掛利息130270041(風管科)--套用
				t6TotalAmt[1] = t6TotalAmt[1] + hDebtEndBal; // 違約金金額
				t6TotalAmt[2] = t6TotalAmt[2] + hDebtEndBal; // 違約金金額
			}

			// 帳外息 TCB取消
			/*
			 * if (hdebtacctcode.compareTo("AI") == 0) { nTotalAmt[3] = nTotalAmt[3] +
			 * hDebtEndBal; nTotalAmt[9] = nTotalAmt[9] + hDebtEndBal;
			 * 
			 * }
			 */

			// 掛失費
			if (hDebtAcctCode.compareTo("LF") == 0) {
//                nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal;
//                nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal;
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // 催收款項(總和
				nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; // 費用130270024(營運科)
			}

			// 法訴費 TCB 轉催前轉成催收費用類
			if (hDebtAcctCode.compareTo("SF") == 0) {
//                nTotalAmt[2] = nTotalAmt[2] + hDebtEndBal;
//                nTotalAmt[8] = nTotalAmt[8] + hDebtEndBal;
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // 催收款項(總和)
				nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; // 費用130270024(營運科)
			}

			// 20230721 AF,CF,PF於TCB均轉入CC催收款科目
			// -------------------------------------------
			// 年費
			if (hDebtAcctCode.equals("AF")) {
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // 催收款項(總和
				nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; // 費用130270024(營運科)
			}

			// 預借現金手續費
			if (hDebtAcctCode.equals("CF")) {
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // 催收款項(總和
				nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; // 費用130270024(營運科)
			}

			// 雜項手續費
			if (hDebtAcctCode.equals("PF")) {
				nTotalAmt[1] = nTotalAmt[1] + hDebtEndBal; // 催收款項(總和)
				nTotalAmt[4] = nTotalAmt[4] + hDebtEndBal; // 費用130270024(營運科)
			}
			// -------------------------------------------

			/* TCB取消--AF,CF,PF原不起帳的動作 */
//            if ((hdebtacctcode.compareTo("AF") == 0) || (hdebtacctcode.compareTo("CF") == 0)
//                    || (hdebtacctcode.compareTo("PF") == 0)) {
//                if (hDebtEndBal > 0) {
//                    if (hdebtacctcode.compareTo("AF") == 0)
//                        amtNovouchAf = amtNovouchAf + hDebtEndBal;
//                    if (hdebtacctcode.compareTo("CF") == 0)
//                        amtNovouchCf = amtNovouchCf + hDebtEndBal;
//                    if (hdebtacctcode.compareTo("PF") == 0)
//                        amtNovouchPf = amtNovouchPf + hDebtEndBal;
//                    tempEndal = hDebtEndBal;
//                    if (hWdayNextAcctMonth.compareTo(hDebtAcctMonth) != 0) {
//                        selectActAcag();
//                        hAcctMinPayBal = hAcctMinPayBal - tempEndal;
//                        if (hAcctMinPayBal < 0)
//                            hAcctMinPayBal = 0;
//                        hAcctRcMinPayBal = hAcctRcMinPayBal - tempEndal;
//                        if (hAcctRcMinPayBal < 0)
//                            hAcctRcMinPayBal = 0;
//                        hAcctTtlAmtBal = hAcctTtlAmtBal - tempEndal;
//                        if (hAcctTtlAmtBal < 0)
//                            hAcctTtlAmtBal = 0;
//                        if (hAcctRcMinPayBal < hAcctRcMinPayM0)
//                            hAcctRcMinPayM0 = hAcctRcMinPayBal;
//                    }
//                    hDebtEndBal = tempEndal;
//                    insertCycPyaj(1);
//                    hAcctAdjustDrCnt = hAcctAdjustDrCnt + 1;
//                    hAcctAdjustDrAmt = hAcctAdjustDrAmt + hDebtEndBal;
//                    hAcctAcctJrnlBal = hAcctAcctJrnlBal - hDebtEndBal;
//                    insertActJrnl(1);
//                    hDebtEndBal = 0;
//                    hDebtDAvailableBal = 0;
//                    updateActDebt();
//                }
//            } else {
//                updateActDebt();
//                insertColBadDetail();
//                updateColBadDebt();
//            }

			updateActDebt();
			if (debug1)
				showLogMessage("I", "", " 770[1]-明細轉呆平帳作業(selectActDebt) acct_code[" + hDebtAcctCode + "],post_date[" + hDebtPostDate + "]");
			insertActJrnl(1); // insert act_jrnl 舊科目(減項)
			insertActJrnl(2); // insert act_jrnl 新科目(加項)
			insertColBadDetail();
			updateColBadDebt();
		}

		/* 20230721 tcb先取消，計算cycle結帳日至轉催日之間的利息轉催收款利息 */
//        if ((nTotalAmt[7] == 0) && (hAcctTempUnbillInterest == 0))
//            return;
//
//        lAmt = (long) hAcctTempUnbillInterest;
//        hAcctTempUnbillInterest = 0;
//        hTempRateAmt = lAmt + tempSumRateAmt;
//        nTotalAmt[3] = nTotalAmt[3] + lAmt;
//        nTotalAmt[9] = nTotalAmt[9] + lAmt;
//
//        hTempAcctCode = "CI";
//        selectPtrActcode();
//        insertActDebt();
//        hDebtAcctCode = "CI";
//        hDebtEndBal = hTempRateAmt;
//        hDebtDAvailableBal = hTempRateAmt;
//        insertColBadDetail();
//        updateColBadDebt();
//        hAcctAcctJrnlBal = hAcctAcctJrnlBal + hTempRateAmt;
//        insertActJrnl(2);
//        insertCycPyaj(2);
	}

	/***********************************************************************/
	void selectActDebt1() throws Exception {
//		double tempEndBal;

		sqlCmd = "select ";
		sqlCmd += "decode(a.acct_code,'RI','RI','PN','PN','AF','AF','SF','SF', ";
		sqlCmd += "'CF','CF','PF','PF','DB') temp_item_ename, ";
		sqlCmd += "a.acct_code, ";
		sqlCmd += "a.acct_month, ";
		sqlCmd += "a.reference_no, ";
		sqlCmd += "a.end_bal, ";
		sqlCmd += "a.post_date, ";
		sqlCmd += "a.d_avail_bal, ";
		sqlCmd += "a.rowid as rowid ";
		sqlCmd += "FROM   act_debt a, col_debt_t b ";
		sqlCmd += "where  a.p_seqno = ? ";
		sqlCmd += "and    a.reference_no = b.reference_no ";
		sqlCmd += "and    a.acct_code != 'DP' ";
		sqlCmd += "and    a.acct_code != 'DB' ";
		sqlCmd += "and    a.acct_code != 'AI' ";
//        sqlCmd += "and    a.acct_code != 'LF' ";
		sqlCmd += "and    a.acct_code != 'CB' ";
		sqlCmd += "and    a.acct_code != 'CI' ";
//        sqlCmd += "and    a.acct_code != 'AF' ";
//        sqlCmd += "and    a.acct_code != 'CF' ";
//        sqlCmd += "and    a.acct_code != 'PF' ";
		sqlCmd += "and    a.acct_code != 'CC' ";
		sqlCmd += "and    a.end_bal >0 ";
		setString(1, hCdbtPSeqno);

		extendField = "act_debt_1.";

		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hTempAcctCode = getValue("act_debt_1.temp_item_ename", i);
			hDebtAcctCode = getValue("act_debt_1.acct_code", i);
			hDebtAcctMonth = getValue("act_debt_1.acct_month", i);
			hDebtReferenceSeq = getValue("act_debt_1.reference_no", i);
			hDebtEndBal = getValueDouble("act_debt_1.end_bal", i);
			hDebtPostDate = getValue("act_debt_1.post_date", i);
			hDebtDAvailableBal = getValueDouble("act_debt_1.d_avail_bal", i);
			hDebtRowid = getValue("act_debt_1.rowid", i);	
			
			
			if (debug1)
				showLogMessage("I", "", " 771[2]-明細平帳作業(selectActDebt1) acct_code[" + hDebtAcctCode + "],post_date[" + hDebtPostDate + "]");
		
					
			if (hTempAcctCode.compareTo("SF") == 0) {
				dTotalAmt[4] = dTotalAmt[4] + hDebtEndBal;
				dTotalAmt[8] = dTotalAmt[8] + hDebtEndBal;
			}
			if (hTempAcctCode.compareTo("RI") == 0) {
				dTotalAmt[2] = dTotalAmt[2] + hDebtEndBal;
				dTotalAmt[6] = dTotalAmt[6] + hDebtEndBal;
			}
			if (hTempAcctCode.compareTo("PN") == 0) {
				dTotalAmt[7] = dTotalAmt[7] + hDebtEndBal;
				dTotalAmt[3] = dTotalAmt[3] + hDebtEndBal;
			}
			if (hTempAcctCode.compareTo("DB") == 0) {
				dTotalAmt[1] = dTotalAmt[1] + hDebtEndBal;
				dTotalAmt[5] = dTotalAmt[5] + hDebtEndBal;
			}
			hTempAcctCode = "DB";

// 20230724 TCB取消            
//            if ((hDebtAcctCode.compareTo("AF") == 0) || (hDebtAcctCode.compareTo("CF") == 0)
//                    || (hDebtAcctCode.compareTo("PF") == 0)) {
//                if (hDebtEndBal > 0) {
//                    if (hDebtAcctCode.compareTo("AF") == 0)
//                        amtNovouchAf = amtNovouchAf + hDebtEndBal;
//                    if (hDebtAcctCode.compareTo("CF") == 0)
//                        amtNovouchCf = amtNovouchCf + hDebtEndBal;
//                    if (hDebtAcctCode.compareTo("PF") == 0)
//                        amtNovouchPf = amtNovouchPf + hDebtEndBal;
//                    tempEndBal = hDebtEndBal;
//                    if (hWdayNextAcctMonth.compareTo(hDebtAcctMonth) != 0) {
//                        selectActAcag();
//                        hAcctMinPayBal = hAcctMinPayBal - hDebtEndBal;
//                        if (hAcctMinPayBal < 0)
//                            hAcctMinPayBal = 0;
//                        hAcctRcMinPayBal = hAcctRcMinPayBal - hDebtEndBal;
//                        if (hAcctRcMinPayBal < 0)
//                            hAcctRcMinPayBal = 0;
//                        hAcctTtlAmtBal = hAcctTtlAmtBal - hDebtEndBal;
//                        if (hAcctTtlAmtBal < 0)
//                            hAcctTtlAmtBal = 0;
//                        if (hAcctRcMinPayBal < hAcctRcMinPayM0)
//                            hAcctRcMinPayM0 = hAcctRcMinPayBal;
//                    }
//                    hDebtEndBal = tempEndBal;
//                    insertCycPyaj(1);
//                    hAcctAdjustDrCnt = hAcctAdjustDrCnt + 1;
//                    hAcctAdjustDrAmt = hAcctAdjustDrAmt + hDebtEndBal;
//                    hAcctAcctJrnlBal = hAcctAcctJrnlBal - hDebtEndBal;
//                    insertActJrnl(1);
//                    hDebtEndBal = 0;
//                    hDebtDAvailableBal = 0;
//                    updateActDebt();
//                }
//            } else {
			hDebtOrgItemEname = hDebtAcctCode;
			if ((hDebtAcctCode.compareTo("CB") == 0) || (hDebtAcctCode.compareTo("CC") == 0)
					|| (hDebtAcctCode.compareTo("CI") == 0))
				selectColBadDetail();
			updateActDebt();
			if (debug1)
				showLogMessage("I", "", " 770[2]-明細轉催平帳作業(selectActDebt) acct_code[" + hDebtAcctCode + "],post_date[" + hDebtPostDate + "]");
			insertActJrnl(1); // insert act_jrnl 舊科目(減項)
			insertActJrnl(2); // insert act_jrnl 新科目(加項)
			insertColBadDetail();
			updateColBadDebt();
//            }
		}
	}

	/***********************************************************************/
	void selectColBadDetail() throws Exception {
		hDebtOrgItemEname = "";
		sqlCmd = "select acct_code ";
		sqlCmd += "from col_bad_detail ";
		sqlCmd += "where trans_type     = '3' ";
		sqlCmd += "and   p_seqno        = ? ";
		sqlCmd += "and   new_acct_code = ? ";
		sqlCmd += "and   reference_no  = ? ";
		setString(1, hCdbtPSeqno);
		setString(2, hDebtAcctCode);
		setString(3, hDebtReferenceSeq);

		extendField = "col_bad_detail.";

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hDebtOrgItemEname = getValue("col_bad_detail.acct_code");
		}
	}

	/***********************************************************************/
	void updateActDebt() throws Exception {
		hAcnoModSeqno = comcr.getModSeq();
		selectPtrActcode();

		daoTable = "act_debt";
		updateSQL = " acct_code    = ?,";
		updateSQL += " org_acct_code     = decode(cast(? as varchar(2)),'DB',cast(? as varchar(2)),''),";
		updateSQL += " acct_code_type    = decode(cast(? as varchar(2)),'DB',";
		updateSQL += "      decode(acct_code,'BL','B','CA','B','IT','B','ID','B',";
		updateSQL += "                             'AO','B','OT','B','RI','I','PN','I',";
		updateSQL += "                             'LF','C','SF','S','CB','B','CC','C',"; // SF轉S
		updateSQL += "                             'CI','I','AI','I','AF','C','X'),''),";
		updateSQL += " item_order_normal   = ?,";
		updateSQL += " item_order_back_date   = ?,";
		updateSQL += " item_order_refund   = ?,";
		updateSQL += " item_class_normal   = ?,";
		updateSQL += " item_class_back_date   = ?,";
		updateSQL += " item_class_refund   = ?,";
		updateSQL += " end_bal   = ?,";
		updateSQL += " dc_end_bal   = ?,";
		updateSQL += " d_avail_bal   = ?,";
		updateSQL += " dc_d_avail_bal   = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user   = ?,";
		updateSQL += " mod_pgm   = ?,";
		updateSQL += " mod_seqno   = ? ";
		whereStr = "where rowid = ? ";
		setString(1, hTempAcctCode);
		setString(2, hTempAcctCode);
		setString(3, hDebtOrgItemEname);
		setString(4, hTempAcctCode);
		setString(5, hDebtItemOrderNormal);
		setString(6, hDebtItemOrderBackDate);
		setString(7, hDebtItemOrderRefund);
		setString(8, hDebtItemClassNormal);
		setString(9, hDebtItemClassBackDate);
		setString(10, hDebtItemClassRefund);
		setDouble(11, hDebtEndBal);
		setDouble(12, hDebtEndBal);
		setDouble(13, hDebtDAvailableBal);
		setDouble(14, hDebtDAvailableBal);
		setString(15, hAcnoModUser);
		setString(16, hAcnoModPgm);
		setDouble(17, hAcnoModSeqno);
		setRowId(18, hDebtRowid);

		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_debt not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertColBadDetail() throws Exception {
		daoTable = "col_bad_detail";
		extendField = daoTable + ".";
		setValue(extendField + "trans_type", hAcnoAcctStatus);
		setValue(extendField + "p_seqno", hAcnoPSeqno);
		setValue(extendField + "trans_date", hBusiBusinessDate);
		setValue(extendField + "acct_code", hDebtAcctCode);
		setValue(extendField + "reference_no", hDebtReferenceSeq);
		setValue(extendField + "acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
		setValueDouble(extendField + "end_bal", hDebtEndBal);
		setValueDouble(extendField + "d_avail_bal", hDebtDAvailableBal);
		setValue(extendField + "new_acct_code", hTempAcctCode);
		setValue(extendField + "mod_user", hAcnoModUser);
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", hAcnoModPgm);

		insertTable();
	}

	/***********************************************************************/
	void insertActJrnl(int type) throws Exception {
		dateTime();
		daoTable = "act_jrnl";
		extendField = daoTable + ".";
		setValue(extendField + "crt_date", sysDate);
		setValue(extendField + "crt_time", sysTime);
		setValue(extendField + "p_seqno", hAcnoPSeqno);
		setValue(extendField + "curr_code", "901");
		setValue(extendField + "acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
		setValue(extendField + "id_p_seqno", hAcnoIdPSeqno);
//        setValue("id_no", h_acno_acct_holder_id);  //no column
//        setValue("id_no_code", h_acno_acct_holder_id_code);  //no column
		setValue(extendField + "corp_p_seqno", hAcnoCorpPSeqno);
//        setValue("corp_no", h_acno_corp_no);  //no column
//        setValue("corp_no_code", h_acno_corp_no_code);  //no column
		setValue(extendField + "acct_date", hBusiBusinessDate);
		setValue(extendField + "tran_class", type == 1 ? "A" : "B");
		setValue(extendField + "tran_type", type == 1 ? "CD01" : "CD02");
		setValue(extendField + "acct_code", type == 1 ? hDebtAcctCode : hTempAcctCode); // 1使用原acct_code、2使用新的acct_code
		setValue(extendField + "dr_cr", type == 1 ? "D" : "C");
		setValue(extendField + "item_date", hDebtPostDate);  //原始交易入帳日期
		setValueDouble(extendField + "transaction_amt", hDebtEndBal);
		setValueDouble(extendField + "jrnl_bal", hAcctAcctJrnlBal);
		setValueDouble(extendField + "item_bal", hDebtEndBal);
		setValueDouble(extendField + "item_d_bal", hDebtDAvailableBal);
		setValueDouble(extendField + "dc_transaction_amt", hDebtEndBal);
		setValueDouble(extendField + "dc_jrnl_bal", hAcctAcctJrnlBal);
		setValueDouble(extendField + "dc_item_bal", hDebtEndBal);
		setValueDouble(extendField + "dc_item_d_bal", hDebtDAvailableBal);
		setValue(extendField + "stmt_cycle", hAcnoStmtCycle);
		setValue(extendField + "mod_user", hAcnoModUser);
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", hAcnoModPgm);

		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_jrnl duplicate!", "", hCallBatchSeqno);
		}
		
		if (debug1)
			showLogMessage("I", "", " 770[3]-insertActJrnl,acct_code[" + hDebtAcctCode + "],TempAcctCode["+hTempAcctCode+"],EndBal["+hDebtEndBal+"]");
	}

	/***********************************************************************/
	void vouchRtn() throws Exception {

		comcr.startVouch("4", hVouchCdKind);
//        comcr.hGsvhMemo1 = tmpstr1;
//        comcr.hGsvhMemo2 = comcr.hGsvhMemo1;

		sqlCmd = "select ";
		sqlCmd += "gen_sys_vouch.ac_no, ";
		sqlCmd += "gen_sys_vouch.dbcr_seq, ";
		sqlCmd += "gen_sys_vouch.dbcr, ";
		sqlCmd += "gen_acct_m.memo3_kind, ";
		sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) memo3_flag, ";
		sqlCmd += "decode(gen_acct_m.dr_flag   ,'','N',gen_acct_m.dr_flag   ) dr_flag, ";
		sqlCmd += "decode(gen_acct_m.cr_flag   ,'','N',gen_acct_m.cr_flag   ) cr_flag ";
		sqlCmd += "from gen_sys_vouch, gen_acct_m ";
		sqlCmd += "where std_vouch_cd = ? ";
		sqlCmd += "and   gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
		sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
		setString(1, hVouchCdKind);

		extendField = "gen_sys_vouch.";

		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hTAcNo = getValue("gen_sys_vouch.ac_no", i);
			hTSeqno = getValueInt("gen_sys_vouch.dbcr_seq", i);
			hTDbcr = getValue("gen_sys_vouch.dbcr", i);
			hTMemo3Kind = getValue("gen_sys_vouch.memo3_kind", i);
			hTMemo3Flag = getValue("gen_sys_vouch.memo3_flag", i);
			hTDrFlag = getValue("gen_sys_vouch.dr_flag", i);
			hTCrFlag = getValue("gen_sys_vouch.cr_flag", i);

			tMemo3 = "";

			if (tTotalAmt[hTSeqno] > 0) {
				comcr.hGsvhMemo1 = tmpstr1;
//            	comcr.hGsvhMemo2= tmpstr;
				comcr.hGsvhCurr = "00";
//            	showLogMessage("I", "", " 999-1 get AcnoFlag["+hAcnoAcnoFlag+"],tTotalAmt["+ tTotalAmt[hTSeqno] +"],tmpstr1["+tmpstr1+"],Memo1["+comcr.hGsvhMemo1+"],Memo2["+ comcr.hGsvhMemo2+"]");
//            	comcr.detailVouch(hTAcNo, hTSeqno, tTotalAmt[hTSeqno]);

				if (comcr.detailVouch(hTAcNo, hTSeqno, tTotalAmt[hTSeqno]) != 0) {
					// comcr.errRtn("", "", hCallBatchSeqno);
					comcr.errRtn(String.format("call detail_vouch error"), "", hCallBatchSeqno);
//                    showLogMessage("I", "", " 999-2 get AcnoFlag["+hAcnoAcnoFlag+"],tmpstr1["+tmpstr1+"],Memo1["+comcr.hGsvhMemo1+"],Memo2["+ comcr.hGsvhMemo2+"]");
				}
			}
		}
	}

	/***********************************************************************/
	void updateActAcct() throws Exception {
		hAcnoModSeqno = comcr.getModSeq();

		daoTable = "act_acct";
		updateSQL = " acct_jrnl_bal   = ?,";
		updateSQL += " temp_unbill_interest  = ?,";
		updateSQL += " min_pay_bal     = ?,";
		updateSQL += " rc_min_pay_bal  = ?,";
		updateSQL += " rc_min_pay_m0   = ?,";
		updateSQL += " ttl_amt_bal     = ?,";
		updateSQL += " adjust_dr_cnt   = ?,";
		updateSQL += " adjust_dr_amt   = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user   = ?,";
		updateSQL += " mod_pgm   = ?,";
		updateSQL += " mod_seqno   = ? ";
		whereStr = "where rowid = ? ";
		setDouble(1, hAcctAcctJrnlBal);
		setDouble(2, hAcctTempUnbillInterest);
		setDouble(3, hAcctMinPayBal);
		setDouble(4, hAcctRcMinPayBal);
		setDouble(5, hAcctRcMinPayM0);
		setDouble(6, hAcctTtlAmtBal);
		setDouble(7, hAcctAdjustDrCnt);
		setDouble(8, hAcctAdjustDrAmt);
		setString(9, hAcnoModUser);
		setString(10, hAcnoModPgm);
		setDouble(11, hAcnoModSeqno);
		setRowId(12, hAcctRowid);

		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_acct not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void updateActAcctCurr() throws Exception {
		daoTable = "act_acct_curr";
		updateSQL = " acct_jrnl_bal   = ?,";
		updateSQL += " dc_acct_jrnl_bal  = ?,";
		updateSQL += " temp_unbill_interest  = ?,";
		updateSQL += " dc_temp_unbill_interest  = ?,";
		updateSQL += " min_pay_bal     = ?,";
		updateSQL += " dc_min_pay_bal  = ?,";
		updateSQL += " ttl_amt_bal   = ?,";
		updateSQL += " dc_ttl_amt_bal     = ?,";
		updateSQL += " adjust_dr_cnt   = ?,";
		updateSQL += " adjust_dr_amt   = ?,";
		updateSQL += " dc_adjust_dr_amt   = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_pgm   = ? ";
		whereStr = "where p_seqno = ? ";
		whereStr += " and curr_code='901' "; /* 20170814 sunny add */
		setDouble(1, hAcctAcctJrnlBal);
		setDouble(2, hAcctAcctJrnlBal);
		setDouble(3, hAcctTempUnbillInterest);
		setDouble(4, hAcctTempUnbillInterest);
		setDouble(5, hAcctMinPayBal);
		setDouble(6, hAcctMinPayBal);
		setDouble(7, hAcctTtlAmtBal);
		setDouble(8, hAcctTtlAmtBal);
		setDouble(9, hAcctAdjustDrCnt);
		setDouble(10, hAcctAdjustDrAmt);
		setDouble(11, hAcctAdjustDrAmt);
		setString(12, hAcnoModPgm);
		setString(13, hAcnoPSeqno);

		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_acct_curr not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void selectPtrActcode() throws Exception {
		hDebtItemOrderNormal = "";
		hDebtItemOrderBackDate = "";
		hDebtItemOrderRefund = "";
		hDebtItemClassNormal = "";
		hDebtItemClassBackDate = "";
		hDebtItemClassRefund = "";
		hDebtAcctItemCname = "";
		hTempRate = 0;

		sqlCmd = "select ";
		sqlCmd += "item_order_normal, ";
		sqlCmd += "item_order_back_date, ";
		sqlCmd += "item_order_refund, ";
		sqlCmd += "item_class_normal, ";
		sqlCmd += "item_class_back_date, ";
		sqlCmd += "item_class_refund, ";
		sqlCmd += "chi_long_name, ";
		sqlCmd += "decode(inter_rate_code,'1',?,'2',?,'3',?,";
		sqlCmd += "       '4',?,'5',?,'6',?,0) temp_rate ";
		sqlCmd += "from   ptr_actcode ";
		sqlCmd += "where  acct_code = ? ";
		setDouble(1, hAgenRevolvingInterest1);
		setDouble(2, hAgenRevolvingInterest2);
		setDouble(3, hAgenRevolvingInterest3);
		setDouble(4, hAgenRevolvingInterest4);
		setDouble(5, hAgenRevolvingInterest5);
		setDouble(6, hAgenRevolvingInterest6);
		setString(7, hTempAcctCode);

		extendField = "ptr_actcode.";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_actcode not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hDebtItemOrderNormal = getValue("ptr_actcode.item_order_normal");
			hDebtItemOrderBackDate = getValue("ptr_actcode.item_order_back_date");
			hDebtItemOrderRefund = getValue("ptr_actcode.item_order_refund");
			hDebtItemClassNormal = getValue("ptr_actcode.item_class_normal");
			hDebtItemClassBackDate = getValue("ptr_actcode.item_class_back_date");
			hDebtItemClassRefund = getValue("ptr_actcode.item_class_refund");
			hDebtAcctItemCname = getValue("ptr_actcode.chi_long_name");
			hTempRate = getValueDouble("ptr_actcode.temp_rate");
		}
	}

	/***********************************************************************/
	void selectPtrActgeneral() throws Exception {
		hAgenRevolvingInterest1 = 0;
		hAgenRevolvingInterest2 = 0;
		hAgenRevolvingInterest3 = 0;
		hAgenRevolvingInterest4 = 0;
		hAgenRevolvingInterest5 = 0;
		hAgenRevolvingInterest6 = 0;

		sqlCmd = "select ";
		sqlCmd += "revolving_interest1, ";
		sqlCmd += "revolving_interest2, ";
		sqlCmd += "revolving_interest3, ";
		sqlCmd += "revolving_interest4, ";
		sqlCmd += "revolving_interest5, ";
		sqlCmd += "revolving_interest6 ";
		sqlCmd += "from   ptr_actgeneral_n ";
		sqlCmd += "fetch first 1 row only ";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hAgenRevolvingInterest1 = getValueDouble("revolving_interest1");
			hAgenRevolvingInterest2 = getValueDouble("revolving_interest2");
			hAgenRevolvingInterest3 = getValueDouble("revolving_interest3");
			hAgenRevolvingInterest4 = getValueDouble("revolving_interest4");
			hAgenRevolvingInterest5 = getValueDouble("revolving_interest5");
			hAgenRevolvingInterest6 = getValueDouble("revolving_interest6");
		}
	}

	/***********************************************************************/
	void insertActDebt() throws Exception {
		hDebtReferenceSeq = "";

		sqlCmd = "select substr(?,3,2)||substr(to_char(bil_postseq.nextval,'0000000000'),4,8) reference_seq ";
		sqlCmd += "from dual ";
		setString(1, hBusiBusinessDate);

		extendField = "h_debt.";

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hDebtReferenceSeq = getValue("h_debt.reference_seq");
		}

		selectCrdCard1();

		daoTable = "act_debt";
		extendField = daoTable + ".";
		setValue(extendField + "reference_no", hDebtReferenceSeq);
		setValue(extendField + "p_seqno", hAcnoPSeqno);
		setValue(extendField + "acno_p_seqno", hAcnoPSeqno); // phopho add
		setValue(extendField + "curr_code", "901");
		setValue(extendField + "acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
		setValue(extendField + "post_date", hBusiBusinessDate);
		setValue(extendField + "item_order_normal", hDebtItemOrderNormal);
		setValue(extendField + "item_order_back_date", hDebtItemOrderBackDate);
		setValue(extendField + "item_order_refund", hDebtItemOrderRefund);
		setValue(extendField + "item_class_normal", hDebtItemClassNormal);
		setValue(extendField + "item_class_back_date", hDebtItemClassBackDate);
		setValue(extendField + "item_class_refund", hDebtItemClassRefund);
		setValue(extendField + "acct_month", hWdayNextAcctMonth);
		setValue(extendField + "stmt_cycle", hWdayStmtCycle);
		setValue(extendField + "bill_type", "OSSG");
		setValue(extendField + "txn_code", "AI");
		setValueDouble(extendField + "beg_bal", hTempRateAmt);
		setValueDouble(extendField + "dc_beg_bal", hTempRateAmt);
		setValueDouble(extendField + "end_bal", hTempRateAmt);
		setValueDouble(extendField + "dc_end_bal", hTempRateAmt);
		setValueDouble(extendField + "d_avail_bal", hTempRateAmt);
		setValueDouble(extendField + "dc_d_avail_bal", hTempRateAmt);
		setValue(extendField + "card_no", hDebtCardNo);
		setValue(extendField + "acct_code", hTempAcctCode);
//        setValue("acct_item_cname", h_debt_acct_item_cname);  //no column
		setValue(extendField + "interest_date", hBusiBusinessDate);
		setValue(extendField + "purchase_date", hBusiBusinessDate);
//        setValue("acquire_date", h_busi_business_date);  //no column
		setValue(extendField + "mod_user", hAcnoModUser);
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", hAcnoModPgm);

		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_debt duplicate!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void selectCrdCard1() throws Exception {
		hDebtCardNo = "";

		sqlCmd = "select ";
		sqlCmd += "card_no ";
		sqlCmd += "from crd_card ";
//        sqlCmd += "where gp_no = ?  ";
		sqlCmd += "where p_seqno = ? ";
		sqlCmd += "fetch first 1 row only ";
		setString(1, hAcnoPSeqno);

		extendField = "crd_card_1.";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_crd_card not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hDebtCardNo = getValue("crd_card_1.card_no");
		}
	}

	/***********************************************************************/
	void selectPtrWorkday() throws Exception {
		hWdayStmtCycle = "";
		hWdayThisCloseDate = "";
		hWdayNextAcctMonth = "";
		hWdayThisAcctMonth = "";

		sqlCmd = "select ";
		sqlCmd += "stmt_cycle, ";
		sqlCmd += "this_close_date, ";
		sqlCmd += "next_acct_month, ";
		sqlCmd += "this_acct_month ";
		sqlCmd += "from  ptr_workday ";
		sqlCmd += "where stmt_cycle = ? ";
		setString(1, hAcnoStmtCycle);

		extendField = "ptr_workday.";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hWdayStmtCycle = getValue("ptr_workday.stmt_cycle");
			hWdayThisCloseDate = getValue("ptr_workday.this_close_date");
			hWdayNextAcctMonth = getValue("ptr_workday.next_acct_month");
			hWdayThisAcctMonth = getValue("ptr_workday.this_acct_month");
		}

		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_workday error!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void selectActAcag() throws Exception {
		sqlCmd = "select ";
		sqlCmd += "pay_amt, ";
		sqlCmd += "acct_month, ";
		sqlCmd += "rowid as rowid ";
		sqlCmd += "from act_acag ";
		sqlCmd += "where p_seqno = ? ";
		sqlCmd += "order by acct_month ";
		setString(1, hAcnoPSeqno);

		extendField = "act_acag.";

		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hAcagPayAmt = getValueDouble("act_acag.pay_amt", i);
			hAcagAcctMonth = getValue("act_acag.acct_month", i);
			hAcagRowid = getValue("act_acag.rowid", i);

			if (hDebtEndBal == 0)
				break;

			if (hDebtEndBal >= hAcagPayAmt) {
				hDebtEndBal -= hAcagPayAmt;
				hAcagPayAmt = 0;
				deleteActAcag();
			} else {
				hAcagPayAmt -= hDebtEndBal;
				hDebtEndBal = 0;
				updateActAcag();
			}
		}
		if (hDebtEndBal != 0) {
			showLogMessage("I", "", "p_seqno[" + hAcnoPSeqno + "] mp balance in act_acct do not match ");
			showLogMessage("I", "", "mp detail in act_acag, SERIOUS ERROR!!! ");
		}
	}

	/***********************************************************************/
	void updateActAcag() throws Exception {
		updateActAcagCurr();
		hAcagModSeqno = comcr.getModSeq();

		daoTable = "act_acag";
		updateSQL = " pay_amt   = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm   = ?,";
		updateSQL += " mod_seqno = ? ";
		whereStr = "where rowid = ? ";
		setDouble(1, hAcagPayAmt);
		setString(2, hAcnoModUser);
		setString(3, hAcnoModPgm);
		setDouble(4, hAcnoModSeqno);
		setRowId(5, hAcagRowid);

		updateTable();
	}

	/***********************************************************************/
	void updateActAcagCurr() throws Exception {
		daoTable = "act_acag_curr";
		updateSQL = " pay_amt    = ?,";
		updateSQL += " dc_pay_amt = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_pgm   = ?,";
		whereStr = "where p_seqno = ? ";
		whereStr += "and  acct_month = ? ";
		setDouble(1, hAcagPayAmt);
		setDouble(2, hAcagPayAmt);
		setString(3, hAcnoModPgm);
		setString(4, hAcnoPSeqno);
		setString(5, hAcagAcctMonth);

		updateTable();
	}

	/***********************************************************************/
	void deleteActAcag() throws Exception {
		deleteActAcagCurr();
		daoTable = "act_acag";
		whereStr = "where rowid = ? ";
		setRowId(1, hAcagRowid);

		deleteTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("delete_act_acag not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void deleteActAcagCurr() throws Exception {
		daoTable = "act_acag_curr";
		whereStr = "where p_seqno = ? ";
		whereStr += "and   acct_month = ? ";
		setString(1, hAcnoPSeqno);
		setString(1, hAcagAcctMonth);

		deleteTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("delete_act_acag_curr not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertCycPyaj(int classCode) throws Exception {
		dateTime();
		daoTable = "cyc_pyaj";
		extendField = daoTable + ".";
		setValue(extendField + "p_seqno", hAcnoPSeqno);
		setValue(extendField + "curr_code", "901");
		setValue(extendField + "acct_type", hAcnoAcctType);
//        setValue("acct_key", h_acno_acct_key);  //no column
		setValue(extendField + "class_code", classCode == 1 ? "A" : "B");
		setValue(extendField + "payment_date", hBusiBusinessDate);
		setValueDouble(extendField + "payment_amt", hDebtEndBal * -1);
		setValueDouble(extendField + "dc_payment_amt", hDebtEndBal * -1);
		setValue(extendField + "payment_type", "CD01");
		setValue(extendField + "stmt_cycle", hAcnoStmtCycle);
		setValue(extendField + "settle_flag", "U");
		setValue(extendField + "reference_no", hDebtReferenceSeq);
		setValue(extendField + "fee_flag", "Y");

		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_cyc_pyaj duplicate!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void novouchReportRtn() throws Exception {
		String hBusinssChiDate = "";
		String cDate = "";
		String szTmp = "";

		temstr = String.format("%s/reports/COL_B022_NOVOU_%4.4s", comc.getECSHOME(), hBusiBusinessDate.substring(4));
		temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);

		buf = "";
		buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 26);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "報表名稱  :COL_B022_NOVOU", 3);
		buf = comcr.insertStrCenter(buf, "催收(科目明細)現金制未起帳金額報表", 80);
		buf = comcr.insertStr(buf, "頁次:", 68);
		szTmp = String.format("%04d", 1);
		buf = comcr.insertStr(buf, szTmp, 73);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		sqlCmd = "select substr(to_char(to_number(?)- 19110000,'0000000'),2,7) businss_chi_date ";
		sqlCmd += "from ptr_businday ";
		setString(1, comcr.hSystemVouchDate);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hBusinssChiDate = getValue("businss_chi_date");
		}

		buf = "";
		buf = comcr.insertStr(buf, "單    位:", 1);
		buf = comcr.insertStr(buf, "009", 11);
		buf = comcr.insertStr(buf, "交易日期:", 58);
		cDate = String.format("%3.3s年%2.2s月%2.2s日", hBusinssChiDate.substring(0, 3), hBusinssChiDate.substring(3, 5),
				hBusinssChiDate.substring(5));
		buf = comcr.insertStr(buf, cDate, 68);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "調 整 性 質  :", 10);
		buf = comcr.insertStr(buf, " D 檔", 25);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "幣        別 :", 10);
		buf = comcr.insertStr(buf, "TWD", 25);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		for (int i = 0; i < 78; i++)
			buf += "=";
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "年 費 金 額   :", 10);
		szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchAf);
		buf = comcr.insertStr(buf, szTmp, 30);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		buf = "";
		buf = comcr.insertStr(buf, "預 現 手 續 費:", 10);
		szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchCf);
		buf = comcr.insertStr(buf, szTmp, 30);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "其 它 手 續 費:", 10);
		szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchPf);
		buf = comcr.insertStr(buf, szTmp, 30);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "帳 外 息 金 額:", 10);
		szTmp = comcr.commFormat("1$,3$,3$,3$.2$", amtNovouchAi);
		buf = comcr.insertStr(buf, szTmp, 30);
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		for (int i = 0; i < 78; i++)
			buf += "=";
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

		comc.writeReport(temstr, lpar1);
		comcr.lpRtn("COL_D_VOUCH", "");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ColB022 proc = new ColB022();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
