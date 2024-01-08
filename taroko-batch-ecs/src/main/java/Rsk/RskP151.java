/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-02-07  V1.00.00  Alex        initial                                  *
 *  112-03-25  V1.00.01  Alex        可用餘額讀取方式變更							 *
 *  112-03-29  V1.00.02  Alex        效能改善
 *  2023-0822  V1.00.04  JH          效能改善
 *  2023-1124  V1.00.05  JH          okExit()
 *  2023-1127  V1.00.06  JH          selectActAnalSub()
 *  2023-1129  V1.00.07  JH          JOIN all Table
 *****************************************************************************/
package Rsk;

import com.*;

import java.text.DecimalFormat;

public class RskP151 extends BaseBatch {

private final String progname = "每日產RP18卡人檔作業(同RskP150-all-join) 2023-1129  V1.00.07";
//CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommString commString = new CommString();
//CommDate commDate = new CommDate();
CommFTP commFTP = null;
CommRoutine comr = null;

DataSet dsCycle=new DataSet();
DataSet dsRcrate=new DataSet();

final String fileName = "RP18VIEWID.txt";
private int iiFileNum = 0;
//--共通欄位
String idPSeqno = "";
String acnoPSeqno = "";
String pSeqno="";
String acctType="";
double cardAcctIdx = 0.0;
boolean lbSup = false;
String lastAcctMonth = "";
String stmtCycle = "";
//--文件所需欄位
String idNo = "";
String birthday = "";
String homeTelPhone = "";
String officeTelPhone = "";
String employeeFlag = "";
String marketAgreeFlag = "";
String eNewsFlag = "";
String eMailAddr = "";
String cellarPhone = "";
String marriageFlag = "";
String businessCode = "";
String sexFlag = "";
String jobPosition = "";
String chiName = "";
String billZip = "";
String billAddr1 = "";
String billAddr2 = "";
String billAddr3 = "";
String billAddr4 = "";
String billAddr5 = "";
String companyName = "";
String otherCntryCode = "";
String passportNo = "";
String passportDate = "";
String serviceYear = "";
int annualIncome = 0;
String education = "";
String spouseId = "";
String spouseBirthday = "";
String spouseChiName = "";
String residentZip = "";
String residentAddr1 = "";
String residentAddr2 = "";
String residentAddr3 = "";
String residentAddr4 = "";
String residentAddr5 = "";
String mailZip = "";
String mailAddr1 = "";
String mailAddr2 = "";
String mailAddr3 = "";
String mailAddr4 = "";
String mailAddr5 = "";
String companyZip = "";
String companyAddr1 = "";
String companyAddr2 = "";
String companyAddr3 = "";
String companyAddr4 = "";
String companyAddr5 = "";
String graduateSchool = "";
String autopayAcctBank = "";
String autopayAcctNo = "";
String autopayIndicator = "";
String billApplyFlag = "";
String statSendInternet = "";
String eMailEbill = "";
int ttlAmtBal = 0;
String consumeSign = "";
int hisPurchseAmt = 0;
String acnoCrtDate = "";
double revolveIntRate = 0.0;
double rcRateYear = 0.0;
int lineOfCreditAmt = 0;
String canUseLimitSign = "";
int canUseLimit = 0;
String paymentRate1 = "";
String paymentRate2 = "";
String paymentRate3 = "";
String paymentRate4 = "";
String paymentRate5 = "";
String paymentRate6 = "";
String paymentRate7 = "";
String paymentRate8 = "";
String paymentRate9 = "";
String paymentRate10 = "";
String paymentRate11 = "";
String paymentRate12 = "";
int overRateCnt = 0;
int intRateMcode = 0;
int minPayBal = 0;
int cpbduePeriod = 0;
String acctStatus = "";
String cpbdueOwnerBank = "";
String cpbdueType = "";
String cpbdueBankType = "";
String cpbdueTcbType = "";
double cpbdueRate = 0.0;
String liacStatus = "";
int totAmtMonth = 0;
String adjEffStartDate = "";
String adjEffEndDate = "";
String blockCode1 = "";
String blockCode2 = "";
String blockCode3 = "";
String blockCode4 = "";
String blockCode5 = "";
String idStatus = "";
String idResult = "";
int procCnt = 0;

public static void main(String[] args) {
   RskP151 proc = new RskP151();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP151 [business_date]");
      okExit(0);
   }

   dbConnect();
   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }

   checkOpen();

//   selectPtrWorkDay();
   selectPtrRcrate();
   loadCol_cpbdue();

   selectCrd_idno("%");
   selectDbc_idno("%");

