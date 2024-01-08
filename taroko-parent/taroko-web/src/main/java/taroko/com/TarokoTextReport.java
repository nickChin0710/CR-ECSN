/*****************************************************************************
*                   TEXT 報表  樣版檔  處理物件                              *
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  110-01-08  V1.00.02   shiyuqi       修改无意义命名                               
*  111-01-19  V1.00.03   Justin       fix Missing Check against Null          *  
******************************************************************************/
package taroko.com;

import java.io.*;
import java.util.*;

import Dxc.Util.SecurityUtil;

public class TarokoTextReport {

  TarokoCommon wp = null;

  public String textTemplate = ""; // TEXT 樣版檔
  public String reportFile = ""; // TEXT 輸出檔名
  public int pageCount = 19; // 每頁資料筆數
  public int dataCount = 0;

  int templateCount = 0, browsePoint = 0, multiDataRow = 0, pageNo = 0, trailerPoint = 0,
      trailerCnt = 0, printCnt = 0;
  String[] bufferData = new String[200];
  String initFlag = "Y", newLine = "", spaces = "";

  byte[] carriage = {0x0D, 0x0A, 0x00};
  boolean changePage = false;

  BufferedWriter dw = null;

  public void genTextReport(TarokoCommon wp) throws Exception {

    this.wp = wp;
    wp.setValue("SYS_DATE", wp.dispDate, 0);
    wp.setValue("SYS_TIME", wp.dispTime, 0);
    wp.setValue("REPORT_ID", "CYC_0100", 0);

    wp.dateTime();
    newLine = new String(carriage, 0, 2);
    for (int i = 0; i < 10; i++) {
      spaces = spaces + "          ";
    }

    loadTemplate();

    String reportOutPath = System.getenv("PROJ_HOME") + "/text_report";
    String filename = reportOutPath + "/" + reportFile;
	 // verify path
    filename = SecurityUtil.verifyPath(filename);
//    dw = new BufferedWriter(new FileWriter(reportOutPath + "/" + reportFile));
    dw = new BufferedWriter(new FileWriter(filename));

    for (int i = 0; i < dataCount; i++) {
      processPageDetail(i);
    }

    dw.close();
  }

  public void loadTemplate() throws Exception {

    String templatePath = System.getenv("PROJ_HOME") + "/text_template";
    String templateFile = templatePath + "/" + textTemplate;
	 // verify path
    templateFile = SecurityUtil.verifyPath(templateFile);
    try (BufferedReader dr = new BufferedReader(new FileReader(templateFile))) {
      int i = 0;

      while (dr.ready()) {
        String inputStr = dr.readLine();
        if (inputStr != null ) {
        	bufferData[i] = inputStr;
            if (inputStr.length() > 0 && inputStr.charAt(0) == 'L') {
              if (browsePoint == 0) {
                browsePoint = i;
              }
              multiDataRow++;
            }

            if (inputStr.length() > 0 && inputStr.charAt(0) == 'T') {
              if (trailerPoint == 0) {
                trailerPoint = i;
              }
              trailerCnt++;
            }
            i++;
		}
      } // while

      templateCount = i;
    } finally {
    }
    return;

  }

  public void processPageDetail(int cnt) throws Exception {

    String outData = "";

    printCnt++;
    if (multiDataRow == 0 || cnt == 0 || printCnt > pageCount) {
      pageNo++;
      wp.setValue("PAGE_NO", "" + pageNo, 0);
      changePage = true;
    }

    if (changePage) {
      for (int j = 0; j < browsePoint; j++) {
        if (multiDataRow == 0) {
          outData = processBufferData(cnt, j);
        } else {
          outData = processBufferData(0, j);
        }
        writeOutput(outData);
        changePage = false;
      }
    }

    for (int k = 0; k < multiDataRow; k++) {
      int n = browsePoint + k;
      outData = processBufferData(cnt, n);
      writeOutput(outData);
    }

    if (cnt == (dataCount - 1) || printCnt == pageCount) {

      for (int k = 0; k < trailerCnt; k++) {
        int n = trailerPoint + k;
        outData = processBufferData(cnt, n);
        writeOutput(outData);
      }
    }

    if (printCnt > pageCount) {
      printCnt = 0;
    }

    return;
  }

  public void writeOutput(String outData) throws Exception {

    if (outData.length() > 0) {
      outData = outData.substring(1);
    }
    dw.write(outData + newLine);
    dw.flush();
    return;

  }

  public String processBufferData(int i, int j) throws Exception {

    String[] fieldName = new String[50];

    int int1 = 0;
    String inBuffer = bufferData[j];
    while (true) {
      int pnt1 = inBuffer.indexOf("{");
      int pnt2 = inBuffer.indexOf("}");

      if (pnt1 == -1 || pnt2 == -1) {
        break;
      }
      fieldName[int1++] = inBuffer.substring(pnt1 + 1, pnt2).toUpperCase();
      inBuffer = inBuffer.substring(pnt2 + 1, inBuffer.length());
    }

    String cvtData = bufferData[j];
    for (int m = 0; m < int1; m++) {

      String replaceValue = wp.getValue(fieldName[m], i);
      String replaceName = "{" + fieldName[m] + "}";

      int pnt = cvtData.indexOf("{");
      int len = replaceValue.getBytes().length;
      int int2 = replaceValue.getBytes().length - (fieldName[m].length() + 2);

      if (int2 < 0) {
        replaceValue = spaces.substring(0, int2 * -1) + replaceValue;
      } else if (int2 > 0) {
        int startPnt = pnt - int2;
        if (startPnt < 0) {
          startPnt = 0;
        }
        replaceName = cvtData.substring(startPnt, pnt) + replaceName;
      }
      cvtData = cvtData.replace(replaceName, replaceValue);

    }

    return cvtData;
  }

} // End of class TextReport
