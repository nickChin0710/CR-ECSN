package rskr01;

/**
 * 2020-0218	JH		list-sort()
 * 2019-1211   JH    UAT
 * 2019-1202   JH    UAT-modify
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

public class Rskq0010Func extends busi.FuncQuery {
public int ilCnt = 0;
public int wpRr = -1;
String chgStatus = "";
busi.DataSet idsData = new busi.DataSet();

@Override
public int querySelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public int dataSelect() {
   return rc;
}

public int selectRskProblem() throws Exception {
		/*      
		ex_s1 	= ctrl_seqno
      ex_s2		= bin_type
      ex_s3 	= prb_status
      ex_s4 	= nvl(close_apr_date,add_apr_date)
      ex_s5    = prb_mark+'-'+prb_src_code+'-'+prb_status   
      ex_s7		= db.clo_result
      ex_s10	= db.prb_comment 
			*/
   dataCheck();
   if (rc != 1) return rc;

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
         + " dc_prb_amount , dc_mcht_repay, dc_mcht_repay_2,"
         + " close_apr_date_2 ,"
         + " clo_result_2 ,"
         + " close_add_user_2 "
         + " from rsk_problem "
         + " where 1=1 and reference_no = ? "
         + " and prb_status <> '10' " //--新增待覆核不顯示
         + " order by reference_seq " //+zzsql.rownum(1)
   ;

   busi.DataSet dsPrbl = new busi.DataSet();
   dsPrbl.colList = this.sqlQuery(strSql, new Object[]{varsStr("reference_no")});
   while (dsPrbl.listNext()) {
      String ls_status = dsPrbl.colStr("prb_status");
      if (commString.strToInt(ls_status) >= 30) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsPrbl.colStr("add_apr_date"));
         idsData.colSet("ex_s1", dsPrbl.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsPrbl.colStr("bin_type"));
         if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_mark") + "30");
         }
         else {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "30");
         }
         idsData.colSet("ex_s7", dsPrbl.colStr("prb_reason_code"));
         idsData.colSet("ex_s10", dsPrbl.colStr("prb_comment"));
         idsData.colSet("ex_s13", dsPrbl.colStr("add_user"));
         idsData.colSet("ex_s16", dsPrbl.colStr("dc_prb_amount"));
      }	else if (commString.strToInt(ls_status) == 10) {
    	  wpRr++;
          idsData.addrow();          
      }

      if (commString.prblClose(ls_status)) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsPrbl.colStr("close_apr_date"));
         idsData.colSet("ex_s1", dsPrbl.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsPrbl.colStr("bin_type"));
         if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_mark") + "80");
         }
         else {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "80");
         }
         idsData.colSet("ex_s7", dsPrbl.colStr("clo_result"));
         idsData.colSet("ex_s10", dsPrbl.colStr("prb_comment"));
         idsData.colSet("ex_s13", dsPrbl.colStr("close_add_user"));
         idsData.colSet("ex_s16", dsPrbl.colStr("dc_prb_amount"));
         idsData.colSet("ex_s17", dsPrbl.colStr("dc_mcht_repay"));
         if (!empty(dsPrbl.colStr("clo_result_2"))) {
            //二次結案
            wpRr++;
            idsData.addrow();
            idsData.colSet("ex_s3", dsPrbl.colStr("close_apr_date_2"));
            idsData.colSet("ex_s1", dsPrbl.colStr("ctrl_seqno"));
            idsData.colSet("ex_s2", dsPrbl.colStr("bin_type"));
            if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
               idsData.colSet("ex_s5", dsPrbl.colStr("prb_mark") + "802");
            }
            else {
               idsData.colSet("ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "802");
            }
            idsData.colSet("ex_s7", dsPrbl.colStr("clo_result_2"));
            idsData.colSet("ex_s10", dsPrbl.colStr("prb_comment"));
            idsData.colSet("ex_s13", dsPrbl.colStr("close_add_user_2"));
            idsData.colSet("ex_s16", dsPrbl.colStr("dc_prb_amount"));
            idsData.colSet("ex_s17", dsPrbl.colStr("dc_mcht_repay_2"));
         }
      }

   }
//		ddd("1.  row="+wpRr+", sql="+strSql);
   return sqlRowNum;
}

