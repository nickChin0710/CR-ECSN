/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-04-18  V1.00.01  Alex        program initial                          *
 *  2023-1026 V1.00.02  JH    取消呆帳戶:acct_status in (3,4)
 *  2023-1120 V1.00.03  JH    twMonth=busi_date-1MM
 *  2023-1121 V1.00.04  JH    re-coding
 *  2023-1127 V1.00.05  JH    his:YY01,02...12, model:YY12,11,10...01
 *  2023-1127 V1.00.06  JH    act_jcic_log.stmt_cycle+acct_month
 *  2023-1128 V1.00.07  JH    act_jcic_log.id_pseqno有二筆(A)
 *****************************************************************************/
package Rsk;

import com.BaseBatch;
import com.Parm2sql;

public class RskP196 extends BaseBatch {
private final String progname = "LGD/EAD 模型資料處理 2023-1128 V1.00.07";
//CommCrd comc = new CommCrd();
//	CommFTP commFTP = null;
//	CommRoutine comr = null;

//--act_jcic_log--
String logType="";
String acctMonth = "";
String twMonth = "";
//String idNo = "";
String idPSeqno = "";
double lineOfCreditAmt = 0;
double fileCreditAmt = 0;
double cashadvLimit = 0;
double ttlAmtBal = 0;
double useAmt = 0;
String acnoPSeqno = "";
int validCnt = 0;
String haveCard = "";
String paymentTimeRate = "";
double totAmtMonth = 0;
String adjEffStartDate = "";
String adjEffEndDate = "";
double acctCashBalance = 0;
double consumeAmt = 0;
String[] aaMonth=new String[12];
double hModSeqno=0;

//--lgd_ead_model
String branch = "";
String procMonth="";
String bCode_flag="";
double cycleRatio = 0;
double[] aaRatio = new double[12];
double[] aaLineAmt = new double[12];
double[] aaLineCash = new double[12];
double[] aaUseCrAmt = new double[12];
double[] aaAmtBal = new double[12];
double[] aaCashBal = new double[12];
String[] aaMCode = new String[12];
double[] aaConsAmt = new double[12];

//--write report
String trunMcode = "";

public static void main(String[] args) {
   RskP196 proc = new RskP196();
   // proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit();
}
//------------
String is_procMonth="";
@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP196 [business_date]");
      okExit(1);
   }

   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }
   dbConnect();

