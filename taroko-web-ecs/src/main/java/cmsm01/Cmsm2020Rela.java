/** 
*109-04-19    shiyuqi       updated for project coding standard
*109-08-07    JustinWu   fix 關係人頁面bug 
*109-12-23    JustinWu    parameterize sql
 * */

package cmsm01;

import busi.FuncAction;

public class Cmsm2020Rela extends FuncAction {
  String isData = "", isKey = "";

  @Override
  public void dataCheck() {
    isKey = wp.itemStr("corp_no") + varsStr("rela_type") + commString.rpad(varsStr("id_no"), 20);
    isData = commString.rpad(varsStr("chi_name"), 76) + commString.rpad(varsStr("eng_name"), 38)
        + commString.rpad(varsStr("birthday"), 8) + commString.rpad(varsStr("cntry_code"), 2);

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

    strSql = " insert into ecs_moddata_tmp ( " + " tmp_pgm , " + " tmp_table , " + " tmp_key , "
        + " tmp_dspdata , " + " tmp_moddata , " + " tmp_audcode , " + " mod_user , "
        + " mod_date , " + " mod_time2 , " + " mod_seqno " + " ) "
        + " values ( " + " 'cmsm2020' , "+ " 'CRD_CORP_RELA' , " + " :tmp_key ," + " '' , " + " :tmp_moddata , " + " 'A' , "
        + " :mod_user , " + " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , "
        + " 1 " + " ) ";

    setString("tmp_key", isKey);
    setString("tmp_moddata", isData);
    setString("mod_user", wp.loginUser);

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int deleteCrdCorpRela() {
    msgOK();

    strSql =
        " delete crd_corp_rela "
    + "where corp_no=:corp_no "
    + " and rela_type =:rela_type "
    + " and id_no =:id_no ";
    var2ParmStr("corp_no");
    var2ParmStr("rela_type");
    var2ParmStr("id_no");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int deleteAllTemp() {
    msgOK();
    strSql =
        " delete ecs_moddata_tmp where tmp_pgm = 'cmsm2020' and tmp_table = 'CRD_CORP_RELA' and tmp_key like :tmp_key ";
    setString("tmp_key", varsStr("corp_no") + "%");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(sqlErrtext);
    } else
      rc = 1;
    return rc;
  }
  
  public int deleteTemp() {
	    msgOK();
	    
	    dataCheck();
	    
	    strSql =
	        " delete ecs_moddata_tmp "
	    + " where tmp_pgm = 'cmsm2020' "
	    + " and tmp_table = 'CRD_CORP_RELA' "
	    + " and tmp_key = :tmp_key ";
	    setString("tmp_key", isKey);
	    
	    sqlExec(strSql);
	    
	    if (sqlRowNum < 0) {
	      errmsg(sqlErrtext);
	    } else {
	    	rc = 1;
	    }
	    
	    return rc;
	  }
  
  public int updateTemp() {
	  msgOK();
	  
	  dataCheck();
	  
	  strSql = 
			      " update ecs_moddata_tmp set "
			    + " tmp_pgm = 'cmsm2020' , " 
			    + " tmp_table = 'CRD_CORP_RELA' , " 
		        + " tmp_dspdata = '' , " 
			    + " tmp_moddata = :tmp_moddata , " 
		        + " tmp_audcode = 'A' , " 
			    + " mod_user = :mod_user , "
		        + " mod_date = to_char(sysdate,'yyyymmdd') , " 
			    + " mod_time2 = to_char(sysdate,'hh24miss'), " 
		        + " mod_seqno = nvl(mod_seqno,0)+1 "
		        + " where tmp_key = :tmp_key";
	  
		setString("tmp_key", isKey);
		setString("tmp_moddata", isData);
		setString("mod_user", wp.loginUser);

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
		}
	  
	  return rc;
  }

  public int dataProc2() {
    msgOK();

    // --刪除主檔
    strSql =
        " delete crd_corp_rela where corp_no =:corp_no and rela_type =:rela_type and id_no =:id_no ";
    var2ParmStr("corp_no");
    var2ParmStr("rela_type");
    var2ParmStr("id_no");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg(sqlErrtext);
      return rc;
    } else
      rc = 1;

    // --新增主檔
    strSql = " insert into crd_corp_rela ( " + " corp_no ," + " rela_type ," + " id_no ,"
        + " chi_name ," + " eng_name ," + " birthday ," + " cntry_code ," + " crt_user ,"
        + " crt_date ," + " apr_flag ," + " apr_date ," + " apr_user ," + " mod_user ,"
        + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ( " + " :corp_no ,"
        + " :rela_type ," + " :id_no ," + " :chi_name ," + " :eng_name ," + " :birthday ,"
        + " :cntry_code ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ," + " 'Y' ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :apr_user ," + " :mod_user ," + " sysdate ,"
        + " 'cmsm2020' ," + " 1 " + " )";

    var2ParmStr("corp_no");
    var2ParmStr("rela_type");
    var2ParmStr("id_no");
    var2ParmStr("chi_name");
    var2ParmStr("eng_name");
    var2ParmStr("birthday");
    var2ParmStr("cntry_code");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }

    // --刪除暫存檔
    strSql = " delete ecs_moddata_tmp where 1=1 and hex(rowid) = ? " ;
    setString(varsStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }

    return rc;
  }

}
