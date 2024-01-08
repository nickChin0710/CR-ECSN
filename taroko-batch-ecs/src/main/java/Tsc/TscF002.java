/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109-11-16  V1.00.01    tanwei    updated for project coding standard       *
 *  110-07-16  V1.00.02    Castor    add Debit-card process                    *
 *  112-05-02  V1.00.03    Alex      add FILE FTP TO /crdatacrea/FAPB2S        *
 *  112-06-05  V1.00.04    Alex      限制 3000 筆                                                                                    *
 *  112-08-10  V1.00.05    Wilson    換行要0D0A                                  *
 *  112-09-18  V1.00.06    Wilson    mark取3000筆                                                                                          *
 *  112-09-20  V1.00.07    Wilson    排除重複資料                                                                                              *
 *  112-09-28  V1.00.08    Wilson    檔案只產生3000筆資料                                                                              *
 *  112-10-06  V1.00.09    Wilson    有產生檔案的資料才異動報送日期時間                                                    *
 *  112/12/19  V1.00.10    Wilson    檔名日期加一日                                                                                          *
 ******************************************************************************/

package Tsc;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommIps;
import com.CommRoutine;

/*悠遊卡黑名單檔(BKEC)媒體產生程式*/
public class TscF002 extends AccessDAO {
    private final String progname = "悠遊卡黑名單檔(BKEC)媒體產生程式   112/12/19 V1.00.10";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommIps comips = new CommIps();
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;
	CommRoutine comr = null;
	
    int    debug = 1;
    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hBkecMediaCreateDate = "";
    String hBkecMediaCreateTime = "";
    String hPrevSysdate = "";
    String hBkecTscCardNo = "";
    String hBkecOppostDate = "";
    String hBkecCrtTime = "";
    String hBkecRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    int hTspmBlockLimitRows = 0;
    int tempInt = 0;

    int forceFlag = 0;
    int totalCnt = 0;
    int hTnlgRecordCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr1 = "";

    int out = -1;
    
    List<String> tscCardNoArr = new ArrayList<String>();

    public int mainProcess(String[] args) throws Exception {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscF002 [notify_date] [force_flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8) {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hTnlgNotifyDate = sgArgs0;
                }
            }
            if (args.length == 2) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTnlgNotifyDate = sgArgs0;
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();
            tmpstr1 = String.format("BKEC.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            showLogMessage("I", "", "Process File=["+forceFlag+"]"+hTnlgFileName);

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscBkecLoga();
            }

//            selectTscStmtParm();
//            if(debug==1) showLogMessage("I", "", "  Parm cnt=["+hTspmBlockLimitRows+"]");
//            hTspmBlockLimitRows = 3000;
            fileOpen();
            updateTscCard();
            updateTscVdCard();
            selectTscBkecLog();
            hTnlgRecordCnt = totalCnt;
            fileClose();

            showLogMessage("I", "", String.format("Process records = [%d]\n", totalCnt));
            updateTscBkecLogb();
            
            //--FTP
//            commFTP = new CommFTP(getDBconnect(), getDBalias());
//            comr = new CommRoutine(getDBconnect(), getDBalias());
//            procFTP();
//            renameFile();
            
            // ==============================================
            // 固定要做的

            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            if (out != -1) {
                closeOutputText(out);
                out = -1;
            }
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    void procFTP() throws Exception {    	
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
    	commFTP.hEflgSystemId = "TSC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    	commFTP.hEriaLocalDir = String.format("%s/media/tsc", comc.getECSHOME());
    	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    	commFTP.hEflgModPgm = javaProgram;

    	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
    	showLogMessage("I", "", "mput " + hTnlgFileName + " 開始傳送....");
    	int errCode = commFTP.ftplogName("TSC_FTP_PUT", "mput " + hTnlgFileName);

    	if (errCode != 0) {
    		showLogMessage("I", "", "ERROR:無法傳送 " + hTnlgFileName + " errcode:" + errCode);
    		insertEcsNotifyLog(hTnlgFileName);
    	}    	    	
    }

    //=====================
    public int insertEcsNotifyLog(String fileName) throws Exception {
    	setValue("crt_date", sysDate);
    	setValue("crt_time", sysTime);
    	setValue("unit_code", comr.getObjectOwner("3", javaProgram));
    	setValue("obj_type", "3");
    	setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
    	setValue("notify_name", "媒體檔名:" + fileName);
    	setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
    	setValue("notify_desc2", "");
    	setValue("trans_seqno", commFTP.hEflgTransSeqno);
    	setValue("mod_time", sysDate + sysTime);
    	setValue("mod_pgm", javaProgram);
    	daoTable = "ecs_notify_log";

    	insertTable();

    	return (0);
    }

