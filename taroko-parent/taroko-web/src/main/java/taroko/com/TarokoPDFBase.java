/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-07-17  V1.00.01  Zuwei       兆豐國際商業銀行 => 合作金庫商業銀行      *
*  109-07-24  V1.00.01  Zuwei       coding standard      *
*  109-12-28   V1.00.02 Justin        zz -> comm
*  110-01-08  V1.00.02   shiyuqi       修改无意义命名
*  110-09-22  V1.00.03  Justin   use old version itext
* 111-01-21  V1.00.04  Justin       fix Redundant Null Check
******************************************************************************/
package taroko.com;
/**
 * 2019-0726      JH    setListIndex()
 *  2019-0508:  JH    ++ sec_window.report_header, report_footer
   2019-0419:  JH    #hi-data:
* */
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public abstract class TarokoPDFBase {
  taroko.base.CommString commString = new taroko.base.CommString();

  public int pageCount = 39; // 35; // 每頁資料筆數[橫印35(39),直印52(57)],不含表頭
  public float fullWidth = 95; // 頁面寬度百分比
  public boolean pageVert = false; // 橫印或直印
  public boolean showWaterMark = false; // 是否列印 浮水印
  public String excelTemplate = ""; // EXCEL 樣版檔
  public int sheetNo = 0;
  public int wpIndx = 0;
  int pageNo = 0;
  private float margeLL = 0, margeRR = 0, margeTT = 10, margeBB = 10;

  XSSFWorkbook wb = null;
  XSSFSheet sheet = null;
  XSSFRow row = null;
  XSSFCell cell = null;

  // private String listEnded = "", pagePrevData = "", firstFlag = "";
  int rowSpanCount = 0, checkRowspan = 0; // , browsePoint = 0, browseRow = 0, multiDataRow = 0;
      // private int sumEndRow = 0, sumLine = 0, footEndRow = 0;
  private int rowNum = 0; // , listPnt = 0, controlLine = 0, lineCount = 0;
  int columnCount = 0, maxRow = 0, maxColumn = 0; // , browseColumn = 0, titlePoint = 0

  InputStream inExcelFile = null;
  XSSFCellStyle cellStyle = null;
  int colLimit = 100; // COLUMN 限制
  int rowLimit = 100; // ROW 限制
  int cm = 100; // 一筆多列 限制
  int bL = 50; // 30; // 小計 限制
  XSSFRow[] listRow = new XSSFRow[rowLimit];
  XSSFRow[] summRow = new XSSFRow[rowLimit];
  XSSFRow[] footRow = new XSSFRow[rowLimit];
  XSSFRow secHeadRow = null;
  XSSFRow secFootRow = null;
  int secHeader = 0, secFooter = 0;

  int iisummRow = 0, iifootRow = 0;
  String[] summkk = new String[50];

  // String[][] cellValue = new String[cm + bL][colLimit];
  // String[][] fontName = new String[cm + bL][colLimit];
  // float[][] topBorder = new float[cm + bL][colLimit];
  // float[][] leftBorder = new float[cm + bL][colLimit];
  // float[][] rightBorder = new float[cm + bL][colLimit];
  // float[][] bottomBorder = new float[cm + bL][colLimit];
  // short[][] fontSize = new short[cm + bL][colLimit];
  // short[][] fontAlign = new short[cm + bL][colLimit];
  // short[][] fontBold = new short[cm + bL][colLimit];
  // byte[][][] bgColor = new byte[cm+bL][colLimit][3];

  int[] colWidth = new int[colLimit];
  float[] widths = null;

  // String[] headField = new String[bL]; // 儲存 控制欄位名稱
  // String[] breakField = new String[bL]; // 儲存 BREAK 小計欄位名稱
  // String[] prevData = new String[bL]; // 儲存 BREAK 前一筆資料
  // String[] compData = new String[bL]; // 儲存 BREAK 小計比較值
  String[][] sumField = null; // new String[bL][colLimit]; // 儲存加總欄位名稱
  double[][] sumValue = null; // new double[bL][colLimit]; // 儲存小計之加總值
  double[][] subSumCount = new double[bL][colLimit]; // 儲存小計之加總筆數

  double[][] totalValue = new double[bL][colLimit];
  double[][] totalCount = new double[bL][colLimit];

  // private String pageBreakField = "";
  // private boolean changePage = false, skipRow = false, fileEnd = false, titleFlag = false;
  // private boolean browseMode = false, multiBrowse = false;
  boolean imageField = false, barCode39 = false; // , subData = false

  HashMap<String, Integer> rowHash = new HashMap<String, Integer>();
  HashMap<String, Integer> colHash = new HashMap<String, Integer>();
  HashMap<String, String> skipHash = new HashMap<String, String>();

  // HashMap<String, String> fieldHash = new HashMap<String, String>();
  Document document = null;
  BaseFont sFont = null, nFont = null;
  PdfPTable pTable = null;
  PdfWriter writer = null;
  PdfContentByte addWaterMark = null;
  Image img = null;
  TarokoCommon wp = null;

  boolean summ = false, footer = false;
  boolean summField = false;
  double imSummValue = 0;
  int procCnt = 0;
  float imSizeDoc = 0, imSize = 0, imSizeRow = 0, imListhh = 0;
  boolean linePrint = true; // -列印模式: true.line-cnt, false.size-cnt-

  // 產生 PDF 報表
  public abstract void procesPDFreport(TarokoCommon wr) throws Exception;

  abstract void printReport() throws Exception;

  abstract void printListdata(int llRow) throws Exception;

  abstract void printSummary(int llRow) throws Exception;

  abstract void printTotal() throws Exception;

  abstract void printFooter() throws Exception;

  public void setListIndex(int idx) throws Exception {
    wpIndx = idx - 1;
    return;
  }

  public void setDocuMarge(float margeLL1, float margeRR1, float margeTT1, float margeBB1) {
    margeLL = margeLL1;
    margeRR = margeRR1;
    margeTT = margeTT1;
    margeBB = margeBB1;
  }

  void procPrepare() throws Exception {
    if (pageVert) {
      document = new Document(PageSize.A4); // , 10, 10, 10, 10);
    } else {
      document = new Document(PageSize.A4.rotate()); // , 10, 10, 10, 10);
    }
    document.setMargins(margeLL, margeRR, margeTT, margeBB);
    imSizeDoc = document.getPageSize().getHeight();

    // -output-filename-
    int liPos = this.excelTemplate.indexOf(".");
    String fileName = excelTemplate;
    if (liPos > 0) {
      fileName = excelTemplate.substring(0, liPos);
    }
    wp.exportFile =
        wp.sysDate.substring(6) + "u" + wp.loginUser + "_" + fileName + "-" + wp.sysTime + ".pdf";

    String dataRoot = TarokoParm.getInstance().getDataRoot();
	sFont = BaseFont.createFont(dataRoot + "/font/kaiu.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
	nFont = BaseFont.createFont(dataRoot + "/font/SIMYOU.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
  }

  // 取得 EXCEL TEMPLATE 總 ROW COUNT
  void getExcelRowCount() throws Exception {
    inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot()  + "/ExcelTemplate/" + excelTemplate);
    wb = new XSSFWorkbook(inExcelFile);
    sheet = wb.getSheetAt(sheetNo);

    spanControl();

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

        // -Title最後一行底色變灰色-
        // if (j == 0 && commString.eq_igno(cellData,"TITLE")) {
        // titlePoint = row.getRowNum();
        // titleFlag = true;
        // continue;
        // }
        if (j == 0 && commString.strIn2(cellData, ",LLLL,LIST")) {
          listRow[liListCnt] = row;
          liListCnt++;
          imListhh += row.getHeightInPoints();
          continue;
        }
        // 紀錄頁尾列--
        if (j == 0 && commString.eqIgno(cellData, "FOOTER")) {
          footer = true;
          footRow[iifootRow] = row;
          iifootRow++;
          imSizeDoc = imSizeDoc - (row.getHeightInPoints() + 2);
          continue;
        }
        // -公用表頭,表尾-
        if (j == 0 && commString.eqIgno(cellData, "#header") && secHeader > 0) {
          secHeadRow = row;
          continue;
        }
        if (j == 0 && commString.eqIgno(cellData, "#footer") && secFooter > 0) {
          footer = true;
          footRow[iifootRow] = row;
          iifootRow++;
          imSizeDoc = imSizeDoc - (row.getHeightInPoints() + 2);
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
            summ = true;
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

    if (summ) {
      summColumn();
    }
    if (liListCnt > 0) {
      imListhh = imListhh / liListCnt;
    }
    imSizeDoc = imSizeDoc - (imListhh * 2);
    // --
    if (linePrint && pageCount > 0) {
      pageCount = pageCount - iifootRow;
    }
    // -FOOTER保留行-
    if (iifootRow > 0)
      pageCount--;

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

  // void print_Header() throws Exception {
  // String ls_cell_data = "";
  //
  // pageNo++;
  // if (pageNo<=1) {
  // wp.col_set("sys_dtime", wp.dispDate.substring(2) + " " + wp.dispTime);
  // wp.setValue("SYS_DATE", wp.dispDate, 0);
  // wp.setValue("SYS_TIME", wp.dispTime, 0);
  // wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);
  // }
  // wp.setValue("PAGE_NO", "" + pageNo, 0);
  //
  // if (pageNo >1) {
  // print_Footer();
  // }
  //
  // document.newPage();
  // ii_proc_cnt =0;
  //
  // im_size =0;
  // im_size_row =0;
  // Iterator rowIterator = sheet.rowIterator();
  // while (rowIterator.hasNext()) {
  // row = (XSSFRow) rowIterator.next();
  // cell = row.getCell(0);
  // ls_cell_data = get_CellData(cell);
  // if (commString.ssIN(ls_cell_data,",EEE,#END"))
  // break;
  // //-無效row-
  // if (commString.eq_igno(ls_cell_data,"###"))
  // continue;
  //
  // if(!commString.empty(ls_cell_data) && !commString.eq_igno(ls_cell_data,"TITLE")
  // && !commString.eq_igno(ls_cell_data,"#header") )
  // break;
  //
  // //-SEC-Header-
  // if (commString.eq_igno(ls_cell_data,"#header")) {
  // print_Sec_header();
  // continue;
  // }
  //
  // row_Initial();
  // for (int kk =0; kk < columnCount; kk++) {
  // cell = null;
  // cellStyle =null;
  // cell = row.getCell(kk);
  // colWidth[kk] = sheet.getColumnWidth(kk);
  // if (cell == null) {
  // cellStyle =wb.createCellStyle();
  // setCellValue(0,kk,"");
  // continue;
  // }
  //
  // String ss =commString.nvl(get_CellData(cell));
  //
  // cellStyle =cell.getCellStyle();
  // setCellValue(0,kk,ss);
  // } //-- end for loop
  //
  // if (checkRowspan >= rowSpanCount) {
  // setColumnWidths();
  // document.add(pTable);
  // pTable = null;
  // im_size +=im_size_row;
  // im_size_row =0;
  // }
  // }
  // }

  // void print_Sec_header(String s1) throws Exception {
  // if (ii_sec_header <=0)
  // return;
  // if (commString.empty(s1))
  // return;
  //
  // for (int ll=0; ll<ii_sec_header; ll++) {
  // row_Initial();
  // for (int kk =0; kk < columnCount; kk++) {
  // cell = null;
  // cellStyle =null;
  // cell = row.getCell(kk);
  // colWidth[kk] = sheet.getColumnWidth(kk);
  // if (cell == null) {
  // cellStyle =wb.createCellStyle();
  // setCellValue(0,kk,"");
  // continue;
  // }
  //
  // // String ss =""; //commString.nvl(get_CellData(cell));
  // // if (commString.eq_igno(ss,"#header"))
  // // ss ="";
  // // setCellValue(0,kk,ss);
  //
  // cellStyle =cell.getCellStyle();
  // if (kk ==1) {
  // setCellValue(0,kk,s1);
  // }
  // else setCellValue(0,kk,"");
  // } //-- end for loop
  //
  // if (checkRowspan >= rowSpanCount) {
  // setColumnWidths();
  // document.add(pTable);
  // pTable = null;
  // im_size +=im_size_row;
  // im_size_row =0;
  // }
  // }
  // }

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

    imSizeRow += row.getHeightInPoints();
  }

  void nullTableRow(float floats) throws Exception {
    float lfHh = row.getHeightInPoints();
    if (floats > 0) {
      row.setHeightInPoints(floats);
      imSize += floats;
    } else {
      row.setHeightInPoints(imListhh);
      imSize += imListhh;
    }
    cellStyle = wb.createCellStyle();
    pTable = new PdfPTable(1);
    setCellValue(0, 0, "");
    pTable.setWidthPercentage(fullWidth);

    document.add(pTable);
    rowSpanCount = 0;
    checkRowspan = 0;
    pTable = null;
    imSizeRow = 0;
    row.setHeightInPoints(lfHh);
    return;
  }

  void setCellValue(int llData, int licc, String str) throws Exception {
    String fieldName = "", cvtData = "", cellData = "";
    int rowCntl = row.getRowNum();
    int cnt = licc;
    // String hh_col="";

    // 跳過 ROW SPAN,COLUMN SPAN 產生之重覆欄位
    String checkSkip = (String) skipHash.get((rowCntl + "#" + cnt));
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
    imageField = false;
    barCode39 = false;

    if (commString.empty(cellData)) {
      pCell = new PdfPCell(new Paragraph(cellData, fon));
    } else {
      boolean lbHide = false;
      int start = cellData.indexOf("{");
      int end = cellData.indexOf("}");
      if (start != -1 && end != -1) {
        fieldName = cellData.substring(start + 1, end).trim();
        // 處理 IMAGE 顯示
        if (fieldName.length() > 5 && fieldName.substring(0, 5).equals("#IMG:")) {
          fieldName = fieldName.substring(5);
          imageField = true;
        } else if (fieldName.length() > 7 && fieldName.substring(0, 7).equals("#BAR39:")) {
          fieldName = fieldName.substring(7);
          barCode39 = true;
        } else if (fieldName.toUpperCase().indexOf("#HH-CARDNO:") >= 0) {
          lbHide = true;
          fieldName = fieldName.substring(11);
        } else if (fieldName.toUpperCase().indexOf("#HH-IDNO:") >= 0) {
          lbHide = true;
          fieldName = fieldName.substring(9);
        } else if (fieldName.toUpperCase().indexOf("#HH-CNAME:") >= 0) {
          lbHide = true;
          fieldName = fieldName.substring(10);
        } else if (fieldName.toUpperCase().indexOf("#HH-EMAIL:") >= 0) {
          lbHide = true;
          fieldName = fieldName.substring(10);
        } else if (fieldName.toUpperCase().indexOf("#HH-TELNO:") >= 0) {
          lbHide = true;
          fieldName = fieldName.substring(10);
        } else if (fieldName.toUpperCase().indexOf("#HH-ADDR:") >= 0) {
          lbHide = true;
          fieldName = fieldName.substring(9);
        } else if (fieldName.toUpperCase().indexOf("#HH-ACCTNO:") >= 0) {
          lbHide = true;
          fieldName = fieldName.substring(11);
        }

        if (summField) {
          wp.convertField(fieldName);
          cvtData = wp.convertFormat(fieldName, "" + imSummValue);
        } else {
          cvtData = cvtFunction(fieldName, llData);
        }
        if (lbHide) {
          if (commString.strIn("#hh-cardno:", cellData.toLowerCase())) {
            cvtData = commString.hideCardNo(cvtData);
          } else if (commString.strIn("#hh-idno:", cellData.toLowerCase())) {
            cvtData = commString.hideIdno(cvtData);
          } else if (commString.strIn("#hh-cname:", cellData.toLowerCase())) {
            cvtData = commString.hideIdnoName(cvtData);
          } else if (commString.strIn("#hh-email:", cellData.toLowerCase())) {
            cvtData = commString.hideEmail(cvtData);
          } else if (commString.strIn("#hh-telno:", cellData.toLowerCase())) {
            cvtData = commString.hideTelno(cvtData);
          } else if (commString.strIn("#hh-addr:", cellData.toLowerCase())) {
            cvtData = commString.hideAddr(cvtData);
          } else if (commString.strIn("#hh-acctno:", cellData.toLowerCase())) {
            cvtData = commString.hideAcctNo(cvtData);
          }
        }

        cellData = cellData.substring(0, start) + cvtData + cellData.substring(end + 1);
      }

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

    pTable.addCell(pCell);
    pCell = null;
    return;
  }

  // 處理 輸出資料顯示轉換
  String cvtFunction(String cvtField, int i) throws Exception {
    cvtField = wp.convertField(cvtField);
    if (wp.descField.equals("JAVA")) {
      wp.orgField = cvtField;
      wp.userTagRr = i;
    }
    String cvtData = wp.getValue(cvtField, i);
    return wp.convertFormat(cvtField, cvtData);
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

}
