/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ptrm02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Ptrp0312 extends BaseProc {
  Ptrp0312Func func;
  int ilOk = 0;
  int ilErr = 0;
  String acctType = "", classCode = "", aprFlag = "";

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
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      detl2Read();
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
      dataProcess();
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ptrp0312")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

  void listWkdata() {
    int cond1 = 0, cond2 = 0;
    String tmpStr = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {

      // --持卡年限<br>between X1 and X2: [>1 and <=5]--
      cond1 = (int) wp.colNum(ii, "card_year_cond1");
      cond2 = (int) wp.colNum(ii, "card_year_cond2");
      if (cond1 > 0 || cond2 > 0) {
        tmpStr = "";
        if (cond1 > 0)
          tmpStr += cond2Str(cond1) + wp.colStr("card_year_x1") + " ";
        if (cond2 > 0) {
          if (!empty(tmpStr))
            tmpStr += " and ";
          tmpStr += cond2Str(cond2) + wp.colStr(ii, "card_year_x2");
        }
        wp.colSet(ii, "wk_card_year", tmpStr);
      }
      // 近 N1 個月內有<br>M_code?N2: [2] [>=12]
      cond1 = (int) wp.colNum(ii, "mcode_cond1");
      if (wp.colNum(ii, "mcode_n2") != 0) {
        tmpStr = wp.colStr(ii, "mcode_n1") + "";
        wp.colSet(ii, "wk_mcode_mm", tmpStr);
        tmpStr = cond2Str(cond1) + " " + wp.colStr(ii, "mcode_n2");
        wp.colSet(ii, "wk_mcode", tmpStr);
      }
      // 最近M_code?N3<br>且本金結欠>=N31
      cond1 = (int) wp.colNum(ii, "curr_mcode_cond1");
      if (cond1 > 0) {
        tmpStr = "" + cond2Str(cond1) + wp.colStr(ii, "curr_mcode_n3");
        wp.colSet(ii, "wk_curr_mcode", tmpStr);
        tmpStr = "" + commString.numFormat(wp.colNum(ii, "curr_mcode_amt"), "#,##0");
        wp.colSet(ii, "wk_curr_mcode_amt", tmpStr);
      }
      // ----RC rate between N4 N5----
      cond1 = (int) wp.colNum(ii, "rc_rate_cond1");
      cond2 = (int) wp.colNum(ii, "rc_rate_cond2");
      if (cond1 > 0 || cond2 > 0) {
        tmpStr = "";
        if (cond1 > 0) {
          tmpStr += cond2Str(cond1) + wp.colStr(ii, "rc_rate_n4") + "% ";
        }
        if (cond2 > 0) {
          if (!empty(tmpStr))
            tmpStr += " and ";
          tmpStr += cond2Str(cond2) + wp.colStr(ii, "rc_rate_n5") + "%";
        }
        wp.colSet(ii, "wk_rc_rate", tmpStr);
      }
      // --額度<br>between N4 and N5--
      cond1 = (int) wp.colNum(ii, "credit_limit_cond1");
      cond2 = (int) wp.colNum(ii, "credit_limit_cond2");
      if (cond1 > 0 || cond2 > 0) {
        tmpStr = "";
        if (cond1 > 0)
          tmpStr += " " + cond2Str(cond1) + commString.numFormat(wp.colNum(ii, "credit_limit_n6"), "#,##0");
        if (cond2 > 0) {
          if (!empty(tmpStr))
            tmpStr += " and ";
          tmpStr += " " + cond2Str(cond2) + commString.numFormat(wp.colNum(ii, "credit_limit_n7"), "#,##0");
        }
        wp.colSet(ii, "wk_credit_limit", tmpStr);
      }
      // --近 N8 月平均額度動用率<br>between N9 and N10--
      cond1 = (int) wp.colNum(ii, "limit_use_cond1");
      cond2 = (int) wp.colNum(ii, "limit_use_cond2");
      if (wp.colNum(ii, "limit_use_n8") > 0) {
        tmpStr = wp.colStr(ii, "limit_use_n8") + "";
        wp.colSet(ii, "wk_limit_use_mm", tmpStr);
      }
      if (cond1 > 0 || cond2 > 0) {
        tmpStr = "";
        if (cond1 > 0)
          tmpStr += " " + cond2Str(cond1) + wp.colStr(ii, "limit_use_n9") + "%";
        if (cond2 > 0) {
          if (!empty(tmpStr))
            tmpStr += " and ";
          tmpStr += " " + cond2Str(cond2) + wp.colStr(ii, "limit_use_n10") + "%";
        }
        wp.colSet(ii, "wk_limit_use", tmpStr);
      }
      // --近 N11(M - N) 月累積消費<br>between N12 and N13<br>(不含MODEL II)--
      if (wp.colNum(ii, "dest_amt_n11") > 0 || wp.colNum(ii, "dest_amt_n11e") > 0) {
        tmpStr = wp.colStr(ii, "dest_amt_n11") + "-" + wp.colStr(ii, "dest_amt_n11e") + "";
        wp.colSet(ii, "wk_dest_amt_n11", tmpStr);
      }
      cond1 = (int) wp.colNum(ii, "dest_amt_cond1");
      cond2 = (int) wp.colNum(ii, "dest_amt_cond2");
      if (cond1 > 0 || cond2 > 0) {
        tmpStr = "";
        if (cond1 > 0)
          tmpStr += " " + cond2Str(cond1) + wp.colStr(ii, "dest_amt_n12");
        if (cond2 > 0) {
          if (!empty(tmpStr))
            tmpStr += " and ";
          tmpStr += " " + cond2Str(cond2) + wp.colStr(ii, "dest_amt_n13");
        }
        wp.colSet(ii, "wk_dest_amt", tmpStr);
      }
      // --近 X3 月Payment Ratio<br>between X4 and X5 --
      cond1 = (int) wp.colNum(ii, "pay_ratio_cond1");
      cond2 = (int) wp.colNum(ii, "pay_ratio_cond2");
      if (wp.colNum(ii, "pay_ratio_x3") > 0) {
        tmpStr = wp.colStr(ii, "pay_ratio_x3") + "";
        wp.colSet(ii, "wk_pay_ratio_x3", tmpStr);
      } else {
        wp.colSet(ii, "wk_pay_ratio_x3", " ");
      }
      if (cond1 > 0 || cond2 > 0) {
        tmpStr = "";
        if (cond1 > 0)
          tmpStr += " " + cond2Str(cond1) + wp.colStr(ii, "pay_ratio_x3") + "%";
        if (cond2 > 0) {
          if (!empty(tmpStr))
            tmpStr += " and ";
          tmpStr += " " + cond2Str(cond2) + wp.colStr(ii, "pay_ratio_x4") + "%";
        }
        wp.colSet(ii, "wk_pay_ratio", tmpStr);
      }

      if (wp.colEq(ii, "pd_rating_cond1", "0")) {
        wp.colSet(ii, "tt_pd_rating_cond1", "--");
      } else if (wp.colEq(ii, "pd_rating_cond1", "1")) {
        wp.colSet(ii, "tt_pd_rating_cond1", "指定");
      } else if (wp.colEq(ii, "pd_rating_cond1", "2")) {
        wp.colSet(ii, "tt_pd_rating_cond1", "排除");
      } else {
        wp.colSet(ii, "tt_pd_rating_cond1", wp.colStr(ii, "pd_rating_cond1"));
      }

      if (wp.colEq(ii, "vip_code_cond", "0")) {
        wp.colSet(ii, "tt_vip_code_cond", "--");
      } else if (wp.colEq(ii, "vip_code_cond", "1")) {
        wp.colSet(ii, "tt_vip_code_cond", "指定");
      } else if (wp.colEq(ii, "vip_code_cond", "2")) {
        wp.colSet(ii, "tt_vip_code_cond", "排除");
      } else {
        wp.colSet(ii, "tt_vip_code_cond", wp.colStr(ii, "vip_code_cond"));
      }
    }
  }

  String cond2Str(int kk) {
    if (kk < 0 || kk > 5)
      return "";

    String[] aa_code = {"", "=", ">", ">=", "<", "<="};
    return aa_code[kk];
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 and apr_flag<>'Y' " + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
        + sqlCol(wp.itemStr("ex_user_id"), "mod_user")
    // +sql_col(wp.loginUser,"mod_user","<>")
    ;

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "acct_type, " + "class_code, " + "cond_type, " + "check_seqno, "
        + "card_year_x1, " + "card_year_cond1, " + "card_year_x2, " + "card_year_cond2, "
        + "mcode_n1, " + "mcode_cond1, " + "mcode_n2, " + "curr_mcode_cond1, " + "curr_mcode_n3, "
        + "rc_rate_cond1, " + "rc_rate_n4, " + "rc_rate_cond2, " + "rc_rate_n5, "
        + "credit_limit_cond1, " + "credit_limit_n6, " + "credit_limit_cond2, "
        + "credit_limit_n7, " + "limit_use_n8, " + "limit_use_cond1, " + "limit_use_n9, "
        + "limit_use_cond2, " + "limit_use_n10, " + "dest_amt_n11, " + "dest_amt_cond1, "
        + "dest_amt_n12, " + "dest_amt_cond2, " + "dest_amt_n13, " + "pay_ratio_x3, "
        + "pay_ratio_cond1, " + "pay_ratio_x4, " + "pay_ratio_cond2, " + "pay_ratio_x5, "
        + "mcc_code_x6, " + "crt_user, " + "crt_date, " + "apr_user, " + "apr_date, " + "apr_flag, "
        + "mod_user, " + "mod_seqno, " + "curr_mcode_cond2, " + "curr_mcode_amt, "
        + "pd_rating_cond1, " + "pd_rating_cond2, " + "vip_code_cond, " + "pd_rating_mm2, "
        + "dest_amt_n11e";
    wp.daoTable = "ptr_class_code2";
    wp.whereOrder = " order by class_code ";
    pageQuery();
    listWkdata();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    acctType = itemKk("data_k1");
    classCode = itemKk("data_k2");
    aprFlag = itemKk("data_k3");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "acct_type, " + "class_code, "
        + "cond_type, " + "check_seqno, " + "card_year_x1, " + "card_year_cond1, "
        + "card_year_x2, " + "card_year_cond2, " + "mcode_n1, " + "mcode_cond1, " + "mcode_n2, "
        + "curr_mcode_cond1, " + "curr_mcode_n3, " + "rc_rate_cond1, " + "rc_rate_n4, "
        + "rc_rate_cond2, " + "rc_rate_n5, " + "credit_limit_cond1, " + "credit_limit_n6, "
        + "credit_limit_cond2, " + "credit_limit_n7, " + "limit_use_n8, " + "limit_use_cond1, "
        + "limit_use_n9, " + "limit_use_cond2, " + "limit_use_n10, " + "dest_amt_n11, "
        + "dest_amt_cond1, " + "dest_amt_n12, " + "dest_amt_cond2, " + "dest_amt_n13, "
        + "pay_ratio_x3, " + "pay_ratio_cond1, " + "pay_ratio_x4, " + "pay_ratio_cond2, "
        + "pay_ratio_x5, " + "mcc_code_x6, " + "crt_user, " + "crt_date, " + "apr_user, "
        + "apr_date, " + "apr_flag, " + "curr_mcode_cond2, " + "curr_mcode_amt, "
        + "pd_rating_cond1, " + "pd_rating_cond2, " + "vip_code_cond, " + "pd_rating_mm2, "
        + "dest_amt_n11e, " + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_date," + "mod_pgm,"
        + "mod_seqno";
    wp.daoTable = "ptr_class_code2";
    wp.whereStr = " where 1=1" + sqlCol(acctType, "acct_type") + sqlCol(classCode, "class_code")
        + sqlCol(aprFlag, "apr_flag");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + acctType + classCode + aprFlag);
      return;
    }
  }

  void detl2Read() throws Exception {
    acctType = wp.itemStr("acct_type");
    classCode = wp.itemStr("class_code");
    aprFlag = wp.itemNvl("apr_flag", "N");

    log("kk1:" + acctType + " kk2:" + classCode + " kk3:" + aprFlag);
    if (eqIgno(wp.respHtml, "ptrp0312_mcc")) {
      detl2ReadMccCode();
    } else if (eqIgno(wp.respHtml, "ptrp0312_pdrate1")) {
      detl2ReadPdrate1();
    } else if (eqIgno(wp.respHtml, "ptrp0312_pdrate2")) {
      detl2ReadPdrate2();
    } else if (eqIgno(wp.respHtml, "ptrp0312_vip")) {
      detl2ReadVip();
    }
  }

  void detl2ReadMccCode() throws Exception {

    wp.selectSQL = "data_value," + "hex(rowid) as rowid";
    wp.daoTable = "ptr_class_code_dtl";
    wp.whereStr = " where 1=1" + sqlCol("MCC-CODE", "data_type") + sqlCol(acctType, "acct_type")
        + sqlCol(classCode, "class_code") + sqlCol(aprFlag, "uf_nvl(apr_flag,'N')");
    logSql();
    pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
    }
    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  void detl2ReadPdrate1() throws Exception {

    wp.selectSQL = "data_value," + "data_value2," + "data_value3," + "hex(rowid) as rowid";
    wp.daoTable = "ptr_class_code_dtl";
    wp.whereStr = " where 1=1" + sqlCol("PD-RATE1", "data_type") + sqlCol(acctType, "acct_type")
        + sqlCol(classCode, "class_code") + sqlCol(aprFlag, "uf_nvl(apr_flag,'N')");
    pageQuery();
    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  void detl2ReadPdrate2() throws Exception {

    wp.selectSQL = "data_value," + "data_value2," + "data_value3," + "hex(rowid) as rowid";
    wp.daoTable = "ptr_class_code_dtl";
    wp.whereStr = " where 1=1" + sqlCol("PD-RATE2", "data_type") + sqlCol(acctType, "acct_type")
        + sqlCol(classCode, "class_code") + sqlCol(aprFlag, "uf_nvl(apr_flag,'N')");
    pageQuery();
    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  void detl2ReadVip() throws Exception {

    wp.selectSQL = "data_value," + "hex(rowid) as rowid";
    wp.daoTable = "ptr_class_code_dtl";
    wp.whereStr = " where 1=1" + sqlCol("VIP-CODE", "data_type") + sqlCol(acctType, "acct_type")
        + sqlCol(classCode, "class_code") + sqlCol(aprFlag, "uf_nvl(apr_flag,'N')");
    pageQuery();
    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  @Override
  public void dataProcess() throws Exception {
    func = new Ptrp0312Func();
    func.setConn(wp.getConn());
    func.modUser = wp.loginUser;
    func.modPgm = wp.modPgm();

    String[] opt = wp.itemBuff("opt");
    String[] acType = wp.itemBuff("acct_type");
    String[] clCode = wp.itemBuff("class_code");
    wp.listCount[0] = wp.itemRows("class_code");

    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = optToIndex(opt[ii]);
      if (rr < 0) {
        continue;
      }
      optOkflag(rr, 0);
      if (eqIgno(wp.loginUser, wp.colStr(rr, "mod_user"))) {
        optOkflag(rr, -1);
        ilErr++;
        continue;
      }

      func.varsSet("acct_type", acType[rr]);
      func.varsSet("class_code", clCode[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      optOkflag(rr, rc);
      if (rc == 1) {
        ilOk++;
        continue;
      }
      ilErr++;
    }

    // -re-Query-
    // queryRead();
    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

  }
}
