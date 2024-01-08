/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/01/22  V1.00.11  Allen Ho   CommFunction2 initial                      *
* 106/02/09  V1.00.12  Allen Ho   Combine jack CommFunction                  *
* 106/02/20  V1.00.13  Allen Ho   GetStr string include " bug                *
* 106/02/25  V1.00.14  Allen Ho   Fix nextDate format bug                    *
* 106/03/01  V1.00.15  Allen Ho   enhance GetStr function                    *
* 106/03/08  V1.00.16  Allen Ho   Add SQLGetStr,formatReport,ChineseCharNum, *
*                                 fillSpecChar                               *
* 106/03/19  V1.00.17  Allen Ho   enhance GetStr function,Add email          *
* 106/04/12  V1.00.18  Lai        Add rtn_chkdig                             *
* 106/05/15  V1.00.19  David FU   Add bankEncode function                    *
* 106/05/22  V1.00.20  Yash        Add parseByAddress function               *
* 106/06/03  V1.00.22  Allen Ho   modify zip,unzip return int                *
* 106/06/23  V1.00.23  Allen Ho   modify structStr last string bug           *
* 106/07/04  V1.00.25  Allen Ho   add lastMonth & nextMonth method           *
* 106/09/20  V1.00.26  Allen Ho   Add toChinDate                             *
* 106/09/27  V1.00.27  Allen Ho   Add nextNDateSecond                        *
* 106/10/06  V1.00.28  Allen Ho   modify monthBetween type int to double     *
* 106/10/23  V1.00.29  Allen Ho   modify lastdateOfmonth input must len=8    *
* 106/12/04  V1.00.30  Allen Ho   modify differenceInMonths return to double *
* 106/12/18  V1.00.32  Allen Ho   add  nextMonthDate                         *
* 107/02/02  V1.00.33  Allen Ho   mod systemCmd and add systemCmdArr         *
* 107/07/31  V1.00.35  Allen Ho   mod GetStr                                 *
* 107/08/01  V1.00.36  Jack Liao  add add isAppActive2                       *
* 107/10/05  V1.00.37  Jack Liao  modify fillRightSpaces and fillLeftSpace   *
* 107/10/08  V1.00.38  Jack Liao  delete extends AccessDAO                   *
* 107/10/30  V1.00.39  Jack Liao  modify isAppActive2  (check os)            *
* 108/10/25  V1.00.40  allen         modify nextMonthDate bug                   *
* 108/11/29  V1.00.41  allen           add hiderefcode                            *
* 109/04/07  V1.00.42  Jack Liao   modify fix isAppActive=isAppActive2        *
* 109/07/06  V1.00.43    Zuwei     coding standard, rename field method & format                   *
* 109/07/22  V1.00.44    Zuwei     coding standard, rename field method                   *
* 109-08-14  V1.00.45  Zuwei        fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
* 109/09/10  V1.00.46  JustinWu   hide print in hideUnzipData
* 110-01-07  V1.00.47    shiyuqi       修改无意义命名                 
* 110-11-05  V1.00.48  JustinWu    ps -few -> ps -elf                                                       *
* 111/01/18  V1.00.49  Justin     fix Erroneous String Compare               *
* 111-01-18  V1.00.50  Justin     fix Throw Inside Finally                  *
* 111-01-19  V1.00.51  Justin     fix Missing Check against Null            *
* 111-01-19  V1.00.52  Justin     fix Unchecked Return Value             
* 111-02-14  V1.00.53  Justin     big5 -> MS950                             *
* 111-02-18  V1.00.54  Justin     add getTelZoneAndNo                       * 
* 111-02-18  V1.00.55  Justin     切電話號碼邏輯改為使用欄位長度切法        *
* *******************************************************************************/
package com;

import java.io.File;
import java.io.*;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

// add by davidfu@20170515
import java.nio.charset.StandardCharsets;

import java.math.RoundingMode;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import Dxc.Util.SecurityUtil;
import java.text.ParseException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// add by davidfu@20170515
//import com.bank.encoding.BankEncoding;

public class CommFunction {
  public int maxSpace = 500, maxZero = 300;
  private int zeroControl = 0, spaceControl = 0;
  private String spaces = "", zeros = "";
  int DEBUG = 0;

  FileLock lock;
  FileChannel channel;

  // ************************************************************************
  public CommFunction() {
    return;
  }

  // ************************************************************************
  public String convertAmount(String amtField, int dec) throws Exception {
    String cvtAmount = String.format("%,14." + dec + "f", Float.parseFloat(amtField));
    return cvtAmount;
  }

