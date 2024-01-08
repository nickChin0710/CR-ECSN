package rskm01;
/**
 * 2022-0113   JH
 * 2021-1005   JH    job_code=dept_no[1,2]
 * 2020-0512   JH    clo=44:  prb_amount>>mcht_repay{問交結案金額}
 * 2019-1128   Alex  bug fixed
 * 2019-1127   JH    gl_user
 * 2019-0703   JH    modify
 * V2018-1204:    JH    Testing
 * V2018-1005:		JH		bugfix
 * V00.00	JH		2017-1128:
 * RSK: act_acaj 共用功能
 */

import busi.FuncBase;


public class ActAcaj extends FuncBase {
busi.CommBusi zzcomm = new busi.CommBusi();

String glCode = "", isBusiDate = "", isDeptNo = "", glUser = "";

private String pSeqno = "";
private String acctType = "";
private String adjustType = "";
private String referenceNo = "";
private String postDate = "";
private double orginalAmt = 0;
private double drAmt = 0;
private double crAmt = 0;
private double befAmt = 0;
private double aftAmt = 0;
private double befDAmt = 0;
private double aftDAmt = 0;
private String acctCode = "";
private String functionCode = "";
private String cardNo = "";
private String cashType = "";
private String valueType = "";
private String transAcctType = "";
private String transAcctKey = "";
private String interestDate = "";
private String adjReasonCode = "";
private String adjComment = "";
private String cDebtKey = "";
private String debitItem = "";
private String jrnlDate = "";
private String jrnlTime = "";
private String paymentType = "";
private String batchNoNew = "";
private String processFlag = "";
private String jobCode = "";
private String vouchJobCode = "";
private String mchtNo = "";
private String currCode = "";
private double dcOrginalAmt = 0;
private double dcDrAmt = 0;
private double dcCrAmt = 0;
private double dcBefAmt = 0;
private double dcAftAmt = 0;
private double dcBefDAmt = 0;
private double dcAftDAmt = 0;
private String crtUser = "";
private String aprFlag = "";
private String rskCtrlSeqno = "";

public void setRskCtrlSeqno(String strName) {
	rskCtrlSeqno = strName;
}
private void getBusiDate() {
   strSql = "select business_date from ptr_businday"
         + " where 1=1" + commSqlStr.rownum(1);
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return;
   isBusiDate = colStr("business_date");
}

private int getVouchJobCode(String userId) {
   glCode = "";
   isDeptNo = "";
   String lsUserId =userId;
   //-系統-
   if (empty(userId) || eq(userId,"system")) {
	   lsUserId =modUser;
   }

   strSql = "select substr('0'||gl_code,1,2) as vouch_job_code"
         + ", gl_code"
         + ", dept_code as user_deptno"
         + " from ptr_dept_code"
         + " where dept_code in ("
         + " select usr_deptno from sec_user where usr_id =?"
         + " )"
   ;
   setParm(1, lsUserId);

   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
//      errmsg("select ptr_dept_code notFind, kk=" + wp.login_deptNo);
      return 0;
   }
   glCode = colStr("gl_code");
   isDeptNo = colStr("user_deptno");
   return 1;
}

void initFunc() {
   this.msgOK();
   getBusiDate();
   if (empty(isBusiDate)) {
      errmsg("無法取得[營業日]");
      return;
   }

   int liRc =getVouchJobCode(glUser);
   if (liRc !=1) {
	   getVouchJobCode(modUser);
   }
   if (empty(glCode)) {
      errmsg("無法取得部門代碼(gl_code) user[%s] err=" + mesg(), glUser);
      return;
   }
}

