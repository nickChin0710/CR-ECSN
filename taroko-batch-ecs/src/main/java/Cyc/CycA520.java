/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/05/12  V1.00.06  Allen Ho   cyc_A590                                   *
* 111/10/18  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class CycA520 extends AccessDAO
{
 private final String PROGNAME = "關帳-對帳單前期利息及費用扣除處理程式 111/10/18  V1.00.02";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate = "";
 double[] feeAmt = new double[13];
 double[] intAmt = new double[13];
 String pSeqno ="", currCode ="";

 final boolean DEBUG = false;
 final boolean DEBUG_P = false;
 String pSeqnoStr = "0000342244";
  
 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA520 proc = new CycA520();
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
   showLogMessage("I","",javaProgram+" "+ PROGNAME);

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
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) return(1);

   selectPtrBusinday();

   showLogMessage("I","","===============================");
   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       showLogMessage("I","","===============================");
       return(0);
      }

   if (hBusiBusinessDate.substring(4,6).equals("01"))
      {
       showLogMessage("I","","本程式一月份不執行:"+ getValue("wday.this_acct_month"));
       showLogMessage("I","","===============================");
       return(0);
      }

   selectActDebt();

   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");
   showLogMessage("I","","===============================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ hBusiBusinessDate +"]");
 }
// ************************************************************************ 
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1, hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void selectActDebt() throws Exception
 {
  sqlCmd   = "SELECT a.p_seqno, "
           + "       max('1'), "
           + "       a.curr_code, "
           + "       substr(a.acct_month,5,2) as acct_months, "
           + "       sum(decode(a.acct_code,'AF',a.dc_d_avail_bal,'CC',a.dc_d_avail_bal, "
           + "                              'CF',a.dc_d_avail_bal,'PN',a.dc_d_avail_bal, "
           + "                              'LF',a.dc_d_avail_bal,'PF',a.dc_d_avail_bal, "
           + "                              'SF',a.dc_d_avail_bal,0)) as fee_amt, "
           + "       sum(decode(a.acct_code,'AI',a.dc_d_avail_bal,'CI',a.dc_d_avail_bal, "
           + "                              'RI',a.dc_d_avail_bal,0)) as interest_amt "
           + "FROM   act_debt a,cyc_acmm_" + getValue("wday.stmt_cycle") +" b "
           + "where  a.p_seqno      = b.p_seqno "
           + "and    substr(a.acct_month,1,4) = ? "
           + "and    a.acct_month             < ? "
           + "and    a.acct_code in ('AF','CF','LF','SF','CC','PN','PF','AI','RI','CI') "
           + "GROUP BY a.p_seqno,substr(a.acct_month,5,2),curr_code "
           + "UNION "
           + "SELECT a.p_seqno, "
           + "       max('2'), "
           + "       a.curr_code, "
           + "       substr(a.acct_month,5,2) as acct_months, "
           + "       sum(decode(a.acct_code,'AF',a.dc_d_avail_bal,'CC',a.dc_d_avail_bal, "
           + "                              'CF',a.dc_d_avail_bal,'PN',a.dc_d_avail_bal, "
           + "                              'LF',a.dc_d_avail_bal,'PF',a.dc_d_avail_bal, "
           + "                              'SF',a.dc_d_avail_bal,0)) as fee_amt, "
           + "       sum(decode(a.acct_code,'AI',a.dc_d_avail_bal,'CI',a.dc_d_avail_bal, "
           + "                              'RI',a.dc_d_avail_bal,0)) as interest_amt "
           + "FROM   act_debt_hst a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b "
           + "where  a.p_seqno      = b.p_seqno "
           + "and    substr(a.acct_month,1,4) = ? "
           + "and    a.acct_month             < ? "
           + "and    a.acct_code in ('AF','CF','LF','SF','CC','PN','PF','AI','RI','CI') "
           + "GROUP BY a.p_seqno,substr(a.acct_month,5,2),curr_code "
           + "ORDER BY 1,3,4 "
           + "";

  daoTable  = "cyc_acmm_"+ getValue("wday.stmt_cycle");

  setString(1, getValue("wday.this_acct_month").substring(0,4));
  setString(2, getValue("wday.this_acct_month"));
  setString(3, getValue("wday.this_acct_month").substring(0,4));
  setString(4, getValue("wday.this_acct_month"));

  openCursor();

  totalCnt=0;
  pSeqno = currCode ="";
  int intCnt=0,intmm=0;        
  for (int int1a=1;int1a<13;int1a++)
    {
     feeAmt[int1a]=0;
     intAmt[int1a]=0;
    }

  while( fetchTable() )
   {
    if ((!pSeqno.equals(getValue("p_seqno")))||
        (!currCode.equals(getValue("curr_code"))))
       {   
        if (pSeqno.length()!=0)
           {
            totalCnt++;
            updateCycBillExt();
            intCnt=0;

            for (int int1a=1;int1a<13;int1a++)
              {
               feeAmt[int1a]=0;
               intAmt[int1a]=0;
              }
           }
       }
    intmm = getValueInt("acct_months");

    feeAmt[intmm] = feeAmt[intmm] + getValueDouble("fee_amt");
    intAmt[intmm] = intAmt[intmm] + getValueDouble("interest_amt");
                                    
    intCnt++;

    pSeqno = getValue("p_seqno");
    currCode = getValue("curr_code");
   } 
  if (intCnt>0) updateCycBillExt();

  closeCursor();
  return;    
 }           
