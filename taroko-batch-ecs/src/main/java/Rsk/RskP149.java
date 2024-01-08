/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  每月月底滾檔
 *  2023-0904 V1.00.02   JH          個人年度消費
 *  2023-0831 V1.00.01     JH        check run date
 *  112-07-07  V1.00.00  Alex        initial                                  *
 *****************************************************************************/
package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class RskP149 extends BaseBatch {
private final String progname = "產生分行商務卡期中覆審清單 2023-0904 V1.00.02";
//CommFunction comm = new CommFunction();
//CommCrd comc = new CommCrd();
CommString commString = new CommString();
CommDate commDate = new CommDate();

String regBankNo = "";
String corpPSeqno = "";
String cardNo = "";
String idPSeqno = "";
String cardSince = "";
String acnoPSeqno = "";
double idLimit = 0.0;
double cardLimit = 0.0;
String corpTel = "";
double thisYearConsume = 0.0;
double lastYearConsume = 0.0;
String thisYear = "";
String lastYear = "";
String reviewMonth = "";
int procCnt = 0;

public static void main(String[] args) {
   RskP149 proc = new RskP149();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP149 [business_date]");
      okExit(0);
   }

   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }
   else {
      hBusiDate = sysDate;
   }
   dbConnect();
   //-check 月底-------------
   String lsDD=commString.right(hBusiDate,2);
   if (!eq(lsDD,"01")) {
      printf("--每月01日處理上月資料, busiDate[%s]", hBusiDate);
      okExit(0);
   }

   hBusiDate = commDate.dateAdd(sysDate,0,0,-1);
   thisYear = hBusiDate.substring(0, 4);
   lastYear = commDate.dateAdd(hBusiDate, -1, 0, 0).substring(0, 4);
   printf("--處理日期: procDate[%s], thisYear[%s], lastYear[%s]--"
    , hBusiDate, thisYear, lastYear);

   procData();

   sqlCommit();
   endProgram();
}

void procData() throws Exception {

   sqlCmd = " select reg_bank_no , corp_p_seqno , card_no , id_p_seqno"
             +" , acno_p_seqno "
             +", ori_issue_date as card_since "
             + " from crd_card "
             +" where current_code ='0' and acct_type ='03' "
             + " and group_code not in ('1500','3750','3751','3760','3790') "
             + " and corp_p_seqno not in "
             + " (select corp_p_seqno from crd_corp where chi_name = '合作金庫' ) "
   ;

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      initData();
      regBankNo = colSs("reg_bank_no");
      corpPSeqno = colSs("corp_p_seqno");
      cardNo = colSs("card_no");
      idPSeqno = colSs("id_p_seqno");
      acnoPSeqno = colSs("acno_p_seqno");
      cardSince = colSs("card_since");
      if (empty(cardSince)==false) {
         reviewMonth = commDate.dateAdd(cardSince, 0, 6, 0).substring(0, 6);
      }
      //--取得本卡額度、開戶日
      selectActAcno1();

      //--取得公司總額度
      selectActAcno2();

      //--取得公司電話
      selectCrdCorp();

      //--取得本年度消費
      thisYearConsume = selectAnalSub(thisYear + "%");
      //--取得上年度消費
      lastYearConsume = selectAnalSub(lastYear + "%");

      if (checkReviewList()) {
         updateReviewList();
      } else {
         insertReviewList();
      }

      procCnt++;

      if (procCnt % 5000 == 0)
         sqlCommit();

   }

   sqlCommit();
   closeCursor();
}

void insertReviewList() throws Exception {
   daoTable = "rsk_review_corp_list";
   setValue("reg_bank_no", regBankNo);
   setValue("corp_p_seqno", corpPSeqno);
   setValue("card_no", cardNo);
   setValue("id_p_seqno", idPSeqno);
   setValue("review_month", reviewMonth);
   setValue("card_since", cardSince);
   setValueDouble("id_limit", idLimit);
   setValueDouble("card_limit", cardLimit);
   setValue("corp_tel", corpTel);
   setValueDouble("last_year_consume", lastYearConsume);
   setValueDouble("this_year_consume", thisYearConsume);
   setValue("crt_date", sysDate);
   setValue("crt_time", sysTime);
   setValue("mod_user", "ecs");
   setValue("mod_time", sysDate + sysTime);
   setValue("mod_pgm", javaProgram);
   setValueInt("mod_seqno", 1);
   insertTable();
}

