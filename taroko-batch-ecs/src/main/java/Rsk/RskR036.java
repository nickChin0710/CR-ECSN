/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  月底出表
 *  112-07-07  V1.00.00  Alex        initial                                  *
 *  112-07-17  V1.00.01  Alex        add FTP                                  * 
 *  112/07-24  V1.00.02  Alex        區分一般商務卡和政府採購卡和公務車加油卡
 *  2023-0831  V1.00.03  JH          check run date
 *  2023-0904  V1.00.04  JH          開戶日=ori_issue_date
 *****************************************************************************/
package Rsk;

import java.text.DecimalFormat;

import com.*;

public class RskR036 extends BaseBatch {
private final String progname = "產生商務卡當月消費狀況明細表 on-demand 2023-0904 V1.00.04";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommString commString = new CommString();
CommDate commDate = new CommDate();
CommFTP commFTP = null;
CommRoutine comr = null;

String fileName = "";
private int iiFileNum = 0;
String busiTwDate = "";
String newLine = "\r\n";
String reportName = "";
int detailCnt = 0;
int pageNo = 0;

String thisYear = "";
String lastYear = "";

String lastBranch = "";
String branch = "";
String branchFullName = "";
String lastCorp = "";
String corpNo = "";
String corpPSeqno = "";
String cardNo = "";
String cardChiName = "";
String currentCode = "";
String ttCurrnetCode = "";
String oppoReason = "";
String newEndDate = "";
String newEndDateMmYy = "";
double lineOfCreditAmt = 0.0;
int intRateMcode = 0;
String lastAcnoPSeqno = "";
String acnoPSeqno = "";
double monthConsume = 0.0;
double pyajAmt = 0.0;
double acctJrnlBal = 0.0;
double corpLineOfCreditAmt = 0.0;
double minPayBal = 0.0;
String corpName = "";
String corpTelNo = "";
String corpAcnoPSeqno = "";
double totDue = 0.0;
double totLimitAmt = 0.0;
String overSixtyFlag = "";
String cardLastSix = "";
String cardSince = "";
//String twCardSince = "";
double ttlAmtBal = 0.0;
double corpTtlAmtBal = 0.0;
double lastPct = 0.0;
int overSixtyCnt = 0;
String overDay = "";
//----------
String is_busiYYMM="";

public static void main(String[] args) {
   RskR036 proc = new RskR036();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP036 [business_date]");
      errExit(1);
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
      printf("--每月01日產出上月報表, busiDate[%s]", hBusiDate);
      okExit(0);
   }
   hBusiDate = commDate.dateAdd(hBusiDate,0,0,-1);
   busiTwDate = commDate.toTwDate(hBusiDate);
   thisYear = hBusiDate.substring(0, 4);
   lastYear = commDate.dateAdd(hBusiDate, -1, 0, 0).substring(0, 4);
   printf(" --處理日期: procDate[%s], busiTwDate[%s], thisYear[%s], lastYear[%s]--"
    ,hBusiDate, busiTwDate, thisYear, lastYear);

   is_busiYYMM =commString.left(hBusiDate,6);

   fileName = "RCRM36.1.TXT";
   checkOpen();

   loadGenBrn();
   //--商務卡當月消費狀況明細表
   procData1();

   //--政府網路採購卡當月消費狀況明細表
   procData2();

   //--本行公務車加油商務卡當月消費狀況明細表
   procData3();

   closeOutputText(iiFileNum);

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   renameFile();

   endProgram();
}

void procFTP() throws Exception {
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "BREPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;
   showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
   int errCode = commFTP.ftplogName("BREPORT", "mput " + fileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 " + fileName + ".txt" + " 資料" + " errcode:" + errCode);
      insertEcsNotifyLog(fileName);
   }
}