//   is_procMonth =commDate.monthAdd(hBusiDate,0,-1,0);
//   printf(" 處理年月=[%s], busi_date[%s] ...", is_procMonth, hBusiDate);

   clear_Lgd_ead_model();
   select_Ptr_workDay();
   sqlCommit();

   inti_Lgd_ead_data_his();

   sqlCommit();
   endProgram();
}
//--------------
void clear_Lgd_ead_model() throws Exception {
   int llCnt=0;
   while(true) {
      sqlCmd ="delete lgd_ead_model"
          +" where 1=1 "
          +commSqlStr.rownum(5000);
      sqlExec("");
      if (sqlNrow <=0) break;
      llCnt +=sqlNrow;
      sqlCommit();
   }
   printf("clear LGD_EAD_MODEL cnt[%s]", llCnt);
   //--
//   String ls_seqno=ecsModSeq(10);
//   hModSeqno =commString.ss2Num(ls_seqno);
//   printf(" process mod_seqno=[%s]", hModSeqno);
}
//--------------
void select_Ptr_workDay() throws Exception {
   sqlCmd ="select max(this_acct_month) proc_acct_month"
       +" from ptr_workday"
       +" where 1=1";
   sqlSelect("");
   if (sqlNrow >0) {
      is_procMonth =colSs("proc_acct_month");
   }
   printf(" 處理年月=[%s], busi_date[%s] ...", is_procMonth, hBusiDate);

   sqlCmd ="SELECT stmt_cycle, this_acct_month "+
       " FROM ptr_workday "+
       " WHERE 1=1 "+
       " ORDER BY stmt_cycle "
       ;
   daoTid ="wday.";
   sqlSelect();
   int ll_nRow=sqlNrow;
   for (int ii = 0; ii <ll_nRow ; ii++) {
      String ls_acctYM =colSs(ii,"wday.this_acct_month");
      String ls_cycle=colSs(ii,"wday.stmt_cycle");
      selectAct_jcic_log(ls_cycle,ls_acctYM);
   }
}
//--
void inti_Lgd_ead_data_his() throws Exception {
   String sql1=" "
       +"update lgd_ead_data_his set"
       +" cycle_ratio_1 =cycle_ratio_2"+
       ", cycle_ratio_2 =cycle_ratio_3"+
       ", cycle_ratio_3 =cycle_ratio_4"+
       ", cycle_ratio_4 =cycle_ratio_5"+
       ", cycle_ratio_5 =cycle_ratio_6"+
       ", cycle_ratio_6 =cycle_ratio_7"+
       ", cycle_ratio_7 =cycle_ratio_8"+
       ", cycle_ratio_8 =cycle_ratio_9"+
       ", cycle_ratio_9 =cycle_ratio_10"+
       ", cycle_ratio_10 =cycle_ratio_11"+
       ", cycle_ratio_11 =cycle_ratio_12"+
       ", cycle_ratio_12 =0"+
       ", lint_of_credit_amt_1  =lint_of_credit_amt_2 "+
       ", lint_of_credit_amt_2  =lint_of_credit_amt_3 "+
       ", lint_of_credit_amt_3  =lint_of_credit_amt_4 "+
       ", lint_of_credit_amt_4  =lint_of_credit_amt_5 "+
       ", lint_of_credit_amt_5  =lint_of_credit_amt_6 "+
       ", lint_of_credit_amt_6  =lint_of_credit_amt_7 "+
       ", lint_of_credit_amt_7  =lint_of_credit_amt_8 "+
       ", lint_of_credit_amt_8  =lint_of_credit_amt_9 "+
       ", lint_of_credit_amt_9  =lint_of_credit_amt_10 "+
       ", lint_of_credit_amt_10 =lint_of_credit_amt_11"+
       ", lint_of_credit_amt_11 =lint_of_credit_amt_12"+
       ", lint_of_credit_amt_12 =0 "+
       ", lint_of_credit_amt_cash_1  =lint_of_credit_amt_cash_2 "+
       ", lint_of_credit_amt_cash_2  =lint_of_credit_amt_cash_3 "+
       ", lint_of_credit_amt_cash_3  =lint_of_credit_amt_cash_4 "+
       ", lint_of_credit_amt_cash_4  =lint_of_credit_amt_cash_5 "+
       ", lint_of_credit_amt_cash_5  =lint_of_credit_amt_cash_6 "+
       ", lint_of_credit_amt_cash_6  =lint_of_credit_amt_cash_7 "+
       ", lint_of_credit_amt_cash_7  =lint_of_credit_amt_cash_8 "+
       ", lint_of_credit_amt_cash_8  =lint_of_credit_amt_cash_9 "+
       ", lint_of_credit_amt_cash_9  =lint_of_credit_amt_cash_10 "+
       ", lint_of_credit_amt_cash_10 =lint_of_credit_amt_cash_11 "+
       ", lint_of_credit_amt_cash_11 =lint_of_credit_amt_cash_12 "+
       ", lint_of_credit_amt_cash_12 =0 "+
       ", use_credit_amt_1  =use_credit_amt_2  "+
       ", use_credit_amt_2  =use_credit_amt_3  "+
       ", use_credit_amt_3  =use_credit_amt_4  "+
       ", use_credit_amt_4  =use_credit_amt_5  "+
       ", use_credit_amt_5  =use_credit_amt_6  "+
       ", use_credit_amt_6  =use_credit_amt_7  "+
       ", use_credit_amt_7  =use_credit_amt_8  "+
       ", use_credit_amt_8  =use_credit_amt_9  "+
       ", use_credit_amt_9  =use_credit_amt_10  "+
       ", use_credit_amt_10 =use_credit_amt_11 "+
       ", use_credit_amt_11 =use_credit_amt_12 "+
       ", use_credit_amt_12 =0 "+
       ", amt_balance_1  =amt_balance_2  "+
       ", amt_balance_2  =amt_balance_3  "+
       ", amt_balance_3  =amt_balance_4  "+
       ", amt_balance_4  =amt_balance_5  "+
       ", amt_balance_5  =amt_balance_6  "+
       ", amt_balance_6  =amt_balance_7  "+
       ", amt_balance_7  =amt_balance_8  "+
       ", amt_balance_8  =amt_balance_9  "+
       ", amt_balance_9  =amt_balance_10  "+
       ", amt_balance_10 =amt_balance_11 "+
       ", amt_balance_11 =amt_balance_12 "+
       ", amt_balance_12 =0 "+
       ", cash_balance_1  =cash_balance_2  "+
       ", cash_balance_2  =cash_balance_3  "+
       ", cash_balance_3  =cash_balance_4  "+
       ", cash_balance_4  =cash_balance_5  "+
       ", cash_balance_5  =cash_balance_6  "+
       ", cash_balance_6  =cash_balance_7  "+
       ", cash_balance_7  =cash_balance_8  "+
       ", cash_balance_8  =cash_balance_9  "+
       ", cash_balance_9  =cash_balance_10  "+
       ", cash_balance_10 =cash_balance_11 "+
       ", cash_balance_11 =cash_balance_12 "+
       ", cash_balance_12 =0 "+
       ", m_code_1  =m_code_2  "+
       ", m_code_2  =m_code_3  "+
       ", m_code_3  =m_code_4  "+
       ", m_code_4  =m_code_5  "+
       ", m_code_5  =m_code_6  "+
       ", m_code_6  =m_code_7  "+
       ", m_code_7  =m_code_8  "+
       ", m_code_8  =m_code_9  "+
       ", m_code_9  =m_code_10  "+
       ", m_code_10 =m_code_11 "+
       ", m_code_11 =m_code_12 "+
       ", m_code_12 ='K' "+
       ", consume_amt_1  =consume_amt_2  "+
       ", consume_amt_2  =consume_amt_3  "+
       ", consume_amt_3  =consume_amt_4  "+
       ", consume_amt_4  =consume_amt_5  "+
       ", consume_amt_5  =consume_amt_6  "+
       ", consume_amt_6  =consume_amt_7  "+
       ", consume_amt_7  =consume_amt_8  "+
       ", consume_amt_8  =consume_amt_9  "+
       ", consume_amt_9  =consume_amt_10  "+
       ", consume_amt_10 =consume_amt_11 "+
       ", consume_amt_11 =consume_amt_12 "+
       ", consume_amt_12 =0 "+
       ", mod_user =?, mod_time =sysdate, mod_pgm =?, mod_seqno =nvl(mod_seqno,0)+1"
       +" where 1=1 "
       +" and mod_user <>? "
       +commSqlStr.rownum(5000)
   ;
   int llCnt=0;
   while(true) {
      ppp(1,is_procMonth);
      ppp(hModPgm);
      //-KK-
      ppp(is_procMonth);

      sqlExec(sql1);
      if (sqlNrow <=0) break;
      llCnt +=sqlNrow;
      sqlCommit();
   }
   printf("init LGD_EAD_DATA_HIS.xxx_12 procMonth[%s] cnt[%s]", is_procMonth, llCnt);
}
//-------------
void selectAct_jcic_log(String a_cycle, String a_acctMonth) throws Exception {
   printf(" process data: cycle=[%s], acct_month=[%s]...", a_cycle, a_acctMonth);

   String ls_twMonth=commDate.toTwDate(is_procMonth+"01");
   ls_twMonth =commString.left(ls_twMonth,5);
   printf("-- process twMonth[%s]...", ls_twMonth);
   //-set data_month: XXX_12.month=procMonth-
   for (int ii =0; ii <12 ; ii++) {
      int mm= ii -11;
      aaMonth[ii] =commDate.monthAdd(is_procMonth, mm);
   }
   printf("dataMonth: [%s,%s...%s] [%s]", aaMonth[0],aaMonth[1],aaMonth[11],is_procMonth);
   if (empty(a_cycle) || empty(a_acctMonth)) {
      printf(" stmt_cycle[%s], acct_month[%s] can not empty",a_cycle,a_acctMonth);
      return;
   }

   sqlCmd = " select A.acct_month, A.log_type "
       //+", uf_idno_id(A.id_p_seqno) as id_no "
       +", A.id_p_seqno , A.p_seqno"
       +" , A.line_of_credit_amt , A.cashadv_limit "
       +", A.ttl_amt_bal"
       +", (A.stmt_this_ttl_amt + A.unpost_inst_fee + A.unpost_card_fee) as use_amt"
       +", A.valid_cnt , A.payment_time_rate "
       +", B.tot_amt_month, B.adj_eff_start_date , B.adj_eff_end_date"
//       +" , C.acct_cash_balance "
       +", (A.billed_end_bal_bl + A.billed_end_bal_it + A.billed_end_bal_id + A.billed_end_bal_ot) as consume_amt "
       +", A.billed_end_bal_CA "
       +" from act_jcic_log A join cca_card_acct B on A.p_seqno=B.acno_p_seqno"
//       +" join cca_acct_balance_cal C on A.p_seqno=C.acno_p_seqno  "
       +" where A.acct_type ='01'"
//          +" and A.acct_status in ('3','4')"
       +" and A.acct_month =?"
       +" and A.stmt_cycle =?"
       +" order by A.id_p_seqno, A.log_type"
   ;

//   setString(1, hBusiDate.substring(0, 6));
   setString(1, a_acctMonth);
   setString(2, a_cycle);
   openCursor();

   int ll_totCnt=0;
   while (fetchTable()) {
      if (dspProcRow(10000)) {
         sqlCommit();
      }
      totalCnt++;
      initData();
      ll_totCnt++;

      logType =colSs("log_type");
      acctMonth = colSs("acct_month");
      twMonth = ls_twMonth;  //commDate.toTwDate(commDate.dateAdd(acctMonth, 0, -1, 0)).substring(0, 5);
      //idNo = colSs("id_no");
      idPSeqno = colSs("id_p_seqno");
      lineOfCreditAmt = colNum("line_of_credit_amt");
      cashadvLimit = colNum("cashadv_limit");
      ttlAmtBal = colNum("ttl_amt_bal");
      useAmt = colNum("use_amt");
      acnoPSeqno = colSs("p_seqno");
      validCnt = colInt("valid_cnt");
      paymentTimeRate = colSs("payment_time_rate");
      totAmtMonth = colNum("tot_amt_month");
      adjEffStartDate = colSs("adj_eff_start_date");
      adjEffEndDate = colSs("adj_eff_end_date");
      acctCashBalance = colNum("billed_end_bal_CA");
      consumeAmt = colNum("consume_amt");
      //--
      selectCrd_card();
      //--額度處理
      if (adjEffStartDate.isEmpty() == false && adjEffEndDate.isEmpty() == false) {
         if (commString.between(hBusiDate, adjEffStartDate, adjEffEndDate)) {

         } else {
            totAmtMonth = 0;
         }
      }

      if (totAmtMonth > lineOfCreditAmt) {
         fileCreditAmt = totAmtMonth;
      } else {
         fileCreditAmt = lineOfCreditAmt;
      }

      if (ttlAmtBal > fileCreditAmt)
         fileCreditAmt = ttlAmtBal;

      //--循環比率
      if (fileCreditAmt <= 0) {
         cycleRatio = 0;
      } else {
         cycleRatio = ttlAmtBal / fileCreditAmt;
      }

      if (cycleRatio < 0)
         cycleRatio = 0;

      if (useAmt < 0)
         useAmt = 0;

      if (acctCashBalance < 0)
         acctCashBalance = 0;

      if (validCnt > 0)
         haveCard = "Y";
      //--
      covertPaymentRate();

      selectLgd_ead_data_his();
      //-本月值-
      aaRatio[11] =cycleRatio;
      aaLineAmt[11]   =lineOfCreditAmt;
      aaLineCash[11]  =cashadvLimit;
      aaUseCrAmt[11]  =useAmt;
      aaAmtBal[11]    =ttlAmtBal;
      aaCashBal[11]   =acctCashBalance;
      aaMCode[11]     =trunMcode;
      aaConsAmt[11]   =consumeAmt;
//      selectLgdEadModelData();
//      covertConsumeAmt();
//			textfileLgd();
      if (!eq(logType,"A")) {
         delete_Lgd_ead_model();
      }
      int liRC=insertLgd_ead_model();
      if (liRC ==0) {
         updateLgd_ead_data_his();
      }
   }
   closeCursor();
   sqlCommit();
   printf(" process count[%s], stmt_cycle[%s], acct_month[%s]"
       ,ll_totCnt,a_cycle,a_acctMonth);
}
//-------------
int tiCard=-1;
int tiCard2=-1;
void selectCrd_card() throws Exception {
   if (tiCard <=0) {
      sqlCmd ="select A.reg_bank_no"
//          +", (select id_no from crd_idno where id_p_seqno=A.id_p_seqno) as id_no"
          +" from crd_card A"
          +" where A.id_p_seqno =?"
          +" order by A.ori_issue_date desc"
          +commSqlStr.rownum(1)
          ;
      tiCard =ppStmtCrt("tiCard","");
   }
   ppp(1, idPSeqno);
   sqlSelect(tiCard);
   if (sqlNrow >0) {
//   idNo =colSs("id_no");
      branch =colSs("reg_bank_no");
   }

   //----------------------
   //b_code_flag //-x(1)  違約註記(有強停無有效卡)--
   if (tiCard2 <=0) {
      sqlCmd ="select sum(decode(current_code,'3',1,0)) card_cnt3"
          +", sum(decode(current_code,'0',1,0)) as card_cnt0"
          +" from crd_card"
          +" where id_p_seqno =?"
          ;
      tiCard2 =ppStmtCrt("tiCard2","");
   }
   ppp(1, idPSeqno);
   sqlSelect(tiCard2);
   if (sqlNrow <=0) return;
   if (colInt("card_cnt3")>0 && colInt("card_cnt0")==0) {
      bCode_flag ="Y";
   }
}
//--------
int tiDmodl=-1;
void delete_Lgd_ead_model() throws Exception {
   if (tiDmodl <=0) {
      sqlCmd ="delete lgd_ead_model"
          +" where id_p_seqno =? and data_date =?"
          ;
      tiDmodl =ppStmtCrt("tiDmodl","");
   }
   ppp(1, idPSeqno);
   ppp(twMonth);
   sqlExec(tiDmodl);
   if (sqlNrow <=0) return;
}
//-----------
com.Parm2sql ttUhis=null;
void updateLgd_ead_data_his() throws Exception {
   if (ttUhis ==null) {
      ttUhis =new Parm2sql();
      ttUhis.update("lgd_ead_data_his");
   }
   ttUhis.aaa("cycle_ratio_1"    ,aaRatio[0]);
   ttUhis.aaa("cycle_ratio_2"    ,aaRatio[1]);
   ttUhis.aaa("cycle_ratio_3"    ,aaRatio[2]);
   ttUhis.aaa("cycle_ratio_4"    ,aaRatio[3]);
   ttUhis.aaa("cycle_ratio_5"    ,aaRatio[4]);
   ttUhis.aaa("cycle_ratio_6"    ,aaRatio[5]);
   ttUhis.aaa("cycle_ratio_7"    ,aaRatio[6]);
   ttUhis.aaa("cycle_ratio_8"    ,aaRatio[7]);
   ttUhis.aaa("cycle_ratio_9"    ,aaRatio[8]);
   ttUhis.aaa("cycle_ratio_10"   ,aaRatio[9]);
   ttUhis.aaa("cycle_ratio_11"   ,aaRatio[10]);
   ttUhis.aaa("cycle_ratio_12"   ,aaRatio[11]);
   //--
   ttUhis.aaa("lint_of_credit_amt_1"   ,aaLineAmt[0]);
   ttUhis.aaa("lint_of_credit_amt_2"   ,aaLineAmt[1]);
   ttUhis.aaa("lint_of_credit_amt_3"   ,aaLineAmt[2]);
   ttUhis.aaa("lint_of_credit_amt_4"   ,aaLineAmt[3]);
   ttUhis.aaa("lint_of_credit_amt_5"   ,aaLineAmt[4]);
   ttUhis.aaa("lint_of_credit_amt_6"   ,aaLineAmt[5]);
   ttUhis.aaa("lint_of_credit_amt_7"   ,aaLineAmt[6]);
   ttUhis.aaa("lint_of_credit_amt_8"   ,aaLineAmt[7]);
   ttUhis.aaa("lint_of_credit_amt_9"   ,aaLineAmt[8]);
   ttUhis.aaa("lint_of_credit_amt_10"  ,aaLineAmt[9]);
   ttUhis.aaa("lint_of_credit_amt_11"  ,aaLineAmt[10]);
   ttUhis.aaa("lint_of_credit_amt_12"  ,aaLineAmt[11]);
   //--
   ttUhis.aaa("lint_of_credit_amt_cash_1"  ,aaLineCash[0]);
   ttUhis.aaa("lint_of_credit_amt_cash_2"  ,aaLineCash[1]);
   ttUhis.aaa("lint_of_credit_amt_cash_3"  ,aaLineCash[2]);
   ttUhis.aaa("lint_of_credit_amt_cash_4"  ,aaLineCash[3]);
   ttUhis.aaa("lint_of_credit_amt_cash_5"  ,aaLineCash[4]);
   ttUhis.aaa("lint_of_credit_amt_cash_6"  ,aaLineCash[5]);
   ttUhis.aaa("lint_of_credit_amt_cash_7"  ,aaLineCash[6]);
   ttUhis.aaa("lint_of_credit_amt_cash_8"  ,aaLineCash[7]);
   ttUhis.aaa("lint_of_credit_amt_cash_9"  ,aaLineCash[8]);
   ttUhis.aaa("lint_of_credit_amt_cash_10"  ,aaLineCash[9]);
   ttUhis.aaa("lint_of_credit_amt_cash_11"  ,aaLineCash[10]);
   ttUhis.aaa("lint_of_credit_amt_cash_12"  ,aaLineCash[11]);
   //--
   ttUhis.aaa("use_credit_amt_1"     ,aaUseCrAmt[0]);
   ttUhis.aaa("use_credit_amt_2"     ,aaUseCrAmt[1]);
   ttUhis.aaa("use_credit_amt_3"     ,aaUseCrAmt[2]);
   ttUhis.aaa("use_credit_amt_4"     ,aaUseCrAmt[3]);
   ttUhis.aaa("use_credit_amt_5"     ,aaUseCrAmt[4]);
   ttUhis.aaa("use_credit_amt_6"     ,aaUseCrAmt[5]);
   ttUhis.aaa("use_credit_amt_7"     ,aaUseCrAmt[6]);
   ttUhis.aaa("use_credit_amt_8"     ,aaUseCrAmt[7]);
   ttUhis.aaa("use_credit_amt_9"     ,aaUseCrAmt[8]);
   ttUhis.aaa("use_credit_amt_10"     ,aaUseCrAmt[9]);
   ttUhis.aaa("use_credit_amt_11"     ,aaUseCrAmt[10]);
   ttUhis.aaa("use_credit_amt_12"     ,aaUseCrAmt[11]);
   //--
   ttUhis.aaa("amt_balance_1"     ,aaAmtBal[0]);
   ttUhis.aaa("amt_balance_2"     ,aaAmtBal[1]);
   ttUhis.aaa("amt_balance_3"     ,aaAmtBal[2]);
   ttUhis.aaa("amt_balance_4"     ,aaAmtBal[3]);
   ttUhis.aaa("amt_balance_5"     ,aaAmtBal[4]);
   ttUhis.aaa("amt_balance_6"     ,aaAmtBal[5]);
   ttUhis.aaa("amt_balance_7"     ,aaAmtBal[6]);
   ttUhis.aaa("amt_balance_8"     ,aaAmtBal[7]);
   ttUhis.aaa("amt_balance_9"     ,aaAmtBal[8]);
   ttUhis.aaa("amt_balance_10"     ,aaAmtBal[9]);
   ttUhis.aaa("amt_balance_11"     ,aaAmtBal[10]);
   ttUhis.aaa("amt_balance_12"     ,aaAmtBal[11]);
   //--
   ttUhis.aaa("cash_balance_1"     ,aaCashBal[0]);
   ttUhis.aaa("cash_balance_2"     ,aaCashBal[1]);
   ttUhis.aaa("cash_balance_3"     ,aaCashBal[2]);
   ttUhis.aaa("cash_balance_4"     ,aaCashBal[3]);
   ttUhis.aaa("cash_balance_5"     ,aaCashBal[4]);
   ttUhis.aaa("cash_balance_6"     ,aaCashBal[5]);
   ttUhis.aaa("cash_balance_7"     ,aaCashBal[6]);
   ttUhis.aaa("cash_balance_8"     ,aaCashBal[7]);
   ttUhis.aaa("cash_balance_9"     ,aaCashBal[8]);
   ttUhis.aaa("cash_balance_10"     ,aaCashBal[9]);
   ttUhis.aaa("cash_balance_11"     ,aaCashBal[10]);
   ttUhis.aaa("cash_balance_12"     ,aaCashBal[11]);
   //
   ttUhis.aaa("m_code_1"    ,aaMCode[0]);
   ttUhis.aaa("m_code_2"    ,aaMCode[1]);
   ttUhis.aaa("m_code_3"    ,aaMCode[2]);
   ttUhis.aaa("m_code_4"    ,aaMCode[3]);
   ttUhis.aaa("m_code_5"    ,aaMCode[4]);
   ttUhis.aaa("m_code_6"    ,aaMCode[5]);
   ttUhis.aaa("m_code_7"    ,aaMCode[6]);
   ttUhis.aaa("m_code_8"    ,aaMCode[7]);
   ttUhis.aaa("m_code_9"    ,aaMCode[8]);
   ttUhis.aaa("m_code_10"    ,aaMCode[9]);
   ttUhis.aaa("m_code_11"    ,aaMCode[10]);
   ttUhis.aaa("m_code_12"    ,aaMCode[11]);
   //--
   ttUhis.aaa("consume_amt_1"     ,aaConsAmt[0]);
   ttUhis.aaa("consume_amt_2"     ,aaConsAmt[1]);
   ttUhis.aaa("consume_amt_3"     ,aaConsAmt[2]);
   ttUhis.aaa("consume_amt_4"     ,aaConsAmt[3]);
   ttUhis.aaa("consume_amt_5"     ,aaConsAmt[4]);
   ttUhis.aaa("consume_amt_6"     ,aaConsAmt[5]);
   ttUhis.aaa("consume_amt_7"     ,aaConsAmt[6]);
   ttUhis.aaa("consume_amt_8"     ,aaConsAmt[7]);
   ttUhis.aaa("consume_amt_9"     ,aaConsAmt[8]);
   ttUhis.aaa("consume_amt_10"     ,aaConsAmt[9]);
   ttUhis.aaa("consume_amt_11"     ,aaConsAmt[10]);
   ttUhis.aaa("consume_amt_12"     ,aaConsAmt[11]);
   //--
   ttUhis.aaa("mod_user"      ,is_procMonth);
   ttUhis.aaaDtime("mod_time");
   ttUhis.aaa("mod_pgm"       ,hModPgm);
   ttUhis.aaaFunc("mod_seqno","nvl(mod_seqno,0)+1" ,"");
   //--
   ttUhis.aaaWhere(" where data_type ='1'"   ,"");
   ttUhis.aaaWhere(" and data_p_seqno =?"    ,idPSeqno);

   if (ttUhis.ti <=0) {
      ttUhis.ti =ppStmtCrt("ttUhis",ttUhis.getSql());
   }

   sqlExec(ttUhis.ti, ttUhis.getParms());
   if (sqlNrow <=0) {
      insertLgd_ead_data_his();
   }
}
//---
com.Parm2sql ttAhis=null;
void insertLgd_ead_data_his() throws Exception {
   if (ttAhis == null) {
      ttAhis = new Parm2sql();
      ttAhis.insert("lgd_ead_data_his");
   }
   ttAhis.aaa("data_p_seqno", idPSeqno);
   ttAhis.aaa("data_type", "1");
   ttAhis.aaa("cycle_ratio_12"         , aaRatio[11]);
   ttAhis.aaa("lint_of_credit_amt_12"  , aaLineAmt[11]);
   ttAhis.aaa("lint_of_credit_amt_cash_12"   , aaLineCash[11]);
   ttAhis.aaa("use_credit_amt_12"      , aaUseCrAmt[11]);
   ttAhis.aaa("amt_balance_12"         , aaAmtBal[11]);
   ttAhis.aaa("cash_balance_12"        , aaCashBal[11]);
   ttAhis.aaa("m_code_12"              , aaMCode[11]);
   ttAhis.aaa("consume_amt_12"         , aaConsAmt[11]);
   ttAhis.aaa("crt_date", sysDate);
   ttAhis.aaa("crt_time", sysTime);
   ttAhis.aaa("mod_user", is_procMonth);
   ttAhis.aaaDtime("mod_time");
   ttAhis.aaa("mod_pgm", hModPgm);
   ttAhis.aaa("mod_seqno", 1);

   if (ttAhis.ti <= 0) {
      ttAhis.ti = ppStmtCrt("ttAhis", ttAhis.getSql());
   }

   sqlExec(ttAhis.ti, ttAhis.getParms());
   if (sqlNrow <=0) {
      printf("insert lgd_ead_data_his error, kk[%s]", idPSeqno);
   }
}

