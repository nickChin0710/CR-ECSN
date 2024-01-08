/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE    Version    AUTHOR                       DESCRIPTION              *
*  --------  -------------------  ------------------------------------------  *
* 107/04/11  V1.01.01   林志鴻             ECS-s1070205-013 program initial            *
* 107/06/13  V1.02.01   林志鴻             BECS-1070613-046 修改撈檔邏輯改善效能                               *
* 107/07/03  V2.00.00   HESYUAN   RECS-s1070628-053 增加特店中文名稱欄位和                        *
*                                                   無資料時傳空檔                                       *
* 107/09/19  V3.00.00   David     transfer to JAVA                            *
* 110/08/05  V4.00.00   Justin    fix the insertion of tmp_bn_data
* 110/12/02  V4.00.01   Brian     mantis#9093 add insert_cyc_pos_entry        *
* 111/01/06  V4.00.02   Brian     mantis#9187 欄位unlimit_start_month以fund_crt_date_s 取代      
* 111-11-11  V1.00.01   Machao    sync from mega & updated for project coding standard *
*******************************************************************************/
package Cyc;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class CycA181 extends AccessDAO {
    public final boolean DEBUG = false;

    private final String PROGNAME = "消費款轉基金由聯名主計算產生檔案處理程式 111-11-11  V1.00.01";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";

    private int    fptr1      = -1;
    private int    totalCnt  = 0;
    private int    totalAll  = 0;
    private int    insertCnt = 0;
    private String filename   = "";

    private String hBusiBusinessDate   = "";
    private String hTempProgramCode    = "";
    private String hTempFeedbackType   = "";
    private String hTempLastAcctMonth = "";
    private String hParmZipPswd        = "";

    private String hWdayStmtCycle      = "";
    private String hWdayThisAcctMonth = "";

    private String hFundCobrandCode    = "";
    private String hFundFundCode       = "";
    private String hFundFeedbackType   = "";
    private String hFundBlCond         = "";
    private String hFundCaCond         = "";
    private String hFundIdCond         = "";
    private String hFundAoCond         = "";
    private String hFundItCond         = "";
    private String hFundOtCond         = "";
    private String hFundMerchantSel    = "";
    private String hFundSourceCodeSel = "";
    // private String h_fund_ex_group_flag = "";
    private String hFundProgramExeType = "";
    private String hFundNewHldrCond    = "";
    private String hFundNewHldrCard    = "";
    private String hFundNewHldrSup     = "";
    // private String h_fund_new_hldr_group_flag = "";
    private long   hFundNewHldrDays     = 0;
    private int    hFundCalMonths        = 0;
    private int    hFundCardFeedMonths2 = 0;
    private int    hFundCardFeedRunDay = 0;
    private int    hFundCardFeedDays    = 0;
    private double hFundPurchReclowAmt  = 0;
    private String hTempStartMonth       = "";
    private String hFundCalSMonth       = "";
    private String hFundCalEMonth       = "";
    // private String h_fund_unlimit_start_month = "";
    private String hFundGroupCodeSel    = "";
    private String hFundAcctTypeSel     = "";
    private String hFundCardTypeSel     = "";
    private String hFundCardFeedDateS  = "";
    private String hFundCardFeedDateE  = "";
    private String hFundCardFeedFlag    = "";
    private String hFundFundFeedFlag    = "";
    private String hFundPurchFeedFlag   = "";
    private String hFundPurchDateS      = "";
    private String hFundPurchDateE      = "";
    private String hFundPurchReclowCond = "";
    private String hFundCurrencySel      = "";
    private String hFundExCurrencySel   = "";
    private String hFundActivateCond     = "";
    private String hFundActivateFlag     = "";
    private String hFundPosEntrySel     = "";
    private String hFundApplyAgeCond    = "";
    private int    hFundApplyAgeS       = 0;
    private int    hFundApplyAgeE       = 0;
    private String hFundPosMerchantSel  = "";
    private String hFundTranBase         = "";

    private String hTbdaProcDate     = "";
    private String hTbdaProcMark     = "";
    private String hBillCardNo       = "";
    private String hBillReferenceNo  = "";
    private String hBillPurchaseDate = "";
    private String hBillAcctKey      = "";
    private String hBillAcctMonth    = "";
    private double hBillDestAmt      = 0;
    private String hTbdaMajorCardNo = "";
    private String hTbdaIdPSeqno    = "";
    private String hTbdaCardNo       = "";
    private String hTbdaIssueDate    = "";
    private String hTbdaRowid         = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 3) {
                comc.errExit("Usage : CycA181 [[feedback_type] [business_date] [program_code]] ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hBusiBusinessDate = "";
            hTempProgramCode = "";
            hTempFeedbackType = "0";

            if (args.length == 1) {

                if (args[0].length() == 1) {
                    hTempFeedbackType = args[0];
                } else if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                } else {
                    hTempProgramCode = args[0];
                }

            } else if (args.length == 2) {

                if (args[0].length() == 1) {
                    hTempFeedbackType = args[0];
                } else if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                } else {
                    hTempProgramCode = args[0];
                }

                if (args[1].length() == 8) {
                    hBusiBusinessDate = args[1];
                } else {
                    hTempProgramCode = args[1];
                }

            } else if (args.length == 3) {

                if (args[0].length() == 1) {
                    hTempFeedbackType = args[0];
                } else if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                } else {
                    hTempProgramCode = args[0];
                }

                if (args[1].length() == 8) {
                    hBusiBusinessDate = args[1];
                } else {
                    hTempProgramCode = args[1];
                }

                if (args[2].length() == 8) {
                    hBusiBusinessDate = args[2];
                } else {
                    hTempProgramCode = args[2];
                }
            }

            selectPtrBusinday();

            showLogMessage("I", "", "====================================");
            showLogMessage("I", "", String.format("參數[%d] ", args.length));
            for (int inti = 0; inti < args.length; inti++)
                showLogMessage("I", "", String.format("[%s] ", args[inti]));
            showLogMessage("I", "", "====================================");

            selectPtrFundp0();
            
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束");
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /*****************************************************************************/
    private void selectPtrBusinday() throws Exception {
        sqlCmd = "SELECT decode(cast(? as varchar(8)), '', business_date, cast(? as varchar(8))) as h_busi_business_date, ";
        sqlCmd += "to_char(add_months(to_date( ";
        sqlCmd += "                 decode(cast(? as varchar(8)), '', business_date, cast(? as varchar(8))), ";
        sqlCmd += "                 'yyyymmdd'),-1),'yyyymm') as h_temp_last_acct_month, ";
        sqlCmd += "'MEGA' ||decode(cast(? as varchar(8)), '', business_date, cast(? as varchar(8))) ||'OPAY' as h_parm_zip_pswd ";
        sqlCmd += " FROM   ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_busi_business_date");
        hTempLastAcctMonth = getValue("h_temp_last_acct_month");
        hParmZipPswd = getValue("h_parm_zip_pswd");

        showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));
    }

    /**************************************************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayStmtCycle = "";
        hWdayThisAcctMonth = "";

        sqlCmd = "SELECT stmt_cycle, ";
        sqlCmd += "this_acct_month ";
        sqlCmd += "FROM  ptr_workday ";
        sqlCmd += "where this_close_date = ? ";
        setString(1, hBusiBusinessDate);
        selectTable();
        if (notFound.equals("Y"))
            return 1;

        hWdayStmtCycle = getValue("stmt_cycle");
        hWdayThisAcctMonth = getValue("this_acct_month");

        return 0;
    }

    /*****************************************************************************/
    void selectPtrFundp0() throws Exception {
        sqlCmd = "SELECT  distinct cobrand_code ";
        sqlCmd += "FROM    ptr_fundp ";
        sqlCmd += "WHERE   valid_period  = 'S' "; // 基金產生方式 Y.本行基金 E.聯名主紅利 S.聯名主計算
        sqlCmd += "and     fund_code = decode(cast(? as varchar(10)), '', fund_code, cast(? as varchar(10))) ";
        sqlCmd += "and     cast(? as varchar(8)) between decode(fund_crt_date_s, '', '20000101', fund_crt_date_s) ";
        sqlCmd += "                              and     decode(fund_crt_date_e, '', '30001231', fund_crt_date_e)  ";
        sqlCmd += "and     feedback_type = decode(cast(? as varchar(10)),'0',feedback_type,cast(? as varchar(10))) ";
        sqlCmd += "and     cobrand_code = decode(cast(? as varchar(10)),'0','OPAY',cobrand_code) ";
        sqlCmd += "and     (decode(purch_feed_flag, '', 'N', purch_feed_flag) = 'Y' ";
        sqlCmd += " or      decode(fund_feed_flag, '', 'N', fund_feed_flag)  = 'Y') ";
        sqlCmd += "and     tran_base     in ('B','C') "; // in ('2','5')
        int index = 1;
        setString(index++, hTempProgramCode);
        setString(index++, hTempProgramCode);
        setString(index++, hBusiBusinessDate);
        setString(index++, hTempFeedbackType);
        setString(index++, hTempFeedbackType);
        setString(index++, hTempFeedbackType);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hFundCobrandCode = getValue("cobrand_code", i);

            checkOpen();
            /*** 1.1撈檔案 ***/
            selectPtrFundp();
            /*** 關閉檔案 ***/
            closeOutputText(fptr1);

            if (totalAll > 0 || hFundCobrandCode.equals("OPAY")) {
                /*** 1.2檔案PKZIP壓縮加密 ***/
                showLogMessage("I", "", String.format("password = %s", hParmZipPswd));

                /*** PKZIP 壓縮 ***/
                if (DEBUG)
                    showLogMessage("I", "", String.format("壓縮 str=[%s]", filename));
                String zipFile = String.format("%s/media/%s/%s_bill_%8.8s.zip", comc.getECSHOME(), hFundCobrandCode,
                        hFundCobrandCode, hBusiBusinessDate);
                zipFile = Normalizer.normalize(zipFile, java.text.Normalizer.Form.NFKD);
                int tmpInt = comm.zipFile(filename, zipFile, hParmZipPswd);
                if (tmpInt == 0) {
                    /*** 1.3上傳至TM系統 ***/
                    ftpProc();
                }
            }
        }
    }
    

    // ************************************************************************
    int insertCycPosEntry() throws Exception {

        sqlCmd = "INSERT INTO cyc_pos_entry( ";
        sqlCmd += "       p_seqno, ";
        sqlCmd += "       reference_no, ";
        sqlCmd += "       fund_code, ";
        sqlCmd += "       acct_month, ";
        sqlCmd += "       dest_amt, ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       major_card_no, ";
        sqlCmd += "       card_no, ";
        sqlCmd += "       stmt_cycle, ";
        sqlCmd += "       mod_time, ";
        sqlCmd += "       mod_pgm ";
        sqlCmd += "       ) ";
        sqlCmd += "SELECT ";
        sqlCmd += "       p_seqno, ";//gp_no
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       major_card_no, ";
        sqlCmd += "       end_card_no, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       sysdate, ";
        sqlCmd += "       ?  ";
        sqlCmd += "FROM   crd_card a ";
        sqlCmd += "WHERE  card_no  = ? ";
        int index = 1;
        setString(index++, hBillReferenceNo);
        setString(index++, hFundFundCode);
        setString(index++, hBillAcctMonth);
        setDouble(index++, hBillDestAmt);
        setString(index++, getValue("stmt_cycle"));
        setString(index++, javaProgram);
        setString(index++, hBillCardNo);

        insertTable();

        return (0);
    }

    /*****************************************************************************/
    void selectPtrFundp() throws Exception {
        hFundFundCode = "";
        hFundFeedbackType = "";
        hFundBlCond = "";
        hFundCaCond = "";
        hFundIdCond = "";
        hFundAoCond = "";
        hFundItCond = "";
        hFundOtCond = "";
        hFundMerchantSel = "";
        hFundSourceCodeSel = "";
        // h_fund_ex_group_flag = "";
        hFundProgramExeType = "";
        hFundNewHldrCond = "";
        hFundNewHldrCard = "";
        hFundNewHldrSup = "";
        // h_fund_new_hldr_group_flag = "";
        hFundNewHldrDays = 0;
        hFundCalMonths = 0;
        hFundCardFeedMonths2 = 0;
        hFundCardFeedRunDay = 0;
        hFundCardFeedDays = 0;
        hFundPurchReclowAmt = 0;
        hTempStartMonth = "";
        hFundCalSMonth = "";
        hFundCalEMonth = "";
        // h_fund_unlimit_start_month = "";
        hFundGroupCodeSel = "";
        hFundAcctTypeSel = "";
        hFundCardTypeSel = "";
        hFundCardFeedDateS = "";
        hFundCardFeedDateE = "";
        hFundCardFeedFlag = "";
        hFundFundFeedFlag = "";
        hFundPurchFeedFlag = "";
        hFundPurchDateS = "";
        hFundPurchDateE = "";
        hFundPurchReclowCond = "";
        hFundCurrencySel = "";
        hFundExCurrencySel = "";
        hFundActivateCond = "";
        hFundActivateFlag = "";
        hFundPosEntrySel = "";
        hFundApplyAgeCond = "";
        hFundApplyAgeS = 0;
        hFundApplyAgeE = 0;
        hFundPosMerchantSel = "";
        hFundTranBase = "";

        sqlCmd = "SELECT  fund_code, ";
        sqlCmd += "        feedback_type, ";
        sqlCmd += "        bl_cond, ";
        sqlCmd += "        ca_cond, ";
        sqlCmd += "        id_cond, ";
        sqlCmd += "        ao_cond, ";
        sqlCmd += "        it_cond, ";
        sqlCmd += "        ot_cond, ";
        sqlCmd += "        merchant_sel, ";
        sqlCmd += "        source_code_sel, ";
        // sqlCmd += " ex_group_flag, ";
        sqlCmd += "        program_exe_type, ";
        sqlCmd += "        new_hldr_cond, ";
        sqlCmd += "        new_hldr_card, ";
        sqlCmd += "        new_hldr_sup, ";
        // sqlCmd += " new_hldr_group_flag, ";
        sqlCmd += "        new_hldr_days, ";
        sqlCmd += "        cal_months, ";
        sqlCmd += "        card_feed_months2, ";
        sqlCmd += "        card_feed_run_day, ";
        sqlCmd += "        card_feed_days, ";
        sqlCmd += "        purch_reclow_amt, ";
        sqlCmd += "        decode(feedback_type, ";
        sqlCmd += "          '1',decode(sign(substr(?, 1, 6) - substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6)), ";
        sqlCmd += "            1,to_char(add_months(to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'), ";
        sqlCmd += "              ceil(months_between(to_date(substr(?,1,6),'yyyymm'), ";
        sqlCmd += "              to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'))/feedback_months-0.0001)*feedback_months), ";
        sqlCmd += "              'yyyymm'), ";
        sqlCmd += "              to_char(add_months(to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'), ";
        sqlCmd += "              ceil(months_between(to_date(substr(?,1,6),'yyyymm'), ";
        sqlCmd += "              to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'))/feedback_months+0.0001)*feedback_months), ";
        sqlCmd += "              'yyyymm')), ";
        sqlCmd += "             '200001') as h_temp_start_month, ";
        sqlCmd += "        decode(feedback_type, ";
        sqlCmd += "          '1',decode(sign(substr(?,1,6) - substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6)), ";
        sqlCmd += "            1,to_char(add_months(to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'), ";
        sqlCmd += "              ceil(months_between(to_date(substr(?,1,6),'yyyymm'), ";
        sqlCmd += "              to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'))/feedback_months-0.0001)* ";
        sqlCmd += "              feedback_months-feedback_months),'yyyymm'), ";
        sqlCmd += "              to_char(add_months(to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'), ";
        sqlCmd += "              ceil(months_between(to_date(substr(?,1,6),'yyyymm'), ";
        sqlCmd += "              to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'))/feedback_months+0.0001)* ";
        sqlCmd += "              feedback_months-feedback_months),'yyyymm')), ";
        sqlCmd += "          '2',decode(program_exe_type,'1',nvl(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'10000101'),'2',cal_s_month,null)) as h_fund_cal_s_month, ";
        sqlCmd += "        decode(feedback_type, ";
        sqlCmd += "          '1',decode(sign(substr(?,1,6) - substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6)), ";
        sqlCmd += "            1,to_char(add_months(to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'), ";
        sqlCmd += "              ceil(months_between(to_date(substr(?,1,6),'yyyymm'), ";
        sqlCmd += "              to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'))/feedback_months-0.0001)* ";
        sqlCmd += "              feedback_months-1),'yyyymm'), ";
        sqlCmd += "              to_char(add_months(to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'), ";
        sqlCmd += "              ceil(months_between(to_date(substr(?,1,6),'yyyymm'), ";
        sqlCmd += "              to_date(substr(decode(fund_crt_date_s,'',?,fund_crt_date_s), 1, 6),'yyyymm'))/feedback_months+0.0001)* ";
        sqlCmd += "              feedback_months-1),'yyyymm')), ";
        sqlCmd += "          '2',decode(program_exe_type,'1','30001231','2',cal_e_month,'')) as h_fund_cal_e_month, ";
        // sqlCmd += " unlimit_start_month, ";
        sqlCmd += "        group_code_sel, ";
        sqlCmd += "        acct_type_sel, ";
        sqlCmd += "        card_type_sel, ";
        sqlCmd += "        decode(card_feed_date_s, '','10000101', card_feed_date_s) as card_feed_date_s, ";
        sqlCmd += "        decode(card_feed_date_e, '','30001231', card_feed_date_e) as card_feed_date_e, ";
        sqlCmd += "        card_feed_flag, ";
        sqlCmd += "        fund_feed_flag, ";
        sqlCmd += "        purch_feed_flag, ";
        sqlCmd += "        decode(purch_date_s, '','10000101', purch_date_s) as purch_date_s, ";
        sqlCmd += "        decode(purch_date_e, '','30001231', purch_date_e) as purch_date_e, ";
        sqlCmd += "        purch_reclow_cond, ";
        sqlCmd += "        currency_sel, ";
        sqlCmd += "        ex_currency_sel, ";
        sqlCmd += "        activate_cond, ";
        sqlCmd += "        activate_flag, ";
        sqlCmd += "        decode(pos_entry_sel, '','0', pos_entry_sel) as pos_entry_sel, ";
        sqlCmd += "        decode(APPLY_AGE_COND, '','N', APPLY_AGE_COND) as apply_age_cond, ";
        sqlCmd += "        apply_age_s, ";
        sqlCmd += "        apply_age_e, ";
        sqlCmd += "        decode(POS_MERCHANT_SEL, '','0', POS_MERCHANT_SEL) as POS_MERCHANT_SEL, ";
        sqlCmd += "        tran_base ";
        sqlCmd += "FROM    ptr_fundp ";
        sqlCmd += "WHERE   valid_period  = 'S'   "; // 基金產生方式 Y.本行基金 E.聯名主紅利 S.聯名主計算
        sqlCmd += "and     cobrand_code = ? ";
        sqlCmd += "and     fund_code = decode(cast(? as varchar(10)), '',fund_code,cast(? as varchar(10))) ";
        sqlCmd += "and     cast(? as varchar(8)) between decode(fund_crt_date_s, '','20000101', fund_crt_date_s) ";
        sqlCmd += "                              and     decode(fund_crt_date_e, '','30001231', fund_crt_date_e)  ";
        sqlCmd += "and     feedback_type = decode(cast(? as varchar(10)),'0',feedback_type,cast(? as varchar(10))) ";
        sqlCmd += "and     (decode(purch_feed_flag, '', 'N', purch_feed_flag) = 'Y' ";
        sqlCmd += " or      decode(fund_feed_flag, '', 'N', fund_feed_flag)  = 'Y') ";
        sqlCmd += "and     tran_base     in ('B','C') "; // (2, 5)
        int index = 1;
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        setString(index++, hFundCobrandCode);
        setString(index++, hTempProgramCode);
        setString(index++, hTempProgramCode);
        setString(index++, hBusiBusinessDate);
        setString(index++, hTempFeedbackType);
        setString(index++, hTempFeedbackType);
        extendField = "fund.";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {

            hFundFundCode = getValue("fund.fund_code", i);
            hFundFeedbackType = getValue("fund.feedback_type", i);
            hFundBlCond = getValue("fund.bl_cond", i);
            hFundCaCond = getValue("fund.ca_cond", i);
            hFundIdCond = getValue("fund.id_cond", i);
            hFundAoCond = getValue("fund.ao_cond", i);
            hFundItCond = getValue("fund.it_cond", i);
            hFundOtCond = getValue("fund.ot_cond", i);
            hFundMerchantSel = getValue("fund.merchant_sel", i);
            hFundSourceCodeSel = getValue("fund.source_code_sel", i);
            // h_fund_ex_group_flag = getValue("ex_group_flag", i);
            hFundProgramExeType = getValue("fund.program_exe_type", i);
            hFundNewHldrCond = getValue("fund.new_hldr_cond", i);
            hFundNewHldrCard = getValue("fund.new_hldr_card", i);
            hFundNewHldrSup = getValue("fund.new_hldr_sup", i);
            // h_fund_new_hldr_group_flag = getValue("new_hldr_group_flag", i);
            hFundNewHldrDays = getValueLong("fund.new_hldr_days", i);
            hFundCalMonths = getValueInt("fund.cal_months", i);
            hFundCardFeedMonths2 = getValueInt("fund.card_feed_months2", i);
            hFundCardFeedRunDay = getValueInt("fund.card_feed_run_day", i);
            hFundCardFeedDays = getValueInt("fund.card_feed_days", i);
            hFundPurchReclowAmt = getValueDouble("fund.purch_reclow_amt", i);
            hTempStartMonth = getValue("fund.h_temp_start_month", i);
            hFundCalSMonth = getValue("fund.h_fund_cal_s_month", i);
            hFundCalEMonth = getValue("fund.h_fund_cal_e_month", i);
            // h_fund_unlimit_start_month = getValue("unlimit_start_month", i);
            hFundGroupCodeSel = getValue("fund.group_code_sel", i);
            hFundAcctTypeSel = getValue("fund.acct_type_sel", i);
            hFundCardTypeSel = getValue("fund.card_type_sel", i);
            hFundCardFeedDateS = getValue("fund.card_feed_date_s", i);
            hFundCardFeedDateE = getValue("fund.card_feed_date_e", i);
            hFundCardFeedFlag = getValue("fund.card_feed_flag", i);
            hFundFundFeedFlag = getValue("fund.fund_feed_flag", i);
            hFundPurchFeedFlag = getValue("fund.purch_feed_flag", i);
            hFundPurchDateS = getValue("fund.purch_date_s", i);
            hFundPurchDateE = getValue("fund.purch_date_e", i);
            hFundPurchReclowCond = getValue("fund.purch_reclow_cond", i);
            hFundCurrencySel = getValue("fund.currency_sel", i);
            hFundExCurrencySel = getValue("fund.ex_currency_sel", i);
            hFundActivateCond = getValue("fund.activate_cond", i);
            hFundActivateFlag = getValue("fund.activate_flag", i);
            hFundPosEntrySel = getValue("fund.pos_entry_sel", i);
            hFundApplyAgeCond = getValue("fund.apply_age_cond", i);
            hFundApplyAgeS = getValueInt("fund.apply_age_s", i);
            hFundApplyAgeE = getValueInt("fund.apply_age_e", i);
            hFundPosMerchantSel = getValue("fund.POS_MERCHANT_SEL", i);
            hFundTranBase = getValue("fund.tran_base", i);

            showLogMessage("I", "", "====================================");
            if (hFundCobrandCode.equals("OPAY")) {
                showLogMessage("I", "", String.format("fund_code[%s]每日產出檔案給歐付寶 ", hFundFundCode));
                hTbdaProcDate = hBusiBusinessDate;
            } else if (hFundFeedbackType.equals("1")) {
                String tmpstr = String.format("%02d", hFundCardFeedRunDay);
                showLogMessage("I", "", String.format("fund_code[%s] run_day[%s] month between [%s]-[%s]", hFundFundCode,
                        tmpstr, hFundCalSMonth, hFundCalEMonth));
                if (comc.getSubString(hBusiBusinessDate, 6).equals(tmpstr) == false)
                    continue;
                hTbdaProcDate = String.format("%6.6s%02d", hTempStartMonth, hFundCardFeedRunDay);
                hWdayThisAcctMonth = hTempLastAcctMonth;
            } else if (hFundFeedbackType.equals("2")) {
                if (selectPtrWorkday() != 0) {
                    showLogMessage("I", "", String.format("fund_code[%s] only execute on cycle date", hFundFundCode));
                    continue;
                }
                if (hFundProgramExeType.equals("2")) {
                    showLogMessage("I", "", String.format("                 only execute between[%s]-[%s]",
                            hFundCalSMonth, hFundCalEMonth));
                    if ((hWdayThisAcctMonth.compareTo(hFundCalSMonth) < 0)
                            || (hWdayThisAcctMonth.compareTo(hFundCalEMonth) > 0))
                        continue;
                }

                showLogMessage("I", "", String.format("fund_code[%s] stmt_cycle[%s] ", hFundFundCode, hWdayStmtCycle));
                hTbdaProcDate = hBusiBusinessDate;
            }
            showLogMessage("I", "", String.format("       Process acct_month[%s]", hWdayThisAcctMonth));
            showLogMessage("I", "", "====================================");
            showLogMessage("I", "", "處理暫存檔開始.......");
            deleteTmpBillData();
            deleteTmpBnData();
            insertTmpBnData();
            showLogMessage("I", "", String.format("     Total process records[%d]", totalCnt));
            showLogMessage("I", "", "====================================");
            showLogMessage("I", "", "處理第一階段符合資格者");
            totalCnt = insertCnt = 0;
            if (hFundTranBase.equals("C")) // 5
                selectDbbBill();
            else {
                selectBilBill();
                updateTmpBillData();
            }

            showLogMessage("I", "", String.format("累計處理筆數[%d] 新增筆數[%d]", totalCnt, insertCnt));
            showLogMessage("I", "", "====================================");
            /************
             * 處理年輕族群
             *****************************************************************/
            if (hFundApplyAgeCond.equals("Y")) {
                showLogMessage("I", "", "\n處理年輕族群");
                totalCnt = insertCnt = 0;
                if (hFundTranBase.equals("C") == false) // 5
                    selectTmpBillDataA();
                showLogMessage("I", "", String.format("累計處理筆數[%d] 排除筆數[%d]", totalCnt, insertCnt));
            }
            /************
             * 排除開卡註記
             *****************************************************************/
            hTbdaProcMark = "0";
            if (hFundActivateCond.equals("Y")) {
                showLogMessage("I", "", "\n處理排除開卡註記\n");
                totalCnt = insertCnt = 0;
                if (hFundTranBase.equals("C") == false) // 5
                    selectTmpBillData1();
                hTbdaProcMark = "1";
                showLogMessage("I", "", String.format("累計處理筆數[%d] 排除筆數[%d]", totalCnt, insertCnt));
            }
            /************
             * 排除指定(團代)流通卡
             *********************************************************/
            if (hFundNewHldrCond.equals("Y")) {
                showLogMessage("I", "", "\n處理排除指定(團代)流通卡n");
                totalCnt = insertCnt = 0;
                if (hFundTranBase.equals("C") == false) // 5
                    selectTmpBillData2(selectTmpBnData());
                hTbdaProcMark = "2";
                showLogMessage("I", "", String.format("累計處理筆數[%d] 排除筆數[%d]", totalCnt, insertCnt));
            }

            selectTmpBillData();

        }
    }

    /*****************************************************************************/
    void selectBilBill() throws Exception {
        String hFundCobrandCodeFlag = hFundCobrandCode.equals("OPAY") ? "Y" : "N";
        sqlCmd = "SELECT  card_no, ";
        sqlCmd += "       stmt_cycle, ";
        sqlCmd += "       reference_no, ";
        sqlCmd += "       purchase_date, ";
        sqlCmd += "       (select acct_key from act_acno where acno_p_seqno = a.acno_p_seqno) as acct_key, ";
        sqlCmd += "       acct_month, ";
        sqlCmd += "       decode(txn_code,'25',dest_amt*-1, ";
        sqlCmd += "                        '27',dest_amt*-1, ";
        sqlCmd += "                        '28',dest_amt*-1, ";
        sqlCmd += "                        '29',dest_amt*-1, ";
        sqlCmd += "                        '06',dest_amt*-1,dest_amt+ decode(acct_code, 'IT',dc_curr_adjust_amt,0)) as h_bill_dest_amt  ";
        sqlCmd += "FROM   bil_bill a ";
        sqlCmd += "WHERE  1=1 ";
//        sqlCmd += "WHERE  stmt_cycle = decode(cast(? as varchar(8)),'OPAY',stmt_cycle,decode(cast(? as varchar(8)),'1',stmt_cycle,cast(? as varchar(8)))) ";
        sqlCmd += "AND    decode(merge_flag, '','N', merge_flag) != 'Y' ";
        sqlCmd += "AND    decode(rsk_type, '', 'x', rsk_type) not in ('1','2','3') ";
        sqlCmd += "AND    decode(curr_code, '', '901', curr_code) = '901' ";
        sqlCmd += "AND   ((? = 'N') ";
        sqlCmd += " or    (? = 'Y' and acct_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd'))) ";
        /************
         * 排除團代
         *********************************************************************/
        // sqlCmd += "AND ((? = 'N') ";
        // sqlCmd += " or (? = 'Y' ";
        // sqlCmd += " and not exists (select data_code ";
        // sqlCmd += " FROM tmp_bn_data ";
        // sqlCmd += " where table_name = ? ";
        // sqlCmd += " and data_type = '2' ";
        // sqlCmd += " and data_code = decode(a.group_code, '', '0000', a.group_code))))
        // ";
        /************
         * 排除團代
         *********************************************************************/
        /************
         * 來代
         *********************************************************************/
        sqlCmd += "AND   ((? = '0')  ";
        sqlCmd += " or    (? = '1' ";
        sqlCmd += "  and    exists   (select data_code ";
        sqlCmd += "                   FROM   tmp_bn_data  ";
        sqlCmd += "                   where  table_name   = ? ";
        sqlCmd += "                   and    data_type    = 'A' ";
        sqlCmd += "                   and    data_code    = decode(a.source_code, '', 'ZZ0000', a.source_code))) ";
        sqlCmd += " or    (? = '2' ";
        sqlCmd += "  and   not  exists   (select data_code ";
        sqlCmd += "                       FROM   tmp_bn_data ";
        sqlCmd += "                       where  table_name   = ? ";
        sqlCmd += "                       and    data_type    = 'A' ";
        sqlCmd += "                       and    data_code    = decode(a.source_code, '', 'ZZ0000', a.source_code)))) ";
        /************
         * 來代
         *********************************************************************/
        /************
         * 特店
         *********************************************************************/
        sqlCmd += "AND   ((? = '0') ";
        sqlCmd += " or    (? = '1' ";
        sqlCmd += "  and   exists   (select data_code ";
        sqlCmd += "                  FROM   tmp_bn_data ";
        sqlCmd += "                  where  table_name   = ? ";
        sqlCmd += "                  and    data_type    = '1' ";
        sqlCmd += "                  and    data_code    = a.mcht_no)) ";
        sqlCmd += " or    (? = '2' ";
        sqlCmd += "  and   not exists   (select data_code ";
        sqlCmd += "                      FROM   tmp_bn_data ";
        sqlCmd += "                      where  table_name   = ? ";
        sqlCmd += "                      and    data_type    = '1' ";
        sqlCmd += "                      and    data_code    = a.mcht_no))) ";
        /************
         * 特店
         *********************************************************************/
        /************
         * 指定排除交易幣別
         **************************************************************/
        sqlCmd += "AND    ((?  = '0' ";// N
        sqlCmd += "   and   ? = '0') ";// N
        sqlCmd += " or     (? = '2'  ";// Y
        sqlCmd += "   and    not exists (select data_type ";
        sqlCmd += "                        from tmp_bn_data h ";
        sqlCmd += "                       where table_name = ? ";
        sqlCmd += "                         and data_type  = '9' ";
        sqlCmd += "                         and h.data_code      = decode(h.data_code,'XXXX', h.data_code,a.bin_type) ";
        sqlCmd += "                         and h.data_code2     = decode(h.data_code2,'XXXX',h.data_code2,a.source_curr) ";
        sqlCmd += "                         and h.data_code3     = decode(h.data_code3,'XXXX',h.data_code3, ";
        sqlCmd += "                                  decode(a.mcht_category, '', 'XXXX', a.mcht_category)))) ";
        sqlCmd += "  or     (? = '1' ";// Y
        sqlCmd += "   and    exists (select data_type ";
        sqlCmd += "                    from tmp_bn_data h ";
        sqlCmd += "                   where table_name = ? ";
        sqlCmd += "                     and data_type  = '7' ";
        sqlCmd += "                     and h.data_code      = decode(h.data_code,'XXXX', h.data_code, a.bin_type) ";
        sqlCmd += "                     and h.data_code2     = decode(h.data_code2,'XXXX',h.data_code2,a.source_curr) ";
        sqlCmd += "                     and h.data_code3     = decode(h.data_code3,'XXXX',h.data_code3, ";
        sqlCmd += "                              decode(a.mcht_category, '', 'XXXX', a.mcht_category))))) ";
        /************
         * 指定排除交易幣別
         **************************************************************/
        /************
         * POST ENTRY MODE beg
         ************************************************************/
        sqlCmd += "AND   ((? = '0') ";
        sqlCmd += " or    (? = '1' ";
        sqlCmd += "  and  (  ";
        sqlCmd += "        exists   (select data_code ";
        sqlCmd += "                  FROM   tmp_bn_data ";
        sqlCmd += "                  where  table_name   = ? ";
        sqlCmd += "                  and    data_type    = 'B' ";
        sqlCmd += "                  and    data_code    = decode(data_code,'XXXX', data_code,decode(a.pos_entry_mode, '', 'x', a.pos_entry_mode)) ";
        sqlCmd += "                  and    data_code2   = decode(data_code2,'XXXX',data_code2,decode(a.ec_ind, '', 'x', a.ec_ind)) ";
        sqlCmd += "                  and    data_code3   = decode(data_code3,'XXXX',data_code3,decode(a.bin_type, '', 'x', a.bin_type))) ";
        sqlCmd += "       and  (  (? != '2') ";
        sqlCmd += "            or (? = '2' ";
        sqlCmd += "                and  ";
        sqlCmd += "                 not exists(select 1 from tmp_bn_data , mkt_rcv_bin  ";
        sqlCmd += "                             where  table_name   = ? ";
        sqlCmd += "                             and tmp_bn_data.data_type  = 'C' ";
        sqlCmd += "                             and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                             and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                             and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "               ) ";
        sqlCmd += "            ) ";
        sqlCmd += "       ) ";
        sqlCmd += "            or (? = '1'  ";
        sqlCmd += "                 and exists (select 1 from tmp_bn_data , mkt_rcv_bin  ";
        sqlCmd += "                             where  table_name   = ? ";
        sqlCmd += "                             and tmp_bn_data.data_type  = 'C' ";
        sqlCmd += "                             and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                             and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                             and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "               ) ";
        sqlCmd += "       )  ";
        sqlCmd += " or    (? = '2' ";
        sqlCmd += "  and  ( ";
        sqlCmd += "      not exists  (select data_code ";
        sqlCmd += "                      FROM   tmp_bn_data ";
        sqlCmd += "                      where  table_name   = ? ";
        sqlCmd += "                      and    data_type    = 'B' ";
        sqlCmd += "                      and    data_code    = decode(data_code,'XXXX',data_code,  decode(a.pos_entry_mode, '', 'x', a.pos_entry_mode)) ";
        sqlCmd += "                      and    data_code2   = decode(data_code2,'XXXX',data_code2,decode(a.ec_ind, '', 'x', a.ec_ind)) ";
        sqlCmd += "                      and    data_code3   = decode(data_code3,'XXXX',data_code3,decode(a.bin_type, '', 'x', a.bin_type)))  ";
        sqlCmd += "       and  (  (? != '2') ";
        sqlCmd += "            or (? = '2' ";
        sqlCmd += "               and  ";
        sqlCmd += "               not exists(select 1 from tmp_bn_data , mkt_rcv_bin  ";
        sqlCmd += "                             where  table_name   = ? ";
        sqlCmd += "                             and tmp_bn_data.data_type  = 'C'  ";
        sqlCmd += "                             and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                             and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                                 and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "                   ) ";
        sqlCmd += "                ) ";
        sqlCmd += "           ) ";
        sqlCmd += "                or ( ? = '1'     ";
        sqlCmd += "                     and exists (select 1 from tmp_bn_data , mkt_rcv_bin ";
        sqlCmd += "                                 where  table_name   = ? ";
        sqlCmd += "                                 and tmp_bn_data.data_type  = 'C' ";
        sqlCmd += "                                 and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                                 and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                                 and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "                   ) ";
        sqlCmd += "           ) )  ";
        /************
         * POST ENTRY MODE end
         ************************************************************/
        /************
         * 消費本金類
         ******************************************************************/
        sqlCmd += " AND   acct_code in (decode(cast(? as varchar(8)), 'Y','BL','XX'), ";
        sqlCmd += "                     decode(cast(? as varchar(8)), 'Y','CA','XX'), ";
        sqlCmd += "                     decode(cast(? as varchar(8)), 'Y','ID','XX'), ";
        sqlCmd += "                     decode(cast(? as varchar(8)), 'Y','AO','XX'), ";
        sqlCmd += "                     decode(cast(? as varchar(8)), 'Y','OT','XX'), ";
        sqlCmd += "                     decode(cast(? as varchar(8)), 'Y','IT','XX')) ";
        /************
         * 消費本金類
         ******************************************************************/
        /************
         * 執行對象-團代
         *****************************************************************/
        sqlCmd += "AND   ((? ='0') ";
        sqlCmd += " or    (? ='1' ";
        sqlCmd += "  and  exists (select data_code ";
        sqlCmd += "               FROM   tmp_bn_data     ";
        sqlCmd += "               where  table_name   = ? ";
        sqlCmd += "               and    data_type    = '3' ";
        sqlCmd += "               and    data_code    = decode(a.group_code, '', '0000', a.group_code)))) ";
        /************
         * 執行對象-團代
         *****************************************************************/
        /************
         * 執行對象-帳戶
         *****************************************************************/
        sqlCmd += "AND   ((?='0') ";
        sqlCmd += " or    (?='1' ";
        sqlCmd += "  and  exists (select data_code ";
        sqlCmd += "               FROM   tmp_bn_data ";
        sqlCmd += "               where  table_name   = ? ";
        sqlCmd += "               and    data_type    = '4' ";
        sqlCmd += "               and    data_code    = a.acct_type))) ";
        /************
         * 執行對象-帳戶
         *****************************************************************/
        /************
         * 執行對象-卡種
         *****************************************************************/
        sqlCmd += "AND   ((?='0') ";
        sqlCmd += " or    (?='1' ";
        sqlCmd += "  and  exists (select data_code ";
        sqlCmd += "               FROM   tmp_bn_data t,crd_card  s ";
        sqlCmd += "               where  table_name   = ? ";
        sqlCmd += "               and    data_type    = '5' ";
        sqlCmd += "                and   s.card_no    = a.card_no  ";
        sqlCmd += "                and   t.data_code  = s.bin_type))) ";
        /************
         * 執行對象-卡種
         *****************************************************************/
        sqlCmd += "AND  ((? = 'Y') ";
        sqlCmd += " or   (? = 'N' and acct_month = ?)) ";

        int index = 1;