void updateReviewList() throws Exception {

   int updateCnt = 0;
   daoTable = "rsk_review_corp_list";
   updateSQL = "id_limit = ? , card_limit = ? , corp_tel = ? , last_year_consume = ? , this_year_consume = ? "
                +", mod_time =sysdate, mod_pgm ='RskP149' ";
   whereStr = " where card_no = ? ";

   setDouble(1, idLimit);
   setDouble(2, cardLimit);
   setString(3, corpTel);
   setDouble(4, lastYearConsume);
   setDouble(5, thisYearConsume);
   setString(6, cardNo);
   updateCnt = updateTable();
   if (updateCnt == 0) {
      printf("update_rsk_review_corp_list error, kk=[%s]", cardNo);
      return;
   }
}

int tiCorpList = -1;

boolean checkReviewList() throws Exception {
   if (tiCorpList <= 0) {
      sqlCmd = " select count(*) as db_cnt from rsk_review_corp_list where card_no = ? ";
      tiCorpList = ppStmtCrt("ti-S-rskReviewCorpList", "");
   }

   setString(1, cardNo);

   sqlSelect(tiCorpList);
   if (sqlNrow <= 0) {
      return false;
   }

   if (colNum("db_cnt") > 0)
      return true;

   return false;
}

int tiAnalSub = -1;

double selectAnalSub(String searchMonth) throws Exception {
   //-個人年度消費-
   if (tiAnalSub <= 0) {
//      sqlCmd = " select sum(his_purchase_amt+his_cash_amt) as sub_amt "
//                +" from act_anal_sub "
//                +" where p_seqno in (select acno_p_seqno "
//                + " from act_acno where corp_p_seqno = ?) and acct_month like ? ";
      sqlCmd = " select sum(his_purchase_amt+his_cash_amt) as sub_amt "
                +" from act_anal_sub "
                +" where p_seqno =? and acct_month like ? ";
      tiAnalSub = ppStmtCrt("ti-S-actAnalSub", "");
   }

//   setString(1, corpPSeqno);
   ppp(1, acnoPSeqno);
   ppp(2, searchMonth);

   sqlSelect(tiAnalSub);
   if (sqlNrow <= 0) {
      return 0;
   }

   return colNum("sub_amt");
}

int tiCorp = -1;

void selectCrdCorp() throws Exception {
   if (tiCorp <= 0) {
      sqlCmd = "select corp_tel_zone1||corp_tel_no1||corp_tel_ext1 as corp_tel from crd_corp where corp_p_seqno = ? ";
      tiCorp = ppStmtCrt("ti-S-crdCorp", "");
   }

   setString(1, corpPSeqno);

   sqlSelect(tiCorp);
   if (sqlNrow <= 0) {
      corpTel = "";
      return;
   }

   corpTel = colSs("corp_tel");

}

int tiAcno1 = -1;

void selectActAcno1() throws Exception {
   if (tiAcno1 <= 0) {
      sqlCmd = "select line_of_credit_amt from act_acno where acno_p_seqno = ? ";
      tiAcno1 = ppStmtCrt("ti-S-actAcno1", "");
   }

   setString(1, acnoPSeqno);

   sqlSelect(tiAcno1);
   if (sqlNrow <= 0) {
      cardLimit = 0;
      return;
   }

   cardLimit = colNum("line_of_credit_amt");
}

int tiAcno2 = -1;

void selectActAcno2() throws Exception {
   if (tiAcno2 <= 0) {
      sqlCmd = "select line_of_credit_amt from act_acno where acno_flag ='2' and corp_p_seqno = ? ";
      tiAcno2 = ppStmtCrt("ti-S-actAcno2", "");
   }

   setString(1, corpPSeqno);

   sqlSelect(tiAcno2);
   if (sqlNrow <= 0) {
      idLimit = 0;
      return;
   }

   idLimit = colNum("line_of_credit_amt");
}

void initData() {
   regBankNo = "";
   corpPSeqno = "";
   cardNo = "";
   idPSeqno = "";
   cardSince = "";
   acnoPSeqno = "";
}

}
