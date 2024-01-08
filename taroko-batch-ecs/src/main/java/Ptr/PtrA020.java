/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/06/06  V1.01.01  Lai         program initial                           *
*  112/08/02  V1.01.02  Lai         add CRM69                                 *
*  112/09/21  V1.01.03  Lai         modify errExit -> showLogMessage          *
*  112/12/25  V1.01.04  Lai         modify hThisAcctMonth                     *
*                                                                             *
******************************************************************************/
package Ptr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*國際信用卡清算明細表-信用卡*/
public class PtrA020 extends AccessDAO {
    private String progname = "每月產生循環信用利率分析統計資料  112/12/25 V1.01.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int    DEBUG = 1;

    String hBusinssDate    = "";
    String hThisAcctMonth  = "";
    String hCallBatchSeqno = "";

    int    totalCnt        = 0;

    String rptId           = "";
    int    hrptSeq         = 0;
    String hCardIndicator  = "";
    String hIsDisableC     = "";
    String hCardType       = "";
    double hRcrateYear     = 0;
    double hRcrateDay      = 0;
    double hRcrateRiSum    = 0;
    double hRcrateCpSum    = 0;
    double hRcrateCountC   = 0;
    int    hRating         = 0;
    int    hFlowCountC     = 0;
    int    hFlowCountA     = 0;
    String hIsLegalPerson  = "";
    String hIsSelect       = "";
    String hAnalRowid      = "";

