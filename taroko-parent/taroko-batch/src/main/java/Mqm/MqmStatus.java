/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------  -------------------  ------------------------------------------ *
* 107/07/01  J1.01 01  proc C convert to Java                                *
* 109-07-06  V1.00.02   shiyuqi       updated for project coding standard     *
* 110-01-07  V1.00.02    shiyuqi       修改无意义命名                           
* 111-01-19  V1.00.04  Justin       fix J2EE Bad Practices: Leftover Debug Code
*****************************************************************************/
package Mqm;
import java.io.*;
import java.util.*;
import com.*;

public class MqmStatus extends AccessDAO {
  String queueString = "", fileName = "", statusCode = "", fileDesc = "";

  CommFunction comm = new CommFunction();
  CharFormatConverter cfc = new CharFormatConverter();

  MQControl mq = new MQControl();

//// J2EE Bad Practices: Leftover Debug Code
//public static void main(String[] args) throws Exception {
//  MqmStatus proc = new MqmStatus();
//  int retCode = proc.mainProcess(args);
//  proc.programEnd(retCode);
//}

  public int mainProcess(String[] args) {
    try {
      logFlag = "mqm";

      dateTime();
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", "MqmStatus - 接收 IBM 主機傳送來之傳輸狀態資料 J1.01 01");

      if (!connectDataBase()) {
        return -1;
      }

      mq.mqHost = mqHost;
      mq.mqPort = mqPort;

      selectPtrBusinday();

      // 讀 STATUS QUEUE
      mq.mqType = "I";
      mq.apQName = "900NU.NUFTP.STATUS.QL";
      int qs = mq.connectMQ();

      while (true) {

        byte[] queueData = mq.readMQbyte(qs);
        if (mq.queueEnd[qs]) {
          break;
        }

        queueString = new String(queueData, "Cp1047");
        String orgString = new String(queueData);

        int messlen = queueString.length();
        if (messlen < 37) {
          showLogMessage("E", "", "RECEIVE STATUS DATA ERROR [" + queueString + "]");
          continue;
        } else {
          showLogMessage("I", "", "RECEIVE STATUS DATA [" + queueString + "] " + messlen);
        }

        fileName = queueString.substring(3, 11);
        statusCode = queueString.substring(35, 37);
        showLogMessage("I", "", "FILE " + fileName + " : " + statusCode);
        if (statusCode.equals("00")) {
          statusCode = "OK";
        }
        updateCycTransfer();

      }

      mq.closeMq(qs);
      mq.disConnectMQ();

      finalProcess();
      return 0;
    }

    catch (Exception ex) {
      expHandle(ex);
      return exceptExit;
    }

  }

  /***************************************************************************/
  public void selectPtrBusinday() throws Exception {
    daoTable = "ptr_businday";
    extendField = "busi.";
    selectSQL = "business_date";
    whereStr = "";
    int n = selectTable();
    if (n == 0) {
      showLogMessage("E", "", "select_ptr_businday ERROR ");
      exitProgram(3);
    }
    return;
  }

  /***************************************************************************/
  public void updateCycTransfer() throws Exception {
    dateTime();

    daoTable = "cyc_transfer";
    selectSQL = "MAX(trans_date) as trans_date";
    whereStr = "WHERE  file_name = ? AND resp_code = 'ET' ";
    setString(1, fileName);
    int cnt = selectTable();
    if (cnt == 0) {
      showLogMessage("E", "", "select CYC_TRANSFER ERROR !! " + fileName);
      return;
    }

    if (statusCode.equals("OK")) {
      daoTable = "cyc_transfer";
      updateSQL = "end_time   = ?," + "file_desc  = to_char(TRANS_CNT), " + "resp_code  = ? ";
      whereStr = "WHERE trans_date = ? AND file_name  = ? and resp_code = 'ET' ";
      setString(1, sysTime);
      setString(2, statusCode);
      setString(3, getValue("trans_date"));
      setString(4, fileName);
    } else {
      daoTable = "cyc_transfer";
      updateSQL = "end_time   = ?," + "resp_code  = ? ";
      whereStr = "WHERE trans_date = ? AND file_name  = ? and resp_code = 'ET' ";
      setString(1, sysTime);
      setString(2, statusCode);
      setString(3, getValue("trans_date"));
      setString(4, fileName);
    }
    cnt = updateTable();
    if (cnt == 0) {
      showLogMessage("E", "",
          "RECEIVE STATUS UPDATE CYC_TRANSFER ERROR !! " + sysDate + " - " + fileName);
    }

    return;
  }

} // end of class
