package Bil;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 111-05-25  V0.00.01     Ryan     initial                                   *
*****************************************************************************/

import com.CommCrd;
import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;


public class BilN010 extends BaseBatch {
	private String PROGNAME = "處理收單系統匯入O953檔案 111/05/25 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate zzdate = new CommDate();
	private String[] isFileNames = {"O953006_MASTER.txt","O953006_VISA.txt"};
	private int iiFileNum = 0;

	String modUser = "";
	double hBlfrFraudAmt = 0;
	String hBlfrFunctionCode = "";
	String hBlfrBinType = "";
	String hBlfrRealCardNo = "";
	String hBlfrFilmNo = "";
	String hBlfrPurchaseDate = "";
	String hBlfrFraudType = "";
	String hBlfrMerchantCategory = "";
	String hBlfrMerchantEngName = "";
	String hBlfrMerchantCity = "";
	String hBlfrMerchantZip = "";
	String hBlfrPosEntryMode = "";

//=**************************************************************************** 
	public static void main(String[] args) {
		BilN010 proc = new BilN010();
		proc.mainProcess(args);
		proc.systemExit();
	}

//=============================================================================
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(PROGNAME);
		dbConnect();
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		modUser = comc.commGetUserID();

		openFile();
		
		showLogMessage("I", "","");
		
		commitDataBase();
		endProgram();
	}

//=============================================================================
	void openFile() throws Exception {

		for (String file : isFileNames) {
			readFile(file);
		}
		
	}

//=============================================================================
	void readFile(String fileName) throws Exception {
		int dataCnt1 = 0,dataCnt2 = 0;
		System.out.println("");
		System.out.println("====Start Read File ====");
		String lsFile = String.format("%s/media/bil/%s", getEcsHome(), fileName);
		System.out.println("  path = [" + lsFile + "]");
		iiFileNum = openInputText(lsFile);
		if (iiFileNum == -1) {
			showLogMessage("I", "", String.format("無檔案可處理 [%s]",fileName));
			return;
		}
		while (true) {
			String fileData = readTextFile(iiFileNum);
			if (endFile[iiFileNum].equals("Y")) {
				break;
			}
			if (empty(fileData))
				break;
	
			byte[] bytes = fileData.getBytes("MS950");
			
			initData();
			dataCnt1++;
			
			hBlfrFunctionCode = comc.subMS950String(bytes, 0, 1).trim();
			if(zzstr.pos(",A,C,D", hBlfrFunctionCode)<1) {
				continue;
			}
			hBlfrBinType = comc.subMS950String(bytes, 1, 1).trim();
			hBlfrRealCardNo = comc.subMS950String(bytes, 2, 19).trim();
			hBlfrFilmNo = comc.subMS950String(bytes, 21, 23).trim();
			hBlfrPurchaseDate = comc.subMS950String(bytes, 44, 8).trim();
			hBlfrFraudType = comc.subMS950String(bytes, 52, 2).trim();
			hBlfrFraudAmt = comc.str2double(comc.subMS950String(bytes, 54, 12).trim());
			hBlfrMerchantCategory = comc.subMS950String(bytes, 66, 4).trim();
			hBlfrMerchantEngName = comc.subMS950String(bytes, 70, 25).trim();
			hBlfrMerchantCity = comc.subMS950String(bytes, 95, 13).trim();
			hBlfrMerchantZip = comc.subMS950String(bytes, 108, 5).trim();
			hBlfrPosEntryMode = comc.subMS950String(bytes, 113, 3).trim();

			insertBilFraudReport();
			totalCnt++;
			dataCnt2++;
		}
		commitDataBase();
		closeInputText(iiFileNum);
		showLogMessage("I", "",String.format("資料筆數=%s ,處理筆數=%s",dataCnt1,dataCnt2));
		renameFile(fileName);
	}

//=============================================================================

	void insertBilFraudReport() throws Exception {
		setValue("function_code", hBlfrFunctionCode);
		setValue("bin_type", hBlfrBinType);
		setValue("card_no", hBlfrRealCardNo);
		setValue("film_no", hBlfrFilmNo);
		setValue("purchase_date", hBlfrPurchaseDate);
		setValue("fraud_type", hBlfrFraudType);
		setValueDouble("fraud_amt", hBlfrFraudAmt);
		setValue("mcht_category", hBlfrMerchantCategory);
		setValue("mcht_eng_name", hBlfrMerchantEngName);
		setValue("mcht_city", hBlfrMerchantCity);
		setValue("mcht_zip", hBlfrMerchantZip);
		setValue("pos_entry_mode", hBlfrPosEntryMode);
		setValue("crt_date", sysDate);
		setValue("apr_flag", "N");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", modUser);
		setValue("mod_pgm", "BilN010");
		setValueInt("mod_seqno", 1);
		
		daoTable = "bil_fraud_report";

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("insert bil_fraud_report error,card_no = [%s]", hBlfrRealCardNo));
		}
	}

//=============================================================================
	void renameFile(String fileName) throws Exception {
		String tmpstr1 = String.format("%s/media/bil/%s", getEcsHome(), fileName);
		String tmpstr2 = String.format("%s/media/bil/backup/%s.%-8.8s", getEcsHome(), fileName, sysDate);

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
//=============================================================================	
	void initData() {
		hBlfrFraudAmt = 0;
		hBlfrFunctionCode = "";
		hBlfrBinType = "";
		hBlfrRealCardNo = "";
		hBlfrFilmNo = "";
		hBlfrPurchaseDate = "";
		hBlfrFraudType = "";
		hBlfrMerchantCategory = "";
		hBlfrMerchantEngName = "";
		hBlfrMerchantCity = "";
		hBlfrMerchantZip = "";
		hBlfrPosEntryMode = "";

	}
}
