/**
 *  PDF報表公用程式(no trim value): for Batch-report V.2019-0115
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  19-0115     JH    modify:優化
*  18-1225:    JH    report_id
* 18-0718:	   JH		initial
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-24  V1.00.01  Zuwei       coding standard      *
* 109-09-30   V1.00.02 JustinWu    change the way to get URL
* 109-12-28   V1.00.03 JustinWu     zz->comm
* 110-01-08  V1.00.02   shiyuqi       修改无意义命名
* 110-09-22  V1.00.03   Justin   use old version itext                       *
* 111-01-21  V1.00.04   Justin       fix Redundant Null Check
******************************************************************************/
package taroko.com;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoPDFBatch {
  taroko.base.CommString commString = new taroko.base.CommString();

  public int pageCount = 19; // 每頁資料筆數
  public float fullWidth = 95; // 頁面寬度百分比
  public boolean pageVert = false; // 橫印或直印
  public boolean showWaterMark = false; // 是否列印 浮水印
  public String excelTemplate = ""; // EXCEL 樣版檔
  public int sheetNo = 0, maxColumn = 0;
  public int wpIndx = 0;
  private String reportID = "";

  XSSFWorkbook wb = null;
  XSSFSheet sheet = null;
  XSSFRow row = null;
  XSSFCell cell = null;

  private String listEnded = "", pagePrevData = "";
  private int rowSpanCount = 0, checkRowspan = 0, browsePoint = 0, browseRow = 0, multiDataRow = 0;
  private int rowNum = 0, listPnt = 0, pageNo = 0, controlLine = 0, lineCount = 0;
  private int columnCount = 0, maxRow = 0; // browseColumn = 0, titlePoint = 0,

  InputStream inExcelFile = null;
  XSSFCellStyle cellStyle = null;
  int colLimit = 100; // COLUMN 限制
  int rowLimit = 100; // ROW 限制
  int cm = 100; // 一筆多列 限制
  int bL = 10; // 小計 限制
  XSSFRow[] listRow = new XSSFRow[rowLimit];
  int iiListRow = 0;

  int[] colWidth = new int[colLimit];
  float[] widths = null;

  private String pageBreakField = "report_content";
  private boolean changePage = false, skipRow = false, fileEnd = false, titleFlag = false,
      browseMode = false, multiBrowse = false;

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
  float imSizeDoc = 0, imSize = 0, imSizeRow = 0, imListhh = 0;

  public void setReportId(String s1) {
    reportID = commString.nvl(s1);
  }

  public void setPageBreakField(String col) {
    pageBreakField = col;
  }

  // 產生 PDF 報表
  public void procesPDFreport(TarokoCommon wp) throws Exception {
    this.wp = wp;
    if (commString.empty(excelTemplate)) {
      wp.errCode = "Y";
      wp.errMesg = "未指定 報表樣板(excelTemplate)";
      return;
    }

    FileOutputStream pdfFile = null;
    try {
      if (pageVert) {
        // document = new Document(PageSize.A4, 10, 10, 10, 10);
        document = new Document(PageSize.A4);
      } else {
        document = new Document(PageSize.A4.rotate()); // , 2, 10, 10, 10);
      }
      // L,R.Top,Bttm
      document.setMargins(-10, 10, 10, 10);

      // -output-filename-
      String fileName = reportID;
      if (commString.empty(fileName)) {
        fileName = excelTemplate;
        int liPos = this.excelTemplate.indexOf(".");
        if (liPos > 0) {
          fileName = excelTemplate.substring(0, liPos);
        }
      }
      // wp.exportFile = file_name + "-u" + wp.loginUser
      // + "-" + wp.sysDate.substring(6) + wp.sysTime + ".pdf";
      wp.exportFile =
          wp.sysDate.substring(6) + "u" + wp.loginUser + "_" + fileName + "-" + wp.sysTime
              + ".pdf";

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
    } catch (Exception ex) {
      wp.expMethod = "procesPDFreport";
      wp.expHandle(ex);
      return;
    } finally {
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
  public void getExcelRowCount() throws Exception {

    inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot()  + "/ExcelTemplate/" + excelTemplate);
    wb = new XSSFWorkbook(inExcelFile);
    sheet = wb.getSheetAt(sheetNo);
    spanControl();
    browseRow = 9999;

    int liListCnt = 0;
    Iterator rowIterator = sheet.rowIterator();
    while (rowIterator.hasNext()) {

      row = (XSSFRow) rowIterator.next();
      for (int j = 0; j < row.getLastCellNum(); j++) {
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
        }
        cellData = cellData.trim();

        if (maxColumn <= 0 && cellData.toUpperCase(Locale.TAIWAN).equals("#EEE")) {
          maxColumn = j;
        }
        if (j > 0)
          continue;

        if (j == 0 && cellData.toUpperCase(Locale.TAIWAN).equals("TITLE")) {
          // titlePoint = row.getRowNum();
          titleFlag = true;
        }

        if (j == 0 && commString.strIn2(cellData, ",LLLL,LIST")) {
          listRow[iiListRow] = row;
          iiListRow++;
          continue;
        }

        if (j == 0 && commString.strIn2(cellData, ",END,#EEE")) {
          maxRow = row.getRowNum() - 1;
          break;
        }

      } // end of for loop

      if (maxRow > 0)
        break;
    } // end of while loop
    //
    // if (sumEndRow == 0) {
    // sumEndRow = browseRow;
    // }

    inExcelFile.close();
    inExcelFile = null;
  }

  void printReport() throws Exception {

    // -print list data-
    for (int ll = 0; ll < wp.listCount[wpIndx]; ll++) {
      printListdata(ll);
    }
  }

  void printListdata(int llRow) throws Exception {
    if (wp.getValue(pageBreakField, llRow).toUpperCase(Locale.TAIWAN).indexOf("##PPP") == 0) {
      document.newPage();
      return;
    }

    for (int ii = 0; ii < iiListRow; ii++) {
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
        // pTable.setSpacingBefore(0);
        document.add(pTable);
        pTable = null;
      }
    }
  }

  void rowInitial() throws Exception {
    rowNum = row.getRowNum();

    columnCount = row.getLastCellNum();
    if (columnCount >= maxColumn && maxColumn > 0) {
      columnCount = maxColumn;
    }

    if (columnCount < 0) {
      nullTableRow(0);
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
    // -row.size=16.15-
    imSizeRow += row.getHeightInPoints();
  }

  // // RESET 處理變數
  // public void resetParameter() throws Exception {
  //
  // for (int i = 0; i < (cm + bL); i++) {
  // for (int j = 0; j < rowLimit; j++) {
  // rowHeight[i][j] = 0;
  // }
  // }
  //
  // for (int i = 0; i < (cm + bL); i++) {
  // for (int j = 0; j < colLimit; j++) {
  // topBorder[i][j] = 0;
  // leftBorder[i][j] = 0;
  // rightBorder[i][j] = 0;
  // bottomBorder[i][j] = 0;
  // fontName[i][j] = "";
  // fontSize[i][j] = 0;
  // fontAlign[i][j] = 0;
  // fontBold[i][j] = 0;
  // }
  // }
  // }

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

  String getCellData(XSSFCell cell) {

    if (cell == null)
      return "";

    String lsCellData = "";

    if (cell.getCellTypeEnum() == CellType.STRING) {
      lsCellData = cell.getStringCellValue();
    } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
      lsCellData = "" + cell.getNumericCellValue();
    }

    if (lsCellData == null) {
      lsCellData = "";
    }

    return lsCellData;
  }

  // 處理 TABLE CELL 資料
  void setCellValue(int llData, int liCc, String str) throws Exception {
    String fieldName = "", cvtData = "", cellData = "";
    int rowCntl = row.getRowNum();
    int cnt1 = liCc;

    // 跳過 ROW SPAN,COLUMN SPAN 產生之重覆欄位
    String checkSkip = (String) skipHash.get((rowCntl + "#" + cnt1));
    if (checkSkip != null) {
      return;
    }

    PdfPCell pCell = null;
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

    cellData = str;
    // imageField = false;
    // barCode39 = false;

    if (commString.empty(cellData)) {
      pCell = new PdfPCell(new Paragraph(cellData, fon));
    } else {
      int start = cellData.indexOf("{");
      int end = cellData.indexOf("}");
      if (start >= 0 && end > 0) {
        fieldName = cellData.substring(start + 1, end).trim();
        cvtData = wp.getValue2(fieldName, llData); // cvtFunction(fieldName,pnt);
        cellData = cellData.substring(0, start) + cvtData + cellData.substring(end + 1);
      }
      // -12-
      fon.setSize(xfont.getFontHeightInPoints());
      pCell = new PdfPCell(new Paragraph(cellData, fon));
    }
    pCell.setNoWrap(true);

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
    // -Title最後一行底色變灰色---
    // if (rowCntl == titlePoint && titleFlag) {
    // pCell.setBackgroundColor(new BaseColor(244, 244, 244));
    // }

    // 處理 ROW SPAN
    Integer checkRow = (Integer) rowHash.get((rowCntl + "#" + cnt1));
    if (checkRow != null) {
      pCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
      pCell.setRowspan(checkRow);
      rowSpanCount = checkRow;
    }

    // 處理 COLUMN SPAN
    Integer checkCol = (Integer) colHash.get((rowCntl + "#" + cnt1));
    if (checkCol != null) {
      pCell.setColspan(checkCol);
    }

    pCell.setBorderWidthTop(cellStyle.getBorderTopEnum().getCode());
    pCell.setBorderWidthRight(cellStyle.getBorderRightEnum().getCode());
    pCell.setBorderWidthBottom(cellStyle.getBorderBottomEnum().getCode());
    pCell.setBorderWidthLeft(cellStyle.getBorderLeftEnum().getCode());
    pCell.setFixedHeight(16f); // row.getHeightInPoints());

    pTable.addCell(pCell);
    pCell = null;
    return;
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
    pTable.setWidths(widths);
    pTable.setWidthPercentage(fullWidth);

  }

  void nullTableRow(float hh) throws Exception {
    float lfHh = row.getHeightInPoints();
    // if (hh >0) {
    // row.setHeightInPoints(hh);
    // im_size +=hh;
    // }
    // else {
    // row.setHeightInPoints(im_list_hh);
    // im_size +=im_list_hh;
    // }
    cellStyle = wb.createCellStyle();
    pTable = new PdfPTable(1);
    setCellValue(0, 0, "");
    pTable.setWidthPercentage(fullWidth);

    document.add(pTable);
    rowSpanCount = 0;
    checkRowspan = 0;
    pTable = null;
    // im_size_row =0;
    row.setHeightInPoints(lfHh);
    return;
  }

  // 處理 輸出資料顯示轉換
  String cvtFunction(String cvtField, int i) throws Exception {
    cvtField = wp.convertField(cvtField);
    if (wp.descField.equals("JAVA")) {
      wp.orgField = cvtField;
      wp.userTagRr = i;
    }
    String cvtData = wp.getValue2(cvtField, i);
    return wp.convertFormat(cvtField, cvtData);
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
