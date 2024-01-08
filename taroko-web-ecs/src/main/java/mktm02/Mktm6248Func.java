/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6248Func extends FuncEdit {
  private String PROGNAME = "深呆戶卡片等級維護處理程式108/09/03 V1.00.01";
  String dataType, dataCode, dataCode1;
  String controlTabName = "mkt_bn_data";

  public Mktm6248Func(TarokoCommon wr) {
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
      dataType = wp.itemStr("kk_data_type");
      dataCode = wp.itemStr("kk_data_code");
      dataCode1 = wp.itemStr("kk_data_code2");
    } else {
      dataType = wp.itemStr("data_type");
      dataCode = wp.itemStr("data_code");
      dataCode1 = wp.itemStr("data_code2");
    }
    if (this.ibAdd)
      if (dataType.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where data_type = ? "
            + " and   data_code = ? " + " and   data_code2 = ? "
            + " and   table_name  =  'MKT_FSTP_PARM_DEEP' "
            + " and   data_key  =  'DEEP_CLASS_CODE' ";
        Object[] param = new Object[] {dataType, dataCode, dataCode1};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[深呆等級][卡片等級][卡　　種] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if ((wp.itemStr("kk_data_code").length() == 0)
          && (wp.itemStr("kk_data_code2").length() == 0)) {
        errmsg("[卡片等級與卡種] 不可全為空白 !");
        return;
      }

      if ((wp.itemStr("kk_data_code").length() == 0)
          && (wp.itemStr("kk_data_code2").length() != 0)) {
        strSql = "select data_code " + " from mkt_bn_data "
            + " where  table_name = 'MKT_FSTP_PARM_DEEP' "
            + " and    data_key   = 'DEEP_CLASS_CODE' " + " and    data_code  = '' "
            + " and    data_code2 = ? ";
        Object[] param = new Object[] {wp.itemStr("kk_data_code2")};
        sqlSelect(strSql, param);

        if (sqlRowNum > 0) {
          errmsg("卡種[" + wp.itemStr("kk_data_code2") + "]資料已存在 !");
          return;
        }
      }
      if ((wp.itemStr("kk_data_code").length() != 0)
          && (wp.itemStr("kk_data_code2").length() == 0)) {
        strSql = "select data_code " + " from mkt_bn_data "
            + " where  table_name = 'MKT_FSTP_PARM_DEEP' "
            + " and    data_key   = 'DEEP_CLASS_CODE' " + " and    data_code  = ? "
            + " and    data_code2 = '' ";
        Object[] param = new Object[] {wp.itemStr("kk_data_code")};
        sqlSelect(strSql, param);

        if (sqlRowNum > 0) {
          errmsg("卡片等級[" + wp.itemStr("kk_data_code") + "]資料已存在 !");
          return;
        }
      }
      if ((wp.itemStr("kk_data_code").length() != 0)
          && (wp.itemStr("kk_data_code2").length() != 0)) {
        strSql = "select data_code " + " from mkt_bn_data "
            + " where  table_name = 'MKT_FSTP_PARM_DEEP' "
            + " and    data_key   = 'DEEP_CLASS_CODE' " + " and    data_code  = ? "
            + " and    data_code2 = ? ";
        Object[] param = new Object[] {wp.itemStr("kk_data_code"), wp.itemStr("kk_data_code2")};
        sqlSelect(strSql, param);

        if (sqlRowNum > 0) {
          errmsg("卡片等級[" + wp.itemStr("kk_data_code") + "] 卡種[" + wp.itemStr("kk_data_code2")
              + "] 資料已存在 !");
          return;
        }
      }
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


    strSql = " insert into  " + controlTabName + " (" + " data_type, " + " data_code, "
        + " data_code2, " + " table_name, " + " data_key, " + " crt_date, " + " crt_user, "
        + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?," + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {dataType, dataCode, dataCode1, "MKT_FSTP_PARM_DEEP", "DEEP_CLASS_CODE",
        wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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
