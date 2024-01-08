/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  109/06/02  V1.01.01  Rou         Initial                                 *
*  109/07/03  V1.01.02  Wilson      MBBBBBBBB -> M00600000                  *
*  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *                                                        *
*  109/07/03  V1.01.03  JustinWu  change the program name and add queryDate*
*  109/07/08  V1.01.04  Wilson    測試修改                                                                                                    *
*  109-07-22  V1.01.04  yanghan    修改了字段名称            *
*  109/08/14  V1.01.06  Wilson      資料夾名稱修改為小寫                                                                        *
*  109/08/17  V1.01.07  Wilson     insert msg_flag = "Y"                    *
*  109/08/27  V1.01.08  Wilson     idno_credit_limit不update                 *
*  109/09/10  V1.01.09  Alex       修改電話號碼取欄位方式                                                                 *
*  109/09/16  V1.01.10  Wilson     電話號碼測試調整                                                                                 *
*  109/09/21  V1.01.11  Wilson     insert crd_idno_seqno 新增debit_idno_flag *
*  109/09/28  V1.01.12  Wilson     無檔案不秀error、讀檔不綁檔名日期                                              *
*  109/09/30  V1.01.13  Wilson     無檔案秀error                              *
*  109/10/06  V1.01.14  Wilson     讀檔要綁檔名日期                                                                                  *
*  109/10/12  V1.01.15  Wilson     檔名日期改營業日                                                                                  *
*  109/10/16  V1.01.16  Wilson     錯誤報表FTP                                *     
*  109-10-19  V1.00.17  shiyuqi       updated for project coding standard    * 
*  109/12/25  V1.01.18  Wilson     無檔案正常結束                                                                                      *
*  110/09/09  V1.01.19  Wilson     異動碼為'c'，額度update act_acno              *
*  110/12/02  V1.01.20  Justin     add commit and rollback when writing error reports *
*  110/12/10  V1.01.21  Wilson     insert crd_idno新增eng_name               *
*  111/01/06  V1.01.22  Wilson     mark 異動碼為'c'，額度update act_acno         *       
*  111/01/21  V1.01.23  Justin     若異動碼為C但UPDATE CRD_IDNO找不到資料時，改成走跟異動碼為A一樣的邏輯
*                                  新增參數讓使用者可以指定要跑異動碼為A或C的資料(若無指定就是都跑)
*  111/01/24  V1.01.24  Justin     修改錯誤訊息                            *
*  111/01/25  V1.01.25  Justin     修改參數順序, 增加insert check_code     *
*  111/01/27  V1.01.26  Justin     修改ifUpdateCrdIdno判斷條件bug          *
*  111/02/14  V1.01.27  Justin     sort files by their modified dates      *
*  111/02/14  V1.01.28    Ryan     big5 to MS950                           *
*  111/02/15  V1.01.29  Justin     prevent from out of index               *
*                                  when substring Chinese name             *
*  111/02/16  V1.01.30  Justin     rename error file date                  * 
*  111/02/17  V1.01.31  Justin     fix the bug of error files              *
*  111/02/17  V1.01.32  Justin     fix the bug of error files              * 
*  111/02/18  V1.01.33  Justin     處理電話號碼問題                        *  
*  111/02/19  V1.01.34  Justin     新增新邏輯                              *
*  111/02/25  V1.01.35  Justin     修改產檔及讀檔邏輯                      *
*  111/03/01  V1.01.36  Justin     check if customerId contains "*"        *
*  111/03/02  V1.01.37  Justin     刪除Err字新增處理訊息                   *
*  111/03/23  V1.01.38  Justin     update crd_idno 增加tmpCreditLimit      *
*  111/05/26  V1.01.39  Justin     處理資料長度異常                        *
*  111/07/19  V1.01.40  Ryan       修改updateCrdIdno(),判斷純附卡人不異動手機號碼            *
*  111/08/08  V1.01.41  Wilson     增加mod_user                              *
*  111/10/05  V1.01.42  Wilson     insertCrdIdno 寫入卡人資料檔重複不當掉                             *
****************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;

import Dxc.Util.SecurityUtil;

/*CARDLINK信用卡客戶資料處理程式*/
public class IcuD001 extends AccessDAO {
private static final String DATA_FILE_NAME = "ICCUSQND";
private static final String ISSUE_UNIT_CODE = "M00600000";
private String progname = "CARDLINK信用卡客戶資料處理程式  111/10/05  V1.01.42";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	int debug = 1;

	String prgmId = "IcuD001";
	
	String fileFolderPath = comc.getECSHOME() + "/media/icu/";
	
