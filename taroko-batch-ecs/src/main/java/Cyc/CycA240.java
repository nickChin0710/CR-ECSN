/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/20  V1.00.24  Allen Ho   mkt_d060                                   *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                            *
* 112-03-30  V1.00.02    Zuwei Su  新增[ 一般消費群組 ]參數帳單資料篩選處理                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA240 extends AccessDAO
{
 private final String PROGNAME = " 紅利-紅利特惠活動(五)符合消費名單處理程式 112-03-30  V1.00.02"  ;
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;
 CommCrdRoutine comcr = null;

 String businessDate   = "";
 String activeCode     = "";

 String tranSeqno = "";
 String[] procWork      = new String[300];
 int[]    onlyCalFlag  = new int[300];
 String   startSDate   = "";
 String   startEDate   = "";

 int cycleFlag = 0;
 int vdFlagCnt = 0;
 int cdFlagCnt = 0;
 int procWorkCnt =0;
 int totalAmtPlus=0,totalAmtMinus=0;
 int[] runTimeCnt = new int [300];

 long    totalCnt=0,updateCnt=0;
 int parmCnt=0,procMonths=1;
 int[] matchCnt= new int[300];
 int[] matchFlag= new int[300];
 String feedbackType = "";

 boolean DEBUG =false;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA240 proc = new CycA240();
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
   	   showLogMessage("I","","PARM 1 : [feedbackType]");
       showLogMessage("I","","PARM 2 : [business_date]");
       showLogMessage("I","","PARM 3 : [active_code]");
       return(1);
      }

	if (args.length == 0 || (!args[0].equals("1") &&
			!args[0].equals("2"))) {
		showLogMessage("I","","請傳入回饋方式 : 1.帳單週期 2.每月 ");
		return(1);
	}  

	feedbackType = args[0];
   
   if (args.length >= 2 )
      { businessDate = args[1]; }

   if (args.length == 3 )
      { activeCode  = args[2]; }
   
   if ( !connectDataBase() ) return(1);

   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   cycleFlag = selectPtrWorkday();
   
   if ((feedbackType.equals("1")) && !(cycleFlag == 0))   {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
   }
   if ((feedbackType.equals("2")) &&  (cycleFlag == 0) )   {
       showLogMessage("I","","回饋方式 : 1.每月指定日 ,本日是關帳日,不需執行 ");
       return(0);
   }  

   showLogMessage("I","","=========================================");
   showLogMessage("I","","清除檔案舊資料(mkt_bpid_data)");
   selectMktBpmh3DataOld();
   commitDataBase();

   showLogMessage("I","","=========================================");
 
   if (activeCode.length()>0)
      {
       showLogMessage("I","","DEBUG MODE");
       deleteMktBpmh3Dtl();
      }
   else
      {
       showLogMessage("I","","清除暫存檔(mkt_bpid_dtl)");
       commitDataBase();
       truncateTable("MKT_BPMH3_DTL");
       commitDataBase();
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","清除檔(mkt_bpid_data)");
   deleteMktBpmh3Data();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktBpmh3();
   for (int inti=0;inti<parmCnt;inti++)
       {
        if (procWork[inti].equals("N")) continue;
        procWorkCnt++;
       }
   
   if (procWorkCnt==0)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","本日無活動代碼需處理");
       showLogMessage("I","","=========================================");
       finalProcess();
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadDbaAcno();
   loadMktMchtgpData();
   loadMktBnData();
   loadMktBpmh3List1();
   loadMktBpmh3ListV();
   loadMktBpmh3List2();
   loadMktBpmh3List3();

   if (cdFlagCnt>0)
       if ((runTimeCnt[0]>0)||(runTimeCnt[1]>0))
          {
           showLogMessage("I","","=========================================");
           showLogMessage("I","","處理帳單(bil_bill)資料");
           if (runTimeCnt[0]>0) selectBilBill(0);
           if (runTimeCnt[1]>0) selectBilBill(1);
          }

   if (vdFlagCnt>0)
       if (runTimeCnt[1]>0) 
          {
           showLogMessage("I","","=========================================");
           showLogMessage("I","","處理帳單(dbb_bill)資料");
           selectDbbBill();
          }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadBilContract();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理分期退貨");
   selectMktBpmh3Dtl0();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(mkt_bpmh3_datal)資料");
   selectMktBpmh3Dtl();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","finalProcess()");
   finalProcess();
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
 int selectBilBill(int runTimeType) throws Exception
 {
  daoTable  = "bil_bill";
  whereStr  = "WHERE acct_type not in ('02','03') "
            + "and   acct_code  in ('BL','ID','IT','AO','OT','CA')  "
            + "and   rsk_type not in ('1','2','3') "
            + "and   merge_flag       != 'Y'  "
            ;
            
  if (runTimeType==0)
     {
      whereStr  = whereStr  
                + " and acct_month = ? "
                + " and stmt_cycle = ? ";

      setString(1 , getValue("wday.this_acct_month"));
      setString(2 , getValue("wday.stmt_cycle"));
      showLogMessage("I","","入帳月份: ["+getValue("wday.this_acct_month")
                           + "]-["+ getValue("wday.stmt_cycle") + "]" );
     }
  else
     {
      whereStr  = whereStr  
                + " and purchase_date between ? and ? ";

      setString(1 , startSDate);
      setString(2 , startEDate);
      showLogMessage("I","","消費日期:[" + startSDate+"]["+startEDate+"]");
     }

  openCursor();

  double[][] parmArr = new double [parmCnt][30];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  totalCnt=0;

  String acqId ="";

  while( fetchTable() ) 
   { 
    totalCnt++;

    if (getValue("group_code").length()==0)
       setValue("group_code" , "0000");

    if (getValue("ori_card_no").length()==0)
       setValue("ori_card_no"    , getValue("card_no"));

    if (getValue("sign_flag").equals("-"))
       setValueDouble("dest_amt" , getValueDouble("dest_amt")*-1);
    else    
       setValueDouble("dest_amt" , getValueDouble("dest_amt"));  // source pgm seem not add adjust_amt
//     setValueDouble("dest_amt" , getValueDouble("dest_amt")+getValueDouble("curr_adjust_amt"));

    for (int inti=0;inti<parmCnt;inti++)
      {
       if (Arrays.asList("1","2","3").contains(getValue("parm.list_cond",inti)))
          //if (getValue("parm.vd_cond",inti).equals("Y")) continue;
    	   if (getValue("parm.vd_flag",inti).equals("Y")) continue; 

       parmArr[inti][0]++;
       if (procWork[inti].equals("N")) continue;

       setValue("data_key", getValue("parm.active_code",inti)); 

       parmArr[inti][1]++;
       if (getValue("parm.purch_cond",inti).equals("Y"))
          {
           //if (getValue("parm.purch_date_s",inti).compareTo("purchase_date")>0) continue;
           if (getValue("parm.purch_s_date",inti).compareTo(getValue("purchase_date"))>0) continue;
           parmArr[inti][2]++;
           if (getValue("parm.purch_e_date",inti).compareTo(getValue("purchase_date"))<0) continue;
          }
       parmArr[inti][3]++;

       if (getValue("parm.issue_cond",inti).equals("Y"))
          {
           if (getValue("card_no").equals(getValue("ori_card_no")))
              {
               selectCrdCard();
              }
           else
              {
               setValue("ori_issue_date" ,getValue("issue_date"));
              }
           parmArr[inti][4]++;
           if ((getValue("ori_issue_date").compareTo(getValue("parm.issue_date_s",inti))<0)||
               (getValue("ori_issue_date").compareTo(getValue("parm.issue_date_e",inti))>0)) continue;
           parmArr[inti][5]++;

           if (comm.nextNDate(getValue("ori_issue_date"),getValueInt("parm.card_re_days",inti)).
               compareTo(businessDate)>0) continue;
          }
   
       parmArr[inti][6]++;

       if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL"))) continue;
       if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA"))) continue;
       if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID"))) continue;
       if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("acct_code").equals("AO"))) continue;
       if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT"))) continue;
       if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("acct_code").equals("IT"))) continue;

       parmArr[inti][7]++;
