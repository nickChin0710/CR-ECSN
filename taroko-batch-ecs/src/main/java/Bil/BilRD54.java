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

/*列印公用事業處理明細表*/
public class BilRD54 extends AccessDAO {
    private String progname = "列印公用事業處理明細表程式  112/06/07  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD54";
    String prgmName = "列印公用事業處理明細表程式";
    
    String rptNameD54A = "合庫信用卡代扣公用事業處理明細表";
    String rptIdD54A = "BILR_D54A";
    int rptSeqD54A = 0;
    List<Map<String, Object>> lparD54A = new ArrayList<Map<String, Object>>();
    
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
    
    double errorADestAmtCR = 0;
    double errorADestAmtDR = 0;
    int errorACntCR = 0;
    int errorACntDR = 0;
    
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

            /*公用事業*/
            showLogMessage("I", "", "處理公用事業資料......");
            initCnt();
            selectBilOnusbillExchangeA();  
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],頁數:"+ pageCnt);
            if (pageCnt > 0) {
                String ftpName = String.format("%s.%s", rptIdD54A, sysDate);
                //String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptIdD54A, sysDate);
                //comc.writeReport(filename, lparD54A);
                comcr.insertPtrBatchRpt(lparD54A);
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

    	sqlCmd =  "select card_no,id_no,file_date,dest_amt,branch,mcht_chi_name,dest_amt,purchase_date,txn_code,status_flag ";
		sqlCmd += " from bil_onusbill_exchange ";
		sqlCmd += "where file_date  = ? ";
		sqlCmd += " and bill_type   = 'CHUP' ";
		sqlCmd += " and rec_type    = '2' ";
		sqlCmd += " order by txn_code ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeaderD54A();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", "##PPP"));
                printHeaderD54A();
                indexCnt = 0;
            }

            printDetailD54A();
        }

        if (indexCnt != 0)
            printFooterD54A();
    }

    /***********************************************************************/
    void printHeaderD54A() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptIdD54A, 1);
        buf = comcr.insertStrCenter(buf, rptNameD54A, 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        //buf = comcr.insertStr(buf, "入帳日 :", 20);
        //buf = comcr.insertStr(buf, hBusinssDate, 30);
        lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));

        buf = "";
        lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 12);
        buf = comcr.insertStr(buf, "持卡人ID", 24);
        buf = comcr.insertStr(buf, "卡號", 38);
        buf = comcr.insertStr(buf, "上傳日期", 60);
        buf = comcr.insertStr(buf, "代扣金額", 75);
        buf = comcr.insertStr(buf, "帳單摘要", 87);
        buf = comcr.insertStr(buf, "處理結果", 107);
        buf = comcr.insertStr(buf, "備註", 117);
        
        lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));
    }

    /***********************************************************************/
    void printFooterD54A() {
    	buf = "";
        lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));
        
    	buf = "";
    	lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "購貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalACntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalADestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	//szTmp = comcr.commFormat("3z,3#", totalABackAmtDR);
        //buf = comcr.insertStr(buf, szTmp, 85);
    	lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨成功筆數:", 6);
    	szTmp = String.format("%5d", totalACntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", totalADestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	//szTmp = comcr.commFormat("3z,3#", totalABackAmtCR);
        //buf = comcr.insertStr(buf, szTmp, 85);
    	lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));

    	buf = "";
    	buf = comcr.insertStr(buf, "購貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorACntDR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorADestAmtDR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));
    	
    	buf = "";
    	buf = comcr.insertStr(buf, "退貨失敗筆數:", 6);
    	szTmp = String.format("%5d", errorACntCR);
        buf = comcr.insertStr(buf, szTmp, 20);
    	buf = comcr.insertStr(buf, "金額小計:", 43);
        szTmp = comcr.commFormat("3z,3z,3#", errorADestAmtCR);
    	buf = comcr.insertStr(buf, szTmp, 71);
    	lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));
    }

    /***********************************************************************/
    void printDetailD54A() throws Exception {
        lineCnt++;
        indexCnt++;
        //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 12);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 12);
        } else if ("25".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "25-購貨沖正", 12);
        } 
        
        buf = comcr.insertStr(buf, getValue("id_no"), 24);
        buf = comcr.insertStr(buf, getValue("card_no"), 38);
        buf = comcr.insertStr(buf, getValue("file_date"), 60);
        
        double destAmt = getValueDouble("dest_amt");
        
		//long backDestAmt = 0;
		//if ("00".equals(getValue("status_flag"))) {
		//	backDestAmt = Math.round(doubleMul(destAmt,0.02));
		//	//showLogMessage("I","",String.format("backDestAmt=[%d]",backDestAmt));
		//}
        
        szTmp = comcr.commFormat("3z,3z,3#", destAmt);
        buf = comcr.insertStr(buf, szTmp, 71);
        
        buf = comcr.insertStr(buf, String.format("%-20s",getValue("mcht_chi_name")), 87);
        
        //szTmp = comcr.commFormat("3z,3#", backDestAmt);
        //buf = comcr.insertStr(buf, szTmp, 85);
        
        buf = comcr.insertStr(buf, getValue("status_flag"), 110);

        if ("00".equals(getValue("status_flag"))) {
        	if ("05".equals(getValue("txn_code")) ) {
        		totalADestAmtDR += destAmt;
        		totalACntDR++;
        	} else {
        		totalADestAmtCR += destAmt;
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
        
        lparD54A.add(comcr.putReport(rptIdD54A, rptNameD54A, sysDate, ++rptSeqD54A, "0", buf));

    }


    /***********************************************************************/
    void printBilRD02TWD() throws Exception {
    	
    	//重跑時要先刪除上一次產生的資料
    	deleteExistRptRecord();
    	commitDataBase();

    	buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款（公用事業）", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalADestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalADestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02.add(comcr.putReport(rptIdD02, rptNameD02, hBusinssDate, 54, "0", buf));
      
        insertPtrBatchRpt(lparD02);
    }
    
    void deleteExistRptRecord() throws Exception {
    	
    	daoTable  = " ptr_batch_rpt ";
		whereStr  = " where 1=1 "; 
		whereStr += " and program_code = ? ";
		whereStr += " and start_date = ? ";
		whereStr += " and seq = 54 ";
		
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
        BilRD54 proc = new BilRD54();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
