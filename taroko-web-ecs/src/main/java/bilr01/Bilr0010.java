/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-19  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 107-03-14  V1.00.02  Andy       Update dddw_list merchant UI               * 
* 107-05-08  V1.00.03  Andy       Update SQL bug                             * 
* 109-04-27  V1.00.04  shiyuqi       updated for project coding standard     * 	
* 109-01-04  V1.00.05   shiyuqi       修改无意义命名                                                                                      *  	
* 111-05-26  V1.00.06   Ryan       移除重複getWhereStr()                        *                                                                           *  	
******************************************************************************/
package bilr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Bilr0010 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "bilr0010";
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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String op = wp.itemStr("ex_op");
    String stmtCycle = wp.itemStr("ex_stmt_cycle");
    String mchtNo = wp.itemStr("ex_merchant");
    String idNo = wp.itemStr("ex_id");
    String exConfirm = wp.itemStr("ex_confirm");
    String exKind = wp.itemStr("ex_kind");

    String lsWhere = " where 1=1 ";
    // ls_where += "and a.unit_price * (a.install_tot_term -
    // install_curr_term) > 0 ";
    if (chkStrend(exDateS, exDateE) == false) {
      alertErr("放行日期起迄輸入錯誤!!");
      return false;
    }
    lsWhere += sqlStrend(exDateS, exDateE, "a.apr_date");

    switch (op) {
      case "1":
        lsWhere += " and a.cps_flag = 'Y'";
        break;
      case "2":
        lsWhere += " and a.cps_flag = 'N'";
        break;
      case "4":
        lsWhere += " and a.cps_flag = 'C'";
        break;
    }

    lsWhere += sqlCol(stmtCycle, "a.stmt_cycle");
    lsWhere += sqlCol(mchtNo, "a.mcht_no");
    lsWhere += sqlCol(idNo, "c.id_no", "like%");

    switch (exConfirm) {
      case "1":
        lsWhere += " and a.apr_flag = 'N'"; //放行前
        break;
      case "2":
        lsWhere += " and a.apr_flag = 'Y'"; //放行後
        break;
    }

    switch (exKind) {
      case "1":
        lsWhere += " and a.contract_kind = '1'";
        break;
      case "2":
        lsWhere += " and a.contract_kind = '1' and a.redeem_kind = '2' ";
        break;
      case "3":
        lsWhere += " and a.contract_kind = '1' and a.redeem_kind = '1' ";
        break;
      case "4":
        lsWhere += " and a.contract_kind = '1' and a.redeem_kind = '0' ";
        break;
      case "5":
        lsWhere += " and a.contract_kind = '2'";
        break;
      case "6":
        lsWhere += " and a.contract_kind = '2' and a.redeem_kind = '2'";
        break;
      case "7":
        lsWhere += " and a.contract_kind = '2' and a.redeem_kind = '1'";
        break;
      case "8":
        lsWhere += " and a.contract_kind = '2' and a.redeem_kind = '0'";
        break;
    }

    wp.whereStr = lsWhere;
    setParameter();
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

//    if (getWhereStr() == false)
//      return;

    wp.selectSQL = "" + "a.mcht_no , " + "a.fh_flag , " + "a.apr_date , " + "a.contract_no , "
        + "a.contract_seq_no , " + "a.card_no , " + "uf_hi_cardno (a.card_no) db_card_no, "// 轉碼:卡號
        + "c.id_no , " + "uf_hi_idno (c.id_no) db_id_no, "// 轉碼:帳號
        + "a.stmt_cycle , " + "a.product_no , " + "a.product_name , " + "a.cps_flag , "
        + "a.tot_amt , " + "a.qty , " + " (a.tot_amt * a.qty - a.exchange_amt) as amt_all ,"
        + "a.install_tot_term , " + "a.unit_price , "
        + " decode(a.fee_flag,'F', a.first_remd_amt, a.remd_amt) as wk_remd_amt ,"
        + "a.exchange_amt , "
        + " decode(a.refund_apr_flag,'Y', decode(sign(a.apr_date - a.refund_apr_date),-1, a.redeem_amt + d.redeem_amt, a.redeem_amt),a.redeem_amt) as amt_redeem ,"
        + " decode(a.refund_apr_flag,'Y', decode(sign(a.apr_date - a.refund_apr_date),-1, a.redeem_point + d.redeem_point, a.redeem_point),a.redeem_point) as point_redeem ,"
        + " a.receive_name, " + " a.delv_date, " + " decode(a.mcht_no,'',0,1) as mcht_cnt ";
    wp.daoTable = " bil_contract a left JOIN crd_card b on a.card_no = b.card_no";
    wp.daoTable += " left join crd_idno c on b.id_p_seqno = c.id_p_seqno";
    wp.daoTable +=
        " left join bil_back_log d on a.contract_no = d.contract_no and a.contract_seq_no = d.contract_seq_no";
    wp.whereOrder = " order by a.mcht_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
    // + wp.whereStr +wp.whereOrder);
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

  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String op = wp.itemStr("ex_op");
    String stmtCycle = wp.itemStr("ex_stmt_cycle");
    String mchtNo = wp.itemStr("ex_merchant");
    String idNo = wp.itemStr("ex_id");
    String exConfirm = wp.itemStr("ex_confirm");
    String exKind = wp.itemStr("ex_kind");
    String title = "";
    title = "";
    if (empty(exDateS) == false || empty(exDateE) == false) {
      title += "放行日期 : ";
      if (empty(exDateS) == false) {
        title += exDateS + " 起  ";
        title += " ";
      }
      if (empty(exDateE) == false) {
        title += " ~ " + exDateE + " 迄  ";
        title += " ";
      }
    }
    if (empty(op) == false) {
      title += "選項 : ";
      title += commString.decode(op, ",1,4,2,3,", ",ONUS,NCCC,人工,全部");
      title += " ";
    }
    if (empty(stmtCycle) == false) {
      title += "結帳日 : ";
      title += stmtCycle;
      title += " ";
    }
    if (empty(mchtNo) == false) {
      title += "   特店代號 : ";
      title += mchtNo;
      title += " ";
    }
    if (empty(idNo) == false) {
      title += " 帳號 : ";
      title += idNo;
      title += " ";
    }
    if (empty(exConfirm) == false) {
      title += " 放行註記 : ";
      title += commString.decode(exConfirm, ",1,2,3,", ",放行前,放行後,全部");
      title += " ";
    }
    if (empty(exKind) == false) {
      title += " 類別 : ";
      title += commString.decode(exKind, ",1,2,3,4,5,6,7,8,9",
          ",分期-全部,分期-折抵,分期-全折,分期-無折,郵購-全部,郵購-折抵,郵購-全折,郵購-無折,全部");
      title += " ";
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
    wp.colSet("cond_1", reportSubtitle);
    // =====================================
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 26;
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
      wp.optionKey = wp.colStr("ex_merchant");
      dddwList("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name",
          "where 1=1 and loan_flag = 'N' order by mcht_no");

    } catch (Exception ex) {
    }
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }
}
