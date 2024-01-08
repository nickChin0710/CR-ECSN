/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/08/28  V1.00.00  Allen Ho                                              *
* 109-12-09  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktH120 extends AccessDAO
{
 private  String progname = "基金-指定雙幣基金無效卡達三個月移除處理程式 109/12/09 V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comc = null;
 CommRoutine comr = null;

 String businessDate  = "";
 String tranSeqno     = "";

 int    cnt1=0,cnt2=0,parmCnt=0;
 long    totalCnt=0,updateCnt=0;
 long    updateCnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktH120 proc = new MktH120();
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
   showLogMessage("I","",javaProgram+" "+progname);

   if (comm.isAppActive2(javaProgram))
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

   comc = new CommCashback(getDBconnect(),getDBalias());
   comr   = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本程式只再關帳日次一日執行 !");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectCycDcFundParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","無參數資料需執行 !");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadActAcctCurr();
   loadCrdCard();
   loadMktParmData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理停卡資料");
   selectCycDcFundDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"]["+updateCnt1+"] 筆");
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

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 void  selectCycDcFundDtl() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "fund_code,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type,"
            + "sum(end_tran_amt) as end_tran_amt";
  daoTable  = "cyc_dc_fund_dtl a";
  whereStr  = "where  end_tran_amt != 0 "
            + "and    exists ( "
            + "       select 1  "
            + "       from cyc_dc_fund_parm "
            + "       where group_card_sel = '1' "
            + "       and fund_code = a.fund_code "
            + "       and group_oppost_cond = 'Y') "
            + "group by p_seqno,curr_code,fund_code "
            + "having sum(end_tran_amt) != 0  "
            ;

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    setValue("acno.p_seqno"   ,  getValue("p_seqno"));
    setValue("acno.curr_code" ,  getValue("curr_code"));
    cnt1 =  getLoadData("acno.p_seqno,acno.curr_code");
    if (cnt1==0) continue;

    int intk=-1;
    for (int intm=0;intm<parmCnt;intm++)
      {
       if (!getValue("fund_code").equals(getValue("parm.fund_code",intm))) continue;
       intk=intm;
       break;
      }
    if (intk==-1)
       {
        showLogMessage("I","","基金代碼["+ getValue("fund_code") +"] not found !");
        continue;
       }

    setValue("card.p_seqno" ,  getValue("p_seqno"));
    cnt1 =  getLoadData("card.p_seqno");
    if (cnt1==0)
       {
        updateCnt++;
        insertCycDcFundDtl(intk);
        updateCycDcFundDtl();
//        if (getValueInt("end_tran_amt")<0)
//           insertCycVouchData(1,intk);
//        else
//           insertCycVouchData(2,intk);
        continue;
       }

    int okFlag=0;
    for (int inti=0;inti<cnt1;inti++)
      {
       setValue("data_key", getValue("parm.fund_code",intk));
       if (getValue("card.group_code",inti).length()==0)
          setValue("card.group_code", "0000" , inti);

       if (selectMktParmData(getValue("card.group_code",inti)
                               ,getValue("card.card_type",inti),"1","1",3)!=0) continue;
          {
           okFlag=1;
           break;
          }
      }
    if (okFlag==1) continue;
    
    updateCnt1++;

//  showLogMessage("I","","2 fund_code ["+ getValue("fund_code") +"]");
//  showLogMessage("I","","  p_seqno   ["+ getValue("p_seqno") +"]");

    insertCycDcFundDtl(intk);

    updateCycDcFundDtl();

//    if (getValueInt("end_tran_amt")<0)
//       insertCycVouchData(1,intk);
//    else
//       insertCycVouchData(2,intk);
    } 
  closeCursor();
  return;
 }
