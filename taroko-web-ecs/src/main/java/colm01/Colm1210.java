/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109-05-06  V1.00.00  Aoyulan       updated for project coding standard     *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm1210 extends BaseEdit {
  CommString commString = new CommString();
  Colm1210Func func;

  String lgdSeqno = "";
  String idCorpNo = "";
  int ilOk = 0;
  int ilErr = 0;
  int ilYet = 0;
  String mProgName = "colm1210";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      // dataProcess();
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
    } else if (eqIgno(wp.buttonCode, "G")) {
      /* Item changed */
      // wp.item_set("id_corp_no", "E222682370");
      wfItemchanged();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      // if (is_action.equals("new") || empty(is_action)) {
      String lsLgdType = "F";
      String lsFromType = "1";
      String lsLgdReason = "B2";

      wp.colSet("lgd_type", lsLgdType);
      wp.colSet("from_type", lsFromType);
      wp.colSet("lgd_reason", lsLgdReason);
      wp.colSet("tt_lgd_type", commString.decode(lsLgdType, ",F", ",F.個人信用卡"));
      wp.colSet("tt_from_type", commString.decode(lsFromType, ",1,2", ",1.人工指定,2.批次處理"));
      String[] cde = new String[] {"A", "B1", "B2"};
      String[] txt = new String[] {"A.本息延滯90日", "B1.申請前置協商,調解,更生清算", "B2.發生重大違約強制停用"};
      wp.colSet("tt_lgd_reason", commString.decode(lsLgdReason, cde, txt));
      wp.itemSet("lgd_type", lsLgdType);
      wp.itemSet("from_type", lsFromType);
      wp.itemSet("lgd_reason", lsLgdReason);
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("exCrtUser");
      dddwList("SecUserIDNameList", "sec_user", "usr_id", "usr_id||' ['||usr_cname||']'",
          "where usr_type = '4' order by usr_id");
    } catch (Exception ex) {
    }
  }

  boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exDateS");
    String lsDate2 = wp.itemStr("exDateE");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔日期-起迄]  輸入錯誤");
      return false;
    }
    if (empty(wp.itemStr("exDateS")) && empty(wp.itemStr("exDateE"))
        && empty(wp.itemStr("exIdCorp")) && empty(wp.itemStr("exSeqNo"))) {
      alertErr2("至少須輸入一個查詢條件");
      return false;
    }
    // if ((empty(wp.item_ss("exSeqNo"))==false) && (wp.item_ss("exSeqNo").length()<6)) {
    // err_alert("[LGD案件號碼]輸入至少6碼!");
    // return false;
    // }
    if ((empty(wp.itemStr("exIdCorp")) == false) && (wp.itemStr("exIdCorp").length() < 6)) {
      alertErr2("[身分證ID/統編]輸入至少6碼!");
      return false;
    }

    wp.whereStr = "where 1=1 ";

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and crt_date >= :crt_dates ";
      setString("crt_dates", wp.itemStr("exDateS"));
    }
    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and crt_date <= :crt_datee ";
      setString("crt_datee", wp.itemStr("exDateE"));
    }
    if (empty(wp.itemStr("exIdCorp")) == false) {
      wp.whereStr += " and id_corp_no like :id_corp_no ";
      setString("id_corp_no", wp.itemStr("exIdCorp") + "%");
    }
    if (empty(wp.itemStr("exSeqNo")) == false) {
      wp.whereStr += " and lgd_seqno like :lgd_seqno ";
      setString("lgd_seqno", wp.itemStr("exSeqNo") + "%");
    }
    if (wp.itemStr("exFromType").equals("1")) {
      wp.whereStr += " and from_type = '1' ";
    } else if (wp.itemStr("exFromType").equals("2")) {
      wp.whereStr += " and from_type = '2' ";
    }

    wp.whereOrder = " order by id_corp_no ";
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL =
        "hex(rowid) as rowid, " + "lgd_seqno, " + "id_corp_no, " + "close_flag, " + "close_date, "
            + "id_corp_type, " + "from_type, " + "lgd_reason, " + "crt_date, " + "crt_user, "
            + "lgd_remark, " + "apr_date, " + "crt_901_date, " + "lgd_early_ym, " + "from_type ";

    wp.daoTable = "col_lgd_901";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String fromType = "";
    String[] cde = new String[] {"1", "2"};
    String[] txt = new String[] {"1.人工指定", "2.批次處理"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // ss =wp.col_ss(ii,"from_type");
      // wp.col_set(ii,"tt_from_type", commString.decode(ss, ",1,2", ",1.有風險,2.無風險"));

      fromType = wp.colStr(ii, "from_type");
      wp.colSet(ii, "tt_from_type", commString.decode(fromType, cde, txt));
    }
  }

  int ofcPreretrieve() throws Exception {
    long llCnt, llCnt2;
    if (!isEmpty(lgdSeqno)) {
      String lsSql = "select sum(decode(from_type, '1', 1, 0)) ll_cnt, "
          + " sum(decode(from_type, '1', 0, 1)) ll_cnt2 "
          + " from col_lgd_901 where lgd_seqno = :lgd_seqno ";
      setString("lgd_seqno", lgdSeqno);
      sqlSelect(lsSql);
      if (sqlRowNum == 0) {
        alertErr("select col_lgd_901.sum error");
        return -1;
      } else {
        llCnt = (long) sqlNum("ll_cnt");
        llCnt2 = (long) sqlNum("ll_cnt2");
      }

      if (llCnt == 0) {
        if (llCnt2 > 0) {
          wp.alertMesg =
              "<script language='javascript'> alert('[LGD案件號碼:" + lgdSeqno + "] 為批次處理, 不可再修改')</script>";
          wp.whereStr = "where lgd_seqno = :lgd_seqno ";
          setString("lgd_seqno", lgdSeqno);
          wp.whereStr += "and from_type = '2' ";
          wp.whereStr += sqlRownum(1);

          // wp.col_set("btnAdd_disable", "disabled");
          // wp.col_set("btnUpdate_disable", "disabled");
          // wp.col_set("btnDelete_disable", "disabled");
          wp.colSet("pho_disable", "disabled");

          return 1;
        } else {
          alertErr("此[ID/統編] 尚未人工指定");
          return -1;
        }
      }

      wp.whereStr = "where lgd_seqno = :lgd_seqno ";
      setString("lgd_seqno", lgdSeqno);
      wp.whereStr += "and from_type = '1' ";
      return 1;
    }

    if (isEmpty(idCorpNo)) {
      alertErr("[LGD案件號碼], [ID/統編] : 不可全部空白");
      return -1;
    }

    wp.whereStr = "where id_corp_no = :id_corp_no ";
    setString("id_corp_no", idCorpNo);
    // wp.whereStr += "and from_type = '1' and lgd_seqno is null ";
    wp.whereStr += "and from_type = '1' and nvl(lgd_seqno,'') = '' ";
    return 1;
  }

  String ofcRetrieve() throws Exception {
    String rtn = "";
    long llCnt;
    String lsSql = "select count(*) ll_cnt " + " from col_lgd_901 where id_corp_no = :id_corp_no ";
    setString("id_corp_no", idCorpNo);
    sqlSelect(lsSql);

    llCnt = (long) sqlNum("ll_cnt");
    if (llCnt > 0) {
      rtn = "系統曾經報送";
    } else {
      rtn = "[ID/統編] 尚未報送, 請新增處理";
    }
    return rtn;
  }

  @Override
  public void querySelect() throws Exception {
    lgdSeqno = wp.itemStr("data_k1");
    idCorpNo = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(lgdSeqno)) {
      lgdSeqno = itemKk("lgd_seqno");
    }
    if (empty(idCorpNo)) {
      idCorpNo = itemKk("id_corp_no");
    }

    if (ofcPreretrieve() < 0)
      return;

    wp.selectSQL = "lgd_seqno, " + "id_corp_no, " + "id_corp_p_seqno, " // phopho add
        + "close_flag, " + "id_corp_type, " + "from_type, " + "lgd_type, " + "lgd_reason, "
        + "crt_date, " + "crt_user, " + "lgd_early_ym, " + "risk_amt, " + "acct_type_s, "
        + "acct_status_s, " + "acno_stop_s, " + "lgd_remark, " + "apr_date, " + "apr_user, "
        + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno, "
        + "substrb(acct_type_s, 1, 2) db_acct_type1, "
        + "substrb(acct_type_s, 3, 2) db_acct_type2, "
        + "substrb(acct_type_s, 5, 2) db_acct_type3, "
        + "substrb(acct_type_s, 7, 2) db_acct_type4, "
        + "substrb(acct_type_s, 9, 2) db_acct_type5, "
        + "substrb(acct_status_s, 1, 1) db_acct_status1, "
        + "substrb(acct_status_s, 2, 1) db_acct_status2, "
        + "substrb(acct_status_s, 3, 1) db_acct_status3, "
        + "substrb(acct_status_s, 4, 1) db_acct_status4, "
        + "substrb(acct_status_s, 5, 1) db_acct_status5, "
        + "substrb(acno_stop_s, 1, 1) db_acno_stop1, "
        + "substrb(acno_stop_s, 2, 1) db_acno_stop2, "
        + "substrb(acno_stop_s, 3, 1) db_acno_stop3, "
        + "substrb(acno_stop_s, 4, 1) db_acno_stop4, "
        + "substrb(acno_stop_s, 5, 1) db_acno_stop5, " + "hex(rowid) as rowid, "
        + "uf_chi_name(id_corp_no) db_chi_name, " + "close_date, " + "crt_901_date, "
        + "lgd_early_ym db_early_ym, " + "overdue_ym, " + "overdue_amt, " + "coll_ym, "
        + "coll_amt ";

    wp.daoTable = "col_lgd_901";
    // wp.whereOrder="order by liac_seqno";

    pageSelect();
    if (sqlNotFind()) {
      String rtnmsg = ofcRetrieve();
      alertErr("資料不存在, " + rtnmsg);
      return;
    }
    listWkdataDetl();
  }

  void listWkdataDetl() throws Exception {
    String wkdata = "";
    String[] cde = new String[] {"A", "B1", "B2"};
    String[] txt = new String[] {"A.本息延滯90日", "B1.申請前置協商,調解,更生清算", "B2.發生重大違約強制停用"};

    // 違約型態
    wkdata = wp.colStr("lgd_type");
    wp.colSet("tt_lgd_type", commString.decode(wkdata, ",F", ",F.個人信用卡"));

    // 來源管道
    wkdata = wp.colStr("from_type");
    wp.colSet("tt_from_type", commString.decode(wkdata, ",1,2", ",1.人工指定,2.批次處理"));

    // 違約原因
    wkdata = wp.colStr("lgd_reason");
    wp.colSet("tt_lgd_reason", commString.decode(wkdata, cde, txt));

    // 帳戶狀態
    cde = new String[] {"1", "2", "3", "4", "5"};
    txt = new String[] {"1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清"};
    wkdata = wp.colStr("db_acct_status1");
    wp.colSet("tt_db_acct_status1", commString.decode(wkdata, cde, txt));
    wkdata = wp.colStr("db_acct_status2");
    wp.colSet("tt_db_acct_status2", commString.decode(wkdata, cde, txt));
    wkdata = wp.colStr("db_acct_status3");
    wp.colSet("tt_db_acct_status3", commString.decode(wkdata, cde, txt));
    wkdata = wp.colStr("db_acct_status4");
    wp.colSet("tt_db_acct_status4", commString.decode(wkdata, cde, txt));
    wkdata = wp.colStr("db_acct_status5");
    wp.colSet("tt_db_acct_status5", commString.decode(wkdata, cde, txt));

    wkdata = wp.colStr("lgd_early_ym");
    wp.colSet("db_early_ym", wkdata);

    wp.colSet("id_corp_no_readonly", "readOnly");
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Colm1210Func(wp);

    if (ofValidation() < 0)
      return;

    if (strAction.equals("A") || strAction.equals("U")) {
      if (ofcUpdatebefore() < 0)
        return;
    }

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  int ofValidation() throws Exception {
    long llCnt;
    // 檢查【來源管道】，須為【1.人工指定】，否則顯示錯誤訊息。
    String lsFromType = wp.itemStr("from_type");
    if (!lsFromType.equals("1")) {
      alertErr("非人工指定, 不可異動");
      return -1;
    }

    // 若為【刪除】功能模式，【報送901日期】須為空，否則顯示錯誤訊息。
    if (strAction.equals("D")) {
      if (!isEmpty(wp.itemStr("crt_901_date"))) {
        alertErr("已報送  不可刪除");
        return -1;
      }
      // goto TAG9000
      // 若為【刪除】功能模式，且【apr_date】不為空，則須執行線上主管覆核。
      if (strAction.equals("D") && (!isEmpty(wp.itemStr("apr_date")))) {
        // -check approve- 主管覆核
        if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
          return -1;
        }
      }

    } else {

      // 若為【新增】功能模式，檢查資料庫中是否存在【來源管道為1.人工指定】、【LGD案件序號為空】、且【尚未結案】的資料，若有，則顯示錯誤訊息。
      String idCorpNo = wp.itemStr("id_corp_no");
      if (strAction.equals("A")) {
        if (wfGetIdCorp(idCorpNo, "check") != 1)
          return -1;

        String lsSql =
            "select count(*) as ll_cnt from col_lgd_901 " + "where id_corp_no = :id_corp_no ";
        setString("id_corp_no", idCorpNo);
        lsSql += "and from_type = '1' and lgd_seqno = '' and close_flag <> 'Y'";

        sqlSelect(lsSql);
        llCnt = (long) sqlNum("ll_cnt");
        if (llCnt > 0) {
          alertErr("[ID/統編] 已存在且未報送/結案, 不可新增");
          return -1;
        }
      }

      // 檢查【最早強停年月(lgd_early_ym)】，不可為空
      if (empty(wp.itemStr("lgd_early_ym"))) {
        alertErr("最早強停年月  不可空白");
        return -1;
      }

      // 檢查資料，若為【修改】功能模式，且頁面填入之【最早強停年月(lgd_early_ym)】與DB查詢所得之【db_early_ym】
      // 不同，需檢查【LGD案件序號(lgd_seqno)】，若【LGD案件序號(lgd_seqno)】不為空值，則顯示錯誤訊息並return -1。
      // 若無上述情形，續檢查【apr_date】，若【apr_date】不為空值，則清空【apr_date】、【apr_user】，並顯示提示訊息。
      // System.out.println("GGG -->is_action="+is_action+",
      // lgd_early_ym="+wp.item_ss("lgd_early_ym")+", db_early_ym="+wp.item_ss("db_early_ym"));
      if ((strAction.equals("U"))
          && (!wp.itemStr("lgd_early_ym").equals(wp.itemStr("db_early_ym")))) {
        if (!isEmpty(wp.itemStr("lgd_seqno"))) {
          alertErr("資料已報送, [最早強停年月]  不可修改");
          return -1;
        }
        if (!isEmpty(wp.itemStr("apr_date"))) {
          // wp.col_set("apr_date", "");
          // wp.col_set("apr_user", "");
          func.varsSet("needSign", "Y");
          wp.alertMesg = "<script language='javascript'> alert('須主管重新覆核')</script>";
        }
      }

    }

    return 1;
  }

  // 若為【新增】功能模式，則將頁面【帳戶類別】、【帳戶狀態】、【帳戶強停】的分列欄位內容字串，進行字串連接成為三個欄位內容，以便存入DB中。
  int ofcUpdatebefore() throws Exception {
    String acctType;

    if (strAction.equals("A")) {
      // dw_data.object.crt_user[1] = 登入者;
      // dw_data.object.crt_date[1] = 系統日期;

      // commString.fill不知為何不能用, 程式會當
      // ss = commString.fill(wp.item_ss("db_acct_type1"),2)
      // +commString.fill(wp.item_ss("db_acct_type2"),2)
      // +commString.fill(wp.item_ss("db_acct_type3"),2)
      // +commString.fill(wp.item_ss("db_acct_type4"),2)
      // +commString.fill(wp.item_ss("db_acct_type5"),2);
      acctType = nvl(wp.itemStr("db_acct_type1")) + nvl(wp.itemStr("db_acct_type2"))
          + nvl(wp.itemStr("db_acct_type3")) + nvl(wp.itemStr("db_acct_type4"))
          + nvl(wp.itemStr("db_acct_type5"));
      wp.itemSet("acct_type_s", acctType);
      func.varsSet("acct_type_s", acctType);

      // ss = commString.fill(wp.item_ss("db_acct_status1"),1)
      // +commString.fill(wp.item_ss("db_acct_status2"),1)
      // +commString.fill(wp.item_ss("db_acct_status3"),1)
      // +commString.fill(wp.item_ss("db_acct_status4"),1)
      // +commString.fill(wp.item_ss("db_acct_status5"),1);
      acctType = nvl(wp.itemStr("db_acct_status1")) + nvl(wp.itemStr("db_acct_status2"))
          + nvl(wp.itemStr("db_acct_status3")) + nvl(wp.itemStr("db_acct_status4"))
          + nvl(wp.itemStr("db_acct_status5"));
      wp.itemSet("acct_status_s", acctType);
      func.varsSet("acct_status_s", acctType);

      // ss = commString.fill(wp.item_ss("db_acno_stop1"),1)
      // +commString.fill(wp.item_ss("db_acno_stop2"),1)
      // +commString.fill(wp.item_ss("db_acno_stop3"),1)
      // +commString.fill(wp.item_ss("db_acno_stop4"),1)
      // +commString.fill(wp.item_ss("db_acno_stop5"),1);
      acctType = nvl(wp.itemStr("db_acno_stop1")) + nvl(wp.itemStr("db_acno_stop2"))
          + nvl(wp.itemStr("db_acno_stop3")) + nvl(wp.itemStr("db_acno_stop4"))
          + nvl(wp.itemStr("db_acno_stop5"));
      wp.itemSet("acno_stop_s", acctType);
      func.varsSet("acno_stop_s", acctType);
    }

    if (strAction.equals("A") || strAction.equals("U")) {

      if (wfSetLgdYmAmt() < 0)
        return -1;

    }

    return 1;
  }

  // 說明:程式 function: wf_get_id_corp。
  // 目的:取得頁面【帳戶類別】、【帳戶狀態】、【帳戶強停】的內容。若傳入的 function 第二個參數為 read，則多取得【最早強停年月(若lgd_early_ym)】的值。
  int wfGetIdCorp(String asKey, String asAction) throws Exception {
    String lsKey, lsSql, lsAcctMonth = "";
    String lsCname = "", lsIdcorp = "", lsPSeqno = "";
    String lsType = "", lsStatus = "", lsStop = "";
    String lsAcctPSeqno[] = new String[5];
    String lsStopDate = "", lsStopDate3 = "", lsStopDate2 = "", lsStopDate1 = "";
    double lmAmt = 0;

    // 若頁面傳入key 值為空，則 return 0
    // if (empty(as_key)) return 0;
    if (empty(asKey)) {
      alertErr("身分證ID/統編 不可為空");
      return -1;
    }

    // 預設 ls_idcorp = '1'，若 頁面傳入key 值為數字，且長度為8，則 ls_idcorp = '2'。
    // ls_idcorp = '1'，表示頁面傳入key 值為身分證號。
    // ls_idcorp = '2'，表示頁面傳入key 值為公司統編。
    lsKey = asKey.trim();
    lsIdcorp = "1";
    if (isNumber(lsKey) && (lsKey.length() == 8))
      lsIdcorp = "2";

    if (lsIdcorp.equals("1")) {
      lsSql =
          "select chi_name, id_p_seqno  " + " from crd_idno where id_no = :id_no " + sqlRownum(1);
      setString("id_no", lsKey);
      sqlSelect(lsSql);
      if (sqlRowNum == 0) {
        alertErr("ID 非本行卡友");
        return -1;
      } else {
        lsCname = sqlStr("chi_name");
        lsPSeqno = sqlStr("id_p_seqno");
      }
    }
    if (lsIdcorp.equals("2")) {
      lsSql = "select chi_name, corp_p_seqno  " + " from crd_corp where corp_no = :corp_no "
          + sqlRownum(1);
      setString("corp_no", lsKey);
      sqlSelect(lsSql);
      if (sqlRowNum == 0) {
        alertErr("統編  非本行卡友");
        return -1;
      } else {
        lsCname = sqlStr("chi_name");
        lsPSeqno = sqlStr("corp_p_seqno");
      }
    }

    // 將查詢取得的資料，設定於頁面欄位物件中
    wp.colSet("db_chi_name", lsCname);
    wp.colSet("id_corp_type", lsIdcorp);
    wp.colSet("id_corp_p_seqno", lsPSeqno);
    wp.itemSet("db_chi_name", lsCname);
    wp.itemSet("id_corp_type", lsIdcorp);
    wp.itemSet("id_corp_p_seqno", lsPSeqno);

    // 將查詢取得的 [ls_p_seqno]，作為查詢條件，查詢act_acno table
    // 原始程式，查詢之欄位為 acct_p_seqno，因為該欄位已經移除，改取得 p_seqno欄位值
    lsSql =
        "select acct_type, acct_status, stop_status, p_seqno " + "from act_acno " + "where decode('"
            + lsIdcorp + "','1',id_p_seqno,corp_p_seqno) = :ls_p_seqno " + "order by acct_type ";
    setString("ls_p_seqno", lsPSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      alertErr("open CURSOR error");
      return 0;
    } else {
      // cusr_acno 依據 不同acct_type 最多包含5筆資料。以迴圈解析之。
      // 上述SQL中，[p_seqno]欄位值，into:ls_acct_p_seqno，避免與另一個物件混淆
      // 將取得之資料，依序對應於 db_acct_type1 ~ db_acct_type5
      // 將取得之資料，依序對應於 db_acct_status1 ~ db_acct_status5
      // 將取得之資料，依序對應於 db_acno_stop1 ~ db_acno_stop5
      String[] cde = new String[] {"1", "2", "3", "4", "5"};
      String[] txt = new String[] {"1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清"};
      for (int ii = 0; ii < sqlRowNum; ii++) {
        if (ii >= 5)
          break;
        lsType = sqlStr(ii, "acct_type");
        lsStatus = sqlStr(ii, "acct_status");
        lsStop = sqlStr(ii, "stop_status");
        lsAcctPSeqno[ii] = sqlStr(ii, "p_seqno");
        int rr = ii + 1;
        wp.colSet("db_acct_type" + rr, lsType);
        wp.colSet("db_acct_status" + rr, lsStatus);
        wp.colSet("tt_db_acct_status" + rr, commString.decode(lsStatus, cde, txt));
        wp.colSet("db_acno_stop" + rr, lsStop);
        wp.itemSet("db_acct_type" + rr, lsType);
        wp.itemSet("db_acct_status" + rr, lsStatus);
        wp.itemSet("db_acno_stop" + rr, lsStop);
      }
    }

    // 原始程式註解 //-jh:1040630 by 強停-
    // 取得特定日期欄位的最小值，因為DB2會把欄位空值者計入，所以，若為空值，帶入'99990101'，以便排除空值資料。
    // 依據 current_code 判斷為強停、申停或掛失，若current_code
    // 有強停，則取得該筆資料的oppost_date(停卡日)設定為lgd_early_ym(最早強停年月)。
    // 若 as_action='read'，則取得【最早強停年月(若lgd_early_ym)】的值。
    if (asAction.equals("read")) {
      lsSql = "select min(decode(oppost_date, '', '99990101', oppost_date)) stop_date "
          + "    , min(decode(current_code,'3',decode(oppost_date, '', '99990909', oppost_date),'99990101')) stop_date3 "
          + "    , min(decode(current_code,'1',decode(oppost_date, '', '99990909', oppost_date),'99990101')) stop_date1 "
          + "    , min(decode(current_code,'2',decode(oppost_date, '', '99990909', oppost_date),'99990101')) stop_date2 "
          + "from crd_card "
          + "where p_seqno in (:ls_acct_p_seqno0,:ls_acct_p_seqno1,:ls_acct_p_seqno2,:ls_acct_p_seqno3,:ls_acct_p_seqno4) ";
      setString("ls_acct_p_seqno0", lsAcctPSeqno[0]);
      setString("ls_acct_p_seqno1", lsAcctPSeqno[1]);
      setString("ls_acct_p_seqno2", lsAcctPSeqno[2]);
      setString("ls_acct_p_seqno3", lsAcctPSeqno[3]);
      setString("ls_acct_p_seqno4", lsAcctPSeqno[4]);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        lsStopDate = sqlStr("stop_date");
        lsStopDate3 = sqlStr("stop_date3");
        lsStopDate1 = sqlStr("stop_date1");
        lsStopDate2 = sqlStr("stop_date2");
        if (!lsStopDate3.equals("99990101")) {
          lsAcctMonth = commString.left(lsStopDate3, 6);
        } else if (!lsStopDate1.equals("99990101")) {
          lsAcctMonth = commString.left(lsStopDate1, 6);
        } else if (!lsStopDate2.equals("99990101")) {
          lsAcctMonth = commString.left(lsStopDate2, 6);
        } else {
          lsAcctMonth = commString.left(lsStopDate, 6);
        }
        wp.colSet("lgd_early_ym", lsAcctMonth);
        wp.itemSet("lgd_early_ym", lsAcctMonth);
      }

      // 若lgd_early_ym(最早強停年月)不為'99990101'，表示 lgd_early_ym 有值。
      // 符合上列條件，則依據條件統計 acct_jrnl_bal 設定為 lm_amt。再將 lm_amt 設定為頁面的 risk_amt。
      // if (!ls_acct_month.equals("99990101")) { //年月6碼?
      if (!lsAcctMonth.equals("999901")) {
        lsSql = "select nvl(sum(nvl(acct_jrnl_bal,0)),0) lm_amt " + "from act_acct_hst "
            + "where acct_month = :acct_month "
            + "  and p_seqno in (:ls_acct_p_seqno0,:ls_acct_p_seqno1,:ls_acct_p_seqno2,:ls_acct_p_seqno3,:ls_acct_p_seqno4) ";
        setString("acct_month", lsAcctMonth);
        setString("ls_acct_p_seqno0", lsAcctPSeqno[0]);
        setString("ls_acct_p_seqno1", lsAcctPSeqno[1]);
        setString("ls_acct_p_seqno2", lsAcctPSeqno[2]);
        setString("ls_acct_p_seqno3", lsAcctPSeqno[3]);
        setString("ls_acct_p_seqno4", lsAcctPSeqno[4]);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          lmAmt = sqlNum("lm_amt");
          wp.colSet("risk_amt", String.valueOf(lmAmt));
          wp.itemSet("risk_amt", String.valueOf(lmAmt));
        }
      }
    }
    return 1;
  }

  // 說明:程式 function: wf_set_lgd_ym_amt。
  // 目的:取得頁面[違約日暴險額(risk_amt)]、取得[列入逾放年月]、[曝險金額]、[轉催收年月]、[暴險金額]等值。
  int wfSetLgdYmAmt() throws Exception {
    String lsEarlyYm = "", lsIdno = "", lsCollYm = "", lsOverYm = "", lsSql = "";
    double lmEarlyAmt = 0, lmCollAmt = 0, lmOverAmt = 0, lmJrnlBal = 0;

    // 與原始程式差異說明:
    // 因為新版table schema，移除 act_acno.acct_p_seqno 欄位，故查詢 acct_p_seqno 欄位，置換成p_seqno。
    // 續上，查詢條件 act_acno.p_seqno = act_acno.acct_p_seqno 則移除。
    // 續上，查詢條件 A.p_seqno = B.acct_p_seqno 轉為 A.p_seqno = B.p_seqno

    // 設定起始值，若頁面id_corp_no 無值，則return 0
    lsIdno = wp.itemStr("id_corp_no");
    if (empty(lsIdno))
      return 0;

    // 取得[違約日暴險額(risk_amt)]
    lsEarlyYm = wp.itemStr("lgd_early_ym");
    if (!isEmpty(lsEarlyYm)) {
      lsSql = "select nvl(sum(nvl(acct_jrnl_bal,0)),0) lm_early_amt " + "from act_acct_hst "
          + "where acct_month = :acct_month "
          + "  and p_seqno in (select p_seqno from act_acno where acct_key = :acct_key ) ";
      // + " and p_seqno in (select p_seqno from act_acno where id_p_seqno =
      // uf_idno_pseqno(:acct_key) ) "; //uf_idno_pseqno() 速度很慢
      setString("acct_month", lsEarlyYm);
      setString("acct_key", lsIdno + "0");
      sqlSelect(lsSql);
      if (sqlRowNum == 0) {
        alertErr("select act_acct_hst error");
        return -1;
      } else {
        lmEarlyAmt = sqlNum("lm_early_amt");
        if (lmEarlyAmt <= 0) {
          lsSql = "select nvl(sum(nvl(acct_jrnl_bal,0)),0) lm_early_amt2 " + "from act_acct "
              + "where p_seqno in (select p_seqno from act_acno where acct_key = :acct_key ) ";
          setString("acct_key", lsIdno + "0");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            lmEarlyAmt = sqlNum("lm_early_amt2");
            if (lmEarlyAmt < 0)
              lmEarlyAmt = 0;
            wp.colSet("risk_amt", String.valueOf(lmEarlyAmt));
            wp.itemSet("risk_amt", String.valueOf(lmEarlyAmt));
          }
        }
      }
    }

    // 取得[列入逾放年月]、[曝險金額]
    lsSql = "select min(substrb(org_delinquent_date,1,6)) ls_over_ym "
        + "from act_acno where acct_key = :acct_key ";
    setString("acct_key", lsIdno + "0");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      lsOverYm = sqlStr("ls_over_ym");
    }
    wp.colSet("overdue_ym", lsOverYm);
    wp.itemSet("overdue_ym", lsOverYm);

    if (!isEmpty(lsOverYm)) {
      lsSql = "select nvl(sum(nvl(acct_jrnl_bal,0)),0) lm_over_amt " + "from act_acct_hst "
          + "where acct_month = :acct_month "
          + "  and p_seqno in (select p_seqno from act_acno where acct_key = :acct_key ) ";
      setString("acct_month", lsOverYm);
      setString("acct_key", lsIdno + "0");
      sqlSelect(lsSql);
      if (sqlRowNum == 0) {
        alertErr("select act_acct_hst error");
        return -1;
      } else {
        lmOverAmt = sqlNum("lm_over_amt");
        if (lmOverAmt <= 0) {
          lsSql = "select nvl(sum(nvl(acct_jrnl_bal,0)),0) lm_over_amt2 " + "from act_acct "
              + "where p_seqno in (select p_seqno from act_acno where acct_key = :acct_key ) ";
          setString("acct_key", lsIdno + "0");
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            lmOverAmt = sqlNum("lm_over_amt2");
            if (lmOverAmt < 0)
              lmOverAmt = 0;
            wp.colSet("overdue_amt", String.valueOf(lmOverAmt));
            wp.itemSet("overdue_amt", String.valueOf(lmOverAmt));
          }
        }
      }
    }

    // 取得轉催收[coll]，包含:[轉催收年月]、[暴險金額]
    lsSql = "select nvl(sum(nvl(A.end_bal,0)),0) lm_coll_amt, "
        + "      nvl(substrb(max(A.trans_date),1,6),'') ls_coll_ym "
        + " from col_bad_detail A, act_acno B "
        + "where A.p_seqno = B.p_seqno and B.acct_key = :acct_key " + "and trans_type = '3' "
        // + "and new_item_ename in ('CB','CI','CC') " //todo no column
        + "and new_acct_code in ('CB','CI','CC') "
        + "and trans_date = (select min(trans_date) from col_bad_detail "
        + "                  where p_seqno=B.p_seqno and trans_type = '3') ";
    setString("acct_key", lsIdno + "0");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      lsCollYm = sqlStr("ls_coll_ym");
      lmCollAmt = sqlNum("lm_coll_amt");
      if (lmCollAmt < 0)
        lmCollAmt = 0;
      wp.colSet("coll_ym", lsCollYm);
      wp.colSet("coll_amt", String.valueOf(lmCollAmt));
      wp.itemSet("coll_ym", lsCollYm);
      wp.itemSet("coll_amt", String.valueOf(lmCollAmt));
    }

    // 取得[違約日暴險額(risk_amt)]、[列入逾放年月對應的曝險金額(overdue_amt)]
    if (lmEarlyAmt == 0 || lmOverAmt == 0) {
      lsSql = "select nvl(sum(nvl(acct_jrnl_bal,0)),0) lm_jrnl_bal " + "from act_acct "
          + "where p_seqno in (select p_seqno from act_acno where acct_key = :acct_key ) ";
      setString("acct_key", lsIdno + "0");
      sqlSelect(lsSql);
      if (sqlRowNum == 0) {
        alertErr("select act_acct error");
        return -1;
      } else {
        lmJrnlBal = sqlNum("lm_jrnl_bal");
      }
      if (lmJrnlBal <= 0)
        return 1;

      if ((!isEmpty(lsEarlyYm)) && (lmEarlyAmt == 0)) {
        // wp.col_set("risk_amt", num_2str(lm_jrnl_bal,""));
        wp.colSet("risk_amt", String.valueOf(lmJrnlBal));
        wp.itemSet("risk_amt", String.valueOf(lmJrnlBal));
      }
      if ((!isEmpty(lsOverYm)) && (lmOverAmt == 0)) {
        wp.colSet("risk_amt", String.valueOf(lmJrnlBal));
        wp.itemSet("risk_amt", String.valueOf(lmJrnlBal));
      }
    }

    return 1;
  }

  // 說明:【新增】功能頁面，輸入【身分證ID/統編】欄位值後，若點擊【最早強停年月(lgd_early_ym)】、【違約備註(lgd_remark)】，即執行此event。
  void wfItemchanged() throws Exception {
    String lsIdCorpNo = wp.itemStr("id_corp_no");

    wfGetIdCorp(lsIdCorpNo, "read");

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
