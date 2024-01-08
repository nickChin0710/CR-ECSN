/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  109/06/09  V1.01.01    Rou         Initial                               *
*  109/07/03  V1.01.02  Wilson      MBBBBBBBB -> M00600000                  *
*  109-07-03  V1.01.03  yanghan       修改了變量名稱和方法名稱                                                            *                                              
*  109/07/03  V1.01.03    JustinWu                  change the program name *
*  109-07-22  V1.01.04  yanghan     修改了字段名称                                                                                    * 
*  109-07-31  V1.01.05  Wilson      測試修改                                                                                               * 
*  109/08/14  V1.01.06  Wilson      資料夾名稱修改為小寫                                                                         *
*  109/08/28  V1.01.07  Wilson      讀檔規則修改                                                                                        *
*  109/09/04  V1.01.08  Wilson      換行符號 -> "\r\n"(0D0A)                   *
*  109/09/09  V1.01.09  Wilson      新增產生ICPSMQNA屬性檔                                                              *
*  109/09/14  V1.01.10  Wilson      新增procFTP                              *
*  109/09/22  V1.01.11  Wilson      update dbc_idno新增birthday              *
*  109/09/28  V1.01.12  Wilson      無檔案不秀error、讀檔不綁檔名日期                                            *
*  109/09/30  V1.01.13  Wilson     無檔案秀error                              *
*  109/10/06  V1.01.14  Wilson     讀檔要綁檔名日期                                                                                  *
*  109/10/12  V1.01.15  Wilson     檔名日期改營業日                                                                                  *
*  109/10/14  V1.01.16  Wilson     LOCAL_FTP_PUT -> NCR2TCB                 *
*  109/10/16  V1.01.17  Wilson     錯誤報表FTP                                 *
*  109-10-19  V1.00.18  shiyuqi    updated for project coding standard     *
*  109/11/09  V1.00.19  Wilson     NCR2TCB -> FISC_FTP_PUT                  *
*  109-12-18  V1.00.20  Justin       add a new condition when the mod_no equals to C
*  109/12/25  V1.00.21  Wilson     無檔案正常結束                                                                                        *
*  109/12/30  V1.00.22  Justin       ++ the function of changing id
*  110/02/01  V1.00.23  Wilson     變更檔名並新增TEMPFILE檔                                                            *
*  110/02/02  V1.00.24  Wilson     mark產檔(CrdF071已經有做)                                                         *
*  110/03/03  V1.00.25  Justin       prevent from causing duplication when updating id_no
*  110/03/04  V1.00.26  Justin       add error reasons
*  110/12/02  V1.00.27  Justin       add commit and rollback when writing error reports *
*  110/12/08  V1.00.28  Wilson       錯誤訊息調整                                                                                          *
*  111/02/14  V1.01.29  Ryan         big5 to MS950                                           *
****************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException;
import com.CommFTP;

/*CARDLINK手機異動資料檔*/
public class IcuD017 extends AccessDAO {
	private static final String DBC_IDNO = "DBC_IDNO";
	private static final String CRD_IDNO = "CRD_IDNO";
	private final String progname = "CARDLINK手機異動資料檔  111/02/14  V1.00.29";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;	

	String prgmId = "IcuD017";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar3 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar4 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;

	protected final String dT1Str = "card_no, activate_date, col3, col4, col5, col6, col7, col8, col9, col10, col11, "
			+ "id_no, birthday, col14, col15, col16, col17, col18, col19, col20, col21, col22, col23, "
			+ "col24, cellar_phone, col26, col27, col28, col29, col30, col31, col32, col33, col34, col35, col36, "
			+ "col37, col38, col39, col40, col41, col42, col43, col44, col45, col46, col47, col48, col49, "
			+ "mod_no, col51, col52, col53, col54, col55, col56 ";

	protected final int[] dt1Length = { 19, 8, 14, 14, 14, 8, 14, 14, 12, 19, 40, 20, 8, 40, 40, 40, 40, 40, 30, 30, 3,
			16, 20, 20, 20, 40, 30, 2, 2, 8, 8, 8, 8, 8, 8, 8, 2, 3, 1, 1, 1, 1, 4, 1, 14, 14, 14, 8, 6, 1, 1, 30, 4, 8,
			7, 4 };

	int rptSeq1 = 0;
	String buf = "";
	String buf4 = "";
	String buf5 = "";
	String queryDate = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	int hRecCnt1 = 0;
	int seq = 0;

	String getFileName;
	String getFileName1;
	String outFileName;
	String hSystemDate = "";
	String filename = "";
	String filename1 = "";
	String filename2 = "";
	String temstr = "";
	String temstr1 = "";
	String temstr2 = "";
	int totalInputFile;
	int totalOutputFile;
	int hNn = 0;