//=====================
public int insertEcsNotifyLog(String fileName) throws Exception {
   setValue("crt_date", sysDate);
   setValue("crt_time", sysTime);
   setValue("unit_code", comr.getObjectOwner("3", javaProgram));
   setValue("obj_type", "3");
   setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
   setValue("notify_name", "媒體檔名:" + fileName);
   setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
   setValue("notify_desc2", "");
   setValue("trans_seqno", commFTP.hEflgTransSeqno);
   setValue("mod_time", sysDate + sysTime);
   setValue("mod_pgm", javaProgram);
   daoTable = "ecs_notify_log";

   insertTable();

   return (0);
}

//=====================
void renameFile() throws Exception {
   String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName + "_" + sysDate);

   if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
      showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr4 + "]");

}


void procData1() throws Exception {

   reportName = "商務卡當月消費狀況明細表";

   sqlCmd = " select A.reg_bank_no , A.corp_no , A.corp_p_seqno , A.card_no "
             +", A.acno_p_seqno "
             +", uf_card_name(A.card_no) as chi_name "
             +", A.ori_issue_date as card_since, A.issue_date "  //activate_date "
             +", decode(A.current_code,'0','*','') as tt_current_code "
             +", A.oppost_reason , A.new_end_date "
             + ", A.current_code "
             +", B.line_of_credit_amt , B.int_rate_mcode "
             + ", C.acct_jrnl_bal , C.ttl_amt_bal , C.min_pay_bal "
             + " from crd_card A join act_acno B on A.acno_p_seqno = B.acno_p_seqno "
             +"   join act_acct C on A.acno_p_seqno = C.p_seqno "
             + " where 1=1 and A.corp_p_seqno <> '' "
             + " and A.reg_bank_no <> '' "
             + " and A.corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1) "
             + " and (A.current_code ='0' or C.acct_jrnl_bal > 0) "
             + " and A.group_code not in ('3713','1599') "
             + " order by A.reg_bank_no Asc , A.corp_p_seqno Asc , A.current_code "
//			   + " fetch first 100 rows only "
   ;

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      initData();
      branch = colSs("reg_bank_no");
      corpNo = colSs("corp_no");
      corpPSeqno = colSs("corp_p_seqno");
      cardNo = colSs("card_no");
      acnoPSeqno = colSs("acno_p_seqno");
      cardChiName = colSs("chi_name");
      cardSince = colSs("activate_date");
      currentCode = colSs("current_code");
      ttCurrnetCode = colSs("tt_current_code");
      oppoReason = colSs("oppost_reason");
      newEndDate = colSs("new_end_date");
      lineOfCreditAmt = colNum("line_of_credit_amt");
      intRateMcode = colInt("int_rate_mcode");
      acctJrnlBal = colNum("acct_jrnl_bal");
      ttlAmtBal = colNum("ttl_amt_bal");
      minPayBal = colNum("min_pay_bal");
      if (empty(cardSince)) {
         cardSince =colSs("issue_date");
      }

      if (empty(lastCorp))
         lastCorp = corpNo;

      if (lastCorp.equals(corpNo) == false) {
         if (empty(lastBranch) || eq(lastBranch,branch)) {
            lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
            printCorpTotal();
            lastCorp = corpNo;
            corpTtlAmtBal = 0;
            lastPct = 0;
         }

      }


      if (empty(lastBranch) || eq(lastBranch,branch) == false) {
         if (empty(lastBranch) == false) {
            lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
            printCorpTotal();
            lastCorp = corpNo;
            corpTtlAmtBal = 0;
            lastPct = 0;

            printBranchFooter();
            printFooter();
         }
         pageNo = 1;
         printHeader(pageNo, 1);
         lastBranch = branch;
      }

      if (lastAcnoPSeqno.equals(acnoPSeqno))
         continue;

      lastAcnoPSeqno = acnoPSeqno;

      //--月消費
      selectBilBill();
//      selectCycPyaj();
      //--現欠
//			selectActAcct();
      //--公司戶額度
      selectActAcno();
      //--公司基本資料
      selectCorpData();

      //--分行名稱
      getBranchName();

      totDue = selectAnalSub(thisYear + "%");
      totLimitAmt = selectAnalSub(lastYear + "%");
      if ((totDue - totLimitAmt) > 600000)
         overSixtyFlag = "V";
      else
         overSixtyFlag = "";

      overDay = getOverDay();

      monthConsume = monthConsume - pyajAmt;

      if ("Y".equals(overSixtyFlag))
         overSixtyCnt++;

      cardLastSix = cardNo.substring(cardNo.length() - 6, cardNo.length());

//      if (twCardSince.length() == 6)
//         twCardSince = "0" + twCardSince;

      newEndDateMmYy = newEndDate.substring(4, 6) + newEndDate.substring(2, 4);

      corpTtlAmtBal += ttlAmtBal;

      printDetail();

      if (detailCnt % 50 == 0) {
         printFooter();
         pageNo++;
         printHeader(pageNo, 1);
      }


   }
   closeCursor();
   lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
   printCorpTotal();
   printBranchFooter();
   printFooter();
}

