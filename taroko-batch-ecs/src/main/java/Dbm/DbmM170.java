/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/03/02  V1.00.15  Allen Ho   dbm_m210                                   *
* 111/11/07  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM170 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-回存消費紅利加贈扣回處理程式 111/11/07  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String hBusiBusinessDate = "";
 String hDbdlTranSeqno = "";

 long    totalCnt=0;
 int     parmCnt =0,cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM170 proc = new DbmM170();
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
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
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

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
   showLogMessage("I","","=========================================");
   if (!hBusiBusinessDate.substring(6,8).equals("10"))
      {
       showLogMessage("I","","本程式只在每月9日換日後執行,本日為"+ hBusiBusinessDate +"日..");
       showLogMessage("I","","=========================================");
       return(0);
      } 

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數暫存資料");
   loadDbmBnData();
   loadMktMchtgpData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectDbmBpmh();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入交易暫存資料");
   loadDbaAcaj();
   showLogMessage("I","","載入卡片暫存資料");
   loadDbcCard();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理請款資料");
   selectDbbBill();
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

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ hBusiBusinessDate +"]");
 }
// ************************************************************************ 
 void selectDbbBill() throws Exception
 {
  selectSQL = "a.mcht_no,"
            + "a.reference_no,"
            + "a.acct_type,"
            + "a.id_p_seqno,"
            + "a.issue_date,"
            + "a.mcht_category,"
            + "a.pos_entry_mode,"
            + "decode(a.group_code,'','0000',group_code) as group_code,"
            + "a.purchase_date,"
            + "a.card_no,"
            + "a.mcht_no,"
            + "ecs_platform_kind,"
            + "ecs_cus_mcht_no ,"               
            + "a.sign_flag,"
            + "a.acq_member_id";
  daoTable  = "dbb_bill a,dba_acaj b";
  whereStr  = "where a.acct_code = 'BL' "
            + "and   b.txn_code = '06' "
            + " and  b.deduct_amt > 0 "
            + " and  b.deduct_proc_code = '00' "
            + " and  (b.adjust_type like 'DE%' "
            + "  or   b.adjust_type = 'DP01' "
            + "  or   b.adjust_type ='RE20') "
            + " and  b.proc_flag = 'Y' "
            + " and  b.deduct_proc_date like ? "
            + " and  a.reference_no = b.reference_no "
            ;

  setString(1 , comm.lastMonth(hBusiBusinessDate,1)+"%");               

  openCursor();
  int[] purchCnt = new int[parmCnt];
  int[] feedCnt  = new int[parmCnt];

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     {
      feedCnt[inti]=0;
      purchCnt[inti]=0;
      for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;
     }

  totalCnt = 0;
  String acqId ="";
  int cnt1 = 0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    setValue("acaj.reference_no" , getValue("reference_no"));
    cnt1 = getLoadData("acaj.reference_no");
    if (cnt1<=0) continue;

    acqId = "";
    if (getValue("acq_member_id").length()!=0)
       acqId = comm.fillZero(getValue("acq_member_id"),8);

    for (int inti=0;inti<parmCnt;inti++)
        {
         parmArr[inti][0]++;
         
         setValue("data_key" , getValue("parm.active_code",inti));

         if (getValueInt("acaj.deduct_amt") <= getValueInt("parm.bp_amt",inti)) continue;

         parmArr[inti][1]++;

         if (getValue("parm.feedback_sel",inti).equals("0"))
            {
             if ((getValue("purchase_date").compareTo(getValue("parm.purch_s_date",inti))<0)||
                 (getValue("purchase_date").compareTo(getValue("parm.purch_e_date",inti))>0)) continue;
            }
         else
            {
             if (getValue("parm.feedback_sel",inti).equals("1"))
                {
                 if ((getValue("issue_date").compareTo(getValue("parm.activate_s_date",inti))<0)||
                     (getValue("issue_date").compareTo(getValue("parm.activate_e_date",inti))>0)) continue;

                 if (getValue("issue_date").substring(0,6).compareTo(
                     comm.lastMonth(hBusiBusinessDate,(getValueInt("parm.re_months")+1)))<0) continue;

                 if ((getValue("purchase_date").compareTo(getValue("issue_date"))<=0)||
                     (getValue("purchase_date").compareTo(comm.nextMonthDate(
                          getValue("issue_date"),getValueInt("parm.re_months")))>0)) continue;
                }
             else
                {
                 setValue("card.card_no" , getValue("card_no"));
                 cnt1 = getLoadData("card.card_no");
                 if (cnt1==0) continue;

                 if ((getValue("card.activate_date").compareTo(getValue("parm.activate_s_date",inti))<0)||
                     (getValue("card.activate_date").compareTo(getValue("parm.activate_e_date",inti))>0)) continue;

                 if (getValue("card.activate_date").substring(0,6).compareTo(
                     comm.lastMonth(hBusiBusinessDate,(getValueInt("parm.re_months")+1)))<0) continue;

                 if ((getValue("purchase_date").compareTo(getValue("card.activate_date"))<=0)||
                     (getValue("purchase_date").compareTo(comm.nextMonthDate(
                          getValue("card.activate_date"),getValueInt("parm.re_months")))>0)) continue;
                }
            }                   
         parmArr[inti][2]++;

         if (selectDbmBnData(getValue("mcht_no"),acqId,
                                 getValue("parm.merchant_sel",inti),"1",3)!=0)
            continue;
         parmArr[inti][3]++;


         if (selectDbmBnData(getValue("group_code"),
                                getValue("parm.group_code_sel",inti),"2",3)!=0)
            continue;                                                  

         parmArr[inti][4]++;

         if (selectDbmBnData(getValue("acct_type"),
                                getValue("parm.acct_type_sel",inti),"3",3)!=0)
            continue;

         parmArr[inti][5]++;

         if (selectDbmBnData(getValue("pos_entry_mode"),
                                getValue("parm.pos_entry_sel",inti),"4",3)!=0)
            continue;

         parmArr[inti][6]++;

         if (selectDbmBnData(getValue("mcht_category"),
                                getValue("parm.mcc_code_sel",inti),"5",3)!=0)
            continue;

         parmArr[inti][7]++;
         if (selectMktMchtgpData(getValue("mcht_no"),acqId,
                              getValue("parm.mcht_group_sel",inti),"6")!=0) 
            continue;
   
         parmArr[inti][8]++;
         
         if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"), "",getValue("parm.platform_kind_sel", inti),
                 "P") != 0)
            continue;
         parmArr[inti][9]++;            
         

         int deductBpInt = (int)Math.ceil(
                             (getValueInt("acaj.deduct_amt")
                           / getValueInt("parm.bp_amt",inti))
                           * getValueInt("parm.bp_pnt",inti) 
                           );

         int deductBp = (int)Math.round(
                         (deductBpInt
                       * getValueInt("parm.add_times",inti))
                       + getValueInt("parm.add_point",inti));

         if (deductBp==0) continue;

         setValue("bdtl.active_code" , getValue("parm.active_code",inti));
         setValue("bdtl.tax_flag"    , "N");
         setValue("bdtl.active_name" , getValue("parm.active_name",inti));

         setValue("bdtl.mod_desc"  , "日期:"
                                   + getValue("acaj.deduct_proc_date")
                                   + ",退貨"
                                   + getValueInt("acaj.deduct_cnt")
                                   + "筆,金額["
                                   + getValueInt("acaj.deduct_amt")+"]元");
         setValue("bdtl.mod_memo"  , "VD回存消費紅利加贈扣回");
         setValue("bdtl.tran_code" , "7");

         parmArr[inti][10]++;

         setValueInt("bdtl.beg_tran_bp"  , deductBp*-1);
         setValueInt("bdtl.end_tran_bp"  , deductBp*-1);
         insertDbmBonusDtl();
         parmArr[inti][11]++;
        }

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
/* 
  showLogMessage("I","","=========================================");
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );
  for (int inti=0;inti<parmCnt;inti++)
     {
      for (int intk=0;intk<20;intk++)
        {
         if (parmArr[inti][intk]==0) continue;
         showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
        }
   
     }
  showLogMessage("I","","=========================================");
*/  
 }