	private String ifUpdateCrdIdnoStr = null;
	String modNoArg = "";
	int dataCntA = 0;
	int dataCntC = 0;
	boolean isSkipA = false;
	boolean isSkipC = false;
	
	ArrayList<String> outputIdnoList = null;
	
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	Buf1 ncccData = new Buf1();
	protected final String dT1Str = "col1, col2, col3, credit_limit, eng_name, sex, birthday, chi_name, col9, home_tel, office_tel, cellar_phone, col13";

	protected final int[] dt1Length = { 1, 8, 11, 9, 30, 1, 8, 80, 2, 12, 12, 20, 4 };
	final int DATA_LENGTH = 198;
	
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
	int totalOutputFileEachFile;

	String tmpModNo;
	String tmpCardUnit;
	String tmpIdnoCode;
	Double tmpCreditLimit;
	String tmpEngName;
	String tmpSex;
    String idnoSex;
	String tmpBirthday;
	String tmpChiName;
	String tmpIdnoRank;
	String tmpHomeTel;
	String tmpOfficeTel;
	String tmpCellarPhone;
	String tmpErrCode;
	String idnoCode;
	String idnoIdPSeqno;
	String homeAreaCode;
	String homeTelNo;
	String officeAreaCode;
	String officeTelNo;
	String errCode;
	String idFlag;
	String billApplyFlag;
	String hBusiBusinessDate = "";
	String hPrevBusiBusinessDate = "";

	protected String[] dT1 = new String[] {};
	Buf1 ncccData1 = new Buf1();

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
			
