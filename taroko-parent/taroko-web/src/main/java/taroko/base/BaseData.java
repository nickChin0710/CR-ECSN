/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
* 109-08-03  V1.00.01  Zuwei       fix code scan issue                       *
* 110-03-08  V1.00.02  Justin        add a PDPA logger
* 110-12-23  V1.00.03  Justin      log4j1 -> log4j2                          *
* 111-01-17  V1.00.04  Justin      change logger from public to private      *
* 111/01/18  V1.00.05  Justin      fix Erroneous String Compare              *   
******************************************************************************/
package taroko.base;
/**
 * 2019-0816   JH    modify-review
 *  2018-0926:  JH      col_setNum()
 * 2018-0821:   JH      iempty()
 * 2018-0427:   JH      item_no-find
 * 2018-0419:   JH      key.find
 * 2018-0127:   JH      col_set not trim(), col_ss trim()
 * 2018-0115: ++col_set(col/,double/,int)
 * 110-01-07  V1.00.03  tanwei        修改意義不明確變量                                                                       *
 * */

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import Dxc.Util.MaskLogger;
import taroko.com.TarokoParm;

@SuppressWarnings({"unchecked"})
public class BaseData {

    public Connection[] conn = new Connection[2];

    @SuppressWarnings("rawtypes")
    public HashMap inputHash = new HashMap();
    @SuppressWarnings("rawtypes")
    public HashMap outputHash = new HashMap();
    @SuppressWarnings("rawtypes")
    public HashMap inputType = new HashMap();

    public byte[] carriage = {0x0D, 0x0A, 0x00};
    public String sysDate = "", sysTime = "", sqlTime = "", chinDate = "", dispDate = "",
            dispTime = "", millSecond = "";
    public String newLine = new String(carriage, 0, 2);

    public String sqlID = "", notFound = ""; // -DB2.function owner-

    static public final String[] NULL_PARAMETER = {""};
    public boolean selectAll = false, nullCheck = false;

    public Blob blobValue = null;
    static private Logger logger = null, loggerPDPA = null;
    static final public String NORMAL_LOGGER_NAME = "normallogger";
    static final public String PDPA_LOGGER_NAME = "pdpalogger";

//    public String debugMode = "";
    public String nullString = "";
    public String loginUser = "", loginDeptNo = "", menuSeq = "";
    public int selectCnt = 0, dataCnt = 0, insertPnt = 0;

