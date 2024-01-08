/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei       coding standard      *
*  110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
******************************************************************************/
/*日期公用程式 V.2018-0731.jh
 * 2018-0731:	JH		daysBetween
 * */
package taroko.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommDate {

  private SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  private SimpleDateFormat dtFmt = new SimpleDateFormat("yyyyMMdd");
  private Calendar date1 = Calendar.getInstance();
  private Calendar date2 = Calendar.getInstance();

  public int sysComp(String date1) {
    return sysDate().compareTo(date1);
  }

  public boolean isDate(String strName) {
    if (strName == null || strName.trim().length() == 0)
      return true;

    try {
      dtFmt.setLenient(false);
      Date ldtDate = dtFmt.parse(strName);
      if (ldtDate == null)
        return false;
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  // public String date_2ss(String s1, String fmt) {
  // //-dd-MMM-yy: [27-Jun-18]-
  // SimpleDateFormat ldt_fmt = new SimpleDateFormat(fmt);
  // //dt_fmt.applyPattern(fmt);
  // try {
  // Date ldate=ldt_fmt.parse(s1);
  // return dt_fmt.format(ldate);
  // }
  // catch (ParseException ex) {
  // ;
  // }
  // return s1;
  // }
  public String toDate(String strName) {
    if (strName == null || strName.trim().length() == 0)
      return "";

    try {
      date1.setTime(dtFmt.parse(strName));
    } catch (ParseException ex) {
      return strName;
    }
    return dtFmt.format(date1.getTime());
  }

  public String twDate() {
    Date currDate = new Date();
    // SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String dateStr = form1.format(currDate);
    return (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + dateStr.substring(4, 8);
  }

  public String disptwDate() {
    Date currDate = new Date();
    // SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String dateStr = form1.format(currDate);
    return (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + "/" + dateStr.substring(4, 6) + "/"
        + dateStr.substring(6, 8);
  }

  public String sysDatetime() {
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmss");
    return form1.format(currDate);
  }

  public String sysDate() {
    Date currDate = new Date();
    // SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    return form1.format(currDate).substring(0, 8);
  }

  public String sysTime() {
    Date currDate = new Date();
    // SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    return form1.format(currDate).substring(8, 14);
  }

  public String millSecond() {
    Date currDate = new Date();
    // SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    return form1.format(currDate).substring(14, 17);
  }

  public String dspDate() {
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
    return form1.format(currDate).substring(0, 10);
  }

  public String dspTime() {
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
    return form1.format(currDate).substring(10, 18);
  }

  public String dspDate(String strName) {
    if (strName.length() == 0)
      return "";

    if (strName.length() > 6) {
      return strName.substring(0, 4) + "/" + strName.substring(4, 6) + "/" + strName.substring(6);
    } else if (strName.length() > 4 && strName.length() <= 6) {
      return strName.substring(0, 4) + "/" + strName.substring(4);
    } else
      return strName;
  }

  public String sysAdd(int num, int mount, int number) {
    return dateAdd(sysDate(), num, mount, number);
  }

  public String monthAdd(String strName, int num) {
    if (strName.trim().length() == 0)
      return "";

    String ss = dateAdd(strName, 0, num, 0).substring(0, 6);
    if (ss.length() >= 8)
      return ss.substring(0, 6);
    return ss;
  }

  public String dateAdd(String strName, int num, int numB, int numD) {
    if (strName.length() == 0) {
      return "";
    }
    String lsDate = "";
    if (strName.length() == 8) {
      lsDate = strName;
    } else if (strName.length() == 6) {
      lsDate = strName + "01";
    } else if (strName.length() == 4) {
      lsDate = strName + "0101";
    } else
      return strName;

    // SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
    // Date d1;
    // d1 = sdFmt.parse(ls_date);
    //
    // Calendar c1 = Calendar.getInstance();
    // c1.setTime(d1);
    try {
      date1.setTime(dtFmt.parse(lsDate));
    } catch (ParseException ex) {
      return strName;
    }

    date1.add(Calendar.YEAR, num);
    date1.add(Calendar.MONTH, numB);
    date1.add(Calendar.DATE, numD);

    return dtFmt.format(date1.getTime());
  }

  public String toTwDate(String strName) {
    String lsDate = strName.trim();
    if (lsDate.length() != 8)
      return lsDate;

    return (Integer.parseInt(lsDate.substring(0, 4)) - 1911) + lsDate.substring(4, 8);
  }

  public String twToAdDate(String strName) {
    if (strName.trim().length() == 0)
      return "";

    String colName = strName.trim();
    String lsDate = "" + (19110000 + Long.parseUnsignedLong(colName));
    if (lsDate.equals("19110000"))
      return "";

    if (isDate(lsDate))
      return lsDate;
    return strName;
  }

  public int yearsBetween(String strName, String colName) {
    if (strName == null || strName.trim().length() == 0)
      strName = "0001";
    if (colName == null || colName.trim().length() == 0)
      colName = "0001";
    try {
      date1.setTime(dtFmt.parse((strName + "0101").substring(0, 8)));
      date2.setTime(dtFmt.parse((colName + "0101").substring(0, 8)));
    } catch (ParseException ex) {
      return -9999;
    }
    return (date1.get(Calendar.YEAR) - date2.get(Calendar.YEAR));
  }

  public int monthsBetween(String strName, String colName) {
    if (strName == null || strName.trim().length() == 0)
      strName = "0001";
    if (colName == null || colName.trim().length() == 0)
      colName = "0001";
    try {
      date1.setTime(dtFmt.parse((strName + "0101").substring(0, 8)));
      date2.setTime(dtFmt.parse((colName + "0101").substring(0, 8)));
    } catch (ParseException ex) {
      return -999999;
    }
    int result = yearsBetween(strName, colName) * 12 + date1.get(Calendar.MONTH) - date2.get(Calendar.MONTH);
    // return result == 0 ? 1 : Math.abs(result);
    return result; // ==0 ? 1 : result;
  }

  public int daysBetween(String start, String end) {
    String strName = (start + "0101").substring(0, 8);
    String colName = (end + "0101").substring(0, 8);

    long aDate, bDate;
    try {
      date1.setTime(dtFmt.parse(strName));
      date2.setTime(dtFmt.parse(colName));
      aDate = date1.getTimeInMillis();
      bDate = date2.getTimeInMillis();
    } catch (ParseException ex) {
      return -99999999;
    }
    // 得到两个日期相差多少秒
    return (int) ((bDate - aDate) / (1000 * 60 * 60 * 24));
  }

}
