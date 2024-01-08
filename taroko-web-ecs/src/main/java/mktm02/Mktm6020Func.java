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
public class Mktm6020Func extends FuncEdit {
  private String PROGNAME = "高階卡友參數維護處理程式108/12/12 V1.00.01";
  String groupCode, cardType;
  String orgControlTabName = "cyc_anul_gp";
  String controlTabName = "cyc_anul_gp_t";

  public Mktm6020Func(TarokoCommon wr) {
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
    strSql = " select " + " card_fee, " + " sup_card_fee, " + " mer_cond, " + " mer_bl_flag, "
        + " mer_ca_flag, " + " mer_it_flag, " + " mer_ao_flag, " + " mer_id_flag, "
        + " mer_ot_flag, " + " major_flag, " + " sub_flag, " + " major_sub, " + " a_merchant_sel, "
        + " a_mcht_group_sel, " + " cnt_cond, " + " cnt_select, " + " month_cnt, "
        + " accumlate_cnt, " + " cnt_bl_flag, " + " cnt_ca_flag, " + " cnt_it_flag, "
        + " cnt_ao_flag, " + " cnt_id_flag, " + " cnt_ot_flag, " + " cnt_major_flag, "
        + " cnt_sub_flag, " + " cnt_major_sub, " + " b_mcc_code_sel, " + " b_merchant_sel, "
        + " b_mcht_group_sel, " + " amt_cond, " + " accumlate_amt, " + " amt_bl_flag, "
        + " amt_ca_flag, " + " amt_it_flag, " + " amt_ao_flag, " + " amt_id_flag, "
        + " amt_ot_flag, " + " amt_major_flag, " + " amt_sub_flag, " + " amt_major_sub, "
        + " c_mcc_code_sel, " + " c_merchant_sel, " + " c_mcht_group_sel, " + " mcode, "
        + " email_nopaper_flag, "
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
      groupCode = wp.itemStr("group_code");
      if (empty(groupCode)) {
        errmsg("團體代號 不可空白");
        return;
      }
      cardType = wp.itemStr("card_type");
    } else {
      groupCode = wp.itemStr("group_code");
      cardType = wp.itemStr("card_type");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (groupCode.length() > 0) {
          strSql = "select count(*) as qua " + "from " + orgControlTabName
              + " where group_code = ? " + "and   card_type = ? ";
          Object[] param = new Object[] {groupCode, cardType};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[團體代號][卡種] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (groupCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where group_code = ? "
            + " and   card_type = ? ";
        Object[] param = new Object[] {groupCode, cardType};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[團體代號][卡種] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("mer_cond").equals("Y"))
      wp.itemSet("mer_cond", "N");
    if (!wp.itemStr("mer_bl_flag").equals("Y"))
      wp.itemSet("mer_bl_flag", "N");
    if (!wp.itemStr("mer_ca_flag").equals("Y"))
      wp.itemSet("mer_ca_flag", "N");
    if (!wp.itemStr("mer_it_flag").equals("Y"))
      wp.itemSet("mer_it_flag", "N");
    if (!wp.itemStr("mer_ao_flag").equals("Y"))
      wp.itemSet("mer_ao_flag", "N");
    if (!wp.itemStr("mer_id_flag").equals("Y"))
      wp.itemSet("mer_id_flag", "N");
    if (!wp.itemStr("mer_ot_flag").equals("Y"))
      wp.itemSet("mer_ot_flag", "N");
    if (!wp.itemStr("major_flag").equals("Y"))
      wp.itemSet("major_flag", "N");
    if (!wp.itemStr("sub_flag").equals("Y"))
      wp.itemSet("sub_flag", "N");
    if (!wp.itemStr("major_sub").equals("Y"))
      wp.itemSet("major_sub", "N");
    if (!wp.itemStr("cnt_cond").equals("Y"))
      wp.itemSet("cnt_cond", "N");
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
    if (!wp.itemStr("cnt_major_flag").equals("Y"))
      wp.itemSet("cnt_major_flag", "N");
    if (!wp.itemStr("cnt_sub_flag").equals("Y"))
      wp.itemSet("cnt_sub_flag", "N");
    if (!wp.itemStr("cnt_major_sub").equals("Y"))
      wp.itemSet("cnt_major_sub", "N");
    if (!wp.itemStr("amt_cond").equals("Y"))
      wp.itemSet("amt_cond", "N");
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
    if (!wp.itemStr("amt_major_flag").equals("Y"))
      wp.itemSet("amt_major_flag", "N");
    if (!wp.itemStr("amt_sub_flag").equals("Y"))
      wp.itemSet("amt_sub_flag", "N");
    if (!wp.itemStr("amt_major_sub").equals("Y"))
      wp.itemSet("amt_major_sub", "N");
    if (!wp.itemStr("email_nopaper_flag").equals("Y"))
      wp.itemSet("email_nopaper_flag", "N");

    if (this.ibAdd) {
      if (cardType.length() != 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where group_code = ? "
            + "and   card_type = '' ";
      } else {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where group_code = ? "
            + "and   card_type != '' ";
      }
      Object[] param = new Object[] {groupCode};
      sqlSelect(strSql, param);
      int qua = Integer.parseInt(colStr("qua"));
      if (qua > 0) {
        errmsg("[卡種:]  不可空白及'有值' 同時設定!");
        return;
      }
    }

    if (checkDecnum(wp.itemStr("accumlate_amt"), 11, 3) != 0) {
      errmsg("C:累積消費金額達 格式超出範圍 : [11][3]");
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

    strSql = " insert into  " + controlTabName + " (" + " group_code, " + " aud_type, "
        + " card_type, " + " card_fee, " + " sup_card_fee, " + " mer_cond, " + " mer_bl_flag, "
        + " mer_ca_flag, " + " mer_it_flag, " + " mer_ao_flag, " + " mer_id_flag, "
        + " mer_ot_flag, " + " major_flag, " + " sub_flag, " + " major_sub, " + " a_merchant_sel, "
        + " a_mcht_group_sel, " + " cnt_cond, " + " cnt_select, " + " month_cnt, "
        + " accumlate_cnt, " + " cnt_bl_flag, " + " cnt_ca_flag, " + " cnt_it_flag, "
        + " cnt_ao_flag, " + " cnt_id_flag, " + " cnt_ot_flag, " + " cnt_major_flag, "
        + " cnt_sub_flag, " + " cnt_major_sub, " + " b_mcc_code_sel, " + " b_merchant_sel, "
        + " b_mcht_group_sel, " + " amt_cond, " + " accumlate_amt, " + " amt_bl_flag, "
        + " amt_ca_flag, " + " amt_it_flag, " + " amt_ao_flag, " + " amt_id_flag, "
        + " amt_ot_flag, " + " amt_major_flag, " + " amt_sub_flag, " + " amt_major_sub, "
        + " c_mcc_code_sel, " + " c_merchant_sel, " + " c_mcht_group_sel, " + " mcode, "
        + " email_nopaper_flag, " 
        + " miner_half_flag, " + " g_cond_flag, " + " g_accumlate_amt, " + " h_cond_flag, " + " h_accumlate_amt, " 
        + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {groupCode, wp.itemStr("aud_type"), cardType, wp.itemNum("card_fee"),
        wp.itemNum("sup_card_fee"), wp.itemStr("mer_cond"), wp.itemStr("mer_bl_flag"),
        wp.itemStr("mer_ca_flag"), wp.itemStr("mer_it_flag"), wp.itemStr("mer_ao_flag"),
        wp.itemStr("mer_id_flag"), wp.itemStr("mer_ot_flag"), wp.itemStr("major_flag"),
        wp.itemStr("sub_flag"), wp.itemStr("major_sub"), wp.itemStr("a_merchant_sel"),
        wp.itemStr("a_mcht_group_sel"), wp.itemStr("cnt_cond"), wp.itemStr("cnt_select"),
        wp.itemNum("month_cnt"), wp.itemNum("accumlate_cnt"), wp.itemStr("cnt_bl_flag"),
        wp.itemStr("cnt_ca_flag"), wp.itemStr("cnt_it_flag"), wp.itemStr("cnt_ao_flag"),
        wp.itemStr("cnt_id_flag"), wp.itemStr("cnt_ot_flag"), wp.itemStr("cnt_major_flag"),
        wp.itemStr("cnt_sub_flag"), wp.itemStr("cnt_major_sub"), wp.itemStr("b_mcc_code_sel"),
        wp.itemStr("b_merchant_sel"), wp.itemStr("b_mcht_group_sel"), wp.itemStr("amt_cond"),
        wp.itemNum("accumlate_amt"), wp.itemStr("amt_bl_flag"), wp.itemStr("amt_ca_flag"),
        wp.itemStr("amt_it_flag"), wp.itemStr("amt_ao_flag"), wp.itemStr("amt_id_flag"),
        wp.itemStr("amt_ot_flag"), wp.itemStr("amt_major_flag"), wp.itemStr("amt_sub_flag"),
        wp.itemStr("amt_major_sub"), wp.itemStr("c_mcc_code_sel"), wp.itemStr("c_merchant_sel"),
        wp.itemStr("c_mcht_group_sel"), wp.itemStr("mcode"), wp.itemStr("email_nopaper_flag"),
        wp.itemEq("miner_half_flag", "Y")?"Y":"N",
        wp.itemEq("g_cond_flag", "Y")?"Y":"N",
        wp.itemNum("g_accumlate_amt"),
        wp.itemEq("h_cond_flag", "Y")?"Y":"N",
        wp.itemNum("h_accumlate_amt"),		
        wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into MKT_BN_DATA_T " + "select * " + "from MKT_BN_DATA "
        + "where table_name  =  'CYC_ANUL_GP' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("group_code") + wp.itemStr("card_type"),};

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

    strSql = "update " + controlTabName + " set " + "card_fee = ?, " + "sup_card_fee = ?, "
        + "mer_cond = ?, " + "mer_bl_flag = ?, " + "mer_ca_flag = ?, " + "mer_it_flag = ?, "
        + "mer_ao_flag = ?, " + "mer_id_flag = ?, " + "mer_ot_flag = ?, " + "major_flag = ?, "
        + "sub_flag = ?, " + "major_sub = ?, " + "a_merchant_sel = ?, " + "a_mcht_group_sel = ?, "
        + "cnt_cond = ?, " + "cnt_select = ?, " + "month_cnt = ?, " + "accumlate_cnt = ?, "
        + "cnt_bl_flag = ?, " + "cnt_ca_flag = ?, " + "cnt_it_flag = ?, " + "cnt_ao_flag = ?, "
        + "cnt_id_flag = ?, " + "cnt_ot_flag = ?, " + "cnt_major_flag = ?, " + "cnt_sub_flag = ?, "
        + "cnt_major_sub = ?, " + "b_mcc_code_sel = ?, " + "b_merchant_sel = ?, "
        + "b_mcht_group_sel = ?, " + "amt_cond = ?, " + "accumlate_amt = ?, " + "amt_bl_flag = ?, "
        + "amt_ca_flag = ?, " + "amt_it_flag = ?, " + "amt_ao_flag = ?, " + "amt_id_flag = ?, "
        + "amt_ot_flag = ?, " + "amt_major_flag = ?, " + "amt_sub_flag = ?, "
        + "amt_major_sub = ?, " + "c_mcc_code_sel = ?, " + "c_merchant_sel = ?, "
        + "c_mcht_group_sel = ?, " + "mcode = ?, " + "email_nopaper_flag = ?, " 
        + " miner_half_flag = ?, " + " g_cond_flag = ?, " + " g_accumlate_amt = ?, " 
        + " h_cond_flag = ?, " + " h_accumlate_amt = ?, " 
        + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemNum("card_fee"), wp.itemNum("sup_card_fee"),
        wp.itemStr("mer_cond"), wp.itemStr("mer_bl_flag"), wp.itemStr("mer_ca_flag"),
        wp.itemStr("mer_it_flag"), wp.itemStr("mer_ao_flag"), wp.itemStr("mer_id_flag"),
        wp.itemStr("mer_ot_flag"), wp.itemStr("major_flag"), wp.itemStr("sub_flag"),
        wp.itemStr("major_sub"), wp.itemStr("a_merchant_sel"), wp.itemStr("a_mcht_group_sel"),
        wp.itemStr("cnt_cond"), wp.itemStr("cnt_select"), wp.itemNum("month_cnt"),
        wp.itemNum("accumlate_cnt"), wp.itemStr("cnt_bl_flag"), wp.itemStr("cnt_ca_flag"),
        wp.itemStr("cnt_it_flag"), wp.itemStr("cnt_ao_flag"), wp.itemStr("cnt_id_flag"),
        wp.itemStr("cnt_ot_flag"), wp.itemStr("cnt_major_flag"), wp.itemStr("cnt_sub_flag"),
        wp.itemStr("cnt_major_sub"), wp.itemStr("b_mcc_code_sel"), wp.itemStr("b_merchant_sel"),
        wp.itemStr("b_mcht_group_sel"), wp.itemStr("amt_cond"), wp.itemNum("accumlate_amt"),
        wp.itemStr("amt_bl_flag"), wp.itemStr("amt_ca_flag"), wp.itemStr("amt_it_flag"),
        wp.itemStr("amt_ao_flag"), wp.itemStr("amt_id_flag"), wp.itemStr("amt_ot_flag"),
        wp.itemStr("amt_major_flag"), wp.itemStr("amt_sub_flag"), wp.itemStr("amt_major_sub"),
        wp.itemStr("c_mcc_code_sel"), wp.itemStr("c_merchant_sel"), wp.itemStr("c_mcht_group_sel"),
        wp.itemStr("mcode"), wp.itemStr("email_nopaper_flag"),
        wp.itemEq("miner_half_flag", "Y")?"Y":"N",
        wp.itemEq("g_cond_flag", "Y")?"Y":"N",
        wp.itemNum("g_accumlate_amt"),
        wp.itemEq("h_cond_flag", "Y")?"Y":"N",
        wp.itemNum("h_accumlate_amt"),
        wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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

    strSql = "delete MKT_BN_DATA_T " + " where table_name  =  'CYC_ANUL_GP' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("group_code") + wp.itemStr("card_type"),};

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
    if (wp.respHtml.equals("mktm6020_aaa1"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6020_bbb2"))
      dataType = "4";
    if (wp.respHtml.equals("mktm6020_ccc2"))
      dataType = "7";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_key, " + "data_type, "
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'CYC_ANUL_GP', " + "?, " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {wp.itemStr("group_code") + wp.itemStr("card_type"), dataType,
        varsStr("data_code"), varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

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
    if (wp.respHtml.equals("mktm6020_aaa1"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6020_bbb2"))
      dataType = "4";
    if (wp.respHtml.equals("mktm6020_ccc2"))
      dataType = "7";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("group_code") + wp.itemStr("card_type"), dataType};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_key = ? " + "and   data_type = ? " + "and   table_name = 'CYC_ANUL_GP' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_key = ? " + "and   data_type = ? "
        + "and   table_name = 'CYC_ANUL_GP'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6020_aaa2"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6020_bbb1"))
      dataType = "3";
    if (wp.respHtml.equals("mktm6020_bbb3"))
      dataType = "5";
    if (wp.respHtml.equals("mktm6020_ccc1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm6020_ccc3"))
      dataType = "8";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_key, " + "data_type, "
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'CYC_ANUL_GP', " + "?, " + "?, " + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {wp.itemStr("group_code") + wp.itemStr("card_type"), dataType,
        varsStr("data_code"), wp.loginUser, wp.loginUser, wp.modPgm()};

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
    if (wp.respHtml.equals("mktm6020_aaa2"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6020_bbb1"))
      dataType = "3";
    if (wp.respHtml.equals("mktm6020_bbb3"))
      dataType = "5";
    if (wp.respHtml.equals("mktm6020_ccc1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm6020_ccc3"))
      dataType = "8";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("group_code") + wp.itemStr("card_type"), dataType};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_key = ? " + "and   data_type = ? " + "and   table_name = 'CYC_ANUL_GP' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_key = ? " + "and   data_type = ? "
        + "and   table_name = 'CYC_ANUL_GP'  ";
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

    Object[] param =
        new Object[] {"CYC_ANUL_GP", wp.itemStr("GROUP_CODE") + wp.itemStr("CARD_TYPE"), "1"};

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
  public int dbInsertI2Bbb2(String tableName, String[] columnCol, String[] columnDat) {
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
  public int dbDeleteD2Bbb2(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param =
        new Object[] {"CYC_ANUL_GP", wp.itemStr("GROUP_CODE") + wp.itemStr("CARD_TYPE"), "4"};

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
  public int dbInsertI2Ccc2(String tableName, String[] columnCol, String[] columnDat) {
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
  public int dbDeleteD2Ccc2(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param =
        new Object[] {"CYC_ANUL_GP", wp.itemStr("GROUP_CODE") + wp.itemStr("CARD_TYPE"), "7"};

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
