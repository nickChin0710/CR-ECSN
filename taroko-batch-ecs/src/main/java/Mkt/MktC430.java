/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/27  V1.00.30  Allen Ho   New   278034                               *
* 111/10/13  V1.00.02  Yang Bo    sync code from mega                        *
* 111/10/27  V1.00.03  Yang Bo    act_debt 改為 act_debt_hst                  *
* 112/03/08  v1.00.04  Grace      rename 'megaline_cond' 為 'banklite_cond'   * 
*                                 rename 'megaline_flag' 為 'banklite_flag'   * 
******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktC430 extends AccessDAO
{
 private  String progname = "首刷禮-判斷回饋項目處理程式 111/10/13  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommDBonus comd = null;

 String businessDate = "";
 String activeCode ="";
 String idPSeqno ="";

 String      perAmtCond1   = "";
 double      perAmt1       = 0;
 String[][]  thresholdSel  = new String[100][10];
 String[][]  perAmtCond    = new String[100][10];
 double[][]  perAmt        = new double[100][10];
 String[][]  sumCntCond    = new String[100][10];
 double[][]  sumAmt        = new double[100][10];
 String[][]  sumAmtCond    = new String[100][20];
 int[][]     sumCnt        = new int[100][10];
 int[][]     purchaseDays  = new int[100][10];
 int[][]     giveDays      = new int[100][10];

 String      purchaseTypeSel = "";
 String[][]  activeSeq        = new String[100][10];
 String[][]  recordGroupNo   = new String[100][10];

 double[] purchaseAmtS =  new double [8];
 double[] purchaseAmtE =  new double [8];
 double[] feedbackAmt   =  new double [8];
 double   feedbackLimit =  0;

 String recordYes = "";
 String recordNo  = "";
 int    amtCntSel = 0;
 int  parmCnt  = 0,pseqCnt=0;
 int  purcCnt  = 0;
 int  matchFlag  = 0;

 int  totalCnt=0;
 boolean DEBUG = false;
 boolean DEBUG1= false;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC430 proc = new MktC430();
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
       showLogMessage("I","","PARM 3 : [id_p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   if ( args.length >= 2 )
       activeCode = args[1];

   if ( args.length == 3 )
       idPSeqno = args[2];

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());
   comd = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktFstpParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","今日["+ businessDate +"]無活動回饋");
       return(0);
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktLineCust();
   loadActAcno();
   loadActDebt();
   loadActDebtHst();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理(mkt_fstp_carddtl)資料");
   selectMktFstpCarddtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
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
 void selectMktFstpCarddtl() throws Exception
 {
  selectSQL  = "";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "where issue_date   <= ? "
            ;

  setString(1 , businessDate);
  
   if (activeCode.length()!=0)
      {
       whereStr  = whereStr 
                 + "and  active_code = ? "; 
       setString(2 , activeCode);
      }

   if (idPSeqno.length()!=0)
      {
       whereStr  = whereStr
                 + "and  id_p_seqno = ? "
                 + "and  (error_code    = '00' "
                 + " or   error_code    like 'B%') "
                 ;
       setString(3 , idPSeqno);
      }
   else 
      {
       whereStr  = whereStr
                 + "and   proc_flag     = 'N' "
                 + "and   error_code    = '00' ";
      }
    
  openCursor();

  int cnt1=0;
  int inti =0;
  int okFlag=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    okFlag=0;
    for (int intm=0;intm<parmCnt;intm++)
      {
       if (getValueInt("parm.pseqcnt",inti)==0) continue;
       if (getValue("active_code").equals(getValue("parm.active_code",intm)))
          {
           inti = intm;
           okFlag=1;
           break;
          }
      }
    if (okFlag==0) continue;

      
    if (getValue("achieve_cond").equals("N"))
        if (businessDate.compareTo(comm.nextNDate(getValue("issue_date")
                                                  ,(getValueInt("parm.purchase_days",inti)
                                                  + getValueInt("parm.n1_days",inti)+1)))<0) continue;

    if (idPSeqno.length()!=0)
       { 
        showLogMessage("I","","p_seqno : ["+ getValue("p_seqno")  +"]");
        showLogMessage("I","","card_no : ["+ getValue("card_no")  +"]");
       }
    matchFlag = 0;
    setValue("tot_sum_amt"    , "0");
    setValue("tot_in_sum_amt" , "0");
    setValue("tot_sum_cnt"    , "0");
    // ***************** annul fee  ********************
    if (getValue("parm.anulfee_cond",inti).equals("Y"))
       {
        if (!getValue("anulfee_flag").equals("Y"))
           {
            setValue("anulfee_flag" , "N");
            setValue("anulfee_date" , "");
            if (businessDate.compareTo(comm.nextNDate(getValue("issue_date"),
                                                       getValueInt("parm.anulfee_days",inti)))<=0)
               {
                setValue("debt.card_no"     , getValue("card_no"));
                cnt1 = getLoadData("debt.card_no");
                if (cnt1>0)  
                   {
                    setValue("anulfee_flag" , "Y");
                    setValue("anulfee_date" , businessDate);
                   }
                else 
                   {
                    setValue("dhst.card_no"  , getValue("card_no"));
                    cnt1 = getLoadData("dhst.card_no");
                    if (cnt1>0)  
                       {
                        setValue("anulfee_flag" , "Y");
                        setValue("anulfee_date" , businessDate);
                       }
                    else matchFlag =1;
                   }
               }
            else matchFlag =1;
           }
       }  
    else setValue("anulfee_flag" , "X");

    if (idPSeqno.length()!=0)
        showLogMessage("I","","STEP 000-1 anulfee_flag : ["+ getValue("anulfee_flag")  +"]");
    // ***************** lineBC ************************
    if (getValue("parm.linebc_cond",inti).equals("Y"))
       {
        if (!getValue("linebc_flag").equals("Y"))
           {
            setValue("linebc_flag"         , "N");
            setValue("cust.id_p_seqno"     , getValue("id_p_seqno"));
            cnt1 = getLoadData("cust.id_p_seqno");
            if (cnt1>0)  setValue("linebc_flag" , "Y");
            else matchFlag =1;
           }
       }
    else setValue("linebc_flag"         , "X");
    if (idPSeqno.length()!=0)
        showLogMessage("I","","STEP 000-2 linebc_flag : ["+ getValue("linebc_flag")  +"]");
    // ***************** select autopay ****************
    if (getValue("parm.selfdeduct_cond",inti).equals("Y"))
       {
        if (!getValue("selfdeduct_flag").equals("Y"))
           {
            setValue("selfdeduct_flag" , "N");
            setValue("acno.p_seqno"     , getValue("p_seqno"));
            cnt1 = getLoadData("acno.p_seqno");
            if (cnt1>0) setValue("selfdeduct_flag" , "Y");
            else matchFlag =1;
           }
       }
    else setValue("selfdeduct_flag" , "X");
    if (idPSeqno.length()!=0)
        showLogMessage("I","","STEP 000-3 selfdeduct_flag : ["+ getValue("selfdeduct_flag")  +"]");
    // ***************** MEGA Lite   ****************
       // MEGA 開發中
    /* 暫時remark, by grace
    if (getValue("parm.megaline_cond",inti).equals("Y"))
       {
        if (!getValue("megaline_flag").equals("Y"))
           {
            setValue("megaline_flag" , "N");
//          setValue("acno.p_seqno"     , getValue("p_seqno"));
//          cnt1 = getLoadData("acno.p_seqno");
//          if (cnt1>0) setValue("megaline_flag" , "Y");
//          else match_flag=1;
           }
       }
    else setValue("megaline_flag" , "X");
    if (idPSeqno.length()!=0)
        showLogMessage("I","","STEP 000-4 megalite_flag : ["+ getValue("megaline_flag")  +"]");
    */
    // rename by grace
    if (getValue("parm.banklite_cond",inti).equals("Y"))
       {
        if (!getValue("banklite_flag").equals("Y"))
           {
            setValue("banklite_flag" , "N");
//          setValue("acno.p_seqno"     , getValue("p_seqno"));
//          cnt1 = getLoadData("acno.p_seqno");
//          if (cnt1>0) setValue("megaline_flag" , "Y");
//          else match_flag=1;
           }
       }
    else setValue("banklite_flag" , "X");
    if (idPSeqno.length()!=0)
        showLogMessage("I","","STEP 000-4 banklite_flag : ["+ getValue("banklite_flag")  +"]");
    
    // ***************** 消費  *************************
    setValue("purchase_flag" , "N");
    setValue("match_active_seq" ,  "");
    okFlag=0;
    int nowActiveSeq=0;
    // ***************** 登錄  *************************
    if (getValue("parm.multi_fb_type",inti).equals("1"))
       {
        setValue("match_active_seq" ,  "00");
        setValue("record_flag" , "N");
        if (getValue("parm.record_cond",inti).equals("N"))
           {
            setValue("record_flag" , "X");
           }
        else
           {
            if (recordGroupNo[inti][0].equals(getValue("record_no"))) 
               setValue("record_flag" , "Y");
           }
       }
    else
        {
         setValue("record_flag" , "");
         matchFlag =0; 
         if (recordYes.length()==0)
             setValue("record_flag" , "X");
         else if (recordNo.length()==0)
            {
             setValue("record_flag" , "N");
             if (getValue("record_no").length()==0) matchFlag =1;
            }
        }
    if (idPSeqno.length()!=0)
        showLogMessage("I","","STEP 000-5 record_flag : ["+ getValue("record_flag")  +"]");
    // ***************** 消費  *************************
    if (!getValue("purchase_flag").equals("1"))
       nowFeedbackCond(inti, matchFlag);

    if (idPSeqno.length()!=0)
        showLogMessage("I","","STEP 000-6 purchase_flag : ["+ getValue("purchase_flag")  +"]");

    updateMktFstpCarddtl(inti);

    processDisplay(10000); // every 10000 display message
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
            + "and   to_char(to_date(issue_date_e,'yyyymmdd')+"
            + "             (purchase_days+n1_days+1) days,'yyyymmdd') >= ?  "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , businessDate);

  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and active_code = ? ";  
      setString(4 , activeCode);
     }
  parmCnt = selectTable();

  for (int inti=0;inti<parmCnt;inti++)
    {
     showLogMessage("I","","活動代號:["+ getValue("parm.active_code",inti) +"]-"+getValue("parm.active_name",inti));
     updateMktFstpCarddtl0(inti);
     if (getValue("parm.multi_fb_type",inti).equals("1"))
        {
         purchaseDays[inti][0]  = getValueInt("parm.purchase_days",inti);
         giveDays[inti][0]      = 0;
           
         thresholdSel[inti][0]   = getValue("parm.threshold_sel",inti);
         perAmtCond[inti][0]    = getValue("parm.per_amt_cond",inti); 
         perAmt[inti][0]         = getValueDouble("parm.per_amt",inti); 
         sumAmtCond[inti][0]    = getValue("parm.sum_amt_cond",inti);
         sumAmt[inti][0]         = getValueDouble("parm.sum_amt",inti);
         sumCntCond[inti][0]    = getValue("parm.sum_cnt_cond",inti);
         sumCnt[inti][0]         = getValueInt("parm.sum_cnt",inti);

         activeSeq[inti][0]      = "00";
         recordGroupNo[inti][0] = "";
         if (getValue("parm.record_cond",inti).equals("Y"))
            { 
             recordGroupNo[inti][0] = getValue("parm.record_group_no",inti);
            }    
         pseqCnt=1;
        }
     else 
        {
         selectMktFstpParmseq(inti);
        }
     setValueInt("parm.pseqcnt" , pseqCnt , inti);    
    }

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 int selectMktFstpParmseq(int inti) throws Exception
 {
  extendField = "pseq.";
  daoTable  = "mkt_fstp_parmseq";
  whereStr  = "WHERE active_code     = ? "
            + "order by level_seq,active_seq"
            ;

  setString(1 , getValue("parm.active_code",inti));

  pseqCnt = selectTable();

  setValue("parm.record_flag" , "N" ,inti);
  for (int intk=0;intk<pseqCnt;intk++)
     {
     showLogMessage("I","","  活動序號:["+ getValue("pseq.active_seq",intk) +"]");
     if (getValue("pseq.pur_date_sel",intk).equals("1"))
        {
         purchaseDays[inti][intk]  = getValueInt("parm.purchase_days",inti);
         giveDays[inti][intk]      = 0;
        }
     else
        {
         purchaseDays[inti][intk]  = getValueInt("pseq.purchase_days",intk);
         giveDays[inti][intk]      = getValueInt("pseq.give_days",intk);
        }
      perAmtCond[inti][intk]    = getValue("pseq.per_amt_cond",intk); 
      thresholdSel[inti][intk]   = getValue("pseq.threshold_sel",intk);
      perAmt[inti][intk]         = getValueDouble("pseq.per_amt",intk); 
      sumAmtCond[inti][intk]    = getValue("pseq.sum_amt_cond",intk); 
      sumAmt[inti][intk]         = getValueDouble("pseq.sum_amt",intk); 
      sumCntCond[inti][intk]    = getValue("pseq.sum_cnt_cond",intk); 
      sumCnt[inti][intk]         = getValueInt("pseq.sum_cnt",intk);

      activeSeq[inti][intk]      = getValue("pseq.active_seq",intk);
      if (getValue("pseq.record_cond",intk).equals("Y"))
         { 
          recordGroupNo[inti][intk] = getValue("pseq.record_group_no",intk);
          recordYes = "Y";
         }    
      else
         { 
          recordGroupNo[inti][intk] = "";
          recordNo = "Y";
         }    
     }

  return(0);
 }
