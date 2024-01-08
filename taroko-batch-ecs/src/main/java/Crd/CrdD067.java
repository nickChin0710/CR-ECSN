/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  110/07/05  V1.01.01  Castor      Initial                                 *
*  112/01/30  V1.01.02  Wilson      檔案格式調整                                                                                        *
*  112/04/07  V1.01.03  Wilson      檔案格式調整為TCB現行格式                                                             *
*  112/06/13  V1.01.04  Wilson      檔名不指定日期                                                                                    *
****************************************************************************/

package Crd;

import java.io.BufferedWriter;
//import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
//import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;
import java.util.Arrays;

import Crd.CrdD067;

/*接收卡廠製卡回饋檔處理程式*/
public class CrdD067 extends AccessDAO {
private String progname = "接收製卡郵寄資料回饋檔處理程式  112/06/13  V1.01.04";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	int debug = 1;

	String prgmId = "CrdD067";
	
	String fileFolderPath = comc.getECSHOME() + "/media/crd/";
	
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	
	protected final String dT1Str = " mail_status, barcode_num, return_seqno, id_no, card_type, " 
	                              + "transfer_date, card_no, acct_no, card_ref_num, chi_name, branch,"
	                              + "home_tel_no, office_tel_no, zip_code, mail_addr, card_code ";

    protected final int[] dt1Length = { 2, 20, 10, 11, 6,
    		                            8, 20, 13, 2, 33, 4,
    		                            18, 18, 5, 66, 1 };
	
	int rptSeq1 = 0;
	String buf = "";
    String queryDate = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	int hRecCnt1 = 0;
	int seq = 0;

	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;
	
	String tmpCardType;
    String tmpCardNo;
    String tmpMailNo;
    String tmpBarcodeNum;
    String tmpMailProcDate;
    String tmpIdNo;
    
    String[] moveFile;
	
	String hBusiBusinessDate = "";

	protected String[] dT1 = new String[] {};
	
	public int mainProcess(String[] args) {

		try {
			dT1 = dT1Str.split(",");
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
			comsecr = new CommSecr(getDBconnect(), getDBalias());

			hModUser = comc.commGetUserID();
			
            selectPtrBusinday();
			
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
			
			openFile();

			// ==============================================
			// 固定要做的
            showLogMessage("I", "", "執行結束,[ 總筆數 : "+ totalInputFile +"],[ 錯誤筆數 : "+ totalOutputFile +"]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
	
		hBusiBusinessDate = getValue("business_date");
			
	}

	/************************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;

		List<String> listOfFiles = comc.listFS(fileFolderPath, "", "");
	
		if (listOfFiles.size() > 0) {	
			
		    moveFile = new String[listOfFiles.size()];
			for (String file : listOfFiles) {
				getFileName = file;
				if (getFileName.length() != 17)
					continue;
				if (!getFileName.substring(0, 5).equals("POSTD"))
					continue;
				if (checkFileCtl() != 0) {
					continue;
				}
			   	moveFile[fileCount]=getFileName;
				fileCount++;
				readFile(getFileName);
		   }
		}
		//把搬檔案移到最後執行
		for (int i = 0; i < listOfFiles.size(); i++) {
	
			if(  moveFile[i] != null) {
				getFileName = moveFile[i];
				insertFileCtl(getFileName);
				renameFile(getFileName);
			}
		}
		
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理");
			
//			comcr.hCallErrorDesc = "無檔案可處理";
//            comcr.errRtn("無檔案可處理","處理日期 = " + queryDate  , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = fileFolderPath + fileName;

		int f = openInputText(fileName2);
		if (f == -1) {
			return 1;
		}
		closeInputText(f);

		setConsoleMode("N");
		fi = openInputText(fileName2, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return 1;
		}

		showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;

			totalInputFile++;
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			processDisplay(1000);
		}

		closeInputText(fi);

		return 0;
	}

	/***********************************************************************/
    private void moveData(Map<String, Object> map) throws Exception {       	
         
    	tmpCardType = (String) map.get("card_type"); //卡別
    	tmpCardType = tmpCardType.trim();
       
        tmpCardNo = (String) map.get("card_no"); //卡號
        tmpCardNo = tmpCardNo.trim();
                
        tmpBarcodeNum = (String) map.get("barcode_num"); //掛號條碼
        tmpBarcodeNum = tmpBarcodeNum.trim();
        
        tmpMailNo = comc.getSubString(tmpBarcodeNum,0,6); //掛號號碼
                        
        if(checkOrdCardType(tmpCardType)) {
        	updateCrdEmboss();
        	updateCrdCard();
        }
        else if(tmpCardType.equals("PP") || tmpCardType.equals("DR")) {
        	updateCrdEmbossPp();
        	updateCrdCardPp();
        }
        else if(tmpCardType.equals("VD") || tmpCardType.equals("VDUC") || tmpCardType.equals("VDLC")) {
        	updateDbcEmboss();
        	updateDbcCard();
        }else {
        	showLogMessage("I", "", "卡號[" + tmpCardNo + "]的卡別[" + tmpCardType + "]不須處理!");
        	totalOutputFile++;
        }
        
        hRecCnt1++;
        
        return;
    }

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
//      sqlCmd += " and crt_date = to_char(?,'yyyymmdd') ";
      setString(1, getFileName);
//      setString(2, queryDate1);
		int recordCnt = selectTable();

		if (recordCnt > 0)
			totalCount = getValueInt("totalCount");

		if (totalCount > 0) {
            showLogMessage("I", "", String.format("此檔案 = [" + getFileName + "]已處理過不可重複處理(crd_file_ctl)"));
			return (1);
		}
		return (0);
	}

	/***********************************************************************/
	 boolean checkOrdCardType(String str) throws Exception {   
		 
		String[] arr = new String[]{"JC","JG","JP","VC","VG","VP","VIE","MG","MC","MP","mg","MDAJ","MDAU","VTOR","VTORA",
				                    "MTHE","MT","MTECC","MTILAN","CMU","MCF","KNA","KNB","J","M","V","JR","MR","VR","MB",
				                    "ComboC","ComboG","ComboP","COMBOP","COMBO","ComboV","ComboM"};
	    return Arrays.stream(arr).anyMatch(s -> s.equals(str));
	 }
	 
	 /***********************************************************************/
	void updateCrdEmboss() throws Exception {

		daoTable = "crd_emboss";
		updateSQL = " mail_no = ?,";
		updateSQL += " barcode_num = ?,";
		updateSQL += " mail_proc_date = ?,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where card_no = ?  ";  
        whereStr +=" and to_vendor_date not in ('','29991231') ";
        whereStr +=" and mail_no = '' ";
        setString(1, tmpMailNo);
        setString(2, tmpBarcodeNum);
        setString(3, sysDate);
        setString(4,prgmId);
        setString(5, sysDate + sysTime);
        setString(6, tmpCardNo);

		updateTable();
		
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "ERROR : crd_emboss無可更新的資料，卡號[" + tmpCardNo + "]");
			totalOutputFile++;
		}

