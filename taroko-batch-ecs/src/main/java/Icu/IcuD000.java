/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  109/12/18  V1.01.01  Ryan         Initial                                *
*  109/12/22  V1.01.02  Wilson       檔名日期改營業日                                                                             *
*  109/12/25  V1.01.03  Wilson       無檔案正常結束                                                                                  *
*  110/03/03  V1.01.04  Justin         prevent from causing duplication when updating id_no
*  110/03/04  V1.01.05  Justin         change notDup->notFound, add error reasons, fix updateSQL bugs and add a new errorCode
*  110/12/02  V1.01.06  Justin       add commit and rollback when writing error reports *
*  110/12/03  V1.01.07  Wilson       程式處理邏輯調整                       *
*  111/01/11  V1.01.08  Justin       update id_no of debit card             *    
*                                    insert dbc_chg_id: acct_no = ""        *  
*  111/02/14  V1.01.09  Justin       sort files by their modified dates     *      
*  111/02/14  V1.01.10  Ryan         big5 to MS950                          *
*  111/02/16  V1.01.11  Justin       fix the bug of error files             *   
*  111/02/17  V1.01.12  Justin       fix the bug of error files             * 
*  111/02/17  V1.01.13  Justin       fix the bug of error files             *
*  111/02/17  V1.01.14  Justin       prevent from out of index exception    *      
*  111/04/12  V1.01.15  Justin       避免重複UPDATE CRD_IDNO_SEQNO          * 
*  111/04/13  V1.01.16  Justin       更新異動信用卡ID及異動VD卡ID邏輯       * 
*  111/04/14  V1.01.17  Justin       更新異動VD卡ID邏輯                     *   
*             V1.01.18  Justin       修改輸出log                            *    
*  111/05/11  V1.00.19  Justin       update dba_acno增加acct_holder_id      *
*  111/05/12  V1.00.20  Justin       修改錯誤筆數顯示問題                   *
*  111/05/23  V1.00.21  Justin       crd_chg_id update old_id_p_seqno       *
*  111/09/13  V1.00.22  Wilson       update act_acno增加card_indicator = '1' *
*  111/10/05  V1.00.23  Wilson       updateActAcno、updateDbaAcnoSetIdNo 找不到資料繼續往下執行                                  
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
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;
import com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException;


/*CARDLINK信用卡客戶資料處理程式*/
public class IcuD000 extends AccessDAO {
private static final String DATA_FILE_NAME = "ICCRDQND";
private static final String ISSUE_UNIT_CODE = "M00600000";
private String progname = "處理CARDLINK信用卡變更ID資料程式  111/10/05  V1.00.23";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	int debug = 1;

	String prgmId = "IcuD000";
	
	String fileFolderPath = comc.getECSHOME() + "/media/icu/";
	
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	Buf1 ncccData = new Buf1();
	protected final String dT1Str = "mod_audcode, issue_code, card_type, cust_id, issue_code_bin, card_seqno, sup_seqno, reissue_seqno, card_chk_code, new_end_date, name, cvc1, cvc2, vip_code, oppost_code, oppst_date, err_code ";//一般實體卡
	protected final int[] dt1Length = { 1, 8, 2, 11, 6, 7, 1, 1, 1, 4, 19, 3, 3, 1, 9, 4, 4 };//一般實體卡
	
	int rptSeq1 = 0;
	String buf = "";
    String queryDate = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	int hRecCnt1 = 0;
	int seq = 0;

	String getFileName;
	String getFileName1;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;
	int totalOutputFileEachFile;
	int totalSuccessCrd;
	int totalSuccessDbc;

	String tmpModAudcode;
	String issueCode;
	String tmpCustId;
	String tmpIssueCodeBin;
    String tmpCardSeqno;
	String tmpSupSeqno;
	String tmpReissueSeqno;
	String tmpCardChkCode;
	String tmpErrCode;
	
	String cardNo;
	String crdAcnoFlag;
	String crdIdPseqno;
	String crdIdNo;
	String crdChiName;
	
