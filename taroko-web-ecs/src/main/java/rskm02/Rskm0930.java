/*****************************************************************************
*                                                                            *

*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-06-19  V1.00.01   JH                  p_xxx >>acno_pxxx                            *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change
* 109-04-27  V1.00.03  Tanwei        updated for project coding standard      *
* 109-07-17  V1.00.04  Zuwei         兆豐 => 合庫      *
* 109-12-24  V1.00.05  Justin          parameterize sql
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名
* 110-01-05  V1.00.06  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *                                                                                      *
******************************************************************************/
package rskm02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskm0930 extends BaseAction implements InfacePdf {
  String seqNo = "";
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // * 查詢功能 */
        strAction = "R";
        dataRead();
        break;
      case "R1":
        // * 查詢功能 */
        strAction = "R1";
        readInitData();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "UPLOAD":
        procFunc();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "PDF":
        // -資料處理-
        strAction = "PDF";
        pdfPrint();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxCardNo();
        break;
      default:
        break;
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskm0930_detl")) {
        wp.optionKey = wp.colStr(0, "charge_user");
        dddwList("dddw_charge_user", "ptr_sys_idtab", "wf_id", "wf_id",
            "where wf_type ='RSKM0930_CHARGE' ");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskm0930_detl")) {
        wp.optionKey = wp.colStr("trial_action");
        ddlbList("dddw_trial_action", wp.colStr("trial_action"), "ecsfunc.DeCodeRsk.trial_action");
      }

    } catch (Exception ex) {
    }
    String lsSql = "";
    try {
      if (wp.colEmpty("id_no") == false) {
        if (wp.colEq("chg", "Y"))
          wp.colSet("card_no", "");
        lsSql = " select A.card_no as db_code , " + " A.card_no as db_desc "
            + " from cca_card_base A , crd_card B "
            + " where A.card_no =B.card_no and B.current_code='0' and B.sup_flag ='0' "
            + " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
            + sqlCol(wp.colStr("id_no"), "id_no") + " ) " + " order by 2 Asc";
        wp.optionKey = wp.colStr("card_no");
        dddwList("dddw_card_no", lsSql);
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("登錄日期: 起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_seq_no"), "seq_no", "like%")
        + sqlCol(wp.itemStr("ex_idno"), "id_no", "like%")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
        + sqlCol(wp.itemStr("ex_tel_user"), "tel_user", "like%")
        + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=");

    if (wp.itemEq("ex_fast_flag", "Y"))
      lsWhere += " and uf_nvl(fast_flag, 'N') = 'Y' ";
    if (!wp.itemEq("ex_close_flag", "0"))
      lsWhere += sqlCol(wp.itemStr("ex_close_flag"), "uf_nvl(close_flag,'N')");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " seq_no ," + " tel_date ," + " tel_time ," + " id_no ," + " id_code ,"
        + " card_no ," + " chi_name ," + " print_flag ," + " close_flag ," + " crt_date ,"
        + " fast_flag, " + " tel_user ";
    wp.daoTable = "rsk_credits_adj";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(0);
    wp.setPageValue();

  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_idno", wp.colStr(ii, "id_no") + "-" + wp.colStr(ii, "id_code"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    seqNo = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(seqNo))
      seqNo = itemkk("seq_no");

    wp.selectSQL = "" + " seq_no ," + " tel_date ," + " tel_time ," + " tel_user ," + " id_no ,"
        + " id_code ," + " card_no ," + " chi_name ," + " print_flag ," + " card_cond ,"
        + " comp_name ," + " comp_title ," + " fast_flag ," + " self_flag ," + " area_code ,"
        + " per_purch_flag ," + " purch_item ," + " can_use_amt ," + " purch_amt ," + " team_flag ,"
        + " fore_flag ," + " travel_id ," + " cntry_code ," + " team_member ," + " abroad_date1 ,"
        + " abroad_date2 ," + " overpay_adj_flag ," + " fax_flag ," + " over_ibm_flag ,"
        + " overpay_amt ," + " reserve_flag ," + " reserve_type ," + " reserve_bank ,"
        + " reserve_acct ," + " reserve_amt ," + " oth_reason ," + " adj_date1 ," + " adj_date2 ,"
        + " fixadj_flag ," + " card_amt1 ," + " card_amt2 ," + " acno_amt1 ," + " acno_amt2 ,"
        + " acno_add_amt ," + " two_card_flag ," + " corp_card_flag ," + " rela_flag ,"
        + " last_adj_date ," + " most_adj_amt ," + " pd_rating ," + " trial_date ,"
        + " risk_group ," + " trial_action ," + " pd_rate ," + " yy_consum_amt ,"
        + " bank_card_flag ," + " most_bank_flag," + " most_oth_flag ," + " conta_remark ,"
        + " sms_flag ," + " tel_flag ," + " tel_no ," + " mail_flag ," + " email_addr ,"
        + " reply_date ," + " reply_time ," + " consum_type1 ," + " consum_type2 ,"
        + " temp_adj_flag ," + " temp_adj_amt ," + " sign_flag ," + " sign_date ,"
        + " acss_jcic_flag ," + " trial_jcic_flag ," + " audit_remark ," + " audit_result ,"
        + " rej_reason ," + " decs_level ," + " charge_level ," + " charge_user ,"
        + " passwd_flag ," + " appr_passwd ," + " impo_cust_flag ," + " branch_no ,"
        + " close_flag ," + " crt_user ," + " crt_date ," + " mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " mod_pgm ," + " mod_seqno ,"
        + " apr_flag ," + " apr_user ," + " oth_flag ," + " fixadj_type ," + " print_cnt ,"
        + " substrb(card_cond,1,1) card_cond_01 ," + " substrb(card_cond,2,1) card_cond_02 ,"
        + " substrb(card_cond,3,1) card_cond_03 ," + " substrb(card_cond,4,1) card_cond_04 ,"
        + " substrb(card_cond,5,1) card_cond_05 ," + " substrb(card_cond,6,1) card_cond_06 ,"
        + " substrb(card_cond,7,1) card_cond_07 ," + " substrb(consum_type1,1,1) db_cons_type_p ,"
        + " substrb(consum_type1,2,1) db_cons_type_d1 ,"
        + " substrb(consum_type1,3,1) db_cons_type_d2 ,"
        + " substrb(consum_type1,4,1) db_cons_type_t ,"
        + " substrb(consum_type1,5,1) db_cons_type_h ,"
        + " substrb(consum_type1,6,1) db_cons_type_e ,"
        + " substrb(consum_type1,7,1) db_cons_type_m ,"
        + " substrb(consum_type1,8,1) db_cons_type_j ,"
        + " substrb(consum_type1,9,1) db_cons_type_r ,"
        + " substrb(consum_type1,10,1) db_cons_type_l ,"
        + " substrb(consum_type1,11,1) db_cons_type_i ,"
        + " substrb(consum_type1,12,1) db_cons_type_x ,"
        + " substrb(consum_type1,13,1) db_cons_type_g ,"
        + " substrb(consum_type1,14,1) db_cons_type_p1 ,"
        + " substrb(consum_type1,15,1) db_cons_type_p2 ,"
        + " substrb(consum_type1,16,1) db_cons_type_p3 ,"
        + " substrb(consum_type1,17,1) db_cons_type_m1 ,"
        + " substrb(consum_type1,18,1) db_cons_type_m2 ,"
        + " substrb(consum_type1,19,1) db_cons_type_p0 ," + " audi_view_flag ," + " audit_prog ,"
        + " substrb(audit_prog,1,1) db_audit_prog01 ,"
        + " substrb(audit_prog,2,1) db_audit_prog02 ,"
        + " substrb(audit_prog,3,1) db_audit_prog03 ,"
        + " substrb(audit_prog,4,1) db_audit_prog04 ," + " card_amt1_txt ," + " card_amt2_txt ,"
        + " acno_amt1_txt ," + " acno_amt2_txt ," + " acno_add_amt_txt ," + " consum_flag ,"
        + " trip_insure ," + " unpad_amt ," + " unpad_fax_flag ," + " unpad_ibm_flag ,"
        + " travel_issue_flag ," + " adjlmt_ing_flag ," + " adjlmt_fax_flag ," + " adjlmt_amt ,"
        + " adj2_flag ," + " adj2_overpay_flag ," + " adj2_other_flag ," + " adj2_remark ,"
        + " adj_corp_flag ," + " lastyy_consum_amt ," + " audit_flag ," + " audit_user ,"
        + " purchase_item ," + " audi_bank_num ," + " audi_bank_amt ," + " most_oth_amt ,"
        + " hex(rowid) as rowid , " + " limit_6mm_flag , " + " sup_acct_flag ,"
        + " no_callback_flag , " + " print_cnt ";
    wp.daoTable = " rsk_credits_adj ";
    wp.whereStr = " where 1=1 " + sqlCol(seqNo, "seq_no");
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.listCount[0] = wp.selectCnt;
    wp.colSet("user_no", wp.loginUser);
    userName();
    dataReadAfter();
  }

  void dataReadAfter() {
    String lsIdPSeqno = "", lsPSeqno = "";
    String sql0 = " select id_p_seqno from crd_idno where id_no = ? ";
    sqlSelect(sql0, new Object[] {wp.colStr("id_no")});

    if (sqlRowNum <= 0)
      return;
    lsIdPSeqno = sqlStr("id_p_seqno");
    // --覆審總分--
    String sql11 = " select " + " C.ecs_jcic_score , A.ecs004 , B.tol_score "
        + " from rsk_trial_list C left join rsk_trial_data_ecs A "
        + " on A.batch_no =C.batch_no and A.id_p_seqno =C.id_p_seqno "
        + " left join rsk_trial_data_jcic B "
        + " on A.batch_no =B.batch_no and A.id_p_seqno =B.id_p_seqno " + " where C.id_p_seqno = ? "
        + " order by C.query_date Desc ";

    sqlSelect(sql11, new Object[] {lsIdPSeqno});

    if (sqlRowNum > 0) {
      if (sqlNum("ecs_jcic_score") == 0) {
        wp.colSet("ecs_jcic_score", sqlNum("ecs004") + sqlNum("tol_score"));
      } else {
        wp.colSet("ecs_jcic_score", sqlNum("ecs_jcic_score"));
      }
    } else {
      wp.colSet("ecs_jcic_score", "");
    }

    String sql12 =
        " select acno_p_seqno, p_seqno from crd_card where id_p_seqno =? and acno_flag<>'Y' ";
    sqlSelect(sql12, new Object[] {lsIdPSeqno});
    if (sqlRowNum <= 0)
      return;
    lsPSeqno = sqlStr("acno_p_seqno");
    wp.colSet("acno_p_seqno", lsPSeqno);

    String sql13 = " select autopay_acct_no from act_acno where p_seqno = ? ";
    String lsPseqno = sqlStr("p_seqno");
    sqlSelect(sql13, new Object[] {lsPseqno});
    if (sqlRowNum > 0)
      wp.colSet("autopay_acct_no", sqlStr("autopay_acct_no"));
  }

  void userName() {
    String sql1 = " select usr_cname " + " from sec_user " + " where usr_id = ? ";

    sqlSelect(sql1, new Object[] {wp.loginUser});

    if (sqlRowNum > 0) {
      wp.colSet("user_name", sqlStr("usr_cname"));
    }

  }

  @Override
  public void saveFunc() throws Exception {
    rskm02.Rskm0930Func func = new rskm02.Rskm0930Func();
    func.setConn(wp);

    if (this.isDelete() && this.checkApproveZz() == false) {
      return;
    }

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      this.saveAfter(false);
      if (this.isAdd()) {
        alertMsg("流水編號:" + func.lsSeqno);
      }
    }

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    if (eqIgno(wp.respHtml, "rskm0930_detl") && (eqIgno(strAction, "new") || empty(strAction))) {
      String sql1 = " select " + " usr_cname " + " from sec_user " + " where usr_id = ? ";

      sqlSelect(sql1, new Object[] {wp.loginUser});

      wp.colSet("tel_user", sqlStr("usr_cname"));
      wp.colSet("tel_date", getSysDate());
      wp.colSet("tel_time", commDate.sysTime());
      wp.colSet("adj_date1", getSysDate());
      wp.colSet("reply_date", getSysDate());
      wp.colSet("sms_flag", "Y");
      if (wp.colEmpty("audit_remark")) {
        wp.colSet("audit_remark", "詳附件");
      }
    }

  }

  public void wfAjaxCardNo() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    // String ls_winid =

    selectData1(wp.itemStr("ax_key"));
    if (rc != 1) {
      wp.addJSON("acno_flag", "");
      wp.addJSON("curr_pd_rating", "");
      wp.addJSON("risk_level", "");
      wp.addJSON("card_amt", "");
      wp.addJSON("acno_amt", "");
      wp.addJSON("company_name", "");
      return;
    }
    wp.addJSON("acno_flag", sqlStr("acno_flag"));
    wp.addJSON("curr_pd_rating", sqlStr("curr_pd_rating"));
    wp.addJSON("risk_level", sqlStr("risk_level"));
    wp.addJSON("card_amt", sqlStr("card_amt"));
    wp.addJSON("acno_amt", sqlStr("acno_amt"));
    wp.addJSON("company_name", sqlStr("company_name"));
  }

  void selectData1(String cardNo) {
    String lsSql = "select " + " A.acno_flag , " + " B.curr_pd_rating , " + " A.corp_p_seqno , "
        + " A.id_p_seqno , " + " A.major_id_p_seqno , " + " B.line_of_credit_amt as card_amt "
        + " from crd_card A join act_acno B on A.acno_p_seqno=B.acno_p_seqno "
        + " where A.card_no = ? ";
    this.sqlSelect(lsSql, new Object[] {cardNo});

    if (sqlRowNum <= 0) {
      alertErr2("此卡號查無資料");
      return;
    }
    String sql2 = "";
    String sql3 = "";
    if (!empty(sqlStr("corp_p_seqno"))) {
      sql2 = " select risk_level from crd_corp_ext where corp_p_seqno = ? ";
      sql3 = " select chi_name as company_name from crd_corp where corp_p_seqno = ? ";
      sqlSelect(sql2, new Object[] {sqlStr("corp_p_seqno")});
      sqlSelect(sql3, new Object[] {sqlStr("corp_p_seqno")});
    } else {
      sql2 = " select risk_level from crd_idno_ext where id_p_seqno = ? ";
      sql3 = " select company_name from crd_idno where id_p_seqno = ? ";
      sqlSelect(sql2, new Object[] {sqlStr("id_p_seqno")});
      sqlSelect(sql3, new Object[] {sqlStr("major_id_p_seqno")});
    }

    String sql4 = " select sum(A.line_of_credit_amt) as acno_amt from act_acno A "
        + " where A.id_p_seqno = ? and A.corp_act_flag <>'Y' "
        + " and exists (select 1 from crd_card B where A.acct_type = B.acct_type "
        + " and A.id_p_seqno = B.id_p_seqno and current_code ='0') ";

    sqlSelect(sql4, new Object[] {sqlStr("major_id_p_seqno")});

    return;
  }


  public void readInitData() {
    String lsIdPSeqno = "", lsLastSixMonth = "";
    double lmAcnoLmt = 0, lmCardLmt = 0;
    String sql1 = " select " + " A.chi_name , " + " A.company_name , " + " A.job_position , "
        + " A.id_p_seqno , " + " uf_nvl(A.student,'N') as student , " + " A.nation , "
        + " uf_nvl(A.asset_value,0) as asset_value , " + " A.id_no , " + " A.e_mail_addr  "
        + " from crd_idno A " + " where A.id_no = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("id_no")});

    if (sqlRowNum <= 0) {
      errmsg("非本行卡友");
      return;
    }

    lsIdPSeqno = sqlStr("id_p_seqno");
    wp.colSet("id_p_seqno", lsIdPSeqno);
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("comp_name", sqlStr("company_name"));
    wp.colSet("comp_title", sqlStr("job_position"));
    wp.colSet("id_no", sqlStr("id_no"));
    wp.colSet("email_addr", sqlStr("e_mail_addr"));

    // if(eq_igno(sql_ss("risk_level"), "L")){
    // wp.col_set("card_cond_07", "N");
    // } else if(eq_igno(sql_ss("risk_level"), "M")){
    // wp.col_set("card_cond_07", "N");
    // } else if(eq_igno(sql_ss("risk_level"), "H")){
    // wp.col_set("card_cond_07", "Y");
    // }

    wp.colSet("card_cond_02", "N");
    if (!eqIgno(sqlStr("student"), "Y") && !eqIgno(sqlStr("nation"), "2")) {
      wp.colSet("card_cond_02", "Y");
    }

    // -- 無保證人 擔保品
    String sql2 = " select " + " count(*) as ll_cnt " + " from crd_rela " + " where id_p_seqno = ? "
        + " and rela_type ='1' ";

    sqlSelect(sql2, new Object[] {lsIdPSeqno});

    wp.colSet("card_cond_03", "Y");

    if (sqlNum("ll_cnt") > 0 || sqlNum("asset_value") > 0) {
      wp.colSet("card_cond_03", "N");
    }

    if (sqlNum("ll_cnt") > 0) {
      wp.colSet("rela_flag", "Y");
    } else {
      wp.colSet("rela_flag", "N");
    }

    // --持卡時間
    lsLastSixMonth = commDate.dateAdd(this.getSysDate(), 0, -6, 0);
    String sql3 = " select " + " count(*) as ll_cnt3 " + " from crd_card " + " where 1=1 "
        + " and id_p_seqno = ? " + " and issue_date < ? ";

    sqlSelect(sql3, new Object[] {lsIdPSeqno, lsLastSixMonth});

    wp.colSet("card_cond_04", "N");
    if (sqlNum("ll_cnt3") > 0) {
      wp.colSet("card_cond_04", "Y");
    }

    // --行員
    wp.colSet("card_cond_05", "N");
    String sql4 = " select " + " count(*) as ll_cnt4 " + " from crd_employee " + " where id = ? "
        + " and status_id in ('1','7') ";

    sqlSelect(sql4, new Object[] {wp.itemStr("id_no")});

    if (sqlNum("ll_cnt4") <= 0) {
      String sql5 = " select " + " sum( "
          + " case when payment_rate1 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
          + " case when payment_rate2 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
          + " case when payment_rate3 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
          + " case when payment_rate4 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
          + " case when payment_rate5 in ('0A','0B','0C','0D','00') then 1 else 0 end + "
          + " case when payment_rate6 in ('0A','0B','0C','0D','00') then 1 else 0 end "
          + " ) as li_0E " + " from act_acno "
          + " where p_seqno in (select p_seqno from crd_card where id_p_seqno = ? ) ";

      sqlSelect(sql5, new Object[] {lsIdPSeqno});

      if (sqlNum("li_0E") >= 3) {
        wp.colSet("card_cond_05", "Y");
      }

    } else {
      wp.colSet("card_cond_05", "Y");
    }

    // --無延滯記錄
    String sql6 = " select " + " sum( "
        + " case when payment_rate1 not in ('0A','0C','00','0E') then 1 else 0 end + "
        + " case when payment_rate2 not in ('0A','0C','00','0E') then 1 else 0 end + "
        + " case when payment_rate3 not in ('0A','0C','00','0E') then 1 else 0 end + "
        + " case when payment_rate4 not in ('0A','0C','00','0E') then 1 else 0 end + "
        + " case when payment_rate5 not in ('0A','0C','00','0E') then 1 else 0 end + "
        + " case when payment_rate6 not in ('0A','0C','00','0E') then 1 else 0 end "
        + " ) as ll_cnt6 " + " from act_acno " + " where id_p_seqno = ? ";
    sqlSelect(sql6, new Object[] {lsIdPSeqno});

    wp.colSet("card_cond_06", "Y");

    if (sqlNum("ll_cnt6") > 0)
      wp.colSet("card_cond_06", "N");

    // //--信用額度
    // String sql7 = " select "
    // + " sum(A.line_of_credit_amt) as lm_acno_lmt "
    // + " from act_acno A "
    // + " where uf_nvl(A.corp_act_flag,'N') <> 'Y' "
    // + " and A.id_p_seqno = ? "
    // + " and exists (select 1 from crd_card B where A.acct_type = B.acct_type "
    // + " and A.id_p_seqno = B.id_p_seqno and B.current_code = '0' ) "
    // ;
    //
    // sqlSelect(sql7,new Object[]{ls_id_p_seqno});
    // lm_acno_lmt = sql_num("lm_acno_lmt");
    // if(sql_nrow<=0) lm_acno_lmt = 0 ;

    // String sql8 = " select "
    // + " sum(line_of_credit_amt) as lm_card_lmt "
    // + " from act_acno "
    // + " where p_seqno in "
    // + " (select p_seqno from crd_card where major_id_p_seqno = ? and current_code ='0' "
    // + " fetch first 1 rows only ) "
    // ;
    //
    // sqlSelect(sql8,new Object[]{ls_id_p_seqno});
    // lm_card_lmt = sql_num("lm_card_lmt");
    // if(sql_nrow<=0) lm_card_lmt = 0 ;
    //
    // wp.col_set("card_amt1", ""+(int)lm_card_lmt);
    // wp.col_set("acno_amt1", ""+(int)lm_acno_lmt);

    // String sql9 = " select "
    // + " count(*) as corp_cnt "
    // + " from act_acno A "
    // + " where uf_nvl(A.corp_act_flag,'N') = 'Y' "
    // + " and A.id_p_seqno = ? "
    // + " and exists ( "
    // + " select 1 from crd_card B where A.acct_type = B.acct_type and A.id_p_seqno = B.id_p_seqno
    // "
    // + " and current_code ='0' ) "
    // ;
    //
    // sqlSelect(sql9,new Object[]{ls_id_p_seqno});
    //
    // if(sql_num("corp_cnt")>0){
    // wp.col_set("acno_amt1", ""+(int)lm_card_lmt);
    // }

    // -近12個月消費金額,近12個月消費金額:-
    wfCardConsume(lsIdPSeqno);

    // --期中覆審--
    String sql10 = " select " + " trial_date , " + " risk_group , " + " action_code "
        + " from rsk_trial_idno " + " where id_p_seqno = ? ";

    sqlSelect(sql10, new Object[] {lsIdPSeqno});

    if (sqlRowNum > 0) {
      wp.colSet("trial_date", sqlStr("trial_date"));
      wp.colSet("risk_group", sqlStr("risk_group"));
      wp.colSet("trial_action", sqlStr("action_code"));
    }

    // --覆審總分--
    String sql11 = " select " + " C.ecs_jcic_score , A.ecs004 , B.tol_score "
        + " from rsk_trial_list C left join rsk_trial_data_ecs A "
        + " on A.batch_no =C.batch_no and A.id_p_seqno =C.id_p_seqno "
        + " left join rsk_trial_data_jcic B "
        + " on A.batch_no =B.batch_no and A.id_p_seqno =B.id_p_seqno " + " where C.id_p_seqno = ? "
        + " order by C.query_date Desc ";

    sqlSelect(sql11, new Object[] {lsIdPSeqno});

    if (sqlRowNum > 0) {
      if (sqlNum("ecs_jcic_score") == 0) {
        wp.colSet("ecs_jcic_score", sqlNum("ecs004") + sqlNum("tol_score"));
      } else {
        wp.colSet("ecs_jcic_score", sqlNum("ecs_jcic_score"));
      }
    } else {
      wp.colSet("ecs_jcic_score", "");
    }

    String sql12 =
        " select p_seqno, acno_p_seqno from crd_card where id_p_seqno =? and acno_flag<>'Y' ";
    sqlSelect(sql12, new Object[] {lsIdPSeqno});
    if (sqlRowNum > 0) {
      wp.colSet("acno_p_seqno", sqlStr("acno_p_seqno"));

      String sql13 = " select autopay_acct_no from act_acno where p_seqno = ? ";
      sqlSelect(sql13, new Object[] {sqlStr("p_seqno")});
      if (sqlRowNum > 0)
        wp.colSet("autopay_acct_no", sqlStr("autopay_acct_no"));
    }

    wp.colSet("chg", "Y");
  }

  void wfCardConsume(String lsIdPSeqno) {
    if (empty(lsIdPSeqno))
      return;

    String lsDate = "", lsYm1 = "", lsYm2 = "";
    double lmLastAmt = 0, lmThisAmt = 0;

    lsDate = commString.mid(commDate.dateAdd(this.getSysDate(), -1, 0, 0), 0, 4);
    lsYm1 = lsDate + "01";
    lsYm2 = lsDate + "02";

    String sql1 = " select " + " sum("
        + " uf_nvl(consume_bl_amt,0)+uf_nvl(consume_ca_amt,0)+uf_nvl(consume_it_amt,0)+"
        + " uf_nvl(consume_ao_amt,0)+uf_nvl(consume_id_amt,0)+uf_nvl(consume_ot_amt,0)"
        + " ) - sum(" + " uf_nvl(sub_bl_amt,0)+uf_nvl(sub_ca_amt,0)+uf_nvl(sub_it_amt,0)+ "
        + " uf_nvl(sub_ao_amt,0)+uf_nvl(sub_id_amt,0)+uf_nvl(sub_ot_amt,0) " + " ) as lm_last_amt "
        + " from mkt_post_consume " + " where card_no in ( "
        + " select card_no from crd_card where major_id_p_seqno = ? " + " ) "
        + " and acct_month >= ? and acct_month <= ? ";

    sqlSelect(sql1, new Object[] {lsIdPSeqno, lsYm1, lsYm2});
    lmLastAmt = sqlNum("lm_last_amt");
    if (eqIgno(sqlStr("lm_last_amt"), null)) {
      lmLastAmt = 0;
    }
    wp.colSet("lastyy_consum_amt", "" + (int) lmLastAmt);

    // --近12個月消費
    lsDate = commDate.dateAdd(getSysDate(), 0, -1, 0);
    lsYm1 = commString.mid(commDate.dateAdd(lsDate, 0, -12, 0), 0, 6);
    lsDate = commString.mid(lsDate, 0, 6);

    String sql2 = " select "
        + " sum(uf_nvl(his_purchase_amt,0))+sum(uf_nvl(his_cash_amt,0)) as lm_amt2 "
        + " from act_anal_sub "
        + " where p_seqno in (select p_seqno from crd_card where major_id_p_seqno =? and acno_flag<>'Y') "
        + " and acct_month >= ? " + " and acct_month <= ? ";

    sqlSelect(sql2, new Object[] {lsIdPSeqno, lsYm1, lsDate});
    lmThisAmt = sqlNum("lm_amt2");
    if (eqIgno(sqlStr("lm_amt2"), null)) {
      lmThisAmt = 0;
    }

    wp.colSet("yy_consum_amt", "" + (int) lmThisAmt);
  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "rskm0930";
    rskm02.Rskm0930Func func = new rskm02.Rskm0930Func();
    func.setConn(wp);

    if (func.printUpdate() != 1) {
      sqlCommit(-1);
      wp.respHtml = "TarokoErrorPDF";
      alertErr2("列印失敗");
      return;
    }

    dataRead();
    setData();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.pageVert = true;
    pdf.excelTemplate = "rskm0930.xlsx";
    pdf.pageCount = 35;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  void setData() {
    if (wp.colEq("apr_flag", "Y")) {
      wp.colSet("ex5E", "■ 檢查人員:" + wp.colStr("apr_user"));
    } else {
      wp.colSet("ex5E", "□ 檢查人員:" + wp.colStr("apr_user"));
    }

    wp.colSet("ex7B", commString.hideIdno(wp.colStr("id_no")));
    wp.colSet("ex7C", commString.hideIdno(wp.colStr("card_no")));
    wp.colSet("ex7E", commString.hideIdnoName(wp.colStr("chi_name")));

    if (wp.colEq("print_flag", "Y")) {
      wp.colSet("ex7F", "■ 已列印 " + wp.colStr("print_cnt"));
    } else {
      wp.colSet("ex7F", "□ 已列印 " + wp.colStr("print_cnt"));
    }

    if (wp.colEq("card_cond_01", "Y")) {
      wp.colSet("ex9B",
          "■ 1.正卡人公司名稱及職稱 ；已變更為公司名稱:" + wp.colStr("comp_name") + "，職稱:" + wp.colStr("comp_title"));
    } else {
      wp.colSet("ex9B",
          "□ 1.正卡人公司名稱及職稱 ；已變更為公司名稱:" + wp.colStr("comp_name") + "，職稱:" + wp.colStr("comp_title"));
    }

    if (wp.colEq("card_cond_02", "Y")) {
      wp.colSet("ex10B", "■ 2.非學生卡、非外國人或外僑");
    } else {
      wp.colSet("ex10B", "□ 2.非學生卡、非外國人或外僑");
    }

    if (wp.colEq("card_cond_05", "Y")) {
      wp.colSet("ex10D", "■ 5.最近6個月繳款記錄達3次(含)以上(合庫國際銀行行員除外)");
    } else {
      wp.colSet("ex10D", "□ 5.最近6個月繳款記錄達3次(含)以上(合庫國際銀行行員除外)");
    }

    if (wp.colEq("card_cond_03", "Y")) {
      wp.colSet("ex11B", "■ 3.信用卡無保證人及擔保品");
    } else {
      wp.colSet("ex11B", "□ 3.信用卡無保證人及擔保品");
    }

    if (wp.colEq("card_cond_06", "Y")) {
      wp.colSet("ex11D", "■ 6.最近6個月(包含本期)無延滯紀錄(包含多額度客戶)");
    } else {
      wp.colSet("ex11D", "□ 6.最近6個月(包含本期)無延滯紀錄(包含多額度客戶)");
    }

    if (wp.colEq("card_cond_04", "Y")) {
      wp.colSet("ex12B", "■ 4.持卡時間至少滿6個月以上");
    } else {
      wp.colSet("ex12B", "□ 4.持卡時間至少滿6個月以上");
    }

    if (wp.colEq("card_cond_07", "Y")) {
      wp.colSet("ex12D", "■ 7.為洗錢高風險等級之卡戶");
    } else {
      wp.colSet("ex12D", "□ 7.為洗錢高風險等級之卡戶");
    }

    if (wp.colEq("self_flag", "Y")) {
      wp.colSet("ex14E", "　　　■ 主動服務");
    } else {
      wp.colSet("ex14E", "　　　□ 主動服務");
    }

    if (wp.colEq("per_purch_flag", "Y")) {
      wp.colSet("ex15B", " ■ 1.預約消費");
    } else {
      wp.colSet("ex15B", " □ 1.預約消費");
    }

    if (wp.colEq("team_flag", "Y")) {
      wp.colSet("ex18B", " ■ 團費(機票)");
    } else {
      wp.colSet("ex18B", " □ 團費(機票)");
    }

    if (wp.colEq("fore_flag", "Y")) {
      wp.colSet("ex19B", " ■ 國外消費");
    } else {
      wp.colSet("ex19B", " □ 國外消費");
    }

    wp.colSet("ex20D", commString.strToYmd(wp.colStr("abroad_date1")) + " -- "
        + commString.strToYmd(wp.colStr("abroad_date2")));

    if (wp.colEq("overpay_adj_flag", "Y")) {
      wp.colSet("ex21B", " ■ 2.未銷帳調整");
    } else {
      wp.colSet("ex21B", " □ 2.未銷帳調整");
    }

    if (wp.colEq("adjlmt_ing_flag", "Y")) {
      wp.colSet("ex22B", " ■ 目前仍在臨調中");
    } else {
      wp.colSet("ex22B", " □ 目前仍在臨調中");
    }

    String ex22C = "";
    ex22C += "(";

    if (wp.colEq("fax_flag", "Y")) {
      ex22C += " ■ FAX收據　　";
    } else {
      ex22C += " □ FAX收據　　";
    }

    if (wp.colEq("over_ibm_flag", "Y")) {
      ex22C += " ■ IBM溢繳　　";
    } else {
      ex22C += " □ IBM溢繳　　";
    }

    ex22C += "金額:" + String.format("%,14.0f", wp.colNum("overpay_amt")) + ")";

    wp.colSet("ex22C", ex22C);

    if (wp.colEq("adjlmt_fax_flag", "Y")) {
      wp.colSet("ex23B", " ■ FAX收據　　");
    } else {
      wp.colSet("ex23B", " □ FAX收據　　");
    }

    if (wp.colEq("unpad_ibm_flag", "Y")) {
      wp.colSet("ex23C", " ■ IBM溢繳　　");
    } else {
      wp.colSet("ex23C", " □ IBM溢繳　　");
    }

    if (wp.colEq("reserve_flag", "Y")) {
      wp.colSet("ex24B", " ■ 3.圈存調整　　");
    } else {
      wp.colSet("ex24B", " □ 3.圈存調整　　");
    }

    wp.colSet("ex24E", commString.hideAcctNo(wp.colStr("reserve_acct")));

    if (wp.colEq("adj2_flag", "Y")) {
      wp.colSet("ex25B", " ■ 4.二段臨調　　");
    } else {
      wp.colSet("ex25B", " □ 4.二段臨調　　");
    }

    if (wp.colEq("adj2_overpay_flag", "Y")) {
      wp.colSet("ex25C", " ■ 未銷(溢繳)已入帳調回原臨調　　");
    } else {
      wp.colSet("ex25C", " □ 未銷(溢繳)已入帳調回原臨調　　");
    }

    if (wp.colEq("adj2_other_flag", "Y")) {
      wp.colSet("ex25D", " ■ 其他:" + wp.colStr("adj2_remark"));
    } else {
      wp.colSet("ex25D", " □ 其他:" + wp.colStr("adj2_remark"));
    }

    if (wp.colEq("adj_corp_flag", "Y")) {
      wp.colSet("ex26B", " ■ 5.商務卡參數調整　　");
    } else {
      wp.colSet("ex26B", " □ 5.商務卡參數調整　　");
    }

    if (wp.colEq("oth_flag", "Y")) {
      wp.colSet("ex27B", " ■ 6.其他:" + wp.colStr("oth_reason"));
    } else {
      wp.colSet("ex27B", " □ 6.其他:" + wp.colStr("oth_reason"));
    }

    wp.colSet("ex29B",
        commString.strToYmd(wp.colStr("adj_date1")) + " －  " + commString.strToYmd(wp.colStr("adj_date2")));

    if (wp.colEq("fixadj_flag", "Y")) {
      wp.colSet("ex29C", " ■ 是否永調");
    } else {
      wp.colSet("ex29C", " □ 是否永調");
    }

    if (wp.colEq("temp_adj_flag", "Y")) {
      wp.colSet("ex29E", " ■ 暫時永調");
    } else {
      wp.colSet("ex29E", " □ 暫時永調");
    }

    String ex34B = "";
    if (wp.colEq("two_card_flag", "Y")) {
      ex34B += " ■ 本行雙額度　";
    } else {
      ex34B += " □ 本行雙額度　";
    }

    if (wp.colEq("corp_card_flag", "Y")) {
      ex34B += " ■ 商務卡　";
    } else {
      ex34B += " □ 商務卡　";
    }

    if (wp.colEq("rela_flag", "Y")) {
      ex34B += " ■ 保證人　";
    } else {
      ex34B += " □ 保證人　";
    }

    wp.colSet("ex34B", ex34B);

    wp.colSet("ex36B", commString.rpad(wp.colStr("pd_rate"), 6, " ") + "%");

    String ex37B = "";

    if (wp.colEq("bank_card_flag", "Y")) {
      ex37B += " ■ 僅持本行卡　";
    } else {
      ex37B += " □ 僅持本行卡　";
    }

    if (wp.colEq("most_bank_flag", "Y")) {
      ex37B += " ■ 本行額度最高　";
    } else {
      ex37B += " □ 本行額度最高　";
    }

    if (wp.colEq("most_oth_flag", "Y")) {
      ex37B += " ■ 他行額度最高　";
    } else {
      ex37B += " □ 他行額度最高　";
    }

    wp.colSet("ex37B", ex37B);

    String ex37E = "";
    if (wp.colEq("limit_6mm_flag", "Y")) {
      ex37E += " ■ 近半年無永調紀錄　";
    } else {
      ex37E += " □ 近半年無永調紀錄　";
    }

    if (wp.colEq("sup_acct_flag", "Y")) {
      ex37E += " ■ 附歸戶　";
    } else {
      ex37E += " □ 附歸戶　";
    }

    wp.colSet("ex37E", ex37E);

    String ex41C = "";

    if (wp.colEq("fast_flag", "Y")) {
      ex41C += " ■ 急件";
    } else {
      ex41C += " □ 急件";
    }

    if (wp.colEq("no_callback_flag", "Y")) {
      ex41C += " ■ 不須回電";
    } else {
      ex41C += " □ 不須回電";
    }

    wp.colSet("ex41C", ex41C);

    String ex42B = "";

    if (wp.colEq("sms_flag", "Y")) {
      ex42B += " ■ 簡訊　";
    } else {
      ex42B += " □ 簡訊　";
    }

    if (wp.colEq("tel_flag", "Y")) {
      ex42B += " ■ 回電　";
    } else {
      ex42B += " □ 回電　";
    }

    ex42B += " 電話:" + commString.hideTelno(wp.colStr("tel_no"));

    wp.colSet("ex42B", ex42B);

    String ex42E = "";

    if (wp.colEq("db_cons_type_p", "Y")) {
      ex42E += " ■ P ";
    } else {
      ex42E += " □ P ";
    }

    if (wp.colEq("db_cons_type_d1", "Y")) {
      ex42E += " ■ D1";
    } else {
      ex42E += " □ D1";
    }

    if (wp.colEq("db_cons_type_t", "Y")) {
      ex42E += " ■ T";
    } else {
      ex42E += " □ T";
    }

    if (wp.colEq("db_cons_type_h", "Y")) {
      ex42E += " ■ H";
    } else {
      ex42E += " □ H";
    }

    if (wp.colEq("db_cons_type_e", "Y")) {
      ex42E += " ■ E ";
    } else {
      ex42E += " □ E ";
    }

    if (wp.colEq("db_cons_type_m", "Y")) {
      ex42E += " ■ M ";
    } else {
      ex42E += " □ M ";
    }

    if (wp.colEq("db_cons_type_j", "Y")) {
      ex42E += " ■ J ";
    } else {
      ex42E += " □ J ";
    }

    if (wp.colEq("db_cons_type_r", "Y")) {
      ex42E += " ■ R";
    } else {
      ex42E += " □ R";
    }

    if (wp.colEq("db_cons_type_p3", "Y")) {
      ex42E += " ■ P3";
    } else {
      ex42E += " □ P3";
    }

    /*
     * if(wp.col_eq("db_cons_type_m1", "Y")){ ex42E += " ■ M1"; } else { ex42E += " □ M1"; }
     */
    wp.colSet("ex42E", ex42E);

    String ex43B = "";

    if (wp.colEq("mail_flag", "Y")) {
      ex43B += " ■ Mail　";
    } else {
      ex43B += " □ Mail　";
    }

    ex43B += " 電子信箱:" + commString.hideEmail(wp.colStr("email_addr"));
    wp.colSet("ex43B", ex43B);

    String ex43E = "";

    if (wp.colEq("db_cons_type_m1", "Y")) {
      ex43E += " ■ M1";
    } else {
      ex43E += " □ M1";
    }

    if (wp.colEq("db_cons_type_m2", "Y")) {
      ex43E += " ■ M2";
    } else {
      ex43E += " □ M2";
    }

    if (wp.colEq("db_cons_type_l", "Y")) {
      ex43E += " ■ L";
    } else {
      ex43E += " □ L";
    }

    if (wp.colEq("db_cons_type_i", "Y")) {
      ex43E += " ■ I";
    } else {
      ex43E += " □ I";
    }

    if (wp.colEq("db_cons_type_x", "Y")) {
      ex43E += " ■ X ";
    } else {
      ex43E += " □ X ";
    }

    if (wp.colEq("db_cons_type_g", "Y")) {
      ex43E += " ■ G ";
    } else {
      ex43E += " □ G ";
    }

    if (wp.colEq("db_cons_type_p0", "Y")) {
      ex43E += " ■ P0";
    } else {
      ex43E += " □ P0";
    }

    if (wp.colEq("db_cons_type_p1", "Y")) {
      ex43E += " ■ P1";
    } else {
      ex43E += " □ P1";
    }


    wp.colSet("ex43E", ex43E);
    String ex44B = "";
    ex44B = "預計回覆日期:" + commString.strToYmd(wp.colStr("reply_date")) + " 時間:";
    if (!empty(commString.mid(wp.colStr("reply_time"), 0, 2))) {
      ex44B += commString.mid(wp.colStr("reply_time"), 0, 2);
    }
    if (!empty(commString.mid(wp.colStr("reply_time"), 2, 2))) {
      ex44B += ":" + commString.mid(wp.colStr("reply_time"), 2, 2);
    }
    if (!empty(commString.mid(wp.colStr("reply_time"), 4, 2))) {
      ex44B += ":" + commString.mid(wp.colStr("reply_time"), 4, 2);
    }
    wp.colSet("ex44B", ex44B);

    if (wp.colEq("db_audit_prog02", "Y")) {
      wp.colSet("ex47B", " ■ 近半年費內容是否異常(如集中消費)之查核");
    } else {
      wp.colSet("ex47B", " □ 近半年費內容是否異常(如集中消費)之查核");
    }

    if (wp.colEq("db_audit_prog04", "Y")) {
      wp.colSet("ex48B", " ■ 持卡家數逾6家以上或總信用卡額度逾200萬以上，");
    } else {
      wp.colSet("ex48B", " □ 持卡家數逾6家以上或總信用卡額度逾200萬以上，");
    }

    if (wp.colEq("sign_flag", "Y")) {
      wp.colSet("ex50B", " ■ 調閱原簽　" + commString.strToYmd(wp.colStr("sign_date")));
    } else {
      wp.colSet("ex50B", " □ 調閱原簽　" + commString.strToYmd(wp.colStr("sign_date")));
    }

    String ex50C = "";

    if (wp.colEq("acss_jcic_flag", "Y")) {
      ex50C += " ■ 調閱JCIC　";
    } else {
      ex50C += " □ 調閱JCIC　";
    }

    if (wp.colEq("trial_jcic_flag", "Y")) {
      ex50C += " ■ 覆審JCIC　";
    } else {
      ex50C += " □ 覆審JCIC　";
    }

    wp.colSet("ex50C", ex50C);
    // ●○

    if (wp.colEq("charge_level", "1")) {
      wp.colSet("ex54B", "●　甲級　○　乙級");
    } else if (wp.colEq("charge_level", "2")) {
      wp.colSet("ex54B", "○　甲級　●　乙級");
    } else {
      wp.colSet("ex54B", "○　甲級　○　乙級");
    }

    if (wp.colEq("passwd_flag", "Y")) {
      wp.colSet("ex54D", " ■ 放行密碼:" + wp.colStr("appr_passwd"));
    } else {
      wp.colSet("ex54D", " □ 放行密碼:" + wp.colStr("appr_passwd"));
    }

    if (wp.colEq("audit_flag", "Y")) {
      wp.colSet("ex54E", " ■ 經辦:" + wp.colStr("audit_user") + "　核判主管: __________");
    } else {
      wp.colSet("ex54E", " □ 經辦:" + wp.colStr("audit_user") + "　核判主管: __________");
    }

    if (wp.colEq("impo_cust_flag", "Y")) {
      wp.colSet("ex55B", " ■ 重要客戶　　　" + wp.colStr("branch_no"));
    } else {
      wp.colSet("ex55B", " □ 重要客戶　　　" + wp.colStr("branch_no"));
    }

    String ex55E = "";

    if (wp.colEq("db_audit_prog01", "Y")) {
      ex55E += "　 ■ 處長";
    } else {
      ex55E += "　 □ 處長";
    }

    if (wp.colEq("db_audit_prog02", "Y")) {
      ex55E += " ■ 分行";
    } else {
      ex55E += " □ 分行";
    }

    if (wp.colEq("db_audit_prog03", "Y")) {
      ex55E += " ■ 營運中心";
    } else {
      ex55E += " □ 營運中心";
    }

    if (wp.colEq("db_audit_prog04", "Y")) {
      ex55E += " ■ 總行";
    } else {
      ex55E += " □ 總行";
    }

    if (wp.colEq("close_flag", "Y")) {
      ex55E += " ■ 結案";
    } else {
      ex55E += " □ 結案";
    }

    wp.colSet("ex55E", ex55E);

    // --
    if (wp.colEmpty("audi_bank_amt")) {
      wp.colSet("audi_bank_amt", "　");
    }
    if (wp.colEmpty("audi_bank_num")) {
      wp.colSet("audi_bank_num", "　");
    }

  }

}
