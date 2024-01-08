/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-21  V1.00.02  Zuwei       code format                              *
*  109-07-13  V1.00.03  Justin        ++ adSysName, adEcsLogin -> adLogin    * 
*  109-07-22  V1.00.04  JUSTIN     ++adDomainName, remove adSysName          *                                                           *  
*  109-07-24  V1.00.01  Zuwei       coding standard                          *
*  109-08-03  V1.00.01  Zuwei       fix code scan issue                       *
*  109-09-04  V1.00.01  yanghan     解決Portability Flaw: Locale Dependent Comparison問題    * 
*  109/09/05  V1.00.06    yanghan     fix code scan issue    
*  109-09-21  V1.00.07  Zuwei  replace logger with MaskLogger
*  109-09-21  V1.00.08  JustinWu add the instance of MaskLogger
*  109-09-28  V1.00.09  Zuwei       fix code scan issue      *
*  109-10-06   V1.00.10  JustinWu   fix the compatibility of HTTP and HTTPS  
*  109-10-13  V1.00.11  Zuwei       解決sql異常時輸出sql到瀏覽器問題      *
*  109-10-14  V1.00.01  Zuwei       字串”系統日志”改為”系統日誌”      *
*  109-10-28  V1.00.12  JustinWu   add initialLog and LOG_ENCRYPTED
*  110-01-05  V1.00.13  JustinWu   updated for XSS
*  110-01-08  V1.00.14  shiyuqi       修改无意义命名
*  110-01-19  V1.00.15  JustinWu   change a variable name
*  110-01-26  V1.00.16  JustinWu   fix the bug causing the error 
*  110-02-19  V1.00.17  JustinWu   fix the error which cannot display TarokoError.html
*  110-03-08  V1.00.18  JustinWu   initialize a PDPA logger
*  110-03-09  V1.00.19  JustinWu   modified for session managements
*  110-04-13  V1.00.20  JustinWu   add system path
*  110-08-17  V1.00.21  JustinWu   indexOf -> startsWith
*  110-10-28  V1.00.22  JustinWu   add getEcsAcdpPath
*  110-12-23  V1.00.23  JustinWu   log4j1 -> log4j2 and delete and compress logs by log4j2
*  110-12-24  V1.00.24  JustinWu   add showPDPALog()
*  111-01-17  V1.00.25  JustinWu   logger -> getNormalLogger()
*  111-01-18  V1.00.26  JustinWu   fix Erroneous String Compare              *   
*  111-01-19  V1.00.27  JustinWu   fix Unchecked Return Value 
*  111-02-07  V1.00.28  JustinWu   fix Redundant Null Check
*  111-02-08  V1.00.29  JustinWu   add the logErr function                   *
*  111-02-25  V1.00.30  JustinWu   修改port 80 輸出檔案錯誤                  *
******************************************************************************/
package taroko.com;


