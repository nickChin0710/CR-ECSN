/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/11/20  V1.00.16  Allen Ho   cyc_C300                                   *
* 111/11/11  V1.00.02  Yang Bo    sync code from mega                        *
* 112/10/02  V1.00.03  Holmes     adjust 標準分錄代號,外幣不起帳務           *
*                                 mark //selectCycVouchData1() not to exec.  *
*                                 mark //selectCycVouchData3() not to exec.  *
* 112/11/17  V1.00.04  Holmes     撥入溢付款帳務調整                         *                                 
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycC300 extends AccessDAO
{
 private final String PROGNAME = "現金回饋-基金會計分錄處理程式 112/11/17  V1.00.04";
 CommFunction comm = new CommFunction();
 CommCrdRoutine comcr = null;

 String hBusiBusinessDate = "";
 double[] vouchAmt = new double[30];

 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycC300 proc = new CycC300();
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
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());


   selectPtrBusinday();
//   showLogMessage("I","","=========================================");
//   showLogMessage("I","","基金產生會計分錄開始.......");
//   selectCycVouchData1();
//   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","現金回饋銷帳入帳會計分錄開始.............");
   loadPtrCurrcode();
   selectCycVouchData2();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");  
   showLogMessage("I","","=========================================");
   showLogMessage("I","","現金回饋銷帳入帳會計分錄(有預繳).........");
   selectCycVouchData2B();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
//   showLogMessage("I","","基金移除會計分錄開始...................");
//   selectCycVouchData3();
//   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
//   showLogMessage("I","","=========================================");
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void selectPtrBusinday() throws Exception
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
 public void selectCycVouchData1() throws Exception
 {
  selectSQL = "curr_code,"
            + "payment_type,"
            + "sum(vouch_amt) as vouch_amt";
  daoTable  = "cyc_vouch_data";
  whereStr  = "WHERE  vouch_data_type = '1' "
            + "AND    proc_flag = 'N' "
            + "GROUP BY curr_code,payment_type "
            ;

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    vouchAmt[1]   = getValueDouble("vouch_amt");
    vouchAmt[2]   = getValueDouble("vouch_amt");

    selectPtrCurrcode();
    if (selectMktLoanParmAdd()!=0)
       insertVoucherRtn(1,getValue("curr_code_gl"));
    else
       insertVoucherRtn(0,getValue("curr_code_gl"));
    updateCycVouchData("1");
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void selectCycVouchData2() throws Exception
 {
  selectSQL = "curr_code ,"
            + "payment_type,"
            + "sum(vouch_amt) as vouch_amt"
            ;
  daoTable  = "cyc_vouch_data";
  whereStr  = "WHERE  vouch_data_type in ('4') "
            + "AND    proc_flag = 'N' "
            + "GROUP BY curr_code,payment_type "            ;

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

//    selectCycVouchDataC();
    
    vouchAmt[1]   = getValueDouble("vouch_amt");
    vouchAmt[2]   = getValueDouble("vouch_amt");
    
    selectPtrCurrcode();    
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(2,getValue("curr_code_gl"));
    }
    
    updateCycVouchData("4");
/*
     double check_amt=0;
     for (int int1=2;int1<=25;int1++)
         {
          check_amt = check_amt + vouch_amt[int1];
//        showLogMessage("I","","STEP 0 check_amt ["+int1+"]["+ check_amt  +"]");
         }

      if (check_amt!=vouch_amt[1]) 
         {
          showLogMessage("I","","STEP 1 payment_type ["+ getValue("payment_type")  +"]");
          showLogMessage("I","","STEP 2 curr_code    ["+ getValue("curr_code")  +"]");
          showLogMessage("I","","STEP 3 comp_amt     ["+ check_amt  +"][" + vouch_amt[1] +"]");
         for (int int1=1;int1<=25;int1++)
             showLogMessage("I","","STEP 5 vouch_amt["+int1+"] = ["+ vouch_amt[int1]  +"]");
          break;
         }
*/
   } 
  closeCursor();
  return;
 }
 
//************************************************************************
 void selectCycVouchData2B() throws Exception
 {
  selectSQL = "curr_code ,"
            + "payment_type,"
            + "sum(vouch_amt) as vouch_amt"
            ;
  daoTable  = "cyc_vouch_data";
  whereStr  = "WHERE  vouch_data_type in ('C') "
            + "AND    proc_flag = 'N' "
            + "GROUP BY curr_code,payment_type "            ;

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

//    selectCycVouchDataC();
    vouchAmt[1]   = getValueDouble("vouch_amt");
    vouchAmt[2]   = getValueDouble("vouch_amt");    

    selectPtrCurrcode();
    
    //外幣不起帳務
    if (getValue("curr_code").equals("901")){
        insertVoucherRtn(20,getValue("curr_code_gl"));
    }
    
    updateCycVouchData("C");
   } 
  closeCursor();
  return;
 } 
 
