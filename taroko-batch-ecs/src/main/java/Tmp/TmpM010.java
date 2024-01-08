package Tmp;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112/02/02  V0.00.01     JeffKung  initial                                  *
*****************************************************************************/

import com.CommCrd;
import com.CommCrdRoutine;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.AccessDAO;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class TmpM010 extends AccessDAO {
	private String PROGNAME = "處理年費優惠名單匯入檔案 112/02/02 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate zzdate = new CommDate();
	private int iiFileNum = 0;

	String modUser = "";

	String hBusiBusinessDate = "";

	public void mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();
			
			//清除cardFeeDate
			if (args.length == 1 && "CARDFEEDATE".equals(args[0])) {
				processCrdCard();
				commitDataBase();
			}

			processSuperId("superid");
			commitDataBase();
			
			processNoFeeId("nofeeid");

			finalProcess();
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return;
		}

	}

	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += " fetch first 1 rows only ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
		}

	}
	
	/***********************************************************************/
	void processCrdCard() throws Exception {

		String cardFeeDate = "";
		String cardNo = "";
		String rowId = "";
		int totalCnt = 0;

		sqlCmd = " select ";
		sqlCmd += " card_fee_date,card_no, ";
		sqlCmd += " rowid as rowid ";
		sqlCmd += " from crd_card ";
		sqlCmd += " where 1=1 ";

		openCursor();
		while (fetchTable()) {
			cardFeeDate = getValue("card_fee_date");
			cardNo = getValue("card_no");
			rowId = getValue("rowid");

			totalCnt++;

				updateCrdCard(cardNo, cardFeeDate, rowId);
				
				if (totalCnt % 10000 == 0) {
					showLogMessage("I", "", "Process Count :  " + totalCnt);
					countCommit();
				}

		}

		closeCursor();
		showLogMessage("I", "", "清除crdFeeDate [" + totalCnt + "] 筆");

	}

	void updateCrdCard(String cardNo, String cardFeeDate, String rowId) throws Exception {

		daoTable = "crd_card";
		updateSQL = "card_fee_date = '' ";
		whereStr = "where rowid = ? ";
		setRowId(1, rowId);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("E", "", String.format("crd_card異動失敗,card_no=[%s]", cardNo));
		}

	}


	//=============================================================================
	void processSuperId(String fileName) throws Exception {

		String readData = "";
		String tmpstr = "";
		int totalCnt = 0;

		String lsFile = String.format("%s/media/mkt/%s", comc.getECSHOME(), fileName);
		showLogMessage("I", "", "file path = [" + lsFile + "]");
		iiFileNum = openInputText(lsFile);
		if (iiFileNum == -1) {
			showLogMessage("I", "", String.format("無檔案可處理 [%s]", fileName));
			return;
		}

		while (true) {
			readData = readTextFile(iiFileNum).trim();

			if (endFile[iiFileNum].equals("Y"))
				break;
			if (readData.length() < 10) {
				showLogMessage("I", "", String.format("資料長度<10有誤:[%s]", readData));
				continue;
			}

			totalCnt++;

			updateCrdIdno(readData);
			
			if (totalCnt % 5000 == 0) {
				showLogMessage("I", "", "Process Count :  " + totalCnt);
				countCommit();
			}
			
		}

		closeInputText(iiFileNum);
		showLogMessage("I", "", "檔案轉入 [" + totalCnt + "] 筆");
		renameFile(fileName);
	}
	
	void updateCrdIdno(String idNo) throws Exception {

		daoTable = "crd_idno";
		updateSQL = "fee_code_i = 'Y' ";
		whereStr = "where id_no = ? ";
		setString(1, idNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("E", "", String.format("crd_idno異動失敗,id_no=[%s]", idNo));
		}

	}

	//=============================================================================
		void processNoFeeId(String fileName) throws Exception {

			String readData = "";
			String tmpstr = "";
			int totalCnt = 0;

			String lsFile = String.format("%s/media/mkt/%s", comc.getECSHOME(), fileName);
			showLogMessage("I", "", "file path = [" + lsFile + "]");
			iiFileNum = openInputText(lsFile);
			if (iiFileNum == -1) {
				showLogMessage("I", "", String.format("無檔案可處理 [%s]", fileName));
				return;
			}

			while (true) {
				readData = readTextFile(iiFileNum).trim();

				if (endFile[iiFileNum].equals("Y"))
					break;
				if (readData.length() < 10) {
					showLogMessage("I", "", String.format("資料長度<10有誤:[%s]", readData));
					continue;
				}

				totalCnt++;

				updateCrdCardNoFeeId(readData);
				
			}

			closeInputText(iiFileNum);
			showLogMessage("I", "", "檔案轉入 [" + totalCnt + "] 筆");
			renameFile(fileName);
		}
		
		void updateCrdCardNoFeeId(String idNo) throws Exception {
			
			String idPSeqno = "";
			
			sqlCmd = " select ";
			sqlCmd += " id_p_seqno ";
			sqlCmd += " from crd_idno ";
			sqlCmd += " where id_no= ? ";

			setString(1,idNo);
			
			selectTable();
			
			if (notFound.equals("Y")) {
				showLogMessage("E", "", String.format("crd_idno無此ID,id_no=[%s]", idNo));
				return;
			}

			idPSeqno = getValue("id_p_seqno");

			daoTable = "crd_card";
			updateSQL = "card_fee_date = '299912' ";
			whereStr = "where id_p_seqno = ? ";
			setString(1, idPSeqno);
			updateTable();
			if (notFound.equals("Y")) {
				showLogMessage("E", "", String.format("crd_card異動失敗,id_no=[%s]", getValue("id_no")));
			}

		}
    //=============================================================================
	void renameFile(String fileName) throws Exception {
		String tmpstr1 = String.format("%s/media/mkt/%s", comc.getECSHOME(), fileName);
		String tmpstr2 = String.format("%s/media/mkt/backup/%s.%-8.8s", comc.getECSHOME(), fileName, sysDate);

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("E", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) throws Exception {
		TmpM010 proc = new TmpM010();
		proc.mainProcess(args);
		return;
	}
	// ************************************************************************

}
