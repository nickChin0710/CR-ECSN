/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/05/31  V1.00.24  Allen Ho   Cyc_A150-1                                 *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                            *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA210 extends AccessDAO
{
 private final String PROGNAME = "關帳-消費紅利特惠專案(一)回饋處理程式 111-11-08  V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate   = "";
 String activeCode     = "";

 int bonuCnt = 0,parmCnt=0 ,cardCnt; 
 String tranSeqno = "",cmpMonth="";
 long    totalCnt=0,updateCnt=0;
   boolean DEBUG = false;
// boolean DEBUG = true;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA210 proc = new CycA210();
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
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }
   
   if ( args.length == 2 )
      { activeCode = args[1]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();
   if (activeCode.length()!=0)
      {
       if (selectPtrWorkday1()!=0)
          {
           showLogMessage("I","","本日非關帳日, 不需執行");
           return(0);
          }
      }
   else if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }
   showLogMessage("I","","this_acct_month["+ getValue("wday.this_acct_month")+"]");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","清除重做資料(mkt_bpmh2_data)");
   deleteMktBpmh2Data();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料(mkt_bpmh2_data)");
   selectMktBpmh2();
   if (parmCnt==0)     
      {
       showLogMessage("I","","今日["+businessDate+"]無活動回饋");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadCrdCard();
   loadBilMerchant();
   loadMktBnData();
   loadMktMchtgpData();
   loadActSysexpLog();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(mkt_bpid_data)資料");
   selectMktBpidData();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理回饋資料(mkt_bpmh2_data)資料");
   selectMktBpmh2Data();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
 {
  selectSQL = "";
  daoTable  = "ptr_businday";
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

  setString(1,businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************ 
 int  selectPtrWorkday1() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where stmt_cycle = ? ";

  setString(1,businessDate.substring(6,8));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  setValue("wday.this_close_date" , businessDate);
  setValue("wday.this_acct_month" , businessDate.substring(0,6));

  return(0);
 }
// ************************************************************************ 
 int selectMktBpidData() throws Exception
 {
  selectSQL = "";
  daoTable  = "mkt_bpid_data";
  whereStr  = "where acct_month  = ? "      
            + "and   stmt_cycle  = ? "       
//          + "and   p_seqno = '0001516820' "        // debug
//debug     + "and   reference_no = '1878488897' "   // debug
            + "and   proc_flag    = 'N' "  
            + "order by p_seqno "
            ;

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  openCursor();

  totalCnt=0;

  int cnt1=0;
  int stoprunFlag=0;

  int[][] parmArr = new int [100][20];
  for (int inti=0;inti<20;inti++)
     for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  while( fetchTable() ) 
   { 
    for (int inti=0;inti<parmCnt;inti++)
        {
         parmArr[inti][0]++;

         if (getValue("parm.issue_cond",inti).equals("Y"))
            {
             parmArr[inti][1]++;
             if (!getValue("card_no").equals(getValue("ori_card_no")))
                {
                 setValue("card.card_no",getValue("card_no"));
                 cnt1 = getLoadData("card.card_no");
                 setValue("ori_issue_date" , getValue("card.ori_issue_date"));
                }

             if ((getValue("ori_issue_date").compareTo(getValue("parm.issue_date_s",inti))<0)||
                 (getValue("ori_issue_date").compareTo(getValue("parm.issue_date_e",inti))>0)) continue;
             parmArr[inti][2]++;

             if (getValueInt("parm.re_months",inti)<comm.monthBetween(getValue("ori_issue_date"),businessDate)) continue;

            }
         parmArr[inti][3]++;
         if (getValue("parm.purch_cond",inti).equals("Y"))
            {
         parmArr[inti][4]++;
             if ((getValue("purchase_date").compareTo(getValue("parm.purch_s_date",inti))<0)||
                 (getValue("purchase_date").compareTo(getValue("parm.purch_e_date",inti))>0)) continue;
            }

         parmArr[inti][5]++;

         if (getValue("parm.new_hldr_cond",inti).equals("Y"))
            if (procCrdCard(inti)!=0) continue;

         parmArr[inti][6]++;
         setValue("data_key", getValue("parm.active_code",inti));

         setValue("match_a_flag" , "Y");
         setValue("match_b_flag" , "Y");
         if (getValue("parm.pre_filter_flag",inti).equals("1"))
            {
             parmArr[inti][7]++;
             if (checkPreFilter1(inti)!=0) setValue("match_a_flag" , "N");
             if (checkPreFilter2(inti)!=0) setValue("match_b_flag" , "N");
            }
         else
            {
             parmArr[inti][8]++;

             stoprunFlag = checkPreFilter1(inti);

             parmArr[inti][11+stoprunFlag]++;

             if (stoprunFlag!=0) continue;

             parmArr[inti][9]++;
             if (checkPreFilter2(inti)!=0) continue;
            }

         parmArr[inti][10]++;
 
         insertMktBpmh2Data(inti);
        }


    totalCnt++;
   } 
  closeCursor();

  if (activeCode.length()>0)
  for (int inti=0;inti<parmCnt;inti++)
    {
     showLogMessage("I","","active_code :["+getValue("parm.active_code",inti)+"]");
     for (int intk=0;intk<20;intk++)
         {
          if (parmArr[inti][intk]==0) continue;
          showLogMessage("I","","絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"] 筆");
         }
    }

  return(0);
 }
// ************************************************************************
 int checkPreFilter1(int inti) throws Exception
 {
  if (getValueInt("parm.limit_amt",inti)!=0) 
     if (Math.abs(getValueDouble("dest_amt"))<getValueDouble("parm.limit_amt",inti)) return(1);

  if (selectMktBnData(getValue("group_code"),
                     getValue("parm.group_code_sel",inti),"2",3)!=0) return(2);

  if (selectMktBnData(getValue("acct_type"),
                         getValue("parm.acct_type_sel",inti),"3",3)!=0) return(3);

  if (selectMktBnData(getValue("card_type"),
                         getValue("parm.card_type_sel",inti),"8",3)!=0) return(4);

  if (selectMktBnData(getValue("bin_type"),getValue("source_curr"),
                         getValue("parm.currency_sel",inti),"7",2)!=0) return(5);

  return(0);
 }
// ************************************************************************
 int checkPreFilter2(int inti) throws Exception
 {
  boolean DEBUG1 = false;
  if (DEBUG1)
  showLogMessage("I",""," STEP A ");

  if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("acct_code").equals("BL"))) return(1); 
  if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("acct_code").equals("CA"))) return(1); 
  if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("acct_code").equals("ID"))) return(1); 
  if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("acct_code").equals("AO"))) return(1); 
  if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("acct_code").equals("OT"))) return(1); 
  if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("acct_code").equals("IT"))) return(1);

  if (DEBUG1)
  showLogMessage("I",""," STEP B ");
   
  if (selectMktBnData(getValue("mcht_category"),
                         getValue("parm.mcc_code_sel",inti),"5",3)!=0) return(1);

  if (DEBUG1)
     {
      showLogMessage("I",""," STEP C1 [" + getValue("pos_entry_mode")+ "]");
      showLogMessage("I",""," STEP C2 [" + getValue("parm.pos_entry_sel",inti) + "]");
     }
  if (selectMktBnData(getValue("pos_entry_mode"),
                         getValue("parm.pos_entry_sel",inti),"L",3)!=0) return(1);  

  if (DEBUG1)
  showLogMessage("I",""," STEP D ");
  if (selectMktBnData(getValue("bin_type"),getValue("source_curr"),
                                 getValue("parm.currencyb_sel",inti),"M",2)!=0) return(1);

  if (DEBUG1)
  showLogMessage("I",""," STEP E ");
  if (selectMktBnData(getValue("bill_type"),"",
                         getValue("parm.bill_type",inti),"6",3)!=0) return(1);
  if (DEBUG1)
  showLogMessage("I",""," STEP F ");

  String acqId = "";
  if (getValue("acq_member_id").length()!=0)
     acqId = comm.fillZero(getValue("acq_member_id"),8);

  if (selectMktBnData(getValue("mcht_no"),acqId,
                         getValue("parm.merchant_sel",inti),"1",3)!=0) return(1);
  if (DEBUG1)
  showLogMessage("I",""," STEP G ");

  if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                             getValue("parm.mcht_group_sel",inti),"B")!=0) return(1);  
  if (DEBUG1)
  showLogMessage("I",""," STEP H ");

  return(0);
 }
