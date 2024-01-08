/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/06/25  V1.00.05  Allen Ho   dbm_m180 reference mkt_a360                *
* 111/11/08  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM530 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-紅利積點區間統計處理程式 111/11/08  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommDBonus comb = null;

 String businessDate = "";
 int seqNo = 0;
 double bpAreaS = 0;
 double bpAreaE = 0;
 double acctCntA = 0;
 double acctRateA = 0;
 double bonusCntA = 0;
 double bonusRateA = 0;
 double acctCntB = 0;
 double acctRateB = 0;
 double bonusCntB = 0;
 double bonusRateB = 0;
 double transOutBp = 0;

 double sumCntA = 0;
 double sumPntA = 0;
 double sumCntB = 0;
 double sumPntB = 0;
  
 double[] arrBpAreaS = new double[50];
 double[] arrBpAreaE = new double[50];
 double[] arrAcctCntA = new double[50];
 double[] arrBonusCntA = new double[50];
 double[] arrAcctCntB = new double[50];
 double[] arrBonusCntB = new double[50];

 long    totalCnt=0;
 int cnt1 = 0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM530 proc = new DbmM530();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public  int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

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
      { businessDate = args[0]; }

   if ( !connectDataBase() )
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();
   showLogMessage("I","","=========================================");
   if (!businessDate.substring(6,8).equals("02"))
      {
       showLogMessage("I","","本程式只在每月2日換日後執行,本日為"+ businessDate +"日..");
       showLogMessage("I","","=========================================");
       return(0);
      } 

   deleteMktBonusStat3();
   procRateRange();

   loadDbcCard();
   selectDbmBonusDtl();
   procMktBonusStat3();

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

  if (businessDate.length()==0)
      businessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
// ************************************************************************  
 void selectDbmBonusDtl() throws Exception
 {
  selectSQL = "sum(beg_tran_bp) as beg_tran_bp, "
            + "id_p_seqno,"
            + "acct_type ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where   tran_date like ?||'%' "
            + "and     beg_tran_bp != 0 "
            + "group   by id_p_seqno,acct_type "
            + "having  sum(beg_tran_bp) > 0 "
            + "";

  setString(1,comm.lastMonth(businessDate).substring(0,6));              

  openCursor();

  for (int inti=0;inti<43;inti++)
    {
     arrAcctCntA[inti]   = 0;
     arrBonusCntA[inti]  = 0;
     arrAcctCntB[inti]   = 0;
     arrBonusCntB[inti]  = 0;
    }

  int okFlag=0;
  while( fetchTable() ) 
   { 
    okFlag=0;

    setValue("card.id_p_seqno" , getValue("id_p_seqno"));
    setValue("card.acct_type"  , getValue("acct_type"));
    cnt1 = getLoadData("card.id_p_seqno,card.acct_type");

    for (int inta1=0;inta1<43;inta1++)
      {
       if ((getValueDouble("beg_tran_bp")<= arrBpAreaE[inta1])&&
           (getValueDouble("beg_tran_bp")>= arrBpAreaS[inta1]))
          {
           if (cnt1>0)
              {
               arrAcctCntA[inta1]++;
               arrBonusCntA[inta1] = arrBonusCntA[inta1] + getValueDouble("beg_tran_bp");
              }
           else
              {
               arrAcctCntB[inta1]++;
               arrBonusCntB[inta1] = arrBonusCntB[inta1] + getValueDouble("beg_tran_bp");
              }
           okFlag=1;
          }
       if (okFlag==1) break;
      }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void procMktBonusStat3()  throws Exception
 {
  sumCntA = 0;
  sumPntA = 0;
  sumCntB = 0;
  sumPntB = 0;

  for (int inta1=0;inta1<43;inta1++)
    {
     sumCntA = sumCntA + arrAcctCntA[inta1];
     sumPntA = sumPntA + arrBonusCntA[inta1];
     sumCntB = sumCntB + arrAcctCntB[inta1];
     sumPntB = sumPntB + arrBonusCntB[inta1];
    }

  for (int inta1=0;inta1<43;inta1++)
    {
     seqNo = inta1;
     bpAreaS = arrBpAreaS[inta1];
     bpAreaE = arrBpAreaE[inta1];
     acctCntA = arrAcctCntA[inta1];
     bonusCntA = arrBonusCntA[inta1];
     acctCntB = arrAcctCntB[inta1];
     bonusCntB = arrBonusCntB[inta1];

     insertMktBonusStat3();
    }
 }
// ************************************************************************
 int insertMktBonusStat3() throws Exception
 {
  acctRateA = 0;
  if (sumCntA !=0)
     acctRateA = Math.rint(acctCntA *10000/ sumCntA)/100;

  bonusRateA = 0;
  if (sumPntA !=0)
     acctRateA = Math.round(bonusCntA *10000/ sumPntA)/100;

  acctRateB = 0;
  if (sumCntB !=0)
     acctRateB = Math.round(acctCntB *10000/ sumCntB)/100;

  acctRateB = 0;
  if (sumPntB !=0)
     acctRateB = Math.round(bonusCntB *10000/ sumPntB)/100;

  setValue("stat_month"           , comm.lastMonth(businessDate));
  setValue("acct_type"            , getValue("acct_type"));
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
  setValue("crt_date"             , businessDate);
  setValue("crt_user"             , javaProgram);
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_bonus_stat3";

  insertTable();

  return(0);
 }
// ************************************************************************
 void procRateRange() throws Exception
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

     arrBpAreaS[inta1]= arrBpAreaE[inta1-1]+1;
     arrBpAreaE[inta1]= arrBpAreaE[inta1-1]+tempBp;
    }
  return;
 }// ************************************************************************

 int deleteMktBonusStat3() throws Exception
 {
  daoTable  = "mkt_bonus_stat3";
  whereStr  = "WHERE stat_month = ? "
            + "and   acct_type in (select acct_type "
            + "                    from dbp_acct_type) ";

  setString(1 ,comm.lastMonth(businessDate)); 

  deleteTable();

  return(0);
 }
// ************************************************************************
 void loadDbcCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "acct_type,"
            + "id_p_seqno";
  daoTable  = "dbc_card";
  whereStr  = "where current_code = '0'";

  int  n = loadTable();
  setLoadData("card.id_p_seqno,card.acct_type");

  showLogMessage("I","","Load dbc_card : ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample
