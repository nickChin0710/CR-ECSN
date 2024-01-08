/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*  110-01-08  V1.00.02  tanwei     修改意義不明確變量                                                                       *   
******************************************************************************/
package ofcapp;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import taroko.com.TarokoParm;

import java.io.*;

@SuppressWarnings({"unchecked", "deprecation"})
public class BaseExcelX extends BasePage {
  String strReportId = "";
  public FileOutputStream outExcel = null;
  InputStream inExcel = null;

  public Workbook ioBook = null;
  public Sheet ioSheet = null;
  public Row ioRow = null;
  public Cell ioCell = null;
  public CellStyle[] dataStyle = new XSSFCellStyle[100];

  /*
   * String is_excelTempl ="rskr0020"; XSSFWorkbook wb = null; XSSFSheet sheet = null; XSSFRow row =
   * null; XSSFCell cell = null; *
   */

  // CellStyle cstyle = null;
  DataFormat format = null;
  int iirr = 0;
  CellStyle styleRR = null;
  CellStyle styleCC = null;
  CellStyle styleLL = null;

  public BaseExcelX(String id) {
    if (isEmpty(id)) {
      strReportId = "sheet1";
    } else {
      strReportId = id;
    }
  }

  // ->>>Template report-
  public void templFileOpen() {
    wp.fileMode = "Y"; // -default-

    try {
      if (inExcel == null) {
        inExcel = new FileInputStream(TarokoParm.getInstance().getDataRoot() + "/ExcelTemplate/" + strReportId + ".xlsx");
        ioBook = new XSSFWorkbook(inExcel);
      }
      ioSheet = ioBook.getSheetAt(0);

      // -format-
      format = ioBook.createDataFormat();
      // cstyle = io_book.createCellStyle();

      styleRR = ioBook.createCellStyle();
      styleRR.setAlignment(CellStyle.ALIGN_RIGHT);
      styleCC = ioBook.createCellStyle();
      styleCC.setAlignment(CellStyle.ALIGN_CENTER);
      styleLL = ioBook.createCellStyle();
      styleLL.setAlignment(CellStyle.ALIGN_LEFT);
    } catch (Exception ex) {
      wp.expMethod = "fileOpen_templ";
      wp.expHandle(ex);
    }
  }

  public void dataFormat(int rowNo) {
    Cell cell = null;
    int rr = rowNo - 1;

    ioRow = (XSSFRow) ioSheet.getRow(rr);
    for (int kk = 0; kk < ioRow.getLastCellNum(); kk++) {
      cell = ioRow.getCell(kk);
      if (cell == null) {
        continue;
      }

      dataStyle[kk] = ioBook.createCellStyle();
      dataStyle[kk].cloneStyleFrom(cell.getCellStyle());
    }

    return;
  }

  public void templOutput() throws Exception {
    wp.exportFile = strReportId + "_" + wp.sysDate.substring(4) + "_" + wp.sysTime + ".xlsx";
    if (wp.fileMode.equals("Y")) {
      // wp.linkMode = "Y";
      // wp.linkURL = "./WebData/work/"+wp.exportFile;
      try (FileOutputStream outExcelFile =
          new FileOutputStream(new File(TarokoParm.getInstance().getDataRoot() + "/work/" + wp.exportFile))) {
        ioBook.write(outExcelFile);
      } finally {
      }
    } else {
      wp.exportXls = "Y";
      wp.ba = new ByteArrayOutputStream();
      ioBook.write(wp.ba);
      wp.ba.close();
    }
    return;
  }


  public void fileOpen() {
    rc = 1;
    try {
      ioBook = new XSSFWorkbook();

      ioSheet = ioBook.createSheet();
      ioBook.setSheetName(0, strReportId);

      format = ioBook.createDataFormat();
      // cstyle = io_book.createCellStyle();

      styleRR = ioBook.createCellStyle();
      styleRR.setAlignment(CellStyle.ALIGN_RIGHT);
      styleCC = ioBook.createCellStyle();
      styleCC.setAlignment(CellStyle.ALIGN_CENTER);
      styleLL = ioBook.createCellStyle();
      styleLL.setAlignment(CellStyle.ALIGN_LEFT);

    } catch (Exception ex) {
      rc = -1;
      wp.expMethod = "file_open";
      wp.expHandle(ex);
      return;
    }

    return;
  }

  public void fileOutput(String outFile) {
    try {
      outExcel = new FileOutputStream(outFile);
      ioBook.write(outExcel);
      outExcel.close();
      ioBook.close();
    } catch (Exception ex) {
      rc = -1;
      wp.expMethod = "file_output";
      wp.expHandle(ex);
    }
    return;
  }

  public void baOutput() {
    try {
      wp.ba = new ByteArrayOutputStream();
      ioBook.write(wp.ba);
      wp.exportXls = "Y";
      wp.ba.close();
      ioBook.close();
    } catch (Exception ex) {
      rc = -1;
      wp.expMethod = "ba_output";
      wp.expHandle(ex);
    }
    return;
  }

  public void getRow(int num) {
    ioRow = ioSheet.getRow(num);
    iirr = ioRow.getRowNum();
  }

  public void addRow() {
    iirr++;
    ioRow = ioSheet.createRow(iirr);
  }

  public void addRow(int rr) {
    ioRow = ioSheet.createRow(rr);
    iirr = ioRow.getRowNum();
  }

  public void cellStyle(CellStyle cstyle) {
    ioCell.setCellStyle(cstyle);
  }

  public void cellStyle(int num) {
    cellStyle(dataStyle[num]);
  }

