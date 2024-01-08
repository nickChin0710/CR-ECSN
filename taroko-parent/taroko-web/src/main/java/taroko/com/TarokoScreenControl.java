/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 111-01-19  V1.00.03   Justin       fix Missing Check against Null          *  
******************************************************************************/
package taroko.com;

import java.io.*;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoScreenControl extends TarokoDAO {
  /* 顯示啟始畫面 */
  public void showScreen(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.showLogMessage("D", "showScreen", "started");

    // wp.queryButton = "Y";
    processTab();
    wp.setValue("LEVEL_CODE", "" + (wp.levelNum + 1), 0);
    return;
  }

  /* TAB 控制處理 */
  public void processTab() throws Exception {
    int tabCnt = checkTab();
    if (tabCnt == 0) {
      return;
    }
    TarokoTAB tab = new TarokoTAB();
    tab.tabType = "K";
    tab.tabWidth = 200;
    tab.tabHeight = 30;
    for (int i = 1; i <= tabCnt; i++) {
      tab.setTabConent(i, "頁簽-" + i);
    }

    tab.generateTab(wp);
    tab = null;
    return;
  }

  public int checkTab() throws Exception {
    String packageDir = wp.packageName.replaceAll("\\.", "/") + "/";
    String htmlInput = TarokoParm.getInstance().getHtmlDir() + packageDir + wp.respHtml + ".html";
    int tabCnt = 0, checkTab = 0;
    FileReader fr = null;
    BufferedReader br = null;

    try {

      fr = new FileReader(htmlInput);
      br = new BufferedReader(fr);

      while (br.ready()) {
        String inputStr = br.readLine();
        if (inputStr != null) {
        	checkTab = inputStr.indexOf("content1");
            if (checkTab != -1) {
              tabCnt = 1;
            }
            checkTab = inputStr.indexOf("content2");
            if (checkTab != -1) {
              tabCnt = 2;
            }
            checkTab = inputStr.indexOf("content3");
            if (checkTab != -1) {
              tabCnt = 3;
            }
            checkTab = inputStr.indexOf("content4");
            if (checkTab != -1) {
              tabCnt = 4;
            }
            checkTab = inputStr.indexOf("content5");
            if (checkTab != -1) {
              tabCnt = 5;
            }
            checkTab = inputStr.indexOf("content6");
            if (checkTab != -1) {
              tabCnt = 6;
            }
		}
      }
    }

    catch (Exception ex) {
      return 0;
    }

    finally {
      try {
        if (fr != null) {
          fr.close();
        }
        fr = null;
        if (br != null) {
          br.close();
        }
        br = null;
      } catch (Exception ex2) {
      }
    }
    return tabCnt;

  } // End of checkTab

  /* 啟動功能 */
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    wp.showLogMessage("D", "actionFunction", "started");

    if (wp.buttonCode.equals("Q")) /* 查詢功能 */
    {
      if (wp.secondQuery) {
        conditionQuery(wp);
      } else {
        processQuery(wp);
      }
    } else if (wp.buttonCode.equals("A")) /* 新增功能 */
    {
      insertFunction(wp);
    } else if (wp.buttonCode.equals("U")) /* 更新功能 */
    {
      updateFunction(wp);
    } else if (wp.buttonCode.equals("D")) /* 刪除功能 */
    {
      deleteFunction(wp);
    } else if (wp.buttonCode.equals("C")) /* 處理功能 */
    {
      confirmFunction(wp);
    } else if (wp.buttonCode.equals("M")) /* 瀏覽功能 */
    {
      if (wp.showSecondQuery) {
        showCondition(wp);
      } else {
        browseFunction(wp);
      }
    } else if (wp.buttonCode.equals("S")) /* 動態查詢 */
    {
      dynamicQuery(wp);
    } else if (wp.buttonCode.equals("L")) /* 清畫面 */
    {
      clearScreen(wp);
    } else if (wp.buttonCode.equals("X")) /* 新視窗顯示 */
    {
      processQuery(wp);
    } else if (wp.buttonCode.equals("L")) /* 清畫面 */
    {
      clearScreen(wp);
    } else if (wp.buttonCode.equals("XLS")) /* 顯示 EXCEL 報表 */
    {
      xlsFunction(wp);
    } else if (wp.buttonCode.equals("PDF")) /* 顯示 PDF 報表 */
    {
      pdfFunction(wp);
    }

    wp.showLogMessage("D", "actionFunction", "ended");
    return;
  } // End of actionFunction

  /* 瀏灠查詢功能 */
  public void conditionQuery(TarokoCommon wr) throws Exception {
    wp.showLogMessage("D", "conditionQuery", "started");

    wp.setQueryMode();
    browseFunction(wp);
    showScreen(wp);
    wp.showLogMessage("D", "conditionQuery", "ended");
    return;
  } // End of conditionQuery

  /* 處理功能 */
  public void confirmFunction(TarokoCommon wr) throws Exception {
    wp.showLogMessage("D", "confirmFunction", "started");
    showScreen(wp);
    wp.showLogMessage("D", "confirmFunction", "ended");
    return;
  }

  /* 顯示瀏灠查詢畫面 */
  public void showCondition(TarokoCommon wr) throws Exception {
    super.wp = wr;

    wp.showLogMessage("D", "showCondition", "started");

    wp.resetOutputData();
    showScreen(wp);
    wp.showLogMessage("D", "showCondition", "ended");
    return;
  }

  /* 查詢功能 */
  public void processQuery(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "processQuery", "started");

      wp.setQueryMode();

      browseFunction(wp);
      wp.showLogMessage("D", "processQuery", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "processQuery";
      wp.expHandle(ex);
    }

    return;
  } // End of processQuery

  /* 瀏覽功能 */
  public void browseFunction(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "browseFunction", "started");
      // wp.pdfButton = "Y";
      // wp.xlsButton = "Y";

      wp.pageControl();

      wp.selectCnt = 12;
      wp.setListCount(1);
      wp.dataCnt = wp.selectCnt;
      wp.totalRows = wp.dataCnt;

      wp.setPageValue();
      showScreen(wp);
      wp.showLogMessage("D", "browseFunction", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "browseFunction";
      wp.expHandle(ex);
    }

    return;
  } // End of browseFunction

  /* 動態查詢功能 */
  public void dynamicQuery(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "dynamicQuery", "started");

      detailFunction();
      showScreen(wp);
      wp.showLogMessage("D", "dynamicQuery", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "dynamicQuery";
      wp.expHandle(ex);
    }

    return;
  } // End of dynamicQuery

  /* 明細功能 */
  public void detailFunction() {
    try {
      wp.showLogMessage("D", "detailFunction", "started");

      wp.varRows = 0;
      // wp.updateButton = "Y";
      // wp.deleteButton = "Y";

      wp.notFound = "";
      wp.dispMesg = "";
      wp.setDetailMode();
      processTab();
      showScreen(wp);
      wp.showLogMessage("D", "detailFunction", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "detailFunction";
      wp.expHandle(ex);
    }

    return;
  }

  /* 新增功能 */
  public void insertFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.showLogMessage("D", "insertFunction", "started");
    showScreen(wp);
    wp.showLogMessage("D", "insertFunction", "ended");
    return;
  }

  /* 更新處理功能 */
  public void updateFunction(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "updateFunction", "started");
      showScreen(wp);
      wp.showLogMessage("D", "updateFunction", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "updateFunction";
      wp.expHandle(ex);
    }

    return;
  }

  /* 刪除處理功能 */
  public void deleteFunction(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "deleteFunction", "started");
      showScreen(wp);
      wp.showLogMessage("D", "deleteFunction", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "deleteFunction";
      wp.expHandle(ex);
    }

    return;
  }

  /* 上下筆功能 */
  public void rowFunction(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "rowFunction", "started");

      // wp.updateButton = "Y";
      // wp.deleteButton = "Y";
      // wp.rowControl();
      detailFunction();
      showScreen(wp);
      wp.showLogMessage("D", "rowFunction", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "rowFunction";
      wp.expHandle(ex);
    }

    return;
  }

  /* 產生 EXCEL 報表 */
  public void xlsFunction(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "xlsFunction", "started");

      browseFunction(wp);

      wp.setValue("REPORT_ID", "SAMPLE_RPT_0001", 0);

      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "SAMPLE_RPT_0001.xlsx";
      // xlsx.sheetNo = 0;
      xlsx.processExcelSheet(wp);
      // xlsx.sheetNo = 1;
      // xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      wp.showLogMessage("D", "xlsFunction", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "xlsFunction";
      wp.expHandle(ex);
    }

    return;
  } // End of xlsFunction

  /* 產生 PDF 報表 */
  public void pdfFunction(TarokoCommon wr) {
    super.wp = wr;
    try {
      wp.showLogMessage("D", "pdfFunction", "started");

      browseFunction(wp);

      wp.setValue("REPORT_ID", "SAMPLE_RPT_0001", 0);

      TarokoPDF pdf = new TarokoPDF();
      wp.fileMode = "Y";
      pdf.excelTemplate = "SAMPLE_RPT_0001.xlsx";
      pdf.sheetNo = 0;
      pdf.procesPDFreport(wp);
      pdf = null;

      wp.showLogMessage("D", "pdfFunction", "ended");
    }

    catch (Exception ex) {
      wp.expMethod = "pdfFunction";
      wp.expHandle(ex);
    }

    return;
  } // End of pdfFunction

  /* 清畫面 */
  public void clearScreen(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.showLogMessage("D", "clearScreen", "started");

    wp.disabledKey = "N";
    wp.resetInputData();
    wp.resetOutputData();
    showScreen(wp);

    wp.showLogMessage("D", "clearScreen", "ended");
    return;
  }

} // End of class TarokoScreenControl
