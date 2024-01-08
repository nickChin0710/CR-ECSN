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
* 106/05/22  V1.00.20  Yash       Add parseByAddress function                *
* 106/06/03  V1.00.22  Allen Ho   modify zip,unzip return int                *
* 106/06/23  V1.00.23  Allen Ho   modify structStr last string bug           *
* 106/07/04  V1.00.25  Allen Ho   add lastMonth & nextMonth method           *
* 106/07/18  V1.00.28  Allen Ho   Modify for codereview checkmarx            *
* 106/12/18  V1.00.32  Allen Ho   add  nextMonthDate                         *
* 107/04/24  V1.00.33  Allen Ho   Add toChinDate                             *
* 108/12/30  V1.00.35  Allen Ho   Add passwd zip unzip rule                  *
* 109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  110-01-07  V1.00.02  tanwei        修改意義不明確變量       
* 110-12-17  V1.00.36  Justin     Add new encryption and decryption methods     
* 110-12-23  V1.00.37  Justin     log4j1 -> log4j2                          *
* 110-12-30  V1.00.38  Justin     increase the iteration count for encrypt   *
* 111/01/18  V1.00.39  Justin     fix Erroneous String Compare               *
* 111-01-19  V1.00.40  Justin     fix Unchecked Return Value             *
******************************************************************************/
package busi.ecs;

import java.io.File;
import java.io.*;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

import Dxc.Util.SecurityUtil;

import java.text.ParseException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// add by davidfu@20170515

