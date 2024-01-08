/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  109-09-13  V1.00.01  Zuwei       fix code scan issue      *
*  112-11-03  V1.00.03	Kevin       Auth batch Fix                           *
******************************************************************************/
package bank.AuthIntf;

// import static org.hamcrest.CoreMatchers.nullValue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AuthGateway {


  private final String EXCEPTION_ERROR_CODE = "99999999999999";
  AuthGlobalParm gb = null;
  AuthGate gate = null;
  Socket G_AuthSocket = null;

  public AuthGateway() {
    // TODO Auto-generated constructor stub

    gb = new AuthGlobalParm();
    gate = new AuthGate();

  }


  public String authHome =
      "C:/Howard/App/MegaBank/Howard/Auth/MegaAuthIntfProg/src/main/java/mega/Auth";
  String sG_IP = "", sG_Port = "", sG_Timeout = "";
  public Logger logger = null;
  public String isNeg ;

  private String genCommandStr(AuthData P_AuthData) {
    String sL_CommandStr = "";
    String sL_TargetChar = " ";
    sL_CommandStr = HpeUtil.fillCharOnRight(P_AuthData.getTransType(), 1, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getTypeFlag(), 1, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getCardNo(), 19, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getExpireDate(), 8, sL_TargetChar);

    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getTransAmt(), 12, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getMccCode(), 4, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getMchtNo(), 15, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getLocalTime(), 14, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getOrgAuthNo(), 6, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getOrgRefNo(), 12, sL_TargetChar);
    sL_CommandStr += HpeUtil.fillCharOnRight(P_AuthData.getCvv2(), 4, sL_TargetChar);
    /* trans_type => 1: regular 2:refund 3:reversal 4:代行 */
    /* 參考 proC Bil_g002.pc */
    /*
     * if ("1".equals(P_AuthData.trans_type)) { //一般交易
     * 
     * } else if ("2".equals(P_AuthData.trans_type)) { //退貨交易
     * 
     * } else if ("3".equals(P_AuthData.trans_type)) { //沖銷
     * 
     * } if ("4".equals(P_AuthData.trans_type)) { //代行
     * 
     * }
     */
    return sL_CommandStr;
  }

  private void genCommandIsoObj(AuthData P_AuthData) {
    String sL_CommandIsoStr = "";
    try {
      String sL_CurrentMonthAndDate = HpeUtil.getCurMonthAndDate("");
      String sL_CurrentHMS = HpeUtil.getCurHMS("");

      gate.isoField[4] = HpeUtil.fillCharOnLeft(P_AuthData.getTransAmt(), 10, "0") + "00"; // 金額
      gate.isoField[7] = sL_CurrentMonthAndDate + sL_CurrentHMS; // Transmission Date and Time
      gate.isoField[11] =
          HpeUtil.fillCharOnLeft(Integer.toString(HpeUtil.getRandomNumber(100000)), 6, "0"); // Audit
                                                                                             // number
      gate.isoField[12] = sL_CurrentHMS; // Local Transaction Time
      gate.isoField[13] = sL_CurrentMonthAndDate; // Local Transaction Date
      gate.isoField[17] = sL_CurrentMonthAndDate; // Capture Date
      gate.isoField[18] = P_AuthData.getMccCode();
      gate.isoField[22] = "00"; // pos entry mode
      gate.isoField[25] = "00"; // Point of Service Condition Mode
      gate.isoField[26] = P_AuthData.getTransType().toUpperCase();// A: install B: mail => 改為 I:
                                                                   // install M: mail C:公共事業
      gate.isoField[32] = "490706";// 資料長度＋收單機構代碼。若為中心特店，此欄位值為493817，TCB：490706

      String sL_ExpireYM = P_AuthData.getExpireDate().substring(2, 6);// 只取年月
      if (!"".equals(P_AuthData.getCvv2()))
        gate.isoField[35] =
            P_AuthData.getCardNo() + "=" + sL_ExpireYM + "        " + P_AuthData.getCvv2(); // Ex:
                                                                                             // 4312349000000048=210670111234998
      else
        gate.isoField[35] = P_AuthData.getCardNo() + "=" + sL_ExpireYM + "           "; // Ex:
                                                                                         // 4312349000000048=210670111234998

      gate.isoField[37] = "000000000000";// Retrieval Reference Number
      gate.isoField[41] = "                ";// 端末機代號
      gate.isoField[42] = "006123600901001";// TCB特店代號
      gate.isoField[43] = "TCB ONUS BATCH AUTH   TAIPEI       TW TW"; // Card Acceptor Name/Location
      gate.isoField[48] = "027010800431         000100"; // Additional data
      gate.isoField[49] = "901";// 固定值"901"，表示新台幣
      gate.isoField[61] = "BK77PRO200000000000"; // 發卡連線機構無須進行特別處理

      if ("1".equals(P_AuthData.trans_type)) { // 一般交易
        gate.mesgType = "0200";
        gate.isoField[3] = "000000"; // processing code
      } else if ("2".equals(P_AuthData.trans_type)) { // 退貨交易
        gate.mesgType = "0220";
        gate.isoField[3] = "200030"; // processing code
        gate.isoField[38] = P_AuthData.getOrgAuthNo();
      } else if ("3".equals(P_AuthData.trans_type)) { // 沖銷
        gate.mesgType = "0420";
        gate.isoField[3] = "000030"; // processing code
        gate.isoField[37] = P_AuthData.getOrgRefNo();

      } else if ("4".equals(P_AuthData.trans_type)) { // 代行

      }


    } catch (Exception e) {
      // TODO: handle exception
    }
    /* trans_type => 1: regular 2:refund 3:reversal 4:代行 */
    /* 參考 proC Bil_g002.pc */



  }

  public String startProcess(AuthData P_AuthData, String sP_PropertyFilePath) {
    String sL_Result = "";
    try {

      loadTextParm(sP_PropertyFilePath);
      // createLogger(sP_PropertyFilePath); //Howard:不寫 log

      if ("".equals(P_AuthData.getFullIsoCommand()))
        genCommandIsoObj(P_AuthData);
      sL_Result = startTrans(P_AuthData.getFullIsoCommand());

    } catch (Exception e) {
      // TODO: handle exception
      sL_Result = "";
      System.out.println("startProcess exception:" + e.getMessage());
    }
    return sL_Result;
  }

  public boolean initConnection(String sP_AuthServerIp, String sP_AuthServerPort) {
    boolean bL_Result = true;
    try {

      G_AuthSocket = new Socket(sP_AuthServerIp, Integer.parseInt(sP_AuthServerPort));

    } catch (Exception e) {
      // TODO: handle exception
      bL_Result = false;
    }

    return bL_Result;
  }

  public void releaseConnection() {

    try {

      if (null != G_AuthSocket)
        G_AuthSocket.getInputStream().close();
      G_AuthSocket.getOutputStream().close();
      G_AuthSocket.close();

    } catch (Exception e) {
      // TODO: handle exception

    }

    return;
  }

  public String startProcess(AuthData P_AuthData, String sP_AuthServerIp, String sP_AuthServerPort)
      throws Exception {
    String sL_Result = "";
    try {

      // loadTextParm(sP_PropertyFilePath);
      // createLogger(sP_PropertyFilePath);
      sG_IP = sP_AuthServerIp;
      sG_Port = sP_AuthServerPort;

      System.out.println("startProcess sG_IP:" + sG_IP +",sG_Port:"+sG_Port);

      if ("".equals(P_AuthData.getFullIsoCommand()))
        genCommandIsoObj(P_AuthData);
      sL_Result = startTrans(P_AuthData.getFullIsoCommand());

    } catch (Exception e) {
      // TODO: handle exception
      sL_Result = EXCEPTION_ERROR_CODE;
      System.out.println("startProcess exception:" + e.getMessage());
    }
    return sL_Result;
  }

  public String sendAuthData(AuthData P_AuthData) {
    String sL_Result = "";
    try {

      // loadTextParm(sP_PropertyFilePath);
      // createLogger(sP_PropertyFilePath);



      if ("".equals(P_AuthData.getFullIsoCommand()))
        genCommandIsoObj(P_AuthData);
      sL_Result = startBatchTrans(P_AuthData.getFullIsoCommand());

    } catch (Exception e) {
      // TODO: handle exception
      sL_Result = "";
      System.out.println("sendAuthData exception:" + e.getMessage());
    }
    return sL_Result;
  }

  private String startTrans(String sP_FullIsoCommand) throws Exception {
    String sL_TranxResult = "";



    try {
        System.out.println("startTrans sG_IP:" + sG_IP +",sG_Port:"+sG_Port+",isNeg:"+isNeg+",sP_FullIsoCommand:"+sP_FullIsoCommand);

      // down, 單筆授權
      AuthProcess authProcess = new AuthProcess(gate, gb, sG_IP, sG_Port, sP_FullIsoCommand,isNeg);

      authProcess.start();



      while (!authProcess.bG_GetResponse) {
        if (authProcess.bG_ExceptionOccurred) {
          break;
        }
        Thread.sleep(100);
      }
      if (authProcess.bG_ExceptionOccurred)
        sL_TranxResult = EXCEPTION_ERROR_CODE;
      else
        sL_TranxResult = authProcess.getAuthResult(); //

      // System.out.println("Get response from server:" + sL_TranxResult + "--");

      // up, 單筆授權



      /*
       * String sL_AuthNo="123456"; String sL_ResponseCode="00"; sL_TranxResult = sL_AuthNo +
       * sL_ResponseCode;
       */


    } catch (Exception e) {
      // TODO: handle exception
      // sL_TranxResult = "00000000";
      sL_TranxResult = EXCEPTION_ERROR_CODE;
      System.out.println("startTrans exception:" + e.getMessage());
    }
    return sL_TranxResult;
  }

  private String startBatchTrans(String sP_FullIsoCommand) throws IOException {
    String sL_TranxResult = "", sL_ResponseFromAuthAuth = "";



    try {

      // down, 單筆授權
      AuthProcess authProcess = new AuthProcess(gate, gb, sP_FullIsoCommand, G_AuthSocket);
      authProcess.start();



      while (!authProcess.bG_GetResponse) {
          if (authProcess.bG_ExceptionOccurred) {
              break;
          }
    	  Thread.sleep(100);
      }
      if (authProcess.bG_ExceptionOccurred)
          sL_TranxResult = EXCEPTION_ERROR_CODE;
        else
          sL_TranxResult = authProcess.getAuthResult(); //
      // System.out.println("Get response from server:" + sL_TranxResult + "--");

      // up, 單筆授權



      /*
       * String sL_AuthNo="123456"; String sL_ResponseCode="00"; sL_TranxResult = sL_AuthNo +
       * sL_ResponseCode;
       */


    } catch (Exception e) {
      // TODO: handle exception
      //sL_TranxResult = "00000000000000";
      sL_TranxResult = EXCEPTION_ERROR_CODE;
      System.out.println("startTrans exception:" + e.getMessage());
    }
    return sL_TranxResult;
  }

  public void loadTextParm(String sP_PropertyFilePath) throws Exception {
    String cvtString = "", dbSystem = "", dbUser = "", dbName = "", checkHome = "";



    Properties props = new Properties();


    // String confFile = authHome+"/parm/Auth_Parm.txt";
    String confFile = sP_PropertyFilePath + "/Auth_Parm.txt";
    System.out.println("config File Path :" + confFile);
    try (FileInputStream fis = new FileInputStream(confFile);) {


    props.load(fis);
//    fis.close();
    }

    sG_IP = props.getProperty("AUTH_HOST").trim();
    sG_Port = props.getProperty("AUTH_PORT").trim();
    sG_Timeout = props.getProperty("TIME_OUT_SEC").trim();



//    fis = null;
    props = null;

    return;

  }

  public void createLogger(String sP_PropertyFilePath) throws Exception {

    URL fis = null;

    // String sL_FilePath = authHome+"/parm/Auth_Log4j.properties";
    String sL_FilePath = sP_PropertyFilePath + "/Auth_Log4j.properties";

    System.out.println("log File Path :" + sL_FilePath);

//    PropertyConfigurator.configure(sL_FilePath);

    /*
     * System.out.println("URL=>" + fis.getFile().toString() ); PropertyConfigurator.configure(fis);
     */
    logger = (Logger) LogManager.getLogger("defaultlogger");

    return;
  }

}
