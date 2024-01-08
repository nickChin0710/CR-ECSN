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
*  110-01-08  V1.00.02   shiyuqi       修改无意义命名                        *
*  110-09-22  V1.00.03   Justin   use old version itext                       *  
******************************************************************************/
package taroko.com;
/** 報表公用程式2[以行數列印]
 * 2019-1021   JH    --Header_Footer
 2019-0419:  JH    vertical_print(int)
 *  19-0104:    JH       pageCount:可印行數, 不含表頭(橫印39)
 *  19-0103:    JH       initial
 *  20-0930   JustinWu  change the way to get URL
 * */

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import com.lowagie.text.pdf.*;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class TarokoPDFLine extends taroko.com.TarokoPDFBase {

  private int printLine = 0;

  public void verticalPrint(int aiLine) {
    pageVert = true;
    if (aiLine > 0)
      pageCount = aiLine;
    else
      pageCount = 57;
  }

  // 產生 PDF 報表
  public void procesPDFreport(TarokoCommon wr) throws Exception {
    this.wp = wr;
    if (wpIndx >= 0) {
      if (wp.listCount[wpIndx] <= 0) {
        wp.log("列印筆數為 0");
        return;
      }
    }

    wp.setValue("SYS_DATE", wp.dispDate, 0);
    wp.setValue("SYS_TIME", wp.dispTime, 0);
    wp.setValue("PAGE_NO", "" + pageNo, 0);
    wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);
    if (wp.colEmpty("user_id")) {
      wp.setValue("USER_ID", wp.loginUser, 0);
    }

    // ecsfunc.SecFunc oo_sec =new SecFunc(wp);
    // oo_sec.report_HeaderFooter(wp.mod_pgm());
    // if (!commString.empty(wp.col_ss(0,"report_header"))) {
    // ii_sec_header++;
    // }
    // if (!commString.empty(wp.col_ss(1,"report_header"))) {
    // ii_sec_header++;
    // }
    // if (wp.col_ss(0,"report_footer").length()>0) {
    // ii_sec_footer++;
    // }
    // if (wp.col_ss(1,"report_footer").length()>0) {
    // ii_sec_footer++;
    // }

    linePrint = true; // -行數列印-
    FileOutputStream pdfFile = null;
    try {
      procPrepare();

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

  void printReport() throws Exception {

    printHeader();

    // -print list data-
    for (int ll = 0; ll < wp.listCount[wpIndx]; ll++) {
      printListdata(ll);
    }
    printSummary(wp.listCount[wpIndx]);
    printTotal();
    printFooter();
  }

  void docuAddTable() throws Exception {
    setColumnWidths();

    int liLine = pTable.getRows().size();
    liLine += printLine;
    if (liLine >= pageCount) {
      PdfPTable loTab = pTable;
      pTable = null;
      printHeader();
      pTable = loTab;
      loTab = null;
    }

    document.add(pTable);
    printLine += pTable.getRows().size();
    pTable = null;
  }

  void printListdata(int llRow) throws Exception {
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
        docuAddTable();
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

  void printSummary(int llRow) throws Exception {
    // 列印小計--
    if (summ == false)
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
        docuAddTable();
        if (lbChgPage && llRow < wp.listCount[wpIndx]) {
          printHeader();
        }
      }
    }

  }

  void printTotal() throws Exception {

    // -列印總計--
    if (summ == false)
      return;
    wp.log("PDF-sum-[%s]-[%s]-[%s]", wp.colStr("tl_up_cnt"), wp.colStr("tl_down_cnt"),
        wp.colStr("tl_adj_cnt"));

    int llRow = wp.listCount[wpIndx];

    String col = "";
    for (int ll = 0; ll < iisummRow; ll++) {

      // -小計不處理:2019-0628-
      // if (commString.empty(summ_kk[ll])==false) {
      // continue;
      // }

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
        docuAddTable();
      }
    }

  }

  void printHeader() throws Exception {
    String lsCellData = "";

    pageNo++;
    if (pageNo <= 1) {
      wp.colSet("sys_dtime", wp.dispDate.substring(2) + " " + wp.dispTime);
      wp.setValue("SYS_DATE", wp.dispDate, 0);
      wp.setValue("SYS_TIME", wp.dispTime, 0);
      wp.setValue("ECS_BANK_NAME", "合作金庫商業銀行", 0);
    }
    wp.setValue("PAGE_NO", "" + pageNo, 0);

    if (pageNo > 1) {
      printFooter();
    }

    document.newPage();
    // ii_proc_cnt =0;
    printLine = 0;

    // im_size =0;
    // im_size_row =0;
    Iterator rowIterator = sheet.rowIterator();
    while (rowIterator.hasNext()) {
      row = (XSSFRow) rowIterator.next();
      cell = row.getCell(0);
      lsCellData = getCellData(cell);
      if (commString.strIn2(lsCellData, ",EEE,#END"))
        break;
      // -無效row-
      if (commString.eqIgno(lsCellData, "###"))
        continue;

      if (!commString.empty(lsCellData) && !commString.eqIgno(lsCellData, "TITLE")
          && !commString.eqIgno(lsCellData, "#header"))
        break;

      // -SEC-Header-
      if (commString.eqIgno(lsCellData, "#header")) {
        printSecHeader(wp.colStr(0, "report_header"));
        printSecHeader(wp.colStr(1, "report_header"));
        continue;
      }

      // _print_line++;
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

        String str = commString.nvl(getCellData(cell));

        cellStyle = cell.getCellStyle();
        setCellValue(0, kk, str);
      } // -- end for loop

      if (checkRowspan >= rowSpanCount) {
        setColumnWidths();
        document.add(pTable);
        printLine += pTable.getRows().size();
        pTable = null;
      }
    }
  }

  void printFooter() throws Exception {
    if (footer == false)
      return;

    // int li_line_empty =(pageCount - ii_proc_cnt);
    int liLineEmpty = (pageCount - printLine);
    if (secFooter > 0)
      liLineEmpty--;

    for (int ii = 0; ii < liLineEmpty; ii++) {
      nullTableRow(0);
    }

    for (int ll = 0; ll < iifootRow; ll++) {
      row = footRow[ll];
      cell = row.getCell(0);
      String lsCellData = getCellData(cell);
      if (commString.eqIgno(lsCellData, "#footer")) {
        printSecFooter(wp.colStr(0, "report_footer"));
        printSecFooter(wp.colStr(1, "report_footer"));
        continue;
      }

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
    }
    setColumnWidths();
    document.add(pTable);
    printLine += pTable.getRows().size();
    pTable = null;
  }

  void printSecHeader(String str) throws Exception {
    if (secHeader <= 0)
      return;
    if (commString.empty(str))
      return;

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

      cellStyle = cell.getCellStyle();
      if (kk == 1) {
        setCellValue(0, kk, str);
      } else
        setCellValue(0, kk, "");
    } // -- end for loop

    if (checkRowspan >= rowSpanCount) {
      setColumnWidths();
      document.add(pTable);
      printLine += pTable.getRows().size();
      pTable = null;
    }
  }

  void printSecFooter(String s1) throws Exception {
    if (secFooter <= 0)
      return;
    String lsFooter = s1;
    if (commString.empty(lsFooter))
      return;

    rowInitial();
    for (int cnt = 0; cnt < columnCount; cnt++) {
      cell = null;
      cellStyle = null;
      cell = row.getCell(cnt);
      colWidth[cnt] = sheet.getColumnWidth(cnt);
      if (cell == null) {
        cellStyle = wb.createCellStyle();
        setCellValue(0, cnt, "");
        continue;
      }

      cellStyle = cell.getCellStyle();
      if (cnt == 1) {
        setCellValue(0, cnt, lsFooter);
      } else
        setCellValue(0, cnt, "");
    } // -- end for loop

    // if (checkRowspan >= rowSpanCount) {
    // setColumnWidths();
    // document.add(pTable);
    // pTable = null;
    // im_size +=im_size_row;
    // im_size_row =0;
    // }
  }


}
