package Mkt;
/**
 * 2023-0823 V1.00.00   JH    initial
 * */

import com.DataSet;
import java.text.Normalizer;

@SuppressWarnings({"unchecked", "deprecation"})
public class MktR400 extends com.BaseBatch {
private final String PROGNAME = "聯名機構員工推廣回饋處理  2023-0823 V1.00.00";
HH hh=new HH();
//-----------
class HH {
   String card_no="";
   String group_code="";
   String id_pseqno="";
   boolean supFlag=false;
   String issue_date="";
   String ori_card_no="";
   String stmt_cycle="";
   String acct_month="";
   String bill_date1="";
   String bill_date2="";
   //-----------
   String new_account="N";
   double consume_amt=0;
   double year_total_amt=0;
   int    total_cnt=0;
   String match_flag="N";
   int    online_cnt=0;
   double feedback_amt=0;
   String chi_name="";
   String major_idPseqno="";
   String member_id="";
   String promote_dept="";
   String promote_emp_no="";
   String introduce_emp_no="";
   String introduce_id="";
   String proj_no="";

   void initData() {
      card_no="";
      group_code="";
      id_pseqno="";
      supFlag=false;
      issue_date="";
      ori_card_no="";
      stmt_cycle="";
      acct_month="";
      bill_date1="";
      bill_date2="";
      //-----------
      proj_no="";
      new_account="N";
      consume_amt=0;
      year_total_amt=0;
      total_cnt=0;
      match_flag="N";
      online_cnt=0;
      feedback_amt=0;
      chi_name="";
      major_idPseqno="";
      member_id="";
      promote_dept="";
      promote_emp_no="";
      introduce_emp_no="";
      introduce_id="";
   }
}
//--
String is_statMonth="";
//=*****************************************************************************
public static void main(String[] args) {
   MktR400 proc = new MktR400();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 1) {
      printf("Usage : MktR400 [busi_date]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
         hBusiDate= Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
      }
   }

   dbConnect();
   is_statMonth =commString.left(hBusiDate,6);
   printf("-- static_month=[%s]", is_statMonth);
   String lsDD=commString.right(hBusiDate,2);
   if (!eq(lsDD,"01")) {
      printf(" 作業程式須在每月01日執行");
      okExit(0);
   }

   loadPtr_workday();
   loadMkt_jointly_parm();
   for (int ii = 0; ii <dsParm.rowCount() ; ii++) {
      dsParm.listCurr(ii);
      selectCrd_card();
   }

   sqlCommit();

   dsParm.dataClear();
   dsParm =null;

