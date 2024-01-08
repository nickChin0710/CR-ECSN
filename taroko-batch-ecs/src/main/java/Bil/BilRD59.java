/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/06  V1.00.01    JeffKung  program initial                           *
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

/*列印住火險保費處理明細表*/
public class BilRD59 extends AccessDAO {
    private String progname = "列印住火險保費處理明細表程式  112/04/06  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD59";
    String prgmName = "列印住火險保費處理明細表程式";
    
    String rptNameD59A = "合庫信用卡代扣新光住火險保費處理明細表";
    String rptIdD59A = "BILR_D59A";
    int rptSeqD59A = 0;
    List<Map<String, Object>> lparD59A = new ArrayList<Map<String, Object>>();
    
    String rptNameD59B = "代扣房貸卡臺產火險保費處理明細表";
    String rptIdD59B = "BILR_D59B";
    int rptSeqD59B = 0;
    List<Map<String, Object>> lparD59B = new ArrayList<Map<String, Object>>();
    
    String rptNameD59C = "代扣房貸卡明台火險保費處理明細表";
    String rptIdD59C = "BILR_D59C";
    int rptSeqD59C = 0;
    List<Map<String, Object>> lparD59C = new ArrayList<Map<String, Object>>();
    
    String rptNameD59D = "代扣房貸卡法巴火險保費處理明細表";
    String rptIdD59D = "BILR_D59D";
    int rptSeqD59D = 0;
    List<Map<String, Object>> lparD59D = new ArrayList<Map<String, Object>>();
    
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

    double totalADestAmtCR = 0;
    double totalADestAmtDR = 0;
    double totalABackAmtCR = 0;
    double totalABackAmtDR = 0;
    int totalACntCR = 0;
    int totalACntDR = 0;
    
    double errorADestAmtCR = 0;
    double errorADestAmtDR = 0;
    int errorACntCR = 0;
    int errorACntDR = 0;
    
    double totalBDestAmtCR = 0;
    double totalBDestAmtDR = 0;
    double totalBBackAmtCR = 0;
    double totalBBackAmtDR = 0;
    int totalBCntCR = 0;
    int totalBCntDR = 0;
    
    double errorBDestAmtCR = 0;
    double errorBDestAmtDR = 0;
    int errorBCntCR = 0;
    int errorBCntDR = 0;
    
    double totalCDestAmtCR = 0;
    double totalCDestAmtDR = 0;
    double totalCBackAmtCR = 0;
    double totalCBackAmtDR = 0;
    int totalCCntCR = 0;
    int totalCCntDR = 0;
    
    double errorCDestAmtCR = 0;
    double errorCDestAmtDR = 0;
    int errorCCntCR = 0;
    int errorCCntDR = 0;
    
    double totalDDestAmtCR = 0;
    double totalDDestAmtDR = 0;
    double totalDBackAmtCR = 0;
    double totalDBackAmtDR = 0;
    int totalDCntCR = 0;
    int totalDCntDR = 0;
    
    double errorDDestAmtCR = 0;
    double errorDDestAmtDR = 0;
    int errorDCntCR = 0;
    int errorDCntDR = 0;
    
