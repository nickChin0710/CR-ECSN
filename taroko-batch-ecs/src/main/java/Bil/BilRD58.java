/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/03/22  V1.00.01    JeffKung  program initial                           *
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

/*列印住火險保費撥款明細表*/
public class BilRD58 extends AccessDAO {
    private String progname = "列印住火險保費撥款明細表程式  112/03/31  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD58";
    String prgmName = "列印住火險保費撥款明細表程式";
    
    String rptNameD58A = "合庫信用卡代扣新光住火險保費撥款明細表";
    String rptIdD58A = "BILR_D58A";
    int rptSeqD58A = 0;
    List<Map<String, Object>> lparD58A = new ArrayList<Map<String, Object>>();
    
    String rptNameD58B = "代扣房貸卡臺產火險保費撥款明細表";
    String rptIdD58B = "BILR_D58B";
    int rptSeqD58B = 0;
    List<Map<String, Object>> lparD58B = new ArrayList<Map<String, Object>>();
    
    String rptNameD58C = "代扣房貸卡明台火險保費撥款明細表";
    String rptIdD58C = "BILR_D58C";
    int rptSeqD58C = 0;
    List<Map<String, Object>> lparD58C = new ArrayList<Map<String, Object>>();
    
    String rptNameD58D = "代扣房貸卡法巴火險保費撥款明細表";
    String rptIdD58D = "BILR_D58D";
    int rptSeqD58D = 0;
    List<Map<String, Object>> lparD58D = new ArrayList<Map<String, Object>>();
    
    String rptNameD02 = "國際信用卡清算彙計表-信用卡TWD";
    String rptIdD02 = "CRD02_TWD";
    int rptSeqD02 = 0;
    List<Map<String, Object>> lparD02 = new ArrayList<Map<String, Object>>();
    
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
    
    double totalBDestAmtCR = 0;
    double totalBDestAmtDR = 0;
    double totalBBackAmtCR = 0;
    double totalBBackAmtDR = 0;
    int totalBCntCR = 0;
    int totalBCntDR = 0;
    
    double totalCDestAmtCR = 0;
    double totalCDestAmtDR = 0;
    double totalCBackAmtCR = 0;
    double totalCBackAmtDR = 0;
    int totalCCntCR = 0;
    int totalCCntDR = 0;
    
