/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  shiyuqi       updated for project coding standard     * 
* 109-07-17  V1.00.03  JustinWu    add three methods to delete and to insert into the related tables
******************************************************************************/
package secm01;

import busi.FuncAction;

public class Secm2020Func extends FuncAction {

	private String programName = "secm2020";
	
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

  public int dbInsertDetl() {
    msgOK();
    strSql = "insert into sec_workgroup (" + " group_id , " + " group_name , " + " log_mark , "
        + " crt_date , " + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , "
        + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values (" + " :group_id , "
        + " :group_name , " + " 'N' , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , "
        + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " '1' " + " )";
    var2ParmStr("group_id");
    var2ParmStr("group_name");
    // var2Parm_nvl("log_mark","N");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", programName);

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert sec_workgroup error, " + getMsg());
    }

    return rc;
  }

  public int dbDeleteDetl() {
    msgOK();
    String groupId = varsStr("group_id");
    if( isAnyoneUsingTheGroup(groupId)) {
    	wp.alertMesg(String.format("delete sec_workgroup 錯誤， 有使用者擁有此群組[%s]，因此無法刪除", groupId));
         return -1;
    }
    
    strSql = "delete sec_workgroup" 
                + " where hex(rowid) =:rowid";
    var2ParmStr("rowid");
    sqlExec(strSql);
    
    if (sqlRowNum < 0) {
      wp.alertMesg("delete sec_workgroup 錯誤，" + getMsg());
      return -1;
    }
    
    boolean isSuccess = false;
	isSuccess = deleteNotProvedSecAuthorityLog(groupId);
	if ( ! isSuccess) return -1;
    
	isSuccess = insertSecAuthoritLog(groupId);
	if ( ! isSuccess) return -1;
	
	isSuccess = deleteSecAuthority(groupId);
	if ( ! isSuccess) return -1;
    
    return rc;
  }

	private boolean deleteNotProvedSecAuthorityLog(String groupId) {
		strSql = " delete SEC_AUTHORITY_LOG " 
	                + " where group_id = :groupId "
	                + " and apr_flag <> 'Y' ";
		setString("groupId", groupId);
		sqlExec(strSql);

		if (sqlRowNum < 0) {
			errmsg("delete sec_authority_log err=" + getMsg());
			return false;
		}
		return true;
	}

private boolean deleteSecAuthority(String groupId) {
	strSql = "delete SEC_AUTHORITY" 
                + " where group_id = :groupId ";
    setString("groupId", groupId);
    sqlExec(strSql);
    
    if (sqlRowNum < 0) {
      errmsg("delete sec_authority err=" + getMsg());
      return false;
    }
    return true;
	
}

private boolean insertSecAuthoritLog(String groupId) {
	strSql =       " INSERT INTO SEC_AUTHORITY_LOG  "
					  + " (GROUP_ID, USER_LEVEL, WF_WINID, "
					  + "  APR_FLAG, AUT_QUERY, AUT_UPDATE, "
					  + "  AUT_APPROVE, AUT_PRINT, "
					  + "  CRT_DATE, CRT_USER, APR_DATE, APR_USER,  "
					  + "  MOD_AUDCODE, MOD_TIME, MOD_USER, MOD_PGM) "
					  + " SELECT GROUP_ID ,USER_LEVEL ,WF_WINID ,  "
					  + "  'Y', AUT_QUERY ,AUT_UPDATE , "
					  + "  AUT_APPROVE ,AUT_PRINT , "
					  + "  CRT_DATE ,CRT_USER ,APR_DATE , :approveUser  , "
					  + "  'D', sysDate, :user , :programName "
					  + " FROM SEC_AUTHORITY ";
	
	setString("approveUser", varsStr("approveUser"));
	setString("user", wp.loginUser);
	setString("programName", programName);
	
	this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert SEC_AUTHORITY_LOG error, " + getMsg());
      return false;
    }
	return true;
}

/**
   * 是否有人正在使用此群組
   * @param group
   * @return
   */
	private boolean isAnyoneUsingTheGroup(String groupId) {
		strSql =  " SELECT 1 "
				+ " FROM DUAL  "
				+ " WHERE EXISTS ("
				+ " SELECT * FROM SEC_USER "
				+ " WHERE USR_GROUP = ? "
				+ " OR USR_GROUP LIKE ? "
				+ " OR USR_GROUP LIKE ? "
				+ " OR USR_GROUP LIKE ? "
				+ " )  " ;
		
		setString(1, groupId);
		setString(2, "%," + groupId + ",%");
		setString(3, groupId + ",%");
		setString(4, "%," + groupId);
		
		sqlSelect(strSql);
		
		if (sqlRowNum <= 0 )
			return false;
		
		return true;
	}

public int dbUpdateDetl() {
    msgOK();
    strSql = "update sec_workgroup set " + " group_name =:group_name ,"
    // + " log_mark =:log_mark ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where hex(rowid) =:rowid";
    var2ParmStr("group_name");
    // var2Parm_nvl("log_mark","N");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", programName);
    var2ParmStr("rowid");
    wp.log(strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update sec_workgroup err=" + getMsg());
      rc = -1;
    } ;
    return rc;
  }

}
