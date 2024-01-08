/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/02/27  V1.00.00    Pino     program initial                           *
*  109/11/13  V1.00.02  yanghan       修改了變量名稱和方法名稱                                                                                *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  111/06/16  V1.00.04    Justin    弱點修正                                  *
*  112/02/08  V1.00.05   Wilson     檔案產生路徑調整                                                                                     *
*  112/03/07  V1.00.06   Wilson     新增procFTP                                *
*  112/03/07  V1.00.07   Wilson     檔案的結束符號改成0D0A                          *
*  112/06/30  V1.00.08   Wilson     檔案格式增加hOnlineOpenflag                   *
*  112/07/03  V1.00.09   Wilson     假日不執行                                                                                                 *
*  112/10/20  V1.00.10   Wilson    增加卡片組織別、卡片TYP欄位                                                                 *
******************************************************************************/

package Dbc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.util.Locale;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*重製/續卡取三軌處理程式*/
public class DbcC005 extends AccessDAO {
	private String progname = "產生VD重製清單檔程式  112/10/20 V1.00.10";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommRoutine    comr  = null;
	CommCrdRoutine comcr = null;
	CommFTP commFTP = null;

	int debug = 1;

	String prgmId = "DbcC005";
	String hCallBatchSeqno = "";
	String hBusinessDate = "";
	String hSystemDate = "";
	String hDcdtCardNo = "";
	String hDcdtBatchno = "";
	String hDcdtSavingActno = "";
	String hDcdtRowid = "";
	String hEmbossReason = "";
	String hRegBankNo = "";
	String hCardRefNum = "";
	String hOldCardNo = "";
	String hElectronicCode = "";
	String hMailBranch = "";
	String hCrtBankNo = "";
	String hVdBankNo = "";
	String hVmjType = "";
	String hCardType = "";
	String hOnlineOpenflag = "";
	String swAsc = "";
	String hCallErrorDesc = "";
	String filename = "";
	String temstr = "";
	String stderr = "";
	int recCnt = 0;
	int hNn = 0;
	int tmpInt = 0;

	BufferedWriter fptr1 = null;

