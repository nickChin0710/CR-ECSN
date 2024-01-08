/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  92/02/19  V1.00.09   Allen Ho    Initial Program Creation                  *
 *  107/09/11 V1.01.00   David       Transfer to JAVA                          *
 *  108/08/23 V1.01.00   JH       Transfer to JAVA                          *
 *  108/08/23 V1.01.00   JH       Transfer to JAVA                          *
 * 109-07-06  V1.00.01  shiyuqi       updated for project coding standard     *
 * 110-01-07  V1.00.02    shiyuqi       修改无意义命名                                                                           *
 ******************************************************************************/

package Sec;

import java.sql.Connection;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;

public class SecR001 extends AccessDAO {
  private final String PROGNAME = "檔案加密處理程式 V1.00.09 93/02/19";
  CommCrd comc = new CommCrd();

  String hFepdPawdVer = "";
  String hFepdPawdSec1 = "";
  String hFepdPawdSec2 = "";



  private String inputData = "";
  private String hTempSystime = "";
  private String inputFile = "";
  private String outputFile = "";

  private int hwdInt1 = 0;
  private int hwdInt2 = 0;
  private int hwdInt3 = 0;

  /****
   * return 值 1 : 資料庫錯誤 & 其他未知錯誤 2 : sec_r001 執行參數錯誤 3 : 無法關閉亂化檔 4 : 無法開啟輸入檔 5 : 無法開啟輸出檔 6 :
   * 輸入檔案無法刪除
   ****************/
  public int mainProcess(String[] args) {

    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + PROGNAME + "," + args.length);
      // =====================================
      if (args.length != 1) {
        showLogMessage("I", "", "Usage : SecR001 filename");
        return 2;
      }

      // 固定要做的

      if (super.conn == null) {
        showLogMessage("I", "", "Pls call setConnection first");
        return 1;
      }
      // ========================================================
      // Init parameter
      inputData = "";
      hTempSystime = "";
      // if (rfd != null) {rfd.close(); rfd = null;}
      // if (ftd2 != null) {ftd2.close(); ftd2 = null;}
      inputFile = "";
      outputFile = "";

      hwdInt1 = 0;
      hwdInt2 = 0;
      hwdInt3 = 0;
      // ========================================================

      inputData = args[0];

      int nRtn = selectSysdate();
      if (nRtn != 0)
        return nRtn;

      nRtn = getSecFilePawd();
      if (nRtn != 0)
        return nRtn;

      nRtn = readFile();
      if (nRtn != 0)
        return nRtn;

