/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/12/16  V1.00.01   Machao      Initial      *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import busi.ecs.CommRoutine;
// ************************************************************************
public class Mktm0510Func extends FuncEdit {
  private final String PROGNAME = "推廣人員維護(匯入)程式111/12/16 V1.00.01";
  String controlTabName = "CRD_EMPLOYEE_A_T";
  CommRoutine comr = new CommRoutine();

@Override
public int querySelect() {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public int dataSelect() {
	// TODO Auto-generated method stub
	return 0;
}

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

public int insertRighList() {
	  comr.setConn(this.wp);
	    msgOK();
	    //執行代碼
	    strSql = " insert into CRD_EMPLOYEE_A_T (" + " mod_date ," + " employ_no ,"
	  	          + " chi_name ," + " id ," + " id_code ," + " acct_no ," + " unit_no ,"
	  	          + " unit_name ," + " subunit_no ," + " subunit_name ," + " position_id ," + " position_name ,"
	  	          + " status_id ," + " status_name ," + " file_name ," + " corp_no ,"
	  	          + " subsidiary_no ," + " error_code ," + " error_desc ," + " aud_type ," + " description ,"
	  	          + " apr_date ," + " apr_user ," + " apr_flag ," + " crt_date ,"
	  	          + " crt_user ," + " mod_user ," + " mod_time ," + " mod_pgm "
	  	          + " )"
	  	          + " values ( " + " to_char(sysdate,'yyyymmdd'), " + " :employ_no ," 
	  	          + " :chi_name ," + " :id ," + " '0' ," + " :acct_no ," + " :unit_no ,"
	  	          + " :unit_name ," + " :subunit_no ," + " :subunit_name ," + " :position_id ," + " :position_name ,"
	  	          + " :status_id ," + " :status_name ," + " :file_name ," + " :corp_no ,"
	  	          + " :subsidiary_no ," + " :error_code ," + " :error_desc ," + " 'A' ," + " :description ," 
	  	          + " :apr_date ," + " :apr_user ," + " 'N' ," + " to_char(sysdate,'yyyymmdd'), " 
	  	          + " :crt_user ," + " :mod_user ," + "to_char(sysdate,'yyyymmddhh24miss') ," + " :mod_pgm "
	  	          + " ) ";
	    item2ParmStr("employ_no");
	    item2ParmStr("chi_name");
	    item2ParmStr("id");
	    item2ParmStr("acct_no");
	    item2ParmStr("unit_no");
	    item2ParmStr("unit_name");
	    item2ParmStr("subunit_no");
	    item2ParmStr("subunit_name");
	    item2ParmStr("position_id");
	    item2ParmStr("position_name");
	    item2ParmStr("status_id");
	    item2ParmStr("status_name");
	    item2ParmStr("file_name");
	    item2ParmStr("corp_no");		    
	    item2ParmStr("subsidiary_no");
	    item2ParmStr("error_code");
	    item2ParmStr("error_desc");		    
	    item2ParmStr("description");		    
	    item2ParmStr("apr_date");		    
	    item2ParmStr("apr_user");
	    setString("crt_user", wp.loginUser);
	    setString("mod_user", wp.loginUser);
	    setString("mod_pgm", wp.modPgm());
	    
	    
	    sqlExec(strSql);
	    
	    if (sqlRowNum <= 0) {
	    	errmsg("insert CRD_EMPLOYEE_A_T error ");
	    }

	    return rc;
	  }

} // End of class
