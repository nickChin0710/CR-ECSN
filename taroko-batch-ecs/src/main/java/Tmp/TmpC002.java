/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
 *  111/04/26  V1.00.00    Ryan      program initial                                  *
 **************************************************************************************/

package Tmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;

import com.CommDate;
import com.CommFTP;

public class TmpC002 extends AccessDAO {
	private final String progname = "F code理由碼補救程式  111/04/26 V1.00.00";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	protected com.CommString zzstr = new com.CommString();
	String modUser = "";

	String fileNameTxt = "RESET_BLOCK_";
	String fileNameTxt2 = "RESET_BLOCK_";
	ArrayList<String> dataList = new ArrayList<String>();
	HashMap<Integer,String> dataListOk = new HashMap<Integer,String>();
	private int totalCnt = 0;
	private int cardNotFoundCnt = 0;
	private int blockCodeSkipCnt = 0;
	private int currentCodeSkipCnt = 0;
	private int orgSkipCnt = 0;
	private int reasonSkipCnt = 0;

	private boolean ibDebit = false;
	private String lineLength = "";
	private String fileCardNo = "";
	private String fileCode = "";
	private String fileReasonCode = "";
	private String hOppostReason = "";
	private String hSysDate = "";
	private String cardCurrentCode = "";
	private String fileOrg = "";
	private int isUpdate = 0;
	private String hCardOppostReason = "";

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			commitDataBase();
			
			selectPtrBusinday();
			if(args.length == 1) {
				hSysDate = args[0];
			}

			if(readFile() == 1) {
				renameFile();
				outPutFile();
				procFTP();
				renameFile2();

				showLogMessage("I", "","");
				showLogMessage("I", "",String.format("BLOCK CODE <> (A,F,L,S,O,B,E,X) SKIP CNT = [%s]", blockCodeSkipCnt));
				showLogMessage("I", "",String.format("ORG <> (106,206,306) SKIP CNT = [%s]", orgSkipCnt));
				showLogMessage("I", "",String.format("CAR_CARD NOT FOUND CNT = [%s]", cardNotFoundCnt));
				showLogMessage("I", "",String.format("CURRENT_CODE <> 5 , SKIP CNT = [%s]", currentCodeSkipCnt));
				showLogMessage("I", "",String.format("OLD OPPOST_REASON = NEW OPPOST_REASON , SKIP CNT = [%s]", reasonSkipCnt));
				showLogMessage("I", "",String.format("TOTAL PROCESSED CNT = [%s]", totalCnt));
			}

			commitDataBase();
			
			// ==============================================
			// 固定要做的
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
	public void selectPtrBusinday() throws Exception {
		sqlCmd = "select business_date from ptr_businday";
		int recordCnt = selectTable();
		hSysDate = getValue("business_date");
	}

	/***********************************************************************/
	int readFile() throws Exception {
		System.out.println("====Start Read File ====");
		BufferedReader br = null;
		try {
			String hSysDate1 = String.format("%s.TXT", Integer.parseInt(hSysDate) - 19110000);
			String hSysDate2 = String.format("%s_UPDATE.TXT", Integer.parseInt(hSysDate) - 19110000);
			fileNameTxt = fileNameTxt + hSysDate1;
			fileNameTxt2 = fileNameTxt2 + hSysDate2;
			String tmpStr = String.format("%s/media/crd/%s", comc.getECSHOME(), fileNameTxt);
			String tempPath = SecurityUtil.verifyPath(tmpStr);
			FileInputStream fis = new FileInputStream(new File(tempPath));
			br = new BufferedReader(new InputStreamReader(fis, "MS950"));

			System.out.println("  tempPath = [" + tempPath + "]");

		} catch (FileNotFoundException exception) {
			System.out.println("bufferedReader exception: " + exception.getMessage());
			return -1;
		}

		while ((lineLength = br.readLine()) != null) {
			isUpdate++;
			dataListOk.put(isUpdate-1, "N,");
			initData();
			if(getFileData() == -1) {
				continue;
			}
			if(selectCardData(fileCardNo) != 1) {
				cardNotFoundCnt ++;
				continue;
			}
			if(!cardCurrentCode.equals("5")) {
				currentCodeSkipCnt ++;
				showLogMessage("I", "", String.format("current_code <> 5 ,card_no = [%s]", fileCardNo));
				continue;
			}
			if(hCardOppostReason.equals(hOppostReason)) {
				reasonSkipCnt ++;
				continue;
			}
			updateCcaOpposition(fileCardNo);
			if(updateCrdCard(fileCardNo)!=1)
				continue;

			totalCnt++;
			commitDataBase();
			dataListOk.put(isUpdate-1, "Y,");
		}
		br.close();
		return 1;
	}

