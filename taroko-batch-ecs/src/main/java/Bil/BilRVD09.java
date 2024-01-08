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
public class BilRVD09 extends AccessDAO {
    private String progname = "列印國際信用卡清算明細表-VISA金融卡程式  112/03/22  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRVD09";
    String prgmName = "列印國際信用卡清算明細表-VISA金融卡程式";
    
    String rptName = "國際信用卡清算明細表-VISA金融卡";
    String rptId = "BILR_VD09";
    int rptSeq = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    
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

    double totalFISCDestAmtCR = 0;
    double totalFISCDestAmtDR = 0;
    double totalNCCCDestAmtCR = 0;
    double totalNCCCDestAmtDR = 0;
    double totalINTLDestAmtCR = 0;
    double totalINTLDestAmtDR = 0;
    double totalOnusSBDestAmtCR = 0;
    double totalOnusSBDestAmtDR = 0;
    
    int totalFISCCntCR = 0;
    int totalFISCCntDR = 0;
    int totalNCCCCntCR = 0;
    int totalNCCCCntDR = 0;
    int totalINTLCntCR = 0;
    int totalINTLCntDR = 0;
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

    	sqlCmd =  "select card_no,settl_flag,dest_amt,purchase_date,txn_code,sign_flag ";
		sqlCmd += " from dbb_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " and settl_flag in ('0','6','8','9') ";
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
        buf = comcr.insertStrCenter(buf, rptName, 132);
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
        buf = comcr.insertStr(buf, "交易摘要", 15);
        buf = comcr.insertStr(buf, "卡號", 35);
        buf = comcr.insertStr(buf, "交易金額/本金", 55);
        buf = comcr.insertStr(buf, "入帳科子目", 75);
        buf = comcr.insertStr(buf, "備__註", 100);
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
    	buf = comcr.insertStr(buf, "借", 35);
    	buf = comcr.insertStr(buf, "筆數", 55);
    	buf = comcr.insertStr(buf, "貸", 70);
    	buf = comcr.insertStr(buf, "筆數", 90);
    	buf = comcr.insertStr(buf, "加總筆數", 100);
    	buf = comcr.insertStr(buf, "加總金額", 121);
    	lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
    	buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
        buf = "";
        buf = comcr.insertStr(buf, "金資____帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalFISCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 30);
        szTmp = String.format("%7d", totalFISCCntDR);
        buf = comcr.insertStr(buf, szTmp, 55);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalFISCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = String.format("%7d", totalFISCCntCR);
        buf = comcr.insertStr(buf, szTmp, 90);
        szTmp = String.format("%7d", (totalFISCCntDR+totalFISCCntCR));
        buf = comcr.insertStr(buf, szTmp, 100);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", (totalFISCDestAmtCR-totalFISCDestAmtDR));
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "ＮＣＣＣ帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 30);
        szTmp = String.format("%7d", totalNCCCCntDR);
        buf = comcr.insertStr(buf, szTmp, 55);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = String.format("%7d", totalNCCCCntCR);
        buf = comcr.insertStr(buf, szTmp, 90);
        szTmp = String.format("%7d", (totalNCCCCntDR+totalNCCCCntCR));
        buf = comcr.insertStr(buf, szTmp, 100);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", (totalNCCCDestAmtCR-totalNCCCDestAmtDR));
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "國外＿＿帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalINTLDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 30);
        szTmp = String.format("%7d", totalINTLCntDR);
        buf = comcr.insertStr(buf, szTmp, 55);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalINTLDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = String.format("%7d", totalINTLCntCR);
        buf = comcr.insertStr(buf, szTmp, 90);
        szTmp = String.format("%7d", (totalINTLCntDR+totalINTLCntCR));
        buf = comcr.insertStr(buf, szTmp, 100);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", (totalINTLDestAmtCR-totalINTLDestAmtDR));
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "自行(SB)帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusSBDestAmtDR);
        buf = comcr.insertStr(buf, szTmp, 30);
        szTmp = String.format("%7d", totalONUSCntDR);
        buf = comcr.insertStr(buf, szTmp, 55);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalOnusSBDestAmtCR);
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = String.format("%7d", totalONUSCntCR);
        buf = comcr.insertStr(buf, szTmp, 90);
        szTmp = String.format("%7d", (totalONUSCntDR+totalONUSCntCR));
        buf = comcr.insertStr(buf, szTmp, 100);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", (totalOnusSBDestAmtDR-totalOnusSBDestAmtCR));
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "__________總合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", (totalFISCDestAmtDR + totalNCCCDestAmtDR + totalINTLDestAmtDR + totalOnusSBDestAmtDR));
        buf = comcr.insertStr(buf, szTmp, 30);
        szTmp = String.format("%7d", (totalFISCCntDR + totalNCCCCntDR + totalINTLCntDR + totalONUSCntDR));
        buf = comcr.insertStr(buf, szTmp, 55);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", (totalFISCDestAmtCR + totalNCCCDestAmtCR + totalINTLDestAmtCR + totalOnusSBDestAmtCR));
        buf = comcr.insertStr(buf, szTmp, 65);
        szTmp = String.format("%7d", (totalFISCCntCR + totalNCCCCntCR + totalINTLCntCR +totalONUSCntCR));
        buf = comcr.insertStr(buf, szTmp, 90);
        szTmp = String.format("%7d", (totalFISCCntDR+totalFISCCntCR+totalNCCCCntDR+totalNCCCCntCR+totalINTLCntDR+totalINTLCntCR+totalONUSCntDR+totalONUSCntCR));
        buf = comcr.insertStr(buf, szTmp, 100);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", (totalFISCDestAmtCR + totalNCCCDestAmtCR + totalINTLDestAmtCR + totalOnusSBDestAmtDR - totalFISCDestAmtDR - totalNCCCDestAmtDR - totalINTLDestAmtDR - totalOnusSBDestAmtCR));
        buf = comcr.insertStr(buf, szTmp, 110);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨", 15);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨", 15);
        } else if ("07".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "07-預現", 15);
        } else if ("25".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "25-購貨沖銷", 15);
        } else if ("26".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "26-退貨沖銷", 15);
        } else if ("27".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "27-預現沖銷", 15);
        }
        
        buf = comcr.insertStr(buf, getValue("card_no"), 35);
        
        szTmp = comcr.commFormat("3$,3$,3$.2$", getValueDouble("dest_amt"));
        buf = comcr.insertStr(buf, szTmp, 55);
        if ("6".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "金資帳款__借", 75);
        		totalFISCDestAmtDR = totalFISCDestAmtDR + getValueDouble("dest_amt");
        		totalFISCCntDR = totalFISCCntDR + 1; 
        	} else {
        		buf = comcr.insertStr(buf, "金資帳款__貸", 75);
        		totalFISCDestAmtCR = totalFISCDestAmtCR + getValueDouble("dest_amt");
        		totalFISCCntCR = totalFISCCntCR + 1;
        	}
        } else if ("8".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "NCCC帳款__借", 75);
        		totalNCCCDestAmtDR = totalNCCCDestAmtDR + getValueDouble("dest_amt");
        		totalNCCCCntDR = totalNCCCCntDR + 1;
        	} else {
        		buf = comcr.insertStr(buf, "NCCC帳款__貸", 75);
        		totalNCCCDestAmtCR = totalNCCCDestAmtCR + getValueDouble("dest_amt");
        		totalNCCCCntCR = totalNCCCCntCR + 1;
        	}
        } else if ("0".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "國外帳款__借", 75);
        		totalINTLDestAmtDR = totalINTLDestAmtDR + getValueDouble("dest_amt");
        		totalINTLCntDR = totalINTLCntDR + 1;
        	} else {
        		buf = comcr.insertStr(buf, "國外帳款__貸", 75);
        		totalINTLDestAmtCR = totalINTLDestAmtCR + getValueDouble("dest_amt");
        		totalINTLCntCR = totalINTLCntCR + 1;
        	}
        } else if ("9".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		buf = comcr.insertStr(buf, "自行ＳＢ__貸", 75);
        		totalOnusSBDestAmtCR = totalOnusSBDestAmtCR + getValueDouble("dest_amt");
        		totalONUSCntCR = totalONUSCntCR + 1;
        	} else {
        		buf = comcr.insertStr(buf, "自行ＳＢ__借", 75);
        		totalOnusSBDestAmtDR = totalOnusSBDestAmtDR + getValueDouble("dest_amt");
        		totalONUSCntDR = totalONUSCntDR + 1;
        	}
        }
        
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

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
        BilRVD09 proc = new BilRVD09();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
