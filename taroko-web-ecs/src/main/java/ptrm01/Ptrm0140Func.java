/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-04  V1.00.00  David FU   program initial                            *
* 106-11-28  V1.00.01  Ryan       update  ptr_actgeneral_n                   *
* 106-12-11  V1.00.02  Ryan       update  問題單0001990				               *
* 109-04-20  V1.00.03  Tanwei     updated for project coding standard        *
* 111-12-09  V1.00.04  Simon      1.handle REVOLVING_INTEREST1 updated from ptrm0010*
*                                 2.remove none TCB parameters               *
*                                 3.add parameter overpayment_lmt            
* 112-12-15  V1.00.05  Ryan       增加年利率計算&顯示                                                                                  *
******************************************************************************/

package ptrm01;

import java.math.BigDecimal;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0140Func extends FuncEdit {

  public Ptrm0140Func(TarokoCommon wr) {
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

    //if (wp.itemNum("instpay_deduct_days") < 2) {
    //  errmsg("分期還款扣款Y營業天(分期還款戶)參數必須大於1");
    //  return;
    //}

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

    countYearRate();

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
        + ", REVOLVING_INTEREST5" + ", REVOLVING_INTEREST6" + ", mix_mp_balance" + ", mp_1_rate"
        + ",mp_1_bl_flag "// 10
        + ",mp_1_ca_flag " + ",mp_1_ot_flag " + ",mp_1_ao_flag " + ",mp_1_id_flag " + ",mp_3_rate "
        + ",mp_mcode " + ",sms_deduct_days " + ",ach_days " + ",post_o_days " + ",delmths "// 20
        + ",rc_use_indicator " + ", autopay_b_due_days" + ", autopay_deduct_days"
      //+ ", instpay_b_due_days" + ", instpay_deduct_days" + ", atm_fee" + ", m12_d_b_days"
        + ", rc_max_rate" + ", payment_lmt"// 29 --> 25
        + ", overpayment_lmt"
        + ", crt_date, crt_user" + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + "  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
        + ", ?, ?, ?, ?, ?, ?, ?, ?, ?" + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1"
        + " )";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("kk_acct_type"), wp.itemNum("REVOLVING_INTEREST1"),
        wp.itemNum("REVOLVING_INTEREST2"), wp.itemNum("REVOLVING_INTEREST3"),
        wp.itemNum("REVOLVING_INTEREST4"), wp.itemNum("REVOLVING_INTEREST5"),
        wp.itemNum("REVOLVING_INTEREST6"), wp.itemNum("mix_mp_balance"), wp.itemNum("mp_1_rate"),
        wp.itemStr("mp_1_bl_flag"), wp.itemStr("mp_1_ca_flag"), wp.itemStr("mp_1_ot_flag"),
        wp.itemStr("mp_1_ao_flag"), wp.itemStr("mp_1_id_flag"), wp.itemNum("mp_3_rate"),
        wp.itemNum("mp_mcode"), wp.itemNum("sms_deduct_days"), wp.itemNum("ach_days"),
        wp.itemNum("post_o_days"), wp.itemNum("delmths"), wp.itemStr("rc_use_indicator"),
        wp.itemNum("autopay_b_due_days"), wp.itemNum("autopay_deduct_days"),
      //wp.itemNum("instpay_b_due_days"), wp.itemNum("instpay_deduct_days"), wp.itemNum("atm_fee"),
      //wp.itemNum("m12_d_b_days"), 
        wp.itemNum("rc_max_rate"), wp.itemNum("payment_lmt"),
        wp.itemNum("overpayment_lmt"),
        wp.itemStr("apr_user"), wp.loginUser, wp.itemStr("mod_pgm")};
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
        + ", mp_1_rate = ?" + ", mp_1_bl_flag =? " + ", mp_1_ca_flag =? " + ", mp_1_ot_flag =? "
        + ", mp_1_ao_flag =? " + ", mp_1_id_flag =? " + ", mp_3_rate =? " + ", mp_mcode =? "
        + ", sms_deduct_days =? " + ", ach_days =? " + ", post_o_days =? " + ", delmths =? "
        + ", rc_use_indicator =? " + ", autopay_b_due_days = ?" + ", autopay_deduct_days = ?"
      //+ ", instpay_b_due_days = ?" + ", instpay_deduct_days = ?" + ", atm_fee = ?"
      //+ ", m12_d_b_days = ?" 
        + ", rc_max_rate = ?" + ", payment_lmt = ?"
        + ", overpayment_lmt = ?"
        + " , crt_date=to_char(sysdate,'yyyymmdd'),crt_user=? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param =
        new Object[] {wp.itemNum("REVOLVING_INTEREST1"), wp.itemNum("REVOLVING_INTEREST2"),
            wp.itemNum("REVOLVING_INTEREST3"), wp.itemNum("REVOLVING_INTEREST4"),
            wp.itemNum("REVOLVING_INTEREST5"), wp.itemNum("REVOLVING_INTEREST6"),
            wp.itemNum("mix_mp_balance"), wp.itemNum("mp_1_rate"), wp.itemStr("mp_1_bl_flag"),
            wp.itemStr("mp_1_ca_flag"), wp.itemStr("mp_1_ot_flag"), wp.itemStr("mp_1_ao_flag"),
            wp.itemStr("mp_1_id_flag"), wp.itemNum("mp_3_rate"), wp.itemNum("mp_mcode"),
            wp.itemNum("sms_deduct_days"), wp.itemNum("ach_days"), wp.itemNum("post_o_days"),
            wp.itemNum("delmths"), wp.itemStr("rc_use_indicator"), wp.itemNum("autopay_b_due_days"),
            wp.itemNum("autopay_deduct_days"), 
          //wp.itemNum("instpay_b_due_days"),
          //wp.itemNum("instpay_deduct_days"), wp.itemNum("atm_fee"), wp.itemNum("m12_d_b_days"),
            wp.itemNum("rc_max_rate"), wp.itemNum("payment_lmt"), 
            wp.itemNum("overpayment_lmt"), 
            wp.itemStr("apr_user"),
            wp.loginUser, wp.itemStr("mod_pgm"), wp.itemStr("acct_type"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;

  }

  private void countYearRate() {
	  for(int i = 1 ;i<= 7;i++) {
		  double revolvingInterest = i==7?wp.itemNum("rc_max_rate"):wp.itemNum("REVOLVING_INTEREST" + i);
		  double yearRate = new BigDecimal(revolvingInterest).	  
	              multiply(BigDecimal.valueOf(365)).
	              divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
		  wp.colSet("year_rate"+i, yearRate);
	  }
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
