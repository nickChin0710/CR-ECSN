/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/11  V1.00.00    phopho     program initial                          *
*  108/12/02  V1.00.01    phopho     fix err_rtn bug                          *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*  112/03/01  V1.00.02    sunny      cancel checkOpen()取消產生檔案                          *
******************************************************************************/

package Col;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

public class ColC021 extends AccessDAO {
    private String progname = "傳送CS(M0)D-產生帳務餘額媒體資料處理程式   112/03/01  V1.00.02 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;

    String rptName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    String buf = "";
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempBusinessDate = "";
    String hAcnoIdPSeqno = "";
    String hAcnoAcctHolderId = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hCmotPSeqno = "";
    String hCmotAcctType = "";
    String hAcnoAcctPSeqno = "";
    String hCmotFormType = "";
    String hAcnoStmtCycle = "";
    String hAcnoCorpNo = "";
    String hAcnoRecourseMark = "";
    String hAcnoAcctStatus = "";
    double hAcctMinPay = 0;
    double hAcctMinPayBal = 0;
    double hAcctAdiEndBal = 0;
    double hCmotStmtOverDueAmt = 0;
    double hCmotTtlAmtBal = 0;
    double hCmotDelAmt = 0;
    double hCmotAdjAmt = 0;
    String hCmotLastDDate = "";
    String hCmotDcCurrFlag = "";
    String hCmotAcctMonth = "";
    String hCmotRowid = "";
    String hWdayThisAcctMonth = "";
    String hWdayThisCloseDate = "";
    String hWdayThisLastpayDate = "";
    int hAcnoIntRateMcode = 0;
    String hIdnoBirthday = "";
    String hIdnoId = "";
    String hIdnoIdCode = "";
    String hCoidAcctCode = "";
    double hCoidUnbillEndBal = 0;
    double hCoidBilledEndBal =0;
    double hAcagPayAmt = 0;
    String hRhblRiskCode = "";
    String hAcsuAcctCode = "";
    double hAcsuCardSpecBal = 0;
    double hAcsuEndBalDbB = 0;
    double hAcsuEndBalDbC = 0;
    double hAcsuEndBalDbI = 0;
    double hAcsuBilledEndBal = 0;
    double hAcsuUnbillEndBal = 0;
    int hCnt = 0;
    double hAdclPayAmt = 0;
    double hD001DeductAmt = 0;
    String hCardIdPSeqno = "";
    String temstr = "";

    double[] endBalDb = new double[5];
    double[] billedArrayAmt = new double[30];
    double[] unbillArrayAmt = new double[30];
    String[] billArray = { "AF", "LF", "CC", "PF", "BL", "CA", "IT", "ID", "RI", "PN", "AO", "AI", "SF", "DP", "CB",
            "CI", "CF", "DB", "OT" };

    double ttlBal1 = 0;
    double ttlBal2 = 0;
    int[] reasonCnt = new int[5];
    int totalCnt = 0;
    int countBal = 0;
    int deleteCnt = 0;
    int updateCnt = 0;
    
