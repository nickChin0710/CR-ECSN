/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-11  V1.00.01  ryan       program initial                            *
* 														                     *
* 109-04-20  V1.00.02  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package ipsm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Ipsm0020 extends BaseEdit {
  Ipsm0020Func func;
  int i = 0, iiUnit = 0;
  String kkCardNo = "", kkCrtDate = "", kkCrtTime = "";

  CommString commString = new CommString();

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
    } else if (eqIgno(wp.buttonCode, "DATA")) {
      strAction = "DATA";
      itemChanged();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_crt_date1", getSysDate());
  }

  int getWhereStr() {
    wp.whereStr = " where 1=1 ";
    String lsDate1 = wp.itemStr("ex_crt_date1");
    String lsDate2 = wp.itemStr("ex_crt_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔日期-起迄]  輸入錯誤");
      return -1;
    }
    String exCardNo = wp.itemStr("ex_card_no");
    if (empty(exCardNo) == false) {
      if (exCardNo.length() < 12) {
        alertErr("卡號 至少要 12 碼");
        return -1;
      }
      wp.whereStr += " and  card_no like :ex_card_no ";
      setString("ex_card_no", exCardNo + "%");
    }
    if (empty(wp.itemStr("ex_crt_date1")) == false) {
      wp.whereStr += " and  crt_date >= :ex_crt_date1 ";
      setString("ex_crt_date1", wp.itemStr("ex_crt_date1"));
    }
    if (empty(wp.itemStr("ex_crt_date2")) == false) {
      wp.whereStr += " and  crt_date <= :ex_crt_date2 ";
      setString("ex_crt_date2", wp.itemStr("ex_crt_date2"));
    }
    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and  crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }
    exCardNo = wp.itemStr("ex_send_flag");
    switch (exCardNo) {
      case "1":
        wp.whereStr += " and  send_date = '' ";
        break;
      case "2":
        wp.whereStr += " and  send_date <> '' ";
        break;
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

    wp.selectSQL = " hex(rowid) as rowid " + " ,crt_date " + " ,crt_time " + " ,card_no "
        + " ,autoload_flag " + " ,risk_remark " + " ,crt_user " + " ,send_date " + " ,ips_card_no "
        + " ,from_mark "
        + " ,(decode (from_mark,'1','1:線上',decode(from_mark,'2','2:批次',from_mark))) db_from_mark "
        + " ,mod_seqno ";

    wp.daoTable = " ips_autooff_log ";
    wp.whereOrder = " order by crt_date,crt_time";
    if (getWhereStr() != 1) {
      return;
    }
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
    kkCardNo = itemKk("data_k1");
    if (empty(kkCardNo)) {
      kkCardNo = wp.itemStr("card_no");
    }
    kkCrtDate = itemKk("data_k2");
    if (empty(kkCrtDate)) {
      kkCrtDate = wp.itemStr("crt_date");
    }
    kkCrtTime = itemKk("data_k3");
    if (empty(kkCrtTime)) {
      kkCrtTime = wp.itemStr("crt_time");
    }
    String ipsCardNo = itemKk("data_k4");
    if (empty(ipsCardNo)) {
      ipsCardNo = wp.itemStr("ips_card_no");
    }
    wp.selectSQL = " hex(rowid) as rowid " + " ,crt_date " + " ,crt_time " + " ,card_no "
        + " ,autoload_flag " + " ,risk_remark " + " ,crt_user " + " ,send_date " + " ,ips_card_no "
        + " ,from_mark "
        + " ,(decode (from_mark,'1','1:線上',decode(from_mark,'2','2:批次',from_mark))) db_from_mark "
        + " ,mod_seqno ";
    wp.daoTable = " ips_autooff_log ";
    wp.whereStr = " where 1=1 ";
    wp.whereOrder = " fetch first 1 row only ";
    wp.whereStr += " and card_no = :kk_card_no ";
    setString("kk_card_no", kkCardNo);
    if (!empty(kkCrtDate)) {
      wp.whereStr += " and crt_date = :kk_crt_date ";
      setString("kk_crt_date", kkCrtDate);
    }
    if (!empty(kkCrtTime)) {
      wp.whereStr += " and crt_time = :kk_crt_time ";
      setString("kk_crt_time", kkCrtTime);
    }
    pageSelect();
    wfIpsCardSelect(kkCardNo, ipsCardNo);
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ipsm0020Func(wp);

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

  }

  void listWkdata() {
    String autoloadFlag = "";
    for (int ll = 0; ll < wp.selectCnt; ll++) {
      autoloadFlag = wp.colStr(ll, "autoload_flag");
      wp.colSet(ll, "tt_autoload_flag", commString.decode(autoloadFlag, ",Y,N", ",開啟,停用"));

    }
  }

  int wfIpsCardSelect(String cardNo, String s2IpsCard) {
    String lsCardNo = "", lsIpsCard = "", lsCurrCode = "", lsEndDate = "";
    String lsAutoFlag = "";
    lsCardNo = cardNo;
    lsIpsCard = s2IpsCard;
    if (empty(lsCardNo)) {
      lsCardNo = "";
    }
    if (empty(lsIpsCard)) {
      lsIpsCard = "";
    }
    if (empty(lsCardNo) && empty(lsIpsCard)) {
      return 0;
    }
    if (empty(lsIpsCard)) {
      String sqlSelect = "select ips_card_no from "
          + " (select ips_card_no from ips_card where card_no =:ls_card_no "
          + " order by crt_date desc, current_code asc) " + " where 1=1 fetch first 1 row only ";
      setString("ls_card_no", lsCardNo);
      sqlSelect(sqlSelect);
      lsIpsCard = sqlStr("ips_card_no");
      if (sqlRowNum <= 0 || empty(lsIpsCard)) {
        alertErr("查無一卡通卡號");
        return -1;
      }
    }
    String sqlSelect = "select ips_card_no, current_code, new_end_date, autoload_flag "
        + " from ips_card where ips_card_no =:ls_ips_card ";
    setString("ls_ips_card", lsIpsCard);
    sqlSelect(sqlSelect);
    lsIpsCard = sqlStr("ips_card_no");
    lsCurrCode = sqlStr("current_code");
    lsEndDate = sqlStr("new_end_date");
    lsAutoFlag = sqlStr("autoload_flag");
    if (sqlRowNum <= 0) {
      wp.colSet("ips_card_no", "");
      wp.colSet("db_ips_curr_code", "");
      wp.colSet("db_ips_end_date", "");
      alertErr("查無一卡通資料");
      return -1;
    }
    wp.colSet("ips_card_no", lsIpsCard);
    wp.colSet("db_ips_curr_code", lsCurrCode);
    wp.colSet("tt_db_ips_curr_code",
        commString.decode(lsCurrCode, ",0,1,2,3,4,5", ",0.正常,1.申停,2.掛失,3.強制,4.其他停用,5.偽卡"));
    wp.colSet("db_ips_end_date", lsEndDate);
    wp.colSet("autoload_flag", lsAutoFlag);
    // ------------------------------------
    String lsIdno = "", lsCname = "", lsBirdate = "";
    sqlSelect = "select A.id_no, A.chi_name, A.birthday " + "from crd_idno A, crd_card B "
        + "where B.id_p_seqno = A.id_p_seqno " + "and B.card_no =:ls_card_no ";
    setString("ls_card_no", lsCardNo);
    sqlSelect(sqlSelect);
    lsIdno = sqlStr("id_no");
    lsCname = sqlStr("chi_name");
    lsBirdate = sqlStr("birthday");
    if (sqlRowNum > 0) {
      wp.colSet("db_idno", lsIdno);
      wp.colSet("db_idno_name", lsCname);
      wp.colSet("db_bir_date", lsBirdate);
    }

    return 1;
  }

  void itemChanged() {
    String data = wp.itemStr("card_no_kk");
    wfIpsCardSelect(data, "");
  }

  int ofValidation() {
    String lsRowid = "", ss = "", lsCardNo = "", lsIpsCard = "";
    double llCnt = 0;
    String lsAutoFlag = "";
    ss = wp.itemStr("from_mark");
    if (!empty(ss) && !ss.equals("1")) {
      alertErr("非人工指定停用, 不可異動");
      return -1;
    }
    if (!empty(wp.itemStr("send_date"))) {
      alertErr("資料己傳送, 不可異動");
      return -1;
    }
    lsRowid = wp.itemStr("rowid");
    String sqlSelect = "select send_date from ips_autooff_log where hex(rowid) = :ls_rowid ";
    setString("ls_rowid", lsRowid);
    sqlSelect(sqlSelect);
    ss = sqlStr("send_date");
    if (!empty(ss)) {
      alertErr("資料己傳送, 不可異動");
      return -1;
    }
    if (strAction.equals("D")) {
      return 1;
    }
    lsCardNo = wp.itemStr("card_no");
    lsIpsCard = wp.itemStr("ips_card_no");
    if (wfIpsCardSelect(lsCardNo, lsIpsCard) != 1) {
      return -1;
    }
    lsIpsCard = wp.itemStr("ips_card_no");
    if (empty(lsIpsCard)) {
      alertErr("一卡通卡號  不可空白");
      return -1;
    }

    sqlSelect = "select decode(autoload_flag,'','N', autoload_flag) as ls_auto_flag "
        + "from ips_card where ips_card_no = :ls_ips_card ";
    setString("ls_ips_card", lsIpsCard);
    sqlSelect(sqlSelect);
    lsAutoFlag = sqlStr("ls_auto_flag");
    if (sqlRowNum < 0) {
      alertErr("select ips_card error; key=" + lsIpsCard);
      return -1;
    }
    if (!lsAutoFlag.equals("Y")) {
      alertErr("自動加值  已停用, 不須再停用處理");
      return -1;
    }
    if (strAction.equals("A")) {
      sqlSelect = "select count(*) as ll_cnt " + " from ips_autooff_log "
          + " where ips_card_no = :ls_ips_card " + " and send_date ='' ";
      setString("ls_ips_card", lsIpsCard);
      sqlSelect(sqlSelect);
      llCnt = this.toNum(sqlStr("ll_cnt"));
      if (llCnt > 0) {
        alertErr("此卡號有資料未傳送, 不可再新增");
        return -1;
      }
    }

    return 1;
  }
}
