/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-28   V1.00.02 Justin         parameterize sql
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ichr0050 extends BaseAction implements InfacePdf {

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
    if (chkStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2")) == false) {
      alertErr2("自動加值日期:起迄錯誤");
      return;
    }

    if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_crt_date")) {
      alertErr2("自動加值日期/請款日期  不可空白");
      return;
    }

    String lsWhere =
        " where 1=1 " + sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "txn_date")
            + sqlCol(wp.itemStr2("ex_crt_date"), "crt_date");

    if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += " and card_no in "
          + " (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno "
          + " where 1=1 " + sqlCol(wp.itemStr2("ex_idno"), "B.id_no") + " ) ";
    } else if (wp.itemEmpty("ex_card_no") == false) {
      lsWhere += sqlCol(wp.itemStr2("ex_card_no"), "card_no", "like%");
    } else if (wp.itemEmpty("ex_ich_card_no") == false) {
      lsWhere += sqlCol(wp.itemStr2("ex_ich_card_no"), "card_no", "like%");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " txn_date ," + " uf_card_name(card_no) as chi_name ,"
        + " uf_idno_id(card_no) as id_no ," + " ich_card_no ," + " card_no ," + " txn_time ,"
        + " txn_amt ," + " store_cd as addr_cd ," + " crt_date , "
        + " uf_hi_idno(uf_idno_id(card_no)) as hh_idno , "
        + " uf_hi_cname(uf_card_name(card_no)) as hh_chi_name , "
        + " uf_hi_cardno(card_no) as hh_card_no ";
    wp.daoTable = " ich_a07b_add ";
    wp.whereOrder = " order by txn_date Desc ";

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
    wp.reportId = "ichr0050";
    wp.pageRows = 9999;
    String tmpStr = "";
    tmpStr = "自動加值日期:" + commString.strToYmd(wp.itemStr2("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr2("ex_date2")) + "  請款日期:"
        + commString.strToYmd(wp.itemStr2("ex_crt_date"));
    wp.colSet("cond1", tmpStr);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ichr0050.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