  // ************************************************************************
  public boolean checkDateFormat(String checkData, String dateFmt) throws Exception {
    try {
      SimpleDateFormat format = new SimpleDateFormat(dateFmt);
      format.setLenient(false);
      Date dateCheck = format.parse(checkData);
    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  // ************************************************************************
  public int datePeriod(String dateStart, String dateStop) throws Exception {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    Date date1 = format.parse(dateStart);
    Date date2 = format.parse(dateStop);
    long diff = date2.getTime() - date1.getTime();

    return (int) (diff / (24 * 60 * 60 * 1000));
  }

  // ************************************************************************
  public int getConvertDate(String parmConv, String parmDate) throws Exception {
    if (parmDate.length() != 8) {
      return 99;
    }

    int cvtDays = 0;
    Calendar ca1 = new GregorianCalendar();
    int cvtYear = Integer.parseInt(parmDate.substring(0, 4));
    int cvtMonth = Integer.parseInt(parmDate.substring(4, 6)) - 1;
    int cvtDate = Integer.parseInt(parmDate.substring(6, 8));
    ca1.set(cvtYear, cvtMonth, cvtDate);
    if (parmConv.equals("DAY_OF_WEEK")) {
      cvtDays = ca1.get(Calendar.DAY_OF_WEEK) - 1;
    } else if (parmConv.equals("DAY_OF_YEAR")) {
      cvtDays = ca1.get(Calendar.DAY_OF_YEAR);
    } else if (parmConv.equals("DAY_OF_WEEK_IN_MONTH")) {
      cvtDays = ca1.get(Calendar.DAY_OF_WEEK_IN_MONTH);
    } else if (parmConv.equals("WEEK_OF_YEAR")) {
      cvtDays = ca1.get(Calendar.WEEK_OF_YEAR);
    }
    return cvtDays;
  }

  // ************************************************************************
  public String convDates(String parmDate, int ki) throws Exception {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Calendar cal = Calendar.getInstance();
    cal.setTime(dateFormat.parse(parmDate));
    cal.add(Calendar.DATE, ki);
    return dateFormat.format(cal.getTime());
  }

  // ************************************************************************
  public boolean numberPatten(String checkData) {
    if (checkData.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
      return true;
    } else {
      return false;
    }
  }

  // ************************************************************************
  public String formatAmount(String pattern, double amount) throws Exception {
    DecimalFormat df = new DecimalFormat(pattern);
    return df.format(amount);
  }

  // ************************************************************************
  public boolean checkIdno(String idString) throws Exception {
    String letters = "ABCDEFGHJKLMNPQRSTUVXYWZIO";

    int[] multiply = {1, 9, 8, 7, 6, 5, 4, 3, 2, 1};
    int[] nums = {0, 0};
    int firstNum = 0, lastNum = 0, total = 0;

    idString = idString.toUpperCase();

    if (idString.length() != 10) {
      return false;
    }

    if (idString.toUpperCase().charAt(0) < 'A' || idString.toUpperCase().charAt(0) > 'Z') {
      return false;
    }

    if (idString.charAt(1) != '1' && idString.charAt(1) != '2') {
      return false;
    }

    if (!isNumber(idString.substring(2))) {
      return false;
    }

    lastNum = Integer.parseInt(idString.substring(9));

    for (int i = 0; i < 26; i++) {
      if (idString.charAt(0) == letters.charAt(i)) {
        firstNum = i + 10;
        nums[0] = (int) (firstNum / 10);
        nums[1] = firstNum - (nums[0] * 10);
        break;
      }
    }

    for (int i = 0; i < multiply.length; i++) {
      if (i < 2) {
        total += nums[i] * multiply[i];
      } else {
        total += Integer.parseInt(idString.substring(i - 1, i)) * multiply[i];
      }
    }

    if ((10 - (total % 10)) != lastNum) {
      return false;
    }

    return true;
  }

  // ************************************************************************
  public boolean checkID(String idString) throws Exception {
    String firstChar[] =
        {"A", "B", "C", "D", "E", "F", "G", "R", "S", "T", "U", "V", "X", "Y", "W", "Z", "I", "O"};

    int inte = -1;
    String chars = String.valueOf(Character.toUpperCase(idString.charAt(0)));
    for (int i = 0; i < 18; i++) {
      if (chars.compareTo(firstChar[i]) == 0) {
        inte = i;
      }
    }

    int total = 0;
    int all[] = new int[11];
    String value = String.valueOf(inte + 10);
    int int1 = Integer.parseInt(String.valueOf(value.charAt(0)));
    int int2 = Integer.parseInt(String.valueOf(value.charAt(1)));
    all[0] = int1;
    all[1] = int2;
    for (int j = 2; j <= 10; j++) {
      all[j] = Integer.parseInt(String.valueOf(idString.charAt(j - 1)));
    }
    for (int k = 1; k <= 9; k++) {
      total += all[k] * (10 - k);
    }

    total += all[0] + all[10];
    if (total % 10 == 0) {
      return true;
    }
    return false;
  }

  // ************************************************************************
  public boolean checkCardNo(String cardNo) throws Exception {
    if (!isNumber(cardNo)) {
      return false;
    } else if (cardNo.length() < 13) {
      return false;
    } else if ("4".equals(cardNo.substring(0, 1)) && cardNo.length() != 13 && cardNo.length() != 16) {
      return false;
    } else if (( "34".equals(cardNo.substring(0, 2)) || "37".equals(cardNo.substring(0, 2)))
        && cardNo.length() != 15) {
      return false;
    } else if (cardNo.length() != 16) {
      return false;
    }

    int j = 1, ckSum = 0, calc = 0;

    for (int i = cardNo.length() - 1; i >= 0; i--) {
      calc = Integer.parseInt(cardNo.substring(i, i + 1)) * j;
      if (calc > 9) {
        ckSum = ckSum + 1;
        calc = calc - 10;
      }
      ckSum = ckSum + calc;
      if (j == 1) {
        j = 2;
      } else {
        j = 1;
      }
    }

    if (ckSum % 10 != 0) {
      return false;
    }

    return true;
  }

  // ************************************************************************
  public String cardChkCode(String cardNo) throws Exception {

    for (int i = 0; i <= 9; i++) {
      String chkStr = Integer.toString(i);
      String cardNo1 = cardNo + Integer.toString(i);
      if (checkCardNo(cardNo1)) {
        return chkStr;
      }
    }

    return "E";
  }

  // ************************************************************************
  public boolean isNumber(String ckString) throws Exception {
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

  // ************************************************************************
  public boolean isAlphaNumber(String ckString) throws Exception {
    byte[] checkData = ckString.getBytes();

    for (int i = 0; i < checkData.length; i++) {
      if (checkData[i] == ' ')
        continue;
      if (checkData[i] >= '0' && checkData[i] <= '9')
        continue;
      if (checkData[i] >= 'a' && checkData[i] <= 'z')
        continue;
      if (checkData[i] >= 'A' && checkData[i] <= 'Z')
        continue;
      return false;
    }
    return true;
  }

  // ************************************************************************
  public boolean isAlphaSpace(String ckString) throws Exception {
    byte[] checkData = ckString.getBytes();

    for (int i = 0; i < checkData.length; i++) {
      if (checkData[i] == ' ')
        continue;
      if (checkData[i] >= 'a' && checkData[i] <= 'z')
        continue;
      if (checkData[i] >= 'A' && checkData[i] <= 'Z')
        continue;
      return false;
    }
    return true;
  }

  // ************************************************************************
  public boolean isValidAlpha(String ckString) throws Exception {
    byte[] checkData = ckString.getBytes();

    for (int i = 0; i < checkData.length; i++) {
      if (checkData[i] >= 'a' && checkData[i] <= 'z')
        continue;
      if (checkData[i] >= 'A' && checkData[i] <= 'Z')
        continue;
      return false;
    }
    return true;
  }

  // ************************************************************************
  public boolean isValidEmail(String email) {
    String ePattern =
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
    java.util.regex.Matcher m = p.matcher(email);
    return m.matches();
  }

  // ************************************************************************
  public String formatNumber(String destValue, String cvtValue, int startPnt, int endPnt)
      throws Exception {

    startPnt--;
    if (cvtValue == null) {
      cvtValue = "";
    }
    int len = endPnt - startPnt;
    cvtValue = fillZero(cvtValue, len);

    byte[] tmpData = destValue.getBytes();
    int tmpLen = tmpData.length;
    destValue = new String(tmpData, 0, startPnt) + cvtValue
        + new String(tmpData, startPnt, (tmpLen - startPnt) - len);
    return destValue;
  }

  // ************************************************************************
  public String formatRightString(String destValue, String cvtValue, int startPnt, int endPnt)
      throws Exception {

    startPnt--;
    if (cvtValue == null) {
      cvtValue = "";
    }
    int len = endPnt - startPnt;
    cvtValue = fillRightSpace(cvtValue, len);
    byte[] tmpData = destValue.getBytes();
    int tmpLen = tmpData.length;
    destValue = new String(tmpData, 0, startPnt) + cvtValue
        + new String(tmpData, startPnt, (tmpLen - startPnt) - len);
    return destValue;
  }

  // ************************************************************************
  public String formatLeftString(String destValue, String cvtValue, int startPnt, int endPnt)
      throws Exception {

    startPnt--;
    if (cvtValue == null) {
      cvtValue = "";
    }
    int len = endPnt - startPnt;
    cvtValue = fillLeftSpace(cvtValue, len);
    byte[] tmpData = destValue.getBytes();
    int tmpLen = tmpData.length;
    destValue = new String(tmpData, 0, startPnt) + cvtValue
        + new String(tmpData, startPnt, (tmpLen - startPnt) - len);
    return destValue;
  }

  // ************************************************************************
  public String fillZero(String value, int lenData) {
    if (zeroControl == 0) {
      zeroControl = 1;
      zeros = "";
      for (int i = 0; i < maxZero; i++) {
        zeros = zeros + "0";
      }
    }
    if (value == null)
      return zeros.substring(0, lenData);
    int len = value.length();
    if (len == lenData)
      return value;
    else if (len < lenData)
      return zeros.substring(0, lenData - len) + value;
    else
      return value.substring(0, lenData);
  }

  // ************************************************************************
  public static String padRightSpace(String param, int length) throws Exception {
    int len1 = param.getBytes("MS950").length;
    int len2 = param.getBytes().length;
    int len3 = length - (len2 - len1);
    return String.format("%1$-" + len3 + "s", param);
  }

  // ************************************************************************
  public static String padLeftSpace(String param, int length) throws Exception {
    int len1 = param.getBytes("MS950").length;
    int len2 = param.getBytes().length;
    int len3 = length - (len2 - len1);
    return String.format("%1$" + len3 + "s", param);
  }
  // ************************************************************************

  public String fillRightSpace(String value, int lenData) throws Exception {
    if (spaceControl == 0) {
      spaceControl = 1;
      spaces = "";
      for (int i = 0; i < maxSpace; i++) {
        spaces = spaces + " ";
      }
    }
    if (value == null)
      return spaces.substring(0, lenData);
    byte[] cvtData = value.getBytes("MS950");
    int len = cvtData.length;
    if (len == lenData)
      return value;
    else if (len < lenData)
      return value + spaces.substring(0, lenData - len);
    else
      return new String(cvtData, 0, lenData);
  }

  // ************************************************************************
  public String fillLeftSpace(String value, int lenData) throws Exception {
    if (spaceControl == 0) {
      spaceControl = 1;
      spaces = "";
      for (int i = 0; i < maxSpace; i++) {
        spaces = spaces + " ";
      }
    }

    if (value == null)
      return spaces.substring(0, lenData);
    byte[] cvtData = value.getBytes("MS950");
    int len = cvtData.length;
    if (len == lenData)
      return value;
    else if (len < lenData)
      return spaces.substring(0, lenData - len) + value;
    else
      return new String(cvtData, 0, lenData);
  }

  // ************************************************************************
  public double monthBetween(String begDate, String endDate) throws Exception {
    String cmpbegDate = begDate;
    if (cmpbegDate.length() == 6)
      cmpbegDate = cmpbegDate + "01";
    String cmpendDate = endDate;
    if (cmpendDate.length() == 6)
      cmpendDate = cmpendDate + "01";
    SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
    Date d1 = f.parse(cmpbegDate);
    Date d2 = f.parse(cmpendDate);
    return differenceInMonths(d1, d2);
  }

  // ************************************************************************
  public String transPasswd(int typeNum, String tranPWD) throws Exception {
    int[] addNum = {7, 34, 295, 4326, 76325, 875392, 2468135, 12357924, 123456789};
    int transInt, int1, int2, dataLen, dataInt = 1;
    String[] fDig = {"08122730435961748596", "04112633405865798792", "03162439425768718095",
        "04152236415768798390", "09182035435266718497", "01152930475463788296",
        "07192132465068748593", "02172931455660788394"};
    String tmpStr = "", tmpStr1 = "", retStr = "", fDigstr = "";

    dataLen = tranPWD.length();
    if (typeNum == 0) {
      for (int1 = 0; int1 < dataLen; int1++)
        tmpStr = tmpStr + fDigstr.substring(((int) tranPWD.charAt(int1) - 48) * 2 + 1, 1);
      for (int1 = 0; int1 < dataLen; int1++)
        dataInt = dataInt * 10;
      tmpStr1 = String.valueOf(dataInt + Integer.valueOf(tmpStr) - addNum[dataLen - 1]);
      retStr = tmpStr1.substring(tmpStr1.length() - 1, dataLen);
      return retStr;
    } else {
      tmpStr1 = String.valueOf(Integer.valueOf(tranPWD) + addNum[dataLen - 1]);
      tmpStr = tmpStr1.substring(tmpStr1.length() - 1, dataLen);
      for (int1 = 0; int1 < dataLen; int1++) {
        fDigstr = fDig[int1];
        for (int2 = 0; int2 < 10; int2++)
          if (tmpStr.substring(int1 - 1, 1).equals(fDigstr.substring(int2 * 2, 1))) {
            retStr = retStr + fDigstr.substring(int2 * 2 - 1, 1);
            break;
          }
      }
      return retStr;
    }
  }

  // ************************************************************************
  private double differenceInMonths(Date date1, Date date2) {
    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();

    startCalendar.setTime(date1);
    endCalendar.setTime(date2);

    int months = 0, days = 0;

    months += ((endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)) * 12);

    int startDays = startCalendar.get(Calendar.DAY_OF_MONTH);
    int endDays = endCalendar.get(Calendar.DAY_OF_MONTH);

    if (endDays >= startDays) {
      months += (endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH));
      days += (endDays - startDays);
    } else {
      months += (endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH) - 1);
      days += ((startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - startDays) + endDays);
    }
    double doMonths =
        months + (days * 1.0) / startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    // System.out.println("Difference");
    // System.out.println("Month(s): " + months);
    // System.out.println("day(s): " + days);
    // System.out.println("last mons days: " +
    // startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

    DecimalFormat df = new DecimalFormat("####0.0");
    df.setRoundingMode(RoundingMode.DOWN);
    return Double.parseDouble(df.format(doMonths));
  }