// ************************************************************************
 int insertCycDcFundDtl(int inti) throws Exception
 {
  dateTime();
  extendField = "cash.";
  tranSeqno     = comr.getSeqno("ecs_dbmseq");
  setValue("cash.tran_date"            , sysDate);
  setValue("cash.tran_time"            , sysTime);
  setValue("cash.tran_seqno"           , tranSeqno);
  setValue("cash.fund_code"            , getValue("fund_code"));
  setValue("cash.fund_name"            , getValue("parm.fund_name",inti));
  setValue("cash.curr_code"            , getValue("curr_code"));
  setValue("cash.acct_type"            , getValue("acct_type"));
  setValue("cash.tran_code"            , "6");
  setValue("cash.mod_desc"             , "指定團代卡種無有效卡移除");
  setValueInt("cash.beg_tran_amt"      , getValueInt("end_tran_amt")*-1);
  setValueInt("cash.end_tran_amt"      , 0);
  setValue("cash.mod_memo"             , "");
  setValue("cash.p_seqno"              , getValue("p_seqno"));
  setValue("cash.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cash.acct_date"            , businessDate);
  setValue("cash.tran_pgm"             , javaProgram);
  setValue("cash.proc_month"           , businessDate.substring(0,6));
  setValue("cash.apr_user"             , javaProgram);
  setValue("cash.apr_flag"             , "Y");
  setValue("cash.apr_date"             , sysDate);
  setValue("cash.crt_user"             , javaProgram);
  setValue("cash.crt_date"             , sysDate);
  setValue("cash.mod_user"             , javaProgram); 
  setValue("cash.mod_time"             , sysDate+sysTime);
  setValue("cash.mod_pgm"              , javaProgram);

  daoTable  = "cyc_dc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateCycDcFundDtl() throws Exception
 {
  dateTime();
  updateSQL = "mod_memo      = ?,"
            + "link_seqno    = ?,"
            + "link_tran_amt = end_tran_amt,"
            + "end_tran_amt   = 0,"
            + "mod_pgm        = ?,"
            + "mod_time       = sysdate";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE p_seqno       = ? "
            + "and   curr_code     = ? "
            + "and   fund_code     = ? "
            + "and   end_tran_amt != 0 ";

  setString(1 , "移除序號["+tranSeqno+"]");
  setString(2 , tranSeqno);
  setString(3 , javaProgram);
  setString(4 , getValue("p_seqno"));
  setString(5 , getValue("curr_code"));
  setString(6 , getValue("fund_code"));

  updateTable();
  return;
 }
// ************************************************************************
 int insertCycVouchData(int numType,int inti) throws Exception
 {
  dateTime();
  setValue("fund.create_date"          , sysDate);
  setValue("fund.create_time"          , sysTime);
  setValue("fund.curr_code"            , getValue("curr_code"));
  setValue("fund.business_date"        , businessDate);
  setValue("fund.acct_type"            , getValue("acct_type"));
  setValue("fund.p_seqno"              , getValue("p_seqno"));
  setValue("fund.apyment_type"         , getValue("fund_code").substring(0,4));
  setValueDouble("fund.vouch_amt"      , Math.abs(getValueDouble("end_tran_amt")));
  setValueDouble("fund.d_vouch_amt"    , 0);
  if (numType==1)
     {
      setValue("vouch_data_type"           , "8");     /* 基金負項移除 */
     }
  else
     {
      setValue("vouch_data_type"           , "6");
     }
  setValue("fund.src_pgm"              , javaProgram);
  setValue("fund.proc_flag"            , "N");
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , javaProgram);

  extendField = "fund.";
  daoTable  = "cyc_vouch_data";

  insertTable();

  return(0);
 }
// ************************************************************************
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "stmt_cycle,"
            + "next_acct_month";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1 , comm.nextNDate(businessDate , -1));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 void  loadActAcctCurr() throws Exception
 {
  extendField = "acno.";
  selectSQL = "a.p_seqno,"
            + "a.curr_code";
  daoTable  = "act_acct_curr a,act_acno b";
  whereStr  = "WHERE b.stmt_cycle = ? "
            + "and   a.p_seqno    = b.p_seqno "
            + "and   a.curr_code != '901' "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  int  n = loadTable();
  setLoadData("acno.p_seqno,acno.curr_code");

  showLogMessage("I","","Load act_acct_curr Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "p_seqno,"
            + "group_code,"
            + "card_type";
  daoTable  = "crd_card";
  whereStr  = "WHERE stmt_cycle = ? "
            + "and   (oppost_date = '' "
            + " or    oppost_date >= ? )"
            + "and   card_no    = major_card_no "
            + "and   curr_code != '901' "
            + "order by p_seqno"
            ;

  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , comm.nextMonthDate(businessDate,-3));

  showLogMessage("I","","oppost_date >= ["+ comm.nextMonthDate(businessDate,-3) +"]");
  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 int selectCycDcFundParm() throws Exception
 {
  extendField = "parm.";
  selectSQL = "fund_code,"
            + "fund_name";
  daoTable  = "cyc_dc_fund_parm";
  whereStr  = "WHERE group_card_sel = '1' "
            + "and   group_oppost_cond = 'Y' "
            ;  

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  showLogMessage("I","","基金參數 ["+ parmCnt +"] 筆");
  return(0);
 }
// ************************************************************************
 public void  loadMktParmData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_parm_data";
  whereStr  = "WHERE TABLE_NAME = 'CYC_DC_FUND_PARM' "
            + "and data_type    = '1' "
            + "order by data_key,data_type,data_code ";

  int  n = loadTable();
  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_parm_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktParmData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktParmData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktParmData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
     }

  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (dataNum==2)
        {
         if ((col1.length()!=0)&&
             (getValue("data.data_code",intm).length()!=0)&&
          (!getValue("data.data_code",intm).equals(col1))) continue;

         if ((col2.length()!=0)&&
             (getValue("data.data_code2",intm).length()!=0)&&
          (!getValue("data.data_code2",intm).equals(col2))) continue;

         if ((col3.length()!=0)&&
             (getValue("data.data_code3",intm).length()!=0)&&
          (!getValue("data.data_code3",intm).equals(col3))) continue;
        }
     else
        {
         if (col2.length()!=0)
            {
             if ((getValue("data.data_code2",intm).length()!=0)&&
                 (!getValue("data.data_code2",intm).equals(col2))) continue;
            }
         if (col3.length()!=0)
            {
             if ((getValue("data.data_code3",intm).length()!=0)&&
                 (!getValue("data.data_code3",intm).equals(col3))) continue;
            }
        }

     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************

}  // End of class FetchSample

