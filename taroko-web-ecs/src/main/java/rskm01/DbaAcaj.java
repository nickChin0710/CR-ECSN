package rskm01;
/**
 * RSK: dba_acaj 共用功能: V.2018-1205
 * 2022-0113   JH    9215: job_code.2byte
 * 2019-1127   JH    gl_user
 * 2018-1205:  JH    vouch_job_code
 * V00.00	JH		2017-1128
 */

import busi.FuncBase;


public class DbaAcaj extends FuncBase {
//busi.zzComm zzcomm = new busi.zzComm();

String glCode = "", isBusiDate = "", isDeptNo = "";
String glUser = "";
private String rskCtrlSeqno="";

public void setRskCtrlSeqno(String strName) {
   rskCtrlSeqno =strName;
}

private void getBusiDate() {
   strSql = "select business_date from ptr_businday"
         + " where 1=1" + commSqlStr.rownum(1);
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return;
   isBusiDate = colStr("business_date");
}

private void getVouchJobcode() {
   glCode = "";
   isDeptNo = "";
   strSql = "select substr('0'||gl_code,1,2) as vouch_job_code"
         + ", gl_code , dept_code as user_deptno"
         + " from ptr_dept_code"
         + " where dept_code in ("
         + " select usr_deptno from sec_user where usr_id =?"
         + " )";

   setParm(1, nvl(glUser, modUser));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("select ptr_dept_code notFind, user=[%s]", glUser);
      return;
   }
   glCode = colStr("gl_code");
   isDeptNo = colStr("user_deptno");
}

