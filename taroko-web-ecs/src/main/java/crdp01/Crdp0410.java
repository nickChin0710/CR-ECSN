/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-11  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-03  V1.00.02  Justin         change ptr_sys_idtab into cca_opp_type_reason
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Crdp0410 extends BaseProc {
  Crdp0410Func func;
  int rr = -1;
  String msg = "";
  String cardNo = "", crtDate = "";
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
  public void initPage() {
    wp.colSet("ex_crt_dat1", wp.sysDate);
    wp.colSet("ex_crt_dat2", wp.sysDate);
  }

  @Override
  public void dddwSelect() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      try {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("card_type");
        dddwList("dddw_card_type", "ptr_card_type", "card_type", "name", "where 1=1");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("m_relation");
        dddwList("dddw_major_rel", "crd_message", "msg_value", "msg",
            "where 1=1 and msg_type = 'MAJOR_REL'");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("current_code");
        dddwList("dddw_jcic_stop", "crd_message", "msg_value", "msg",
            "where 1=1 and msg_type = 'JCIC_STOP'");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("oppost_reason");
        dddwList("dddw_crd_oppost", "cca_opp_type_reason", "opp_status", "opp_remark",
            "where 1=1 order by opp_status ");

      } catch (Exception ex) {
      }
    }
  }

  // for query use only
  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_crt_dat1");
    String lsDate2 = wp.itemStr("ex_crt_dat2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[起迄日期-起迄]  輸入錯誤");
      return -1;
    }
    wp.whereStr = " where 1=1 and a.to_jcic_date ='' and a.apr_user ='' and a.apr_date = ''";

    if (empty(wp.itemStr("ex_crt_dat1")) == false) {
      wp.whereStr += " and a.crt_date >= :ex_crt_dat1 ";
      setString("ex_crt_dat1", wp.itemStr("ex_crt_dat1"));
    }
    if (empty(wp.itemStr("ex_crt_dat2")) == false) {
      wp.whereStr += " and a.crt_date <= :ex_crt_dat2 ";
      setString("ex_crt_dat2", wp.itemStr("ex_crt_dat2"));
    }
    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and a.crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(a.rowid) as rowid, " + " a.crt_date, " + " a.update_date, " + " a.card_no, "
        + " a.trans_type, " + " a.chi_name, " + " a.crt_user, " + " a.mod_seqno, "
        + " b.id_no||'_'||b.id_no_code as wk_id ";
    wp.daoTable = " crd_jcic_card as a left join crd_idno as b on a.id_p_seqno = b.id_p_seqno ";

    wp.whereOrder = " order by a.crt_date,a.card_no ";
    if (getWhereStr() != 1)
      return;
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

    // wp.col_set("exCnt", int_2Str(wp.selectCnt));
    listWkdata();

  }

  void listWkdata() throws Exception {
    String transType = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {

      transType = wp.colStr(ii, "trans_type");
      wp.colSet(ii, "tt_trans_type", commString.decode(transType, ",A,C,D", ",A:新增,C:修改,D:刪除"));

    }
  }

  void listWkdata2() {

    if (empty(wp.colStr("id_no"))) {
      wp.colSet("id_no", "");
    }
    if (!wp.colStr("db_id_no").equals(wp.colStr("id_no"))) {
      wp.colSet("id_no_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_card_type").equals(wp.colStr("card_type"))) {
      wp.colSet("card_type_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_card_since").equals(wp.colStr("card_since"))) {
      wp.colSet("card_since_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_sup_flag").equals(wp.colStr("sup_flag"))) {
      wp.colSet("sup_flag_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_chi_name").equals(wp.colStr("chi_name"))) {
      wp.colSet("chi_name_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_eng_name").equals(wp.colStr("eng_name"))) {
      wp.colSet("eng_name_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_major_card_no").equals(wp.colStr("m_card_no"))) {
      wp.colSet("m_card_no_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_major_relation").equals(wp.colStr("m_relation"))) {
      wp.colSet("m_relation_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_major_id_p_seqno").equals(wp.colStr("m_id_p_seqno"))) {
      wp.colSet("major_id_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_oppost_reason").equals(wp.colStr("oppost_reason"))) {
      wp.colSet("oppost_reason_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_oppost_date").equals(wp.colStr("oppost_date"))) {
      wp.colSet("oppost_date_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_credit_lmt").equals(wp.colStr("credit_lmt"))) {
      wp.colSet("credit_lmt_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_current_code").equals(wp.colStr("current_code"))) {
      wp.colSet("current_code_color", "style=\"background-color:#00DDDD\"");
    }
  }


  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(itemKk("data_k1")) == false) {
      cardNo = itemKk("data_k1");
    }

    if (empty(itemKk("data_k2")) == false) {
      crtDate = itemKk("data_k2");
    }

    wp.selectSQL = "hex(a.rowid) as rowid2, a.mod_seqno, " + " a.card_no, " + " a.crt_date, "
        + " a.trans_type, " + " b.id_no as major_id, " + " a.m_id_p_seqno, "
        + " UF_IDNO_ID(a.id_p_seqno) as id_no, " + " a.chi_name, " + " a.eng_name, "
        + " a.jcic_card_type, " + " a.card_type, " + " a.sup_flag, " + " a.card_since, "
        + " a.m_card_no, " + " a.m_id_p_seqno, " + " a.m_relation, " + " a.current_code, "
        + " a.credit_lmt, " + " a.credit_flag, " + " a.oppost_date, " + " a.oppost_reason, "
        + " a.payment_date, " + " a.risk_amt, " + " a.debit_trans_code, " + " a.update_date, "
        + " a.bill_type_flag, " + " a.rela_id, " + " a.crt_user, " + " a.to_jcic_date, "
        + " UF_IDNO_ID(c.id_p_seqno) as db_id_no," + " c.card_type as db_card_type, "
        + " c.issue_date as db_card_since, " + " c.sup_flag as db_sup_flag, "
        + " b.chi_name as db_chi_name, " + " c.eng_name as db_eng_name, "
        + " c.major_card_no as db_major_card_no, " + " c.major_relation as db_major_relation, "
        + " c.major_id_p_seqno as db_major_id_p_seqno, " + " c.oppost_reason as db_oppost_reason, "
        + " c.oppost_date as db_oppost_date, " + " d.line_of_credit_amt as db_credit_lmt, "
        + " c.current_code as db_current_code ";
    wp.daoTable = " crd_jcic_card as a left join crd_idno as b on a.m_id_p_seqno = b.id_p_seqno "
        + " left join crd_card as c on  a.card_no = c.card_no , " + " act_acno as d, "
        + " crd_message as e ";

    wp.whereStr = " where 1=1 " + " and ( " + " ( c.id_p_seqno = b.id_p_seqno ) "
        + " and ( c.acno_p_seqno = d.acno_p_seqno ) " + " and ( c.current_code = e.msg_value ) "
        + " and ( msg_type='JCIC_STOP')" + " ) ";
    if (empty(cardNo) == false) {
      wp.whereStr += " and  a.card_no = :card_no ";
      setString("card_no", cardNo);
    }
    if (empty(crtDate) == false) {
      wp.whereStr += " and  a.crt_date = :crt_date ";
      setString("crt_date", crtDate);
    }
    pageSelect();
    listWkdata2();
    if (sqlNotFind()) {

      alertErr("此卡號之明細資料不存在");
      return;
    }
  }

  @Override
  public void dataProcess() throws Exception {
    // -check approve-
    /*
     * if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) { return; }
     */
    func = new Crdp0410Func(wp);

    String[] aaCrtDate = wp.itemBuff("crt_date");
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] opt = wp.itemBuff("opt");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");

    wp.listCount[0] = aaCardNo.length;

    // -update-
    for (rr = 0; rr < aaCardNo.length; rr++) {
      if (!checkBoxOptOn(rr, opt)) {
        continue;
      }
      func.varsSet("aa_card_no", aaCardNo[rr]);
      func.varsSet("aa_crt_date", aaCrtDate[rr]);
      func.varsSet("aa_mod_seqno", aaModSeqno[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc != 1) {
        wp.colSet(rr, "ok_flag", "!");
        ilErr++;
        continue;
      }
      wp.colSet(rr, "ok_flag", "V");
      ilOk++;
    }
    // queryFunc();
    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