//   String ssIdno="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//   for (int ii = 0; ii <ssIdno.length() ; ii++) {
//      String lsId=commString.mid(ssIdno,ii,1);
//      if (empty(lsId)) continue;
//      selectCrdIdNoData(lsId+"%");
//   }
//   for (int ii = 0; ii <ssIdno.length() ; ii++) {
//      String lsId=commString.mid(ssIdno,ii,1);
//      if (empty(lsId)) continue;
//      selectDbcIdNoData(lsId+"%");
//   }

   closeOutputText(iiFileNum);

//   commFTP = new CommFTP(getDBconnect(), getDBalias());
//   comr = new CommRoutine(getDBconnect(), getDBalias());
//   procFTP();
//   //--BackUp File
//   renameFile();

   endProgram();
}

//--FTP
void procFTP() throws Exception {
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料" + " errcode:" + errCode);
      insertEcsNotifyLog(fileName);
   }
}

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

void renameFile() throws Exception {
   String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName + "_" + sysDate);

   if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
      showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr4 + "]");
}

//--產生檔案
void checkOpen() throws Exception {
   //RP18VIEWID.txt
   String lsFile =String.format("/crdatacrea/CREDITALL/%s", fileName);

   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum < 0) {
      printf("[%s] 產檔失敗 !",lsFile);
      okExit(0);
   }

   return;
}