//---
com.Parm2sql ttAmodl=null;
int insertLgd_ead_model() throws Exception {
   if (ttAmodl ==null) {
      ttAmodl =new Parm2sql();
      ttAmodl.insert("lgd_ead_model");
   }
   ttAmodl.aaa("data_date"    ,twMonth);
   ttAmodl.aaa("branch"       ,branch);
   ttAmodl.aaa("id_p_seqno"   ,idPSeqno);
   ttAmodl.aaa("card_flag"    ,haveCard);
   ttAmodl.aaa("b_code_flag"  ,bCode_flag);
   //--updateSQL += "cycle_ratio_1 = ? , ";
   ttAmodl.aaa("cycle_ratio_1"    ,aaRatio[11]);
   ttAmodl.aaa("cycle_ratio_2"    ,aaRatio[10]);
   ttAmodl.aaa("cycle_ratio_3"    ,aaRatio[9]);
   ttAmodl.aaa("cycle_ratio_4"    ,aaRatio[8]);
   ttAmodl.aaa("cycle_ratio_5"    ,aaRatio[7]);
   ttAmodl.aaa("cycle_ratio_6"    ,aaRatio[6]);
   ttAmodl.aaa("cycle_ratio_7"    ,aaRatio[5]);
   ttAmodl.aaa("cycle_ratio_8"    ,aaRatio[4]);
   ttAmodl.aaa("cycle_ratio_9"    ,aaRatio[3]);
   ttAmodl.aaa("cycle_ratio_10"   ,aaRatio[2]);
   ttAmodl.aaa("cycle_ratio_11"   ,aaRatio[1]);
   ttAmodl.aaa("cycle_ratio_12"   ,aaRatio[0]);
   //--"lint_of_credit_amt_1 = ? ,";
   ttAmodl.aaa("lint_of_credit_amt_1"   ,aaLineAmt[11]);
   ttAmodl.aaa("lint_of_credit_amt_2"   ,aaLineAmt[10]);
   ttAmodl.aaa("lint_of_credit_amt_3"   ,aaLineAmt[9]);
   ttAmodl.aaa("lint_of_credit_amt_4"   ,aaLineAmt[8]);
   ttAmodl.aaa("lint_of_credit_amt_5"   ,aaLineAmt[7]);
   ttAmodl.aaa("lint_of_credit_amt_6"   ,aaLineAmt[6]);
   ttAmodl.aaa("lint_of_credit_amt_7"   ,aaLineAmt[5]);
   ttAmodl.aaa("lint_of_credit_amt_8"   ,aaLineAmt[4]);
   ttAmodl.aaa("lint_of_credit_amt_9"   ,aaLineAmt[3]);
   ttAmodl.aaa("lint_of_credit_amt_10"  ,aaLineAmt[2]);
   ttAmodl.aaa("lint_of_credit_amt_11"  ,aaLineAmt[1]);
   ttAmodl.aaa("lint_of_credit_amt_12"  ,aaLineAmt[0]);
   //--"lint_of_credit_amt_cash_1 = ? ,";
   ttAmodl.aaa("lint_of_credit_amt_cash_1"   ,aaLineCash[11]);
   ttAmodl.aaa("lint_of_credit_amt_cash_2"   ,aaLineCash[10]);
   ttAmodl.aaa("lint_of_credit_amt_cash_3"   ,aaLineCash[9]);
   ttAmodl.aaa("lint_of_credit_amt_cash_4"   ,aaLineCash[8]);
   ttAmodl.aaa("lint_of_credit_amt_cash_5"   ,aaLineCash[7]);
   ttAmodl.aaa("lint_of_credit_amt_cash_6"   ,aaLineCash[6]);
   ttAmodl.aaa("lint_of_credit_amt_cash_7"   ,aaLineCash[5]);
   ttAmodl.aaa("lint_of_credit_amt_cash_8"   ,aaLineCash[4]);
   ttAmodl.aaa("lint_of_credit_amt_cash_9"   ,aaLineCash[3]);
   ttAmodl.aaa("lint_of_credit_amt_cash_10"  ,aaLineCash[2]);
   ttAmodl.aaa("lint_of_credit_amt_cash_11"  ,aaLineCash[1]);
   ttAmodl.aaa("lint_of_credit_amt_cash_12"  ,aaLineCash[0]);
   //--"use_credit_amt_1 = ? ,";
   ttAmodl.aaa("use_credit_amt_1"      ,aaUseCrAmt[11]);
   ttAmodl.aaa("use_credit_amt_2"      ,aaUseCrAmt[10]);
   ttAmodl.aaa("use_credit_amt_3"      ,aaUseCrAmt[9]);
   ttAmodl.aaa("use_credit_amt_4"      ,aaUseCrAmt[8]);
   ttAmodl.aaa("use_credit_amt_5"      ,aaUseCrAmt[7]);
   ttAmodl.aaa("use_credit_amt_6"      ,aaUseCrAmt[6]);
   ttAmodl.aaa("use_credit_amt_7"      ,aaUseCrAmt[5]);
   ttAmodl.aaa("use_credit_amt_8"      ,aaUseCrAmt[4]);
   ttAmodl.aaa("use_credit_amt_9"      ,aaUseCrAmt[3]);
   ttAmodl.aaa("use_credit_amt_10"     ,aaUseCrAmt[2]);
   ttAmodl.aaa("use_credit_amt_11"     ,aaUseCrAmt[1]);
   ttAmodl.aaa("use_credit_amt_12"     ,aaUseCrAmt[0]);
   //--"amt_balance_1 = ? ,";
   ttAmodl.aaa("amt_balance_1"      ,aaAmtBal[11]);
   ttAmodl.aaa("amt_balance_2"      ,aaAmtBal[10]);
   ttAmodl.aaa("amt_balance_3"      ,aaAmtBal[9]);
   ttAmodl.aaa("amt_balance_4"      ,aaAmtBal[8]);
   ttAmodl.aaa("amt_balance_5"      ,aaAmtBal[7]);
   ttAmodl.aaa("amt_balance_6"      ,aaAmtBal[6]);
   ttAmodl.aaa("amt_balance_7"      ,aaAmtBal[5]);
   ttAmodl.aaa("amt_balance_8"      ,aaAmtBal[4]);
   ttAmodl.aaa("amt_balance_9"      ,aaAmtBal[3]);
   ttAmodl.aaa("amt_balance_10"     ,aaAmtBal[2]);
   ttAmodl.aaa("amt_balance_11"     ,aaAmtBal[1]);
   ttAmodl.aaa("amt_balance_12"     ,aaAmtBal[0]);
   //--"cash_balance_1 = ? ,";
   ttAmodl.aaa("cash_balance_1"      ,aaCashBal[11]);
   ttAmodl.aaa("cash_balance_2"      ,aaCashBal[10]);
   ttAmodl.aaa("cash_balance_3"      ,aaCashBal[9]);
   ttAmodl.aaa("cash_balance_4"      ,aaCashBal[8]);
   ttAmodl.aaa("cash_balance_5"      ,aaCashBal[7]);
   ttAmodl.aaa("cash_balance_6"      ,aaCashBal[6]);
   ttAmodl.aaa("cash_balance_7"      ,aaCashBal[5]);
   ttAmodl.aaa("cash_balance_8"      ,aaCashBal[4]);
   ttAmodl.aaa("cash_balance_9"      ,aaCashBal[3]);
   ttAmodl.aaa("cash_balance_10"     ,aaCashBal[2]);
   ttAmodl.aaa("cash_balance_11"     ,aaCashBal[1]);
   ttAmodl.aaa("cash_balance_12"     ,aaCashBal[0]);
   //"m_code_1 = ? ,"--
   ttAmodl.aaa("m_code_1"     ,aaMCode[11]);
   ttAmodl.aaa("m_code_2"     ,aaMCode[10]);
   ttAmodl.aaa("m_code_3"     ,aaMCode[9]);
   ttAmodl.aaa("m_code_4"     ,aaMCode[8]);
   ttAmodl.aaa("m_code_5"     ,aaMCode[7]);
   ttAmodl.aaa("m_code_6"     ,aaMCode[6]);
   ttAmodl.aaa("m_code_7"     ,aaMCode[5]);
   ttAmodl.aaa("m_code_8"     ,aaMCode[4]);
   ttAmodl.aaa("m_code_9"     ,aaMCode[3]);
   ttAmodl.aaa("m_code_10"    ,aaMCode[2]);
   ttAmodl.aaa("m_code_11"    ,aaMCode[1]);
   ttAmodl.aaa("m_code_12"    ,aaMCode[0]);
   //--"consume_amt_1 = ? ,--
   ttAmodl.aaa("consume_amt_1"      ,aaConsAmt[11]);
   ttAmodl.aaa("consume_amt_2"      ,aaConsAmt[10]);
   ttAmodl.aaa("consume_amt_3"      ,aaConsAmt[9]);
   ttAmodl.aaa("consume_amt_4"      ,aaConsAmt[8]);
   ttAmodl.aaa("consume_amt_5"      ,aaConsAmt[7]);
   ttAmodl.aaa("consume_amt_6"      ,aaConsAmt[6]);
   ttAmodl.aaa("consume_amt_7"      ,aaConsAmt[5]);
   ttAmodl.aaa("consume_amt_8"      ,aaConsAmt[4]);
   ttAmodl.aaa("consume_amt_9"      ,aaConsAmt[3]);
   ttAmodl.aaa("consume_amt_10"     ,aaConsAmt[2]);
   ttAmodl.aaa("consume_amt_11"     ,aaConsAmt[1]);
   ttAmodl.aaa("consume_amt_12"     ,aaConsAmt[0]);
   //--
   ttAmodl.aaa("crt_date"     ,sysDate);
   ttAmodl.aaa("crt_time"     ,sysTime);
   ttAmodl.aaa("mod_user"     ,is_procMonth);  //-XXX_12值代表:處理acct_month--
   ttAmodl.aaaDtime("mod_time");
   ttAmodl.aaa("mod_pgm"      ,hModPgm);
   ttAmodl.aaa("mod_seqno"    ,1);

   if (ttAmodl.ti <=0) {
      ttAmodl.ti =ppStmtCrt("ttAmodl",ttAmodl.getSql());
   }

   sqlExec(ttAmodl.ti, ttAmodl.getParms());
   if (sqlNrow <=0) {
      printf("insert lgd_ead_model error, kk[%s]", idPSeqno);
      return 1;
   }
   return 0;
}

