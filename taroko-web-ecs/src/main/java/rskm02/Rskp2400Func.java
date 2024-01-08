package rskm02;

import busi.FuncAction;
import busi.func.AcnoBlockReason;
import busi.func.OutgoingBlock;

public class Rskp2400Func extends FuncAction {
	String blockReason4 = "" , acnoPSeqno = "" , reviewDate = "" , idPSeqno = "";
	double cardAcctIdx = 0.0;
	String oldBlockReason1 = "" , oldBlockReason2 = "" , oldBlockReason3 = "" , oldBlockReason5 = "";
	String block15 = ""  ;	
	busi.func.OutgoingBlock ooOutgo = null;
	public String errorDesc = "";
	@Override
	public void dataCheck() {
		blockReason4 = varsStr("block_reason4");
		acnoPSeqno = varsStr("acno_p_seqno");
		reviewDate = varsStr("review_date");
		
		if(empty(blockReason4)) {
			errmsg("執行凍結碼: 不可空白");
			return ;
		}
		
		if(empty(acnoPSeqno)) {
			errmsg("帳戶流水號: 不可空白");
			return ;
		}
		
		getCardAcctIdx();				
		block15 = oldBlockReason1 + oldBlockReason2 + oldBlockReason3 + blockReason4 + oldBlockReason5;
	}
	
	void getCardAcctIdx() {
		
		String sql1 = " select card_acct_idx , block_reason1 , block_reason2 , block_reason3 , block_reason5 , id_p_seqno from cca_card_acct where acno_p_seqno = ? ";
		sqlSelect(sql1,new Object[] {acnoPSeqno});
		
		if(sqlRowNum > 0 ) {
			cardAcctIdx = colNum("card_acct_idx");
			oldBlockReason1 = colStr("block_reason1");
			oldBlockReason2 = colStr("block_reason2"); 
			oldBlockReason3 = colStr("block_reason3");
			oldBlockReason5 = colStr("block_reason5");
			idPSeqno = colStr("id_p_seqno");
		}
		
	}
	
	@Override
	public int dbInsert() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataProc() {
		msgOK();
		
		dataCheck();
		
		updateCcaCardAcct();
		if(rc!=1)
			return rc;
		
		AcnoBlockReason ooAclg = new AcnoBlockReason();
		ooAclg.setConn(wp);
		if (ooAclg.ccaM2040Update(cardAcctIdx) != 1) {
			errmsg(ooAclg.getMsg());
			errorDesc = getMsg();
			return rc;
		}
										
		updateCrdCard();				
		
		if(rc!=1)
			return rc;
		
		insertSmsMsgDtl();
		if(rc!=1)
			return rc;
		
		updateRskReviewBlock();
		
		if(rc!=1)
			return rc;
		
		cardOutgoing();
		
		return rc;
	}
	
	void updateCcaCardAcct() {
		msgOK();
		
		strSql = " update cca_card_acct set block_status = 'Y' , block_reason2 =:block_reason2 , block_date = to_char(sysdate,'yyyymmdd') , "
			   + " unblock_date = '' , block_sms_flag ='Y' , mod_time = sysdate , mod_pgm =:mod_pgm , mod_user =:mod_user , "
			   + " mod_seqno = nvl(mod_seqno,0)+1 where acno_p_seqno =:acno_p_seqno  ";
		
		setString("block_reason2",blockReason4);
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		setString("acno_p_seqno",acnoPSeqno);
		
		sqlExec(strSql);
		
		if(sqlRowNum <=0) {
			errmsg("update cca_card_acct error");
			errorDesc = getMsg();
			return ;
		}
		
	}	
	
	void cardOutgoing() {
		ooOutgo = new OutgoingBlock();
		ooOutgo.setConn(wp);
		ooOutgo.isCallAutoAuth = false;
		// -VD有可能凍結,特指-
		// 2018-1107 if(ib_debit) return ;
		String lsVdFlag = "N";						
		ooOutgo.cardOutgoingUpdate(lsVdFlag, acnoPSeqno, blockReason4);
	}
	
	private void updateCrdCard() {
		strSql = " update crd_card set block_code =?, block_date =to_char(sysdate,'yyyymmdd') ," + commSqlStr.setModxxx(modUser, modPgm)
			   + " where acno_p_seqno =?";

		setString2(1, block15);		
		setString2(2, acnoPSeqno);
		sqlExec(strSql);
		if (sqlRowNum < 0) {
			errmsg("update crd_card error");
			errorDesc = getMsg();
		}
		return;
	}
	
	void updateRskReviewBlock() {
		
		strSql = " update rsk_review_block set apr_flag ='Y' , apr_user =:apr_user , apr_date = to_char(sysdate,'yyyymmdd') , "
			   + " mod_pgm =:mod_pgm , mod_seqno = nvl(mod_seqno,0)+1 where acno_p_seqno =:acno_p_seqno and review_date =:review_date ";
		
		setString("mod_pgm",wp.modPgm());
		setString("apr_user",wp.loginUser);
		setString("acno_p_seqno",acnoPSeqno);
		setString("review_date",reviewDate);
		
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update rsk_review_block error");
			errorDesc = getMsg();
		}
		return;
		
	}
	
	void insertSmsMsgDtl() {
		msgOK();
		
		String sql1 = " select count(*) as db_card_cnt from crd_card where current_code ='0' and acno_p_seqno = ? ";
		sqlSelect(sql1,new Object[] {acnoPSeqno});
		
		//--沒有有效卡不發送
		if(colNum("db_card_cnt") <=0) {			
			return ;
		}
		
		//--前協不發送
		String sql2 = " select count(*) as db_cnt2 from col_liac_nego where id_p_seqno = ? ";
		sqlSelect(sql2,new Object[] {idPSeqno});
		if(colNum("db_cnt2") > 0) {			
			return ;
		}
		
		busi.func.SmsMsgDetl ooSms = new busi.func.SmsMsgDetl();
	    ooSms.setConn(wp);
	    rc = ooSms.rskP2400(idPSeqno, "RSKP2400",blockReason4);
	    if(rc <=0)
	    	errorDesc = ooSms.hsms.errorDesc;
	}
	
}
