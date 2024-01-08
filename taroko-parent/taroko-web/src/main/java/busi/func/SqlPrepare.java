/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*  110-01-07  V1.00.02  tanwei        修改意義不明確變量                                                                          *
******************************************************************************/
package busi.func;
/** 20190620:  JH    sql_parm()
 * 2019-0415:  JH    mod_XXX()
   2019-0409:  JH    aaa(sql1,sql2,obj)
   2018-1019:	JH		aa_xxx()
 * 2018-0809:	JH		sql2Where(,)
 * 2017-1204: JH>>++ppp()
 * V00.0		JH		2017-0802: initial
 * */
import java.util.Arrays;
import java.util.HashMap;

public class SqlPrepare {

  // --
  StringBuffer sbSql1;
  StringBuffer sbSql2;
  String sqlTable = "";
  boolean sqlInsert = false;
  boolean sqlUpdate = false;
  int parmIndex = 0;

  private HashMap<Integer, Object> sortHash = new HashMap<Integer, Object>();

  taroko.base.BaseData wp;

  taroko.base.SqlParm sqlParm = null;

  public void wp2Parm(taroko.com.TarokoCommon wr) {
    wp = wr;
  }

  // -AA-組SQL-cmd---------------------------------------------------
  public void sql2Stmt(String sql1) {
    sql2Stmt(sql1, null);
  }

  public void sql2Stmt(String sql1, taroko.com.TarokoCommon wr) {
    sqlInsert = false;
    sqlUpdate = false;
    parmIndex = 0;
    sbSql1 = new StringBuffer(sql1);
    sbSql2 = new StringBuffer("");
    sortHash.clear();
    wp = wr;
    sqlParm = new taroko.base.SqlParm();
  }

  public void sql2Insert(String sTable) {
    sql2Insert(sTable, null);
  }

  public void sql2Insert(String sTable, taroko.base.BaseData wr) {
    sqlInsert = true;
    sqlUpdate = false;
    parmIndex = 0;
    sqlTable = "insert into " + sTable + " ( ";
    sbSql1 = new StringBuffer("");
    sbSql2 = new StringBuffer("");
    sortHash.clear();
    wp = wr;
    sqlParm = new taroko.base.SqlParm();
  }

  public void sql2Update(String sTable) {
    sql2Update(sTable, null);
  }

  public void sql2Update(String sTable, taroko.base.BaseData wr) {
    sqlInsert = false;
    sqlUpdate = true;
    parmIndex = 0;
    sqlTable = "update " + sTable + " set ";
    sbSql1 = new StringBuffer("");
    sbSql2 = new StringBuffer("");
    sortHash.clear();
    wp = wr;
    sqlParm = new taroko.base.SqlParm();
  }

  public void rowid2Where(String strName) {
    sbSql2.append(" where rowid =x'" + strName + "'");
  }

  // public void sql2Where(String sql1, String s1) {
  // sb_sql2.append(sql1);
  // if (sql1.indexOf("?")>=0) {
  // sortHash.put(1+ii_parm++,s1);
  // }
  // }
  // public void sql2Where(String sql1, double num1) {
  // sb_sql2.append(sql1);
  // if (sql1.indexOf("?")>=0) {
  // //setDouble(1+ii_parm++,num1);
  // sortHash.put(1+ii_parm++,num1);
  // }
  // }
  public void sql2Where(String sql1, Object obj) {
    sbSql2.append(sql1);
    if (sql1.indexOf("?") >= 0) {
      // setDouble(1+ii_parm++,num1);
      sortHash.put(1 + parmIndex++, obj);
    }
  }

  // --<<----------------------------------------------------------
  public void addsqlYmd2(String col) {
    if (sqlInsert) {
      sbSql1.append(", " + col);
      sbSql2.append(", to_char(sysdate,'yyyymmdd') ");
    } else if (sqlUpdate) {
      sbSql1.append(", " + col + " =to_char(sysdate,'yyyymmdd') ");
    }
  }

  public void addsqlTime2(String col) {
    if (sqlInsert) {
      sbSql1.append(", " + col);
      sbSql2.append(", to_char(sysdate,'hh24miss') ");
    } else if (sqlUpdate) {
      sbSql1.append(", " + col + " =to_char(sysdate,'hh24miss') ");
    }
  }

