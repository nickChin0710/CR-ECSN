package rskr01;

public class Rskr0040Func extends busi.FuncBase {
public int ilCnt = 0;
public int wpRr = -1;
String chgStatus = "", refNo = "";
busi.DataSet idsData = new busi.DataSet();

int selectRskProblem() {
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
         + " close_add_user , close_add_date,"
         + " dc_prb_amount ,"
         + " close_apr_date_2 ,"
         + " clo_result_2 ,"
         + " close_add_user_2 "
         + " from rsk_problem "
         + " where reference_no = ? "
         + " order by reference_seq " //+zzsql.rownum(1)
   ;

   busi.DataSet dsPrbl = new busi.DataSet();
   dsPrbl.colList = this.sqlQuery(strSql, new Object[]{refNo});
   while (dsPrbl.listNext()) {
      int liStatus = commString.strToInt(dsPrbl.colStr("prb_status"));
      if (liStatus == 30) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsPrbl.colStr("add_apr_date"));
         if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_mark") + liStatus);
         }
         else {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + liStatus);
         }
      }
      else if (liStatus == 60) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsPrbl.colStr("close_add_date"));
         if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_mark") + liStatus);
         }
         else {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + liStatus);
         }
      }

      if (commString.prblClose("" + liStatus)) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsPrbl.colStr("close_apr_date"));
         if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_mark") + "80");
         }
         else {
            idsData.colSet("ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "80");
         }
         if (!empty(dsPrbl.colStr("clo_result_2"))) {
            //二次結案
            wpRr++;
            idsData.addrow();
            idsData.colSet("ex_s3", dsPrbl.colStr("close_apr_date_2"));
            if (eqIgno(dsPrbl.colStr("prb_src_code").substring(0, 1), "S")) {
               idsData.colSet("ex_s5", dsPrbl.colStr("prb_mark") + "802");
            }
            else {
               idsData.colSet("ex_s5", dsPrbl.colStr("prb_src_code").substring(0, 1) + "802");
            }
         }
      }
   }

   return sqlRowNum;
}

public int select_rsk_receipt() {
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
   dsRecp.colList = this.sqlQuery(strSql, new Object[]{refNo});
   while (dsRecp.listNext()) {
      if (dsRecp.colStr("rept_status").compareTo("30") >= 0) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsRecp.colStr("add_date"));
         idsData.colSet("ex_s5", "RR30");
      }

      if (dsRecp.colStr("rept_status").compareTo("60") >= 0) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsRecp.colStr("close_add_date"));
         idsData.colSet("ex_s5", "RR80");
      }
   }
   return sqlRowNum;
}

public int selectRskChgback() {
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
   dsChg.colList = this.sqlQuery(strSql, new Object[]{refNo});
   while (dsChg.listNext()) {
      chgStatus = "";
      if (dsChg.colStr("chg_stage").compareTo("1") >= 0 && eqIgno(dsChg.colStr("fst_status"), "30")) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsChg.colStr("fst_apr_date"));
         if (eqIgno(dsChg.colStr("chg_times"), "1")) {
            idsData.colSet("ex_s5", "C130");
            chgStatus = "C130";
         }
         else {
            idsData.colSet("ex_s5", "C330");
            chgStatus = "C330";
         }
      }

      if (dsChg.colStr("chg_stage").compareTo("3") >= 0 && eqIgno(dsChg.colStr("sec_status"), "30")) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsChg.colStr("sec_apr_date"));
         idsData.colSet("ex_s5", "C130");
      }

      if (dsChg.colStr("chg_stage").compareTo("2") >= 0 && eqIgno(dsChg.colStr("rep_status"), "30")) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsChg.colStr("rep_apr_date"));
         idsData.colSet("ex_s5", "C230");
      }

      if (!empty(dsChg.colStr("final_close"))) {
         wpRr++;
         idsData.addrow();

         idsData.colSet("ex_s3", dsChg.colStr("close_apr_date"));
         idsData.colSet("ex_s5", chgStatus + dsChg.colStr("final_close"));
      }
   }
   return sqlRowNum;
}

