/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/09/28  V1.00.00    phopho     program initial                          *
*  109/12/10  V1.00.01    shiyuqi       updated for project coding standard   *
*  112/04/12  V1.00.02    Ryan       報表修正                                                                                                   *
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

/*未有強停即辦理前置協商報表處理程式*/
public class ColA490 extends AccessDAO {
    private String progname = "未有強停即辦理前置協商報表處理程式 112/04/12  V1.00.02  ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String rptName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";
    String hCallBatchSeqno = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    String hCurpModWs = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String hCallRProgramCode = "";

    String hBusiBusinessDate = "";
    String hTempApplyDate = "";
    String hTempLastDate = "";
    String hPaccCurrCode = "";
    String hPcceCurrChiName = "";
    int hTempCount = 0;
    String hPaccAcctType = "";
    String hPaccChinName = "";
    int hTempType = 0;
    String hClolId = "";
    String hClolIdPSeqno = "";
    String hClolAcctType = "";
    String hClolIssueDate = "";
    String hClolApplyDate = "";
    long hClolAcctJrnlBal = 0;
    long hClolAcctJrnlBal2 = 0;
    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String filename= "";

    String currDate = "";
    String szTmp1 = "";
    String dispDate = "";
    String rptDesc1 = "";
    int intaa = 0;
    int indexCnt = 0;
    int totalCnt = 0;
    int pageLine = 0;
    int ttotalCnt = 0;
    int lineCnt = 0;
    long totalCbAmt = 0;
    long totalCcAmt = 0;
    long ttotalCbAmt = 0;
    long ttotalCcAmt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length !=  0 && args.length != 1) {
                comc.errExit("Usage : ColA490 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if ((args.length == 1) && (args[0].length() == 8)) {
                hBusiBusinessDate = args[0];
                intaa = 1;
            }
            selectPtrBusinday();

            if (!hBusiBusinessDate.substring(6, 8).equals("01")) {
            	exceptExit = 0;
                comcr.errRtn(String.format("本程式設定於每月01日執行!!本日[%s]", hBusiBusinessDate), "", hCallBatchSeqno);
            }

            for (int inta1 = 0; inta1 <= 2; inta1++) {
                hTempType = inta1;
                indexCnt = pageLine = 0;
                totalCnt = ttotalCnt = 0;
                totalCbAmt = ttotalCbAmt = 0;
                totalCcAmt = ttotalCcAmt = 0;
                
                lpar1 = new ArrayList<Map<String, Object>>();
                
                checkOpen();
                selectPtrAcctType();

                comc.writeReport(filename, lpar1);
                
	            if(lpar1.size()>0){
	       		 comcr.insertPtrBatchRpt(lpar1); /* 寫入ptr_batch_rpt online報表 */
	        	}
	        	lpar1.clear();
 
                // if (ttotal_cnt>0) lp_rtn("COL_A490");
            }

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
        hTempApplyDate = "";
        hTempLastDate = "";
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date,";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))),'yyyymmdd'),-1),'yyyymmdd') h_temp_apply_date,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))), 'yyyymmdd')-1 days,'yyyymmdd') h_temp_last_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempApplyDate = getValue("h_temp_apply_date");
            hTempLastDate = getValue("h_temp_last_date");
        }
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        filename = String.format("%s/reports/COL_A490_%d_%8.8s_%8.8s", comc.getECSHOME(), hTempType,
                hTempApplyDate, hTempLastDate);
        filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
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

            hTempCount = 0;
            ttotalCnt = 0;

            selectPtrAcctType1();
            if (hTempCount > 0) {
                printTtotal();                
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    void printTtotal() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "帳戶類別之加總 :", 1);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "總戶數及欠款總額 :", 1);
        szTmp = String.format("%6d", ttotalCnt);
        buf = comcr.insertStr(buf, szTmp, 28);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotalCbAmt);
        buf = comcr.insertStr(buf, szTmp, 82);
        szTmp = comcr.commFormat("2$,3$,3$,3$", ttotalCcAmt);
        buf = comcr.insertStr(buf, szTmp, 103);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = String.format("%8.8s經/副理:%17.17s襄理:%17.17s會計:%17.17s覆核:%17.17s文件製作人:\n", " ", " ", " ", " ", " ");
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
    	
