package cmsm03;
/**
 * 2019-1224:  Alex  dataRead2 fix
 * 2019-1219:  Alex  ptr_branch -> gen_brn
 * 2019-1127:  Alex  proAppr(),crt_time
 * 2019-1028   JH    使用條件
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 2020-0107:  Ru    modify AJAX
 * 2020-0203:  Ru    增貴賓卡欄、修改檢核申請條件
 * 2020-04-20  shiyuqi       updated for project coding standard     
 * 2020-05-21  JustinWu     modify the where condition of dataRead2()*
 * 2020-11-19   JustinWu     fix a bug dataRead2()
 * 109-12-30  V1.00.01  shiyuqi       修改无意义命名
 * 110-01-05  V1.00.02  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
 * 112-01-03  V1.00.03  Machao      頁面bug調整   *
 * 112-04-24  V1.00.04  Wilson      mark insert crd_ppcard_apply_temp error *
 * 112-04-25  V1.00.05  Wilson      新增申請信用卡號欄位  *
 * 112-05-16  V1.00.06  Wilson      bin_type改抓crd_card  *
 * 112-06-14  V1.00.07  Wilson      apply_credit_card_no改成card_no  *
 * 112-07-13  V1.00.08  Wilson      取消線上覆核  *
 * 2023-0816  JH    *card.html.eng_name
 */

import java.text.ParseException;

import ofcapp.BaseAction;