//--查詢信用卡人資料
void selectCrd_idno(String aIdno) throws Exception {
   sqlCmd = "select A.id_no , A.id_p_seqno , A.birthday , A.home_area_code1||A.home_tel_no1 as home_tel_phone"
       +", A.office_area_code1||A.office_tel_no1||A.office_tel_ext1 as office_tel_phone"
       +", A.staff_flag , A.market_agree_base"
       +", A.e_news , A.e_mail_addr , A.cellar_phone , A.marriage , A.business_code , A.sex"
       +", A.job_position , A.chi_name , A.company_name , A.other_cntry_code "
       +", A.passport_no , A.passport_date , A.service_year , A.annual_income/10000 as annual_income "
       +", A.education , A.spouse_id_no , A.spouse_birthday , A.spouse_name , A.resident_zip , A.resident_addr1 "
       +", A.resident_addr2 , A.resident_addr3 , A.resident_addr4 , A.resident_addr5 , A.mail_zip , A.mail_addr1 "
       +", A.mail_addr2 , A.mail_addr3 , A.mail_addr4 , A.mail_addr5 "
       +", A.company_zip , A.company_addr1 , A.company_addr2 , A.company_addr3 , A.company_addr4 , A.company_addr5 "
       +", A.graduation_elementarty , A.card_since "
       +", AA.acct_type, AA.acno_p_seqno, AA.p_seqno "
       +", AA.bill_sending_zip , AA.bill_sending_addr1 , AA.bill_sending_addr2 , AA.bill_sending_addr3 "
       +", AA.bill_sending_addr4 , AA.bill_sending_addr5 "
       +", AA.autopay_indicator , AA.bill_apply_flag , AA.stat_send_internet "
       +", AA.e_mail_ebill , AA.crt_date , AA.revolve_int_rate , AA.line_of_credit_amt "
       +", AA.payment_rate1 , AA.payment_rate2 , AA.payment_rate3 , AA.payment_rate4 "
       +", AA.payment_rate5 , AA.payment_rate6 , AA.payment_rate7 , AA.payment_rate8 "
       +", AA.payment_rate9 , AA.payment_rate10 , AA.payment_rate11 , AA.payment_rate12 "
       +", AA.int_rate_mcode , AA.stmt_cycle , AA.acct_status "
       +", BB.autopay_acct_bank , BB.autopay_acct_no "
       +", nvl(C1.ttl_amt_bal,0) ttl_amt_bal , nvl(C1.min_pay_bal,0) min_pay_bal "
       +", nvl(CC.tot_amt_month,0) tot_amt_month, CC.adj_eff_start_date , CC.adj_eff_end_date "
       +", CC.block_reason1 , CC.block_reason2, CC.block_reason3 "
       +", CC.block_reason4, CC.block_reason5 "
       +", nvl((SELECT CB.acct_amt_balance from cca_acct_balance_cal CB where CB.acno_p_seqno =AA.acno_p_seqno LIMIT 1),0) AS acct_amt_balance "
       +", nvl((SELECT SS.his_purchase_amt FROM act_anal_sub SS WHERE SS.p_seqno=AA.p_seqno AND SS.acct_month=WW.last_acct_month LIMIT 1) ,0) as anal_his_purchase_amt "
       +" from crd_idno A "
       +" left join act_acno AA on A.id_p_seqno=AA.id_p_seqno and AA.acno_flag='1' "
       +" LEFT join act_acct_curr BB on BB.p_seqno=AA.acno_p_seqno and BB.curr_code='901' "
       +" left join act_acct C1 on C1.p_seqno=AA.acno_p_seqno "
       +" LEFT JOIN cca_card_acct CC ON CC.acno_p_seqno=AA.acno_p_seqno AND CC.debit_flag<>'Y' "
       +" left join ptr_workday WW on WW.stmt_cycle=AA.stmt_cycle "
       +" where 1=1 "
       +" and A.id_no like ?"
   ;

   ppp(1, aIdno);
   openCursor();

   while (fetchTable()) {
      initData();
      procCnt++;
      totalCnt++;
      dspProcRow(10000);
      //-idno-
      idNo = colSs("id_no");
      idPSeqno = colSs("id_p_seqno");
      birthday = colSs("birthday");
      homeTelPhone = colSs("home_tel_phone");
      officeTelPhone = colSs("office_tel_phone");
      employeeFlag = colSs("staff_flag");
      marketAgreeFlag = colNvl("market_agree_base", "N");
      eNewsFlag = colNvl("e_news", "N");
      eMailAddr = colSs("e_mail_addr");
      cellarPhone = colSs("cellar_phone");
      marriageFlag = colSs("marriage");
      businessCode = colSs("business_code");
      sexFlag = colSs("sex");
      jobPosition = colSs("job_position");
      chiName = colSs("chi_name");
      companyName = colSs("company_name");
      otherCntryCode = colSs("other_cntry_code");
      passportNo = colSs("passport_no");
      passportDate = colSs("passport_date");
      serviceYear = colSs("service_year");
      annualIncome = colInt("annual_income");
      education = colSs("education");
      spouseId = colSs("spouse_id_no");
      spouseBirthday = colSs("spouse_birthday");
      spouseChiName = colSs("spouse_name");
      residentZip = colSs("resident_zip");
      residentAddr1 = colSs("resident_addr1");
      residentAddr2 = colSs("resident_addr2");
      residentAddr3 = colSs("resident_addr3");
      residentAddr4 = colSs("resident_addr4");
      residentAddr5 = colSs("resident_addr5");
      mailZip = colSs("mail_zip");
      mailAddr1 = colSs("mail_addr1");
      mailAddr2 = colSs("mail_addr2");
      mailAddr3 = colSs("mail_addr3");
      mailAddr4 = colSs("mail_addr4");
      mailAddr5 = colSs("mail_addr5");
      companyZip = colSs("company_zip");
      companyAddr1 = colSs("company_addr1");
      companyAddr2 = colSs("company_addr2");
      companyAddr3 = colSs("company_addr3");
      companyAddr4 = colSs("company_addr4");
      companyAddr5 = colSs("company_addr5");
      graduateSchool = colSs("graduation_elementarty");
      acnoCrtDate = colSs("card_since");

      lbSup =false;
      //-acno------
      acnoPSeqno = colSs("acno_p_seqno");
      pSeqno =colSs("p_seqno");
      acctType =colSs("acct_type");
      billZip = colSs("bill_sending_zip");
      billAddr1 = colSs("bill_sending_addr1");
      billAddr2 = colSs("bill_sending_addr2");
      billAddr3 = colSs("bill_sending_addr3");
      billAddr4 = colSs("bill_sending_addr4");
      billAddr5 = colSs("bill_sending_addr5");
      autopayIndicator = colSs("autopay_indicator");
      billApplyFlag = colSs("bill_apply_flag");
      statSendInternet = colSs("stat_send_internet");
      eMailEbill = colSs("e_mail_ebill");
      revolveIntRate = colNum("revolve_int_rate");
      lineOfCreditAmt = colInt("line_of_credit_amt");
      paymentRate1 = colSs("payment_rate1");
      paymentRate2 = colSs("payment_rate2");
      paymentRate3 = colSs("payment_rate3");
      paymentRate4 = colSs("payment_rate4");
      paymentRate5 = colSs("payment_rate5");
      paymentRate6 = colSs("payment_rate6");
      paymentRate7 = colSs("payment_rate7");
      paymentRate8 = colSs("payment_rate8");
      paymentRate9 = colSs("payment_rate9");
      paymentRate10 = colSs("payment_rate10");
      paymentRate11 = colSs("payment_rate11");
      paymentRate12 = colSs("payment_rate12");
      intRateMcode = colInt("int_rate_mcode");
      acctStatus = colSs("acct_status");
      stmtCycle =colSs("stmt_cycle");
      //-acct_curr-
      autopayAcctBank = colSs("autopay_acct_bank");
      autopayAcctNo = colSs("autopay_acct_no");
      //-act_acct-
      ttlAmtBal = colInt("ttl_amt_bal");
      minPayBal = colInt("min_pay_bal");
      //-cca_card_acct-
      totAmtMonth = colInt("tot_amt_month");
      adjEffStartDate = colSs("adj_eff_start_date");
      adjEffEndDate = colSs("adj_eff_end_date");
      blockCode1 = colSs("block_reason1");
      blockCode2 = colSs("block_reason2");
      blockCode3 = colSs("block_reason3");
      blockCode4 = colSs("block_reason4");
      blockCode5 = colSs("block_reason5");
      if (commString.between(sysDate, adjEffStartDate, adjEffEndDate) == false) {
         //--不在臨調效期內
         totAmtMonth = 0;
         adjEffStartDate = "";
         adjEffEndDate = "";
      }
      //-cca_acct_balance_cal--
      canUseLimit = colInt("acct_amt_balance");
      if (canUseLimit < 0)
         canUseLimitSign = "-";
      //-act_anal_sub---
      hisPurchseAmt =colInt("anal_his_purchase_amt");
      if (hisPurchseAmt >= 0) consumeSign = " ";
      else consumeSign = "-";

      //--附卡人找不到帳戶資訊故直接寫檔--
      lbSup =empty(acnoPSeqno);
      if (lbSup) {
         writeFile();
         continue;
      }

      getRCrate();
      getCol_cpbdue("01");
      calOverCnt();

      //--逾期註記 > 0 , 才有逾期金額
      if (intRateMcode == 0) {
         minPayBal = 0;
      }

      //--cpbdueType : 1. 銀行公會; 2. 本行協商
      if (eq(cpbdueType,"1")) {
         cpbdueTcbType = "";
      }
      else if (eq(cpbdueType,"2")) {
         cpbdueBankType = "";
      }
      writeFile();
   }
   closeCursor();
   printf("Crd_idno done , Proc Cnt = [" + procCnt + "]");
}

