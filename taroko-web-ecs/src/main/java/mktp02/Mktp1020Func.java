/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard
* 110-07-08  V1.00.03   MaChao       新增匯入檔案的邏輯處理                                          *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1020Func extends busi.FuncProc {
  private String PROGNAME = "市區停車手KEY資料審核作業處理程式110/07/08 V1.00.01";
// String kk1;
  String approveTabName = "mkt_dodo_resp";
  String controlTabName = "mkt_dodo_resp_t";

  public Mktp1020Func(TarokoCommon wr) {
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
    String errCode = colStr("err_code");
    if (errCode.equals("99") ||errCode.isEmpty()||colStr("use_point").equals("0")) {   
    	errCode="02";
    }
    strSql = " insert into  " + approveTabName + " (" + " tran_seqno, " + " acct_type, "
        + " card_no, " + " park_vendor, " + " park_hr, " + " free_hr, " + " use_bonus_hr, "
        + " use_point, " + " manual_reason, " + " verify_flag, " + " verify_remark, " + " p_seqno, "
        + " id_p_seqno, " + " pass_type, " + " tran_date, " + " tran_time, " + " proc_date, "
        + " proc_flag, " + " err_code, " + " park_date_s, " + " park_time_s, " + " apr_flag, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm ," 
        + " park_date_e," + " park_time_e, " + " station_id, " + " file_name, " + " data_date, " + " action_cd " 
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,"
        + "?,?,?,?,?,?,?,?,?,?," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?," 
        + "?," + "?," + "?," + "?," + "?," + "?," + "?," + "?) ";

    Object[] param = new Object[] {colStr("tran_seqno"), colStr("acct_type"), colStr("card_no"),
        colStr("park_vendor"), colStr("park_hr"), colStr("free_hr"), colStr("use_bonus_hr"),
        colStr("use_point"), colStr("manual_reason"), "1",
        colStr("verify_remark"), colStr("p_seqno"), colStr("id_p_seqno"), colStr("pass_type"),
        colStr("tran_date"), colStr("tran_time"), colStr("proc_date"), "Y",
        errCode, colStr("park_date_s"), colStr("park_time_s"), "Y", wp.loginUser,
        colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser,
        colStr("mod_seqno"), wp.modPgm(),
        colStr("park_date_e"),colStr("park_time_e"),colStr("station_id"),colStr("file_name"),colStr("data_date"),colStr("action_cd")};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " tran_seqno, " + " acct_type, " + " card_no, " + " park_vendor, "
        + " park_hr, " + " free_hr, " + " use_bonus_hr, " + " use_point, " + " manual_reason, "
        + " verify_flag, " + " verify_remark, " + " p_seqno, " + " id_p_seqno, " + " pass_type, "
        + " tran_date, " + " tran_time, " + " proc_date, " + " proc_flag, " + " err_code, "
        + " park_date_s, " + " park_time_s, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno ,"
        + " park_date_e," + " park_time_e, " + " station_id, " + " file_name, " + " data_date, " + " action_cd " + " from "
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

    busi.ecs.MktBonus comb = new busi.ecs.MktBonus();
    comb.setConn(wp);
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

    colSet("proc_date", wp.sysDate);
    colSet("proc_flag", "Y");
    if (colStr("verify_flag").equals("1")) {
      colSet("bonus_type", "BONU");
      double endTranBp = comb.bonusSum(colStr("p_seqno"), colStr("bonus_type"));
      if (endTranBp < colNum("use_point")) {
        selectCrdCard();
        selectBilDodoParm();
        double chargeAmt = colNum("parm.charge_amt") * colNum("use_bonus_hr");
        colSet("err_code", "20");
        colSet("act_use_point", "0");
        colSet("act_charge_amt", String.format("%.0f", chargeAmt));
        if (chargeAmt > 0)
          insertBilSysexp();
      } else {
        colSet("act_charge_amt", "0");
        colSet("err_codee", "00");
        if (colNum("use_point") > 0) {
          colSet("act_use_point", colStr("use_point"));
          colSet("beg_tran_bp", String.format("%.0f", colNum("use_point") * -1));
          colSet("end_tran_bp", String.format("%.0f", colNum("use_point") * -1));
          selectMktParkParm();

          insertMktBonusDtl(0, colStr("tran_seqno"));

          comb.bonusFunc(colStr("tran_seqno"));
        } else {
          colSet("act_use_point", "0");
        }
      }
    } else if (colStr("verify_flag").equals("3")) {
      colSet("act_charge_amt", "0");
      colSet("err_codee", "00");
      colSet("act_use_point", "0");
    }

    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "verify_flag = ?, " + "verify_remark = ?, "
        + "err_code = ?, " + "proc_flag = ?, " + "proc_date = ?, " + "act_use_point = ?, "
        + "act_charge_amt = ?, " + "crt_user  = ?, " + "crt_date  = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, " + "mod_user  = ?, "
        + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')," + "mod_pgm   = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   tran_seqno  = ? ";

    Object[] param = new Object[] {colStr("verify_flag"), colStr("verify_remark"),
        colStr("err_code"), colStr("proc_flag"), colStr("proc_date"), colNum("act_use_point"),
        colNum("act_charge_amt"), colStr("crt_user"), colStr("crt_date"), wp.loginUser, apr_flag,
        colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"), colStr("tran_seqno")};

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and tran_seqno = ? ";

    Object[] param = new Object[] {colStr("tran_seqno")};

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

  int insertMktBonusDtl(int intType, String tranSeqno) {
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

    Object[] param = new Object[] {colStr("acct_type"), colStr("bonus_type"), "", // col_ss("active_code"),
        "市區停車費-" + colStr("vendor_name"), "4", colNum("beg_tran_bp"), colNum("beg_tran_bp"), "", // res_tran_date
        0, // res_tran_bp
        "N", // col_ss("tax_flag"),
        "", // col_ss("effect_e_date"),
        colStr("park_date_s") + " " + colStr("park_time_s") + "-停"
            + String.format("%.0f", colNum("use_bonus_hr")) + "小時", // mod_desc
        "", // mod_reason
        "", // mod_memo
        wp.sysDate, // tran_date,
        wp.sysTime, // tran_time
        colStr("p_seqno"), colStr("id_p_seqno"), wp.modPgm(), colStr("tran_seqno"),
        comr.getBusinDate().substring(0, 6), comr.getBusinDate(), "Y", wp.loginUser,
        colStr("crt_date"), colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser, wp.modPgm(),
        0};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(sqlErrtext);

    return (1);
  }

  // ************************************************************************
  public int insertBilSysexp() {
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    strSql = " insert into bil_sysexp (" + "card_no, " + "p_seqno, " + "bill_type, " + "txn_code, "
        + "purchase_date, " + "src_amt, " + "dest_amt, " + "dc_dest_amt, " + "dest_curr, "
        + "curr_code, " + "bill_desc, " + "post_flag, " + "ref_key, " + "mod_user, " + "mod_time, "
        + "mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?," + "?,?,?,?,?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + " ?) ";

    Object[] param = new Object[] {colStr("card_no"), colStr("p_seqno"), "OSSG", "DO",
        comr.getBusinDate(), colNum("act_charge_amt"), colNum("act_charge_amt"),
        colNum("act_charge_amt"), "901", "901",
        "市區停車費(" + comm.toChinDate(colStr("park_date_s")) + " "
            + colStr("park_time_s").substring(0, 2) + ":" + colStr("park_time_s").substring(2, 4)
            + ")",

        "N", colStr("tran_seqno"), wp.loginUser, wp.sysDate + wp.sysTime, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return (0);
    }

    return (1);
  }

  // ************************************************************************
  int selectCrdCard() {
    daoTid = "card.";
    strSql =
        " select " + " group_code, " + " card_type " + " from crd_card  a" + " where card_no = ?  ";

    Object[] param = new Object[] {colStr("card_no")};

    sqlSelect(strSql, param);

    return (1);
  }

  // ************************************************************************
  int selectBilDodoParm() {
    daoTid = "parm.";
    strSql = " select " + " min(a.charge_amt) as charge_amt " + " from bil_dodo_parm a,"
        + "      (select action_cd,data_code " + "       from   bil_dodo_bn_data "
        + "       where  data_type = '01') b," + "      (select action_cd,data_code "
        + "       from   bil_dodo_bn_data " + "       where  data_type = '02') c "
        + " where a.action_cd = b.action_cd " + " and   a.action_cd = c.action_cd "
        + " and   b.data_code = ?  " + " and   c.data_code = ? "
        + " group by b.data_code,c.data_code ";

    Object[] param = new Object[] {colStr("card.card_type"), colStr("card.group_code")};

    sqlSelect(strSql, param);

    return (1);
  }

  // ************************************************************************
  int selectMktParkParm() {
    strSql = " select " + " vendor_name " + " from  mkt_park_parm " + " where park_vendor  = ? ";


    Object[] param = new Object[] {colStr("park_vendor")};

    sqlSelect(strSql, param);

    return (1);
  }
  // ************************************************************************

  // ************************************************************************

}  // End of class
