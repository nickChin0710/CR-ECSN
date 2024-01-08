/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/05/12  V1.00.61  Allen Ho   cyc_a180                                   *
* 110/09/14  V1.01.01  Allen Ho   bug for stop_date                          *
* 110/11/08  V1.01.02  Lai        M: 9015                                    *
* 110/11/10  V1.01.03  Lai        M: 9034                                    *
* 110/12/01  V1.02.07  Allen Ho   revise from V1.01.01                       *
* 110/12/10  V1.02.08  Allen Ho   mcht_group_sel bug                         *
* 111/03/02  V1.03.02  Allen Ho   new_group_cond mantis 9288                 *
* 111-11-11  V1.04.01  Machao     sync from mega & updated for project coding standard            *
* 112-01-06  V1.04.02  Holmes     dest_max  異動 && 退貨無扣點 異動          *  
* 112-10-10  V1.04.03  Holmes     一般回饋加贈處理程式                       *
******************************************************************************/
package Cyc;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA300 extends AccessDAO
{
 private final String PROGNAME = "台幣基金-消費款轉基金篩選處理程式 12-10-10  V1.04.03";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;

 String businessDate   = "";

 String feedbackType   = "2";
 String fundCode       = "";
 String minAcctMonth  = "999999";
 String pSeqno      = "";
 
 int    cycleFlag = 0;   // 0 : cycle
 
 long    totalCnt=0;
 int parmCnt=0,cnt1=0;;
 int cycleCnt=0;;
 int matchCnt = 0;;

 boolean DEBUG = false;
 boolean DEBUGAmt = false;
 
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA300 proc = new CycA300();
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
       showLogMessage("I","","PARM 1 : feedback_type  1.每月 2.帳單週期");
       showLogMessage("I","","PARM 2 : [business_date]");
       showLogMessage("I","","PARM 3 : [fund_code]");
       showLogMessage("I","","PARM 4 : [p_seqno]");
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

   if ( !connectDataBase() ) return(1);

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
   
   selectPtrFundp();   
   
   if (matchCnt==0)
      {
       showLogMessage("I","","今日["+businessDate+"]無活動回饋");
       if (args.length > 1 )
          showLogMessage("I",""," Feedback_type ["+ feedbackType+"]");
       if (args.length > 2 )
          showLogMessage("I",""," fund_code     ["+ fundCode +"]");
       countCommit();
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","清除暫存檔(ptr_fundp_bill)");
   if (fundCode.length()!=0)
      {
       showLogMessage("I","","   Delete PTR_FUNDP_BILL , CYC_ADDON_BILL ,CYC_ADDON_CALROW ");
       deletePtrFundpBill();
       deleteCycAddonBill();
       deleteCycAddonCalRow();
       commitDataBase();
      }
   else
      {
       showLogMessage("I","","   Truncate PTR_FUNDP_BILL , CYC_ADDON_BILL ,CYC_ADDON_CALROW ");
       commitDataBase();
       truncateTable("PTR_FUNDP_BILL");
       commitDataBase();
       truncateTable("CYC_ADDON_BILL");
       commitDataBase();       
       truncateTable("CYC_ADDON_CALROW");       
       commitDataBase();
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadPtrFundData();
   loadPtrFundCdata();
   loadMktMchtgpData();
   loadMktRcvBin();
   loadMktAddonIdlist();   
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理資料(bil_bill)...");
   selectBilBill(0);
// if (cycleCnt!=0)  select_bil_bill(1);
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料(crd_card,crd_idno)");
   loadCrdCard();
   loadCrdCard1();
   loadCrdIdno();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理消費門檻資料(ptr_fundp__bill)...");
   selectPtrFundpBill();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理回饋(產生)期間 (ptr_fundp_bill");
   selectPtrFundpBill1();
   showLogMessage("I","","-----------------------------------------");
   showLogMessage("I","","處理基金回饋比例資料(cyc_cal_fund)...");
   selectPtrFundpBill2();
   showLogMessage("I","","-----------------------------------------");
   showLogMessage("I","","處理對帳單POS_ENTRY (cyc_pos_entry)...");
   selectPtrFundpBill3();
   showLogMessage("I","","-----------------------------------------");
   showLogMessage("I","","處理基金回饋比例加碼(cyc_addon_calfund)..");
   selectCycAddonBill_2();
   showLogMessage("I","","=========================================");

   if (pSeqno.length()==0) finalProcess();

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
  int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1 , businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectBilBill(int cycleInt) throws Exception
 {
  daoTable  = "bil_bill";
  whereStr  = "where acct_code in ('BL','IT','ID','CA','OT','AO') "
            + "and   rsk_type not in ('1','2','3') "
            + "and   merge_flag       != 'Y'  "
            + "and   curr_code    = '901' "
            + "and   dest_amt != 0 "
            ;
            
  if (feedbackType.equals("2"))
     {
      whereStr  = whereStr 
                + "and   acct_month   between ? and ? "
                + "and   stmt_cycle = ? ";
                ;
       
      setString(1, minAcctMonth);
      setString(2, getValue("wday.this_acct_month"));
      setString(3, getValue("wday.stmt_cycle"));

      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(4, pSeqno);
         }

      showLogMessage("I",""," 帳務年月 :["+minAcctMonth +"] ~ ["+ getValue("wday.this_acct_month") +"]");
     }
  else
      {
//    if (cycle_int==0)
         {
          whereStr  = whereStr
                     + "and   acct_month  = ? "
                     ;

          setString(1, comm.lastMonth(businessDate,1));
          if (pSeqno.length()!=0)
             {
              whereStr  = whereStr 
                        + "and   p_seqno = ? ";
              setString(2, pSeqno);
             }
          showLogMessage("I",""," 帳務年月 :["+ comm.lastMonth(businessDate,1) +"]");
         }
/* cancel post_date item , mega don't agree
      else
         {
          whereStr  = whereStr
                     + "and post_date between ? and  ? "
                     ;
    
          setString(1, comm.lastMonth(business_date,1)+"01"); 
          setString(2, comm.lastdateOfmonth(comm.lastMonth(business_date,1)));

          if (p_seqno.length()!=0)
             {
              whereStr  = whereStr 
                        + "and   p_seqno = ? ";
              setString(3, p_seqno);
             }
          showLogMessage("I",""," 入帳日期 :["+ comm.lastMonth(business_date,1)+"01"
                                +"] ~ ["+ comm.lastdateOfmonth(comm.lastMonth(business_date,1)) +"]");
         }
*/
     }

  openCursor();

  String icaStr ="";
  int currFlag=0;
  totalCnt=0;
  int[] purchCnt = new int[parmCnt];
  int[] feedCnt  = new int[parmCnt];

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     {
      feedCnt[inti]=0;
      purchCnt[inti]=0;
      for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;
     }

  String acqId ="";
  int posFlag=0;
  String fillBankNo = "";
  while( fetchTable() ) 
   { 
    totalCnt++;
    setValue("vd_flag" , "N");

    if (!getValue("sign_flag").equals("+"))
        setValueDouble("dest_amt" , getValueDouble("dest_amt")*-1);
       
    if (getValue("acct_code").equals("IT"))
       setValueDouble("dest_amt" , getValueDouble("dest_amt")
                                 + getValueDouble("dc_curr_adjust_amt"));

    if (getValueDouble("dest_amt")==0) continue;

    if (getValue("group_code").length()==0)
        setValue("group_code" , "0000");

    if (getValue("source_code").length()==0)
        setValue("source_code" , "ZZ0000");
    
    if (getValue("mcht_country").length()==3) 
        setValue("mcht_country" , getValue("mcht_country").substring(0,2));         
    if (getValue("mcht_country").equals("")) 
        setValue("mcht_country" , "TW");    
    
    for (int inti=0;inti<parmCnt;inti++)
    {
         parmArr[inti][0]++;
         if (getValue("parm.parmrun_flag",inti).equals("N")) continue;

/*  cancel feedback_cycle_flag ,mega don't agree
         if (feedback_type.equals("1"))
            {
             if ((getValue("parm.feedback_cycle_flag",inti).equals("1"))&& 
                 (cycle_int!=1)) continue;
             if ((getValue("parm.feedback_cycle_flag",inti).equals("2"))&& 
                 (cycle_int==1)) continue;
            }
*/

         parmArr[inti][1]++;

         if (getValue("parm.feedback_type",inti).equals("2"))
            if (!getValue("stmt_cycle").equals(getValue("wday.stmt_cycle"))) continue;

         parmArr[inti][2]++;
         
		// 國內外消費篩選: foreign_code
		if (!getValue("parm.foreign_code", inti).equals("3")) {
			if (getValue("parm.foreign_code", inti).equals("2") && getValue("mcht_country").equals("TW")) {
				continue;
			}
			if (getValue("parm.foreign_code", inti).equals("1") && !getValue("mcht_country").equals("TW")) {
				continue;
			} 			
			
		}
  
         if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL"))) continue;
         if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("acct_code").equals("IT"))) continue;
         if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA"))) continue;
         if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID"))) continue;
         if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("acct_code").equals("AO"))) continue;
         if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT"))) continue;

         parmArr[inti][3]++;

         if (DEBUGAmt)
            {
             showLogMessage("I",""," STEP AMT-01 reference_no ["+getValue("reference_no")+"]");
             showLogMessage("I",""," STEP AMT-01 sign_flag    ["+getValue("sign_flag")+"]");
             showLogMessage("I",""," STEP AMT-01 dest_amt     ["+getValueDouble("dest_amt")+"]");
             showLogMessage("I",""," STEP AMT-01 adjust_amt   ["+getValueDouble("dc_curr_adjust_amt")+"]");
            }

         if (DEBUGAmt)
            {
             showLogMessage("I",""," STEP AMT-02 reference_no ["+getValue("reference_no")+"]");
             showLogMessage("I",""," STEP AMT-02 dest_amt     ["+getValueDouble("dest_amt")+"]");
            }

         setValue("data_key" , getValue("parm.fund_code",inti));
         parmArr[inti][4]++;

         if (selectPtrFundData(getValue("group_code"),
                                  getValue("parm.group_code_sel",inti),"3",3)!=0) continue;

         parmArr[inti][5]++;
         acqId = "";
         if (getValue("acq_member_id").length()!=0)
            acqId = comm.fillZero(getValue("acq_member_id"),8);

         if (pSeqno.length()>0)
            showLogMessage("I",""," STEP 0330-0001 reference_no ["+ getValue("reference_no") +"]");       

         if (!getValue("parm.merchant_sel",inti).equals("0"))
            {
             posFlag=0;
             setValue("aica.ica_no" ,  acqId);
             cnt1 =  getLoadData("aica.ica_no");

             if (cnt1==0) 
                {
                 setValue("aica.bank_no","");
                 cnt1=1;
                }

             if (pSeqno.length()>0)
                {
                 showLogMessage("I",""," STEP 0330-00A1 acq_id ["+ acqId +"]");
                 showLogMessage("I",""," STEP 0330-00A2 cnt1   ["+ cnt1 +"]");
                }

             if (pSeqno.length()>0)
                {
                 showLogMessage("I",""," STEP 0330-00A3 merchant_sel ["+ getValue("parm.merchant_sel",inti) +"]");
                 showLogMessage("I",""," STEP 0330-00A4 mcht_no      ["+ getValue("mcht_no") +"]");
                }
             if (getValue("parm.merchant_sel",inti).equals("1"))
                {
                 posFlag=0;
                 for (int intk=0;intk< cnt1;intk++)
                    {
                     if (pSeqno.length()>0)
                        showLogMessage("I",""," STEP 0330-00C1 bank_no ["+ getValue("aica.bank_no",intk) +"]");

                     if (selectPtrFundData(getValue("mcht_no"),getValue("aica.bank_no",intk),
                                              getValue("parm.merchant_sel",inti),"1",3)==0)
                        {
                         posFlag=1;
                         break;
                        }
                    }
                }
             if (getValue("parm.merchant_sel",inti).equals("2"))
                {
                 posFlag=1;
                 for (int intk=0;intk< cnt1;intk++)
                    {
                     if (pSeqno.length()>0)
                        showLogMessage("I",""," STEP 0330-00C2 bank_no ["+ getValue("aica.bank_no",intk) +"]");

                     if (selectPtrFundData(getValue("mcht_no"),getValue("aica.bank_no",intk),
                                              getValue("parm.merchant_sel",inti),"1",3)!=0)
                        {
                         posFlag=0;
                         break;
                        }
                    }
                }
             if (pSeqno.length()>0)
                {
                 if (posFlag==0)
                    showLogMessage("I",""," STEP 0330-00A6 資格不符 ");
                 else
                    showLogMessage("I",""," STEP 0330-00A6 資格符合 ");
                }
             if (posFlag==0) continue;
            }
         
		// 判斷特店中英名稱
		if (selectPtrFundCdata(getValue("parm.mcht_cname_sel", inti),
				getValue("parm.mcht_ename_sel", inti)) != 0)
			continue;         
         
         parmArr[inti][6]++;

         if (!getValue("parm.mcht_group_sel",inti).equals("0"))
            {
             setValue("aica.ica_no" ,  acqId);
             cnt1 =  getLoadData("aica.ica_no");
             if (cnt1==0) 
                {
                 setValue("aica.bank_no","");
                 cnt1=1;
                }
             if (pSeqno.length()>0)
                {
                 showLogMessage("I",""," STEP 0330-00B1 acq_id ["+ acqId +"]");
                 showLogMessage("I",""," STEP 0330-00B2 cnt1   ["+ cnt1 +"]");
                }
             if (pSeqno.length()>0)
                {
                 showLogMessage("I",""," STEP 0330-00B3 mcht_group_sel ["+ getValue("parm.mcht_group_sel",inti) +"]");
                 showLogMessage("I",""," STEP 0330-00B4 mcht_no        ["+ getValue("mcht_no") +"]");
                }
             if (getValue("parm.mcht_group_sel",inti).equals("1"))
                {
                 posFlag=0;
                 for (int intk=0;intk< cnt1;intk++)
                    {
                     fillBankNo = comm.fillZero(getValue("aica.bank_no",intk),8);
                     if (pSeqno.length()>0)
                        showLogMessage("I",""," STEP 0330-00D1 bank_no ["+ fillBankNo +"]");
                     if (selectMktMchtgpData(getValue("mcht_no"), fillBankNo,
                                                getValue("parm.mcht_group_sel",inti),"H")==0)
                        {
                         posFlag=1;
                         break;
                        }
                    }
                }
             if (getValue("parm.mcht_group_sel",inti).equals("2"))
                {
                 posFlag=1;
                 for (int intk=0;intk< cnt1;intk++)
                    {
                     fillBankNo = comm.fillZero(getValue("aica.bank_no",intk),8);
                     if (pSeqno.length()>0)
                        showLogMessage("I",""," STEP 0330-00D2 fill_bank_no   ["+ fillBankNo +"]");
                     if (selectMktMchtgpData(getValue("mcht_no"), fillBankNo,
                                                getValue("parm.mcht_group_sel",inti),"H")!=0)
                        {
                         posFlag=0;
                         break;
                        }
                    }
                }
             if (pSeqno.length()>0)
                {
                 if (posFlag==0)
                    showLogMessage("I",""," STEP 0330-00B6 資格不符 ");
                 else
                    showLogMessage("I",""," STEP 0330-00B6 資格符合 ");
                }
             if (posFlag==0) continue;
            }
         parmArr[inti][7]++;
         
         if (!getValue("parm.platform_kind_sel",inti).equals("0"))
         {
          setValue("aica.ica_no" ,  acqId);
          cnt1 =  getLoadData("aica.ica_no");
          if (cnt1==0) 
             {
              setValue("aica.bank_no","");
              cnt1=1;
             }
          if (pSeqno.length()>0)
             {
              showLogMessage("I",""," STEP 0330-00P1 acq_id ["+ acqId +"]");
              showLogMessage("I",""," STEP 0330-00P2 cnt1   ["+ cnt1 +"]");
             }
          if (pSeqno.length()>0)
             {
              showLogMessage("I",""," STEP 0330-00P3 platform_kind_sel ["+ getValue("parm.platform_kind_sel",inti) +"]");
              showLogMessage("I",""," STEP 0330-00P4 ecs_cus_mcht_no        ["+ getValue("ecs_cus_mcht_no") +"]");
             }
          if (getValue("parm.platform_kind_sel",inti).equals("1"))
             {
              posFlag=0;
              for (int intk=0;intk< cnt1;intk++)
                 {
                  if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"), "",
                                             getValue("parm.platform_kind_sel",inti),"P")==0)
                     {
                      posFlag=1;
                      break;
                     }
                 }
             }
          if (getValue("parm.platform_kind_sel",inti).equals("2"))
             {
              posFlag=1;
              for (int intk=0;intk< cnt1;intk++)
                 {
                  if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"), "",
                                             getValue("parm.platform_kind_sel",inti),"P")!=0)
                     {
                      posFlag=0;
                      break;
                     }
                 }
             }
          if (pSeqno.length()>0)
             {
              if (posFlag==0)
                 showLogMessage("I",""," STEP 0330-00P6 資格不符 ");
              else
                 showLogMessage("I",""," STEP 0330-00P6 資格符合 ");
             }
          if (posFlag==0) continue;
         }
         parmArr[inti][8]++;
         if (selectPtrFundData(getValue("card_type"),
                                  getValue("parm.card_type_sel",inti),"5",3)!=0) continue;

         parmArr[inti][9]++;
         if (selectPtrFundData(getValue("source_code"),
                                  getValue("parm.source_code_sel",inti),"A",3)!=0) continue;