            // 若沒有給定查詢日期，則查詢日期為系統日
            if(args.length == 0) {
            	modNoArg = "";
//            	queryDate = hBusiBusinessDate;
            	queryDate = "";
            }else if(args.length == 1) {
            	if (args[0].trim().isEmpty() == false && 
                		( "A".equals(args[0]) || "C".equals(args[0]) ) ) {
                	modNoArg = args[0];
				}else {
					modNoArg = "";
				}
//            	queryDate = hBusiBusinessDate;
            	queryDate = "";
            }else if(args.length == 2) {
            	if (args[0].trim().isEmpty() == false && 
                		( "A".equals(args[0]) || "C".equals(args[0]) ) ) {
                	modNoArg = args[0];
				}else {
					modNoArg = "";
				}
            	
            	if ( ! new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
                    showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[1]));
                    return -1;
                }
                queryDate = args[1];
                
            }else {
                comc.errExit("參數1：預設會跑異動碼A與C，可輸入異動碼A或C; 參數2：非必填，非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
            }                       
			
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
		hPrevBusiBusinessDate = "";

		sqlCmd = " select business_date , to_char( to_date(business_date, 'yyyymmdd') - 1 DAYS , 'yyyymmdd') as prev_business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("business_date");
		hPrevBusiBusinessDate = getValue("prev_business_date");
	}

	/************************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;


		List<String> listOfFiles = comc.listFsSort(fileFolderPath);

//				final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "M00600000", "ICCUSQND",
//				new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式			

		/////////////////////////
		String fileNameTemplate  = ""; // String fileNameTemplate
		String fileNameTemplate2 = ""; // previous business date

		if (queryDate.length() > 0) {
			fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
					ISSUE_UNIT_CODE, 
					DATA_FILE_NAME
					,new CommDate().getLastTwoTWDate(queryDate), 
					queryDate.substring(4, 8)); // 檔案正規表達式
			
			showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate));
		}else {
			fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
					ISSUE_UNIT_CODE, 
					DATA_FILE_NAME
					,new CommDate().getLastTwoTWDate(hBusiBusinessDate), 
					hBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
			
			fileNameTemplate2 = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
					ISSUE_UNIT_CODE, 
					DATA_FILE_NAME,
					new CommDate().getLastTwoTWDate(hPrevBusiBusinessDate), 
					hPrevBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
			
			showLogMessage("I", "", String.format("尋找檔案[%s] 或", fileNameTemplate));
			showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate2));
		}
		
		/////////////////////////
		
		if (listOfFiles.size() > 0)
		for (String file : listOfFiles) {
			getFileName = file;
//			if (getFileName.length() != 27)
//				continue;
//			if (!getFileName.substring(0, 19).equals("M00600000.ICCUSQND."))
//				continue;
			if( !( file.matches(fileNameTemplate) || ( fileNameTemplate2.length() > 0 && file.matches(fileNameTemplate2) ) ))  
				continue;
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理,處理日期 = " + queryDate);
			
//			comcr.hCallErrorDesc = "無檔案可處理";
//            comcr.errRtn("無檔案可處理","處理日期 = " + queryDate  , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		totalOutputFileEachFile = 0;
		
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = fileFolderPath + fileName;

		int f = openInputText(fileName2);
		if (f == -1) {
			return 1;
		}
		closeInputText(f);

		showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		dataCntA = 0;
		dataCntC = 0;
		if ("C".equals(modNoArg)) {
			// load the data from the file
			String fileCPath = fileFolderPath + fileName.substring(0,19) + "C" + fileName.substring(18);
			fileCPath = SecurityUtil.verifyPath(fileCPath);
			showLogMessage("I", "", String.format("參數異動碼為[%s]，讀取[%s]", modNoArg, fileCPath));
			int fileCIndex = openInputText(fileCPath, "MS950");
			if (fileCIndex != -1) {
				while (true) {
					String text = readTextFile(fileCIndex); 
					if (endFile[fileCIndex].equals("Y")) break;
					totalInputFile++;
					moveData(processDataRecord(getFieldValue(text, dt1Length), dT1), text);
					processDisplay(1000);	
				}
				closeInputText(fileCIndex);
			}else {
				showLogMessage("I", "", String.format("檔案[%s]不存在，因此無法處理", fileCPath));
			}
		}else {
			// 
			if ("A".equals(modNoArg)) {
				outputIdnoList = new ArrayList<String>();
			}
			
			setConsoleMode("N");
			fi = openInputText(fileName2, "MS950");
			setConsoleMode("Y");
			if (fi == -1) {
				return 1;
			}
			
			// read each line
			while (true) {
				rec = readTextFile(fi); // read file data
				if (endFile[fi].equals("Y")) break;
				totalInputFile++;
				moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1), rec);
				processDisplay(1000);
			}
			
			closeInputText(fi);
		}

		// output error report
		if (totalOutputFileEachFile > 0) {
			outPutTextFile(fileName);
			comc.writeReport(outFileName, lpar1, "MS950");
			
			insertFileCtl();

			lpar1.clear();
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}
		
		// write update files
		if ("A".equals(modNoArg) && outputIdnoList!= null && outputIdnoList.isEmpty() == false) {
			String outputIdnoFilePath = fileFolderPath + fileName.substring(0,19) + "C" + fileName.substring(18);
			outputIdnoFilePath = SecurityUtil.verifyPath(outputIdnoFilePath);
			int outputIdnoFileIndex = openOutputText(outputIdnoFilePath, "MS950");
			for (int j = 0; j < outputIdnoList.size(); j++) {
				writeTextFile(outputIdnoFileIndex, outputIdnoList.get(j) + "\r\n");
			}
			outputIdnoList = null;
			closeOutputText(outputIdnoFileIndex);
		}		
		
		showLogMessage("I", "", String.format("此檔案[%s]進行異動碼[%s]處理，總共處理：異動碼A[%d],異動碼C[%d]", fileName, modNoArg, dataCntA, dataCntC));
		
		switch(modNoArg) {
		case "":
			if (isSkipA == false) {
				insertFileCtl1(fileName, "A", dataCntA);
			}
			if (isSkipC == false) {
				insertFileCtl1(fileName, "C", dataCntC);
			}
			break;
		case "A":
			if (isSkipA == false) {
				insertFileCtl1(fileName, "A", dataCntA);
			}
			break;
		case "C":
			if (isSkipC == false) {
				insertFileCtl1(fileName, "C", dataCntC);
			}
			break;
		}
		
//		insertFileCtl1(fileName);
		
		if (ifAllModNoDone()) {
			renameFile(fileName);
			String filePath = fileFolderPath + fileName.substring(0,19) + "C" + fileName.substring(18);
			filePath = SecurityUtil.verifyPath(filePath);
			File file = new File(filePath);
			if (file.exists()) {
				renameFile(fileName.substring(0,19) + "C" + fileName.substring(18));
			}
		}

		return 0;
	}
	
	/***********************************************************************/

	private boolean ifAllModNoDone() throws Exception {
		sqlCmd = "select CHECK_CODE ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
		sqlCmd += " AND CHECK_CODE in ('A', 'C') ";
        setString(1, getFileName);
        int recordCnt = selectTable();
        return recordCnt == 2;
	}

	/***********************************************************************/
    private void moveData(Map<String, Object> map, String text) throws Exception {       
        String tmpChar1 = "";       
       
        tmpModNo = (String) map.get("col1"); //異動碼
        tmpModNo = tmpModNo.trim();
        
        // Justin 
        if ( ("A".equals(tmpModNo) && isSkipA) || ("C".equals(tmpModNo) && isSkipC) ) {
			return;
		}
        
        tmpCardUnit = (String) map.get("col2"); //發卡單位代號
        tmpCardUnit = tmpCardUnit.trim();
       
        tmpIdnoCode = (String) map.get("col3"); //客戶代號
        if (tmpIdnoCode.indexOf("*") != -1) {
        	showLogMessage("I", "", "id_no = [" + tmpIdnoCode + "]，Error : 客戶代號不可為*");
            errCode = "5";
            createErrReport();     
            totalOutputFile ++;
            return;
		}
        
        /** 處理資料長度異常 **/
        if (text.getBytes("MS950").length > DATA_LENGTH) {
        	showLogMessage("I", "", "id_no = [" + tmpIdnoCode + "]，Error : " + "資料長度[" + text.getBytes("MS950").length + "]超過" + DATA_LENGTH);
            errCode = "6";
            createErrReport();     
            totalOutputFile ++;
            return;
        }

        tmpChar1 = tmpIdnoCode.substring(10); //延伸碼
        idnoCode = tmpIdnoCode.substring(0, 10);
        if (!tmpChar1.equals(" ") && !tmpChar1.equals("R")) {
            showLogMessage("I", "", "id_no = [" + idnoCode + "]，Error : 客戶代號第11碼延伸碼不為空白 或'R'");
            errCode = "3";
            createErrReport();     
            totalOutputFile ++;
            return;
        }
       
        tmpCreditLimit = Double.parseDouble(((String) map.get("credit_limit")).trim());
       
        tmpEngName = (String) map.get("eng_name");
        tmpEngName = tmpEngName.substring(0, 25);
        tmpEngName = tmpEngName.trim();
       
        tmpSex = (String) map.get("sex");
        tmpSex = tmpSex.trim();
        if(tmpSex.equals("M") ){
            idnoSex = "1";
        }
        else{
            idnoSex = "2";
        }
       
        tmpBirthday = (String) map.get("birthday");
        tmpBirthday = tmpBirthday.trim();
       
        tmpChiName = (String) map.get("chi_name");
        tmpChiName = tmpChiName.length() > 50 ? tmpChiName.substring(0, 50) : tmpChiName; // Justin: prevent from outOfIndex
        tmpChiName = tmpChiName.trim();
       
        tmpIdnoRank = (String) map.get("col9"); //客戶等級碼
        tmpIdnoRank = tmpIdnoRank.trim();
       
        tmpHomeTel = (String) map.get("home_tel");
        tmpHomeTel = tmpHomeTel.trim();
        String[] hometelArr = comm.getTelZoneAndNo(tmpHomeTel);
        if (hometelArr.length == 3) {
        	homeAreaCode = hometelArr[0];
        	homeTelNo = hometelArr[1];     
		}else {
			homeAreaCode = "";
        	homeTelNo = "";     
		}
        
        tmpOfficeTel = (String) map.get("office_tel");
        tmpOfficeTel = tmpOfficeTel.trim();
        String[] officeTelArr = comm.getTelZoneAndNo(tmpOfficeTel); 
        if (officeTelArr.length == 3) {
        	officeAreaCode = officeTelArr[0];
        	officeTelNo = officeTelArr[1];
		}else {
        	officeAreaCode = "";
        	officeTelNo = "";
		}          
       
        tmpCellarPhone = (String) map.get("cellar_phone");
        tmpCellarPhone = tmpCellarPhone.substring(0, 15);
        tmpCellarPhone = tmpCellarPhone.trim();
       
        tmpErrCode = (String) map.get("col13"); //錯誤回覆碼
        tmpErrCode = tmpErrCode.trim();
       
        showLogMessage("I", "", "id_no = [" + idnoCode + "], mod_no = [" + tmpModNo + "]");
       
        // 如果使用者所輸入的modNoArg是空的 OR modNoArg等於tmpModNo
        // 則使用tmpModNo判斷要做新增還是刪除的動作
        if (modNoArg.isEmpty() || "A".equals(modNoArg) || modNoArg.equals(tmpModNo)) {
            switch (tmpModNo) {
            case "A":
            	dataCntA ++;
                getIdPSeqno();
                insertCrdIdno();
                break;
            case "C":
            	dataCntC ++;
            	
            	if ("A".equals(modNoArg)) {
            		if (isIdnoInCrdIdno(idnoCode)) {
						// write to a report 
            			outputIdnoList.add(text);
					}else {
						// 若異動碼為C但CRD_IDNO找不到資料時，改成走跟異動碼為A一樣的邏輯
                    	getIdPSeqno();
                        insertCrdIdno();
					}
				}else {
					// 若該ID底下有ACCT_TYPE = “03”的活卡且無ACCT_TYPE = “01”跟”06”且GROUP_CODE為”1599”的活卡，
	            	// 就不UPDATE CRD_IDNO，直接做下一筆
	            	if (ifUpdateCrdIdno()) {
	            		boolean rtn = updateCrdIdno();
	                    // 若異動碼為C但UPDATE CRD_IDNO找不到資料時，改成走跟異動碼為A一樣的邏輯
	                    if (rtn == false) {
	                    	tmpModNo = "A";
	                    	getIdPSeqno();
	                        insertCrdIdno();
	        			}
					}
	                //2022.01.06 ICU的額度可能包含臨調額度，故不可UPDATE，改由亞洲致遠提供的檔案處理
//	                if (rtn) {            	
//	                	updateActAcno();
//	    			}
				}
            	
            	
                break;
            }
		}
        commitDataBase();
        return;
    }

	/***********************************************************************/

	private boolean ifUpdateCrdIdno() throws Exception {
		if (ifUpdateCrdIdnoStr == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ( ( ");
			sb.append("	SELECT 1 ");
			sb.append("	FROM DUAL  ");
			sb.append("	WHERE EXISTS ( ");
			sb.append("		SELECT 1  ");
			sb.append("		FROM CRD_CARD cc  ");
			sb.append("		WHERE ID_P_SEQNO = ( SELECT ID_P_SEQNO FROM crd_idno WHERE ID_NO = ? ) ");
			sb.append("		AND cc.CURRENT_CODE = '0' ");
			sb.append("		AND cc.ACCT_TYPE = '03' ");
			sb.append("	) ) ");
			sb.append("	+ ");
			sb.append(" (SELECT 1 ");
			sb.append("	FROM DUAL  ");
			sb.append("	WHERE NOT EXISTS ( ");
			sb.append("		SELECT 1  ");
			sb.append("		FROM CRD_CARD cc  ");
			sb.append("		WHERE ID_P_SEQNO = ( SELECT ID_P_SEQNO FROM crd_idno WHERE ID_NO = ? ) ");
			sb.append("		AND cc.CURRENT_CODE = '0' ");
			sb.append("		AND ( cc.ACCT_TYPE = '01' OR ( cc.ACCT_TYPE = '06' AND cc.GROUP_CODE = '1599' ) )  ");
			sb.append("	) ) ");
			sb.append(") as ans ");
			sb.append("FROM dual  ");
			ifUpdateCrdIdnoStr = sb.toString();
		}
		
		sqlCmd = ifUpdateCrdIdnoStr;
		setString(1, idnoCode);
        setString(2, idnoCode);
		int recordCnt = selectTable();

		if (recordCnt > 0 && getValueInt("ans") == 2)
			return false;
		else
			return true;
	}

	/***********************************************************************/
	int outPutTextFile(String fileName) throws Exception {
		int fileNo = 0;

        sqlCmd  = "select max(substr(file_name, 22, 2)) file_no";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += " where file_name like ?";
        sqlCmd += "  and crt_date  = ? ";
        setString(1, "ICCUSQND.ERR." + "%" + ".TXT");
        setString(2, sysDate);

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");

		hNcccFilename = String.format("ICCUSQND.ERR.%s%02d.TXT", sysDate, fileNo + 1);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%serror/%s", fileFolderPath, hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		isSkipA = false;
		isSkipC = false;

		sqlCmd = "select CHECK_CODE ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
//      sqlCmd += " and crt_date = to_char(?,'yyyymmdd') ";
        setString(1, getFileName);
//      setString(2, queryDate1);
        if (modNoArg.isEmpty()) {
        	sqlCmd += " AND CHECK_CODE in ('A', 'C') ";
        	int recordCnt = selectTable();
        	switch(recordCnt) {
        	case 2:	
            	showLogMessage("I", "", String.format("此檔案 = [%s]已處理過「異動碼A」和「異動碼C」，不可重複處理(crd_file_ctl)", getFileName));
    			return (1);
        	case 1:	
        		if ("A".equals(getValue("CHECK_CODE"))) {
					isSkipA = true;
					showLogMessage("I", "", String.format("此檔案 = [%s]已處理過「異動碼A」，此次執行只處理「異動碼C」(crd_file_ctl)", getFileName));
				}else {
					isSkipC = true;
					showLogMessage("I", "", String.format("此檔案 = [%s]已處理過「異動碼C」，此次執行只處理「異動碼A」(crd_file_ctl)", getFileName));
				}
    			return (0);
        	case 0:	
        		showLogMessage("I", "", String.format("此檔案 = [%s]未處理過，此次執行處理「異動碼A」和「異動碼C」(crd_file_ctl)", getFileName));
    			return (0);
    		default:
        		showLogMessage("I", "", String.format("此檔案 = [%s]已處理過[%d]次，使用者輸入異動碼[%s]，此為異常情況(crd_file_ctl)", getFileName, recordCnt, modNoArg));
    			return (1);
        	}
		}else {
			sqlCmd += " AND CHECK_CODE = ? ";
			setString(2, modNoArg);
			int recordCnt = selectTable();
        	switch(recordCnt) {
        	case 1:	
            	showLogMessage("I", "", String.format("此檔案 = [%s]已處理過「異動碼%s」不可重複處理(crd_file_ctl)", getFileName, modNoArg));
    			return (1);
        	case 0:	
        		showLogMessage("I", "", String.format("此檔案 = [%s]未處理過，此次執行處理「異動碼%s」(crd_file_ctl)", getFileName, modNoArg));
    			return (0);
        	default:
        		showLogMessage("I", "", String.format("此檔案 = [%s]已處理過[%d]次，使用者輸入異動碼[%s]，此為異常情況(crd_file_ctl)", getFileName, recordCnt, modNoArg));
    			return (1);
        	}
        	
		}

	}

	/***********************************************************************/
	void createErrReport() throws Exception {
		totalOutputFileEachFile++;

		ncccData1 = new Buf1();

		seq++;
		ncccData1.modNo = tmpModNo;
		ncccData1.idnoId = tmpIdnoCode;

		switch (errCode) {
		case "1":
			ncccData1.errReason = String.format("%-200s", "異動碼為'C'，但主檔無資料");
			break;
		case "2":
			ncccData1.errReason = String.format("%-200s", "異動碼為'A'，但資料已存在主檔");
			break;
		case "3":
			ncccData1.errReason = String.format("%-200s", "客戶代號第11碼延伸碼不為空白或'R'");
			break;
		case "4":
			ncccData1.errReason = String.format("%-200s", "異動碼為'C'，但帳戶檔無資料");
			break;
		case "5":
			ncccData1.errReason = String.format("%-200s", "客戶代號不可為*");
			break;
		case "6":
			ncccData1.errReason = String.format("%-200s", "資料長度超過" + DATA_LENGTH);
			break;
		case "7":
			ncccData1.errReason = String.format("%-200s", "寫入卡人資料檔重複");
			break;
		}

		ncccData1.date = sysDate;

		buf = ncccData1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}
	
	/***********************************************************************/
	private boolean isIdnoInCrdIdno(String idnoCode) throws Exception {
		sqlCmd = "select id_no ";
		sqlCmd += "from crd_idno ";
		sqlCmd += "where id_no = ? ";
		setString(1, idnoCode);
		return selectTable() > 0;
	}

	/***********************************************************************/
	/**
	* @ClassName: IcuD001
	* @Description: insertCrdIdno 寫入卡人資料檔重複不當掉
	* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
	* @Company: DXC Team.
	* @author Wilson
	* @version V1.00.42, Oct 05, 2022
	*/
	void insertCrdIdno() throws Exception {

		if (isIdnoInCrdIdno(idnoCode)) {
			showLogMessage("I", "", "id_no = [" + idnoCode + "]，Error:異動碼為“A”，但資料已存在主檔");
			errCode = "2";
			createErrReport();
			totalOutputFile++;
			rollbackDataBase();
			return;
		}
		commitDataBase();

		setValue("id_p_seqno", idnoIdPSeqno);
		setValue("id_no", idnoCode);
		setValue("id_no_code", "0");
        setValueDouble("idno_credit_limit", tmpCreditLimit);
        setValue("eng_name", tmpEngName);
        setValue("sex", idnoSex);
		setValue("birthday", tmpBirthday);
		setValue("chi_name", tmpChiName.trim());
		setValue("home_area_code1", homeAreaCode);
		setValue("home_tel_no1", homeTelNo);
		setValue("office_area_code1", officeAreaCode);
		setValue("office_tel_no1", officeTelNo);
		setValue("cellar_phone", tmpCellarPhone);
		setValue("msg_flag", "Y");
		setValue("crt_date", sysDate);
        setValue("crt_user", prgmId);
        setValue("apr_date", sysDate);
        setValue("apr_user", prgmId);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        setValue("mod_user", prgmId);
        setValueInt("MSG_PURCHASE_AMT", 0); //  INSERT CRD_IDNO時增加MSG_PURCHASE_AMT(固定給0)

		daoTable = "crd_idno";

		insertTable();

		if (dupRecord.equals("Y")) {
//			comcr.errRtn("insert_crd_idno error[duplicate]"," ID_NO = " + idnoCode + ",ID_P_SEQNO = " + idnoIdPSeqno, "");
			showLogMessage("I", "", "id_no = [" + idnoCode + "],id_p_seqno = [" + idnoIdPSeqno + "]，Error:寫入卡人資料檔重複");
			errCode = "7";
		    createErrReport();
		    totalOutputFile++;
		    rollbackDataBase();
		    return;			
		}
		return;
	}

	/***********************************************************************/
	void getIdPSeqno() throws Exception {

		sqlCmd = " select id_p_seqno ";
		sqlCmd += " from crd_idno_seqno ";
		sqlCmd += " where id_no = ? ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, idnoCode);
		if (selectTable() > 0)
			idnoIdPSeqno = getValue("id_p_seqno");
		else {
			sqlCmd = " select substr(to_char(ecs_acno.nextval,'0000000000'), 2,10) as temp_x10 ";
			sqlCmd += " from dual ";
			if (selectTable() > 0) {
				idnoIdPSeqno = getValue("temp_x10");
				insertCrdIdnoSeqno();
			} else
				comcr.errRtn("select_ecs_acno error[notFind]", "", hCallBatchSeqno);
		}
		showLogMessage("I", "", "id_no = [" + idnoCode + "]，id_p_seqno = [" + idnoIdPSeqno + "]");

		return;
	}

	/***********************************************************************/
	void insertCrdIdnoSeqno() throws Exception {
//		idFlag++;
//		billApplyFlag++;
		setValue("id_no", idnoCode);
		setValue("id_p_seqno", idnoIdPSeqno);
    	setValue("id_flag", "");		
    	setValue("bill_apply_flag", "");
    	setValue("debit_idno_flag", "N");
		daoTable = "crd_idno_seqno";
		insertTable();
	}

	/***********************************************************************/
	boolean updateCrdIdno() throws Exception {
		boolean isSupFlag = getSubFlag(idnoCode);
		
		daoTable = "crd_idno";
		updateSQL =  " idno_credit_limit = ?,";
		updateSQL += " eng_name = ?,";
		updateSQL += " sex = ?,";
		updateSQL += " birthday = ?,";
		updateSQL += " chi_name = ?,";
		updateSQL += " home_area_code1  = ?,";
		updateSQL += " home_tel_no1 = ?,";
		updateSQL += " office_area_code1 = ?,";
		updateSQL += " office_tel_no1 = ?,";
		if(!isSupFlag) 
			updateSQL += " cellar_phone  = ?,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
        updateSQL += " mod_user  = ? ";
        whereStr = " where id_no = ? "; 
        
        int ii = 1;
        setDouble(ii++, tmpCreditLimit);
        setString(ii++, tmpEngName);
        setString(ii++, idnoSex);
        setString(ii++, tmpBirthday);
        setString(ii++, tmpChiName);
        setString(ii++, homeAreaCode);
        setString(ii++, homeTelNo);
        setString(ii++, officeAreaCode);
        setString(ii++, officeTelNo);
        if(!isSupFlag) 
        	setString(ii++, tmpCellarPhone);
        setString(ii++,prgmId);
        setString(ii++, sysDate + sysTime);
        setString(ii++,prgmId);
        setString(ii++, idnoCode);
        
        showLogMessage("D", "", updateSQL);
        showLogMessage("D", "", String.format("[%f]", tmpCreditLimit));
        showLogMessage("D", "", String.format("[%s]", tmpEngName));
        showLogMessage("D", "", String.format("[%s]", idnoSex));
        showLogMessage("D", "", String.format("[%s]", tmpBirthday));
        showLogMessage("D", "", String.format("[%s]", tmpChiName));
        showLogMessage("D", "", String.format("[%s]", homeAreaCode));
        showLogMessage("D", "", String.format("[%s]", homeTelNo));
        showLogMessage("D", "", String.format("[%s]", officeAreaCode));
        showLogMessage("D", "", String.format("[%s]", officeTelNo));
        if(!isSupFlag)
        	showLogMessage("D", "", String.format("[%s]", tmpCellarPhone));
        showLogMessage("D", "", String.format("[%s]", prgmId));
        showLogMessage("D", "", String.format("[%s]", sysDate + sysTime));
        showLogMessage("D", "", String.format("[%s]", idnoCode));

		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "id_no = [" + idnoCode + "]，異動碼為'C',但主檔無資料，改以新增處理");
			// 若異動碼為C但UPDATE CRD_IDNO找不到資料時，改成走跟異動碼為A一樣的邏輯
//			errCode = "1";
//			createErrReport();
//			totalOutputFile++;
			rollbackDataBase();
			return false;
		}

		return true;
	}
	
	/**
	 * @throws Exception *********************************************************************/
	
	boolean getSubFlag(String idnoCode) throws Exception {
		int supFlag1 = 0;
		int supFlag0 = 0;
		
		sqlCmd = " SELECT COUNT(DECODE(SUP_FLAG,'1','Y')) AS SUP_FLAG_1, ";
		sqlCmd += " COUNT(DECODE(SUP_FLAG,'0','Y')) AS SUP_FLAG_0 ";
		sqlCmd += " from CRD_CARD ";
		sqlCmd += " where ID_P_SEQNO = UF_IDNO_PSEQNO(?) ";
		sqlCmd += " GROUP BY ID_P_SEQNO ";
		setString(1, idnoCode);
		if (selectTable() > 0) {
			supFlag1 = getValueInt("SUP_FLAG_1");
			supFlag0 = getValueInt("SUP_FLAG_0");
		}
		showLogMessage("I", "", "id_no = [" + idnoCode + "] , SUP_FLAG_1 = [" + supFlag1 + "] , SUP_FLAG_0 = [" + supFlag0 + "]");
		if(supFlag1 > 0 && supFlag0 == 0) {
			return true;
		}
		
		return false;
	}

	/***********************************************************************/
