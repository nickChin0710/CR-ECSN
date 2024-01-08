package Sec;
/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/09/11  V1.00.00    Brian     program initial                          
*  109/06/15  V1.00.01    yanghan   resolve Unreleased Resource: Streams      *
* 109/06/15   V1.00.88  Zuwei  fix    coding scan issue                      * 
* 109-07-06   V1.00.02  shiyuqi       updated for project coding standard     *
*  109/09/05  V1.00.06    yanghan     fix code scan issue    
*  110-01-07  V1.00.02    shiyuqi       修改无意义命名                                                                           *
******************************************************************************/

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.Scanner;

import com.AccessDAO;
import com.CommCrd;
import com.CommFunction;

import Dxc.Util.SecurityUtil;

/* 檔案解密處理程式 */
public class SecR002 extends AccessDAO {
  final int DEBUG = 1;
  private final String PROGNAME = "檔案解密處理程式 V1.02.04 93/09/03";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();

  String inputData = "";
  String temstr1 = "";
  String temstr2 = "";
  int int1 = 0;
  int int2 = 0;
  int int3 = 0;
  int intD = 0;
  String hide1 = "";
  String inputDir = "";
  String inputFile = "";
  String outputFile = "";
  String dataBuffer = "";
  String hFepdPawdVer = "";
  String hFepdPawdOp = "";
  String str600 = "";
  String hTempSystime = "";
  int hwdInt1 = 0;
  int hwdInt2 = 0;
  int hwdInt3 = 0;
  String hSdtgDecryptDesc = "";
  String hFepdPawdSec1 = "";
  String hFepdPawdSec2 = "";

  int out = -1;

