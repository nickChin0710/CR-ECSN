/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/09/24  V1.00.00  Allen Ho   new                                        *
* 109-12-03  V1.00.01  tanwei     updated for project coding standard        *  
* 112-10-20  V1.00.02  Holmes     only executed on the 2nd of every month    * 
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktA340 extends AccessDAO
{
 private  String progname = "紅利-每月紅利各交易類別統計處理程式 109/12/03 V1.00.01";
 CommFunction comm = new CommFunction();

 String businessDate  = "";

 int cnt1 =0;
 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA340 proc = new MktA340();
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
   showLogMessage("I","",javaProgram+" "+progname);

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
   
   if ( !connectDataBase() ) exitProgram(1);

   selectPtrBusinday();
   if (!businessDate.substring(6,8).equals("02"))
   {
    showLogMessage("I","","本程式只在每月2日換日後執行,本日為"+ businessDate +"日..");
    showLogMessage("I","","=========================================");
    return(0);
   }   

   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除統計資料...");
   deleteMktBonusStat1();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","讀取 交易來源明細代碼 資料");
   loadMktBonusSrcdtl();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 紅利統計 資料");
   selectMktBonusDtl();
   showLogMessage("I","","=========================================");

   finalProcess();
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
  showLogMessage("I","","本日營業日 : ["+ businessDate+"]");
 }
