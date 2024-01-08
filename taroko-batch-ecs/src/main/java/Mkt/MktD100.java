/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/01/11  V1.00.15  Allen Ho   mkt_d100                                   *
 * 110/10/06  V1.01.01  Allen Ho   393 not round error                        *
 * 111/11/11  V1.01.02  jiangyigndong  updated for project coding standard    *
 * 112/10/04  V1.01.03  Holmes     adjust 標準分錄代號,外幣不起帳務           *
 *                                 mark //selectCycFundDtl1()  not to exec.   *
 *                                 mark //selectCycFundDtl3()  not to exec.   *
 *                                 mark //selectCycFundDtl4()  not to exec.   *    
 *                                 mark //selectCycFundDtl0()  not to exec.   *  
 *                                 change selectCycFundDtl2()-->selectCycFundDtl2A()* 
 *                                 add    selectCycFundDtl2B() 退貨加檔入帳   * 
 * 112/11/17  V1.01.04  Holmes     change cd_kind = 'A371' as 'H005'          *    
 * 112/12/19  V1.01.05  Holmes     add H006 轉檔基金轉溢繳                    *      
 *                                 add J003 刷卡金存入VD金融帳戶              *                                          
 ******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktD100 extends AccessDAO
{
 private final String PROGNAME = "現金回饋-基金會計分錄處理程式 112/12/19 V1.01.05";
 CommFunction comm = new CommFunction();
 CommCrdRoutine comcr = null;

 String business_date   = "";
 double[] vouchAmt = new double[30];

 long    totalCnt=0,updateCnt=0;
 int inti;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktD100 proc = new MktD100();
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
   { business_date = args[0]; }

   if ( !connectDataBase() ) exitProgram(1);
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   selectPtrBusinday();
//   showLogMessage("I","","=========================================");
//   showLogMessage("I","","基金產生會計分錄(A-36)開始.......");
//   selectCycFundDtl1();
//   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");
//   showLogMessage("I","","基金銷帳會計分錄(A372)開始.......");
   showLogMessage("I","","基金銷帳會計入帳分錄(H001)開始.............");   
   selectCycFundDtl2A();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");
   showLogMessage("I","","基金退貨加檔入帳會計分錄(H003)開始.........");   
   selectCycFundDtl2B();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
 showLogMessage("I","","=============================================");   
//   showLogMessage("I","","基金移除正項會計分錄(A393)開始.......");
//   selectCycFundDtl3();
//   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
//   showLogMessage("I","","=========================================");
//   showLogMessage("I","","基金移除負項會計分錄(A394)開始.......");
//   selectCycFundDtl4();
//   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
//   showLogMessage("I","","=========================================");
   showLogMessage("I","","基金轉溢繳會計分錄(H005)開始...............");
   selectCycFundDtl6();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");
//   showLogMessage("I","","專案基金指定會計分錄開始.......");
//   selectCycFundDtl0();
//   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
//   showLogMessage("I","","=========================================");
   showLogMessage("I","","轉檔基金轉溢繳會計分錄(H006)開始...........");
   selectCycFundDtl7();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");   
   showLogMessage("I","","刷卡金存入VD客人帳戶會計分錄(J003)開始.....");
   selectCycFundDtl8();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");  
   showLogMessage("I","","信用卡兌換刷卡金(扣點)會計分錄(J005)開始...");
   selectCycFundDtl9();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");  
   showLogMessage("I","","VD兌換刷卡金(扣點)會計分錄(J004)開始.......");
   selectCycFundDtl10();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");    
   showLogMessage("I","","紅利線上折抵會計分錄(J010)開始.............");
   selectCycFundDtl11();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");      
   showLogMessage("I","","紅利線上折抵沖回會計分錄(J008)開始.........");
   selectCycFundDtl12();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");   
   showLogMessage("I","","失效卡點數移會計分錄(J009)開始.............");
   selectCycFundDtl13();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");   
   showLogMessage("I","","到期點數移除會計分錄(J011)開始.............");
   selectCycFundDtl14();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===========================================");   
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

  if (business_date.length()==0)
   business_date   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+business_date+"]");
 }
 // ************************************************************************
 void selectCycFundDtl1() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag= 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'A-36' "
          + "GROUP BY substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
          + "having sum(fund_amt) != 0 "
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;
   vouchAmt[1]   = getValueDouble("fund_amt");
   selectPtrCurrcode();
   insertVoucherRtn(1,getValue("curr_code_gl"));
   updateCycFundDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void selectCycFundDtl2A() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag= 'N' "
