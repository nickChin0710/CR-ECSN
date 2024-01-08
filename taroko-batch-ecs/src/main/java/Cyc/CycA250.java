/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/20  V1.00.18  Allen Ho   mkt_D064                                   *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                             *
* 112/03/30  V1.00.02    Zuwei Su  新增[ 一般消費群組 ]參數帳單資料篩選處理                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA250 extends AccessDAO
{
 private final String PROGNAME = "紅利-紅利特惠活動(五) [加贈] 符合消費名單處理程式 112/03/30  V1.00.02";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;
 CommCrdRoutine comcr = null;

 String hBusinessDate   = "";
 String activeCode     = "";
 String tranSeqno = "";
 String[] procWork  = new String[300];

 int cycleFlag = 0;
 int totalAmtPlus=0,totalAmtMinus=0;
 double[][] addAmtS2 = new double[300][10];
 double[][] addAmtE2 = new double[300][10];

 long    totalCnt=0,updateCnt=0;
 int parmCnt=0,procMonths=1;
 int cnt1=0;
 String feedbackType = "";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA250 proc = new CycA250();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 2)
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
      { hBusinessDate = args[1]; }

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
   
   selectMktBpmh3();
   if (parmCnt==0) 
      {
       showLogMessage("I","","今日 ["+ hBusinessDate +"]無參數條件需處理! ");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktMchtgpData();
   loadMktBnData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理(mkt_bpmh3_data)條件資料");
   selectMktBpmh3Data();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");

/*
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   for (int inti=0;inti<parmCnt;inti++)
       {
        if (matchCnt[inti]==0) continue;
        showLogMessage("I","","活動代碼 ["+getValue("active_code",inti)+"] 處理 "+matchCnt[inti]+" 筆");
       }
   showLogMessage("I","","=========================================");
*/

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

  if (hBusinessDate.length()==0)
      hBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusinessDate+"]");
 }
// ************************************************************************ 
 int selectMktBpmh3Data() throws Exception
 {
  selectSQL = "p_seqno,"
            + "active_code,"
            + "vd_flag";
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "WHERE feedback_date = ?  "
            + "and   proc_flag     = 'N' "
            ;

  setString(1, hBusinessDate);

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(2 , activeCode);
     }            

  whereStr  = whereStr 
            + "group by p_seqno,active_code,vd_flag ";

  openCursor();

  int retCnt =0;
  while( fetchTable() ) 
   {
    for (int inti=0;inti<parmCnt;inti++)
      {
       if (procWork[inti].equals("N")) continue;

       if (!getValue("parm.active_code",inti).equals(getValue("active_code"))) continue;

       setValue("data_key", getValue("parm.active_code",inti));

       if (getValue("parm.doorsill_flag",inti).equals("Y"))
          {
           setValue("doorsill_amt" , "0");
           setValue("doorsill_cnt" , "0");

           if (getValue("vd_flag").equals("Y"))
              retCnt =selectDbbBill(inti);
           else
              retCnt =selectBilBill(inti);

           if (retCnt==0)
              {
               updateMktBpmh3Dtl("X");
               updateMktBpmh3Data1();
              }
           else
              {
               if (checkDoorsill(inti)==0)
                  {
                   updateMktBpmh3Dtl("X");
                   updateMktBpmh3Data("X");
                  }
               else
                  {
                   updateMktBpmh3Dtl("Y");
                   updateMktBpmh3Data("N");
                  }

              }
          }
       else
          updateMktBpmh3Dtl("Y");
      }
    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();

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
            + "and   doorsill_flag = 'Y' "
            ;
  whereStr += " and run_time_type = ?";
  int i = 1;
  setString(i++ , hBusinessDate);
  setString(i++ , hBusinessDate);
  setString(i++ , hBusinessDate);
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
     procWork[inti] = "Y";

     addAmtS2[inti][0] = getValueDouble("parm.d_add_amt_s1",inti);
     addAmtS2[inti][1] = getValueDouble("parm.d_add_amt_s2",inti);
     addAmtS2[inti][2] = getValueDouble("parm.d_add_amt_s3",inti);
     addAmtS2[inti][3] = getValueDouble("parm.d_add_amt_s4",inti);
     addAmtS2[inti][4] = getValueDouble("parm.d_add_amt_s5",inti);
     addAmtS2[inti][5] = getValueDouble("parm.d_add_amt_s6",inti);
     addAmtS2[inti][6] = getValueDouble("parm.d_add_amt_s7",inti);
     addAmtS2[inti][7] = getValueDouble("parm.d_add_amt_s8",inti);
     addAmtS2[inti][8] = getValueDouble("parm.d_add_amt_s9",inti);
     addAmtS2[inti][9] = getValueDouble("parm.d_add_amt_s10",inti);

     addAmtE2[inti][0] = getValueDouble("parm.d_add_amt_e1",inti);
     addAmtE2[inti][1] = getValueDouble("parm.d_add_amt_e2",inti);
     addAmtE2[inti][2] = getValueDouble("parm.d_add_amt_e3",inti);
     addAmtE2[inti][3] = getValueDouble("parm.d_add_amt_e4",inti);
     addAmtE2[inti][4] = getValueDouble("parm.d_add_amt_e5",inti);
     addAmtE2[inti][5] = getValueDouble("parm.d_add_amt_e6",inti);
     addAmtE2[inti][6] = getValueDouble("parm.d_add_amt_e7",inti);
     addAmtE2[inti][7] = getValueDouble("parm.d_add_amt_e8",inti);
     addAmtE2[inti][8] = getValueDouble("parm.d_add_amt_e9",inti);
     addAmtE2[inti][9] = getValueDouble("parm.d_add_amt_e10",inti);

     if (getValue("parm.run_start_month",inti).length()!=0)
        {
         if (getValue("parm.run_start_month",inti).compareTo(hBusinessDate.substring(0,6))>0)
            {
             procWork[inti] = "N";
             continue;
            }
        }

     if (getValue("parm.run_time_type",inti).equals("1"))
        {
         if (cycleFlag!=0) 
            {
             procWork[inti] = "N";
             continue;
            }
        }
     else
        {
         if (!hBusinessDate.substring(6,8).equals(
             String.format("%02d",getValueInt("parm.run_time_dd",inti))))
            {
             procWork[inti] = "N";
             continue;
            }
        }
    }

  return(0);
 }
