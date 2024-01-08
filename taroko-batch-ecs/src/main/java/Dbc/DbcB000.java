/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  108/11/26  V1.00.00    Rou       update dbc_emap_tmp()、dbc_debit_tmp       *
*  109/03/30  V1.00.00    Rou       update data                               *
*  109/04/07  V1.00.01    Wilson    寫入dbc_emap_tmp欄位調整                                                            *
*  109/04/14  V1.00.02    Wilson    mail_branch放branch                        *
*  109-11-12  V1.00.03  yanghan       修改了變量名稱和方法名稱                                                                 *
*  109/11/25  V1.00.04    Wilson    select_dbc_card_type not found!           *
**  109/12/24  V1.00.05  yanghan       修改了變量名稱和方法名稱            *
*  110/06/15  V1.00.06   Wilson     insert加入digital_flag                     *
*  110/06/18  V1.00.07   Wilson     預製卡要insert card_no                      *
*  110/08/19  V1.00.08   Wilson     英文姓名取前25碼                                                                                    *
*  111/04/18  V1.00.09   Wilson     check_code D13 -> D48                     *
*  111/07/22  V1.00.10   Ryan       檔案格式調整
*  111/09/19  V1.00.11   Wilson     revolve_int_rate_year不處理                                              *
*  111/12/25  V1.00.12   Wilson     檔案格式修正                                                                                             *
*  112/01/17  V1.00.13   Wilson     有中文字的欄位增加replace處理全形空白                                       *
*  112/04/12  V1.00.14   Wilson     調整stat_send_internet處理邏輯                                           *
*  112/07/03  V1.00.15   Wilson     假日不執行                                                                                                 *
*  112/08/30  V1.00.16   Wilson     檢核有誤增加rollback                          *
*  112/11/11  V1.00.17   Wilson     mark檢核假日不執行                                                                               *
*  112/11/12  V1.00.18   Wilson     取消mark檢核假日不執行                                                                        *
*  112/11/30  V1.00.19   Wilson     判斷數位帳戶                                                                                              *
*  112/12/11  V1.00.20   Wilson     卡別為200且帳號5~7碼為988才是數位帳戶                                              *
******************************************************************************/

package Dbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*VD預製卡分行整批製卡/後製卡申請資料檔案*/
public class DbcB000 extends AccessDAO {
    private String progname = "VD預製卡分行整批製卡/後製卡申請資料檔案  112/12/11 V1.00.20";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    int debugD = 1;
    int tmpInt = 0;
    int totalCnt = 0;
    int totalCnt2 = 0;
    String checkHome = "";
    String hTempUser = "";
    String cmdStr = "";

    String prgmId = "DbcB000";

    protected final String dt1Str = "col1, ap1_apply_date, col3, col4, apply_no, act_no, card_ref_num, col8, apply_type, apply_source," 
    		+ "eng_name, col12, col13, col14, col15, reg_bank_no, branch, crt_bank_no, vd_bank_no,"  
    		+ "home_area_code1, home_tel_no1, cellar_phone, office_area_code1, office_tel_no1, office_tel_ext1,"  
    		+ "col26, col27, col28, apply_id, birthday, sex, col32, col33, col34, col35,"  
    		+ "col36, stmt_cycle, revolve_int_rate_year, e_mail_addr, col40, col41, col42, col43, col44,"  
    		+ "col45, card_no, col47, col48, col49, col50, col51, market_agree_base, col53,"  
    		+ "col54, col55, col56, col57, col58, col59, col60, col61, col62, col63,"  
//    		+ "col64, col65, col66, col67, col68, col69, chi_name," 
		    + "col64, col65, col66, col67, col68, col69, col70, col71, chi_name," 
    		+ "resident_zip, resident_addr1, resident_addr2, resident_addr3, resident_addr4, resident_addr5,"  
    		+ "mail_zip, mail_addr1, mail_addr2, mail_addr3, mail_addr4, mail_addr5,"  
    		+ "company_zip, company_addr1, company_addr2, company_addr3, company_addr4, company_addr5,"  
//    		+ "col88, col89, col90, col91, col92,";
    		+ "col91, col92, col93, col94 ";
        
