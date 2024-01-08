package rskr02;
/** 2019-0621:  JH    p_xxx >>acno_p_xxx
 * 109-04-28  V1.00.01  Tanwei       updated for project coding standard
 * *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
 * */
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Rskr0952 extends BaseAction implements InfaceExcel, InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
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
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_adj_yymm"), "A.adj_yymm")
        + sqlCol(wp.itemStr("ex_adj_loc_flag"), "A.adj_loc_flag");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        " uf_idno_id(A.id_p_seqno) as id_no , A.* , uf_idno_name(A.id_p_seqno) as chi_name ";
    wp.daoTable = " rsk_r001_data2 A ";
    wp.whereOrder = " order by 1 ";
    pageQuery();

    if (sqlNotFind() && eqIgno(strAction, "PDF")) {
      wp.colSet(0, "id_no", "無資料");
      wp.listCount[0] = 1;
      queryAfter();
      return;
    }

    if (sqlNotFind()) {
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
    queryAfter();
  }

  public void queryAfter() {
    String sql1 = " select " + " sum(decode(payment_rate,'01',1,0)) as li_m1 ,  "
        + " sum(decode(payment_rate,'02',1,0)) as li_m2 ,  "
        + " sum(decode(payment_rate,'03',1,0)) as li_m3 , "
        + " sum(decode(acno_stop_flag,'Y',1,0)) as li_stop " + " from rsk_r001_data2 "
        + " where adj_yymm = ? " + " and adj_loc_flag = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("ex_adj_yymm"), wp.itemStr("ex_adj_loc_flag")});

    wp.colSet("li_m1", sqlNum("li_m1"));
    wp.colSet("li_m2", sqlNum("li_m2"));
    wp.colSet("li_m3", sqlNum("li_m3"));
    wp.colSet("li_stop", sqlNum("li_stop"));

    String sql2 = " select " + " count(*) as li_m4 " + " from rsk_r001_data2 "
        + " where adj_yymm = ? " + " and adj_loc_flag = ? " + " and uf_2num(payment_rate)>=4 ";

    sqlSelect(sql2, new Object[] {wp.itemStr("ex_adj_yymm"), wp.itemStr("ex_adj_loc_flag")});
    wp.colSet("li_m4", sqlNum("li_m4"));
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
    wp.reportId = "rskr0952";


    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();

    String allStr = " 額度覆核月份: " + commString.strToYmd(wp.itemStr("ex_adj_yymm")) + " 資料處理日期: "
        + commString.strToYmd(wp.colStr("proc_date"));
    wp.colSet("cond1", allStr);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0952.xlsx";
    pdf.pageCount = 28;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  @Override
  public void xlsPrint() throws Exception {

    if (this.checkApproveZz() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }

    try {
      log("xlsFunction: started--------");
      wp.reportId = "rskr0950";
      String allStr = "額度覆核月份: " + commString.strToYmd(wp.itemStr("ex_adj_yymm"));
      wp.colSet("cond1", allStr);
      wp.colSet("user_id", wp.loginUser);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "rskr0952.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

}