//--查詢VD卡人資料
void selectDbc_idno(String aIdno) throws Exception {
   sqlCmd ="select A.id_no, A.id_p_seqno, A.birthday, A.home_area_code1||A.home_tel_no1 as home_tel_phone "
       +", A.office_area_code1||A.office_tel_no1||A.office_tel_ext1 as office_tel_phone "
       +", A.staff_flag , A.market_agree_base "
       +", A.e_news , A.e_mail_addr , A.cellar_phone , A.marriage , A.business_code "
       +", A.sex , A.job_position , A.chi_name "
       +", A.company_name , A.other_cntry_code , A.passport_no "
       +", A.service_year , A.annual_income/10000 as annual_income "
       +", A.education , A.spouse_id_no , A.spouse_birthday , A.spouse_name "
       +", A.resident_zip , A.resident_addr1 , A.resident_addr2 "
       +", A.resident_addr3 , A.resident_addr4 , A.resident_addr5 "
       +", A.mail_zip , A.mail_addr1 , A.mail_addr2 "
       +", A.mail_addr3 , A.mail_addr4 , A.mail_addr5 "
       +", A.company_zip , A.company_addr1 , A.company_addr2 "
       +", A.company_addr3 , A.company_addr4 , A.company_addr5 "
       +", A.card_since "
       +", AA.p_seqno as acno_p_seqno "
       +", AA.bill_sending_zip, AA.bill_sending_addr1, AA.bill_sending_addr2 "
       +", AA.bill_sending_addr3, AA.bill_sending_addr4, AA.bill_sending_addr5 "
       +", AA.autopay_indicator, AA.bill_apply_flag, AA.stat_send_internet "
       +", AA.e_mail_ebill, AA.crt_date, AA.revolve_int_rate, AA.line_of_credit_amt "
       +", AA.payment_rate1, AA.payment_rate2, AA.payment_rate3, AA.payment_rate4 "
       +", AA.payment_rate5, AA.payment_rate6, AA.payment_rate7, AA.payment_rate8 "
       +", AA.payment_rate9, AA.payment_rate10, AA.payment_rate11, AA.payment_rate12 "
       +", AA.stmt_cycle, AA.acct_status "
       +", nvl(CC.tot_amt_month,0) tot_amt_month, CC.adj_eff_start_date , CC.adj_eff_end_date "
       +", CC.block_reason1 , CC.block_reason2, CC.block_reason3 "
       +", CC.block_reason4, CC.block_reason5 "
       +", nvl((SELECT CB.acct_amt_balance from cca_acct_balance_cal CB where CB.acno_p_seqno =AA.p_seqno and CB.acct_type =AA.acct_type LIMIT 1),0) AS acct_amt_balance "
       +", A1.id_no AS crd_id_no"
       +" from dbc_idno A "
       +" LEFT JOIN crd_idno A1 ON A1.id_no=A.id_no "
       +" left join dba_acno AA on AA.id_p_seqno=A.id_p_seqno "
       +" LEFT JOIN cca_card_acct CC ON CC.acno_p_seqno=AA.p_seqno AND CC.debit_flag='Y' "
       +" where 1=1 "
//       +" AND NOT EXISTS (SELECT id_no FROM crd_idno WHERE crd_idno.id_no=A.id_no) "
       +" and A.id_no like ? "
       +" order by A.id_no"
       ;

   ppp(1, aIdno);
   openCursor();

   String ls_kk_idNo="";
   while (fetchTable()) {
      initData();

      String ls_crdIdno=colSs("crd_id_no");
      //-存在crd_idno-
      if (!empty(ls_crdIdno)) continue;

      idNo = colSs("id_no");
      //-同一ID多ACNO--
      if (eq(idNo,ls_kk_idNo)) continue;
      procCnt++;
      totalCnt++;
      dspProcRow(10000);
      ls_kk_idNo =idNo;

      idPSeqno = colSs("id_p_seqno");
      birthday = colSs("birthday");
      homeTelPhone = colSs("home_tel_phone");
      officeTelPhone = colSs("office_tel_phone");
      employeeFlag = colSs("staff_flag");
      marketAgreeFlag = colNvl("market_agree_base", "N");
      eNewsFlag = colNvl("e_news", "N");
      eMailAddr = colSs("e_mail_addr");
      cellarPhone = colSs("cellar_phone");
      marriageFlag = colSs("marriage");
      businessCode = colSs("business_code");
      sexFlag = colSs("sex");
      jobPosition = colSs("job_position");
      chiName = colSs("chi_name");
      companyName = colSs("company_name");
      otherCntryCode = colSs("other_cntry_code");
      passportNo = colSs("passport_no");
      serviceYear = colSs("service_year");
      annualIncome = colInt("annual_income");
      education = colSs("education");
      spouseId = colSs("spouse_id_no");
      spouseBirthday = colSs("spouse_birthday");
      spouseChiName = colSs("spouse_name");
      residentZip = colSs("resident_zip");
      residentAddr1 = colSs("resident_addr1");
      residentAddr2 = colSs("resident_addr2");
      residentAddr3 = colSs("resident_addr3");
      residentAddr4 = colSs("resident_addr4");
      residentAddr5 = colSs("resident_addr5");
      mailZip = colSs("mail_zip");
      mailAddr1 = colSs("mail_addr1");
      mailAddr2 = colSs("mail_addr2");
      mailAddr3 = colSs("mail_addr3");
      mailAddr4 = colSs("mail_addr4");
      mailAddr5 = colSs("mail_addr5");
      companyZip = colSs("company_zip");
      companyAddr1 = colSs("company_addr1");
      companyAddr2 = colSs("company_addr2");
      companyAddr3 = colSs("company_addr3");
      companyAddr4 = colSs("company_addr4");
      companyAddr5 = colSs("company_addr5");
      acnoCrtDate = colSs("card_since");

      //-acno---
      acnoPSeqno = colSs("acno_p_seqno");
      pSeqno =colSs("acno_p_seqno");
      billZip = colSs("bill_sending_zip");
      billAddr1 = colSs("bill_sending_addr1");
      billAddr2 = colSs("bill_sending_addr2");
      billAddr3 = colSs("bill_sending_addr3");
      billAddr4 = colSs("bill_sending_addr4");
      billAddr5 = colSs("bill_sending_addr5");
      autopayIndicator = colSs("autopay_indicator");
      billApplyFlag = colSs("bill_apply_flag");
      statSendInternet = colSs("stat_send_internet");
      eMailEbill = colSs("e_mail_ebill");
//			acnoCrtDate = colSs("crt_date");
      revolveIntRate = colNum("revolve_int_rate");
      lineOfCreditAmt = colInt("line_of_credit_amt");
      paymentRate1 = colSs("payment_rate1");
      paymentRate2 = colSs("payment_rate2");
      paymentRate3 = colSs("payment_rate3");
      paymentRate4 = colSs("payment_rate4");
      paymentRate5 = colSs("payment_rate5");
      paymentRate6 = colSs("payment_rate6");
      paymentRate7 = colSs("payment_rate7");
      paymentRate8 = colSs("payment_rate8");
      paymentRate9 = colSs("payment_rate9");
      paymentRate10 = colSs("payment_rate10");
      paymentRate11 = colSs("payment_rate11");
      paymentRate12 = colSs("payment_rate12");
//			intRateMcode = colInt("int_rate_mcode");
      stmtCycle =colSs("stmt_cycle");
      acctStatus = colSs("acct_status");
      //-cca_card_acct-
      totAmtMonth = colInt("tot_amt_month");
      adjEffStartDate = colSs("adj_eff_start_date");
      adjEffEndDate = colSs("adj_eff_end_date");
      blockCode1 = colSs("block_reason1");
      blockCode2 = colSs("block_reason2");
      blockCode3 = colSs("block_reason3");
      blockCode4 = colSs("block_reason4");
      blockCode5 = colSs("block_reason5");

      if (commString.between(sysDate, adjEffStartDate, adjEffEndDate) == false) {
         //--不在臨調效期內
         totAmtMonth = 0;
         adjEffStartDate = "";
         adjEffEndDate = "";
      }
      //-cca_acct_balance_cal--
      canUseLimit = colInt("acct_amt_balance");
      if (canUseLimit < 0) {
         canUseLimitSign = "-";
         canUseLimit =0 - canUseLimit;
      }

      getRCrate();
      getCol_cpbdue("90");
      calOverCnt();

      //--逾期註記 > 0 , 才有逾期金額
      if (intRateMcode == 0) {
         minPayBal = 0;
      }

      //--cpbdueType : 1. 銀行公會; 2. 本行協商
      if (eq(cpbdueType,"1")) {
         cpbdueTcbType = "";
      }
      else if (eq("2",cpbdueType)) {
         cpbdueBankType = "";
      }
      //------
      canUseLimit = 0;
      canUseLimitSign = "";

      writeFile();
   }
   closeCursor();
   printf("Dbc_idno done , Proc Cnt = [" + procCnt + "]");
}