  // ************************************************************************
  public int zipFile(String targetPath, String destinationFilePath, String password) {
    try {
      ZipParameters parameters = new ZipParameters();
      parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
      parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

      if (password.length() > 0) {
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        parameters.setPassword(password);
      }

      ZipFile zipFile = new ZipFile(destinationFilePath);

      File targetFile = new File(targetPath);
      if (targetFile.isFile()) {
        zipFile.addFile(targetFile, parameters);
      } else if (targetFile.isDirectory()) {
        zipFile.addFolder(targetFile, parameters);
      }

    } catch (Exception e) {
      // e.printStackTrace();
      // showLogMessage("I","","ERROR:檔案壓縮失敗...");
      return (1);
    }
    return (0);
  }

  // ************************************************************************
  public int unzipFile(String source, String destination, String password) throws ZipException {
    try {
      ZipFile zipFile = new ZipFile(source);
      if (zipFile.isEncrypted()) {
        zipFile.setPassword(password);
      }
      zipFile.extractAll(destination);
    } catch (ZipException e) {
      // e.printStackTrace();
      // showLogMessage("I","","ERROR:檔案解壓縮失敗...");
      return (1);
    }
    return (0);
  }

  // ************************************************************************
  public void systemExit(int rtnCode) throws ZipException {
    systemExit(rtnCode, "");
  }

