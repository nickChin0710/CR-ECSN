/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/02/17   V1.00.06  Jack Liao  initial                                   *
* 106/02/17   V1.00.08  Allen HO   Modify setRowId method                    *
* Last Update V1.00.10 2017/03/20  by jack Liao                              *
* 106/05/04   V1.00.12  Allen HO   Modify executeSql method                  *
* 106/05/05   V1.00.14  Jack Liao  Modify getTableIndex method               *
* 106/05/18   V1.00.15  Jack Liao  Modify getTableIndex method               *
*                                  sqlRc and notExit control                 *
* 106/05/19   V1.00.15  Jack Liao  Modify getTableIndex ,accessHash          *
* 106/06/02   V1.00.16  Allen Ho   openInputTezt add Ms950 fileTYpe          *
* 106/06/21   V1.00.17  Jack Liao  fix getTableIndex  bug , add noTrim Flag  *
* 106/06/23   V1.00.18  Allen Ho   Nodify rollback not to clear hash value   *
* 106/06/26   V1.00.19  Allen Ho   rename password word to hidewd            *
* 106/07/12   V1.00.21  Allen Ho   codereview check,arx                      *
* 106/07/12   V1.00.22  Jack Liao  insertTable support extendField func      *
* 106/07/19   V1.00.28  Allen Ho   codereview check,arx ^ openOutputText     *
* 106/08/07   V1.00.29  jack Liao  insertTable SqlCmd bug fixed              *
* 106/08/07   V1.00.30  jack Liao  update not found bug fixed                *
* 106/08/08   V1.00.31  jack Liao  rollback update                           *
* 106/08/10   V1.00.32  jack Liao  update log4j console mode                 *
* 106/10/16   V1.00.33  jack Liao  add closeBinaryInput,  closeBinaryOutput  *
* 106/11/03   V1.00.34  JH         合併JH.source                             *
* 106/12/11   V1.00.35  Jack,Liao  add setUpdateLoad,updateLoadTable         *
* 106/12/12   V1.00.36  Jack,Liao  update updateLoadTable parameter          *
* 106/12/25   V1.00.37  Jack,Liao  add table display                         *
* 107/01/02   V1.00.38  Jack,Liao  add displayCheck                          *
* 107/01/02   V1.00.39  JH        add setValue2()                            *
* 107/01/23   V1.00.40  JH        openOutputText()                           *
* 107/02/07   V1.00.41  JH        setTid()                                   *
* 107/03/20   V1.00.42  Lai        add 預先準備sql command                   *
* 107/05/08   V1.00.43  Jack Liao  add Exception Close DB connect            *
* 107/05/08   V1.00.44  JH         ++get_sqlParm()                           *
* 107/05/09   V1.00.44  JH         exitProgram()                             *
* 107/05/10   V1.00.45  Jack Liao  modify getLoadData  extendField           *
* 107/05/10   V1.00.46  Jack Liao  modify setLoadData  ,getLoadData          *
* 107/05/16   V1.00.47  Jack Liao  modify insert value too large             *
* 107/05/16   V1.00.48  Jack Liao  add getLoadSort method                    *
* 107/05/18   V1.00.49  Jack Liao  modify closeConnect clear HashMap         *
* 107/05/19   V1.00.50  Jack Liao  modify loadCnt array                      *
* 107/05/28   V1.00.51  Jack Liao  modify Hashmap array size to 20           *
* 107/06/08   V1.00.52  Jack Liao  modify System.exit() from Exception       *
* 107/06/12   V1.00.53  JH         ++clearParm()                             *
* 107/07/03   V1.00.54  JH-Jack    readBinFile()                             *
* 107/07/13   V1.00.55  Liao,Jack  modify loadTable Bug                      *
* 107/07/17   V1.00.56  Liao,Jack  modify updateLoadTable Bug                *
* 107/07/24   V1.00.57  Liao,Jack  modify getSystemParm add schema           *
* 107/08/21   V1.00.58  Liao,Jack  modify excuteSqlFile                      *
* 107/08/29   V1.00.59  Allen      Add  excuteMulSqlFile                     *
* 107/09/06   V1.00.59  Liao,Jack  Add  convMonth                            *
* 107/09/07   V1.00.59  Liao,Jack  Add  monthsBetween                        *
* 107/09/07   V1.00.60  Liao,Jack  Add  setValueDouble                       *
* 107/09/14   V1.00.61  Liao,Jack  Add  add update control value too large   *
* 107/09/28   V1.00.62  Liao,Jack  modify loadTable add showMemory           *
* 107/10/03   V1.00.63  Liao,Jack  modify openOutputText                     *
* 107/10/09   V1.00.64  Liao,Jack  add getWriteCount                         *
* 107/10/18   V1.00.64  Liao,Jack  add debug message                         *
* 107/10/24   V1.00.65  Liao,Jack  modify readBinFile                        *
* 107/10/25   V1.00.65  JH         ++getValue2()                             *
* 107/10/31   V1.00.66  Liao,Jack  modify loadTable                          *
* 107/10/31   V1.00.67  Liao,Jack  add insertTable(int i)                    *
* 107/11/07   V1.00.68  Liao,Jack  update getTableIndex                      *
* 107/11/09   V1.00.69  Liao,Jack  insertTable debug                         *
* 107/11/12   V1.00.70  Liao,Jack  loadTable modify                          *
* 107/11/20   V1.00.71  Liao,Jack  modify selectTable -302 value too large   *
* 107/11/20   V1.00.72  Liao,Jack  modify getValueDouble                     *
* 107/11/22   V1.00.73  Liao,Jack  modify listHash location                  *
* 107/11/23   V1.00.73  Liao,Jack  add byteToHexString                       *
* 107/11/27   V1.00.74  Liao,Jack  add displayConnect Flag                   *
* 107/12/05   V1.00.74  Liao,Jack  update getLoadData add extendField=""     *
* 107/12/13   V1.00.74  Liao,Jack  modify updateTable and appc log           *
* 107/12/27   V1.00.74  Liao,Jack  modify flow control                       *
* 108/01/03   V1.00.75  Liao,Jack  modify javaProgram                        *
* 108/01/29   V1.00.76  Liao,Jack  disable display commit                    *
* 108/01/30   V1.00.77  Liao,Jack  add mq,appc parameter                     *
* 108/01/30   V1.00.78  Liao,Jack  update fetchTable                         *
* 108/02/21   V1.00.79  Liao,Jack  update checkTable, and console mode       *
* 108/02/22   V1.00.80  Liao,Jack  update updateTable                        *
* 108/03/27   V1.00.81  Liao,Jack  update insertTable                        *
* 108/05/16   V1.00.82  Liao,Jack  update System.gc and rollback             *
* 108/06/13   V1.00.83  Liao,Jack  add    logFlag control                    *
* 108/07/22   V1.00.83  Liao,Jack  add    insertTable displayCheck           *
* 108/08/20   V1.00.84  Liao,Jack  add    add openBinaryOutput2              *
* 108/09/05   V1.00.85  Liao,Jack  add    delete appc conf                   *
* 108/09/20   V1.00.86  Liao,Jack  add    dupRecord resetParm                *
* 108/11/08   V1.00.87  Liao,Jack  add    setBigDecimal                      *
* 109/06/15   V1.00.88  Zuwei  fix    coding scan issue                      *
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/07/22  V0.00.02    Zuwei     coding standard, rename field method                   *
* 109-08-03  V1.00.01  Zuwei       fix code scan issue                       *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  109-09-04  V1.00.01  yanghan     解决Portability Flaw: Locale Dependent Comparison问题    *
*  109-09-19  V1.00.89  JeffKung  add TCB db connection properties
*  109-09-21  V1.00.89  Zuwei  replace logger with MaskLogger
*  109-09-22  V1.00.90  JustinWu skip MaskLog if not in TCB
*  109-09-23  V1.00.91  JustinWu  dcConfig->config
*  109/09/28  v1.00.92    Zuwei    Fix code scan issue             *
*  109-10-13   V1.00.93  JustinWu use mask logger depending on config 
*  109-10-28   V1.00.94  JustinWu  check pawdEncrypted is null
*  109-11-19    V1.00.95  JustinWu  fix bugs that show parameters when SQL error code equals to -302
*  109-11-20    V1.00.96  JustinWu  fix bugs that show parameters when SQL error code equals to -302
*  110-01-07  V1.00.97   shiyuqi       修改无意义命名
*  110-03-02  V1.00.98    JustinWu  get hide password from a property file
*  110-03-05  V1.00.99    JustinWu  prevent from trimming a null value
*  110-11-16  V1.01.00    JustinWu  change getEcsAcdpPath() to be static
*  110-12-23  V1.01.01    JustinWu  log4j1 -> log4j2
*  111-01-19  V1.01.02    JustinWu  fix Missing Check against Null            *
*  111-01-20  V1.01.03    JustinWu  fix Code Correctness: Call to System.gc()
*  111/10/04  V1.01.04    JeffKung  update setValueDouble 
*                                   (modify copy from mega source code) 
*                                   **109/11/25   V1.00.A1  Liao,Jack  update setValueDouble
*  111/10/20  V1.01.05    Zuwei     sync method from mega (clearLoadBuffer, getLoadIndex, setLoadIndex)
*  112/03/07  V1.01.06    Simon     complete skipCheck function (designed by Liao,Jack)*
*  112/05/16  V1.01.07    Simon     use contains("DXC_NOSHOW_EXCEPTION") instead of equals("DXC_NOSHOW_EXCEPTION") in exphandle()*
******************************************************************************/

package com;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;

import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.net.URL;

import com.tcb.ap4.tool.*;

import Dxc.Util.MaskLogger;
import Dxc.Util.SecurityUtil;

@SuppressWarnings({"unchecked", "deprecation"})
public class AccessDAO {
  /* private variable */
  private static final String DEFAULT_LOGGER_NAME = "defaultlogger";

public boolean showUseMemory = false;

  PreparedStatement gInsertCurPostPs = null;
  private int tbLimit = 80;
  private int colLimit = 1500;
  private int blobLimit = 1000;
  private int tbIndex = 1;
  private int ftIndex = 0;
  private int qi = 0;

  public Connection[] conn = new Connection[4];
  private String columnString = "", valueString = "";
  private Object[] parmData = new Object[colLimit];
  protected String[][] columnName = new String[tbLimit][colLimit];
  private String[][] insertColumn = new String[tbLimit][colLimit];
  protected String[][] dataType = new String[tbLimit][colLimit];
  private Integer[][] dataLength = new Integer[tbLimit][colLimit];
  private Integer[][] dataScale = new Integer[tbLimit][colLimit];
  protected PreparedStatement[] pf = new PreparedStatement[tbLimit];
  private ResultSet[] rf = new ResultSet[tbLimit];
  private CallableStatement procStmt = null;

  private String[] dbType = {"", "", "", "", ""};
  private String[] dbComName = {"", "", "", "", ""};

  private String dbName = "", dbUser = "", dbOwner = "", dbhideWd = "", dbHost = "",
      programVersion = "", schema = "";
  private String expErrorMesg = "", expSystemMesg = "", expGrade = "", expApMesg = "",
      expDbUser = "", exceptionFlag = "";
  private String portNo = "", dbUrl = "", checkType = "", sysName = "", defaultSystem = "",
      garStr = "", sqlFileMode = "";
  private String pawdEncrypted = "", defaultDbTyp = "";
  private String pgmStartDate = "", pgmStartTime = "", flowDB = "";

  private Blob blobValue = null;

  private int[] loadCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private String[] loadExt =
      {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};

  private String[] dispFlag = new String[tbLimit];
  private String[] dispTable = new String[tbLimit];
  private long[] dispCount = new long[tbLimit];
  protected int[] columnCnt = new int[tbLimit];
  private int[] prevSelect = new int[tbLimit];

  private String startMillis = "", startDate = "", startTime = "";
  private String initialFlag = "Y", consoleMode = "", debugMode = "", subFlag = "", accessCode = "",
      debugSQL = "";

  private int di = 0, ci = 0, ti = 0, li = -1;
  private int parmCount = 0, saveParm = 0, dbCount = 0, recCount = 0, dispControl = 0,
      updateCnt = 0, workLoad = 0;

  private long pid = 0;
  private int[] readCnt = {0, 0, 0, 0, 0};
  private int[] writeCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      writeBin = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

  private FileInputStream fin = null;
  private DataInputStream dis = null;

  private int maxFetch = 4, maxInFile = 5, maxOutFile = 15, maxBinOut = 15;

  private String[] inFile = new String[maxInFile];
  public String[] endFile = new String[maxInFile];
  private String[] outFile = new String[maxOutFile];
  private String[] outBin = new String[maxBinOut];

  private int[] fetchTi = new int[maxFetch];
  private String[] fetchName = new String[maxFetch];
  private BufferedReader[] dr = new BufferedReader[maxInFile];
  private BufferedWriter[] dw = new BufferedWriter[maxOutFile];
  private DataOutputStream[] dos = new DataOutputStream[maxBinOut];

  private Logger logger = null;

  /* public variable */