    public void dateTime() throws Exception {

        String dateStr = "", dispStr = "";
        Date currDate = new Date();
        SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        SimpleDateFormat form2 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
        SimpleDateFormat form3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateStr = form1.format(currDate);
        dispStr = form2.format(currDate);
        sqlTime = form3.format(currDate);

        sysDate = dateStr.substring(0, 8);
        chinDate = (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + dateStr.substring(4, 8);
        sysTime = dateStr.substring(8, 14);
        millSecond = dateStr.substring(14, 17);
        dispDate = dispStr.substring(0, 10);
        dispTime = dispStr.substring(10, 18);

        return;
    }

    public Connection conn() {
        return conn[0];
    }

    public Connection getConn() {
        return conn[0];
    }

    public void commitOnly() {
        try {
            if (conn[0] != null) {
                conn[0].commit();
            }
        } catch (Exception ex) {
            showLog("commitOnly", ex.getMessage());
        }
        return;
    }

    public void rollbackOnly() {
        try {
            if (conn[0] != null) {
                conn[0].rollback();
            }
        } catch (Exception ex) {
            showLog("rollbackOnly", ex.getMessage());
        }
        return;
    }

    public String[] getInBuffer(String fieldName) {
        String[] inputFieldData = {""};
        String col = fieldName.trim().toUpperCase();
        // try {
        nullCheck = false;
        inputFieldData = (String[]) inputHash.get(col);
        if (inputFieldData == null) {
            nullCheck = true;
            return NULL_PARAMETER;
        }
        for (int i = 0; i < inputFieldData.length; i++) {
            if (inputFieldData[i] == null) {
                inputFieldData[i] = "";
            }
            if (inputFieldData[i].equals("###")) {
                inputFieldData[i] = "";
                selectAll = true;
            } else {
                inputFieldData[i] = inputFieldData[i].trim();
            }
        }
        // } catch (Exception ex) {
        // expMethod = "getInBuffer."+fieldName;
        // expHandle(ex);
        // }

        return inputFieldData;
    }

    public String getParameter(String fieldName) {
        return getParameter(0, fieldName);
    }

    public String getParameter(int num, String fieldName) {
        String strName = getInBuffer(fieldName.trim().toUpperCase())[num];
        if (strName == null) {
            return "";
        }
        return strName.trim();
    }

    public String getValue2(String fieldName, int num) {
        // DB value no-trim()
        fieldName = fieldName.trim().toUpperCase() + "-" + num;
        String strName = (String) outputHash.get(fieldName);
        if (strName == null) {
            return nullString;
        }

        return strName;
    }

    public String getValue(String fieldName) {

        return getValue(fieldName, 0);
    }

    public String getValue(String fieldName, int num) {
        String retnStr = "";
        // try {
        fieldName = fieldName.trim().toUpperCase() + "-" + num;
        retnStr = (String) outputHash.get(fieldName);
        if (retnStr == null) {
            return nullString;
        }

        retnStr = retnStr.trim();
        // } catch (Exception ex) {
        // expMethod = "getValue";
        // expHandle(ex);
        // }

        return retnStr;
    }

    public double getNumber(String fieldName, int num) {
        return colNum(num, fieldName);
    }

    public String getBlobValue() {
        String retnStr = "";
        InputStream blobIn = null;
        try {
            blobIn = blobValue.getBinaryStream();
            byte[] blobData = new byte[500];
            int readLength = 0;
            while (true) {
                readLength = blobIn.read(blobData);
                if (readLength == -1) {
                    break;
                }
                retnStr = retnStr + new String(blobData, 0, readLength);
            }
            retnStr = retnStr.trim();
        } catch (Exception ex) {
            showLog("getBlobValue", ex.getMessage());
        } finally {
            try {
                if (blobIn != null) {
                    blobIn.close();
                }
                blobIn = null;
            } catch (Exception ex2) {
            }
        }
        return retnStr;
    }


    public void setValue(String fieldName, String setData) {

        setValue(fieldName, setData, 0);

    }

    public void setValue(String fieldName, String setData, int num) {
        // try {
        if (setData == null) {
            setData = "";
        }

        // setData = setData.trim();
        fieldName = fieldName.trim().toUpperCase() + "-" + num;
        outputHash.put(fieldName, setData);
        // } catch (Exception ex) {
        // ddd("setValue: "+ex.getMessage());
        // }
    }

    public void setNumber(String fieldName, double num1, int num) {
        // try {
        String ss = "0";
        if ((num1 % 1) == 0) {
            ss = "" + (long) num1;
        } else
            ss = "" + num1;
        fieldName = fieldName.trim().toUpperCase() + "-" + num;
        outputHash.put(fieldName, ss);
        // } catch (Exception ex) {
        // expMethod = "setNumber";
        // expHandle(ex);
        // }

        return;
    }

    public void dataClear(String col) {
        colSet(col, "");
        itemSet(col, "");
    }

    public void colClear(int num, String col) {
        removeValue(col, num);
    }

    public void removeValue(String fieldName, int num) {
        // try {
        fieldName = fieldName.trim().toUpperCase() + "-" + num;
        outputHash.remove(fieldName);
        // } catch (Exception ex) {
        // expHandle("removeValue",ex);
        // }

    }

    public void setParameter(String col, String val) {
        // try {
        // String[] setValue = {val};
        // setInBuffer(col, setValue);
        setInBuffer(col, new String[] {val});
        // } catch (Exception ex) {
        // expMethod = "setParameter."+fieldName;
        // expHandle(ex);
        // }
    }

    public void setInBuffer(String col, String[] aaVal) {
        if (aaVal == null) {
            return;
        }

        // try {
        // String col = fieldName.trim().toUpperCase();
        for (int i = 0; i < aaVal.length; i++) {
            // if (aa_val[i] == null) {
            // aa_val[i] = "";
            // continue;
            // }
            aaVal[i] = (aaVal[i] == null) ? "" : aaVal[i].trim();
        }
        inputHash.put(col.trim().toUpperCase(), aaVal);
        // } catch (Exception ex) {
        // expMethod = "setInBuffer."+fieldName;
        // expHandle(ex);
        // }
    }

    public void colSetNum(String col, double num1, int param) {
        colSetNum(0, col, num1, param);
    }

    public void colSetNum(int num, String col, double num1, int param) {
        String ss = String.format("%32." + param + "f", num1);//
        colSet(num, col, ss);
    }

    public void colSet(int num, String col, double num1) {
        setNumber(col, num1, num);
    }

    public void colSet(int num, String col, int num1) {
        setNumber(col, num1, num);
    }

    public void colSet(String col, String strName) {
        colSet(0, col, strName);
    }

    public void colSet(String col, double num1) {
        setNumber(col, num1, 0);
    }

    public void colSet(String col, int num1) {
        setNumber(col, num1, 0);
    }

    public void colSet(int num, String fieldName, String setData) {
        if (setData == null) {
            setData = "";
        }
        fieldName = fieldName.trim().toUpperCase() + "-" + num;
        outputHash.put(fieldName, setData);
    }

    public boolean colEmpty(String col) {
        return empty(colStr(0, col));
    }

    public boolean colEmpty(int num, String col) {
        return empty(colStr(num, col));
    }

    public boolean colEq(String col, String s1) {
        return colStr(0, col).equals(s1);
    }

    public boolean colEq(int num, String col, String strName) {
        return colStr(num, col).equals(strName);
    }

    public String colNvl(int num, String col, String strName) {
        String colName = colStr(num, col);
        if (empty(colName))
            return strName;
        return colName;
    }

    public String colYn(int num, String col) {
        return colNvl(num, col, "N");
    }

    public String colYn(String col) {
        return colNvl(0, col, "N");
    }

    public String colNvl(String col, String strName) {
        return colNvl(0, col, strName);
    }

    public String colStr(String col) {
        return colStr(0, col);
    }

    public String colStr(int num, String fieldName) {
        String colName = "";
        // try {
        fieldName = fieldName.trim().toUpperCase() + "-" + num;
        // colName_find(fieldName);
        colName = (String) outputHash.get(fieldName);
        if (colName == null) {
            // showLog("W","<<<?????>>>wp.col_name not find: "+fieldName);
            return "";
        }
        // } catch (Exception ex) {
        // expMethod = "col_ss";
        // expHandle(ex);
        // }

        return colName.trim();
    }

    public double colNum(String col) {
        return colNum(0, col);
    }

    public double colNum(int num, String fieldName) {
        String colName = colStr(num, fieldName);
        try {
            return Double.parseDouble(colName);
        } catch (Exception ex) {
            return 0;
        }
    }

    public int colInt(String col) {
        return (int) colNum(0, col);
    }

    public int colInt(int num, String col) {
        return (int) colNum(num, col);
    }

    public double colNum(String fieldName, int num) {
        return colNum(num, fieldName);
    }

    public boolean itemEq(int num, String col, String strName) {
        return itemStr(num, col).equals(strName);
    }

    public boolean itemEq(String col, String strName) {
        return itemStr(col).equals(strName);
    }

    public boolean itemNe(String col, String strName) {
        return !itemStr(col).equals(strName);
    }

    public boolean iempty(String col) {
        return empty(itemStr(col));
    }

    public boolean itemEmpty(String col) {
        return empty(itemStr(col));
    }

    public boolean itemEmpty(int num, String col) {
        return empty(itemStr(num, col));
    }

    public String itemNvl(String col, String strName) {
        if (empty(itemStr(col))) {
            return strName;
        }
        return itemStr(col);
    }

    public String itemYn(String col) {
        return itemNvl(col, "N");
    }

    public int itemLen(String col) {
        return itemStr(col).length();
    }

    public int itemBuffLen(String col) {
        String[] aa = (String[]) inputHash.get(col.trim().toUpperCase());
        if (aa == null) {
            // this.ddd("item not find: "+fieldName);
            // return nullParameter;
            return 0;
        }
        return aa.length;
    }

    public int itemBuffLen(String[] arr) {
        if (arr == null)
            return 0;
        if (arr.length == 1 && empty(arr[0])) {
            return 0;
        }
        return arr.length;
    }

    public String[] itemBuff(String fieldName) {
        String[] inputFieldData = {""};
        // try {
        // itemName_find(fieldName);
        inputFieldData = (String[]) inputHash.get(fieldName.trim().toUpperCase());
        if (inputFieldData == null) {
            // showLog("W","<<<?????>>>wp.item_name not find: "+fieldName);
            return NULL_PARAMETER;
            // return null;
        }
        for (int i = 0; i < inputFieldData.length; i++) {
            if (inputFieldData[i] == null) {
                inputFieldData[i] = "";
            }
            if (inputFieldData[i].equals("###")) {
                inputFieldData[i] = "";
                selectAll = true;
            } else {
                inputFieldData[i] = inputFieldData[i].trim();
            }
        }
        // } catch (Exception ex) {
        // expMethod = "item_buff";
        // expHandle(ex);
        // }

        return inputFieldData;
    }

    public String itemStr2(String col1) {
        // return col_ss(0,col1);
        // return item_ss(0,col1);
        String[] aa = itemBuff(col1);
        if (aa == null)
            return "";

        if (empty(aa[0]))
            return "";
        return aa[0].trim();
    }

    public String itemStr(String col1) {
        String[] arr = itemBuff(col1);
        if (arr == null)
            return "";

        if (empty(arr[0]))
            return "";
        return arr[0].trim();
    }

    public String itemStr(int num, String col) {
        String[] arr = itemBuff(col);
        if (arr == null || num >= arr.length)
            return "";

        if (empty(arr[num]))
            return "";
        return arr[num];
    }

    public String itemNvl(int num, String col, String strName) {
        String colName = "";
        // try {
        colName = getParameter(num, col);
        if (empty(colName))
            return strName;
        // } catch (Exception ex) {
        // showLogMessage("E", "item_ss:col=" + col, ex.getMessage());
        // }
        return colName;
    }

    public double num(String col1) {
        return itemNum(0, col1);
    }

    public double itemNum(String col1) {
        // double num = 0;
        return itemNum(0, col1);
    }

    public double itemNum(int rr, String col) {
        String ss = itemStr(rr, col);
        try {
            return Double.parseDouble(ss);
        } catch (Exception ex) {
            return 0;
        }
    }

    public void itemSet2(String col, String val) {
        this.setParameter(col, val);
    }

    public void itemSet(String fieldName, String setData) {
        this.setParameter(fieldName, setData);
    }

    public boolean isNumber(String strName) throws Exception {

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

    public String pgmId() {
        return menuSeq;
    }

    public String modPgm() {
        if (itemEmpty("mod_pgm") == false)
            return itemStr("mod_pgm");

        return menuSeq;
    }

    public String modSeqno() {
        return itemNvl("mod_seqno", "0");
    }

    public boolean empty(String strName) {
        if (strName == null) {
            return true;
        }
        return strName.trim().length() == 0;
    }

    public void showLog(String actCode, String mesg) {

        if (logger == null) {
            System.out.print("BaseData.showlog: " + mesg);
            return;
        }
        String str = CommString.validateLogData("> " + mesg);
        if (actCode.equalsIgnoreCase("D") && TarokoParm.getInstance().getDebugMode().equalsIgnoreCase("Y")) {
            logger.debug(str);
        } else if (actCode.equalsIgnoreCase("I")) {
            logger.info(str);
        } else if (actCode.equalsIgnoreCase("W")) {
            logger.warn(str);
        } else if (actCode.equalsIgnoreCase("E")) {
            logger.error(str);
        } else if (actCode.equalsIgnoreCase("U")) {
            logger.debug(str);
        } else if (actCode.equalsIgnoreCase("P")) {
        	if (logger instanceof MaskLogger) 
        		((MaskLogger)logger).privacy(str);
        	else 
        		logger.info("[PDPA]" +str);
        }

        return;
    }

    public void initialLogger() {
    	BaseData.logger = (Logger) LogManager.getLogger(BaseData.NORMAL_LOGGER_NAME); 
    	BaseData.loggerPDPA = (Logger) LogManager.getLogger(BaseData.PDPA_LOGGER_NAME);


    	String logEncrypted = TarokoParm.getInstance().getLogEncrypted();
    	if (logEncrypted == null || !logEncrypted.trim().equalsIgnoreCase("N")) {
    		BaseData.logger = new MaskLogger(BaseData.logger);
    		BaseData.loggerPDPA = new MaskLogger(BaseData.loggerPDPA);
    	}
    }
    
    public static Logger getNormalLogger() {
    	return logger;
    }
    
    public static Logger getPDPALogger() {
    	return loggerPDPA;
    }

}
