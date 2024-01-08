/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/04/12  V1.00.01  Lai        Initial                                    *
* 108/11/29  V1.00.02  Brian      add method encryptForDb() & decryptForDb() *
* 109/04/10  V1.00.03  Pino       add method isValidNewId()                  *
* 109/06/15   V1.00.88  Zuwei  fix    coding scan issue                      *
* 109/06/29  V1.01.00  Wilson     銀行代碼改006                                                                                             * 
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/07/16  V1.01.01  JustinWu listFS : check is listFile null
*  109/07/22  V0.01.02    Zuwei     coding standard, rename field method                    *  
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  109-09-04  V1.02.01  yanghan     处理Portability Flaw: File Separator 问题       *
*  110-01-07   V1.00.02    shiyuqi       修改无意义命名                           
*  111-01-19  V1.02.02   Justin       fix Missing Check against Null          *  
*  111-01-19  V1.00.03    Justin      fix Unchecked Return Value             *
* 111-01-21  V1.00.04  Justin       fix Redundant Null Check
*  111-02-07  V1.00.05   Justin     fix Unchecked Return Value
*  111-02-14  V1.00.06   Alex       add file sort                            *
*  111-02-14  V1.00.07   Justin     big5 -> MS950
*  111-02-18  V1.00.08   Alex       add transTelNo                           *
*****************************************************************************/
package com;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.channels.*;
import java.nio.charset.*;
// add by davidfu@20170515
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.text.ParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import Dxc.Util.SecurityUtil;

// add by davidfu@20170515
//import com.bank.encoding.BankEncoding;

public class CommCrd extends AccessDAO {
  public final String TSCC_BANK_ID8 = "00600000";
  public final String IPS_BANK_ID4 = "0006";
  public final String ICH_BANK_ID3 = "006";
  public final String TSCC_BANK_ID2 = "58";
  public int maxSpace = 500, maxZero = 30;
  private int zeroControl = 0, spaceControl = 0;
  private String spaces = "", zeros = "";
  FileLock lock;
  FileChannel channel;
//  BankEncoding mBankEnc = new BankEncoding();
  private static final Map<String, String> numTable = new HashMap<String, String>() {
    {
      put("A", "10");
      put("B", "11");
      put("C", "12");
      put("D", "13");
      put("E", "14");
      put("F", "15");
      put("G", "16");
      put("H", "17");
      put("J", "18");
      put("K", "19");
      put("L", "20");
      put("M", "21");
      put("N", "22");
      put("P", "23");
      put("Q", "24");
      put("R", "25");
      put("S", "26");
      put("T", "27");
      put("U", "28");
      put("V", "29");
      put("X", "30");
      put("Y", "31");
      put("W", "32");
      put("Z", "33");
      put("I", "34");
      put("O", "35");

    }
  };

  // ************************************************************************
//  public CommCrd() {
//    try {
//      String tmpstr = System.getenv("PROJ_HOME") + "/lib/";
//      mBankEnc.mgcInitialize(tmpstr + "MGC_CHARDATA.MGC");
//    } catch (Exception ex) {
//
//    } finally {
//
//    }
//    return;
//  }

  // ************************************************************************
  // 將big5字串，透過bank提供的api，轉換成utf8字串
  // 針對主機下傳之BIG-5找無對應之UNICODE一般字區時，會轉為★(API規則)
  // MGC_CHARDATA.MGC要放在與bank-encoding.jar相同路徑(API規則)
  // 如果呼叫API function失敗，回傳null
  // add by davidfu@20170515
  // ************************************************************************
//  public String bankEncode(String big5Str) {
//    try {
//
//      byte[] bytesBig5Data = big5Str.getBytes("MS950");
//      byte[] result = mBankEnc.getMgcBytes(BankEncoding.MGCCODE_ENCODE_BIG5,
//          BankEncoding.MGCCODE_ENCODE_UTF8, 0, bytesBig5Data);
//      int retCode = mBankEnc.getLastStatusCode();
//      if (retCode != BankEncoding.MGCCODE_SUCCESS && retCode != BankEncoding.MGCCODE_SOSI_FIXED) {
//        System.out.println("bankEnc.getMgcBytes get error");
//        return null;
//      }
//
//      String sUtf8 = new String(result, StandardCharsets.UTF_8);
//      return sUtf8;
//    } catch (Exception ex) {
//      System.out.println("bankEncode error : " + ex.getMessage());
//      return null;
//    }
//  }

//  public byte[] bankEncodeIBM(String big5Str) {
//    try {
//      // BankEncoding bankEnc = new BankEncoding();
//      byte[] bytesBig5Data = big5Str.getBytes("MS950");
//      byte[] result = mBankEnc.getMgcBytes(BankEncoding.MGCCODE_ENCODE_BIG5,
//          BankEncoding.MGCCODE_ENCODE_IBM, 0, bytesBig5Data);
//      int retCode = mBankEnc.getLastStatusCode();
//      if (retCode != BankEncoding.MGCCODE_SUCCESS && retCode != BankEncoding.MGCCODE_SOSI_FIXED) {
//        System.out.println("bankEnc.getMgcBytes get error");
//        return null;
//      }
//
//      return result;
//    } catch (Exception ex) {
//      System.out.println("bankEncode error : " + ex.getMessage());
//      return null;
//    }
//  }

