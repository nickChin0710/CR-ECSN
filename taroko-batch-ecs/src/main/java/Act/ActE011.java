/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  107/02/23  V1.00.01    Brian     error correction                          *
 *  111-10-14  V1.00.02    Machao    sync from mega & updated for project coding standard *                                                                           *
 *  112/03/20  V1.00.03    Simon     1.add update to act_master_bal.af_bal、cf_bal、pf_bal、ai_bal*
 *                                   2.remove act_master_bal.BONUS_NOTAX_ADV(頂級卡不含稅)、act_master_bal.BONUS_TAX_ADV(頂級卡含稅)*
 *  112/03/30  V1.00.04    Simon     not to do generateGenPostLog()            *
 *  112/06/06  V1.00.05    Simon     1.調整 ACT_E011R0                         *
 *                                   2.remove ACT_E011R1                       *
 *  112/09/26  V1.00.06    Holmes    1.mark  selectMktBonusDtlAdv()            *                                 
 ******************************************************************************/

package Act;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.DecimalFormat;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*每日信用卡帳務科目餘額總數表*/
public class ActE011 extends AccessDAO {

    public static final boolean DEBUG_MODE = true;

    private final String PROGNAME = "每日信用卡帳務科目餘額總數表  112-09-26  V1.00.06";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "ActE011";
    String hModUser = "";
    long hSrcPgmPostseq = 0;
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";
    String buf = "";
    String hPostNote = "帳務科目過帳餘額新增/更新";

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptId1 = "ACT_E011R0";
    String rptName1 = "每日信用卡帳務科目餘額總數表";
    int rptSeq1 = 0;
    String szTmp = "";

    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    String rptId2 = "ACT_E011R1";
    String rptName2 = "現金制帳務科目過帳不平報表";
    int rptSeq2 = 0;

    String temstr  = "";
    String temstr2 = "";
    String hBusiBusinessDate = "";
    String hAmblCheckDate = "";
    String hBusiVouchDate = "", hPreVouchDate = "";
    String hPcceCurrCode = "";
    String hPcceCurrChiName = "";
    String hAcsmCurrCode = "";
    String hAcsmAcctCode = "";
    double hAcsmBilledEndBal = 0;
    double hAcsmUnbillEndBal = 0;
    double hAcsmCardSpecBal = 0;
    String hPcodChiLongName = "";
    double hAcctAdiEndBal = 0;
    double hAcurAcctJrnlBal = 0;
    double hAcurTtlAmtBal = 0;
    double hAcurEndBalOp = 0;
    double hAcurEndBalLk = 0;
    String hPrintName = "";
    String hRptName = "";
    double hAmblVdBlBal = 0;
    double hAmblVdCaBal = 0;
    double hAmblVdOtBal = 0;
    double hAmblVdIfBal = 0;
    double hAmblVdRefundBal = 0;
    double hAmblProblemBal = 0;
    double hAmblIllicitBal = 0;
    double hAmblVdProblemBal = 0;
    double hAmblVdIllicitBal = 0;
    double hAmblInBal = 0;
    double hAmblBonusMmk = 0;
    double hAmblBonusMerchant = 0;
    double hAmblVdBonusNotax = 0;
    double hAmblVdBonusTax = 0;
    double hAmblFundBal = 0;
    double hAmblBonusNotax = 0;
    double hAmblBonusTax = 0;
    double hAmblBonusNotaxAdv = 0;
    double hAmblBonusTaxAdv = 0;
    double hThisBusiE011CalBal = 0;
    double hThisBusiDiffBal = 0;
    double hSumPreBusiE011Bal = 0;
    double hSumThisBusiAddAmt = 0;
    double hSumThisCancelAmt = 0;
    double hSumThisAdjustAmt = 0;
    String hPostBalanceMark = "";
    String hPostAcctCode = "";
    double[] hTempAcctCodeAmt = new double[20];
    double[] hTempAcctCodeSumAmt = new double[20];
    String[] aPcceCurrCode = new String[250];
    String[] aPcceCurrChiName = new String[250];
    String[] aAcsmCurrCode = new String[250];
    String[] aAcsmAcctCode = new String[250];
    String[] aPcodChiLongName = new String[250];
    double[] aAcsmBilledEndBal = new double[250];
    double[] aAcsmUnbillEndBal = new double[250];
    double[] aAcsmCardSpecBal = new double[250];
    String[] acctCode = { "BL", "CA", "ID", "IT", "AO", "OT", "LF", "RI", "PN", "AF", "CF", "PF", "CB", "CI", "CC",
            "DB", "SF", "AI", "DP" };
    String[] hPostChiLongName = new String[19];
    int actCurrSumCnt = 0;
    int ptrCurrcodeCnt = 0;
    String tmpstr = "";
    String hExecDate = "";
    String hVouchDate = "";
    String hVoucAcNo = "", hVoucModUser = "";
    double hPreMasterBal = 0, hThisMasterBal = 0;
    int pageCnt = 0;
    int lineCnt = 0;
    int rowsCount = 0;
    int pageCnt2 = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ActE011", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);
            hVoucModUser = comc.commGetUserID();

            selectPtrBusinday();

            selectPtrCurrcode();

            selectActCurrSum();

            checkOpen();

            for (int inti = 0; inti < ptrCurrcodeCnt; inti++) {
                showLogMessage("I", "",
                        String.format("處理 幣別 [%s-%s]", aPcceCurrCode[inti], aPcceCurrChiName[inti]));
                hAcsmBilledEndBal = hAcsmUnbillEndBal = hAcsmCardSpecBal = 0;
                for (int intm = 0; intm < 20; intm++) {
                    hTempAcctCodeAmt[intm] = 0;
                    hTempAcctCodeSumAmt[intm] = 0;
                }
                printHeader(inti);
                for (int intk = 0; intk < actCurrSumCnt; intk++) {
                    if (!aPcceCurrCode[inti].equals(aAcsmCurrCode[intk]))
                        continue;
                    hPcceCurrCode = aPcceCurrCode[inti]; 
                    for (int intm = 0; intm < 19; intm++) {
                        if (!acctCode[intm].equals(aAcsmAcctCode[intk]))
                            continue;
                        hAcsmAcctCode = aAcsmAcctCode[intk];
                        hPcodChiLongName = aPcodChiLongName[intk];
                        hPostChiLongName[intm] = aPcodChiLongName[intk];
                        printDetail(intk);
                        hAcsmBilledEndBal += aAcsmBilledEndBal[intk];
                        hAcsmUnbillEndBal += aAcsmUnbillEndBal[intk];
                        hAcsmCardSpecBal += aAcsmCardSpecBal[intk];
                        hTempAcctCodeAmt[intm] += aAcsmBilledEndBal[intk];
                        hTempAcctCodeSumAmt[intm] += aAcsmBilledEndBal[intk] + aAcsmUnbillEndBal[intk];
                        tmpstr = "";
                        if (intm < 6) {
                            hTempAcctCodeAmt[19] += aAcsmBilledEndBal[intk];
                            hTempAcctCodeSumAmt[19] += aAcsmBilledEndBal[intk] + aAcsmUnbillEndBal[intk];
                            tmpstr = String.format("累計本金金類[%14.2f]", hTempAcctCodeAmt[19]);
                        }
                        showLogMessage("I", "",
                                String.format("  處理 科目 [%s-%-16.16s] billed[%14.2f] unbill[%14.2f] %s",
                                        hAcsmAcctCode, hPcodChiLongName, aAcsmBilledEndBal[intk],
                                        aAcsmUnbillEndBal[intk], tmpstr));
                    }
                }
                showLogMessage("I", "", String.format(
                        "============================================================================================================="));
                showLogMessage("I", "", String.format("  累計 %26.26s billed[%14.2f] unbill[%14.2f] 本金金類[%14.2f]", " ",
                        hAcsmBilledEndBal, hAcsmUnbillEndBal, hTempAcctCodeAmt[19]));

                showLogMessage("I", "", String.format("  "));
                printTailer1();
                selectActAcct();
                selectActAcctCurr();
                printTailer2();
                printTailer3();

                if (hPcceCurrCode.equals("901")) {
                    selectDbaDebt();
                    selectDbaAcaj();
                    selectRskProblemVd();
                    selectBilContract();
                  //select_mmk_bonus_log();
                  //select_mkt_gift_order();
                    selectMktGiftBpexchg();
                    selectDbmBonusDtl();
                    selectMktCashbackDtl();
                    selectMktBonusDtl();
                } else {
                    selectCyDcFundDtl();
                }
                printTailer4();
                lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));
                selectRskProblem();
                formatDoubleActMasterBal();
                if (insertActMasterBal() != 0)
                    updateActMasterBal();
                procNextActPostlog();
                procThisActPostLog();
              //if (hAmblCheckDate.equals(hBusiVouchDate))
              //   {generateGenPostLog();}

            }

