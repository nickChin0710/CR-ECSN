/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-18  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-02-22  V1.00.02  Andy		  update : report format, UI                 *
* 107-03-14  V1.00.03  Andy       Update dddw_list merchant UI               *
* 109-04-20  V1.00.04  shiyuqi    updated for project coding standard        *
* 110-10-15  V1.00.04  Yang Bo    joint sql replace to parameters way        *
* 111-05-26  V1.00.05  Ryan       dddwSelect up bil_prod union bil_prod_nccc *
* 111-09-29  V1.00.06  JeffKung   add query field : cardNo                   *
******************************************************************************/
package bilr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF2;

public class Bilr0011 extends BaseReport {


  InputStream inExcelFile = null;
  String mProgName = "bilr0011";

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
    } else if (eqIgno(wp.buttonCode, "ItemChange")) {
      /* TEST */
      strAction = "ItemChange";
      itemChange();
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
    String mchtNo = wp.itemStr("ex_merchant");
    String productNo = wp.itemStr("ex_product_no");
    String idNo = wp.itemStr("ex_id");
    String cardNo = wp.itemStr("ex_cardNo");
    String op = wp.itemStr("ex_op");
    if (empty(mchtNo) && empty(idNo) && empty(cardNo)) {
      alertErr("特店代號或身分證號或卡號不可同時空白!");
      return false;
    }
    String lsWhere = "where 1=1 ";
    lsWhere += "and install_tot_term != install_curr_term ";
    lsWhere += "and contract_kind = 1 ";
    lsWhere += "and post_cycle_dd > 0 ";
    if (empty(mchtNo) == false) {
      lsWhere += " and mcht_no = :mcht_no";
      setString("mcht_no", mchtNo);
    }
    if (empty(productNo) == false) {
      lsWhere += " and product_no = :product_no";
      setString("product_no", productNo);
    }
    if (empty(idNo) == false) {
      lsWhere += " and id_p_seqno = (select id_p_seqno from crd_idno where 1=1 and id_no =:ex_id) ";
      setString("ex_id", idNo);
    }
    
    if (empty(cardNo) == false) {
        lsWhere += " and card_no = :ex_cardNo ";
        setString("ex_cardNo", cardNo);
    }

    switch (op) {
      case "1":
        lsWhere += " and cps_flag = 'Y' ";
        break;
      case "2":
        lsWhere += " and cps_flag = 'C' ";
        break;
      case "3":
        lsWhere += " and cps_flag = 'N' ";
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
    wp.selectSQL = "" + "mcht_no , " + "uf_idno_id(id_p_seqno) id_no , "
        + "uf_hi_idno(uf_idno_id(id_p_seqno)) db_id_no , "// 編碼:持卡人ID
        + "card_no , " + "uf_hi_cardno (card_no) db_card_no, "// 轉碼:交易卡號
        + "contract_no , " + "contract_seq_no , " + "product_no , " + "product_name , "
        + "cps_flag , " + " (tot_amt * qty - exchange_amt) as sum_amt ," + "unit_price , "
        + "install_tot_term , " + "first_remd_amt , " + "remd_amt , " + "install_curr_term , "
        + " decode( fee_flag ,'L', unit_price * (install_tot_term - install_curr_term) + remd_amt, "
        + " decode(install_curr_term,0,unit_price * (install_tot_term - install_curr_term)+first_remd_amt, unit_price * (install_tot_term - install_curr_term))) db_rem , " 
        + " fee_flag,  "
        + " decode(mcht_no,'',0,1) as mcht_cnt ";
    wp.daoTable = "bil_contract ";
    wp.whereOrder = " order by mcht_no ";
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);

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

  void listWkdata() {
    int rowCt = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt = 1;
      wp.colSet(ii, "row_ct", intToStr(rowCt));
    }
    wp.colSet("user_id", wp.loginUser);
  }

  void subTitle() {
    String mchtNo = wp.itemStr("ex_mcht_no");
    String productNo = wp.itemStr("ex_product_no");
    String idNo = wp.itemStr("ex_id");
    String cardNo = wp.itemStr("ex_cardNo");
    String op = wp.itemStr("ex_op");
    String subTitle = "";

    if (!empty(mchtNo)) {
      subTitle += "特店代號 : " + mchtNo;
    }

    if (!empty(productNo)) {
      subTitle += "商品代號 : " + productNo;
    }

    if (empty(idNo) == false) {
      subTitle += "持卡人身分證字號 : " + idNo;
    }
    
    if (empty(cardNo) == false) {
        subTitle += "交易卡號 : " + cardNo;
      }

    String[] cde1 = new String[] {"1", "2", "3", "4"};
    String[] txt1 = new String[] {"ONUS", "NCCC", "人工", "全部"};
    subTitle += "選項 : " + commString.decode(op, cde1, txt1);

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
    // ===========================
    wp.pageRows = 99999;
    queryFunc();
    // wp.setListCount(1);
    wp.colSet("user_id", wp.loginUser);
    TarokoPDF2 pdf = new TarokoPDF2();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    // 表頭固定欄位
    pdf.fixHeader[0] = "user_id";
    pdf.fixHeader[1] = "cond_1";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
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
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_merchant");
      // //dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name", "where 1=1 and
      // loan_flag = 'N' order by mcht_no");
      // dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no", "", "where 1=1 and loan_flag = 'N'
      // order by mcht_no");

      String exMchtNo = wp.itemStr("ex_merchant");
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_product_no");
      if(!empty(exMchtNo)) {
			String sqlStr = "select product_no,product_no||'_'||product_name as product_name from bil_prod_nccc where mcht_no = :mcht_no1 "
					+ " union select product_no,product_no||'_'||product_name as product_name from bil_prod where mcht_no = :mcht_no2 "
					+ " order by product_no ";
			setString("mcht_no1", exMchtNo);
			setString("mcht_no2", exMchtNo);
			sqlSelect(sqlStr);
			wp.colSet("dddw_product_no", this.dddwOption("product_no", "product_name"));
      }
//       為下面dddwList方法傳參數
//      setString("mcht_no", exMchtNo);
//      this.dddwList("dddw_product_no", "bil_prod", "product_no", "product_name",
//          "where 1=1 and mcht_no = :mcht_no order by product_no");
// 
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

  void itemChange() throws Exception {
    // String ex_mcht_no = wp.item_ss("ex_merchant");
    // System.out.println("ex_merchant : "+ex_mcht_no);
    // wp.initOption = "--";
    // wp.optionKey = wp.item_ss("ex_product_no");
    // this.dddw_list("dddw_product_no", "bil_prod", "product_no", "product_name", "where 1=1 and
    // mcht_no ='"+ex_mcht_no+"' order by product_no");
  }
}
