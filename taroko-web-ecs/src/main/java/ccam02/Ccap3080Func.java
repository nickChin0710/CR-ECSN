/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/09/27  V1.00.01  Alex        覆核時覆核人員不可和異動人員相同                                                 *
 ******************************************************************************/
package ccam02;

import busi.FuncAction;

public class Ccap3080Func extends FuncAction {
	String errorDesc = "";
	String scrAcctType = "" , scrCorpPSeqno = "", scrIdnoPSeqno = "" , scrAudCode = "" , srcStartDate = "" , srcEndDate = "" , srcWithSupCard = "";
	String srcAcnoPSeqno = "";
	String dbAcctType = "" , dbCorpPSeqno = "" , dbIdnoPSeqno = "" , dbAudCode = "" , dbStartDate = "" , dbEndDate = "" , dbWithSupCard = "";
	String dbModUser = "" , dbAcnoPSeqno = "" , dbCrtDate = "" , dbCrtUser = "" ;
	@Override
	public void dataCheck() {
		scrAcctType = varsStr("acct_type");
		scrCorpPSeqno = varsStr("corp_p_seqno");
		scrIdnoPSeqno = varsStr("idno_p_seqno");
		scrAudCode = varsStr("mod_audcode");
		srcStartDate = varsStr("start_date");
		srcEndDate = varsStr("end_date");
		srcWithSupCard = varsStr("with_sup_card");
		srcAcnoPSeqno = varsStr("acno_p_seqno");
		errorDesc = "";
		
		if(selectDbData() == false) {
			errorDesc = "未覆核資料已不存在 , 請重新讀取 ! ";
			rc = -1;
			return ;
		}
		
		if(dbCrtUser.equals(wp.loginUser)) {
			errorDesc = "覆核人員和異動人員相同 , 不可覆核";
			rc = -1;
			return ;
		}
		
		if(scrAcctType.equals(dbAcctType) == false) {
			errorDesc = "帳戶類別已被異動 , 請重新讀取 !";
			rc = -1;
			return ;
		}
		
		if(scrCorpPSeqno.equals(dbCorpPSeqno) == false) {
			errorDesc = "商務卡卡號已被異動 , 請重新讀取 !";
			rc = -1 ;
			return ;
		}
		
		if(scrIdnoPSeqno.equals(dbIdnoPSeqno) == false) {
			errorDesc = "主卡人已被異動 , 請重新讀取 !";
			rc = -1 ;
			return ;
		}
		
		if(srcWithSupCard.equals(dbWithSupCard) == false) {
			errorDesc = "是否含附卡旗標已被異動 , 請重新讀取 !";
			rc = -1 ; 
			return ;
		}
		
		if(srcStartDate.equals(dbStartDate) == false) {
			errorDesc = "有效日期(起) 已被異動 , 請重新讀取 !";
			rc = -1 ; 
			return ;
		}
		
		if(srcEndDate.equals(dbEndDate) == false) {
			errorDesc = "有效日期(迄) 已被異動 , 請重新讀取 !";
			rc = -1 ; 
			return ;		
		}
		
		if(scrAudCode.equals(dbAudCode) == false) {
			errorDesc = "處理指示已被異動 , 請重新讀取 !";
			rc = -1 ; 
			return ;
		}
		
		if(srcAcnoPSeqno.equals(dbAcnoPSeqno) == false) {
			errorDesc = "商務卡卡號已被異動 , 請重新讀取 !";
			rc = -1;
			return ;
		}
		
	}
	
	//--查詢執行當下資料庫內資料 , 避免該筆資料被其他人異動
	boolean selectDbData() {
		String sql1 = "select acct_type , corp_p_seqno , idno_p_seqno , with_sup_card , start_date , end_date , acno_p_seqno , mod_audcode , mod_user , "
				+ " crt_date , crt_user from cca_vip_t where acct_type = ? and corp_p_seqno = ? and idno_p_seqno = ? "
				;
		
		sqlSelect(sql1,new Object[] {scrAcctType,scrCorpPSeqno,scrIdnoPSeqno});
		if(sqlRowNum <=0)
			return false ;
		
		dbAcctType = colStr("acct_type");
		dbCorpPSeqno = colStr("corp_p_seqno");
		dbIdnoPSeqno = colStr("idno_p_seqno");
		dbWithSupCard = colStr("with_sup_card");
		dbStartDate = colStr("start_date");
		dbEndDate = colStr("end_date");
		dbAudCode = colStr("mod_audcode");
		dbAcnoPSeqno = colStr("acno_p_seqno");
		dbModUser = colStr("mod_user");
		dbCrtDate = colStr("crt_date");
		dbCrtUser = colStr("crt_user");
		
		return true;
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
		dataCheck();
		if(rc!=1)
			return rc;
		
		if("A".equals(dbAudCode)) {
			insertVip();
		}	else if("U".equals(dbAudCode)) {
			updateVip();
		}	else if("D".equals(dbAudCode)) {
			deleteVip();
		}	else	{
			errorDesc = "處理指示並不是新增/修改/刪除 , 請重新鍵檔該筆資料";
			rc = -1;
		}
		
		if(rc != 1)
			return rc;
		
		insertVipLog();
		if(rc !=1)
			return rc;
		
		deleteVipTemp();		
		
		return rc;
	}
	
