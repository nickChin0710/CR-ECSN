/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/07/24  V1.00.00   Zuwei      program initial                          *
*  112/08/30  V1.00.01   Wilson     本月消費金額改讀入帳日期                                                                    *
*  112/09/10  V1.00.02   Wilson     換行符號0A                                  *
*  112/11/10  V1.00.03   Wilson     日期減一天                                                                                              *
*****************************************************************************/
package Crd;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

public class CrdR107 extends AccessDAO {
    private final String PROGNAME = "產生信用卡持卡人明細月報表檔程式  112/11/10  V1.00.03";
    private CommCrd comc = new CommCrd();
    private CommCrd commCrd = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommCrdRoutine comcr = null;
    
    private final int OUTPUT_BUFF_SIZE = 55;
    private final String CRM_FOLDER = String.format("%s/media/crd", comc.getECSHOME());
    private final String DATA_FORM = "RCRM107";
    private String lineSeparator = "\n";

//    private CalBalance calBalance = null;
    private String searchDate = "";
    private String searchDateTw = "";
    private String searchDateTwYear = "";
    private String searchDateMonth = "";
    private String searchDateDay = "";
    private String sysTwDate = StringUtils.leftPad(commDate.twDate(), 7, "0");
    
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hLastDay = "";
    int totalCnt = 0;
    int pageCnt1 = 0;

