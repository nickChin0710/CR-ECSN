/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-22  V1.00.01  Zuwei       coding standard      *
*  110-01-08  V1.00.02  tanwei      修改意義不明確變量                                                                          *   
******************************************************************************/
package taroko.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SuppressWarnings({"unchecked", "deprecation"})
public class SqlAccess extends taroko.base.BaseSQL {

  public void sqlClear() {
    dataClear();
  }

  public String strSql = "";

  public void sqlSelect(Connection conn) throws Exception {
    sqlSelect(conn, strSql, null);
  }

  public void sqlSelect(Connection conn, String sql1) throws Exception {
    sqlSelect(conn, sql1, null);
  }

  public String getString(Connection conn, String sql1, Object[] obj) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    String strName = "";
    sqlInit();
    // sql_nrow = 0;

    if (empty(sql1)) {
      return "";
    }
    strSql = sql1;
    Object[] param = null;
    if (obj == null && sqlParm.nameSqlParm(sql1)) {
      strSql = sqlParm.getConvSQL();
      param = sqlParm.getConvParm();
    } else
      param = obj;
    // if (Arrays.asList("N","S").contains("parmFlag")) {
    // param =this.nameSqlParm(sql1);
    // is_sql =this.convertSQL;
    // }
    // else param =obj;

    try {
      ps = conn.prepareStatement(strSql);
      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }

      rs = ps.executeQuery();
      if (rs.next()) {
        strName = rs.getString(1);
      }
      rs.close();
      ps.close();
      rs = null;
      ps = null;
    } // End of try
    catch (Exception ex) {
      setSqlErrmsg(ex.getMessage());
      return "";
    } finally {
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
    if (empty(strName))
      return "";

    return strName;
  }

  public String getString(Connection conn) {
    return getString(conn, strSql, null);
  }

  public String getString(Connection conn, String sql1) {
    return getString(conn, sql1, null);
  }

  public String getString(Connection conn, Object[] param) {
    return getString(conn, strSql, param);
  }

  public double getNumber(Connection conn, String sql1, Object[] obj) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    double liNum = 0;
    sqlInit();

    if (empty(sql1)) {
      return 0;
    }

    strSql = sql1;
    Object[] param = null;
    // if (Arrays.asList("N","S").contains("parmFlag")) {
    // param =this.nameSqlParm(sql1);
    // is_sql =this.convertSQL;
    // }
    // else param =obj;
    if (obj == null && sqlParm.nameSqlParm(sql1)) {
      strSql = sqlParm.getConvSQL();
      param = sqlParm.getConvParm();
    } else
      param = obj;

    try {
      ps = conn.prepareStatement(strSql);
      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }

      rs = ps.executeQuery();
      if (rs.next()) {
        liNum = rs.getDouble(1);
      }
      rs.close();
      ps.close();
      rs = null;
      ps = null;
    } // End of try
    catch (Exception ex) {
      setSqlErrmsg(ex.getMessage());
      return 0;
    } finally {
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
    return liNum;
  }

  public double getNumber(Connection conn) {
    return getNumber(conn, strSql, null);
  }

  public double getNumber(Connection conn, String sql1) {
    return getNumber(conn, sql1, null);
  }

  public double getNumber(Connection conn, Object[] param) {
    return getNumber(conn, strSql, param);
  }

}
