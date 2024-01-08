/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/06/21  V1.00.00    David     program initial                           *
*  109/11/18  V1.00.01    tanwei    updated for project coding standard       *
*  112/05/12  V1.00.02    Sunny     change file name                          *
*  112/08/07  V1.00.03    Ryan      修正FTP傳送 ,增加備份處理                                                                    *
*  112/08/09  V1.00.04    Ryan      保留新版送法                                                                                             *
******************************************************************************/

package Sms;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;

public class SmsE090 extends AccessDAO {

    private String progname = "電子發票-信用卡實體卡及虛擬卡對照異動檔產生處理程式   112/08/07 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate  commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;
    CommFTP commFTP = null;

    String isFileName = "CF310-EK0002-006-"+ commDate.sysDate() +"-001-P.txt";
    
    String hCallBatchSeqno = "";
    private String hBusiBusinessDate = "";
    private String hFileDate = "";
    private String hFileTime = "";
    private int fileSeq = 0;
    private int fptr = 0;
    private int totalCnt = 0;
    private String hHardCardNo = "";
    private String hHardVCardNo = "";
    private String hHardRowid = "";
    List<String> lpar = new ArrayList<String>();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            if ((args.length != 0) && (args.length != 1)) {
                comc.errExit("Usage :  SmsE090 ", "");
            }
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      		commFTP = new CommFTP(getDBconnect(), getDBalias());
    		comr = new CommRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            selectPtrBusinday();

            totalCnt = 0;

            openFile();

            selectHceCard();

            closeFile();
            
            showLogMessage("I", "", String.format("累計新增 [%d] 筆", totalCnt));
            
            procFTP();
            renameFile();
            showLogMessage("I","","=========================================");
            showLogMessage("I","","產生送出紀錄檔");
            showLogMessage("I","","=========================================");

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /**************************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = " select business_date, ";
        sqlCmd += " to_char(sysdate,'yyyymmdd') as h_file_date, ";
        sqlCmd += " to_char(sysdate,'hh24miss') as h_file_time ";
        sqlCmd += " from   ptr_businday ";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
        hFileDate = getValue("h_file_date");
        hFileTime = getValue("h_file_time");
    }

    /*************************************************************************/
    void selectHceCard() throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();
        lpar.clear();

        sqlCmd = "SELECT  card_no, ";
        sqlCmd += " v_card_no,";
        sqlCmd += " rowid as rowid";
        sqlCmd += " FROM    hce_card";
        sqlCmd += " WHERE   snd_date = '' ";

        openCursor();
        while (fetchTable()) {

            hHardCardNo = getValue("card_no");
            hHardVCardNo = getValue("v_card_no");
            hHardRowid = getValue("rowid");

            totalCnt++;

            /* card_no */
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(h_hard_card_no.getBytes(StandardCharsets.UTF_8));
            byte[] hash = hexStrToByteArr(comm.sha256(hHardCardNo));

            String tmpstr1 = "";
            tmpstr1 = encoder.encodeToString(hash);
            //String tmpstr = String.format("%6.6s%s", hHardCardNo, tmpstr1);
            String tmpstr = String.format("%6.6s%s", "B00006", tmpstr1); /* 新版BIN */

            /* v_card_no */
            hash = null;
//            hash = digest.digest(h_hard_v_card_no.getBytes(StandardCharsets.UTF_8));
            hash = hexStrToByteArr(comm.sha256(hHardVCardNo));
            
            tmpstr1 = "";
            tmpstr1 = encoder.encodeToString(hash);
            String tmpstrV = String.format("%6.6s%s", hHardVCardNo, tmpstr1);
            String tmpstrVNew = String.format("%6.6s%s", "B00006", tmpstr1);

//            lpar.add(String.format("%1.1s%-64.64s%-64.64s%-100.100s%4.4s\n", "A", tmpstr, tmpstrV, " ", "0000"));
            lpar.add(String.format("%1.1s%-64.64s%-64.64s%-100.100s%4.4s\n", "A", tmpstr, tmpstrVNew, " ", "0000")); /* 新版送法 */

            updateHceCard();
        }
        closeCursor();
    }

    /**************************************************************************/
    void openFile() throws Exception {
        fileSeq++;
        
        /*CF310-EK0002-006-YYYYMMDD-001-P.txt*/
        //String fileName = String.format("O028017%-8.8s%02dC", hFileDate, fileSeq);
 
        String parmFile = String.format("%s/media/sms/%s", comc.getECSHOME(), isFileName);

        showLogMessage("I", "", String.format("媒體檔位置[%s]", parmFile));

        fptr = openOutputText(parmFile, "MS950");
        if (fptr == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", isFileName), "", hCallBatchSeqno);
        }
    }

    /**************************************************************************/
    void closeFile() throws Exception {
        writeTextFile(fptr, String.format("%8.8s%6.6s%010d%10.10s%199.199s\n", hFileDate, hFileTime, totalCnt, "0000000000", " "));
        for (String str : lpar) {
            writeTextFile(fptr, str);
        }
        closeOutputText(fptr);
    }

    /*****************************************************************************/
    void updateHceCard() throws Exception {
        daoTable = "hce_card";
        updateSQL = "snd_date = ?,";
        updateSQL += "snd_time = ? ";
        whereStr = "where  rowid = ? ";
        setString(1, hFileDate);
        setString(2, hFileTime);
        setRowId(3, hHardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_hce_card not found!", "", hCallBatchSeqno);
        }

    }
    
  //***********************************************************************/
  	void procFTP() throws Exception {
  		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  		commFTP.hEflgSystemId = "FISC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
  		commFTP.hEriaLocalDir = String.format("%s/media/sms/", comc.getECSHOME());
  		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  		commFTP.hEflgModPgm = javaProgram;

  		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
  		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
  		int errCode = commFTP.ftplogName("FISC_FTP_PUT", "mput " + isFileName);

  		if (errCode != 0) {
  			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
  			insertEcsNotifyLog(isFileName);
  		}
  	}
  	
	void renameFile() throws Exception {
		String tmpStr1 = Paths.get(comc.getECSHOME(), "/media/sms/", isFileName).toString();
		String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
		String tmpStr2 = String.format("%s/media/sms/backup/%s_%s", comc.getECSHOME(), isFileName, sysDate + sysTime);
		String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

		if (!comc.fileMove(tempPath1, tempPath2)) {
			showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]移動失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + isFileName + "] 移動至 [" + tempPath2 + "]");
	}
  	
  // ************************************************************************

  	int insertEcsNotifyLog(String fileName) throws Exception
  	 {
  	  dateTime();
  	  extendField = "noti.";
  	  setValue("noti.crt_date"           , sysDate);
  	  setValue("noti.crt_time"           , sysTime);
  	  setValue("noti.unit_code"          , comr.getObjectOwner("3",javaProgram));
  	  setValue("noti.obj_type"           , "3");
  	  setValue("noti.notify_head"        , "無法產生 信用卡實體卡及虛擬卡對照異動檔 資料");
  	  setValue("noti.notify_name"        , "媒體檔名:"+fileName);
  	  setValue("noti.notify_desc1"       , "程式 SmsE090 無法產生 信用卡實體卡及虛擬卡對照異動檔 資料");
  	  setValue("noti.notify_desc2"       , "");
  	  setValue("noti.trans_seqno"        , "");
  	  setValue("noti.mod_time"           , sysDate+sysTime);
  	  setValue("noti.mod_pgm"            , javaProgram);
  	  daoTable  = "ecs_notify_log";

  	  insertTable();

  	  return(0);
  	 }

    /*****************************************************************************/
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        SmsE090 proc = new SmsE090();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
