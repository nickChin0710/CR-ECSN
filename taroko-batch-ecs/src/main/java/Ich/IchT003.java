/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/12/22  V1.01.00  Lai         program initial                           *
 *  109-12-16   V1.01.01    tanwei      updated for project coding standard    *
 *  112/05/17  V1.01.02    Wilson    procFTP調整                                                                                         *
 *  112/07/29  V1.01.03    Wilson    增加ARPB總檔procFTP                          *
 ******************************************************************************/

package Ich;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class IchT003 extends AccessDAO {
    private String progname = "特約機構(BnnB)媒體FTP傳送程式  112/07/29 V1.01.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int    debug  = 1;
    String stderr = "";
    String root    = String.format("%s/media/ich/", comc.getECSHOME());

    String hTnlgNotifyDate = "";
    String hPrevNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTnlgNotifyTime = "";
    String hTnlgFtpSendDate = "";
    String hTnlgFileName     = "";
    int    hTnlgRecordCnt    = 0;
    int    hTnlgRecordSucc   = 0;
    String hTnlgRowid = "";
    String hEflgFileName = "";
    String hEflgRowid = "";
    String hEflgFileDate = "";
    String hEflgProcCode = "";
    String hEflgProcDesc = "";

    String tmpstr1 = "";
    String tmpstr2 = "";
    String tmpstr3 = "";
    String fileSeq = "";
    String hEflgRefIpCode = "";
    String hEriaFileZipHidewd = "";
    String nUserpid = "";
    String tmpstr = "";
    String hHash = "";
    int forceFlag = 0;
    int totCntO = 0;
    int totCntI = 0;
    int succCnt = 0;
    int nRetcode = 0;
    int errCode = 0;
    String zipfile = "";
    int tmpInt  = 0;

    int out = -1;

    public int mainProcess(String[] args)
    {
        try
        {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            //if (comm.isAppActive(javaProgram)) {
            //    comc.err_exit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            //}
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchT003 [[notify_date][force_flag]] [force_flag]", "");
            }
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = this.getClass().getName();
            String hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }


            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTnlgNotifyDate = args[0];
            }
            if (args.length == 2) {
                hTnlgNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();

            if(debug==1)
                showLogMessage("I","","Process date=["+hTnlgNotifyDate+"]"+forceFlag+","+hPrevNotifyDate);

            if (forceFlag == 0) {
                errCode = selectIchNotifyLog();
                if (errCode == 1) {
                    exceptExit = 0;
                    comcr.errRtn("本日會員銀行通知檔媒體已處理完成, 不可再處理!(error)"
                            , "",comcr.hCallBatchSeqno);
                }
            }

            selectIchNotifyLogI();

            hTnlgRecordCnt = totCntO + totCntI;

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + hTnlgRecordCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)    comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex)
        { expMethod = "mainProcess"; expHandle(ex);
            return exceptExit;
        }
    }
    /***********************************************************************/
    void selectPtrBusinday() throws Exception
    {

        sqlCmd = "select business_date,";
        sqlCmd += "to_char(to_date(cast(decode( cast(? as varchar(8)), '', business_date, ?) as varchar(8)),'yyyymmdd')- 1,'yyyymmdd') h_prev_notify_date,";
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgNotifyDate);

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hPrevNotifyDate   = getValue("h_prev_notify_date");
            hTnlgNotifyDate   = getValue("h_tnlg_notify_date");
            hTnlgNotifyTime   = getValue("h_tnlg_notify_time");
        }

    }
    /***********************************************************************/
    int selectIchNotifyLog() throws Exception {
        sqlCmd  = "select ftp_send_date ";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where notify_date             = ?  ";
        sqlCmd += "   and decode(ftp_send_date,'','N',ftp_send_date) <> 'N'  ";
        sqlCmd += "   fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        if (recordCnt > 0) {
            hTnlgFtpSendDate = getValue("ftp_send_date");
        }

        return 1;
    }
    /***********************************************************************/
    void selectIchNotifyLogI() throws Exception
    {
        tmpstr1   = String.format("BRQA_%3.3s_%8.8s",comc.ICH_BANK_ID3,hTnlgNotifyDate);
//        zipfile = tmpstr1 + ".zip";

//        selectEcsRefIpAddr("ICH_SFTP_IN");

        String temstr2 = String.format("%s/%s", root , tmpstr1);
        temstr2   = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr2, "big5");

        hHash  = "0000000000000000000000000000000000000000";

        selectSQL  = " file_name  , record_cnt , record_succ, rowid rowid1 ";
        daoTable   = " ich_notify_log  ";
        whereStr   = " where notify_date   = ? ";
        whereStr  += "   and tran_type     = 'I' ";
        whereStr  += "   and decode(ftp_send_date,'','N',ftp_send_date) = 'N'  ";
        whereStr  += " order by file_name        ";
        setString(1, hTnlgNotifyDate);
        int recCnt = selectTable();
        if(debug==1) showLogMessage("I", "","Read 'I' file cnt=["+recCnt +"]"+hTnlgNotifyDate);

        for (int inti = 0; inti < recCnt; inti++)
        {
            hTnlgFileName   = getValue("file_name", inti);
            hTnlgRecordCnt  = getValueInt("record_cnt", inti);
            hTnlgRecordSucc = getValueInt("record_succ", inti);
            hTnlgRowid       = getValue("rowid1", inti);
            totCntI++;
            if(debug==1) showLogMessage("I", "","Read 'I' file=["+totCntI+"]"+hTnlgFileName);
            if(totCntI ==1)
            {
                tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%04d%6.6s%40.40s" ,
                        "B99B","01","0001",comc.ICH_BANK_ID3,recCnt," ",hHash);
                writeTextFile(out, tmpstr2 + "\r\n");
            }
            dateTime();
            tmpstr2 = String.format("D%-25.25s%08d%08d%08d%14.14s",hTnlgFileName
                    , hTnlgRecordCnt,hTnlgRecordSucc,0,sysDate+sysTime);
            writeTextFile(out, tmpstr2 + "\r\n");

            // ======================================================
          
            procFtp();
            renameFile1(hTnlgFileName);

