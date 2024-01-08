package com;
/** Sql.Insert/Update 功能二
 * 2020-0106   JH    initial
 * */
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class SqlParm2 {
private HashMap<Integer,Object> sortHash = new HashMap<Integer,Object>();

public String sql_from="";
private String _convSQL="";
private Object[] _convParm;
private int _parmIndx=0;
private int _audCode=0;
private String _tableName="";

public int ti=-1;

private StringBuffer _sql1=new StringBuffer("");
private StringBuffer _sql2=new StringBuffer("");
private StringBuffer _sqlWhere=new StringBuffer("");

public void insert(String a_tab) {
   _parmIndx =0;
   if (ti>0) return;
   _audCode =1;
   _tableName =a_tab;
   _sql1 =new StringBuffer("");
   _sql2 =new StringBuffer("");
}
public void update(String a_tab) {
   _parmIndx =0;
   if (ti>0) return;
   _audCode =2;
   _tableName =a_tab;
   _sql1 =new StringBuffer("");
   _sql2 =new StringBuffer("");
   _sqlWhere =new StringBuffer("");
}
public String get_convSQL() {
   _convSQL ="";
   if (_audCode ==1) {
      _convSQL ="insert into "+_tableName+" ( "+
      _sql1.substring(0,_sql1.length() -1)+" ) values ( "+
      _sql2.substring(0,_sql2.length()-1)+" )";
   }
   else if (_audCode ==2) {
      _convSQL ="update "+_tableName+" set "+
         _sql1.substring(0,_sql1.length()-1)+" "+
         _sqlWhere.toString();
   }
   sql_from =_convSQL;
   return _convSQL;
}
public Object[] get_convParm(boolean a_clear) {
   _parmIndx =0;
   sort_2Parm(a_clear);
   return _convParm;
}
public Object[] get_convParm() {
   _parmIndx =0;
   sort_2Parm(true);
   return _convParm;
}
public void clear() {
   _convSQL = "";
   _convParm =null;
   sortHash.clear();
   _parmIndx =0;
}

private void sort_2Parm(boolean a_clear) {
   _convParm = new Object[sortHash.size()];
   Object[] keys2 = sortHash.keySet().toArray();
   Arrays.sort(keys2);
   int ii=-1;
   for( Object sortData : keys2) {
      ii++;
      _convParm[ii] = sortHash.get(sortData);
   }

   if (a_clear)
      sortHash.clear();
}

private String nvl(String s1) {
   if (s1==null) return "";
   return s1.trim();
}
private void addSql(String col, String sql1) {
   if (ti >0) return;
   if (_audCode==1) {
      _sql1.append(col+" ,");
      _sql2.append(sql1+" ,");
   }
   else if (_audCode ==2) {
      _sql1.append(col+" ="+sql1+" ,");
   }
}
private void addSql(String col) {
   if (ti >0) return;
   if (_audCode==1) {
      _sql1.append(col+" ,");
      _sql2.append("? ,");
   }
   else if (_audCode ==2) {
      _sql1.append(col+" =? ,");
   }
}
//-sql-function-
public void aaa_func(String col, String sql1, Object s1) {
   if (sql1.indexOf("?")>=0) {
      parmAdd(s1);
   }
   addSql(col,sql1);
}
//public void aaFunc(String col, String sql1, double num1) {
//   if (sql1.indexOf("?")>=0) {
//      parmAdd(num1);
//   }
//   addSql(col,sql1);
//}
//public void aaFunc(String col, String sql1, int int1) {
//   if (sql1.indexOf("?")>=0) {
//      parmAdd(int1);
//   }
//   addSql(col,sql1);
//}

//-parm=?-
public void aaa(String col, Object s1) {
   parmAdd(s1);
   addSql(col);
}
//public void aaa(String col, double num1) {
//   parmAdd(num1);
//   addSql(col);
//}
//public void aaa(String col, int int1) {
//   parmAdd(int1);
//   addSql(col);
//}
//-where-
public void aaa_where(String sql1, Object s1) {
   if (sql1.indexOf("?") >=0) {
      parmAdd(s1);
   }
   if (_audCode !=2) return;
   _sqlWhere.append(" "+sql1);
}

public void aaa_modxxx(String mod_user, String mod_pgm) {
   if (_audCode==1) {
      aaa("mod_user",mod_user);
      aaa_func("mod_time","sysdate","");
      aaa("mod_pgm",mod_pgm);
      aaa_func("mod_seqno","1","");
   }
   if (_audCode ==2) {
      aaa("mod_user",mod_user);
      aaa_func("mod_time","sysdate","");
      aaa("mod_pgm",mod_pgm);
      aaa_func("mod_seqno","nvl(mod_seqno,0)+1","");
   }
}

private void parmAdd(Object s1) {
   _parmIndx++;
   sortHash.put(_parmIndx,s1);
}

public Object[] getConvParm() {
	   _parmIndx =0;
	   sort_2Parm(true);
	   return _convParm;
	}

}