//       if (getValue("mcht_country").length()==3) 
//           setValue("mcht_country" , getValue("mcht_country").substring(0,2));

         currFlag=1;
         if ((!getValue("parm.ex_currency_sel",inti).equals("0"))||
             (!getValue("parm.currency_sel",inti).equals("0")))
         {
             currFlag=0;
             if (!getValue("parm.ex_currency_sel",inti).equals("0"))
             {
                if (selectPtrFundData(getValue("mcht_country"),getValue("source_curr"),getValue("mcht_category"),
                    getValue("parm.ex_currency_sel",inti),"9",2)==0) {
                	currFlag=1;
                //treat as Domain
                }else if (selectPtrFundData(getValue("mcht_country"),"","",
                          getValue("parm.ex_currency_sel",inti),"9",2)==0) 
                { 
                	      currFlag=1;            	 
                }
             }
             if (!getValue("parm.currency_sel",inti).equals("0"))
             {
                if (selectPtrFundData(getValue("mcht_country"),getValue("source_curr"),getValue("mcht_category"),
                                     getValue("parm.currency_sel",inti),"7",2)==0) 
                { 
                	currFlag=1;
                //treat as Domain
                }else if (selectPtrFundData(getValue("mcht_country"),"","",
                        getValue("parm.currency_sel",inti),"7",2)==0) 
                { 
              	        currFlag=1;            	 
                }                	
             }

         }

         parmArr[inti][10]++;
         if (currFlag==0) continue;

         parmArr[inti][11]++;
         if (selectPtrFundData(getValue("acct_type"),
                                  getValue("parm.acct_type_sel",inti),"4",3)!=0) continue;

         parmArr[inti][12]++;

         if (DEBUGAmt)
            {
             showLogMessage("I",""," STEP 001-1 ["+ getValue("parm.pos_entry_sel",inti) +"]");
             showLogMessage("I",""," STEP 001-2 ["+ getValue("parm.pos_merchant_sel",inti) +"]");
            }

         parmArr[inti][13]++;
         if (pSeqno.length()>0)
            showLogMessage("I",""," STEP 0330-00X1 pos_entry_sel ["+ getValue("parm.pos_entry_sel",inti) +"]");

         posFlag=0;
         if (!getValue("parm.pos_entry_sel",inti).equals("0")) 
            {
             if ((!getValue("parm.pos_merchant_sel",inti).equals("0"))||
                 (!getValue("parm.pos_mcht_group_sel",inti).equals("0")))
                {
                 setValue("aica.ica_no" ,  acqId);
                 cnt1 =  getLoadData("aica.ica_no");
                }
             if (pSeqno.length()>0)
                {
                 showLogMessage("I",""," STEP 0330-00F1 acq_id ["+ acqId +"]");
                 showLogMessage("I",""," STEP 0330-00F2 cnt1   ["+ cnt1 +"]");
                }

             if (cnt1==0)
                {
                 setValue("aica.bank_no","");
                 cnt1=1;
                }
             if (pSeqno.length()>0)
                {
                 showLogMessage("I",""," STEP 0330-00F3 pos_mcht_group_sel ["+ getValue("parm.pos_mcht_group_sel",inti) +"]");
                 showLogMessage("I",""," STEP 0330-00F4 mcht_no            ["+ getValue("mcht_no") +"]");
                }
             if (getValue("parm.pos_merchant_sel",inti).equals("1"))
                {
                 for (int intk=0;intk< cnt1;intk++)
                    {
                     if (pSeqno.length()>0)
                         showLogMessage("I",""," STEP 0330-00FA bank_no ["+ getValue("aica.bank_no",intk) +"]");

                      if (selectPtrFundData(getValue("mcht_no"),getValue("aica.bank_no",intk),
                                               getValue("parm.pos_merchant_sel",inti),"C",3)==0)
                        {
                         posFlag=1;
                         break;
                        }
                    }
                }
             if (posFlag==0)
             if (getValue("parm.pos_mcht_group_sel",inti).equals("1"))
                {
                 for (int intk=0;intk< cnt1;intk++)
                    {
                     fillBankNo = comm.fillZero(getValue("aica.bank_no",intk),8);
                     if (pSeqno.length()>0)
                         showLogMessage("I",""," STEP 0330-00GA bank_no ["+ fillBankNo +"]");

                     if (selectMktMchtgpData(getValue("mcht_no"), fillBankNo,
                                                getValue("parm.pos_mcht_group_sel",inti),"M")==0)
                        {
                         posFlag=1;
                         break;
                        }
                    }
                }

             if (posFlag==0)
                {
                 if (selectPtrFundData(getValue("bin_type"),getValue("pos_entry_mode"),getValue("ec_ind"),
                                          getValue("parm.pos_entry_sel",inti),"B",3)!=0) continue;
                 posFlag=0;

                 if (getValue("parm.pos_merchant_sel",inti).equals("2"))
                    {
                     for (int intk=0;intk< cnt1;intk++)
                         {
                         if (pSeqno.length()>0)
                             showLogMessage("I",""," STEP 0330-00FA bank_no ["+ getValue("aica.bank_no",intk) +"]");
                          if (selectPtrFundData(getValue("mcht_no"),getValue("aica.bank_no",intk),
                                                  getValue("parm.pos_merchant_sel",inti),"C",3)!=0)
                             {
                              posFlag=1;
                              break;
                             }
                         }
                    }
                 if (posFlag==1) continue;

                 posFlag=0;
                 if (getValue("parm.pos_mcht_group_sel",inti).equals("2"))
                    {
                     for (int intk=0;intk< cnt1;intk++)
                        {
                         fillBankNo = comm.fillZero(getValue("aica.bank_no",intk),8);
                         if (pSeqno.length()>0)
                             showLogMessage("I",""," STEP 0330-00GB fill_bank_no   ["+ fillBankNo +"]");
                         if (selectMktMchtgpData(getValue("mcht_no"), fillBankNo,
                                                    getValue("parm.pos_mcht_group_sel",inti),"M")!=0)
                            {
                             posFlag=1;
                             break;
                            }
                         }
                    }
                 if (posFlag==1) continue;
                }
             if (pSeqno.length()>0) showLogMessage("I",""," STEP 0330-00F6 資格符合 ");
            }
         parmArr[inti][14]++;

        if (getValue("parm.fund_feed_flag",inti).equals("Y")) 
        {
            parmArr[inti][15]++;
            feedCnt[inti]++;
            if (DEBUGAmt)
             {
               showLogMessage("I",""," STEP AMT-01 reference_no ["+getValue("reference_no")+"]");
               showLogMessage("I",""," STEP AMT-01 dest_amt     ["+getValueDouble("dest_amt")+"]");
             }

            if (getValue("parm.onlyaddon_calcond",inti).equals("Y"))
            {
            	int cnt_ok = 0 ;
            	String retID = "";            	
            	if (getValue("parm.hapcare_trust_cond",inti).equals("Y")){
            		retID = getCrdIdno(getValue("major_id_p_seqno")) ;
            		if (retID.equals("")) {
            			cnt_ok = 0 ;
            		}else {            			
	            		setValue("idlist.list_type" ,  "0101");
	            		setValue("idlist.id_no" ,  retID);
	            		cnt_ok = getLoadData("idlist.list_type,idlist.id_no");
            		}
            		if (cnt_ok > 0) {
	        			if (getValue("parm.purchase_type_sel",inti).equals("5") )	        				
		        		    insertCycAddonCalRow(inti,"0","0101",getValueDouble("parm.hapcare_trust_rate",inti) ,getValueInt("parm.happycare_fblmt",inti) );
		        	    else 
            		        insertCycAddonBill(inti,"0","0101",getValueDouble("parm.hapcare_trust_rate",inti) ,getValueInt("parm.happycare_fblmt",inti) );
            	    }       		
            	}
            	
            	if (getValue("parm.housing_endow_cond",inti).equals("Y")){
            		retID = getCrdIdno(getValue("major_id_p_seqno")) ;
            		if (retID.equals("")) {
            			cnt_ok = 0 ;
            		}else {            			
	            		setValue("idlist.list_type" ,  "0102");
	            		setValue("idlist.id_no" ,  retID);
	            		cnt_ok = getLoadData("idlist.list_type,idlist.id_no");
            		}
	        		if (cnt_ok > 0) {
	        			if (getValue("parm.purchase_type_sel",inti).equals("5") )	        				
			        		insertCycAddonCalRow(inti,"0","0102",getValueDouble("parm.housing_endow_rate",inti) ,getValueInt("parm.happycare_fblmt",inti) );
			        	else 
        		            insertCycAddonBill(inti,"0","0102",getValueDouble("parm.housing_endow_rate",inti) ,getValueInt("parm.happycare_fblmt",inti) );
	        	    }	        		
            	}
            	
            	if (getValue("parm.mortgage_cond",inti).equals("Y")){
            		retID = getCrdIdno(getValue("major_id_p_seqno")) ;
            		if (retID.equals("")) {
            			cnt_ok = 0 ;
            		}else {            			
	            		setValue("idlist.list_type" ,  "0201");
	            		setValue("idlist.id_no" ,  retID);
	            		cnt_ok = getLoadData("idlist.list_type,idlist.id_no");
            		}
	        		if (cnt_ok > 0) {
	        			if (getValue("parm.purchase_type_sel",inti).equals("5") )	        				
			        		insertCycAddonCalRow(inti,"0","0201",getValueDouble("parm.mortgag_rate",inti) ,getValueInt("parm.mortgage_fblmt",inti) );	
			        	else 
    		                insertCycAddonBill(inti,"0","0201",getValueDouble("parm.mortgag_rate",inti) ,getValueInt("parm.mortgage_fblmt",inti) );	        			
	        	    }	        		
            	}     
            	           	        	           	            	            	            	
            	if (getValue("parm.twpay_cond",inti).equals("Y")){
	        		if (getValue("ecs_platform_kind").equals("t1") || 
	        		    getValue("payment_type").equals("Q")	){
	        			if (getValue("parm.purchase_type_sel",inti).equals("5") )	        				
			        		insertCycAddonCalRow(inti,"0","1001",getValueDouble("parm.twpay_rate",inti) ,getValueInt("parm.eco_fblmt",inti) );
			        	else 
                            insertCycAddonBill(inti,"0","1001",getValueDouble("parm.twpay_rate",inti) ,getValueInt("parm.eco_fblmt",inti) );
	        	    }	        		
            	}             	
            	if (getValue("parm.tcblife_ec_cond",inti).equals("Y")){
	        		if (getValue("mcht_chi_name").contains("合庫人壽")){
	        			if (getValue("parm.purchase_type_sel",inti).equals("5") )	        				
			        		insertCycAddonCalRow(inti,"0","1002",getValueDouble("parm.tcblife_ec_rate",inti) ,getValueInt("parm.eco_fblmt",inti) );
			        	else 
                            insertCycAddonBill(inti,"0","1002",getValueDouble("parm.tcblife_ec_rate",inti) ,getValueInt("parm.eco_fblmt",inti) );
	        	    }	        		
            	}
            	continue; 
            	
            } else {            	
                if (getValue("parm.extratwpay_cond",inti).equals("Y")) {
	        		if (getValue("ecs_platform_kind").equals("t1") || 
			        	getValue("payment_type").equals("Q")){  
	        			if (getValue("parm.purchase_type_sel",inti).equals("5") )	        				
			               insertCycCalrowFund(inti);
	        			else 
	        			   insertPtrFundpBill(inti,"0");
	        		}
			        continue; 
                }                          
             
    			if (getValue("parm.purchase_type_sel",inti).equals("5") )
		           insertCycCalrowFund(inti);
     			else 
     			   insertPtrFundpBill(inti,"0");
                
            }//parm.onlyaddon_calcond
        }//parm.fund_feed_flag

        //不判斷加碼篩選條件 ,符合主參數條件就回饋      
        if (getValue("parm.purch_feed_flag",inti).equals("Y")) 
           {
            parmArr[inti][16]++;

            if (getValue("purchase_date").compareTo(getValue("parm.purch_date_s",inti))<0) continue;
            if (getValue("purchase_date").compareTo(getValue("parm.purch_date_e",inti))>0) continue;

            parmArr[inti][17]++;
            if (getValue("parm.purch_reclow_cond",inti).equals("Y")) 
               if (getValueDouble("parm.purch_reclow_amt",inti)>0)
//                  if (getValueDouble("dest_amt") < getValueDouble("parm.purch_reclow_amt",inti)) continue;
//refund adjust            	   
                   if (Math.abs(getValueDouble("dest_amt")) < getValueDouble("parm.purch_reclow_amt",inti)) continue;
            	   parmArr[inti][18]++;
            purchCnt[inti]++;
            insertPtrFundpBill(inti,"1");
           }
         parmArr[inti][19]++;
    }//for parmCnt

    if (feedbackType.equals("2"))
       processDisplay(100000); // every 10000 display message
    else
       processDisplay(200000); // every 10000 display message
   }//while bil_bill
  closeCursor();

  showLogMessage("I","","=========================================");
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );

  if (fundCode.length()!=0)
  for (int inti=0;inti<parmCnt;inti++)
     {
      if (getValue("parm.parmrun_flag",inti).equals("N")) continue;

      showLogMessage("I","","    ["+String.format("%03d",inti)
                           + "] 基金 [" + getValue("parm.fund_code",inti) 
                           + "] 處理日期[" + getValue("parm.proc_date",inti) 
                           + "] 消費型["+ getValue("parm.purch_feed_flag",inti) +"] ["+ purchCnt[inti] 
                           + "] 筆 , 門檻式["+ getValue("parm.fund_feed_flag",inti) +"] ["+ feedCnt[inti] +"] 筆" );
   
      if (fundCode.length()!=0)  
      for (int intk=0;intk<20;intk++)
        {
         if (parmArr[inti][intk]==0) continue;
         showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
        }
   
     }
  showLogMessage("I","","=========================================");  

  return(0);
 }
