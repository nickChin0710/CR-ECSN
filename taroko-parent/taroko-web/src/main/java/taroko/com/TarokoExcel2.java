/**
 *  Excel公用程式 V.2018-0911.jh
 * 2018-0911:  JH   ++outputExcel_url()
 * 2018-0706:  Alex  ++ECS_BANK_NAME
 * 2018-0316: Jack  workDir
 * 2018-0309: JH    ++file_name
 * 2017-1221:   jack  outputExcel()
 * 2019-05-27:  jack  add break field
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-07-17  V1.00.01  Zuwei       兆豐國際商業銀行 => 合作金庫商業銀行      *
* 109-07-24  V1.00.01  Zuwei       coding standard      *
* 109-09-04  V1.00.01  yanghan    解决Portability Flaw: Locale Dependent Comparison問題    * 
* 109-09-30  V1.00.04  JustinWu   change the way to get URL 
* 110-01-08  V1.00.02   shiyuqi       修改无意义命名
******************************************************************************/
package taroko.com;
import java.io.*;
import java.util.*;
import java.util.Iterator;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.util.CellRangeAddress;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoExcel2 {

  XSSFWorkbook wb = null;
  XSSFSheet sheet = null;
  XSSFRow row = null;
  XSSFRow dummyRow = null;
  XSSFRow sumaryRow = null;
  XSSFCell cell = null;

  // public String file_name="";
  public String pageBreak = "N"; // 是否分頁
  public int pageCount = 0; // 每頁幾筆
  public String[] breakField = {"", "", "", "", "", "", "", "", "", "", ""}; // 分頁條件,欄位名稱
  public String[] comBreakValue = {"", "", "", "", "", "", "", "", "", "", ""};
  public int breakFieldCnt = 0;

  public String excelTemplate = "", browseMark = "", browseLine = "", fieldName = "",
      nextPage = "";
  public int toalPage = 0, rowPoint = 0, rowNum = 0, copyStart = 0, copyEnd = 0, workStart = 0,
      workEnd = 0, sumRow = 0, pageNo = 0, workCount = 0;
  public int rowControl = 1, pageControl = 1;
  public String[] sheetName = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", ""};

  public String[] sumField = new String[100];
  public int initValue = 0, fCnt = 0;
  InputStream inExcelFile = null;

  int colLimit = 100, maximumColumn = 0;

  String[][] svCellValue = new String[200][colLimit];
  boolean fileEnd = false, fieldBreak = false;

  TarokoCommon wp = null;
  XSSFCellStyle style = null;
  CellCopyPolicy policy = new CellCopyPolicy();
  HashMap<String, Integer> rowHash = new HashMap<String, Integer>();

  public void processExcelSheet(TarokoCommon wp) throws Exception {
    this.wp = wp;
    if (inExcelFile == null) {
      inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot() + "/ExcelTemplate/" + excelTemplate);
      wb = new XSSFWorkbook(inExcelFile);
      style = wb.createCellStyle();
    }

    for (int i = 0; i < breakField.length; i++) {
      if (breakField[i].length() > 0) {
        breakFieldCnt++;
      }
    }

    calculateTotalPage();
    pageNo++;
    wp.setValue("SYS_DATE", wp.dispDate, 0);
    wp.setValue("SYS_TIME", wp.dispTime, 0);
    wp.setValue("PAGE_NO", "" + pageNo + "/" + toalPage, 0);
    wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);

    List<String> sheetNames = new ArrayList<String>();
    for (int i = 0; i < wb.getNumberOfSheets(); i++) {
      genExcelSheet(i);
    }
  }

  public void genExcelSheet(int sheetNo) throws Exception {

    rowNum = 0;
    copyStart = 0;
    copyEnd = 0;
    int controlCnt = 0;

    policy.setCopyMergedRegions(true);
    policy.setCopyRowHeight(true);
    policy.setCopyCellStyle(true);
    policy.setCopyCellValue(false);

    sheet = wb.getSheetAt(sheetNo);
    spanControl();
    if (sheetName[sheetNo].length() > 0) {
      wb.setSheetName(sheetNo, sheetName[sheetNo]);
    }

    Iterator rowIterator = sheet.rowIterator();
    fileEnd = false;
    while (rowIterator.hasNext()) {

      row = (XSSFRow) rowIterator.next();
      rowPoint = row.getRowNum();
      wp.setValue("SUM-ROWCOUNT", "" + wp.listCount[sheetNo], 0);
      int int1 = 0;
      for (int cnt = 0; cnt < row.getLastCellNum(); cnt++) {
        if (cnt == 0) {
          sheet.setColumnWidth(0, 0);
        }

        String cellValue = "";

        cell = row.getCell(cnt);
        if (cell == null) {
          continue;
        }
        if (cnt >= maximumColumn && maximumColumn > 0) {
          break;
        }

        if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
          cellValue = cell.getStringCellValue();
        } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
          cellValue = "" + cell.getNumericCellValue();
        }

        svCellValue[rowPoint][cnt] = cellValue;

        if (cnt == 0 && cellValue.equals("###")) {
          dummyRow = row;
          break;
        }
        if (cellValue.toUpperCase(Locale.TAIWAN).equals("#EEE")) {
          row.removeCell(cell);
          maximumColumn = cnt;
          break;
        }

        if (cnt == 0 && cellValue.toUpperCase(Locale.TAIWAN).equals("END")) {
          fileEnd = true;
          break;
        }

        if (cnt == 0 && cellValue.equals("LLLL")) {
          browseMark = "Y";
          rowNum = rowPoint;
          controlCnt++;
          if (copyStart == 0) {
            copyStart = rowPoint;
          }
        } else if (cnt == 0 && cellValue.equals("LIST")) {
          browseLine = "Y";
          rowNum = rowPoint;
          copyEnd = rowPoint;
          if (copyStart == 0) {
            copyStart = copyEnd;
          }
        } else if (cnt == 0 && cellValue.length() >= 5 && cellValue.substring(0, 5).equals("SUM-P")) {
          sumaryRow = row;
          sumRow = rowPoint;
        } else if (cnt == 0 && cellValue.length() >= 5 && cellValue.substring(0, 5).equals("SUM-W")) {
          if (workStart == 0) {
            workStart = rowPoint;
          }
          workEnd = rowPoint;
          workCount++;
        }

        initValue = 0;
        procCellValue(0, cnt, cellValue);
        initValue = 1;

      } // end of for loop

      if (fileEnd) {
        break;
      }
    } // end of while loop

    int shiftCnt = 0;
    if (browseLine.equals("Y") && wp.listCount[sheetNo] > 0) {

      browseMark = "";
      browseLine = "";
      rowNum = copyEnd;
      if (sumRow > 0) {
        rowNum = sumRow;
      } else if (workEnd > 0) {
        rowNum = workEnd;
      }

      /*
       * shiftCnt = (wp.listCount[sheetNo]-1) * (controlCnt+1); if ( pageBreak.equals("Y") &&
       * pageCount > 0 ) {
       * 
       * int totalCount = wp.listCount[sheetNo] + getSumCount(); int pgCnt = totalCount / pageCount;
       * if ( (wp.listCount[sheetNo] % pageCount) == 0 ) { pgCnt--; } shiftCnt += ( pgCnt *
       * copyStart);
       * 
       * }
       * 
       * if ( rowNum < rowPoint && shiftCnt > 0 ) { sheet.shiftRows(rowNum+1,rowPoint,shiftCnt); }
       */

      if (sumRow > 0 || workStart > 0) {
        breakFieldControl(0);
      }

      rowControl = 1;
      pageControl = 1;
      // 一頁一筆　特殊處理
      if (pageBreak.equals("Y") && pageCount == 1) {
        processNextPage(0);
      }

      for (int i = 1; i < wp.listCount[sheetNo]; i++) {

        if (sumRow > 0 || workStart > 0) {
          checkBreakField(i);
        }

        for (int m = 0; m < fCnt; m++) {
          processSumValue(sumField[m], i);
        }

        listBrowseData(i, copyStart, copyEnd, controlCnt, "L"); // 處理 瀏覽區之資料
        if (nextPage.equals("Y") && (i != wp.listCount[sheetNo] - 1)) {
          processNextPage(i);
        }
      }

      controlCnt = 0;
    }

    int cnt = wp.listCount[sheetNo] - 1;
    wp.setValue("SUM-TOTAL-CNT", "" + rowControl, cnt);
    if (sumRow > 0) {
      listBrowseData(cnt, sumRow, sumRow, 0, "S");
    } else if (workStart > 0) {
      listBrowseData(0, workStart, workEnd, 0, "W");
    }

    if (dummyRow != null) {
      sheet.removeRow(dummyRow);
      dummyRow = null;
    }

    int lastRowNum = sheet.getLastRowNum();

    if (sumRow > 0) {
      sheet.getRow(sumRow).setZeroHeight(true);
    }

    if (workStart > 0) {
      for (int i = workStart; i <= workEnd; i++) {
        sheet.getRow(i).setZeroHeight(true);
      }
    }

  } // end of genExcelSheet

  // TABLE 跨列處理 ROW SPAN 要合併 COPY
  void spanControl() throws Exception {

    int regionNumber = sheet.getNumMergedRegions();
    for (int i = 0; i < regionNumber; i++) {
      CellRangeAddress region = sheet.getMergedRegion(i);
      int spanRowFirst = region.getFirstRow();
      int spanRowLast = region.getLastRow();
      int n = spanRowLast - spanRowFirst;
      if (n > 0) {
        rowHash.put("#-" + spanRowFirst, n);
      } // 紀錄 ROW-SPAN 欄位
    } // end of for loop

    return;
  } // end of spanControl

  // 處理 瀏覽區之背景顏色
  public void setBackgroudColor(String rgbColor) throws Exception {

    String[] rVal = rgbColor.split(",");
    byte[] rgb = new byte[3];
    rgb[0] = (byte) Integer.parseInt(rVal[0].trim());
    rgb[1] = (byte) Integer.parseInt(rVal[1].trim());
    rgb[2] = (byte) Integer.parseInt(rVal[2].trim());
    XSSFColor bgColor = new XSSFColor(rgb);
    // XSSFCellStyle style = cell.getCellStyle();
    style.setFillForegroundColor(bgColor);
    style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

    cell.setCellStyle(style);
    return;
  }

  // 處理 瀏覽區之資料
  public void listBrowseData(int i, int startPnt, int endPnt, int controlCnt, String procFlag)
      throws Exception {

    int svRow = rowNum;
    int listRow = controlCnt;
    if (Arrays.asList("H", "S", "W").contains(procFlag)) {
      listRow = (endPnt - startPnt);
    }

    for (int j = 0; j <= listRow; j++) {
      rowNum++;
      row = sheet.createRow(rowNum);
    }

    // ROW SPAN 要合併 COPY
    int row1 = startPnt;
    int dest = svRow + 1;
    while (row1 <= endPnt) {
      Integer spanRow = (Integer) rowHash.get("#-" + row1);
      if (spanRow == null) {
        spanRow = 0;
      }
      sheet.copyRows(row1, row1 + spanRow, dest, policy);
      row1 = row1 + 1 + spanRow;
      dest = dest + 1 + spanRow;
    }

    String rgbData = "";
    if (procFlag.equals("L")) {
      rgbData = wp.getValue("XLS_COLOR", i);
    }

    for (int j = startPnt; j <= (startPnt + listRow); j++) {
      svRow++;
      row = sheet.getRow(svRow);
      for (int k = 0; k < row.getLastCellNum(); k++) {
        if (svCellValue[j][k] == null) {
          continue;
        }
        cell = row.getCell(k);
        if (cell == null) {
          continue;
        }
        procCellValue(i, k, svCellValue[j][k]);
        if (rgbData.length() > 0) {
          setBackgroudColor(rgbData);
        }
      }
    }

    if (procFlag.equals("L")) {
      rowControl++;
      pageControl++;
    }

    if (pageBreak.equals("Y") && pageControl >= pageCount) {
      nextPage = "Y";
    }

    return;

  }

  public void calculateTotalPage() throws Exception {

    int totalLine = 0;
    boolean changePage = false;
    for (int i = 0; i < wp.listCount[0]; i++) {

      totalLine++;
      for (int m = 0; m < breakFieldCnt; m++) {
        if (i > 0 && !comBreakValue[m].equals(wp.getValue(breakField[m], i))) {
          changePage = true;
          break;
        }
      }

      if (changePage) {
        toalPage++;
        changePage = false;
        totalLine = 1;
      } else if (totalLine >= pageCount) {
        toalPage++;
        totalLine = 0;
      }

      for (int m = 0; m < breakFieldCnt; m++) {
        comBreakValue[m] = wp.getValue(breakField[m], i);
      }

    }

    if (totalLine > 0) {
      toalPage++;
    }

    return;
  }

  public void checkBreakField(int i) throws Exception {

    fieldBreak = false;
    for (int m = 0; m < breakFieldCnt; m++) {
      if (!comBreakValue[m].equals(wp.getValue(breakField[m], i))) {
        fieldBreak = true;
        break;
      }
    }

    if (fieldBreak) {
      wp.setValue("SUM-TOTAL-CNT", "" + rowControl, i);
      if (sumRow > 0) {
        listBrowseData(i, sumRow, sumRow, 0, "S");
      } else {
        listBrowseData(0, workStart, workEnd, 0, "W");
        for (int m = 0; m < fCnt; m++) {
          initialSumValue(sumField[m], -1);
        }
      }
      processNextPage(i);
      rowControl = 0;
      fieldBreak = false;
    }

    breakFieldControl(i);
    return;
  }

  public void breakFieldControl(int i) throws Exception {

    for (int m = 0; m < breakFieldCnt; m++) {
      comBreakValue[m] = wp.getValue(breakField[m], i);
    }

    return;
  }

  // 處理 換頁
  public void processNextPage(int i) throws Exception {

    pageNo++;
    wp.setValue("SYS_DATE", wp.dispDate, i);
    wp.setValue("SYS_TIME", wp.dispTime, i);
    wp.setValue("PAGE_NO", "" + pageNo + "/" + toalPage, i);
    wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", i);

    sheet.setRowBreak(rowNum);
    listBrowseData(i, 0, copyStart - 1, 0, "H");
    pageControl = 0;
    nextPage = "N";
    return;
  }

  // 處理 TABLE CELL 之值
  public void procCellValue(int i, int k, String cellValue) throws Exception {

    int start = cellValue.indexOf("{");
    int end = cellValue.indexOf("}");
    if (start != -1 && end != -1) {
      fieldName = cellValue.substring(start + 1, end).toUpperCase(Locale.TAIWAN);

      if (initValue == 0) {
        if (fieldName.length() > 5 && fieldName.substring(0, 4).equals("SUM-")
            && !fieldName.equals("SUM-TOTAL-CNT")) {
          String realField = fieldName;
          int pnt = fieldName.indexOf(".");
          if (pnt != -1) {
            realField = fieldName.substring(0, pnt);
          }
          sumField[fCnt] = realField;
          fCnt++;
          initialSumValue(realField, i);
        }
      }
      String cvtData = cvtFunction(fieldName, i);
      cellValue = cellValue.substring(0, start) + cvtData + cellValue.substring(end + 1);
      if (k == 0) {
        cellValue = "";
      }
    }

    cell.setCellValue(cellValue);
    return;
  }

  public void initialSumValue(String realField, int i) throws Exception {

    long tempSum = 0;
    if (i != -1) {
      tempSum = (long) wp.getNumber(realField.substring(4), i);
    }
    wp.setValue(realField, "" + tempSum, 0);
    // wp.showLogMessage("D","TTT a : ",realField+" tempSum "+tempSum+" "+i);

    return;
  }

  public void processSumValue(String realField, int i) throws Exception {

    String workSum = wp.getValue(realField, 0);
    if (workSum.length() == 0) {
      return;
    }

    long tempSum = (long) wp.getNumber(realField.substring(4), i);
    long totalSum = Long.parseLong(workSum) + tempSum;
    wp.setValue(realField, "" + totalSum, 0);
    // wp.showLogMessage("D","TTT b : ",realField+" tempSum "+totalSum+" "+i);
    return;
  }

  // 處理 TABLE CELL FORMAT 轉換
  public String cvtFunction(String cvtField, int i) throws Exception {

    cvtField = wp.convertField(cvtField);
    if (wp.descField.equals("JAVA")) {
      wp.orgField = cvtField;
      wp.userTagRr = i;
    }
    String cvtData = wp.getValue(cvtField, i);

    // wp.showLogMessage("D","TTT cvtFunction : ",cvtField+" "+i+" "+cvtData );
    return wp.convertFormat(cvtField, cvtData);
  }

  // 輸出 EXCEL FILE
  public void outputExcel() throws Exception {
    int liPos = this.excelTemplate.indexOf(".");
    String fileName = excelTemplate;
    if (liPos > 0) {
      fileName = excelTemplate.substring(0, liPos);
    }

    wp.exportFile = fileName + "-" + wp.sysDate.substring(4) + "-" + wp.sysTime + ".xlsx";
    wp.fileMode = "Y"; // modify jack 2018/07/18
    if (wp.fileMode.equals("Y")) {
      wp.linkMode = "Y";
      wp.linkURL = wp.getWorkPath(wp.exportFile);

      // FileOutputStream outExcelFile = new FileOutputStream(new File(wp.dataRoot +
      // "/work/"+wp.exportFile));
      try (FileOutputStream outExcelFile =
          new FileOutputStream(new File(TarokoParm.getInstance().getWorkDir() + wp.exportFile))) {
        wb.write(outExcelFile);
      } finally {
      }
    } else {
      wp.exportXls = "Y";
      wp.ba = new ByteArrayOutputStream();
      wb.write(wp.ba);
      wp.ba.close();
    }

    inExcelFile.close();
    inExcelFile = null;
    return;
  }

  public String outputExcelUrl() throws Exception {

    int liPos = this.excelTemplate.indexOf(".");
    String fileName = excelTemplate;
    if (liPos > 0) {
      fileName = excelTemplate.substring(0, liPos);
    }

    wp.exportFile = fileName + "-" + wp.sysDate.substring(4) + "-" + wp.sysTime + ".xlsx";
    // wp.linkMode = "Y";
    String lsLinkURL = "";
    lsLinkURL = wp.getWorkPath(wp.exportFile);

    // FileOutputStream outExcelFile = new FileOutputStream(new File(wp.dataRoot +
    // "/work/"+wp.exportFile));
    try (FileOutputStream outExcelFile = new FileOutputStream(new File(TarokoParm.getInstance().getWorkDir() + wp.exportFile))) {
      wb.write(outExcelFile);
    } finally {
    }

    inExcelFile.close();
    inExcelFile = null;
    return lsLinkURL;
  }

}