void procData2() throws Exception {

   reportName = "政府網路採購卡當月消費狀況明細表";

   sqlCmd = " select A.reg_bank_no , A.corp_no , A.corp_p_seqno , A.card_no "
             +", A.acno_p_seqno "
             +", uf_card_name(A.card_no) as chi_name "
             +", A.ori_issue_date as card_since, A.issue_date "  //activate_date "
             +", decode(A.current_code,'0','*','') as tt_current_code "
             +", A.oppost_reason , A.new_end_date "
             + ", A.current_code "
             +", B.line_of_credit_amt , B.int_rate_mcode "
             + ", C.acct_jrnl_bal , C.ttl_amt_bal , C.min_pay_bal "
             + " from crd_card A join act_acno B on A.acno_p_seqno = B.acno_p_seqno "
             +"   join act_acct C on A.acno_p_seqno = C.p_seqno "
             + " where 1=1 and A.corp_p_seqno <> '' "
             + " and A.reg_bank_no <> '' "
             + " and A.corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1) "
             + " and (A.current_code ='0' or C.acct_jrnl_bal > 0) "
             + " and A.group_code = '1599' "
             + " order by A.reg_bank_no Asc , A.corp_p_seqno Asc , A.current_code "
//			   + " fetch first 100 rows only "
   ;

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      initData();
      branch = colSs("reg_bank_no");
      corpNo = colSs("corp_no");
      corpPSeqno = colSs("corp_p_seqno");
      cardNo = colSs("card_no");
      cardChiName = colSs("chi_name");
      cardSince = colSs("card_since");
      currentCode = colSs("current_code");
      ttCurrnetCode = colSs("tt_current_code");
      oppoReason = colSs("oppost_reason");
      newEndDate = colSs("new_end_date");
      lineOfCreditAmt = colNum("line_of_credit_amt");
      intRateMcode = colInt("int_rate_mcode");
      acnoPSeqno = colSs("acno_p_seqno");
      acctJrnlBal = colNum("acct_jrnl_bal");
      ttlAmtBal = colNum("ttl_amt_bal");
      minPayBal = colNum("min_pay_bal");
      if (empty(cardSince)) {
         cardSince =colSs("issue_date");
      }

      if (empty(lastCorp))
         lastCorp = corpNo;

      if (lastCorp.equals(corpNo) == false) {
         if (empty(lastBranch) || eq(lastBranch,branch)) {
            lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
            printCorpTotal();
            lastCorp = corpNo;
            corpTtlAmtBal = 0;
            lastPct = 0;
         }

      }


      if (empty(lastBranch) || eq(lastBranch,branch) == false) {
         if (empty(lastBranch) == false) {
            lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
            printCorpTotal();
            lastCorp = corpNo;
            corpTtlAmtBal = 0;
            lastPct = 0;

            printBranchFooter();
            printFooter();
         }
         pageNo = 1;
         printHeader(pageNo, 2);
         lastBranch = branch;
      }

      if (lastAcnoPSeqno.equals(acnoPSeqno))
         continue;

      lastAcnoPSeqno = acnoPSeqno;

      //--月消費
      selectBilBill();
//      selectCycPyaj();
      //--現欠
//			selectActAcct();
      //--公司戶額度
      selectActAcno();
      //--公司基本資料
      selectCorpData();

      //--分行名稱
      getBranchName();

      totDue = selectAnalSub(thisYear + "%");
      totLimitAmt = selectAnalSub(lastYear + "%");
      if ((totDue - totLimitAmt) > 600000)
         overSixtyFlag = "Y";
      else
         overSixtyFlag = "";

      overDay = getOverDay();

      monthConsume = monthConsume - pyajAmt;

      if ("Y".equals(overSixtyFlag))
         overSixtyCnt++;

      cardLastSix = cardNo.substring(cardNo.length() - 6, cardNo.length());

      newEndDateMmYy = newEndDate.substring(4, 6) + newEndDate.substring(2, 4);

      corpTtlAmtBal += ttlAmtBal;

      printDetail();

      if (detailCnt % 50 == 0) {
         printFooter();
         pageNo++;
         printHeader(pageNo, 2);
      }


   }
   closeCursor();
   lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
   printCorpTotal();
   printBranchFooter();
   printFooter();
}

void procData3() throws Exception {

   reportName = "本行公務車加油商務卡當月消費狀況明細表";

   sqlCmd = " select A.reg_bank_no , A.corp_no , A.corp_p_seqno , A.card_no "
             +", A.acno_p_seqno "
             +", uf_card_name(A.card_no) as chi_name "
             +", A.ori_issue_date as card_since, A.issue_date "  //activate_date "
             +", decode(A.current_code,'0','*','') as tt_current_code "
             +", A.oppost_reason , A.new_end_date "
             + ", A.current_code "
             +", B.line_of_credit_amt , B.int_rate_mcode "
             + ", C.acct_jrnl_bal , C.ttl_amt_bal , C.min_pay_bal "
             + " from crd_card A join act_acno B on A.acno_p_seqno = B.acno_p_seqno join act_acct C on B.acno_p_seqno = C.p_seqno "
             + " where 1=1 and A.corp_p_seqno <> '' "
             + " and A.reg_bank_no <> '' "
             + " and A.corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1) "
             + " and (A.current_code ='0' or C.acct_jrnl_bal > 0) "
             + " and A.group_code = '3713' "
             + " order by A.reg_bank_no Asc , A.corp_p_seqno Asc , A.current_code "
//			   + " fetch first 100 rows only "
   ;

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      initData();
      branch = colSs("reg_bank_no");
      corpNo = colSs("corp_no");
      corpPSeqno = colSs("corp_p_seqno");
      cardNo = colSs("card_no");
      cardChiName = colSs("chi_name");
      cardSince = colSs("activate_date");
      currentCode = colSs("current_code");
      ttCurrnetCode = colSs("tt_current_code");
      oppoReason = colSs("oppost_reason");
      newEndDate = colSs("new_end_date");
      lineOfCreditAmt = colNum("line_of_credit_amt");
      intRateMcode = colInt("int_rate_mcode");
      acnoPSeqno = colSs("acno_p_seqno");
      acctJrnlBal = colNum("acct_jrnl_bal");
      ttlAmtBal = colNum("ttl_amt_bal");
      minPayBal = colNum("min_pay_bal");
      if (empty(cardSince)) {
         cardSince =colSs("issue_date");
      }

      if (empty(lastCorp))
         lastCorp = corpNo;

      if (lastCorp.equals(corpNo) == false) {
         if (empty(lastBranch) || eq(lastBranch,branch)) {
            lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
            printCorpTotal();
            lastCorp = corpNo;
            corpTtlAmtBal = 0;
            lastPct = 0;
         }

      }


      if (empty(lastBranch) || eq(lastBranch,branch) == false) {
         if (empty(lastBranch) == false) {
            lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
            printCorpTotal();
            lastCorp = corpNo;
            corpTtlAmtBal = 0;
            lastPct = 0;

            printBranchFooter();
            printFooter();
         }
         pageNo = 1;
         printHeader(pageNo, 3);
         lastBranch = branch;
      }

      if (lastAcnoPSeqno.equals(acnoPSeqno))
         continue;

      lastAcnoPSeqno = acnoPSeqno;

      //--月消費
      selectBilBill();
//      selectCycPyaj();
      //--現欠
//			selectActAcct();
      //--公司戶額度
      selectActAcno();
      //--公司基本資料
      selectCorpData();

      //--分行名稱
      getBranchName();

      totDue = selectAnalSub(thisYear + "%");
      totLimitAmt = selectAnalSub(lastYear + "%");
      if ((totDue - totLimitAmt) > 600000)
         overSixtyFlag = "Y";
      else
         overSixtyFlag = "";

      overDay = getOverDay();

      monthConsume = monthConsume - pyajAmt;

      if ("Y".equals(overSixtyFlag))
         overSixtyCnt++;

      cardLastSix = cardNo.substring(cardNo.length() - 6, cardNo.length());

      newEndDateMmYy = newEndDate.substring(4, 6) + newEndDate.substring(2, 4);

      corpTtlAmtBal += ttlAmtBal;

      printDetail();

      if (detailCnt % 50 == 0) {
         printFooter();
         pageNo++;
         printHeader(pageNo, 3);
      }


   }
   closeCursor();
   lastPct = corpTtlAmtBal / corpLineOfCreditAmt;
   printCorpTotal();
   printBranchFooter();
   printFooter();
}
//-----------
com.DataSet dsBrn=new DataSet();
void loadGenBrn() throws Exception {
   dsBrn.dataClear();
   sqlCmd ="select branch, full_chi_name"+
            " from gen_brn "+
            " where 1=1"+
            " order by 1";

   sqlQuery(dsBrn,"",null);
   dsBrn.loadKeyData("branch");
   printf(" load gen_brn row=[%s]", dsBrn.rowCount());
}
//-------
void getBranchName() {
   branchFullName ="";
   int liRow =dsBrn.getKeyData(branch);
   if (liRow >0) {
      branchFullName =dsBrn.colSs("full_chi_name");
   }
}

