/** 字串公用程式 V.2019-0523.JH
*  109/07/07  V0.00.03    Zuwei     coding standard, rename field method                   *
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
 * 2019-0523:  JH    between
   2019-0423:  JH    ss_2num()
   2019-0122:  JH    bug: bb_mid()
*  2019-0114:  JH    xx_hi_xxx()
 * 2018-0921:	JH		bb_mid()
 * 2018-0724:	JH		++hi_addr()
 * 2018-0720:	JH		++fixnum()
 * 2018-0715:	JH		++bb_fixlen()
 * 2018-0605:	JH		++round()
 * 110-01-07   V1.00.02    shiyuqi       修改无意义命名       
 * 111-01-21  V1.00.04  Justin       fix Redundant Null Check                                                                    *
 */
package com;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Arrays;

public class CommString {
	
	public boolean arrayFind(String[] arr, String s1) {
		return Arrays.asList(arr).contains(s1);
	}

  public boolean between(String val1, String nvl1, String nvl2) {
    String param1 = nvl(val1);
    String param2 = nvl(nvl1);
    String param3 = nvl(nvl2);
    if (empty(param2) == false && param1.compareTo(param2) < 0)
      return false;
    if (!empty(param3) && param1.compareTo(param3) > 0)
      return false;
    return true;
  }

  public String ibm2asc(byte[] bbData, int pp, int len) {
    try {
      byte[] bbData1 = new byte[len];
      System.arraycopy(bbData, pp, bbData1, 0, len);
      return new String(bbData1, 0, bbData1.length, "Cp1047");
    } catch (Exception ex) {
    }
    return "";
  }

  public int payRate2int(String param) {
    return ss2int(param);
  }

  public void replace(StringBuffer sb, int ps, String param) {
    sb.replace(ps, ps + param.length(), param);
  }
	public String replace(String string, char oldStr, char newStr) {
		char[] cc =string.toCharArray();
		for (int ii=0; ii<cc.length; ii++) {
			if (cc[ii]== oldStr) {
				cc[ii] = newStr;
			}
		}
		return String.copyValueOf(cc);
	}

  public void insert(StringBuffer sbuf, String param, int ps) {
    if (ps == 0 && param.length() == 0) {
      // sbuf.delete(0,sbuf.length());
      return;
    }
    int len = 0;
    for (int i = 0; i < sbuf.length(); i++) {

      int acsii = sbuf.charAt(i);
      int len2 = (acsii < 0 || acsii > 128) ? 2 : 1;
      if (len + len2 >= ps)
        break;
      len += len2;
    }
    for (int i = len + 1; i < ps; i++)
      sbuf.append(" ");
    sbuf.append(param);
  }

  public void insertCenter(StringBuffer sbuf, String str, int nWidth) {
    int len = 0;
    for (int i = 0; i < sbuf.length(); i++) {
      int acsii = sbuf.charAt(i);
      len += (acsii < 0 || acsii > 128) ? 2 : 1;
    }
    int nSbufWidth = len;

    len = 0;
    for (int i = 0; i < str.length(); i++) {
      int acsii = str.charAt(i);
      len += (acsii < 0 || acsii > 128) ? 2 : 1;
    }
    int nStrWidth = len;

    int nSpace = (nWidth - nStrWidth) / 2 - nSbufWidth;
    sbuf.append(this.space(nSpace));
    sbuf.append(str);
  }

  public String hiCardNo(String param) {
    if (param.trim().length() < 6)
      return "";
    // --信用卡卡號隱第7~12碼-
    if (param.length() > 12)
      return param.substring(0, 6) + "XXXXXX" + param.substring(12);
    return param.substring(0, 6) + "XXXXXX";
  }

  public String hiIdno(String aIdno) {
    if (aIdno.length() < 10)
      return aIdno;
    // 身分證字號隱第4~7碼
    if (aIdno.length() > 7)
      return aIdno.substring(0, 3) + "XXXX" + aIdno.substring(7);
    return aIdno.substring(0, 3) + "XXXX";
  }

