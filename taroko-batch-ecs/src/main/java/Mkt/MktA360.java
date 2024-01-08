/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/06/12  V1.00.00  Allen Ho   mkt_a360                                   *
* 109-12-03  V1.00.01  tanwei     updated for project coding standard        *
* 112-10-20  V1.00.02  Holmes     only executed on the 2nd of every month    * 
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktA360 extends AccessDAO
{
 private  String progname = "紅利-紅利積點區間統計處理程式 109/12/03 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommDBonus comb = null;

 String hBusiBusinessDate = "";
 int    seqNo        = 0;
 double bpAreaS     = 0;
 double bpAreaE     = 0;
 double acctCntA    = 0;
 double acctRateA   = 0;
 double bonusCntA   = 0;
 double bonusRateA  = 0;
 double acctCntB    = 0;
 double acctRateB   = 0;
 double bonusCntB   = 0;
 double bonusRateB  = 0;
 double transOutBp  = 0;

 double sumCntA     = 0;
 double sumPntA     = 0;
 double sumCntB     = 0;
 double sumPntB     = 0;
  
 double[] arrBpAreaS   = new double[50];
 double[] arrBpAreaE   = new double[50];

 double[][] arrAcctCntA  = new double[10][50];
 double[][] arrBonusCntA = new double[10][50];
 double[][] arrAcctCntB  = new double[10][50];
 double[][] arrBonusCntB = new double[10][50];

 long    totalCnt=0;
 int cnt1 = 0,parmCnt=0;;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA360 proc = new MktA360();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+progname);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
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

   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();
   if (!hBusiBusinessDate.substring(6,8).equals("02"))
   {
    showLogMessage("I","","本程式只在每月2日換日後執行,本日為"+ hBusiBusinessDate +"日..");
    showLogMessage("I","","=========================================");
    return(0);
   }    

   showLogMessage("I","","=========================================");
   showLogMessage("I","","清除重做資料");
   deleteMktBonusStat3();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 統計分配比率 資料");
   procRateRange();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadCrdCard();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 統計分配比率 資料");
   selectPtrAcctType();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 紅利明細 資料");
   selectMktBonusDtl();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 統計 資料");
   procMktBonusStat3();
   showLogMessage("I","","=========================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
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
      hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************
 public void  selectPtrAcctType() throws Exception
 {
  extendField = "type.";
  daoTable  = "ptr_acct_type";

  parmCnt = selectTable();

  for (int inti=0;inti<parmCnt;inti++)
    for (int intk=0;intk<43;intk++)
      {
       arrAcctCntA[inti][intk]   = 0;
       arrBonusCntA[inti][intk]  = 0;
       arrAcctCntB[inti][intk]   = 0;
       arrBonusCntB[inti][intk]  = 0;
      }
  return;
 }
// ************************************************************************  
 public void  selectMktBonusDtl() throws Exception
 {
  selectSQL = "sum(beg_tran_bp) as beg_tran_bp, "
            + "max(acct_type) as acct_type, "
            + "p_seqno";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where beg_tran_bp != 0 "
            + "and   acct_date like ?||'%' "
            + "and   bonus_type = 'BONU' "
            + "group by p_seqno "
            + "having  sum(beg_tran_bp) != 0 "
            + "";

  setString(1,comm.lastMonth(hBusiBusinessDate).substring(0,6));              

  openCursor();

  int okFlag=0;
  int validInt=0;
  while( fetchTable() ) 
   { 
    for ( int inti=0; inti<parmCnt; inti++ )
      {
       if (!getValue("acct_type").equals(getValue("type.acct_type",inti))) continue;

       setValue("card.p_seqno" , getValue("p_seqno"));
       cnt1 = getLoadData("card.p_seqno");

       okFlag=0;
       for (int inta1=0;inta1<43;inta1++)
         {
          if ((getValueDouble("beg_tran_bp")<=arrBpAreaE[inta1])&&
              (getValueDouble("beg_tran_bp")>=arrBpAreaS[inta1]))
             {
              if (cnt1>0)
                 {
                  arrAcctCntA[inti][inta1]++;
                  arrBonusCntA[inti][inta1] = arrBonusCntA[inti][inta1] + getValueDouble("beg_tran_bp");
                 }
              else
                 {
                  arrAcctCntB[inti][inta1]++;
                  arrBonusCntB[inti][inta1] = arrBonusCntB[inti][inta1] + getValueDouble("beg_tran_bp");
                 }
              okFlag=1;
             }
          if (okFlag==1) break;
         }
      }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "crd_card";
  whereStr  = "where current_code = '0' ";

  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","Load crd_card : ["+n+"]");
 }
// ************************************************************************
 void procMktBonusStat3()  throws Exception
 {
  for ( int intk=0; intk<parmCnt; intk++ )
    {
     sumCntA = 0;
     sumPntA = 0;
     sumCntB = 0;
     sumPntB = 0;

     for (int inta1=0;inta1<43;inta1++)
       {
        sumCntA = sumCntA + arrAcctCntA[intk][inta1];
        sumPntA = sumPntA + arrBonusCntA[intk][inta1];
        sumCntB = sumCntB + arrAcctCntB[intk][inta1];
        sumPntB = sumPntB + arrBonusCntB[intk][inta1];
       }

     setValue("acct_type"            , getValue("type.acct_type",intk));
     showLogMessage("I","","帳戶類別 : ["+getValue("acct_type")+"]");
     for (int inta1=0;inta1<43;inta1++)
       {
        seqNo       = inta1;
        bpAreaS    = arrBpAreaS[inta1];
        bpAreaE    = arrBpAreaE[inta1];
        acctCntA   = arrAcctCntA[intk][inta1];
        bonusCntA  = arrBonusCntA[intk][inta1];
        acctCntB   = arrAcctCntB[intk][inta1];
        bonusCntB  = arrBonusCntB[intk][inta1];

        insertMktBonusStat3();
       }
    }
 }
// ************************************************************************
public int insertMktBonusStat3() throws Exception
 {
  acctRateA = 0;
  if (sumCntA!=0)
     acctRateA = Math.rint(acctCntA*10000/sumCntA)/100;

  bonusRateA = 0;
  if (sumPntA!=0)
     acctRateA = Math.round(bonusCntA*10000/sumPntA)/100;

  acctRateB = 0;
  if (sumCntB!=0)
     acctRateB = Math.round(acctCntB*10000/sumCntB)/100;

  acctRateB = 0;
  if (sumPntB!=0)
     acctRateB = Math.round(bonusCntB*10000/sumPntB)/100;

  setValue("stat_month"           , comm.lastMonth(hBusiBusinessDate));
  setValueInt("seq_no"            , seqNo);
  setValueDouble("bp_area_s"      , bpAreaS);
  setValueDouble("bp_area_e"      , bpAreaE);
  setValueDouble("acct_cnt_a"     , acctCntA);
  setValueDouble("acct_rate_a"    , acctRateA);
  setValueDouble("bonus_cnt_a"    , bonusCntA);
  setValueDouble("bonus_rate_a"   , bonusRateA);
  setValueDouble("acct_cnt_b"     , acctCntB);
  setValueDouble("acct_rate_b"    , acctRateB);
  setValueDouble("bonus_cnt_b"    , bonusCntB);
  setValueDouble("bonus_rate_b"   , bonusRateB);
  setValueDouble("trans_out_bp"   , transOutBp);
  setValue("crt_date"             , hBusiBusinessDate);
  setValue("crt_user"             , javaProgram);
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_bonus_stat3";

  insertTable();

  return(0);
 }
// ************************************************************************
public void procRateRange() throws Exception
 {
  double  tempBp=0;
    
  arrBpAreaS[0]=0;
  arrBpAreaE[0]=1000;

  for (int inta1=1;inta1<43;inta1++)
    {
     if (inta1 == 1 ) {tempBp = 1000;}
     else if (inta1 >= 2  && inta1 <= 17) {tempBp = 500;}
     else if (inta1 >= 18 && inta1 <= 27) {tempBp = 1000;} 
     else if (inta1 >= 28 && inta1 <= 39) {tempBp = 5000;}
     else if (inta1 >= 40) {tempBp = 10000;}

     arrBpAreaS[inta1]=arrBpAreaE[inta1-1]+1;
     arrBpAreaE[inta1]=arrBpAreaE[inta1-1]+tempBp;
    }
  return;
 }
// ************************************************************************
 public int deleteMktBonusStat3() throws Exception
 {
  daoTable  = "mkt_bonus_stat3";
  whereStr  = "WHERE stat_month = ? "
            + "and   acct_type in (select acct_type "
            + "                    from ptr_acct_type) ";

  setString(1 ,comm.lastMonth(hBusiBusinessDate)); 

  deleteTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
