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
 *  2023-1128  V1.00.07  JH          bug-fix
 *  2023-1206  V1.00.08  JH          剔除瘦身卡友
 *  2023-1207  V1.00.09  JH          bug-fix: act_anal_sub
 *  2023-1211  V1.00.10  JH          bug-fix: dsAcno.anal_his...
 *  2024-0104  V1.00.11  JH          path:media/rsk moveTo /crdatacrea/CREDITALL/..
 *****************************************************************************/
package Rsk;

import com.*;

import java.text.DecimalFormat;

public class RskP150 extends BaseBatch {

private final String progname = "每日產RP18卡人檔作業 2024-0104  V1.00.11";
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
   RskP150 proc = new RskP150();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP150 [business_date]");
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

   String ssIdno="";
   sqlCmd ="select distinct substring(id_no,1,1) as idno1"+
       " from crd_idno";
   sqlSelect();
   int ll_Nrow =sqlNrow;
   for (int ii = 0; ii <ll_Nrow ; ii++) {
      ssIdno +=colSs(ii,"idno1");
   }
   printf("process CRD.idno=[%s]",ssIdno);
   for (int ii = 0; ii <ssIdno.length() ; ii++) {
      String lsId=commString.mid(ssIdno,ii,1);
      if (empty(lsId)) continue;
      selectCrdIdNoData(lsId+"%");
   }

   //----------
   ssIdno="";
   sqlCmd ="select distinct substring(id_no,1,1) as idno1"+
       " from dbc_idno";
   sqlSelect();
   ll_Nrow =sqlNrow;
   for (int ii = 0; ii <ll_Nrow ; ii++) {
      ssIdno +=colSs(ii,"idno1");
   }
   printf("process DBC.idno=[%s]",ssIdno);
   for (int ii = 0; ii <ssIdno.length() ; ii++) {
      String lsId=commString.mid(ssIdno,ii,1);
      if (empty(lsId)) continue;
      selectDbcIdNoData(lsId+"%");
   }

   closeOutputText(iiFileNum);

   //-move file-
   String frPath = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String toPath = String.format("/crdatacrea/CREDITALL/%s", fileName);

   if (comc.fileMove(frPath, toPath) == false) {
      printf("ERROR : 檔案[" + fileName + "] moveTo [%s] fail !", toPath);
   }
   else {
      printf("檔案 [" + fileName + "] 已移至 [" + toPath + "]");
   }

//   commFTP = new CommFTP(getDBconnect(), getDBalias());
//   comr = new CommRoutine(getDBconnect(), getDBalias());
//   procFTP();
//   //--BackUp File
//   renameFile();

   dsAcno.dataClear();
   dsAcno =null;

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
//   String lsFile =String.format("/crdatacrea/CREDITALL/%s", fileName);
   String lsFile =getEcsHome()+"/media/rsk/"+fileName;

   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum < 0) {
      printf("[%s] 產檔失敗 !",lsFile);
      okExit(0);
   }

   return;
}