    int lineCnt = 0;

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

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            }
            
            showLogMessage("I", "", "資料日期=[" + hBusinssDate + "]");

            /*新光火險*/
            showLogMessage("I", "", "處理新光火險資料......");
            initCnt();
            selectBilOnusbillExchangeA();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdD59A, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD59A, sysDate);
                //comc.writeReport(filename, lparD59A);
                comcr.insertPtrBatchRpt(lparD59A);
                //ftpMput(ftpName);
            }

            /*臺產火險*/
            showLogMessage("I", "", "處理臺產火險資料......");
            initCnt();
            selectBilOnusbillExchangeB();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {

                String ftpName = String.format("%s.%s", rptIdD59B, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD59B, sysDate);
                //comc.writeReport(filename, lparD59B);
                comcr.insertPtrBatchRpt(lparD59B);
                //ftpMput(ftpName);
            }
            
            /*明台火險*/
            showLogMessage("I", "", "處理明台火險資料......");
            initCnt();
            selectBilOnusbillExchangeC();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdD59C, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD59C, sysDate);
                //comc.writeReport(filename, lparD59C);
                comcr.insertPtrBatchRpt(lparD59C);
                //ftpMput(ftpName);
            }
            
            /*法巴火險*/
            showLogMessage("I", "", "處理法巴火險資料......");
            initCnt();
            selectBilOnusbillExchangeD();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdD59D, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD59D, sysDate);
                //comc.writeReport(filename, lparD59D);
                comcr.insertPtrBatchRpt(lparD59D);
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
    void selectBilOnusbillExchangeA() throws Exception {

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'SKIC' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD59A();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", "##PPP"));
                printHeaderD59A();
                indexCnt = 0;
            }

            printDetailD59A();
        }

        if (indexCnt != 0)
            printFooterD59A();
    }

    /***********************************************************************/
    void printHeaderD59A() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD59A, 1);
        buf = comcr.insertStrCenter(buf, rptNameD59A, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));

        buf = "";
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "處理結果", 107);
        buf = comcr.insertStr(buf, "備註", 117);
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));
    }

    /***********************************************************************/
    void printFooterD59A() {
    	buf = "";
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));
        
    	buf = "";
    	lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalACntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalADestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalABackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalACntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalADestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalABackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "購貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorACntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorADestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorACntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorADestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊新光產物-活期存款帳號:0770-717-236262", 1);
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));
   	
    }

    /***********************************************************************/
    void printDetailD59A() throws Exception {
        lineCnt++;
        indexCnt++;
        //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 12);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 12);
        } 
        
        buf = comcr.insertStr(buf, getValue("id_no"), 24);
        buf = comcr.insertStr(buf, getValue("card_no"), 38);
        buf = comcr.insertStr(buf, getValue("file_date"), 60);
        
        double destAmt = getValueDouble("dest_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
		}
		szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        buf = comcr.insertStr(buf, getValue("status_flag"), 110);
        
        if ("00".equals(getValue("status_flag"))) {
        	if ("05".equals(getValue("txn_code")) ) {
        		totalADestAmtDR += destAmt;
        		totalABackAmtDR += backDestAmt;
        		totalACntDR++;
        	} else {
        		totalADestAmtCR += destAmt;
        		totalABackAmtCR += backDestAmt;
        		totalACntCR++;
        	}
        } else {
        	if ("05".equals(getValue("txn_code")) ) {
        		errorADestAmtDR += destAmt;
        		errorACntDR++;
        	} else {
        		errorADestAmtCR += destAmt;
        		errorACntCR++;
        	}	
        }
        
        lparD59A.add(comcr.putReport(rptIdD59A, rptNameD59A, sysDate, ++rptSeqD59A, "0", buf));

    }

    /***********************************************************************/
    void selectBilOnusbillExchangeB() throws Exception {

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'TFMI' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD59B();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", "##PPP"));
                printHeaderD59B();
                indexCnt = 0;
            }

            printDetailD59B();
        }

        if (indexCnt != 0)
            printFooterD59B();
    }

    /***********************************************************************/
    void printHeaderD59B() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD59B, 1);
        buf = comcr.insertStrCenter(buf, rptNameD59B, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));

        buf = "";
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "處理結果", 107);
        buf = comcr.insertStr(buf, "備註", 117);
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));
    }

    /***********************************************************************/
    void printFooterD59B() {
    	buf = "";
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));
        
    	buf = "";
    	lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalBCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalBDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalBBackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalBCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalBDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalBBackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "購貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorBCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorBDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorBCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorBDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊臺產火險-活期存款帳號:0110-717-267362", 1);
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailD59B() throws Exception {
        lineCnt++;
        indexCnt++;
        //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 12);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 12);
        } 
        
        buf = comcr.insertStr(buf, getValue("id_no"), 24);
        buf = comcr.insertStr(buf, getValue("card_no"), 38);
        buf = comcr.insertStr(buf, getValue("file_date"), 60);
        
        double destAmt = getValueDouble("dest_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
		}
		szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        buf = comcr.insertStr(buf, getValue("status_flag"), 110);
        
        if ("00".equals(getValue("status_flag"))) {
        	if ("05".equals(getValue("txn_code")) ) {
        		totalBDestAmtDR += destAmt;
        		totalBBackAmtDR += backDestAmt;
        		totalBCntDR++;
        	} else {
        		totalBDestAmtCR += destAmt;
        		totalBBackAmtCR += backDestAmt;
        		totalBCntCR++;
        	}
        } else {
        	if ("05".equals(getValue("txn_code")) ) {
        		errorBDestAmtDR += destAmt;
        		errorBCntDR++;
        	} else {
        		errorBDestAmtCR += destAmt;
        		errorBCntCR++;
        	}	
        }
        
        lparD59B.add(comcr.putReport(rptIdD59B, rptNameD59B, sysDate, ++rptSeqD59B, "0", buf));

    }
    
    /***********************************************************************/
    void selectBilOnusbillExchangeC() throws Exception {

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'MTIG' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD59C();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", "##PPP"));
                printHeaderD59C();
                indexCnt = 0;
            }

            printDetailD59C();
        }

        if (indexCnt != 0)
            printFooterD59C();
    }

    /***********************************************************************/
    void printHeaderD59C() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD59C, 1);
        buf = comcr.insertStrCenter(buf, rptNameD59C, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));

        buf = "";
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "處理結果", 107);
        buf = comcr.insertStr(buf, "備註", 117);
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));
    }

    /***********************************************************************/
    void printFooterD59C() {
    	buf = "";
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));
        
    	buf = "";
    	lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalCCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalCDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalCBackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalCCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalCDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalCBackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "購貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorCCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorCDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorCCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorCDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊明台火險-活期存款帳號:0914-717-235721", 1);
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailD59C() throws Exception {
        lineCnt++;
        indexCnt++;
        //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 12);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 12);
        } 
        
        buf = comcr.insertStr(buf, getValue("id_no"), 24);
        buf = comcr.insertStr(buf, getValue("card_no"), 38);
        buf = comcr.insertStr(buf, getValue("file_date"), 60);
        
        double destAmt = getValueDouble("dest_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
		}
		szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        buf = comcr.insertStr(buf, getValue("status_flag"), 110);
        
        if ("00".equals(getValue("status_flag"))) {
        	if ("05".equals(getValue("txn_code")) ) {
        		totalCDestAmtDR += destAmt;
        		totalCBackAmtDR += backDestAmt;
        		totalCCntDR++;
        	} else {
        		totalCDestAmtCR += destAmt;
        		totalCBackAmtCR += backDestAmt;
        		totalCCntCR++;
        	}
        } else {
        	if ("05".equals(getValue("txn_code")) ) {
        		errorCDestAmtDR += destAmt;
        		errorCCntDR++;
        	} else {
        		errorCDestAmtCR += destAmt;
        		errorCCntCR++;
        	}	
        }
       
        lparD59C.add(comcr.putReport(rptIdD59C, rptNameD59C, sysDate, ++rptSeqD59C, "0", buf));

    }
    
    /***********************************************************************/
    void selectBilOnusbillExchangeD() throws Exception {

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'FPLF' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD59D();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", "##PPP"));
                printHeaderD59D();
                indexCnt = 0;
            }

            printDetailD59D();
        }

        if (indexCnt != 0)
            printFooterD59D();
    }

    /***********************************************************************/
    void printHeaderD59D() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD59D, 1);
        buf = comcr.insertStrCenter(buf, rptNameD59D, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));

        buf = "";
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "處理結果", 107);
        buf = comcr.insertStr(buf, "備註", 117);
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));
    }

    /***********************************************************************/
    void printFooterD59D() {
    	buf = "";
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));
        
    	buf = "";
    	lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalDCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalDDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalDBackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalDCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalDDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalDBackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "購貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorDCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorDDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorDCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorDDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊法巴火險-活期存款帳號:0450-717-527815", 1);
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));
   	
    }

    /***********************************************************************/
    void printDetailD59D() throws Exception {
        lineCnt++;
        indexCnt++;
        //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 12);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 12);
        } 
        
        buf = comcr.insertStr(buf, getValue("id_no"), 24);
        buf = comcr.insertStr(buf, getValue("card_no"), 38);
        buf = comcr.insertStr(buf, getValue("file_date"), 60);
        
        double destAmt = getValueDouble("dest_amt"); 
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
		}
		szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        buf = comcr.insertStr(buf, getValue("status_flag"), 110);
        
        if ("00".equals(getValue("status_flag"))) {
        	if ("05".equals(getValue("txn_code")) ) {
        		totalDDestAmtDR += destAmt;
        		totalDBackAmtDR += backDestAmt;
        		totalDCntDR++;
        	} else {
        		totalDDestAmtCR += destAmt;
        		totalDBackAmtCR += backDestAmt;
        		totalDCntCR++;
        	}
        } else {
        	if ("05".equals(getValue("txn_code")) ) {
        		errorDDestAmtDR += destAmt;
        		errorDCntDR++;
        	} else {
        		errorDDestAmtCR += destAmt;
        		errorDCntCR++;
        	}	
        }
        
        lparD59D.add(comcr.putReport(rptIdD59D, rptNameD59D, sysDate, ++rptSeqD59D, "0", buf));

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
        BilRD59 proc = new BilRD59();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
