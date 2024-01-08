/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE    Version    AUTHOR                       DESCRIPTION              *
*  --------  -------------------  ------------------------------------------  *
*  89/04/01  V1.0 a1   Jack Liao  將 RISC/600 資料傳輸至 IBM 主機             *
*  92/01/22  V1.0 a2   Kevin Chu  E-Mail對帳單改由MQ傳送                      *
*  93/07/06  V1.01.01  Allen Ho   add NUVISA* type data                       *
*  93/08/09  V1.01.02  Monica     add NUCDPA01 不進行轉碼                     *
*  93/08/23  V1.02.01  Allen Ho   add NUVISALT                                *
*  93/10/04  V1.03.01  Allen Ho   MOD NUCDPA01 => NUVDPA01                    *
*  93/10/05  V1.03.02  Jena Lee   add NUCDVDAI                                *
*  96/05/17  V1.03.03  icbcsmb    add NU*CUST NU*FEER to BK                   *
* 107/07/01  J1.03 01  proc C convert to Java                                 *
* 108/05/08  V1.03.04  Lai        mark  showLogMessage                        *
* 109-07-06  V1.03.05   shiyuqi       updated for project coding standard     *
* 109-07-22  V1.03.06   Zuwei       updated for project coding standard     *
*110-01-07  V1.00.02    shiyuqi       修改无意义命名                                                                           *
******************************************************************************/
package Mqm;

import java.io.*;
import java.util.*;
import com.*;

public class MqmSend extends AccessDAO {

  public String ftpHome = "", startTime = "";
  int recLength = 0, blockCount = 0, blockSize = 0, totalCount = 0, messlen = 0, showCnt = 0;
  String inputFile = "", fileName = "", convCode = "", transDate = "", eofString = "";

  byte[] eofData = {(byte) 0xC5, (byte) 0xD6, (byte) 0xC6};

  final int DEBUG0 = 0;

  CommFunction comm = new CommFunction();
  // CharFormatConverter cfc = new CharFormatConverter();

  MQControl mq = new MQControl();

//// J2EE Bad Practices: Leftover Debug Code
//public static void main(String[] args) throws Exception {
//  MqmSend proc = new MqmSend();
//  int retCode = proc.mainProcess(args);
//  proc.programEnd(retCode);
//}

  public int mainProcess(String[] args) {

    try {
      logFlag = "mqm";
      dateTime();
      if (args.length < 1) {
        showLogMessage("E", "", "Required parameter transfer filename\n");
        programEnd(-1);
      }

      if (args.length < 2) {
        showLogMessage("E", "", "Required parameter transfer file record length\n");
        programEnd(-1);
      }

      showLogMessage("I", "", "MQ_SEND 將 RISC/6000 資料傳輸至 IBM 主機 V 1.0 2018/06/25 ");
      javaProgram = this.getClass().getName();

      if (!connectDataBase()) {
        return -1;
      }

      mq.mqHost = mqHost;
      mq.mqPort = mqPort;
      if (mq.mqPort.equals("9999")) {
        return 0;
      }

      fileName = args[0];
      recLength = Integer.parseInt(args[1]);
      blockCount = (204800 - 512) / recLength;
      if (blockCount > 9999) {
        blockCount = 9999;
      }
      blockSize = blockCount * recLength;
      totalCount = 0;

      selectPtrBusinday();

      insertCycTransfer();

      convCode = "Y";
      eofString = new String(eofData, 0, 3);

      if (fileName.equals("NUPCIDAT") || fileName.equals("NUPCIACT") || fileName.equals("NUCDCOAI")
          || fileName.equals("NUCURBIL") || fileName.equals("NUMONBIL")
          || fileName.equals("NUVISAC1") || fileName.equals("NUVISAD1")
          || fileName.equals("NUACJR08") || fileName.equals("NUVISALT")
          || fileName.equals("NUVDPA01") || fileName.equals("NUCDVDAI")) {
        convCode = "N";
      }

      mq.apQName = "009NU." + fileName + ".QR";
      int qs = mq.connectMQ();
      sendDataToQueue(qs);
      mq.closeMq(qs);

      if (totalCount == 0) {
        showLogMessage("E", "", "FILE NO DATA FOUND " + fileName);
        mq.disConnectMQ();
        return -1;
      }

      mq.apQName = "009NU.NUFTP.CONTROL.QR";
      int qc = mq.connectMQ();
      sendControlQueue(qc);
      mq.closeMq(qc);

      mq.disConnectMQ();

      updateCycTransfer();
      finalProcess();

      return 0;
    }

    catch (Exception ex) {
      expHandle(ex);
      return exceptExit;
    }

  }