import java.io.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import Dxc.Util.MaskLogger;
import Dxc.Util.SecurityUtil;
import taroko.base.Base64;
import taroko.base.BaseData;
import taroko.base.CommString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoCommon extends BaseData {

  public BufferedWriter fw;
  public ByteArrayOutputStream ba;

  public String packageDir = "", respCode = "", hideData = "";

  public String[] hideField = new String[24];

  public String[] dropDownKey = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", "", "", ""};

  public String actionCode = "";
  public String levelCode = "", buttonCode = "";
  public String requHtml = "", respHtml = "", packageName = "", javaName = "", methodName = "",
      programPackage = "";

  public String sumField = "", authData = "";
  public String linkMode = "", funCode = "", disabledKey = "", saveKey = "";
  public String showHtml = "", fileMode = "Y", exportSrc = "", exportPdf = "", exportXls = "", exportFile = "";
  public String pageButton = "", rowButton = "", ajaxFlag = "", pluginFile = "";
  public String respMesg = "", errField = "", errMesg = "", alertMesg = "";
  public String jsonCode = "", jsonComa = "", responseJson = "", checkJson = "";
  public String menuCode = "", menuDesc = "";
  public String tabsName = "", csrfValue = "", browserType = "", reportId = "";
  public String ajaxInfo = "", downloadFlag = "";
  public String sessionKey = "";

  boolean showLogon = false;

  public int totalPage = 0, totalRows = 0, pageRows = 1, currPage = 1, sumLine = 0;
  public int firstRow = 0, currRows = 0, varRows = 0, dropDownPnt = 0;
  
  public String unionFlag = "";

  public String[] inputName = {""}; // new String[360];
  public String[] insetValue = {""};

  public int[] listCount = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

  public String sessionId = "";
  public String whereStr = "", queryWhere = "", orderField = "", svWhere = "", svFlag = "";
  public String initFlag = "", funType = "", dispMesg = "", serNum = "", optionKey = "", initOption = "";
  public String expMethod = "", daoTable = "", reportNo = "", descField = "";
  public String linkURL = "", dupRecord = "", deleteNotFound = "";
  public String errInput = "", errCode = "", comTable = "", firstBrowse = "", secondBrowse = "", errSql = "";
  public String sqlCmd = "", selectSQL = "", updateSQL = "", insertSQL = "", pageSQL1 = "", pageSQL2 = "",
			rowSQL = "", specialSQL = "";
  public int insertCnt = 0, updateCnt = 0, deleteCnt = 0;

  public int levelNum = 0, db = 0, queryKeyCnt = 0, stepCount = 0;

  public boolean errorInput = false, autoJSON = false, comSelect = false, notFoundMesg = false;
  public boolean showSecondQuery = false, secondQuery = false, pluginHtml = false, userTag = false;

  public boolean specialFunc = false;

  public StringBuffer menuBuf = null;
  public StringBuffer htmlBuf = null;

  /* PDF variable ended */

  public PrintWriter out = null;
  public String whereOrder = ""; // Query order by-
  public String pageCountSql = ""; // 算select總筆數-
  public String queryReason = ""; // top.查詢理由-
  
  // -#JAVA-convert-
  public String cvtJava = "", cvtMethod = "", orgField = "";
  public int userTagRr = 0;

  public HttpServletRequest request = null;
  public HttpSession session = null;

  public boolean logSql = false;
  private String pgmVersion = "";


  public boolean localHost() {

    if (request.getServerName().indexOf("192.168.") == 0)
      return true;
    if (request.getServerName().indexOf("127.0.0.") == 0)
      return true;

    return false; 
  }

  public int itemRows(String col) {
    String[] buff = this.itemBuff(col);
    if (buff == null) {
      return 0;
    }
    if (buff.length > 1)
      return buff.length;

    if (Arrays.equals(NULL_PARAMETER, buff)) {
      return 0;
    }
    return 1;
  }

  /* INPUT 控制處理 */
  public boolean inputControl() throws Exception {
    try {
      showLogon = true;
      if (Arrays.asList(inputName).contains("AJAX_JASON")) {
        decodeJASON(getInBuffer("AJAX_JASON")[0]);
        hideData = getParameter("HIDE");
        showLogon = false;
      }
      if (Arrays.asList(inputName).contains("HIDE")) {
        hideData = getParameter("HIDE");
        showLogon = false;
      }

      splitHideData();
      if (Arrays.asList("S", "U", "D").contains(actionCode)) {
        queryKeyCnt = 1;
      }

      if (actionCode.equalsIgnoreCase("AJAX")) {
        this.ajaxFlag = "Y";
        if (!Arrays.asList("Y", "O", "US", "UP").contains(menuCode)) {
          menuCode = "S3";
        }
      }

      int cnt = 0, pnt = 0;
      String[] dir = {"", "", "", "", "", ""};
      String tmpString = packageName;

      while (true) {
        pnt = tmpString.indexOf(".");
        if (pnt == -1) {
          dir[cnt] = tmpString;
          cnt++;
          break;
        }
        dir[cnt] = tmpString.substring(0, pnt);
        tmpString = tmpString.substring(pnt + 1);
        cnt++;
      }

      for (int i = 0; i < cnt; i++) {
        packageDir = packageDir + dir[i] + "/";
      }

      // package, program method MAPPING 處理
      if (!Arrays.asList("S").contains(menuCode)) /* "S" : 點選功能表 MainControl 控制 */
      {
        boolean result = programControl();
        if (result == false) {
			return false;
		}
      }

      errField = "";
      respMesg = "";
      pageButton = "N";
      rowButton = "N";

      buttonCode = actionCode;
      if (buttonCode.equalsIgnoreCase("M")) {
        showSecondQuery = true;
      }
      if (buttonCode.equalsIgnoreCase("B")) {
        buttonCode = "M";
      }

      if (!requHtml.equalsIgnoreCase(respHtml)) {
        secondQuery = true;
      }

      if (respHtml.length() < 3) {
        respHtml = requHtml;
      }

      if (methodName.equalsIgnoreCase("clearScreen") || methodName.equalsIgnoreCase("showScreen")
          || buttonCode.equalsIgnoreCase("L")) {
        initFlag = "Y";
        respHtml = requHtml;
      }

    } catch (Exception ex) {
      expMethod = "inputControl";
      expHandle(ex);
    }
    return true;
  }

  public void setQueryMode() throws Exception {
    actionCode = "Q";
    firstBrowse = "Y";
    svFlag = "Y";
    totalPage = 0;
    totalRows = 0;
    currPage = 0;
    currRows = 0;
    firstRow = 0; // -2017-0809-
    return;
  }

  public void setNonBrowse() throws Exception {
    actionCode = "";
    return;
  }

  public void setDetailMode() throws Exception {
    actionCode = "W";
    return;
  }

  public void setDownload(String downloadFile) throws Exception {
    try (FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(TarokoParm.getInstance().getWorkDir() + downloadFile));
        DataInputStream dis = new DataInputStream(fis);) {
      int fileSize = fis.available();
      byte[] inData = new byte[fileSize];
      dis.readFully(inData);
      ba = new ByteArrayOutputStream();
      ba.write(inData, 0, fileSize);
      exportFile = downloadFile;
      exportSrc = "Y";
    } finally {
    }
    return;
  }

  public void setDownload2(String downloadFile) throws Exception {

    linkMode = "Y";
    
    linkURL = getWorkPath(downloadFile);
    
    downloadFlag = "Y";
    return;
  }

  /**
   * 確認port是否顯示在url上。如果使用http連線且使用port 80連線 或是 使用https連線且使用port 443連線，url上不會顯示port
   * @return port是否顯示在url上
   */
	public boolean isPortInUrl() {
		return ("http".equalsIgnoreCase(request.getScheme()) && request.getServerPort() != 80)
				|| ("https".equalsIgnoreCase(request.getScheme()) && request.getServerPort() != 443);
	}
	
	public String getWorkPath(String fileName) {
		String url = "";
	      if (isPortInUrl()) {
	    	  url = linkURL =
	          		request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/WebData/work/"
	                  + fileName;
	      } else {
	    	  url = request.getScheme() + "://" + request.getServerName() + request.getContextPath() + "/WebData/work/" + fileName;

	      }
	      return url;
	}

  public void processWhere() {
    try {
      if (getInBuffer("SEC_BROWSE")[0].equals("Y")) {
        whereStr = pageSQL2;
      } else {
        whereStr = pageSQL1;
      }
      if (whereStr.length() < 5) {
        whereStr = "";
      }
      // queryWhere = rowSQL;
    } catch (Exception ex) {
      expMethod = "processWhere";
      expHandle(ex);
    }

    return;
  }


  public void decodeJASON(String requestJson) throws Exception {
    jsonCode = "Y";

    requestJson = requestJson.replaceAll("\n", "\\\\n");

    JSONObject outerObj = new JSONObject(requestJson);
    String[] outNames = JSONObject.getNames(outerObj);
    for (String elmName : outNames) {
      try {
        String value = outerObj.getString(elmName);
        value =
            value.replaceAll("@~", "\"").replaceAll("~;", "%").replaceAll(";-", "&")
                .replaceAll("@=", "#");
        setAjaxData(elmName, value);
        // showLogMessage("D","JSON-1 : "+elmName,value);
      } catch (Exception ex) {
        subArray(outerObj, elmName);
      }
    }
  }

  public void subArray(JSONObject outerObj, String parmName) throws Exception {
    // showLogMessage("D","JSON-2 : ",parmName);
    JSONArray jsonArray = outerObj.getJSONArray(parmName);
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject obj = jsonArray.getJSONObject(i);
      String[] inNames = JSONObject.getNames(obj);
      for (String elmName : inNames) {
        String value = obj.getString(elmName);
        value =
            value.replaceAll("@~", "\"").replaceAll("~;", "%").replaceAll(";-", "&")
                .replaceAll("@=", "#");
        setAjaxData(elmName, value);
        // showLogMessage("D","JSON-3 : "+elmName,value);
      }
    }
  }

  public void resetJSON() throws Exception {
    responseJson = "";
    return;
  } // resetJSON

  public void addJSONLabel(String jsonField, String jsonValue) throws Exception {
    addJSON(("@_" + jsonField), jsonValue);
    return;
  } // addJSONLabel

  public void addJSON(String jsonField, String jsonValue) throws Exception {

    jsonCode = "Y";
    jsonField = convertField(jsonField);
    jsonValue = convertFormat(jsonField, jsonValue);
    jsonValue =
        jsonValue.replaceAll("\"", "@~").replaceAll("%", "~;").replaceAll("&", ";-")
            .replaceAll("#", "@=");
    String comName = jsonField;

    int n = checkJson.indexOf(comName);
    if (n == -1) {
      responseJson = responseJson + jsonComa + "\"" + comName + "\":\"" + jsonValue + "\"";
    } else {
      responseJson = responseJson + "},{\"" + comName + "\":\"" + jsonValue + "\"";
      checkJson = "";
    }
    checkJson = checkJson + "@" + comName;
    jsonComa = ",";

    return;
  } // addJSON

  public String convertField(String cvtField) throws Exception {
    descField = "";
    if (cvtField.indexOf("#") != -1) {
      if (cvtField.indexOf("#YMD:") != -1) {
        descField = "YMD";
        cvtField = cvtField.substring(5);
      } else if (cvtField.indexOf("#YM:") != -1) {
        descField = "YM";
        cvtField = cvtField.substring(4);
      } else if (cvtField.indexOf("#TIME:") != -1) {
        descField = "TIME";
        cvtField = cvtField.substring(6);
      } else if (cvtField.indexOf("#AMT:") != -1) {
        descField = "AMT";
        cvtField = cvtField.substring(5);
      } else if (cvtField.indexOf("#JAVA:") != -1) {
        descField = "JAVA";
        userTag = true;
      } else if (cvtField.indexOf("#JAVA2:") != -1) {
        descField = "JAVA2";
        userTag = true;
      } else if (cvtField.indexOf("#PLUGIN:") != -1) { // PLUGIN 為 同 一網頁為其他網頁共用
        descField = "PLUGIN";
        userTag = true;
      } else if (cvtField.indexOf("#SQL-TT:") != -1) {
        descField = "SQL";
        userTag = true;
      } else if (cvtField.indexOf("#DECODE:") != -1) {
        descField = "DECODE";
        userTag = true;
      }

      return cvtField;
    }

    if (cvtField.indexOf(".") != -1) {
      if (cvtField.indexOf(".YMD") > 0) {
        descField = "YMD";
        cvtField = cvtField.substring(0, cvtField.length() - 4);
      } else if (cvtField.indexOf(".TIME") > 0) {
        descField = "TIME";
        cvtField = cvtField.substring(0, cvtField.length() - 5);
      } else if (cvtField.indexOf(".(999)") > 0) {
        descField = "(999)";
        cvtField = cvtField.substring(0, cvtField.length() - 6);
      } else if (cvtField.indexOf(".(999.00)") > 0) {
        descField = "(999.00)";
        cvtField = cvtField.substring(0, cvtField.length() - 9);
      } else if (cvtField.indexOf(".(999.000)") > 0) {
        descField = "(999.000)";
        cvtField = cvtField.substring(0, cvtField.length() - 10);
      } else if (cvtField.indexOf(".(999.0000)") > 0) {
        descField = "(999.0000)";
        cvtField = cvtField.substring(0, cvtField.length() - 11);
      }
    }
    return cvtField;
  }

  String nvl(String str1, String str2) {
    if (str1 == null || str1.length() == 0)
      return str2.trim();
    return str1.trim();
  }

  public String convertFormat(String cvtField, String cvtValue) throws Exception {

    cvtValue = nvl(cvtValue, "");
    
    if (! ("SYS_SCRIPT".equals(cvtField)  || cvtValue.startsWith("<option")) ) {
    	cvtValue = replaceSpecChar(cvtValue);
	}
    
    if (userTag == false) {
      if (cvtValue.length() == 0)
        return "";
    }
    if (descField.length() == 0) {
      return cvtValue;
    }

    if (descField.equalsIgnoreCase("YMD")) {
      if (cvtValue.trim().length() == 8) {
        cvtValue =
            cvtValue.substring(0, 4) + "/" + cvtValue.substring(4, 6) + "/"
                + cvtValue.substring(6, 8);
      } else if (cvtValue.trim().length() == 6) {
        cvtValue = cvtValue.substring(0, 4) + "/" + cvtValue.substring(4, 6);
      }
    } else if (descField.equalsIgnoreCase("TIME")) {
      if (cvtValue.trim().length() == 6) {
        cvtValue =
            cvtValue.substring(0, 2) + ":" + cvtValue.substring(2, 4) + ":"
                + cvtValue.substring(4, 6);
      } else if (cvtValue.trim().length() == 4) {
        cvtValue = cvtValue.substring(0, 2) + ":" + cvtValue.substring(2, 4);
      }
    } else if (descField.equals("(999)")) {
      cvtValue = cvtValue.replaceAll(",", "");
      if (cvtValue.length() == 0) {
        cvtValue = "0";
      }
      if (isNumber(cvtValue)) {
        cvtValue = String.format("%,14.0f", Double.parseDouble(cvtValue));
      }
    } else if (descField.equals("(999.00)")) {
      cvtValue = cvtValue.replaceAll(",", "");
      if (cvtValue.length() == 0) {
        cvtValue = "0.00";
      } else if (isNumber(cvtValue)) {
        cvtValue = String.format("%,14.2f", Double.parseDouble(cvtValue));
      }
    } else if (descField.equals("JAVA")) {
      int pnt = orgField.indexOf("(");
      if (pnt <= 0) {
        return "";
      }
      String cvmData = orgField.substring(6, pnt);
      String tmpField = orgField.substring(pnt + 1, orgField.length() - 1).toUpperCase();
      int pnt2 = cvmData.lastIndexOf(".");
      cvtMethod = cvmData.substring(pnt2 + 1);
      cvtJava = cvmData.substring(0, pnt2);
      cvtValue = getValue(tmpField, userTagRr);
      if (cvtValue.length() == 0) {
        return "";
      }
      Class<?> boClass = Class.forName(cvtJava);
      Method boMethod = boClass.getMethod(cvtMethod, String.class);
      Object bo = boClass.newInstance();
      cvtValue = (String) boMethod.invoke(bo, cvtValue);
    } else if (descField.equals("JAVA2")) {
      int pnt = orgField.indexOf("(");
      if (pnt <= 0) {
        return "";
      }
      String cvmData = orgField.substring(7, pnt);
      String tmpField = orgField.substring(pnt + 1, orgField.length() - 1).toUpperCase();
      int pnt2 = cvmData.lastIndexOf(".");
      cvtMethod = cvmData.substring(pnt2 + 1);
      cvtJava = cvmData.substring(0, pnt2);
      cvtValue = getValue(tmpField, userTagRr);
      if (cvtValue.length() == 0) {
        return "";
      }
      Class<?> boClass = Class.forName(cvtJava);
      Method boMethod = boClass.getMethod(cvtMethod, java.sql.Connection.class, String.class);
      Object bo = boClass.newInstance();
      cvtValue = (String) boMethod.invoke(bo, getConn(), cvtValue);
    } else if (descField.equals("DECODE")) {
      String[] cvtTmp = orgField.substring(8).split(";");
      if (cvtTmp.length < 2) {
        return "";
      }
      String tmpField = cvtTmp[0];
      for (int i = 1; i < cvtTmp.length - 1; i++) {
        String[] decodeValue = cvtTmp[i].split(",");
        if (getValue(tmpField, userTagRr).equals(decodeValue[0])) {
          return decodeValue[1];
        }
      }

      cvtValue = cvtTmp[cvtTmp.length - 1];
      if (cvtValue.toUpperCase(Locale.TAIWAN).equals("_BLANK")) {
        cvtValue = "";
      }
    } else if (descField.equals("(999.0000)")) {
      cvtValue = cvtValue.replaceAll(",", "");
      if (cvtValue.length() == 0) {
        cvtValue = "0.0000";
      } else if (isNumber(cvtValue)) {
        cvtValue = String.format("%,16.4f", Double.parseDouble(cvtValue));
      }
    } else if (descField.equals("(999.000)")) {
      cvtValue = cvtValue.replaceAll(",", "");
      if (cvtValue.length() == 0) {
        cvtValue = "0.000";
      } else if (isNumber(cvtValue)) {
        cvtValue = String.format("%,15.3f", Double.parseDouble(cvtValue));
      }
    } else if (descField.equals("SQL")) {
      cvtValue = "no-support: Html-SQL"; // TarokoUserTag.processSqlTag(cvtField,this);
    }

    descField = "";
    return cvtValue.trim();
  }

	private String replaceSpecChar(String cvtValue) {
//	  & --> &amp;
//	  < --> &lt;
//	  > --> &gt;
//	  " --> &quot;
//	  ' --> &#x27;
		cvtValue = cvtValue
				.replaceAll("&", "&amp;")
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
				.replaceAll("\"", "&quot;")
				.replaceAll("'", "&#x27;");
		return cvtValue;
	}

