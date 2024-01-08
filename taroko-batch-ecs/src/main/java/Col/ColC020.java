/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/08  V1.00.00    phopho     program initial                          *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*  110/08/03  V1.00.01    chilai     fix SQL 
*  111/12/30  V1.00.02    Ryan       增加col_cs_info處理邏輯                                            * 
*  112/08/14  V1.00.03    Sunny      Cancel deubg                              * 
******************************************************************************/

package Col;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColC020 extends AccessDAO {
	public final boolean debug = false;
    private String progname = "傳送CS(M0)C-篩選符合M0條件資料處理程式   112/08/14  V1.00.03 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommCol comCol = null;
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempBusinessDate = "";
    String hCmpmStmtCycle = "";
    String hCmpmOtherDeductFlag = "";
    String hCmpmSelfDeductFlag = "";
    String hCmpmSelfPayFlag = "";
    String hCmpmAfFlag = "";
    String hCmpmLfFlag = "";
    String hCmpmCfFlag = "";
    String hCmpmPfFlag = "";
    String hCmpmRiFlag = "";
    String hCmpmPnFlag = "";
    String hCmpmExceedPayDays = "";
    double hCmpmMinMpAmt = 0;
    int hCmpmNormalMonths = 0;
    String hWdayThisCloseDate = "";
    String hWdayThisLastpayDate = "";
    String hAcnoIdPSeqno = "";
    String hAcnoAcctHolderId = "";
    String hAcnoCardIndicator = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoPSeqno = "";
    String hAcnoCorpPSeqno = "";
    String hAcnoAcctPSeqno = "";
    String hAcnoStmtCycle = "";
    String hAcnoCorpNo = "";
    String hAcnoRecourseMark = "";
    String hAcnoAcctStatus = "";
    String hAcnoAutopayAcctBank = "";
    String hAcnoPaymentRate1 = "";
    String hAcnoPaymentRate2 = "";
    String hAcnoPaymentRate3 = "";
    String hAcnoPaymentRate4 = "";
    String hAcnoPaymentRate5 = "";
    String hAcnoPaymentRate6 = "";
    String hAcnoPaymentRate7 = "";
    String hAcnoPaymentRate8 = "";
    String hAcnoPaymentRate9 = "";
    String hAcnoPaymentRate10 = "";
    String hAcnoPaymentRate11 = "";
    String hAcnoPaymentRate12 = "";
    String hCloseFlag = "";
    String hDebtCurrCode = "";
    String hDebtCardNo = "";
    String hRegBankNo = "";
    String hIssueBankNo = "";
    long hAcsuBilledEndBal = 0;
    String[] hAcnoPaymentRate20 = new String[12];
    int[] reasonCnt = new int[5];
    int insertCnt = 0;
    int deleteCnt = 0;
    int totalCnt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ColC020 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comCol = new CommCol(getDBconnect(), getDBalias());

            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            showLogMessage("I", "", String.format("本日[%s] (執行日期) 換日前[%s](產檔資料日期)", hBusiBusinessDate, hTempBusinessDate));

            selectPtrWorkday();

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
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
        hTempBusinessDate = "";
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) , 'yyyymmdd') - 1 days,'yyyymmdd') temp_business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempBusinessDate = getValue("temp_business_date");
        }
    }

    /***********************************************************************/
    void selectPtrWorkday() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "b.stmt_cycle,";
        sqlCmd += "b.other_deduct_flag,";
        sqlCmd += "b.self_deduct_flag,";
        sqlCmd += "b.self_pay_flag,";
        sqlCmd += "b.af_flag,";
        sqlCmd += "b.lf_flag,";
        sqlCmd += "b.cf_flag,";
        sqlCmd += "b.pf_flag,";
        sqlCmd += "b.ri_flag,";
        sqlCmd += "b.pn_flag,";
        sqlCmd += "b.exceed_pay_days,";
        sqlCmd += "b.min_mp_amt,";
        sqlCmd += "b.normal_months,";
        sqlCmd += "a.this_close_date,";
        sqlCmd += "to_char(to_date(a.this_lastpay_date,'yyyymmdd')+b.exceed_pay_days,'yyyymmdd') h_wday_this_lastpay_date ";
        sqlCmd += "from ptr_workday a,col_m0_parm b ";
        sqlCmd += "where a.stmt_cycle = b.stmt_cycle ";
        sqlCmd += " order by b.stmt_cycle ";

        openCursor();
        while (fetchTable()) {
            hCmpmStmtCycle = getValue("stmt_cycle");
            hCmpmOtherDeductFlag = getValue("other_deduct_flag");
            hCmpmSelfDeductFlag = getValue("self_deduct_flag");
            hCmpmSelfPayFlag = getValue("self_pay_flag");
            hCmpmAfFlag = getValue("af_flag");
            hCmpmLfFlag = getValue("lf_flag");
            hCmpmCfFlag = getValue("cf_flag");
            hCmpmPfFlag = getValue("pf_flag");
            hCmpmRiFlag = getValue("ri_flag");
            hCmpmPnFlag = getValue("pn_flag");
            hCmpmExceedPayDays = getValue("exceed_pay_days");
            hCmpmMinMpAmt = getValueDouble("min_mp_amt");
            hCmpmNormalMonths = getValueInt("normal_months");
            hWdayThisCloseDate = getValue("this_close_date");
            hWdayThisLastpayDate = getValue("h_wday_this_lastpay_date");

            /* 作業日參數之本次關帳日=營業日減一,則刪除此cycle的base檔 */
            showLogMessage("I", "", String.format("提醒說明：stmt_cycle[%s] 刪檔日期[%s] 產檔日期[%s] (營業日-1)", hCmpmStmtCycle,
                    hWdayThisCloseDate, hWdayThisLastpayDate));

            if (hWdayThisCloseDate.equals(hTempBusinessDate)) {           	
            	showLogMessage("I", "", String.format("本次關帳日     [%s] ..", hWdayThisCloseDate));
            	showLogMessage("I", "", String.format("本次營業日-1 [%s] ..", hTempBusinessDate));
                showLogMessage("I", "", String.format("刪除 stmt_cycle [%s] ..", hCmpmStmtCycle));
                deleteColM0Base();
            }
            /* 繳款日+ col_m0_parm:exceed_pay_days = 營業日減一, 則產生此cycle的base檔 */
            if (!hWdayThisLastpayDate.equals(hTempBusinessDate))
                continue;

            insertCnt = 0;
            showLogMessage("I", "", String.format("處理 stmt_cycle [%s] 最低應繳餘額[%.0f]", hCmpmStmtCycle, hCmpmMinMpAmt));
            totalCnt = 0;
            deleteColM0Base();
            selectActAcno();
            showLogMessage("I", "", String.format("累計處理筆數[%d] 新增筆數[%d]", totalCnt, insertCnt));
            showLogMessage("I", "", String.format("   排除項目:"));
            showLogMessage("I", "", String.format("    1.他行扣款     筆數[%d][%s]", reasonCnt[0], hCmpmOtherDeductFlag));
            showLogMessage("I", "", String.format("    2.本行扣款     筆數[%d][%s]", reasonCnt[1], hCmpmSelfDeductFlag));
            showLogMessage("I", "", String.format("    3.自行扣款     筆數[%d][%s]", reasonCnt[2], hCmpmSelfPayFlag));
            showLogMessage("I", "", String.format("    4.正常繳款月數 筆數[%d][%d]", reasonCnt[3], hCmpmNormalMonths));
            showLogMessage("I", "", String.format("    5.無欠本金類   筆數[%d]", reasonCnt[4]));
        }
        closeCursor();
    }

    /***********************************************************************/
    void deleteColM0Base() throws Exception {
        daoTable = "col_m0_base";
        whereStr = "where stmt_cycle = ? ";
        setString(1, hCmpmStmtCycle);
        deleteCnt = deleteTable();
        showLogMessage("I", "", String.format("    累計刪除筆數[%d]", deleteCnt));
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        int intia = 0;

        for (int inti = 0; inti < reasonCnt.length; inti++)
            reasonCnt[inti] = 0;

        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "UF_IDNO_ID(a.id_p_seqno) acct_holder_id,";
        sqlCmd += "a.card_indicator,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acct_key,";
//        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.corp_p_seqno,";
//        sqlCmd += "a.gp_no as acct_p_seqno,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "c.corp_no,";
        sqlCmd += "a.recourse_mark,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "a.autopay_acct_bank,";
        sqlCmd += "a.payment_rate1,";
        sqlCmd += "decode(a.payment_rate2,'','0A',a.payment_rate2) h_acno_payment_rate2,";
        sqlCmd += "decode(a.payment_rate3,'','0A',a.payment_rate3) h_acno_payment_rate3,";
        sqlCmd += "decode(a.payment_rate4,'','0A',a.payment_rate4) h_acno_payment_rate4,";
        sqlCmd += "decode(a.payment_rate5,'','0A',a.payment_rate5) h_acno_payment_rate5,";
        sqlCmd += "decode(a.payment_rate6,'','0A',a.payment_rate6) h_acno_payment_rate6,";
        sqlCmd += "decode(a.payment_rate7,'','0A',a.payment_rate7) h_acno_payment_rate7,";
        sqlCmd += "decode(a.payment_rate8,'','0A',a.payment_rate8) h_acno_payment_rate8,";
        sqlCmd += "decode(a.payment_rate9,'','0A',a.payment_rate9) h_acno_payment_rate9,";
        sqlCmd += "decode(a.payment_rate10,'','0A',a.payment_rate10) h_acno_payment_rate10,";
        sqlCmd += "decode(a.payment_rate11,'','0A',a.payment_rate11) h_acno_payment_rate11,";
        sqlCmd += "decode(a.payment_rate12,'','0A',a.payment_rate12) h_acno_payment_rate12 ";
        sqlCmd += "from act_acno a,act_acct b, crd_corp c ";
        sqlCmd += "where a.acno_flag <> 'Y' ";
        sqlCmd += "and a.id_p_seqno = '' ";
        sqlCmd += "and a.corp_p_seqno = c.corp_p_seqno ";
//        sqlCmd += "and a.p_seqno = b.p_seqno ";
        sqlCmd += "and a.acno_p_seqno = b.p_seqno ";
        sqlCmd += "and a.stmt_cycle = ? ";
        sqlCmd += "and b.min_pay_bal > ? ";
//        sqlCmd += "and a.pay_by_stage_flag = '' ";   //20221221 sunny mark 有協商也要下檔
        sqlCmd += "and a.acct_status not in ('3','4') ";
        sqlCmd += "and a.payment_rate1 between '0A' and '0E' ";
        sqlCmd += "UNION ";
        sqlCmd += "select a.id_p_seqno, ";
        sqlCmd += "UF_IDNO_ID(a.id_p_seqno) acct_holder_id, ";
        sqlCmd += "a.card_indicator, ";
        sqlCmd += "a.acct_type, ";
        sqlCmd += "a.acct_key, ";
//        sqlCmd += "a.p_seqno, ";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.corp_p_seqno, ";
//        sqlCmd += "a.gp_no as acct_p_seqno, ";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.stmt_cycle, ";
        sqlCmd += "d.corp_no, ";
        sqlCmd += "a.recourse_mark, ";
        sqlCmd += "a.acct_status, ";
        sqlCmd += "a.autopay_acct_bank, ";
        sqlCmd += "a.payment_rate1, ";
        sqlCmd += "decode(a.payment_rate2,'','0A',a.payment_rate2), ";
        sqlCmd += "decode(a.payment_rate3,'','0A',a.payment_rate3), ";
        sqlCmd += "decode(a.payment_rate4,'','0A',a.payment_rate4), ";
        sqlCmd += "decode(a.payment_rate5,'','0A',a.payment_rate5), ";
        sqlCmd += "decode(a.payment_rate6,'','0A',a.payment_rate6), ";
        sqlCmd += "decode(a.payment_rate7,'','0A',a.payment_rate7), ";
        sqlCmd += "decode(a.payment_rate8,'','0A',a.payment_rate8), ";
        sqlCmd += "decode(a.payment_rate9,'','0A',a.payment_rate9), ";
        sqlCmd += "decode(a.payment_rate10,'','0A',a.payment_rate10), ";
        sqlCmd += "decode(a.payment_rate11,'','0A',a.payment_rate11), ";
        sqlCmd += "decode(a.payment_rate12,'','0A',a.payment_rate12) ";
        sqlCmd += "from act_acno a,act_acct b,crd_idno c  left join crd_corp d on d.corp_p_seqno = a.corp_p_seqno ";
        sqlCmd += "where a.acno_flag <> 'Y'  ";
        sqlCmd += "and a.id_p_seqno = c.id_p_seqno ";
//        sqlCmd += "and a.p_seqno = b.p_seqno ";
        sqlCmd += "and a.acno_p_seqno = b.p_seqno ";
        sqlCmd += "and a.stmt_cycle = ? ";
        sqlCmd += "and b.min_pay_bal > ? ";
//        sqlCmd += "and a.pay_by_stage_flag = '' ";       //20221221 sunny mark 有協商也要下檔
        sqlCmd += "and a.acct_status not in ('3','4') ";
//        sqlCmd += "and decode(c.salary_holdin_flag,'','N',c.salary_holdin_flag)!='Y' ";
        sqlCmd += "and a.payment_rate1 between '0A' and '0E' ";
        setString(1, hCmpmStmtCycle);
        setDouble(2, hCmpmMinMpAmt);
        setString(3, hCmpmStmtCycle);
        setDouble(4, hCmpmMinMpAmt);

        extendField = "act_acno.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hAcnoIdPSeqno = getValue("act_acno.id_p_seqno",i);
            hAcnoAcctHolderId = getValue("act_acno.acct_holder_id",i);
            hAcnoCardIndicator = getValue("act_acno.card_indicator",i);
            hAcnoAcctType = getValue("act_acno.acct_type",i);
            hAcnoAcctKey = getValue("act_acno.acct_key",i);
//            h_acno_p_seqno = getValue("p_seqno");
            hAcnoPSeqno = getValue("act_acno.acno_p_seqno",i);
            hAcnoCorpPSeqno = getValue("act_acno.corp_p_seqno",i);
//            h_acno_acct_p_seqno = getValue("acct_p_seqno");
            hAcnoAcctPSeqno = getValue("act_acno.p_seqno",i);
            hAcnoStmtCycle = getValue("act_acno.stmt_cycle",i);
            hAcnoCorpNo = getValue("act_acno.corp_no",i);
            hAcnoRecourseMark = getValue("act_acno.recourse_mark",i);
            hAcnoAcctStatus = getValue("act_acno.acct_status",i);
            hAcnoAutopayAcctBank = getValue("act_acno.autopay_acct_bank",i);
            hAcnoPaymentRate1 = getValue("act_acno.payment_rate1",i);
            hAcnoPaymentRate2 = getValue("act_acno.h_acno_payment_rate2",i);
            hAcnoPaymentRate3 = getValue("act_acno.h_acno_payment_rate3",i);
            hAcnoPaymentRate4 = getValue("act_acno.h_acno_payment_rate4",i);
            hAcnoPaymentRate5 = getValue("act_acno.h_acno_payment_rate5",i);
            hAcnoPaymentRate6 = getValue("act_acno.h_acno_payment_rate6",i);
            hAcnoPaymentRate7 = getValue("act_acno.h_acno_payment_rate7",i);
            hAcnoPaymentRate8 = getValue("act_acno.h_acno_payment_rate8",i);
            hAcnoPaymentRate9 = getValue("act_acno.h_acno_payment_rate9",i);
            hAcnoPaymentRate10 = getValue("act_acno.h_acno_payment_rate10",i);
            hAcnoPaymentRate11 = getValue("act_acno.h_acno_payment_rate11",i);
            hAcnoPaymentRate12 = getValue("act_acno.h_acno_payment_rate12",i);

            hAcnoPaymentRate20[1] = hAcnoPaymentRate2;
            hAcnoPaymentRate20[2] = hAcnoPaymentRate3;
            hAcnoPaymentRate20[3] = hAcnoPaymentRate4;
            hAcnoPaymentRate20[4] = hAcnoPaymentRate5;
            hAcnoPaymentRate20[5] = hAcnoPaymentRate6;
            hAcnoPaymentRate20[6] = hAcnoPaymentRate7;
            hAcnoPaymentRate20[7] = hAcnoPaymentRate8;
            hAcnoPaymentRate20[8] = hAcnoPaymentRate9;
            hAcnoPaymentRate20[9] = hAcnoPaymentRate10;
            hAcnoPaymentRate20[10] = hAcnoPaymentRate11;
            hAcnoPaymentRate20[11] = hAcnoPaymentRate12;
            
            
            //debug
            if(debug)
            {
            showLogMessage("I", "", String.format("  [%s],hAcnoPaymentRate1[%s]",hAcnoAcctKey ,hAcnoPaymentRate1));
            showLogMessage("I", "", String.format("  [%s],hAcnoPaymentRate2[%s]",hAcnoAcctKey ,hAcnoPaymentRate20[1]));
            showLogMessage("I", "", String.format("  [%s],hAcnoPaymentRate3[%s]",hAcnoAcctKey,hAcnoPaymentRate20[2]));
            }
            
            totalCnt++;

            if ((hCmpmOtherDeductFlag.substring(0, 1).equals("Y")) && (hAcnoAutopayAcctBank.length() != 0)
                    && (!hAcnoAutopayAcctBank.equals("006"))) {
                reasonCnt[0]++;
                continue;
            }

            if ((hCmpmSelfDeductFlag.substring(0, 1).equals("Y")) && (hAcnoAutopayAcctBank.equals("006"))) {
                reasonCnt[1]++;
                continue;
            }

            if ((hCmpmSelfPayFlag.substring(0, 1).equals("Y")) && (hAcnoAutopayAcctBank.length() == 0)) {
                reasonCnt[2]++;
                continue;
            }

            if (hCmpmNormalMonths != 0) {
                for (intia = 1; intia < hCmpmNormalMonths; intia++) {
                    if ((hAcnoPaymentRate20[intia].equals("0A")) || (hAcnoPaymentRate20[intia].equals("0B"))
                            || (hAcnoPaymentRate20[intia].equals("0C"))
                            || (hAcnoPaymentRate20[intia].equals("0E"))) {
                        continue;
                    }
                    break;
                }
                if (intia >= hCmpmNormalMonths) {
                    reasonCnt[3]++;
                    continue;
                }
            }

            if (selectActAcctSum() == 0) {
                reasonCnt[4]++;
                continue;
            }
            
             
            String[] debtArray = comCol.selectMaxCardDebtAmt(hAcnoPSeqno);
            hDebtCurrCode = debtArray[0];
            hDebtCardNo = debtArray[1];
            
            hRegBankNo = comCol.selectCardRiskBankNo(hDebtCardNo);
            hIssueBankNo = comCol.selectCardIssueRegBankNo(hDebtCardNo);
            
            if(selectColCsInfo() == 0) {
            	if(!hCloseFlag.equals("Y")) //理論上是非M0的案件 即維持現狀，跳過不處理，繼續處理下一筆
            		continue;
            	updateColCsInfo();
            }else {
            	insertColCsInfo();
            }
            updateAcnoRegBankNo();
            insertColM0Base();
        }
        
    }

    /***********************************************************************/
    int selectActAcctSum() throws Exception {
        hAcsuBilledEndBal = 0;
        sqlCmd = "select sum(billed_end_bal) h_acsu_billed_end_bal ";
        sqlCmd += " from act_acct_sum ";
        sqlCmd += "where p_seqno = ?  ";
        sqlCmd += "and decode(acct_code,'','x',acct_code) not in (";
        sqlCmd += "decode(cast(? as varchar(1)),'Y','AF','XX'), decode(cast(? as varchar(1)),'Y','LF','XX'), ";
        sqlCmd += "decode(cast(? as varchar(1)),'Y','CF','XX'), decode(cast(? as varchar(1)),'Y','PF','XX'), ";
        sqlCmd += "decode(cast(? as varchar(1)),'Y','RI','XX'), decode(cast(? as varchar(1)),'Y','PN','XX')) ";
        setString(1, hAcnoPSeqno);
        setString(2, hCmpmAfFlag);
        setString(3, hCmpmLfFlag);
        setString(4, hCmpmCfFlag);
        setString(5, hCmpmPfFlag);
        setString(6, hCmpmRiFlag);
        setString(7, hCmpmPnFlag);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_sum not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAcsuBilledEndBal = getValueLong("h_acsu_billed_end_bal");
        }

        if (hAcsuBilledEndBal == 0)
            return (0);
        return (1);
    }
    
    int selectColCsInfo() throws Exception {
    	hCloseFlag = "";
    	sqlCmd = "select close_flag from col_cs_info where p_seqno = ? and curr_code = ? ";
    	setString(1,hAcnoPSeqno);
    	setString(2,hDebtCurrCode);
    	int recordCnt = selectTable();
    	if(recordCnt > 0) {
    		hCloseFlag =  getValue("close_flag");
    		return 0; 
    	}
    	return 1;
    }

    /***********************************************************************/
    void insertColM0Base() throws Exception {
        insertCnt++;
        setValue("p_seqno", hAcnoPSeqno);
        setValue("acct_type", hAcnoAcctType);
        setValue("stmt_cycle", hCmpmStmtCycle);
        setValue("id_p_seqno", hAcnoIdPSeqno);
        setValue("corp_p_seqno", hAcnoCorpPSeqno);
        setValue("id_no", hAcnoAcctHolderId);
        setValue("corp_no", hAcnoCorpNo);
        setValue("card_indicator", hAcnoCardIndicator);
        setValue("recourse_mark", hAcnoRecourseMark);
        setValue("af_flag", hCmpmAfFlag);
        setValue("lf_flag", hCmpmLfFlag);
        setValue("cf_flag", hCmpmCfFlag);
        setValue("pf_flag", hCmpmPfFlag);
        setValue("ri_flag", hCmpmRiFlag);
        setValue("pn_flag", hCmpmPnFlag);
        setValueDouble("min_mp_amt", hCmpmMinMpAmt);
        setValue("acct_status", hAcnoAcctStatus);
        setValue("proc_mark", "N");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "col_m0_base";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_m0_base duplicate!", "", hCallBatchSeqno);
        }

    }
    
    void insertColCsInfo() throws Exception {
		setValue("p_seqno", hAcnoPSeqno);
		setValue("corp_p_seqno", !hAcnoAcctType.equals("01")?hAcnoCorpPSeqno:"");
		setValue("id_p_seqno", hAcnoIdPSeqno);
		setValue("curr_code", hDebtCurrCode);
		setValue("card_no", hDebtCardNo);
		setValue("reg_bank_no", hRegBankNo);
		setValue("issue_bank_no", hIssueBankNo);
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "col_cs_info";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insertColCsInfo duplicate!", "", hCallBatchSeqno);
		}
    }
    
    void updateColCsInfo() throws Exception {
        daoTable = "col_cs_info";
        updateSQL = "card_no = ?, ";
        updateSQL += " reg_bank_no = ?, ";
        updateSQL += " issue_bank_no = ?, ";
        updateSQL += " mod_time = sysdate, ";
        updateSQL += " mod_pgm = ? ";
        whereStr = "where p_seqno = ? and curr_code = ? ";
        setString(1, hDebtCardNo);
        setString(2, hRegBankNo);
        setString(3, hIssueBankNo);
        setString(4, javaProgram);
        setString(5, hAcnoPSeqno);
        setString(6, hDebtCurrCode);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("updateColCsInfo not found!", "", hCallBatchSeqno);
        }
    }
    
    void updateAcnoRegBankNo() throws Exception {
    	  daoTable = "act_acno";
          updateSQL = " reg_bank_no = ? ";
          whereStr = "where p_seqno = ? ";
          setString(1, hRegBankNo);
          setString(2, hAcnoPSeqno);
          updateTable();
          if (notFound.equals("Y")) {
              comcr.errRtn("updateAcnoRegBankNo not found!", "", hCallBatchSeqno);
          }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC020 proc = new ColC020();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
