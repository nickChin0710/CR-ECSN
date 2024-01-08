/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/09/21  V1.00.00  Zuwei Su    program initial                                                 *   
 *  112/10/04  V1.00.01  Grace       修訂 selectBilBillData() 條件                                                                                                                  *
 *****************************************************************************************************/
package Mkt;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class MktC915 extends AccessDAO {
    private final String PROGNAME = "漢來美食世界卡生日禮回饋資料篩選處理程式  112/09/21 V1.00.00";
    CommFunction comm = new CommFunction();
    private CommCrd comc = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommString commStr = new CommString();
    private CommFTP commFTP = null;
    private CommRoutine comr;
    private CommCrdRoutine comcr;

    private static final String PATH_FOLDER = "/media/mkt/";

    private String hBusinessDate = "";
    private int totCnt = 0;
    private int fptr1 = -1;

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", "Usage MktC915 [business_date]");

            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length >= 1) {
                hBusinessDate = args[0];
                showLogMessage("I", "", String.format("程式參數1: [%s]", hBusinessDate));
            } else {
                // get searchDate
                selectPtrBusinday();
                hBusinessDate = businessDate;
            }

            if (!commDate.isDate(hBusinessDate)) {
                showLogMessage("I", "", "請傳入參數合格值: YYYYMMDD");
                return -1;
            }

            showLogMessage("I", "", String.format("執行日期[%s]", hBusinessDate));

            String data = selectBilBillData();
            String filename = "R_WORLDBRD_YYYYMMDD.txt".replace("YYYYMMDD", hBusinessDate);
            writeReport(filename, data);
            
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            comcr.callbatchEnd();
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    private void selectPtrBusinday() throws Exception {
        sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
        selectTable();

        if (notFound.equals("Y")) {
            comc.errExit("執行結束, 營業日為空!!", "");
        }

        businessDate = getValue("BUSINESS_DATE");
    }

    private String selectBilBillData() throws Exception {
        sqlCmd = " SELECT                                                                                                                                "
                // + " --a.ACCT_TYPE, c.BIRTHDAY, a.MCHT_CHI_NAME, "
                //+ " a.P_SEQNO, a.ID_P_SEQNO, c.ID_NO, c.CHI_NAME, SUBSTRING(a.GROUP_CODE, LENGTH(a.GROUP_CODE) - 2) as type, a.CARD_NO, c.CELLAR_PHONE,                                 "
                + " c.ID_NO, SUBSTRING(a.GROUP_CODE, LENGTH(a.GROUP_CODE) - 2) as type, a.MAJOR_CARD_NO, c.CELLAR_PHONE,   "
                + " d.BILL_SENDING_ZIP, "
                + " c.CHI_NAME, "
                + " d.BILL_SENDING_ADDR1||d.BILL_SENDING_ADDR2||d.BILL_SENDING_ADDR3||d.BILL_SENDING_ADDR4||d.BILL_SENDING_ADDR5  as bill_sending_addr     "
                //+ " , sum(decode(a.SIGN_FLAG, '-', -1*a.CASH_PAY_AMT, a.CASH_PAY_AMT)) as amt_sum  "
                + " FROM BIL_BILL a                                                     "
                //+ " LEFT JOIN CRD_CARD b ON b.CARD_NO=a.CARD_NO                         "
                //+ " LEFT JOIN CRD_IDNO c ON c.ID_P_SEQNO=a.ID_P_SEQNO                   "	
                + " LEFT JOIN CRD_CARD b ON b.CARD_NO=a.MAJOR_CARD_NO                   "
                + " LEFT JOIN CRD_IDNO c ON c.ID_P_SEQNO=a.MAJOR_ID_P_SEQNO             "	
                + " LEFT JOIN ACT_ACNO d ON d.P_SEQNO=a.P_SEQNO                         "
                + " WHERE                                                               "
                + " a.ACCT_TYPE='01'        " // 一般卡
                + " AND a.GROUP_CODE='1631'                " // --漢來美食世界卡
                + " AND a.PURCHASE_DATE>=? AND a.PURCHASE_DATE<=?    " // --消費日=營業日的前2個月
                + " AND SUBSTRING(c.BIRTHDAY,5,2) = ?     " // --營業日-月份 (每月10日處理營業日當月生日者)
//                + " --AND a.CARD_NO IN ('5452020100367101')                                                       "
                //+ " AND (b.CURRENT_CODE='0'           OR (b.CURRENT_CODE<>'0'             AND b.OPPOST_DATE>=? ))   " // --有效卡(or 消費日之後停卡者)
                + " AND ( b.CURRENT_CODE IN ('0','5')  OR (b.CURRENT_CODE NOT IN ('0','5') AND b.OPPOST_DATE>=? ))   "	// --有效卡(or 消費日之後停卡者, 含偽卡者)
                + " AND ( a.mcht_category <> '9311' )                                                               "	//排除稅賦項目
                + " AND ( SUBSTRING(a.MCHT_CHI_NAME,1,1) NOT IN ('$','＄','#','＃') )	                                "	//排除第一碼為 $、#
                + " AND SUBSTRING(a.MCHT_CHI_NAME,1,2) NOT IN                                                       "	//排除第1, 2碼
                + " ('f%','ｆ％','G%','Ｇ％','d%','ｄ％','M%','Ｍ％','b%','ｂ％','e%','ｅ％','V%','Ｖ％','U%','Ｕ％','A%','Ａ％','$%','＄％','#%','＃％') "
                //+ " GROUP BY a.P_SEQNO, a.ID_P_SEQNO, c.ID_NO, c.CHI_NAME, SUBSTRING(a.GROUP_CODE, LENGTH(a.GROUP_CODE) - 2), a.CARD_NO, c.CELLAR_PHONE,                        "
                //+ " d.BILL_SENDING_ZIP, d.BILL_SENDING_ADDR1||d.BILL_SENDING_ADDR2||d.BILL_SENDING_ADDR3||d.BILL_SENDING_ADDR4||d.BILL_SENDING_ADDR5      "
                + " GROUP BY c.ID_NO, SUBSTRING(a.GROUP_CODE, LENGTH(a.GROUP_CODE) - 2), a.MAJOR_CARD_NO, c.CELLAR_PHONE,                                             "
                + " d.BILL_SENDING_ZIP, c.CHI_NAME, d.BILL_SENDING_ADDR1||d.BILL_SENDING_ADDR2||d.BILL_SENDING_ADDR3||d.BILL_SENDING_ADDR4||d.BILL_SENDING_ADDR5      "
                + " HAVING sum(decode(a.SIGN_FLAG, '-', -1*a.CASH_PAY_AMT, a.CASH_PAY_AMT)) >= 20000          " // --一般消費累積超逾2萬元
                + " ORDER BY a.MAJOR_CARD_NO  ";
        String purchaseDateStart = commDate.monthAdd(hBusinessDate, -2) + "01";
        String purchaseDateEnd = commDate.dateAdd(hBusinessDate.substring(0, 6) + "01", 0, 0, -1);
        String month = hBusinessDate.substring(4, 6);
        setString(1, purchaseDateStart);
        setString(2, purchaseDateEnd);
        setString(3, month);
        //setString(4, hBusinessDate);
        setString(4, purchaseDateStart);	//改以 消費日之後停卡者, 仍視為有效
        showLogMessage("I", "", "BIL_BILL 查詢參數: ");
        showLogMessage("I", "", "  purchase date begin: " + purchaseDateStart);
        showLogMessage("I", "", "  purchase date end: " + purchaseDateEnd);
        showLogMessage("I", "", "  birthday month: " + month);
        showLogMessage("I", "", "  oppost date: " + purchaseDateStart);
        int cnt = selectTable();
        totCnt = cnt;
        StringBuilder sb = new StringBuilder(10240);
        for (int i = 0; i < cnt; i++) {
            String idNo = getValue("ID_NO", i);
            String type = getValue("type", i);
            //String cardNo = getValue("card_no", i);
            String cardNo = getValue("MAJOR_CARD_NO", i);
            String cellarPhone = getValue("CELLAR_PHONE", i);
            String billSendingZip = getValue("BILL_SENDING_ZIP", i);
            String chiName = getValue("chi_name", i);
            String billSendingAddr = getValue("BILL_SENDING_ADDR", i);            

            sb.append(commStr.rpad(idNo, 10)).append(",");
            sb.append(commStr.rpad(type, 3)).append(",");
            sb.append(commStr.rpad(cardNo, 16)).append(",");
            sb.append(commStr.rpad(cellarPhone, 15)).append(",");
            sb.append(commStr.rpad(billSendingZip, 3)).append(",");
            sb.append(commStr.rpad(chiName, 20)).append(",");
            sb.append(commStr.rpad(billSendingAddr, 20)).append("\n");
        }
        
        return sb.toString();
    }

    private void writeReport(String filename, String filedata) throws Exception {
        fileOpen(filename);
        showLogMessage("I", "", "開始寫入檔案: " + filename);
        writeTextFile(fptr1, filedata);
        closeOutputText(fptr1);
        ftpProc(filename);
    }

    /*******************************************************************/
    private void fileOpen(String filename) throws Exception {
        String tempStr1 = String.format("%s%s%s", comc.getECSHOME(), PATH_FOLDER, filename);
        String fileName = Normalizer.normalize(tempStr1, java.text.Normalizer.Form.NFKD);
        fptr1 = openOutputText(fileName, "MS950");

        if (fptr1 == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void ftpProc(String filename) throws Exception {
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "TOHOST"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + PATH_FOLDER; // 相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName("CREDITCARD", tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + "CREDITCARD" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktC915 執行完成 傳送  CREDITCARD 失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        renameFile(filename);
    }

    // ************************************************************************
    private void renameFile(String removeFileName) throws Exception {
        String tmpStr1 = comc.getECSHOME() + PATH_FOLDER + removeFileName;
        String tmpStr2 = comc.getECSHOME() + PATH_FOLDER + removeFileName + "." + sysDate + sysTime;

        if (!comc.fileRename2(tmpStr1, tmpStr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpStr2 + "]");
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC915 proc = new MktC915();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
