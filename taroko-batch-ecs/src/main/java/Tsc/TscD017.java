/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/12  V1.00.00    Ryan     program initial                            *
*                                                                             *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊VD卡卡人資料檔(DCPS)媒體RPT處理程式*/
public class TscD017 extends AccessDAO {

    private final String progname = "悠遊VD卡卡人資料檔(DCPS)媒體RPT處理程式  110/01/12 V1.00.01";
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
    String hDcpfRptRespCode = "";
    String hTempRptRespDate = "";
    String hTempRptRespTime = "";
    String hDcpfTscCardNo = "";
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
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String hTnlgProcFlag = "";
    String hCgecBillType = "";

    Buf1 dtl = new Buf1();

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
                comc.errExit("Usage : TscF017 [notify_date [force_flag]]", "");
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
            showLogMessage("I", "", String.format("處理TSCC檔案日期[%s][%s]", hTnlgNotifyDate, hTnlgProcFlag));

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
        sqlCmd += " rowid rowid ";
        sqlCmd += " from tsc_notify_log ";
        sqlCmd += "where proc_flag = '1' ";
        sqlCmd += "  and file_iden = 'DCPS' ";
        sqlCmd += "  and notify_date = decode(cast(? as varchar(8)) , '', notify_date, ?) ";
        sqlCmd += "order by notify_date ";
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
            comc.fileRename(temstr1, tmpstr2);
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
                    && (!comc.getSubString(str600, 0, 1).equals("T"))) {
                hTnlgCheckCode = "0102"; /* FIRST_CHR_ERR */
                updateTscNotifyLog();
                return (1);
            }
            if ((!comc.getSubString(str600, 0, 1).equals("H")) && (inta == 1)) {
                hTnlgCheckCode = "0112"; /* HEADER_NOT_FOUND */
                updateTscNotifyLog();
                return (1);
            }
            if (inta > 500000) {
                hTnlgCheckCode = "0103"; /* RECORD_QTY_OVER */
                updateTscNotifyLog();
                return (1);
            }

            if (comc.getSubString(str600, 0, 1).equals("D"))
                intb++;
            if (comc.getSubString(str600, 0, 1).equals("H")) {
                if (headTag != 0) {
                    hTnlgCheckCode = "0111"; /* HEADER_DUPLICATE */
                    updateTscNotifyLog();
                    return (1);
                }
                headTag = 1;
                if (!hTnlgFileIden.equals(comc.getSubString(str600, 1, 1 + 4))) {
                    hTnlgCheckCode = "0114"; /* HEADER_FILE_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                if (!comc.TSCC_BANK_ID8.equals(comc.getSubString(str600, 5, 5 + 8))) {
                    hTnlgCheckCode = "0117"; /* HEADER_BANK_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 13, 13 + 8));
                hTempRptRespDate = tmpstr1;
                if (!comc.commDateCheck(tmpstr1)) {
                    hTnlgCheckCode = "0115"; /* HEADER_DATE_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                tmpstr1 = String.format("%6.6s", comc.getSubString(str600, 21, 21 + 6));
                hTempRptRespTime = tmpstr1;
                if (!comc.commTimeCheck(tmpstr1)) {
                    hTnlgCheckCode = "0116"; /* HEADER_TIME_NAME_OVER */
                    updateTscNotifyLog();
                    return (1);
                }
                if (!comc.getSubString(str600, 27, 31).equals("0000")) {
                    hTnlgCheckCode = "B001"; /* 與通知檔錯誤代碼不一致 */
                    updateTscNotifyLog();
                    return (1);
                }

                tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 31, 31 + 8));
                if (comcr.str2long(tmpstr1) != hTnlgRecordSucc) {
                    hTnlgCheckCode = "B002"; /* 與通知檔成功筆數不一致 */
                    updateTscNotifyLog();
                    return (1);
                }
                tmpstr1 = String.format("%8.8s", str600.subSequence(39, 39 + 8));
                if (comcr.str2long(tmpstr1) != hTnlgRecordFail) {
                    hTnlgCheckCode = "B003"; /* 與通知檔失敗筆數不一致 */
                    updateTscNotifyLog();
                    return (1);
                }
            }

            if (comc.getSubString(str600, 0, 1).equals("T")) {
                if (tailTag != 0) {
                    hTnlgCheckCode = "0121"; /* TAILER_DUPLICATE */
                    updateTscNotifyLog();
                    return (1);
                }
                tailTag = 1;
                String tmpstr3 = String.format("%8.8s", comc.getSubString(str600, 1));
                hTnlgRecordCnt = comcr.str2int(tmpstr3);

                if (comcr.str2long(tmpstr3) != intb) {
                    hTnlgCheckCode = "0124"; /* TOTAL_QTY_ERR */
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
            hTnlgCheckCode = "0112"; /* HEADER_NOT_FOUND */
            updateTscNotifyLog();
            return (1);
        }
        if (tailTag != 1) {
            hTnlgCheckCode = "0120"; /* TAILER_NOT_FOUND */
            updateTscNotifyLog();
            return (1);
        }
        hTnlgCheckCode = "0000";
        updateTscNotifyLog();
        return (0);
    }

    /***********************************************************************/
    void fileOpen2() throws Exception {
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
            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1+2).equals("01"))) {
                showLogMessage("I", "", String.format("Line [%d] format error!", totalCnt));
                continue;
            }

            hDcpfRptRespCode = comc.rtrim(dtl.rptRespCode);
            hDcpfTscCardNo = comc.rtrim(dtl.tscCashNo);

            showLogMessage("I", "", String.format("Line [%08d][%s] resp_code[%s] error!", totalCnt, hDcpfTscCardNo,
                    hDcpfRptRespCode));

            updateTscDcpfLog();
        }
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    void updateTscDcpfLog() throws Exception {
        daoTable = "tsc_dcpf_log";
        updateSQL = " rpt_resp_code = ?,";
        updateSQL += " rpt_resp_date = ?,";
        updateSQL += " rpt_resp_time = ?,";
        updateSQL += " proc_flag     = 'N',";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where tsc_card_no = ?  ";
        whereStr += " and file_name   = ? ";
        setString(1, hDcpfRptRespCode);
        setString(2, hTempRptRespDate);
        setString(3, hTempRptRespTime);
        setString(4, javaProgram);
        setString(5, hDcpfTscCardNo);
        setString(6, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcpf_log not found!", "", hCallBatchSeqno);
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
        TscD017 proc = new TscD017();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String ntype;
        String nattri;
        String rptRespCode;
        String type;
        String attri;
        String txType;
        String txRsn;
        String tscCashNo;
        String effcYymm;
        String tscMarketFlag;
        String id;
        String chiName;
        String chiNameSpec;
        String birthday;
        String telephone;
        String mobil;
        String zipCode;
        String addr;
        String email;
        String filler1;
        String hashValue;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(ntype, 1);
            rtn += comc.fixLeft(nattri, 2);
            rtn += comc.fixLeft(rptRespCode, 4);
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(txType, 1);
            rtn += comc.fixLeft(txRsn, 1);
            rtn += comc.fixLeft(tscCashNo, 20);
            rtn += comc.fixLeft(effcYymm, 4);
            rtn += comc.fixLeft(tscMarketFlag, 1);
            rtn += comc.fixLeft(id, 20);
            rtn += comc.fixLeft(chiName, 40);
            rtn += comc.fixLeft(chiNameSpec, 80);
            rtn += comc.fixLeft(birthday, 8);
            rtn += comc.fixLeft(telephone, 20);
            rtn += comc.fixLeft(mobil, 20);
            rtn += comc.fixLeft(zipCode, 5);
            rtn += comc.fixLeft(addr, 100);
            rtn += comc.fixLeft(email, 50);
            rtn += comc.fixLeft(filler1, 19);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.ntype = comc.subMS950String(bytes, 0, 1);
        dtl.nattri = comc.subMS950String(bytes, 1, 2);
        dtl.rptRespCode = comc.subMS950String(bytes, 3, 4);
        dtl.type = comc.subMS950String(bytes, 7, 1);
        dtl.attri = comc.subMS950String(bytes, 8, 2);
        dtl.txType = comc.subMS950String(bytes, 10, 1);
        dtl.txRsn = comc.subMS950String(bytes, 11, 1);
        dtl.tscCashNo = comc.subMS950String(bytes, 12, 20);
        dtl.effcYymm = comc.subMS950String(bytes, 32, 4);
        dtl.tscMarketFlag = comc.subMS950String(bytes, 36, 1);
        dtl.id = comc.subMS950String(bytes, 37, 20);
        dtl.chiName = comc.subMS950String(bytes, 57, 40);
        dtl.chiNameSpec = comc.subMS950String(bytes, 97, 80);
        dtl.birthday = comc.subMS950String(bytes, 177, 8);
        dtl.telephone = comc.subMS950String(bytes, 185, 20);
        dtl.mobil = comc.subMS950String(bytes, 205, 20);
        dtl.zipCode = comc.subMS950String(bytes, 225, 5);
        dtl.addr = comc.subMS950String(bytes, 230, 100);
        dtl.email = comc.subMS950String(bytes, 330, 50);
        dtl.filler1 = comc.subMS950String(bytes, 380, 19);
        dtl.hashValue = comc.subMS950String(bytes, 399, 16);
        dtl.fillerEnd = comc.subMS950String(bytes, 415, 2);
    }

}