package ccam02;

import busi.FuncAction;

public class Ccam5273Func extends FuncAction {
	String smsPriority = "";
	@Override
	public void dataCheck() {
		if(ibAdd) {
			smsPriority = wp.itemStr("kk_sms_priority");
			if(checkInsertDup() == false) {
				errmsg("此優先序已存在");
				return ;
			}
									
		}	else
			smsPriority = wp.itemStr("sms_priority");
		
		if(ibAdd || ibUpdate) {
			if(wp.itemEq("spec_list", "Y") && "0".equals(smsPriority) == false) {
				errmsg("指定名單優先序必須為 0");
				return ;
			}
						
			if(wp.itemEq("spec_list", "Y")) {
				//--指定名單不可勾選檢核
				if(wp.itemEq("area_type", "0") == false ||
					wp.itemEq("cond_success","N") == false ||
					wp.itemEq("cond_country", "0") == false ||
					wp.itemEq("cond_curr", "0") == false ||
					wp.itemEq("cond_bin", "0") == false ||
					wp.itemEq("cond_group", "0") == false ||
					wp.itemEq("cond_mcht", "0") == false ||
					wp.itemEq("cond_mcc", "0") == false ||
					wp.itemEq("cond_pos", "0") == false ||
					wp.itemEq("cond_trans_type", "0") == false ||
					wp.itemEq("cond_resp_code", "0") == false ||
					wp.itemEq("cond_amt", "Y")||
					wp.itemEq("cond_or_and1", "Y") ||
					wp.itemEq("cond_cnt2", "Y") ||
					wp.itemEq("cond_or_and2", "Y") ||
					wp.itemEq("cond_cnt1", "Y")) {
					errmsg("指定名單不可勾選檢核條件");
					return ;
				}					
			}	else	{
				//--非指定名單至少勾選1個檢核
				if(wp.itemEq("area_type", "0") &&
					wp.itemEq("cond_success","N") &&
					wp.itemEq("cond_country", "0") &&
					wp.itemEq("cond_curr", "0") &&
					wp.itemEq("cond_bin", "0") &&
					wp.itemEq("cond_group", "0") &&
					wp.itemEq("cond_mcht", "0") &&
					wp.itemEq("cond_mcc", "0") &&
					wp.itemEq("cond_pos", "0") &&
					wp.itemEq("cond_trans_type", "0") &&
					wp.itemEq("cond_resp_code", "0") &&
					wp.itemEq("cond_amt", "Y") == false &&
					wp.itemEq("cond_or_and1", "Y") == false &&
					wp.itemEq("cond_cnt2", "Y") == false &&
					wp.itemEq("cond_or_and2", "Y") == false &&
					wp.itemEq("cond_cnt1", "Y") == false) {
					errmsg("非指定名單至少勾選1個檢核");
					return ;
				}
			}
			
		}
		
		if(wp.itemEmpty("msg_id")) {
			errmsg("簡訊代號不可空白");
			return ;
		}
		
		if(checkSms() == false) {
			errmsg("簡訊代號 不存在 , 請重新設定簡訊代號");
			return ;
		}
		
		if(ibAdd)
			return ;
		
		sqlWhere = " where 1=1 and sms_priority = ? and nvl(mod_seqno,0) = ? ";
		Object[] parms = new Object []{smsPriority , wp.itemNum("mod_seqno")};
		
		if (this.isOtherModify("sms_msg_parm", sqlWhere, parms)) {			
		    return;
		}		
	}
	
	boolean checkSms() {
		
		String sql1 = "select count(*) as db_cnt from sms_msg_content where msg_id = ? ";
		sqlSelect(sql1,new Object[] {wp.itemStr("msg_id")});
		
		if(colNum("db_cnt") > 0)
			return true;
		
		return false ;
	}
	