void covertConsumeAmt() {
//   double tmp[] = new double[12];
//   String convertTmp[] = new String[12];
//   DecimalFormat df = new DecimalFormat("0");
//   tmp[0] = consumeAmt;
//   tmp[1] = consumeAmt1;
//   tmp[2] = consumeAmt2;
//   tmp[3] = consumeAmt3;
//   tmp[4] = consumeAmt4;
//   tmp[5] = consumeAmt5;
//   tmp[6] = consumeAmt6;
//   tmp[7] = consumeAmt7;
//   tmp[8] = consumeAmt8;
//   tmp[9] = consumeAmt9;
//   tmp[10] = consumeAmt10;
//   tmp[11] = consumeAmt11;
//   double temp = 0;
//   String tmpStr = "", tmpSign = "", lastStr = "";
//   for (int i = 0; i < 12; i++) {
//      temp = tmp[i];
//      if (temp < 0)
//         temp = 0;
//      tmpStr = df.format(temp);
//      if (temp < 10) {
//         lastStr = tmpStr;
//      } else {
//         lastStr = tmpStr.substring(tmpStr.length()-1, tmpStr.length());
//      }
//
//      if (temp >= 0) {
//         switch (lastStr) {
//            case "0":
//               tmpSign = "{";
//               break;
//            case "1":
//               tmpSign = "A";
//               break;
//            case "2":
//               tmpSign = "B";
//               break;
//            case "3":
//               tmpSign = "C";
//               break;
//            case "4":
//               tmpSign = "D";
//               break;
//            case "5":
//               tmpSign = "E";
//               break;
//            case "6":
//               tmpSign = "F";
//               break;
//            case "7":
//               tmpSign = "G";
//               break;
//            case "8":
//               tmpSign = "H";
//               break;
//            case "9":
//               tmpSign = "I";
//               break;
//         }
//      }
//      convertTmp[i] = commString.lpad(tmpStr.substring(0, tmpStr.length()-1)+tmpSign, 11, "0");
//   }
//
//   trunConsume = convertTmp[0];
//   trunConsume1 = convertTmp[1];
//   trunConsume2 = convertTmp[2];
//   trunConsume3 = convertTmp[3];
//   trunConsume4 = convertTmp[4];
//   trunConsume5 = convertTmp[5];
//   trunConsume6 = convertTmp[6];
//   trunConsume7 = convertTmp[7];
//   trunConsume8 = convertTmp[8];
//   trunConsume9 = convertTmp[9];
//   trunConsume10 = convertTmp[10];
//   trunConsume11 = convertTmp[11];

}

