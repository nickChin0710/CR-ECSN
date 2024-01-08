/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-30  V1.00.00  ryan       program initial                            *
* 109-01-02  V1.00.01  JustinWu       updated for archit.  change               
*  109-05-06  V1.00.02  shiyuqi      updated for project coding standard      *           
* 109-06-29  V1.00.03  Zuwei        fix code scan issue
* 109-12-23   V1.00.04 Justin         parameterize sql
******************************************************************************/
package crdq01;


import java.util.Locale;
import java.util.regex.Pattern;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Crdq0031 extends BaseProc {
  int rr = -1;
  String msg = "", inQty = "", outQty = "";
  String lsKey1 = "", lsKey2 = "", lsKey3 = "", lsNextYY = "";
  int ilOk = 0;
  int ilErr = 0;
  double ldQty = 0, ldTmp = 0;
  Pattern pattern = Pattern.compile("[0-9]*");

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        dataProcess();
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
      // case "A":
      // /* 新增功能 */
      // saveFunc();
      // break;
      // case "U":
      // /* 更新功能 */
      // saveFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // saveFunc();
      // break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "AJAX":
        // AJAX
        itemchanged();
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_group_code");
      dddwList("dddw_group_code", " ptr_group_code ", "group_code", "",
          " where 1=1  group by group_code");
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_card_type");
      dddwList("dddw_card_type", " ptr_card_type ", "card_type", "",
          " where 1=1  group by card_type");
    } catch (Exception ex) {
    }

  }

  private int getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    String lsDate1 = String.format("%-10s", wp.itemStr("ex_seqnos")).replace(" ", "0");
    String lsDate2 = String.format("%-10s", wp.itemStr("ex_seqnoe")).replace(" ", "9");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[流水號區間-起迄]  輸入錯誤");
      return -1;
    }

    if (!empty(wp.itemStr("ex_card_type"))) {
      wp.whereStr += " and card_type = :ex_card_type ";
      setString("ex_card_type", wp.itemStr("ex_card_type"));
    }
    if (!empty(wp.itemStr("ex_group_code"))) {
      wp.whereStr += " and group_code = :ex_group_code ";
      setString("ex_group_code", wp.itemStr("ex_group_code"));

    }
    if (!empty(wp.itemStr("ex_bin_no"))) {
      wp.whereStr += " and bin_no = :ex_bin_no ";
      setString("ex_bin_no", wp.itemStr("ex_bin_no"));
    }

    if (!empty(wp.itemStr("ex_seqnos"))) {
      wp.whereStr += " and seqno >= :ex_seqnos ";
      setString("ex_seqnos", lsDate1);
      wp.colSet("ex_seqnos", lsDate1);
    }

    if (!empty(wp.itemStr("ex_seqnoe"))) {
      wp.whereStr += " and seqno <= :ex_seqnoe ";
      setString("ex_seqnoe", lsDate2);
      wp.colSet("ex_seqnoe", lsDate2);
    }

    // if (!empty(wp.item_ss("ex_reserve"))) {
    // wp.whereStr += " and decode(reserve,'','N',reserve) = :ex_reserve ";
    // setString("ex_reserve", wp.item_ss("ex_reserve"));
    // }
    wp.whereStr += " and reserve = 'Y'";


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

    wp.selectSQL = " hex(rowid) as rowid, " + " bin_no, " + " seqno, " + " group_code, "
        + " card_type, " + " decode(reserve,'','N',reserve) as  reserve ";

    wp.daoTable = "crd_seqno_log";
    wp.whereOrder = "order by bin_no,seqno,group_code,card_type";
    if (getWhereStr() != 1)
      return;
    // System.out.println("select "+wp.selectSQL+" from "+wp.daoTable+" "+wp.whereStr+"
    // "+wp.whereOrder);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      return;
    }

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

  }

  @Override
  public void dataProcess() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // -- Check 卡號之檢查碼--
  public void itemchanged() throws Exception {

    // super.wp = wr;
    String lsCardno = "", lsCardtype = "", exCkno = "";
    lsCardno = wp.itemStr("data_card_no");
    if (empty(lsCardno)) {
      return;
    }
    if (lsCardno.length() != 10 && lsCardno.length() != 14 && lsCardno.length() != 15
        && lsCardno.length() != 16) {
      alertErr("Error,卡號輸入錯誤~");
      return;
    }

    lsCardtype = ofGetCardtype(lsCardno);
    lsCardtype = (lsCardtype.charAt(0) + "").toUpperCase(Locale.TAIWAN);

    switch (lsCardtype) {
      case "N":// --聯合信用卡
        exCkno = ofGetCkcodeNC(lsCardno);
        wp.addJSON("ex_ckno", exCkno);
        break;
      case "V": // --Visa
        exCkno = ofGetCkcodeVM(lsCardno);
        wp.addJSON("ex_ckno", exCkno);
        break;
      case "M": // --mastercard
        exCkno = ofGetCkcodeVM(lsCardno);
        wp.addJSON("ex_ckno", exCkno);
        break;
      case "J": // --jcb
        exCkno = ofGetCkcodeJCB(lsCardno);
        wp.addJSON("ex_ckno", exCkno);
        break;
      case "A":
        exCkno = ofGetCkcodeAE(lsCardno);
        wp.addJSON("ex_ckno", exCkno);
        break;
      default:
        alertErr("Error,非本行發行之信用卡 !");
    }
    return;
  }

  public String ofGetCardtype(String asCardno) {

    String lsVal = "", sVal2 = "";
    if (empty(asCardno))
      return "";

    lsVal = asCardno.substring(0, 6);
    if (empty(lsVal))
      return "";
    String sql = "select bin_type from ptr_bintable where bin_no = ? ";
    setString(lsVal);
    sqlSelect(sql);
    sVal2 = sqlStr("bin_type");

    if (sqlRowNum <= 0 || empty(sVal2)) {
      sVal2 = "";
    }
    sVal2 = sVal2.toUpperCase(Locale.TAIWAN);
    if (!empty(sVal2))
      return sVal2;

    return "";
  }

  // --Get 卡號之檢查碼 for 聯合信用卡--
  public String ofGetCkcodeNC(String asCardno) {

    String lsA = "", lsC1 = "";
    asCardno = asCardno.replaceAll("\\s+", "");
    asCardno = asCardno.substring(0, 4);
    if (!asCardno.equals("4000")) {
      alertErr("不是聯合信用卡, 無法取用檢查碼");
      return "";
    }
    // --check bytes--
    if (asCardno.length() < 14) {
      alertErr("卡號位數小 14, 無法取用檢查碼");
      return "";
    }
    long[] llA = {0, 0, 0, 0, 7, 3, 5, 6, 7, 9, 8, 5, 9, 7, 0, 0};
    long llVal = 0;
    for (int i = 4; i <= 13; i++) {
      llVal += (int) this.toNum(asCardno.charAt(i) + "") * llA[i];
    }
    lsA = String.format("%03d", llVal);
    llVal = (int) this.toNum(lsA.charAt(0) + "") * 5;
    llVal += (int) this.toNum(lsA.charAt(1) + "") * 9;
    llVal += (int) this.toNum(lsA.charAt(2) + "") * 7;
    long llD2 = ((llVal % 10) + 3) % 10;
    if (llD2 == 0) {
      lsC1 = "0";
    } else {
      lsC1 = (10 - llD2) + "";
    }

    return lsC1;
  }

  // --Get 卡號之檢查碼 for Visa & MasterCard --
  public String ofGetCkcodeVM(String asCardno) {

    String lsA = "", lsC2 = "";
    asCardno = asCardno.replaceAll("\\s+", "");

    // --check bytes--
    if (asCardno.length() < 15) {
      alertErr("卡號位數小 15, 無法取用檢查碼");
      return "";
    }
    long[] llA = {2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0};
    long llVal = 0;
    for (int i = 0; i <= 14; i++) {
      lsA = String.format("%02d", (int) this.toNum(asCardno.charAt(i) + "") * llA[i]);
      llVal += (int) this.toNum(lsA.charAt(0) + "") + (int) this.toNum(lsA.charAt(1) + "");
    }
    llVal = llVal % 10;
    if (llVal == 0) {
      lsC2 = "0";
    } else {
      lsC2 = (10 - llVal) + "";
    }

    return lsC2;
  }

  // --Get 卡號之檢查碼 for JCB --
  public String ofGetCkcodeJCB(String asCardno) {

    String lsA = "", lsC2 = "";
    asCardno = asCardno.replaceAll("\\s+", "");

    // --check bytes--
    if (asCardno.length() < 15) {
      alertErr("卡號位數小 15, 無法取用檢查碼");
      return "";
    }
    long[] llA = {2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0};
    long llVal = 0;
    for (int i = 0; i <= 14; i++) {
      lsA = String.format("%02d", (int) this.toNum(asCardno.charAt(i) + "") * llA[i]);
      llVal += (int) this.toNum(lsA.charAt(0) + "") + (int) this.toNum(lsA.charAt(1) + "");
    }
    llVal = llVal % 10;
    if (llVal == 0) {
      lsC2 = "0";
    } else {
      lsC2 = (10 - llVal) + "";
    }

    return lsC2;
  }

  // --Get 卡號之檢查碼 for JCB --
  public String ofGetCkcodeAE(String asCardno) {

    String lsC4 = "", lsMutiple = "";
    int remainderValue = 0;
    long mutipleValue = 0, totalValue1 = 0, totalValue2 = 0, totalSum = 0, nearSumValue = 0;
    asCardno = asCardno.replaceAll("\\s+", "");
    // --check bytes--
    if (asCardno.length() < 14) {
      alertErr("卡號位數小 14, 無法取用檢查碼");
      return "";
    }
    for (int i = 1; i <= asCardno.length(); i += 2) {
      mutipleValue = (int) this.toNum(asCardno.charAt(i) + "");
      totalValue1 = totalValue1 + mutipleValue;
    }
    for (int i = 2; i <= asCardno.length(); i += 2) {
      mutipleValue = (int) this.toNum(asCardno.charAt(i) + "") * 2;
      if (mutipleValue >= 10) {
        lsMutiple = mutipleValue + "";
        for (int j = 1; j <= lsMutiple.length(); j++) {
          totalValue2 = totalValue2 + (int) this.toNum(lsMutiple.charAt(j) + "");
        }
      } else {
        totalValue2 = totalValue2 + mutipleValue;
      }
    }
    totalSum = totalValue1 + totalValue2;
    remainderValue = (int) totalSum % 10;
    if (remainderValue == 0) {
      lsC4 = "";
    } else {
      nearSumValue = (totalSum - remainderValue) + 10;
      lsC4 = (nearSumValue - totalSum) + "";
    }

    return lsC4;

  }

}
