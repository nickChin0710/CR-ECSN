/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/05/30  V1.00.00    Yang Bo                 program initial                                   *
 *  112/06/08  V1.00.01    Yang Bo                 program initial                                   *
 *  112/06/09  V1.00.02    Yang Bo                 產出檔案不須分隔符號(,)                           *
 *  112/06/15  V1.00.03    Zuwei Su                移除第5欄(存款帳號 )                           *
 *  112/06/16  V1.00.04    Zuwei Su                只有傳出  1,2 欄，產出backup文檔名調整                           *
 *  112/08/10  V1.00.05    Zuwei Su                換行改為\r\n格式，送檔名固定為PBMB_ID.TXT                           *
 *****************************************************************************************************/
package Mkt;

import com.*;

import java.text.Normalizer;

public class MktFs01 extends AccessDAO {
    private static final String PROGNAME = "產生送ap1,i享樂卡&combo卡&金鑽卡流通卡名單檔程式 112/08/10  V1.00.05 ";
    private static final String DOC_NAME = "PBMB_ID_YYYYMMDD.TXT";
//    private static final String DOC_NAME = "PBMB_ID_YYYYMMDD";
    private static final String PATH_FOLDER = "/media/mkt/";
    private final static String LINE_SEPARATOR = System.lineSeparator();
    CommCrd comc = new CommCrd();
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;
    CommString commStr = new CommString();
    CommDate commDate = new CommDate();

    private String hBusiBusinessDate;
    private Integer totCnt;
    private int fptr1 = -1;

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktFs01 proc = new MktFs01();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            // 固定要做的
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = sysDate;
            if (args.length == 0) {
                //selectPtrBusinday();
            	//營業日改成日曆日執行
            } else if (args.length == 1) {
                hBusiBusinessDate = args[0];
            }

            if (commStr.empty(hBusiBusinessDate) || !commDate.isDate(hBusiBusinessDate)) {
                //comcr.errRtn(String.format("BusindayDate [%s] 無效, 請傳入參數合格值YYYYMMDD! ", hBusiBusinessDate), "", comcr.hCallBatchSeqno);
				showLogMessage("I", "", "請傳入參數合格值yyyymmdd[" + hBusiBusinessDate + "]");
				return 1;            	
            } else if (!commStr.eqIgno(hBusiBusinessDate.substring(6), "01")) {
                //comcr.errRtn(String.format("BusindayDate [%s] 非執行日期: 每月1日執行! ", hBusiBusinessDate), "", comcr.hCallBatchSeqno);
    			showLogMessage("I", "", "非每月 1 日，程式不執行[" + hBusiBusinessDate + "]");
    			return 0 ;            	            	
            }

            selectCrdCard();
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

    private void selectPtrBusinday() throws Exception {
        sqlCmd = "  select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    private void selectCrdCard() throws Exception {
        sqlCmd = " select DISTINCT b.id_no ";
        //sqlCmd = " , (case when a.group_code = '1616' then 'I'";
        //sqlCmd += "                      when a.group_code = '1657' then 'I'";
        //sqlCmd += "                      when a.group_code = '1622' then 'G'";
        //sqlCmd += "                      else 'C' end) as card_flag, ";
        //sqlCmd += " a.card_no, (case when a.group_code = '1622' then '' else a.combo_acct_no end) as combo_acct_no ";
        sqlCmd += " from crd_card a, crd_idno b ";
        sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += "   and a.group_code in ('1616','1657','1622','1450','1451','1452','1453','1460','1461','1470','1471','1480','1481', ";
        sqlCmd += "                        '1550','1551','1553','1560','1561','1570','1571','1580','1581','1616','1640','1650','1651', ";
        sqlCmd += "                        '1652','1653','1654','1655','1656','1657','1661') ";
        sqlCmd += "   and a.sup_flag = '0' ";
        sqlCmd += "   and a.current_code = '0' ";
        //sqlCmd += " order by a.group_code ";
        sqlCmd += " order by b.id_no ";        

        int cursorIndex = openCursor();
        StringBuilder sb = new StringBuilder();
        totCnt = 0;
        while (fetchTable()) {
            String idNo = getValue("id_no");
            //String cardFlag = getValue("card_flag");
            //String cardNo = getValue("card_no");
            //String comboAcctNo = getValue("combo_acct_no");

            sb.append(commStr.lpad(commDate.dateAdd(hBusiBusinessDate, 0, -1, 0), 6));//.append(",");
            sb.append(commStr.lpad(idNo, 10));//.append(",");
//            sb.append(commStr.lpad(cardFlag, 1));//.append(",");
//            sb.append(commStr.lpad(cardNo, 16));//.append(",");
//            sb.append(commStr.lpad(comboAcctNo, 13));
//            sb.append(LINE_SEPARATOR);
            sb.append("\r\n");

            totCnt++;
        }
        closeCursor(cursorIndex);

        // 寫入檔案
        writeReport(sb.toString());
    }

    private void writeReport(String text) throws Exception {
//        String filename = DOC_NAME.replace("YYYYMMDD", hBusiBusinessDate);
        String filename = DOC_NAME.replace("_YYYYMMDD", "");

        fileOpen(filename);
        showLogMessage("I", "", "開始寫入檔案: " + filename);
        writeTextFile(fptr1, text);
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
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + PATH_FOLDER;    //相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "mput " + filename + " 開始上傳....");

        String tmpChar = "mput " + filename;

        int errCode = commFTP.ftplogName("NCR2TCB", tmpChar);
        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 " + "NCR2EMP" + " 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktFs01執行完成 傳送EMP失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        renameFile(filename);
    }

    // ************************************************************************
    public void renameFile(String removeFileName) throws Exception {
        String tmpStr1 = comc.getECSHOME() + PATH_FOLDER + removeFileName;
        String backupName = DOC_NAME.replace("YYYYMMDD", hBusiBusinessDate + "_" + sysTime);
        String tmpStr2 = comc.getECSHOME() + PATH_FOLDER + "backup/" + backupName;

        if (!comc.fileRename2(tmpStr1, tmpStr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }

        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpStr2 + "]");
    }
}
