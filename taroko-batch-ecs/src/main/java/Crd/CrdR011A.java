/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/07/29  V1.00.00   Zuwei      program initial                          *
*  112/08/02  V1.00.01   Zuwei Su   每一個分行頁表頭前都有一行的控制資料                          *
*  112/08/08  V1.00.02   Wilson     產檔格式調整                                                                                           *
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

public class CrdR011A extends AccessDAO {
    private final String PROGNAME = "產生機場貴賓(龍騰卡)製妥明細表檔程式  112/09/10  V1.00.03";
    private CommCrd comc = new CommCrd();
    private CommCrd commCrd = new CommCrd();
    private CommDate commDate = new CommDate();
    
    private final int OUTPUT_BUFF_SIZE = 55;
    private final String CRM_FOLDER = String.format("%s/media/crd", comc.getECSHOME());
    private final String DATA_FORM = "RCRF11A";
    private String lineSeparator = "\n";

//    private CalBalance calBalance = null;
    private String searchDate = "";
    private String searchDateTw = "";
    private String searchDateTwYear = "";
    private String searchDateMonth = "";
    private String searchDateDay = "";
    
    int totalCnt = 0;
    int type1Cnt = 0;
    int type2Cnt = 0;
    int type3Cnt = 0;
    int type4Cnt = 0;
    int type5Cnt = 0;
    int type9Cnt = 0;
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
            showLogMessage("I","","Usage CrdR011A [business_date]");

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
            type1Cnt = 0;
            type2Cnt = 0;
            type3Cnt = 0;
            type4Cnt = 0;
            type5Cnt = 0;
            type9Cnt = 0;
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
                    type1Cnt = 0;
                    type2Cnt = 0;
                    type3Cnt = 0;
                    type4Cnt = 0;
                    type5Cnt = 0;
                    type9Cnt = 0;
                }
                rowCount++;
                if (cardNo != null && cardNo.length() > 0) {
                	lineCnt++;
                    String rowOfDAT = getRowOfDAT(lineCnt);
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

    // 讀取各分行當日寫入主檔的龍騰卡製卡資料
    private void selectCrdEmbossData() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT ");
        sb.append("     D.BRANCH, ");
        sb.append("     D.FULL_CHI_NAME, ");
        sb.append("     C.REG_BANK_NO, ");
        sb.append("     A.EMBOSS_SOURCE, ");
        sb.append("     A.REISSUE_REASON, ");
        sb.append("     C.GROUP_CODE, ");
        sb.append("     A.CARD_NO, ");
        sb.append("     B.ID_NO, ");
        sb.append("     B.CHI_NAME, ");
        sb.append("     A.PP_CARD_NO, ");
        sb.append("     B.HOME_AREA_CODE1 || '-' || HOME_TEL_NO1 AS HOME_NO, ");
        sb.append("     B.OFFICE_AREA_CODE1 || '-' || OFFICE_TEL_NO1 || '-' || OFFICE_TEL_EXT1 AS OFFICE_NO ");
        sb.append(" FROM ");
        sb.append("     CRD_EMBOSS_PP A, ");
        sb.append("     CRD_IDNO B, ");
        sb.append("     CRD_CARD C, ");
        sb.append("     GEN_BRN D ");
        sb.append(" WHERE ");
        sb.append("     A.ID_NO = B.ID_NO ");
        sb.append("     AND A.CARD_NO = C.CARD_NO ");
        sb.append("     AND C.REG_BANK_NO = D.BRANCH ");
        sb.append("     AND A.VIP_KIND = '2' ");
        sb.append("     AND A.IN_MAIN_ERROR = '0' ");
        sb.append("     AND A.IN_MAIN_DATE = ? ");
        sb.append(" ORDER BY D.BRANCH ");

        sqlCmd = sb.toString();
        setString(1, searchDate); // 批次處理日期
        openCursor();
    }

    private int header(StringBuffer sb) throws Exception {
        String branch = getValue("BRANCH");
        String fullChiName = getValue("FULL_CHI_NAME");
        String str = "";
        String temp = "";
        
        str = commCrd.fixLeft("3144", 10) + commCrd.fixLeft("CRF11A", 16) + commCrd.fixLeft(searchDateTw + "機場貴賓（龍騰）卡製妥明細表", 88) + commCrd.fixLeft("N", 8); 
        sb.append(str).append(lineSeparator);
        sb.append(commCrd.fixLeft(commCrd.fixRight("合作金庫商業銀行", 70), 132)).append(lineSeparator);
        str = commCrd.fixLeft("分行代號: 3144  信用卡部", 54) + commCrd.fixLeft("機場貴賓（龍騰）卡製妥明細表", 62) + commCrd.fixLeft("保存年限: 一年", 16); 
        sb.append(str).append(lineSeparator);
        String strDate = "中華民國 " + searchDateTwYear + " 年 " + searchDateMonth + " 月 " + searchDateDay + " 日";
        temp = String.format("%04d", pageCnt1);
        str = "報表代號: " + commCrd.fixLeft("CRF11A", 15) + commCrd.fixLeft("科目代號: ", 29) + commCrd.fixLeft(strDate, 56) + commCrd.fixRight("第" + temp + "頁",18);
        sb.append(commCrd.fixLeft(str, 132)).append(lineSeparator);
        sb.append(commCrd.fixLeft("分行別: " + branch + "  " + fullChiName, 132)).append(lineSeparator);
        sb.append("====================================================================================================================================").append(lineSeparator);
        str = " 序號  申請種類   卡別      卡號               身份證字號     姓名        龍騰卡號區間八碼     電話(宅)         電話(公) ";
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
            str = "合計: 首次直接申請龍騰卡(TYPE 1): " + type1Cnt + "  掛失龍騰卡(TYPE 2): " + type2Cnt
                    + "  毀損補發龍騰卡(TYPE 3): " + type3Cnt + "  偽冒龍騰卡(TYPE 5): " + type5Cnt
                    + "  龍騰卡續卡(TYPE 9): " + type9Cnt;
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
    private String getRowOfDAT(int seq) throws Exception {
        String cardNo = getValue("CARD_NO");
        String groupCode = getValue("GROUP_CODE");
        String chiName = getValue("CHI_NAME");
        String idNo = getValue("ID_NO");
        String ppCardNo = getValue("PP_CARD_NO");
        String ppCardNo8 = ppCardNo.substring(6, 14);

        StringBuffer sb = new StringBuffer();
        sb.append(commCrd.fixLeft("" , 2)); // 空白
        sb.append(commCrd.fixLeft("" + seq, 9)); // 序號
        String embossSource = getValue("EMBOSS_SOURCE");
        String reissueReason = getValue("REISSUE_REASON");
        String applyCategory = "";
        switch (embossSource) {
            case "1":
                applyCategory = "1";
                type1Cnt++;
                break;
            case "5":
                switch (reissueReason) {
                    case "1":
                        applyCategory = "2";
                        type2Cnt++;
                        break;
                    case "2":
                        applyCategory = "3";
                        type3Cnt++;
                        break;
                    case "3":
                        applyCategory = "5";
                        type5Cnt++;
                        break;
                    default:
                        break;
                }
                break;
            case "3":
            case "4":
                applyCategory = "9";
                type9Cnt++;
                break;
            default:
                break;
        }
        sb.append(commCrd.fixLeft(applyCategory, 7)); // 申請種類
        sb.append(commCrd.fixLeft(groupCode, 9)); // 卡別/團代
        sb.append(commCrd.fixLeft(cardNo, 20)); // 卡號
        sb.append(commCrd.fixLeft(idNo, 15)); // 身份證字號
        sb.append(commCrd.fixLeft(chiName, 12)); // 姓名
        sb.append(commCrd.fixLeft(ppCardNo8, 20)); // 龍騰卡號區間八碼
        sb.append(commCrd.fixLeft(getValue("HOME_NO"), 17)); // 電話(宅)
        sb.append(commCrd.fixLeft(getValue("OFFICE_NO"), 24)); // 電話(公)
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
      CrdR011A proc = new CrdR011A();
      int retCode = proc.mainProcess(args);
      System.exit(retCode);
    }

}
