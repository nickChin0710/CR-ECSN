/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;
/** 2019-0619:  JH    p_xxx >>acno_pxxx
 * */
import busi.FuncAction;

public class Rskm0940Func extends FuncAction {
  String seqNo = "", lsSeqno = "", lsCardCond = "", lsAudiItem = "", lsRiskTypeVal = "",
      lsRemark = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      if (setSeqNo() < 0) {
        errmsg("流水編號 編號錯誤 !");
        log("A:" + rc);
        return;
      }
      seqNo = lsSeqno;
    } else {
      seqNo = wp.itemStr("seq_no");
    }

    if (this.ibDelete) {
      if (wp.itemEq("apr_flag", "Y")) {
        errmsg("已覆核 不可刪除");
        return;
      }
      return;
    }

    if (wp.itemEmpty("id_no") || wp.itemEmpty("card_no")) {
      errmsg("持卡人ID 及 卡號  不可空白");
      return;
    }

    if (checkCard() == false) {
      errmsg("卡號為無效卡 or 卡人ID與卡號ID  不同");
      return;
    }

    if (this.chkStrend(wp.itemStr("abroad_date1"), wp.itemStr("abroad_date2")) == -1) {
      errmsg("出國消費  起迄輸入錯誤~");
      return;
    }

    if (wp.itemEmpty("adj_date1") || wp.itemEmpty("adj_date2")) {
      errmsg("異動期間: 不可空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("adj_date1"), wp.itemStr("adj_date2")) == -1) {
      errmsg("異動期間  起迄輸入錯誤~");
      return;
    }

    lsCardCond = wp.itemNvl("db_card_cond01", "N") + wp.itemNvl("db_card_cond02", "N")
        + wp.itemNvl("db_card_cond03", "N") + wp.itemNvl("db_card_cond04", "N");

    if (this.pos(lsCardCond, "Y") < 0) {
      errmsg("基本資料及持卡狀況: 至少要勾選一個");
      return;
    }

    lsAudiItem = wp.itemNvl("db_audi_item01", "N") + wp.itemNvl("db_audi_item02", "N")
        + wp.itemNvl("db_audi_item03", "N") + wp.itemNvl("db_audi_item04", "N")
        + wp.itemNvl("db_audi_item05", "N") + wp.itemNvl("db_audi_item06", "N")
        + wp.itemNvl("db_audi_item07", "N") + wp.itemNvl("db_audi_item08", "N")
        + wp.itemNvl("db_audi_item09", "N") + wp.itemNvl("db_audi_item10", "N");

    lsRiskTypeVal = wp.itemNvl("db_risk_P", "N") + wp.itemNvl("db_risk_D1", "N")
        + wp.itemNvl("db_risk_T", "N") + wp.itemNvl("db_risk_H", "N") + wp.itemNvl("db_risk_E", "N")
        + wp.itemNvl("db_risk_M", "N") + wp.itemNvl("db_risk_J", "N") + wp.itemNvl("db_risk_R", "N")
        + wp.itemNvl("db_risk_L", "N") + wp.itemNvl("db_risk_I", "N") + wp.itemNvl("db_risk_X", "N")
        + wp.itemNvl("db_risk_G", "N") + wp.itemNvl("db_risk_P1", "N")
        + wp.itemNvl("db_risk_P2", "N") + wp.itemNvl("db_risk_P3", "N")
        + wp.itemNvl("db_risk_P0", "N") + wp.itemNvl("db_risk_M1", "N")
        + wp.itemNvl("db_risk_M2", "N") + wp.itemNvl("db_risk_C", "N");

    lsRemark = wp.itemStr("adj_remark0") + wp.itemStr("adj_remark1");

  }

  int setSeqNo() {
    int seqNoI = 0;
    String seqNoS = "";
    String sql1 =
        " select " + " max(seq_no) as ls_seqno " + " from rsk_credits_vd " + " where crt_date = ? ";
    sqlSelect(sql1, new Object[] {getSysDate()});

    if (sqlRowNum < 0)
      return -1;
    if (sqlRowNum == 0 || empty(colStr("ls_seqno"))) {
      lsSeqno = this.getSysDate() + "001";
      return 1;
    }

    seqNoI = commString.strToInt(commString.mid(colStr("ls_seqno"), 7, 4)) + 1;
    seqNoS = commString.mid(commString.intToStr(seqNoI), 1, 3);
    lsSeqno = getSysDate() + seqNoS;
    return 1;
  }

  boolean checkCard() {
    String sql1 = " select " + " count(*) as db_cnt " + " from dbc_card A , dbc_idno B "
        + " where A.id_p_seqno = B.id_p_seqno " + " and A.card_no = ? " + " and B.id_no = ? "
        + " and A.current_code ='0' ";
    sqlSelect(sql1, new Object[] {wp.itemStr("card_no"), wp.itemStr("id_no")});
    if (sqlRowNum <= 0 || colNum("db_cnt") == 0) {
      return false;
    }

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " insert into rsk_credits_vd ( " + " seq_no , " + " tel_user , " + " tel_date , "
        + " tel_time , " + " apr_flag , " + " apr_user , " + " id_no , " + " id_code , "
        + " card_no , " + " chi_name , " + " print_flag , " + " terror_money , " + " card_cond , "
        + " audi_item , " + " risk_type_val , " + " audi_oth_note , " + " area_code , "
        + " self_flag , " + " purch_flag , " + " purch_item_flag , " + " consum_item , "
        + " consum_amt_note , " + " abroad_flag , " + " cntry_code , " + " abroad_date1 , "
        + " abroad_date2 , " + " over_lmt_parm_flag , " + " over_time_flag , " + " over_dd_flag , "
        + " over_mm_flag , " + " dd_cnt_flag , " + " mm_cnt_flag , " + " purchase_item , "
        + " purchase_item2 , " + " purchase_item3 , " + " oth_flag , " + " oth_reason , "
        + " adj_date1 , " + " adj_date2 , " + " adj_remark , " + " adj_remark2 , " + " fast_flag , "
        + " close_flag , " + " sms_flag , " + " tel_flag , " + " tel_no , " + " email_flag , "
        + " email_addr , " + " reply_date , " + " reply_time , " + " audit_remark , "
        + " audit_result , " + " charge_user , " + " appr_passwd , " + " user_id , "
        + " decs_user , " + " time_lmt , " + " dd_lmt , " + " mm_lmt , " + " time_pcnt , "
        + " dd_pcnt , " + " mm_pcnt , " + " dd_cnt_pcnt , " + " mm_cnt_pcnt , " + " dd_cnt , "
        + " mm_cnt ," + " no_callback_flag ," + " risk_type2 ," + " crt_user , " + " crt_date , "
        + " mod_user , " + " mod_time , " + " mod_pgm ," + " mod_seqno " + " ) values ( "
        + " :seq_no , " + " :tel_user , " + " :tel_date , " + " :tel_time , " + " :apr_flag , "
        + " :apr_user , " + " :id_no , " + " '0' , " + " :card_no , " + " :chi_name , "
        + " :print_flag , " + " :terror_money , " + " :card_cond , " + " :audi_item , "
        + " :risk_type_val , " + " :audi_oth_note , " + " :area_code , " + " :self_flag , "
        + " :purch_flag , " + " :purch_item_flag , " + " :consum_item , " + " :consum_amt_note , "
        + " :abroad_flag , " + " :cntry_code , " + " :abroad_date1 , " + " :abroad_date2 , "
        + " :over_lmt_parm_flag , " + " :over_time_flag , " + " :over_dd_flag , "
        + " :over_mm_flag , " + " :dd_cnt_flag , " + " :mm_cnt_flag , " + " :purchase_item , "
        + " :purchase_item2 , " + " :purchase_item3 , " + " :oth_flag , " + " :oth_reason , "
        + " :adj_date1 , " + " :adj_date2 , " + " :adj_remark , " + " :adj_remark2 , "
        + " :fast_flag , " + " :close_flag , " + " :sms_flag , " + " :tel_flag , " + " :tel_no , "
        + " :email_flag , " + " :email_addr , " + " :reply_date , " + " :reply_time , "
        + " :audit_remark , " + " :audit_result , " + " :charge_user , " + " :appr_passwd , "
        + " :user_id , " + " :decs_user , " + " :time_lmt , " + " :dd_lmt , " + " :mm_lmt , "
        + " :time_pcnt , " + " :dd_pcnt , " + " :mm_pcnt , " + " :dd_cnt_pcnt , "
        + " :mm_cnt_pcnt , " + " :dd_cnt , " + " :mm_cnt ," + " :no_callback_flag ,"
        + " :risk_type2 ," + " :crt_user , " + " to_char(sysdate,'yyyymmdd') ," + " :mod_user , "
        + " sysdate , " + " :mod_pgm ," + " 1 " + " ) ";

    setString("seq_no", seqNo);
    item2ParmStr("tel_user");
    item2ParmStr("tel_date");
    item2ParmStr("tel_time");
    item2ParmNvl("apr_flag", "N");
    item2ParmStr("apr_user");
    item2ParmStr("id_no");
    item2ParmStr("card_no");
    item2ParmStr("chi_name");
    item2ParmNvl("print_flag", "N");
    item2ParmNvl("terror_money", "N");
    setString("card_cond", lsCardCond);
    setString("audi_item", lsAudiItem);
    setString("risk_type_val", lsRiskTypeVal);
    item2ParmStr("audi_oth_note");
    item2ParmStr("area_code");
    item2ParmNvl("self_flag", "N");
    item2ParmNvl("purch_flag", "N");
    item2ParmNvl("purch_item_flag", "N");
    item2ParmStr("consum_item");
    item2ParmStr("consum_amt_note");
    item2ParmNvl("abroad_flag", "N");
    item2ParmStr("cntry_code");
    item2ParmStr("abroad_date1");
    item2ParmStr("abroad_date2");
    item2ParmNvl("over_lmt_parm_flag", "N");
    item2ParmNvl("over_time_flag", "N");
    item2ParmNvl("over_dd_flag", "N");
    item2ParmNvl("over_mm_flag", "N");
    item2ParmNvl("dd_cnt_flag", "N");
    item2ParmNvl("mm_cnt_flag", "N");
    item2ParmStr("purchase_item");
    item2ParmStr("purchase_item2");
    item2ParmStr("purchase_item3");
    item2ParmNvl("oth_flag", "N");
    item2ParmStr("oth_reason");
    item2ParmStr("adj_date1");
    item2ParmStr("adj_date2");
    setString("adj_remark", lsRemark);
    item2ParmStr("adj_remark2");
    item2ParmNvl("fast_flag", "N");
    item2ParmNvl("close_flag", "N");
    item2ParmNvl("sms_flag", "N");
    item2ParmNvl("tel_flag", "N");
    item2ParmStr("tel_no");
    item2ParmNvl("email_flag", "N");
    item2ParmStr("email_addr");
    item2ParmStr("reply_date");
    item2ParmStr("reply_time");
    item2ParmStr("audit_remark");
    item2ParmStr("audit_result");
    item2ParmStr("charge_user");
    item2ParmStr("appr_passwd");
    item2ParmStr("user_id");
    item2ParmStr("decs_user");
    item2ParmNum("time_lmt");
    item2ParmNum("dd_lmt");
    item2ParmNum("mm_lmt");
    item2ParmNum("time_pcnt");
    item2ParmNum("dd_pcnt");
    item2ParmNum("mm_pcnt");
    item2ParmNum("dd_cnt_pcnt");
    item2ParmNum("mm_cnt_pcnt");
    item2ParmNum("dd_cnt");
    item2ParmNum("mm_cnt");
    item2ParmNvl("no_callback_flag", "N");
    item2ParmStr("risk_type2");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskm0940");
    wp.logSql2("A:", strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_credits_vd error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update rsk_credits_vd set " + " tel_user =:tel_user ," + " tel_date =:tel_date ,"
        + " tel_time =:tel_time ," + " apr_flag =:apr_flag ," + " apr_user =:apr_user ,"
        + " id_no =:id_no ," + " card_no =:card_no ," + " chi_name =:chi_name ,"
        + " print_flag =:print_flag ," + " terror_money =:terror_money ,"
        + " card_cond =:card_cond ," + " audi_item =:audi_item ,"
        + " risk_type_val =:risk_type_val ," + " audi_oth_note =:audi_oth_note ,"
        + " area_code =:area_code ," + " self_flag =:self_flag ," + " purch_flag =:purch_flag ,"
        + " purch_item_flag =:purch_item_flag ," + " consum_item =:consum_item ,"
        + " consum_amt_note =:consum_amt_note ," + " abroad_flag =:abroad_flag ,"
        + " cntry_code =:cntry_code ," + " abroad_date1 =:abroad_date1 ,"
        + " abroad_date2 =:abroad_date2 ," + " over_lmt_parm_flag =:over_lmt_parm_flag ,"
        + " over_time_flag =:over_time_flag ," + " over_dd_flag =:over_dd_flag ,"
        + " over_mm_flag =:over_mm_flag ," + " dd_cnt_flag =:dd_cnt_flag ,"
        + " mm_cnt_flag =:mm_cnt_flag ," + " purchase_item =:purchase_item ,"
        + " purchase_item2 =:purchase_item2 ," + " purchase_item3 =:purchase_item3 ,"
        + " oth_flag =:oth_flag ," + " oth_reason =:oth_reason ," + " adj_date1 =:adj_date1 ,"
        + " adj_date2 =:adj_date2 ," + " adj_remark =:adj_remark ," + " adj_remark2 =:adj_remark2 ,"
        + " fast_flag =:fast_flag ," + " close_flag =:close_flag ," + " sms_flag =:sms_flag ,"
        + " tel_flag =:tel_flag ," + " tel_no =:tel_no ," + " email_flag =:email_flag ,"
        + " email_addr =:email_addr ," + " reply_date =:reply_date ," + " reply_time =:reply_time ,"
        + " audit_remark =:audit_remark ," + " audit_result =:audit_result ,"
        + " charge_user =:charge_user ," + " appr_passwd =:appr_passwd ," + " user_id =:user_id ,"
        + " decs_user =:decs_user ," + " time_lmt =:time_lmt ," + " dd_lmt =:dd_lmt ,"
        + " mm_lmt =:mm_lmt ," + " time_pcnt =:time_pcnt ," + " dd_pcnt =:dd_pcnt ,"
        + " mm_pcnt =:mm_pcnt ," + " dd_cnt_pcnt =:dd_cnt_pcnt ," + " mm_cnt_pcnt =:mm_cnt_pcnt ,"
        + " dd_cnt =:dd_cnt ," + " mm_cnt =:mm_cnt ," + " no_callback_flag =:no_callback_flag ,"
        + " risk_type2 =:risk_type2 ," + " mod_user =:mod_user ," + " mod_time =sysdate ,"
        + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where seq_no =:seq_no ";

    setString("seq_no", seqNo);
    item2ParmStr("tel_user");
    item2ParmStr("tel_date");
    item2ParmStr("tel_time");
    item2ParmNvl("apr_flag", "N");
    item2ParmStr("apr_user");
    item2ParmStr("id_no");
    item2ParmStr("card_no");
    item2ParmStr("chi_name");
    item2ParmNvl("print_flag", "N");
    item2ParmNvl("terror_money", "N");
    setString("card_cond", lsCardCond);
    setString("audi_item", lsAudiItem);
    setString("risk_type_val", lsRiskTypeVal);
    item2ParmStr("audi_oth_note");
    item2ParmStr("area_code");
    item2ParmNvl("self_flag", "N");
    item2ParmNvl("purch_flag", "N");
    item2ParmNvl("purch_item_flag", "N");
    item2ParmStr("consum_item");
    item2ParmStr("consum_amt_note");
    item2ParmNvl("abroad_flag", "N");
    item2ParmStr("cntry_code");
    item2ParmStr("abroad_date1");
    item2ParmStr("abroad_date2");
    item2ParmNvl("over_lmt_parm_flag", "N");
    item2ParmNvl("over_time_flag", "N");
    item2ParmNvl("over_dd_flag", "N");
    item2ParmNvl("over_mm_flag", "N");
    item2ParmNvl("dd_cnt_flag", "N");
    item2ParmNvl("mm_cnt_flag", "N");
    item2ParmStr("purchase_item");
    item2ParmStr("purchase_item2");
    item2ParmStr("purchase_item3");
    item2ParmNvl("oth_flag", "N");
    item2ParmStr("oth_reason");
    item2ParmStr("adj_date1");
    item2ParmStr("adj_date2");
    setString("adj_remark", lsRemark);
    item2ParmStr("adj_remark2");
    item2ParmNvl("fast_flag", "N");
    item2ParmNvl("close_flag", "N");
    item2ParmNvl("sms_flag", "N");
    item2ParmNvl("tel_flag", "N");
    item2ParmStr("tel_no");
    item2ParmNvl("email_flag", "N");
    item2ParmStr("email_addr");
    item2ParmStr("reply_date");
    item2ParmStr("reply_time");
    item2ParmStr("audit_remark");
    item2ParmStr("audit_result");
    item2ParmStr("charge_user");
    item2ParmStr("appr_passwd");
    item2ParmStr("user_id");
    item2ParmStr("decs_user");
    item2ParmNum("time_lmt");
    item2ParmNum("dd_lmt");
    item2ParmNum("mm_lmt");
    item2ParmNum("time_pcnt");
    item2ParmNum("dd_pcnt");
    item2ParmNum("mm_pcnt");
    item2ParmNum("dd_cnt_pcnt");
    item2ParmNum("mm_cnt_pcnt");
    item2ParmNum("dd_cnt");
    item2ParmNum("mm_cnt");
    item2ParmNvl("no_callback_flag", "N");
    item2ParmStr("risk_type2");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "rskm0940");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update rsk_credits_vd error !");
    }

    return rc;
  }

  public int printUpdate() {
    msgOK();
    strSql = " update rsk_credits_vd set " + " print_flag ='Y' , "
        + " print_cnt = nvl(print_cnt,0)+1 " + " where seq_no =:kk1";

    setString("kk1", wp.itemStr("seq_no"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_credits_vd error , error : " + this.getMsg());
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = "delete rsk_credits_vd where seq_no =:kk1";
    setString("kk1", seqNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete rsk_credits_vd error !");
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
