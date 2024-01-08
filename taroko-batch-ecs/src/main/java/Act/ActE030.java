/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/03/26  V1.00.01  Allen Ho   New                                        *
* 111/01/04  V1.00.02  Simon      M#9174：台幣基金撥入溢付款處理時須同步更新 *
*                                 ttl_amt_bal,min_pay_bal,act_acag,..        *
* 111/10/12  V1.00.03  Yang Bo    sync code from mega                        *
* 111/11/17  V1.00.04  Holmes     change accounting code as H005             *
******************************************************************************/
package Act;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class ActE030 extends AccessDAO
{
 private final String PROGNAME = "台幣基金-基金撥入溢付款處理程式 111/10/12  V1.00.03";
 CommFunction comm = new CommFunction();
 CommCashback comc = null;
 CommRoutine comr = null;
 CommCrdRoutine comcr    = null;

 String businessDate = "";
 String tranSeqno ="";

 long    totalCnt=0,updateCnt=0;
 int parmCnt=0;
 int okFlag =0;
 int cnt1=0;
 int[] fundCnt = new int[500];
 double[] fundAmt = new double[500];
 double hCurrTtlAmtBal =0, hCurrDcTtlAmtBal =0, hCurrMinPayBal =0, hCurrDcMinPayBal =0;

// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  ActE030 proc = new ActE030();
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
      { businessDate = args[0]; }
   
   if ( !connectDataBase() ) exitProgram(1);
   comc = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日不處理...");
       return(0);
      }

   if (selectMktLoanParm()!=0)
      {
       showLogMessage("I","","無入溢付款參數資料");
       return(0);
      }
   showLogMessage("I","","=========================================");
   loadActAcno();
   loadMktParmData();