		return;
	}

	/***********************************************************************/
	void updateCrdCard() throws Exception {
		daoTable = "crd_card";
		updateSQL = " mail_no = ?,";
		updateSQL += " barcode_num = ?,";
		updateSQL += " mail_proc_date = ?,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where card_no = ?  ";  
        setString(1, tmpMailNo);
        setString(2, tmpBarcodeNum);
        setString(3, sysDate);
        setString(4,prgmId);
        setString(5, sysDate + sysTime);
        setString(6, tmpCardNo);

		updateTable();
		
		return;
	}
	
	/***********************************************************************/
	void updateCrdEmbossPp() throws Exception {

		daoTable = "crd_emboss_pp";
		updateSQL = " mail_no = ?,";
		updateSQL += " barcode_num = ?,";
		updateSQL += " mail_proc_date = ?,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where pp_card_no = ?  ";
        whereStr +=" and to_vendor_flag = 'Y' ";
        whereStr +=" and mail_no = '' ";
        setString(1, tmpMailNo);
        setString(2, tmpBarcodeNum);
        setString(3, sysDate);
        setString(4,prgmId);
        setString(5, sysDate + sysTime);
        setString(6, tmpCardNo);

		updateTable();
		
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "ERROR : crd_emboss_pp無可更新的資料，卡號[" + tmpCardNo + "]");
			totalOutputFile++;
		}

		return;
	}

	/***********************************************************************/
	void updateCrdCardPp() throws Exception {

		daoTable = "crd_card_pp";
		updateSQL = " mail_no = ?,";
		updateSQL += " barcode_num = ?,";
		updateSQL += " mail_proc_date = ?,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where pp_card_no = ?  ";
        setString(1, tmpMailNo);
        setString(2, tmpBarcodeNum);
        setString(3, sysDate);
        setString(4,prgmId);
        setString(5, sysDate + sysTime);
        setString(6, tmpCardNo);

		updateTable();
		
		return;
	}

	/***********************************************************************/
	void updateDbcEmboss() throws Exception {

		daoTable = "dbc_emboss";
		updateSQL = " mail_no = ?,";
		updateSQL += " barcode_num = ?,";
		updateSQL += " mail_proc_date = ?,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where card_no = ?  ";  
        whereStr +=" and to_nccc_date <> '' ";
        whereStr +=" and mail_no = '' ";
        setString(1, tmpMailNo);
        setString(2, tmpBarcodeNum);
        setString(3, sysDate);
        setString(4,prgmId);
        setString(5, sysDate + sysTime);
        setString(6, tmpCardNo);

		updateTable();
		
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "ERROR : dbc_emboss無可更新的資料，卡號[" + tmpCardNo + "]");
			totalOutputFile++;
		}

		return;
	}

	/***********************************************************************/
	void updateDbcCard() throws Exception {

		daoTable = "dbc_card";
		updateSQL = " mail_no = ?,";
		updateSQL += " barcode_num = ?,";
		updateSQL += " mail_proc_date = ?,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where card_no = ?  ";  
        setString(1, tmpMailNo);
        setString(2, tmpBarcodeNum);
        setString(3, sysDate);
        setString(4,prgmId);
        setString(5, sysDate + sysTime);
        setString(6, tmpCardNo);

		updateTable();
		
		return;
	}

	/***********************************************************************/
	void insertFileCtl(String fileName) throws Exception {
		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", hRecCnt1);
		setValueInt("record_cnt", hRecCnt1);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, hRecCnt1);
			setInt(2, hRecCnt1);
			setString(3, hNcccFilename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		CrdD067 proc = new CrdD067();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
	
	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = fileFolderPath + removeFileName;
		String tmpstr2 = fileFolderPath +"backup/" + removeFileName;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/****************************************************************************/
	String[] getFieldValue(String rec, int[] parm) {
		int x = 0;
		int y = 0;
		byte[] bt = null;
		String[] ss = new String[parm.length];
		try {
			bt = rec.getBytes("MS950");
		} catch (Exception e) {
			showLogMessage("I", "", comc.getStackTraceString(e));
		}
		for (int i : parm) {
			try {
				ss[y] = new String(bt, x, i, "MS950");
			} catch (Exception e) {
				showLogMessage("I", "", comc.getStackTraceString(e));
			}
			y++;
			x = x + i;
		}
		return ss;
	}

	/****************************************************************************/	
	private Map<String,Object> processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}
	
}