	String tmpCardNo;
	String tmpActivateDate;
	String tmpcol2;
	String tmpcol3;
	String tmpcol4;
	String tmpcol5;
	String tmpcol6;
	String tmpcol7;
	String tmpcol8;
	String tmpcol9;
	String tmpcol10;
	String tmpcol11;
	String tmpIdnoCode;
	String tmpcol13;
	String tmpcol14;
	String tmpcol15;
	String tmpcol16;
	String tmpcol17;
	String tmpcol18;
	String tmpcol19;
	String tmpcol20;
	String tmpcol21;
	String tmpcol22;
	String tmpcol23;
	String tmpcol24;
	String tmpCellarPhone;
	String tmpcol26;
	String tmpIdNo;
	String tmpcol28;
	String tmpcol29;
	String tmpcol30;
	String tmpcol31;
	String tmpcol32;
	String tmpcol33;
	String tmpcol34;
	String tmpcol35;
	String tmpcol36;
	String tmpcol37;
	String tmpcol38;
	String tmpcol39;
	String tmpcol40;
	String tmpcol41;
	String tmpcol42;
	String tmpcol43;
	String tmpcol44;
	String tmpcol45;
	String tmpcol46;
	String tmpcol47;
	String tmpcol48;
	String tmpcol49;
	String tmpModNo;
	String tmpcol51;
	String tmpcol52;
	String tmpcol53;
	String tmpcol54;
	String tmpcol55;
	String tmpBirthDay;

	ErrorReason errCode;
	String cardType;
	String outErrFileName;
	String tatolInputData;
	String errFileName;
	String hBusiBusinessDate = "";
	
	protected String[] dT1 = new String[] {};
	Buf1 ncccData1 = new Buf1();
	Buf2 icuData = new Buf2();
	Buf3 tempData = new Buf3();

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
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());

			hModUser = comc.commGetUserID();
			
			/*** 取得系統日期 ***/
			commonRtn();
			
			// 若沒有給定查詢日期，則查詢日期為系統日
            if(args.length == 0) {
                queryDate = hBusiBusinessDate;
            }else
            if(args.length == 1) {
                if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
                    showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
                    return -1;
                }
                queryDate = args[0];
            }else {
                comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
            }           
			
			openFile();
			
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束,[" + totalInputFile + "],[" + totalOutputFile + "]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
    void commonRtn() throws Exception {
    	hBusiBusinessDate = "";
    	
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date, ";
        sqlCmd += " business_date ";
        sqlCmd += " from ptr_businday ";
        int tmpInt = selectTable();
        if (tmpInt > 0) {
            hSystemDate = getValue("h_system_date");
            hBusiBusinessDate = getValue("business_date");
        }
    }
    
    /***************************************************************************/    
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());

		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "M00600000", "ICPSMQND",
				new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式
		
//		final String fileNameTemplate = String.format("%s\\.%s\\..*", "M00600000", "ICPSMQND"); // 檔案正規表達式

		for (String file : listOfFiles) {
			getFileName = file;
//			getFileName1 = String.format("%s.ORG%s",getFileName.substring(0,18),getFileName.substring(18));			
//			showLogMessage("I", "", " FileName =[" + getFileName + "]");
//			showLogMessage("I", "", " FileName1 =[" + getFileName1 + "]");
//			if (getFileName.length() != 27)
//				continue;
//			if (!getFileName.substring(0, 19).equals("M00600000.ICPSMQND."))
//				continue;
			if( getFileName.matches(".*\\.bak"))
				continue;
			if( ! getFileName.matches(fileNameTemplate))
				continue;
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
			
//		    procFTP();
//		    renameFile1(filename);
//		    renameFile1(filename1);
//		    renameFile3(filename2);
		}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理,處理日期 = " + queryDate);
			
//			comcr.hCallErrorDesc = "無檔案可處理";
//			comcr.errRtn("無檔案可處理", "處理日期 = " + queryDate , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = comc.getECSHOME() + "/media/icu/" + fileName;

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

		showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/icu ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
			
			totalInputFile++;
			Map<String, Object> dataMap = processDataRecord(getFieldValue(rec, dt1Length), dT1);
			moveData(dataMap);
			
			tmpIdNo = (String) dataMap.get("id_no"); // 客戶代號
			String extendCode = tmpIdNo.substring(10, 11); // 延伸碼
			tmpIdNo = tmpIdNo.substring(0, 10);
			String idPSeqnoSelectedByTxtCardNo =  (String)dataMap.get("idPSeqnoSelectedByTxtCardNo");
			
			if (checkExtendCode(extendCode)) {
				
				switch (cardType) {
				
				case "VD":
					System.out.println(String.format("檢查碼為:%s", (String) dataMap.get("mod_no")));
					// check 異動碼是否為C
					if ( "C".equalsIgnoreCase( (String)dataMap.get("mod_no") ) ) {	
						String idNoFromFile = tmpIdNo;
						DbcDataD017 dbaData = getDbcData(tmpCardNo);
						if (dbaData != null) {
							if ( isIdNoChg(dbaData.idNo, idNoFromFile) ) {
								log(String.format("Update id_no. [id_p_seqno = %s]", dbaData.idPSeqno));
								boolean isupdateOk = updateIdNo(idNoFromFile, dbaData);
								if (isupdateOk == false) continue;
							}
						}
					}
					if (isDbcIdnoExist()) {
						if (checkIdno(DBC_IDNO, tmpIdNo, idPSeqnoSelectedByTxtCardNo) == false) {
							rollbackDataBase();
							continue;
						}
						updateDbcIdno();
					}
					break;
					
				default:
					if (isCrdIdnoExist()) {
						if (checkIdno(CRD_IDNO, tmpIdNo, idPSeqnoSelectedByTxtCardNo) == false) {
							continue;
						}
						updateCrdIdno();
					}
					break;
					
				}
			}	
			commitDataBase();
			processDisplay(1000);
			
		}

		if (totalOutputFile > 0) {
			outPutTextFile();
			comc.writeReport(outErrFileName, lpar1, "MS950");
			insertFileCtl();
			lpar1.clear();
			
			procFTP1();
		    renameFile2(errFileName);
		}

		closeInputText(fi);

		insertFileCtl1(getFileName);

		renameFile(fileName);

		// 將開卡日期塞回原本的檔案