    PrtBuf02 prtData = new PrtBuf02();

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
                comc.errExit("Usage : ColC021 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            if (args.length == 1)
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            showLogMessage("I", "", String.format("Now business_date [%s]", hBusiBusinessDate));
            //checkOpen();
            totalCnt = 0;
            showLogMessage("I", "", String.format("處理帳務餘額媒體開始....."));
            selectColM0Out();
            showLogMessage("I", "", String.format("累計處理筆數 [%d] 檔案幣數[%d]", totalCnt, countBal));
            showLogMessage("I", "", String.format("    1.商務卡公司  筆數[%d]", reasonCnt[0]));
            showLogMessage("I", "", String.format("      商務卡總繳  筆數[%d]", reasonCnt[3]));
            showLogMessage("I", "", String.format("    2.商務卡個繳  筆數[%d]", reasonCnt[1]));
            showLogMessage("I", "", String.format("    3.一般卡      筆數[%d]", reasonCnt[2]));
            showLogMessage("I", "", String.format("  刪除筆數 [%d] 更新筆數[%d]", deleteCnt, updateCnt));
            //checkClose();
            showLogMessage("I", "", String.format("========================================="));
            
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
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))), 'yyyymmdd') -1 days,'yyyymmdd') temp_business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempBusinessDate = getValue("temp_business_date");
        }
    }

    /*************************************************************************/
    void checkOpen() {
        temstr = String.format("%s/media/col/CS/M0ALBAL.%2.2s", comc.getECSHOME(), comc.getSubString(hTempBusinessDate,6));
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);

        buf = String.format("%15.15s%8.8s%06d", " ", " ", 0);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void selectColM0Out() throws Exception {

        for (int inti = 0; inti < reasonCnt.length; inti++)
            reasonCnt[inti] = 0;

//        sqlCmd = "select ";
//        sqlCmd += "a.id_p_seqno,";
//        sqlCmd += "UF_IDNO_ID(a.id_p_seqno) acct_holder_id,";
//        sqlCmd += "a.acct_type h_acno_acct_type,";
//        sqlCmd += "a.acct_key,";
//        sqlCmd += "a.p_seqno,";
//        sqlCmd += "c.acct_type h_cmot_acct_type,";
//        sqlCmd += "a.gp_no acct_p_seqno,";
//        sqlCmd += "c.form_type,";
//        sqlCmd += "a.stmt_cycle,";
//        sqlCmd += "p.corp_no,";
//        sqlCmd += "a.recourse_mark,";
//        sqlCmd += "a.acct_status,";
//        sqlCmd += "b.min_pay,";
//        sqlCmd += "b.min_pay_bal,";
//        sqlCmd += "b.adi_end_bal,";
//        sqlCmd += "c.stmt_over_due_amt,";
//        sqlCmd += "c.ttl_amt_bal,";
//        sqlCmd += "decode(del_date,?,c.del_amt,0) h_cmot_del_amt,";
//        sqlCmd += "decode(adj_date,?,c.adj_amt,0) h_cmot_adj_amt,";
//        sqlCmd += "c.last_d_date,";
//        sqlCmd += "c.dc_curr_flag,";
//        sqlCmd += "c.acct_month,";
//        sqlCmd += "c.rowid as rowid,";
//        sqlCmd += "d.this_acct_month,";
//        sqlCmd += "d.this_close_date,";
//        sqlCmd += "to_char(to_date(d.this_lastpay_date,'yyyymmdd')+e.exceed_pay_days,'yyyymmdd') h_wday_this_lastpay_date,";
//        sqlCmd += "a.int_rate_mcode ";
//        sqlCmd += "from act_acno a,act_acct b,col_m0_out c,ptr_workday d,col_m0_parm e ";
//        sqlCmd += " left join crd_corp p on p.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
//        sqlCmd += "where a.p_seqno = c.p_seqno ";
//        sqlCmd += "and a.p_seqno = b.p_seqno ";
//        sqlCmd += "and c.stmt_cycle = d.stmt_cycle ";
//        sqlCmd += "and c.stmt_cycle = e.stmt_cycle ";
//        sqlCmd += "and ((c.form_type = '1' ";
//        sqlCmd += "and c.corp_on_flag = 'Y') ";
//        sqlCmd += "OR c.form_type != '1') ";
//        sqlCmd += "and a.acno_flag <> 'Y' ";
//        setString(1, h_temp_business_date);
//        setString(2, h_temp_business_date);
        
        sqlCmd = "select ";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "q.id_no,";
        sqlCmd += "a.acct_type h_acno_acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "c.acct_type h_cmot_acct_type,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "c.form_type,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "p.corp_no,";
        sqlCmd += "a.recourse_mark,";
        sqlCmd += "a.acct_status,";
        sqlCmd += "b.min_pay,";
        sqlCmd += "b.min_pay_bal,";
        sqlCmd += "b.adi_end_bal,";
        sqlCmd += "c.stmt_over_due_amt,";
        sqlCmd += "c.ttl_amt_bal,";
        sqlCmd += "decode(del_date,?,c.del_amt,0) h_cmot_del_amt,";
        sqlCmd += "decode(adj_date,?,c.adj_amt,0) h_cmot_adj_amt,";
        sqlCmd += "c.last_d_date,";
        sqlCmd += "c.dc_curr_flag,";
        sqlCmd += "c.acct_month,";
        sqlCmd += "c.rowid as rowid,";
        sqlCmd += "d.this_acct_month,";
        sqlCmd += "d.this_close_date,";
        sqlCmd += "to_char(to_date(d.this_lastpay_date,'yyyymmdd')+e.exceed_pay_days,'yyyymmdd') h_wday_this_lastpay_date,";
        sqlCmd += "a.int_rate_mcode ";
        sqlCmd += "from act_acno a,act_acct b,col_m0_out c,ptr_workday d,col_m0_parm e ";
        sqlCmd += " left join crd_corp p on p.corp_p_seqno = a.corp_p_seqno "; //find corp_no in crd_corp
        sqlCmd += " left join crd_idno q on q.id_p_seqno = a.id_p_seqno ";
        sqlCmd += "where a.acno_p_seqno = c.p_seqno ";
        sqlCmd += "and a.acno_p_seqno = b.p_seqno ";
        sqlCmd += "and c.stmt_cycle = d.stmt_cycle ";
        sqlCmd += "and c.stmt_cycle = e.stmt_cycle ";
        sqlCmd += "and ((c.form_type = '1' ";
        sqlCmd += "and c.corp_on_flag = 'Y') ";
        sqlCmd += "OR c.form_type != '1') ";
        sqlCmd += "and a.acno_flag <> 'Y' ";
        setString(1, hTempBusinessDate);
        setString(2, hTempBusinessDate);
        
        openCursor();
        while (fetchTable()) {
            initDetail();
            hAcnoIdPSeqno = getValue("id_p_seqno");
//            h_acno_acct_holder_id = getValue("acct_holder_id");
            hAcnoAcctHolderId = getValue("id_no");
            hAcnoAcctType = getValue("h_acno_acct_type");
            hAcnoAcctKey = getValue("acct_key");
//            h_cmot_p_seqno = getValue("p_seqno");
            hCmotPSeqno = getValue("acno_p_seqno");
            hCmotAcctType = getValue("h_cmot_acct_type");
//            h_acno_acct_p_seqno = getValue("acct_p_seqno");
            hAcnoAcctPSeqno = getValue("p_seqno");
            hCmotFormType = getValue("form_type");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoCorpNo = getValue("corp_no");
            hAcnoRecourseMark = getValue("recourse_mark");
            hAcnoAcctStatus = getValue("acct_status");
            hAcctMinPay = getValueDouble("min_pay");
            hAcctMinPayBal = getValueDouble("min_pay_bal");
            hAcctAdiEndBal = getValueDouble("adi_end_bal");
            hCmotStmtOverDueAmt = getValueDouble("stmt_over_due_amt");
            hCmotTtlAmtBal = getValueDouble("ttl_amt_bal");
            hCmotDelAmt = getValueDouble("h_cmot_del_amt");
            hCmotAdjAmt = getValueDouble("h_cmot_adj_amt");
            hCmotLastDDate = getValue("last_d_date");
            hCmotDcCurrFlag = getValue("dc_curr_flag");
            hCmotAcctMonth = getValue("acct_month");
            hCmotRowid = getValue("rowid");
            hWdayThisAcctMonth = getValue("this_acct_month");
            hWdayThisCloseDate = getValue("this_close_date");
            hWdayThisLastpayDate = getValue("h_wday_this_lastpay_date");
            hAcnoIntRateMcode = getValueInt("int_rate_mcode");

            totalCnt++;

            hAdclPayAmt = 0;
            if (!hWdayThisLastpayDate.equals(hTempBusinessDate))
                selectActDebtCancel();

            if ((hAdclPayAmt == 0) && (hAcctMinPayBal <= 0)) {
                deleteColM0Out();
                continue;
            }

            selectActAcag();
            selectRskHiriskBill();
            selectActAcctSum();
            if (!hCmotDcCurrFlag.equals("Y")) {
                selectActAcctCurr();
                if (hCmotDcCurrFlag.equals("Y"))
                    updateColM0Out();
            }
            selectActD001r1();

            switch (comcr.str2int(hCmotFormType)) {
            case 1:/*商務卡公司*/
                printDetail();
                selectCrdCard();
                reasonCnt[0]++;
                break;
            case 2: /*商務卡個繳*/
                selectCrdIdno();
                printDetail();
                reasonCnt[1]++;
                break;
            case 3: /*一般卡*/
                hIdnoId = hAcnoAcctKey;
                printDetail();
                reasonCnt[2]++;
                break;
            }
            if ((hWdayThisCloseDate.equals(hTempBusinessDate)))
                deleteColM0Out();

            if (totalCnt % 5000 == 0) {
                showLogMessage("I", "", String.format("  目前處理筆數 [%d]", totalCnt));
                showLogMessage("I", "", String.format("    1.商務卡公司  筆數[%d]", reasonCnt[0]));
                showLogMessage("I", "", String.format("      商務卡總繳  筆數[%d]", reasonCnt[3]));
                showLogMessage("I", "", String.format("    2.商務卡個繳  筆數[%d]", reasonCnt[1]));
                showLogMessage("I", "", String.format("    3.一般卡      筆數[%d]", reasonCnt[2]));
            }
        }
        closeCursor();
    }

    /*************************************************************************/
    void checkClose() {

        buf = String.format("%15.15s%8.8s%06d", "TRAILER : END  ", hTempBusinessDate, countBal);
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = String.format("%15.15s%8.8s%06d", "HEADER : START ", hTempBusinessDate, countBal);
        lpar1.set(0, comcr.putReport(rptName1, rptName1, sysDate, rptSeq1++, "0", buf));

        comc.writeReport(temstr, lpar1);
    }

    /***********************************************************************/
    void selectActDebtCancel() throws Exception {
        hAdclPayAmt = 0;
        sqlCmd = "select nvl(sum(pay_amt),0) h_adcl_pay_amt ";
        sqlCmd += " from act_debt_cancel  ";
        sqlCmd += "where p_seqno  = ? ";
        sqlCmd += "and  substr(batch_no,9,4) not in ('9005','9007','9008','9009','9999') ";
        setString(1, hAcnoAcctPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_act_debt_cancel not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAdclPayAmt = getValueDouble("h_adcl_pay_amt");
        }
    }

    /***********************************************************************/
    void deleteColM0Out() throws Exception {
        daoTable = "col_m0_out";
        whereStr = "where rowid = ? ";
        setRowId(1, hCmotRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_col_m0_out not found!", "", hCallBatchSeqno);
        }

        deleteCnt++;
    }

    /***********************************************************************/
    void selectActAcag() throws Exception {
        hAcagPayAmt = 0;

        sqlCmd = "select pay_amt ";
        sqlCmd += " from act_acag  ";
        sqlCmd += "where p_seqno = ? ORDER BY acct_month ";
        setString(1, hCmotPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcagPayAmt = getValueDouble("pay_amt");
        } else {
            hAcagPayAmt = 0;
            return;
        }

        hAcctMinPayBal = hAcagPayAmt;
    }

    /***********************************************************************/
    void selectActAcctSum() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "acct_code,";
        sqlCmd += "unbill_end_bal,";
        sqlCmd += "billed_end_bal,";
        sqlCmd += "end_bal_db_b,";
        sqlCmd += "end_bal_db_c,";
        sqlCmd += "end_bal_db_i,";
        sqlCmd += "card_spec_bal ";
        sqlCmd += "from act_acct_sum ";
        sqlCmd += "where p_seqno = ? ";
        setString(1, hAcnoAcctPSeqno);

        int recordCnt = selectTable();
        for(int i =0; i <recordCnt; i++) {
            hAcsuAcctCode = getValue("acct_code",i);
            hAcsuUnbillEndBal = getValueDouble("unbill_end_bal",i);
            hAcsuBilledEndBal = getValueDouble("billed_end_bal",i);
            hAcsuEndBalDbB = getValueDouble("end_bal_db_b",i);
            hAcsuEndBalDbC = getValueDouble("end_bal_db_c",i);
            hAcsuEndBalDbI = getValueDouble("end_bal_db_i",i);
            hAcsuCardSpecBal = getValueDouble("card_spec_bal",i);
            for (int inta = 0; inta < 19; inta++) {
                if (hAcsuAcctCode.equals("DB")) {
                    endBalDb[0] = hAcsuEndBalDbB;
                    endBalDb[1] = hAcsuEndBalDbC;
                    endBalDb[2] = hAcsuEndBalDbI;
                }
                if (hAcsuAcctCode.equals(billArray[inta])) {
                    billedArrayAmt[inta] = hAcsuBilledEndBal;
                    unbillArrayAmt[inta] = hAcsuUnbillEndBal;
                    break;
                }
            }
            billedArrayAmt[20] = billedArrayAmt[20] + hAcsuCardSpecBal;
        }

        for (int inta = 0; inta < 19; inta++) {
            if (inta == 13)
                continue;
            ttlBal1 = ttlBal1 + billedArrayAmt[inta];
            ttlBal2 = ttlBal2 + unbillArrayAmt[inta];
        }
    }

    /***********************************************************************/
    void selectRskHiriskBill() throws Exception {
        hRhblRiskCode = "";
//        sqlCmd = "select min(risk_code) as risk_code ";
//        sqlCmd += " from rsk_hirisk_bill ";
//        sqlCmd += "where p_seqno = ?  ";  //No column p_seqno??? 
//        sqlCmd += "and decode(proc_flag,'','N',proc_flag) != 'Y' ";
        
        sqlCmd = "select min(risk_code) as risk_code ";
        sqlCmd += " from rsk_hirisk_bill, crd_card ";
        sqlCmd += "where rsk_hirisk_bill.card_no = crd_card.card_no ";
        sqlCmd += "and crd_card.acno_p_seqno = ? ";
        sqlCmd += "and decode(proc_flag,'','N',proc_flag) != 'Y' ";
        setString(1, hCmotPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hRhblRiskCode = getValue("risk_code");
        }
    }

    /***********************************************************************/
    void selectActAcctCurr() throws Exception {
        hCmotDcCurrFlag = "N";
        sqlCmd = "select 1 ";
        sqlCmd += " from act_acct_curr  ";
        sqlCmd += "where curr_code != '901'  ";
        sqlCmd += "and p_seqno = ?  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hAcnoAcctPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCmotDcCurrFlag = "Y";
        }

    }

    /***********************************************************************/
    void selectActD001r1() throws Exception {
        hD001DeductAmt = 0;
        sqlCmd = "select sum(nvl(deduct_amt,0)) h_d001_deduct_amt ";
        sqlCmd += " from act_d001r1  ";
      //sqlCmd += "where id_no = ?  ";
        sqlCmd += "where id_p_seqno = ?  ";
        sqlCmd += "and proc_date = ? ";
      //setString(1, h_acno_acct_holder_id);
        setString(1, hAcnoIdPSeqno);
        setString(2, hTempBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
          //comcr.err_rtn("select_select_act_d001r1 not found!", "", h_call_batch_seqno);
            hD001DeductAmt = 0;
        }
        if (recordCnt > 0) {
            hD001DeductAmt = getValueDouble("h_d001_deduct_amt");
        }
    }

    /***********************************************************************/
    void updateColM0Out() throws Exception {
        daoTable = "col_m0_out";
        updateSQL = "dc_curr_flag = 'Y'";
        whereStr = "where rowid = ? ";
        setRowId(1, hCmotRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_m0_out not found!", "", hCallBatchSeqno);
        }

        updateCnt++;
    }

    /***********************************************************************/
    void printDetail() throws Exception {

        String tmpstr = "";

        prtData.corpNo = hAcnoCorpNo;
        prtData.id = hIdnoId;
        prtData.acctType = hAcnoAcctType;
        tmpstr = String.format("%14.2f", billedArrayAmt[1]);
        prtData.arrayAmt01 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[0]);
        prtData.arrayAmt02 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[3]);
        prtData.arrayAmt03 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[16]);
        prtData.arrayAmt04 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[8]);
        prtData.arrayAmt05 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[9]);
        prtData.arrayAmt06 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[12]);
        prtData.arrayAmt07 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[10]);
        prtData.arrayAmt08 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[4] + billedArrayAmt[5] + billedArrayAmt[6]
                + billedArrayAmt[7] + billedArrayAmt[18]);
        prtData.arrayAmt09 = tmpstr;
        tmpstr = String.format("%14.2f", ttlBal1 + ttlBal2 + hAcctAdiEndBal);
        prtData.arrayAmt10 = tmpstr;
        tmpstr = String.format("%14.2f", ttlBal1);
        prtData.arrayAmt11 = tmpstr;
        tmpstr = String.format("%14.2f", ttlBal2 + hAcctAdiEndBal);
        prtData.arrayAmt12 = tmpstr;
        tmpstr = String.format("%14.2f", hAcctMinPay);
        prtData.arrayAmt13 = tmpstr;
        tmpstr = String.format("%14.2f", hAcctMinPayBal);
        prtData.arrayAmt14 = tmpstr;
        tmpstr = String.format("%14.2f", hAdclPayAmt);
        prtData.arrayAmt15 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[13] + unbillArrayAmt[13]);
        prtData.arrayAmt16 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[2] + unbillArrayAmt[2] + billedArrayAmt[14]
                + unbillArrayAmt[14] + billedArrayAmt[15] + unbillArrayAmt[15]);
        prtData.arrayAmt17 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[14] + unbillArrayAmt[14]);
        prtData.arrayAmt18 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[15] + unbillArrayAmt[15]);
        prtData.arrayAmt19 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[2] + unbillArrayAmt[2]);
        prtData.arrayAmt20 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[11] + unbillArrayAmt[11] + hAcctAdiEndBal);
        prtData.arrayAmt21 = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[17] + unbillArrayAmt[17]);
        prtData.arrayAmt22 = tmpstr;
        prtData.recourseMark = hAcnoRecourseMark;
        prtData.acctStatus = hAcnoAcctStatus;