  public String hiIdnoName(String aChiName) {
    // --姓名隱第2碼-
    if (aChiName.length() == 0)
      return "";

    if (aChiName.length() >= 3)
      return aChiName.substring(0, 1) + "Ｘ" + aChiName.substring(2);

    return aChiName.substring(0, 1) + "Ｘ";
  }

  public String hiAddr(String param) {
    // substr(a_s1,1,6)||'ＸＸＸＸ'||substr(a_s1,11);
    if (empty(param))
      return "";
    if (param.length() <= 8) {
      return left(param, 6) + "ＸＸＸＸ";
    }

    return left(param, 6) + "ＸＸＸＸ" + param.substring(8);
  }

  public String hiAcctNo(String param) {
    if (param.length() == 0 || param.length() < 4)
      return param;

    // --存款帳號隱第5~8碼-
    if (param.length() < 8)
      return param.substring(0, 4) + "XXXX";

    return param.substring(0, 4) + "XXXX" + param.substring(8);
  }

  // -四拾五入-
  public double numScale(double num1, int param) {
    return new BigDecimal(num1).setScale(param, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  public double round(double num1, int param) {
    return BigDecimal.valueOf(num1).setScale(param, BigDecimal.ROUND_HALF_UP).doubleValue();
    // return new BigDecimal(num1).setScale(pp, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  // -Decode-
  public String decode(String val1, String code1, String txt1) {
    // String c1=",";
    // String c2=",";
    // if (pos("|,;",code1.substring(0,1))>=0)
    // c1 =code1.substring(0,1);
    // if (pos("|,;",txt1.substring(0,1))>=0)
    // c2 =txt1.substring(0,1);
    // return decode(val1,code1.split(c1),txt1.split(c2));
    if (code1.contains(",")) {
      return decode(val1, code1.split(","), txt1.split(","));
    } else if (code1.contains(";")) {
      return decode(val1, code1.split(";"), txt1.split(";"));
    }
    return decode(val1, code1.split("|"), txt1.split("|"));
  }

  public String decode(String param, String[] id1, String[] txt1) {
    if (param == null || param.trim().length() == 0)
      return "";

    int ii = Arrays.asList(id1).indexOf(param.trim());
    if (ii >= 0 && ii < txt1.length) {
      return txt1[ii];
    }

    return param;
  }

  public int bbLen(String param) {
    try {
      return param.getBytes("Big5").length;
    } catch (UnsupportedEncodingException ex) {
    }
    return param.length();
  }

  public boolean ssIn(String param, String param1) {
    if (empty(param) || empty(param1))
      return false;
    return (param1.indexOf(param) >= 0);
  }

  public boolean ssIn(int num1, String num) {
    return ssIn("" + num1, num);
  }

  // -POS-
  public int pos(String param1, String param2) {
    /*
     * String tcCode="05"; If ( Arrays.asList("05","06","25","26").contains(tcCode) {
     * System.out.println("MATCH"); }
     */
    if (param1 == null || param2 == null)
      return -1;
    if (param1.trim().length() == 0)
      return -1;
    if (param2.trim().length() == 0)
      return -1;

    return param1.trim().indexOf(param2.trim());
  }

  public int posAny(String param1, String param2) {
    if (param1 == null || param2 == null)
      return -1;
    if (param1.trim().length() == 0)
      return -1;
    if (param2.trim().length() == 0)
      return -1;

    return param1.toLowerCase().trim().indexOf(param2.toLowerCase().trim());
  }

  // --
  public String rpad(String str, int size) {
    if (size < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (size == 0) {
      return "";
    }
    String str1 = "";
    if (str != null) {
      str1 = str;
    }

    return String.format("%-" + size + "s", str1);
  }

  public String rpad(String param1, int size, String param2) {
    if (size < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (size == 0) {
      return "";
    }
    String string = param1;
    if (string == null)
      string = "";
    if (param2 == null || param2.trim().length() == 0) {
      return String.format("%-" + size + "s", string);
    }
    while (string.length() < size) {
      string += param2;
    }

    return string.substring(0, size);
  }

  public String fixnum(String fmt, int num, int len) {
    if (len > 0) {
      return lpad(String.format(fmt, num), len, "0");
    }
    return String.format(fmt, num);
  }

  public String fixnum(String fmt, double num, int len) {
    if (len > 0) {
      return lpad(String.format(fmt, num), len, "0");
    }
    return String.format(fmt, num);
  }

  public String fixlenNum(String num1, int len1) {
    if (num1.length() >= len1)
      return left(num1, len1);

    return lpad(num1, len1, "0");
  }

  public String bbFixlen(String param1, int len1, char value) {
    if (len1 <= 0)
      return "";

    String rtn = "";
    int len = 0;
    for (int ii = 0; ii < param1.length(); ii++) {
      int acsii = param1.charAt(ii);
      len += (acsii < 0 || acsii > 128) ? 2 : 1;
      rtn += param1.charAt(ii);
      if (len >= len1)
        break;
    }
    if (len1 > len) {
      String valueString = String.valueOf(value);
      if (empty(valueString))
        valueString = " ";
      rtn += fill(valueString, (len1 - len));
    }

    return rtn;
  }

  public String bbFixlen(String param1, int len1) {
    return bbFixlen(param1, len1, ' ');
    // if (len1<=0)
    // return "";
    //
    // String rtn = "";
    // int len = 0;
    // for (int ii = 0; ii< s1.length(); ii++) {
    //
    // int acsii = s1.charAt(ii);
    // len += (acsii < 0 || acsii > 128) ? 2 : 1;
    // rtn += s1.charAt(ii);
    // if (len >= len1)
    // break;
    // }
    // if (len1>len)
    // rtn +=space(len1 - len);
    //
    // return rtn;
  }

  public String bbAdd(String sbuf, String str, int ps) {
    // String rtn = "";
    // int len = 0;
    // for (int i = 0; i < sbuf.length(); i++) {
    //
    // int acsii = sbuf.charAt(i);
    // int n = (acsii < 0 || acsii > 128) ? 2 : 1;
    // if (len + n >= ps)
    // break;
    // len += n;
    // rtn += sbuf.charAt(i);
    // }
    // for (int i = len + 1; i < ps; i++)
    // rtn += " ";
    // rtn += str;
    return bbFixlen(sbuf, ps - 1) + str;
  }

  public String lpad(String param1, int len) {
    return lpad(param1, len, " ");
  }

  public String lpad(String param1, int size, String param2) {
	String string = "";
	if (param1 != null) {
		if (param1.length() >= size) {
			return param1.substring(0, size);
		}
		string = param1;
	}
	  
	  
//    if (param1.length() >= size) {
//      return param1.substring(0, size);
//    }
//
//    String string = "";
//    if (param1 != null)
//      string = param1;

    String string2 = param2;
    if (string2 == null || string2.length() == 0)
      string2 = " ";

    while (string.length() < size) {
      string = string2 + string;
    }
    return string.substring(0, size);
  }

  public String space(int size) {
    return rpad(" ", size);
  }

  public String fill(String param1, int size) {
    if (param1 == null) {
      return space(size);
    }

    if (param1.length() >= size) {
      return param1.substring(0, size);
    }

    String string = "";

    while (string.length() < size) {
      string += param1;
    }
    return string;
  }

  public String left(String param, int len) {
    String string = param;
    if (string == null || string.length() == 0) {
      return "";
    }
    if (string.length() < len)
      return string;

    return string.substring(0, len);
  }

  public String right(String param, int len) {
    String string = param;
    if (string == null || string.length() == 0) {
      return "";
    }
    if (string.length() < len)
      return string;

    return string.substring(string.length() - len, string.length());
  }

  public String mid(String param, int pos, int len) {
    String string = param;
    if (string == null || string.length() == 0) {
      return "";
    }

    if (pos >= string.length()) {
      return "";
    }
    if (string.length() < (pos + len)) {
      return string.substring(pos);
    }

    return string.substring(pos, (pos + len));
  }

  public String bbToken(StringBuffer sb, int len) {
    String string = "";
    if (sb.length() <= len) {
      string = sb.toString();
      sb.delete(0, len);
      return string;
    }

    string = sb.substring(0, len);
    sb.delete(0, len);
    return string;
  }

  public String bbToken(String[] list, int len) {
    list[1] = list[0];
    String mid = bbMid(list[0], 0, len);
    list[0] = bbMid(list[0], len);
    return mid;
  }

  public String bbMid(String param, int pos, int len) {
    try {
      byte[] bytes = param.getBytes("Big5");
      if (pos >= bytes.length)
        return "";

      byte[] spaceBytes = space(len).getBytes("Big5"); // new byte[len];
      int ll = pos;
      for (int ii = 0; ii < len; ii++) {
        spaceBytes[ii] = bytes[ll];
        ll++;
        if (ll >= bytes.length)
          break;
      }
      return new String(spaceBytes, "Big5");
    } catch (Exception ex) {
    }

    return "";
  }

  public String bbMid(String param, int pos) {
    try {
      byte[] bytes = param.getBytes("Big5");
      if (pos >= bytes.length)
        return "";

      int len = bytes.length - pos;
      byte[] splaceBytes = space(len).getBytes("Big5"); // new byte[len];
      int ll = pos;
      for (int ii = 0; ii < len; ii++) {
        splaceBytes[ii] = bytes[ll];
        ll++;
        if (ll >= bytes.length)
          break;
      }
      return new String(splaceBytes, "Big5");
    } catch (Exception ex) {
    }

    return "";
  }
  public byte[] ss2ms950(String s1) throws UnsupportedEncodingException {
		return s1.getBytes("MS950");
	}

  public String mid(String param, int pos) {
    return mid(param, pos, param.length());
  }

  public String midBig5(String param1, int pos, int len) {
    return bbMid(param1, pos, len);
  }

  public String midBig5(String param1, int pos) {
    return bbMid(param1, pos);
  }
  
  public String midBig5(byte[] bb, int pos, int len) throws UnsupportedEncodingException {
		return subString(bb,pos,len,"MS950");
	}
  private String subString(byte[] bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
		if (bytes.length < offset)
			return "";
		int len = bytes.length >= offset + length ? length : bytes.length - offset;
		return new String(subArray(bytes, offset, len), charsetName);
	}
  private byte[] subArray(byte[] bb, int pos, int len) {
		byte[] cc = new byte[len];
		int len2 =bb.length - pos;
		if (len2 <=0)
			return cc;
		if (len2 <len) {
			System.arraycopy(bb, pos, cc, 0, len2);
		}
		else {
			System.arraycopy(bb, pos, cc, 0, len);
		}
		return cc;
	}

  public String numFormat(double num1, String fm1) {
    if (fm1 == null || fm1.length() == 0) {
      return "" + num1;
    }

    DecimalFormat df = new DecimalFormat(fm1);
    return df.format(num1);
  }

  public int ssComp(String param1, String param2, int len) {
    String string1 = param1;
    String string2 = param2;

    if (param1 == null)
      string1 = "";
    if (param2 == null)
      string2 = "";
    if (string1.length() >= len)
      string1 = string1.substring(0, len);
    if (string2.length() >= len)
      string2 = string2.substring(0, len);

    return string1.compareTo(string2);
  }

  public int ssComp(String param1, String param2) {
    String string1 = param1;
    String string2 = param2;

    if (param1 == null)
      string1 = "";
    if (param2 == null)
      string2 = "";

    return string1.compareTo(string2);
  }

  public int ssCompIngo(String param1, String param2) {
    String string1 = param1;
    String string2 = param2;

    if (param1 == null)
      string1 = "";
    if (param2 == null)
      string2 = "";

    return string1.compareToIgnoreCase(string2);
  }

  public String ss2ymd(String param) {
    String trim = param.trim();
    if (trim.length() == 0) {
      return "";
    }
    if (trim.length() == 8) {
      return trim.substring(0, 4) + "/" + trim.substring(4, 6) + "/" + trim.substring(6, 8);
    } else if (trim.length() == 6) {
      return trim.substring(0, 4) + "/" + trim.substring(4, 6);
    }
    return trim;
  }

  public String ss2big5(String param) {
    // throws UnsupportedEncodingException {

    String big = param;
    if (big == null || big.length() == 0)
      return "";

    try {
      return new String(big.getBytes("iso-8859-1"), "big5");
    } catch (Exception ex) {
    }
    return param;
  }

  public String ss2big5(String param, int len) {
    // throws UnsupportedEncodingException {
    String big = param;
    if (big == null || big.length() == 0)
      return space(len);

    // byte[] bb = ss.getBytes("iso-8859-1");
    // byte[] cc = new byte[len];
    // for (int ii=0; ii<len; ii++) {
    // cc[ii] = bb[ii];
    // }
    if (param.length() < len) {
      big = param + space(len - param.length());
    }

    try {
      // byte[] bb = ss.getBytes("Big5");
      byte[] bytes = big.getBytes("MS950");
      byte[] byteLen = new byte[len];
      for (int ii = 0; ii < len; ii++) {
        byteLen[ii] = bytes[ii];
      }
      return new String(byteLen, "Big5");
    } catch (Exception ex) {
    }
    return big;
  }

  public int ss2int(String param) {
    try {
      param = param.trim().replaceAll(",", "");
      if (empty(param) || !isNumber(param)) {
        return 0;
      }
      return Integer.parseInt(param.trim());
    } catch (Exception ex) {
      return 0;
    }
  }

  public double ss2Num(String param) {
    try {
      param = param.trim().replaceAll(",", "");
      if (empty(param) || !isNumber(param)) {
        return 0;
      }
      return Double.parseDouble(param);
    } catch (Exception ex) {
      return 0;
    }
  }

  public boolean isNumber(String param) {
    param = param.trim().replaceAll(",", "");
    if (empty(param)) {
      return false;
    }

    try {
      double num = Double.parseDouble(param.trim().replaceAll(",", ""));
      if (Double.isNaN(num)) {
        return false;
      }
    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  public String int2Str(int num1) {
    /*
     * int number = 12345; DecimalFormat decimalFormat = new DecimalFormat("#,##0"); String
     * numberAsString = decimalFormat.format(number);
     */
    DecimalFormat decFMT = new DecimalFormat("###0");
    return decFMT.format(num1);
  }

  public String nvl(String param) {
    return nvl(param, "");
  }

  public String nvl(String param1, String param2) {
    if (param1 == null) {
      return param2.trim();
    }
    if (param1.trim().length() == 0) {
      return param2.trim();
    }

    return param1.trim();
  }

  public boolean eqIgno(String param1, String param2) {
    String trim1 = "";
    if (param1 != null)
      trim1 = param1.trim();
    String trim2 = "";
    if (param2 != null)
      trim2 = param2.trim();
    return trim1.equalsIgnoreCase(trim2);
  }

  public boolean eqAny(String param1, String param2) {
    // if (s1 == null || s2 == null) {
    // return false;
    // }
    String trim1 = "";
    if (param1 != null)
      trim1 = param1.trim();
    String trim2 = "";
    if (param2 != null)
      trim2 = param2.trim();
    return trim1.equals(trim2);
  }

  public byte[] hexStrToByteArr(String bytes) {
    int len = bytes.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(bytes.charAt(i), 16) << 4) + Character.digit(bytes.charAt(i + 1), 16));
    return data;
  }

  public String replace(String param1, int str, int len, String cc1) {
    if (empty(param1) || len == 0)
      return cc1;
    if (str >= param1.length())
      return cc1;

    String str2 = param1.substring(0, str) + cc1;
    if ((str + len) > param1.length())
      return str2;

    return str2 + param1.substring(str + len);
  }

  public String repeat(String param, int ll) {
    if (param == null) {
      return new String(new char[ll]).replace("\0", " ");
    }
    return new String(new char[ll]).replace("\0", param);
  }

  public String fmtTelno(String param1, String param2, String param3) {
    String str = "";
    if (empty(param1) == false)
      str += "(" + param1.trim() + ") ";
    if (empty(param2) == false)
      str += param2.trim();
    if (empty(param3) == false)
      str += "-" + param3.trim();
    return str;
  }

  public boolean empty(String param1) {
    if (param1 == null)
      return true;
    return param1.trim().length() == 0;
  }

  public String[] token(String[] str, String str1) {
    String[] strs = new String[] {"", ""};
    if (str.length == 0) {
      return strs;
    }
    if (str1.length() == 0) {
      strs[1] = str[0];
      return strs;
    }

    int int1 = str[0].indexOf(str1);
    if (int1 < 0) {
      strs[1] = str[0];
      return strs;
    }

    strs[1] = str[0].substring(0, int1);
    strs[0] = str[0].substring(int1 + str1.length());

    return strs;
  }

  public String token(String[] str) {
    String param = "";
    if (str.length == 0) {
      return "";
    }
    if (str.length < 2) {
      param = str[0];
      str[0] = "";
      return param;
    }

    String strs = str[1];
    if (strs.length() == 0) {
      param = str[0];
      str[0] = "";
      return param;
    }

    int int1 = str[0].indexOf(strs);
    if (int1 < 0) {
      param = str[0];
      str[0] = "";
      return param;
    }

    param = str[0].substring(0, int1);
    str[0] = str[0].substring(int1 + str[1].length());

    return param;
  }

  public String[] token(String[] strs, int len) {
    String[] str = new String[] {"", ""};
    if (strs.length == 0) {
      return str;
    }
    if (len == 0) {
      str[1] = strs[0];
      return str;
    }

    str[1] = strs[0].substring(0, len);
    str[0] = strs[0].substring(len);

    return str;
  }

  public String rtrim(String str) {
    int i = str.length() - 1;
    while (i >= 0 && Character.isWhitespace(str.charAt(i))) {
      i--;
    }
    return str.substring(0, i + 1);
  }

  public String ltrim(String str) {
    int i = 0;
    while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
      i++;
    }
    return str.substring(i);
  }

  // public String logParm(String s1,String[] obj) {
  // String ss = s1;
  //
  // if (s1.indexOf("?")>=0) {
  // for (Object o1 : obj) {
  // if (o1==null) o1="NULL";
  // //if (ss.indexOf("?")>=0)
  // ss = ss.replaceFirst("\\?", "'"+o1+"'");
  // //else ss +="; ["+o1.toString()+"]";
  // }
  // return ss;
  // }
  // for (Object o1:obj) {
  // if (o1==null) o1 ="NULL";
  // int pp =ss.indexOf("%s");
  // if (pp<0) break;
  // ss =ss.replaceFirst("%s",o1.toString());
  // }
  // return ss;
  // }

public static String validateLogData(String message) {
    String msg = Normalizer.normalize(message, Normalizer.Form.NFKC);
    for (String str : new String[] {"%0d", "%0a", "%0A", "%0D", "\r", "\n"}) {
        msg = msg.replaceAll(str, "");
    }
    return msg;
}
}