  public void sendDataToQueue(int qs) throws Exception {
    String inputFile = System.getenv("PROJ_HOME") + "/ecs_ftp/" + fileName;
    if (!openBinaryInput(inputFile)) {
      exitProgram(-1);
    }
    showLogMessage("I", "", "START MQ SEND " + mq.apQName);

    byte[] inputData = new byte[blockSize];
    while (true) {
      // messlen = readBinFile(inputData,blockSize);
      messlen = readBinFile(inputData);
      showCnt++;
      if (messlen >= recLength) {
        if (convCode.equals("Y")) {
          byte[] outData = new String(inputData, 0, messlen).getBytes("Cp1047");
          int outLength = outData.length;
          mq.writeMQbyte(qs, outData, outLength);
          String showData = byteToHexString(new String(inputData, 0, messlen).getBytes("Cp1047"));
          if (DEBUG0 == 1)
            showLogMessage("I", "", "SEND QUEUE EBCDIC CONV Cp1047:[" + showCnt + " "
                + showData.length() + "]" + showData);
          showLogMessage("I", "",
              "SEND QUEUE EBCDIC CONV Cp1047:[" + showCnt + " " + showData.length() + "]");
        } else {
          mq.writeMQbyte(qs, inputData, messlen);
          String showData = byteToHexString(new String(inputData, 0, messlen).getBytes());
          // showLogMessage("I","","SEND QUEUE EBCDIC NO Cp1047 : "+showData);
          showLogMessage("I", "",
              "SEND QUEUE EBCDIC NO Cp1047 :[" + showCnt + " " + showData.length() + "]");
        }
        totalCount = totalCount + (messlen / recLength);
      } else if (totalCount == 0) {
        break;
      } else {
        mq.writeMQbyte(qs, eofData, 3);
        break;
      }
    }

    showLogMessage("I", "", "ENDED MQ SEND =  " + totalCount);
    return;

  }

  /******************************************************************/
  /*                                                                */
  /* When all data sned to the mqueue */
  /* sens contrl data to control queue */
  /*                                                                */
  /******************************************************************/
  public void sendControlQueue(int qc) throws Exception {
    if (totalCount == 0) {
      return;
    }

    String contrlData = "009" + comm.fillRightSpace(fileName, 8) + comm.fillZero("" + totalCount, 8)
        + comm.fillZero("" + recLength, 4) + comm.fillZero("" + blockCount, 4);

    showLogMessage("I", "", "START MQ SEND CONTROL ASCII : " + mq.apQName + "," + contrlData);

    mq.writeMQbyte(qc, contrlData.getBytes("Cp1047"), 27);
    String showData = byteToHexString(contrlData.getBytes("Cp1047"));
    // showLogMessage("I","","CONTROL QUEUE EBCDIC DATA : "+showData);
    showLogMessage("I", "", "ENDED MQ SEND CONTROL EBCDIC: " + mq.apQName + "," + showData);

    return;
  }

  public int selectPtrBusinday() throws Exception {
    daoTable = "ptr_businday";
    extendField = "busi.";
    selectSQL = "business_date";
    whereStr = "";
    int n = selectTable();
    if (n == 0) {
      showLogMessage("E", "", "select_ptr_businday ERROR ");
      exitProgram(3);
    }
    return n;
  }

  public void insertCycTransfer() throws Exception {
    startTime = sysTime;
    daoTable = "cyc_transfer";
    dateTime();
    transDate = sysDate;
    setValue("trans_date", sysDate);
    setValue("trans_type", "S");
    setValue("start_time", startTime);
    setValue("end_time", "");
    setValue("file_name", fileName);
    setValue("file_desc", "");
    setValue("trans_cnt", "0");
    setValue("resp_code", "ST");
    int n = insertTable();
    if (n == 0) {
      showLogMessage("E", "", "SEND INSERT CYC_TRANSFER ERROR !! " + fileName);
    }

    return;
  }

  public void updateCycTransfer() throws Exception {
    dateTime();

    daoTable = "cyc_transfer";
    updateSQL = "end_time   = ?," + "trans_cnt  = ?," + "resp_code  = ? ";
    whereStr = "WHERE trans_date = ? AND file_name = ? and start_time = ? ";

    setString(1, sysTime);
    setString(2, "" + totalCount);
    setString(3, "ET");
    setString(4, transDate);
    setString(5, fileName);
    setString(6, startTime);
    int cnt = updateTable();
    if (cnt == 0) {
      showLogMessage("E", "", "SEND UPDATE CYC_TRANSFER ERROR !! " + fileName);
    }

    return;
  }

} // enf of class MqmSend