//        int inta = comr.getMcode(h_cmot_acct_type, h_cmot_p_seqno);// get_M_code(h_cmot_p_seqno);
        int inta = hAcnoIntRateMcode;
        if (inta > 24)
            inta = 24;
        tmpstr = String.format("%02d", inta);
        prtData.mcode = tmpstr;
        tmpstr = String.format("%14.2f", hCmotStmtOverDueAmt);
        prtData.stmtOverDueAmt = tmpstr;
        tmpstr = String.format("%14.2f", hCmotTtlAmtBal);
        prtData.ttlAmtBal = tmpstr;
        tmpstr = String.format("%14.2f", billedArrayAmt[20]);
        prtData.arrayAmt23 = tmpstr;
        prtData.riskCode = hRhblRiskCode;
        prtData.lastDDate = hCmotLastDDate;
        tmpstr = String.format("%14.2f", hD001DeductAmt);
        prtData.deductAmt = tmpstr;
        prtData.dcCurrFlag = hCmotDcCurrFlag;
        tmpstr = String.format("%14.2f", endBalDb[0]);
        prtData.endBalDbB = tmpstr;
        tmpstr = String.format("%14.2f", endBalDb[1]);
        prtData.endBalDbC = tmpstr;
        tmpstr = String.format("%14.2f", endBalDb[2]);
        prtData.endBalDbI = tmpstr;
        tmpstr = String.format("%14.2f", hCmotDelAmt);
        prtData.delAmt = tmpstr;
        tmpstr = String.format("%14.2f", hCmotAdjAmt);
        prtData.adjAmt = tmpstr;

        buf = prtData.allText();
        
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
        countBal++;
    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_p_seqno ";
        sqlCmd += "from crd_card ";