public int rskp0020Approve() {
   String lsRefno = wp.itemStr("A.reference_no");
   //-add_user-
   glUser = "";
   String sql1 = "select add_user from rsk_problem" +
         " where reference_no=?" +
         " order by reference_seq" + commSqlStr.rownum(1);
   sqlSelect(sql1, lsRefno);
   glUser = colStr("add_user");

   //--
   initFunc();
   if (rc != 1) return rc;

   selectActDebt(lsRefno);
   if (rc != 1) return rc;

   if (colNeq("curr_code", wp.itemStr("A.curr_code"))) {
      errmsg("帳務[debt]幣別 與 問交幣別  不同!!");
      return -1;
   }

   if (wp.num("A.dc_prb_amount") > colNum("dc_d_avail_bal")) {
      errmsg("問交金額 > 可D數？！");
      return -1;
   }

   rskCtrlSeqno = zzcomm.rskCtrlSeqnoPrbl(
         wp.itemStr("A.ctrl_seqno")
         , wp.itemStr("A.bin_type")
         , wp.itemStr("A.prb_status"));
   double lm_amt = wp.num("A.prb_amount");
   double lm_dc_amt = wp.num("A.dc_prb_amount");
   
   dataInit();
   pSeqno = colStr("p_seqno");
   acctType = colStr("acct_type");
   referenceNo = wp.itemStr("A.reference_no");
   adjustType = wp.itemStr("A.adjust_type");
   postDate = isBusiDate;
   functionCode = "U";
//   valueType = "1";
   valueType = "2";
   aprFlag = "Y";
   cardNo = colStr("card_no");
   acctCode = colStr("acct_code");
   orginalAmt = colNum("beg_bal");
   drAmt = lm_amt;
   befAmt = colNum("end_bal");
   aftAmt = colNum("end_bal") - lm_amt;
   befDAmt = colNum("d_avail_bal");
   aftDAmt = colNum("d_avail_bal") - lm_amt;
   currCode = colStr("curr_code");
   dcOrginalAmt = colNum("dc_beg_bal");
   dcDrAmt = lm_dc_amt;
   dcBefAmt = colNum("dc_end_bal");
   dcAftAmt = colNum("dc_end_bal") - lm_dc_amt;
   dcBefDAmt = colNum("dc_d_avail_bal");
   dcAftDAmt = colNum("dc_d_avail_bal") - lm_dc_amt;
   jobCode = commString.left(isDeptNo,2);
   vouchJobCode = "0" + commString.left(glCode, 1);
//   rsk_ctrl_seqno = ls_rsk_ctrl_seqno;
   crtUser = nvl(glUser, modUser);

   dbInsert();

   return rc;
}

//void select_rsk_problem() {
//	daoTid ="A.";
//	strSql ="select reference_no,p_seqno"
//			+", dc_prb_amount"
//			+", dc_mcht_repay"
//			+" from rsk_problem"
//			+" where rowid =?"
//			;
//	this.setRowId(1,vars_ss("rowid"));
//	sqlSelect(strSql);
//	if (sqlRowNum<=0) {
//		errmsg("Act_acaj: rsk_problem not find");
//	}
//}

