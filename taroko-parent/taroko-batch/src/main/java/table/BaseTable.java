/** 
 * ECS-Table公用程式 V.2018-0521-JH
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
 *  110-01-07  V1.00.02    shiyuqi       修改无意义命名                                                                           *
 * 
 * */
package table;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class BaseTable {

  private HashMap<String, Object> parmHash = new HashMap<String, Object>();
  private HashMap<Integer, Object> sortHash = new HashMap<Integer, Object>();

  public String sqlFrom = "";
  private int parmCnt = 0;
  private String parmFlag = "";
  private String convSQL = "";
  private Object[] convParm;
  private String[] parmKey;
  private int parmKeyIndx = 0;
  private String mesg = "";
  // private Object[] _parmVal;
  // private String internalCall="";
  public int pfidx = -1;
  public String useTable = "";

  protected final String sqlDTime = " sysdate ";
  protected final String sqlYYmd = " to_char(sysdate,'yyyymmdd') ";
  protected final String sqlTime = " to_char(sysdate,'hh24miss') ";
  protected final String sqlDual = " SYSIBM.SYSDUMMY1 ";

  // public String ddd_sql() {
  // return new com.commString().logParm(sql_from,this.get_convParm(false));
  // }
  public String getMesg() {
    return mesg;
  }

  public boolean errIndex() {
    if (this.pfidx > 0)
      return false;

    mesg = "canot create prepareStmt; sql=" + sqlFrom;
    return true;
  }

  public void ppp(int i, int parmNum) {
    sortHash.put(i, parmNum);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void ppp(String parmField, int parmNum) {
    int ii = parmKeyFind(parmField);

    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
      parmFlag = "S";
      return;
    }

    ppp(ii, parmNum);
    return;
  }

  public void ppp(int i, double parmNum) {
    sortHash.put(i, parmNum);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void ppp(String parmField, double parmNum) {
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
      parmFlag = "S";
      return;
    }

    ppp(ii, parmNum);
    return;
  }

  // public void setNumber(String col,double num1) {
  // setDouble(col,num1);
  // }
  // public void setNumber(String col) {
  // setDouble(col,0);
  // }
  public void ppp(int i, String parmString) {
    if (parmString == null)
      parmString = "";

    sortHash.put(i, parmString.trim());
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void ppp(String parmField, String parmString) {
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmString);
      parmFlag = "S";
      return;
    }

    if (parmString != null)
      ppp(ii, parmString);
    else
      ppp(ii, "");

    return;
  }

  public void parmInit(String col) {
    ppp(col.trim(), "");
  }

  public void ppRowid(int ii, String parmRowId) {
    sortHash.put(ii, hex2Byte(parmRowId));
    parmFlag = "N";
    parmCnt = ii;
    return;
  }

  public void ppRowid(String parmField, String parmRowId) {
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(),
          hex2Byte(parmRowId));
      parmFlag = "S";
      return;
    }

    ppRowid(ii, parmRowId);
    return;
  }

  public void setDate(String parmField) {
    Date currDate = new Date();
    SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmss");
    String s_date = form_1.format(currDate).substring(0, 8);
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), s_date);
      parmFlag = "S";
      return;
    }

    ppp(ii, s_date);
    return;
  }

  public void setTime(String parmField) {
    Date currDate = new Date();
    SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmss");
    String str = form_1.format(currDate).substring(8);
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), str);
      parmFlag = "S";
      return;
    }

    ppp(ii, str);
    return;
  }

  public void setDateTime(String parmField) {
    Date currDate = new Date();
    SimpleDateFormat form_1 = new SimpleDateFormat("yyyyMMddHHmmss");
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(),
          form_1.format(currDate));
      parmFlag = "S";
      return;
    }

    ppp(ii, form_1.format(currDate));
    return;
  }

  byte[] hex2Byte(String str) {
    int len = str.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
    return data;
  }
  // -VV------------------------------------------------------

  // public boolean has_convSql() {
  // return _convSQL.length()>0;
  // }
  public String getConvSQL() {
    if (convSQL.length() == 0) {
      nameSqlParm(sqlFrom);
    }
    return convSQL;
  }

  public Object[] getConvParm() {
    return getConvParm(true);
  }

  public Object[] getConvParm(boolean b_clear) {
    sort2Parm();
    if (b_clear) {
      sortHash.clear();
    }
    return convParm;
  }

  public void clear() {
    convSQL = "";
    convParm = null;
    parmKey = null;

  }

  public boolean nameSqlParm(String cvtSql) {
    if (cvtSql.trim().length() == 0) {
      cvtSql = this.sqlFrom;
    }
    convSQL = cvtSql;
    convParm = null;
    parmKey = null;
    // _parmVal=null;

    if (Arrays.asList("N", "S").contains(parmFlag) == false) {
      return false;
    }

    HashMap<Integer, String> kkHash = new HashMap<Integer, String>();

    if (parmFlag.equals("S")) {
      Object[] keys1 = parmHash.keySet().toArray();
      Arrays.sort(keys1);
      for (Object parmKey : keys1) {
        String str = parmKey.toString();
        String replaceField = str.substring(str.indexOf(":"));
        // -相同變數取代-
        int ii = cvtSql.indexOf(replaceField);
        while (ii > 0) {
          convSQL = convSQL.replaceFirst(replaceField, "?");
          sortHash.put(ii, parmHash.get(parmKey));
          kkHash.put(ii, replaceField.toUpperCase().trim());
          parmCnt++;
          cvtSql = cvtSql.replaceFirst(replaceField,
              new String(new char[replaceField.length()]).replace("\0", "-"));
          // -next-value-
          ii = cvtSql.indexOf(replaceField);
        }
      }
    }

    if (parmCnt == 0) {
      return false;
    }

    // sort_2Parm();
    // _convParm = new Object[parmCnt];
    // Object[] keys2 = sortHash.keySet().toArray();
    // Arrays.sort(keys2);
    // int i=0;
    // for( Object sortData : keys2) {
    // _convParm[i++] = sortHash.get(sortData);
    // }

    if (kkHash.size() > 0) {
      parmKey = new String[kkHash.size()];
      Object[] param = kkHash.keySet().toArray();
      Arrays.sort(param);
      int ii = 0;
      for (Object ss : param) {
        parmKey[ii] = kkHash.get(ss).trim().toUpperCase().replace(":", "");
        // System.out.println(_parmKey[ii]);
        ii++;
      }
    }

    // sortHash.clear();
    parmHash.clear();
    parmCnt = 0;
    parmFlag = "";
    // return param;
    return true;
  }

  public String pmkk(int ii, String param1) {
    if (param1.trim().length() == 0)
      return "";
    parmKeyIndx = ii;
    return pmkk(param1);
  }

  public String pmkk(String param1) {
    if (param1.trim().length() == 0)
      return "";
    parmKeyIndx++;
    String col = param1.replaceAll(",", "").replaceAll(":", "").trim();
    sortHash.put(parmKeyIndx, col.toLowerCase().trim());

    return param1.replace(col, "?").replaceAll(":", "");
  }

  public void parmKeySort() {
    Object[] keys2 = sortHash.keySet().toArray();
    this.parmKey = new String[keys2.length];
    Arrays.sort(keys2);
    int ii = -1;
    for (Object sortData : keys2) {
      ii++;
      parmKey[ii] = sortHash.get(sortData).toString().toUpperCase().trim();
    }
    sortHash.clear();
    return;
  }

  public String parmKeyPrint() {
    Object[] keys2 = sortHash.keySet().toArray();
    Arrays.sort(keys2);
    String str = "";
    for (Object sortData : keys2) {
      str += "," + sortData.toString() + ":" + sortHash.get(sortData);
    }
    return str;
  }

  public void parmValueClear() {
    sortHash.clear();
  }

  void sort2Parm() {
    if (parmKey.length > 0) {
      convParm = new Object[parmKey.length];
      for (int ii = 0; ii < parmKey.length; ii++) {
        convParm[ii] = sortHash.get(ii);
      }
    } else {
      convParm = new Object[sortHash.size()];
      Object[] keys2 = sortHash.keySet().toArray();
      Arrays.sort(keys2);
      int ii = -1;
      for (Object sortData : keys2) {
        ii++;
        convParm[ii] = sortHash.get(sortData);
      }
    }
  }

  int parmKeyFind(String col) {
    if (parmKey == null) {
      this.parmKeySort();
    }
    return Arrays.asList(parmKey).indexOf(col.trim().toUpperCase());
  }

}
