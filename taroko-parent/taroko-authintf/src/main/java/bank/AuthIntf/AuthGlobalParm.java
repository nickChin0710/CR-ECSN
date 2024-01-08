/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  109-09-04  V1.00.03  Zuwei      fix code scan issue     *
*  110-12-23  V1.00.04  Justin     log4j1 -> log4j2                          *  
******************************************************************************/
package bank.AuthIntf;


import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.Date;
import java.net.*;
import java.text.SimpleDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import Dxc.Util.SecurityUtil;

public class AuthGlobalParm {
  // public String authHome="D:/IBT_system/AuthSource";
  public String authHome =
      "C:/Howard/App/MegaBank/Howard/Auth/MegaAuthProg/src/main/java/mega/Auth";
  public boolean service4NCCC = false, service4AE = false, service4BatchAuth = false,
      service4Manual = false;
  // public IbmMqUtil G_IbmMqUtil = null;

  public String[] connName = {"", "", "", "", ""};

  boolean systemError = false, dbConnected = true, reloadParm = false;
  public Connection[] conn = new Connection[3];
  public Logger logger = null;
  public String confFile = "", debugMode = "", consoleMode = "", sqlCmd = "", monitorUser = "";
  public byte[] carriage = new byte[3];
  public String sysDate = "", sysTime = "", SQLTime = "", chinDate = "", dispDate = "",
      dispTime = "", millSecond = "", durTime = "";
  public String expMethod = "", newLine = "", dispMesg = "", exceptionFlag = "";
  public String localIp = "", ba24Host = "", visaHost = "", mastHost = "";
  public int ci = 0, dbCheckSec = 0;
  public int contTimout = 0, contExcept = 0, contAuth = 0, totTimout = 0, totExcept = 0;
  public int maxContTimout = 0, maxContExcept = 0;
  public int ba24Port = 0, visaPort = 0, mastPort = 0, webServerPort = 0;
  public int ba24Chan = 0, visaChan = 0, mastChan = 0, ba24ReadSocketTimeout = 0;
  public int dbCount = 0, fhmChan = 0;
  public float warningSec = 0;

  final int maxFhm = 100;
  int fhmPnt = 0;
  Object[] doneLock = new Object[maxFhm];

  public String hsmHost = "", ifEnableBa24 = "N";
  public int DbRetryTimes = 1000000, DbRetryPeriod = 60;
  public String ifEnableHsmVerifyCvv = "N", ifEnableHsmVerifyArqc = "N",
      ifEnableHsmChangeIwk = "N", ifEnableHsmVerifyIwk = "N", ifEnableHsmVerifyPvv = "N",
      ifEnableHsmVerifyPinBlock = "N", ifEnableHsmVerifyACSAAV = "N",
      ifEnableHsmTransPinBlock = "N", ifEnableHsmGenAtmPvv = "N", ifEnableHsmTransAtmPin = "N",
      ifEnableMq = "N";
  public int hsmPort = 0;

  public String[] dbType = {"", "", "", "", ""};
  public String[] dbNameCom = {"", "", "", "", ""};
  public String[] dbUserCom = {"", "", "", "", ""};
  public String[] downFlag = {"", "", "", "", ""};

  public String dbName = "", dbUser = "", dbOwner = "", dbPInfo = "", dbHost = "", homeDir = "",
      dbPort = "";

  public HashMap[] authQueue = new HashMap[20];

  public HashMap<String, String> cvtHash = new HashMap<String, String>();
  public HashMap<String, String> fhmRequest = new HashMap<String, String>();
  public HashMap<String, String> fhmResponse = new HashMap<String, String>();

  public InetAddress localHost = null;
  public String[] threadConnectionStatusArray = null;
  // public String[] isConnectingArray=null;
  public int WebThreadChanNum = 1001;

  // -JH-
  public String mod_pgm = "";

  @SuppressWarnings("unchecked")
  AuthGlobalParm() {
    for (int i = 0; i < 20; i++) {
      authQueue[i] = new HashMap();
    }

    return;
  }