	ErrorReason errCode;
	String idFlag;
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
//                queryDate = hBusiBusinessDate;
            	queryDate = "";
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
            showLogMessage("I", "", String.format("執行結束,[ 總筆數 : %d],[ 變更ID成功筆數 : %d (CRD_IDNO : %d, DBC_IDNO: %d)],[ 錯誤筆數 : %d]", 
            		totalInputFile, totalSuccessCrd+totalSuccessDbc, totalSuccessCrd, totalSuccessDbc, totalOutputFile));
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

//		final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "M00600000", "ICCRDQND"
//				,new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式		
		
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
			// Justin prevent from substring the file name with the length less than 18
//			getFileName1 = String.format("%s.CHGID%s",getFileName.substring(0,18),getFileName.substring(18));

			if( !( file.matches(fileNameTemplate) || ( fileNameTemplate2.length() > 0 && file.matches(fileNameTemplate2) ) ))  
				continue;
			
			getFileName1 = String.format("%s.CHGID%s",getFileName.substring(0,18),getFileName.substring(18));
			
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

			if(rec.trim().length()<=0)
				break;
			
			totalInputFile++;
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			processDisplay(1000);
		}

		if (totalOutputFileEachFile > 0) {
			outPutTextFile();
			comc.writeReport(outFileName, lpar1, "MS950");
			insertFileCtl();
			lpar1.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}

		closeInputText(fi);

		insertFileCtl1(getFileName1);

		return 0;
	}
	
	/***********************************************************************/
    private void moveData(Map<String, Object> map) throws Exception {          
    	
        tmpModAudcode = (String) map.get("mod_audcode"); //異動碼
        tmpModAudcode = tmpModAudcode.trim();
        issueCode = (String) map.get("issue_code"); 
        String tmpTpanFlag = commString.mid(issueCode, 0,1).trim();
        if(!tmpModAudcode.equals("C")||tmpTpanFlag.equals("T"))
        	return;
        
        tmpCustId = (String) map.get("cust_id");
        tmpCustId = tmpCustId.trim();//客戶代號
        tmpIssueCodeBin = (String) map.get("issue_code_bin");
        tmpIssueCodeBin = tmpIssueCodeBin.trim();//發卡單位bin
        tmpCardSeqno = (String) map.get("card_seqno");
        tmpCardSeqno = tmpCardSeqno.trim();//卡片流水號
        tmpSupSeqno = (String) map.get("sup_seqno");
        tmpSupSeqno = tmpSupSeqno.trim();//正附卡序號
        tmpReissueSeqno = (String) map.get("reissue_seqno");
        tmpReissueSeqno = tmpReissueSeqno.trim();//補發卡序號
        tmpCardChkCode = (String) map.get("card_chk_code");
        tmpCardChkCode = tmpCardChkCode.trim();//卡片檢查號

        cardNo = tmpIssueCodeBin+tmpCardSeqno+tmpSupSeqno+tmpReissueSeqno+tmpCardChkCode;
        
        //GET ID_NO、CHI_NAME、ACNO_FLAG
        if(getIdNo()!=1) {
        	return;        	
        }
        
        if(crdAcnoFlag.equals("1") && tmpCustId.length()==10) {
        	
        	// 2022/04/13
//        	1. 異動信用卡ID
//        	(1)SELECT CRD_IDNO(用檔案的ID)，若存在則不往下做(要記錄一筆到錯誤報表)，若不存在則接續做原本的UPDATE
//        	(2)UPDATE CRD_IDNO_SEQNO 要增加WHERE條件 -> DEBIT_IDNO_FLAG = 'N'
//        	(3)UPDATE CRD_IDNO、ACT_ACNO、CRD_IDNO_SEQNO 若重複則寫錯誤報表但不ROLLBACK
//        	2.異動VD卡ID
//        	(1)SELECT DBC_IDNO(用檔案的ID)，若存在則不往下做(要記錄一筆到錯誤報表)，若不存在則接續做原本的UPDATE
//        	(2)UPDATE CRD_IDNO_SEQNO 要增加WHERE條件 -> DEBIT_IDNO_FLAG = 'Y'
//        	(3)UPDATE DBC_IDNO、DBA_ACNO、CRD_IDNO_SEQNO 若重複則寫錯誤報表但不ROLLBACK
        	
        	
    		showLogMessage("I", "", "tmpCustId = [" + tmpCustId + "]");
    		showLogMessage("I", "", "crdIdNo = [" + crdIdNo + "]");
    		showLogMessage("I", "", "cardNo = [" + cardNo + "]");
        	if(tmpCustId.equals(crdIdNo)) return ;
        	
        	// 2022/04/13 Justin 更新信用卡的ID
        	if(updateCrdId() == 1) {
        		updateCrdEmployee();
        		totalSuccessCrd++;
        		commitDataBase();
        	}

            // 2022/01/11 Justin 同步VD卡的ID
            try {
            	showLogMessage("D", "", String.format("開始確認並同步VD卡的ID[%s]->[%s]", crdIdNo, tmpCustId));
            	if (updateDbcId() == 1) {
            		totalSuccessDbc++;
            		commitDataBase();
            	}
            }catch (Exception e) {
            	showLogMessage("I", "", "更新VD卡的ID發生錯誤:" + e.getLocalizedMessage());
			}
            
        }
        
        return;
    }

	private int updateDbcId() throws Exception {
		boolean sqlResult = false; 
		
		sqlResult = updateDbcIdnoSetIdNo(tmpCustId, crdIdPseqno);
		if (sqlResult == false) return -1;
		
		sqlResult = updateDbaAcnoSetIdNo(tmpCustId, crdIdPseqno);
		if (sqlResult == false) return -1;
		
		updateCrdIdnoSeqno(true);
		insertDbcChgId(tmpCustId, crdIdNo);
		
		showLogMessage("D", "", "成功同步VD卡的ID");
		return 1;
	}
    
    /***********************************************************************/
    
