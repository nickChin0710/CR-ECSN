/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/02  V1.00.01   Allen Ho      Initial                              *
* 109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                          *
* 112-04-03  V1.00.03   yang han       增加 [一般消費群組]選項
* 112-05-08  V1.00.04   Zuwei Su       一般消費群組data_type寫入'P'
*****************************************************************************/
package dbmm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmm0190Func extends FuncEdit {
  private final String PROGNAME = "Visa金融卡紅利加贈參數維護-新發卡處理程式113-04-03  V1.00.03";
  String activeCode;
  String orgControlTabName = "dbm_bpis";
  String controlTabName = "dbm_bpis_t";

  public Dbmm0190Func(TarokoCommon wr) {
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
    strSql = " select " + " active_name, " + " give_code, " + " tax_flag, " + " new_card_cond, "
        + " active_s_date, " + " active_e_date, " + " re_months, " + " acct_type_sel, "
        + " group_code_sel, " + " merchant_sel, " + " mcht_group_sel, " + 
        "platform_kind_sel, "+ " mcc_code_sel, "
        + " pos_entry_sel, " + " bp_amt, " + " bp_pnt, " + " add_times, " + " add_point, "
        + " give_name, "
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
      activeCode = wp.itemStr("active_code");
      if (empty(activeCode)) {
        errmsg("活動代號 不可空白");
        return;
      }
    } else {
      activeCode = wp.itemStr("active_code");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (activeCode.length() > 0) {
          strSql = "select count(*) as qua " + "from " + orgControlTabName
              + " where active_code = ? ";
          Object[] param = new Object[] {activeCode};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[活動代號] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (activeCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where active_code = ? ";
        Object[] param = new Object[] {activeCode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[活動代號] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("new_card_cond").equals("Y"))
      wp.itemSet("new_card_cond", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if ((wp.itemStr("active_s_date").length() == 0)
          || (wp.itemStr("active_e_date").length() == 0)) {
        errmsg("活動期間起迄, 均不可為空白!");
        return;
      }

      if (wp.itemStr("re_months").length() == 0)
        wp.itemSet("re_months", "0");
      if (wp.itemNum("re_months") == 0) {
        errmsg("[活動回饋類別] ，需指定開卡回饋月數!");
        return;
      }

      if (wp.itemStr("bp_amt").length() == 0)
        wp.itemSet("bp_amt", "0");
      if (wp.itemStr("bp_pnt").length() == 0)
        wp.itemSet("bp_pnt", "0");
      if ((wp.itemNum("bp_amt") == 0) || (wp.itemNum("bp_pnt") == 0)) {
        errmsg("每筆交易金額與兌換點數, 不可為 0!");
        return;
      }
      if (wp.itemStr("add_times").length() == 0)
        wp.itemSet("add_times", "0");
      if (wp.itemStr("add_point").length() == 0)
        wp.itemSet("add_point", "0");
      if ((wp.itemNum("add_times") == 0) && (wp.itemNum("add_point") == 0)) {
        errmsg("加贈倍數與點數, 不可同時為 0!");
        return;
      }
      strSql = "select " + " wf_desc as give_name " + " from ptr_sys_idtab "
          + " where wf_type='GIVE_CODE' " + " and wf_id = ? ";
      Object[] param = new Object[] {wp.itemStr("give_code")};
      sqlSelect(strSql, param);

      wp.itemSet("give_name", colStr("give_name"));

    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("active_s_date") && (!wp.itemEmpty("issue_e_date")))
        if (wp.itemStr("active_s_date").compareTo(wp.itemStr("issue_e_date")) > 0) {
          errmsg("開卡期間[" + wp.itemStr("active_s_date") + "]>[" + wp.itemStr("issue_e_date")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if (checkDecnum(wp.itemStr("add_times"), 3, 4) != 0) {
      errmsg("加贈點數 格式超出範圍 : [3][4]");
      return;
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("give_code")) {
        errmsg("贈送代碼 不可空白");
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

    dbInsertD2T();
    dbInsertI2T();

    strSql = " insert into  " + controlTabName + " (" + " active_code, " + " aud_type, "
        + " active_name, " + " give_code, " + " tax_flag, " + " new_card_cond, "
        + " active_s_date, " + " active_e_date, " + " re_months, " + " acct_type_sel, "
        + " group_code_sel, " + " merchant_sel, " + " mcht_group_sel, " + "platform_kind_sel," + " mcc_code_sel, "
        + " pos_entry_sel, " + " bp_amt, " + " bp_pnt, " + " add_times, " + " add_point, "
        + " give_name, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {activeCode, wp.itemStr("aud_type"), wp.itemStr("active_name"),
        wp.itemStr("give_code"), wp.itemStr("tax_flag"), wp.itemStr("new_card_cond"),
        wp.itemStr("active_s_date"), wp.itemStr("active_e_date"), wp.itemNum("re_months"),
        wp.itemStr("acct_type_sel"), wp.itemStr("group_code_sel"), wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"),wp.itemStr("platform_kind_sel"),  wp.itemStr("mcc_code_sel"), wp.itemStr("pos_entry_sel"),
        wp.itemNum("bp_amt"), wp.itemNum("bp_pnt"), wp.itemNum("add_times"),
        wp.itemNum("add_point"), wp.itemStr("give_name"), wp.loginUser, wp.modSeqno(), wp.loginUser,
        wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into DBM_BN_DATA_T " + "select * " + "from DBM_BN_DATA "
        + "where table_name  =  'DBM_BPIS' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("active_code"),};

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

    strSql = "update " + controlTabName + " set " + "active_name = ?, " + "give_code = ?, "
        + "tax_flag = ?, " + "new_card_cond = ?, " + "active_s_date = ?, " + "active_e_date = ?, "
        + "re_months = ?, " + "acct_type_sel = ?, " + "group_code_sel = ?, " + "merchant_sel = ?, "
        + "mcht_group_sel = ?, "  + "platform_kind_sel = ?, "+ "mcc_code_sel = ?, " + "pos_entry_sel = ?, " + "bp_amt = ?, "
        + "bp_pnt = ?, " + "add_times = ?, " + "add_point = ?, " + "give_name = ?, "
        + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param =
        new Object[] {wp.itemStr("active_name"), wp.itemStr("give_code"), wp.itemStr("tax_flag"),
            wp.itemStr("new_card_cond"), wp.itemStr("active_s_date"), wp.itemStr("active_e_date"),
            wp.itemNum("re_months"), wp.itemStr("acct_type_sel"), wp.itemStr("group_code_sel"),
            wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"), wp.itemStr("platform_kind_sel"), wp.itemStr("mcc_code_sel"),
            wp.itemStr("pos_entry_sel"), wp.itemNum("bp_amt"), wp.itemNum("bp_pnt"),
            wp.itemNum("add_times"), wp.itemNum("add_point"), wp.itemStr("give_name"), wp.loginUser,
            wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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

    dbInsertD2T();

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
  public int dbInsertD2T() {
    msgOK();

    strSql =
        "delete DBM_BN_DATA_T " + " where table_name  =  'DBM_BPIS' " + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 DBM_BN_DATA_T 錯誤");

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
  public int dbInsertI2() throws Exception {
    msgOK();

    String data_type = "";
    if (wp.respHtml.equals("dbmm0190_acty"))
      data_type = "3";
    if (wp.respHtml.equals("dbmm0190_grop"))
      data_type = "2";
    if (wp.respHtml.equals("dbmm0190_aaa1"))
      data_type = "6";
    if (wp.respHtml.equals("dbmm0190_platform"))
        data_type = "P";
    if (wp.respHtml.equals("dbmm0190_mccd"))
      data_type = "5";
    if (wp.respHtml.equals("dbmm0190_enty"))
      data_type = "4";
    strSql = "insert into DBM_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'DBM_BPIS', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {data_type, wp.itemStr("active_code"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 DBM_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("dbmm0190_acty"))
      dataType = "3";
    if (wp.respHtml.equals("dbmm0190_grop"))
      dataType = "2";
    if (wp.respHtml.equals("dbmm0190_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("dbmm0190_platform"))
        dataType = "P";
    if (wp.respHtml.equals("dbmm0190_mccd"))
      dataType = "5";
    if (wp.respHtml.equals("dbmm0190_enty"))
      dataType = "4";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("DBM_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'DBM_BPIS' ",
        param) <= 0)
      return 1;

    strSql = "delete DBM_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'DBM_BPIS'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("dbmm0190_mcht"))
      dataType = "1";
    strSql = "insert into DBM_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'DBM_BPIS', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 DBM_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("dbmm0190_mcht"))
      dataType = "1";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("DBM_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'DBM_BPIS' ",
        param) <= 0)
      return 1;

    strSql = "delete DBM_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'DBM_BPIS'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2Mcht(String tableName, String[] columnCol, String[] columnDat) {
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
  public int dbDeleteD2Mcht(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"DBM_BPIS", wp.itemStr("active_code"), "1"};

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
