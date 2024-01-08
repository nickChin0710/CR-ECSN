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
* 110-01-08  V1.00.02   shiyuqi       修改无意义命名
******************************************************************************/
package taroko.com;
/** Excel公用程式
 * 109-09-30  V1.00.07    Justin         change the way to get URL 
 * 109/09/05  V1.00.06    yanghan     fix code scan issue    
 * 109-09-04  yanghan     解决Portability Flaw: Locale Dependent Comparison问题    * 
 * 2020-0221   JustinWu modify the way to get the path of excel templates
 * 2020-0122   JustinWu read the template in war
 * 2019-0902   JH    user_id
 * 2018-0911:  JH   ++outputExcel_url()
 * 2018-0706:  Alex  ++ECS_BANK_NAME
 * 2018-0316: Jack  workDir
 * 2018-0309: JH    ++file_name
 * 2017-1221:   jack  outputExcel()

 * */
import java.io.*;
import java.util.*;
import java.util.Iterator;
import org.apache.poi.xssf.usermodel.*;

import Dxc.Util.SecurityUtil;

import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.util.CellRangeAddress;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoExcel {

  XSSFWorkbook wb = null;
  XSSFSheet sheet = null;
  XSSFRow row = null;
  XSSFRow dummyRow = null;
  XSSFCell cell = null;

  // public String file_name="";
  public String pageBreak = "N"; // 是否分頁
  public int pageCount = 0; // 每頁幾筆

  public String excelTemplate = "", browseMark = "", browseLine = "", fieldName = "",
      nextPage = "";
  public int rowPoint = 0, rowNum = 0, copyStart = 0, copyEnd = 0, pageControl = 1;

  public String[] sheetName = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", ""};

  InputStream inExcelFile = null;

  int colLimit = 100, maximumColumn = 0;

  String[][] svCellValue = new String[200][colLimit];
  boolean fileEnd = false;

  TarokoCommon wp = null;
  XSSFCellStyle style = null;
  CellCopyPolicy policy = new CellCopyPolicy();
  HashMap<String, Integer> rowHash = new HashMap<String, Integer>();

  public void processExcelSheet(TarokoCommon wp) throws Exception {
    this.wp = wp;
    if (inExcelFile == null) {
    	inExcelFile = new FileInputStream(SecurityUtil.verifyPath(TarokoParm.getInstance().getDataRoot() + "/ExcelTemplate/"+excelTemplate));
      wb = new XSSFWorkbook(inExcelFile);
      style = wb.createCellStyle();
    }

    wp.setValue("SYS_DATE", wp.dispDate, 0);
    wp.setValue("SYS_TIME", wp.dispTime, 0);
    wp.setValue("PAGE_NO", "1", 0);
    wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);
    wp.setValue("USER_ID", wp.loginUser, 0);

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

        if (cnt == 0 && cellValue.indexOf("###") == 0) {
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
        }

        procCellValue(0, cnt, cellValue);

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
      shiftCnt = (wp.listCount[sheetNo] - 1) * (controlCnt + 1);
      if (pageBreak.equals("Y") && pageCount > 0) {

        int pgCnt = wp.listCount[sheetNo] / pageCount;
        if ((wp.listCount[sheetNo] % pageCount) == 0) {
          pgCnt--;
        }
        shiftCnt += (pgCnt * copyStart);

      }

      if (rowNum < rowPoint && shiftCnt > 0) {
        sheet.shiftRows(rowNum + 1, rowPoint, shiftCnt);
      }

      // 一頁一筆　特殊處理
      pageControl = 1;
      if (pageBreak.equals("Y") && pageCount == 1) {
        processNextPage();
      }

      for (int i = 1; i < wp.listCount[sheetNo]; i++) {
        listBrowseData(i, copyStart, copyEnd, controlCnt, "L"); // 處理 瀏覽區之資料
        if (nextPage.equals("Y") && (i != wp.listCount[sheetNo] - 1)) {
          processNextPage();
        }
      }
      controlCnt = 0;
    }

    if (dummyRow != null) {
      sheet.removeRow(dummyRow);
      dummyRow = null;
    }

  } // end of genExcelSheet


  // TABLE 跨列處理 ROW SPAN 要合併 COPY
  void spanControl() throws Exception {

    int regionNumber = sheet.getNumMergedRegions();
    for (int i = 0; i < regionNumber; i++) {
      CellRangeAddress region = sheet.getMergedRegion(i);
      int spanRowFirst = region.getFirstRow();
      int spanRowLast = region.getLastRow();
      int row = spanRowLast - spanRowFirst;
      if (row > 0) {
        rowHash.put("#-" + spanRowFirst, row);
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
    if (procFlag.equals("H")) {
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

    pageControl++;
    if (pageBreak.equals("Y") && pageControl >= pageCount) {
      nextPage = "Y";
    }

    return;

  }

  // 處理 換頁
  public void processNextPage() throws Exception {

    sheet.setRowBreak(rowNum);
    listBrowseData(0, 0, copyStart - 1, 0, "H");
    pageControl = 0;
    nextPage = "N";
    return;
  }

  // 處理 TABLE CELL 之值
  public void procCellValue(int i, int cnt, String cellValue) throws Exception {

    int start = cellValue.indexOf("{");
    int end = cellValue.indexOf("}");
    if (start != -1 && end != -1) {
      fieldName = cellValue.substring(start + 1, end);
      String cvtData = cvtFunction(fieldName, i);
      cellValue = cellValue.substring(0, start) + cvtData + cellValue.substring(end + 1);
      if (cnt == 0) {
        cellValue = "";
      }
    }
    cell.setCellValue(cellValue);
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
          new FileOutputStream(new File(SecurityUtil.verifyPath(TarokoParm.getInstance().getWorkDir()) +SecurityUtil.verifyPath(wp.exportFile) ))) {
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
    try (FileOutputStream outExcelFile = new FileOutputStream(new File(SecurityUtil.verifyPath(TarokoParm.getInstance().getWorkDir()+wp.exportFile)))) {
      wb.write(outExcelFile);
    } finally {
    }

    inExcelFile.close();
    inExcelFile = null;
    return lsLinkURL;
  }

}