// ************************************************************************
int selectPtrFundp() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "ptr_fundp";
  whereStr  = "WHERE apr_flag    = 'Y' "
            + "AND   (stop_flag  = 'N' "
            + " OR    (stop_flag = 'Y' "
            + "  and   ? < stop_date)) "
            + "and tran_base = 'B'     "   // 'C' process in act_k001 == CycA350
            ;

  setString(1,businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
     }

  whereStr  = whereStr
            + "order by fund_code ";
       
  parmCnt = selectTable();

  showLogMessage("I","","參數檢核筆數 ["+ parmCnt + "] 筆" );
  int runInt =0;

  int n1Months=0,n2Months=0,n3Months=0;
  matchCnt=0;

  for (int inti=0;inti<parmCnt;inti++)
      {
       setValue("parm.parmrun_flag" , "N" ,inti);

       if (feedbackType.length()!=0)
          if (!getValue("parm.feedback_type",inti).equals(feedbackType)) continue;

       if (getValue("parm.fund_crt_date_s",inti).length()==0) setValue("parm.fund_crt_date_s", "20000101" ,inti);
       if (getValue("parm.fund_crt_date_e",inti).length()==0) setValue("parm.fund_crt_date_e", "30000101" ,inti);

       if (businessDate.compareTo(getValue("parm.fund_crt_date_s",inti))<0) continue;
       if (businessDate.compareTo(getValue("parm.fund_crt_date_e",inti))>0)
          {
           updatePtrFundp(inti,"產生期間到期, 系統自動設定停止");
           continue;
          }

       if (getValue("parm.card_feed_date_s",inti).length()==0) setValue("parm.card_feed_date_s", "20000101" ,inti);
       if (getValue("parm.card_feed_date_e",inti).length()==0) setValue("parm.card_feed_date_e", "30000101" ,inti);

       if (getValue("parm.purch_date_s",inti).length()==0) setValue("parm.purch_date_s", "20000101" ,inti);
       if (getValue("parm.purch_date_e",inti).length()==0) setValue("parm.purch_date_e", "30000101" ,inti);

       if (getValue("parm.unlimit_start_month",inti).length()==0) 
          setValue("parm.unlimit_start_month", "100001" ,inti);

       String ulmtSMonth = "";

       if (getValue("parm.program_exe_type",inti).equals("1"))
          {
           ulmtSMonth = getValue("parm.unlimit_start_month",inti);
          }
       else if (getValue("parm.program_exe_type",inti).equals("2"))
          {
           ulmtSMonth = getValue("parm.cal_s_month",inti);
          }

       if (getValue("parm.fund_feed_flag",inti).equals("Y"))
          if (getValueDouble("parm.fund_s_amt_1",inti)>getValueDouble("parm.fund_e_amt_1",inti))
             {
              if (!getValue("parm.purch_feed_flag",inti).equals("Y")) continue;
              setValue("parm.fund_feed_flag" , "N" ,inti);
             }
               
       if (getValue("parm.feedback_type",inti).equals("1"))
          {
           if (getValueInt("parm.card_feed_run_day",inti)==0)
               setValueInt("parm.card_feed_run_day",inti,1);

           if (getValueInt("parm.feedback_months",inti)==0)
               setValueInt("parm.feedback_months",inti,1);

           if (getValueInt("parm.feedback_months",inti)<=1)
              ulmtSMonth = businessDate.substring(0,6);

           if (businessDate.substring(0,6).compareTo(ulmtSMonth)>0)
              {
               n1Months = (int)Math.ceil(comm.monthBetween(ulmtSMonth , businessDate)
                         / getValueInt("parm.feedback_months",inti) *1.0 - 0.0001)
                         * getValueInt("parm.feedback_months",inti);
               n2Months = (int)Math.ceil(comm.monthBetween(ulmtSMonth , businessDate)
                         / getValueInt("parm.feedback_months",inti)*1.0 - 0.0001)
                         * getValueInt("parm.feedback_months",inti)
                         - getValueInt("parm.feedback_months",inti);
               n3Months = (int)Math.ceil(comm.monthBetween(ulmtSMonth , businessDate)
                         / getValueInt("parm.feedback_months",inti)*1.0 - 0.0001)
                         * getValueInt("parm.feedback_months",inti)
                         - 1;
              }
           else
              {
               if (comm.monthBetween(ulmtSMonth , businessDate)==0)
                  n1Months = 0;
               else
                   n1Months  
                         = (int)Math.ceil(comm.monthBetween(ulmtSMonth , businessDate)
                         / getValueInt("parm.feedback_months",inti)*1.0 + 0.0001)
                         * getValueInt("parm.feedback_months",inti);

               n2Months = (int)Math.ceil(comm.monthBetween(ulmtSMonth , businessDate)
                         / getValueInt("parm.feedback_months",inti)*1.0 + 0.0001)
                         * getValueInt("parm.feedback_months",inti)
                         - getValueInt("parm.feedback_months",inti);
               n3Months = (int)Math.ceil(comm.monthBetween(ulmtSMonth , businessDate)
                         / getValueInt("parm.feedback_months",inti)*1.0 + 0.0001)
                         * getValueInt("parm.feedback_months",inti)
                         - 1;
              }

           setValue("parm.temp_start_month" , comm.nextMonth(ulmtSMonth , n1Months) , inti);
           setValue("parm.cal_s_month"      , comm.nextMonth(ulmtSMonth , n2Months) , inti); 
           setValue("parm.cal_e_month"      , comm.nextMonth(ulmtSMonth , n3Months) , inti);
            
          }
       else
          {
           setValue("parm.temp_start_month" , "200001" ,inti);

           if (getValue("parm.program_exe_type",inti).equals("1"))
              { 
               setValue("parm.cal_s_month"  , ulmtSMonth , inti); 
               setValue("parm.cal_e_month"  , "30001231"   , inti);
              } 
           else if (!getValue("parm.program_exe_type",inti).equals("2"))
              { 
               setValue("parm.cal_s_month"  , "" , inti); 
               setValue("parm.cal_e_month"  , "" , inti);
              } 
          }
       if (getValue("parm.program_exe_type",inti).equals("2"))
          { 
           if (businessDate.substring(0,6).compareTo(getValue("parm.cal_e_month",inti))>0) 
              {
               updatePtrFundp(inti,"回饋一段期間到期, 系統自動設定停止");
               continue;
              }
          }
       if (getValue("parm.program_exe_type",inti).equals("3"))
          { 
           if (getValue("parm.card_feed_flag",inti).equals("3"))
              {
               if (comm.nextNDate(getValue("parm.card_feed_date_e",inti),
                                  getValueInt("parm.card_feed_days",inti)).compareTo(businessDate)<0)
                  {
                   showLogMessage("I","","基金代碼 ["+ getValue("parm.fund_code",inti) + "] "
                                        + "效期 ["+ comm.nextNDate(getValue("parm.card_feed_date_e",inti),
                                                                   getValueInt("parm.card_feed_days",inti))
                                        + "到期, 系統自動設定停止日期");
                   updatePtrFundp(inti,"發卡期間到期, 系統自動設定停止");
                   showLogMessage("I","","=========================================");  
                   continue;
                  }
              }
          }
           
       if (getValue("parm.feedback_type",inti).equals("1"))
          {
           if (!businessDate.substring(6,8).equals
               (String.format("%02d",getValueInt("parm.card_feed_run_day",inti)))) continue;

           setValue("parm.proc_date" ,getValue("parm.temp_start_month",inti)+
                            String.format("%02d",getValueInt("parm.card_feed_run_day",inti)),inti);
                
           if (fundCode.length()!=0)                        
              showLogMessage("I","","執行日期  ["+ getValue("parm.proc_date",inti) + "] ");

           if (getValue("parm.program_exe_type",inti).equals("2"))
           if (getValue("parm.proc_date",inti).substring(0,6).compareTo(
               getValue("parm.cal_e_month",inti))>0) 
              {
               updatePtrFundp(inti,"回饋方式執行日期到期, 系統自動設定停止");
               continue;
              }
           setValue("wday.this_acct_month" , comm.lastMonth(businessDate,1));
           setValue("parm.acct_month", getValue("wday.this_acct_month") , inti);
//         if (getValue("parm.feedback_cycle_flag",inti).equals("1")) cycleCnt++;
          }
       else
          {
           if (selectPtrWorkday()!=0)
              {
               setValue("wday.this_acct_month" , businessDate.substring(0,6)); 
               setValue("wday.stmt_cycle"      , businessDate.substring(6,8));
              }
           minAcctMonth = getValue("wday.this_acct_month");

           if (getValue("parm.program_exe_type",inti).equals("2"))
              if ((getValue("wday.this_acct_month").compareTo(getValue("parm.cal_s_month",inti))<0)||
                  (getValue("wday.this_acct_month").compareTo(getValue("parm.cal_e_month",inti))>0)) continue;

           runInt++;
//         showLogMessage("I","","["+String.format("%03d",run_int)+"]基金代碼 : ["+ getValue("parm.fund_code",inti) 
//                              + "] ~ cycle ["+ getValue("wday.stmt_cycle") +"]");

           setValue("parm.proc_date", businessDate , inti);
           setValue("parm.acct_month", getValue("wday.this_acct_month") , inti);
          }

       setValue("parm.parmrun_flag" , "Y" ,inti);
   
       matchCnt++;
       deleteCycPurchFund(inti);
       deleteCycCalFund(inti);
       deleteCycCalrowFund(inti);
       
       deleteCycAddonCalFund(inti);
       deleteCycAddonCalRowSum(inti);       
      }
  if (fundCode.length()!=0)
     showLogMessage("I","","基金 : ["+ getValue("parm.fund_code") + "-" +  getValue("parm.fund_name") +"]");
  showLogMessage("I","","參數符合筆數 ["+ matchCnt + "] 筆" );
  return(0);
 }
