/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/12  V1.00.01   Ray Ho        Initial                              *   
* 112-05-03  V1.00.02   Ryan       移除團體代號欄位與相關邏輯                                                                                 *    
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0200Func extends FuncEdit {
  private String PROGNAME = "聯名機構基本資料維護處理程式110/03/31 V0.00.01";
  String exMemberCorpNo, acctNo,exMemberName;
  String controlTabName = "mkt_member";

  public Mktm0200Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
	   Object[] param;
	      acctNo = wp.itemStr("acct_no");
	      exMemberCorpNo = wp.itemStr("ex_member_corp_no");
	      exMemberName=wp.itemStr("member_name");
	  if ((this.ibAdd)) {
	      if (empty(acctNo)||empty(exMemberCorpNo)||empty(exMemberName)) {
	        errmsg("必輸入欄位,請輸入資料");
	        return;
	      }
	      strSql = "select member_corp_no " + "from mkt_member " + " where member_corp_no = ?  ";
	      param = new Object[] {exMemberCorpNo};
	      sqlSelect(strSql, param);

	      if (sqlRowNum > 0) {
	        errmsg("聯名機構統編不可重覆!");
	        return;
	      }
	      
	   
	  }
	    if (this.ibUpdate) {   
	      if (empty(acctNo)||empty(exMemberCorpNo)||empty(exMemberName)) {
	        errmsg("必輸入欄位,請輸入資料");
	        return;
	      }	      
	    }
	  
    }
    

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;


    strSql = " insert into  " + controlTabName + " (" + " member_corp_no, " + " member_name, "
        + " acct_no, "+ " apr_flag, "+ " active_status, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,'Y','Y',"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {exMemberCorpNo,exMemberName,acctNo,
        wp.loginUser, wp.loginUser,  wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");
    

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + " member_name=?, "
            + " acct_no=?, "  + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
       + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where member_corp_no = ? ";

    Object[] param =
        new Object[] {exMemberName,acctNo, wp.loginUser, 
            wp.loginUser, wp.itemStr("mod_pgm"),exMemberCorpNo};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
	actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete " + controlTabName +" where member_corp_no = ?";

    Object[] param = new Object[] {exMemberCorpNo};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
} // End of class