//          + "AND   (business_Date  = ? "
//          + " or    create_date    = ? ) "
          + "AND   (business_Date  <= ? "
          + " or    create_date    <= ? ) "          
//          + "AND   cd_kind = 'A372'  "
          + "AND   cd_kind = 'H001'  "          
          + "GROUP BY substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
          + "having sum(fund_amt) != 0 "
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;
   if (getValueInt("fund_amt")!=0)
   {
    for (int int1=0;int1<=23;int1++) vouchAmt[int1]=0;
    vouchAmt[1]   = getValueDouble("fund_amt");
    vouchAmt[2]   = getValueDouble("fund_amt");
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(2,getValue("curr_code_gl"));
    }
   }
   updateCycFundDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void selectCycFundDtl2B() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag= 'N' "
          + "AND   (business_Date  <= ? "
          + " or    create_date    <= ? ) "          
          + "AND   cd_kind = 'H003'  "          
          + "GROUP BY substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
          + "having sum(fund_amt) != 0 "
  ;
  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();
  totalCnt=0;
  while( fetchTable() )
  {
   totalCnt++;
   if (getValueInt("fund_amt")!=0)
   {
    for (int int1=0;int1<=23;int1++) vouchAmt[int1]=0;
    vouchAmt[1]   = getValueDouble("fund_amt");
    vouchAmt[2]   = getValueDouble("fund_amt");
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(20,getValue("curr_code_gl"));
    }    
   }
   updateCycFundDtl(getValue("cd_kind"));
  }//while
  
  closeCursor();
  return;
 } 
 // ************************************************************************
 void selectCycFundDtl3() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND  ( cd_kind = 'A393' OR cd_kind = 'A394' )  "
          + "GROUP BY substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;
   if (getValueInt("fund_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("fund_amt");
    vouchAmt[2]   = getValueDouble("fund_amt");
    selectPtrCurrcode();
    insertVoucherRtn(3,getValue("curr_code_gl"));
   }
   updateCycFundDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void selectCycFundDtl4() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'A394' "
          + "GROUP BY substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;
   if (getValueInt("fund_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("fund_amt");
    vouchAmt[2]   = getValueDouble("fund_amt");    
    selectPtrCurrcode();
    insertVoucherRtn(4,getValue("curr_code_gl"));
   }
   updateCycFundDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void selectCycFundDtl6() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
//        + "AND   cd_kind = 'A371' "
          + "AND   cd_kind = 'H005' "          
          + "GROUP BY substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("fund_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("fund_amt");
    vouchAmt[2]   = getValueDouble("fund_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(6,getValue("curr_code_gl"));
    }     
    
   }
   updateCycFundDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }
 
 // ************************************************************************
 void selectCycFundDtl7() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'H006' "          
          + "GROUP BY substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("fund_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("fund_amt");
    vouchAmt[2]   = getValueDouble("fund_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(7,getValue("curr_code_gl"));
    }     
    
   }
   updateCycFundDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }
 // ************************************************************************
