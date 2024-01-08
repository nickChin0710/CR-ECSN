/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/08/09  V1.00.13  Allen Ho   cyc_C220                                   *
* 111/01/08  V1.01.01  Allen Ho   mantis 9252,9254                           *
* 111/11/10  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class CycC220 extends AccessDAO
{
 private final String PROGNAME = "雙幣卡-外幣基金停卡與效期到期移除處理程式 111/11/10  V1.00.02";
 CommFunction comm = new CommFunction();
 CommDCFund comDCF = null;
 CommRoutine comr = null;

 String pSeqno = "";
 String commitFlag = "N";
 String businessDate = "";
 String tranSeqno ="";

 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycC220 proc = new CycC220();
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

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [p_seqno]");
       showLogMessage("I","","PARM 3 : [commit_flag(Y/N)]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   if ( args.length >= 2 )
      { pSeqno = args[1]; }

   if ( args.length == 3 )
      { commitFlag = args[2]; }
   
   if ( !connectDataBase() ) return(1);

   comDCF = new CommDCFund(getDBconnect(),getDBalias());
   comr   = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectCycDcFundDtl0()>0)   // if no this check, will not right
     {
      commitDataBase();
      showLogMessage("I","","啟動 CycC250 執行後續動作 .....");
      showLogMessage("I","","===============================");

      String[] hideArgs = new String[1];
      try {
           hideArgs[0] = "";

           CycC250 cycC250 = new CycC250();
           int rtn = cycC250.mainProcess(hideArgs);
           if(rtn < 0)   return (1);
           showLogMessage("I","","CycC250 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex) 
               {
                showLogMessage("I","","無法執行 CycC250 ERROR!");
               }
     }
   totalCnt = 0;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理停卡資料");
   selectCrdCard();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");
   totalCnt = 0;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理效期到期資料");
   selectCycDcFundDtl1();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
  
   if ((pSeqno.length()==0)||
       (commitFlag.equals("Y"))) finalProcess();
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
 void selectCrdCard() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "max(decode(oppost_date,'','30001231',oppost_date)) as oppost_date";
  daoTable  = "crd_card b";
  whereStr  = "where curr_code != '901' "
            + "and   card_no    = major_card_no "
            ;
   if (pSeqno.length()!=0)
      {
       whereStr  = whereStr
                 + "and p_seqno = ? "
                 ;
       setString(1 , pSeqno); 
      }
                 
  whereStr  = whereStr   
            + "group by p_seqno,curr_code "
            + "having max(decode(oppost_date,'','30001231',oppost_date))!='30001231' ";

  if (businessDate.substring(6,8).equals("01"))
     {
      showLogMessage("I","","判斷日期 : ["
                           +  comm.nextMonthDate(businessDate, -37)
                           +"]-["
                           + comm.nextMonthDate(businessDate, -3)
                           + "]");
     }
  else
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextMonthDate(businessDate, -6)
                           +"]-["
                           + comm.nextMonthDate(businessDate, -3)
                           + "]");
     }

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   {
    totalCnt++; 
    if (comm.nextMonthDate(getValue("oppost_date"),3).compareTo(businessDate)>0) continue;

//    if (businessDate.substring(6,8).equals("20"))
//       if (comm.nextMonthDate(getValue("oppost_date"),6).compareTo(businessDate)<0) continue;

    if (businessDate.substring(6,8).equals("01")) {
       if (comm.nextMonthDate(getValue("oppost_date"),37).compareTo(businessDate)<0) continue;
    }else {
       if (comm.nextMonthDate(getValue("oppost_date"),6).compareTo(businessDate)<0) continue;    	
    }

    selectCycDcFundDtl();
     if (updateCnt>30000)
        {
         showLogMessage("I","","本日移除筆數["+updateCnt+"] 超過 30000 筆, 不再執行 ");
         break;
        }

   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void selectCycDcFundDtl() throws Exception
 {
  selectSQL = "fund_code,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type,"
            + "sum(end_tran_amt) as end_tran_amt,"
            + "max(fund_name) as fund_name";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  end_tran_amt != 0 "
            + "and    p_seqno      = ? "
            + "and    curr_code    = ? "
            + "group by p_seqno,fund_code,curr_code "
            + "having sum(end_tran_amt) != 0  "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("curr_code"));

  totalCnt=0;
  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
    { 
     updateCnt++;

     setValue("cddd.mod_desc"             , "停卡三個月移除");
   if (pSeqno.length()!=0)
      {
       showLogMessage("I","","p_seqno[" + pSeqno + "] id_p_seqno["+ getValue("id_p_seqno",inti)+"]");
       showLogMessage("I","","   curr_code[" + getValue("curr_code") + "] end_tran_amt[" + getValueDouble("end_tran_amt",inti)+"]");
      }
     insertCycDcFundDtl(inti);

     updateCycDcFundDtl(inti);

//     if (getValueInt("end_tran_amt",inti)<0)
//        insertCycFundDtl(1,inti);
//     else
//        insertCycFundDtl(2,inti);
    } 
  return;
 }
// ************************************************************************
 int insertCycDcFundDtl(int inti) throws Exception
 {
  tranSeqno = comr.getSeqno("ecs_dbmseq");
  dateTime();
  extendField = "cddd.";
  setValue("cddd.tran_date"            , sysDate);
  setValue("cddd.tran_time"            , sysTime);
  setValue("cddd.tran_seqno"           , tranSeqno);
  setValue("cddd.fund_code"            , getValue("fund_code",inti));
  setValue("cddd.fund_name"            , getValue("fund_name",inti));
  setValue("cddd.curr_code"            , getValue("curr_code"));
  setValue("cddd.tran_code"            , "6");
  setValue("cddd.acct_type"            , getValue("acct_type",inti));
  setValue("cddd.p_seqno"              , getValue("p_seqno"));
  setValue("cddd.id_p_seqno"           , getValue("id_p_seqno",inti));
  setValue("cddd.acct_date"            , businessDate);
  setValue("cddd.tran_pgm"             , javaProgram);
  setValue("cash.proc_month"           , businessDate.substring(0,6));
  setValue("cddd.mod_memo"             , "");
  setValueDouble("cddd.beg_tran_amt"   , getValueDouble("end_tran_amt",inti)*-1); 
  setValue("cddd.end_tran_amt"         , "0");
  setValue("cddd.apr_flag"             , "Y");
  setValue("cddd.apr_user"             , javaProgram);
  setValue("cddd.apr_date"             , sysDate);
  setValue("cddd.crt_user"             , javaProgram);
  setValue("cddd.crt_date"             , sysDate);
  setValue("cddd.mod_user"             , javaProgram); 
  setValue("cddd.mod_time"             , sysDate+sysTime);
  setValue("cddd.mod_pgm"              , javaProgram);

  daoTable  = "cyc_dc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateCycDcFundDtl(int inti) throws Exception
 {
  updateSQL = "mod_memo    = ?,"
            + "link_seqno    = ?,"
            + "link_tran_amt = end_tran_amt,"
            + "end_tran_amt = 0,"
            + "mod_pgm      = ?,"
            + "mod_time     = sysdate";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE p_seqno       = ? "
            + "and   fund_code     = ? "
            + "and   curr_code     = ? "
            + "and   end_tran_amt != 0 ";

  setString(1 , "移除序號["+ tranSeqno +"]");
  setString(2 , tranSeqno);
  setString(3 , javaProgram);
  setString(4 , getValue("p_seqno"));
  setString(5 , getValue("fund_code",inti));
  setString(6 , getValue("curr_code"));

  updateTable();
  return;
 }
// ************************************************************************
 int insertCycFundDtl(int numType, int inti) throws Exception
 {
  dateTime();
  setValue("fund.create_date"          , sysDate);
  setValue("fund.create_time"          , sysTime);
  setValue("fund.curr_code"            , getValue("curr_code"));
  setValue("fund.business_date"        , businessDate);
  setValue("fund.acct_type"            , getValue("acct_type",inti));
  setValue("fund.p_seqno"              , getValue("p_seqno"));
  setValue("fund.id_p_seqno"           , getValue("id_p_seqno",inti));
  setValue("fund.fund_code"            , getValue("fund_code",inti));
  setValue("fund.vouch_type"           , "3");
  if (numType==1)
     setValue("fund.cd_kind"           , "A394");   /* 基金負項移除 */
  else
     setValue("fund.cd_kind"           , "A393");
  setValue("fund.tran_code"            , "6");
  setValue("fund.memo1_type"           , "1");
  setValueDouble("fund.fund_amt"       , Math.abs(getValueDouble("end_tran_amt",inti)));  
  setValue("fund.proc_flag"            , "N");
  setValue("fund.proc_date"            , "");
  setValue("fund.execute_date"         , businessDate);
  setValue("fund.fund_cnt"             , "1");
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , javaProgram);

  extendField = "fund.";
  daoTable  = "cyc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectCycDcFundDtl0() throws Exception
 {
  selectSQL = "count(*) as data_cnt";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  end_tran_amt != 0 "
            + "group by p_seqno,curr_code,fund_code "
            + "having (sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 "
            + " and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0) "
            + "and count(*) > 0 "
            ;

  showLogMessage("I","","===============================");
  showLogMessage("I","","檢查是否已重整 ...");

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      showLogMessage("I","","資料已重整");
      return(0);
     }

  showLogMessage("I","","資料未重整");

  return(1);
 }
// ************************************************************************
 void selectCycDcFundDtl1() throws Exception
 {
  selectSQL = "p_seqno,"
            + "effect_e_date,"
            + "curr_code, "
            + "fund_code, "
            + "max(fund_name) as fund_name,"
            + "sum(end_tran_amt) as end_tran_amt, "
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where end_tran_amt > 0 "
            + "and   effect_e_date between ? and ? "
            ;

  if (businessDate.substring(6,8).equals("01"))
     setString(1 , comm.nextMonthDate(businessDate, -36));
  else
     setString(1 , comm.nextNDate(businessDate, -7));

  setString(2 , comm.nextNDate(businessDate, -1));

   if (pSeqno.length()!=0)
      {
       whereStr  = whereStr
                 + "and p_seqno = ? "
                 ;
       setString(3 , pSeqno); 
      }
  whereStr  = whereStr
            + "group by p_seqno,effect_e_date,fund_code,curr_code ";

  if (businessDate.substring(6,8).equals("01"))
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextMonthDate(businessDate, -36)
                           +"]-["
                           + comm.nextNDate(businessDate, -1)
                           + "]");
     }
  else
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextNDate(businessDate, -7)
                           +"]-["
                           + comm.nextNDate(businessDate, -1)
                           + "]");
     }

  openCursor();
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    setValue("cddd.mod_desc"       , "效期到期移除");

    insertCycDcFundDtl(0);

    updateCycDcFundDtl1();