  // ************************************************************************
  public void systemExit(int rtnCode, String rtnMsg) throws ZipException {
    System.exit(rtnCode);
  }

  // ************************************************************************
  public boolean isAppActiveX(String PgmName) throws Exception {
      String userhome = System.getProperty("user.home");
      // verify path string
      String tempPath = SecurityUtil.verifyPath(userhome);
      String tempFilename = SecurityUtil.verifyPath(PgmName + ".CheckRun..tmp");
    File file = new File(tempPath, tempFilename); // new File(System.getProperty("user.home"), PgmName + ".CheckRun..tmp");
    String ppDir = System.getProperty("user.home") + "/" + PgmName + ".CheckRun..tmp";
    // showLogMessage("I", "888", "888 dir=" + pp_dir );

    channel = new RandomAccessFile(file, "rw").getChannel();

    lock = channel.tryLock();
    if (lock == null) {
      return true;
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          lock.release();
          channel.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    return false;
  }

  // ************************************************************************
  public boolean isAppActive(String checkProgram) throws Exception {
    return isAppActive2(checkProgram);
  }

  // ************************************************************************
  public boolean isAppActive2(String checkProgram) throws Exception {

	String osName = System.getProperty("os.name");
    if (osName == null || osName.startsWith("Windows")) {
      return false;
    }

    String process;
//    Process p = Runtime.getRuntime().exec("ps -few"); // AIX does not have -w
    Process p = Runtime.getRuntime().exec("ps -elf");
    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
    int i = 0;
    while ((process = input.readLine()) != null) {
      if (process.indexOf(checkProgram) != -1) {
        i++;
      }
    }
    input.close();
    if (i > 1) {
      return true;
    }
    return false;
  }

  // ************************************************************************
  public String structStr(String StrBuf, int[] begPos, int strLen) {
    String buf = "";
    if (begPos[0] > StrBuf.length())
      return "";
    int endPos = begPos[0] + strLen;
    if (begPos[0] + strLen > StrBuf.length() + 1)
      endPos = StrBuf.length();
    buf = StrBuf.substring(begPos[0] - 1, endPos - 1);
    begPos[0] = endPos;
    return buf;
  }

  // ************************************************************************
  public String structStrB(String StrBuf, int[] begPos, int strLen) {
    int intLen = 0;
    String buf = "";
    int begByte = 0, endByte = 0;
    for (int inti = 0; inti < StrBuf.length(); inti++) {
      if (begPos[0] == intLen + 1)
        begByte = inti;
      if (StrBuf.charAt(inti) >= 128)
        intLen++;
      intLen++;
    }
    // System.out.println("STEP 001 begpos=["+begPos[0]+"] begByte=["+begByte+"]["+strLen+"]");

    intLen = 0;
    endByte = 0;
    for (int inti = begByte; inti < StrBuf.length(); inti++) {
      if (StrBuf.charAt(inti) >= 128)
        intLen++;
      intLen++;
      if (strLen == intLen) {
        endByte = inti + 1;
        break;
      }
    }
    // System.out.println("STEP 002 begpos=["+begByte+"] endByte=["+endByte+"]");
    if (endByte == 0)
      endByte = StrBuf.length();

    buf = StrBuf.substring(begByte, endByte);
    begPos[0] = endByte + 1;
    return buf;
  }

  // ************************************************************************
  public int systemCmd(String command, String[] returnMsg, int[] arrInt) {
    String[] commands = new String[20];
    int listCnt = (int) command.chars().filter(ch -> ch == ' ').count();
    if (DEBUG == 1)
      System.out.println("STEP systemCmd STEP [" + command + "][" + listCnt + "]");
    for (int inti = 0; inti < listCnt + 1; inti++) {
      commands[inti] = getStr(command, inti + 1, " ");
      if (DEBUG == 1)
        System.out.println("STEP A1 =[" + commands[inti] + "]");
    }

    String[] y = Arrays.copyOf(commands, listCnt + 1);

    return (systemCmdArr(y, listCnt + 1, returnMsg, arrInt));
  }

  // ************************************************************************
  public int systemCmdArr(String[] commands, int listCnt, String[] returnMsg, int[] arrInt) {
    StringBuffer output = new StringBuffer();

    if (DEBUG == 1)
      System.out.println("STEP systemCmdArr=" + listCnt);
    String[] y = Arrays.copyOf(commands, listCnt);
    /*
     * mark for (int inti=0;inti<list_cnt;inti++)
     * System.out.println("STEP A2 =["+commands[inti]+"]");
     * System.out.println("STEP A3 =["+y.length+"]");
     */
    /*
     * mark for (int inti=0;inti<y.length;inti++)
     * System.out.println("STEP A =["+inti+"]=["+y[inti]+"]");
     */

    int exitVal = 0;
    Process proc;
    try {

      proc = Runtime.getRuntime().exec(y);
      proc.waitFor();
      exitVal = proc.exitValue();
      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      String line = reader.readLine();
      while (line != null) {
        output.append(line); // every output display, many lines
        // showLogMessage("I","","批次啟動:"+ line);
        line = reader.readLine();
      }

    } catch (Exception e) {
      return (1);
    }

    returnMsg[0] = output.toString();
    arrInt[0] = exitVal;
    return (0);
  }

  // ************************************************************************
  public static String getStr(String newLine, int seqIndex, String splitchar) {
    String line = newLine;
    if (line == null)
      line = "";

    int strInt = 1;
    int strSPos = 0;
    int strEPos = 0;
    int cmpFlag = 0;
    for (int inta = 0; inta < line.length(); inta++) {
      if (line.charAt(inta) == "\"".charAt(0)) {
        if (cmpFlag == 1) {
          for (int intb = inta + 1; intb < line.length(); intb++) {
            if (line.charAt(intb) == ' ')
              continue;
            for (int intc = 0; intc < splitchar.length(); intc++)
              if (line.charAt(intb) == splitchar.charAt(intc)) {
                cmpFlag = 0;
                break;
              }
            break;
          }
        } else
          cmpFlag = 1;
      }
      strEPos = inta + 1;

      if (cmpFlag == 0) {
        int inti;
        for (inti = 0; inti < splitchar.length(); inti++) {
          // System.out.println("STEP D.1["+ line.charAt(inta) +"]["+splitchar.charAt(inti)+"]");
          if (line.charAt(inta) == splitchar.charAt(inti))
            break;
        }
        if (inti < splitchar.length()) {
          strEPos = inta;
          if (strInt == seqIndex)
            break;
          strInt++;
          strSPos = inta + 1;
        }
      }

    }
    if (strInt < seqIndex)
      return "";
    if (strEPos == 0)
      return "";
    if (strSPos >= strEPos)
      return "";

    String vals = line.substring(strSPos, strEPos).trim();

    String valsa = vals;
    if (vals.length() > 2)
      if ((vals.substring(0, 1).equals("\""))
          && (vals.substring(vals.length() - 1, vals.length()).equals("\"")))
        valsa = vals.substring(1, vals.length() - 1);

    return valsa;
  }

  // ************************************************************************
  public byte[] hexToByte(String hex) {
    int hexLength = hex.length();
    byte[] data = new byte[hexLength / 2];
    for (int i = 0; i < hexLength; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
          + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }

  // ************************************************************************
  public String sha256(final String strText) throws Exception {
    return sha(strText, "SHA-256");
  }

  // ************************************************************************
  public String sha512(final String strText) throws Exception {
    return sha(strText, "SHA-512");
  }

  // ************************************************************************
  private String sha(final String strText, final String strType) {
    // 返回值
    String strResult = null;

    // 是否是有效字串
    if (strText != null && strText.length() > 0) {
      try {
        // SHA 加密?始
        // 建加密象 並傳入加密類型
        MessageDigest messageDigest = MessageDigest.getInstance(strType);
        // 入要加密的字串
        messageDigest.update(strText.getBytes());
        // 得到 byte 類型果
        byte byteBuffer[] = messageDigest.digest();

        // 將 byte 轉換 string
        StringBuffer strHexString = new StringBuffer();
        // 遍歷 byte buffer
        for (int i = 0; i < byteBuffer.length; i++) {
          String hex = Integer.toHexString(0xff & byteBuffer[i]);
          if (hex.length() == 1) {
            strHexString.append('0');
          }
          strHexString.append(hex);
        }
        // 得到返回結果
        strResult = strHexString.toString();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }

    return strResult;
  }

  // ************************************************************************
  public String nextDate(String date) {
    return nextNDate(date, 1);
  }

  // ************************************************************************
  public String lastDate(String date) {
    return nextNDate(date, -1);
  }

  // ************************************************************************
  public String nextNDate(String date, int nDays) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate parsedDate = LocalDate.parse(date, formatter);
    LocalDate addedDate = parsedDate.plusDays(nDays);
    return addedDate.format(formatter);
  }

  // ************************************************************************
  public String nextNDateMin(String datetime, int nMins)
  // parm1 format yyyymmddhh24miss
  {
    if (datetime.length() == 8)
      datetime = datetime + "000000";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    LocalDateTime parsedDate = LocalDateTime.parse(datetime, formatter);
    LocalDateTime addedDate = parsedDate.plusMinutes(nMins);
    return addedDate.format(formatter);
  }

  // ************************************************************************
  public String lastdateOfmonth(String date) {
    if (date.length() == 6)
      date = date + "01";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate parsedDate = LocalDate.parse(date, formatter);
    LocalDate lastDay = parsedDate.with(TemporalAdjusters.lastDayOfMonth());
    return lastDay.format(formatter);
  }

  // ************************************************************************
  public String lastMonth(String date) {
    return lastMonth(date, 1);
  }

  // ************************************************************************
  public String lastMonth(String date, int nMonths) {
    String monthDate = date;
    if (monthDate.length() == 6)
      monthDate = monthDate + "01";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter formatterM = DateTimeFormatter.ofPattern("yyyyMM");
    LocalDate parsedDate = LocalDate.parse(monthDate, formatter);

    YearMonth thisMonth = YearMonth.from(parsedDate);
    YearMonth lastMonth = thisMonth.minusMonths(nMonths);

    return lastMonth.format(formatterM);
  }

  // ************************************************************************
  public String nextMonth(String date) {
    return nextMonth(date, 1);
  }

  // ************************************************************************
  public String nextMonth(String date, int nMonths) {
    String monthDate = date;
    if (monthDate.length() == 6)
      monthDate = monthDate + "01";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter formatterM = DateTimeFormatter.ofPattern("yyyyMM");
    LocalDate parsedDate = LocalDate.parse(monthDate, formatter);

    YearMonth thisMonth = YearMonth.from(parsedDate);
    YearMonth nextMonth = thisMonth.plusMonths(nMonths);

    return nextMonth.format(formatterM);
  }

  // ************************************************************************
  public String nextMonthDate(String date, int nMonths) throws Exception {
    String monthDate = date;

    if (monthDate.length() == 6)
      monthDate = monthDate + "01";
    if (!checkDateFormat(monthDate, "yyyymmdd"))
      return monthDate;
    DateTimeFormatter formatterM = DateTimeFormatter.ofPattern("yyyyMM");
    DateTimeFormatter formatterD = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate parsedDate = LocalDate.parse(monthDate, formatterD);

    YearMonth thisMonth = YearMonth.from(parsedDate);
    YearMonth nextMonth = thisMonth.plusMonths(nMonths);

    if (lastdateOfmonth(nextMonth.format(formatterM))
        .compareTo(nextMonth.format(formatterM) + monthDate.substring(6, 8)) < 0)
      return nextMonth.format(formatterM)
          + lastdateOfmonth(nextMonth.format(formatterM)).substring(6, 8);
    else
      return nextMonth.format(formatterM) + monthDate.substring(6, 8);
  }

  // ************************************************************************
  public String toChinDate(String date) {
    return String.format("%3d", Integer.valueOf(date.substring(0, 4)) - 1911)
        + "/"
        + date.substring(4, 6)
        + "/"
        + date.substring(6, 8);
  }

  // ************************************************************************
  public int chineseCharNum(String str) {
    int cNum = 0;
    for (int i = 0; i < str.length();) {
      int codepoint = str.codePointAt(i);
      i += Character.charCount(codepoint);
      if (Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN)
        cNum++;
    }
    return cNum;
  }

  // ************************************************************************
  public String fillSpecChar(String speChar, int lenData) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lenData; i++)
      sb.append(speChar);

    return "" + sb;
  }

