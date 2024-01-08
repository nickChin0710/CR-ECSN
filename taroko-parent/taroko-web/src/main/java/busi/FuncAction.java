/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.02  Zuwei       code format                              *
* 110-01-07  V1.00.03  tanwei        修改意義不明確變量                                                                          *  
******************************************************************************/
package busi;
/**
 * 2019-1122   JH    ++ss2Rowid()
 *  2019-0423:  JH    ++aa_dtime()
* */
public abstract class FuncAction extends FuncBase {

  protected boolean ibAdd = false;
  protected boolean ibUpdate = false;
  protected boolean ibDelete = false;

  public abstract void dataCheck();

  public abstract int dbInsert();

  public abstract int dbUpdate();

  public abstract int dbDelete();

  public abstract int dataProc();

  // -2019-0409>>>SQL=============================================================
  private busi.SqlPrepare ttSql = new busi.SqlPrepare();

  protected void sql2Insert(String sTable) {
    ttSql.parmClear();
    ttSql.sql2Insert(sTable);
  }

  protected void sql2Update(String sTable) {
    ttSql.parmClear();
    ttSql.sql2Update(sTable);
  }

  protected void addsql2(String sql1) {
    ttSql.addsql(sql1);
  }

  protected void addsqlParm(String sql1, String sql2, Object obj) {
    ttSql.addsqlParm(sql1, sql2, obj);
  }

  protected void addsqlParm(String sql1, String strName) {
    ttSql.addsqlParm(sql1, strName);
  }

  protected void addsqlParm(String sql1, double strName) {
    ttSql.addsqlParm(sql1, strName);
  }

  protected void addsqlParm(String sql1, int strName) {
    ttSql.addsqlParm(sql1, strName);
  }

  protected void addsqlModXXX(String modUser, String modPgm) {
    if (ttSql.sqlInsert) {
      ttSql.addsqlParm(",?", ", mod_user", modUser);
      ttSql.addsqlDate(", mod_time");
      ttSql.addsqlParm(",?", ", mod_pgm", modPgm);
      ttSql.addsqlParm(", mod_seqno", ", 1");
    } else if (ttSql.sqlUpdate) {
      ttSql.addsqlParm(", mod_user =?", modUser);
      ttSql.addsql2(", mod_time =sysdate");
      ttSql.addsqlParm(", mod_pgm =?", modPgm);
      ttSql.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
    }
  }

  protected void addsqlYmd(String sql1) {
    ttSql.addsql(sql1, ", to_char(sysdate,'yyyymmdd') ");
  }

  protected void addsqlTime(String sql1) {
    ttSql.addsql(sql1, ", to_char(sysdate,'hh24miss')");
  }

  protected void addsqlDate(String sql1) {
    ttSql.addsql(sql1, ", sysdate");
  }

  protected void addsqlDate2(String sql1) {
    ttSql.addsql(sql1, ", sysdate");
  }

  protected void addsql(String sql1) {
    ttSql.addsql(sql1);
  }

  protected void addsql(String sql1, String sql2) {
    ttSql.addsql(sql1, sql2);
  }

  protected void sqlWhere(String sql1, Object strName) {
    ttSql.sql2Where(sql1, strName);
  }

  protected void sqlWhereRowid(String strName) {
    ttSql.sql2Where(" where rowid =?", str2Rowid(strName));
  }

  public byte[] str2Rowid(String strName) {
    int len = strName.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(strName.charAt(i), 16) << 4) + Character.digit(strName.charAt(i + 1), 16));
    return data;
  }

  protected Object[] sqlParms() {
    return ttSql.sqlParm();
  }

  protected String sqlStmt() {
    return ttSql.sqlStmt();
  }

  // -<<<SQL===============================================================

  public int dbSave(String sAud) {
    actionInit(sAud);
    if (eqIgno(sAud, "A")) {
      return dbInsert();
    } else if (eqIgno(sAud, "U")) {
      return dbUpdate();
    } else if (eqIgno(sAud, "D")) {
      return dbDelete();
    }

    errmsg("DB-action 不是 A/U/D");
    return rc;
  }

  protected void actionInit(String strName) {
    actionCode = strName;
    ibAdd = isAdd();
    ibUpdate = isUpdate();
    ibDelete = isDelete();
    msgOK();
  }

  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String varsStr2(String col) {
    return varsStr(col);
  }

  public void varsSet2(String col, String strName) {
    varsSet(col, strName);
  }

}
