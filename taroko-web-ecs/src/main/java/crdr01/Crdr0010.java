/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-07  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-06-19  V1.00.02  Andy		  update : UI,report format                  *
* 108-12-23  V1.00.03  Andy		  update : fix bug                           *	
* 108-12-31	 V1.00.04  Andy       update : crd_nccc_card => crd_card_item    * 		
* 109-05-06  V1.00.05  shiyuqi    updated for project coding standard        * 
* 109-01-04  V1.00.06  shiyuqi    修改无意义命名                                                                                             * 
* 112-12-16  V1.00.07  Wilson     調整排序                                                                                                        *   
*****************************************************************************/
package crdr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0010 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "crdr0010";
  String reportSubtitle = "";
  String condWhere = "";

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
    String exDateS = wp.itemStr("exDateS");
    String exPlace = wp.itemStr("ex_place");
    String exTnsType = wp.itemStr("ex_tns_type");
    String exTransReason1 = wp.itemStr("ex_trans_reason1");
    String exTransReason2 = wp.itemStr("ex_trans_reason2");
    String exLotNo = wp.itemStr("ex_lot_no");

    String lsWhere = "where 1=1  ";
    if (empty(exDateS) == false) {
      lsWhere += " and a.warehouse_date = :exDateS ";
      setString("exDateS", exDateS);
    } else {
      alertErr("異動日期為必要條件!!請重新輸入.");
      return false;
    }

    if (empty(exPlace) == false) {
      lsWhere += " and a.place = :ex_place ";
      setString("ex_place", exPlace);
    }
    // 批號
    lsWhere += sqlCol(exLotNo, "b.lot_no");

    switch (exTnsType) {
      case "1":
        lsWhere += " and a.tns_type = '1' ";
        if (empty(exTransReason1) == false) {
          lsWhere += " and a.trans_reason = :ex_trans_reason1 ";
          setString("ex_trans_reason1", exTransReason1);
        }
        break;
      case "2":
        lsWhere += " and a.tns_type = '2' ";
        if (empty(exTransReason2) == false) {
          lsWhere += " and a.trans_reason = :ex_trans_reason2 ";
          setString("ex_trans_reason2", exTransReason2);
        }
        break;
    }

    wp.whereStr = lsWhere;
    // System.out.println("ls_where:"+ls_where);
    setParameter();

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;

    // 判斷crd_tx_bal是否已有結算資料
    String lsSql = "";
    String exDateS = wp.itemStr("exDateS");
    lsSql = "select count(*) ct from crd_tx_bal ";
    lsSql += "where tx_date = :exDateS ";
    setString("exDateS", exDateS);
    sqlSelect(lsSql);
    if (sqlNum("ct") < 1) {
      alertErr("此異動日期尚未結算 !!" + exDateS);
      return;
    }

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

    wp.selectSQL =    		
         "" + "decode(substring(a.card_item,5,2),'VD','Y','N') as debit_flag, "
    		+ "a.warehouse_no, " + "a.card_type, " + "a.place, " + "nvl(b.lot_no,'') as lot_no, "
            + "a.item_amt, " + "a.tns_type, " + "a.use_total, " + "a.warehouse_date, "
            + "a.card_item, " + "a.trans_reason, " + "0 icbc_qty, " + "0 nccc_qty, "
            // + "0 tot_icbc_qty, " //tot_icbc_qty PB捨棄無用欄位
            // + "0 tot_nccc_qty, " //tot_nccc_qty PB捨棄無用欄位
            + "0 wk_inqty, " + "0 wk_outqty, " + "'' db_1, " + "'' db_2, " + "a.unit_code, "
            + "0 db_out_235," + "'' wk_temp ";
    wp.daoTable = " crd_whtrans a left join crd_whtx_dtl b on a.warehouse_no = b.warehouse_no "
                              + " left join ptr_group_code c on substring(a.card_item,1,4) = c.group_code ";
    wp.whereOrder = " order by debit_flag desc, c.combo_indicator desc, a.card_item, a.place ";

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
    String lsSql = "", lsPlace = "", dbPalce = "", lsTransType = "", lsTransReson = "",
        dbTransReson;
    String ibcQty = "", nccQty = "", lsCardItem = "";
    int sum1 = 0, sum2 = 0;

    String exDateS = wp.itemStr("exDateS");
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;

      // place 中文
      lsPlace = wp.colStr(ii, "place");
      lsSql = "select wf_desc from ptr_sys_idtab ";
      lsSql += " where wf_id = :ls_place ";
      lsSql += "and wf_type = 'WH_LOC' ";
      setString("ls_place", lsPlace);
      sqlSelect(lsSql);
      dbPalce = sqlStr("wf_desc");
      wp.colSet(ii, "place", dbPalce);

      // trans_reason中文
      lsSql = "";
      lsTransType = wp.colStr(ii, "tns_type");
      lsTransReson = wp.colStr(ii, "trans_reason");
      lsSql = "select wf_desc from ptr_sys_idtab ";
      lsSql += " where wf_id = :ls_trans_reson ";
      setString("ls_trans_reson", lsTransReson);
      switch (lsTransType) {
        case "1":
          lsSql += "and wf_type = 'TRNS_IN_RSN' ";
          break;
        case "2":
          lsSql += "and wf_type = 'TRNS_RSN1' ";
          break;
      }
      sqlSelect(lsSql);
      dbTransReson = sqlStr("wf_desc");
      wp.colSet(ii, "trans_reason", dbTransReson);

      // ibc_qty,ncc_qty
      lsCardItem = wp.colStr(ii, "card_item");
      lsSql = "";
      lsSql = "select sum(pre_total) pre_total,sum(total_bal) total_bal from crd_tx_bal ";
      lsSql += " where card_item = :ls_card_item ";
      setString("ls_card_item", lsCardItem);
      if (empty(exDateS) == false) {
        lsSql += " and tx_date = :exDateS ";
        setString("exDateS", exDateS);
      }
      sqlSelect(lsSql);
      ibcQty = sqlStr("pre_total");
      nccQty = sqlStr("total_bal");
      wp.colSet(ii, "ibc_qty", ibcQty);
      wp.colSet(ii, "ncc_qty", nccQty);

      // System.out.println("pre_total:"+ibc_qty+"total_bal:"+ncc_qty);

      // wk_inqty

      if (lsTransType.equals("1")) {
        wp.colSet(ii, "wk_inqty", wp.colStr(ii, "use_total"));
        sum1 += Integer.parseInt(wp.colStr(ii, "use_total"));
      } else {
        wp.colSet(ii, "wk_inqty", "0");
      }
      // wk_outqty
      if (lsTransType.equals("2")) {
        wp.colSet(ii, "wk_outqty", wp.colStr(ii, "use_total"));
        sum2 += Integer.parseInt(wp.colStr(ii, "use_total"));
      } else {
        wp.colSet(ii, "wk_outqty", "0");
      }
      // 卡樣中文
      lsSql = "select card_item,name from crd_card_item ";
      lsSql += " where card_item = :ls_card_item ";
      setString("ls_card_item", lsCardItem);
      sqlSelect(lsSql);
      wp.colSet(ii, "db_card_item_name", sqlStr("name"));
    }
    wp.colSet("row_ct", intToStr(rowCt));
    wp.colSet("sum1", intToStr(sum1));
    wp.colSet("sum2", intToStr(sum2));
    wp.colSet("user_id", wp.loginUser);

  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exPlace = wp.itemStr("ex_place");
    String exTnsType = wp.itemStr("ex_tns_type");
    String exTransReason1 = wp.itemStr("ex_trans_reason1");
    String exTransReason2 = wp.itemStr("ex_trans_reason2");
    String exLotNo = wp.itemStr("ex_lot_no");
    String title = "";
    String lsSql = "";
    title = "異動日期: " + exDateS;
    if (!empty(exLotNo)) {
      title = "  批號: " + exLotNo;
    }

    if (!empty(exPlace)) {
      lsSql = "select wf_desc as ex_place_desc from ptr_sys_idtab "
          + "where 1=1 and wf_type = 'WH_LOC' " + "and wf_id =:wf_id " + "group by wf_id,wf_desc";
      setString("wf_id", exPlace);
      sqlSelect(lsSql);
      title += "  庫位: " + sqlStr("ex_place_desc");
    }
    if (exTnsType.equals("0")) {
      title += "  庫存種類: 全部";
    }
    if (!exTnsType.equals("0")) {

      switch (exTnsType) {
        case "1":
          title += "  庫存種類: 入庫  ";
          if (!empty(exTransReason1)) {
            lsSql = "select wf_id||':'||wf_desc as trans_reason_desc " + "from ptr_sys_idtab "
                + "where 1=1 and wf_type = 'TRNS_IN_RSN' " + "and wf_id =:wf_id"
                + "group by wf_id,wf_desc ";
            setString("wf_id", exTransReason1);
            sqlSelect(lsSql);
            title += " 入庫原因: " + sqlStr("trans_reason_desc");
          }

          break;
        case "2":
          title += "  庫存種類: 出庫  ";
          if (!empty(exTransReason2)) {
            lsSql = "select wf_id||':'||wf_desc as trans_reason_desc " + "from ptr_sys_idtab "
                + "where 1=1 and wf_type = 'TRNS_RSN1' " + "and wf_id =:wf_id"
                + "group by wf_id,wf_desc ";
            setString("wf_id", exTransReason2);
            sqlSelect(lsSql);
            title += "  出庫原因: " + sqlStr("trans_reason_desc");
          }

          break;
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
    wp.colSet("cond_1", reportSubtitle);
    // String ss = "PDFTEST: ";
    // wp.col_set("cond_1", ss);
    wp.pageRows = 99999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 27;
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
      // dddw_place
      // wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_place");
      dddwList("dddw_place", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'WH_LOC' group by wf_id,wf_desc order by wf_id");

      // dddw_place
      wp.optionKey = wp.colStr("ex_trans_reason1");
      dddwList("dddw_trans_reason1", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'TRNS_IN_RSN' group by wf_id,wf_desc order by wf_id");

      // dddw_place
      wp.optionKey = wp.colStr("ex_trans_reason2");
      dddwList("dddw_trans_reason2", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'TRNS_RSN1' group by wf_id,wf_desc order by wf_id");

    } catch (Exception ex) {
    }
  }

}
