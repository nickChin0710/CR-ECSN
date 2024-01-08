/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 111-11-25  V1.00.02  Zuwei       Sync from mega                              *
*                                                                            *  
******************************************************************************/
package busi;
/** busiFUNC 公用程式
 * 2019-1127   JH    ++sqlSelect[o]
 * 2019-1028   JH    setConn()
 * 2019-1021   JH    EcsApprove()
2019-0408:     JH    vset(xx,xx)
 * 2018-1005:   JH      --commString,commSqlStr 
 * 2018-0726:   JH      ++ppRowid(,,)
 * 2018-0115:   JH      ++ppp(Str,num,int)
 * 110-01-07  V1.00.08  tanwei        修改意義不明確變量                                                                          *  
 * */

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FuncBase extends DbAccess {
  protected String actionCode = "";

  protected String sqlID = "";
  protected String strSql = "";
  public String sqlSelect = "";
  public String sqlFrom = "";
  public String sqlWhere = "";
  public String sqlOrder = "";

  protected String sqlTime = "";
  protected String sysDate = "";
  protected String sysTwDate = "";
  protected String sysTime = "";
  protected String sysTime9 = "";

  public String modUser = "";
  public String modPgm = "";

  protected final String errOtherModify = "資料已被異動 OR 不存在";

  // --
  // StringBuffer sb_sql1;
  // StringBuffer sb_sql2;
  // String sql_table = "";
  // boolean sql_insert = false;
  // boolean sql_update = false;
  int parmIndex = 0;

  protected String getVersion() {
    return this.getClass().getName() + ": Ver." + versionStr;
  }
  
  private void printVersion() {	  
	  String s1= modVersion();
	  if (wp==null || empty(s1))		  
		  return;
	  wp.log("[[[vvv]]]"+this.getClass().getSimpleName()+": "+s1);	  
  }
  
  public void printVersion(String s1) {	  
	  if (wp==null || empty(s1))
		  return;
	  wp.log("[[[vvv]]]"+this.getClass().getSimpleName()+": "+s1);
  }
  
  public String modVersion() {
	  return "";
  }
  
  public void setConn(taroko.com.TarokoCommon wr) {
    wp = wr;
    conn = wr.getConn();
    modUser = wr.loginUser;
    modPgm = wr.modPgm();
    sysDate = wr.sysDate;
    sysTime = wr.sysTime;
	printVersion();
  }

  public boolean checkApprove(String aAprid, String aPasswd) {
    ofcapp.EcsApprove func = new ofcapp.EcsApprove(wp);
    return func.onlineApprove(aAprid, aPasswd);
  }

  public boolean checkApproveZz() {
    return checkApprove(wp.itemStr2("approval_user"), wp.itemStr2("approval_passwd"));
  }

  protected void sqlInit() {
    sqlSelect = "";
    sqlFrom = "";
    sqlWhere = "";
    sqlOrder = "";
    strSql = "";
    return;
  }

  protected void sqlSelect(String sql1, boolean bLog) {
    wp.logSql = bLog;
    sqlSelect(sql1, null);
    return;
  }

  protected void sqlErr(String aTable) {
    if (sqlRowNum == 0) {
      errmsg("執行SQL:" + aTable + ", 資料不存在");
      return;
    }

    errmsg("執行SQL:" + aTable + " 錯誤, err=" + sqlErrtext);
  }

public String dbSysDate() throws Exception {
	String sql1="select varchar_format(sysdate,'yyyymmdd') as db_sysdate from dual";
	sqlSelect(sql1);
	if (sqlRowNum>0)
		return colStr("db_sysdate");
	return sysDate;
}
  public void varsSet2(String col, String strName) {
    varsSet(col, strName);
  }

  public void vset(String col, int num1) {
    varsSet(col, "" + num1);
  }

  public void vset(String col, double num1) {
    varsSet(col, "" + num1);
  }

  protected void col2wpCol(String col) {
    wp.colSet(col, colStr(col));
  }

  protected void col2wpCol(int num, String col) {
    wp.colSet(num, col, colStr(col));
  }

  protected void col2wpItem(String col) {
    wp.itemSet(col, colStr(col));
  }

  // -AA-setSQL-parm------------------------------------------
  protected void setRowId2(String col, String val1) {
    this.setRowId(col, val1);
  }

  protected void setRowId2(int num, String val1) {
    parmIndex = num;
    this.setRowId(num, val1);
  }

  protected void setRowId(String val1) {
    parmIndex++;
    setRowId(parmIndex, val1);
  }

  protected void setString2(String col, String val1) {
    setString(col, val1);
  }

  protected void setString(String val1) {
    parmIndex++;
    setString(parmIndex, val1);
  }

  protected void setString2(int col, String val1) {
    parmIndex = col;
    setString(col, val1);
  }

  protected void setDouble2(String col, double num1) {
    setDouble(col, num1);
  }

  protected void setDouble2(int col, double num1) {
    parmIndex = col;
    setDouble(col, num1);
  }

  protected void setDouble(double num1) {
    parmIndex++;
    setDouble(parmIndex, num1);
  }

  protected void setInt2(String col, int num1) {
    setInt(col, num1);
  }

  protected void setInt2(int col, int num1) {
    parmIndex = col;
    setInt(col, num1);
  }

  protected void setInt(int num1) {
    parmIndex++;
    setInt(parmIndex, num1);
  }

  protected void item2ParmNvl(String sName, String col, String strName) {
    try {
      this.setString(sName, wp.itemNvl(col, strName));
    } catch (Exception e) {
    }
  }

  protected void item2ParmNvl(String col, String strName) {
    try {
      this.setString(col, wp.itemNvl(col, strName));
    } catch (Exception e) {
    }
  }

  protected void item2ParmStr(String sName, String col) {
    try {
      this.setString(sName, wp.itemStr(col));
    } catch (Exception e) {
    }
  }

  protected void item2ParmStr(String col) {
    try {
      this.setString(col, wp.itemStr(col));
    } catch (Exception e) {
      wp.log("col=" + col + ": item2Parm_ss.Exception");
    }
  }

  protected void item2ParmNum(String sName, String col) {
    try {
      this.setDouble(sName, wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  protected void item2ParmNum(String col) {
    try {
      this.setDouble(col, wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  protected void item2ParmInt(String sName, String col) {
    try {
      this.setInt(sName, (int) wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  protected void item2ParmInt(String col) {
    try {
      this.setInt(col, (int) wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  // -VV------------------------------------------------------
  // -AA-setSQL-parm-col---------------------------------------
  protected void col2ParmNvl(String sName, String col, String strName) {
    try {
      this.setString(sName, colNvl(col, strName));
    } catch (Exception e) {
    }
  }

  protected void col2ParmNvl(String col, String strName) {
    try {
      this.setString(col, colNvl(col, strName));
    } catch (Exception e) {
    }
  }

  protected void col2ParmStr(String sName, String col) {
    try {
      this.setString(sName, colStr(col));
    } catch (Exception e) {
    }
  }

  protected void col2ParmStr(String col) {
    try {
      this.setString(col, colStr(col));
    } catch (Exception e) {
      wp.log("col=" + col + ": col2Parm_ss.Exception");
    }
  }

  protected void col2ParmNum(String sName, String col) {
    try {
      this.setDouble(sName, colNum(col));
    } catch (Exception e) {
    }
  }

  protected void col2ParmNum(String col) {
    try {
      this.setDouble(col, colNum(col));
    } catch (Exception e) {
    }
  }

  protected void col2ParmInt(String sName, String col) {
    try {
      this.setInt(sName, (int) colNum(col));
    } catch (Exception e) {
    }
  }

  protected void col2ParmInt(String col) {
    try {
      this.setInt(col, (int) colNum(col));
    } catch (Exception e) {
    }
  }

  // -VV-setSQL-col------------------------------------------

  protected String colYn(int num, String col) {
    return nvl(colStr(num, col), "N");
  }

  protected String colYn(String col) {
    return nvl(colStr(0, col), "N");
  }

  // -AA-setSQL-var------------------------------------------
  protected void var2ParmNvl(String sName, String col, String strName) {
    try {
      this.setString(sName, varsNvl(col, strName));
    } catch (Exception e) {
    }
  }

  protected void var2ParmNvl(String col, String strName) {
    try {
      this.setString(col, varsNvl(col, strName));
    } catch (Exception e) {
    }
  }

  protected void var2ParmStr(String sName, String col) {
    try {
      this.setString(sName, varsStr(col));
    } catch (Exception e) {
    }
  }

  protected void var2ParmStr(String col) {
    try {
      this.setString(col, varsStr(col));
    } catch (Exception e) {
    }
  }

  protected void var2ParmNum(String sName, String col) {
    try {
      this.setDouble(sName, varsNum(col));
    } catch (Exception e) {
    }
  }

  protected void var2ParmNum(String col) {
    try {
      this.setDouble(col, varsNum(col));
    } catch (Exception e) {
    }
  }

  protected void var2ParmInt(String sName, String col) {
    try {
      this.setInt(sName, (int) varsNum(col));
    } catch (Exception e) {
    }
  }

  protected void var2ParmInt(String col) {
    try {
      this.setInt(col, (int) varsNum(col));
    } catch (Exception e) {
    }
  }

  // -VV:var2parm--------------------------------------------------

  public String getSelectSql() {
    if (isEmpty(strSql)) {
      strSql = sqlSelect + " " + sqlFrom + " " + sqlWhere + " " + sqlOrder;
    }
    return strSql;
  }

  protected String sqlRowId(String strName) {
    return sqlRowId(strName, "");
  }

  protected String sqlRowId(String strName, String dbcol) {
    if (empty(strName)) {
      return "";
    }
    setString(nvl(strName));
    if (empty(dbcol)) {
      return " and hex(rowid) = ? ";
    }
    return " and hex(" + dbcol + ") = ? ";
  }

  protected double sqlRowcount(String s1Table, String s2Where) {
    return sqlRowcount(s1Table, s2Where, null);
  }

  protected double sqlRowcount(String s1Table, String s2Where, Object[] param) {
    if (isEmpty(s1Table))
      return 0;

    strSql = "select count(*) as tot_cnt" + " from " + s1Table;

    String lsWhere = s2Where.trim();
    if (lsWhere.toLowerCase().indexOf("where") == 0)
      strSql += " " + lsWhere;
    else
      strSql += " where " + lsWhere;
    sqlSelect(strSql, param);
    return colNum("tot_cnt");
  }

  protected String varModuser() {
    return varsStr("mod_user");
  }

  protected String varModpgm() {
    return varsStr("mod_pgm");
  }

  protected String varModseqno() {
    return varsStr("mod_seqno");
  }

  public int varPos(String val1, String col) {
    int pp = pos(val1, varsStr(col));
    if (pp >= 0)
      return pp;
    return -1;
  }

  public boolean varEmpty(String strName) {
    return (varsStr(strName).length() == 0);
  }

  public boolean varHas(String strName) {
    return (varsStr(strName).length() > 0);
  }

  public boolean varEq(String col, String strName) {
    return varsStr(col).equals(strName);
  }

  public boolean notEmpty(String strName) {
    return !empty(strName);
  }

  public boolean empty(String strName) {
    if (strName == null)
      return true;
    if (strName.trim().length() == 0)
      return true;

    return false;
  }

  protected boolean isEmpty(String strName) {
    if (strName == null)
      return true;
    if (strName.trim().length() == 0)
      return true;

    return false;
  }

  protected boolean isNumber(String strName) {
    if (strName == null || strName.trim().length() == 0)
      return false;

    // double lm_val=0;
    try {
      Double.parseDouble(strName);
    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  protected int chkStrend(String strName, String colName) {
    if (isEmpty(strName) || isEmpty(colName))
      return 1;
    if (nvl(strName).compareTo(nvl(colName)) > 0)
      return -1;

    return 1;
  }

  protected int condStrend(String strName, String colName) {
    return chkStrend(strName, colName);
  }

  // //-SQL-syntax-
  // protected String where_col(String s1, String col) {
  // return where_col(s1,col,"=");
  // }
  // public String where_col(String s1, String col, String cond) {
  // if (isEmpty(s1) || isEmpty(col))
  // return "";
  // if (isEmpty(cond)) {
  // return " and "+col+" ='"+s1+"'";
  // }
  //
  // return " and "+col+" "+cond+" '"+s1+"'";
  // }

  public void setModxxx(String modUser, String modPgm, String modSeqno) {
    varsSet("mod_user", modUser);
    varsSet("mod_pgm", modPgm);
    varsSet("mod_seqno", nvl(modSeqno, "0"));
  }

  public void varModxxx(String modUser, String modPgm, String modSeqno) {
    setModxxx(modUser, modPgm, modSeqno);
  }

  // public String sql_modxxx() {
  // return "mod_time=sysdate, mod_user=?, mod_pgm=?,
  // mod_seqno=nvl(mod_seqno,0)+1";
  // }

  public String sqlModxxx() {
    String lsPgm = varsStr("mod_pgm");
    if (isEmpty(lsPgm)) {
      lsPgm = this.getClass().getSimpleName();
    }
    return "mod_time=sysdate, mod_user ='" + varsStr("mod_user") + "'" + ", mod_pgm ='" + lsPgm
        + "'" + ", mod_seqno=nvl(mod_seqno,0)+1";
  }

  protected int colPos(String val, String col) {
    return pos(val, colStr(col));
  }

  protected int pos(String strName, String colName) {
    if (strName == null || colName == null)
      return -1;
    if (strName.trim().length() == 0)
      return -1;
    if (colName.trim().length() == 0)
      return -1;

    int numVal = strName.trim().indexOf(colName.trim());
    if (numVal >= 0)
      return numVal;
    return -1;
  }

  // -data-Conert-------------
  protected String numDisp(double num, String strName) {
    String fmt = "0";
    if (isEmpty(strName) == false) {
      fmt = strName;
    }
    DecimalFormat formatter = new DecimalFormat(fmt);
    return formatter.format(num);
  }

  protected String numDisp(double num) {
    return numDisp(num, "0");
  }

  protected String intToStr(int num1) {
    /*
     * int number = 12345; DecimalFormat decimalFormat = new DecimalFormat("#,##0"); String
     * numberAsString = decimalFormat.format(number);
     */
    DecimalFormat decFMT = new DecimalFormat("#,##0");
    return decFMT.format(num1);
  }

  protected String strMid(String strName, int indx1, int len1) {
    return commString.mid2(strName, indx1, len1);
    // if (s1.length() == 0) {
    // return "";
    // }
    // if (indx_1 > s1.length() || len_1 > s1.length()) {
    // return s1;
    // }
    // if ((indx_1 + len_1) >= s1.length()) {
    // return s1.substring(indx_1);
    // }
    //
    // return s1.substring(indx_1, (indx_1 + len_1));
  }

  protected String numToStr(double num1, String fmt1) {
    DecimalFormat decFMT;
    if (isEmpty(fmt1)) {
      decFMT = new DecimalFormat("#,##0");
    } else {
      decFMT = new DecimalFormat(fmt1);
    }
    return decFMT.format(num1);
  }

  protected double strToNum(String strName) {
    return commString.strToNum(strName);
  }

  protected int strToInt(String strName) {
    return commString.strToInt(strName);
  }

  protected boolean eq(String strName, String colName) {
    return nvl(strName).equalsIgnoreCase(nvl(colName));
  }

  protected boolean eqIgno(String strName, String colName) {
    String strNameVal = nvl(strName);
    String colNameVal = nvl(colName);
    return strNameVal.equalsIgnoreCase(colNameVal);
  }

  protected boolean eqAny(String strName, String colName) {
    if (strName == null || colName == null)
      return false;
    return strName.equals(colName);
  }

  protected boolean neIgno(String strName, String colName) {
    String strNameVal = nvl(strName);
    String colNameVal = nvl(colName);
    return !strNameVal.equalsIgnoreCase(colNameVal);
  }

  protected boolean neAny(String strName, String colName) {
    if (strName == null || colName == null)
      return true;
    return !strName.equals(colName);
  }

  // -insert,update,delete-
  protected boolean isAdd() {
    return (actionCode.indexOf("A") == 0);
  }

  protected boolean isUpdate() {
    return (actionCode.indexOf("U") == 0);
  }

  protected boolean isDelete() {
    return (actionCode.indexOf("D") == 0);
  }

  protected int audCheck(String itemName, String colName) {
    if (sqlRowNum <= 0) {
      if (isUpdate() || isDelete()) {
        errmsg("資料不存在, 不可[修改,刪除]");
        return rc;
      }
    } else {
      if (isAdd()) {
        errmsg("資料已存在, 不可[新增]");
        return rc;
      }
      if (empty(itemName))
        itemName = "mod_seqno";
      if (empty(colName))
        colName = "mod_seqno";
      if (wp.itemNum(itemName) != colNum(colName)) {
        errmsg(errOtherModify);
        return rc;
      }
    }
    return rc;
  }

  protected boolean isOtherModify(String s1Tab, String s2Where, Object[] param) {
    // -有人異動: return true-
    if (sqlRowcount(s1Tab, s2Where, param) > 0)
      return false;

    errmsg("資料已被異動 or 不存在");
    return true;
  }

  protected boolean isOtherModify(String s1Tab, String s2Where) {
    // -有人異動: return true-
    if (sqlRowcount(s1Tab, s2Where) > 0)
      return false;

    errmsg("資料已被異動 or 不存在");
    return true;
  }

  protected String businDate() {
    strSql = "select business_date as db_1" + " from ptr_businday" + this.sqlRownum(1);
    this.sqlSelect(strSql);
    return colStr("db_1");
  }

  protected void dateTime() {
    String dateStr = ""; // dispStr="";
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    // SimpleDateFormat form_2 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
    SimpleDateFormat form3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    dateStr = form1.format(currDate);
    // dispStr = form_2.format(currDate);
    sqlTime = form3.format(currDate);

    sysDate = dateStr.substring(0, 8);
    sysTwDate = (Integer.parseInt(dateStr.substring(0, 4)) - 1911) + dateStr.substring(4, 8);
    sysTime = dateStr.substring(8, 14);
    sysTime9 = dateStr.substring(8, 17);
    // dispDate = dispStr.substring(0,10);
    // dispTime = dispStr.substring(10,18);
    // carriage[0] = 0x0D;
    // carriage[1] = 0x0A;
    // newLine = new String(carriage,0,2);
    return;
  }

  protected String getSysDate() {
    dateTime();
    return sysDate;
  }

  protected int errExit(String strName) {
    errmsg(strName);
    return -1;
  }

  protected boolean wpIsNull() {
    if (wp == null) {
      errmsg("wp is no avalid");
      return true;
    }
    return false;
  }

  protected String userDeptNo() {
    if (empty(wp.loginUser))
      return "";

    String sql1 = "select usr_deptno" 
			+ " from sec_user " 
			+ " where usr_id =?";
    sqlSelect(sql1, new String[] {wp.loginUser});
    if (sqlRowNum > 0) {
      return colStr("usr_deptno");
    }

    return "";
  }

  protected void colDataToWpCol(String daoTid) {
    if (wp == null || colName.length == 0)
      return;
    for (int ii = 0; ii < colName.length; ii++) {
      if (empty(colName[ii]))
        continue;

      wp.colSet(daoTid + colName[ii], colStr(daoTid + colName[ii]));
    }
  }

  protected void colDataToWpItem(String daoTid) {
    if (wp == null || colName.length == 0)
      return;
    for (int ii = 0; ii < colName.length; ii++) {
      if (empty(colName[ii]))
        continue;

      wp.itemSet(daoTid + colName[ii], colStr(daoTid + colName[ii]));
    }
  }
protected void col_2Item(String tid) {
	if (wp == null || colName.length == 0)
		return;
	for (int ii = 0; ii < colName.length; ii++) {
		if (empty(colName[ii]))
			continue;
		if (sqlRowNum <=0) {
			wp.itemSet(daoTid + colName[ii], "");
		}
		else {
			wp.itemSet(daoTid + colName[ii], colStr(daoTid + colName[ii]));
		}
	}
}


  protected void sqlSelect(String sql1, Object obj1) {
    sqlSelect(sql1, new Object[] {obj1});
  }

}
