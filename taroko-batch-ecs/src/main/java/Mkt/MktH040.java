/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/08/24  V1.00.00  Allen Ho   new                                        *
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
public class MktH040 extends AccessDAO
{
 private  String progname = "台幣基金-專案回饋金(imloan)檢核及轉入處理程式 109/12/09 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate   = "";
 String tranSeqno = "";
 String fundCode  = "";

 int    parmCnt=0;
 long   totalCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktH040 proc = new MktH040();
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

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","     2 : [fund_code]");
       return(1);
      }

   if (args.length >= 1)
      businessDate = args[0]; 

   if (args.length >= 2)
      fundCode = args[1]; 
   
   if ( !connectDataBase() ) exitProgram(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktParmData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktLoanParm();
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
 int selectMktLoanParm() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "mkt_loan_parm";
  whereStr  = "WHERE list_cond   = 'Y' "
            + "and   list_feedback_date = '' "
            ;

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr  
                + "and   fund_code   = ? ";
      setString(1 , fundCode);
     }

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) 
     {
      showLogMessage("I","","無符合之基金參數可處理 !");
      return(1);
     }

  for (int inti=0;inti<parmCnt;inti++)
    {
     showLogMessage("I","","符合之基金參數:["+getValue("parm.fund_code",inti)+"]["+getValue("parm.fund_name",inti)+"]");
     setValue("data_key" , getValue("parm.fund_code",inti));

     showLogMessage("I","","累計處理筆數 [" + totalCnt + "]");
     selectMktImloanList(inti);
     updateMktLoanParm(inti);
    }
  return(0);
 }
// ************************************************************************
  int selectMktImloanList(int inti) throws Exception
 {
  selectSQL = "list_flag,"
            + "list_data,"
            + "id_p_seqno,"
            + "acct_type,"
            + "p_seqno,"
            + "card_no,"
            + "fund_code,"
            + "feedback_amt,"
            + "error_code ,"
            + "rowid as rowid";
  daoTable  = "mkt_imloan_list";
  whereStr  = "WHERE fund_code    = ? " 
            ;

  setString(1, getValue("parm.fund_code",inti));

  openCursor();

  totalCnt = 0;
  while( fetchTable() ){    
     totalCnt++;

     if (getValue("list_flag").equals("1")){
         if (selectCrdIdno(inti)!=0)
            {
             updateMktImloanList0("3");  // 回饋條件不符
             continue;
            }                     
     }else if (getValue("list_flag").equals("2")){
    	 //reserve another check condition (force check in mktm4070 )
    	 if ( !getValue("error_code").equals("00") ) {
             updateMktImloanList0("Y"); 
             continue;
          } 
         if ( (getValueLong("feedback_amt") == 0) ){
             updateMktImloanList0("Y"); 
             continue;        	 
         }
    	 
     }else if (getValue("list_flag").equals("6")){   
    	//reserve check condition (force check in mktm4070 )
    	 if ( !getValue("error_code").equals("00") ) {
             updateMktImloanList0("Y"); 
             continue;
          }    
         if ( (getValueLong("feedback_amt") == 0) ){
             updateMktImloanList0("Y"); 
             continue;        	 
         }   	 
     }else{
         if (selectMktParmData(getValue("acct_type"),
                                  getValue("parm.acct_type_sel",inti),"1",2)!=0)
            {
             updateMktImloanList0("1");  // 帳戶類別不符
             continue;
            }                     
         if (selectCrdCard(inti)!=0)
            {
             updateMktImloanList0("2");  // 團體代號不符
             continue;
            }              
     }

     selectPtrWorkday();
     if ( !(getValueLong("feedback_amt") == 0) ){
        insertMktCashbackDtl(inti);
//      insertCycFundDtl(1,inti);
     }
        
     if (getValue("list_flag").equals("1")){
        updateMktImloanList1();
     }else{
    	updateMktImloanList3("Y"); 
     }
  }//while     

  return(0);
 }