void covertPaymentRate() {
   //1,2,3,4,5,6,7,N,X--
   String tmpSign = "";
   switch (paymentTimeRate) {
      case "0":
         tmpSign = "0";
         trunMcode = "A";
         break;
      case "1":
         tmpSign = "1";
         trunMcode = "A";
         break;
      case "2":
         tmpSign = "2";
         trunMcode = "B";
         break;
      case "3":
         tmpSign = "3";
         trunMcode = "C";
         break;
      case "4":
         tmpSign = "4";
         trunMcode = "D";
         break;
      case "5":
         tmpSign = "5";
         trunMcode = "E";
         break;
      case "6":
         tmpSign = "6";
         trunMcode = "F";
         break;
      case "7":
         tmpSign = "7";
         trunMcode = "G";
         break;
      case "N":
         tmpSign = "-1";
         trunMcode = "J";
         break;
      case "X":
         tmpSign = "-2";
         trunMcode = "K";
         break;
   }

//   switch (tmpSign) {
//      case "1":
//         trunMcode = "A";
//         break;
//      case "2":
//         trunMcode = "B";
//         break;
//      case "3":
//         trunMcode = "C";
//         break;
//      case "4":
//         trunMcode = "D";
//         break;
//      case "5":
//         trunMcode = "E";
//         break;
//      case "6":
//         trunMcode = "F";
//         break;
//      case "7":
//         trunMcode = "G";
//         break;
//      case "-2":
//         trunMcode = "K";
//         break;
//      case "-1":
//         trunMcode = "J";
//         break;
//      case "0":
//         trunMcode = "A";
//         break;
//   }

}
//---------
int tiLhis=-1;
void selectLgd_ead_data_his() throws Exception {
   if (tiLhis <= 0) {
      sqlCmd = " select A.* "
          +", A.mod_user proc_month"
          +" from lgd_ead_data_his A "
          +" where A.data_p_seqno = ? "
          +" and A.data_type ='1' "
      ;
      tiLhis = ppStmtCrt("ti-Lgd-his", "");
   }

   ppp(1, idPSeqno);
   sqlSelect(tiLhis);
   if (sqlNrow <= 0) {
      return;
   }

   String ls_hisMM =colSs("proc_month");
   if (commDate.isDate(ls_hisMM+"01") ==false) {
      ls_hisMM =aaMonth[10];
   }
   String ls_MM01=commDate.monthAdd(ls_hisMM,-11);

   //-YYMM: YY01,02,03...12--
   boolean lb_month=false;
   int col=0, ii=-1;
   for (int kk =0; kk <12 ; kk++) {
      col++;
      if (!lb_month && eq(aaMonth[0],ls_MM01)) {
         lb_month =true;
      }
      if (!lb_month) {
         ls_MM01 =commDate.monthAdd(ls_MM01,1);
         continue;
      }

      ii++;
      aaRatio[ii] =colNum("cycle_ratio_"+col);
      aaLineAmt[ii] = colNum("lint_of_credit_amt_"+col);
      aaLineCash[ii] =colNum("lint_of_credit_amt_cash_"+col);
      aaUseCrAmt[ii] =colNum("use_credit_amt_"+col);
      aaAmtBal[ii] = colNum("amt_balance_"+col);
      aaCashBal[ii] = colNum("cash_balance_"+col);
      aaMCode[ii] = colSs("m_code_"+col);
      aaConsAmt[ii]= colNum("consume_amt_"+col);
   }
}
//----------
//void textfileLgd() throws Exception {
//
//   String tmpString = "";
//   StringBuffer tt = new StringBuffer();
//   StringBuffer ee = new StringBuffer();
//   DecimalFormat df = new DecimalFormat("0");
//
//   //--資料日期 X5
//   tt.append(commString.bbFixlen(twMonth, 5));
//
//   //--分行 X4
//   tt.append(commString.bbFixlen(branch, 5));
//
//   //--身分證字號 X10
//   tt.append(commString.bbFixlen(idNo, 10));
//
//   //--客戶性質 X2 放空白
//   tt.append(commString.space(2));
//
//   //--貸款額度 X11 放空白
//   tt.append(commString.space(11));
//
//   //--還本繳息方式 X1 放空白
//   tt.append(commString.space(1));
//
//   //--平均相對帳齡 X4 放空白
//   tt.append(commString.space(4));
//
//   //--貸放超逾6個月註記 X1
//   tt.append(commString.space(1));
//
//   //--是否有本行信用卡 X1
//   tt.append(commString.bbFixlen(haveCard, 1));
//
//   //--信貸平均還款本金 X11 放空白
//   tt.append(commString.space(11));
//
//   //--房貸平均放款本金 X11 放空白
//   tt.append(commString.space(11));
//
//   //--是否有房貸 X1 放空白
//   tt.append(commString.space(1));
//
//   //--違約註記 X1 放空白
//   tt.append(commString.space(1));
//
//   //--X324 放空白
//   tt.append(commString.space(324));
//
//   //--X6 近1個月循環信用比率
//   tmpString = df.format(cycleRatio * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近2個月循環信用比率
//   tmpString = df.format(cycleRatio1 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近3個月循環信用比率
//   tmpString = df.format(cycleRatio2 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近4個月循環信用比率
//   tmpString = df.format(cycleRatio3 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近5個月循環信用比率
//   tmpString = df.format(cycleRatio4 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近6個月循環信用比率
//   tmpString = df.format(cycleRatio5 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近7個月循環信用比率
//   tmpString = df.format(cycleRatio6 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近8個月循環信用比率
//   tmpString = df.format(cycleRatio7 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近9個月循環信用比率
//   tmpString = df.format(cycleRatio8 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近10個月循環信用比率
//   tmpString = df.format(cycleRatio9 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近11個月循環信用比率
//   tmpString = df.format(cycleRatio10 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X6 近12個月循環信用比率
//   tmpString = df.format(cycleRatio11 * 10000);
//   tt.append(commString.lpad(tmpString, 6, "0"));
//
//   //--X11 近1個月信用卡永久額度
//   tmpString = df.format(fileCreditAmt);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近2個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt1);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近3個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt2);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近4個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt3);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近5個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt4);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近6個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt5);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近7個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt6);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近8個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt7);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近9個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt8);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近10個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt9);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近11個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt10);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近12個月信用卡永久額度
//   tmpString = df.format(lineOfCreditAmt11);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近1個月預借現金額度
//   tmpString = df.format(cashadvLimit);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近2個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash1);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近3個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash2);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近4個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash3);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近5個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash4);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近6個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash5);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近7個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash6);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近8個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash7);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近9個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash8);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近10個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash9);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近11個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash10);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近12個月預借現金額度
//   tmpString = df.format(lineOfCreditAmtCash11);
//   if ("0".equals(tmpString))
//      tmpString = "-9999999999";
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近1個月信用卡使用額度
//   tmpString = df.format(useAmt);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近2個月信用卡使用額度
//   tmpString = df.format(useCreditAmt1);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近3個月信用卡使用額度
//   tmpString = df.format(useCreditAmt2);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近4個月信用卡使用額度
//   tmpString = df.format(useCreditAmt3);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近5個月信用卡使用額度
//   tmpString = df.format(useCreditAmt4);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近6個月信用卡使用額度
//   tmpString = df.format(useCreditAmt5);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近7個月信用卡使用額度
//   tmpString = df.format(useCreditAmt6);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近8個月信用卡使用額度
//   tmpString = df.format(useCreditAmt7);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近9個月信用卡使用額度
//   tmpString = df.format(useCreditAmt8);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近10個月信用卡使用額度
//   tmpString = df.format(useCreditAmt9);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近11個月信用卡使用額度
//   tmpString = df.format(useCreditAmt10);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近12個月信用卡使用額度
//   tmpString = df.format(useCreditAmt11);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近1個月循環餘額
//   tmpString = df.format(ttlAmtBal);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近2個月循環餘額
//   tmpString = df.format(amtBalance1);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近3個月循環餘額
//   tmpString = df.format(amtBalance2);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近4個月循環餘額
//   tmpString = df.format(amtBalance3);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近5個月循環餘額
//   tmpString = df.format(amtBalance4);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近6個月循環餘額
//   tmpString = df.format(amtBalance5);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近7個月循環餘額
//   tmpString = df.format(amtBalance6);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近8個月循環餘額
//   tmpString = df.format(amtBalance7);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近9個月循環餘額
//   tmpString = df.format(amtBalance8);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近10個月循環餘額
//   tmpString = df.format(amtBalance9);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近11個月循環餘額
//   tmpString = df.format(amtBalance10);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近12個月循環餘額
//   tmpString = df.format(amtBalance11);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近1個月預借現金餘額
//   tmpString = df.format(acctCashBalance);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近2個月預借現金餘額
//   tmpString = df.format(cashBalance1);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近3個月預借現金餘額
//   tmpString = df.format(cashBalance2);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近4個月預借現金餘額
//   tmpString = df.format(cashBalance3);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近5個月預借現金餘額
//   tmpString = df.format(cashBalance4);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近6個月預借現金餘額
//   tmpString = df.format(cashBalance5);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近7個月預借現金餘額
//   tmpString = df.format(cashBalance6);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近8個月預借現金餘額
//   tmpString = df.format(cashBalance7);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近9個月預借現金餘額
//   tmpString = df.format(cashBalance8);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近10個月預借現金餘額
//   tmpString = df.format(cashBalance9);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近11個月預借現金餘額
//   tmpString = df.format(cashBalance10);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X11 近12個月預借現金餘額
//   tmpString = df.format(cashBalance11);
//   tt.append(commString.lpad(tmpString, 11, "0"));
//
//   //--X1 近1個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(trunMcode, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode1, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode2, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode3, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode4, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode5, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode6, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode7, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode8, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode9, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode10, 1));
//
//   //--X1 近2個月信用卡繳款狀況
//   tt.append(commString.bbFixlen(mCode11, 1));
//
//   //--將LGD文件複製到EAD文件
//   ee = tt;
//
//   //--換行符號 0D0A
//   tt.append(newLine);
//
//   writeTextFile(iiFileNum, tt.toString());
//
//   //--X11 近1個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume, 1));
//
//   //--X11 近2個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume1, 1));
//
//   //--X11 近3個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume2, 1));
//
//   //--X11 近4個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume3, 1));
//
//   //--X11 近5個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume4, 1));
//
//   //--X11 近6個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume5, 1));
//
//   //--X11 近7個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume6, 1));
//
//   //--X11 近8個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume7, 1));
//
//   //--X11 近9個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume8, 1));
//
//   //--X11 近10個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume9, 1));
//
//   //--X11 近11個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume10, 1));
//
//   //--X11 近12個月信用卡消費金額
//   ee.append(commString.bbFixlen(trunConsume11, 1));
//
//   //--換行符號0D0A
//   ee.append(newLine);
//
//   writeTextFile(iiFileNum2, ee.toString());
//}

