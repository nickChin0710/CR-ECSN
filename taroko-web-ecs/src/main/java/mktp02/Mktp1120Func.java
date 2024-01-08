/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 2019/11/18 V1.00.01  Ru Chen        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1120Func extends busi.FuncProc {
  private String PROGNAME = "新貴通/龍騰卡貴賓室申請參數覆核2019/11/18 V1.00.01";
  //String kk1, kk2;
  String approveTabName = "mkt_ppcard_apply";
  String controlTabName = "mkt_ppcard_apply_t";

  public Mktp1120Func(TarokoCommon wr) {
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
  public void dataCheck() {}

  // ************************************************************************
  @Override
  public int dataProc() {
    return rc;
  }

  // ************************************************************************
  public int dbInsertA4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = " insert into  " + approveTabName + " (" + " card_type, " + " group_code, "
        + " bin_type, " + " proj_desc, " + " major_flag, " + " card_purch_code, " + " first_cond, "
        + " fir_purch_mm, " + " fir_item_ename_bl, " + " fir_item_ename_ca, "
        + " fir_item_ename_it, " + " fir_item_ename_id, " + " fir_item_ename_ao, "
        + " fir_item_ename_ot, " + " fir_it_type, " + " fir_min_amt, " + " fir_amt_cond, "
        + " fir_tot_amt, " + " fir_cnt_cond, " + " fir_tot_cnt, " + " last_amt_cond, "
        + " last_tot_amt, " + " nofir_cond, " + " purch_mm, " + " item_ename_bl, "
        + " item_ename_ca, " + " item_ename_it, " + " item_ename_id, " + " item_ename_ao, "
        + " item_ename_ot, " + " it_type, " + " min_amt, " + " amt_cond, " + " tot_amt, "
        + " cnt_cond, " + " tot_cnt, " + " vip_kind, " + " vip_group_code, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, " + " mod_pgm, " + " mod_seqno "
        + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?," + "?," + "?) ";

    Object[] param = new Object[] {colStr("card_type"), colStr("group_code"), colStr("bin_type"),
        colStr("proj_desc"), colStr("major_flag"), colStr("card_purch_code"), colStr("first_cond"),
        colNum("fir_purch_mm"), colStr("fir_item_ename_bl"), colStr("fir_item_ename_ca"),
        colStr("fir_item_ename_it"), colStr("fir_item_ename_id"), colStr("fir_item_ename_ao"),
        colStr("fir_item_ename_ot"), colStr("fir_it_type"), colNum("fir_min_amt"),
        colStr("fir_amt_cond"), colNum("fir_tot_amt"), colStr("fir_cnt_cond"),
        colNum("fir_tot_cnt"), colStr("last_amt_cond"), colNum("last_tot_amt"),
        colStr("nofir_cond"), colNum("purch_mm"), colStr("item_ename_bl"), colStr("item_ename_ca"),
        colStr("item_ename_it"), colStr("item_ename_id"), colStr("item_ename_ao"),
        colStr("item_ename_ot"), colStr("it_type"), colNum("min_amt"), colStr("amt_cond"),
        colNum("tot_amt"), colStr("cnt_cond"), colNum("tot_cnt"), colStr("vip_kind"),
        colStr("vip_group_code"), wp.loginUser, colStr("crt_date"), colStr("crt_user"),
        colStr("mod_pgm"), colNum("mod_seqno"),};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " card_type, " + " group_code, " + " vip_kind, " + " vip_group_code, "
        + " bin_type, " + " proj_desc, " + " major_flag, " + " card_purch_code, " + " first_cond, "
        + " fir_purch_mm, " + " fir_item_ename_bl, " + " fir_item_ename_ca, "
        + " fir_item_ename_it, " + " fir_item_ename_id, " + " fir_item_ename_ao, "
        + " fir_item_ename_ot, " + " fir_it_type, " + " fir_min_amt, " + " fir_amt_cond, "
        + " fir_tot_amt, " + " fir_cnt_cond, " + " fir_tot_cnt, " + " last_amt_cond, "
        + " last_tot_amt, " + " nofir_cond, " + " purch_mm, " + " item_ename_bl, "
        + " item_ename_ca, " + " item_ename_it, " + " item_ename_id, " + " item_ename_ao, "
        + " item_ename_ot, " + " it_type, " + " min_amt, " + " amt_cond, " + " tot_amt, "
        + " cnt_cond, " + " tot_cnt, " + " aud_type, " + " chg_remark, " + " crt_date, "
        + " crt_user, " + " apr_date, " + " apr_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String aprFlag = "Y";
    strSql = "update " + approveTabName + " set " + "card_type = ?, " + "group_code = ?, "
        + "bin_type = ?, " + "proj_desc = ?, " + "major_flag = ?, " + "card_purch_code = ?, "
        + "first_cond = ?, " + "fir_purch_mm = ?, " + "fir_item_ename_bl = ?, "
        + "fir_item_ename_ca  = ?, " + "fir_item_ename_it  = ?, " + "fir_item_ename_id  = ?, "
        + "fir_item_ename_ao  = ?, " + "fir_item_ename_ot  = ?, " + "fir_it_type  = ?, "
        + "fir_min_amt  = ?, " + "fir_amt_cond  = ?, " + "fir_tot_amt  = ?, "
        + "fir_cnt_cond  = ?, " + "fir_tot_cnt  = ?, " + "last_amt_cond  = ?, "
        + "last_tot_amt  = ?, " + "nofir_cond  = ?, " + "purch_mm  = ?, " + "item_ename_bl  = ?, "
        + "item_ename_ca  = ?, " + "item_ename_it  = ?, " + "item_ename_id  = ?, "
        + "item_ename_ao  = ?, " + "item_ename_ot  = ?, " + "it_type  = ?, " + "min_amt  = ?, "
        + "amt_cond  = ?, " + "tot_amt  = ?, " + "cnt_cond  = ?, " + "tot_cnt  = ?, "
        + "vip_kind  = ?, " + "vip_group_code  = ?, " + "apr_user = ?, "
        + "apr_date = to_char(sysdate,'yyyymmdd'), " + "mod_user = ?, "
        + "mod_time = timestamp_format(?,'yyyymmdd')," + "mod_pgm = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1 = 1 " + "and group_code  = ? "
        + "and card_type  = ? " + "and bin_type = ? " + "and vip_kind = ? "
    // + "and vip_group_code = ? "
    ;

    Object[] param = new Object[] {colStr("card_type"), colStr("group_code"), colStr("bin_type"),
        colStr("proj_desc"), colStr("major_flag"), colStr("card_purch_code"), colStr("first_cond"),
        colStr("fir_purch_mm"), colStr("fir_item_ename_bl"), colStr("fir_item_ename_ca"),
        colStr("fir_item_ename_it"), colStr("fir_item_ename_id"), colStr("fir_item_ename_ao"),
        colStr("fir_item_ename_ot"), colStr("fir_it_type"), colStr("fir_min_amt"),
        colStr("fir_amt_cond"), colStr("fir_tot_amt"), colStr("fir_cnt_cond"),
        colStr("fir_tot_cnt"), colStr("last_amt_cond"), colStr("last_tot_amt"),
        colStr("nofir_cond"), colStr("purch_mm"), colStr("item_ename_bl"), colStr("item_ename_ca"),
        colStr("item_ename_it"), colStr("item_ename_id"), colStr("item_ename_ao"),
        colStr("item_ename_ot"), colStr("it_type"), colStr("min_amt"), colStr("amt_cond"),
        colStr("tot_amt"), colStr("cnt_cond"), colStr("tot_cnt"), colStr("vip_kind"),
        colStr("vip_group_code"), wp.loginUser, colStr("crt_user"), colStr("crt_date"),
        colStr("mod_pgm"), colStr("group_code"), colStr("card_type"), colStr("bin_type"),
        colStr("vip_kind"),
        // col_ss("vip_group_code")
    };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and group_code = ? "
        + "and card_type = ? " + "and bin_type = ? " + "and vip_kind = ? "
    // + "and vip_group_code = ? "
    ;

    Object[] param = new Object[] {colStr("group_code"), colStr("card_type"), colStr("bin_type"),
        colStr("vip_kind"),
        // col_ss("vip_group_code")
    };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + approveTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4T() {
    strSql = "delete " + controlTabName + " " + "where 1 = 1 " + "and group_code = ? "
        + "and card_type = ? " + "and bin_type = ? " + "and vip_kind = ? "
        + "and vip_group_code = ? ";

    Object[] param = new Object[] {colStr("group_code"), colStr("card_type"), colStr("bin_type"),
        colStr("vip_kind"), colStr("vip_group_code")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
