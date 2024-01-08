/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/31  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1060Func extends FuncEdit {
  private String PROGNAME = "高鐵生檔不送簡訊清單處理程式108/10/31 V1.00.01";
  String dataType1, dataCode;
  String controlTabName = "mkt_bn_data";

  public Mktm1060Func(TarokoCommon wr) {
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
      dataType1 = wp.itemStr("kk_data_type");
      dataCode = wp.itemStr("kk_data_code");
    } else {
      dataType1 = wp.itemStr("data_type");
      dataCode = wp.itemStr("data_code");
    }
    if (this.ibAdd)
      if (dataType1.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where data_type = ? "
            + " and   table_name  =  'MKT_THSR_UPTXNP' " + " and   data_key  =  'THSR_LIST' ";
        Object[] param = new Object[] {dataType1};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[資料類別] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("kk_data_code").length() == 0) {
        errmsg("[身分證號/手機號碼] 不可空白 !");
        return;
      }

      if (wp.itemStr("kk_data_type").equals("1")) {
        strSql = "select id_no " + " from crd_idno " + " where  id_no = ? ";
        Object[] param = new Object[] {wp.itemStr("kk_data_code")};
        sqlSelect(strSql, param);

        if (sqlRowNum <= 0) {
          errmsg("卡人資料不存在 !");
          return;
        }
      } else {
        if (!wp.itemStr("kk_data_code").matches("[0-9]+")) {
          errmsg("不合格的手機號碼 !");
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
        + " table_name, " + " data_key, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?," + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {dataType1, dataCode, "MKT_THSR_UPTXN", "THSR_LIST", wp.loginUser,
        wp.modSeqno(), wp.loginUser, wp.modPgm()};

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
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }
  // ************************************************************************

} // End of class