void selectActDebt(String aReferenceNo) {
   strSql = "SELECT beg_bal, end_bal, d_avail_bal,"
         + " card_no, acct_code, p_seqno, acct_type, "
         + " purchase_date,"
         + " uf_dc_amt2(beg_bal,dc_beg_bal) as dc_beg_bal,"
         + " uf_dc_amt2(end_bal,dc_end_bal) as dc_end_bal,"
         + " uf_dc_amt2(d_avail_bal,dc_d_avail_bal) as dc_d_avail_bal,"
         + " uf_dc_curr(curr_code) as curr_code"
         + " FROM act_debt"
         + " where reference_no =?" +
         " union " +
         "SELECT beg_bal, end_bal, d_avail_bal,"
         + " card_no, acct_code, p_seqno, acct_type, "
         + " purchase_date,"
         + " uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as dc_beg_bal,"
         + " uf_dc_amt(curr_code,end_bal,dc_end_bal) as dc_end_bal,"
         + " uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as dc_d_avail_bal,"
         + " uf_dc_curr(curr_code) as curr_code"
         + " FROM act_debt_hst"
         + " where reference_no =?";
   setString(aReferenceNo);
   setString(aReferenceNo);
   sqlSelect(strSql);
//	if (sqlRowNum<=0) {
//		strSql ="SELECT beg_bal, end_bal, d_avail_bal,"
//				+" card_no, acct_code, p_seqno, acct_type, "
//				+" purchase_date,"
//				+" uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as dc_beg_bal,"
//				+" uf_dc_amt(curr_code,end_bal,dc_end_bal) as dc_end_bal,"
//				+" uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as dc_d_avail_bal,"
//				+" uf_dc_curr(curr_code) as curr_code"
//				+" FROM act_debt_hst"
//				+" where reference_no =?";
//		setString(1,a_reference_no);
//		sqlSelect(strSql);
//	}

   if (sqlRowNum <= 0) {
      errmsg("select act_debt[_hst] not find; kk[%s]", aReferenceNo);
      return;
   }
}

void selectActDebt2(String aPSeqno, String aReferNo) {
   strSql = "select end_bal as t_end_bal"
         + ", uf_dc_amt(curr_code,end_bal,dc_end_bal) as t_dc_end_bal "
         + " from act_debt"
         + " where 1=1 and p_seqno =?"
         + " and dp_reference_no =?";  //-爭議款相關序號-
   sqlSelect(strSql, new Object[]{ aPSeqno,aReferNo});
   if (sqlRowNum <= 0) {
      strSql = "select end_bal as t_end_bal"
            + ", uf_dc_amt(curr_code,end_bal,dc_end_bal) as t_dc_end_bal "
            + " from act_debt"
            + " where reference_no =?"
            + " union select end_bal as t_end_bal"
            + ", uf_dc_amt(curr_code,end_bal,dc_end_bal) as t_dc_end_bal"
            + " from act_debt_hst where reference_no =?"
      ;
      sqlSelect(strSql, new Object[]{aReferNo, aReferNo});
   }
   if (sqlRowNum <= 0) {
      colSet("t_end_bal", "0");
      colSet("t_dc_end_bal", "0");
   }

}