    protected final int[] dt1length1 = { 1, 7, 2, 6, 12, 13, 2, 1, 4, 1,
    									26, 1, 2, 7, 16, 4, 4, 4, 4,
    									4, 10, 10, 4, 10, 6,
    									4, 4, 6, 11, 8, 1, 1, 1, 11, 8, 
    									3, 2, 4, 30, 10, 1, 8, 1, 4,
    									3, 16, 20, 2, 20, 8, 8, 1, 8,
    									10, 1, 6, 2, 4, 1, 4, 7, 2, 3,
//    									1, 1, 1, 8, 1, 1, 102,
    									1, 1, 1, 8, 1, 1, 1, 105, 102,
    									6, 10, 10, 12, 12, 56,
    									6, 10, 10, 12, 12, 56,
    									6, 10, 10, 12, 12, 56,
//    									30, 14, 102, 10, 106};
    									30, 14, 102, 10};
    
    protected  String[] dt1 = new String[] {};
    String stderr = "";
    String hModUser = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String hDcepCardNo = "";
    String hDcepApplySource = "";
    String hDcepApplyId = "";
    String hDcepCompanyName = "";
    String hDcepBirthday = "";
    String hDcepActNo = "";
    String hDdtpRtnIbmDate = "";
    String hDdtpBatchno = "";
    double hDdtpRecno = 0;
    String hDdtpRowid = "";
    String hDdtpIssuer = "";
    String hDdtpSavingActno = "";
    String hDcepUnitCode     = "";
    String hDcepIcFlag       = "";
    String hDcepDigitalFlag  = "";
    String hDcepGroupCode    = "";
    String hDcepCardType     = "";
    String hDcepSourceCode   = "";
    String hDcepValidFm      = "";
    String hDcepValidTo      = "";
    String hDcepApplyIdCode = "";
    String hDcepBatchno = "";
    String hDcepCorpNo = "";
    String hDcepCorpNoCode = "";
    String hDcepNation = "";
    String hDcepSex = "";
    String hDcepMailType = "";
    String hDcepRegBankNo = "";
    String hDcepCheckCode = "";
    String hDcepNcccType = "";
    String hDcepAgeIndicator = "";
    String hAcctType = "";
    String hDcepServiceCode = "";
    String hDcceCardCode = "";
    String getFileName;

    String hTmpDate = "";
    String hEngName = "";
    String hDcepBusinessCode  = "";
    String hDcepResidentZip   = "";
    String hDcepResidentAddr1 = "";
    String hDcepResidentAddr2 = "";
    String hDcepResidentAddr3 = "";
    String hDcepResidentAddr4 = "";
    String hDcepResidentAddr5 = "";
    String hDcepChiName   = "";
    String hDcepMailZip   = "";
    String hDcepMailAddr1 = "";
    String hDcepMaiAddr2 = "";
    String hDcepMailAddr3 = "";
    String hDcepMailAddr4 = "";
    String hDcepMailAddr5 = "";
    String hDcepCompanyZip   = "";
    String hDcepCompanyAddr1 = "";
    String hDcepCompanyAddr2 = "";
    String hDcepCompanyAddr3 = "";
    String hDcepCompanyAddr4 = "";
    String hDcepCompanyAddr5 = "";
    String hDcepHomeAreaCode1 = "";
    String hDcepHomeTelNo1 = "";
    String hDcepHomeTelExt1 = "";
    String hDcepOfficeAreaCode1 = "";
    String hDcepOfficeTelNo1 = "";
    String hDcepOfficeTelExt1 = "";
    String hDcepCellarPhone = "";
    String hDcepCardRefNum = "";
    String hDcepJobPosition = "";
    String hDcepRevolveIntRateYear = "";
    String hDcepMarketAgreeBase = "";
    String hDcepServiceYear = "";
    String hDcepSalary = "";
    String hDcepMarriage = "";
    String hDcepEducation = "";
    String hDcepSpouseName = "";
    String hDcepSpouseIdNo = "";
    String hDcepSpouseBirthday = "";
    String hDcepBillApplyFlag= "";
    String hDcepCreditLmt = "";
    String hDcepStmCycle = "";
    String hDcepEMailAddr = "";
    String hDcepIntroduceEmpNo = "";
    String hDcepIntroduceId = "";
    String hDcepResidentNoExpireDate = "";
    String hDcepOtherCntryCode = "";
    String hDcepPassportNo = "";
    String hDcepPassportDate = "";
    String hDcepPromoteDept = "";
    String hDcepStatSendInternet = "";
    String hDcepUrFlag = "";
    String hDcepENews = "";
    String hDcepGraduationElementarty = "";
    String hDcepPromoteEmpNo = "";
    String hDcepApplyNo = "";
    String hDdtpSavingActnoExt = "";
    String hDcepBranch = "";
    String hDcepCrtBankNo = "";
    String hDcepVdBankNo = "";
    String hDdtpTxCode = "";
    String hThirdRsn = "";
    String hDdtpCardNo = "";
    String hTmpCardNo = "";
    String hTmpBinNo = "";