  // ************************************************************************
  public String formatReport(String oriStr, String newStr, int charPos) {
    int intLen = 0;
    int inti;

    if (oriStr.length() + chineseCharNum(oriStr) >= charPos) {
      for (inti = 0; inti < oriStr.length(); inti++)
        if (inti + 1 + chineseCharNum(oriStr.substring(0, inti + 1)) >= charPos - 1)
          break;

      String oriStr1 = oriStr.substring(0, inti + 1);
      if (oriStr1.length() + chineseCharNum(oriStr1) >= charPos)
        oriStr = oriStr.substring(0, inti);
      else
        oriStr = oriStr1;
    }

    intLen = charPos - oriStr.length() - chineseCharNum(oriStr) - 1;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < intLen; i++)
      sb.append(" ");

    return oriStr + sb + newStr;
  }

  // ************************************************************************
  public String sqlGetStr(String line, int seqIndex, String splitchar, String endStr) {
    if (line == null)
      line = "";

    int strInt = 1, strSPos = 0, strEPos = 0, cmpFlag = 0, stackFlag = 0;
    for (int inta = 0; inta < line.length(); inta++) {
      if (line.charAt(inta) == "'".charAt(0)) {
        if (cmpFlag == 1)
          cmpFlag = 0;
        else
          cmpFlag = 1;
      }
      if (line.charAt(inta) == "(".charAt(0)) {
        stackFlag++;
        cmpFlag = 1;
      }
      if (line.charAt(inta) == ")".charAt(0)) {
        stackFlag--;
        if (stackFlag <= 0)
          stackFlag = 0;
        if (stackFlag == 0)
          cmpFlag = 0;
      }

      strEPos = inta + 1;

      if (cmpFlag == 0) {
        String cmpStr = "";
        int endFlag = 0;
        if (endStr.length() > 0) {
          if (inta + endStr.length() > line.length()) {
            strEPos = line.length();
            break;
          }
          cmpStr = line.substring(inta, inta + endStr.length());
          if (cmpStr.equals(endStr))
            endFlag = 1;
        }
        if ((cmpStr.equals(endStr) && (endStr.length() > 0))
            || (line.charAt(inta) == splitchar.charAt(0))) {
          strEPos = inta;
          if ((strInt < seqIndex) && (endFlag == 1))
            break;
          if (strInt == seqIndex)
            break;
          strInt++;
          strSPos = inta + 1;
        }
      }
    }
    if (strInt < seqIndex)
      return "";
    if (strEPos == 0)
      return "";
    if (strSPos >= strEPos)
      return "";

    String vals =
        line.substring(strSPos, strEPos).trim().replaceAll("\"$", "").replaceAll("^\"", "");
    return vals;
  }

  // ************************************************************************
  public void sendNoticeEmail(String abendMessage) {
    try {
      EmailObject mail = new EmailObject();

      mail.mailServer = "smtp.gmail.com";
      mail.portNo = "2525";
      mail.from = "jack.liao@hpe.com";
      mail.to = "allen.ho.liao@hpe.com";
      // mail.subject = "PROGRAM : "+javaProgram +" "+programVersion+" ABEND !!";
      mail.subject = "PROGRAM :  ABEND !!";
      mail.bodyText = abendMessage;
      mail.attachFile = "";
    } catch (Exception ex) {
      System.out.println("sendNoticeEmail fail " + ex.getMessage());
      return;
    }

    return;
  }

  // ************************************************************************
  // ************************************************************************
  // ************************************************************************
  public String commAPPC(String sendbuf, String server, int PORT) throws IOException {
    // if(DEBUG==1) showLogMessage("I","","Proc APPC Server 000 ="+server+",port="+PORT);
    return commAPPC(sendbuf, server, PORT, 1024);
  }

  // ************************************************************************
  public String commAPPC(String sendbuf, String server, int port, int recvbufLen)
      throws IOException {
    String recvbuf = "";

    try(Socket socket = new Socket(server, port);) {
      // if(DEBUG==1) showLogMessage("I","","Proc APPC Server="+server+",port="+PORT);
      
      DataInputStream input = null;
      DataOutputStream output = null;

      System.out.println("Starting...");
      System.out.println("sendData : " + sendbuf);
      try {
        while (true) {
          output = new DataOutputStream(socket.getOutputStream());
          System.out.println("Send data : [" + sendbuf + "]");
          output.write(sendbuf.getBytes());
          output.flush();

          input = new DataInputStream(socket.getInputStream());
          int inputLen = 0;
          byte[] inData = new byte[recvbufLen];

          inputLen = input.read(inData, 0, inData.length);
          if (inputLen > 0)
            recvbuf = new String(inData, 0, inputLen, "MS950");

          break;
        }
      } catch (Exception e) {
      } finally {
        if (input != null)
          input.close();
        if (output != null)
          output.close();
        System.out.println("Terminated");
      }
    } catch (IOException e) {
      System.out.println("Exception : " + e.getMessage());
      e.printStackTrace();
    } 

    return recvbuf;
  }

  // ************************************************************************
  public String getDateFreeFormat(String datestr) throws Exception {
    int int1a, int1b;
    int[] slashInt = new int[10];
    String tmpYear = "";
    String tmpMonth = "";
    String tmpDay = "";
    String outstr = "";

    if (datestr.length() == 0)
      return outstr;

    int1a = 0;
    for (int1b = 0; int1b < datestr.length(); int1b++)
      if (datestr.toCharArray()[int1b] == '/' || datestr.toCharArray()[int1b] == '-'
          || datestr.toCharArray()[int1b] == '.') {
        slashInt[int1a] = int1b;
        int1a++;
      }
    if (int1a == 0) {
      outstr = datestr;
    }
    if (int1a == 2) {
      int int1 = slashInt[0];
      int int2 = slashInt[1] - slashInt[0] - 1;
      tmpYear = String.format("%" + int1 + "." + int1 + "s", datestr);
      tmpMonth = String.format("%" + int2 + "." + int2 + "s", datestr.substring(slashInt[0] + 1));
      tmpDay = String.format("%s", datestr.substring(slashInt[1] + 1));

      outstr = String.format("%04d%02d%02d", Integer.parseInt(tmpYear),
          Integer.parseInt(tmpMonth), Integer.parseInt(tmpDay));
    }
    return outstr;
  }

  // ************************************************************************
  public String hideZipData(String hidedata, String hiderefcode) {
    if (hiderefcode.length() == 0)
      return hidedata;
    String refCode = hiderefcode + hiderefcode + hiderefcode + hiderefcode;
    refCode = refCode.substring((int) refCode.charAt(9) & 0xff - 48);

    int signInt = 0;
    char ch = '0';
    String respCode = hidedata;
    String respCodeHex = "";
    for (int inti = 0; inti < hidedata.length(); inti++) {
      int refInt = (int) refCode.charAt(inti) & 0xff - 48;
      int dataInt = (int) hidedata.charAt(inti) & 0xff;

      if (signInt == 0)
        ch = (char) (dataInt + refInt);
      else
        ch = (char) (dataInt - refInt);

      if (inti == 0)
        respCode = ch + respCode.substring(inti + 1);
      else if (inti < hidedata.length() - 1)
        respCode = respCode.substring(0, (inti)) + ch + respCode.substring(inti + 1);
      else
        respCode = respCode.substring(0, (inti)) + ch;

      respCodeHex = respCodeHex + String.format("%02x", (int) respCode.charAt(inti) & 0xff);
      if (signInt == 0)
        signInt = 1;
      else
        signInt = 0;
    }
    return respCodeHex;
  }

  // ************************************************************************
  public String hideUnzipData(String hidedatahex, String hiderefcode) {
    if (hiderefcode.length() == 0)
      return hidedatahex;
    String refCode = hiderefcode + hiderefcode + hiderefcode + hiderefcode;
    refCode = refCode.substring((int) refCode.charAt(9) & 0xff - 48);
    String hex = hidedatahex;
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < hex.length(); i += 2) {
      String str = hex.substring(i, i + 2);
      output.append((char) Integer.parseInt(str, 16));
    }
    hex = output.toString();