  public int ttt = 0;
  public int loadRow = 2000, fetchRow = 5000, loadLimit = 6000000;
  public int sqlRc = 0;
  public String sysDate = "", sysTime = "", chinDate = "", dispDate = "", dispTime = "",
      millSecond = "", juliDate = "";
  public String javaProgram = "", dataBase = "", dispMesg = "", durTime = "";
  public String sqlCmd = "", selectSQL = "", insertSQL = "", updateSQL = "", whereStr = "",
      daoTable = "";
  public String notFound = "", dupRecord = "", specialSQL = "", indexHint = "", expMethod = "";
  public String businessDate = "", extendField = "", fetchExtend = "", covertBig5 = "", noTrim = "";
  public String finalExit = "Y", showNotFound = "N", displayParmData = "N", showReadWrite = "Y",
      debugInsert = "N", noticeMail = "N", displayConnect = "Y", notExit = "";
  public String mqHost = "", mqPort = "", logFlag = "";

  public int normalExit = 0, exceptExit = -1; // change default value by david at 2018.09.17

  private SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSSDDD");
  private SimpleDateFormat form2 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");

  private HashMap<String, Integer> accessHash = new HashMap<String, Integer>();

  private HashMap[] listHash = new HashMap[20];
  private HashMap[] indexHash = new HashMap[20];
  private HashMap workHash = new HashMap();
  private HashMap updateHash = new HashMap();
  private HashMap outputHash = new HashMap();
  private Map<String,String> skipHash = null;
  private String skipKeyField;
  private boolean skipCheck=false;

  public AccessDAO() {
    for (int i = 0; i < tbLimit; i++) {
      dispFlag[i] = "";
      dispTable[i] = "";
      dispCount[i] = 0;
      columnCnt[i] = 0;
      prevSelect[i] = 0;
    }

    for (int i = 0; i < maxInFile; i++) {
      inFile[i] = "";
      endFile[i] = "";
    }

    for (int i = 0; i < maxOutFile; i++) {
      outFile[i] = "";
    }

    return;
  }

  public String getBusiDate() throws Exception {
    daoTable = "ptr_businday";
    selectSQL = "business_date";
    whereStr = "";
    int cnt = selectTable();
    if (cnt == 0) {
      showLogMessage("E", "", "select_ptr_businday ERROR ");
      exitProgram(3);
    }

    businessDate = getValue("business_date");
    return businessDate;
  }

  public boolean checkRerun() throws Exception {

    return true;
  }

  public boolean checkPrevious(String checkProgram) throws Exception {


    return true;
  }

  public void setConsoleMode(String modeParm) throws Exception {
    consoleMode = modeParm;
    return;
  }

  public boolean checkWorkDate(String parmDate) throws Exception {
    daoTable = "PTR_HOLIDAY";
    selectSQL = "HOLIDAY";
    whereStr = "WHERE HOLIDAY = ? ";
    setString(1, parmDate);
    int cnt = selectTable();
    if (cnt == 0) {
      return false;
    }
    return true;
  }

  public boolean callStoreProcedure(String procName, String procParm) throws Exception {
    procStmt = conn[0].prepareCall("{ call " + procName + "(" + procParm + ") }");
    procStmt.executeUpdate();
    procStmt.close();

    return true;
  }