    String filename1 = "";
    String filename2 = "";
    String hBusinessDate = "";
    String hSystemDate = "";

    double hDcepRecno = 0;
    int hCount = 0;
    int recordCnt = 0;
    int tTotalCnt = 0;
    int tSuccessCnt = 0;
    int tFailCnt = 0;
    int insertCnt = 0;
    int insertFCnt = 0;
    int getInsertCnt = 0;
    int fi = 0;
    long tmpLong = 0;
    int totalFile = 0;
    
    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
        	dt1 = dt1Str.split(",");
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
            
            commonRtn();
            
            showLogMessage("I", "", String.format("今日營業日 = [%s]", hBusinessDate));
            
            if (checkPtrHoliday() != 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }

            checkOpen();

            // ==============================================
            // 固定要做的                       
            comcr.hCallErrorDesc = "程式執行結束,筆數=["+totalCnt2 + "][" + getInsertCnt + "]";
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
    void checkOpen() throws Exception {
    	
    	checkHome = comc.getECSHOME();
    	String filePath = String.format("%s/media/dbc", checkHome);
    	
        filePath = Normalizer.normalize(filePath, java.text.Normalizer.Form.NFKD);
        List<String> listOfFiles = comc.listFS(filePath, "", "");
        for (String file : listOfFiles) {
            if (file.length() != 20)                	
            	continue; 
            if (!file.substring(0,6).equals("vd_pr_"))
                continue;    
	            getFileName = file;
	            iFileName = String.format(getFileName);
	            filename1 = String.format("%s/media/dbc/%s", checkHome ,iFileName);
	            filename1 = Normalizer.normalize(filename1, java.text.Normalizer.Form.NFKD);
	            showLogMessage("I", "", String.format("Open filename[%s]", filename1));  
	            getEmapBatchno();  
	            if (fileRead() == 1)
	            	continue;
        }
        
        if (totalFile < 1) {
        	showLogMessage("I", "", "無檔案可處理");
        }
        
        return; 
    }
    

	/***********************************************************************/
    void getEmapBatchno() throws Exception {
    	ZonedDateTime toDayDate = ZonedDateTime.now(); 
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyMMdd");
        hTmpDate = toDayDate.format(dateFmt); 
        
        selectSQL = " max(substr(batchno,7,2))  as h_tmp_no ";
        daoTable = "dbc_emap_tmp";
        whereStr = "WHERE dbc_emap_tmp.batchno like ? || '%' ";
        setString(1, hTmpDate.substring(0, 6));
        int recCnt = selectTable();

        String tmp = "1";
        if (getValueInt("h_tmp_no") >= 1) {
        	tmp = Integer.toString(getValueInt("h_tmp_no") + 1);
        }
        String tmpNum = comm.fillZero(tmp, 2);
        hDcepBatchno = hTmpDate.substring(0, 6) + tmpNum;
        if (debug == 1)
	    	showLogMessage("I", "", "Process Batch=[" + hDcepBatchno + "]");
    }
    