// ************************************************************************
 int selectPtrFundpBill() throws Exception
 {
  boolean DEBUG02 = false;
  selectSQL = "major_card_no," 
            + "fund_code,"
            + "max(p_seqno) as p_seqno,"
            + "major_id_p_seqno";
  daoTable  = "ptr_fundp_bill";
  whereStr  = "where 1 = 1 "
            ;
            
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

  whereStr  = whereStr
            + "group by major_card_no,major_id_p_seqno,fund_code";

  openCursor();

  totalCnt=0;
  int matchCnt=0;
  int inti = 0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    for (int intm=0;intm<parmCnt;intm++)
      {
       if (!getValue("fund_code").equals(getValue("parm.fund_code",intm))) continue;

       inti = intm;
       break;
      }

    if (DEBUG02)
       showLogMessage("I","","STEP 02-1 ["+ getValue("fund_code")+"] activate["+getValue("parm.activate_cond",inti) + "]");
    if (getValue("parm.activate_cond",inti).equals("Y"))
       {
    if (DEBUG02)
       showLogMessage("I","","STEP 02-2 ["+ getValue("major_card_no") +"]" );
        setValue("cact.major_card_no" ,  getValue("major_card_no"));
        cnt1 =  getLoadData("cact.major_card_no");
    if (DEBUG02)
       showLogMessage("I","","STEP 02-2A ["+cnt1+"]" );
        if (cnt1==0)
           {
    if (DEBUG02)
       showLogMessage("I","","STEP 02-3 " );
            updatePtrFundpBill(inti,"01");
            continue;
           }
        int okFlag=0;
        if (getValue("parm.activate_flag",inti).equals("1"))
// rt   if (getValue("parm.activate_flag",inti).equals("0"))
           {
            if (getValueInt("cact.card_cnt1") +
                getValueInt("cact.card_cnt3") ==0) okFlag=1;
            else if (getValueInt("cact.card_cnt1") !=
                     getValueInt("cact.card_cnt2")) okFlag=1;
            else if (getValueInt("cact.card_cnt3") !=
                     getValueInt("cact.card_cnt4")) okFlag=1;
           }
        else if (getValue("parm.activate_flag",inti).equals("2"))
// rt   else if (getValue("parm.activate_flag",inti).equals("1"))
           {
            if (getValueInt("cact.card_cnt1")==0) okFlag=1;
            else if (getValueInt("cact.card_cnt1") !=
                     getValueInt("cact.card_cnt2")) okFlag=1;
           }
        else if (getValue("parm.activate_flag",inti).equals("3"))
// rt   else if (getValue("parm.activate_flag",inti).equals("2"))
           {
            if (getValueInt("cact.card_cnt2") +
                getValueInt("cact.card_cnt4")==0) okFlag=1;
           }
        if (okFlag==1)
           {
            updatePtrFundpBill(inti,"02");
            continue;
           }
       }
    if (DEBUG02)
       showLogMessage("I","","STEP 02-4 " );

    if ((getValue("parm.apply_age_cond",inti).equals("Y"))||
        (getValue("parm.new_hldr_cond",inti).equals("Y")))
       {
        setValue("cori.card_no" ,  getValue("major_card_no"));
        cnt1 =  getLoadData("cori.card_no");
        if (cnt1==0)
           {
            showLogMessage("I","","MAJOR_CARD_NO ["+ getValue("major_card_no") + "] 不存在" );
            continue;
           }
       }

    // ************  處理年輕族群 ************
    if (getValue("parm.apply_age_cond",inti).equals("Y"))
       {
        setValue("idno.id_p_seqno" ,  getValue("major_id_p_seqno"));
        cnt1 =  getLoadData("idno.id_p_seqno");
        if (cnt1==0)
           {
            showLogMessage("I","","MAJOR_ID_P_SEQNO ["+ getValue("major_id_p_seqno") + "] 不存在" );
            continue;
           }

        int applyAge = (int)Math.floor(comm.monthBetween(
                         getValue("idno.birthday"),getValue("cori.ori_issue_date"))/12.0);

        if ((applyAge< getValueInt("parm.apply_age_s",inti))||
            (applyAge> getValueInt("parm.apply_age_e",inti)))
           {
            updatePtrFundpBill(inti,"03");
            continue;
           }
       }
     // ************ 新卡友判斷 *******************
     if (getValue("parm.new_hldr_cond",inti).equals("Y"))
        {
         if (getValue("parm.new_hldr_flag",inti).equals("1"))
            {
             if (selectCrdCard2(inti,0)!=0)
                {
                 updatePtrFundpBill(inti,"04");
                 continue;
                }
             if (selectCrdCard2(inti,1)!=0)
                {
                 updatePtrFundpBill(inti,"04");
                 continue;
                }
            }
         else if (getValue("parm.new_hldr_flag",inti).equals("2"))
            {
             if (selectCrdCard1(inti,0)!=0)
                {
                 updatePtrFundpBill(inti,"04");
                 continue;
                }
             if (getValueInt("parm.new_card_days",inti)<9999)
             if (selectCrdCard1(inti,1)!=0)
                {
                 updatePtrFundpBill(inti,"04");
                 continue;
                }
            }
        }
    matchCnt++;

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆, 合格 ["+matchCnt+"] 筆" );

  return(0);
 }