// ************************************************************************
void selectCycVouchDataC() throws Exception 
 {
  extendField = "canb.";
  selectSQL = "sum(vouch_amt) as vouch_amt,"
            + "sum(decode(vouch_data_type,'C',vouch_amt,0)) as c_vouch_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'BL',vouch_amt,0))) as bl_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'CA',vouch_amt,0))) as ca_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'IT',vouch_amt,0))) as it_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'ID',vouch_amt,0))) as id_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'AO',vouch_amt,0))) as ao_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'LF',vouch_amt,0))) as lf_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'RI',vouch_amt,0))) as ri_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'PN',vouch_amt,0))) as pn_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'AF',vouch_amt,0))) as af_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'CF',vouch_amt,0))) as cf_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'PF',vouch_amt,0))) as pf_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'SF',vouch_amt,0))) as sf_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'DB',vouch_amt,0))) as db_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'CB',vouch_amt,0))) as cb_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'CI',vouch_amt,0))) as ci_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'CC',vouch_amt,0))) as cc_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'AI',vouch_amt,0))) as ai_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'OT',vouch_amt,0))) as ot_amt,"
            + "sum(decode(vouch_data_type,'C',0,decode(acct_code,'OF',vouch_amt,0))) as of_amt";
  daoTable  = "cyc_vouch_data";
  whereStr  = "WHERE  vouch_data_type in ('4','C') "
            + "AND    proc_flag = 'N' "
            + "AND   curr_code       = ? "
            + "AND   payment_type    = ? "
            ;
            ;

  setString(1,getValue("curr_code"));
  setString(2,getValue("payment_type"));

  int recCnt = selectTable();

  for (int int1=1;int1<=25;int1++) vouchAmt[int1]=0;

  vouchAmt[1]    = getValueDouble("canb.vouch_amt");

//        showLogMessage("I","","STEP 1 vouch_amt    ["+ getValueDouble("canb.vouch_amt") +"]");
//        showLogMessage("I","","STEP 2 couch_amt    ["+ getValueDouble("canb.c_vouch_amt") +"]");
//        showLogMessage("I","","STEP 3.1 vouch_amt_1  ["+ vouch_amt[1] +"]");

  vouchAmt[2]    = getValueDouble("canb.bl_amt");
  vouchAmt[3]    = getValueDouble("canb.ca_amt");
  vouchAmt[4]    = getValueDouble("canb.it_amt");
  vouchAmt[5]    = getValueDouble("canb.id_amt");
  vouchAmt[6]    = getValueDouble("canb.ao_amt");
  vouchAmt[7]    = getValueDouble("canb.lf_amt");
  vouchAmt[8]    = getValueDouble("canb.ri_amt");
  vouchAmt[9]    = getValueDouble("canb.pn_amt");
  vouchAmt[16]   = getValueDouble("canb.sf_amt");
  vouchAmt[17]   = getValueDouble("canb.db_amt");
  vouchAmt[18]   = getValueDouble("canb.cb_amt");
  vouchAmt[19]   = getValueDouble("canb.ci_amt");
  vouchAmt[20]   = getValueDouble("canb.cc_amt");
  vouchAmt[21]   = getValueDouble("canb.ai_amt");
  vouchAmt[22]   = getValueDouble("canb.ot_amt");
  vouchAmt[23]   = getValueDouble("canb.of_amt");
  vouchAmt[24]   = getValueDouble("canb.of_amt");
  vouchAmt[25]   = getValueDouble("canb.c_vouch_amt");

  vouchAmt[11] = commCurrAmt(getValue("curr_code"),
                                getValueDouble("canb.af_amt")*5/100.0,0);
  vouchAmt[10] = getValueDouble("canb.af_amt") - vouchAmt[11];

  vouchAmt[13] = commCurrAmt(getValue("curr_code"),
                                getValueDouble("canb.cf_amt")*5/100.0,0);
  vouchAmt[12] = getValueDouble("canb.cf_amt") - vouchAmt[13];

  vouchAmt[15] = commCurrAmt(getValue("curr_code"),
                                getValueDouble("canb.pf_amt")*5/100.0,0);
  vouchAmt[14] = getValueDouble("canb.pf_amt") - vouchAmt[15];

  vouchAmt[24] = commCurrAmt(getValue("curr_code"),
                                getValueDouble("canb.of_amt")*5/100.0,0);
  vouchAmt[23] = getValueDouble("canb.of_amt") - vouchAmt[24];
 }
// ************************************************************************
 void selectCycVouchData3() throws Exception
 {
  selectSQL = "curr_code,"
            + "payment_type,"
            + "sum(vouch_amt) as vouch_amt";
  daoTable  = "cyc_vouch_data";
  whereStr  = "WHERE  vouch_data_type in  ('6','7') "   // Cycc200 use vouch_data_type '7'
            + "AND    proc_flag = 'N' "
            + "GROUP BY curr_code,payment_type "
            ;

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    vouchAmt[1]   = getValueDouble("vouch_amt");
    vouchAmt[2]   = getValueDouble("vouch_amt");

    selectPtrCurrcode();
    if (selectMktLoanParmRem()!=0)
       insertVoucherRtn(3,getValue("curr_code_gl"));
    else
       insertVoucherRtn(0,getValue("curr_code_gl"));
    updateCycVouchData("6");
    updateCycVouchData("7");
   } 
  closeCursor();
  return;
 }
