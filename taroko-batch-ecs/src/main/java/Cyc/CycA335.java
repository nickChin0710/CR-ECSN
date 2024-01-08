/*****************************************************************************
*                              MODIFICATION LOG                              *
*    DATE    Version    AUTHOR                  DESCRIPTION                  *
*  --------  --------------------------------------------------------------- *
* 112-08-22  V1.00.01   Holmes                  一般回饋加贈處理程式         *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA335 extends AccessDAO
{
 private final String PROGNAME = "台幣基金-消費比例一般回饋加贈處理程式 112-08-22  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;

 String businessDate   = "";
 String tranSeqno = "";
 String feedbackType   = "2";  //2 cycle  1: Month Fixed Date
 String fundCode="";
 String pSeqno = "";
 int    cycleFlag = 0;   // 0 : cycle  1: Month Fixed Date
 double tempDestAmt = 0;
 long    totalCnt=0;
 long    cashbackCnt=0;
 int parmCnt=0;;
 int[] dInt = {0,0,0,0};
 int nBegTranAmt = 0;
 int nBegTranAmt1 = 0;
 int nBegTranAmt2 = 0;
 int insertCnt=0,updateCnt=0;
 int runCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA335 proc = new CycA335();
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

   
   if ((args.length>4)||(args.length<1))
   {
    showLogMessage("I","","請輸入參數:");
    showLogMessage("I","","PARM 1 : [feedbackType  1.每月 2.帳單週期]");
    showLogMessage("I","","PARM 2 : [businessDate]");
    showLogMessage("I","","PARM 3 : [fundCode]");
    showLogMessage("I","","PARM 4 : [pSeqno]");
    return(1);
   }

   if (args.length == 4 )
	      pSeqno = args[3];
	   if (args.length >= 3 )
	      fundCode = args[2];
	   if (args.length >= 2 )
	      businessDate = args[1];
	   if (args.length >= 1 )
	      feedbackType  = args[0];
  
   if ((!feedbackType.equals("1"))&&
	       (!feedbackType.equals("2")))
   {
	       showLogMessage("I","","回饋方式 : 1.每月 2.帳單週期 ");
	       return(1);
   }

   if ( !connectDataBase() ) 
       return(1);
   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   cycleFlag = selectPtrWorkday();
   
// String feedbackType   = "2";  //2 cycle  1: Month Fixed Date
// int    cycleFlag = 0;   // 0 : cycle  1: Month Fixed Date  
   
   if ((feedbackType.equals("2")) && !(cycleFlag == 0) )   {
	       showLogMessage("I","","回饋方式 : 2.帳單週期 ,本日非關帳日,不需執行");
	       showLogMessage("I","","本日非關帳日, 不需執行");
	       return(0);
   }
   if ((feedbackType.equals("1")) &&  (cycleFlag == 0) )   {
	       showLogMessage("I","","回饋方式 : 1.每月指定日 ,本日是關帳日,不需執行 ");
	       return(0);
   }
   

   if (selectPtrFundp()==0)
      {
       showLogMessage("I","","本日無符合參數, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadPtrFundData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除逾期明細資料(cyc_addon_calfund)...");
   deleteCycAddonCalfund();
   showLogMessage("I","","刪除逾期明細資料(cyc_addon_calrowsum)...");
   deleteCycAddonCalrowsum();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理( cyc_addon_calfund )資料...");
   selectCycAddonCalfund();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","STEP-1:處理(cyc_addon_calrow constrain check)資料..."); 
   selectCycAddonCalrow_1();
   showLogMessage("I","","STEP-1:處理  count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","STEP-2:處理(cyc_addon_calrow 彙總to cyc_addon_calrowsum)資料...");
   //to-do
   selectCycAddonCalrow_2();
   showLogMessage("I","","STEP-2:處理  count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","STEP-3處理(cyc_addon_calrowsum produce mkt_cashback_dtl )資料...");
   //to-do
   selectCycAddonCalrow_3();
   showLogMessage("I","","STEP-3處理  count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");

   if (pSeqno.length()==0) finalProcess();

   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
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
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "stmt_cycle";
  daoTable  = "ptr_workday";
  whereStr  = "where stmt_cycle = ? ";

  setString(1 , businessDate.substring(6,8));

  int recCnt = selectTable();

  setValue("wday.this_acct_month" , businessDate.substring(0,6));
  setValue("wday.last_acct_month" , comm.lastMonth(businessDate,1));

  if ( notFound.equals("Y") )
     {
      setValue("wday.stmt_cycle" , "");
      return(1);
     }

  setValue("wday.this_close_date" , businessDate);
  setValue("wday.last_close_date" , comm.lastMonth(businessDate,1)
                                  + businessDate.substring(6,8)); 

  return(0);
 }
// ************************************************************************
//to-do
 void  selectCycAddonCalfund() throws Exception
 {   
	  selectSQL = "proc_date ,"
	            + "p_seqno ,"
	            + "fund_code ,"
	            + "fund_type ,"           
	            + "acct_month ,"
	            + "group_addoncode," 
	            + "group_cblimit  ," 
	            + "group_cashback  ," 	            
	            + "addon_code1 ,"            
	            + "addoncode1_destamt ,"	            	            
	            + "addoncode1_rate  ,"           	            	            	            
	            + "addoncode1_cashback ,"
	            + "addon_code2 ,"               
	            + "addoncode2_destamt ,"
	            + "addoncode2_rate ," 
	            + "addoncode2_cashback ,"	            
	            + "acct_type ,"
	            + "id_p_seqno ,"
	            + "major_id_p_seqno ,"  
	            + "major_card_no ,"          
	            + "group_code  ," 
	            + "rowid as rowid "          
	            ; 
	  daoTable  = "cyc_addon_calfund a ";
	  whereStr  = "where fund_type  = '0' "
	            + "and   proc_date    = ? "
	            + "and   proc_mark    = 'N' " 
	            ;
	  
	  setString(1 , businessDate);	  
	  
	  if (fundCode.length()!=0)
	     {
	      whereStr  = whereStr 
	                + "and   fund_code = ? ";
	      setString(1, fundCode);
	      if (pSeqno.length()!=0)
	         {
	          whereStr  = whereStr 
	                    + "and   p_seqno = ? ";
	          setString(2, pSeqno);
	         }
	     }
	  
	  openCursor();

	  totalCnt=0;
	  cashbackCnt=0;
	  
	  int inti = -1;
	  while( fetchTable() ) 
	  { 
	    totalCnt++;

	    for (int intm=0;intm<parmCnt;intm++)
	      {
	       if (!getValue("parm.fund_code",intm).equals(getValue("fund_code"))) continue;
	       inti = intm;
	       break;
	      }
	    if (inti==-1) continue;
	    
	    if (getValue("addoncode1_rate").equals("")) {
	    	setValueDouble("addoncode1_rate",0.0);
	    }
	    if (getValue("addoncode2_rate").equals("")) {
	    	setValueDouble("addoncode2_rate",0.0);
	    }	    
	//  addoncode_rate 參數設定,並已寫入 cyc_addon_calfund
	    nBegTranAmt1 = (int)Math.round(getValueDouble("addoncode1_destamt")*getValueDouble("addoncode1_rate")/100.0) ; 
	    nBegTranAmt2 = (int)Math.round(getValueDouble("addoncode2_destamt")*getValueDouble("addoncode2_rate")/100.0) ;	
	    nBegTranAmt  = nBegTranAmt1 + nBegTranAmt2 ;
	    
	    //setValueInt("beg_tran_amt" ,  getValueInt("nBegTranAmt") );
	    //if (getValueInt("beg_tran_amt") > 0)
		if (nBegTranAmt > 0){
	        //if ((getValueInt("beg_tran_amt") > getValueInt("group_cblimit"))&&(getValueInt("group_cblimit") > 0))
	        if ((nBegTranAmt > getValueInt("group_cblimit"))&&(getValueInt("group_cblimit") > 0))
			   //setValueInt("beg_tran_amt" ,  getValueInt("group_cblimit"));
	        	nBegTranAmt =  getValueInt("group_cblimit") ;
	    }else{
	        //if ((getValueInt("beg_tran_amt")*-1 > getValueInt("group_cblimit"))&&(getValueInt("group_cblimit") > 0))
		    if ((nBegTranAmt*-1 > getValueInt("group_cblimit"))&&(getValueInt("group_cblimit") > 0))
	        	//setValueInt("beg_tran_amt" ,  getValueInt("group_cblimit")*-1);
	           nBegTranAmt =  getValueInt("group_cblimit")*-1 ;
	       }	  
	    //if (!(getValueInt("beg_tran_amt") == 0)  )
		  if (!(nBegTranAmt == 0)  )	
	    {
           insertMktCashbackDtl(inti);
	       cashbackCnt++;
	    }	    
	    updateCycAddonCalfund("Y");
	    
	  }//end while  
	  closeCursor();
	  showLogMessage("I","","處理 cyc_addon_calfund 筆數["+ totalCnt + "] 筆");
	  showLogMessage("I","","產生mkt_cashback_dtl 筆數["+ cashbackCnt + "] 筆");
	  
	  return;	 	 
	 
 }
// ************************************************************************
 void  selectCycAddonCalrow_1() throws Exception
 {
// ignore  constrain check 
 }
// ************************************************************************
 
//************************************************************************
 void  selectCycAddonCalrow_2() throws Exception
 {
  selectSQL = "p_seqno,"
            + "fund_code,"
            + "group_addoncode,"
            + "group_cblimit,"
            + "addon_code         ,"
            + "addoncode_destamt  ,"
            + "addoncode_rate     ,"
            + "addoncode_cashback ,"
            + "major_card_no	  ,"
            + "card_no            ,"
            + "group_code         ,"
            + "major_id_p_seqno   ,"
            + "id_p_seqno         ,"            
            + "rowid as rowid "
            ;
  daoTable  = "cyc_addon_calrow";
  whereStr  = "where proc_date    = ? "
            + "and   proc_mark    = 'N' " 
            ;

  setString(1 , businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(3, pSeqno);
         }
     }

  openCursor();

  int    loadCnt1       = 0; 
  totalCnt=0;

  while( fetchTable() ) 
   { 

    totalCnt++;
    
    if (getValue("addoncode_rate").equals("")) {
    	setValueDouble("addoncode_rate",0.0);
    }
//  addoncode_rate 參數設定,並已寫入 cyc_addon_calrow
    nBegTranAmt = (int)Math.round(getValueDouble("addoncode_destamt")*getValueDouble("addoncode_rate")/100.0) ; 

    updateCycAddonCalrow(nBegTranAmt);
   } 
  closeCursor();
  
  return;
 }
 
//************************************************************************
 void  selectCycAddonCalrow_3() throws Exception
 {
	  selectSQL = "proc_date ,"
	            + "p_seqno ,"
	            + "a.fund_code ,"
	            + "max(a.fund_type) as fund_type ,"           
	            + "acct_month  ,"
	            + "group_addoncode  ," 
	            + "max(group_cblimit) as group_cblimit  ," 
	            + "sum( " 
	            + "case when substring(addon_code ,3, 2 ) = '01' "
	            + "     or  substring(addon_code ,3, 2 )  = '02' "
	            + "     then nvl(addoncode_cashback, 0) "
	            + "     else 0	"
	            + "end ) as group_cashback ,"
	            + "max( " 
	            + "case when substring(addon_code ,3, 2 ) = '01' "
	            + "     then nvl(addon_code, '') "
	            + "     else ''	"
	            + "end ) as addon_code1  ,"            
	            + "sum( " 
	            + "case when substring(addon_code ,3, 2 ) = '01' "
	            + "     then nvl(addoncode_destamt, 0) "
	            + "     else 0	"
	            + "end ) as addoncode1_destamt  ,"	            	            
	            + "max( "
	            + "case when substring(addon_code ,3, 2 ) = '01' "
	            + "     then nvl( addoncode_rate , 0) "
	            + "else 0 "
	            + "end ) as addoncode1_rate  ,"           	            	            	            
	            + "sum( " 
	            + "case when substring(addon_code ,3, 2 ) = '01' "
	            + "     then nvl(addoncode_cashback, 0) "
	            + "     else 0	"
	            + "end ) as addoncode1_cashback , "
	            + "max( " 
	            + "case when substring(addon_code ,3, 2 ) = '02' "
	            + "     then nvl(addon_code, '') "
	            + "     else ''	"
	            + "end ) as addon_code2  ,"               
	            + "sum( "
	            + "case when substring(addon_code ,3, 2 ) = '02' "
	            + "     then nvl( addoncode_destamt, 0)	"
	            + "else 0 "
	            + "end ) as addoncode2_destamt , "
	            + "max( "
	            + "case when substring(addon_code ,3, 2 ) = '02' "
	            + "     then nvl( addoncode_rate , 0) "
	            + "else 0 "
	            + "end ) as addoncode2_rate  ," 
	            + "sum( " 
	            + "case when substring(addon_code ,3, 2 ) = '02' "
	            + "     then nvl(addoncode_cashback, 0) "
	            + "     else 0	"
	            + "end ) as addoncode2_cashback ,"	            
	            + "max(b.feedback_type) as feedback_type ,"
	            + "max(acct_type) as acct_type ,"
	            + "max(id_p_seqno) as id_p_seqno  ,"
	            + "max(major_id_p_seqno) as major_id_p_seqno ,"  
	            + "max(major_card_no) as major_card_no  ,"          
	            + "max(card_no) as card_no  ,"
	            + "max(group_code) as group_code " 
	            ; 	  
	  daoTable  = "cyc_addon_calrow a , ptr_fundp b";
	  whereStr  = "where fund_type  = '0' "
	            + "and   a.fund_code = b.fund_code "
	            + "and   a.proc_date    = ? "
	            + "and   proc_mark    = 'N' " 
	            ;
	  
	  setString(1 , businessDate);	  
	  
	  if (fundCode.length()!=0)
	     {
	      whereStr  = whereStr 
	                + "and   b.fund_code = ? ";
	      setString(1, fundCode);
	      if (pSeqno.length()!=0)
	         {
	          whereStr  = whereStr 
	                    + "and   p_seqno = ? ";
	          setString(2, pSeqno);
	         }
	     }

	  whereStr  = whereStr
	            + "group by proc_date,a.fund_code,p_seqno,acct_month , group_addoncode  "
	            + "having sum(addoncode_cashback) != 0  ";
	            
	  openCursor();

	  totalCnt=0;
	  int inti = -1;
	  while( fetchTable() ) 
	  { 
	    totalCnt++;

	    for (int intm=0;intm<parmCnt;intm++)
	      {
	       if (!getValue("parm.fund_code",intm).equals(getValue("fund_code"))) continue;
	       inti = intm;
	       break;
	      }
	    if (inti==-1) continue;
	    
	    setValueInt("beg_tran_amt" ,  getValueInt("group_cashback") );

	    if (getValueInt("beg_tran_amt") > 0)
	       {
	        if ((getValueInt("beg_tran_amt") > getValueInt("group_cblimit"))&&(getValueInt("group_cblimit") > 0))
	           setValueInt("beg_tran_amt" ,  getValueInt("group_cblimit"));
	       }
	    else
	       {
	        if ((getValueInt("beg_tran_amt")*-1 > getValueInt("group_cblimit"))&&(getValueInt("group_cblimit") > 0))
	           setValueInt("beg_tran_amt" ,  getValueInt("group_cblimit")*-1);
	       }	    
        insertMktCashbackDtl(inti);
	    insertCycAddonCalrowsum();
	    updateCycAddonCalrow_P("Y");
	    
	  }//end while  
	  closeCursor();
	  showLogMessage("I","","處理cyc_addon_calrow產生cyc_addon_calrowsum筆數["+ totalCnt + "] 筆");
	  showLogMessage("I","","處理cyc_addon_calrow產生mkt_cashback_dtl 筆數["+ totalCnt + "] 筆");
	  
	  return;	 	 
 }
 
// ************************************************************************
int selectPtrFundp() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "ptr_fundp";
  whereStr  = "WHERE apr_flag      = 'Y' "
            + "AND   (stop_flag  = 'N' "
            + " OR    (stop_flag = 'Y' "
            + "  and   ? < stop_date)) "
            + "and   feedback_type in ('1','2') "
            + "and   fund_feed_flag = 'Y' "; //比率回饋

  setString(1,businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
     }

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) return(0);

  for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("parm.feedback_type",inti).equals("1"))
          {
           if (!businessDate.substring(6,8).equals
               (String.format("%02d",getValueInt("parm.card_feed_run_day",inti)))) continue;
          }
       else
          {
           if (cycleFlag!=0) continue;
          }

       if (getValue("parm.fund_crt_date_s",inti).length()==0)
          setValue("parm.fund_crt_date_s","20000101",inti);

       if (getValue("parm.fund_crt_date_e",inti).length()==0)
          setValue("parm.fund_crt_date_e","30001231",inti);

       if ((businessDate.compareTo(getValue("parm.fund_crt_date_s",inti))<0)|| 
           (businessDate.compareTo(getValue("parm.fund_crt_date_e",inti))>0))  continue;

       setValue("parm.parmrun_flag" , "N" ,inti);

       if (getValue("parm.acct_type_sel",inti).equals("0")) 
          setValue("parm.valid_card_flag" , "N" ,inti);

       if (getValue("parm.effect_type",inti).length()==0)
          {
           if (getValueInt("parm.effect_months",inti)>0)
              setValue("parm.effect_type" , "1" ,inti);
           else
              setValue("parm.effect_type" , "0" ,inti);
          }

       setValue("parm.parmrun_flag" , "Y" ,inti);
       runCnt++;
      }


  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt + "] 本日執行筆數[" + runCnt + "]");

  return(runCnt);
 }

//************************************************************************
//to-do
int insertCycAddonCalrowsum() throws Exception
{
dateTime();
setValue("acalrowsum.proc_date"       , getValue("proc_date"));
setValue("acalrowsum.acct_month"      , getValue("acct_month"));  
setValue("acalrowsum.fund_code"       , getValue("fund_code"));
setValue("acalrowsum.fund_type"       , getValue("fund_type"));  
setValue("acalrowsum.p_seqno"         , getValue("p_seqno"));
setValue("acalrowsum.acct_type"       , getValue("acct_type"));
setValue("acalrowsum.major_id_p_seqno", getValue("major_id_p_seqno"));  
setValue("acalrowsum.id_p_seqno"      , getValue("id_p_seqno"));

setValue("acalrowsum.group_addoncode" , getValue("group_addoncode"));  
setValueInt("acalrowsum.group_cblimit", getValueInt("group_cblimit"));
setValueInt("acalrowsum.group_cashback", getValueInt("group_cashback"));    
setValue("acalrowsum.addon_code1"       , getValue("addon_code1"));
setValueDouble("acalrowsum.addoncode1_destamt", getValueDouble("addoncode1_destamt") );
setValueDouble("acalrowsum.addoncode1_rate"   , getValueDouble("addoncode1_rate")  );
setValueInt("acalrowsum.addoncode1_cashback", getValueInt("addoncode1_cashback") );  
setValue("acalrowsum.addon_code2"       , getValue("addon_code2"));
setValueDouble("acalrowsum.addoncode2_destamt", getValueDouble("addoncode2_destamt") );
setValueDouble("acalrowsum.addoncode2_rate"   , getValueDouble("addoncode2_rate")  );
setValueInt("acalrowsum.addoncode2_cashback", getValueInt("addoncode2_cashback") );   

setValue("acalrowsum.major_card_no"   , "");
setValue("acalrowsum.group_code"      , "");  
setValue("acalrowsum.proc_mark"       , "Y");
setValue("acalrowsum.mod_user"         , javaProgram);  
setValue("acalrowsum.mod_time"        , sysDate+sysTime);
setValue("acalrowsum.mod_pgm"         , javaProgram);

extendField = "acalrowsum.";
daoTable  = "cyc_addon_calrowsum";

insertTable();

return(0);
}

// ************************************************************************
 int insertMktCashbackDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("mkt_modseq");
  dateTime();

  setValue("tran_date"            , sysDate);
  setValue("tran_time"            , sysTime);
  setValue("fund_code"            , getValue("parm.fund_code",inti));
  setValue("fund_name"            , getValue("parm.fund_name",inti));
  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("acct_type"            , getValue("acct_type"));
  setValue("id_p_seqno"           , getValue("major_id_p_seqno"));
  if (nBegTranAmt>0)
     {
      setValue("tran_code"            , "2");
      setValue("mod_desc"             , "回饋刷卡金");
     }
  else
     {
      setValue("tran_code"            , "7");
      setValue("mod_desc"             , "回饋回饋刷卡金系統扣回");
     }
  setValue("tran_pgm"             , javaProgram);
  //setValueInt("beg_tran_amt"      , getValueInt("beg_tran_amt"));
  //setValueInt("end_tran_amt"      , getValueInt("beg_tran_amt"));
  setValueInt("beg_tran_amt"      , nBegTranAmt);
  setValueInt("end_tran_amt"      , nBegTranAmt);
  
  setValueInt("res_tran_amt"      , 0);
  setValueInt("res_total_cnt"     , 0);
  setValueInt("res_tran_cnt"      , 0);
  setValue("res_s_month"          , "");
  setValue("res_upd_date"         , "");

  if (getValueInt("beg_tran_amt")<0)
      setValue("effect_e_date"        , "");
  else if (getValue("parm.effect_type",inti).equals("0"))
      setValue("effect_e_date"        , "");
  else if (getValue("parm.effect_type",inti).equals("1"))
     setValue("effect_e_date"    , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  else if (getValue("parm.effect_type",inti).equals("2"))
    {
     setValue("effect_e_date"    , comm.nextMonth( businessDate , getValueInt("parm.effect_years",inti)*12).substring(0,4)
                                 + String.format("%02d" , getValueInt("parm.effect_fix_month",inti))
                                 + businessDate.substring(6,8));
    }
  setValue("tran_seqno"           , tranSeqno);
  setValue("proc_month"           , businessDate.substring(0,6));
  setValue("acct_date"            , businessDate);
  setValue("mod_memo"             , "");
  setValue("mod_reason"           , "");
  setValue("case_list_flag"       , "N");
  setValue("crt_user"             , javaProgram);
  setValue("crt_date"             , sysDate);
  setValue("apr_date"             , sysDate);
  setValue("apr_user"             , javaProgram);
  setValue("apr_flag"             , "Y");
  setValue("mod_user"             , javaProgram); 
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteCycAddonCalfund() throws Exception
 {
  daoTable  = "cyc_addon_calfund";
  whereStr  = "WHERE ((proc_mark = 'Y' "
            + "  and   proc_date < ?)  "
            + " or    (proc_mark != 'Y' "
            + "  and   proc_date < ?)) ";

  setString(1, comm.nextMonthDate(businessDate,-12));
  setString(2, comm.nextMonthDate(businessDate,-6));

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(3, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(4, pSeqno);
         }
     }

  int n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete cyc_addon_calfund  [" + n + "] records");

  return;
 }
 
//************************************************************************
 void deleteCycAddonCalrowsum() throws Exception
 {
  daoTable  = "cyc_addon_calrowsum";
  whereStr  = "WHERE ((proc_mark = 'Y' "
            + "  and   proc_date < ?)  "
            + " or    (proc_mark != 'Y' "
            + "  and   proc_date < ?)) ";

  setString(1, comm.nextMonthDate(businessDate,-12));
  setString(2, comm.nextMonthDate(businessDate,-6));

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(3, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(4, pSeqno);
         }
     }

  int n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete cyc_addon_calrowsum [" + n + "] records");

  return;
 }

// ************************************************************************
 void updateCycAddonCalfund(String procMark) throws Exception
 {
  dateTime();

  updateSQL = "proc_mark = ? ,"
            + "group_cashback = ? , "		
            + "addoncode1_cashback = ? , "
            + "addoncode2_cashback = ? , "	
            + "mod_pgm    = ? , "
            + "mod_time   = sysdate";  
  daoTable  = "cyc_addon_calfund";
  whereStr  = "WHERE rowid  = ?"
            ;

  setString(1 , procMark);
  setLong(2 , nBegTranAmt);    
  setLong(3, nBegTranAmt1);
  setLong(4, nBegTranAmt2);
  setString(5 , javaProgram);  
  setRowId(6 , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateCycAddonCalrow_P(String procMark) throws Exception
 {
  dateTime();

  updateSQL = "proc_mark  = ?,"
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";  
  daoTable  = "cyc_addon_calrow";
  whereStr  = "WHERE proc_date    = ? "
            + "and   fund_code    = ? "
            + "and   p_seqno      = ? "
            + "and   proc_mark    = 'N' "
            ;

  setString(1 , procMark);
  setString(2 , getValue("stmt_cycle"));
  setString(3 , javaProgram);
  setString(4 , getValue("proc_date"));
  setString(5 , getValue("fund_code"));
  setString(6 , getValue("p_seqno"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateCycAddonCalrow(int cashBack ) throws Exception
 {
  dateTime();

  updateSQL = "addoncode_cashback  = ?,"
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";  
  daoTable  = "cyc_addon_calrow";
  whereStr  = "WHERE rowid  = ? "
            ;

  setDouble(1 , cashBack);
  setString(2 , javaProgram);
  setRowId(3 , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int insertCycFundDtl(int numType,int inti) throws Exception
 {
  dateTime();
  extendField = "fdtl.";

  setValue("fdtl.business_date"        , businessDate);
  setValue("fdtl.curr_code"            , "901");
  setValue("fdtl.create_date"          , sysDate);
  setValue("fdtl.create_time"          , sysTime);
  setValue("fdtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("fdtl.p_seqno"              , getValue("p_seqno"));
  setValue("fdtl.acct_type"            , getValue("acct_type"));
  setValue("fdtl.card_no"              , "");
  setValue("fdtl.fund_code"            , getValue("parm.fund_code",inti).substring(0,4));
  setValue("fdtl.vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
  if (numType==1)
     {
      setValue("fdtl.tran_code"            , "1");
      setValue("fdtl.cd_kind"              , "H001");    // 新增ew add
     }
  else
     {
      setValue("fdtl.tran_code"            , "7");
      setValue("fdtl.cd_kind"              , "H003");  // 0-移轉 1-新增 2-贈與 3-調整 4-使用 5-匯入 6-移除 7-扣回
     }
  setValue("fdtl.memo1_type"           , "1");   /* fund_code 必須有值 */
  setValueInt("fdtl.fund_amt"          , Math.abs(getValueInt("beg_tran_amt")));
  setValueInt("fdtl.other_amt"         , 0);
  setValue("fdtl.proc_flag"            , "N");
  setValue("fdtl.proc_date"            , "");
  setValue("fdtl.execute_date"         , businessDate);
  setValue("fdtl.fund_cnt"             , "1");
  setValue("fdtl.mod_user"             , javaProgram); 
  setValue("fdtl.mod_time"             , sysDate+sysTime);
  setValue("fdtl.mod_pgm"              , javaProgram);

  daoTable  = "cyc_fund_dtl";

  insertTable();

  insertCnt++;
  return(0);
 }
