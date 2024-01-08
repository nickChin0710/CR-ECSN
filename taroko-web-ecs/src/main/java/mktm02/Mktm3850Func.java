/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm3850Func extends FuncEdit {
  private String PROGNAME = "指定繳款方式基金參數維護處理程式108/12/12 V1.00.01";
  String fundCode;
  String orgControlTabName = "mkt_nfc_parm";
  String controlTabName = "mkt_nfc_parm_t";

  public Mktm3850Func(TarokoCommon wr) {
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
    strSql = " select " + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, "
        + " stop_flag, " + " stop_date, " + " stop_desc, " + " effect_months, "
        + " group_card_sel, " + " group_code_sel, " + " payment_sel, " + " merchant_sel, "
        + " mcht_group_sel, " + " mcc_code_sel, " + " bl_cond, " + " it_cond, " + " ca_cond, "
        + " id_cond, " + " ao_cond, " + " ot_cond, " + " feedback_rate, " + " feedback_lmt, "
        + " cancel_period, " + " cancel_scope, " + " cancel_event, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, "
        + " group_oppost_cond "
        + " from "
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
      fundCode = wp.itemStr("fund_code");
    } else {
      fundCode = wp.itemStr("fund_code");
    }
    
    if (!wp.itemStr("group_oppost_cond").equals("Y")) wp.itemSet("group_oppost_cond","N");
    if (!wp.itemStr("stop_flag").equals("Y"))
      wp.itemSet("stop_flag", "N");
    if (!wp.itemStr("bl_cond").equals("Y"))
      wp.itemSet("bl_cond", "N");
    if (!wp.itemStr("it_cond").equals("Y"))
      wp.itemSet("it_cond", "N");
    if (!wp.itemStr("ca_cond").equals("Y"))
      wp.itemSet("ca_cond", "N");
    if (!wp.itemStr("id_cond").equals("Y"))
      wp.itemSet("id_cond", "N");
    if (!wp.itemStr("ao_cond").equals("Y"))
      wp.itemSet("ao_cond", "N");
    if (!wp.itemStr("ot_cond").equals("Y"))
      wp.itemSet("ot_cond", "N");

    if ((this.ibAdd) && (!wp.itemStr("control_tab_name").equals(orgControlTabName))) {
      strSql = "select type_name " + " from vmkt_fund_name " + " where fund_code =  ? ";
      Object[] param = new Object[] {wp.itemStr("fund_code")};
      sqlSelect(strSql, param);

      if (sqlRowNum > 0) {
        errmsg("[" + colStr("type_name") + "] 已使用本基金代碼!");
        return;
      }

      strSql = "select payment_type " + " from ptr_payment " + " where payment_type =  ? ";
      param = new Object[] {wp.itemStr("fund_code").substring(0, 4)};
      sqlSelect(strSql, param);

      if (sqlRowNum <= 0) {
        errmsg("[" + wp.itemStr("fund_code").substring(0, 4) + "] 不存在於 ptrm0030(繳款類別資料)");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if ((!wp.itemStr("bl_cond").equals("Y")) && (!wp.itemStr("ot_cond").equals("Y"))
          && (!wp.itemStr("it_cond").equals("Y")) && (!wp.itemStr("ca_cond").equals("Y"))
          && (!wp.itemStr("id_cond").equals("Y")) && (!wp.itemStr("ao_cond").equals("Y"))) {
        errmsg("[消費本金類] 至少要選一個!");
        return;
      }
      if (wp.itemStr("feedback_rate").length() == 0)
        wp.itemSet("feedback_rate", "0");
      if (wp.itemNum("feedback_rate") == 0) {
        errmsg("[回饋比例] 不可為 0 !");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("fund_crt_date_s") && (!wp.itemEmpty("FUND_CRT_DATE_E")))
        if (wp.itemStr("fund_crt_date_s").compareTo(wp.itemStr("FUND_CRT_DATE_E")) > 0) {
          errmsg("活動期間：[" + wp.itemStr("fund_crt_date_s") + "]>[" + wp.itemStr("FUND_CRT_DATE_E")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if (checkDecnum(wp.itemStr("feedback_rate"), 3, 2) != 0) {
      errmsg("回饋比例： 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_lmt"), 12, 2) != 0) {
      errmsg("回饋上限： 格式超出範圍 : [12][2]");
      return;
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("fund_name")) {
        errmsg("回饋基金名稱： 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("fund_crt_date_s")) {
        errmsg("活動期間： 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("fund_crt_date_e")) {
        errmsg(" 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("feedback_rate")) {
        errmsg("回饋比例： 不可空白");
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

    dbInsertD3T();
    dbInsertI3T();

    strSql = " insert into  " + controlTabName + " (" + " fund_code, " + " aud_type, "
        + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, " + " stop_flag, "
        + " stop_date, " + " stop_desc, " + " effect_months, " + " group_card_sel, "
        + " group_code_sel, " + " payment_sel, " + " merchant_sel, " + " mcht_group_sel, "
        + " mcc_code_sel, " + " bl_cond, " + " it_cond, " + " ca_cond, " + " id_cond, "
        + " ao_cond, " + " ot_cond, " + " feedback_rate, " + " feedback_lmt, " + " cancel_period, "
        + " cancel_scope, " + " cancel_event, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm, "  
        + " group_oppost_cond "
        + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd'),"
        + "?," + "?," + "sysdate,?,?,?)";

    Object[] param = new Object[] {fundCode, wp.itemStr("aud_type"), wp.itemStr("fund_name"),
        wp.itemStr("fund_crt_date_s"), wp.itemStr("fund_crt_date_e"), wp.itemStr("stop_flag"),
        wp.itemStr("stop_date"), wp.itemStr("stop_desc"), wp.itemNum("effect_months"),
        wp.itemStr("group_card_sel"), wp.itemStr("group_code_sel"), wp.itemStr("payment_sel"),
        wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"), wp.itemStr("mcc_code_sel"),
        wp.itemStr("bl_cond"), wp.itemStr("it_cond"), wp.itemStr("ca_cond"), wp.itemStr("id_cond"),
        wp.itemStr("ao_cond"), wp.itemStr("ot_cond"), wp.itemNum("feedback_rate"),
        wp.itemNum("feedback_lmt"), wp.itemStr("cancel_period"), wp.itemStr("cancel_scope"),
        wp.itemStr("cancel_event"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()
        ,wp.itemStr("group_oppost_cond")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI3T() {
    msgOK();

    strSql = "insert into MKT_PARM_DATA_T " + "select * " + "from MKT_PARM_DATA "
        + "where table_name  =  'MKT_NFC_PARM' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("fund_code"),};

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

    strSql = "update " + controlTabName + " set " + "fund_name = ?, " + "fund_crt_date_s = ?, "
        + "fund_crt_date_e = ?, " + "stop_flag = ?, " + "stop_date = ?, " + "stop_desc = ?, "
        + "effect_months = ?, " + "group_card_sel = ?, " + "group_code_sel = ?, "
        + "payment_sel = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "mcc_code_sel = ?, "
        + "bl_cond = ?, " + "it_cond = ?, " + "ca_cond = ?, " + "id_cond = ?, " + "ao_cond = ?, "
        + "ot_cond = ?, " + "feedback_rate = ?, " + "feedback_lmt = ?, " + "cancel_period = ?, "
        + "cancel_scope = ?, " + "cancel_event = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ?, "
        + "group_oppost_cond = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("fund_name"), wp.itemStr("fund_crt_date_s"),
        wp.itemStr("fund_crt_date_e"), wp.itemStr("stop_flag"), wp.itemStr("stop_date"),
        wp.itemStr("stop_desc"), wp.itemNum("effect_months"), wp.itemStr("group_card_sel"),
        wp.itemStr("group_code_sel"), wp.itemStr("payment_sel"), wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"), wp.itemStr("mcc_code_sel"), wp.itemStr("bl_cond"),
        wp.itemStr("it_cond"), wp.itemStr("ca_cond"), wp.itemStr("id_cond"), wp.itemStr("ao_cond"),
        wp.itemStr("ot_cond"), wp.itemNum("feedback_rate"), wp.itemNum("feedback_lmt"),
        wp.itemStr("cancel_period"), wp.itemStr("cancel_scope"), wp.itemStr("cancel_event"),
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")
        , wp.itemStr("group_oppost_cond")
        , wp.itemRowId("rowid"),
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

    dbInsertD3T();

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
  public int dbInsertD3T() {
    msgOK();

    strSql = "delete MKT_PARM_DATA_T " + " where table_name  =  'MKT_NFC_PARM' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("fund_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_PARM_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm3850_gpcd"))
      dataType = "1";
    if (wp.respHtml.equals("mktm3850_mrch"))
      dataType = "4";
    strSql = "insert into MKT_PARM_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_NFC_PARM', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("fund_code"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_PARM_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm3850_gpcd"))
      dataType = "1";
    if (wp.respHtml.equals("mktm3850_mrch"))
      dataType = "4";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("fund_code")};
    if (sqlRowcount("MKT_PARM_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_NFC_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_PARM_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_NFC_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm3850_grcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm3850_paym"))
      dataType = "3";
    if (wp.respHtml.equals("mktm3850_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm3850_mccd"))
      dataType = "5";
    strSql = "insert into MKT_PARM_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_NFC_PARM', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("fund_code"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_PARM_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm3850_grcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm3850_paym"))
      dataType = "3";
    if (wp.respHtml.equals("mktm3850_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm3850_mccd"))
      dataType = "5";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("fund_code")};
    if (sqlRowcount("MKT_PARM_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_NFC_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_PARM_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_NFC_PARM'  ";
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

    Object[] param = new Object[] {"MKT_NFC_PARM", wp.itemStr("fund_code"), "4"};

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

} // End of class