public class CommFunction {
  final static int ITER_COUNT = 100000; // fix Weak Cryptographic Hash: Insecure PBE Iteration Count
  public int maxSpace = 500, maxZero = 30;
  private int zeroControl = 0, spaceControl = 0;
  private String spaces = "", zeros = "";
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
      Date dateCheck = format.parse(checkData);
    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  // ************************************************************************
  public int datePeriod(String dateStart, String dateStop) throws Exception {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    Date d1 = format.parse(dateStart);
    Date d2 = format.parse(dateStop);
    long diff = d2.getTime() - d1.getTime();

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
    if ("DAY_OF_WEEK".equals(parmConv)) {
      cvtDays = ca1.get(Calendar.DAY_OF_WEEK) - 1;
    } else if ("DAY_OF_YEAR".equals(parmConv)) {
      cvtDays = ca1.get(Calendar.DAY_OF_YEAR);
    } else if ("DAY_OF_WEEK_IN_MONTH".equals(parmConv)) {
      cvtDays = ca1.get(Calendar.DAY_OF_WEEK_IN_MONTH);
    } else if ("WEEK_OF_YEAR".equals(parmConv)) {
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
  public boolean checkIdno(String idString) {
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
    String strName = String.valueOf(Character.toUpperCase(idString.charAt(0)));
    for (int i = 0; i < 26; i++) {
      if (strName.compareTo(firstChar[i]) == 0) {
        inte = i;
      }
    }

    int total = 0;
    int all[] = new int[11];
    String strVal = String.valueOf(inte + 10);
    int numOne = Integer.parseInt(String.valueOf(strVal.charAt(0)));
    int numTwo = Integer.parseInt(String.valueOf(strVal.charAt(1)));
    all[0] = numOne;
    all[1] = numTwo;
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
  public boolean checkCardNo(String cardNo) {
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

    int num = 1, ckSum = 0, calc = 0;

    for (int i = cardNo.length() - 1; i >= 0; i--) {
      calc = Integer.parseInt(cardNo.substring(i, i + 1)) * num;
      if (calc > 9) {
        ckSum = ckSum + 1;
        calc = calc - 10;
      }
      ckSum = ckSum + calc;
      if (num == 1) {
        num = 2;
      } else {
        num = 1;
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
  public boolean isNumber(String ckString) {
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
    destValue =
        new String(tmpData, 0, startPnt) + cvtValue
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
    destValue =
        new String(tmpData, 0, startPnt) + cvtValue
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
    destValue =
        new String(tmpData, 0, startPnt) + cvtValue
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
    byte[] cvtData = value.getBytes();
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
    byte[] cvtData = value.getBytes();
    int len = cvtData.length;
    if (len == lenData)
      return value;
    else if (len < lenData)
      return spaces.substring(0, lenData - len) + value;
    else
      return new String(cvtData, 0, lenData);
  }

  // ************************************************************************
  public int monthBetween(String begDate, String endDate) throws Exception {
    String cmpbegDate = begDate;
    if (cmpbegDate.length() == 6)
      cmpbegDate = cmpbegDate + "01";
    String cmpendDate = endDate;
    if (cmpendDate.length() == 6)
      cmpendDate = cmpendDate + "01";
    // SimpleDateFormat f = new SimpleDateFormat("uuuummdd");
    SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
    Date dateDe = f.parse(cmpbegDate);
    Date dateDa = f.parse(cmpendDate);
    int dateNum = differenceInMonths(dateDe, dateDa);
    return dateNum + 1;
  }

  // ************************************************************************
  public String transPasswd(int typeNum, String tranPWD) throws Exception {
    int[] addNum = {7, 34, 295, 4326, 76325, 875392, 2468135, 12357924, 123456789};
    int transInt, int1, int2, dataLen, dataInt = 1;
    String[] fDig =
        {"08122730435961748596", "04112633405865798792", "03162439425768718095",
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
  private static int differenceInMonths(Date dateOne, Date dateTwo) {
    Calendar calendarOne = Calendar.getInstance();
    calendarOne.setTime(dateOne);
    Calendar calendarTwo = Calendar.getInstance();
    calendarTwo.setTime(dateTwo);
    int diff = 0;
    if (calendarTwo.after(calendarOne)) {
      while (calendarTwo.after(calendarOne)) {
        calendarOne.add(Calendar.MONTH, 1);
        if (calendarTwo.after(calendarOne)) {
          diff++;
        }
      }
    } else if (calendarTwo.before(calendarOne)) {
      while (calendarTwo.before(calendarOne)) {
        calendarOne.add(Calendar.MONTH, -1);
        if (calendarOne.before(calendarTwo)) {
          diff--;
        }
      }
    }
    return diff;
  }

  // ************************************************************************
  public boolean isAppActive(String pgmName) throws Exception {
	  String userhome = System.getProperty("user.home");
	  // verify path
	  userhome = SecurityUtil.verifyPath(userhome);
    File file = new File(userhome, pgmName + ".CheckRun..tmp");
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
  public String structStr(String strBuf, int[] begPos, int strLen) {
    String buf = "";
    if (begPos[0] > strBuf.length())
      return "";
    int endPos = begPos[0] + strLen;
    if (begPos[0] + strLen > strBuf.length() + 1)
      endPos = strBuf.length();
    buf = strBuf.substring(begPos[0] - 1, endPos - 1);
    begPos[0] = endPos;
    return buf;
  }

  // ************************************************************************
  public String structStrB(String strBuf, int[] begPos, int strLen) {
    int intLen = 0;
    int begByte = 0, endByte = 0;
    for (int inti = 0; inti < strBuf.length(); inti++) {
      if (begPos[0] == intLen + 1)
        begByte = inti;
      if (strBuf.charAt(inti) >= 128)
        intLen++;
    }
    intLen = 0;
    endByte = 0;
    for (int inti = begByte + 1; inti < strBuf.length(); inti++) {
      if (strBuf.charAt(inti) >= 128)
        intLen++;
      if (strLen == intLen)
        endByte = inti;
    }
    if (endByte == 0)
      endByte = strBuf.length();
    String buf = "";
    buf = strBuf.substring(begByte, endByte - 1);
    begPos[0] = endByte;
    return buf;
  }

//  // ************************************************************************
//  public int systemCmd(String command, String[] returnMsg, int[] arrInt) {
//    String passCmd = command;
//    StringBuffer output = new StringBuffer();
//
//    // System.out.println("STEP D.1["+ command +"]");
//
//    int exitVal = 0;
//    Process proc;
//    try {
//      proc = Runtime.getRuntime().exec(passCmd);
//      if (proc.waitFor() != 0) {
//  		System.out.println("systemCmdArr錯誤");
//  	  }
//      exitVal = proc.exitValue();
//      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//
//      String line = reader.readLine();
//      while (line != null) {
//        output.append(line); // every output display, many lines
//        line = reader.readLine();
//      }
//
//    } catch (Exception e) {
//      return (1);
//    }
//
//    returnMsg[0] = output.toString();
//    arrInt[0] = exitVal;
//    return (0);
//  }

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

    String vals =
        line.substring(strSPos, strEPos).trim().replaceAll("\"$", "").replaceAll("^\"", "");
    // System.out.println("STEP D.2["+ vals +"]");
    return vals;
  }

  // ************************************************************************
  public byte[] hexToByte(String hex) {
    int num = hex.length();
    byte[] data = new byte[num / 2];
    for (int i = 0; i < num; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character
              .digit(hex.charAt(i + 1), 16));
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

    // 是否是有效字符串
    if (strText != null && strText.length() > 0) {
      try {
        // SHA 加密?始
        // 建加密象 并傳入加密類型
        MessageDigest messageDigest = MessageDigest.getInstance(strType);
        // 入要加密的字符串
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
  public int chineseCharNum(String s) {
    int cNum = 0;
    for (int i = 0; i < s.length();) {
      int codepoint = s.codePointAt(i);
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
  public String sqlGetStr(String newLine, int seqIndex, String splitchar, String endStr) {
    String line = newLine;
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
  public String nextMonthDate(String date, int nMonths) {
    String monthDate = date;

    if (monthDate.length() == 6)
      monthDate = monthDate + "01";
    // if (!checkDateFormat(monthDate,"yyyymmdd")) return monthDate;
    DateTimeFormatter formatterM = DateTimeFormatter.ofPattern("yyyyMM");
    DateTimeFormatter formatterD = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate parsedDate = LocalDate.parse(monthDate, formatterD);

    YearMonth thisMonth = YearMonth.from(parsedDate);
    YearMonth nextMonth = thisMonth.plusMonths(nMonths);

    if (lastdateOfmonth(nextMonth.format(formatterM)).compareTo(
        nextMonth.format(formatterM) + monthDate.substring(6, 8)) < 0)
      return nextMonth.format(formatterM)
          + lastdateOfmonth(nextMonth.format(formatterM)).substring(6, 8);
    else
      return nextMonth.format(formatterM) + monthDate.substring(6, 8);
  }

  // ************************************************************************
  public String toChinDate(String date) {
    return String.format("%3d", Integer.valueOf(date.substring(0, 4)) - 1911) + "/"
        + date.substring(4, 6) + "/" + date.substring(6, 8);
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
    return respCode;
  }
  
	public String encrytPassword(String unhidePassword, String saltStr) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (saltStr == null || saltStr.length() == 0) {
			return unhidePassword;
		}
		byte[] salt = Base64.getDecoder().decode(saltStr.getBytes("UTF8"));
		KeySpec spec = new PBEKeySpec(unhidePassword.toCharArray(), salt, ITER_COUNT, 64);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = factory.generateSecret(spec).getEncoded();
		return new String(Base64.getEncoder().encode(hash), "UTF8");
	}
	
	public byte[] generateSalt() {
		byte[] salt = new byte[4];
		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);
		return salt;
	}
	
	public String generateSaltStr() throws UnsupportedEncodingException {
		return new String(Base64.getEncoder().encode(generateSalt()), "UTF8");
	}
  
  
  // ************************************************************************



} // End of class CommFunction