//    private String getAcctNoFromDbcCard(String cardNo) throws Exception {
//    	sqlCmd =  " SELECT acct_no ";
//		sqlCmd += " FROM  dbc_card ";
//		sqlCmd += " WHERE card_no = ? ";
//		sqlCmd += " FETCH first 1 rows only ";
//		setString(1, cardNo);
//		if (selectTable() > 0) {
//			return getValue("acct_no");
//		}else {
//			showLogMessage("I", "", "getAcctNoFromDbcCard Error : 該卡號不存在dbc_card卡檔, card_no = [" + cardNo + "]");
//			return null;
//		}
//
//	}

	/***********************************************************************/

    private boolean insertDbcChgId(String newIdno, String currentIdno) throws Exception {
    	setValue("acct_no", "");
    	setValue("id", currentIdno);
    	setValue("id_code", "0");
    	setValue("corp_flag", "N");
    	setValue("aft_id", newIdno);
    	setValue("aft_id_code", "0");
    	setValue("crt_date", sysDate);
    	setValue("process_flag", "Y");
    	setValue("mod_user", prgmId);
    	setValue("mod_time", sysDate + sysTime);
    	setValue("mod_pgm", prgmId);
    	daoTable = "dbc_chg_id";
    	int insertCnt = insertTable();
    	if (insertCnt <= 0 ) {
    		return false;
    	}
    	
    	return true;				
    }

    /***********************************************************************/

    /**
     * 
     * @param idNoFromFile
     * @param idPSeqno
     * @return
     * @throws Exception
     */
    /**
	* @ClassName: IcuD000
	* @Description: updateDbaAcnoSetIdNo 找不到資料繼續往下執行
	* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
	* @Company: DXC Team.
	* @author Wilson
	* @version V1.00.23, Oct 05, 2022
	*/
    private boolean updateDbaAcnoSetIdNo(String newIdno, String idPSeqno) throws Exception {
    	daoTable = "dba_acno";
    	updateSQL =  " acct_key  = ?,";
    	updateSQL += " mod_user  = ? ,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') , ";
        updateSQL += " acct_holder_id  = ? ";
        whereStr   = " where id_p_seqno = ? ";      
        setString(1, newIdno + "0");
        setString(2, prgmId);
        setString(3, prgmId);
        setString(4, sysDate + sysTime);
        setString(5, newIdno);
        setString(6, idPSeqno);

    	updateTable();

    	if (notFound.equals("Y")) {
    		errCode = ErrorReason.ERR9;
    		showLogMessage("I", "", "update DBA_ACNO[id_no] not found! id_p_seqno = [" + idPSeqno + "], Error: " + errCode);
//			createErrReport();
//			totalOutputFile++;
//			rollbackDataBase();
//    		return false;
    	}

    	return true;
    	
    }

    /***********************************************************************/

    /**
     * 
     * @param idNoFromFile
     * @param idPSeqno
     * @return
     * @throws Exception
     */
    private boolean updateDbcIdnoSetIdNo(String newIdno, String idPSeqno) throws Exception {
    	daoTable = "dbc_idno";
    	updateSQL =  " id_no  = ?,";
    	updateSQL += " mod_user  = ? ,";
        updateSQL += " mod_pgm  = ? ,";
        updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr   = " where id_p_seqno = ? ";      
        setString(1, newIdno);
        setString(2, prgmId);
        setString(3, prgmId);
        setString(4, sysDate + sysTime);
        setString(5, idPSeqno);

        try {
        	updateTable();
        }catch (Exception e) {
    		if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
    			errCode = ErrorReason.ERR11;
    			showLogMessage("I", "","update DBC_IDNO[id_no]: Duplicate! id_p_seqno = [" + idPSeqno + "], Error: " + errCode);
				createErrReport();
				totalOutputFile++;
    		}
    		return false;
    	}

    	if (notFound.equals("Y")) {
    		errCode = ErrorReason.ERR8;
    		showLogMessage("I", "", "update DBC_IDNO[id_no]: Not found! id_p_seqno = [" + idPSeqno + "], Error: " + errCode);
//			createErrReport();
//			totalOutputFile++;
    		return false;
    	}

    	return true;
    	
    }    

	/***********************************************************************/
	int outPutTextFile() throws Exception {
		int fileNo = 0;

        sqlCmd  = "select max(substr(file_name, 28, 2)) file_no";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += " where file_name like ?";
        sqlCmd += "  and crt_date  = ? ";
        setString(1, "ICCRDQND.CHGID.ERR." + "%" + ".TXT");
        setString(2, sysDate);

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");
		
		hNcccFilename = String.format("ICCRDQND.CHGID.ERR.%s%02d.TXT", sysDate, fileNo + 1);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%serror/%s", fileFolderPath, hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
//      sqlCmd += " and crt_date = to_char(?,'yyyymmdd') ";
      setString(1, getFileName1);
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
	void createErrReport() throws Exception {
		totalOutputFileEachFile++;

		ncccData1 = new Buf1();

		seq++;
		ncccData1.modNo = tmpModAudcode;
		ncccData1.cardNo = cardNo;
		ncccData1.idnoId = tmpCustId;

		ncccData1.errReason = errCode.getErrorCode();

		ncccData1.date = sysDate;

		buf = ncccData1.allText();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}
	
	private enum ErrorReason{
		ERR1(String.format("%-200s", "異動卡人檔查無資料(CRD)")), 
		ERR2(String.format("%-200s", "異動帳戶檔查無資料(CRD)")), 
		ERR3(String.format("%-200s", "該卡號不存在卡檔")), 
		ERR4(String.format("%-200s", "該卡號的原身份證號不存在卡人檔")), 
		ERR5(String.format("%-200s", "異動身分證序號代碼檔查無資料(CRD)")), 
		ERR6(String.format("%-200s", "此ID已存在卡人檔(CRD)")),
		ERR7(String.format("%-200s", "卡人流水號檔已存在相同的ID(CRD)")),
		ERR8(String.format("%-200s", "異動卡人檔查無資料(DBC)")), 
		ERR9(String.format("%-200s", "異動帳戶檔查無資料(DBC)")), 
		ERR10(String.format("%-200s", "異動身分證序號代碼檔查無資料(DBC)")), 
		ERR11(String.format("%-200s", "此ID已存在卡人檔(DBC)")),
		ERR12(String.format("%-200s", "卡人流水號檔已存在相同的ID(DBC)"))
		;
		private String info;
		ErrorReason(String info){
			this.info = info;
		}
		public String getErrorCode() {
			return this.info;
		}
	}

	

	/***********************************************************************/
	int getIdNo() throws Exception {

		sqlCmd = " select id_p_seqno,acno_flag ";
		sqlCmd += " from crd_card ";
		sqlCmd += " where card_no = ? ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, cardNo);
		if (selectTable() > 0) {
			crdIdPseqno = getValue("id_p_seqno");
		    crdAcnoFlag = getValue("acno_flag");
		}else {
			showLogMessage("I", "", "card_no = [" + cardNo + "]，Error : 該卡號不存在卡檔");
			errCode = ErrorReason.ERR3;
			createErrReport();
			totalOutputFile++;
			return -1;
		}

		showLogMessage("I", "", "id_p_seqno = [" + crdIdPseqno + "]，acno_flag = [" + crdAcnoFlag + "]");

		
		sqlCmd = " select id_no,chi_name ";
		sqlCmd += " from crd_idno ";
		sqlCmd += " where id_p_seqno = ? ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, crdIdPseqno);
		if (selectTable() > 0) {
			crdIdNo = getValue("id_no");
		    crdChiName = getValue("chi_name");
		}else {
			showLogMessage("I", "", "id_p_seqno = [" + crdIdPseqno + "]，Error : 該卡號的原身份證號不存在卡人檔");
			errCode = ErrorReason.ERR4;
			createErrReport();
			totalOutputFile++;
			return -1;
		}
		
		showLogMessage("I", "", "id_no = [" + crdIdNo + "]，chi_name = [" + crdChiName + "]");
		
		return 1;
	}


	/***********************************************************************/
	int updateCrdId() throws Exception {
		
		boolean result = updateCrdIdno();
		if (result == false) return -1;
		
		result = updateActAcno();
		if (result == false) return -1;
		
		updateCrdIdnoSeqno(false);
		
		insertCrdChgId();
		
		return 1;
	}

	private boolean updateCrdIdno() throws Exception {
		daoTable = "crd_idno";
		updateSQL = " id_no = ?,";
		updateSQL += " mod_user  = 'IcuD000' ,";
        updateSQL += " mod_pgm  = 'IcuD000' ,";
        updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        whereStr = " where id_p_seqno = ? ";      
        setString(1, tmpCustId);
        setString(2, sysDate + sysTime);
        setString(3, crdIdPseqno);
		
		try {
        	updateTable();
        }catch (Exception e) {
			if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
				//duplicate
				errCode = ErrorReason.ERR6;
				showLogMessage("I", "", "update CRD_IDNO id_p_seqno = [" + crdIdPseqno + "]，Error : " + errCode);
				createErrReport();
				totalOutputFile++;
				return false;
			}
			comcr.errRtn("update CRD_IDNO id_p_seqno = [" + crdIdPseqno + "]，Error : " + e.getMessage(), "", "");
		}

		if (notFound.equals("Y")) {
			errCode = ErrorReason.ERR1;
			showLogMessage("I", "", "update CRD_IDNO id_p_seqno = [" + crdIdPseqno + "]，Error : " + errCode);
			createErrReport();
			totalOutputFile++;
			return false;
		}
		
		return true;
	}
	/**
	* @ClassName: IcuD000
	* @Description: updateActAcno 異動帳戶資料檔的帳戶查詢碼時增加綁定限一般卡(商務卡不需異動)
	* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
	* @Company: DXC Team.
	* @author Wilson
	* @version V1.00.22, Sep 13, 2022
	*/
	/**
	* @ClassName: IcuD000
	* @Description: updateActAcno 找不到資料繼續往下執行
	* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
	* @Company: DXC Team.
	* @author Wilson
	* @version V1.00.23, Oct 05, 2022
	*/
	private boolean updateActAcno() throws Exception {
		daoTable = "act_acno";
		updateSQL = " acct_key = ?,";
		updateSQL += " apr_user  = 'IcuD000' ,";
		updateSQL += " apr_date  = to_char(sysdate,'YYYYMMDD') ,";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " mod_user  = 'IcuD000' ,";
        updateSQL += " mod_pgm  = 'IcuD000' ";
        whereStr = " where id_p_seqno = ? and card_indicator = '1' ";      
        setString(1, tmpCustId+"0");
        setString(2, sysDate + sysTime);
        setString(3, crdIdPseqno);

		updateTable();
		
		if (notFound.equals("Y")) {
			errCode = ErrorReason.ERR2;
			showLogMessage("I", "", "update act_acno id_p_seqno = [" + crdIdPseqno + "]，Error : " + errCode);
//			createErrReport();
//			totalOutputFile++;
//			rollbackDataBase();
//			return false;
		}
		
		return true;
	}

	private int updateCrdIdnoSeqno(boolean isDebit) throws Exception {
		daoTable = "crd_idno_seqno";
		updateSQL = " id_no = ? ";
        whereStr = " where id_p_seqno = ? AND DEBIT_IDNO_FLAG = ? ";      
        setString(1, tmpCustId);
        setString(2, crdIdPseqno);
        setString(3, (isDebit) ? "Y" : "N");
//        updateTable();
        
		// 2022/04/12 Justin prevent from updating duplicate crd_idno_seqno 
		try {
        	updateTable();
        }catch (Exception e) {
			if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
				// duplicate
				errCode = (isDebit) ? ErrorReason.ERR12 : ErrorReason.ERR7;
				showLogMessage("I", "", "id_p_seqno = [" + crdIdPseqno + "]，Error : " + errCode);
				createErrReport();
//				rollbackDataBase();
				totalOutputFile++;
				return 1;
			}
			comcr.errRtn("id_p_seqno = [" + crdIdPseqno + "]，Error : " + e.getMessage(), "", "");
		}

		if (notFound.equals("Y")) {
			errCode = (isDebit) ? ErrorReason.ERR10 : ErrorReason.ERR5;
			showLogMessage("I", "", "id_p_seqno = [" + crdIdPseqno + "]，Error : " + errCode);
			createErrReport();
			totalOutputFile++;
//			rollbackDataBase();
		}
		
		return 1;
	}

	/***********************************************************************/
	void insertCrdChgId() throws Exception {
		sqlCmd = " select count(*) as tot_cnt ";
		sqlCmd += " from crd_chg_id ";
		sqlCmd += " where old_id_no = ? ";
		setString(1, crdIdNo);
		selectTable();
		int totCnt = getValueInt("tot_cnt");
		
		if(totCnt<1) {
			setValue("old_id_no", crdIdNo);
			setValue("old_id_no_code", "0");
			setValue("id_p_seqno", crdIdPseqno);
	        setValue("old_id_p_seqno", crdIdPseqno);
			setValue("id_no", tmpCustId);
			setValue("id_no_code", "0");
			setValue("post_jcic_flag", "Y");
			setValue("chi_name", crdChiName);
			setValue("chg_date", sysDate);
			setValue("crt_date", sysDate);
	        setValue("crt_user", prgmId);
	        setValue("apr_date", sysDate);
	        setValue("apr_user", prgmId);
	        setValue("src_from", "2");
	        setValue("mod_time", sysDate + sysTime);
	        setValue("mod_user", prgmId);
	        setValue("mod_pgm", prgmId);
			daoTable = "crd_chg_id";
			insertTable();				
		}else {
			daoTable = "crd_chg_id";
			updateSQL = " id_no = ?,";
			updateSQL += " id_p_seqno = ?,";
			updateSQL += " old_id_p_seqno = ?,";
			updateSQL += " chi_name = ?,";
			updateSQL += " chg_date = to_char(sysdate,'YYYYMMDD'),";
			updateSQL += " apr_user  = 'IcuD000' ,";
			updateSQL += " apr_date  = to_char(sysdate,'YYYYMMDD') ,";
			updateSQL += " src_from  = '2' ,";
			updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
			updateSQL += " mod_user  = 'IcuD000' ,";
	        updateSQL += " mod_pgm  = 'IcuD000' ";
	        whereStr = " where old_id_no = ? ";      
	        setString(1, tmpCustId);
	        setString(2, crdIdPseqno);
	        setString(3, crdIdPseqno);
	        setString(4, crdChiName);
	        setString(5, sysDate + sysTime);
	        setString(6, crdIdNo);
			updateTable();
		}

	}
	
	/*********************************************************************/
	void updateCrdEmployee() throws Exception {
		daoTable = "crd_employee";
		updateSQL = " id = ? ";
        whereStr = " where id = ? ";      
        setString(1, tmpCustId);
        setString(2, crdIdNo);
		updateTable();
	}

	/***********************************************************************/
	void insertFileCtl1(String fileName) throws Exception {

		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
	}

	/***********************************************************************/
	void insertFileCtl() throws Exception {
		setValue("file_name", hNcccFilename);
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
		IcuD000 proc = new IcuD000();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String modNo;
		String idnoId;
		String cardNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(modNo, 1);
			rtn += fixLeft(cardNo, 16);
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
		ncccData1.cardNo = comc.subMS950String(bytes, 1, 17);
		ncccData1.idnoId = comc.subMS950String(bytes, 17, 29);
		ncccData1.errReason = comc.subMS950String(bytes, 29, 229);
		ncccData1.date = comc.subMS950String(bytes, 229, 237);
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
