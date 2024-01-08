/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-10-25  v1.00.00  Yang Bo    Sync code from mega                        *
* 111-11-16  V1.00.01  Simon      cancel autopay_indicator='3'               *
******************************************************************************/
package actp01;
/**
 * 2022-0415	JH		mt9363: mod_user
 * */
import busi.FuncAction;

public class Actp2050Func extends FuncAction {
	public String modVersion() { return "v22.0415 mt9636"; }

	String lsPSeqno = "";	
	//--0703
	String isOsign = "" , isOsym = "" , isOeym = "" , isReason = "" ;
	String isOsign2 = "" ,  isOsym2 = "" , isOeym2 = "" , isReason2 = "" , isRevolReasonO ="";
	double idcOrate = 0 , idcOrate2 = 0 ;
	boolean lbIntChg1 = false ;
	String Errmsg= "";
	//--
	@Override
	public void dataCheck() {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int dataProc0701() throws Exception{
		msgOK();
		strSql = " update act_acno set "
				 + " no_penalty_flag =:no_penalty_flag , "
				 + " no_penalty_s_month =:no_penalty_s_month , "
				 + " no_penalty_e_month =:no_penalty_e_month , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm ='actp2050' , "
				 + " mod_seqno =nvl(mod_seqno,0)+1 "
				 + " where acct_type =:acct_type "
				 + " and acct_key =:acct_key "				 
				 ;
		
		var2ParmStr("no_penalty_flag");
		var2ParmStr("no_penalty_s_month");
		var2ParmStr("no_penalty_e_month");
		setString("mod_user",wp.loginUser);
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error ");
			return rc;
		}	else	{
			delDual();
		}
		
		return rc;
	}
	
