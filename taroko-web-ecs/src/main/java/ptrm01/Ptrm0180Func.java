/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-23   V1.00.01  Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package ptrm01;

import taroko.base.CommSqlStr;

/**
 */
public class Ptrm0180Func extends busi.FuncEdit {

  String zipCode;

  public Ptrm0180Func(taroko.com.TarokoCommon wr) {
    wp = wr;
    conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated
                                                                   // methods, choose Tools |
                                                                   // Templates.
  }

  @Override
  public int dataSelect() {
    return -1;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      zipCode = wp.itemStr("zip_code");
    } else {
      zipCode = varsStr("zip_code");
    }

    if (empty(zipCode)) {
      errmsg("[郵遞區號] 不可空白");
      return;
    }

    if (this.ibAdd)
      return;


    sqlWhere = " where zip_code = ? and nvl(mod_seqno,0) =nvl(?,0)";
    if (isOtherModify("ptr_zipcode", sqlWhere, new Object[] {zipCode,varsStr("mod_seqno")})) {
      return;
    }
    if (isDelete()) {
      return;
    }

    if (empty(varsStr("zip_city")) || empty(varsStr("zip_town"))) {
      errmsg("[縣市, 鄉鎮] 不可空白");
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "" + "insert into ptr_zipcode (" + " zip_code, " + " zip_city, " + " zip_town, "
        + " mod_user, mod_time, mod_pgm, mod_seqno" + " ) values (" + "?,?,?," + "?, sysdate, ?, 1"
        + " )";
    Object[] param = new Object[] {zipCode, wp.itemStr("zip_city"), wp.itemStr("zip_town"),
        wp.loginUser, wp.itemStr("mod_pgm")};

    sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg("Insert" + this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "" + " update ptr_zipcode set " + "  zip_city =?" + ", zip_town =?" + ", mod_user =?"
        + ", mod_time =sysdate" + ", mod_pgm  =?" + ", mod_seqno =nvl(mod_seqno,0)+1" + sqlWhere;
    Object[] param =
        new Object[] {varsStr("zip_city"), varsStr("zip_town"), wp.loginUser, "ptrm0180", zipCode,varsStr("mod_seqno")};
    sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg("Update:" + sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete ptr_zipcode " + sqlWhere;

    sqlExec(strSql, new Object[] {zipCode,varsStr("mod_seqno")});
    if (sqlRowNum <= 0) {
      errmsg("Delete:" + sqlErrtext);
    }
    return rc;
  }

}
