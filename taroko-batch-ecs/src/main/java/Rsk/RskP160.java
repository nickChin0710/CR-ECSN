/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-02-07  V1.00.00  Alex        initial                                  *
 *  112-03-25  V1.00.01  Alex        可用餘額讀取方式變更							 *
 *  112/04-14  V1.00.02  Alex        讀取卡片可用餘額修正                                                 *
 *  112/04/19  V1.00.03  Alex        修正執行處理完畢後關閉檔案
 *  2023-0823 V1.00.04  JH           效能改善  *
 *  2023-0824 V1.00.05   Alex        acct_amt_balance 補上 nvl          *
 *  ???待確認  2023-1024 V1.00.06   JH          卡片額度=子片額度/帳戶額度
 *  2024-0104 V1.00.07  JH    ./media/rsk move /....
 *****************************************************************************/
package Rsk;

import java.text.DecimalFormat;

import com.*;

import Cca.CalBalance;

public class RskP160 extends BaseBatch {

private final String progname = "每日產RP18卡片檔作業 2024-0104 V1.00.07";
//CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
//CommString commString = new CommString();
//CommDate commDate = new CommDate();
CommFTP commFTP = null;
CommRoutine comr = null;
CalBalance calBal = null;

final String fileName = "RP18VIEWCARD.txt";
private int iiFileNum = 0;

//--共用欄位
String cardNo = "";
String groupCode = "";
String acctType = "";
String idNo = "";
String idPSeqno = "";
String corpPSeqno = "";
String acnoPSeqno = "";
String newEndDate = "";
String supFlag = "";
String regBankNo = "";
String majorIdNo = "";
double indivCrdLmt = 0.0;
double cardLimit = 0.0;
double cardAdjLimit = 0.0;
String cardAdjDate1 = "";
String cardAdjDate2 = "";
String normalFlag = "";
String consumeFlag = "";
String oppostDate = "";
String oppostReason = "";
String currentCode = "";
String comboAcctNo = "";
String lastConsumeDate = "";
String activateFlag = "";
String issueDate = "";
String embossData = "";
String promptDept = "";
String rmCarNo = "";
String introduceId = "";
String sonCardFlag = "";
String expireChgFlag = "";
String preCashFlag = "";
String majorIdPSeqno = "";
String majorEngName = "";
String currCode = "";
int cardCanUse = 0;
int acctCanUse = 0;
//--帳戶相關 VD不提供
String autopayAcctBank = "";
String autopayAcctNo = "";
String autopayIndicator = "";
String billApplyFlag = "";
String statSendInternet = "";
String eMailBill = "";
double ttlAmtBal = 0.0;
String purchaseSign = "";
String lastAcctMonth = "";
double hisPurchaseAmt = 0.0;
String cardSince = "";
double revolveIntRate = 0.0;
double rcRateYear = 0.0;
double lineOfCreditAmt = 0.0;
String canUseLimitSign = "";
double canUseLimit = 0.0;
String[] payRate =new String[12];
int overRateCnt = 0;
int intRateMcode = 0;
double minPayBal = 0.0;
double cpbduePeriod = 0.0;
String acctStatus = "";
String cpbdueOwnerBank = "";
String cpbdueBankType = "";
String cpbdueType = "";
String cpbdueTcbType = "";
double cpbdueRate = 0.0;
String liacStatus = "";
double totAmtMonth = 0.0;
String adjEffStartDate = "";
String adjEffEndDate = "";
String blockCode1 = "";
String blockCode2 = "";
String blockCode3 = "";
String blockCode4 = "";
String blockCode5 = "";
String idStatus = "";
String idResult = "";
String stmtCycle = "";
int procCntCRD = 0;
int procCntDBC=0;

public static void main(String[] args) {
   RskP160 proc = new RskP160();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : RskP160 [business_date]");
      okExit(0);
   }

   dbConnect();
   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }
//		calBal = new CalBalance(getDBconnect(), getDBalias());

   checkOpen();

   loadCardBin();
   selectPtrWorkDay();
   selectPtrRcrate();
   selectColCpbdue();
   selectColLiacNego();
   //-all acno_flag<>1 acno-
   loadActAcno2();

   for (int ii = 0; ii <dsBin.rowCount() ; ii++) {
      dsBin.listCurr(ii);
      String ls_binNo=dsBin.colSs("bin_no");
      if (dsBin.colEq("vd_flag","D")) {
         printf(" process dbc_card like [%s]-----------",ls_binNo);
         selectDbcCardData(ls_binNo);
      }
      else {
         printf(" process crd_card like [%s]-----------",ls_binNo);
         selectCrdCardData(ls_binNo);
      }
   }

   closeOutputText(iiFileNum);

   printf(" end Process CRD_CARD.row[%s], DBC_CARD.row[%s]", procCntCRD, procCntDBC);

   dsAcno.dataClear();
   dsAcno2.dataClear();
   dsBin.dataClear();
   dsCycle.dataClear();
   dsRcrate.dataClear();
   dsLiac.dataClear();
   dsCpbdue.dataClear();

   //-move file-
   String frPath = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
   String toPath = String.format("/crdatacrea/CREDITALL/%s", fileName);
   if (comc.fileMove(frPath, toPath) == false) {
      printf("ERROR : 檔案[" + fileName + "] moveTo [%s] fail !", toPath);
   }
   else {
      printf("檔案 [" + fileName + "] 已移至 [" + toPath + "]");
   }

   //-檔案太大直接放在FTP目錄-
