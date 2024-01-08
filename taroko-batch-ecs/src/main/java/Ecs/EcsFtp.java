/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/06  V1.00.01  Sunny               ECS FTP收檔共用程式     *
* 109/07/08  V1.00.02  JustinWu          增加動態替換檔名方法以及檢核帶入參數  
* 109/07/13  V1.00.03  JustinWu          renameTo -> fileMove*
* 109/07/14  V1.00.04  JustinWu          business day -> sysDate
* 109/07/15  V1.00.05  JustinWu          file_date可填BUSINDAY, SYSDAY,或指定日期
* 109/09.04  V1.00.06  yanghan     解决Portability Flaw: Locale Dependent Comparison问题    * 
* 109/09/13  v1.00.07  Sunny             cancel isAppActive                   *
* 109-10-19  V1.00.08  shiyuqi       updated for project coding standard     *
* 110/07/16  V1.00.09  Justin      only allow MOVE_TYPE to move data to backup folder
******************************************************************************/
package Ecs;

import com.*;

import java.nio.file.Paths;
import java.util.*;


@SuppressWarnings("unchecked")
public class EcsFtp extends AccessDAO {
	private static final String MOVE_TYPE = "0";
    private static final String FTP_TYPE = "1";
    private static final String SFTP_TYPE = "2";  
	private String progname = "ECS FTP收檔共用程式 110/07/16  V1.00.09";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommFTP commFTP = null;
    CommRoutine comr = null;

    String fileDate = "";


    // ************************************************************************
    public static void main(String[] args) throws Exception {
        EcsFtp proc = new EcsFtp();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }

