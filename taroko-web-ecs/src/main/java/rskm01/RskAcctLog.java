package rskm01;
/**
 * RSK 帳務記錄功能
 * 2019-1213   JH    UAT
 * 2018-1206:  JH    modify
 * V00.00	JH		2017-1128:
 */

import busi.FuncBase;
import ofcapp.DataBean;

public class RskAcctLog extends FuncBase {

private String isBusiDate = "", isVouchJobcode = "";
private final DataBean ds = new DataBean();

void initFunc(String aUser) {
   msgOK();

   strSql = "select business_date, vouch_date"
         + " from ptr_businday"
         + " where 1=1" + commSqlStr.rownum(1);
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return;
   isBusiDate = colStr("business_date");
   vouchDate(colStr("vouch_date"));
   if (empty(isBusiDate)) {
      errmsg("無法取得[營業日]");
      return;
   }

   //--
   strSql = "select substr('0'||gl_code,1,2) as gl_code"
         + ", dept_code as user_deptno"
         + " from ptr_dept_code"
         + " where dept_code in ("
         + " select usr_deptno from sec_user where usr_id =?"
         + " )"
   ;
   setString(1, aUser);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("select ptr_classcode notFind, kk=" + wp.loginDeptNo);
      return;
   }
   isVouchJobcode = colStr("gl_code");
   userDeptno(colStr("user_deptno"));
   if (empty(isVouchJobcode)) {
      return;
   }

}

//public int rskP0020_Approve() {
//	init_func();
//	//select_rsk_problem();
//
//   hh.table_id ="PRBL";
//   hh.ctrl_seqno =wp.item_ss("A.ctrl_seqno");
//   hh.bin_type =wp.item_ss("A.bin_type");
//   hh.rsk_status ="30";
//   hh.reference_no =wp.item_ss("A.reference_no");
//   hh.debit_flag =wp.item_ss("A.debit_flag");
//   hh.card_no =wp.item_ss("A.card_no");
//   hh.acaj_flag =wp.item_ss("A.acaj_flag");
//
//	dbInsert();
//	if (rc!=1) {
//		errmsg("insert rsk_acct_log error kk=PRBL-"+hh.ctrl_seqno);
//	}
//
//	return rc;
//}

public int dbInsert() {
   msgOK();

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("rsk_acct_log");

   spp.ppstr("ctrl_seqno", ds.colStr("ctrl_seqno"));
   spp.ppstr("bin_type", ds.colStr("bin_type"));
   spp.ppstr("rsk_status", ds.colStr("rsk_status"));
   spp.ppstr("table_id", ds.colStr("table_id"));
   spp.ppstr("reference_no", ds.colStr("reference_no"));
   spp.ppstr("debit_flag", ds.colStr("debit_flag"));
   spp.ppstr("card_no", ds.colStr("card_no"));
   spp.ppstr("clo_result", ds.colStr("clo_result"));
   spp.ppstr("acaj_flag", ds.colStr("acaj_flag"));
   spp.ppstr("rskok_flag", ds.colStr("rskok_flag"));
   spp.ppstr("vouch_flag", ds.colStr("vouch_flag"));
   if (ds.colEq("vouch_flag", "Y")) {
      spp.ppstr("std_vouch_cd", ds.colStr("std_vouch_cd"));
      spp.ppstr("vouch_date", ds.colStr("vouch_date"));
      spp.ppstr("user_deptno", ds.colStr("user_deptno"));
   }
   spp.ppstr("vouch_proc_flag", ds.colStr("vouch_proc_flag"));
   spp.ppstr("vouch_proc_date", ds.colStr("vouch_proc_date"));
   spp.ppstr("new_prbl_flag", ds.colStr("new_prbl_flag"));
   spp.ppstr("oversea_fee_flag", ds.colStr("oversea_fee_flag"));
   spp.ppstr("vouch_merge_flag", ds.colStr("vouch_merge_flag"));
   spp.ppstr("gl_memo3", ds.colStr("gl_memo3"));

   if (empty(ds.colStr("crt_date")))
      spp.ppymd("crt_date");
   else spp.ppstr("crt_date", ds.colStr("crt_date"));
   if (empty(ds.colStr("crt_time")))
      spp.pptime("crt_time");
   else spp.ppstr("crt_time", ds.colStr("crt_time"));
   if (empty(ds.colStr("crt_user")))
      spp.ppstr("crt_user", modUser);
   else spp.ppstr("crt_user", ds.colStr("crt_user"));

   spp.modxxx(modUser, modPgm);
   //wp.ddd(spp.sql_stmt(), spp.sql_parm());
   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum < 0) {
      errmsg("delete rsk_acct_log error");
   }
   return rc;
}