//            comc.writeReport(temstr, lpar1);
//            comc.writeReport(temstr2, lpar2);
//            lpRtn("ACT_E011");
            comcr.insertPtrBatchRpt(lpar1);
          //comcr.insertPtrBatchRpt(lpar2);

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        String strb = "";

        hBusiBusinessDate = "";
        hAmblCheckDate = "";
        hBusiVouchDate = "";

        sqlCmd = "select business_date,";
        sqlCmd += " to_char(to_date(business_date,'yyyymmdd')+1 days,'yyyymmdd') h_ambl_check_date,";
        sqlCmd += " vouch_date ";
        sqlCmd += "  from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hAmblCheckDate = getValue("h_ambl_check_date");
            hBusiVouchDate = getValue("vouch_date");
        }

        strb = String.format("%07d", comcr.str2long(hBusiBusinessDate) - 19110000);
        hExecDate = strb;

        strb = String.format("%07d", comcr.str2long(hBusiVouchDate) - 19110000);
        hVouchDate = strb;
    }

    /***********************************************************************/
    void printHeader(int seq) throws Exception {
        String temp = "";
        hPcceCurrCode = aPcceCurrCode[seq];
        pageCnt++;

        buf = "";
        buf = comcr.insertStr(buf, " " + comcr.bankName + " ", 28);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱  :ACT_E011R0", 1);
        buf = comcr.insertStrCenter(buf, "每日信用卡帳務科目餘額總數表", 80);
        buf = comcr.insertStr(buf, "頁次:", 63);
        temp = String.format("%04d", pageCnt);
        buf = comcr.insertStr(buf, temp, 68);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "執行日期  :", 1);
        buf = comcr.insertStr(buf, hExecDate, 12);
        buf = comcr.insertStr(buf, "幣別:", 63);
        buf = comcr.insertStr(buf, hPcceCurrCode, 68);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "會計帳日期:", 1);
        buf = comcr.insertStr(buf, hVouchDate, 12);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
      //buf = comcr.insertStr(buf, "科目代號", 1);
      //buf = comcr.insertStr(buf, "中文全稱", 12);
        buf = comcr.insertStr(buf, "科目代號餘額", 45);
        buf = comcr.insertStr(buf, "戶數", 85);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "科目                      -----------------------------------------------------", 1);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "代號", 1);
        buf = comcr.insertStr(buf, "中文全稱", 9);
        buf = comcr.insertStr(buf, "billed", 35);
        buf = comcr.insertStr(buf, "unbill", 50);
        buf = comcr.insertStr(buf, "合計", 67);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        for (int i = 0; i < 89; i++)
            buf += "=";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }


    /***********************************************************************/
    void checkOpen() throws Exception {
        temstr = String.format("%s/reports/ACT_E011R0_%s", comc.getECSHOME(), hBusiBusinessDate.substring(4));
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        
        temstr2 = String.format("%s/reports/ACT_E011R1_%s", comc.getECSHOME(), hBusiBusinessDate.substring(4));
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
    }

    /***********************************************************************/
    void printDetail(int seq) throws Exception {

        buf = "";
        buf = comcr.insertStr(buf, hAcsmAcctCode, 1); /* 科目代號 */

        buf = comcr.insertStr(buf, hPcodChiLongName, 9); /* 中文全稱 */

        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", aAcsmBilledEndBal[seq]); /* billed */
        buf = comcr.insertStr(buf, szTmp, 23);

        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", aAcsmUnbillEndBal[seq]); /* unbill */
        buf = comcr.insertStr(buf, szTmp, 42);

        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", aAcsmUnbillEndBal[seq] + aAcsmBilledEndBal[seq]); /* 合計 */
        buf = comcr.insertStr(buf, szTmp, 61);

        szTmp = comcr.commFormat("1z,3z,3z", aAcsmCardSpecBal[seq]); /* 戶數 */
        buf = comcr.insertStr(buf, szTmp, 80);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        lineCnt++;
        rowsCount++;
    }

   
    /***********************************************************************/
    void printTailer1() throws Exception {
        buf = "";
        for (int i = 0; i < 89; i++)
            buf += "=";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "合計", 9);

        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcsmBilledEndBal);
        buf = comcr.insertStr(buf, szTmp, 23);

        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcsmUnbillEndBal);
        buf = comcr.insertStr(buf, szTmp, 42);

        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcsmBilledEndBal + hAcsmUnbillEndBal);
        buf = comcr.insertStr(buf, szTmp, 61);

        szTmp = comcr.commFormat("1z,3z,3z", hAcsmCardSpecBal);
        buf = comcr.insertStr(buf, szTmp, 80);

        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAdiEndBal = 0.0;
        sqlCmd = "select sum(floor(adi_end_bal)) h_acct_adi_end_bal "; /* UNBILLED帳外息餘額 */
        sqlCmd += " from act_acct ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcctAdiEndBal = getValueDouble("h_acct_adi_end_bal");
        }

    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hAcurAcctJrnlBal = 0.0;
        hAcurTtlAmtBal = 0.0;
        hAcurEndBalOp = 0.0;
        hAcurEndBalLk = 0.0;

        sqlCmd = "select sum(decode(curr_code,'901',acct_jrnl_bal, dc_acct_jrnl_bal)) h_acur_acct_jrnl_bal,"; /* 目前總欠餘額 */
        sqlCmd += " sum(decode(curr_code,'901',ttl_amt_bal, dc_ttl_amt_bal))     h_acur_ttl_amt_bal,"; /* 總欠款餘額 */
        sqlCmd += " sum(decode(curr_code,'901',end_bal_op , dc_end_bal_op))      h_acur_end_bal_op,"; /* 未圈溢付款餘額 */
        sqlCmd += " sum(decode(curr_code,'901',end_bal_lk , dc_end_bal_lk))      h_acur_end_bal_lk "; /* 已圈溢付款餘額 */
        sqlCmd += "  from act_acct_curr  ";
        sqlCmd += " where curr_code = ? ";
        setString(1, hPcceCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_curr not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcurAcctJrnlBal = getValueDouble("h_acur_acct_jrnl_bal");
            hAcurTtlAmtBal = getValueDouble("h_acur_ttl_amt_bal");
            hAcurEndBalOp = getValueDouble("h_acur_end_bal_op");
            hAcurEndBalLk = getValueDouble("h_acur_end_bal_lk");
        }

    }

    /***********************************************************************/
    void printTailer2() throws Exception {
        buf = "   ";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "目前總欠餘額 ACCT_JRNL_BAL", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcurAcctJrnlBal);
        buf = comcr.insertStr(buf, szTmp, 46);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "科目餘額", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcsmBilledEndBal + hAcsmUnbillEndBal);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "減:爭議款(合計)", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[18]);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
      //buf = comcr.insertStr(buf, "減:未圈溢付款餘額", 5);
        buf = comcr.insertStr(buf, "減:溢付款餘額", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcurEndBalOp);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

/***
        buf = "";
        buf = comcr.insertStr(buf, "減:已圈溢付款餘額", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcurEndBalLk);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        if (!hPcceCurrCode.equals("901"))
            return;

        buf = "";
        buf = comcr.insertStr(buf, "加:UNBILL帳外息餘額", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAcctAdiEndBal);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
***/

    }

    /***********************************************************************/
    void printTailer3() throws Exception {
        buf = "   ";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "目前billed本金類科目餘額", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[19]);
        buf = comcr.insertStr(buf, szTmp, 46);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "簽帳款(BL)", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[0]);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "加:預借現金(CA)", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[1]);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "加:代收款(ID)", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[2]);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "加:分期付款(IT)", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[3]);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