void initFunc() {
   this.msgOK();
   getBusiDate();
   if (empty(isBusiDate)) {
      errmsg("無法取得[營業日]");
      return;
   }
   getVouchJobcode();
   if (empty(glCode)) {
      errmsg("無法取得部門代碼(gl_code), err=" + mesg());
      return;
   }
}
private String rskCtrlSeqnoPrbl(String aCtrlSeqno, String aStatus) {
   return aCtrlSeqno+"-PR"+aStatus;
}
public int rskp0020Approve() {
   this.msgOK();
   String lsRefno = wp.itemStr("A.reference_no");
   strSql = "select add_user from rsk_problem" +
         " where reference_no =?" +
         " order by reference_seq " + commSqlStr.rownum(1);
   sqlSelect(strSql, lsRefno);
   glUser = colStr("add_user");

   initFunc();
   if (rc != 1)
      return rc;

   strSql = "SELECT beg_bal, end_bal, d_avail_bal,"
         + " card_no, acct_code, p_seqno, acct_type, "
         + " acct_no, purchase_date, mcht_no, "
         + " txn_code "
         + " FROM dba_debt"
         + " where reference_no =?";
   setString(1, lsRefno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      strSql = "SELECT beg_bal, end_bal, d_avail_bal,"
            + " card_no, acct_code, p_seqno, acct_type, "
            + " acct_no, purchase_date, mcht_no, "
            + " txn_code "
            + " FROM dba_debt_hst"
            + " where reference_no =?";
      setString(1, lsRefno);
      sqlSelect(strSql);
   }
   if (sqlRowNum <= 0) {
      errmsg("select dba_debt[_hst] not find; kk=" + wp.itemStr("A.reference_no"));
      return rc;
   }

   if (wp.itemNum("A.prb_amount") > colNum("d_avail_bal")) {
      errmsg("問交金額 > 可D數？！");
      return -1;
   }

   rskCtrlSeqno = rskCtrlSeqnoPrbl(wp.itemStr("A.ctrl_seqno"), wp.itemStr("A.prb_status"));
   String lsJobCode=commString.left(isDeptNo,2);
   double lm_amt = wp.itemNum("A.prb_amount");

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("dba_acaj");
   spp.ppymd("crt_date");
   spp.pptime("crt_time");
   spp.ppstr("p_seqno", colStr("p_seqno"));
   spp.ppstr("acct_type", colStr("acct_type"));
   spp.ppstr("reference_no", wp.itemStr("A.reference_no"));
   spp.ppstr("adjust_type", strMid(wp.itemStr("A.adjust_type"), 0, 4));
   spp.ppstr("post_date", isBusiDate);
   spp.ppstr("func_code", "U");
//   spp.ppstr("value_type", "1");
   spp.ppstr("value_type", "2");
   spp.ppstr("apr_flag", "Y");
   spp.ppymd("apr_date");
   spp.ppstr("apr_user", modUser);
   spp.ppstr("proc_flag", "N");
   spp.ppstr("card_no", colStr("card_no"));
   spp.ppstr("acct_code", colStr("acct_code"));
   spp.ppnum("orginal_amt", colNum("beg_bal"));
   spp.ppnum("dr_amt", lm_amt);
   spp.ppnum("bef_amt", colNum("end_bal"));
   spp.ppnum("aft_amt", colNum("end_bal") - lm_amt);
   spp.ppnum("bef_d_amt", colNum("d_avail_bal"));
   spp.ppnum("aft_d_amt", colNum("d_avail_bal") - lm_amt);
   spp.ppstr("job_code", lsJobCode);
   spp.ppstr("vouch_job_code", "0" + commString.left(glCode, 1));
   spp.ppstr("acct_no", colStr("acct_no"));
   spp.ppstr("txn_code", colStr("txn_code"));
   spp.ppstr("mcht_no", colStr("mcht_no"));
   spp.ppstr("purchase_date", colStr("purchase_date"));
   spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
   spp.modxxx(modUser, modPgm);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("新增 DBA_ACAJ 檔失敗, kk[%s]", rskCtrlSeqno);
   }
   
   //--一併寫手續費 dba_acaj
   daoTid = "fee.";
   strSql = " select A.reference_no , A.acct_code , B.beg_bal , B.end_bal , B.d_avail_bal , "
		  + " B.acct_no , B.txn_code , B.mcht_no , B.purchase_date "
		  + " from dbb_bill A join dba_debt B on A.reference_no = B.reference_no "
		  + " where reference_no_original = ? and A.acct_code ='PF' "
		  ;
   
   setString(1,wp.itemStr("A.reference_no"));
   sqlSelect(strSql);
   if(sqlRowNum <=0) {
	   daoTid = "fee.";
	   strSql = " select A.reference_no , A.acct_code , B.beg_bal , B.end_bal , B.d_avail_bal , "
			  + " B.acct_no , B.txn_code , B.mcht_no , B.purchase_date "
			  + " from dbb_bill A join dba_debt_hst B on A.reference_no = B.reference_no "
			  + " where reference_no_original = ? and A.acct_code ='PF' "
			  ;
	   
	   setString(1,wp.itemStr("A.reference_no"));
	   sqlSelect(strSql);
	   if(sqlRowNum <=0)
		   return rc;
   }
   
   spp.sql2Insert("dba_acaj");
   spp.ppymd("crt_date");
   spp.pptime("crt_time");
   spp.ppstr("p_seqno", colStr("p_seqno"));
   spp.ppstr("acct_type", colStr("acct_type"));
   spp.ppstr("reference_no", colStr("fee.reference_no"));
   spp.ppstr("adjust_type", strMid(wp.itemStr("A.adjust_type"), 0, 4));
   spp.ppstr("post_date", isBusiDate);
   spp.ppstr("func_code", "U");
   spp.ppstr("value_type", "2");
   spp.ppstr("apr_flag", "Y");
   spp.ppymd("apr_date");
   spp.ppstr("apr_user", modUser);
   spp.ppstr("proc_flag", "N");
   spp.ppstr("card_no", colStr("card_no"));
   spp.ppstr("acct_code", colStr("fee.acct_code"));
   spp.ppnum("orginal_amt", colNum("fee.beg_bal"));
   spp.ppnum("dr_amt", colNum("fee.beg_bal"));
   spp.ppnum("bef_amt", colNum("fee.end_bal"));
   spp.ppnum("aft_amt", 0);
   spp.ppnum("bef_d_amt", colNum("fee.d_avail_bal"));
   spp.ppnum("aft_d_amt", 0);
   spp.ppstr("job_code", lsJobCode);
   spp.ppstr("vouch_job_code", "0" + commString.left(glCode, 1));
   spp.ppstr("acct_no", colStr("fee.acct_no"));
   spp.ppstr("txn_code", colStr("fee.txn_code"));
   spp.ppstr("mcht_no", colStr("fee.mcht_no"));
   spp.ppstr("purchase_date", colStr("fee.purchase_date"));
   spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
   spp.modxxx(modUser, modPgm);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("新增 DBA_ACAJ 檔失敗, kk[%s]", rskCtrlSeqno);
   }
   
   return rc;
}

