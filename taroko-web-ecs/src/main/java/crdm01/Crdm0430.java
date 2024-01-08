/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-21  V1.00.01  ryan           program initial                             *
* 109-04-28 V1.00.02  YangFang   updated for project coding standard        *
* 109-12-03  V1.00.03  Justin         change ptr_sys_idtab into cca_opp_type_reason
* 109-12-04  V1.00.04  Justin         add a condition statement in dddwSelect
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0430 extends BaseEdit {
  Crdm0430Func func;
  int i = 0;
  String kk1CardNo = "";
  String kk2CrtDate = "";

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
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      wfCardData();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("crt_date", getSysDate());
    wp.colSet("update_date", getSysDate());
  }

  private int getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    int i = 0;
    String lsDate1 = wp.itemStr("ex_crt_date1");
    String lsDate2 = wp.itemStr("ex_crt_date2");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[鍵檔日期-起迄]  輸入錯誤");
      return -1;
    }

    if (empty(wp.itemStr("ex_card_no")) == false) {
      wp.whereStr += " and  a.card_no = :ex_card_no ";
      setString("ex_card_no", wp.itemStr("ex_card_no"));
      i++;
    }
    if (empty(lsDate1) == false) {
      wp.whereStr += " and  a.crt_date >= :ex_crt_date1 ";
      setString("ex_crt_date1", lsDate1);
      i++;
    }
    if (empty(lsDate2) == false) {
      wp.whereStr += " and  a.crt_date <= :ex_crt_date2 ";
      setString("ex_crt_date2", lsDate2);
      i++;
    }
    if(i == 0) {
    	alertErr2("需輸入鍵檔日期 或 卡號 ");
        return -1;
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

    wp.selectSQL = "a.card_no, " + " a.id_p_seqno, " + " a.oppost_date, " + " a.oppost_reason,"
        + " a.current_code, " + " a.to_jcic_date, " + " a.trans_type, " + " a.crt_user, "
        + " a.sup_flag, " + " a.eng_name, " + " a.crt_date, " + " a.mod_time, " + " a.mod_user, "
        + " a.card_type," + " a.m_relation, " + " b.id_p_seqno,  " + " c.id_no ";

    wp.daoTable = "crd_jcic_card as a left join crd_card as b on a.card_no = b.card_no "
        + " left join crd_idno as c on a.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " order by a.card_no";
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    kk1CardNo = itemKk("data_k1");
    if (empty(kk1CardNo)) {
      kk1CardNo = wp.itemStr("kk_card_no");
    }
    if (empty(kk1CardNo)) {
      kk1CardNo = wp.itemStr("card_no");
    }

    kk2CrtDate = itemKk("data_k2");
    if (empty(kk2CrtDate)) {
      kk2CrtDate = wp.itemStr("kk_crt_date");
    }
    if (empty(kk2CrtDate)) {
      kk2CrtDate = wp.itemStr("crt_date");
    }

    wp.selectSQL = "hex(a.rowid) as rowid, a.mod_seqno, " + "a.card_no, " + "a.trans_type, "
        + "a.id_p_seqno , " + "a.corp_no, " + "a.corp_no_code, " + "a.jcic_card_type, "
        + "a.card_type, " + "a.card_since, " + "a.sup_flag,  " + "a.chi_name, " + "a.eng_name, "
        + "a.old_chi_name, " + "a.m_card_no, " + "a.m_relation, " + "a.m_id_p_seqno, "
        + "a.current_code, " + "a.oppost_reason, " + "a.oppost_date, " + "a.credit_lmt,  "
        + "a.credit_flag, " + "a.payment_date, " + "a.risk_amt, " + "a.debit_trans_code, "
        + "a.update_date, " + "a.err_code, " + "a.to_jcic_date, " + "a.jcic_filename, "
        + "a.apr_date, " + "a.apr_user, " + "a.bill_type_flag, " + "a.rela_id, " + "a.crt_user, "
        + "a.crt_date, " + "a.crt_date as old_crt_date, " + "uf_2ymd(a.mod_time) as mod_date, "
        + "a.mod_user, " + "b.id_p_seqno, " + "c.id_no,"
        + "UF_IDNO_ID(a.m_id_p_seqno) as major_id ";

    wp.daoTable =
        "crd_jcic_card as a left join crd_card as b on a.card_no= b.card_no left join crd_idno as c on b.id_p_seqno = c.id_p_seqno";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  a.card_no = :kk1_card_no and a.crt_date =:kk2_crt_date ";
    setString("kk1_card_no", kk1CardNo);
    setString("kk2_crt_date", kk2CrtDate);
    pageSelect();
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      wfCardData();
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {
    func = new Crdm0430Func(wp);
    if (ofValidation() != 1) {
      return;
    }
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {

		try {
			if (wp.respHtml.indexOf("_detl") != -1) {
				  wp.optionKey = wp.colStr("card_type");
			      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "",
			          "where 1=1 order by card_type");
			      wp.optionKey = wp.colStr("m_relation");
			      this.dddwList("dddw_m_relation", "crd_message", "msg_value", "msg",
			          "where 1=1 and msg_type='MAJOR_REL' order by msg_value");
			      wp.optionKey = wp.colStr("current_code");
			      this.dddwList("dddw_current_code", "crd_message", "msg_value", "msg",
			          "where 1=1 and msg_type='JCIC_STOP' order by msg_value");
			      wp.optionKey = wp.colStr("oppost_reason");
			      this.dddwList("dddw_oppost_reason", "cca_opp_type_reason", "opp_status", "opp_status || '.' || opp_remark",
			          "where 1=1  order by opp_status");
			}
		} catch (Exception ex) {
		}
  }

  void wfCardData() {
    if (empty(wp.itemStr("kk_card_no"))) {
      return;
    }
    String lsPSeqno = "", lsAcnoPSeqno = "", lsId = "", lsIdCode = "", lsIdPSeqno = "",
        lsCurrentCode = "";
    String sqlSelect = " select " + "card_no, " + "id_p_seqno , " + "corp_no, " + "corp_no_code, "
        + "group_code, " + "card_type, " + "current_code, " + "sup_flag,  " + "p_seqno, "
        + "acno_p_seqno, " + "eng_name, " + "issue_date as card_since, "
        + "major_card_no as m_card_no, " + "major_relation as m_relation, "
        + "UF_IDNO_ID(major_id_p_seqno) as ls_major_id, " + "oppost_reason, " + "oppost_date, "
        + "apr_date, " + "apr_user " + " from crd_card " + " where card_no = :kk1_card_no ";
    setString("kk1_card_no", wp.itemStr("kk_card_no"));
    sqlSelect(sqlSelect);
    wp.colSet("card_no", wp.itemStr("kk_card_no"));
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此卡號");
      return;
    }
    lsIdPSeqno = sqlStr("id_p_seqno");
    lsPSeqno = sqlStr("p_seqno");
    lsCurrentCode = sqlStr("current_code");
    lsAcnoPSeqno = sqlStr("acno_p_seqno");
    String lsEngName = sqlStr("eng_name");
    String lsOppostDate = sqlStr("oppost_date");
    String lsOppostReason = sqlStr("oppost_reason");
    String lsCardType = sqlStr("card_type");
    String lsGroupCode = sqlStr("group_code");
    String lsIssueDate = sqlStr("card_since");
    String lsSupFlag = sqlStr("sup_flag");
    String lsMajorId = sqlStr("ls_major_id");
    String lsMajorCardNo = sqlStr("m_card_no");
    String lsMajorRelation = sqlStr("m_relation");
    String lsCardSince = sqlStr("card_since");

    sqlSelect = " select id_no ,id_no_code " + " from crd_idno "
        + " where 1=1 and id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", lsIdPSeqno);
    sqlSelect(sqlSelect);
    lsId = sqlStr("id_no");
    lsIdCode = sqlStr("id_no_code");

    sqlSelect = " select rela_id " + " from crd_rela "
        + " where 1=1 and  acno_p_seqno = :ls_acno_p_seqno  and rela_type  = '1' and mod_time in ( "
        + " select max(mod_time "
        + " ) from crd_rela where acno_p_seqno = :ls_acno_p_seqno2 and rela_type  = '1')";
    setString("ls_acno_p_seqno", lsAcnoPSeqno);
    setString("ls_acno_p_seqno2", lsAcnoPSeqno);
    sqlSelect(sqlSelect);
    String lsRelaId = sqlStr("rela_id");

    sqlSelect = "select post_jcic_flag " + " from crd_chg_id "
        + " where old_id_no  = :ls_id   and   old_id_no_code = :ls_id_code ";
    setString("ls_id", lsId);
    setString("ls_id_code", lsIdCode);
    sqlSelect(sqlSelect);
    String lsPostJcicFlag = sqlStr("post_jcic_flag");

    if (lsPostJcicFlag.equals("N")) {
      alertErr("JCIC 變更 ID 已存在, 不可再送舊ID !!");
      return;
    }
    sqlSelect =
        "select bill_type_flag " + " FROM act_jcic_log " + " WHERE p_seqno   = :ls_acct_p_seqno "
            + " and acct_month in ( " + " select max(acct_month) " + " FROM act_jcic_log "
            + " WHERE p_seqno = :ls_acct_p_seqno2 " + " ) fetch first 1 rows only ";
    setString("ls_acct_p_seqno", lsPSeqno);
    setString("ls_acct_p_seqno2", lsPSeqno);
    sqlSelect(sqlSelect);
    String lsBillTypeFlag = sqlStr("bill_type_flag");

    sqlSelect = "select chi_name " + " from crd_idno " + " where id_p_seqno = :ls_id_p_seqno ";
    setString("ls_id_p_seqno", lsIdPSeqno);
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此卡人資料");
      return;
    }
    String lsChiName = sqlStr("chi_name");

    sqlSelect = "select line_of_credit_amt " + " from act_acno "
        + " where acno_p_seqno = :ls_acno_p_seqno ";
    setString("ls_acno_p_seqno", lsAcnoPSeqno);
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此帳戶資料");
      return;
    }
    String liCreditLmt = sqlStr("line_of_credit_amt");

    sqlSelect = " select map_value " + " from crd_message "
        + " where msg_type = 'JCIC_STOP' and msg_value = :ls_current_code ";
    setString("ls_current_code", lsCurrentCode);
    sqlSelect(sqlSelect);
    String lsMapValue = sqlStr("map_value");

    wp.colSet("eng_name", lsEngName);
    wp.colSet("chi_name", lsChiName);
    wp.colSet("old_chi_name", lsChiName);
    wp.colSet("oppost_date", lsOppostDate);
    wp.colSet("oppost_reason", lsOppostReason);
    wp.colSet("card_type", lsCardType);
    wp.colSet("current_code", lsMapValue);
    wp.colSet("id_no", lsId);
    wp.colSet("id_no_code", lsIdCode);
    wp.colSet("sup_flag", lsSupFlag);
    wp.colSet("major_id", lsMajorId);
    wp.colSet("m_card_no", lsMajorCardNo);
    wp.colSet("m_relation", lsMajorRelation);
    wp.colSet("card_since", lsCardSince);
    wp.colSet("credit_lmt", liCreditLmt);
    wp.colSet("bill_type_flag", lsBillTypeFlag);
    wp.colSet("rela_id", lsRelaId);

  }

  int ofValidation() {
    String exCardNo = "";
    String exCrtDate = "";
    if (strAction.equals("A")) {
      exCardNo = wp.itemStr("kk_card_no");
      exCrtDate = wp.itemStr("crt_date");
    }
    if (strAction.equals("U") || strAction.equals("D")) {
      exCardNo = wp.itemStr("card_no");
      exCrtDate = wp.itemStr("crt_date");
    }
    if (empty(exCardNo) == true || empty(exCrtDate) == true) {
      errmsg("卡號,建檔日期不能空白");
      return -1;
    }
    if (!empty(wp.itemStr("to_jcic_date"))) {
      alertErr("此卡號已存在檔內,並且已送JCIC,不可再做新增,修改或刪除");
      return -1;
    }
    if (strAction.equals("D")) {
      return 1;
    }
    if (empty(wp.itemStr("major_id"))) {
      return 1;
    }
    String lsSql = "select id_p_seqno from crd_idno where 1=1 and id_no = :major_id";
    setString("major_id", wp.itemStr("major_id"));
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      alertErr("正卡身份證號有誤");
      return -1;
    }
    return 1;
  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String transType = wp.colStr(ii, "trans_type");;
      String[] cde = new String[] {"A", "C", "D",};
      String[] txt = new String[] {"新增", "異動", "刪除"};
      wp.colSet(ii, "tt_trans_type", commString.decode(transType, cde, txt));
    }
  }
}
