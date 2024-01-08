/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-04-16  V1.00.01  Zuwei       coding standard      *
* 109-08-03  V1.00.01  Zuwei       add log data validation method                       *
* 110-01-08  V1.00.02  tanwei        修改意義不明確變量                           
* 111-01-21  V1.00.04  Justin       fix Redundant Null Check                 * 
* 111-02-14  V1.00.05  Justin       big5 -> MS950                            *
* 111-11-14  V1.00.06  Zuwei        sync from mega                            *
******************************************************************************/
package taroko.base;
/** 字串公用程式
 * 2019-1015   JH    aa2str()
 * 2019-0730   JH    ss_2int()
   2019-0423:  JH    ss_2Num()
 */

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Arrays;

public class CommString {

  public boolean prblClose(String strName) {
    return strIn2(strName, ",80,83,85");
  }

  public String spilt(String strName, int len, String colName) {
    String rowName = "";
    String[] tt = new String[] {strName, ""};
    while (tt[0].length() > 0) {
      rowName += token2(tt, len) + colName;
    }
    return rowName;
  }

  public String aa2Str(String[] arr, char car) {
    if (arr == null || arr.length == 0)
      return "";
    String colName = "";
    for (int ii = 0; ii < arr.length; ii++)
      colName += arr[ii] + car;
    return colName;
  }

  public String formatSqlString(String strName, Object[] aObj) {
    if (aObj == null || aObj.length == 0)
      return strName;

    Object[] obj = aObj;
    String colName = strName.replaceAll("\\?", "'%s'");
    for (int ii = 0; ii < obj.length; ii++) {
      try {
        colName = colName.replaceFirst("%s", obj[ii].toString());
      } catch (Exception ex) {
        if (obj[ii] == null) {
          colName = colName.replaceFirst("'%s'", "NULL");
        } else
          colName = colName.replaceFirst("%s", "<" + ii + ">") + ";" + obj[ii].toString();
      }
    }

    /*--
     if (a_obj==null)
     return s1;
    
     Object[] obj=new Object[a_obj.length];
     for (int ii = 0; ii < a_obj.length; ii++) {
     if (a_obj[ii] == null)
     obj[ii] = "NULL";
     else obj[ii] =a_obj[ii];
     }

     String ss = s1.replaceAll("\\?", "'%s'");
     for (int ii = 0; ii < obj.length; ii++)
     ss = ss.replaceFirst("%s", obj[ii].toString());
     --*/
    return colName;
  }

  public String acctKey(String strName) {
    String colName = nvl(strName);
    if (colName.length() == 8)
      return colName+"000";
    else if (colName.length() == 10)
      return colName + "0";
    return colName;
  }

  public String hideAcctNo(String strName) {
    if (empty(strName))
      return "";
    return mid(strName, 0, 4) + "XXXX" + mid(strName, 8);
  }

  public String hideAddr(String strName) {
    if (empty(strName))
      return "";
    return mid(strName, 0, 6) + "ＸＸＸＸ" + mid(strName, 10);
  }

  public String hideCardNo(String strName) {
    if (strName.trim().length() == 0)
      return "";
    // --信用卡卡號隱第7~12碼-
    return mid(strName, 0, 6) + "XXXXXX" + mid(strName, 12);
  }

  public String hideEmail(String strName) {
    if (empty(strName))
      return "";
    int num = strName.indexOf("@");
    if (num <= 0)
      return strName;

    String colNameOne = strName;
    String colNameTwo = strName.substring(num);
    if (num < 3)
      return "XXX" + colNameTwo;
    return mid(colNameOne, 0, num - 3) + "XXX" + colNameTwo;
  }

  public String hideIdno(String aIdno) {
    if (aIdno.length() < 10)
      return aIdno;
    // 身分證字號隱第4~7碼
    return mid(aIdno, 0, 3) + "XXXX" + mid(aIdno, 7);
  }

  public String hideIdnoName(String aChiName) {
    // --姓名隱第2碼-
    if (aChiName.length() == 0)
      return "";
    return mid(aChiName, 0, 1) + "Ｘ" + mid(aChiName, 2);
  }

