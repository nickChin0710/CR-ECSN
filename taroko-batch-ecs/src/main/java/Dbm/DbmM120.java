/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/02/24  V1.00.26  Allen Ho   dbm_m120                                   *
 * 111/11/07  V1.00.27  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM120 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-生日加贈點數處理程式 110/02/24 V1.00.26";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String idPSeqno = "";
 String birthdayFlag1 = "N";
 String birthdayFlag2 = "N";
 String minMonth = "N";

 String birthMonth1 = "";
 String birthMonth2 = "";

 long    totalCnt=0;
 int     parmCnt =0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM120 proc = new DbmM120();
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

   if ( args.length >= 2 )
   { idPSeqno = args[1]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (!businessDate.substring(6,8).equals("05"))
   {
    showLogMessage("I","","=========================================");
    showLogMessage("I","","本程式只在每月5日執行,本日為"+ businessDate +"日..");
    showLogMessage("I","","=========================================");
    return(0);
   }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectDbmBpbir();
   if (parmCnt==0)
   {
    showLogMessage("I","","無參數資料需執行");
    return(0);
   }
   selectDbmSysparm();
   showLogMessage("I","","=========================================");

   if (idPSeqno.length()>0)
   {
    showLogMessage("I","","DEBUG MODE");
    deleteDbmBpbirIddtl();
   }
   else
   {
    commitDataBase();
    truncateTable("DBM_BPBIR_IDDTL");
   }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數暫存資料");
// load_mkt_rcv_bin();
   loadDbmBnData();
   loadMktMchtgpData();
   loadDbcIdno();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","讀取請款資料(不含含生日當月)");
   selectDbbBill();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入扣款資料");
   selectDbaDeductTxn();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","讀符合生日資料");
   selectDbmBpbirIddtl();
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
          + "decode(mcht_category,'','5542',mcht_category) as mcht_category,"
          + "pos_entry_mode,"
          + "decode(group_code,'','0000',group_code) as group_code,"
          + "purchase_date,"
          + "dest_amt,"
          + "card_no,"
          + "mcht_no,"
          + "acq_member_id";
  daoTable  = "dbb_bill";
  whereStr  = "where purchase_date between ? and ? "
          + "and   acct_code  = 'BL' "
          + "and   sign_flag  = '+' "
          + "and   post_date <= ? "
          + "";

  setString(1 , minMonth +"01");
  setString(2 , comm.lastdateOfmonth(comm.lastMonth(businessDate,1)));
  setString(3 , businessDate);
  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and id_p_seqno = ? ";
   setString(4 , idPSeqno);
  }

  showLogMessage("I","","消費日期 : ["
          + minMonth +"01"
          + "]-["
          + comm.lastdateOfmonth(comm.lastMonth(businessDate,1))
          + "]");

  if (birthdayFlag1.equals("Y"))
   showLogMessage("I","","生日月份1 : [" + businessDate.substring(4,6) + "]");

  if (birthdayFlag2.equals("Y"))
   showLogMessage("I","","生日月份2 : [" + comm.lastMonth(businessDate,1).substring(4,6) + "]");



  openCursor();
  int[] purchCnt = new int[parmCnt];
  int[] feedCnt  = new int[parmCnt];

  int[][] parmArr = new int [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
  {
   feedCnt[inti]=0;
   purchCnt[inti]=0;
   for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;
  }

  totalCnt = 0;
  String acqId ="";
  int cnt1 = 0;
  birthMonth1 = businessDate.substring(4,6);
  birthMonth2 = comm.lastMonth(businessDate,1).substring(4,6);
  while( fetchTable() )
  {
   setValue("idno.id_p_seqno" , getValue("id_p_seqno"));
   cnt1 = getLoadData("idno.id_p_seqno");
   if (cnt1<=0) continue;

   totalCnt++;

   acqId = "";
   if (getValue("acq_member_id").length()!=0)
    acqId = comm.fillZero(getValue("acq_member_id"),8);

   for (int inti=0;inti<parmCnt;inti++)
   {
    if (getValue("parm.birthday_flag",inti).equals("Y"))
    {
     if (!getValue("idno.birth_month").equals(birthMonth2)) continue;
     if (getValue("purchase_date").substring(0,6).compareTo(
             comm.lastMonth(businessDate, getValueInt("parm.bir_month_bef",inti)+1))<0) continue;
    }
    else
    {
     if (!getValue("idno.birth_month").equals(birthMonth1)) continue;
     if (getValue("purchase_date").substring(0,6).compareTo(
             comm.lastMonth(businessDate, getValueInt("parm.bir_month_bef",inti)))<0) continue;
    }
    parmArr[inti][0]++;

    setValue("data_key" , getValue("parm.active_code",inti));

    parmArr[inti][1]++;

    if (selectDbmBnData(getValue("mcht_no"),acqId,
            getValue("parm.merchant_sel",inti),"1",3)!=0)
     continue;

    parmArr[inti][2]++;

    if (selectDbmBnData(getValue("group_code"),
            getValue("parm.group_code_sel",inti),"2",3)!=0)
     continue;

    parmArr[inti][3]++;

    if (selectDbmBnData(getValue("acct_type"),
            getValue("parm.acct_type_sel",inti),"3",3)!=0)
     continue;

    parmArr[inti][4]++;

    if (selectDbmBnData(getValue("pos_entry_mode"),
            getValue("parm.pos_entry_sel",inti),"4",3)!=0)
     continue;

    parmArr[inti][5]++;

    if (selectDbmBnData(getValue("mcht_category"),
            getValue("parm.mcc_code_sel",inti),"5",3)!=0)
     continue;

    parmArr[inti][6]++;
    if (selectMktMchtgpData(getValue("mcht_no"),acqId,
            getValue("parm.mcht_group_sel",inti),"6")!=0)
     continue;

    parmArr[inti][7]++;
/*
       if (getValue("mcht_category").equals("5542"))
          {
           setValue("rcvb.ica_no" , acq_id);
           cnt1 = getLoadData("rcvb.ica_no");
           if (cnt1<=0) continue;
          }
*/

    parmArr[inti][8]++;

    insertDbmBpbirIddtl(inti);
   }

   processDisplay(50000); // every 10000 display message
  }
  closeCursor();

  showLogMessage("I","","=========================================");
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );

  if (idPSeqno.length()>0)
   for (int inti=0;inti<parmCnt;inti++)
   {
    for (int intk=0;intk<20;intk++)
    {
     if (parmArr[inti][intk]==0) continue;
     showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
    }

   }
  showLogMessage("I","","=========================================");

 }
 // ************************************************************************
 void selectDbaDeductTxn() throws Exception
 {
  selectSQL = "a.reference_no,"
          + "a.deduct_amt,"
          + "b.rowid as rowid";
  daoTable  = "dba_deduct_txn a,dbm_bpbir_iddtl b";
  whereStr  = "where a.reference_no = b.reference_no "
          + "and   a.deduct_proc_date <= ? "
  ;

  setString(1 , businessDate);
  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and b.id_p_seqno = ? ";
   setString(2 , idPSeqno);
  }

  openCursor();

  while( fetchTable() )
  {
   updateDbmBpbirIddtl();
   processDisplay(50000); // every 10000 display message
  }
  closeCursor();
 }
 // ************************************************************************
 void selectDbmBpbirIddtl() throws Exception
 {
  selectSQL = "acct_type,"
          + "id_p_seqno,"
          + "active_code,"
          + "sum(deduct_amt) as deduct_amt";
  daoTable  = "dbm_bpbir_iddtl";
  whereStr  = ""
  ;

  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " where  id_p_seqno = ? ";
   setString(1 , idPSeqno);
  }

  whereStr  = whereStr
          + "group by id_p_seqno,active_code,acct_type ";

  openCursor();

  while( fetchTable() )
  {
   for (int inti=0;inti<parmCnt;inti++)
   {
    if (!getValue("active_code").equals(getValue("parm.active_code",inti))) continue;

    if (getValueInt("deduct_amt") < getValueInt("parm.total_amt",inti)) continue;
    if (getValueInt("deduct_amt") < getValueInt("parm.bp_amt",inti)) continue;

    int deductBpInt = (int)Math.ceil(
            (getValueInt("deduct_amt")
                    / getValueInt("parm.bp_amt",inti))
                    * getValueInt("parm.bp_pnt",inti)
    );

    int deductBp = (int)Math.round(
            (deductBpInt
                    * getValueInt("parm.add_times",inti))
                    + getValueInt("parm.add_point",inti));

    if (deductBp==0) continue;

    setValue("bdtl.active_code" , getValue("parm.active_code",inti));
    setValue("bdtl.tax_flag"    , getValue("parm.tax_flag",inti));
    setValue("bdtl.active_name" , getValue("parm.active_name",inti));

    setValue("bdtl.mod_desc"  ,  businessDate.substring(4,6) + "月生日");
    setValue("bdtl.mod_memo"  , "生日加贈點數");
    setValue("bdtl.tran_code" , "2");

    setValueInt("bdtl.beg_tran_bp"  , deductBp);
    setValueInt("bdtl.end_tran_bp"  , deductBp);
    insertDbmBonusDtl();
   }

   processDisplay(50000); // every 10000 display message
  }
  closeCursor();
 }
 // ************************************************************************
 void selectDbmBpbir() throws Exception
 {
  extendField = "parm.";
  daoTable  = "dbm_bpbir";
  whereStr  = "WHERE apr_flag = 'Y' "
          + "and    ? between active_s_date "
          + "         and     to_char(add_months(to_date(active_e_date,'yyyymmdd'),1),'yyyymmdd') "
  ;

  setString(1, businessDate);

  parmCnt = selectTable();

  minMonth = "999999";
  String cmpMonth= "";
  for (int inti=0;inti<parmCnt;inti++)
  {
   if (getValue("parm.birthday_flag",inti).equals("Y"))
   {
    birthdayFlag2 ="Y";
    cmpMonth = comm.lastMonth(businessDate, getValueInt("parm.bir_month_bef",inti)+1);
   }
   else
   {
    birthdayFlag1 ="Y";
    cmpMonth = comm.lastMonth(businessDate, getValueInt("parm.bir_month_bef",inti));
   }
   if (cmpMonth.compareTo(minMonth)<0) minMonth = cmpMonth;

   showLogMessage("I","","活動代號  : ["+ getValue("parm.active_code",inti) +"][" + getValue("parm.active_name",inti)+"]");
   showLogMessage("I","","  活動期間: ["+ getValue("parm.active_s_date",inti) +"-" + getValue("parm.active_e_date",inti)+"]");
  }


  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return;
 }
 // ************************************************************************
 int truncateTable(String tableName) throws Exception
 {
  showLogMessage("I","","   刪除 " + tableName);
  String trunSQL = "TRUNCATE TABLE "+ tableName + " "
          + "IGNORE DELETE TRIGGERS "
          + "DROP STORAGE "
          + "IMMEDIATE "
          ;
  executeSqlCommand(trunSQL);

  return(0);
 }
 // ************************************************************************
 int insertDbmBonusDtl() throws Exception
 {
  setValue("bdtl.tran_seqno"     , comr.getSeqno("ECS_DBMSEQ"));

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
  setValue("bdtl.bonus_type"           , "BONU");
  setValue("bdtl.acct_type"            , getValue("acct_type"));
  setValue("bdtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("bdtl.tran_pgm"             , javaProgram);
  setValue("bdtl.mod_time"             , sysDate+sysTime);
  setValue("bdtl.mod_pgm"              , javaProgram);

  extendField = "bdtl.";
  daoTable  = "dbm_bonus_dtl";

  insertTable();

  return(0);
 }
 // ************************************************************************
 int insertDbmBpbirIddtl(int inti) throws Exception
 {
  extendField = "idtl.";
  setValue("idtl.active_code"          , getValue("parm.active_code",inti));
  setValue("idtl.acct_type"            , getValue("acct_type"));
  setValue("idtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("idtl.reference_no"         , getValue("reference_no"));
  setValue("idtl.dest_amt"             , getValue("dest_amt"));
  daoTable  = "dbm_bpbir_iddtl";

  insertTable();

  return(0);
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
 void loadDbmBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
          + "data_type,"
          + "data_code,"
          + "data_code2";
  daoTable  = "dbm_bn_data b";
  whereStr  = "WHERE TABLE_NAME = 'DBM_BPBIR' "
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
  int ok_flag=0;
  for (int inti=0;inti<cnt1;inti++)
  {
   if ((getValue("mcht.data_code2",inti).length()==0)||
           ((getValue("mcht.data_code2",inti).length()!=0)&&
                   (getValue("mcht.data_code2",inti).equals(col2))))
   {
    ok_flag=1;
    break;
   }
  }

  if (sel.equals("1"))
  {
   if (ok_flag==0) return(1);
   return(0);
  }
  else
  {
   if (ok_flag==0) return(0);
   return(1);
  }
 }
 // ************************************************************************
 void updateDbmBpbirIddtl() throws Exception
 {
  dateTime();
  updateSQL = "deduct_amt   = deduct_amt + ? ";
  daoTable  = "dbm_bpbir_iddtl";
  whereStr  = "WHERE rowid    = ? ";

  setInt(1 , getValueInt("deduct_amt"));
  setRowId(2  , getValue("rowid"));

  updateTable();
  return;
 }
 // ************************************************************************
 int deleteDbmBpbirIddtl() throws Exception
 {
  daoTable  = "dbm_bpbir_iddtl";
  whereStr  = "where  id_p_seqno = ? ";

  setString(1 , idPSeqno);

  int n = deleteTable();

  showLogMessage("I","","Delete dbm_bpbir_iddtl  [" + n +"] 筆");

  return(0);
 }
 // ************************************************************************
 void loadDbcIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_p_seqno,"
          + "substr(birthday,5,2) as birth_month";
  daoTable  = "dbc_idno";
  whereStr  = "WHERE substr(birthday,5,2) between ? and ? "
  ;
  if (birthdayFlag1.equals("Y"))
   setString(1 , businessDate.substring(4,6));
  else
   setString(1 , comm.lastMonth(businessDate,1).substring(4,6));

  if (birthdayFlag2.equals("Y"))
   setString(2 , comm.lastMonth(businessDate,1).substring(4,6));
  else
   setString(2 , businessDate.substring(4,6));

  if (idPSeqno.length()>0)
  {
   whereStr  = whereStr
           + " and id_p_seqno = ? ";
   setString(3 , idPSeqno);
  }

  int  n = loadTable();

  setLoadData("idno.id_p_seqno");

  showLogMessage("I","","Load dbc_idno Count: ["+n+"]");
 }
 // ************************************************************************
 void loadMktRcvBin() throws Exception
 {
  extendField = "rcvb.";
  selectSQL = "lpad(ica_no,8,'0') as ica_no";
  daoTable  = "mkt_rcv_bin";
  whereStr  = "where  bank_no != '300' "
          + "and lpad(ica_no,8,'0') = '00493817' "
          + "order by lpad(ica_no,8,'0') "
  ;

  int  n = loadTable();

  setLoadData("rcvb.ica_no");

  showLogMessage("I","","Load mkt_rcv_bin Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample
