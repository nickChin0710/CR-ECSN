/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                   DESCRIPTION               *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112/08/24  V1.00.00    Bo Yang              program initial               *
 *  112/08/29  V1.00.01    Grace Huang          微調各欄位間距, 金額右靠         *
 *  112/08/30  V1.00.02    Bo Yang              報表記錄加入PTRR_0000統一作業    *
 *****************************************************************************/
package Gen;

import Dxc.Util.SecurityUtil;
import com.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenA007 extends AccessDAO {
    private final String PROGNAME = "數位帳戶VD卡扣款回覆檔明細 112/08/24 V1.00.01";
    private CommCrd comc = new CommCrd();
    private CommDate commDate = new CommDate();
    private CommCrd commCrd = new CommCrd();
    private CommCrdRoutine comcr = null;

    private final String CRM_FOLDER = "/media/gen";
    private final String DATA_FORM = "VDD04_RSP.YYYYMMDD";
    private final String FILE_NAME = "GenA007";
    private final String lineSeparator = System.lineSeparator();

    private String searchDate = "";

    int totCnt = 0;
    List<Map<String, Object>> lpar = new ArrayList<>();
    String rptId = "GenA007";
    String rptName = "數位帳戶VD卡扣款回覆檔處理";
    int rptSeq = 0;

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            showLogMessage("I", "", "Usage GenA007 [business_date]");

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            // get searchDate
            if (args.length >= 1) {
                searchDate = args[0];
                showLogMessage("I", "", String.format("程式參數1: [%s]", searchDate));
            } else {
                searchDate = selectPtrBusinday();
            }

            if (!commDate.isDate(searchDate)) {
                showLogMessage("I", "", "請傳入參數合格值: YYYYMMDD");
                return -1;
            }

            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

            String fileName = DATA_FORM.replace("YYYYMMDD", searchDate);
            // get the name and the path of the .DAT file
            if (openFile(fileName) == 0) {
                readFile(fileName);
            }

            showLogMessage("I", "", "執行結束, 總計筆數=[" + totCnt + "]");
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

    //=============================================================================
    int openFile(String filename) {
        String path = String.format("%s%s/%s", comc.getECSHOME(), CRM_FOLDER, filename);
        path = Normalizer.normalize(path, Normalizer.Form.NFKD);

        int rec = openInputText(path);
        if (rec == -1) {
            showLogMessage("D", "", "無檔案可處理  " + "");
            return 1;
        }

        closeInputText(rec);
        return (0);
    }

    //=============================================================================
    void readFile(String filename) throws Exception {
        String datFilePath = Paths.get(comc.getECSHOME() + CRM_FOLDER, FILE_NAME).toString();
        boolean isOpen = openBinaryOutput(datFilePath);
        if (!isOpen) {
            showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
            return;
        }

        try {
            showLogMessage("I", "", "======== Start Read File ========");
            BufferedReader bufferedReader;
            try {
                String tmpStr = String.format("%s%s/%s", comc.getECSHOME(), CRM_FOLDER, filename);
                String tempPath = SecurityUtil.verifyPath(tmpStr);
                FileInputStream fileInputStream = new FileInputStream(tempPath);
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "MS950"));

                showLogMessage("I", "", "  tempPath = [" + tempPath + "]");
            } catch (FileNotFoundException exception) {
                showLogMessage("I", "", "bufferedReader exception: " + exception.getMessage());
                return;
            }

            String lineLength;
            String buf = "";
            while ((lineLength = bufferedReader.readLine()) != null) {
                if (lineLength.length() < 10) {
                    continue;
                }
                byte[] bytes = lineLength.getBytes("MS950");
                String detInfo = comc.subMS950String(bytes, 0, 1).trim();

                // 排除首筆(第1碼=1)/尾筆(第1碼=3)資料
                if (!"2".equals(detInfo)) {
                    continue;
                }

                totCnt++;
                String bankCode = comc.subMS950String(bytes, 1, 3).trim();
                String tradeCode = comc.subMS950String(bytes, 4, 3).trim();
                String circleNum = comc.subMS950String(bytes, 7, 10).trim();
                String tradeDate = comc.subMS950String(bytes, 17, 8).trim();
                String amt = comc.subMS950String(bytes, 25, 12).trim();
                String amtNo = comc.subMS950String(bytes, 37, 13).trim();
                String vdCardNo = comc.subMS950String(bytes, 50, 16).trim();
                String debitNo = comc.subMS950String(bytes, 66, 15).trim();
                String idNo = comc.subMS950String(bytes, 81, 10).trim();
                String replyCode = comc.subMS950String(bytes, 91, 2).trim();
                String replyCodeDesc = selectReplyCodeDesc(replyCode);
                String cardNo = comc.subMS950String(bytes, 93, 16).trim();
                String specType = comc.subMS950String(bytes, 109, 4).trim();
                String specCode = comc.subMS950String(bytes, 113, 15).trim();
                String authCode = comc.subMS950String(bytes, 128, 6).trim();
                String reserveBit = comc.subMS950String(bytes, 134, 30).trim();
                String summaryCode = comc.subMS950String(bytes, 164, 4).trim();
                String tradeDescEN = comc.subMS950String(bytes, 168, 16).trim();
                String tradeDescCN = comc.subMS950String(bytes, 184, 16).trim();

                buf = header(buf);
                for (String s : buf.split(lineSeparator)) {
                    lpar.add(comcr.putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", s + lineSeparator));
                }
                /*
                buf += commCrd.fixLeft("", 4);
                buf += commCrd.fixLeft(tradeCode, 3);
                buf += commCrd.fixLeft(circleNum, 10);
                buf += commCrd.fixLeft(tradeDate, 8);
                buf += commCrd.fixLeft(amt, 12);
                buf += commCrd.fixLeft(amtNo, 13);
                buf += commCrd.fixLeft(vdCardNo, 16);
                buf += commCrd.fixLeft(debitNo, 15);
                buf += commCrd.fixLeft(idNo, 10);
                buf += commCrd.fixLeft(replyCode, 2);
                buf += commCrd.fixLeft(replyCodeDesc, 40);
                buf += commCrd.fixLeft(summaryCode, 4);
                buf += commCrd.fixLeft(tradeDescCN, 16);
                */
                String buf1 = "";
                buf1 += commCrd.fixLeft(tradeCode, 7);
                buf1 += commCrd.fixLeft(circleNum, 12);
                buf1 += commCrd.fixLeft(tradeDate, 8);
                //buf += commCrd.fixLeft(amt, 12);			//金額(文字, 左靠)             
                Long tmpamt = commCrd.str2long(amt);
                buf1 += commCrd.fixRight(tmpamt + "", 10) + " ";    //金額(數字, 右靠, 去除前面0)
                buf1 += commCrd.fixLeft(amtNo, 15);
                buf1 += commCrd.fixLeft(vdCardNo, 18);
                buf1 += commCrd.fixLeft(debitNo, 12);        //解圈扣款流水號
                buf1 += commCrd.fixLeft(idNo, 12);
                buf1 += commCrd.fixLeft(replyCode, 4);        //處理回覆碼
                buf1 += commCrd.fixLeft(replyCodeDesc, 10);    //處理回覆說明
                buf1 += commCrd.fixLeft(summaryCode, 6);        //摘要代號
                buf1 += commCrd.fixLeft(tradeDescCN, 50);

                for (String s : buf1.split(lineSeparator)) {
                    lpar.add(comcr.putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "1", s + lineSeparator));
                }

                buf += buf1;
            }

            if (totCnt == 0) {
                showLogMessage("I", "", " VDD04_RSP.YYYYMMDD 檔案內容空檔 !! ");
            }

            byte[] tmpBytes = buf.getBytes("MS950");
            writeBinFile(tmpBytes, tmpBytes.length);

            comcr.deletePtrBatchRpt(rptId, sysDate);
            comcr.insertPtrBatchRpt(lpar);

            bufferedReader.close();
        } finally {
            closeBinaryOutput();
        }

        //ftpProc(FILE_NAME, "NCR2TCB", "NCR2TCB");
        ftpProc(FILE_NAME, "NCR2EMP", "NCR2EMP");
    }

    private String header(String str) throws Exception {
        str += "報表代號: " + commCrd.fixLeft(FILE_NAME, 42) + commCrd.fixLeft("數位帳戶VD卡扣款回覆檔處理", 26) + commCrd.fixRight("頁次: 1", 54) + lineSeparator;
        str += "單位: " + commCrd.fixLeft("3144", 102) + commCrd.fixLeft(String.format("報表日期: %s年%2$s月%3$s日", searchDate.substring(0, 4), searchDate.substring(4, 6), searchDate.substring(6, 8)), 24) + lineSeparator;
        str += lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;
        str += "交易代碼  圈存序號   交易日期    金額   金融帳號     VD卡號       解圈扣款流水號  帳戶歸屬的ID 處理回覆碼/說明 摘要代號 交易中文說明" + lineSeparator;
        str += "====================================================================================================================================" + lineSeparator;
        return str;
    }

    private String selectReplyCodeDesc(String replyCode) throws Exception {
        sqlCmd = " select wf_desc from ptr_sys_idtab " +
                "  where wf_type = 'DBAR0010' " +
                "    and wf_id = ? ";
        setString(1, replyCode);

        selectTable();

        if (notFound.equals("Y")) {
            return "";
        }

        return getValue("wf_desc");
    }

    // ************************************************************************
    private void ftpProc(String filename, String systemId, String refIpCode) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = systemId; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + CRM_FOLDER;    //相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        String hEflgRefIpCode = refIpCode; //"NCR2EMP";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName(hEflgRefIpCode, tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + hEflgRefIpCode + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "GenA007 執行完成 傳送EMP失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        backup(filename);
    }

    /****************************************************************************/
    private void backup(String removeFileName) {
        String tmpStr1 = comc.getECSHOME() + CRM_FOLDER + "/" + removeFileName;
        String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
        String tmpStr2 = comc.getECSHOME() + CRM_FOLDER + "/backup/" + String.format(removeFileName + "_%s", sysDate + sysTime);
        String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

        if (!comc.fileCopy(tempPath1, tempPath2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]備份失敗!");
            return;
        }
        comc.fileDelete(tmpStr1);
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 備份至 [" + tempPath2 + "]");
    }

    public static void main(String[] args) {
        GenA007 proc = new GenA007();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
}