//        sqlCmd += "where gp_no = ? ";
//        sqlCmd += "and decode(p_seqno,'','x',p_seqno) != gp_no ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and decode(acno_p_seqno,'','x',acno_p_seqno) != p_seqno ";
        sqlCmd += "group by id_p_seqno ";
        setString(1, hAcnoAcctPSeqno);
        int recordCnt = selectTable();
        for (int i =0; i <recordCnt; i++) {
            hCardIdPSeqno = getValue("id_p_seqno",i);

            initDetail();

            reasonCnt[3]++;
            selectActCoidSum();
            selectCrdIdno();
            printDetail();
        }
    }

    /***********************************************************************/
    void initDetail() throws Exception {
        ttlBal1 = 0;
        ttlBal2 = 0;
        for (int inta = 0; inta < 3; inta++)
            endBalDb[inta] = 0;
        for (int inta = 0; inta < 29; inta++)
            billedArrayAmt[inta] = unbillArrayAmt[inta] = 0;
        hIdnoId = "";
    }

    /***********************************************************************/
    void selectActCoidSum() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "acct_code,";
        sqlCmd += "unbill_end_bal,";
        sqlCmd += "billed_end_bal ";
        sqlCmd += "from act_coid_sum ";
        sqlCmd += "where p_seqno = ? ";
        sqlCmd += "and id_p_seqno = ? ";
        setString(1, hAcnoAcctPSeqno);
        setString(2, hCardIdPSeqno);

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCoidAcctCode = getValue("acct_code", i);
            hCoidUnbillEndBal = getValueDouble("unbill_end_bal", i);
            hCoidBilledEndBal = getValueDouble("billed_end_bal", i);
            for (int inta = 0; inta < 19; inta++) {
                if (hCoidAcctCode.equals(billArray[inta])) {
                    billedArrayAmt[inta] = hCoidBilledEndBal;
                    unbillArrayAmt[inta] = hCoidUnbillEndBal;
                }
            }
        }

        for (int inta = 0; inta < 19; inta++) {
            if (inta == 13)
                continue;
            ttlBal1 = ttlBal1 + billedArrayAmt[inta];
            ttlBal2 = ttlBal2 + unbillArrayAmt[inta];
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoBirthday = "";
        hIdnoId = "";
        hIdnoIdCode = "";

        sqlCmd = "select birthday,";
        sqlCmd += "id_no||id_no_code id_no,";
        sqlCmd += "id_no_code ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hAcnoIdPSeqno); //20221222 原為hCardIdPSeqno，SUNNY FIX 
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            //comcr.errRtn("select_crd_idno not found!","", hCallBatchSeqno);
            showLogMessage("I", "", String.format("select_crd_idno not found! id_p_seqno[%s]", hAcnoIdPSeqno));
        }
        if (recordCnt > 0) {
            hIdnoBirthday = getValue("birthday");
            hIdnoId = getValue("id_no");
            hIdnoIdCode = getValue("id_no_code");
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColC021 proc = new ColC021();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class PrtBuf02 {
        String corpNo;
        String id                ;
        String acctType;
        String arrayAmt01;
        String arrayAmt02;
        String arrayAmt03;
        String arrayAmt04;
        String arrayAmt05;
        String arrayAmt06;
        String arrayAmt07;
        String arrayAmt08;
        String arrayAmt09;
        String arrayAmt10;
        String arrayAmt11;
        String arrayAmt12;
        String arrayAmt13;
        String arrayAmt14;
        String arrayAmt15;
        String arrayAmt16;
        String arrayAmt17;
        String arrayAmt18;
        String arrayAmt19;
        String arrayAmt20;
        String arrayAmt21;
        String arrayAmt22;
        String recourseMark;
        String acctStatus;
        String mcode             ;
        String stmtOverDueAmt;
        String ttlAmtBal;
        String arrayAmt23;
        String riskCode;
        String lastDDate;
        String deductAmt        ;
        String dcCurrFlag;
        String endBalDbB;
        String endBalDbI;
        String endBalDbC;
        String delAmt;
        String adjAmt;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
//            rtn += fixLeft(corp_no          ,9);       
//            rtn += fixLeft(id               ,12);         
//            rtn += fixLeft(acct_type        ,3);              
//            rtn += fixLeft(array_amt01      ,15);       
//            rtn += fixLeft(array_amt02      ,15); 
//            rtn += fixLeft(array_amt03      ,15);     
//            rtn += fixLeft(array_amt04      ,15);     
//            rtn += fixLeft(array_amt05      ,15);     
//            rtn += fixLeft(array_amt06      ,15);     
//            rtn += fixLeft(array_amt07      ,15);     
//            rtn += fixLeft(array_amt08      ,15);     
//            rtn += fixLeft(array_amt09      ,15);     
//            rtn += fixLeft(array_amt10      ,15);     
//            rtn += fixLeft(array_amt11      ,15);     
//            rtn += fixLeft(array_amt12      ,15);     
//            rtn += fixLeft(array_amt13      ,15);     
//            rtn += fixLeft(array_amt14      ,15);     
//            rtn += fixLeft(array_amt15      ,15);     
//            rtn += fixLeft(array_amt16      ,15);     
//            rtn += fixLeft(array_amt17      ,15);     
//            rtn += fixLeft(array_amt18      ,15);     
//            rtn += fixLeft(array_amt19      ,15);     
//            rtn += fixLeft(array_amt20      ,15);     
//            rtn += fixLeft(array_amt21      ,15);     
//            rtn += fixLeft(array_amt22      ,15);     
//            rtn += fixLeft(recourse_mark    ,2);     
//            rtn += fixLeft(acct_status      ,2);     
//            rtn += fixLeft(mcode            ,3);
//            rtn += fixLeft(stmt_over_due_amt,15);     
//            rtn += fixLeft(ttl_amt_bal      ,15);
//            rtn += fixLeft(array_amt23      ,15);      
//            rtn += fixLeft(risk_code        ,3);
//            rtn += fixLeft(last_d_date      ,9); 
//            rtn += fixLeft(deduct_amt       ,15); 
//            rtn += fixLeft(dc_curr_flag     ,2);         
//            rtn += fixLeft(end_bal_db_b     ,15);         
//            rtn += fixLeft(end_bal_db_i     ,15);       
//            rtn += fixLeft(end_bal_db_c     ,15);       
//            rtn += fixLeft(del_amt          ,15);       
//            rtn += fixLeft(adj_amt          ,15);       

            //文字靠左,數字靠右,分隔符號^
            rtn += fixLeft(corpNo,8)+"^";
            rtn += fixLeft(id               ,11)+"^";
            rtn += fixLeft(acctType,2)+"^";
            rtn += fixRight(arrayAmt01,14)+"^";
            rtn += fixRight(arrayAmt02,14)+"^";
            rtn += fixRight(arrayAmt03,14)+"^";
            rtn += fixRight(arrayAmt04,14)+"^";
            rtn += fixRight(arrayAmt05,14)+"^";
            rtn += fixRight(arrayAmt06,14)+"^";
            rtn += fixRight(arrayAmt07,14)+"^";
            rtn += fixRight(arrayAmt08,14)+"^";
            rtn += fixRight(arrayAmt09,14)+"^";
            rtn += fixRight(arrayAmt10,14)+"^";
            rtn += fixRight(arrayAmt11,14)+"^";
            rtn += fixRight(arrayAmt12,14)+"^";
            rtn += fixRight(arrayAmt13,14)+"^";
            rtn += fixRight(arrayAmt14,14)+"^";
            rtn += fixRight(arrayAmt15,14)+"^";
            rtn += fixRight(arrayAmt16,14)+"^";
            rtn += fixRight(arrayAmt17,14)+"^";
            rtn += fixRight(arrayAmt18,14)+"^";
            rtn += fixRight(arrayAmt19,14)+"^";
            rtn += fixRight(arrayAmt20,14)+"^";
            rtn += fixRight(arrayAmt21,14)+"^";
            rtn += fixRight(arrayAmt22,14)+"^";
            rtn += fixLeft(recourseMark,1)+"^";
            rtn += fixLeft(acctStatus,1)+"^";
            rtn += fixRight(mcode            ,2)+"^";
            rtn += fixRight(stmtOverDueAmt,14)+"^";
            rtn += fixRight(ttlAmtBal,14)+"^";
            rtn += fixRight(arrayAmt23,14)+"^";
            rtn += fixLeft(riskCode,2)+"^";
            rtn += fixLeft(lastDDate,8)+"^";
            rtn += fixRight(deductAmt       ,14)+"^";
            rtn += fixLeft(dcCurrFlag,1)+"^";
            rtn += fixRight(endBalDbB,14)+"^";
            rtn += fixRight(endBalDbI,14)+"^";
            rtn += fixRight(endBalDbC,14)+"^";
            rtn += fixRight(delAmt,14)+"^";
            rtn += fixRight(adjAmt,14);
            return rtn;
        }

        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("MS950");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "MS950");
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
    }
}
