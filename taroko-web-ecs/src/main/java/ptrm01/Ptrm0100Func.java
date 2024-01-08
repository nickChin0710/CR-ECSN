/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-01  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0100Func extends FuncEdit {
  String sourceCode = "", dataKK2 = "";

  public Ptrm0100Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
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
      sourceCode = wp.itemStr("kk_source_code");
      if (sourceCode.length() < 6) {
        errmsg("ERROR 來源代碼 ");
        return;
      }

    } else {
      sourceCode = wp.itemStr("source_code");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from ptr_src_code where source_code = ?";
      Object[] param = new Object[] {sourceCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;

    }

    // ddd(this.actionCode+", kk1="+kk1+", mod_seqno="+wp.mod_seqno());

    // -other modify-
    sqlWhere = " where source_code= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {sourceCode, wp.modSeqno()};
    if (this.isOtherModify("ptr_src_code", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql =
        "insert into ptr_src_code (" + " source_code, " + "source_name, " + "third_data_reissue,"
            + "not_auto_installment," + " crt_date, " + " crt_user, " + " mod_pgm, " + " mod_seqno"
            + " ) values (" + " ?,?,?,? " + ",to_char(sysdate,'yyyymmdd'),?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {sourceCode // source_code
        , wp.itemStr("source_name"),
        empty(wp.itemStr("third_data_reissue")) ? "N" : wp.itemStr("third_data_reissue"),
        empty(wp.itemStr("not_auto_installment")) ? "N" : wp.itemStr("not_auto_installment"),
        wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update ptr_src_code set " + " source_name =?, " + " third_data_reissue =?, "
        + " not_auto_installment =?, " + " mod_user =?, " + " mod_time=sysdate, " + " mod_pgm =? ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("source_name"),
        empty(wp.itemStr("third_data_reissue")) ? "N" : wp.itemStr("third_data_reissue"),
        empty(wp.itemStr("not_auto_installment")) ? "N" : wp.itemStr("not_auto_installment"),
        wp.loginUser, wp.itemStr("mod_pgm"), sourceCode, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }


  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete ptr_src_code " + sqlWhere;
    // ddd("del-sql="+is_sql);
    Object[] param = new Object[] {sourceCode, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