//		commFTP = new CommFTP(getDBconnect(), getDBalias());
//		comr = new CommRoutine(getDBconnect(), getDBalias());
//		procFTP();
//		//--BackUp File
//		renameFile();

   endProgram();

}

//=============
com.DataSet dsBin = new DataSet();
void loadCardBin() throws Exception {
   dsBin.dataClear();

   sqlCmd = "SELECT 'C' vd_flag, substr(card_no,1,6) AS bin_no , count(*) as card_cnt"
             + " FROM crd_card "
             + " WHERE 1=1 "
//             +" and substr(card_no,1,6) in (356763)"  //('356713','356763')"
             + " GROUP BY substr(card_no,1,6) "
             + " UNION "
             + " SELECT 'D' vd_flag, substr(card_no,1,8) AS bin_no , count(*) as card_cnt"
             + " FROM dbc_card "
             + " WHERE 1=1 "
//             +" and substr(card_no,1,8) in ('46019960','46019961')"
             + " GROUP BY substr(card_no,1,8)"
             + " ORDER BY 1,2"
   ;

   sqlQuery(dsBin, "", null);
   printf("loadCardBin row[%s]", dsBin.rowCount());
}


void selectCrdCardData(String aBinNo) throws Exception {
   if (empty(aBinNo)) {
      printf(" crd_card.Parm.bin_no is empty");
      return;
   }

   //loadActAcno(aBinNo);

   sqlCmd = "select A.card_no , A.group_code , A.acct_type, A.id_p_seqno, A.acno_p_seqno "
             +", A.major_id_p_seqno, A.corp_p_seqno, A.current_code "
//             +", uf_idno_id2(A.id_p_seqno,A.acct_type) as id_no "
             +", (select id_no from crd_idno where crd_idno.id_p_seqno=A.id_p_seqno) as id_no "
//             +", substr(A.new_end_date,5,2)||substr(A.new_end_date,3,2) as new_end_date "
             +", A.eng_name, A.new_end_date "
             +", A.sup_flag , A.reg_bank_no "
//             +", decode(A.acct_type,'01',uf_idno_id2(A.major_id_p_seqno,A.acct_type),A.corp_no) as major_id_no ,";
             +", decode(A.acno_flag,'1','',A.corp_no) as major_id_no "
             +", A.acno_flag, A.indiv_crd_lmt "
//             +", decode(A.current_code,'0','1','0') as normal_flag "
             +", A.oppost_date , A.oppost_reason "
             +", A.combo_acct_no , A.last_consume_date , A.activate_flag "
             +", A.issue_date , A.emboss_data , A.promote_dept "
             +", A.introduce_id , A.son_card_flag , A.expire_chg_flag "
             +", A.apply_atm_flag , A.curr_code "
             +", nvl(B.card_amt_balance,0) as card_amt_balance "
             +", CB.card_adj_limit, CB.card_adj_date1 , CB.card_adj_date2 "
             +", (SELECT rm_carno FROM cms_roadmaster WHERE CMS_ROADMASTER.card_no=A.card_no AND rm_status IN ('1','2','3') LIMIT 1) AS rm_carno "
             +" from crd_card A "
             +" left join cca_card_base CB on CB.card_no=A.card_no "
             +"  left join cca_card_balance_cal B on A.card_no = B.card_no "
             +" where 1=1 "
             +" and A.card_no like ?"
    ;

   ppp(1, aBinNo+"%");
   openCursor();

   int ll_procCnt=0;
   while (fetchTable()) {
      initData();
      procCntCRD++;
      ll_procCnt++;
      totalCnt++;
      dspProcRow(10000);

      cardNo = colSs("card_no");
      groupCode = colSs("group_code");
      acctType = colSs("acct_type");
      idNo = colSs("id_no");
      idPSeqno = colSs("id_p_seqno");
      newEndDate = colSs("new_end_date");
// +", substr(A.new_end_date,5,2)||substr(A.new_end_date,3,2) as new_end_date "
      newEndDate =commString.mid(newEndDate,4,2)+commString.mid(newEndDate,2,2);
      supFlag = colSs("sup_flag");
      regBankNo = colSs("reg_bank_no");
      majorIdNo = colSs("major_id_no");
      majorEngName =colSs("eng_name");
      indivCrdLmt = colNum("indiv_crd_lmt");
// +", decode(A.current_code,'0','1','0') as normal_flag "
      normalFlag = (colEq("current_code","0")) ? "1" :"0";
      oppostDate = colSs("oppost_date");
      oppostReason = colSs("oppost_reason");
      currentCode = colSs("current_code");
      comboAcctNo = colSs("combo_acct_no");
      lastConsumeDate = colSs("last_consume_date");
      activateFlag = colSs("activate_flag");
      issueDate = colSs("issue_date");
      embossData = colSs("emboss_data");
      promptDept = colSs("prompt_dept");
      introduceId = colSs("introduce_id");
      sonCardFlag = colSs("son_card_flag");
      expireChgFlag = colSs("expire_chg_flag");
      preCashFlag = colSs("apply_atm_flag");
      majorIdPSeqno = colSs("major_id_p_seqno");
      currCode = colSs("curr_code");
      corpPSeqno = colSs("corp_p_seqno");
      acnoPSeqno = colSs("acno_p_seqno");
      cardCanUse = colInt("card_amt_balance");
      //-cca_card_base-
      cardAdjLimit = colNum("card_adj_limit");
      cardAdjDate1 = colSs("card_adj_date1");
      cardAdjDate2 = colSs("card_adj_date2");
      //-cms_roadmaster-
      rmCarNo = colSs("rm_carno");
      //---------
      String ls_acnoFlag =colNvl("acno_flag","1");

      //selectCcaCardBase();
      //selectCmsRoadMaster();
      //-附卡正卡人IDNO,eng_name-
      if (eq(idPSeqno,majorIdPSeqno)) {
         majorIdNo =idNo;
      }
      else {
         selectCrdIdNo();
      }
      selectCrdCorp();
      //selectActAcno();
      if (eq(ls_acnoFlag,"1")) {
         //getActAcno();
      }
      else {
         getActAcno2();
//      selectCcaCardAcct();
//      selectActAcctCurr();
         getLastAcctMonth();  //selectPtrWorkDay();
         selectActAcct();
//      selectActAnalSub();
         //selectColCpbdue();
         getColCpbdue();
         getColLiacNego();
         getRCrate();
      }

      if (eq("Y",sonCardFlag)) {
         if (commString.between(sysDate, cardAdjDate1, cardAdjDate2))
            cardLimit = cardAdjLimit;
         else cardLimit = indivCrdLmt;
         canUseLimit = cardCanUse;
      } else {
         cardLimit = lineOfCreditAmt;
         canUseLimit = acctCanUse;
      }

      if (canUseLimit < 0)
         canUseLimitSign = "-";

      calOverCnt();

      //--逾期註記 > 0 , 才有逾期金額
      if (intRateMcode == 0) {
         minPayBal = 0;
      }

      //--cpbdueType : 1. 銀行公會; 2. 本行協商
      if (eq("1",cpbdueType)) {
         cpbdueTcbType = "";
      } else if (eq("2",cpbdueType)) {
         cpbdueBankType = "";
      }

      //--判斷6個月內有無消費
      if (commDate.monthsBetween(sysDate, lastConsumeDate) <= 6) {
         consumeFlag = "1";
      }

//			calBalance();

      writeFile();

   }
   closeCursor();
   printf(" -- Crd_card[%s] done , Proc Cnt[%s]", aBinNo, ll_procCnt);
}
//====================
void selectDbcCardData(String a_binNo) throws Exception {
   if (empty(a_binNo)) {
      printf(" proc DBC_CARD.bin_no is empty");
      return;
   }

   sqlCmd = "select card_no , group_code , acct_type"
//             +", ecscrdb.uf_idno_id2(id_p_seqno,acct_type) as id_no"
             +", (select id_no from dbc_idno where dbc_idno.id_p_seqno=dbc_card.id_p_seqno) as id_no"
             +", p_seqno as acno_p_seqno, current_code"
             +", eng_name, new_end_date "
//             +", substr(new_end_date,5,2)||substr(new_end_date,3,2) as new_end_date "
             +", sup_flag , reg_bank_no"
//             +", ecscrdb.uf_idno_id2(major_id_p_seqno,acct_type) as major_id_no "
             +", 0 as indiv_crd_lmt "
//             +", decode(current_code,'0','1','0') as normal_flag "
             +", oppost_date , oppost_reason "
             +", acct_no as combo_acct_no, last_consume_date, activate_flag "
             +", issue_date, emboss_data, promote_dept, id_p_seqno "
             +", introduce_id , son_card_flag , expire_chg_flag , apply_atm_flag "
             +", major_id_p_seqno, corp_p_seqno "
             +" from dbc_card "
             +" where 1=1 "
             +" and card_no like ?"
    ;
//		sqlCmd += " fetch first 10 rows only ";

   ppp(1, a_binNo+"%");
   openCursor();

   int ll_procCnt=0;
   while (fetchTable()) {
      initData();
      procCntDBC++;
      ll_procCnt++;
      totalCnt++;
      dspProcRow(10000);

      cardNo = colSs("card_no");
      groupCode = colSs("group_code");
      acctType = colSs("acct_type");
      idNo = colSs("id_no");
      idPSeqno = colSs("id_p_seqno");
      // +", substr(new_end_date,5,2)||substr(new_end_date,3,2) as new_end_date "
      newEndDate = colSs("new_end_date");
      newEndDate =commString.mid(newEndDate,4,2)+commString.mid(newEndDate,2,2);
      supFlag = colSs("sup_flag");
      regBankNo = colSs("reg_bank_no");
      majorIdNo = idNo;  //colSs("major_id_no");
      majorEngName =colSs("eng_name");
      indivCrdLmt = colNum("indiv_crd_lmt");
// +", decode(current_code,'0','1','0') as normal_flag "
      normalFlag =(colEq("current_code","0")) ? "1" :"0";
      oppostDate = colSs("oppost_date");
      oppostReason = colSs("oppost_reason");
      currentCode = colSs("current_code");
      comboAcctNo = colSs("combo_acct_no");
      lastConsumeDate = colSs("last_consume_date");
      activateFlag = colSs("activate_flag");
      issueDate = colSs("issue_date");
      embossData = colSs("emboss_data");
      promptDept = colSs("prompt_dept");
      introduceId = colSs("introduce_id");
      sonCardFlag = colSs("son_card_flag");
      expireChgFlag = colSs("expire_chg_flag");
      preCashFlag = colSs("apply_atm_flag");
      majorIdPSeqno = colSs("major_id_p_seqno");
      currCode = colSs("curr_code");
      corpPSeqno = colSs("corp_p_seqno");
      acnoPSeqno = colSs("acno_p_seqno");

      getColCpbdue();
      getColLiacNego();
      getRCrate();
      //--cpbdueType : 1. 銀行公會; 2. 本行協商
      if (eq("1",cpbdueType)) {
         cpbdueTcbType = "";
      } else if (eq("2",cpbdueType)) {
         cpbdueBankType = "";
      }

      //--判斷6個月內有無消費
      if (commDate.monthsBetween(sysDate, lastConsumeDate) <= 6) {
         consumeFlag = "1";
      }

//			calBalance();

      writeFile();
   }
   closeCursor();
   printf(" -- Dbc_card[%s] done , Proc Cnt[%s]", a_binNo, ll_procCnt);
}