//            tmpInt = comm.zipFile(root + hTnlgFileName , root + zipfile, hEriaFileZipHidewd);
//            if (tmpInt != 0) {
//                comcr.errRtn(String.format("無法壓縮檔案[%s]", hTnlgFileName), ""
//                        , comcr.hCallBatchSeqno);
//            }

//            moveBackup(hTnlgFileName);

            updateIchNotifyLog();
        }

        selectIchNotifyLogO();

        closeOutputText(out);
        if (recCnt > 0) {
//            tmpInt = comm.zipFile(root + tmpstr1, root+zipfile, hEriaFileZipHidewd);
//            if(tmpInt != 0)
//                comcr.errRtn(String.format("無法壓縮檔案[%s]", tmpstr1),"",comcr.hCallBatchSeqno);
        	hTnlgFileName = tmpstr1;
            procFtp();
            renameFile1(hTnlgFileName);
        }

    }
    /***********************************************************************/
    void selectIchNotifyLogO() throws Exception
    {
        totCntO = 0;
        tmpstr   = String.format("ARPB_%3.3s_%8.8s",comc.ICH_BANK_ID3,hTnlgNotifyDate);
        tmpstr3  = String.format("ARQB_%3.3s_%8.8s",comc.ICH_BANK_ID3,hPrevNotifyDate);
        comc.fileCopy(root + tmpstr3, root +tmpstr);
        hHash  = "0000000000000000000000000000000000000000";

        selectSQL  = " file_name  , record_cnt , record_succ, rowid rowid1 ";
        daoTable   = " ich_notify_log  ";
        whereStr   = " where notify_date   = ? ";
        whereStr  += "   and tran_type     = 'O' ";
        whereStr  += "   and decode(ftp_send_date,'','N',ftp_send_date) = 'N'   ";
        whereStr  += "   and decode(file_iden ,'','N',file_iden )   <> 'N'   ";
        whereStr  += " order by file_name        ";
        setString(1, hPrevNotifyDate);
//    setString(2, h_tnlg_notify_date);
        int recCnt = selectTable();
        if(debug==1) showLogMessage("I", "","Read 'O' file cnt=["+recCnt +"]"+hPrevNotifyDate);

        for (int inti = 0; inti < recCnt; inti++)
        {
            hTnlgFileName   = getValue("file_name", inti);
            hTnlgRecordCnt  = getValueInt("record_cnt", inti);
            hTnlgRecordSucc = getValueInt("record_succ", inti);
            hTnlgRowid       = getValue("rowid1", inti);
            totCntO++;
            if(debug==1) showLogMessage("I", "","Read 'O' file=["+totCntO+"]"+hTnlgFileName);

            dateTime();

            if(hTnlgFileName.substring(1,3).equals("RQ") )
            {
                hTnlgFileName = hTnlgFileName.substring(0,1)+"RP"+hTnlgFileName.substring(3,9)
                        + hTnlgNotifyDate + hTnlgFileName.substring(17);
            }
            if(debug==1) showLogMessage("I", "","Read 'O' NEW file=["+hTnlgFileName+"]");
            if(totCntO ==1)
            {
                tmpstr2 = String.format("D%-25.25s%08d%08d%08d%14.14s",tmpstr
                        , 0,0,0,sysDate+sysTime);
                writeTextFile(out, tmpstr2 + "\r\n");;
            }
            dateTime();
            tmpstr2 = String.format("D%-25.25s%08d%08d%08d%14.14s",hTnlgFileName
                    , hTnlgRecordCnt,hTnlgRecordSucc,0,sysDate+sysTime);
            writeTextFile(out, tmpstr2 + "\r\n");
            
         // ======================================================
            
            procFtp();
            renameFile1(hTnlgFileName);

//            tmpInt = comm.zipFile(root + hTnlgFileName, root+zipfile, hEriaFileZipHidewd);
//            if (tmpInt != 0) {
//                comcr.errRtn(String.format("無法壓縮檔案[%s]", hTnlgFileName), ""
//                        , comcr.hCallBatchSeqno);
//            }

//            moveBackup(hTnlgFileName);

            updateIchNotifyLog();
        }

        // 總檔
        if(recCnt > 0)
        {
//            tmpInt = comm.zipFile(root + tmpstr, root+zipfile, hEriaFileZipHidewd);
//            if(tmpInt != 0)
//                comcr.errRtn(String.format("無法壓縮檔案[%s]", tmpstr), "", comcr.hCallBatchSeqno);
//            moveBackup(tmpstr);
        	hTnlgFileName = tmpstr;
            procFtp();
            renameFile1(hTnlgFileName);
            renameFile1(tmpstr3);
        }

    }
    /***********************************************************************/
    long getFileSize(String spFileName) {
        long lFilelength =  comc.getFileLength(spFileName);

        return lFilelength;
    }
    /***********************************************************************/
    void procFtp() throws Exception
    {

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用  (必要)*/
        commFTP.hEflgSystemId   = "ICH_FTP_PUT";   /* 區分不同類的 FTP 檔案-大類     (必要)*/
        commFTP.hEflgGroupId    = "000000";        /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
        commFTP.hEflgSourceFrom = "ICH_FTP";      /* 區分不同類的 FTP 檔案-細分類 (非必要)*/
        commFTP.hEriaLocalDir   = String.format("%s/media/ich", comc.getECSHOME());
        commFTP.hEflgModPgm     = this.getClass().getName();
        String hEflgRefIpCode  = "ICH_FTP_PUT";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s", hTnlgFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

        if (errCode != 0) {
            comcr.errRtn(String.format("FTP error"), "", comcr.hCallBatchSeqno);
        }
        
    }
    /***********************************************************************/
  	void renameFile1(String removeFileName) throws Exception {
  		String tmpstr1 = comc.getECSHOME() + "/media/ich/" + removeFileName;
  		String tmpstr2 = comc.getECSHOME() + "/media/ich/backup/" + removeFileName;
  		
  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
  			return;
  		}
  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
  	}
  	
    /****************************************************************************/ 
    void updateIchNotifyLog() throws Exception {
        daoTable   = "ich_notify_log";
        updateSQL  = " ftp_send_date = ?,";
        updateSQL += " ftp_send_time = ?,";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_time      = sysdate";
        whereStr   = "where rowid    = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyTime);
        setString(3, javaProgram);
        setRowId(4, hTnlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "",comcr.hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void selectEcsRefIpAddr(String ipCode) throws Exception
    {
        hEriaFileZipHidewd = "";

        sqlCmd  = "select file_zip_hidewd ";
        sqlCmd += "  from ecs_ref_ip_addr ";
        sqlCmd += " where ref_ip_code = ? ";
        setString(1, ipCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ecs_ref_ip_addr not found!", ipCode
                    , comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hEriaFileZipHidewd = getValue("file_zip_hidewd");
        }
    }
    /***********************************************************************/
//    void moveBackup(String moveFile) throws Exception
//    {
//        String src    = String.format("%s/%s", root, moveFile);
////String target = String.format("%s/BACKUP/%s/%s.BAK", root, h_tnlg_notify_date, move_file);
//        String target = String.format("%s/BACKUP/%s/%s"    , root, hTnlgNotifyDate, moveFile);
//
//        if(debug==1) showLogMessage("I", "","MOVE_BACK=["+src+"]"+target);
//
//        comc.fileMove(src, target);
//    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception
    {
        IchT003 proc = new IchT003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
/***********************************************************************/
}