    double hRcrateCpAmt   = 0;
    double hRcrateRiAmt   = 0;

/***********************************************************************/
public static void main(String[] args) throws Exception {
   PtrA020 proc = new PtrA020();
   int retCode = proc.mainProcess(args);
   proc.programEnd(retCode);
}
/***********************************************************************/
public int mainProcess(String[] args) 
{
try 
  {

   // ====================================
   // 固定要做的
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I", "", javaProgram + " " + progname);

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   commonRtn();
   
   showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
   
   if (args.length == 1 && args[0].length() == 8) {
   	hBusinssDate = args[0];
   }
   
   hThisAcctMonth = comc.getSubString(hBusinssDate, 0, 6);
   showLogMessage("I", "", "資料日期=[" + hBusinssDate + "] Month=" + hThisAcctMonth);

   int h_ten_day  = Integer.parseInt(comc.getSubString(hBusinssDate, 6, 8));
   if(h_ten_day != 27)
     {
      showLogMessage("I", "", "今天非27號，不執行。 今天是=["+h_ten_day+"]");
      return 0;
     }

   deletePtrRcrateAnal();

   /*******************************************************************/
   rptId    = "CRM141";
   selectActDebt();    // CRM141 , CRM68   for 動用循環
   showLogMessage("I", "", "DEBT 程式執行結束,筆數=[" + totalCnt + "]");
   totalCnt = 0;
   selectPtrRcrateAnal(1);
   showLogMessage("I", "", "ANAL 程式執行結束,筆數=[" + totalCnt + "]");
   selectPtrRcrateAnal_1(1);

   /*******************************************************************/
   rptId    = "CRM69";
   totalCnt = 0;
   selectCrdCard();    // CRM69   for All card
   showLogMessage("I", "", "CARD 程式執行結束,筆數=[" + totalCnt + "]");
   totalCnt = 0;
   selectPtrRcrateAnal(2);
   showLogMessage("I", "", "ANAL 程式執行結束,筆數=[" + totalCnt + "]");
// selectPtrRcrateAnal_1(2);

   /*******************************************************************/

   commitDataBase();
   
   // ==============================================
   // 固定要做的
   showLogMessage("I", "", "執行結束");
   finalProcess();
   return 0;
  }  catch (Exception ex) {
            expMethod = "mainProcess"; expHandle(ex); return exceptExit;
       }
}
/***********************************************************************/
void commonRtn() throws Exception {
   hBusinssDate = "";
   sqlCmd  = "select business_date ";
   sqlCmd += "  from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select_ptr_businday not found!", "", "");
   }
   if (recordCnt > 0) {
       hBusinssDate = getValue("business_date");
   }

   sqlCmd  = "select this_acct_month ";
   sqlCmd += "  from ptr_workday  ";
   sqlCmd += " where stmt_cycle in (select min(stmt_cycle) from ptr_workday) ";
       recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select_ptr_workday  not found!", "", "");
   }
   if (recordCnt > 0) {
       hThisAcctMonth = getValue("this_acct_month");
   }
}
/***********************************************************************/
void selectActDebt() throws Exception {

   // 動用循環---  利息金額 , 本金金額
   sqlCmd  = "select c.card_indicator, d.card_type ";
   sqlCmd += "     , decode(d.current_code , '0' , 'N' ,'Y')  as is_disable_c ";
   sqlCmd += "     , c.rcrate_year ";
   sqlCmd += "     , count(DISTINCT(d.card_no))        as flow_count_c ";
   sqlCmd += "     , count(DISTINCT(d.acno_p_seqno))   as flow_count_a ";
   sqlCmd += "     , sum(decode(b.acct_code      , 'RI', a.end_bal, 0))  as rcrate_ri_sum  ";
   sqlCmd += "     , sum(decode(b.interest_method, 'Y' , a.end_bal, 0))  as rcrate_cp_sum  ";
   sqlCmd += "     , count(DISTINCT(a.card_no))        as rcrate_count_c ";
   sqlCmd += "  from crd_card d, act_debt a, ptr_actcode b, act_acno c ";
   sqlCmd += " where a.acct_code  = b.acct_code ";
   sqlCmd += "   and c.p_seqno    = a.p_seqno and c.acno_flag  in('1','3') ";
   sqlCmd += "   and d.p_seqno    = a.p_seqno   ";
   sqlCmd += "   and a.acct_month <= ?     ";
   sqlCmd += "   and a.curr_code  = '901' ";
   sqlCmd += "   and a.end_bal    > 0     ";
   sqlCmd += " group by c.card_indicator,d.card_type,decode(d.current_code,'0','N','Y'),c.rcrate_year ";
   sqlCmd += " order by c.card_indicator,d.card_type,decode(d.current_code,'0','N','Y'),c.rcrate_year ";
// sqlCmd += " order by c.card_indicator, d.card_type, d.current_code, c.rcrate_year ";

   setString(1,hThisAcctMonth);

   int cursorIndex = openCursor();
   while (fetchTable(cursorIndex)) {

      totalCnt++;
      if (totalCnt % 10000 == 0 || totalCnt==1) showLogMessage("I","","Process record=["+totalCnt+"]");

      hCardIndicator    = getValue("card_indicator");
      hIsDisableC       = getValue("is_disable_c");       // current_code 是否停用卡 Y/N
      hCardType         = getValue("card_type");
      hFlowCountC       = getValueInt("flow_count_c");
      hFlowCountA       = getValueInt("flow_count_a");
      hRcrateYear       = getValueDouble("rcrate_year");
      hRcrateRiSum      = getValueDouble("rcrate_ri_sum");
      hRcrateCpSum      = getValueDouble("rcrate_cp_sum");
      hRcrateCountC     = getValueDouble("rcrate_count_c");
      if(DEBUG == 1)
        showLogMessage("I", "", "Read main Indicator="+hCardIndicator+","+hCardType+","+hRcrateYear+","+hRcrateRiSum+","+hRcrateCpSum+","+totalCnt);

      selectPtrRcrate(1);

      insertPtrRcrateAnal(1);
   }

   closeCursor(cursorIndex);
}
/***********************************************************************/
void deletePtrRcrateAnal() throws Exception {

   daoTable  = " ptr_rcrate_anal ";
   whereStr  = " where 1=1 ";
   whereStr += "   and proc_month = ? ";
// whereStr += "   and rpt_id     = ? ";

   setString(1, hThisAcctMonth);
// setString(2, rptId);

   deleteTable();

   daoTable  = " ptr_rpt_realrate ";
   whereStr  = " where 1=1 ";
   whereStr += "   and proc_month = ? ";

   setString(1, hThisAcctMonth);

   deleteTable();
}
/***********************************************************************/
void selectPtrRcrate(int idx) throws Exception {

  hRating    = 0;
  hRcrateDay = 0;
  sqlCmd  = "select rating      ";
  sqlCmd += "     , rcrate_day  ";
  sqlCmd += " from ptr_rcrate   ";
  sqlCmd += "where rcrate_year = ? ";
  setDouble(1, hRcrateYear);
  int recordCnt = selectTable();
  if (notFound.equals("Y")) 
      showLogMessage("I", "", "  select ptr_rcrate not find=["+hRcrateYear+"]");
     
  if (recordCnt > 0) {
      hRating    = getValueInt("rating");
      hRcrateDay = getValueDouble("rcrate_day");
  }
if(DEBUG == 1)
   showLogMessage("I", "", "  Get Year="+hRcrateYear+","+hRating+","+hRcrateDay);
}
/***********************************************************************/
void insertPtrRcrateAnal(int idx)  throws Exception {

if(DEBUG == 1)
   showLogMessage("I", "", "  insert="+hThisAcctMonth+","+rptId+","+totalCnt);
//if(hCardIndicator.equals("2") && (hRcrateYear != 6 && hRcrateYear != 14.6) )  return;

  daoTable = "ptr_rcrate_anal";
  extendField = daoTable + ".";
  setValue(extendField + "proc_month"           , hThisAcctMonth);
  setValue(extendField + "rpt_id"               , rptId);
  setValueInt(extendField + "rpt_seq"           , totalCnt);
  setValue(extendField + "card_type"            , hCardType);
  setValue(extendField + "is_legal_person"      , hCardIndicator.equals("2") ? "Y" : "N");
  setValueDouble(extendField + "rcrate_year"    , hRcrateYear );
  setValueDouble(extendField + "rcrate_day"     , hRcrateDay  );
  setValueInt(extendField + "rating"            , hRating);
  setValueDouble(extendField + "rcrate_cp_amt"  , hRcrateCpSum );
  setValueDouble(extendField + "rcrate_ri_amt"  , hRcrateRiSum );
  setValueDouble(extendField + "rcrate_count_c" , hRcrateCountC );
  setValue(extendField       + "is_disable_c"   , hIsDisableC);
  setValue(extendField       + "card_type"      , hCardType);
  setValueInt(extendField    + "flow_count_c"   , hFlowCountC);
  setValueInt(extendField    + "flow_count_a"   , hFlowCountA);
/*
  setValue(extendField + "is_select"            , "Y");
*/
  setValue(extendField + "mod_pgm"              , javaProgram);
  setValue(extendField + "mod_time"             , sysDate + sysTime);
  insertTable();
  if (dupRecord.equals("Y")) {
      comcr.errRtn("insert ptr_rcrate_anal duplicate", hThisAcctMonth, comcr.hCallBatchSeqno);
  }

}
/***********************************************************************/
void insertPtrRptRealRate()  throws Exception {
if(DEBUG == 1)
   showLogMessage("I", "", "  insert 2="+hThisAcctMonth+","+hRcrateRiAmt+","+hRcrateCpAmt);
  daoTable = "ptr_rpt_realrate ";
  extendField = daoTable + ".";
  setValue(extendField + "proc_month"         , hThisAcctMonth);
  setValue(extendField + "card_flag"          , hIsLegalPerson.equals("Y") ? "2" : "1");
  setValueDouble(extendField + "real_int_rate", hRcrateCpAmt == 0 ? 0 : hRcrateRiAmt*100/hRcrateCpAmt);
  setValue(extendField + "mod_pgm"            , javaProgram);
  setValue(extendField + "mod_time"           , sysDate + sysTime);
  insertTable();
  if (dupRecord.equals("Y")) {
      comcr.errRtn("insert ptr_rpt_realrate duplicate", hThisAcctMonth, comcr.hCallBatchSeqno);
  }
}
/***********************************************************************/
void selectPtrRcrateAnal(int idx) throws Exception {

   sqlCmd  = "select a.is_legal_person ";
   sqlCmd += "     , a.rcrate_year     ";
   sqlCmd += "     , a.rcrate_day      ";
   sqlCmd += "     , a.rating          ";
   sqlCmd += "     , a.rpt_seq         ";
   sqlCmd += "     , a.rowid as rowid  ";
   sqlCmd += "  from ptr_rcrate_anal a ";
   sqlCmd += " where a.proc_month  = ? ";
   sqlCmd += "   and a.rpt_id      = ? ";
   sqlCmd += " order by a.is_legal_person,a.rcrate_year ";

   setString(1, hThisAcctMonth);
   setString(2, rptId);

   int recordCnt = selectTable();
   for (int i = 0; i < recordCnt; i++) {

      totalCnt++;
      if (totalCnt % 10000 == 0 || totalCnt==1) showLogMessage("I","","Process rec 2=["+totalCnt+"]");

      hIsLegalPerson    = getValue("is_legal_person"   , i);
      hRcrateYear       = getValueDouble("rcrate_year" , i);
      hRcrateDay        = getValueDouble("rcrate_day"  , i);
      hRating           = getValueInt("rating"         , i);
      hrptSeq           = getValueInt("rpt_seq"        , i);
      hAnalRowid        = getValue("rowid"             , i);
      
      if(DEBUG == 1)
        showLogMessage("I","","Read main 2="+hIsLegalPerson+","+hRcrateYear+","+hRcrateDay+","+hRating);

      hIsSelect      = "Y";
      if(hRcrateYear == 0 && hRcrateDay == 0)  
        {
         hIsSelect   = "N";
         hRating     = 0;
        }

      if(hIsLegalPerson.equals("Y"))
        {
         if((hRcrateYear == 6 || hRcrateYear == 14.6))
           {
             hIsSelect   = "Y";
           }
         else
           {
            hIsSelect   = "N";
            hRating     = 0;
           }
        }
      if(hIsLegalPerson.equals("N") && hRcrateYear == 6)
        {
         hIsSelect   = "Y";
         hRating     = 0;
        }
      if(DEBUG == 1)
         showLogMessage("I","","   end select="+hIsSelect+","+hRating);

      updatePtrRcrateAnal();
   }

}
/***********************************************************************/
void selectPtrRcrateAnal_1(int idx) throws Exception {

   sqlCmd  = "select a.is_legal_person  ";
   sqlCmd += "     , sum(rcrate_cp_amt) as rcrate_cp_amt  ";
   sqlCmd += "     , sum(rcrate_ri_amt) as rcrate_ri_amt  ";
   sqlCmd += "  from ptr_rcrate_anal a  ";
   sqlCmd += " where a.proc_month    = ? ";
   sqlCmd += "   and a.rpt_id        = ? ";
   sqlCmd += "   and a.is_disable_c <> 'Y' ";  // 是否停用卡 Y/N
   sqlCmd += " group by a.is_legal_person ";

   setString(1, hThisAcctMonth);
   setString(2, rptId);

   int recordCnt = selectTable();
   if(DEBUG == 1) showLogMessage("I","","   select anal_1="+hThisAcctMonth+","+rptId+","+recordCnt);

   for (int i = 0; i < recordCnt; i++) {

      hIsLegalPerson    = getValue("is_legal_person"     , i);
      hRcrateCpAmt      = getValueDouble("rcrate_cp_amt" , i);
      hRcrateRiAmt      = getValueDouble("rcrate_ri_amt" , i);
      if(DEBUG == 1)
        showLogMessage("I","","Read main 3="+hIsLegalPerson+","+hRcrateRiAmt+","+hRcrateCpAmt);

      if(idx == 1)  insertPtrRptRealRate();
   }
}
/***********************************************************************/
void updatePtrRcrateAnal() throws Exception {

   daoTable  = "ptr_rcrate_anal  ";
   updateSQL = " is_select    = ? , "
             + " rating       = ? , "
             + " mod_time     = sysdate ";
   whereStr  = "where rowid   = ? ";


   setString(1, hIsSelect);
   setInt   (2, hRating);
   setRowId( 3, hAnalRowid);
   updateTable();
   if (notFound.equals("Y")) {
       String stderr = "updater ptr_rcrate_anal not found="+hrptSeq;
       comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
      }
}
/***********************************************************************/
void selectCrdCard() throws Exception 
{
   sqlCmd  = "select c.card_indicator, a.card_type ";
   sqlCmd += "     , decode(a.current_code , '0' , 'N' ,'Y')  as is_disable_c ";
   sqlCmd += "     , c.rcrate_year ";
   sqlCmd += "     , count(DISTINCT(a.card_no))        as flow_count_c ";
   sqlCmd += "     , count(DISTINCT(a.acno_p_seqno))   as flow_count_a ";
   sqlCmd += "     , count(DISTINCT(a.card_no))        as rcrate_count_c ";
   sqlCmd += "  from act_acno c, crd_card a";
   sqlCmd += " where 1=1  ";
   sqlCmd += "   and c.acno_p_seqno  = a.acno_p_seqno   ";
   sqlCmd += " group by c.card_indicator,a.card_type,decode(a.current_code,'0','N','Y'),c.rcrate_year ";
   sqlCmd += " order by c.card_indicator,a.card_type,decode(a.current_code,'0','N','Y'),c.rcrate_year ";

// setString(1,hThisAcctMonth);

   int cursorIndex2 = openCursor();
   while (fetchTable(cursorIndex2)) {

      totalCnt++;
      if (totalCnt % 10000 == 0 || totalCnt==1) showLogMessage("I","","Process record=["+totalCnt+"]");

      hCardIndicator    = getValue("card_indicator");
      hIsDisableC       = getValue("is_disable_c");       // current_code 是否停用卡 Y/N
      hCardType         = getValue("card_type");
      hFlowCountC       = getValueInt("flow_count_c");
      hFlowCountA       = getValueInt("flow_count_a");
      hRcrateYear       = getValueDouble("rcrate_year");
      hRcrateRiSum      = 0;
      hRcrateCpSum      = 0;
      hRcrateCountC     = getValueDouble("rcrate_count_c");
      if(DEBUG == 1)
        showLogMessage("I", "", "Read main Indicator="+hCardIndicator+","+hCardType+","+hRcrateYear+","+hRcrateRiSum+","+hRcrateCpSum+","+totalCnt);

      selectPtrRcrate(2);

      insertPtrRcrateAnal(2);
   }

   closeCursor(cursorIndex2);
}
/**********************  END  **********************************************/
}
/*
   V-無限 V-御璽 V-白金 V-金卡 V-普卡 
   M-世界 M-鈦金 M-鈦商 M-白金 M-金卡 M-普卡 
   J-白金 J-金卡 J-普卡 J-晶緻 
　      第2碼   C       普      C:普卡
　              G       金      G:金卡
　              P       白金卡  P:白金卡
　              T       晶緻卡/御璽卡/鈦金      S:卓越卡
　              B       商務卡  　
　              E       電子採購卡      　
　              I       無限/世界卡     I:頂級卡
　              O       COMBO 普        　
　              Q       COMBO 金        　
　              R       COMBO 白金卡    　
　              S       COMBO 鈦金/御璽卡       　
　              D       VD      　
*/
