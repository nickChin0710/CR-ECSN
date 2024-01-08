/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                     
*  110/1/4   V1.00.04  yanghan       修改了變量名稱和方法名稱                *
* 111-01-07  V1.00.05  Justin     fix a bug of SQL Error                     *
******************************************************************************/
package ptrm02;

import java.util.Arrays;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0312 extends BaseEdit {
  Ptrm0312Func func;
  String acctType = "", classCode = "", aprFlag = "";
  String is_data_type = "";

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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      detl2Read();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      /* 存檔 */
      strAction = "C";
      procFunc();
    } else if (eqIgno(wp.buttonCode, "U2")) {
      /* 新增明細功能 */
      insertDetl();
    } else if (eqIgno(wp.buttonCode, "D2")) {
      /* 刪除明細功能 */
      deleteDetl();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      // 共用參數維護-S2 --
      doParmRead();
    } else if (eqIgno(wp.buttonCode, "U3")) {
        // 共用參數維護-U3 --
        doParmSave();
    }
    
    dddwSelect();
    initButton();
  }

  void detl2Read() throws Exception {
    acctType = wp.itemStr("acct_type");
    classCode = wp.itemStr("class_code");
    aprFlag = wp.itemNvl("apr_flag", "N");

    String lsType = "";
    if (wp.respHtml.lastIndexOf("_mcc") > 0) {
      lsType = "MCC-CODE";
      // detl2_Read_mcc_code();
    } else if (wp.respHtml.lastIndexOf("_pdrate1") > 0) {
      lsType = "PD-RATE1";
      // detl2_Read_pdrate1();
    } else if (wp.respHtml.lastIndexOf("_pdrate2") > 0) {
      lsType = "PD-RATE2";
      // detl2_Read_pdrate2();
    } else if (wp.respHtml.lastIndexOf("_vip") > 0) {
      lsType = "VIP-CODE";
      // detl2_Read_vip();
    }

    wp.selectSQL = "data_value," + "data_value2," + "data_value3," + "hex(rowid) as rowid";
    wp.daoTable = "ptr_class_code_dtl";
    wp.whereStr = " where 1=1" + sqlCol(lsType, "data_type") + sqlCol(acctType, "acct_type")
        + sqlCol(classCode, "class_code") + sqlCol(aprFlag, wp.sqlID + "uf_nvl(apr_flag,'N')");
    pageQuery();
    if (this.sqlNotFind()) {
      this.selectOK();
    }
    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ptrm0312")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ptrm0312_detl")) {
        wp.optionKey = wp.colStr(0, "kk_acct_type");
        dddwList("dddw_kk_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }
    // --pdrate1--//
    try {
      if (wp.respHtml.indexOf("_pdrate1") > 0) {
        wp.optionKey = wp.colStr("ex_data_value");
        dddwList("dddw_ex_data_code", "select distinct pdr_type as db_code, pdr_type as db_desc"
            + " from rsk_pdr_class where 1=1");

        wp.optionKey = wp.colStr("ex_data_value2");
        dddwList("dddw_data_risk1", "select distinct pdr_class as db_code, pdr_class as db_desc"
            + " from rsk_pdr_class where 1=1");

        wp.optionKey = wp.colStr("ex_data_value3");
        dddwShare("dddw_data_risk2");
      }
    } catch (Exception ex) {
    }
    // --pdrate2--//
    try {
      if (wp.respHtml.indexOf("_pdrate2") > 0) {
        wp.optionKey = wp.colStr("ex_data_value");
        dddwList("dddw_ex_data_value", "select distinct pdr_type as db_code, pdr_type as db_desc"
            + " from rsk_pdr_class where 1=1");

        wp.optionKey = wp.colStr("ex_data_value2");
        dddwList("dddw_data_value2", "select distinct pdr_class as db_code, pdr_class as db_desc"
            + " from rsk_pdr_class where 1=1");

        wp.optionKey = wp.colStr("ex_data_value3");
        dddwShare("dddw_data_value3");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ptrm0312_vip")) {
        wp.optionKey = wp.colStr(0, "ex_data_value");
        dddwList("d_dddw_vip",
            "select distinct vip_code as db_code , vip_code as db_desc from ptr_vip_code where apr_flag ='Y' ");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    readParm();
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_acct_type"), "acct_type");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  void readParm() {
    String sql1 =
        " select wf_value6 from ptr_sys_parm where wf_parm='w_ptrm0312' and wf_key ='NEWCARD_DAY' ";
    sqlSelect(sql1);

    if (sqlRowNum > 0) {
      wp.colSet("ex_newcard_day", sqlStr("wf_value6"));
    }
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "acct_type, " + "class_code, " + "cond_type, "
        + "decode(cond_type,'1','商戶卡總繳戶','2','VIP','9','一般卡') as tt_cond_type ," + "check_seqno, "
        + "card_year_x1, " + "card_year_cond1, " + "card_year_x2, " + "card_year_cond2, "
        + "mcode_n1, " + "mcode_cond1, " + "mcode_n2, " + "curr_mcode_cond1, " + "curr_mcode_n3, "
        + "rc_rate_cond1, " + "rc_rate_n4, " + "rc_rate_cond2, " + "rc_rate_n5, "
        + "credit_limit_cond1, " + "credit_limit_n6, " + "credit_limit_cond2, "
        + "credit_limit_n7, " + "limit_use_n8, " + "limit_use_cond1, " + "limit_use_n9, "
        + "limit_use_cond2, " + "limit_use_n10, " + "dest_amt_n11, " + "dest_amt_cond1, "
        + "dest_amt_n12, " + "dest_amt_cond2, " + "dest_amt_n13, " + "pay_ratio_x3, "
        + "pay_ratio_cond1, " + "pay_ratio_x4, " + "pay_ratio_cond2, " + "pay_ratio_x5, "
        + "mcc_code_x6, " + "crt_user, " + "crt_date, " + "apr_user, " + "apr_date, " + "apr_flag, "
        + "mod_user, " + "mod_time, " + "mod_pgm, "
        // + "mod_ws, "
        + "mod_seqno, "
        // + "mod_log, "
        + "curr_mcode_cond2, " + "curr_mcode_amt, " + "pd_rating_cond1, " + "pd_rating_cond2, "
        + "vip_code_cond, " + "pd_rating_mm2, " + "dest_amt_n11e";
    wp.daoTable = "ptr_class_code2";
    wp.whereOrder = " order by acct_type , cond_type , check_seqno Asc , class_code ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    pageQuery();
    listWkdata();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
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
          tmpStr += cond2Str(cond1) + wp.colStr(ii, "card_year_x1") + " ";
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

    String[] aaCode = {"", "=", ">", ">=", "<", "<="};
    return aaCode[kk];
  }

  @Override
  public void querySelect() throws Exception {
    acctType = wp.itemStr("data_k1");
    classCode = wp.itemStr("data_k2");
    aprFlag = wp.itemStr("data_k3");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acctType)) {
      acctType = itemKk("acct_type");
    }
    if (empty(classCode)) {
      classCode = itemKk("class_code");
    }
    if (empty(aprFlag)) {
      aprFlag = itemKk("apr_flag");
    }
    // -check-unAppr-
    String lsWhere = " where 1=1" + sqlCol(acctType, "acct_type") + sqlCol(classCode, "class_code");

    String lsSelect = "hex(rowid) as rowid, mod_seqno, " + "acct_type, " + "class_code, "
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

    wp.selectSQL = lsSelect;
    wp.daoTable = "ptr_class_code2";
    wp.whereStr = lsWhere + " and nvl(apr_flag,'x')<>'Y'";
    pageSelect();
    if (sqlNotFind()) {
      wp.selectSQL = lsSelect;
      wp.daoTable = "ptr_class_code2";
      lsWhere = " where 1=1" + sqlCol(acctType, "acct_type") + sqlCol(classCode, "class_code");
      wp.whereStr = lsWhere + " and nvl(apr_flag,'x')='Y'";
      pageSelect();
    }

    if (sqlNotFind()) {
      alertErr("查無資料, key=" + acctType + classCode + aprFlag);
      return;
    }

    // -detl-count-
    dataCountDetl();

    if (rc == 1) {
      if (pos("|A|U", strAction) > 0) {
        wp.colSet("toPage_detl", " || 1==1 ");
      }
    }
  }

  void dataCountDetl() {
    wp.selectSQL = "sum(decode(data_type,'MCC-CODE',1,0)) as wk_detl_mcc, "
        + " sum(decode(data_type,'PD-RATE1',1,0)) as wk_detl_pdrate1, "
        + " sum(decode(data_type,'PD-RATE2',1,0)) as wk_detl_pdrate2, "
        + " sum(decode(data_type,'VIP-CODE',1,0)) as wk_detl_vip ";
    wp.daoTable = "ptr_class_code_dtl";
    wp.whereStr = " where 1=1 " + sqlCol(acctType, "acct_type") + sqlCol(classCode, "class_code")
        + sqlCol(wp.colStr("apr_flag"), "apr_flag");
    pageSelect();
  }

  @Override
  public void saveFunc() throws Exception {
    this.addRetrieve = true;
    this.updateRetrieve = true;

    if (wp.itemEq("apr_flag", "Y") && this.isDelete()) {
      if (this.checkApproveZz() == false)
        return;
    }

    func = new ptrm02.Ptrm0312Func(wp);
    rc = func.dbSave(strAction);
    // ddd(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (rc == 1 && this.pos("|A|U", strAction) > 0) {
      userAction = true;
      dataRead();
    }

  }

  void procFunc() throws Exception {
    func = new ptrm02.Ptrm0312Func(wp);
    rc = func.updatePtrSysParm();
    wp.listCount[0] = wp.itemRows("class_code");
    if (rc == 1) {
      sqlCommit(rc);
      wp.respMesg = "存檔完成";
    } else {
      dbRollback();
      errmsg(func.getMsg());
    }
  }

  @Override
  public void initButton() {
    // wp.ddd("menu-id="+wp.menuSeq);
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
      return;
    }
    if (wp.respHtml.indexOf("_mcc") > 0 || wp.respHtml.indexOf("_pdrate") > 0
        || wp.respHtml.indexOf("_vip") > 0) {
      // ddd("apr_flag="+wp.item_ss("apr_flag"));
      if (wp.itemEq("apr_flag", "Y")) {
        this.btnUpdateOn(false);
      } else {
        this.btnModeUd("1");
      }
    }

  }

  void insertDetl() throws Exception {
    func = new ptrm02.Ptrm0312Func(wp);
    wp.listCount[0] = wp.itemRows("data_value");

    rc = func.insertDetl();
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else
      alertMsg("明細新增完成 請重新讀取");

  }

  void deleteDetl() throws Exception {
    int ilOk = 0, ilErr = 0, ilCnt = 0;
    func = new ptrm02.Ptrm0312Func(wp);
    wp.listCount[0] = wp.itemRows("data_value");
    String[] lsOpt = wp.itemBuff("opt");
    String[] lsDataValue = wp.itemBuff("data_value");
    String[] lsDataValue2 = wp.itemBuff("data_value2");
    String[] lsDataValue3 = wp.itemBuff("data_value3");

    for (int ii = 0; ii < wp.itemRows("data_value"); ii++) {
      if (checkBoxOptOn(ii, lsOpt) == false)
        continue;
      ilCnt++;
      func.varsSet("data_value", lsDataValue[ii]);
      func.varsSet("data_value2", lsDataValue2[ii]);
      func.varsSet("data_value3", lsDataValue3[ii]);

      if (func.deleteDetl() != 1) {
        ilErr++;
        dbRollback();
        wp.colSet(ii, "ok_flag", "X");
        continue;
      } else {
        ilOk++;
        sqlCommit(1);
        wp.colSet(ii, "ok_flag", "V");
        continue;
      }

    }

    if (ilCnt == 0) {
      alertErr2("請選擇欲刪除資料");
      return;
    }

    alertMsg("刪除完成 , 成功:" + ilOk + " 失敗:" + ilErr);
  }
  
  void doParmRead() throws Exception {	  
	  readParm();
	  String ls_acct_type = wp.itemStr("ex_acct_type");
	  String sql1 = "select CHIN_NAME as tt_acct_type from ptr_acct_type where acct_type =?";
	  sqlSelect(sql1, ls_acct_type);
	  if (sqlRowNum > 0) {
	     wp.colSet("tt_acct_type", sqlStr("tt_acct_type"));
	  }
  }
  
  void doParmSave() throws Exception {
	  msgOK();
	  int li_days = (int) wp.itemNum("ex_newcard_day");
	  if (li_days < 0) {		  
		  alertErr2("新卡戶天數, 不可<0");
	      return;
	  }
	  //--
	  if (checkApproveZz() == false) {
	     return;
	  }

	  String sql1 = "update ptr_sys_parm set wf_value6=? where wf_parm='w_ptrm0312' and wf_key='NEWCARD_DAY'";

	  setInt(1, li_days);
	  sqlExec(sql1);
	  if (sqlRowNum <= 0) {		  
		  alertErr2("新卡戶天數, 存檔失敗");
	  }
	  sqlCommit(rc);
	  if (rc == 1) {
	     alertMsg("存檔成功, 主管覆核完成");
	  }
	}
  
}
