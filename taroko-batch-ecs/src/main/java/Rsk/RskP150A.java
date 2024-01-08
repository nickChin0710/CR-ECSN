/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112/12/25  V1.00.00  Wilson       initial                                 *
 *****************************************************************************/
package Rsk;

import com.*;

import java.text.DecimalFormat;

public class RskP150A extends BaseBatch {

private final String progname = "每日產RP18卡人檔作業 112/12/25  V1.00.00";
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
String lastIdNo = "";

public static void main(String[] args) {
   RskP150A proc = new RskP150A();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP150A [business_date]");
      okExit(0);
   }

   dbConnect();
   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }

   checkOpen();

   selectPtrWorkDay();
   selectPtrRcrate();
   loadCol_cpbdue();
   
   selectCrdIdNoData();
   
   selectDbcIdNoData();

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

//--查詢信用卡正人資料
void selectCrdIdNoData() throws Exception {

   sqlCmd = " select a.id_no , a.id_p_seqno , a.birthday , a.home_area_code1||a.home_tel_no1 as home_tel_phone , "
          + "        a.office_area_code1||a.office_tel_no1||a.office_tel_ext1 as office_tel_phone , a.staff_flag , "
          + "        a.market_agree_base , a.e_news , a.e_mail_addr , a.cellar_phone , a.marriage , a.business_code , "
          + "        a.sex , a.job_position , a.chi_name , a.company_name , a.other_cntry_code , a.passport_no , "
          + "        a.passport_date , a.service_year , a.annual_income/10000 as annual_income , a.education ,  "
          + "        a.spouse_id_no , a.spouse_birthday , a.spouse_name , a.resident_zip , a.resident_addr1 , "
          + "        a.resident_addr2 , a.resident_addr3 , a.resident_addr4 , a.resident_addr5 , a.mail_zip , "
          + "        a.mail_addr1 , a.mail_addr2 , a.mail_addr3 , a.mail_addr4 , a.mail_addr5 , a.company_zip , "
          + "        a.company_addr1 , a.company_addr2 , a.company_addr3 , a.company_addr4 , a.company_addr5 , "
          + "        a.graduation_elementarty , a.card_since , "           
          + "        b.acct_type, b.acno_p_seqno, b.p_seqno , b.bill_sending_zip , b.bill_sending_addr1 , "
	      + "        b.bill_sending_addr2 , b.bill_sending_addr3 , b.bill_sending_addr4 , b.bill_sending_addr5 , "
	      + "        b.autopay_indicator , b.bill_apply_flag , b.stat_send_internet , b.e_mail_ebill , b.crt_date , "
	      + "        b.revolve_int_rate , b.line_of_credit_amt , b.payment_rate1 , b.payment_rate2 , b.payment_rate3 , "
	      + "        b.payment_rate4 , b.payment_rate5 , b.payment_rate6 , b.payment_rate7 , b.payment_rate8 , "
	      + "        b.payment_rate9 , b.payment_rate10 , b.payment_rate11 , b.payment_rate12 , b.int_rate_mcode , "
	      + "        b.stmt_cycle , b.acct_status, c.autopay_acct_bank , c.autopay_acct_no , "
	      + "        nvl(d.ttl_amt_bal,0) ttl_amt_bal , nvl(d.min_pay_bal,0) min_pay_bal , "
	      + "        nvl(e.tot_amt_month,0) tot_amt_month, e.adj_eff_start_date , e.adj_eff_end_date , "
	      + "        e.block_reason1 , e.block_reason2, e.block_reason3, e.block_reason4, e.block_reason5 , "
	      + "        nvl((SELECT g.acct_amt_balance from cca_acct_balance_cal g "
	      + "              where g.acno_p_seqno = b.acno_p_seqno and g.acct_type = b.acct_type LIMIT 1),0) AS acct_amt_balance , "
	      + "        nvl((SELECT h.his_purchase_amt FROM act_anal_sub h "
	      + "               JOIN ptr_workday i ON h.acct_month = i.last_acct_month "
	      + "              WHERE h.p_seqno = b.p_seqno AND i.stmt_cycle = b.stmt_cycle LIMIT 1) ,0) as anal_his_purchase_amt "          
          + "   from crd_idno a left join act_acno b on a.id_p_seqno = b.id_p_seqno and b.acno_flag = '1' "
          + "                   left join act_acct_curr c on b.p_seqno = c.p_seqno and c.curr_code = '901' "
          + "                   left join act_acct d on b.p_seqno = d.p_seqno "
          + "                   left join cca_card_acct e on b.acno_p_seqno = e.acno_p_seqno and e.debit_flag <> 'Y' "
          + "  where 1=1 "
          + "    and exists (select 1 FROM crd_card f where a.id_p_seqno = f.id_p_seqno limit 1) "
		  + "  order by id_no Asc "
//			   + commSqlStr.rownum(10000)
   		   ;
   
   openCursor();

   while (fetchTable()) {
      initData();
      procCnt++;
      totalCnt++;
      dspProcRow(10000);

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
      
      acnoPSeqno = colSs("acno_p_seqno");            
      pSeqno = colSs("p_seqno");                      
      acctType = colSs("acct_type");                  
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
//   			acnoCrtDate = colSs("crt_date");                 
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
       //--
       //-cca_acct_balance_cal--
       canUseLimit = colInt("acct_amt_balance");
       if (canUseLimit < 0)
          canUseLimitSign = "-";
       //-act_anal_sub---
       hisPurchseAmt = colInt("anal_his_purchase_amt");
       if (hisPurchseAmt >= 0) consumeSign = " ";
       else consumeSign = "-";

      lbSup = false;
      
      if(acnoPSeqno.length() <= 0) {
    	  lbSup = true;
      }

      //--附卡人找不到帳戶資訊故直接寫檔
      if (lbSup) {
         writeFile();
         continue;
      }
//    selectCcaCardAcct("N");
//		selectActAcctCurrData();
//		selectActAcctData();
      getRCrate();  //selectPtrRcrate();
/*-load_act_acno --
      getLastAcctMonth();
      selectActAnalSub();
*/
//      selectColCpbdue("01");
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

//			calBalance();

      writeFile();      
   }
   closeCursor();
   showLogMessage("I", "", "Crd_idno done , Proc Cnt = [" + procCnt + "]");
}