  /* ���J ���v �t�ΰѼ� */
  public void loadTextParm() throws Exception {
    String cvtString = "", dbSystem = "", dbUser = "", dbName = "", checkHome = "";

    // InetAddress thisIp = InetAddress.getLocalHost();
    // localIp = thisIp.getHostAddress();

    checkHome = System.getenv("HOME");
    if (checkHome != null) {
      authHome = checkHome + "/auth_system";
    }
    // { authHome = checkHome+"auth_system"; }

    confFile = authHome + "/parm/Auth_Parm.txt";
    Properties props = new Properties();
    // verify path
    confFile = SecurityUtil.verifyPath(confFile);
    try (FileInputStream fis = new FileInputStream(confFile);) {
    	props.load(fis);
    }
//    fis.close();

    debugMode = props.getProperty("DEBUG_MODE").trim();
    consoleMode = props.getProperty("CONSOLE_MODE").trim();

    cvtString = props.getProperty("TIME_OUT_SEC").trim();
    warningSec = Float.parseFloat(cvtString);
    cvtString = props.getProperty("CONT_TIME_OUT").trim();
    maxContTimout = Integer.parseInt(cvtString);
    cvtString = props.getProperty("CONT_EXCEPTION").trim();
    maxContExcept = Integer.parseInt(cvtString);

    cvtString = props.getProperty("WEB_SERVER_PORT").trim();
    webServerPort = Integer.parseInt(cvtString);

    visaHost = props.getProperty("VISA_HOST").trim();
    cvtString = props.getProperty("VISA_CONNECT_PORT").trim();
    visaPort = Integer.parseInt(cvtString);
    cvtString = props.getProperty("VISA_CHANNEL_COUNT").trim();
    visaChan = Integer.parseInt(cvtString);

    mastHost = props.getProperty("MAST_HOST").trim();
    cvtString = props.getProperty("MAST_CONNECT_PORT").trim();
    mastPort = Integer.parseInt(cvtString);
    cvtString = props.getProperty("MAST_CHANNEL_COUNT").trim();
    mastChan = Integer.parseInt(cvtString);

    ba24Host = props.getProperty("BA24_HOST").trim();
    /*
     * cvtString = props.getProperty("IPASS_CONNECT_PORT").trim(); ipassPort =
     * Integer.parseInt(cvtString);
     */
    /*
     * cvtString = props.getProperty("IPASS_CHANNEL_COUNT").trim(); ipassChan =
     * Integer.parseInt(cvtString);
     */
    ba24Chan = 1;
    cvtString = props.getProperty("BA24_READ_SOCKET_TIMEOUT").trim();
    ba24ReadSocketTimeout = Integer.parseInt(cvtString);

    hsmHost = props.getProperty("HSM_HOST").trim();
    cvtString = props.getProperty("HSM_CONNECT_PORT").trim();
    hsmPort = Integer.parseInt(cvtString);


    ifEnableBa24 = props.getProperty("ENABLE_BA24").trim();

    ifEnableHsmVerifyCvv = props.getProperty("ENABLE_HSM_VERIFY_CVV").trim();
    ifEnableHsmVerifyPvv = props.getProperty("ENABLE_HSM_VERIFY_PVV").trim();
    ifEnableHsmVerifyPinBlock = props.getProperty("ENABLE_HSM_VERIFY_PIN_BLOCK").trim();
    ifEnableHsmVerifyACSAAV = props.getProperty("ENABLE_HSM_VERIFY_ACSAAV").trim();
    ifEnableHsmTransPinBlock = props.getProperty("ENABLE_HSM_TRANS_PINBLOCK").trim();
    ifEnableHsmGenAtmPvv = props.getProperty("ENABLE_HSM_GEN_ATM_PVV").trim();
    ifEnableHsmTransAtmPin = props.getProperty("ENABLE_HSM_TRANS_ATM_PIN").trim();
    ifEnableHsmVerifyArqc = props.getProperty("ENABLE_HSM_VERIFY_ARQC").trim();
    ifEnableHsmChangeIwk = props.getProperty("ENABLE_HSM_CHANGE_IWK").trim();
    ifEnableHsmVerifyIwk = props.getProperty("ENABLE_HSM_VERIFY_IWK").trim();
    ifEnableMq = props.getProperty("ENABLE_MQ").trim();



    /*
     * cvtString = props.getProperty("DB_RETRY_TIMES").trim(); DbRetryTimes =
     * Integer.parseInt(cvtString);
     * 
     * cvtString = props.getProperty("DB_RETRY_PERIOD").trim(); DbRetryPeriod =
     * Integer.parseInt(cvtString);
     */

    for (int k = 1; k < 10; k++) {
      dbSystem = props.getProperty("SYSTEM_NAME_" + k);
      if (dbSystem == null) {
        break;
      }
      dbUser = props.getProperty(dbSystem + "_USERID_" + k).trim();
      dbName = props.getProperty(dbSystem + "_DBNAME_" + k).trim();
      connName[k - 1] = dbName + ":" + dbUser;
      if (dbSystem.equals("MONITOR")) {
        monitorUser = connName[k - 1];
      }
      dbCount++;
    }

//    fis = null;
    props = null;

    return;
  }