/***
        buf = "";
        buf = comcr.insertStr(buf, "加:代償款(AO)", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[4]);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
***/
        buf = "";
        buf = comcr.insertStr(buf, "加:其他應收款(OT)", 5);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeAmt[5]);
        buf = comcr.insertStr(buf, szTmp, 30);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void selectDbaDebt() throws Exception {
        hAmblVdBlBal = 0.0;
        hAmblVdCaBal = 0.0;
        hAmblVdOtBal = 0.0;
        hAmblVdIfBal = 0.0;

        sqlCmd = "select sum(decode(acct_code,'BL',end_bal,0)) h_ambl_vd_bl_bal,"; /* VD簽帳款 */
        sqlCmd += " sum(decode(acct_code,'CA',end_bal,0)) h_ambl_vd_ca_bal,"; /* VD預借現金 */
        sqlCmd += " sum(decode(acct_code,'OT',end_bal,0)) h_ambl_vd_ot_bal,"; /* VD其他應收款 */
        sqlCmd += " sum(decode(acct_code,'LF',end_bal,0)) h_ambl_vd_lf_bal "; /* VD掛失費 */
        sqlCmd += "  from dba_debt  ";
        sqlCmd += " where end_bal <> 0 ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dba_debt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAmblVdBlBal = getValueDouble("h_ambl_vd_bl_bal");
            hAmblVdCaBal = getValueDouble("h_ambl_vd_ca_bal");
            hAmblVdOtBal = getValueDouble("h_ambl_vd_ot_bal");
            hAmblVdIfBal = getValueDouble("h_ambl_vd_lf_bal");
        }

    }

    /***********************************************************************/
    void selectDbaAcaj() throws Exception {
        hAmblVdRefundBal = 0.0;

        sqlCmd = "select sum(dr_amt) as h_ambl_vd_refund_bal "; /* VD負項請款待回存金額 */
        sqlCmd += "  from dba_acaj  ";
        sqlCmd += " where from_code = '1'  ";
        sqlCmd += "   and acct_code in ('BL','CA')  ";
        sqlCmd += "   and proc_flag in ('N','0') ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dba_acaj not found!", "", hCallBatchSeqno);
        }
        hAmblVdRefundBal = getValueDouble("h_ambl_vd_refund_bal");
    }

    /***********************************************************************/
    void selectRskProblemVd() throws Exception {
        hAmblVdProblemBal = 0;
        hAmblVdIllicitBal = 0;
        sqlCmd = "select sum(decode(prb_mark,'Q',prb_amount,0)) h_ambl_vd_problem_bal,"; /* VD卡問交餘額 */
        sqlCmd += " sum(decode(prb_mark,'E',prb_amount,0)) h_ambl_vd_illicit_bal "; /* VD卡不合格帳單 */
        sqlCmd += "  from rsk_problem  ";
        sqlCmd += " where prb_status >='30'  ";
        sqlCmd += "   and prb_status <'80'  ";
        sqlCmd += "   and decode(debit_flag,'','N',debit_flag) = 'Y'  ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = ?  ";
        sqlCmd += "   and txn_code not in ('06','25','27','28','29') ";
        setString(1, hPcceCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_rsk_problem not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAmblVdProblemBal = getValueDouble("h_ambl_vd_problem_bal");
            hAmblVdIllicitBal = getValueDouble("h_ambl_vd_illicit_bal");
        }

        sqlCmd = "select  ? -sum(decode(deduct_date,'',dr_amt,  ? ,decode(deduct_proc_date,'',deduct_amt,0),0)) h_ambl_vd_problem_bal "; /* VD卡問交餘額 */
        sqlCmd += " from dba_acaj  ";
        sqlCmd += "where adjust_type = 'DP01' ";
        setDouble(1, hAmblVdProblemBal);
        setString(2, hBusiBusinessDate);
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dba_acaj not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAmblVdProblemBal = getValueDouble("h_ambl_vd_problem_bal");
        }

    }

    /***********************************************************************/
    void selectBilContract() throws Exception {
        hAmblInBal = 0.0;
        sqlCmd = "select sum((unit_price*(install_tot_term-install_curr_term)+ remd_amt+decode(install_curr_term,0,first_remd_amt,0))) h_ambl_in_bal "; /* 目前分期付款未到期金額 */
        sqlCmd += "  from bil_contract  ";
        sqlCmd += " where install_tot_term != install_curr_term  ";
        sqlCmd += "   and contract_kind = 1  ";
        sqlCmd += "   and post_cycle_dd > 0 ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_bil_contract not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAmblInBal = getValueDouble("h_ambl_in_bal");
        }

    }

    /***********************************************************************/
    void selectMmkBonusLog() throws Exception {
        hAmblBonusMmk = 0.0;
        sqlCmd = "select sum(gift_cash_value) h_ambl_bonus_mmk "; /* 應付MMK特店款:萊爾富兌換紅利贈品紀錄檔*/
        sqlCmd += " from mmk_bonus_log  ";
        sqlCmd += "where gl_date >= '20110701'  ";
        sqlCmd += "and proc_mark <> '0' ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mmk_bonus_log not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAmblBonusMmk = getValueDouble("h_ambl_bonus_mmk");
        }

    }

    /***********************************************************************/
    /*
    void select_mkt_gift_order() throws Exception {
        h_ambl_bonus_merchant = 0.0;
        sqlCmd = "select sum(unit_price*chg_qty) h_ambl_bonus_merchant "; // 應付紅利特店款         
        sqlCmd += "  from mkt_gift_order o  ";
        sqlCmd += " where gl_date != ''  ";
        sqlCmd += "   and user_pay_amt = 0  ";
        sqlCmd += "   and not exists (select 1 from mkt_gift_payment p where p.order_date = o.order_date  ";
        sqlCmd += "   and p.exchg_seqno = o.exchg_seqno  ";
        sqlCmd += "   and apr_flag = 'Y')  ";
        sqlCmd += "   and not exists (select 1 from mkt_gift_return r where r.order_date = o.order_date  ";
        sqlCmd += "   and r.exchg_seqno = o.exchg_seqno  ";
        sqlCmd += "   and apr_flag = 'Y') ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.err_rtn("select_mkt_gift_order not found!", "", h_call_batch_seqno);
        }
        h_ambl_bonus_merchant = getValueDouble("h_ambl_bonus_merchant");

    }
    */
    // CnvM280 convert mkt_gift_order, mkt_gift_payment, mkt_gift_return into mkt_gift_bpexchg
    /***********************************************************************/
    void selectMktGiftBpexchg() throws Exception {
        hAmblBonusMerchant = 0.0;
      //sqlCmd = "select nvl(sum(exchg_cnt*cash_value), 0) h_ambl_bonus_merchant "; /* 應付紅利特店款 */         
        sqlCmd = "select nvl(sum(decode(pay_gl_date,'',exchg_cnt,unpay_cnt) * cash_value), 0) "
               + " h_ambl_bonus_merchant "; /* 應付紅利特店款 */ 
        sqlCmd += "  from mkt_gift_bpexchg ";
        sqlCmd += " where exg_gl_date != ''  ";
        sqlCmd += "   and user_pay_amt = 0  ";
      //sqlCmd += "   and pay_gl_date = ''  ";
        sqlCmd += "   and ret_apr_date = ''  ";
        sqlCmd += "   and fund_code = ''  ";
        sqlCmd += "   and ecoupon_gl_date = ''  ";
      //sqlCmd += "   and (pay_gl_date = '' "
      //        + "    or (pay_gl_date != '' and ecoupon_gl_date = '' and unpay_cnt != 0) ) ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_gift_bpexchg not found!", "", hCallBatchSeqno);
        }
        hAmblBonusMerchant = getValueDouble("h_ambl_bonus_merchant");

    }
    /***********************************************************************/
    void selectDbmBonusDtl() throws Exception {
        hAmblVdBonusNotax = 0;
        hAmblVdBonusTax = 0;
        sqlCmd = "select sum(decode(tax_flag,'Y',0,end_tran_bp)) h_ambl_vd_bonus_notax,"; /* 免稅總點數 */
        sqlCmd += " sum(decode(tax_flag,'Y',end_tran_bp,0)) h_ambl_vd_bonus_tax "; /* 應稅總點數 */
        sqlCmd += "  from dbm_bonus_dtl  ";
        sqlCmd += " where end_tran_bp <> 0 ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbm_bonus_dtl not found!", "", hCallBatchSeqno);
        }
        hAmblVdBonusNotax = getValueDouble("h_ambl_vd_bonus_notax");
        hAmblVdBonusTax = getValueDouble("h_ambl_vd_bonus_tax");

    }

    /***********************************************************************/
    void selectMktBonusDtl() throws Exception {
        hAmblBonusNotax = 0.0;
        hAmblBonusTax = 0.0;
        
        sqlCmd = "select sum(decode(tax_flag,'Y',0,end_tran_bp + res_tran_bp)) as no_tax_tran_bp_1, ";
        sqlCmd += " sum(decode(tax_flag,'Y',end_tran_bp + res_tran_bp,0)) as tax_tran_bp_1 ";
        sqlCmd += " from mkt_bonus_dtl  ";
        sqlCmd += " where (END_TRAN_BP + RES_TRAN_BP) <> 0 ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_bonus_dtl not found!", "", hCallBatchSeqno);
        }
        hAmblBonusNotax = getValueDouble("no_tax_tran_bp_1");
        hAmblBonusTax = getValueDouble("tax_tran_bp_1");

//        selectMktBonusDtlAdv();