//	void updateActAcno() throws Exception {
//
//		daoTable = "act_acno";
//		updateSQL += " line_of_credit_amt  = ?,";
//        updateSQL += " mod_pgm  = ? ,";
//        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
//        whereStr = " where acct_type = '01' " + " and acct_key = ? ";      
//        setDouble(1, tmpCreditLimit);
//        setString(2, prgmId);
//        setString(3, sysDate + sysTime);
//        setString(4, tmpIdnoCode.substring(0, 10) + 0);
//
//		updateTable();
//
//		if (notFound.equals("Y")) {
//			showLogMessage("I", "", "acct_key = [" + tmpIdnoCode.substring(0, 10) + 0 + "]，Error : 異動碼為'C'，但帳戶檔無資料");
//			errCode = "4";
//			createErrReport();
//			totalOutputFile++;
//			rollbackDataBase();
//		}
//
//		return;
//	}
//
	/***********************************************************************/
	void insertFileCtl1(String fileName, String modNo, int cnt) throws Exception {

		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", cnt);
		setValueInt("record_cnt", cnt);
		setValue("trans_in_date", sysDate);
		setValue("CHECK_CODE", modNo);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("fileName[%s], modNo[%s]已存在crd_file_ctl", fileName, modNo));
		}
		commitDataBase();
	}

	/***********************************************************************/
	void insertFileCtl() throws Exception {
		setValue("file_name", hNcccFilename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", totalOutputFile);
		setValueInt("record_cnt", totalOutputFile);
		setValue("trans_in_date", sysDate);
		setValue("CHECK_CODE", "");
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " CHECK_CODE = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, totalOutputFile);
			setInt(2, totalOutputFile);
			setString(3, "");
			setString(4, hNcccFilename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
		commitDataBase();
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + hNcccFilename + " 開始傳送....");
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + hNcccFilename);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + hNcccFilename + " 資料"+" errcode:"+err_code);
	          insertEcsNotifyLog(hNcccFilename);          
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
		String tmpstr1 = comc.getECSHOME() + "/media/icu/error/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		IcuD001 proc = new IcuD001();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String modNo;
		String idnoId;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(modNo, 1);
			rtn += fixLeft(idnoId, 11);
			rtn += fixLeft(errReason, 200);
			rtn += fixLeft(date, 8);
			return rtn;
		}

		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = str + spc;
			byte[] bytes = str.getBytes("MS950");
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, 0, vResult, 0, len);

			return new String(vResult, "MS950");
		}
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData1.modNo = comc.subMS950String(bytes, 0, 1);
		ncccData1.idnoId = comc.subMS950String(bytes, 1, 12);
		ncccData1.errReason = comc.subMS950String(bytes, 12, 212);
		ncccData1.date = comc.subMS950String(bytes, 212, 220);
	}

	/****************************************************************************/
	String fixAllLeft(String str, int len) throws UnsupportedEncodingException {
		String spc = "";
		for (int i = 0; i < 100; i++)
			spc += "　";
		if (str == null)
			str = "";
		str = str + spc;
		byte[] bytes = str.getBytes("MS950");
		byte[] vResult = new byte[len];
		System.arraycopy(bytes, 0, vResult, 0, len);
		return new String(vResult, "MS950");
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = fileFolderPath + removeFileName;
		String tmpstr2 = fileFolderPath +"backup/" + removeFileName + "." + sysDate;

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