//		outPutFile();
//		comc.writeReport(temstr, lpar2, "big5");
//		lpar2.clear();
//		
//		insertFileCtl1(filename);
//		
//		createPsmqa();
//		comc.writeReport(temstr1, lpar3, "big5");
//		lpar3.clear();
//		
//		insertFileCtl1(filename1);
//
//		createTempfile();
//		comc.writeReport(temstr2, lpar4, "big5");
//		lpar4.clear();
//		
//		insertFileCtl1(filename2);
		
		return 0;
	}

	private boolean checkIdno(String idNoTable, String txtIdNo, String idPSeqnoSelectedByTxtCardNo) throws Exception {
		boolean isVDCard = false;
		if (DBC_IDNO.equalsIgnoreCase(idNoTable)) {
			idNoTable = DBC_IDNO;
			isVDCard = true;
		}else {
			idNoTable = CRD_IDNO;
		}
		
		sqlCmd =    "select id_no ";
		sqlCmd += String.format(" from %s ", idNoTable);
		sqlCmd += "where id_p_seqno = ? ";
		setString(1, idPSeqnoSelectedByTxtCardNo); 

		int cnt = selectTable();
		if (cnt == 0) {
			if (isVDCard) {
				showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號不存在VD卡人主檔");
				putErrReportList(ErrorReason.ERR12);
			}else {
				showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號不存在卡人主檔");
				putErrReportList(ErrorReason.ERR11);
			}		
			return false;
		}

		String idNo = getValue("id_no");
		if (txtIdNo.equalsIgnoreCase(idNo) == false) {
			if (isVDCard) {
				showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號與VD卡人檔不一致");
				putErrReportList(ErrorReason.ERR10);
			}else {
				showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號與卡人檔不一致");
				putErrReportList(ErrorReason.ERR9);
			}		
			
			return false;
		}

		return true;
	}

	private boolean checkExtendCode(String extendCode) throws Exception {
		showLogMessage("I", "", "id_no = [" + tmpIdNo + extendCode + "]");

		if (!extendCode.equals(" ") && !extendCode.equals("R")) {
			showLogMessage("I", "", "Error : 客戶代號第11碼延伸碼不為空白 或'R'");
			putErrReportList(ErrorReason.ERR1);
			return false;
		}
		return true;
		
	}

	private void putErrReportList(ErrorReason errorCode) throws Exception {
		this.errCode = errorCode;
		createErrReport();
		totalOutputFile++;
	}

	/***********************************************************************/
	private void moveData(Map<String, Object> map) throws Exception {
		String idPSeqnoSelectedByTxtCardNo = "";
		String tmpStr;
		boolean isCard;
		String newLine = "\r";

		tmpCardNo = (String) map.get("card_no");
		tmpCardNo = tmpCardNo.trim();
		showLogMessage("I", "", "card_no = [" + tmpCardNo + "]");
		// 2020/12/18 Justin revised the misunderstanding function name and extract some functions
		idPSeqnoSelectedByTxtCardNo = getIdPSeqnoFromCard();
		isCard = idPSeqnoSelectedByTxtCardNo != null && idPSeqnoSelectedByTxtCardNo.trim().length() != 0;
		map.put("idPSeqnoSelectedByTxtCardNo", idPSeqnoSelectedByTxtCardNo);

		if (tmpActivateDate.isEmpty())
			tmpActivateDate = String.format("%-8s", " ");
		showLogMessage("I", "", "開卡日期 = [" + tmpActivateDate + "]");

		if (cardType.equals("CC")) // 若為信用卡，取開卡日期
			tmpStr = tmpActivateDate;
		else
			tmpStr = (String) map.get("activate_date");

		tatolInputData = (String) map.get("card_no") + tmpStr + (String) map.get("col3") + (String) map.get("col4")
				+ (String) map.get("col5") + (String) map.get("col6") + (String) map.get("col7")
				+ (String) map.get("col8") + (String) map.get("col9") + (String) map.get("col10")
				+ (String) map.get("col11") + (String) map.get("id_no") + (String) map.get("birthday")
				+ (String) map.get("col14") + (String) map.get("col15") + (String) map.get("col16")
				+ (String) map.get("col17") + (String) map.get("col18") + (String) map.get("col19")
				+ (String) map.get("col20") + (String) map.get("col21") + (String) map.get("col22")
				+ (String) map.get("col23") + (String) map.get("col24") + (String) map.get("cellar_phone")
				+ (String) map.get("col26") + (String) map.get("col27") + (String) map.get("col28")
				+ (String) map.get("col29") + (String) map.get("col30") + (String) map.get("col31")
				+ (String) map.get("col32") + (String) map.get("col33") + (String) map.get("col34")
				+ (String) map.get("col35") + (String) map.get("col36") + (String) map.get("col37")
				+ (String) map.get("col38") + (String) map.get("col39") + (String) map.get("col40")
				+ (String) map.get("col41") + (String) map.get("col42") + (String) map.get("col43")
				+ (String) map.get("col44") + (String) map.get("col45") + (String) map.get("col46")
				+ (String) map.get("col47") + (String) map.get("col48") + (String) map.get("col49")
				+ (String) map.get("mod_no") + (String) map.get("col51") + (String) map.get("col52")
				+ (String) map.get("col53") + (String) map.get("col54") + (String) map.get("col55")
				+ (String) map.get("col56") + newLine;

		lpar2.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", tatolInputData));

		// if the card is not exist, return
		if (isCard == false) return;

		tmpCellarPhone = (String) map.get("cellar_phone");
		tmpCellarPhone = tmpCellarPhone.trim();
		showLogMessage("I", "", "cellar_phone = [" + tmpCellarPhone + "]");
		
		tmpBirthDay = (String) map.get("birthday");
		tmpBirthDay = tmpBirthDay.trim();
		showLogMessage("I", "", "birthday = [" + tmpBirthDay + "]");

		return;
	}

	/**
	 * update or insert all tables in which there are columns related to id_no
	 * @param idNoFromFile
	 * @param dbaData
	 * @return
	 * @throws Exception
	 */
	private boolean updateIdNo(String idNoFromFile, DbcDataD017 dbaData) throws Exception {
		boolean sqlResult = false;
		
		sqlResult = updateDbcIdnoSetIdNo(idNoFromFile, dbaData.idPSeqno);
		if ( ! sqlResult) {
			rollbackDataBase();
			return false;
		}
		
		sqlResult = updateDbaAcnoSetIdNo(idNoFromFile, dbaData.idPSeqno);
		if ( ! sqlResult) {
			rollbackDataBase();
			return false;
		}
		
		sqlResult = insertDbcChgId(idNoFromFile, dbaData);
		if ( ! sqlResult) {
			rollbackDataBase();
			return false;
		}
		
		sqlResult = updateCrdEmployeeSetIdno(idNoFromFile, dbaData.idNo);
		if ( ! sqlResult) {
			rollbackDataBase();
			return false;
		}
		
		sqlResult = updateCrdIdnoSeqnoSetIdno(idNoFromFile, dbaData.idPSeqno);
		if ( ! sqlResult) {
			rollbackDataBase();
			return false;
		}

		return true;
		
	}

	private boolean updateCrdIdnoSeqnoSetIdno(String idNoFromFile, String idPSeqno) throws Exception {
		daoTable = "crd_idno_seqno";
		updateSQL =   " id_no  = ? ";
        whereStr   = " where id_p_seqno = ? ";      
        setString(1, idNoFromFile);
        setString(2, idPSeqno);
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "update CRD_IDNO_SEQNO not found! id_p_seqno= " + idPSeqno);;
//			return false; //沒更新也沒關係
		}

		return true;
		
	}

	private boolean updateCrdEmployeeSetIdno(String idNoFromFile, String idNoFromDB) throws Exception {
		daoTable = "crd_employee";
		updateSQL =   " id  = ? ";
        whereStr   = " where id = ? ";      
        setString(1, idNoFromFile);
        setString(2, idNoFromDB);
		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "update crd_employee not found! id = " + idNoFromDB);;