// ************************************************************************
  int selectPtrFundpBill1() throws Exception
 {
  boolean DEBUG03 = false;

  selectSQL = "a.fund_code," 
            + "a.fund_type," 
            + "a.acct_month," 
            + "a.reference_no,"
            + "a.p_seqno,"
            + "a.card_no," 
            + "a.major_card_no," 
            + "a.id_p_seqno," 
            + "a.major_id_p_seqno,"
            + "a.purchase_date,"
            + "a.dest_amt,"
            + "a.rowid as rowid";
  daoTable  = "ptr_fundp_bill a";
  whereStr  = "where a.error_code = '00' "
            + "and   (a.program_exe_type = '3' "
            + " or    a.fund_type        = '1') "
            ;
            
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

  int matchFlag=0;
  int[] hideCnt  = new int[parmCnt];

  for (int inti=0;inti<parmCnt;inti++) hideCnt[inti] = 0;

  int inti = 0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    for (int intm=0;intm<parmCnt;intm++)
        {
         if (!getValue("fund_code").equals(getValue("parm.fund_code",intm))) continue;
         inti = intm;
         break;
        }

    if (getValue("parm.program_exe_type",inti).equals("3"))
       {
        if (getValue("parm.card_feed_flag",inti).equals("4"))
           {
            setValue("cori.card_no" ,  getValue("major_card_no"));
            cnt1 =  getLoadData("cori.card_no");
           }
        else
           {
            setValue("cori.card_no" ,  getValue("card_no"));
            cnt1 =  getLoadData("cori.card_no");
           }

        if (cnt1==0)
           {
            showLogMessage("I","","卡號 ["+ getValue("cori.card_no") + "] 不存在" );
            continue;
           }
        if (DEBUG03)  
           {
            showLogMessage("I","","卡號 ["+ getValue("cori.card_no") + "]" );
            showLogMessage("I","","   STEP A01  ori_issue_date  [" + getValue("cori.ori_issue_date") + "]");
            showLogMessage("I","","   STEP A02  card_feed_date_s[" + getValue("parm.card_feed_date_s",inti) + "]");
            showLogMessage("I","","   STEP A03  card_feed_date_e[" + getValue("parm.card_feed_date_e",inti) + "]");
            showLogMessage("I","","   STEP A04  card_feed_flag  [" + getValue("parm.card_feed_flag",inti) + "]");
           }

        if (!getValue("parm.card_feed_flag",inti).equals("4"))
           {
            if (getValue("cori.ori_issue_date").compareTo(getValue("parm.card_feed_date_s",inti))<0)
               {
                updatePtrFundpBill("05");
                continue;
               }
            if (getValue("cori.ori_issue_date").compareTo(getValue("parm.card_feed_date_e",inti))>0)
               {
                updatePtrFundpBill("06");
                continue;
               }
           }

        if (getValue("parm.card_feed_flag",inti).equals("1"))
           {
            if (getValue("acct_month").compareTo(comm.nextMonth(getValue("cori.ori_issue_date"),
                                       getValueInt("parm.cal_months",inti)*-1))<=0)
               {
                updatePtrFundpBill("07");
                continue;
               }
           }
        else if (getValue("parm.card_feed_flag",inti).equals("2"))
           {
            if (getValue("acct_month").compareTo(comm.nextMonth(getValue("cori.ori_issue_date"),
                                       getValueInt("parm.card_feed_months2",inti)-1))>0)
               {
                updatePtrFundpBill("08");
                continue;
               }
           }
        else if (getValue("parm.card_feed_flag",inti).equals("3"))
           {
            if (DEBUG03)
               {
                showLogMessage("I","","   STEP A02  ori_issue_date  [" + getValue("cori.ori_issue_date") + "]");
                showLogMessage("I","","   STEP A02  compare         [" + getValueInt("parm.card_feed_days",inti) + "]");
                showLogMessage("I","","   STEP A02  last_date       [" + comm.nextNDate(getValue("cori.ori_issue_date"),
                                                                         getValueInt("parm.card_feed_days",inti)-1) +"]");
               }
// sl check if (business_date.compareTo(comm.nextNDate(getValue("cori.ori_issue_date"),
//                                   getValueInt("parm.card_feed_days",inti)-1))>0)
            if (getValue("purchase_date").compareTo(comm.nextNDate(getValue("cori.ori_issue_date"),
                                     getValueInt("parm.card_feed_days",inti)-1))>0)
               {
                updatePtrFundpBill("09");
                continue;
               }
           }
        if ((getValue("parm.new_hldr_sel",inti).equals("1"))||
            (getValue("parm.new_hldr_sel",inti).equals("2")))
           {
            matchFlag = 0;
            if (selectCrdCard4(inti)!=0) matchFlag=1;
             
            if (matchFlag==0)
               if (selectCrdCard3(inti,0)!=0) matchFlag=2; 

            if (matchFlag==0)
               if (selectCrdCard3(inti,1)!=0) matchFlag=3; 
            
            if  ((getValue("parm.new_hldr_sel",inti).equals("1"))&&
                 (matchFlag!=0))
               {
                updatePtrFundpBill("09");
                continue;
               }

            if  ((getValue("parm.new_hldr_sel",inti).equals("2"))&&
                 (matchFlag==0))
               {
                updatePtrFundpBill("10");
                continue;
               }
           }
       }

    if (getValue("fund_type").equals("1"))
       {
        if (getValue("parm.purch_rec_amt_cond",inti).equals("Y"))
//         if (getValueInt("parm.purch_rec_amt",inti) > Math.abs((getValueInt("dest_max"))))
//error field adjust 
           if (getValueInt("parm.purch_rec_amt",inti) > Math.abs((getValueInt("dest_amt"))))        	   
              {
                updatePtrFundpBill(inti,"11"); 
                continue;
              }
        insertCycPurchFund();
       }

    hideCnt[inti]++;

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );
/*
  for (int inti=0;inti<parmCnt;inti++)
     {
      showLogMessage("I","","    基金 ["+ getValue("parm.fund_code",inti) 
                           + "] 筆 , 符合 ["+ hide_cnt[inti] +"] 筆" );
     }
*/
  return(0);
 }
// ************************************************************************
 int selectPtrFundpBill2() throws Exception
 {
  selectSQL = "proc_date,"
            + "proc_type,"
            + "p_seqno,"
            + "a.fund_code,"
            + "acct_month,"
            + "sum(dest_amt) as dest_amt,"
            + "max(b.feedback_type) as feedback_type,"
            + "max(acct_type) as acct_type,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(major_card_no) as major_card_no";
  daoTable  = "ptr_fundp_bill a,ptr_fundp b";
  whereStr  = "where fund_type  = '0' "
            + "and   a.fund_code = b.fund_code "
            + "and   error_code = '00' "
            ;
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
            + "group by proc_date,proc_type,a.fund_code,p_seqno,acct_month "
            + "having sum(dest_amt) != 0 ";
            
  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;
    insertCycCalFund();

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );

  return(0);
 }
// ************************************************************************
 int selectPtrFundpBill3() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.reference_no,"
            + "a.fund_code,"
            + "a.acct_month,"
            + "a.dest_amt,"
            + "a.acct_type,"
            + "a.major_card_no,"
            + "a.card_no";
  daoTable  = "ptr_fundp_bill a,ptr_fundp b";
  whereStr  = "where a.error_code = '00' "
            + "and   a.pos_entry_sel = '1' "
            + "and   a.fund_code     = b.fund_code "
            + "and   b.valid_period not in ('E','S') "
            ;
            
  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   a.fund_code = ? ";
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

  while( fetchTable() ) 
   { 
    totalCnt++;
    insertCycPosEntry();

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );

  return(0);
 }
//************************************************************************ 
// int selectCycAddonBill_1() throws Exception { 
// return(0); 
//}
 
//************************************************************************ 
 int selectCycAddonBill_2() throws Exception
 {
  selectSQL = "proc_date , "
            + "p_seqno , "
            + "a.fund_code , "
            + "max(a.fund_type) as fund_type , "           
            + "acct_month , "
            + "group_addoncode , " 
            + "max(group_cblimit) as group_cblimit ," 
            + "max( " 
            + "case when substring(addon_code ,3, 2 )  = '01' "
            + "     then nvl(addon_code, '') "
            + "     else '' "
            + "end ) as addon_code1 ,"            
            + "sum( " 
            + "case when substring(addon_code ,3, 2 )  = '01' "
            + "     then nvl(addoncode_destamt, 0) "
            + "     else 0 "
            + "end ) as addoncode1_destamt ,"
            + "max( "
            + "case when substring(addon_code ,3, 2 )  = '01' "
            + "     then nvl( addoncode_rate , 0) "
            + "else 0 "
            + "end ) as addoncode1_rate , "
            + "max( " 
            + "case when substring(addon_code ,3, 2 )  = '02' "
            + "     then nvl(addon_code, '') "
            + "     else '' "
            + "end ) as addon_code2 ,"               
            + "sum( "
            + "case when substring(addon_code ,3, 2 )  = '02' "
            + "     then nvl( addoncode_destamt, 0)	"
            + "else 0 "
            + "end ) as addoncode2_destamt , "
            + "max( "
            + "case when substring(addon_code ,3, 2 )  = '02' "
            + "     then nvl( addoncode_rate , 0) "
            + "else 0 "
            + "end ) as addoncode2_rate  ,"            
            + "max(b.feedback_type) as feedback_type , "
            + "max(acct_type) as acct_type , "
            + "max(id_p_seqno) as id_p_seqno  , "
            + "max(major_id_p_seqno) as major_id_p_seqno , "  
            + "max(major_card_no) as major_card_no , "          
            + "max(card_no) as card_no , "
            + "max(group_code) as group_code " 
            ;
  
  daoTable  = "cyc_addon_bill a , ptr_fundp b";
  whereStr  = "where fund_type  = '0' "
            + "and   a.fund_code = b.fund_code "
            + "and   error_code = '00' " ;
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
            + "having sum(addoncode_destamt) != 0 ";
            
  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;
    insertCycAddonCalfund();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
  showLogMessage("I","","處理cyc_addon_bill產生cyc_addon_calfund筆數["+ totalCnt + "] 筆");

  return(0);
 } 
 
//************************************************************************  
//int selectCycAddonBill_3() throws Exception { } 
//return(0); 
//}
 