//-check 有正卡-
int tiCard=-1;
void selectCrdCard() throws Exception {
   lbSup = false;
   if (tiCard <=0) {
      sqlCmd ="select count(*) as sup0_cnt"+
              " from crd_card"+
              " where id_p_seqno =? and sup_flag='0'"
              ;
      tiCard =ppStmtCrt("ti-S-card","");
   }
   ppp(1, idPSeqno);
   sqlSelect(tiCard);
   //-有正卡-
   if (sqlNrow >0 && colInt(0,"sup0_cnt")>0) {
      lbSup=false;
      return;
   }

   lbSup =true;
}
//--查詢VD卡人資料
void selectDbcIdNoData() throws Exception {

   procCnt = 0;
   sqlCmd = " select a.id_no , a.id_p_seqno , a.birthday , a.home_area_code1||a.home_tel_no1 as home_tel_phone , "
          + "        a.office_area_code1||a.office_tel_no1||a.office_tel_ext1 as office_tel_phone , a.staff_flag , "
          + "        a.market_agree_base , a.e_news , a.e_mail_addr , a.cellar_phone , a.marriage , a.business_code , "
          + "        a.sex , a.job_position , a.chi_name , a.company_name , a.other_cntry_code , a.passport_no , "
          + "        a.service_year , a.annual_income/10000 as annual_income , a.education , a.spouse_id_no , "
          + "        a.spouse_birthday , a.spouse_name , a.resident_zip , a.resident_addr1 , a.resident_addr2 , "
          + "        a.resident_addr3 , a.resident_addr4 , a.resident_addr5 , a.mail_zip , a.mail_addr1 , a.mail_addr2 , "
          + "        a.mail_addr3 , a.mail_addr4 , a.mail_addr5 , a.company_zip , a.company_addr1 , a.company_addr2 , "
          + "        a.company_addr3 , a.company_addr4 , a.company_addr5 , a.card_since, "          
          + "        b.p_seqno as acno_p_seqno , b.id_p_seqno , b.bill_sending_zip , b.bill_sending_addr1 , "
          + "        b.bill_sending_addr2 , b.bill_sending_addr3 , b.bill_sending_addr4 , b.bill_sending_addr5 , "
          + "        b.autopay_indicator , b.bill_apply_flag , b.stat_send_internet , b.e_mail_ebill , b.crt_date , "
          + "        b.revolve_int_rate , b.line_of_credit_amt , b.payment_rate1 , b.payment_rate2 , b.payment_rate3 , "
          + "        b.payment_rate4 , b.payment_rate5 , b.payment_rate6 , b.payment_rate7 , b.payment_rate8 , "
          + "        b.payment_rate9 , b.payment_rate10 , b.payment_rate11 , b.payment_rate12 , b.stmt_cycle , "
          + "        b.acct_status , nvl(c.tot_amt_month,0) tot_amt_month , c.adj_eff_start_date , "
          + "        c.adj_eff_end_date , c.block_reason1 , c.block_reason2 , c.block_reason3 , c.block_reason4 , "
          + "        c.block_reason5 "         
          + " from dbc_idno a left join dba_acno b on a.id_p_seqno = b.id_p_seqno "
          + "                 left join cca_card_acct c on b.p_seqno = c.acno_p_seqno "
          + " where 1=1 "
          + " and exists (select 1 from dbc_card g where a.id_p_seqno = g.id_p_seqno limit 1) "
          + " and not exists (select k.id_no FROM crd_idno k where a.id_no = k.id_no) "
          + " order by id_no "
//			   +commSqlStr.rownum(10000)
   ;

   openCursor();

   while (fetchTable()) {
      initData();
      procCnt++;

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
//   			acnoCrtDate = colSs("crt_date");
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
//   			intRateMcode = colInt("int_rate_mcode");
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
      
      //VD卡人只取一筆
      if(lastIdNo.equals(idNo)) {
    	  continue;
      }
      
      totalCnt++;
      dspProcRow(10000);

      if (commString.between(sysDate, adjEffStartDate, adjEffEndDate) == false) {
         //--不在臨調效期內
         totAmtMonth = 0;
         adjEffStartDate = "";
         adjEffEndDate = "";
      }

      //-VD:没有--
//      selectDbaAcnoData();
//      selectCcaCardAcct("Y");

      //selectActAcctCurrData();
      //selectActAcctData();
      getRCrate();  //selectPtrRcrate();
      //selectPtrWorkDay();
/*
      getLastAcctMonth();
      //selectActAnalSub();
//      selectColCpbdue("90");
*/
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
      
      lastIdNo = idNo;
   }
   closeCursor();
   showLogMessage("I", "", "Dbc_idno done , Proc Cnt = [" + procCnt + "]");
}
//-----------