//     if (getValue("acct_code").equals("IT"))  // debug
//        continue;                             //debug

       if (getValue("acct_code").equals("IT"))
          if (getValue("parm.it_flag",inti).equals("1"))
             {
              parmArr[inti][8]++;
              if (getValueInt("install_curr_term")!=1) continue; 
              setValue("dest_amt",getValue("contract_amt"));
             }

       parmArr[inti][9]++;
       if (getValueDouble("parm.add_item_amt",inti)!=0) 
          if (Math.abs(getValueDouble("dest_amt"))<getValueDouble("parm.add_item_amt",inti)) continue;

       setValue("vd_flag", "N");
       parmArr[inti][10]++;

       if (getValue("parm.list_cond",inti).equals("1")) 
          if (selectMktBpmh3List1()!=0) continue;
       parmArr[inti][11]++;
       if (getValue("parm.list_cond",inti).equals("2")) 
          if (selectMktBpmh3List2()!=0) continue;
       parmArr[inti][12]++;
       if (getValue("parm.list_cond",inti).equals("3")) 
          if (selectMktBpmh3List3()!=0) continue;

       parmArr[inti][13]++;
       if (selectMktBnData(getValue("acct_type"),
                              getValue("parm.acct_type_sel",inti),"1",3)!=0) continue;
       parmArr[inti][14]++;
       if (selectMktBnData(getValue("mcht_category"),
                              getValue("parm.mcc_code_sel",inti),"4",3)!=0) continue;
       parmArr[inti][15]++;
       if (selectMktBnData(getValue("bill_type"),
                              getValue("parm.bill_type_sel",inti),"5",3)!=0) continue;

       parmArr[inti][16]++;
       if (selectMktBnData(getValue("bin_type"),getValue("source_curr"),
                              getValue("parm.currency_sel",inti),"6",3)!=0) continue;
       parmArr[inti][17]++;