	/*************************************************************************/
	int getFileData() throws Exception {
		String wordsStr = new String(lineLength.getBytes("MS950"), "MS950");
		String[] wordsArray = wordsStr.split(",");
		dataList.add(wordsStr);
		
		if(wordsArray.length<6) {
			return 0;
		}
		for (int i = 0; i < wordsArray.length; i++) {

			if (StringUtils.isNotBlank(wordsArray[i]) && wordsArray[i].toUpperCase(Locale.TAIWAN).equals("NULL")) {
				wordsArray[i] = "";
			}

		}
		fileOrg = wordsArray[0].trim();
		fileCardNo = wordsArray[3].trim();
		fileCode = wordsArray[4].trim();
		fileReasonCode = wordsArray[5].trim();

		if(zzstr.pos(",106,206,306", fileOrg)<=0) {
			orgSkipCnt++;
			showLogMessage("I", "", String.format("ORG <> (106,206,306) ,card_no = [%s]", fileCardNo));
			return -1;
		}

		if(zzstr.empty(fileCode)) {
			return -1;
		}
		if(!fileCode.equals("F")) {
			blockCodeSkipCnt ++;
			showLogMessage("I", "", String.format("block code <> F ,card_no = [%s]", fileCardNo));
			return -1;
		}

		if (fileCode.equals("F")) {
			switch(fileReasonCode) {
			case "FM":
				hOppostReason = "M1";
				break;
			case "FN":
				hOppostReason = "N1";
				break;
			case "FO":
				hOppostReason = "AK";
				break;
			default:
				hOppostReason = "M2";
				break;
			}
		}
		return 1;
	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	public boolean isDebitcard(String cardNo) throws Exception {
		String lsCardNo = cardNo;
		if (lsCardNo.length() < 6)
			return false;

		sqlCmd = "select count(*) as xx_cnt" + " from ptr_bintable"
				+ " where ? between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')"
				+ " and debit_flag ='Y'";
		setString(1, cardNo);
		int recordCnt = selectTable();
		if (recordCnt <= 0)
			return false;

		if (getValueDouble("xx_cnt") > 0)
			return true;

		return false;
	}

	/*************************************************************************/
	int selectCardData(String cardNo) throws Exception {
		ibDebit = isDebitcard(fileCardNo);
		if (ibDebit) {
			sqlCmd = "select C.current_code ,C.oppost_reason "
					+ " from dbc_idno A, dba_acno B, dbc_card C " 
					+ " where card_no = ? "
					+ " and A.id_p_seqno = C.id_p_seqno and B.p_seqno = C.p_seqno ";
		} else {
			sqlCmd = "select C.current_code ,C.oppost_reason "
					+ " from crd_idno A, act_acno B, crd_card C "
					+ " where card_no = ? " 
					+ " and A.id_p_seqno = C.id_p_seqno and B.acno_p_seqno = C.acno_p_seqno ";
		}

		setString(1, cardNo);
		int recordCnt = selectTable();

		if (recordCnt <= 0) {
			showLogMessage("I", "", String.format("select CRD[DBC]_CARD not found,card_no = [%s]", cardNo));
			return -1;
		}
		hCardOppostReason = getValue("oppost_reason");
		cardCurrentCode = getValue("current_code");
		return 1;
	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	int updateCrdCard(String aCardNo) throws Exception {
		if (ibDebit == false) {
			daoTable = "crd_card";
		} else {
			daoTable = "dbc_card";
		}

		updateSQL = " oppost_reason = ? , mod_pgm = 'TmpC002' , mod_time = sysdate ";
		whereStr = " where card_no = ? ";
		setString(1, hOppostReason);
		setString(2, aCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CRD[DBC]_CARD not found,card_no = [%s]", aCardNo));
			return -1;
		}
		return 1;
	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	void updateCcaOpposition(String aCardNo) throws Exception {
		daoTable = "cca_opposition";
		updateSQL += "oppo_status = ? ,chg_time = to_char(sysdate,'hh24miss') ,mod_pgm = 'TmpC002' ";
		whereStr = "where card_no = ? ";
		setString(1, hOppostReason);
		setString(2, aCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update cca_opposition error,card_no = [%s]", aCardNo));
		}
	}


	void outPutFile() throws Exception {
		int outPutFile = openOutputText(comc.getECSHOME() + "/media/crd/" + fileNameTxt2, "MS950");
		if(outPutFile<0) {
			comcr.errRtn("更新"+fileNameTxt2+"檔案失敗", "", "");
			return;
		}
		for(int i = 0 ; i < dataList.size() ; i++) {
			writeTextFile(outPutFile, dataListOk.get(i).toString() + dataList.get(i).toString() + "\r\n");
		}
		
		closeOutputText(outPutFile);
	}
	
	/***********************************************************************/
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileNameTxt2 + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fileNameTxt2);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileNameTxt2 + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileNameTxt2);
		}
	}
	
	/************************************************************************/
	public void renameFile() throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + fileNameTxt;
		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + fileNameTxt;

		
		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameTxt + "]更名失敗!" + tmpstr2);
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameTxt + "] 已移至 [" + tmpstr2 + "]");

	}
	
	/************************************************************************/
	public void renameFile2() throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + fileNameTxt2;
		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + fileNameTxt2;

		
		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameTxt2 + "]更名失敗!" + tmpstr2);
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameTxt2 + "] 已移至 [" + tmpstr2 + "]");

	}
	
	/***********************************************************************/
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
	
	
	/***********************************************************************/
	public void initData() {
		
		fileCardNo = "";
		fileCode = "";
		hOppostReason = "";
		hSysDate = "";
		cardCurrentCode = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TmpC002 proc = new TmpC002();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