public class Cmsm3120 extends BaseAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  Cmsm3120Func func = new Cmsm3120Func();

  String applyNo = "", dataKK2 = "";
 // String aa = "";

  @Override
  public void userAction() throws Exception {
    func.setConn(wp);

    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      String lsIdNo = "";
      lsIdNo = wp.itemStr2("ex_idno");
      clearFunc();
      initData(lsIdNo);
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "R1")) {
      // -檢核申請條件-
      strAction = "R";
      dataRead2();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -檢核使用條件-
      strAction = "R";
      dataRead3();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -可申請-
      procAppr();
    }
    // 20200107 modify AJAX
    else if (eqIgno(wp.buttonCode, "AJAX")) {
      if ("1".equals(wp.getValue("ID_CODE"))) {
        wfAjaxIdNo();
      }
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsm3120_detl")) {
        wp.optionKey = wp.colStr(0, "mail_branch");
        this.dddwList("dddw_branch",
            "select branch as db_code , branch||'_'||full_chi_name as db_desc from gen_brn where 1=1 order by 1 Asc ");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "cmsm3120_detl")) {
        wp.optionKey = wp.colStr(0, "zip_code");
        this.dddwList("d_dddw_zipcode",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 order by zip_code");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("建檔日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_crt_date1"), "A.crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "A.crt_date", "<=")
        + sqlCol(wp.itemStr("ex_user"), "A.crt_user")
        + sqlCol(wp.itemStr("ex_idno"), "B.id_no", "like%");
    if (!eqIgno(wp.itemStr("ex_bin_type"), "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_bin_type"), "A.bin_type");
    }
    if (!eqIgno(wp.itemStr("ex_vip_kind"), "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_vip_kind"), "A.vip_kind");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " A.apply_no ," + " A.apply_date ," + " B.id_no ," + " B.id_no_code ,"
        + " A.eng_name ," + " A.reg_card_type ," + " A.bin_type ," + " A.user_remark ,"
        + " A.crt_user ," + " A.crt_date ," + " A.proc_date ," + " A.pp_card_no ," + " B.chi_name ,"
        + " B.sex ," + " A.vip_kind ";
    wp.daoTable = "crd_ppcard_apply A left join crd_idno B on A.id_p_seqno = B.id_p_seqno ";
    wp.whereOrder = " order by A.apply_no, A.apply_date ";
    logSql();
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setPageValue();

  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_idno", wp.colStr(ii, "id_no") + "-" + wp.colStr(ii, "id_no_code"));
      if ("1".equals(wp.colStr(ii, "vip_kind"))) {
        wp.colSet(ii, "vip_kind", "1_新貴通");
      } else if ("2".equals(wp.colStr(ii, "vip_kind"))) {
        wp.colSet(ii, "vip_kind", "2_龍騰卡");
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    applyNo = wp.itemStr("data_k1");
    dataKK2 = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(applyNo))
      applyNo = itemkk("apply_no");
    if (empty(applyNo)) {
      alertErr2("申請編號:不可空白");
      return;
    }

    wp.selectSQL = "" + " A.* ," + " hex(A.rowid) as rowid , " + " B.chi_name, B.sex, B.id_no "
        + ", to_char(A.mod_time,'yyyymmdd') as mod_date " + ", B.sex as db_sex "
        + ", 'readOnly' as off_text ";
 //       + ", decode(A.bin_type,'V','VISA','M','MasterCard','J','JCB') as tt_bin_type ";
    wp.daoTable = "crd_ppcard_apply A left join crd_idno B on A.id_p_seqno =B.id_p_seqno ";
    wp.whereStr = "where 1=1" + sqlCol(applyNo, "A.apply_no");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + applyNo);
      return;
    } else {
      if ("1".equals(wp.colStr("vip_kind"))) {
        wp.colSet("vip_kind", "1_新貴通");
        wp.colSet("kk_vip_kind", "1");
      } else if ("2".equals(wp.colStr("vip_kind"))) {
        wp.colSet("vip_kind", "2_龍騰卡");
        wp.colSet("kk_vip_kind", "2");
      }
    }
  }

  void dataRead2() throws Exception {
    // -檢核申請條件-
    if (wp.itemEmpty("id_p_seqno")) {
      alertErr2("身份證ID 不可空白");
      return;
    }

    // 查詢CRD_PPCARD_APPLY_TEMP
    int result = queryCrdPPCardApplyTemp();

    if (result == 0) {
      return;
    }

    wp.sqlCmd = " select "
        + " (select max(issue_seq) from mkt_ppcard_issue where bin_type=A.bin_type) as db_pp_seqno ,"
        + " A.card_no ," + " A.card_type ," + " nvl(A.group_code,'0000') group_code ,"
        + " A.sup_flag ," + " A.eng_name ," + " A.p_seqno, A.acno_p_seqno,A.id_p_seqno,"
        + " A.issue_date ," + " A.bin_type ," + " B.major_flag ," + " B.card_purch_code ,"
        + " B.first_cond ," + " B.fir_purch_mm ," + " B.fir_item_ename_bl ,"
        + " B.fir_item_ename_ca ," + " B.fir_item_ename_it ," + " B.fir_item_ename_id ,"
        + " B.fir_item_ename_ao ," + " B.fir_item_ename_ot ," + " B.fir_it_type ,"
        + " B.fir_min_amt ," + " B.fir_amt_cond ," + " B.fir_tot_amt ," + " B.fir_cnt_cond ,"
        + " B.fir_tot_cnt ," + " B.last_amt_cond ," + " B.last_tot_amt ," + " B.nofir_cond ,"
        + " B.purch_mm ," + " B.item_ename_bl ," + " B.item_ename_ca ," + " B.item_ename_it ,"
        + " B.item_ename_id ," + " B.item_ename_ao ," + " B.item_ename_ot ," + " B.it_type , "
        + " B.min_amt , " + " B.amt_cond ," + " B.tot_amt ," + " B.cnt_cond ," + " B.tot_cnt ,"
        + " 0 db_cnt ," + " 0 db_amt ," + " 0 db_amt_last ," + " 'N' as db_apply_flag ,"
        + " '' as db_errmsg ," + " '' as db_err_type "
        + " from crd_card A left join mkt_ppcard_apply B "
        + " on A.card_type =B.card_type and A.group_code =B.group_code "
        + " where A.current_code ='0' " 
        + sqlCol(wp.itemStr("id_p_seqno"), "A.id_p_seqno")
//        + sqlCol(wp.itemStr("vip_kind"), "B.vip_kind") 
        + " order by 1";
    logSql();
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr("卡友已無有效卡");
      return;
    }
    wp.setListCount(1);
    // dataRead2_sum();
    dataRead2After();
  }

  void dataRead2After() throws Exception {
    int ilApply = 0;
    String lsBusiDate = "", lsLastYy = "";
    String lsCardPurchCode = "", lsCardType = "", lsGroupCode = "";

    lsBusiDate = busiDate();
    lsLastYy = commDate.dateAdd(lsBusiDate, -1, 0, 0);

    String string = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // -首年金額,筆數-
      if (wp.colEq(ii, "first_cond", "Y") && wp.colEq(ii, "fir_amt_cond", "Y"))
        wp.colSet(ii, "db_cond_amt_fir", wp.colStr(ii, "fir_tot_amt"));
      else
        wp.colSet(ii, "db_cond_amt_fir", 0);
      if (wp.colEq(ii, "first_cond", "Y") && wp.colEq(ii, "fir_cnt_cond", "Y"))
        wp.colSet(ii, "wk_cond_cnt_fir", wp.colStr(ii, "fir_tot_cnt"));
      else
        wp.colSet(ii, "wk_cond_cnt_fir", 0);
      // -非首年金額wk_cond_amt,筆數wk_cond_cnt-
      if (wp.colEq(ii, "nofir_cond", "Y") && wp.colEq(ii, "amt_cond", "Y"))
        wp.colSet(ii, "wk_cond_amt", wp.colStr(ii, "tot_amt"));
      else
        wp.colSet(ii, "wk_cond_amt", 0);
      if (wp.colEq(ii, "nofir_cond", "Y") && wp.colEq(ii, "cnt_cond", "Y"))
        wp.colSet(ii, "wk_cond_cnt", wp.colStr(ii, "tot_cnt"));
      else
        wp.colSet(ii, "wk_cond_cnt", 0);
      // -去年度金額:wk_cond_amt_last-
      if (wp.colEq(ii, "last_amt_cond", "Y"))
        wp.colSet(ii, "wk_cond_amt_last", wp.colStr(ii, "last_tot_amt"));
      else
        wp.colSet(ii, "wk_cond_amt_last", 0);

      string = wp.colStr(ii, "card_purch_code");
      if (eqIgno(string, "1"))
        wp.colSet(ii, "tt_card_purch", "正附卡合併");
      else if (eqIgno(string, "2"))
        wp.colSet(ii, "tt_card_purch", "正附卡分開");

      if (empty(wp.colStr(ii, "bin_type")))
        continue;

      if (eqIgno(wp.colStr(ii, "major_flag"), "Y")) {
        lsCardPurchCode = "1";
      } else {
        lsCardPurchCode = wp.colStr(ii, "card_purch_code");
      }
      lsCardType = wp.colStr(ii, "card_type");
      lsGroupCode = wp.colStr(ii, "group_code");
      wfSetApplyParm(ii);
      if (func.selectBilBill(lsCardPurchCode, lsCardType, lsGroupCode) != 1) {
        wp.colSet(ii, "db_errmsg", func.getMsg());
        continue;
        // errmsg(bil.getMsg());
      } else {
        wp.colSet(ii, "db_cnt", func.varsStr("ii_cnt"));
        wp.colSet(ii, "db_amt", func.varsStr("im_amt"));
      }

      if (wp.colEq(ii, "last_amt_cond", "Y")) {
        func.varsSet("bl", wp.colStr(ii, "item_ename_bl"));
        func.varsSet("it", wp.colStr(ii, "item_ename_it"));
        func.varsSet("id", wp.colStr(ii, "item_ename_id"));
        func.varsSet("ca", wp.colStr(ii, "item_ename_ca"));
        func.varsSet("ao", wp.colStr(ii, "item_ename_ao"));
        func.varsSet("ot", wp.colStr(ii, "item_ename_ot"));
        func.varsSet("beg_ym", lsLastYy + "01");
        func.varsSet("end_ym", lsLastYy + "12");
        if (func.selectMktCardConsume(lsCardPurchCode, lsCardType, lsGroupCode) != 1) {
          wp.colSet(ii, "db_errmsg", func.getMsg());
          // errmsg(bil.getMsg());
        } else {
          wp.colSet(ii, "db_amt_last", func.varsStr("im_amt"));
        }
      }

      wfCheckApply(ii);
      // ddd("err:"+wp.col_ss(ii,"db_err_type"));
      if (wp.colEq(ii, "db_apply_flag", "Y"))
        ilApply++;
    }
    wp.colSet("il_apply", "" + ilApply);
    if (ilApply <= 0) {
      alertMsg("所有卡片均不符合申請 貴賓卡 之條件");
    }

    // 寫入CRD_PPCARD_APPLY_TEMP
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      rc = insertCrdPPCardApplyTemp(ii);
      sqlCommit(rc);
    }

    // 查詢CRD_PPCARD_APPLY_TEMP
    queryCrdPPCardApplyTemp();
  }

  void dataRead3() throws Exception {
    String lsBusiDate = busiDate();

    wp.sqlCmd = "select * " + " from cms_right_cal" + " where (item_no ='11' or item_no = '10')"
        + sqlCol(wp.colStr("id_p_seqno"), "id_p_seqno")
        + sqlCol(lsBusiDate.substring(0, 4), "curr_year") + " order by card_no Asc ";

    pageQuery();
    if (sqlRowNum <= 0) {
      selectOK();
      // err_alert("卡友 未申請貴賓卡 或 末執行免費使用條件處理");
      // return;
    }

    dataRead3After();
    wp.setListCount(1);
    dataRead3Tab2();
    wp.colSet("tb1_cnt", wp.listCount[0]);
    wp.colSet("tb2_cnt", wp.listCount[1]);
  }

  void dataRead3After() {
    int llNrow = wp.listCount[0];
    String lsSql = "select pp_card_no, current_code, valid_to"
        + " from crd_card_pp where card_no =?" + " order by issue_date desc " + commSqlStr.rownum(1);

    for (int ii = 0; ii < llNrow; ii++) {
      setString2(1, wp.colStr(ii, "card_no"));
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "pp_card_no", sqlStr("pp_card_no"));
        wp.colSet(ii, "pp_current_code", sqlStr("current_code"));
        wp.colSet(ii, "pp_valid_to", sqlStr("valid_to"));
      }
    }
  }

  void dataRead3Tab2() {
    wp.sqlCmd = "select right_date, right_cnt, use_cnt, last_use_date"
        + " from cms_right_list where (item_no='11' or item_no='10')"
        + " and id_p_seqno =? and right_cnt >use_cnt" + " order by right_date";
    daoTid = "B-";
    setString2(1, wp.colStr("id_p_seqno"));
    pageQuery();
    wp.setListSernum(1, "B-ser_num", sqlRowNum);
  }

  @Override
  public void saveFunc() throws Exception {
    cmsm03.Cmsm3120Func func = new cmsm03.Cmsm3120Func();
    func.setConn(wp);

    // --10/15 客戶若符合申請資格，無需覆核即可申請
//    if (isAdd() && wp.itemEq("apr_flag", "Y") == false) {
//      if (!wp.colEq("apr_yn", "Y")) {
//        if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
//          return;
//        }
//      }
//    } else {
//      wp.itemSet("approval_user", wp.loginUser);
//    }

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
    }
  }

  @Override
  public void initPage() {
    if (eqIgno(strAction, "new")) {
      wp.colSet("proc_flag", "N");
    }

  }

  // 20200107 modify AJAX
  public void wfAjaxIdNo() throws Exception {
    // super.wp = wr;

    // String ls_winid =
    selectData(wp.itemStr("ax_id_no"));
    if (rc != 1) {
      wp.addJSON("id_p_seqno", "");
      wp.addJSON("chi_name", "");
      wp.addJSON("sex", "");
      wp.addJSON("db_sex", "");
      return;
    }
    wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
    wp.addJSON("chi_name", sqlStr("chi_name"));
    wp.addJSON("sex", sqlStr("sex"));
    wp.addJSON("db_sex", sqlStr("db_sex"));

  }

  void selectData(String idNo) {
    String lsSql = " select " + " id_p_seqno, chi_name, sex , sex as db_sex " + " from crd_idno"
        + " where id_no ='" + idNo + "'";
    this.sqlSelect(lsSql);

    if (sqlRowNum <= 0) {
      rc = -1;
      return;
    }
    return;
  }

  void wfSetApplyParm(int ll) throws ParseException {
    // bil = new BilBill();
    // bil.setConn(wp);
    func.varsSet("id_p_seqno", wp.colStr(ll, "id_p_seqno"));

    String lsBusiDate = "", lsEndYm = "";
    int liPurchMm = 0;

    lsBusiDate = busiDate();
    lsEndYm = commDate.dateAdd(lsBusiDate, 0, -1, 0).substring(0, 6);
    // -首年-
    if (eqIgno(wp.colStr(ll, "issue_date").substring(0, 4), lsBusiDate.substring(0, 4))) {
      func.varsSet("bl", wp.colStr(ll, "fir_item_ename_bl"));
      func.varsSet("it", wp.colStr(ll, "fir_item_ename_it"));
      func.varsSet("id", wp.colStr(ll, "fir_item_ename_id"));
      func.varsSet("ca", wp.colStr(ll, "fir_item_ename_ca"));
      func.varsSet("ao", wp.colStr(ll, "fir_item_ename_ao"));
      func.varsSet("ot", wp.colStr(ll, "fir_item_ename_ot"));
      func.varsSet("is_it_type", wp.colStr(ll, "fir_it_type"));
      func.varsSet("is_date2", lsEndYm);
      liPurchMm = (int) (0 - wp.colNum(ll, "fir_purch_mm"));
      func.varsSet("is_date1", commDate.dateAdd(lsBusiDate, 0, liPurchMm, 0).substring(0, 6));
      func.varsSet("im_low_amt", wp.colStr(ll, "fir_min_amt"));
      func.varsSet("is_idno", wp.itemStr("id_no"));
      return;
    }
    // -非首年-
    func.varsSet("bl", wp.colStr(ll, "item_ename_bl"));
    func.varsSet("it", wp.colStr(ll, "item_ename_it"));
    func.varsSet("id", wp.colStr(ll, "item_ename_id"));
    func.varsSet("ca", wp.colStr(ll, "item_ename_ca"));
    func.varsSet("ao", wp.colStr(ll, "item_ename_ao"));
    func.varsSet("ot", wp.colStr(ll, "item_ename_ot"));
    func.varsSet("is_it_type", wp.colStr(ll, "it_type"));
    func.varsSet("is_date2", lsEndYm);
    liPurchMm = (int) (0 - wp.colNum(ll, "purch_mm"));
    func.varsSet("is_date1", commDate.dateAdd(lsBusiDate, 0, liPurchMm, 0).substring(0, 6));
    func.varsSet("im_low_amt", wp.colStr(ll, "min_amt"));
    func.varsSet("is_idno", wp.itemStr("id_no"));
    return;
  }

  void wfCheckApply(int ii) {
    if (ii < 0)
      return;
    if (empty(wp.colStr(ii, "bin_type"))) {
      wp.colSet(ii, "db_err_type", "1");
      wp.colSet(ii, "db_errmsg", "卡種不可申請[貴賓卡]");
      return;
    }

    if (eqIgno(wp.colStr(ii, "major_flag"), "Y")) {
      if (eqIgno(wp.colStr(ii, "sup_flag"), "1")) {
        wp.colSet(ii, "db_err_type", "1");
        wp.colSet(ii, "db_err_text", "卡片不符");
        wp.colSet(ii, "db_errmsg", "附卡不可申請[貴賓卡]");
        return;
      }
    }
    String lsYy1 = "", lsYy2 = "";

    lsYy1 = busiDate().substring(0, 4);
    lsYy2 = wp.colStr(ii, "issue_date").substring(0, 4);

    // *--首年
    if (eqIgno(lsYy1, lsYy2)) {
      if (!wp.colEq(ii, "first_cond", "Y")) {
        wp.colSet(ii, "db_err_type", "2");
        wp.colSet(ii, "db_err_text", "消費不足");
        wp.colSet(ii, "db_errmsg", "核卡首年不可申請[貴賓卡]");
        return;
      }
      if (wp.colEq(ii, "fir_amt_cond", "Y")) {
        if (wp.colNum(ii, "db_amt") >= wp.colNum(ii, "fir_tot_amt")) {
          wp.colSet(ii, "db_apply_flag", "Y");
          return;
        }
      }
      wp.colSet(ii, "db_err_type", "2");
      wp.colSet(ii, "db_errmsg", "核卡首年不可申請[貴賓卡]");
      return;
    }

    // *--非首年
    if (!wp.colEq(ii, "last_amt_cond", "Y") && !wp.colEq(ii, "nofir_cond", "Y")) {
      wp.colSet(ii, "db_err_type", "2");
      wp.colSet(ii, "db_errmsg", "非首年核卡不可申請[貴賓卡]");
      return;
    }
    String lsMsg = "", lsApplyFlag = "N";

    if (wp.colEq(ii, "nofir_cond", "Y")) {
      if (wp.colEq(ii, "amt_cond", "Y")) {
        if (wp.colNum(ii, "db_amt") >= wp.colNum(ii, "tot_amt")) {
          lsApplyFlag = "Y";
        }
      }

      if (wp.colEq(ii, "cnt_cond", "Y")) {
        if (wp.colNum(ii, "db_cnt") >= wp.colNum(ii, "tot_cnt")) {
          lsApplyFlag = "Y";
        }
      }

      if (eqIgno(lsApplyFlag, "N")) {
        wp.colSet(ii, "db_err_type", "2");
        wp.colSet(ii, "db_err_text", "消費不足");
        lsMsg = "去年度消費不足, 如欲申請[貴賓卡]，需酌收製卡費";
        wp.colSet(ii, "db_errmsg", lsMsg);
        wp.colSet(ii, "db_apply_flag", lsApplyFlag);
        return;
      }
    }

    // *--去年
    if (wp.colEq(ii, "last_amt_cond", "Y")) {
      if (wp.colNum(ii, "db_amt_last") >= wp.colNum(ii, "last_tot_amt")) {
        lsApplyFlag = "Y";
      } else {
        lsApplyFlag = "N";
        wp.colSet(ii, "db_err_type", "2");
        wp.colSet(ii, "db_err_text", "消費不足");
        lsMsg = "去年度消費不足, 如欲申請[貴賓卡]，需酌收製卡費";
      }
    }
    wp.colSet(ii, "db_errmsg", lsMsg);
    wp.colSet(ii, "db_apply_flag", lsApplyFlag);
  }

  void procAppr() {
    String[] lsCardNo = wp.itemBuff("card_no");
    wp.listCount[0] = lsCardNo.length;

    int i = Integer.parseInt(wp.itemStr("data_k1")) - 1;
    int llRow = -1;

    if (!empty(wp.colStr(i, "bin_type"))) {
      if (wp.colNum(0, "il_apply") > 0) {
        if (eqIgno(wp.colStr(i, "db_apply_flag"), "Y")) {
          llRow = i;
        }
      } else {
        log("AA:" + wp.colStr(i, "db_err_type"));
        if (eqIgno(wp.colStr(i, "db_err_type"), "2")) {
          llRow = i;
        }
      }
    }

    if (eqIgno(wp.colStr(i, "db_apply_flag"), "N")) {
      if (itemEq("conf_flag", "Y") == false) {
        wp.respMesg = "消費條件不符合，需酌收製卡費，確定無誤";
        wp.colSet("conf_mesg", " || 1==1 ");
        wp.colSet("serNum", i + 1);
        return;
      }
    }

    wp.colSet("apply_flag", "|| 1==1");
    wp.colSet("bin_type", wp.colStr(i, "bin_type"));
    wp.colSet("eng_name", wp.colStr(i, "eng_name"));
    wp.colSet("reg_card_type", wp.colStr(i, "card_type"));
    wp.colSet("reg_group_code", wp.colStr(i, "group_code"));
    wp.colSet("card_no", wp.colStr(i, "card_no"));
    String AcnoPSeqno = selectCrdCard(lsCardNo[i]);
    getMailAddr(AcnoPSeqno);
    wp.colSet("zip_code", sqlStr("bill_sending_zip"));
    wp.colSet("mail_addr1", sqlStr("bill_sending_addr1"));
    wp.colSet("mail_addr2", sqlStr("bill_sending_addr2"));
    wp.colSet("mail_addr3", sqlStr("bill_sending_addr3"));
    wp.colSet("mail_addr4", sqlStr("bill_sending_addr4"));
    wp.colSet("mail_addr5", sqlStr("bill_sending_addr5"));
//    if (wp.colEq(i, "bin_type", "V")) {
//      wp.colSet("tt_bin_type", "VISA");
//    } else if (wp.colEq(i, "bin_type", "M")) {
//      wp.colSet("tt_bin_type", "MasterCard");
//    } else if (wp.colEq(i, "bin_type", "J")) {
//      wp.colSet("tt_bin_type", "JCB");
//    }
    // -是否覆核-
    if (wp.colNum(0, "il_apply") > 0 && llRow >= 0)
      wp.colSet("apr_yn", "Y");
    else
      wp.colSet("apr_yn", "N");
  }
  
  public String selectCrdCard(String a) {
//	  String acnoPSeqno = "";
	  String sql1 = " select " + " acno_p_seqno " +  " from crd_card where 1=1 and card_no = ? ";
	  sqlSelect(sql1, new Object[] {a});
	  String acnoPSeqno = sqlStr("acno_p_seqno");
	  return acnoPSeqno;
  }

  void getMailAddr(String acnoPSeqno) {
    if (empty(acnoPSeqno)) {
      alertErr2("查無持卡人帳單地址");
      return;
    }

    String sql1 = " select " + " bill_sending_zip , " + " bill_sending_addr1 , "
        + " bill_sending_addr2 , " + " bill_sending_addr3 , " + " bill_sending_addr4 , "
        + " bill_sending_addr5   " + " from act_acno where 1=1 and acno_p_seqno = ? ";

    sqlSelect(sql1, new Object[] {acnoPSeqno});

    if (sqlRowNum <= 0) {
      alertErr2("查無持卡人帳單地址");
      return;
    }

  }

  void initData(String idNo) {
    wp.colSet("kk_vip_kind", "2");
    wp.colSet("vip_kind", "2_龍騰卡");
    if (empty(idNo))
      return;
    wp.colSet("id_no", idNo);
    selectData(idNo);
    if (rc != 1) {
      alertErr2("身分證ID:輸入錯誤");
      return;
    }
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("db_sex", sqlStr("db_sex"));
  }

  int queryCrdPPCardApplyTemp() throws Exception {
    wp.sqlCmd = "select A.id_p_seqno, "
            + "A.seqno, A.card_no, A.card_type, A.group_code, "
        + "A.sup_flag, A.db_apply_flag, A.bin_type, A.issue_date, "
            + "A.db_cnt, A.db_amt, "
        + "A.db_amt_last, A.major_flag, A.db_cond_amt_fir, A.wk_cond_cnt_fir, "
        + "A.wk_cond_amt, A.wk_cond_cnt, A.wk_cond_amt_last, A.tt_card_purch, "
        + "A.card_purch_code, A.db_errmsg, A.db_err_type, "
            + "A.db_err_text, A.mod_user, "
        + "to_char(A.mod_time,'yyyymmdd') as mod_time, "
            + "A.mod_pgm, A.mod_seqno "
        + "from crd_ppcard_apply_temp A "
            + " where 1=1 "
        + sqlCol(wp.itemStr("id_p_seqno"), "A.id_p_seqno")
        + " order by A.db_amt desc, A.db_cnt desc, A.card_no ";

    logSql();
    pageQuery();
    if (sqlRowNum <=0) return 1;
    if (sqlRowNum > 0) {
      if (!wp.colStr("mod_time").equals(wp.sysDate)) {
        deleteCrdPPCardApplyTemp();
        return 1;
      }
    }
    wp.setListCount(1);

    //-JH:get eng_name-
    String sql1 ="select eng_name from crd_card where card_no =?";
    int llNrow =sqlRowNum;
    for (int ll = 0; ll <llNrow ; ll++) {
      String ls_cardNo =wp.colStr(ll,"card_no");
      sqlSelect(sql1, ls_cardNo);
      if (sqlRowNum >0) {
        wp.colSet(ll,"eng_name", sqlStr("eng_name"));
      }
    }

    return 0;
  }

  int deleteCrdPPCardApplyTemp() {
    String isSql = "delete crd_ppcard_apply_temp where id_p_seqno =:id_p_seqno ";
    item2ParmStr("id_p_seqno");
    sqlExec(isSql);
    if (sqlRowNum <= 0)
      errmsg("delete crd_ppcard_apply_temp error !");
    return rc;
  }

  int insertCrdPPCardApplyTemp(int ii) {
    String isSql = "insert into crd_ppcard_apply_temp ( " + "id_p_seqno, " + "seqno, " + "card_no, "
        + "card_type, " + "group_code, " + "sup_flag, " + "db_apply_flag, " + "bin_type, "
        + "issue_date, " + "db_cnt, " + "db_amt, " + "db_amt_last, " + "major_flag, "
        + "db_cond_amt_fir, " + "wk_cond_cnt_fir, " + "wk_cond_amt, " + "wk_cond_cnt, "
        + "wk_cond_amt_last, " + "tt_card_purch, " + "card_purch_code, " + "db_errmsg, "
        + "db_err_type, " + "db_err_text, " + "mod_user, " + "mod_time, " + "mod_pgm, "
        + "mod_seqno " + ") values ( " + ":id_p_seqno, " + ":seqno, " + ":card_no, "
        + ":card_type, " + ":group_code, " + ":sup_flag, " + ":db_apply_flag, " + ":bin_type, "
        + ":issue_date, " + ":db_cnt, " + ":db_amt, " + ":db_amt_last, " + ":major_flag, "
        + ":db_cond_amt_fir, " + ":wk_cond_cnt_fir, " + ":wk_cond_amt, " + ":wk_cond_cnt, "
        + ":wk_cond_amt_last, " + ":tt_card_purch, " + ":card_purch_code, " + ":db_errmsg, "
        + ":db_err_type, " + ":db_err_text, " + ":mod_user, " + "sysdate, " + ":mod_pgm, " + "'1' "
        + ")";
    setString("id_p_seqno", wp.colStr(ii, "id_p_seqno"));
    setString("seqno", wp.colStr(ii, "seqno"));
    setString("card_no", wp.colStr(ii, "card_no"));
    setString("card_type", wp.colStr(ii, "card_type"));
    setString("group_code", wp.colStr(ii, "group_code"));
    setString("sup_flag", wp.colStr(ii, "sup_flag"));
    setString("db_apply_flag", wp.colStr(ii, "db_apply_flag"));
    setString("bin_type", wp.colStr(ii, "bin_type"));
    setString("issue_date", wp.colStr(ii, "issue_date"));
    setDouble("db_cnt", wp.colNum(ii, "db_cnt"));
    setDouble("db_amt", wp.colNum(ii, "db_amt"));
    setDouble("db_amt_last", wp.colNum(ii, "db_amt_last"));
    setString("major_flag", wp.colStr(ii, "major_flag"));
    setDouble("db_cond_amt_fir", wp.colNum(ii, "db_cond_amt_fir"));
    setDouble("wk_cond_cnt_fir", wp.colNum(ii, "wk_cond_cnt_fir"));
    setDouble("wk_cond_amt", wp.colNum(ii, "wk_cond_amt"));
    setDouble("wk_cond_cnt", wp.colNum(ii, "wk_cond_cnt"));
    setDouble("wk_cond_amt_last", wp.colNum(ii, "wk_cond_amt_last"));
    setDouble("tt_card_purch", wp.colNum(ii, "tt_card_purch"));
    setString("card_purch_code", wp.colStr(ii, "card_purch_code"));
    setString("db_errmsg", wp.colStr(ii, "db_errmsg"));
    setString("db_err_type", wp.colStr(ii, "db_err_type"));
    setString("db_err_text", wp.colStr(ii, "db_err_text"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    sqlExec(isSql);
    if (sqlRowNum <= 0) {
//pp卡與龍騰卡會有重複的資料，故不須跳error(20230424 Wilson)    	
//      errmsg("insert crd_ppcard_apply_temp error !");
      return rc;
    }
    return rc;
  }

}