// ************************************************************************
 void updateCycBillExt() throws Exception
 {
  dateTime();
  for (int inti=1;inti<13;inti++)
    if (Integer.valueOf(getValue("wday.this_acct_month").substring(4,6))<inti)
       {
        feeAmt[inti] = 0;
        intAmt[inti] = 0;
       }
  updateSQL = "fee_amt_01      = ?, "
            + "fee_amt_02      = ?, "
            + "fee_amt_03      = ?, "
            + "fee_amt_04      = ?, "
            + "fee_amt_05      = ?, "
            + "fee_amt_06      = ?, "
            + "fee_amt_07      = ?, "
            + "fee_amt_08      = ?, "
            + "fee_amt_09      = ?, "
            + "fee_amt_10      = ?, "
            + "fee_amt_11      = ?, "
            + "interest_amt_01 = ?, "
            + "interest_amt_02 = ?, "
            + "interest_amt_03 = ?, "
            + "interest_amt_04 = ?, "
            + "interest_amt_05 = ?, "
            + "interest_amt_06 = ?, "
            + "interest_amt_07 = ?, "
            + "interest_amt_08 = ?, "
            + "interest_amt_09 = ?, "
            + "interest_amt_10 = ?, "
            + "interest_amt_11 = ?, "
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";
  daoTable  = "cyc_bill_ext";
  whereStr  = "WHERE  p_seqno   = ? "
            + "AND    acct_year = ? "
            + "AND    curr_code = ? ";

  setDouble(1 , feeAmt[1]);
  setDouble(2 , feeAmt[2]);
  setDouble(3 , feeAmt[3]);
  setDouble(4 , feeAmt[4]);
  setDouble(5 , feeAmt[5]);
  setDouble(6 , feeAmt[6]);
  setDouble(7 , feeAmt[7]);
  setDouble(8 , feeAmt[8]);
  setDouble(9 , feeAmt[9]);
  setDouble(10, feeAmt[10]);
  setDouble(11, feeAmt[11]);
  setDouble(12, intAmt[1]);
  setDouble(13, intAmt[2]);
  setDouble(14, intAmt[3]);
  setDouble(15, intAmt[4]);
  setDouble(16, intAmt[5]);
  setDouble(17, intAmt[6]);
  setDouble(18, intAmt[7]);
  setDouble(19, intAmt[8]);
  setDouble(20, intAmt[9]);
  setDouble(21, intAmt[10]);
  setDouble(22, intAmt[11]);
  setString(23 , javaProgram);
  setString(24 , pSeqno);
  setString(25 , getValue("wday.this_acct_month").substring(0,4));
  setString(26 , currCode);

  updateTable();
  return;
 }
// ************************************************************************


}  // End of class FetchSample

