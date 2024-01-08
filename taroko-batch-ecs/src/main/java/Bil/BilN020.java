package Bil;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112/02/16  V0.00.01     JeffKung  initial                                  *
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

public class BilN020 extends AccessDAO {
	private String PROGNAME = "處理NCCC分期特店匯入檔案 112/02/16 V0.00.01";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate zzdate = new CommDate();
	private String isFileNames = "NCCC_MCHT_IN.TXT";
	private int iiFileNum = 0;

	String modUser = "";
	// ******** nccc來源 ********//
	String npMchtNoM = "";
	String npMchtNo = "";
	String npMccCode = "";
	String npStartDate = "";
	String npTotTerm = "";
	String npEndDate = "";
	String npFeeCat = "";
	Double npInterestRate = 0.0;
	String npBinNoStart = "";
	String npBinNoEnd = "";
	String npMchtChiName = "";
	String npMchtRegiName = "";
	String npCorpNo = "";
	String npBizAddr = "";
	String npProcessRC = "";

	String hBusiBusinessDate = "";

	public void mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================
			if (args.length != 0) {
				comc.errExit("Usage : BilN020 ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();
			
			//開啟回覆檔案
			String filename1 = String.format("%s/media/bil/NCCC_MCHT_RSP.%8.8s", comc.getECSHOME(), hBusiBusinessDate);
		    if ( openBinaryOutput(filename1) == false) {
		    	showLogMessage("E", "", String.format("無法開啟回覆檔案[%s]", filename1));
		    	return;
		    }

			readFile(isFileNames);
			
			closeBinaryOutput2(0);
			procFTP(String.format("NCCC_MCHT_RSP.%8.8s",hBusiBusinessDate));

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

	//=============================================================================
	void readFile(String fileName) throws Exception {

		String readData = "";
		String outData = "";
		String tmpstr = "";
		int totalCnt = 0;
		int realCnt = 0;
		int fieldCnt = 0;

		String lsFile = String.format("%s/media/bil/%s", comc.getECSHOME(), fileName);
		showLogMessage("I", "", "file path = [" + lsFile + "]");
		iiFileNum = openInputText(lsFile,"MS950");
		if (iiFileNum == -1) {
			showLogMessage("I", "", String.format("無檔案可處理 [%s]", fileName));
			return;
		}

		while (true) {

			readData = readTextFile(iiFileNum).trim();
			
			npProcessRC = "";
			outData = readData;

			if (endFile[iiFileNum].equals("Y"))
				break;
			if (readData.length() < 10) {
				npProcessRC = "資料長度有誤不處理";
				tmpstr = npProcessRC + "," + outData + "\r\n";
			    writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
				continue;
			}

			totalCnt++;

			if (totalCnt == 1) {
				// 欄位說明-bypass不處理
				npProcessRC = "處理結果";
				tmpstr = npProcessRC + "," + outData + "\r\n";
			    writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
				continue;
			}

			realCnt++;

			// 在每列最後加上1欄結束符號",E"
			readData += ",E";

			String[] dataArr = readData.split(",");
			fieldCnt = dataArr.length;
			
			//debug
			//showLogMessage("I", "", String.format("讀入資料欄位數 [%d]", fieldCnt));

			npMchtNoM = dataArr[3].trim();
			npMchtNo = dataArr[4].trim();
			npMccCode = dataArr[5].trim();
			npMchtRegiName = dataArr[6].trim();
			npMchtChiName = dataArr[7].trim();
			npCorpNo = dataArr[8].trim();
			npBizAddr = dataArr[9].trim();
			npStartDate = convertDate(dataArr[10].trim());
			npEndDate = convertDate(dataArr[11].trim());
			npTotTerm = dataArr[12].trim();
			npInterestRate = comc.str2double(dataArr[13].trim());
			
			//***資料檢核還要加入
			if (npMchtNo.length()==0) {
				npProcessRC = "特店代號空值";
			} else if (npMccCode.length()==0) {
				npProcessRC = "MCCC空值";
			} else if (npMchtChiName.length()==0) {
				npProcessRC = "特店中文名稱為空值";
			} else if (npStartDate.length()==0) {
				npProcessRC = "生效日期為空值";
			} else if (npEndDate.length()==0) {
				npProcessRC = "停用日期為空值";
			} else if (comc.str2int(npTotTerm) == 0) {
				npProcessRC = "期數為0或不為數字";
			} else {
				insertBilProdNccc();
			}
			
			if (realCnt % 100 == 0) {
				showLogMessage("I", "", "Process Count :  " + realCnt);
				countCommit();
			}
			
			if (npProcessRC.length() == 0) {
				npProcessRC = "處理成功";
				if (npInterestRate == 0) {
					npProcessRC += "-特店手續費率為0或不為數字";
				}
			}
			
			tmpstr = npProcessRC + "," + outData + "\r\n";
		    writeBinFile2(0, tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

		}

		closeInputText(iiFileNum);
		showLogMessage("I", "", "檔案轉入 [" + realCnt + "] 筆");
		renameFile(fileName);
	}

	// =============================================================================
	String convertDate(String oldFormatData) throws Exception {
		String newFormatData = "";

		SimpleDateFormat oldData = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat newData = new SimpleDateFormat("yyyyMMdd");
		try {
			Date old = oldData.parse(oldFormatData);
			newFormatData = newData.format(old);
		} catch (Exception ex) {
			showLogMessage("E", "", "日期格式轉換有誤 [" + oldFormatData + "] ");
		}

		return newFormatData;
		
	}

	//=============================================================================
	void insertBilProdNccc() throws Exception {
		daoTable = "BIL_PROD_NCCC";
		setValue("PRODUCT_NO", String.format("%02d", comc.str2int(npTotTerm)));
		setValue("PRODUCT_NAME", "分期交易NCCC");
		setValue("MCHT_NO", npMchtNo);
		setValueInt("SEQ_NO", 1);
		setValue("START_DATE", npStartDate);
		setValue("END_DATE", npEndDate);
		setValueDouble("LIMIT_MIN", 0.0);
		setValueDouble("UNIT_PRICE", 0.0);
		setValueDouble("TOT_AMT", 0.0);
		setValueInt("TOT_TERM", 0);
		setValueDouble("REMD_AMT", 0.0);
		setValueDouble("EXTRA_FEES", 0.0);
		setValueDouble("FEES_FIX_AMT", 0.0);
		setValueDouble("FEES_MIN_AMT", 0.0);
		setValueDouble("FEES_MAX_AMT", 0.0);
		setValueDouble("INTEREST_RATE", npInterestRate);
		setValueDouble("INTEREST_MIN_RATE", 0.0);
		setValueDouble("INTEREST_MAX_RATE", 0.0);
		setValueDouble("CLT_FEES_FIX_AMT", 0.0);
		setValueDouble("CLT_INTEREST_RATE", 0.0);
		setValueInt("AGAINST_NUM", 0);
		setValue("DTL_FLAG", "Y");
		setValue("CONFIRM_FLAG", "");
		setValue("INSTALLMENT_FLAG", "");
		setValueDouble("YEAR_FEES_RATE", 0.0);
		setValueDouble("TRANS_RATE", 0.0);
		setValue("CRT_DATE", hBusiBusinessDate);
		setValue("CRT_USER", "system");
		setValue("MOD_USER", "system");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValueInt("MOD_SEQNO", 0);
		setValue("MCHT_NO_M", npMchtNoM);
		setValue("MCC_CODE", npMccCode);
		setValue("FEE_CAT", "I");
		setValue("BIN_NO_START", "");
		setValue("BIN_NO_END", "");

		insertTable();

		if (dupRecord.equals("Y")) {
			npProcessRC = "特店分期商品資料(prod_nccc)已存在";
			return;
		}

		insertBilProdNcccBin();
		
		if (npProcessRC.length() ==0) {
			insertBilMerchant();
		}

	}

	void insertBilProdNcccBin() throws Exception {
		// insert 2筆 DTL_VALUE = '01' & '03'
		daoTable = "BIL_PROD_NCCC_BIN";
		setValue("PRODUCT_NO", String.format("%02d", comc.str2int(npTotTerm)));
		setValue("MCHT_NO", npMchtNo);
		setValueInt("SEQ_NO", 1);
		setValue("BIN_NO", "N");
		setValue("DTL_KIND", "ACCT-TYPE");
		setValue("DTL_VALUE", "01");
		insertTable();

		if (dupRecord.equals("Y")) {
			npProcessRC = "特店分期商品資料(prod_nccc_bin)已存在";
		}

		daoTable = "BIL_PROD_NCCC_BIN";
		setValue("PRODUCT_NO", String.format("%02d", comc.str2int(npTotTerm)));
		setValue("MCHT_NO", npMchtNo);
		setValueInt("SEQ_NO", 1);
		setValue("BIN_NO", "N");
		setValue("DTL_KIND", "ACCT-TYPE");
		setValue("DTL_VALUE", "03");
		insertTable();

		if (dupRecord.equals("Y")) {
			npProcessRC = "特店分期商品資料(prod_nccc_bin)已存在";
		}
	}

	void insertBilMerchant() throws Exception {
		daoTable = "BIL_MERCHANT";
		setValue("mcht_no", npMchtNo);
		setValue("uniform_no", npCorpNo);
		setValue("mcht_status", "1");
		setValue("mcc_code", npMccCode);
		if (npMchtNo.equals(npMchtNoM)) {
			setValue("mcht_property", "M");
		} else {
			setValue("mcht_property", "S");
		}
		setValue("mcht_type", "2");
		setValue("stmt_inst_flag", "N");
		setValue("installment_delay", "Y");
		setValue("trans_flag", "N");
		setValue("mcht_chi_name", npMchtChiName);
		setValue("mcht_address", npBizAddr);
		setValueInt("mp_rate", 100);
		setValue("mcht_city","");
		setValue("mcht_country","TW");
		setValue("mcht_state","TW");
		setValue("tx_type","2");  //1.一般 2.分期 3.郵購 4.網路
		
		setValue("MOD_USER", "system");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		setValueInt("MOD_SEQNO", 0);
		insertTable();

		if (dupRecord.equals("Y")) {
			npProcessRC = "處理成功-特店資料(merchant)已存在";
			if (npInterestRate == 0) {
				npProcessRC += "-特店手續費率為0或不為數字";
			}
		}
	}

	void procFTP(String isFileName) throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/bil", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		showLogMessage("I", "", "put " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2EMP", "put " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
		} else {
			comc.fileRename2(String.format("%s/media/bil/", comc.getECSHOME()) + isFileName,
					String.format("%s/media/bil/backup/", comc.getECSHOME()) + isFileName);
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

    //=============================================================================
	void renameFile(String fileName) throws Exception {
		String tmpstr1 = String.format("%s/media/bil/%s", comc.getECSHOME(), fileName);
		String tmpstr2 = String.format("%s/media/bil/backup/%s.%-8.8s", comc.getECSHOME(), fileName, sysDate);

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("E", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) throws Exception {
		BilN020 proc = new BilN020();
		proc.mainProcess(args);
		return;
	}
	// ************************************************************************

}
