/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/03/02  V1.00.18  Allen Ho   dbm_m140                                   *
 * 111/11/07  V1.00.19  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM140 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-回存扣回處理程式 110/03/02 V1.00.18";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String dataKey = "";
 String idPSeqno = "";

 long    totalCnt=0;
 int parmCnt=0,cnt1=0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM140 proc = new DbmM140();
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

   if (args.length > 2)
   {
    showLogMessage("I","","請輸入參數:");
    showLogMessage("I","","PARM 1 : [business_date]");
    showLogMessage("I","","PARM 2 : [id_p_seqno]");
    return(1);
   }

   if ( args.length >= 1 )
   { businessDate = args[0]; }

   if ( args.length == 2 )
   { idPSeqno = args[1]; }

   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadDbmBnData();
   loadMktMchtgpData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectDbmBpid();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入回存暫存資料");
   loadDbaAcaj();
   showLogMessage("I","","=========================================");
   if (idPSeqno.length()>0)
    showLogMessage("I",""," DEBUG Mode id_p_seqno["+ idPSeqno +"]");
   showLogMessage("I","","回存扣回資料");
   selectDbbBill();
   showLogMessage("I","","=========================================");

   if (idPSeqno.length()==0)
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
 void selectDbbBill() throws Exception
 {
  selectSQL = "a.mcht_no,"
          + "a.reference_no,"
          + "a.acct_type,"
          + "a.id_p_seqno,"
          + "a.mcht_category,"
          + "a.mcht_country,"
          + "decode(a.mcht_category,'6010','3','6011','3',"
          + "    decode(a.mcht_country,'TW','1','2')) as item_code,"
          + "a.issue_date,"
          + "a.mcht_category,"
          + "a.pos_entry_mode,"
          + "decode(a.group_code,'','0000',group_code) as group_code,"
          + "a.purchase_date,"
          + "a.card_no,"
          + "a.mcht_no,"
          + "a.sign_flag,"
          + "b.deduct_amt,"
          + "a.acq_member_id";
  daoTable  = "dbb_bill a,dba_acaj b";
  whereStr  = "where a.acct_code = 'BL' "
          + " and  b.deduct_amt > 0 "
          + " and  b.deduct_proc_code = '00' "
          + " and  (b.adjust_type like 'DE%' "
          + "  or   b.adjust_type = 'DP01' "
          + "  or   b.adjust_type ='RE20') "
          + " and  b.proc_flag = 'Y' "
          + " and  b.deduct_proc_date =  ? "
          + " and  a.reference_no = b.reference_no "
  ;

  setString(1 , comm.lastDate(businessDate));

  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and id_p_seqno = ? ";
   setString(2 , idPSeqno);
  }

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

  int inti=0;
  totalCnt = 0;
  String acqId ="";
  int cnt1 = 0;
  int okFlag = 0;
  while( fetchTable() )
  {
   totalCnt++;

   if (idPSeqno.length()>0)
   {
    showLogMessage("I","","STEP 0.1   : ["+ getValue("reference_no")  +"]");
    showLogMessage("I","","STEP 0.2   : ["+ getValue("mcht_category")  +"]");
    showLogMessage("I","","STEP 0.3   : ["+ getValue("mcht_country")  +"]");
   }
   okFlag=0;
   for (int intk=0;intk<parmCnt;intk++)
   {
    if (!getValue("item_code").equals(getValue("parm.item_code",intk))) continue;
    if (!getValue("acct_type").equals(getValue("parm.acct_type",intk))) continue;
    okFlag = 1;
    inti = intk;
    break;
   }
   if (okFlag==0) continue;

   parmArr[inti][0]++;

   setValue("acaj.reference_no" , getValue("reference_no"));
   cnt1 = getLoadData("acaj.reference_no");
   if (cnt1<=0) continue;

   parmArr[inti][1]++;
   setValue("group_code",getValue("dcrd.group_code"));

   setValue("data_key" , getValue("parm.years",inti)
           + getValue("parm.acct_type",inti)
           + getValue("parm.item_code",inti));

   acqId = "";
   if (getValue("acq_member_id").length()!=0)
    acqId = comm.fillZero(getValue("acq_member_id"),8);

   if (selectDbmBnData(getValue("mcht_no"), acqId,
           getValue("parm.merchant_sel",inti),"1",3)!=0) continue;

   parmArr[inti][2]++;

   if (selectDbmBnData(getValue("group_code"),
           getValue("parm.group_code_sel",inti),"2",3)!=0) continue;

   parmArr[inti][3]++;

   if (selectDbmBnData(getValue("pos_entry_mode"),
           getValue("parm.pos_entry_sel",inti),"4",3)!=0) continue;

   parmArr[inti][4]++;

   if (selectDbmBnData(getValue("mcht_category"),
           getValue("parm.mcc_code_sel",inti),"5",3)!=0) continue;

   parmArr[inti][5]++;

   if (selectMktMchtgpData(getValue("mcht_no"),acqId,
           getValue("parm.mcht_group_sel",inti),"6")!=0) continue;

   parmArr[inti][6]++;

   setValue("bdtl.mod_desc   " , "回存紅利扣回");
   setValue("bdtl.active_name" , "回存紅利扣回");
   setValue("bdtl.tax_flag"    , "N");
   setValue("bdtl.active_code" ,  "A0" + getValue("data_key"));
   setValue("bdtl.tran_code"   , "3");
   setValue("bdtl.mod_desc"    , "VD紅回存紅利調整");
   setValue("bdtl.mod_memo"    , "參考編號["+getValue("reference_no")+"] 回存["+getValueInt("deduct_amt")+"]元");

   int deductBp = 0;

   if (getValue("parm.bp_type",inti).equals("1"))
   {
    deductBp = getValueInt("parm.give_bp",inti);
   }
   else
   {
    if (getValueInt("deduct_amt") >= getValueInt("parm.bp_amt",inti) )
    {
     int deductBpInt = (int)Math.ceil(getValueInt("deduct_amt")
             /getValueInt("parm.bp_amt",inti));
     deductBp = (int)Math.round(deductBpInt*getValueInt("parm.bp_pnt",inti));
    }
   }

   if (deductBp==0) continue;

   parmArr[inti][7]++;

   setValueInt("bdtl.beg_tran_bp"  , deductBp*-1);
   setValueInt("bdtl.end_tran_bp"  , deductBp*-1);
   insertDbmBonusDtl();
   parmArr[inti][8]++;

   processDisplay(10000); // every 10000 display message
  }
  closeCursor();

  if (idPSeqno.length()>0)
  {
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );
   for (inti=0;inti<parmCnt;inti++)
   {
    for (int intk=0;intk<20;intk++)
    {
     if (parmArr[inti][intk]==0) continue;
     showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
    }
   }
   showLogMessage("I","","=========================================");
  }
 }
 // ************************************************************************
 int selectDbmBpid() throws Exception
 {
  extendField = "parm.";
  daoTable  = "dbm_bpid";
  whereStr  = "WHERE apr_flag = 'Y' "
          + "and   years = ? "
  ;

  setString(1, businessDate.substring(0,4));

  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
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
  whereStr  = "WHERE  deduct_amt > 0 "
          + " and   deduct_proc_code = '00' "
          + " and   (adjust_type like 'DE%' "
          + "  or    adjust_type = 'DP01' "
          + "  or    adjust_type ='RE20') "
          + " and   proc_flag = 'Y' "
          + " and   deduct_proc_date = ? "
          + " group by reference_no "
  ;

  setString(1 , comm.lastDate(businessDate));

  int  n = loadTable();

  setLoadData("acaj.reference_no");

  showLogMessage("I","","Load dba_acaj cnt: ["+n+"]");
 }
 // ************************************************************************
 int insertDbmBonusDtl() throws Exception
 {
  setValue("bdtl.tran_seqno"     , comr.getSeqno("ECS_DBMSEQ"));

  extendField = "bdtl.";
  setValue("bdtl.acct_date"            , businessDate);
  setValue("bdtl.tran_date"            , sysDate);
  setValue("bdtl.tran_time"            , sysTime);
  setValue("bdtl.crt_date"             , sysDate);
  setValue("bdtl.crt_user"             , javaProgram);
  setValue("bdtl.apr_date"             , sysDate);
  setValue("bdtl.apr_user"             , javaProgram);
  setValue("bdtl.apr_flag"             , "Y");
  setValue("bdtl.acct_month"           , businessDate.substring(0,6));
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
          + "and   b.data_type  = '7' "
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
 int selectDbmBnData(String col1, String sel, String data_type, int dataNum) throws Exception
 {
  return selectDbmBnData(col1,"","",sel,data_type,dataNum);
 }
 // ************************************************************************
 int selectDbmBnData(String col1, String col2, String sel, String data_type, int dataNum) throws Exception
 {
  return selectDbmBnData(col1,col2,"",sel,data_type,dataNum);
 }
 // ************************************************************************
 int selectDbmBnData(String col1, String col2, String col3, String sel, String data_type, int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",data_type);

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

}  // End of class FetchSample