/* 直接轉溢繳 , 故不需執行重整
   showLogMessage("I","","=========================================");
   showLogMessage("I","","重整基金資料...");
   selectMktCashbackDtl0();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
*/
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理基金轉溢繳資料...");
   selectMktCashbackDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");

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
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where next_close_date = ? "
            ;

  setString(1, businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void selectMktCashbackDtl0() throws Exception
 {
  selectSQL = "fund_code,"
            + "p_seqno,"
            + "tran_seqno";
  daoTable  = "mkt_cashback_dtl a";
  whereStr  = "where end_tran_amt < 0 "
            + "and   exists ( "
            + "      select fund_code "
            + "      from mkt_loan_parm "           
            + "      where move_cond = 'Y'  "
            + "      and   fund_code = a.fund_code ) "
            ;

  openCursor();
  //comc.hMcdlModPgm = javaProgram;
  comc.modPgm = javaProgram;
 totalCnt=0;   
  while( fetchTable() ) 
   {
    setValue("acno.p_seqno",getValue("p_seqno"));
    cnt1 = getLoadData("acno.p_seqno");
    if (cnt1<=0) continue;

    totalCnt++;
    updateCnt = updateCnt + comc.cashbackFunc(getValue("tran_seqno"));
   } 
  closeCursor();

  return;
 }
// ************************************************************************
 void selectMktCashbackDtl() throws Exception
 {
  selectSQL = "p_seqno,"
            + "fund_code, "
            + "max(fund_name) as fund_name,"
            + "sum(end_tran_amt) as end_tran_amt, "
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type";
  daoTable  = "mkt_cashback_dtl a";
  whereStr  = "where end_tran_amt != 0 "  
//          + "and   p_seqno = '0001757610' "
            + "and   exists ( "
            + "      select fund_code "
            + "      from mkt_loan_parm "           
            + "      where move_cond = 'Y'  "
            + "      and   fund_code = a.fund_code ) "
            + "group by p_seqno,fund_code "
            + "having sum(end_tran_amt) > 0 "
            ;
   
  openCursor();
  totalCnt=0;
  int inti=0;
  setValue("jrnl.enq_seqno" , "0");
  while( fetchTable() ) 
   { 
    setValue("acno.p_seqno",getValue("p_seqno"));
    cnt1 = getLoadData("acno.p_seqno");
    if (cnt1<=0) continue;

    okFlag=0;
    for (int intm =0;intm<parmCnt;intm++)
     { 
      if (getValue("parm.fund_code",intm).equals(getValue("fund_code")))
         {
          inti = intm;
          setValue("data_key" , getValue("parm.fund_code",inti));

          if (selectMktParmData(getValue("acct_type"),
                                   getValue("parm.acct_type_sel",inti),"1",2)!=0) break;

          if (selectCrdCard(inti,getValue("parm.cancel_event",inti))!=0) break;

          okFlag=1;
          break;
         }
     } 
    if (okFlag==0) continue;

//  if (!getValue("p_seqno").equals("0001886740")) continue;

    totalCnt++;
    fundCnt[inti]++;
    fundAmt[inti] = fundAmt[inti] + getValueDouble("end_tran_amt");
  
    insertMktCashbackDtl();
    updateMktCashbackDtl();
    insertCycFundDtl();
    selectActAcctCurr();
    lastCurrData();        // 處理 ttl_amt_bal,min_pay_bal,act_acag,...
    updateActAcctCurr();
    selectActAcctCurr1();
    updateActAcct();
    insertActJrnl();
    insertCycPyaj();
 
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();

  for (inti=0;inti<parmCnt;inti++)
    {
     if (fundCnt[inti]==0) continue;
     showLogMessage("I","","活動代號:["+ getValue("parm.fund_code",inti) +"]-"
                                       + getValue("parm.fund_name",inti) + " 筆數 ["
                                       + fundCnt[inti] + "]  累計金額 ["
                                       + fundAmt[inti] + "]");

    }  

  return;
 }
// ************************************************************************
 int insertMktCashbackDtl() throws Exception
 {
  dateTime();
  extendField = "cash.";
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");
  setValue("cash.tran_date"            , sysDate);
  setValue("cash.tran_time"            , sysTime);
  setValue("cash.tran_seqno"           , tranSeqno);
  setValue("cash.fund_code"            , getValue("fund_code"));
  setValue("cash.fund_name"            , getValue("fund_name"));
  setValue("cash.acct_type"            , getValue("acct_type"));
  setValue("cash.tran_code"            , "0");
  setValueInt("cash.beg_tran_amt"      , getValueInt("end_tran_amt")*-1);
  setValueInt("cash.end_tran_amt"      , 0);
  setValue("cash.tax_flag"             , "N");
  setValue("cash.mod_desc"             , "基金撥入溢付款");
  setValue("cash.mod_memo"             , "基金轉溢付款移除"); 
  setValue("cash.p_seqno"              , getValue("p_seqno"));
  setValue("cash.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cash.acct_date"            , businessDate);
  setValue("cash.tran_pgm"             , javaProgram);
  setValue("cash.acct_month"           , getValue("wday.next_acct_month"));
  setValue("cash.proc_month"           , businessDate.substring(0,6));
  setValue("cash.apr_user"             , javaProgram);
  setValue("cash.apr_flag"             , "Y");
  setValue("cash.apr_date"             , sysDate);
  setValue("cash.crt_user"             , javaProgram);
  setValue("cash.crt_date"             , sysDate);
  setValue("cash.mod_user"             , javaProgram); 
  setValue("cash.mod_time"             , sysDate+sysTime);
  setValue("cash.mod_pgm"              , javaProgram);

  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktCashbackDtl() throws Exception
 {
  dateTime();
  updateSQL = "remove_date   = ?, "
            + "mod_memo      = ?,"
            + "link_seqno    = ?,"
            + "link_tran_amt = end_tran_amt,"
            + "end_tran_amt   = 0,"
            + "mod_pgm        = ?,"
            + "mod_time       = sysdate";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE p_seqno       = ? "
            + "and   fund_code     = ? "
            + "and   end_tran_amt > 0 ";

  setString(1 , sysDate);
  setString(2 , "移除序號["+ tranSeqno +"]");
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("fund_code"));


  updateTable();
  return;
 }
// ************************************************************************
 int insertCycFundDtl() throws Exception
 {
  extendField = "fund.";
  setValue("fund.create_date"          , sysDate);
  setValue("fund.create_time"          , sysTime);
  setValue("fund.business_date"        , businessDate);
  setValue("fund.p_seqno"              , getValue("p_seqno"));
  setValue("fund.acct_type"            , getValue("acct_type"));
  setValue("fund.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("fund.execute_date"         , businessDate);
  setValue("fund.curr_code"            , "901");
  setValue("fund.fund_code"            , getValue("fund_code"));
  setValue("fund.tran_code"            , "0");
  setValue("fund.vouch_type"           , "O");
  setValue("fund.fund_amt"             , getValue("end_tran_amt"));
//setValue("fund.cd_kind"              , "A371");
  if (getValue("fund_code").substring(0,4).equals("T998"))//T998 cardlink轉檔現金回饋餘額轉入溢繳款 
      setValue("fund.cd_kind"              , "H006");  
  else
      setValue("fund.cd_kind"              , "H005");      //金庫幣,員工慶生活動費,TaiwanPay
  
  setValue("fund.memo1_type"           , "1");
  setValue("fund.src_pgm"              , javaProgram); 
  setValue("fund.proc_flag"            , "N");
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , javaProgram);

  daoTable  = "cyc_fund_dtl";

  insertTable();

  return(0);
 }
//************************************************************************
 int selectActAcctCurr() throws Exception
 {
  extendField = "curr.";
  selectSQL = "end_bal_op,"
            + "dc_end_bal_op,"
            + "acct_jrnl_bal,"
            + "dc_acct_jrnl_bal,"
            + "ttl_amt,"
            + "ttl_amt_bal,"
            + "dc_ttl_amt,"
            + "dc_ttl_amt_bal,"
            + "min_pay_bal,"
            + "dc_min_pay_bal,"
            + "rowid as rowid";
  daoTable  = "act_acct_curr";
  whereStr  = "where p_seqno   = ? "
            + "and   curr_code = '901' "
            ;

  setString(1 , getValue("p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) 
    {
     showLogMessage("I","","act_acct_curr p_seqno :["+ getValue("p_seqno") +"] not found error");
     comcr.errRtn("select_act_acct_curr not found!", "", "");
     return(1);
    }

  hCurrTtlAmtBal = getValueDouble("curr.ttl_amt_bal");
  hCurrDcTtlAmtBal = getValueDouble("curr.dc_ttl_amt_bal");
  hCurrMinPayBal = getValueDouble("curr.min_pay_bal");
  hCurrDcMinPayBal = getValueDouble("curr.dc_min_pay_bal");
  return(0);
 }

//************************************************************************
 public void lastCurrData() throws Exception
 {

  if ( getValueDouble("curr.dc_ttl_amt_bal") <= 0 )
     { return; }

  double tmpDcTotAmtPaid = 0;

  if ( getValueDouble("end_tran_amt") >= getValueDouble("curr.dc_ttl_amt_bal") )
     { tmpDcTotAmtPaid = getValueDouble("curr.dc_ttl_amt_bal"); } 
  else 
     { tmpDcTotAmtPaid = getValueDouble("end_tran_amt"); }


  hCurrDcTtlAmtBal = getValueDouble("curr.dc_ttl_amt_bal")
                           - tmpDcTotAmtPaid;

  hCurrDcTtlAmtBal =  convAmtDp2R(hCurrDcTtlAmtBal);
  hCurrTtlAmtBal = hCurrDcTtlAmtBal;

  /************************* 以下處理最低應繳餘額 ******************************/

  if ( getValueDouble("curr.dc_min_pay_bal") <= 0 )
     { return; }

  double tmpAcurDcMinPayBal = getValueDouble("curr.dc_min_pay_bal")
                                 - getValueDouble("end_tran_amt");
      
  tmpAcurDcMinPayBal = convAmtDp2R(tmpAcurDcMinPayBal);

  setValue("curr.dc_min_pay_bal",""+tmpAcurDcMinPayBal);

  if ( getValueDouble("curr.dc_min_pay_bal") >= hCurrDcTtlAmtBal)
     { setValue("curr.dc_min_pay_bal",  ""+ hCurrDcTtlAmtBal); }

  if ( getValueDouble("curr.dc_min_pay_bal") < 0 )
     { setValue("curr.dc_min_pay_bal","0"); }

  setValue("adcl.p_seqno", getValue("p_seqno"));
  setValue("adcl.curr_code", "901");
  selectActAcagCurr();
  deleteActAcag();

  hCurrDcMinPayBal =  getValueDouble("curr.dc_min_pay_bal");
  hCurrMinPayBal = hCurrDcMinPayBal;
   
  return;
 }

//************************************************************************
 public int selectActAcagCurr()  throws Exception
 {
    double tempDcDouble = getValueDouble("curr.dc_min_pay_bal");
    setValue("curr.min_pay_bal","0");

    daoTable    = "act_acag_curr";
    extendField = "aacr.";
    selectSQL   = "curr_code,"
                + "acct_month,"
                + "pay_amt,"
                + "dc_pay_amt,"
                + "rowid as rowid ";
    whereStr    = "WHERE   p_seqno   = ?"
                + " AND    curr_code = ?"
                + " ORDER  BY acct_month DESC";
    setString(1,getValue("adcl.p_seqno"));
    setString(2,getValue("adcl.curr_code"));
    int n = selectTable();

    for ( int i=0; i<n; i++)   {

         if ( getValueDouble("aacr.dc_pay_amt",i) == 0 || tempDcDouble == 0 )
            {
              deleteActAcagCurr(i);
              updateActAcag(i);
              continue;
            }

         if ( tempDcDouble >= getValueDouble("aacr.dc_pay_amt",i) )
            {
              tempDcDouble = tempDcDouble - getValueDouble("aacr.dc_pay_amt",i);
              setValueDouble("curr.min_pay_bal", getValueDouble("curr.min_pay_bal") + getValueDouble("aacr.pay_amt",i));
              continue;
            }

         double cvtData = tempDcDouble * (getValueDouble("aacr.pay_amt",i)/getValueDouble("aacr.dc_pay_amt",i));
         setValueDouble("aacr.pay_amt",comcr.commCurrAmt("901",cvtData,0),i);
         setValueDouble("curr.min_pay_bal", (getValueDouble("curr.min_pay_bal") + getValueDouble("aacr.pay_amt",i)));
         setValueDouble("aacr.dc_pay_amt",tempDcDouble,i);
         if ( getValue("adcl.curr_code").equals("901") )
            { setValue("aacr.pay_amt", getValue("aacr.dc_pay_amt",i),i); }
         if ( getValueDouble("aacr.dc_pay_amt",i) == 0 )
            { setValue("aacr.pay_amt","0",i); }
         tempDcDouble = 0;
         if ( getValueDouble("aacr.dc_pay_amt",i) <= 0  )
            { deleteActAcagCurr(i); }
         else
            { updateActAcagCurr(i); }
         updateActAcag(i);
       }

    return n;
 }

/******************* DELETE *************/
 public int deleteActAcag() throws Exception
 {
   daoTable = "act_acag";
   whereStr = "WHERE p_seqno = ? and pay_amt = 0";
   setString(1,getValue("adcl.p_seqno"));
   int n = deleteTable();

   return n;
 }

//************************************************************************
 public int deleteActAcagCurr(int i) throws Exception
 {
   daoTable = "act_acag_curr";
   whereStr = "WHERE rowid = ? ";
   setRowId(1,getValue("aacr.rowid",i));
   int n = deleteTable();

   return n;
 }

//************************************************************************
 public int updateActAcag(int i) throws Exception
 {
   extendField = "temp.";
   daoTable    = "act_acag_curr";
   selectSQL   = "sum(pay_amt) as  pay_amt ";
   whereStr    = "WHERE p_seqno    = ? "
               + "AND   acct_month = ?";
   setString(1,getValue("adcl.p_seqno"));
   setString(2,getValue("aacr.acct_month",i));
   selectTable();

   double tempAmount = getValueDouble("temp.pay_amt");

   daoTable  = "act_acag";
   updateSQL = "pay_amt = ? ";
   whereStr  = "WHERE p_seqno    = ? "
             + "AND   acct_month = ? ";

   setString(1,""+tempAmount);
   setString(2,getValue("adcl.p_seqno"));
   setString(3,getValue("aacr.acct_month",i));
   int n = updateTable();
   if ( n > 0 )
      { return n; }

   insertActAcag(i);

   return 0;
 }

//************************************************************************
 public int insertActAcag(int i) throws Exception
 {
   sqlCmd =  "insert into act_acag ("
          +  "p_seqno,"
          +  "seq_no,"
          +  "acct_type,"
          +  "acct_month,"
          +  "stmt_cycle,"
          +  "pay_amt,"
          +  "mod_time,"
          +  "mod_pgm )"
          + "select p_seqno,"
          + "max(0),"
          + "max(acct_type),"
          + "acct_month,"
          + "max(stmt_cycle),"
          + "sum(pay_amt),"
          + "max(sysdate),"
          + "max('ActE004') "
          + "FROM   act_acag_curr "
          + "WHERE  p_seqno    = ? "
          + "AND    acct_month = ? "
          + "GROUP BY p_seqno,acct_month ";
   setString(1,getValue("adcl.p_seqno"));
   setString(2,getValue("aacr.acct_month",i));
   int n = executeSqlCommand(sqlCmd);

   return n;
 }

//************************************************************************
 public int updateActAcagCurr(int i) throws Exception
 {
   daoTable  = "act_acag_curr";
   updateSQL = "pay_amt    = ?,"
             + "dc_pay_amt = ?";
   whereStr = "WHERE  rowid = ? ";
   setDouble(1,getValueDouble("aacr.pay_amt",i));
   setDouble(2,getValueDouble("aacr.dc_pay_amt",i));
   setRowId(3,getValue("aacr.rowid",i));
   int n = updateTable();
   if ( n == 0 )
      { showLogMessage("E","","update_act_acag_curr ERROR");  }

   return n;
 }

// ************************************************************************
 void updateActAcctCurr() throws Exception
 {
  dateTime();
  updateSQL = "end_bal_op       = ?, "
            + "dc_end_bal_op    = ?, "
            + "acct_jrnl_bal    = ?, "
            + "dc_acct_jrnl_bal = ?, "
            + "ttl_amt_bal      = ?, "
            + "dc_ttl_amt_bal   = ?, "
            + "min_pay_bal      = ?, "
            + "dc_min_pay_bal   = ?, "
            + "mod_pgm          = ?,"
            + "mod_time         = sysdate";
  daoTable  = "act_acct_curr";
  whereStr  = "WHERE rowid      = ? "
            ;

  setInt(1 , getValueInt("curr.end_bal_op") + getValueInt("end_tran_amt")); 
  setInt(2 , getValueInt("curr.dc_end_bal_op") + getValueInt("end_tran_amt")); 
  setInt(3 , getValueInt("curr.acct_jrnl_bal") - getValueInt("end_tran_amt")); 
  setInt(4 , getValueInt("curr.dc_acct_jrnl_bal") - getValueInt("end_tran_amt")); 
  setDouble(5 , hCurrTtlAmtBal);
  setDouble(6 , hCurrDcTtlAmtBal);
  setDouble(7 , hCurrMinPayBal);
  setDouble(8 , hCurrDcMinPayBal);
  setString(9 , javaProgram);
  setRowId(10 , getValue("curr.rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int selectActAcctCurr1() throws Exception
 {
  extendField = "acct.";
  selectSQL = "sum(end_bal_op) as end_bal_op,"
            + "sum(acct_jrnl_bal) as acct_jrnl_bal,"
            + "sum(ttl_amt_bal)   as ttl_amt_bal,"
            + "sum(min_pay_bal)   as min_pay_bal";
  daoTable  = "act_acct_curr";
  whereStr  = "where p_seqno   = ? "
            ;
  setString(1 , getValue("p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) 
    {
     showLogMessage("I","","act_acct p_seqno :["+ getValue("p_seqno") +"] not found error");
     return(1);
    }
  return(0);
 }
// ************************************************************************
 void updateActAcct() throws Exception
 {
  dateTime();
  updateSQL = "end_bal_op     = ?, "
            + "acct_jrnl_bal  = ?, "
            + "ttl_amt_bal    = ?, "
            + "min_pay_bal    = ?, "
            + "mod_pgm        = ?,"
            + "mod_time       = sysdate";
  daoTable  = "act_acct";
  whereStr  = "WHERE p_seqno  = ? "
            ;

  setInt(1 , getValueInt("acct.end_bal_op")); 
  setInt(2 , getValueInt("acct.acct_jrnl_bal")); 
  setInt(3 , getValueInt("acct.ttl_amt_bal")); 
  setInt(4 , getValueInt("acct.min_pay_bal")); 
  setString(5 , javaProgram);
  setString(6 , getValue("p_seqno"));

  updateTable();
  return;
 }
// ************************************************************************
 int insertActJrnl() throws Exception
 {
  dateTime();
  extendField = "jrnl.";
  setValueInt("jrnl.enq_seqno"             , getValueInt("jrnl.enq_seqno")+1);
  setValue("jrnl.p_seqno"                  , getValue("p_seqno"));
  setValue("jrnl.curr_code"                , "901");
  setValue("jrnl.acct_type"                , getValue("acct_type"));
  setValue("jrnl.acct_date"                , businessDate);
  setValue("jrnl.id_p_seqno"               , getValue("id_p_seqno"));
  setValue("jrnl.corp_p_seqno"             , "");
  setValue("jrnl.tran_type"                , getValue("fund_code").substring(0,4));
  setValue("jrnl.tran_class"               , "P");
  setValue("jrnl.acct_code"                , "PY");
  setValue("jrnl.dr_cr"                    , "D");
  setValue("jrnl.transaction_amt"          , getValue("end_tran_amt"));
  setValue("jrnl.dc_transaction_amt"       , getValue("end_tran_amt")); 
  setValueDouble("jrnl.jrnl_bal"           , getValueInt("curr.acct_jrnl_bal") - getValueInt("end_tran_amt"));
  setValue("jrnl.dc_jrnl_bal"              , getValue("jrnl.jrnl_bal"));
  setValue("jrnl.item_d_bal"               , "0"); 
  setValue("jrnl.dc_item_d_bal"            , "0"); 
  setValue("jrnl.can_by_fund_bal"          , "0"); 
  setValue("jrnl.dc_can_by_fund_bal"       , "0");
  setValue("jrnl.interest_date"            , businessDate);
  setValue("jrnl.item_date"                , "");
  setValue("jrnl.reference_no"             , "");
  setValue("jrnl.item_bal"                 , "0");
  setValue("jrnl.dc_item_bal"              , "0");   
  setValue("jrnl.order_seq"                , "1");
  setValue("jrnl.batch_no"                 , tranSeqno); 
  setValue("jrnl.jrnl_seqno"               , comr.getSeqno("ECS_JRNLSEQ")); 
  setValue("jrnl.stmt_cycle"               , getValue("wday.stmt_cycle"));
  setValue("jrnl.crt_date"                 , sysDate);
  setValue("jrnl.crt_time"                 , sysTime);
  setValue("jrnl.mod_user"                 , "SYSTEM");
  setValue("jrnl.mod_time"                 , sysDate+sysTime);
  setValue("jrnl.mod_pgm"                  , javaProgram);

  daoTable  = "act_jrnl";
  insertTable();

  return(0);
 }
// ************************************************************************
 int selectMktLoanParm() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "mkt_loan_parm";
  whereStr  = "where move_cond = 'Y' "
            + "order by apr_date desc "
            ;

  parmCnt = selectTable();

  if (parmCnt==0) 
    {
     showLogMessage("I","","select mkt_loan_parm error ");
     return(1);
    }
//for (int inti=0;inti<parmCnt;inti++)
//  {
//   showLogMessage("I","","活動代號:["+ getValue("parm.fund_code",inti) +"]-"+getValue("parm.fund_name",inti));
//  }  
  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);  
 }
// ************************************************************************
 void loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno";
//daoTable  = "act_acno_"+getValue("wday.stmt_cycle");
  daoTable  = "act_acno";
  whereStr  = "where acno_p_seqno = p_seqno "
            + "and stmt_cycle = ? ";

  setString(1,getValue("wday.stmt_cycle"));
  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load act_acno p_seqno for cycle "+getValue("wday.stmt_cycle")+" Count: ["+n+"]");
 }
// ************************************************************************
 void loadMktParmData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_parm_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_LOAN_PARM' "
            + "and data_type in ('1','2') "
            + "order by data_key,data_type,data_code,data_code2"
            ;

  int  n = loadTable();
  setLoadData("data.data_key,data.data_type");
  setLoadData("data.data_key,data.data_type,data.data_code");

  showLogMessage("I","","Load mkt_parm_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktParmData(String col1, String sel, String dataType, int dataNum) throws Exception
 {
  return selectMktParmData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1, String col2, String sel, String dataType, int dataNum) throws Exception
 {
  return selectMktParmData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1, String col2, String col3, String sel, String dataType, int dataNum) throws Exception
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
 int selectCrdCard(int intm, String cancelEvent) throws Exception
 {
  if (cancelEvent.equals("1")) return(0);
  extendField = "card.";
  selectSQL = "group_code";
  daoTable  = "crd_card";
  whereStr  = "where current_code = '0' "
            + "and   p_seqno      = ? "
            + "group by group_code "
            ;

  setString(1 , getValue("p_seqno"));
  int recCnt = selectTable();

  if ((recCnt>0)&&(cancelEvent.equals("2"))) return(0);

  for (int inti=0;inti<recCnt;inti++)
    {
     if (selectMktParmData(getValue("card.group_code",inti),"",
                              getValue("parm.group_code_sel",intm),"2",2)!=0) continue;
     return(0);
    } 
  return(1);
 }
// ************************************************************************
 void insertCycPyaj() throws Exception
 {
  extendField = "pyaj.";

  setValue("pyaj.p_seqno"              , getValue("p_seqno"));
  setValue("pyaj.acct_type"            , getValue("acct_type"));
  setValue("pyaj.class_code"           , "P");
  setValue("pyaj.payment_date"         , businessDate);
  setValue("pyaj.payment_amt"          , getValue("end_tran_amt")); 
  setValue("pyaj.curr_code"            , "901");
  setValue("pyaj.dc_payment_amt"       , getValue("end_tran_amt"));
  setValue("pyaj.payment_type"         , getValue("fund_code").substring(0,4)); 
  setValue("pyaj.fund_Code"            , getValue("fund_code")); 
  setValue("pyaj.fee_flag"             , "N");
  setValue("pyaj.stmt_cycle"           , getValue("wday.stmt_cycle"));
  setValue("pyaj.settle_flag"          , "U");
  setValue("pyaj.reference_no"         , "");
  setValue("pyaj.crt_date"             , sysDate);
  setValue("pyaj.crt_user"             , javaProgram);
  setValue("pyaj.mod_pgm"              , javaProgram);
  setValue("pyaj.mod_time"             , sysDate+sysTime);

  daoTable    = "cyc_pyaj";
  insertTable();
  return;
}
// ************************************************************************
 /*** convAmtDp2R(x) 有以下兩點作用：
  1.校正微小誤差：double 變數運算後會發生 .99999999...的問題，例如 19.125, 
    實際會變成 19.1249999999999999...，所以執行 conv_amt_dp2r(x)變成 19.13
  2.四捨五入到小數以下第二位
 ***/
 public double convAmtDp2R(double cvtAmt) throws Exception
 {
   long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.00001);
   double cvtDouble =  ((double) cvtLong) / 100;
   return cvtDouble;
 }

}  // End of class FetchSample