public int rskP0030Comgl5(String aAdjType) {
   this.msgOK();

   String lsRefno = wp.itemStr("A.reference_no");
   strSql = "select close_add_user"+
           ", ctrl_seqno, bin_type, prb_status"+
           " from rsk_problem" +
         " where reference_no =?" +
         " order by reference_seq " + commSqlStr.rownum(1);
   sqlSelect(strSql, lsRefno);
   glUser = colStr("close_add_user");
//   String ls_rsk_seqno = zzcomm.rsk_ctrlSeqno_Prbl(
//           colStr("ctrl_seqno")
//           , colStr("bin_type"), "801");

   initFunc();
   if (rc != 1)
      return rc;

   double aAmt1 = wp.num("A.prb_amount");
   double aAmt2 = wp.num("A.mcht_repay");

   strSql = "select beg_bal, end_bal, d_avail_bal"
         + ", acct_code, card_no, p_seqno , acct_no "
         + " from dba_debt"
         + " where reference_no =?";
   this.setString(1, lsRefno);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      strSql = "select beg_bal, end_bal, d_avail_bal"
            + ", acct_code, card_no, p_seqno , acct_no "
            + " from dba_debt_hst"
            + " where reference_no =?";
      this.setString(1, wp.itemStr("A.reference_no"));
      sqlSelect(strSql);
   }
   if (sqlRowNum <= 0) {
      colSet("beg_bal", 0);
      colSet("end_bal", 0);
      colSet("d_avail_bal", 0);
   }

   String lsJobCode=commString.left(isDeptNo,2);

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("dba_acaj");
   spp.ppymd("crt_date");
   spp.pptime("crt_time");
   spp.ppstr("acct_type", wp.itemStr("A.acct_type"));
   spp.ppstr("p_seqno", wp.itemStr("A.p_seqno"));
   spp.ppstr("reference_no", wp.itemStr("A.reference_no"));
   spp.ppstr("adjust_type", strMid(aAdjType, 0, 4));
   spp.ppstr("post_date", isBusiDate);
   spp.ppstr("func_code", "U");
   spp.ppstr("apr_flag", "Y");
   spp.ppymd("apr_date");
   spp.ppstr("apr_user", modUser);
   spp.ppstr("proc_flag", "N");
   spp.ppstr("acct_no", colStr("acct_no"));
   spp.ppstr("txn_code", wp.itemStr("A.txn_code"));
   spp.ppstr("mcht_no", wp.itemStr("A.mcht_no"));
   spp.ppstr("purchase_date", wp.itemStr("A.purchase_date"));
   spp.ppstr("card_no", wp.itemStr("A.card_no"));
   spp.ppstr("job_code", lsJobCode);
   spp.ppstr("vouch_job_code", "0" + commString.left(glCode, 1));
   spp.ppnum("orginal_amt", colNum("beg_bal"));
   spp.ppstr("acct_code", colStr("acct_code"));
   if (pos("|DP01,DP04", aAdjType) > 0) {
      spp.ppnum("dr_amt", colNum("d_avail_bal"));
      spp.ppnum("bef_amt", colNum("end_bal"));
      spp.ppnum("aft_amt", colNum("end_bal") - colNum("d_avail_bal"));
      spp.ppnum("bef_d_amt", colNum("d_avail_bal"));
   }
   if (pos("|DP04", aAdjType) > 0) {
      spp.ppstr("c_debt_key", wp.itemStr("A.prb_glmemo3"));
   }
   if (pos("|DP03", aAdjType) > 0) {
      spp.ppnum("dr_amt", 0);
      spp.ppnum("cr_amt", aAmt2);
      spp.ppnum("bef_amt", colNum("end_bal"));
      spp.ppnum("aft_amt", colNum("end_bal") + aAmt2);
      spp.ppnum("bef_d_amt", colNum("d_avail_bal"));
      spp.ppnum("aft_d_amt", colNum("d_avail_bal") + aAmt2);
      spp.ppstr("value_type", "2");
   }
   if (eqAny("DP03-1", aAdjType)) {
      spp.ppnum("dr_amt", 0);
      spp.ppnum("cr_amt", aAmt1);
      spp.ppnum("bef_amt", colNum("end_bal"));
      spp.ppnum("aft_amt", colNum("end_bal") + aAmt1);
      spp.ppnum("bef_d_amt", colNum("d_avail_bal"));
      spp.ppnum("aft_d_amt", colNum("d_avail_bal") + aAmt1);
      spp.ppstr("value_type", "1");
   }
   if (eqAny("DP03-2", aAdjType)) {
      spp.ppnum("dr_amt", 0);
      spp.ppnum("cr_amt", aAmt1);
      spp.ppnum("bef_amt", colNum("end_bal"));
      spp.ppnum("aft_amt", colNum("end_bal") + aAmt1);
      spp.ppnum("bef_d_amt", colNum("d_avail_bal"));
      spp.ppnum("aft_d_amt", colNum("d_avail_bal") + aAmt1);
      spp.ppstr("value_type", "2");
   }
   spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
   spp.modxxx(modUser, modPgm);
   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("新增 dba_ACAJ 檔失敗; kk[%s]",rskCtrlSeqno);
   }
   
   //--一併寫手續費 dba_acaj
   daoTid = "fee.";
   strSql = " select A.reference_no , A.acct_code , B.beg_bal , B.end_bal , B.d_avail_bal , "
		  + " B.acct_no , B.txn_code , B.mcht_no , B.purchase_date "
		  + " from dbb_bill A join dba_debt B on A.reference_no = B.reference_no "
		  + " where reference_no_original = ? and A.acct_code ='PF' "
		  ;
   
   setString(1,wp.itemStr("A.reference_no"));
   sqlSelect(strSql);
   if(sqlRowNum <=0) {
	   daoTid = "fee.";
	   strSql = " select A.reference_no , A.acct_code , B.beg_bal , B.end_bal , B.d_avail_bal , "
			  + " B.acct_no , B.txn_code , B.mcht_no , B.purchase_date "
			  + " from dbb_bill A join dba_debt_hst B on A.reference_no = B.reference_no "
			  + " where reference_no_original = ? and A.acct_code ='PF' "
			  ;
	   
	   setString(1,wp.itemStr("A.reference_no"));
	   sqlSelect(strSql);
	   if(sqlRowNum <=0)
		   return rc;
   }
   
   spp.sql2Insert("dba_acaj");
   spp.ppymd("crt_date");
   spp.pptime("crt_time");
   spp.ppstr("p_seqno", wp.itemStr("A.p_seqno"));
   spp.ppstr("acct_type", wp.itemStr("A.acct_type"));
   spp.ppstr("reference_no", colStr("fee.reference_no"));
   spp.ppstr("adjust_type", strMid(aAdjType, 0, 4));
   spp.ppstr("post_date", isBusiDate);
   spp.ppstr("func_code", "U");
   spp.ppstr("value_type", "1");
   spp.ppstr("apr_flag", "Y");
   spp.ppymd("apr_date");
   spp.ppstr("apr_user", modUser);
   spp.ppstr("proc_flag", "N");
   spp.ppstr("card_no", colStr("card_no"));
   spp.ppstr("acct_code", colStr("fee.acct_code"));
   spp.ppnum("orginal_amt", colNum("fee.beg_bal"));
   spp.ppnum("dr_amt", 0);
   spp.ppnum("cr_amt", colNum("fee.beg_bal"));
   spp.ppnum("bef_amt", colNum("fee.end_bal"));
   spp.ppnum("aft_amt", colNum("fee.end_bal")+colNum("fee.beg_bal"));
   spp.ppnum("bef_d_amt", colNum("fee.d_avail_bal"));
   spp.ppnum("aft_d_amt", colNum("fee.d_avail_bal")+colNum("fee.beg_bal"));
   spp.ppstr("job_code", lsJobCode);
   spp.ppstr("vouch_job_code", "0" + commString.left(glCode, 1));
   spp.ppstr("acct_no", colStr("fee.acct_no"));
   spp.ppstr("txn_code", colStr("fee.txn_code"));
   spp.ppstr("mcht_no", colStr("fee.mcht_no"));
   spp.ppstr("purchase_date", colStr("fee.purchase_date"));
   spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
   spp.modxxx(modUser, modPgm);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("新增 DBA_ACAJ 檔失敗, kk[%s]", rskCtrlSeqno);
   }
   
   return rc;
}

