/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/03/25  V1.00.12  Allen Ho   dbm_m060                                   *
 * 111/11/07  V1.00.13  jiangyigndong  updated for project coding standard    *
 * 112/10/30  V1.00.14  Ryan             增加group by p_seqno處理                                             *
 ******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM060 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-無有效卡達N個月紅利移除處理程式 112/10/30 V1.00.14";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String tranSeqno = "";

 long    totalCnt=0;
 long    updateCnt=0;
 int     updateSum=0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM060 proc = new DbmM060();
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
   { businessDate = args[0]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

/*
   if ((!business_date.substring(6,8).equals("01"))&&
       (!business_date.substring(6,8).equals("20")))
      {
       showLogMessage("I","","本程式為每月01,20日執行");
       return(0);
      }
*/

   selectDbmSysparm();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   if (loadDbcCard()>0)
   {
    showLogMessage("I","","=========================================");
    showLogMessage("I","","移除 無效卡達 "+getValueInt("parm.novalid_card_mm")+" 個月資料");
    selectDbmBonusDtl();
   }
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 移除 ["+updateCnt+"] 筆 , 點數 [" + updateSum +"]");
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

  if (businessDate.length()==0)
   businessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
 // ************************************************************************
 int selectDbmSysparm() throws Exception
 {
  extendField = "parm.";
  selectSQL = "novalid_card_mm";
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
 void selectDbmBonusDtl() throws Exception
 {
  selectSQL = "acct_type,"
          + "id_p_seqno, "
		  + "p_seqno ";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where acct_date < ? "
          + "GROUP by id_p_seqno,p_seqno,acct_type "
          + "Having sum(end_tran_bp) != 0 ";

  setString(1 , comm.nextMonthDate(businessDate,(getValueInt("parm.novalid_card_mm"))*-1));

  openCursor();

  totalCnt=0;
  while( fetchTable() )
  {
   setValue("card.acct_type"  , getValue("acct_type"));
   setValue("card.id_p_seqno" , getValue("id_p_seqno"));
   setValue("card.p_seqno"    , getValue("p_seqno"));
   int cnt1 = getLoadData("card.id_p_seqno,card.p_seqno,card.acct_type");

   if (cnt1==0) continue;

   totalCnt++;

   if (selectDbmBonusDtl1()!=0) continue;

//  showLogMessage("I","","id_p_seqno : ["+ getValue("id_p_seqno") +"]");

  }
  closeCursor();
  return;
 }
 // ************************************************************************
 int selectDbmBonusDtl1() throws Exception
 {
  extendField = "dbdl.";
  selectSQL = "id_p_seqno,"
		  + "p_seqno,"
          + "bonus_type,"
		  + "acct_type,"
          + "sum(end_tran_bp) as end_tran_bp, "
          + "max(bonus_type) as bonus_type,"
//          + "max(acct_type) as acct_type,"
          + "sum(decode(tax_flag,'Y',end_tran_bp,0)) as tax_tran_bp";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "WHERE end_tran_bp != 0 "
          + "and id_p_seqno = ? "
          + "and p_seqno = ? "
          + "and acct_type  = ? "
          + "group by id_p_seqno,p_seqno,acct_type,bonus_type "
          + "having sum(end_tran_bp) != 0  "
  ;

  setString(1, getValue("id_p_seqno"));
  setString(2, getValue("p_seqno"));
  setString(3, getValue("acct_type"));

  int recCnt = selectTable();

  if (recCnt==0) return(1);

  for ( int inti=0; inti<recCnt; inti++ )
  {
   updateSum = updateSum + getValueInt("dbdl.end_tran_bp",inti);
   insertDbmBonusDtl(inti);
   updateDbmBonusDtl(inti);
   updateCnt++;

   if (updateCnt>10000) countCommit();

   if (updateCnt>50000) break;
  }
  return(0);
 }
 // ************************************************************************
 int insertDbmBonusDtl(int inti) throws Exception
 {
  tranSeqno = comr.getSeqno("ECS_DBMSEQ");

  setValue("ddtl.acct_type"            , getValue("dbdl.acct_type",inti));
  setValue("ddtl.id_p_seqno"           , getValue("dbdl.id_p_seqno",inti));
  setValue("ddtl.p_seqno"              , getValue("dbdl.p_seqno",inti));
  setValue("ddtl.active_code"          , "");
  setValue("ddtl.active_name"          , "無有效卡紅利移除");
  setValue("ddtl.mod_desc"             , "無有效卡達 "+ getValueInt("parm.novalid_card_mm") + " 個月");
  setValue("ddtl.bonus_type"           , getValue("dbdl.bonus_type",inti));
  setValue("ddtl.tran_code"            , "6");
  setValue("ddtl.tran_pgm"             , javaProgram);
  setValueInt("ddtl.beg_tran_bp"       , getValueInt("dbdl.end_tran_bp",inti)*-1);
  setValue("ddtl.end_tran_bp"          , "0");
  setValue("ddtl.tax_tran_bp"          , getValue("dbdl.tax_tran_bp",inti));
  setValue("ddtl.tax_flag"             , "N");
  setValue("ddtl.tran_seqno"           , tranSeqno);
  setValue("ddtl.acct_date"            , businessDate);
  setValue("ddtl.acct_month"           , businessDate.substring(0,6));
  setValue("ddtl.tran_date"            , sysDate);
  setValue("ddtl.tran_time"            , sysTime);
  setValue("ddtl.crt_date"             , sysDate);
  setValue("ddtl.crt_user"             , "SYSTEM");
  setValue("ddtl.apr_date"             , businessDate);
  setValue("ddtl.apr_user"             , "SYSTEM");
  setValue("ddtl.apr_flag"             , "Y");
  setValue("ddtl.mod_user"             , "SYSTEM");
  setValue("ddtl.mod_time"             , sysDate+sysTime);
  setValue("ddtl.mod_pgm"              , javaProgram);

  extendField = "ddtl.";
  daoTable  = "dbm_bonus_dtl";

  int n = insertTable();

  if  (dupRecord.equals("Y"))
  {
   showLogMessage("I","","insert dbm_bonus_dtl error" );
   exitProgram(1);
  }

  return(0);
 }
 // ************************************************************************
 int updateDbmBonusDtl(int inti) throws Exception
 {
  updateSQL = "end_tran_bp  = 0,"
          + "link_seqno   = ?,"
          + "link_tran_bp = end_tran_bp,"
          + "effect_flag  = '2', "
          + "remove_date  = ?, "
          + "mod_memo     = ?,"
          + "mod_pgm      = ?, "
          + "mod_time     = sysdate";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "WHERE id_p_seqno  = ? "
		  + "and p_seqno = ? "
		  + "and acct_type = ? "
          + "and bonus_type    = ? "
          + "and end_tran_bp  != 0 ";

  setString(1 , tranSeqno);
  setString(2 , sysDate);
  setString(3 , "移除序號["+ tranSeqno +"]");
  setString(4 , javaProgram);
  setString(5 , getValue("dbdl.id_p_seqno",inti));
  setString(6 , getValue("dbdl.p_seqno",inti));
  setString(7 , getValue("dbdl.acct_type",inti));
  setString(8 , getValue("dbdl.bonus_type",inti));

  int cnt = updateTable();

  return(0);
 }
 // ************************************************************************
 int loadDbcCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "acct_type,"
          + "id_p_seqno,"
		  + "p_seqno,"
          + "max(decode(oppost_date,'','30001231',oppost_date)) as max_date";
  daoTable  = "dbc_card";
  whereStr  = "where issue_date < ? "
          + "group by id_p_seqno,p_seqno,acct_type "
          + "having  max(decode(oppost_date,'','30001231',oppost_date)) between ? and ? "
          + "order by id_p_seqno,acct_type "
  ;

  setString(1 , businessDate);

  String sDate="",eDate="",tmpDate="";
  tmpDate = comm.nextMonthDate(businessDate,(getValueInt("parm.novalid_card_mm"))*-1);
  eDate   = comm.nextNDate(tmpDate , -1);

  if (businessDate.substring(6,8).equals("01"))
   sDate   = comm.nextMonthDate(eDate , -37);
  else
   sDate   = comm.nextNDate(eDate , -7);

  showLogMessage("I","","判斷日期 : ["+ sDate +"]-["+ eDate + "]");
  setString(2 , sDate);
  setString(3 , eDate);

  int  n = loadTable();
  setLoadData("card.id_p_seqno,card.p_seqno,card.acct_type");

  showLogMessage("I","","Load dbc_card cnt: ["+n+"]");
  return(n);
 }
// ************************************************************************

}  // End of class FetchSample
