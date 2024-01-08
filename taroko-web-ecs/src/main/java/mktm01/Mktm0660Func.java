/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0660Func extends FuncEdit {
  private String PROGNAME = "WEB登錄代碼群組檔處理程式108/12/12 V1.00.01";
  String recordGroupNo;
  String orgControlTabName = "web_record_group";
  String controlTabName = "web_record_group_t";

  public Mktm0660Func(TarokoCommon wr) {
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
    String procTabName = "";
    procTabName = wp.itemStr("control_tab_name");
    strSql = " select " + " record_group_name, " + " active_date_s, " + " active_date_e, "
        + " voice_record_sel, " + " web_record_sel, " + " merchant_sel, " + " mcht_group_sel, "
        + " record_cnt_cond, " + " record_cnt, " + " record_id_cond, " + " purchase_cond, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      recordGroupNo = wp.itemStr("record_group_no");
      if (empty(recordGroupNo)) {
        errmsg("登錄群組代碼 不可空白");
        return;
      }
    } else {
      recordGroupNo = wp.itemStr("record_group_no");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (recordGroupNo.length() > 0) {
          strSql = "select count(*) as qua " + "from " + orgControlTabName
              + " where record_group_no = ? ";
          Object[] param = new Object[] {recordGroupNo};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[登錄群組代碼] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (recordGroupNo.length() > 0) {
        strSql =
            "select count(*) as qua " + "from " + controlTabName + " where record_group_no = ? ";
        Object[] param = new Object[] {recordGroupNo};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[登錄群組代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("record_cnt_cond").equals("Y"))
      wp.itemSet("record_cnt_cond", "N");
    if (!wp.itemStr("record_id_cond").equals("Y"))
      wp.itemSet("record_id_cond", "N");
    if (!wp.itemStr("purchase_cond").equals("Y"))
      wp.itemSet("purchase_cond", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if ((wp.itemStr("active_date_s").length() == 0)
          || (wp.itemStr("active_date_e").length() == 0)) {
        errmsg("[活動日期:活動日期起迄日均必須輸入 !");
        return;
      }
      if (wp.itemStr("record_group_name").length() == 0) {
        errmsg("[登錄活動名稱]必須輸入 !");
        return;
      }
      if (wp.itemStr("record_cnt_cond").equals("Y")) {
        if (wp.itemStr("record_cnt").length() == 0)
          wp.itemSet("record_cnt", "0");
        if (wp.itemNum("record_cnt") == 0) {
          errmsg("[登錄組數:登錄組數值必須大於 0 !");
          return;
        }
      }


    }
    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("active_date_s") && (!wp.itemEmpty("ACTIVE_DATE_e")))
        if (wp.itemStr("active_date_s").compareTo(wp.itemStr("ACTIVE_DATE_e")) > 0) {
          errmsg("活動日期：[" + wp.itemStr("active_date_s") + "]>[" + wp.itemStr("ACTIVE_DATE_e")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("record_group_name")) {
        errmsg("登錄活動名稱： 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("active_date_s")) {
        errmsg("活動日期： 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("active_date_e")) {
        errmsg("~ 不可空白");
        return;
      }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD4T();
    dbInsertI4T();

    strSql = " insert into  " + controlTabName + " (" + " record_group_no, " + " aud_type, "
        + " record_group_name, " + " active_date_s, " + " active_date_e, " + " voice_record_sel, "
        + " web_record_sel, " + " merchant_sel, " + " mcht_group_sel, " + " record_cnt_cond, "
        + " record_cnt, " + " record_id_cond, " + " purchase_cond, " + " crt_date, " + " crt_user, "
        + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {recordGroupNo, wp.itemStr("aud_type"), wp.itemStr("record_group_name"),
        wp.itemStr("active_date_s"), wp.itemStr("active_date_e"), wp.itemStr("voice_record_sel"),
        wp.itemStr("web_record_sel"), wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"),
        wp.itemStr("record_cnt_cond"), wp.itemNum("record_cnt"), wp.itemStr("record_id_cond"),
        wp.itemStr("purchase_cond"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI4T() {
    msgOK();

    strSql = "insert into MKT_BN_DATA_T " + "select * " + "from MKT_BN_DATA "
        + "where table_name  =  'WEB_RECORD_GROUP' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("record_group_no"),};

    sqlExec(strSql, param);


    return 1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "record_group_name = ?, "
        + "active_date_s = ?, " + "active_date_e = ?, " + "voice_record_sel = ?, "
        + "web_record_sel = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, "
        + "record_cnt_cond = ?, " + "record_cnt = ?, " + "record_id_cond = ?, "
        + "purchase_cond = ?, " + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), "
        + "mod_user  = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, "
        + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("record_group_name"), wp.itemStr("active_date_s"),
        wp.itemStr("active_date_e"), wp.itemStr("voice_record_sel"), wp.itemStr("web_record_sel"),
        wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"), wp.itemStr("record_cnt_cond"),
        wp.itemNum("record_cnt"), wp.itemStr("record_id_cond"), wp.itemStr("purchase_cond"),
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"),
        wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD4T();

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
  public int dbInsertD4T() {
    msgOK();

    strSql = "delete MKT_BN_DATA_T " + " where table_name  =  'WEB_RECORD_GROUP' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("record_group_no"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_BN_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertI4() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0660_vore"))
      dataType = "1";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "data_code3," + "crt_date, " + "crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + ") values (" + "'WEB_RECORD_GROUP', "
        + "?, " + "?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1,"
        + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("record_group_no"), varsStr("data_code"),
        varsStr("data_code2"), varsStr("data_code3"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0660_vore"))
      dataType = "1";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("record_group_no")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'WEB_RECORD_GROUP' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'WEB_RECORD_GROUP'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0660_were"))
      dataType = "2";
    if (wp.respHtml.equals("mktm0660_aaa1"))
      dataType = "4";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'WEB_RECORD_GROUP', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("record_group_no"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0660_were"))
      dataType = "2";
    if (wp.respHtml.equals("mktm0660_aaa1"))
      dataType = "4";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("record_group_no")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'WEB_RECORD_GROUP' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'WEB_RECORD_GROUP'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0660_mrch"))
      dataType = "3";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'WEB_RECORD_GROUP', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("record_group_no"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0660_mrch"))
      dataType = "3";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("record_group_no")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'WEB_RECORD_GROUP' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'WEB_RECORD_GROUP'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa1(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"WEB_RECORD_GROUP", wp.itemStr("record_group_no"), "3"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg) {
    dateTime();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    comr.setConn(wp);

    if (!comm.isNumber(errMsg[10]))
      errMsg[10] = "0";
    if (!comm.isNumber(errMsg[1]))
      errMsg[1] = "0";
    if (!comm.isNumber(errMsg[2]))
      errMsg[2] = "0";

    strSql = " insert into ecs_media_errlog (" + " crt_date, " + " crt_time, " + " file_name, "
        + " unit_code, " + " main_desc, " + " error_seq, " + " error_desc, " + " line_seq, "
        + " column_seq, " + " column_data, " + " trans_seqno, " + " column_desc, "
        + " program_code, " + " mod_time, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?," // 10
                                                                                                   // record
        + "?,?,?," // 4 trvotfd
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, wp.itemStr("zz_file_name"),
        comr.getObjectOwner("3", wp.modPgm()), errMsg[0], Integer.valueOf(errMsg[1]), errMsg[4],
        Integer.valueOf(errMsg[10]), Integer.valueOf(errMsg[2]), errMsg[3], tranSeqStr, errMsg[5],
        wp.modPgm(), wp.sysDate + wp.sysTime, wp.modPgm()};

    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_media_errlog 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt) {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    dateTime();
    strSql = " insert into ecs_notify_log (" + " crt_date, " + " crt_time, " + " unit_code, "
        + " obj_type, " + " notify_head, " + " notify_name, " + " notify_desc1, "
        + " notify_desc2, " + " trans_seqno, " + " mod_time, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?," // 9 record
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, comr.getObjectOwner("3", wp.modPgm()),
        "3", "媒體檔轉入資料有誤(只記錄前100筆)", "媒體檔名:" + wp.itemStr("zz_file_name"),
        "程式 " + wp.modPgm() + " 轉 " + wp.itemStr("zz_file_name") + " 有" + errorCnt + " 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤", tranSeqStr, wp.sysDate + wp.sysTime, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_modify_log 錯誤");
    return rc;
  }
  // ************************************************************************

}  // End of class