//--查詢帳戶資料--
/*--
int tiAcno=-1;
int tiCcabal=-1;
void selectActAcnoData() throws Exception {
   if (tiAcno <=0) {
      sqlCmd = " select A.acno_p_seqno, A.p_seqno , A.bill_sending_zip , A.bill_sending_addr1 , A.bill_sending_addr2 , A.bill_sending_addr3 , "
              + " A.bill_sending_addr4 , A.bill_sending_addr5 , A.autopay_indicator , A.bill_apply_flag , A.stat_send_internet , "
              + " A.e_mail_ebill , A.crt_date , A.revolve_int_rate , A.line_of_credit_amt , A.payment_rate1 , A.payment_rate2 , "
              + " A.payment_rate3 , A.payment_rate4 , A.payment_rate5 , A.payment_rate6 , A.payment_rate7 , A.payment_rate8 ,"
              + " A.payment_rate9 , A.payment_rate10 , A.payment_rate11 , A.payment_rate12 , A.int_rate_mcode , A.stmt_cycle , A.acct_status , "
              +" 0 as acct_amt_balance"
              +", B.autopay_acct_bank , B.autopay_acct_no"
              +", C.ttl_amt_bal , C.min_pay_bal"
      +" from act_acno A join act_acct_curr B on B.p_seqno=A.acno_p_seqno and B.curr_code='901'"
              +" left join act_acct C on C.p_seqno=A.acno_p_seqno"
      +" where A.id_p_seqno =? and A.acct_type='01'"
      +commSqlStr.rownum(1)
      ;
      tiAcno =ppStmtCrt("ti-S-Acno","");
   }

   //setString(1, idNo + "0");
   ppp(1, idPSeqno);
   sqlSelect(tiAcno);

   if (sqlNrow <= 0) {
      lbSup =true;
      return;
   }
   acnoPSeqno = colSs("acno_p_seqno");
   pSeqno =colSs("p_seqno");
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
   intRateMcode = colInt("int_rate_mcode");
   acctStatus = colSs("acct_status");
   stmtCycle =colSs("stmt_cycle");
   //-acct_curr-
   autopayAcctBank = colSs("autopay_acct_bank");
   autopayAcctNo = colSs("autopay_acct_no");
   //-act_acct-
   ttlAmtBal = colInt("ttl_amt_bal");
   minPayBal = colInt("min_pay_bal");
   //--
//      canUseLimit = colInt("acct_amt_balance");
//      if (canUseLimit < 0)
//         canUseLimitSign = "-";
}
 */