// ************************************************************************
 void loadDbaAcaj() throws Exception
 {
  extendField = "acaj.";
  selectSQL = "reference_no,"
            + "max(deduct_proc_date) as deduct_proc_date,"
            + "count(*) as deduct_cnt,"
            + "sum(deduct_amt) as deduct_amt";
  daoTable  = "dba_acaj";
  whereStr  = "WHERE  txn_code = '06' "
            + " and   deduct_amt > 0 "
            + " and   deduct_proc_code = '00' "
            + " and   (adjust_type like 'DE%' "
            + "  or    adjust_type = 'DP01' "
            + "  or    adjust_type ='RE20') "
            + " and   proc_flag = 'Y' "
            + " and   deduct_proc_date like ? "
            + " group by reference_no "
            ;

  setString(1 , comm.lastMonth(hBusiBusinessDate,1)+"%");               

  int  n = loadTable();
  setLoadData("acaj.reference_no");

  showLogMessage("I","","Load dba_acaj cnt: ["+n+"]");
 }
// ************************************************************************
 void loadDbcCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "card_no,"
            + "activate_date";
  daoTable  = "dbc_card a";
  whereStr  = "where card_no in "
            + " (select distinct(card_no) from dba_acaj "
            + "  WHERE  txn_code = '06' "
            + "   and   deduct_amt > 0 "
            + "   and   deduct_proc_code = '00' "
            + "   and   (adjust_type like 'DE%' "
            + "    or    adjust_type = 'DP01' "
            + "    or    adjust_type ='RE20') "
            + "   and   proc_flag = 'Y' "
            + "   and   deduct_proc_date like ? ) "
            + "";

  setString(1 , comm.lastMonth(hBusiBusinessDate,1)+"%");               

  int  n = loadTable();
  setLoadData("card.card_no");

  showLogMessage("I","","Load dbc_card cnt: ["+n+"]");
 }
