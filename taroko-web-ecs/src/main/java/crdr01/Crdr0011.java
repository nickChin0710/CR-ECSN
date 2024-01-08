/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-01-20  V1.00.00  Zuwei       Create                                     *
* 109-01-31  V1.00.01  Ru             add excel report                           *
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      *                                                                             *
* 109-06-30  V1.00.03  Zuwei       fix code scan issue Unreleased Resource: Streams                           *
* 109-09-30  V1.00.04  JustinWu   change the way to get URL 
* 109-01-04  V1.00.05   shiyuqi       修改无意义命名                                                                                      *  
* 112-04-26  V1.00.06  Zuwei Su   分頁問題                                                                                      *  
******************************************************************************/
package crdr01;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoParm;

public class Crdr0011 extends BaseEdit {

  String fileName = "crdr0011";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // 打印debug日志
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
      exportExcel();
    } else if (eqIgno(wp.buttonCode, "XLS2")) { // -Excel-
      strAction = "XLS";
      exportExcel2();
      // } else if (eq_igno(wp.buttonCode, "PDF")) { // -PDF-
      // is_action = "PDF";
      // wp.setExcelMode();
      // pdfPrint();
    }

    dddwSelect();
  }

  @Override
  public void queryFunc() throws Exception {
    // 判斷crd_tx_bal是否已有結算資料
    String lsSql = "";
    String sTxDate = wp.itemStr("s_tx_date");
    lsSql = "select count(*) ct from crd_tx_bal ";
    lsSql += "where tx_date = :txDate ";
    setString("txDate", sTxDate);
    sqlSelect(lsSql);
    if (empty(sqlStr("ct")) || Integer.parseInt(sqlStr("ct")) < 1) {
      alertErr("此異動日期尚未結算 !!" + sTxDate);
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  /**
   * 查詢列表數據
   */
  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    String sTxDate = wp.itemStr("s_tx_date");
    String sPlace = wp.itemStr("s_place");

    wp.selectSQL = " distinct " + "a.tx_date, " + "a.card_item, " + "c.name as card_name, "
        + "a.in_qty, " + "a.out_qty, " + "a.total_bal, " + "a.scrap_qty, "
        + "p.wf_desc as place_name, " + "a.place "
        + ", b.debit_flag , b.bin_type, substr(a.card_item, 1, 1) , substr(a.card_item, 2, 3) , substr(a.card_item, 5, 2) ";
    wp.daoTable = " crd_tx_bal a, ptr_bintable b, crd_card_item c, ptr_sys_idtab p ";
    wp.whereStr = " where substr(a.card_item, 5, 2) = b.card_type "
        + "and a.card_item = c.card_item " + "and p.wf_id = a.place " + "and p.wf_type = 'WH_LOC' ";
    if (!empty(sTxDate)) {
      wp.whereStr += " and a.tx_date = :txdate ";
      setString("txdate", sTxDate);
    } else {
      alertErr("異動日期為必要條件!!請重新輸入.");
      return;
    }
    if (!empty(sPlace)) {
      wp.whereStr += " and a.place = :place ";
      setString("place", sPlace);
    }
    wp.whereOrder +=
        " order by b.debit_flag asc, b.bin_type desc, substr(a.card_item, 1, 1) asc, substr(a.card_item, 2, 3) desc, substr(a.card_item, 5, 2) asc ";

    // // 設置列表查詢的統計總數sql，如果此sql爲空，會自動構造一個count sql
    wp.pageCountSql = "SELECT " + wp.selectSQL + " FROM " + wp.daoTable + " " + wp.whereStr;
    wp.pageCountSql = "select count(1) ct from (" + wp.pageCountSql + ")";
    if (strAction.equals("XLS")) {
      selectNoLimit();
    }
    pageQuery();
    // total();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dataRead() throws Exception {}

  @Override
  public void saveFunc() throws Exception {}

  @Override
  public void initButton() {}

  @Override
  public void dddwSelect() {
    try {
      // dddw_place
      wp.initOption = "--";
      // 下拉框已選擇的值
      wp.optionKey = wp.colStr("s_place");
      dddwList("dddw_place", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'WH_LOC' group by wf_id,wf_desc order by wf_id");
    } catch (Exception ex) {
    }
  }

  // void total() {
  // double sum1=0d, sum2=0d, sum3=0d, sum4=0d;
  // for (int ii = 0; ii < wp.selectCnt; ii++) {
  // sum1 += wp.col_num(ii,"in_qty");
  // sum2 += wp.col_num(ii,"out_qty");
  // sum3 += wp.col_num(ii,"total_bal");
  // sum4 += wp.col_num(ii,"scrap_qty");
  // }
  // wp.col_set("row_ct", int_2Str(wp.selectCnt));
  // wp.col_set("sum1", num_2str(sum1,"###"));
  // wp.col_set("sum2", num_2str(sum2,"###"));
  // wp.col_set("sum3", num_2str(sum3,"###"));
  // wp.col_set("sum4", num_2str(sum4,"###"));
  // wp.col_set("user_id", wp.loginUser);
  // }

  void exportExcel() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = fileName;
      // -cond-
      String sTxDate = wp.itemStr("s_tx_date");
      String sPlace = wp.itemStr("s_place");
      String cond1 = "";
      if (empty(sTxDate) == false) {
        cond1 += " 異動日期 : " + sTxDate;
      }
      if (!empty(sPlace)) {
        String lsSql = "select wf_desc from ptr_sys_idtab " + "where 1=1 and wf_type = 'WH_LOC' "
            + "and wf_id =:wf_id " + "group by wf_id,wf_desc";
        setString("wf_id", sPlace);
        sqlSelect(lsSql);
        cond1 += "  庫位: " + sqlStr("wf_desc");
      }
      wp.colSet("cond_1", cond1);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = fileName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      wp.setListCount(1);
      // debug log
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();

      log("xlsFunction: ended-------------");
      // 設置輸出excle變數為Y
      wp.exportXls = "Y";
      // 從exportFile讀取文件内容到wb對象
      wp.setDownload(wp.exportFile);
      // 設置輸出源内容為N
      wp.exportSrc = "N";
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void exportExcel2() throws Exception {
    String place = wp.itemStr("s_place");
    String txDateE = wp.itemStr("s_tx_date");;
    String txDateS = "";
    String cardItem = "";
    String cardName = "";
    String txDate = "";
    String tDate = "";
    String outQty = "";
    String sumOutQty = "";
    String sumInQty = "";
    String sumScrapQty = "";
    String totalBal = "";
    String placeC = "";
    int days = 0;
    int cardItemNum = 0;
    int rowNum = 0;
    int totalNum = 0;

    String lsSql = "select a.card_item, b.bin_type, a.place " + "from crd_tx_bal a "
        + "left join ptr_bintable b on substring(a.card_item,5,2) = b.card_type " + "where 1=1 ";

    if (!empty(place)) {
      lsSql += "and a.place = :place ";
    }
    if (!empty(txDateE)) {
      days = Integer.parseInt(txDateE.substring(6));
      txDateS = txDateE.substring(0, 6) + "01";
      lsSql += "and a.tx_date between :txDateS and :txDateE ";

      lsSql += "group by a.card_item, b.bin_type, a.place "
          + "order by b.bin_type desc, substring(a.card_item,1,4), substring(a.card_item,5,2) ";

      setString("txDateS", txDateS);
      setString("txDateE", txDateE);
      setString("place", place);
      sqlSelect(lsSql);
      cardItemNum = sqlRowNum;
    } else {
      alertErr2("日結日期未輸入");
      return;
    }

    XSSFWorkbook wb = new XSSFWorkbook();
    XSSFSheet sheet = wb.createSheet("工作表1");
    XSSFRow row = null;
    XSSFCell cell = null;
    XSSFCellStyle titleStyle = wb.createCellStyle();
    XSSFCellStyle numberStyle = wb.createCellStyle();
    XSSFCellStyle numStyle = wb.createCellStyle();
    XSSFCellStyle strStyle = wb.createCellStyle();

    titleStyle.setAlignment(HorizontalAlignment.CENTER);
    numberStyle.setAlignment(HorizontalAlignment.RIGHT);
    numStyle.setAlignment(HorizontalAlignment.RIGHT);
    numStyle.setBorderTop(BorderStyle.THIN);
    numStyle.setBorderBottom(BorderStyle.THIN);
    numStyle.setBorderLeft(BorderStyle.THIN);
    numStyle.setBorderRight(BorderStyle.THIN);
    strStyle.setBorderTop(BorderStyle.THIN);
    strStyle.setBorderBottom(BorderStyle.THIN);
    strStyle.setBorderLeft(BorderStyle.THIN);
    strStyle.setBorderRight(BorderStyle.THIN);

    if (cardItemNum > 0) {
      lsSql = "select wf_desc from ptr_sys_idtab " + "where 1=1 and wf_type = 'WH_LOC' "
          + "and wf_id = :wf_id " + "group by wf_id,wf_desc";
      setString("wf_id", place);
      sqlSelect(lsSql);

      if (sqlRowNum > 0) {
        placeC = sqlStr("wf_desc");
      }

      // 產生row
      row = sheet.createRow(0);
      cell = row.createCell(0);
      cell.setCellValue("crdr0011");
      cell = row.createCell(1);
      cell.setCellStyle(titleStyle);
      cell.setCellValue("卡片庫存日累計明細表");
//      cell = row.createCell(cardItemNum);
//      cell.setCellValue("Date: " + wp.sysDate.substring(0, 4) + "/" + wp.sysDate.substring(4, 6)
//          + "/" + wp.sysDate.substring(6));
//      sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, cardItemNum - 1));
      row = sheet.createRow(1);
      cell = row.createCell(0);
      cell.setCellValue("Date: " + wp.sysDate.substring(0, 4) + "/" + wp.sysDate.substring(4, 6)
      + "/" + wp.sysDate.substring(6));
      cell = row.createCell(1);
      cell.setCellValue("Time: " + wp.sysTime.substring(0, 2) + ":" + wp.sysTime.substring(2, 4)
          + ":" + wp.sysTime.substring(4));
      row = sheet.createRow(2);
      cell = row.createCell(0);
      cell.setCellValue("日結日期：" + txDateE + "，庫位：" + placeC);
      sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, cardItemNum));
      row = sheet.createRow(3);
      cell = row.createCell(0);
      cell.setCellStyle(strStyle);
      cell.setCellValue("卡樣");
      row = sheet.createRow(4);
      cell = row.createCell(0);
      cell.setCellStyle(strStyle);
      cell.setCellValue("卡名");
      for (int i = 0; i < days; i++) {
        row = sheet.createRow(i + 5);
        cell = row.createCell(0);
        cell.setCellStyle(strStyle);
        if ((i + 1) < 10) {
          cell.setCellValue(txDateE.substring(0, 6) + "0" + (i + 1));
        } else {
          cell.setCellValue(txDateE.substring(0, 6) + (i + 1));
        }
      }
      row = sheet.createRow(days + 5);
      cell = row.createCell(0);
      cell.setCellStyle(strStyle);
      cell.setCellValue("當月出庫累計");
      row = sheet.createRow(days + 6);
      cell = row.createCell(0);
      cell.setCellStyle(strStyle);
      cell.setCellValue("當月入庫累計");
      row = sheet.createRow(days + 7);
      cell = row.createCell(0);
      cell.setCellStyle(strStyle);
      cell.setCellValue("");
      row = sheet.createRow(days + 8);
      cell = row.createCell(0);
      cell.setCellStyle(strStyle);
      cell.setCellValue("庫存");
      row = sheet.createRow(days + 9);
      cell = row.createCell(0);
      cell.setCellStyle(strStyle);
      cell.setCellValue("當月廢卡累計");
      row = sheet.createRow(days + 10);
      cell = row.createCell(0);
      cell.setCellValue("庫位");

      // 寫入column
      for (int i = 0; i < cardItemNum; i++) {
        // 卡樣
        cardItem = sqlStr(i, "card_item");
        row = sheet.getRow(3);
        cell = row.createCell(i + 1);
        cell.setCellStyle(strStyle);
        cell.setCellValue(cardItem);

        // 查詢卡名、出庫量
        lsSql = "select b.name, a.out_qty, a.tx_date " + "from crd_tx_bal a "
            + "left join crd_card_item b on a.card_item = b.card_item " + "where 1=1 ";

        place = sqlStr(i, "place");
        if (!empty(place)) {
          lsSql += "and a.place = :place ";
        }
        if (!empty(txDateE)) {
          lsSql += "and a.tx_date between :txDateS and :txDateE ";
        }
        if (!empty(cardItem)) {
          lsSql += "and a.card_item = :cardItem ";
        }

        lsSql += "order by a.tx_date ";

        setString("txDateS", txDateS);
        setString("txDateE", txDateE);
        setString("place", place);
        setString("cardItem", cardItem);
        sqlSelect(lsSql);
        rowNum = sqlRowNum;

        // 卡名
        cardName = sqlStr("name");
        row = sheet.getRow(4);
        cell = row.createCell(i + 1);
        cell.setCellStyle(strStyle);
        cell.setCellValue(cardName);

        // 出庫量
        for (int j = 0; j < days; j++) {
          row = sheet.getRow(j + 5);
          cell = row.createCell(i + 1);
          cell.setCellStyle(numStyle);
          if ((j + 1) < 10) {
            tDate = txDateE.substring(0, 6) + "0" + (j + 1);
          } else {
            tDate = txDateE.substring(0, 6) + (j + 1);
          }
          for (int k = 0; k < rowNum; k++) {
            txDate = sqlStr(k, "tx_date");
            outQty = sqlStr(k, "out_qty");
            if (txDate.equals(tDate)) {
              cell.setCellValue(outQty);
            }
          }
        }

        // 查詢出庫累計、入庫累計、廢卡累計
        lsSql = "select sum(out_qty) as sum_out_qty, " + "sum(in_qty) as sum_in_qty, "
            + "sum(scrap_qty) as sum_scrap_qty " + "from crd_tx_bal " + "where 1=1 ";

        if (!empty(place)) {
          lsSql += "and place = :place ";
        }
        if (!empty(txDateE)) {
          lsSql += "and substring(tx_date,1,6) = substring(:txDateE,1,6) ";
        }
        if (!empty(cardItem)) {
          lsSql += "and card_item = :cardItem ";
        }

        setString("txDateS", txDateS);
        setString("txDateE", txDateE);
        setString("place", place);
        setString("cardItem", cardItem);
        sqlSelect(lsSql);

        // 當月出庫累計
        sumOutQty = sqlStr("sum_out_qty");
        row = sheet.getRow(days + 5);
        cell = row.createCell(i + 1);
        cell.setCellStyle(numStyle);
        cell.setCellValue(sumOutQty);

        // 當月入庫累計
        sumInQty = sqlStr("sum_in_qty");
        row = sheet.getRow(days + 6);
        cell = row.createCell(i + 1);
        cell.setCellStyle(numStyle);
        cell.setCellValue(sumInQty);

        // 空白
        row = sheet.getRow(days + 7);
        cell = row.createCell(i + 1);
        cell.setCellStyle(strStyle);
        cell.setCellValue("");

        // 查詢庫存
        lsSql = "select total_bal " + "from crd_tx_bal " + "where 1=1 ";

        if (!empty(place)) {
          lsSql += "and place = :place ";
        }
        if (!empty(txDateE)) {
          lsSql += "and tx_date = :txDateE ";
        }
        if (!empty(cardItem)) {
          lsSql += "and card_item = :cardItem ";
        }

        setString("txDateE", txDateE);
        setString("place", place);
        setString("cardItem", cardItem);
        sqlSelect(lsSql);
        totalNum = sqlRowNum;

        // 庫存
        row = sheet.getRow(days + 8);
        cell = row.createCell(i + 1);
        cell.setCellStyle(numStyle);
        if (totalNum > 0) {
          totalBal = sqlStr("total_bal");
          cell.setCellValue(totalBal);
        } else {
          cell.setCellValue("0");
        }

        // 當月廢卡累計
        sumScrapQty = sqlStr("sum_scrap_qty");
        row = sheet.getRow(days + 9);
        cell = row.createCell(i + 1);
        cell.setCellStyle(numStyle);
        cell.setCellValue(sumScrapQty);

        // 查詢庫位中文
        lsSql = "select wf_desc from ptr_sys_idtab " + "where 1=1 and wf_type = 'WH_LOC' "
            + "and wf_id = :wf_id " + "group by wf_id,wf_desc";
        setString("wf_id", place);
        sqlSelect(lsSql);

        if (sqlRowNum > 0) {
          placeC = sqlStr("wf_desc");
        }

        // 庫位
        row = sheet.getRow(days + 10);
        cell = row.createCell(i + 1);
        cell.setCellStyle(numberStyle);
        cell.setCellValue(placeC);
      }

      for (int i = 0; i <= cardItemNum; i++) {
        sheet.autoSizeColumn(i);
        sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 17 / 10);
      }
    }

    wp.exportFile = "crdr0011-" + wp.sysDate.substring(4) + "-" + wp.sysTime + ".xlsx";
    wp.fileMode = "Y";
    if (wp.fileMode.equals("Y")) {
      wp.linkMode = "Y";
      wp.linkURL = wp.getWorkPath(wp.exportFile);

      try (FileOutputStream outExcelFile = new FileOutputStream(new File(TarokoParm.getInstance().getWorkDir() + wp.exportFile));) {
	      wb.write(outExcelFile);
	      outExcelFile.close();
      }
    } else {
      wp.exportXls = "Y";
      wp.ba = new ByteArrayOutputStream();
      wb.write(wp.ba);
      wp.ba.close();
    }

    // 設置輸出excle變數為Y
    wp.exportXls = "Y";
    // 從exportFile讀取文件内容到wb對象
    wp.setDownload(wp.exportFile);
    // 設置輸出源内容為N
    wp.exportSrc = "N";
  }

}