//    System.out.println("STEP 3   =[" + hex + "]");

    int signInt = 0;
    char ch = '0';
    String hidedata = hex;
    String respCode = hidedata;
    for (int inti = 0; inti < hidedata.length(); inti++) {
      int refInt = (int) refCode.charAt(inti) & 0xff - 48;
      int dataInt = (int) hidedata.charAt(inti) & 0xff;

      if (signInt == 0)
        ch = (char) (dataInt - refInt);
      else
        ch = (char) (dataInt + refInt);

      if (inti == 0)
        respCode = ch + respCode.substring(inti + 1);
      else if (inti < hidedata.length() - 1)
        respCode = respCode.substring(0, (inti)) + ch + respCode.substring(inti + 1);
      else
        respCode = respCode.substring(0, (inti)) + ch;

      if (signInt == 0)
        signInt = 1;
      else
        signInt = 0;
    }
//    System.out.println("STEP 4   =[" + respCode + "]");
    return respCode;
  }
  // ************************************************************************
  public String[] getTelZoneAndNo(String phone) {
//		String corpTelCode = "";
//		String corpTelNo = "";
//		String corpExtNo = "";
//		
//		if (phone == null) {
//			phone = "";
//		}else {
//			phone = phone.trim();
//		}
//		
//		if (phone.isEmpty()) {
//			corpTelCode = "";
//			corpTelNo = "";
//			corpExtNo = "";
//		} else {
//			if (phone.indexOf("-") == -1) {
//				// 無-
//				if (phone.length() <= 10) {
//					corpTelCode = "";
//					corpTelNo = phone;
//				} else {
//					corpTelCode = phone.substring(0, 3);
//					corpTelNo = phone.substring(3);
//				}
//
//			} else {
//				// 有-
//				corpTelCode = phone.substring(0, phone.indexOf("-")).trim();
//				corpTelNo = phone.substring(phone.indexOf("-") + 1).trim();
//				if ((corpTelNo.indexOf("-") == -1) == false) {
//					corpExtNo = corpTelNo.substring(corpTelNo.indexOf("-") + 1).trim();
//					corpTelNo = corpTelNo.substring(0, corpTelNo.indexOf("-")).trim();
//				}
//			}
//		}
//
//		if (corpTelCode.length() > 4) {
//			String oldP1 = corpTelCode;
//			corpTelNo = corpTelCode.substring(4) + corpTelNo;
//			corpTelCode = corpTelCode.substring(0, 4);
//			System.out.println(String.format("電話區碼長度超過4碼，縮減[%s]為[%s]", oldP1, corpTelNo));
//		}
//
//		if (corpTelNo.length() > 12) {
//			String oldP2 = corpTelNo;
//			corpExtNo = corpTelNo.substring(12);
//			corpTelNo = corpTelNo.substring(0, 12);
//			System.out.println(String.format("電話長度超過12碼，縮減[%s]為[%s]", oldP2, corpTelNo));
//		}
//		
//		if (corpExtNo.length() > 6) {
//			String oldP3 = corpExtNo;
//			corpExtNo = corpExtNo.substring(0, 6);
//			System.out.println(String.format("電話分機超過6碼，縮減[%s]為[%s]", oldP3, corpExtNo));
//		}
//
//		return new String []{corpTelCode, corpTelNo, corpExtNo};
		CommCrd commCrd = new CommCrd();
		try {
			return commCrd.transTelNo(phone);
		} catch (Exception e) {
			System.out.println(String.format("電話[%s]格式異常", phone));
			e.printStackTrace();
			return new String[] {"", "", ""};
		}
	}



} // End of class CommFunction

