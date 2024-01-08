package Rsk;
/**
 * 2020-1022  V1.00.01   Alex  改為日批跑
 * 2020-0406  V1.00.02   JH    anal.acct_month-1
 * 2020-0401  V1.00.03   JH    總額度增加幅度(%)
 * 2020-0210  V1.00.04   JH    區間修正
 * 2020-0109  V1.00.05   JH    modify
 * 2019-0802  V1.00.06   JH    消費年月
 * 2019-0730  V1.00.07   JH    format
 * 2019-0628  V1.00.08   JH    modify
 * 2019-0625  V1.00.09   JH    p_xxx >>acno_p_xxx
 * 107/10/19  V1.00.10  Alex              initial
 * 109-11-11  V1.00.11  tanwei    updated for project coding standard
 */

import com.BaseBatch;
import com.DataSet;

public class RskR002 extends BaseBatch {
// -----------------------------------------------------------------------------
hdata.RskAcnoLog hAclg = new hdata.RskAcnoLog();
DataSet r001Parm = new DataSet();
double[] imLimit = new double[6];
// -----------------------------------------------------------------------------
double[] data1AcnoCnt = new double[6];
double[] data1BefLocAmt = new double[6];
double[] data1AftLocAmt = new double[6];
double[] data1BefPurchAmt = new double[6];
double[] data1AftPurchAmt = new double[6];
private final String progname = "篩選永久調高額度名單統計處理  109/11/11 V1.00.11";
private int tiAcno = -1;
private int tiSub = -1;
private int tiSub2 = -1;
// -----------------------------------------------------------------------------
private String isAdjYymm = "";
private String isAdjLocFlag = "";
private String hAdjUser1 = "";
private String hAdjUser2 = "";
private String hAdjUser3 = "";
private String hAdjUser4 = "";
private String hAdjUser5 = "";
private String hAdjUser6 = "";
private double hAdjLimitE1 = 0;
private double hAdjLimitE2 = 0;
private double hAdjLimitE3 = 0;
private double hAdjLimitE4 = 0;
private double hAdjLimitE5 = 0;
private String hAdjYymm = "";
private String hAdjLocFlag = "";
private String hPurchYm1 = "";
private String hPurchYm2 = "";
private double hTotAmtBefore = 0;
private double hTotAmtAfter = 0;
private com.Parm2sql ttData2 = null;
private com.Parm2sql ttData1 = null;

// =============================================================================
public static void main(String[] args) {
   RskR002 proc = new RskR002();
//   proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);
   int liArg = args.length;
   if (liArg > 3) {
      printf("Usage : RskR002 YYYYMM [1/2, batch_seq]");
      okExit(0);
   }

   dbConnect();
   if (liArg > 0)
      isAdjYymm = args[0];
   if (liArg >= 2 && args[1].length() == 1)
      isAdjLocFlag = args[1];
   if (liArg == 3) {
      callBatchSeqno(args[2]);
   }
   callBatch(0, 0, 0);

//   printf("-->覆核月份[%s], 調高調低[%s]", is_adj_yymm, is_adj_loc_flag);

   selectRskR001Parm();

   sqlCommit(1);
   endProgram();
}

//--------------------------------------------------------------------------
void selectRskR001Parm() throws Exception {
   sqlCmd = " select *"
      + ", uf_date_add(adj_yymm,0,0-purch_mm1,0) as purch_date1"
      + ", uf_date_add(adj_yymm,0,purch_mm2,0) as purch_date2"
      + " from rsk_r001_parm"
      + " where 1=1 "
//      + " where adj_yymm =? and adj_loc_flag like ?"
      + " and apr_date <>'' and proc_date = '' "
      + "order by adj_loc_flag";
   
   this.sqlQuery(r001Parm, sqlCmd, new Object[] {});
   
//   this.sqlQuery(r001_parm, sqlCmd, new Object[]{
//      is_adj_yymm, is_adj_loc_flag + "%"
//   });

   for (int ii = 0; ii < r001Parm.listCnt; ii++) {
	  totalCnt++;
      hAdjUser1 = r001Parm.colSs(ii, "adj_user1");
      hAdjUser2 = r001Parm.colSs(ii, "adj_user2");
      hAdjUser3 = r001Parm.colSs(ii, "adj_user3");
      hAdjUser4 = r001Parm.colSs(ii, "adj_user4");
      hAdjUser5 = r001Parm.colSs(ii, "adj_user5");
      hAdjUser6 = r001Parm.colSs(ii, "adj_user6");
      hAdjLimitE1 = r001Parm.colNum(ii, "adj_limit_e1");
      hAdjLimitE2 = r001Parm.colNum(ii, "adj_limit_e2");
      hAdjLimitE3 = r001Parm.colNum(ii, "adj_limit_e3");
      hAdjLimitE4 = r001Parm.colNum(ii, "adj_limit_e4");
      hAdjLimitE5 = r001Parm.colNum(ii, "adj_limit_e5");
      hAdjYymm = r001Parm.colSs(ii, "adj_yymm");
      hAdjLocFlag = r001Parm.colSs(ii, "adj_loc_flag");
      hPurchYm1 = r001Parm.colSs(ii, "purch_date1");
      hPurchYm2 = r001Parm.colSs(ii, "purch_date2");

      deleteRskR001Data();
      //--調高
      if (eq(hAdjLocFlag, "1"))
         selectRskAcnolog1();
      //--調低
      if (eq(hAdjLocFlag, "2"))
         selectRskAcnolog2();

      updateRskR001Parm(hAdjYymm, hAdjLocFlag);
   }
}

void updateRskR001Parm(String aYymm, String aLocFlag) throws Exception {
   sqlCmd = "update rsk_r001_parm set"
      + " proc_date =?"
      + ", purch_bef_ym =?"
      + ", purch_aft_ym =?"
      + modxxxSet(",")
      + " where adj_yymm =?"
      + " and adj_loc_flag =?";
   ppp(1, sysDate);
   ppp(hPurchYm1);
   ppp(hPurchYm2);
   //-kk-
   ppp(aYymm);
   ppp(aLocFlag);
   sqlExec("");
   if (sqlNrow <= 0) {
      sqlerr("update rsk_r001_parm error");
      errExit();
   }
}

void deleteRskR001Data() throws Exception {
   sqlCmd = "delete rsk_r001_data1 where ADJ_YYMM =? and ADJ_LOC_FLAG=?";
   ppp(1, hAdjYymm);
   ppp(hAdjLocFlag);
   sqlExec(sqlCmd);
   if (sqlNrow < 0) {
      errmsg("delete rsk_r001_data1, kk[%s],[%s]", hAdjYymm, hAdjLocFlag);
      errExit(1);
   }

   sqlCmd = "delete rsk_r001_data2 where ADJ_YYMM =? and ADJ_LOC_FLAG=?";
   ppp(1, hAdjYymm);
   ppp(hAdjLocFlag);
   sqlExec(sqlCmd);
   if (sqlNrow < 0) {
      errmsg("delete rsk_r001_data2, kk[%s],[%s]", hAdjYymm, hAdjLocFlag);
      errExit(1);
   }
}

//--------------------------------------------------------------------------
void selectRskAcnolog1() throws Exception {
   //-調高-
   String lsModWhere = "";
   sqlCmd = "select distinct a.acno_p_seqno, a.acct_type , a.id_p_seqno, A.bef_loc_amt, A.aft_loc_amt, A.adj_loc_flag , "
      + "A.log_reason, A.emend_type, A.mod_user , A.apr_date"
      + " from rsk_acnolog A join ("
      + " select acno_p_seqno, max(mod_time) as mod_time from rsk_acnolog"
      + " where log_date >=? and apr_date like ? and log_reason not in ('','H')"
      + " and emend_type ='1' and log_type='1' and kind_flag='A' group by acno_p_seqno ) B "
      + " on  A.acno_p_seqno=B.acno_p_seqno and A.mod_time=B.mod_time"
      + " where A.log_date >? and A.apr_date like ?"
      + " and A.log_type='1' and emend_type='1' and kind_flag='A' and adj_loc_flag='1'"
      + " and A.log_reason not in ('','H')"
   ;
   ppp(1, hAdjYymm);
   ppp(hAdjYymm + "%");
   ppp(hAdjYymm);
   ppp(hAdjYymm + "%");
   if (noEmpty(hAdjUser1 + hAdjUser2 + hAdjUser3 + hAdjUser4 + hAdjUser5 + hAdjUser6)) {
      sqlCmd += " and mod_user<>'' and mod_user in (?,?,?,?,?,?)";
      ppp(hAdjUser1);
      ppp(hAdjUser2);
      ppp(hAdjUser3);
      ppp(hAdjUser4);
      ppp(hAdjUser5);
      ppp(hAdjUser6);
   }

   dddSql();
//   im_limit[0] = 0;
   imLimit[0] = hAdjLimitE1;
   imLimit[1] = hAdjLimitE2;
   imLimit[2] = hAdjLimitE3;
   imLimit[3] = hAdjLimitE4;
   imLimit[4] = hAdjLimitE5;

   int idx = -1;
   daoTable = "open-Cursor-A";
   openCursor();
//   this.fetchExtend ="AA.";
   while (fetchTable()) {
      hAclg.acnoPSeqno = colSs("acno_p_seqno");
      hAclg.acctType = colSs("acct_type");
      hAclg.idPSeqno = colSs("id_p_seqno");
      hAclg.befLocAmt = colNum("bef_loc_amt");
      hAclg.aftLocAmt = colNum("aft_loc_amt");
      hAclg.adjLocFlag = colSs("adj_loc_flag");
      hAclg.logReason = colSs("log_reason");
      hAclg.emendType = colSs("emend_type");
      hAclg.modUser = colSs("mod_user");
      hAclg.aprDate = colSs("apr_date");
      if (eqIgno(hAclg.logReason, "H")) continue;
      if (!eqIgno(hAclg.adjLocFlag, "1")) continue;
      totalCnt++;

//      select_act_anal_sub();

      insertR001Data2();

      idx = -1;
      double lmAmt = hAclg.befLocAmt;
      if (lmAmt > 0 && lmAmt < imLimit[0]) idx = 0;
      else if (lmAmt >= imLimit[0] && lmAmt < imLimit[1]) idx = 1;
      else if (lmAmt >= imLimit[1] && lmAmt < imLimit[2]) idx = 2;
      else if (lmAmt >= imLimit[2] && lmAmt < imLimit[3]) idx = 3;
      else if (lmAmt >= imLimit[3] && lmAmt < imLimit[4]) idx = 4;
      else if (lmAmt >= imLimit[4]) idx = 5;
      if (idx < 0) continue;

      data1AcnoCnt[idx]++;
      data1BefLocAmt[idx] += hAclg.befLocAmt;
      data1AftLocAmt[idx] += hAclg.aftLocAmt;
      data1BefPurchAmt[idx] += hTotAmtBefore;
      data1AftPurchAmt[idx] += hTotAmtAfter;
   }
   closeCursor();

   insertR001Data1();
}

void selectRskAcnolog2() throws Exception {
   //-調低-
   String lsModWhere = "";
   sqlCmd = "select distinct a.acno_p_seqno, a.acct_type , a.id_p_seqno, A.bef_loc_amt, A.aft_loc_amt, A.adj_loc_flag , "
      + "A.log_reason, A.emend_type, A.mod_user , A.apr_date"
      + " from rsk_acnolog A join ("
      + " select acno_p_seqno, max(mod_time) as mod_time from rsk_acnolog"
      + " where log_date >=? and apr_date like ? and log_reason not in ('','H')"
      + "   and emend_type ='1' and log_type='1' and kind_flag='A' group by acno_p_seqno ) B "
      + " on A.acno_p_seqno=B.acno_p_seqno and A.mod_time=B.mod_time"
      + " where A.log_date >=? and A.apr_date like ?"
      + " and A.log_type='1' and emend_type='1' and kind_flag='A' and adj_loc_flag='2' "
      + " and A.log_reason not in ('','H')";
   ppp(1, hAdjYymm);
   ppp(2, hAdjYymm + "%");
   ppp(3, hAdjYymm);
   ppp(4, hAdjYymm + "%");

   if (noEmpty(hAdjUser1 + hAdjUser2 + hAdjUser3 + hAdjUser4 + hAdjUser5 + hAdjUser6)) {
      sqlCmd += " and mod_user<>'' and mod_user in (?,?,?,?,?,?)";
      ppp(hAdjUser1);
      ppp(hAdjUser2);
      ppp(hAdjUser3);
      ppp(hAdjUser4);
      ppp(hAdjUser5);
      ppp(hAdjUser6);
   }

   //im_limit[0] = 0;
   imLimit[0] = hAdjLimitE1;
   imLimit[1] = hAdjLimitE2;
   imLimit[2] = hAdjLimitE3;
   imLimit[3] = hAdjLimitE4;
   imLimit[4] = hAdjLimitE5;
   int idx = -1;

   daoTable = "open-Cursor-B";
   openCursor();
   while (fetchTable()) {

      hAclg.acnoPSeqno = colSs("acno_p_seqno");
      hAclg.acctType = colSs("acct_type");
      hAclg.idPSeqno = colSs("id_p_seqno");
      hAclg.befLocAmt = colNum("bef_loc_amt");
      hAclg.aftLocAmt = colNum("aft_loc_amt");
      hAclg.adjLocFlag = colSs("adj_loc_flag");
      hAclg.logReason = colSs("log_reason");
      hAclg.emendType = colSs("emend_type");
      hAclg.modUser = colSs("mod_user");
      hAclg.aprDate = colSs("apr_date");
      if (!eqIgno(hAclg.adjLocFlag, "2")) continue;
      totalCnt++;

//      select_act_anal_sub();
      insertR001Data2();

      idx = -1;
      double lmAmt = hAclg.befLocAmt;
      if (lmAmt > 0 && lmAmt <= imLimit[0]) idx = 0;
      else if (lmAmt > imLimit[0] && lmAmt <= imLimit[1]) idx = 1;
      else if (lmAmt > imLimit[1] && lmAmt <= imLimit[2]) idx = 2;
      else if (lmAmt > imLimit[2] && lmAmt <= imLimit[3]) idx = 3;
      else if (lmAmt > imLimit[3] && lmAmt <= imLimit[4]) idx = 4;
      else if (lmAmt > imLimit[4]) idx = 5;
      if (idx < 0) continue;

      data1AcnoCnt[idx]++;
      data1BefLocAmt[idx] += hAclg.befLocAmt;
      data1AftLocAmt[idx] += hAclg.aftLocAmt;
      data1BefPurchAmt[idx] += hTotAmtBefore;
      data1AftPurchAmt[idx] += hTotAmtAfter;
   }
   closeCursor();

   insertR001Data1();
}

void insertR001Data2() throws Exception {
   if (tiAcno <= 0) {
      sqlCmd = " select "
         + " B.acct_jrnl_bal , "
         + " A.stop_status as acno_stop_flag, "
         + " A.payment_rate1 as acno_payment_rate"
         + " from act_acno A join act_acct B on B.p_seqno=A.p_seqno "
         + " where A.p_seqno = ? and acno_flag<>'Y'";
      tiAcno = ppStmtCrt("tiAcno", "");
   }

   ppp(1, hAclg.acnoPSeqno);
   sqlSelect(tiAcno);

   String lsMcode = "";
   String lsStop = "";
   double lmEndBal = 0;
   if (sqlNrow > 0) {
      lsMcode = colSs("acno_payment_rate");
      lsStop = colSs("acno_stop_flag");
      lmEndBal = colNum("acct_jrnl_bal");
   }
   //-Mcode>0, stop=Y才insert-
//   if (ss_2int(ls_mcode)<=0 && !eq(ls_stop,"Y")) {
//      return;
//   }

   if (ttData2 == null) {
      ttData2 = new com.Parm2sql();
      ttData2.insert("rsk_r001_data2");
   }

   ttData2.aaa("adj_yymm", hAdjYymm);
   ttData2.aaa("adj_loc_flag", hAdjLocFlag);
   ttData2.aaa("acno_p_seqno", hAclg.acnoPSeqno);
   ttData2.aaa("acct_type", hAclg.acctType);
   ttData2.aaa("id_p_seqno", hAclg.idPSeqno);
   ttData2.aaa("bef_loc_amt", hAclg.befLocAmt);
   ttData2.aaa("aft_loc_amt", hAclg.aftLocAmt);
   ttData2.aaa("apr_date", hAclg.aprDate);
   ttData2.aaa("end_bal", lmEndBal);
   ttData2.aaa("payment_rate", lsMcode);
   ttData2.aaa("acno_stop_flag", lsStop);
//   tt_data2.aaa("bef_purch_amt", h_tot_amt_before);
//   tt_data2.aaa("aft_purch_amt", h_tot_amt_after);
   ttData2.aaaYmd("proc_date");
   ttData2.aaaDtime("mod_time");
   ttData2.aaa("mod_pgm", hModPgm);
   if (ttData2.ti <= 0) {
      ttData2.ti = ppStmtCrt("ttData2", ttData2.getSql());
   }

   sqlExec(ttData2.ti, ttData2.getConvParm());
   if (sqlNrow <= 0) {
      sqlerr("insert rsk_r001_data2 error");
      errExit(1);
   }
   return;
}

void selectActAnalSub() throws Exception {
   //-adj_yymm=201906, 調前年月(201812~201905) 調後年月(201907~201912) -
   //-act_anal_sub: acct_month要多減一個月-
   //-調前-
   if (tiSub <= 0) {
      sqlCmd = " select "
         + " sum(his_purchase_amt)+sum(his_cash_amt) as bef_amount "
         + " from act_anal_sub "
         + " where acct_month >=? and acct_month <? "
         + " and p_seqno = ? "
      ;
      tiSub = ppStmtCrt("tiSub", "");
   }
   //-調後-
   if (tiSub2 <= 0) {
      sqlCmd = " select "
         + " sum(his_purchase_amt)+sum(his_cash_amt) as aft_amount "
         + " from act_anal_sub "
         + " where acct_month >? and acct_month <=? "
         + " and p_seqno = ? "
      ;
      tiSub2 = ppStmtCrt("tiSub2", "");
   }

   String lsPurchYm1 = commDate.monthAdd(hPurchYm1,-1);
   String lsAdjYymm = commDate.monthAdd(isAdjYymm,-1);
   String lsPurchYm2 = commDate.monthAdd(hPurchYm2,-1);

   hTotAmtAfter = 0;
   hTotAmtBefore = 0;
   //-調整前-
   ppp(1, lsPurchYm1);
   ppp(lsAdjYymm);
   ppp(hAclg.acnoPSeqno);
   sqlSelect(tiSub);
   if (sqlNrow > 0) {
      hTotAmtBefore = colNum("bef_amount");
   }

   //-調整後-
   ppp(1, lsAdjYymm);
   ppp(lsPurchYm2);
   ppp(hAclg.acnoPSeqno);
   sqlSelect(tiSub2);
   if (sqlNrow > 0) {
      hTotAmtAfter = colNum("aft_amount");
   }
}

void insertR001Data1() throws Exception {

   if (ttData1 == null) {
      ttData1 = new com.Parm2sql();
      ttData1.insert("rsk_r001_data1");
   }

   double lmAmt =0;
   for (int ii = 0; ii <= 5; ii++) {
      ttData1.aaa("adj_yymm", hAdjYymm);
      ttData1.aaa("adj_loc_flag", hAdjLocFlag);
      ttData1.aaa("rec_no", ii + 1);
      String lsDesc = "";
      if (ii == 0)
         lsDesc = "0 - " + commString.numFormat(hAdjLimitE1, "#,##0");
      else if (ii == 1)
         lsDesc = commString.numFormat(hAdjLimitE1, "#,##0") + "(含) - " + commString.numFormat(hAdjLimitE2, "#,##0");
      else if (ii == 2)
         lsDesc = commString.numFormat(hAdjLimitE2, "#,##0") + "(含) - " + commString.numFormat(hAdjLimitE3, "#,##0");
      else if (ii == 3)
         lsDesc = commString.numFormat(hAdjLimitE3, "#,##0") + "(含) - " + commString.numFormat(hAdjLimitE4, "#,##0");
      else if (ii == 4)
         lsDesc = commString.numFormat(hAdjLimitE4, "#,##0") + "(含) - " + commString.numFormat(hAdjLimitE5, "#,##0");
      else if (ii == 5)
         lsDesc = commString.numFormat(hAdjLimitE5, "#,##0") + "(含)以上";
      ttData1.aaa("rec_desc", lsDesc);
      ttData1.aaa("acno_cnt", data1AcnoCnt[ii]);
      ttData1.aaa("bef_loc_amt", data1BefLocAmt[ii]);
      ttData1.aaa("aft_loc_amt", data1AftLocAmt[ii]);
      if (data1BefLocAmt[ii] == 0)
         ttData1.aaa("loc_amt_pct", 0);
      else {
         lmAmt =data1AftLocAmt[ii] - data1BefLocAmt[ii];
         ttData1.aaa("loc_amt_pct", (lmAmt / data1BefLocAmt[ii]) * 100);
      }

      ttData1.aaa("bef_purch_amt", data1BefPurchAmt[ii]);
      if (data1BefLocAmt[ii] == 0)
         ttData1.aaa("bef_limit_pct", 0);
      else ttData1.aaa("bef_limit_pct", (data1BefPurchAmt[ii] / data1BefLocAmt[ii]) * 100);

      ttData1.aaa("aft_purch_amt", data1AftPurchAmt[ii]);
      if (data1AftLocAmt[ii] == 0)
         ttData1.aaa("aft_limit_pct", 0);
      else ttData1.aaa("aft_limit_pct", (data1AftPurchAmt[ii] / data1AftLocAmt[ii]) * 100);

      if (data1AftPurchAmt[ii] == 0)
         ttData1.aaa("purch_grow_pct", 0);
      else {
         lmAmt =data1AftPurchAmt[ii] - data1BefPurchAmt[ii];
         ttData1.aaa("purch_grow_pct", (lmAmt / data1AftPurchAmt[ii]) * 100);
      }

      ttData1.aaaYmd("proc_date");
      ttData1.aaaDtime("mod_time");
      ttData1.aaa("mod_pgm", hModPgm);

      if (ttData1.ti <= 0) {
         ttData1.ti = ppStmtCrt("ttData1", ttData1.getConvSQL());
      }

      sqlExec(ttData1.ti, ttData1.getParms());
      if (sqlNrow <= 0) {
         sqlerr("insert rsk_r001_data1 error");
         errExit(1);
      }
   }

}

}
