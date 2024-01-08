/*****************************************************************************
 *   PDF公用程式-V.19-0116
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
   19-0116:    JH    調整邊界(-10,10,10,10)
   18-1228:    JH       exportFile=DD-uXXXX_report-hhmiss.PDF
 * 2018-1224:  Liao     barCode39
 * 2018-1123:  JH       bL: 30>>50, widths[pTable.colNum]
 * 2018-1016: JH      SUM-G
 * 2018-1012: JH      subData, 多一空白頁[footEndRow]
 * 2018-0522:  Alex     BL擴大至30
 * 2018-0316: Jack    workDir
 * 2018-0315: JH    output name
 * 2019-0527: Jack, Liao modify title
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-07-17  V1.00.01  Zuwei       兆豐國際商業銀行 => 合作金庫商業銀行      *
*  109-07-24  V1.00.01  Zuwei       coding standard      *
*  109-09-04  V1.00.01  yanghan     解决Portability Flaw: Locale Dependent Comparison问题    * 
* 109-09-30   V1.00.02  JustinWu    change the way to get URL
* 110-01-07  V1.00.03  tanwei        修改意義不明確變量                              
* 110-01-08  V1.00.02   shiyuqi       修改无意义命名                                            *
* 110-09-22  V1.00.03  Justin   use old version itext
******************************************************************************/
package taroko.com;