// ************************************************************************
 String resSMonth(String yearSDate,String nowDate,int calMonths) throws Exception
 {
  String okDate="";
  for (int inti=0;inti<6000;inti++)
    {
     okDate = comm.nextMonthDate(yearSDate,inti*calMonths);
     if (okDate.compareTo(nowDate)>=0) break;
    }
  return okDate;
 }

// ************************************************************************
 void  loadPtrFundData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "ptr_fund_data";
  whereStr  = "WHERE TABLE_NAME = 'PTR_FUNDP' "
            + "and   data_type in ('3','4','5','A') "
            + "and   data_key in "
            + "     (select fund_code from ptr_fundp "
            + "      WHERE apr_flag      = 'Y' "
            + "      AND   (stop_flag  = 'N' "
            + "       OR    (stop_flag = 'Y' "
            + "         and   ? < stop_date)) "
            + "       and   feedback_type in ('1','2') "
            + "       and   fund_feed_flag = 'Y' " 
            ;

  setString(1, businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
     }

  whereStr  = whereStr
            + "          ) "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load ptr_fund_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectPtrFundData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectPtrFundData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectPtrFundData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectPtrFundData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectPtrFundData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
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
         if (getValue("data.data_code",intm).length()!=0)
            {
             if (col1.length()!=0)
                {
                 if (!getValue("data.data_code",intm).equals(col1)) continue;
                }
              else
                {
                 if (sel.equals("1")) continue;
                }
            }
        }
     if (getValue("data.data_code2",intm).length()!=0)
        {
         if (col2.length()!=0)
            {
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
            }
          else
            {
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
         if (col3.length()!=0)
            {
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
            }
          else
            {
             continue;
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
 void  loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,ptr_fund_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'PTR_FUNDP' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  = '6' "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_fstpgp_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktMchtgpData(String col1,String col2,String sel,String dataType) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("mcht.data_key" , getValue("data_key"));
  setValue("mcht.data_type",dataType);
  setValue("mcht.data_code",col1);

  int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  int okFlag=0;
  for (int inti=0;inti<cnt1;inti++)
    {
     if ((getValue("mcht.data_code2",inti).length()==0)||
         ((getValue("mcht.data_code2",inti).length()!=0)&&
          (getValue("mcht.data_code2",inti).equals(col2))))
        {
         okFlag=1;
         break;
        }
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
 int insertCycCobrandFund(int inti) throws Exception
 {
  dateTime();
  extendField = "cobr.";

  setValue("cobr.proc_date"            , businessDate);
  setValue("cobr.program_code"         , getValue("fund_code").substring(0,4));
  setValue("cobr.cobrand_code"         , getValue("parm.cobrand_code",inti));
  setValue("cobr.card_code"            , "C");
  setValue("cobr.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cobr.p_seqno"              , getValue("p_seqno"));
  setValue("cobr.acct_type"            , getValue("acct_type"));
  setValue("cobr.acct_month"           , getValue("acct_month"));
  setValue("cobr.major_card_no"        , getValue("major_card_no")); 
  setValueInt("cobr.fund_amt"          , getValueInt("beg_tran_amt"));
  setValue("cobr.mod_time"             , sysDate+sysTime);
  setValue("cobr.mod_pgm"              , javaProgram);

  daoTable  = "cyc_cobrand_fund";

  insertTable();

  return(0);
 }
// ************************************************************************
 void  loadPtrActgeneralN() throws Exception
 {
  extendField = "agnn.";
  selectSQL = "acct_type,"
            + "mp_3_rate";
  daoTable  = "ptr_actgeneral_n";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("agnn.acct_type");

  showLogMessage("I","","Load ptr_actgeneral_n Count: ["+n+"]");
 }
// ************************************************************************
 int   calThresholdAmt1(int inti,double tempDAmt) throws Exception
 {
  double dTempTotAmt = 0;
  int    tempLAmt     = 0;
  int    loadCnt1       = 0; 
  double dTempDAmt   = 0;

  if (tempDAmt<0) tempDAmt = tempDAmt*-1;

  if ((getValueInt("parm.fund_s_amt_5",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_5",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_5",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_5",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_5",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_5",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_4",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_4",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_4",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_4",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_4",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_4",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_3",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_3",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_3",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_3",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_3",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_3",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_2",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_2",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_2",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_2",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_2",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_2",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_1",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_1",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_1",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_1",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_1",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_1",inti);
       tempDAmt = tempLAmt;
      }

  return((int)Math.round(dTempTotAmt));
 }
// ************************************************************************
 int   calThresholdAmt2(int inti,double tempDCnt,double tempDAmt) throws Exception
 {
  int signFlag=1;
  if (tempDCnt<0) tempDCnt = tempDCnt*-1;
  if (tempDAmt<0) signFlag = -1;

  if ((getValueInt("parm.fund_s_amt_5",inti) > 0)&&
      (tempDCnt >= getValueInt("parm.fund_s_amt_5",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_5",inti)/100.0
                + getValueDouble("parm.fund_amt_5",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_4",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_4",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_4",inti)/100.0
                + getValueDouble("parm.fund_amt_4",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_3",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_3",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_3",inti)/100.0
                + getValueDouble("parm.fund_amt_3",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_2",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_2",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_2",inti)/100.0
                + getValueDouble("parm.fund_amt_2",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_1",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_1",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_1",inti)/100.0
                 + getValueDouble("parm.fund_amt_1",inti))*signFlag)*signFlag); 

  return(0);
 }
 
 
// ************************************************************************

}  // End of class FetchSample