public int selectRskReceipt() throws Exception  {
   dataCheck();
   if (rc != 1) return -1;
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
         + " where reference_no = ? " +
         " order by reference_seq " //+zzsql.rownum(1)
   ;

   busi.DataSet dsRecp = new busi.DataSet();
   dsRecp.colList = this.sqlQuery(strSql, new Object[]{varsStr("reference_no")});
   while (dsRecp.listNext()) {
      if (dsRecp.colStr("rept_status").compareTo("30") >= 0) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsRecp.colStr("add_date"));
         idsData.colSet("ex_s1", dsRecp.colStr("ctrl_seqno") + "-" + dsRecp.colStr("rept_seqno"));
         idsData.colSet("ex_s2", dsRecp.colStr("bin_type"));
         idsData.colSet("ex_s5", "RR30");
         if (eqIgno(dsRecp.colStr("rept_type"), "1")) {
            idsData.colSet("ex_s7", dsRecp.colStr("reason_code") + "-影本");
         }
         else if (eqIgno(dsRecp.colStr("rept_type"), "2")) {
            idsData.colSet("ex_s7", dsRecp.colStr("reason_code") + "-正本");
         }
         else {
            idsData.colSet("ex_s7", dsRecp.colStr("reason_code") + "-微縮影本");
         }
         idsData.colSet("ex_s9", dsRecp.colStr("reject_mark"));
         idsData.colSet("ex_s13", dsRecp.colStr("add_user"));
      }

      if (dsRecp.colStr("rept_status").compareTo("60") >= 0) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsRecp.colStr("close_add_date"));
         idsData.colSet("ex_s1", dsRecp.colStr("ctrl_seqno") + "-" + dsRecp.colStr("rept_seqno"));
         idsData.colSet("ex_s2", dsRecp.colStr("bin_type"));
         idsData.colSet("ex_s5", "RR80");
         if (eqIgno(dsRecp.colStr("rept_type"), "1")) {
            idsData.colSet("ex_s7", dsRecp.colStr("reason_code") + "-影本");
         }
         else if (eqIgno(dsRecp.colStr("rept_type"), "2")) {
            idsData.colSet("ex_s7", dsRecp.colStr("reason_code") + "-正本");
         }
         else {
            idsData.colSet("ex_s7", dsRecp.colStr("reason_code") + "-微縮影本");
         }
         idsData.colSet("ex_s9", dsRecp.colStr("reject_mark"));
         idsData.colSet("ex_s13", dsRecp.colStr("add_user"));
      }

   }
   log("2.  row=" + wpRr + ", sql=" + strSql);
   return sqlRowNum;
}

public int selectRskChgback() throws Exception  {
   ilCnt = 0;
   dataCheck();
   if (rc != 1) return -1;
		
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
      
      if (dsChg.colStr("chg_stage").compareTo("3") >= 0 && eqIgno(dsChg.colStr("sec_status"), "30")) {
          wpRr++;
          idsData.addrow();
          idsData.colSet("ex_s3", dsChg.colStr("sec_apr_date"));
          idsData.colSet("ex_s1", dsChg.colStr("ctrl_seqno"));
          idsData.colSet("ex_s2", dsChg.colStr("bin_type"));
          idsData.colSet("ex_s5", "C130");
          chgStatus = "C130";
          idsData.colSet("ex_s7", dsChg.colStr("sec_reason_code"));
          idsData.colSet("ex_s8", dsChg.colStr("sec_reverse_mark"));
          idsData.colSet("ex_s9", dsChg.colStr("sec_rebuild_mark"));
          idsData.colSet("ex_s10", dsChg.colStr("sec_msg"));
          idsData.colSet("ex_s13", dsChg.colStr("sec_add_user"));
          idsData.colSet("ex_s17", dsChg.colStr("sec_twd_amt"));
       }
      
      if (dsChg.colStr("chg_stage").compareTo("2") >= 0 && eqIgno(dsChg.colStr("rep_status"), "30")) {
          wpRr++;
          idsData.addrow();
          idsData.colSet("ex_s3", dsChg.colStr("rep_apr_date"));
          idsData.colSet("ex_s1", dsChg.colStr("ctrl_seqno"));
          idsData.colSet("ex_s2", dsChg.colStr("bin_type"));
          idsData.colSet("ex_s5", "C230");
          chgStatus = "C230";
          idsData.colSet("ex_s10", dsChg.colStr("rep_msg"));
          idsData.colSet("ex_s13", dsChg.colStr("rep_add_user"));
       }
      
      if (dsChg.colStr("chg_stage").compareTo("1") >= 0 && eqIgno(dsChg.colStr("fst_status"), "30")) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsChg.colStr("fst_apr_date"));
         idsData.colSet("ex_s1", dsChg.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsChg.colStr("bin_type"));
         if (eqIgno(dsChg.colStr("chg_times"), "1")) {
            idsData.colSet("ex_s5", "C130");
            chgStatus = "C130";
         }
         else {
            idsData.colSet("ex_s5", "C330");
            chgStatus = "C330";
         }
         idsData.colSet("ex_s7", dsChg.colStr("fst_reason_code"));
         idsData.colSet("ex_s8", dsChg.colStr("fst_reverse_mark"));
         idsData.colSet("ex_s9", dsChg.colStr("fst_rebuild_mark"));
         idsData.colSet("ex_s10", dsChg.colStr("fst_msg"));
         idsData.colSet("ex_s13", dsChg.colStr("fst_add_user"));
         idsData.colSet("ex_s17", dsChg.colStr("fst_twd_amt"));
      }

      if (!empty(dsChg.colStr("final_close"))) {
         wpRr++;
         idsData.addrow();

         idsData.colSet("ex_s3", dsChg.colStr("close_apr_date"));
         idsData.colSet("ex_s1", dsChg.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsChg.colStr("bin_type"));
         idsData.colSet("ex_s5", chgStatus + dsChg.colStr("final_close"));
         idsData.colSet("ex_s11", "99");
         idsData.colSet("ex_s13", dsChg.colStr("close_add_user"));
      }
      idsData.colSet("ex_s12", dsChg.colStr("clo_result"));
   }