//--------
int tiMidno=-1;
void selectCrdIdNo() throws Exception {
   if (tiMidno <=0) {
      sqlCmd = "select id_no, eng_name as major_eng_name from crd_idno where id_p_seqno = ? ";
      tiMidno =ppStmtCrt("ti-Midno","");
   }
   ppp(1, majorIdPSeqno);
   daoTid ="idno.";
   sqlSelect(tiMidno);
   if (sqlNrow > 0) {
      majorEngName = colSs("idno.major_eng_name");
      if (empty(majorIdNo)) majorIdNo =colSs("idno.id_no");
   }
}
//===========
com.DataSet dsAcno=new DataSet();
void loadActAcno(String a_binNo) throws Exception {
   dsAcno.dataClear();
   if (empty(a_binNo)) return;

   sqlCmd = "select DISTINCT A.acno_p_seqno, A.line_of_credit_amt"
             + ", nvl(B.acct_amt_balance,0) as acct_amt_balance "
             + " from act_acno A "
             + "  join crd_card C on A.acno_p_seqno=C.acno_p_seqno"
             + "  left join cca_acct_balance_cal B on A.acno_p_seqno = B.acno_p_seqno"
//             +"  left join cca_card_acct CC on CC.acno_p_seqno=A.acno_p_seqno "
             + " where C.card_no like ? "
             +" and C.acno_flag in ('1','')";

   sqlQuery(dsAcno, "", new Object[]{a_binNo + "%"});
   dsAcno.loadKeyData("acno_p_seqno");
   printf(" -->load act_acno-1.bin_no[%s], row[%s]", a_binNo, dsAcno.rowCount());
}
//--------
void getActAcno() throws Exception {
   if (empty(acnoPSeqno)) return;

   int liCnt =dsAcno.getKeyData(acnoPSeqno);
   if (liCnt <=0) return;

   lineOfCreditAmt = dsAcno.colNum("line_of_credit_amt");
   acctCanUse = dsAcno.colInt("acct_amt_balance");
   //-cca_card_acct-
   totAmtMonth = dsAcno.colNum("tot_amt_month");
   adjEffStartDate = dsAcno.colSs("adj_eff_start_date");
   adjEffEndDate = dsAcno.colSs("adj_eff_end_date");
}
//========
com.DataSet dsAcno2=new DataSet();
void loadActAcno2() throws Exception {
   dsAcno2.dataClear();

   sqlCmd = "select DISTINCT A.acno_p_seqno, A.autopay_indicator, A.bill_apply_flag, A.stat_send_internet"
             + ", A.e_mail_ebill , A.revolve_int_rate "
             + ", A.line_of_credit_amt"
             + ", A.payment_rate1 , A.payment_rate2 , A.payment_rate3"
             + ", A.payment_rate4 , A.payment_rate5 , A.payment_rate6 "
             + ", A.payment_rate7 , A.payment_rate8 , A.payment_rate9 "
             + ", A.payment_rate10 , A.payment_rate11 , A.payment_rate12 "
             + ", A.int_rate_mcode , A.acct_status , A.stmt_cycle "
             + ", nvl(B.acct_amt_balance,0) as acct_amt_balance "
             +", AC.autopay_acct_bank , AC.autopay_acct_no "
             +", CC.tot_amt_month , CC.adj_eff_start_date , CC.adj_eff_end_date"
             +", CC.block_reason1, CC.block_reason2, CC.block_reason3 "
             +", CC.block_reason4, CC.block_reason5 "
             + " from act_acno A "
             + "  left join cca_acct_balance_cal B on A.acno_p_seqno = B.acno_p_seqno"
             +"  left join cca_card_acct CC on CC.acno_p_seqno=A.acno_p_seqno "
             +"  LEFT JOIN act_acct_curr AC ON AC.p_seqno=A.p_seqno AND AC.CURR_CODE='901' "
             + " where 1=1 "
             +" and A.acno_flag not in ('1')";

   sqlQuery(dsAcno2, "", null);
   dsAcno2.loadKeyData("acno_p_seqno");
   printf(" -->load act_acno<>1, row[%s]", dsAcno2.rowCount());
}
//--------
void getActAcno2() throws Exception {
   if (empty(acnoPSeqno)) return;

   int liCnt =dsAcno2.getKeyData(acnoPSeqno);
   if (liCnt <=0) return;

   autopayIndicator = dsAcno2.colSs("autopay_indicator");
   billApplyFlag = dsAcno2.colSs("bill_apply_flag");
   statSendInternet = dsAcno2.colSs("stat_send_internet");
   eMailBill = dsAcno2.colSs("e_mail_ebill");
   revolveIntRate = dsAcno2.colNum("revolve_int_rate");
   lineOfCreditAmt = dsAcno2.colNum("line_of_credit_amt");
   payRate[0] = dsAcno2.colSs("payment_rate1");
   payRate[1] = dsAcno2.colSs("payment_rate2");
   payRate[2] = dsAcno2.colSs("payment_rate3");
   payRate[3] = dsAcno2.colSs("payment_rate4");
   payRate[4] = dsAcno2.colSs("payment_rate5");
   payRate[5] = dsAcno2.colSs("payment_rate6");
   payRate[6] = dsAcno2.colSs("payment_rate7");
   payRate[7] = dsAcno2.colSs("payment_rate8");
   payRate[8] = dsAcno2.colSs("payment_rate9");
   payRate[9] = dsAcno2.colSs("payment_rate10");
   payRate[10] = dsAcno2.colSs("payment_rate11");
   payRate[11] = dsAcno2.colSs("payment_rate12");
   intRateMcode = dsAcno2.colInt("int_rate_mcode");
   acctStatus = dsAcno2.colSs("acct_status");
   stmtCycle = dsAcno2.colSs("stmt_cycle");
   acctCanUse = dsAcno2.colInt("acct_amt_balance");
   //-cca_card_acct-
   totAmtMonth = dsAcno2.colNum("tot_amt_month");
   adjEffStartDate = dsAcno2.colSs("adj_eff_start_date");
   adjEffEndDate = dsAcno2.colSs("adj_eff_end_date");
   blockCode1 = dsAcno2.colSs("block_reason1");
   blockCode2 = dsAcno2.colSs("block_reason2");
   blockCode3 = dsAcno2.colSs("block_reason3");
   blockCode4 = dsAcno2.colSs("block_reason4");
   blockCode5 = dsAcno2.colSs("block_reason5");
   //-act_acct_curr---
   autopayAcctBank = dsAcno2.colSs("autopay_acct_bank");
   autopayAcctNo = dsAcno2.colSs("autopay_acct_no");
   //-------
}
//----------
int tiAcct=-1;
void selectActAcct() throws Exception {
   if (tiAcct <=0) {
      sqlCmd = "select A.ttl_amt_bal , A.min_pay_bal "
                +" from act_acct A "
                +" left join act_anal_sub B on B.p_seqno=A.p_seqno and B.acct_month =?"
                +" where A.p_seqno = ?  ";
      tiAcct =ppStmtCrt("tiAcct","");
   }
   ppp(1, lastAcctMonth);
   ppp(acnoPSeqno);
   sqlSelect(tiAcct);
   if (sqlNrow >0) {
      ttlAmtBal = colNum("ttl_amt_bal");
      minPayBal = colNum("min_pay_bal");
      hisPurchaseAmt = colNum("his_purchse_amt");
      if (hisPurchaseAmt < 0) {
         purchaseSign = "-";
      }
   }
}

