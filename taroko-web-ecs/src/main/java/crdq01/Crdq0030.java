/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-26  V1.00.00  Andy Liu      program initial                         *
* 106-12-14            Andy		  update : program name : Crdi1301==>Crdq1301*
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      *
* 111-01-19  V1.00.03  machao      系统弱扫： Erroneous String Compare 
******************************************************************************/
package crdq01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdq0030 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdq0030";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
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
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    // String sysdate1="",sysdate0="";
    // sysdate1 = ss_mid(get_sysDate(),0,8);
    // 續卡日期起-迄日
    // wp.col_set("exDateS", "");
    // wp.col_set("exDateE", sysdate1);
  }

  private boolean getWhereStr() throws Exception {

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;
    String exCardNo = wp.itemStr("ex_card_no");
    String exCheckCode = cardChkCode(exCardNo);
    wp.colSet("ex_check_code", exCheckCode);
  }

  void listWkdata() throws Exception {

  }



  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public boolean checkCardNo(String cardNo) throws Exception {
    if (!isNumber(cardNo)) {
      return false;
    } else if (cardNo.length() < 13) {
      return false;
//    } else if (cardNo.substring(0, 1) == "4" && cardNo.length() != 13 && cardNo.length() != 16) {
    } else if ("4".equals(cardNo.substring(0, 1)) && cardNo.length() != 13  && cardNo.length() != 16) {
      return false;
//    } else if ((cardNo.substring(0, 2) == "34" || cardNo.substring(0, 2) == "37")
  	} else if ("34".equals(cardNo.substring(0, 2)) || "37".equals(cardNo.substring(0, 2))
        && cardNo.length() != 15) {
      return false;
    } else if (cardNo.length() != 16) {
      return false;
    }

    int j = 1, ckSum = 0, calc = 0;

    for (int i = cardNo.length() - 1; i >= 0; i--) {
      calc = Integer.parseInt(cardNo.substring(i, i + 1)) * j;
      if (calc > 9) {
        ckSum = ckSum + 1;
        calc = calc - 10;
      }
      ckSum = ckSum + calc;
      if (j == 1) {
        j = 2;
      } else {
        j = 1;
      }
    }

    if (ckSum % 10 != 0) {
      return false;
    }

    return true;
  }

  // ************************************************************************
  public String cardChkCode(String cardNo) throws Exception {

    for (int i = 0; i <= 9; i++) {
      String chkStr = Integer.toString(i);
      String cardNo1 = cardNo + Integer.toString(i);
      if (checkCardNo(cardNo1)) {
        return chkStr;
      }
    }

    return "E";
  }
}

