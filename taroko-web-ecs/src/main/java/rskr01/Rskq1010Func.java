package rskr01;

/**
 * 2019-0106   JH    initial
 */
public class Rskq1010Func extends busi.FuncAction {
public int ilCnt = 0;
public int wpRr = -1;
String chgStatus = "";

@Override
public void dataCheck() {
   if (varEmpty("reference_no")) {
      errmsg("帳單流水號: 不可空白");
      return;
   }
}

@Override
public int dbInsert() {
   return 0;
}

@Override
public int dbUpdate() {
   return 0;
}

@Override
public int dbDelete() {
   return 0;
}

@Override
public int dataProc() {
   dataCheck();
   if (rc != 1)
      return rc;

   selectRskProblem();
   selectRskReceipt();
   selectRskChgback();
   selectRskPrecompl();
   selectRskPrearbit();
   return 1;
}

private int selectRskProblem() {
		/*
		ex_s1 	= ctrl_seqno
      ex_s2		= bin_type
      ex_s3 	= prb_status
      ex_s4 	= nvl(close_apr_date,add_apr_date)
      ex_s5    = prb_mark+'-'+prb_src_code+'-'+prb_status
      ex_s7		= db.clo_result
      ex_s10	= db.prb_comment
			*/

   strSql = "select "
         + " prb_status ,"
         + " add_apr_date ,"
         + " ctrl_seqno ,"
         + " bin_type ,"
         + " prb_src_code ,"
         + " prb_mark ,"
         + " prb_reason_code ,"
         + " prb_comment ,"
         + " add_user ,"
         + " prb_amount ,"
         + " close_apr_date ,"
         + " clo_result ,"
         + " close_add_user ,"
         + " dc_prb_amount ,"
         + " close_apr_date_2 ,"
         + " clo_result_2 ,"
         + " close_add_user_2 "
         + " from rsk_problem "
         + " where reference_no = ? "
         + " order by reference_seq " //+zzsql.rownum(1)
   ;

   busi.DataSet dsPrbl = new busi.DataSet();
   dsPrbl.colList = this.sqlQuery(strSql, new Object[]{varsStr("reference_no")});
   while (dsPrbl.listNext()) {
      String lsStatus = dsPrbl.colStr("prb_status");
      if (commString.strToInt(lsStatus) >= 30) {
         wpRr++;
         if (wpRr < 9) {
            wp.colSet(wpRr, "ser_num", "0" + (wpRr + 1));
         }
         else {
            wp.colSet(wpRr, "ser_num", "" + (wpRr + 1));
         }
         wp.colSet(wpRr, "ex_s3", dsPrbl.colStr("add_apr_date"));
         wp.colSet(wpRr, "ex_s1", dsPrbl.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsPrbl.colStr("bin_type"));
         if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
            wp.colSet(wpRr, "ex_s5", dsPrbl.colStr("prb_mark") + "30");
         }
         else {
            wp.colSet(wpRr, "ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "30");
         }
         wp.colSet(wpRr, "ex_s7", dsPrbl.colStr("prb_reason_code"));
         wp.colSet(wpRr, "ex_s10", dsPrbl.colStr("prb_comment"));
         wp.colSet(wpRr, "ex_s13", dsPrbl.colStr("add_user"));
         wp.colSet(wpRr, "ex_s16", dsPrbl.colStr("prb_amount"));
      }

      if (commString.prblClose(lsStatus)) {
         wpRr++;
         if (wpRr < 9) {
            wp.colSet(wpRr, "ser_num", "0" + (wpRr + 1));
         }
         else {
            wp.colSet(wpRr, "ser_num", "" + (wpRr + 1));
         }
         wp.colSet(wpRr, "ex_s3", dsPrbl.colStr("close_apr_date"));
         wp.colSet(wpRr, "ex_s1", dsPrbl.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsPrbl.colStr("bin_type"));
         if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
            wp.colSet(wpRr, "ex_s5", dsPrbl.colStr("prb_mark") + "80");
         }
         else {
            wp.colSet(wpRr, "ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "80");
         }
         wp.colSet(wpRr, "ex_s7", dsPrbl.colStr("clo_result"));
         wp.colSet(wpRr, "ex_s10", dsPrbl.colStr("prb_comment"));
         wp.colSet(wpRr, "ex_s13", dsPrbl.colStr("close_add_user"));
         wp.colSet(wpRr, "ex_s16", dsPrbl.colStr("dc_prb_amount"));
         if (!empty(dsPrbl.colStr("clo_result_2"))) {
            //二次結案
            wpRr++;
            if (wpRr < 9) {
               wp.colSet(wpRr, "ser_num", "0" + (wpRr + 1));
            }
            else {
               wp.colSet(wpRr, "ser_num", "" + (wpRr + 1));
            }
            wp.colSet(wpRr, "ex_s3", dsPrbl.colStr("close_apr_date_2"));
            wp.colSet(wpRr, "ex_s1", dsPrbl.colStr("ctrl_seqno"));
            wp.colSet(wpRr, "ex_s2", dsPrbl.colStr("bin_type"));
            if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
               wp.colSet(wpRr, "ex_s5", dsPrbl.colStr("prb_mark") + "802");
            }
            else {
               wp.colSet(wpRr, "ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "802");
            }
            wp.colSet(wpRr, "ex_s7", dsPrbl.colStr("clo_result_2"));
            wp.colSet(wpRr, "ex_s10", dsPrbl.colStr("prb_comment"));
            wp.colSet(wpRr, "ex_s13", dsPrbl.colStr("close_add_user_2"));
            wp.colSet(wpRr, "ex_s16", dsPrbl.colStr("dc_prb_amount"));
         }
      }

   }
//		ddd("1.  row="+wpRr+", sql="+strSql);
   return sqlRowNum;
}

private int selectRskChgback() {
   ilCnt = 0;
		/*		if fst_add_date <>'' {
         ==>add-rows()
         ex_s1		= ctrl_seqno
         ex_s2		= bin_type
         ex_s3    = '1.一扣'
         ex_s4		= fst_add_date
         ex_s7		= fst_reason_code
         ex_s8		= fst_reverse_mark
         ex_s9		= fst_rebuild_mark
         ex_s10	= fst_msg
		}
		*/
   strSql = "select "
         + " chg_stage , "
         + " fst_status , "
         + " fst_apr_date , "
         + " ctrl_seqno , "
         + " bin_type , "
         + " chg_times , "
         + " fst_reason_code , "
         + " fst_reverse_mark , "
         + " fst_rebuild_mark , "
         + " fst_msg , "
         + " fst_add_user , "
         + " fst_twd_amt , "
         + " sec_status , "
         + " sec_apr_date , "
         + " sec_reason_code , "
         + " sec_reverse_mark , "
         + " sec_rebuild_mark , "
         + " sec_msg , "
         + " sec_add_user , "
         + " sec_twd_amt , "
         + " rep_status , "
         + " rep_apr_date , "
         + " rep_msg , "
         + " rep_add_user , "
         + " final_close , "
         + " close_apr_date , "
         + " close_add_user , "
         + " clo_result "
         + " from rsk_chgback "
         + " where reference_no = ? " +
         " order by reference_seq " //+zzsql.rownum(1)
   ;
   busi.DataSet dsChg = new busi.DataSet();
   dsChg.colList = this.sqlQuery(strSql, new Object[]{varsStr("reference_no")});
   while (dsChg.listNext()) {
      chgStatus = "";
      if (dsChg.colStr("chg_stage").compareTo("1") >= 0 && eqIgno(dsChg.colStr("fst_status"), "30")) {
         wpRr++;

         wp.colSet(wpRr, "ex_s3", dsChg.colStr("fst_apr_date"));
         wp.colSet(wpRr, "ex_s1", dsChg.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsChg.colStr("bin_type"));
         if (eqIgno(dsChg.colStr("chg_times"), "1")) {
            wp.colSet(wpRr, "ex_s5", "C130");
            chgStatus = "C130";
         }
         else {
            wp.colSet(wpRr, "ex_s5", "C330");
            chgStatus = "C330";
         }
         wp.colSet(wpRr, "ex_s7", dsChg.colStr("fst_reason_code"));
         wp.colSet(wpRr, "ex_s8", dsChg.colStr("fst_reverse_mark"));
         wp.colSet(wpRr, "ex_s9", dsChg.colStr("fst_rebuild_mark"));
         wp.colSet(wpRr, "ex_s10", dsChg.colStr("fst_msg"));
         wp.colSet(wpRr, "ex_s13", dsChg.colStr("fst_add_user"));
         wp.colSet(wpRr, "ex_s17", dsChg.colStr("fst_twd_amt"));
      }

      if (dsChg.colStr("chg_stage").compareTo("3") >= 0 && eqIgno(dsChg.colStr("sec_status"), "30")) {
         wpRr++;
         if (wpRr < 9) {
            wp.colSet(wpRr, "ser_num", "0" + (wpRr + 1));
         }
         else {
            wp.colSet(wpRr, "ser_num", "" + (wpRr + 1));
         }
         wp.colSet(wpRr, "ex_s3", dsChg.colStr("sec_apr_date"));
         wp.colSet(wpRr, "ex_s1", dsChg.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsChg.colStr("bin_type"));
         wp.colSet(wpRr, "ex_s5", "C130");
         chgStatus = "C130";
         wp.colSet(wpRr, "ex_s7", dsChg.colStr("sec_reason_code"));
         wp.colSet(wpRr, "ex_s8", dsChg.colStr("sec_reverse_mark"));
         wp.colSet(wpRr, "ex_s9", dsChg.colStr("sec_rebuild_mark"));
         wp.colSet(wpRr, "ex_s10", dsChg.colStr("sec_msg"));
         wp.colSet(wpRr, "ex_s13", dsChg.colStr("sec_add_user"));
         wp.colSet(wpRr, "ex_s17", dsChg.colStr("sec_twd_amt"));
      }

      if (dsChg.colStr("chg_stage").compareTo("2") >= 0 && eqIgno(dsChg.colStr("rep_status"), "30")) {
         wpRr++;
         if (wpRr < 9) {
            wp.colSet(wpRr, "ser_num", "0" + (wpRr + 1));
         }
         else {
            wp.colSet(wpRr, "ser_num", "" + (wpRr + 1));
         }
         wp.colSet(wpRr, "ex_s3", dsChg.colStr("rep_apr_date"));
         wp.colSet(wpRr, "ex_s1", dsChg.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsChg.colStr("bin_type"));
         wp.colSet(wpRr, "ex_s5", "C230");
         chgStatus = "C230";
         wp.colSet(wpRr, "ex_s10", dsChg.colStr("rep_msg"));
         wp.colSet(wpRr, "ex_s13", dsChg.colStr("rep_add_user"));
      }

      if (!empty(dsChg.colStr("final_close"))) {
         wpRr++;
         if (wpRr < 9) {
            wp.colSet(wpRr, "ser_num", "0" + (wpRr + 1));
         }
         else {
            wp.colSet(wpRr, "ser_num", "" + (wpRr + 1));
         }
         wp.colSet(wpRr, "ex_s3", dsChg.colStr("close_apr_date"));
         wp.colSet(wpRr, "ex_s1", dsChg.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsChg.colStr("bin_type"));
         wp.colSet(wpRr, "ex_s5", chgStatus + dsChg.colStr("final_close"));
         wp.colSet(wpRr, "ex_s11", "99");
         wp.colSet(wpRr, "ex_s13", dsChg.colStr("close_add_user"));
      }
      wp.colSet(wpRr, "ex_s12", dsChg.colStr("clo_result"));
   }
   log("3.  row=" + wpRr + ", sql=" + strSql);
   return sqlRowNum;
}

private int selectRskReceipt() {

		/*      ex_s1		= ctrl_seqno
      ex_s2		= bin_type
      ex_s3    = curr_status
      ex_s4    = nvl(close_add_date,add_date)
      ex_s5    = receipt_type(1.影本,2.正本,4.微縮影本)
      ex_s7    = reason_code
      ex_s10	= reject_mark*/
   strSql = "select "
         + " rept_status , "
         + " apr_date , "
         + " ctrl_seqno , "
         + " rept_seqno , "
         + " bin_type , "
         + " rept_type , "
         + " reason_code , "
         + " reject_mark , "
         + " add_user , "
         + " close_add_date , "
         + " add_date "
         + " from rsk_receipt "
         + " where reference_no ='" + varsStr("reference_no") + "'" +
         " order by reference_seq " //+zzsql.rownum(1)
   ;

   busi.DataSet dsRecp = new busi.DataSet();
   dsRecp.colList = this.sqlQuery(strSql, new Object[]{});
   while (dsRecp.listNext()) {
      if (dsRecp.colStr("rept_status").compareTo("30") >= 0) {
         wpRr++;

         wp.colSet(wpRr, "ex_s3", dsRecp.colStr("add_date"));
         wp.colSet(wpRr, "ex_s1", dsRecp.colStr("ctrl_seqno") + "-" + dsRecp.colStr("rept_seqno"));
         wp.colSet(wpRr, "ex_s2", dsRecp.colStr("bin_type"));
         wp.colSet(wpRr, "ex_s5", "RR30");
         if (eqIgno(dsRecp.colStr("rept_type"), "1")) {
            wp.colSet(wpRr, "ex_s7", dsRecp.colStr("reason_code") + "-影本");
         }
         else if (eqIgno(dsRecp.colStr("rept_type"), "2")) {
            wp.colSet(wpRr, "ex_s7", dsRecp.colStr("reason_code") + "-正本");
         }
         else {
            wp.colSet(wpRr, "ex_s7", dsRecp.colStr("reason_code") + "-微縮影本");
         }
         wp.colSet(wpRr, "ex_s9", dsRecp.colStr("reject_mark"));
         wp.colSet(wpRr, "ex_s13", dsRecp.colStr("add_user"));
      }

      if (dsRecp.colStr("rept_status").compareTo("60") >= 0) {
         wpRr++;
         if (wpRr < 9) {
            wp.colSet(wpRr, "ser_num", "0" + (wpRr + 1));
         }
         else {
            wp.colSet(wpRr, "ser_num", "" + (wpRr + 1));
         }
         wp.colSet(wpRr, "ex_s3", dsRecp.colStr("close_add_date"));
         wp.colSet(wpRr, "ex_s1", dsRecp.colStr("ctrl_seqno") + "-" + dsRecp.colStr("rept_seqno"));
         wp.colSet(wpRr, "ex_s2", dsRecp.colStr("bin_type"));
         wp.colSet(wpRr, "ex_s5", "RR80");
         if (eqIgno(dsRecp.colStr("rept_type"), "1")) {
            wp.colSet(wpRr, "ex_s7", dsRecp.colStr("reason_code") + "-影本");
         }
         else if (eqIgno(dsRecp.colStr("rept_type"), "2")) {
            wp.colSet(wpRr, "ex_s7", dsRecp.colStr("reason_code") + "-正本");
         }
         else {
            wp.colSet(wpRr, "ex_s7", dsRecp.colStr("reason_code") + "-微縮影本");
         }
         wp.colSet(wpRr, "ex_s9", dsRecp.colStr("reject_mark"));
         wp.colSet(wpRr, "ex_s13", dsRecp.colStr("add_user"));
      }

   }
   return sqlRowNum;
}

private int selectRskPrearbit() {
   ilCnt = 0;

   strSql = "select "
         + " arbit_times ,"
         + " pre_add_date ,"
         + " ctrl_seqno ,"
         + " bin_type ,"
         + " pre_status ,"
         + " pre_add_user ,"
         + " arb_status ,"
         + " arb_add_date ,"
         + " arb_add_user "
         + " from rsk_prearbit "
         + " where reference_no = ? and reference_seq=0"
   ;

   busi.DataSet dsArbit = new busi.DataSet();
   dsArbit.colList = this.sqlQuery(strSql, new Object[]{varsStr("reference_no")});
   while (dsArbit.listNext()) {
      int liTimes = dsArbit.colInt("arbit_times");
      String lsStatus = dsArbit.colStr("pre_status");
      if (liTimes >= 1) {
         wpRr++;
         wp.colSet(wpRr, "ex_s3", dsArbit.colStr("pre_add_date"));
         wp.colSet(wpRr, "ex_s1", dsArbit.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsArbit.colStr("bin_type"));
         wp.colSet(wpRr, "ex_s13", dsArbit.colStr("pre_add_user"));

         if (eqIgno(lsStatus, "10")) {
            wp.colSet(wpRr, "ex_s5", "預備仲裁建檔");
         }
         else if (eqIgno(lsStatus, "30")) {
            wp.colSet(wpRr, "ex_s5", "預備仲裁覆核");
         }
         else wp.colSet(wpRr, "ex_s5", "pre_ARB-" + lsStatus);
      }

      if (liTimes == 2) {
         wpRr++;
         lsStatus = dsArbit.colStr("arb_status");
         wp.colSet(wpRr, "ex_s3", dsArbit.colStr("arb_add_date"));
         wp.colSet(wpRr, "ex_s1", dsArbit.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsArbit.colStr("bin_type"));
         wp.colSet(wpRr, "ex_s13", dsArbit.colStr("arb_add_user"));

         if (eqIgno(lsStatus, "30")) {
            wp.colSet(wpRr, "ex_s5", "仲裁覆核");
         }
         else if (eqIgno(lsStatus, "80")) {
            wp.colSet(wpRr, "ex_s5", "預備仲裁結案");
         }
         else wp.colSet(wpRr, "ex_s5", "ARB-" + lsStatus);
      }
   }
   return sqlRowNum;
}

private int selectRskPrecompl() {
   ilCnt = 0;

   strSql = "select "
         + " compl_times , "
         + " pre_add_date , "
         + " ctrl_seqno , "
         + " bin_type , "
         + " pre_status , "
         + " pre_add_user , "
         + " com_status , "
         + " com_add_date , "
         + " com_add_user "
         + " from rsk_precompl "
         + " where reference_no = ? and reference_seq=0"
   ;

   busi.DataSet dsCompl = new busi.DataSet();
   dsCompl.colList = this.sqlQuery(strSql, new Object[]{varsStr("reference_no")});
   while (dsCompl.listNext()) {
      int liTimes = dsCompl.colInt("compl_times");
      String lsStatus = dsCompl.colStr("pre_status");

      if (liTimes >= 1) {
         wpRr++;

         wp.colSet(wpRr, "ex_s3", dsCompl.colStr("pre_add_date"));
         wp.colSet(wpRr, "ex_s1", dsCompl.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsCompl.colStr("bin_type"));
         if (eqIgno(lsStatus, "10")) {
            wp.colSet(wpRr, "ex_s5", "預備依從權建檔");
         }
         else if (eqIgno(lsStatus, "80")) {
            wp.colSet(wpRr, "ex_s5", "預備依從權結案");
         }
         else wp.colSet(wpRr, "ex_s5", "pre-COM-" + lsStatus);
         wp.colSet(wpRr, "ex_s13", dsCompl.colStr("pre_add_user"));
      }
      if (liTimes >= 2) {
         wpRr++;
         lsStatus = dsCompl.colStr("com_status");

         wp.colSet(wpRr, "ex_s3", dsCompl.colStr("com_add_date"));
         wp.colSet(wpRr, "ex_s1", dsCompl.colStr("ctrl_seqno"));
         wp.colSet(wpRr, "ex_s2", dsCompl.colStr("bin_type"));
         if (eqIgno(lsStatus, "30")) {
            wp.colSet(wpRr, "ex_s5", "依從權覆核");
         }
         else if (eq(lsStatus, "80")) {
            wp.colSet(wpRr, "ex_s5", "依從權結案");
         }
         else {
            wp.colSet(wpRr, "ex_s5", "COM-" + lsStatus);
         }
         wp.colSet(wpRr, "ex_s13", dsCompl.colStr("com_add_user"));
      }
   }
   return sqlRowNum;
}

}