public int rskP0030Comgl4(String aAdjType) {
   String lsRefno = wp.itemStr("A.reference_no");
   //-add_user-
   glUser = "";
   String sql1 = "select close_add_user"+
           ", ctrl_seqno, bin_type "+
           " from rsk_problem" +
         " where reference_no=?" +
         " order by reference_seq" + commSqlStr.rownum(1);
   sqlSelect(sql1, new Object[]{lsRefno});
   glUser = colStr("close_add_user");
//   //-1st次結案-
//   String ls_rsk_ctrl_seqno = zzcomm.rsk_ctrlSeqno_Prbl(colStr("ctrl_seqno")
//           , colStr("bin_type"), "801");

   initFunc();
   if (rc != 1) return rc;

   dataInit();

   referenceNo = lsRefno;
   adjustType = aAdjType;
   postDate = isBusiDate;
   functionCode = "U";
   aprFlag = "Y";
   jobCode = commString.left(isDeptNo,2);
   vouchJobCode = "0" + commString.left(glCode, 1);

   selectActDebt(wp.itemStr("A.reference_no"));
   if (rc != 1) return rc;

   acctType = colStr("acct_type");
   pSeqno = colStr("p_seqno");

   double lmRepay =wp.num("A.mcht_repay");
   double lmDcRepay =wp.num("A.dc_mcht_repay");

   if (eqIgno(adjustType, "DP03")) {
      selectActDebt2(pSeqno, referenceNo);
      double lmEndBal = colNum("t_dc_end_bal");
      if (lmDcRepay > lmEndBal) {
         errmsg("特店自動退款[結算]金額 > 期末[外幣]餘額");
         return -1;
      }
   }

   cardNo = colStr("card_no");
   orginalAmt = colNum("beg_bal");
   dcOrginalAmt = colNum("dc_beg_bal");
   acctCode = colStr("acct_code");

   if (pos("|DP01|DP04", aAdjType) > 0) {
      drAmt = colNum("d_avail_bal");
      befAmt = colNum("end_bal");
      aftAmt = colNum("end_bal") - colNum("d_avail_bal");
      befDAmt = colNum("d_avail_bal");
      //-DC-AMT-
      dcDrAmt = colNum("dc_d_avail_bal");
      dcBefAmt = colNum("dc_end_bal");
      dcAftAmt = colNum("dc_end_bal") - colNum("dc_d_avail_bal");
      dcBefDAmt = colNum("dc_d_avail_bal");
   }
   if (eqIgno(aAdjType, "DP04")) {
      cDebtKey = wp.itemStr("A.prb_glmemo3");
   }
   if (eqIgno(aAdjType, "DP03")) {
      drAmt = 0;
      crAmt = lmRepay;
      befAmt = colNum("end_bal");
      aftAmt = colNum("end_bal") + lmRepay;
      befDAmt = colNum("d_avail_bal");
      aftDAmt = colNum("d_avail_bal") + lmRepay;
      valueType = "3";
      dcDrAmt = 0;
      dcCrAmt = lmDcRepay;
      dcBefAmt = colNum("dc_end_bal");
      dcAftAmt = colNum("dc_end_bal") + lmDcRepay;
      dcBefDAmt = colNum("dc_d_avail_bal");
      dcAftDAmt = colNum("dc_d_avail_bal") + lmDcRepay;
   }

   if (eqIgno(aAdjType, "DP03-1")) {
      drAmt = 0;
      crAmt = lmRepay;
      befAmt = colNum("end_bal");
      aftAmt = colNum("end_bal") + lmRepay; //wp.num("A.prb_amount");
      befDAmt = colNum("d_avail_bal");
      aftDAmt = colNum("d_avail_bal") + lmRepay; //wp.num("A.prb_amount");
      valueType = "1";
      dcDrAmt = 0;
      dcCrAmt = lmDcRepay;
      dcBefAmt = colNum("dc_end_bal");
      dcAftAmt = colNum("dc_end_bal") + lmDcRepay; //wp.num("A.dc_prb_amount");
      dcBefDAmt = colNum("dc_d_avail_bal");
      dcAftDAmt = colNum("dc_d_avail_bal") + lmDcRepay; //wp.num("A.dc_prb_amount");
   }
   if (eqIgno(aAdjType, "DP03-2")) {
      drAmt = 0;
      crAmt = lmRepay;
      befAmt = colNum("end_bal");
      aftAmt = colNum("end_bal") + lmRepay; //wp.num("A.prb_amount");
      befDAmt = colNum("d_avail_bal");
      aftDAmt = colNum("d_avail_bal") + lmRepay; //wp.num("A.prb_amount");
      valueType = "2";
      dcDrAmt = 0;
      dcCrAmt = lmDcRepay;
      dcBefAmt = colNum("dc_end_bal");
      dcAftAmt = colNum("dc_end_bal") + lmDcRepay; //wp.num("A.dc_prb_amount");
      dcBefDAmt = colNum("dc_d_avail_bal");
      dcAftDAmt = colNum("dc_d_avail_bal") + lmDcRepay;  //wp.num("A.dc_prb_amount");
   }   
   
   if (eqIgno(aAdjType, "DP03-3")) {	   
	   drAmt = 0;
	   crAmt = lmRepay;
	   befAmt = colNum("end_bal");
	   aftAmt = colNum("end_bal") + lmRepay; //wp.num("A.prb_amount");
	   befDAmt = colNum("d_avail_bal");
	   aftDAmt = colNum("d_avail_bal") + lmRepay; //wp.num("A.prb_amount");
	   valueType = "3";
	   dcDrAmt = 0;
	   dcCrAmt = lmDcRepay;
	   dcBefAmt = colNum("dc_end_bal");
	   dcAftAmt = colNum("dc_end_bal") + lmDcRepay; //wp.num("A.dc_prb_amount");
	   dcBefDAmt = colNum("dc_d_avail_bal");
	   dcAftDAmt = colNum("dc_d_avail_bal") + lmDcRepay;  //wp.num("A.dc_prb_amount");
   }

   currCode = colStr("curr_code");
   //rsk_ctrl_seqno = ls_rsk_ctrl_seqno;
   crtUser = nvl(glUser, modUser);

   dbInsert();
   if (rc != 1) {
      errmsg("問交結案覆核: 新增 ACT_ACAJ.[%s]檔失敗, kk=%s", aAdjType, wp.itemStr("A.reference_no"));
   }
   return rc;
}