//--查詢對應年利率
//int tiRcrate=-1;
void selectPtrRcrate() throws Exception {
   sqlCmd = " select rcrate_day, rcrate_year from ptr_rcrate where 1=1"
           +" order by rcrate_day";
   sqlQuery(dsRcrate,"",null);
}
void getRCrate() throws Exception {
   rcRateYear =0;
   for (int ll = 0; ll <dsRcrate.rowCount() ; ll++) {
      double lmRateDD =dsRcrate.colNum(ll,"rcrate_day");
      if (lmRateDD == revolveIntRate) {
         rcRateYear =dsRcrate.colNum(ll,"rcrate_year");
         break;
      }
   }
   rcRateYear =rcRateYear * 100;
}

//--查詢關帳年月
void selectPtrWorkDay() throws Exception {
   dsCycle.dataClear();
   sqlCmd ="select stmt_cycle, last_acct_month"
           +" from ptr_workday"
           +" order by stmt_cycle"
           ;
   daoTable ="ptr_workday";
   sqlQuery(dsCycle, "", null);
}
//---
void getLastAcctMonth() throws Exception {
   lastAcctMonth ="";
   if (empty(stmtCycle)) return;

   for (int ii = 0; ii <dsCycle.rowCount() ; ii++) {
      String lsCycle=dsCycle.colSs(ii,"stmt_cycle");
      if (eq(lsCycle,stmtCycle)) {
         lastAcctMonth =dsCycle.colSs(ii,"last_acct_month");
         break;
      }
   }
}

