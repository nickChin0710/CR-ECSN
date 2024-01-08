/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/10  V1.00.00   Zuwei     program initial                           *
*  112/03/09  V1.00.01   Wilson    欄位內容調整                                                                                              *
*  112/03/10  V1.00.02   Wilson    增加回覆碼訊息欄位                                                                                  *
*****************************************************************************/
package Crd;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.ObjectUtils;
import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommTxInf;
import Cca.CalBalance;

public class CrdF035 extends AccessDAO {
    private static final int OUTPUT_BUFF_SIZE = 1000;
    private final String progname = "產生送徵審系統NonCombo製卡回饋檔程式  112/03/10  V1.00.02";
    private static final String CRM_FOLDER = "media/crd/";
    private static final String DATA_FORM = "NonCombo";
    private final String lineSeparator = System.lineSeparator();

    CommCrd comc = new CommCrd();
    CommCrd commCrd = new CommCrd();    
    CommDate commDate = new CommDate();
    CalBalance calBalance = null;
    
    String tmpMsg = "";

    public int mainProcess(String[] args) {

        try {
            CommCrd comc = new CommCrd();
            calBalance = new CalBalance(conn, getDBalias());
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================

            // get searchDate
            String searchDate = new SimpleDateFormat("YYYYMMdd").format(new Date());
//            showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
//            searchDate = getProgDate(searchDate, "D");
//            showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

            // convert YYYYMMDD into YYMMDD
            String fileNameDate = searchDate;

            // get the name and the path of the .DAT file
            String datFileName = String.format("%s_R_%s%s", DATA_FORM, fileNameDate,
                    ".txt");
            String fileFolder = Paths.get(commCrd.getECSHOME(), CRM_FOLDER).toString();

            // 產生主要檔案 .txt
            int dataCount = generateDatFile(fileFolder, datFileName);

            // 產生Header檔
//            CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
//            dateTime(); // update the system date and time
//            boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate,
//                    sysDate, sysTime.substring(0, 4), dataCount);
//            if (isGenerated == false) {
//                comc.errExit("產生HDR檔錯誤!", "");
//            }

            // CR_STATUS_YYMMDD.DAT -> CR_STATUS_YYMMDD.HDR
//            String hdrFileName =
//                    datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);

            // run FTP
            procFTP(fileFolder, datFileName, datFileName);
            renameFile1(datFileName);

            showLogMessage("I", "", "執行結束");
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
    private int generateDatFile(String fileFolder, String datFileName)
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
            StringBuffer sb = new StringBuffer();
            showLogMessage("I", "", "開始產生.TXT檔......");

            // 處理非combo卡資料
            showLogMessage("I", "", "開始處理NonCombo製卡回饋檔......");
            selectNonComboData();           
            
            // 首筆資料
            sb.append(commCrd.fixLeft("1", 1)); // 資料識別碼
            sb.append(commCrd.fixLeft(commDate.twDate(), 11)); // 案件編號
            sb.append(lineSeparator);
            
            int successCnt = 0;
            int faileCnt = 0;
            while (fetchTable()) {
            	
            	if(!getValue("REJECT_CODE").equals("")) {
            		selectCrdMessage();
            	}
            	
                String rowOfData = getRowOfData();
                sb.append(rowOfData);
                rowCount++;
                countInEachBuffer++;
                if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
                    showLogMessage("I", "",
                            String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
                    byte[] tmpBytes = sb.toString().getBytes("MS950");
                    writeBinFile(tmpBytes, tmpBytes.length);
                    sb = new StringBuffer();
                    countInEachBuffer = 0;
                }
                // 異動TO_AVT_DATE
                updateToAvtDate();
                successCnt += "".equals(getValue("REJECT_CODE")) ? 1 : 0;
                faileCnt += "".equals(getValue("REJECT_CODE")) ? 0 : 1;
            }
            closeCursor();
            
            String rowCountFile = String.format("%05d", rowCount);
            String successCntFile = String.format("%05d", successCnt);
            String faileCntFile = String.format("%05d", faileCnt);
            
            // 尾筆資料
            sb.append(commCrd.fixLeft("3", 1)); // 資料識別碼
            sb.append(commCrd.fixLeft(rowCountFile, 5)); // 全部筆數
            sb.append(commCrd.fixLeft(successCntFile, 5)); // 成功筆數
            sb.append(commCrd.fixLeft(faileCntFile, 5)); // 失敗筆數
            sb.append(commCrd.fixLeft(commDate.twDate(), 7)); // 處理日期
            sb.append(lineSeparator);
            
            // write the rest of bytes on the file 
            if (countInEachBuffer > 0) {
                showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
                byte[] tmpBytes = sb.toString().getBytes("MS950");
                writeBinFile(tmpBytes, tmpBytes.length);
            }

            if (rowCount == 0) {
                showLogMessage("I", "", "無資料可寫入.TXT檔");
            } else {
                showLogMessage("I", "", String.format("產生.TXT檔完成！，共產生%d筆資料", rowCount));
            }

        } finally {
            closeBinaryOutput();
        }

        return rowCount;
    }

