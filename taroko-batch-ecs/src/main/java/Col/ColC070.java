/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 106/08/17  V1.01.01  phopho     Initial                                    *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*****************************************************************************/
package Col;

import com.*;

public class ColC070 extends AccessDAO {
    private String progname = "LGD 檔案FTP JCIC 處理程式 109/12/15  V1.00.01  ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    int debug = 0;
    int debug1 = 0;
    String hBusiBusinessDate = "";

    String hEflgFileName = "";
    long hEflgTransSeq = 0;
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEflgTransSeqno = "";
    String hEflgProcCode = "";
    String hEflgProcDesc = "";
    String hEflgRowid = "";
    String hEriaLocalDir = "";

//    File fptr1;
    int errCode;
    int okFlag = 0;

    String hTempSysdate = "";
    String tmpstr = "";
    String transSeqno = "";
    String tempFilename = "";

    // ???
    // typedef struct
    // {
    // char err_code[2];
    // char msg_code[2];
    // char msg_desc[301];
    // } ecs_ftp_buf;
    //
    // ecs_ftp_buf ecsftp;
    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC070 proc = new ColC070();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (comm.isAppActive(javaProgram)) {
                String err1 = "comm.isAppActive    error";
                String err2 = "";
                comc.errExit(err1, err2);
            }

            // 檢查參數
            if (args.length > 1) {
                String err1 = "Usage : ColC070 [file_name]";
                String err2 = "";
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                String err1 = "connect DataBase     error";
                String err2 = "";
                comc.errExit(err1, err2);
            }
            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            tempFilename = "";
            hBusiBusinessDate = "";
            if (args.length == 1) {
                if (new CommDate().isDate(args[0])) {
                    hBusiBusinessDate = args[0];
                } else {
                    tempFilename = args[0];
                }
            }

            selectPtrBusinday();
            showLogMessage("I", "", "本日[" + hBusiBusinessDate + "]");
            showLogMessage("I", "", "=========================================");
            transSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (非必要) */
            showLogMessage("I", "", "開始FTP檔案.....");
            ftpFile();
            commFTP.hEflgTransSeqno = transSeqno;
            selectEcsFtpLog();
            commitDataBase();

            showLogMessage("I", "", "=========================================");
            showLogMessage("I", "", "程式執行結束");

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
    // ************************************************************************

    private void selectPtrBusinday() throws Exception {
        selectSQL = "decode(cast(? as varchar(8)), '',business_date, cast(? as varchar(8))) business_date ";
        daoTable = "ptr_businday";
        whereStr = "fetch first 1 row only";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno );
        }
        hBusiBusinessDate = getValue("business_date");
    }

    // ************************************************************************
    private void ftpFile() throws Exception {
        int inta, fileCnt = 2;
        ;
        String fileName = "";
        String fileStr[] = { "006", "006", "" };
        String tailStr[] = { "9", "9", "" };
        String extStr[] = { ".901", ".902", "" };

        if (tempFilename.length() != 0)
            fileCnt = 1;
        for (inta = 0; inta < fileCnt; inta++) {
            commFTP.hEflgSystemId = "LGD_JCIC"; /* 區分不同類的 FTP 檔案-大類 (必要) */
            commFTP.hEflgTransSeqno = transSeqno;
            commFTP.hEflgGroupId = extStr[inta]; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = "LGD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir = comc.getECSHOME() + "/media/col";
            System.setProperty("user.dir", commFTP.hEriaLocalDir);
            // commFTP.h_eflg_mod_pgm = javaProgram;

            if (fileCnt == 1) {
                fileName = tempFilename;
            } else {
                fileName = fileStr[inta] + hBusiBusinessDate + tailStr[inta] + extStr[inta];
            }

            showLogMessage("I", "", "檔案[" + fileName + "] 開始傳送....");

            tmpstr = "put " + fileName;

            errCode = commFTP.ftplogName("JCIC_FTP", tmpstr);
            
            //phopho add 2019.5.27 無檔案的return message須自己判斷
            if (errCode == 0 && commFTP.fileList.size() == 0) {
            	showLogMessage("I", "", String.format("[%-20.20s] => [無資料可傳送]", tmpstr));
            }

            if (errCode != 0) {
                showLogMessage("I", "", "[" + tmpstr + "] => [無法接收資料]");
                showLogMessage("I", "", "     => msg_code[] return_code[" + errCode + "]");
                //showLogMessage("I", "", "     => ERROR_MSG[" + commFTP.h_eflg_ftp_desc + "]");
                continue;
            }

            if (inta == 0)
                okFlag = 1;
            showLogMessage("I", "", "檔案[" + fileName + "] FTP完成.....");
        }
        /**********
         * COMM_FTP common function usage
         ****************************************/
    }

    // ************************************************************************
    private void selectEcsFtpLog() throws Exception {
        String tmpstr = "";
        selectSQL = "file_name, to_char(sysdate,'ddhh24miss') temp_sysdate, rowid as rowid ";
        daoTable = "ecs_ftp_log";
        whereStr = "where trans_seqno = ? and   system_id = 'LGD_JCIC' and   trans_resp_code = 'Y' ";
        setString(1, commFTP.hEflgTransSeqno);

        openCursor();
        while (fetchTable()) {
            hEflgFileName = getValue("file_name");
            hTempSysdate = getValue("temp_sysdate");
            hEflgRowid = getValue("rowid");
            hEflgProcCode = "0";
            hEflgProcDesc = "";

            commFTP.hEriaLocalDir = comc.getECSHOME() + "/media/col";
            System.setProperty("user.dir", commFTP.hEriaLocalDir);

//            tmpstr = comc.GetECSHOME() + "/media/col/" + h_eflg_file_name + "." + h_temp_sysdate;
            tmpstr = hEflgFileName + "." + hTempSysdate;
            showLogMessage("I", "", "  檔案: [" + hEflgFileName + "]");
            showLogMessage("I", "", "  更名: [" + tmpstr + "]");

//            File oldfile = new File(h_eflg_file_name);
//            File newfile = new File(tmpstr);
//            oldfile.renameTo(newfile);
            
            String fs = String.format("%s/media/col/%s", comc.getECSHOME(), hEflgFileName);
            String ft = String.format("%s/media/col/%s.%s", comc.getECSHOME(), hEflgFileName, hTempSysdate);
            if (comc.fileRename2(fs, ft) == false) {
                showLogMessage("I","", String.format("無法搬移[%s]", fs));
            }

            updateEcsFtpLog();
        }
        closeCursor();
    }

    // ************************************************************************
    private void updateEcsFtpLog() throws Exception {
        updateSQL = "file_date  = ?, proc_code  = ?, proc_desc  = ?, mod_pgm    = ?, "
                + "mod_time   = sysdate ";
        daoTable = "ecs_ftp_log";
        whereStr = "WHERE rowid = ?";
        setString(1, hBusiBusinessDate);
        setString(2, hEflgProcCode);
        setString(3, hEflgProcDesc);
        setString(4, javaProgram);
        setRowId(5, hEflgRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_ecs_ftp_log error!";
            String err2 = "rowid=[" + hEflgRowid + "]";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }
    }
    // ************************************************************************
}