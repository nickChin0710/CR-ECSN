package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*/

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar0140 extends BaseAction implements InfacePdf {

  @Override
  public void userAction() throws Exception {
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
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("日期起迄：輸入錯誤");
      return;
    }

    int lmAmt1 = 0, lmAmt2 = 0;
    lmAmt1 = (int) wp.itemNum("ex_amt1");
    lmAmt2 = (int) wp.itemNum("ex_amt2");


    String lsWhere = " where 1=1 and auth_unit = 'K' and curr_otb_amt < 0 and cacu_amount = 'Y' "
        + sqlCol(wp.itemStr("ex_date1"), "tx_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "tx_date", "<=")
        + sqlCol(wp.itemStr("ex_apr_user"), "apr_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_user"), "auth_user", "like%");

    if (lmAmt1 != 0)
      lsWhere += sqlCol(intToStr(lmAmt1),"curr_tot_unpaid",">=");
    if (lmAmt2 != 0)
    	lsWhere += sqlCol(intToStr(lmAmt2),"curr_tot_unpaid","<=");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " card_no ," + " tx_date ," + " tx_time ," + " auth_no ," + " mcht_no ,"
        + " mcc_code ," + " class_code ," + " eff_date_end ," + " auth_status_code ,"
        + " auth_user ," + " apr_user ," + " nt_amt ," + " curr_otb_amt ," + " card_acct_idx ,"
        + " curr_tot_std_amt ," + " iso_resp_code ," + " stand_in ," + " tx_cvc2 ," + " risk_type ,"
        + " auth_remark ," + " auth_unit ," + " cacu_amount ," + " curr_tot_unpaid ";
    wp.daoTable = "cca_auth_txlog";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryReadAfter();
    wp.setPageValue();
  }

  void queryReadAfter() {
    double liOtbAmt = 0, liStdAmt = 0;
    double liOverPct = 0.0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String sql1 =
          " select " + " ccas_mcode " + " from cca_card_acct " + " where card_acct_idx = ? ";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_acct_idx")});
      if (sqlRowNum <= 0) {
        wp.colSet(ii, "ccas_mcode", "");
      } else
        wp.colSet(ii, "ccas_mcode", sqlStr("ccas_mcode"));


      // -- 計算 OTB/LOC

      if (wp.colNum(ii, "curr_otb_amt") == 0) {
        liOtbAmt = 0;
      } else {
        liOtbAmt = wp.colNum(ii, "curr_otb_amt");
      }

      if (wp.colNum(ii, "curr_tot_std_amt") == 0) {
        liStdAmt = 1;
      } else {
        liStdAmt = wp.colNum(ii, "curr_tot_std_amt");
      }

      liOverPct = liOtbAmt * 100 / liStdAmt;
      wp.colSet(ii, "over_pct", "" + liOverPct);



    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "Ccar0140";

    String cond1 = "日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2")) + " 總未付金額為 :" + wp.itemNum("ex_amt1") + " 萬   --  "
        + wp.itemNum("ex_amt2") + " 萬";;
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ccar0140.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

}
