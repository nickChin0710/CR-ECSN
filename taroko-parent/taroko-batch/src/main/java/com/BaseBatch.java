package com;
/**批次程式母版二
 * 109/12/22   Justin zz chg name
*  109/07/22  V0.00.03    Zuwei     coding standard, rename field method                   *
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
 * 2020-0430	JH		print_sql()
 * 2020-0203	JH		dsp_procRow()
 * 2019-1216   JH    ++sqlQuery(ii,[])
 * 2019-0722   JH    modify
 * 18-1228:    JH    ++_errMsg
 * 2018-1207:  JH    file_Backup()
 * 2018-1030:	JH		ok_exit(),mainProcess()
 *   110-01-07   shiyuqi       修改无意义命名
 * 2021-11-30  Alex   isAppActive2 fix
 * 111/01/19   Justin     fix Denial of Service: Format String
 * 2023-0815  JH    setModxxx(): hModPgm不轉小寫
 * 2023-0829   JH    ecsModSeq()
 * */

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseBatch extends AccessDAO {

  public boolean runCheck = true;
  protected int rc = 0;
  private String errMsg = "";
  public boolean debug = false;
  public int totalCnt = 0;

  private int iiParmCnt = 0;
  private int[] tableRows = new int[300];
  public int sqlNrow = 0;
  public String sqlErrtext = "";
  public String daoTid = "";
  // public int selectOnly = 0;
  protected boolean sqlDuplRecord = false;

  public String hBusiDate = "";
  String hOnlineDate = "";
  public String hCallBatchSeqno = ""; // -callBatch-
  public String hCallErrorCode = "";
  public String hCallErrorDesc = "";
  public String hModUser = "system", hModPgm = "";

  protected com.CommString commString = new com.CommString();
  protected com.CommSqlStr commSqlStr = new com.CommSqlStr();
  protected com.CommDate commDate = new com.CommDate();
  protected String newLine = "\n";

  private String zzDaoTable = "";
  private String[] tiSql = new String[300];
  private java.sql.Savepoint savepoint = null;
  private boolean dspSql = false;
  private int hFetchRow = -1;

  public int mainProcess(String[] args) {
    dateTime();
    try {
      if (debug) {
        this.setConsoleMode("Y");
      }

      if (args == null)
        args = new String[0];

      // -程式執行中-
      if (runCheck && debug == false) {
        com.CommFunction comm = new com.CommFunction();
        if (comm.isAppActive2(this.getClass().getName())) {
//        if (comm.isAppActive2(this.getClass().getSimpleName())) {
          printf("[" + this.getClass().getSimpleName() + "]程式執行中...");
          exitProgram(1); // throw ex
        }
      }

      dataProcess(args);
      finalProcess();
    } catch (Exception ex) {
      if (strIN("<<<ok-exit>>>", ex.getMessage())) {
        return rc;
      }

      // -error--
      errmsg("Error: " + ex.getMessage());
      if (getConnection() != null) {
        this.rollbackDataBase();
      }
      expMethod = "mainProcess";
      expHandle(ex);
    }

    return rc;
  }

  // public abstract void sql_Initial() throws Exception;
  // protected void preProcess(String[] args) throws Exception{}
  protected abstract void dataProcess(String[] args) throws Exception;

  protected boolean dspProcRow(int alRow) {
    if (alRow <= 0)
      return false;
    if ((totalCnt % alRow) == 0) {
      printf("-->Process row=[" + totalCnt + "]");
      return true;
    }
    return false;
  }

  protected void dbConnect() throws Exception {
    if (!connectDataBase())
      errExit(1);
    printf("-->connect DB: " + getDBalias()[0]);
    selectPtrBusinday();
    setModXxx();
  }

  protected String ecsModSeq(int aiLen) throws Exception {
    //SELECT RIGHT('0000000000'||nextval FOR ecs_modseq,10) AS mod_seqno10 FROM dual
    //sqlCmd = "select nextval for ecs_modseq as mod_seqno" + " from " + commSqlStr.sqlDual;
    sqlCmd ="select '00000000000000000000'||(nextval FOR ecs_modseq) as mod_seqno"
            +" from "+commSqlStr.sqlDual;
    sqlSelect();
    if (sqlNrow <= 0) {
      errmsg("select ecs_modseq error");
      errExit();
    }
    String modSeqno = colSs("mod_seqno");
    if (aiLen <= modSeqno.length())
      return commString.right(modSeqno,aiLen);

    return commString.lpad(modSeqno, aiLen, "0");
  }

  @Override
  public int openInputText(String aFile) {
    String lsFile = java.text.Normalizer.normalize(aFile, java.text.Normalizer.Form.NFKD);
    return openInputText(lsFile, "MS950");
  }

  @Override
  public int openOutputText(String parmFile) throws Exception {
    String lsFile = java.text.Normalizer.normalize(parmFile, java.text.Normalizer.Form.NFKD);
    return openOutputText(lsFile, "MS950");
  }

  public boolean fileDelete(String aFile) {
    com.CommFile commfile = new com.CommFile();
    return commfile.fileDelete(aFile);
  }

  public boolean fileBackup(String aFile, String dirFrom, String dirTo) {
    String lsFile1 = getEcsHome() + "/" + dirFrom + "/" + aFile;
    String lsFile2 = getEcsHome() + "/" + dirTo + "/" + aFile + "_" + commString.right(sysDate, 4);
    com.CommFile commfile = new com.CommFile();
    if (commfile.fileCopy(lsFile1, lsFile2)) {
      return commfile.fileDelete(lsFile1);
    }

    return false;
  }

  public String getEcsHome() {
//    return System.getenv("TCB_HOME");
//     printf("proj_home=[PROJ_HOME]");
    return System.getenv("PROJ_HOME");
  }

  protected void systemExit() {
    // -0.OK, ; 1.error,shell不執行; 7.error,shell繼續執行-
    if (rc == 0)
      exceptExit = 0;
    else if (rc != 0)
      exceptExit = (rc > 0 ? rc : (0 - rc));

    systemExit(exceptExit);
  }

  protected void systemExit(int aiExit) {
    if (aiExit != 0)
      printf("-->system exit code[" + aiExit + "]");
    this.programEnd(aiExit);
  }

  protected void dspProgram(String aPgm) {
    this.javaProgram = this.getClass().getSimpleName();
    printf(commString.repeat("-", 80));
    printf(javaProgram + " " + aPgm + ", debug=" + debug);
  }

  public void setBusiDate(String param) {
    if (param.trim().length() == 8) {
      hBusiDate = param.trim();
      printf("-->參數: 營業日=[" + hBusiDate + "]");
    }
  }

  public void callBatchSeqno(String param) {
    if (param.trim().length() == 20)
      hCallBatchSeqno = param.trim();
  }

  public boolean iscallBatch() {
    return (hCallBatchSeqno.length() == 20);
  }

  // public void callBatch(int a_strend) {
  // callBatch(0,0,a_strend);
  // }
  public void callBatch(int aType, int aVal2, int aStrend) {
    // Parm:1[0.程式,1.shell], 2[??], 3[0.start,1.end]
    try {
      if (iscallBatch() == false)
        return;
      if (aStrend == 0) {
        hModUser = callbatchUser();
        callBatchBeg();
      } else
        callBatchEnd();
    } catch (Exception ex2) {
      ;
    }
  }

  void callBatchBeg() {
    try {
      sqlCmd = "update ptr_callbatch set"
          + " execute_date_s ="
          + commSqlStr.sysYYmd
          + ", execute_time_s ="
          + commSqlStr.sysTime
          + " where batch_seqno =?";
      sqlExec(new Object[] {this.hCallBatchSeqno});
      sqlCommit();
    } catch (Exception ex2) {
      ;
    }
  }

  void callBatchEnd() {
    try {
      sqlCmd = "update ptr_callbatch set"
          + " execute_date_e ="
          + commSqlStr.sysYYmd
          + ", execute_time_e ="
          + commSqlStr.sysTime
          + ", error_code =?"
          + ", error_desc =?"
          + " where batch_seqno =?";
      sqlExec(new Object[] {nvl(hCallErrorCode, "0000"), hCallErrorDesc, this.hCallBatchSeqno});
      sqlCommit();
    } catch (Exception ex2) {
      ;
    }

  }

  public String callbatchUser() {
    if (iscallBatch() == false)
      return "system";
    sqlCmd = "select user_id from ptr_callbatch" + " where batch_seqno =?";

    try {
      ppp(1, hCallBatchSeqno);
      sqlSelect();
      if (sqlNrow > 0) {
        return colSs("user_id");
      }
    } catch (Exception e) {
      return "system";
    }
    return "system";
  }

  public void setModXxx() {
    if (empty(hModUser)) {
      hModUser = "system";
    }
    if (empty(hModPgm)) {
      hModPgm = this.javaProgram;
    }
    if (empty(hModPgm)) {
      hModPgm = this.getClass().getSimpleName();
    }
    hModPgm = hModPgm.trim();  //.toLowerCase();
  }

  // --AAA------------------------------
  public int gettid(String aDtab) {
    return getTableId(aDtab);
  }

  public int getTableId(String aDaoT) {
    this.daoTable = aDaoT;
    return getTableId();
  }

  // -create PS.index-
  public int ppStmtCrt(String aTable, String sql1) throws Exception {
    if (noEmpty(aTable)) {
      daoTable = aTable;
    }
    if (noEmpty(sql1))
      sqlCmd = sql1;

    int tid = ppStmtCrt();
    if (noEmpty(daoTable) && tid <= 0) {
      errmsg("cannot Create PS.index: " + aTable + "; sql=" + tiSql[0]);
      errExit();
    }
    return tid;
  }

  // public int preparedStmt_crt(String sql1) throws Exception {
  // sqlCmd = sql1;
  // return ppStmt_crt();
  // }
  //
  // public int ppStmt_crt(String sql1) throws Exception {
  // sqlCmd = sql1;
  // return ppStmt_crt();
  // }

  public int ppStmtCrt() throws Exception {
    int ti = -1;
    if (empty(daoTable) == false) {
      ti = getTableId();
      if (ti >= 0)
        return ti;
    }
    tiSql[0] = sqlCmd;
    if (empty(sqlCmd)) {
      ti = -1;
    } else {
      ti = preparedStmtGet();
    }
    if (ti < 0) {
      errmsg("preparedStmt_crt.tid error, daoTable=" + daoTable);
      errExit();
    }
    zzDaoTable = daoTable;
    daoTable = "";

    tiSql[ti] = tiSql[0];
    return ti;
  }

  // public int preparedStmt_crt() throws Exception {
  // return ppStmt_crt();
  // }

  public int sqlExec(int tid, Object[] param) throws Exception {
    sqlNrow = 0;
    sqlErrtext = "";
    sqlDuplRecord = false;
    setTid(tid);

    if (dspSql) {
      dddSql(tid, param);
    }

    try {
      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          pf[tid].setObject(ii + 1, param[ii]);
        }
        clearParm();
      } else {
        this.setParmData(pf[tid]);
      }
      sqlNrow = pf[tid].executeUpdate();
    } catch (SQLException ex2) {
      sqlNrow = -1;
      sqlRc = ex2.getErrorCode();
      if (ex2.getErrorCode() == 1 || ex2.getErrorCode() == -803) {
        sqlNrow = 0;
        dupRecord = "Y";
        sqlDuplRecord = true;
        return 0;
      } else if (ex2.getErrorCode() == -302) {
        for (int i = 0; i < columnCnt[tid]; i++) {
          String dispData = getValue(extendField + columnName[tid][i]);
          showLogMessage("E", "",
              columnName[tid][i] + " --- [" + dispData + "] LEN = " + dispData.length());
        }
        throw new Exception(ex2);
      } else {
        throw new Exception(ex2);
      }
    } catch (Exception ex) {
      sqlNrow = -1;
      sqlErrtext = "sqlExec: " + ex.getMessage();
      printf(sqlErrtext);
      throw new Exception(ex);
    } finally {
      if (tid == 0) {
        try {
          pf[0].close();
        } catch (Exception e) {
        }
      }
    }

    daoTable = "";
    return 1;
  }

  public int sqlExec(Object[] param) throws Exception {
    return sqlExec("", param);
  }

  public int sqlExec(String sql1, Object[] param) throws Exception {
    // printf(">>>>>"+sql1,param);
    if (!empty(sql1)) {
      sqlCmd = sql1;
    }
    tiSql[0] =sqlCmd;
    int tid = this.preparedStmtGet();

    return sqlExec(tid, param);
  }

  public int sqlExec(int tid) throws Exception {
    return sqlExec(tid, null);
    // sql_nrow = 0;
    // sql_errtext = "";
    // this.setParmData(pf[tid]);
    // sql_nrow = pf[tid].executeUpdate();
    // if (tid==0)
    // pf[0].close();
  }

  public int sqlExec(String sql1) throws Exception {
    sqlNrow = 0;
    sqlErrtext = "";
    if (empty(sql1) == false) {
      sqlCmd = sql1;
    }

    tiSql[0] =sqlCmd;
    int tid = this.preparedStmtGet();

    this.setParmData(pf[tid]);
    sqlNrow = pf[tid].executeUpdate();
    if (tid == 0) {
      pf[0].close();
    }

    // try {
    // this.setParmData(pf[tid]);
    // sql_nrow = pf[tid].executeUpdate();
    // }
    // catch (Exception e) {
    // sql_nrow = -1;
    // sql_errtext = "sqlExec[sql1]: " + e.getMessage();
    // expMethod = "sqlExec";
    // printf(sql_errtext);
    // return 0;
    // }
    // finally {
    // if (tid == 0) {
    // try {
    // pf[0].close();
    // }
    // catch (Exception e) {
    // }
    // }
    // }
    daoTable = "";
    return 1;
  }

  // protected String modXxx_sql() {
  // return " mod_user, mod_time, mod_pgm, mod_seqno ";
  // }
  protected String modxxxInsert() {
    return modxxxInsert("");
  }

  protected String modxxxInsert(String param) {
    return param + " '" + hModUser + "', sysdate, '" + hModPgm + "', 1 ";
  }

  protected String modxxxSet(String param) {
    return param
        + " mod_user ='"
        + hModUser
        + "', mod_time =sysdate, mod_pgm ='"
        + hModPgm
        + "', mod_seqno =nvl(mod_seqno,0)+1 ";
  }

  protected String modxxxSet() {
    return modxxxSet("");
  }

  public void sqlSelect(String sql1) throws Exception {
    if (empty(sql1) == false)
      sqlCmd = sql1;
    sqlSelect();
  }

  public void sqlSelect(String sql1, int aiFetch) throws Exception {
	hFetchRow = aiFetch;
    if (empty(sql1) == false)
      sqlCmd = sql1;
    sqlSelect();
  }

  public void sqlSelect(String sql1, Object[] param) throws Exception {
    if (empty(sql1) == false) {
      sqlCmd = sql1;
    }
    tiSql[0] =sqlCmd;
    int tid = this.preparedStmtGet();

    sqlSelect(tid, param);
  }

  public void sqlSelect() throws Exception {

    tiSql[0] =sqlCmd;
    int tid = this.preparedStmtGet();

    sqlSelect(tid, null);
  }

  public void sqlSelect(int tid) throws Exception {
    sqlSelect(tid, null);
  }

  public void sqlSelect(int tid, int aiFetchRow) throws Exception {
	hFetchRow = aiFetchRow;
    sqlSelect(tid, null);
  }

  public void sqlSelect(int tid, Object[] param) throws Exception {
    sqlNrow = 0;
    setTid(tid);
    ResultSet rs = null;
    // extendField =daoTid;

    // -initial-
    if (tid > 0) {
      dataRemove(tid, daoTid, tableRows[tid]);
    }

    int rr = -1;
    try {
      if (param == null) {
        this.setParmData(pf[tid]);
      } else {
        for (int ii = 0; ii < param.length; ii++) {
          pf[tid].setObject(ii + 1, param[ii]);
        }
        clearParm();
      }
      if (dspSql) {
        dddSql(tid, param);
      }
      rs = pf[tid].executeQuery();

      // -get column-Name-
      if (tid == 0 || this.columnCnt[tid] <= 0) {
        this.processColumnName(rs);
      }
      // -set Value-
      while (rs.next()) {
        rr++;
        sqlNrow = rr + 1;
        // -setdata-
        for (int ii = 0; ii < columnCnt[tid]; ii++) {
          String col = columnName[tid][ii];
          colSet(rr, daoTid + col, rs.getObject(col));
          // ddd(""+ii+".col="+daoTid + columnName[tid][ii]+",
          // data="+rs.getObject(col));
        }
        // -select row Only-
        if (hFetchRow > 0 && sqlNrow >= hFetchRow) {
          break;
        }
      }
      rs.close();
      if (tid == 0) {
        pf[tid].close();
      }
      tableRows[tid] = sqlNrow;
      hFetchRow = -1;
    }
    // catch ( SQLException ex2 ) {
    // _fetch_row =-1;
    // sqlRc = ex2.getErrorCode();
    // if ( ex2.getErrorCode() == -302 ) {
    // printf("sqlSelect: param.len too large; err="+ex2.getMessage());
    // }
    // else throw new Exception(ex2);
    // }
    catch (Exception ex) {
      hFetchRow = -1;
      printSql(tid, param);

      if (sqlErrHandle("sqlSelect(): " + ex.getMessage()) == false) {
        sqlNrow = -1;
        throw new Exception(ex);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (Exception ex2) {
        ;
      }
    }

    daoTid = "";
    daoTable = "";
    iiParmCnt = 0;
  }

  public double getNumber(int tid, Object... param) throws Exception {
    double lmRc = 0;
    sqlNrow = 0;
    setTid(tid);
    ResultSet rs = null;
    // extendField =daoTid;

    // -initial-
    int rr = -1;
    try {
      if (param == null) {
        this.setParmData(pf[tid]);
      } else {
        for (int ii = 0; ii < param.length; ii++) {
          pf[tid].setObject(ii + 1, param[ii]);
        }
        clearParm();
      }
      rs = pf[tid].executeQuery();

      // -get column-Name-
      if (tid == 0 || this.columnCnt[tid] <= 0) {
        this.processColumnName(rs);
      }
      // -set Value-
      if (rs.next()) {
        lmRc = rs.getDouble(1);
      }

      rs.close();
      if (tid == 0) {
        pf[tid].close();
      }
      tableRows[tid] = sqlNrow;
      hFetchRow = -1;
    } catch (Exception ex) {
      hFetchRow = -1;
      if (sqlErrHandle("getNumber(): " + ex.getMessage()) == false) {
        sqlNrow = -1;
        throw new Exception(ex);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (Exception ex2) {
        ;
      }
    }

    daoTid = "";
    daoTable = "";
    iiParmCnt = 0;

    return lmRc;
  }

  public void sqlQuery(com.DataSet ds, int tid, Object[] param) throws Exception {
    ResultSet rs = null;

    int rr = -1;
    try {
      if (param == null) {
        this.setParmData(pf[tid]);
      } else {
        for (int ii = 0; ii < param.length; ii++) {
          pf[tid].setObject(ii + 1, param[ii]);
        }
        clearParm();
      }
      if (dspSql) {
        dddSql(tid, param);
      }
      rs = pf[tid].executeQuery();

      // -get column-Name-
      if (tid == 0 || this.columnCnt[tid] <= 0) {
        this.processColumnName(rs);
      }
      // -set Value-
      while (rs.next()) {
        rr++;
        sqlNrow = rr + 1;
        // -setdata-
        for (int ii = 0; ii < columnCnt[tid]; ii++) {
          String col = columnName[tid][ii];
          ds.colSet(rr, daoTid + col, rs.getObject(col));
        }
      }
      rs.close();
      if (tid == 0) {
        pf[tid].close();
      }
      tableRows[tid] = sqlNrow;
      hFetchRow = -1;
    }
    // catch ( SQLException ex2 ) {
    // _fetch_row =-1;
    // sqlRc = ex2.getErrorCode();
    // if ( ex2.getErrorCode() == -302 ) {
    // printf("sqlSelect: param.len too large; err="+ex2.getMessage());
    // }
    // else throw new Exception(ex2);
    // }
    catch (Exception ex) {
      hFetchRow = -1;
      printSql(tid, param);

      if (sqlErrHandle("sqlQuery(): " + ex.getMessage()) == false) {
        sqlNrow = -1;
        throw new Exception(ex);
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (Exception ex2) {
        ;
      }
    }

    ds.listCnt = sqlNrow;
    return;
  }

  public void sqlQuery(com.DataSet ds, String sql1, Object[] param) throws Exception {
    sqlNrow = 0;
    ds.dataClear();

    if (empty(sql1) == false) {
      sqlCmd = sql1;
    }
    sqlNrow = 0;
    int tid = ppStmtCrt(daoTable, "");
    sqlQuery(ds, tid, param);
    return;
  }

  // public List<Map<String, String>> sqlQuery(String sql1, Object[] param) throws Exception {
  // sql_nrow = 0;
  //
  // // java.sql.ResultSet rs = null;
  // ArrayList<Map<String, String>> colList = new ArrayList<>();
  //
  // if (empty(sql1) == false) {
  // sqlCmd = sql1;
  // }
  //
  // int tid = this.preparedStmt_get();
  // sqlSelect(tid, param);
  // if (sql_nrow <= 0)
  // return colList;
  //
  // for (int ll = 0; ll < sql_nrow; ll++) {
  // Map<String, String> map = new HashMap<>();
  // for (int ii = 0; ii < columnCnt[tid]; ii++) {
  // map.put(this.columnName[tid][ii], col_ss(ll, this.columnName[tid][ii]));
  // }
  // colList.add(map);
  // }
  //
  // return colList;
  // }

  boolean sqlErrHandle(String aMsg) {
    if (aMsg.toLowerCase().indexOf("sqlcode=-302") >= 0) {
      sqlNrow = 0;
      this.sqlErrtext = "(value too large[302]); " + aMsg;
      ddd(">>>sqlErr:" + sqlErrtext);
      return true;
    }

    return false;
  }

  public void setSavePoint() throws SQLException {
    savepoint = getSavePoint("point1");
  }

  public void setSavePoint(java.sql.Savepoint spoint) {
    savepoint = spoint;
  }

  protected java.sql.Savepoint getSavePoint(String aPoint) throws SQLException {
    return this.getConnection().setSavepoint(aPoint);
  }

  public void sqlCommit(int aCommit) throws Exception {
    if (aCommit == 1)
      this.sqlCommit();
    else if (aCommit == -1)
      sqlRollback();
  }

  public void sqlCommit() throws Exception {
    // printf("dataBase COMMIT");
    this.getConnection().commit();
  }

  public void sqlRollback(java.sql.Savepoint sPoint) throws SQLException {
    this.getConnection().rollback(sPoint);
  }

  public void sqlRollback() throws SQLException {
    printf("dataBase ROLLBACK");
    if (savepoint == null)
      this.getConnection().rollback();
    else {
      this.getConnection().rollback(savepoint);
      // getConnection().releaseSavepoint(_savepoint);
      // _savepoint =null;
    }
  }

  // --VVV-------------------------------
  // public void col_move(int rr, String col, String to_id) {
  // col_move(rr,col,daoTid,to_id);
  // }
  public void colMove(int rr, String col, String fromId, String toId) throws Exception {
    colSet(toId + col, colSs(rr, fromId + col));
  }

  public String colNvl(String col, String param) throws Exception {
    return colNvl(0, col, param);
  }

  public String colNvl(int int1, String col, String param) throws Exception {
    String col1 = colSs(int1, col);
    if (empty(col1))
      return param;
    return col1;
  }

  public boolean colEmpty(String col) {
    return empty(colSs(col));
  }

  public String colSs(String col) {
    return colSs(0, col);
  }

  public String colSs(int rr, String col) {
    try {
      return getValue2(col.trim().toUpperCase(), rr);
    } catch (Exception ex) {
      printf(col + "-" + rr + ": " + ex.getMessage());
    }
    return "";
  }

  public String colNoTrim(int int1, String col) {
    try {
      noTrim = "Y";
      String col1 = getValue(col.trim().toUpperCase(), int1);
      noTrim = "";
      return col1;
    } catch (Exception ex) {
      printf(col + "-" + int1 + ": " + ex.getMessage());
    }
    return "";
  }

  public int colInt(String col) {
    return colInt(0, col);
  }

  public int colInt(int rr, String col) {
    try {
      return getValueInt(col.trim().toUpperCase(), rr);
    } catch (Exception ex) {
      // printf("Except.col_int: %s-%s, " + ex.getMessage(), col, rr);
    }
    return 0;
  }

  public double colNum(String col) {
    return colNum(0, col);
  }

  public double colNum(int rr, String col) {
    return getValueDouble(col.trim().toUpperCase(), rr);
  }

  public void colAdd(int rr, String col, double num1) {
    double num = colNum(rr, col) + num1;
    colSet(rr, col, "" + num);
  }

  public void colAdd(int rr, String col, int int1) throws Exception {
    double num = colInt(rr, col) + int1;
    colSet(rr, col, "" + num);
  }

  public void colAdd(String col, int int1) throws Exception {
    colAdd(0, col, int1);
  }

  public void colAdd(String col, double num1) {
    colAdd(0, col, num1);
  }

  public void colSet(int rr, String col, Object obj1) {
    if (obj1 == null) {
      setValue2(col.toUpperCase(), "", rr);
    } else {
      setValue2(col.toUpperCase(), obj1.toString(), rr);
    }
  }

  public void colSet(String col, int int1) {
    colSet(0, col, int1);
  }

  public void colSet(int rr, String col, int int1) {
    if (Double.isNaN(int1)) {
      setValue2(col.trim().toUpperCase(), "0", rr);
    } else
      setValue2(col.trim().toUpperCase(), "" + int1, rr);
  }

  public void colSet(String col, double num1) {
    colSet(0, col, num1);
  }

  public void colSet(int rr, String col, double num1) {
    if (Double.isNaN(num1)) {
      setValue2(col.trim().toUpperCase(), "0", rr);
    } else
      setValue2(col.trim().toUpperCase(), "" + num1, rr);
  }

  public void colSet(String col, String param) {
    colSet(0, col, param);
  }

  public void colSet(int rr, String col, String param) {
    setValue2(col.trim().toUpperCase(), param, rr);
  }

  // ----------------------------------------------------
  public boolean colEq(int rr, String col, String param) {
    return colSs(rr, col).equals(param);
  }

  public boolean colEq(String col, String param) {
    return eq(colSs(col), param);
  }

  protected boolean colIn(String col, String param) {
    return (param.indexOf(colSs(col)) >= 0);
  }

  public int colComp(String col, String param) {
    return colSs(col).compareTo(param);
  }

  public String nvl(String param, String param1) {
    if (empty(param) == false)
      return param;
    return param1;
  }

  public boolean noEmpty(String param) {
    return !(param == null || param.trim().length() == 0);
  }

  public boolean empty(String param) {
    return (param == null || param.trim().length() == 0);
  }

  public boolean eqIgno(String param1, String param2) {
    return param1.equalsIgnoreCase(param2);
  }

  public boolean eq(String param, String param1) {
    return param.equalsIgnoreCase(param1);
    // return s1.equals(s2);
  }

  public boolean eqCase(String param1, String param2) {
    return param1.equals(param2);
  }

  protected boolean strIN(String param1, String param2) {
    if (empty(param1) || empty(param2))
      return false;
    return (param2.indexOf(param1) >= 0);
  }

  protected int ss2int(String param1) {
    return (int) ss2Num(param1);
  }

  protected double ss2Num(String param1) {
    try {
      if (empty(param1)) {
        return 0;
      }
      return Double.parseDouble(param1);
    } catch (Exception ex) {
      return 0;
    }
  }

  protected int ssComp(String param1, String param2) {
    return commString.ssCompIngo(param1, param2);
  }

  protected void printf(String param1, Object... objs) {
    if (empty(param1) && objs.length == 0)
      return;

    String decs = String.format(param1, objs);
    // String ss=s1;
    // for(Object oo:objs) {
    // if (ss.indexOf("%s")>=0)
    // ss =ss.replaceFirst("%s",oo.toString());
    // else ss +=", "+oo.toString();
    // }

    this.hCallErrorDesc = decs;
    showLogMessage("I", "", decs);
    if (debug)
      System.out.println(decs);
  }
  
  protected void printf(String param1) {
	    if (empty(param1))
	      return;

	    this.hCallErrorDesc = param1;
	    showLogMessage("I", "", param1);
	    if (debug)
	      System.out.println(param1);
  }
  
  private void ddd(String param1, String str, Object[] objs) {
    if (!debug)
      return;

    // String ss = commString.logParm(s1, (String[])objs);
    // if (debug && (objs==null || objs.length==0)) {
    // System.out.println(s1);
    // return;
    // }

    // for (int ii=0; ii<objs.length; ii++) {
    // if (objs[ii]==null)
    // objs[ii] ="NULL";
    // }
    String replaceString = param1;
    if (objs != null && objs.length > 0) {
      replaceString = param1.replaceAll("\\?", "'%s'");
      for (int ii = 0; ii < objs.length; ii++) {
        try {
          replaceString = replaceString.replaceFirst("%s", objs[ii].toString());
        } catch (Exception ex) {
          if (objs[ii] == null)
            replaceString = replaceString.replaceFirst("'%s'", "null");
          else
            replaceString = replaceString.replaceFirst("%s", "<" + ii + ">") + ";" + objs[ii].toString();
        }
      }
    }

    if (debug) {
      System.out.println(replaceString);
    } else {
      showLogMessage("I", "DDD", replaceString);
    }

    // if (No_empty(cc) && s1.indexOf(cc) >=0) {
    // if (eq(cc,"?")) {
    // String cc2=(cc.equals("?") ? "\\?" :cc);
    // for (Object o1 : objs) {
    // if (o1==null) o1="NULL";
    // if (ss.indexOf(cc) <0)
    // ss +="; ["+o1.toString()+"]";
    // else {
    // ss = ss.replaceFirst(cc2, "'"+o1.toString()+"'");
    // }
    // }
    // }
    // else {
    // for (Object o1 : objs) {
    // if (o1==null) o1="NULL";
    // if (ss.indexOf(cc) <0)
    // ss +="; ["+o1.toString()+"]";
    // else {
    // ss = ss.replaceFirst(cc, o1.toString());
    // }
    // }
    // }
    // System.out.println(ss);
    // return;
    // }
    //
    // //-No-param---
    // for(Object oo:objs) {
    // if (oo==null) oo="NULL";
    // ss +="; "+oo.toString();
    // }
    // System.out.println(ss);
  }

  protected void ddd(String param, Object... objs) {
    if (debug == false)
      return;

    ddd(param, "%s", objs);
    // //String ss = commString.logParm(s1, (String[])objs);
    // if (objs==null || objs.length==0) {
    // System.out.println(s1);
    // return;
    // }
    //
    // String ss=s1;
    // if (s1.indexOf("?") >=0) {
    // for (Object o1 : objs) {
    // if (o1==null) o1="NULL";
    // if (ss.indexOf("?") <0)
    // ss +="; ["+o1.toString()+"]";
    // else ss = ss.replaceFirst("\\?", "'"+o1+"'");
    // }
    // }
    // else if (s1.indexOf("%s") >=0) {
    // for(Object oo:objs) {
    // if (ss.indexOf("%s") <0)
    // ss +=", "+oo.toString();
    // else ss =ss.replaceFirst("%s",oo.toString());
    // }
    // }
    //
    // System.out.println(ss);
  }

  protected void dddSql(boolean aTrue) {
    dspSql = aTrue;
  }

  protected void dddSql() {
    if (!debug)
      return;

    if (noEmpty(sqlCmd))
      ddd(sqlCmd, "?", getSqlParm());
    else {
      String lsSql = "select " + selectSQL + " from " + daoTable + " " + whereStr;
      ddd(lsSql, "?", getSqlParm());
    }
    dspSql = (debug ? dspSql : false);
  }

  protected void printSql(int tid, Object[] param) {
    String tid1 = tiSql[tid];
    String replaceString = tid1;
    Object[] objs = param;
    if (objs == null) {
      objs = this.getSqlParm();
    }

    if (objs != null && objs.length > 0) {
      replaceString = tid1.replaceAll("\\?", "'%s'");
      for (int ii = 0; ii < objs.length; ii++) {
        try {
          replaceString = replaceString.replaceFirst("%s", objs[ii].toString());
        } catch (Exception ex) {
          if (objs[ii] == null)
            replaceString = replaceString.replaceFirst("'%s'", "null");
          else
            replaceString = replaceString.replaceFirst("%s", "<" + ii + ">") + ";" + objs[ii].toString();
        }
      }
    }
    printf(replaceString);
    // showLogMessage("I", "DDD", ss);
  }

  protected void dddSql(int tid, Object[] pps) {
    if (!debug)
      return;

    ddd(tiSql[tid], "?", pps);
    dspSql = (debug ? dspSql : false);
  }

  protected void dddSql(int tid) {
    if (!debug)
      return;

    ddd(tiSql[tid], "?", this.getSqlParm());
    dspSql = (debug ? dspSql : false);
  }

  protected void sqlerr(String param1, Object... objs) {
    setExceptExit(1);
    rc = -1;
    // String ss = commString.logParm(s1, (String[])objs);

    String des = String.format(param1, objs);
    // for(Object oo:objs) {
    // if (ss.indexOf("%s") <0)
    // ss +=", "+oo.toString();
    // else ss =ss.replaceFirst("%s",oo.toString());
    // }

    this.hCallErrorDesc = des;
    showLogMessage("E", "", "sqlCode=" + sqlRc + ";" + des);
    if (debug) {
      System.out.println("sqlCode=" + sqlRc + ";" + des);
    }
  }

  protected void errmsg(String param, Object... objs) {
    setExceptExit(1);
    rc = -1;
    String msg = String.format(param, objs);
    // for(Object oo:objs) {
    // if (ss.indexOf("%s") <0)
    // ss +=", "+oo.toString();
    // else ss =ss.replaceFirst("%s",oo.toString());
    // }

    errMsg = msg;
    this.hCallErrorDesc = msg;
    showLogMessage("E", "", msg);
    if (debug) {
      System.out.println(msg);
    }
  }

  // private String logParm(String s1,Object[] obj) {
  // String ss = s1;
  //
  // if (s1.indexOf("?")>=0) {
  // for (Object o1 : obj) {
  // if (o1==null) o1="NULL";
  // ss = ss.replaceFirst("\\?", "'"+o1.toString()+"'");
  // }
  // return ss;
  // }
  //
  // for (Object o1:obj) {
  // int pp =ss.indexOf("%s");
  // if (pp<0) break;
  // //String aa =ss.substring(pp+2);
  // if (o1==null)
  // o1 ="NULL";
  // ss =ss.substring(0,pp)+o1.toString()+ss.substring(pp+2);
  // }
  // return ss;
  // }
  protected double numRound(double num1, int po1) {
    return new BigDecimal(num1).setScale(po1, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  protected void ppp(int col1, String param) {
    iiParmCnt = col1;

    try {
      this.setString(col1, param);
    } catch (Exception ex) {
      printf("ppp.setString error; " + col1 + "," + param);
    }
  }

  protected void ppp(String cnt) {
    iiParmCnt++;
    try {
      this.setString(iiParmCnt, cnt);
    } catch (Exception ex) {
      printf("ppp.setString error; " + iiParmCnt + "," + cnt);
    }
  }

  public void ppRowId(String cnt) {
    iiParmCnt++;
    try {
      setRowId(iiParmCnt, cnt);
    } catch (Exception ex) {
      printf("setRowid error; " + iiParmCnt + "," + cnt);
    }
    return;
  }

  protected void ppp(int col1, double num1) {
    iiParmCnt = col1;
    try {
      this.setDouble(col1, num1);
    } catch (Exception ex) {
      printf("ppp.setDouble error; " + col1 + "," + num1);
    }
  }

  protected void ppp(double num1) {
    iiParmCnt++;
    try {
      this.setDouble(iiParmCnt, num1);
    } catch (Exception ex) {
      printf("ppp.setDouble error; " + num1);
    }
  }

  protected void ppp(int col1, int num1) {
    iiParmCnt = col1;
    try {
      this.setDouble(col1, num1);
    } catch (Exception ex) {
      printf("ppp.setDouble error; " + col1 + "," + num1);
    }
  }

  protected void ppp(int num1) {
    iiParmCnt++;
    try {
      this.setDouble(iiParmCnt, num1);
    } catch (Exception ex) {
      printf("ppp.setDouble error; " + num1);
    }
  }

  protected void selectPtrBusinday() throws Exception {
    // dataBase = dbNameTo;
    // daoTable = "PTR_BUSINDAY";
    // whereStr = "FETCH FIRST 1 ROW ONLY";
    // selectTable();
    sqlCmd = "select business_date,online_date from ptr_businday" + commSqlStr.rownum(1);
    sqlSelect();
    if (sqlNrow <= 0) {
      printf("select ptr_businday error!");
      exitProgram(1);
    }

    if (empty(hBusiDate)) {
      hBusiDate = colSs("business_date");
    }
    hOnlineDate = colSs("online_date");
    printf("本日營業日 : [" + hBusiDate + "], 系統日期: [" + sysDate + "]");
  }

  protected void endProgram(int aiCnt) throws Exception {
    printf("程式處理結束, 筆數=" + aiCnt);
    this.hCallErrorDesc = "程式處理結束, 筆數=" + aiCnt;
    hCallErrorCode = "0000";
    callBatch(0, 0, 1);
  }

  protected void endProgram() throws Exception {
    endProgram(totalCnt);
  }

  protected void okExit(int aiRc) throws Exception {
    printf("程式結束: <<<ok-exit>>>");
    rc = aiRc;

    setExceptExit(aiRc);
    if (getConnection() != null) {
      sqlRollback();
      if (empty(hCallErrorDesc)) {
        hCallErrorDesc = "程式中止執行";
      }
      hCallErrorCode = "0000";
      this.callBatch(0, 0, 1);
    }
    finalProcess();
    // throw new Exception("DXC_NOSHOW_EXCEPTION");
    throw new Exception(new Exception("<<<ok-exit>>>"));
  }

  protected void errExit(int aiRc) throws Exception {
    rc = aiRc;
    setExceptExit(aiRc);
    if (getConnection() != null) {
      sqlRollback();
      if (empty(hCallErrorDesc)) {
        if (aiRc == 0)
          hCallErrorDesc = "程式未完成結束";
        else {
          hCallErrorDesc = "程式不正常結束";
        }
      }
      if (aiRc == 0)
        hCallErrorCode = "0000";
      else
        hCallErrorCode = "" + aiRc;
      this.callBatch(0, 0, 1);
    }
    // throw new Exception(new Exception("<<<err-exit>>>"));
    // throw new Exception("DXC_NOSHOW_EXCEPTION");
    exitProgram(aiRc);
  }

  protected void errExit() throws Exception {
    if (rc == 0)
      errExit(0);
    else
      errExit(1);
  }

  protected void errIndex(int aiIndx) throws Exception {
    if (aiIndx > 0)
      return;
    errmsg("cannot create PS.index: " + nvl(daoTable, zzDaoTable) + "; Exit Program!!!");
    // throw new Exception(new Exception(h_call_error_desc));
    errExit(1);
  }

}
