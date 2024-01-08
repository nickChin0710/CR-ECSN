/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  110-08-16  V1.00.00    jiangyingdong     			create                 *
 *  112/03/04  V1.00.01    Zuwei Su              錯誤:每筆總長是150 Byte    *
 ******************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

import java.io.File;

import com.BaseBatch;
import com.CommString;
import com.*;

public class InfC050 extends BaseBatch {
	private final String progname = "產生送CRDB 50異動繳款註記資料檔程式  112/03/04  V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	String isFileName = "";
	private int ilFile50;
  CommDate  commDate = new CommDate();
	String hSysDate = "";
	String hIdNo = "";
	String hCardNo = "";
	String hMark = "";

	public static void main(String[] args) {
		InfC050 proc = new InfC050();
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfCrb50 [business_date]");
			errExit(1);
		}
		dbConnect();
		if (liArg == 1) {
			hSysDate = args[0];
		}	

		if (empty(hSysDate)) {
			hSysDate =  hBusiDate;
		}

    hSysDate = commDate.dateAdd(hSysDate, 0, 0, -1) ;
		isFileName = "CRU23B1_TYPE_50_" + hSysDate + ".txt";
		
				
		checkOpen();
		selectIdNo(hSysDate);
		closeOutputText(ilFile50);
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile();
		endProgram();
	}


	void selectIdNo(String hSysDate) throws Exception {
		sqlCmd = "SELECT b.card_no AS card_nmbr, c.id_no AS id_nmbr, CASE WHEN a.min_pay_bal>0 THEN '1' ELSE '' END AS crrplg "
				+ "FROM act_acct a "
				+ "LEFT JOIN crd_card b ON a.p_seqno=b.acno_p_seqno "
				+ "LEFT JOIN crd_idno c ON a.id_p_seqno=c.id_p_seqno "
				+ "WHERE min_pay > 0 "
				+ "ORDER BY a.acct_type, a.stmt_cycle, a.p_seqno";		
		
		int llCnt = selectTable();
		for (int ii = 0; ii < llCnt; ii++) {
			hCardNo = colSs(ii, "card_nmbr");
			hIdNo = colSs(ii, "id_nmbr");
			hMark = colSs(ii, "crrplg");
			writeTextFile("");
		}
	}


	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		File file = checkFileExistence(lsTemp); // 目錄不存在則創建
		ilFile50 = openOutputText(lsTemp, "big5");
		if (ilFile50 < 0) {
			printf("CRU23B1-TYPE-50 產檔失敗 ! ");
			errExit(1);
		}
		
		// TXT文件加上頭部標題
//		if (file.length() == 0) {            
//            writeTextFile("new");
//		}
	}
	
	void writeTextFile(String mode) throws Exception {		
		StringBuffer tempBuf = new StringBuffer();
		String tempStr = "", newLine = "\r\n";
//		if (mode.equalsIgnoreCase("new")) {
//			tempBuf.append(comc.fixLeft("CODE", 6)); 
//			tempBuf.append(comc.fixLeft("CARD-NMBR", 22)); 
//			tempBuf.append(comc.fixLeft("ID-NMBR", 33)); 
//			tempBuf.append(comc.fixLeft("CRRPLG", 34));
//			tempBuf.append(comc.fixLeft("FILLER", 154));
//		} else {
			tempBuf.append(comc.fixLeft("50", 2)); // --代碼 固定 50
			tempBuf.append(comc.fixLeft(hCardNo, 16)); 
			tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
			tempBuf.append(comc.fixLeft(hMark, 1));
			tempBuf.append(comc.fixLeft("", 120));
			totalCnt++;
//		}
			tempBuf.append(newLine);
		this.writeTextFile(ilFile50, tempBuf.toString());
	}

	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
		}
	}

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

	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/crdb/%s", getEcsHome(), isFileName);
		String tmpstr2 = String.format("%s/media/crdb/backup/%s", getEcsHome(), isFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + isFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
	 /**
     * 检查文件的存在性,不存在则创建相应的路由，文件
     * @param filepath
     * @return 创建失败则返回null
     */
	public File checkFileExistence(String filepath){        
        File file = new File(filepath);
        try {
            if (!file.exists()){
                if (filepath.charAt(filepath.length()-1) == '/' || filepath.charAt(filepath.length()-1) == '\\') {
					file.mkdirs();
                } else {
                	String[] split = filepath.split("[^/\\\\]+$");
                    checkFileExistence(split[0]);
                    file.createNewFile();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

}