  // *********************************************************
  public int mainProcess(String[] args) {
    try {
      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + PROGNAME);
      // =====================================
      // 固定要做的

      if (args.length != 1 && args.length != 2) {
        showLogMessage("I", "", "Usage : SecR002 inputdata passwd");
        return 1;
      }

      if (super.conn == null) {
        showLogMessage("I", "", "Pls call setConnection first");
        return -1;
      }
      int rtn = 0;
      // rtn = insert_sec_decrypt_log();
      // if (rtn != 0)
      // return rtn;
      inputData = args[0];

      if (args.length == 1) {

        // temstr1 = getpass("輸入檔案解密密碼 : ");
        Scanner scanner = new Scanner(System.in);
        showLogMessage("I", "", "輸入檔案解密密碼 : ");
        temstr1 = scanner.nextLine();

        byte[] tem1Byte = temstr1.getBytes();
        for (int1 = 0; int1 < temstr1.length(); int1++) {
          int2 = int1 + 1 % 20;
          if (int2 == 0)
            int2 = 20;
          int3 = (byteToUnsignedInt(tem1Byte[int1]) + int2 * 101) % 122;
          if (int3 == 0)
            int3 = 122;
          tem1Byte[int1] = (byte) int3;
        }
        temstr1 = new String(tem1Byte);
        hide1 = temstr1;
      } else {
        hide1 = args[1];
        if (DEBUG == 1)
          showLogMessage("I", "", hide1);
        temstr1 = "";
        intD = hide1.length() / 3;
        for (int1 = 0; int1 < intD; int1++) {
          temstr2 = String.format("%3.3s", hide1.substring(int1 * 3));
          temstr1 += (char) comc.str2int(temstr2);
        }
        hide1 = temstr1;
        /*
         * hide1 = String.format("%s",argv[2]+3); hide1[strlen(hide1)-2]=ECS_NULL;
         * showLogMessage("I", "", String.format("step 0003[%s]",hide1)); for
         * (ftd1=0;ftd1<strlen(hide1);ftd1++) { ftd2= ftd1 % 3; hide1[ftd1]=hide1[ftd1]+ftd2; }
         */
      }
      rtn = selectSecFilePasswd(hide1);
      if (rtn != 0)
        return rtn;

      rtn = readOpen();
      if (rtn != 0)
        return rtn;
      rtn = readFile();
      if (rtn != 0)
        return rtn;

      // chmod(output_file,S_IRUSR|S_IWUSR|S_IXUSR);
      comc.chmodSec(outputFile, false);
      // ==============================================
      // 固定要做的
      showLogMessage("I", "", "程式執行結束");
      // finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      showLogMessage("I", "", ex.getMessage() + "\n" + comc.getStackTraceString(ex));
      // expHandle(ex);
      return 1;
    }
  }

  /**************************************************************************/
  int readOpen() throws Exception {

    inputDir = inputData;
    inputFile = inputData;
    if (inputFile.toCharArray()[inputFile.length() - 1] == '.')
      inputFile = inputFile.substring(0, inputFile.length() - 1);

    int file = openInputText(inputFile);
    if (file == -1) {
      showLogMessage("I", "", String.format("OPEN input file [%s] error", inputData));
      closeInputText(file);
      return 1;
    }
    closeInputText(file);

    if (inputFile.substring(inputFile.length() - 4).equals(".SEC")) {
      outputFile = inputFile;
      outputFile = outputFile.substring(0, inputFile.length() - 4);
    } else {
      outputFile = String.format("%s.UNSEC", inputFile);
    }

    try {
      openBinaryOutput(outputFile);
    } catch (FileNotFoundException exception) {
      showLogMessage("I", "", String.format("OPEN output file [%s] error\n", outputFile));
      return 1;
    }
    return 0;

  }

  /************************************************************************/
  int readFile() throws Exception {
    int recSize = 0, int1a = 0, intnb = 0;
    byte[] temstr = new byte[27];
    String str600 = "";
    int int1 = 0, int2 = 0, int3 = 0, int4 = 0;

    byte[] bytes = new byte[1024];
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(SecurityUtil.verifyPath(inputFile)))) {
      while ((recSize = br.read(bytes, 0, 1024)) > 0) {
        byte[] tmpBytes = new byte[recSize];
        System.arraycopy(bytes, 0, tmpBytes, 0, recSize);

        if (int1a == 0) {
          System.arraycopy(tmpBytes, 0, temstr, 0, 27);
          // temstr = String.format("%27.27s", data_buffer);
          str600 = "";
          int4 = byteToUnsignedInt(temstr[26]) + 23;
          showLogMessage("I", "", "int4 = " + int4);
          if (int4 >= 256)
            int4 = int4 - 256;
          switch (int4) {
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
            default:
              showLogMessage("I", "", String.format("檔案[%s]非ICBC加密檔案", inputData));
              return 1;
          }

          byte[] str600Byte = new byte[27];
          for (int1 = 0; int1 < 27; int1++) {
            int2 = int1 + 1 % 20;
            if (int2 == 0)
              int2 = 20;
            if (byteToUnsignedInt(temstr[int1]) > 122)
              temstr[int1] = (byte) (temstr[int1] - 122);
            int3 = (2440 + byteToUnsignedInt(temstr[int1]) - int2 * intnb) % 122;
            if (int3 == 0)
              int3 = 122;
            str600Byte[int1] = (byte) int3;
          }
          // str600 = new String(str600Byte);
          hTempSystime = new String(str600Byte, 22, 26 - 22);
          str600 = new String(str600Byte, 0, 14);
          str600Byte = null;
          for (int1 = 0; int1 < 14; int1++) {
            if ((str600.toCharArray()[int1] < '0') || (str600.toCharArray()[int1] > '9')) {
              showLogMessage("I", "", String.format("檔案[%s]非ICBC加密檔案", inputData));
              return 1;
            }
          }
          hFepdPawdVer = str600;
          int rtn = getSecFilePasswd();
          if (rtn != 0)
            return rtn;

          byte[] tmpByte = rSecurity(recSize, tmpBytes, hwdInt1, hwdInt2, hwdInt3);

          int1 = comc.str2int(hTempSystime.substring(2)) * 3;
          String tmpstr = String.format("%2.2s", hTempSystime);
          int1 = int1 + comc.str2int(tmpstr) + 27;

          byte[] dataByte = new byte[tmpByte.length - int1];
          System.arraycopy(tmpByte, int1, dataByte, 0, tmpByte.length - int1);
          writeBinFile(dataByte, dataByte.length);
          int1a = 1;
          tmpByte = null;
          dataByte = null;
        } else {
          byte[] dataByte = rSecurity(recSize, tmpBytes, hwdInt1, hwdInt2, hwdInt3);
          writeBinFile(dataByte, dataByte.length);
          dataByte = null;
        }
      }
      br.close();
    }

    closeBinaryOutput();

    return 0;

  }

  /*************************************************************************/
  byte[] rSecurity(int dSize, byte[] dBuffer, int hwdInt1, int hwdInt2, int hwdInt3) {
    byte[] stra = new byte[30];
    int int1;
    int inta, intb, intc;

    inta = hwdInt1 % 8;
    intb = hwdInt2 % 8;
    intc = hwdInt3;

    for (int1 = 0; int1 < dSize; int1++) {
      stra[1] = dBuffer[int1];
      stra[2] = stra[1];
      stra[11] = (byte) (Byte.toUnsignedInt(stra[1]) << 8 - intb);
      stra[12] = (byte) (Byte.toUnsignedInt(stra[2]) >> intb);
      stra[20] = (byte) (byteToUnsignedInt(stra[11]) + byteToUnsignedInt(stra[12]));

      stra[1] = stra[20];
      stra[2] = stra[20];
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
      dBuffer[int1] = (byte) ((byteToUnsignedInt(stra[19]) + byteToUnsignedInt(stra[15])
          + byteToUnsignedInt(stra[17]) + 256 - intc) % 256);
    }

    return dBuffer;

  }

  /*************************************************************************/
  int getSecFilePasswd() throws Exception {
    int int1, int2, int3, int5, int6;
    // unsigned char temp_str[300];
    String tempStr = "";

    hFepdPawdSec1 = "";
    hFepdPawdSec2 = "";
    sqlCmd = " select passwd_sec1, ";
    sqlCmd += "        passwd_sec2 ";
    sqlCmd += " from   sec_log_passwd ";
    sqlCmd += " where  passwd_ver = ? ";
    setString(1, hFepdPawdVer);
    selectTable();

    if (notFound.equals("Y")) {
      showLogMessage("I", "", String.format("select sec_file_passwd error"));
      return 1;
    }
    hFepdPawdSec1 = getValue("passwd_sec1");
    hFepdPawdSec2 = getValue("passwd_sec2");

    int5 = 0;
    tempStr = String.format("%s", hFepdPawdSec1);
    byte[] tmpByte = tempStr.getBytes();
    for (int1 = 0; int1 < hFepdPawdSec1.length(); int1++) {

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
    tempStr = String.format("%s", hFepdPawdSec2);
    tmpByte = tempStr.getBytes();
    for (int1 = 0; int1 < hFepdPawdSec2.length(); int1++) {
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
  int selectSecFilePasswd(String pwdStr1) throws Exception {
    String tmpStr1 = "";

    tmpStr1 = pwdStr1;
    /*
     * for (int1=0;int1<strlen(pwd_str1;int1++) { int2 = int1+1 % 20; if (int2==0) int2=20; int3 =
     * ((unsigned char)pwd_str1[int1] + int2*101) % 122; if (int3==0) int3=122; tmp_str1[int1] =
     * int3; }
     */
    hFepdPawdOp = "";
    sqlCmd = " select passwd_op ";
    sqlCmd += " from   sec_file_passwd ";
    sqlCmd += "fetch first 1 rows only ";
    selectTable();

    if (notFound.equals("Y")) {
      showLogMessage("I", "", String.format("\n檔案加密資訊主管密碼尚未開戶 !"));
      return 1;
    }
    hFepdPawdOp = getValue("passwd_op");

    if (hFepdPawdOp.equals(tmpStr1) == false) {
      showLogMessage("I", "", String.format("\n檔案解密密碼錯誤 !"));
      return 1;
    }
    return 0;
  }

  /*************************************************************/
  public void setConnection(Connection conn[], String[] dbAlias) throws Exception {
    // TODO Auto-generated constructor stub
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    return;
  }

  /*************************************************************************/
  int insertSecDecryptLog() throws Exception {
    long nUserpid;
    int int1 = 0;
    String template = "template=XXXXXX";
    String temFile = "";
    /*
     * fd = mkstemp(template); close(fd);
     */
    nUserpid = comc.getPID();

    template = String.format("%08d", nUserpid);
    str600 =
        String.format("ps -ef|grep -v grep|grep %d|grep -v ofc_callbatch|grep sec_r002 1>/tmp/%s",
            nUserpid, template);
    showLogMessage("I", "", "str600 command = " + str600);
    comc.systemCmd(str600);

    temFile = String.format("/tmp/%s", template);

    int file = openInputText(temFile);
    if (file == -1) {
      showLogMessage("I", "", String.format("OPEN temp file [%s] error", template));
      closeInputText(file);
      return 1;
    }
    closeInputText(file);

    byte[] bytes = new byte[600];
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(temFile))) {
      while (br.read(bytes, 0, 600) > 0) {
        dataBuffer = new String(bytes);
        byte[] tmpbyte = dataBuffer.getBytes();
        for (int1 = dataBuffer.length() - 1; int1 >= 0; int1--)
          if ((tmpbyte[int1] < 20) || (tmpbyte[int1] > 120)) {
            tmpbyte = comc.subArray(tmpbyte, 0, int1);
          } else {
            break;
          }
        dataBuffer = new String(tmpbyte);
        if (dataBuffer.length() < 10)
          continue;

        comc.rtrim(dataBuffer);

        hSdtgDecryptDesc = dataBuffer;

        daoTable = "sec_decrypt_log";
        setValue("decrypt_date", sysDate + sysTime);
        setValue("decrypt_desc", hSdtgDecryptDesc);
        setValue("mod_pgm", javaProgram);

        insertTable();

        if (dupRecord.equals("Y")) {
          showLogMessage("I", "", "insert sec_decrypt_log duplicate!");
          return 1;
        }
      }

      br.close();
    }


    comc.fileDelete(temFile);
    return 0;
  }

  // **********************************************************
  private int byteToUnsignedInt(byte bytes) {
    return 0x00 << 24 | bytes & 0xff;
  }
  /***********************************************************************/
}
