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

public class Rskm0930Func extends FuncAction {
  String seqNo = "", lsCardCond = "", lsConsumType1 = "", lsAuditProg = "", lsIdPSeqno = "",
      lsContaRemark = "";
  public String lsSeqno = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      if (setSeqNo() < 0) {
        errmsg("流水編號 編號錯誤 !");
        return;
      }
      seqNo = lsSeqno;
    } else {
      seqNo = wp.itemStr("seq_no");
    }

    if (this.ibDelete) {
      if (this.checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")) == false) {
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
    /*
     * if(wp.item_empty("adj_date1")||wp.item_empty("adj_date2")){ errmsg("異動期間: 不可空白"); return ; }
     * 
     * if(wp.item_num("card_amt2")<=0 || wp.item_num("acno_amt2")<=0){ errmsg("調整後本卡/卡戶額度: 須>0");
     * return ; }
     */

    // -- 往來情形
    if (wp.itemEq("two_card_flag", "Y")) {
      if (checkAcno() == false) {
        errmsg("持卡人  無2個(以上)有效卡帳戶");
        return;
      }
    }

    lsCardCond = wp.itemNvl("card_cond_01", "N") + wp.itemNvl("card_cond_02", "N")
        + wp.itemNvl("card_cond_03", "N") + wp.itemNvl("card_cond_04", "N")
        + wp.itemNvl("card_cond_05", "N") + wp.itemNvl("card_cond_06", "N")
        + wp.itemNvl("card_cond_07", "N");
    log("card_cond : " + lsCardCond);
    log("X:" + lsCardCond.indexOf("Y"));
    if (this.pos(lsCardCond, "Y") < 0) {
      errmsg("基本資料及持卡狀況: 至少要勾選一個");
      return;
    }

    lsConsumType1 = wp.itemNvl("db_cons_type_p", "N") + wp.itemNvl("db_cons_type_d1", "N")
        + wp.itemNvl("db_cons_type_d2", "N") + wp.itemNvl("db_cons_type_t", "N")
        + wp.itemNvl("db_cons_type_h", "N") + wp.itemNvl("db_cons_type_e", "N")
        + wp.itemNvl("db_cons_type_m", "N") + wp.itemNvl("db_cons_type_j", "N")
        + wp.itemNvl("db_cons_type_r", "N") + wp.itemNvl("db_cons_type_l", "N")
        + wp.itemNvl("db_cons_type_i", "N") + wp.itemNvl("db_cons_type_x", "N")
        + wp.itemNvl("db_cons_type_g", "N") + wp.itemNvl("db_cons_type_p1", "N")
        + wp.itemNvl("db_cons_type_p2", "N") + wp.itemNvl("db_cons_type_p3", "N")
        + wp.itemNvl("db_cons_type_m1", "N") + wp.itemNvl("db_cons_type_m2", "N")
        + wp.itemNvl("db_cons_type_p0", "N");

    lsAuditProg = wp.itemNvl("db_audit_prog01", "N") + wp.itemNvl("db_audit_prog02", "N")
        + wp.itemNvl("db_audit_prog03", "N") + wp.itemNvl("db_audit_prog04", "N");

    lsContaRemark = wp.itemStr("conta_remark1") + wp.itemStr("conta_remark2");



  }

  int setSeqNo() {
    int seqNoI = 0;
    String seqNoS = "";
    String sql1 = " select " + " max(seq_no) as ls_seqno " + " from rsk_credits_adj "
        + " where crt_date = ? ";
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
    String sql1 = " select " + " count(*) as ll_cnt " + " from crd_card A , crd_idno B "
        + " where A.id_p_seqno = B.id_p_seqno " + " and A.card_no = ? " + " and B.id_no = ? "
        + " and A.current_code ='0' ";
    sqlSelect(sql1, new Object[] {wp.itemStr("card_no"), wp.itemStr("id_no")});
    if (sqlRowNum < 0 || colNum("ll_cnt") == 0) {
      return false;
    }

    String sql2 = " select " + " id_p_seqno " + " from crd_idno " + " where id_no = ? ";
    sqlSelect(sql2, new Object[] {wp.itemStr("id_no")});
    if (sqlRowNum <= 0)
      return false;

    lsIdPSeqno = colStr("id_p_seqno");

    return true;
  }

  boolean checkAcno() {
    String sql1 = " select " + " count(*) as ll_cnt " + " from act_acno " + " where p_seqno in ( "
        + " select p_seqno from crd_card where id_p_seqno = ? and current_code='0' and acct_type<>'02' "
        + " )";
    sqlSelect(sql1, new Object[] {lsIdPSeqno});
    if (sqlRowNum <= 0 || colNum("ll_cnt") < 2)
      return false;
    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " insert into rsk_credits_adj ( " + " seq_no ," + " tel_user ," + " tel_date ,"
        + " tel_time ," + " apr_flag ," + " apr_user ," + " id_no ," + " id_code ," + " card_no ,"
        + " chi_name ," + " print_flag ," + " print_cnt ," + " card_cond ," + " comp_name ,"
        + " comp_title ," + " area_code ," + " self_flag ," + " per_purch_flag ," + " can_use_amt ,"
        + " purch_item ," + " purch_amt ," + " team_flag ," + " travel_id ," + " cntry_code ,"
        + " travel_issue_flag ," + " fore_flag ," + " team_member ," + " abroad_date1 ,"
        + " abroad_date2 ," + " overpay_adj_flag ," + " adjlmt_ing_flag ," + " fax_flag ,"
        + " over_ibm_flag ," + " overpay_amt ," + " adjlmt_fax_flag ," + " unpad_ibm_flag ,"
        + " adjlmt_amt ," + " purchase_item ," + " reserve_flag ," + " reserve_type ,"
        + " reserve_bank ," + " reserve_acct ," + " reserve_amt ," + " adj2_flag ,"
        + " adj2_overpay_flag ," + " adj2_other_flag ," + " adj2_remark ," + " adj_corp_flag ,"
        + " oth_flag ," + " oth_reason ," + " adj_date1 ," + " adj_date2 ," + " fixadj_flag ,"
        + " fixadj_type ," + " temp_adj_flag ," + " temp_adj_amt ," + " card_amt1 ,"
        + " card_amt1_txt ," + " acno_amt1 ," + " acno_amt1_txt ," + " card_amt2 ,"
        + " card_amt2_txt ," + " acno_amt2 ," + " acno_amt2_txt ," + " acno_add_amt ,"
        + " acno_add_amt_txt ," + " most_adj_amt ," + " two_card_flag ," + " corp_card_flag ,"
        + " rela_flag ," + " last_adj_date ," + " pd_rating ," + " trial_date ," + " risk_group ,"
        + " trial_action ," + " pd_rate ," + " yy_consum_amt ," + " lastyy_consum_amt ,"
        + " bank_card_flag ," + " most_bank_flag ," + " most_oth_flag ," + " most_oth_amt ,"
        + " conta_remark ," + " fast_flag ," + " consum_flag ," + " sms_flag ," + " tel_flag ,"
        + " tel_no ," + " mail_flag ," + " email_addr ," + " reply_date ," + " reply_time ,"
        + " consum_type2 ," + " consum_type1 ," + " audi_bank_num ," + " audi_bank_amt ,"
        + " sign_flag ," + " sign_date ," + " acss_jcic_flag ," + " trial_jcic_flag ,"
        + " audit_remark ," + " audit_result ," + " rej_reason ," + " audit_user ,"
        + " charge_level ," + " charge_user ," + " passwd_flag ," + " appr_passwd ,"
        + " audit_flag ," + " impo_cust_flag ," + " branch_no ," + " decs_level ," + " audit_prog ,"
        + " close_flag ," + " audi_view_flag ," + " unpad_fax_flag ," + " trip_insure ,"
        + " unpad_amt ," + " limit_6mm_flag ," + " sup_acct_flag , " + " no_callback_flag ,"
        + " crt_user ," + " crt_date ," + " mod_user , " + " mod_time , " + " mod_pgm , "
        + " mod_seqno " + " ) values ( " + " :kk1 ," + " :tel_user ," + " :tel_date ,"
        + " :tel_time ," + " :apr_flag ," + " :apr_user ," + " :id_no ," + " '0' ," + " :card_no ,"
        + " :chi_name ," + " :print_flag ," + " :print_cnt ," + " :card_cond ," + " :comp_name ,"
        + " :comp_title ," + " :area_code ," + " :self_flag ," + " :per_purch_flag ,"
        + " :can_use_amt ," + " :purch_item ," + " :purch_amt ," + " :team_flag ," + " :travel_id ,"
        + " :cntry_code ," + " :travel_issue_flag ," + " :fore_flag ," + " :team_member ,"
        + " :abroad_date1 ," + " :abroad_date2 ," + " :overpay_adj_flag ," + " :adjlmt_ing_flag ,"
        + " :fax_flag ," + " :over_ibm_flag ," + " :overpay_amt ," + " :adjlmt_fax_flag ,"
        + " :unpad_ibm_flag ," + " :adjlmt_amt ," + " :purchase_item ," + " :reserve_flag ,"
        + " :reserve_type ," + " :reserve_bank ," + " :reserve_acct ," + " :reserve_amt ,"
        + " :adj2_flag ," + " :adj2_overpay_flag ," + " :adj2_other_flag ," + " :adj2_remark ,"
        + " :adj_corp_flag ," + " :oth_flag ," + " :oth_reason ," + " :adj_date1 ,"
        + " :adj_date2 ," + " :fixadj_flag ," + " :fixadj_type ," + " :temp_adj_flag ,"
        + " :temp_adj_amt ," + " :card_amt1 ," + " :card_amt1_txt ," + " :acno_amt1 ,"
        + " :acno_amt1_txt ," + " :card_amt2 ," + " :card_amt2_txt ," + " :acno_amt2 ,"
        + " :acno_amt2_txt ," + " :acno_add_amt ," + " :acno_add_amt_txt ," + " :most_adj_amt ,"
        + " :two_card_flag ," + " :corp_card_flag ," + " :rela_flag ," + " :last_adj_date ,"
        + " :pd_rating ," + " :trial_date ," + " :risk_group ," + " :trial_action ," + " :pd_rate ,"
        + " :yy_consum_amt ," + " :lastyy_consum_amt ," + " :bank_card_flag ,"
        + " :most_bank_flag ," + " :most_oth_flag ," + " :most_oth_amt ," + " :conta_remark ,"
        + " :fast_flag ," + " :consum_flag ," + " :sms_flag ," + " :tel_flag ," + " :tel_no ,"
        + " :mail_flag ," + " :email_addr ," + " :reply_date ," + " :reply_time ,"
        + " :consum_type2 ," + " :consum_type1 ," + " :audi_bank_num ," + " :audi_bank_amt ,"
        + " :sign_flag ," + " :sign_date ," + " :acss_jcic_flag ," + " :trial_jcic_flag ,"
        + " :audit_remark ," + " :audit_result ," + " :rej_reason ," + " :audit_user ,"
        + " :charge_level ," + " :charge_user ," + " :passwd_flag ," + " :appr_passwd ,"
        + " :audit_flag ," + " :impo_cust_flag ," + " :branch_no ," + " :decs_level ,"
        + " :audit_prog ," + " :close_flag ," + " :audi_view_flag ," + " :unpad_fax_flag ,"
        + " :trip_insure ," + " :unpad_amt ," + " :limit_6mm_flag ," + " :sup_acct_flag ,"
        + " :no_callback_flag ," + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " ) ";
    setString("kk1", seqNo);
    item2ParmStr("tel_user");
    item2ParmStr("tel_date");
    item2ParmStr("tel_time");
    item2ParmNvl("apr_flag", "N");
    item2ParmStr("apr_user");
    item2ParmStr("id_no");
    item2ParmStr("id_code");
    item2ParmStr("card_no");
    item2ParmStr("chi_name");
    item2ParmNvl("print_flag", "N");
    setString("print_cnt", "0");
    setString("card_cond", lsCardCond);
    item2ParmStr("comp_name");
    item2ParmStr("comp_title");
    item2ParmStr("area_code");
    item2ParmNvl("self_flag", "N");
    item2ParmNvl("per_purch_flag", "N");
    item2ParmStr("can_use_amt");
    item2ParmStr("purch_item");
    item2ParmStr("purch_amt");
    item2ParmNvl("team_flag", "N");
    item2ParmStr("travel_id");
    item2ParmStr("cntry_code");
    item2ParmNvl("travel_issue_flag", "N");
    item2ParmNvl("fore_flag", "N");
    item2ParmStr("team_member");
    item2ParmStr("abroad_date1");
    item2ParmStr("abroad_date2");
    item2ParmNvl("overpay_adj_flag", "N");
    item2ParmNvl("adjlmt_ing_flag", "N");
    item2ParmNvl("fax_flag", "N");
    item2ParmNvl("over_ibm_flag", "N");
    item2ParmNum("overpay_amt");
    item2ParmNvl("adjlmt_fax_flag", "N");
    item2ParmNvl("unpad_ibm_flag", "N");
    item2ParmNum("adjlmt_amt");
    item2ParmStr("purchase_item");
    item2ParmNvl("reserve_flag", "N");
    item2ParmStr("reserve_type");
    item2ParmStr("reserve_bank");
    item2ParmStr("reserve_acct");
    item2ParmStr("reserve_amt");
    item2ParmNvl("adj2_flag", "N");
    item2ParmNvl("adj2_overpay_flag", "N");
    item2ParmNvl("adj2_other_flag", "N");
    item2ParmStr("adj2_remark");
    item2ParmNvl("adj_corp_flag", "N");
    item2ParmNvl("oth_flag", "N");
    item2ParmStr("oth_reason");
    item2ParmStr("adj_date1");
    item2ParmStr("adj_date2");
    item2ParmNvl("fixadj_flag", "N");
    item2ParmStr("fixadj_type");
    item2ParmNvl("temp_adj_flag", "N");
    item2ParmNum("temp_adj_amt");
    item2ParmNum("card_amt1");
    item2ParmStr("card_amt1_txt");
    item2ParmNum("acno_amt1");
    item2ParmStr("acno_amt1_txt");
    item2ParmNum("card_amt2");
    item2ParmStr("card_amt2_txt");
    item2ParmNum("acno_amt2");
    item2ParmStr("acno_amt2_txt");
    item2ParmNum("acno_add_amt");
    item2ParmStr("acno_add_amt_txt");
    item2ParmNum("most_adj_amt");
    item2ParmNvl("two_card_flag", "N");
    item2ParmNvl("corp_card_flag", "N");
    item2ParmNvl("rela_flag", "N");
    item2ParmStr("last_adj_date");
    item2ParmStr("pd_rating");
    item2ParmStr("trial_date");
    item2ParmStr("risk_group");
    item2ParmStr("trial_action");
    item2ParmNum("pd_rate");
    item2ParmNum("yy_consum_amt");
    item2ParmNum("lastyy_consum_amt");
    item2ParmNvl("bank_card_flag", "N");
    item2ParmNvl("most_bank_flag", "N");
    item2ParmNvl("most_oth_flag", "N");
    item2ParmStr("most_oth_amt");
    setString("conta_remark", lsContaRemark);
    item2ParmNvl("fast_flag", "N");
    item2ParmNvl("consum_flag", "N");
    item2ParmNvl("sms_flag", "N");
    item2ParmNvl("tel_flag", "N");
    item2ParmStr("tel_no");
    item2ParmNvl("mail_flag", "N");
    item2ParmStr("email_addr");
    item2ParmStr("reply_date");
    item2ParmStr("reply_time");
    item2ParmStr("consum_type2");
    setString("consum_type1", lsConsumType1);
    item2ParmStr("audi_bank_num");
    item2ParmStr("audi_bank_amt");
    item2ParmNvl("sign_flag", "N");
    item2ParmStr("sign_date");
    item2ParmNvl("acss_jcic_flag", "N");
    item2ParmNvl("trial_jcic_flag", "N");
    item2ParmStr("audit_remark");
    item2ParmStr("audit_result");
    item2ParmStr("rej_reason");
    item2ParmStr("audit_user");
    item2ParmStr("charge_level");
    item2ParmStr("charge_user");
    item2ParmNvl("passwd_flag", "N");
    item2ParmStr("appr_passwd");
    item2ParmNvl("audit_flag", "N");
    item2ParmNvl("impo_cust_flag", "N");
    item2ParmStr("branch_no");
    item2ParmStr("decs_level");
    setString("audit_prog", lsAuditProg);
    item2ParmNvl("close_flag", "N");
    item2ParmStr("audi_view_flag");
    item2ParmNvl("unpad_fax_flag", "N");
    item2ParmStr("trip_insure");
    setString("unpad_amt", "0");
    item2ParmNvl("limit_6mm_flag", "N");
    item2ParmNvl("sup_acct_flag", "N");
    item2ParmNvl("no_callback_flag", "N");
    setString("crt_user",wp.loginUser);
    setString("mod_user",wp.loginUser);
    item2ParmStr("mod_pgm");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_credits_adj error , error : " + this.getMsg());
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update rsk_credits_adj set " + " tel_user =:tel_user ," + " tel_date =:tel_date ,"
        + " tel_time =:tel_time ," + " apr_flag =:apr_flag ," + " apr_user =:apr_user ,"
        + " id_no =:id_no ," + " card_no =:card_no ," + " chi_name =:chi_name ,"
        + " print_flag =:print_flag ," + " print_cnt =:print_cnt ," + " card_cond =:card_cond ,"
        + " comp_name =:comp_name ," + " comp_title =:comp_title ," + " area_code =:area_code ,"
        + " self_flag =:self_flag ," + " per_purch_flag =:per_purch_flag ,"
        + " can_use_amt =:can_use_amt ," + " purch_item =:purch_item ," + " purch_amt =:purch_amt ,"
        + " team_flag =:team_flag ," + " travel_id =:travel_id ," + " cntry_code =:cntry_code ,"
        + " travel_issue_flag =:travel_issue_flag ," + " fore_flag =:fore_flag ,"
        + " team_member =:team_member ," + " abroad_date1 =:abroad_date1 ,"
        + " abroad_date2 =:abroad_date2 ," + " overpay_adj_flag =:overpay_adj_flag ,"
        + " adjlmt_ing_flag =:adjlmt_ing_flag ," + " fax_flag =:fax_flag ,"
        + " over_ibm_flag =:over_ibm_flag ," + " overpay_amt =:overpay_amt ,"
        + " adjlmt_fax_flag =:adjlmt_fax_flag ," + " unpad_ibm_flag =:unpad_ibm_flag ,"
        + " adjlmt_amt =:adjlmt_amt ," + " purchase_item =:purchase_item ,"
        + " reserve_flag =:reserve_flag ," + " reserve_type =:reserve_type ,"
        + " reserve_bank =:reserve_bank ," + " reserve_acct =:reserve_acct ,"
        + " reserve_amt =:reserve_amt ," + " adj2_flag =:adj2_flag ,"
        + " adj2_overpay_flag =:adj2_overpay_flag ," + " adj2_other_flag =:adj2_other_flag ,"
        + " adj2_remark =:adj2_remark ," + " adj_corp_flag =:adj_corp_flag ,"
        + " oth_flag =:oth_flag ," + " oth_reason =:oth_reason ," + " adj_date1 =:adj_date1 ,"
        + " adj_date2 =:adj_date2 ," + " fixadj_flag =:fixadj_flag ,"
        + " fixadj_type =:fixadj_type ," + " temp_adj_flag =:temp_adj_flag ,"
        + " temp_adj_amt =:temp_adj_amt ," + " card_amt1 =:card_amt1 ,"
        + " card_amt1_txt =:card_amt1_txt ," + " acno_amt1 =:acno_amt1 ,"
        + " acno_amt1_txt =:acno_amt1_txt ," + " card_amt2 =:card_amt2 ,"
        + " card_amt2_txt =:card_amt2_txt ," + " acno_amt2 =:acno_amt2 ,"
        + " acno_amt2_txt =:acno_amt2_txt ," + " acno_add_amt =:acno_add_amt ,"
        + " acno_add_amt_txt =:acno_add_amt_txt ," + " most_adj_amt =:most_adj_amt ,"
        + " two_card_flag =:two_card_flag ," + " corp_card_flag =:corp_card_flag ,"
        + " rela_flag =:rela_flag ," + " last_adj_date =:last_adj_date ,"
        + " pd_rating =:pd_rating ," + " trial_date =:trial_date ," + " risk_group =:risk_group ,"
        + " trial_action =:trial_action ," + " pd_rate =:pd_rate ,"
        + " yy_consum_amt =:yy_consum_amt ," + " lastyy_consum_amt =:lastyy_consum_amt ,"
        + " bank_card_flag =:bank_card_flag ," + " most_bank_flag =:most_bank_flag ,"
        + " most_oth_flag =:most_oth_flag ," + " most_oth_amt =:most_oth_amt ,"
        + " conta_remark =:conta_remark ," + " fast_flag =:fast_flag ,"
        + " consum_flag =:consum_flag ," + " sms_flag =:sms_flag ," + " tel_flag =:tel_flag ,"
        + " tel_no =:tel_no  ," + " mail_flag =:mail_flag ," + " email_addr =:email_addr ,"
        + " reply_date =:reply_date ," + " reply_time =:reply_time ,"
        + " consum_type2 =:consum_type2 ," + " consum_type1 =:consum_type1 ,"
        + " audi_bank_num =:audi_bank_num ," + " audi_bank_amt =:audi_bank_amt ,"
        + " sign_flag =:sign_flag ," + " sign_date =:sign_date ,"
        + " acss_jcic_flag =:acss_jcic_flag ," + " trial_jcic_flag =:trial_jcic_flag ,"
        + " audit_remark =:audit_remark ," + " audit_result =:audit_result ,"
        + " rej_reason =:rej_reason ," + " audit_user =:audit_user ,"
        + " charge_level =:charge_level ," + " charge_user =:charge_user ,"
        + " passwd_flag =:passwd_flag ," + " appr_passwd =:appr_passwd ,"
        + " audit_flag =:audit_flag ," + " impo_cust_flag =:impo_cust_flag ,"
        + " branch_no =:branch_no ," + " decs_level =:decs_level ," + " audit_prog =:audit_prog ,"
        + " close_flag =:close_flag ," + " audi_view_flag =:audi_view_flag ,"
        + " unpad_fax_flag =:unpad_fax_flag ," + " trip_insure =:trip_insure ,"
        + " unpad_amt =:unpad_amt ," + " limit_6mm_flag =:limit_6mm_flag ,"
        + " sup_acct_flag =:sup_acct_flag , " + " no_callback_flag =:no_callback_flag ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where seq_no =:kk1";

    setString("kk1", seqNo);
    item2ParmStr("tel_user");
    item2ParmStr("tel_date");
    item2ParmStr("tel_time");
    item2ParmNvl("apr_flag", "N");
    item2ParmStr("apr_user");
    item2ParmStr("id_no");
    item2ParmStr("id_code");
    item2ParmStr("card_no");
    item2ParmStr("chi_name");
    item2ParmNvl("print_flag", "N");
    setString("print_cnt", "0");
    setString("card_cond", lsCardCond);
    item2ParmStr("comp_name");
    item2ParmStr("comp_title");
    item2ParmStr("area_code");
    item2ParmNvl("self_flag", "N");
    item2ParmNvl("per_purch_flag", "N");
    item2ParmStr("can_use_amt");
    item2ParmStr("purch_item");
    item2ParmStr("purch_amt");
    item2ParmNvl("team_flag", "N");
    item2ParmStr("travel_id");
    item2ParmStr("cntry_code");
    item2ParmNvl("travel_issue_flag", "N");
    item2ParmNvl("fore_flag", "N");
    item2ParmStr("team_member");
    item2ParmStr("abroad_date1");
    item2ParmStr("abroad_date2");
    item2ParmNvl("overpay_adj_flag", "N");
    item2ParmNvl("adjlmt_ing_flag", "N");
    item2ParmNvl("fax_flag", "N");
    item2ParmNvl("over_ibm_flag", "N");
    item2ParmNum("overpay_amt");
    item2ParmNvl("adjlmt_fax_flag", "N");
    item2ParmNvl("unpad_ibm_flag", "N");
    item2ParmNum("adjlmt_amt");
    item2ParmStr("purchase_item");
    item2ParmNvl("reserve_flag", "N");
    item2ParmStr("reserve_type");
    item2ParmStr("reserve_bank");
    item2ParmStr("reserve_acct");
    item2ParmStr("reserve_amt");
    item2ParmNvl("adj2_flag", "N");
    item2ParmNvl("adj2_overpay_flag", "N");
    item2ParmNvl("adj2_other_flag", "N");
    item2ParmStr("adj2_remark");
    item2ParmNvl("adj_corp_flag", "N");
    item2ParmNvl("oth_flag", "N");
    item2ParmStr("oth_reason");
    item2ParmStr("adj_date1");
    item2ParmStr("adj_date2");
    item2ParmNvl("fixadj_flag", "N");
    item2ParmStr("fixadj_type");
    item2ParmNvl("temp_adj_flag", "N");
    item2ParmNum("temp_adj_amt");
    item2ParmNum("card_amt1");
    item2ParmStr("card_amt1_txt");
    item2ParmNum("acno_amt1");
    item2ParmStr("acno_amt1_txt");
    item2ParmNum("card_amt2");
    item2ParmStr("card_amt2_txt");
    item2ParmNum("acno_amt2");
    item2ParmStr("acno_amt2_txt");
    item2ParmNum("acno_add_amt");
    item2ParmStr("acno_add_amt_txt");
    item2ParmNum("most_adj_amt");
    item2ParmNvl("two_card_flag", "N");
    item2ParmNvl("corp_card_flag", "N");
    item2ParmNvl("rela_flag", "N");
    item2ParmStr("last_adj_date");
    item2ParmStr("pd_rating");
    item2ParmStr("trial_date");
    item2ParmStr("risk_group");
    item2ParmStr("trial_action");
    item2ParmNum("pd_rate");
    item2ParmNum("yy_consum_amt");
    item2ParmNum("lastyy_consum_amt");
    item2ParmNvl("bank_card_flag", "N");
    item2ParmNvl("most_bank_flag", "N");
    item2ParmNvl("most_oth_flag", "N");
    item2ParmStr("most_oth_amt");
    setString("conta_remark", lsContaRemark);
    item2ParmNvl("fast_flag", "N");
    item2ParmNvl("consum_flag", "N");
    item2ParmNvl("sms_flag", "N");
    item2ParmNvl("tel_flag", "N");
    item2ParmStr("tel_no");
    item2ParmNvl("mail_flag", "N");
    item2ParmStr("email_addr");
    item2ParmStr("reply_date");
    item2ParmStr("reply_time");
    item2ParmStr("consum_type2");
    setString("consum_type1", lsConsumType1);
    item2ParmStr("audi_bank_num");
    item2ParmStr("audi_bank_amt");
    item2ParmNvl("sign_flag", "N");
    item2ParmStr("sign_date");
    item2ParmNvl("acss_jcic_flag", "N");
    item2ParmNvl("trial_jcic_flag", "N");
    item2ParmStr("audit_remark");
    item2ParmStr("audit_result");
    item2ParmStr("rej_reason");
    item2ParmStr("audit_user");
    item2ParmStr("charge_level");
    item2ParmStr("charge_user");
    item2ParmNvl("passwd_flag", "N");
    item2ParmStr("appr_passwd");
    item2ParmNvl("audit_flag", "N");
    item2ParmNvl("impo_cust_flag", "N");
    item2ParmStr("branch_no");
    item2ParmStr("decs_level");
    setString("audit_prog", lsAuditProg);
    item2ParmNvl("close_flag", "N");
    item2ParmStr("audi_view_flag");
    item2ParmNvl("unpad_fax_flag", "N");
    item2ParmStr("trip_insure");
    setString("unpad_amt", "0");
    item2ParmNvl("limit_6mm_flag", "N");
    item2ParmNvl("sup_acct_flag", "N");
    item2ParmNvl("no_callback_flag", "N");
    setString("mod_user",wp.loginUser);
    item2ParmStr("mod_pgm");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_credits_adj error , error : " + this.getMsg());
    }

    return rc;
  }

  public int printUpdate() {
    msgOK();
    strSql = " update rsk_credits_adj set " + " print_flag ='Y' , "
        + " print_cnt = uf_nvl(print_cnt,0)+1 " + " where seq_no =:kk1";

    setString("kk1", wp.itemStr("seq_no"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_credits_adj error , error : " + this.getMsg());
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = "delete rsk_credits_adj where seq_no =:kk1";
    setString("kk1", seqNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete rsk_credits_adj error !");
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
