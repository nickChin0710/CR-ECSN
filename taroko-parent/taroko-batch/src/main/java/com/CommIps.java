/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/07/03  V1.00.00    Zuwei     fix compile error                           *
*  109/07/06  V0.00.01    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V0.00.02    Zuwei     coding standard, rename field method                   *
*  110-01-07   V1.00.02    shiyuqi       修改无意义命名                             
*  111-01-19  V1.00.03    Justin      fix Unchecked Return Value             *
* 111-01-21  V1.00.04  Justin       fix Redundant Null Check
 ******************************************************************************/
package com;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Arrays;

import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGateway;

public class CommIps extends AccessDAO {
  /***********************************************************************/
  public class returnData {
    public String mData;
    public String mItalCol002;
  }

  /***********************************************************************/
  private String logFilename = "";
  CommCrd comc = new CommCrd();
  public AuthGateway authGatewayTest = new AuthGateway();
  public AuthData lAuthData = new AuthData();
  public IpassFormat IpassFormat = null;

  public boolean DEBUG1 = false;
  public String prgmId = "";

  public String hCardCurrentCode = "";
  public String hCardCardNo = "";
  public String hCardPSeqno = "";
  public String hCardNewEndDate = "";
  public String hCardTransCvv2 = "";
  public String hCardActivateFlag = "";

  public String hIardNewEndDate = "";
  public String hIardAutoloadFlag = "";
  public String hIardBlackltFlag = "";
  public String hIardIpsOppostDate = "";
  public String hIardRowid = "";

  public String hIotgIpsCardNo = "";

  public String hItalMsgTypeId = "";
  public String hItalBitmap1 = "";
  public String hItalBitmap2 = "";
  public String hItalCol002 = "";
  public String hItalCol003 = "";
  public String hItalCol004 = "";
  public String hItalCol007 = "";
  public String hItalCol007D = "";
  public String hItalCol007T = "";
  public String hItalCol011 = "";
  public String hItalCol013 = "";
  public String hItalCol014 = "";
  public String hItalCol036 = "";
  public String hItalCol039 = "";
  public String hItalCol041 = "";
  public String hItalCol045 = "";
  public String hItalCol063 = "";
  public String hItalCol070 = "";
  public String hItalCol073 = "";
  public String hItalCol091 = "";

  // public rcvBuf rcv_data = null;
  // public sndBuf snd_data = null;

  public String ccasRespCode = "";
  public String ccasAuthCode = "";

  public String hItalModPgm = "";
  public String hIclgCardNo = "";
  public String hIclgExpireDate = "";
  public double hIclgTransAmount = 0;
  public String hIclgLocalTime = "";
  public String hIclgOrgRefNo = "";
  public String hIclgAuthCode = "";
  public String hIclgCcasRespCode = "";

  public String hOwsmWfValue;
  public String hOwsmWfValue2;

  /***********************************************************************/
  public byte[] bytesTrim(byte[] bytes) {
    int i = bytes.length - 1;
    while (i >= 0 && bytes[i] == 0) {
      --i;
    }

    return Arrays.copyOf(bytes, i + 1);
  }

  /***********************************************************************/
  public void setLogFilename(String filename) {
    logFilename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
    File f = new File(logFilename);
    boolean result = f.getParentFile().mkdirs();
	if (result == false) {
		if (f.getParentFile().exists() == false) {
			showLogMessage("I", "", "Fail to create directories");
		}
	}
  }

  /***********************************************************************/
  public returnData returnVal() {
    returnData rtnData = new returnData();
    rtnData.mData = IpassFormat.isoString;
    rtnData.mItalCol002 = hItalCol002;
    showLogMessage("I", "",
        "[DEBUG]rtnData.m_data = ["
            + rtnData.mData
            + "], rtnData.m_ital_col002 = ["
            + rtnData.mItalCol002
            + "]");
    return rtnData;
  }