// ************************************************************************ 
 void  selectMktBonusDtl() throws Exception
 {
  selectSQL = "acct_type,"
            + "tran_code,"
            + "upper(tran_pgm) as tran_pgm,"
            + "substr(acct_date,1,6) as stat_month,"
            + "decode(sign(beg_tran_bp),-1,'2','1') as sign_flag,"
            + "sum(beg_tran_bp) as month_pnt,"
            + "sum(end_tran_bp) as end_tran_bp,"
            + "sum(decode(end_tran_bp,0,0,1)) as end_tran_cnt," 
            + "count(*) as month_cnt"; 
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where ((beg_tran_bp != 0 "
            + "and     substr(acct_date,1,6)  between  ? and ? ) "
            + " or    (end_tran_bp != 0   "
            + "  and   substr(acct_date,1,6)  <= ?)) "
//          + "and   substr(acct_date,1,6)  = '201912' "
//          + "and   acct_type              = '01' "
            + "group by acct_type,tran_code,upper(tran_pgm),substr(acct_date,1,6),decode(sign(beg_tran_bp),-1,'2','1') "
            + "order by substr(acct_date,1,6) "
            ;
  setString(1 , comm.nextMonth(businessDate,-13));
  setString(2 , comm.nextMonth(businessDate,-1));
  setString(3 , comm.nextMonth(businessDate,-13));

  showLogMessage("I","","統計月份 ["+ comm.nextMonth(businessDate,-1) +"]");
  showLogMessage("I","","上月月份 ["+ comm.nextMonth(businessDate,-2)  +"]");
  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    setValue("srcd.tran_pgm"  ,  getValue("tran_pgm"));
    setValue("srcd.sign_flag" ,  getValue("sign_flag"));
    setValue("srcd.tran_code" ,  getValue("tran_code"));
    if (procMktBonusSrcdtl()!=0)
       {
        setValue("srcd.tran_pgm"  ,  getValue("tran_pgm"));
        setValue("srcd.sign_flag" ,  "");
        setValue("srcd.tran_code" ,  getValue("tran_code"));
        if (procMktBonusSrcdtl()!=0)
           {
            setValue("srcd.tran_pgm"  ,  getValue("tran_pgm"));
            setValue("srcd.sign_flag" ,  getValue("sign_flag"));
            setValue("srcd.tran_code" ,  "");
            if (procMktBonusSrcdtl()!=0)
               {
                setValue("srcd.tran_pgm"  ,  getValue("tran_pgm"));
                setValue("srcd.sign_flag" ,  "");
                setValue("srcd.tran_code" ,  "");
//              if (procMktBonusSrcdtl()!=0) continue;
                procMktBonusSrcdtl();
               }
          }
       }

    if (getValue("stat_month").equals(comm.lastMonth(businessDate)))
        if (insertMktBonusStat1(1)!=0) updateMktBonusStat1(1);

    if (getValue("stat_month").substring(0,4).equals(comm.lastMonth(businessDate).substring(0,4)))
        if (updateMktBonusStat1(2)!=0) insertMktBonusStat1(2);
  
    if (getValue("stat_month").equals(comm.nextMonth(businessDate,-2)))
        if (updateMktBonusStat1(3)!=0) insertMktBonusStat1(3);

    if (getValueInt("end_tran_bp")!=0)
        if (updateMktBonusStat1(4)!=0) insertMktBonusStat1(4);
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int updateMktBonusStat1(int inti) throws Exception
 {
  if (inti==1)
     updateSQL = "month_cnt      = month_cnt + ?,"
               + "month_pnt      = month_pnt + ?";

  if (inti==2)
     updateSQL = "year_cnt      = year_cnt + ?,"
               + "year_pnt      = year_pnt + ?";

  if (inti==3)
     updateSQL = "last_month_cnt = last_month_cnt + ?,"
               + "last_month_pnt = last_month_pnt + ?";

  if (inti==4)
     updateSQL = "end_tran_cnt   = end_tran_cnt   + ?,"
               + "end_tran_bp    = end_tran_bp    + ?";

  daoTable  = "mkt_bonus_stat1";
  whereStr  = "WHERE stat_month    = ? "
            + "and   acct_type     = ? "
            + "and   tran_code     = ? "
            + "and   tran_src_code = ? "
            + "and   sign_flag     = ? "
            + "and   stat_type     = ? "
            ;

  if (inti==4)
     {
      setInt(1    , getValueInt("end_tran_cnt"));
      setDouble(2 , getValueDouble("end_tran_bp"));
     }
  else
     {
      setInt(1    , getValueInt("month_cnt"));
      setDouble(2 , getValueDouble("month_pnt"));
     }
  setString(3 , comm.lastMonth(businessDate));
  setString(4 , getValue("acct_type"));
  setString(5 , getValue("tran_code"));
  setString(6 , getValue("tran_src_code"));
  setString(7 , getValue("sign_flag"));
  setString(8 , getValue("stat_type"));

  updateTable();
  if (notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 int insertMktBonusStat1(int inti) throws Exception
 {
  dateTime();
  setValue("stat.month_cnt"       , "0");
  setValue("stat.month_pnt"       , "0");
  setValue("stat.last_month_cnt"  , "0");
  setValue("stat.last_month_pnt"  , "0");
  setValue("stat.year_cnt"        , "0");
  setValue("stat.year_pnt"        , "0");
  setValue("stat.end_tran_cnt"    , "0");
  setValue("stat.end_tran_bp"     , "0");

  if (inti==1)
     {
      setValue("stat.month_cnt"       , getValue("month_cnt"));
      setValue("stat.month_pnt"       , getValue("month_pnt"));
     }

  if (inti==2)
     {
      setValue("stat.year_cnt"        , getValue("month_cnt"));
      setValue("stat.year_pnt"        , getValue("month_pnt"));
     }

  if (inti==3)
     {
      setValue("stat.last_month_cnt"  , getValue("month_cnt"));
      setValue("stat.last_month_pnt"  , getValue("month_pnt"));
     }

  if (inti==4)
     {
      setValue("stat.end_tran_cnt"    , getValue("end_tran_cnt"));
      setValue("stat.end_tran_bp"     , getValue("end_tran_bp"));
     }

  setValue("stat.stat_month"           , comm.lastMonth(businessDate));
  setValue("stat.acct_type"            , getValue("acct_type"));
  setValue("stat.stat_type"            , getValue("stat_type"));
  setValue("stat.tran_code"            , getValue("tran_code"));
  setValue("stat.tran_src_code"        , getValue("tran_src_code"));
  setValue("stat.sign_flag"            , getValue("sign_flag"));

  setValue("stat.mod_time"             , sysDate+sysTime);
  setValue("stat.mod_pgm"              , javaProgram);

  extendField = "stat.";
  daoTable  = "mkt_bonus_stat1";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void deleteMktBonusStat1() throws Exception
 {
  daoTable  = "mkt_bonus_stat1";
  whereStr  = "WHERE stat_month    = ? " 
            + "and   acct_type    != '90' ";

  setString(1 , comm.lastMonth(businessDate));

  int n = deleteTable();

  showLogMessage("I","","刪除["+ n + "] 筆資料");

  return;
 }
// ************************************************************************
 int loadMktBonusSrcdtl() throws Exception
 {
  extendField = "srcd.";
  selectSQL = "upper(tran_pgm) as tran_pgm,"
            + "stat_type,"
            + "sign_flag,"
            + "tran_code,"
            + "tran_src_code";
  daoTable  = "mkt_bonus_srcdtl";
  whereStr  = "where sign_flag     != 'X' "
            + "and   tran_src_code != 'XXX' "
            + "order by upper(tran_pgm),sign_flag,tran_code desc"
            ;

  int  n = loadTable();
  setLoadData("srcd.tran_pgm,srcd.sign_flag,srcd.tran_code");

  showLogMessage("I","","Load mkt_bonus_srcdtl Count: ["+n+"]");

  return(0);
 }
// ************************************************************************
 int procMktBonusSrcdtl() throws Exception
 {
  cnt1 =  getLoadData("srcd.tran_pgm,srcd.sign_flag,srcd.tran_code");
  if (cnt1==0) 
     {
      setValue("stat_type"     , "0");
      setValue("tran_src_code" , "Z");
      return(1);
     }

  setValue("tran_src_code", getValue("srcd.tran_src_code"));
  setValue("stat_type"    , getValue("srcd.stat_type"));

  return(0);
 }
// ************************************************************************


}  // End of class FetchSample