   endProgram();
}
//=======================
//'BL','CA','IT','ID','AO','OT'
String isCond_acctCode="";
void selectCrd_card() throws Exception {
   //消費條件：核卡後：近： 60 天內刷卡消費
   int li_currPreDay =dsParm.colInt("curr_pre_day");
   String ls_issueDate =commDate.dateAdd(hBusiDate,0,0,0 - li_currPreDay);
//   String ls_issueDate =commDate.dateAdd(hBusiDate,-1,0,0 - li_currPreDay);
   String ls_prrojCode =dsParm.colSs("proj_code");
   isCond_acctCode="";
   if (dsParm.colEq("consume_bl","Y")) isCond_acctCode +="-BL";
   if (dsParm.colEq("consume_it","Y")) isCond_acctCode +="-IT";
   if (dsParm.colEq("consume_ca","Y")) isCond_acctCode +="-CA";
   if (dsParm.colEq("consume_id","Y")) isCond_acctCode +="-ID";
   if (dsParm.colEq("consume_ao","Y")) isCond_acctCode +="-AO";
   if (dsParm.colEq("consume_ot","Y")) isCond_acctCode +="-OT";

   sqlCmd ="select A.card_no, A.id_p_seqno, A.major_id_p_seqno "+
            ", A.card_type, A.group_code "+
            ", A.sup_flag "+
            ", A.ori_card_no, A.ori_issue_date as issue_date "+
            ", A.stmt_cycle "+
            ", A.member_id, A.promote_dept, A.promote_emp_no "+
            ", A.introduce_emp_no, A.introduce_id "+
            " from crd_card A"+
            " where A.current_code ='0' "+
            " and A.group_code in ( "+
            " select group_code from ptr_group_code "+
            " where member_flag='Y' and member_corp_no <>'' "+
            " ) "+
            " and ori_issue_date >=? ";
   ppp(1, ls_issueDate);
   //-帳戶類別-
   if (dsParm.colEq("acct_type_flag","1")) {
      sqlCmd +=" and acct_type in ( select data_code from MKT_JOINTLY_PARM_DETL "+
                " where proj_code =? and data_type ='01' )";
      ppp(ls_prrojCode);
   }
   //-卡片種類-
   if (dsParm.colEq("card_type_flag","1")) {
      sqlCmd +=" and card_type in ( select data_code from MKT_JOINTLY_PARM_DETL "+
                " where proj_code =? and data_type ='03' )";
      ppp(ls_prrojCode);
   }
   //-團體代號 :-
   if (dsParm.colEq("group_code_flag","1")) {
      sqlCmd +=" and group_code in ( select data_code from MKT_JOINTLY_PARM_DETL "+
                " where proj_code =? and data_type ='02' )";
      ppp(ls_prrojCode);
   }
   //-TTT--
//   sqlCmd +=" "+commSqlStr.rownum(1000);
   //-------------
//   debug =true;
//   dddSql();
//   debug =false;

   openCursor();
   int ll_cnt=0;
   while (fetchTable()) {
      hh.initData();
      totalCnt++;
      ll_cnt++;
      //-------------
      hh.card_no =colSs("card_no");
      hh.group_code =colSs("group_code");
      hh.id_pseqno =colSs("id_p_seqno");
      hh.major_idPseqno =colSs("major_id_p_seqno");
      hh.supFlag =colEq("sup_flag","1");
      hh.issue_date =colSs("issue_date");
      hh.ori_card_no =colSs("ori_card_no");
      hh.stmt_cycle =colSs("stmt_cycle");
      //--------
      hh.proj_no =ls_prrojCode;
      hh.member_id =colSs("member_id");
      hh.promote_dept =colSs("promote_dept");
      hh.promote_emp_no =colSs("promote_emp_no");
      hh.introduce_emp_no =colSs("introduce_emp_no");
      hh.introduce_id =colSs("introduce_id");
      //-正附卡---
      if (hh.supFlag) {
         if (dsParm.colEq("debut_sup_flag_1","Y")==false) continue;
      }
      else {
         if (dsParm.colEq("debut_sup_flag_0","Y")==false) continue;
      }
      //-卡片已回饋---
      int liRC =selectMkt_member_reward();
      if (liRC >0) continue;
      //-check首年-----
      liRC =checkFirstCard();
      if (liRC ==0) hh.new_account="Y";
      else hh.new_account="N";
      //-消費金額-----------
      hh.bill_date1 =hh.issue_date;
      int liDD =dsParm.colInt("curr_pre_day");
      hh.bill_date2 =commDate.dateAdd(hh.issue_date,0,0,liDD);

      selectBil_bill_1();
      selectBil_bill_2();
      //-累積消費金額：達  1 元(含)以上-
      if (hh.year_total_amt >= dsParm.colNum("curr_amt")) hh.match_flag ="Y";
      //-或 累積消費筆數達 1 筆(含)以上--
      if (!eq(hh.match_flag,"Y") && dsParm.colEq("curr_tot_cond","Y")) {
         if (hh.total_cnt > dsParm.colInt("curr_tot_cnt")) {
            hh.match_flag ="Y";
         }
      }
      //-或 自動加值次數達 0 筆(含)以上--
      if (!eq(hh.match_flag,"Y") && dsParm.colEq("online_cond","Y")) {
         selectTsc_card();
         if (hh.online_cnt > dsParm.colInt("online_cnt")) {
            hh.match_flag ="Y";
         }
      }
      //-回饋方式： 回饋金額比例  0% 回饋金額：200 元--
      if (eq(hh.match_flag,"Y")) {
         if (dsParm.colEq("feedback_type","1")) {
            hh.feedback_amt = hh.year_total_amt * (dsParm.colNum("feedback_rate") / 100);
         }
         else {
            hh.feedback_amt =dsParm.colNum("feedback_amt");
         }
         hh.feedback_amt =commString.numScale(hh.feedback_amt,0);
      }

      //--
      insertMkt_member_reward();

   }
   closeCursor();
   printf(" --| proj_code[%s], card_cnt[%s]", ls_prrojCode, ll_cnt);
}
//------------------
int tiBill=-1;
void selectBil_bill_1() throws Exception {
   //-5.2.	CONSUME_AMT=當月消費金額-
   getLastAcctMonth();
   if (empty(hh.acct_month)) return;

   if (tiBill <=0) {
      sqlCmd = " select A.acct_code "
                + ", sum(case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end) as dest_amt "
                + " from bil_bill A"
                + " where 1=1 "
                +" and A.card_no = ?"
                +" and A.acct_month =?"
                +" and A.acct_code in ('BL','CA','IT','ID','AO','OT')"
                +" group by A.acct_code "
      ;
      tiBill =ppStmtCrt("ti-bill-S","");
   }
   ppp(1, hh.card_no);
   ppp(hh.acct_month);

   sqlSelect(tiBill);
   if (sqlNrow <=0) return;
   int ll_nRow =sqlNrow;
   hh.consume_amt=0;
   for (int ll = 0; ll <ll_nRow ; ll++) {
      String ls_acctCode =colSs(ll,"acct_code");
      double lm_destAmt =colNum(ll,"dest_amt");
      if (commString.ssIn(ls_acctCode,isCond_acctCode)) {
         hh.consume_amt +=lm_destAmt;
      }
   }
}
//--------------------------
int tiBill2=-1;
void selectBil_bill_2() throws Exception {
   //-YEAR_TOTAL_AMT: 當月消費金額+之前的消費金額累加-
   if (tiBill2 <=0) {
      sqlCmd = " select "
                + " A.acct_code "
                + ", sum(case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end) as dest_amt "
                + ", sum(decode(A.sign_flag,'-',0,1)) as dest_cnt "
                + " from bil_bill A"
                + " where 1=1 "
                +" and A.card_no = ?"
                //-消費資料 消費期間--
                +" and A.post_date >= ? and A.post_date < ?"
                +" and A.acct_code in ('BL','CA','IT','ID','AO','OT')"
                +" group by A.acct_code "
      ;
      tiBill2 =ppStmtCrt("ti-bill2-S","");
   }
//   String ls_postDate1 =hh.issue_date;
//   int liDD =dsParm.colInt("curr_pre_day");
//   String ls_postDate2 =commDate.dateAdd(hh.issue_date,0,0,liDD);

   ppp(1, hh.card_no);
   ppp(hh.bill_date1);
   ppp(hh.bill_date2);

   sqlSelect(tiBill2);
   if (sqlNrow <=0) return;
   int ll_nRow =sqlNrow;
   hh.year_total_amt=0;
   for (int ll = 0; ll <ll_nRow ; ll++) {
      String ls_acctCode =colSs(ll,"acct_code");
      double lm_destAmt =colNum(ll,"dest_amt");
      int ll_destCnt =colInt(ll,"dest_cnt");

      if (commString.ssIn(ls_acctCode,isCond_acctCode)) {
         hh.year_total_amt += lm_destAmt;
         hh.total_cnt +=ll_destCnt;
      }
   }
}
//------------
int tiOnline=-1;
void selectTsc_card() throws Exception {
   if (tiOnline <=0) {
      sqlCmd ="SELECT 'TSC' AS kk_table, count(*) kk_cnt " +
               " FROM tsc_ecti_log " +
               " WHERE TSC_CARD_NO IN (SELECT tsc_card_no FROM tsc_card WHERE card_no =?) " +
               " AND   crt_date BETWEEN ? AND ? " +
               " UNION " +
               " SELECT 'IPS' AS kk_table, count(*) kk_cnt " +
               " FROM ips_cgec_all " +
               " WHERE IPS_CARD_NO IN (SELECT IPS_CARD_NO FROM ips_card WHERE CARD_NO =?) " +
               " AND   txn_date BETWEEN ? AND ? " +
               " UNION " +
               " SELECT 'ICH' AS kk_table, count(*) kk_cnt " +
               " FROM ich_a07b_add " +
               " WHERE ICH_CARD_NO IN (SELECT ICH_CARD_NO FROM ich_card WHERE CARD_NO =?) " +
               " AND   txn_date BETWEEN ? AND ?"
       ;
      tiOnline =ppStmtCrt("tiOnline","");
   }
   //-TSC-
   ppp(1, hh.card_no);
   ppp(hh.bill_date1);
   ppp(hh.bill_date2);
   //-IPS-
   ppp(hh.card_no);
   ppp(hh.bill_date1);
   ppp(hh.bill_date2);
   //-ICH-
   ppp(hh.card_no);
   ppp(hh.bill_date1);
   ppp(hh.bill_date2);

   sqlSelect(tiOnline);
   if (sqlNrow <=0) return;
   for (int ll = 0; ll <sqlNrow ; ll++) {
      hh.online_cnt +=colInt(ll,"kk_cnt");
   }
}
//------------------
int tiFcard=-1;
int checkFirstCard() throws Exception {
   if (tiFcard <=0) {
      sqlCmd = "select count(*) first_cnt "
                + " from crd_card "
                + " where id_p_seqno = ?"
                + " and ori_card_no <>?"
                + " and ori_issue_date >=? and ori_issue_date < ?"
      ;
      tiFcard =ppStmtCrt("ti-fcard","");
   }
   //-全新卡:從未持有本行任一信用卡-
   String ls_yearType =dsParm.colSs("debut_year_flag");
   if (eq(ls_yearType,"2")) {
      ppp(1, hh.id_pseqno);
      ppp(hh.ori_card_no);
      ppp("");
      ppp(hh.issue_date);
      sqlSelect(tiFcard);
      if (sqlNrow >0 && colInt("first_cnt")>0) return 1;
      return 0;
   }
   //-新申辦卡:核卡日前 6 個月內未持有本行任一信用卡--
   int liMM =dsParm.colInt("debut_month1");
   String ls_issueDate1 =commDate.dateAdd(hh.issue_date,0,0 - liMM, 0);
   ppp(1, hh.id_pseqno);
   ppp(hh.ori_card_no);
   ppp(ls_issueDate1);
   ppp(hh.issue_date);
   sqlSelect(tiFcard);
   if (sqlNrow >0 && colInt("first_cnt")>0) return 1;
   return 0;
}
//---------------------
int tiRewd=-1;
int selectMkt_member_reward() throws Exception {
   if (tiRewd <=0) {
      sqlCmd ="select count(*) rewd_cnt"
       +" from mkt_member_reward "
       +" where 1=1"
       +" and card_no =? and group_code =? "
       +" and match_flag ='Y' ";
      tiRewd =ppStmtCrt("ti-rewd","");
   }

   ppp(1, hh.card_no);
   ppp(hh.group_code);
   sqlSelect(tiRewd);
   if (sqlNrow <=0) return 0;

   return colInt("rewd_cnt");
}
//===================
com.Parm2sql ttArewd=null;
void insertMkt_member_reward() throws Exception {
   //-ttInsert--------------------------
   if (ttArewd ==null) {
      ttArewd =new com.Parm2sql();
      ttArewd.insert("mkt_member_reward");
   }
   ttArewd.aaa("static_month"              , is_statMonth);   //---
   ttArewd.aaa("card_no"                   , hh.card_no);   //---
   ttArewd.aaa("group_code"                , hh.group_code);   //---
   ttArewd.aaa("id_p_seqno"                , hh.id_pseqno);   //---
   ttArewd.aaa("issue_date"                , hh.issue_date);   //---
   ttArewd.aaa("chi_name"                  , hh.chi_name);   //---
   ttArewd.aaa("new_account"               , hh.new_account);   //---
   ttArewd.aaa("match_flag"                , hh.match_flag);   //---
   ttArewd.aaa("major_id_p_seqno"          , hh.major_idPseqno);   //---
   ttArewd.aaa("member_id"                 , hh.member_id);   //---
   ttArewd.aaa("promote_dept"              , hh.promote_dept);   //---
   ttArewd.aaa("promote_emp_no"            , hh.promote_emp_no);   //---
   ttArewd.aaa("introduce_emp_no"          , hh.introduce_emp_no);   //---
   ttArewd.aaa("introduce_id"              , hh.introduce_id);   //---
   ttArewd.aaa("proj_no"                   , hh.proj_no);   //---
   ttArewd.aaa("feedback__amt"             , hh.feedback_amt);   //---
//   ttArewd.aaa("feedback_date"             , hh.feedback_date);   //---
   ttArewd.aaa("consume_amt"               , hh.consume_amt);   //---
   ttArewd.aaa("year_total_amt"            , hh.year_total_amt);   //---
//   ttArewd.aaa("post_date"                 , hh.post_date);   //---
//   ttArewd.aaa("post_seqno"                , hh.post_seqno);   //---
   ttArewd.aaa("crt_date"                  , sysDate);   //---
   ttArewd.aaa("mod_user"                  , hModUser);   //---
   ttArewd.aaaDtime("mod_time");   //---
   ttArewd.aaa("mod_pgm"                   , hModPgm);   //---

   if (ttArewd.ti <=0) {
      ttArewd.ti =ppStmtCrt("ttArewd", ttArewd.getSql());
   }

//   debug =true;
//   dddSql(ttArewd.ti, ttArewd.getConvParm(false));
//   debug =false;

   sqlExec(ttArewd.ti, ttArewd.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert mkt_member_reward error");
      errExit(1);
   }
}
//--------------------------------
com.DataSet dsParm=new DataSet();
void loadMkt_jointly_parm() throws Exception {
   dsParm.dataClear();
   sqlCmd ="select * "+
            " from mkt_jointly_parm "+
            " where apr_flag ='Y' "+
            " and ? BETWEEN proj_date_s AND decode(PROJ_DATE_E,'','20991231',PROJ_DATE_E) "+
            " and debut_sup_flag_0 ='Y' "
   ;
   sqlQuery(dsParm,"",new Object[]{hBusiDate});

   printf(" -- mkt_jointly_parm 推廣參數筆數[%s]", dsParm.rowCount());
}
//-----------------
com.DataSet dsWday=new DataSet();
void loadPtr_workday() throws Exception {
   sqlCmd ="SELECT stmt_cycle, THIS_ACCT_MONTH , LAST_ACCT_MONTH " +
            "FROM ptr_workday"
    ;

   sqlQuery(dsWday,"",null);
}
//------
void getLastAcctMonth() {
   for (int ll = 0; ll <dsWday.rowCount() ; ll++) {
      dsWday.listCurr(ll);
      if (dsWday.colEq("stmt_cycle",hh.stmt_cycle)) {
         hh.acct_month =dsWday.colSs("last_acct_month");
         break;
      }
   }
}

//-engPgm-
}
