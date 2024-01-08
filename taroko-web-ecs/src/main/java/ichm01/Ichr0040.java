/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-28  V1.00.02  Justin         parameterize sql
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ichm01;
/* 2019-0419:  JH    initial
* */
import ofcapp.InfacePdf;
import taroko.com.TarokoPDFLine;

public class Ichr0040 extends ofcapp.BaseAction implements InfacePdf {
  private String totTable = "", totWhere = "";
  boolean pdf = false;

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "C")) {
    // // -資料處理-
    // procFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdf = true;
      pdfPrint();
    }
  }

  @Override
  public void dddwSelect() {

  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_date1")) {
      alertErr2("餘額返還日期: 不可空白");
      return;
    }
    if (chkStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2")) == false) {
      alertErr2("餘額返還日期: 起迄錯誤");
      return;
    }

    String ls_where =
        " where 1=1" + sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "A.crt_date")
            + sqlCol(wp.itemStr2("ex_idno"), "B.id_no", "like%")
            + sqlCol(wp.itemStr2("ex_card_no"), "D.card_no", "like%")
            + sqlCol(wp.itemStr2("ex_ich_cardno"), "A.ich_card_no", "like%");

    wp.whereStr = ls_where;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    totWhere = ls_where;
    
    sqlParm.setSqlParmNoClear(true);
    queryRead();
    if (rc != 1)
      return;

    if (!eqIgno(strAction, "PDF"))
      queryRead_After();
    
    sqlParm.clear();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        " C.card_no" + ", A.ich_card_no, A.txn_date, A.txn_amt, A.crt_date, A.balance_amt"
            + ", B.id_no, B.chi_name" + ", A.merchine_type as txn_type";
    // if (eq_igno(is_action,"PDF")) {
    // wp.selectSQL +=", uf_hi_cardno(C.card_no) as hh_card_no"+
    // ", uf_hi_idno(B.id_no) as hh_id_no"+
    // ", uf_hi_cname(B.chi_name) as hh_chi_name";
    // }

    wp.daoTable = " ich_a09b_bal A join ich_card D on A.ich_card_no=D.ich_card_no"
        + " join crd_card C on C.card_no=D.card_no"
        + " join crd_idno B on B.id_p_seqno=C.id_p_seqno";
    totTable = wp.daoTable;

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
  }

  private void queryRead_After() {
    String sql1 = "select sum(A.balance_amt) as tot_balance_amt" + ", sum(A.txn_amt) as tot_txn_amt"
        + " from " + totTable + " " + totWhere;
    sqlSelect(sql1);
    wp.colSet("tot_balance_amt", sqlNum("tot_balance_amt"));
    wp.colSet("tot_txn_amt", sqlNum("tot_txn_amt"));
  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

  }

  @Override
  public void procFunc() throws Exception {

  }

  @Override
  public void initButton() {

  }

  @Override
  public void initPage() {

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "ichr0040";
    wp.pageRows = 9999;

    String tmpStr = "";
    tmpStr = "餘額返還日期:" + commString.strToYmd(wp.itemStr2("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr2("ex_date2"));
    wp.colSet("cond1", tmpStr);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDFLine pdf = new TarokoPDFLine();
    pdf.verticalPrint(0);
    // pdf.pageVert =true;
    // pdf.pageCount =52; //35,52
    wp.fileMode = "Y";
    pdf.excelTemplate = "ichr0040.xlsx";
    // pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }
}