//---

//--查詢委扣資料
int tiAcnoCurr=-1;
void selectActAcctCurrData() throws Exception {
   if (tiAcnoCurr <=0) {
      sqlCmd = " select autopay_acct_bank , autopay_acct_no from act_acct_curr"
      +" where p_seqno = ? and curr_code ='901' "
      ;
      tiAcnoCurr =ppStmtCrt("ti-S-AcnoCurr","");
   }
   ppp(1, acnoPSeqno);
   sqlSelect(tiAcnoCurr);
   if (sqlNrow > 0) {
      autopayAcctBank = colSs("autopay_acct_bank");
      autopayAcctNo = colSs("autopay_acct_no");
   }
}

//--查詢帳務資料
int tiAcct=-1;
void selectActAcctData() throws Exception {
   if (tiAcct <=0) {
      sqlCmd = " select ttl_amt_bal , min_pay_bal from act_acct where p_seqno = ? ";
      tiAcct =ppStmtCrt("ti-S-acct","");
   }
   ppp(1, acnoPSeqno);

   sqlSelect(tiAcct);
   if (sqlNrow > 0) {
      ttlAmtBal = colInt("ttl_amt_bal");
      minPayBal = colInt("min_pay_bal");
   }
}

//--查詢授權帳戶檔
int tiCcaAcct=-1;
void selectCcaCardAcct(String debitFlag) throws Exception {
   if (empty(acnoPSeqno)) {
      return;
   }
   if (tiCcaAcct <=0) {
//      sqlCmd = " select A.tot_amt_month , A.adj_eff_start_date , A.adj_eff_end_date , A.block_reason1 , A.block_reason2 "
//              + ", A.block_reason3 , A.block_reason4 , A.block_reason5"
//              +", B.acct_amt_balance"
//              +" from cca_card_acct A left join cca_acct_balance_cal B"
//              +" on B.id_p_seqno=A.id_p_seqno and B.acno_p_seqno=A.acno_p_seqno"
//              + " where A.acno_p_seqno =? and decode(A.debit_flag,'Y','Y','N') =?"
//      +commSqlStr.rownum(1)
//      ;
      sqlCmd = " select B.acct_amt_balance"
              +" from cca_acct_balance_cal B"
              + " where B.acno_p_seqno =? and B.acct_type =?"
              +commSqlStr.rownum(1)
      ;
      tiCcaAcct =ppStmtCrt("ti-S-ccaAcct","");
   }

//   sqlCmd = " select A.tot_amt_month , A.adj_eff_start_date , A.adj_eff_end_date , A.block_reason1 , A.block_reason2 , "
//           + " A.block_reason3 , A.block_reason4 , A.block_reason5"
//           +", B.autopay_acct_bank , B.autopay_acct_no "
//           + ", C.ttl_amt_bal , C.min_pay_bal "
//           + " from cca_card_acct A join act_acct_curr B on A.acno_p_seqno = B.p_seqno join act_acct C on A.acno_p_seqno = C.p_seqno "
//           + " where A.acno_p_seqno = ? and B.curr_code = '901' ";

   ppp(1, acnoPSeqno);
   ppp(acctType);
//   ppp(nvl(debitFlag,"N"));

   sqlSelect(tiCcaAcct);

   if (sqlNrow > 0) {
//      totAmtMonth = colInt("tot_amt_month");
//      adjEffStartDate = colSs("adj_eff_start_date");
//      adjEffEndDate = colSs("adj_eff_end_date");
//      blockCode1 = colSs("block_reason1");
//      blockCode2 = colSs("block_reason2");
//      blockCode3 = colSs("block_reason3");
//      blockCode4 = colSs("block_reason4");
//      blockCode5 = colSs("block_reason5");
//      if (commString.between(sysDate, adjEffStartDate, adjEffEndDate) == false) {
//         //--不在臨調效期內
//         totAmtMonth = 0;
//         adjEffStartDate = "";
//         adjEffEndDate = "";
//      }
      //--
      canUseLimit = colInt("acct_amt_balance");
      if (canUseLimit < 0) {
         canUseLimitSign = "-";
         canUseLimit = 0 - canUseLimit;
      }
   }
}

