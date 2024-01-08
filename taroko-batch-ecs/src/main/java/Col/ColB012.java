/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/03/29  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  108/12/04  V1.00.02    phopho     fix substring error                      *
*  109/02/12  V1.00.03    phopho     Mantis 0002594: 列印表日設定為系統日期.   *
*  109/12/12  V1.00.04    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import java.text.Normalizer;

import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB012 extends AccessDAO {
    private String progname = "資產評估表處理程式  109/12/12  V1.00.04 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "COL_B012R1";
    String rptDesc1 = "資產評估表";
    int    rptSeq1            = 0;
    String buf                = "";
    String szTmp              = "";
    String hCallBatchSeqno = "";

    String hTempProcMonth = "";
    String hMndaProcMonth = "";
    String hPaccCurrCode = "";
    String hPcceCurrChiName = "";
    int hTempCount = 0;
    String hPaccAcctType = "";
    String hPaccChinName = "";
    long hTempType = 0;
    String hTempTransType = "";
    String hMndaPSeqno = "";
    String hMndaId = "";
    String hIdnoChiName = "";
    String hCorpBusinessCode = "";
    String hMndaIdPSeqno = "";
    String hMndaCreditActNo = "";
    String hMndaTransDate = "";
    String hMndaCorpNo = "";
    String hMndaCorpActFlag = "";
    String hMndaCorpPSeqno = "";
    String hMndaIdCode = "";
    long hMndaMcode = 0;
    double hMndaMcodeRate = 0;
    String hMndaLawsuitProcessLog = "";
    long hMndaAcctJrnlBal = 0;
    String hTempProcMonth1 = "";
    String hCorpChiName = "";
    String hBusiBusinessDate = "";
    String hPrintName = "";
    String hRptName = "";

    String temstr               = "";
    String temstr1              = "";
    String szBuffer             = "";
    String szTmp1               = "";
    int pageCnt = 0;
    int lineCnt = 0;
    int totalCnt = 0;
    int ttotalCnt = 0;
    int indexCnt = 0;
    int pageLine = 0;
    int    inta                 = 0;
    int    intb                 = 0;
    long pageAmt = 0;
    long totalAmt = 0;
    long   ttotalAmt           = 0;
    long pageCbAmt = 0;
    long totalCbAmt = 0;
    long ttotalCbAmt = 0;
    long pageCiAmt = 0;
    long totalCiAmt = 0;
    long ttotalCiAmt = 0;
    long pageCcAmt = 0;
    long totalCcAmt = 0;
    long ttotalCcAmt = 0;
    String sId = "";
    String sName = "";
    double nSMcodeRate = 0;
    int nEMcodeRate = 0;
    long nSAcctJrnlBal = 0;
    long nEAcctJrnlBal = 0;
    long nLineOfCreditAmt = 0;
    String tempMonth = "";
    int rInt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                comc.errExit("Usage : ColB012 month", "              1.month  : 月份");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connectDataBaseerror", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            //online call batch 時須記錄
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);

            selectPtrBusinday();
            if ((args.length == 0) || ((args.length == 1) && (args[0].length() > 6))) {
                tempMonth = comc.getSubString(hBusiBusinessDate,0,6);
                temstr = comcr.getBusinday(hBusiBusinessDate, -1);
                if ((!comc.getSubString(hBusiBusinessDate,4,6).equals("03"))
                        && (!comc.getSubString(hBusiBusinessDate,4,6).equals("06"))
                        && (!comc.getSubString(hBusiBusinessDate,4,6).equals("09"))
                        && (!comc.getSubString(hBusiBusinessDate,4,6).equals("12"))) {
                	exceptExit = 0;
                    comcr.errRtn(String.format("本報表只能在每季執行"), "", hCallBatchSeqno);
                } else if (!comc.getSubString(hBusiBusinessDate,0,8).equals(temstr)) {
                	exceptExit = 0;
                    comcr.errRtn("本報表只能在每季該月最後營業日前一日執行",
                    		String.format("最後營業日前一日[%s]", temstr), hCallBatchSeqno);
                }
                hTempProcMonth = tempMonth;
            } else {
//                szTmp = String.format("%6d", comcr.str2long(args[0]) + 191100);
                if (args[0].length() == 6) {
                    String sGArgs0 = "";
                    sGArgs0 = args[0];
                    sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                    szTmp = sGArgs0;
                }
                hTempProcMonth = szTmp;
            }

            checkOpen();
            selectProcMonth();

            for (intb = 2; intb < 4; intb++) {
                pageCnt = lineCnt = totalCnt = ttotalCnt = indexCnt = pageLine = 0;
                pageAmt = totalAmt = ttotalAmt = 0;
                pageCbAmt = totalCbAmt = ttotalCbAmt = 0;
                pageCiAmt = totalCiAmt = ttotalCiAmt = 0;
                pageCcAmt = totalCcAmt = ttotalCcAmt = 0;
                szTmp = String.format("%d", intb);
                hTempTransType = szTmp;
                selectPtrAcctType();
            }

            comcr.insertPtrBatchRpt(lpar1);  //phopho add 問題單:0001126
            comc.writeReport(temstr, lpar1);
            if (args.length != 1)
                comcr.lpRtn("COL_D_VOUCH", "");

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***************************************************************************/
    void checkOpen() {
        temstr = String.format("%s/reports/COL_B012_%s", comc.getECSHOME(), hTempProcMonth);
        showLogMessage("I", "", String.format("報表名稱 : %s", temstr));

        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectProcMonth() throws Exception {
        hMndaProcMonth = "";

        sqlCmd = "select to_char(add_months(to_date( ? ,'yyyymm'),-1),'yyyymm') h_mnda_proc_month ";
        sqlCmd += " from dual ";
        setString(1, hTempProcMonth);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_proc_month not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hMndaProcMonth = getValue("h_mnda_proc_month");
        }
    }

    /***********************************************************************/
    void selectPtrAcctType() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "b.curr_code,";
        sqlCmd += "min(a.curr_chi_name) h_pcce_curr_chi_name,";
        sqlCmd += "count(*) h_temp_count ";
        sqlCmd += "from ptr_currcode a,ptr_acct_type b ";
        sqlCmd += "where a.curr_code = b.curr_code ";
        sqlCmd += "group by b.curr_code ";

        openCursor();
        while (fetchTable()) {
            hPaccCurrCode = getValue("curr_code");
            hPcceCurrChiName = getValue("h_pcce_curr_chi_name");
            hTempCount = getValueInt("h_temp_count");

            hTempType = 1;
            hTempCount = 0;
            selectPtrAcctType1();
            if (hTempCount > 1) {
                pageCnt = lineCnt = totalCnt = indexCnt = pageLine = 0;
                pageAmt = totalAmt = 0;
                pageCbAmt = totalCbAmt = 0;
                pageCiAmt = totalCiAmt = 0;
                pageCcAmt = totalCcAmt = 0;
                hTempType = 0;
                totalCnt = ttotalCnt;
                totalAmt = ttotalAmt;
                totalCbAmt = ttotalCbAmt;
                totalCiAmt = ttotalCiAmt;
                totalCcAmt = ttotalCcAmt;
                printHeader();
                printTotal();
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectPtrAcctType1() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "acct_type,";
        sqlCmd += "chin_name ";
        sqlCmd += "from ptr_acct_type ";
        sqlCmd += "where curr_code = ? ";
        sqlCmd += "order by acct_type ";
        setString(1, hPaccCurrCode);
        
        extendField = "ptr_acct_type_1.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hPaccAcctType = getValue("ptr_acct_type_1.acct_type", i);
            hPaccChinName = getValue("ptr_acct_type_1.chin_name", i);

            pageCnt = lineCnt = totalCnt = indexCnt = pageLine = 0;
            pageAmt = totalAmt = 0;
            pageCbAmt = totalCbAmt = 0;
            pageCiAmt = totalCiAmt = 0;
            pageCcAmt = totalCcAmt = 0;

            selectColMonthData0();
            ttotalCnt = ttotalCnt + totalCnt;
            ttotalAmt = ttotalAmt + totalAmt;
            ttotalCbAmt = ttotalCbAmt + totalCbAmt;
            ttotalCiAmt = ttotalCiAmt + totalCiAmt;
            ttotalCcAmt = ttotalCcAmt + totalCcAmt;

            if (totalCnt > 0) {
                printTotal();
                hTempCount++;
            }
        }
    }

    /***********************************************************************/
    void selectColMonthData0() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "id_no ";
        sqlCmd += "from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and acct_type = decode(cast(? as int),0,acct_type,cast(? as varchar(2))) ";
        sqlCmd += "and trans_type = ? ";
        sqlCmd += "order by id_no ";
        setString(1, hTempProcMonth);
        setLong(2, hTempType);
        setString(3, hPaccAcctType);
        setString(4, hTempTransType);
        
        extendField = "col_month_data_0.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMndaPSeqno = getValue("col_month_data_0.p_seqno", i);
            hMndaId = getValue("col_month_data_0.id_no",i);
            sId = "";
            sName = "";
            nSAcctJrnlBal = 0;
            nEAcctJrnlBal = 0;
            nSMcodeRate = 0;
            nEMcodeRate = 0;

            hTempProcMonth1 = hTempProcMonth;
            rInt = selectColMonthData();
            nSMcodeRate = hMndaMcodeRate;
            nEAcctJrnlBal = (long) (hMndaAcctJrnlBal + 0.5); /* 取消以仟元為單位 */
            if (hMndaCorpActFlag.equals("Y")) {
                selectCrdCorp();
                sId = String.format("%s", hMndaCorpNo);
                sName = String.format("%s", hCorpChiName);
            } else {
                selectCrdIdno();
                sId = String.format("%s", hMndaId);
                sName = String.format("%s", hIdnoChiName);
            }

            if (indexCnt == 0)
                printHeader();

            printDetail();
            if (indexCnt >= 45) {
                printFooter();
                indexCnt = 0;
            }

        }

        if (pageCnt != 0)
            printFooter();
    }

    /***********************************************************************/
    int selectColMonthData() throws Exception {
        hMndaCreditActNo = "";
        hMndaTransDate = "";
        hMndaCorpNo = "";
        hMndaCorpActFlag = "";
        hMndaCorpPSeqno = "";
        hMndaIdPSeqno = "";
        hMndaId = "";
        hMndaIdCode = "";
        hMndaLawsuitProcessLog = "";
        hMndaAcctJrnlBal = 0;
        hMndaMcode = 0;
        hMndaMcodeRate = 0;

        sqlCmd = "select credit_act_no,";
        sqlCmd += "trans_date,";
        sqlCmd += "corp_no,";
        sqlCmd += "corp_act_flag,";
        sqlCmd += "corp_p_seqno,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "id_no,";
        sqlCmd += "id_code,";
        sqlCmd += "mcode,";
        sqlCmd += "mcode_rate,";
        sqlCmd += "substr(lawsuit_process_log,1,26) h_mnda_lawsuit_process_log,";
        sqlCmd += "decode(cast(? as varchar(1)), '2',nvl(billed_1_amt,0)+nvl(unbill_1_amt,0), '3',nvl(billed_1_amt,0)+nvl(unbill_1_amt,0)+ nvl(billed_2_amt,0)+nvl(unbill_2_amt,0)+ nvl(billed_3_amt,0)+nvl(unbill_3_amt,0), acct_jrnl_bal) h_mnda_acct_jrnl_bal ";
        sqlCmd += " from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and p_seqno = ?  ";
        sqlCmd += "and trans_type = ? ";
        setString(1, hTempTransType);
        setString(2, hTempProcMonth1);
        setString(3, hMndaPSeqno);
        setString(4, hTempTransType);
        
        extendField = "col_month_data.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hMndaCreditActNo = getValue("col_month_data.credit_act_no");
            hMndaTransDate = getValue("col_month_data.trans_date");
            hMndaCorpNo = getValue("col_month_data.corp_no");
            hMndaCorpActFlag = getValue("col_month_data.corp_act_flag");
            hMndaCorpPSeqno = getValue("col_month_data.corp_p_seqno");
            hMndaIdPSeqno = getValue("col_month_data.id_p_seqno");
            hMndaId = getValue("col_month_data.id_no");
            hMndaIdCode = getValue("col_month_data.id_code");
            hMndaMcode = getValueLong("col_month_data.mcode");
            hMndaMcodeRate = getValueDouble("col_month_data.mcode_rate");
            hMndaLawsuitProcessLog = getValue("col_month_data.h_mnda_lawsuit_process_log");
            hMndaAcctJrnlBal = getValueLong("col_month_data.h_mnda_acct_jrnl_bal");
        } else {
            return 1;
        }
        return 0;
    }

    /***********************************************************************/
    void selectCrdCorp() throws Exception {
        hCorpChiName = "";
        hCorpBusinessCode = "";

        sqlCmd = "select chi_name,";
        sqlCmd += "business_code ";
        sqlCmd += " from crd_corp ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hMndaCorpPSeqno);
        
        extendField = "crd_corp.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCorpChiName = getValue("crd_corp.chi_name");
            hCorpBusinessCode = getValue("crd_corp.business_code");
        }
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        hCorpBusinessCode = "";

        sqlCmd = "select uf_hi_cname(chi_name) h_idno_chi_name,";
        sqlCmd += "business_code ";
        sqlCmd += " from crd_idno ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hMndaIdPSeqno);
        
        extendField = "crd_idno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("crd_idno.h_idno_chi_name");
            hCorpBusinessCode = getValue("crd_idno.business_code");
        }
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        String sTransDate = "";

        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, sId, 1);
        buf = comcr.insertStr(buf, sName, 13);
        sTransDate = hMndaTransDate;
        szTmp1 = String.format("%4.4s", sTransDate);
        szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1)-1911, 
        		comc.getSubString(sTransDate,4), comc.getSubString(sTransDate,6));
        buf = comcr.insertStr(buf, szTmp, 38);
        buf = comcr.insertStr(buf, hMndaLawsuitProcessLog, 48);
        szTmp = comcr.commFormat("1$,3$,3$,3$", nEAcctJrnlBal);
        if (nSMcodeRate == 0) {
            buf = comcr.insertStr(buf, "可全部收回", 76);
            buf = comcr.insertStr(buf, szTmp, 88);
            pageCbAmt = pageCbAmt + nEAcctJrnlBal;
            totalCbAmt = totalCbAmt + nEAcctJrnlBal;
        }
        if ((nSMcodeRate > 0) && (nSMcodeRate < 100)) {
            buf = comcr.insertStr(buf, "可部份收回", 76);
            buf = comcr.insertStr(buf, szTmp, 104);
            pageCiAmt = pageCiAmt + nEAcctJrnlBal;
            totalCiAmt = totalCiAmt + nEAcctJrnlBal;
        }
        if (nSMcodeRate == 100) {
            buf = comcr.insertStr(buf, "收回無望", 76);
            buf = comcr.insertStr(buf, szTmp, 120);
            pageCcAmt = pageCcAmt + nEAcctJrnlBal;
            totalCcAmt = totalCcAmt + nEAcctJrnlBal;
        }
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        pageCnt++;
        totalCnt++;
    }

    /***********************************************************************/
    void printFooter() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "筆數:", 1);
        szTmp = String.format("%6d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 12);
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageCbAmt);
        buf = comcr.insertStr(buf, szTmp, 87);
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageCiAmt);
        buf = comcr.insertStr(buf, szTmp, 103);
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageCcAmt);
        buf = comcr.insertStr(buf, szTmp, 119);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        pageCnt = 0;
        pageCbAmt = 0;
        pageCiAmt = 0;
        pageCcAmt = 0;
        pageAmt = 0;
    }

    /***********************************************************************/
    void printHeader() throws Exception {
        String dispDate = "";
        String[] sItem = { "逾期放款", "催收款  " };
        String[] tItem = { "原始逾放日", "轉列催收日" };

        pageLine++;
        buf = "";
        buf = comcr.insertStr(buf, "COL_B012R1", 1);
        szTmp = comcr.bankName;
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        buf = comcr.insertStr(buf, "列印表日 :", 110);
        //disp_date = comc.convDates(sysDate, 1);
        //Mantis 0002594:
        //1. 目前現行系統（舊系統）是「執行批次日期+1天」為報表之「列印表日」。
        //2. 新系統可以執行線上批次，造成日期差一天的疑慮。
        //3. 現依據user提出，將「列印表日」修改為執行批次之日期。
        dispDate = sysDate;

        buf = comcr.insertStr(buf, dispDate, 121);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStrCenter(buf, "資  產  評  估  表", 132);
        buf = comcr.insertStr(buf, "列印頁數 :", 110);
        szTmp = String.format("%4d", pageLine);
        buf = comcr.insertStr(buf, szTmp, 125);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "帳戶類別:", 1);
        if (hTempType != 0) {
            szTmp = String.format("%2.2s %s", hPaccAcctType, hPaccChinName);
        } else {
            szTmp = String.format("%s 合計", hPcceCurrChiName);
        }
        buf = comcr.insertStr(buf, szTmp, 10);
        szTmp = String.format("貨幣單位 : %s 元", hPcceCurrChiName);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        szTmp = String.format("原放款科目:%8.8s", sItem[intb - 2]);
        buf = comcr.insertStr(buf, szTmp, 1);
        szTmp = String.format("%03.0f年%2.2s月份", (comcr.str2long(hTempProcMonth) - 191100) / 100.0,
                comc.getSubString(hTempProcMonth,4));
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "評  估  摘  要", 56);
        buf = comcr.insertStr(buf, "評  估  分  類  金  額", 98);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "客戶ID", 3);
        buf = comcr.insertStr(buf, "客  戶  名  稱", 13);
        szTmp = String.format("%10.10s", tItem[intb - 2]);
        buf = comcr.insertStr(buf, szTmp, 36);
        buf = comcr.insertStr(buf, "處理情形", 48);
        buf = comcr.insertStr(buf, "收回狀況", 76);
        buf = comcr.insertStr(buf, "可全部收回金額", 87);
        buf = comcr.insertStr(buf, "可部份收回金額", 103);
        buf = comcr.insertStr(buf, "收回無望金額", 121);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printTotal() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "總筆數:", 1);
        szTmp = String.format("%6d", totalCnt);
        buf = comcr.insertStr(buf, szTmp, 12);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCbAmt);
        buf = comcr.insertStr(buf, szTmp, 87);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCiAmt);
        buf = comcr.insertStr(buf, szTmp, 103);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCcAmt);
        buf = comcr.insertStr(buf, szTmp, 119);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "總金額:", 1);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCbAmt + totalCiAmt + totalCcAmt);
        buf = comcr.insertStr(buf, szTmp, 119);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = String.format("%10.10s經/副理%20.20s襄理%18.18s會計%18.18s覆核%18.18s文件製作人", " ", " ", " ", " ", " ");
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB012 proc = new ColB012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