int tiAnalSub = -1;

double selectAnalSub(String searchMonth) throws Exception {
   if (tiAnalSub <= 0) {
      sqlCmd = " select sum(his_purchase_amt+his_cash_amt) as sub_amt from act_anal_sub where p_seqno in (select acno_p_seqno "
                + " from act_acno where corp_p_seqno = ?) and acct_month like ? ";
      tiAnalSub = ppStmtCrt("ti-S-actAnalSub", "");
   }

   setString(1, corpPSeqno);
   setString(2, searchMonth);

   sqlSelect(tiAnalSub);
   if (sqlNrow <= 0) {
      return 0;
   }

   return colNum("sub_amt");
}

int tiCrdCorp = -1;

void selectCorpData() throws Exception {
   if (tiCrdCorp <= 0) {
      sqlCmd = "select chi_name as corp_name , corp_tel_zone1||corp_tel_no1||corp_tel_ext1 as office_tel from crd_corp where corp_no = ?";
      tiCrdCorp = ppStmtCrt("ti-S-crdCorp", "");
   }

   setString(1, corpNo);

   sqlSelect(tiCrdCorp);
   if (sqlNrow <= 0) {
      corpName = "";
      corpTelNo = "";
      return;
   }

   corpName = colSs("corp_name");
   corpTelNo = colSs("office_tel");
}

int tiActAcno = -1;