  public String hidePassport(String strName) {
    if (empty(strName))
      return "";
    return mid(strName, 0, 3) + "XXXX" + mid(strName, 7);
  }

  public String hideTelno(String strName) {
    if (empty(strName))
      return "";
    return mid(strName, 0, 5) + "XXXX" + mid(strName, 9);
  }

  // -四拾五入-
//-未上PROD-
//public double toNum(double num1, int pp) {
//   return new BigDecimal(num1+0.000000001).setScale(pp,BigDecimal.ROUND_DOWN).doubleValue();
//}
  public double numScale(double num1, int scale) {
	num1 =num1 + 0.00000000000001;
    return new BigDecimal(num1).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
  }
  public double round(double num1, int scale) {
		if (num1>0) {
			num1 =num1 + 0.00000000000001;
		}
		else if (num1 <0) {
			num1 =num1 - 0.00000000000001;
		}
		return new BigDecimal(num1).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	public String numToStr(double num1, double fmt, boolean bZero) {
		String ss="";
		String lsFmt="%"+fmt+"f";
		if (bZero) lsFmt="%0"+fmt+"f";
		return String.format(lsFmt,num1);
	}
	public String numToStr(double num1, String fmt, String cc) {
		String ss="";
		if (isNumber(fmt)) {
			ss =String.format("%"+fmt+"f",num1).replaceAll(" ",cc); //String.valueOf(cc));
		}
		else {
			ss =String.format("%f",num1);
			if ((num1%1)==0) {
				ss =String.format("%.0f",num1);
			}
			else ss =String.format("%f",num1);
		}
		return ss.trim();
	}
	public String formatYmd(String s1) {
		if (empty(s1)) return "";
		if (s1.length()>6) {
			return s1.substring(0,4)+"/"+s1.substring(4,6)+"/"+s1.substring(6);
		}
		if (s1.length()>4 && s1.length()<=6) {
			return s1.substring(0,4)+"/"+s1.substring(4);
		}
		return s1;
	}

  // -Decode-
public String decode(String s1, char cc, String cdeTxt) {
//	switch (cc) {
//		case ',':
//			return decode(s1,cde_txt.split(","));
//		case ';':
//			return decode(s1,cde_txt.split(";"));
//		case '|':
//			return decode(s1,cde_txt.split(";"));
//		default:
//	}
	if (empty(s1) || empty(cdeTxt)) {
		return "";
	}
	String cc2=Character.toString(cc);
	return decode(s1,cdeTxt.split(cc2));
}
public String decode(String s1, String cdeTxt) {
	String ss= decode(s1,cdeTxt.split(","));
	if (empty(ss))
		return s1;
	return ss;
}
public String decode(String s1, String[] aaTxt) {
	if (empty(s1) || aaTxt ==null) return "";
	int ll_loop =aaTxt.length -1;
	for (int ii=0; ii<ll_loop; ii++) {
		if (s1.equals(aaTxt[ii])) {
			return aaTxt[ii + 1];
		}
	}
	return "";
}
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

  public String decode(String strName, String[] id1, String[] txt1) {
    if (strName == null || strName.trim().length() == 0)
      return "";

    int ii = Arrays.asList(id1).indexOf(strName.trim());
    if (ii >= 0 && ii < txt1.length) {
      return txt1[ii];
    }

    return strName;
  }

  public boolean strIn(String strNmae, String colName) {
    if (empty(colName) || empty(strNmae))
      return false;
    return (colName.toUpperCase().indexOf(strNmae.toUpperCase()) >= 0);
  }

  public boolean strIn2(String strName, String colName) {
    if (empty(strName) || empty(colName))
      return false;

    return (colName.toLowerCase().indexOf(strName.toLowerCase()) >= 0);
  }

  // -POS-
  public int pos(String strName, String colName) {
    /*
	 * String tcCode="05"; If (
     * String tcCode="05"; If ( Arrays.asList("05","06","25","26").contains(tcCode) {
     * System.out.println("MATCH"); }
     */
    if (empty(strName) || empty(colName))
      return -1;

    return strName.trim().toLowerCase().indexOf(colName.trim().toLowerCase());
  }

  public int posAny(String strName, String colName) {
    if (strName == null || colName == null)
      return -1;
    if (strName.trim().length() == 0)
      return -1;
    if (colName.trim().length() == 0)
      return -1;

    return strName.toLowerCase().trim().indexOf(colName.toLowerCase().trim());
  }

  // --
  public String rpad(String str, int size) {
    if (size < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (size == 0) {
      return "";
    }
    String colName = "";
    if (str != null) {
      colName = str;
    }

    return String.format("%-" + size + "s", colName);
  }

  public String rpad(String strName, int size, String colName) {
    if (size < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (size == 0) {
      return "";
    }
    String colNameStr = strName;
    if (colNameStr == null)
      colNameStr = "";
    if (colName == null || colName.trim().length() == 0) {
      return String.format("%-" + size + "s", colNameStr);
    }
    while (colNameStr.length() < size) {
      colNameStr += colName;
    }

    return colNameStr.substring(0, size);
  }

  public String lpad(String strName, int size, String colName) {
	    String colNameStr = "";
		if (strName != null) {
			if (strName.length() >= size) {
				return strName.substring(0, size);
			}
			colNameStr = strName;
		}
	  
//  if (strName.length() >= size) {
//    return strName.substring(0, size);
//  }
//
//  String colNameStr = "";
//  if (strName != null)
//    colNameStr = strName;

  String rowName = colName;
  if (rowName == null || rowName.length() == 0)
    rowName = " ";

  while (colNameStr.length() < size) {
    colNameStr = rowName + colNameStr;
  }
  return colNameStr.substring(0, size);
}

  public String space(int size) {
    return rpad(" ", size);
  }

  public String fill(char car, int num) {
    String colName = "";
    for (int ii = 0; ii < num; ii++)
      colName += car;
    return colName;
  }

  public String fill(String strName, int size) {
    if (strName == null) {
      return space(size);
    }

    if (strName.length() >= size) {
      return strName.substring(0, size);
    }

    String colName = "";

    while (colName.length() < size) {
      colName += strName;
    }
    return colName;
  }

  public String left(String strName, int len) {
    String colName = strName;
    if (colName == null || colName.length() == 0) {
      return "";
    }
    if (colName.length() < len)
      return colName;

    return colName.substring(0, len);
  }

  public String right(String strName, int len) {
    String colName = strName;
    if (colName == null || colName.length() == 0) {
      return "";
    }
    if (colName.length() < len)
      return colName;

    return colName.substring(colName.length() - len, colName.length());
  }

  public String mid(String strName, int pos, int len) {
    String colName = strName;
    if (colName == null || colName.length() == 0) {
      return "";
    }

    if (pos >= colName.length()) {
      return "";
    }
    if (colName.length() < (pos + len)) {
      return colName.substring(pos);
    }

    return colName.substring(pos, (pos + len));
  }

  public String mid(String strName, int pos) {
    return mid(strName, pos, strName.length());
  }

  public String midBig5(String strName, int pos, int len) {
    try {
      byte[] bb = strName.getBytes("Big5");
      byte[] cc = new byte[len];
      int ll = pos;
      for (int ii = 0; ii < len; ii++) {
        cc[ii] = bb[ll];
        ll++;
        if (ll > bb.length)
          break;
      }
      return new String(cc, "Big5");
    } catch (Exception ex) {
    }

    return "";
  }

  public String midBig5(String strName, int pos) {
    try {
      byte[] byteArr = strName.getBytes("Big5");
      int len = byteArr.length - pos;
      byte[] bteArr = new byte[len];
      int num = pos;
      for (int ii = 0; ii < len; ii++) {
        bteArr[ii] = byteArr[num];
        num++;
        if (num > byteArr.length)
          break;
      }
      return new String(bteArr, "Big5");
    } catch (Exception ex) {
    }

    return "";
  }

public String midBig5Two(String s1, int pos, int len) {
	return mid3(s1,pos,len);
//	try {
//		byte[] bb = s1.getBytes("Big5");
//		byte[] cc = new byte[len];
//		int ll = pos;
//		for (int ii = 0; ii < len; ii++) {
//			cc[ii] = bb[ll];
//			ll++;
//			if (ll > bb.length)
//				break;
//		}
//		return new String(cc, "Big5");
//	}
//	catch (Exception ex) {
//	}
//
//	return "";
}

public String midBig5Two(String s1, int pos) {
	return mid3(s1,pos);
//	try {
//		byte[] bb = s1.getBytes("Big5");
//		int len = bb.length - pos;
//		byte[] cc = new byte[len];
//		int ll = pos;
//		for (int ii = 0; ii < len; ii++) {
//			cc[ii] = bb[ll];
//			ll++;
//			if (ll > bb.length)
//				break;
//		}
//		return new String(cc, "Big5");
//	}
//	catch (Exception ex) {
//	}
//
//	return "";
}

  public String numFormat(double num1, String fm1) {
    if (fm1 == null || fm1.length() == 0) {
      return "" + num1;
    }

    DecimalFormat df = new DecimalFormat(fm1);
    return df.format(num1);
  }

  // public String num_2str(double num1, String fmt1) {
  // DecimalFormat decFMT;
  // if (empty(fmt1)) {
  // decFMT = new DecimalFormat("#,##0");
  // } else {
  // decFMT = new DecimalFormat(fmt1);
  // }
  // return decFMT.format(num1);
  // }
public String valbyBetween(String s1, String aBeg, String aEnd, String aDate) {
//	if (empty(a_beg) && empty(a_end))
//		return s1;
//	if (empty(a_end))
//		return s1;
//	//-a_date>a_beg-
//	if (ss_comp(a_date,a_beg)<0) {
//		return "";
//	}
//	//-a_date<=a_end-
//	if (!empty(a_end) && ss_comp(a_date,a_end)>0) {
//		return "";
//	}
	return  (between(aDate,aBeg,aEnd) ? s1 : "");
}
public double valbyBetween(double num1, String aBeg, String aEnd, String aDate) {
//	if (empty(a_beg) && empty(a_end))
//		return num1;
//	if (empty(a_end))
//		return num1;
//	//-a_date>a_beg-
//	if (ss_comp(a_date,a_beg)<0) {
//		return 0;
//	}
//	//-a_date<=a_end-
//	if (!empty(a_end) && ss_comp(a_date,a_end)>0) {
//		return 0;
//	}
	return  (between(aDate,aBeg,aEnd) ? num1 : 0);
}
public boolean between(String s1, String aBeg, String aEnd) {
	if (!empty(aBeg) && s1.compareToIgnoreCase(aBeg)<0)
		return false;
	if (!empty(aEnd) && s1.compareToIgnoreCase(aEnd)>0)
		return false;
	return true;
}

//-compare-
public boolean compIgno(String s1, String cond, String s2) {
	return comp(s1.toUpperCase(),cond,s2.toUpperCase());
}
public boolean comp(String s1, String cond, String s2) {
	int liComp=s1.compareTo(s2);
	switch (nvl(cond)) {
		case ">=":
			return (liComp>=0);
		case "<=":
			return (liComp<=0);
		case ">":
			return (liComp>0);
		case "<":
			return (liComp<0);
		case "=":
			return (liComp==0);
	}
	return false;
}
  public int comp(String strName, String colName, int len) {
    String ls1 = strName;
    if (ls1 == null)
      ls1 = "";
    else if (strName.length() > len)
      ls1 = strName.substring(0, len);

    String ls2 = colName;
    if (ls2 == null)
      ls2 = "";
    else if (colName.length() > len)
      ls2 = colName.substring(0, len);

    return ls1.compareTo(ls2);
  }

  public int strComp(String strName, String colName) {
    String ls1 = strName;
    String ls2 = colName;

    if (strName == null)
      ls1 = "";
    if (colName == null)
      ls2 = "";

    return ls1.compareTo(ls2);
  }

  public int strCompIngo(String strName, String colName) {
    String ls1 = strName;
    String ls2 = colName;

    if (strName == null)
      ls1 = "";
    if (colName == null)
      ls2 = "";

    return ls1.compareToIgnoreCase(ls2);
  }

  public String strToYmd(String strName) {
    String colName = strName.trim();
    if (colName.length() == 0) {
      return "";
    }
    if (colName.length() == 8) {
      return colName.substring(0, 4) + "/" + colName.substring(4, 6) + "/" + colName.substring(6, 8);
    } else if (colName.length() == 6) {
      return colName.substring(0, 4) + "/" + colName.substring(4, 6);
    }
    return colName;
  }
public String strToTime(String str) {
	String timeStr = str.trim();
	switch (timeStr.length()) {
		case 6:
			return timeStr.substring(0,2)+":"+timeStr.substring(2,4)+":"+timeStr.substring(4,6);
		case 4:
			return timeStr.substring(0,2)+":"+timeStr.substring(2,4);
	}
	return timeStr;
}

  public String strToBig5(String strName) {
    // throws UnsupportedEncodingException {

    String colName = strName;
    if (colName == null || colName.length() == 0)
      return "";

    try {
      // return new String(ss.getBytes("iso-8859-1"), "big5");
      return new String(colName.getBytes("MS950"), "big5");
    } catch (Exception ex) {
    }
    return strName;
  }

  public String strToBig5(String strName, int len) {
    // throws UnsupportedEncodingException {
    String colName = strName;
    if (colName == null || colName.length() == 0)
      return space(len);

    // byte[] bb = ss.getBytes("iso-8859-1");
    // byte[] cc = new byte[len];
    // for (int ii=0; ii<len; ii++) {
    // cc[ii] = bb[ii];
    // }
    if (strName.length() < len) {
      colName = strName + space(len - strName.length());
    }

    try {
      byte[] byteArr = colName.getBytes("Big5");
      byte[] bytArr = new byte[len];
      for (int ii = 0; ii < len; ii++) {
        bytArr[ii] = byteArr[ii];
      }
      return new String(bytArr, "Big5");
    } catch (Exception ex) {
    }
    return colName;
  }

  public double strToNum(String strName) {
    strName = strName.trim().replaceAll(",", "");
    try {
      return Double.parseDouble(strName);
    } catch (Exception ex) {
    }
    return 0;
  }

  public int strToInt(String strName) {
    strName = strName.trim().replaceAll(",", "");
    // if (isNumber(s1)==false)
    // return 0;
    try {
      return Integer.parseInt(strName);
    } catch (Exception ex) {
      return 0;
    }
  }

  public boolean isNumber(String strName) {
    if (empty(strName))
      return false;

    // for(int ii=0; ii<s1.length(); ii++) {
    // String cc =s1.substring(ii,1).trim();
    // if (cc.length()==0) continue;
    // if ("1234567890,.-".indexOf(cc)<0)
    // return false;
    // }
    strName = strName.trim().replaceAll(",", "");
    if (empty(strName)) {
      return false;
    }

    try {
      double num = Double.parseDouble(strName);
      if (Double.isNaN(num)) {
        return false;
      }
    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  public String intToStr(int num1) {
    /*
	 * int number = 12345;
     * int number = 12345; DecimalFormat decimalFormat = new DecimalFormat("#,##0"); String
     * numberAsString = decimalFormat.format(number);
     */
    DecimalFormat decFMT = new DecimalFormat("###0");
    return decFMT.format(num1);
  }


  public String nvl(String strName) {
    return nvl(strName, "");
  }

  public String nvl(String strName, String colName) {
    if (strName == null) {
      return colName.trim();
    }
    if (strName.trim().length() == 0) {
      return colName.trim();
    }

    return strName.trim();
  }

  public boolean eqIgno(String strName, String colName) {
    String colNameStr = "";
    if (strName != null)
      colNameStr = strName.trim();
    String colNameStrTwo = "";
    if (colName != null)
      colNameStrTwo = colName.trim();
    return colNameStr.equalsIgnoreCase(colNameStrTwo);
  }

  public boolean eqAny(String strName, String colName) {
    // if (s1 == null || s2 == null) {
    // return false;
    // }
    String colNameStr = "";
    if (strName != null)
      colNameStr = strName.trim();
    String strNameStrTwo = "";
    if (colName != null)
      strNameStrTwo = colName.trim();
    return colNameStr.equals(strNameStrTwo);
  }

  public byte[] hexStrToByteArr(String strName) {
    int len = strName.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(strName.charAt(i), 16) << 4) + Character.digit(strName.charAt(i + 1), 16));
    return data;
  }

  public String repeat(String strName, int num) {
    if (strName == null) {
      return new String(new char[num]).replace("\0", " ");
    }
    return new String(new char[num]).replace("\0", strName);
  }
public String replace(String s1, char ccOld, char ccNew) {
	if (s1.length()<=0) return "";
	char[] cc =s1.toCharArray();
	for (int ii=0; ii<cc.length; ii++) {
		if (cc[ii]== ccOld) {
			cc[ii] = ccNew;
		}
	}
	return String.copyValueOf(cc);
}

  public String fmtTelno(String strName, String colName, String rowName) {
    String name = "";
    if (empty(strName) == false)
      name += "(" + strName.trim() + ") ";
    if (empty(colName) == false)
      name += colName.trim();
    if (empty(rowName) == false)
      name += "-" + rowName.trim();
    return name;
  }

  public boolean empty(String strName) {
    if (strName == null)
      return true;
    return strName.trim().length() == 0;
  }

  public String format(String strName, Object... obj) {
    if (obj == null || obj.length == 0)
      return strName;
    for (int ii = 0; ii < obj.length; ii++) {
      if (obj[ii] == null)
        obj[ii] = "NULL";
    }

    String colName = strName.replaceAll("\\?", "%s");
    for (int ii = 0; ii < obj.length; ii++)
      colName = colName.replaceFirst("%s", obj[ii].toString());

    return colName;
  }

  // String log_parm(String s1, Object[] obj){
  // for(int ii=0; ii<obj.length;ii++) {
  // if (obj[ii]==null)
  // obj[ii]="NULL";
  // }
  //
  // String ss=s1.replaceAll("\\?", "'%s'");
  // for(int ii=0; ii<obj.length; ii++)
  // ss =ss.replaceFirst("%s",obj[ii].toString());
  //
  // return ss;
  // }
  public String token(String[] arr) {
    String colName = "";
    if (arr.length == 0) {
      return "";
    }
    if (arr.length < 2) {
      colName = arr[0];
      arr[0] = "";
      return colName;
    }

    String cc1 = arr[1];
    if (cc1.length() == 0) {
      colName = arr[0];
      arr[0] = "";
      return colName;
    }

    int num = arr[0].indexOf(cc1);
    if (num < 0) {
      colName = arr[0];
      arr[0] = "";
      return colName;
    }

    colName = arr[0].substring(0, num);
    arr[0] = arr[0].substring(num + 1);

    return colName;
  }

  public String fixlen(String strName, int len1, char ch) {
    if (len1 <= 0)
      return "";

    String rtn = "";
    int len = 0;
    for (int ii = 0; ii < strName.length(); ii++) {
      int acsii = strName.charAt(ii);
      len += (acsii < 0 || acsii > 128) ? 2 : 1;
      rtn += strName.charAt(ii);
      if (len >= len1)
        break;
    }
    if (len1 > len) {
      String colName = String.valueOf(ch);
      if (empty(colName))
        colName = " ";
      rtn += fill(colName, (len1 - len));
    }

    return rtn;
  }

  public String fixlen(String strName, int len1) {
    return fixlen(strName, len1, ' ');
  }

  public int len(String strName) {
    try {
      return strName.getBytes("MS950").length;
    } catch (UnsupportedEncodingException ex) {
    }
    return strName.length();
  }

  public int len2(String strName) {
//	try {
//		return s1.getBytes("Big5").length;
//	}
//	catch (UnsupportedEncodingException ex) {
//	}
//	return s1.length();
	int len = 0;
	for (int i = 0; i < strName.length(); i++) {
		int acsii = strName.charAt(i);
		len += (acsii < 0 || acsii > 128) ? 2 : 1;
	}
	return len;
  }

  // --
  public String rpad(String str1, int aSize, char ch) {
    if (aSize < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (aSize == 0) {
      return "";
    }
    String lscc = String.valueOf(ch);
    if (empty(lscc))
      lscc = " ";

    int liLen = len(str1);
    if (liLen >= aSize)
      return str1;
    return str1 + fill(lscc, aSize - liLen);

  }

  public String rpad2(String str, int size) {
    if (size < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (size == 0) {
      return "";
    }

    int liLen = len(str);
    if (liLen >= size)
      return str;
    return str + space(size - liLen);
  }

  public String lpad(String str, int size) {
    if (size < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (size == 0) {
      return "";
    }

    int liLen = len(str);
    if (liLen >= size)
      return str;
    return space(size - liLen) + str;
  }

  public String lpad(String str, int size, char ch) {
    if (size < 0) {
      throw new RuntimeException("補空白後的長度不可小於零");
    }
    if (size == 0) {
      return "";
    }

    int liLen = len(str);
    if (liLen >= size)
      return str;
    return fill(ch, size - liLen) + str;
  }

  public String mid2(String strName, int pos) {
    try {
      byte[] arr = strName.getBytes("MS950");
      if (pos >= arr.length)
        return "";

      int len = arr.length - pos;
      byte[] arrByte = new byte[len];
      int ll = pos;
      for (int ii = 0; ii < len; ii++) {
        arrByte[ii] = arr[ll];
        ll++;
        if (ll >= arr.length)
          break;
      }
      return new String(arrByte, "MS950");
    } catch (Exception ex) {
    }

    return "";
  }

  public String mid2(String strName, int pos, int len) {
    try {
      byte[] arr = strName.getBytes("MS950");
      if (pos >= arr.length)
        return "";

      // int ll_src=bb.length;
      byte[] arrByte = new byte[len];
      int num = pos;
      for (int ii = 0; ii < len; ii++) {
        arrByte[ii] = arr[num];
        num++;
        if (num >= arr.length)
          break;
      }
      return new String(arrByte, "MS950");
    } catch (Exception ex) {
    }

    return "";
  }

public String mid3(String s1,int pos) {
	int liLen =s1.length() -pos;
	if (liLen<=0) return "";
	return mid3(s1,pos,liLen);

//	if (empty(s1)) return "";
//	try {
//		byte[] bb =ss_2ms950(s1);
//		if (pos >=bb.length)
//			return "";
//		int len=bb.length - pos; // +1;
//		String ss= new String(subArray(bb,pos,len),"MS950");
//		return ss;
//	}
//	catch (Exception ex) {
//	}
//	return "";

}
public String mid3(String s1, int pos, int len) {
	try {
		if (s1.length()==0) return "";
		String ss =s1;  //new String(s1.getBytes("Big5"),"Big5");
		int liPos=0;
		int liLen2=0;
		String lsRc="";
		while (ss.length()>0) {
			String cc=ss.substring(0,1);
			ss =ss.substring(1);
			liPos =liPos + len(cc);
			if (liPos<=pos) continue;
			liLen2 +=len(cc);
			if (liLen2 >len) break;
			lsRc +=cc;
		}
		return lsRc;
	}
	catch (Exception ex) {
	}

	return "";
	//-----------------------------------
//	if (empty(s1)) return "";
//	try {
//		byte[] bb = ss_2ms950(s1);
//		if (pos >=bb.length)
//			return "";
//		return new String(subArray(bb,pos,len), "Big5");
//	}
//	catch (Exception ex) {
//	}
//	return "";
}

  public String token2(String[] strName, int len) {
	  // Justin
	if (strName == null || strName.length == 0) {
		return "";
	}
    if (len == 0 && strName.length >= 2) {
      strName[1] = strName[0];
      strName[0] = "";
      return strName[1];
    }
    // Justin: 這段邏輯怪怪的，先改成上面
//    if (strName == null || strName.length == 0 || len == 0) {
//        strName[1] = strName[0];
//        strName[0] = "";
//        return strName[1];
//    }
    
    if (strName.length == 1) {
      strName = new String[] {strName[0], ""};
    }
    strName[1] = "";

    if (len(strName[0]) <= len) {
      strName[1] = strName[0];
      strName[0] = "";
      return strName[1];
    }

    strName[1] = mid2(strName[0], 0, len);
    strName[0] = mid2(strName[0], len);

    return strName[1];
  }

public String bbToken(String[] ssa,int len) {
	ssa[1] ="";
	if (ssa[0].length()==0) return "";

	int liLen=len;
	String lsRc="";
	while(liLen>0) {
		String cc =ssa[0].substring(0,1);
		liLen =liLen - len(cc);
		if (liLen <0) break;
		lsRc +=cc;
		ssa[0] =ssa[0].substring(1);
		if (ssa[0].length()==0) break;
	}
	ssa[1] =lsRc;
	return lsRc;
	//-2021-0726--------
//	if (s1==null || s1.length==0 || len==0) {
//		s1[1] =s1[0];
//		s1[0] ="";
//		return s1[1];
//	}
//	if (s1.length ==1) {
//	   s1 =new String[]{s1[0],""};
//   }
//   s1[1] ="";
//
//	if (bb_len(s1[0])<=len) {
//		s1[1] =s1[0];
//		s1[0] ="";
//		return s1[1];
//	}
//
//	s1[1] =bb_mid(s1[0],0,len);
//	s1[0] =bb_mid(s1[0],len);
//
//	return s1[1];
}

  public String[] token(String[] strName, String colName) {
    String[] colNameStr = new String[] {"", ""};
    if (strName.length == 0) {
      return colNameStr;
    }
    if (colName.length() == 0) {
      colNameStr[1] = strName[0];
      return colNameStr;
    }

    int num = strName[0].indexOf(colName);
    if (num < 0) {
      colNameStr[1] = strName[0];
      return colNameStr;
    }

    colNameStr[1] = strName[0].substring(0, num);
    colNameStr[0] = strName[0].substring(num + 1);

    return colNameStr;
  }

  public String[] token(String[] strName, int len) {
    String[] colName = new String[] {"", ""};
    if (strName.length == 0) {
      return colName;
    }
    if (len == 0) {
      strName = new String[] {"", ""};
      return colName;
    }

    if (strName[0].length() <= len || strName[0].length() == 0) {
      strName[1] = strName[0];
      strName[0] = "";
      return strName;
    }

    strName[1] = left(strName[0], len);
    strName[0] = mid(strName[0], len);

    return strName;
  }

  public String rtrim(String strName) {
    int i = strName.length() - 1;
    while (i >= 0 && Character.isWhitespace(strName.charAt(i))) {
      i--;
    }
    return strName.substring(0, i + 1);
  }

  public String ltrim(String strName) {
    int i = 0;
    while (i < strName.length() && Character.isWhitespace(strName.charAt(i))) {
      i++;
    }
    return strName.substring(i);
  }

public static String validateLogData(String message) {
    String msg = Normalizer.normalize(message, Normalizer.Form.NFKC);
    for (String c : new String[] {"%0d", "%0a", "%0A", "%0D", "\r", "\n"}) {
        msg = msg.replaceAll(c, "");
    }
    return msg;
}

/**
 * split the zip code into two parts if the length of the zip code is greater than 3
 * @param zipCode
 * @return a String array in which the first element is the first three numbers of the zip code,
 *  and the second element is the rest numbers. 
 */
public String[] splitZipCode(String zipCode) {
	String[] zipArr = new String[2];
	zipArr[0] = zipCode;
	if (zipCode.length() > 3) {
		zipArr[0] = zipCode.substring(0,3);
		zipArr[1] = zipCode.substring(3);
	}
	return zipArr;
	
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
private byte[] strToMs950(String s1) throws UnsupportedEncodingException {
	return s1.getBytes("MS950");
}

}