 // ************************************************************************
    public int selectCrdFileCtl() throws Exception {
        selectSQL = "count(*) as all_cnt";
        daoTable = "crd_file_ctl";
        whereStr = "WHERE file_name  = ? ";

        setString(1, getFileName);
        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0) {
            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + getFileName + "]");
            return 1;
        }

        return 0;
    }

    /***********************************************************************/
    int fileRead() throws Exception {

        String rec = "";
        fi = openInputText(filename1, "MS950");
        hCount = 0;
        hDcepRecno = 0;

        if (openBinaryInput(filename1) == false) {
            comcr.errRtn(String.format("檔案[%s]不存在!!", filename1), "", comcr.hCallBatchSeqno);
        }        
        
        showLogMessage("I", "", "888 Process file=" + filename1);

        tmpInt = selectCrdFileCtl(); 
        if (tmpInt > 0) //The file already exists
            return 0;

        totalCnt = 0;
        int dataHead = 1;        
        totalFile++;

        while (true) {
        	 if (dataHead == 1) {         
             	rec = readTextFile(fi); //read file data
             	if (endFile[fi].equals("Y")) 
                	break;

             	if (rec.length() == 1100) {            		         			
         			if (!rec.substring(0, 1).equals("0")) {
                 		showLogMessage("D", "", "Error : 此檔案 =  " + getFileName + " 首筆資料辨識碼不正確，不可轉入");
                 		rollbackDataBase();
                 		return 1;
                 	}   
             	}         
             	else {
             		showLogMessage("D", "", "Error : 此檔案 =  " + getFileName + " 首筆資料長度不正確，不可轉入");
             		rollbackDataBase();
             		return 1;
             	}         		
             }
            dataHead = 0;
            rec = readTextFile(fi);
            if (rec.trim().length() == 29) { //tailer
            	if (!rec.substring(0, 1).equals("9")) {
            		showLogMessage("D", "", " rror : 此檔案 =  " + getFileName + "尾筆資料辨識碼不正確，不可轉入 = [" + filename1 + "]");
            		rollbackDataBase();
            		return 1;
            	}
            	tTotalCnt = Integer.parseInt(rec.substring(16, 17));
            	if (tTotalCnt != hCount) {
            		showLogMessage("D", "", String.format("Error : 此檔案 =  " + getFileName + " 資料錯誤: 總筆數不符   "));
            		rollbackDataBase();
            		return 1;
            	}
            	tSuccessCnt = Integer.parseInt(rec.substring(22, 23));
            	if (tSuccessCnt != insertCnt) {
            		showLogMessage("D", "", String.format("Error : 此檔案 =  " + getFileName + " 資料錯誤: 成功筆數不符   "));
            		rollbackDataBase();
            		return 1;
            	}
            	tFailCnt = Integer.parseInt(rec.substring(28, 29));
            	if (tFailCnt != insertFCnt) {
            		showLogMessage("D", "", String.format("Error : 此檔案 =  " + getFileName + " 資料錯誤: 失敗筆數不符   "));
            		rollbackDataBase();
            		return 1;
            	}
            	getInsertCnt = getInsertCnt + insertCnt;
            	dataHead = 1;
            	hCount = 0;
            	insertCnt = 0;
            	insertFCnt = 0;
            	continue;
            }
            if (rec.trim().length() > 29){
            	if (!rec.substring(0, 1).equals("1")) {
            		showLogMessage("D", "", " Error : 此檔案 =  " + getFileName + " 明細資料辨識碼不正確，不可轉入");
            		rollbackDataBase();
            		return 1;
            	}
            	else {
            		byte[] bt = rec.getBytes("MS950");                     
                    
                    hCount++;
                    totalCnt++;
                    totalCnt2++;

                    moveData(processDataRecord(getFieldValue(rec, dt1length1), dt1));                                    
                    processDisplay(1000);
            	}
            }
            else {
            	showLogMessage("D", "", " Error : 此檔案 =  " + getFileName + " 資料長度不正確，不可轉入");
            	rollbackDataBase();
        		return 1;
            }
        }

        closeInputText(fi);
        
        closeBinaryInput();

        insertCrdFileCtl();

        renameFile(getFileName);
        
        commitDataBase();

        return 0;
    }

 // ************************************************************************
    private int moveData(Map<String, Object> map) throws Exception {
    	dateTime();    
    	
    	String tmpApplyType = "";
    	String tmpDigitsFlag = "N";
    	String tmpDigitsCode = "";
    	
    	hDcepRecno ++;
    	String tmpChar;
        tmpChar = (String) map.get("apply_no");
        hDcepApplyNo = tmpChar.trim();
        
        tmpChar = (String) map.get("act_no");
        hDcepActNo = tmpChar.trim();
        hDdtpSavingActno = tmpChar.trim();
        
        tmpChar = (String) map.get("card_ref_num");
        hDcepCardRefNum = tmpChar.trim();
        
        tmpChar = (String) map.get("col8");        
        if (!tmpChar.trim().equals("V"))
        	setValue("check_code", "D48");
        else {
        	tmpChar = (String) map.get("col45");
        	if(!tmpChar.trim().equals("000"))
        		setValue("check_code", tmpChar.trim());   
        	else
            	setValue("check_code", "");  
        }
        
        tmpChar = (String) map.get("apply_type");
        tmpApplyType = tmpChar.trim();
        
        tmpDigitsCode = comc.getSubString(hDcepActNo, 4, 7);
        
        if(tmpApplyType.equals("200") && tmpDigitsCode.equals("988")) {
        	tmpDigitsFlag = "Y";
        }
                
        sqlCmd  = "select group_code, card_type, unit_code, source_code, digital_flag ";
        sqlCmd += " from dbc_card_type ";
        sqlCmd += "where card_code = ? and digital_flag = ? ";
        setString(1, tmpApplyType);
        setString(2, tmpDigitsFlag);
        if (selectTable() > 0) {
	        hDcepGroupCode  = getValue("group_code");
	        hDcepCardType   = getValue("card_type");
	        hDcepUnitCode   = getValue("unit_code");
	        hDcepSourceCode = getValue("source_code");
	        hDcepDigitalFlag = getValue("digital_flag");
        }
        else {
        	comcr.errRtn("select_dbc_card_type not found!", "card_code =" + tmpChar.trim(), comcr.hCallBatchSeqno);
        }
        
        tmpChar = (String) map.get("apply_source");
        hDcepApplySource = tmpChar.trim();
        
        tmpChar = (String) map.get("eng_name");
        tmpChar = tmpChar.substring(0, 25);
        hEngName = tmpChar.trim();
        
        tmpChar = (String) map.get("col12");
        if(tmpChar.equals("5")) {
        	hDcepStatSendInternet= "N";
        }
        else {
        	hDcepStatSendInternet= "Y";
        }
        
        tmpChar = (String) map.get("reg_bank_no");
        hDcepRegBankNo = tmpChar;
        
        tmpChar = (String) map.get("branch");
        hDcepBranch = tmpChar;
        
        tmpChar = (String) map.get("crt_bank_no");
        hDcepCrtBankNo = tmpChar;
        
        tmpChar = (String) map.get("vd_bank_no");
        hDcepVdBankNo = tmpChar;
        
        tmpChar = (String) map.get("home_area_code1");
        hDcepHomeAreaCode1 = tmpChar.trim();
        
        tmpChar = (String) map.get("home_tel_no1");
        hDcepHomeTelNo1 = tmpChar.trim();
        
        tmpChar = (String) map.get("cellar_phone");
        hDcepCellarPhone = tmpChar.trim();
        
        tmpChar = (String) map.get("office_area_code1");
        hDcepOfficeAreaCode1 = tmpChar.trim();
        
        tmpChar = (String) map.get("office_tel_no1");
        hDcepOfficeTelNo1 = tmpChar.trim();
        
        tmpChar = (String) map.get("office_tel_ext1");
        hDcepOfficeTelExt1 = tmpChar.trim();
        
        tmpChar = (String) map.get("apply_id");
        hDcepApplyId      = tmpChar.trim();

        tmpChar = (String) map.get("birthday");
        if(tmpChar.trim().length() != 0) {
           tmpLong = Long.parseLong(tmpChar) + 19110000;
           tmpChar = Long.toString(tmpLong);
        }
        hDcepBirthday = tmpChar.trim();
        
        tmpChar = (String) map.get("sex"); 
        hDcepSex = tmpChar;        
        
        tmpChar = (String) map.get("stmt_cycle"); 
        hDcepStmCycle = tmpChar;
        
        tmpChar = (String) map.get("revolve_int_rate_year");  
//		hDcepRevolveIntRateYear = String.valueOf(((Double.parseDouble(tmpChar) / 100)));
        
        tmpChar = (String) map.get("e_mail_addr"); 
        hDcepEMailAddr = tmpChar;
        
        tmpChar = (String) map.get("card_no"); 
        hDcepCardNo = tmpChar;
        
        if(hDcepApplySource.equals("P")) {
        	hTmpCardNo = hDcepCardNo;
        	hTmpBinNo = hDcepCardNo.substring(0,6);
        }
        else {
        	hTmpCardNo = "";
        	hTmpBinNo  = "";
        }
        
        tmpChar = (String) map.get("market_agree_base");
        hDcepMarketAgreeBase = tmpChar;
        
        tmpChar = (String) map.get("chi_name"); 
        hDcepChiName = tmpChar.trim().replace("　", "");               

        tmpChar = (String) map.get("resident_zip"); 
        hDcepResidentZip  = tmpChar.trim();          
        
        tmpChar = (String) map.get("resident_addr1"); 
        hDcepResidentAddr1 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("resident_addr2"); 
        hDcepResidentAddr2 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("resident_addr3"); 
        hDcepResidentAddr3 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("resident_addr4"); 
        hDcepResidentAddr4 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("resident_addr5"); 
        hDcepResidentAddr5 = tmpChar.trim().replace("　", "");        
        
        tmpChar = (String) map.get("mail_zip"); 
        hDcepMailZip = tmpChar.trim(); 
        
        tmpChar = (String) map.get("mail_addr1"); 
        hDcepMailAddr1 = tmpChar.trim().replace("　", "");       
        
        tmpChar = (String) map.get("mail_addr2"); 
        hDcepMaiAddr2 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("mail_addr3");
        hDcepMailAddr3 = tmpChar.trim().replace("　", "");

        tmpChar = (String) map.get("mail_addr4");
        hDcepMailAddr4 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("mail_addr5");
        hDcepMailAddr5 = tmpChar.trim().replace("　", "");
      
        tmpChar = (String) map.get("company_zip"); 
        hDcepCompanyZip = tmpChar.trim();      
        
        tmpChar = (String) map.get("company_addr1"); 
        hDcepCompanyAddr1 = tmpChar.trim().replace("　", "");

        tmpChar = (String) map.get("company_addr2"); 
        hDcepCompanyAddr2 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("company_addr3");
        hDcepCompanyAddr3 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("company_addr4");
        hDcepCompanyAddr4 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("company_addr5");
        hDcepCompanyAddr5 = tmpChar.trim().replace("　", "");
        
        tmpChar = (String) map.get("ap1_apply_date");
        tmpLong = Long.parseLong(tmpChar) + 19110000;
        tmpChar = Long.toString(tmpLong);
        setValue("ap1_apply_date", tmpChar.trim());
         
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd");        
        String tmpCharLastDate = lastDay.format(dateFmt);
        sqlCmd  = "select new_extn_mm ";
        sqlCmd += " from crd_item_unit ";
        sqlCmd += "where unit_code = ?  ";
        setString(1, hDcepUnitCode);
        if (selectTable() > 0) {   
        	String hNewExtnMm = getValue("new_extn_mm"); 
        	hNewExtnMm = hNewExtnMm + String.format("%0" + (5 - hNewExtnMm.length()) + "d", 0);
        	tmpCharLastDate = String.valueOf((Integer.parseInt(tmpCharLastDate) + Integer.parseInt(hNewExtnMm)));	                
        }
        hDcepValidTo = tmpCharLastDate.trim(); //h_dcep_valid_to值取當月最後一天
        
        LocalDate firstDay = LocalDate.of(today.getYear(), today.getMonth(), 1);
        String tmpCharFirstDay = firstDay.format(dateFmt);
        hDcepValidFm = tmpCharFirstDay.trim(); //h_dcep_valid_fm值取當月第一天
        
        hDcepMailType = "4";                    

        hDcepApplyIdCode = "0";               

        if (debug == 1) {
            stderr = String.format(" 770 Read exist=[%s]no[%s]type=[%s][%s],Group=[%s][%s][%s]",
                    hDcepApplySource, hDdtpCardNo  , hDcepCardType, hDdtpTxCode
                  , hDcepGroupCode  , hDcepApplyId , hDcceCardCode);
            showLogMessage("I", "", stderr);
        }
      
      insertDbcEmap();
      insertDbcDebitTmp();
      insertCnt ++;
      
      return 0;
    }