      // chmod(output_file,S_IRUSR|S_IWUSR|S_IXUSR|S_IRGRP);
      comc.chmodSec(outputFile);
      if (comc.fileDelete(inputData) == false) {
        showLogMessage("I", "", "輸入檔案無法刪除");
        return 6;
      }
      // ==============================================
      // 固定要做的
      showLogMessage("I", "", String.format("程式執行結束"));
      // finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      showLogMessage("I", "", ex.getMessage() + "\n" + comc.getStackTraceString(ex));
      // expHandle(ex);
      return 1;
    }
  }

  public void setConnection(Connection conn[], String[] dbAlias) throws Exception {
    // TODO Auto-generated constructor stub
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    return;
  }

  /**************************************************************************/
  private int readOpen() throws Exception {
    inputFile = inputData;

    if (inputFile.substring(inputFile.length() - 1, inputFile.length()).equals("."))
      inputFile = inputFile.substring(0, inputFile.length());

    outputFile = String.format("%s.SEC", inputFile);

    inputFile = Normalizer.normalize(inputFile, java.text.Normalizer.Form.NFKD);
    outputFile = Normalizer.normalize(outputFile, java.text.Normalizer.Form.NFKD);

    int f = openInputText(inputFile);
    if (f == -1) {
      showLogMessage("I", "", String.format("OPEN input file [%s] error", inputData));
      return 4;
    }
    closeInputText(f);

    if (openBinaryOutput(outputFile) == false) {
      showLogMessage("I", "", String.format("OPEN output file [%s] error\n", outputFile));
      return 5;
    }

    return 0;

  }

  /*************************************************************************/
  private int readFile() throws Exception {
    int int2, int3, recSize, intna;
    int intnb = 0;
    String tmpStr = "";

    int nRtn = readOpen();
    if (nRtn != 0)
      return nRtn;

    intna = comc.str2int(hTempSystime) % 5;
    showLogMessage("I", "", "intna = " + intna);
    switch (intna) {
      case 0:
        intnb = 11;
        break;
      case 1:
        intnb = 13;
        break;
      case 2:
        intnb = 17;
        break;
      case 3:
        intnb = 19;
        break;
      case 4:
        intnb = 23;
        break;
    }
    String data_buffer =
        String.format("%14.14s%3.3s%3.3s%2.2s%4.4s ", hFepdPawdVer, " ", " ", " ", hTempSystime);
    showLogMessage("I", "", "SecR001 data_buffer1 = " + data_buffer);
    byte[] tmpByte = data_buffer.getBytes();
    for (int int1 = 0; int1 < 27; int1++) {
      int2 = int1 + 1 % 20;
      if (int2 == 0)
        int2 = 20;
      int3 = (byteToUnsignedInt(tmpByte[int1]) + int2 * intnb) % 122;
      if (int3 < 20)
        int3 = int3 + 122;
      tmpByte[int1] = (byte) int3;
    }
    tmpByte[26] = (byte) (intna - 23);
    // data_buffer = new String(tmpByte);
    writeBinFile(tmpByte, tmpByte.length);

    recSize = comc.str2int(hTempSystime.substring(2)) * 3;
    byte[] tmpByte1 = new byte[recSize];
    for (int int1 = 0; int1 < recSize; int1++)
      tmpByte1[int1] = (byte) ((recSize + 77 + int1 * 3) % 256);
    // data_buffer = new String(tmpByte1);
    tmpByte1 = wSecurity(recSize, tmpByte1);
    writeBinFile(tmpByte1, tmpByte1.length);
    tmpByte1 = null;

    tmpStr = String.format("%2.2s", hTempSystime);
    recSize = comc.str2int(tmpStr);
    byte[] tmpByte2 = new byte[recSize];
    for (int int1 = 0; int1 < recSize; int1++)
      tmpByte2[int1] = (byte) ((recSize + 17 + int1 * 7) % 256);

    // data_buffer = new String(tmpByte2);
    tmpByte2 = wSecurity(recSize, tmpByte2);
    writeBinFile(tmpByte2, tmpByte2.length);
    tmpByte2 = null;

    byte[] bytes = new byte[1024];
    openBinaryInput(inputFile);
    while ((recSize = readBinFile(bytes)) > 0) {
      // data_buffer = new String(bytes, 0, rec_size);
      byte[] tmpBytes = new byte[recSize];
      System.arraycopy(bytes, 0, tmpBytes, 0, recSize);
      tmpBytes = wSecurity(recSize, tmpBytes);
      writeBinFile(tmpBytes, tmpBytes.length);
    }
    closeBinaryInput();
    bytes = null;


    try {
      closeBinaryOutput();
    } catch (Exception ex) {
      showLogMessage("I", "", String.format("無法亂化 [%s],Exception[%s]", inputData, ex.getMessage()));
      return 3;
    }

    return 0;

  }

  /*************************************************************************/
  private int getSecFilePawd() throws Exception {
    long int2, int3, int5, int6;
    String tempStr = "";

    sqlCmd = "select passwd_ver, ";
    sqlCmd += "       passwd_sec1, ";
    sqlCmd += "       passwd_sec2 ";
    sqlCmd += "from   sec_file_passwd ";
    sqlCmd += "fetch first 1 rows only ";

    selectTable();
    if (notFound.equals("Y")) {
      showLogMessage("I", "", "select sec_file_passwd error");
      return 1;
    }
    hFepdPawdVer = getValue("passwd_ver");
    hFepdPawdSec1 = getValue("passwd_sec1");
    hFepdPawdSec2 = getValue("passwd_sec2");


    int5 = 0;
    tempStr = String.format("%s", hFepdPawdSec1);
    byte[] tmpByte = tempStr.getBytes();
    for (int int1 = 0; int1 < hFepdPawdSec1.length(); int1++) {
      int2 = int1 + 1 % 20;
      if (int2 == 0)
        int2 = 20;
      if (byteToUnsignedInt(tmpByte[int1]) > 122)
        tmpByte[int1] = (byte) (byteToUnsignedInt(tmpByte[int1]) - 122);
      int3 = (2440 + byteToUnsignedInt(tmpByte[int1]) - int2 * 41) % 122;
      if (int3 == 0)
        int3 = 122;
      int5 = int5 + int3 * int2;
    }
    hwdInt1 = (int) (int5 % 123);

    int6 = 0;
    tempStr = hFepdPawdSec2;
    tmpByte = tempStr.getBytes();
    for (int int1 = 0; int1 < hFepdPawdSec2.length(); int1++) {
      int2 = int1 + 1 % 20;
      if (int2 == 0)
        int2 = 20;
      if (byteToUnsignedInt(tmpByte[int1]) > 122)
        tmpByte[int1] = (byte) (byteToUnsignedInt(tmpByte[int1]) - 122);
      int3 = (2440 + byteToUnsignedInt(tmpByte[int1]) - int2 * 71) % 122;
      if (int3 == 0)
        int3 = 122;
      int6 = int6 + int3 * int2;
    }
    hwdInt2 = (int) (int6 % 231);

    hwdInt3 = (hwdInt1 * 31 + hwdInt2 * 67 + comc.str2int(hTempSystime)) % 117;

    return 0;
  }

  /*************************************************************************/
  private int selectSysdate() throws Exception {
    hTempSystime = "";

    sqlCmd = "select to_char(sysdate,'miss') as h_temp_systime ";
    sqlCmd += " from   dual ";
    selectTable();
    if (notFound.equals("Y")) {
      showLogMessage("I", "", "select sysdate error");
      return 1;
    }
    hTempSystime = getValue("h_temp_systime");
    return 0;

  }

  /*************************************************************************/
  private byte[] wSecurity(int dSize, byte[] dBuffer) {
    byte[] stra = new byte[130];
    // byte[] d_buf_byte = d_buffer.getBytes();

    int inta = hwdInt1 % 8;
    int intb = hwdInt2 % 8;
    int intc = hwdInt3;

    for (int int1 = 0; int1 < dSize; int1++) {
      stra[100] = dBuffer[int1];
      // long a = ((intc + byteToUnsignedInt(stra[100])) % 256);
      stra[0] = (byte) ((intc + byteToUnsignedInt(stra[100])) % 256);
      stra[1] = stra[0];
      stra[2] = stra[0];
      stra[11] = (byte) (Byte.toUnsignedInt(stra[1]) << inta - 1);
      stra[12] = (byte) (Byte.toUnsignedInt(stra[11]) >> 7);


      if (byteToUnsignedInt(stra[12]) == 0)
        stra[13] = 1;
      else
        stra[13] = 0;
      stra[19] = (byte) (Byte.toUnsignedInt(stra[13]) << 8 - inta);
      stra[14] = (byte) (Byte.toUnsignedInt(stra[1]) >> 9 - inta);
      stra[15] = (byte) (Byte.toUnsignedInt(stra[14]) << 9 - inta);
      stra[16] = (byte) (Byte.toUnsignedInt(stra[1]) << inta);
      stra[17] = (byte) (Byte.toUnsignedInt(stra[16]) >> inta);
      stra[18] = (byte) (byteToUnsignedInt(stra[19]) + byteToUnsignedInt(stra[15])
          + byteToUnsignedInt(stra[17]));
      stra[33] = (byte) (byteToUnsignedInt(stra[19]) + byteToUnsignedInt(stra[15])
          + byteToUnsignedInt(stra[17]));

      stra[28] = stra[18];
      stra[1] = stra[18];
      stra[2] = stra[18];
      stra[11] = (byte) (Byte.toUnsignedInt(stra[1]) >> 8 - intb);
      stra[12] = (byte) (Byte.toUnsignedInt(stra[2]) << intb);
      stra[10] = (byte) (byteToUnsignedInt(stra[11]) + byteToUnsignedInt(stra[12]));
      dBuffer[int1] = (byte) (byteToUnsignedInt(stra[11]) + byteToUnsignedInt(stra[12]));
    }

    return (dBuffer);
  }

  // **********************************************************
  // **********************************************************
  private int byteToUnsignedInt(byte bytes) {
    return 0x00 << 24 | bytes & 0xff;
  }
  /***********************************************************************/


}
