/*****************************************************************************
 *  報表公用程式2 V.2019-0103
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  19-0103:    JH       initial
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                               *
* 109-07-17  V1.00.01  Zuwei       兆豐國際商業銀行 => 合作金庫商業銀行                                                 *
*  109-07-24  V1.00.01  Zuwei       coding standard                          *
* 109-09-30   V1.00.02  JustinWu   change the way to get URL                 *
* 109-12-28    V1.00.03 JustinWu    zz -> comm                               *
* 110-01-08  V1.00.02   shiyuqi    修改無意義的命名                          *
* 110-09-22  V1.00.13   Justin   use old version itext                       *
* 111-01-21  V1.00.14  Justin       fix Redundant Null Check
* ******************************************************************************/
package taroko.com;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class TarokoPDF22 {
  taroko.base.CommString commString = new taroko.base.CommString();

  public int pageCount = 19; // 每頁資料筆數
  public float fullWidth = 95; // 頁面寬度百分比
  public boolean pageVert = false; // 橫印或直印
  public boolean showWaterMark = false; // 是否列印 浮水印
  public String excelTemplate = ""; // EXCEL 樣版檔
  public int sheetNo = 0, maxColumn = 0;

  XSSFWorkbook wb = null;
  XSSFSheet sheet = null;
  XSSFRow row = null;
  XSSFCell cell = null;
  XSSFRow[] listRow = new XSSFRow[10];
  XSSFRow[] summRow = new XSSFRow[50];
  XSSFRow[] footRow = new XSSFRow[10];
  int iisummRow = 0, iiFootRow = 0;
  String[] summkk = new String[50];

  private String listEnded = "", pagePrevData = "", firstFlag = "";
  private int rowSpanCount = 0, checkRowspan = 0, browsePoint = 0, browseRow = 0, multiDataRow = 0;
  private int sumEndRow = 0, sumLine = 0, footEndRow = 0, wpIndx = 0;
  private int rowNum = 0, listPnt = 0, pageNo = 0, controlLine = 0, lineCount = 0;
  private int columnCount = 0, browseColumn = 0, titlePoint = 0, maxRow = 0;

  InputStream inExcelFile = null;
  XSSFCellStyle cellStyle = null;
  int colLimit = 100; // COLUMN 限制
  int rowLimit = 100; // ROW 限制
  int cm = 100; // 一筆多列 限制
  int bL = 50; // 30; // 小計 限制

  float[][] rowHeight = new float[cm + bL][rowLimit];

  String[][] cellValue = new String[cm + bL][colLimit];
  String[][] fontName = new String[cm + bL][colLimit];
  float[][] topBorder = new float[cm + bL][colLimit];
  float[][] leftBorder = new float[cm + bL][colLimit];
  float[][] rightBorder = new float[cm + bL][colLimit];
  float[][] bottomBorder = new float[cm + bL][colLimit];
  short[][] fontSize = new short[cm + bL][colLimit];
  short[][] fontAlign = new short[cm + bL][colLimit];
  short[][] fontBold = new short[cm + bL][colLimit];
  // byte[][][] bgColor = new byte[cm+bL][colLimit][3];

  int[] colWidth = new int[colLimit];
  float[] widths = null;

  String[] headField = new String[bL]; // 儲存 控制欄位名稱
  String[] breakField = new String[bL]; // 儲存 BREAK 小計欄位名稱
  String[] prevData = new String[bL]; // 儲存 BREAK 前一筆資料
  String[] compData = new String[bL]; // 儲存 BREAK 小計比較值
  String[][] sumField = null; // new String[bL][colLimit]; // 儲存加總欄位名稱
  double[][] sumValue = null; // new double[bL][colLimit]; // 儲存小計之加總值
  double[][] subSumCount = new double[bL][colLimit]; // 儲存小計之加總筆數

  double[][] totalValue = new double[bL][colLimit];
  double[][] totalCount = new double[bL][colLimit];

  private String pageBreakField = "";
  private boolean changePage = false, skipRow = false, fileEnd = false, titleFlag = false,
      browseMode = false, multiBrowse = false;
  private boolean imageField = false, subData = false, barCode39 = false;

  HashMap<String, Integer> rowHash = new HashMap<String, Integer>();
  HashMap<String, Integer> colHash = new HashMap<String, Integer>();
  HashMap<String, String> skipHash = new HashMap<String, String>();

  HashMap<String, String> fieldHash = new HashMap<String, String>();
  Document document = null;
  BaseFont sFont = null, nFont = null;
  PdfPTable pTable = null;
  PdfWriter writer = null;
  PdfContentByte addWaterMark = null;
  Image img = null;
  TarokoCommon wp = null;

  boolean isSumm = false, isFooter = false, isChangePage = false;
  private boolean summField = false;
  private double imSummValue = 0;
  int ilProcCnt = 0;

  // 產生 PDF 報表
  public void procesPDFreport(TarokoCommon wr) throws Exception {
    this.wp = wr;
    if (wpIndx >= 0)
      if (wp.listCount[wpIndx] <= 0) {
        wp.log("列印筆數為 0");
        return;
      }

    FileOutputStream pdfFile = null;
    try {
      if (pageVert) {
        document = new Document(PageSize.A4, 10, 10, 10, 10);
      } else {
        document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
      }

      // -output-filename-
      int liPos = this.excelTemplate.indexOf(".");
      String fileName = excelTemplate;
      if (liPos > 0) {
        fileName = excelTemplate.substring(0, liPos);
      }

      // wp.exportFile = file_name + "-" + wp.sysDate.substring(4) + "_" + wp.sysTime + ".pdf";
      wp.exportFile =
          wp.sysDate.substring(6) + "u" + wp.loginUser + "_" + fileName + "-" + wp.sysTime
              + ".pdf";

      wp.fileMode = "Y"; // modify jack 2018/07/18
      if (wp.fileMode.equals("Y")) {
        wp.linkMode = "Y";
        wp.linkURL = wp.getWorkPath(wp.exportFile);

        // pdfFile = new FileOutputStream(wp.dataRoot +
        // "/work/"+wp.exportFile);
        pdfFile = new FileOutputStream(TarokoParm.getInstance().getWorkDir() + wp.exportFile);
        writer = PdfWriter.getInstance(document, pdfFile);
      } else {
        wp.exportPdf = "Y";
        wp.ba = new ByteArrayOutputStream();
        writer = PdfWriter.getInstance(document, wp.ba);
      }

      String dataRoot = TarokoParm.getInstance().getDataRoot();
	  sFont = BaseFont.createFont(dataRoot + "/font/kaiu.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
	  nFont = BaseFont.createFont(dataRoot + "/font/SIMYOU.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

      document.open();
      // if (showWaterMark) {
      // pdfWaterMark(); // 處理浮水印
      // addWaterMark.addImage(img);
      // }
      getExcelRowCount();
      printReport();

      document.close();
      if (wp.fileMode.equals("Y")) {
        if (pdfFile != null) {
          pdfFile.close();
        }
        pdfFile = null;
      } else {
        wp.ba.close();
      }
      rowHash.clear();
      colHash.clear();
      skipHash.clear();
    }

    catch (Exception ex) {
      wp.expMethod = "procesPDFreport";
      wp.expHandle(ex);
      return;
    }

    finally {
      try {
        if (pdfFile != null) {
          pdfFile.close();
          pdfFile = null;
        }
      } catch (Exception ex2) {
      }
    }
  }

  // 取得 EXCEL TEMPLATE 總 ROW COUNT
  private void getExcelRowCount() throws Exception {

    // for (int i = 0; i < bL; i++) {
    // headField[i] = "";
    // breakField[i] = "";
    // prevData[i] = "";
    // compData[i] = "";
    // firstFlag = "Y";
    // for (int j = 0; j < colLimit; j++) {
    // subSumValue[i][j] = 0;
    // subSumCount[i][j] = 0;
    // totalValue[i][j] = 0;
    // totalCount[i][j] = 0;
    // sumField[i][j] = "";
    // }
    // }

    inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot()  + "/ExcelTemplate/" + excelTemplate);
    wb = new XSSFWorkbook(inExcelFile);
    sheet = wb.getSheetAt(sheetNo);

    spanControl();
    browseRow = 9999;
    Iterator rowIterator = sheet.rowIterator();
    int llRow = 0, liListCnt = 0;
    boolean lbRowEnd = false, lbSummRow = false;
    Iterator rowIt = sheet.rowIterator();
    while (rowIterator.hasNext()) {
      llRow++;
      row = (XSSFRow) rowIterator.next();
      for (int j = 0; j < row.getLastCellNum(); j++) {
        if (llRow > 1 && j > 0) {
          break;
        }
        // -column over---
        if (maxColumn > 0 && j >= maxColumn) {
          break;
        }

        cell = null;
        cell = row.getCell(j);
        if (cell == null) {
          continue;
        }

        String cellData = getCellData(cell);
        if (commString.empty(cellData)) {
          continue;
        }

        if (j == 0 && commString.eqIgno(cellData, "###"))
          continue;

        if (j == 0 && commString.strIn2(cellData, ",#EEE,END")) {
          maxRow = row.getRowNum() - 1;
          lbRowEnd = true;
          break;
        }
        if (j > 0 && commString.eqIgno(cellData, "#EEE") && maxColumn == 0) {
          maxColumn = j;
          continue;
        }

        if (j == 0 && commString.eqIgno(cellData, "TITLE")) {
          titlePoint = row.getRowNum();
          titleFlag = true;
          continue;
        }
        if (j == 0 && commString.strIn2(cellData, ",LLLL,LIST")) {
          listRow[liListCnt] = row;
          liListCnt++;
          continue;
        }
        // 紀錄頁尾列--
        if (j == 0 && commString.eqIgno(cellData, "FOOTER")) {
          isFooter = true;
          footRow[iiFootRow] = row;
          iiFootRow++;
          footEndRow = row.getRowNum();
          continue;
        }

        // -sum[合計,小計],sum-P[小計換頁],sum-G[小計不換頁],sum-T[總計]-
        if (j == 0) {
          String[] checkCell = {"", "", ""};
          if (cellData.length() > 3) {
            checkCell = cellData.split(",");
            checkCell[0] = checkCell[0].toUpperCase();
          }
          if (Arrays.asList("SUM", "SUM-P", "SUM-G", "SUM-T").contains(checkCell[0])) {
            isSumm = true;
            summRow[iisummRow] = row;
            summkk[iisummRow] = ""; // "##TOTAL";
            if (checkCell.length > 1) {
              summkk[iisummRow] = checkCell[1].trim();
            }
            iisummRow++;
          }
        }
      } // end of for loop
      if (lbRowEnd)
        break;
    } // end of while loop

    if (isSumm) {
      summColumn();
    }

    inExcelFile.close();
    inExcelFile = null;
  }

  void summColumn() throws Exception {
    sumField = new String[iisummRow][maxColumn];
    sumValue = new double[iisummRow][maxColumn];

    for (int ll = 0; ll < iisummRow; ll++) {
      row = summRow[ll];
      for (int ii = 0; ii < maxColumn; ii++) {
        sumField[ll][ii] = "";
        sumValue[ll][ii] = 0;

        cell = null;
        cell = row.getCell(ii);
        if (cell == null) {
          continue;
        }
        String str = getCellData(cell);
        if (commString.empty(str))
          continue;
        int cnt = str.toUpperCase().indexOf("{SUM-");
        if (cnt < 0)
          continue;

        str = str.substring(cnt + 5, str.length() - 1).trim();
        cnt = str.indexOf(".");
        if (cnt < 0)
          sumField[ll][ii] = str;
        else
          sumField[ll][ii] = str.substring(0, cnt);
      }
    }
  }

  // TABLE 跨欄 跨列處理 ROW-SPAN ,COLUMN-SPAN
  public void spanControl() throws Exception {

    int regionNumber = sheet.getNumMergedRegions();
    for (int i = 0; i < regionNumber; i++) {

      CellRangeAddress region = sheet.getMergedRegion(i);
      int spanRowFirst = region.getFirstRow();
      int spanRowLast = region.getLastRow();
      int spanColFirst = region.getFirstColumn();
      int spanColLast = region.getLastColumn();

      // 紀錄 COLUMN-SPAN 欄位
      int col = spanColLast - spanColFirst;
      if (col > 0) {
        colHash.put((spanRowFirst + "#" + spanColFirst), (col + 1));
        // wp.ddd("col[%s],[%s]", spanRowFirst + "#" + spanColFirst, (m + 1));
      }

      // 紀錄 ROW-SPAN 欄位
      int row = spanRowLast - spanRowFirst;
      if (row > 0) {
        rowHash.put((spanRowFirst + "#" + spanColFirst), (row + 1));
        // wp.ddd("row[%s],[%s]", spanRowFirst + "#" + spanColFirst, (n + 1));
      }

      // 紀錄 ROW-SPAN ,COLUMN-SPAN 產生之重覆欄位
      if (col > 0 || row > 0) {
        for (int j = spanRowFirst; j <= spanRowLast; j++) {
          for (int k = spanColFirst; k <= spanColLast; k++) {
            if (j == spanRowFirst && k == spanColFirst) {
              continue;
            }
            skipHash.put((j + "#" + k), "Y");
            // wp.ddd("skip:[%s]", "" + j + "#" + k);
          }
        }
      }
    } // end of for loop

    return;
  } // end of spanControl

  private void printReport() throws Exception {

    printHeader();

    // -print list data-
    for (int ll = 0; ll < wp.listCount[wpIndx]; ll++) {
      if (ilProcCnt >= pageCount) {
        printFooter();
        document.newPage();
        printHeader();
        ilProcCnt = 0;
      }
      ilProcCnt++;
      printListdata(ll);
    }
    isChangePage = false;
    printSummary(wp.listCount[wpIndx]);
    printTotal();
    printFooter();
  }

  private void printHeader() throws Exception {
    String lsCellData = "";

    pageNo++;
    if (pageNo <= 1) {
      wp.colSet("sys_dtime", wp.dispDate.substring(2) + " " + wp.dispTime);
      wp.setValue("SYS_DATE", wp.dispDate, 0);
      wp.setValue("SYS_TIME", wp.dispTime, 0);
      wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);
    }
    wp.setValue("PAGE_NO", "" + pageNo, 0);

    document.newPage();

    Iterator rowIterator = sheet.rowIterator();
    while (rowIterator.hasNext()) {
      row = (XSSFRow) rowIterator.next();
      cell = row.getCell(0);
      lsCellData = getCellData(cell);
      if (lsCellData.length() != 0 && !lsCellData.equals("TITLE"))
        break;
      if (commString.eqIgno(lsCellData, "EEE") || commString.eqIgno(lsCellData, "#END"))
        break;
      // -無效row-
      if (commString.eqIgno(lsCellData, "###"))
        continue;

      rowInitial();
      for (int kk = 0; kk < columnCount; kk++) {
        cell = null;
        cellStyle = null;
        cell = row.getCell(kk);
        colWidth[kk] = sheet.getColumnWidth(kk);
        if (cell == null) {
          cellStyle = wb.createCellStyle();
          setCellValue(0, kk, "");
          continue;
        }

        String ss = commString.nvl(getCellData(cell));
        // if (k == 0 && ss.equals("###")) {
        // skipRow = true;
        // break;
        // }

        cellStyle = cell.getCellStyle();
        setCellValue(0, kk, ss);
        // if (rowNum < browsePoint || rowNum > sumEndRow || !browseMode) {
        // setCellValue(0, 0, k);
        // }
      } // -- end for loop

      // if (skipRow) {
      // continue;
      // }

      // if (rowNum >= browsePoint && rowNum <= browseRow && browseMode) {
      // multiDataRow++;
      // browseColumn = columnCount;
      // }
      // if (rowNum >= browsePoint && rowNum <= sumEndRow && browseMode) {
      // controlLine++;
      // }

      // if ((rowNum < browsePoint || rowNum > sumEndRow || !browseMode)
      // && checkRowspan >= rowSpanCount) {
      // setColumnWidths();
      // document.add(pTable);
      // pTable = null;
      // }
      if (checkRowspan >= rowSpanCount) {
        setColumnWidths();
        document.add(pTable);
        pTable = null;
      }

    }
  }

  private void printListdata(int llRow) throws Exception {
    if (llRow > 0) {
      printSummary(llRow);
    }

    for (int ii = 0; ii < listRow.length; ii++) {
      if (listRow[ii] == null)
        break;

      row = listRow[ii];
      rowInitial();
      for (int kk = 0; kk < columnCount; kk++) {
        cell = null;
        cellStyle = null;
        cell = row.getCell(kk);
        colWidth[kk] = sheet.getColumnWidth(kk);
        if (cell == null || kk == 0) {
          cellStyle = wb.createCellStyle();
          setCellValue(llRow, kk, "");
          continue;
        }

        String str = commString.nvl(getCellData(cell));
        cellStyle = cell.getCellStyle();
        setCellValue(llRow, kk, str);
      }

      if (checkRowspan >= rowSpanCount) {
        setColumnWidths();
        document.add(pTable);
        pTable = null;
      }
    }

    // -summary-
    for (int ll = 0; ll < iisummRow; ll++) {
      String col = "";
      // -key-value-
      if (commString.empty(summkk[ll]) == false) {
        col = "kk" + ll + "-" + summkk[ll];
        wp.colSet(col, wp.colStr(llRow, summkk[ll]));
      }
      for (int ii = 0; ii < maxColumn; ii++) {
        col = sumField[ll][ii];
        if (commString.empty(col))
          continue;
        if (commString.eqIgno(col, "rowcount"))
          sumValue[ll][ii] += 1;
        else
          sumValue[ll][ii] += wp.colNum(llRow, col);
      }
    }
  }

  private void printSummary(int llRow) throws Exception {
    // 列印小計--
    if (isSumm == false)
      return;

    String col = "";
    boolean lbChgPage = false;
    for (int ll = 0; ll < iisummRow; ll++) {

      if (commString.empty(summkk[ll])) {
        continue;
      }

      String str = wp.colStr("kk" + ll + "-" + summkk[ll]);
      // -Key相同:不處理-
      if (commString.eqAny(str, wp.colStr(llRow, summkk[ll])))
        continue;

      // -break-
      lbChgPage = false;
      row = summRow[ll];
      rowInitial();
      for (int ii = 0; ii < maxColumn; ii++) {
        cell = null;
        cellStyle = null;
        cell = row.getCell(ii);
        colWidth[ii] = sheet.getColumnWidth(ii);
        String lsCell = getCellData(cell);
        if (cell == null || ii == 0) {
          cellStyle = wb.createCellStyle();
          setCellValue(llRow, ii, "");
          if (commString.strIn2("SUM-P,", lsCell.toUpperCase())) {
            lbChgPage = true;
          }
          continue;
        }

        // --
        cellStyle = cell.getCellStyle();
        summField = false;
        if (!commString.empty(sumField[ll][ii])) {
          summField = true;
          imSummValue = sumValue[ll][ii];
          sumValue[ll][ii] = 0;
        }
        setCellValue(llRow, ii, lsCell);
      }

      if (checkRowspan >= rowSpanCount) {
        setColumnWidths();
        document.add(pTable);
        pTable = null;
        if (lbChgPage && llRow < wp.listCount[wpIndx]) {
          printFooter();
          document.newPage();
          printHeader();
          ilProcCnt = 0;
        }
      }

    }

  }

  private void printTotal() throws Exception {
    // -列印總計--
    if (isSumm == false)
      return;

    int llRow = wp.listCount[wpIndx];

    String col = "";
    for (int ll = 0; ll < iisummRow; ll++) {

      // -小計不處理-
      if (commString.empty(summkk[ll]) == false) {
        continue;
      }

      // -break-
      row = summRow[ll];
      rowInitial();
      for (int ii = 0; ii < maxColumn; ii++) {
        cell = null;
        cellStyle = null;
        cell = row.getCell(ii);
        colWidth[ii] = sheet.getColumnWidth(ii);
        if (cell == null || ii == 0) {
          cellStyle = wb.createCellStyle();
          setCellValue(llRow, ii, "");
          continue;
        }
        String lsCell = getCellData(cell);
        cellStyle = cell.getCellStyle();
        summField = false;
        if (!commString.empty(sumField[ll][ii])) {
          summField = true;
          imSummValue = sumValue[ll][ii];
          sumValue[ll][ii] = 0;
        }
        setCellValue(llRow, ii, lsCell);
      }

      if (checkRowspan >= rowSpanCount) {
        setColumnWidths();
        document.add(pTable);
        pTable = null;
      }

    }

  }

  private void printFooter() throws Exception {
    if (isFooter == false)
      return;

    if (this.footEndRow > 0) {
      for (int i = 0; i < pageCount - ilProcCnt; i++) {
        nullTableRow();
      }
    }

    for (int ll = 0; ll < iiFootRow; ll++) {

      row = footRow[ll];
      rowInitial();
      for (int ii = 0; ii < maxColumn; ii++) {
        cell = null;
        cellStyle = null;
        cell = row.getCell(ii);
        colWidth[ii] = sheet.getColumnWidth(ii);
        if (cell == null || ii == 0) {
          cellStyle = wb.createCellStyle();
          setCellValue(0, ii, "");
          continue;
        }
        String lsCell = getCellData(cell);
        cellStyle = cell.getCellStyle();
        setCellValue(0, ii, lsCell);
      }

      if (checkRowspan >= rowSpanCount) {
        setColumnWidths();
        document.add(pTable);
        pTable = null;
      }

    }

  }

  private String getCellData(XSSFCell cell) {

    if (cell == null)
      return "";

    String lsCellData = "";

    if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
      lsCellData = cell.getStringCellValue();
    } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
      lsCellData = "" + cell.getNumericCellValue();
    }
    if (lsCellData == null) {
      lsCellData = "";
    }

    return lsCellData;
  }

  private void rowInitial() throws Exception {
    rowNum = row.getRowNum();
    // if (rowNum < browsePoint || rowNum > sumEndRow) {
    // resetParameter();
    // }
    columnCount = row.getLastCellNum();
    if (columnCount >= maxColumn && maxColumn > 0) {
      columnCount = maxColumn;
    }

    if (columnCount < 0) {
      nullTableRow();
      return;
    }

    checkRowspan++;
    if (pTable == null) { // if (rowSpanCount < checkRowspan) {
      pTable = new PdfPTable(columnCount);
      rowSpanCount = 0;
      checkRowspan = 1;
    }
    int liCol = pTable.getNumberOfColumns();
    // widths = new float[columnCount];
    widths = new float[liCol];
    for (int i = 0; i < widths.length; i++) {
      widths[i] = 0;
    }

    // int j = controlLine;
    // rowHeight[j][rowNum] = row.getHeightInPoints();
  }

  void nullTableRow() throws Exception {
    cellStyle = wb.createCellStyle();
    pTable = new PdfPTable(1);
    setCellValue(0, 0, "");
    pTable.setWidthPercentage(fullWidth);
    // wp.ddd("--->docu.newPage; nullTableRow");
    document.add(pTable);
    rowSpanCount = 0;
    checkRowspan = 0;
    pTable = null;

    return;
  }

  public void setCellValue(int llData, int liCc, String str) throws Exception {

    String fieldName = "", cvtData = "", cellData = "";
    int rowCntl = row.getRowNum();
    int cnt = liCc;

    // 跳過 ROW SPAN,COLUMN SPAN 產生之重覆欄位
    String checkSkip = (String) skipHash.get((rowCntl + "#" + cnt));
    if (checkSkip != null) {
      return;
    }

    cellData = str;

    int start = cellData.indexOf("{");
    int end = cellData.indexOf("}");

    if (start != -1 && end != -1) {
      fieldName = cellData.substring(start + 1, end).trim();
      imageField = false;
      barCode39 = false;
      // 處理 IMAGE 顯示
      if (fieldName.length() > 5 && fieldName.substring(0, 5).equals("#IMG:")) {
        fieldName = fieldName.substring(5);
        imageField = true;
      } else if (fieldName.length() > 7 && fieldName.substring(0, 7).equals("#BAR39:")) {
        fieldName = fieldName.substring(7);
        barCode39 = true;
      }
      if (summField) {
        wp.convertField(fieldName);
        cvtData = wp.convertFormat(fieldName, "" + imSummValue);
      } else {
        cvtData = cvtFunction(fieldName, llData);
      }

      cellData = cellData.substring(0, start) + cvtData + cellData.substring(end + 1);
    }

    BaseFont bFont = nFont;
    // -字型-
    XSSFFont xfont = cellStyle.getFont();
    if (commString.eqIgno(xfont.getFontName(), "標楷體")) {
      bFont = sFont;
    }
    // -粗體-
    Font fon = new Font(bFont, xfont.getFontHeightInPoints());
    if (xfont.getBold())
      fon.setStyle(Font.BOLD);

    PdfPCell pCell = null;
    if (imageField) {
      imageField = false;
      String imgPath = TarokoParm.getInstance().getDataRoot()  + "/ExcelTemplate/" + fieldName;
      Image img = Image.getInstance(imgPath);
      pCell = new PdfPCell(new PdfPCell(img, true));
    } else if (barCode39) {
      barCode39 = false;
      BaseFont barfont =
	  BaseFont.createFont(TarokoParm.getInstance().getDataRoot() + "/font/V100016_39code.TTF",
						BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
      Font fon2 = new Font(barfont);
      fon2.setSize(xfont.getFontHeightInPoints());
      pCell = new PdfPCell(new Paragraph(cellData, fon2));
    } else {
      fon.setSize(xfont.getFontHeightInPoints());
      pCell = new PdfPCell(new Paragraph(cellData, fon));
    }

    pCell.setNoWrap(true);
    // if (rowCntl == titlePoint && titleFlag) {
    // pCell.setBackgroundColor(new BaseColor(244, 244, 244));
    // }

    pCell.setVerticalAlignment(Element.ALIGN_CENTER);
    if (cellStyle.getAlignmentEnum() == HorizontalAlignment.CENTER) {
      pCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    } else if (cellStyle.getAlignmentEnum() == HorizontalAlignment.RIGHT) {
      pCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      pCell.setPaddingRight(3f);
    } else {
      pCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      pCell.setPaddingLeft(3f);
    }
    if (rowCntl == titlePoint && titleFlag) {
      pCell.setBackgroundColor(new Color(244, 244, 244));
    }

    // 處理 ROW SPAN
    Integer checkRow = (Integer) rowHash.get((rowCntl + "#" + cnt));
    if (checkRow != null) {
      pCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
      pCell.setRowspan(checkRow);
      rowSpanCount = checkRow;
    }

    // 處理 COLUMN SPAN
    Integer checkCol = (Integer) colHash.get((rowCntl + "#" + cnt));
    if (checkCol != null) {
      pCell.setColspan(checkCol);
    }

    pCell.setBorderWidthTop(cellStyle.getBorderTopEnum().getCode());
    pCell.setBorderWidthRight(cellStyle.getBorderRightEnum().getCode());
    pCell.setBorderWidthBottom(cellStyle.getBorderBottomEnum().getCode());
    pCell.setBorderWidthLeft(cellStyle.getBorderLeftEnum().getCode());
    pCell.setFixedHeight(row.getHeightInPoints());
    // wp.ddd("[%s]-row-H[%s]",row.getRowNum(),row.getHeightInPoints());

    pTable.addCell(pCell);
    pCell = null;
    return;
  }

  public boolean checkBreakField(String checkField) throws Exception {
    for (int i = 0; i < sumLine; i++) {
      if (checkField.equals(breakField[i]) && checkField.length() > 0) {
        return true;
      }
    }
    return false;
  }

  // 處理 輸出資料顯示轉換
  public String cvtFunction(String cvtField, int i) throws Exception {
    cvtField = wp.convertField(cvtField);
    if (wp.descField.equals("JAVA")) {
      wp.orgField = cvtField;
      wp.userTagRr = i;
    }
    String cvtData = wp.getValue(cvtField, i);
    return wp.convertFormat(cvtField, cvtData);
  }

  // 處理欄位寬度比例
  public void setColumnWidths() throws Exception {

    float totalWidth = 0;
    colWidth[0] = 0; // 隱藏控制欄位
    for (int i = 0; i < widths.length; i++) {
      totalWidth += colWidth[i];
    }

    for (int i = 0; i < widths.length; i++) {
      widths[i] = ((float) colWidth[i] / totalWidth);
    }

    // int li_col =pTable.getNumberOfColumns();
    // wp.ddd("pdf: width[%s], col[%s]",widths.length,li_col);
    pTable.setWidths(widths);
    pTable.setWidthPercentage(fullWidth);

  }

}