void selectActAcno() throws Exception {
   if (tiActAcno <= 0) {
      sqlCmd = "select line_of_credit_amt as corp_line_of_credit_amt , acno_p_seqno as corp_acno_p_seqno from act_acno where corp_p_seqno = ? and acno_flag ='2'";
      tiActAcno = ppStmtCrt("ti-S-actAcno", "");
   }

   setString(1, corpPSeqno);

   sqlSelect(tiActAcno);
   if (sqlNrow <= 0) {
      corpLineOfCreditAmt = 0;
      corpAcnoPSeqno = "";
      return;
   }

   corpLineOfCreditAmt = colNum("corp_line_of_credit_amt");
   corpAcnoPSeqno = colSs("corp_acno_p_seqno");
}

int tiActAcct = -1;

void selectActAcct() throws Exception {
   if (tiActAcct <= 0) {
      sqlCmd = "select acct_jrnl_bal , ttl_amt_bal , min_pay_bal from act_acct where p_seqno = ?";
      tiActAcct = ppStmtCrt("ti-S-actAcct", "");
   }

   setString(1, acnoPSeqno);

   sqlSelect(tiActAcct);
   if (sqlNrow <= 0) {
      acctJrnlBal = 0;
      ttlAmtBal = 0;
      minPayBal = 0;
      return;
   }

   acctJrnlBal = colNum("acct_jrnl_bal");
   ttlAmtBal = colNum("ttl_amt_bal");
   minPayBal = colNum("min_pay_bal");
}

int tiBilBill = -1;
void selectBilBill() throws Exception {
   if (tiBilBill <= 0) {
//      sqlCmd = "select sum(dest_amt) as month_consume "
//                +" from bil_bill where purchase_date like ? and card_no = ? ";
      sqlCmd ="select sum(decode(sign_flag,'-', 0 - dest_amt, dest_amt)) as month_consume"
               +" from bil_bill "
               +" where p_seqno =? "
               +" and substring(post_date,1,6) =?"
               +" and acct_code in ('BL','CA','IT','AO','ID','OT') "
       ;
      tiBilBill = ppStmtCrt("ti-S-bilBill", "");
   }

//   setString(1, hBusiDate.substring(0, 6) + "%");
//   setString(2, cardNo);
   ppp(1, acnoPSeqno);
   ppp(is_busiYYMM);
   sqlSelect(tiBilBill);
   if (sqlNrow <= 0) {
      monthConsume = 0;
      return;
   }

   monthConsume = colNum("month_consume");
}

//int tiPyaj = -1;
//void selectCycPyaj() throws Exception {
//   if (tiPyaj <= 0) {
//      sqlCmd = "select sum(payment_amt) as pyaj_amt "
//                +" from cyc_pyaj "
//                +" where payment_date like ? and p_seqno = ? ";
//      tiPyaj = ppStmtCrt("ti-S-cycPyaj", "");
//   }
//
//   setString(1, hBusiDate.substring(0, 6) + "%");
//   setString(2, acnoPSeqno);
//
//   sqlSelect(tiPyaj);
//   if (sqlNrow <= 0) {
//      pyajAmt = 0;
//      return;
//   }
//
//   pyajAmt = colNum("pyaj_amt");
//}

void printBranchFooter() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   tempBuf.append("  合計                                     ");
   tempBuf.append("增逾６０萬元註記Ｖ：共");
   tempBuf.append(comc.fixRight(getNumbericValue(overSixtyCnt), 10));
   tempBuf.append("戶");
   tempBuf.append(newLine);

   tempBuf.append("備註：「增逾６０萬元註記」法人信用卡客戶名下各卡累積簽帳金額，較上年度各卡總簽帳金額增逾６０萬元者，註記為〝Ｖ〞。");
   tempBuf.append(newLine);
   tempBuf.append("活卡註記：★係最近半年內實際有交易且未辦理停用或強制停卡者。");
   tempBuf.append(newLine);
   tempBuf.append("本表列出法人卡流通卡或現欠金額大於零之卡片資料。");
   tempBuf.append(newLine);
   writeTextFile(iiFileNum, tempBuf.toString());
}