//        showLogMessage("I","","group_code :["+ getValue("group_code")+"] ["+ getValue("card_type")+"]");
       if (selectMktBnData(getValue("group_code"),getValue("card_type"),
                              getValue("parm.group_card_sel",inti),"2",3)!=0) continue;
       parmArr[inti][18]++;

       acqId = "";
       if (getValue("acq_member_id").length()!=0)
          acqId = comm.fillZero(getValue("acq_member_id"),8);

       if (selectMktBnData(getValue("mcht_no"),acqId,
                              getValue("parm.merchant_sel",inti),"3",3)!=0) continue;
       parmArr[inti][19]++;

       if (selectMktMchtgpData(getValue("mcht_no"), acqId,
                                  getValue("parm.mcht_group_sel",inti),"7")!=0) continue;
       matchCnt[inti]++;
       parmArr[inti][20]++;

		// 一般消費群組
       if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"), "",
                                  getValue("parm.platform_kind_sel",inti),"P")!=0) continue;
       matchCnt[inti]++;
       parmArr[inti][21]++;

       insertMktBpmh3Dtl(inti);
      }

    processDisplay(200000); // every 10000 display message
   } 
  closeCursor();

  for (int inti=0;inti<parmCnt;inti++)
    {
     if (procWork[inti].equals("N")) continue;
     setValue("active_code",getValue("parm.active_code",inti));
     updateMktBpmh3();
    }
  
  if (activeCode.length()>0)
  for (int inti=0;inti<parmCnt;inti++)
    {
       showLogMessage("I","","active_code :["+getValue("parm.active_code",inti)+"]");
     for (int intk=0;intk<30;intk++)
         {
          if (parmArr[inti][intk]==0) continue;
          showLogMessage("I","","絆腳石  :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
         }
    }
   
  return(0);
 }
