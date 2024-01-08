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

/*信用卡繳學費明細表*/
public class BilRD93 extends AccessDAO {
    private String progname = "列印信用卡繳學費明細表程式  112/03/22  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilRD93";
    String prgmName = "列印信用卡繳學費明細表程式";
    String rptName = "信用卡繳學費明細表";
    String rptId = "BILR_D93";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
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

    double totalNCCCDestAmt = 0;
    double totalFISCDestAmt = 0;
    double totalVDDestAmt = 0;
    double totalCCDestAmt = 0;

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

    	sqlCmd =  " (select this_close_date,card_no,purchase_date,settl_flag,decode(sign_flag,'-',dest_amt*-1,dest_amt) dest_amt ,sign_flag, 'CC' as card_group";
		sqlCmd += "  from bil_curpost ";
		sqlCmd += "  where ecs_platform_kind in('b1','G2') and this_close_date = ? ) ";
		sqlCmd += " union all ";
		sqlCmd += " (select this_close_date,card_no,purchase_date,settl_flag,decode(sign_flag,'-',dest_amt*-1,dest_amt) dest_amt,sign_flag,'VD' as card_group ";
		sqlCmd += "  from dbb_curpost ";
		sqlCmd += "  where ecs_platform_kind in('b1','G2') and this_close_date = ? ) ";
		sqlCmd += " order by  card_group,card_no";
		
		setString(1,hBusinssDate);
		setString(2,hBusinssDate);
		
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
        buf = comcr.insertStrCenter(buf, "信用卡繳學費明細表", 132);
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
        buf = comcr.insertStr(buf, "卡號", 21);
        buf = comcr.insertStr(buf, "金額", 41);
        buf = comcr.insertStr(buf, "清算單位", 61);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printFooter() {
    	
    	buf = "";
    	lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	buf = "";
    	lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    	
        buf = "";
        buf = comcr.insertStr(buf, "金資帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalFISCDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "ＮＣＣＣ帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalNCCCDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "ＶＤ＿＿帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalVDDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "非ＶＤ＿帳款合計:", 1);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", totalCCDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
        buf = comcr.insertStr(buf, getValue("card_no"), 21);
        szTmp = comcr.commFormat("3$,3$,3$.2$", getValueDouble("dest_amt"));
        buf = comcr.insertStr(buf, szTmp, 41);
        if ("6".equals(getValue("settl_flag"))) {
        	buf = comcr.insertStr(buf, "金資帳款", 61);
        	totalFISCDestAmt = totalFISCDestAmt + getValueDouble("dest_amt");
        } else {
        	buf = comcr.insertStr(buf, "NCCC帳款", 61);
        	totalNCCCDestAmt = totalNCCCDestAmt + getValueDouble("dest_amt");
        }
        
        if ("VD".equals(getValue("card_group"))) {
        	totalVDDestAmt = totalVDDestAmt + getValueDouble("dest_amt");
        } else {
        	totalCCDestAmt = totalCCDestAmt + getValueDouble("dest_amt");
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
        BilRD93 proc = new BilRD93();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