//--計算逾期繳款次數
void calOverCnt() throws Exception {

   String[] paymentRate = new String[12];
   paymentRate[0] = paymentRate1;
   paymentRate[1] = paymentRate2;
   paymentRate[2] = paymentRate3;
   paymentRate[3] = paymentRate4;
   paymentRate[4] = paymentRate5;
   paymentRate[5] = paymentRate6;
   paymentRate[6] = paymentRate7;
   paymentRate[7] = paymentRate8;
   paymentRate[8] = paymentRate9;
   paymentRate[9] = paymentRate10;
   paymentRate[10] = paymentRate11;
   paymentRate[11] = paymentRate12;
   for (int ii = 0; ii < 12; ii++) {
      if (commString.ss2int(paymentRate[ii]) > 1)
         overRateCnt++;
   }

}

//--查詢催收相關資料---
DataSet dsCpbd=new DataSet();
DataSet dsLiac=new DataSet();
void loadCol_cpbdue() throws Exception {
   sqlCmd = " select cpbdue_id_p_seqno||'-'||cpbdue_acct_type as kk_pseqno"
       +", cpbdue_period , cpbdue_owner_bank , cpbdue_type , cpbdue_bank_type , cpbdue_tcb_type , "
       + " cpbdue_rate"
       +" from col_cpbdue"
       +" where 1=1"
       +" order by cpbdue_id_p_seqno,cpbdue_acct_type"
   ;
   sqlQuery(dsCpbd,sqlCmd,null);
   dsCpbd.loadKeyData("kk_pseqno");
   printf("load col_cpbdue.Cnt[%s]", dsCpbd.rowCount());
   //----------
   sqlCmd = " select id_p_seqno, liac_status "
       +" from col_liac_nego "
       +" where 1=1 "
       +" order by id_p_seqno ";
   sqlQuery(dsLiac,sqlCmd,null);
   dsLiac.loadKeyData("id_p_seqno");
   printf("load col_liac_nego.Cnt[%s]", dsLiac.rowCount());
}
//--
void getCol_cpbdue(String a_debit) throws Exception {
   String lsAcctType="01";
   if (eq(a_debit,"90")) lsAcctType="90";
   int liCnt =dsCpbd.getKeyData(idPSeqno+"-"+lsAcctType);
   if (liCnt >0) {
      cpbduePeriod = dsCpbd.colInt("cpbdue_period");
      cpbdueOwnerBank = dsCpbd.colSs("cpbdue_owner_bank");
      cpbdueType = dsCpbd.colSs("cpbdue_type");
      cpbdueBankType = dsCpbd.colSs("cpbdue_bank_type");
      cpbdueTcbType = dsCpbd.colSs("cpbdue_tcb_type");
      cpbdueRate = dsCpbd.colNum("cpbdue_rate");
      cpbdueRate = cpbdueRate * 100;
   }
   //--
   liCnt =dsLiac.getKeyData(idPSeqno);
   if (liCnt >0) {
      liacStatus = dsLiac.colSs("liac_status");
   }
}
//---
//int tiCpbdue=-1;
//int tiLiac=-1;
//void selectColCpbdue(String debitFlag) throws Exception {
//   if (tiCpbdue <=0) {
//      sqlCmd = " select cpbdue_period , cpbdue_owner_bank , cpbdue_type , cpbdue_bank_type , cpbdue_tcb_type , "
//              + " cpbdue_rate"
//      +" from col_cpbdue"
//      +" where cpbdue_id_p_seqno = ? and cpbdue_acct_type = ? ";
//      tiCpbdue =ppStmtCrt("ti-cpbdue","");
//   }
//   String lsAcctType="01";
//   if (eq(debitFlag,"90")) lsAcctType="90";
//
//   ppp(1, idPSeqno);
//   ppp(lsAcctType);
//   sqlSelect(tiCpbdue);
//   if (sqlNrow > 0) {
//      cpbduePeriod = colInt("cpbdue_period");
//      cpbdueOwnerBank = colSs("cpbdue_owner_bank");
//      cpbdueType = colSs("cpbdue_type");
//      cpbdueBankType = colSs("cpbdue_bank_type");
//      cpbdueTcbType = colSs("cpbdue_tcb_type");
//      cpbdueRate = colNum("cpbdue_rate");
//      cpbdueRate = cpbdueRate * 100;
//   }
//
//   if (tiLiac <=0) {
//      sqlCmd = " select liac_status from col_liac_nego where id_p_seqno = ? ";
//      tiLiac =ppStmtCrt("ti-liac","");
//   }
//   ppp(1, idPSeqno);
//   sqlSelect(tiLiac);
//   if (sqlNrow > 0) {
//      liacStatus = colSs("liac_status");
//   }
//
//}