    double totalDDestAmtCR = 0;
    double totalDDestAmtDR = 0;
    double totalDBackAmtCR = 0;
    double totalDBackAmtDR = 0;
    int totalDCntCR = 0;
    int totalDCntDR = 0;
    
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
                String ftpName = String.format("%s.%s", rptIdD58A, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD58A, sysDate);
                //comc.writeReport(filename, lparD58A);
                comcr.insertPtrBatchRpt(lparD58A);
                //ftpMput(ftpName);
            }

            /*臺產火險*/
            showLogMessage("I", "", "處理臺產火險資料......");
            initCnt();
            selectBilOnusbillExchangeB();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {

                String ftpName = String.format("%s.%s", rptIdD58B, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD58B, sysDate);
                //comc.writeReport(filename, lparD58B);
                comcr.insertPtrBatchRpt(lparD58B);
                //ftpMput(ftpName);
            }
            
            /*明台火險*/
            showLogMessage("I", "", "處理明台火險資料......");
            initCnt();
            selectBilOnusbillExchangeC();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdD58C, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD58C, sysDate);
                //comc.writeReport(filename, lparD58C);
                comcr.insertPtrBatchRpt(lparD58C);
                //ftpMput(ftpName);
            }
            
            /*法巴火險*/
            showLogMessage("I", "", "處理法巴火險資料......");
            initCnt();
            selectBilOnusbillExchangeD();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdD58D, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD58D, sysDate);
                //comc.writeReport(filename, lparD58D);
                comcr.insertPtrBatchRpt(lparD58D);
                //ftpMput(ftpName);
            }
            
            commitDataBase();
            
            //處理CRD02_TWD報表
            printBilRD02TWD();

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
		sqlCmd += " and status_flag = '00' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD58A();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", "##PPP"));
                printHeaderD58A();
                indexCnt = 0;
            }

            printDetailD58A();
        }

        if (indexCnt != 0)
            printFooterD58A();
    }

    /***********************************************************************/
    void printHeaderD58A() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD58A, 1);
        buf = comcr.insertStrCenter(buf, rptNameD58A, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));

        buf = "";
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "備註", 110);
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));
    }

    /***********************************************************************/
    void printFooterD58A() {
    	buf = "";
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));
        
    	buf = "";
    	lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨筆數:", 10);
    	szTmp = String.format("%5d", totalACntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalADestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalABackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨筆數:", 10);
    	szTmp = String.format("%5d", totalACntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalADestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalABackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "實際撥款總筆數:", 4);
    	szTmp = String.format("%5d", (totalACntCR+totalACntDR));
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "實際撥款金額合計:", 35);
        szTmp = comcr.commFormat("3z,3z,3#", (totalADestAmtDR-totalADestAmtCR));
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", (totalABackAmtDR-totalABackAmtCR));
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊新光產物-活期存款帳號:0770-717-236262", 1);
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));
        buf = "";
    	buf = comcr.insertStr(buf, "回饋金=交易金額*2%予持卡人之信用卡帳戶。", 3);
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailD58A() throws Exception {
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
		long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
			//showLogMessage("I","",String.format("backDestAmt=[%d]",backDestAmt));
		}
        
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        if ("05".equals(getValue("txn_code")) ) {
        	totalADestAmtDR += destAmt;
        	totalABackAmtDR += backDestAmt;
        	totalACntDR++;
        } else if ("06".equals(getValue("txn_code")) ) {
        	totalADestAmtCR += destAmt;
        	totalABackAmtCR += backDestAmt;
        	totalACntCR++;
        }
        
        lparD58A.add(comcr.putReport(rptIdD58A, rptNameD58A, sysDate, ++rptSeqD58A, "0", buf));

    }

    /***********************************************************************/
    void selectBilOnusbillExchangeB() throws Exception {

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'TFMI' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " and status_flag = '00' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD58B();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", "##PPP"));
                printHeaderD58B();
                indexCnt = 0;
            }

            printDetailD58B();
        }

        if (indexCnt != 0)
            printFooterD58B();
    }

    /***********************************************************************/
    void printHeaderD58B() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD58B, 1);
        buf = comcr.insertStrCenter(buf, rptNameD58B, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));

        buf = "";
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "備註", 110);
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));
    }

    /***********************************************************************/
    void printFooterD58B() {
    	buf = "";
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));
        
    	buf = "";
    	lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨筆數:", 10);
    	szTmp = String.format("%5d", totalBCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalBDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalBBackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨筆數:", 10);
    	szTmp = String.format("%5d", totalBCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalBDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalBBackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "實際撥款總筆數:", 4);
    	szTmp = String.format("%5d", (totalBCntCR+totalBCntDR));
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "實際撥款金額合計:", 35);
        szTmp = comcr.commFormat("3z,3z,3#", (totalBDestAmtDR-totalBDestAmtCR));
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", (totalBBackAmtDR-totalBBackAmtCR));
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊臺產火險-活期存款帳號:0110-717-267362", 1);
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));
        buf = "";
    	buf = comcr.insertStr(buf, "回饋金=交易金額*2%予持卡人之信用卡帳戶。", 3);
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailD58B() throws Exception {
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
		long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
			//showLogMessage("I","",String.format("backDestAmt=[%d]",backDestAmt));
		}
        
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        if ("05".equals(getValue("txn_code")) ) {
        	totalBDestAmtDR += destAmt;
        	totalBBackAmtDR += backDestAmt;
        	totalBCntDR++;
        } else if ("06".equals(getValue("txn_code")) ) {
        	totalBDestAmtCR += destAmt;
        	totalBBackAmtCR += backDestAmt;
        	totalBCntCR++;
        }
        
        lparD58B.add(comcr.putReport(rptIdD58B, rptNameD58B, sysDate, ++rptSeqD58B, "0", buf));

    }
    
    /***********************************************************************/
    void selectBilOnusbillExchangeC() throws Exception {

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'MTIG' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " and status_flag = '00' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD58C();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", "##PPP"));
                printHeaderD58C();
                indexCnt = 0;
            }

            printDetailD58C();
        }

        if (indexCnt != 0)
            printFooterD58C();
    }

    /***********************************************************************/
    void printHeaderD58C() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD58C, 1);
        buf = comcr.insertStrCenter(buf, rptNameD58C, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));

        buf = "";
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "備註", 110);
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));
    }

    /***********************************************************************/
    void printFooterD58C() {
    	buf = "";
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));
        
    	buf = "";
    	lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨筆數:", 10);
    	szTmp = String.format("%5d", totalCCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalCDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalCBackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨筆數:", 10);
    	szTmp = String.format("%5d", totalCCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalCDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalCBackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "實際撥款總筆數:", 4);
    	szTmp = String.format("%5d", (totalCCntCR+totalCCntDR));
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "實際撥款金額合計:", 35);
        szTmp = comcr.commFormat("3z,3z,3#", (totalCDestAmtDR-totalCDestAmtCR));
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", (totalCBackAmtDR-totalCBackAmtCR));
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊明台火險-活期存款帳號:0914-717-235721", 1);
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));
        buf = "";
    	buf = comcr.insertStr(buf, "回饋金=交易金額*2%予持卡人之信用卡帳戶。", 3);
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailD58C() throws Exception {
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
		long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
			//showLogMessage("I","",String.format("backDestAmt=[%d]",backDestAmt));
		}
        
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        if ("05".equals(getValue("txn_code")) ) {
        	totalCDestAmtDR += destAmt;
        	totalCBackAmtDR += backDestAmt;
        	totalCCntDR++;
        } else if ("06".equals(getValue("txn_code")) ) {
        	totalCDestAmtCR += destAmt;
        	totalCBackAmtCR += backDestAmt;
        	totalCCntCR++;
        }
        
        lparD58C.add(comcr.putReport(rptIdD58C, rptNameD58C, sysDate, ++rptSeqD58C, "0", buf));

    }
    
    /***********************************************************************/
    void selectBilOnusbillExchangeD() throws Exception {

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'FPLF' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " and status_flag = '00' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD58D();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", "##PPP"));
                printHeaderD58D();
                indexCnt = 0;
            }

            printDetailD58D();
        }

        if (indexCnt != 0)
            printFooterD58D();
    }

    /***********************************************************************/
    void printHeaderD58D() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD58D, 1);
        buf = comcr.insertStrCenter(buf, rptNameD58D, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));

        buf = "";
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "回饋金", 87);
        buf = comcr.insertStr(buf, "貸放分行", 97);
        buf = comcr.insertStr(buf, "備註", 110);
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));
    }

    /***********************************************************************/
    void printFooterD58D() {
    	buf = "";
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));
        
    	buf = "";
    	lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨筆數:", 10);
    	szTmp = String.format("%5d", totalDCntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalDDestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalDBackAmtDR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨筆數:", 10);
    	szTmp = String.format("%5d", totalDCntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalDDestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", totalDBackAmtCR);
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "實際撥款總筆數:", 4);
    	szTmp = String.format("%5d", (totalDCntCR+totalDCntDR));
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "實際撥款金額合計:", 35);
        szTmp = comcr.commFormat("3z,3z,3#", (totalDDestAmtDR-totalDDestAmtCR));
    	buf = comcr.insertStr(buf, szTmp, 71);
    	szTmp = comcr.commFormat("3z,3#", (totalDBackAmtDR-totalDBackAmtCR));
        buf = comcr.insertStr(buf, szTmp, 85);
    	lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "＊法巴火險-活期存款帳號:0450-717-527815", 1);
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));
        buf = "";
    	buf = comcr.insertStr(buf, "回饋金=交易金額*2%予持卡人之信用卡帳戶。", 3);
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));
    	
    }

    /***********************************************************************/
    void printDetailD58D() throws Exception {
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
		long backDestAmt = 0;
		if ("00".equals(getValue("status_flag"))) {
			backDestAmt = Math.round(doubleMul(destAmt,0.02));
			//showLogMessage("I","",String.format("backDestAmt=[%d]",backDestAmt));
		}
        
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        szTmp = comcr.commFormat("3z,3#", backDestAmt);
        buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("branch"), 99);
        
        if ("05".equals(getValue("txn_code")) ) {
        	totalDDestAmtDR += destAmt;
        	totalDBackAmtDR += backDestAmt;
        	totalDCntDR++;
        } else if ("06".equals(getValue("txn_code")) ) {
        	totalDDestAmtCR += destAmt;
        	totalDBackAmtCR += backDestAmt;
        	totalDCntCR++;
        }
        
        lparD58D.add(comcr.putReport(rptIdD58D, rptNameD58D, sysDate, ++rptSeqD58D, "0", buf));

    }
    
    /***********************************************************************/
    void printBilRD02TWD() throws Exception {
    	
    	//重跑時要先刪除上一次產生的資料
    	deleteExistRptRecord();
    	commitDataBase();

    	buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（新光火險）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalADestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalADestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, 56, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（臺產火險）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalBDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalBDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, 57, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（明台火險）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, 58, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（法巴火險）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalDDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalDDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, 59, "0", buf));
        
        insertPtrBatchRpt(lparD02);
    }
    
    void deleteExistRptRecord() throws Exception {
    	
    	daoTable  = " ptr_batch_rpt ";
		whereStr  = " where 1=1 "; 
		whereStr += " and program_code = ? ";
		whereStr += " and start_date = ? ";
		whereStr += " and seq >= 56 ";
		
		setString(1, rptIdD02);
		setString(2, hBusinssDate);
	
		deleteTable();
    	
    }
    int insertPtrBatchRpt(List<Map<String, Object>> lpar) throws Exception {
        int actCnt = 0;
        noTrim = "Y";
        String tmpStr = hBusinssDate + "000002";
        for (int i = 0; i < lpar.size(); i++) {
            if (tmpStr.length() > 8) {
                setValue("start_date", tmpStr.substring(0, 8));
                setValue("start_time", tmpStr.substring(8));
            } else {
                setValue("start_date", tmpStr.substring(0));
                setValue("start_time", "");
            }
            setValue("program_code", lpar.get(i).get("prgmId").toString());
            setValue("rptname", lpar.get(i).get("prgmName").toString());
            setValue("seq", lpar.get(i).get("seq").toString());
            setValue("kind", lpar.get(i).get("kind").toString());
            setValue("report_content", lpar.get(i).get("content").toString());

            daoTable = "ptr_batch_rpt";
            insertTable();
            if (dupRecord.equals("Y")) {
                return 0;
            }
        }
        noTrim = "";
        return actCnt;
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
        BilRD58 proc = new BilRD58();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
