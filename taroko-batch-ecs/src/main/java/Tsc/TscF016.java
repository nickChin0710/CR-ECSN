/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  106/11/09  V1.00.01    SUP       error correction                          *
 *  109-11-16  V1.00.02    tanwei    updated for project coding standard       *
 *  110/01/11  V1.00.03    Wilson    修正bug                                    *
 ******************************************************************************/

package Tsc;


import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊卡簽帳資料檔(STMT)媒體RPT處理程式*/
public class TscF016 extends AccessDAO {

    private final String progname = "悠遊卡簽帳資料檔(STMT)媒體RPT處理程式  110/01/11 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps comips = new CommIps();

    String hCallBatchSeqno = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTnlgFileName = "";
    String hTnlgFileIden = "";
    String hTnlgRespCode = "";
    String hTnlgRowid = "";
    String hTnlgCheckCode = "";
    String hStmtRptRespCode = "";
    String hStmtRptRespDate = "";
    String hStmtRptRespTime = "";
    String hStmtTscCardNo = "";
    int hTnlgRecordSucc = 0;
    int hTnlgRecordFail = 0;

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int nUserpid = 0;
    int nRetcode = 0;
    int succCnt = 0;
    int hCgecSeqNo = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String fileSeq = "";
    String temstr1 = "";
    String tmpstr2 = "";
    String hCgecBillType = "";
    String hTnlgProcFlag = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }

            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscF016 [notify_date [force_flag]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hTnlgProcFlag = "N";
            hTnlgNotifyDate = "";
            if (args.length >= 1)
                hTnlgNotifyDate = args[0];
            if ((args.length == 2) && (args[1].toCharArray()[0] == 'Y'))
                hTnlgProcFlag = "Y";
            selectPtrBusinday();

            selectTscNotifyLog();

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only";

        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? comm.nextNDate(hBusiBusinessDate,-1) : hTnlgNotifyDate;
    }

    /***********************************************************************/
    void selectTscNotifyLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += " file_name,";
        sqlCmd += " file_iden,";
        sqlCmd += " resp_code,";
        sqlCmd += " record_succ,";
        sqlCmd += " record_fail,";
        sqlCmd += " rowid rowid";
        sqlCmd += " from tsc_notify_log ";
        sqlCmd += "where proc_flag = '1' ";
        sqlCmd += "  and file_iden = 'STMT' ";
        sqlCmd += "  and notify_date = decode(cast(? as varchar(8)) , '', notify_date, ?) ";
        sqlCmd += " order by notify_date ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int cursorIndex = openCursor();

        while (fetchTable(cursorIndex)) {
            hTnlgFileName = getValue("file_name");
            hTnlgFileIden = getValue("file_iden");
            hTnlgRespCode = getValue("resp_code");
            hTnlgRecordSucc = getValueInt("record_succ");
            hTnlgRecordFail = getValueInt("record_fail");
            hTnlgRowid = getValue("rowid");

            showLogMessage("I", "", String.format("[%s] 處理中 ..", hTnlgFileName));
            if (!hTnlgRespCode.equals("0000")) {
                updateTscNotifyLog();
                showLogMessage("I", "",
                        String.format("[%s] 整檔處理失敗, 錯誤代碼[%s](error)!", hTnlgFileName, hTnlgRespCode));
                continue;
            }
            if (fileOpen1() != 0) {
                showLogMessage("I", "", String.format("檢核檔案失敗, 錯誤代碼[%s](error)!", hTnlgCheckCode));
                continue;
            }
            fileOpen2();
            hTnlgCheckCode = "0000";
            updateTscNotifyLog();

            tmpstr1 = String.format("%s/media/tsc/%s.RPT", comc.getECSHOME(), hTnlgFileName);
            tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
            tmpstr2 = String.format("%s/media/tsc/backup/%s.RPT", comc.getECSHOME(), hTnlgFileName);
            comc.fileRename(hTnlgFileName, tmpstr2);
            showLogMessage("I", "", String.format("     處理錯誤筆數[%d]", totalCnt));
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    @SuppressWarnings("resource")
    int fileOpen1() throws Exception {
        int inta = 0, intb = 0, headTag = 0, tailTag = 0;
        String str600 = "";

        temstr1 = String.format("%s/media/tsc/%s.RPT", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format(" 處理檔案=[%s]", temstr1));
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("找不到回覆檔[%s], 請通知相關人員處理(error)", temstr1), "", hCallBatchSeqno);
        }

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            inta++;
            if ((!comc.getSubString(str600, 0, 1).equals("H")) && (!comc.getSubString(str600, 0, 1).equals("D"))
                    && ((!comc.getSubString(str600, 0, 1).equals("T")))) {
                hTnlgCheckCode = "0102";/* FIRST_CHR_ERR */
                updateTscNotifyLog();
                return (1);
            }
            if ((!comc.getSubString(str600, 0, 1).equals("H")) && (inta == 1)) {
                hTnlgCheckCode = "0112";/* HEADER_NOT_FOUND */
                updateTscNotifyLog();
                return (1);
            }
            if (inta > 500000) {
                hTnlgCheckCode = "0103";/* RECORD_QTY_OVER */
                updateTscNotifyLog();
                return (1);
            }

            if (comc.getSubString(str600, 0, 1).equals("D"))
                intb++;
            if (comc.getSubString(str600, 0, 1).equals("H")) {
                if (headTag != 0) {
                    hTnlgCheckCode = "0111";/* HEADER_DUPLICATE */
                    updateTscNotifyLog();
                    return (1);
                }
                headTag = 1;
                if (!hTnlgFileIden.equals(comc.getSubString(str600, 1, 1 + 4))) {
                    hTnlgCheckCode = "0114";/* HEADER_FILE_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                if (!comc.TSCC_BANK_ID8.equals(comc.getSubString(str600, 5, 5 + 8))) {
                    hTnlgCheckCode = "0117";/* HEADER_BANK_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                if (!comc.commDateCheck(comc.getSubString(str600, 13, 13 + 8))) {
                    hTnlgCheckCode = "0115";/* HEADER_DATE_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                if (!comc.commTimeCheck(comc.getSubString(str600, 21, 21 + 6))) {
                    hTnlgCheckCode = "0116";/* HEADER_TIME_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                if (!comc.getSubString(str600, 27, 31).equals("0000")) {
                    hTnlgCheckCode = "B001";/* 與通知檔錯誤代碼不一致 */
                    updateTscNotifyLog();
                    return (1);
                }
                tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 31, 31 + 8));
                if (comcr.str2long(tmpstr1) != hTnlgRecordSucc) {
                    hTnlgCheckCode = "B002";/* 與通知檔成功筆數不一致 */
                    updateTscNotifyLog();
                    return (1);
                }
                tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 39, 39 + 8));
                if (comcr.str2long(tmpstr1) != hTnlgRecordFail) {
                    hTnlgCheckCode = "B003";/* 與通知檔失敗筆數不一致 */
                    updateTscNotifyLog();
                    return (1);
                }

            }
            if (comc.getSubString(str600, 0, 1).equals("T")) {
                if (tailTag != 0) {
                    hTnlgCheckCode = "0121";/* TAILER_DUPLICATE */
                    updateTscNotifyLog();
                    return (1);
                }
                tailTag = 1;
                String tmpstr3 = String.format("%8.8s", comc.getSubString(str600, 1, 1 + 8));
                hTnlgRecordCnt = comcr.str2int(tmpstr3);
                if (comcr.str2long(tmpstr3) != intb) {
                    hTnlgCheckCode = "0124";/* TOTAL_QTY_ERR */
                    updateTscNotifyLog();
                    return (1);
                }
            }
            tmpstr1 = comc.subBIG5String(str600.getBytes("big5"), 0, 415);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
            if (!tmpstr2.substring(0, 16).equals(comc.subBIG5String(str600.getBytes("big5"), 415, 415+16))) {
                showLogMessage("I", "", String.format("明細資料第[%d]筆hash值檢核錯誤.", intb));

            }
        }

        closeInputText(br);
        if (headTag != 1) {
            hTnlgCheckCode = "0112";/* HEADER_NOT_FOUND */
            updateTscNotifyLog();
            return (1);
        }
        if (tailTag != 1) {
            hTnlgCheckCode = "0120";/* TAILER_NOT_FOUND */
            updateTscNotifyLog();
            return (1);
        }
        hTnlgCheckCode = "0000";
        updateTscNotifyLog();
        return (0);
    }

    /***********************************************************************/
    void fileOpen2() throws Exception {
        int inta = 0;
        String str600 = "";
        tmpstr1 = String.format("%s.RPT", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;
            if ((totalCnt % 3000) == 0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1+2).equals("01"))) {
                showLogMessage("I", "", String.format("Line [%d] format error!", totalCnt));
                continue;
            }

            tmpstr1 = String.format("%4.4s", comc.getSubString(str600, 3));
            hStmtRptRespCode = tmpstr1;

            tmpstr1 = String.format("%20.20s", comc.getSubString(str600, 10));
            for (inta = tmpstr1.length() - 1; inta >= 0; inta--) {
                if (tmpstr1.toCharArray()[inta] != ' ')
                    break;
            }
            hStmtTscCardNo = tmpstr1.substring(0, inta);

            showLogMessage("I", "", String.format("Line [%08d][%s] resp_code[%s] error!", totalCnt,
                    hStmtTscCardNo, hStmtRptRespCode));

            updateTscStmtLog();
        }
        closeInputText(br);
    }

    /***********************************************************************/
    void updateTscStmtLog() throws Exception {
        daoTable = "tsc_stmt_log";
        updateSQL = " rpt_resp_code = ?,";
        updateSQL += " rpt_resp_date = ?,";
        updateSQL += " rpt_resp_time = ?,";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where tsc_card_no = ? ";
        whereStr += " and file_name   = ? ";
        setString(1, hStmtRptRespCode);
        setString(2, hStmtRptRespDate);
        setString(3, hStmtRptRespTime);
        setString(4, javaProgram);
        setString(5, hStmtTscCardNo);
        setString(6, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_stmt_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscNotifyLog() throws Exception {
        daoTable = "tsc_notify_log";
        updateSQL = "check_code  = ?,";
        updateSQL += " proc_flag = '2',";
        updateSQL += " proc_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " proc_time = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where file_name  = ? ";
        setString(1, hTnlgCheckCode);
        setString(2, javaProgram);
        setString(3, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF016 proc = new TscF016();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
