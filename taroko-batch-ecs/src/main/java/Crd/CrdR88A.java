/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/27  V1.00.00   Zuwei      program initial                          *
*  112/03/28  V1.00.01   Zuwei      layout 調整                          *
*  112/04/12  V1.00.02   Wilson     產檔路徑參數調整為BREPORT                      *
*  112/09/10  V1.00.03   Wilson     換行符號0A                                  *
*****************************************************************************/
package Crd;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommTxInf;
import Cca.CalBalance;

public class CrdR88A extends AccessDAO {
    private final String PROGNAME = "產生員工國際信用卡核發備查簿報表檔程式  112/09/10  V1.00.03";
    private CommCrd comc = new CommCrd();
    private CommCrd commCrd = new CommCrd();
    private CommDate commDate = new CommDate();
    
    private final int OUTPUT_BUFF_SIZE = 55;
    private final String CRM_FOLDER = String.format("%s/media/crd", comc.getECSHOME());
    private final String DATA_FORM = "RCRD88A";
    private String lineSeparator = "\n";

//    private CalBalance calBalance = null;
    private String searchDate = "";
    private String searchDateTw = "";
    private String searchDateTwYear = "";
    private String searchDateMonth = "";
    private String searchDateDay = "";
    private String sysTwDate = StringUtils.leftPad(commDate.twDate(), 7, "0");
    
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
            showLogMessage("I","","Usage CrdR88A [business_date]");

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================

            // get searchDate

            if (args.length >= 1) {
                searchDate  = args[0];
                showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
            } else {
                searchDate = new SimpleDateFormat("YYYYMMdd").format(new Date());
            }
//            searchDate = getProgDate(searchDate, "D");
            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
            searchDateTw = StringUtils.leftPad(commDate.toTwDate(searchDate), 7, "0");
            searchDateTwYear = searchDateTw.substring(0, searchDateTw.length() - 4);
            searchDateMonth = searchDateTw.substring(searchDateTw.length() - 4).substring(0, 2);
            searchDateDay = searchDateTw.substring(searchDateTw.length() - 2);

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
            StringBuffer sb = new StringBuffer(10240);
            showLogMessage("I", "", "開始產生.TXT檔......");

            // 處理各分行當日寫入主檔的員工信用卡製卡資料
            showLogMessage("I", "", "開始處理各分行當日寫入主檔的員工信用卡製卡資料......");
            selectCrdEmbossData();
            String lastBranch = "";
            int lineCnt = 0;
            while (fetchTable()) {
                String branch = getValue("BRANCH");
                String cardNo = getValue("CARD_NO");
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
                if (cardNo != null && cardNo.length() > 0) {
                	lineCnt++;
                    String rowOfDAT = getRowOfDAT();
                    sb.append(rowOfDAT);
                    countInEachBuffer++;                    
                    totalCnt++;
                }
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                	pageCnt1++;
                	sb.append(lineSeparator);
                	header(sb);
                	
//                    showLogMessage("I", "",
//                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
            }
            if (lastBranch.length() > 0) {
                countInEachBuffer += tail(sb, lineCnt);
            }
            closeCursor();
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
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

    // 讀取各分行當日寫入主檔的員工信用卡製卡資料
    private void selectCrdEmbossData() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("   SELECT  ");
        sb.append("        C.BRANCH, ");
        sb.append("        C.FULL_CHI_NAME, ");
        sb.append("        A.CARD_NO, ");
        sb.append("        A.GROUP_CODE, ");
        sb.append("        A.REG_BANK_NO, ");
        sb.append("        B.CHI_NAME, ");
        sb.append("        B.ID_NO, ");
        sb.append("        A.ACT_NO, ");
        sb.append("        A.IN_MAIN_DATE ");
        sb.append("   FROM CRD_EMBOSS A,CRD_IDNO B,GEN_BRN C ");
        sb.append("  WHERE A.APPLY_ID = B.ID_NO ");
        sb.append("    AND A.REG_BANK_NO = C.BRANCH ");
        sb.append("    AND A.EMBOSS_SOURCE IN ('1','5') ");
        sb.append("    AND A.REJECT_CODE = '' ");
        sb.append("    AND A.IN_MAIN_ERROR = '0' ");
        sb.append("    AND B.STAFF_FLAG = 'Y' ");
        sb.append("    AND A.IN_MAIN_DATE = ? ");
        sb.append("  ORDER BY C.BRANCH ");