//-------
int tiCorp=-1;
void selectCrdCorp() throws Exception {
   if (empty(corpPSeqno)) {
      return;
   }

   if (tiCorp <=0) {
      sqlCmd = "select card_since from crd_corp where corp_p_seqno = ? ";
      tiCorp =ppStmtCrt("tiCorp","");
   }
   ppp(1, corpPSeqno);

   sqlSelect(tiCorp);
   if (sqlNrow > 0) {
      cardSince = colSs("card_since");
   }
}
//---------
com.DataSet dsCpbdue=new DataSet();
void selectColCpbdue() throws Exception {

   sqlCmd = "select cpbdue_id_p_seqno||'-'||cpbdue_acct_type as kk_data"
             + ", cpbdue_period , cpbdue_owner_bank, cpbdue_type"
             + ", cpbdue_bank_type , cpbdue_tcb_type "
             + ", cpbdue_rate "
             + " from col_cpbdue "
             + " where 1=1";
   //cpbdue_id_p_seqno = ? and cpbdue_acct_type = ? ";

   sqlQuery(dsCpbdue, "", null);
   dsCpbdue.loadKeyData("kk_data");
   printf(" load col_cpbdue row[%s]", dsCpbdue.rowCount());
}
//------
void getColCpbdue() throws Exception {
   int liCnt =dsCpbdue.getKeyData(idPSeqno+"-"+acctType);
   if (liCnt <=0) return;

   cpbduePeriod = dsCpbdue.colNum("cpbdue_period");
   cpbdueOwnerBank = dsCpbdue.colSs("cpbdue_owner_bank");
   cpbdueType = dsCpbdue.colSs("cpbdue_type");
   cpbdueBankType = dsCpbdue.colSs("cpbdue_bank_type");
   cpbdueTcbType = dsCpbdue.colSs("cpbdue_tcb_type");
   cpbdueRate = dsCpbdue.colNum("cpbdue_rate");
   cpbdueRate = cpbdueRate * 100;
}
//-------
com.DataSet dsLiac=new DataSet();
void selectColLiacNego() throws Exception {
   sqlCmd = "SELECT id_p_seqno, liac_status " +
             " FROM col_liac_nego " +
             " WHERE 1=1 " +
             " ORDER BY id_p_seqno, CRT_DATE , CRT_TIME"
   ;
   sqlQuery(dsLiac, "", null);
   dsLiac.loadKeyData("id_p_seqno");
   printf(" load col_liac_nego row[%s]", dsLiac.rowCount());
}
//---
void getColLiacNego() throws Exception {
   if (empty(idPSeqno)) return;

   int liCnt =dsLiac.getKeyData(idPSeqno);
   if (liCnt >0) {
      liacStatus = dsLiac.colSs("liac_status");
   }
}
//==========================
//--查詢關帳年月----
com.DataSet dsCycle=new DataSet();
void selectPtrWorkDay() throws Exception {
   dsCycle.dataClear();
   sqlCmd ="select stmt_cycle, last_acct_month"
            +" from ptr_workday"
            +" order by stmt_cycle"
   ;
   daoTable ="ptr_workday";
   sqlQuery(dsCycle, "", null);
}
//------
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
//===========================
//--查詢對應年利率
com.DataSet dsRcrate=new DataSet();
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
//==============================
void calOverCnt() throws Exception {
   for (int ii = 0; ii < 12; ii++) {
      if (commString.ss2int(payRate[ii]) > 1)
         overRateCnt++;
   }

}