import java.io.*;
import java.util.Iterator;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import java.util.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoPDF2 {

  public int pageCount = 19; // 每頁資料筆數
  public float fullWidth = 95; // 頁面寬度百分比
  public boolean pageVert = false; // 橫印或直印
  public boolean showWaterMark = false; // 是否列印 浮水印
  public String excelTemplate = ""; // EXCEL 樣版檔
  public int sheetNo = 0, maximumColumn = 0;
  public boolean noTrim = false;

  public String[] fixHeader = {"", "", "", "", "", "", "", "", "", ""}; // 每頁 HERDER 固定資料之欄位名稱

  XSSFWorkbook wb = null;
  XSSFSheet sheet = null;
  XSSFRow row = null;
  XSSFCell cell = null;

  private String listEnded = "";
  private int rowSpanCount = 0, checkRowspan = 0, browsePoint = 0, browseRow = 0, multiDataRow = 0;
  private int sumEndRow = 0, sumLine = 0, footEndRow = 0, wpIndx = 0;
  private int rowNum = 0, listPnt = 0, pageNo = 0, controlLine = 0, lineCount = 0;
  private int columnCount = 0, browseColumn = 0, titlePoint = 0, maximumRow = 0;

  InputStream inExcelFile = null;

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
  String[][] sumField = new String[bL][colLimit]; // 儲存加總欄位名稱
  double[][] subSumValue = new double[bL][colLimit]; // 儲存小計之加總值
  double[][] subSumCount = new double[bL][colLimit]; // 儲存小計之加總筆數

  double[][] totalValue = new double[bL][colLimit];
  double[][] totalCount = new double[bL][colLimit];

  private String pageBreakField = "";
  private boolean changePage = false, skipRow = false, fileEnd = false, titleFlag = false,
      browseMode = false, multiBrowse = false;
  private boolean imageField = false, subData = false, barCode39 = false;
  private float[] docuMarge = new float[] {0, 10, 10, 0};

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


  public void setDocuMarge(float lNum, float rNum, float tNum, float bNum) {
    docuMarge[0] = lNum;
    docuMarge[1] = rNum;
    docuMarge[2] = tNum;
    docuMarge[3] = bNum;
  }

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
        document = new Document(PageSize.A4); // , 10, 10, 10, 10);
      } else {
        document = new Document(PageSize.A4.rotate()); // , 10, 10, 10, 10);
      }
      // document.setMargins(0,10,10,0);
      document.setMargins(docuMarge[0], docuMarge[1], docuMarge[2], docuMarge[3]);

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
	  sFont =
          BaseFont.createFont(dataRoot  + "/font/kaiu.ttf", BaseFont.IDENTITY_H,
              BaseFont.NOT_EMBEDDED);
      nFont =
          BaseFont.createFont(dataRoot  + "/font/SIMYOU.TTF", BaseFont.IDENTITY_H,
              BaseFont.NOT_EMBEDDED);

      document.open();
      // if (showWaterMark) {
      // pdfWaterMark(); // 處理浮水印
      // addWaterMark.addImage(img);
      // }
      getExcelRowCount();
      processExcelSheet();

      while (!listEnded.equals("Y")) {
        // wp.ddd("--->docu.newPage");
        document.newPage();
        // if (showWaterMark) {
        // addWaterMark.addImage(img);
        // }
        processExcelSheet();
        if (listEnded.equals("Y")) {
          break;
        }
      }

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

    for (int i = 0; i < bL; i++) {
      headField[i] = "";
      breakField[i] = "";
      prevData[i] = "";
      compData[i] = "";
//      firstFlag = "Y"; // Justin
      for (int j = 0; j < colLimit; j++) {
        subSumValue[i][j] = 0;
        subSumCount[i][j] = 0;
        totalValue[i][j] = 0;
        totalCount[i][j] = 0;
        sumField[i][j] = "";
      }
    }

    inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot()  + "/ExcelTemplate/" + excelTemplate);
    wb = new XSSFWorkbook(inExcelFile);
    sheet = wb.getSheetAt(sheetNo);
    spanControl();
    browseRow = 9999;

    int llRow = 0;
    boolean lbRowEnd = false;

    Iterator rowIterator = sheet.rowIterator();
    while (rowIterator.hasNext()) {
      llRow++;
      row = (XSSFRow) rowIterator.next();
      for (int j = 0; j < row.getLastCellNum(); j++) {
        // -column over---
        if (maximumColumn > 0 && j >= maximumColumn) {
          break;
        }

        cell = null;
        cell = row.getCell(j);
        if (cell == null) {
          continue;
        }
        String cellData = "";
        if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
          cellData = cell.getStringCellValue();
        } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
          cellData = "" + cell.getNumericCellValue();
        }
        if (cellData == null) {
          cellData = "";
        } else
          cellData = cellData.trim();
        if (cellData.length() == 0) {
          continue;
        }

        if (j == 0 && (cellData.equalsIgnoreCase("#EEE") || cellData.toUpperCase(Locale.TAIWAN).equals("END"))) {
          maximumRow = row.getRowNum() - 1;
          lbRowEnd = true;
          break;
        }
        if (j > 0 && cellData.equalsIgnoreCase("#EEE") && maximumColumn == 0) {
          maximumColumn = j;
        }
        if (j == 0 && cellData.toUpperCase(Locale.TAIWAN).equals("TITLE")) {
          titlePoint = row.getRowNum();
          titleFlag = true;
        }
        if (j == 0 && cellData.toUpperCase(Locale.TAIWAN).equals("LLLL") && !multiBrowse) {
          browsePoint = row.getRowNum();
          multiBrowse = true;
        }
        if (j == 0 && cellData.toUpperCase(Locale.TAIWAN).equals("LIST")) {
          browseMode = true;
          browseRow = row.getRowNum();
          if (!multiBrowse) {
            browsePoint = browseRow;
          }
        }

        String[] checkCell = {"", "", ""};
        if (j == 0 && cellData.length() > 3) {
          checkCell = cellData.split(",");
          checkCell[0] = checkCell[0].toUpperCase(Locale.TAIWAN);
        }

        // -sum[合計,小計],sum-P[小計換頁],sum-G[小計不換頁],sum-T[總計]-
        if (j == 0 && Arrays.asList("SUM", "SUM-P", "SUM-G", "SUM-T").contains(checkCell[0])) {
          sumEndRow = row.getRowNum();
          headField[sumLine] = checkCell[0].trim();
          if (Arrays.asList("SUM", "SUM-P", "SUM-G").contains(checkCell[0])) {
            breakField[sumLine] = checkCell[1].trim();
          }
          if (checkCell[0].equals("SUM-P")) {
            pageBreakField = breakField[sumLine];
          }

          sumLine++;
        }

        int line = sumLine - 1;
        int pnt = cellData.toUpperCase().indexOf("{SUM-");
        if (pnt != -1) {
          sumField[line][j] = cellData.substring(pnt + 1, cellData.length() - 1).trim();
        }

        if (j == 0 && cellData.toUpperCase(Locale.TAIWAN).equals("FOOTER")) { // 紀錄頁尾列
          footEndRow = row.getRowNum();
        }

      } // end of for loop
      if (lbRowEnd)
        break;
    } // end of while loop

    // wp.ddd("-->rowCount=%s, colCnt=%s",ll_row, maximumColumn);

    if (sumEndRow == 0) {
      sumEndRow = browseRow;
    }

    inExcelFile.close();
    inExcelFile = null;
  }

  // 讀取 EXCEL TEMLATE 產生 PDF 報表
  private void processExcelSheet() throws Exception {

    inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot()  + "/ExcelTemplate/" + excelTemplate);
    wb = new XSSFWorkbook(inExcelFile);

    resetParameter();
    pageNo++;
    multiDataRow = 0;
    controlLine = 0;
    lineCount = 0;
    wp.setValue("SYS_DATE", wp.dispDate, 0);
    wp.setValue("SYS_TIME", wp.dispTime, 0);
    wp.setValue("PAGE_NO", "" + pageNo, 0);
    wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);

    sheet = wb.getSheetAt(sheetNo);
    spanControl();
    changePage = false;
    fileEnd = false;

    Iterator rowIterator = sheet.rowIterator();
    int liProcRow = 0;
    while (rowIterator.hasNext()) {
      liProcRow++;

      row = (XSSFRow) rowIterator.next();
      skipRow = false;
      rowNum = row.getRowNum();
      if (rowNum < browsePoint || rowNum > sumEndRow) {
        resetParameter();
      }
      columnCount = row.getLastCellNum();
      if (columnCount >= maximumColumn && maximumColumn > 0) {
        columnCount = maximumColumn;
      }

      if (columnCount < 0) {
        nullTableRow();
        continue;
      }

      checkRowspan++;
      if (rowSpanCount < checkRowspan) {
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

      int line = controlLine;
      rowHeight[line][rowNum] = row.getHeightInPoints();

      for (int k = 0; k < columnCount; k++) {

        cell = null;
        cell = row.getCell(k);
        colWidth[k] = sheet.getColumnWidth(k);
        if (cell == null) {
          cellValue[line][k] = "";
          if (rowNum != sumEndRow) {
            setCellValue(0, 0, k);
          }
          continue;
        }

        cellValue[line][k] = "";
        if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
          cellValue[line][k] = cell.getStringCellValue();
        } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
          cellValue[line][k] = "" + cell.getNumericCellValue();
        }
        if (cellValue[line][k] == null) {
          cellValue[line][k] = "";
        }
        String strVal = cellValue[line][k].trim();
        if (k == 0 && strVal.equals("###")) {
          skipRow = true;
          break;
        }
        if (k == 0 && (strVal.equalsIgnoreCase("#EEE") || strVal.equalsIgnoreCase("END"))) {
          fileEnd = true;
          break;
        }

        skipRow = false;
        cellValue[line][k] = cellValue[line][k].trim();
        fontName[line][k] = cell.getCellStyle().getFont().getFontName();
        fontSize[line][k] = cell.getCellStyle().getFont().getFontHeightInPoints();
        fontBold[line][k] = Font.NORMAL;
        fontAlign[line][k] = cell.getCellStyle().getAlignment();
        topBorder[line][k] = cell.getCellStyle().getBorderTop();
        leftBorder[line][k] = cell.getCellStyle().getBorderLeft();
        rightBorder[line][k] = cell.getCellStyle().getBorderRight();
        bottomBorder[line][k] = cell.getCellStyle().getBorderBottom();
        // bgColor[j][k] =
        // cell.getCellStyle().getFillForegroundColorColor().getRGB();

        if (cell.getCellStyle().getFont().getBold()) {
          fontBold[line][k] = Font.BOLD;
        }

        if (rowNum < browsePoint || rowNum > sumEndRow || !browseMode) {
          setCellValue(listPnt, 0, k);
        }
      } // end of column for loop

      if (skipRow) {
        continue;
      } else if (fileEnd) {
        break;
      }

      if (rowNum >= browsePoint && rowNum <= browseRow && browseMode) {
        multiDataRow++;
        browseColumn = columnCount;
      }
      if (rowNum >= browsePoint && rowNum <= sumEndRow && browseMode) {
        controlLine++;
      }

      if ((rowNum < browsePoint || rowNum > sumEndRow || !browseMode)
          && checkRowspan >= rowSpanCount) {
        setColumnWidths();
        // wp.ddd("--->docu.add, procExcelSheet");
        document.add(pTable);
        pTable = null;
      }

      if ((rowNum == sumEndRow) && browseMode) {
        listBrowseData();
      }

      if (changePage && footEndRow == 0) {
        break;
      }

      if (rowNum == maximumRow && !browseMode) // 沒有瀏灠模式
      {
        listPnt++; // 讀下一筆
        lineCount++;
        if ((lineCount >= pageCount)) {
          changePage = true;
          break;
        }
      }

    } // end of while

    if (listPnt >= wp.listCount[wpIndx]) {
      listEnded = "Y";
    }

    inExcelFile.close();
    inExcelFile = null;

  } // end od processExcelSheet

  // RESET 處理變數
  private void resetParameter() throws Exception {

    for (int i = 0; i < (cm + bL); i++) {
      for (int j = 0; j < rowLimit; j++) {
        rowHeight[i][j] = 0;
      }
    }

    for (int i = 0; i < (cm + bL); i++) {
      for (int j = 0; j < colLimit; j++) {
        topBorder[i][j] = 0;
        leftBorder[i][j] = 0;
        rightBorder[i][j] = 0;
        bottomBorder[i][j] = 0;
        fontName[i][j] = "";
        fontSize[i][j] = 0;
        fontAlign[i][j] = 0;
        fontBold[i][j] = 0;
      }
    }
  }

  // TABLE 跨欄 跨列處理 ROW-SPAN ,COLUMN-SPAN
  void spanControl() throws Exception {

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
      }

      // 紀錄 ROW-SPAN 欄位
      int row = spanRowLast - spanRowFirst;
      if (row > 0) {
        rowHash.put((spanRowFirst + "#" + spanColFirst), (row + 1));
      }

      // 紀錄 ROW-SPAN ,COLUMN-SPAN 產生之重覆欄位
      if (col > 0 || row > 0) {
        for (int j = spanRowFirst; j <= spanRowLast; j++) {
          for (int k = spanColFirst; k <= spanColLast; k++) {
            if (j == spanRowFirst && k == spanColFirst) {
              continue;
            }
            skipHash.put((j + "#" + k), "Y");
          }
        }
      }
    } // end of for loop

    return;
  } // end of spanControl

  // 處理 TABLE CELL 資料
  void setCellValue(int pnt, int i, int j) throws Exception {

    String fieldName = "", cvtData = "", cellData = "";
    int rowCntl = rowNum + i;

    // 跳過 ROW SPAN,COLUMN SPAN 產生之重覆欄位
    String checkSkip = (String) skipHash.get((rowCntl + "#" + j));
    if (checkSkip != null) {
      return;
    }

    if (cellValue[i][j] == null) {
      cellValue[i][j] = "";
    }
    cellData = cellValue[i][j];

    int start = cellData.indexOf("{");
    int end = cellData.indexOf("}");

    if (start != -1 && end != -1) {
      fieldName = cellData.substring(start + 1, end).trim();
      imageField = false;
      // 處理 IMAGE 顯示
      if (fieldName.length() > 5 && fieldName.substring(0, 5).equals("#IMG:")) {
        fieldName = fieldName.substring(5);
        imageField = true;
      } else if (fieldName.length() > 7 && fieldName.substring(0, 7).equals("#BAR39:")) {
        fieldName = fieldName.substring(7);
        barCode39 = true;
      }

      if (checkBreakField(fieldName) == true && subData == true && listPnt > 0) {
        pnt = listPnt - 1;
      } else if (checkBreakField(fieldName) == true) {
        pnt = listPnt;
      }
      // subData = false;

      if (Arrays.asList("SYS_DATE", "SYS_TIME", "PAGE_NO", "ECS_BANK_NAME").contains(
          fieldName.toUpperCase())) {
        cvtData = cvtFunction(fieldName, 0);
      } else if (checkFixHeader(fieldName.toUpperCase(Locale.TAIWAN))) {
        cvtData = cvtFunction(fieldName, 0);
      } else {
        cvtData = cvtFunction(fieldName, pnt);
      }

      cellData = cellData.substring(0, start) + cvtData + cellData.substring(end + 1);
    }

    BaseFont bFont = nFont;
    if (fontName[i][j].equals("標楷體")) {
      bFont = sFont;
    }

    if (j == 0 || cellData.length() == 0) {
      cellData = "";
    }

    Font fon = new Font(bFont, fontSize[i][j], fontBold[i][j]);

    PdfPCell pCell = null;
    String dataRoot = TarokoParm.getInstance().getDataRoot();
	if (imageField) {
      imageField = false;
      String imgPath = dataRoot  + "/ExcelTemplate/" + fieldName;
      Image img = Image.getInstance(imgPath);
      pCell = new PdfPCell(new PdfPCell(img, true));
    } else if (barCode39) {
      barCode39 = false;
		BaseFont barfont = BaseFont.createFont(dataRoot + "/font/V100016_39code.TTF",
				BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
      Font fon2 = new Font(barfont, fontSize[i][j], fontBold[i][j]);
      pCell = new PdfPCell(new Paragraph(cellData, fon2));
    } else {
      pCell = new PdfPCell(new Paragraph(cellData, fon));
    }
    // PdfPCell pCell = new PdfPCell(new Paragraph(cellData,fon));
    pCell.setNoWrap(true);

    pCell.setVerticalAlignment(Element.ALIGN_CENTER);
    if (fontAlign[i][j] == 2) {
      pCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    } else if (fontAlign[i][j] == 3) {
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
    Integer checkRow = (Integer) rowHash.get((rowCntl + "#" + j));
    if (checkRow != null) {
      pCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
      pCell.setRowspan(checkRow);
      rowSpanCount = checkRow;
    }

    // 處理 COLUMN SPAN
    Integer checkCol = (Integer) colHash.get((rowCntl + "#" + j));
    if (checkCol != null) {
      pCell.setColspan(checkCol);
    }

    pCell.setBorderWidthTop(topBorder[i][j]);
    pCell.setBorderWidthRight(rightBorder[i][j]);
    pCell.setBorderWidthBottom(bottomBorder[i][j]);
    pCell.setBorderWidthLeft(leftBorder[i][j]);
    pCell.setFixedHeight(rowHeight[i][rowCntl]);

    pTable.addCell(pCell);
    pCell = null;
    return;
  }

  // 處理 表頭固定值
  public boolean checkFixHeader(String parmField) throws Exception {

    for (int m = 0; m < fixHeader.length; m++) {
      if (parmField.equals(fixHeader[m].toUpperCase(Locale.TAIWAN))) {
        return true;
      }
      if (fixHeader[m].length() == 0) {
        break;
      }
    }
    return false;
  }

  // 處理 輸出資料顯示轉換
  String cvtFunction(String cvtField, int num) throws Exception {

    cvtField = wp.convertField(cvtField);
    if (wp.descField.equals("JAVA")) {
      wp.orgField = cvtField;
      wp.userTagRr = num;
    }
    String cvtData = "";
    if (noTrim) {
      return wp.getValue2(cvtField, num);
    } else {
      cvtData = wp.getValue(cvtField, num);
    }
    return wp.convertFormat(cvtField, cvtData);
  }

  // 輸出瀏灠區資料
  void listBrowseData() throws Exception {

    pTable = new PdfPTable(browseColumn);
    widths = new float[browseColumn];
    for (int i = 0; i < widths.length; i++) {
      widths[i] = 0;
    }

    rowNum = browsePoint;
    lineCount = 0;
    while (true) {
      this.subData = false;

      // 瀏灠資料結束
      if (listPnt >= wp.listCount[wpIndx]) {
        listEnded = "Y";
        for (int i = 0; i < sumLine; i++) {
          processSubData(i);
        } // 輸出小計及總計資料
        break;
      }

      // 查核換頁, 查核是否輸出換頁小計
      if ((lineCount >= pageCount)) {
        changePage = true;
        break;
      }

      // 處理小計作業
      checkDataBreak();
      if (changePage) {
        break;
      }

      if (sumLine > 0) {
        calculateSubTotal();
      }

      // 輸出瀏灠資料
      for (int n = 0; n < multiDataRow; n++) {
        for (int m = 0; m < browseColumn; m++) {
          setCellValue(listPnt, n, m);
        }
        lineCount++;
      }

      listPnt++; // 讀下一筆

    } // end of while

    setColumnWidths();
    // wp.ddd("--->docu.newPage; listBrowseData");
    document.add(pTable);

    // 輸出頁尾空白列
    if (this.footEndRow > 0) {
      for (int i = 0; i < pageCount - lineCount; i++) {
        nullTableRow();
      }
    }

    multiDataRow = 0;
    controlLine = 0;
    rowSpanCount = 0;
    checkRowspan = 0;
  }

  // 處理小計作業
  void checkDataBreak() throws Exception {

    // 查核是否輸出小計
    int breakCnt = 0;
    for (int i = 0; i < sumLine; i++) {
      if (breakField[i].length() == 0) {
        continue;
      }

      prevData[i] = compData[i]; // 儲存 BREAK 前一筆資料
      // if ( !wp.getValue(breakField[i],listPnt).equals(compData[i]) &&
      // firstFlag.equals("N") )
      if (!wp.getValue(breakField[i], listPnt).equals(compData[i]) && listPnt > 0) {
        breakCnt = i + 1;
        if (breakField[i].equals(pageBreakField)) {
          changePage = true;
//          pagePrevData = compData[i]; // Justin
        }
      }
      // firstFlag="N";
      compData[i] = wp.getValue(breakField[i], listPnt);
    }

    for (int i = 0; i < breakCnt; i++) {
      processSubData(i);
    } // 輸出小計資料

    return;
  }

  // 輸出小計資料
  void processSubData(int i) throws Exception {

    lineCount++;
    subData = true;

    for (int j = 0; j < browseColumn; j++) {

      int pnt2 = sumField[i][j].indexOf(".");
      if (pnt2 != -1) {
        wp.setValue(sumField[i][j].substring(0, pnt2), "" + "" + subSumValue[i][j], i);
      } else {
        wp.setValue(sumField[i][j], "" + subSumValue[i][j], i);
      }
      wp.setValue("SUM-ROWCOUNT", "" + (int) subSumCount[i][j], i);
      setCellValue(i, multiDataRow + i, j);
      subSumValue[i][j] = 0;
      subSumCount[i][j] = 0;
      if (headField[i].equals("SUM-G")) {
        wp.setValue((j + "-N#" + breakField[i]), "0", 0);
        wp.setValue((j + "-G#" + breakField[i]), "0", 0);
      }
    }
    subData = false;
  }

  // 小計欄位加總處理
  void calculateSubTotal() throws Exception {

    // fieldHash.clear();
    for (int i = 0; i < sumLine; i++) {
      fieldHash.clear();
      for (int j = 0; j < columnCount; j++) {
        if (sumField[i][j].length() <= 4) {
          continue;
        }

        String cvtTemp = "";
        int pnt2 = sumField[i][j].indexOf(".");
        if (pnt2 != -1) {
          cvtTemp = sumField[i][j].substring(0, pnt2).substring(4);
        } else {
          cvtTemp = sumField[i][j].substring(4);
        }

        if (headField[i].equals("SUM-G")) { // (合計)
          subSumValue[i][j] = wp.getNumber((j + "-G#" + breakField[i]), 0);
          subSumCount[i][j] = wp.getNumber((j + "-N#" + breakField[i]), 0);
          // 2018-1016 if (sumLine != 1) {
          // continue;
          // }
        } else if (headField[i].equals("SUM-T")) { // (總計)
          subSumValue[i][j] = totalValue[i][j];
          subSumCount[i][j] = totalCount[i][j];
          // if ( sumLine != 1 )
          // { continue; }
        }
        subSumValue[i][j] += wp.getNumber(cvtTemp, listPnt);
        subSumCount[i][j]++;

        // 計算 (合計) 數量及筆數
        double groupValue =
            wp.getNumber((j + "-G#" + breakField[i]), 0) + wp.getNumber(cvtTemp, listPnt);
        wp.setValue(j + "-G#" + breakField[i], "" + groupValue, 0);
        double groupCount = wp.getNumber((j + "-N#" + breakField[i]), 0) + 1;
        wp.setValue(j + "-N#" + breakField[i], "" + groupCount, 0);

        // 計算 (總計) 數量及筆數
        if ((String) fieldHash.get(cvtTemp) == null) // 查核是重覆之欄位避免重覆計算
        {
          totalValue[i][j] += wp.getNumber(cvtTemp, listPnt);
          totalCount[i][j]++;
          // wp.showLogMessage("D","TTT ",""+i+" "+j+" "+cvtTemp+"
          // "+subSumValue[i][j]+" "+totalCount[i][j]);
        }
        fieldHash.put(cvtTemp, "Y");

      } // end for loop j
    } // end for loop i
  }

  boolean checkBreakField(String checkField) throws Exception {

    for (int i = 0; i < sumLine; i++) {
      if (checkField.equals(breakField[i]) && checkField.length() > 0) {
        return true;
      }
    }
    return false;
  }

  // 處理欄位寬度比例
  void setColumnWidths() throws Exception {

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

  public void setListIndex(int idx) throws Exception {
    wpIndx = idx - 1;
    return;
  }

  void nullTableRow() throws Exception {
    pTable = new PdfPTable(1);
    setCellValue(0, 0, 0);
    pTable.setWidthPercentage(fullWidth);
    // wp.ddd("--->docu.newPage; nullTableRow");
    document.add(pTable);
    rowSpanCount = 0;
    checkRowspan = 0;
    pTable = null;

    return;
  }

  // 處理浮水印
  void pdfWaterMark() throws Exception {

    TarokoPDFWaterMark water = new TarokoPDFWaterMark();
    water.waterSize = 48;
    water.pngSource = "/waterMark/PdfWaterMark.png";
    water.pdfWater = "_PDF_";
    water.createWaterMark(wp);
    Image logo = Image.getInstance(TarokoParm.getInstance().getWorkDir() + wp.loginUser + "_PDF_.png");
    img = Image.getInstance(logo);
    if (pageVert) {
      img.setAbsolutePosition(60, 300);
    } else {
      img.setAbsolutePosition(170, 30);
    }
    addWaterMark = writer.getDirectContentUnder();

  } // end of pdfWaterMark

} // end of class