// ************************************************************************
 void loadMktLineCust() throws Exception
 {
  extendField = "cust.";
  selectSQL = "id_p_seqno";
  daoTable  = "mkt_line_cust a,crd_idno b";
  whereStr  = "where a.id_no = b.id_no "
            + "and   a.status_code = '0' "
            + "and   b.id_p_seqno in ( "
            + "      select distinct(id_p_seqno) "
            + "      from   mkt_fstp_carddtl "
            ;

   if (idPSeqno.length()!=0)
      {
       whereStr  = whereStr 
                 + "where  (error_code    = '00' "
                 + " or   error_code    like 'B%')) "
                 + "and  b.id_p_seqno = ? "; 
       setString(1 , idPSeqno);
      }
   else
      {
       whereStr  = whereStr 
                 + "      where  proc_flag  = 'N' "
                 + "      AND   error_code  = '00' ) "; 
      }

  int  n = loadTable();
  setLoadData("cust.id_p_seqno");

  showLogMessage("I","","Load mkt_line_cust Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno";
  daoTable  = "act_acno";
  whereStr  = "where autopay_acct_s_date <= ? "
            + "and   decode(autopay_acct_e_date,'','30001231',autopay_acct_e_date) >= ? "
            + "and   p_seqno in ( "
            + "      select distinct(p_seqno) "
            + "      from   mkt_fstp_carddtl "
            + "      where  1 = 1 "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);

   if (idPSeqno.length()!=0)
      {
       whereStr  = whereStr 
                 + "and  (error_code    = '00' "
                 + " or   error_code    like 'B%')) "
                 + "and  id_p_seqno = ? "; 
       setString(3 , idPSeqno);
      }
   else
      {
       whereStr  = whereStr 
                 + "      and    proc_flag  = 'N' "
                 + "      AND   error_code  = '00' ) "; 
      }
  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load act_acno Count: ["+n+"]");
 }
