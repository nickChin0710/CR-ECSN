/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                   DESCRIPTION               *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112/07/18  V1.00.00    Bo Yang              program initial               *
 *  112/08/21  V1.00.01    Bo Yang              update log                    *
 *****************************************************************************/
package Mkt;

import Dxc.Util.SecurityUtil;
import com.*;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.text.DecimalFormat;

public class MktRt01 extends AccessDAO {
    private final String PROGNAME = "產生員工生日禮報表檔程式 112/08/21 V1.00.01";
    private CommCrd comc = new CommCrd();
    private CommCrd commCrd = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommString commStr = new CommString();
    private final int OUTPUT_BUFF_SIZE = 50;
    private final String CRM_FOLDER = String.format("%s/media/mkt", comc.getECSHOME());
    private final String DATA_FORM_FAILED = "RCRM181B";
    private final String REPORT_CODE_FAILED = "CRM181B";
    private final String DATA_FORM_SUCCEED = "RCRM181A";
    private final String REPORT_CODE_SUCCEED = "CRM181A";
    private final String lineSeparator = System.lineSeparator();

    //    private CalBalance calBalance = null;
    private String searchDate = "";
    private String searchDateTw = "";
    private String searchDateTwYear = "";
    private String searchDateMonth = "";
    private String searchDateDay = "";

    private int failCnt = 0;
    private int successCnt = 0;

    int headerRowCnt = 0;
    int tailRowCnt = 0;
    int pageCnt = 0;
    int totalCnt = 0;

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", "Usage MktRt01 [business_date]");

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================

            // get searchDate
            if (args.length >= 1) {
                searchDate = args[0];
                showLogMessage("I", "", String.format("程式參數1: [%s]", searchDate));
            } else {
                searchDate = selectPtrBusinday();
            }

            if (!commDate.isDate(searchDate)) {
                showLogMessage("I", "", "請傳入參數合格值yyyymmdd");
                return -1;
            }

            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
            searchDateTw = StringUtils.leftPad(commDate.toTwDate(searchDate), 7, "0");
            searchDateTwYear = commStr.mid(searchDateTw,0, searchDateTw.length() - 4);
//            searchDateMonth = searchDateTw.substring(searchDateTw.length() - 4).substring(0, 2);
            searchDateMonth = commStr.mid(searchDateTw,searchDateTw.length() - 4).substring(0, 2);            
//            searchDateDay = searchDateTw.substring(searchDateTw.length() - 2);
            searchDateDay = commStr.mid(searchDateTw,searchDateTw.length() - 2);

            // get the name and the path of the .DAT file

            // 判斷程式執行
            selectMktImloanList();
            if (failCnt == 0 && successCnt == 0) {
                showLogMessage("I", "", "變數failCnt = 0 && 變數successCnt = 0程式不執行");
                return 0;
            }

            // 產生失敗檔案
            String failFilename = String.format("%s.1.TXT", DATA_FORM_FAILED);
            String failFileFolder = Paths.get(CRM_FOLDER).toString();
            generateReport(failFileFolder, failFilename, false, REPORT_CODE_FAILED, "本行員工慶生活動費入刷卡金不成功明細表");

            // 產生成功檔案
            String successFilename = String.format("%s.1.TXT", DATA_FORM_SUCCEED);
            String successFolder = Paths.get(CRM_FOLDER).toString();
            generateReport(successFolder, successFilename, true, REPORT_CODE_SUCCEED, "本行員工慶生活動費入刷卡金成功明細表");

            // 產生Header檔
//            CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
//            dateTime(); // update the system date and time
//            boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, filename, searchDate,
//                    sysDate, sysTime.substring(0, 4), dataCount);
//            if (isGenerated == false) {
//                comc.errExit("產生HDR檔錯誤!", "");
//            }

            // CR_STATUS_YYMMDD.DAT -> CR_STATUS_YYMMDD.HDR
//            String hdrFileName = filename.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);

