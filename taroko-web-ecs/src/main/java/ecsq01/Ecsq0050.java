/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-12-07  V1.00.00  動態 SQL 查詢                                         *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                           *
* 109-06-29  V1.00.03  Zuwei        fix code scan issue
* 110-12-06  V1.00.04  Justin   convert string and bytes through UTF-8 
* 111-01-18  V1.00.05  machao      系统弱掃：Missing Check against Null       *
* 111-01-18  V1.00.06  Justin   force to use UTF8 to read and write files    *
* 111-01-20  V1.00.07  Justin   Missing Check against Null:isEmpty() -> ==null *
* 111-01-20  V1.00.08  Justin   increase the number of data displayed        *
******************************************************************************/

package ecsq01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ecsq0050 extends BaseAction {

  private static final int OUT_LEN = 9000;

@Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;
    wp.showLogMessage("D", "actionFunction", "start");

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "Q":
        queryFunc(); /* 查詢功能 */
        break;
      case "X":
        procFunc(); /* 執行功能 */
        break;
      case "L":
        strAction = ""; /* 清畫面 */
        clearFunc();
        break;
      case "M":
        queryRead(); /* 瀏覽功能 */
        break;
      default:
        break;
    }
    
    initButton();
  }


  public void formatSQLComm() throws Exception {

    String sqlCommand = wp.getParameter("SQL_CMD").trim();
    byte[] inData = sqlCommand.getBytes(StandardCharsets.UTF_8);
    byte[] outData = new byte[OUT_LEN];

    int j = 0;
    for (int i = 0; i < inData.length; i++) {
      if (inData[i] == 0x0D || inData[i] == 0x0A || inData[i] == 0x3B) {
        outData[j] = 0x20;
      } else {
        outData[j] = inData[i];
      }
      j++;
    }

    wp.sqlCmd = new String(outData, 0, j, StandardCharsets.UTF_8);
    wp.showLogMessage("D", "SQL command : ", wp.sqlCmd);
    return;

  } // End of formatSQLComm

  public void createResultHTML() throws Exception {
    wp.showLogMessage("D", "createResultHTML", "started");

    try (FileInputStream fis = new FileInputStream(TarokoParm.getInstance().getRootDir() + "/html/ecsq01/ecsq0050.html");
         FileOutputStream fos = new FileOutputStream(TarokoParm.getInstance().getRootDir() + "/html/ecsq01/ecsq0050_R.html");
         InputStreamReader isr = new InputStreamReader(fis, "UTF8");
         OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
         BufferedReader dr = new BufferedReader(isr);
         BufferedWriter dw = new BufferedWriter(osw);) {

      while (dr.ready()) {
        String inputData = dr.readLine();
        //check: Missing Check against Null 
        if(inputData == null) {
        	break;
        }else if (inputData.indexOf("#TITLE#") != -1) {
          for (int m = 1; m < columnCnt; m++) {
            String outData = inputData.replaceAll("#TITLE#", colName[m]);
            dw.write(outData + wp.newLine);
          }
        } else if (inputData.indexOf("#COLUMN#") != -1) {
          for (int m = 1; m < columnCnt; m++) {
            String outData = "";
            if (strAction.equals("X")) {
              outData = inputData.replaceAll("#COLUMN#", "X");
            } else {
              outData = inputData.replaceAll("#COLUMN#", colName[m]);
            }
            dw.write(outData + wp.newLine);
          }
        } else {
          dw.write(inputData + wp.newLine);
        }
      }
    } finally {
      // releases resources with the stream
      // if (fr != null) {
      // fr.close();
      // }
      // if (fw != null) {
      // fw.close();
      // }
      // if (dr != null) {
      // dr.close();
      // dr = null;
      // }
      // if (dw != null) {
      // dw.flush();
      // dw.close();
      // dw = null;
      // }
    }
    wp.respHtml = "ecsq0050_R";
    wp.showLogMessage("D", "createResultHTML", "ended");

    return;
  } // End of createTemplate

  @Override
  public void queryFunc() throws Exception {

    String lsWhere = "";
    lsWhere = "";

    String sqlCommand = wp.getParameter("SQL_CMD").trim();

    if (sqlCommand.length() <= 6) {
      alertErr2("SQL COMMAND ERROR ");
      return;
    }

    if (!sqlCommand.substring(0, 6).toUpperCase(Locale.TAIWAN).equals("SELECT")) {
      alertErr2("只允許 SELECT COMMAND");
      return;
    }

    formatSQLComm();
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
    return;
  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
    createResultHTML();

  }

  @Override
  public void dddwSelect() {}

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {}

  @Override
  public void clearFunc() {
    wp.setValue("SQL_CMD", "", 0);
    wp.respHtml = "ecsq0050";
  }

  @Override
  public void initPage() {
    wp.respHtml = "ecsq0050";
  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");
  }

  @Override
  public void procFunc() throws Exception {

    wp.showLogMessage("D", "procFunc", "start");

    String resultMessage = "";
    String sqlCommand = wp.getParameter("SQL_CMD").trim();

    if (sqlCommand.length() <= 6) {
      alertErr2("SQL COMMAND ERROR ");
      return;
    }

    String checkCmd = sqlCommand.substring(0, 6).toUpperCase(Locale.TAIWAN);
    if (!Arrays.asList("INSERT", "UPDATE", "DELETE").contains(checkCmd)) {
      alertErr2("只允許 INSERT,UPDATE,DELETE COMMAND");
      return;
    }

    formatSQLComm();
    colName = new String[2];
    if (wp.sqlCmd.toUpperCase(Locale.TAIWAN).indexOf("COMMIT") != -1) {
      wp.commitOnly();
      resultMessage = "COMMIT completed";
    } else if (wp.sqlCmd.toUpperCase(Locale.TAIWAN).indexOf("ROLLBACK") != -1) {
      wp.rollbackOnly();
      resultMessage = "ROLLBACK completed";
    } else {
      sqlExec(wp.sqlCmd);
      int execRows = sqlRowNum;
      if (checkCmd.equals("INSERT")) {
        resultMessage = "INSERT  ROWS : " + execRows;
      } else if (checkCmd.equals("UPDATE")) {
        resultMessage = "UPDATE  ROWS : " + execRows;
      } else if (checkCmd.equals("DELETE")) {
        resultMessage = "DELETE  ROWS : " + execRows;
      }
    }

    columnCnt = 2;
    colName[1] = resultMessage;
    createResultHTML();
    wp.respHtml = "ecsq0050_R";
    wp.showLogMessage("D", "procFunc", "ended");
  }

  @Override
  public void userAction() {}

  @Override
  public void saveFunc() throws Exception {}

} // end of class