	boolean checkInsertDup() {
		
		String sql1 = "select count(*) as db_cnt from sms_msg_parm where 1=1 and sms_priority = ? ";
		sqlSelect(sql1,new Object[] {smsPriority});
		
		if(colNum("db_cnt") > 0 )
			return false;
		
		return true ;
	}
	
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc !=1)
			return rc;
		
		strSql = " insert into sms_msg_parm ( "
				+ " sms_priority , msg_id , sms_remark , spec_list , area_type , cond_success , cond_country , cond_curr ,"
				+ " cond_bin ,cond_group , cond_mcht , cond_mcc , cond_pos , cond_trans_type , cond_resp_code ,"
				+ " cond_amt , tx_amt , cond_cnt1 , tx_day , tx_dat_cnt , cond_cnt2 , tx_hour ,"
				+ " tx_hour_cnt , cond_or_and1 , cond1_amt , cond_or_and2 , cond2_amt , "
				+ " crt_date , crt_user , apr_date , apr_user , mod_user , mod_time , mod_pgm , mod_seqno "
				+ " ) values ( "
				+ " :sms_priority , :msg_id , :sms_remark , :spec_list , :area_type , :cond_success , :cond_country , :cond_curr ,"
				+ " :cond_bin ,:cond_group , :cond_mcht , :cond_mcc , :cond_pos , :cond_trans_type , :cond_resp_code ,"
				+ " :cond_amt , :tx_amt , :cond_cnt1 , :tx_day , :tx_dat_cnt , :cond_cnt2 , :tx_hour ,"
				+ " :tx_hour_cnt , :cond_or_and1 , :cond1_amt , :cond_or_and2 , :cond2_amt , "
				+ " to_char(sysdate,'yyyymmdd') , :crt_user , to_char(sysdate,'yyyymmdd') , :apr_user , "
				+ " :mod_user , sysdate , :mod_pgm , 1 )"
				;
		
		setString("sms_priority",smsPriority);
		item2ParmStr("msg_id");
		item2ParmStr("sms_remark");
		item2ParmNvl("spec_list","N");
		item2ParmNvl("area_type","3");
		item2ParmNvl("cond_success","N");
		item2ParmNvl("cond_country","0");
		item2ParmNvl("cond_curr","0");
		item2ParmNvl("cond_bin","0");
		item2ParmNvl("cond_group","0");
		item2ParmNvl("cond_mcht","0");
		item2ParmNvl("cond_mcc","0");
		item2ParmNvl("cond_pos","0");
		item2ParmNvl("cond_trans_type","0");
		item2ParmNvl("cond_resp_code","0");
		item2ParmNvl("cond_amt","0");
		item2ParmNum("tx_amt");
		item2ParmNvl("cond_cnt1","N");
		item2ParmNum("tx_day");
		item2ParmNum("tx_dat_cnt");
		item2ParmNvl("cond_cnt2","N");
		item2ParmNum("tx_hour");
		item2ParmNum("tx_hour_cnt");
		item2ParmStr("cond_or_and1");
		item2ParmNum("cond1_amt");
		item2ParmStr("cond_or_and2");
		item2ParmNum("cond2_amt");
		setString("crt_user",wp.loginUser);
		setString("apr_user",wp.itemStr("approval_user"));
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("insert sms_msg_parm error");			
		}
		
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc !=1)
			return rc;
		
		strSql = " update sms_msg_parm set "
				+ " msg_id =:msg_id ,"
				+ " sms_remark =:sms_remark ,"
				+ " spec_list =:spec_list ,"
				+ " area_type =:area_type ,"
				+ " cond_success =:cond_success ,"
				+ " cond_country =:cond_country ,"
				+ " cond_curr =:cond_curr ,"
				+ " cond_bin =:cond_bin ,"
				+ " cond_group =:cond_group ,"
				+ " cond_mcht =:cond_mcht ,"
				+ " cond_mcc =:cond_mcc ,"
				+ " cond_pos =:cond_pos ,"
				+ " cond_trans_type =:cond_trans_type ,"
				+ " cond_resp_code =:cond_resp_code ,"
				+ " cond_amt =:cond_amt ,"
				+ " tx_amt =:tx_amt ,"
				+ " cond_cnt1 =:cond_cnt1 ,"
				+ " tx_day =:tx_day ,"
				+ " tx_dat_cnt =:tx_dat_cnt ,"
				+ " cond_cnt2 =:cond_cnt2 ,"
				+ " tx_hour =:tx_hour ,"
				+ " tx_hour_cnt =:tx_hour_cnt , "
				+ " cond_or_and1 =:cond_or_and1 , "
				+ " cond1_amt =:cond1_amt ,"
				+ " cond_or_and2 =:cond_or_and2 , "
				+ " cond2_amt =:cond2_amt ,"
				+ " apr_date =to_char(sysdate,'yyyymmdd') ,"
				+ " apr_user =:apr_user ,"
				+ " mod_user =:mod_user ,"
				+ " mod_time =sysdate ,"
				+ " mod_pgm =:mod_pgm ,"
				+ " mod_seqno = nvl(mod_seqno,0)+1 "
				+ " where sms_priority =:sms_priority "
				+ " and nvl(mod_seqno,0) = :mod_seqno "
				;
		
		item2ParmStr("msg_id");
		item2ParmStr("sms_remark");
		item2ParmNvl("spec_list","N");
		item2ParmNvl("area_type","3");
		item2ParmNvl("cond_success","N");
		item2ParmNvl("cond_country","0");
		item2ParmNvl("cond_curr","0");
		item2ParmNvl("cond_bin","0");
		item2ParmNvl("cond_group","0");
		item2ParmNvl("cond_mcht","0");
		item2ParmNvl("cond_mcc","0");
		item2ParmNvl("cond_pos","0");
		item2ParmNvl("cond_trans_type","0");
		item2ParmNvl("cond_resp_code","0");
		item2ParmNvl("cond_amt","0");
		item2ParmNum("tx_amt");
		item2ParmNvl("cond_cnt1","N");
		item2ParmNum("tx_day");
		item2ParmNum("tx_dat_cnt");
		item2ParmNvl("cond_cnt2","N");
		item2ParmNum("tx_hour");
		item2ParmNum("tx_hour_cnt");		
		item2ParmStr("cond_or_and1");
		item2ParmNum("cond1_amt");
		item2ParmStr("cond_or_and2");
		item2ParmNum("cond2_amt");
		setString("apr_user",wp.itemStr("approval_user"));
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		setString("sms_priority",smsPriority);
		setNumber("mod_seqno",wp.itemNum("mod_seqno"));
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("update sms_msg_parm error ");
		}
		
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc !=1)
			return rc;
		
		strSql = "delete sms_msg_parm where sms_priority =:sms_priority and nvl(mod_seqno,0) =:mod_seqno ";
		setString("sms_priority",smsPriority);
		setNumber("mod_seqno",wp.itemNum("mod_seqno"));		
		sqlExec(strSql);
		
		if(sqlRowNum <=0) {
			errmsg("delete sms_msg_parm error ");
			return rc;
		}
		
		deleteSmsDetl(smsPriority);		
		
		return rc;
	}
	
	void deleteSmsDetl(String mastPriority) {
		msgOK();
		
		if(mastPriority.isEmpty())
			mastPriority = wp.itemStr("sms_priority");
		
		strSql = "delete sms_msg_parm_detl where sms_priority =:sms_priority ";
		setString("sms_priority",mastPriority);
		sqlExec(strSql);
		if(sqlRowNum < 0) {
			errmsg("delete sms_msg_parm_detl error");
		}	else if(sqlRowNum ==0) {
			rc = 1;
		}
		return ;
	}
	
	public int detlInsert() throws Exception {
		msgOK();
		if(checkDetl(wp.itemStr("sms_priority"),wp.itemStr("data_type"),wp.itemStr("ex_data_code")) == false) {
			errmsg("明細資料已存在");
			return rc;
		}
		
		strSql = " insert into sms_msg_parm_detl ( "
				+ " sms_priority ,data_type ,data_code1 ,data_code2 , "
				+ " data_code3 ,crt_date ,crt_user ,apr_date ,apr_user , "
				+ " mod_user ,mod_time ,mod_pgm ,mod_seqno "
				+ " ) values ( "
				+ " :sms_priority ,:data_type ,:data_code1 ,:data_code2 , "
				+ " :data_code3 ,to_char(sysdate,'yyyymmdd') ,:crt_user ,to_char(sysdate,'yyyymmdd') ,:apr_user , "
				+ " :mod_user ,sysdate ,:mod_pgm ,1 "
				+ " ) "
				;
		
		item2ParmStr("sms_priority");
		item2ParmStr("data_type");
		setString("data_code1",wp.itemStr("ex_data_code"));
		setString("data_code2",wp.itemStr("ex_data_code2"));
		setString("data_code3",wp.itemStr("ex_data_code3"));
		setString("crt_user",wp.loginUser);
		setString("apr_user",wp.itemStr("approval_user"));
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("insert sms_msg_parm_detl error ");
		}
		
		return rc;
	}
	
	boolean checkDetl(String priority , String type , String code) {
		
		String sql1 = "select count(*) as db_cnt from sms_msg_parm_detl where sms_priority = ? "
				+ " and data_type = ? and data_code1 = ? ";
		
		sqlSelect(sql1,new Object[] {priority,type,code});
		
		if(colNum("db_cnt") > 0)
			return false;
		
		return true;
	}
	
	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int dbDeleteDetl() {
		msgOK();
		strSql = "delete sms_msg_parm_detl where sms_priority =:sms_priority and data_type =:data_type and data_code1 =:data_code1 ";
		var2ParmStr("sms_priority");
		var2ParmStr("data_type");
		var2ParmStr("data_code1");
		sqlExec(strSql);
		if(sqlRowNum <= 0) {
			errmsg("delete sms_msg_parm_detl error ");			
		}
		return rc;				
	}
	
	public int dbDeleteDetlAll() {
		msgOK();
		strSql = "delete sms_msg_parm_detl where sms_priority =:sms_priority and data_type =:data_type ";
		item2ParmStr("sms_priority");
		item2ParmStr("data_type");
		
		sqlExec(strSql);
		if(sqlRowNum <0) {
			errmsg("delete All sms_msg_parm_detl error ");
		}	else	
			rc =1;		
		return rc;
	}
	
	public int dbFileInputDetl() {
		msgOK();
		
		strSql = " insert into sms_msg_parm_detl ( "
				+ " sms_priority ,data_type ,data_code1 ,data_code2 , "
				+ " data_code3 ,crt_date ,crt_user ,apr_date ,apr_user , "
				+ " mod_user ,mod_time ,mod_pgm ,mod_seqno "
				+ " ) values ( "
				+ " :sms_priority ,:data_type ,:data_code1 ,:data_code2 , "
				+ " :data_code3 ,to_char(sysdate,'yyyymmdd') ,:crt_user ,to_char(sysdate,'yyyymmdd') ,:apr_user , "
				+ " :mod_user ,sysdate ,:mod_pgm ,1 "
				+ " ) "
				;
		
		item2ParmStr("sms_priority");
		item2ParmStr("data_type");
		item2ParmStr("data_code1");
		item2ParmStr("data_code2");
		item2ParmStr("data_code3");
		setString("crt_user",wp.loginUser);
		setString("apr_user",wp.itemStr("approval_user"));
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("insert All sms_msg_parm_detl error ");
		}
		
		return rc;
	}
	
}