// ************************************************************************
 void loadActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "card_no";
  daoTable  = "act_debt a";
  whereStr  = "where acct_code = 'AF' "
            + "and   end_bal   = 0 "
            + "and   post_date <= ?  "
            + "and   exists  ( "
            + "      select 1 "
            + "      from   mkt_fstp_carddtl "
            + "      where  anulfee_flag != 'X' "
            + "      and    card_no = a.card_no "
            + "      and    issue_date    < a.post_date "
            ;

  setString(1 , businessDate);

   if (idPSeqno.length()!=0)
      {
       whereStr  = whereStr 
                 + "and  (error_code    = '00' "
                 + " or   error_code    like 'B%') "
                 + "and  id_p_seqno = ?) "; 
       setString(2 , idPSeqno);
      }
   else
      {
       whereStr  = whereStr 
                 + "      and    proc_flag  = 'N' "
                 + "      AND   error_code  = '00' ) "; 
      }

  int  n = loadTable();
  setLoadData("debt.card_no");

  showLogMessage("I","","Load act_debt Count: ["+n+"]");
 }
// ************************************************************************
 void loadActDebtHst() throws Exception
 {
  extendField = "dhst.";
  selectSQL = "card_no";
  daoTable  = "act_debt_hst a";
  whereStr  = "where acct_code = 'AF' "
            + "and   end_bal   = 0 "
            + "and   post_date <= ?  "
            + "and   exists  ( "
            + "      select 1 "
            + "      from   mkt_fstp_carddtl "
            + "      where  anulfee_flag != 'X' "
            + "      and    card_no = a.card_no "
            + "      and    issue_date    < a.post_date "
            ;

  setString(1 , businessDate);

   if (idPSeqno.length()!=0)
      {
       whereStr  = whereStr 
                 + "and  (error_code    = '00' "
                 + " or   error_code    like 'B%') "
                 + "and  id_p_seqno = ?) "; 
       setString(2 , idPSeqno);
      }
   else
      {
       whereStr  = whereStr 
                 + "      and    proc_flag  = 'N' "
                 + "      AND   error_code  = '00' ) "; 
      }

  int  n = loadTable();
  setLoadData("dhst.card_no");

  showLogMessage("I","","Load act_debt_hst Count: ["+n+"]");
 }