  public void addData(int num, String strName, CellStyle cstyle) {
    ioCell = ioRow.createCell(num);
    ioCell.setCellValue(strName);
    ioCell.setCellStyle(cstyle);
  }

  public void addData(int num, double num1, CellStyle cstyle) {
    ioCell = ioRow.createCell(num);
    ioCell.setCellValue(num1);
    ioCell.setCellStyle(cstyle);
  }

  public void addData(int num, String strName) {
    ioCell = ioRow.createCell(num);
    ioCell.setCellValue(strName);

    // CellStyle cstyle = io_book.createCellStyle();
    // cstyle.setAlignment(CellStyle.ALIGN_CENTER);
    // cell.setCellStyle(cstyle);

  }

  public void addData(int num, String strName, AlignEm algn) {
    ioCell = ioRow.createCell(num);
    ioCell.setCellValue(strName);

    // CellStyle cstyle = io_book.createCellStyle();
    if (algn.equals(AlignEm.left)) {
      // cstyle.setAlignment(CellStyle.ALIGN_LEFT);
      ioCell.setCellStyle(styleLL);
    } else if (algn.equals(AlignEm.center)) {
      // cstyle.setAlignment(CellStyle.ALIGN_CENTER);
      ioCell.setCellStyle(styleCC);
    } else if (algn.equals(AlignEm.right)) {
      // cstyle.setAlignment(CellStyle.ALIGN_RIGHT);
      ioCell.setCellStyle(styleRR);
    } else {
      // cstyle.setAlignment(CellStyle.ALIGN_CENTER);
      ioCell.setCellStyle(styleCC);
    }
    // cell.setCellStyle(cstyle);
  }

  public void addData(int num, String strName, String algn) {
    ioCell = ioRow.createCell(num);
    ioCell.setCellValue(strName);

    // CellStyle cstyle = io_book.createCellStyle();
    if (eqIgno(algn, "left") || eqIgno(algn, "L")) {
      // cstyle.setAlignment(CellStyle.ALIGN_LEFT);
      ioCell.setCellStyle(styleLL);
    } else if (eqIgno(algn, "center") || eqIgno(algn, "C")) {
      // cstyle.setAlignment(CellStyle.ALIGN_CENTER);
      ioCell.setCellStyle(styleCC);
    } else if (eqIgno(algn, "right") || eqIgno(algn, "R")) {
      // cstyle.setAlignment(CellStyle.ALIGN_RIGHT);
      ioCell.setCellStyle(styleRR);
    } else {
      // cstyle.setAlignment(CellStyle.ALIGN_CENTER);
      ioCell.setCellStyle(styleCC);
    }
    // cell.setCellStyle(cstyle);
  }

  public void addData(int numData, double numDou) {
    addData(numData, numDou, "#,##0");
    // Cell cell=io_row.createCell(cc);
    // cell.setCellValue(num1);
    //
    // CellStyle cstyle = io_book.createCellStyle();
    // cstyle.setAlignment(CellStyle.ALIGN_RIGHT);
    // cstyle.setDataFormat(format.getFormat("#,##0"));
    // cell.setCellStyle(cstyle);
  }

  public void addData(int numData, double numDou, String fmt) {
    ioCell = ioRow.createCell(numData);
    ioCell.setCellValue(numDou);

    CellStyle cstyle = ioBook.createCellStyle();
    cstyle.setAlignment(CellStyle.ALIGN_RIGHT);
    cstyle.setDataFormat(format.getFormat(fmt));
    ioCell.setCellStyle(cstyle);
  }

  // -ADD_data: date-
  public void addYmd(int num, String strName) {
    ioCell = ioRow.createCell(num);
    if (isEmpty(strName)) {
      ioCell.setCellValue("");
    }

    String colName = nvl(strName);
    switch (colName.length()) {
      case 8:
        ioCell.setCellValue(colName.substring(0, 4) + "/" + colName.substring(4, 6) + "/" + colName.substring(6));
        break;
      case 7:
        ioCell.setCellValue(colName.substring(0, 3) + "/" + colName.substring(3, 5) + "/" + colName.substring(5));
        break;
      case 6:
        ioCell.setCellValue(colName.substring(0, 2) + "/" + colName.substring(2, 4) + "/" + colName.substring(4));
        break;
      case 4:
        ioCell.setCellValue(colName.substring(0, 2) + "/" + colName.substring(2));
        break;
      default:
        ioCell.setCellValue(colName);
    }
    ioCell.setCellStyle(styleCC);

    // CellStyle cstyle = io_book.createCellStyle();
    // cstyle.setAlignment(CellStyle.ALIGN_RIGHT);
    // cell.setCellStyle(cstyle);
  }

  public void addTime(int num, String strName) {
    ioCell = ioRow.createCell(num);
    if (isEmpty(strName)) {
      ioCell.setCellValue("");
    }

    String colName = nvl(strName);
    switch (colName.length()) {
      case 6:
        ioCell.setCellValue(colName.substring(0, 2) + ":" + colName.substring(2, 4) + ":" + colName.substring(4));
        break;
      case 4:
        ioCell.setCellValue(colName.substring(0, 2) + ":" + colName.substring(2));
        break;
      default:
        ioCell.setCellValue(colName);
    }

    ioCell.setCellStyle(styleCC);
    // CellStyle cstyle = io_book.createCellStyle();
    // cstyle.setAlignment(CellStyle.ALIGN_RIGHT);
    // cell.setCellStyle(cstyle);
  }


}