// ************************************************************************
  int selectDbbBill() throws Exception
 {
  daoTable  = "dbb_bill";
  whereStr  = "WHERE purchase_date between ? and ? ";

  setString(1 , startSDate);
  setString(2 , startEDate);
  showLogMessage("I","","Debit 消費日期:[" + startSDate+"]["+startEDate+"]");

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;
    for (int inti=0;inti<parmCnt;inti++)
      {
       if (procWork[inti].equals("N")) continue;

       //if (getValue("parm.purch_date_type",inti).equals("1"))
       //   if (getValue("parm.purch_run_type",inti).equals("1")) continue;

       //if (getValue("parm.purch_date_type",inti).equals("2"))
       //    if (getValue("parm.run_time_type",inti).equals("1")) continue;
       if (getValue("parm.run_time_type",inti).equals("1")) continue;

       setValue("active_code",getValue("parm.active_code",inti));
       
       //if (getValue("parm.purch_date_type",inti).equals("1"))
       if (getValue("parm.purch_cond",inti).equals("Y"))
          {
           if ((getValue("purchase_date").compareTo(startSDate)<0)||
               (getValue("purchase_date").compareTo(startEDate)>0)) continue;

           if ((getValue("purchase_date").compareTo(getValue("parm.purch_s_date",inti))<0)||
               (getValue("purchase_date").compareTo(getValue("parm.pruch_e_date",inti))>0)) continue;
          }

       //if (getValue("parm.purch_date_type",inti).equals("2"))    	   
       //   {
       //    if ((getValue("purchase_date").compareTo(startSDate)<0)||
       //        (getValue("purchase_date").compareTo(startEDate)>0)) continue;
       //
       //   }
       
       if (getValue("parm.issue_cond",inti).equals("Y"))
          {
           if ((getValue("issue_date").compareTo(getValue("parm.issue_date_s",inti))<0)||
               (getValue("issue_date").compareTo(getValue("parm.issue_date_e",inti))>0)) continue;

           if (comm.nextNDate(getValue("issue_date"),getValueInt("parm.card_re_days",inti)).
               compareTo(businessDate)>0) continue;
          }

       if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL"))) continue;
       if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA"))) continue;
       if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID"))) continue;
       if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT"))) continue;

       if (getValue("sign_flag").equals("-"))
           setValueDouble("dest_amt" , getValueDouble("dest_amt")*-1);
        else    
           setValueDouble("dest_amt" , getValueDouble("dest_amt"));       
       
       //if (getValueDouble("dest_amt")<getValueDouble("parm.add_item_amt",inti)) continue;
       if (Math.abs(getValueDouble("dest_amt"))<getValueDouble("parm.add_item_amt",inti)) continue;


       if (getValue("parm.vd_corp_flag",inti).equals("N"))
          if (selectDbaAcno()==0) continue;

       setValue("vd_flag", "Y");
       if (getValue("parm.list_cond",inti).equals("1")) 
          if (selectMktBpmh3ListV()!=0) continue;
       if (getValue("parm.list_cond",inti).equals("2")) 
          if (selectMktBpmh3List2()!=0) continue;
       if (getValue("parm.list_cond",inti).equals("3")) 
          if (selectMktBpmh3List3()!=0) continue;

       setValue("data_key", getValue("parm.active_code",inti)); 

       if (selectMktBnData(getValue("acct_type"),
                              getValue("parm.acct_type_sel",inti),"1",3)!=0) continue;
       if (selectMktBnData(getValue("mcht_category"),
                              getValue("parm.mcc_code_sel",inti),"4",3)!=0) continue;

       if (selectMktBnData(getValue("bill_type"),
                              getValue("parm.bill_type_sel",inti),"5",3)!=0) continue;

       if (selectMktBnData(getValue("bin_type"),getValue("source_curr"),
                              getValue("parm.currency_sel",inti),"6",3)!=0) continue;

       if (selectMktBnData(getValue("group_code"),getValue("card_type"),
                              getValue("parm.group_card_sel",inti),"2",2)!=0) continue;

       if (selectMktBnData(getValue("mcht_no"),getValue("mcht_category"),
                              getValue("parm.merchant_sel",inti),"3",3)!=0) continue;

       if (selectMktMchtgpData(getValue("mcht_no"),getValue("mcht_category"),
                                  getValue("parm.mcht_group_sel",inti),"7")!=0) continue;
		// 一般消費群組
       if (selectMktMchtgpData(getValue("ecs_cus_mcht_no") , "" ,
                                  getValue("parm.platform_kind_sel",inti),"P")!=0) continue;
       matchCnt[inti]++;

       insertMktBpmh3Dtl(inti);
      }
    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();

  for (int inti=0;inti<parmCnt;inti++)
    {
     if (procWork[inti].equals("N")) continue;

     //if (getValue("parm.purch_date_type",inti).equals("1"))
     //   if (getValue("parm.purch_run_type",inti).equals("1")) continue;

     //if (getValue("parm.purch_date_type",inti).equals("2"))
     //   if (getValue("parm.run_time_type",inti).equals("1")) continue;
     //20230305
     if (getValue("parm.run_time_type",inti).equals("1")) continue;
     
     if (!(getValue("parm.vd_cond",inti).equals("Y") || vdFlagCnt > 0 )) continue;

     setValue("active_code",getValue("parm.active_code",inti));
     updateMktBpmh3();
    }

  return(0);
 }