  public boolean checkTable(String parmTable) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      setConnectIndex();
      String checkSql = "";
      if ("DB2".equals(dbType[ci])) {
        checkSql = "select 1 from " + parmTable + " FETCH FIRST 1 ROW ONLY";
      } else if ("ORACLE".equals(dbType[ci])) {
        checkSql = "select 1 from " + parmTable + " WHERE rownum < 2";
      } else {
        checkSql = "select top 1 1 from " + parmTable;
      }
      ps = conn[ci].prepareStatement(checkSql, ResultSet.TYPE_FORWARD_ONLY,
          ResultSet.CONCUR_READ_ONLY);
      rs = ps.executeQuery();
      rs.close();
      ps.close();
      rs = null;
      ps = null;
    } catch (Exception ex) {
      return false;
    }

    finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
      } catch (Exception ex2) {
      }
    }

    return true;
  }

  public PreparedStatement initPs(String param) {
    try {
      gInsertCurPostPs = conn[ci].prepareStatement(param);
    } catch (Exception e) {
    }

    return gInsertCurPostPs;

  }

  public Connection getConnection() {
    return conn[ci];
  }

  public int insertTable(int i) throws Exception {
    qi = i;
    int retCode = insertTable();
    return retCode;
  }

  public int insertTable() throws Exception {
    try {
      String insertData = "", insertValue = "";

      getTableIndex("I");
      dupRecord = "";
      setConnectIndex();
      dispFlag[ti] = "I";
      if (sqlCmd.length() > 0) {
        int svTi = ti;
        if (!sqlFileMode.equals("Y")) // 20190722 modiy by jack
        {
          displayCheck();
        }
        ti = svTi;

        pf[ti] = conn[ci].prepareStatement(sqlCmd);
        setParmData(pf[ti]);
        int retCode = pf[ti].executeUpdate();
        /*
         * if ( retCode == 0 ) { showLogMessage("W","",daoTable+" INSERT ERROR "); }
         */
        dispCount[ti] = dispCount[ti] + retCode;
        dataBase = "";
        sqlCmd = "";
        pf[ti].close();
        pf[ti] = null;
        return retCode;
      } else if (columnCnt[ti] == 0) {
        processFullColumn();

        StringBuffer colbuf = new StringBuffer();
        StringBuffer valbuf = new StringBuffer();
        columnString = "";
        valueString = "";
        for (int i = 0; i < columnCnt[ti]; i++) {
          insertColumn[ti][i] = null;

          String ckString = (String) getValue3(extendField + columnName[ti][i]);
          if (ckString == null) {
            continue;
          }

          insertColumn[ti][i] = "Y";
          colbuf.append((columnName[ti][i] + ","));

          if ("ORACLE".equals(dbType[ci])) {
            String checkFunction = getFunction(columnName[ti][i]);
            if (checkFunction.length() > 0) {
              valbuf.append(checkFunction + ",");
            } else if ("DATE".equals(dataType[ti][i])) {
              valbuf.append("TO_DATE(?,'YYYYMMDDHH24MISS'),");
            } else {
              valbuf.append("?,");
            }
          } else if ("DB2".equals(dbType[ci])) {
            String checkFunction = getFunction(columnName[ti][i]);
            if (checkFunction.length() > 0) {
              valbuf.append(checkFunction + ",");
            } else if ("TIMESTAMP".equals(dataType[ti][i])) {
              valbuf.append("TIMESTAMP_FORMAT(?,'YYYYMMDDHH24MISS'),");
            } else if ("DATE".equals(dataType[ti][i])) {
              valbuf.append("DATE(TO_DATE(?,'YYYYMMDD')),");
            } else if ("TIME".equals(dataType[ti][i])) {
              valbuf.append("TIME(TO_DATE(?,'HH24MISS')),");
            } else {
              valbuf.append("?,");
            }
          } else {
            valbuf.append("?,");
          }
        }

        columnString = colbuf.toString();
        columnString = columnString.substring(0, columnString.length() - 1);
        valueString = valbuf.toString();
        valueString = valueString.substring(0, valueString.length() - 1);

        sqlCmd = "INSERT INTO "
            + daoTable
            + " ( "
            + columnString
            + " ) VALUES "
            + " ( "
            + valueString
            + " ) ";
        if (pf[ti] != null) {
          pf[ti].close();
          pf[ti] = null;
        }
        
        pf[ti] = conn[ci].prepareStatement(sqlCmd);
        debugSQL = sqlCmd;
        sqlCmd = "";
      }

      extendField = extendField.toUpperCase();

      int int1 = 0;
      for (int i = 0; i < columnCnt[ti]; i++) {
        if (insertColumn[ti][i] == null) {
          continue;
        }
        if (debugInsert.equals("Y")) {
          showLogMessage("D", "insertTable",
              columnName[ti][i]
                  + " : "
                  + getValue(extendField + columnName[ti][i], qi)
                  + " : "
                  + getValue(extendField + columnName[ti][i], qi).getBytes().length);
        }

        if ("BLOB".equals(dataType[ti][i])) {
          pf[ti].setBytes(int1 + 1, getBlob(extendField + columnName[ti][i]));
        } else
        // if ( "DECIMAL".equals( dataType[ti][i]) || "DOUBLE".equals(dataType[ti][i]) )
        // { pf[ti].setDouble(k+1,getValueDouble(extendField+columnName[ti][i],qi)); }
        // else
        if ("DECIMAL".equals(dataType[ti][i]) || "DOUBLE".equals(dataType[ti][i])) {
          pf[ti].setBigDecimal(int1 + 1, getValueBigDecimal(extendField + columnName[ti][i], qi));
        } else if ("INTEGER".equals(dataType[ti][i]) || "LONG".equals(dataType[ti][i])) {
          pf[ti].setLong(int1 + 1, getValueLong(extendField + columnName[ti][i], qi));
        } else if ("DB2".equals(dbType[ci]) && "TIMESTAMP".equals(dataType[ti][i])
            && getValue(extendField + columnName[ti][i], qi).length() != 14) {
          pf[ti].setNull(int1 + 1, Types.TIMESTAMP);
        } else if ("DB2".equals(dbType[ci]) && "DATE".equals(dataType[ti][i])
            && getValue(extendField + columnName[ti][i], qi).length() != 8) {
          pf[ti].setNull(int1 + 1, Types.DATE);
        } else if ("DB2".equals(dbType[ci]) && "TIME".equals(dataType[ti][i])
            && getValue(extendField + columnName[ti][i], qi).length() != 6) {
          pf[ti].setNull(int1 + 1, Types.TIME);
        } else {
          pf[ti].setString(int1 + 1, getValue(extendField + columnName[ti][i], qi));
        }
        int1++;
      }

      qi = 0;
      int retCode = pf[ti].executeUpdate();
      dataBase = "";
      extendField = "";
      daoTable = "";
      if (retCode == 0) {
        showLogMessage("W", "", daoTable + " INSERT ERROR ");
        return 0;
      }
      // { dispMesg = "INSERT ERROR"; return 0; }
      // dispFlag[ti] = "I";
      dispCount[ti]++;
      return retCode;
    }

    catch (SQLException ex2) {
      sqlRc = ex2.getErrorCode();
      if (ex2.getErrorCode() == 1 || ex2.getErrorCode() == -803) {
        dupRecord = "Y";
        resetParm();
        // showLogMessage("E","insertTable","errCode="+sqlRc+","+ex2.getMessage());
        // showLogMessage("W","insertTable","insert "+daoTable.toUpperCase()+" DMPDuplicate ");
        return 0;
      } else if (ex2.getErrorCode() == -302) {
        for (int i = 0; i < columnCnt[ti]; i++) {
          String dispData = getValue(extendField + columnName[ti][i], qi);
          showLogMessage("E", "",
              columnName[ti][i] + " --- [" + dispData + "] LEN = " + dispData.length());
        }
        throw new Exception(ex2);
      } else {
        showLogMessage("E", "insertTable", columnCnt[ti] + " : " + debugSQL);
        throw new Exception(ex2);
      }
    }

    catch (Exception ex) {
      throw new Exception(ex);
    }

  } // End of insertTable

  public void setFunction(String fieldName, String setData) throws Exception {
    outputHash.put("##-" + fieldName.toUpperCase(), setData);
    return;
  }

  public String getFunction(String fieldName) throws Exception {
    String retnStr = (String) outputHash.get("##-" + fieldName.toUpperCase());
    if (retnStr == null) {
      return "";
    }
    return retnStr;
  }

  public int openCursor() throws Exception {
    int ft = -1;

    for (int i = 0; i < maxFetch; i++) {
      if (fetchName[i] != null) {
        continue;
      }
      fetchName[i] = daoTable;
      ft = i;
      break;
    }
    fetchExtend = fetchExtend.toUpperCase();

    fetchName[ft] = daoTable;
    ftIndex = ft;
    getTableIndex("F");
    fetchTi[ft] = ti;
    setConnectIndex();
    if (sqlCmd.length() == 0) {
      if (selectSQL.length() == 0) {
        processFullColumn();
        processSelectColumn();
      } else {
        columnString = selectSQL;
      }
      sqlCmd = "SELECT " + columnString + " FROM " + daoTable + " " + whereStr;
    }

    pf[ti] =
        conn[ci].prepareStatement(sqlCmd, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    setParmData(pf[ti]);
    rf[ti] = pf[ti].executeQuery();
    rf[ti].setFetchSize(fetchRow);
    if (columnCnt[ti] == 0) {
      processColumnName(rf[ti]);
    }

    resetParm();
    recCount = 0;

    return ft;
  } // End of openCursor

  public boolean fetchTable() throws Exception {
    return fetchTable(0);
  }

  public boolean fetchTable(int i) throws Exception {
    ftIndex = i;
    ti = fetchTi[i];
    // getTableIndex("F");
    // setConnectIndex();
    accessCode = "F";
    extendField = fetchExtend;

    if (retrieveTableData(rf[ti]) == 0) {
      accessCode = "";
      extendField = "";
      return false;
    }
    accessCode = "";
    extendField = "";
    skipCheck = false;

    return true;
  } // End of fetchTable

  public int loadTable() throws Exception {
    dateTime();

    String hashKey = specialSQL + (daoTable.hashCode() + whereStr.hashCode());
    Integer checkLoad = accessHash.get(hashKey);
    if (checkLoad == null) {
      li++;
      accessHash.put(hashKey, li);
    } else {
      li = checkLoad.intValue();
    }

    if (listHash[li] == null) {
      listHash[li] = new HashMap();
      indexHash[li] = new HashMap();
    } else {
      listHash[li].clear();
      indexHash[li].clear();
      listHash[li] = null;
      indexHash[li] = null;
      listHash[li] = new HashMap();
      indexHash[li] = new HashMap();
    }

    String tmpTable = daoTable;
    loadCnt[li] = 0;
    accessCode = "L";
    extendField = extendField.toUpperCase();
    loadExt[li] = extendField;
    selectTable();
    dateTime();
    daoTable = tmpTable;
    showMemory(loadCnt[li]);
    daoTable = "";
    extendField = "";
    return loadCnt[li];
  }

  public int selectTable() throws Exception {
    ResultSet rs = null;
    int si = 0;

    notFound = "";
    extendField = extendField.toUpperCase();
    if ("DUAL".equals(daoTable.trim().toUpperCase(Locale.TAIWAN))) {
      ti = 0;
      columnCnt[ti] = 0;
    } else if (accessCode.equals("L")) {
      getTableIndex("L");
    } else {
      accessCode = "S";
      getTableIndex("S");
    }

    setConnectIndex();
    if (columnCnt[ti] == 0 || ti == 0) {
      if (sqlCmd.length() == 0) {
        if (selectSQL.length() == 0) {
          processFullColumn();
          processSelectColumn();
        } else {
          columnString = selectSQL;
        }
        sqlCmd = "SELECT " + columnString + " FROM " + daoTable + " " + whereStr;
      } else {
        if (dispTable[ti].length() == 0) {
          String[] tmpSql = sqlCmd.toUpperCase().split(" FROM ");
          dispTable[ti] = tmpSql[1].split("WHERE")[0].trim();
          if (dispTable[ti].length() > 30) {
            dispTable[ti] = dispTable[ti].substring(0, 30);
          }
        }
      }

      if (pf[ti] != null) {
        pf[ti].close();
        pf[ti] = null;
      }
      pf[ti] = conn[ci].prepareStatement(sqlCmd, ResultSet.TYPE_FORWARD_ONLY,
          ResultSet.CONCUR_READ_ONLY);
      sqlCmd = "";
    }

    setParmData(pf[ti]);
    try {
      rs = pf[ti].executeQuery();
    } catch (SQLException ex2) {
      if (ex2.getErrorCode() == -302) {
        notFound = "Y";
        resetParm();
        return 0;
      } else {
        throw new Exception(ex2);
      }
    } catch (Exception ex) {
      throw new Exception(ex);
    }

    if (accessCode.equals("L")) {
      rs.setFetchSize(loadRow);
    } else {
      rs.setFetchSize(1);
    }

    if (columnCnt[ti] == 0 || ti == 0) {
      processColumnName(rs);
    }

    si = retrieveTableData(rs);
    if (si == 0 && showNotFound.equals("Y")) {
      dispMesg = "NOT FOUND ";
      displayAccessKey();
    }

    rs.close();
    // if ( accessCode.equals("L") || "DUAL".equals(daoTable.trim().toUpperCase()) )
    if ("DUAL".equals(daoTable.trim().toUpperCase(Locale.TAIWAN))) {
      pf[ti].close();
      pf[ti] = null;
    }

    if (si < prevSelect[ti]) {
      resetColumnValue(si, prevSelect[ti]);
    }

    prevSelect[ti] = si;
    resetParm();
    skipCheck = false;

    return si;
  } // End of selectTable

  private void displayAccessKey() throws Exception {
    byte[] carriage = {0x0D, 0x0A, 0x00};
    String newLine = new String(carriage, 0, 2);
    String keyData = newLine;
    for (int i = 1; i <= saveParm; i++) {
      keyData = keyData + "( " + i + " ) " + (String) parmData[i] + newLine;
    }
    showLogMessage("W", "", dispMesg + " " + keyData);
  }

  private int retrieveTableData(ResultSet rs) throws Exception {
    notFound = "";
    int cntl = 0, tCnt = 0;

    while (rs.next()) {
      if ( skipCheck ) {
           String  checkCode = (String)skipHash.get(rs.getString(skipKeyField));
           if ( checkCode == null )
              { continue; }
      }

      for (int k = 0; k < columnCnt[ti]; k++) {
        if ("BLOB".equals(dataType[ti][k]) && "ORACLE".equals(dbType[ci])) {
          blobValue = rs.getBlob(k + 1);
          if (blobValue == null) {
            setBlob(extendField + columnName[ti][k], " ".getBytes());
          } else {
            byte[] blobData = new byte[blobLimit];
            int readLength = 0, blobLen = 0;
            try (InputStream blobIn = blobValue.getBinaryStream();) {
              readLength = blobIn.read(blobData);
            }
            byte[] respData = new byte[readLength];
            for (int i = 0; i < readLength; i++) {
              respData[i] = blobData[i];
            }
            setBlob(extendField + columnName[ti][k], respData);
          }
        } else if (accessCode.equals("L")) {
          setList(extendField + columnName[ti][k], rs.getString(k + 1), cntl);
        } else if ("DECIMAL".equals(dataType[ti][k]))
        // { setValue(extendField+columnName[ti][k],""+rs.getDouble(k+1),cntl); }
        {
          setValue(extendField + columnName[ti][k], "" + rs.getBigDecimal(k + 1), cntl);
        } else if ("DATETIME".equals(dataType[ti][k]) && "MYSQL".equals(dbType[ci])) {
          setValue(extendField + columnName[ti][k], rs.getString(k + 1).substring(0, 19), cntl);
        } else {
          setValue(extendField + columnName[ti][k], rs.getString(k + 1), cntl);
        }

      }

      cntl++;
      dispCount[ti]++;
      tCnt++;

      if (accessCode.equals("L")) {
        if (tCnt == 100000) {
          showMemory(cntl);
          tCnt = 0;
        }
      }

      if (accessCode.equals("F")) {
        break;
      } else if ((accessCode.equals("S") || accessCode.equals("L")) && cntl > loadLimit) {
        break;
      }
    }

    if (accessCode.equals("S")) {
      dispFlag[ti] = "S";
    } else if (accessCode.equals("L")) {
      dispFlag[ti] = "L";
    } else if (accessCode.equals("F")) {
      dispFlag[ti] = "F";
    }

    if (accessCode.equals("L")) {
      loadCnt[li] = cntl;
    }


    if (cntl == 0) {
      notFound = "Y";
      return cntl;
    }

    return cntl;
  } // End of retrieveTableData

  public int updateTable() throws Exception {
    try {
      if (sqlCmd.length() > 0) {
        int n = executeSqlCommand(sqlCmd);
        return n;
      }

      getTableIndex("U");
      setConnectIndex();
      notFound = "";

      if (pf[ti] == null || ti == 0 || ti == (tbLimit - 1)) {
        if (pf[ti] != null) {
          pf[ti].close();
          pf[ti] = null;
        }
        String sqlCmd = "UPDATE " + daoTable + " SET " + updateSQL + " " + whereStr;
        pf[ti] = conn[ci].prepareStatement(sqlCmd);

        sqlCmd = "";
      }

      setParmData(pf[ti]);
      updateCnt = pf[ti].executeUpdate();
      daoTable = "";
    }

    catch (SQLException ex2) {
      if (ex2.getErrorCode() == -302) {
        for (int i = 1; i <= saveParm; i++) {
        	if (parmData[i] instanceof String) {
        		showLogMessage("E", "",
			              " --- [" + (String) parmData[i] + "] LEN = " + ((String) parmData[i]).length() + " (String)");
			}else if (parmData[i] instanceof Integer) {
				showLogMessage("E", "",
			              " --- [" + (Integer) parmData[i] + "] LEN = " + Integer.toString((Integer) parmData[i]).length() +  " (Integer) ");
			}else if (parmData[i] instanceof BigDecimal){
				BigDecimal bd = (BigDecimal) parmData[i];
				showLogMessage("E", "",
			              " --- [" + bd + "] LEN = " + bd.setScale(0, BigDecimal.ROUND_DOWN).toString().length()  + "," + bd.scale() + " (BigDecimal)");
			}else if (parmData[i] instanceof Long){
				showLogMessage("E", "",
			              " --- [" + (Long) parmData[i] + "] LEN = " + Long.toString((Long) parmData[i]).length() + " (Long)");
			}else if (parmData[i] instanceof byte[]){
				showLogMessage("E", "",
			              " --- [" + byteToHexString((byte[]) parmData[i]) + "] (rowid)");
			}else {
				showLogMessage("E", "",
			              " --- [] (" + parmData[i].getClass() +") 此型別參數無法print出");
			}
       }
        throw new Exception(ex2);
      } else {
        throw new Exception(ex2);
      }
    } catch (Exception ex) {
      throw new Exception(ex);
    }

    dispFlag[ti] = "U";
    dispCount[ti] = dispCount[ti] + updateCnt;
    dataBase = "";
    updateSQL = "";
    whereStr = "";
    extendField = "";

    if (updateCnt == 0 && displayParmData.equals("Y")) {
      displayAccessKey();
    }

    if (updateCnt == 0) {
      notFound = "Y";
      dispMesg = "UPDATE NOT FOUND";
      return 0;
    }

    return updateCnt;
  } // End of updateTable

  public int deleteTable() throws Exception {
    PreparedStatement ps = null;
    int retCode = 0;

    getTableIndex("D");
    setConnectIndex();
    notFound = "";

    sqlCmd = "DELETE from " + daoTable;
    sqlCmd = sqlCmd + " " + whereStr;
    ps = conn[ci].prepareStatement(sqlCmd);
    setParmData(ps);
    retCode = ps.executeUpdate();
    ps.close();
    whereStr = "";
    sqlCmd = "";

    if (retCode == 0) {
      notFound = "Y";
      dispMesg = "DELETE ERROR";
    }

    dispFlag[ti] = "D";
    dispCount[ti] = dispCount[ti] + retCode;
    dataBase = "";

    return retCode;
  } // End of deleteTable

  public boolean executeMulSqlFile(String sqlFile) throws Exception {
    byte[] carriage = {0x0D, 0x0A, 0x00};
    String newLine = new String(carriage, 0, 2);
    StringBuffer strbuf = new StringBuffer();
    int fi = openInputText(sqlFile);
    if (fi == -1) {
      return false;
    }

    sqlFileMode = "Y";
    String checkStr = "";
    while (true) {
      String readData = readTextFile(fi);
      if (endFile[fi].equals("Y")) {
        break;
      }
      if (readData != null) {
    	  strbuf.append(readData).append(newLine);
          int intpos1 = 0;
          intpos1 = readData.indexOf("--");
          if (intpos1 != -1)
            readData = readData.substring(0, intpos1);

          int endMark = 0;
          int fstC = readData.indexOf(";");
          if (fstC != -1) {
            checkStr = readData.replace("''", "").trim();
            int fstD = checkStr.indexOf("'");
            fstC = checkStr.indexOf(";");
            int lstD = checkStr.lastIndexOf("'");
            int lstC = checkStr.lastIndexOf(";");

            if (fstC != -1) {
              if (fstC != lstC)
                endMark = 1;
              else if (fstD == -1)
                endMark = 1;
              else if ((fstC < fstD) || (fstC > lstD))
                endMark = 1;
            }
          }

          if (endMark == 1) {
            if (strbuf.toString().replace(";", "").trim().length() > 5) {
              notExit = "Y";
              executeSqlCommand(strbuf.toString().replace(";", ""));
              notExit = "";
              strbuf = new StringBuffer();
            }
          }
	  }
    }
    String trialCommand = strbuf.toString().replace(";", "").trim();
    if (trialCommand.length() > 5) {
      executeSqlCommand(trialCommand);
    }
    showReadWrite = "N";
    closeInputText(fi);
    showReadWrite = "Y";
    sqlFileMode = "";
    return true;
  } // End of executeSqlFile

  public boolean executeSqlFile(String sqlFile) throws Exception {
    byte[] carriage = {0x0D, 0x0A, 0x00};
    String newLine = new String(carriage, 0, 2);
    StringBuffer strbuf = new StringBuffer();
    int fi = openInputText(sqlFile);
    if (fi == -1) {
      return false;
    }

    sqlFileMode = "Y";
    String checkStr = "";
    while (true) {
      String readData = readTextFile(fi); 
	  if (readData != null) {
		if (readData.equals("@")) {
			continue;
		}
		if (endFile[fi].equals("Y")) {
			break;
		}
		strbuf.append(readData).append(newLine);
	  }
      /*
       * int intpos1=0; intpos1= readData.indexOf("--"); if (intpos1!=-1) readData =
       * readData.substring(0,intpos1);
       */
    }
    String trialCommand = strbuf.toString();
    if (trialCommand.length() > 5) {
      executeSqlCommand(trialCommand);
    }
    showReadWrite = "N";
    closeInputText(fi);
    showReadWrite = "Y";
    sqlFileMode = "";
    return true;
  } // End of executeSqlFile

  public int executeSqlCommand(String newsqlStr) throws Exception {
    String sqlStatement = newsqlStr;
    PreparedStatement ps = null;
    int retCode = 0;

    setConnectIndex();

    sqlCmd = sqlStatement;
    if (!sqlFileMode.equals("Y")) {
      displayCheck();
    }
    ps = conn[ci].prepareStatement(sqlCmd);
    setParmData(ps);
    retCode = ps.executeUpdate();
    ps.close();
    if (retCode == 0) {
      notFound = "Y";
      dispMesg = "SQL ERROR";
    }

    dispCount[ti] += retCode;
    dataBase = "";
    sqlCmd = "";


    return retCode;
  } // End of executeSqlCommand

  public void displayCheck() throws Exception {
    daoTable = "";
    if (sqlCmd.toUpperCase().indexOf("INSERT ") != -1) {
      getTableIndex("I");
      if (dispTable[ti].length() == 0) {
        String[] tmpSql = sqlCmd.toUpperCase().split(" INTO ");
        String[] cvtSql = tmpSql[1].split(" ");
        dispTable[ti] = cvtSql[0].trim();
        dispFlag[ti] = "I";
      }
    } else if (sqlCmd.toUpperCase().indexOf("UPDATE ") != -1) {
      getTableIndex("U");
      if (dispTable[ti].length() == 0) {
        String[] tmpSql = sqlCmd.toUpperCase().split("UPDATE ");
        String[] cvtSql = tmpSql[1].split(" ");
        dispTable[ti] = cvtSql[0].trim();
        dispFlag[ti] = "U";
      }
    } else if (sqlCmd.toUpperCase().indexOf("DELETE ") != -1) {
      getTableIndex("D");
      if (dispTable[ti].length() == 0) {
        String[] tmpSql = sqlCmd.toUpperCase().split("DELETE ");
        String[] cvtSql = tmpSql[1].split(" ");
        dispTable[ti] = cvtSql[0].trim();
        dispFlag[ti] = "D";
      }
    }
    return;
  }

  public boolean processFullColumn() throws Exception {
    String metaCommand = "";

    if ("DB2".equals(dbType[ci])) {
      metaCommand = "SELECT * FROM " + daoTable + " FETCH FIRST 1 ROW ONLY ";
    } else if ("ORACLE".equals(dbType[ci])) {
      metaCommand = "SELECT * FROM " + daoTable + " WHERE rownum < 2 ";
    }

    Statement st = conn[ci].createStatement();
    ResultSet rs = st.executeQuery(metaCommand);
    processColumnName(rs);

    rs.close();
    st.close();
    rs = null;
    st = null;

    return true;
  } // End of processFullColumn

  protected void processColumnName(ResultSet rs) throws Exception {
    ResultSetMetaData md = rs.getMetaData();
    columnCnt[ti] = md.getColumnCount();
    for (int i = 0; i < columnCnt[ti]; i++) {
      String columnLabel = md.getColumnLabel(i + 1);
      if (columnLabel != null && columnLabel.length() > 0) {
        columnName[ti][i] = columnLabel;
      } else {
        columnName[ti][i] = md.getColumnName(i + 1);
      }
      // parseColumnName(i);
      // System.out.println("AA : "+columnName[ti][i]);
      dataType[ti][i] = md.getColumnTypeName(i + 1).toUpperCase();
      dataLength[ti][i] = md.getPrecision(i + 1);
    }

    if (columnCnt[ti] == 0) {
      showLogMessage("W", "processColumnName",
          "TABLE NAME ERROR : " + daoTable + " " + dbUser + " " + ci);
    }

    return;
  } // End of processColumnName

  public void parseColumnName(int i) throws Exception {
    String fieldString = columnName[ti][i];
    int pnt = fieldString.lastIndexOf("(");
    if (pnt == -1) {
      return;
    }
    fieldString = fieldString.substring(pnt + 1);
    pnt = fieldString.indexOf(",");
    int pnt2 = fieldString.indexOf(")");
    if (pnt2 < pnt && pnt2 != -1) {
      pnt = pnt2;
    }
    fieldString = fieldString.substring(0, pnt);
    pnt = fieldString.indexOf(".");
    columnName[ti][i] = fieldString.substring(pnt + 1).trim();
  } // End of parseColumnName

  private boolean processSelectColumn() throws Exception {

    StringBuffer strbuf = new StringBuffer();
    columnString = "";

    for (int i = 0; i < columnCnt[ti]; i++) {
      if ("ORACLE".equals(dbType[ci]) && "DATE".equals(dataType[ti][i])) {
        strbuf.append("NVL(TO_CHAR(" + columnName[ti][i] + ",'YYYYMMDDHH24MISS'),' '),");
      } else if ("DB2".equals(dbType[ci]) && "TIMESTAMP".equals(dataType[ti][i])) {
        strbuf.append("VARCHAR_FORMAT(" + columnName[ti][i] + ",'YYYYMMDDHH24MISS'),");
      } else if ("ORACLE".equals(dbType[ci]) && "DATE".equals(dataType[ti][i])) {
        strbuf.append("VARCHAR_FORMAT(" + columnName[ti][i] + ",'YYYYMMDD'),");
      } else if ("SQLserver".equals(dbType[ci])) {
        if ("DATE".equals(dataType[ti][i]) || "DATETIME".equals(dataType[ti][i])) {
          strbuf.append("CONVERT(varchar," + columnName[ti][i] + ",120),");
        } else {
          strbuf.append(columnName[ti][i] + ",");
        }
      } else {
        strbuf.append(columnName[ti][i] + ",");
      }
    }

    columnString = strbuf.toString();
    columnString = columnString.substring(0, columnString.length() - 1);

    return true;
  } // End of processSelectColumn

  public int getTablePoint() throws Exception {
    return ti;
  }

  public String[] getTableColumn(int i) throws Exception {
    return columnName[i];
  }

  public String[] getColumnDataType(int i) throws Exception {
    return dataType[i];
  }

  public Integer[] getColumnLength(int i) throws Exception {
    return dataLength[i];
  }

  public int getTableColumnCnt(int i) throws Exception {
    return columnCnt[i];
  }

  public void switchDatabase(Connection con) throws Exception {

    /*
     * not useed marked 20170717 by allen PreparedStatement ps = null; String swicthCmd =
     * "use "+dbName; ps = con.prepareStatement(swicthCmd); int retCode = ps.executeUpdate();
     * ps.close();
     */

    return;
  } // End of switchDatabase

  public void setConnectIndex() throws Exception {
    if (dataBase.length() == 0) {
      dataBase = defaultSystem;
    }
    ci = 0;
    for (int i = 0; i < dbCount; i++) {
      if (dataBase.equals(dbComName[i])) {
        ci = i;
      }
    }

  } // End of setConnectIndex

  public void closeCursor() throws Exception {
    closeCursor(0);
    return;
  }

  public void closeCursor(int i) throws Exception {
    fetchName[i] = null;
    int j = fetchTi[i];
    rf[j].close();
    rf[j] = null;
    pf[j].close();
    pf[j] = null;
    columnCnt[j] = 0;
    if (dispCount[j] > 0) {
      showLogMessage("I", "", "FETCH  COUNT : " + dispCount[j] + "  " + dispTable[j]);
    }
    dispCount[j] = 0;
    recCount = 0;
    dispControl = 0;
    return;
  }

  public void closeTableObject() {
    try {
      for (int i = 0; i < rf.length; i++) {
        if (rf[i] != null) {
          rf[i].close();
          rf[i] = null;
        }
        if (pf[i] != null) {
          pf[i].close();
          pf[i] = null;
          columnCnt[i] = 0;
        }
      }
      recCount = 0;
      dispControl = 0;
      // tbIndex = 1;
      accessHash.clear();
      // showLogMessage("I","","closeTableObject ended ");
    }

    catch (Exception ex) {
      return;
    }

    return;
  } // End of closeTableObject

  public boolean setRowId(int pnt, String parmRowId) throws Exception {
    parmData[pnt] = hexStrToByteArr(parmRowId);
    parmCount = pnt;
    return true;
  }

  public byte[] hexStrToByteArr(String str) {
    int len = str.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
    return data;
  }

  public Object[] getSqlParm() {
    Object[] obj = new Object[parmCount];
    for (int ii = 1; ii <= parmCount; ii++)
      obj[ii - 1] = parmData[ii];

    return obj;
  }

  public boolean setString(int pnt, String parmString) throws Exception {
    parmData[pnt] = parmString;
    parmCount = pnt;
    return true;
  }

  public boolean setInt(int pnt, int parmInt) throws Exception {
    parmData[pnt] = parmInt;
    parmCount = pnt;
    return true;
  }

  public boolean setLong(int pnt, long parmLong) throws Exception {
    parmData[pnt] = parmLong;
    parmCount = pnt;
    return true;
  }

  public boolean setDouble(int pnt, double parmDouble) throws Exception {
    BigDecimal cvtNumber = BigDecimal.valueOf(parmDouble);
    parmData[pnt] = cvtNumber;
    parmCount = pnt;
    return true;
  }

  public boolean setParmData(PreparedStatement ps) throws Exception {
    for (int i = 1; i <= parmCount; i++)
    /*
     * { System.out.println("DDDDDDDD "+parmData[i]); ps.setObject(i,parmData[i]); }
     */
    {
      ps.setObject(i, parmData[i]);
    }
    saveParm = parmCount;
    parmCount = 0;
    return true;
  }

  public void clearParm() {
    parmCount = 0;
  }

  public void resetParm() {
    parmCount = 0;
    selectSQL = "";
    indexHint = "";
    dataBase = "";
    accessCode = "";
    whereStr = "";
    extendField = "";
    sqlCmd = "";
    daoTable = "";
    return;
  }

  public void resetValue() {
    if (outputHash != null) {
      outputHash.clear();
      outputHash = null;
    }

    if (workHash != null) {
      workHash.clear();
      workHash = null;
    }

    if (updateHash != null) {
      updateHash.clear();
      updateHash = null;
    }

    for (int k = 0; k <= li; k++) {
      if (listHash[k] != null) {
        listHash[k].clear();
        listHash[k] = null;
      }
      if (indexHash[k] != null) {
        indexHash[k].clear();
        indexHash[k] = null;
      }
    }
//    System.gc(); // fix Code Correctness: Call to System.gc()
    return;
  }

  public void resetColumnValue(int startPnt, int endPnt) {
    for (int i = startPnt; i < endPnt; i++) {
      for (int k = 0; k < columnCnt[ti]; k++) {
        setValue(extendField + columnName[ti][k], "", i);
      }
    }
    return;
  }

  public byte[] getBlob(String fieldName) throws Exception {
    byte[] retnBlob = null;

    fieldName = fieldName.toUpperCase();
    retnBlob = (byte[]) workHash.get(fieldName);
    if (retnBlob == null) {
      return " ".getBytes();
    }

    return retnBlob;
  }

  public HashMap getBufferHash() {
    return outputHash;
  }

  public HashMap[] getListBuffer() {
    return listHash;
  }

  public void setBufferHash(HashMap outputHash) {
    this.outputHash = outputHash;
    return;
  }

  /**
   * sync from mega
   * @param i
   */
  public void clearLoadBuffer(int i) {
    listHash[i].clear();
    indexHash[i].clear();
    return;
  }

  public void setValue(String fieldName, String setData) {
    setValue(fieldName, setData, 0);
    return;
  }

  public void setValueInt(String fieldName, int setData) {
    setValue(fieldName, "" + setData, 0);
    return;
  }

  public void setValueInt(String fieldName, int setData, int int1) {
    setValue(fieldName, "" + setData, int1);
    return;
  }

  public void setValueLong(String fieldName, long setData) {
    setValue(fieldName, "" + setData, 0);
    return;
  }

  public void setValueLong(String fieldName, long setData, int int1) {
    setValue(fieldName, "" + setData, int1);
    return;
  }

  public void setValueDouble(String fieldName, double setData) {
	   
	/* 20201125
    long cvtLong = (long) (setData * 10000000);
    double cvtnNumber = ((double) cvtLong) / 10000000;

    
    setValue(fieldName, "" + cvtnNumber, 0);
    */
	  
    setValue(fieldName,""+setData,0);
    
    return;
  }

  public void setValueDouble(String fieldName, double setData, int int1) {
    setValue(fieldName, "" + setData, int1);
    return;
  }

  public void setValue(String fieldName, String setData, int int1) {

    fieldName = fieldName.toUpperCase() + "#" + int1;
    if (setData == null) {
      setData = "";
    }

    if (!noTrim.equals("Y")) {
      setData = setData.trim();
    }
    outputHash.put(fieldName, setData);

    return;
  }

  public void setValue2(String fieldName, String setData, int int1) {

    fieldName = fieldName.toUpperCase() + "#" + int1;
    if (setData == null) {
      setData = "";
    }

    outputHash.put(fieldName, setData.trim());

    return;
  }

  public void setBlob(String fieldName, byte[] setData) {

    fieldName = fieldName.toUpperCase() + "#0";
    if (setData == null) {
      setData = " ".getBytes();
    }

    byte saveData[] = new byte[setData.length];

    for (int i = 0; i < setData.length; i++) {
      saveData[i] = setData[i];
    }

    workHash.put(fieldName, saveData);

    return;
  }

  public String getValue(String fieldName) throws Exception {
    return getValue(fieldName, 0);
  }

  public int getValueInt(String fieldName) throws Exception {
    return getValueInt(fieldName, 0);
  }

  public long getValueLong(String fieldName) throws Exception {
    return getValueLong(fieldName, 0);
  }

  public double getValueDouble(String fieldName) {
    return getValueDouble(fieldName, 0);
  }

  public BigDecimal getValueBigDecimal(String fieldName) {
    return getValueBigDecimal(fieldName, 0);
  }

  public String getValue2(String fieldName, int int1) throws Exception {
    String retnStr = "";

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) outputHash.get(fieldName);
    if (retnStr == null) {
      return "";
    }

    return retnStr.trim();
  }

  public String getValue3(String fieldName) throws Exception {
    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#0";
    String retnStr = (String) outputHash.get(fieldName);
    if (retnStr == null) {
      return null;
    }

    if (covertBig5.equals("Y")) {
      retnStr = new String(retnStr.getBytes("iso-8859-1"), "big5");
    }
    if (!noTrim.equals("Y")) {
      retnStr = retnStr.trim();
    }

    return retnStr;
  }


  public String getValue(String fieldName, int int1) throws Exception {
    String retnStr = "";

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) outputHash.get(fieldName);
    if (retnStr == null) {
      return "";
    }

    if (covertBig5.equals("Y")) {
      retnStr = new String(retnStr.getBytes("iso-8859-1"), "big5");
    }
    if (!noTrim.equals("Y")) {
      retnStr = retnStr.trim();
    }

    return retnStr;
  }

  public int getValueInt(String fieldName, int int1) throws Exception {
    String retnStr = "";
    int retnNumber = 0;

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) outputHash.get(fieldName);
    if (retnStr == null) {
      return 0;
    }
    if (retnStr.equals("null")) {
      return 0;
    }
    if (retnStr.length() == 0) {
      return 0;
    }

    // retnNumber = Integer.parseInt(retnStr.trim());
    retnNumber = (int) (Double.parseDouble(retnStr.trim()));

    return retnNumber;
  }

  public long getValueLong(String fieldName, int int1) {
    String retnStr = "";
    long retnNumber = 0;

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) outputHash.get(fieldName);
    if (retnStr == null) {
      return 0;
    }
    if (retnStr.equals("null")) {
      return 0;
    }
    if (retnStr.length() == 0) {
      return 0;
    }

    // retnNumber = Long.parseLong(retnStr.trim());
    retnNumber = (long) (Double.parseDouble(retnStr.trim()));

    return retnNumber;
  }

  public double getValueDouble(String fieldName, int int1) {
    String retnStr = "";
    double retnNumber = 0;

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) outputHash.get(fieldName);
    if (retnStr == null) {
      return 0;
    }
    if (retnStr.equals("null")) {
      return 0;
    }
    if (retnStr.length() == 0) {
      return 0;
    }
    retnNumber = Double.parseDouble(retnStr.trim());

    return retnNumber;
  }

  public BigDecimal getValueBigDecimal(String fieldName, int int1) {
    double cvtNumber = getValueDouble(fieldName, int1);
    BigDecimal retnNumber = BigDecimal.valueOf(cvtNumber);
    return retnNumber;
  }

  public String getList(String fieldName, int int1) throws Exception {
    String retnStr = "";

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) listHash[li].get(fieldName);
    if (retnStr == null) {
      return "";
    }

    if (covertBig5.equals("Y")) {
      retnStr = new String(retnStr.getBytes("iso-8859-1"), "big5");
    }

    retnStr = retnStr.trim();

    return retnStr;
  }

  public double getListDouble(String fieldName, int int1) throws Exception {
    String retnStr = "";
    double retnNumber = 0;

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) listHash[li].get(fieldName);
    if (retnStr == null) {
      return 0;
    }
    if (retnStr.length() == 0) {
      return 0;
    }

    retnNumber = Double.parseDouble(retnStr.trim());

    return retnNumber;
  }

  public long getListLong(String fieldName, int int1) {
    String retnStr = "";
    long retnLong = 0;

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) listHash[li].get(fieldName);
    if (retnStr == null) {
      return 0;
    }
    if (retnStr.length() == 0) {
      return 0;
    }

    retnLong = Long.parseLong(retnStr.trim());

    return retnLong;
  }

  public int getListInt(String fieldName, int int1) {
    String retnStr = "";
    int retnInteger = 0;

    fieldName = fieldName.toUpperCase();
    fieldName = fieldName + "#" + int1;
    retnStr = (String) listHash[li].get(fieldName);
    if (retnStr == null) {
      return 0;
    }
    if (retnStr.length() == 0) {
      return 0;
    }

    retnInteger = Integer.parseInt(retnStr.trim());

    return retnInteger;
  }

  public int getLoadCnt() {
    return loadCnt[li];
  }

  public int getLoadIndex() {
	return li;
  }

  public void setLoadIndex(int i) {
     li = i;
     return;
  }

  public void setList(String fieldName, String listValue, int int1) {

    fieldName = fieldName.toUpperCase();
    if (listHash[li] == null) {
      listHash[li] = new HashMap();
      indexHash[li] = new HashMap();
    }

    if (listValue == null) {
      listValue = "";
    }
    listValue = listValue.trim();
    fieldName = fieldName + "#" + int1;
    listHash[li].put(fieldName, listValue);

    return;
  }

  public void setLoadData(String parmKeyField) throws Exception {
    workHash.put(parmKeyField + "-T", "" + ti);
    workHash.put(parmKeyField + "-L", "" + li);

    String accessKey = "";
    String[] colkeyField = parmKeyField.split(",");
    int row = 0;
    for (int i = 0; i < getLoadCnt(); i++) {
      accessKey = "";
      for (int j = 0; j < colkeyField.length; j++) {
        accessKey = accessKey + "#" + getList(colkeyField[j], i);
      }
      if ((String) indexHash[li].get("#" + row + accessKey) == null) {
        row = 0;
      } else {
        row++;
      }
      indexHash[li].put("#" + row + accessKey, "" + i);
      // System.out.println("#"+row+accessKey+" "+i);
    }
    return;
  }

  /*
   * public int getLoadSort(String parmKeyField,String[] sortField) throws Exception { String
   * tempExtend = extendField; extendField = "TMP.";
   * 
   * int cnt = getLoadData(parmKeyField);
   * 
   * if ( tempExtend.length() < 2 ) { tempExtend = loadExt[li]; }
   * 
   * SortObject srt = new SortObject(this); srt.sortLoadData(sortField,cnt);
   * 
   * for ( int i=0; i<cnt; i++ ) { int k = srt.getSortIndex(i); for( int j=0; j<columnCnt[ti]; j++ )
   * { setValue(tempExtend+columnName[ti][j],getValue("TMP."+columnName[ti][j],k),i); } } srt =
   * null; return cnt; }
   */
  public int getLoadData(String parmKeyField) throws Exception {
    extendField = extendField.toUpperCase();
    String iT = (String) workHash.get(parmKeyField + "-T");
    String nL = (String) workHash.get(parmKeyField + "-L");
    if (iT == null) {
      notFound = "Y";
      return 0;
    }
    // { showLogMessage("E","","LOAD KEY ERROR : "+parmKeyField); notFound="Y"; return 0; }

    String[] colkeyField = parmKeyField.split(",");
    String parmKeyData = "";
    for (int j = 0; j < colkeyField.length; j++) {
      parmKeyData = parmKeyData + "#" + getValue(colkeyField[j]);
    }

    ti = Integer.parseInt(iT);
    li = Integer.parseInt(nL);
    String cvtField = loadExt[li];
    if (extendField.length() >= 2) {
      cvtField = extendField;
    }
    extendField = "";

    String kP = (String) indexHash[li].get("#0" + parmKeyData);

    if (kP == null) {
      for (int j = 0; j < columnCnt[ti]; j++) {
        if (parmKeyField.indexOf(columnName[ti][j]) == -1) {
          setValue(cvtField + columnName[ti][j], "");
        }
      }
      // showLogMessage("W","","NOT FOUND KEY : "+parmKeyData);
      notFound = "Y";
      return 0;
    }

    int row = 0;
    for (row = 0; row < loadLimit; row++) {
      kP = (String) indexHash[li].get("#" + row + parmKeyData);
      if (kP == null) {
        break;
      }

      int k = Integer.parseInt(kP);
      for (int j = 0; j < columnCnt[ti]; j++) {
        setValue(cvtField + columnName[ti][j], getList(loadExt[li] + columnName[ti][j], k), row);
      }
    }
    return row;
  }

  /* ###### add memory update start ########## */

  public void setUpdateLoad(String fieldName, String parmValue) {
    updateHash.put(fieldName.toUpperCase(), parmValue);
    return;
  }

  // 單筆更新 BUFFER LOAD DATA
  public int updateLoadTable(String parmKeyField, int ui) throws Exception {
    String iT = (String) workHash.get(parmKeyField + "-T");
    String nL = (String) workHash.get(parmKeyField + "-L");
    if (iT == null) {
      notFound = "Y";
      updateHash.clear();
      return 0;
    }

    String[] colkeyField = parmKeyField.split(",");
    String parmKeyData = "";
    for (int j = 0; j < colkeyField.length; j++) {
      parmKeyData = parmKeyData + "#" + getValue(colkeyField[j]);
    }
    ti = Integer.parseInt(iT);
    li = Integer.parseInt(nL);

    String kP = (String) indexHash[li].get("#" + ui + parmKeyData);
    if (kP == null) {
      notFound = "Y";
      updateHash.clear();
      return 0;
    }

    int int1 = Integer.parseInt(kP);
    for (int j = 0; j < columnCnt[ti]; j++) {
      String updData = (String) updateHash.get(loadExt[li] + columnName[ti][j]);
      if (updData == null) {
        continue;
      }
      setList(loadExt[li] + columnName[ti][j], updData, int1);
    }
    updateHash.clear();
    return 1;
  }

  // 多筆更新 BUFFER LOAD DATA
  public int updateLoadTable(String parmKeyField) throws Exception {
    String iT = (String) workHash.get(parmKeyField + "-T");
    String nL = (String) workHash.get(parmKeyField + "-L");
    if (iT == null) {
      notFound = "Y";
      updateHash.clear();
      return 0;
    }

    String[] colkeyField = parmKeyField.split(",");
    String parmKeyData = "";
    for (int j = 0; j < colkeyField.length; j++) {
      parmKeyData = parmKeyData + "#" + getValue(colkeyField[j]);
    }

    ti = Integer.parseInt(iT);
    li = Integer.parseInt(nL);

    String kP = (String) indexHash[li].get("#0" + parmKeyData);
    if (kP == null) {
      notFound = "Y";
      updateHash.clear();
      return 0;
    }

    int updateRow = 0;
    for (int row = 0; row < loadLimit; row++) {
      kP = (String) indexHash[li].get("#" + row + parmKeyData);
      if (kP == null) {
        break;
      }
      int int1 = Integer.parseInt(kP);
      // System.out.println("ttt updTable 222 "+"#"+row+parmKeyData);
      updateRow++;
      for (int j = 0; j < columnCnt[ti]; j++) {
        String updData = (String) updateHash.get(loadExt[li] + columnName[ti][j]);
        if (updData == null) {
          continue;
        }
        setList(loadExt[li] + columnName[ti][j], updData, int1);
        // System.out.println("ttt updTable 333 "+loadExt[li]+columnName[ti][j]+" "+updData+" "+k);
      }
    }
    updateHash.clear();
    return updateRow;
  }

  /* ###### add memory update ended ########## */

  public void getTableIndex(String sqlCode) throws Exception {
    String checkWhere = whereStr;
    if (sqlCode.equals("F")) {
      daoTable = fetchName[ftIndex];
      checkWhere = "";
    }

    String comTable = "";
    if (specialSQL.equals("R")) // SQL 變數可以用 加 的
    {
      ti = 0;
      specialSQL = "";
      return;
    }

    if (specialSQL.equals("W")) // FIX ti
    {
      ti = tbLimit - 1;
      columnCnt[ti] = 0;
      specialSQL = "";
      return;
    }

    if (specialSQL.length() > 0) {
      comTable = sqlCode + specialSQL + daoTable;
    } else if (sqlCmd.length() > 0 && !sqlCode.equals("F")) // full SQL excute
    {
      comTable = sqlCode + sqlCmd.hashCode();
    } else if (sqlCode.equals("S")) // select SQL
    {
      comTable = sqlCode + (daoTable.hashCode() + checkWhere.hashCode() + selectSQL.hashCode());
    } else if (sqlCode.equals("F") || sqlCode.equals("I")) // FETCH or Insert SQL
    {
      comTable = sqlCode + daoTable;
    } else // update SQL
    {
      comTable = sqlCode + (daoTable.hashCode() + checkWhere.hashCode() + updateSQL.hashCode());
    }

    specialSQL = "";
    Integer chkPnt = accessHash.get(comTable);
    if (chkPnt != null) {
      ti = chkPnt.intValue();
      return;
    }
    ti = tbIndex;
    accessHash.put(comTable, ti);
    dispTable[ti] = daoTable;
    tbIndex++;
    if (tbIndex >= tbLimit) {
      showLogMessage("E", "",
          "getTableIndex out of range Exception : " + tbIndex + " : " + daoTable);
      ti = -1;
    }
    return;
  }

  public int openInputText(String parmFile) {
    return openInputText(parmFile, "");
  }

  public int openInputText(String parmFile, String fileType) {
    // filter escape char
    String tempPath = SecurityUtil.verifyPath(parmFile);
    int fr = -1;
    try {
      for (int i = 0; i < maxInFile; i++) {
        if (dr[i] != null) {
          continue;
        }
        if (fileType.length() == 0)
          dr[i] = new BufferedReader(new FileReader(tempPath));
        else
          dr[i] =
              new BufferedReader(new InputStreamReader(new FileInputStream(tempPath), fileType));
        inFile[i] = tempPath;
        endFile[i] = "";
        fr = i;
        break;
      }
    } catch (Exception ex) {
      showLogMessage("E", "openInputText", "INPUT FILE ERROR : " + tempPath);
      return -1;
    }

    return fr;
  }

  public int closeInputText(int i) {
    try {
      if (readCnt[i] > 0 && showReadWrite.equals("Y")) {
        /* showLogMessage("I","",inFile[i]+" FILE-READ  COUNT : "+readCnt[i]); */ }
      dr[i].close();
      dr[i] = null;
      readCnt[i] = 0;
      endFile[i] = "";
    } catch (Exception ex) {
      showLogMessage("E", "closeInputText", "closeInputText INPUT FILE ERROR : " + i);
      dr[i] = null;
      return -1;
    }

    return i;
  }

  public String readTextFile(int i) throws Exception {
    int inti = i;
    String inString = "";
    if (dr[i].ready()) {
      inString = dr[inti].readLine();
      readCnt[inti]++;
      return inString;
    }
    endFile[inti] = "Y";
    return "";
  }

  public int openOutputText(String parmFile) throws Exception {
    return openOutputText(parmFile, "");
  }

  public int openOutputText(String parmFile, String fileType) throws Exception {
	  String tempPath = SecurityUtil.verifyPath(parmFile);
    int fo = -1;

    for (int i = 0; i < maxOutFile; i++) {
      if (dw[i] != null) {
        continue;
      }
      if (fileType.length() == 0) {
        dw[i] = new BufferedWriter(new FileWriter(tempPath));
      } else if (fileType.equals("append")) {
        dw[i] = new BufferedWriter(new FileWriter(tempPath, true));
      } else {
        dw[i] =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempPath), fileType));
      }

      outFile[i] = tempPath;
      fo = i;
      break;
    }

    return fo;
  }

  public int closeOutputText(int i) throws Exception {
    if (writeCnt[i] > 0) {
      showLogMessage("I", "", outFile[i] + " FILE-WRITE  COUNT : " + writeCnt[i]);
    }
    dw[i].close();
    dw[i] = null;
    writeCnt[i] = 0;

    return i;
  }

  public boolean writeTextFile(int i, String outData) throws Exception {
    dw[i].write(outData);
    dw[i].flush();
    writeCnt[i]++;
    return true;
  }

  public int getWriteCount(int i) throws Exception {
    return writeCnt[i];
  }

  public boolean openBinaryInput(String parmFile) {
	String tempPath = SecurityUtil.verifyPath(parmFile);
    try {
      fin = new FileInputStream(tempPath);
      dis = new DataInputStream(fin);
      inFile[0] = tempPath;
    } catch (Exception ex) {
      showLogMessage("E", "openBinaryInput", "FILE : " + tempPath);
      return false;
    }
    return true;
  }

  public int readBinFile(byte[] inData) throws Exception {
    int inputLen = dis.read(inData);
    readCnt[0]++;
    return inputLen;
  }

  public void closeBinaryInput() throws Exception {
    dis.close();
    dis = null;
    return;
  }

  public boolean openBinaryOutput(String parmFile) throws Exception {
    openBinaryOutput2(parmFile);
    return true;
  }

  public int openBinaryOutput2(String parmFile) throws Exception {
	  String tempPath = SecurityUtil.verifyPath(parmFile);
    int fo = -1;
    for (int i = 0; i < maxBinOut; i++) {
      if (dos[i] != null) {
        continue;
      }
      dos[i] = new DataOutputStream(new FileOutputStream(tempPath));
      outBin[i] = tempPath;
      fo = i;
      break;
    }
    return fo;
  }

  public boolean writeBinFile(byte[] outData, int len) throws Exception {
    writeBinFile2(0, outData, len);
    return true;
  }

  public boolean writeBinFile2(int i, byte[] outData, int len) throws Exception {
    dos[i].write(outData, 0, len);
    dos[i].flush();
    writeBin[i]++;
    return true;
  }

  public void closeBinaryOutput() throws Exception {
    closeBinaryOutput2(0);
    return;
  }

  public void closeBinaryOutput2(int i) throws Exception {
    dos[i].close();
    dos[i] = null;
    return;
  }

  public void commitDataBase() {

    for (int i = 0; i < dbCount; i++) {
      try {
        if (conn[i] != null) {
          conn[i].commit();
        }
      } catch (Exception ex2) {
        continue;
      }
    }

    // showLogMessage("I","","COMMIT");
  }

  public void countCommit() throws Exception {
    setConnectIndex();
    conn[ci].commit(); // showLogMessage("I","","COMMIT "+dataBase);

  }

  public void closeConnect() {
    try {
      for (int i = 0; i < dbCount; i++) {
        if (conn[i] != null) {
          conn[i].close();
          conn[i] = null;
        }
      }

      for (int i = 0; i < maxInFile; i++) {
        if (dr[i] != null) {
          dr[i].close();
          dr[i] = null;
        }
      }

      for (int i = 0; i < maxOutFile; i++) {
        if (dw[i] != null) {
          dw[i].close();
          dw[i] = null;
        }
      }

      for (int i = 0; i < maxBinOut; i++) {
        if (dos[i] != null) {
          dos[i].close();
          dos[i] = null;
        }
      }

      for (int i = 0; i < 20; i++) {
        if (listHash[i] != null) {
          listHash[i].clear();
          listHash[i] = null;
        }
        if (indexHash[i] != null) {
          indexHash[i].clear();
          indexHash[i] = null;
        }
      }

      workHash.clear();
      updateHash.clear();
      outputHash.clear();
      workHash = null;
      updateHash = null;
      outputHash = null;

      if (fin != null) {
        fin.close();
        fin = null;
      }

      // showLogMessage("I","","closeConnect ended ");
    }

    catch (Exception ex) {
      expMethod = "closeConnect";
      return;
    }

    return;
  }

  public void rollbackDataBase() {
    try {
      for (int i = 0; i < dbCount; i++) {
        if (conn[i] != null) {
          conn[i].rollback();
        }
      }

      /*
       * if ( outputHash != null ) { outputHash.clear(); outputHash = null; }
       */
    }

    catch (Exception ex) {
      expMethod = "rollbackDataBase";
      System.out.println("ROLLBACK DATABASE EXCEPTION ");
      return;
    }

    return;
  }

  public long getPID() {
    String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    return Long.parseLong(processName.split("@")[0]);
  }

  public boolean connectDataBase() throws Exception {
    return connectDataBase("");
  }

  public boolean connectDataBase(String FSbase1, String FSbase2) throws Exception {
    if (!connectDataBase(FSbase1))
      return false;
    return connectDataBase(FSbase2);
  }

  public boolean connectDataBase(String FSbase1, String FSbase2, String FSbase3) throws Exception {
    if (!connectDataBase(FSbase1))
      return false;
    if (!connectDataBase(FSbase2))
      return false;
    return connectDataBase(FSbase3);
  }

  public boolean connectDataBase(String fsSystem) throws Exception {
    // javaProgram = this.getClass().getName();
//    System.gc(); // fix Code Correctness: Call to System.gc()
    javaProgram = super.getClass().getSimpleName();

    if (!getSystemParm(fsSystem))
      return false;

    fsSystem = sysName;

    // tbIndex = 1;
    for (int i = 0; i < tbLimit; i++) {
      columnCnt[i] = 0;
    }

    dbType[di] = checkType;
    if (conn[di] != null) {
      conn[di].close();
    }

    if ("ORACLE".equals(dbType[di])) {
      garStr = "oracle.jdbc.driver.OracleDriver";
      // Class.forName(garStr);
      conn[di] = DriverManager.getConnection(
          "jdbc:oracle:thin:@" + dbHost + ":" + portNo + ":" + dbName, dbUser, dbhideWd);
      conn[di].setAutoCommit(false);
    } else if ("ORACLE12".equals(dbType[di])) {
      // Class.forName("oracle.jdbc.driver.OracleDriver");
      conn[di] = DriverManager.getConnection(
          "jdbc:oracle:thin:@" + dbHost + ":" + portNo + "/" + dbName, dbUser, dbhideWd);
      conn[di].setAutoCommit(false);
      dbType[di] = "ORACLE";
    } else if ("DB2".equals(dbType[di])) {
      garStr = "com.ibm.db2.jcc.DB2Driver";
      // Class.forName(garStr);
      // conn[di] =
      // DriverManager.getConnection("jdbc:db2://"+dbHost+":"+portNo+"/"+dbName,dbUser,dbhideWd);
      conn[di] = DriverManager.getConnection("jdbc:db2://"
          + dbHost
          + ":"
          + portNo
          + "/"
          + dbName
          + ":currentSchema="
          + schema
          + ";currentFunctionPath="
          + schema
          + ";", dbUser, dbhideWd);
      conn[di].setAutoCommit(false);
    } else {
      showLogMessage("E", "connectDataBase",
          fsSystem + " : CONNECT  DATABASE FAILED : NO MATCH DATABASE TYPE ");
      return false;
    }

    dbComName[di] = fsSystem;
    dbCount++;
    if ("DB2".equals(dbType[di]) && flowDB.length() == 0) {
      insertFlowControl();
    }
    di++;

    dateTime();
    if (displayConnect.equals("Y")) {
      showLogMessage("I", "connectDataBase",
          "CONNECT  DATABASE " + checkType + " " + dbName + " " + dbUser + " SUCCESS ");
    }

    return true;
  }

  public boolean getSystemParm(String fsSystem) throws Exception {
    startMillis = "" + System.currentTimeMillis();
    startDate = sysDate;
    startTime = sysTime;

    String checkHome = System.getenv("PROJ_HOME");
    if (checkHome == null) {
      checkHome = "";
      ClassLoader classLoader = this.getClass().getClassLoader();
      if (classLoader != null) {
		URL resource = classLoader.getResource("");
		if (resource != null) {
			checkHome = resource.getPath();
		}
	  }
    }

    //String confFile = checkHome + "/conf/DB_Connect_Parm.txt";
    String confFile = checkHome + "/conf/ecsParameter.properties";
    confFile = SecurityUtil.verifyPath(confFile);
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(confFile);) {
      props.load(fis);
      fis.close();
    }
    
    String dbConfFile = checkHome + "/conf/config.properties";
    dbConfFile = SecurityUtil.verifyPath(dbConfFile);
    Properties dbProps = new Properties();
    try (FileInputStream dbFis = new FileInputStream(dbConfFile);) {
      dbProps.load(dbFis);
      dbFis.close();
    }

    debugMode = props.getProperty("DEBUG_MODE").trim();
    if (debugMode == null) {
      debugMode = "N";
    }

    consoleMode = props.getProperty("CONSOLE_MODE").trim();
    if (consoleMode == null) {
      consoleMode = "N";
    }

    mqHost = props.getProperty("MQ_HOST");
    mqPort = props.getProperty("MQ_PORT");

    if (mqHost != null) {
      mqHost = mqHost.trim();
    }

    if (mqPort != null) {
      mqPort = mqPort.trim();
    }

    if (fsSystem.equals("C")) {
      return true;
    }

    
    defaultSystem = props.getProperty("DEFAULT_CONN_DB");
    if (fsSystem.length() == 0) {
      fsSystem = defaultSystem;
    }
    //checkType = props.getProperty(fsSystem + "_TYPE");
    defaultDbTyp = props.getProperty("DEFAULT_DB_TYPE");
    if (defaultDbTyp == null) {
      showLogMessage("E", "connectDataBase", "連線資料庫 " + fsSystem + " 不存在定義檔中");
      return false;
    } 
    checkType = defaultDbTyp.trim();

    portNo = dbProps.getProperty(fsSystem + ".port").trim();
    dbUser = dbProps.getProperty(fsSystem + ".user").trim();
    dbhideWd = dbProps.getProperty(fsSystem + ".pw");
    dbhideWd = dbhideWd != null ? dbhideWd.trim() : dbhideWd;
    dbName = dbProps.getProperty(fsSystem + ".db").trim();
    dbHost = dbProps.getProperty(fsSystem + ".ip").trim();
    //dbOwner = props.getProperty(fsSystem + "_DBOWNER").trim();
    dbOwner = "";
    schema = dbProps.getProperty(fsSystem + ".schema").trim();
    sysName = fsSystem;
    
    pawdEncrypted = props.getProperty("PWD_ENCRYPTED"); 
    if ("TCB".equals(pawdEncrypted.trim())) {
    	dbhideWd = getDBPwdFromAcdp(fsSystem, checkHome);
    	Decryptor decrptor = new Decryptor();
        dbhideWd = decrptor.doDecrypt(dbhideWd);
        //showLogMessage("I", "xxxxxxxx","dbhideWd=[" + dbhideWd + "]"); //debug use
    }
    
    if (schema == null || schema.trim().length() == 0) {
      schema = dbUser.toUpperCase();
    }

    return true;
  }

	private String getDBPwdFromAcdp(String fsSystem, String checkHome) throws IOException, FileNotFoundException {
		String acdpFile = "/PKI/acdp.properties";
//		String acdpFile = checkHome + "/PKI/acdp.properties";
		acdpFile = SecurityUtil.verifyPath(acdpFile);
		Properties acdpProps = new Properties();
		try (FileInputStream fis = new FileInputStream(acdpFile);) {
			acdpProps.load(fis);
			fis.close();
		}
		return acdpProps.getProperty(fsSystem + ".db").trim();
	}

  public void setSubParm(String[] dbAlias) throws Exception {
    subFlag = "Y";
    initialFlag = "N";
    finalExit = "N";
    dbCount = 0;
    for (int i = 0; i < dbAlias.length; i++) {
      if (dbAlias[i].length() == 0) {
        continue;
      }
      getSystemParm(dbAlias[i]);
      dbType[i] = checkType;
      dbCount++;
    }
    return;
  }

  public Connection getCurrConnect(String dbParm) throws Exception {
    dataBase = dbParm;
    setConnectIndex();
    return conn[ci];
  }

  public Connection[] getDBconnect() {
    return conn;
  }

  public String[] getDBalias() {
    return dbComName;
  }

  public void setDBalias(String[] dbAlias) throws Exception {
    this.dbComName = dbAlias;
    return;
  }

  public long durationTime(String startMillis) {
    long startNum = 0, endNum = 0, duration = 0;

    if (startMillis.length() == 0) {
      startMillis = "" + System.currentTimeMillis();
    }

    startNum = Long.parseLong(startMillis);
    endNum = System.currentTimeMillis();

    duration = (endNum - startNum) / 1000;
    durTime = String.format("%02d", duration / 3600)
        + " : "
        + String.format("%02d", (duration % 3600) / 60)
        + " : "
        + String.format("%02d", (duration % 60));

    return duration;
  }

  public void processDisplay(int dispLimit) {
    recCount++;
    dispControl++;
    if (dispControl == dispLimit) {
      showLogMessage("I", "processDisplay", "PROCESS COUNT : " + recCount);
      dispControl = 0;
    }

    return;
  } // End of processDisplay

  public void exitProgram(int exitCode) throws Exception {
    closeTableObject(); // add by Jack,Liao
    closeConnect(); // add by jack,liao 2018/05/08
    setExceptExit(exitCode);
    // showLogMessage("E","","error occurs exit by program : "+javaProgram);
    throw new Exception();
  }

  public void programEnd(int exitCode) {
    System.exit(exitCode);
  }
  
  public void setSkipHash(HashMap parmHash,String parmKeyField)
  {
     this.skipHash = parmHash;
     this.skipKeyField = parmKeyField;
     this.skipCheck = true;
     return;

  }

  public void setNormalExit(int exitCode) {
    normalExit = exitCode;
  }

  public void setExceptExit(int exitCode) {
    exceptExit = exitCode;
  }

  public int finalProcess() {
    durationTime(startMillis);
    updateFlowControl();
    showStatistic();
    if (subFlag.equals("Y")) {
      return 0;
    }

    if (!exceptionFlag.equals("Y")) {
      commitDataBase();
    }

    closeTableObject();
    closeConnect();
    resetValue();
    showLogMessage("I", "", "");
    showLogMessage("I", "", "ENDED    TIME");
    showLogMessage("I", "", "DURATION TIME : " + durTime);
    // if ( finalExit.equals("Y") )
    // { System.exit(normalExit); }

    return normalExit;
  } // End of finalProcess

  public void showStatistic() {
    showLogMessage("I", "", "");
    for (int i = 0; i < 2; i++) {
      if (readCnt[i] > 0 && showReadWrite.equals("Y")) {
        showLogMessage("I", "", inFile[i] + " FILE-READ  COUNT : " + readCnt[i]);
      }
    }

    String countData = "", spaces = "           ";
    for (int i = 0; i < tbLimit; i++) {
      if (dispCount[i] == 0) {
        continue;
      }

      if (dispTable[i].equals("ECS_FLOW_CONTROL")) {
        continue;
      }

      countData = "" + dispCount[i];
      countData = dispCount[i] + spaces.substring(0, 10 - countData.length());
      if (dispFlag[i].equals("L")) {
        showLogMessage("I", "", "LOAD   COUNT : " + countData + dispTable[i]);
      } else if (dispFlag[i].equals("F")) {
        showLogMessage("I", "", "FETCH  COUNT : " + countData + dispTable[i]);
      } else if (dispFlag[i].equals("S")) {
        showLogMessage("I", "", "SELECT COUNT : " + countData + dispTable[i]);
      } else if (dispFlag[i].equals("U")) {
        showLogMessage("I", "", "UPDATE COUNT : " + countData + dispTable[i]);
      } else if (dispFlag[i].equals("I")) {
        showLogMessage("I", "", "INSERT COUNT : " + countData + dispTable[i]);
      } else if (dispFlag[i].equals("D")) {
        showLogMessage("I", "", "DELETE COUNT : " + countData + dispTable[i]);
      }

      dispCount[i] = 0;
      dispTable[i] = "";
    }

    for (int i = 0; i < 15; i++) {
      if (writeCnt[i] > 0) {
        showLogMessage("I", "", outFile[i] + " FILE-WRITE COUNT : " + writeCnt[i]);
      }
      if (writeBin[i] > 0) {
        showLogMessage("I", "", outBin[i] + " FILE-WRITE COUNT : " + writeBin[i]);
      }
      writeCnt[i] = 0;
      writeBin[i] = 0;
    }

    return;
  }

  public void insertFlowControl() {

    try {
      dateTime();
      pgmStartDate = sysDate;
      pgmStartTime = sysTime;
      setValue("PROGRAM_NAME", javaProgram);
      setValue("LOG_START_DATE", pgmStartDate);
      setValue("LOG_START_TIME", pgmStartTime);
      setValue("LOG_END_DATE_DATE", "");
      setValue("LOG_END_TIME", "");
      setValue("STATUS_CODE", "01");
      setValue("MESG_DATA", "處理中.....");
      daoTable = "ECS_FLOW_CONTROL";
      getTableIndex("I");
      insertTable();
      commitDataBase();
      flowDB = dbComName[ci];
    }

    catch (Exception ex2) {
      flowDB = "";
    }

    return;
  }


  public void updateFlowControl() {

    try {
      String procStatus = "", procMesg = "";

      if (flowDB.length() == 0) {
        return;
      }

      dateTime();
      if (exceptionFlag.equals("Y")) {
        procMesg = expSystemMesg;
        procStatus = "EX";
      } else {
        procMesg = "處理完成";
        procStatus = "00";
      }

      daoTable = "ECS_FLOW_CONTROL";
      updateSQL =
          "LOG_END_DATE = ?, LOG_END_TIME = ? ,DURATION_TIME = ?,STATUS_CODE = ?,MESG_DATA = ? ";
      whereStr = "WHERE PROGRAM_NAME = ? and LOG_START_DATE = ? and  LOG_START_TIME = ? ";
      setString(1, sysDate);
      setString(2, sysTime);
      setString(3, durTime);
      setString(4, procStatus);
      setString(5, procMesg);
      setString(6, javaProgram);
      setString(7, pgmStartDate);
      setString(8, pgmStartTime);
      dataBase = flowDB;
      getTableIndex("U");
      updateTable();
      if (exceptionFlag.equals("Y")) {
        commitDataBase();
      }
    }

    catch (Exception ex2) {
      ;
    }

    return;
  }

  public void programException(Exception ex, String errorMesg, String grade, String apMesg)
      throws Exception {
    expErrorMesg = errorMesg;
    expGrade = grade;
    expApMesg = apMesg;
    exitProgram(-1);
    return;
  }

  public void expHandle(Exception ex) {
    try {
      exceptionFlag = "Y";

      rollbackDataBase();
      // commitDataBase();

      String newLine = "";
      byte[] carriage = new byte[3];

      carriage[0] = 0x0D;
      carriage[1] = 0x0A;
      newLine = new String(carriage, 0, 2);
      if (logger == null) {
        initialLog4j();
      }

      // if ( displayParmData.equals("Y") )
      // { displayAccessKey(); }

      //if (ex.getMessage() == null || !ex.getMessage().equals("DXC_NOSHOW_EXCEPTION")) // modify by
                                                                                      // brian
                                                                                      // 2018/09/19
      if (ex.getMessage() == null || !ex.getMessage().contains("DXC_NOSHOW_EXCEPTION"))  
      {
        logger.fatal(" >> ####### PROGRAM ERROR MESSAGE STARTED ######");
        logger.fatal(CommString.validateLogData(" SQL command : " + sqlCmd));
        dispMesg = "PROGRAM : " + javaProgram + " <BR>" + ex.getMessage();
        expSystemMesg = ex.getMessage();
        logger.fatal("Exception_Message : ", ex);
        logger.fatal(" >> ####### PROGRAM ERROR MESSAGE ENDED   ######");
      }

      // updateFlowControl(); // add by Jack,Liao 2019/05/16
      closeTableObject(); // add by Jack,Liao
      closeConnect(); // add by jack,liao 2018/05/08
    } catch (Exception ex2) {
      ;
    }

    return;
  }

  public void initialLog4j() {
	    try {
	      getSystemParm("C");
	      String checkHome = System.getenv("PROJ_HOME");
	      if (checkHome == null) {
	    	  checkHome = "";
	    	  ClassLoader classLoader = this.getClass().getClassLoader();
	    	  if (classLoader != null) {
	    		URL resource = classLoader.getResource("");
				if (resource != null) {
					checkHome = resource.getPath();
				}
			  }
	      }

//      String logConf = checkHome + "/conf/Log4j.properties";
//      switch (logFlag) {
//        case "mqm":
//          logConf = checkHome + "/conf/Log4j_mqm.properties";
//          break; // MQ
//        case "appc":
//          logConf = checkHome + "/conf/Log4j_appc.properties";
//          break; // APPC
//        case "call":
//          logConf = checkHome + "/conf/Log4j_call_batch.properties";
//          break; // ON-LINE CALL BATCH
//        case "ips":
//          logConf = checkHome + "/conf/Log4j_ips.properties";
//          break; // 一卡通
//        case "web":
//          logConf = checkHome + "/conf/Log4j_web.properties";
//          break; // 網路商城
//        case "wbk":
//          logConf = checkHome + "/conf/Log4j_wbk.properties";
//          break; // 新網路銀行
//        case "ebk":
//          logConf = checkHome + "/conf/Log4j_ebk.properties";
//          break; // E BANK ( 線上辦卡 )
//        case "sms":
//          logConf = checkHome + "/conf/Log4j_sms.properties";
//          break; // 簡訊
//        case "cti":
//          logConf = checkHome + "/conf/Log4j_cti.properties";
//          break; // 新語音系統
//        case "voc":
//          logConf = checkHome + "/conf/Log4j_voc.properties";
//          break; // 語音系統
//        case "mob":
//          logConf = checkHome + "/conf/Log4j_mob.properties";
//          break; // 行動支附
//        case "tsc":
//          logConf = checkHome + "/conf/Log4j_tsc.properties";
//          break; // 悠遊卡
//        case "ibn":
//          logConf = checkHome + "/conf/Log4j_ibn.properties";
//          break; // IBON
//        default:
//          logConf = checkHome + "/conf/Log4j.properties";
//          break; // 一般 BATCH
//      }
//      logConf = Normalizer.normalize(logConf, Normalizer.Form.NFKC);
//      PropertyConfigurator.configure(logConf);
//      logger = (Logger) LogManager.getLogger(AccessDAO.class);
      
		String logName = DEFAULT_LOGGER_NAME;

		switch (logFlag) {
		case "mqm":
			logName = "mqm";
			break; // MQ
		case "appc":
			logName = "appc";
			break; // APPC
		case "call":
			logName = "call";
			break; // ON-LINE CALL BATCH
		case "ips":
			logName = "ips";
			break; // 一卡通
		case "web":
			logName = "web";
			break; // 網路商城
		case "wbk":
			logName = "wbk";
			break; // 新網路銀行
		case "ebk":
			logName = "ebk";
			break; // E BANK ( 線上辦卡 )
		case "sms":
			logName = "sms";
			break; // 簡訊
		case "cti":
			logName = "cti";
			break; // 新語音系統
		case "voc":
			logName = "voc";
			break; // 語音系統
		case "mob":
			logName = "mob";
			break; // 行動支附
		case "tsc":
			logName = "tsc";
			break; // 悠遊卡
		case "ibn":
			logName = "ibn";
			break; // IBON
		default:
			logName = DEFAULT_LOGGER_NAME;
			break; // 一般 BATCH
		}  
      
      logger = (Logger) LogManager.getLogger(logName);
      
      if (pawdEncrypted.trim().length() == 0) {
			String confFile = checkHome + "/conf/ecsParameter.properties";
			confFile = SecurityUtil.verifyPath(confFile);
			Properties props = new Properties();
			try (FileInputStream fis = new FileInputStream(confFile);) {
				props.load(fis);
				fis.close();
			}
			pawdEncrypted = props.getProperty("PWD_ENCRYPTED");
	  }
	  
      if ( pawdEncrypted == null || ! pawdEncrypted.trim().equalsIgnoreCase("none")) {
    	// for mask log data 2020/09/20 Zuwei
    	  logger = new MaskLogger(logger);
	  }

    }

    catch (Exception ex) {
      System.out.println("initialLog4j Error");
    }
    return;
  }

  public void sendNoticeEmail(String abendMessage) {

    /*
     * EmailObject mail = new EmailObject();
     * 
     * mail.mailServer = "smtp.gmail.com"; mail.portNo = "2525"; mail.from = "jack.liao@hpe.com";
     * mail.to = "allen.ho.liao@hpe.com"; mail.subject = "PROGRAM : "+javaProgram
     * +"  "+programVersion+" ABEND !!"; mail.bodyText = abendMessage; mail.attachFile = "";
     * mail.sendEmail();
     */

    return;
  }

  public void log(String actionMsg) {
    showLogMessage("D", "", actionMsg);
    return;
  }

  public void showLogMessage(String actCode, String procMethod, String actionMsg) {
    String stepMesg = "";
    String actionMessage = actionMsg;
    if (actCode.equals("V")) {
      actCode = "I";
      programVersion = actionMessage;
    }
    if (logger == null) {
      initialLog4j();
    }

    if (pid == 0) {
      pid = getPID();
    }
    actionMessage = actionMessage;

    if (initialFlag.equals("Y")) {
      try {
        if (startMillis.length() == 0) {
          getSystemParm("C");
        }
      } catch (Exception ex) {
        expHandle(ex);
      }

      if (javaProgram.length() == 0) {
        javaProgram = super.getClass().getSimpleName();
      }

    }

    initialFlag = "N";

    String msg = CommString.validateLogData(pid + " > " + actionMessage);
    if ("D".equals(actCode) && "Y".equals(debugMode)) {
      logger.debug(msg);
    } else if (actCode.equals("I")) {
      logger.info(msg);
    } else if (actCode.equals("W")) {
      logger.warn(msg);
    } else if (actCode.equals("E")) {
      logger.error(msg);
    }

    return;
  }

  public String convDates(String parmDate, int ki) throws Exception {
    if (parmDate.length() != 8) {
      return parmDate;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Calendar cal = Calendar.getInstance();
    cal.setTime(dateFormat.parse(parmDate));
    cal.add(Calendar.DATE, ki);
    return dateFormat.format(cal.getTime());
  }

  public String convMonths(String parmDate, int ki) throws Exception {
    if (parmDate.length() != 8) {
      return parmDate;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Date current = sdf.parse(parmDate);
    Calendar cal = Calendar.getInstance();
    cal.setTime(current);
    cal.set(Calendar.MONTH, (cal.get(Calendar.MONTH) + ki));
    current = cal.getTime();
    return sdf.format(current);

  }

  public int monthsBetween(String startDate, String endDate) throws Exception {
    if (startDate.length() != 8 || endDate.length() != 8) {
      return 0;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate start = LocalDate.parse(startDate, formatter);
    LocalDate end = LocalDate.parse(endDate, formatter);
    Period diff = Period.between(start, end);
    return (int) diff.toTotalMonths();

  }

  public void dateTime() {
    String dateStr = "", dispStr = "";
    Date currDate = new Date();

    dateStr = form1.format(currDate);
    dispStr = form2.format(currDate);

    sysDate = dateStr.substring(0, 8);
    chinDate = (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + dateStr.substring(4, 8);
    sysTime = dateStr.substring(8, 14);
    millSecond = dateStr.substring(14, 17);
    dispDate = dispStr.substring(0, 10);
    dispTime = dispStr.substring(10, 18);
    juliDate = dateStr.substring(17, 20);
    // dispTime = dispStr.substring(10,18) + ":"+ millSecond;
    return;
  }

  // below add by allen
  // ************************************************************************
  public int executeSqlUtlCmd(String sqlStatement) throws Exception {
    PreparedStatement ps = null;
    int retCode = 0;

    System.out.println("SQL : " + sqlCmd);

    setConnectIndex();

    sqlCmd = sqlStatement;
    displayCheck();
    ps = conn[ci].prepareStatement(sqlCmd);
    setParmData(ps);
    retCode = ps.executeUpdate();
    ps.close();
    dispCount[ti] = dispCount[ti] + retCode;
    dataBase = "";
    sqlCmd = "";

    return retCode;
  } // End of executeSqlUtlCmd
  // ************************************************************************

  public int executeSqlUtlCmdErrmsg(String errorMsg) {
    String sqlErrmc = "";
    String errMsg = errorMsg;
    System.out.println("DB2 SQL Error");
    int intPos1 = errMsg.indexOf("SQLCODE=");
    sqlErrmc = errMsg.substring(intPos1 + 8, errMsg.length());
    int intPos2 = sqlErrmc.indexOf(",");
    int errCode = Integer.valueOf(sqlErrmc.substring(0, intPos2));
    System.out.println("errorcode:" + errCode);

    intPos1 = errMsg.indexOf("SQLSTATE=");
    sqlErrmc = errMsg.substring(intPos1 + 9, errMsg.length());
    intPos2 = sqlErrmc.indexOf(",");
    int stateCode = Integer.valueOf(sqlErrmc.substring(0, intPos2));
    System.out.println("errorstate:" + stateCode);

    intPos1 = errMsg.indexOf("SQLERRMC=");
    sqlErrmc = errMsg.substring(intPos1 + 9, errMsg.length());
    intPos2 = sqlErrmc.indexOf(",");
    sqlErrmc = sqlErrmc.substring(0, intPos2);
    System.out.println("errorMSC:" + sqlErrmc);

    return (0);
  } // End of executeSqlUtlCmd
  // ************************************************************************

  public int utlopenCursor() throws Exception {
    int ft = -1;

    for (int i = 0; i < maxFetch; i++) {
      if (fetchName[i] != null) {
        continue;
      }
      fetchName[i] = daoTable;
      ft = i;
      break;
    }

    if (extendField.length() > 0) {
      fetchExtend = extendField;
    }
    fetchExtend = fetchExtend.toUpperCase();

    fetchName[ft] = daoTable;
    ftIndex = ft;
    getTableIndex("F");
    fetchTi[ft] = ti;
    setConnectIndex();
    if (sqlCmd.length() == 0) {
      if (selectSQL.length() == 0) {
        processFullColumn();
        processSelectColumn();
      } else {
        columnString = selectSQL;
      }
      sqlCmd = "SELECT " + columnString + " FROM " + daoTable + " " + whereStr;
    }

    pf[ti] = conn[ci].prepareStatement(sqlCmd, ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    setParmData(pf[ti]);
    rf[ti] = pf[ti].executeQuery();
    rf[ti].setFetchSize(fetchRow);
    if (columnCnt[ti] == 0) {
      processColumnName(rf[ti]);
    }

    resetParm();
    recCount = 0;
    showLogMessage("I", "openCursor", "FETCH " + daoTable + " STARTED");

    return ft;

  } // End of openCursor
  // ************************************************************************

  public boolean utlfetchTable() throws Exception {
    getTableIndex("F");
    setConnectIndex();
    accessCode = "F";
    extendField = fetchExtend;
    if (retrieveTableData(rf[ti]) == 0) {
      accessCode = "";
      return false;
    }
    accessCode = "";

    return true;
  } // End of fetchTable
  // ************************************************************************

  // --JH:ADD*********************************************************************
  public void dataRemove(int tid, String daoTid, int rr) {
    if (tid <= 0 || rr <= 0)
      return;
    for (int ii = 0; ii < columnName[tid].length; ii++) {
      for (int ll = 0; ll < rr; ll++) {
        outputHash.remove((daoTid + columnName[tid][ii]).toUpperCase() + "#" + ll);
      }
    }
  }

  protected void setTid(int tid) {
    ti = tid;
  }

  public int getTableId() {
    if (daoTable.equalsIgnoreCase("DUAL") || daoTable.trim().length() == 0) {
      return 0;
    }
    Integer chkPnt = accessHash.get(daoTable.trim().toUpperCase());
    if (chkPnt != null) {
      return chkPnt.intValue();
    }
    return -1;
  }

  public int preparedStmtGet() throws Exception {
    if (daoTable.equalsIgnoreCase("DUAL") || daoTable.trim().length() == 0) {
      ti = preparedStmtDual();
      return ti;
    }

    ti = getTableId();
    // if (ti<=0 && sqlCmd.length()==0) {
    // return -1;
    // }

    if (ti <= 0) {
      ti = tbIndex;
      accessHash.put(daoTable.trim().toUpperCase(), ti);
      dispTable[ti] = daoTable;
      tbIndex++;
      if (tbIndex >= tbLimit) {
        showLogMessage("E", "",
            "getTableIndex out of range Exception : " + tbIndex + " : " + daoTable);
        ti = -1;
        return -1;
      }
    }

    if (pf[ti] == null || pf[ti].isClosed()) {
      setConnectIndex();
      pf[ti] = conn[ci].prepareStatement(sqlCmd, ResultSet.TYPE_FORWARD_ONLY,
          ResultSet.CONCUR_READ_ONLY);
      // System.out.println("preStmt: ti="+ti+", Table="+daoTable);
    }
    sqlCmd = "";


    return ti;
  }

  private int preparedStmtDual() throws Exception {
    if (this.sqlCmd.length() == 0) {
      return -1;
    }
    ti = 0;
    if (pf[0] != null && pf[0].isClosed() == false) {
      pf[0].close();
    }

    setConnectIndex();
    pf[0] =
        conn[ci].prepareStatement(sqlCmd, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    sqlCmd = "";

    return 0;
  }

  public static String byteToHexString(byte[] inBytes) {

    StringBuilder sb = new StringBuilder(inBytes.length * 2);
    for (byte b : inBytes) {
      sb.append(String.format("%02x", b));
    }

    return sb.toString().toUpperCase();
  }

  public void showMemory(int cnt) throws Exception {
    long total = (long) (Runtime.getRuntime().totalMemory() / (1024 * 1024));
    long free = (long) (Runtime.getRuntime().freeMemory() / (1024 * 1024));
    long use = total - free;
    if (total >= 4500) {
      showLogMessage("I", "",
          "LOAD [ "
              + daoTable.toUpperCase()
              + " COUNT "
              + cnt
              + " ] MEMORY TOTAL : "
              + total
              + " FREE : "
              + free
              + " USE : "
              + use);
    }
    return;
  }
  
    /**
     * get the system date, the business date, or the first argument
     * and then convert it into the format provided  
     * @param progArgs1 程式第一個參數
     * @param DateType 日期格式。例如(M:取前六碼，即YYYYMM；其餘不變動日期格式)
     * @return
     * @throws Exception
     */
	public String getProgDate(String progArgs1, String DateType) throws Exception {
		String searchDate = "";
		if (progArgs1 != null && progArgs1.trim().length() > 0) {
			if (progArgs1.toUpperCase(Locale.TAIWAN).equals("SYSDAY")) {
				searchDate = sysDate;
				showLogMessage("I", "", "[AccessDAO.getProgDate] 判斷參數值為系統日期[" + searchDate + "]");
			} else {
				searchDate = progArgs1;
				showLogMessage("I", "", "[AccessDAO.getProgDate] 判斷參數值為指定日期[" + searchDate + "]");
			}
		} else {
			searchDate = getBusiDate();
			showLogMessage("I", "", "[AccessDAO.getProgDate] 判斷參數為空白取營業日[" + searchDate + "]");
		}
		
		// 如果DateType為M，則取searchDate前6碼，即YYYYMM；DateType若為其他值視同D來處理。
		if (DateType.equals("M")) {
			if (searchDate == null || searchDate.trim().length() < 6) {
				throw new Exception(String.format("[AccessDAO.getProgDate] DateType為M無法取前6碼，因為參數日期[%s]為NULL或長度小於6", searchDate));
			}
			searchDate = searchDate.substring(0, 6);
			showLogMessage("I", "", "[AccessDAO.getProgDate] DateType為M取前6碼[" + searchDate + "]");
		}
		return searchDate;
	}
	
	static public String getEcsAcdpPath() {
		String path = new CommCrd().getECSHOME() + "/conf/ecsAcdp.properties";
		return SecurityUtil.verifyPath(path);
	}


} // End of class AccessDAO