  /*************************************************************************/
  public int recvData(byte[] rcvByte) {
    showLogMessage("I", "", "[DEBUG]rcv_byte = " + rcvByte);
    IpassFormat = new IpassFormat(rcvByte.length, rcvByte);
    IpassFormat.iso2Host();

    dateTime();
    //
    hItalMsgTypeId = IpassFormat.mesgType;
    // h_ital_bitmap1 = IpassFormat.byteMap;
    hItalBitmap1 = IpassFormat.hItalBitmap1;

    /****************
     * initial field
     **********************************************/
    hItalCol002 = "";
    hItalCol003 = "";
    hItalCol004 = "";
    hItalCol007 = "";
    hItalCol007D = "";
    hItalCol007T = "";
    hItalCol011 = "";
    hItalCol013 = "";
    hItalCol014 = "";
    hItalCol036 = "";
    hItalCol039 = "00";
    hItalCol041 = "";
    hItalCol045 = "";
    hItalCol063 = "";
    hItalCol070 = "";
    hItalCol073 = "";
    hItalCol091 = "";
    /*****************************************************************************/

    String[] rcvArr = IpassFormat.isoField;
    // for (int i = 0; i < IpassFormat.isoField.length; i++)
    // showLogMessage("I", "", "[DEBUG]IpassFormat.isoField[" + i + "] = " +
    // IpassFormat.isoField[i]);
    hItalCol002 = rcvArr[2];
    hItalCol003 = rcvArr[3];

    hItalCol004 = rcvArr[4];
    rcvArr[4] = comc.getSubString(rcvArr[4], 0, 10);

    hItalCol007 = rcvArr[7];
    hItalCol007D = String.format("%4.4s", rcvArr[7]);
    hItalCol007T = String.format("%6.6s", comc.getSubString(rcvArr[7], 4));

    hItalCol011 = rcvArr[11];
    hItalCol013 = rcvArr[13];
    hItalCol014 = rcvArr[14];
    hItalCol036 = rcvArr[36];
    hItalCol039 = rcvArr[39];
    hItalCol041 = rcvArr[41];
    hItalCol045 = rcvArr[45];
    hItalCol063 = rcvArr[63];
    hItalCol070 = rcvArr[70];
    hItalCol073 = rcvArr[73];
    hItalCol091 = rcvArr[91];

    return (0);
  }

  /*****************************************************************************/
  public byte[] commHashUnpack(byte[] tmpstr1) {
    byte[] tmpstr3 = commHashBytes(tmpstr1);
    byte[] tmpstr2 = commBytesBitmap(tmpstr3);
    return tmpstr2;
  }

  /****************************************************************/
  public byte[] commHashBytes(byte[] tmpstr1) {
    int quot = 0;
    int rem = 0;
    int cmpCnt;
    byte[] hashSum = new byte[10];
    byte[] tmpData1 = new byte[20];
    byte[] tmpData2 = new byte[20];
    byte[] tmpstr3 = new byte[1000];
    byte[] tmpstr2 = new byte[1000];

    quot = tmpstr1.length / 8;
    rem = tmpstr1.length % 8;
    Arrays.fill(tmpstr3, (byte) '\0');
    System.arraycopy(tmpstr1, 0, tmpstr3, 0, tmpstr1.length);
    if (rem == 0)
      cmpCnt = quot;
    else
      cmpCnt = quot + 1;

    System.arraycopy(tmpstr1, 0, hashSum, 0, 8);
    for (int int1 = 1; int1 < cmpCnt; int1++) {
      System.arraycopy(hashSum, 0, tmpData1, 0, 8);
      System.arraycopy(tmpstr3, int1 * 8, tmpData2, 0, 8);
      for (int int2 = 0; int2 < 8; int2++)
        hashSum[int2] = (byte) (tmpData1[int2] ^ tmpData2[int2]);
    }

    tmpstr3 = null;
    System.arraycopy(hashSum, 0, tmpstr2, 0, 8);
    return tmpstr2;
  }