// ************************************************************************
 int selectMktBpmh3() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_bpmh3";
  whereStr  = "WHERE apr_flag        = 'Y' "
            + "AND   apr_date       != ''  "
            + "AND   (stop_flag     != 'Y'  "
            + " or    (stop_flag     = 'Y'  "
            + "  and  stop_date      > ? )) "
            + "AND   (active_date_e  = ''  "
            + " or    active_date_e >= ? ) "
            + "AND   (active_date_s  = ''  "
            + " or    active_date_s <= ? ) "
            ;
  whereStr += " and run_time_type = ?";
  int i = 1;
  setString(i++ , businessDate);
  setString(i++ , businessDate);
  setString(i++ , businessDate);
  setString(i++ , feedbackType);
  
  if("2".equals(feedbackType)) {
	  CommString coms = new CommString();
	  whereStr += " and run_time_dd = ? ";
	  setInt(i++ ,coms.ss2int(coms.right(businessDate, 2)));
  }

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(i++ , activeCode);
     }            

  parmCnt = selectTable();

  String tempDate="";

  int nowInt = 0;
  for (int inti=0;inti<parmCnt;inti++)
    {
     setValue("active_code",getValue("parm.active_code",inti));

     procWork[inti] = "Y";
     onlyCalFlag[inti] = 0;
     //setValue("parm.vd_flag" , "N" , inti);

     if (Arrays.asList("1","2","3").contains(getValue("parm.list_cond",inti)))
        //if (getValue("parm.vd_cond",inti).equals("Y"))
    	 if (getValue("parm.vd_flag",inti).equals("Y")) 
           {
            vdFlagCnt = vdFlagCnt + 1;
           }

     if (getValue("parm.purch_cond",inti).equals("Y"))
        {
         //if (getValue("parm.purch_date_s",inti).length()==0)
    	 if (getValue("parm.purch_s_date",inti).length()==0)	 
            //setValue("parm.purch_date_s","00000000",inti);
    		setValue("parm.purch_s_date","00000000",inti); 
         //if (getValue("parm.purch_date_e",inti).length()==0)
         if (getValue("parm.purch_e_date",inti).length()==0)	 
            //setValue("parm.purch_date_s","99999999",inti);
            setValue("parm.purch_e_date","99999999",inti);
        }

     if (getValue("parm.run_start_month",inti).length()!=0)
        {
         if (getValue("parm.run_start_month",inti).compareTo(businessDate.substring(0,6))>0)
            {
             procWork[inti] = "N";
             continue;
            }

         tempDate = getValue("parm.run_start_month",inti);
         if (getValue("parm.proc_date",inti).length()!=0)
            tempDate = getValue("parm.proc_date",inti).substring(0,6);

         if (checkStartMonth(tempDate,
                               getValue("wday.this_acct_month"),
                               getValueInt("parm.run_time_mm",inti))!=0) 
            {
             onlyCalFlag[inti]=1;
             setValue("parm.run_date" , getValue("run_date") , inti);
            }
        }

     nowInt++;
     showLogMessage("I","","("+nowInt+")活動代號 : ["+getValue("active_code")+"]");

     if (getValue("parm.run_time_type",inti).equals("1"))
        {
         if (cycleFlag!=0) 
            {
             procWork[inti] = "N";
             continue;
            }
         procWork[inti] = "1";
         runTimeCnt[0]++;
         showLogMessage("I","","    執行區間 帳務月份:[" + getValue("wday.this_acct_month") +"]");
        }
     else
        {
         if (!businessDate.substring(6,8).equals(
             String.format("%02d",getValueInt("parm.run_time_dd",inti))))
            {
             procWork[inti] = "N";
             continue;
            }
         procWork[inti] = "2";
         startSDate = comm.lastMonth(businessDate)+"01";
         startEDate = comm.lastdateOfmonth(startSDate);
         runTimeCnt[1]++;
         showLogMessage("I","","    執行區間 消費日期:[" + startSDate+"]["+startEDate+"]");
        }
     selectVmktAcctType(inti);
     vdFlagCnt = vdFlagCnt + getValueInt("vmkt.vd_flag_cnt");
     cdFlagCnt = cdFlagCnt + getValueInt("vmkt.cd_flag_cnt");

     setValue("parm.run_date" , businessDate , inti);
    }

  return(0);
 }
// ************************************************************************
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "WHERE this_close_date = ? ";
  
  setString(1, businessDate);

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktBnData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
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
 int selectMktBpmh3List1() throws Exception
 {
  setValue("list1.active_code",getValue("active_code"));
  setValue("list1.id_p_seqno",getValue("id_p_seqno"));

  int cnt1 = getLoadData("list1.active_code,list1.id_p_seqno");
  if (cnt1==0) return(1);
  return(0);
 }
// ************************************************************************
 int selectMktBpmh3ListV() throws Exception
 {
  setValue("listv.active_code",getValue("active_code"));
  setValue("listv.id_p_seqno",getValue("id_p_seqno"));

  int cnt1 = getLoadData("listv.active_code,listv.id_p_seqno");
  if (cnt1==0) return(1);
  return(0);
 }
