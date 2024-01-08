/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/04/12  V1.00.01  Lai        Initial                                    *
* 109-03-24  V1.00.02  Zhenwu Zhu 統一證號檢核方法                                                  *                                                                           *
* 109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-06-09  V1.00.02  shiyuqi      修改程式名稱                                                                                        *
*****************************************************************************/
package busi.ecs;

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

public class CommBusiCrd {
  public final String TSCC_BANK_ID8 = "01700000";
  public final String IPS_BANK_ID4 = "0017";
  public final String ICH_BANK_ID3 = "017";
  public final String TSCC_BANK_ID2 = "54";
  public int maxSpace = 500, maxZero = 30;
  private int zeroControl = 0, spaceControl = 0;
  private String spaces = "", zeros = "";
  FileLock lock;
  FileChannel channel;
  String tempX01 = "";
  String tempX02 = "";
  String tempX10 = "";
  String tempX011 = "";
  int tempInt = 0;
  CommFunction comm = new CommFunction();
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
  public int idCheck(String id) throws Exception {
    id = id.toUpperCase();
    /*
     * if(comm.checkID(p1) == false) return(1); return(0);
     */
    int sum, cnt1;
    int sum1 = 0;
    String val;
    String[] fdig =
        {"10", "11", "12", "13", "14", "15", "16", "17", "34", "18", "19", "20", "21", "22", "35",
            "23", "24", "25", "26", "27", "28", "29", "32", "30", "31", "33"};

    if ((id.charAt(0) < (char) 65) || (id.charAt(0) > (char) 90))
      return (1);
    if (id.length() != 10)
      return (1);

    sum =
        (str2int(fdig[(int) id.charAt(0) - 65].substring(0, 1)) - 0)
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
  public Boolean checkId(String id) {
    Boolean error = false;
    try {
      if (isNumber(getSubString(id, 1, 2)) == true) {
        if ( ! "8".equals(id.substring(1, 1)) || "9".equals(id.substring(1, 1))) {
          if (idCheck(id) != 0) { // 本國人
            error = true;
          }
        } else {
          if (!isValidNewId(id)) { // 外國人新式統號
            error = true;
          }
        }
      } else { // 外國人
        tempX10 = id;
        convertNoNtn();
        tempX011 = String.format("%1.1s", id.substring(9));
        if (!tempX01.equals(tempX011)) {
          error = true;
        }
      }
    } catch (Exception e) {
      error = true;
    }

    return error;
  }
  void convertNoNtn() throws Exception {
    String aftConvert = "";
    String tmpConvert = "";
    int[] prefixN = {1, 9, 8, 7, 6, 5, 4, 3, 2, 1};

    tempX01 = tempX10.substring(0, 1);
    convertDig();
    tmpConvert = String.format("%2d", tempInt);

    tempX01 = tempX10.substring(1, 2);
    convertDig();

    tempX02 = String.format("%2d", tempInt);

    tmpConvert += tempX02.substring(1, 2);
    tmpConvert += tempX10.substring(2, 2 + 7);


    for (int int2 = 0; int2 < 10; int2++) {
      tempX01 = String.format("%1.1s", tmpConvert.substring(int2));
      tempX02 = String.format("%2d", str2int(tempX01) * prefixN[int2]);
      aftConvert += tempX02.substring(1);
    }

    tempInt = 0;
    for (int int2 = 0; int2 < 10; int2++) {
      tempX01 = String.format("%1.1s", aftConvert.substring(int2, int2 + 1));
      tempInt = tempInt + str2int(tempX01);
    }

    tempX02 = String.format("%2d", tempInt);
    tempX01 = tempX02.substring(1);
    tempInt = 10 - str2int(tempX01);

    if (tempInt == 10) {
      tempX01 = String.format("0");
    } else {
      tempX01 = String.format("%1d", tempInt);
    }
  }
  void convertDig() throws Exception {
    if (tempX01.equals("A"))
      tempInt = 10;
    if (tempX01.equals("B"))
      tempInt = 11;
    if (tempX01.equals("C"))
      tempInt = 12;
    if (tempX01.equals("D"))
      tempInt = 13;
    if (tempX01.equals("E"))
      tempInt = 14;
    if (tempX01.equals("F"))
      tempInt = 15;
    if (tempX01.equals("G"))
      tempInt = 16;
    if (tempX01.equals("H"))
      tempInt = 17;
    if (tempX01.equals("I"))
      tempInt = 34;
    if (tempX01.equals("J"))
      tempInt = 18;
    if (tempX01.equals("K"))
      tempInt = 19;
    if (tempX01.equals("L"))
      tempInt = 20;
    if (tempX01.equals("M"))
      tempInt = 21;
    if (tempX01.equals("N"))
      tempInt = 22;
    if (tempX01.equals("O"))
      tempInt = 35;
    if (tempX01.equals("P"))
      tempInt = 23;
    if (tempX01.equals("Q"))
      tempInt = 24;
    if (tempX01.equals("R"))
      tempInt = 25;
    if (tempX01.equals("S"))
      tempInt = 26;
    if (tempX01.equals("T"))
      tempInt = 27;
    if (tempX01.equals("U"))
      tempInt = 28;
    if (tempX01.equals("V"))
      tempInt = 29;
    if (tempX01.equals("W"))
      tempInt = 30;
    if (tempX01.equals("X"))
      tempInt = 31;
    if (tempX01.equals("Y"))
      tempInt = 32;
    if (tempX01.equals("Z"))
      tempInt = 33;
  }
}