	public int mainProcess(String[] args) throws IOException {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 2) {
				comc.errExit("Usage : DbcC005 Y/N  batch_seq", "Usage : Y-->IBM 碼  , N-->非 IBM 碼 ");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			if (args.length == 0) {
				swAsc = "N"; // default N 非IBM碼
			} else {
				swAsc = args[0];
			}
			if (!swAsc.toUpperCase(Locale.TAIWAN).equals("Y") && !swAsc.toUpperCase(Locale.TAIWAN).equals("N")) {
				comcr.hCallErrorDesc = " Usage : DbcC005  Y/N !! ";
				comcr.errRtn(hCallErrorDesc, "Usage : Y-->IBM 碼  , N-->非 IBM 碼 ", hCallBatchSeqno);
			}

			if (hCallBatchSeqno.length() == 20)
				comcr.hCallBatchSeqno = hCallBatchSeqno;

			comcr.hCallRProgramCode = javaProgram;

			comcr.callbatch(0, 0, 0);

			commonRtn();
			
			showLogMessage("I", "", String.format("今日營業日 = [%s]", hBusinessDate));
			
            if (checkPtrHoliday() != 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }

			checkOpen();
			selectDbcDebit();
			insertFileCtl();
			
    		commFTP = new CommFTP(getDBconnect(), getDBalias());
    	    comr = new CommRoutine(getDBconnect(), getDBalias());
    	    procFTP();
    	    renameFile1(filename);
			
			showLogMessage("I", "", String.format("\n程式執行結束 筆數 = [%d]\n", recCnt));

			commitDataBase();

			// ==============================================
			// 固定要做的
			if (fptr1 != null) {
				fptr1.close();
				fptr1 = null;
			}
			if (hCallBatchSeqno.length() == 20)
				comcr.callbatch(1, 0, 0);
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			if (fptr1 != null) {
				fptr1.close();
				fptr1 = null;
			}
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date,to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from ptr_businday ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
        }
    }

    /***********************************************************************/
    int checkPtrHoliday() throws Exception {
        int hCount = 0;

        sqlCmd = "select count(*) h_count ";
        sqlCmd += " from ptr_holiday  ";
        sqlCmd += "where holiday = ? ";
        setString(1, hBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_holiday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCount = getValueInt("h_count");
        }

        if (hCount > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /***********************************************************************/
	void checkOpen() throws Exception {
		checkFileCtl();
		filename = String.format("rqst_vd_s_%8s%02d.txt", hSystemDate, hNn);
		temstr = String.format("%s/media/dbc/%s", comc.getECSHOME(), filename);
		showLogMessage("I", "", "  Open file=[" + temstr + "]");
		temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
		try {
			comc.mkdirsFromFilenameWithPath(temstr);
			fptr1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temstr), "MS950"));
		} catch (Exception ex) {
			comcr.errRtn(String.format("開啟檔案[%s]失敗[%s]", temstr, ex.getMessage()), "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void checkFileCtl() throws Exception {
		String likeFilename = "";
		String hFileName = "";
		likeFilename = String.format("rqst_vd_s_%8s", hSystemDate) + "%";
		sqlCmd = "select file_name ";
		sqlCmd += " from crd_file_ctl  ";
		sqlCmd += "where file_name like ?  ";
		sqlCmd += " and crt_date  = to_char(sysdate,'yyyymmdd') ";
		sqlCmd += " order by file_name desc  ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, likeFilename);
		int tmpInt = selectTable();
		if (notFound.equals("Y")) {
			hNn++;
		} else {
			hFileName = getValue("file_name");
			hNn = Integer.valueOf(hFileName.substring(18, 20)) + 1;
		}

	}

	/***********************************************************************/
	void insertFileCtl() throws Exception {
		daoTable = "crd_file_ctl";
		setValue("file_name", filename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", recCnt);
		setValueInt("record_cnt", recCnt);
		setValue("trans_in_date", sysDate);
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt  = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name  = ? ";
			setInt(1, recCnt);
			setInt(2, recCnt);
			setString(3, filename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/***********************************************************************/
	void selectDbcDebit() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "a.card_no,";
		sqlCmd += "a.batchno,";
		sqlCmd += "a.saving_actno,";
		sqlCmd += "a.rowid as rowid, ";
		sqlCmd += "b.emboss_reason, ";
		sqlCmd += "b.reg_bank_no, ";
		sqlCmd += "b.card_ref_num, ";
		sqlCmd += "b.old_card_no, ";
		sqlCmd += "b.electronic_code, ";
		sqlCmd += "b.mail_branch, ";
		sqlCmd += "b.crt_bank_no, ";
		sqlCmd += "b.vd_bank_no ";
		sqlCmd += " from dbc_debit a , dbc_emboss b ";
		sqlCmd += "where a.to_ibm_date = '' ";
		sqlCmd += "and b.emboss_source = '5' ";
		sqlCmd += "and a.batchno = b.batchno ";
		sqlCmd += "and a.card_no = b.card_no ";
		sqlCmd += "order by a.card_no ";
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hDcdtCardNo = getValue("card_no", i);
			hDcdtBatchno = getValue("batchno", i);
			hDcdtSavingActno = getValue("saving_actno", i);
			hDcdtRowid = getValue("rowid", i);
			hEmbossReason = getValue("emboss_reason", i);
			hRegBankNo = getValue("reg_bank_no", i);
			hCardRefNum = getValue("card_ref_num", i);
			hOldCardNo = getValue("old_card_no", i);
			hElectronicCode = getValue("electronic_code", i);
			hMailBranch = getValue("mail_branch", i);
			hCrtBankNo = getValue("crt_bank_no", i);
			hVdBankNo = getValue("vd_bank_no", i);
			recCnt++;
			if (recCnt == 1 || recCnt % 1000 == 0)
				showLogMessage("I", "", String.format(" Currnt  process count = [%d]", recCnt));

			writeRtn();
			updateDbcDebit();
		}
		if (fptr1 != null) {
			fptr1.close();
			fptr1 = null;
		}
		return;
	}

	/***********************************************************************/
	void updateDbcDebit() throws Exception {
		daoTable = "dbc_debit";
		updateSQL = " to_ibm_date = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += " mod_pgm     = ?,";
		updateSQL += " mod_user    = ?,";
		updateSQL += " mod_time    = sysdate";
		whereStr = "where rowid  = ? ";
		setString(1, prgmId);
		setString(2, comc.commGetUserID());
		setRowId(3, hDcdtRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_debit not found!", "", hCallBatchSeqno);
		}

		daoTable = "dbc_emboss";
		updateSQL = " to_ibm_date   = to_char(sysdate,'yyyymmdd'),";
		updateSQL += " mod_pgm       = ?,";
		updateSQL += " mod_time      = sysdate";
		whereStr = "where card_no  = ? ";
		setString(1, prgmId);
		setString(2, hDcdtCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_emboss not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void writeRtn() throws Exception {
		String temp1 = "";
		String str300 = "";

		temp1 = String.format("%-13.13s", hDcdtSavingActno);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-2.2s", hCardRefNum);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-16.16s", hOldCardNo);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-16.16s", hDcdtCardNo);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", "V");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", " ");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		if (hElectronicCode.equals("01")) {
			temp1 = String.format("%-1.1s", "Y");
		} else
			temp1 = String.format("%-1.1s", "N");
		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		if (hEmbossReason.equals("1")) {
			temp1 = String.format("%-1.1s", "2");
		} else if (hEmbossReason.equals("2")) {
			temp1 = String.format("%-1.1s", "3");
		} else if (hEmbossReason.equals("3")) {
			temp1 = String.format("%-1.1s", "5");
		}
		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		if (hElectronicCode.equals("01")) {
			temp1 = String.format("%-1.1s", "R");
		} else
			temp1 = String.format("%-1.1s", " ");
		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-4.4s", hRegBankNo);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-4.4s", hMailBranch);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-4.4s", hCrtBankNo);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-4.4s", hVdBankNo);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;
		
		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;
		
		hVmjType = "";
				
		temp1 = String.format("%-1.1s", hVmjType);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;
		
		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;
		
		hCardType = "";
		
		temp1 = String.format("%-3.3s", hCardType);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;
		
		temp1 = String.format("%-1.1s", ";");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;
		
		hOnlineOpenflag = selectCcaOpposition();
		temp1 = String.format("%-1.1s", hOnlineOpenflag);

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

		temp1 = String.format("%-12.12s", ";;;;;;;;;;;;");

		if (swAsc.toUpperCase(Locale.TAIWAN).equals("Y")) {
			temp1 = new String(temp1.getBytes("Cp1047"), "Cp1047");
		}

		str300 += temp1;