//   ddd("3.  row=" + wpRr + ", sql=" + strSql);
   return sqlRowNum;
}

public int selectRskPrecompl() throws Exception  {
   ilCnt = 0;
   dataCheck();
   if (rc != 1) return -1;

   strSql = "select "
         + " compl_times , "
         + " pre_add_date , "
         + " ctrl_seqno , "
         + " bin_type , "
         + " pre_status , pre_amt,"
         + " pre_add_user , "
         + " com_status , com_amt,"
         + " com_add_date , "
         + " com_add_user "
         + " from rsk_precompl "
         + " where reference_no = ? and reference_seq=0"
   ;

   busi.DataSet dsCompl = new busi.DataSet();
   dsCompl.colList = this.sqlQuery(strSql, new Object[]{varsStr("reference_no")});
   while (dsCompl.listNext()) {
      int liTimes = dsCompl.colInt("compl_times");
      String lsStatus1 =dsCompl.colStr("pre_status");
      String lsStatus2 =dsCompl.colStr("com_status");

      if (liTimes >= 1 && notEmpty(lsStatus1)) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsCompl.colStr("pre_add_date"));
         idsData.colSet("ex_s1", dsCompl.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsCompl.colStr("bin_type"));

         if (eqIgno(lsStatus1, "10")) {
            idsData.colSet("ex_s5", "預備依從權建檔");
         }
         else if (eq(lsStatus1,"30")) {
            idsData.colSet("ex_s5", "預備依從權覆核");
         }
         else if (eqIgno(lsStatus1, "80")) {
            idsData.colSet("ex_s5", "預備依從權結案");
         }
         else idsData.colSet("ex_s5", "pre-COM-" + lsStatus1);

         idsData.colSet("ex_s16", dsCompl.colNum("pre_amt")); //問題金額
         idsData.colSet("ex_s13", dsCompl.colStr("pre_add_user"));
      }
      if (liTimes == 2 && notEmpty(lsStatus2)) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsCompl.colStr("com_add_date"));
         idsData.colSet("ex_s1", dsCompl.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsCompl.colStr("bin_type"));
         if (eq(lsStatus2,"10"))
            idsData.colSet("ex_s5","依從權建檔");
         else if (eqIgno(lsStatus2, "30"))
            idsData.colSet("ex_s5","依從權覆核");
         else if (eq(lsStatus2, "80"))
            idsData.colSet("ex_s5", "依從權結案");
         else idsData.colSet("ex_s5", "COM-" + lsStatus2);

         idsData.colSet("ex_s16", dsCompl.colNum("com_amt")); //問題金額
         idsData.colSet("ex_s13", dsCompl.colStr("com_add_user"));
      }
   }
   return sqlRowNum;
}