public int rskP0030DP02() {
   String lsRefno = wp.itemStr("A.reference_no");
   //-add_user-
   glUser = "";
   String sql1 = "select close_add_user"+
           ", ctrl_seqno, bin_type "+
           " from rsk_problem" +
         " where reference_no=?" +
         " order by reference_seq" + commSqlStr.rownum(1);
   sqlSelect(sql1, new Object[]{lsRefno});
   glUser = colStr("close_add_user");

   initFunc();
   if (rc != 1) return rc;

   //-1st次結案=801, 2nd=802-
//   String ls_rsk_ctrl_seqno = zzcomm.rsk_ctrlSeqno_Prbl(colStr("ctrl_seqno")
//         , colStr("bin_type"), "801");

   dataInit();
   referenceNo = wp.itemStr("A.reference_no");
   adjustType = "DP02";
   postDate = isBusiDate;
   functionCode = "U";
   aprFlag = "Y";
   jobCode = commString.left(isDeptNo,2);
   vouchJobCode = "0" + commString.left(glCode, 1);

   selectActDebt(wp.itemStr("A.reference_no"));
   if (rc != 1) return rc;

   pSeqno = colStr("p_seqno");
   acctType = colStr("acct_type");
   cardNo = colStr("card_no");
   orginalAmt = colNum("beg_bal");
   acctCode = colStr("acct_code");
   dcOrginalAmt = colNum("dc_beg_bal");
   currCode = colStr("curr_code");
   //rsk_ctrl_seqno = ls_rsk_ctrl_seqno;

   crtUser = nvl(glUser, modUser);

   dbInsert();
   if (rc != 1) {
      errmsg("問交結案: 新增 ACT_ACAJ檔失敗, kk=" + wp.itemStr("A.reference_no"));
   }
   return rc;
}