// ************************************************************************
 int selectMktBpmh2Data() throws Exception
 {
  selectSQL = "p_seqno,"
            + "active_code,"
            + "max(pre_filter_flag) as pre_filter_flag,"
            + "min(acct_type) as acct_type,"
            + "min(id_p_seqno) as id_p_seqno,"
            + "max(match_a_flag) as match_a_flag,"
            + "sum(decode(match_a_flag,'Y',dest_amt,0)) as dest_amt_a,"
            + "sum(decode(match_b_flag,'Y',dest_amt,0)) as dest_amt,"
            + "max(run_time_amt) as run_time_amt";
  daoTable  = "mkt_bpmh2_data";
  whereStr  = "where bonus_date = ? "
            + "and   proc_flag    = 'N' "
            ;
                        
  setString(1 , businessDate);

  if (activeCode.length()>0)
     {
      whereStr  = whereStr 
                + "and active_code = ? "; 
      setString(2 , activeCode);
     }
  whereStr  = whereStr 
            + "group by active_code,p_seqno ";

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;
    for (int inti=0;inti<parmCnt;inti++)
        {
  if (DEBUG)
  showLogMessage("I",""," STEP A ");
         if (!getValue("parm.active_code",inti).equals(getValue("active_code"))) continue;
         if (getValueDouble("run_time_amt")<getValueDouble("parm.run_time_amt",inti))
            {
             updateMktBpmh2Data("X");
             continue;
            }
         if (getValueDouble("parm.per_point_amt",inti)==0)  setValue("parm.per_point_amt", "1" , inti);
  if (DEBUG)
  showLogMessage("I",""," STEP B ");

         if (getValueDouble("dest_amt_a")<=0)
            {
             updateMktBpmh2Data("4");
             continue;
            }
  if (DEBUG)
  showLogMessage("I",""," STEP C ");
          

         if (getValueDouble("dest_amt_a")<getValueDouble("parm.run_time_amt",inti))
            {
             updateMktBpmh2Data("1");
             continue;
            }
  if (DEBUG)
  showLogMessage("I",""," STEP D ");

         if (getValueDouble("dest_amt")==0)
            {
             updateMktBpmh2Data("2");
             continue;
            }
  if (DEBUG)
  showLogMessage("I",""," STEP E ");
 
         int begTranBp = (int)Math.floor(getValueDouble("dest_amt")
                         / getValueDouble("parm.per_point_amt",inti));

             begTranBp = (int)Math.floor(begTranBp
                         * getValueDouble("parm.add_times",inti));

/*  
  showLogMessage("I",""," STEP b ["+ getValueDouble("dest_amt") 
                       + "]/[" 
                       + getValueDouble("parm.per_point_amt",inti)
                       + "] * ["
                       + getValueDouble("parm.add_times",inti)
                       + "] = ["
                       +  beg_tran_bp
                       );
*/  

         if (begTranBp ==0)
            {
             updateMktBpmh2Data("3");
             continue;
            }
  if (DEBUG)
  showLogMessage("I",""," STEP F ");
             
         if (begTranBp >0)
            { 
             begTranBp = begTranBp + getValueInt("parm.add_point",inti);
             if (getValueInt("parm.feedback_lmt",inti)>0)
                if (begTranBp > getValueInt("parm.feedback_lmt",inti))
                   begTranBp = getValueInt("parm.feedback_lmt",inti);
            }
         else                
            begTranBp = begTranBp - getValueInt("parm.add_point",inti);

         setValueInt("modl.beg_tran_bp"       , begTranBp);
         setValueInt("modl.end_tran_bp"       , begTranBp);

         insertMktBonusDtl(inti);
         updateMktBpmh2Data("Y");
         break;
        }
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktBpmh2() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_bpmh2";
  whereStr  = "WHERE (stop_flag  = 'N' "
            + " or    (stop_flag = 'Y' "
            + "  and   stop_date > ? )) "
            + "and   ? between decode(active_month_s,'','000000',active_month_s) "
            + "        and     decode(active_month_e,'','999999',active_month_e) "
            ;

  setString(1 , businessDate);
  setString(2 , getValue("wday.this_acct_month"));

  if (activeCode.length()>0)
     {
      whereStr  = whereStr 
                + "and active_code = ? "; 
      setString(3 , activeCode);
     }

  whereStr  = whereStr 
            + "order by active_code ";

  parmCnt = selectTable();

  String startMonth="";

  for (int inti=0;inti<parmCnt;inti++)
      {
       showLogMessage("I","","活動代號 : ["+ getValue("parm.active_code",inti) +"]-[" + getValue("parm.active_name",inti) +"]");
       setValue("proc_flag" , "Y",inti);
      }

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 int insertMktBonusDtl(int inti) throws Exception
 {
  dateTime();
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");

  setValue("modl.active_code"          , getValue("parm.active_code",inti));
  setValue("modl.active_name"          , getValue("parm.active_name",inti)); 
  setValue("modl.mod_memo"             , "");
  setValue("modl.p_seqno"              , getValue("p_seqno")); 
  setValue("modl.id_p_seqno"           , getValue("id_p_seqno")); 
  setValue("modl.acct_type"            , getValue("acct_type")); 
  setValue("modl.tax_flag"             , getValue("parm.tax_flag",inti));
  setValue("modl.tran_code"            , "2");
  if (getValueInt("modl.beg_tran_bp")>0)
     {
      setValue("modl.mod_desc"             , "利特惠專案(一)回饋");
     }
  else
     {
      setValue("modl.mod_desc"             , "利特惠專案(一)回饋扣回");
     }
  setValue("modl.tran_date"            , sysDate);
  setValue("modl.tran_time"            , sysTime);
  setValue("modl.tran_seqno"           , tranSeqno);
  setValue("mbdl.effect_e_date"   , "");
  if (getValueInt("parm.effect_months",inti)>0)
      setValue("modl.effect_e_date"        , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  setValue("modl.bonus_type"           , getValue("parm.bonus_type",inti));
  setValue("modl.acct_date"            , businessDate);
  setValue("modl.proc_month"           , businessDate.substring(0,6));
  setValue("modl.tran_pgm"             , javaProgram);
  setValue("modl.apr_flag"             , "Y");
  setValue("modl.apr_user"             , javaProgram);
  setValue("modl.apr_date"             , sysDate);
  setValue("modl.crt_user"             , javaProgram);
  setValue("modl.crt_date"             , sysDate);
  setValue("modl.mod_user"             , javaProgram);
  setValue("modl.mod_time"             , sysDate+sysTime);
  setValue("modl.mod_pgm"              , javaProgram);

  extendField = "modl.";
  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertMktBpmh2Data(int inti) throws Exception
 {
  dateTime();
  extendField = "bpmh.";
  setValue("bpmh.acct_month"           , getValue("wday.this_acct_month"));
  setValue("bpmh.active_code"          , getValue("parm.active_code",inti));
  setValue("bpmh.p_seqno"              , getValue("p_seqno")); 
  setValue("bpmh.id_p_seqno"           , getValue("id_p_seqno")); 
  setValue("bpmh.acct_type"            , getValue("acct_type")); 
  setValue("bpmh.reference_no"         , getValue("reference_no")); 
  setValue("bpmh.pre_filter_flag"      , getValue("parm.pre_filter_flag",inti));
  setValue("bpmh.match_a_flag"         , getValue("match_a_flag")); 
  setValue("bpmh.match_b_flag"         , getValue("match_b_flag")); 
  setValue("bpmh.proc_flag"            , "N"); 
  setValue("bpmh.proc_date"            , ""); 
  setValue("bpmh.bonus_date"           , getValue("wday.this_close_date")); 
  setValue("bpmh.dest_amt"             , getValue("dest_amt"));
  setValue("bpmh.run_time_amt"         , getValue("parm.run_time_amt",inti));
  setValue("bpmh.mod_time"             , sysDate+sysTime);
  setValue("bpmh.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpmh2_data";

  insertTable();

  return(0);
 }
// ************************************************************************
 void  loadActSysexpLog() throws Exception
 {
  extendField = "sysp.";
  selectSQL = "reference_no_new as reference_no ";
  daoTable  = "act_sysexp_log";
  whereStr  = "WHERE source_type = '02' ";

  int  n = loadTable();

  setLoadData("sysp.reference_no");

  showLogMessage("I","","Load act_sysexp_log Count: ["+n+"]");
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
  whereStr  = "WHERE TABLE_NAME = 'MKT_BPMH2' "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
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
            + "and   b.TABLE_NAME = 'MKT_BPMH2' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  = 'B' "
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
 void  loadBilMerchant() throws Exception
 {
  extendField = "unon.";
  selectSQL = "mcht_no";
  daoTable  = "bil_merchant";
  whereStr  = "WHERE UNIFORM_NO  = '78506552' "
            + "and   MCC_CODE   in ('6010','4814') ";

  int  n = loadTable();
  setLoadData("unon.mcht_no");
  showLogMessage("I","","Load bil_merchant Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "ori_issue_date,"
            + "card_no ";
  daoTable  = "crd_card";
  whereStr  = "WHERE card_no in ( " 
            + "      select card_no from mkt_bpid_data "
            + "      where  card_no != ori_Card_no ) ";

  int  n = loadTable();
  setLoadData("card.card_no");
  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 String getRuntMonth(String startMonth,String nowMonth,int monthCnt) throws Exception
 {
  if (startMonth.equals("now_month")) return startMonth;
  for (int inti=1;inti<600;inti++)
      {
       if (nowMonth.compareTo(comm.nextMonth(startMonth,inti*monthCnt))>0) continue;
       return comm.nextMonth(startMonth,inti*monthCnt); 
      }

  return startMonth;
 }
// ************************************************************************
 void updateMktBpmh2Data(String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_flag       = ?, "
            + "proc_date       = ?, "
            + "mod_pgm         = ?, "
            + "mod_time        = timestamp_format(?,'yyyymmddhh24miss')";
  daoTable  = "mkt_bpmh2_data";
  whereStr  = "WHERE  p_seqno     = ? "
            + "and    active_code = ? "
            + "and    bonus_date  = ? "
            ;

  setString(1 , procFlag);
  setString(2 , businessDate);
  setString(3 , javaProgram);
  setString(4 , sysDate+sysTime);
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("active_code"));
  setString(7 , businessDate);

  updateTable();
  return;
 }
// ************************************************************************
 void deleteMktBpmh2Data() throws Exception
 {
  daoTable  = "mkt_bpmh2_data";
  whereStr  = "where (bonus_date < ? "
            + " or    bonus_date = ?) ";

  setString(1, comm.nextMonthDate(getValue("wday.this_close_date"),-24));
  setString(2, getValue("wday.this_close_date"));

  if (activeCode.length()>0)
     {
      whereStr  = whereStr 
                + "and active_code = ? ";
      setString(3, activeCode);
     }              

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 int procCrdCard(int inti) throws Exception
 {
  selectCrdCard();

  String lastDate = "";
  for ( int intm=0; intm<cardCnt; intm++ )
      {
      if (getValue("card.group_code",intm).length()==0) setValue("card.group_code" , "0000",intm);

       if (getValue("card.card_no",intm).equals(getValue("card_no"))) continue;

       if (getValue("card.ori_issue_date",intm).compareTo(getValue("ori_issue_date"))>0) continue;

       if (getValue("card.oppost_date",intm).length()==0) return(1);

       lastDate = comm.nextNDate(getValue("ori_issue_date"),
                                  getValueInt("parm.new_hldr_days",inti)*-1);

       if (getValue("card.oppost_date",intm).length()!=0)
          if (getValue("card.oppost_date",intm).compareTo(lastDate)<0) continue;

       if ((!getValue("parm.new_hldr_card" ,inti).equals("Y"))&&
           (getValue("card.sup_flag",intm).equals("0"))) continue;

       if ((!getValue("parm.new_hldr_sup"  ,inti).equals("Y"))&&
           (getValue("card.sup_flag",intm).equals("1"))) continue;

       if (getValue("parm.new_group_cond",inti).equals("Y"))
          {
           if (selectMktBnData(getValue("card.group_code",intm),"1","4",2)==0) return(1);
          }
       else if (!getValue("card.card_indicator",intm).equals("1")) 
          {
           return(1);
          }
      }

  return(0);
 }
// ************************************************************************
 void  selectCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "id_p_seqno,"
            + "a.card_no,"
            + "a.group_code,"
            + "a.sup_flag,"
            + "a.oppost_date,"
            + "b.card_indicator";
  daoTable  = "crd_card a,ptr_acct_type b";
  whereStr  = "WHERE  a.stmt_cycle     = ? "
            + "AND    a.acct_type  = b.acct_type "
            + "AND    a.id_p_seqno = ? "
            ;

  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , getValue("id_p_seqno"));

  cardCnt  = selectTable();

 }
// ************************************************************************
/*
add active_month_s
    active_month_e
    purch_cond

del RUN_START_MONTH
    RUN_TIME_MM
    RUN_TIME_TYPE
    PURCH_DATE_TYPE
    CYCLE_FLAG
*/
}  // End of class FetchSample