// ************************************************************************
 int insertCycPurchFund() throws Exception
 {
  dateTime();

  setValue("puch.proc_date"       , businessDate);
  setValue("puch.proc_type"       , "1");
  setValue("puch.p_seqno"         , getValue("p_seqno"));
  setValue("puch.fund_code"       , getValue("fund_code"));
  setValue("puch.acct_month"      , getValue("acct_month"));
  setValue("puch.acct_type"       , getValue("acct_type"));
  setValue("puch.id_p_seqno"      , getValue("id_p_seqno"));
  setValue("puch.stmt_cycle"      , businessDate.substring(6,8));
  setValue("puch.reference_no"    , getValue("reference_no"));
  setValue("puch.major_card_no"   , getValue("cori.end_card_no"));
  setValue("puch.dest_amt"        , getValue("dest_amt"));
  setValue("puch.stmt_cycle"      , getValue("wday.stmt_cycle"));
  setValue("puch.proc_mark"       , "N");
  setValue("puch.mod_time"        , sysDate+sysTime);
  setValue("puch.mod_pgm"         , javaProgram);

  extendField = "puch.";
  daoTable  = "cyc_purch_fund";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertCycCalFund() throws Exception
 {
  dateTime();

  setValue("ccal.proc_date"       , getValue("proc_date"));
  setValue("ccal.proc_type"       , getValue("proc_type"));
  setValue("ccal.p_seqno"         , getValue("p_seqno"));
  setValue("ccal.fund_code"       , getValue("fund_code"));
  setValue("ccal.acct_month"      , getValue("acct_month"));
  setValue("ccal.acct_type"       , getValue("acct_type"));
  setValue("ccal.group_code"      , "");
  setValue("ccal.major_card_no"   , getValue("major_card_no"));
  setValue("ccal.stmt_cycle"      , "");
  if (getValue("feedback_type").equals("2"))
     setValue("ccal.stmt_cycle"      , businessDate.substring(6,8));
  setValue("ccal.dest_amt"        , getValue("dest_amt"));
  setValue("ccal.proc_mark"       , "N");
  setValue("ccal.mod_time"        , sysDate+sysTime);
  setValue("ccal.mod_pgm"         , javaProgram);

  extendField = "ccal.";
  daoTable  = "cyc_cal_fund";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertCycCalrowFund(int inti) throws Exception
 {
  dateTime();

  setValue("ccal.proc_date"       , getValue("parm.proc_date",inti)); 
  if (getValue("parm.feedback_type",inti).equals("2"))  // cycle
     setValue("ccal.proc_type"    , "1");
  else
     setValue("ccal.proc_type"    , "3");
  setValue("ccal.p_seqno"         , getValue("p_seqno"));
  setValue("ccal.id_p_seqno"      , getValue("id_p_seqno"));
  setValue("ccal.reference_no"    , getValue("reference_no"));
  setValue("ccal.fund_code"       , getValue("parm.fund_code",inti));
  setValue("ccal.group_code"      , getValue("group_code"));
  setValue("ccal.acct_month"      , getValue("acct_month"));
  setValue("ccal.dest_amt"        , getValue("dest_amt"));
  setValue("ccal.acct_type"       , getValue("acct_type"));
  setValue("ccal.id_p_seqno"      , getValue("id_p_seqno"));
  setValue("ccal.major_card_no"   , getValue("major_card_no"));
  setValue("ccal.stmt_cycle"      , businessDate.substring(6,8));
  setValue("ccal.proc_mark"       , "N");
  setValue("ccal.mod_time"        , sysDate+sysTime);
  setValue("ccal.mod_pgm"         , javaProgram);

  extendField = "ccal.";
  daoTable  = "cyc_calrow_fund";

  insertTable();

  return(0);
 }
//************************************************************************
//to-do 
int insertCycAddonBill(int inti , String fundType , String addonCode ,double addoncodeRate , int groupCblimit) throws Exception
{
dateTime();

setValue("addonbill.fund_type"       , fundType);
setValue("addonbill.proc_date"       , getValue("parm.proc_date",inti));

//if (getValue("parm.feedback_type",inti).equals("2"))  // cycle
//  setValue("addonbill.proc_type"    , "1");
//else
//  setValue("addonbill.proc_type"    , "3");

setValue("addonbill.p_seqno"          , getValue("p_seqno"));
setValue("addonbill.reference_no"     , getValue("reference_no"));
setValue("addonbill.fund_code"        , getValue("parm.fund_code",inti));
setValue("addonbill.acct_month"       , getValue("parm.acct_month",inti));
//setValue("addonbill.dest_amt"         , getValue("dest_amt"));
setValue("addonbill.acct_type"        , getValue("acct_type"));
setValue("addonbill.id_p_seqno"       , getValue("id_p_seqno"));
setValue("addonbill.major_id_p_seqno" , getValue("major_id_p_seqno"));

setValue("addonbill.group_addoncode"  , addonCode.substring(0,2) );
setValueInt("addonbill.group_cblimit"    , groupCblimit);
setValue("addonbill.addon_code"       , addonCode);
setValueDouble("addonbill.addoncode_destamt", getValueDouble("dest_amt") );
setValueDouble("addonbill.addoncode_rate"   , addoncodeRate );
setValueInt("addonbill.addoncode_cashback", 0);

setValue("addonbill.major_card_no"    , getValue("major_card_no"));
setValue("addonbill.group_code"       , getValue("group_code"));
setValue("addonbill.card_no"          , getValue("card_no"));
setValue("addonbill.purchase_date"    , getValue("purchase_date"));
setValue("addonbill.pos_entry_sel"    , getValue("parm.pos_entry_sel",inti));
setValue("addonbill.program_exe_type" , getValue("parm.program_exe_type",inti));
setValue("addonbill.error_code"       , "00");
setValue("addonbill.mod_time"         , sysDate+sysTime);
setValue("addonbill.mod_pgm"          , javaProgram);

extendField = "addonbill.";
daoTable  = "cyc_addon_bill";

insertTable();

return(0);
}
//************************************************************************
//to-do
 int insertCycAddonCalfund() throws Exception
 {
  dateTime();

  setValue("addoncal.proc_date"       , getValue("proc_date"));
  setValue("addoncal.acct_month"      , getValue("acct_month"));  
  setValue("addoncal.fund_code"       , getValue("fund_code"));
  setValue("addoncal.fund_type"       , getValue("fund_type"));  
  setValue("addoncal.p_seqno"         , getValue("p_seqno"));
  setValue("addoncal.acct_type"       , getValue("acct_type"));
  setValue("addoncal.major_id_p_seqno", getValue("major_id_p_seqno"));  
  setValue("addoncal.id_p_seqno"      , getValue("id_p_seqno"));
  
  setValue("addoncal.group_addoncode" , getValue("group_addoncode"));  
  setValueInt("addoncal.group_cblimit", getValueInt("group_cblimit"));
  setValueInt("addoncal.group_cashback", 0);    
  setValue("addoncal.addon_code1"       , getValue("addon_code1"));
  setValueDouble("addoncal.addoncode1_destamt", getValueDouble("addoncode1_destamt") );
  setValueDouble("addoncal.addoncode1_rate"   , getValueDouble("addoncode1_rate")  );
  setValueInt("addoncal.addoncode1_cashback", 0);  
  setValue("addoncal.addon_code2"       , getValue("addon_code2"));
  setValueDouble("addoncal.addoncode2_destamt", getValueDouble("addoncode2_destamt") );
  setValueDouble("addoncal.addoncode2_rate"   , getValueDouble("addoncode2_rate")  );
  setValueInt("addoncal.addoncode2_cashback", 0);    

  setValue("addoncal.major_card_no"   , "");
  setValue("addoncal.group_code"      , "");  
  setValue("addoncal.proc_mark"       , "N");
  setValue("addoncal.mod_user"         , javaProgram);  
  setValue("addoncal.mod_time"        , sysDate+sysTime);
  setValue("addoncal.mod_pgm"         , javaProgram);

  extendField = "addoncal.";
  daoTable  = "cyc_addon_calfund";

  insertTable();

  return(0);
 }
 
//************************************************************************
//to-do
int insertCycAddonCalRow(int inti , String fundType , String addonCode ,double addoncodeRate , int groupCblimit) throws Exception
{
dateTime();

setValue("addoncr.proc_date"       , getValue("parm.proc_date",inti)); 
setValue("addoncr.acct_month"      , getValue("acct_month"));
setValue("addoncr.reference_no"    , getValue("reference_no"));
//if (getValue("parm.feedback_type",inti).equals("2"))  // cycle
//   setValue("addoncr.proc_type"    , "1");
//else
//   setValue("addoncr.proc_type"    , "3");
setValue("addoncr.fund_code"       , getValue("parm.fund_code",inti));
setValue("addoncr.fund_type"       , fundType);
setValue("addoncr.p_seqno"         , getValue("p_seqno"));
setValue("addoncr.acct_type"       , getValue("acct_type"));
setValue("addoncr.major_id_p_seqno", getValue("major_id_p_seqno"));
setValue("addoncr.id_p_seqno"      , getValue("id_p_seqno"));

//setValue("addoncr.dest_amt"        , getValue("dest_amt"));
setValue("addoncr.group_addoncode"  , addonCode.substring(0,2) );
setValueInt("addoncr.group_cblimit"    , groupCblimit);
setValue("addoncr.addon_code"       , addonCode);
setValueDouble("addoncr.addoncode_destamt", getValueDouble("dest_amt") );
setValueDouble("addoncr.addoncode_rate"   , addoncodeRate );
setValueInt("addoncr.addoncode_cashback", 0);

setValue("addoncr.major_card_no"   , getValue("major_card_no"));
setValue("addoncr.card_no"   , getValue("card_no"));
setValue("addoncr.group_code"      , getValue("group_code"));
//setValue("addoncr.stmt_cycle"      , businessDate.substring(6,8));
setValue("addoncr.purchase_date"    , getValue("purchase_date"));
setValue("addoncr.pos_entry_sel"    , getValue("parm.pos_entry_sel",inti));
setValue("addoncr.proc_mark"       , "N");
setValue("addoncr.mod_user"        , javaProgram);
setValue("addoncr.mod_time"        , sysDate+sysTime);
setValue("addoncr.mod_pgm"         , javaProgram);

extendField = "addoncr.";
daoTable  = "cyc_addon_calrow";

insertTable();

return(0);
} 
 
// ************************************************************************
 void deleteCycCalFund(int inti) throws Exception
 {
  daoTable  = "cyc_cal_fund";
  whereStr  = "where  proc_date    = ? "
            + "and    proc_type    = ? "
            + "and    fund_code    = ? "
            + "and    acct_month   = ? "
            + "and    proc_mark    = 'N' ";

  setString(1, getValue("parm.proc_date",inti));
  if (getValue("parm.feedback_type",inti).equals("2"))
     setString(2, "1");
  else setString(2, "3");
  setString(3, getValue("parm.fund_code",inti));
  setString(4, getValue("wday.this_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr
                + "and   p_seqno = ? ";
      setString(5, pSeqno);
     }

  int  n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete ["+getValue("parm.fund_code",inti)+"] cyc_cal_fund [" + n + "] records");

  return;
 }
// ************************************************************************
 void deleteCycCalrowFund(int inti) throws Exception
 {
  daoTable  = "cyc_calrow_fund";
  whereStr  = "where  proc_date    = ? "
            + "and    proc_type    = ? "
            + "and    fund_code    = ? "
            + "and    acct_month   = ? "
            + "and    proc_mark    = 'N' ";

  setString(1, getValue("parm.proc_date",inti));
  if (getValue("parm.feedback_type",inti).equals("2"))
     setString(2, "1");
  else setString(2, "3");
  setString(3, getValue("parm.fund_code",inti));
  setString(4, getValue("wday.this_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr
                + "and   p_seqno = ? ";
      setString(5, pSeqno);
     }

  int  n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete ["+getValue("parm.fund_code",inti)+"] cyc_calrow_fund [" + n + "] records");

  return;
 }
 
//************************************************************************
void deleteCycAddonCalFund(int inti) throws Exception
{
daoTable  = "cyc_addon_calfund";
whereStr  = "where  proc_date    = ? "
          //+ "and    proc_type    = ? "
          + "and    fund_code    = ? "
          + "and    acct_month   = ? "
          + "and    proc_mark    = 'N' ";

setString(1, getValue("parm.proc_date",inti));
//if (getValue("parm.feedback_type",inti).equals("2"))
//   setString(2, "1");
//else setString(2, "3");
setString(2, getValue("parm.fund_code",inti));
setString(3, getValue("wday.this_acct_month"));

if (pSeqno.length()!=0)
   {
    whereStr  = whereStr
              + "and   p_seqno = ? ";
    setString(4, pSeqno);
   }

int  n = deleteTable();

if (n>0) 
   showLogMessage("I","","Delete ["+getValue("parm.fund_code",inti)+"] cyc_addon_calfund [" + n + "] records");

return;
}

//************************************************************************
void deleteCycAddonCalRowSum(int inti) throws Exception
{
daoTable  = "cyc_addon_calrowsum";
whereStr  = "where  proc_date    = ? "
          //+ "and    proc_type    = ? "
          + "and    fund_code    = ? "
          + "and    acct_month   = ? "
          + "and    proc_mark    = 'N' ";

setString(1, getValue("parm.proc_date",inti));
//if (getValue("parm.feedback_type",inti).equals("2"))
//   setString(2, "1");
//else setString(2, "3");
setString(2, getValue("parm.fund_code",inti));
setString(3, getValue("wday.this_acct_month"));

if (pSeqno.length()!=0)
   {
    whereStr  = whereStr
              + "and   p_seqno = ? ";
    setString(4, pSeqno);
   }

int  n = deleteTable();

if (n>0) 
   showLogMessage("I","","Delete ["+getValue("parm.fund_code",inti)+"] cyc_addon_calrowsum [" + n + "] records");

return;
} 
 
// ************************************************************************
 void deletePtrFundpBill() throws Exception
 {
  daoTable  = "ptr_fundp_bill";
  whereStr  = "where  fund_code    = ? ";

  setString(1, fundCode);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   p_seqno = ? ";
      setString(2, pSeqno);
     }

  int  n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete ["+ fundCode +"] ptr_fundp_bill [" + n + "] records");

  return;
 }
 
//************************************************************************
void deleteCycAddonBill() throws Exception
{
daoTable  = "cyc_addon_bill";
whereStr  = "where  fund_code    = ? ";

setString(1, fundCode);

if (pSeqno.length()!=0)
   {
    whereStr  = whereStr 
              + "and   p_seqno = ? ";
    setString(2, pSeqno);
   }

int  n = deleteTable();

if (n>0) 
   showLogMessage("I","","Delete ["+ fundCode +"] ptr_addon_bill [" + n + "] records");

return;
}

//************************************************************************
void deleteCycAddonCalRow() throws Exception
{
daoTable  = "cyc_addon_calrow";
whereStr  = "where  fund_code    = ? ";

setString(1, fundCode);

if (pSeqno.length()!=0)
 {
  whereStr  = whereStr 
            + "and   p_seqno = ? ";
  setString(2, pSeqno);
 }

int  n = deleteTable();

if (n>0) 
 showLogMessage("I","","Delete ["+ fundCode +"] ptr_addon_calrow [" + n + "] records");

return;
}

// ************************************************************************
 void deleteCycPurchFund(int inti) throws Exception
 {
  daoTable  = "cyc_purch_fund";
  whereStr  = "where  proc_date    = ? "
            + "and    proc_type    = ? "
            + "and    fund_code    = ? "
            + "and    acct_month   = ? "
            + "and    proc_mark    = 'N' ";

  setString(1, getValue("parm.proc_date",inti));
  setString(2, "1");
  setString(3, getValue("parm.fund_code",inti));
  setString(4, getValue("wday.this_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr
                + "and   p_seqno = ? ";
      setString(5, pSeqno);
     }

  int  n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete ["+getValue("parm.fund_code",inti)+"] cyc_purch_fund [" + n + "] records");

  return;
 }
// ************************************************************************
  int truncateTable(String tableName) throws Exception
 {
  String truncateSQL = "TRUNCATE TABLE "+ tableName + " "
                     + "IGNORE DELETE TRIGGERS "
                     + "DROP STORAGE "
                     + "IMMEDIATE "
                     ;

  showLogMessage("I","","Truncate Table : ["+ tableName + "]");

  executeSqlCommand(truncateSQL);

  return(0);
 }
// ************************************************************************
 void  loadPtrFundData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2,"
            + "data_code3";
  daoTable  = "ptr_fund_data";
  whereStr  = "WHERE TABLE_NAME = 'PTR_FUNDP' "
            + "and   data_key in "
            + "     (select fund_code from ptr_fundp "
            + "      WHERE apr_flag      = 'Y' "
            + "      AND   (stop_flag  = 'N' "
            + "       OR    (stop_flag = 'Y' "
            + "         and   ? < stop_date)) "
            + "         and valid_period in ('Y','E','S') "  
            + "         and (purch_feed_flag  = 'Y' "
            + "          or  fund_feed_flag   = 'Y') "
            + "         and tran_base  = 'B' "
            ;

  setString(1, businessDate);
  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
     }
  whereStr  = whereStr 
            + "     ) "
            + "order by data_key,data_type,data_code,data_code2";


  int  n = loadTable();
  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load ptr_fund_data Count: ["+n+"]");
 }
 
//************************************************************************
void  loadPtrFundCdata() throws Exception
{
extendField = "datc.";
selectSQL = "data_key,"
          + "data_type,"
          + "data_code " ;
daoTable  = "ptr_fund_cdata";
whereStr  = "WHERE TABLE_NAME = 'PTR_FUNDP' "
          + "and   data_key in "
          + "     (select fund_code from ptr_fundp "
          + "      WHERE apr_flag      = 'Y' "
          + "      AND   (stop_flag  = 'N' "
          + "       OR    (stop_flag = 'Y' "
          + "         and   ? < stop_date)) "
          + "         and valid_period in ('Y','E','S') "  
          + "         and (purch_feed_flag  = 'Y' "
          + "          or  fund_feed_flag   = 'Y') "
          + "         and tran_base  = 'B' "
          ;

setString(1, businessDate);
if (fundCode.length()!=0)
   {
    whereStr  = whereStr 
              + "and   fund_code = ? ";
    setString(2, fundCode);
   }
whereStr  = whereStr 
          + "     ) "
          + "order by data_key,data_type,data_code";


int  n = loadTable();
//setLoadData("datc.data_key,data.data_type,data.data_code");
setLoadData("datc.data_key,data.data_type");

showLogMessage("I","","Load ptr_fund_cdata Count: ["+n+"]");
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
  boolean DEBUGBn = false;
  String  checkData = "1";
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  if (DEBUGBn)
  if (dataType.equals(checkData))
     {
      showLogMessage("I","","STEP data-001 key ["+ getValue("data.data_key") +"]");
      showLogMessage("I","","STEP data_002 type ["+ getValue("data.data_type") +"]");
      showLogMessage("I","","STEP data_003 code ["+ col1 +"]");
     }

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

  if (DEBUGBn)
  if (dataType.equals(checkData))
     showLogMessage("I","","STEP data_004 cnt1 ["+ cnt1 +"]");

  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (DEBUGBn)
     if (dataType.equals(checkData))
        {
         showLogMessage("I","","STEP AA0 ["+ intm +"]");
         showLogMessage("I","","STEP AA1 ["+getValue("data.data_code",intm)+"]");
         showLogMessage("I","","STEP AA2 ["+getValue("data.data_code2",intm)+"]");
         showLogMessage("I","","STEP AA3 ["+getValue("data.data_code3",intm)+"]");
        }

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
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C1");
         if (col2.length()!=0)
            {
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C2");
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C3");
            }
          else
            {
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C4");
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C6");
         if (col3.length()!=0)
            {
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C7");
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C8");
            }
          else
            {
  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP C9");
             continue;
            }
        }
     

  if (DEBUGBn)
  if (dataType.equals(checkData))
  showLogMessage("I","","STEP CB");
     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (DEBUGBn)
      if (dataType.equals(checkData))
         showLogMessage("I","","STEP CC ");
      if (okFlag==0) return(1);
      if (DEBUGBn)
      if (dataType.equals(checkData))
         showLogMessage("I","","STEP CD return 0");
      return(0);
     }
  else
     {
      if (DEBUGBn)
      if (dataType.equals(checkData))
         showLogMessage("I","","STEP CE");
      if (okFlag==0) return(0);
      if (DEBUGBn)
      if (dataType.equals(checkData))
         showLogMessage("I","","STEP CF return 1");
      return(1);
     }
 }
 
	// ************************************************************************
	int selectPtrFundCdata(String cnameSel, String enameSel) throws Exception {
		if (cnameSel.equals("0") && enameSel.equals("0"))
			return (0);

		int okFlag = 0;
		int cnt1 = 0;
		// 特店中文名稱條件
		if ((!cnameSel.equals("0")) && (getValue("mcht_chi_name").length() != 0)) { // 特店中文名稱

			setValue("datc.data_key", getValue("data_key"));
			setValue("datc.data_type", "A");
			cnt1 = getLoadData("datc.data_key,datc.data_type");
		}

		for (int inti = 0; inti < cnt1; inti++) {
			int indexInt = getValue("mcht_chi_name").indexOf(getValue("datc.data_code", inti));
			if (indexInt != -1) {
				if (cnameSel.equals("1")) { // 指定
					okFlag = 1; // 指定 ok , 1st round pass  ok
				} else {// 排除
					okFlag = 2; // 排除則不再判斷
					break;
				}
			}
		}

		if (okFlag != 0) {
			return (0);
		}
		// 特店英文名稱條件
		if ((!enameSel.equals("0")) && (getValue("mcht_eng_name").length() != 0)) {// 特店英文名稱
			setValue("datc.data_key", getValue("data_key"));
			setValue("datc.data_type", "B");
			cnt1 = getLoadData("datc.data_key,datc.data_type");
		}

		for (int inti = 0; inti < cnt1; inti++) {
			int indexInt = getValue("mcht_eng_name").toUpperCase(Locale.TAIWAN)
					.indexOf(getValue("datc.data_code", inti).toUpperCase(Locale.TAIWAN));
			if (indexInt != -1) {
				if (enameSel.equals("1")) { // 指定
					okFlag = 1; // 指定 ok , 1st round pass  ok
				} else {// 排除
					okFlag = 2; // 排除則不再判斷
					break;
				}
			}
		}
		if (okFlag != 0) {
			return (0);
		}
		return (1);
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
            + "and   b.data_type  in ('H','M','P') "
            + "and   b.data_key in "
            + "     (select fund_code from ptr_fundp "
            + "      WHERE apr_flag      = 'Y' "
            + "      AND   (stop_flag  = 'N' "
            + "       OR    (stop_flag = 'Y' "
            + "         and   ? < stop_date)) "
            + "       and valid_period in ('Y','E','S') "  
            + "       and (purch_feed_flag  = 'Y' "
            + "        or  fund_feed_flag   = 'Y') "
            + "       and tran_base  = 'B' "
            ;

  setString(1, businessDate);
   
  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code   =  ? ";
      setString(2, fundCode);
     }
  whereStr  = whereStr 
            + "     ) "
            + "order by b.data_key,b.data_type,a.data_code";

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }
 
 
 
