/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/06/09  V1.00.16  Allen Ho   New                                        *
* 111/04/15  V1.01.01  Allen Ho   Mantis 9325                                *
* 111/10/13  V1.00.02  Yang Bo    sync code from mega   
* 111/11/01  V1.00.03  Machao    字段名稱調整                     *
* 111/11/03  V1.00.04  Zuwei     table名稱調整                     *
* 112/07/11  V1.00.05  Zuwei/Grace ins mkt_bonus_dtl.active_name, x(50) 長度不足  *
* 112/08/17  V1.00.06  Grace      原insertCycFundDtl(1,inti) 改由holmes 統計產生傳票資料, 故此版remark	*
*************************************************************************************************/
package Mkt;

import com.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktC440 extends AccessDAO
{
 private final String progname = "首刷禮-回饋處理程式 111/11/03  V1.00.06";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommDBonus comd = null;

 String businessDate = "";
 String activeCode ="";
 String pSeqno ="";
 String tranSeqno = "";

 int  parmCnt  = 0;

 int     totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC440 proc = new MktC440();
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
   showLogMessage("I","",javaProgram+" "+ progname);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       showLogMessage("I","","PARM 3 : [p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   activeCode = "";
   if ( args.length >= 2 )
       activeCode = args[1];

   if ( args.length == 3 )
       pSeqno = args[2];

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());
   comd = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   selectMktFstpParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","今日["+ businessDate +"]無活動回饋");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
//   loadMktGiftAddrlist();  暫remark, 待日後應用調整
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理回饋資料 (selectMktFstpCarddtl1())");
   selectMktFstpCarddtl1();  // for execute_date
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理無法回饋資料");
   selectMktFstpCarddtl2();  // for last_execute_date
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");

   if (pSeqno.length()==0) finalProcess();
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
 void selectMktFstpCarddtl1() throws Exception
 {
  selectSQL  = "";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "where proc_flag        = 'N' "   
            + "and   execute_date     = ?  "
            + "and   linebc_flag     in ('X','Y') "
            + "and   selfdeduct_flag in ('X','Y') "
            + "and   banklite_flag   in ('X','Y') "
            + "and   purchase_flag   in ('X','Y') "
            + "and   record_flag     in ('X','Y') "
            + "and   anulfee_flag    in ('X','Y') "
            ;

  setString(1 , businessDate);
  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? ";
      setString(2 , pSeqno);
     }

  openCursor();

  int cnt1=0;
  int matchFlag  = 0;
  int inti =0;
  int okFlag=0;
  totalCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    okFlag=0;
    for (int intm=0;intm<parmCnt;intm++)
      {
       if (getValue("active_code").equals(getValue("parm.active_code",intm)))
          {
           inti = intm;
           okFlag=1;
           break;
          }
      }
    if (okFlag==0) continue;

    tranSeqno = "";
    // **********************************************
    if (getValue("active_type").equals("1"))
       {
        insertMktBonusDtl(inti);
       }
    else if (getValue("active_type").equals("2"))
       {
        selectVmktFundName();
        insertMktCashbackDtl(inti);
        //insertCycFundDtl(1,inti);	//20230817, grace, 改由holmes 統計產生傳票資料
       }
    else if (getValue("active_type").equals("3"))
       {
        if (getValue("group_type").equals("1"))
            {
             selectCrdIdno();
             setValue("prog.data_type" , "1");
             setValue("prog.data_id"   , getValue("idno.id_no"));
             setValue("prog.id_no"     , getValue("idno.id_no"));
             setValue("prog.card_no"   , "");
            }
         else
            {
             setValue("prog.data_type" , "3");
             setValue("prog.data_id"   , getValue("card_no"));
             setValue("prog.id_no"     , "");
             setValue("prog.card_no"   , getValue("card_no"));
            }
        if (selectIbnProgDtl()==0)
            updateIbnProgDtl();
        else insertIbnProgDtl();
        insertIbnProgTxn();
       }
    else if (getValue("active_type").equals("4"))
       {
        setValue("list.id_p_seqno" , getValue("id_p_seqno"));
        cnt1 = getLoadData("liast.id_p_seqno");
        if (cnt1>0) continue;

        selectCrdIdno();
        selectActAcno();
        selectMktSpecGift();
//        insertMktGiftAddrlist();
       }

    updateMktFstpCarddtl(0);
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void selectMktFstpCarddtl2() throws Exception
 {
  selectSQL  = "";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "where proc_flag    = 'N' " 
            + "and   error_code   = '00' "
            + "and   last_execute_date <= ?  "
            ;

  setString(1 , businessDate);
  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? ";
      setString(2 , pSeqno);
     }

  openCursor();

  int cnt1=0;
  int matchFlag  = 0;
  int inti =0;
  int okFlag=0;
  totalCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    if (!getValue("error_code").equals("00"))
       {
        updateMktFstpCarddtl(1);
        continue;
       }

    okFlag=0;
    for (int intm=0;intm<parmCnt;intm++)
       if (getValue("active_code").equals(getValue("parm.active_code",intm)))
          {
           inti = intm;
           okFlag=1;
           break;
          }
    if (okFlag==0) continue;
      
    tranSeqno = "";
    if (getValue("record_flag").equals("N"))
       {
        if (pSeqno.length()!=0)
             showLogMessage("I","","Mantis 9325-000 未登錄");

        setValue("error_code" , "B5");
        setValue("error_desc" , "未登錄");
        updateMktFstpCarddtl(1);
        continue;
       }
    else if (getValue("linebc_flag").equals("N"))
       {
        setValue("error_code" , "B2");
        setValue("error_desc" , "LineBC 條件不符");
        updateMktFstpCarddtl(1);
        continue;
       }
    else if (getValue("selfdeduct_flag").equals("N"))
       {
        setValue("error_code" , "B3");
        setValue("error_desc" , " 自動扣款戶條件不符");
        updateMktFstpCarddtl(1);
        continue;
       }
    // ***************** BANK Line   ****************
    else if (getValue("banklite_flag").equals("N"))
       {
        setValue("error_code" , "B4");
        setValue("error_desc" , "BANK Lite 條件不符");
        updateMktFstpCarddtl(1);
        continue;
       }
    else if (getValue("purchase_flag").equals("N"))
       {
        setValue("error_code" , "B1");
        setValue("error_desc" , "消費條件未達到");
        updateMktFstpCarddtl(1);
        continue;
       }
    // **********************************************
    else if (getValue("anulfee_flag").equals("N"))
       {
        if (pSeqno.length()!=0)
            {
             showLogMessage("I","","Mantis 9325-001 issue_date   ["+ getValue("issue_date") +"]");
             showLogMessage("I","","Mantis 9325-002 anulfee_days ["+ getValueInt("parm.anulfee_days",inti) +"]");
             showLogMessage("I","","Mantis 9325-003 lastchk_date ["+ comm.nextNDate(getValue("issue_date"),getValueInt("parm.anulfee_days",inti)) +"]");
            }
        if (businessDate.compareTo(
            comm.nextNDate(getValue("issue_date"),getValueInt("parm.anulfee_days",inti)))<=0)
           {
            if (pSeqno.length()!=0)
               showLogMessage("I","","Mantis 9325-004 年費檢核日未到");
            continue;
           }
        if (pSeqno.length()!=0)
           showLogMessage("I","","Mantis 9325-005 年費檢核日已過");
        setValue("error_code" , "B6");
        setValue("error_desc" , "年費未於規定時間前繳清");
        updateMktFstpCarddtl(1);
        continue;
       }
    // **********************************************
    setValue("error_desc" , "");
    updateMktFstpCarddtl(0);
   } 
  closeCursor();
 }
