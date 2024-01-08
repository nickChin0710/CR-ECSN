package taroko.base;
/**
 * 2020-0624   JH    HashMap >> ArrayList
 * 2019-0109   JH    ++aaa_ymd/time/dtime
 * */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Parm2Sql {
private ArrayList<Object> aa_parms =new ArrayList<>();

private String _convSQL="";
private Object[] _convParm;
private boolean _insert=false, _update=false;
private String _tableName="";

private StringBuffer _sql1=new StringBuffer("");
private StringBuffer _sql2=new StringBuffer("");
private StringBuffer _sqlWhere=new StringBuffer("");

public void insert(String a_tab) {
   _insert =true;
   _update =false;
   _tableName =a_tab;
   _sql1 =new StringBuffer("");
   _sql2 =new StringBuffer("");
   _sqlWhere =new StringBuffer("");
   clear();
}
public void update(String a_tab) {
//   _parmIndx =0;
   _insert =false;
   _update =true;

   _tableName =a_tab;
   _sql1 =new StringBuffer("");
   _sql2 =new StringBuffer("");
   _sqlWhere =new StringBuffer("");
   clear();
}

public String getSql() {
   if (_convSQL.length()>0) return _convSQL;
   return getConvSql();
}
public Object[] getParms() {
   return getConvParm();
}
private String getConvSql() {
   _convSQL ="";
   if (_insert) {
      _convSQL ="insert into "+_tableName+" ( "+
         _sql1.substring(0,_sql1.length() -1)+" ) values ( "+
         _sql2.substring(0,_sql2.length()-1)+" )";
   }
   else if (_update) {
      _convSQL ="update "+_tableName+" set "+
         _sql1.substring(0,_sql1.length()-1)+" "+
         _sqlWhere.toString();
   }
   return _convSQL;
}
private Object[] getConvParm() {
   sort2Parm(true);
   return _convParm;
}
public void clear() {
   _convSQL = "";
   _convParm =null;
   aa_parms.clear();
}

private void sort2Parm(boolean a_clear) {
   _convParm =aa_parms.toArray();
   if (a_clear) aa_parms.clear();
}
private void addSql(String col, String sql1) {
   if (_insert) {
      _sql1.append(col+" ,");
      _sql2.append(sql1+" ,");
   }
   else if (_update) {
      _sql1.append(col+" ="+sql1+" ,");
   }
}
private void addSql(String col) {
   if (_insert) {
      _sql1.append(col+" ,");
      _sql2.append("? ,");
   }
   else if (_update) {
      _sql1.append(col+" =? ,");
   }
}
//-sql-function-
public void funcSet(String col, String sql1, Object s1) {
   if (sql1.indexOf("?")>=0) {
      parmAdd(s1);
   }
   addSql(col,sql1);
}
public void parmYmd(String col) {
   addSql(col," to_char(sysdate,'yyyymmdd') ");
}
public void parmTime(String col) {
   addSql(col," to_char(sysdate,'hh24miss') ");
}
public void parmDtime(String col) {
   addSql(col," sysdate ");
}
public void parmDtime(String col, String s1) {
   parmAdd(s1);
   addSql(col," TIMESTAMP_FORMAT(?,'YYYYMMDDHH24MISS') ");
}

//-parm=?-
public void parmSet(String col, Object s1) {
   parmAdd(s1);
   addSql(col);
}
//-where-
public void whereRowid(String s1) {
   _sqlWhere.append(" where rowid=x'"+s1+"'");
}
public void whereParm(String sql1, Object s1) {
   if (sql1.indexOf("?") >=0) {
      parmAdd(s1);
   }
   if (!_update) return;
   _sqlWhere.append(" "+sql1);
}

public void modxxxSet(String mod_user, String mod_pgm) {
   if (_insert) {
      parmSet("mod_user",mod_user);
      funcSet("mod_time","sysdate","");
      parmSet("mod_pgm",mod_pgm);
      funcSet("mod_seqno","1","");
   }
   if (_update) {
      parmSet("mod_user",mod_user);
      funcSet("mod_time","sysdate","");
      parmSet("mod_pgm",mod_pgm);
      funcSet("mod_seqno","nvl(mod_seqno,0)+1","");
   }
}

private void parmAdd(Object s1) {
   aa_parms.add(s1);
}

}