//--查詢信用卡人資料
void selectCrdIdNoData(String aIdno) throws Exception {

   loadActAcno(aIdno);

   sqlCmd = " select id_no , id_p_seqno , birthday "
       +", rpad(home_area_code1,4)||rpad(home_tel_no1,10) as home_tel_phone "
       +", rpad(office_area_code1,4)||rpad(office_tel_no1,10)||rpad(office_tel_ext1,6) as office_tel_phone "
       +", staff_flag , market_agree_base , "
       +" e_news , e_mail_addr , cellar_phone , marriage , business_code , sex , job_position , chi_name , "
       +" company_name , other_cntry_code , passport_no , passport_date , service_year , annual_income/10000 as annual_income , "
       +" education , spouse_id_no , spouse_birthday , spouse_name , resident_zip , resident_addr1 , "
       +" resident_addr2 , resident_addr3 , resident_addr4 , resident_addr5 , mail_zip , mail_addr1 , "
       +" mail_addr2 , mail_addr3 , mail_addr4 , mail_addr5 , company_zip , company_addr1 , company_addr2 , "
       +" company_addr3 , company_addr4 , company_addr5 , graduation_elementarty , card_since"
       +" from crd_idno "
       +" where 1=1 "
       +" and id_no like ?"
       +" AND EXISTS (SELECT 1 FROM crd_card WHERE id_p_seqno=crd_idno.id_p_seqno LIMIT 1) "
       +" order by id_no Asc "
   ;
   ppp(1, aIdno);
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

//      selectCrdCard();

      lbSup =false;
      //selectActAcnoData();
      getActAcnoData();
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
void selectDbcIdNoData(String aIdno) throws Exception {
   loadDbaAcno(aIdno);

   procCnt = 0;
   sqlCmd = " select id_no , id_p_seqno , birthday , home_area_code1||home_tel_no1 as home_tel_phone , "
           + " office_area_code1||office_tel_no1||office_tel_ext1 as office_tel_phone , staff_flag , market_agree_base , "
           + " e_news , e_mail_addr , cellar_phone , marriage , business_code , sex , job_position , chi_name , "
           + " company_name , other_cntry_code , passport_no , service_year , annual_income/10000 as annual_income , "
           + " education , spouse_id_no , spouse_birthday , spouse_name , resident_zip , resident_addr1 , "
           + " resident_addr2 , resident_addr3 , resident_addr4 , resident_addr5 , mail_zip , mail_addr1 , "
           + " mail_addr2 , mail_addr3 , mail_addr4 , mail_addr5 , company_zip , company_addr1 , company_addr2 , "
           + " company_addr3 , company_addr4 , company_addr5 , card_since from dbc_idno"
           +" where 1=1"
      +" and id_no like ?"
       +" AND EXISTS (SELECT 1 FROM dbc_card WHERE id_p_seqno=dbc_idno.id_p_seqno LIMIT 1) "
       +" AND NOT EXISTS (SELECT id_no FROM crd_idno"+
      " WHERE crd_idno.id_no=dbc_idno.id_no and crd_idno.id_no like ?)"
   +" order by id_no"
//			   +commSqlStr.rownum(10000)
   ;

   ppp(1, aIdno);
   ppp(aIdno);
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

      //-VD:没有--
//      selectDbaAcnoData();
//      selectCcaCardAcct("Y");
      getDbaAcnoData();
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
   }
   closeCursor();
   showLogMessage("I", "", "Dbc_idno done , Proc Cnt = [" + procCnt + "]");
}
//-----------
DataSet dsAcno=new DataSet();
void loadActAcno(String aIdno) throws Exception {
   dsAcno.dataClear();

   sqlCmd = " select A.id_p_seqno, A.acct_type, A.acno_p_seqno, A.p_seqno , A.bill_sending_zip , A.bill_sending_addr1 , A.bill_sending_addr2 , A.bill_sending_addr3 , "
       +" A.bill_sending_addr4 , A.bill_sending_addr5 , A.autopay_indicator , A.bill_apply_flag , A.stat_send_internet , "
       +" A.e_mail_ebill , A.crt_date , A.revolve_int_rate , A.line_of_credit_amt , A.payment_rate1 , A.payment_rate2 , "
       +" A.payment_rate3 , A.payment_rate4 , A.payment_rate5 , A.payment_rate6 , A.payment_rate7 , A.payment_rate8 ,"
       +" A.payment_rate9 , A.payment_rate10 , A.payment_rate11 , A.payment_rate12"
       +", A.int_rate_mcode , A.stmt_cycle , A.acct_status "
       +", B.autopay_acct_bank , B.autopay_acct_no"
       +", nvl(C.ttl_amt_bal,0) ttl_amt_bal , nvl(C.min_pay_bal,0) min_pay_bal"
       +", nvl(CC.tot_amt_month,0) tot_amt_month, CC.adj_eff_start_date , CC.adj_eff_end_date "
       +", CC.block_reason1 , CC.block_reason2, CC.block_reason3"
       +", CC.block_reason4, CC.block_reason5 "
       +", nvl((SELECT CB.acct_amt_balance from cca_acct_balance_cal CB "
       +"       where CB.acno_p_seqno =A.acno_p_seqno and CB.acct_type =A.acct_type LIMIT 1)"
       +",0) AS acct_amt_balance "
       +", nvl((SELECT SS.his_purchase_amt FROM act_anal_sub SS JOIN ptr_workday WW ON SS.acct_month=WW.last_acct_month"
       +"      WHERE SS.p_seqno=A.p_seqno AND WW.stmt_cycle=A.stmt_cycle LIMIT 1) "
       +",0) as anal_his_purchase_amt"
       +" from act_acno A join act_acct_curr B on B.p_seqno=A.acno_p_seqno and B.curr_code='901'"
       +" left join act_acct C on C.p_seqno=A.acno_p_seqno"
       +" join crd_idno I on A.id_p_seqno=I.id_p_seqno and A.acno_flag='1' "
       +" LEFT JOIN cca_card_acct CC ON CC.acno_p_seqno=A.acno_p_seqno AND CC.debit_flag<>'Y'"
       +" where 1=1 "
       +" and I.id_no like ? "
       +" order by A.id_p_seqno "
   ;
   printf(" load table act_acno kk=[%s] start...", aIdno);
   sqlQuery(dsAcno, "", new Object[]{aIdno});
   dsAcno.loadKeyData("id_p_seqno");
   printf(" load table act_acno kk[%s].row[%s]", aIdno, dsAcno.rowCount());
}
//---------
int getActAcnoData() throws Exception {
   if (empty(idPSeqno)) return 1;
   int liCnt =dsAcno.getKeyData(idPSeqno);
   if (liCnt <=0) {
      lbSup =true;
      return 1;
   }

   acnoPSeqno = dsAcno.colSs("acno_p_seqno");
   pSeqno =dsAcno.colSs("p_seqno");
   acctType =dsAcno.colSs("acct_type");
   billZip = dsAcno.colSs("bill_sending_zip");
   billAddr1 = dsAcno.colSs("bill_sending_addr1");
   billAddr2 = dsAcno.colSs("bill_sending_addr2");
   billAddr3 = dsAcno.colSs("bill_sending_addr3");
   billAddr4 = dsAcno.colSs("bill_sending_addr4");
   billAddr5 = dsAcno.colSs("bill_sending_addr5");
   autopayIndicator = dsAcno.colSs("autopay_indicator");
   billApplyFlag = dsAcno.colSs("bill_apply_flag");
   statSendInternet = dsAcno.colSs("stat_send_internet");
   eMailEbill = dsAcno.colSs("e_mail_ebill");
//			acnoCrtDate = colSs("crt_date");
   revolveIntRate = dsAcno.colNum("revolve_int_rate");
   lineOfCreditAmt = dsAcno.colInt("line_of_credit_amt");
   paymentRate1 = dsAcno.colSs("payment_rate1");
   paymentRate2 = dsAcno.colSs("payment_rate2");
   paymentRate3 = dsAcno.colSs("payment_rate3");
   paymentRate4 = dsAcno.colSs("payment_rate4");
   paymentRate5 = dsAcno.colSs("payment_rate5");
   paymentRate6 = dsAcno.colSs("payment_rate6");
   paymentRate7 = dsAcno.colSs("payment_rate7");
   paymentRate8 = dsAcno.colSs("payment_rate8");
   paymentRate9 = dsAcno.colSs("payment_rate9");
   paymentRate10 = dsAcno.colSs("payment_rate10");
   paymentRate11 = dsAcno.colSs("payment_rate11");
   paymentRate12 = dsAcno.colSs("payment_rate12");
   intRateMcode = dsAcno.colInt("int_rate_mcode");
   acctStatus = dsAcno.colSs("acct_status");
   stmtCycle =dsAcno.colSs("stmt_cycle");
   //-acct_curr-
   autopayAcctBank = dsAcno.colSs("autopay_acct_bank");
   autopayAcctNo = dsAcno.colSs("autopay_acct_no");
   //-act_acct-
   ttlAmtBal = dsAcno.colInt("ttl_amt_bal");
   minPayBal = dsAcno.colInt("min_pay_bal");
   //-cca_card_acct-
   totAmtMonth = dsAcno.colInt("tot_amt_month");
   adjEffStartDate = dsAcno.colSs("adj_eff_start_date");
   adjEffEndDate = dsAcno.colSs("adj_eff_end_date");
   blockCode1 = dsAcno.colSs("block_reason1");
   blockCode2 = dsAcno.colSs("block_reason2");
   blockCode3 = dsAcno.colSs("block_reason3");
   blockCode4 = dsAcno.colSs("block_reason4");
   blockCode5 = dsAcno.colSs("block_reason5");
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
   hisPurchseAmt =dsAcno.colInt("anal_his_purchase_amt");
   if (hisPurchseAmt >= 0) consumeSign = " ";
   else consumeSign = "-";

   return 0;
}
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
int tiDbacno=-1;
void selectDbaAcnoData() throws Exception {
   if (tiDbacno <=0) {
      sqlCmd = " select p_seqno as acno_p_seqno , bill_sending_zip , bill_sending_addr1 , bill_sending_addr2 , bill_sending_addr3 , "
              + " bill_sending_addr4 , bill_sending_addr5 , autopay_indicator , bill_apply_flag , stat_send_internet , "
              + " e_mail_ebill , crt_date , revolve_int_rate , line_of_credit_amt , payment_rate1 , payment_rate2 , "
              + " payment_rate3 , payment_rate4 , payment_rate5 , payment_rate6 , payment_rate7 , payment_rate8 ,"
              + " payment_rate9 , payment_rate10 , payment_rate11 , payment_rate12  , stmt_cycle , acct_status"
      +" from dba_acno "
      +" where id_p_seqno =?acct_holder_id =?"
      +" order by acct_holder_id, p_seqno desc"
      +commSqlStr.rownum(1)
      ;
      tiDbacno =ppStmtCrt("ti-dbacno","");
   }

   ppp(1, idPSeqno);
   sqlSelect(tiDbacno);

   if (sqlNrow > 0) {
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
//			intRateMcode = colInt("int_rate_mcode");
      acctStatus = colSs("acct_status");

   }
   else {
      lbSup = true;
   }
}
//=============================
DataSet dsDbacno=new DataSet();
void loadDbaAcno(String aIdno) throws Exception {
   dsDbacno.dataClear();

   sqlCmd = "select A.p_seqno as acno_p_seqno, A.id_p_seqno"
      +", A.bill_sending_zip, A.bill_sending_addr1, A.bill_sending_addr2"
      +", A.bill_sending_addr3, A.bill_sending_addr4, A.bill_sending_addr5"
      +", A.autopay_indicator, A.bill_apply_flag, A.stat_send_internet"
      +", A.e_mail_ebill, A.crt_date, A.revolve_int_rate, A.line_of_credit_amt"
      +", A.payment_rate1, A.payment_rate2, A.payment_rate3, A.payment_rate4"
      +", A.payment_rate5, A.payment_rate6, A.payment_rate7, A.payment_rate8 "
      +", A.payment_rate9, A.payment_rate10, A.payment_rate11, A.payment_rate12 "
      +", A.stmt_cycle, A.acct_status "
      +", nvl(CC.tot_amt_month,0) tot_amt_month, CC.adj_eff_start_date , CC.adj_eff_end_date "
      +", CC.block_reason1 , CC.block_reason2, CC.block_reason3"
      +", CC.block_reason4, CC.block_reason5 "
//      +", nvl((SELECT CB.acct_amt_balance from cca_acct_balance_cal CB "
//      +"       where CB.acno_p_seqno =A.p_seqno and CB.acct_type =A.acct_type LIMIT 1)"
//      +",0) AS acct_amt_balance "
      +" from dba_acno A "
      +" LEFT JOIN cca_card_acct CC ON CC.acno_p_seqno=A.p_seqno AND CC.debit_flag='Y' "
      +" where A.acct_holder_id LIKE ? "
      +" AND NOT EXISTS (SELECT id_no FROM crd_idno "
      +" WHERE crd_idno.id_no=A.acct_holder_id) "
      +" order by A.acct_holder_id, A.p_seqno "
   ;
   printf(" load table dba_acno kk=[%s].Start...", aIdno);
   sqlQuery(dsDbacno, "", new Object[]{aIdno});
   dsDbacno.loadKeyData("id_p_seqno");
   printf(" load table dba_acno kk=[%s].row[%s]", aIdno, dsDbacno.rowCount());
}
//---------
int getDbaAcnoData() throws Exception {
   if (empty(idPSeqno)) return 1;
   int liCnt =dsDbacno.getKeyData(idPSeqno);
   if (liCnt <=0) {
      lbSup =true;
      return 1;
   }

   acnoPSeqno = dsDbacno.colSs("acno_p_seqno");
   pSeqno =dsDbacno.colSs("acno_p_seqno");
   billZip = dsDbacno.colSs("bill_sending_zip");
   billAddr1 = dsDbacno.colSs("bill_sending_addr1");
   billAddr2 = dsDbacno.colSs("bill_sending_addr2");
   billAddr3 = dsDbacno.colSs("bill_sending_addr3");
   billAddr4 = dsDbacno.colSs("bill_sending_addr4");
   billAddr5 = dsDbacno.colSs("bill_sending_addr5");
   autopayIndicator = dsDbacno.colSs("autopay_indicator");
   billApplyFlag = dsDbacno.colSs("bill_apply_flag");
   statSendInternet = dsDbacno.colSs("stat_send_internet");
   eMailEbill = dsDbacno.colSs("e_mail_ebill");
//			acnoCrtDate = colSs("crt_date");
   revolveIntRate = dsDbacno.colNum("revolve_int_rate");
   lineOfCreditAmt = dsDbacno.colInt("line_of_credit_amt");
   paymentRate1 = dsDbacno.colSs("payment_rate1");
   paymentRate2 = dsDbacno.colSs("payment_rate2");
   paymentRate3 = dsDbacno.colSs("payment_rate3");
   paymentRate4 = dsDbacno.colSs("payment_rate4");
   paymentRate5 = dsDbacno.colSs("payment_rate5");
   paymentRate6 = dsDbacno.colSs("payment_rate6");
   paymentRate7 = dsDbacno.colSs("payment_rate7");
   paymentRate8 = dsDbacno.colSs("payment_rate8");
   paymentRate9 = dsDbacno.colSs("payment_rate9");
   paymentRate10 = dsDbacno.colSs("payment_rate10");
   paymentRate11 = dsDbacno.colSs("payment_rate11");
   paymentRate12 = dsDbacno.colSs("payment_rate12");
//			intRateMcode = colInt("int_rate_mcode");
   stmtCycle =dsDbacno.colSs("stmt_cycle");
   acctStatus = dsDbacno.colSs("acct_status");
   //-cca_card_acct-
   totAmtMonth = dsDbacno.colInt("tot_amt_month");
   adjEffStartDate = dsDbacno.colSs("adj_eff_start_date");
   adjEffEndDate = dsDbacno.colSs("adj_eff_end_date");
   blockCode1 = dsDbacno.colSs("block_reason1");
   blockCode2 = dsDbacno.colSs("block_reason2");
   blockCode3 = dsDbacno.colSs("block_reason3");
   blockCode4 = dsDbacno.colSs("block_reason4");
   blockCode5 = dsDbacno.colSs("block_reason5");

   if (commString.between(sysDate, adjEffStartDate, adjEffEndDate) == false) {
      //--不在臨調效期內
      totAmtMonth = 0;
      adjEffStartDate = "";
      adjEffEndDate = "";
   }
   //-cca_acct_balance_cal--
   //-VD無可用餘顑-
//   canUseLimit = colInt("acct_amt_balance");
//   if (canUseLimit < 0) {
//      canUseLimitSign = "-";
//      canUseLimit =0 - canUseLimit;
//   }
   //--
   return 0;
}
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
//-----------
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
