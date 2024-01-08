/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-18  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-05-03  V1.00.02  Andy		  update : DE BUG,report format              *
* 107-05-25  V1.00.03  Andy		  update : pagecount SQL     				 *
* 109-03-09  V1.00.04  Zhu Zhenwu update : modify SQL           			 *
* 109-04-20  V1.00.05  shiyuqi    updated for project coding standard        * 
* 109-11-18  V1.00.01  Kirin      bilr1110移至mktr01,並更名Mktr1010          *
* 112-02-22  V1.00.07  Zuwei Su   身分證號查詢條件會有error                  *
******************************************************************************/
package mktr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktr1010 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "mktr1010";

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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // wp.setExcelMode();
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      // wp.setExcelMode();
      pdfPrint();
    }

    dddwSelect();
    // init_button();
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exActionCd = wp.itemStr("ex_action_cd");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");

    if (this.chkStrend(exDateS, exDateE) == false) {
      alertErr2("[異動日期-起迄]  輸入錯誤");
      return;
    }

    if (getWhereStr() == false)
      return;

    wp.sqlCmd = "select tx_date, " + "action_cd, " + "document_desc, " + "id_no, "
        + "uf_hi_idno(id_no) db_id_no , "// 編碼:身分證ID
        + "card_no, " + "uf_hi_cardno (card_no) db_card_no, "// 轉碼:卡號
        + "sum (car_hours) db_car_hours, " + "count (card_no) db_card_cnt, "
        + "lpad (' ', 40, ' ') db_ename, "
        + "decode (action_cd,'0003','台灣聯通','0004','台灣聯通','永固/台灣聯通/VIVI PARK') db_park_name "
        + "from (" + "select a.tx_date, " + "a.action_cd, "
        + "(SELECT document_desc FROM bil_dodo_parm WHERE action_cd = a.action_cd) document_desc, "
        + "uf_idno_id (a.id_p_seqno) id_no, " + "a.card_no, " + "a.car_hours "
        + "from bil_dodo_dtl a " + "where 1=1 ";
    if (!empty(exDateS) || !empty(exDateE)) {
      wp.sqlCmd += sqlStrend(exDateS, exDateE, "tx_date");
    }
    wp.sqlCmd += sqlCol(exActionCd, "action_cd");
    if (empty(exIdNo) == false) {
      wp.sqlCmd +=
          " and id_p_seqno = (select c.id_p_seqno from crd_idno c where 1=1 and c.id_no =?) ";
      setString(exIdNo);
    }
    wp.sqlCmd += sqlCol(exCardNo, "a.card_no");
    wp.sqlCmd += ") " + "group by id_no,card_no,tx_date,action_cd,document_desc ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // wp.daoTable);
    wp.pageCountSql = "select count(*) from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql += ")";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    int sum1 = 0;
    int selCt = wp.selectCnt;
    String wkActionCd = "";
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
      sum1 += Integer.parseInt(wp.colStr(ii, "db_car_hours"));
    } ;
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("sum1", intToStr(sum1));
    wp.colSet("ft_cnt", Integer.toString(rowCt));
    wp.colSet("user_id", wp.loginUser);
    dddwSelect();
  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String exActionCd = wp.itemStr("ex_action_cd");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");
    String subTitle = "";
    // 寄件日期
    if (empty(exDateS) == false || empty(exDateE) == false) {
      subTitle += "異動日期  : ";
      if (empty(exDateS) == false) {
        subTitle += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        subTitle += " ~ " + exDateE + " 迄 ";
      }
    }
    if (!empty(exActionCd)) {
      subTitle += " 活動代號 : " + exActionCd;
    }

    if (!empty(exIdNo)) {
      subTitle += " 身分證號  : " + exIdNo;
    }

    if (empty(exCardNo) == false) {
      subTitle += " 卡號  : " + exCardNo;
    }
    reportSubtitle = subTitle;
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + ".xlsx";
      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      wp.setListCount(1);
      wp.setPageValue();
      log("Detl: rowcnt:" + wp.listCount[1]);
      xlsx.processExcelSheet(wp);
      /*
       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
       */
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    // 表頭固定欄位
//    pdf.fixHeader[0] = "user_id";
//    pdf.fixHeader[1] = "cond_1";

    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    // pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);

    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // dddw_mcht_no
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_action_cd");
      dddwList("dddw_action_cd", "bil_dodo_parm", "action_cd", "document_desc",
          "where 1=1 order by action_cd");
    } catch (Exception ex) {
    }
  }

}