/***********************************************************************/
int insertDbcEmap() throws Exception 
{
   hDcepNcccType = "1";

   if(hDcepDigitalFlag.equals("Y"))
     {
      hDcepMailType = "1";
     }

        setValue("batchno"         , hDcepBatchno);
        setValueDouble("recno"     , hDcepRecno);
        setValue("source"          , "1");
        setValue("seqno"           , "1");
        setValue("apply_id"        , hDcepApplyId);
        setValue("apply_id_code"   , hDcepApplyIdCode);
        setValue("pm_id"           , hDcepApplyId);
        setValue("pm_id_code"      , hDcepApplyIdCode);
        setValue("card_type"       , hDcepCardType);
        setValue("group_code"      , hDcepGroupCode);
        setValue("source_code"     , hDcepSourceCode);
        setValue("unit_code"       , hDcepUnitCode);
        setValue("birthday"        , hDcepBirthday);
        setValue("nation"          , hDcepNation);
        setValue("act_no"          , hDcepActNo);
        setValue("valid_fm"        , hDcepValidFm);
        setValue("valid_to"        , hDcepValidTo);
        setValue("sex"             , hDcepSex);
        setValue("mail_branch"     , hDcepBranch);
        setValue("reg_bank_no"     , hDcepRegBankNo);
        setValue("pm_birthday"     , hDcepBirthday);
        setValue("check_code"      , hDcepCheckCode);
        setValue("oth_chk_code"    , "0");
        setValue("crt_date"        , sysDate);
        setValue("crt_bank_no"     , hDcepCrtBankNo);
        setValue("vd_bank_no"      , hDcepVdBankNo);
        setValue("nccc_type"       , hDcepNcccType);
        setValue("branch"          , hDcepBranch);
        setValue("mod_user"        , hModUser);
        setValue("mod_time"        , sysDate + sysTime);
        setValue("mod_pgm"         , prgmId);
        setValue("apply_source"    , hDcepApplySource);
        setValue("eng_name"    , hEngName);
        setValue("home_area_code1"    , hDcepHomeAreaCode1);
        setValue("home_tel_no1"    , hDcepHomeTelNo1);
        setValue("cellar_phone"    , hDcepCellarPhone);
        setValue("office_area_code1"    , hDcepOfficeAreaCode1);
        setValue("office_tel_no1"    ,  hDcepOfficeTelNo1);
        setValue("office_tel_ext1"    , hDcepOfficeTelExt1);
        setValue("card_ref_num"    ,  hDcepCardRefNum);
        setValue("salary"    ,  hDcepSalary);
        setValue("chi_name"    ,  hDcepChiName);       
        setValue("marriage"    ,  hDcepMarriage);
        setValue("education"    ,  hDcepEducation);
        setValue("bill_apply_flag"    ,  hDcepBillApplyFlag);
        setValue("stmt_cycle"    ,  hDcepStmCycle);
        setValue("e_mail_addr"    ,  hDcepEMailAddr); 
        setValue("other_cntry_code"    ,  hDcepOtherCntryCode);
        setValue("stat_send_internet"    ,  hDcepStatSendInternet);
        setValue("check_code"     ,  hDcepCheckCode);
        setValue("apply_no"    ,  hDcepApplyNo);
//        setValue("revolve_int_rate_year"    ,  hDcepRevolveIntRateYear);
        setValue("market_agree_base"    ,  hDcepMarketAgreeBase);
        setValue("resident_zip"    ,  hDcepResidentZip);
        setValue("resident_addr1"    ,  hDcepResidentAddr1);
        setValue("resident_addr2"    ,  hDcepResidentAddr2);
        setValue("resident_addr3"    ,  hDcepResidentAddr3);
        setValue("resident_addr4"    ,  hDcepResidentAddr4);
        setValue("resident_addr5"    ,  hDcepResidentAddr5);
        setValue("mail_zip"    ,  hDcepMailZip); 
        setValue("mail_addr1"    ,  hDcepMailAddr1);
        setValue("mail_addr2"    ,  hDcepMaiAddr2);
        setValue("mail_addr3"    ,  hDcepMailAddr3);
        setValue("mail_addr4"    ,  hDcepMailAddr4);
        setValue("mail_addr5"    ,  hDcepMailAddr5);
        setValue("company_zip"    ,  hDcepCompanyZip);
        setValue("company_addr1"    ,  hDcepCompanyAddr1);
        setValue("company_addr2"    ,  hDcepCompanyAddr2);
        setValue("company_addr3"    ,  hDcepCompanyAddr3);
        setValue("company_addr4"    ,  hDcepCompanyAddr4);
        setValue("company_addr5"    ,  hDcepCompanyAddr5);
        setValue("digital_flag"     ,  hDcepDigitalFlag);
        setValue("card_no"          ,  hTmpCardNo);
        setValue("bin_no"           ,  hTmpBinNo);
        daoTable = "dbc_emap_tmp";
        insertTable();
        if (dupRecord.equals("Y")) {
           comcr.errRtn("insert_dbc_emap_tmp duplicate1", hDcepBatchno+","+hDcepRecno
                         , comcr.hCallBatchSeqno);
        }
        return 0;
}

