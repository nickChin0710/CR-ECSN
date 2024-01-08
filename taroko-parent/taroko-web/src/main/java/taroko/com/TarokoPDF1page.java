/**
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
 *  PDF公用程式-[套版]-V.18-1227
   18-1227:    JH    initial
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-07-17  V1.00.01  Zuwei       兆豐國際商業銀行 => 合作金庫商業銀行      *
* 109-07-24  V1.00.01  Zuwei       coding standard      *
* 109-09-30  V1.00.02  JustinWu   change the way to get URL
* 110-01-08  V1.00.02   shiyuqi       修改无意义命名
* 110-09-22  V1.00.03  Justin   use old version itext
******************************************************************************/
package taroko.com;

import com.lowagie.text.pdf.*;
import org.apache.poi.xssf.usermodel.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

public class TarokoPDF1page extends TarokoPDFBase {

  // 產生 PDF 報表
  public void procesPDFreport(TarokoCommon wr) throws Exception {
    this.wp = wr;
    if (wpIndx >= 0) {
      if (wp.listCount[wpIndx] <= 0) {
        wp.log("列印筆數為 0");
        return;
      }
    }

    linePrint = true;
    FileOutputStream pdfFile = null;
    try {
      procPrepare();

      wp.fileMode = "Y"; // modify jack 2018/07/18
      if (wp.fileMode.equals("Y")) {
        wp.linkMode = "Y";
        wp.linkURL = wp.getWorkPath(wp.exportFile);

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

      // -open ExcelTemplate-----------------
      inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot()  + "/ExcelTemplate/" + excelTemplate);
      wb = new XSSFWorkbook(inExcelFile);
      sheet = wb.getSheetAt(sheetNo);
      spanControl();

      getExcelRowCount();

      wp.setValue("SYS_DATE", wp.dispDate, 0);
      wp.setValue("SYS_TIME", wp.dispTime, 0);
      wp.setValue("PAGE_NO", "" + pageNo, 0);
      wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);

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

      inExcelFile.close();
      inExcelFile = null;
    } catch (Exception ex) {
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

  @Override
  void printReport() throws Exception {
    for (int ll = 0; ll < wp.listCount[wpIndx]; ll++) {
      pageNo++;
      document.newPage();
      printListdata(ll);
    }
  }

  @Override
  void printListdata(int llRow) throws Exception {
    pageNo++;

    Iterator rowIterator = sheet.rowIterator();
    int liProcRow = 0;
    while (rowIterator.hasNext()) {
      liProcRow++;

      row = (XSSFRow) rowIterator.next();
      rowInitial();

      boolean skipRow = false;
      boolean fileEnd = false;
      for (int cnt = 0; cnt < columnCount; cnt++) {
        cell = null;
        cell = row.getCell(cnt);
        colWidth[cnt] = sheet.getColumnWidth(cnt);
        if (cell == null) {
          cellStyle = wb.createCellStyle();
          setCellValue(0, 0, "");
          continue;
        }

        String str = commString.nvl(getCellData(cell));
        if (cnt == 0 && commString.eqIgno(str, "###")) {
          skipRow = true;
          break;
        }
        if (cnt == 0 && (str.equalsIgnoreCase("#EEE") || str.equalsIgnoreCase("END"))) {
          fileEnd = true;
          break;
        }

        if (cnt == 0)
          str = "";

        cellStyle = cell.getCellStyle();
        setCellValue(llRow, cnt, str);
      } // end of column for loop

      if (skipRow) {
        continue;
      }

      if (checkRowspan >= rowSpanCount) {
        setColumnWidths();
        document.add(pTable);
        pTable = null;
      }

      if (fileEnd) {
        break;
      }
    } // end of while

  }

  @Override
  void printSummary(int llRow) throws Exception {

  }

  @Override
  void printTotal() throws Exception {

  }

  @Override
  void printFooter() throws Exception {

  }


}