public int selectRskPrecompl() {
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
   dsCompl.colList = this.sqlQuery(strSql, new Object[]{refNo});
   while (dsCompl.listNext()) {
      int liTimes = dsCompl.colInt("compl_times");
      String lsStatus = dsCompl.colStr("pre_status");

      if (liTimes >= 1) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsCompl.colStr("pre_add_date"));
         if (eqIgno(lsStatus, "10")) {
        	 idsData.colSet(wpRr, "ex_s5", "預備依從權建檔");
         }
         else if (eqIgno(lsStatus, "80")) {
        	 idsData.colSet(wpRr, "ex_s5", "預備依從權結案");
         } 
         else {
        	 idsData.colSet(wpRr, "ex_s5", "預備依從權覆核");
         }
      }
      if (liTimes >= 2) {
         wpRr++;
         lsStatus = dsCompl.colStr("com_status");

         idsData.colSet("ex_s3", dsCompl.colStr("com_add_date"));
         if (eqIgno(lsStatus, "30")) {
        	 idsData.colSet(wpRr, "ex_s5", "依從權覆核");
         }
         else if (eq(lsStatus, "80")) {
        	 idsData.colSet(wpRr, "ex_s5", "依從權結案");
         }
         else {
        	 idsData.colSet(wpRr, "ex_s5", "依從權建檔");
         }
      }
   }
   return sqlRowNum;
}

public int selectRskPrearbit() {
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
         + " where reference_no = ?  and reference_seq=0"
   ;

   busi.DataSet dsArbit = new busi.DataSet();
   dsArbit.colList = this.sqlQuery(strSql, new Object[]{refNo});
   while (dsArbit.listNext()) {
      int liTimes = dsArbit.colInt("arbit_times");
      String lsStatus = dsArbit.colStr("pre_status");
      if (liTimes >= 1) {
         wpRr++;
         idsData.addrow();
         idsData.colSet("ex_s3", dsArbit.colStr("pre_add_date"));
         if (eqIgno(lsStatus, "10")) {
            idsData.colSet("ex_s5", "預備仲裁建檔");
         }
         else if (eqIgno(lsStatus, "30")) {
            idsData.colSet("ex_s5", "預備仲裁覆核");
         }
         else wp.colSet(wpRr, "ex_s5", "pre_ARB-" + lsStatus);
      }

      if (liTimes == 2) {
         wpRr++;
         idsData.addrow();
         lsStatus = dsArbit.colStr("arb_status");
         idsData.colSet("ex_s3", dsArbit.colStr("arb_add_date"));
         if (eqIgno(lsStatus, "30")) {
            idsData.colSet("ex_s5", "仲裁覆核");
         }
         else if (eqIgno(lsStatus, "80")) {
            idsData.colSet("ex_s5", "預備仲裁結案");
         }
         else idsData.colSet(wpRr, "ex_s5", "ARB-" + lsStatus);
      }
   }
   return sqlRowNum;
}

public String rskLastStatus(String arefNo) {
   refNo = arefNo;
   if (empty(refNo))
      return "";

   selectRskProblem();
   select_rsk_receipt();
   selectRskChgback();
   selectRskPrecompl();
   selectRskPrearbit();
   int ll_nrow = idsData.listRows();
   if (ll_nrow <= 0) {
      wp.listCount[0] = 0;
      return "";
   }

   idsData.sort("ex_s3");
   int ll = idsData.sortRow[ll_nrow - 1];
   idsData.listFetch(ll);
   return idsData.colStr("ex_s3") + " " + getDesc(idsData.colStr("ex_s5"));
}

private String getDesc(String a_code) {
   strSql = "select wf_desc from ptr_sys_idtab" +
         " where wf_type ='RSK-STATUS-DESC'" +
         " and wf_id =?";
   sqlSelect(strSql, a_code);
   if (sqlRowNum <= 0)
      return a_code;

   return colStr("wf_desc");
}

}