// ************************************************************************
 void  loadMktParmData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_parm_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_LOAN_PARM' "
            + "and   data_key in ( "
            + "      select fund_code "
            + "      from mkt_loan_parm "
            + "      WHERE list_cond   = 'Y' "
            + "      and   list_feedback_date = '') "
            + "order by data_key,data_type,data_code"
            ;

  int  n = loadTable();
  setLoadData("data.data_key,data.data_type,data.data_code");

  showLogMessage("I","","Load mkt_parm_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectCrdIdno(int intm) throws Exception
 {
  extendField = "card.";
  selectSQL = "a.acct_type,"
            + "a.p_seqno,"
            + "a.id_p_seqno,"
            + "a.group_code,"
            + "a.card_no,"
            + "a.stmt_cycle,"
            + "a.current_code,"
            + "a.oppost_date";
  daoTable  = "crd_card a,crd_idno b";
  whereStr  = "WHERE a.id_p_seqno = b.id_p_seqno "
            + "and   b.id_no      = ? "
            + "and   a.current_code in ('0','2','5') "
  //狀態碼        0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡
            ;

  setString(1 , getValue("list_data"));

  int recCnt = selectTable();

  for (int inti=0;inti<recCnt;inti++){
  //${cancel_event} 1.不限  2.有有效卡  3.有聯名卡有效卡
     if (getValue("parm.cancel_event",intm).equals("2"))
        if (!getValue("card.current_code",inti).equals("0"))
           if (businessDate.compareTo(comm.nextMonthDate(getValue("card.oppost_date",inti),3)) >0) continue;

     if (selectMktParmData(getValue("card.acct_type",inti),
                              getValue("parm.acct_type_sel",intm),"1",2)!=0) continue;

     if (selectMktParmData(getValue("card.group_code",inti),"",
                              getValue("parm.group_code_sel",intm),"2",2)!=0) continue;

     if (getValue("parm.cancel_event",intm).equals("3"))
        if (!getValue("card.current_code",inti).equals("0"))
           if (businessDate.compareTo(comm.nextMonthDate(getValue("card.oppost_date",inti),3)) >0) continue;

     setValue("acct_type"  , getValue("card.acct_type",inti));
     setValue("p_seqno"    , getValue("card.p_seqno",inti)); 
     setValue("id_p_seqno" , getValue("card.id_p_seqno",inti)); 
     setValue("card_no"    , getValue("card.card_no",inti)); 
     setValue("stmt_cycle" , getValue("card.stmt_cycle",inti)); 
     return(0);
  } 
  return(1);
 }
// ************************************************************************
 int selectCrdCard(int intm) throws Exception
 {
  extendField = "card.";
  selectSQL = "group_code,"
            + "stmt_cycle,"
            + "current_code,"
            + "oppost_date";
  daoTable  = "crd_card";
  whereStr  = "WHERE card_no      = ? "
            + "and   current_code in ('0','2','5') "
            ;
  //狀態碼        0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡
  
  setString(1 , getValue("card_no"));

  int recCnt = selectTable();

  for (int inti=0;inti<recCnt;inti++)
    {
     if (getValue("parm.cancel_event",intm).equals("2"))
        if (!getValue("card.current_code",inti).equals("0"))
           if (businessDate.compareTo(comm.nextMonthDate(getValue("card.oppost_date",inti),3)) >0) continue;

     if (selectMktParmData(getValue("card.group_code",inti),"",
                              getValue("parm.group_code_sel",intm),"2",2)!=0) continue;

     if (getValue("parm.cancel_event",intm).equals("3"))
        if (!getValue("card.current_code",inti).equals("0"))
           if (businessDate.compareTo(comm.nextMonthDate(getValue("card.oppost_date",inti),3)) >0) continue;

     setValue("stmt_cycle" , getValue("card.stmt_cycle",inti)); 
     return(0);
    } 
  return(1);
 }
// ************************************************************************
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "next_acct_month";
  daoTable  = "ptr_workday a";
  whereStr  = "WHERE stmt_cycle = ? "
            ;
  setString(1, getValue("stmt_cycle"));

  selectTable();

  return(0);
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
 int insertMktCashbackDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("mkt_modseq");
  dateTime();

  setValue("mcdl.tran_date"            , sysDate);
  setValue("mcdl.tran_time"            , sysTime);
  setValue("mcdl.fund_code"            , getValue("fund_code"));
  setValue("mcdl.fund_name"            , getValue("parm.fund_name",inti));
  setValue("mcdl.p_seqno"              , getValue("p_seqno"));
  setValue("mcdl.acct_type"            , getValue("acct_type"));
  setValue("mcdl.id_p_seqno"           , getValue("id_p_seqno"));

  setValue("mcdl.mod_desc"             , "");
  setValue("mcdl.mod_memo"             , "");
  if (!getValue("list_flag").equals("1"))
     {
      setValue("mcdl.mod_desc"             , "卡號 :["+getValue("card_no")+"]");
      if (getValue("list_flag").equals("2"))
          setValue("mcdl.mod_memo"             , "TaiwanPay卡號 :["+getValue("list_data")+"]");       
      if (getValue("list_flag").equals("3"))
          setValue("mcdl.mod_memo"             , "匯入一卡通卡號 :["+getValue("list_data")+"]"); 
      else if (getValue("list_flag").equals("4"))
          setValue("mcdl.mod_memo"             , "匯悠遊入卡號 :["+getValue("list_data")+"]");
      else if (getValue("list_flag").equals("5"))
          setValue("mcdl.mod_memo"             , "匯入愛金卡號 :["+getValue("list_data")+"]");
      else if (getValue("list_flag").equals("6"))
          setValue("mcdl.mod_memo"             , "生日禮 :[" + getValue("list_data") + "]");
     }

  setValue("mcdl.tran_pgm"             , javaProgram);
  setValue("mcdl.effect_e_date"   , "");
  if (getValueInt("parm.effect_months",inti)>0)
     setValue("mcdl.effect_e_date"     , comm.nextMonthDate(businessDate
                                       , getValueInt("parm.effect_months",inti)));
  setValue("mcdl.beg_tran_amt"         , getValue("feedback_amt"));
  setValue("mcdl.end_tran_amt"         , getValue("feedback_amt"));
  setValue("mcdl.res_tran_amt"         , "0");
  setValue("mcdl.res_total_cnt"        , "0");
  setValue("mcdl.res_tran_cnt"         , "0");
  setValue("mcdl.res_s_month"          , "");
  setValue("mcdl.res_upd_date"         , "");

  if (getValueInt("feedback_amt")>0)
     {
      setValue("mcdl.tran_code"            , "2");

      if (getValue("parm.res_flag",inti).equals("2"))
         {
          if (getValueInt("parm.exec_s_months",inti)>0)
             {
              setValueInt("mcdl.end_tran_amt"  , 0);
              setValue("mcdl.res_s_month"      , comm.nextMonth(getValue("wday.next_acct_month")
                                               , getValueInt("parm.exec_s_months",inti)));

              setValue("mcdl.res_tran_amt"     , getValue("feedback_amt"));
              setValueInt("mcdl.res_total_cnt" , 1);
              if (getValueInt("parm.effect_months",inti)!=0)
                 setValue("mcdl.effect_e_date"  , comm.nextMonthDate(businessDate
                                                , getValueInt("parm.effect_months",inti)
                                                + getValueInt("parm.exec_s_months",inti)));
             }
         }
      else
         {
          int avgAmt = (int)Math.floor(getValueInt("feedback_amt")
                      / getValueInt("parm.res_total_cnt",inti));

          int fstAmt = getValueInt("feedback_amt")
                      - (avgAmt * (getValueInt("parm.res_total_cnt",inti)-1));

          setValueInt("mcdl.end_tran_amt"      , fstAmt);
          setValueInt("mcdl.res_tran_amt"      , avgAmt * (getValueInt("parm.res_total_cnt",inti)-1));
          setValueInt("mcdl.res_total_cnt"     , getValueInt("parm.res_total_cnt",inti));
          setValueInt("mcdl.res_tran_cnt"      , getValueInt("parm.res_total_cnt",inti)-1);
          setValue("mcdl.res_s_month"          , businessDate.substring(0,6));
          setValue("mcdl.res_upd_date"         , businessDate);
         }
     }
  else
     {
      setValue("mcdl.tran_code"            , "3");
     }

  setValue("mcdl.tran_seqno"           , tranSeqno);
  setValue("mcdl.acct_month"           , getValue("wday.next_acct_month"));
  setValue("mcdl.acct_date"            , businessDate);
  setValue("mcdl.mod_reason"           , "");
  setValue("mcdl.case_list_flag"       , "Y");
  setValue("mcdl.crt_user"             , javaProgram);
  setValue("mcdl.crt_date"             , sysDate);
  setValue("mcdl.apr_date"             , sysDate);
  setValue("mcdl.apr_user"             , javaProgram);
  setValue("mcdl.apr_flag"             , "Y");
  setValue("mcdl.mod_user"             , javaProgram);
  setValue("mcdl.mod_time"             , sysDate+sysTime);
  setValue("mcdl.mod_pgm"              , javaProgram);

  extendField = "mcdl.";
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
// int insertCycFundDtl(int numType,int inti) throws Exception
// {
//  dateTime();
//  extendField = "cfdl.";
//
//  setValue("cfdl.business_date"        , businessDate);
//  setValue("cfdl.create_date"          , sysDate);
//  setValue("cfdl.curr_code"            , "901");
//  setValue("cfdl.create_time"          , sysTime);
//  setValue("cfdl.id_p_seqno"           , getValue("id_p_seqno"));
//  setValue("cfdl.p_seqno"              , getValue("p_seqno"));
//  setValue("cfdl.acct_type"            , getValue("acct_type"));
//  setValue("cfdl.card_no"              , "");
//  setValue("cfdl.fund_code"            , getValue("fund_code"));
//  setValue("cfdl.vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
//  if (numType==1)
//     {
//      setValue("cfdl.tran_code"            , "2");
//      if (getValue("parm.add_vouch_no",inti).length()>0)
//         setValue("cfdl.cd_kind"              , getValue("parm.add_vouch_no",inti));   /* 基金產生 */
//      else
//         setValue("cfdl.cd_kind"              , "H001");   /* 基金產生 */
//     }
//  else
//     {
//      setValue("cfdl.tran_code"            , "3");
//      if (getValue("parm.rem_vouch_no",inti).length()>0)
//          setValue("cfdl.cd_kind"              , getValue("parm.rem_vouch_no",inti));
//      else
//         setValue("cfdl.cd_kind"              , "H003");
//     }
//  setValue("cfdl.memo1_type"           , "1");   /* fund_code 必須有值 */
//  setValueInt("cfdl.fund_amt"          , Math.abs(getValueInt("feedback_amt")));
//  setValue("cfdl.other_amt"            , "0");
//  setValue("cfdl.proc_flag"            , "N");
//  setValue("cfdl.proc_date"            , "");
//  setValue("cfdl.execute_date"         , businessDate);
//  setValue("cfdl.fund_cnt"             , "1");
//  setValue("cfdl.mod_user"             , javaProgram);
//  setValue("cfdl.mod_time"             , sysDate+sysTime);
//  setValue("cfdl.mod_pgm"              , javaProgram);
//
//  daoTable  = "cyc_fund_dtl";
//
//  insertTable();
//
//  return(0);
// }
// ************************************************************************
 void updateMktImloanList0(String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag       = ?,"
            + "proc_date       = ?,"
            + "mod_pgm         = ?,"
            + "mod_time        = sysdate";  
  daoTable  = "mkt_imloan_list";
  whereStr  = "WHERE rowid     = ? ";

  setString(1 , procFlag);
  setString(2 , businessDate);
  setString(3 , javaProgram);
  setRowId(4 , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktImloanList1() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag       = 'Y',"
            + "proc_date       = ?, "
            + "id_p_seqno      = ?,"
            + "tran_seqno      = ?,"
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";
  daoTable  = "mkt_imloan_list";
  whereStr  = "WHERE rowid     = ? ";

  setString(1 , businessDate);
  setString(2 , getValue("id_p_seqno"));
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktImloanList2() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag       = 'Y',"
            + "proc_date       = ?, "
            + "acct_type       = ?,"
            + "p_seqno         = ?,"
            + "id_p_seqno      = ?,"
            + "card_no         = ?,"
            + "tran_seqno      = ?,"
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";
  daoTable  = "mkt_imloan_list";
  whereStr  = "WHERE rowid     = ? ";

  setString(1 , businessDate);
  setString(2 , getValue("acct_type"));
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("id_p_seqno"));
  setString(5 , getValue("card_no"));
  setString(6 , tranSeqno);
  setString(7 , javaProgram);
  setRowId(8  , getValue("rowid"));

  updateTable();
  return;
 }

//************************************************************************
 void updateMktImloanList3(String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag       = ?,"
            + "proc_date       = ?,"
            + "tran_seqno      = ?,"
            + "mod_pgm         = ?,"
            + "mod_time        = sysdate";  
  daoTable  = "mkt_imloan_list";
  whereStr  = "WHERE rowid     = ? ";

  setString(1 , procFlag);
  setString(2 , businessDate);
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setRowId(5 , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int updateMktLoanParm(int inti) throws Exception
 {
  daoTable  = "mkt_loan_parm";
  updateSQL = "list_feedback_date = ?, "
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";   
  whereStr  = "where fund_code = ? "
            ;

  setString(1 , businessDate);
  setString(2 , javaProgram);
  setString(3 , getValue("parm.fund_code",inti));

  int n = updateTable();

  return n;
 }
// ************************************************************************

}  // End of class FetchSample

