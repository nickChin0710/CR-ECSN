/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *                                                                           *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ichr0100 extends BaseAction implements InfacePdf {
  String isClose = "", isReportName = "";

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
    if (wp.itemEmpty("ex_txn_date1")) {
      alertErr2("請輸入 日期期間");
      return;
    }

    if (chkStrend(wp.itemStr2("ex_txn_date1"), wp.itemStr2("ex_txn_date2")) == false) {
      alertErr2("交易日期:起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " 
        + sqlCol(wp.itemStr2("ex_txn_date1"), "A.crt_date", ">=")
        + sqlCol(wp.itemStr2("ex_txn_date2"), "A.crt_date", "<=");


    isReportName = "愛金卡帳務調整明細表";

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL  = " A.ich_card_no, A.adjust_no, A.adjust_rsn, A.adjust_amt, ";
    wp.selectSQL += " A.pre_pay_date, A.pay_vendor, A.sys_date as ich_sys_date, A.sys_time as ich_sys_time, ";
    wp.selectSQL += " B.card_no ";

    wp.daoTable = " ich_a10b_adjust A left join ich_card B on A.ich_card_no = B.ich_card_no ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

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
    wp.reportId = "ichr0100";
    wp.pageRows = 9999;
    String tmpStr = "";
    tmpStr = "處理日期:" + commString.strToYmd(wp.itemStr2("ex_txn_date1")) + " -- "
        + commString.strToYmd(wp.itemStr2("ex_txn_date2")) ;
    wp.colSet("cond1", tmpStr);
    queryFunc();
    wp.colSet("report_name", isReportName);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ichr0100.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