//        hAmblBonusNotax = hAmblBonusNotax - hAmblBonusNotaxAdv;
//        hAmblBonusTax   = hAmblBonusTax   - hAmblBonusTaxAdv;

    }

    /***********************************************************************/
    void selectMktBonusDtlAdv() throws Exception {
        hAmblBonusNotaxAdv = 0.0;
        hAmblBonusTaxAdv = 0.0;
        
        sqlCmd  = "select sum(decode(tax_flag_2,'Y',0,end_tran_bp_2)) as no_tax_tran_bp_2, ";
        sqlCmd += "sum(decode(tax_flag_2,'Y',end_tran_bp_2,0)) as tax_tran_bp_2 ";
        sqlCmd += "from ";
        sqlCmd += "( ";
        sqlCmd += " select distinct m0.p_seqno as p_seqno_2, (m0.end_tran_bp + m0.res_tran_bp) as end_tran_bp_2, m0.tax_flag as tax_flag_2";
        sqlCmd += " from mkt_bonus_dtl m0,  ";
				sqlCmd += "  (select distinct acno_p_seqno ";
				sqlCmd += "   from crd_card ";
				sqlCmd += "   where acct_type in ('01') ";
				sqlCmd += "   and current_code = '0' ";
				sqlCmd += "   and sup_flag = '0' ";
				sqlCmd += "   and uf_nvl(group_code,'xxxx') in ('5168','5169') ";
				sqlCmd += "  ) v0 ";
        sqlCmd += " where v0.acno_p_seqno = m0.p_seqno ";
        sqlCmd += " and m0.bonus_type = 'BONU' " ;
        sqlCmd += " and (m0.end_tran_bp + m0.res_tran_bp) <> 0 " ;
        sqlCmd += ") ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_bonus_dtl_adv not found!", "", hCallBatchSeqno);
        }
        hAmblBonusNotaxAdv = getValueDouble("no_tax_tran_bp_2");
        hAmblBonusTaxAdv   = getValueDouble("tax_tran_bp_2");

    }

    /***********************************************************************/
    void selectCyDcFundDtl() throws Exception {
        hAmblFundBal = 0.0;
        sqlCmd = "select sum(end_tran_amt) h_ambl_fund_bal ";
        sqlCmd += " from cyc_dc_fund_dtl  "; 
        sqlCmd += "where curr_code = ? ";
        setString(1, hPcceCurrCode);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_cyc_dc_fund_dtl not found!", "", hCallBatchSeqno);
        }
        hAmblFundBal = getValueDouble("h_ambl_fund_bal");
    }
    /***********************************************************************/
    void selectMktCashbackDtl() throws Exception {
        hAmblFundBal = 0.0;
        sqlCmd = "select sum(end_tran_amt + res_tran_amt) h_ambl_fund_bal ";
        sqlCmd += " from mkt_cashback_dtl  "; 
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_mkt_cashback_dtl not found!", "", hCallBatchSeqno);
        }
        hAmblFundBal = getValueDouble("h_ambl_fund_bal");

    }
    /***********************************************************************/
    void printTailer4() throws Exception {
        buf = "   ";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "目前現金回饋總額 NET_TTL_BP", 1);        
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hAmblFundBal);
        buf = comcr.insertStr(buf, szTmp, 46);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        return;
    }

    /***********************************************************************/
    void selectRskProblem() throws Exception {
        hAmblProblemBal = 0;
        hAmblIllicitBal = 0;
        sqlCmd = "select sum(decode(prb_mark, 'Q', decode(decode(curr_code,'','901',curr_code), '901', prb_amount, dc_prb_amount), 0)) h_ambl_problem_bal,"; /* 信用卡問交餘額 */
        sqlCmd += " sum(decode(prb_mark, 'E', prb_amount*(decode(decode(curr_code, 0,'901',curr_code),'901',1, (dc_dest_amt/dest_amt))),0)) h_ambl_illicit_bal "; /* 信用卡不合格帳單 */
        sqlCmd += "  from rsk_problem  ";
        sqlCmd += " where prb_status >='30'  ";
        sqlCmd += "   and prb_status <'80'  ";
        sqlCmd += "   and decode(curr_code,'','901',curr_code) = ?  ";
        sqlCmd += "   and decode(debit_flag ,'','N',debit_flag ) != 'Y'  ";
        sqlCmd += "   and txn_code not in ('06','25','27','28','29') ";
        setString(1, hPcceCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_rsk_problem not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAmblProblemBal = getValueDouble("h_ambl_problem_bal");
            hAmblIllicitBal = getValueDouble("h_ambl_illicit_bal");
        }

        hAmblProblemBal = comcr.commCurrAmt(hPcceCurrCode, hAmblProblemBal, 0);
        hAmblIllicitBal = comcr.commCurrAmt(hPcceCurrCode, hAmblIllicitBal, 0);
    }

    void formatDoubleActMasterBal() {

        DecimalFormat decimal = new DecimalFormat("#.00");
        
        hTempAcctCodeSumAmt[0] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[0]));
        hTempAcctCodeSumAmt[1] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[1]));
        hTempAcctCodeSumAmt[2] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[2]));
        hTempAcctCodeSumAmt[3] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[3]));
        hTempAcctCodeSumAmt[4] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[4]));
        hTempAcctCodeSumAmt[5] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[5]));
        hTempAcctCodeSumAmt[6] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[6]));
        hTempAcctCodeSumAmt[7] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[7]));
        hTempAcctCodeSumAmt[8] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[8]));
        hTempAcctCodeSumAmt[12] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[12]));
        hTempAcctCodeSumAmt[13] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[13]));
        hTempAcctCodeSumAmt[14] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[14]));
        hTempAcctCodeSumAmt[16] = comc.str2double(decimal.format(hTempAcctCodeSumAmt[16]));
        hAcurEndBalOp = comc.str2double(decimal.format(hAcurEndBalOp));
        hAcurEndBalLk = comc.str2double(decimal.format(hAcurEndBalLk));
        hAmblFundBal = comc.str2double(decimal.format(hAmblFundBal));
        hAmblBonusNotax = comc.str2double(decimal.format(hAmblBonusNotax));
        hAmblBonusTax = comc.str2double(decimal.format(hAmblBonusTax));
        hAmblBonusNotaxAdv = comc.str2double(decimal.format(hAmblBonusNotaxAdv));
        hAmblBonusTaxAdv = comc.str2double(decimal.format(hAmblBonusTaxAdv));
        hAmblVdBonusNotax = comc.str2double(decimal.format(hAmblVdBonusNotax));
        hAmblVdBonusTax = comc.str2double(decimal.format(hAmblVdBonusTax));
        hAmblBonusMerchant = comc.str2double(decimal.format(hAmblBonusMerchant));
        hAmblBonusMmk = comc.str2double(decimal.format(hAmblBonusMmk));
        hAmblInBal = comc.str2double(decimal.format(hAmblInBal));
        hAmblProblemBal = comc.str2double(decimal.format(hAmblProblemBal));
        hAmblIllicitBal = comc.str2double(decimal.format(hAmblIllicitBal));
        hAmblVdBlBal = comc.str2double(decimal.format(hAmblVdBlBal));
        hAmblVdCaBal = comc.str2double(decimal.format(hAmblVdCaBal));
        hAmblVdOtBal = comc.str2double(decimal.format(hAmblVdOtBal));
        hAmblVdIfBal = comc.str2double(decimal.format(hAmblVdIfBal));
        hAmblVdRefundBal = comc.str2double(decimal.format(hAmblVdRefundBal));
        hAmblVdProblemBal = comc.str2double(decimal.format(hAmblVdProblemBal));
        hAmblVdIllicitBal = comc.str2double(decimal.format(hAmblVdIllicitBal) );
    }
    /***********************************************************************/
    int insertActMasterBal() throws Exception {
        
        setValue("check_date", hAmblCheckDate);
        setValue("curr_code", hPcceCurrCode);
        setValueDouble("bl_bal", hTempAcctCodeSumAmt[0]);
        setValueDouble("ca_bal", hTempAcctCodeSumAmt[1]);
        setValueDouble("id_bal", hTempAcctCodeSumAmt[2]);
        setValueDouble("it_bal", hTempAcctCodeSumAmt[3]);
        setValueDouble("ao_bal", hTempAcctCodeSumAmt[4]);
        setValueDouble("ot_bal", hTempAcctCodeSumAmt[5]);
        setValueDouble("lf_bal", hTempAcctCodeSumAmt[6]);
        setValueDouble("ri_bal", hTempAcctCodeSumAmt[7]);
        setValueDouble("pn_bal", hTempAcctCodeSumAmt[8]);
        setValueDouble("af_bal", hTempAcctCodeSumAmt[9]);
        setValueDouble("cf_bal", hTempAcctCodeSumAmt[10]);
        setValueDouble("pf_bal", hTempAcctCodeSumAmt[11]);
        setValueDouble("cb_bal", hTempAcctCodeSumAmt[12]);
        setValueDouble("ci_bal", hTempAcctCodeSumAmt[13]);
        setValueDouble("cc_bal", hTempAcctCodeSumAmt[14]);
        setValueDouble("sf_bal", hTempAcctCodeSumAmt[16]);
        setValueDouble("ai_bal", hTempAcctCodeSumAmt[17]);
        setValueDouble("op_bal", hAcurEndBalOp);
        setValueDouble("lk_bal", hAcurEndBalLk);
        setValueDouble("fund_bal", hAmblFundBal);
        setValueDouble("bonus_notax", hPcceCurrCode.equals("901") ? hAmblBonusNotax : 0);
        setValueDouble("bonus_tax", hPcceCurrCode.equals("901") ? hAmblBonusTax : 0);
      //setValueDouble("bonus_notax_adv", hPcceCurrCode.equals("901") ? hAmblBonusNotaxAdv : 0);
      //setValueDouble("bonus_tax_adv", hPcceCurrCode.equals("901") ? hAmblBonusTaxAdv : 0);
        setValueDouble("vd_bonus_notax", hPcceCurrCode.equals("901") ? hAmblVdBonusNotax : 0);
        setValueDouble("vd_bonus_tax", hPcceCurrCode.equals("901") ? hAmblVdBonusTax : 0);
        setValueDouble("bonus_merchant", hPcceCurrCode.equals("901") ? hAmblBonusMerchant : 0);
        setValueDouble("bonus_mmk", hPcceCurrCode.equals("901") ? hAmblBonusMmk : 0);
        setValueDouble("bonus_ibon", 0);
        setValueDouble("in_bal", hPcceCurrCode.equals("901") ? hAmblInBal : 0);
        setValueDouble("problem_bal", hAmblProblemBal);
        setValueDouble("illicit_bal", hAmblIllicitBal);
        setValueDouble("vd_bl_bal", hPcceCurrCode.equals("901") ? hAmblVdBlBal : 0);
        setValueDouble("vd_ca_bal", hPcceCurrCode.equals("901") ? hAmblVdCaBal : 0);
        setValueDouble("vd_ot_bal", hPcceCurrCode.equals("901") ? hAmblVdOtBal : 0);
        setValueDouble("vd_lf_bal", hPcceCurrCode.equals("901") ? hAmblVdIfBal : 0);
        setValueDouble("vd_refund_bal", hPcceCurrCode.equals("901") ? hAmblVdRefundBal : 0);
        setValueDouble("vd_problem_bal", hPcceCurrCode.equals("901") ? hAmblVdProblemBal : 0);
        setValueDouble("vd_illicit_bal", hPcceCurrCode.equals("901") ? hAmblVdIllicitBal : 0);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", "ActE011");
        daoTable = "act_master_bal";
        insertTable();
        if (dupRecord.equals("Y"))
            return (1);
        return (0);
    }

    /***********************************************************************/
    void updateActMasterBal() throws Exception {
        daoTable = "act_master_bal";
        updateSQL = "bl_bal         = ?,";
        updateSQL += " ca_bal         = ?,";
        updateSQL += " id_bal         = ?,";
        updateSQL += " it_bal         = ?,";
        updateSQL += " ao_bal         = ?,";
        updateSQL += " ot_bal         = ?,";
        updateSQL += " lf_bal         = ?,";
        updateSQL += " ri_bal         = ?,";
        updateSQL += " pn_bal         = ?,";
        updateSQL += " af_bal         = ?,";
        updateSQL += " cf_bal         = ?,";
        updateSQL += " pf_bal         = ?,";
        updateSQL += " cb_bal         = ?,";
        updateSQL += " ci_bal         = ?,";
        updateSQL += " cc_bal         = ?,";
        updateSQL += " sf_bal         = ?,";
        updateSQL += " ai_bal         = ?,";
        updateSQL += " op_bal         = ?,";
        updateSQL += " lk_bal         = ?,";
        updateSQL += " fund_bal       = ?,";
        updateSQL += " bonus_notax    = ?,";
        updateSQL += " bonus_tax      = ?,";
      //updateSQL += " bonus_notax_adv = ?,";
      //updateSQL += " bonus_tax_adv  = ?,";
        updateSQL += " vd_bonus_notax = ?,";
        updateSQL += " vd_bonus_tax   = ?,";
        updateSQL += " bonus_merchant = ?,";
        updateSQL += " bonus_ibon     = 0,";
        updateSQL += " bonus_mmk      = ?,";
        updateSQL += " in_bal         = ?,";
        updateSQL += " problem_bal    = ?,";
        updateSQL += " illicit_bal    = ?,";
        updateSQL += " vd_bl_bal      = ?,";
        updateSQL += " vd_ca_bal      = ?,";
        updateSQL += " vd_ot_bal      = ?,";
        updateSQL += " vd_lf_bal      = ?,";
        updateSQL += " vd_refund_bal  = ?,";
        updateSQL += " vd_problem_bal = ?,";
        updateSQL += " vd_illicit_bal = ?,";
        updateSQL += " mod_time       = sysdate,";
        updateSQL += " mod_pgm        = 'ActE011'";
        whereStr = "where check_date  = ?  ";
        whereStr += "  and curr_code    = ? ";
        setDouble(1, hTempAcctCodeSumAmt[0]);
        setDouble(2, hTempAcctCodeSumAmt[1]);
        setDouble(3, hTempAcctCodeSumAmt[2]);
        setDouble(4, hTempAcctCodeSumAmt[3]);
        setDouble(5, hTempAcctCodeSumAmt[4]);
        setDouble(6, hTempAcctCodeSumAmt[5]);
        setDouble(7, hTempAcctCodeSumAmt[6]);
        setDouble(8, hTempAcctCodeSumAmt[7]);
        setDouble(9, hTempAcctCodeSumAmt[8]);
        setDouble(10, hTempAcctCodeSumAmt[9]);
        setDouble(11, hTempAcctCodeSumAmt[10]);
        setDouble(12, hTempAcctCodeSumAmt[11]);
        setDouble(13, hTempAcctCodeSumAmt[12]);
        setDouble(14, hTempAcctCodeSumAmt[13]);
        setDouble(15, hTempAcctCodeSumAmt[14]);
        setDouble(16, hTempAcctCodeSumAmt[16]);
        setDouble(17, hTempAcctCodeSumAmt[17]);
        setDouble(18, hAcurEndBalOp);
        setDouble(19, hAcurEndBalLk);
        setDouble(20, hAmblFundBal);
        setDouble(21, hPcceCurrCode.equals("901") ? hAmblBonusNotax : 0);
        setDouble(22, hPcceCurrCode.equals("901") ? hAmblBonusTax : 0);
      //setDouble(23, hPcceCurrCode.equals("901") ? hAmblBonusNotaxAdv : 0);
      //setDouble(24, hPcceCurrCode.equals("901") ? hAmblBonusTaxAdv : 0);
        setDouble(23, hPcceCurrCode.equals("901") ? hAmblVdBonusNotax : 0);
        setDouble(24, hPcceCurrCode.equals("901") ? hAmblVdBonusTax : 0);
        setDouble(25, hPcceCurrCode.equals("901") ? hAmblBonusMerchant : 0);
        setDouble(26, hPcceCurrCode.equals("901") ? hAmblBonusMmk : 0);
        setDouble(27, hPcceCurrCode.equals("901") ? hAmblInBal : 0);
        setDouble(28, hAmblProblemBal);
        setDouble(29, hAmblIllicitBal);
        setDouble(30, hPcceCurrCode.equals("901") ? hAmblVdBlBal : 0);
        setDouble(31, hPcceCurrCode.equals("901") ? hAmblVdCaBal : 0);
        setDouble(32, hPcceCurrCode.equals("901") ? hAmblVdOtBal : 0);
        setDouble(33, hPcceCurrCode.equals("901") ? hAmblVdIfBal : 0);
        setDouble(34, hPcceCurrCode.equals("901") ? hAmblVdRefundBal : 0);
        setDouble(35, hPcceCurrCode.equals("901") ? hAmblVdProblemBal : 0);
        setDouble(36, hPcceCurrCode.equals("901") ? hAmblVdIllicitBal : 0);
        setString(37, hAmblCheckDate);
        setString(38, hPcceCurrCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_act_master_bal not found!", "", hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void procNextActPostlog() throws Exception {

        for (int intn = 0; intn < 19; intn++) {
            hPostAcctCode    = acctCode[intn];
            if (!Arrays.asList("AF","CF","PF","AI").contains(hPostAcctCode))
                {continue;}
            daoTable    = "act_post_log";
            extendField = "post.";
            setValue("post.BUSINESS_DATE",hAmblCheckDate);
            setValue("post.CURR_CODE",hPcceCurrCode);
            setValue("post.ACCT_CODE",acctCode[intn]);
            setValue("post.SRC_PGM",javaProgram);
            setValueLong("post.SRC_PGM_POSTSEQ", 1);
            setValue("post.POST_TYPE","P1");
            hTempAcctCodeSumAmt[intn] = convAmt(hTempAcctCodeSumAmt[intn]);
            setValueDouble("post.POST_TYPE_AMT",hTempAcctCodeSumAmt[intn]);
            setValue("post.POST_NOTE",hPostNote);
            setValue("post.MOD_TIME",sysDate + sysTime);	
            setValue("post.MOD_PGM",javaProgram);
            insertTable();
            if (dupRecord.equals("Y")) {
               showLogMessage("I"," insert initial act_post_log dup : ",hAmblCheckDate+" "+  
               hPcceCurrCode+" "+acctCode[intn]+" "+javaProgram+" "+"1" );
               deleteActPostLog();
               daoTable    = "act_post_log";
               extendField = "post.";
               setValue("post.BUSINESS_DATE",hAmblCheckDate);
               setValue("post.CURR_CODE",hPcceCurrCode);
               setValue("post.ACCT_CODE",acctCode[intn]);
               setValue("post.SRC_PGM",javaProgram);
               setValueLong("post.SRC_PGM_POSTSEQ", 1);
               setValue("post.POST_TYPE","P1");
               hTempAcctCodeSumAmt[intn] = convAmt(hTempAcctCodeSumAmt[intn]);
               setValueDouble("post.POST_TYPE_AMT",hTempAcctCodeSumAmt[intn]);
               setValue("post.POST_NOTE",hPostNote);
               setValue("post.MOD_TIME",sysDate + sysTime);	
               setValue("post.MOD_PGM",javaProgram);
               insertTable();
            }
 
        }
    }

    /***********************************************************************/
    void deleteActPostLog() throws Exception {
        daoTable  = "act_post_log";
        whereStr  = " where BUSINESS_DATE    = ? ";
        whereStr += "  and CURR_CODE         = ? ";      
        whereStr += "  and ACCT_CODE         = ? ";      
        setString(1, hAmblCheckDate);
        setString(2, hPcceCurrCode);
        setString(3, hPostAcctCode);
        deleteTable();
    }

    /***********************************************************************/
    void procThisActPostLog() throws Exception {

        for (int intn = 0; intn < 19; intn++) {
            hPostAcctCode    = acctCode[intn];
            if (!Arrays.asList("AF","CF","PF","AI").contains(hPostAcctCode))
                {continue;}
                
            hThisBusiE011CalBal = 0;
            hThisBusiDiffBal = 0;
            hSrcPgmPostseq = 0;
            hPostBalanceMark = "";

            daoTable    = "act_post_log";
            selectSQL   = " nvl(max(SRC_PGM_POSTSEQ), 0) + 1 as h_src_pgm_postseq";
            whereStr  = " where BUSINESS_DATE    = ? ";
            whereStr += "  and CURR_CODE         = ? ";      
            whereStr += "  and ACCT_CODE         = ? ";      
            whereStr += "  and SRC_PGM           = ? ";      
            setString(1, hBusiBusinessDate);
            setString(2, hPcceCurrCode);
            setString(3, acctCode[intn]);
            setString(4, javaProgram);
            int m = selectTable();
            if ( m == 0 )
            //{ 
            // comcr.err_rtn("select_this act_post_log notfound ", h_busi_business_date+" "+
            // h_pcce_curr_code+" "+acct_code[intn]+" "+javaProgram, h_call_batch_seqno);
            //}
              {
               showLogMessage("I"," select this act_post_log notfound : ",hBusiBusinessDate+" "+  
               hPcceCurrCode+" "+acctCode[intn]+" "+javaProgram+" "+"1" );
               return;
              }

            hSrcPgmPostseq = getValueLong("h_src_pgm_postseq");

            daoTable    = "act_post_log";
            extendField = "post.";
            setValue("post.BUSINESS_DATE",hBusiBusinessDate);
            setValue("post.CURR_CODE",hPcceCurrCode);
            setValue("post.ACCT_CODE",acctCode[intn]);
            setValue("post.SRC_PGM",javaProgram);
            setValueLong("post.SRC_PGM_POSTSEQ", hSrcPgmPostseq );
            setValue("post.POST_TYPE","P2");
            hTempAcctCodeSumAmt[intn] = convAmt(hTempAcctCodeSumAmt[intn]);
            setValueDouble("post.POST_TYPE_AMT",hTempAcctCodeSumAmt[intn]);
            setValue("post.POST_NOTE",hPostNote);
            setValue("post.MOD_TIME",sysDate + sysTime);	
            setValue("post.MOD_PGM",javaProgram);
            insertTable();
            if (dupRecord.equals("Y")) 
              { 
             //comcr.err_rtn("insert_this act_post_log ERROR ", h_busi_business_date+" "+
             //h_pcce_curr_code+" "+acct_code[intn]+" "+javaProgram+h_src_pgm_postseq, h_call_batch_seqno);
               showLogMessage("I"," insert this act_post_log dup : ",hBusiBusinessDate+" "+  
               hPcceCurrCode+" "+acctCode[intn]+" "+javaProgram+" "+hSrcPgmPostseq );
               
               daoTable    = "act_post_log";
               updateSQL   = " POST_TYPE               = 'P2',";
               updateSQL  += " POST_TYPE_AMT           = ?,";
               updateSQL  += " POST_NOTE              = ?,";
               updateSQL  += " MOD_PGM                = ?,";
               updateSQL  += " MOD_TIME                = sysdate ";
               whereStr  = " where BUSINESS_DATE    = ? ";
               whereStr += "  and CURR_CODE         = ? ";      
               whereStr += "  and ACCT_CODE         = ? ";      
               whereStr += "  and SRC_PGM           = ? ";      
               whereStr += "  and SRC_PGM_POSTSEQ     = ? ";      
               setDouble(1, hTempAcctCodeSumAmt[intn]);
               setString(2, hPostNote);
               setString(3, javaProgram);
               setString(4, hBusiBusinessDate);
               setString(5, hPcceCurrCode);
               setString(6, acctCode[intn]);
               setString(7, javaProgram);
               setLong(8, hSrcPgmPostseq);
               updateTable();
              } 
              
            extendField = "post.";
            daoTable    = "act_post_log";
            selectSQL   = " sum(decode(POST_TYPE,'P1',POST_TYPE_AMT,0)) h_sum_pre_busi_e011_bal,";
            selectSQL  += " sum(decode(POST_TYPE,'A1',POST_TYPE_AMT,0)) h_sum_this_busi_add_amt,";
            selectSQL  += " sum(decode(POST_TYPE,'C1',POST_TYPE_AMT,0)) h_sum_this_cancel_amt,";
            selectSQL  += " sum(decode(POST_TYPE,'D1',POST_TYPE_AMT,0)) h_sum_this_adjust_amt ";
            whereStr  = " where BUSINESS_DATE    = ? ";
            whereStr += "  and CURR_CODE         = ? ";      
            whereStr += "  and ACCT_CODE         = ? ";      
            setString(1, hBusiBusinessDate);
            setString(2, hPcceCurrCode);
            setString(3, acctCode[intn]);
            int n = selectTable();
            if ( n == 0 )
              { 
               comcr.errRtn("select_SUM this act_post_log ERROR ", hBusiBusinessDate+" "+
               hPcceCurrCode+" "+acctCode[intn], hCallBatchSeqno);
              }
            hThisBusiE011CalBal = getValueDouble("post.h_sum_pre_busi_e011_bal") 	
                                     + getValueDouble("post.h_sum_this_busi_add_amt") 	
                                     + getValueDouble("post.h_sum_this_adjust_amt")
                                     + getValueDouble("post.h_sum_this_cancel_amt"); 
           
            hThisBusiE011CalBal       = convAmt(hThisBusiE011CalBal);
            hTempAcctCodeSumAmt[intn] = convAmt(hTempAcctCodeSumAmt[intn]);
            hThisBusiDiffBal           = hThisBusiE011CalBal
                                           - hTempAcctCodeSumAmt[intn];

            if (hThisBusiDiffBal     == 0) 
               hPostBalanceMark = "Y";
            else
               hPostBalanceMark = "N";
                
            daoTable    = "act_post_log";
            updateSQL   = " POST_BALANCE_MARK  = ?,";
            updateSQL  += " MOD_PGM              = ?, ";
            updateSQL  += " BAL_FLAG_MOD_TIME    = sysdate ";
            whereStr  = " where BUSINESS_DATE    = ? ";
            whereStr += "  and CURR_CODE         = ? ";      
            whereStr += "  and ACCT_CODE         = ? ";      
            setString(1, hPostBalanceMark);
            setString(2, javaProgram);
            setString(3, hBusiBusinessDate);
            setString(4, hPcceCurrCode);
            setString(5, acctCode[intn]);
            int u = updateTable();
            if ( u == 0 )
              { 
               comcr.errRtn("update_this act_post_log ERROR ", hBusiBusinessDate+" "+
               hPcceCurrCode+" "+acctCode[intn], hCallBatchSeqno);
              }
            
          //printPostBal(intn);

        }
    }
    
    /***********************************************************************/
    void generateGenPostLog() throws Exception {
                      
        hPreVouchDate = comcr.increaseDays(hBusiVouchDate, -1);
        checkGenVouchChg();

        selectPreActMasterBal();
        selectThisActMasterBal();

      //01- insert 六大本金 gen_post_log    
        hVoucAcNo = "14811101";
        hPreMasterBal  = getValueDouble("pmbal.bl_bal") 
                          + getValueDouble("pmbal.ca_bal") 
                          + getValueDouble("pmbal.id_bal") 
                          + getValueDouble("pmbal.it_bal") 
                          + getValueDouble("pmbal.ao_bal") 
                          + getValueDouble("pmbal.ot_bal"); 

        hThisMasterBal = getValueDouble("tmbal.bl_bal") 
                          + getValueDouble("tmbal.ca_bal") 
                          + getValueDouble("tmbal.id_bal") 
                          + getValueDouble("tmbal.it_bal") 
                          + getValueDouble("tmbal.ao_bal") 
                          + getValueDouble("tmbal.ot_bal"); 

        insertGenPostLog(hVoucAcNo);

      //02- insert 掛失費 gen_post_log    
        hVoucAcNo = "14811105";
        hPreMasterBal  = getValueDouble("pmbal.lf_bal"); 

        hThisMasterBal = getValueDouble("tmbal.lf_bal"); 

        insertGenPostLog(hVoucAcNo);

      //03- insert 催收款─本金 gen_post_log    
        hVoucAcNo = "14816001";
        hPreMasterBal  = getValueDouble("pmbal.cb_bal"); 

        hThisMasterBal = getValueDouble("tmbal.cb_bal"); 

        insertGenPostLog(hVoucAcNo);

      //04- insert 催收款─利息 gen_post_log    
        hVoucAcNo = "14816002";
        hPreMasterBal  = getValueDouble("pmbal.ci_bal"); 

        hThisMasterBal = getValueDouble("tmbal.ci_bal"); 

        insertGenPostLog(hVoucAcNo);

      //05- insert 催收款─費用+法訴費 gen_post_log    
        hVoucAcNo = "14816003";
        hPreMasterBal  = getValueDouble("pmbal.cc_bal")
                          + getValueDouble("pmbal.sf_bal"); 

        hThisMasterBal = getValueDouble("tmbal.cc_bal") 
                          + getValueDouble("tmbal.sf_bal"); 

        insertGenPostLog(hVoucAcNo);

      //06- insert 利息 gen_post_log    
        hVoucAcNo = "14834000";
        hPreMasterBal  = getValueDouble("pmbal.ri_bal"); 

        hThisMasterBal = getValueDouble("tmbal.ri_bal"); 

        insertGenPostLog(hVoucAcNo);

      //07- insert 逾期手續費 gen_post_log    
        hVoucAcNo = "14837000";
        hPreMasterBal  = getValueDouble("pmbal.pn_bal"); 

        hThisMasterBal = getValueDouble("tmbal.pn_bal"); 

        insertGenPostLog(hVoucAcNo);

      //08- insert 溢繳款 gen_post_log    
        hVoucAcNo = "27241504";
        hPreMasterBal  = getValueDouble("pmbal.op_bal")
                          + getValueDouble("pmbal.lk_bal"); 

        hThisMasterBal = getValueDouble("tmbal.op_bal") 
                          + getValueDouble("tmbal.lk_bal"); 

        insertGenPostLog(hVoucAcNo);

      //09- insert 基金 gen_post_log    
        hVoucAcNo = "24861700";
        hPreMasterBal  = getValueDouble("pmbal.fund_bal"); 

        hThisMasterBal = getValueDouble("tmbal.fund_bal"); 

        insertGenPostLog(hVoucAcNo);

      //10- insert 分期付款 gen_post_log    
        hVoucAcNo = "14811104";
        hPreMasterBal  = getValueDouble("pmbal.in_bal"); 

        hThisMasterBal = getValueDouble("tmbal.in_bal"); 

        insertGenPostLog(hVoucAcNo);

      //11- insert 問交或不合格帳單 gen_post_log    
        hVoucAcNo = "14815601";
        hPreMasterBal  = getValueDouble("pmbal.problem_bal")
                          + getValueDouble("pmbal.illicit_bal"); 

        hThisMasterBal = getValueDouble("tmbal.problem_bal") 
                          + getValueDouble("tmbal.illicit_bal"); 

        insertGenPostLog(hVoucAcNo);

      //12- insert VD 三大本金 gen_post_log    
        hVoucAcNo = "14816201";
        hPreMasterBal  = getValueDouble("pmbal.vd_bl_bal")
                          + getValueDouble("pmbal.vd_ca_bal") 
                          + getValueDouble("pmbal.vd_ot_bal"); 

        hThisMasterBal = getValueDouble("tmbal.vd_bl_bal") 
                          + getValueDouble("tmbal.vd_ca_bal") 
                          + getValueDouble("tmbal.vd_ot_bal"); 

        insertGenPostLog(hVoucAcNo);

      //13- insert VD 掛失費 gen_post_log    
        hVoucAcNo = "14816202";
        hPreMasterBal  = getValueDouble("pmbal.vd_lf_bal");

        hThisMasterBal = getValueDouble("tmbal.vd_lf_bal"); 

        insertGenPostLog(hVoucAcNo);

      //14- insert VD 問交或不合格帳單 gen_post_log    
        hVoucAcNo = "14816203";
        hPreMasterBal  = getValueDouble("pmbal.vd_problem_bal")
                          + getValueDouble("pmbal.vd_illicit_bal"); 

        hThisMasterBal = getValueDouble("tmbal.vd_problem_bal") 
                          + getValueDouble("tmbal.vd_illicit_bal"); 

        insertGenPostLog(hVoucAcNo);

      //15- insert VD 負項請款待回存金額 gen_post_log    
        hVoucAcNo = "24816202";
        hPreMasterBal  = getValueDouble("pmbal.vd_refund_bal");

        hThisMasterBal = getValueDouble("tmbal.vd_refund_bal"); 

        insertGenPostLog(hVoucAcNo);

      //16- insert 應付紅利特店款 gen_post_log    
        hVoucAcNo = "24815200";
        hPreMasterBal  = getValueDouble("pmbal.bonus_merchant")
                          + getValueDouble("pmbal.bonus_mmk") 
                          + getValueDouble("pmbal.bonus_ibon"); 

        hThisMasterBal = getValueDouble("tmbal.bonus_merchant") 
                          + getValueDouble("tmbal.bonus_mmk") 
                          + getValueDouble("tmbal.bonus_ibon"); 

        insertGenPostLog(hVoucAcNo);

    }
    
    /***********************************************************************/
    public void checkGenVouchChg() throws Exception {

        sqlCmd = "SELECT DISTINCT b.pre_vouch_date as h_pre_vouch_date "; 
        sqlCmd += "  FROM gen_vouch_chg a, gen_post_log b   ";
        sqlCmd += " WHERE a.old_tx_date = b.vouch_date   ";
        sqlCmd += "   AND a.old_tx_date = ?  ";
        setString(1, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
          showLogMessage("I", "", "select_gen_vouch_chg not found for old_tx_chg = "+hBusiBusinessDate);
        } else {
          hPreVouchDate = getValue("h_pre_vouch_date");
        }
    }

   /************************************************************************/
    public void  selectPreActMasterBal() throws Exception
    {
     extendField = "pmbal.";
     selectSQL   = ""; //放空值表示 select All columns 
     daoTable    = "act_master_bal";
     whereStr    = " where check_date      = ? ";
     whereStr   += "   and curr_code       = ? ";      

     setString(1 , hPreVouchDate);
     setString(2 , hPcceCurrCode);

     int recordCnt = selectTable();
    
     if (notFound.equals("Y")) {
         comcr.errRtn("select_pre_act_master_bal not found!", "", hCallBatchSeqno);
     }

   }

    /************************************************************************/
    public void  selectThisActMasterBal() throws Exception
    {
     extendField = "tmbal.";
     selectSQL   = ""; //放空值表示 select All columns 
     daoTable    = "act_master_bal";
     whereStr    = " where check_date      = ? ";
     whereStr   += "   and curr_code       = ? ";      

     setString(1 , hBusiVouchDate);
     setString(2 , hPcceCurrCode);

     int recordCnt = selectTable();
    
     if (notFound.equals("Y")) {
         comcr.errRtn("select_this_act_master_bal not found!", "", hCallBatchSeqno);
     }

   }

   /***********************************************************************/
    void insertGenPostLog(String txVoucAcNo) throws Exception {

        daoTable    = "gen_post_log";
        extendField = "post.";
        setValue("post.CURR_CODE",hPcceCurrCode);
        setValue("post.VOUCH_DATE",hBusiVouchDate);
        setValue("post.AC_NO",txVoucAcNo);
        setValue("post.PRE_VOUCH_DATE",hPreVouchDate);
        setValueDouble("post.PRE_MASTER_BAL",hPreMasterBal);
        setValueDouble("post.THIS_MASTER_BAL",hThisMasterBal);
        setValueDouble("post.THIS_VOUCH_DR_AMT",0);
        setValueDouble("post.THIS_VOUCH_CR_AMT",0);
        setValueDouble("post.ADJ_VOUCH_DR_AMT" ,0 );
        setValueDouble("post.ADJ_VOUCH_CR_AMT" ,0 );
        setValue("post.CRT_DATE",hBusiBusinessDate);
        setValue("post.CRT_USER",hVoucModUser);
        setValue("post.APR_DATE",hBusiBusinessDate);
        setValue("post.APR_USER",hVoucModUser);
        setValue("post.MOD_USER",hVoucModUser);	
        setValue("post.MOD_PGM",javaProgram);
        setValue("post.MOD_TIME",sysDate + sysTime);	
        setValue("post.MOD_PGM",javaProgram);
        setValueLong("post.MOD_SEQNO", 1);
        insertTable();
        if (dupRecord.equals("Y")) 
        { 
         //comcr.err_rtn("insert_this act_post_log ERROR ", h_busi_business_date+" "+
         //h_pcce_curr_code+" "+acct_code[intn]+" "+javaProgram+h_src_pgm_postseq, h_call_batch_seqno);
           showLogMessage("I",""," insert this gen_post_log dup : "+hBusiVouchDate+" "+  
           hPcceCurrCode+" "+hVoucAcNo+" "+javaProgram );
               
           daoTable    = "gen_post_log";
           extendField = "post.";
           
           updateSQL   = " PRE_VOUCH_DATE          = ?,";
           updateSQL  += " PRE_MASTER_BAL          = ?,";
           updateSQL  += " THIS_MASTER_BAL         = ?,";
           updateSQL  += " THIS_VOUCH_DR_AMT       = 0,";
           updateSQL  += " THIS_VOUCH_CR_AMT       = 0,";
           updateSQL  += " ADJ_VOUCH_DR_AMT        = 0,";
           updateSQL  += " ADJ_VOUCH_CR_AMT        = 0,";
           updateSQL  += " CRT_DATE                = ?,";
           updateSQL  += " CRT_USER                = ?,";
           updateSQL  += " APR_DATE                = ?,";
           updateSQL  += " APR_USER                = ?,";
           updateSQL  += " MOD_USER                = ?,";
           updateSQL  += " MOD_PGM                 = ?,";
           updateSQL  += " MOD_TIME                = sysdate ";
           
           whereStr    = " where VOUCH_DATE        = ? ";
           whereStr   += "   and CURR_CODE         = ? ";      
           whereStr   += "   and AC_NO             = ? ";      

           setString(1, hPreVouchDate);
           setDouble(2, hPreMasterBal);
           setDouble(3, hThisMasterBal);
           setString(4, hBusiBusinessDate);
           setString(5, hVoucModUser);
           setString(6, hBusiBusinessDate);
           setString(7, hVoucModUser);
           setString(8, hVoucModUser);
           setString(9, javaProgram);
           setString(10, hBusiVouchDate);
           setString(11, hPcceCurrCode);
           setString(12, txVoucAcNo);
           int n = updateTable();
           if ( n == 0 )
           { showLogMessage("E","","update_gen_post_log ERROR "+getValue("post.vouch_date")+" "+
           	 getValue("post.curr_code")+" "+getValue("post.ac_no") );  
           }

        } 
        
    }
  /***********************************************************************/
    void printPostBal(int seq) throws Exception {
        String temp2 = "";
        pageCnt2++;

        buf = "";
      //buf = comcr.insertStr(buf, " " + comcr.bank_name + " ", 26);
        buf = comcr.insertStrCenter(buf, " " + comcr.bankName + " ", 80);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱  :ACT_E011R1", 3);
        buf = comcr.insertStrCenter(buf, "現金制帳務科目過帳不平報表", 80);
        buf = comcr.insertStr(buf, "頁    次:", 63);
        temp2 = String.format("%04d", pageCnt2);
        buf = comcr.insertStr(buf, temp2, 72);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "執行日期:", 63);
        buf = comcr.insertStr(buf, hExecDate, 72);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "     過帳日期:", 10);
        buf = comcr.insertStr(buf, hExecDate, 24);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "     幣    別:", 10);
        buf = comcr.insertStr(buf, hPcceCurrCode, 24);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "     帳務科目:", 10);
        buf = comcr.insertStr(buf, acctCode[seq], 24);      /* 科目代號 */
        buf = comcr.insertStr(buf, hPostChiLongName[seq], 27);  /* 科目中文全稱 */
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "----------------------------------------------------------------------", 10);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "     前日餘額:", 10);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", getValueDouble("post.h_sum_pre_busi_e011_bal")); 
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, " (+) 本日新增:", 10);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", getValueDouble("post.h_sum_this_busi_add_amt")); 
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, " (+) 本日銷帳:", 10);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", getValueDouble("post.h_sum_this_cancel_amt")); 
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, " (+) 本日調整:", 10);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", getValueDouble("post.h_sum_this_adjust_amt")); 
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "======================================================================", 10);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, " (=)計算後餘額:", 10);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hThisBusiE011CalBal); 
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "      實際餘額:", 10);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hTempAcctCodeSumAmt[seq]); 
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "======================================================================", 10);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "        差異數:", 10);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", hThisBusiDiffBal); 
        buf = comcr.insertStr(buf, szTmp, 25);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", "\f"));

        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", "##PPP"));
    }

    /***********************************************************************/
    public double  convAmt(double cvtAmt) throws Exception
    {
      long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
      double cvtDouble =  ((double) cvtLong) / 100;
      return cvtDouble;
    }

    /***********************************************************************/
    void selectActCurrSum() throws Exception {
        sqlCmd = "select decode(a.curr_code,'','901',a.curr_code) h_acsm_curr_code,";
        sqlCmd += " a.acct_code,";
        sqlCmd += " sum(a.billed_end_bal)     h_acsm_billed_end_bal,";
        sqlCmd += " sum(a.unbill_end_bal)     h_acsm_unbill_end_bal,";
        sqlCmd += " count(*)                  h_acsm_card_spec_bal,";
        sqlCmd += " max(b.chi_long_name)      h_pcod_chi_long_name ";
        sqlCmd += "  from act_curr_sum a ,ptr_actcode b  ";
        sqlCmd += " where a.acct_code = b.acct_code ";
        sqlCmd += " GROUP BY decode(a.curr_code, '', '901',a.curr_code), a.acct_code ";
        sqlCmd += " ORDER BY decode(a.acct_code, 'BL', 1,'CA',2, 'ID',3 ,'IT',4 ,'AO',5 ,'OT',6 , 'LF',7 ,'RI',8 ,'PN',8 ,'AF',10,'CF',11,'PF',12, 'CB',13,'CI',14,'CC',15,'DB',16,'SF',17,'AI',18,'DP',19) ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aAcsmCurrCode[i] = getValue("h_acsm_curr_code", i);
            aAcsmAcctCode[i] = getValue("acct_code", i);
            aAcsmBilledEndBal[i] = getValueDouble("h_acsm_billed_end_bal", i);
            aAcsmUnbillEndBal[i] = getValueDouble("h_acsm_unbill_end_bal", i);
            aAcsmCardSpecBal[i] = getValueDouble("h_acsm_card_spec_bal", i);
            aPcodChiLongName[i] = getValue("h_pcod_chi_long_name", i);
        }

        actCurrSumCnt = recordCnt;

    }

    /***********************************************************************/
    void selectPtrCurrcode() throws Exception {
        sqlCmd = "select curr_code,";
        sqlCmd += " curr_chi_name ";
        sqlCmd += "  from ptr_currcode b  ";
        sqlCmd += " where bill_sort_seq != '' ORDER BY bill_sort_seq ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aPcceCurrCode[i] = getValue("curr_code", i);
            aPcceCurrChiName[i] = getValue("curr_chi_name", i);
        }

        ptrCurrcodeCnt = recordCnt;

    }

    /*****************************************************************************/
    void lpRtn(String lstr) throws Exception {
        String hPrintName = "";
        String hRptName = "";
        String lpStr = "";

        hRptName = lstr;

        sqlCmd = "select print_name ";
        sqlCmd += "  from bil_rpt_prt";
        sqlCmd += " where report_name like ? || '%'";
        sqlCmd += " fetch first 1 rows only";
        setString(1, hRptName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPrintName = getValue("print_name");
        }

        if (hPrintName.length() > 0) { /* 當表單代號及對應印表機存在bil_rpt_prt時才印 */
            lpStr = String.format("lp -d %s %s/reports/%s_%s", hPrintName, comc.getECSHOME(), lstr,
                    hBusiBusinessDate.substring(4));
            lpStr = Normalizer.normalize(lpStr, java.text.Normalizer.Form.NFKD);
            // comc.systemCmd(lp_str);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActE011 proc = new ActE011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
