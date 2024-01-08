/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/27  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6205Func extends FuncEdit {
  private String PROGNAME = "特店群組代碼維護處理程式108/05/27 V1.00.01";
  String dataKey, dataCode, dataCode2;
  String controlTabName = "mkt_mchtgp_data";

  public Mktm6205Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      dataKey = wp.itemStr("kk_data_key");
      dataCode = wp.itemStr("kk_data_code");
      dataCode2 = wp.itemStr("kk_data_code2");
    } else {
      dataKey = wp.itemStr("data_key");
      dataCode = wp.itemStr("data_code");
      dataCode2 = wp.itemStr("data_code2");
    }

    if (this.ibAdd) {
      if (wp.itemStr("kk_data_code").length() == 0) {
        errmsg("特店代碼必須輸入 !");
        return;
      }
      if ((wp.itemStr("kk_data_code2").length() != 0)
          && (wp.itemStr("kk_data_code2").length() > 8)) {
        errmsg("收單行代號長度不可超過8碼 !");
        return;
      }
      if (wp.itemStr("kk_data_code2").length() < 8) {
        wp.itemSet("kk_data_code2",
            "00000000".substring(0, 8 - wp.itemStr("kk_data_code2").length())
                + wp.itemStr("kk_data_code2"));
        dataCode2 = wp.itemStr("kk_data_code2");

      }

      strSql = "select count(*) as qua " + "from " + controlTabName + " where data_key = ? "
          + " and   data_code = ? " + " and   data_code2 = ? "
          + " and   table_name = 'MKT_MCHT_GP' " + " and   data_type = '1' ";
      Object[] param = new Object[] {dataKey, dataCode, dataCode2};
      sqlSelect(strSql, param);
      int qua = Integer.parseInt(colStr("qua"));
      if (qua > 0) {
        errmsg("[特店群組代碼][特店代號][收單行] 不可重複 ,請重新輸入!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
    }



    if (this.isAdd())
      return;

    // -other modify-
    sqlWhere =
        "where rowid = x'" + wp.itemStr("rowid") + "'" + " and nvl(mod_seqno,0)=" + wp.modSeqno();

    if (this.isOtherModify(controlTabName, sqlWhere)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;


    strSql = " insert into  " + controlTabName + " (" + " data_key, " + " data_code, "
        + " data_code2, " + " TABLE_NAME, " + " data_type, " + " crt_date, " + " crt_user, "
        + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?," + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {dataKey, dataCode, dataCode2, "MKT_MCHT_GP", "1", wp.loginUser, wp.modSeqno(),
        wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
