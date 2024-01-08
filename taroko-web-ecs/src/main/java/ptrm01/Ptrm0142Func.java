/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-04  V1.00.00  David FU   program initial                            *
* 106-11-28  V1.00.01  Ryan       update  ptr_actgeneral_n                   *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 109-07-31  V1.00.02  yanghan       修改了页面覆核栏位的名称      *
******************************************************************************/

package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0142Func extends FuncEdit {

  public Ptrm0142Func(TarokoCommon wr) {
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
    if (wp.itemNum("autopay_deduct_days") < 1) {
      errmsg("(一般正常戶)參數必須大於0");
      return;
    }

    if (wp.itemNum("instpay_b_due_days") < 2) {
      errmsg("分期還款繳款截止日前X營業天(分期還款戶)參數必須大於1");
      return;
    }

    if (wp.itemNum("autopay_deduct_days") < 2) {
      errmsg("本行帳號連續自動扣繳Y營業天(一般正常戶)參數必須大於1");
      return;
    }

    if (wp.itemNum("sms_deduct_days") < 1) {
      errmsg("自動扣繳失敗第 N 營業日發送簡訊參數必須大於0");
      return;
    }

    if (wp.itemNum("ach_days") < 1) {
      errmsg("(一般正常戶,郵局帳號除外)參數必須大於0");
      return;
    }


    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from ptr_actgeneral_n where 1=1 and acct_type = ? ";          
      sqlSelect(lsSql, new Object[] {wp.itemStr("kk_acct_type")});
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where acct_type = ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {wp.itemStr("acct_type"), wp.modSeqno()};
      isOtherModify("ptr_actgeneral_n", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_actgeneral_n (" + "  acct_type " + ", REVOLVING_INTEREST1 "
        + ", REVOLVING_INTEREST2" + ", REVOLVING_INTEREST3" + ", REVOLVING_INTEREST4"
        + ", REVOLVING_INTEREST5" + ", REVOLVING_INTEREST6" + ", mix_mp_balance"
        + ", min_percent_payment" + ", mp_1_rate" + ",mp_1_bl_flag " + ",mp_1_ca_flag "
        + ",mp_1_ot_flag " + ",mp_1_ao_flag " + ",mp_1_id_flag " + ",mp_3_rate " + ",mp_mcode "
        + ",sms_deduct_days " + ",ach_days " + ",post_o_days " + ",delmths " + ",rc_use_indicator "
        + ", autopay_b_due_days" + ", autopay_deduct_days" + ", instpay_b_due_days"
        + ", instpay_deduct_days"
        // + ", mi_d_mcode"
        + ", atm_fee" + ", m12_d_b_days" + ", rc_max_rate" + ", payment_lmt"
        + ", crt_date, crt_user" + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
        + " , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" + ", to_char(sysdate,'yyyymmdd'), ?"
        + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("kk_acct_type"), wp.itemNum("REVOLVING_INTEREST1"),
        wp.itemNum("REVOLVING_INTEREST2"), wp.itemNum("REVOLVING_INTEREST3"),
        wp.itemNum("REVOLVING_INTEREST4"), wp.itemNum("REVOLVING_INTEREST5"),
        wp.itemNum("REVOLVING_INTEREST6"), wp.itemNum("mix_mp_balance"),
        wp.itemNum("min_percent_payment"), wp.itemNum("mp_1_rate"), wp.itemStr("mp_1_bl_flag"),
        wp.itemStr("mp_1_ca_flag"), wp.itemStr("mp_1_ot_flag"), wp.itemStr("mp_1_ao_flag"),
        wp.itemStr("mp_1_id_flag"), wp.itemNum("mp_3_rate"), wp.itemNum("mp_mcode"),
        wp.itemNum("sms_deduct_days"), wp.itemNum("ach_days"), wp.itemNum("post_o_days"),
        wp.itemNum("delmths"), wp.itemStr("rc_use_indicator"), wp.itemNum("autopay_b_due_days"),
        wp.itemNum("autopay_deduct_days"), wp.itemNum("instpay_b_due_days"),
        wp.itemNum("instpay_deduct_days")
        // , wp.item_num("mi_d_mcode")
        , wp.itemNum("atm_fee"), wp.itemNum("m12_d_b_days"), wp.itemNum("rc_max_rate"),
        wp.itemNum("payment_lmt"), wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_actgeneral_n set " + " REVOLVING_INTEREST1 = ?"
        + ", REVOLVING_INTEREST2 = ?" + ", REVOLVING_INTEREST3 = ?" + ", REVOLVING_INTEREST4 = ?"
        + ", REVOLVING_INTEREST5 = ?" + ", REVOLVING_INTEREST6 = ?" + ", mix_mp_balance = ?"
        + ", min_percent_payment = ? " + ", mp_1_rate = ?" + ", mp_1_bl_flag =? "
        + ", mp_1_ca_flag =? " + ", mp_1_ot_flag =? " + ", mp_1_ao_flag =? " + ", mp_1_id_flag =? "
        + ", mp_3_rate =? " + ", mp_mcode =? " + ", sms_deduct_days =? " + ", ach_days =? "
        + ", post_o_days =? " + ", delmths =? " + ", rc_use_indicator =? "
        + ", autopay_b_due_days = ?" + ", autopay_deduct_days = ?" + ", instpay_b_due_days = ?"
        + ", instpay_deduct_days = ?"
        // + ", mi_d_mcode = ?"
        + ", atm_fee = ?" + ", m12_d_b_days = ?" + ", rc_max_rate = ?" + ", payment_lmt = ?"
        + " , crt_date=to_char(sysdate,'yyyymmdd'),crt_user=? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemNum("REVOLVING_INTEREST1"),
        wp.itemNum("REVOLVING_INTEREST2"), wp.itemNum("REVOLVING_INTEREST3"),
        wp.itemNum("REVOLVING_INTEREST4"), wp.itemNum("REVOLVING_INTEREST5"),
        wp.itemNum("REVOLVING_INTEREST6"), wp.itemNum("mix_mp_balance"),
        wp.itemNum("min_percent_payment"), wp.itemNum("mp_1_rate"), wp.itemStr("mp_1_bl_flag"),
        wp.itemStr("mp_1_ca_flag"), wp.itemStr("mp_1_ot_flag"), wp.itemStr("mp_1_ao_flag"),
        wp.itemStr("mp_1_id_flag"), wp.itemNum("mp_3_rate"), wp.itemNum("mp_mcode"),
        wp.itemNum("sms_deduct_days"), wp.itemNum("ach_days"), wp.itemNum("post_o_days"),
        wp.itemNum("delmths"), wp.itemStr("rc_use_indicator"), wp.itemNum("autopay_b_due_days"),
        wp.itemNum("autopay_deduct_days"), wp.itemNum("instpay_b_due_days"),
        wp.itemNum("instpay_deduct_days"), wp.itemNum("atm_fee"), wp.itemNum("m12_d_b_days"),
        wp.itemNum("rc_max_rate"), wp.itemNum("payment_lmt"), wp.itemStr("approval_user"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemStr("acct_type"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;

  }


  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    return rc;
  }

}