// ************************************************************************
 int selectMktBpmh3List2() throws Exception
 {
  setValue("list3.active_code",getValue("active_code"));
  setValue("list2.vd_flag",getValue("vd_flag")); 
  setValue("list2.p_seqno",getValue("p_seqno")); 

  int cnt1 = getLoadData("list2.active_code,list2.vd_flag,list2.p_seqno");
  if (cnt1==0) return(1);
  return(0);
 }
// ************************************************************************
 int selectMktBpmh3List3() throws Exception
 {
  setValue("list3.active_code",getValue("active_code"));
  setValue("list3.card_no",getValue("card_no")); 

  int cnt1 = getLoadData("list3.active_code,list3.card_no");
  if (cnt1==0) return(1);
  return(0);
 }
// ************************************************************************
 void  loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,mkt_bn_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'MKT_BPMH3' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  in ('7','P') "
            + "order by b.data_key,b.data_type,data_code "
            ;

  int  n = loadTable();
  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
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
 void  loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_BPMH3' "
            + "and   data_type between '1' and '6' "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 void  loadDbaAcno() throws Exception
 {
  extendField = "dba.";
  selectSQL = "p_seqno";
  daoTable  = "dba_acno";
  whereStr  = "WHERE corp_p_seqno !='' "
            + "order by p_seqno "
            ;

  int  n = loadTable();
  setLoadData("dba.p_seqno");
  showLogMessage("I","","Load dba_acno Count: ["+n+"]");
 }
// ************************************************************************
 int selectDbaAcno() throws Exception
 {
  setValue("dba.p_seqno",getValue("p_seqno"));

  int cnt1 = getLoadData("dba.p_seqno");
  if (cnt1==0) return(1);
  return(0);
 }
// ************************************************************************
 void  loadMktBpmh3List1() throws Exception
 {
  extendField = "list1.";
  selectSQL = "id_p_seqno,"
            + "active_code";
  daoTable  = "mkt_bpmh3_list a,crd_idno b";
  whereStr  = "WHERE list_cond = '1' "
            + "and   a.id_no = b.id_no"
            ;

  int  n = loadTable();
  setLoadData("list1.active_code,list1.id_p_seqno");
  showLogMessage("I","","Load mkt_bpmh3_list_1 Count: ["+n+"]");
 }
// ************************************************************************
 void  loadMktBpmh3ListV() throws Exception
 {
  extendField = "listv.";
  selectSQL = "id_p_seqno,"
            + "active_code";
  daoTable  = "mkt_bpmh3_list a,dbc_idno b";
  whereStr  = "WHERE list_cond = '1' "
            + "and   a.id_no = b.id_no";

  int  n = loadTable();
  setLoadData("listv.active_code,listv.id_p_seqno");
  showLogMessage("I","","Load mkt_bpmh3_list_v Count: ["+n+"]");
 }
// ************************************************************************
 void  loadMktBpmh3List2() throws Exception
 {
  extendField = "list2.";
  selectSQL = "vd_flag,"
            + "p_seqno,"
            + "active_code";
  daoTable  = "mkt_bpmh3_list";
  whereStr  = "WHERE list_cond = '2' ";

  int  n = loadTable();
  setLoadData("list2.active_code,list2.vd_flag,list2.p_seqno");

  showLogMessage("I","","Load mkt_bpmh3_list_2 Count: ["+n+"]");
 }
// ************************************************************************
 void  loadMktBpmh3List3() throws Exception
 {
  extendField = "list3.";
  selectSQL = "data_code as card_no,"
            + "active_code";
  daoTable  = "mkt_bpmh3_list";
  whereStr  = "WHERE list_cond = '3' ";

  int  n = loadTable();
  setLoadData("list3.active_code,list3.card_no");
  showLogMessage("I","","Load mkt_bpmh3_list_3 Count: ["+n+"]");
 }
// ************************************************************************
 void updateMktBpmh3() throws Exception
 {
  dateTime();
  updateSQL = "proc_date     = ?, "
            + "feedback_date = ?, "
            + "feedback_flag = 'N', "
            + "mod_pgm       = ?, "
            + "mod_time      = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "mkt_bpmh3";
  whereStr  = "WHERE active_code   = ? ";

  setString(1 , sysDate);
  setString(2 , businessDate);
  setString(3 , javaProgram);
  setString(4 , sysDate+sysTime);
  setString(5 , getValue("active_code"));

  updateTable();
  return;
 }