// ************************************************************************
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "this_acct_month,"
            + "stmt_cycle";
  daoTable  = "ptr_workday";
  whereStr  = "WHERE this_close_date = ? ";
  
  setString(1, hBusinessDate);

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
            + "and   b.data_type  in ('G','P2') "
            + "order by b.data_key,b.data_type,a.data_code"
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
            + "and   data_type >= 'A' "
            + "and   data_type != 'G' " 
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 void updateMktBpmh3Dtl(String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_date       = ?, "
            + "proc_flag       = ?, "
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";  
  daoTable  = "mkt_bpmh3_dtl";
  whereStr  = "WHERE active_code  = ? "
            + "and   p_seqno      =  ?  "
            + "and   vd_flag      =  ?  "
            ;

  setString(1 , hBusinessDate);
  setString(2 , procFlag);
  setString(3 , javaProgram);
  setString(4 , getValue("active_code"));
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("vd_flag"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktBpmh3Data(String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag       = ?,"
            + "doorsill_cnt    = ?, "
            + "doorsill_amt    = ?, "
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";  
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "WHERE active_code   = ? "
            + "and   vd_flag       = ?  "
            + "and   p_seqno       = ?  "
            + "and   feedback_date = ? "
            ;

  setString(1 , procFlag);
  setInt(2    , getValueInt("doorsill_cnt"));
  setDouble(3 , getValueDouble("doorsill_amt"));
  setString(4 , javaProgram);
  setString(5 , getValue("active_code"));
  setString(6 , getValue("vd_flag"));
  setString(7 , getValue("p_seqno"));
  setString(8 , hBusinessDate); 

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktBpmh3Data1() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag       = 'X',"
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";  
  daoTable  = "mkt_bpmh3_data";
  whereStr  = "WHERE active_code   = ? "
            + "and   vd_flag       = ?  "
            + "and   p_seqno       = ?  "
            + "and   feedback_date = ? "
            ;

  setString(1 , javaProgram);
  setString(2 , getValue("active_code"));
  setString(3 , getValue("vd_flag"));
  setString(4 , getValue("p_seqno"));
  setString(5 , hBusinessDate); 

  updateTable();
  return;
 }
// ************************************************************************
 int selectBilBill(int inti) throws Exception
 {
  extendField = "bill.";
  daoTable  = "bil_bill";
  whereStr  = "WHERE acct_type not in ('02','03') "
            + "and   acct_code  in ('BL','ID','IT','AO','OT','CA')  "
            + "and   rsk_type not in ('1','2','3') "
            + "and   merge_flag       != 'Y'  "
            + "and   p_seqno = ? " 
            ;

  setString(1 , getValue("p_seqno"));
            
  if (getValue("parm.run_time_type").equals("1"))
     {
      whereStr  = whereStr  
                + " and acct_month = ? "
                + " and stmt_cycle = ? ";

      setString(2 , hBusinessDate.substring(0,6));
      setString(3 , hBusinessDate.substring(6,8));
     }
  else
     {
      whereStr  = whereStr  
                + " and purchase_date between ? and ? ";

      setString(2 , comm.lastMonth(hBusinessDate ,-1)+"01");
      setString(3 , comm.lastMonth(hBusinessDate ,-1)+"31");

     }

  int recCnt = selectTable();

  String acqId ="";
  int matchFlag=0;
  for (int intm=0;intm<recCnt;intm++)
   { 
    acqId ="";

    if (getValue("bill.acq_member_id",intm).length()!=0)
        acqId = comm.fillZero(getValue("bill.acq_member_id",intm),8);

    if (selectMktBnData(getValue("bill.group_code",intm),getValue("bill.card_type",intm),
                           getValue("parm.d_group_card_sel",inti),"A",2)!=0) continue;

    if (selectMktBnData(getValue("bill.mcht_no",intm),acqId,
                           getValue("parm.d_merchant_sel",inti),"B",3)!=0) continue; 
    
    if (selectMktMchtgpData(getValue("bill.mcht_no",intm),acqId,
            getValue("parm.d_mcht_group_sel",inti),"G")!=0) continue; 

    //if (selectMktMchtgpData(getValue("bill.ecs_platform_kind",intm),acqId,
    //                           getValue("parm.platform_kind_sel",inti),"P")!=0) continue; 

    if (selectMktMchtgpData(getValue("bill.ecs_cus_mcht_no",intm),"", 
                               getValue("parm.platform2_kind_sel",inti),"P2")!=0) continue; 

    if (selectMktBnData(getValue("bill.card_type",intm),
                           getValue("parm.d_card_type_sel",inti),"F",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.mcht_category",intm),
                           getValue("parm.d_mcc_code_sel",inti),"C",3)!=0) continue; 

    if ((!getValue("parm.d_bl_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("BL"))) continue; 
    if ((!getValue("parm.d_ca_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("CA"))) continue; 
    if ((!getValue("parm.d_id_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("ID"))) continue; 
    if ((!getValue("parm.d_ao_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("AO"))) continue; 
    if ((!getValue("parm.d_ot_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("OT"))) continue; 
    if ((!getValue("parm.d_it_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("IT"))) continue; 

    if (getValue("bill.acct_code",intm).equals("IT"))      // 20210112 能惠 mktm0360 
       if (getValue("parm.d_it_flag",inti).equals("1"))
          {
           if (getValueInt("bill.install_curr_term",intm)!=1) continue; 
           setValue("bill.dest_amt",getValue("bill.contract_amt",intm),intm);
          }

    if (selectMktBnData(getValue("bill.bill_type",intm),
                           getValue("parm.d_bill_type_sel",inti),"D",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.bin_type",intm),getValue("bill.source_curr",intm),
                           getValue("parm.d_currency_sel",inti),"E",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.pos_entry_mode",intm),
                           getValue("parm.d_pos_entry_sel",inti),"H",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.ucaf",intm),
                           getValue("parm.d_ucaf_sel",inti),"I",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.ec_ind",intm),
                           getValue("parm.d_eci_sel",inti),"J",3)!=0) continue;

    if (getValue("bill.sign_flag",intm).equals("-"))
       setValueDouble("bill.dest_amt" , getValueDouble("bill.dest_amt",intm)*-1,intm);
    else    
       setValueDouble("bill.dest_amt" , getValueDouble("bill.dest_amt",intm)
                                      + getValueDouble("bill.curr_adjust_amt",intm) , intm);

    setValueDouble("doorsill_amt" ,  getValueDouble("doorsill_amt")
                                  +  getValueDouble("bill.dest_amt",intm));
    setValueInt("doorsill_cnt" , getValueInt("doorsill_cnt")
                                + 1);
    matchFlag=1;
   } 
   
  return(matchFlag);
 }
// ************************************************************************
 int selectDbbBill(int inti) throws Exception
 {
  extendField = "bill.";
  daoTable  = "dbb_bill";
  whereStr  = "WHERE acct_code  in ('BL','ID','IT','AO','OT','CA')  "
            + "and   rsk_type not in ('1','2','3') "
            + "and   p_seqno = ? " 
            + " and purchase_date between ? and ? "
            ;

  setString(1 , getValue("p_seqno"));
  //setString(2 , comm.lastMonth(hBusinessDate ,-1)+"01");
  //setString(3 , comm.lastMonth(hBusinessDate ,-1)+"31");
  setString(2 , comm.lastMonth(hBusinessDate ,1)+"01");
  setString(3 , comm.lastMonth(hBusinessDate ,1)+"31");

  int recCnt = selectTable();

  String acqId ="";
  int matchFlag=0;
  for (int intm=0;intm<recCnt;intm++)
   { 
    acqId ="";

    if (getValue("bill.acq_member_id",intm).length()!=0)
        acqId = comm.fillZero(getValue("bill.acq_member_id",intm),8);

    if (selectMktBnData(getValue("bill.group_code",intm),getValue("bill.card_type",intm),
                           getValue("parm.d_group_card_sel",inti),"A",2)!=0) continue;

    if (selectMktBnData(getValue("bill.mcht_no",intm),acqId,
                           getValue("parm.d_merchant_sel",inti),"B",3)!=0) continue; 

    if (selectMktMchtgpData(getValue("bill.mcht_no",intm),acqId,
                               getValue("parm.d_mcht_group_sel",inti),"G")!=0) continue; 

    //if ((getValue("bill.ecs_platform_kind",intm),acqId,
    //                           getValue("parm.platform_kind_sel",inti),"P")!=0) continue; 
    if (selectMktMchtgpData(getValue("bill.ecs_cus_mcht_no",intm) , "" , 
                               getValue("parm.platform2_kind_sel",inti),"P2")!=0) continue; 

    if (selectMktBnData(getValue("bill.card_type",intm),
                           getValue("parm.d_card_type_sel",inti),"F",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.mcht_category",intm),
                           getValue("parm.d_mcc_code_sel",inti),"C",3)!=0) continue; 

    if ((!getValue("parm.d_bl_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("BL"))) continue; 
    if ((!getValue("parm.d_ca_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("CA"))) continue; 
    if ((!getValue("parm.d_id_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("ID"))) continue; 
    if ((!getValue("parm.d_ao_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("AO"))) continue; 
    if ((!getValue("parm.d_ot_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("OT"))) continue; 
    if ((!getValue("parm.d_it_cond",inti).equals("Y"))&&(getValue("billacct_code",intm).equals("IT"))) continue; 

    if (selectMktBnData(getValue("bill.bill_type",intm),
                           getValue("parm.d_bill_type_sel",inti),"D",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.bin_type",intm),getValue("bill.source_curr",intm),
                           getValue("parm.d_currency_sel",inti),"E",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.pos_entry_mode",intm),
                           getValue("parm.d_pos_entry_sel",inti),"H",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.ucaf",intm),
                           getValue("parm.d_ucaf_sel",inti),"I",3)!=0) continue; 

    if (selectMktBnData(getValue("bill.ec_ind",intm),
                           getValue("parm.d_eci_sel",inti),"J",3)!=0) continue;

    if (getValue("bill.sign_flag",intm).equals("-"))
       setValueDouble("bill.dest_amt" , getValueDouble("bill.dest_amt",intm)*-1,intm);
    else    
       setValueDouble("bill.dest_amt" , getValueDouble("bill.dest_amt",intm)
                                      + getValueDouble("bill.curr_adjust_amt",intm) , intm);

    setValueDouble("doorsill_amt" ,  getValueDouble("doorsill_amt")
                                  +  getValueDouble("bill.dest_amt",intm));
    setValueInt("doorsill_cnt" , getValueInt("doorsill_cnt")
                                + 1);
    matchFlag=1;
   } 
   
  return(matchFlag);
 }
// ************************************************************************
 int  checkDoorsill(int inti) throws Exception
 {
  double tempAmt;
  int    matchFlag =0;

  if (getValue("parm.d_add_item_flag",inti).equals("1"))
     tempAmt = getValueDouble("doorsill_amt");
  else
     tempAmt = getValueDouble("doorsill_cnt");

  for (int inta=0;inta<10;inta++)
    {
     if (addAmtE2[inti][inta]==0) break;

     if ((tempAmt>=addAmtS2[inti][inta])&&
         (tempAmt<=addAmtE2[inti][inta]))
        {
         matchFlag =1;
        }
     else {continue;}
    }
  return(matchFlag);
 }
// ************************************************************************


}  // End of class FetchSample