void dbInsert() {
   String lsJobCode =commString.left(jobCode,2);

   busi.SqlPrepare spp = new busi.SqlPrepare();
   spp.sql2Insert("act_acaj");
   spp.ppstr("p_seqno", pSeqno);
   spp.ppstr("acct_type", acctType);
   spp.ppstr("adjust_type", strMid(adjustType, 0, 4));
   spp.ppstr("reference_no", referenceNo);
   spp.ppstr("post_date", postDate);
   spp.ppnum("orginal_amt", orginalAmt);
   spp.ppnum("dr_amt", drAmt);
   spp.ppnum("cr_amt", crAmt);
   spp.ppnum("bef_amt", befAmt);
   spp.ppnum("aft_amt", aftAmt);
   spp.ppnum("bef_d_amt", befDAmt);
   spp.ppnum("aft_d_amt", aftDAmt);
   spp.ppstr("acct_code", acctCode);
   spp.ppstr("function_code", functionCode);
   spp.ppstr("card_no", cardNo);
   spp.ppstr("cash_type", cashType);
   spp.ppstr("value_type", valueType);
   spp.ppstr("trans_acct_type", transAcctType);
   spp.ppstr("trans_acct_key", transAcctKey);
   spp.ppstr("interest_date", interestDate);
   spp.ppstr("adj_reason_code", adjReasonCode);
   spp.ppstr("adj_comment", adjComment);
   spp.ppstr("c_debt_key", cDebtKey);
   spp.ppstr("debit_item", debitItem);
   spp.ppstr("jrnl_date", jrnlDate);
   spp.ppstr("jrnl_time", jrnlTime);
   spp.ppstr("payment_type", paymentType);
   spp.ppstr("batch_no_new", batchNoNew);
   spp.ppstr("process_flag", processFlag);
   spp.ppstr("job_code", lsJobCode);
   spp.ppstr("vouch_job_code", vouchJobCode);
   spp.ppstr("mcht_no", mchtNo);
   spp.ppstr("curr_code", currCode);
   spp.ppnum("dc_orginal_amt", dcOrginalAmt);
   spp.ppnum("dc_dr_amt", dcDrAmt);
   spp.ppnum("dc_cr_amt", dcCrAmt);
   spp.ppnum("dc_bef_amt", dcBefAmt);
   spp.ppnum("dc_aft_amt", dcAftAmt);
   spp.ppnum("dc_bef_d_amt", dcBefDAmt);
   spp.ppnum("dc_aft_d_amt", dcAftDAmt);
   spp.ppymd("crt_date");
   spp.pptime("crt_time");
   spp.ppstr("crt_user", crtUser);
//	spp.ppstr("update_date",update_date);
//	spp.ppstr("update_user",update_user);
   spp.ppstr("apr_flag", aprFlag);
   if (eqIgno(aprFlag, "Y")) {
      spp.ppymd("apr_date");
      spp.ppstr("apr_user", modUser);
   }
   spp.ppstr("rsk_ctrl_seqno", rskCtrlSeqno);
   spp.modxxx(modUser, modPgm);

   sqlExec(spp.sqlStmt(), spp.sqlParm());
   if (sqlRowNum <= 0) {
      errmsg("Insert act_acaj error; kk[%s]",rskCtrlSeqno);
   }

}

public int dbDelete(String rsk_ctrl_seqno) {
   strSql = "delete act_acaj"
         + " where rsk_ctrl_seqno =?"
         + " and process_flag <>'Y'";
   setString(1, rsk_ctrl_seqno);
   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("delete act_acaj error");
      return -1;
   }
   return sqlRowNum;
}

void dataInit() {
   pSeqno = "";
   acctType = "";
   adjustType = "";
   referenceNo = "";
   postDate = "";
   orginalAmt = 0;
   drAmt = 0;
   crAmt = 0;
   befAmt = 0;
   aftAmt = 0;
   befDAmt = 0;
   aftDAmt = 0;
   acctCode = "";
   functionCode = "";
   cardNo = "";
   cashType = "";
   valueType = "";
   transAcctType = "";
   transAcctKey = "";
   interestDate = "";
   adjReasonCode = "";
   adjComment = "";
   cDebtKey = "";
   debitItem = "";
   jrnlDate = "";
   jrnlTime = "";
   paymentType = "";
   batchNoNew = "";
   processFlag = "N";
   jobCode = "";
   vouchJobCode = "";
   mchtNo = "";
   currCode = "";
   dcOrginalAmt = 0;
   dcDrAmt = 0;
   dcCrAmt = 0;
   dcBefAmt = 0;
   dcAftAmt = 0;
   dcBefDAmt = 0;
   dcAftDAmt = 0;
//   rsk_ctrl_seqno = "";
   //this.dateTime();
//	String crt_date        =this.sys_Date;
//	String crt_time        =this.sys_Time;
//	String crt_user        =mod_user;
//	String update_date     ="";
//	String update_user     ="";
//	String apr_flag        ="Y";
//	String apr_date        =sys_Date;
//	String apr_user        =mod_user;
}

}