void writeFile() throws Exception {
   StringBuffer tempBuf = new StringBuffer();
   String tempAmt = "", newLine = "\r\n", cutSign = ";";
   ;
   DecimalFormat df = new DecimalFormat("0");

   if ("01".equals(acctType)) {
      clearData01();
   } else if ("90".equals(acctType)) {
      clearData90();
   }

   tempBuf.append(comc.fixLeft(cardNo, 16));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(groupCode, 4));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(acctType, 2));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(idNo, 11));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(newEndDate, 4));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(supFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(regBankNo, 4));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(majorIdNo, 11));
   tempBuf.append(cutSign);
   tempAmt = df.format(cardLimit);
   tempAmt = commString.lpad(tempAmt, 10, "0");
//		tempAmt = commString.lpad(Double.toString(cardLimit), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(normalFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(consumeFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(oppostDate, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(currentCode, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(oppostReason, 2));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(comboAcctNo, 16));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(lastConsumeDate, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(activateFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(issueDate, 8));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(embossData, 20));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(promptDept, 11));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(rmCarNo, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(introduceId, 11));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(promptDept, 11));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(sonCardFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(expireChgFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(preCashFlag, 1));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(majorEngName, 25));
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
   tempBuf.append(comc.fixLeft(eMailBill, 50));
   tempBuf.append(cutSign);
   tempAmt = df.format(ttlAmtBal);
   tempAmt = commString.lpad(tempAmt, 10, "0");