public int rskP0030Insert(String aAdjType) {
//   String ls_rsk_seqno = zzcomm.rsk_ctrlSeqno_Prbl(wp.itemStr("A.ctrl_seqno")
//         , wp.itemStr("A.bin_type"), "801");

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("dba_acaj");
   spp.ppymd("crt_date");
   spp.pptime("crt_time");
   spp.ppstr("reference_no", wp.itemStr("A.reference_no"));
   spp.ppstr("adjust_type", strMid(aAdjType, 0, 4));
   spp.ppstr("post_date", isBusiDate);
   spp.ppstr("func_code", "U");
   spp.ppstr("card_no", wp.itemStr("A.card_no"));
   spp.ppstr("p_seqno", wp.itemStr("A.p_seqno"));
   spp.ppstr("acct_type", wp.itemStr("A.acct_type"));
   spp.ppstr("acct_no", wp.itemStr("A.debit_acct_no"));
   spp.ppstr("txn_code", wp.itemStr("A.txn_code"));
   spp.ppstr("mcht_no", wp.itemStr("A.mcht_no"));
   spp.ppstr("purchase_date", wp.itemStr("A.purchase_date"));
   spp.ppstr("apr_flag", "Y");
   spp.ppymd("apr_date");
   spp.ppstr("apr_user", modUser);
   spp.ppstr("proc_flag", "N");
   if (eqAny("RE10", aAdjType)) {
      //-解圈金額-
      spp.ppnum("orginal_amt", wp.num("A.dest_amt"));
   }
   else if (eqAny(aAdjType, "RE20")) {
      //-結案金額-
      spp.ppnum("dr_amt", wp.num("A.mcht_repay"));
      spp.ppstr("vouch_flag", "N");
   }
   spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
   spp.modxxx(modUser, modPgm);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("新增 dba_ACAJ 檔失敗; kk[%s]",rskCtrlSeqno);
   }

   return rc;
}