//        setString(index++, h_fund_cobrand_code);
//        setString(index++, h_fund_feedback_type);
//        setString(index++, h_wday_stmt_cycle);
        setString(index++, hFundCobrandCodeFlag);
        setString(index++, hFundCobrandCodeFlag);
        setString(index++, hBusiBusinessDate);
        // setString(index++, h_fund_ex_group_flag);
        // setString(index++, h_fund_ex_group_flag);
        // setString(index++, h_fund_fund_code);
        setString(index++, hFundSourceCodeSel);
        setString(index++, hFundSourceCodeSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundSourceCodeSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundMerchantSel);
        setString(index++, hFundMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundCurrencySel);
        setString(index++, hFundExCurrencySel);
        setString(index++, hFundExCurrencySel);
        setString(index++, hFundFundCode);
        setString(index++, hFundCurrencySel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosEntrySel);
        setString(index++, hFundPosEntrySel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosEntrySel);
        setString(index++, hFundFundCode);

        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosMerchantSel);

        setString(index++, hFundFundCode);
        setString(index++, hFundBlCond);
        setString(index++, hFundCaCond);
        setString(index++, hFundIdCond);
        setString(index++, hFundAoCond);
        setString(index++, hFundOtCond);
        setString(index++, hFundItCond);

        setString(index++, hFundGroupCodeSel);
        setString(index++, hFundGroupCodeSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundAcctTypeSel);
        setString(index++, hFundAcctTypeSel);
        setString(index++, hFundFundCode);

        setString(index++, hFundCardTypeSel);
        setString(index++, hFundCardTypeSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundCobrandCodeFlag);
        setString(index++, hFundCobrandCodeFlag);
        setString(index++, hWdayThisAcctMonth);

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
//          sqlCmd += "WHERE  stmt_cycle = decode(cast(? as varchar(8)),'OPAY',stmt_cycle,decode(cast(? as varchar(8)),'1',stmt_cycle,cast(? as varchar(8)))) ";
//          setString(index++, h_fund_cobrand_code);
//          setString(index++, h_fund_feedback_type);
//          setString(index++, h_wday_stmt_cycle);
            
            /* 改善效能 */
            if (hFundCobrandCode.equals("OPAY") == false) {
                if (hFundFeedbackType.equals("1") == false) {
                    if (hWdayStmtCycle.equals(getValue("stmt_cycle")) == false) {
                        continue;
                    }
                }
            }
            
            hBillCardNo = getValue("card_no");
            hBillReferenceNo = getValue("reference_no");
            hBillPurchaseDate = getValue("purchase_date");
            hBillAcctKey = getValue("acct_key");
            hBillAcctMonth = getValue("acct_month");
            hBillDestAmt = getValueDouble("h_bill_dest_amt");

            /************
             * 排除指定(團代)流通卡
             *********************************************************/
            totalCnt++;
            if ((totalCnt % 5000) == 0)
                showLogMessage("I", "", String.format("Process records[%d]", totalCnt));

            if (hFundFundFeedFlag.equals("Y"))
                insertTmpBillData(0);

            if (hFundPurchFeedFlag.equals("Y")) {
                /************
                 * 消費門檻
                 **********************************************************************/
                if ((hBillPurchaseDate.compareTo(hFundPurchDateS) < 0)
                        || (hBillPurchaseDate.compareTo(hFundPurchDateE) > 0))
                    continue;
                if (hFundPurchReclowCond.equals("Y")) {
                    if ((hBillDestAmt > 0) && (hBillDestAmt < hFundPurchReclowAmt))
                        continue;
                }
                insertTmpBillData(1);
            }
            
            
            
        }
        closeCursor(cursorIndex); // End of for() statement
    }

    /*****************************************************************************/
    void insertTmpBillData(int hInt) throws Exception {
        insertCnt++;
        sqlCmd = "INSERT INTO tmp_bill_data( ";
        sqlCmd += "       fund_type, ";
        sqlCmd += "       proc_date, ";
        sqlCmd += "       proc_type, ";
        sqlCmd += "       p_seqno, ";
        sqlCmd += "       reference_no, ";
        sqlCmd += "       fund_code, ";
        sqlCmd += "       acct_month, ";
        sqlCmd += "       dest_amt, ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       acct_key, ";
        sqlCmd += "       issue_date, ";
        sqlCmd += "       major_card_no, ";
        sqlCmd += "       card_no, ";
        sqlCmd += "       id_p_seqno, ";
        sqlCmd += "       group_code, ";
        sqlCmd += "       current_code, ";
        sqlCmd += "       activate_flag, ";
        sqlCmd += "       proc_mark ";
        sqlCmd += "       ) ";
        sqlCmd += "SELECT decode(?,0,'0','1'), ";
        sqlCmd += "       ?, ";
        sqlCmd += "       decode(cast(? as varchar(8)),'2','1','3'), ";
        sqlCmd += "       p_seqno, ";//gp_no
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ori_issue_date, ";
        sqlCmd += "       major_card_no, ";
        sqlCmd += "       end_card_no, ";
        sqlCmd += "       id_p_seqno, ";
        sqlCmd += "       group_code, ";
        sqlCmd += "       current_code, ";
        sqlCmd += "       activate_flag, ";
        sqlCmd += "       'N' ";
        sqlCmd += "FROM   crd_card a ";
        sqlCmd += "WHERE  card_no  = ? ";
        /************
         * 回饋(產生)期間
         ****************************************************************/
        sqlCmd += "AND    ((?='1') ";
        sqlCmd += " or    (?='2') ";
        sqlCmd += " or    (?='3' ";
        sqlCmd += "  and   a.ori_issue_date between ?     and     ? ";
        sqlCmd += "   and  ((? = '1' ";
        sqlCmd += "     and  ? <= to_char(add_months(to_date(decode(a.ori_issue_date,'',null,a.ori_issue_date),'yyyymmdd'), ?-1),'yyyymm')) ";
        sqlCmd += "    or   (? = '2'  ";
        sqlCmd += "     and  ? > to_char(add_months(to_date(decode(a.ori_issue_date,'',null,a.ori_issue_date),'yyyymmdd'), ?-1),'yyyymm')) ";
        sqlCmd += "    or   (? = '3'  ";
        sqlCmd += "     and   ? <= to_char(to_date(decode(a.ori_issue_date,'',null,a.ori_issue_date),'yyyymmdd')+ (?-1) days,'yyyymmdd'))))) ";
        /************
         * 回饋(產生)期間
         ****************************************************************/
        int index = 1;
        setInt(index++, hInt);
        setString(index++, hTbdaProcDate);
        setString(index++, hFundFeedbackType);
        setString(index++, hBillReferenceNo);
        setString(index++, hFundFundCode);
        setString(index++, hBillAcctMonth);
        setDouble(index++, hBillDestAmt);
        setString(index++, hBillAcctKey);
        setString(index++, hBillCardNo);
        setString(index++, hFundProgramExeType);
        setString(index++, hFundProgramExeType);
        setString(index++, hFundProgramExeType);
        setString(index++, hFundCardFeedDateS);
        setString(index++, hFundCardFeedDateE);
        setString(index++, hFundCardFeedFlag);
        setString(index++, hBillAcctMonth);
        setInt(index++, hFundCalMonths);
        setString(index++, hFundCardFeedFlag);
        setString(index++, hBillAcctMonth);
        setInt(index++, hFundCardFeedMonths2);
        setString(index++, hFundCardFeedFlag);
        setString(index++, hBillPurchaseDate);
        setInt(index++, hFundCardFeedDays);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate", "", hCallBatchSeqno);
        }
        
        // 處理對帳單POS_ENTRY (cyc_pos_entry)...
        if (hFundPosEntrySel.equals("1"))
            insertCycPosEntry();

    }

    /*****************************************************************************/
    void selectDbbBill() throws Exception {
        sqlCmd = "SELECT card_no, ";
        sqlCmd += "       reference_no, ";
        sqlCmd += "       purchase_date, ";
        sqlCmd += "       (select acct_key from act_acno where acno_p_seqno = a.p_seqno) as acct_key, ";
        sqlCmd += "       acct_month, ";
        sqlCmd += "       decode(txn_code,'25',dest_amt*-1, ";
        sqlCmd += "                       '27',dest_amt*-1, ";
        sqlCmd += "                       '28',dest_amt*-1, ";
        sqlCmd += "                       '29',dest_amt*-1, ";
        sqlCmd += "                       '06',dest_amt*-1,dest_amt+ decode(acct_code, 'IT',curr_adjust_amt,0)) as h_bill_dest_amt  ";
        sqlCmd += "FROM   dbb_bill a ";
        sqlCmd += "WHERE  decode(stmt_cycle, '', 'x', stmt_cycle) = decode(cast(? as varchar(8)),'1',decode(stmt_cycle, '', 'x', stmt_cycle),'x')  "; /***
                                                                                                                                                       * 皆是null
                                                                                                                                                       ***/
        /***
         * bil_bill才有 AND nvl(merge_flag,'N') != 'Y' sqlCmd += " AND
         * nvl(curr_code,'901') = '901'
         ***/
        sqlCmd += "    AND   (rsk_type = '' ";
        sqlCmd += "     or    (rsk_type = '9' and (txn_code = '26' or rsk_apr_user != ''))) ";
        sqlCmd += "    AND   (? not in ('OPAY') ";
        sqlCmd += "     or    (? in ('OPAY') and (acct_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd') ";
        sqlCmd += "                                              or  rsk_apr_date = to_char(to_date(?,'yyyymmdd')-1 days,'yyyymmdd')))) ";
        /************
         * 排除團代
         *********************************************************************/
        // sqlCmd += " AND ((? = 'N') ";
        // sqlCmd += " or (? = 'Y' ";
        // sqlCmd += " and not exists (select data_code ";
        // sqlCmd += " FROM tmp_bn_data ";
        // sqlCmd += " where table_name = ? ";
        // sqlCmd += " and data_type = '2' ";
        // sqlCmd += " and data_code = decode(a.group_code, '', '0000', a.group_code))))
        // ";
        /************
         * 排除團代
         *********************************************************************/
        /************
         * 來代
         *********************************************************************/
        sqlCmd += "    AND   ((? = '0')  ";
        sqlCmd += "     or    (? = '1' ";
        sqlCmd += "      and    exists   (select data_code ";
        sqlCmd += "                       FROM   tmp_bn_data  ";
        sqlCmd += "                       where  table_name   = ? ";
        sqlCmd += "                       and    data_type    = 'A' ";
        sqlCmd += "                       and    data_code    = decode(a.source_code, '', 'ZZ0000', a.source_code))) ";
        sqlCmd += "     or    (? = '2' ";
        sqlCmd += "      and   not  exists   (select data_code ";
        sqlCmd += "                           FROM   tmp_bn_data ";
        sqlCmd += "                           where  table_name   = ? ";
        sqlCmd += "                           and    data_type    = 'A' ";
        sqlCmd += "                           and    data_code    = decode(a.source_code, '', 'ZZ0000', a.source_code)))) ";
        /************
         * 來代
         *********************************************************************/
        /************
         * 特店
         *********************************************************************/
        sqlCmd += "    AND   ((? = '0') ";
        sqlCmd += "     or    (? = '1' ";
        sqlCmd += "      and   exists   (select data_code ";
        sqlCmd += "                      FROM   tmp_bn_data ";
        sqlCmd += "                      where  table_name   = ? ";
        sqlCmd += "                      and    data_type    = '1' ";
        sqlCmd += "                      and    data_code    = a.mcht_no)) ";
        sqlCmd += "     or    (? = '2' ";
        sqlCmd += "      and   not exists   (select data_code ";
        sqlCmd += "                          FROM   tmp_bn_data ";
        sqlCmd += "                          where  table_name   = ? ";
        sqlCmd += "                          and    data_type    = '1' ";
        sqlCmd += "                          and    data_code    = a.mcht_no))) ";
        /************
         * 特店
         *********************************************************************/
        /************
         * 指定排除交易幣別
         **************************************************************/
        sqlCmd += "    AND    ((?  = 'N' ";
        sqlCmd += "       and   ? = 'N') ";
        sqlCmd += "     or     (? = 'Y'  ";
        sqlCmd += "       and    not exists (select data_type ";
        sqlCmd += "                            from tmp_bn_data h ";
        sqlCmd += "                           where table_name = ? ";
        sqlCmd += "                             and data_type  = '9' ";
        sqlCmd += "                             and h.data_code      = decode(h.data_code,'XXXX',h.data_code,a.bin_type) ";
        sqlCmd += "                             and h.data_code2     = decode(h.data_code2,'XXXX',h.data_code2,a.source_curr) ";
        sqlCmd += "                             and h.data_code3     = decode(h.data_code3,'XXXX',h.data_code3, ";
        sqlCmd += "                                      decode(a.mcht_category, '', 'XXXX', a.mcht_category)))) ";
        sqlCmd += "      or     (? = 'Y' ";
        sqlCmd += "       and    exists (select data_type ";
        sqlCmd += "                        from tmp_bn_data h ";
        sqlCmd += "                       where table_name = ? ";
        sqlCmd += "                         and data_type  = '7' ";
        sqlCmd += "                         and h.data_code      = decode(h.data_code,'XXXX',h.data_code,a.bin_type) ";
        sqlCmd += "                         and h.data_code2     = decode(h.data_code2,'XXXX',h.data_code2,a.source_curr) ";
        sqlCmd += "                         and h.data_code3     = decode(h.data_code3,'XXXX',h.data_code3, ";
        sqlCmd += "                                  decode(a.mcht_category, '', 'XXXX', a.mcht_category))))) ";
        /************
         * 指定排除交易幣別
         **************************************************************/
        /************
         * POST ENTRY MODE beg
         ************************************************************/
        sqlCmd += "    AND   ((? = '0') ";
        sqlCmd += "     or    (? = '1' ";
        sqlCmd += "      and  (   ";
        sqlCmd += "            exists   (select data_code ";
        sqlCmd += "                      FROM   tmp_bn_data ";
        sqlCmd += "                      where  table_name   = ? ";
        sqlCmd += "                      and    data_type    = 'B' ";
        sqlCmd += "                      and    data_code    = decode(data_code,'XXXX',data_code, decode(a.pos_entry_mode, '', 'x', a.pos_entry_mode)) ";
        sqlCmd += "                      and    data_code2   = decode(data_code2,'XXXX',data_code2,decode(a.ec_ind, '', 'x', a.ec_ind)) ";
        sqlCmd += "                      and    data_code3   = decode(data_code3,'XXXX',data_code3,decode(a.bin_type, '', 'x', a.bin_type))) ";
        sqlCmd += "           and  (  (? != '2') ";
        sqlCmd += "                or (? = '2' ";
        sqlCmd += "                    and  ";
        sqlCmd += "                     not exists(select 1 from tmp_bn_data , mkt_rcv_bin  ";
        sqlCmd += "                                 where  table_name   = ? ";
        sqlCmd += "                                 and tmp_bn_data.data_type  = 'C' ";
        sqlCmd += "                                 and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                                 and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                                 and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "                   ) ";
        sqlCmd += "                ) ";
        sqlCmd += "           ) ";
        sqlCmd += "                or (? = '1'    ";
        sqlCmd += "                     and exists (select 1 from tmp_bn_data , mkt_rcv_bin  ";
        sqlCmd += "                                 where  table_name   = ? ";
        sqlCmd += "                                 and tmp_bn_data.data_type  = 'C' ";
        sqlCmd += "                                 and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                                 and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                                 and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "                   ) ";
        sqlCmd += "           )  ";
        sqlCmd += "     or    (? = '2' ";
        sqlCmd += "      and  ( ";
        sqlCmd += "          not exists  (select data_code ";
        sqlCmd += "                          FROM   tmp_bn_data ";
        sqlCmd += "                          where  table_name   = ? ";
        sqlCmd += "                          and    data_type    = 'B' ";
        sqlCmd += "                          and    data_code    = decode(data_code,'XXXX',data_code,  decode(a.pos_entry_mode, '', 'x', a.pos_entry_mode)) ";
        sqlCmd += "                          and    data_code2   = decode(data_code2,'XXXX',data_code2,decode(a.ec_ind, '', 'x', a.ec_ind)) ";
        sqlCmd += "                          and    data_code3   = decode(data_code3,'XXXX',data_code3,decode(a.bin_type, '', 'x', a.bin_type)))  ";
        sqlCmd += "           and  (  (? != '2') ";
        sqlCmd += "                or (? = '2' ";
        sqlCmd += "                   and  ";
        sqlCmd += "                   not exists(select 1 from tmp_bn_data , mkt_rcv_bin  ";
        sqlCmd += "                                 where  table_name   = ? ";
        sqlCmd += "                                 and tmp_bn_data.data_type  = 'C'  ";
        sqlCmd += "                                 and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                                 and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                                 and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "                   ) ";
        sqlCmd += "                ) ";
        sqlCmd += "           ) ";
        sqlCmd += "                or ( ? = '1'  ";
        sqlCmd += "                     and exists (select 1 from tmp_bn_data , mkt_rcv_bin ";
        sqlCmd += "                                 where  table_name   = ? ";
        sqlCmd += "                                 and tmp_bn_data.data_type  = 'C' ";
        sqlCmd += "                                 and lpad(mkt_rcv_bin.ica_no,8,'0') = lpad(decode(a.acq_member_id, '', '0', a.acq_member_id),8,'0') ";
        sqlCmd += "                                 and tmp_bn_data.data_code = mkt_rcv_bin.bank_no  ";
        sqlCmd += "                                 and tmp_bn_data.data_code2 = a.mcht_no) ";
        sqlCmd += "                   ) ";
        sqlCmd += "           ) )    ";
        /************
         * POST ENTRY MODE end
         ************************************************************/
        /************
         * 消費本金類
         ******************************************************************/
        sqlCmd += "    AND   acct_code in (decode(cast(? as varchar(8)), 'Y','BL','XX'), ";
        sqlCmd += "                        decode(cast(? as varchar(8)), 'Y','CA','XX'), ";
        sqlCmd += "                        decode(cast(? as varchar(8)), 'Y','ID','XX'), ";
        sqlCmd += "                        decode(cast(? as varchar(8)), 'Y','AO','XX'), ";
        sqlCmd += "                        decode(cast(? as varchar(8)), 'Y','OT','XX'), ";
        sqlCmd += "                        decode(cast(? as varchar(8)), 'Y','IT','XX')) ";
        /************
         * 消費本金類
         ******************************************************************/
        /************
         * 執行對象-團代
         *****************************************************************/
        sqlCmd += "    AND   ((? ='0') ";
        sqlCmd += "     or    (? ='1' ";
        sqlCmd += "      and  exists (select data_code ";
        sqlCmd += "                   FROM   tmp_bn_data     ";
        sqlCmd += "                   where  table_name   = ? ";
        sqlCmd += "                   and    data_type    = '3' ";
        sqlCmd += "                   and    data_code    = decode(a.group_code, '', '0000', a.group_code)))) ";
        /************
         * 執行對象-團代
         *****************************************************************/
        /************
         * 執行對象-帳戶
         *****************************************************************/
        sqlCmd += "    AND   ((?='0') ";
        sqlCmd += "     or    (?='1' ";
        sqlCmd += "      and  exists (select data_code ";
        sqlCmd += "                   FROM   tmp_bn_data ";
        sqlCmd += "                   where  table_name   = ? ";
        sqlCmd += "                   and    data_type    = '4' ";
        sqlCmd += "                   and    data_code    = a.acct_type))) ";
        /************
         * 執行對象-帳戶
         *****************************************************************/
        /************
         * 執行對象-卡種
         *****************************************************************/
        sqlCmd += "    AND   ((?='0') ";
        sqlCmd += "     or    (?='1' ";
        sqlCmd += "      and  exists (select data_code ";
        sqlCmd += "                   FROM   tmp_bn_data t,dbc_card s ";
        sqlCmd += "                   where  table_name   = ? ";
        sqlCmd += "                   and    data_type    = '5' ";
        sqlCmd += "                    and   s.card_no    = a.card_no ";
        sqlCmd += "                    and   t.data_code  = s.bin_type))) ";
        /************
         * 執行對象-卡種
         *****************************************************************/
        sqlCmd += "    AND  (? in ('OPAY') ";
        sqlCmd += "     or   (? not in ('OPAY') and acct_month = ?)) ";

        int index = 1;
        setString(index++, hFundFeedbackType);
        setString(index++, hFundCobrandCode);
        setString(index++, hFundCobrandCode);
        setString(index++, hBusiBusinessDate);
        setString(index++, hBusiBusinessDate);
        // setString(index++, h_fund_ex_group_flag);
        // setString(index++, h_fund_ex_group_flag);
        // setString(index++, h_fund_fund_code);
        setString(index++, hFundSourceCodeSel);
        setString(index++, hFundSourceCodeSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundSourceCodeSel);
        setString(index++, hFundFundCode);

        setString(index++, hFundMerchantSel);
        setString(index++, hFundMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundMerchantSel);
        setString(index++, hFundFundCode);

        setString(index++, hFundCurrencySel);
        setString(index++, hFundExCurrencySel);
        setString(index++, hFundExCurrencySel);
        setString(index++, hFundFundCode);
        setString(index++, hFundCurrencySel);
        setString(index++, hFundFundCode);

        setString(index++, hFundPosEntrySel);
        setString(index++, hFundPosEntrySel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosEntrySel);
        setString(index++, hFundFundCode);

        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundPosMerchantSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundBlCond);
        setString(index++, hFundCaCond);
        setString(index++, hFundIdCond);
        setString(index++, hFundAoCond);
        setString(index++, hFundOtCond);
        setString(index++, hFundItCond);

        setString(index++, hFundGroupCodeSel);
        setString(index++, hFundGroupCodeSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundAcctTypeSel);
        setString(index++, hFundAcctTypeSel);
        setString(index++, hFundFundCode);

        setString(index++, hFundCardTypeSel);
        setString(index++, hFundCardTypeSel);
        setString(index++, hFundFundCode);
        setString(index++, hFundCobrandCode);
        setString(index++, hFundCobrandCode);
        setString(index++, hWdayThisAcctMonth);

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hBillCardNo = getValue("card_no");
            hBillReferenceNo = getValue("reference_no");
            hBillPurchaseDate = getValue("purchase_date");
            hBillAcctKey = getValue("acct_key");
            hBillAcctMonth = getValue("acct_month");
            hBillDestAmt = getValueDouble("h_bill_dest_amt");

            /************
             * 排除指定(團代)流通卡
             *********************************************************/
            totalCnt++;
            if ((totalCnt % 5000) == 0)
                showLogMessage("I", "", String.format("Process records[%d]", totalCnt));

            if (hFundFundFeedFlag.equals("Y"))
                insertTmpBillDataVd(0);

            if (hFundPurchFeedFlag.equals("Y")) {
                /************
                 * 消費門檻
                 **********************************************************************/
                if ((hBillPurchaseDate.compareTo(hFundPurchDateS) < 0)
                        || (hBillPurchaseDate.compareTo(hFundPurchDateE) > 0))
                    continue;
                if (hFundPurchReclowCond.equals("Y")) {
                    if ((hBillDestAmt > 0) && (hBillDestAmt < hFundPurchReclowAmt))
                        continue;
                }
                insertTmpBillDataVd(1);
            }
        }
        closeCursor(cursorIndex); // End of for() loop
    }

    /*****************************************************************************/
    void insertTmpBillDataVd(int hInt) throws Exception {
        insertCnt++;
        sqlCmd = "INSERT INTO tmp_bill_data( ";
        sqlCmd += "       fund_type, ";
        sqlCmd += "       proc_date, ";
        sqlCmd += "       proc_type, ";
        sqlCmd += "       p_seqno, ";
        sqlCmd += "       reference_no, ";
        sqlCmd += "       fund_code, ";
        sqlCmd += "       acct_month, ";
        sqlCmd += "       dest_amt, ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       acct_key, ";
        sqlCmd += "       issue_date, ";
        sqlCmd += "       major_card_no, ";
        sqlCmd += "       card_no, ";
        sqlCmd += "       id_p_seqno, ";
        sqlCmd += "       group_code, ";
        sqlCmd += "       current_code, ";
        sqlCmd += "       activate_flag, ";
        sqlCmd += "       proc_mark ";
        sqlCmd += "       ) ";
        sqlCmd += "SELECT decode(?,0,'0','1'), ";
        sqlCmd += "       ?, ";
        sqlCmd += "       decode(cast(? as varchar(10)),'2','1','3'), ";
        sqlCmd += "       gp_no, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       ?, ";
        sqlCmd += "       issue_date, ";
        sqlCmd += "       major_card_no, ";
        sqlCmd += "       card_no, ";
        sqlCmd += "       id_p_seqno, ";
        sqlCmd += "       group_code, ";
        sqlCmd += "       current_code, ";
        sqlCmd += "       activate_flag, ";
        sqlCmd += "       'N' ";
        sqlCmd += "FROM   dbc_card a ";
        sqlCmd += "WHERE  card_no  = ? ";

        int index = 1;
        setInt(index++, hInt);
        setString(index++, hTbdaProcDate);
        setString(index++, hFundFeedbackType);
        setString(index++, hBillReferenceNo);
        setString(index++, hBillAcctMonth);
        setDouble(index++, hBillDestAmt);
        setString(index++, hBillAcctKey);
        setString(index++, hBillCardNo);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate", "", hCallBatchSeqno);
        }

    }

    /*****************************************************************************/
    void selectTmpBillData1() throws Exception {
        int hTempCnt1 = 0;
        int hTempCnt2 = 0;
        int hTempCnt3 = 0;
        int hTempCnt4 = 0;
        hTbdaMajorCardNo = "";
        sqlCmd = " SELECT  major_card_no, ";
        sqlCmd += "        sum(decode(card_no,major_card_no,1,0)) as h_temp_cnt1,  ";
        sqlCmd += "        sum(decode(card_no,major_card_no, ";
        sqlCmd += "            decode(activate_flag,'2',1,0),0)) as h_temp_cnt2, ";
        sqlCmd += "        sum(decode(card_no,major_card_no,0,1)) as h_temp_cnt3, ";
        sqlCmd += "        sum(decode(card_no,major_card_no,0,decode(activate_flag,'2',1,0))) as h_temp_cnt4 ";
        sqlCmd += "FROM    tmp_bill_data b  ";
        sqlCmd += "WHERE   current_code = '0' ";
        sqlCmd += "GROUP   BY major_card_no  ";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTbdaMajorCardNo = getValue("major_card_no", i);
            hTempCnt1 = getValueInt("h_temp_cnt1", i);
            hTempCnt2 = getValueInt("h_temp_cnt2", i);
            hTempCnt3 = getValueInt("h_temp_cnt3", i);
            hTempCnt4 = getValueInt("h_temp_cnt4", i);

            totalCnt++;
            if ((totalCnt % 5000) == 0)
                showLogMessage("I", "", String.format("Process records[%d]\n", totalCnt));

            switch (hFundActivateFlag) {
            case "1":
                if (hTempCnt1 + hTempCnt3 == 0)
                    continue;
                if (hTempCnt1 != hTempCnt2)
                    continue;
                if (hTempCnt3 != hTempCnt4)
                    continue;
                break;
            case "2":
                if (hTempCnt1 == 0)
                    continue;
                if (hTempCnt1 != hTempCnt2)
                    continue;
                break;
            case "3":
                if (hTempCnt2 + hTempCnt4 == 0)
                    continue;
                break;
            default:
                continue;
            }
            insertCnt++;
            updateTmpBillData1();
        }

    }

    /*****************************************************************************/
    void updateTmpBillData1() throws Exception {
        daoTable = "tmp_bill_data";
        updateSQL = "proc_mark    = '1' ";
        whereStr = " where  major_card_no = ? ";
        setString(1, hTbdaMajorCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tmp_bill_data_1 error!", "", hCallBatchSeqno);
        }

    }

    /*****************************************************************************/
    void selectTmpBillData2(int hInt) throws Exception {
        hTbdaIdPSeqno = "";
        hTbdaCardNo = "";
        hTbdaMajorCardNo = "";
        hTbdaIssueDate = "";
        sqlCmd = "SELECT  b.id_p_seqno, ";
        sqlCmd += "        a.card_no, ";
        sqlCmd += "        min(a.major_card_no) as h_tbda_major_card_no, ";
        sqlCmd += "        min(b.ori_issue_date) as h_tbda_issue_date ";
        sqlCmd += "from   tmp_bill_data a,crd_card b ";
        sqlCmd += "where  proc_mark = decode(cast(? as varchar(8)),'0','N','1') ";
        sqlCmd += "and    b.card_no = a.major_card_no  ";
        sqlCmd += "group  by b.id_p_seqno,a.card_no ";
        setString(1, hTbdaProcMark);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {

            hTbdaIdPSeqno = getValue("id_p_seqno", i);
            hTbdaCardNo = getValue("card_no", i);
            hTbdaMajorCardNo = getValue("h_tbda_major_card_no", i);
            hTbdaIssueDate = getValue("h_tbda_issue_date", i);

            totalCnt++;

            if ((totalCnt % 5000) == 0)
                showLogMessage("I", "", String.format("Process records[%d]", totalCnt));

            if (selectCrdCard(hInt) != 0)
                continue;
            insertCnt++;
            updateTmpBillData2();
        }

    }

    /*****************************************************************************/
    void updateTmpBillData2() throws Exception {
        daoTable = "tmp_bill_data";
        updateSQL = "proc_mark    = '2' ";
        whereStr = " where  card_no = ? ";
        setString(1, hTbdaCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tmp_bill_data_2 error!", "", hCallBatchSeqno);
        }
    }

    /*****************************************************************************/
    void selectTmpBillDataA() throws Exception {
        hTbdaRowid = "";
        sqlCmd = "SELECT  a.rowid as rowid ";
        sqlCmd += "from    tmp_bill_data a,crd_card b,crd_idno c ";
        sqlCmd += "where   a.major_card_no = b.major_card_no ";
        sqlCmd += "and     b.major_id_p_seqno = c.id_p_seqno ";
        sqlCmd += "and     b.sup_flag <> '1' ";
        sqlCmd += "and     floor(months_between(to_date(b.ori_issue_date,'yyyymmdd'), ";
        sqlCmd += "                             to_date(c.birthday,'yyyymmdd'))/12) ";
        sqlCmd += "      not between ? and ? ";
        setInt(1, hFundApplyAgeS);
        setInt(2, hFundApplyAgeE);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hTbdaRowid = getValue("rowid", i);

            totalCnt++;

            if ((totalCnt % 5000) == 0)
                showLogMessage("I", "", String.format("Process records[%d]", totalCnt));

            insertCnt++;
            updateTmpBillDataA();
        }

    }

    /*****************************************************************************/
    void updateTmpBillDataA() throws Exception {
        daoTable = "tmp_bill_data";
        updateSQL = "proc_mark    = 'A' ";
        whereStr = " where  rowid = ? ";
        setString(1, hTbdaRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tmp_bill_data_a error!", "", hCallBatchSeqno);
        }
    }

    /*****************************************************************************/
    int selectTmpBnData() throws Exception {
        sqlCmd = "SELECT 1 ";
        sqlCmd += "FROM   tmp_bn_data ";
        sqlCmd += "WHERE  table_name   = ? ";
        sqlCmd += "and    data_type    = '0' ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hFundFundCode);
        selectTable();
        if (notFound.equals("Y"))
            return 1;

        return 0;
    }

    /*****************************************************************************/
    void deleteTmpBnData() throws Exception {
        daoTable = "tmp_bn_data";
        deleteTable();
    }

    /**************************************************************************************************/
    void insertTmpBnData() throws Exception {
        sqlCmd = "INSERT INTO tmp_bn_data( ";
        sqlCmd += "       table_name, ";
        sqlCmd += "       data_key, ";
        sqlCmd += "       data_type, ";
        sqlCmd += "       data_code, ";
        sqlCmd += "       data_code2, ";
        sqlCmd += "       data_code3 ";
        sqlCmd += "       ) ";
        sqlCmd += "select data_key, ";
        sqlCmd += "       '1', ";
        sqlCmd += "       data_type, ";
        
        /* Mantis #7913
           move data_code2 to data_code, data_code3 to data_code2, and data_code to data_code3 if data_type is B 
           move exchange data_code and data_code2 if data_type is C 
        */
//        sqlCmd += "       decode(data_code , '', 'XXXX', data_code), ";
//        sqlCmd += "       decode(data_code2, '', 'XXXX', data_code2), ";
//        sqlCmd += "       decode(data_code3, '', 'XXXX', data_code3) ";
        sqlCmd += " decode(data_type, '', 'XXXX', 'B', data_code2, 'C', data_code2, data_code), ";
        sqlCmd += " decode(data_type, '', 'XXXX', 'B', data_code3, 'C', data_code, data_code2), ";
        sqlCmd += " decode(data_type, '', 'XXXX', 'B', data_code, 'C', data_code3, data_code3) ";

        
        sqlCmd += "from   ptr_fund_data ";
        sqlCmd += "where  data_key = ? ";
        setString(1, hFundFundCode);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void deleteTmpBillData() throws Exception {
        daoTable = "tmp_bill_data";
        deleteTable();
    }

    /*****************************************************************************/
    void updateTmpBillData() throws Exception {
        daoTable = "tmp_bill_data a ";
        updateSQL = " major_card_no = (select major_card_no ";
        updateSQL += "                    from   crd_card ";
        updateSQL += "                     where  card_no = a.card_no) ";
        whereStr = "WHERE  major_card_no !=(select major_card_no ";
        whereStr += "                     from   crd_card ";
        whereStr += "                     where  card_no = a.card_no) ";

        updateTable();
    }

    /*****************************************************************************/
    int selectCrdCard(int hInt) throws Exception {
        sqlCmd = "SELECT 1 ";
        sqlCmd += "FROM   crd_card ";
        sqlCmd += "WHERE  id_p_seqno     = ? ";
        sqlCmd += "AND    card_no       != ? ";
        sqlCmd += "AND    ori_issue_date < ? ";
        sqlCmd += "AND    sup_flag  in (decode(cast(? as varchar(8)),'Y','0','X'), ";
        sqlCmd += "                        decode(cast(? as varchar(8)),'Y','1','X')) ";
        sqlCmd += "AND    ((? = 1)  AND acct_type in  (select acct_type from ptr_acct_type where card_indicator = '1') ";
        sqlCmd += " or     (? = 0 ";
        sqlCmd += "  and    exists (select data_code ";
        sqlCmd += "                 FROM tmp_bn_data ";
        sqlCmd += "                 where table_name = ? ";
        sqlCmd += "                 and data_type  = '0' ";
        sqlCmd += "                 and data_code  = decode(group_code, '', 'XXXX', group_code)))) ";
        sqlCmd += "AND    to_date(?,'yyyymmdd') - ";
        sqlCmd += "       to_date(decode(oppost_date, '', '30001231', oppost_date),'yyyymmdd') <= ? ";
        sqlCmd += "fetch first 1 rows only ";
        int index = 1;
        setString(index++, hTbdaIdPSeqno);
        setString(index++, hTbdaMajorCardNo);
        setString(index++, hTbdaIssueDate);
        setString(index++, hFundNewHldrCard);
        setString(index++, hFundNewHldrSup);
        setInt(index++, hInt);
        setInt(index++, hInt);
        setString(index++, hFundFundCode);
        setString(index++, hTbdaIssueDate);
        setLong(index++, hFundNewHldrDays);

        if (DEBUG) {
            showLogMessage("I", "", "*******************************************");
            showLogMessage("I", "", String.format("id_p_seqno[%s]", hTbdaIdPSeqno));
            showLogMessage("I", "", String.format("   card_no[%s]", hTbdaMajorCardNo));
            showLogMessage("I", "", String.format("issue_date[%s]", hTbdaIssueDate));
            showLogMessage("I", "", "*******************************************");
        }
        selectTable();
        if (notFound.equals("Y"))
            return 0;
        return (1);
    }

    /*****************************************************************************/
    void checkOpen() throws Exception {
        String temstr1 = String.format("%s/media/%s/%s_bill_%s.txt", comc.getECSHOME(), hFundCobrandCode,
                hFundCobrandCode, hBusiBusinessDate);
        filename = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", "Write File : " + filename);
        comc.mkdirsFromFilenameWithPath(filename);
        fptr1 = openOutputText(filename, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", filename), "", comcr.hCallBatchSeqno);
        }

    }

    /***************************************************************************/
    void selectTmpBillData() throws Exception {
        String outData = "";
        long totalCount = 0;
        String hCardMajorId = "";
        String hCardId = "";
        String hTempCardCode = "";
        String hBillAcctDate = "";
        String hTbdaFundCode = "";
        String hBillAuthCode = "";
        String hTbdaAcctMonth = "";
        String hCardStmtCycle = "";
        String hBillMchtChiName = "";

        sqlCmd = "SELECT   (select id_no from crd_idno where id_p_seqno = c.major_id_p_seqno) as major_id, ";
        sqlCmd += "        (select id_no from crd_idno where id_p_seqno = c.id_p_seqno) as id, ";
        sqlCmd += "        'C' as h_temp_card_code,  ";
        sqlCmd += "        b.card_no, ";
        sqlCmd += "        b.acct_date, ";
        sqlCmd += "        b.purchase_date, ";
        sqlCmd += "        a.dest_amt,  ";
        sqlCmd += "        a.fund_code as h_tbda_fund_code, ";
        sqlCmd += "        b.auth_code, ";
        sqlCmd += "        a.acct_month,  ";
        sqlCmd += "        c.stmt_cycle, ";
        sqlCmd += "        a.reference_no, ";
        sqlCmd += "        decode(b.bill_type, 'OICU', b.mcht_chi_name, decode(decode(b.source_curr,'TWD','901',b.source_curr), ";
        sqlCmd += "               '901',decode(substr(decode(b.mcht_country, '', 'TW', b.mcht_country),1,2), ";
        sqlCmd += "                     'TW',decode(trim(b.mcht_chi_name), '',b.mcht_eng_name, ";
        sqlCmd += "                          decode(substrb(b.mcht_chi_name,1,4),'　　',b.mcht_eng_name, ";
        sqlCmd += "                          b.mcht_chi_name)), ";
        sqlCmd += "                     decode(trim(b.mcht_eng_name), '',b.mcht_chi_name,b.mcht_eng_name)), ";
        sqlCmd += "               decode(trim(b.mcht_eng_name), '',b.mcht_chi_name,b.mcht_eng_name))) as h_bill_mcht_chi_name ";
        sqlCmd += "FROM    tmp_bill_data a,bil_bill b,crd_card c   ";
        sqlCmd += "where   a.reference_no = b.reference_no ";
        sqlCmd += "and     b.card_no = c.card_no ";
        sqlCmd += "and     a.acct_type in (select acct_type from ptr_acct_type) ";
        sqlCmd += "and     a.proc_mark = decode(cast(? as varchar(8)),'0','N',cast(? as varchar(8))) ";
        sqlCmd += "union ";
        sqlCmd += "SELECT  (select id_no from dbc_idno where id_p_seqno = c.major_id_p_seqno), ";
        sqlCmd += "        (select id_no from dbc_idno where id_p_seqno = c.id_p_seqno), ";
        sqlCmd += "        'D',     ";
        sqlCmd += "        b.card_no, ";
        sqlCmd += "        b.acct_date, ";
        sqlCmd += "        b.purchase_date, ";
        sqlCmd += "        a.dest_amt, ";
        sqlCmd += "        a.fund_code, ";
        sqlCmd += "        b.auth_code, ";
        sqlCmd += "        a.acct_month, ";
        sqlCmd += "        c.stmt_cycle, ";
        sqlCmd += "        a.reference_no, ";
        sqlCmd += "        decode(b.bill_type, 'OICU', b.mcht_chi_name, decode(decode(b.source_curr,'TWD','901',b.source_curr), ";
        sqlCmd += "               '901',decode(substr(decode(b.mcht_country, '', 'TW', b.mcht_country),1,2), ";
        sqlCmd += "                     'TW',decode(trim(b.mcht_chi_name), '',b.mcht_eng_name, ";
        sqlCmd += "                          decode(substrb(b.mcht_chi_name,1,4),'　　',b.mcht_eng_name, ";
        sqlCmd += "                          b.mcht_chi_name)), ";
        sqlCmd += "                     decode(trim(b.mcht_eng_name), '',b.mcht_chi_name,b.mcht_eng_name)), ";
        sqlCmd += "               decode(trim(b.mcht_eng_name), '',b.mcht_chi_name,b.mcht_eng_name))) ";
        sqlCmd += "FROM    tmp_bill_data a,dbb_bill b,dbc_card c   ";
        sqlCmd += "where   a.reference_no = b.reference_no ";
        sqlCmd += "and     b.card_no = c.card_no ";
        sqlCmd += "and     a.acct_type in (select acct_type from dbp_acct_type) ";
        sqlCmd += "and     a.proc_mark = decode(cast(? as varchar(8)),'0','N',cast(? as varchar(8))) ";
        int index = 1;
        setString(index++, hTbdaProcMark);
        setString(index++, hTbdaProcMark);
        setString(index++, hTbdaProcMark);
        setString(index++, hTbdaProcMark);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {

            hCardMajorId = getValue("major_id", i);
            hCardId = getValue("id", i);
            hTempCardCode = getValue("h_temp_card_code", i);
            hBillCardNo = getValue("card_no", i);
            hBillAcctDate = getValue("acct_date", i);
            hBillPurchaseDate = getValue("purchase_date", i);
            hBillDestAmt = getValueDouble("dest_amt", i);
            hTbdaFundCode = getValue("h_tbda_fund_code", i);
            hBillAuthCode = getValue("auth_code", i);
            hTbdaAcctMonth = getValue("acct_month", i);
            hCardStmtCycle = getValue("stmt_cycle", i);
            hBillReferenceNo = getValue("reference_no", i);
            hBillMchtChiName = getValue("h_bill_mcht_chi_name", i);

            totalAll++;

            outData = String.format("%10.10s%10.10s%1.1s%4.4s%8.8s%8.8s%11.0f%4.4s%6.6s%6.6s%6.6s%2.2s%10.10s",
                    hCardMajorId, hCardId, hTempCardCode, comc.getSubString(hBillCardNo, 12), hBillAcctDate,
                    hBillPurchaseDate, hBillDestAmt, hTbdaFundCode, hBillAuthCode, hTbdaAcctMonth,
                    hTbdaAcctMonth, hCardStmtCycle, hBillReferenceNo);
            outData += fixLeft(hBillMchtChiName, 20);
            writeTextFile(fptr1, outData + "\n");
            totalCount++;
            if (totalCount % 25000 == 0)
                showLogMessage("I", "", String.format("    處理筆數 [%d]", totalCount));
        }
    }
    String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)
            spc += " ";
        if (str == null)
            str = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }
    /*************************************************************************/
    void ftpProc() throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */

        /**********
         * COMM_FTP common function usage
         ****************************************/
        commFTP.hEflgSystemId = String.format("%s_SFTP", hFundCobrandCode); /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "bill"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = hFundCobrandCode; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/%s", comc.getECSHOME(), hFundCobrandCode);
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = commFTP.hEflgSystemId;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s_bill_%s.zip", hFundCobrandCode, hBusiBusinessDate);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s]檔案傳送%s有誤(error), 請通知相關人員處理\n", procCode, hEflgRefIpCode));
        }
        /*** SENDMSG ***/
        String cmdStr = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \" %s 執行完成 傳送%s %s [%s]\"", javaProgram,
                hEflgRefIpCode, errCode == 0 ? "無誤" : "失敗", procCode);
        showLogMessage("I", "",  cmdStr);
//        boolean ret_code = comc.systemCmd(cmd_str);
//        showLogMessage("I", "", String.format("%s [%d]", cmd_str, ret_code == true ? 0 : 1));
        /**********
         * COMM_FTP common function usage
         ****************************************/
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CycA181 proc = new CycA181();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