  // 將utf8字串，透過bank提供的api，轉換成big5字串
  // 針對主機下傳之BIG-5找無對應之UNICODE一般字區時，會轉為★(API規則)
  // MGC_CHARDATA.MGC要放在與bank-encoding.jar相同路徑(API規則)
  // 如果呼叫API function失敗，回傳null
  // add by davidfu@20170724
//  public static String bankDecode(String utf8Str) {
//    try {
//      BankEncoding bankEnc = new BankEncoding();
//      byte[] bytesUtf8Data = utf8Str.getBytes("UTF-8");
//      byte[] result = bankEnc.getMgcBytes(BankEncoding.MGCCODE_ENCODE_UTF8,
//          BankEncoding.MGCCODE_ENCODE_BIG5, 0, bytesUtf8Data);
//      int retCode = bankEnc.getLastStatusCode();
//      if (retCode != BankEncoding.MGCCODE_SUCCESS && retCode != BankEncoding.MGCCODE_SOSI_FIXED) {
//        System.out.println("bankEnc.getMgcBytes get error");
//        return null;
//      }
//
//      String sbig5 = new String(result, "big5");
//      return sbig5;
//    } catch (Exception ex) {
//      System.out.println("bankDecode error : " + ex.getMessage());
//      return null;
//    }
//  }

  // ************************************************************************
  // 用正規式拆解中文地址
  // 開頭一定要是3或5個數字的郵遞區號，如果不是，解析不會出錯，但ZipCode為空
  // 除了一些較特殊的地址如釣魚台 南海島 南沙群島等，其它大部分都可解析出來
  // 地址段的部分必須用中文數字，其他鄰、巷、弄、號、序號及樓層必須是半形數字，如違反上述規則解析會有問題。
  // ************************************************************************
  public HashMap<String, String> parseByAddress(String address) {
    HashMap<String, String> map = new HashMap<String, String>();
    String pattern =
        "(?<zipcode>(^\\d{5}|^\\d{3})?)(?<city>\\D+?[縣市])(?<region>\\D+?(市區|鎮區|鎮市|[鄉鎮市區]))?(?<village>\\D+?[村里])?(?<neighbor>\\d+[鄰])?(?<road>\\D+?(村路|[路街道段]))?(?<section>\\D?段)?(?<lane>\\d+巷)?(?<alley>\\d+弄)?(?<no>\\d+號?)?(?<seq>-\\d+?(號))?(?<floor>\\d+樓)?(?<others>.+)?";

    // Create a Pattern object
    Pattern pattern1 = Pattern.compile(pattern);

    // Now create matcher object.
    Matcher match = pattern1.matcher(address);
    if (match.find()) {
      // zipcode 為郵遞區號，只接受3碼及5碼
      // City 為縣市，Region為鄉鎮市區，Village為村里，Neighbor為鄰，other為其他
      map.put("zipcode", match.group("zipcode"));
      map.put("city", match.group("city"));
      map.put("region", match.group("region"));
      map.put("village", match.group("village"));
      map.put("neighbor", match.group("neighbor"));

      map.put("others",
          (match.group("road") == null ? "" : match.group("road"))
              + (match.group("section") == null ? "" : match.group("section"))
              + (match.group("lane") == null ? "" : match.group("lane"))
              + (match.group("alley") == null ? "" : match.group("alley"))
              + (match.group("no") == null ? "" : match.group("no"))
              + (match.group("seq") == null ? "" : match.group("seq"))
              + (match.group("floor") == null ? "" : match.group("floor"))
              + (match.group("others") == null ? "" : match.group("others")));

    } else {
      map.put("zipcode", "error");
    }

    return map;

  }

  // ************************************************************************
  public void errExit(String err1, String err2) throws Exception {
    showLogMessage("I", "", err1);
    showLogMessage("I", "", err2);
    throw new Exception("DXC_NOSHOW_EXCEPTION");
  }
  // ************************************************************************

  /***
   * create by davidfu for get user id
   * 
   * @return
   */
  public String commGetUserID() {
    return "ecs";
  }

  public String getECSHOME() {
    String tmpstr = System.getenv("PROJ_HOME");
    tmpstr = java.text.Normalizer.normalize(tmpstr, java.text.Normalizer.Form.NFKD);
    return tmpstr;
  }

  public String getIBMftp() {
    return getECSHOME() + "/ecs_ftp";
  }

  public String getPURftp() {
    return getECSHOME() + "/purchase";
  }