  /*****************************************************************************/
  public byte[] commBytesBitmap(byte[] tmpstr1) {
    String tmpaa = "";

    for (int inta = 0; inta < 8; inta++) {
      String tmpbb = String.format("%02X", tmpstr1[inta]);
      tmpaa = tmpaa + tmpbb;
    }
    return tmpaa.getBytes();
  }

  /*************************************************************************/
  public int callCcas(int hInt) throws Exception {
    String tmpstr = "";

    tmpstr = String.format("%1d", hInt);
    lAuthData.setTransType(tmpstr);

    String sLTranxResult = authGatewayTest.startProcess(lAuthData, hOwsmWfValue, hOwsmWfValue2);
    tmpstr = sLTranxResult.substring(0, 2);
    ccasRespCode = tmpstr;

    if (tmpstr.equals("DM")) {
      hItalCol039 = "51"; /* 授權失敗 */
      return (1);
    } else if (tmpstr.equals("00") == false && tmpstr.equals("85") == false) {
      hItalCol039 = "57"; /* 授權失敗 */
      return (1);
    }

    if (tmpstr.equals("85"))
      hItalCol039 = "00";

    tmpstr = sLTranxResult.substring(2, 8);
    ccasAuthCode = tmpstr;

    return (0);
  }

  /*************************************************************************/
  public String toHex(byte[] bytes) {
	    if (bytes != null) {
	      StringBuffer sb = new StringBuffer(6 * bytes.length);
	      for (int i = 0; i < bytes.length; ++i) {
	        sb.append(Integer.toHexString(bytes[i] & 0xFF) + ((i == bytes.length - 1) ? "" : ","));
      }
      return sb.toString();
    }

    return "";
  }

  /*************************************************************************/
  public int byteToUnsignedInt(byte bytes) {
    return 0x00 << 24 | bytes & 0xff;
  }

  /*************************************************************************/
  public void hexDump(String prompt, byte[] buf) throws Exception {
    int i = 0;
    byte[] ascBuffer = new byte[17];
    String[] hexData = toHex(buf).split(",");
    String buffer = "";
    int len = buf.length;
    logPrintf(0, String.format("HEX_DUMP [%-11s] Len=(%d)", prompt, len));
    for (i = 0; i < len; i++) {
      if ((i % 16) == 0 && i != 0) {

        logPrintf(0, String.format("%-3s%04X %-48s %.16s", " ", i / 16, buffer,
            new String(bytesTrim(ascBuffer), "big5")));
        buffer = "";
        Arrays.fill(ascBuffer, (byte) '\0');
      }
      buffer = buffer + String.format("%2.2s ", hexData[i]);

      int nAscii = byteToUnsignedInt(buf[i]);
      if (nAscii > 128) {
        ascBuffer[i % 16] = buf[i];
      } else {
        ascBuffer[i % 16] = (byte) (buf[i] >= 0x20 && buf[i] <= 0x7E ? buf[i] : '.');
      }
    }

    if (buffer.length() != 0) {
      logPrintf(0, String.format("%-3s%04X %-48s %.16s", " ", i / 16 + 1, buffer,
          new String(bytesTrim(ascBuffer), "big5")));
    }
    ascBuffer = null;
    hexData = null;
  }

  /*************************************************************************/
  public int selectPtrSysParmCcaslink() throws Exception {
    hOwsmWfValue = "";
    hOwsmWfValue2 = "";
    sqlCmd = "SELECT wf_value, wf_value2 "
        + "FROM ptr_sys_parm "
        + "WHERE  wf_parm = 'SYSPARM' AND wf_key = 'CCASLINK'";
    if (selectTable() > 0) {
      hOwsmWfValue = getValue("wf_value");
      hOwsmWfValue2 = getValue("wf_value2");
    } else {
      hItalCol039 = "96"; /* 系統錯誤 */
      return (-1);
    }
    return (0);
  }