            showLogMessage("I", "", "執行結束, 總計筆數=[" + totalCnt + "]");
            return 0;
        } catch (Exception e) {
            expMethod = "mainProcess";
            expHandle(e);
            return exceptExit;
        } finally {
            finalProcess();
        }
    }

    private String selectPtrBusinday() throws Exception {
        sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
        selectTable();

        if (notFound.equals("Y")) {
            comc.errExit("執行結束, 營業日為空!!", "");
        }

        return getValue("BUSINESS_DATE");
    }

    private void selectMktImloanList() throws Exception {
        sqlCmd = "SELECT count(*) as fail_cnts FROM MKT_IMLOAN_LIST A " +
                " WHERE A.ERROR_CODE <> '00' " +
                "  AND  A.LIST_FLAG = '6' " +
                "  AND  A.PROC_FLAG = 'Y' " +
                "  AND  A.PROC_DATE  = ? ";
        setString(1, searchDate);
        selectTable();
        failCnt = getValueInt("fail_cnts");

        sqlCmd = "SELECT count(*) as sucess_cnts FROM MKT_IMLOAN_LIST A " +
                " WHERE A.ERROR_CODE = '00' " +
                "  AND  A.LIST_FLAG = '6' " +
                "  AND  A.PROC_FLAG = 'Y' " +
                "  AND  A.PROC_DATE  = ? ";
        setString(1, searchDate);
        selectTable();
        successCnt = getValueInt("sucess_cnts");
    }

    /**
     * generate a .Dat file
     *
     * @param fileFolder  檔案的資料夾路徑
     * @param datFileName .dat檔的檔名
     */
    private void generateReport(String fileFolder, String datFileName, boolean selectSuccess, String reportCode, String headerTitle) throws Exception {
        String datFilePath = Paths.get(fileFolder, datFileName).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (!isOpen) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return;
        }

        pageCnt = 1;
        int rowCount = 0;
        int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a
        // specified value
        try {
            StringBuffer sb = new StringBuffer(10240);
            if (selectSuccess) {
                showLogMessage("I", "", "開始產生成功報表.TXT檔......");
            } else {
                showLogMessage("I", "", "開始產生失敗報表.TXT檔......");
            }

            selectReport(selectSuccess);
            String lastBranch = "";
            int lineCnt = 0;
            while (fetchTable()) {
                String branch = getValue("BRANCH");
                String idNo = getValue("ID_NO");
                if (!lastBranch.equals(branch)) {
                    if (lastBranch.length() > 0) {
                        tail(sb, lineCnt, selectSuccess, true);
                        countInEachBuffer = 0;
                        pageCnt = 1;
                    }
                    lastBranch = branch;
                    header(sb, reportCode, selectSuccess, headerTitle);
                    countInEachBuffer += headerRowCnt;
                    lineCnt = 0;
                }
                rowCount++;
                if (idNo != null && idNo.length() > 0) {
                    lineCnt++;
                    String rowOfDAT = getRowOfDAT(selectSuccess);
                    sb.append(rowOfDAT);
                    countInEachBuffer++;
                    totalCnt++;
                }
                if (countInEachBuffer == OUTPUT_BUFF_SIZE - 3) {
                    tail(sb, lineCnt, selectSuccess, false);
                    countInEachBuffer += tailRowCnt;
                    lastBranch = "";
                }
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            if (lastBranch.length() > 0) {
                tail(sb, lineCnt, selectSuccess, true);
                countInEachBuffer += tailRowCnt;
            }
            closeCursor();

            // write the rest of bytes on the file
            if (countInEachBuffer > 0) {
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.DAT檔");
            } else {
                if (selectSuccess) {
                    showLogMessage("I", "", String.format("產生成功報表完成！，共產生%d筆資料", rowCount));
                } else {
                    showLogMessage("I", "", String.format("產生失敗報表完成！，共產生%d筆資料", rowCount));
                }
            }
        } finally {
            closeBinaryOutput();
        }

        // run FTP
        procFTP(fileFolder, datFileName);

        // backup
        backup(datFileName);

    }

    // 讀取各分行當日寫入主檔的員工信用卡製卡資料
    private void selectReport(boolean selectSuccess) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT C.BRANCH, ");
        sb.append("        C.FULL_CHI_NAME, ");
        sb.append("        D.STAFF_BRANCH, ");
        sb.append("        D.BRANCH_ACCTNO, ");
        sb.append("        D.ID_NO, ");
        sb.append("        D.NAME, ");
        if (selectSuccess) {
            sb.append("    D.CARD_NO, ");
            sb.append("    D.FEEDBACK_AMT ");
        } else {
            sb.append("    D.BANK_ACCTNO, ");
            sb.append("    D.ERROR_CODE  ");
        }
        sb.append("   FROM GEN_BRN C ");
        sb.append("   LEFT JOIN ( SELECT (lpad(A.BRANCH_ONLINENO,4,'0')||' '||A.BRANCH_NAME ) AS STAFF_BRANCH, ");
        sb.append("        A.BRANCH_ACCTNO, ");
        sb.append("        SUBSTRING(A.LIST_DATA, 1, 10)  AS ID_NO, ");
        sb.append("        A.NAME, ");
        if (selectSuccess) {
            sb.append("    A.CARD_NO, ");
            sb.append("    A.FEEDBACK_AMT, ");
        } else {
            sb.append("    A.BANK_ACCTNO, ");
            sb.append("    A.ERROR_CODE, ");
        }
        sb.append("        A.BRANCH_ONLINENO ");
        sb.append("   FROM MKT_IMLOAN_LIST A ");
        if (selectSuccess) {
            sb.append("   WHERE A.ERROR_CODE = '00' ");
        } else {
            sb.append("   WHERE A.ERROR_CODE <> '00' ");
        }
        sb.append("    AND  A.LIST_FLAG = '6' ");
        sb.append("    AND  A.PROC_FLAG = 'Y' ");
        sb.append("    AND  A.PROC_DATE  = ?   ");
        sb.append(" ) D ON  1=1  ");
        sb.append(" WHERE C.BRANCH IN ( '0010', '3144' ) ");
        sb.append(" ORDER BY C.BRANCH, D.BRANCH_ONLINENO, D.ID_NO ");

        sqlCmd = sb.toString();
        setString(1, searchDate); // 批次處理日期
        openCursor();
    }

    private void header(StringBuffer sb, String reportCode, boolean selectSuccess, String headerTitle) throws Exception {
        String branch = getValue("BRANCH");
        String fullChiName = getValue("FULL_CHI_NAME");
        String str = commCrd.fixLeft(branch, 10) + commCrd.fixLeft(reportCode, 16) + commCrd.fixLeft(searchDateTw + headerTitle, 88) + commCrd.fixLeft("N", 8);
        sb.append(str).append(lineSeparator);
        sb.append(commCrd.fixLeft(commCrd.fixRight("合作金庫商業銀行", 70), 132)).append(lineSeparator);
        str = commCrd.fixLeft(" 分行代號: " + branch + " " + fullChiName, 54) + commCrd.fixLeft(headerTitle, 62) + commCrd.fixLeft("保存年限: 三年", 16);
        sb.append(str).append(lineSeparator);
        String strDate = "中華民國 " + searchDateTwYear + " 年 " + searchDateMonth + " 月 " + searchDateDay + " 日";
        str = " 報表代號: " + commCrd.fixLeft(reportCode, 15) + commCrd.fixLeft("科目代號: ", 28) + commCrd.fixLeft(strDate, 56) + commCrd.fixRight(String.format("第%04d頁", pageCnt++), 18);
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
//        sb.append(commCrd.fixLeft(" 單位別: " + branch + "  " + fullChiName, 132)).append(lineSeparator);
        sb.append("====================================================================================================================================").append(lineSeparator);
        if (selectSuccess) {
            str = "     分行代號                       會計代號  員工ID      員工姓名      信用卡號          入刷卡金額      備註      ";
        } else {
            str = "     分行代號                       會計代號  員工ID      員工姓名      員存帳號         理由註記      備註      ";
        }
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        sb.append("====================================================================================================================================").append(lineSeparator);

        headerRowCnt = 7;
    }

    private void tail(StringBuffer sb, int lineCnt, boolean selectSuccess, boolean last) throws Exception {
        int line = 0;
        String str;
        if (lineCnt == 0) {
            sb.append(lineSeparator);
            str = "*** 查無資料 ***";
            sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
            sb.append(lineSeparator);
            line += 3;
        }
        sb.append("====================================================================================================================================").append(lineSeparator);
        if (lineCnt > 0) {
            if (!selectSuccess && last) {
                str = "說明：理由註記：01   未申請信用卡   02：無任一流通卡   03：其他";
                sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
                String totAmt = new DecimalFormat("#,###").format(failCnt * 1000L);
                str = "不成功總計： 筆數   " + failCnt + "    總金額   " + totAmt;
                sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
                sb.append(lineSeparator);
                line += 3;
            }
            str = "合計:          " + (last ? (selectSuccess ? successCnt : failCnt) : "") + " 筆";
            sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
            str = "製表單位: 資訊部                                                  經辦:                          核章: ";
            sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
            line += 2;
        }
        line += 1;
        tailRowCnt = line;
    }

    /**
     * 產生檔案
     *
     * @return String
     * @throws Exception
     */
    private String getRowOfDAT(boolean selectSuccess) throws Exception {
        StringBuffer sb = new StringBuffer();
        String staffBranch = getValue("STAFF_BRANCH");
        String branchAcctno = getValue("BRANCH_ACCTNO");
        String idNo = getValue("ID_NO");
        String name = getValue("NAME");

        if (selectSuccess) {
            String cardNo = getValue("CARD_NO");
            Double feedbackAmt = getValueDouble("FEEDBACK_AMT");

            sb.append(commCrd.fixLeft(staffBranch, 36)); // 分行代號
            sb.append(commCrd.fixLeft(branchAcctno, 10)); // 會計代號
            sb.append(commCrd.fixLeft(idNo, 12)); // 員工ID
            sb.append(commCrd.fixLeft(name, 14)); // 員工姓名
            sb.append(commCrd.fixLeft(commStr.mid(cardNo,0, 6) + "******" + commStr.mid(cardNo,12), 18)); // 信用卡號
            sb.append(commCrd.fixLeft(new DecimalFormat("#,###").format(feedbackAmt), 16)); // 入刷卡金額
            sb.append(commCrd.fixLeft("", 20)); // 備註
        } else {
            String bankAcctno = getValue("BANK_ACCTNO");
            String errorCode = getValue("ERROR_CODE");

            sb.append(commCrd.fixLeft(staffBranch, 36)); // 分行代號
            sb.append(commCrd.fixLeft(branchAcctno, 10)); // 會計代號
            sb.append(commCrd.fixLeft(idNo, 12)); // 員工ID
            sb.append(commCrd.fixLeft(name, 14)); // 員工姓名
            sb.append(commCrd.fixLeft(commStr.mid(bankAcctno,0, 4) + "-" + commStr.mid(bankAcctno,4, 7) + "-" + commStr.mid(bankAcctno,7, 17) ,15 )); // 員存帳號
            sb.append(commCrd.fixLeft(errorCode, 14)); // 理由註記
            sb.append(commCrd.fixLeft("", 20)); // 備註
        }
        sb.append(lineSeparator);

        return sb.toString();
    }

    void procFTP(String fileFolder, String datFileName) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "BREPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = fileFolder;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;

        // 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
        String ftpCommand = String.format("mput %s", datFileName);

        showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
        int errCode = commFTP.ftplogName("BREPORT", ftpCommand);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
            commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
        }
    }

    /****************************************************************************/
    private void backup(String removeFileName) {
        String tmpStr1 = CRM_FOLDER + "/" + removeFileName;
        String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
        String tmpStr2 = CRM_FOLDER + "/backup/" + String.format(removeFileName + "_%s", sysDate + sysTime);
        String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

        if (!comc.fileCopy(tempPath1, tempPath2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]備份失敗!");
            return;
        }
        comc.fileDelete(tmpStr1);
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 備份至 [" + tempPath2 + "]");
    }

    public static void main(String[] args) {
        MktRt01 proc = new MktRt01();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
}