//    	if(lpar1.size()>0){
//   		comcr.insertPtrBatchRpt(lpar1); /* 寫入ptr_batch_rpt online報表 */
//    	}
//    	lpar1.clear();

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
            
            //LogMessage("I", ""," curr_code = " + hPaccCurrCode +" , AcctType = " + hPaccAcctType +" ,ChinName = " + hPaccChinName);

            indexCnt = 0;
            totalCnt = 0;
            
            selectColLiacOppdtl();
 
            ttotalCnt = ttotalCnt + totalCnt;
            ttotalCbAmt = ttotalCbAmt + totalCbAmt;
            ttotalCcAmt = ttotalCcAmt + totalCcAmt;
 
            if (totalCnt > 0) {
                printTotal();

                hTempCount++;
            }
        }
    }

    /***********************************************************************/
    void printTotal() throws Exception {
        buf = "";
        for (int i = 0; i < 130; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = comcr.insertStr(buf, "總戶數及欠款總額 :", 1);
        szTmp = String.format("%6d", totalCnt);
        buf = comcr.insertStr(buf, szTmp, 28);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCbAmt);
        buf = comcr.insertStr(buf, szTmp, 82);
        szTmp = comcr.commFormat("2$,3$,3$,3$", totalCcAmt);
        buf = comcr.insertStr(buf, szTmp, 103);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        totalCnt = 0;
        totalCbAmt = 0;
        totalCcAmt = 0;
    }

    /***********************************************************************/
    void selectColLiacOppdtl() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_no,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "acct_type,";
        sqlCmd += "issue_date,";
        sqlCmd += "apply_date,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "acct_jrnl_bal2 ";
        sqlCmd += "from col_liac_oppdtl ";
        sqlCmd += "where acct_jrnl_bal > 0 ";
        sqlCmd += "and apply_date between ? and ? ";
        sqlCmd += "and acct_type = ? ";
        sqlCmd += "and ? = decode(pay_type,'0',0,decode(sign( ";
        sqlCmd += "months_between(to_date(apply_date,'yyyymmdd'), ";
        sqlCmd += "to_date(issue_date,'yyyymmdd'))-6),-1,1,2)) ";
        sqlCmd += "order by id_no asc ";
        setString(1, hTempApplyDate);
        setString(2, hTempLastDate);
        setString(3, hPaccAcctType);
        setInt(4, hTempType);

        extendField = "col_liac_oppdtl.";
        
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hClolId = getValue("col_liac_oppdtl.id_no", i);
            hClolIdPSeqno = getValue("col_liac_oppdtl.id_p_seqno", i);
            hClolAcctType = getValue("col_liac_oppdtl.acct_type", i);
            hClolIssueDate = getValue("col_liac_oppdtl.issue_date", i);
            hClolApplyDate = getValue("col_liac_oppdtl.apply_date", i);
            hClolAcctJrnlBal = getValueLong("col_liac_oppdtl.acct_jrnl_bal", i);
            hClolAcctJrnlBal2 = getValueLong("col_liac_oppdtl.acct_jrnl_bal2", i);

            //showLogMessage("I", "","select col_liac_oppdtl, id_p_seqno = " + hClolIdPSeqno);
            
            if (indexCnt == 0)
                printHeader();
            selectCrdIdno();
            printDetail();

            if (indexCnt >= 50)
                indexCnt = 0;
        }
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, hClolId, 1);
        buf = comcr.insertStr(buf, hClolAcctType, 20);
        buf = comcr.insertStr(buf, hIdnoChiName, 32);
        szTmp1 = String.format("%4.4s", hClolIssueDate);
        szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1)-1911, hClolIssueDate.substring(4,6), hClolIssueDate.substring(6,8));
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp1 = String.format("%4.4s", hClolApplyDate);
        szTmp = String.format("%03d/%2.2s/%2.2s", comcr.str2long(szTmp1)-1911, hClolApplyDate.substring(4,6), hClolApplyDate.substring(6,8));
        buf = comcr.insertStr(buf, szTmp, 67);
        szTmp = comcr.commFormat("1$,3$,3$,3$", hClolAcctJrnlBal);
        buf = comcr.insertStr(buf, szTmp, 83);
        szTmp = comcr.commFormat("1$,3$,3$,3$", hClolAcctJrnlBal2);
        buf = comcr.insertStr(buf, szTmp, 104);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        totalCnt++;

        totalCbAmt = totalCbAmt + hClolAcctJrnlBal;
        totalCcAmt = totalCcAmt + hClolAcctJrnlBal2;
    }

    /***********************************************************************/
    void selectCrdIdno() throws Exception {
        hIdnoChiName = "";
        sqlCmd = "select chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hClolIdPSeqno);
        
        extendField = "crd_idno.";
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_idno not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoChiName = getValue("crd_idno.chi_name");
        }
    }

    /***********************************************************************/
    void printHeader() throws Exception {
        pageLine++;
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
        buf = "";        
        switch (hTempType) {
        case 0:
            buf = comcr.insertStr(buf, "COL_A490R0", 1);
            rptName1="COL_A490R0";
            break;
        case 1:
            buf = comcr.insertStr(buf, "COL_A490R1", 1);
            rptName1="COL_A490R1";
            break;
        case 2:
            buf = comcr.insertStr(buf, "COL_A490R2", 1);
            rptName1="COL_A490R2";
            break;
        }
        
        switch (hTempType) {
        case 0:
        	rptDesc1 = String.format("從未繳款即前置協商報告表");
            break;
        case 1:
        	rptDesc1 = String.format("有繳款且發卡六個月(含)內即前置協商報告表");
            break;
        case 2:
        	rptDesc1 = String.format("有繳款且發卡六個月(不含)以上, 十二個月(含)以下即前置協商報告表");
            break;
        }
       
        szTmp = comcr.bankName;
        buf = comcr.insertStrCenter(buf, szTmp, 130);
        buf = comcr.insertStr(buf, "列印表日 :", 105);
        dispDate = comc.convDates(sysDate, 1);
        // conv_date('1',curr_date,szTmp1,szTmp,szTmp);
        buf = comcr.insertStr(buf, dispDate, 116);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        if (intaa == 1) {
            buf = comcr.insertStr(buf, "申請日期:", 1);
            szTmp = String.format("%03.0f/%2.2s/%2.2s", (comcr.str2double(hTempApplyDate) - 19110000) / 10000.0,
                    hTempApplyDate.substring(4,6), hTempApplyDate.substring(6,8));
            buf = comcr.insertStr(buf, szTmp, 10);
        }

        showLogMessage("I", "", "rptName1= " + rptName1);
        showLogMessage("I", "", "rptDesc1= " + rptDesc1);
        
        buf = comcr.insertStrCenter(buf, rptDesc1, 130);
        buf = comcr.insertStr(buf, "列印頁數 :", 105);
        szTmp = String.format("%4d", pageLine);
        buf = comcr.insertStr(buf, szTmp, 120);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "帳戶類別:", 1);
        szTmp = String.format("%2.2s %s", hPaccAcctType, hPaccChinName);
        buf = comcr.insertStr(buf, szTmp, 10);
        szTmp = String.format("%03.0f年%2.2s月份", (comcr.str2double(hBusiBusinessDate) - 19110000) / 10000.0,
                hBusiBusinessDate.substring(4));

        buf = comcr.insertStrCenter(buf, szTmp, 130);
        szTmp = String.format("貨幣單位 : %s 元", hPcceCurrChiName);
        buf = comcr.insertStr(buf, szTmp, 105);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "身分証號", 2);
        buf = comcr.insertStr(buf, "帳戶類別", 17);
        buf = comcr.insertStr(buf, "客 戶 名 稱", 31);
        buf = comcr.insertStr(buf, "發 卡 日", 50);
        buf = comcr.insertStr(buf, "前 協 日 期", 66);
        buf = comcr.insertStr(buf, "期 初 餘 額", 86);
        buf = comcr.insertStr(buf, "同一ID欠款總金額", 105);
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        for (int i = 0; i < 130; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));        
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA490 proc = new ColA490();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