public int rskp0030VdBack(String adType , String rfNo) {	
	this.msgOK();
	String lsRefno = rfNo;
	strSql = "select add_user from rsk_problem" +
			 " where reference_no =?" +
	         " order by reference_seq " + commSqlStr.rownum(1);
	sqlSelect(strSql, lsRefno);
	glUser = colStr("add_user");

	initFunc();
	if (rc != 1)		
	    return rc;

	strSql = "SELECT beg_bal, end_bal, d_avail_bal,"
	       + " card_no, acct_code, p_seqno, acct_type, "
	       + " acct_no, purchase_date, mcht_no, "
	       + " txn_code "
	       + " FROM dba_debt"
	       + " where reference_no =?";
	setString(1, wp.itemStr("A.reference_no"));
	sqlSelect(strSql);
	if (sqlRowNum <= 0) {		
	    strSql = "SELECT beg_bal, end_bal, d_avail_bal,"
	           + " card_no, acct_code, p_seqno, acct_type, "
	           + " acct_no, purchase_date, mcht_no, "
	           + " txn_code "
	           + " FROM dba_debt_hst"
	           + " where reference_no =?";
	    setString(1, wp.itemStr("A.reference_no"));
	    sqlSelect(strSql);
	}
	if (sqlRowNum <= 0) {		
	    errmsg("select dba_debt[_hst] not find; kk=" + wp.itemStr("A.reference_no"));
	    return rc;
	}

	if (wp.itemNum("A.prb_amount") > colNum("d_avail_bal")) {
	    errmsg("問交金額 > 可D數？！");
	    return -1;
	}

//	rskCtrlSeqno = rskCtrlSeqnoPrbl(wp.itemStr("A.ctrl_seqno"), wp.itemStr("A.prb_status"));
	String lsJobCode=commString.left(isDeptNo,2);
	double lm_amt = wp.itemNum("A.prb_amount");

	busi.SqlPrepare spp = new busi.SqlPrepare();
	spp.sql2Insert("dba_acaj");
	spp.ppymd("crt_date");
	spp.pptime("crt_time");
	spp.ppstr("p_seqno", colStr("p_seqno"));
	spp.ppstr("acct_type", colStr("acct_type"));
	spp.ppstr("reference_no", rfNo);
	spp.ppstr("adjust_type", strMid(adType, 0, 4));
	spp.ppstr("post_date", isBusiDate);
	spp.ppstr("func_code", "U");	
	spp.ppstr("value_type", "2");
	spp.ppstr("apr_flag", "Y");
	spp.ppymd("apr_date");
	spp.ppstr("apr_user", modUser);
	spp.ppstr("proc_flag", "N");
	spp.ppstr("card_no", colStr("card_no"));
	spp.ppstr("acct_code", colStr("acct_code"));
	spp.ppnum("orginal_amt", colNum("beg_bal"));
	spp.ppnum("dr_amt", lm_amt);
	spp.ppnum("bef_amt", colNum("end_bal"));
	spp.ppnum("aft_amt", colNum("end_bal") - lm_amt);
	spp.ppnum("bef_d_amt", colNum("d_avail_bal"));
	spp.ppnum("aft_d_amt", colNum("d_avail_bal") - lm_amt);
	spp.ppstr("job_code", lsJobCode);
	spp.ppstr("vouch_job_code", "0" + commString.left(glCode, 1));
	spp.ppstr("acct_no", colStr("acct_no"));
	spp.ppstr("txn_code", colStr("txn_code"));
	spp.ppstr("mcht_no", colStr("mcht_no"));
	spp.ppstr("purchase_date", colStr("purchase_date"));
	spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
	spp.modxxx(modUser, modPgm);
    sqlExec(spp.sqlStmt(), spp.sqlParm());
    if (sqlRowNum <= 0) {    	
        errmsg("新增 DBA_ACAJ 檔失敗, kk[%s]", rskCtrlSeqno);
	}
    
  //--一併寫手續費 dba_acaj
    daoTid = "fee.";
    strSql = " select A.reference_no , A.acct_code , B.beg_bal , B.end_bal , B.d_avail_bal , "
 		  + " B.acct_no , B.txn_code , B.mcht_no , B.purchase_date "
 		  + " from dbb_bill A join dba_debt B on A.reference_no = B.reference_no "
 		  + " where reference_no_original = ? and A.acct_code ='PF' "
 		  ;
    
    setString(1,rfNo);
    sqlSelect(strSql);
    if(sqlRowNum <=0) {
 	   daoTid = "fee.";
 	   strSql = " select A.reference_no , A.acct_code , B.beg_bal , B.end_bal , B.d_avail_bal , "
 			  + " B.acct_no , B.txn_code , B.mcht_no , B.purchase_date "
 			  + " from dbb_bill A join dba_debt_hst B on A.reference_no = B.reference_no "
 			  + " where reference_no_original = ? and A.acct_code ='PF' "
 			  ;
 	   
 	   setString(1,rfNo);
 	   sqlSelect(strSql);
 	   if(sqlRowNum <=0)
 		   return rc;
    }
    
    spp.sql2Insert("dba_acaj");
    spp.ppymd("crt_date");
    spp.pptime("crt_time");
    spp.ppstr("p_seqno", colStr("p_seqno"));
    spp.ppstr("acct_type", colStr("acct_type"));
    spp.ppstr("reference_no", colStr("fee.reference_no"));
    spp.ppstr("adjust_type", strMid(adType, 0, 4));
    spp.ppstr("post_date", isBusiDate);
    spp.ppstr("func_code", "U");
    spp.ppstr("value_type", "2");
    spp.ppstr("apr_flag", "Y");
    spp.ppymd("apr_date");
    spp.ppstr("apr_user", modUser);
    spp.ppstr("proc_flag", "N");
    spp.ppstr("card_no", colStr("card_no"));
    spp.ppstr("acct_code", colStr("fee.acct_code"));
    spp.ppnum("orginal_amt", colNum("fee.beg_bal"));
    spp.ppnum("dr_amt", colNum("fee.beg_bal"));
    spp.ppnum("bef_amt", colNum("fee.end_bal"));
    spp.ppnum("aft_amt", 0);
    spp.ppnum("bef_d_amt", colNum("fee.d_avail_bal"));
    spp.ppnum("aft_d_amt", 0);
    spp.ppstr("job_code", lsJobCode);
    spp.ppstr("vouch_job_code", "0" + commString.left(glCode, 1));
    spp.ppstr("acct_no", colStr("fee.acct_no"));
    spp.ppstr("txn_code", colStr("fee.txn_code"));
    spp.ppstr("mcht_no", colStr("fee.mcht_no"));
    spp.ppstr("purchase_date", colStr("fee.purchase_date"));
    spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
    spp.modxxx(modUser, modPgm);

    sqlExec(spp.sqlStmt(), spp.sqlParm());
    if (sqlRowNum <= 0) {
       errmsg("新增 DBA_ACAJ 檔失敗, kk[%s]", rskCtrlSeqno);
    }
    
	return rc;
}

public int dbDelete(String rskCtrlSeqno) {
   strSql = "delete dba_acaj"
         + " where rsk_ctrl_seqno =?"
         + " and proc_flag <>'Y'";
   setString(1, rskCtrlSeqno);
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete dba_acaj error, kk=" + rskCtrlSeqno);
      return -1;
   }
   return sqlRowNum;
}

}
