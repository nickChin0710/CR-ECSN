/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名  
* 110-10-21  V1.00.03   machao        SQL Injection修改                                                                                     *    
******************************************************************************/
package colm05;
/*m05 整批強停參數覆核-主管作業
 * */
import busi.FuncEdit;
//import taroko.com.TarokoCommon;

public class Colp5720Func extends FuncEdit {
  String paramType = "1", acctType = "", validDate = "";

  // public Colm720_func(TarokoCommon wr) {
  // wp = wr;
  // this.conn = wp.getConn();
  // }
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
    if (this.ibAdd) {
      acctType = wp.itemStr("kk_acct_type");
    } else {
      acctType = wp.itemStr("acct_type");
    }

    if (this.ibAdd) {
      validDate = wp.itemStr("kk_valid_date");
    } else {
      validDate = wp.itemStr("valid_date");
      if (this.isAdd()) {
        return;
      }
//      sqlWhere = " where param_type='" + paramType + "'" + " and acct_type='" + acctType + "'"
//          + " and valid_date='" + validDate + "'" + " and nvl(mod_seqno,0) =" + wp.modSeqno();
      
      sqlWhere = "where param_type = :paramType and acct_Type = :acctType and valid_date = :validDate and nvl(mod_seqno,0) = :modSeqno";
      setString("paramType",paramType);
      setString("acctType",acctType);
      setString("validDate",validDate);
      setString("modSeqno",wp.modSeqno());
      
      log("sql-where=" + sqlWhere);
      if (this.isOtherModify("ptr_stopparam", sqlWhere)) {
        return;
      }
    }
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
    if (rc != 1) {
      return rc;
    }
    strSql = "update ptr_stopparam set " + " apr_flag =?, " + " pause_flag =?, "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? " + ", mod_seqno =nvl(mod_seqno,0)+1 "
        + " where param_type = ? and acct_Type = ? and valid_date = ? and nvl(mod_seqno,0) = ? ";
    log("sql-where=" + sqlWhere);
    Object[] param = new Object[] {wp.itemStr("apr_flag"), wp.itemStr("pause_flag"),
        wp.itemStr("mod_user"), wp.itemStr("mod_pgm"),paramType,acctType,validDate,wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

}
