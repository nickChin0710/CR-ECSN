/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/12  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6255Func extends busi.FuncProc {
  private String PROGNAME = "首刷禮活動卡片明細覆核作業處理程式108/06/12 V1.00.01";
 // String kk1, kk2;
  String approveTabName = "mkt_fstp_carddtl";
  String controlTabName = "mkt_fstp_carddtl_t";

  public Mktp6255Func(TarokoCommon wr) {
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
    strSql = " insert into  " + approveTabName + " (" + " card_no, " + " active_code, "
        + " active_type, " + " bonus_type, " + " beg_tran_bp, " + " fund_code, " + " beg_tran_amt, "
        + " group_type, " + " prog_code, " + " prog_s_date, " + " prog_e_date, " + " gift_no, "
        + " tran_pt, " + " spec_gift_no, " + " spec_gift_cnt, " + " mod_desc, " + " id_p_seqno, "
        + " id_p_seqno, " + " p_seqno, " + " acct_type, " + " active_code, " + " feedback_date, "
        + " last_execute_date, " + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("card_no"), colStr("active_code"), colStr("active_type"),
        colStr("bonus_type"), colStr("beg_tran_bp"), colStr("fund_code"), colStr("beg_tran_amt"),
        colStr("group_type"), colStr("prog_code"), colStr("prog_s_date"), colStr("prog_e_date"),
        colStr("gift_no"), colStr("tran_pt"), colStr("spec_gift_no"), colStr("spec_gift_cnt"),
        colStr("mod_desc"), colStr("id_p_seqno"), colStr("id_p_seqno"), colStr("p_seqno"),
        colStr("acct_type"), colStr("active_code"), colStr("feedback_date"),
        colStr("last_execute_date"), "Y", wp.loginUser, colStr("crt_date"), colStr("crt_user"),
        wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " card_no, " + " active_code, " + " active_type, " + " bonus_type, "
        + " beg_tran_bp, " + " fund_code, " + " beg_tran_amt, " + " group_type, " + " prog_code, "
        + " prog_s_date, " + " prog_e_date, " + " gift_no, " + " tran_pt, " + " spec_gift_no, "
        + " spec_gift_cnt, " + " mod_desc, " + " id_p_seqno, " + " id_p_seqno, " + " p_seqno, "
        + " acct_type, " + " active_code, " + " feedback_date, " + " last_execute_date, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(" 讀取 " + procTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    colSet("undo_flag", "N");
    rc = dbSelectS4Prefix();
    if (rc != 1)
      return rc;

    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "active_type = ?, " + "bonus_type = ?, "
        + "beg_tran_bp = ?, " + "fund_code = ?, " + "beg_tran_amt = ?, " + "group_type = ?, "
        + "prog_code = ?, " + "prog_s_date = ?, " + "prog_e_date = ?, " + "gift_no = ?, "
        + "tran_pt = ?, " + "spec_gift_no = ?, " + "spec_gift_cnt = ?, " + "mod_desc = ?, "
        + "mod_date = ?, " + "feedback_date = ?, " + "crt_user  = ?, " + "crt_date  = ?, "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, "
        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 "
        + "and   card_no  = ? " + "and   active_code  = ? ";

    Object[] param = new Object[] {colStr("active_type"), colStr("bonus_type"),
        colStr("beg_tran_bp"), colStr("fund_code"), colStr("beg_tran_amt"), colStr("group_type"),
        colStr("prog_code"), colStr("prog_s_date"), colStr("prog_e_date"), colStr("gift_no"),
        colStr("tran_pt"), colStr("spec_gift_no"), colStr("spec_gift_cnt"), colStr("mod_desc"),
        wp.sysDate, colStr("feedback_date"), colStr("crt_user"), colStr("crt_date"), wp.loginUser,
        apr_flag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"), colStr("card_no"),
        colStr("active_code")};

    sqlExec(strSql, param);
    if (colStr("undo_flag").equals("N")) {
      rc = dbSelectS4Postfix();
      if (rc != 1)
        return rc;
    }


    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and card_no = ? "
        + "and active_code = ? ";

    Object[] param = new Object[] {colStr("card_no"), colStr("active_code")};

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
  public int dbDelete() {
    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }

  int dbSelectS4Prefix() {
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    String procTabName = approveTabName;
    String modMemo = "";
    strSql = " select " + " active_type as bef_active_type," + " bonus_type as bef_bonus_type, "
        + " beg_tran_bp as bef_beg_tran_bp, " + " fund_code as bef_fund_code, "
        + " beg_tran_amt as bef_beg_tran_amt, " + " tran_pt as bef_tran_pt, "
        + " group_type as  bef_group_type, " + " prog_code as  bef_prog_code, "
        + " prog_s_date as bef_prog_s_date, " + " prog_e_date as bef_prog_e_date, "
        + " gift_no as bef_gift_no, " + " spec_gift_no as bef_spec_gift_no, "
        + " spec_gift_cnt as bef_spec_gift_cnt " + " from " + procTabName + " where card_no  = ? "
        + " and   active_code  = ? ";


    Object[] param = new Object[] {colStr("card_no"), colStr("active_code")};

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (colStr("active_type").length() == 0) {
      colSet("feedback_date", "");
      colSet("execute_date", colStr("last_execute_date"));
      colSet("proc_date", "");
      colSet("proc_flag", "N");
    } else if (colStr("feedback_date").length() == 0) {
      colSet("feedback_date", comr.getBusinDate());
      colSet("execute_date", comr.getBusinDate());
      colSet("proc_date", wp.sysDate);
      colSet("proc_flag", "Y");
      colSet("undo_flag", "Y");
    }
    if ((colStr("bef_active_type").equals("1")) || (colStr("bef_active_type").equals("2"))) {
      colSet("bef_tran_seqno", comr.getSeqno("MKT_MODSEQ"));
    } else if (colStr("bef_active_type").equals("3")) {
      if (colStr("bef_group_type").equals("1")) {
        select_crd_idno();
        colSet("data_type", "1");
        colSet("data_id", colStr("id_no"));
      } else {
        colSet("data_type", "3");
        colSet("data_id", colStr("card_no"));
      }

      if (selectIbnProgDtl() != 1) {
        if (colNum("rem_gift_cnt") < colNum("bef_tran_pt")) {
          int lengthInt = 47;
          if (colStr("mod_Desc").length() < 47)
            lengthInt = colStr("mod_Desc").length();
          colSet("mod_desc", "＊[點數已被兌換不許更改]" + colStr("mod_Desc").substring(0, lengthInt));
          colSet("active_type", colStr("bef_active_type"));
          colSet("bonus_type", colStr("bef_bonus_type"));
          colSet("beg_tran_bp", colStr("bef_beg_tran_bp"));
          colSet("fund_code", colStr("bef_fund_code"));
          colSet("beg_tran_amt", colStr("bef_beg_tran_amt"));
          colSet("group_type", colStr("bef_group_type"));
          colSet("prog_code", colStr("bef_prog_code"));
          colSet("prog_s_date", colStr("bef_prog_s_date"));
          colSet("prog_e_date", colStr("bef_prog_e_date"));
          colSet("gift_no", colStr("bef_gift_no"));
          colSet("tran_pt", colStr("bef_tran_pt"));
          colSet("spec_gift_no", colStr("bef_spec_gift_no"));
          colSet("spec_gift_cnt", colStr("bef_spec_gift_cnt"));
          colSet("undo_flag", "Y");
          return (1);
        }
      }
    }

    if (colStr("active_type").length() == 0) {
    } else if (colStr("active_type").equals("1")) {
      selectMktFstpParm();
      colSet("tran_code", "1");
      // col_set("beg_tran_bp" , col_ss("beg_tran_bp"));
      colSet("end_tran_bp", colStr("beg_tran_bp"));
      colSet("res_e_date", "");
      colSet("res_tran_bp", 0);
      colSet("tax_flag", colStr("tax_flag"));
      colSet("effect_e_date", "");
      if (colNum("effect_months") > 0)
        colSet("effect_e_date", comm.nextMonthDate(wp.sysDate, (int) colNum("effect_months")));
      colSet("bdtl.mod_desc", "首x收刷禮變更新項目新增");
      colSet("mod_reason", "");
      modMemo = "原項目:";
      if (colStr("bef_active_type").equals("1"))
        modMemo = modMemo + "紅利(" + colStr("bef_beg_tran_bp") + "點";
      else if (colStr("bef_active_type").equals("2"))
        modMemo =
            modMemo + "基金(" + colStr("bef_fund_code") + ")" + colStr("bef_beg_tran_amt") + "元";
      else if (colStr("bef_active_type").equals("3"))
        modMemo = modMemo + "豐富點(" + colStr("bef_gift_no") + ")" + colStr("bef_tran_pt") + "點";
      else if (colStr("bef_active_type").equals("4"))
        modMemo = modMemo + "特殊商品(" + colStr("bef_spec_gift_no") + ")" + colStr("bef_spec_gift_cnt")
            + "件";
      else
        modMemo = "人工線上新增";
      colSet("mod_memo", modMemo);
      colSet("tran_date", wp.sysDate);
      colSet("tran_time", wp.sysTime);
      colSet("crt_date", wp.sysDate);
      colSet("crt_user", wp.loginUser);
      colSet("tran_seqno", comr.getSeqno("MKT_MODSEQ"));
      insertMktBonusDtl();
      busi.ecs.MktBonus comb = new busi.ecs.MktBonus();
      comb.setConn(wp);
      comb.bonusFunc(colStr("tran_seqno"));
    } else if (colStr("active_type").equals("2")) {
      selectVmktFundName();
      colSet("beg_tran_amt", colStr("beg_tran_amt"));
      colSet("end_tran_amt", colStr("beg_tran_amt"));
      colSet("tran_code", "1");
      colSet("bdtl.mod_desc", "首刷禮變更新項目新增");
      colSet("mod_reason", "");
      modMemo = "原項目:";
      if (colStr("bef_active_type").equals("1"))
        modMemo = modMemo + "紅利(" + colStr("bef_beg_tran_bp") + "點";
      else if (colStr("bef_active_type").equals("2"))
        modMemo =
            modMemo + "基金(" + colStr("bef_fund_code") + ")" + colStr("bef_beg_tran_amt") + "元";
      else if (colStr("bef_active_type").equals("3"))
        modMemo = modMemo + "豐富點(" + colStr("bef_gift_no") + ")" + colStr("bef_tran_pt") + "點";
      else if (colStr("bef_active_type").equals("4"))
        modMemo = modMemo + "特殊商品(" + colStr("bef_spec_gift_no") + ")" + colStr("bef_spec_gift_cnt")
            + "件";
      else
        modMemo = "人工線上新增";

      colSet("mod_memo", modMemo);
      colSet("effect_e_date", "");
      if (colNum("effect_months") > 0)
        colSet("effect_e_date", comm.nextMonthDate(wp.sysDate, (int) colNum("effect_months")));

      colSet("tran_seqno", comr.getSeqno("MKT_MODSEQ"));
      insertMktCashbackDtl();
      busi.ecs.MktCashback comc = new busi.ecs.MktCashback();
      comc.setConn(wp);
      comc.cashbackFunc(colStr("tran_seqno"));
      colSet("fund_amt", colStr("beg_tran_amt"));
      insert_cyc_fund_dtl(0);
    } else if (colStr("active_type").equals("3")) {
      selectMktFstpParm();
      if (colStr("group_type").equals("1")) {
        select_crd_idno();
        colSet("data_type", "1");
        colSet("data_id", colStr("id_no"));
      } else {
        colSet("data_type", "3");
        colSet("data_id", colStr("card_no"));
      }
      colSet("tran_seqno", comr.getSeqno("COL_MODSEQ"));
      if (selectIbnProgDtl1() != 1) {
        colSet("data_seqno", colStr("tran_seqno"));
        insertIbnProgDtl();
        colSet("beg_gift_cnt", "0");
        colSet("aft_gift_cnt", String.format("%.0f", colNum("tran_pt")));
      } else {
        colSet("beg_gift_cnt", String.format("%.0f", colNum("rem_gift_cnt")));
        colSet("aft_gift_cnt", String.format("%.0f", colNum("tran_pt") + colNum("rem_gift_cnt")));
        updateIbnProgDtl1();
      }
      colSet("tot_gift_cnt", String.format("%.0f", colNum("tran_pt")));
      insertIbnProgTxn();
    } else if (colStr("active_type").equals("4")) {
    }
    return 1;
  }

  // ************************************************************************
  int dbSelectS4Postfix() {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    String modMemo = "";
    if (colStr("bef_active_type").equals("1")) {
      selectMktFstpParm();
      colSet("bonus_type", colStr("bef_bonus_type"));
      colSet("tran_code", "1");
      colSet("beg_tran_bp", String.format("%d", (int) colNum("bef_beg_tran_bp") * -1));
      colSet("end_tran_bp", String.format("%d", (int) colNum("bef_beg_tran_bp") * -1));
      colSet("res_e_date", "");
      colSet("res_tran_bp", 0);
      colSet("effect_e_date", "");
      colSet("tax_flag", "");
      colSet("bdtl.mod_desc", "首刷禮變更舊項目移除");
      colSet("mod_reason", "");
      modMemo = "新項目:";
      if (colStr("active_type").equals("1"))
        modMemo = modMemo + "紅利(" + colStr("beg_tran_bp") + "點";
      else if (colStr("active_type").equals("2"))
        modMemo = modMemo + "基金(" + colStr("fund_code") + ")" + colStr("beg_tran_amt") + "元";
      else if (colStr("active_type").equals("3"))
        modMemo = modMemo + "豐富點(" + colStr("gift_no") + ")" + colStr("tran_pt") + "點";
      else if (colStr("active_type").equals("4"))
        modMemo = modMemo + "特殊商品(" + colStr("spec_gift_no") + ")" + colStr("spec_gift_cnt") + "件";
      else
        modMemo = "人工線上移除";

      colSet("mod_memo", modMemo);

      colSet("tran_date", wp.sysDate);
      colSet("tran_time", wp.sysTime);
      colSet("crt_date", wp.sysDate);
      colSet("crt_user", wp.loginUser);
      colSet("tran_seqno", colStr("bef_tran_seqno"));
      insertMktBonusDtl();
      busi.ecs.MktBonus comb = new busi.ecs.MktBonus();
      comb.setConn(wp);
      comb.bonusFunc(colStr("tran_seqno"));
    } else if (colStr("bef_active_type").equals("2")) {
      colSet("fund_code", colStr("bef_fund_code"));
      colSet("tran_code", "1");
      colSet("beg_tran_amt", String.format("%d", (int) colNum("bef_beg_tran_amt") * -1));
      colSet("end_tran_amt", String.format("%d", (int) colNum("bef_beg_tran_amt") * -1));
      colSet("effect_e_date", "");
      colSet("bdtl.mod_desc", "首刷禮變更舊項目移除");
      colSet("mod_reason", "");
      modMemo = "新項目:";
      if (colStr("active_type").equals("1"))
        modMemo = modMemo + "紅利(" + colStr("beg_tran_bp") + "點";
      else if (colStr("active_type").equals("2"))
        modMemo = modMemo + "基金(" + colStr("fund_code") + ")" + colStr("beg_tran_amt") + "元";
      else if (colStr("active_type").equals("3"))
        modMemo = modMemo + "豐富點(" + colStr("gift_no") + ")" + colStr("tran_pt") + "點";
      else if (colStr("active_type").equals("4"))
        modMemo = modMemo + "特殊商品(" + colStr("spec_gift_no") + ")" + colStr("spec_gift_cnt") + "件";
      else
        modMemo = "人工線上移除";

      colSet("mod_memo", modMemo);

      selectVmktFundName();
      colSet("tran_seqno", colStr("bef_tran_seqno"));
      insertMktCashbackDtl();
      busi.ecs.MktCashback comc = new busi.ecs.MktCashback();
      comc.setConn(wp);
      comc.cashbackFunc(colStr("tran_seqno"));
      colSet("fund_amt", colStr("bef_beg_tran_amt"));
      insert_cyc_fund_dtl(1);
    } else if (colStr("bef_active_type").equals("3")) {
      selectMktFstpParm();
      if (colStr("bef_group_type").equals("1")) {
        select_crd_idno();
        colSet("data_type", "1");
        colSet("data_id", colStr("id_no"));
      } else {
        colSet("data_type", "3");
        colSet("data_id", colStr("card_no"));
      }
      selectIbnProgDtl();
      colSet("beg_gift_cnt", String.format("%.0f", colNum("rem_gift_cnt")));
      colSet("aft_gift_cnt", String.format("%.0f", colNum("rem_gift_cnt") - colNum("bef_tran_pt")));
      colSet("tot_gift_cnt", String.format("%.0f", colNum("bef_tran_pt") * -1));
      update_ibn_prog_dtl();
      insertIbnProgTxn();
    } else if (colStr("bef_active_type").equals("4")) {
    }

    return 1;
  }

  // ************************************************************************
  int insertMktBonusDtl() {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    strSql = " insert into mkt_bonus_dtl(" + " acct_type, " + " bonus_type, " + " active_code, "
        + " active_name, " + " tran_code, " + " beg_tran_bp, " + " end_tran_bp, " + " res_e_date, "
        + " res_tran_bp, " + " tax_flag, " + " effect_e_date, " + " mod_desc, " + " mod_reason, "
        + " mod_memo, " + " tran_date, " + " tran_time, " + " p_seqno, " + " id_p_seqno, "
        + " tran_pgm, " + " tran_seqno, " + " proc_month, " + " acct_date, " + " apr_flag, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " mod_time,mod_user,mod_pgm,mod_seqno " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," // last: tran_time
        + "?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

    Object[] param = new Object[] {colStr("acct_type"), colStr("bonus_type"), colStr("active_code"),
        colStr("active_name"), colStr("tran_code"), colNum("beg_tran_bp"), colNum("end_tran_bp"),
        colStr("res_e_date"), colNum("res_tran_bp"), colStr("tax_flag"), colStr("effect_e_date"),
        colStr("bdtl.mod_desc"), colStr("mod_reason"), colStr("mod_memo"), colStr("tran_date"),
        colStr("tran_time"), colStr("p_seqno"), colStr("id_p_seqno"), wp.modPgm(),
        colStr("tran_seqno"), comr.getBusinDate().substring(0, 6), comr.getBusinDate(), "Y",
        wp.loginUser, colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
        wp.modPgm(), colStr("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(sqlErrtext);

    return (1);
  }

  // ************************************************************************
  public int insert_cyc_fund_dtl(int cd_type) {

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    if (cd_type == 0)
      colSet("cd_kind", "A-36");
    else
      colSet("cd_kind", "A393");

    strSql = " insert into cyc_fund_dtl (" + " business_date, " + " curr_code, " + " create_date, "
        + " create_time, " + " id_p_seqno, " + " p_seqno, " + " acct_type, " + " fund_code, "
        + " tran_code, " + " vouch_type, " + " cd_kind, " + " memo1_type, " + " fund_amt, "
        + " other_amt, " + " proc_flag, " + " proc_date, " + " execute_date, " + " fund_cnt, "
        + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss')," + "?,?,?,?,?,?,?,?,"
        + "?,?,?,?,?,?," + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {comr.getBusinDate(), "901", colStr("id_p_seqno"),
        colStr("p_seqno"), colStr("acct_type"), colStr("fund_code"), colStr("tran_code"), "3",
        colStr("cd_kind"), "1", colNum("fund_amt"), 0, "N", "", comr.getBusinDate(), 1,
        wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return (0);
    }

    return (1);
  }

  // ************************************************************************
  int insertMktCashbackDtl() {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    strSql = " insert into mkt_cashback_dtl (" + " fund_code, " + " fund_name, " + " acct_type, "
        + " tran_code, " + " beg_tran_amt, " + " end_tran_amt, " + " effect_e_date, "
        + " mod_desc, " + " mod_reason, " + " mod_memo, " + " tran_date, " + " tran_time, "
        + " p_seqno, " + " id_p_seqno, " + " tran_pgm, " + " tran_seqno, " + " acct_date, "
        + " acct_month, " + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?,?,?," // cpr_user rt_Date crt_user
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?) ";

    Object[] param = new Object[] {colStr("fund_code"), colStr("fund_name"), colStr("acct_type"),
        colStr("tran_code"), colNum("beg_tran_amt"), colNum("beg_tran_amt"),
        colStr("effect_e_date"), colStr("bdtl.mod_desc"), colStr("mod_reason"), colStr("mod_memo"),
        colStr("p_seqno"), colStr("id_p_seqno"), wp.modPgm(), colStr("tran_seqno"),
        comr.getBusinDate(), comr.getBusinDate().substring(0, 6), "Y", wp.loginUser,
        colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
        colStr("mod_seqno"), wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return (0);
    }

    return (1);
  }

  // ************************************************************************
  int selectVmktFundName() {
    strSql = " select " + " fund_name," + " table_name " + " from vmkt_fund_name "
        + " where fund_code = ?  ";

    Object[] param = new Object[] {colStr("fund_code")};

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0)
      colSet("fund_name", "");

    return (1);
  }

  // ************************************************************************
  int getEffectMonths() {
    strSql =
        " select " + " effect_months " + " from " + colStr("table_name") + " where fund_code = ?  ";

    Object[] param = new Object[] {colStr("fund_code")};

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0)
      colSet("effect_months", "36");

    return (1);
  }

  // ************************************************************************
  int select_mkt_gift() {
    strSql = " select " + " gift_name " + " from mkt_spec_gift " + " where gift_no = ?  ";

    Object[] param = new Object[] {colStr("gift_no")};

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0)
      colSet("gift_name", "noname");

    return (1);
  }

  // ************************************************************************
  int selectCycBpid() {
    strSql = " select " + " effect_months " + " from cyc_bpid " + " where years      = ?  "
        + " and   acct_type  = ?  " + " and   bonus_type = ?  " + " and   item_code  = '1'  ";

    Object[] param = new Object[] {colStr("tran_date").substring(0, 4), colStr("acct_type"),
        colStr("bonus_type")};

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0)
      colSet("effect_months", "36");

    return (1);
  }

  // ************************************************************************
  int selectMktFstpParm() {
    strSql = " select " + " active_name, " + " tax_flag," + " effect_months "
        + " from  mkt_fstp_parm " + " where active_code = ?  ";

    Object[] param = new Object[] {colStr("active_code")};

    sqlSelect(strSql, param);

    return (1);
  }

  // ************************************************************************
  int selectIbnProgDtl() {
    strSql = " select " + " tot_gift_cnt, " + " rem_gift_cnt, " + " data_seqno, "
        + " rowid as dtl_rowid " + " from   ibn_prog_dtl " + " where  data_type   = ?  "
        + " and    data_id     = ? " + " and    group_type  = ? " + " and    prog_code   = ? "
        + " and    prog_s_date = ? " + " and    gift_no     = ? ";

    Object[] param = new Object[] {colStr("data_type"), colStr("data_id"), colStr("bef_group_type"),
        colStr("bef_prog_code"), colStr("bef_prog_s_date"), colStr("bef_gift_no")};

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0)
      return (0);
    return (1);
  }

  // ************************************************************************
  int selectIbnProgDtl1() {
    strSql = " select " + " tot_gift_cnt, " + " rem_gift_cnt, " + " data_seqno, "
        + " rowid as dtl_rowid " + " from   ibn_prog_dtl " + " where  data_type   = ?  "
        + " and    data_id     = ? " + " and    group_type  = ? " + " and    prog_code   = ? "
        + " and    prog_s_date = ? " + " and    gift_no     = ? ";

    Object[] param = new Object[] {colStr("data_type"), colStr("data_id"), colStr("group_type"),
        colStr("prog_code"), colStr("prog_s_date"), colStr("gift_no")};

    sqlSelect(strSql, param);

    if (sqlRowNum <= 0)
      return (0);
    return (1);
  }

  // ************************************************************************
  int select_crd_idno() {
    strSql = " select " + " id_no " + " from   crd_idno " + " where  id_p_seqno = ? ";

    Object[] param = new Object[] {colStr("id_p_seqno"),};

    sqlSelect(strSql, param);

    return (1);
  }

  // ************************************************************************
  int insertIbnProgDtl() {
    strSql = " insert into ibn_prog_dtl (" + " group_type," + " data_type," + " data_id,"
        + " data_seqno," + " id_p_seqno," + " id_no," + " acct_type, " + " p_seqno," + " card_no,"
        + " prog_code, " + " gift_no, " + " prog_s_date, " + " prog_e_date, " + " vd_flag,"
        + " tot_gift_cnt, " + " rem_gift_cnt, " + " apr_flag, " + " apr_date, " + " apr_user, "
        + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user, " + " mod_seqno, "
        + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?," + "?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "sysdate,"
        + "?,0," + " ?) ";

    Object[] param = new Object[] {colStr("group_type"), colStr("data_type"), colStr("data_id"),
        colStr("data_seqno"), colStr("id_p_seqno"), colStr("id_no"), colStr("acct_type"),
        colStr("p_seqno"), colStr("card_no"), colStr("prog_code"), colStr("gift_no"),
        colStr("prog_s_date"), colStr("prog_e_date"), "N", colNum("tran_pt"), colNum("tran_pt"),
        "Y", wp.loginUser, wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return (0);
    }

    return (1);
  }

  // ************************************************************************
  int update_ibn_prog_dtl() {
    strSql = "update  ibn_prog_dtl " + " set    tot_gift_cnt = tot_gift_cnt - ?,  "
        + "        rem_gift_cnt = rem_gift_cnt - ?,  " + "        mod_pgm      = ?,  "
        + "        mod_time     = sysdate  " + " where  data_type   = ?  "
        + " and    data_id     = ? " + " and    group_type  = ? " + " and    prog_code   = ? "
        + " and    prog_s_date = ? " + " and    gift_no     = ? ";;

    Object[] param = new Object[] {colNum("bef_tran_pt"), colNum("bef_tran_pt"), wp.modPgm(),
        colStr("data_type"), colStr("data_id"), colStr("bef_group_type"), colStr("bef_prog_code"),
        colStr("bef_prog_s_date"), colStr("bef_gift_no")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("更新 ibn_prog_dtl 錯誤");

    return rc;
  }

  // ************************************************************************
  int updateIbnProgDtl1() {
    strSql = "update  ibn_prog_dtl " + " set    tot_gift_cnt = tot_gift_cnt + ?,  "
        + "        rem_gift_cnt = rem_gift_cnt + ?,  " + "        mod_pgm      = ?,  "
        + "        mod_time     = sysdate  " + " where  data_type   = ?  "
        + " and    data_id     = ? " + " and    group_type  = ? " + " and    prog_code   = ? "
        + " and    prog_s_date = ? " + " and    gift_no     = ? ";;

    Object[] param = new Object[] {colNum("tran_pt"), colNum("tran_pt"), wp.modPgm(),
        colStr("data_type"), colStr("data_id"), colStr("group_type"), colStr("prog_code"),
        colStr("prog_s_date"), colStr("gift_no")};


    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("更新 ibn_prog_dtl_1 錯誤");

    return rc;
  }

  // ************************************************************************
  int insertIbnProgTxn() {
    strSql = " insert into ibn_prog_txn (" + " txn_date," + " txn_time," + " data_seqno,"
        + " txn_seqno," + " txn_type," + " tot_gift_cnt," + " beg_gift_cnt," + " aft_gift_cnt,"
        + " mod_pgm, " + " mod_time " + " ) values (" + "?,?,?,?,?,?,?,?,?," + "sysdate)";

    Object[] param =
        new Object[] {wp.sysDate, wp.sysTime, colStr("data_seqno"), colStr("active_code"), "F",
            colNum("tot_gift_cnt"), colNum("beg_gift_cnt"), colNum("aft_gift_cnt"), wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return (0);
    }

    return (1);
  }
  // ************************************************************************


  // ************************************************************************

}  // End of class
