package com;
/** 日期公用程式 V.2018-1114
*  109/07/22  V0.00.04    Zuwei     coding standard, rename field method                   *
*  2020-0715 V0.00.03     JustinWu  ++ getLastTwoTWDate
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
 * 2019-0923   JH    ++isNumber()
   2018-1117:  JH    month_add()
 * 2018-0716:	JH		++toYddd()
 * 2018-0307:	JH		++monthAdd()
 * 110-01-07  shiyuqi       修改无意义命名                                                                           *
 * 
 * */
//import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class CommDate {

  private SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  private SimpleDateFormat dtFmt = new SimpleDateFormat("yyyyMMdd");
  private Calendar date1 = Calendar.getInstance();
  private Calendar date2 = Calendar.getInstance();

  public int sysComp(String date1) {
    return date1.compareTo(sysDate());
  }

  public boolean isDate(String param) {
    if (param == null || param.trim().length() == 0)
      return true;
    if (!isNumber(param))
      return false;
    try {
      dtFmt.setLenient(false);
      Date ldtDate = dtFmt.parse(param);
      if (ldtDate == null)
        return false;
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  public String toDate(String param) {
    if (param == null || param.trim().length() == 0 || isNumber(param) == false)
      return "";

    try {
      date1.setTime(dtFmt.parse(param));
    } catch (ParseException ex) {
      return param;
    }
    return dtFmt.format(date1.getTime());
  }

  public String twDate() {
    Date currDate = new Date();
    String dateStr = form1.format(currDate);
    return (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + dateStr.substring(4, 8);
    // String dateStr = "", dispStr = "";
    // Date currDate = new Date();
    // SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    // SimpleDateFormat form_2 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
    // SimpleDateFormat form_3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    // dateStr = form_1.format(currDate);
    // dispStr = form_2.format(currDate);
    // SQLTime = form_3.format(currDate);
    //
    // sysDate = dateStr.substring(0, 8);
    // chinDate = (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + dateStr.substring(4, 8);
    // sysTime = dateStr.substring(8, 14);
    // millSecond = dateStr.substring(14, 17);
    // dispDate = dispStr.substring(0, 10);
    // dispTime = dispStr.substring(10, 18);
  }

  public String disptwDate() {
    Date currDate = new Date();
    // SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String dateStr = form1.format(currDate);
    return (Integer.parseInt(dateStr.substring(0, 4)) - 1911)
        + "/"
        + dateStr.substring(4, 6)
        + "/"
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

  public String dspTime(String param) {
    if (param.length() == 0)
      return "";
    if (param.length() > 4)
      return param.substring(0, 2) + ":" + param.substring(2, 4) + ":" + param.substring(4);
    else if (param.length() > 2)
      return param.substring(0, 2) + ":" + param.substring(2);
    return param;
  }

  public String dspDate(String param) {
    if (param.length() == 0)
      return "";

    if (param.length() > 6) {
      return param.substring(0, 4) + "/" + param.substring(4, 6) + "/" + param.substring(6);
    } else if (param.length() > 4 && param.length() <= 6) {
      return param.substring(0, 4) + "/" + param.substring(4);
    }
    return param;
  }

  public String monthAdd(String param, int month) {
    if (param.trim().length() == 0)
      return "";

    String date = dateAdd(param, 0, month, 0).substring(0, 6);
    if (date.length() == 8)
      return date.substring(0, 6);
    return date;
  }

  public String dateAdd(String param, int year, int month, int day) {
    if (param.length() == 0) {
      return "";
    }
    String lsDate = "";
    if (param.length() == 8) {
      lsDate = param;
    } else if (param.length() == 6) {
      lsDate = param + "01";
    } else if (param.length() == 4) {
      lsDate = param + "0101";
    } else
      return param;

    // SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
    // Date d1;
    // d1 = sdFmt.parse(ls_date);
    //
    // Calendar c1 = Calendar.getInstance();
    // c1.setTime(d1);
    try {
      date1.setTime(dtFmt.parse(lsDate));
    } catch (ParseException ex) {
      // ex.printStackTrace();
      return param;
    }

    date1.add(Calendar.YEAR, year);
    date1.add(Calendar.MONTH, month);
    date1.add(Calendar.DATE, day);
    return dtFmt.format(date1.getTime());
  }

  public String monthAdd(String param, int year, int month, int day) {
    String lsDate = dateAdd(param, year, month, day);
    if (lsDate.length() >= 6) {
      return lsDate.substring(0, 6);
    }
    return lsDate;
  }

  public String toTwDate(String param) {
    String lsDate = param.trim();
    if (lsDate.length() != 8)
      return lsDate;
    if (!isNumber(lsDate))
      return lsDate;

    return (Integer.parseInt(lsDate.substring(0, 4)) - 1911) + lsDate.substring(4);
  }
  
  public String tw2adYmd(String s1) {
		String ss=s1.trim();
		if (s1.length()==0 || !isNumber(ss))
			return "";

		switch (ss.length()) {
			case 1:
			case 2:
			case 3:
				return ""+(1911+Long.parseUnsignedLong(ss));
			case 4:
			case 5:
				return ""+(191100+Long.parseUnsignedLong(ss));
			case 6:
			case 7:
				return ""+(19110000+Long.parseUnsignedLong(ss));
		}
		return s1;
	}

  public String tw2adDate(String param) {
    String param1 = param.trim();
    if (param.length() == 0)
      return "";
    if (!isNumber(param1))
      return "";

    if (Long.parseUnsignedLong(param1) <= 10100)
      return "";

    String lsDate = "" + (19110000 + Long.parseUnsignedLong(param1));
    if (isDate(lsDate))
      return lsDate;
    return param;
  }

  boolean isNumber(String param) {
    if (param.trim().length() == 0)
      return false;
    Pattern pattern1 = Pattern.compile("[0-9]*");
    return pattern1.matcher(param.trim()).matches();
  }

  public int yearsBetween(String param1, String param2) {
    if (param1 == null || param1.trim().length() == 0)
      param1 = "0001";
    if (param2 == null || param2.trim().length() == 0)
      param2 = "0001";
    try {
      date1.setTime(dtFmt.parse((param1 + "0101").substring(0, 8)));
      date2.setTime(dtFmt.parse((param2 + "0101").substring(0, 8)));
    } catch (ParseException ex) {
      return -9999;
    }
    return (date1.get(Calendar.YEAR) - date2.get(Calendar.YEAR));
  }

  public int monthsBetween(String month, String month1) {
    if (month == null || month.trim().length() == 0)
      month = "0001";
    if (month1 == null || month1.trim().length() == 0)
      month1 = "0001";
    try {
      date1.setTime(dtFmt.parse((month + "0101").substring(0, 8)));
      date2.setTime(dtFmt.parse((month1 + "0101").substring(0, 8)));
    } catch (ParseException ex) {
      return -999999;
    }
    int result = yearsBetween(month, month1) * 12 + date1.get(Calendar.MONTH) - date2.get(Calendar.MONTH);
    // return result == 0 ? 1 : Math.abs(result);
    return result; // ==0 ? 1 : result;
  }

  @SuppressWarnings("deprecation")
  public int daysBetween(String start, String end) {
    String startDay = (start + "0101").substring(0, 8);
    String endDay = (end + "0101").substring(0, 8);

    // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    long aDate, bDate;
    try {
      date1.setTime(dtFmt.parse(startDay));
      date2.setTime(dtFmt.parse(endDay));
      aDate = date1.getTimeInMillis();
      bDate = date2.getTimeInMillis();
      // A_Date = sdf.parse(s1).getHours();
      // B_Date = sdf.parse(s2).getHours();
    } catch (ParseException ex) {
      return -99999999;
    }
    // long dd=B_Date - A_Date;
    // 得到两个日期相差多少秒
    return (int) ((bDate - aDate) / (1000 * 60 * 60 * 24));
  }

  public int toYddd(String aDate) {
    if (aDate.length() == 0)
      return 0;

    String bDate = aDate.substring(0, 4) + "0101";
    return daysBetween(bDate, aDate) + 1;

  }

	/**
	 * get the last two number of 民國年
	 * @param queryDate
	 * @return
	 */
	public String getLastTwoTWDate(String queryDate) {
		return String.format("%02d", (Integer.parseInt(queryDate.substring(0,4)) - 1911) % 100);
	}

}