//		tempAmt = commString.lpad(Double.toString(ttlAmtBal), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(purchaseSign, 1));
   tempBuf.append(cutSign);
   tempAmt = df.format(hisPurchaseAmt);
   tempAmt = commString.lpad(tempAmt, 10, "0");
//		tempAmt = commString.lpad(Double.toString(hisPurchaseAmt), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(cardSince, 8));
   tempBuf.append(cutSign);
   tempAmt = df.format(rcRateYear);
   tempAmt = commString.lpad(tempAmt, 4, "0");
//		tempAmt = commString.lpad(Double.toString(rcRateYear), 4, "0");		
   tempBuf.append(comc.fixLeft(tempAmt, 4));
   tempBuf.append(cutSign);
   tempAmt = df.format(lineOfCreditAmt);
   tempAmt = commString.lpad(tempAmt, 10, "0");
//		tempAmt = commString.lpad(Double.toString(lineOfCreditAmt), 10, "0"); 
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(canUseLimitSign, 1));
   tempBuf.append(cutSign);
   tempAmt = df.format(canUseLimit);
   tempAmt = commString.lpad(tempAmt, 10, "0");
//		tempAmt = commString.lpad(Double.toString(canUseLimit), 10, "0"); 
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempAmt = df.format(overRateCnt);
   tempAmt = commString.lpad(tempAmt, 2, "0");
