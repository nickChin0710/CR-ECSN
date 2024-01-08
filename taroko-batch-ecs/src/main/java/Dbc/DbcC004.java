/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/12/24  V1.00.00    Pino      program initial                           *
*  109/03/11  V1.00.01    Pino      program initial                           *
*  109/05/14  V1.00.02    Wilson    bk -> backup                              *
*  109/11/13  V1.00.03 yanghan       修改了變量名稱和方法名稱                                                                                *
*  109/12/24  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
*  112/02/08  V1.00.05    Wilson    讀取檔案路徑調整                                                                                      *
*  112/04/25  V1.00.06    Wilson    hDdtpRtnCode = "000" -> hRejectCode = ""  *
*  112/05/18  V1.00.07    Wilson    新增日期參數                                                                                             *
*  112/06/13  V1.00.08    Wilson    無檔案不當掉                                                                                             *
*  112/06/19  V1.00.09    Wilson    hRejectCode chg to hCheckCode             *
*  112/07/03  V1.00.10    Wilson    假日不執行                                                                                                 *
******************************************************************************/

package Dbc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*讀取VD續卡清單回覆檔作業*/
public class DbcC004 extends AccessDAO {
	private String progname = "讀取VD續卡清單回覆檔作業  112/07/03 V1.00.10";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	int debug = 1;
	int debugD = 1;
	long totalCnt = 0;
	int tmpInt = 0;
	String checkHome = "";
	String hTempUser = "";
	String cmdStr = "";

	String prgmId = "DbcC004";
	String stderr = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String iFileName = "";

	String hDcepCardNo = "";
	int recCnt = 0;
	String hDcepApplySource = "";
	String hDdtpRtnIbmDate = "";
	String hDdtpBatchno = "";
	double hDdtpRecno = 0;
	String hDdtpRowid = "";
	String hDdtpRtnCode = "";
	String hProcDate = "";
	String hCheckCode = "";
    String hInMainError = "";
    String hInMainMsg = "";
	String hApplySource = "";
	String hDcepBatchno = "";
	String hSystemDate = "";
	String hDdtpSavingActnoExt = "";

	String hTmpOldCardNo = "";
	String hTmpCardRefNum = "";
	String hTmpAp1ApplyDate = "";

	String filename1 = "";
	String hBusinessDate = "";

	double hDcepRecno = 0;
	int hCount = 0;
	int hNn = 0;

	buf1 detailSt = new buf1();
	String hBusiBusinessDate = "";
    String queryDate = "";
    String fileFolderPath = comc.getECSHOME() + "/media/dbc/";
	// ************************************************************************

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

			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

			String checkHome = comc.getECSHOME();
			if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
				comcr.hCallBatchSeqno = "no-call";
			}

			comcr.hCallRProgramCode = javaProgram;
			hTempUser = "";
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

			hModUser = comc.commGetUserID();
						