	void insertVip() {
		msgOK();
		strSql = " insert into cca_vip (acct_type , corp_p_seqno , idno_p_seqno , with_sup_card , start_date , end_date , "
				+ " crt_date , crt_user , apr_date , apr_user , mod_user , mod_time , mod_pgm , acno_p_seqno ) values ( "
				+ " :acct_type , :corp_p_seqno , :idno_p_seqno , :with_sup_card , :start_date , :end_date , :crt_date , :crt_user , "
				+ " to_char(sysdate,'yyyymmdd') , :apr_user , :mod_user , sysdate , :mod_pgm , :acno_p_seqno ) "
				;
		
		setString("acct_type",dbAcctType);
		setString("corp_p_seqno",dbCorpPSeqno);
		setString("idno_p_seqno",dbIdnoPSeqno);
		setString("with_sup_card",dbWithSupCard);
		setString("start_date",dbStartDate);
		setString("end_date",dbEndDate);
		setString("crt_date",dbCrtDate);
		setString("crt_user",dbCrtUser);
		setString("apr_user",wp.loginUser);
		setString("mod_user",dbModUser);
		setString("mod_pgm",wp.modPgm());
		setString("acno_p_seqno",dbAcnoPSeqno);
		
		sqlExec(strSql);
		
		if(sqlRowNum <= 0) {
			errorDesc = "insert cca_vip error !";
			return ;
		}
		
		return ;		
	}
	
	void updateVip() {
		msgOK();
		
		strSql = " update cca_vip set start_date =:start_date , end_date =:end_date , "
				+ " mod_time = sysdate , mod_user =:mod_user , apr_date = to_char(sysdate,'yyyymmdd') , "
				+ " apr_user =:apr_user where acct_type =:acct_type and corp_p_seqno =:corp_p_seqno "
				+ " and idno_p_seqno =:idno_p_seqno ";
		
		setString("start_date",dbStartDate);
		setString("end_date",dbEndDate);
		setString("mod_user",dbModUser);
		setString("apr_user",wp.loginUser);
		setString("acct_type",dbAcctType);
		setString("corp_p_seqno",dbCorpPSeqno);
		setString("idno_p_seqno",dbIdnoPSeqno);
		
		sqlExec(strSql);
		
		if(sqlRowNum <= 0) {
			errorDesc = "update cca_vip error !";
			return ;
		}
		
		return ;				
	}
	
	void deleteVip() {
		msgOK();
		
		strSql = " delete cca_vip where acct_type =:acct_type and corp_p_seqno =:corp_p_seqno and idno_p_seqno =:idno_p_seqno ";
		setString("acct_type",dbAcctType);
		setString("corp_p_seqno",dbCorpPSeqno);
		setString("idno_p_seqno",dbIdnoPSeqno);
		
		sqlExec(strSql);
		
		if(sqlRowNum <= 0) {
			errorDesc = "delete cca_vip error ";
			return ;
		}
		
		return ;
	}
	
	void deleteVipTemp() {
		msgOK();
		
		strSql = " delete cca_vip_t where acct_type =:acct_type and corp_p_seqno =:corp_p_seqno and idno_p_seqno =:idno_p_seqno ";
		
		setString("acct_type",dbAcctType);
		setString("corp_p_seqno",dbCorpPSeqno);
		setString("idno_p_seqno",dbIdnoPSeqno);
		
		sqlExec(strSql);
		
		if(sqlRowNum <= 0) {
			errorDesc = "delete cca_vip_t error ";
			return ;
		}
		return ;
	}
	
	void insertVipLog() {
		msgOK();
		
		strSql = " insert into cca_vip_log (acct_type , corp_p_seqno , idno_p_seqno , with_sup_card , start_date , "
				+ " end_date , mod_user , mod_time , mod_pgm , mod_audcode , acno_p_seqno ) values ( "
				+ " :acct_type , :corp_p_seqno , :idno_p_seqno , :with_sup_card , :start_date , :end_date , :mod_user , "
				+ " sysdate , :mod_pgm , :mod_audcode , :acno_p_seqno ) "
				;
		
		setString("acct_type",dbAcctType);
		setString("corp_p_seqno",dbCorpPSeqno);
		setString("idno_p_seqno",dbIdnoPSeqno);
		setString("with_sup_card",dbWithSupCard);
		setString("start_date",dbStartDate);
		setString("end_date",dbEndDate);
		setString("mod_user",dbModUser);
		setString("mod_pgm",wp.modPgm());
		setString("mod_audcode",dbAudCode);
		setString("acno_p_seqno",dbAcnoPSeqno);
		
		sqlExec(strSql);
		
		if(sqlRowNum <=0) {
			errorDesc = "insert cca_vip_log error ";
			return ;
		}
		
		return ;
	}
	
}