public int selectRskPrearbit() throws Exception  {
   ilCnt = 0;
   dataCheck();
   if (rc != 1) return -1;

   strSql = "select "
         + " arbit_times ,"
         + " pre_add_date ,"
         + " ctrl_seqno ,"
         + " bin_type ,"
         + " pre_status , pre_amt, "
         + " pre_add_user ,"
         + " arb_status , arb_amt, "
         + " arb_add_date ,"
         + " arb_add_user "
         + " from rsk_prearbit "
         + " where reference_no ='" + varsStr("reference_no") + "' and reference_seq=0"
   ;

   busi.DataSet dsArbit = new busi.DataSet();
   dsArbit.colList = this.sqlQuery(strSql, new Object[]{});
   while (dsArbit.listNext()) {
      int liTimes = dsArbit.colInt("arbit_times");
      String lsStatus1 = dsArbit.colStr("pre_status");
      String lsStatus2 = dsArbit.colStr("arb_status");

      if (liTimes >= 1 && notEmpty(lsStatus1)) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsArbit.colStr("pre_add_date"));
         idsData.colSet("ex_s1", dsArbit.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsArbit.colStr("bin_type"));
         idsData.colSet("ex_s13", dsArbit.colStr("pre_add_user"));
         idsData.colSet("ex_s16", dsArbit.colNum("pre_amt"));

         if (eqIgno(lsStatus1, "10")) {
            idsData.colSet("ex_s5", "預備仲裁建檔");
         }
         else if (eqIgno(lsStatus1, "30")) {
            idsData.colSet("ex_s5", "預備仲裁覆核");
         }
         else if (eqIgno(lsStatus1, "80")) {
            idsData.colSet("ex_s5", "預備仲裁結案");
         }
         else wp.colSet(wpRr, "ex_s5", "pre_ARB-" + lsStatus1);
      }

      if (liTimes == 2) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsArbit.colStr("arb_add_date"));
         idsData.colSet("ex_s1", dsArbit.colStr("ctrl_seqno"));
         idsData.colSet("ex_s2", dsArbit.colStr("bin_type"));
         idsData.colSet("ex_s13", dsArbit.colStr("arb_add_user"));
         idsData.colSet("ex_s16", dsArbit.colNum("arb_amt"));

         if (eq(lsStatus2,"10")) {
            idsData.colSet("ex_s5", "仲裁建檔");
         }
         else if (eqIgno(lsStatus2, "30")) {
            idsData.colSet("ex_s5", "仲裁覆核");
         }
         else if (eqIgno(lsStatus2, "80")) {
            idsData.colSet("ex_s5", "預備仲裁結案");
         }
         else wp.colSet(wpRr, "ex_s5", "ARB-" + lsStatus2);
      }
   }
   return sqlRowNum;
}

@Override
public void dataCheck() {
   if (varEmpty("reference_no")) {
      errmsg("帳單流水號: 不可空白");
      return;
   }
}

public void readData() throws Exception  {
   dataCheck();
   selectRskProblem();
   selectRskReceipt();
   selectRskChgback();
   selectRskPrecompl();
   selectRskPrearbit();
   
   if(wpRr==-1) {
	   //--避免listRows當掉
	   wpRr++;
       idsData.addrow();          
   }
   
   int llNrow = idsData.listRows();
   if (llNrow <= 0) {
      wp.listCount[0] = 0;
      return;
   }

   idsData.sort("ex_s3");
//		for(int ii=0; ii<idsData.list_rows(); ii++) {
//			idsData.list_2Col(idsData.aa_sortRow[ii]);
//			ddd("-->S3[%s], S1[%s]",idsData.colStr("ex_s3"),idsData.colStr("ex_s1"));
//		}

   String lsKk1 = "", lsKk2 = "";
   for (int ii = 0; ii < llNrow; ii++) {	  
      idsData.listToCol(idsData.sortRow[ii]);
      wp.setListSernum(0, "", ii + 1);
      wp.colSet(ii, "ex_s3", idsData.colStr("ex_s3"));
      wp.colSet(ii, "ex_s1", idsData.colStr("ex_s1"));
      wp.colSet(ii, "ex_s2", idsData.colStr("ex_s2"));
      wp.colSet(ii, "ex_s5", idsData.colStr("ex_s5"));
      wp.colSet(ii, "ex_s7", idsData.colStr("ex_s7"));
      wp.colSet(ii, "ex_s8", idsData.colStr("ex_s8"));
      wp.colSet(ii, "ex_s9", idsData.colStr("ex_s9"));
      wp.colSet(ii, "ex_s10", idsData.colStr("ex_s10"));
      wp.colSet(ii, "ex_s12", idsData.colStr("ex_s12"));
      wp.colSet(ii, "ex_s13", idsData.colStr("ex_s13"));
      wp.colSet(ii, "ex_s16", idsData.colStr("ex_s16"));
      wp.colSet(ii, "ex_s17", idsData.colStr("ex_s17"));
      String ss = idsData.colStr("ex_s3");
      if (commString.strComp(ss, lsKk1) > 0) {
         lsKk1 = ss;
         lsKk2 = idsData.colStr("ex_s5");
      }
   }
         
   wp.listCount[0] = idsData.listRows();
//		wp.col_set("ex_last_date",lsKk1);
//		wp.col_set("ex_last_remark",lsKk2);
   
   if(idsData.listRows() == 1 && wp.colEmpty(0,"ex_s5")) {
	   wp.listCount[0] = 0 ;
	   wpRr = -1;
   }
   
}

}