  public void writeReportForTest(String filename, List<Map<String, Object>> lpar) {
    String tmpstr = SecurityUtil.verifyPath(filename);
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpstr), "UTF-8"))) {
      for (int i = 0; i < lpar.size(); i++) {
        String content = lpar.get(i).get("content").toString() + "\n";
        writer.write(content);
      }
      writer.close();
    } catch (IOException ex) {
      // do something
      showLogMessage("I", "writeReport", "FileName : " + tmpstr);
      showLogMessage("I", "writeReport", "Error : " + ex.getMessage());
    }
  }

  public void writeReport(String filename, List<Map<String, Object>> lpar) {
    writeReport(filename, lpar, "MS950");
  }

  public void writeReport(String filename, List<Map<String, Object>> lpar, String csn) {
    writeReport(filename, lpar, csn, true);
  }

  public void writeReport(String filename, List<Map<String, Object>> lpar, String csn,
      boolean bCRLF) {
	String separatorFilename=SecurityUtil.verifyPath(filename);
    //String tmpstr = java.text.Normalizer.normalize( SecurityUtil.verifyPath(filename), java.text.Normalizer.Form.NFKD);
    try {
        File file = new File(separatorFilename);    
        boolean result = file.getParentFile().mkdirs();
        if (result == false) {
    		if (file.getParentFile().exists() == false) {
    			showLogMessage("I", "", "Fail to create directories");
    		}
    	  }
      // 2020_0615 resolve Unreleased Resource: Streams by yanghan
      try (BufferedWriter writer =
          new BufferedWriter(new OutputStreamWriter(new FileOutputStream(separatorFilename), csn))) {
        for (int i = 0; i < lpar.size(); i++) {
          String content = lpar.get(i).get("content").toString();
          if (bCRLF)
            content += "\n";
          writer.write(content);
        }
        writer.close();
      }
    } catch (IOException ex) {
      // do something
      showLogMessage("I", "writeReport", "FileName : " + separatorFilename);
      showLogMessage("I", "writeReport", "Error : " + ex.getMessage());
    }
  }

  // ************************************************************************
  // 0:加密 1:解密
  // return password
  //
  //
  // ************************************************************************
  public String transPasswd(int type, String fromPasswd) throws Exception {
    long addNum[] = {7, 34, 295, 4326, 76325, 875392, 2468135, 12357924, 123456789};
    int transInt, int1, int2, datalen;
    long dataint = 1;
    String fdig[] = {"08122730435961748596", "04112633405865798792", "03162439425768718095",
        "04152236415768798390", "09182035435266718497", "01152930475463788296",
        "07192132465068748593", "02172931455660788394"};
    String tmpstr = "";
    String tmpstr1 = "";
    String toPawd = "";

    if (fromPasswd.length() < 1)
      return "";

    if (type == 0) {
      // 加密
      datalen = fromPasswd.length();
      for (int1 = 0; int1 < datalen; int1++) {
        int sbn = Integer.parseInt(fromPasswd.substring(int1, int1 + 1)) * 2 + 1;
        tmpstr += fdig[int1].substring(sbn, sbn + 1);
      }

      for (int1 = 0; int1 < datalen; int1++) {
        dataint = dataint * 10;
      }
      tmpstr1 = String.valueOf(dataint + Long.parseLong(tmpstr) - addNum[datalen - 1]);
      toPawd = tmpstr1.substring(tmpstr1.length() - datalen);

    } else {
      // 解密
      datalen = fromPasswd.length();

      tmpstr1 = String.format("%d", Long.parseLong(fromPasswd) + addNum[datalen - 1]);
      tmpstr = tmpstr1.substring(tmpstr1.length() - datalen);
      for (int1 = 0; int1 < datalen; int1++) {
        for (int2 = 0; int2 < 10; int2++) {
          int po = int2 * 2 + 1;
          if (tmpstr.substring(int1, int1 + 1).equals(fdig[int1].substring(po, po + 1))) {
            po = int2 * 2;
            toPawd += fdig[int1].substring(po, po + 1);
            break;
          }
        }
      }
    }

    return toPawd;
  }

  /***
   * 
   * @param bytes : MS950的bytes
   * @param offset : 開始讀取位元
   * @param length : 讀取長度
   * @return 位元資料
   */
  public byte[] subArray(byte[] bytes, int offset, int length) {
    byte[] vResult = new byte[length];
    System.arraycopy(bytes, offset, vResult, 0, length);
    return vResult;
  }

  /***
   * 從big5的bytes字串中，讀取一定長度資料
   * 
   * @param bytes : MS950的bytes
   * @param offset : 開始讀取位元
   * @param length : 讀取長度
   * @return 讀取MS950字串
   * @throws UnsupportedEncodingException
   */
  public String subMS950String(byte[] bytes, int offset, int length)
      throws UnsupportedEncodingException {
    return subString(bytes, offset, length, "MS950");
  }

  // ************************************************************************
  public String subMS950StringR(byte[] bytes, int offset, int length)
      throws UnsupportedEncodingException {
    return subStringR(bytes, offset, length, "MS950");
  }

  // ************************************************************************
  public String subBIG5String(byte[] bytes, int offset, int length)
      throws UnsupportedEncodingException {
    return subString(bytes, offset, length, "big5");
  }

  // ************************************************************************
  public String subBIG5StringR(byte[] bytes, int offset, int length)
      throws UnsupportedEncodingException {
    return subStringR(bytes, offset, length, "big5");
  }

  // ************************************************************************
  public String subString(byte[] bytes, int offset, int length, String charsetName)
      throws UnsupportedEncodingException {
    if (bytes.length < offset)
      return "";
    int len = bytes.length >= offset + length ? length : bytes.length - offset;
    return new String(subArray(bytes, offset, len), charsetName);
  }

  // ************************************************************************
  public String subStringR(byte[] bytes, int offset, int length, String charsetName)
      throws UnsupportedEncodingException {
    String tmpstr = subString(bytes, offset, length, charsetName);
    return rtrim(tmpstr);
  }

  // ************************************************************************
  public String removeSpace(String dataStr) {
    return dataStr.trim();
  }

  public String eraseSpace(String dataStr) {
    String buf = "";
    for (int i = dataStr.length() - 1; i >= 0; i--) {
      if (dataStr.substring(i, i + 1).equals(" ") == false) {
        buf = dataStr.substring(0, i + 1);
        break;
      }
    }
    return buf;
  }

  public String ltrim(String str) {
    return str.replaceAll("^\\s+", "");
  }

  public String rtrim(String str) {
    return str.replaceAll("\\s+$", "");
  }

  public boolean isThisDateValid(String dateToValidate, String dateFromat) {

    if (dateToValidate == null) {
      return false;
    }

    SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
    sdf.setLenient(false);

    try {

      // if not valid, it will throw ParseException
      Date date = sdf.parse(dateToValidate);

    } catch (ParseException e) {

      e.printStackTrace();
      return false;
    }

    return true;
  }

  // ************************************************************************
  public boolean systemCmd(String cmdStr) {

    return systemCmd(cmdStr, false);
  }

  // ************************************************************************
  public boolean systemCmd(String cmdStr, boolean bHaveWilcard) {
    int exitVal = 0;
    Process proc;
    try {

      if (bHaveWilcard == true)
        proc = Runtime.getRuntime().exec(new String[] {"sh", "-c", cmdStr});
      else {
        String[] commands = cmdStr.split(" ");
        proc = Runtime.getRuntime().exec(commands);
      }

      // any error message?
      StreamConsumer errorConsumer = new StreamConsumer(proc.getErrorStream(), "error");
      // any output?
      StreamConsumer outputConsumer = new StreamConsumer(proc.getInputStream(), "output");
      // kick them off
      errorConsumer.start();
      outputConsumer.start();

      exitVal = proc.waitFor();

    } catch (Exception e) {
      return false;
    }

    if ((exitVal != 0)) {
      return false;
    }
    return true;
  }


  // ************************************************************************
  public char getCharAt(String str, int index) {
    if (str.length() > index)
      str.charAt(index);
    return '\0';
  }

  // ************************************************************************
  public String getSubString(String str, int beginIndex, int endIndex) {
    if (beginIndex < 0)
      return "";
    if (endIndex < 0)
      return "";
    if (beginIndex >= endIndex)
      return "";
    if (str.length() > beginIndex) {
      int eIndex = Math.min(endIndex, str.length());
      return str.substring(beginIndex, eIndex);
    }
    return "";
  }

  // ************************************************************************
  public String getSubString(String str, int beginIndex) {
    if (str.length() > beginIndex) {
      return str.substring(beginIndex);
    }
    return "";
  }

  // ************************************************************************
  public int chgnRtn(String str) {
    int[] preFix = {2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2};
    String tempX02 = "";
    int tempInt = 0;
    int tempInt1 = 0;
    int i = 0;
    int rtn = 0;

    tempInt1 = 0;
    for (i = 0; i < 15; i++) {
      tempInt = (str2int(getSubString(str, i, i + 1))) * preFix[i];
      tempX02 = String.format("%02d", tempInt);
      tempInt1 = tempInt1 + str2int(tempX02.substring(0, 1)) + str2int(tempX02.substring(1, 2));
    }

    int rem = tempInt % 10;
    rtn = 10 - rem;
    if (rtn == 10)
      rtn = 0;
    return rtn;

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

  // ************************************************************************
  public long str2long(String val) {
    long rtn = 0;
    try {
      rtn = Long.parseLong(val.replaceAll(",", "").trim());
    } catch (Exception e) {
      rtn = 0;
    }
    return rtn;
  }

  // ************************************************************************
  public double str2double(String val) {
    double rtn = 0;
    try {
      rtn = Double.parseDouble(val.replaceAll(",", ""));
    } catch (Exception e) {
      rtn = 0;
    }
    return rtn;
  }
  // ************************************************************************

  public boolean fileRename(String filename, String newFilename) {
    File file = new File(SecurityUtil.verifyPath(filename).trim());
    File file2 = new File(SecurityUtil.verifyPath(newFilename).trim());

    if (file2.exists())
      return false;

    boolean result = file2.getParentFile().mkdirs();
    if (result == false) {
		if (file2.getParentFile().exists() == false) {
			showLogMessage("I", "", "Fail to create directories");
		}
	}
    
    return file.renameTo(file2);
  }

  public String getDataFromArray(String[] strArray, int index) {
    if (strArray.length > index)
      return strArray[index];
    return "";
  }

  public class CommOracleIPData {
    public String server = "";
    public String svrName = "";
    public String filename = "";
    public int errCode = 0;
  }

  public CommOracleIPData commOracleIp() throws IOException {
    CommOracleIPData rtn = new CommOracleIPData();
    String[] argStrIn = "tcp, host, port, 1521".split(",");

    String filename = String.format("%s/network/admin/listener.ora", System.getenv("ORACLE_HOME"));
    // verify path string
    rtn.filename = SecurityUtil.verifyPath(filename);
    File file = new File(rtn.filename);
    if (file.exists() == false) {
      rtn.errCode = -99;
      return rtn;
    }
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (FileInputStream fis = new FileInputStream(new File(rtn.filename));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "MS950"))) {
      String str600 = "";
      while ((str600 = br.readLine()) != null) {
    	str600 = str600.toLowerCase();
        byte[] bytes = str600.getBytes("MS950");
        for (int int1 = bytes.length - 1; int1 >= 0; int1--) {
          if ((bytes[int1] < 20) || (bytes[int1] > 120)) {
            bytes[int1] = ' ';
          } else {
            break;
          }
        }

        if (str600.length() < 2)
          continue;

        boolean chgFlag = false;
        for (int int1 = 0; int1 < 4; int1++) {
          if (str600.indexOf(argStrIn[int1]) == -1) {
            chgFlag = true;
            break;
          }
        }

        if (chgFlag)
          continue;
      }
      br.close();
    }
    return rtn;

  }

  /***********************************************************************/
  public void writeLogfile(String filename, String logStr) {
    try {
      File f = new File(filename);
      if (f.exists()) {
        Files.write(Paths.get(filename), logStr.getBytes(), StandardOpenOption.APPEND);
      } else {
        Files.write(Paths.get(filename), logStr.getBytes(), StandardOpenOption.CREATE_NEW);
      }

    } catch (IOException e) {
      // exception handling left as an exercise for the reader
    }
  }

  public String cmpStr(String st1, int ln1, String st2, int ln2) {
    String spc = "";
    for (int i = 0; i < 100; i++)
      spc += " ";
    st1 += spc;
    st2 += spc;
    return st1.substring(0, ln1) + st2.substring(0, ln2);
  }

  /***********************************************************************/
  /***
   * 轉中文全型至半型程式
   * 
   * @param p1
   * @param p2
   * @return
   */
  public String commBig5Asc(String str) {
    for (char c : str.toCharArray()) {
      str = str.replaceAll("　", " ");
      if ((int) c >= 65281 && (int) c <= 65374) {
        str = str.replace(c, (char) (((int) c) - 65248));
      }
    }
    return str;
  }

  /***********************************************************************/
  /***
   * 調整英文姓名為大寫程式
   * 
   * @param p1
   * @param p2
   * @return
   */
  public String commAdjEngname(String data1) {
    String regEx = "[^A-Z-,. ]";

    Pattern pattern1 = Pattern.compile(regEx);
    Matcher match = pattern1.matcher(data1.toUpperCase());
    String rtn = match.replaceAll("").trim();
    return rtn.trim();
  }

  /***********************************************************************/
  public int byteToUnsignedInt(byte bytes) {
    return 0x00 << 24 | bytes & 0xff;
  }

  /***********************************************************************/
  public boolean fileRename2(String src, String target) {
	File fs = new File(SecurityUtil.verifyPath(src));
    File ft = new File(SecurityUtil.verifyPath(target));
    boolean result = ft.getParentFile().mkdirs();
    if (result == false) {
  		if (ft.getParentFile().exists() == false) {
  			showLogMessage("I", "", "Fail to create directories");
  		}
  	}
    return fs.renameTo(ft);
  }

  /***********************************************************************/
  public boolean fileCopy(String src, String target) {
    try {
		File fs = new File(SecurityUtil.verifyPath(src));
		File ft = new File(SecurityUtil.verifyPath(target));
		boolean result = ft.getParentFile().mkdirs();
		if (result == false) {
			if (ft.getParentFile().exists() == false) {
				showLogMessage("I", "", "Fail to create directories");
			}
		}
      FileUtils.copyFile(fs, ft);
      return true;
    } catch (Exception ex) {
      showLogMessage("I", "file_copy", "file_copy Error : " + ex.getMessage());
      return false;
    }
  }

  /***********************************************************************/
  public boolean fileMerge(String src, String target) throws IOException {
    /*
     * //first way BufferedOutputStream out = null; InputStream in = null; try { // create
     * FileOutputStream for file to append to out = new BufferedOutputStream(new
     * FileOutputStream(target, true)); // create FileInputStream for file to append from in = new
     * BufferedInputStream(new FileInputStream(src)); byte[] buffer = new byte[1024 * 4]; long count
     * = 0; int n = 0; while (-1 != (n = in.read(buffer))) { out.write(buffer, 0, n); count += n; }
     * if (in != null) in.close(); if (out != null) out.close(); return true;
     * 
     * } catch (IOException e) { if (in != null) in.close(); if (out != null) out.close(); return
     * false; }
     */
    // second way
//    FileChannel in = null;
//    FileChannel out = null;
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (
        FileInputStream inStream = new FileInputStream(Normalizer.normalize(src.trim(), java.text.Normalizer.Form.NFKD));
        FileOutputStream outStream = new FileOutputStream(Normalizer.normalize(target.trim(), java.text.Normalizer.Form.NFKD), true);
    	FileChannel in = inStream.getChannel();
    	FileChannel out = outStream.getChannel();) {
      out.position(out.size());
      in.transferTo(0, in.size(), out);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /***********************************************************************/
  public boolean fileDelete(String src) {
	  File fs = new File(SecurityUtil.verifyPath(src));
    return fs.delete();
  }

  /***********************************************************************/
  public boolean fileMove(String src, String target) {
    if (fileCopy(src, target)) {
      fileDelete(src);
      return true;
    }
    return false;
  }

  /***********************************************************************/
  public void chmod777(String file) {
    // the first way
    File f = new File(SecurityUtil.verifyPath(file.trim()));
    f.setExecutable(true, false);
    f.setReadable(true, false);
    f.setWritable(true, false);
    // the second way
    /*
     * Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>(); //add owners permission
     * perms.add(PosixFilePermission.OWNER_READ); perms.add(PosixFilePermission.OWNER_WRITE);
     * perms.add(PosixFilePermission.OWNER_EXECUTE); //add group permissions
     * perms.add(PosixFilePermission.GROUP_READ); perms.add(PosixFilePermission.GROUP_WRITE);
     * perms.add(PosixFilePermission.GROUP_EXECUTE); //add others permissions
     * perms.add(PosixFilePermission.OTHERS_READ); perms.add(PosixFilePermission.OTHERS_WRITE);
     * perms.add(PosixFilePermission.OTHERS_EXECUTE);
     * 
     * Files.setPosixFilePermissions(Paths.get(file), perms);
     */
  }

  /***********************************************************************/
  public void chmod600(String file) throws IOException {
    Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
    // add owners permission
    perms.add(PosixFilePermission.OWNER_READ);
    perms.add(PosixFilePermission.OWNER_WRITE);
    // perms.add(PosixFilePermission.OWNER_EXECUTE);
    // add group permissions
    // perms.add(PosixFilePermission.GROUP_READ);
    // perms.add(PosixFilePermission.GROUP_WRITE);
    // perms.add(PosixFilePermission.GROUP_EXECUTE);
    // add others permissions
    // perms.add(PosixFilePermission.OTHERS_READ);
    // perms.add(PosixFilePermission.OTHERS_WRITE);
    // perms.add(PosixFilePermission.OTHERS_EXECUTE);

    Files.setPosixFilePermissions(Paths.get(file.trim()), perms);
  }

  /***********************************************************************/
  public void chmodSec(String file) throws IOException {
    chmodSec(file.trim(), true);
  }

  /***********************************************************************/
  public void chmodSec(String file, boolean bGroupRead) throws IOException {
    Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
    // add owners permission
    perms.add(PosixFilePermission.OWNER_READ);
    perms.add(PosixFilePermission.OWNER_WRITE);
    perms.add(PosixFilePermission.OWNER_EXECUTE);
    // add group permissions
    if (bGroupRead)
      perms.add(PosixFilePermission.GROUP_READ);
    // perms.add(PosixFilePermission.GROUP_WRITE);
    // perms.add(PosixFilePermission.GROUP_EXECUTE);
    // add others permissions
    // perms.add(PosixFilePermission.OTHERS_READ);
    // perms.add(PosixFilePermission.OTHERS_WRITE);
    // perms.add(PosixFilePermission.OTHERS_EXECUTE);

    Files.setPosixFilePermissions(Paths.get(SecurityUtil.verifyPath(file.trim())), perms);
  }

  /*********************************************************
   * 檢查時間格式hhmmss
   */
  /****************************************************************/
  public boolean commTimeCheck(String data1) {

    // if (data1.length() != 6)
    // return false;
    try {
      String tData = String.format("%2.2s", data1);
      int tInt = Integer.valueOf(tData);
      if ((tInt < 0) || (tInt > 23))
        return false;

      tData = String.format("%2.2s", data1.substring(2));
      tInt = Integer.valueOf(tData);
      if ((tInt < 0) || (tInt > 59))
        return false;

      tData = String.format("%2.2s", data1.substring(4));
      tInt = Integer.valueOf(tData);
      if ((tInt < 0) || (tInt > 59))
        return false;
    } catch (Exception ex) {
      return false;
    }

    return true;

  }

  /***********************************************************************/
  public boolean commDateCheck(String data1) throws Exception {
    try {
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
      format.setLenient(false);
      Date dateCheck = format.parse(getSubString(data1, 0, 8));
    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  // /***********************************************************************/
  // public boolean COMM_format_check(String ckString) throws Exception {
  // ckString = ckString.replaceAll("\\/", "");
  // ckString = ckString.replaceAll("-" , "");
  // //最多補 月、日 兩次
  // for(int i=0; i<2; i++) {
  // if ( ckString.length() == 8 ) {
  // return COMM_date_check(ckString);
  // } else if(ckString.length() == 7){
  // ckString = Integer.toString((Integer.parseInt(ckString) + 19110000));
  // //國歷換西元年
  // return COMM_date_check(ckString);
  // }
  // ckString += "01";
  // }
  // return false; // 長度錯誤
  // }

  /************************************************************************/
  public boolean commDigitCheck(String ckString) throws Exception {
    if (ckString.length() == 0) {
      return false;
    }

    byte[] checkData = ckString.getBytes();

    for (int i = 0; i < checkData.length; i++) {
      if ((i == 0) && (checkData[i] == (byte) '-'))
        continue;
      if (checkData[i] < '0' || checkData[i] > '9')
        return false;
    }
    return true;
  }

  /***********************************************************************/
  public int commTSCCCardNoCheck(String inStr) {

    int inta = 0, intb = 0, intc = 0;
    String tempX02;// [2+1]

    intc = 0;
    for (inta = 0; inta < inStr.length() - 1; inta++) {
      intb = 1;
      if ((inta % 2) == 0)
        intb = 2;
      tempX02 = String.format("%02d", (inStr.toCharArray()[inta] - 48) * intb);
      intc = intc + tempX02.toCharArray()[0] - 48 + tempX02.toCharArray()[1] - 48;
    }

    if (inStr.toCharArray()[inStr.length() - 1] - 48 == (10 - (intc % 10)) % 10)
      return (0);
    else
      return (1);
  }

  /***********************************************************************/
  public boolean mkdirsFromFilenameWithPath(String filename) {
    try {
        File file = new File(SecurityUtil.verifyPath(filename));
        boolean result = file.getParentFile().mkdirs();
        if (result == false) {
    		if (file.getParentFile().exists() == false) {
    			showLogMessage("I", "", "Fail to create directories");
    		}
    	  }
    } catch (Exception ex) {
      showLogMessage("I", "mkdirsFromFilenameWithPath", "FileName : " + SecurityUtil.verifyPath(filename));
      showLogMessage("I", "mkdirsFromFilenameWithPath", "mkdirs Error : " + ex.getMessage());
      return false;
    }
    return true;
  }

  /***********************************************************************/
  public boolean mkdirsFromPath(String filename) {
    try {
      File file = new File(filename);
      boolean result = file.mkdirs();
      if (result == false) {
    	  showLogMessage("I", "", "Fail to create the directory");
  	  }
    } catch (Exception ex) {
      showLogMessage("I", "mkdirsFromFilenameWithPath", "FileName : " + filename);
      showLogMessage("I", "mkdirsFromFilenameWithPath", "mkdirs Error : " + ex.getMessage());
      return false;
    }
    return true;
  }

  /***********************************************************************/
  public List<String> listFS(String path, String start, String end) {
    List<String> rtn = new ArrayList<String>();
    File folder = new File(SecurityUtil.verifyPath(path));
    File[] listFiles = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(start) && name.endsWith(end);
      }
    });
    if (listFiles == null) return rtn;
    for (File file : listFiles) {
      if (!file.isFile())
        continue;
      rtn.add(file.getName());
    }
    folder = null;
    return rtn;
  }

  /***********************************************************************/
  public long getFileLength(String path) {
    File file = new File(path);
    return file.length();
  }

  /***********************************************************************/
  public String getStackTraceString(Exception ex) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    String sRtn = sw.toString();
    sw = null;
    return sRtn;
  }

  /***********************************************************************/
  public int idCheck(String id) throws Exception {
    id = id.toUpperCase();
    /*
     * if(comm.checkID(p1) == false) return(1); return(0);
     */
    int sum, cnt1;
    int sum1 = 0;
    String val;
    String[] fdig = {"10", "11", "12", "13", "14", "15", "16", "17", "34", "18", "19", "20", "21",
        "22", "35", "23", "24", "25", "26", "27", "28", "29", "32", "30", "31", "33"};

    if ((id.charAt(0) < (char) 65) || (id.charAt(0) > (char) 90))
      return (1);
    if (id.length() != 10)
      return (1);

    sum = (str2int(fdig[(int) id.charAt(0) - 65].substring(0, 1)) - 0)
        + (str2int(fdig[(int) id.charAt(0) - 65].substring(1, 2)) - 0) * 9;

    for (cnt1 = 1; cnt1 <= 8; cnt1++) {
      if (isNumber(id.substring(cnt1, cnt1 + 1)) == false)
        return (1);
      sum = sum + ((str2int(id.substring(cnt1, cnt1 + 1)) - 0) * (9 - cnt1));
    }
    val = "";
    sum1 = sum % 10;
    if (sum1 > 0) {
      sum1 = 10 - sum1;
    }
    val = (char) (sum1 + 48) + "";
    if (id.substring(9, 10).equals(val))
      return (0);
    else
      return (1);
  }

  /******************************************************************************/
  private boolean isNumber(String ckString) throws Exception {
    if (ckString.length() == 0) {
      return false;
    }

    byte[] checkData = ckString.getBytes();

    for (int i = 0; i < checkData.length; i++) {
      if (checkData[i] < '0' || checkData[i] > '9')
        return false;
    }
    return true;
  }

  /*******************************************************************************/

  /*
   * 新式統一證號(ID)檢核
   */
  public boolean isValidNewId(String id) {
    // 特定數
    final int[] targetNums = {1, 9, 8, 7, 6, 5, 4, 3, 2, 1};

    if (id.length() != 10) {
      return false;
    } else {
      String firstAlpha = id.substring(0, 1);
      if (null == numTable.get(firstAlpha)) {
        return false;
      }
      String unifiedCode = numTable.get(firstAlpha) + id.substring(1);

      long alphaToNum = 0;
      try {
        alphaToNum = Long.parseLong(unifiedCode);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }

      if (!Long.toString(alphaToNum).equals(unifiedCode)) {
        return false;
      }

      // 基數
      int baseNum = 0;
      for (int i = 0; i < unifiedCode.length(); i++) {
        baseNum += (((int) unifiedCode.charAt(i) - 48) * targetNums[i]) % 10;
      }

      // 檢查碼
      int checkCode = baseNum % 10;
      if (checkCode != 0) {
        checkCode = (10 - checkCode);
      }
      return Integer.toString(checkCode).equals(id.substring(id.length() - 1));
    }
  }

  /*******************************************************************************/
  /**
   * SHA 加密
   * 
   * @param data 需要加密的字符串
   * @return 加密之后的字符串
   * @throws Exception
   */
  public String encryptSHA(String data, String keySha, String type) throws Exception {
    MessageDigest sha = MessageDigest.getInstance(keySha);
    sha.update(data.getBytes(type));
    byte[] bytes = sha.digest();

    return byteToHexString(bytes);
  }

  public String encryptSHA(byte[] data, String keySha) throws Exception {
    MessageDigest sha = MessageDigest.getInstance(keySha);
    sha.update(data);
    byte[] bytes = sha.digest();

    return byteToHexString(bytes);
  }

  /***********************************************************************/
  /**
   * 四捨五入
   * 
   * @param tmpdouble 取四捨五入的數
   * @param num 取小數點後num位
   * @return 四捨五入後的值
   */
  public double round(double tmpdouble, int num) {
    BigDecimal decimal1 = new BigDecimal(tmpdouble);
    BigDecimal decimal2 = decimal1.setScale(num, RoundingMode.HALF_UP);
    return Double.parseDouble(decimal2.toString());
  }

  /***********************************************************************/
  /* for HTTPSERVER to getParameter */
  public Map<String, String> formData2Dic(String formData) {
    Map<String, String> result = new HashMap<>();
    if (formData == null || formData.trim().length() == 0) {
      return result;
    }
    final String[] items = formData.split("&");
    Arrays.stream(items).forEach(item -> {
      final String[] keyAndVal = item.split("=");
      if (keyAndVal.length == 2) {
        try {
          final String key = URLDecoder.decode(keyAndVal[0], "utf8");
          final String val = URLDecoder.decode(keyAndVal[1], "utf8");
          result.put(key, val);
        } catch (UnsupportedEncodingException e) {
        }
      }
    });
    return result;
  }

  /************************************************************************/
  public String fixLeft(String str, int len) throws UnsupportedEncodingException {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
      spc += " ";
    if (str == null)
      str = "";
    str = str + spc;
    byte[] bytes = str.getBytes("MS950");
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, 0, vResult, 0, len);

    return new String(vResult, "MS950");
  }

  /************************************************************************/
  public String fixRight(String str, int len) throws UnsupportedEncodingException {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
      spc += " ";
    if (str == null)
      str = "";
    str = spc + str;
    byte[] bytes = str.getBytes("MS950");
    int offset = bytes.length - len;
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, offset, vResult, 0, len);
    return new String(vResult, "MS950");
  }

  /************************************************************************/
  public int getNextRandom(int bound) throws Exception {
    SecureRandom rd = SecureRandom.getInstance("SHA1PRNG");
    return rd.nextInt(bound);
  }

  /************************************************************************/
  public void sendSocket(Socket socket, byte[] data) throws IOException {
    DataOutputStream output = new DataOutputStream(socket.getOutputStream());

    output.write(data);
    output.flush();

    output.close();
    output = null;
  }

  /************************************************************************/
  /* ADD for db pass encrypt & decrypt */

  public String encryptForDb(String str1) {
    String uLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String uLetterEn = "QAZWSXEDCRFVTGBYHNUJMIKOLP";
    String lLetter = "abcdefghijklmnopqrstuvwxyz";
    String lLetterEn = "qazwsxedcrfvtgbyhnujmikolp";
    String num = "1234567890";
    String numEn = "0987654321";
    String rtnstr = "";
    char[] temarr = str1.toCharArray();
    for (char tempchar : temarr) {
      if (Character.isUpperCase(tempchar)) {
        rtnstr += uLetterEn.charAt(uLetter.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isLowerCase(tempchar)) {
        rtnstr += lLetterEn.charAt(lLetter.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isDigit(tempchar)) {
        rtnstr += numEn.charAt(num.indexOf(Character.toString(tempchar)));
        continue;
      }
      rtnstr += Character.toString(tempchar);
    }
    return rtnstr;
  }

  public String decryptForDb(String str1) {
    String uLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String uLetterEn = "QAZWSXEDCRFVTGBYHNUJMIKOLP";
    String lLetter = "abcdefghijklmnopqrstuvwxyz";
    String lLetterEn = "qazwsxedcrfvtgbyhnujmikolp";
    String num = "1234567890";
    String numEn = "0987654321";
    String rtnstr = "";
    char[] temarr = str1.toCharArray();
    for (char tempchar : temarr) {
      if (Character.isUpperCase(tempchar)) {
        rtnstr += uLetter.charAt(uLetterEn.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isLowerCase(tempchar)) {
        rtnstr += lLetter.charAt(lLetterEn.indexOf(Character.toString(tempchar)));
        continue;
      }
      if (Character.isDigit(tempchar)) {
        rtnstr += num.charAt(numEn.indexOf(Character.toString(tempchar)));
        continue;
      }
      rtnstr += Character.toString(tempchar);
    }
    return rtnstr;
  }
  
  /************************************************************************/
  //--按檔案最後修改時間排序
  public List<String> listFsSort(String path) throws Exception {	 
	  List<String> temp = new ArrayList<String>() ;		
	  File folder = new File(SecurityUtil.verifyPath(path));
	  File [] fs = folder.listFiles();
	  Arrays.sort(fs, new SortFile());
	  for(File file : fs) {
		  if(file.isFile() == false)
			  continue ;
		  temp.add(file.getName());
	  }			
	  return temp;
  }
  
  //--切電話
  
  public String[] transTelNo(String phone) throws Exception {	  
	  String[] temp = new String[3];
	  byte[] bytes = phone.getBytes("MS950");
	  temp[0] = subMS950String(bytes, 0, 4);
	  temp[1] = subMS950String(bytes, 4, 10);
	  temp[2] = subMS950String(bytes, 14, 6);
	  return temp ;
  }
  
} // End of class CommCrd


class StreamConsumer extends Thread {
  InputStream is;
  String type;

  StreamConsumer(InputStream is, String type) {
    this.is = is;
    this.type = type;
  }

  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null)
        System.out.println(type + ">" + line);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}

class SortFile implements Comparator<File> {	
	public int compare(File f1, File f2) {			
    	long diff = f1.lastModified()-f2.lastModified();
		if(diff>0)
		  return 1;
		else if(diff==0)
  		  return 0;
		else
		  return -1;
	}	
}