/* OUTPUT 控制處理 */
  public void outputControl() {
    try {
      String typeName = "";

      for (int i = 0; i < inputName.length; i++) {
        if (errInput.equals("Y")) {
          insetValue = getInBuffer(inputName[i]);
          for (int k = 0; k < insetValue.length; k++) {
            if (insetValue.length == 1) {
              setValue(inputName[i], insetValue[k], 0);
            } else {
              setValue(inputName[i] + "-" + (k + 1), insetValue[k], 0);
            }
          }
        }

        typeName = getType(inputName[i]);

        String ss = getValue(inputName[i], 0);
        if (typeName.equalsIgnoreCase("CHECKBOX")) {
          if (ss.length() == 0) {
            setValue("DEFAULT-" + inputName[i], "checked", 0);
          } else {
            setValue(inputName[i] + "-" + ss, "checked", 0);
          }
        } else if (typeName.equalsIgnoreCase("RADIO")) {
          if (getValue(inputName[i], 0).length() == 0) {
            setValue("DEFAULT-" + inputName[i], "checked", 0);
          } else {
            setValue(inputName[i] + "-" + ss, "checked", 0);
          }
        } else if (typeName.equalsIgnoreCase("SELECT")) {
          setValue(inputName[i] + "-" + ss, "selected", 0);
        }
      }

      if (initFlag.equalsIgnoreCase("Y")) {
        setValue("DEFAULT_CHK", "checked", 0);
        setValue("DEFAULT_SEL", "selected", 0);
      }

      if (disabledKey.equalsIgnoreCase("Y")) {
        setValue("DISABLE", "disabled='disabled'", 0);
        setValue("DISABLED", "disabled", 0);
        setValue("READONLY", "readonly", 0);
      }

      if ((notFound.equalsIgnoreCase("Y") || notFoundMesg == true) 
	  	&& dispMesg.length() == 0) {
        respCode = "01";
        dispMesg = "查無資料";
        respMesg = dispMesg;
        actionCode = "Q";
      } else if (errorInput) {
        respCode = "88";
        if (errMesg.length() > 0) {
          respMesg = errMesg;
        } else {
          respMesg = "資料錯誤";
        }
      } else if (errCode.equalsIgnoreCase("Y")) {
        respCode = "99";
        if (!empty(errMesg))
          dispMesg += " " + errMesg;
        else if (empty(dispMesg)) {
          dispMesg = "資料錯誤";
        }
      }
      // -OK-
      if (respCode.equalsIgnoreCase("00") && empty(respMesg)) {
        if (this.dispMesg.length() > 0) {
          respMesg = dispMesg;
        } else {
          switch (buttonCode) {
            case "A":
              respMesg = "新增完成";
              break;
            case "U":
              respMesg = "修改完成";
              break;
            case "D":
              respMesg = "刪除完成";
              break;
            case "C":
              respMesg = "確認完成";
              break;
            default:
              break;
          }
        }
      }

      // setValue("userId", this.loginUser, 0);

      requHtml = respHtml;

      createHideData();

      if (jsonCode.equalsIgnoreCase("Y")) {
        if (errField.length() > 0) {
          addJSON("ERR_FIELD", errField);
        }

        addJSON("RC_CODE", respCode);
        addJSON("RC_MESG", respMesg);
        responseJson = "{'ajaxInfo':[{" + responseJson + "}]}";
        setValue("AJAX_RESP_DATA", responseJson, 0);
      }

    } catch (Exception ex) {
      expMethod = "outputControl";
      expHandle(ex);
    }

    return;
  }

  public void resetInputData() {
    try {
      inputHash.clear();
    } catch (Exception ex) {
      expMethod = "resetInputData";
      expHandle(ex);
    }

  }

  public void resetOutputData() throws Exception {
    outputHash.clear();
    return;
  }

  public float durationTime(String parmStart, String methodNme) throws Exception {
    long startTime = 0, endTime = 0;
    startTime =
        Integer.parseInt(parmStart.substring(0, 2)) * 3600000
            + Integer.parseInt(parmStart.substring(2, 4)) * 60000
            + Integer.parseInt(parmStart.substring(4, 6)) * 1000
            + Integer.parseInt(parmStart.substring(6, 9));

    dateTime();
    endTime =
        Integer.parseInt(sysTime.substring(0, 2)) * 3600000
            + Integer.parseInt(sysTime.substring(2, 4)) * 60000
            + Integer.parseInt(sysTime.substring(4, 6)) * 1000 
			+ Integer.parseInt(millSecond);

    float durTime = (float) (endTime - startTime) / 1000;
    if (durTime >= TarokoParm.getInstance().getWarningSec()) {
      showLogMessage("W", javaName + "." + methodNme + " DURATION TIME : " + durTime + " SECONDS",
          "");
      showLogMessage("W", "Active TOO-LONG : " + sqlCmd, "");
      // showLogMessage("W", "TIME-OUT SQL : " + sqlCmd, "");
    }
    return durTime;
  }

  void splitHideData() {
    try {
      int i = 0, pnt = 0;

      if (hideData.length() < 3) {
        initHideData();
        createHideData();
      }

      byte[] b64Data = Base64.decode(hideData);
      String hideData = new String(b64Data, "UTF-8");

      hideField = hideData.split("#");

      loginUser = hideField[0];
      menuCode = hideField[1];
      menuSeq = hideField[2]; // 功能作業代碼
      requHtml = hideField[3];
      respHtml = hideField[4];
      levelCode = hideField[5];
      tabsName = hideField[6];
      csrfValue = hideField[7];
      actionCode = hideField[8];
      funCode = hideField[9];
      pageButton = hideField[10];
      respCode = hideField[11];
      try {
        totalPage = Integer.parseInt(hideField[12]);
      } catch (Exception ex) {
        totalPage = 0;
      }
      try {
        totalRows = Integer.parseInt(hideField[13]);
      } catch (Exception ex) {
        totalRows = 0;
      }
      try {
        pageRows = Integer.parseInt(hideField[14]);
      } catch (Exception ex) {
        pageRows = 0;
      }
      try {
        currPage = Integer.parseInt(hideField[15]);
      } catch (Exception ex) {
        currPage = 0;
      }
      try {
        currRows = Integer.parseInt(hideField[16]);
      } catch (Exception ex) {
        currRows = 0;
      }

      browserType = hideField[17];
      menuDesc = hideField[18];
      errField = hideField[19];
      respMesg = hideField[20];
      queryReason = hideField[21];
      loginDeptNo = hideField[22];
      authData = hideField[23];

      try {
        levelNum = Integer.parseInt(levelCode);
      } catch (Exception ex) {
        levelNum = 0;
      }

    } catch (Exception ex) {
      expMethod = "splitHideData";
      expHandle(ex);
    }

  }

  void initHideData() throws Exception {
    initFlag = "Y";
    levelCode = "0";
    actionCode = "N";
    funCode = "N";
    pageButton = "N";
    rowButton = "N";
    respCode = "00";
    totalPage = 0;
    totalRows = 0;
    pageRows = 20;
    currPage = 0;
    currRows = 0; // 1;
    return;
  }

  public void createHideData() throws Exception {
    try {
      showHtml = respHtml;
      
      csrfValue = "" + sysTime.hashCode();

      hideField[0] = loginUser; // 使用者 ID
      hideField[1] = menuCode; // 控制碼 
                               // "S" : 點選功能表 ; "S2" : 變更處理程式; 
                               // "S3" 一般 SUBMIT ; "Y","O","US","UP" : 特殊功能處理;
      hideField[2] = menuSeq; // 程式序號
      hideField[3] = requHtml; // 需求頁面
      hideField[4] = respHtml; // 回覆頁面
      hideField[5] = levelCode; // 處理 LEVEL
      hideField[6] = tabsName; // 處理 TABS_NAME
      hideField[7] = csrfValue; // CSRF VALUE
      hideField[8] = actionCode; // 動作碼
      hideField[9] = funCode; // 瀏覽碼 "FP":第一頁,"PP" : 上一頁,"NP" : 下一頁,"LP" : 最後一頁 , "CP" CURRENT 頁更新
      hideField[10] = pageButton; // 瀏覽 BUTTON 控制
      hideField[11] = respCode; // 回覆碼
      hideField[12] = totalPage + ""; // 總頁數
      hideField[13] = totalRows + ""; // 總筆數
      hideField[14] = pageRows + ""; // 每頁筆數
      hideField[15] = currPage + ""; // CURRENT 頁碼
      hideField[16] = currRows + ""; // CURRENT ROW 碼
      hideField[17] = browserType; // 瀏覽器類別
      hideField[18] = menuDesc; // 抬頭區說明文字
      hideField[19] = errField; // 檢核錯誤說明
      hideField[20] = respMesg; // 回覆訊息
      hideField[21] = queryReason; // 他行特殊需求
      hideField[22] = loginDeptNo; // 他行特殊需求
      hideField[23] = authData; // 授權代碼

      StringBuffer hideBuff = new StringBuffer();
      hideBuff.append(hideField[0]);
      for (int i = 1; i < hideField.length; i++) {
        if (hideField[i].length() == 0) {
          hideField[i] = " ";
        }
        hideBuff.append(("#" + hideField[i]));
      }

      hideData = hideBuff.toString();
      hideData = Base64.encode(hideData.getBytes("UTF-8"));
      setValue("HIDE_DATA", hideData, 0);
      setValue("DISP_MESG", dispMesg, 0);

      if (errCode.equalsIgnoreCase("Y")) {
        respHtml = "TarokoError";
        setValue("DISP_MESG", "程式處理錯誤 " + dispMesg, 0);
      }
      setValue("pgm_version", pgmVersion, 0);
    } catch (Exception ex) {
      expMethod = "createHideData";
      expHandle(ex);
    }

    return;
  }

  // 取得 session 資料
  public boolean getSessionData() throws Exception {
    try {
      String serverCsrf = "";
      sessionKey = loginUser + "#" + tabsName + "#";

      String sessionValue = (String) session.getValue(sessionKey);
      if (sessionValue == null) {
        showLogMessage("E", "", "getSessionData error " + sessionKey);
        return false;
      }
      String[] cvtData = sessionValue.split("#");
      packageName = cvtData[0];
      javaName = cvtData[1];
      methodName = cvtData[2];
      authData = cvtData[3];
      serverCsrf = cvtData[4];
      pageSQL1    = cvtData[5];
      // 比較 client 端與 server 端 CSRF 值
      if (!serverCsrf.equals(csrfValue)) {
//        showLogMessage("E", "", "CSRF CHECK ERROR server " + serverCsrf + " client : " + csrfValue);
        // throw new Exception();
//        return false;
      }
      programPackage = packageName;
    }

    catch (Exception ex) {
      expMethod = "getSessionData";
      expHandle(ex);
    }

    return true;
  }

  // 設定 session 資料
  public void setSessionData() throws Exception {
    try {
      sessionKey = loginUser + "#" + tabsName + "#";

      String serverCsrf = "" + sysTime.hashCode(); // 產生 CSRF 值
      String sessionValue = packageName + "#" 
	  		  + javaName + "#" 
			  + methodName + "#" 
			  + authData + "#" 
			  + csrfValue + "#"
			  + pageSQL1+"#"
              + rowSQL+"#NULL#";
      session.putValue(sessionKey, sessionValue);
    }

    catch (Exception ex) {
      expMethod = "setSessionData";
      expHandle(ex);
    }

    return;
  }

  // package, program method MAPPING 處理
  public boolean programControl() throws Exception {

    if (showLogon) { // 顯示 ECS LOGIN
			packageName = "taroko.com";
			javaName = "EcsLogin";
			requHtml = "ecs_login"; 
			methodName = "showScreen";
    } else if (Arrays.asList("Y", "O", "US", "UP").contains(menuCode)) { // 特殊功能處理
      specialFunc = true;
      packageName = "taroko.com";
      switch (menuCode) {
        case "Y": // ajax 取 URL hostname,port,ap name
          javaName = "TarokoApplControl";
          methodName = "getSystemUrl";
          break; 
        case "O": // 執行 登出
          javaName = "TarokoApplControl";
          methodName = "logout";
          break; 
        case "US": // POPUP 顯示 上傳檔案畫面
          javaName = "TarokoUpload";
          requHtml = "TarokoUpload";
          methodName = "showScreen";
          break; 
        case "UP": // 執行 上傳檔案
          javaName = "TarokoUpload";
          requHtml = "TarokoUpload";
          methodName = "actionFunction";
          break; 
        default:
          break;
      }
    } else if (menuSeq.length() >= 4) {
      // 取得 session 資料
      if (!Arrays.asList("S2").contains(menuCode)) {
        boolean result = getSessionData();
        if (result == false) {
			return false;
		}
      }

      if (Arrays.asList("S2", "S3").contains(menuCode)) // 功能 BUTTON submit 功能
      {
        methodName = "actionFunction";
      } // 現行規範固定執行之 method
    }

    String loginParmCode = getParameter("DXC_LOGIN");
    if (loginParmCode.length() > 0 ) { // DXC LOGIN 
      packageName = "taroko.com";
      javaName = "EcsLogin";
      if (loginParmCode.equals("TREE")){
    	// 2020/04/20 方法名規範化修改 ecs_loginTree -> ecsLoginTree
    	// 處理左邊功能選單
        methodName = "ecsLoginTree";
      } 
      else if (loginParmCode.equals("MAIN")) {
    	// 處理右邊主要功能區
        methodName = "firstLink";
      } 
      else if (loginParmCode.equals("FIRST")) {
    	// 處理主要功能區第一個 LINK
        methodName = "showScreen";
      } 
      else{
    	// 2020/04/20 方法名規範化修改 ecs_mainTree -> ecsMainTree
    	// 處理系統主頁面 LINK 左邊功能選單,右邊主要功能區
        methodName = "ecsMainTree";
      } // 處理系統主頁面 LINK 左邊功能選單,右邊主要功能區

      if (!itemEmpty("LOGIN_USER")) {
        loginUser = itemStr("LOGIN_USER").toUpperCase();
      }
    }
    return true;
  } // end of programControl

  // 瀏覽 BUTTON 處理
  public void pageControl() {
    // -no-page-
    if (this.pageRows >= 999) {
      return;
    }
    try {
    if (!actionCode.equalsIgnoreCase("Q") && !secondBrowse.equalsIgnoreCase("Y")) {
         processWhere();
       }

      totalPage = totalRows / pageRows;
      if ((totalRows % pageRows) != 0) {
        totalPage++;
      }

      if (currPage > totalPage) {
        currPage = totalPage;
      }

      if (funCode.equalsIgnoreCase("FP") || currPage <= 0) {
        currPage = 1;
      }

      if (funCode.equalsIgnoreCase("NP")) {
        if ((currPage + 1) <= totalPage) {
          currPage++;
        }
      }

      if (funCode.equalsIgnoreCase("PP")) {
        if ((currPage - 1) > 0) {
          currPage--;
        }
      }

      if (funCode.equalsIgnoreCase("LP")) {
        currPage = totalPage;
      }

      firstRow = ((currPage - 1) * pageRows);
      if (firstRow < 0) {
        firstRow = 0;
      }

      if (firstRow > totalRows) {
        firstRow = totalRows;
      }

      funCode = "CP";
      actionCode = "B";
    } catch (Exception ex) {
      expMethod = "pageControl";
      expHandle(ex);
    }

  }

  public void setPageValue() throws Exception {
    if (this.pageRows >= 999) {
      return;
    }
    totalPage = totalRows / pageRows;
    if ((totalRows % pageRows) != 0) {
      totalPage++;
    }
    actionCode = "B";
    return;
  }

  public void pgmVersion(String aVer) {

    pgmVersion = aVer;
  }

  public void javascript(String aScript) {
    if (empty(aScript))
      return;

    addAlert(aScript);
  }

  public void setAlert(String fieldName, String errorMesg) {
    try {
      errorInput = true;
      comSelect = true;

      if (fieldName.length() > 0) {
        if (errField.length() == 0) {
          errField = fieldName.trim().toUpperCase();
        } else {
          errField = errField + "," + fieldName.trim().toUpperCase();
        }
      }
      comTable = daoTable.trim();
      if (jsonCode.equalsIgnoreCase("Y")) {
        errMesg = "資料錯誤**" + errorMesg;
      } else {
        addAlert("alert('" + errorMesg + "');");
        errMesg = "資料錯誤: " + errorMesg;
      }
      if (this.respHtml.toLowerCase().indexOf("tarokoerror") >= 0) {
        dispMesg += " " + errMesg;
      } else
        dispMesg = errMesg;
    } catch (Exception ex) {
      expMethod = "setError";
      expHandle(ex);
    }
    return;
  }

  public void alertMesg(String mesg) {
    try {
      if (jsonCode.equalsIgnoreCase("Y")) {
        errMesg = "資料錯誤**" + mesg;
      } else {
        addAlert("alert('" + mesg + "');");
        errMesg = mesg;
      }
    } catch (Exception ex) {
      expMethod = "alertMesg";
      expHandle(ex);
    }
  }

  private void addAlert(String str) {
    if (alertMesg.indexOf(str) >= 0)
      return;
    alertMesg += "<script language='javascript'> " + str + " </script>" + newLine;
  }

  public void setError(String fieldName, String errorMesg) {
    try {
      errorInput = true;
      comSelect = true;

      if (errField.length() == 0) {
        errField = fieldName.trim().toUpperCase();
      } else {
        errField = errField + "," + fieldName.trim().toUpperCase();
      }

      comTable = daoTable.trim();
      errMesg = errMesg + " " + errorMesg;
    } catch (Exception ex) {
      expMethod = "setError";
      expHandle(ex);
    }
    return;
  }

  public void setAjaxData(String fieldName, String parmValue) {
    try {
      String[] ajaxData = getInBuffer(fieldName);
      if (nullCheck) {
        setParameter(fieldName, parmValue);
        setValue(fieldName, getParameter(fieldName), 0);
        return;
      }

      String[] newData = new String[ajaxData.length + 1];
      for (int i = 0; i < ajaxData.length; i++) {
        newData[i] = ajaxData[i];
      }
      newData[ajaxData.length] = parmValue;
      setInBuffer(fieldName.trim().toUpperCase(), newData);
    } catch (Exception ex) {
      expMethod = "setAjaxData";
      expHandle(ex);
    }
    return;
  }

  /* 動態選單處理 first level */
  public void setListCount(int idx) throws Exception {
    if (idx == 0) {
      listCount[idx] = selectCnt + sumLine;
    } else {
      listCount[idx - 1] = selectCnt + sumLine;
    }

  }

  public void setListCount(int idx, String colID) {
    if (idx > 0)
      idx = idx - 1;
    setListSernum(idx, colID + "ser_num");
  }

  public void setListSernum(int idx, String col, int aListRow) {
    if (empty(col)) {
      col = "SER_NUM";
    }
    listCount[idx] = aListRow; // + sumLine;

    int liSerNum = this.firstRow;
    for (int ii = 0; ii < aListRow; ii++) {
      liSerNum++;
      if (liSerNum < 10) {
        this.setValue(col, "0" + liSerNum, ii);
      } else
        this.setValue(col, "" + liSerNum, ii);
    }
  }

  public void setListSernum(int idx, String col) {
    setListSernum(idx, col, this.selectCnt);
  }


  /* 動態選單處理-2 */
  public void dynamicDropdown(String dynamicName, String fieldName, String optinonName) {
    try {
      dropDownKey[0] = optionKey;
      multiOptionList(0, dynamicName, fieldName, optinonName);
    } catch (Exception ex) {
      expMethod = "dynamicDropdown";
      expHandle(ex);
    }

    return;
  }

  /* 多重 動態選單處理 */
  public void multiOptionList(int cnt, String dynamicName, String fieldName, String optinonName) {
    try {
      String optionString = "", selectValue = "";

      StringBuffer optionBuf = new StringBuffer();
      if (initOption.length() != 0) {
        optionBuf.append("<option value='' >").append(initOption).append("</option>").append(newLine);
      }

      for (int i = 0; i < selectCnt; i++) {
        selectValue = "";
        if (dropDownKey[cnt].equalsIgnoreCase(getValue(fieldName, i))) {
          selectValue = "selected";
          dropDownPnt = i;
        }
        optionBuf.append("<option value='").append(getValue(fieldName, i)).append("' ").append(selectValue).append(" >")
                 .append(getValue(optinonName, i))
                 .append("</option>").append(newLine);
      }

      optionString = optionBuf.toString();
      if (unionFlag.equalsIgnoreCase("Y")) {
        setValue(dynamicName, getValue(dynamicName, 0) + optionString, cnt);
      } else {
        setValue(dynamicName, optionString, cnt);
      }
      unionFlag = "";
    } catch (Exception ex) {
      expMethod = "multiOptionList";
      expHandle(ex);
    }

  }

  public String getType(String fieldName) {
    String retnStr = "";
    try {
      retnStr = (String) inputType.get(fieldName.toUpperCase());
      if (retnStr == null) {
        return nullString;
      }

      retnStr = retnStr.trim();
    } catch (Exception ex) {
      expMethod = "getType";
      expHandle(ex);
    }
    return retnStr;
  }

  public void putType(String fieldName, String setValue) {
    try {
      if (setValue == null) {
        setValue = "";
      }

      setValue = setValue.trim();
      inputType.put(fieldName.toUpperCase(), setValue);
    } catch (Exception ex) {
      expMethod = "putType";
      expHandle(ex);
    }
    return;
  }

  public void commitDataBase() {
    try {
      for (int i = 0; i < TarokoParm.getInstance().getConnCount(); i++) {
        if (conn[i] != null) {
          try {
            conn[i].commit();
          } catch (Exception ex) {
            log("<--->wp.commit err<--->");
          }
          conn[i].close();
        }
      }

      inputHash.clear();
      outputHash.clear();
      inputType.clear();
    } catch (Exception ex) {
      expMethod = "commitDataBase";
      expHandle(ex);
    }
    return;
  }

  public void closeDataBase() {
    try {
      for (int i = 0; i < TarokoParm.getInstance().getConnCount(); i++) {
        if (conn[i] != null) {
          conn[i].close();
        }
      }
    } catch (Exception ex) {
      expMethod = "commitDataBase";
      expHandle(ex);
    }
    return;
  }

  public void rollbackDataBase() {
    try {
      for (int i = 0; i < TarokoParm.getInstance().getConnCount(); i++) {
        if (conn[i] != null) {
          try {
            conn[i].rollback();
          } catch (Exception ex) {
            log("<--->wp.rollback err<--->");
          }
          conn[i].close();
        }
      }
      inputHash.clear();
      outputHash.clear();
      inputType.clear();
    } catch (Exception ex) {
      expHandle("rollbackDataBase", ex);
    }
  }
  
  /**
   * use normal logger if LOG_ENCRYPTED is N, else use MaskLogger
   * @throws IOException
   */
  private void initialLog() throws IOException {
	  initialLogger();
  }

	public void logActive() {
		if (getNormalLogger() == null) {
			try {
				initialLog();
			} catch (IOException e) {
				System.out.println("initial Log4j Error");
			}
		}
	}

  public void logSql(String str, Object... obj) {
    String str1 = str;
    // -no-log sql Statment--
    if (str1 == null || str1.trim().length() == 0) {
      return;
    }
    logActive();
    if (obj == null || obj.length == 0) {
    	getNormalLogger().info(CommString.validateLogData(" > >>>sql>>> " + loginUser + "." + javaName + ":" + str1));
    } else {
    	getNormalLogger().info(CommString.validateLogData(" > >>>sql>>> " + loginUser + "." + javaName + ":"
          + logParm(str, obj, true)));
    }
    return;
  }

  public void logSql2(String str, Object... obj) {
    String str1 = str;
    // -no-log sql Statment--
    if (logSql == false || str1 == null || str1.trim().length() == 0) {
      // dddSql_log =false;
      return;
    }
    // -sql相同不重覆LOG-
    // if (sqlCmd.length()>0 && _ddd_sql.equalsIgnoreCase(sqlCmd))
    // return;
    // _ddd_sql =sqlCmd;
    // dddSql_log =false;
    logActive();
    if (obj == null || obj.length == 0) {
    	getNormalLogger().info(CommString.validateLogData(" > >>>sql>>> " + loginUser + "." + javaName + ":" + str1));
    } else {
    	getNormalLogger().info(CommString.validateLogData(" > >>>sql>>> " + loginUser + "." + javaName + ":"
          + logParm(str, obj, true)));
    }
    return;
  }

  public void log(String str1, Object... obj) {
    String str = str1;
    if (str == null || str.trim().length() == 0) {
      showLogMessage("U", "", "[user message is null or empty]");
      return;
    }
    if (obj == null || obj.length == 0) {
      showLogMessage("U", "", str);
      return;
    }

    showLogMessage("U", "", logParm(str1, obj));
    return;
  }
  
  public void logErr(String str1, Object... obj) {
	    String str = str1;
	    if (str == null || str.trim().length() == 0) {
	      showLogMessage("I", "", "[user message is null or empty]");
	      return;
	    }
	    if (obj == null || obj.length == 0) {
	      showLogMessage("I", "", str);
	      return;
	    }

	    showLogMessage("I", "", logParm(str1, obj));
	    return;
  }

  String logParm(String str, Object[] aObj) {
    return logParm(str, aObj, false);
  }

  String logParm(String str, Object[] aObj, boolean bMark) {
    if (aObj == null || aObj.length == 0)
      return str;

    String str1 = str.replaceAll("\\?", "%s");
    for (int ii = 0; ii < aObj.length; ii++) {
      try {
        if (bMark) {
          str1 = str1.replaceFirst("%s", "'" + aObj[ii].toString() + "'");
        } else {
          str1 = str1.replaceFirst("%s", aObj[ii].toString());
        }
      } catch (Exception ex) {
        if (aObj[ii] == null) {
          str1 = str1.replaceFirst("%s", "NULL");
        } else
          str1 = str1.replaceFirst("%s", "<" + ii + ">") + ";" + aObj[ii].toString();
      }
    }

    return str1;
  }

  @SuppressWarnings("null")
  public void showLogMessage(String actCode, String procMethod, String actionMessage) {

    String stepMesg = "";

    logActive();

    if (!deleteNotFound.equalsIgnoreCase("Y") && !dupRecord.equalsIgnoreCase("Y")) {
      stepCount++;
    }
    if (stepCount == 2000) {
      actionMessage = "$$$$ PROGRAM LOOP $$$$";
    }

    /* LOOP CAUSE Exception */
    // JJJJJ: log 太多會Exception
    if (stepCount > 2000) {
      // String abend = null;
      // if (abend.equalsIgnoreCase("Y")) {
      // ;
      // }
      errCode = "Y";
      return;
    }

    if (stepCount < 10) {
      stepMesg = loginUser + " 0" + stepCount;
    } else {
      stepMesg = loginUser + " " + stepCount;
    }

    if (actCode.equalsIgnoreCase("D") && TarokoParm.getInstance().getDebugMode().equalsIgnoreCase("Y")) {
    	getNormalLogger().debug(CommString.validateLogData("> " + stepMesg + " " + javaName + "." + procMethod + " "
          + actionMessage));
    } else {
      switch (actCode) {
        case "I":
        	getNormalLogger().info(CommString.validateLogData(" > " + stepMesg + " " + javaName + "." + procMethod
              + " " + actionMessage));
          break;
        case "W":
        	getNormalLogger().warn(CommString.validateLogData(" > " + stepMesg + " " + javaName + "." + procMethod
              + " " + actionMessage));
          break;
        case "E":
        	getNormalLogger().error(CommString.validateLogData("> " + stepMesg + " " + javaName + "." + procMethod
              + " " + actionMessage));
          break;
        case "U":
        	getNormalLogger().debug(CommString.validateLogData("> " + actionMessage));
          break;
        case "D":
        	getNormalLogger().debug(CommString.validateLogData("> " + actionMessage));
          break;
        default:
          break;
      }
    }

    return;
  }
  
	public void showPDPALog(String message) {
		Logger pdpaLogger = getPDPALogger();
		if (pdpaLogger == null) {
			try {
				initialLog();
				pdpaLogger = getPDPALogger();
			} catch (IOException e) {
				System.out.println("initial PDPA Log4j Error");
				e.printStackTrace();
			}
		}
		
		if (pdpaLogger != null) {
			if (pdpaLogger instanceof MaskLogger)
				((MaskLogger) pdpaLogger).privacy(message);
			else
				pdpaLogger.info("[PDPA]" + message);
		}

	}

  public void expHandle(String method, Exception ex) {
    this.expMethod = method;
    expHandle(ex);
  }

  public void expHandle(Exception ex) {
    this.rollbackOnly();
    respHtml = "TarokoError";
    packageDir = "taroko/com/";
    String fatalMesg = "";
    if (errCode.equalsIgnoreCase("Y")) {
      return;
    }

    Logger logger = getNormalLogger();
	if (logger == null) {
		try {
			initialLog();
			logger = getNormalLogger();
		} catch (IOException e) {
			System.out.println("initialLog4j Error");
		}
	}
	
	if (logger == null) {
		return;
	}
	
    errCode = "Y";
    respCode = "99";
    respMesg = "處理錯誤";
    logger.fatal(CommString.validateLogData(" >> ####### TarokoException MESSAGE STARTED ######" + newLine));

    dispMesg = "PROGRAM : " + javaName + " METHOD : " + expMethod + "  系統異常，請查看系統日誌"; //+ ex.getMessage();
    fatalMesg = " >> PROGRAM : " + javaName + " METHOD : " + expMethod;

    if (errSql.equalsIgnoreCase("Y")) {
//      dispMesg = dispMesg + " <BR> " + sqlCmd;
      fatalMesg = fatalMesg + newLine + sqlCmd + newLine;
    }

    logger.fatal(CommString.validateLogData(fatalMesg));
    logger.fatal("Exception_Message : ", ex);
    logger.fatal(CommString.validateLogData(" >> ####### TarokoException MESSAGE   ENDED ######" + newLine));
  }

  // -rowid-----
  public byte[] itemRowId(String col) {
    return hexStrToByteArr(itemBuff(col)[0]);
  }

  public byte[] hexStrToByteArr(String str) {
    int len = str.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
    return data;
  }

  public boolean autQuery() {
    return (authData.length() > 0);
  }

  public boolean autUpdate() {
    return (authData.indexOf("1") >= 0);
  }

  public boolean autApprove() {
    return (this.authData.indexOf("5") >= 0);
  }

  public boolean autPrint() {
    return (this.authData.indexOf("6") >= 0);
  }

	@SuppressWarnings("rawtypes")
	public void putHTMLInputValues() {
		LinkedList<String> aacol=new LinkedList<>();
		Enumeration parmNames = this.request.getParameterNames();
		while (parmNames.hasMoreElements()) {
		   String str=(String) parmNames.nextElement();
			 this.insetValue = this.request.getParameterValues(str);
			 setInBuffer(str, this.insetValue);
			 aacol.add(str.trim().toUpperCase());
       if ( this.insetValue == null )
          { continue; }
         for (int kk=0; kk< this.insetValue.length; kk++) {
            setValue(str.trim().toUpperCase(), this.insetValue[kk], kk);
         }
		}

	  this.inputName = new String[aacol.size()];
      aacol.toArray(this.inputName);

	}
	
	public void checkChgDateForWorkAndLog() throws Exception {
		TarokoParm tarokoParm = TarokoParm.getInstance();
		if (!tarokoParm.getProcessWorkAndLogDate().equals(sysDate)) {
			synchronized (tarokoParm.getProcessWorkAndLogDate()) {
				processWorkAndLog();
			}	
		}
	}

	private void processWorkAndLog() throws Exception {
		TarokoParm tarokoParm = TarokoParm.getInstance();
		if (!tarokoParm.getProcessWorkAndLogDate().equals(sysDate)) {
			deleteWorkFile();
			// 2021/12/23 Justin delete and compress logs by log4j2
//			if ("Y".equals(tarokoParm.getLogDeleteFlag())) {
//				processLog();
//			}
			tarokoParm.setProcessWorkAndLogDate(sysDate);
		}
	}
	
	  /**
	   * delete the files in the work directory
	   * @throws Exception
	   */
	  public void deleteWorkFile() throws Exception {

		     showLogMessage("I","","delete work file started");
		     int fileCnt =0;
//		     String   appHost = request.getServerName().toLowerCase();
		     TarokoParm tarokoParm = TarokoParm.getInstance();
		     String   workDir = tarokoParm.getRootDir()+"/WebData/work";
		     if ( tarokoParm.getResourceName()[0].indexOf("java") != -1 )
		        { workDir = tarokoParm.getDataRoot()+"/work"; }

			 // verify path
		     workDir = SecurityUtil.verifyPath(workDir);
		     File     workFolder = new File(workDir);
		     String[] workList   = workFolder.list();
		     for ( int i=0; i < workList.length; i++ ) {
		    	 String temppath = workDir+"/"+workList[i];
		    	// verify path
		    	 temppath = SecurityUtil.verifyPath(temppath);
		         	 File file  = new File(temppath);// new File(workDir+"/"+workList[i]);
		           if  ( file.isDirectory() )
		               { continue; }
		           if(file.delete() == false) 
		               { showLogMessage("I", "", String.format("刪除[%s]失敗", file.getPath().toString()));}
		           fileCnt++;
		           file = null;
		         }
		     workFolder = null;
		     showLogMessage("I","","delete work file ended : delete count "+fileCnt);

		     return;
		  }
	  