/***********************************************************************/
    int insertDbcDebitTmp() throws Exception {
        String hTransType = "";

        hTransType = "1";

        setValue("apply_id"         , hDcepApplyId);
        setValue("apply_id_code"    , hDcepApplyIdCode);
        setValue("birthday"         , hDcepBirthday);
        setValue("sup_flag"         , "0");
        setValue("pm_id"            , hDcepApplyId);
        setValue("pm_id_code"       , hDcepApplyIdCode);
        setValue("apply_date"       , sysDate);
        setValue("batchno"          , hDcepBatchno);
        setValueDouble("recno"      , hDcepRecno);
        setValue("trans_type"       , hTransType);
        setValue("saving_actno"     , hDcepActNo);
        setValue("to_ibm_date"      , sysDate);
        setValue("rtn_ibm_date"     , sysDate);
        setValue("crt_date"         , sysDate);
        setValue("mod_user"         , hModUser);
        setValue("mod_time"         , sysDate + sysTime);
        setValue("mod_pgm"          , prgmId);
        setValue("card_no"          , hTmpCardNo);
        daoTable = "dbc_debit_tmp";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_dbc_debit_tmp duplicate2",hDcepBatchno+","+hDcepRecno
                         , comcr.hCallBatchSeqno);
        }
        return 0;
    }
 

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcB000 proc = new DbcB000();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    
    
 // ************************************************************************
    public void renameFile(String removeFileName) throws Exception {
        String tmpstr1 = checkHome + "/media/dbc/" + removeFileName;
        String tmpstr2 = checkHome + "/media/dbc/backup/" + removeFileName;
        
        if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
    }
    
 // ************************************************************************
    public int insertCrdFileCtl() throws Exception {
            
        setValue("file_name"     , getFileName);
        setValue("crt_date"      , sysDate);
        setValue("trans_in_date" , sysDate);       

        daoTable = "crd_file_ctl";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_file_ctl  error[dupRecord]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

 // ************************************************************************
    private Map processDataRecord(String[] row, String[] dT) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        int j = 0;
        for (String s : dT) {
            map.put(s.trim(), row[i]);
            // if(DEBUG == 1) showLogMessage("D",""," Data=" + s + ":[" + row[i]
            // + "]");
            i++;
        }
        return map;

    }
    
 // ************************************************************************
    public String[] getFieldValue(String rec, int[] parm) {
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
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     