// ************************************************************************
 int deleteMktBpmh3Data() throws Exception
 {
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "where feedback_date = ? "
            ;

  setString(1 , businessDate);
  
  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(2 , activeCode);
     }            

  int n = deleteTable();

  if (n>0) showLogMessage("I","","    刪除 mkt_bpmh3_data  [" + n +"] 筆");

  return(0);
 }
// ************************************************************************
 int deleteMktBpmh3Dtl() throws Exception
 {
  daoTable  = "mkt_bpmh3_dtl";
  whereStr  = "where active_code = ? ";

  setString(1 , activeCode); 

  int n = deleteTable();

  if (n>0) showLogMessage("I","","    刪除 mkt_bpmh3_dtl [" + n +"] 筆");

  return(0);
 }
// ************************************************************************
 int deleteMktBpmh3Dtl0() throws Exception
 {
  daoTable  = "mkt_bpmh3_dtl";
  whereStr  = "where  rowid = ? ";

  setRowId(1 , getValue("rowid")); 

  deleteTable();

  return(0);
 }
// ************************************************************************
 int checkStartMonth(String startMonth,int monthCnt) throws Exception
 {
  for (int inti=0;inti<600;inti++)
     {
      if (businessDate.substring(0,6).compareTo(comm.nextMonth(startMonth,inti*monthCnt))==0) return(0);
      if (businessDate.substring(0,6).compareTo(comm.nextMonth(startMonth,inti*monthCnt))>0) continue;
      return(1);
     }
  return(1);
 }
// ************************************************************************
 int checkStartMonth(String startMonth,String thisMonth,int monthCnt) throws Exception
 {
  for (int inti=0;inti<600;inti++)
     {
      if (thisMonth.compareTo(comm.nextMonth(startMonth,inti*monthCnt))>0) 
         {
          setValue("run_date" , comm.nextMonth(startMonth,inti*monthCnt)); 
          continue;
         }
      if (thisMonth.equals(comm.nextMonth(startMonth,inti*monthCnt))) return(0);
      return(1);
     }
  return(1);
 }