//		tempAmt = commString.lpad(Double.toString(overRateCnt), 2, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 2));
   tempBuf.append(cutSign);
   tempAmt = commString.lpad(commString.int2Str(intRateMcode), 4, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 4));
   tempBuf.append(cutSign);
   tempAmt = df.format(minPayBal);
   tempAmt = commString.lpad(tempAmt, 10, "0");
//		tempAmt = commString.lpad(Double.toString(minPayBal), 10, "0");
   tempBuf.append(comc.fixLeft(tempAmt, 10));
   tempBuf.append(cutSign);
   tempAmt = df.format(cpbduePeriod);
   tempAmt = commString.lpad(tempAmt, 3, "0");
//		tempAmt = commString.lpad(Double.toString(cpbduePeriod), 3, "0");
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
   tempBuf.append(comc.fixLeft(tempAmt, 4));
   tempBuf.append(cutSign);
   tempBuf.append(comc.fixLeft(liacStatus, 1));
   tempBuf.append(cutSign);
//		tempAmt = commString.lpad(Double.toString(totAmtMonth), 10, "0");
   tempAmt = df.format(totAmtMonth);
   tempAmt = commString.lpad(tempAmt, 10, "0");
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

//--計算可用餘額
void calBalance() throws Exception {
   canUseLimit = calBal.cardBalance(cardNo);
   if (canUseLimit < 0) {
      canUseLimitSign = "-";
      canUseLimit = canUseLimit * -1;
   }

}

//===============
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
   //RP18VIEWCARD.txt
//   String lsFile = String.format("/crdatacrea/CREDITALL/%s", fileName);
   String lsFile = String.format("%s/media/rsk/%s", getEcsHome(),fileName);

   printf(" openOutputText: "+lsFile);
   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum < 0) {
      printf("[%s] 產檔失敗 !", lsFile);
      okExit(0);
   }

   return;
}