// void selectCycFundDtl0() throws Exception
// {
//  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
//          + "a.vouch_type,"
//          + "a.cd_kind as cd_kind,"
//          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
//          + "sum(abs(fund_amt)) as fund_amt,"
//          + "min(a.memo1_type) as memo1_type,"
//          + "min(a.mod_pgm) as mod_pgm";
//  daoTable  = "cyc_fund_dtl a";
//  whereStr  = "WHERE a.proc_flag = 'N' "
//          + "AND   (business_Date  = ? "
//          + " or    create_date    = ? ) "
//          + "AND   cd_kind not in ('A-05','A-36','A372','A393','A394','A371') "
//          + "GROUP BY a.cd_kind,substr(a.fund_code,1,4),a.vouch_type,decode(a.curr_code,'','901',curr_code) "
//  ;
//
//  setString(1 , business_date);
//  setString(2 , business_date);
//  openCursor();
//
//  totalCnt=0;
//
//  while( fetchTable() )
//  {
//   totalCnt++;
//   if (getValueInt("fund_amt")!=0)
//   {
//    vouchAmt[1]   = getValueDouble("fund_amt");
//    vouchAmt[2]   = getValueDouble("fund_amt");    
//    selectPtrCurrcode();
//    insertVoucherRtn(0,getValue("curr_code_gl"));
//   }
//   updateCycFundDtl(getValue("cd_kind"));
//  }
//  closeCursor();
//  return;
// }
 
 // ************************************************************************  
 void selectCycFundDtl8() throws Exception
 {
  selectSQL = "substr(a.fund_code,1,4) as fund_code,"
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code,"
          + "sum(abs(fund_amt)) as fund_amt,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_fund_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'J003' "          
          + "GROUP BY substr(a.fund_code,1,4) , a.vouch_type,decode(a.curr_code,'','901',curr_code) "
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("fund_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("fund_amt");
    vouchAmt[2]   = getValueDouble("fund_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(8,getValue("curr_code_gl"));
    }     
    
   }
   updateCycFundDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }

 // ************************************************************************
 void selectCycFundDtl9() throws Exception
 {
  selectSQL = "min(a.active_code) , "
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code ,"
          + "sum(abs(bonus_amt)) as bonus_amt ,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_bonusac_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'J005' "          
          + "GROUP BY a.vouch_type ,decode(a.curr_code,'','901',curr_code) "          
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("bonus_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("bonus_amt");
    vouchAmt[2]   = getValueDouble("bonus_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(9,getValue("curr_code_gl"));
    }     
    
   }
   updateCycBonusAcDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void selectCycFundDtl10() throws Exception
 {
  selectSQL = "min(a.active_code) , "
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code ,"
          + "sum(abs(bonus_amt)) as bonus_amt ,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_bonusac_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'J004' "          
          + "GROUP BY a.vouch_type ,decode(a.curr_code,'','901',curr_code) "          
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("bonus_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("bonus_amt");
    vouchAmt[2]   = getValueDouble("bonus_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(10,getValue("curr_code_gl"));
    }     
    
   }
   updateCycBonusAcDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 } 
 // ************************************************************************
 void selectCycFundDtl11() throws Exception
 {
  selectSQL = "min(a.active_code) , "
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code ,"
          + "sum(abs(bonus_amt)) as bonus_amt ,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_bonusac_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'J010' "          
          + "GROUP BY a.vouch_type ,decode(a.curr_code,'','901',curr_code) "          
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("bonus_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("bonus_amt");
    vouchAmt[2]   = getValueDouble("bonus_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(11,getValue("curr_code_gl"));
    }     
    
   }
   updateCycBonusAcDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 } 
 
 // ************************************************************************
 void selectCycFundDtl12() throws Exception
 {
  selectSQL = "min(a.active_code) , "
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code ,"
          + "sum(abs(bonus_amt)) as bonus_amt ,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_bonusac_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'J008' "          
          + "GROUP BY a.vouch_type ,decode(a.curr_code,'','901',curr_code) "          
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("bonus_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("bonus_amt");
    vouchAmt[2]   = getValueDouble("bonus_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(12,getValue("curr_code_gl"));
    }     
    
   }
   updateCycBonusAcDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 } 
 // ************************************************************************
 void selectCycFundDtl13() throws Exception
 {
  selectSQL = "min(a.active_code) , "
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code ,"
          + "sum(abs(bonus_amt)) as bonus_amt ,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_bonusac_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'J009' "          
          + "GROUP BY a.vouch_type ,decode(a.curr_code,'','901',curr_code) "          
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("bonus_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("bonus_amt");
    vouchAmt[2]   = getValueDouble("bonus_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(13,getValue("curr_code_gl"));
    }     
    
   }
   updateCycBonusAcDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 } 
 // ************************************************************************
 void selectCycFundDtl14() throws Exception
 {
  selectSQL = "min(a.active_code) , "
          + "a.vouch_type,"
          + "decode(a.curr_code,'','901',curr_code) as curr_code ,"
          + "sum(abs(bonus_amt)) as bonus_amt ,"
          + "min(a.cd_kind) as cd_kind,"
          + "min(a.memo1_type) as memo1_type,"
          + "min(a.mod_pgm) as mod_pgm";
  daoTable  = "cyc_bonusac_dtl a";
  whereStr  = "WHERE a.proc_flag = 'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "AND   cd_kind = 'J011' "          
          + "GROUP BY a.vouch_type ,decode(a.curr_code,'','901',curr_code) "          
  ;

  setString(1 , business_date);
  setString(2 , business_date);
  openCursor();

  totalCnt=0;

  while( fetchTable() )
  {
   totalCnt++;

   if (getValueInt("bonus_amt")!=0)
   {
    vouchAmt[1]   = getValueDouble("bonus_amt");
    vouchAmt[2]   = getValueDouble("bonus_amt");    
    selectPtrCurrcode();
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(14,getValue("curr_code_gl"));
    }     
    
   }
   updateCycBonusAcDtl(getValue("cd_kind"));
  }
  closeCursor();
  return;
 }  
 // ************************************************************************
 void updateCycFundDtl(String cd_kind) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag    = 'Y', "
          + "proc_date    = ?, "
          + "execute_date = ?";
  daoTable  = "cyc_fund_dtl";
  whereStr  = "WHERE fund_code like ? "
          + "and   proc_flag =   'N' "
          + "AND   (business_Date  = ? "
          + " or    create_date    = ? ) "
          + "and   cd_kind         = ? "
  ;

  setString(1 , sysDate);
  setString(2 , business_date);
  setString(3 , getValue("fund_code")+"%");
  setString(4 , business_date);
  setString(5 , business_date);
  setString(6 , cd_kind);

  updateTable();
  return;
 } 
 // ************************************************************************
 void updateCycBonusAcDtl(String cd_kind) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag    = 'Y', "
          + "proc_date    = ? , "
          + "execute_date = ?";
  daoTable  = "cyc_bonusac_dtl";
  whereStr = "WHERE proc_flag =   'N' "
           + "AND   (business_Date  = ? "
           + " or    create_date    = ? ) "
           + "and   cd_kind         = ? "
  ;

  setString(1 , sysDate);
  setString(2 , business_date);
  setString(3 , business_date);
  setString(4 , business_date);
  setString(5 , cd_kind);

  updateTable();
  return;
 }
 // ************************************************************************
 void insertVoucherRtn(int index, String idxCurr) throws Exception
 {
  String hVouchCdKind = "";
  double callVoucherAmt = 0;
  int  vouchCnt =0;

  comcr.hGsvhCurr    = idxCurr;
  comcr.hGsvhModPgm = javaProgram;

  switch (index)
  {
//   case 0: /* 專案基金指定 */
//    hVouchCdKind = getValue("cd_kind");
//    comcr.startVouch("1", hVouchCdKind);
//    break;
//   case 1:/* 基金產生 */
//    hVouchCdKind = "A-36";
//    comcr.startVouch("1", hVouchCdKind);
//    break;
   case 2:/* 基金銷帳 */
//    hVouchCdKind = "A372";
    hVouchCdKind = "H001";   
    comcr.startVouch("1", hVouchCdKind);
    break;
   case 20:/* 現金回饋沖銷 */
	    hVouchCdKind = "H003";   
	    comcr.startVouch("1", hVouchCdKind);
	    break;    
//   case 3:/* 基金正項移除 */
//    hVouchCdKind = "A393";
//    comcr.startVouch("1", hVouchCdKind);
//    break;
//   case 4:/* 基金負項移除 */
//    hVouchCdKind = "A394";
//    comcr.startVouch("1", hVouchCdKind);
//    break;
//   case 5:/* 不入基金轉溢繳 */
//    hVouchCdKind = "A-05";
//    comcr.startVouch("1", hVouchCdKind);
//    break;
   case 6:/* 基金轉溢付款 */
//  hVouchCdKind = "A371";
    hVouchCdKind = "H005";    
    comcr.startVouch("1", hVouchCdKind);
    break;
   case 7:/* 轉檔基金轉溢付款 */
	    hVouchCdKind = "H006";    
	    comcr.startVouch("1", hVouchCdKind);
	    break;  
   case 8:/* 刷卡金存入VD客人帳戶 */
	    hVouchCdKind = "J003";    
	    comcr.startVouch("1", hVouchCdKind);
	    break;  
   case 9:/* 信用卡兌換刷卡金(扣點) */
	    hVouchCdKind = "J005";    
	    comcr.startVouch("1", hVouchCdKind);
	    break; 
   case 10:/* VD兌換刷卡金(扣點) */
	    hVouchCdKind = "J004";    
	    comcr.startVouch("1", hVouchCdKind);
	    break; 	 
   case 11:/* 紅利線上折抵 */
	    hVouchCdKind = "J010";    
	    comcr.startVouch("1", hVouchCdKind);
	    break; 
   case 12:/* 紅利線上折抵沖回 */
	    hVouchCdKind = "J008";    
	    comcr.startVouch("1", hVouchCdKind);
	    break; 	  
   case 13:/* 失效卡點數移除 */
	    hVouchCdKind = "J009";    
	    comcr.startVouch("1", hVouchCdKind);
	    break; 		
   case 14:/* 到期點數移除 */
	    hVouchCdKind = "J011";    
	    comcr.startVouch("1", hVouchCdKind);
	    break; 		    
  }

  selectSQL = "a.ac_no,"
          + "a.dbcr_seq,"
          + "a.dbcr,"
          + "b.memo3_kind,"
          + "decode(b.memo3_flag,'','N',b.memo3_flag) as memo3_flag,"
          + "decode(b.dr_flag,'','N',b.dr_flag) as dr_flag,"
          + "decode(b.cr_flag,'','N',b.cr_flag) as cr_flag";
  daoTable  = "gen_sys_vouch a,gen_acct_m b";
  whereStr  = "where std_vouch_cd        = ? "
          + "and   a.ac_no = b.ac_no "
          + "order by a.dbcr_seq,decode(dbcr,'D','A',dbcr) "
  ;

  setString(1 , hVouchCdKind);

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
  {
   comcr.hGsvhMemo1   = getValue("fund_code");
   switch (index){
     case 2:/* 基金銷帳 */
   	      comcr.hGsvhMemo1   = "現金回饋銷帳";
          break;
     case 20:	
    	  comcr.hGsvhMemo1   = "現金回饋沖銷";
          break;
     case 6: /* 基金轉溢繳 */ 
    	  comcr.hGsvhMemo1   = "現金回饋轉溢付款";
          break;   
     case 7: /* 基金轉溢繳 */ 
   	  comcr.hGsvhMemo1   = "現金回饋轉溢付款(轉檔)";
         break;    
     case 8: /* 刷卡金存入VD客人帳戶 */ 
      	  comcr.hGsvhMemo1   = "刷卡金存入VD客人帳戶";
            break;    
     case 9: /* 信用卡兌換刷卡金(扣點) */ 
     	  comcr.hGsvhMemo1   = "信用卡兌換刷卡金(扣點)";
           break;  
     case 10: /* VD兌換刷卡金(扣點) */ 
    	  comcr.hGsvhMemo1   = "VD兌換刷卡金(扣點)";
          break;
     case 11: /* 紅利線上折抵 */ 
    	  comcr.hGsvhMemo1   = "紅利線上折抵";
          break;  
    case 12: /* 紅利線上折抵沖回 */ 
   	  comcr.hGsvhMemo1   = "紅利線上折抵沖回";
         break; 
    case 13: /* 失效卡點數移除 */ 
     	  comcr.hGsvhMemo1   = "失效卡點數移除";
           break;  
    case 14: /* 到期點數移除 */ 
   	  comcr.hGsvhMemo1   = "到期點數移除";
         break;            
   }	   

//   if (getValue("memo1_type").equals("1"))
//    comcr.hGsvhMemo1 = String.format("%-4.4s",getValue("fund_code"));
//   if (getValue("memo3_flag",inti).equals("Y"))
//   {
//    if (((getValue("dbcr",inti).equals("D")))&&(getValue("cr_flag",inti).equals("Y"))||
//            ((getValue("dbcr",inti).equals("C")))&&(getValue("dr_flag",inti).equals("Y")))
//     vouchCnt++;
//   }

   comcr.hVoucSysRem = hVouchCdKind;
   comcr.hGsvhMemo3 = "";
/*
     comcr.h_gsvh_memo3 = comm.toChinDate(getValue("vouch_date")).substring(1,7)
                        + comcr.h_vouc_refno
                        + String.format("%02d", vouch_cnt);
*/

// callVoucherAmt = vouchAmt[1];
   callVoucherAmt = vouchAmt[getValueInt("dbcr_seq",inti)]; 

   if(callVoucherAmt != 0)
   {
    if (comcr.detailVouch(getValue("ac_no",inti),getValueInt("dbcr_seq",inti)
            ,callVoucherAmt,idxCurr)!=0)
    {
     showLogMessage("I","","call detail_vouch error");
     exitProgram(1);
    }
   }
  }
 }
 // ************************************************************************
 void selectPtrCurrcode() throws Exception
 {
  selectSQL = "curr_code_gl";
  daoTable  = "ptr_currcode";
  whereStr  = "WHERE curr_code = ? ";

  setString(1, getValue("curr_code"));
  int recCnt = selectTable();
 }
// ************************************************************************

}  // End of class FetchSample

