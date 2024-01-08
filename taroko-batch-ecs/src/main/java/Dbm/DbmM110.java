/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/04/23  V1.00.29  Allen Ho   dbm_m110                                   *
 * 111/11/07  V1.00.30  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM110 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-消費加贈點數處理程式 110/04/23 V1.00.29";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String idPSeqno = "";
 String activeCode = "";
 String[] procFlag = new String[100];

 long    totalCnt=0;
 int     parmCnt =0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM110 proc = new DbmM110();
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

   if (args.length > 3)
   {
    showLogMessage("I","","請輸入參數:");
    showLogMessage("I","","PARM 1 : [business_date]");
    showLogMessage("I","","PARM 2 : [active_code]");
    showLogMessage("I","","PARM 3 : [id_p_seqno]");
    return(1);
   }

   if ( args.length >= 1 )
   { businessDate = args[0]; }

   if ( args.length >= 2 )
   { activeCode = args[1]; }

   if ( args.length == 3 )
   { idPSeqno = args[2]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
   showLogMessage("I","","=========================================");
   if (!businessDate.substring(6,8).equals("10"))
   {
    showLogMessage("I","","本程式只在每月9日換日後執行,本日為"+ businessDate +"日..");
    showLogMessage("I","","=========================================");
    return(0);
   }

   showLogMessage("I","","載入參數資料");
   selectDbmBpmh();
   if (parmCnt==0)
   {
    showLogMessage("I","","無參數資料需執行");
    return(0);
   }

   selectDbmSysparm();
   showLogMessage("I","","載入參數暫存資料");
   loadDbmBnData();
   loadMktMchtgpData();
   showLogMessage("I","","載入暫存資料");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入交易暫存資料");
   loadDbaDeductTxn();
   showLogMessage("I","","載入卡片暫存資料");
   loadDbcCard();
   loadDbcCard0();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理請款資料");
   selectDbbBill();
   showLogMessage("I","","=========================================");

   if (idPSeqno.length()==0) finalProcess();
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
  selectSQL = "mcht_no,"
          + "reference_no,"
          + "acct_type,"
          + "id_p_seqno,"
          + "issue_date,"
          + "mcht_category,"
          + "pos_entry_mode,"
          + "decode(group_code,'','0000',group_code) as group_code,"
          + "purchase_date,"
          + "acct_code,"
          + "source_curr,"
          + "mcht_country,"
          + "major_card_no as card_no,"
          + "mcht_no,"
          + "sign_flag,"
          + "acq_member_id";
  daoTable  = "dbb_bill c";
  whereStr  = "where post_date <=   ? "
          + "and   acct_code = 'BL' "
          + "and   purchase_date between ? and ? "
          + "and   sign_flag  = '+' "
  ;

  setString(1 , businessDate);
  setString(2 , comm.lastMonth(businessDate)+"01");
  setString(3 , comm.lastdateOfmonth(comm.lastMonth(businessDate)));

  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and id_p_seqno = ? ";
   setString(4 , idPSeqno);
  }

  openCursor();
  int[] purch_cnt = new int[parmCnt];
  int[] feed_cnt  = new int[parmCnt];

  int[][] parmArr = new int [parmCnt][30];
  for (int inti=0;inti<parmCnt;inti++)
   for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;

  totalCnt = 0;
  String acqId ="";
  String cmpDate = "";
  int ok_flag=0;
  while( fetchTable() )
  {
   totalCnt++;
   setValue("duct.reference_no" , getValue("reference_no"));
   int cnt1 = getLoadData("duct.reference_no");
   if (cnt1<=0) continue;

   if (idPSeqno.length()>0)
   {
    showLogMessage("I","","card_no      ["+ getValue("card_no")  +"]");
    showLogMessage("I","","reference_no ["+ getValue("reference_no")  +"]");
   }

   acqId = "";
   if (getValue("acq_member_id").length()!=0)
    acqId = comm.fillZero(getValue("acq_member_id"),8);

   for (int inti=0;inti<parmCnt;inti++)
   {
    if (procFlag[inti].equals("N")) continue;

    if (idPSeqno.length()>0)
     showLogMessage("I","","ACTIVE_CODE ["+ getValue("parm.active_code",inti)  +"]");

    parmArr[inti][0]++;
    setValue("data_key" , getValue("parm.active_code",inti));

    if (getValueInt("duct.deduct_amt") < getValueInt("parm.bp_amt",inti)) continue;


    if (idPSeqno.length()>0)
    {
     showLogMessage("I","","mcht_country ["+ getValue("mcht_country")  +"]");
     showLogMessage("I","","source_curr  ["+ getValue("source_curr")  +"]");
     showLogMessage("I","","in_bl_cond   ["+ getValue("parm.in_bl_cond")  +"]");
     showLogMessage("I","","out_bl_cond  ["+ getValue("parm.out_bl_cond")  +"]");
    }

    if (checkInOutCond(inti)!=0) continue;

    if (idPSeqno.length()>0)
     showLogMessage("I","","currency ok");
    parmArr[inti][1]++;

    if (getValue("parm.feedback_sel",inti).equals("0"))
    {
     if ((getValue("purchase_date").compareTo(getValue("parm.purch_s_date",inti))<0)||
             (getValue("purchase_date").compareTo(getValue("parm.purch_e_date",inti))>0)) continue;
    }
    else
    {
     setValue("car0.card_no" , getValue("card_no"));
     cnt1 = getLoadData("car0.card_no");
     if (cnt1==0) continue;
     parmArr[inti][2]++;

     setValue("card.id_p_seqno" , getValue("id_p_seqno"));
     cnt1 = getLoadData("card.id_p_seqno");
     if (cnt1==0) continue;

     parmArr[inti][3]++;

     if (!getValue("car0.ori_issue_date").equals(getValue("card.issue_date"))) continue;
     parmArr[inti][4]++;

//           if (select_dbc_card_1(getValue("car0.ori_card_no"))!=0) continue;

     if (getValue("card.change_reason").length()!=0) continue;
     parmArr[inti][5]++;

     if (idPSeqno.length()>0)
     {
      showLogMessage("I","","mcht_country ["+ getValue("mcht_country")  +"]");
      showLogMessage("I","","source_curr  ["+ getValue("source_curr")  +"]");
     }


     if (getValue("parm.feedback_sel",inti).equals("1"))
     {
      cmpDate = comm.nextMonthDate(getValue("card.issue_datee"),
              getValueInt("parm.re_months",inti));

      if (idPSeqno.length()>0)
      {
       showLogMessage("I","","issue_date : ["+ getValue("card.issue_date") +"]");
       showLogMessage("I","","issue_date   ["+ getValue("issue_date") +"]");
       showLogMessage("I","","cmp_date     ["+ cmpDate  +"]");
      }

      if (getValue("issue_date").compareTo(getValue("parm.activate_s_date",inti))<0) continue;
      parmArr[inti][6]++;
      if (getValue("purchase_date").compareTo(cmpDate)>0) continue;
      parmArr[inti][7]++;
     }
     else
     {
      if (getValue("card.activate_date").length()==0) continue;
      cmpDate = comm.nextMonthDate(getValue("card.activate_date"),
              getValueInt("parm.re_months",inti));
      if (idPSeqno.length()>0)
      {
       showLogMessage("I","","active_date : ["+ getValue("card.activate_date") +"]");
       showLogMessage("I","","purchase_date ["+ getValue("purchase_date") +"]");
       showLogMessage("I","","cmp_date      ["+ cmpDate  +"]");
      }

      if (getValue("card.activate_date").compareTo(getValue("parm.activate_s_date",inti))<0) continue;
      parmArr[inti][8]++;
      if (getValue("purchase_date").compareTo(cmpDate)>0) continue;
      parmArr[inti][9]++;
     }
    }

    parmArr[inti][10]++;
    if (selectDbmBnData(getValue("mcht_no"),acqId,
            getValue("parm.merchant_sel",inti),"1",3)!=0)
     continue;

    parmArr[inti][11]++;
    if (selectDbmBnData(getValue("group_code"),
            getValue("parm.group_code_sel",inti),"2",3)!=0)
     continue;

    parmArr[inti][12]++;
    if (selectDbmBnData(getValue("acct_type"),
            getValue("parm.acct_type_sel",inti),"3",3)!=0)
     continue;

    parmArr[inti][13]++;
    if (selectDbmBnData(getValue("pos_entry_mode"),
            getValue("parm.pos_entry_sel",inti),"4",3)!=0)
     continue;

    parmArr[inti][14]++;
    if (idPSeqno.length()>0)
    {
     showLogMessage("I","","mcc_code     ["+ getValue("mcht_category")  +"]");
     showLogMessage("I","","mcc_code_sel ["+ getValue("parm.mcc_code_sel",inti)  +"]");
    }

    if (selectDbmBnData(getValue("mcht_category"),
            getValue("parm.mcc_code_sel",inti),"5",3)!=0)
     continue;

    parmArr[inti][15]++;
    if (selectMktMchtgpData(getValue("mcht_no"),acqId,
            getValue("parm.mcht_group_sel",inti),"6")!=0)
     continue;

    if (idPSeqno.length()>0)
     showLogMessage("I","","feedback");

    setValue("bdtl.active_code" , getValue("parm.active_code",inti));
    setValue("bdtl.tax_flag"    , getValue("parm.tax_flag",inti));
    setValue("bdtl.active_name" , getValue("parm.active_name",inti));

    if (getValueInt("duct.deduct_amt") > 0)
    {
     setValue("bdtl.tran_code" , "2");
     setValue("bdtl.mod_memo"  , "VD紅利消費特惠贈送");
    }
    else
    {
     setValue("bdtl.tran_code" , "3");
     setValue("bdtl.mod_memo"  , "VD紅利消費特惠退貨調整");
    }

    int deductBpInt = (int)Math.ceil(
            (getValueInt("duct.deduct_amt")
                    / getValueInt("parm.bp_amt",inti))
                    * getValueInt("parm.bp_pnt",inti)
    );

    int deductBp = (int)Math.round(
            (deductBpInt
                    * getValueInt("parm.add_times",inti))
                    + getValueInt("parm.add_point",inti));

    if (deductBp==0) continue;

    parmArr[inti][16]++;

    setValueInt("bdtl.beg_tran_bp"  , deductBp);
    setValueInt("bdtl.end_tran_bp"  , deductBp);
    insertDbmBonusDtl();
    parmArr[inti][17]++;
   }
   processDisplay(50000); // every 10000 display message
  }
  closeCursor();

  showLogMessage("I","","=========================================");
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );

  if (idPSeqno.length()>0)
   for (int inti=0;inti<parmCnt;inti++)
   {
    showLogMessage("I","","active_code :["+getValue("parm.active_code",inti)+"]");
    for (int intk=0;intk<30;intk++)
    {
     if (parmArr[inti][intk]==0) continue;
     showLogMessage("I","","絆腳石  :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
    }
   }
 }
 // ************************************************************************
 void selectDbmBpmh() throws Exception
 {
  extendField = "parm.";
  daoTable  = "dbm_bpmh";
  whereStr  = "WHERE apr_flag = 'Y' "
  ;

  if (activeCode.length()>0)
  {
   whereStr  = whereStr
           + " and active_code  = ? ";
   setString(1 , activeCode);
  }

  parmCnt = selectTable();

  for (int inti=0;inti<parmCnt;inti++)
  {
   procFlag[inti] = "N";

   if (getValue("parm.feedback_sel").equals("0"))
   {
    if (comm.lastMonth(businessDate,1).compareTo(getValue("parm.purch_s_code",inti))<0) continue;
    if (comm.lastMonth(businessDate,1).compareTo(getValue("parm.purch_e_code",inti))>0) continue;
   }
   else
   {
    if (comm.lastMonth(businessDate,1).compareTo(getValue("parm.activate_s_code",inti))<0) continue;
   }

   showLogMessage("I","","活動代號  : ["+ getValue("parm.active_code",inti) +"][" + getValue("parm.active_name",inti)+"]");

   if (getValue("parm.feedback_sel",inti).equals("0"))
    showLogMessage("I","","  消費期間: ["+ getValue("parm.purch_s_date",inti) +"-" + getValue("parm.purch_e_date",inti)+"]");
   else if (getValue("parm.feedback_sel",inti).equals("1"))
    showLogMessage("I","","  發卡期間: ["+ getValue("parm.activate_s_date",inti) +"-" + getValue("parm.activate_e_date",inti)+"]");
   else if (getValue("parm.feedback_sel",inti).equals("2"))
    showLogMessage("I","","  開卡期間: ["+ getValue("parm.activate_s_date",inti) +"-" + getValue("parm.activate_e_date",inti)+"]");

   procFlag[inti] = "Y";
  }

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return;
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
  setValue("bdtl.effect_e_date"        , "");
  setValue("bdtl.effect_flag"          , "");
  if (getValueInt("bdtl.end_tran_bp")>0)
  {
   setValue("bdtl.effect_e_date"    , comm.nextMonthDate(businessDate,getValueInt("dbmp.effect_months")));
   setValue("bdtl.effect_flag"      , "1");
  }

  setValue("bdtl.acct_month"           , businessDate.substring(0,6));
  setValue("bdtl.acct_date"            , businessDate);
  setValue("bdtl.bonus_type"           , "BONU");
  setValue("bdtl.batch_no"             , getValue("reference_no"));
  setValue("bdtl.acct_type"            , getValue("acct_type"));
  setValue("bdtl.card_no"              , getValue("card_no"));
  setValue("bdtl.id_p_seqno"           , getValue("duct.id_p_seqno"));
  setValue("bdtl.tran_pgm"             , javaProgram);
  setValue("bdtl.mod_time"             , sysDate+sysTime);
  setValue("bdtl.mod_pgm"              , javaProgram);
  daoTable  = "dbm_bonus_dtl";

  insertTable();

  return(0);
 }
 // ************************************************************************
 void loadDbaDeductTxn() throws Exception
 {
  extendField = "duct.";
  selectSQL = "reference_no,"
          + "max(id_p_seqno) as id_p_seqno,"
          + "sum(deduct_amt)as deduct_amt";
  daoTable  = "dba_deduct_txn";
  whereStr  = "where deduct_proc_date >= ? "
          + "and   purchase_date like ? "
          + "and   acct_code = 'BL' "
          + "and   deduct_amt > 0 "
          + "and   deduct_proc_date <= ? "
  ;

  setString(1 , comm.lastMonth(businessDate, 1)+"01");
  setString(2 , comm.lastMonth(businessDate,1)+"%");
  setString(3 , businessDate);

  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and id_p_seqno = ? ";
   setString(4 , idPSeqno);
  }

  whereStr  = whereStr
          + "group by reference_no ";

  int  n = loadTable();
  setLoadData("duct.reference_no");

  showLogMessage("I","","Load dba_deduct_txn count: ["+n+"]");
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

  showLogMessage("I","","Load dbm_bn_data Count ["+n+"]");
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
 void loadDbcCard0() throws Exception
 {
  extendField = "car0.";
  selectSQL = "card_no,"
          + "ori_card_no,"
          + "ori_issue_date";
  daoTable  = "dbc_card";
  whereStr  = "where card_no in "
          + " (select distinct(card_no) from dba_deduct_txn "
          + "  where deduct_proc_date >= ? "
          + "  and   purchase_date like ? "
          + "  and   acct_code = 'BL' "
          + "  and   deduct_amt > 0) "
  ;

  setString(1 , comm.lastMonth(businessDate, 1)+"01");
  setString(2 , comm.lastMonth(businessDate,1)+"%");

  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and id_p_seqno = ? ";
   setString(3 , idPSeqno);
  }
  int  n = loadTable();
  setLoadData("car0.card_no");

  showLogMessage("I","","Load dbc_card_0 cnt: ["+n+"]");
 }
 // ************************************************************************
 int selectDbcCard1(String cardNo) throws Exception
 {
  extendField = "dbm1.";
  selectSQL = "1 as chg_cnt";
  daoTable  = "dbc_card";
  whereStr  = "WHERE card_no = ? "
          + "and   change_reason = '' ";

  setString(1 , cardNo);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
 // ************************************************************************
 void loadDbcCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "id_p_seqno,"
          + "card_no,"
          + "change_reason,"
          + "activate_date,"
          + "issue_date";
  daoTable  = "dbc_card a";
  whereStr  = "where id_p_seqno in "
          + " (select distinct(id_p_seqno) from dba_deduct_txn "
          + "  where deduct_proc_date >= ? "
          + "  and   purchase_date like ? "
          + "  and   acct_code = 'BL' "
          + "  and   deduct_amt > 0) "
          + "and   issue_date <= ?  "
  ;

  setString(1 , comm.lastMonth(businessDate, 1)+"01");
  setString(2 , comm.lastMonth(businessDate,1)+"%");
  setString(3 , businessDate);

  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and id_p_seqno = ? ";
   setString(4 , idPSeqno);
  }
  whereStr  = whereStr
          + "  order by id_p_seqno,issue_date ";

  int  n = loadTable();
  setLoadData("card.id_p_seqno");

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
          + "and   b.data_type  = '6' "
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
 int selectDbmSysparm() throws Exception
 {
  extendField = "dbmp.";
  selectSQL = "effect_months";
  daoTable  = "dbm_sysparm";
  whereStr  = "WHERE parm_type = '01' "
          + "and   apr_date !='' ";

  int recCnt = selectTable();

  if ( notFound.equals("Y") )
  {
   showLogMessage("I","","select dbm_sysparm error!" );
   exitProgram(0);
  }

  return(0);
 }
 // ************************************************************************
 int checkInOutCond(int inti) throws Exception
 {
  if (getValue("parm.in_bl_cond",inti).equals("Y"))
   if (checkInBlCond(inti)==0) return(0);

  if (getValue("parm.out_bl_cond",inti).equals("Y"))
   if (checkOutBlCond(inti)==0) return(0);

  if (getValue("parm.out_ca_cond",inti).equals("Y"))
   if (checkOutCaCond(inti)==0) return(0);

  return(1);
 }
 // ************************************************************************
 int checkInBlCond(int inti) throws Exception
 {
  if (!getValue("acct_code").equals("BL")) return(1);

  if (!getValue("source_curr").equals("901")) return(1);
  if (!getValue("mcht_country").equals("TW")) return(1);
/*
  if ((!getValue("mcht_country").equals("TW"))&&
      (!getValue("mcht_country").equals("TWN"))) return(1);

  if ((!getValue("source_curr").equals("901"))&&
      (!getValue("source_curr").equals("TWD"))) return(1);
*/
  return(0);
 }
 // ************************************************************************
 int checkOutBlCond(int inti) throws Exception
 {
  if (!getValue("acct_code").equals("BL")) return(1);

  if ((!getValue("source_curr").equals("901"))||
          (!getValue("mcht_country").equals("TW"))) return(0);
/*
  if ((!getValue("mcht_country").equals("TW"))&&
      (!getValue("mcht_country").equals("TWN"))) return(0);

  if ((!getValue("source_curr").equals("901"))&&
      (!getValue("source_curr").equals("TWD"))) return(0);
*/
  return(1);
 }
 // ************************************************************************
 int checkOutCaCond(int inti) throws Exception
 {
  if (!getValue("acct_code").equals("CA")) return(1);

  if ((getValue("source_curr").equals("901"))||
          (getValue("source_curr").equals("TWD")))
  {
   if ((getValue("mcht_country").equals("TW"))||
           (getValue("mcht_country").equals("TWN"))) return(1);
  }

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