// ************************************************************************
 void loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,dbm_bn_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'DBM_BPMH' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type in ('7','P') "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktMchtgpData(String col1, String col2, String sel, String dataType) throws Exception
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
 void loadDbmBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "dbm_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'DBM_BPMH' "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load dbm_bn_data: ["+n+"]");
 }
// ************************************************************************
 int selectDbmBnData(String col1, String sel, String dataType, int dataNum) throws Exception
 {
  return selectDbmBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectDbmBnData(String col1, String col2, String sel, String dataType, int dataNum) throws Exception
 {
  return selectDbmBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectDbmBnData(String col1, String col2, String col3, String sel, String dataType, int dataNum) throws Exception
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
 void selectDbmBpmh() throws Exception
 {
  extendField = "parm.";
  daoTable  = "dbm_bpmh";
  whereStr  = "WHERE apr_flag = 'Y' "
            + "and   substr(decode(feedback_sel,'0',purch_e_date,activate_e_date),1,6) "
            + "      >= ? "
            ;

  setString(1 , comm.lastMonth(hBusiBusinessDate,1));
                 
  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return;
 }
// ************************************************************************ 
 int insertDbmBonusDtl() throws Exception
 {
  setValue("bdtl.tran_seqno"     , comr.getSeqno("ECS_DBMSEQ"));

  dateTime();
  extendField = "bdtl.";
  setValue("bdtl.tran_date"            , sysDate);
  setValue("bdtl.tran_time"            , sysTime);
  setValue("bdtl.crt_date"             , sysDate);
  setValue("bdtl.crt_user"             , javaProgram);
  setValue("bdtl.apr_date"             , sysDate);
  setValue("bdtl.apr_user"             , javaProgram);
  setValue("bdtl.apr_flag"             , "Y");
  setValue("bdtl.acct_month"           , hBusiBusinessDate.substring(0,6));
  setValue("bdtl.acct_date"            , hBusiBusinessDate);
  setValue("bdtl.bonus_type"           , "BONU");
  setValue("bdtl.batch_no"             , getValue("reference_no"));
  setValue("bdtl.acct_type"            , getValue("acct_type"));
  setValue("bdtl.card_no"              , getValue("card_no"));
  setValue("bdtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("bdtl.tran_pgm"             , javaProgram);
  setValue("bdtl.mod_time"             , sysDate+sysTime);
  setValue("bdtl.mod_pgm"              , javaProgram);
  daoTable  = "dbm_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
