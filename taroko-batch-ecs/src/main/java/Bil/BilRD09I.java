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
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*國際信用卡清算明細表-信用卡*/
public class BilRD09I extends AccessDAO {
    private String progname = "列印國際信用卡清算明細表-分期付款程式  112/03/22  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD09I";
    String prgmName = "列印國際信用卡清算明細表-分期付款程式";
    String rptName = "國際信用卡清算明細表-分期付款";
    String rptId = "BILR_D09_INST";
    int rptSeq = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    
    String rptNameD02TWD = "國際信用卡清算彙計表-信用卡TWD";
    String rptIdD02TWD = "CRD02_TWD";
    int rptSeqD02TWD = 0;
    List<Map<String, Object>> lparD02TWD = new ArrayList<Map<String, Object>>();
    
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

    double totalNCCCDestAmtCR = 0;
    double totalNCCCDestAmtDR = 0;
    double totalONUSDestAmtCR = 0;
    double totalONUSDestAmtDR = 0;
    
    int totalNCCCCntCR = 0;
    int totalNCCCCntDR = 0;
    int totalONUSCntCR = 0;
    int totalONUSCntDR = 0;

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

            selectBilCurpost();

            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]"+ pageCnt);

            if (pageCnt > 0) {

                String ftpName = String.format("%s.%s_%s", rptId, sysDate, hBusinssDate);
                String filename = String.format("%s/reports/%s.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusinssDate);
                //改為線上報表
                comc.writeReport(filename, lpar1);
                //comcr.insertPtrBatchRpt(lpar1);
                
                ftpMput(ftpName);
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
    void selectBilCurpost() throws Exception {

    	sqlCmd =  "select card_no,settl_flag,dest_amt,purchase_date,txn_code,sign_flag, ";
    	sqlCmd += " auth_code,install_tot_term,install_first_amt,install_per_amt ";
    	sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and payment_type = 'I' ";
		sqlCmd += " order by sign_flag,txn_code,settl_flag ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt == 0) {
                printHeader();
            }

            if (indexCnt > 25) {
            	//分頁控制
                lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
            }

            printDetail();
        }

        if (indexCnt != 0)
            printFooter();
    }

    /***********************************************************************/
    void printHeader() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptId, 1);
        buf = comcr.insertStrCenter(buf, "國際信用卡清算明細表-分期付款", 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, sysDate, 10);
        buf = comcr.insertStr(buf, "入帳日 :", 20);
        buf = comcr.insertStr(buf, hBusinssDate, 30);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "交易日期", 1);
        buf = comcr.insertStr(buf, "交易摘要", 10);
        buf = comcr.insertStr(buf, "卡號", 25);
        buf = comcr.insertStr(buf, "交易金額", 45);
        buf = comcr.insertStr(buf, "期數", 65);
        buf = comcr.insertStr(buf, "授權碼", 71);
        buf = comcr.insertStr(buf, "首期金額", 85);
        buf = comcr.insertStr(buf, "每期金額", 99);
        buf = comcr.insertStr(buf, "入帳科子目", 113);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printFooter() {
    	
    	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
    	buf = "";
    	lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	buf = "";
    	buf = comcr.insertStr(buf, "借", 40);
    	buf = comcr.insertStr(buf, "筆數", 65);
    	buf = comcr.insertStr(buf, "貸", 75);
    	buf = comcr.insertStr(buf, "筆數", 100);
    	lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "NCCC分期帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalNCCCCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalNCCCCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "自行分期帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalONUSDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 40);
        szTmp = String.format("%7d", totalONUSCntDR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalONUSDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 75);
        szTmp = String.format("%7d", totalONUSCntCR);
        buf = comcr.insertStr(buf, szTmp, 100);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 10);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 10);
        } else if ("07".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "07-預現", 10);
        } else if ("25".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "25-購貨沖銷", 10);
        } else if ("26".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "26-退貨沖銷", 10);
        } else if ("27".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "27-預現沖銷", 10);
        }
        
        buf = comcr.insertStr(buf, getValue("card_no"), 25);
        
        szTmp = comcr.commFormat("3$,3$,3$.2$", getValueDouble("dest_amt"));
        buf = comcr.insertStr(buf, szTmp, 45);
        
        szTmp = String.format("%2d", getValueInt("install_tot_term"));
        buf = comcr.insertStr(buf, szTmp, 65);
        
        buf = comcr.insertStr(buf, getValue("auth_code"), 71);

        szTmp = comcr.commFormat("3$,3$,3$", getValueDouble("install_first_amt"));
        buf = comcr.insertStr(buf, szTmp, 85);

        szTmp = comcr.commFormat("3$,3$,3$", getValueDouble("install_per_amt"));
        buf = comcr.insertStr(buf, szTmp, 99);

        if ("8".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "NCCC分期__貸", 113);
        		totalNCCCDestAmtCR = totalNCCCDestAmtCR + getValueDouble("dest_amt");
        		totalNCCCCntCR = totalNCCCCntCR + 1;
        	} else {
        		buf = comcr.insertStr(buf, "NCCC分期__借", 113);
        		totalNCCCDestAmtDR = totalNCCCDestAmtDR + getValueDouble("dest_amt");
        		totalNCCCCntDR = totalNCCCCntDR + 1;
        	}
        } else if ("9".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "自行分期__貸", 113);
        		totalONUSDestAmtCR = totalONUSDestAmtCR + getValueDouble("dest_amt");
        		totalONUSCntCR = totalONUSCntCR + 1;
        	} else {
        		buf = comcr.insertStr(buf, "自行分期__借", 113);
        		totalONUSDestAmtDR = totalONUSDestAmtDR + getValueDouble("dest_amt");
        		totalONUSCntDR = totalONUSCntDR + 1;
        	}
        }
        
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }
    
    /***********************************************************************/
    void printBilRD02TWD() throws Exception {
    	
    	//重跑時要先刪除上一次產生的資料
    	deleteExistRptRecord();
    	commitDataBase();
    	
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款NCCC分", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 32, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "應收帳款—信用卡墊款(分期付款)", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalONUSDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 50);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalONUSDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 80);
        lparD02TWD.add(comcr.putReport(rptIdD02TWD, rptNameD02TWD, hBusinssDate, 55, "0", buf));
        
        insertPtrBatchRpt(lparD02TWD);
    }

    void deleteExistRptRecord() throws Exception {
    	
    	daoTable  = " ptr_batch_rpt ";
		whereStr  = " where 1=1 "; 
		whereStr += " and program_code = ? ";
		whereStr += " and start_date = ? ";
		whereStr += " and seq = 55 ";
		
		setString(1, rptIdD02TWD);
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
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "CREDITCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "CREDITCARD";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD09I proc = new BilRD09I();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