  /*************************************************************************/
  public int insertIpsTcpLog() throws Exception {
    daoTable = "ips_tcp_log";
    setValue("crt_date", sysDate);
    setValue("crt_time", sysTime);
    setValue("msg_type_id", hItalMsgTypeId);
    setValue("bitmap1", hItalBitmap1);
    setValue("bitmap2", hItalBitmap2);
    setValue("col002", hItalCol002);
    setValue("col003", hItalCol003);
    setValue("col004", hItalCol004);
    setValue("col007", hItalCol007);
    setValue("col007_d", hItalCol007D);
    setValue("col007_t", hItalCol007T);
    setValue("col011", hItalCol011);
    setValue("col013", hItalCol013);
    setValue("col014", hItalCol014);
    setValue("col036", hItalCol036);
    setValue("col039", hItalCol039);
    setValue("col041", hItalCol041);
    setValue("col045", hItalCol045);
    setValue("col063", hItalCol063);
    setValue("col070", hItalCol070);
    setValue("col073", hItalCol073);
    setValue("col091", hItalCol091);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", hItalModPgm);
    insertTable();
    if (dupRecord.equals("Y")) {
      hItalCol039 = "96"; /* 系統錯誤 */
      return (-1);
    }

    return (0);
  }

  /***********************************************************************/
  public int insertIpsCcasLog() throws Exception {
    setValue("crt_date", sysDate);
    setValue("crt_time", sysTime);
    setValue("card_no", hIclgCardNo);
    setValue("ips_card_no", hItalCol002);
    setValue("msg_type", hItalMsgTypeId);
    setValue("c36_track_3_data", hItalCol036);
    setValue("c39_resp_code", hItalCol039);
    setValue("c41_term_id", hItalCol041);
    setValue("c45_track_1_data", hItalCol045);
    setValue("c63_system_id", hItalCol063);
    setValue("ips_tran_date", hItalCol013 + hItalCol007D);
    setValue("ips_tran_time", hItalCol007T);
    setValue("expire_date", hIclgExpireDate);
    setValueDouble("trans_amount", hIclgTransAmount);
    setValue("local_time", hIclgLocalTime);
    setValue("proc_flag", "N");
    // setValue("auth_code", null);
    // setValue("ccas_resp_code", null);
    setValue("org_ref_no", hIclgOrgRefNo);
    setValue("reverse_flag", "N");
    // setValue("reverse_date", null);
    // setValue("reverse_time", null);
    setValue("mod_pgm", hItalModPgm);
    daoTable = "ips_ccas_log";
    insertTable();
    if (dupRecord.equals("Y")) {
      hItalCol039 = "96"; /* 系統錯誤 */
      return (-1);
    }

    return (0);
  }

  // ************************************************************************
  public int str2int(String val) {
    int rtn = 0;
    try {
      rtn = Integer.parseInt(val.replaceAll(",", "").trim());
    } catch (Exception e) {
      rtn = 0;
    }
    return rtn;
  }

  /***********************************************************************/
  public void logPrintf(int intType, String buf) {
    boolean bWrite = false;
    dateTime();

    String bufb = String.format("\t[%s %s] %s\n", sysDate, sysTime, buf);

    // int_type = 1;
    // if ((sw_check.equals("Y")) || (sw_check.equals("T"))) int_type = 0;
    switch (intType) {
      case 0:
      case 1:
      case 2:
        bWrite = true;
        break;
    }

    if (bWrite) {
      // try {
      // File f = new File(log_filename);
      // if (f.exists()) {
      // Files.write(Paths.get(log_filename), bufb.getBytes(), StandardOpenOption.APPEND);
      // } else {
      // Files.write(Paths.get(log_filename), bufb.getBytes(), StandardOpenOption.CREATE_NEW);
      // }
      //
      //
      // } catch (IOException e) {
      // // exception handling left as an exercise for the reader
      // System.out.println(String.format("寫檔[%s]失敗", log_filename));
      // return;
      // }
      showLogMessage("I", "", bufb);
    }

  }

}