    // ************************************************************************
    public int mainProcess(String[] args) {
        String argFileName = "";
        String argGroupId = "";
        String argRefIpCode = "";
        String argRequire = "";
        String fileName = "";

        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            /* 20200930 cancel
            if (comm.isAppActive(javaProgram)) {
                showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
                return (1);
            }
            */

            /* 參數只有定義ref_ip_code而沒有system_id，是認為ref_ip_code應該要等於system_id */
            if (args.length != 5 ) {
                showInputHint();
                return (1);
            }

            if (!connectDataBase())
                return (1);

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            argFileName = args[0];
            argRefIpCode = args[1].toUpperCase(Locale.TAIWAN); // 一律轉大寫
            argGroupId = args[2].toUpperCase(Locale.TAIWAN); // 一律轉大寫
            
            if (!Arrays.asList("0", "1").contains(args[3])) {
            	showInputHint();
            	showLogMessage("E", "", String.format("Require[%s]須為0或1", args[3]));
                return (1);
			}
            argRequire = args[3];
            
            if ( new CommFunction().checkDateFormat(args[4], "yyyyMMdd"))
                fileDate = args[4];
            else
            if (args[4].toUpperCase(Locale.TAIWAN).equals("BUSINDAY"))
                fileDate = selectPtrBusinday();
            else
            if (args[4].toUpperCase(Locale.TAIWAN).equals("SYSDAY"))
            	fileDate = sysDate;
            else {
            	showInputHint();
            	showLogMessage("E", "", String.format("file_date[%s]須符合指定格式", args[4]));
                return (1);
            }

            showLogMessage("I", "", String.format("[args before getFileName]  run EcsFtp %s %s %s %s %s",
                    argFileName, argRefIpCode, argGroupId, argRequire, fileDate));

            fileName = getFileName(argFileName);

            showLogMessage("I", "", String.format("[args  after getFileName]  run EcsFtp %s %s %s %s %s",
                    fileName, argRefIpCode, argGroupId, argRequire, fileDate));

            showLogMessage("I", "", "ECS FTP程式執行開始");

            boolean isSuccess = ftpMgetFiles(argRefIpCode, argGroupId, fileName, argRequire);

            showLogMessage("I", "", "ECS FTP程式執行結束");
            
            if (isSuccess) {
            	moveToBackupFolder();
			}

            finalProcess();
            return (0);
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }

    } // End of mainProcess
      // ************************************************************************

	public String selectPtrBusinday() throws Exception {
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        // int recordCnt =

        selectTable();

        if (notFound.equals("Y")) {
            showLogMessage("I", "", "select ptr_businday error!");
            setExceptExit(1);
        	throw new Exception("select ptr_businday error!");
        }
        return getValue("BUSINESS_DATE");

    }

    // ************************************************************************
	
	/**
	 * 進行FTP(SFTP, or FTPS) mget
	 * @param refIpCode
	 * @param groupId
	 * @param fileName
	 * @param isRequire
	 * @return true if mget is successful, otherwise, return false 
	 * @throws Exception
	 */
    boolean ftpMgetFiles(String refIpCode, String groupId, String fileName, String isRequire)
            throws Exception {
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = refIpCode; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = groupId; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;

        showLogMessage("I", "", "mget " + fileName + " 開始接收....");
        String command = "mget " + fileName;
        int errCode = commFTP.ftplogName(refIpCode, command);

        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法接收 " + fileName + " 資料");
            insertEcsNotifyLog(fileName);

            if (isRequire.equalsIgnoreCase("1")) {
            	setExceptExit(1);
            	throw new Exception("無法接收 " + fileName + " 資料");
            }
            return false;
        }else {
        	return true;
        }


    }



    // ************************************************************************

    public void moveToBackupFolder() throws Exception {
        selectSQL ="FILE_NAME, REF_IP_CODE, FTP_TYPE as ftpType, ROWID as rowid ";
        daoTable = "ECS_FTP_LOG";
        whereStr = "WHERE trans_seqno = ? AND trans_resp_code = 'Y' ";

        setString(1, commFTP.hEflgTransSeqno);

        openCursor();

        while (fetchTable()) {
            String hEflgFileName = getValue("FILE_NAME");
            String hEflgRefIpCode = getValue("REF_IP_CODE");
            String hEflgRowid = getValue("ROWID");
            String hEflgProcCode = "0";
            String hEflgProcDesc = ""; 
            String ftpType = getValue("ftpType");
            
            if (MOVE_TYPE.equals(ftpType)) {
            	String fs = Paths.get(commFTP.hEriaRemoteDir, hEflgFileName).toString();
                String ft = Paths.get(commFTP.hEriaRemoteDir, "backup", hEflgFileName).toString();
                String cmdStr = String.format("mv -i -f %s %s", fs, ft);

                showLogMessage("I", "", "備份遠端檔案: mv 檔案指令=" + cmdStr);

                if (comc.fileMove(fs, ft) == false) {
                    showLogMessage("I", "", "ERROR : mv 檔案指令=" + cmdStr);
                    hEflgProcCode = "B";
                    hEflgProcDesc = "備份遠端檔案[" + hEflgFileName + "%]失敗";
                    updateEcsFtpLog(hEflgProcCode, hEflgProcDesc, hEflgRowid);
                    continue;
                }
                showLogMessage("I", "", "備份遠端檔案[" + hEflgFileName + "]完成.....");

                hEflgProcDesc = "+備份遠端檔案完成";

                updateEcsFtpLog(hEflgProcCode, hEflgProcDesc, hEflgRowid);
			}

        }
    }

    // ************************************************************************
    public void updateEcsFtpLog(String hEflgProcCode, String hEflgProcDesc,
            String hEflgRowid) throws Exception {
        dateTime();
        updateSQL = "file_date  = ?, "
                + "proc_code  = ?, "
                + "trans_desc = trans_desc||?, "
                + "proc_desc  = ?, "
                + "mod_pgm    = ?, "
                + "mod_time   = timestamp_format(?,'yyyymmddhh24miss')";
        daoTable = "ecs_ftp_log";
        whereStr = "WHERE rowid = ?";

        setString(1, fileDate);
        setString(2, hEflgProcCode);
        setString(3, hEflgProcDesc);
        setString(4, hEflgProcDesc);
        setString(5, javaProgram);
        setString(6, sysDate + sysTime);
        setRowId(7, hEflgRowid);

        updateTable();
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "UPDATE ecs_ftp_log error " + hEflgRowid);
            setExceptExit(1);
        	throw new Exception("UPDATE ecs_ftp_log error " + hEflgRowid);
        }
        return;
    }

    // ************************************************************************
    public int insertEcsNotifyLog(String fileName) throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("unit_code", comr.getObjectOwner("3", javaProgram));
        setValue("obj_type", "3");
        setValue("notify_head", "無法 FTP 接收 " + fileName + " 資料");
        setValue("notify_name", "媒體檔名:" + fileName);
        setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 接收 " + fileName + " 資料");
        setValue("notify_desc2", "");
        setValue("trans_seqno", commFTP.hEflgTransSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ecs_notify_log";

        insertTable();

        return (0);
    }
    // ************************************************************************

    /**
     * 將日期帶入「非固定檔名」中<br>
     * 若有含「*」，則會取前面字元相同之所有檔案,但此符號不可單獨使用<br>
     * 含有「YYYYMMDD」字元會自動取代為西元年月日8碼<br>
     * 含有「YYYMMDD」字元會自動取代為民國年月日7碼<br>
     * 含有「AYYMMDD」字元會自動取代為西元年月日後6碼<br>
     * 含有「BYYMMDD」字元會自動取代為民國年月日後6碼
     * 
     * @param argFileName
     * @return
     * @throws Exception
     */
    private String getFileName(String argFileName) throws Exception {
        String newFileName = "";
        CommDate  commDate = new CommDate();

        if (argFileName.equals("*")) {
            showLogMessage("I", "", "不可單獨使用「*」符號");
            setExceptExit(1);
        	throw new Exception("不可單獨使用「*」符號");
        } else if (argFileName.lastIndexOf("YYYYMMDD") != -1) {
            newFileName = argFileName.replace("YYYYMMDD", fileDate);
        } else if (argFileName.lastIndexOf("YYYMMDD") != -1) {
            newFileName = argFileName.replace("YYYMMDD",  commDate.toTwDate(fileDate));
        } else if (argFileName.lastIndexOf("AYYMMDD") != -1) {
            newFileName = argFileName.replace("AYYMMDD", fileDate.substring(2));
        } else if (argFileName.lastIndexOf("BYYMMDD") != -1) {
            String twDate =  commDate.toTwDate(fileDate);
            newFileName = argFileName.replace("BYYMMDD",
                    twDate.length() == 2 ? twDate : twDate.substring(1));
        } else {
            return argFileName;
        }

        showLogMessage("I", "", "getFileName() : 檔名(file_name)替換成" + newFileName);
        return newFileName;
    }

	// ************************************************************************
	
	private void showInputHint() {
		showLogMessage("I", "", "請輸入參數，均為必填:");
		showLogMessage("I", "", "PARM 1 : [file_name]      必填，檔案名稱，可以為固定檔名，若非固定檔案，如下:");
		showLogMessage("I", "","                           若有含「*」，則會取前面字元相同之所有檔案，但此符號不可單獨使用");
		showLogMessage("I", "", "                          含有「YYYYMMDD」字元會自動取代為西元年月日8碼");
		showLogMessage("I", "", "                          含有「YYYMMDD」字元會自動取代為民國年月日7碼");
		showLogMessage("I", "", "                          含有「AYYMMDD」字元會自動取代為西元年月日後6碼");
		showLogMessage("I", "", "                          含有「BYYMMDD」字元會自動取代為民國年月日後6碼");
		showLogMessage("I", "", "PARM 2 : [ref_ip_code]    必填，KEY值，必須與system_id相同");
		showLogMessage("I", "", "PARM 3 : [group_id]       必填，細項分類，如無使用請預設000000");
		showLogMessage("I", "", "PARM 4 : [Require]        必填，是否為必要檔案， 1:True(無檔案會當掉) 0:False(無檔案正常結束)");
		showLogMessage("I", "", "PARM 5 : [file_date]        必填,可指定日期、指定使用營業日、指定使用系統日");
		showLogMessage("I", "", "                          可輸入指定日期YYYYMMDD，如20200715");
		showLogMessage("I", "", "                          輸入「BUSINDAY」表示抓取營業日");
		showLogMessage("I", "", "                          輸入「SYSDAY」表示抓取系統日");
	}

} // End of class FetchSample

