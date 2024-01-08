/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/12/21  V1.00.00    phopho     program initial                          *
*  108/11/29  V1.00.01    phopho     fix err_rtn bug                          *
*  108/12/04  V1.00.02    phopho     from BRD: change args                    *
*  109/02/12  V1.00.03    phopho     Mantis 0002594: 列印表日設定為系統日期.   *
*  109/12/11  V1.00.04    shiyuqi       updated for project coding standard   *
******************************************************************************/

package Col;

import java.text.Normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB008 extends AccessDAO {
    private String progname = "催收款月報表處理程式   109/12/11  V1.00.04  ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "COL_B008R1";
    String rptDesc1 = "催收款月報表";
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
    int hTempType = 0;
    String hTempIdNo = "";
    String hTempIdPSeqno = "";
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
    int hMndaMcode = 0;
    double hMndaMcodeRate = 0;
    String hMndaLawsuitProcessLog = "";
    double hMndaLineOfCreditAmt = 0;
    String hMndaPayByStageFlag = "";
    double hMndaAcctJrnlBal = 0;
    String hTempProcMonth1 = "";
    String hCorpChiName = "";
    int negoCnt = 0;
    String hBusiBusinessDate = "";
    String hPrintName = "";
    String hRptName = "";
    String hMndaTransType = "";
    String hMndaRecourseMark = "";
    String currDate = "";
    String szTmp1                     = "";
    String dispDate = "";
    String sCreditActNo = "";
    String sId = "";
    String sName = "";
    String sTransDate = "";
    String sNego = "";
    String tempMonth = "";
    String temstr                     = "";
    String hTempPayByStageFlag = "";

    int pageCnt = 0, lineCnt = 0, totalCnt = 0, ttotalCnt = 0, indexCnt = 0, pageLine = 0;
    long pageAmt = 0, totalAmt = 0, ttotalAmt = 0;
    long pageCbAmt = 0, totalCbAmt = 0, ttotalCbAmt = 0;
    long pageCiAmt = 0, totalCiAmt = 0, ttotalCiAmt = 0;
    long pageCcAmt = 0, totalCcAmt = 0, ttotalCcAmt = 0;
    long pageC1Amt = 0, totalC1Amt = 0, ttotalC1Amt = 0;
    long pageC2Amt = 0, totalC2Amt = 0, ttotalC2Amt = 0;
    long pageC3Amt = 0, totalC3Amt = 0, ttotalC3Amt = 0;
    long nSAcctJrnlBal, nEAcctJrnlBal, nLineOfCreditAmt;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 3) {
//                comc.err_exit("Usage : ColB008 [month]", "              1.month : 催收款月份");  //依BRD需求修改argment
                showLogMessage("I", "", String.format("Usage : ColB008 [month [id_no] [seqno]]"));
                showLogMessage("I", "", String.format("               1.month : 催收款月份(yyyymm)"));
                showLogMessage("I", "", String.format("               2.id_no : 身份證字號"));
                comc.errExit("", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            //online call batch 時須記錄
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);

            selectPtrBusinday();
//            if ((args.length == 0) || ((args.length >= 1) && (args[0].length() > 6))) {
//                temp_month = comc.getSubString(h_busi_business_date,0,6);
//                temstr = comcr.get_businday(h_busi_business_date, -1);
//                if (!h_busi_business_date.equals(temstr)) {
//                	exceptExit = 0;
//                    comcr.err_rtn(String.format("本報表只能在每月最後營業日前一日執行"),
//                    		String.format("最後營業日前一日[%s]", temstr), h_call_batch_seqno);
//                }
//                h_temp_proc_month = temp_month;
            //BRD: 改以每月一日產生報表。
            if (args.length == 0) {  //批次執行: (無參數)
                tempMonth = comc.getSubString(hBusiBusinessDate,0,6);
                temstr = String.format("%2.2s", comc.getSubString(hBusiBusinessDate,6));
                if (!temstr.equals("01")) {
                	exceptExit = 0;
                	comcr.errRtn(String.format("本報表只能在每月一日執行 [%s]", hBusiBusinessDate),
                    		"", hCallBatchSeqno);
                }
                hTempProcMonth = tempMonth;
            } else {  //Online call 批次: 年月(必填) 身份證字號(選填)
//                szTmp = String.format("%6d", comcr.str2long(args[0]) + 191100);
                if (args[0].length() == 6) {
                    String sGArgs0 = "";
                    sGArgs0 = args[0];
                    sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                    szTmp = sGArgs0;
                }
                hTempProcMonth = szTmp;
                
                //BRD: add id_no
                if (args.length > 1) {
                	if (args[1].length() == 10) {
                		String sGArgs1 = "";
                        sGArgs1 = args[1];
                        sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
                        hTempIdNo = sGArgs1;
                	}
                }
                if (hTempIdNo.trim().equals("")==false)
                	hTempIdPSeqno = selectCrdIdno1(hTempIdNo);
            }

            checkOpen();

            selectProcMonth();
            showLogMessage("I", "", String.format("hMndaProcMonth : %s", hMndaProcMonth));
            selectPtrAcctType();

            comcr.insertPtrBatchRpt(lpar1);  //phopho add 問題單:0001125
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

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectProcMonth() throws Exception {
        hMndaProcMonth = "";

        sqlCmd = "select to_char(add_months(to_date(?,'yyyymm'),-1), 'yyyymm') h_mnda_proc_month ";
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
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
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
                pageC1Amt = totalC1Amt = 0;
                pageC2Amt = totalC2Amt = 0;
                pageC3Amt = totalC3Amt = 0;
                hTempType = 0;
                totalCnt = ttotalCnt;
                totalAmt = ttotalAmt;
                totalCbAmt = ttotalCbAmt;
                totalCiAmt = ttotalCiAmt;
                totalCcAmt = ttotalCcAmt;
                totalC1Amt = ttotalC1Amt;
                totalC2Amt = ttotalC2Amt;
                totalC3Amt = ttotalC3Amt;
                printHeader();
                printTotal();
            }
        }
        closeCursor(cursorIndex);
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
            pageC1Amt = totalC1Amt = 0;
            pageC2Amt = totalC2Amt = 0;
            pageC3Amt = totalC3Amt = 0;

            selectColMonthData0();
            ttotalCnt = ttotalCnt + totalCnt;
            ttotalAmt = ttotalAmt + totalAmt;
            ttotalCbAmt = ttotalCbAmt + totalCbAmt;
            ttotalCiAmt = ttotalCiAmt + totalCiAmt;
            ttotalCcAmt = ttotalCcAmt + totalCcAmt;
            ttotalC1Amt = ttotalC1Amt + totalC1Amt;
            ttotalC2Amt = ttotalC2Amt + totalC2Amt;
            ttotalC3Amt = ttotalC3Amt + totalC3Amt;

            if (totalCnt > 0) {
                printTotal();
                hTempCount++;
            }
        }
    }

    /***********************************************************************/
    void selectColMonthData0() throws Exception {
        int rInt, doFlag = 0;

        sqlCmd = "select ";
        sqlCmd += "p_seqno,";
        sqlCmd += "id_no ";
        sqlCmd += "from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and acct_type = decode(cast(? as integer),0,acct_type,cast(? as varchar(2))) ";
        sqlCmd += "and trans_type ='3' ";
        //phopho add
        if (hTempIdPSeqno.equals("")==false)
        	sqlCmd += "and id_p_seqno = ? ";
        //phopho add end
        sqlCmd += "UNION ";
        sqlCmd += "select p_seqno, ";
        sqlCmd += "id_no ";
        sqlCmd += "from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and acct_type = decode(cast(? as integer),0,acct_type,cast(? as varchar(2))) ";
        sqlCmd += "and trans_type ='3' ";
        //phopho add
        if (hTempIdPSeqno.equals("")==false)
        	sqlCmd += "and id_p_seqno = ? ";
        //phopho add end
        sqlCmd += "order by 2 ";
//        setString(1, h_mnda_proc_month);
//        setInt(2, h_temp_type);
//        setString(3, h_pacc_acct_type);
//        setString(4, h_temp_proc_month);
//        setInt(5, h_temp_type);
//        setString(6, h_pacc_acct_type);
        int cnt = 1;
        setString(cnt++, hMndaProcMonth);
        setInt(cnt++, hTempType);
        setString(cnt++, hPaccAcctType);
        if (hTempIdPSeqno.equals("")==false)
        	setString(cnt++, hTempIdPSeqno);
        setString(cnt++, hTempProcMonth);
        setInt(cnt++, hTempType);
        setString(cnt++, hPaccAcctType);
        if (hTempIdPSeqno.equals("")==false)
        	setString(cnt++, hTempIdPSeqno);
        
        extendField = "col_month_data_0.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMndaPSeqno = getValue("col_month_data_0.p_seqno", i);
            // h_mnda_id = getValue("id_no", i);
            doFlag = 0;
            sId = "";
            sName = "";
            sNego = "";
            nSAcctJrnlBal = 0;
            nEAcctJrnlBal = 0;

            selectColMonthData1();

            hTempProcMonth1 = hMndaProcMonth;
            rInt = selectColMonthData();
            if (rInt == 0) {
                if (hMndaCorpActFlag.equals("Y")) {
                    selectCrdCorp();
                    sId = String.format("%s", hMndaCorpNo);
                    sName = String.format("%s", hCorpChiName);
                } else {
                    selectCrdIdno();
                    sId = String.format("%s", hMndaId);
                    sName = String.format("%s", hIdnoChiName);
                }
                doFlag = 1;
                nSAcctJrnlBal = (long) ((hMndaAcctJrnlBal) * 1.0 + 0.5); /* 取消以仟元為單位 */
                sCreditActNo = String.format("%s", hMndaCreditActNo);
                sTransDate = String.format("%s", hMndaTransDate);
            } else {
                nSAcctJrnlBal = 0;
            }

            hTempProcMonth1 = hTempProcMonth;
            rInt = selectColMonthData();
            if (rInt == 0) {
                nEAcctJrnlBal = (long) ((hMndaAcctJrnlBal) + 0.5); /* 取消以仟元為單位 */
                nLineOfCreditAmt = (long) ((hMndaLineOfCreditAmt) + 0.5); /* 取消以仟元為單位 */
                hTempPayByStageFlag = hMndaPayByStageFlag;
                if (doFlag == 0) {
                    if (hMndaCorpActFlag.equals("Y")) {
                        selectCrdCorp();
                        sId = String.format("%s", hMndaCorpNo);
                        sName = String.format("%s", hCorpChiName);
                    } else {
                        selectCrdIdno();
                        sId = String.format("%s", hMndaId);
                        sName = String.format("%s", hIdnoChiName);
                    }
                    sCreditActNo = String.format("%s", hMndaCreditActNo);
                    sTransDate = String.format("%s", hMndaTransDate);
                }
            } else {
                nEAcctJrnlBal = 0;
            }
            if ((nSAcctJrnlBal == 0) && (nEAcctJrnlBal == 0))
                continue;

            if (indexCnt == 0)
                printHeader();

            if (nEAcctJrnlBal != 0) {
                if (selectColLiacNego() != 0)
                    sNego = String.format("NF");
                else if (selectColLiabNego() != 0)
                    sNego = String.format("NE");
                else if (hTempPayByStageFlag.length() != 0)
                    sNego = String.format("NW");
            }
            printDetail();
            if (indexCnt >= 24) {
                printFooter();
                indexCnt = 0;
            }

        }
        if (pageCnt != 0)
            printFooter();
    }

    /***********************************************************************/
    void selectColMonthData1() throws Exception {
        hMndaTransType = "";
        hMndaRecourseMark = "";

        sqlCmd = "select trans_type,";
        sqlCmd += "recourse_mark ";
        sqlCmd += " from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hTempProcMonth);
        setString(2, hMndaPSeqno);
        
        extendField = "col_month_data_1.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hMndaTransType = getValue("col_month_data_1.trans_type");
            hMndaRecourseMark = getValue("col_month_data_1.recourse_mark");
        } else
            hMndaTransType = "1";
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
        hMndaPayByStageFlag = "";
        hMndaLineOfCreditAmt = 0;
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
        sqlCmd += "substrb(lawsuit_process_log,1,30) h_mnda_lawsuit_process_log,";
        sqlCmd += "line_of_credit_amt,";
        sqlCmd += "pay_by_stage_flag,";
        sqlCmd += "billed_1_amt+unbill_1_amt+ billed_2_amt+unbill_2_amt+ billed_3_amt+unbill_3_amt h_mnda_acct_jrnl_bal ";
        sqlCmd += " from col_month_data ";
        sqlCmd += "where proc_month = ? ";
        sqlCmd += "and p_seqno = ?  ";
        sqlCmd += "and trans_type = '3' ";
        setString(1, hTempProcMonth1);
        setString(2, hMndaPSeqno);
        
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
            hMndaMcode = getValueInt("col_month_data.mcode");
            hMndaMcodeRate = getValueDouble("col_month_data.mcode_rate");
            hMndaLawsuitProcessLog = getValue("col_month_data.h_mnda_lawsuit_process_log");
            hMndaLineOfCreditAmt = getValueDouble("col_month_data.line_of_credit_amt");
            hMndaPayByStageFlag = getValue("col_month_data.pay_by_stage_flag");
            hMndaAcctJrnlBal = getValueDouble("col_month_data.h_mnda_acct_jrnl_bal");
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
    int selectColLiacNego() throws Exception {
        negoCnt = 0;

        sqlCmd = "select decode(count(*),0,0,1) nego_cnt ";
        sqlCmd += " from col_liac_nego ";
//        sqlCmd += "where id_no = ?  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and liac_status in ('1','2','3') ";
//        setString(1, h_mnda_id);
        setString(1, hMndaIdPSeqno);
        
        extendField = "col_liac_nego.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_col_liab_nego not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            negoCnt = getValueInt("col_liac_nego.nego_cnt");
        }

        return negoCnt;
    }

    /***********************************************************************/
    int selectColLiabNego() throws Exception {
        negoCnt = 0;

        sqlCmd = "select decode(count(*),0,0,1) nego_cnt ";
        sqlCmd += " from col_liab_nego ";
//        sqlCmd += "where id_no = ?  ";
        sqlCmd += "where id_p_seqno = ? ";
        sqlCmd += "and liab_status in ('3') ";
//        setString(1, h_mnda_id);
        setString(1, hMndaIdPSeqno);
        
        extendField = "col_liab_nego.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn("select_col_liac_nego not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            negoCnt = getValueInt("col_liab_nego.nego_cnt");
        }

        return negoCnt;
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        double nMcodeRate;

        lineCnt++;
        indexCnt++;

        buf = "";
        szTmp1 = String.format("%4.4s", sTransDate);
        szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1)-1911, 
        		comc.getSubString(sTransDate,4), comc.getSubString(sTransDate,6));
        buf = comcr.insertStr(buf, szTmp, 1);
        buf = comcr.insertStr(buf, sCreditActNo, 12);
        buf = comcr.insertStr(buf, sId, 21);
        buf = comcr.insertStr(buf, sName, 34);
        szTmp = comcr.commFormat("2$,3$,3$,3$", nLineOfCreditAmt);
        buf = comcr.insertStr(buf, szTmp, 60);
        buf = comcr.insertStr(buf, hCorpBusinessCode, 78);
        buf = comcr.insertStr(buf, sNego, 102);
        buf = comcr.insertStr(buf, hMndaLawsuitProcessLog, 106);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        szTmp = comcr.commFormat("3$,3$,3$", nSAcctJrnlBal);
        buf = comcr.insertStr(buf, szTmp, 7);
        szTmp = comcr.commFormat("3$,3$,3$", nEAcctJrnlBal - nSAcctJrnlBal);
        buf = comcr.insertStr(buf, szTmp, 29);
        szTmp = comcr.commFormat("3$,3$,3$", nEAcctJrnlBal);
        buf = comcr.insertStr(buf, szTmp, 51);
        if (nEAcctJrnlBal > 0) {
            nMcodeRate = hMndaMcodeRate;

            szTmp = comcr.commFormat("3$,3$,3$", nEAcctJrnlBal);

            if (nMcodeRate == 0) {
                buf = comcr.insertStr(buf, szTmp, 75);
                pageC1Amt = pageC1Amt + nEAcctJrnlBal;
                totalC1Amt = totalC1Amt + nEAcctJrnlBal;
            }
            if ((nMcodeRate != 100) && (nMcodeRate != 0)) {
                buf = comcr.insertStr(buf, szTmp, 88);
                szTmp = String.format("%3.0f %s", nMcodeRate, "%");
                buf = comcr.insertStr(buf, szTmp, 100);
                pageC2Amt = pageC2Amt + nEAcctJrnlBal;
                totalC2Amt = totalC2Amt + nEAcctJrnlBal;
            }
            if (nMcodeRate == 100) {
                szTmp = comcr.commFormat("3$,3$,3$", nEAcctJrnlBal);
                buf = comcr.insertStr(buf, szTmp, 107);
                pageC3Amt = pageC3Amt + nEAcctJrnlBal;
                totalC3Amt = totalC3Amt + nEAcctJrnlBal;
            }
        }
        if (comc.getSubString(hMndaTransType,0,1).equals("1"))
            buf = comcr.insertStr(buf, "結清", 119);
        if (comc.getSubString(hMndaTransType,0,1).equals("2"))
            buf = comcr.insertStr(buf, "逾放戶", 119);
        if ((comc.getSubString(hMndaTransType,0,1).equals("4")) && (comc.getSubString(hMndaRecourseMark,0,1).equals("N")))
            buf = comcr.insertStr(buf, "轉呆帳", 119);
        if ((comc.getSubString(hMndaTransType,0,1).equals("4")) && (comc.getSubString(hMndaRecourseMark,0,1).equals("Y")))
            buf = comcr.insertStr(buf, "轉追索債權", 119);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        pageCnt++;
        totalCnt++;

        pageCbAmt = pageCbAmt + nSAcctJrnlBal;
        pageCiAmt = pageCiAmt + nEAcctJrnlBal - nSAcctJrnlBal;
        pageCcAmt = pageCcAmt + nEAcctJrnlBal;

        totalCbAmt = totalCbAmt + nSAcctJrnlBal;
        totalCiAmt = totalCiAmt + nEAcctJrnlBal - nSAcctJrnlBal;
        totalCcAmt = totalCcAmt + nEAcctJrnlBal;
    }

    /***********************************************************************/
    void printFooter() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "筆數:", 1);
        szTmp = String.format("%6d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 12);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageCbAmt);
        buf = comcr.insertStr(buf, szTmp, 4);
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageCiAmt);
        buf = comcr.insertStr(buf, szTmp, 26);
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageCcAmt);
        buf = comcr.insertStr(buf, szTmp, 48);
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageC1Amt);
        buf = comcr.insertStr(buf, szTmp, 72);
        szTmp = comcr.commFormat("1$,3$,3$,3$", pageC2Amt);
        buf = comcr.insertStr(buf, szTmp, 86);
        szTmp = comcr.commFormat("2$,3$,3$,3$", pageC3Amt);
        buf = comcr.insertStr(buf, szTmp, 104);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        pageCnt = 0;
        pageCbAmt = 0;
        pageCiAmt = 0;
        pageCcAmt = 0;
        pageC1Amt = 0;
        pageC2Amt = 0;
        pageC3Amt = 0;
        pageAmt = 0;
    }

    /***********************************************************************/
    void printHeader() throws Exception {
        pageLine++;

        buf = "";
        buf = comcr.insertStr(buf, "COL_B008R1", 1);
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
        buf = comcr.insertStr(buf, "帳戶類別:", 1);
        if (hTempType != 0) {
            szTmp = String.format("%2.2s %s", hPaccAcctType, hPaccChinName);
        } else {
            szTmp = String.format("%s 合計", hPcceCurrChiName);
        }
        buf = comcr.insertStr(buf, szTmp, 10);
        buf = comcr.insertStrCenter(buf, "催 收 款 月 報 表", 132);
        buf = comcr.insertStr(buf, "列印頁數 :", 110);
        szTmp = String.format("%4d", pageLine);
        buf = comcr.insertStr(buf, szTmp, 125);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "核決層級:營業單位", 1);
        szTmp = String.format("%03.0f年%2.2s月份", (comcr.str2long(hTempProcMonth) - 191100) / 100.0,
                comc.getSubString(hTempProcMonth,4));
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        szTmp = String.format("貨幣單位 : %s 元", hPcceCurrChiName);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "不良放款性質:催收款      原放款科目:信用卡", 1);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "轉    列", 1);
        buf = comcr.insertStr(buf, "授信帳號", 11);
        buf = comcr.insertStr(buf, "客戶ID", 22);
        buf = comcr.insertStr(buf, "客  戶  名  稱", 33);
        buf = comcr.insertStr(buf, "原放款額度", 64);
        buf = comcr.insertStr(buf, "行業別", 76);
        buf = comcr.insertStr(buf, "註記", 101);
        buf = comcr.insertStr(buf, "處理情形及催收計劃", 106);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "催收日期", 1);
        buf = comcr.insertStr(buf, "上月底餘額", 14);
        buf = comcr.insertStr(buf, "本月變動餘額", 35);
        buf = comcr.insertStr(buf, "本月底餘額", 58);
        buf = comcr.insertStr(buf, "可全部收回", 76);
        buf = comcr.insertStr(buf, "可部份收回", 91);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "本    金", 10);
        buf = comcr.insertStr(buf, "利  息", 22);
        buf = comcr.insertStr(buf, "本    金", 32);
        buf = comcr.insertStr(buf, "利  息", 44);
        buf = comcr.insertStr(buf, "本    金", 54);
        buf = comcr.insertStr(buf, "利  息", 66);
        buf = comcr.insertStr(buf, "金      額", 76);
        buf = comcr.insertStr(buf, "預估損失金額", 87);
        buf = comcr.insertStr(buf, "比率", 101);
        buf = comcr.insertStr(buf, "收回無望金額", 106);
        buf = comcr.insertStr(buf, "本月狀況", 119);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printTotal() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "總筆數:", 1);
        szTmp = String.format("%6d", totalCnt);
        buf = comcr.insertStr(buf, szTmp, 12);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCbAmt);
        buf = comcr.insertStr(buf, szTmp, 4);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCiAmt);
        buf = comcr.insertStr(buf, szTmp, 26);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCcAmt);
        buf = comcr.insertStr(buf, szTmp, 48);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalC1Amt);
        buf = comcr.insertStr(buf, szTmp, 72);
        szTmp = comcr.commFormat("1$,3$,3$,3$", totalC2Amt);
        buf = comcr.insertStr(buf, szTmp, 86);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalC3Amt);
        buf = comcr.insertStr(buf, szTmp, 104);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = String.format("%10.10s經/副理%20.20s襄理%18.18s會計%18.18s覆核%18.18s文件製作人", " ", " ", " ", " ", " ");
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        temstr = String.format("%s/reports/COL_B008_%s", comc.getECSHOME(), hTempProcMonth);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("報表名稱 : %s", temstr));

    }
    
    /***********************************************************************/
    String selectCrdIdno1(String asIdNo) throws Exception {
    	String outIdPSeqno = asIdNo;
        sqlCmd = "select id_p_seqno from crd_idno where id_no = ? ";
        setString(1, asIdNo);
        
        extendField = "crd_idno_1.";
        
        if (selectTable() > 0) {
        	outIdPSeqno = getValue("crd_idno_1.id_p_seqno");
        }

        return outIdPSeqno;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB008 proc = new ColB008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