public int dbDelete(String aCtrlSeqno, String aBinType, String aStatus, String aTable) {
   strSql = "delete rsk_acct_log"
         + " where ctrl_seqno =?"
         + " and bin_type =?"
         + " and rsk_status =?"
         + " and table_id =?"
         + " and vouch_proc_flag <>'Y'";

   Object[] param = {aCtrlSeqno, aBinType, aStatus, aTable};   
   sqlExec(strSql, param);
   if (sqlRowNum < 0) {
      errmsg("delete rsk_acct_log error");
      return -1;
   }
   return sqlRowNum;
}

public void initData() {
   ds.dataClear();
   ds.colSet("debit_flag", "N");
   ds.colSet("acaj_flag", "N");
   ds.colSet("rskok_flag", "N");
   ds.colSet("vouch_flag", "N");
   ds.colSet("vouch_proc_flag", "N");
   ds.colSet("new_prbl_flag", "N");
   ds.colSet("oversea_fee_flag", "N");
   ds.colSet("vouch_merge_flag", "N");
}

public void ctrlSeqno(String s1) {
   ds.colSet("ctrl_seqno", s1);
}

public void binType(String s1) {
   ds.colSet("bin_type", s1);
}

public void rskStatus(String s1) {
   ds.colSet("rsk_status", s1);
}

public void tableId(String s1) {
   ds.colSet("table_id", s1);
}

public void referenceNo(String s1) {
   ds.colSet("reference_no", s1);
}

public void debitFlag(String s1) {
   ds.colSet("debit_flag", s1);
}

public void cardNo(String s1) {
   ds.colSet("card_no", s1);
}

public void cloResult(String s1) {
   ds.colSet("clo_result", s1);
}

public void acajFlag(String s1) {
   ds.colSet("acaj_flag", s1);
}

public void rskokFlag(String s1) {
   ds.colSet("rskok_flag", s1);
}

public void vouchFlag(String s1) {
   ds.colSet("vouch_flag", s1);
}

public void stdVouchCd(String s1) {
   ds.colSet("std_vouch_cd", s1);
   if (!empty(s1))
      vouchFlag("Y");
}

public void vouchDate(String s1) {
   ds.colSet("vouch_date", s1);
}

public void userDeptno(String s1) {
   ds.colSet("user_deptno", s1);
}

public void vouchProcFlag(String s1) {
   ds.colSet("vouch_proc_flag", s1);
}

public void vouchProcDate(String s1) {
   ds.colSet("vouch_proc_date", s1);
}

public void newPrblFlag(String s1) {
   ds.colSet("new_prbl_flag", s1);
}

public void overseaFeeFlag(String s1) {
   ds.colSet("oversea_fee_flag", s1);
}

public void vouchMergeFlag(String s1) {
   ds.colSet("vouch_merge_flag", s1);
}

public void glMemo3(String s1) {
   ds.colSet("gl_memo3", s1);
}

public void crtDate(String s1) {
   ds.colSet("crt_date", s1);
}

public void crtTime(String s1) {
   ds.colSet("crt_time", s1);
}

public void crtUser(String s1) {
   ds.colSet("crt_user", s1);
}

public void rowid(String s1) {
   ds.colSet("rowid", s1);
}

}