// ************************************************************************
	public int selectMktMchtgpData(String col1, String col2, String sel, String dataType) throws Exception {
		if (sel.equals("0"))
			return (0);

		setValue("mcht.data_key", getValue("data_key"));
		setValue("mcht.data_type", dataType);
		setValue("mcht.data_code", col1);

		int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
		int okFlag = 0;
		for (int inti = 0; inti < cnt1; inti++) {
			if ("P".equals(dataType)) {
				okFlag = 1;
				break;
			} else {
				if ((getValue("mcht.data_code2", inti).length() == 0)
						|| ((getValue("mcht.data_code2", inti).length() != 0)
								&& (getValue("mcht.data_code2", inti).equals(col2)))) {
					okFlag = 1;
					break;
				}
			}

		}

		if (sel.equals("1")) {
			if (okFlag == 0)
				return (1);
			return (0);
		} else {
			if (okFlag == 0)
				return (0);
			return (1);
		}
	}
// ************************************************************************
 int insertPtrFundpBill(int inti,String fundType) throws Exception
 {
  dateTime();

  setValue("bill.fund_type"       , fundType);
  setValue("bill.proc_date"       , getValue("parm.proc_date",inti));

  if (getValue("parm.feedback_type",inti).equals("2"))  // cycle
     setValue("bill.proc_type"    , "1");
  else
     setValue("bill.proc_type"    , "3");

  setValue("bill.p_seqno"          , getValue("p_seqno"));
  setValue("bill.reference_no"     , getValue("reference_no"));
  setValue("bill.fund_code"        , getValue("parm.fund_code",inti));
  setValue("bill.acct_month"       , getValue("parm.acct_month",inti));
  setValue("bill.dest_amt"         , getValue("dest_amt"));
  setValue("bill.acct_type"        , getValue("acct_type"));
  setValue("bill.id_p_seqno"       , getValue("id_p_seqno"));
  setValue("bill.major_id_p_seqno" , getValue("major_id_p_seqno"));
  setValue("bill.major_card_no"    , getValue("major_card_no"));
  setValue("bill.card_no"          , getValue("card_no"));
  setValue("bill.purchase_date"    , getValue("purchase_date"));
  setValue("bill.pos_entry_sel"    , getValue("parm.pos_entry_sel",inti));
  setValue("bill.program_exe_type" , getValue("parm.program_exe_type",inti));
  setValue("bill.error_code"       , "00");
  setValue("bill.mod_time"         , sysDate+sysTime);
  setValue("bill.mod_pgm"          , javaProgram);

  extendField = "bill.";
  daoTable  = "ptr_fundp_bill";

  insertTable();

  return(0);
 }
 

 // ************************************************************************
 int insertCycPosEntry() throws Exception
 {
  dateTime();

  setValue("pose.p_seqno"          , getValue("p_seqno"));
  setValue("pose.reference_no"     , getValue("reference_no"));
  setValue("pose.fund_code"        , getValue("fund_code"));
  setValue("pose.acct_month"       , getValue("acct_month"));
  setValue("pose.dest_amt"         , getValue("dest_amt"));
  setValue("pose.acct_type"        , getValue("acct_type"));
  setValue("pose.major_card_no"    , getValue("major_card_no"));
  setValue("pose.card_no"          , getValue("card_no"));
  setValue("pose.stmt_cycle"       , getValue("wday.stmt_cycle"));
  setValue("pose.mod_time"         , sysDate+sysTime);
  setValue("pose.mod_pgm"          , javaProgram);

  extendField = "pose.";
  daoTable  = "cyc_pos_entry";

  insertTable();

  return(0);
 }
// ************************************************************************
 int loadCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_p_seqno,"
            + "birthday";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno in ( "
            + "      select distinct major_id_p_seqno "
            + "      from   ptr_fundp_bill "
            + "      where  1 = 1 "
            ;

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

  whereStr  = whereStr
            + "      ) ";

  int  n = loadTable();
  setLoadData("idno.id_p_seqno");

  showLogMessage("I","","Load crd_idno Count: ["+n+"]");
  return(n);
 }
// ************************************************************************
 int loadCrdCard() throws Exception
 {
  extendField = "cori.";
  selectSQL = "card_no,"
            + "ori_card_no,"
            + "end_card_no,"
            + "activate_flag,"
            + "ori_issue_date";
  daoTable  = "crd_card";
  whereStr  = "where card_no in ( "
            + "      select distinct card_no "
            + "      from   ptr_fundp_bill "
            + "      where  1 = 1  "
            ;

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(1, fundCode);
     }

  whereStr  = whereStr
            + "      union  "
            + "      select distinct major_card_no "
            + "      from   ptr_fundp_bill "
            + "      where  1 = 1 "
            ;

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";

      setString(2, fundCode);
     }

  whereStr  = whereStr
            + " ) ";

  int  n = loadTable();
  setLoadData("cori.card_no");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
  return(n);
 }