void printFooter() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   tempBuf.append("合計" + newLine + newLine);
   writeTextFile(iiFileNum, tempBuf.toString());
}

void printCorpTotal() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   DecimalFormat df = new DecimalFormat("###.##");
   tempBuf.append("    上年度總簽帳金額：");
   tempBuf.append(comc.fixRight(getNumbericValue(totLimitAmt), 15));
   tempBuf.append("    本年度總簽帳金額：");
   tempBuf.append(comc.fixRight(getNumbericValue(totDue), 15));
   tempBuf.append("    增逾６０萬元註記：");
   tempBuf.append(comc.fixLeft(overSixtyFlag, 2));
   tempBuf.append("    上期循環比率：");
   tempBuf.append(comc.fixRight(df.format(lastPct), 7));
   detailCnt++;
   //--中間要多空一行
   tempBuf.append(newLine);
   detailCnt++;
   writeTextFile(iiFileNum, tempBuf.toString());
}

void printDetail() throws Exception {
   StringBuffer tempBuf = new StringBuffer();

   String ls_twCardSince =commString.right("0000000"+commDate.toTwDate(cardSince),7);

   tempBuf.append(comc.fixLeft(corpName, 18));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixLeft(corpNo, 9));
   tempBuf.append(comc.fixRight(getNumbericValue(corpLineOfCreditAmt), 11));
   tempBuf.append(comc.fixRight(cardLastSix, 7));
   tempBuf.append(comc.fixRight(cardChiName, 10));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixRight(getNumbericValue(lineOfCreditAmt), 9));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixRight(ls_twCardSince, 7));
   tempBuf.append(comc.fixRight(newEndDateMmYy, 10));
   tempBuf.append(comc.fixRight(getNumbericValue(monthConsume), 14));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixRight(getNumbericValue(acctJrnlBal), 9));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixRight(overDay, 3));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixLeft(corpTelNo, 12));
   tempBuf.append(commString.space(1));
   tempBuf.append(comc.fixLeft(ttCurrnetCode, 1));
   tempBuf.append(commString.space(3));
   tempBuf.append(comc.fixLeft(oppoReason, 3));
   tempBuf.append(newLine);
   writeTextFile(iiFileNum, tempBuf.toString());
   detailCnt++;
}

