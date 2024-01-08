/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-24  V1.00.00  David FU   program initial                            *
* 109-01-07  V1.00.01  Ru Chen    modify AJAX                                *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-07-24  V1.00.03  Wilson     卡片號用途註記修改                                                                                    *
******************************************************************************/

package crdm01;

import java.text.DecimalFormat;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0020 extends BaseEdit {
  String mExTransNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      wp.colSet("trans_no", getTransNo());
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
      wp.colSet("trans_no", getTransNo());
    }
    // 20200107 modify AJAX
    else if (eqIgno(wp.buttonCode, "AJAX")) {
      itemchanged();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_card_flag")) == false) {
      wp.whereStr += " and  card_flag = :card_flag ";
      setString("card_flag", wp.itemStr("ex_card_flag"));
    }
    if (empty(wp.itemStr("ex_trans_date")) == false) {
      wp.whereStr += " and  trans_date >= :trans_date ";
      setString("trans_date", wp.itemStr("ex_trans_date"));
    }
    if (empty(wp.itemStr("ex_trans_date2")) == false) {
      wp.whereStr += " and  trans_date <= :trans_date2 ";
      setString("trans_date2", wp.itemStr("ex_trans_date2"));
    }
    if (empty(wp.itemStr("ex_group_code")) == false) {
      wp.whereStr += " and  group_code = :group_code ";
      setString("group_code", wp.itemStr("ex_group_code"));
    }
    if (empty(wp.itemStr("ex_card_type")) == false) {
      wp.whereStr += " and  card_type = :card_type ";
      setString("card_type", wp.itemStr("ex_card_type"));
    }
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();

    if (empty(wp.itemStr("ex_group_code")) && empty(wp.itemStr("ex_card_type"))
        && empty(wp.itemStr("ex_trans_date")) && empty(wp.itemStr("ex_trans_date2"))) {
      alertErr("請至少輸入一項查詢條件!!");
      return;
    }

    String lsDate1 = wp.itemStr("ex_trans_date");
    String lsDate2 = wp.itemStr("ex_trans_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[異動日期-起迄]  輸入錯誤");
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " trans_no" + ", trans_date" + ", group_code" + ", card_type" + ", card_flag"
        + ", decode(card_flag,'1','一般用','2','緊急替代用','5','HCE TPAN用','7','BANK PAY',card_flag) as card_desc "
        + ", bin_no" + ", beg_seqno" + ", end_seqno" + ", post_flag" + ", crt_date" + ", crt_user"
        + ", charge_date" + ", charge_id" + ", remark_40" + ", mod_user"
        + ", uf_2ymd(mod_time) as mod_date";

    wp.daoTable = "crd_cardno_range";
    wp.whereOrder = " order by group_code,card_type,beg_seqno";
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
    mExTransNo = wp.itemStr("trans_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mExTransNo)) {
      mExTransNo = itemKk("data_k1");
    }
    if (empty(mExTransNo)) {
      mExTransNo = wp.itemStr("kk_trans_no");
    }
    if (empty(mExTransNo)) {
      mExTransNo = wp.colStr("trans_no");
    }

    if (empty(mExTransNo)) {
      alertErr("無異動流水號");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", trans_no " + ", trans_date"
        + ", group_code" + ", card_type" + ", card_flag" + ", bin_no" + ", beg_seqno"
        + ", end_seqno" + ", post_flag" + ", crt_date" + ", crt_user" + ", charge_date"
        + ", charge_id" + ", remark_40";
    wp.daoTable = "crd_cardno_range";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  trans_no = :trans_no ";
    setString("trans_no", mExTransNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, trans_no=" + mExTransNo);
    }

    String lsSql =
        " select bin_no_2_fm,bin_no_2_to from ptr_bintable where card_type=:card_type and bin_no=:bin_no ";
    setString("card_type", wp.colStr("card_type"));
    setString("bin_no", wp.colStr("bin_no"));
    sqlSelect(lsSql);
    wp.colSet("bin_no_2_fm", sqlStr("bin_no_2_fm"));
    wp.colSet("bin_no_2_to", sqlStr("bin_no_2_to"));
  }

  @Override
  public void saveFunc() throws Exception {

    Crdm0020Func func = new Crdm0020Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }


  @Override
  public void initPage() {

    wp.colSet("trans_date", getSysDate());


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
      /*
       * if (wp.respHtml.indexOf("_detl") > 0) wp.optionKey = wp.item_ss("kk_bin_no");
       * this.dddw_list("dddw_bin_no","ptr_bintable","bin_no","card_desc","where 1=1");
       */

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("card_type");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_card_type");
      }
      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name", "where 1=1 ");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("group_code");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_group_code");
      }
      this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 ");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("bin_no");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("kk_bin_no");
      }
      String lsCardType = wp.colStr("card_type");
      String dddwWhere = "where 1=1 and card_type = '" + lsCardType
          + "' group by bin_no,card_type,bin_no_2_fm,bin_no_2_to order by bin_no ";
      this.dddwList("dddw_bin_no", "ptr_bintable", "bin_no", "bin_no||'_'||bin_no_2_fm||'--'||bin_no_2_to", dddwWhere);

    } catch (Exception ex) {
    }
  }

  // 20200107 modify AJAX
  public int itemchanged() throws Exception {
    // super.wp = wr;
    String option = "", dateCardtype = "", dateBin = "", lsBegSeqno = "", lsEndSeqno = "";
    String ajaxName = "";
    String dddwWhere = "";


    ajaxName = wp.itemStr("ajaxName");
    switch (ajaxName) {

      case "cardtype":
        dateCardtype = wp.itemStr("date_cardtype");
        dddwWhere = " and card_type = :card_type ";
        String lsSql =
            "select bin_no "  + " ,bin_no_2_fm " + " ,bin_no_2_to "
                + " from ptr_bintable " + " where 1=1 " + dddwWhere + " group by bin_no,card_type,bin_no_2_fm,bin_no_2_to order by bin_no ";
        setString("card_type", dateCardtype);
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          break;
        }
        // option += "<option value=\"\">--</option>";
        for (int ii = 0; ii < sqlRowNum; ii++) {
          option += "<option value=\"" + sqlStr(ii, "bin_no") + "\"  ${bin_no-" + sqlStr(ii, "bin_no") + "}>" + sqlStr(ii, "bin_no")+"_"+sqlStr(ii,"bin_no_2_fm")+"--"+sqlStr(ii,"bin_no_2_to")
              + "</option>";
        }
        wp.addJSON("dddw_bin_no2", option);
//        wp.addJSON("a_bin_no_2_fm", sqlStr("bin_no_2_fm"));
//        wp.addJSON("a_bin_no_2_to", sqlStr("bin_no_2_to"));
        break;

      case "seq":
        String lsMsg = "";

        dateBin = wp.itemStr("ls_bin_no");
        lsBegSeqno = wp.itemStr("ls_beg_seqno");
        lsEndSeqno = wp.itemStr("ls_end_seqno");

        if (empty(lsBegSeqno)) {
          lsMsg = "起不能空白!";
          break;
        }

        if (toInt(lsBegSeqno) >= toInt(lsEndSeqno)) {
          lsMsg = "迄不能小於起!";
          break;
        }
        String lsSql3 =
            "select count(*) as qty from crd_prohibit where CARD_NO >= :ls_beg_seqno   and CARD_NO <= :ls_end_seqno  ";
        setString("ls_beg_seqno", dateBin + lsBegSeqno);
        setString("ls_end_seqno", dateBin + lsEndSeqno);
        sqlSelect(lsSql3);
        int qty = (int) sqlNum("qty");
        lsMsg = "區間可用數:" + String.valueOf(toInt(lsEndSeqno) - toInt(lsBegSeqno) - qty + 1);
        wp.addJSON("a_totalCnt", lsMsg);
        break;

    }

    return 1;
  }

  private String getTransNo() {
    String transNo = chineseYMD(getSysDate());
    int len = transNo.length();
    String lsSql =
        "select COALESCE(max(substr(trans_no,?)),0) as tot_cnt from crd_cardno_range where substr(trans_no, 1, ?) = ?";
    Object[] param = new Object[] {len + 1, len, transNo};
    sqlSelect(lsSql, param);
    double totCnt = sqlNum("tot_cnt");
    if (totCnt == 0) {
      transNo = transNo + "01";
    } else {
      totCnt++;
      // 欄位只保留兩碼
      if (totCnt >= 100)
        return "";
      DecimalFormat df = new DecimalFormat("00");
      String cnt = df.format(totCnt);
      transNo = transNo + cnt;
    }

    return transNo;
  }


  private String chineseYMD(String engYYYYMMDD) {
    String yy = Integer.toString(Integer.parseInt(engYYYYMMDD.substring(0, 4)) - 1911);
    String mmdd = engYYYYMMDD.substring(4);
    return yy + mmdd;
  }
}