  public void addsqlDate2(String col) {
    if (sqlInsert) {
      sbSql1.append(", " + col);
      sbSql2.append(", sysdate ");
    } else if (sqlUpdate) {
      sbSql1.append(", " + col + " =sysdate ");
    }
  }

  // ->>>-2018-0727-
  public void addsql2(String sql1) {
    addsql(sql1);
  }

  public void addsqlParm(String sql1, String sql2, Object strName) {
    if (sql1.indexOf("?") >= 0) {
      addsql(sql2, sql1);
    } else
      addsql(sql1, sql2);
    if ((sql1 + sql2).indexOf("?") >= 0) {
      parmIndex++;
      sortHash.put(parmIndex, strName);
    }
  }

  // public void aaa(String sql1, String sql2, double s1) {
  // if (sql1.indexOf("?")>=0) {
  // addsql(sql2,sql1);
  // }
  // else addsql(sql1,sql2);
  // if ((sql1+sql2).indexOf("?")>=0) {
  // ii_parm++;
  // sortHash.put(ii_parm,s1);
  // }
  // }
  // public void aaa(String sql1, String sql2, int s1) {
  // if (sql1.indexOf("?")>=0) {
  // addsql(sql2,sql1);
  // }
  // else addsql(sql1,sql2);
  // if ((sql1+sql2).indexOf("?")>=0) {
  // ii_parm++;
  // sortHash.put(ii_parm,s1);
  // }
  // }
  public void addsqlParm3(String sql1, String strName) {
    if (sqlInsert) {
      addsql(sql1, strName);
      return;
    }
    addsql(sql1);
    if (sql1.indexOf("?") >= 0) {
      parmIndex++;
      sortHash.put(parmIndex, strName);
    }
  }

  public void addsqlParm3(String sql1, double douName) {
    addsql(sql1);
    if (sql1.indexOf("?") >= 0) {
      parmIndex++;
      sortHash.put(parmIndex, douName);
    }
  }

  public void addsqlParm3(String sql1, int numName) {
    addsql(sql1);
    if (sql1.indexOf("?") >= 0) {
      parmIndex++;
      sortHash.put(parmIndex, numName);
    }
  }

  public void addsqlYmd(String sql1) {
    addsql(sql1, ", to_char(sysdate,'yyyymmdd') ");
  }

  public void addsqlTime(String sql1) {
    addsql(sql1, ", to_char(sysdate,'hh24miss')");
  }

  public void addsqlDate(String sql1) {
    addsql(sql1, ", sysdate");
  }

  // -<<<-2018-0727--
  public void addsql(String sql1) {
    sbSql1.append(sql1);
  }

  public void addsql(String sql1, String sql2) {
    sbSql1.append(sql1);
    sbSql2.append(sql2);
  }

  public void addsqlParm(String col) {
    addsqlParm(col, wp.itemStr(col));
  }

  public void addsqlParmNvl(String col, String strName) {
    addsqlParm(col, wp.itemNvl(col, strName));
  }

  public void addsqlParm2(String col, String strName) {
    addsqlParm(col, strName);
  }

  public void addsqlParm(String col, String strName) {
    if (sqlInsert) {
      sbSql1.append(", " + col);
      sbSql2.append(", ? ");
    } else if (sqlUpdate) {
      sbSql1.append(", " + col + " =? ");
    } else if (sqlParm != null) {
      sqlParm.setString(col, strName);
      return;
    } else
      return;

    parmIndex++;
    // this.setString(ii_parm,s1);
    sortHash.put(parmIndex, strName);
  }

  public void addsqlParm2(String col) {
    addsqlParm2(col, wp.itemNum(col));
  }

  public void addsqlParm(String col, double num1) {
    if (sqlInsert) {
      sbSql1.append(", " + col);
      sbSql2.append(", ? ");
    } else if (sqlUpdate) {
      sbSql1.append(", " + col + " =? ");
    } else if (sqlParm != null) {
      sqlParm.setDouble(col, num1);
      return;
    } else
      return;

    parmIndex++;
    // this.setDouble(ii_parm, num1);
    sortHash.put(parmIndex, num1);
  }

