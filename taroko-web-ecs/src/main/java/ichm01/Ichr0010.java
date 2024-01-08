/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ichr0010 extends BaseAction implements InfacePdf {

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

    if (wp.itemEmpty("ex_crt_date1") && wp.itemEmpty("ex_crt_date2")) {
      alertErr2("處理日期: 不可空白");
      return;
    }

    if (chkStrend(wp.itemStr2("ex_crt_date1"), wp.itemStr2("ex_crt_date2")) == false) {
      alertErr2("處理日期: 起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr2("ex_crt_date1"), "A.crt_date", ">=")
        + sqlCol(wp.itemStr2("ex_crt_date2"), "A.crt_date", "<=");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        "" + " A.crt_date ," + " A.txn_date ," + " A.txn_time ," + " A.txn_amt ," + " A.agency_cd ,"
            + " A.store_cd ," + " A.merchine_type ," + " A.merchine_no ," + " A.ich_card_no ,"
            + " A.resp_code_ich ," + " A.op_mark ," + " A.online_mark ," + " B.card_no ";

    wp.daoTable = " ich_a07b_add A join ich_card B on A.ich_card_no = B.ich_card_no ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
    queryAfter();
  }

  void queryAfter() {
    String wTxnDate = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wTxnDate = commString.strToYmd(wp.colStr(ii, "txn_date")) + "  "
          + commString.mid(wp.colStr(ii, "txn_time"), 0, 2) + ":"
          + commString.mid(wp.colStr(ii, "txn_time"), 2, 2) + ":"
          + commString.mid(wp.colStr(ii, "txn_time"), 4, 2);
      wp.colSet(ii, "wk_txn_date", wTxnDate);
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
    wp.reportId = "ichr0010";
    wp.pageRows = 9999;
    String tmpStr = "";
    tmpStr = "處理日期:" + commString.strToYmd(wp.itemStr2("ex_crt_date1")) + " -- "
        + commString.strToYmd(wp.itemStr2("ex_crt_date2"));
    wp.colSet("cond1", tmpStr);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ichr0010.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