//			return false; //沒更新也沒關係
		}

		return true;
		
	}

	private boolean insertDbcChgId(String idNoFromFile, DbcDataD017 dbaData) throws Exception {
		setValue("acct_no", dbaData.acctNo);
		setValue("id", dbaData.idNo);
		setValue("id_code", dbaData.idNoCode);
		setValue("corp_flag", "N");
		setValue("aft_id", idNoFromFile);
		setValue("aft_id_code", "0");
		setValue("crt_date", sysDate);
		setValue("process_flag", "Y");
		setValue("mod_user", prgmId);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		daoTable = "dbc_chg_id";
		int insertCnt = insertTable();
		if (insertCnt <= 0 ) {
			comcr.errRtn("Error! insert DBC_CHG_ID error!", "", "");
		}
		
		return true;				
	}

	private boolean updateDbaAcnoSetIdNo(String idNoFromFile, String idPSeqno) throws Exception {
		daoTable = "dba_acno";
		updateSQL =   " acct_key  = ?,";
		updateSQL += " mod_user  = ? ,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS')  ";
        whereStr   = " where id_p_seqno = ? ";      
        setString(1, idNoFromFile + "0");
        setString(2, prgmId);
        setString(3, prgmId);
        setString(4, sysDate + sysTime);
        setString(5, idPSeqno);

		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "update DBA_ACNO[id_no] not found! id_p_seqno = " + idPSeqno);
			putErrReportList(ErrorReason.ERR8);
			return false;
		}

		return true;
		
	}

	private boolean updateDbcIdnoSetIdNo(String idNoFromFile, String idPSeqno) throws Exception {
		daoTable = DBC_IDNO;
		updateSQL = " id_no  = ?,";
		updateSQL += " mod_user  = ? ,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr   = " where id_p_seqno = ? ";      
        setString(1, idNoFromFile);
        setString(2, prgmId);
        setString(3, prgmId);
        setString(4, sysDate + sysTime);
        setString(5, idPSeqno);

        try {
        	updateTable();
        }catch (Exception e) {
			if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
				showLogMessage("I", "","update DBC_IDNO[id_no]: Duplicate! id_p_seqno = " + idPSeqno);
				putErrReportList(ErrorReason.ERR7);
				return false;
			}
			comcr.errRtn("Error! : update DBC_IDNO[id_no]: " + e.getMessage() + ". id_p_seqno = " + idPSeqno, "", "");
		}

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "update DBC_IDNO[id_no]: Not found! id_p_seqno = " + idPSeqno);
			putErrReportList(ErrorReason.ERR6);
			return false;
		}

		return true;
		
	}

	/**
	 * 
	 * @param idNoFromFile
	 * @param idNoFromDB
	 * @return
	 */
	private boolean isIdNoChg(String idNoFromDB, String idNoFromFile) {
		return ! idNoFromFile.trim().equals(idNoFromDB);
	}
	
	/**
	 * get data from dbc_crd and dbc_idno
	 * @return
	 * @throws Exception 
	 */
	private DbcDataD017 getDbcData(String cardNo) throws Exception {
		DbcDataD017 dbaData = new DbcDataD017();
		boolean selectResult = false;
		
		selectResult = selectDbcCard(cardNo);
		if (selectResult == false) {
			return null;
		}
		dbaData.idPSeqno = getValue("id_p_seqno");
		dbaData.acctNo = getValue("acct_no");
		
		
		selectResult = selectDbcIdno(dbaData.idPSeqno);
		if (selectResult == false) {
			return null;
		}
		dbaData.idNo = getValue("id_no");
		dbaData.idNoCode = getValue("id_no_code");
		
		return dbaData;
	}

	
	/***********************************************************************/
	int outPutTextFile() throws Exception {
		int fileNo = 0;

		sqlCmd = "select max(substr(file_name, 22, 2)) file_no";
		sqlCmd += " from crd_file_ctl  ";
		sqlCmd += " where file_name like ?";
		sqlCmd += "  and crt_date  = ? ";
		setString(1, "ICPSMQND.ERR." + "%" + ".TXT");
		setString(2, queryDate);

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");

		errFileName = String.format("ICPSMQND.ERR.%s%02d.TXT", queryDate, fileNo + 1);
		showLogMessage("I", "", "Output Filename = [" + errFileName + "]");

		outErrFileName = String.format("%s/media/icu/error/%s", comc.getECSHOME(), errFileName);
		outErrFileName = Normalizer.normalize(outErrFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outErrFileName + "]");

		return 0;
	}

	/***********************************************************************/
	int outPutFile() throws Exception {

//		outFileName = String.format("%s/media/icu/out/%s", comc.getECSHOME(), getFileName);
//		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		
		String yy = (String.valueOf(Integer.parseInt(hSystemDate.substring(0, 4))-1911)).substring(1, 3); //民國年後兩碼
    	checkFileCtl1(yy);
    	filename = String.format("PSMQD.%8s%02d", hSystemDate, hNn);
    	temstr = String.format("%s/media/icu/out/%s", comc.getECSHOME(),filename);
		showLogMessage("I", "", "Output Filepath = [" + filename + "]");
		temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
		
		filename1 = String.format("PSMQA.%8s%02d", hSystemDate, hNn);
    	temstr1 = String.format("%s/media/icu/out/%s", comc.getECSHOME(),filename1);
		showLogMessage("I", "", "Output Filepath = [" + filename1 + "]");
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

		filename2 = String.format("TEMPFILE");
    	temstr2 = String.format("%s/media/icu/out/%s", comc.getECSHOME(),filename2);
		showLogMessage("I", "", "Output Filepath = [" + filename2 + "]");
		temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
		
		return 0;
	}

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
//		sqlCmd += " and crt_date = to_char(sysdate,'yyyymmdd') ";
		setString(1, getFileName);
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
    void checkFileCtl1(String YY) throws Exception {
    	String likeFilename = "";
    	String hFilename = "";
    	likeFilename = String.format("PSMQD.%8s", hSystemDate)+"%";
        sqlCmd = "select file_name ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
//        sqlCmd += " and crt_date  = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += " order by file_name desc  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, likeFilename);
        int tmpInt = selectTable();
        if (notFound.equals("Y")) {
        	hNn++;
        }else {
        	hFilename = getValue("file_name");
        	hNn = Integer.valueOf(hFilename.substring(14, 16))+1;
        }

    }
    /***************************************************************************/	
	void createErrReport() throws Exception {

		ncccData1 = new Buf1();

		seq++;
		ncccData1.idNo = tmpIdNo;
		ncccData1.cardNo = tmpCardNo;

		ncccData1.errReason = errCode.getErrorCode();

		ncccData1.date = sysDate;

		buf = ncccData1.allText();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}
	
	private enum ErrorReason{
		ERR1(String.format("%-200s", "客戶代號第11碼延伸碼不為空白或“R”")), 
		ERR2(String.format("%-200s", "卡片號碼不存在卡片主檔")), 
		ERR3(String.format("%-200s", "客戶代號不存在卡人主檔")), 
		ERR4(String.format("%-200s", "主卡人ID不存在信用卡卡人主檔")), 
		ERR5(String.format("%-200s", "主卡人ID不存在VD卡卡人主檔")), 
		ERR6(String.format("%-200s", "異動卡人檔查無資料")), 
		ERR7(String.format("%-200s", "此ID已存在卡人檔")), 
		ERR8(String.format("%-200s", "此ID_P_SEQNO不存在帳戶檔")), 
		ERR9(String.format("%-200s", "客戶代號與卡人檔不一致")),
		ERR10(String.format("%-200s", "客戶代號與VD卡人檔不一致")),
		ERR11(String.format("%-200s", "使用檔案中的卡號從crd_card取得的id_p_seqno不存在卡人主檔")),
		ERR12(String.format("%-200s", "使用檔案中的卡號從dbc_card取得的id_p_seqno不存在VD卡人主檔"))
		;
				
	    private String info = "";
		ErrorReason(String info){
			this.info = info;
		}
		public String getErrorCode() {
			return info;
		}
	}

	/**********************************************************************/
	void createPsmqa() throws Exception {

		icuData = new Buf2();

		seq++;
		icuData.dataProduceUnit = "00600000";
		icuData.dataReceiveUnit = "95000000";
		icuData.dataCount = String.format("%07d",totalInputFile);
		icuData.errCode = "";
//		icuData.filler = "";

		buf = icuData.allText();
		lpar3.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}
    /***********************************************************************/
    void createTempfile() throws Exception {

    	tempData = new Buf3();
    	
    	tempData.uploadFileName = filename1;
    	buf4 = tempData.allText();
    	buf4 = buf4 + "\n";
        
        tempData.uploadFileName = filename;
    	buf5 = tempData.allText();
    	
    	buf = buf4 + buf5;
    	lpar4.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}

	/**********************************************************************/
	String getIdPSeqnoFromCard() throws Exception {
		String idPSeqno = getIdPSeqnoFromCrdCard();
		if (idPSeqno == null || idPSeqno.trim().length() == 0 ) {
			tmpActivateDate = "";
			idPSeqno = getIdPSeqnoFromDbcCard();
			if (idPSeqno == null || idPSeqno.trim().length() == 0) {
				cardType = "";
				return null;
			}
		}

		return idPSeqno;
	}
	
	/**********************************************************************/
	String getIdPSeqnoFromCrdCard() throws Exception {
		String idPSeqno = "";

		sqlCmd = "select id_p_seqno ";
		sqlCmd += "from crd_card ";
		sqlCmd += "where card_no = ? ";
		setString(1, tmpCardNo);

		int cnt = selectTable();
		if (cnt == 0) {
			return null;
		}
			
		idPSeqno = getValue("id_p_seqno");
		
		tmpActivateDate = getActivateDate(); // 開卡日期
		cardType = "CC"; // 卡片為信用卡
		showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，為信用卡");
		
		return idPSeqno;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getActivateDate() throws Exception {
		sqlCmd = "select activate_date ";
		sqlCmd += "from crd_card ";
		sqlCmd += "where card_no = ? ";
		setString(1, tmpCardNo);
		selectTable();
		
		return getValue("activate_date");
	}

	/**********************************************************************/
//	boolean isIdnoExist() throws Exception {
//		
//		if (isCrdIdnoExist() == false) {
//			if (isDbcIdnoExist() == false) {
//				return false;
//			}
//		}
//		
//		return true;
//	}
	/**********************************************************************/
	
	boolean isCrdIdnoExist() throws Exception {

		sqlCmd = "select count(*) tmpInt ";
		sqlCmd += "from crd_idno ";
		sqlCmd += "where id_no = ? ";
		setString(1, tmpIdNo); // 客戶代號前10碼

		selectTable();

		int tmpInt = getValueInt("tmpInt");
		if (tmpInt <= 0) {
			showLogMessage("I", "", "Error : id_no = [" + tmpIdNo + "]，客戶代號不存在信用卡人主檔");
			putErrReportList(ErrorReason.ERR3);
			return false;
		}

		return true;
	}

	/**********************************************************************/
	String getIdPSeqnoFromDbcCard() throws Exception {
		String idPSeqno = "";
	
		sqlCmd = "select id_p_seqno ";
		sqlCmd += "from dbc_card ";
		sqlCmd += "where card_no = ? ";
		setString(1, tmpCardNo);
	
		int cnt = selectTable();
		
		if (cnt == 0) {
			showLogMessage("I", "", "Error : card_no = [" + tmpCardNo + "]，卡片號碼不存在卡片主檔");
			putErrReportList(ErrorReason.ERR2);
			return null;
		}
		
		cardType = "VD"; // 卡片為VD信用卡
		showLogMessage("I", "", "card_no = [" + tmpCardNo + "]，為VD卡");
		idPSeqno = getValue("id_p_seqno");
	
		return idPSeqno;
	}

	/**********************************************************************/
	boolean isDbcIdnoExist() throws Exception {

		sqlCmd = "select count(*) tmpInt ";
		sqlCmd += "from dbc_idno ";
		sqlCmd += "where id_no = ? ";
		setString(1, tmpIdNo); // 客戶代號前10碼

		selectTable();

		int tmpInt = getValueInt("tmpInt");
		if (tmpInt == 0) {
			showLogMessage("I", "", "Error : id_no = [" + tmpIdNo + "]，客戶代號不存在VD卡人主檔");
			putErrReportList(ErrorReason.ERR3);
			return false;
		}

		return true;
	}

	/**********************************************************************/
//	int selectCrdIdnoA() throws Exception {
//
//		sqlCmd = "select count(*) tmpInt ";
//		sqlCmd += "from crd_idno ";
//		sqlCmd += "where id_no = ? ";
//		setString(1, tmpIdNo); // 主卡人ID
//
//		selectTable();
//
//		int tmpInt = getValueInt("tmpInt");
//
//		if (tmpInt == 0) {
//			showLogMessage("I", "", "Error : id_no = [" + tmpIdNo + "]，主卡人ID不存在信用卡卡人主檔");
//			errCode = "4";
//			createErrReport();
//			totalOutputFile++;
//			return 1;
//		}

//		return 0;
//	}

	/**********************************************************************/
//	int selectDbcIdnoA() throws Exception {
//
//		sqlCmd = "select count(*) tmpInt ";
//		sqlCmd += "from dbc_idno ";
//		sqlCmd += "where id_no = ? ";
//		setString(1, tmpIdNo); // 主卡人ID
//
//		selectTable();
//
//		int tmpInt = getValueInt("tmpInt");
//
//		if (tmpInt == 0) {
//			showLogMessage("I", "", "Error : id_no = [" + tmpIdNo + "]，主卡人ID不存在VD卡卡人主檔");
//			errCode = "5";
//			createErrReport();
//			totalOutputFile++;
//			return 1;
//		}
//
//		return 0;
//	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	private boolean selectDbcCard(String cardNo) throws Exception {
		sqlCmd =   "select id_p_seqno, acct_no ";
		sqlCmd += "from dbc_card ";
		sqlCmd += "where card_no = ? ";
		setString(1, cardNo);

		int selectCnt = selectTable();

		if (selectCnt <= 0) {
			showLogMessage("I", "", "Error : card_no = [" + cardNo + "]，卡片號碼不存在卡片主檔[selectDbcCard]");
//			errCode = "2";
//			createErrReport();
//			totalOutputFile++;
			return false;
		}

		return true;
		
	}

	/**
	 * 
	 * @param idPSeqno
	 * @return
	 * @throws Exception 
	 */
	private boolean selectDbcIdno(String idPSeqno) throws Exception {
		sqlCmd =   "select id_no, id_no_code ";
		sqlCmd += "from dbc_idno ";
		sqlCmd += "where id_p_seqno = ? ";
		setString(1, idPSeqno);

		int selectCnt = selectTable();

		if (selectCnt <= 0) {
			showLogMessage("I", "", "Error : id_p_seqno = [" + idPSeqno + "]，客戶代號不存在卡人主檔[selectDbcIdno]");
//			errCode = "3";
//			createErrReport();
//			totalOutputFile++;
			return false;
		}

		return true;
	}

	/***********************************************************************/
	void updateDbcIdno() throws Exception {

		daoTable = DBC_IDNO;
		updateSQL = " cellar_phone  = ?,";
		updateSQL += " birthday  = ? ,";
		updateSQL += " mod_user  = ? ,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr   = " where id_no = ? ";      
        setString(1, tmpCellarPhone);
        setString(2, tmpBirthDay);
        setString(3, prgmId);
        setString(4, prgmId);
        setString(5, sysDate + sysTime);
        setString(6, tmpIdNo);

		updateTable();

		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_idno not found!", "", "");
		}

		return;
	}

	/***********************************************************************/
	void updateCrdIdno() throws Exception {

		daoTable = CRD_IDNO;
		updateSQL = " cellar_phone  = ?,";
		updateSQL += " mod_user  = 'IcdD017',";
		updateSQL += " mod_pgm  = 'IcdD017',";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
		whereStr = " where id_no = ? ";
		setString(1, tmpCellarPhone);
		setString(2, sysDate + sysTime);
		setString(3, tmpIdNo);

		updateTable();

		if (notFound.equals("Y")) {
			comcr.errRtn("update_crd_idno not found! ", tmpIdNo, "");
		}

		return;
	}

	/***********************************************************************/
	void insertFileCtl1(String getFileName) throws Exception {

		setValue("file_name", getFileName);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
	}

	/***********************************************************************/
	void insertFileCtl() throws Exception {
		setValue("file_name", errFileName);
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
			setString(3, errFileName);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD017 proc = new IcuD017();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData1.idNo = comc.subMS950String(bytes, 0, 20);
		ncccData1.cardNo = comc.subMS950String(bytes, 20, 39);
		ncccData1.errReason = comc.subMS950String(bytes, 39, 239);
		ncccData1.date = comc.subMS950String(bytes, 239, 247);
	}

	void splitBuf2(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		icuData.dataProduceUnit = comc.subMS950String(bytes, 0, 8);
		icuData.dataReceiveUnit = comc.subMS950String(bytes, 8, 8);
		icuData.dataCount = comc.subMS950String(bytes, 16, 7);
		icuData.errCode = comc.subMS950String(bytes, 23, 4);
//		icuData.filler = comc.subMS950String(bytes, 27, 4);
	}
		
	void splitBuf3(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		tempData.uploadFileName = comc.subMS950String(bytes, 0, 16);
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;

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
	private Map<String, Object> processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}
	/****************************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "FISC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/out", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + filename + " 開始傳送....");
	      int errCode = commFTP.ftplogName("FISC_FTP_PUT", "mput " + filename);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + filename + " 資料"+" errcode:"+errCode);
	          insertEcsNotifyLog(filename);          
	      }
	      
	      showLogMessage("I", "", "mput " + filename1 + " 開始傳送....");
	      int errCode1 = commFTP.ftplogName("FISC_FTP_PUT", "mput " + filename1);
	      
	      if (errCode1 != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + filename1 + " 資料"+" errcode:"+errCode1);
	          insertEcsNotifyLog(filename1);          
	      }
	      
	      showLogMessage("I", "", "mput " + filename2 + " 開始傳送....");
	      int errCode2 = commFTP.ftplogName("FISC_FTP_PUT", "mput " + filename2);
	      
	      if (errCode1 != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + filename2 + " 資料"+" errcode:"+errCode2);
	          insertEcsNotifyLog(filename2);          
	      }
	  }
	/****************************************************************************/
	void procFTP1() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + errFileName + " 開始傳送....");
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + errFileName);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + errFileName + " 資料"+" errcode:"+err_code);
	          insertEcsNotifyLog(errFileName);          
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
			String tmpstr1 = comc.getECSHOME() + "/media/icu/out/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
	  /****************************************************************************/
		void renameFile2(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/icu/error/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
		
		// 錯誤報表
	  /****************************************************************************/
		void renameFile3(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/icu/out/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + filename;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}

	/***********************************************************************/
	class Buf1 {
		String cardNo;
		String idNo;
		String errReason;
		String date;
	
		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(idNo, 20);
			rtn += fixLeft(cardNo, 19);
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

	/****************************************************************************/
	class Buf2 {
		String dataProduceUnit;
		String dataReceiveUnit;
		String dataCount;
		String errCode;
//		String filler;
	
		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(dataProduceUnit, 8);
			rtn += fixLeft(dataReceiveUnit, 8);
			rtn += fixLeft(dataCount, 7);
			rtn += fixLeft(errCode, 4);
//			rtn += fixLeft(filler, 4);
			rtn += "\r";
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
	
	/***********************************************************************/
    class Buf3 {
		String uploadFileName;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(uploadFileName, 16);
			rtn += "\r";
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
    
	/***********************************************************************/
	class DbcDataD017 {
		String idPSeqno;
		String acctNo;
		String idNo;
		String idNoCode;
	}
}


