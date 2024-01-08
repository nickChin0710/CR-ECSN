/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/03/27  V1.00.01    JeffKung  program initial                           *
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

/*上傳分期請款檔失敗報表*/
public class BilRD49B extends AccessDAO {
    private String progname = "列印上傳分期請款檔失敗報表程式  112/03/27  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD49B";
    String prgmName = "列印上傳分期請款檔失敗報表程式";
    String rptName = "上傳分期請款檔失敗報表";
    String rptId = "CRD49B";
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
            
            //寫報表表頭
            printHeader();

            selectBilCurpost();

            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]"+ pageCnt);

            //只出線上報表
            comcr.insertPtrBatchRpt(lpar1);

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

    	sqlCmd =  "select card_no,settl_flag,dest_amt,purchase_date,txn_code,sign_flag,rsk_rsn, ";
    	sqlCmd += " auth_code,install_tot_term,install_first_amt,install_per_amt,mcht_no ";
    	sqlCmd += " from bil_curpost ";
		sqlCmd += "where this_close_date = ? ";
		sqlCmd += " and bill_type = 'FISC' ";
		sqlCmd += " and payment_type = 'I' ";
		sqlCmd += " and tx_convt_flag <> 'R' ";
		sqlCmd += " and rsk_rsn like 'I%' ";
		sqlCmd += " order by sign_flag,txn_code,settl_flag ";
		
		setString(1,hBusinssDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;

            if (indexCnt > 25) {
            	//分頁控制
                lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
            }

            printDetail();
        }

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
        buf = comcr.insertStr(buf, "交易摘要", 10);
        buf = comcr.insertStr(buf, "______卡號______", 37);
        buf = comcr.insertStr(buf, "_______交易金額", 55);
        buf = comcr.insertStr(buf, "期數", 75);
        buf = comcr.insertStr(buf, "授權碼", 81);
        buf = comcr.insertStr(buf, "特店代號_______", 90);
        buf = comcr.insertStr(buf, "失敗原因", 110);
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
        
        String srcDesc = "";
        if ("8".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		srcDesc = "--NCCC分期_貸";
        		totalNCCCDestAmtCR = totalNCCCDestAmtCR + getValueDouble("dest_amt");
        		totalNCCCCntCR = totalNCCCCntCR + 1;
        	} else {
        		srcDesc = "--NCCC分期_借";
        		totalNCCCDestAmtDR = totalNCCCDestAmtDR + getValueDouble("dest_amt");
        		totalNCCCCntDR = totalNCCCCntDR + 1;
        	}
        } else if ("9".equals(getValue("settl_flag"))) {
        	if ("-".equals(getValue("sign_flag"))) {
        		srcDesc = "--自行分期_貸";
        		totalONUSDestAmtCR = totalONUSDestAmtCR + getValueDouble("dest_amt");
        		totalONUSCntCR = totalONUSCntCR + 1;
        	} else {
        		srcDesc = "--自行分期_借";
        		totalONUSDestAmtDR = totalONUSDestAmtDR + getValueDouble("dest_amt");
        		totalONUSCntDR = totalONUSCntDR + 1;
        	}
        }
        
        if ("05".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "05-購貨"+srcDesc, 10);
        } else if ("06".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "06-退貨"+srcDesc, 10);
        } else if ("07".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "07-預現"+srcDesc, 10);
        } else if ("25".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "25-購貨沖銷"+srcDesc, 10);
        } else if ("26".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "26-退貨沖銷"+srcDesc, 10);
        } else if ("27".equals(getValue("txn_code")) ) {
        	buf = comcr.insertStr(buf, "27-預現沖銷"+srcDesc, 10);
        }
        
        buf = comcr.insertStr(buf, getValue("card_no"), 37);
        
        szTmp = comcr.commFormat("3$,3$,3$.2$", getValueDouble("dest_amt"));
        buf = comcr.insertStr(buf, szTmp, 55);
        
        szTmp = String.format("%2d", getValueInt("install_tot_term"));
        buf = comcr.insertStr(buf, szTmp, 75);
        
        buf = comcr.insertStr(buf, getValue("auth_code"), 81);

        buf = comcr.insertStr(buf, getValue("mcht_no"), 90);

        if ("I0".equals(getValue("rsk_rsn"))) {
        	buf = comcr.insertStr(buf, "卡號有誤", 110);
        } else if ("I1".equals(getValue("rsk_rsn"))) {
        	buf = comcr.insertStr(buf, "雙幣卡非台幣交易不可分期", 110);
        } else if ("I2".equals(getValue("rsk_rsn"))) {
        	buf = comcr.insertStr(buf, "疑義交易不可分期", 110);
        } else if ("I3".equals(getValue("rsk_rsn"))) {
        	buf = comcr.insertStr(buf, "不合格交易不可分期", 110);
        } else if ("I4".equals(getValue("rsk_rsn"))) {
        	buf = comcr.insertStr(buf, "不合格交易-金額計算錯誤", 110);
        } else if ("I5".equals(getValue("rsk_rsn"))) {
        	buf = comcr.insertStr(buf, "退貨比對不到原始交易", 110);
        } else if ("I6".equals(getValue("rsk_rsn"))) {
        	buf = comcr.insertStr(buf, "退貨金額不等於請款金額", 110);
        }
        
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

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
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilRD49B proc = new BilRD49B();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
