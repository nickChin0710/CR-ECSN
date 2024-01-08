/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-29  V1.00.01    Ryan       program initial                          *
* 109-05-06  V1.00.02    Aoyulan       updated for project coding standard   * 
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
* 112-10-31  V1.00.04    Ryan       修正修改存檔錯誤                                                                                    *   
******************************************************************************/
package colm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Colm1020Func extends FuncEdit {
  String liabType = "", liabStatus = "";
  String blockMark1 = "", blockMark3 = "";
  String payStageMark = "";
  String dBalFlag = "";
  String oppostFlag = "";
  String oppostReason = "", jcicPayrateFlag = "", noautoBalanceFlag = "";

  public Colm1020Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }


  @Override
  public void dataCheck() {
    liabType = wp.itemStr("kk_liab_type");
    liabStatus = wp.itemStr("kk_liab_status");
    if (empty(liabType)) {
      liabType = wp.itemStr("liab_type");
    }
    if (empty(liabStatus)) {
      liabStatus = wp.itemStr("liab_status");
    }
    if (liabType.length() < 1) {
      errmsg("請輸入 參數類別 不可空白");
      return;
    }
    if (liabStatus.length() < 1) {
      errmsg("請輸入 參數類別狀態 不可空白");
      return;
    }
    blockMark1 = wp.itemStr("block_mark1");
    blockMark3 = wp.itemStr("block_mark3");
    payStageMark = wp.itemStr("pay_stage_mark");
    dBalFlag = wp.itemStr("d_bal_flag");
    oppostFlag = wp.itemStr("oppost_flag");
    oppostReason = wp.itemStr("oppost_reason");
    jcicPayrateFlag = wp.itemStr("jcic_payrate_flag");
    noautoBalanceFlag = wp.itemStr("noauto_balance_flag");
    if (this.isDelete()) {
      return;
    }
    if (!wp.itemStr("block_flag").equals("Y")) {
      blockMark1 = "";
      blockMark3 = "";
    }
    if (wp.itemStr("block_flag").equals("Y")) {
      if (empty(blockMark1)) {
        errmsg("請輸入 凍結碼註記一 !");
        return;
      }
      if (empty(blockMark3)) {
        errmsg("請輸入 凍結碼註記三 !");
        return;
      }
    }
    if (wp.itemStr("pay_stage_flag").equals("Y")) {
      switch (wp.itemStr("liab_type")) {
        case "1":
          payStageMark = "NE";
          break;
        case "2":
          payStageMark = "NF";
          break;
        case "3":
          payStageMark = "NK";
          break;
        case "4":
          payStageMark = "NO";
          break;
        case "7":
          payStageMark = "NG";
          break;
      }
    }

    if (!liabType.equals("2") || !liabStatus.equals("3")) {
      dBalFlag = "N";
    }

    if (liabType.equals("2") && oppostFlag.equals("Y")) {
      if (empty(oppostReason)) {
        oppostReason = "AX";
      }
    } else if ((liabType.equals("3") || liabType.equals("4")) && oppostFlag.equals("Y")) {
      if (empty(oppostReason)) {
        oppostReason = "AZ";
      }
    } else if (liabType.equals("6") && oppostFlag.equals("Y")) {
      if (empty(oppostReason)) {
        oppostReason = "A0";
      }
    } else if (liabType.equals("7") && oppostFlag.equals("Y")) {
      if (empty(oppostReason)) {
        oppostReason = "AX";
      }
    } else
      oppostReason = "";

    if (liabType.equals("1")) {
      oppostReason = "";
      jcicPayrateFlag = "N";
      oppostFlag = "N";
      noautoBalanceFlag = "N";
    }

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from col_liab_param where liab_type = ? and liab_status=?";
      Object[] param = new Object[] {liabType, liabStatus};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    }

    sqlWhere = " where liab_type= ? " + "and liab_status=?" + " and mod_seqno = ? ";
    Object[] param = new Object[] {liabType, liabStatus, wp.modSeqno()};
    if (this.isOtherModify("col_liab_param", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql =
        "insert into col_liab_param (" + " liab_type, " + " liab_status, " + "stat_unprint_flag,"
            + "no_tel_coll_flag," + " no_delinquent_flag, " + " no_collection_flag, "
            + " no_f_stop_flag, " + " revolve_rate_flag, " + " no_penalty_flag, " + " no_sms_flag, "//10
            + " min_pay_flag, " + " autopay_flag, " + " pay_stage_flag, " + " pay_stage_mark, "
            + " block_flag, " + " block_mark1, " + " block_mark3, " + " send_cs_flag, "
            + " d_bal_flag, " + " jcic_payrate_flag, " + " oppost_flag, " + " oppost_reason, "//22
            + " noauto_balance_flag, " + " no_interest_flag, " + " end_flag," + " end_d_bal_flag,"//26
            // +" nego_effect_flag,"
            + " crt_date, " + " crt_user, " + " apr_date, " + " apr_user, " + " mod_user, "
            + " mod_time, " + " mod_pgm, " + " mod_seqno" + " ) values (" //34
            + " ?,?,?,?,?,?,?,?,?,?,"//10
            + " ?,?,?,?,?,?,?,?,?,?,"//10
            + " ?,?,?,?,?,?"//6
            + ",to_char(sysdate,'yyyymmdd'),?,to_char(sysdate,'yyyymmdd'),?,?,sysdate,?,1" + " )";//8


    // -set ?value-
    Object[] param = new Object[] {liabType // source_code
        , liabStatus, wp.itemStr("stat_unprint_flag"), wp.itemStr("no_tel_coll_flag"),
        wp.itemStr("no_delinquent_flag"), wp.itemStr("no_collection_flag"),
        wp.itemStr("no_f_stop_flag"), wp.itemStr("revolve_rate_flag"),
        wp.itemStr("no_penalty_flag"), wp.itemStr("no_sms_flag"), wp.itemStr("min_pay_flag"),//11
        wp.itemStr("autopay_flag"), wp.itemStr("pay_stage_flag"), payStageMark,
        wp.itemStr("block_flag"), blockMark1, blockMark3, wp.itemStr("send_cs_flag"), dBalFlag,
        jcicPayrateFlag, oppostFlag, oppostReason, noautoBalanceFlag,//23
        wp.itemStr("no_interest_flag"), wp.itemStr("end_flag"), wp.itemStr("end_d_bal_flag")
        // , wp.item_ss("nego_effect_flag")
        , wp.loginUser, wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm")};//30
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  public int insertLog() {
    dataCheck();
    strSql = "insert into col_liab_param_log (" + " liab_type, " + " liab_status, "
        + "stat_unprint_flag," + "no_tel_coll_flag," + " no_delinquent_flag, "
        + " no_collection_flag, " + " no_f_stop_flag, " + " revolve_rate_flag, "
        + " no_penalty_flag, " + " no_sms_flag, " + " min_pay_flag, " + " autopay_flag, "//12
        + " pay_stage_flag, " + " pay_stage_mark, " + " block_flag, " + " block_mark1, "
        + " block_mark3, " + " send_cs_flag, " + " d_bal_flag, " + " jcic_payrate_flag, "//20
        + " oppost_flag, " + " oppost_reason, " 
        //+ " noauto_balance_flag, " 
        + " no_interest_flag, "
        + " end_flag, " + " end_d_bal_flag, " + " mod_action, " + " mod_datetime, "//27
        // +" nego_effect_flag,"
        + " crt_date, " + " crt_user, " + " apr_date, " + " apr_user, " + " mod_user, "
        + " mod_time, " + " mod_pgm, " + " mod_seqno" + " ) values ("//35
        + " ?,?,?,?,?,?,?,?,?,?,"//10
        + " ?,?,?,?,?,?,?,?,?,?,"//10
        + " ?,?,?,?,?,?,to_char(sysdate,'yyyymmdd')||' '||to_char(sysdate,'hhmmss')"//7
        + ",to_char(sysdate,'yyyymmdd'),?,to_char(sysdate,'yyyymmdd'),?,?,sysdate,?,1" + " )";//8


    // -set ?value-
    Object[] param = new Object[] {
    	  liabType // source_code
        , liabStatus, 
        wp.itemStr("stat_unprint_flag"), 
        wp.itemStr("no_tel_coll_flag"),
        wp.itemStr("no_delinquent_flag"), 
        wp.itemStr("no_collection_flag"),
        wp.itemStr("no_f_stop_flag"), 
        wp.itemStr("revolve_rate_flag"),
        wp.itemStr("no_penalty_flag"), 
        wp.itemStr("no_sms_flag"), //10
        wp.itemStr("min_pay_flag"),
        wp.itemStr("autopay_flag"), 
        wp.itemStr("pay_stage_flag"), 
        payStageMark,
        wp.itemStr("block_flag"), 
        blockMark1,
        blockMark3, 
        wp.itemStr("send_cs_flag"), 
        dBalFlag,
        jcicPayrateFlag,  //20
        oppostFlag,
        oppostReason, 
       // noautoBalanceFlag,
        wp.itemStr("no_interest_flag"), 
        wp.itemStr("end_flag"), 
        wp.itemStr("end_d_bal_flag"),
        varsStr("is_action")
        // , wp.item_ss("nego_effect_flag")
        , wp.loginUser
        , wp.itemStr("approval_user"), 
        wp.loginUser
        , wp.itemStr("mod_pgm")};//30
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update col_liab_param set " + "stat_unprint_flag=?," + "no_tel_coll_flag=?,"
        + " no_delinquent_flag=?, " + " no_collection_flag=?, " + " no_f_stop_flag=?, "
        + " revolve_rate_flag=?, " + " no_penalty_flag=?, " + " no_sms_flag=?, "
        + " min_pay_flag=?, " + " autopay_flag=?, " + " pay_stage_flag=?, " + " pay_stage_mark=?, "
        + " block_flag=?, " + " block_mark1=?, " + " block_mark3=?, " + " send_cs_flag=?, "
        + " d_bal_flag=?, " + " jcic_payrate_flag=?, " + " oppost_flag=?, " + " oppost_reason=?, "
        + " noauto_balance_flag=?, " + " no_interest_flag=?, " + " end_flag = ?,"
        + " end_d_bal_flag = ?, "
        // +" nego_effect_flag =?, "
        + " apr_date = to_char(sysdate,'yyyymmdd'), " + " apr_user = ?, " + " mod_user =?, "
        + " mod_time=sysdate, " + " mod_pgm =? ," + " mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;

    Object[] param = new Object[] {wp.itemStr("stat_unprint_flag"), wp.itemStr("no_tel_coll_flag"),
        wp.itemStr("no_delinquent_flag"), wp.itemStr("no_collection_flag"),
        wp.itemStr("no_f_stop_flag"), wp.itemStr("revolve_rate_flag"),
        wp.itemStr("no_penalty_flag"), wp.itemStr("no_sms_flag"), wp.itemStr("min_pay_flag"),
        wp.itemStr("autopay_flag"), wp.itemStr("pay_stage_flag"), payStageMark,
        wp.itemStr("block_flag"), blockMark1, blockMark3, wp.itemStr("send_cs_flag"), dBalFlag,
        jcicPayrateFlag, oppostFlag, oppostReason, noautoBalanceFlag,
        wp.itemStr("no_interest_flag"), wp.itemStr("end_flag"), wp.itemStr("end_d_bal_flag")
        // , wp.item_ss("nego_effect_flag")
        , wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"), liabType, liabStatus, wp.modSeqno()};


    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }


  @Override
  public int dbDelete() {
    actionInit("D");
    if (rc != 1) {
      return rc;
    }
    sqlWhere = " where liab_type= ? " + "and liab_status=?" + " and nvl(mod_seqno,0) = ? ";
    strSql = "delete col_liab_param" + sqlWhere;
    // ddd("del-sql="+is_sql);
    Object[] param =
        new Object[] {wp.itemStr("liab_type"), wp.itemStr("liab_status"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