  public void addsqlParm2(String col, double num1) {
    if (sqlInsert) {
      sbSql1.append(", " + col);
      sbSql2.append(", ? ");
    } else if (sqlUpdate) {
      sbSql1.append(", " + col + " =? ");
    } else if (sqlParm != null) {
      sqlParm.setDouble(col, num1);
      return;
    } else
      return;

    parmIndex++;
    // this.setDouble(ii_parm, num1);
    sortHash.put(parmIndex, num1);
  }

  public void addsqlParm2(String col, int num1) {
    addsqlParm(col, num1);
  }

  public void addsqlParm(String col, int num1) {
    if (sqlInsert) {
      sbSql1.append(", " + col);
      sbSql2.append(", ? ");
    } else if (sqlUpdate) {
      sbSql1.append(", " + col + " =? ");
    } else if (sqlParm != null) {
      sqlParm.setInt(col, num1);
      return;
    } else
      return;

    parmIndex++;
    // this.setDouble(ii_parm, num1);
    sortHash.put(parmIndex, num1);
  }

  public String sqlStmt() {
    String isSql = "";

    if (sqlInsert) {
      isSql = sqlTable;
      if (sbSql1.indexOf(",") == 0) {
        isSql += sbSql1.substring(1);
      } else
        isSql += sbSql1.toString();
      isSql += " ) values ( ";
      if (sbSql2.indexOf(",") == 0) {
        isSql += sbSql2.substring(1);
      } else
        isSql += sbSql2.toString();
      isSql += " )";
    } else if (sqlUpdate) {
      isSql = sqlTable;
      if (sbSql1.indexOf(",") == 0) {
        isSql += sbSql1.substring(1);
      } else
        isSql += sbSql1.toString();

      isSql += " " + sbSql2.toString();
    } else if (sqlParm != null) {
      sqlParm.nameSqlParm(sbSql1.toString());
      return sqlParm.getConvSQL();
    } else {
      return "";
    }

    // wp.ddd(is_sql);
    // ii_parm =0;
    // sql_table ="";
    // sb_sql1 =null;
    // sb_sql2 =null;
    return isSql;
  }

  public Object[] sqlParm() {
    if (sqlInsert == false && sqlUpdate == false) {
      return sqlParm.getConvParm();
    }
    if (sqlParm != null && sqlParm.getParmcnt() > 0) {
      return sqlParm.getConvParm();
    }

    Object param[] = new Object[sortHash.size()];
    Object[] keys2 = sortHash.keySet().toArray();
    Arrays.sort(keys2);
    int num = 0;
    for (Object sortData : keys2) {
      param[num++] = sortHash.get(sortData);
    }
    // sortHash.clear();
    return param;
  }

  public void modxxx() {
    modxxx(wp.loginUser, wp.modPgm());
  }

  public void modxxx(String modUser, String modPgm) {
    if (sqlInsert) {
      addsqlParm(",?", ", mod_user", modUser);
      addsqlDate(", mod_time");
      addsqlParm(",?", ", mod_pgm", modPgm);
      addsql(", mod_seqno", ", 1");
    } else if (sqlUpdate) {
      addsqlParm3(", mod_user =?", modUser);
      addsql2(", mod_time =sysdate");
      addsqlParm3(", mod_pgm =?", modPgm);
      addsql(", mod_seqno =nvl(mod_seqno,0)+1");
    }
    // ppss("mod_user",mod_user);
    // ppdate("mod_time");
    // ppss("mod_pgm",mod_pgm);
    // if (sql_insert) {
    // ppint("mod_seqno",1);
    // }
    // else if (sql_update) {
    // this.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
    // }
  }

  public void parmClear() {
    parmIndex = 0;
    sortHash.clear();
  }

  // -VV-組SQL-cmd---------------------------------------------------
  //
  //
  // String col_ss(String col) {
  // String ss = "";
  // try {
  // ss = (String) colHash.get(col.trim().toUpperCase());
  // } catch (Exception ex) {
  // //expHandle("col_ss", ex);
  // }
  // return ss.trim();
  // }
  //
  // double col_num(String col) {
  // String ss = "";
  // ss =col_ss(col);
  // try {
  // return Double.parseDouble(ss);
  // } catch(Exception ex) {
  // //expHandle("col_num", ex);
  // }
  // return 0;
  // }

}
