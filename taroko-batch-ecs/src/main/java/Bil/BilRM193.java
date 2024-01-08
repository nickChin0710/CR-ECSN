/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/10/06  V1.00.01    JeffKung  program initial                           *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*列印電子採購卡手續費月報表*/
public class BilRM193 extends AccessDAO {
    private String progname = "列印電子採購卡手續費月報表程式  112/10/06  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr = null;

    String prgmId = "BilRM193";
    String prgmName = "列印電子採購卡手續費月報表程式";
    
    String rptNameM193 = "合庫信用卡電子採購卡手續費月報表";
    String rptIdM193 = "CRM193";
    int rptSeqM193 = 0;
    List<Map<String, Object>> lparM193 = new ArrayList<Map<String, Object>>();
    
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String hBusinssDate = "";

    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt = 0;
    int lineCnt = 0;

    double totalSettlAmt = 0;
    double totalBankFeeAmt = 0;
    double totalChtFeeAmt = 0;
    double totalPaymentAmt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            }
            
            String nextMonthFirstDate = comm.nextMonth(hBusinssDate, 1) + "01";
            String thisMonthFirstDate = comc.getSubString(hBusinssDate,0,6) + "01";
            
            //取得本月最後一個營業日
            comr.increaseDays(nextMonthFirstDate,-1);
            String lastBusindate = comr.increaseNewDate;
            
            if (hBusinssDate.equals(lastBusindate)==false) {
            	showLogMessage("I", "", "本日非執行日,本月執行日為["+ lastBusindate+"]");
            	return 0;
            }
            
            //取得前月最後一個營業日
            comr.increaseDays(thisMonthFirstDate,-1);
            String lastMonthlastBusindate = comr.increaseNewDate;
            String condDateFrom = comm.nextDate(lastMonthlastBusindate);
            
            showLogMessage("I", "", "資料日期=[" + condDateFrom +"~"+ hBusinssDate + "]");

            showLogMessage("I", "", "程式開始處理......");
            initCnt();
            selectBilGovpurchase(condDateFrom,hBusinssDate);  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdM193, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdM193, sysDate);
                //comc.writeReport(filename, lparM193);
                comcr.insertPtrBatchRpt(lparM193);
                //ftpMput(ftpName);
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
    void commonRtn() throws Exception {
        hBusinssDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectBilGovpurchase(String fromDate, String toDate) throws Exception {

    	sqlCmd =  "select settl_date,payment_date, ";
    	sqlCmd += " sum(to_number(settl_amt)) settl_amt, ";
    	sqlCmd += " sum(to_number(bank_fee_amt)) bank_fee_amt, ";
    	sqlCmd += " sum(to_number(cht_fee_amt)) cht_fee_amt, ";
    	sqlCmd += " sum(to_number(payment_amt)) payment_amt ";
    	sqlCmd += " from bil_govpurchase ";
		sqlCmd += "where 1=1 ";
		sqlCmd += " and settl_date >= ? ";
		sqlCmd += " and settl_date <= ? ";
		sqlCmd += " group by settl_date,payment_date ";
		sqlCmd += " order by settl_date,payment_date ";
		
		setString(1,fromDate);
		setString(2,toDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderM193(fromDate,toDate);
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", "##PPP"));
                printHeaderM193(fromDate,toDate);
                indexCnt = 0;
            }

            printDetailM193();
        }

        if (indexCnt != 0)
            printFooterM193();
    }

    /***********************************************************************/
    void printHeaderM193(String fromDate, String toDate) {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdM193, 1);
        buf = comcr.insertStrCenter(buf, rptNameM193, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        buf = comcr.insertStr(buf, "請款日期 :", 20);
        buf = comcr.insertStr(buf, (fromDate+" - "+toDate), 30); 
        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));

        buf = "";
        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "請款日期", 1);
        buf = comcr.insertStr(buf, "交易金額", 19);
        buf = comcr.insertStr(buf, "手續費", 41);
        buf = comcr.insertStr(buf, "中華電手續費", 55);
        buf = comcr.insertStr(buf, "撥款金額", 79);

        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));
    }

    /***********************************************************************/
    void printFooterM193() {
    	buf = "";
        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));
        
    	buf = "";
    	lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));
    	
    	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));
        
    	buf = "";
    	buf = comcr.insertStr(buf, "合計:", 1);
    	
        szTmp = comcr.commFormat("3z,3z,3#", totalSettlAmt);
        buf = comcr.insertStr(buf, szTmp, 15);
       
        szTmp = comcr.commFormat("3z,3z,3#", totalBankFeeAmt);
        buf = comcr.insertStr(buf, szTmp, 35);
        
        szTmp = comcr.commFormat("3z,3z,3#", totalChtFeeAmt);
        buf = comcr.insertStr(buf, szTmp, 55);
        
        szTmp = comcr.commFormat("3z,3z,3#", totalPaymentAmt);
        buf = comcr.insertStr(buf, szTmp, 75);

    	lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailM193() throws Exception {
        lineCnt++;
        indexCnt++;
        double tempAmt = 0;

        buf = "";
        buf = comcr.insertStr(buf, getValue("settl_date"), 1);
        
        tempAmt = getValueDouble("settl_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", tempAmt);
        buf = comcr.insertStr(buf, szTmp, 15);
        totalSettlAmt += tempAmt;
        
        tempAmt = getValueDouble("bank_fee_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", tempAmt);
        buf = comcr.insertStr(buf, szTmp, 35);
        totalBankFeeAmt += tempAmt;
        
        tempAmt = getValueDouble("cht_fee_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", tempAmt);
        buf = comcr.insertStr(buf, szTmp, 55);
        totalChtFeeAmt += tempAmt;
        
        tempAmt = getValueDouble("payment_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", tempAmt);
        buf = comcr.insertStr(buf, szTmp, 75);
        totalPaymentAmt += tempAmt;
        
        lparM193.add(comcr.putReport(rptIdM193, rptNameM193, sysDate, ++rptSeqM193, "0", buf));

    }
    
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "RPQS_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "RPQS_FTP";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }
    
    void initCnt() {
    	totalCnt = 0;
        indexCnt = 0;
        pageCnt = 0;
        lineCnt = 0;
    }
    
    public static Double doubleMul(Double v1,Double v2){

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.multiply(b2).doubleValue();

	}
    
    
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRM193 proc = new BilRM193();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