//if(DEBUG==1) showLogMessage("I", "", "Str300="+"["+str300+"]"+str300.length());

		fptr1.write(String.format("%-100.100s", str300) + "\r\n");

		return;
	}
	/***********************************************************************/
    String selectCcaOpposition()throws Exception {
    	int count = 0;

    	if(hEmbossReason.equals("1")) {
        	sqlCmd  = "select count(*) cnt ";
        	sqlCmd += "from cca_opposition ";
            sqlCmd += "where mod_pgm = 'Etb0001' ";
            sqlCmd += "and oppo_type = '2' ";
            sqlCmd += "and card_no = ? ";
            setString(1, hOldCardNo);
            selectTable();
            count = getValueInt("cnt");
            
            if(count > 0) {
            	return "Y";
            }
            else {
            	return "N";
            }
    	}
    	else {
    		return "N";
    	}
    }
    
    /***********************************************************************/
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	    commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	    commFTP.hEriaLocalDir = String.format("%s/media/dbc", comc.getECSHOME());
	    commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	    commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	    commFTP.hEflgModPgm = javaProgram;
	    

	    // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	    showLogMessage("I", "", "mput " + filename + " 開始傳送....");
	    int errCode = commFTP.ftplogName("NCR2TCB", "mput " + filename);
	    
	    if (errCode != 0) {
	        showLogMessage("I", "", "ERROR:無法傳送 " + filename + " 資料"+" errcode:"+errCode);
	        insertEcsNotifyLog(filename);          
	    }
	}

	/****************************************************************************/
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
	/****************************************************************************/
		void renameFile1(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/dbc/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/dbc/backup/" + removeFileName;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcC005 proc = new DbcC005();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