//--計算可用餘額
//	void calBalance() throws Exception {
//		canUseLimit = (int) calBal.idNoBalance(idNo);
//		if(canUseLimit <0) {
//			canUseLimitSign = "-";
//			canUseLimit = canUseLimit * -1;
//		}
//			
//	}	

//--寫檔案
void writeFile() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String tempAmt = "", newLine = "\r\n", cutSign = ";";
   DecimalFormat df = new DecimalFormat("0");
   tempBuf.append(comc.fixLeft(idNo, 11));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(birthday, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(homeTelPhone, 14));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(officeTelPhone, 24));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(employeeFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(marketAgreeFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(eNewsFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(eMailAddr, 50));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(cellarPhone, 15));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(marriageFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(businessCode, 4));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(sexFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(jobPosition, 24));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(chiName, 50));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(billZip, 6));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(billAddr1, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(billAddr2, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(billAddr3, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(billAddr4, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(billAddr5, 56));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(companyName, 24));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(otherCntryCode, 5));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(passportNo, 20));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(passportDate, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(serviceYear, 4));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(annualIncome), 6, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 6));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(education, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(spouseId, 11));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(spouseBirthday, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(spouseChiName, 50));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(residentZip, 6));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(residentAddr1, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(residentAddr2, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(residentAddr3, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(residentAddr4, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(residentAddr5, 56));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(mailZip, 6));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(mailAddr1, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(mailAddr2, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(mailAddr3, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(mailAddr4, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(mailAddr5, 56));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(companyZip, 6));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(companyAddr1, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(companyAddr2, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(companyAddr3, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(companyAddr4, 12));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(companyAddr5, 56));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(graduateSchool, 20));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(autopayAcctBank, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(autopayAcctNo, 16));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(autopayIndicator, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(billApplyFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(statSendInternet, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(eMailEbill, 50));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(ttlAmtBal), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(consumeSign, 1));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(hisPurchseAmt), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(acnoCrtDate, 8));
   tempBuf.append(cutSign);
   tempAmt = df.format(rcRateYear);
   tempAmt = commString.lpad(tempAmt, 4, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 4));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(lineOfCreditAmt), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(canUseLimitSign, 1));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(canUseLimit), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(overRateCnt), 2, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 2));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(intRateMcode), 4, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 4));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(minPayBal), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempAmt = df.format(cpbduePeriod);
   tempAmt = commString.lpad(tempAmt, 3, "0");