//	  /**
//	   * Compress or delete log files
//	   */
//	  private void processLog() {
//			showLogMessage("I","","Start processing log files and ");
//			 // verify path
//		    String logFolderPath = SecurityUtil.verifyPath(TarokoParm.LOG_FOLDER_PATH);
//		    File     logFolder = new File(logFolderPath);
//		    showLogMessage("D", "", String.format("Check log folder[%s]", logFolder.getPath()));
//		    if (logFolder.isDirectory()) {
//		    	String[] logNames   = logFolder.list();
//		    	for (int i = 0; i < logNames.length; i++) {
//		    		String fileName = logNames[i];
//		    		String tempPath = logFolderPath + "/" + fileName;
//		    		// verify path
//		    		tempPath = SecurityUtil.verifyPath(tempPath);
//		    		File file = new File(tempPath);
//		    		if (file.isDirectory()) {
//		    			continue;
//		    		}
//		    		if (file.isFile()) {
//						if (fileName.matches("Taroko_Log4j\\.log\\..*\\.gz") || fileName.matches("PDPA\\.log\\..*\\.gz") ) {
//							//  fileName == Taroko_Log4j.logXXXX-XX-XX.gz
//							if (isLogGtResDay(file)) {
//								deleteFile(fileName, file); 	
//							}		
//						}else {
//							if (fileName.matches("Taroko_Log4j\\.log\\..*") || fileName.matches("PDPA\\.log\\..*")) {
//								// fileName == Taroko_Log4j.log.XXXX-XX-XX
//								int compressCond = compressLog(file, logFolderPath, file.lastModified());
//								if (compressCond == 0) {
//									showLogMessage("I", "", String.format("Successfully compress %s", fileName));
//									deleteFile(fileName, file); 
//								}else if (compressCond == 1){
//									showLogMessage("E", "", String.format("Unsuccessfully compress %s", fileName));
//								}else if (compressCond == 2){
//									showLogMessage("W", "", String.format("Unsuccessfully Compress %s.gz  because this gz file has existed.", fileName, fileName));
//									showLogMessage("W", "", String.format("Try to delete %s ...", fileName));
//									deleteFile(fileName, file); 			
//								}
//							}
//						}
//					}		
//		    		file = null;
//		    	}
//			}
//		    logFolderPath = null;
//		    showLogMessage("I","","Processing log files ends;");
//
//		    return;
//			
//		}