// ************************************************************************
 int selectMktFstpParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_fstp_parm";
  whereStr  = "WHERE apr_flag        = 'Y' "
            + "AND   apr_date       != ''  "
            + "AND   (stop_flag     != 'Y'  "
            + " or    (stop_flag     = 'Y'  "
            + "  and  stop_date      > ? )) "
            + "and   issue_date_s <= ? "
            + "and   ((anulfee_cond = 'N' "
            + "  and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "              (purchase_days+n1_days+1) days,'yyyymmdd') >= ?)  "
            + "or     (anulfee_cond = 'Y' "
            + "  and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "               (anulfee_days+1) days,'yyyymmdd') >= ?))  "
            ;
  if (activeCode.length()>0) 
     whereStr  = whereStr 
               + "and active_code = ? ";  

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);
  setString(4 , businessDate);

  if (activeCode.length()>0) 
     setString(5 , activeCode);

  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 int updateMktFstpCarddtl(int matchFlag) throws Exception
 {
  dateTime();

  setValue("feedback_date"   , "");
  if (matchFlag==0)
     setValue("feedback_date"   , businessDate);
      
  updateSQL = ""
            + "tran_seqno         = ?,"
            + "feedback_date      = ?, "
            + "error_code         = ?, "
            + "error_desc         = ?, "
            + "proc_flag          = 'Y', "
            + "mod_time           = sysdate,"
            + "mod_pgm            = ? ";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "WHERE  card_no     = ? "
            + "AND    active_code = ? ";

  setString(1 , tranSeqno);
  setString(2 , getValue("feedback_date"));
  setString(3 , getValue("error_code"));
  setString(4 , getValue("error_desc"));
  setString(5 , javaProgram);
  setString(6 , getValue("card_no"));
  setString(7 , getValue("active_code"));
//  setString(2 , getValue("feedback_date"));
//  setString(3 , getValue("error_code"));
//  setString(5 , javaProgram);
//  setString(6 , getValue("card_no"));
//  setString(7 , getValue("active_code"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int insertMktCashbackDtl(int inti) throws Exception
 {
  dateTime();
  extendField = "fund.";
  tranSeqno = comr.getSeqno("mkt_modseq");

  setValue("fund.tran_date"            , sysDate);
  setValue("fund.tran_time"            , sysTime);
  setValue("fund.fund_code"            , getValue("fund_code"));
  setValue("fund.fund_name"            , getValue("loan.fund_name"));
  setValue("fund.p_seqno"              , getValue("p_seqno"));
  setValue("fund.acct_type"            , getValue("acct_type"));
  setValue("fund.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("fund.mod_memo"             , "");
  setValue("fund.tran_code"            , "1");
  setValue("fund.mod_desc"             , "首刷活動代號:"+getValue("parm.active_code",inti));
  setValue("fund.tran_pgm"             , javaProgram);
  setValue("fund.beg_tran_amt"         , getValue("beg_tran_amt"));
  setValue("fund.end_tran_amt"         , getValue("beg_tran_amt"));
  setValue("fund.res_tran_amt"         , "0");
  setValue("fund.res_total_cnt"        , "0");
  setValue("fund.res_tran_cnt"         , "0");
  setValue("fund.res_s_month"          , "");
  setValue("fund.res_upd_date"         , "");
  setValue("fund.effect_e_date"        , "");
  if (getValueInt("parm.effect_months",inti)>0)
     setValue("fund.effect_e_date"     , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));

  setValue("fund.tran_seqno"           , tranSeqno);
  setValue("fund.proc_month"           , businessDate.substring(0,6));
  setValue("fund.acct_month"           , businessDate.substring(0,6));
  setValue("fund.acct_date"            , businessDate);
  setValue("fund.mod_memo"             , "");
  setValue("fund.mod_reason"           , "");
  setValue("fund.case_list_flag"       , "N");
  setValue("fund.crt_user"             , javaProgram);
  setValue("fund.crt_date"             , sysDate);
  setValue("fund.apr_date"             , sysDate);
  setValue("fund.apr_user"             , javaProgram);
  setValue("fund.apr_flag"             , "Y");
  setValue("fund.mod_user"             , javaProgram);
  setValue("fund.mod_time"             , sysDate+sysTime);
  setValue("fund.mod_pgm"              , javaProgram);
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertCycFundDtl(int numType, int inti) throws Exception
 {
  dateTime();
  extendField = "fdtl.";

  setValue("fdtl.business_date"        , businessDate);
  setValue("fdtl.create_date"          , sysDate);
  setValue("fdtl.create_time"          , sysTime);
  setValue("fdtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("fdtl.p_seqno"              , getValue("p_seqno"));
  setValue("fdtl.acct_type"            , getValue("acct_type"));
  setValue("fdtl.card_no"              , "");
  setValue("fdtl.fund_code"            , getValue("fund_code"));
  setValue("fdtl.vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
  if (numType==1)
     {
      setValue("fdtl.tran_code"            , "2");
      setValue("fdtl.cd_kind"              , "A-36");   /* 基金產生 */
     }
  else
     {
      setValue("fdtl.tran_code"            , "3");
      setValue("fdtl.cd_kind"              , "A393");
     }
  setValue("fdtl.memo1_type"           , "1");   /* fund_code 必須有值 */
  setValueInt("fdtl.fund_amt"          , Math.abs(getValueInt("beg_tran_amt")));
  setValue("fdtl.other_amt"            , "0");
  setValue("fdtl.proc_flag"            , "N");
  setValue("fdtl.proc_date"            , "");
  setValue("fdtl.execute_date"         , businessDate);
  setValueInt("fdtl.fund_cnt"          , 1);
  setValue("fdtl.mod_user"             , javaProgram); 
  setValue("fdtl.mod_time"             , sysDate+sysTime);
  setValue("fdtl.mod_pgm"              , javaProgram);

  daoTable  = "cyc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertMktBonusDtl(int inti) throws Exception
 {
  dateTime();
  extendField = "bonu.";
  tranSeqno = comr.getSeqno("MKT_MODSEQ");

  setValue("bonu.tran_code"            , "1");
  setValue("bonu.beg_tran_bp"          , getValue("beg_tran_bp"));
  setValue("bonu.end_tran_bp"          , getValue("beg_tran_bp"));
  setValue("bonu.res_e_date"           , "");
  setValue("bonu.res_tran_bp"          , "0");
  setValue("bonu.tax_flag"             , getValue("parm.tax_flag",inti));
  setValue("bonu.effect_e_date"        , "");
  if (getValueInt("parm.effect_months",inti)>0)
     setValue("bonu.effect_e_date"     , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  setValue("bonu.mod_desc"             , "");
  setValue("bonu.mod_reason"           , "");
  setValue("bonu.mod_memo"             , "");
  setValue("bonu.tran_date"            , sysDate);
  setValue("bonu.tran_time"            , sysTime);
  setValue("bonu.tran_seqno"           , tranSeqno);
  setValue("bonu.active_code"          , getValue("parm.active_code",inti));
  //setValue("bonu.active_name"          , getValue("parm.active_name",inti));	//parm. 長60
  setValue("bonu.active_name" 		   , StringUtils.left(getValue("parm.active_name",inti), 50));
  setValue("bonu.p_seqno"              , getValue("p_seqno")); 
  setValue("bonu.id_p_seqno"           , getValue("id_p_seqno")); 
  setValue("bonu.acct_type"            , getValue("acct_type")); 
  setValue("bonu.bonus_type"           , getValue("bonus_type"));
  setValue("bonu.acct_date"            , businessDate);
  setValue("bonu.proc_month"           , businessDate.substring(0,6));
  setValue("bonu.tran_pgm"             , javaProgram);
  setValue("bonu.apr_flag"             , "Y");
  setValue("bonu.apr_user"             , javaProgram);
  setValue("bonu.apr_date"             , sysDate);
  setValue("bonu.crt_user"             , javaProgram);
  setValue("bonu.crt_date"             , sysDate);
  setValue("bonu.mod_user"             , javaProgram);
  setValue("bonu.mod_time"             , sysDate+sysTime);
  setValue("bonu.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectIbnProgDtl() throws Exception
 {
  extendField = "pdtl.";
  selectSQL = "data_seqno,"
            + "tot_gift_cnt,"
            + "rem_gift_cnt,"
            + "rowid as rowid";
  daoTable  = "ibn_prog_dtl";;
  whereStr  = "where  data_type   = ? "
            + "and    data_id     = ? "
            + "and    group_type  = ? "
            + "and    prog_code   = ? "
            + "and    prog_s_date = ? "
            ;

  setString(1 , getValue("prog.data_type"));
  setString(2 , getValue("prog.data_id"));
  setString(3 , getValue("group_type"));
  setString(4 , getValue("prog_code"));
  setString(5 , getValue("prog_s_date"));
   
  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int updateIbnProgDtl() throws Exception
 {
  updateSQL = "tot_gift_cnt = tot_gift_cnt + ?, "
            + "rem_gift_cnt = rem_gift_cnt + ?, "
            + "mod_time     = sysdate,"
            + "mod_pgm      = ? ";
  daoTable  = "ibn_prog_dtl";
  whereStr  = "WHERE  rowid = ? ";

  setString(1 , getValue("tran_pt"));
  setString(2 , getValue("tran_pt"));
  setString(3 , javaProgram);
  setRowId(4  , getValue("pdtl.rowid"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int insertIbnProgDtl() throws Exception
 {
  dateTime();
  extendField = "prog.";
  tranSeqno = comr.getSeqno("COL_MODSEQ");

  setValue("prog.group_type"           , getValue("group_type"));
  setValue("prog.p_seqno"              , getValue("p_seqno"));
  setValue("prog.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("prog.acct_type"            , getValue("acct_type"));
  setValue("prog.prog_code"            , getValue("prog_code"));  
  setValue("prog.prog_s_date"          , getValue("prog_s_date"));  
  setValue("prog.prog_e_date"          , getValue("prog_e_date")); 
  setValue("prog.gift_no"              , getValue("gift_no")); 
  setValue("prog.tot_gift_cnt"         , getValue("tran_pt"));
  setValue("prog.rem_gift_cnt"         , getValue("tran_pt"));
  setValue("prog.data_seqno"           , tranSeqno);
  setValue("prog.vd_flag"              , "N");
  setValue("prog.mod_time"             , sysDate+sysTime);
  setValue("prog.mod_pgm"              , javaProgram);

  daoTable  = "ibn_prog_dtl";

  insertTable();
                                 
  return(0);
 }                               
// ************************************************************************
 int insertIbnProgTxn() throws Exception
 {
  dateTime();
  extendField = "ptxn.";
  setValue("ptxn.txn_date"            , sysDate);
  setValue("ptxn.txn_time"            , sysTime);
  setValue("ptxn.data_seqno"           , tranSeqno);
  setValue("ptxn.txn_seqno"            , getValue("active_code"));
  setValue("ptxn.txn_type"             , "F");
  setValue("ptxn.tot_gift_cnt"         , getValue("tran_pt"));
  setValueInt("ptxn.beg_gift_cnt"      , getValueInt("pdtl.tot_gift_cnt"));
  setValueInt("ptxn.aft_gift_cnt"      , getValueInt("pdtl.tot_gift_cnt") 
                                       + getValueInt("tran_pt")); 
  setValue("ptxn.mod_time"             , sysDate+sysTime);
  setValue("ptxn.mod_pgm"              , javaProgram);

  daoTable  = "ibn_prog_txn";

  insertTable();
                                 
  return(0);
 }                               
// ************************************************************************
 int selectVmktFundName() throws Exception
 {
  extendField = "loan.";
  daoTable  = "vmkt_fund_name";
  whereStr  = "WHERE fund_code       = ? "
            ;

  setString(1 , getValue("fund_code"));

  selectTable();

  return(0);
 }
// ************************************************************************
 int insertMktGiftAddrlist() throws Exception
 {
  extendField = "addr.";
  tranSeqno = comr.getSeqno("ECS_DBMSEQ");

  setValue("addr.active_code"          , getValue("active_code"));
  setValue("addr.vd_flag"              , "N");
  setValue("addr.acct_type"            , getValue("acct_type"));
  setValue("addr.p_seqno"              , getValue("p_seqno"));
  setValue("addr.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("addr.gift_group"           , "1");
  setValue("addr.acct_no"              , "");
  setValue("addr.tran_seqno"           , tranSeqno);
  setValue("addr.vendor_no"            , getValue("spec.vendor_no"));
  setValue("addr.gift_no"              , getValue("spec_gift_no")); 
  setValue("addr.gift_int"             , getValue("spec_gift_cnt")); 
  setValue("addr.id_no"                , getValue("idno.id_no"));
  setValue("addr.chi_name"             , getValue("idno.chi_name"));
  setValue("addr.bill_sending_zip"     , getValue("acno.bill_sending_zip"));
  setValue("addr.bill_sending_addr1"   , getValue("acno.bill_sending_addr1")); 
  setValue("addr.bill_sending_addr2"   , getValue("acno.bill_sending_addr2")); 
  setValue("addr.bill_sending_addr3"   , getValue("acno.bill_sending_addr3")); 
  setValue("addr.bill_sending_addr4"   , getValue("acno.bill_sending_addr4")); 
  setValue("addr.bill_sending_addr5"   , getValue("acno.bill_sending_addr5")); 
  setValue("addr.feedback_date"        , businessDate);
  setValue("addr.crt_date"             , sysDate);
  setValue("addr.crt_user"             , javaProgram); 
  setValue("addr.apr_date"             , sysDate);
  setValue("addr.apr_user"             , javaProgram); 
  setValue("addr.apr_flag"             , "Y"); 
  setValue("addr.mod_time"             , sysDate+sysTime);
  setValue("addr.mod_pgm"              , javaProgram);

  daoTable  = "mkt_gift_addrlist";

  insertTable();

  return(0);
 }
// ************************************************************************
 int deleteMktGiftAddrlist(int inti) throws Exception
 {
  daoTable  = "mkt_gift_addrlist";
  whereStr  = "where gift_group    = '1' "
            + "and   feedback_date = ? "
            + "and   active_code   = ? "
            ;

  setString(1 , businessDate);
  setString(2 , getValue("parm.active_code",inti));

  int recCnt = deleteTable();

  if (recCnt>0)
     showLogMessage("I","","刪除 mkt_gift_addrlist 筆數  : ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_no,"
            + "chi_name";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno = ? "
            ;

  setString(1 , getValue("id_p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  return(0);
 }
// ************************************************************************
 int selectActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "bill_sending_zip ,"
            + "bill_sending_addr1,"
            + "bill_sending_addr2,"
            + "bill_sending_addr3,"
            + "bill_sending_addr4,"
            + "bill_sending_addr5";
  daoTable  = "act_acno";
  whereStr  = "where p_seqno = ? "
            ;

  setString(1 , getValue("p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  return(0);
 }
// ************************************************************************
 int selectMktSpecGift() throws Exception
 {
  extendField = "spec.";
  selectSQL  = "vendor_no";
  daoTable  = "mkt_spec_gift";
  whereStr  = "WHERE gift_no       = ? "
            + "and   gift_group    = '1' "
            ;
  setString(1 , getValue("spec_gift_no"));
  selectTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select mkt_spec_gift error!" );
      showLogMessage("I","","gift_no ["+ getValue("spec_gift_no")+"]" );
      //exitProgram(1);  //grace:不直接exit
     }

  return(0);
 }
// ************************************************************************
 void loadMktGiftAddrlist() throws Exception
 {
  extendField = "list.";
  selectSQL = "id_p_seqno";
  daoTable  = "mkt_gift_addrlist";
  whereStr  = "WHERE active_code in ( "
            + "      select active_code "
            + "      from   mkt_fstp_parm "
            + "      WHERE apr_flag        = 'Y' "
            + "      AND   apr_date       != ''  "
            + "      AND   (stop_flag     != 'Y'  "
            + "       or    (stop_flag     = 'Y'  "
            + "        and  stop_date      > ? )) "
            + "      and   issue_date_s <= ? "
            + "      and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "                   (purchase_days+n1_days+1) days,'yyyymmdd') >= ?   "
            ;
  if (activeCode.length()>0) 
     whereStr  = whereStr 
               + "and active_code = ? ";  

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);

  if (activeCode.length()>0) 
     setString(4 , activeCode);

  whereStr  = whereStr + "     ) ";

  int  n = loadTable();
  setLoadData("list.id_p_seqno");

  showLogMessage("I","","Load mkt_gift_addrlist Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample
