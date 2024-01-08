/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/12/21  V1.00.00    phopho     program initial                          *
*  108/12/04  V1.00.02    phopho     fix substring error                      *
*  109/12/14  V1.00.02    shiyuqi    updated for project coding standard      *
*  112/01/02  V1.00.03    sunny      同步程式(Mantis 0002594)，列印表日設定為系統日期  *
******************************************************************************/

package Col;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class ColB031 extends AccessDAO {
    private String progname = "強停報告表列印處理程式  112/01/02  V1.00.03";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptName1 = "COL_B031R1";
    String rptDesc1 = "逾期強停報表";
    int    rptSeq1            = 0;
    String buf                = "";
    String szTmp              = "";
    String hCallBatchSeqno = "";

    String hCoscStaticMode = "";
    String hPaccAcctType = "";
    String hTempAcctMonth = "";
    String hCoscAcctMonth = "";
    String hCoscOppostId = "";
    String hCoscOppostPSeqno = "";
    String hCoscCardIndicator = "";
    String hCoscIssueDate = "";
    String hCoscOppostDate = "";
    double hCoscAcctJrnlBal = 0;
    double hCoscAcctJrnlBal2 = 0;
    double hCoscHisPayAmt = 0;
    String hBusiBusinessDate = "";
    String hPaccCurrCode = "";
    String hPcceCurrChiName = "";
    int hTempCount = 0;
    String hPaccChinName = "";
    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String currDate = "";
    String szTmp1                = "";
    String dispDate = "";

    int lineCnt = 0, indexCnt = 0, pageLine = 0, inta1;
    int pageCnt = 0, totalCnt = 0, ttotalCnt = 0;
    int page1Cnt = 0, total1Cnt = 0, ttotal1Cnt = 0;
    int page2Cnt = 0, total2Cnt = 0, ttotal2Cnt = 0;
    double pageAmt = 0, totalAmt = 0, ttotalAmt = 0;
    double page1Amt = 0, total1Amt = 0, ttotal1Amt = 0;
    double page2Amt = 0, total2Amt = 0, ttotal2Amt = 0;
    double pageCbAmt = 0, totalCbAmt = 0, ttotalCbAmt = 0;
    double page1CbAmt = 0, total1CbAmt = 0, ttotal1CbAmt = 0;
    double page2CbAmt = 0, total2CbAmt = 0, ttotal2CbAmt = 0;
    double pageCcAmt = 0, totalCcAmt = 0, ttotalCcAmt = 0;
    double page1CcAmt = 0, total1CcAmt = 0, ttotal1CcAmt = 0;
    double page2CcAmt = 0, total2CcAmt = 0, ttotal2CcAmt = 0;
    String   temstr             = "";
    String tmpPar1 = "";
    String tmpPar2 = "";
    String tmpPar3 = "";
    String[] seasonSMonth = { "01", "04", "07", "10" };
    String[] seasonEMonth = { "03", "06", "09", "12" };
    int      intaa, printTag = 0;
    int hTempType = 0;
    String hCoscStaticType = "";
    String   filename           = "";

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 2 && args.length != 3 && args.length != 4) {
                showLogMessage("I", "", String.format("Usage : ColB031 "));
                showLogMessage("I", "", String.format("                 1.月報表 : 1 yyyymm"));
                showLogMessage("I", "", String.format("                 2.季報表 : 2 yyyy season"));
                showLogMessage("I", "", String.format("                 3.年報表 : 3 yyyy"));
                comc.errExit("", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            selectPtrBusinday();

            hCoscStaticType = args[0];
            tmpPar1 = args[0];
            tmpPar2 = args[1];
            if (args[0].equals("1")) {
//                temstr = String.format("%6d", comcr.str2long(args[1]) + 191100);
//                h_cosc_acct_month = temstr;
//                h_temp_acct_month = temstr;
                hCoscAcctMonth = args[1];
                hTempAcctMonth = args[1];
                if (args.length > 2)
                    printTag = 1;
            }
            if (args[0].equals("2")) {
            	tmpPar3 = args[2];
//                temstr = String.format("%4d%2.2s", comcr.str2long(args[1]) + 1911,
//                        season_s_month[comc.str2int(args[2]) - 1]);
                temstr = String.format("%4.4s%2.2s", args[1],
                        seasonSMonth[comc.str2int(args[2]) - 1]);
                hCoscAcctMonth = temstr;
//                temstr = String.format("%4d%2.2s", comcr.str2long(args[1]) + 1911,
//                        season_e_month[comc.str2int(args[2]) - 1]);
                temstr = String.format("%4.4s%2.2s", args[1],
                		seasonEMonth[comc.str2int(args[2]) - 1]);
                hTempAcctMonth = temstr;
                if (args.length > 3)
                    printTag = 1;
            }
            if (args[0].equals("3")) {
//                temstr = String.format("%4d01", comcr.str2long(args[1]) + 1911);
                temstr = String.format("%4.4s01", args[1]);
                hCoscAcctMonth = temstr;
//                temstr = String.format("%4d12", comcr.str2long(args[1]) + 1911);
                temstr = String.format("%4.4s12", args[1]);
                hTempAcctMonth = temstr;
                if (args.length > 2)
                    printTag = 1;
            }
            if (tmpPar3.length() > 0) tmpPar2 += "_"+ tmpPar3;
            
            for (inta1 = 0; inta1 <= 2; inta1++) {
                hTempType = 0;
                lineCnt = indexCnt = pageLine = 0;
                pageCnt = totalCnt = ttotalCnt = 0;
                page1Cnt = total1Cnt = ttotal1Cnt = 0;
                page2Cnt = total2Cnt = ttotal2Cnt = 0;
                pageAmt = totalAmt = ttotalAmt = 0;
                page1Amt = total1Amt = ttotal1Amt = 0;
                page2Amt = total2Amt = ttotal2Amt = 0;
                pageCbAmt = totalCbAmt = ttotalCbAmt = 0;
                page1CbAmt = total1CbAmt = ttotal1CbAmt = 0;
                page2CbAmt = total2CbAmt = ttotal2CbAmt = 0;
                pageCcAmt = totalCcAmt = ttotalCcAmt = 0;
                page1CcAmt = total1CcAmt = ttotal1CcAmt = 0;
                page2CcAmt = total2CcAmt = ttotal2CcAmt = 0;
                checkOpen();
                selectPtrAcctType(args.length, args);
                
                comcr.insertPtrBatchRpt(lpar1);  //phopho add 2019.4.23 問題單:0001128
                comc.writeReport(filename, lpar1);
                lpar1.clear();
                buf = "";
                if (printTag == 1)
                    comcr.lpRtn("COL_D_VOUCH", "");
                TimeUnit.SECONDS.sleep(1);  //phopho add 2019.4.23
            }

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
        hTempAcctMonth = "";

        sqlCmd = "select business_date,";
        sqlCmd += "to_char(add_months(to_date(business_date,'yyyymmdd'),-1),'yyyymm') h_temp_acct_month ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempAcctMonth = getValue("h_temp_acct_month");
        }
    }

    /***********************************************************************/
    void selectPtrAcctType(int argc, String args[]) throws Exception {
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

            ttotalCnt = 0;

            selectPtrAcctType1(argc, args);
            if (hTempCount > 0) {
                printTtotal();
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    void selectPtrAcctType1(int argc, String args[]) throws Exception {
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
            hPaccAcctType = getValue("ptr_acct_type_1.acct_type",i);
            hPaccChinName = getValue("ptr_acct_type_1.chin_name",i);

            lineCnt = indexCnt = 0;
            pageCnt = totalCnt = 0;
            page1Cnt = total1Cnt = 0;
            page2Cnt = total2Cnt = 0;
            pageCbAmt = totalCbAmt = 0;
            page1CbAmt = total1CbAmt = 0;
            page2CbAmt = total2CbAmt = 0;
            pageCcAmt = totalCcAmt = 0;
            page1CcAmt = total1CcAmt = 0;
            page2CcAmt = total2CcAmt = 0;

            indexCnt = lineCnt = 0;
            temstr = String.format("%d", inta1);
            hCoscStaticMode = temstr;
            selectColOppostStatic();

            ttotalCnt = ttotalCnt + totalCnt;
            ttotal1Cnt = ttotal1Cnt + total1Cnt;
            ttotal2Cnt = ttotal2Cnt + total2Cnt;
            ttotalCbAmt = ttotalCbAmt + totalCbAmt;
            ttotal1CbAmt = ttotal1CbAmt + total1CbAmt;
            ttotal2CbAmt = ttotal2CbAmt + total2CbAmt;
            ttotalCcAmt = ttotalCcAmt + totalCcAmt;
            ttotal1CcAmt = ttotal1CcAmt + total1CcAmt;
            ttotal2CcAmt = ttotal2CcAmt + total2CcAmt;

            if (totalCnt > 0) {
                printTotal();
                hTempCount++;
            }
        }
    }

    /***********************************************************************/
    void selectColOppostStatic() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "oppost_id,";
        sqlCmd += "oppost_p_seqno,";
        sqlCmd += "card_indicator,";
        sqlCmd += "issue_date,";
        sqlCmd += "oppost_date,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "acct_jrnl_bal2,";
        sqlCmd += "his_pay_amt ";
        sqlCmd += "from col_oppost_static ";
        sqlCmd += "where static_type = '1' ";
        sqlCmd += "and ((? = '0' and his_pay_amt = 0) ";
        sqlCmd += " or (? != '0' and his_pay_amt != 0 and static_mode = ?)) ";
        sqlCmd += "and acct_type = ? ";
        sqlCmd += "and acct_month <= ? ";
        sqlCmd += "and acct_month >= ? ";
        sqlCmd += "and issue_date >= to_char(add_months(to_date(?,'yyyymm'),-12),'yyyymmdd') ";
        sqlCmd += "order by oppost_date asc,oppost_id asc ";
        setString(1, hCoscStaticMode);
        setString(2, hCoscStaticMode);
        setString(3, hCoscStaticMode);
        setString(4, hPaccAcctType);
        setString(5, hTempAcctMonth);
        setString(6, hCoscAcctMonth);
        setString(7, hCoscAcctMonth);
        
        extendField = "col_oppost_static.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCoscOppostId = getValue("col_oppost_static.oppost_id", i);
            hCoscOppostPSeqno = getValue("col_oppost_static.oppost_p_seqno", i);
            hCoscCardIndicator = getValue("col_oppost_static.card_indicator", i);
            hCoscIssueDate = getValue("col_oppost_static.issue_date", i);
            hCoscOppostDate = getValue("col_oppost_static.oppost_date", i);
            hCoscAcctJrnlBal = getValueDouble("col_oppost_static.acct_jrnl_bal", i);
            hCoscAcctJrnlBal2 = getValueDouble("col_oppost_static.acct_jrnl_bal2", i);
            hCoscHisPayAmt = getValueDouble("col_oppost_static.his_pay_amt", i);

            if (indexCnt == 0)
                printHeader();
            if (hCoscCardIndicator.equals("1")) {
                selectCrdIdno();
            } else {
                selectCrdcorp();
            }
            printDetail();
            if (indexCnt >= 50) {
                indexCnt = 0;
            }
        }
    }

    /***********************************************************************/
    void printHeader() throws Exception {
    	//phopho mod//
    	if (comc.getSubString(hCoscStaticType,0,1).equals("1")) {
            if (comc.getSubString(hCoscStaticMode,0,1).equals("0"))
                temstr = String.format("從未繳款報告表");
            if (comc.getSubString(hCoscStaticMode,0,1).equals("1"))
                temstr = String.format("有繳款且發卡六個月(含)內即強停報告表");
            if (comc.getSubString(hCoscStaticMode,0,1).equals("2"))
                temstr = String.format("有繳款且發卡六個月(含)以上, 十二個月(不含)以下即強停報告表");
        }
        if (comc.getSubString(hCoscStaticType,0,1).equals("2")) {
            if (comc.getSubString(hCoscStaticMode,0,1).equals("0"))
                temstr = String.format("從未繳款(季)報告表");
            if (comc.getSubString(hCoscStaticMode,0,1).equals("1"))
                temstr = String.format("有繳款且發卡六個月內強停欠款(季)報告表");
            if (comc.getSubString(hCoscStaticMode,0,1).equals("2"))
                temstr = String.format("有繳款且發卡7-12月內強停欠款(季)報告表");
        }
        if (comc.getSubString(hCoscStaticType,0,1).equals("3")) {
            if (comc.getSubString(hCoscStaticMode,0,1).equals("0"))
                temstr = String.format("從未繳款(年)報告表");
            if (comc.getSubString(hCoscStaticMode,0,1).equals("1"))
                temstr = String.format("有繳款且發卡六個月內強停欠款(年)報告表");
            if (comc.getSubString(hCoscStaticMode,0,1).equals("2"))
                temstr = String.format("有繳款且發卡7-12月內強停欠款(年)報告表");
        }
        rptDesc1 = temstr;  //phopho add set report name
    	//phopho mod//
    	
        pageLine++;
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "COL_B031R1", 1);
        szTmp = comcr.bankName;
        buf = comcr.insertStrCenter(buf, szTmp, 132);
        buf = comcr.insertStr(buf, "列印表日 :", 110);
        dispDate = comc.convDates(sysDate, 1);
        // conv_date('1',curr_date,szTmp1,szTmp,szTmp);
        
        //Mantis 0002594:
        //1. 目前現行系統（舊系統）是「執行批次日期+1天」為報表之「列印表日」。
        //2. 新系統可以執行線上批次，造成日期差一天的疑慮。
        //3. 現依據user提出，將「列印表日」修改為執行批次之日期。
        dispDate = sysDate; 
        
        buf = comcr.insertStr(buf, dispDate, 121);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
//        if (h_cosc_static_type.substring(0, 1).equals("1")) {
//            if (h_cosc_static_mode.substring(0, 1).equals("0"))
//                temstr = String.format("從未繳款報告表");
//            if (h_cosc_static_mode.substring(0, 1).equals("1"))
//                temstr = String.format("有繳款且發卡六個月(含)內即強停報告表");
//            if (h_cosc_static_mode.substring(0, 1).equals("2"))
//                temstr = String.format("有繳款且發卡六個月(含)以上, 十二個月(不含)以下即強停報告表");
//        }
//        if (h_cosc_static_type.substring(0, 1).equals("2")) {
//            if (h_cosc_static_mode.substring(0, 1).equals("0"))
//                temstr = String.format("從未繳款(季)報告表");
//            if (h_cosc_static_mode.substring(0, 1).equals("1"))
//                temstr = String.format("有繳款且發卡六個月內強停欠款(季)報告表");
//            if (h_cosc_static_mode.substring(0, 1).equals("2"))
//                temstr = String.format("有繳款且發卡7-12月內強停欠款(季)報告表");
//        }
//        if (h_cosc_static_type.substring(0, 1).equals("3")) {
//            if (h_cosc_static_mode.substring(0, 1).equals("0"))
//                temstr = String.format("從未繳款(年)報告表");
//            if (h_cosc_static_mode.substring(0, 1).equals("1"))
//                temstr = String.format("有繳款且發卡六個月內強停欠款(年)報告表");
//            if (h_cosc_static_mode.substring(0, 1).equals("2"))
//                temstr = String.format("有繳款且發卡7-12月內強停欠款(年)報告表");
//        }
        buf = comcr.insertStrCenter(buf, temstr, 132);
        buf = comcr.insertStr(buf, "列印頁數 :", 110);
        szTmp = String.format("%4d", pageLine);
        buf = comcr.insertStr(buf, szTmp, 125);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "帳戶類別:", 1);
        if (hTempType != 0) {
            szTmp = String.format("%2.2s %s", hPaccAcctType, hPaccChinName);
        }

        buf = comcr.insertStr(buf, szTmp, 10);
        if (comc.getSubString(hCoscStaticType,0,1).equals("1"))
//            szTmp = String.format("%03.0f年%2.2s月份", (comcr.str2long(h_cosc_acct_month) - 191100) / 100.0,
//                    h_cosc_acct_month.substring(4));
        	szTmp = String.format("%4.4s年%2.2s月份", hCoscAcctMonth, comc.getSubString(hCoscAcctMonth,4));
        if (comc.getSubString(hCoscStaticType,0,1).equals("2"))
//            szTmp = String.format("%03.0f年%2.2s月~%03.0f年%2.2s月", (comcr.str2long(h_cosc_acct_month) - 191100) / 100.0,
//                    h_cosc_acct_month.substring(4), (comcr.str2long(h_cosc_acct_month) - 191100) / 100.0,
//                    h_temp_acct_month.substring(4));
        	szTmp = String.format("%4.4s年%2.2s月 ~ %4.4s年%2.2s月", hCoscAcctMonth,
        			comc.getSubString(hCoscAcctMonth,4), hTempAcctMonth, comc.getSubString(hTempAcctMonth,4));
        if (comc.getSubString(hCoscStaticType,0,1).equals("3"))
//            szTmp = String.format("%03.0f年度", (comcr.str2long(h_cosc_acct_month) - 191100) / 100.0);
        	szTmp = String.format("%4.4s年度", hCoscAcctMonth);

        buf = comcr.insertStrCenter(buf, szTmp, 132);
        szTmp = String.format("貨幣單位 : %s 元", hPcceCurrChiName);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "身分証號/ 統編", 1);
        buf = comcr.insertStr(buf, "帳戶類別", 17);
        buf = comcr.insertStr(buf, "客   戶   名   稱", 43);
        buf = comcr.insertStr(buf, "發 卡 日", 67);
        buf = comcr.insertStr(buf, "強 停 日", 81);
        buf = comcr.insertStr(buf, "期 初 餘 額", 96);
        buf = comcr.insertStr(buf, "同一ID欠款總金額", 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";

        sqlCmd = "select uf_hi_cname(chi_name) h_idno_chi_name ";
        sqlCmd += " from crd_idno ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hCoscOppostPSeqno);
        
        extendField = "crd_idno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("crd_idno.h_idno_chi_name");
        }
    }

    /***********************************************************************/
    void selectCrdcorp() throws Exception {
        hIdnoChiName = "";

        sqlCmd = "select chi_name ";
        sqlCmd += " from crd_corp ";
        sqlCmd += "where corp_p_seqno = ? ";
        setString(1, hCoscOppostPSeqno);
        
        extendField = "crd_corp.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_corp not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("crd_corp.chi_name");
        }
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, hCoscOppostId, 1);
        buf = comcr.insertStr(buf, hPaccAcctType, 17);
        buf = comcr.insertStr(buf, hIdnoChiName, 45);
//        szTmp1 = String.format("%4.4s", h_cosc_issue_date);
//        szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1)-1911, h_cosc_issue_date.substring(4,6), h_cosc_issue_date.substring(6,8));
        szTmp = String.format("%4.4s/%2.2s/%2.2s", hCoscIssueDate, comc.getSubString(hCoscIssueDate,4), comc.getSubString(hCoscIssueDate,6));
        buf = comcr.insertStr(buf, szTmp, 67);
//        szTmp1 = String.format("%4.4s", h_cosc_oppost_date);
//        szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1)-1911, h_cosc_oppost_date.substring(4,6), h_cosc_oppost_date.substring(6,8));
        szTmp = String.format("%4.4s/%2.2s/%2.2s", hCoscOppostDate, comc.getSubString(hCoscOppostDate,4), comc.getSubString(hCoscOppostDate,6));
        buf = comcr.insertStr(buf, szTmp, 81);
        szTmp = comcr.commFormat("2$,3$,3$,3$", hCoscAcctJrnlBal);
        buf = comcr.insertStr(buf, szTmp, 95);
        szTmp = comcr.commFormat("2$,3$,3$,3$", hCoscAcctJrnlBal2);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        page2Cnt++;
        total2Cnt++;
        page2CbAmt = page2CbAmt + hCoscAcctJrnlBal;
        page2CcAmt = page2CcAmt + hCoscAcctJrnlBal2;
        total2CbAmt = total2CbAmt + hCoscAcctJrnlBal;
        total2CcAmt = total2CcAmt + hCoscAcctJrnlBal2;
        pageCnt++;
        totalCnt++;

        pageCbAmt = pageCbAmt + hCoscAcctJrnlBal;
        pageCcAmt = pageCcAmt + hCoscAcctJrnlBal2;

        totalCbAmt = totalCbAmt + hCoscAcctJrnlBal;
        totalCcAmt = totalCcAmt + hCoscAcctJrnlBal2;
    }

    /***********************************************************************/
    void printTotal() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "總戶數及欠款總額 :", 1);
        szTmp = String.format("%6d", totalCnt);
        buf = comcr.insertStr(buf, szTmp, 28);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCbAmt);
        buf = comcr.insertStr(buf, szTmp, 95);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCcAmt);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        totalCnt = 0;
        total1Cnt = 0;
        total2Cnt = 0;
        totalCbAmt = 0;
        total1CbAmt = 0;
        total2CbAmt = 0;
        totalCcAmt = 0;
        total1CcAmt = 0;
        total2CcAmt = 0;
    }

    /***********************************************************************/
    void printTtotal() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "帳戶類別之加總 :", 1);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "未繳款戶數及欠款總額 :", 1);
        szTmp = String.format("%6d", ttotal1Cnt);
        buf = comcr.insertStr(buf, szTmp, 28);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotal1CbAmt);
        buf = comcr.insertStr(buf, szTmp, 95);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotal1CcAmt);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "除未繳款戶數及欠款總額 :", 1);
        szTmp = String.format("%6d", ttotal2Cnt);
        buf = comcr.insertStr(buf, szTmp, 28);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotal2CbAmt);
        buf = comcr.insertStr(buf, szTmp, 95);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotal2CcAmt);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "總戶數及欠款總額 :", 1);
        szTmp = String.format("%6d", ttotalCnt);
        buf = comcr.insertStr(buf, szTmp, 28);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotalCbAmt);
        buf = comcr.insertStr(buf, szTmp, 95);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotalCcAmt);
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = String.format("%10.10s經/副理%20.20s襄理%18.18s會計%18.18s覆核%18.18s文件製作人", " ", " ", " ", " ", " ");
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
//        filename = String.format("%s/reports/COL_B031_%d_%s_%s", comc.GetECSHOME(), inta1, tmp_par1, h_cosc_acct_month);
        filename = String.format("%s/reports/COL_B031_%d_%s_%s", comc.getECSHOME(), inta1, tmpPar1, tmpPar2);
        filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);

        showLogMessage("I", "", String.format("報表名稱 : %s", filename));
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB031 proc = new ColB031();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