void initData() {
   acctMonth = "";
//   idNo = "";
   idPSeqno = "";
   lineOfCreditAmt = 0;
   cashadvLimit = 0;
   ttlAmtBal = 0;
   useAmt = 0;
   acnoPSeqno = "";
   validCnt = 0;
   paymentTimeRate = "";
   totAmtMonth = 0;
   adjEffStartDate = "";
   adjEffEndDate = "";
   fileCreditAmt = 0;
   acctCashBalance = 0;
   branch = "";
   bCode_flag="";
   procMonth ="";
   cycleRatio = 0;
   for (int ii = 0; ii <12 ; ii++) {
      aaRatio[ii] =0;
      aaLineAmt[ii] =0;
      aaLineCash[ii] =0;
      aaUseCrAmt[ii] =0;
      aaAmtBal[ii] =0;
      aaCashBal[ii] =0;
      aaMCode[ii] ="K";  //-K(-2)-
      aaConsAmt[ii] =0;
   }
   trunMcode = "";
   haveCard = "";
//   trunConsume = "";
//   trunConsume1 = "";
//   trunConsume2 = "";
//   trunConsume3 = "";
//   trunConsume4 = "";
//   trunConsume5 = "";
//   trunConsume6 = "";
//   trunConsume7 = "";
//   trunConsume8 = "";
//   trunConsume9 = "";
//   trunConsume10 = "";
//   trunConsume11 = "";
}
}
