/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktm6040Func extends FuncEdit {
  private String PROGNAME = "專案免年費參數檔維護處理程式108/12/12 V1.00.01";
  String projectNo;
  String orgControlTabName = "cyc_anul_project";
  String controlTabName = "cyc_anul_project_t";

  public Mktm6040Func(TarokoCommon wr) {
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
    strSql = " select " + " project_name, " + " acct_type_sel, " + " group_code_sel, "
        + " source_code_sel, " + " recv_month_tag, " + " recv_s_date, " + " recv_e_date, "
        + " issue_date_tag, " + " issue_date_s, " + " issue_date_e, " + " mcard_cond, "
        + " scard_cond, " + " cnt_months_tag, " + " cnt_months, " + " accumulate_cnt, "
        + " average_amt, " + " cnt_bl_flag, " + " cnt_ca_flag, " + " cnt_it_flag, "
        + " cnt_ao_flag, " + " cnt_id_flag, " + " cnt_ot_flag, " + " amt_months_tag, "
        + " amt_months, " + " accumulate_amt, " + " amt_bl_flag, " + " amt_ca_flag, "
        + " amt_it_flag, " + " amt_ao_flag, " + " amt_id_flag, " + " amt_ot_flag, "
        + " adv_months_tag, " + " adv_months, " + " adv_cnt, " + " adv_amt, " + " mcode, "
        + " free_fee_cnt, "
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
      projectNo = wp.itemStr("project_no");
      if (empty(projectNo)) {
        errmsg("專案代號 不可空白");
        return;
      }
    } else {
      projectNo = wp.itemStr("project_no");
    }
    if (!wp.itemStr("recv_month_tag").equals("Y"))
      wp.itemSet("recv_month_tag", "N");
    if (!wp.itemStr("issue_date_tag").equals("Y"))
      wp.itemSet("issue_date_tag", "N");
    if (!wp.itemStr("mcard_cond").equals("Y"))
      wp.itemSet("mcard_cond", "N");
    if (!wp.itemStr("scard_cond").equals("Y"))
      wp.itemSet("scard_cond", "N");
    if (!wp.itemStr("cnt_months_tag").equals("Y"))
      wp.itemSet("cnt_months_tag", "N");
    if (!wp.itemStr("cnt_bl_flag").equals("Y"))
      wp.itemSet("cnt_bl_flag", "N");
    if (!wp.itemStr("cnt_ca_flag").equals("Y"))
      wp.itemSet("cnt_ca_flag", "N");
    if (!wp.itemStr("cnt_it_flag").equals("Y"))
      wp.itemSet("cnt_it_flag", "N");
    if (!wp.itemStr("cnt_ao_flag").equals("Y"))
      wp.itemSet("cnt_ao_flag", "N");
    if (!wp.itemStr("cnt_id_flag").equals("Y"))
      wp.itemSet("cnt_id_flag", "N");
    if (!wp.itemStr("cnt_ot_flag").equals("Y"))
      wp.itemSet("cnt_ot_flag", "N");
    if (!wp.itemStr("amt_months_tag").equals("Y"))
      wp.itemSet("amt_months_tag", "N");
    if (!wp.itemStr("amt_bl_flag").equals("Y"))
      wp.itemSet("amt_bl_flag", "N");
    if (!wp.itemStr("amt_ca_flag").equals("Y"))
      wp.itemSet("amt_ca_flag", "N");
    if (!wp.itemStr("amt_it_flag").equals("Y"))
      wp.itemSet("amt_it_flag", "N");
    if (!wp.itemStr("amt_ao_flag").equals("Y"))
      wp.itemSet("amt_ao_flag", "N");
    if (!wp.itemStr("amt_id_flag").equals("Y"))
      wp.itemSet("amt_id_flag", "N");
    if (!wp.itemStr("amt_ot_flag").equals("Y"))
      wp.itemSet("amt_ot_flag", "N");
    if (!wp.itemStr("adv_months_tag").equals("Y"))
      wp.itemSet("adv_months_tag", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("free_fee_cnt").length() == 0)
        wp.itemSet("free_fee_cnt", "0");
      if (wp.itemNum("free_fee_cnt") <= 0) {
        errmsg("可免年費年數必須大於或等於1");
        return;
      }
      /*
       * if ((!wp.item_ss("cnt_months_tag").equals("Y"))&&
       * (!wp.item_ss("amt_months_tag").equals("Y"))&& (!wp.item_ss("adv_months_tag").equals("Y")))
       * { errmsg("A,B,C 選項至少咬選一個"); return; }
       */

      if (wp.itemStr("cnt_months_tag").equals("Y")) {
        if (wp.itemStr("cnt_months").length() == 0)
          wp.itemSet("cnt_months", "0");
        if (wp.itemNum("cnt_months") < 1) {
          errmsg("A.發卡後 N 月必須大於或等於1");
          return;
        }
        if (wp.itemNum("cnt_months") > 10) {
          errmsg("A.發卡後 N 月必須小於或等於6");
          return;
        }
        if (wp.itemStr("accumulate_cnt").length() == 0)
          wp.itemSet("accumulate_cnt", "0");
        if (wp.itemStr("average_amt").length() == 0)
          wp.itemSet("average_amt", "0");
        if ((wp.itemNum("accumulate_cnt") <= 0) && (wp.itemNum("average_amt") <= 0)) {
          errmsg("累積刷卡次數與平均每次金額不可同時為0");
          return;
        }
        if ((!wp.itemStr("cnt_bl_flag").equals("Y")) && (!wp.itemStr("cnt_ca_flag").equals("Y"))
            && (!wp.itemStr("cnt_id_flag").equals("Y")) && (!wp.itemStr("cnt_it_flag").equals("Y"))
            && (!wp.itemStr("cnt_ao_flag").equals("Y"))
            && (!wp.itemStr("cnt_ot_flag").equals("Y"))) {
          errmsg("A.消費金額本金類至少要選一個");
          return;
        }
      }
      if (wp.itemStr("amt_months_tag").equals("Y")) {
        if (wp.itemStr("amt_months").length() == 0)
          wp.itemSet("amt_months", "0");
        if (wp.itemNum("amt_months") < 1) {
          errmsg("B.發卡後 N 月必須大於或等於1");
          return;
        }
        if (wp.itemNum("amt_months") > 10) {
          errmsg("B.發卡後 N 月必須小於或等於6");
          return;
        }
        if (wp.itemStr("accumulate_amt").length() == 0)
          wp.itemSet("accumulate_amt", "0");
        if (wp.itemStr("average_amt").length() == 0)
          wp.itemSet("average_amt", "0");
        if (wp.itemNum("accumulate_cnt") <= 0) {
          errmsg("累積消費總不可為0");
          return;
        }
        if ((!wp.itemStr("amt_bl_flag").equals("Y")) && (!wp.itemStr("amt_ca_flag").equals("Y"))
            && (!wp.itemStr("amt_id_flag").equals("Y")) && (!wp.itemStr("amt_it_flag").equals("Y"))
            && (!wp.itemStr("amt_ao_flag").equals("Y"))
            && (!wp.itemStr("amt_ot_flag").equals("Y"))) {
          errmsg("B.消費金額本金類至少要選一個");
          return;
        }
      }
      if (wp.itemStr("adv_months_tag").equals("Y")) {
        if (wp.itemStr("adv_months").length() == 0)
          wp.itemSet("adv_months", "0");
        if (wp.itemNum("adv_months") < 1) {
          errmsg("C.發卡後 N 月必須大於或等於1");
          return;
        }
        if (wp.itemNum("adv_months") > 10) {
          errmsg("C.發卡後 N 月必須小於或等於6");
          return;
        }
        if (wp.itemStr("adv_cnt").length() == 0)
          wp.itemSet("adv_cnt", "0");
        if (wp.itemStr("adv_amt").length() == 0)
          wp.itemSet("adv_amt", "0");
        if ((wp.itemNum("adv_cnt") <= 0) && (wp.itemNum("adv_amt") <= 0)) {
          errmsg("累積預借現金次數累積金額不可同時為0");
          return;
        }
      }
    }
    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("recv_s_date") && (!wp.itemEmpty("recv_e_date")))
        if (wp.itemStr("recv_s_date").compareTo(wp.itemStr("recv_e_date")) > 0) {
          errmsg("收件期間:[" + wp.itemStr("recv_s_date") + "]>[" + wp.itemStr("recv_e_date")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("issue_date_s") && (!wp.itemEmpty("issue_date_e")))
        if (wp.itemStr("issue_date_s").compareTo(wp.itemStr("issue_date_e")) > 0) {
          errmsg("發卡期間:[" + wp.itemStr("issue_date_s") + "]>[" + wp.itemStr("issue_date_e")
              + "] 起迄值錯誤!");
          return;
        }
    }

    if (checkDecnum(wp.itemStr("average_amt"), 11, 3) != 0) {
      errmsg("，或平均每次達 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("accumulate_amt"), 11, 3) != 0) {
      errmsg("滿 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("adv_amt"), 11, 3) != 0) {
      errmsg("，且累積金額達 格式超出範圍 : [11][3]");
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

    strSql = " insert into  " + controlTabName + " (" + " project_no, " + " aud_type, "
        + " project_name, " + " acct_type_sel, " + " group_code_sel, " + " source_code_sel, "
        + " recv_month_tag, " + " recv_s_date, " + " recv_e_date, " + " issue_date_tag, "
        + " issue_date_s, " + " issue_date_e, " + " mcard_cond, " + " scard_cond, "
        + " cnt_months_tag, " + " cnt_months, " + " accumulate_cnt, " + " average_amt, "
        + " cnt_bl_flag, " + " cnt_ca_flag, " + " cnt_it_flag, " + " cnt_ao_flag, "
        + " cnt_id_flag, " + " cnt_ot_flag, " + " amt_months_tag, " + " amt_months, "
        + " accumulate_amt, " + " amt_bl_flag, " + " amt_ca_flag, " + " amt_it_flag, "
        + " amt_ao_flag, " + " amt_id_flag, " + " amt_ot_flag, " + " adv_months_tag, "
        + " adv_months, " + " adv_cnt, " + " adv_amt, " + " mcode, " + " free_fee_cnt, "
        + " crt_date, " + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm "
        + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {projectNo, wp.itemStr("aud_type"), wp.itemStr("project_name"),
        wp.itemStr("acct_type_sel"), wp.itemStr("group_code_sel"), wp.itemStr("source_code_sel"),
        wp.itemStr("recv_month_tag"), wp.itemStr("recv_s_date"), wp.itemStr("recv_e_date"),
        wp.itemStr("issue_date_tag"), wp.itemStr("issue_date_s"), wp.itemStr("issue_date_e"),
        wp.itemStr("mcard_cond"), wp.itemStr("scard_cond"), wp.itemStr("cnt_months_tag"),
        wp.itemNum("cnt_months"), wp.itemNum("accumulate_cnt"), wp.itemNum("average_amt"),
        wp.itemStr("cnt_bl_flag"), wp.itemStr("cnt_ca_flag"), wp.itemStr("cnt_it_flag"),
        wp.itemStr("cnt_ao_flag"), wp.itemStr("cnt_id_flag"), wp.itemStr("cnt_ot_flag"),
        wp.itemStr("amt_months_tag"), wp.itemNum("amt_months"), wp.itemNum("accumulate_amt"),
        wp.itemStr("amt_bl_flag"), wp.itemStr("amt_ca_flag"), wp.itemStr("amt_it_flag"),
        wp.itemStr("amt_ao_flag"), wp.itemStr("amt_id_flag"), wp.itemStr("amt_ot_flag"),
        wp.itemStr("adv_months_tag"), wp.itemNum("adv_months"), wp.itemNum("adv_cnt"),
        wp.itemNum("adv_amt"), wp.itemStr("mcode"), wp.itemNum("free_fee_cnt"), wp.loginUser,
        wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into CYC_BN_DATA_T " + "select * " + "from CYC_BN_DATA "
        + "where table_name  =  'CYC_ANUL_PROJECT' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("project_no"),};

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

    strSql = "update " + controlTabName + " set " + "project_name = ?, " + "acct_type_sel = ?, "
        + "group_code_sel = ?, " + "source_code_sel = ?, " + "recv_month_tag = ?, "
        + "recv_s_date = ?, " + "recv_e_date = ?, " + "issue_date_tag = ?, " + "issue_date_s = ?, "
        + "issue_date_e = ?, " + "mcard_cond = ?, " + "scard_cond = ?, " + "cnt_months_tag = ?, "
        + "cnt_months = ?, " + "accumulate_cnt = ?, " + "average_amt = ?, " + "cnt_bl_flag = ?, "
        + "cnt_ca_flag = ?, " + "cnt_it_flag = ?, " + "cnt_ao_flag = ?, " + "cnt_id_flag = ?, "
        + "cnt_ot_flag = ?, " + "amt_months_tag = ?, " + "amt_months = ?, " + "accumulate_amt = ?, "
        + "amt_bl_flag = ?, " + "amt_ca_flag = ?, " + "amt_it_flag = ?, " + "amt_ao_flag = ?, "
        + "amt_id_flag = ?, " + "amt_ot_flag = ?, " + "adv_months_tag = ?, " + "adv_months = ?, "
        + "adv_cnt = ?, " + "adv_amt = ?, " + "mcode = ?, " + "free_fee_cnt = ?, "
        + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("project_name"), wp.itemStr("acct_type_sel"),
        wp.itemStr("group_code_sel"), wp.itemStr("source_code_sel"), wp.itemStr("recv_month_tag"),
        wp.itemStr("recv_s_date"), wp.itemStr("recv_e_date"), wp.itemStr("issue_date_tag"),
        wp.itemStr("issue_date_s"), wp.itemStr("issue_date_e"), wp.itemStr("mcard_cond"),
        wp.itemStr("scard_cond"), wp.itemStr("cnt_months_tag"), wp.itemNum("cnt_months"),
        wp.itemNum("accumulate_cnt"), wp.itemNum("average_amt"), wp.itemStr("cnt_bl_flag"),
        wp.itemStr("cnt_ca_flag"), wp.itemStr("cnt_it_flag"), wp.itemStr("cnt_ao_flag"),
        wp.itemStr("cnt_id_flag"), wp.itemStr("cnt_ot_flag"), wp.itemStr("amt_months_tag"),
        wp.itemNum("amt_months"), wp.itemNum("accumulate_amt"), wp.itemStr("amt_bl_flag"),
        wp.itemStr("amt_ca_flag"), wp.itemStr("amt_it_flag"), wp.itemStr("amt_ao_flag"),
        wp.itemStr("amt_id_flag"), wp.itemStr("amt_ot_flag"), wp.itemStr("adv_months_tag"),
        wp.itemNum("adv_months"), wp.itemNum("adv_cnt"), wp.itemNum("adv_amt"), wp.itemStr("mcode"),
        wp.itemNum("free_fee_cnt"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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

    strSql = "delete CYC_BN_DATA_T " + " where table_name  =  'CYC_ANUL_PROJECT' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("project_no"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 CYC_BN_DATA_T 錯誤");

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

    String dataType = "";
    if (wp.respHtml.equals("mktm6040_acty"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6040_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6040_srcd"))
      dataType = "3";
    strSql = "insert into CYC_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'CYC_ANUL_PROJECT', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("project_no"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 CYC_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6040_acty"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6040_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6040_srcd"))
      dataType = "3";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("project_no")};
    if (sqlRowcount("CYC_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'CYC_ANUL_PROJECT' ",
        param) <= 0)
      return 1;

    strSql = "delete CYC_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'CYC_ANUL_PROJECT'  ";
    sqlExec(strSql, param);


    return 1;

  }
  // ************************************************************************

} // End of class