        sqlCmd = sb.toString();
        setString(1, searchDate); // 批次處理日期
        openCursor();
    }

    private int header(StringBuffer sb) throws Exception {
        String branch = getValue("BRANCH");
        String fullChiName = getValue("FULL_CHI_NAME");
//        String cardNo = getValue("CARD_NO");
//        String groupCode = getValue("GROUP_CODE");
//        String regBankNo = getValue("REG_BANK_NO");
//        String chiName = getValue("CHI_NAME");
//        String idNo = getValue("ID_NO");
//        String actNo = getValue("ACT_NO");
        String temp = "";
        
        String str = commCrd.fixLeft("3144", 10) + commCrd.fixLeft("CRD88A", 16) + commCrd.fixLeft(searchDateTw + "員工國際信用卡核發備查簿", 88) + commCrd.fixLeft("N", 8); 
        sb.append(str).append(lineSeparator);
        sb.append(commCrd.fixLeft(commCrd.fixRight("合作金庫商業銀行", 70), 132)).append(lineSeparator);
        str = commCrd.fixLeft("分行代號: 3144  信用卡部", 54) + commCrd.fixLeft("員工國際信用卡核發備查簿", 62) + commCrd.fixLeft("保存年限: 一年", 16); 
        sb.append(str).append(lineSeparator);
        String strDate = "中華民國 " + searchDateTwYear + " 年 " + searchDateMonth + " 月 " + searchDateDay + " 日";
        temp = String.format("%04d", pageCnt1);
        str = "報表代號: " + commCrd.fixLeft("CRD88A", 15) + commCrd.fixLeft("科目代號: ", 29) + commCrd.fixLeft(strDate, 56) + commCrd.fixRight("第" + temp + "頁",18);
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        sb.append(commCrd.fixLeft("單位別: " + branch + "  " + fullChiName, 132)).append(lineSeparator);
        sb.append("====================================================================================================================================").append(lineSeparator);
        str = "      卡號        團代   發卡單位    姓名        持卡人ID   員工姓名       存款帳號       領卡簽收   核發日期   核發人員    備註";
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        sb.append("====================================================================================================================================").append(lineSeparator);
        
        return 8;
    }

    private int tail(StringBuffer sb, int lineCnt) throws Exception {
        int line = 0;
        String str = "";
        if (lineCnt == 0) {
            sb.append(lineSeparator);
            str = "查無資料";
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
        	str = "合計:      " + lineCnt + "  卡";
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
    private String getRowOfDAT() throws Exception {
        String branch = getValue("BRANCH");
        String fullChiName = getValue("FULL_CHI_NAME");
        String cardNo = getValue("CARD_NO");
        String groupCode = getValue("GROUP_CODE");
        String regBankNo = getValue("REG_BANK_NO");
        String chiName = getValue("CHI_NAME");
        String idNo = getValue("ID_NO");
        String actNo = getValue("ACT_NO");

        StringBuffer sb = new StringBuffer();
        sb.append(commCrd.fixLeft(cardNo, 18)); // 卡號
        sb.append(commCrd.fixLeft(groupCode, 9)); // 團代
        sb.append(commCrd.fixLeft(regBankNo, 9)); // 發卡單位
        sb.append(commCrd.fixLeft(chiName, 12)); // 姓名
        sb.append(commCrd.fixLeft(idNo, 12)); // 正卡持卡人身份證字號
        sb.append(commCrd.fixLeft(chiName, 12)); // 員工姓名
        sb.append(commCrd.fixLeft(actNo, 18)); // 存款帳號
        sb.append(commCrd.fixLeft("", 12)); // 領卡人簽收
        sb.append(commCrd.fixLeft("", 10)); // 核發日期
        sb.append(commCrd.fixLeft("", 12)); // 核發人員
        sb.append(commCrd.fixLeft("", 14)); // 備註
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
      CrdR88A proc = new CrdR88A();
      int retCode = proc.mainProcess(args);
      System.exit(retCode);
    }

}
