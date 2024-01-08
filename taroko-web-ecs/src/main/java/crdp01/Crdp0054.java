/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-03-01  V1.00.00  ryan       program initial                            *
* 112-03-01  V1.00.00   Ryan      修改getWhereStr 99 --> 98                                                                          *
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Crdp0054 extends BaseProc {
  String mExBatchno = "";
  String mExApplyNo = "";
  String mExApplyId = "";
  String mExCorpNo = "";
  String mExRecno = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      /* 資料處理 */
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      /* 資料讀取 */
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " batchno" + ", recno" + ", apply_no" + ", group_code" + ", card_type"
        + ", unit_code" + ", source_code" + ", online_mark" + ", corp_no" + ", corp_act_flag"
        + ", stmt_cycle" + ", apply_id" + ", sex" + ", birthday" + ", chi_name" + ", eng_name"
        + ", nation" + ", other_cntry_code" + ", resident_no_expire_date" + ", passport_no"
        + ", passport_date" + ", ur_flag" + ", marriage" + ", education" + ", business_code"
        + ", graduation_elementarty" + ", cellar_phone" + ", e_mail_addr" + ", home_area_code1"
        + ", home_tel_no1" + ", home_tel_ext1" + ", office_area_code1" + ", office_tel_no1"
        + ", office_tel_ext1" + ", resident_zip" + ", resident_addr1" + ", resident_addr2"
        + ", resident_addr3" + ", resident_addr4" + ", resident_addr5" + ", mail_zip"
        + ", mail_addr1" + ", mail_addr2" + ", mail_addr3" + ", mail_addr4" + ", mail_addr5"
        + ", company_zip" + ", company_addr1" + ", company_addr2" + ", company_addr3"
        + ", company_addr4" + ", company_addr5" + ", bill_apply_flag" + ", branch" + ", mail_type"
        + ", company_name" + ", job_position" + ", service_year" + ", salary" + ", spouse_id_no"
        + ", spouse_birthday" + ", spouse_name" + ", act_no_l" + ", act_no_l_ind"
        + ", autopay_acct_bank" + ", credit_lmt" + ", revolve_int_rate" + ", revolve_int_rate_year"
        + ", sms_amt" + ", send_pwd_flag" + ", son_card_flag" + ", indiv_crd_lmt" + ", fee_code"
        + ", fee_code_i" + ", inst_flag" + ", roadside_assist_apply" + ", market_agree_base"
        + ", e_news" + ", introduce_emp_no" + ", introduce_id" + ", reg_bank_no"
        + ", emboss_4th_data" + ", promote_emp_no" + ", promote_dept" + ", credit_level_new"
        + ", jcic_score" + ", crt_date, crt_user " + ", apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno";

    wp.daoTable = "crd_emap_tmp";
    // wp.whereOrder=" order by card_item";

    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    mExBatchno = itemKk("data_k1");
    mExApplyNo = itemKk("data_k2");
    mExApplyId = itemKk("data_k3");
    mExCorpNo = itemKk("data_k4");
    mExRecno = itemKk("data_k5");
    wp.selectSQL = "hex(rowid) as rowid" + ", batchno " + ", recno" + ", apply_no" + ", group_code"
        + ", card_type" + ", unit_code" + ", source_code" + ", online_mark" + ", corp_no"
        + ", corp_act_flag" + ", stmt_cycle" + ", apply_id" + ", sex" + ", birthday" + ", chi_name"
        + ", eng_name" + ", nation" + ", other_cntry_code" + ", resident_no_expire_date"
        + ", passport_no" + ", passport_date" + ", ur_flag" + ", marriage" + ", education"
        + ", business_code" + ", graduation_elementarty" + ", cellar_phone" + ", e_mail_addr"
        + ", home_area_code1" + ", home_tel_no1" + ", home_tel_ext1" + ", office_area_code1"
        + ", office_tel_no1" + ", office_tel_ext1" + ", resident_zip" + ", resident_addr1"
        + ", resident_addr2" + ", resident_addr3" + ", resident_addr4" + ", resident_addr5"
        + ", mail_zip" + ", mail_addr1" + ", mail_addr2" + ", mail_addr3" + ", mail_addr4"
        + ", mail_addr5" + ", company_zip" + ", company_addr1" + ", company_addr2"
        + ", company_addr3" + ", company_addr4" + ", company_addr5" + ", bill_apply_flag"
        + ", branch" + ", mail_type" + ", company_name" + ", job_position"
        + ", substring(service_year,1,2) as service_year"
        + ", substring(service_year,3) as service_month" + ", salary" + ", spouse_id_no"
        + ", spouse_birthday" + ", spouse_name" + ", act_no_l" + ", act_no_l_ind"
        + ", autopay_acct_bank" + ", credit_lmt" + ", revolve_int_rate" + ", revolve_int_rate_year"
        + ", sms_amt" + ", send_pwd_flag" + ", son_card_flag" + ", indiv_crd_lmt" + ", fee_code"
        + ", fee_code_i" + ", inst_flag" + ", roadside_assist_apply" + ", market_agree_base"
        + ", e_news" + ", introduce_emp_no" + ", introduce_id" + ", reg_bank_no"
        + ", emboss_4th_data" + ", promote_emp_no" + ", promote_dept" + ", credit_level_new"
        + ", jcic_score" + ", stat_send_internet" + ", crt_date, crt_user "
        + ", apr_date, apr_user " + ", mod_time, mod_user, mod_pgm, mod_seqno";

    wp.daoTable = "crd_emap_tmp";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  batchno = :batchno" + " and apply_no = :apply_no"
        + " and apply_id = :apply_id" + " and corp_no = :corp_no" + " and recno = :recno";
    setString("batchno", mExBatchno);
    setString("apply_no", mExApplyNo);
    setString("apply_id", mExApplyId);
    setString("corp_no", mExCorpNo);
    setString("recno", mExRecno);

    pageSelect();
    if (sqlNotFind()) {
      alertErr(
          "查無資料, batchno = " + mExBatchno + " and apply_no = " + mExApplyNo + " and apply_id = "
              + mExApplyId + " and corp_no = " + mExCorpNo + " and recno = " + mExRecno);
    }

    // 修改日期/人員
    wp.colSet("mod_time", getSysDate());
    wp.colSet("mod_user", wp.loginUser);

    // 推廣人員ID
    if ("".equals(wp.colStr("introduce_emp_no"))) {
      wp.colSet("introduce_emp_no", wp.colStr("introduce_id"));
    }

    // 總繳/個繳註記
    if ("Y".equals(wp.colStr("corp_act_flag"))) {
      wp.colSet("corp_act_flag", "Y:總繳");
    } else if ("N".equals(wp.colStr("corp_act_flag"))) {
      wp.colSet("corp_act_flag", "N:個繳");
    }

    // 性別
    if ("1".equals(wp.colStr("sex"))) {
      wp.colSet("sex", "1:男");
    } else if ("2".equals(wp.colStr("sex"))) {
      wp.colSet("sex", "2:女");
    }

    // 國籍
    if ("1".equals(wp.colStr("nation"))) {
      wp.colSet("nation", "1:本國");
    } else if ("2".equals(wp.colStr("nation"))) {
      wp.colSet("nation", "2:外國");
    }

    // 婚姻
    if ("1".equals(wp.colStr("marriage"))) {
      wp.colSet("marriage", "1:已婚");
    } else if ("2".equals(wp.colStr("marriage"))) {
      wp.colSet("marriage", "2:未婚");
    }
    
    // 拒絕行銷註記
    if ("0".equals(wp.colStr("market_agree_base"))) {
      wp.colSet("tt_market_agree_base", " 0(不同意)");
    } else if ("1".equals(wp.colStr("marriage"))) {
      wp.colSet("tt_market_agree_base", "1(同意共銷)");
    } else if ("2".equals(wp.colStr("marriage"))) {
      wp.colSet("tt_market_agree_base", "2(同意共享)");
    }
  }

  @Override
  public void dataProcess() throws Exception {
    String[] opt = wp.itemBuff("opt");
    String[] recno = wp.itemBuff("recno");

    Crdp0055Func func = new Crdp0055Func(wp);

    for (int rr = 0; rr < recno.length; rr++) {
      if (!checkBoxOptOn(rr, opt)) {
        continue;
      }
      func.varsSet("recno", recno[rr]);

      if (func.updateFunc() < 0) {
        alertErr("update crd_emap_tmp err");
        sqlCommit(0);
        wp.colSet(rr, "ok_flag", "!");
        return;
      }

      sqlCommit(1);
      queryFunc();
      errmsg("處理完成");
    }
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // 教育程度
      wp.optionKey = wp.colStr("education");
      this.dddwList("dddw_education", "crd_message", "msg_value", "msg",
          "where msg_type='EDUCATION' ");
      // 行業別
      wp.optionKey = wp.colStr("business_code");
      this.dddwList("dddw_business_code", "crd_message", "msg_value", "msg",
          "where msg_type='BUS_CODE' ");
      // 寄送分行
      wp.optionKey = wp.colStr("branch");
      this.dddwList("dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
      // 推廣人員/進件分行
      wp.optionKey = wp.colStr("reg_bank_no");
      this.dddwList("dddw_reg_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
      // 信評等級
      wp.optionKey = wp.colStr("credit_level_new");
      this.dddwList("dddw_credit_level_new", "ptr_rcrate", "distinct credit_rating", "where 1=1 ");
    } catch (Exception ex) {
    }
  }

  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_batchno")) == false) {
      wp.whereStr += " and  batchno = :batchno ";
      setString("batchno", wp.itemStr("ex_batchno"));
    }
    if (empty(wp.itemStr("ex_apply_no")) == false) {
      wp.whereStr += " and  apply_no = :apply_no ";
      setString("apply_no", wp.itemStr("ex_apply_no"));
    }
    if (empty(wp.itemStr("ex_apply_id")) == false) {
      wp.whereStr += " and  apply_id = :apply_id ";
      setString("apply_id", wp.itemStr("ex_apply_id"));
    }
    if (empty(wp.itemStr("ex_crt_date")) == false) {
      wp.whereStr += " and  crt_date = :crt_date ";
      setString("crt_date", wp.itemStr("ex_crt_date"));
    }
    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and  crt_user = :crt_user ";
      setString("crt_user", wp.itemStr("ex_crt_user"));
    }
    wp.whereStr += " and  apr_date = ''";
    wp.whereStr += " and  right(batchno,2) = '98' ";
  }

}
