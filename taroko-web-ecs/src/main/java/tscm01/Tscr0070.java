/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 110-01-06  V1.00.03  shiyuqi       修改无意义命名
* 111-04-14  V1.00.01  yangqinkai     TSC畫面整合                             *                                                        *    
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Tscr0070 extends BaseAction implements InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
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
    if (wp.itemEmpty("ex_crt_date1")) {
      alertErr2("處理日期 (起) : 不可空白 !");
      return;
    }

    if (chkStrend(wp.itemStr2("ex_crt_date1"), wp.itemStr2("ex_crt_date2")) == false) {
      alertErr2("處理日期起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 and A.tsc_card_no = B.tsc_card_no and A.tran_code ='7229' "
        + sqlCol(wp.itemStr("ex_crt_date1"), "A.crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "A.crt_date", "<=");


    sum(lsWhere);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "" + " A.tsc_card_no , " + " A.chgback_reason , " + " A.tran_code , " + " A.notify_date , "
            + " A.reference_no , " + " B.tsc_card_no , " + " B.card_no , " + " A.tran_time , "
            + " A.tran_amt , " + " A.traff_subname , " + " A.place_subname , " + " A.tran_date ";
    wp.daoTable = " tsc_ecti_log A , tsc_card B ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
  }

  void sum(String lsWhere) {
	sqlParm.setSqlParmNoClear(true);
    String sql1 = " select " + " count(*) as tl_cnt , " + " sum(A.tran_amt) as tl_amt "
        + " from tsc_ecti_log A , tsc_card B " + lsWhere;
    sqlSelect(sql1);

    wp.colSet("tl_cnt", "" + sqlStr("tl_cnt"));
    wp.colSet("tl_amt", "" + sqlStr("tl_amt"));

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
    wp.reportId = "Rskr9000";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "處理日期: " + commString.strToYmd(wp.itemStr("ex_crt_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_crt_date2"));
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    
    if(sqlNotFind()) {
    	wp.respHtml = "TarokoErrorPDF";
    	return;
    }
    
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "tscr0070.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
