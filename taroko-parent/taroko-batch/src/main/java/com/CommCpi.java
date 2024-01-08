/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/06/15   V1.00.88  Zuwei  fix    coding scan issue                      *
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V0.00.03    Zuwei     coding standard, rename field method & format                   *
*  110-01-07   V1.00.04    shiyuqi       修改无意义命名                                         
* 111-01-21  V1.00.05  Justin       fix Redundant Null Check                                  *
******************************************************************************/
package com;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Arrays;

public class CommCpi extends AccessDAO {
  private byte[] ebc = {(byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
      (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
      (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
      (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
      (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
      (byte) 0x5A, (byte) 0x7F, (byte) 0x7B, (byte) 0x5B, (byte) 0x6C, (byte) 0x50, (byte) 0x7D,
      (byte) 0x4D, (byte) 0x5D, (byte) 0x5C, (byte) 0x4E, (byte) 0x6B, (byte) 0x60, (byte) 0x4B,
      (byte) 0x61, (byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5,
      (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, (byte) 0x7A, (byte) 0x5E, (byte) 0x4C,
      (byte) 0x7E, (byte) 0x6E, (byte) 0x6F, (byte) 0x7C, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3,
      (byte) 0xC4, (byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0xD1,
      (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8,
      (byte) 0xD9, (byte) 0xE2, (byte) 0xE3, (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7,
      (byte) 0xE8, (byte) 0xE9, (byte) 0x4A, (byte) 0xE0, (byte) 0x4F, (byte) 0x5F, (byte) 0x6D,
      (byte) 0x79, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86,
      (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x91, (byte) 0x92, (byte) 0x93, (byte) 0x94,
      (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98, (byte) 0x99, (byte) 0xA2, (byte) 0xA3,
      (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8, (byte) 0xA9, (byte) 0xC0,
      (byte) 0x6A, (byte) 0xD0, (byte) 0xA1, (byte) 0x40};
  byte big1, big2, ibm1, ibm2;
  private long val, bigLoc, rem, extra, offset, iic;
  int ii, kk, bigtmp;
  int cnt, lp;
  byte[] conStr = new byte[1600];

  /***********************************************************************/


  public byte[] big5Ibm(byte[] cvtData) {

    int cnt = cvtData.length;
    ii = 0;

    if (cnt > 1600)
      return null;
    System.arraycopy(cvtData, 0, conStr, 0, cvtData.length);

    while (true) {
      if (ii >= cnt) {
        break;
      }

      byte bytes = conStr[ii];
      if (bytes == (byte) 0x0E) {
        conStr[ii++] = (byte) 0x0E;
      } else if (bytes == (byte) 0x0F) {
        conStr[ii++] = (byte) 0x0F;
      } else if (byteToUnsignedInt(bytes) > 127) {
        processBig2ibm();
      } else {
        conStr[ii++] = ebc[bytes];
      }
    }

    byte[] rtn = new byte[ii];
    System.arraycopy(conStr, 0, rtn, 0, rtn.length);

    return rtn;
  }

  private void processBig2ibm() {
    kk = ii;
    big1 = conStr[ii++];
    big2 = conStr[ii++];
    int nBig1 = byteToUnsignedInt(big1);
    int nBig2 = byteToUnsignedInt(big2);
    val = nBig1 * 256 + nBig2;

    if ((val > 42047 && val < 50815) || (val > 51519 && val < 63958)) {
      if (((nBig1 > 163 && nBig1 < 199) || (nBig1 > 200 && nBig1 < 250))
          && ((nBig2 > 63 && nBig2 < 127) || (nBig2 > 160 && nBig2 < 255))) {
        convertIbmCode();
      }
    } else if (val > 41309 && val < 41704) {
      specialIbm();
    } else {
      ibm1 = 0x40;
      ibm2 = 0x40;
    }

    conStr[kk++] = ibm1;
    conStr[kk] = ibm2;
    return;
  }

  private void convertIbmCode() {
    if (val < 50815) {
      offset = 42047;
      extra = 0;
    } else {
      offset = 51519;
      extra = 5401;
    }

    val -= offset;
    bigtmp = (int) (val / 256);
    rem = val % 256;

    if (rem > 63) {
      rem -= 34;
    }

    bigLoc = bigtmp * 157 + rem + extra;

    if (bigLoc > 5401) {
      bigLoc += 51;
    }

    iic = bigLoc / 188;
    rem = bigLoc % 188;

    if (rem == 0) {
      iic--;
      rem = 188;
    }

    if (iic > 29) {
      iic -= 29;
      val = 26944;
    } else {
      val = 19520;
    }

    if (rem > 63) {
      rem++;
    }

    val = val + iic * 256 + rem;
    ibm1 = (byte) (val / 256);
    ibm2 = (byte) (val % 256);

    return;
  }

  private void specialIbm() {
    int lp = 0;
    for (lp = 0; lp <= 9; lp++) {
      if (big1 == 0xA2 && big2 == 0xAF + lp) {
        ibm1 = 0x42;
        ibm2 = (byte) (0xF0 + lp);
        return;
      }
    }

    for (lp = 0; lp <= 8; lp++) {
      if (big1 == 0xA2 && big2 == 0xCF + lp) {
        ibm1 = 0x42;
        ibm2 = (byte) (0xC1 + lp);
        return;
      }
    }

    for (lp = 0; lp <= 8; lp++) {
      if (big1 == 0xA2 && big2 == 0xD8 + lp) {
        ibm1 = 0x42;
        ibm2 = (byte) (0xD1 + lp);
        return;
      }
    }

    for (lp = 0; lp <= 7; lp++) {
      if (big1 == 0xA2 && big2 == 0xE1 + lp) {
        ibm1 = 0x42;
        ibm2 = (byte) (0xE2 + lp);
        return;
      }
    }

    if (big1 == 0xA1 && big2 == 0x5D) {
      ibm1 = 0x42;
      ibm2 = 0x4B;
      return;
    }

    if (big1 == 0xA1 && big2 == 0x5E) {
      ibm1 = 0x42;
      ibm2 = 0x5D;
      return;
    }

    return;
  }

  private int byteToUnsignedInt(byte b) {
    return 0x00 << 24 | b & 0xff;
  }
  // *****************************************************************************

  public String getIPbyName(String name) {
    try {

      InetAddress inetAddr = InetAddress.getByName(name);

      byte[] addr = inetAddr.getAddress();
      // Convert to dot representation
      String ipAddr = "";
      for (int i = 0; i < addr.length; i++) {
        if (i > 0) {
          ipAddr += ".";
        }
        ipAddr += addr[i] & 0xFF;
      }
      // System.out.println("IP Address: " + ipAddr);
      return ipAddr;
    } catch (UnknownHostException e) {
      System.out.println("Host not found: " + e.getMessage());
      return "";
    }
  }

  // ***************************************************************************

  public void logPrintf(int intType, String msg, String filename) {
    String temstr = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
    dateTime();
    String tmpstr =
        String.format("%s[%06d](%s %s) \n", msg, getPID(), sysDate.substring(4), sysTime);
    try {
      File f = new File(temstr);
      if (f.exists()) {
        Files.write(Paths.get(temstr), tmpstr.getBytes(), StandardOpenOption.APPEND);
      } else {
        Files.write(Paths.get(temstr), tmpstr.getBytes(), StandardOpenOption.CREATE_NEW);
      }

    } catch (IOException e) {
      // exception handling left as an exercise for the reader
    }
  }

  /*************************************************************************/
  public String toHex(byte[] in) {
    if (in != null) {
      StringBuffer sb = new StringBuffer(6 * in.length);
      for (int i = 0; i < in.length; ++i) {
        sb.append(Integer.toHexString(in[i] & 0xFF) + ((i == in.length - 1) ? "" : ","));
      }
      return sb.toString();
    }

    return "";
  }

  /***********************************************************************/
  public byte[] bytesTrim(byte[] bytes) {
    int i = bytes.length - 1;
    while (i >= 0 && bytes[i] == 0) {
      --i;
    }

    return Arrays.copyOf(bytes, i + 1);
  }

  /***********************************************************************/
  public void hexDump(int logStatus, String prompt, byte[] buf, String filename)
      throws Exception {

    int i = 0;
    byte[] ascBuffer = new byte[17];
    String[] hexData = toHex(buf).split(",");
    String buffer = "";
    int len = buf.length;
    logPrintf(0, String.format("HEX_DUMP [%-11s] Len=(%d)", prompt, len), filename);
    for (i = 0; i < len; i++) {
      if ((i % 16) == 0 && i != 0) {

        logPrintf(0, String.format("%-3s%04X %-48s %.16s", " ", i / 16, buffer,
            new String(bytesTrim(ascBuffer), "big5")), filename);
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
          new String(bytesTrim(ascBuffer), "big5")), filename);
    }
    ascBuffer = null;
    hexData = null;

  }

  /***********************************************************************/
  public void hexDump(int logStatus, String prompt, byte[] buf, int len, String filename)
      throws Exception {
    if (buf == null)
      return;
    byte[] rtn = null;
    if (len != 0) {
      if (buf.length > len) {
        rtn = new byte[len];
        System.arraycopy(buf, 0, rtn, 0, len);
        hexDump(logStatus, prompt, rtn, filename);
      } else {
        hexDump(logStatus, prompt, buf, filename);
      }
    } else
      hexDump(logStatus, prompt, buf, filename);
  }

  /***********************************************************************/
  public void hexDump(String prompt, byte[] buf, int len) throws Exception {
    if (buf == null)
      return;
    byte[] rtn = null;
    if (len != 0) {
      if (buf.length > len) {
        rtn = new byte[len];
        System.arraycopy(buf, 0, rtn, 0, len);
        hexDump(prompt, rtn);
      } else {
        hexDump(prompt, buf);
      }
    } else
      hexDump(prompt, buf);

  }

  /***********************************************************************/
  public void hexDump(String prompt, byte[] buf) throws Exception {
    String temp = "";
    if (buf == null)
      return;

    temp = String.format("HEX_DUMP [%s] buf=[%s]", prompt, new String(buf, "big5"));
    showLogMessage("I", "", temp);

    String hexData = toHex(buf);

    temp = String.format("%7s hex data = [%s]\r\n", " ", hexData);
    showLogMessage("I", "", temp);

  }

  /***********************************************************************/
  public String ebcAsc(String conStr) throws UnsupportedEncodingException {

    byte[] asc =
        "                                                                          [.<(+]&         !$*);^-/        |,%_>?         `:#@'=#0abcdefghi       jklmnopqr       ~stuvwxyz                      {ABCDEFGHI      }JKLMNOPQR      #0STUVWXYZ      0123456789      "
            .getBytes("ASCII");

    byte[] bytes = conStr.getBytes("ASCII");
    byte[] rtn = new byte[bytes.length];

    for (int i = 0; i < bytes.length; i++) {
      int j = byteToUnsignedInt(bytes[i]);
      rtn[i] = asc[j];
    }
    return new String(rtn, "ASCII");

  }

  /***********************************************************************/
  public String ebcAsc(byte[] conStr) throws UnsupportedEncodingException {

    byte[] asc =
        "                                                                          [.<(+]&         !$*);^-/        |,%_>?         `:#@'=#0abcdefghi       jklmnopqr       ~stuvwxyz                      {ABCDEFGHI      }JKLMNOPQR      #0STUVWXYZ      0123456789      "
            .getBytes("ASCII");

    byte[] rtn = new byte[conStr.length];

    for (int i = 0; i < conStr.length; i++) {
      int j = byteToUnsignedInt(conStr[i]);
      rtn[i] = asc[j];
    }
    return new String(rtn, "ASCII");

  }

  /***
   * 
   */
  public class RtnDecCvrt {
    public String output = "";
    public long num = 0;
  }

  public RtnDecCvrt decCvrt(int codeint, int strLength, long inputnum) {
    RtnDecCvrt rtn = new RtnDecCvrt();
    byte[] outputstr = new byte[5];
    String strCodNum = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    long calNum, addNum, totalNum;
    int calLen = 1, int1, int2, int3, nowInt;
    long ldivQuot, ldivRem;

    byte[] codNum = strCodNum.getBytes();

    if (inputnum != 0) {
      for (int i = 0; i < outputstr.length; i++) {
        outputstr[i] = ' ';
      }

      calNum = inputnum;

      while (calNum >= codeint) {
        ldivQuot = calNum / codeint;
        ldivRem = calNum % codeint;
        outputstr[strLength - calLen] = codNum[(int) ldivRem];
        calLen++;
        calNum = ldivQuot;
      }
      outputstr[strLength - calLen] = codNum[(int) calNum];

      for (int1 = 0; int1 < strLength; int1++) {
        if (outputstr[int1] != ' ')
          break;
        outputstr[int1] = '0';
      }
    } else if ((inputnum == 0) && (outputstr.length != 0)) {
      nowInt = 0;
      inputnum = 0;
      calLen = outputstr.length - 1;
      for (int1 = calLen; int1 >= 0; int1--) {
        nowInt++;
        for (int2 = 0; int2 < codeint; int2++) {
          if (outputstr[int1] == codNum[int2])
            break;
        }

        if (int2 >= 36) {
          rtn.num = inputnum;
          rtn.output = new String(outputstr);
          return rtn;
        }
        addNum = int2;
        for (int3 = 1; int3 < nowInt; int3++) {
          addNum = addNum * codeint;
        }
        inputnum = inputnum + addNum;
      }
    } else if ((inputnum == 0) && (outputstr.length == 0)) {
      for (int i = 0; i < strLength; i++) {
        outputstr[i] = '0';
      }
    }

    rtn.num = inputnum;
    rtn.output = new String(outputstr);
    rtn.output = ""; // no use

    return rtn;
  }

  /***
   * 半形轉全形
   * 
   * @param data1
   * @return
   */
  public String commTransChinese(String data1) {

    if (data1 == null || data1.equals("")) {
      return "";
    }

    char[] chars = data1.toCharArray();

    for (int i = 0; i < chars.length; i++) {

      if (chars[i] > '\200') {
        continue;
      }
      if (chars[i] == 32) {
        chars[i] = (char) 12288;
        continue;
      }
      if (Character.isLetterOrDigit(chars[i])) {
        chars[i] = (char) (chars[i] + 65248);
        continue;
      }

      chars[i] = (char) 12288;
    }

    return String.valueOf(chars);
  }

}
