/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-06-21  V1.00.01   JH                  p_xxx >>acno_p_xxx
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change
* 109-04-28  V1.00.03   Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名           
* 110-01-13  V1.00.04   Justin            parameterize sql                                                                           * 
******************************************************************************/
package rskr02;


import ofcapp.BaseAction;

public class Rskq0930 extends BaseAction {
  String lsWhere = "";
  String lsWhere1 = "", lsWhere2 = "";
  String dataKK1 = "", seqNo = "";

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
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
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
        /* 動態查詢 */
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
      case "XLS":
        // -Excel-
        strAction = "XLS";
        // xlsPrint();
        break;
      case "PDF":
        // -PDF-
        strAction = "PDF";
        // pdfPrint();
        break;
      default:
        break;
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("此條件查無資料");
      return;
    }

    getWhere();


    wp.setQueryMode();
    queryRead();

  }

  void getWhere() {
	sqlParm.clear();
    lsWhere = " and 1=1 " ;
    if (wp.itemEmpty("ex_date1") == false) {
    	lsWhere += " and :ex_date1 <= tel_date";
    	setString("ex_date1", wp.itemStr("ex_date1"));
	}
    if (wp.itemEmpty("ex_date2") == false) {
    	lsWhere += " and tel_date <= :ex_date2 ";
    	setString("ex_date2", wp.itemStr("ex_date2"));
	}
    if (wp.itemEmpty("ex_idno") == false) {
    	lsWhere += " and id_no like :ex_idno";
    	setString("ex_idno", wp.itemStr("ex_idno")+"%");
	}
    if (wp.itemEmpty("ex_card_no") == false) {
    	lsWhere += " and card_no like :ex_card_no";
    	setString("ex_card_no", wp.itemStr("ex_card_no")+"%");
	}

    if (wp.itemEq("ex_self_flag", "Y")) {
      lsWhere += " and uf_nvl(self_flag,'N')='Y' ";
    } else if (wp.itemEq("ex_self_flag", "N")) {
      lsWhere += " and uf_nvl(self_flag,'N')='N' ";
    }

    if (wp.itemEq("ex_audi_result", "Y")) {
      lsWhere += " and audit_result <> '' ";
    } else if (wp.itemEq("ex_audi_result", "N")) {
      lsWhere += " and audit_result = '' ";
    }

    lsWhere1 = lsWhere;
    lsWhere2 = lsWhere;

    if (wp.itemEq("ex_temp_adj", "Y")) {
      lsWhere1 += " and uf_nvl(temp_adj_flag,'N')='Y' ";
    } else if (wp.itemEq("ex_temp_adj", "N")) {
      lsWhere1 += " and uf_nvl(temp_adj_flag,'N')='N' ";
    }
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    getWhere();
    wp.sqlCmd = "";
    if (wp.itemEq("ex_vd_flag", "0") || wp.itemEq("ex_vd_flag", "N")) {
      wp.sqlCmd += " select " + " 'N' db_vd_flag ," + " seq_no ," + " tel_date ," + " tel_user ,"
          + " apr_flag ," + " id_no ," + " id_code ," + " card_no ," + " chi_name ,"
          + " print_flag ," + " fast_flag ," + " adj_date1 ," + " adj_date2 ," + " reply_date ,"
          + " reply_time ," + " audit_result " + " from rsk_credits_adj " + " where 1=1 "
          + lsWhere1;
    }
    if (wp.itemEq("ex_vd_flag", "0"))
      wp.sqlCmd += " union all ";

    if (wp.itemEq("ex_vd_flag", "0") || wp.itemEq("ex_vd_flag", "Y")) {
      wp.sqlCmd += " select " + " 'Y' db_vd_flag ," + " seq_no ," + " tel_date ," + " tel_user ,"
          + " apr_flag ," + " id_no ," + " id_code ," + " card_no ," + " chi_name ,"
          + " print_flag ," + " fast_flag ," + " adj_date1 ," + " adj_date2 ," + " reply_date ,"
          + " reply_time ," + " audit_result " + " from rsk_credits_vd " + " where 1=1 "
          + lsWhere2;
    }
    if (wp.itemEq("ex_vd_flag", "0")) {
      wp.pageCountSql =
          " select count(*) from " + " (select distinct hex(rowid) from rsk_credits_adj where 1=1 "
              + lsWhere1 + " union all select distinct hex(rowid) from rsk_credits_vd where 1=1 "
              + lsWhere2 + " ) ";
    } else if (wp.itemEq("ex_vd_flag", "N")) {
      wp.pageCountSql = " select count(*) from "
          + " (select distinct hex(rowid) from rsk_credits_adj where 1=1 " + lsWhere1 + " ) ";
    } else if (wp.itemEq("ex_vd_flag", "Y")) {
      wp.pageCountSql = " select count(*) from " + " ( "
          + " select distinct hex(rowid) from rsk_credits_vd where 1=1 " + lsWhere2 + " ) ";
    }

    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    log("A:" + sqlRowNum);
    queryAfter();
    wp.setListCount(0);
    wp.setPageValue();
  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_idno", wp.colStr(ii, "id_no") + " - " + wp.colStr(ii, "id_code"));
      wp.colSet(ii, "reply_date", commString.mid(wp.colStr(ii, "reply_date"), 0, 2) + "/"
          + commString.mid(wp.colStr(ii, "reply_date"), 2, 2));
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataKK1 = wp.itemStr2("data_k1");
    seqNo = wp.itemStr2("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (eqIgno(dataKK1, "N")) {
      wp.selectSQL = "" + " seq_no ," + " tel_date ," + " tel_time ," + " tel_user ," + " id_no ,"
          + " id_code ," + " card_no ," + " chi_name ," + " print_flag ," + " card_cond ,"
          + " comp_name ," + " comp_title ," + " fast_flag ," + " self_flag ," + " area_code ,"
          + " per_purch_flag ," + " purch_item ," + " can_use_amt ," + " purch_amt ,"
          + " team_flag ," + " fore_flag ," + " travel_id ," + " cntry_code ," + " team_member ,"
          + " abroad_date1 ," + " abroad_date2 ," + " overpay_adj_flag ," + " fax_flag ,"
          + " over_ibm_flag ," + " overpay_amt ," + " reserve_flag ," + " reserve_type ,"
          + " reserve_bank ," + " reserve_acct ," + " reserve_amt ," + " oth_reason ,"
          + " adj_date1 ," + " adj_date2 ," + " fixadj_flag ," + " card_amt1 ," + " card_amt2 ,"
          + " acno_amt1 ," + " acno_amt2 ," + " acno_add_amt ," + " two_card_flag ,"
          + " corp_card_flag ," + " rela_flag ," + " last_adj_date ," + " most_adj_amt ,"
          + " pd_rating ," + " trial_date ," + " risk_group ," + " trial_action ," + " pd_rate ,"
          + " yy_consum_amt ," + " bank_card_flag ," + " most_bank_flag," + " most_oth_flag ,"
          + " conta_remark ," + " sms_flag ," + " tel_flag ," + " tel_no ," + " mail_flag ,"
          + " email_addr ," + " reply_date ," + " reply_time ," + " consum_type1 ,"
          + " consum_type2 ," + " temp_adj_flag ," + " temp_adj_amt ," + " sign_flag ,"
          + " sign_date ," + " acss_jcic_flag ," + " trial_jcic_flag ," + " audit_remark ,"
          + " audit_result ," + " rej_reason ," + " decs_level ," + " charge_level ,"
          + " charge_user ," + " passwd_flag ," + " appr_passwd ," + " impo_cust_flag ,"
          + " branch_no ," + " close_flag ," + " crt_user ," + " crt_date ," + " mod_user ,"
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
          + " no_callback_flag ";
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
      dataReadAfterN();
    } else if (eqIgno(dataKK1, "Y")) {
      wp.selectSQL = "" + " seq_no ," + " tel_date ," + " tel_time ," + " tel_user ,"
          + " apr_flag ," + " apr_user ," + " id_no ," + " id_code ," + " print_flag ,"
          + " comp_name ," + " comp_title ," + " audi_other ," + " audi_oth_note ," + " fast_flag ,"
          + " self_flag ," + " area_code ," + " consum_item ," + " consum_amt ,"
          + " over_time_flag ," + " over_dd_flag ," + " over_mm_flag ," + " abroad_flag ,"
          + " abroad_date1 ," + " abroad_date2 ," + " oth_flag ," + " oth_reason ," + " sms_flag ,"
          + " tel_flag ," + " tel_no ," + " email_flag ," + " email_addr ,"
          + " email_addr as h_mail ," + " reply_date ," + " reply_time ," + " adj_date1 ,"
          + " adj_date2 ," + " adj_item ," + " adj_rate ," + " adj_amt ," + " adj_remark ,"
          + " adj_remark2 ," + " sign_flag ," + " sign_date ," + " audit_remark ,"
          + " audit_result ," + " charge_user ," + " appr_passwd ," + " user_id ," + " decs_user ,"
          + " crt_user ," + " crt_date ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
          + " mod_seqno ," + " card_no ," + " chi_name ," + " card_cond ," + " print_cnt ,"
          + " substrb(card_cond,1,1) db_card_cond01 ," + " substrb(card_cond,2,1) db_card_cond02 ,"
          + " substrb(card_cond,3,1) db_card_cond03 ," + " substrb(card_cond,4,1) db_card_cond04 ,"
          + " audi_item ," + " substrb(audi_item,1,1) db_audi_item01 ,"
          + " substrb(audi_item,2,1) db_audi_item02 ," + " substrb(audi_item,3,1) db_audi_item03 ,"
          + " substrb(audi_item,4,1) db_audi_item04 ," + " substrb(audi_item,5,1) db_audi_item05 ,"
          + " substrb(audi_item,6,1) db_audi_item06 ," + " substrb(audi_item,7,1) db_audi_item07 ,"
          + " substrb(audi_item,8,1) db_audi_item08 ," + " substrb(audi_item,9,1) db_audi_item09 ,"
          + " substrb(audi_item,10,1) db_audi_item10 ," + " consum_amt_note ," + " adj_bef_amt ,"
          + " purch_flag ," + " cntry_code ," + " over_lmt_parm_flag ," + " dd_cnt_flag ,"
          + " mm_cnt_flag ," + " risk_type_flag ,"
          + " rpad(nvl(risk_type_val,'N'),15,'N') as risk_type_val ," + " time_lmt ," + " dd_lmt ,"
          + " mm_lmt ," + " limit_flag ," + " time_pcnt ," + " dd_pcnt ," + " mm_pcnt ,"
          + " dd_cnt_pcnt ," + " mm_cnt_pcnt ," + " purch_item_flag ,"
          + " substrb(risk_type_val,1,1) as db_risk_p ,"
          + " substrb(risk_type_val,2,1) as db_risk_d1 ,"
          + " substrb(risk_type_val,3,1) as db_risk_t ,"
          + " substrb(risk_type_val,4,1) as db_risk_H ,"
          + " substrb(risk_type_val,5,1) as db_risk_E ,"
          + " substrb(risk_type_val,6,1) as db_risk_M ,"
          + " substrb(risk_type_val,7,1) as db_risk_J ,"
          + " substrb(risk_type_val,8,1) as db_risk_R ,"
          + " substrb(risk_type_val,9,1) as db_risk_L ,"
          + " substrb(risk_type_val,10,1) as db_risk_I ,"
          + " substrb(risk_type_val,11,1) as db_risk_X ,"
          + " substrb(risk_type_val,12,1) as db_risk_G ,"
          + " substrb(risk_type_val,13,1) as db_risk_P1 ,"
          + " substrb(risk_type_val,14,1) as db_risk_P2 ,"
          + " substrb(risk_type_val,15,1) as db_risk_P3 ,"
          + " substrb(risk_type_val,16,1) as db_risk_P0 ,"
          + " substrb(risk_type_val,17,1) as db_risk_M1 ,"
          + " substrb(risk_type_val,18,1) as db_risk_M2 ,"
          + " substrb(risk_type_val,19,1) as db_risk_C ," + " dd_cnt ," + " mm_cnt ,"
          + " 'N' as db_user_id_flag ," + " hex(rowid) as rowid ," + " terror_money ,"
          + " purchase_item ," + " purchase_item2 ," + " purchase_item3 ,"
          + " nvl(close_flag,'N') as close_flag , "
          + " time_lmt * time_pcnt / 100 as wk_time_amt , "
          + " dd_lmt * dd_pcnt / 100 as wk_dd_amt , " + " mm_lmt * mm_pcnt / 100 as wk_mm_amt , "
          + " dd_cnt * dd_cnt_pcnt / 100 as wk_dd_cnt , "
          + " mm_cnt * mm_cnt_pcnt / 100 as wk_mm_cnt , "
          + " to_char(mod_time,'yyyymmdd') as mod_date , " + " no_callback_flag , "
          + " risk_type2 ";
      wp.daoTable = "rsk_credits_vd";
      wp.whereStr = "where 1=1" + sqlCol(seqNo, "seq_no");
      pageSelect();
      if (sqlRowNum <= 0) {
        alertErr2("查無資料");
        return;
      }
      wp.colSet("adj_remark0", commString.mid(wp.colStr("adj_remark"), 0, 100));
      wp.colSet("adj_remark1", commString.mid(wp.colStr("adj_remark"), 100, 100));

      wp.colSet("user_no", wp.loginUser);
      userName();
      dataReadAfterY();
    }

  }

  void dataReadAfterN() {
    String lsIdPSeqno = "", lsKkPseqno = "";
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

    String sql12 = " select acno_p_seqno from crd_card where id_p_seqno = ? ";
    sqlSelect(sql12, new Object[] {lsIdPSeqno});
    if (sqlRowNum <= 0)
      return;
    lsKkPseqno = sqlStr("acno_p_seqno");
    wp.colSet("p_seqno", lsKkPseqno);

    String sql13 = " select autopay_acct_no from act_acno where acno_p_seqno = ? ";
    sqlSelect(sql13, new Object[] {lsKkPseqno});
    if (sqlRowNum > 0)
      wp.colSet("autopay_acct_no", sqlStr("autopay_acct_no"));
  }

  void dataReadAfterY() {
    String sql1 = "select " + " B.e_mail_addr " + " from dbc_card A , dbc_idno B "
        + " where A.id_p_seqno = B.id_p_seqno " + " and A.current_code = '0' "
        + " and B.id_no = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("id_no")});

    if (sqlRowNum > 0) {
      wp.colSet("h_mail", sqlStr("e_mail_addr"));
    }

    wp.colSet("wk_dd_cnt", (int) Math.floor(wp.colNum("wk_dd_cnt")));
    wp.colSet("wk_mm_cnt", (int) Math.floor(wp.colNum("wk_mm_cnt")));
  }

  void userName() {
    String sql1 = " select " + " usr_cname " + " from sec_user " + " where usr_id = ? ";

    sqlSelect(sql1, new Object[] {wp.loginUser});

    if (sqlRowNum > 0) {
      wp.colSet("user_name", sqlStr("usr_cname"));
    }

  }


  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