// ************************************************************************
void selectPtrCurrcode() throws Exception 
 {
  selectSQL = "curr_code_gl";
  daoTable  = "ptr_currcode";
  whereStr  = "WHERE curr_code = ? "
            ;

  setString(1,getValue("curr_code"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) 
     {
      showLogMessage("I","","ERROR : 會計分錄未設定(ptr_currcode) ["+getValue("curr_code")+"]");
      exitProgram(0);
     }
 }
// ************************************************************************
 int selectMktLoanParmAdd() throws Exception 
 {
  selectSQL = "add_vouch_no as loan_vouch_no";
  daoTable  = "mkt_loan_parm";
  whereStr  = "WHERE substr(fund_code,1,4) = ? "
            + "and   add_vouch_no != '' "
            ;

  setString(1,getValue("payment_type"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectMktLoanParmRem() throws Exception 
 {
  selectSQL = "rem_vouch_no as loan_vouch_no";
  daoTable  = "mkt_loan_parm";
  whereStr  = "WHERE substr(fund_code,1,4) = ? "
            + "and   rem_vouch_no != '' "
            ;

  setString(1,getValue("payment_type"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 void updateCycVouchData(String dataType) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag = 'Y', "
            + "proc_date = ?, "
            + "mod_pgm   = ?, "
            + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "cyc_vouch_data";
  whereStr  = "WHERE  proc_flag    = 'N' "
            + "and    curr_code    = ? "
            + "and    payment_type = ? "
            + "and    vouch_data_type = ? "
            ;

  setString(1 , sysDate);
  setString(2 , javaProgram);
  setString(3 , sysDate+sysTime);
  setString(4 , getValue("curr_code"));
  setString(5 , getValue("payment_type"));
  setString(6 , dataType);

  updateTable();
  return;
 }
// ************************************************************************
 void insertVoucherRtn(int index, String idxCurr) throws Exception 
 {
  String hVouchCdKind = "";
  double callVoucherAmt = 0;

  comcr.hGsvhCurr    = idxCurr;
  comcr.hGsvhModPgm = javaProgram;

  switch (index)
    {
     case 0:/* 專案特殊會計分錄 */
            hVouchCdKind = getValue("loan_vouch_no");
            comcr.startVouch("1", hVouchCdKind);
            break;
//   case 1:/* 基金產生 */
//          hVouchCdKind = "A-36";        
//          comcr.startVouch("1", hVouchCdKind);
//          break;
     case 2: /* 基金銷帳 */
//          hVouchCdKind = "A372";
            hVouchCdKind = "H001";         
            comcr.startVouch("1", hVouchCdKind);
            break;
     case 20:/* 基金轉溢付款 */ /*有預繳(同銷帳會計分錄)*/
//          hVouchCdKind = "H005";     
            hVouchCdKind = "H001";   
            comcr.startVouch("1", hVouchCdKind);
            break;            
//     case 3:/* 基金移除 */
//            hVouchCdKind = "A393";	 
//            comcr.startVouch("1", hVouchCdKind);
//            break;
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
	  comcr.hGsvhMemo1   = getValue("payment_type"); 
	  switch (index)
	    {
//	     case 0:/* 專案特殊會計分錄 */
//	            hVouchCdKind = getValue("loan_vouch_no");
//	            comcr.startVouch("1", hVouchCdKind);
//	            break;
	     case 2: /* 基金銷帳 */
	    	  comcr.hGsvhMemo1   = "現金回饋銷帳";
	          break;
	     case 20: /* 基金轉溢付款 */ 	
//	    	  comcr.hGsvhMemo1   = "現金回饋轉溢付款";
	    	  comcr.hGsvhMemo1   = "現金回饋銷帳(有預繳帳務)";	    	  
	          break;	          
//	     case 3:/* 基金移除 */
//	    	  comcr.hGsvhMemo1   = "現金回饋移除";
//	          break;
	    }	  

     comcr.hVoucSysRem = hVouchCdKind;
     callVoucherAmt = vouchAmt[getValueInt("dbcr_seq",inti)]; 

     if(callVoucherAmt != 0)
       {
        if (comcr.detailVouch(getValue("ac_no",inti),
                               getValueInt("dbcr_seq",inti),
                               callVoucherAmt,
                               getValue("curr_code"))!=0)
           {
            showLogMessage("I","","call detail_vouch error");
            exitProgram(1);
           }
       }
    }
 }
// ************************************************************************
 double commCurrAmt(String currCode, double val, int rnd) throws Exception
 {

  setValue("pcde.curr_code" , currCode);
  int cnt1 = getLoadData("pcde.curr_code");

  val = val * 10000.0;
  val = Math.round(val);

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal("10000"));

  if (cnt1==0) return(currAmt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

  return(retNum);
 }
// ************************************************************************
 void loadPtrCurrcode() throws Exception
 {
  extendField = "pcde.";
  selectSQL = "curr_code,"
            + "curr_amt_dp";
  daoTable  = "ptr_currcode";
  whereStr  = "";

  int  n = loadTable();

  setLoadData("pcde.curr_code");

  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