//            // 若沒有給定查詢日期，則查詢日期為系統日
//            if(args.length == 0) {
//                queryDate = hBusiBusinessDate;
//            }else
//            if(args.length == 1) {
//                if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
//                    showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
//                    return -1;
//                }
//                queryDate = args[0];
//            }else {
//                comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
//            }
            
            commonRtn();
            
			showLogMessage("I", "", String.format("今日營業日 = [%s]", hBusinessDate));
			
            if (checkPtrHoliday() != 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }

            openFile();

			// ==============================================
			// 固定要做的

			comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "][" + hCount + "]";
			showLogMessage("I", "", comcr.hCallErrorDesc);

			if (comcr.hCallBatchSeqno.length() == 20)
				comcr.callbatch(1, 0, 1); // 1: 結束

			finalProcess();
			return 0;
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess

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
	int openFile() throws Exception {
		int fileCount = 0;
		
		List<String> listOfFiles = comc.listFS(fileFolderPath, "", "");

//				final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "F00600000", "ICCRSQND",
//				new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式		
		
//		final String fileNameTemplate = String.format("%s\\.%s\\..*", "F00600000", "ICCRSQND"); // 檔案正規表達式		
		
		if (listOfFiles.size() > 0)
		for (String file : listOfFiles) {
			iFileName = file;

			if (iFileName.length() != 24)
				continue;
			
			if(!comc.getSubString(iFileName,0,10).equals("resp_vd_t_"))  
			{
//				System.out.println(file+" NOT MAP "+fileNameTemplate);
				continue;
			}
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(iFileName);
		}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理");
			
//			comcr.hCallErrorDesc = "無檔案可處理";
//            comcr.errRtn("無檔案可處理","處理日期 = " + queryDate  , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	   int checkFileCtl() throws Exception {
	        sqlCmd = "select count(*) as all_cnt ";
	        sqlCmd += " from crd_file_ctl  ";
	        sqlCmd += "where file_name like ?  ";
	        setString(1, iFileName);
	        selectTable();
	        if (debugD == 1)
	            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
	        if (getValueInt("all_cnt") > 0) {
	            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + iFileName + "]");
	            return 1;
	        }

	        return 0;
	    }    
	    
	 // ************************************************************************
	int readFile(String fileName) throws Exception {
		String str900 = "";
		String fileName2;
		fileName2 = fileFolderPath + fileName;
		hCount = 0;

		if (openBinaryInput(fileName2) == false) {
			return 1;
		}
		
		showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		byte[] bytes = new byte[detailSt.allText().length()];
		while (readBinFile(bytes) > 0) {
			str900 = new String(bytes, "MS950");
			splitBuf1(str900);
			moveData();
		}
		closeBinaryInput();
	 
		String cmdStr = String.format("mv %s %s/media/dbc/backup/%s", fileName2, comc.getECSHOME(), iFileName);	
		String target = String.format("%s/media/dbc/backup/%s", comc.getECSHOME(), iFileName);
		comc.fileRename2(fileName2, target);
		showLogMessage("I", "", String.format("[%s]", cmdStr));
		
		insertFileCtl();
		
		return 0;
	}

	/***********************************************************************/
	int moveData() throws Exception {
		String tmpStr = "";
		int rtn;

		init();

		tmpStr = comc.eraseSpace(detailSt.cardKind);
//		if (!tmpStr.equals("V"))
//			hCheckCode = "D51";

		tmpStr = comc.eraseSpace(detailSt.cardSource);
//		if (!tmpStr.equals("T"))
//			hCheckCode = "D52";
		hDcepApplySource = tmpStr;

		tmpStr = comc.eraseSpace(detailSt.savingActno);
		hDdtpSavingActnoExt = tmpStr;

		tmpStr = comc.eraseSpace(detailSt.oldCardNo);
		hTmpOldCardNo = tmpStr;

		tmpStr = comc.eraseSpace(detailSt.newCardNo);
		hDcepCardNo = tmpStr;

		tmpStr = comc.eraseSpace(detailSt.cardRefNum);
		hTmpCardRefNum = tmpStr;

		tmpStr = comc.eraseSpace(detailSt.responseDate);
		hTmpAp1ApplyDate = tmpStr;

		tmpStr = comc.eraseSpace(detailSt.responseCode);
		hDdtpRtnCode = tmpStr;

		updateDbcCebit();
		totalCnt++;

		return 0;
	}

	/***********************************************************************/
	int updateDbcCebit() throws Exception {
		if (debug == 1)
			showLogMessage("I", "", " update_dbc_debit=[" + "]" + hDcepCardNo);
		if ((hDcepCardNo.length() > 0)) {
			hDdtpRtnIbmDate = "";
			hDdtpBatchno = "";
			hDdtpRecno = 0;
			hDdtpRowid = "";
			sqlCmd = "select rtn_ibm_date,";
			sqlCmd += "batchno,";
			sqlCmd += "recno,";
			sqlCmd += "rowid  as rowid ";
			sqlCmd += " from dbc_debit  ";
			sqlCmd += "where card_no = ? ";
			setString(1, hDcepCardNo);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hDdtpRtnIbmDate = getValue("rtn_ibm_date");
				hDdtpBatchno = getValue("batchno");
				hDdtpRecno = getValueDouble("recno");
				hDdtpRowid = getValue("rowid");
			} else {
				return (1);
			}

			if (hDdtpRtnIbmDate.length() >= 8) {
				stderr = String.format("cardno[%s]此筆資料AP1已回饋,不可重複寫入", hDcepCardNo);
				showLogMessage("I", "", stderr);
				return (1);
			}
			daoTable = "dbc_debit";
			updateSQL += " rtn_code     = ? ,";
			updateSQL += " rtn_ibm_date = ?,";
			updateSQL += " mod_time     = sysdate,";
			updateSQL += " mod_user     = ?,";
			updateSQL += " mod_pgm      = ?";
			whereStr = "where rowid  = ? ";
			setString(1, hDdtpRtnCode);
			setString(2, sysDate);
			setString(3, hModUser);
			setString(4, prgmId);
			setRowId(5, hDdtpRowid);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_dbc_debit not found!", "", comcr.hCallBatchSeqno);
			}

            hCheckCode = "";
            if (hDdtpRtnCode.substring(0, 3).equals("000")) {
            	hCheckCode = "";
            	hInMainError = "";
            	hInMainMsg = "";
            	updateDbcEmboss();
                hCount++;
            }
            else if (!hDdtpRtnCode.equals("000")) {
            	hCheckCode = hDdtpRtnCode;
            	hInMainError = "1";
            	selectCrdMessage();
            	updateDbcEmboss();
            }			
		}

		return (0);
	}

	/***********************************************************************/
	void updateDbcEmboss() throws Exception {

		hApplySource = hDcepApplySource;

		daoTable = "dbc_emboss";
		updateSQL = " check_code   = ?,";
        updateSQL += " in_main_error = ?,";
        updateSQL += " in_main_msg = ?,";
		updateSQL += " ap1_apply_date  = ?,";
		updateSQL += " card_ref_num  = ?,";
		updateSQL += " mod_time      = sysdate,";
		updateSQL += " mod_user      = ?,";
		updateSQL += " mod_pgm       = ?";
		whereStr = "where batchno  = ?  ";
		whereStr += "  and recno    = ? ";
		setString(1, hCheckCode);
        setString(2, hInMainError);
        setString(3, hInMainMsg);
		setString(4, hTmpAp1ApplyDate);
		setString(5, hTmpCardRefNum);
		setString(6, hModUser);
		setString(7, prgmId);
		setString(8, hDdtpBatchno);
		setDouble(9, hDdtpRecno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_emboss not found!", "", comcr.hCallBatchSeqno);
		}

		return;
	}

	/***********************************************************************/
    void selectCrdMessage() throws Exception { 
    	
   	sqlCmd = "select msg ";
       sqlCmd += " from crd_message  ";
       sqlCmd += "where msg_type = 'NEW_CARD' ";
       sqlCmd += "and msg_value = ? ";
       setString(1, hCheckCode);
       int recordCnt = selectTable();
       if (recordCnt > 0) {
       	hInMainMsg = getValue("msg");
       }       
   }      
   
   /**********************************************************************/
	void insertFileCtl() throws Exception {
		recCnt = (int) totalCnt;
		daoTable = "crd_file_ctl";
		setValue("file_name", iFileName);
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
			setString(3, iFileName);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/***********************************************************************/
	void init() {

		hDcepCardNo = "";
		hDcepApplySource = "";
		hDdtpBatchno = "";
		hDdtpRecno = 0;
		hDdtpSavingActnoExt = "";
		hDdtpRtnIbmDate = "";
		hDdtpRtnCode = "";
		hDdtpRowid = "";

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcC004 proc = new DbcC004();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class buf1 {
		String cardKind;
		String cardSource;
		String savingActno;
		String oldCardNo;
		String newCardNo;
		String cardRefNum;
		String responseDate;
		String responseCode;
		String enter;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(cardKind, 1);
			rtn += comc.fixLeft(cardSource, 1);
			rtn += comc.fixLeft(savingActno, 13);
			rtn += comc.fixLeft(oldCardNo, 16);
			rtn += comc.fixLeft(newCardNo, 16);
			rtn += comc.fixLeft(cardRefNum, 2);
			rtn += comc.fixLeft(responseDate, 8);
			rtn += comc.fixLeft(responseCode, 3);
			rtn += comc.fixLeft(enter, 2);

			return rtn;
		}
	}

	// ************************************************************************
	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");

		detailSt.cardKind = comc.subMS950StringR(bytes, 0, 1);
		detailSt.cardSource = comc.subMS950StringR(bytes, 1, 1);
		detailSt.savingActno = comc.subMS950StringR(bytes, 2, 13);
		detailSt.oldCardNo = comc.subMS950StringR(bytes, 15, 16);
		detailSt.newCardNo = comc.subMS950StringR(bytes, 31, 16);
		detailSt.cardRefNum = comc.subMS950StringR(bytes, 47, 2);
		detailSt.responseDate = comc.subMS950StringR(bytes, 49, 8);
		detailSt.responseCode = comc.subMS950StringR(bytes, 57, 3);
		detailSt.enter = comc.subMS950StringR(bytes, 60, 2);

	}

	// ************************************************************************
}