    // update to_avt_date
    private int updateToAvtDate() throws Exception {
        StringBuffer sb = new StringBuffer();
        if ("1".equals(getValue("TYPE"))) {
            sb.append(" UPDATE CRD_EMAP_TMP ");
            sb.append("   SET TO_AVT_DATE = ?, ");
            sb.append("       MOD_USER = ?, ");
            sb.append("       MOD_TIME = sysdate, ");
            sb.append("       MOD_PGM = ? ");
            sb.append(" WHERE hex(ROWID) = ? ");
        } else {
            sb.append(" UPDATE CRD_EMBOSS ");
            sb.append("   SET TO_AVT_DATE = ?, ");
            sb.append("       MOD_USER = ?, ");
            sb.append("       MOD_TIME = sysdate, ");
            sb.append("       MOD_PGM = ? ");
            sb.append(" WHERE hex(ROWID) = ? ");
        }

        setString(1, commDate.sysDate());
        setString(2, "CRDF035");
//        setString(3, commDate.sysTime());
        setString(3, "CRDF035");
        setString(4, getValue("ROWID"));
        int result = executeSqlCommand(sb.toString());
        return result;
    }

    private void selectCrdMessage() throws Exception {
        sqlCmd  = "SELECT MSG ";
        sqlCmd += "  FROM CRD_MESSAGE  ";
        sqlCmd += " WHERE MSG_TYPE = 'NEW_CARD' ";
        sqlCmd += "   AND MSG_VALUE = ? ";
        setString(1, getValue("REJECT_CODE"));
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	tmpMsg = getValue("MSG");
        }               	
    }
    
    /**
     * 產生檔案
     * 
     * @return String
     * @throws Exception
     */
    private String getRowOfData() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(commCrd.fixLeft("2", 1)); // 資料識別碼
        sb.append(commCrd.fixLeft(getValue("APPLY_NO"), 11)); // 案件編號
        sb.append(commCrd.fixLeft(getValue("PM_ID"), 11)); // 正卡人ID
        sb.append(commCrd.fixLeft("0".equals(getValue("SUP_FLAG")) ? getValue("GROUP_CODE") : "", 4)); // 正卡團體代號
        sb.append(commCrd.fixLeft("", 6)); // 鍵檔人員代號
        sb.append(commCrd.fixLeft("", 6)); // 覆審人員代號
        int creditAmt = getValueInt("INDIV_CRD_LMT");
        creditAmt = creditAmt > 0 ? creditAmt : getValueInt("CREDIT_LMT");
        creditAmt = creditAmt/10000;
        sb.append(commCrd.fixLeft(String.format("%04d", creditAmt), 4)); // 信用額度        
        sb.append(commCrd.fixLeft(getValue("REVOLVE_INT_RATE_YEAR_CODE"), 4)); // 優惠代號(年利率)
        sb.append(commCrd.fixLeft(getValue("CHI_NAME"), 50)); // 正卡人中文姓名
        sb.append(commCrd.fixLeft("0".equals(getValue("SUP_FLAG")) ? "" : getValue("GROUP_CODE"), 4)); // 附卡團體代號
        sb.append(commCrd.fixLeft("0".equals(getValue("SUP_FLAG")) ? "" : getValue("APPLY_ID"), 11)); // 附卡ID
        sb.append(commCrd.fixLeft("0".equals(getValue("SUP_FLAG")) ? getValue("CARD_NO") : "", 16)); // 正卡卡號
        sb.append(commCrd.fixLeft("0".equals(getValue("SUP_FLAG")) ? "" : getValue("CARD_NO"), 16)); // 附卡卡號
        sb.append(commCrd.fixLeft("".equals(getValue("REJECT_CODE")) ? "0000" : getValue("REJECT_CODE"), 4)); // 回覆碼
        sb.append(commCrd.fixLeft("".equals(getValue("REJECT_CODE")) ? "" : tmpMsg, 100)); // 回覆碼訊息

        sb.append(lineSeparator);

        return sb.toString();
    }

    // 讀取製卡成功及失敗(被退件)的非combo卡資料
    private void selectNonComboData() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT '1' AS TYPE, ");
        sb.append("         APPLY_NO, ");
        sb.append("         SUP_FLAG, ");
        sb.append("         PM_ID, ");
        sb.append("         APPLY_ID, ");
        sb.append("         GROUP_CODE, ");
        sb.append("         CREDIT_LMT, ");
        sb.append("         INDIV_CRD_LMT, ");
        sb.append("         REVOLVE_INT_RATE_YEAR_CODE, ");
        sb.append("         CHI_NAME, ");
        sb.append("         CARD_NO, ");
        sb.append("         REJECT_CODE, ");
        sb.append("         hex(ROWID) as ROWID ");
        sb.append(" FROM CRD_EMAP_TMP ");
        sb.append(" WHERE COMBO_INDICATOR = 'N' ");
        sb.append("   AND CORP_NO ='' ");
        sb.append("   AND SOURCE ='1' ");
        sb.append(" AND REJECT_CODE <> 'Y' ");
        sb.append(" AND TO_AVT_DATE ='' ");
        sb.append(" UNION ");
        sb.append(" SELECT '2' AS TYPE, ");
        sb.append("         APPLY_NO, ");
        sb.append("         SUP_FLAG, ");
        sb.append("         PM_ID, ");
        sb.append("         APPLY_ID, ");
        sb.append("         GROUP_CODE, ");
        sb.append("         CREDIT_LMT, ");
        sb.append("         INDIV_CRD_LMT, ");
        sb.append("         REVOLVE_INT_RATE_YEAR_CODE, ");
        sb.append("         CHI_NAME, ");
        sb.append("         CARD_NO, ");
        sb.append("         REJECT_CODE, ");
        sb.append("         hex(ROWID) AS ROWID ");
        sb.append(" FROM CRD_EMBOSS ");
        sb.append(" WHERE COMBO_INDICATOR = 'N' ");
        sb.append("   AND CORP_NO ='' ");
        sb.append("   AND EMBOSS_SOURCE ='1' ");
        sb.append(" AND (IN_MAIN_DATE <>'' OR REJECT_CODE <> '') ");
        sb.append(" AND TO_AVT_DATE ='' ");

        sqlCmd = sb.toString();
        openCursor();
    }

    void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = fileFolder;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
//        commFTP.hEriaLocalDir = String.format("%s/media/crm", comc.getECSHOME());
        commFTP.hEriaRemoteDir = "crdatacrea/NCR2TCB";

        // 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
        String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

        showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
        int errCode = commFTP.ftplogName("NCR2TCB", ftpCommand);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
            commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
            commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
        }
    }
    
 	void renameFile1(String removeFileName) throws Exception {
  		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + removeFileName;
  		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + removeFileName;
  		
  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
  			return;
  		}
  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
  	}

    public static void main(String[] args) {
        CrdF035 proc = new CrdF035();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
}