// ************************************************************************
 int insertMktBpmh3Dtl(int inti) throws Exception
 {
  dateTime();
  extendField = "mh3d.";
  setValue("mh3d.active_code"          , getValue("parm.active_code",inti));
  setValue("mh3d.feedback_date"        , businessDate);
  setValue("mh3d.vd_flag"              , getValue("vd_flag"));
  setValue("mh3d.p_seqno"              , getValue("p_seqno"));
  if (getValue("vd_flag").equals("Y")) 
  {
    setValue("mh3d.id_p_seqno"           , getValue("id_p_seqno"));
  } else {
	setValue("mh3d.id_p_seqno"           , getValue("major_id_p_seqno"));	  
  }
  setValue("mh3d.acct_type"            , getValue("acct_type"));
  setValue("mh3d.reference_no"         , getValue("reference_no"));
  setValue("mh3d.purchase_date"        , getValue("purchase_date"));
  setValue("mh3d.contract_no"          , getValue("contract_no"));
  setValue("mh3d.stmt_cycle"           , getValue("stmt_cycle"));
  setValue("mh3d.card_no"              , getValue("card_no"));
  setValue("mh3d.issue_date"           , getValue("issue_date"));
  setValue("mh3d.ori_issue_date"       , getValue("issue_date"));
  if (getValue("ori_card_no").length()==0)
      setValue("ori_card_no"            , getValue("card_no"));
  setValue("mh3d.ori_card_no"          , getValue("ori_card_no"));
  setValue("mh3d.group_code"           , getValue("group_code"));
  setValue("mh3d.card_type"            , getValue("card_type"));
  setValue("mh3d.acct_code"            , getValue("acct_code"));
  setValue("mh3d.mcht_category"        , getValue("mcht_category"));
  setValue("mh3d.bill_type"            , getValue("bill_type"));
  setValue("mh3d.pos_entry_mode"       , getValue("pos_entry_mode"));
  setValue("mh3d.source_curr"          , getValue("source_curr"));
  setValue("mh3d.mcht_no"              , getValue("mcht_no"));
  setValue("mh3d.acq_member_id"        , getValue("acq_member_id"));
  setValue("mh3d.bin_type"             , getValue("bin_type"));
  setValue("mh3d.proc_flag"            , "N");
  setValue("mh3d.proc_date"            , "");
  setValue("mh3d.dest_amt"             , getValue("dest_amt"));
  setValue("mh3d.mod_time"             , sysDate+sysTime);
  setValue("mh3d.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpmh3_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectCrdCard() throws Exception
 {
  selectSQL = "ori_issue_date";
  daoTable  = "crd_card";
  whereStr  = "WHERE card_no = ? ";
  
  setString(1, getValue("card_no"));

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
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
 int selectVmktAcctType(int inti) throws Exception
 {
  extendField = "vmkt.";
  selectSQL = "sum(decode(vd_flag,'Y',1,0)) as vd_flag_cnt,"
            + "sum(decode(vd_flag,'Y',0,1)) as cd_flag_cnt";
  daoTable  = "MKT_bn_data a,VMKT_ACCT_TYPE b";
  whereStr  = "WHERE table_name='MKT_BPMH3' "
            + "and data_type='1' "
            + "and a.data_code = b.acct_type "
            + "and a.data_key  = ? ";
  
  setString(1, getValue("parm.active_code",inti));

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktBpmh3Dtl0() throws Exception
 {
  selectSQL = "contract_no,"
            + "rowid as rowid";
  daoTable  = "mkt_bpmh3_dtl";
  whereStr  = "where acct_code = 'IT' "
            ;

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(1 , activeCode);
     }            

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    setValue("cont.contract_no",getValue("contract_no"));

    int cnt1 = getLoadData("cont.contract_no");

    if (cnt1>0) deleteMktBpmh3Dtl0();

    processDisplay(200000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktBpmh3Dtl() throws Exception
 {
  selectSQL = "active_code,"
            + "vd_flag,"
            + "p_seqno,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type,"
            + "max(acct_no) as acct_no,"
            + "max(card_no) as card_no,"
            + "sum(dest_amt) as dest_amt,"
            + "count(*) as dest_cnt";
  daoTable  = "mkt_bpmh3_dtl";
  whereStr  = "where proc_flag = 'N' "
            ;
  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(1 , activeCode);
     }            
  whereStr  = whereStr 
            + "group by active_code,vd_flag,p_seqno";

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    insertMktBpmh3Data();

    processDisplay(200000); // every 10000 display message
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int insertMktBpmh3Data() throws Exception
 {
  dateTime();
  extendField = "mbdl.";
  setValue("mbdl.active_code"          , getValue("active_code"));
  setValue("mbdl.feedback_date"        , businessDate);
  setValue("mbdl.vd_flag"              , getValue("vd_flag"));
  setValue("mbdl.p_seqno"              , getValue("p_seqno")); 
  setValue("mbdl.acct_type"            , getValue("acct_type"));
  setValue("mbdl.id_p_seqno"           , getValue("id_p_seqno")); 
  setValue("mbdl.card_no"              , getValue("card_no")); 
  setValue("mbdl.dest_amt"             , getValue("dest_amt"));
  setValue("mbdl.dest_cnt"             , getValue("dest_cnt")); 
  setValue("mbdl.feedback_flag"        , "N");
  setValue("mbdl.proc_flag"            , "N");
  setValue("mbdl.proc_date"            , "");
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpmh3_data";

  insertTable();

  if (dupRecord.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 void loadBilContract() throws Exception
 {
  extendField = "cont.";
  selectSQL = "contract_no";
  daoTable  = "bil_contract";
  whereStr  = "WHERE contract_no in ( " 
            + "      select contract_no "
            + "      from   mkt_bpmh3_dtl "
            + "      where  acct_code = 'IT') "
            + "and   refund_apr_flag = 'Y' "
            ;

  int  n = loadTable();
  setLoadData("cont.contract_no");
  showLogMessage("I","","Load bil_contract Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktBpmh3DataOld() throws Exception
 {
  selectSQL = "rowid as rowid";
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "where feedback_date < ? "
            ;
  setString(1 , comm.nextMonthDate(businessDate,-60)); 

  openCursor();

  showLogMessage("I","","    刪除 mkt_bpmh3_data[" + comm.nextMonthDate(businessDate,-60) +"]");

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    if (deleteMktBpmh3DataOld()!=0) break;
    if (totalCnt>=10000) break;
   } 
  closeCursor();

  showLogMessage("I","","    累計  [" + totalCnt +"] 筆");

  return(0);
 }
// ************************************************************************
 int deleteMktBpmh3DataOld() throws Exception
 {
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "where  rowid = ? ";

  setRowId(1 , getValue("rowid")); 

  deleteTable();

  if (notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