// ************************************************************************
 int loadCrdCard1() throws Exception
 {
  extendField = "cact.";
  selectSQL = "a.major_card_no,"
            + "sum(decode(a.card_no,a.major_card_no,1,0)) as card_cnt1,"
            + "sum(decode(a.card_no,a.major_card_no, "
            + "    decode(activate_flag,'2',1,0),0)) as card_cnt2,"
            + "sum(decode(a.card_no,a.major_card_no,0,1)) as card_cnt3,"
            + "sum(decode(a.card_no,a.major_card_no,0,"
            + "    decode(activate_flag,'2',1,0))) as card_cnt4";
  daoTable  = "ptr_fundp_bill a,crd_card b";
  whereStr  = "where a.card_no =b.card_no "
            + "and   b.current_code = '0' "
            ;

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   a.fund_code = ? ";
      setString(1, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   a.p_seqno = ? ";
          setString(2, pSeqno);
         }
     }

  whereStr  = whereStr
            + "group by a.major_card_no ";

  int  n = loadTable();
  setLoadData("cact.major_card_no");

  showLogMessage("I","","Load crd_card_1 Count: ["+n+"]");
  return(n);
 }
// ************************************************************************
 int selectCrdCard1(int intk,int cardMode) throws Exception 
 {
  if (pSeqno.length()!=0)
      showLogMessage("I","","select_crd_card_1 intk[" + intk +"] card_mode [ " + cardMode +"]");
  extendField = "card.";
  selectSQL = "a.group_code,"
            + "a.sup_flag,"
            + "a.oppost_date,"
            + "b.card_indicator";
  if (cardMode==0)
     daoTable  = "crd_card a,ptr_acct_type b";
  else
     daoTable  = "ecs_crd_card a,ptr_acct_type b";

  whereStr  = "WHERE  a.id_p_seqno     = ? "
//          + "AND    a.sup_flag       =  '0' "  // 20220301
            + "AND    a.ori_card_no   !=  ? "
            + "AND    a.ori_issue_date < ? "
            + "AND    a.acct_type = b.acct_type "
            + "AND    b.card_indicator = '1' "
            ;

  setString(1 , getValue("major_id_p_seqno"));
  setString(2 , getValue("cori.ori_card_no"));
  setString(3 , getValue("cori.ori_issue_date"));

  int recCnt = selectTable();

  if (pSeqno.length()!=0)
     {
      showLogMessage("I","","STEP 0 id_p_seqno    ["+ getValue("major_id_p_seqno")           +"]");
      showLogMessage("I","","       ori_card_no   ["+ getValue("cori.ori_card_no")     +"]");
      showLogMessage("I","","       ori_issue_date["+ getValue("cori.ori_issue_date")  +"]");
     }

  for ( int inti=0; inti<recCnt; inti++ )
      {
       if (pSeqno.length()!=0)
          {
           showLogMessage("I","","STEP 1 ["+ getValue("parm.new_hldr_card" ,intk) +"]");
           showLogMessage("I","","STEP 2 ["+ getValue("parm.new_hldr_sup" ,intk) +"]");
           showLogMessage("I","","STEP 3 ["+ getValue("card.sup_flag",inti) +"]");
          }
       if ((!getValue("parm.new_hldr_card" ,intk).equals("Y"))&&(getValue("card.sup_flag",inti).equals("0"))) continue;
       if ((!getValue("parm.new_hldr_sup"  ,intk).equals("Y"))&&(getValue("card.sup_flag",inti).equals("1"))) continue;

       if (pSeqno.length()!=0)
          {
           showLogMessage("I","","STEP 4 ["+ getValue("card.oppost_date",inti) +"]");
           showLogMessage("I","","STEP 5 ["+ getValueInt("parm.new_hldr_days",intk) +"]");
           showLogMessage("I","","STEP 6 ["+ getValue("cori.ori_issue_date") +"]");
           showLogMessage("I","","STEP 7 ["+ getValue("card.group_code",inti) +"]");
          }

       setValue("data_key" , getValue("parm.fund_code" ,intk));
       if (getValue("parm.new_group_cond",intk).equals("Y"))
          {
           if (selectPtrFundData(getValue("card.group_code",inti),
                                 "1","0",3)!=0)
              {                    
               if (pSeqno.length()!=0) showLogMessage("I","","STEP 8A");
               continue;
              }
           else
              {
               if (pSeqno.length()!=0) showLogMessage("I","","STEP 8B");
               if (getValue("card.oppost_date",inti).length()==0)  return(1);
              }
          }
       else
          {
           if (pSeqno.length()!=0) showLogMessage("I","","STEP 8C");
           if (getValue("card.oppost_date",inti).length()==0)  return(1);
          }

       if (pSeqno.length()!=0)
           showLogMessage("I","","STEP 9["+ comm.nextNDate(getValue("card.oppost_date",inti),
                                              getValueInt("parm.new_hldr_days",intk)) +"]");
       if (comm.nextNDate(getValue("card.oppost_date",inti),
           getValueInt("parm.new_hldr_days",intk)).compareTo(getValue("cori.ori_issue_date"))>=0) return(1);

       if (pSeqno.length()!=0) showLogMessage("I","","STEP 10 ");
      }

  if (pSeqno.length()!=0) showLogMessage("I","","STEP OK ");
  return(0);
 }
// ************************************************************************
 int selectCrdCard2(int intk,int cardMode) throws Exception 
 {
  if (getValueInt("parm.new_card_days",intk)!=0)
     {
      if (comm.nextNDate(getValue("cori.ori_issue_date"),
                         getValueInt("parm.new_card_days",intk)).compareTo(businessDate)>0) 
         return(1);
     }
  extendField = "card.";
  selectSQL = "a.group_code";
  if (cardMode==0)
     daoTable  = "crd_card a,ptr_acct_type b";
  else
     daoTable  = "ecs_crd_card a,ptr_acct_type b";
  whereStr  = "WHERE  a.id_p_seqno     = ? "
            + "AND    a.ori_card_no   !=  ? "
            + "AND    a.sup_flag       =  '0' "
            + "AND    a.ori_issue_date < ? "
            + "AND    a.acct_type = b.acct_type "
            + "AND    b.card_indicator = '1' "
            ;

  setString(1 , getValue("major_id_p_seqno"));
  setString(2 , getValue("cori.ori_card_no"));
  setString(3 , getValue("cori.ori_issue_date"));

  int recCnt = selectTable();
  if (recCnt>0) return(1);

  return(0);
 }
// ************************************************************************
 int selectCrdCard3(int intk,int cardMode) throws Exception 
 {
  extendField = "card.";
  selectSQL = "a.group_code";
  if (cardMode==0)
     daoTable  = "crd_card a";
  else
     daoTable  = "ecs_crd_card a";
  whereStr  = "where  a.id_p_seqno     = ? "
            + "and    a.ori_card_no   != ? "
            + "and    a.ori_issue_date < ? "
            + "and    a.sup_flag       = '0' "
            ;

  setString(1 , getValue("major_id_p_seqno"));
  setString(2 , getValue("car4.card_no"));
  setString(3 , getValue("car4.issue_date"));

  int recCnt = selectTable();

  return(recCnt);
 }
// ************************************************************************
 int selectCrdCard4(int inti) throws Exception 
 {
  extendField = "car4.";
  selectSQL = "card_no,"
            + "issue_date";
  daoTable  = "crd_card";
  whereStr  = "where  id_p_seqno     = ? "
            + "and    sup_flag       = '0' "
            + "and    group_code in ( "
            + "       select data_code  "
            + "       from   ptr_fund_data "
            + "       where  table_name = 'PTR_FUNDP' "
            + "       and    data_key   = ? "
            + "       and    data_type  = '3') "
            + "order by issue_date "
            ;

  setString(1 , getValue("major_id_p_seqno"));
  setString(2 , getValue("parm.fund_code",inti));

  int recCnt = selectTable();

  if (recCnt==0) return(1);

  if (getValue("car4.issue_date").compareTo(getValue("parm.card_feed_date_s",inti))<0) return(1);
  if (getValue("car4.issue_date").compareTo(getValue("parm.card_feed_date_e",inti))>0) return(1);

  return(0);
 }
// ************************************************************************
 int updatePtrFundpBill(String errorCode) throws Exception
 {
  daoTable  = "ptr_fundp_bill";
  updateSQL = "error_code     = ? ";
  whereStr  = "where rowid    = ? "
            + "and error_code = '00' "
            ;

  setString(1 , errorCode);
  setRowId(2  , getValue("rowid"));

  int n = updateTable();
  return n;
 }
// ************************************************************************
 int updatePtrFundpBill(int inti,String errorCode) throws Exception
 {
  daoTable  = "ptr_fundp_bill";
  updateSQL = "error_code     = ? ";
  whereStr  = "where fund_code     = ? "
            + "and   major_card_no = ? "
            + "and   error_code    = '00' "
            ;

  setString(1 , errorCode);
  setString(2 , getValue("parm.fund_code",inti));
  setString(3 , getValue("major_card_no"));

  int n = updateTable();
  return n;
 }
// ************************************************************************
 int updatePtrFundpBill(String majorCardNo,String errorCode) throws Exception
 {
  daoTable  = "ptr_fundp_bill";
  updateSQL = "error_code     = ? ";
  whereStr  = "where major_card_no = ? "
            + "and   error_code    = '00' "
            ;

  setString(1 , errorCode);
  setString(2 , getValue("major_card_no"));

  int n = updateTable();
  return n;
 }
// ************************************************************************
 int loadMktRcvBin() throws Exception
 {
  extendField = "aica.";
  selectSQL = "lpad(ica_no,8,'0') as ica_no,"
            + "bank_no";
  daoTable  = "mkt_rcv_bin";
  whereStr  = "order by lpad(ica_no,8,'0')";

  int  n = loadTable();

  setLoadData("aica.ica_no");

  showLogMessage("I","","Load mkt_rcv_bin count: ["+n+"]");
  return(n);
 }
 
//************************************************************************
 int loadMktAddonIdlist() throws Exception
 {
  extendField = "idlist.";
  selectSQL = "list_type , "
  		    + "id_no " 
  ;
  daoTable  = "mkt_addon_idlist";
  whereStr  = " where  data_month     = ? "
            + " order by list_type , id_no "
          ;
  setString(1, comm.lastMonth(businessDate,1));
  int  n = loadTable();

  setLoadData("idlist.list_type,idlist.id_no");

  showLogMessage("I","","Load mkt_addon_idlist data-month:[" + comm.lastMonth(businessDate,1) + "]  count: ["+n+"]");
  return(n);
 }
 
// ************************************************************************
 int updatePtrFundp(int inti,String stopDesc) throws Exception
 {
  daoTable  = "ptr_fundp";
  updateSQL = "stop_date  = ?, "
            + "stop_flag  = 'Y',"
            + "stop_desc  = ?,"
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate ";   
  whereStr  = "where fund_code = ? "
            ;

  setString(1 , businessDate);
  setString(2 , stopDesc);
  setString(3 , javaProgram);                                               
  setString(4 , getValue("parm.fund_code",inti));

  int n = updateTable();
  showLogMessage("I","","基金代碼:[" + getValue("parm.fund_code",inti)  
                       + "] 日期["+ businessDate 
                       + "][" + stopDesc + "]"); 
  return n;
 }
//************************************************************************
 String getCrdIdno(String idPSeqno) throws Exception {
		sqlCmd  =  "SELECT id_no  ";
		sqlCmd += " FROM crd_idno";
		sqlCmd += " WHERE id_p_seqno = ? ";
		setString(1, idPSeqno);
		selectTable();
		if (notFound.equals("Y")) {
			return "";
		}
		return  getValue("id_no");
 }
// ************************************************************************
/*
 1.check stmt_cycle end date is achieve
 2. transfer acct_month maybe error
*/


}  // End of class FetchSample

