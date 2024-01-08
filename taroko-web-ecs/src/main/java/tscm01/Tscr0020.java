/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-13  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 108-12-20  v1.00.03  Andy       Update ptr_branch=>gen_brn                 *	
* 109-05-06  V1.00.04  shiyuqi      updated for project coding standard      * 
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名
* 111-04-20  V1.00.05  dingwenhao      TSC畫面整合                                                                                        *  
******************************************************************************/
package tscm01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Tscr0020 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "tscr0020";

  String condWhere = "";
  String reportSubtitle = "", whereStr = "", daoTable = "";

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
    String exOppostDate1 = wp.itemStr("ex_oppost_date1");
    String exCrtDate1 = wp.itemStr("ex_crt_date1");
    String exOppostDate2 = wp.itemStr("ex_oppost_date2");
    String exCrtDate2 = wp.itemStr("ex_crt_date2");

    if (empty(exOppostDate1) && empty(exOppostDate2)) {
      alertErr("掛失日期起迄不能全部空白");
      return false;
    }

    String lsWhere = "where 1=1  ";
    // 固定搜尋條件
    lsWhere += " and a.current_code = '2' ";
    lsWhere += " and a.tsc_sign_flag = 'Y' ";

    // user搜尋條件
    // 掛失日期
    lsWhere += sqlStrend(exOppostDate1, exOppostDate2, "a.oppost_date");
    // 入帳日期
    lsWhere += sqlStrend(exCrtDate1, exCrtDate2, "d.crt_date");
    whereStr = lsWhere;
    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    if (getWhereStr() == false)
//      return;
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

    wp.selectSQL = "" + "a.tsc_card_no, " + "a.card_no, " + "uf_hi_cardno (a.card_no) db_card_no, "// 轉碼:卡號
        + "a.oppost_date, " + "a.autoload_flag, " + "a.current_code, " + "a.tsc_sign_flag, "
        + "b.sup_flag, " + "b.acct_type, " + "b.id_p_seqno, " + "c.id_no, " + "c.id_no_code, "
        + "'' wk_id_no, "
        // + "to_char (ASCII (substr (c.id_no, 1, 1)) - 64, '00') || substr (c.id_no, 2, length
        // (c.id_no)) wk_id_no, "
        + "c.chi_name, " + "uf_hi_cname(c.chi_name) db_chi_name, "// 轉碼:姓名
        + "d.tran_amt_0h, " + "d.tran_amt_6h, " + "d.crt_date";
    wp.daoTable = "tsc_card a " + "left JOIN crd_card b ON a.card_no = b.card_no "
        + "left JOIN crd_idno c ON b.id_p_seqno = c.id_p_seqno "
        + "left JOIN tsc_btrd_log d ON a.tsc_card_no = d.tsc_card_no";
    daoTable = wp.daoTable;
    wp.whereOrder = " order by a.oppost_date,a.tsc_card_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    // wp.daoTable);

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
    String aId = "", bId = "";
    int cnt = 0;
    double sum1 = 0, sum2 = 0;
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");
      // 3h餘額 & 6H餘額
      sum1 += toNum(wp.colStr(ii, "tran_amt_0h"));
      sum2 += toNum(wp.colStr(ii, "tran_amt_6h"));

      aId = strMid(wp.colStr(ii, "id_no"), 0, 1);
      bId = strMid(wp.colStr(ii, "id_no"), 1, wp.colStr(ii, "id_no").length());
      if (!empty(aId)) {
        cnt = aId.charAt(0) - 64;
        bId = String.format("%02d", cnt) + bId;
        wp.colSet(ii, "wk_id_no", bId);
      }
    }
    getWhereStr();
    String sqlSelect =
        "select sum(d.tran_amt_0h) as sum_tran_amt_0h , sum(d.tran_amt_6h) as sum_tran_amt_6h "
            + " from " + daoTable + " " + whereStr;
    sqlSelect(sqlSelect);
    double sumtranAmt0h = sqlNum("sum_tran_amt_0h");
    double sumTranAmt6h = sqlNum("sum_tran_amt_6h");
    wp.colSet("sum_tran_amt_0h", sumtranAmt0h);
    wp.colSet("sum_tran_amt_6h", sumTranAmt6h);

    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("sum1", numToStr(sum1, "#,###"));
    wp.colSet("sum2", numToStr(sum2, "#,###"));
  }

  void subTitle() {
    String exOppostDate1 = wp.itemStr("ex_oppost_date1");
    String exOppostDate2 = wp.itemStr("ex_oppost_date2");
    String exCrtDate1 = wp.itemStr("ex_crt_date1");
    String exCrtDate2 = wp.itemStr("ex_crt_date2");
    String title = "列表條件 : ";
    // 掛失日期起迄
    if (empty(exOppostDate1) == false || empty(exOppostDate2) == false) {
      title += " 掛失日期起迄 : ";
      if (empty(exOppostDate1) == false) {
        title += exOppostDate1 + " 起 ";
      }
      if (empty(exOppostDate2) == false) {
        title += " ~ " + exOppostDate2 + " 迄 ";
      }
    }
    // 基金墊付日期起迄
    if (empty(exCrtDate1) == false || empty(exCrtDate2) == false) {
      title += " 入帳日期起迄 : ";
      if (empty(exCrtDate1) == false) {
        title += exCrtDate1 + " 起 ";
      }
      if (empty(exCrtDate2) == false) {
        title += " ~ " + exCrtDate2 + " 迄 ";
      }
    }

    reportSubtitle = title;
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
      log("Detl: rowcnt:" + wp.listCount[0]);
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
    // wp.dddSql_log=false;
    wp.colSet("cond_1", reportSubtitle);
    // ===========================
    wp.pageRows = 99999;
    queryFunc();

    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
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
      // dddw_group_code
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_group_code");
      // dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1 group
      // by group_code,group_name order by group_code");

      // dddw_branch
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_branch");
      // dddw_list("dddw_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    } catch (Exception ex) {
    }
  }

}