  public synchronized void transStatistic(float durSec, int authCnt) throws Exception {
    if (durSec >= warningSec) {
      contTimout++;
      totTimout++;
    } else {
      contTimout = 0;
    }

    if (authCnt > 0) {
      contAuth += authCnt;
    } else {
      contAuth = 0;
    }

    if (!exceptionFlag.equals("Y")) {
      contExcept = 0;
    }

    if (contTimout >= maxContTimout || contExcept >= maxContExcept) {
      systemError = true;
    }

    if (contTimout >= maxContTimout) {
      showLogMessage("W", "�s�����O�ɶW�L : " + maxContTimout + " ��", "" + durSec + " sec");
    }

    if (contExcept >= maxContExcept) {
      showLogMessage("W", "�s�������`�W�L : " + maxContExcept + " ��", "" + durSec + " sec");
    }
    return;
  }

  public void dateTime() {

    String dateStr = "", dispStr = "";
    Date currDate = new Date();
    SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    SimpleDateFormat form_2 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
    SimpleDateFormat form_3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    dateStr = form_1.format(currDate);
    dispStr = form_2.format(currDate);
    SQLTime = form_3.format(currDate);

    sysDate = dateStr.substring(0, 8);
    chinDate = (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + dateStr.substring(4, 8);
    sysTime = dateStr.substring(8, 14);
    millSecond = dateStr.substring(14, 17);
    dispDate = dispStr.substring(0, 10);
    dispTime = dispStr.substring(10, 18);
    carriage[0] = 0x0D;
    carriage[1] = 0x0A;
    newLine = new String(carriage, 0, 2);
    return;
  }

  public synchronized float durationTime(String startMillis) {
    long startNum = 0, endNum = 0, duration = 0, milsec = 0;
    float floatSec = 0;
    startNum = Long.parseLong(startMillis);
    endNum = System.currentTimeMillis();

    floatSec = ((float) (endNum - startNum)) / 1000;
    duration = (endNum - startNum) / 1000;
    milsec = (endNum - startNum) % 1000;
    durTime =
        duration / 3600 + " : " + (duration % 3600) / 60 + " : " + (duration % 60) + " : " + milsec;
    return floatSec;
  }


  public void createLogger(String idCode) throws Exception {
	String loggerName = "";
    if (idCode.equals("P")) {
//      String sL_FilePath = authHome + "/parm/Auth_Log4j.properties";
      // System.out.println("log File Path :" + sL_FilePath);
//      PropertyConfigurator.configure(authHome + "/parm/Auth_Log4j.properties");
      loggerName = "auth";
    } else {
//      PropertyConfigurator.configure(authHome + "/parm/Simulator_Log4j.properties");
      loggerName = "simulator";
    }
    logger = (Logger) LogManager.getLogger(loggerName);

    return;
  }

  public void showLogMessage(String actCode, String procMethod, String actionMessage) {
    try {
      String stepMesg = "";
      if (consoleMode.equals("Y")) {
        dateTime();

        System.out.println(dispTime + "-" + millSecond + " " + procMethod + " " + actionMessage);
        return;
      }

      stepMesg = "";
      if (actCode.equals("D") && debugMode.equals("Y")) {
        logger.debug("> " + stepMesg + " " + procMethod + " " + actionMessage);
      } else if (actCode.equals("I")) {
        logger.info(" > " + stepMesg + " " + procMethod + " " + actionMessage);
      } else if (actCode.equals("W")) {
        logger.warn(" > " + stepMesg + " " + procMethod + " " + actionMessage);
      } else if (actCode.equals("E")) {
        logger.error("> " + stepMesg + " " + procMethod + " " + actionMessage);
      }
    } catch (Exception ex) {
      expHandle(ex);
    }

    return;
  }

  public void expHandle(Exception ex) {
    if (ex == null)
      return;
    String fatalMesg = "";

    exceptionFlag = "Y";
    contExcept++;
    totExcept++;
    // if ( consoleMode.equals("Y") )
    // { ex.printStackTrace(); return; }

    ex.printStackTrace();

    logger.fatal(" >> ####### AUTH Exception MESSAGE STARTED ######" + newLine);
    dispMesg = "" + ex.getMessage();
    if (sqlCmd.length() > 0) {
      fatalMesg = dispMesg + newLine + sqlCmd + newLine;
    }
    logger.fatal(fatalMesg);
    logger.fatal("Exception_Message : ", ex);
    logger.fatal(" >> ####### AUTH system Exception MESSAGE   ENDED ######" + newLine);

    sqlCmd = "";
    expMethod = "";


    return;
  }

}