//    if (getValueInt("end_tran_amt")<0)
//       insertCycFundDtl(1,0);
//    else
//       insertCycFundDtl(2,0);

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();

  return;
 }
// ************************************************************************
 void updateCycDcFundDtl1() throws Exception
 {
  updateSQL = "mod_memo    = ?,"
            + "link_seqno    = ?,"
            + "link_tran_amt = end_tran_amt,"
            + "end_tran_amt = 0,"
            + "mod_pgm      = ?,"
            + "mod_time     = sysdate";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE p_seqno       = ? "
            + "and   fund_code     = ? "
            + "and   curr_code     = ? "
            + "and   end_tran_amt != 0 "
            + "and   effect_e_date between ? and ? "
            ;

  setString(1 , "移除序號["+ tranSeqno +"]");
  setString(2 , tranSeqno);
  setString(3 , javaProgram);
  setString(4 , getValue("p_seqno"));
  setString(5 , getValue("fund_code"));
  setString(6 , getValue("curr_code"));
  if (businessDate.substring(6,8).equals("01"))
     setString(7 , comm.nextMonthDate(businessDate, -36));
  else
     setString(7 , comm.nextNDate(businessDate, -7));

  setString(8 , comm.nextNDate(businessDate, -1));

  updateTable();
  return;
 }
// ************************************************************************

}  // End of class FetchSample