//==========================
void initData() {
   cardNo = "";
   groupCode = "";
   acctType = "";
   idNo = "";
   idPSeqno = "";
   corpPSeqno = "";
   acnoPSeqno = "";
   newEndDate = "";
   supFlag = "";
   regBankNo = "";
   majorIdNo = "";
   indivCrdLmt = 0.0;
   cardLimit = 0.0;
   cardAdjLimit = 0.0;
   cardAdjDate1 = "";
   cardAdjDate2 = "";
   normalFlag = "";
   consumeFlag = "";
   oppostDate = "";
   oppostReason = "";
   currentCode = "";
   comboAcctNo = "";
   lastConsumeDate = "";
   activateFlag = "";
   issueDate = "";
   embossData = "";
   promptDept = "";
   rmCarNo = "";
   introduceId = "";
   sonCardFlag = "";
   expireChgFlag = "";
   preCashFlag = "";
   majorIdPSeqno = "";
   majorEngName = "";
   currCode = "";
   autopayAcctBank = "";
   autopayAcctNo = "";
   autopayIndicator = "";
   billApplyFlag = "";
   statSendInternet = "";
   eMailBill = "";
   ttlAmtBal = 0.0;
   purchaseSign = "";
   lastAcctMonth = "";
   hisPurchaseAmt = 0.0;
   cardSince = "";
   revolveIntRate = 0.0;
   rcRateYear = 0.0;
   lineOfCreditAmt = 0.0;
   canUseLimitSign = "";
   canUseLimit = 0.0;
   for (int ii = 0; ii <12 ; ii++) {
      payRate[ii] = "";
   }
   overRateCnt = 0;
   intRateMcode = 0;
   minPayBal = 0.0;
   cpbduePeriod = 0.0;
   acctStatus = "";
   cpbdueOwnerBank = "";
   cpbdueBankType = "";
   cpbdueType = "";
   cpbdueTcbType = "";
   cpbdueRate = 0.0;
   liacStatus = "";
   totAmtMonth = 0.0;
   adjEffStartDate = "";
   adjEffEndDate = "";
   blockCode1 = "";
   blockCode2 = "";
   blockCode3 = "";
   blockCode4 = "";
   blockCode5 = "";
   idStatus = "";
   idResult = "";
   stmtCycle = "";
}

void clearData01() throws Exception {
   autopayAcctBank = "";
   autopayAcctNo = "";
   autopayIndicator = "";
   billApplyFlag = "";
   statSendInternet = "";
   eMailBill = "";
   ttlAmtBal = 0.0;
   purchaseSign = "";
   lastAcctMonth = "";
   hisPurchaseAmt = 0.0;
   cardSince = "";
   revolveIntRate = 0.0;
   rcRateYear = 0.0;
   lineOfCreditAmt = 0.0;
   canUseLimitSign = "";
   canUseLimit = 0.0;
   overRateCnt = 0;
   intRateMcode = 0;
   minPayBal = 0.0;
   cpbduePeriod = 0.0;
   acctStatus = "";
   cpbdueOwnerBank = "";
   cpbdueBankType = "";
   cpbdueType = "";
   cpbdueTcbType = "";
   cpbdueRate = 0.0;
   liacStatus = "";
   totAmtMonth = 0.0;
   adjEffStartDate = "";
   adjEffEndDate = "";
   blockCode1 = "";
   blockCode2 = "";
   blockCode3 = "";
   blockCode4 = "";
   blockCode5 = "";
   idStatus = "";
   idResult = "";
   stmtCycle = "";
   //paymentRate01--12---
   for (int i = 0; i <payRate.length ; i++) {
      payRate[i] ="";
   }
}

void clearData90() throws Exception {
   cardLimit = 0;
   autopayAcctBank = "";
   autopayAcctNo = "";
   autopayIndicator = "";
   billApplyFlag = "";
   statSendInternet = "";
   eMailBill = "";
   ttlAmtBal = 0.0;
   purchaseSign = "";
   lastAcctMonth = "";
   hisPurchaseAmt = 0.0;
   cardSince = "";
   revolveIntRate = 0.0;
   rcRateYear = 0.0;
   lineOfCreditAmt = 0.0;
   canUseLimitSign = "";
   canUseLimit = 0.0;
   overRateCnt = 0;
   intRateMcode = 0;
   minPayBal = 0.0;
   cpbduePeriod = 0.0;
   acctStatus = "";
   cpbdueOwnerBank = "";
   cpbdueBankType = "";
   cpbdueType = "";
   cpbdueTcbType = "";
   cpbdueRate = 0.0;
   liacStatus = "";
   totAmtMonth = 0.0;
   adjEffStartDate = "";
   adjEffEndDate = "";
   blockCode1 = "";
   blockCode2 = "";
   blockCode3 = "";
   blockCode4 = "";
   blockCode5 = "";
   idStatus = "";
   idResult = "";
   stmtCycle = "";
   cardCanUse = 0;
   acctCanUse = 0;
   //paymentRate01--12---
   for (int i = 0; i <payRate.length ; i++) {
      payRate[i] ="";
   }
}

}