    //=====================
    void renameFile() throws Exception {
    	String tmpstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
    	String tmpstr2 = String.format("%s/media/tsc/backup/%s", comc.getECSHOME(), hTnlgFileName);

    	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "ERROR : 檔案[" + hTnlgFileName + "]更名失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + hTnlgFileName + "] 已移至 [" + tmpstr2 + "]");    	        	
    }
    
    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        hPrevSysdate = "";
        sqlCmd = "select to_char(add_days(to_date(business_date,'yyyymmdd'),1),'yyyymmdd') h_business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_bkec_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_bkec_media_create_time,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_prev_sysdate ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hBkecMediaCreateDate = getValue("h_bkec_media_create_date");
        hBkecMediaCreateTime = getValue("h_bkec_media_create_time");
        hPrevSysdate = getValue("h_prev_sysdate");

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd = "select media_crt_date,";
        sqlCmd += "ftp_send_date ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_crt_date");
            hTnlgFtpSendDate = getValue("ftp_send_date");
        } else {
            return 0;
        }

        if (hTnlgFtpSendDate.length() != 0) {
            showLogMessage("I", "", String.format("通知檔 [%s] 已FTP至TSCC, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            showLogMessage("I", "", String.format("通知檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscBkecLoga() throws Exception {
        daoTable = "tsc_bkec_log";
        updateSQL = "proc_flag = 'N'   , ";
        updateSQL += "mod_time  = sysdate ";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_bkec_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectTscStmtParm() throws Exception {
        hTspmBlockLimitRows = 0;

        sqlCmd = "select block_limit_rows ";
        sqlCmd += " from tsc_stmt_parm  ";
        sqlCmd += "where apr_flag = 'Y' ";
        sqlCmd += "fetch first 1 rows only";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_stmt_parm not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTspmBlockLimitRows = getValueInt("block_limit_rows");
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        comc.mkdirsFromFilenameWithPath(temstr1);
        out = openOutputText(temstr1, "big5");
        if (out == -1) {
            comcr.errRtn(String.format("產生檔案有錯誤[%s]", temstr1), "", hCallBatchSeqno);
        }
        tmpstr1 = String.format("HBKEC%8.8s%8.8s%6.6s%53.53s", comc.TSCC_BANK_ID8, hBkecMediaCreateDate,
                hBkecMediaCreateTime, " ");

        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        String buf = String.format("%-80.80s%16.16s", tmpstr1, tmpstr2);
        writeTextFile(out, buf + "\r\n");
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%08d%71.71s", totalCnt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        writeTextFile(out, String.format("%-80.80s%16.16s\r\n", tmpstr1, new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }

    }

    /***********************************************************************/
    void updateTscCard() throws Exception {
if(debug==1) showLogMessage("I", "", "  update tsc_card =["+hPrevSysdate+"]");
        daoTable = "tsc_card";
        updateSQL = " blacklt_e_date = ?,";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
        whereStr = "where blacklt_s_date <> ''  ";
        whereStr += "  and blacklt_e_date  = ''  ";
        whereStr += "  and tsc_card_no not in ( select tsc_card_no FROM tsc_bkec_log a where 1 = 1  ";
        whereStr += "  and a.proc_flag = 'N'  ";
        whereStr += "  and not exists ( select tsc_card_no from tsc_bkec_expt c where c.tsc_card_no  = a.tsc_card_no  ";
        whereStr += "  and ( c.black_flag = '3'  ";
        whereStr += "  and to_char(sysdate,'yyyymmdd') between decode(c.send_date_s,'','19000101',c.send_date_s)  ";
        whereStr += "and decode(c.send_date_e,'','29991231', c.send_date_e) ) ) ) ";
        setString(1, hPrevSysdate);
        setString(2, javaProgram);
        updateTable();

if(debug==1) showLogMessage("I", "", "  update tsc_card end =["+hPrevSysdate+"]");
    }

    /***********************************************************************/
    void updateTscVdCard() throws Exception {
if(debug==1) showLogMessage("I", "", "  update tsc_vd_card =["+hPrevSysdate+"]");
        daoTable = "tsc_vd_card";
        updateSQL = " blacklt_e_date = ?,";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
        whereStr = "where blacklt_s_date <> ''  ";
        whereStr += "  and blacklt_e_date  = ''  ";
        whereStr += "  and tsc_card_no not in ( select tsc_card_no FROM tsc_bkec_log a where 1 = 1  ";
        whereStr += "  and a.proc_flag = 'N'  ";
        whereStr += "  and not exists ( select tsc_card_no from tsc_bkec_expt c where c.tsc_card_no  = a.tsc_card_no  ";
        whereStr += "  and ( c.black_flag = '3'  ";
        whereStr += "  and to_char(sysdate,'yyyymmdd') between decode(c.send_date_s,'','19000101',c.send_date_s)  ";
        whereStr += "and decode(c.send_date_e,'','29991231', c.send_date_e) ) ) ) ";
        setString(1, hPrevSysdate);
        setString(2, javaProgram);
        updateTable();

if(debug==1) showLogMessage("I", "", "  update tsc_vd_card end =["+hPrevSysdate+"]");
    }

    /***********************************************************************/
    void selectTscBkecLog() throws Exception {    	
    	
if(debug==1) showLogMessage("I", "", "  select_tsc_bkec_log");
        sqlCmd = "select ";
        sqlCmd += "a.tsc_card_no,";
        sqlCmd += "decode(a.oppost_date,'',a.crt_date,a.oppost_date) h_bkec_oppost_date,";
        sqlCmd += "a.crt_time ";
//        sqlCmd += "rowid as rowid ";
        sqlCmd += "from tsc_bkec_log a ";
        sqlCmd += "where 1 = 1 ";
        sqlCmd += "and a.proc_flag = 'N' ";
        sqlCmd += "and not exists ";
        sqlCmd += "( select tsc_card_no ";
        sqlCmd += "from tsc_bkec_expt c ";
        sqlCmd += "where c.tsc_card_no = a.tsc_card_no ";
        sqlCmd += "and ( ";
        sqlCmd += "c.black_flag = '3' and ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') ";
        sqlCmd += "between decode(c.send_date_s,'','19000101',c.send_date_s) ";
        sqlCmd += "and decode(c.send_date_e,'','29991231',c.send_date_e) ";
        sqlCmd += ") ";
        sqlCmd += ") ";
        sqlCmd += "order by a.order_seqno ";
//        sqlCmd += " fetch first 3000 rows only ";
        openCursor();
        while (fetchTable()) {
            hBkecTscCardNo = getValue("tsc_card_no");
            hBkecOppostDate = getValue("h_bkec_oppost_date");
            hBkecCrtTime = getValue("crt_time");
//            hBkecRowid = getValue("rowid");
            
            String dupFlag = "N";
            
            for(int i = 0; i < tscCardNoArr.size(); i++) {
                if(hBkecTscCardNo.equals(tscCardNoArr.get(i))) {
                	dupFlag = "Y";
                	continue;
                }
            }

            if(dupFlag.equals("Y")) {
            	continue;
            }
            
            tscCardNoArr.add(hBkecTscCardNo);
            
            //檔案只產生3000筆資料
            if(totalCnt >= 3000) {
            	updateTscBkecLog(2);
            	continue;
            }
            
            tmpstr1 = String.format("D01%-20.20s%-8.8s%-6.6s%43.43s", hBkecTscCardNo, hBkecOppostDate,
                    hBkecCrtTime, " ");
            byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
            tmpstr2 = new String(tmp);
            String buf = String.format("%-80.80s%16.16s", tmpstr1, tmpstr2);
            writeTextFile(out, buf + "\r\n");

            updateTscBkecLog(1);
            totalCnt++;
            if (totalCnt % 1000 == 0 || totalCnt == 1)
                showLogMessage("I","",String.format("tsc_bkec_log Process record=[%d]",totalCnt));

        }
        closeCursor();
if(debug==1) showLogMessage("I", "", "  select_tsc_bkec_log end ");

    }

    /***********************************************************************/
    void updateTscBkecLog(int idx) throws Exception {
        daoTable = "tsc_bkec_log";
        
        updateSQL = " notify_date    = ?,";
        
        if(idx == 1) {
            updateSQL += " media_crt_date = ?,";
            updateSQL += " media_crt_time = ?,";
            updateSQL += " file_name      = ?,";
        }

        updateSQL += " proc_flag      = 'Y',";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
//        whereStr = "where rowid     = ? ";
        whereStr   = "where proc_flag = 'N' and tsc_card_no = ? ";
        
        int i = 1;
        
        setString(i++, hTnlgNotifyDate);
        
        if(idx == 1) {
            setString(i++, hBkecMediaCreateDate);
            setString(i++, hBkecMediaCreateTime);
            setString(i++, hTnlgFileName);
        }

        setString(i++, javaProgram);
//        setRowId(i++, hBkecRowid);
        setString(i++, hBkecTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_bkec_log not found!", "tsc_card_no = " + hBkecTscCardNo, hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscBkecLogb() throws Exception {
        daoTable = "tsc_bkec_log";
        updateSQL = "proc_flag       = 'U',";
        updateSQL += "mod_pgm         = ?,";
        updateSQL += "mod_time        = sysdate ";
        whereStr = "where proc_flag   = 'N'";
        setString(1, javaProgram);
        updateTable();
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF002 proc = new TscF002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