	public int delDual() throws Exception{
		msgOK();
		strSql = " delete act_dual where 1=1 "+commSqlStr.whereRowid(varsStr2("rowid"));
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("delete act_dual error !");
		}
		return rc;
	}
	
	public int dataProc0702() throws Exception{
		msgOK();
		if(selectPseqno()==false){
			errmsg("資料主檔 [acno] 資料不存在, 無法更新");
			return rc;
		}
		
		strSql = " update act_acno set "
				 + " autopay_indicator =:autopay_indicator , "
			 //+ " autopay_fix_amt =:autopay_fix_amt , "
			 //+ " autopay_rate =:autopay_rate , "
				 + " autopay_acct_e_date =:autopay_acct_e_date , "
				 + " mod_user =:mod_user , "
				 + " mod_time =:mod_time , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno =:mod_seqno "
				 + " where acct_type =:acct_type "
				 + " and acct_key =:acct_key "				 
				 ;
		
		var2ParmStr("autopay_indicator");
	//var2ParmNum("autopay_fix_amt");
	//var2ParmNum("autopay_rate");
		var2ParmStr("autopay_acct_e_date");
		var2ParmStr("mod_user");
		var2ParmStr("mod_time");
		var2ParmStr("mod_pgm");
		var2ParmNum("mod_seqno");
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		sqlExec(strSql);
		
		if(sqlRowNum<=0){
			errmsg("資料[act_acno] 更新失敗!; acct_key="+varsStr2("acct_key"));
			return rc;
		}
		
		strSql = " update act_acct_curr set "
				 + " autopay_indicator =:autopay_indicator , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm = 'actp2050' , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where p_seqno =:p_seqno "
				 + " and curr_code ='901' "
				 ;
		var2ParmStr("autopay_indicator");
		var2ParmStr("mod_user");
		setString("p_seqno",lsPSeqno);
		sqlExec(strSql);
		
		if(sqlRowNum<=0){
			errmsg("資料[act_acct_curr] 更新失敗!; acct_key="+varsStr2("acct_key"));
			return rc ;
		}
		
		delDual();
		if(sqlRowNum<=0){
			errmsg("刪除[act_dual] 失敗");			
		}
		
		return rc;
	}
	
	boolean selectPseqno() throws Exception{
		String sql1 = " select "
						+ " p_seqno "
						+ " from act_acno "
						+ " where acct_type = ? "
						+ " and acct_key = ? "
						;
		sqlSelect(sql1,new Object[]{varsStr2("acct_type"),varsStr2("acct_key")});
		if(sqlRowNum<=0){			
			return false;
		}
		lsPSeqno = colStr("p_seqno");
		return true;
	}
	
	public int dataProc0703() throws Exception{
		msgOK();
		if(selectActAcno0703()==false){
			errmsg("select ACT_ACNO error");
			return rc;
		}

		String pCorpPSeqno = "";
    pCorpPSeqno = colStr("corp_p_seqno");
 	  if(!empty(pCorpPSeqno)) {
		 	 if(selectCrdCorp0703(pCorpPSeqno)==false){
		    	errmsg("select CRD_CORP error");
		   	  return rc;
		   } 
		}
    
		//--因actm log_data 最後面塞入原始利率加減碼時可能為空值會被trim掉故加了1個/所以長度變為122
		if(varsStr2("log_data").length()!=122){
			errmsg("舊資料異動格式, 請重新修改");
			return rc;
		}
		
		if(dataCheck0703()==false){
//			errmsg(Errmsg);
			errmsg("經辦調整時, 帳戶利率減碼值與目前帳戶值不同; 請重新修改");
			return rc ;
		}
		
		updateActAcno0703();
		if(rc!=1){
			errmsg("Update act_acno.利率加減碼 error ");
			return rc;
		}
		insertAcctMark();
		if(rc!=1){
			errmsg("Insert act_acct_mrk error");
			return rc;
		}
		
		if(lbIntChg1){
			insertActIntHst();
			if(rc!=1){
				errmsg("Insert act_int_hst.利率加減碼 error");
				return rc;
			}
		}
		
		delDual();
		if(rc!=1){
			errmsg("Delete act_dual error");
		}
		
		return rc;
	}
	
	boolean selectActAcno0703() throws Exception{

		String sql1 = " select "
						+ " *  "						
						+ " from act_acno "
						+ " where acct_type = ? "
						+ " and acct_key = ? "
						;
		sqlSelect(sql1,new Object[]{varsStr2("acct_type"),varsStr2("acct_key")});
		
		if(sqlRowNum<=0)	return false ;
		
		isOsign = colStr("revolve_int_sign");
		idcOrate = colNum("revolve_int_rate"); 
		isOsym = colStr("revolve_rate_s_month");
		isOeym = colStr("revolve_rate_e_month");
		isReason = colStr("revolve_reason");
		isOsign2 = colStr("revolve_int_sign_2");
		idcOrate2 = colNum("revolve_int_rate_2");
		isOsym2 = colStr("revolve_rate_s_month_2");
		isOeym2 = colStr("revolve_rate_e_month_2");
		isReason2 = colStr("revolve_reason_2");
		isRevolReasonO =  isReason;
		
		lbIntChg1 = false;
		
		return true ;
	}
	
	boolean selectCrdCorp0703(String corpPSeqno ) throws Exception{
		String lsSql = "select corp_no from crd_corp where corp_p_seqno = :corp_p_seqno";
		setString("corp_p_seqno", corpPSeqno);
		sqlSelect(lsSql);
		if(sqlRowNum<=0)	return false ;
		
		return true ;
	}

	boolean dataCheck0703() throws Exception{
		//--old
		
		if(!varEq("O_sign",isOsign) ||
			varsNum("O_rate")!=idcOrate ||
			!varEq("O_sym",isOsym) ||
			!varEq("O_eym",isOeym) ||
			!varEq("O_sign_2",isOsign2) ||
			varsNum("O_rate_2")!=idcOrate2 ||
			!varEq("O_sym_2",isOsym2) ||
			!varEq("O_eym_2",isOeym2)){
//			Errmsg = " A:"+vars_ss("O_sign")+" | "+is_Osign
//					 + " B:"+vars_num("O_rate")+" | "+idc_Orate
//					 + " C:"+vars_ss("O_sym")+" | "+is_Osym
//					 + " D:"+vars_ss("O_eym")+" | "+is_Oeym
//					 + " E:"+vars_ss("O_sign_2")+" | "+is_Osign2
//					 + " F:"+vars_num("O_rate_2")+" | "+idc_Orate2
//					 + " G:"+vars_ss("O_sym_2")+" | "+is_Osym2
//					 + " H:"+vars_ss("O_eym_2")+" | "+is_Oeym2;
//			ddd("A:"+vars_ss("O_sign")+" | "+is_Osign);
//			ddd("B:"+vars_ss("O_rate")+" | "+idc_Orate);
//			ddd("C:"+vars_ss("O_sym")+" | "+is_Osym);
//			ddd("D:"+vars_ss("O_eym")+" | "+is_Oeym);
//			ddd("E:"+vars_ss("O_sign_2")+" | "+is_Osign2);
//			ddd("F:"+vars_ss("O_rate_2")+" | "+idc_Orate2);
//			ddd("G:"+vars_ss("O_sym_2")+" | "+is_Osym2);
//			ddd("H:"+vars_ss("O_eym_2")+" | "+is_Oeym2);			
			return false ;
		}
		
		if(!varEq("sign_1",isOsign) ||
			varsNum("int_rate_1")!=idcOrate ||
			!varEq("ex_start_date_1",isOsym) ||
			!varEq("ex_end_date_1",isOeym) ){
			lbIntChg1 =true;
			isReason = "2";			
		}
		
		if(!varEq("sign_2",isOsign) ||
			varsNum("int_rate_2")!=idcOrate ||
			!varEq("ex_start_date_2",isOsym) ||
			!varEq("ex_end_date_2",isOeym) ){			
			isReason2 = "2";			
		}
		
		return true;
	}
	
	public int updateActAcno0703() throws Exception{
		strSql = " update act_acno set "
				 + " prev_int_sign =:prev_int_sign , "
				 + " prev_int_rate =:prev_int_rate , "
				 + " prev_rate_s_month =:prev_rate_s_month , "
				 + " prev_rate_e_month =:prev_rate_e_month , "
				 + " revolve_int_sign =:revolve_int_sign , "
				 + " revolve_int_rate =:revolve_int_rate , "
				 + " revolve_rate_s_month =:revolve_rate_s_month , "
				 + " revolve_rate_e_month =:revolve_rate_e_month , "
				 + " revolve_reason =:revolve_reason , "
				 + " revolve_int_sign_2 =:revolve_int_sign_2 , "
				 + " revolve_int_rate_2 =:revolve_int_rate_2 , "
				 + " revolve_rate_s_month_2 =:revolve_rate_s_month_2 , "
				 + " revolve_rate_e_month_2 =:revolve_rate_e_month_2 , "
				 + " revolve_reason_2 =:revolve_reason_2 , "
				 + " mod_user =:mod_user , "
				 + " mod_time =sysdate , "
				 + " mod_pgm ='actp2050' , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where acct_type=:acct_type "
				 + " and acct_key=:acct_key "
				 ;
		
		setString("prev_int_sign",isOsign);
		setString("prev_int_rate",""+idcOrate);
		setString("prev_rate_s_month",isOsym);
		setString("prev_rate_e_month",isOeym);
		setString("revolve_int_sign",varsStr2("sign_1"));
		setString("revolve_int_rate",varsStr2("int_rate_1"));
		setString("revolve_rate_s_month",varsStr2("ex_start_date_1"));
		setString("revolve_rate_e_month",varsStr2("ex_end_date_1"));
		setString("revolve_reason",isReason);
		setString("revolve_int_sign_2",varsStr2("sign_2"));
		setString("revolve_int_rate_2",varsStr2("int_rate_2"));
		setString("revolve_rate_s_month_2",varsStr2("ex_start_date_2"));
		setString("revolve_rate_e_month_2",varsStr2("ex_end_date_2"));
		setString("revolve_reason_2",isReason2);
		setString("mod_user",wp.loginUser);
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("Update act_acno.利率加減碼 error ");
		}
		return rc;
	}
	
	public int insertAcctMark() throws Exception{
		msgOK();
		strSql = " insert into act_acct_mrk ( "
				 + " mod_time ,"
				 + " mod_pgm ,"
				 + " mod_user ,"
				 + " mod_audcode ,"
				 + " acct_type ,"
				 + " p_seqno ,"
				 + " corp_p_seqno ,"
			   + " corp_no ,"
				 + " id_p_seqno ,"
				 + " revolve_int_sign ,"
				 + " revolve_int_rate ,"
				 + " revolve_int_sign2 , "
				 + " revolve_int_rate2 "
				 + " ) values ( "
				 + " sysdate , "
				 + " :mod_pgm , "
				 + " :mod_user , "
				 + " 'U' , "
				 + " :acct_type , "
				 + " :acct_p_seqno , "
				 + " :corp_p_seqno , "
			   + " :corp_no , "
				 + " :id_p_seqno , "
				 + " :revolve_int_sign , "
				 + " :revolve_int_rate , "
				 + " :revolve_int_sign2 , "
				 + " :revolve_int_rate2 "
				 + " )"
				 ;

		String lsChgUser =varsStr2("chg_date");
		if (empty(lsChgUser)) {
			lsChgUser =wp.loginUser;
		}

		setString("mod_pgm","actp2050");
		setString("mod_user",lsChgUser);
		col2ParmStr("acct_type");
		col2ParmStr("acct_p_seqno","p_seqno");
		col2ParmStr("corp_p_seqno");
		col2ParmStr("corp_no");
		col2ParmStr("id_p_seqno");
		setString("revolve_int_sign",varsStr2("sign_1"));
		setString("revolve_int_rate",varsStr2("int_rate_1"));
		setString("revolve_int_sign2",varsStr2("sign_2"));
		setString("revolve_int_rate2",varsStr2("int_rate_2"));
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("Insert act_acct_mrk error");			
		}
		return rc;
	}
	
	public int insertActIntHst() throws Exception{
		msgOK();
		strSql = " insert into act_int_hst ( "
				 + " p_seqno ,"
				 + " crt_date ,"
				 + " crt_time ,"
				 + " from_type ,"
				 + " acct_type ,"
				 + " stmt_cycle ,"
				 + " revolve_int_sign ,"
				 + " revolve_int_rate ,"
				 + " revolve_rate_s_month ,"
				 + " revolve_rate_e_month ,"
				 + " revolve_int_sign2 ,"
				 + " revolve_int_rate2 ,"
				 + " revolve_rate_s_month2 ,"
				 + " revolve_rate_e_month2 ,"
				 + " mod_user ,"
				 + " mod_time ,"
				 + " mod_pgm ,"
				 + " update_date ,"
				 + " update_user ,"
				 + " revol_reason_o ,"
				 + " int_sign_o ,"
				 + " int_rate_o ,"
				 + " int_yymm_s_o ,"
				 + " int_yymm_e_o "
				 + " ) values ( "
				 + " :p_seqno ,"
				 + " to_char(sysdate,'yyyymmdd') ,"
				 + " to_char(sysdate,'hh24miss') ,"
				 + " '1' ,"
				 + " :acct_type ,"
				 + " :stmt_cycle ,"
				 + " :revolve_int_sign ,"
				 + " :revolve_int_rate ,"
				 + " :revolve_rate_s_month ,"
				 + " :revolve_rate_e_month ,"
				 + " :revolve_int_sign2 ,"
				 + " :revolve_int_rate2 ,"
				 + " :revolve_rate_s_month2 ,"
				 + " :revolve_rate_e_month2 ,"
				 + " :mod_user ,"
				 + " sysdate ,"
				 + " 'actp2050' ,"
				 + " :update_date ,"
				 + " :update_user ,"
				 + " :revol_reason_o ,"
				 + " :int_sign_o ,"
				 + " :int_rate_o ,"
				 + " :int_yymm_s_o ,"
				 + " :int_yymm_e_o "
				 + " )"
				 ;

		String lsChgUser =varsStr2("chg_user");
		String lsChgDate =varsStr2("chg_date");
		if (empty(lsChgDate)) lsChgDate=wp.sysDate;
		if (empty(lsChgUser)) lsChgUser=wp.loginUser;

		col2ParmStr("p_seqno");
		col2ParmStr("acct_type");
		col2ParmStr("stmt_cycle");
		setString("revolve_int_sign",varsStr2("sign_1"));
		setString("revolve_int_rate",varsStr2("int_rate_1"));
		setString("revolve_rate_s_month",varsStr2("ex_start_date_1"));
		setString("revolve_rate_e_month",varsStr2("ex_end_date_1"));		
		setString("revolve_int_sign2",varsStr2("sign_2"));
		setString("revolve_int_rate2",varsStr2("int_rate_2"));
		setString("revolve_rate_s_month2",varsStr2("ex_start_date_2"));
		setString("revolve_rate_e_month2",varsStr2("ex_end_date_2"));	
		setString("mod_user",wp.loginUser);
		setString("update_date", lsChgDate);
		setString("update_user", lsChgUser);
		setString("revol_reason_o",isRevolReasonO);
		setString("int_sign_o",isOsign);
		setString("int_rate_o",""+idcOrate);
		setString("int_yymm_s_o",isOsym);
		setString("int_yymm_e_o",isOeym);
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("Insert act_int_hst.利率加減碼 error");
		}		
		return rc;
	}
			
	public int dataProc0704() throws Exception{
		msgOK();
		if(selectPseqno()==false){
			errmsg("0704-資料主檔 [acno] 資料不存在, 無法更新!");
			return rc;
		}
		if (varsStr2("curr_code").equals("901")) {
  		updateActAcno0704();
	  	if(rc!=1)	return rc;
		}
		
		if(varEq("mp_flag","1")){
			updateActAcctCurr();
			if(rc!=1)	return rc;
		}
		
		delDual();
		
		return rc;
	}
	
	public int updateActAcno0704() throws Exception{
		msgOK();
		strSql = " update act_acno set "
				 + " mp_flag =:mp_flag , "				 
				 ;
		if(varEq("mp_flag","0")){
			strSql += " min_pay_rate =:min_pay_rate , "
					  + " min_pay_rate_s_month =:min_pay_rate_s_month , "
					  + " min_pay_rate_e_month =:min_pay_rate_e_month , "
					  ;
		}	else	{
			strSql += " min_pay_rate = min_pay_rate , "
					  + " min_pay_rate_s_month = min_pay_rate_s_month , "
					  + " min_pay_rate_e_month = min_pay_rate_e_month , "
					  ;
		}
		
		if(varEq("mp_flag","1")){
			strSql += " mp_1_amt =:mp_1_amt , "
					  + " mp_1_s_month =:mp_1_s_month , "
					  + " mp_1_e_month =:mp_1_e_month , "					  
					  ;
		}	else	{
			strSql += " mp_1_amt = mp_1_amt , "
					  + " mp_1_s_month = mp_1_s_month , "
					  + " mp_1_e_month = mp_1_e_month , "
					  ;
		}
		
		strSql += " mod_user =:mod_user , "
				  + " mod_time = sysdate , "
				  + " mod_pgm =:mod_pgm , "
				  + " mod_seqno =nvl(mod_seqno,0)+1 "
				  + " where p_seqno =:p_seqno "
				  ;
		var2ParmStr("mp_flag");
		var2ParmNum("min_pay_rate");
		var2ParmStr("min_pay_rate_s_month");
		var2ParmStr("min_pay_rate_e_month");
		var2ParmNum("mp_1_amt");
		var2ParmStr("mp_1_s_month");
		var2ParmStr("mp_1_e_month");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp2050");
		setString("p_seqno",lsPSeqno);
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error ");
		}
		
		return rc;
	}
	
	public int updateActAcctCurr() throws Exception{
		msgOK();
		strSql = " update act_acct_curr set "
				 + " mp_1_amt =:mp_1_amt , "
				 + " mp_1_s_month =:mp_1_s_month , "
				 + " mp_1_e_month =:mp_1_e_month , "
				 + " apr_flag = 'Y' , "
				 + " apr_date = to_char(sysdate,'yyyymmdd') , "
				 + " apr_user =:apr_user , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "				 
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where p_seqno =:p_seqno "
				 + " and curr_code =:curr_code "
				 ;
		
		var2ParmNum("mp_1_amt");
		var2ParmStr("mp_1_s_month");
		var2ParmStr("mp_1_e_month");
		setString("apr_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp2050");
		setString("p_seqno",lsPSeqno);
		var2ParmNvl("curr_code","901");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("act_acct_curr");
		}
		
		return rc;
	}
	
	public int dataProc0705() throws Exception{
		msgOK();		
		if(selectPseqno()==false){
			errmsg("資料主檔 [acno] 資料不存在, 無法更新!");
			return rc;
		}
		
		strSql = " update act_acno set "
				 + " special_stat_code =:special_stat_code , "
				 + " special_stat_s_month =:special_stat_s_month , "
				 + " special_stat_e_month =:special_stat_e_month , "
				 + " special_stat_division =:special_stat_division , "
				 + " special_comment =:special_comment , "
				 + " special_stat_fee =:special_stat_fee , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where acct_type =:acct_type"
				 + " and acct_key =:acct_key "
				 ;
		
		var2ParmStr("special_stat_code");
		var2ParmStr("special_stat_s_month");
		var2ParmStr("special_stat_e_month");
		var2ParmStr("special_stat_division");
		var2ParmStr("special_comment");
		var2ParmNum("special_stat_fee");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp2050");
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error !");
			return rc;
		}
		
		delDual();
		
		return rc;
	}
	
	public int dataProc0706() throws Exception{
		msgOK();		
		if(selectPseqno()==false){
			errmsg("資料主檔 [acno] 資料不存在, 無法更新!");
			return rc;
		}
		
		strSql = " update act_acno set "
				 + " stat_send_paper =:stat_send_paper , "
				 + " stat_send_internet =:stat_send_internet , "
				 + " stat_send_fax =:stat_send_fax , "
				 + " stat_send_s_month =:stat_send_s_month , "
				 + " stat_send_e_month =:stat_send_e_month , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno =nvl(mod_seqno,0)+1 "
				 + " where acct_type =:acct_type "
				 + " and acct_key =:acct_key "
				 ;
		
		var2ParmStr("stat_send_paper");
		var2ParmStr("stat_send_internet");
		var2ParmStr("stat_send_fax");
		var2ParmStr("stat_send_s_month");
		var2ParmStr("stat_send_e_month");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp2050");
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error ");
			return rc;
		}
		
		delDual();
		
		return rc;
	}
	
	public int dataProc0707() throws Exception{
		msgOK();		
		if(selectPseqno()==false){
			errmsg("資料主檔 [acno] 資料不存在, 無法更新!");
			return rc;
		}
		strSql = " update act_acno set "
				 + " stat_unprint_flag =:stat_unprint_flag , "
				 + " stat_unprint_s_month =:stat_unprint_s_month , "
				 + " stat_unprint_e_month =:stat_unprint_e_month , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno =nvl(mod_seqno,)+1 "
				 + " where acct_type =:acct_type "
				 + " and acct_key =:acct_key "
				 ;
		
		var2ParmStr("stat_unprint_flag");
		var2ParmStr("stat_unprint_s_month");
		var2ParmStr("stat_unprint_e_month");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp2050");
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error ");
			return rc;
		}
		
		delDual();
		
		return rc;
	}
	
	public int dataProc0708() throws Exception{
		msgOK();		
		if(selectPseqno()==false){
			errmsg("資料主檔 [acno] 資料不存在, 無法更新!");
			return rc;
		}
		
		strSql = " update act_acno set "
				 + " rc_use_b_adj =rc_use_indicator , "
				 + " rc_use_indicator =:rc_use_indicator , "
				 + " rc_use_s_date =:rc_use_s_date , "
				 + " rc_use_e_date =:rc_use_e_date , "
				 + " mod_user =:mod_user , "
				 + " mod_time =sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where acct_type =:acct_type "
				 + " and acct_key =:acct_key "
				 ;
		
		var2ParmStr("rc_use_b_adj");
		var2ParmStr("rc_use_indicator");
		var2ParmStr("rc_use_s_date");
		var2ParmStr("rc_use_e_date");
		setString("mod_user");
		setString("mod_pgm","actp2050");
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error ");
			return rc;
		}
		
		delDual();
				
		return rc;
	}
	
	public int dataProc0709() throws Exception{
		msgOK();		
		if(selectPseqno()==false){
			errmsg("資料主檔 [acno] 資料不存在, 無法更新!");
			return rc;
		}
		
		strSql = " update act_acno set "
				 + " no_cancel_debt_flag =:no_cancel_debt_flag , "
				 + " no_cancel_debt_s_date =:no_cancel_debt_s_date , "
				 + " no_cancel_debt_e_date =:no_cancel_debt_e_date , "
				 + " mod_user =:mod_user , "
				 + " mod_time =sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where acct_type =:acct_type "
				 + " and acct_key =:acct_key  "
				 ;
		
		var2ParmStr("no_cancel_debt_flag");
		var2ParmStr("no_cancel_debt_s_date");
		var2ParmStr("no_cancel_debt_e_date");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp2050");
		
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error ");
			return rc;
		}
		
		delDual();
				
		return rc;
	}
	
	public int dataProc0710() throws Exception{
		msgOK();		
		if(selectPseqno()==false){
			errmsg("資料主檔 [acno] 資料不存在, 無法更新!");
			return rc;
		}
		
		updateActAcctCurr0710();
		if(rc!=1)	return rc;
		
		if(varEq("curr_code","901")){
			updateActAcno0710();
			if(rc!=1)	return rc;
		}
		
		delDual();
		
		return rc;
	}
	
	public int updateActAcctCurr0710() throws Exception {
		msgOK();
		strSql = " update act_acct_curr set "
				 + " no_interest_flag =:no_interest_flag , "
				 + " no_interest_s_month =:no_interest_s_month , "
				 + " no_interest_e_month =:no_interest_e_month , "
				 + " apr_user =:apr_user , "
				 + " apr_date = to_char(sysdate,'yyyymmdd') , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where p_seqno =:p_seqno "				 
				 ;
		
		var2ParmNvl("no_interest_flag","N");
		var2ParmStr("no_interest_s_month");
		var2ParmStr("no_interest_e_month");
		setString("apr_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp_2050");
		setString("p_seqno",lsPSeqno);
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acct_curr.no_interest error");
		}		
		return rc;
	}
	
	public int updateActAcno0710() throws Exception{
		msgOK();
		
		strSql = " update act_acno set "
				 + " no_interest_flag = :no_interest_flag , "
				 + " no_interest_s_month = :no_interest_s_month , "
				 + " no_interest_e_month = :no_interest_e_month , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_pgm = :mod_pgm , "
				 + " mod_seqno = nvl(mod_seqno,0)+1 "
				 + " where acct_type =:acct_type "
				 + " and acct_key =:acct_key "
				 ;
		
		var2ParmNvl("no_interest_flag","N");
		var2ParmStr("no_interest_s_month");
		var2ParmStr("no_interest_e_month");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm","actp_2050");
		var2ParmStr("acct_type");
		var2ParmStr("acct_key");
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acno error !");
		}
		return rc;
	}
	
}
