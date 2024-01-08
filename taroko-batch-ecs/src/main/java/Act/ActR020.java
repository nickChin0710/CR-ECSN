/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/06/01  V1.00.01  Allen Ho   Initial                                    *
* 112/08/09  V1.00.02  Simon      1.程式名稱修正                             *
*                                 2.每月最後一個工作日產生並排入cr_d_act003 after cr_d_gen001*
*                                 3.新增寫入act_interest_draw.process_date、interest_end_date*
*                                 4.ptr_batch_rpt.start_date 改以 business_date 取代 sysDate*
*                                 5.rptId_r1 改以 CRM188 取代 ActR020R1      *
*                                 6.if (getValue("debt.post_date",inti).compareTo(getValue("wday.next_close_date"))>0) return;*
*                                   修改以 wday.this_close_date 判斷 in procInterest()*
*                                 7.非getValueInt("int_rate_mcode")==0 之 intr.interest_rate*
*                                   需包含計算act_acno.revolve_int_rate      *
*                                 8.loadActDebt()不要放迴圈(因 nL = (String) workHash.get(parmKeyField + "-L")會錯亂*
*                                 9.loadPtrWorkday() 需新增讀取 .this_close_date*
* 112/11/06  V1.00.03  Simon      act_acct_curr.bill_sort_seq 有空值(轉檔進來為空值)，
*                                 改以 ptr_currcode.bill_sort_seq 取代       *
******************************************************************************/
package Act;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class ActR020 extends AccessDAO
{
 private  String PROGNAME = "產生循環利息收入提存表 CRM188 112/11/06 V1.00.03";
 CommFunction comm = new CommFunction();
 CommRoutine  comr = null;
 CommCrdRoutine comcr = null;

 String businessDate    = "";
 String chiDate         = "";
 String hPseqno         = "";
 long    totalCnt=0,currCnt=0;
 double[] tempRevolvingInterest = new double[10];
 double   fitRevolvingRate1 = 0;
 int cnt1=0,workCnt=0;
 String rptId_r1  = "";
 String rptName1  = "";
 int    rptSeq1   = 0;
 String buf = "",tmp="";
 int pageCnt=0,lineCnt=0;

 String localDir   = "";
 String fileName1  = "";
 int fo1;
 String   newLine="\n";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ActR020 proc = new ActR020();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 ) 
      { businessDate = args[0]; }

   if ( !connectDataBase() ) return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comcr = new CommCrdRoutine(getDBconnect(),getDBalias());
   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","檢核執行日期");
   if (businessDate.equals(dateOfLastWorkday(businessDate,1)))
      { showLogMessage("I","","本日["+ businessDate + "]為資料試算日期");  }
   else
      {
       showLogMessage("I","","本日["+ businessDate + "] 非程式執行日期");
       return(0);
      }
   deleteActInterestDraw();
   selectPtrWorkday();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔...");
   loadPtrWorkday();
   loadPtrCurrcode();
   loadPtrActgeneralN();
   loadActAcctCurr();
   loadActDebt();
   loadActDebt1();
   loadBilMerchant();   //  must put to last
   for (int intm=0;intm<workCnt;intm++)
     {
      setValue("wday.stmt_cycle" , getValue("stmt_cycle",intm));
      cnt1 = getLoadData("wday.stmt_cycle");

      showLogMessage("I","","stmt_cycle     ["+getValue("wday.stmt_cycle")+"]");
      showLogMessage("I","","=========================================");
    //loadActDebt();
      totalCnt=0;
      selectActAcno();
     }
   deletePtrBatchRpt();
   selectActInterestDrawR1();

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("business_date");
  showLogMessage("I","","本日營業日 : ["+ businessDate+"]");
//chiDate = String.format("%03d",Integer.valueOf(sysDate.substring(0,4))-1911)+sysDate.substring(4,8);
  chiDate = String.format("%03d",Integer.valueOf(businessDate.substring(0,4))-1911)+
            businessDate.substring(4,8);
  return;
 }
// ************************************************************************ 
 void selectPtrWorkday() throws Exception
 {
  selectSQL = "stmt_cycle";
  daoTable  = "ptr_workday";
  whereStr  = "order by stmt_cycle";

  workCnt = selectTable();
  return;
 }