//		tempAmt = commString.lpad(commString.int2Str(cpbduePeriod), 3, "0");		
   tempBuf.append(comc.fixLeft(tempAmt, 3));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(acctStatus, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(cpbdueOwnerBank, 4));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(cpbdueType, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(cpbdueBankType, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(cpbdueTcbType, 1));
   tempBuf.append(cutSign);
   tempAmt = df.format(cpbdueRate);
   tempAmt = commString.lpad(tempAmt, 4, "0");
//		tempAmt = commString.lpad(Double.toString(cpbdueRate), 4, "0");		
   tempBuf.append(comc.fixLeft(tempAmt, 4));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(liacStatus, 1));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(totAmtMonth), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(adjEffStartDate, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(adjEffEndDate, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(blockCode1, 2));
   tempBuf.append(comc.fixLeft(blockCode2, 2));
   tempBuf.append(comc.fixLeft(blockCode3, 2));
   tempBuf.append(comc.fixLeft(blockCode4, 2));
   tempBuf.append(comc.fixLeft(blockCode5, 2));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(idStatus, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(idResult, 1));
   tempBuf.append(newLine);
   writeTextFile(iiFileNum, tempBuf.toString());
}

//--清空欄位
void initData() throws Exception {
   lbSup = false;
   idPSeqno = "";
   acnoPSeqno = "";
   pSeqno ="";
   cardAcctIdx = 0.0;
   idNo = "";
   birthday = "";
   homeTelPhone = "";
   officeTelPhone = "";
   employeeFlag = "";
   marketAgreeFlag = "";
   eNewsFlag = "";
   eMailAddr = "";
   cellarPhone = "";
   marriageFlag = "";
   businessCode = "";
   sexFlag = "";
   jobPosition = "";
   chiName = "";
   billZip = "";
   billAddr1 = "";
   billAddr2 = "";
   billAddr3 = "";
   billAddr4 = "";
   billAddr5 = "";
   companyName = "";
   otherCntryCode = "";
   passportNo = "";
   passportDate = "";
   serviceYear = "";
   annualIncome = 0;
   education = "";
   spouseId = "";
   spouseBirthday = "";
   spouseChiName = "";
   residentZip = "";
   residentAddr1 = "";
   residentAddr2 = "";
   residentAddr3 = "";
   residentAddr4 = "";
   residentAddr5 = "";
   mailZip = "";
   mailAddr1 = "";
   mailAddr2 = "";
   mailAddr3 = "";
   mailAddr4 = "";
   mailAddr5 = "";
   companyZip = "";
   companyAddr1 = "";
   companyAddr2 = "";
   companyAddr3 = "";
   companyAddr4 = "";
   companyAddr5 = "";
   graduateSchool = "";
   autopayAcctBank = "";
   autopayAcctNo = "";
   autopayIndicator = "";
   billApplyFlag = "";
   statSendInternet = "";
   eMailEbill = "";
   ttlAmtBal = 0;
   consumeSign = "";
   hisPurchseAmt = 0;
   acnoCrtDate = "";
   rcRateYear = 0.0;
   lineOfCreditAmt = 0;
   canUseLimitSign = "";
   canUseLimit = 0;
   paymentRate1 = "";
   paymentRate2 = "";
   paymentRate3 = "";
   paymentRate4 = "";
   paymentRate5 = "";
   paymentRate6 = "";
   paymentRate7 = "";
   paymentRate8 = "";
   paymentRate9 = "";
   paymentRate10 = "";
   paymentRate11 = "";
   paymentRate12 = "";
   overRateCnt = 0;
   intRateMcode = 0;
   minPayBal = 0;
   cpbduePeriod = 0;
   acctStatus = "";
   cpbdueOwnerBank = "";
   cpbdueType = "";
   cpbdueBankType = "";
   cpbdueTcbType = "";
   cpbdueRate = 0.0;
   liacStatus = "";
   totAmtMonth = 0;
   adjEffStartDate = "";
   adjEffEndDate = "";
   blockCode1 = "";
   blockCode2 = "";
   blockCode3 = "";
   blockCode4 = "";
   blockCode5 = "";
   lastAcctMonth = "";
   stmtCycle = "";
   idStatus = "";
   idResult = "";
}
}