void printHeader(int page, int type) throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String reportBank = "合作金庫商業銀行", reportId = "CRM36";
   String twYear = "", twMonth = "", twDate = "", reportTwDate = "", title = "";

   twYear = busiTwDate.substring(0, busiTwDate.length() - 4);
   twMonth = busiTwDate.substring(busiTwDate.length() - 4).substring(0, 2);
   twDate = busiTwDate.substring(busiTwDate.length() - 2);

   reportTwDate = "中華民國 " + twYear + " 年 " + twMonth + " 月 " + twDate + " 日";

   //--控制行
   if (type == 1) {
      tempBuf.append(commString.rpad(branch, 10));
      tempBuf.append(commString.rpad("CRM36", 16));
      tempBuf.append(commString.rpad(busiTwDate + reportName, 88) + "N");
      tempBuf.append(newLine);
   } else if (type == 2) {
      tempBuf.append(commString.rpad(branch, 10));
      tempBuf.append(commString.rpad("CRM36", 16));
      tempBuf.append(commString.rpad(busiTwDate + reportName, 84) + "N");
      tempBuf.append(newLine);
   } else if (type == 3) {
      tempBuf.append(commString.rpad(branch, 10));
      tempBuf.append(commString.rpad("CRM36", 16));
      tempBuf.append(commString.rpad(busiTwDate + reportName, 81) + "N");
      tempBuf.append(newLine);
   }


   //--銀行
   tempBuf.append(commString.space(57));
   tempBuf.append(reportBank);
   tempBuf.append(newLine);

   //--分行、名稱、保存期限
   if (type == 1) {
      tempBuf.append(commString.bbFixlen(" 分行代號:" + branch + " " + branchFullName, 53));
//			tempBuf.append(commString.rpad(" 分行代號:"+branch+" "+branchFullName, 45));
      tempBuf.append(commString.rpad(reportName, 53));
      tempBuf.append("保存期限:一年");
      tempBuf.append(newLine);
   } else if (type == 2) {
      tempBuf.append(commString.bbFixlen(" 分行代號:" + branch + " " + branchFullName, 49));
//			tempBuf.append(commString.rpad(" 分行代號:"+branch+" "+branchFullName, 41));
      tempBuf.append(commString.rpad(reportName, 53));
      tempBuf.append("保存期限:一年");
      tempBuf.append(newLine);
   } else if (type == 3) {
      tempBuf.append(commString.bbFixlen(" 分行代號:" + branch + " " + branchFullName, 45));
//			tempBuf.append(commString.rpad(" 分行代號:"+branch+" "+branchFullName, 37));
      tempBuf.append(commString.rpad(reportName, 54));
      tempBuf.append("保存期限:一年");
      tempBuf.append(newLine);
   }

   //--報表代號、科目代號、日期、頁次
   tempBuf.append(" 報表代號:");
   tempBuf.append(commString.rpad(reportId, 10));
   tempBuf.append(commString.rpad(" 科目代號: ", 28));
   tempBuf.append(commString.rpad(reportTwDate, 64));
   tempBuf.append("第");
   tempBuf.append(commString.lpad(commString.int2Str(page), 4, "0"));
   tempBuf.append("頁");
   tempBuf.append(newLine);

   //--分隔
   tempBuf.append("====================================================================================================================================").append(newLine);

   //--欄位一
   title = " 公司名稱          客戶編號     公司信用 卡號     持卡人    卡片     卡片    卡片到期日  本月        現欠  逾期  電話     活卡  停掛";
   tempBuf.append(commString.rpad(title, 132));
   tempBuf.append(newLine);

   title = "";
   //--欄位二
   title = "                                 總額度 末六碼              額度    開戶日    ＭＭＹＹ   消費金額          天數           註記  原因";
   tempBuf.append(commString.rpad(title, 132));
   tempBuf.append(newLine);

   //--分隔
   tempBuf.append("====================================================================================================================================").append(newLine);

   writeTextFile(iiFileNum, tempBuf.toString());
}

void checkOpen() throws Exception {
   //fileName = "RCRM36.1.TXT";
   String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum < 0) {
      showLogMessage("I", "", "無權限產擋");
      errExit(1);
   }

   return;
}

void initData() {
   branch = "";
   corpNo = "";
   corpPSeqno = "";
   cardNo = "";
   cardChiName = "";
   cardSince = "";
   ttCurrnetCode = "";
   oppoReason = "";
   newEndDate = "";
   lineOfCreditAmt = 0;
   intRateMcode = 0;
   acnoPSeqno = "";
}

String getNumbericValue(double a) {
   String number = "";
   number = DecimalFormat.getNumberInstance().format(a);
   return number;
}

String getOverDay() {
   String returnDay = "";
   switch (intRateMcode) {
      case 0:
         if (minPayBal > 0)
            returnDay = "15";
         else
            returnDay = "0";
         break;
      case 1:
         returnDay = "30";
         break;
      case 2:
         returnDay = "60";
         break;
      case 3:
         returnDay = "90";
         break;
      case 4:
         returnDay = "120";
         break;
      case 5:
         returnDay = "150";
         break;
      case 6:
         returnDay = "180";
         break;
      case 7:
         returnDay = "210";
         break;
      default:
         if (intRateMcode >= 7)
            returnDay = "210";
         else
            returnDay = "0";
   }

   return returnDay;
}

}