//--查詢對應年利率
//int tiRcrate=-1;
void selectPtrRcrate() throws Exception {
   sqlCmd = " select rcrate_day, rcrate_year from ptr_rcrate where 1=1"
           +" order by rcrate_day";
   sqlQuery(dsRcrate,"",null);

//   if (tiRcrate <=0) {
//      sqlCmd = " select rcrate_year from ptr_rcrate where 1=1 and rcrate_day = ? "
//      +commSqlStr.rownum(1);
//      tiRcrate =ppStmtCrt("ti-rcrate","");
//   }
//   ppp(1, revolveIntRate);
//   sqlSelect(tiRcrate);
//   if (sqlNrow > 0) {
//      rcRateYear = colNum("rcrate_year");
//      rcRateYear = rcRateYear * 100;
//   }
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

//--查詢當期新增消費款
int tiAnal=-1;
void selectActAnalSub() throws Exception {
   if (empty(acnoPSeqno) || empty(lastAcctMonth)) {
      return;
   }
   if (tiAnal <=0) {
      //sqlCmd = " select his_purchase_amt from act_anal_sub where p_seqno = ? and acct_month = ? "
      sqlCmd ="select nvl((SELECT CB.acct_amt_balance from cca_acct_balance_cal CB "+
          "       where CB.acno_p_seqno =? LIMIT 1),0) AS acct_amt_balance "+
          ", nvl((SELECT SS.his_purchase_amt FROM act_anal_sub SS "+
          "       WHERE SS.p_seqno=? AND acct_month=? LIMIT 1) ,0) as anal_his_purchase_amt "+
          " FROM "+commSqlStr.sqlDual
      ;
      tiAnal =ppStmtCrt("ti-anal","");
   }
   ppp(1, acnoPSeqno);
   ppp(acnoPSeqno);
   ppp(lastAcctMonth);

   sqlSelect(tiAnal);
   if (sqlNrow > 0) {
      //-cca_acct_balance_cal--
      canUseLimit = colInt("acct_amt_balance");
      if (canUseLimit < 0)
         canUseLimitSign = "-";
      //-act_anal_sub---
      hisPurchseAmt =colInt("anal_his_purchase_amt");
      if (hisPurchseAmt >= 0) consumeSign = " ";
      else consumeSign = "-";
   }

}

//--查詢關帳年月
void selectPtrWorkDay() throws Exception {
   dsCycle.dataClear();
   //sqlCmd = " select last_acct_month from ptr_workday where stmt_cycle = ? ";
   //setString(1, stmtCycle);
//   sqlSelect();
//   if (sqlNrow > 0) {
//      lastAcctMonth = colSs("last_acct_month");
//   }
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
com.DataSet dsCpbd=new DataSet();
com.DataSet dsLiac=new DataSet();
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