    public int mainProcess(String[] args) {

        try {            
//            calBalance = new CalBalance(conn, getDBalias());
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I","","Usage CrdR107 [business_date]");

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            // get searchDate

            if (args.length >= 1) {
                searchDate  = args[0];
                showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
            } else {
                searchDate = new SimpleDateFormat("YYYYMMdd").format(new Date());
                searchDate = comcr.increaseDays(searchDate,-1);
            }
//            searchDate = getProgDate(searchDate, "D");
            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
            searchDateTw = StringUtils.leftPad(commDate.toTwDate(searchDate), 7, "0");
            searchDateTwYear = searchDateTw.substring(0, searchDateTw.length() - 4);
            searchDateMonth = searchDate.substring(searchDate.length() - 4).substring(0, 2);
            searchDateDay = searchDate.substring(searchDate.length() - 2);
            
            selectPtrBusinday();
            
            hLastDay = hEndDate;
            
            if (!searchDate.equals(hLastDay)) {
				showLogMessage("E", "", "報表日不為該月最後一天,不執行此程式");
				return 0;
            }

            // get the name and the path of the .DAT file
            String filename = String.format("%s.1.TXT", DATA_FORM);
            String fileFolder = Paths.get(CRM_FOLDER).toString();

            // 產生主要檔案
            int dataCount = generateFile(fileFolder, filename);

            // 產生Header檔
//            CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
//            dateTime(); // update the system date and time
//            boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, filename, searchDate,
//                    sysDate, sysTime.substring(0, 4), dataCount);
//            if (isGenerated == false) {
//                comc.errExit("產生HDR檔錯誤!", "");
//            }

            // CR_STATUS_YYMMDD.DAT -> CR_STATUS_YYMMDD.HDR
//            String hdrFileName =
//                    filename.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);

            // run FTP
            procFTP(fileFolder, filename, filename);
            
            // backup
            backup(filename);

            showLogMessage("I", "", "執行結束,筆數=[" + totalCnt + "]");
            return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }

    /**
     * generate a .Dat file
     * 
     * @param fileFolder 檔案的資料夾路徑
     * @param datFileName .dat檔的檔名
     * @return the number of rows written. If the returned value is -1, it means the path or the
     *         file does not exist.
     * @throws Exception
     */
    private int generateFile(String fileFolder, String datFileName)
            throws Exception {

        String datFilePath = Paths.get(fileFolder, datFileName).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (isOpen == false) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return -1;
        }

        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
                                   // specified value
        try {
            StringBuilder sb = new StringBuilder(10240);
            showLogMessage("I", "", "開始產生.TXT檔......");

            // 處理各分行信用卡持卡人流通卡相關資料
            showLogMessage("I", "", "開始處理各分行信用卡持卡人流通卡相關資料......");
            Map<String, Integer> creditAmtMap = getCreditAmount();
            Map<String, Integer> cardDestAmtMap = getMonthCardDestAmt();
            Map<String, Integer> overdueCountMap = getYearOverdueCount();
            selectGenBrnData();
            String lastBranch = "";
            int lineCnt = 0;
            
            while (fetchTable()) {
            	String branch = getValue("BRANCH");
                String pSeqno = getValue("P_SEQNO");
                String cardNo = getValue("CARD_NO");
                Integer balSum = creditAmtMap.get(pSeqno);
                Integer purchaseAmt = cardDestAmtMap.get(cardNo);
                Integer overdueCnt = overdueCountMap.get(pSeqno);

                if (!lastBranch.equals(branch)) {
                    if (lastBranch.length() > 0) {
                        countInEachBuffer += tail(sb, lineCnt);
                    }
                    lastBranch = branch;
                    pageCnt1 = 1;
                    countInEachBuffer += header(sb);
                    lineCnt = 0;
                }
                rowCount++;
                if (cardNo == null || cardNo.length() == 0) {
                    continue;
                }
            	lineCnt++;
            	
                if (rowCount % 5000 == 0 || rowCount == 1)                        	
                	showLogMessage("I", "", String.format("R107 Process 1 record=[%d]\n", rowCount));
                
                String rowOfDAT = getRowOfDAT(balSum, purchaseAmt, overdueCnt);
                sb.append(rowOfDAT);              

                countInEachBuffer++;                    
                totalCnt++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                	pageCnt1++;
                	sb.append(lineSeparator);
                	header(sb);
//                    showLogMessage("I", "",
//                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuilder();
                    countInEachBuffer = 0;
                }
            }
            closeCursor();

            countInEachBuffer += tail(sb, lineCnt);
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
//                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            } else {
                showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
            }

        } finally {
            closeBinaryOutput();
        }

        return rowCount;
    }
    
    private void selectPtrBusinday() throws Exception {
    	   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-5),'yyyymm')||'01' h_beg_date_bil ";
    	   sqlCmd += "     , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date_bil ";
    	   sqlCmd += "     , to_char(to_date(?,'yyyymmdd'),'yyyymm')||'01' h_beg_date ";
    	   sqlCmd +="      , to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
    	   sqlCmd += " from dual ";
    	   setString(1, searchDate);
    	   setString(2, searchDate);
    	   setString(3, searchDate);
    	   setString(4, searchDate);

    	   int recordCnt = selectTable();
    	   if(recordCnt > 0) {
    		  hBegDateBil = getValue("h_beg_date_bil");
    		  hEndDateBil = getValue("h_end_date_bil");
    	      hBegDate = getValue("h_beg_date");
    	      hEndDate = getValue("h_end_date");  
    	   }

    	   hChiYymmdd = commDate.toTwDate(searchDate);
    	   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , searchDate
    	           , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate));    	   
    }

    // 讀取資料
    private void selectGenBrnData() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT                                    ");
        sb.append("     E.BRANCH,                             ");
        sb.append("     E.FULL_CHI_NAME,                      ");
        sb.append("     A.REG_BANK_NO,                        ");
        sb.append("     B.ID_NO,                              ");
        sb.append("     D.ID_NO AS MAJOR_ID_NO,               ");
        sb.append("     B.CHI_NAME,                           ");
        sb.append("     A.BIN_TYPE,                           ");
        sb.append("     A.CARD_NO,                            ");
        sb.append("     A.ORI_ISSUE_DATE,                     ");
        sb.append("     A.NEW_END_DATE,                       ");
        sb.append("     C.LINE_OF_CREDIT_AMT,                 ");
        sb.append("     C.P_SEQNO,                            ");
        sb.append("     A.CURRENT_CODE,                       ");
        sb.append("     B.STAFF_FLAG,                         ");
        sb.append("     A.LAST_CONSUME_DATE                   ");
        sb.append(" FROM                                      ");
        sb.append("     CRD_CARD A,                           ");
        sb.append("     CRD_IDNO B,                           ");
        sb.append("     ACT_ACNO C,                           ");
        sb.append("     CRD_IDNO D,                           ");
        sb.append("     GEN_BRN E                             ");
        sb.append(" WHERE                                     ");
        sb.append("     A.ID_P_SEQNO = B.ID_P_SEQNO           ");
        sb.append("     AND A.REG_BANK_NO = E.BRANCH          ");
        sb.append("     AND A.ACNO_P_SEQNO = C.ACNO_P_SEQNO   ");
        sb.append("     AND A.MAJOR_ID_P_SEQNO = D.ID_P_SEQNO ");
        sb.append("     AND A.CURRENT_CODE = '0'              ");
        sb.append(" ORDER BY E.BRANCH                         ");

        sqlCmd = sb.toString();
        openCursor();
    }
    
    // 讀取循環信用金額
    private Map<String, Integer> getCreditAmount()
            throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT                                                                    ");
        sb.append("     A.P_SEQNO AS DEBT_END_BAL_P_SEQNO,                                    ");
        sb.append("     SUM(DECODE(B.INTEREST_METHOD, 'Y', A.END_BAL, 0)) AS DEBT_END_BAL_SUM ");
        sb.append(" FROM                                                                      ");
        sb.append("     ACT_DEBT A,                                                           ");
        sb.append("     PTR_ACTCODE B,                                                        ");
        sb.append("     PTR_WORKDAY C                                                         ");
        sb.append(" WHERE                                                                     ");
        sb.append("     A.ACCT_CODE = B.ACCT_CODE                                             ");
        sb.append("     AND A.STMT_CYCLE = C.STMT_CYCLE                                       ");
        sb.append("     AND A.ACCT_MONTH < C.THIS_ACCT_MONTH                                  ");
        sb.append(" GROUP BY                                                                  ");
        sb.append("     A.P_SEQNO                                                             ");

        sqlCmd = sb.toString();
        openCursor();
        Map<String, Integer> map = new HashMap<>();
        while (fetchTable()) {
            String pSeqno = getValue("DEBT_END_BAL_P_SEQNO");
            Integer balSum = getValueInt("DEBT_END_BAL_SUM");
            map.put(pSeqno, balSum);
        }
        closeCursor();
        return map;
    }
    
    // 讀取本月消費金額
    private Map<String, Integer> getMonthCardDestAmt()
            throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT                                                                       ");
        sb.append("     CARD_NO as DEST_CARD_NO,                                                 ");
        sb.append("     SUM(DECODE(SIGN_FLAG, '+', DEST_AMT, DEST_AMT *-1)) AS DEST_PURCHASE_AMT ");
        sb.append(" FROM                                                                    ");
        sb.append("     BIL_BILL                                                            ");
        sb.append(" WHERE                                                                   ");
        sb.append("     ACCT_CODE IN ('BL', 'ID', 'IT', 'AO', 'OT')                         ");
        sb.append("     AND POST_DATE BETWEEN ? AND ?                                       ");
        sb.append(" GROUP BY                                                                ");
        sb.append("     CARD_NO                                                             ");

        sqlCmd = sb.toString();
        setString(1 , hBegDate);
        setString(2 , hEndDate);
        openCursor();
        Map<String, Integer> map = new HashMap<>();
        while (fetchTable()) {
            String cardNo = getValue("DEST_CARD_NO");
            int puchaseAmt = getValueInt("DEST_PURCHASE_AMT");
            map.put(cardNo, puchaseAmt);
        }
        closeCursor();
        return map;
    }
    
    // 讀取最近一年逾期繳款次數
    private Map<String, Integer> getYearOverdueCount()
            throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT                               ");
        sb.append("     P_SEQNO AS OVERDUE_P_SEQNO,      ");
        sb.append("     COUNT(*) AS OVERDUE_CNT          ");
        sb.append(" FROM                                 ");
        sb.append("     ACT_PENALTY_LOG                  ");
        sb.append(" WHERE                                ");
        sb.append("     ACCT_MONTH BETWEEN ? AND ?       ");
        sb.append(" GROUP BY                             ");
        sb.append("     P_SEQNO                          ");

        sqlCmd = sb.toString();
        String lastMonth = commDate.monthAdd(searchDate, -12);
        setString(1, lastMonth);
        setString(2, comc.getSubString(searchDate, 0, 6));
        openCursor();
        Map<String, Integer> map = new HashMap<>();
        while (fetchTable()) {
            String pSeqno = getValue("OVERDUE_P_SEQNO");
            Integer overdueCnt = getValueInt("OVERDUE_CNT");
            map.put(pSeqno, overdueCnt);
        }
        closeCursor();
        return map;
    }
    
    private int header(StringBuilder sb) throws Exception {
        String branch = getValue("BRANCH");
        String fullChiName = getValue("FULL_CHI_NAME");
//        String cardNo = getValue("CARD_NO");
//        String groupCode = getValue("GROUP_CODE");
//        String regBankNo = getValue("REG_BANK_NO");
//        String chiName = getValue("CHI_NAME");
//        String idNo = getValue("ID_NO");
//        String actNo = getValue("ACT_NO");
        String str = "";
        String temp = "";        
        
        str = commCrd.fixLeft(branch, 10) + commCrd.fixLeft("CRM107", 16) + commCrd.fixLeft(searchDateTw + "信用卡持卡人明細月報表", 88) + commCrd.fixLeft("N", 8); 
        sb.append(str).append(lineSeparator);
        sb.append(commCrd.fixLeft(commCrd.fixRight("合作金庫商業銀行", 70), 132)).append(lineSeparator);
        str = commCrd.fixLeft("分行代號: 3144  信用卡部", 54) + commCrd.fixLeft("信用卡持卡人明細月報表", 62) + commCrd.fixLeft("保存年限: 一年", 16); 
        sb.append(str).append(lineSeparator);
        String strDate = "中華民國 " + searchDateTwYear + " 年 " + searchDateMonth + " 月 " + searchDateDay + " 日";
        temp = String.format("%04d", pageCnt1);
        str = "報表代號: " + commCrd.fixLeft("CRM107", 15) + commCrd.fixLeft("科目代號: ", 29) + commCrd.fixLeft(strDate, 56) + commCrd.fixRight("第" + temp + "頁",18);
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        sb.append(commCrd.fixLeft("分行別: " + branch + "  " + fullChiName, 132)).append(lineSeparator);
        sb.append("====================================================================================================================================").append(lineSeparator);
        str = " 正      卡                     （末六位）             卡片                                           最近一年                    ";
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        str = " 身份證字號  姓  名      卡別    卡    號   開戶日     到期日    信用額度    循環信用    本    月     逾期繳款  有效卡  卡片  員工";
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        str = "                                                       MMYY                  金    額    消費金額     次數      註  記  狀態  註記";
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        sb.append("====================================================================================================================================").append(lineSeparator);
        
        return 5;
    }

    private int tail(StringBuilder sb, int lineCnt) throws Exception {
        int line = 0;
        String str = "";
        if (lineCnt == 0) {
            sb.append(lineSeparator);
            str = "*** 查無資料 ***";
            sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
            sb.append(lineSeparator);
            line += 3;
        }
        sb.append("====================================================================================================================================").append(lineSeparator);
        sb.append(lineSeparator);
//        str = "    備註: （１）狀態為『重設密碼』者，請併客戶填具之『全球金融網（ＥＯＩ）最高權限管理者重設密碼申請書』一併保存。";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
//        str = "          （２）若無交易資料亦須列印歸檔。";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
//        sb.append(lineSeparator);
//        str = "製表單位: 資訊部                                                  經辦:                          核章: ";
//        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        if (lineCnt > 0) {
            str = "合計:          " + lineCnt + "卡";
            sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
            sb.append(lineSeparator);
            line += 1;
        }
        line += 1;
        return line;
    }

    /**
     * 
     * 產生檔案
     * 
     * @return String
     * @throws Exception
     */
    private String getRowOfDAT(Integer balSum, Integer purchaseAmt, Integer overdueCnt) throws Exception {
    	int cardNoCnt = 0;
    	
        // 分行代號    (1)讀取到的REG_BANK_NO
        String branch = getValue("REG_BANK_NO");
        // 分行名稱    (1)讀取到的FULL_CHI_NAME
        String fullChiName = getValue("FULL_CHI_NAME");
        String cardNo = getValue("CARD_NO");
        String groupCode = getValue("GROUP_CODE");
        String regBankNo = getValue("REG_BANK_NO");
        String chiName = getValue("CHI_NAME");
        String idNo = getValue("ID_NO");
        String actNo = getValue("ACT_NO");
        String lastConsumeDate = getValue("LAST_CONSUME_DATE");
//            (1)讀取到的BIN_TYPE為V  VISA卡
//            (1)讀取到的BIN_TYPE為M  MAST卡
//            (1)讀取到的BIN_TYPE為J  JCB卡
        String binType = getValue("BIN_TYPE");
        String cardType = "";
        switch (binType) {
            case "V":
                cardType = "VISA";
                break;
            case "M":
                cardType = "MAST";
                break;
            case "J":
                cardType = "JCB";
                break;
            default:
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(commCrd.fixLeft("", 1));
        sb.append(commCrd.fixLeft(idNo, 12)); // 正卡持卡人身份證字號
        sb.append(commCrd.fixLeft(chiName, 12)); // 持卡人姓名
        sb.append(commCrd.fixLeft(cardType, 8)); // 卡別
        String cardNo6 = "";
        if (cardNo.length() >= 16) {
            cardNo6 = cardNo.substring(10, 16);
        } else if (cardNo.length() >= 10) {
            cardNo6 = cardNo.substring(10);
        }
        sb.append(commCrd.fixLeft(cardNo6, 12)); // (末六位)卡號 (1)讀取到的CARD_NO第11~16碼
        sb.append(commCrd.fixLeft(getValue("ORI_ISSUE_DATE"), 10)); // 開戶日
        String newEndDate = getValue("NEW_END_DATE");
        String newEndDateMonth = "";
        if (newEndDate != null && newEndDate.length() >= 6) {
            newEndDateMonth = newEndDate.substring(4, 6) + newEndDate.substring(2, 4);
        }
        sb.append(commCrd.fixLeft(newEndDateMonth, 6)); // 卡片到期日MMYY  (1)讀取到的NEW_END_DATE第5~6碼 || (1)讀取到的NEW_END_DATE第3~4碼
        sb.append(commCrd.fixRight(String.format("%12d", getValueInt("LINE_OF_CREDIT_AMT")), 12)); // 信用額度    (1)讀取到的LINE_OF_CREDIT_AMT
        sb.append(commCrd.fixRight(String.format("%12d", balSum == null ? 0 : balSum), 12)); // 循環信用金額  (2)讀取到的DEBT_END_BAL_SUM
        sb.append(commCrd.fixRight(String.format("%12d", purchaseAmt == null ? 0 : purchaseAmt), 12)); // 本月消費金額    (3)讀取到的PURCHASE_AMT
        sb.append(commCrd.fixRight((overdueCnt == null ? 0 : overdueCnt) + "", 8)); // 最近一年逾期繳款次數    (4)讀取到的OVERDUE_CNT
        
        // 有效卡註記 判斷:
//        (5)讀取到的有效卡註記為Y  黑色星號
//        (5)讀取到的有效卡註記為N  空白
        if(comcr.str2long(lastConsumeDate) >= comcr.str2long(hBegDateBil)) {
        	cardNoCnt = 1;
        }
        
        sb.append(commCrd.fixLeft("", 8)); 
        sb.append(commCrd.fixLeft((cardNoCnt > 0) ? "*" : "", 8)); 
        sb.append(commCrd.fixLeft(getValue("CURRENT_CODE"), 6)); // 控管碼 卡片狀態碼   (1)讀取到的CURRENT_CODE
        // 員工註記 判斷:
//        (1)讀取到的STAFF_FLAG為Y  黑色星號
//        (1)讀取到的STAFF_FLAG不為Y  空白
        sb.append(commCrd.fixLeft("Y".equalsIgnoreCase(getValue("STAFF_FLAG")) ? "*" : "", 1)); 
        sb.append(lineSeparator);
        
        return sb.toString();
    }

    void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "BREPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = fileFolder;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
//        commFTP.hEriaLocalDir = String.format("%s/media/crm", comc.getECSHOME());
//        commFTP.hEriaRemoteDir = "crdatacrea/NEWCENTER";

        // 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
        String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

        showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
        int errCode = commFTP.ftplogName("BREPORT", ftpCommand);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
//            commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
//            commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
        }
    }

    /****************************************************************************/
    private void backup(String removeFileName) throws Exception {
        String tmpstr1 = CRM_FOLDER + "/" + removeFileName;
        String backupFilename = String.format(DATA_FORM + "_%s.1.TXT", searchDate);
        String tmpstr2 = CRM_FOLDER + "/backup/" + backupFilename;

        if (commCrd.fileRename2(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
    }

    public static void main(String[] args) {
      CrdR107 proc = new CrdR107();
      int retCode = proc.mainProcess(args);
      System.exit(retCode);
    }

}
