/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/09/18  V1.00.00  Zuwei Su    program initial                                                 *                                   
 *  112/12/19  V1.00.01  Zuwei Su    errRtn改為 show message & return 1  *  
 *****************************************************************************************************/
package Mkt;

import com.*;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class MktC951 extends AccessDAO {
    private final String PROGNAME = "行銷活動群組 - 產生檔案下載處理 (一般名單) 112/09/18 V1.00.00";
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    private static final String PATH_FOLDER = "/media/mkt/";

    private String hBusinessDate = "";
    private int totCnt = 0;
    private int fptr1 = -1;
    
    Map<String, String> outfileTypeMap = new HashMap<>();
    Map<String, StringBuilder> outfileFileMap = new HashMap<>();

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            // 固定要做的
            if (!connectDataBase()) {
//                comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error" );
                return 1;
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());


            selectPtrBusinday();

            if (args.length == 1 && args[0].length() == 8) {
                hBusinessDate = args[0];
            }
            showLogMessage("I", "", "本日營業日期=[" + hBusinessDate + "]");

            selectOutfileType();
            writeReport();
            
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    private void selectOutfileType() throws Exception {
        sqlCmd = " SELECT DISTINCT c.ACTIVE_GROUP_ID, c.CARD_NO, c.ID_P_SEQNO, c.P_SEQNO, a.OUTFILE_TYPE, "
                + " d.wf_key, d.wf_value2, "
                + " Crd_card.group_code, "
                + " crd_idno.ID_NO, crd_idno.CELLAR_PHONE, crd_idno.E_MAIL_ADDR, crd_idno.chi_name, "
                + " ACT_ACNO.BILL_SENDING_ZIP,                                                                                             "
                + " ACT_ACNO.BILL_SENDING_ADDR1 || ACT_ACNO.BILL_SENDING_ADDR2 || ACT_ACNO.BILL_SENDING_ADDR3 || ACT_ACNO.BILL_SENDING_ADDR4 || ACT_ACNO.BILL_SENDING_ADDR5 as BILL_SENDING_ADDR "
                + " FROM MKT_CHANNEL_PARM a"
                + " inner join MKT_CHANNELGP_DATA b on b.ACTIVE_CODE=a.ACTIVE_CODE "
                + " inner join MKT_CHANNELGP_LIST c on c.ACTIVE_GROUP_ID=b.ACTIVE_GROUP_ID "
                + " inner join ptr_sys_parm d on d.wf_key=a.OUTFILE_TYPE "
                + " left join crd_card on crd_card.card_no = c.card_no "
                //+ " left join crd_idno on crd_idno.id_p_seqno = c.id_p_seqno "			//--以MKT_CHANNELGP_LIST為主
                + " left join crd_idno on crd_idno.id_p_seqno = Crd_card.id_p_seqno "		//--以crd_card為主
                + " left join ACT_ACNO on act_acno.p_seqno = c.p_seqno "
                + " WHERE a.cal_def_date = ?                                                        "
                + " And a.lottery_type = '3'                                                                "
                + " ORDER BY c.ACTIVE_GROUP_ID, c.CARD_NO                                                 ";
        setString(1, hBusinessDate);
        int cnt = selectTable();
        for (int i = 0; i < cnt; i++) {
            String outfileType = getValue("OUTFILE_TYPE", i);
            String wfValue2 = getValue("wf_value2", i);
            
            String filename = outfileTypeMap.get(outfileType);
            StringBuilder sb = null;
            if (filename == null) {
                filename = wfValue2.replace("YYYYMM", hBusinessDate);
                outfileTypeMap.put(outfileType, filename);
                sb = new StringBuilder(10240);
                outfileFileMap.put(filename, sb);
            } else {
                sb = outfileFileMap.get(filename);
            }
            String idNo = getValue("ID_NO", i);
            String groupCode = getValue("group_code", i);
            String cardGroupType = groupCode.length() < 3 ? groupCode : groupCode.substring(groupCode.length()-3);
            String cardNo = getValue("card_no", i);
            String cellarPhone = getValue("CELLAR_PHONE", i);
            String eMailAddr = getValue("E_MAIL_ADDR", i);
            String billSendingZip = getValue("BILL_SENDING_ZIP", i);
            String billSendingAddr = getValue("BILL_SENDING_ADDR", i);
            String chiName = getValue("chi_name", i);
            
            sb.append(comStr.rpad(idNo, 10)).append(",");
            sb.append(comStr.rpad(cardGroupType, 3)).append(",");
            sb.append(comStr.rpad(cardNo, 16)).append(",");
            sb.append(comStr.rpad("0", 3)).append(",");
            sb.append(comStr.rpad("0", 3)).append(",");
            sb.append(comStr.rpad(cellarPhone, 15)).append(",");
            sb.append(comStr.rpad(eMailAddr, 16)).append(",");
            sb.append(comStr.rpad(billSendingZip, 3)).append(",");
            sb.append(comStr.rpad(chiName, 20)).append(",");
            sb.append(comStr.rpad(billSendingAddr, 20)).append("\n");
        }
    }

    private void selectPtrBusinday() throws Exception {

        sqlCmd = "select business_date, to_char(to_date(business_date,'yyyymmdd') - 1 months ,'yyyymm') as business_month ";
        sqlCmd += "from ptr_businday fetch first 1 row only ";

        selectTable();

        if (notFound.equals("Y")) {
            showLogMessage("I", "", "select ptr_businday error!");
            exitProgram(1);
        }
        hBusinessDate = getValue("business_date");
    }

    private void writeReport() throws Exception {
        // 依據不同的產檔格式, 分別產出檔案及傳送至遠端
        for (Map.Entry<String, StringBuilder> entry : outfileFileMap.entrySet()) {
            String filename = entry.getKey();

            fileOpen(filename);
            showLogMessage("I", "", "開始寫入檔案: " + filename);
            writeTextFile(fptr1, entry.getValue().toString());
            closeOutputText(fptr1);
            ftpProc(filename);
        }
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
        // commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
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
            // showLogMessage("I", "", "檔案傳送 " + "NCR2EMP" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "檔案傳送 " + "CREDITCARD" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktC951 執行完成 傳送EMP失敗[" + filename + "]");
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
        MktC951 proc = new MktC951();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
