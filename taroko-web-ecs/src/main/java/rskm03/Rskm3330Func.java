/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                         *  
* 111-03-23  V1.00.04  Justin       若三碼國別碼在country_code查不到，擷取兩碼*  
******************************************************************************/
package rskm03;

import busi.FuncAction;

public class Rskm3330Func extends FuncAction {
  String cardNo = "", txDate = "", txTime = "", authNo = "", traceNo = "", isProcDesc = "",
      isOldStatusCode = "" , mchtCountry = "";
  boolean lbUpdate = false;

  @Override
  public void dataCheck() {
    cardNo = wp.itemStr("card_no");
    txDate = wp.itemStr("tx_date");
    txTime = wp.itemStr("tx_time");
    authNo = wp.itemStr("auth_no");
    traceNo = wp.itemStr("trace_no");

    isProcDesc = wp.itemStr("proc_desc1") + wp.itemStr("proc_desc2") + wp.itemStr("proc_desc3");
    
    if(isAdd() || isUpdate()) {
    	if(wp.itemEmpty("proc_date") || wp.itemEmpty("proc_time")) {
        	errmsg("處理日期、時間 : 不可空白");
        	return ;
        }
    	
    	if(wp.itemEq("status_code", "5") || wp.itemEq("status_code", "9")) {
    		if(wp.itemEmpty("proc_result")) {
    			errmsg("處理階段為處理中或處理完成時，處理結果不可空白 !");
    			return ;
    		}
    	}
    	
    	if(wp.itemEq("proc_result", "01")) {
    		if(wp.itemEq("status_code", "9") == false ) {
    			errmsg("處理結果為無須後續處理時，處理階段需為處理完成 !");
    			return ;
    		}
    	}
    	
    	mchtCountry = wp.itemStr("mcht_country");
    	if(mchtCountry.length() == 3)
    		mchtCountry = getCountryCode(mchtCountry);
    	
    }    
    
    lbUpdate = checkMaster();

    if (lbUpdate == true && eqIgno(isOldStatusCode, "9")) {
      errmsg("該筆交易已處理完成，不可異動 !");
      return;
    }

  }
  
  String getCountryCode(String country) {
	  String tempCode = "";
	  String sql1 = "select country_code from cca_country where bin_country = ? ";
	  sqlSelect(sql1,new Object[] {country});
	  if(sqlRowNum <=0) {
		  tempCode = country.substring(0,2) ;		  
		  wp.showLogMessage("I", "", String.format("查無country_code[%s]，擷取前兩碼[%s]", country, tempCode));
	  }	else	{
		  tempCode = colStr("country_code");
	  }
	  return tempCode ;
  }
  
  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    if (lbUpdate == false) {
      insertFactorMaster();
    } else {
      updateFactorMaster();
    }

    if (rc != 1)
      return rc;

    updateAuthTxlog();

    return rc;
  }

  boolean checkMaster() {

    String sql1 = " select status_code from rsk_factormaster where 1=1 "
        + " and card_no = ? and tx_date = ? and auth_no = ? and trace_no = ? and tx_time = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("card_no"), wp.itemStr("tx_date"),
        wp.itemStr("auth_no"), wp.itemStr("trace_no"), wp.itemStr("tx_time")});

    if (sqlRowNum <= 0)
      return false;

    isOldStatusCode = colStr("status_code");

    return true;
  }

  void insertFactorMaster() {
    msgOK();
    strSql = " insert into rsk_factormaster ( " + " card_no , " + " tx_date , " + " tx_time , "
        + " auth_no , " + " trace_no , " + " risk_score , " + " auth_status_code , "
        + " tx_currency , " + " ori_amt , " + " nt_amt , " + " mcht_no , " + " mcht_country , "
        + " mcht_city , " + " mcht_city_name , " + " mcht_name , " + " proc_date , "
        + " proc_time , " + " content_result , " + " status_code , " + " proc_result , "
        + " proc_desc , " + " problem_flag , " + " iso_resp_code , " + " crt_date , "
        + " crt_user , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values ( " + " :kk1 , " + " :kk2 , " + " :kk3 , " + " :kk4 , " + " :kk5 , "
        + " :risk_score , " + " :auth_status_code , " + " :tx_currency , " + " :ori_amt , "
        + " :nt_amt , " + " :mcht_no , " + " :mcht_country , " + " :mcht_city , "
        + " :mcht_city_name , " + " :mcht_name , " + " :proc_date , " + " :proc_time , "
        + " :content_result , " + " :status_code , " + " :proc_result , " + " :proc_desc , "
        + " :problem_flag , " + " :iso_resp_code , " + " to_char(sysdate,'yyyymmdd') , "
        + " :crt_user , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";

    setString("kk1", cardNo);
    setString("kk2", txDate);
    setString("kk3", txTime);
    setString("kk4", authNo);
    setString("kk5", traceNo);
    item2ParmNum("risk_score");
    item2ParmStr("auth_status_code");
    item2ParmStr("tx_currency");
    item2ParmNum("ori_amt");
    item2ParmNum("nt_amt");
    item2ParmStr("mcht_no");
    setString("mcht_country",mchtCountry);
//    item2ParmStr("mcht_country");
    item2ParmStr("mcht_city");
    item2ParmStr("mcht_city_name");
    item2ParmStr("mcht_name");
    item2ParmStr("proc_date");
    item2ParmStr("proc_time");
    item2ParmStr("content_result");
    item2ParmStr("status_code");
    item2ParmStr("proc_result");
    setString("proc_desc", isProcDesc);
    item2ParmNvl("problem_flag", "N");
    item2ParmStr("iso_resp_code");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

  }

  void updateFactorMaster() {
    msgOK();

    strSql = " update rsk_factormaster set " + " proc_date =:proc_date , "
        + " proc_time =:proc_time , " + " content_result =:content_result , "
        + " status_code =:status_code , " + " proc_result =:proc_result , "
        + " proc_desc =:proc_desc , " + " problem_flag =:problem_flag , "
        + " mod_user =:mod_user , " + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno = nvl(mod_seqno,0)+1 " + " where card_no =:kk1 " + " and tx_date =:kk2 "
        + " and tx_time =:kk3 " + " and auth_no =:kk4 " + " and trace_no =:kk5 "
        + " and mod_seqno =:mod_seqno ";

    item2ParmStr("proc_date");
    item2ParmStr("proc_time");
    item2ParmStr("content_result");
    item2ParmStr("status_code");
    item2ParmStr("proc_result");
    setString("proc_desc", isProcDesc);
    item2ParmNvl("problem_flag", "N");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString("kk1", cardNo);
    setString("kk2", txDate);
    setString("kk3", txTime);
    setString("kk4", authNo);
    setString("kk5", traceNo);
    item2ParmNum("mod_seqno");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

  }

  void updateAuthTxlog() {
    msgOK();
    strSql = " update cca_auth_txlog set " + " status_code =:status_code " + " where card_no =:kk1 "
        + " and tx_date =:kk2 " + " and tx_time =:kk3 " + " and auth_no =:kk4 "
        + " and trace_no =:kk5 ";

    item2ParmStr("status_code");
    setString("kk1", cardNo);
    setString("kk2", txDate);
    setString("kk3", txTime);
    setString("kk4", authNo);
    setString("kk5", traceNo);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

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

}