//		private boolean isLogGtResDay(File file) {
//			int logReserveDay = TarokoParm.getInstance().getLogReserveDay();
//			if (logReserveDay == 0) {
//				return false;
//			}
//			return ChronoUnit.DAYS.between(
//					new Date(file.lastModified()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), 
//					LocalDate.now(ZoneId.systemDefault())) > logReserveDay;
//		}
		
		private boolean deleteFile(String fileName, File file) {
			boolean isDeleteOk = false;
			try {
				isDeleteOk = file.delete();
				if( isDeleteOk == false) {
					showLogMessage("E", "", String.format("Unsuccessfully Delete %s", fileName));
				}else {
					showLogMessage("I", "", String.format("Successfully Delete %s ", fileName));
				}
			} catch (SecurityException se) {
				showLogMessage("E", "", String.format("Unsuccessfully delete %s due to its permession", fileName));
			} catch (Exception e) {
				showLogMessage("E", "", String.format("Unsuccessfully delete %s", fileName));
			}
			return isDeleteOk;
		}
		
		private int compressLog(File logFile, String logFolderPath, long lastModified) {
			String outFilePath = SecurityUtil.verifyPath(logFolderPath + "/" +  logFile.getName() + ".gz");
			if (new File(outFilePath).exists() == false) {
				try(FileOutputStream fos = new FileOutputStream(outFilePath);
					    GZIPOutputStream gzipOut = new GZIPOutputStream(fos);
					    FileInputStream fis = new FileInputStream(logFile);){
					byte[] bytes = new byte[1024];
				    int length;
				    while((length = fis.read(bytes)) >= 0) {
				    	gzipOut.write(bytes, 0, length);
				    } 
				} catch (Exception e) {
					showLogMessage("E", "", e.getLocalizedMessage());
					e.printStackTrace();
					return 1; //檔案出現錯誤
				}
				
				File outputFile = new File(outFilePath);
				outputFile.setLastModified(logFile.lastModified());
			}else {
				return 2; // 檔案已被壓縮
			}
			
		    return 0;
			
		}
		
		boolean isNotFromLoginPage() {
			return itemEmpty("HIDE") == false && itemEmpty("DXC_LOGIN");
		}
		
		public String getEcsAcdpPath() {
			String path = "/cr/ecs/conf/ecsAcdp.properties";
			return SecurityUtil.verifyPath(path);
		}


}