// ************************************************************************
 int getMatchActiveSeq(int inti, int intk) throws Exception
 {
  if ((!sumAmtCond[inti][intk].equals("Y"))&&
      (!getValue("parm.mcht_in_cond",inti).equals("Y"))&&
      (!sumCntCond[inti][intk].equals("Y"))) return(0);

  perAmtCond1 = perAmtCond[inti][intk];
  perAmt1 = perAmt[inti][intk];
  selectMktFstpPurcdtl(inti,intk,1);

  if (sumAmtCond[inti][intk].equals("Y"))
     if (getValueDouble("pdtl.dest_amt") < sumAmt[inti][intk]) return(1);

  if (getValue("parm.mcht_in_cond",inti).equals("Y"))
     if (getValueDouble("pdtl.in_dest_amt") < getValueDouble("parm.mcht_in_amt",inti)) return(1); 

  if (sumCntCond[inti][intk].equals("Y"))
     if (getValueInt("pdtl.dest_cnt") < sumCnt[inti][intk]) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktFstpCarddtl0(int inti) throws Exception
 {
  dateTime();
  updateSQL = "prog_code            = 'N' ";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "WHERE  error_code    = '00' "
            + "AND    active_code   = ? "
            + "AND    prog_code    != 'N' "
            ;

  setString(1, getValue("active_code"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int updateMktFstpCarddtl(int inti) throws Exception
 {
  dateTime();
  updateSQL = "purchase_flag        = ?,"
            + "linebc_flag          = ?, "
            //+ "megaline_flag        = ?, "
            + "banklite_flag        = ?, "
            + "selfdeduct_flag      = ?, "
            + "anulfee_flag         = ?, "
            + "record_flag          = ?, "
            + "match_active_seq     = ?, "
            + "active_type          = ?, "
            + "bonus_type           = ?, "
            + "beg_tran_bp          = ?, "
            + "fund_code            = ?, "
            + "beg_tran_amt         = ?, "
            + "group_type           = ?, "
            + "prog_code            = ?, "
            + "prog_s_date          = ?, "
            + "prog_e_date          = ?, "
            + "gift_no              = ?, "
            + "tran_pt              = ?, "
            + "spec_gift_no         = ?, "
            + "spec_gift_cnt        = ?, "
            + "dest_amt             = ?, "
            + "dest_cnt             = ?, "
            + "execute_date         = ?, "
            + "mod_time             = sysdate,"
            + "mod_user             = ?, "
            + "mod_pgm              = ? ";
  daoTable  = "mkt_fstp_carddtl";
  whereStr  = "WHERE  card_no       = ? "
            + "AND    active_code   = ? ";

  setString(1 , getValue("purchase_flag"));
  setString(2 , getValue("linebc_flag")); 
  //setString(3 , getValue("megaline_flag"));
  setString(3 , getValue("banklite_flag")); 
  setString(4 , getValue("selfdeduct_flag")); 
  setString(5 , getValue("anulfee_flag"));               // nocheck 
  setString(6 , getValue("record_flag")); 
  setString(7 , getValue("match_active_seq"));
  setString(8 , getValue("active_type"));
  setString(9 , getValue("bonus_type"));
  setDouble(10, getValueDouble("beg_tran_bp"));
  setString(11, getValue("fund_code"));
  setDouble(12, getValueDouble("beg_tran_amt"));
  setString(13, getValue("group_type"));
  setString(14, getValue("prog_code"));
  setString(15, getValue("prog_s_date"));
  setString(16, getValue("prog_e_date"));
  setString(17, getValue("gift_no"));
  setDouble(18, getValueDouble("tran_pt"));
  setString(19, getValue("spec_gift_no"));
  setDouble(20, getValueDouble("spec_gift_cnt"));
  setDouble(21, getValueDouble("dest_amt"));
  setDouble(22, getValueDouble("dest_cnt"));
  if (getValue("parm.achieve_cond",inti).equals("Y"))
     setString(23, businessDate);
  else
     setString(23, getValue("last_execute_date"));
  setString(24, javaProgram);
  setString(25, javaProgram);
  setString(26, getValue("card_no"));
  setString(27, getValue("active_code"));

  updateTable();

  return(0);
 }
// ************************************************************************
 void initCarddtlData() throws Exception
 {
  setValue("active_type"   , "");
  setValue("bonus_type"    , "");
  setValue("beg_tran_bp"   , "0");
  setValue("fund_code"     , "");
  setValue("beg_tran_amt"  , "0");
  setValue("group_type"    , "");
  setValue("prog_code"     , "");
  setValue("prog_s_date"   , "");
  setValue("prog_e_date"   , "");
  setValue("gift_no"       , "");
  setValue("tran_pt"       , "0");
  setValue("spec_gift_no"  , "");
  setValue("spec_gift_cnt" , "0");
 }
// ************************************************************************
 int nowFeedbackCond(int inti, int matchFlag) throws Exception
 {
  initCarddtlData();

  if (matchFlag==1) return(1);

  if (!getValue("parm.multi_fb_type",inti).equals("2"))
     if (getValue("match_active_seq").length()==0) return(1);

  if (getValue("parm.multi_fb_type",inti).equals("2"))
     {
      if (idPSeqno.length()!=0)
          showLogMessage("I","","STEP 20210415 STEP 001 ]");

      for (int intk=0;intk<getValueInt("parm.pseqcnt",inti);intk++)
          {
           setValue("active_seq"  , activeSeq[inti][intk]);

           if (giveDays[inti][intk]>0)
           if (businessDate.compareTo(comm.nextNDate(getValue("issue_date")
                                                     ,(purchaseDays[inti][intk]
                                                     + giveDays[inti][intk]+1)))<0) continue;
           if (getMatchActiveSeq(inti,intk)!=0) continue;
           if (selectMktFstpParmseq1(inti, activeSeq[inti][intk])!=0) continue;

           if ((recordGroupNo[inti][intk].length()!=0)&&
               (getValue("record_no").length()==0)) continue;

           if ((recordGroupNo[inti][intk].length()!=0)&&
               (!recordGroupNo[inti][intk].equals(getValue("record_no")))) continue;

           if (idPSeqno.length()!=0)
              showLogMessage("I","","STEP 20201020 STEP 001 active_seq ["+ activeSeq[inti][intk] +"]");
           purchaseTypeSel = getValue("pact.purchase_type_sel");
           setValue("active_type" , getValue("pact.active_type"));
           if (checkMatchData(inti,intk)!=0) continue;

           if (recordGroupNo[inti][intk].length()==0)
              setValue("record_flag" , "X");
           else
               setValue("record_flag" , "Y");
           setValue("match_active_seq" ,  activeSeq[inti][intk]);
           break;
          }
     }
  else
     {
      if (idPSeqno.length()!=0)
          showLogMessage("I","","STEP 20210415 STEP 002 ]");

      setValue("active_type" , getValue("parm.active_type",inti));
      setValue("active_seq"  , activeSeq[inti][0]);

      if (getValue("parm.active_type",inti).equals("1"))
         setValue("bonus_type"   , getValue("parm.bonus_type",inti));
      else if (getValue("parm.active_type",inti).equals("2"))
         setValue("fund_code"    , getValue("parm.fund_code",inti));
      else if (getValue("parm.active_type",inti).equals("3"))
        {
         setValue("group_type"   , getValue("parm.group_type" , inti));
         setValue("prog_code"    , getValue("parm.prog_code" , inti));
         setValue("prog_s_date"  , getValue("parm.prog_s_date" , inti));
         setValue("prog_e_date"  , getValue("parm.prog_e_date" , inti));
         setValue("gift_no"      , getValue("parm.gift_no" ,inti));
        }
      else if (getValue("parm.active_type",inti).equals("4"))
         setValue("spec_gift_no" , getValue("parm.spec_gift_no",inti));

      purchaseAmtS[0] = getValueDouble("parm.purchase_amt_s1",inti);
      purchaseAmtS[1] = getValueDouble("parm.purchase_amt_s2",inti);
      purchaseAmtS[2] = getValueDouble("parm.purchase_amt_s3",inti);
      purchaseAmtS[3] = getValueDouble("parm.purchase_amt_s4",inti);
      purchaseAmtS[4] = getValueDouble("parm.purchase_amt_s5",inti);

      for (int intm=0;intm<5;intm++)
          {
           if (purchaseAmtS[intm]==0) break;
           amtCntSel = intm+1;
          }

      purchaseAmtE[0] = getValueDouble("parm.purchase_amt_e1",inti);
      purchaseAmtE[1] = getValueDouble("parm.purchase_amt_e2",inti);
      purchaseAmtE[2] = getValueDouble("parm.purchase_amt_e3",inti);
      purchaseAmtE[3] = getValueDouble("parm.purchase_amt_e4",inti);
      purchaseAmtS[4] = getValueDouble("parm.purchase_amt_s1",inti);

      feedbackAmt[0]   = getValueDouble("parm.feedback_amt_1",inti);
      feedbackAmt[1]   = getValueDouble("parm.feedback_amt_2",inti);
      feedbackAmt[2]   = getValueDouble("parm.feedback_amt_3",inti);
      feedbackAmt[3]   = getValueDouble("parm.feedback_amt_4",inti);
      feedbackAmt[4]   = getValueDouble("parm.feedback_amt_5",inti);

      feedbackLimit = getValueDouble("parm.feedback_limit",inti);
      purchaseTypeSel = getValue("parm.purchase_type_sel",inti);

      perAmtCond1 = getValue("parm.per_amt_cond",inti);
      perAmt1 = getValueDouble("parm.per_amt",inti);

      checkMatchData(inti,0);
     }
  return(0);
 }
// ************************************************************************
 int checkMatchData(int inti, int intk) throws Exception
 {
  if (idPSeqno.length()!=0)
     {
      showLogMessage("I","","STEP B4.0  purchase_type_sel :  ["+ purchaseTypeSel +"]");
      showLogMessage("I","","STEP B4.01 intk :  ["+ intk +"]");
     }

  if ((purchaseTypeSel.equals("1"))||
      (purchaseTypeSel.equals("2")))
     selectMktFstpPurcdtl(inti,intk,1);
  else if ((purchaseTypeSel.equals("3"))||
           (purchaseTypeSel.equals("4")))
     selectMktFstpPurcdtl(inti,intk,2);
  else
     selectMktFstpPurcdtl(inti,intk,3);

  setValue("dest_amt " ,"0");
  setValue("dest_cnt " ,"0");
  double calAmt = 0;
  double destAmt = 0;
  double sumFeedbackAmt = 0;
  setValue("dest_amt" , "0");
  setValue("dest_cnt" , "0");

  if (idPSeqno.length()!=0)
    showLogMessage("I","","STEP B4 purcCnt :  ["+ purcCnt +"]");

  for (int intj=0;intj<purcCnt;intj++)
      {
       if ((purchaseTypeSel.equals("1"))||
           (purchaseTypeSel.equals("2")))
          {
           setValue("dest_amt" , getValue("pdtl.dest_amt",intj));
           setValue("dest_cnt" , getValue("pdtl.dest_cnt",intj));
          }
       else if ((purchaseTypeSel.equals("3"))||
                (purchaseTypeSel.equals("4")))
          {
           setValueDouble("dest_amt" , getValueDouble("dest_amt") + getValueDouble("pdtl.dest_amt",intj));
           setValueDouble("dest_cnt" , getValueDouble("dest_cnt") + getValueDouble("pdtl.dest_cnt",intj));
          }
       else 
          {
           setValueDouble("dest_amt" , getValueDouble("dest_amt") + getValueDouble("pdtl.dest_amt",intj));
           setValueInt("dest_cnt"    , getValueInt("dest_cnt") + 1);
          }

       if ((purchaseTypeSel.equals("1"))||
           (purchaseTypeSel.equals("3"))||
           (purchaseTypeSel.equals("5")))
          destAmt = getValueDouble("pdtl.dest_amt",intj);
       else 
          destAmt = getValueDouble("pdtl.dest_cnt",intj);
    if (idPSeqno.length()!=0)
       {
      showLogMessage("I","","STEP 0005  0901  :  ["+ destAmt +"]");
      showLogMessage("I","","STEP 0006  0901  :  ["+ thresholdSel[inti][intk] +"]");
      showLogMessage("I","","STEP 0007  0901  :  ["+ amtCntSel +"]");
      }
 
       if (thresholdSel[inti][intk].equals("1"))
          { 
           for (int intm = 0; intm< amtCntSel; intm++)
               {
                if ((Math.abs(destAmt)>= purchaseAmtS[intm])&&
                    (Math.abs(destAmt)<= purchaseAmtE[intm]))
                   {
                    if (destAmt<0)
                       calAmt = feedbackAmt[intm]*-1;
                    else
                       calAmt = feedbackAmt[intm];
                     sumFeedbackAmt = sumFeedbackAmt + calAmt;
                   }
               }
          }
       else if (thresholdSel[inti][intk].equals("2"))
          {
           for (int intm = 0; intm< amtCntSel; intm++)
           {   
           if ((Math.abs(destAmt)>= purchaseAmtS[intm])&&
               (Math.abs(destAmt)<= purchaseAmtE[intm]))
              {
               if (destAmt<0)
                  calAmt = feedbackAmt[intm]*-1;
               else
                  calAmt = feedbackAmt[intm];

               sumFeedbackAmt = sumFeedbackAmt + calAmt;
               break;
              }
           }
          }
        }

    if (idPSeqno.length()!=0)
      showLogMessage("I","","STEP  0012  0901 :  ["+ sumFeedbackAmt +"]");

    if (feedbackLimit >0)
       if (sumFeedbackAmt> feedbackLimit)
          sumFeedbackAmt = feedbackLimit;

    if (idPSeqno.length()!=0)
      {
      showLogMessage("I","","STEP  0013  feedback_amt :  ["+ sumFeedbackAmt +"]");
      showLogMessage("I","","STEP  0014  active_type :  ["+ getValue("active_type") +"]");
      showLogMessage("I","","STEP  0015  purchase_flag :  ["+ getValue("purchase_flag") +"]");
     }

  if (sumFeedbackAmt==0)
     {
      initCarddtlData();
      setValue("purchase_flag","N");
      matchFlag =1;
      return(1);
     }

//    showLogMessage("I","","STEP  0017  0902 :  ["+ getValue("active_type") +"]");

  if (getValue("active_type").equals("1"))
     setValueDouble("beg_tran_bp" ,sumFeedbackAmt);
  else if (getValue("active_type").equals("2"))
     setValueDouble("beg_tran_amt" ,sumFeedbackAmt);
  else if (getValue("active_type").equals("3"))
     setValueDouble("tran_pt" ,sumFeedbackAmt);
  else
     setValueDouble("spec_gift_cnt" ,sumFeedbackAmt);

    setValue("purchase_flag","Y");

    if (idPSeqno.length()!=0)
       showLogMessage("I","","STEP  0020  0901 :  ["+ sumFeedbackAmt +"][" + getValue("card_no") +"]");

  return(0);
 }
// ************************************************************************
 int selectMktFstpParmseq1(int inti, String activeSeq) throws Exception
 {
  extendField = "pact.";
  daoTable  = "mkt_fstp_parmseq";
  whereStr  = "WHERE active_code     = ? "
            + "and   active_seq      = ? "
            ;

  setString(1 , getValue("parm.active_code",inti));
  setString(2 , activeSeq);

  selectTable();

  if (DEBUG1)
     {
      showLogMessage("I","","STEP C1    : ["+ getValue("pact.active_type") +"]");
      showLogMessage("I","","STEP C2    : ["+ getValue("match_active_seq") +"]");
     }
  setValue("active_type" , getValue("pact.active_type"));
  if (getValue("pact.active_type").equals("1"))
     setValue("bonus_type"   , getValue("pact.bonus_type"));
  else if (getValue("pact.active_type").equals("2"))
     setValue("fund_code"    , getValue("pact.fund_code"));
  else if (getValue("pact.active_type").equals("3"))
    {
     setValue("group_type"   , getValue("pact.group_type"));
     setValue("prog_code"    , getValue("pact.prog_code" ));
     setValue("prog_s_date"  , getValue("pact.prog_s_date" ));
     setValue("prog_e_date"  , getValue("pact.prog_e_date" ));
     setValue("gift_no"      , getValue("pact.gift_no" ));
    }
  else if (getValue("pact.active_type").equals("4"))
          setValue("spec_gift_no" , getValue("pact.spec_gift_no"));

  purchaseAmtS[0] = getValueDouble("pact.purchase_amt_s1");
  purchaseAmtS[1] = getValueDouble("pact.purchase_amt_s2");
  purchaseAmtS[2] = getValueDouble("pact.purchase_amt_s3");
  purchaseAmtS[3] = getValueDouble("pact.purchase_amt_s4");
  purchaseAmtS[4] = getValueDouble("pact.purchase_amt_s5");

  for (int intm=0;intm<5;intm++)
      {
       if (purchaseAmtS[intm]==0) break;
       amtCntSel = intm+1;
      }

  purchaseAmtE[0] = getValueDouble("pact.purchase_amt_e1");
  purchaseAmtE[1] = getValueDouble("pact.purchase_amt_e2");
  purchaseAmtE[2] = getValueDouble("pact.purchase_amt_e3");
  purchaseAmtE[3] = getValueDouble("pact.purchase_amt_e4");
  purchaseAmtS[4] = getValueDouble("pact.purchase_amt_s1");

  feedbackAmt[0]   = getValueDouble("pact.feedback_amt_1");
  feedbackAmt[1]   = getValueDouble("pact.feedback_amt_2");
  feedbackAmt[2]   = getValueDouble("pact.feedback_amt_3");
  feedbackAmt[3]   = getValueDouble("pact.feedback_amt_4");
  feedbackAmt[4]   = getValueDouble("pact.feedback_amt_5");

  feedbackLimit = getValueDouble("pact.feedback_limit");
  purchaseTypeSel = getValue("pact.purchase_type_sel");
  perAmtCond1 = getValue("pact.per_amt_cond"); 
  perAmt1 = getValueDouble("pact.per_amt"); 

  return(0);
 }
// ************************************************************************
 void selectMktFstpPurcdtl(int inti, int intk, int intm) throws Exception
 {
  extendField = "pdtl.";
  if ((intm==1)||(intm==2))
     selectSQL = "sum(dest_amt) as dest_amt, "
               + "sum(in_dest_amt) as in_dest_amt, "
               + "sum(decode(sign(dest_amt),1,1,0)) as dest_cnt ";
  else if (intm==3)
     selectSQL = "dest_amt ";

  daoTable  = "mkt_fstp_purcdtl";
  whereStr  = "where ori_major_card_no = ? " 
            + "and   active_code       = ? "
            + "and   acct_date        <= ? "
            + "and   (dest_amt         < 0 "
            + " or    dest_amt         > ?) "
            + "and   purchase_date between ? and ?  "
            + "and   active_seq       = ? "
            + "and   ori_major_card_no in ( "
            + "      select card_no "
            + "      from mkt_fstp_carddtl "
            + "      where 1 = 1 "
            ;

  setString(1 , getValue("card_no"));
  setString(2 , getValue("active_code"));
  setString(3 , businessDate);
  if (perAmtCond1.equals("Y"))
     setDouble(4 , perAmt1);
  else
     setDouble(4 , 0); 
  setString(5 , getValue("issue_date"));
  setString(6 , comm.nextNDate(getValue("issue_date"),(purchaseDays[inti][intk]+1)));
  if (getValue("parm.mcht_seq_flag",inti).equals("N"))
     setString(7 , "00");
  else
     setString(7 , getValue("active_seq")); 

  if (idPSeqno.length()!=0)
     {
       whereStr  = whereStr 
                 + "and  id_p_seqno = ? " 
                 + "and  (error_code    = '00' "
                 + " or   error_code    like 'B%')) "
                 ;
       setString(8 , idPSeqno);
      }
   else
      {
       whereStr  = whereStr 
                 + "and   proc_flag     = 'N' "
                 + "and   error_code    = '00') ";
      }

  if (intm==1)
     {
       whereStr  = whereStr 
                 + "group by ori_major_card_no";
     }
  else if (intm==2)
     {
       whereStr  = whereStr 
                 + "group by ori_major_card_no,purchase_date";
     }

  purcCnt = selectTable();

  return;
 }
// ************************************************************************

}  // End of class FetchSample
