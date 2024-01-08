/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112-05-03  V1.00.00  Alex        initial                                  * 
*  112-09-25  V1.00.01  Wilson      調整卡特指判斷邏輯                                                                                *
*****************************************************************************/
package Tsc;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class TscB023 extends BaseBatch {
	private final String progname = "凍結/特指 報送/取消 悠遊卡 拒絕授權 112/09/25 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	
	String acnoPSeqno = "";
	String sendReason = "";
	String blockReason = "";
	String specStatus = "";
	String specDelDate = "";
	String restoreDate = "";
	String cardNo = "";
	String blockCond = "";
	String specCond = "";
	
	int totalCnt = 0;
	int totalCnt2 = 0;
	int totalCnt3 = 0;
	int totalCnt4 = 0;
	
	public static void main(String[] args) {
		TscB023 proc = new TscB023();
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : TscB023 [business_date]");
			errExit(1);
		}
		
		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}
						
		dateTime();
		
		totalCnt = 0;
		totalCnt2 = 0;
		totalCnt3 = 0;
		totalCnt4 = 0;
		
		//--查詢排除凍結、特指條件
		selectRskCommParm();
		
		//--凍結、戶特指
		selectRskAcnoLog1();
		
		//--卡特指
		selectRskAcnoLog2();
		
		//--解凍、解除戶特指
		selectRskAcnoLog3();
		
		//--解除卡特指
		selectRskAcnoLog4();
		
		showLogMessage("I", "", String.format("凍結、戶特指 [%d] 筆\n", totalCnt));
		showLogMessage("I", "", String.format("卡特指 [%d] 筆\n", totalCnt2));
		showLogMessage("I", "", String.format("解凍、解除戶特指 [%d] 筆\n", totalCnt3));
		showLogMessage("I", "", String.format("解除卡特指 [%d] 筆\n", totalCnt4));
		
		endProgram(totalCnt+totalCnt2+totalCnt3+totalCnt4);
	}
	
	void selectRskCommParm() throws Exception {
		
		String sql1 = " select "
					+ " decode(bkec_block_cond,'','','Y',bkec_block_reason) as block_cond , "
					+ " decode(auto_block_cond,'','','Y',auto_block_reason) as spec_cond "
					+ " from rsk_comm_parm where parm_type ='W_TSCM2250' and seq_no = '10' "
					;
		
		sqlSelect(sql1);
		if(sqlNrow <=0)
			return ;
		
		String tmpBlockCond = "" , tmpSpecCond = "";
		int blockLengh = 0 , specLength = 0 , num=0;
		tmpBlockCond = colSs("block_cond");
		tmpSpecCond = colSs("spec_cond");
		blockLengh = tmpBlockCond.length() / 2;
		specLength = tmpSpecCond.length() / 2;		
		for (int ii = 0; ii < blockLengh; ii++) {			
		    if (ii == 0) {		    	
		    	blockCond += tmpBlockCond.substring(num, num + 2);
		    } else {		    	
		    	blockCond += "," + tmpBlockCond.substring(num, num + 2);
		    }
		    num = num + 2;
		}
		
		num = 0;
		
		for (int ii = 0; ii < specLength; ii++) {			
		    if (ii == 0) {		    	
		    	specCond += tmpSpecCond.substring(num, num + 2);
		    } else {		    	
		    	specCond += "," + tmpSpecCond.substring(num, num + 2);
		    }
		    num = num + 2;
		}
		
	}
	
	void selectRskAcnoLog1() throws Exception {
		
		sqlCmd = " select acno_p_seqno , block_reason||block_reason2||block_reason3||block_reason4||block_reason5 as block_reason , "
			   + " uf_spec_status(spec_status,spec_del_date) as spec_status , spec_del_date "
			   + " from rsk_acnolog where 1=1 and log_date = ? and kind_flag = 'A' and log_type ='3' ";			   
		
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			acnoPSeqno = colSs("acno_p_seqno");
			blockReason = colSs("block_reason");
			specStatus = colSs("spec_status");
			specDelDate = colSs("spec_del_date");
			
			//--確認該筆log是否為當日最後一筆
			if(checkCcaCardAcct(1) == false)
				continue;
			
			totalCnt++;
			
			if(specStatus.isEmpty() == false) {
				sendReason = "30";
//				restoreDate = specDelDate;
				restoreDate = "";
			}
							
			if(blockReason.isEmpty() == false) {
				sendReason = "20";
				restoreDate = "";
			}
							
			selectTscCard1();						
		}
		
		closeCursor();
	}
	
	void selectRskAcnoLog2() throws Exception {
		
		sqlCmd = "select card_no , spec_status , spec_del_date from rsk_acnolog where log_date = ? and kind_flag ='C' and log_type ='6' and log_reason = 'A' ";
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			cardNo = colSs("card_no");
			specStatus = colSs("spec_status");
			specDelDate = colSs("spec_del_date");
			
			if(checkCcaCardBase(1) == false)
				continue ;
			
			totalCnt2++;
			
			sendReason = "30";
//			restoreDate = specDelDate;
			restoreDate = "";
			
			selectTscCard2();			
		}
		
		closeCursor();		
	}
	
	void selectRskAcnoLog3() throws Exception {
		
		sqlCmd = " select acno_p_seqno , block_reason||block_reason2||block_reason3||block_reason4||block_reason5 as block_reason , "
			   + " uf_spec_status(spec_status,spec_del_date) as spec_status , spec_del_date "
			   + " from rsk_acnolog where 1=1 and log_date = ? and kind_flag = 'A' and log_type ='4' ";			   
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			acnoPSeqno = colSs("acno_p_seqno");
			blockReason = colSs("block_reason");
			specStatus = colSs("spec_status");
			specDelDate = colSs("spec_del_date");
			
			if(checkCcaCardAcct(2) == false)
				continue;
			
			totalCnt3++;
			
			selectTscCard3();			
		}
		
		closeCursor();		
	}
	
	void selectRskAcnoLog4() throws Exception {
		
		sqlCmd = "select card_no , spec_status , spec_del_date from rsk_acnolog where log_date = ? and kind_flag ='C' and log_type ='6' and log_reason = 'D' ";
		setString(1,hBusiDate);
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			cardNo = colSs("card_no");
			specStatus = colSs("spec_status");
			specDelDate = colSs("spec_del_date");
			
			if(checkCcaCardBase(2) == false)
				continue ;
			
			totalCnt4++;
			
			sendReason = "30";			
			
			selectTscCard4();			
		}
		
		closeCursor();		
	}
	
	boolean checkCcaCardBase(int idx) throws Exception {
		String cardSpecStatus = "";
		boolean cardLbSend = false ;
		
		String sql1 = "select spec_status as db_spec_status , spec_del_date as db_spec_del_date from cca_card_base where card_no = ? ";
		
		sqlSelect(sql1,new Object[] {cardNo});
		
		if(sqlNrow <=0)
			return false ;
		
		if(colEq("db_spec_status",specStatus) == false)
			return false ;
		
		if(colEq("db_spec_del_date",specDelDate) == false)
			return false ;
		
		
		cardSpecStatus = colSs("db_spec_status");
		
		if(idx == 1) {
			if(cardSpecStatus.isEmpty() == false) {
				if(commString.ssIn(cardSpecStatus, specCond) == false)
					cardLbSend = true;
			}
		}
		else {
			if(selectTscRmActauth2() == true)
				cardLbSend = true;
		}		
		
		if(cardLbSend == false)
			return false ;
		
		return true;
	}
	
	boolean selectTscRmActauth2() throws Exception {
		
		String sql1 = "select count(*) db_cnt from tsc_rm_actauth where send_reason = '30' and card_no = ? ";
		sqlSelect(sql1,new Object[] {cardNo});
				
		if(colNum("db_cnt") <= 0)
			return false;
						
		return true;		
	}
	
	boolean checkCcaCardAcct(int idx) throws Exception {
		String dbBlockReason1 = "" , dbBlockReason2 = "" , dbBlockReason3 = "" , dbBlockReason4 = "" , dbBlockReason5 = "";		
		String dbSpecStatus = "";
		boolean lbSend = false ;
		String sql1 = " select block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as db_block_reason , "
					+ " uf_spec_status(spec_status,spec_del_date) as db_spec_status , spec_del_date as db_spec_del_date , "
					+ " block_reason1 , block_reason2 , block_reason3 , block_reason4 , block_reason5  "
					+ " from cca_card_acct where acno_p_seqno = ? ";
		
		sqlSelect(sql1,new Object[] {acnoPSeqno});
		if(sqlNrow <=0)
			return false;
		
		//--log 和 目前狀態不同 , 不是最後一筆
		if(colEq("db_block_reason",blockReason) == false)
			return false;
		
		//--log 和 目前狀態不同 , 不是最後一筆
		if(colEq("db_spec_status",specStatus) == false)
			return false;
		
		if(colEq("db_spec_del_date",specDelDate) == false)
			return false;
		
		dbBlockReason1 = colSs("block_reason1");
		dbBlockReason2 = colSs("block_reason2");
		dbBlockReason3 = colSs("block_reason3");
		dbBlockReason4 = colSs("block_reason4");
		dbBlockReason5 = colSs("block_reason5");
		dbSpecStatus = colSs("db_spec_status");
		
		if(idx == 1) {
			if(dbBlockReason1.isEmpty() == false) {
				if(commString.ssIn(dbBlockReason1, blockCond) == false)
					lbSend = true;
			}
			
			if(dbBlockReason2.isEmpty() == false) {
				if(commString.ssIn(dbBlockReason2, blockCond) == false)
					lbSend = true;
			}
			
			if(dbBlockReason3.isEmpty() == false) {
				if(commString.ssIn(dbBlockReason3, blockCond) == false)
					lbSend = true;
			}
			
			if(dbBlockReason4.isEmpty() == false) {
				if(commString.ssIn(dbBlockReason4, blockCond) == false)
					lbSend = true;
			}
			
			if(dbBlockReason5.isEmpty() == false) {
				if(commString.ssIn(dbBlockReason5, blockCond) == false)
					lbSend = true;
			}
			
			if(dbSpecStatus.isEmpty() == false) {
				if(commString.ssIn(dbSpecStatus, specCond) == false)
					lbSend = true;
			}
		}
		else {
			if(selectTscRmActauth1() == true)
				lbSend = true;
		}
				
		if(lbSend == false)
			return false ;
		
		return true;		
	}
	
	boolean selectTscRmActauth1() throws Exception {
		
		String sql1 = "select count(*) db_cnt from tsc_rm_actauth where send_reason in ('20','30') and acno_p_seqno = ? ";
		sqlSelect(sql1,new Object[] {acnoPSeqno});
				
		if(colNum("db_cnt") <= 0)
			return false;
						
		return true;		
	}
			
	void selectTscCard1() throws Exception {
		String sql1 = "select tsc_card_no from tsc_card where card_no in (select card_no from crd_card where acno_p_seqno = ? ) and current_code ='0' ";
		sqlSelect(sql1,new Object[] {acnoPSeqno});
		if(sqlNrow <=0)
			return ;
		
		String tscCardNo = "";
		for(int ii=0 ; ii<sqlNrow ; ii++) {
			tscCardNo = "";
			tscCardNo = colSs(ii,"tsc_card_no");
			insertTscRmActauth(tscCardNo);
		}
	}
	
	void selectTscCard2() throws Exception {
		String sql1 = "select tsc_card_no from tsc_card where card_no = ? and current_code ='0' ";
		sqlSelect(sql1,new Object[] {cardNo});
		if(sqlNrow <=0)
			return ;
		
		String tscCardNo = "";
		tscCardNo = colSs("tsc_card_no");
		insertTscRmActauth(tscCardNo);
	}
	
	void selectTscCard3() throws Exception {
		String sql1 = "select tsc_card_no , card_no from tsc_card where card_no in (select card_no from crd_card where acno_p_seqno = ? ) and current_code ='0' ";
		sqlSelect(sql1,new Object[] {acnoPSeqno});
		if(sqlNrow <=0)
			return ;
		
		String tscCardNo = "" , cardNo = "";
		int tscCardCnt = sqlNrow;
		for(int ii=0 ; ii<tscCardCnt ; ii++) {
			tscCardNo = "";
			cardNo = "";
			tscCardNo = colSs(ii,"tsc_card_no");
			cardNo = colSs(ii,"card_no");
			if(checkCard(cardNo,tscCardNo) == false)
				continue;
			updateTscRmActAuth(tscCardNo);
		}
	}
	
	void selectTscCard4() throws Exception {
		String sql1 = "select tsc_card_no from tsc_card where card_no = ? and current_code ='0' ";
		sqlSelect(sql1,new Object[] {cardNo});
		if(sqlNrow <=0)
			return ;
		
		String tscCardNo = "";
		tscCardNo = colSs("tsc_card_no");
		
		String sql2 = " select block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as db_block_reason , "
				+ " uf_spec_status(spec_status,spec_del_date) as db_spec_status "
				+ " from cca_card_acct where acno_p_seqno in (select acno_p_seqno from crd_card where card_no = ?) ";
		
		sqlSelect(sql2,new Object[] {cardNo});
		
		if(sqlNrow <=0)
			return ;
		
		if(colEmpty("db_block_reason") == false)
			return ;
		
		if(colEmpty("db_spec_status") == false)
			return ;				
		
		updateTscRmActAuth(tscCardNo);
	}
	
	boolean checkCard(String aCardNo , String aTscCardNo) throws Exception {
		
		String sql1 = "select A.spec_status , B.current_code from cca_card_base A join crd_card B on A.card_no = B.card_no where A.card_no = ? ";
		sqlSelect(sql1,new Object[] {aCardNo});
		
		if(sqlNrow <=0 )
			return false;
		
		//--仍有卡特指不傳送
		if(colEmpty("spec_status") == false)
			return false ;
		
		//--卡片已失效不傳送
//		if(colEq("current_code","0") == false)
//			return false ;
		
		//--未傳送過拒絕代行名單
		String sql2 = "select count(*) as db_cnt from tsc_rm_actauth where tsc_card_no = ? and rm_send_date <> ''  and risk_class ='57' and rt_send_date = '' ";
		sqlSelect(sql2,new Object[] {aTscCardNo});
						
		if(sqlNrow <=0)
			return false;
				
		if(colNum("db_cnt") <= 0)
			return false;
						
		return true;		
	}
	
	int insertTscRmActauth(String aTscCardNo) throws Exception {
		
		sqlCmd = "insert into tsc_rm_actauth ";
        sqlCmd += "(send_reason,";
        sqlCmd += "risk_class,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "remove_date,";
        sqlCmd += "restore_date,";
        sqlCmd += "card_no,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "new_end_date,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "'57',";
        sqlCmd += "b.tsc_card_no,";
        sqlCmd += "to_char(sysdate,'yyyymmdd'),";
        sqlCmd += "?,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "b.new_end_date,";
        sqlCmd += "sysdate,";
        sqlCmd += "? ";
        sqlCmd += "from crd_card a,tsc_card b where a.card_no  = b.card_no and b.tsc_card_no = ? ";
        setString(1, sendReason);
        setString(2, restoreDate);
        setString(3, javaProgram);
        setString(4, aTscCardNo);
        insertTable();
        sqlCmd = ""; /* initail */
        if (dupRecord.equals("Y")) {
            return (1);
        }
        return (0);		
	}
	
	void updateTscRmActAuth(String aTscCardNo) throws Exception {
        daoTable = "tsc_rm_actauth";
        updateSQL = " restore_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm = ? ";
        whereStr = "where tsc_card_no = ?  ";
        whereStr += "  and risk_class  = '57'  ";
        whereStr += "  and send_reason in ('20', '30') ";
        setString(1, javaProgram);
        setString(2, aTscCardNo);
        updateTable();        			
	}
	
	void initData() {
		acnoPSeqno = "";
		sendReason = "";
		blockReason = "";
		specStatus = "";
		specDelDate = "";
		restoreDate = "";
		cardNo = "";
	}
	
}