// ************************************************************************
 void loadPtrWorkday() throws Exception
  {
   extendField = "wday.";
   selectSQL = "stmt_cycle,"
             + "this_acct_month," 
             + "next_acct_month," 
             + "this_close_date," 
             + "next_close_date," 
             + "last_close_date," 
             + "this_lastpay_date," 
             + "this_interest_date";
   daoTable  = "ptr_workday";
   whereStr  = "order by stmt_cycle";

   int  n = loadTable();

   setLoadData("wday.stmt_cycle");

   showLogMessage("I","","Load ptr_workday Count: ["+n+"]");
  }
// ************************************************************************
 void  selectActAcno() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "acno_flag,"
            + "corp_p_seqno,"
            + "id_p_seqno,"
            + "revolve_int_sign,"
            + "decode(revolve_int_sign,'+',revolve_int_rate,revolve_int_rate*-1)"
            + " as revolve_int_rate,"
            + "revolve_rate_s_month,"
            + "decode(revolve_rate_e_month,'','300012',revolve_rate_e_month) "
            + " as revolve_rate_e_month,"
            + "new_cycle_month,"
            + "int_rate_mcode,"
            + "last_interest_date";
  daoTable  = "act_acno";
  whereStr  = "where stmt_cycle = ? "
            + "and   acno_flag in ('1','3') "
            + "and   acct_status < '3' "
            + "order by p_seqno "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    hPseqno = getValue("p_seqno");
    setValue("acmm.interest_amt"      , "0"); 
    setValue("temp_interest_amt"      , "0");  /* 記錄帳戶該cycle所有利息加總 */

    setValue("agen.acct_type" , getValue("acct_type"));
    cnt1 = getLoadData("agen.acct_type");

    tempRevolvingInterest[1]  = getValueDouble("agen.revolving_interest1");
    tempRevolvingInterest[2]  = getValueDouble("agen.revolving_interest2");
    tempRevolvingInterest[3]  = getValueDouble("agen.revolving_interest3");
    tempRevolvingInterest[4]  = getValueDouble("agen.revolving_interest4");
    tempRevolvingInterest[5]  = getValueDouble("agen.revolving_interest5");
    tempRevolvingInterest[6]  = getValueDouble("agen.revolving_interest6");
    // **************************************************************************
    selectActAcctCurr();

    selectActDebt();

    double newInterestAmt=0,unbillInterestAmt=0;

    for (int intj=0;intj<currCnt;intj++)
     {
      int intSeq=getValueInt("acul.bill_sort_seq",intj);

      newInterestAmt = newInterestAmt 
                     + getValueDouble("temp.interest_amt",intSeq);

      unbillInterestAmt = unbillInterestAmt 
                        + getValueDouble("acur.temp_unbill_interest",intSeq);

     }
    setValueDouble("draw.new_interest_amt"    , newInterestAmt );
    setValueDouble("draw.unbill_interest_amt" , unbillInterestAmt);

    setValue("draw.debt_interest_amt" , "0");
    setValue("debb.p_seqno"   , getValue("p_seqno"));
    cnt1 = getLoadData("debb.p_seqno");
    if (cnt1!=0)
       setValue("draw.debt_interest_amt" , getValue("debb.debt_interest_amt"));

    newInterestAmt = getValueDouble("draw.new_interest_amt")
                   + getValueDouble("draw.unbill_interest_amt")
                   + getValueDouble("draw.debt_interest_amt");

  //setValueDouble("draw.sum_interest_amt"    , commCurrAmt("901", newInterestAmt ,-1));
    setValueDouble("draw.sum_interest_amt"    , commCurrAmt("901", newInterestAmt ,0));

    if (getValueDouble("draw.sum_interest_amt")>0) insertActInterestDraw();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void loadBilMerchant() throws Exception
 {
  extendField = "merc.";
  selectSQL = "mcht_no,"
            + "trans_flag";
  daoTable  = "bil_merchant";
  whereStr  = "where trans_flag = 'Y'";

  int  n = loadTable();

  setLoadData("merc.mcht_no");

  showLogMessage("I","","Load bil_merchant Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcctCurr() throws Exception
 {
  extendField = "acul.";
  selectSQL = "a.p_seqno,"
            + "a.curr_code,"
            + "a.temp_unbill_interest,"
            + "p.bill_sort_seq";
  daoTable  = "act_acct_curr a, ptr_currcode p ";
  whereStr  = "where a.curr_code = p.curr_code "
            + "order by a.p_seqno, a.curr_code ";
    
  int n  = loadTable();

  setLoadData("acul.p_seqno");

  showLogMessage("I","","Load act_acct_curr Count: ["+n+"]");
 }
// ************************************************************************
void selectActAcctCurr() throws Exception 
 {
  setValue("acul.p_seqno"   , getValue("p_seqno"));
  currCnt  = getLoadData("acul.p_seqno");

  for (int intj=0;intj<currCnt;intj++)
    {
     int intSeq = getValueInt("acul.bill_sort_seq",intj);

     setValue("acur.curr_code"              , getValue("acul.curr_code",intj)                , intSeq);
     setValue("acur.temp_unbill_interest"   , getValue("acul.temp_unbill_interest",intj)     , intSeq);

     setValue("cacr.interest_amt"           , "0.0" , intSeq);
     setValue("temp.interest_amt"           , "0.0" , intSeq);   /* 記錄帳戶該cycle所有利息加總 */
    }
}
// ************************************************************************
 void loadPtrCurrcode() throws Exception
 {
  extendField = "pcde.";
  selectSQL = "curr_code,"
            + "curr_amt_dp,"
            + "bill_sort_seq";
  daoTable  = "ptr_currcode";
  whereStr  = "where bill_sort_seq !='' ";

  int  n = loadTable();

  setLoadData("pcde.curr_code");

  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************
 void loadPtrActgeneralN() throws Exception
 {
  extendField = "agen.";
  selectSQL = "acct_type,"
            + "revolving_interest1,"
            + "revolving_interest2,"
            + "revolving_interest3,"
            + "revolving_interest4,"
            + "revolving_interest5,"
            + "revolving_interest6,"
            + "rc_max_rate";
  daoTable  = "ptr_actgeneral_n";
  whereStr  = "";

  int  n = loadTable();

  setLoadData("agen.acct_type");

  showLogMessage("I","","Load ptr_acct_type Count: ["+n+"]");
 }
// ************************************************************************
  void loadActDebt() throws Exception
  {
   extendField = "debt.";
   selectSQL = "a.p_seqno,"
             + "a.curr_code,"
             + "a.mcht_no,"
             + "a.acct_code,"
             + "a.interest_rs_date,"
             + "a.interest_date,"
             + "a.new_it_flag,"
             + "a.post_date,"
             + "a.ao_flag,"
             + "a.end_bal,"
             + "a.dc_end_bal,"
             + "a.int_rate_flag,"
             + "a.int_rate,"
             + "b.inter_rate_code,"
             + "b.inter_rate_code2";
   daoTable  = "act_debt a,ptr_actcode b,ptr_workday w";
   whereStr  = "where a.end_bal+a.dc_end_bal > 0 "
             + "and   a.acct_code in ('BL','CA','ID','AO','OT','IT') "
             + "and   a.acct_code = b.acct_code "
             + "and   b.interest_method = 'Y'  "
             + "and   a.post_date != '00000000' "
           //+ "and   a.acct_month < ? "
           //+ "and   a.stmt_cycle = ? "
             + "and   a.stmt_cycle = w.stmt_cycle "
             + "and   a.acct_month < w.next_acct_month "
             + "order by a.p_seqno "
             ;
 //setString(1 , getValue("wday.next_acct_month"));
 //setString(2 , getValue("wday.stmt_cycle"));

   int  n = loadTable();

   setLoadData("debt.p_seqno");
    
   showLogMessage("I","","Load act_debt Count: [" + n + "]");
   return;
  }
// ************************************************************************
 void loadActDebt1() throws Exception
  {
   extendField = "debb.";
   selectSQL = "p_seqno,"
             + "sum(end_bal) as debt_interest_amt";
   daoTable  = "act_debt";
   whereStr  = "where end_bal > 0 "
             + "and   acct_code = 'RI' "
             + "group by p_seqno "
             ;

   int  n = loadTable();

   setLoadData("debb.p_seqno");
    
   showLogMessage("I","","Load act_debt1 Count: [" + n + "]");
   return;
  }
// ************************************************************************
 void selectActDebt() throws Exception 
  {
/*** for debug set ***/
  String tmpPseqno="", tmpCurrCode="";
  tmpPseqno=getValue("p_seqno");
/*** for debug end ***/
  setValue("debt.p_seqno"   , getValue("p_seqno"));
  int cntM = getLoadData("debt.p_seqno");

  for (int inti=0;inti<cntM;inti++)
     {
/*** for debug set ***/
      tmpCurrCode=getValue("debt.curr_code",inti);
/*** for debug end ***/
      setValue("pcde.curr_code" , getValue("debt.curr_code",inti));
      cnt1 = getLoadData("pcde.curr_code");

      int intSeq=getValueInt("pcde.bill_sort_seq");

      setValue("merc.trans_flag" , "");
      if (getValue("debt.acct_code",inti).equals("IT"))
         {
          setValue("merc.mcht_no" , getValue("debt.mcht_no",inti));
          int cnt2 = getLoadData("merc.mcht_no");

          if (cnt2==0) setValue("merc.trans_flag" , "N");
         }
      if (!getValue("merc.trans_flag").equals("Y")) 
         {
          procInterest(inti,intSeq);
         }
     }
   return;
  }
// ************************************************************************
 void procInterest(int inti,int intSeq) throws Exception 
  {
   double minIntRate=0;

   setValue("intr.intr_s_date"     , "");
   setValue("intr.interest_rate"   , "0");
   setValue("temp_interest_amt"    ,  "0");

   fitRevolvingRate1  = tempRevolvingInterest[getValueInt("debt.inter_rate_code",inti)];

   if (getValue("debt.ao_flag",inti).equals("Y"))     /* 代償使用第二段利率, 非 act_commute */
      fitRevolvingRate1 = tempRevolvingInterest[getValueInt("debt.inter_rate_code2",inti)];
       
   setValueDouble("intr.interest_rate" , fitRevolvingRate1);
   if ((getValueInt("int_rate_mcode")==0)&&
       (getValue("debt.int_rate_flag",inti).equals("Y")))
      {
       setValue("intr.interest_rate" , getValue("debt.int_rate",inti));
      }
   else
      {
       minIntRate = 0;
       if ((getValue("wday.next_acct_month").compareTo(getValue("revolve_rate_s_month"))>=0)&&
           (getValue("wday.next_acct_month").compareTo(getValue("revolve_rate_e_month"))<= 0))
           minIntRate = getValueDouble("revolve_int_rate");

       setValueDouble("intr.interest_rate" , getValueDouble("intr.interest_rate")
                                           + minIntRate);
      }

   setValue("intr.interest_amt" , "0");

   /***** 設定利息起算日 START ************************************************************/
   if (getValue("debt.post_date",inti).compareTo(getValue("wday.last_close_date"))>0)
      { 
       if (getValue("debt.new_it_flag",inti).equals("Y"))
          {
           setValue("intr.intr_s_date" , getValue("wday.this_lastpay_date"));
          }
       else
          {
           setValue("intr.intr_s_date" , getValue("debt.interest_date",inti));
          }  
      }
   else
      {
       if ((getValue("new_cycle_month").length()>0)&&
           (getValue("wday.next_acct_month").compareTo(getValue("new_cycle_month"))==0))
          {
           /* 變更cycle以前cycle利息起算日 */
           setValue("intr.intr_s_date" , getValue("last_interest_date"));
          }
       else
          {
           setValue("intr.intr_s_date" , getValue("wday.this_interest_date"));
          }
      }

   if (getValue("debt.interest_rs_date",inti).length() == 8)
      setValue("intr.intr_s_date" , getValue("debt.interest_rs_date",inti));

   if (getValue("intr.intr_s_date").equals("00000000"))
      setValue("intr.intr_s_date" , getValue("debt.post_date",inti));

 //if (getValue("debt.post_date",inti).compareTo(getValue("wday.next_close_date"))>0) return;
   if (getValue("debt.post_date",inti).compareTo(getValue("wday.this_close_date"))>0) return;

   if ((getValue("new_cycle_month").length()>0)&&   /* 變更 cycle */
       (getValue("wday.next_acct_month").compareTo(getValue("new_cycle_month"))==0))
      if (getValue("debt.post_date",inti).compareTo(getValue("last_interest_date"))>0) return;

   setValue("intr.intr_e_date" , comm.lastdateOfmonth(businessDate));


   int calDays = comm.datePeriod(getValue("intr.intr_s_date"),comm.lastdateOfmonth(businessDate));
    
   if (getValueDouble("intr.interest_rate") > fitRevolvingRate1)
      setValueDouble("intr.interest_rate" , fitRevolvingRate1);


   setValueDouble("intr.interest_amt"    , calDays 
                                         * getValueDouble("debt.end_bal",inti)    
                                         * getValueDouble("intr.interest_rate")
                                         / 10000.0);

   setValueDouble("intr.interest_amt"    , (int)Math.round(getValueDouble("intr.interest_amt")*100.0 + 0.00001)/100.0);

   setValueDouble("temp_interest_amt"     , getValueDouble("temp_interest_amt")  
                                          + getValueDouble("intr.interest_amt"));
   setValueDouble("temp.interest_amt"     , getValueDouble("temp.interest_amt",intSeq) 
                                          + getValueDouble("intr.interest_amt")    , intSeq);
   return;
  }
// ************************************************************************
 String dateOfLastWorkday(String tmpBusiDate,int calDay) throws Exception
 {
  extendField = "holi.";
  selectSQL = "substr(holiday,7,2) as hday";
  daoTable  = "ptr_holiday";
  whereStr  = "WHERE holiday like ? "
            + "order by holiday desc "
            ;

  setString(1 , tmpBusiDate.substring(0,6)+"%");

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) 
     {
      showLogMessage("I","","Table ptr_holiday 資料不完整, 請確認["+ recCnt +"]");
      exitProgram(1);
     }

  String maxMonthDay=comm.lastdateOfmonth(tmpBusiDate).substring(6,8);
  int okFlag=0,daysOfWork=0;
  
  for (int inti= Integer.valueOf(maxMonthDay);inti>=1;inti--)
     {
      okFlag=0;
      for (int intk= 0;intk<recCnt;intk++)
        {
         if (String.format("%02d",inti).compareTo(getValue("holi.hday",intk))>0) break;
         if (String.format("%02d",inti).equals(getValue("holi.hday",intk))) 
            {
             okFlag=1;
             break;
            }
        }
      if (okFlag==0) daysOfWork++;
      if (daysOfWork==calDay) 
         {
        //okFlag=inti-1;
          okFlag=inti;
          break;
         }
     }
  return(tmpBusiDate.substring(0,6)+String.format("%02d",okFlag));
 }
// ************************************************************************
 double  commCurrAmt(String currCode,double val,int rnd) throws Exception
 {
  setValue("pcde.curr_code" , currCode);
  cnt1 = getLoadData("pcde.curr_code");

  val = val * 10000.0;
  val = Math.round(val);

  BigDecimal curr_amt = new BigDecimal(val).divide(new BigDecimal("10000"));

  if (cnt1==0) return(curr_amt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = curr_amt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = curr_amt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = curr_amt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

  return(retNum);
 }
// ************************************************************************
 void insertActInterestDraw() throws Exception
 {
  extendField = "draw.";
  setValue("draw.p_seqno"                 , getValue("p_seqno"));
  setValue("draw.acct_type"               , getValue("acct_type")); 
  setValue("draw.acno_flag"               , getValue("acno_flag")); 
  setValue("draw.stmt_cycle"              , getValue("stmt_cycle"));
  setValue("draw.process_date"            , businessDate);
  setValue("draw.interest_end_date"       , comm.lastdateOfmonth(businessDate));
  setValue("draw.mod_time"                , sysDate+sysTime);
  setValue("draw.mod_pgm"                 , javaProgram);

  daoTable = "act_interest_draw";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_col_ifrs_base error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 int deleteActInterestDraw() throws Exception
 {
  daoTable  = "act_interest_draw";
  whereStr  = "";

  int recCnt = deleteTable();

  showLogMessage("I","","delete act_interest_draw cnt :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 void  loadPtrActcode() throws Exception
 {
  extendField = "acde.";
  selectSQL = "acct_code,"
            + "inter_rate_code,"
            + "inter_rate_code2,"
            + "interest_method";
  daoTable  = "ptr_actcode";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("acde.acct_code");
  showLogMessage("I","","Load ptr_actcode Count: ["+n+"]");
 }
// ************************************************************************
 int selectActInterestDrawR1() throws Exception
 {
  localDir  = System.getenv("PROJ_HOME")+"/media/act";
//fileName1 = localDir+"/CRM188_" + sysDate + ".TXT";
  fileName1 = localDir+"/CRM188_" + businessDate + ".TXT";

  fo1 = openOutputText(fileName1);
  if (fo1 == -1)
     {
      showLogMessage("I","","檔案"+fileName1+"無法開啟寫入 error!" );
      return(1);
     }

  selectSQL = "sum(sum_interest_amt) as sum_interest_amt";
  daoTable  = "act_interest_draw";    
  whereStr  = "";

  int recordCnt = selectTable();

  headFileR1();

  buf  = fixLeft("應收信用卡款項一循環信用總" , 38  )
       + fixLeft("130270041" , 16  )
       + fixRight(String.format("%,.0f", getValueDouble("sum_interest_amt")) , 14 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf  = fixLeft("信用卡循環利息收入一循環信用息" , 38  )
       + fixLeft("410610011" , 16  )
       + fixLeft(""          , 17  )
       + fixRight(String.format("%,.0f", getValueDouble("sum_interest_amt")) , 14 );
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  closeOutputText(fo1);
  return(0);
 }
// ************************************************************************
 void headFileR1() throws Exception 
 {
  String temp = "";
//rptId_r1  = "ActR020R1";
  rptId_r1  = "CRM188";
  rptName1  = "循環利息收入提存表";

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  buf = comcr.insertStr(buf, "分行代號: "    + "3144 信用卡部"          ,  1);
  buf = comcr.insertStr(buf, " "          + rptName1                 , 51);
  buf = comcr.insertStr(buf, "保存年限: 十年"                           ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  tmp = String.format("%3.3s年%2.2s月%2.2s日", chiDate.substring(0, 3),
                 chiDate.substring(3, 5), chiDate.substring(5));
  buf = comcr.insertStr(buf, "報表代號: CRM188    科目代號:"            ,  1);
  buf = comcr.insertStr(buf, "中華民國 " + tmp                          , 50);
  temp = String.format("%4d", pageCnt);
  buf = comcr.insertStr(buf, "頁    次: 1"                               ,100);
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "           會計項子科目名稱          子科目代號             借方金額         貸方金額                     備註    ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  buf = "================================================================================================================== ";
  insertPtrBatchRpt(rptId_r1, rptName1, ++rptSeq1, "0", buf);
  writeTextFile(fo1,buf+newLine);

  lineCnt = 6;
 }
/***********************************************************************/
 String fixRight(String str, int len) throws UnsupportedEncodingException 
   {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
        spc += " ";
    if (str == null)
        str = "";
    str = spc + str;
    byte[] bytes = str.getBytes("MS950");
    int offset = bytes.length - len;
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, offset, vResult, 0, len);
    return new String(vResult, "MS950");
   }
/************************************************************************/
 String fixLeft(String str, int len) throws UnsupportedEncodingException 
  {
   int size = (Math.floorDiv(len, 100) + 1) * 100;
   String spc = "";
   for (int i = 0; i < size; i++)
       spc += " ";
   if (str == null)
       str = "";
   str = str + spc;
   byte[] bytes = str.getBytes("MS950");
   byte[] vResult = new byte[len];
   System.arraycopy(bytes, 0, vResult, 0, len);

   return new String(vResult, "MS950");
 }
/************************************************************************/
 int deletePtrBatchRpt() throws Exception
 {
  daoTable  = "ptr_batch_rpt";
  whereStr  = "where program_code like 'ActR020%' "
            + "and   start_date = ? ";

//setString(1 , sysDate);
  setString(1 , businessDate);

  int recCnt = deleteTable();

  showLogMessage("I","","delete ptr_batch_rpt cnt :["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 void insertPtrBatchRpt(String rptId_r1,String rptName1,int seq,String kind,String  buf) throws Exception 
 {
  noTrim= "Y";
  extendField = "rpt1.";
  setValue("rpt1.program_code"       , rptId_r1);
  setValue("rpt1.rptname"            , rptName1);
//setValue("rpt1.start_date"         , sysDate);
  setValue("rpt1.start_date"         , businessDate);
  setValue("rpt1.start_time"         , sysTime); 
  setValueInt("rpt1.seq"             , seq);
  setValue("rpt1.kind"               , kind);
  setValue("rpt1.report_content"    , buf);

  daoTable = "ptr_batch_rpt";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_ptr_batch_rpt error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************


}  // End of class FetchSample
