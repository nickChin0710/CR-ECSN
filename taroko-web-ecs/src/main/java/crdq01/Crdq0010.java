/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-02  V1.00.01  Ryan       program initial                            *
* 106-12-14            Andy		  update : program name : Crdi0010==>Crdq0010*
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-06-25	 V1.00.02  Andy       update : add colum : lot_no                *
* 108-12-23	 V1.00.03  Andy       update : add pdf print                     * 
* 108-12-31	 V1.00.04  Andy       update : crd_nccc_card => crd_card_item    * 	 
* 109-05-06  V1.00.05  shiyuqi      updated for project coding standard      * 
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                          
* 110-03-31  V1.00.05  Justin     fix XSS                                   
* 112-12-15  V1.00.06  Ryan       modify 期初庫存量                                                                                  *  	 
******************************************************************************/

package crdq01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdq0010 extends BaseReport {
  String ttCloseReason = "", pgmId = "";
  String mProgName = "crdq0010";
  String reportSubtitle = "";
  int ii = 0;
  CommString comms = new CommString() ;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";

      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      // is_action = "R";
      // dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_place");
      dddwList("d_dddw_place", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'WH_LOC' order by wf_id");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_card_item1");
      dddwList("d_dddw_card_item",
          "crd_card_item as a left join crd_item_unit as b on a.card_item = b.card_item",
          "a.card_item", "b.unit_code", "where 1=1");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_card_item2");
      dddwList("d_dddw_card_item2",
          "crd_card_item as a left join crd_item_unit as b on a.card_item = b.card_item",
          "a.card_item", "b.unit_code", "where 1=1");

    } catch (Exception ex) {
    }
  }

  @Override
  public void initPage() {
    wp.colSet("ex_date", wp.sysDate.substring(0, 6));
  }

  @Override
  public void queryFunc() throws Exception {

    String cardItem1 = wp.itemStr("ex_card_item1");
    String cardItem2 = wp.itemStr("ex_card_item2");

    if (this.chkStrend(cardItem1, cardItem2) == false) {
      alertErr2("[卡樣-起迄]  輸入錯誤");
      return;
    }

    wp.whereStr = " where 1=1 and a.card_item = b.card_item ";

    if (empty(wp.itemStr("ex_date")) == false) {
      wp.whereStr += " and a.wh_year = :wh_year";
      setString("wh_year", wp.itemStr("ex_date").substring(0, 4));
    }
    // 庫存年月==>批號 Andy20180815 add
    if (empty(wp.itemStr("ex_date")) == false) {
      wp.whereStr += " and a.lot_no <= :lot_no ";
      setString("lot_no", wp.itemStr("ex_date") + "9999");
    }

    if (empty(wp.itemStr("ex_place")) == false) {
      wp.whereStr += " and a.place = :ex_place";
      setString("ex_place", wp.itemStr("ex_place"));
    }
    
    if (empty(wp.itemStr("ex_card_item1")) == false) {
        wp.whereStr += " and a.card_item >= :ex_card_item1";
        setString("ex_card_item1", wp.itemStr("ex_card_item1"));
    }
    
    if (empty(wp.itemStr("ex_card_item2")) == false) {
        wp.whereStr += " and a.card_item <= :ex_card_item2";
        setString("ex_card_item2", wp.itemStr("ex_card_item2"));
    }


    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " a.wh_year, " + " a.card_item, " + " a.place, " + " a.lot_no, "
        + " a.pre_total, " + " a.in_qty01, " + " a.in_qty02, " + " a.in_qty03, " + " a.in_qty04, "
        + " a.in_qty05, " + " a.in_qty06, " + " a.in_qty07, " + " a.in_qty08, " + " a.in_qty09, "
        + " a.in_qty10, " + " a.in_qty11, " + " a.in_qty12, " + " a.out_qty01, " + " a.out_qty02, "
        + " a.out_qty03, " + " a.out_qty04, " + " a.out_qty05, " + " a.out_qty06, "
        + " a.out_qty07, " + " a.out_qty08, " + " a.out_qty09, " + " a.out_qty10, "
        + " a.out_qty11, " + " a.out_qty12, " + " a.in_qty01_buy, " + " a.in_qty02_buy, "
        + " a.in_qty03_buy, " + " a.in_qty04_buy, " + " a.in_qty05_buy, " + " a.in_qty06_buy, "
        + " a.in_qty07_buy, " + " a.in_qty08_buy, " + " a.in_qty09_buy, " + " a.in_qty10_buy, "
        + " a.in_qty11_buy, " + " a.in_qty12_buy, " + " a.pre_total, " + " a.pre_total "
        + "  +  ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
        + "   in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
        + "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
        + "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12) total_bal,"
        + " 0 cur_qty, " + " b.safe_qty, " + " 0 db_real_qty, "
        + " nvl(c.unit_code,'') as unit_code ";
    wp.daoTable =
        " crd_warehouse as a,crd_card_item as b left join crd_item_unit as c on b.card_item = c.card_item ";
    wp.whereOrder = "order by a.card_item,a.place,a.lot_no ";
    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    // System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.colSet("sum_tolrow", numToStr(wp.selectCnt, ""));
    wp.colSet("IdUser", wp.loginUser);
    listWkdata();
    // dddw_select();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  void listWkdata() throws Exception {
    double ldPreTotal = 0, ldInqty = 0, ldOutqty = 0, ldInqtyBuy = 0, ldRealQty = 0;
    int itemTotQty3 = 0, itemTotQty0 = 0, itemTotQty1 = 0, itemTotQty2 = 0, itemTotQty = 0,
        itemTotQtyOut = 0, total = 0, itemTotQtyOutAll = 0, ttCurQty = 0;
    String lsMm = "", lsYyyymm = "", lsCardItem = "", lsPlace = "", safeQty = "", lsLotNo = "";
    String lsSql = "";
    lsMm = wp.itemStr("ex_date").substring(4, 6);
    lsYyyymm = wp.itemStr("ex_date");
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      // 庫位
      String sql =
          "select wf_id||'('||wf_desc||')' as tt_place from ptr_sys_idtab where 1=1 and wf_type = 'WH_LOC' and wf_id = :ex_place ";
      setString("ex_place", wp.colStr(ii, "place"));
      sqlSelect(sql);
      wp.colSet(ii, "tt_place", sqlStr("tt_place"));
      // 目前存量
//      ldPreTotal = wp.colNum(ii, "pre_total");
      ttCurQty = (int) wp.colNum(ii, "total_bal");
      wp.colSet(ii, "tt_cur_qty", ttCurQty + "");
      // 當月
      ldInqty = wp.colNum(ii, "in_qty" + lsMm); // 當月入庫
      ldInqtyBuy = wp.colNum(ii, "in_qty" + lsMm + "_buy"); // 當月新購
      ldOutqty = wp.colNum(ii, "out_qty" + lsMm); // 當月出庫
      wp.colSet(ii, "tt_in_qty01", (int) ldInqty + "");
      wp.colSet(ii, "tt_in_qty01_buy", (int) ldInqtyBuy + "");
      wp.colSet(ii, "tt_out_qty01", (int) ldOutqty + "");

      // 非當月資料有結餘時將存量==>pre_total //20180821 Andy
//      if (ldPreTotal == 0 && ldInqty == 0 && ldOutqty == 0) {
//        ldPreTotal = ttCurQty;
//        wp.colSet(ii, "pre_total", (int) ldPreTotal + "");
//      }
      
      countPreTotal(ii);
      ldPreTotal = wp.colNum(ii,"pre_total");

      lsCardItem = wp.colStr(ii, "card_item");
      lsPlace = wp.colStr(ii, "place");
      lsLotNo = wp.colStr(ii, "lot_no");
      String sql1 = "select sum(use_total) as ld_real_qty " + "from crd_whtrans "
          + "where warehouse_date like :ls_yyyymm || '%' " + "and card_item = :ls_card_item "
          + "and place = :ls_place " + "and lot_no = :ls_lot_no " + "and trans_reason in ('1','3') "
          + "and tns_type in ('1') ";
      setString("ls_yyyymm", lsYyyymm);
      setString("ls_card_item", lsCardItem);
      setString("ls_place", lsPlace);
      setString("ls_lot_no", lsLotNo);
      sqlSelect(sql1);
      if (!empty(sqlStr("ld_real_qty")))
        ldRealQty = this.toNum(sqlStr("ld_real_qty"));
      else
        ldRealQty = 0;
      wp.colSet(ii, "wk_real_qty", ((int) (wp.colNum(ii, "out_qty" + lsMm) - ldRealQty)) + "");
      // if (wp.col_ss(ii, "card_item").equals(wp.col_ss(ii + 1, "card_item"))) {
      // item_tot_qty_3 += (int) ld_pre_total;
      // item_tot_qty_0 += (int) ld_inqty_buy;
      // item_tot_qty_1 += (int) ld_inqty;
      // item_tot_qty_2 += (int) ld_outqty;
      // item_tot_qty += tt_cur_qty;
      // item_tot_qty_out += (int) (wp.col_num(ii, "out_qty"+ls_mm) - ld_real_qty);
      // }
      //
      // if (!wp.col_ss(ii, "card_item").equals(wp.col_ss(ii + 1, "card_item"))
      // && !wp.col_ss(ii - 1, "card_item").equals(wp.col_ss(ii, "card_item"))) {
      // item_tot_qty_3 += (int) ld_pre_total;
      // item_tot_qty_0 += (int) ld_inqty_buy;
      // item_tot_qty_1 += (int) ld_inqty;
      // item_tot_qty_2 += (int) ld_outqty;
      // item_tot_qty += tt_cur_qty;
      // item_tot_qty_out = (int) (wp.col_num(ii, "out_qty"+ls_mm) - ld_real_qty);
      // }
      itemTotQty3 += (int) ldPreTotal;
      itemTotQty0 += (int) ldInqtyBuy;
      itemTotQty1 += (int) ldInqty;
      itemTotQty2 += (int) ldOutqty;
      itemTotQty += ttCurQty;
      itemTotQtyOut += (int) (wp.colNum(ii, "out_qty" + lsMm) - ldRealQty);
      // total += (int) (ld_pre_total + ld_inqty - ld_outqty);
      total += ttCurQty;
      itemTotQtyOutAll += (int) (wp.colNum(ii, "out_qty" + lsMm) - ldRealQty);

      // 頁面插入小計
      if (!wp.colStr(ii, "card_item").equals(wp.colStr(ii + 1, "card_item"))) {
        safeQty = wp.colStr(ii, "safe_qty");
        wp.colSet(ii, "safe_qty1", safeQty);
        wp.colSet(ii, "itemTotQty3", numToStr(itemTotQty3, "#,###"));
        wp.colSet(ii, "itemTotQty0", numToStr(itemTotQty0, "#,###"));
        wp.colSet(ii, "itemTotQty1", numToStr(itemTotQty1, "#,###"));
        wp.colSet(ii, "itemTotQty2", numToStr(itemTotQty2, "#,###"));
        wp.colSet(ii, "itemTotQty", numToStr(itemTotQty, "#,###"));
        wp.colSet(ii, "itemTotQtyOut", numToStr(itemTotQtyOut, "#,###"));
        
//        wp.colSet(ii, "tr", "<tr>"
//            + "<td nowrap class=\"td_data\" align=\"left\" colspan=3 style=\"color: blue\">安全庫存量:"
//            + safeQty + "</td>" // safe_qty 安全庫存量
//            + "<td nowrap class=\"td_data\" align=\"right\">小計:</td>"
//            + "<td nowrap class=\"list_rr\">" + numToStr(itemTotQty3, "#,###") + "</td>" // item_tot_qty_3期初庫存量小計
//            + "<td nowrap class=\"list_rr\">" + numToStr(itemTotQty0, "#,###") + "</td>" // 新購卡數小計
//            + "<td nowrap class=\"list_rr\">" + numToStr(itemTotQty1, "#,###") + "</td>" // 本月入庫量數小計
//            + "<td nowrap class=\"list_rr\">" + numToStr(itemTotQty2, "#,###") + "</td>" // 本月出庫量數小計
//            + "<td nowrap class=\"list_rr\">" + numToStr(itemTotQty, "#,###") + "</td>" // 目前存量小計
//            + "<td nowrap class=\"list_rr\">" + numToStr(itemTotQtyOut, "#,###") + "</td>"
//            + "</tr>"); // 當月實際領卡數小計
        itemTotQty3 = 0;
        itemTotQty0 = 0;
        itemTotQty1 = 0;
        itemTotQty2 = 0;
        itemTotQty = 0;
        itemTotQtyOut = 0;
        itemTotQty = 0;
        ldPreTotal = 0;
      }
      wp.colSet("total", total + "");
      wp.colSet("item_tot_qty_out_all", itemTotQtyOutAll + "");
      // 卡樣中文
      lsSql = "select card_item,name from crd_card_item ";
      lsSql += " where card_item = :ls_card_item ";
      setString("ls_card_item", lsCardItem);
      sqlSelect(lsSql);
      wp.colSet(ii, "name", sqlStr("name"));
    }

  }

  @Override
  public void querySelect() throws Exception {

  }

  public void ptrSysIdtabDesc() {

  }

  public void dataProcess() throws Exception {
    queryFunc();
    if (wp.selectCnt == 0) {
      alertErr2("報表無資料可比對");
      return;
    }

  }
  
  
  void countPreTotal(int ii) {
	   int preTotal = 0;
	   int inQty = 0;
	   int outQty = 0;
	   int month = comms.strToInt(comms.right(wp.itemStr("ex_date"), 2)) ;
		for (int x = 2; x <= month; x++) {
			inQty += wp.colInt(ii, String.format("IN_QTY%02d", x-1));
			outQty += wp.colInt(ii, String.format("OUT_QTY%02d", x-1));
		}
	    preTotal = wp.colInt(ii,"PRE_TOTAL") + inQty - outQty;
	    wp.colSet(ii, "PRE_TOTAL" , preTotal);
  }

  void subTitle() {
    String exDate = wp.itemStr("ex_date");
    String exPlace = wp.itemStr("ex_place");
    String exCardItem1 = wp.itemStr("ex_card_item1");
    String exCardItem2 = wp.itemStr("ex_card_item2");
    String title = "";
    String lsSql = "";
    title = "庫存年月: " + exDate;
    // 庫位
    if (!empty(exPlace)) {
      lsSql = "select wf_desc as ex_place_desc from ptr_sys_idtab "
          + "where 1=1 and wf_type = 'WH_LOC' " + "and wf_id =:wf_id " + "group by wf_id,wf_desc";
      setString("wf_id", exPlace);
      sqlSelect(lsSql);
      title += "  庫位: " + sqlStr("ex_place_desc");
    }
    // 卡樣
    if (!empty(exCardItem1) || !empty(exCardItem2)) {
      title += "  卡樣: ";
      title += exCardItem1;
      title += " ~ " + exCardItem2;
    }
    reportSubtitle = title;
  }

  void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      // xlsx.report_id ="rskr0020";
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

    wp.pageRows = 9999;
    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
