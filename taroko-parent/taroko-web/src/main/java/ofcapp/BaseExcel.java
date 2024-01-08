/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*  110-01-08  V1.00.02  tanwei     修改意義不明確變量                                                                        *   
******************************************************************************/
package ofcapp;

import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

import org.apache.poi.hssf.usermodel.*;
import java.io.*;

@SuppressWarnings({"unchecked", "deprecation"})
public abstract class BaseExcel extends BasePage {
  public String strReportId = "";

  public FileOutputStream outExcel = null;
  public HSSFWorkbook ioBook = null;
  public HSSFSheet ioSheet = null;
  public HSSFRow ioRow = null;

  public enum em_align {
    left, center, right
  };

  HSSFCellStyle cstyle = null;

  int iirr = 0;

  public abstract void showScreen(TarokoCommon wr) throws Exception;

  public abstract void actionFunction(TarokoCommon wr) throws Exception;

  public abstract void clearFunc() throws Exception;

  public abstract void queryFunc() throws Exception;

  public abstract void queryRead() throws Exception;

  public abstract void querySelect() throws Exception;

  public void excelOpen(String id) {
    rc = 1;
    try {
      if (empty(id)) {
        wp.exportFile =
            this.loginUser() + "_" + wp.sysDate.substring(4) + "_" + wp.sysTime + ".xls";
        strReportId = "sheet1";

      } else {
        wp.exportFile =
            this.loginUser() + "_" + id + "_" + wp.sysDate.substring(4) + "_" + wp.sysTime
                + ".xls";
        strReportId = id;
      }

      // if (eq_any(wp.fileMode,"Y")) {
      // wp.linkURL ="./WebData/work/"+wp.exportFile;
      // outExcel = new FileOutputStream(wp.dataRoot+"/work/"+wp.exportFile);
      // }
      // else {
      // wp.ba = new ByteArrayOutputStream();
      // }

      ioBook = new HSSFWorkbook();
      ioSheet = ioBook.createSheet();
      // HSSFPatriarch patriarch = sh.createDrawingPatriarch();
      if (empty(id) == false) {
        ioBook.setSheetName(0, strReportId);
      }

      cstyle = ioBook.createCellStyle();
    } catch (Exception ex) {
      rc = -1;
      wp.expMethod = "excel_open";
      wp.expHandle(ex);
      return;
    }

    // ddd("openFile="+wp.exportFile);
    return;
  }

  public void excelOutput() {
    try {
      if (eqAny(wp.fileMode, "Y")) {
        wp.linkURL = "./WebData/work/" + wp.exportFile;
        outExcel = new FileOutputStream(TarokoParm.getInstance().getDataRoot() + "/work/" + wp.exportFile);
        ioBook.write(outExcel);
        wp.linkMode = "Y";
        outExcel.close();
      } else {
        wp.ba = new ByteArrayOutputStream();
        ioBook.write(wp.ba);
        wp.exportXls = "Y";
        wp.ba.close();
      }
    } catch (Exception ex) {
      rc = -1;
      wp.expMethod = "excel_output";
      wp.expHandle(ex);
    }
    return;
  }

  // public void fileClose() {
  // ddd("fileClose="+outExcel.toString());
  //
  // if (outExcel == null) {
  // return;
  // }
  //
  // try {
  // if ( wp.fileMode.equals("Y") ) {
  // io_book.write(outExcel);
  // wp.linkMode = "Y";
  // outExcel.close();
  // } else {
  // io_book.write(wp.ba);
  // wp.exportXls = "Y";
  // wp.ba.close();
  // }
  // } catch(Exception ex) {
  // rc =-1;
  // wp.expMethod = "fileClose";
  // wp.expHandle(ex);
  // }
  //
  // return;
  // }

  public void addRow() {
    iirr = ioSheet.getLastRowNum() + 1;
    ioRow = ioSheet.createRow(iirr);
    // ddd("add_row="+ii_rr);
  }

  public void addRow(int num) {
    ioRow = ioSheet.createRow(num);
    iirr = ioRow.getRowNum();
    // ddd("add_row="+ii_rr);
  }

  public void cellValue(int num, String strName) {
    HSSFCell cell = ioRow.createCell(num);
    cell.setCellValue(strName);

    // HSSFCellStyle cstyle = io_book.createCellStyle();
    cstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    cell.setCellStyle(cstyle);

  }

  public void cellValue(int num, String strName, em_align algn) {
    HSSFCell cell = ioRow.createCell(num);
    cell.setCellValue(strName);

    // HSSFCellStyle cstyle = io_book.createCellStyle();
    cstyle = ioBook.createCellStyle();
    if (algn.equals(em_align.left)) {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
    } else if (algn.equals(em_align.center)) {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    } else if (algn.equals(em_align.right)) {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
    } else {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    }
    cell.setCellStyle(cstyle);
  }

  public void cellValue(int num, String strName, String algn) {
    HSSFCell cell = ioRow.createCell(num);
    cell.setCellValue(strName);

    // HSSFCellStyle cstyle = io_book.createCellStyle();
    if (eqIgno(algn, "left") || eqIgno(algn, "L")) {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
    } else if (eqIgno(algn, "center") || eqIgno(algn, "C")) {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    } else if (eqIgno(algn, "right") || eqIgno(algn, "R")) {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
    } else {
      cstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    }
    cell.setCellStyle(cstyle);
  }

  public void cellValue(int num, double num1) {
    HSSFCell cell = ioRow.createCell(num);
    cell.setCellValue(num1);

    // HSSFCellStyle cstyle = io_book.createCellStyle();
    cstyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
    cstyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
    cell.setCellStyle(cstyle);
  }

  public void cellValue(int cc, double num1, String fmt) {
    HSSFCell cell = ioRow.createCell(cc);
    cell.setCellValue(num1);

    // HSSFCellStyle cstyle = io_book.createCellStyle();
    cstyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
    cstyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(fmt));
    cell.setCellStyle(cstyle);
  }

}
