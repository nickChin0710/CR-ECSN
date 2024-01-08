/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/04/24  V1.01.01  Lai        Initial                                    *
* 107/05/25  V1.09.01  詹曜維     RECS-s1070419-032 MGM介紹人ID轉碼          *
* 107/07/31  V1.10.01  吳伯榮     RECS-s1070730-062 介紹人代號小寫轉大寫     *
* 108/03/13  V1.11.01  詹曜維     RECS-s1080225-017 線上申辦受理行轉換       *
* 108/05/27  V1.11.01  Brian      update to V1.11.01                         *
* 108/12/13  V1.11.01  Rou        Update field data                         *
* 109/02/10  V1.12.01  Wilson     nccc_type = '1'                            *
* 109/11/03  V1.12.02  Wilson     mark chi_name轉碼                                                                          *
* 109/11/12  V1.12.03  Wilson     修正bug                                     *
* 109/12/17  V1.00.04    shiyuqi       updated for project coding standard   *
* 110/08/13  V1.00.05  Wilson     介紹人代號、分行相關欄位調整                                                                 * 
* 111/12/09  V1.00.06  Wilson     調整收檔路徑                                                                                                * 
* 112/01/16  V1.00.07  Wilson     有中文字的欄位增加replace處理全形空白                                            *
* 112/01/17  V1.00.08  Wilson     推廣人員相關欄位調整                                                                                 *
* 112/02/01  V1.00.09  Wilson     調整sms_amt處理邏輯                                                                            *
* 112/02/26  V1.00.10  Wilson     婚姻狀態3要轉成1                               *
* 112/03/01  V1.00.11  Wilson     修正附卡團代值                                                                                           *
* 112/03/10  V1.00.12  Wilson     insert crd_emap_tmp add revolve_int_rate_year_code*
* 112/03/20  V1.00.13  Wilson     sms_amt = 空白 -> 要發簡訊(金額0元)              *
* 112/03/22  V1.00.14  Wilson     國籍、婚姻欄位調整                                                                                   *
* 112/03/25  V1.00.15  Wilson     insert crd_emap_tmp add oth_chk_code       *
* 112/05/05  V1.00.16  Wilson     調整market_agree_base                       *
* 112/07/03  V1.00.17  Wilson     假日不執行                                                                                                   *
* 112/08/23  V1.00.18  Wilson     推廣人員相關欄位調整                                                                                 *
* 112/08/30  V1.00.19  Wilson     尾筆檢核有誤增加rollback                        *
* 112/11/11  V1.00.20  Wilson     mark檢核假日不執行                                                                                 *
* 112/11/17  V1.00.21  Wilson     信評等級補0                                   *
* 112/11/29  V1.00.22  Wilson     寫入鍵檔人員代號                                                                                        *
* 112/11/30  V1.00.23  Wilson     認同集團碼、效期迄日放空白                                                                     *
* 112/12/05  V1.00.24  Wilson     改為系統日                                                                                                   *
*****************************************************************************/
package Crd;

import com.*;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("unchecked")
public class CrdB000 extends AccessDAO {
	private String progname = "noncombo資料轉入申請書暫存檔處理112/12/05  V1.00.24 ";
	private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = null;
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;

    int debug   = 1;
    int debugD = 0;
    
    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    int totalCnt2 = 0;
    int totalFile = 0;

    String fileName = "", readData = "";
    String fileName1 = "", fileName2 = "";
    String hElgRowid = "";
    String tmpPositiveCard = "";
    String tmpSubCard = "";
    String stderr = "";

    int hHeadCnt = 0;
    
    protected final String dt1Str = "col_1, apply_no, positive_card, sub_card, introduce_id, employee_bank_no, promote_emp_no, promote_dept, col_9, market_agree_base, "
    		+ "e_news, stat_send_internet, se_id, service_type, mno_id, ur_flag, key_user1, key_user2, apply_id, group_code, sex, "
    		+ "birthday, eng_name, col_24, marriage, education, business_code, col_28, cellar_phone, service_year, salary, e_mail_addr, "
    		+ "send_pwd_flag, credit_lmt, spouse_id_no, spouse_birthday, resident_no_expire_date, other_cntry_code, "
    		+ "passport_no, passport_date, roadside_assist_apply, bill_apply_flag, home_area_code1, home_tel_no1, "
    		+ "office_area_code1, office_tel_no1, office_tel_ext1, incoming_bank_no, revolve_int_rate_year_code, resident_zip, mail_zip, company_zip, "
    		+ "co_promote_dept, corp_no, branch, act_no_l_ind, autopay_acct_bank, act_no_l, col_59, "
    		+ "sub_apply_id, sub_group_code, sub_sex, sub_birthday, sub_eng_name, sub_marriage, sub_education, "
    		+ "sub_roadside_assist_apply, sub_rel_with_pm, inst_flag, fee_code_i, seg_sym, graduation_elementarty, seg_sym, "
    		+ "spouse_name, seg_sym, chi_name, seg_sym, job_position, seg_sym, company_name, seg_sym, "
    		+ "resident_addr1, resident_addr2, resident_addr3, resident_addr4, resident_addr5, seg_sym,"
    		+ "mail_addr1, mail_addr2, mail_addr3, mail_addr4, mail_addr5, seg_sym,"
    		+ "company_addr1, company_addr2, company_addr3, company_addr4, company_addr5, seg_sym,"
    		+ "sub_chi_name, seg_sym, act_no_f_ind, act_no_f, curr_change_accout, filler, source_code, online_mark,"
    		+ "mail_type, card_type, unit_code, stmt_cycle, son_card_flag, indiv_crd_lmt, sms_amt, credit_level_new,"
    		+ "jcic_score, fee_code, end";
        
    protected final int[] de1length = { 1, 12, 1, 1, 10, 4, 10, 8, 1, 1,
    		1, 1, 32, 2, 3, 1, 6, 6, 11, 4, 1,
    		8, 50, 1, 1, 1, 4, 1, 10, 4, 6, 50,
    		1, 4, 11, 8, 8, 2,
    		20, 8, 8, 1, 4, 10,
    		4, 10, 6, 4, 4, 6, 6, 6,
    		10, 8, 4, 2, 7, 14, 20,
    		11, 4, 1, 8, 50, 1, 1,
    		8, 1, 1, 1, 3, 10, 3,
    		100, 3, 100, 3, 12, 3, 28, 3,
    		10, 10, 12, 12, 56, 3,
    		10, 10, 12, 12, 56, 3,
    		10, 10, 12, 12, 56, 3,
    		100, 3, 2, 13, 13, 6, 6, 1,
    		1, 2, 4, 2, 1, 4, 7, 2,
    		3, 1, 3,};
    protected  String[] dt1 = new String[] {};
//    String hTmpIntroduceId = "";
//    String hEmapIntroduceId = "";
    String hBatchno = "";
    double hRecno = 0;
    String hGroupCode = "";
    String hRegBankNo = "";
//    String h_introduce_no = "";
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;
    int fi;
    int foldIdx = 0;
    int fileIdx = 0;    
    List<String> listArr = new ArrayList<String>();
    List<String> foldArr = new ArrayList<String>();
    int temp = 0;
    int firstDataLength;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
		CrdB000 proc = new CrdB000();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);		
    }

    // ************************************************************************
    public int mainProcess(String[] args) {
        try {
        	dt1 = dt1Str.split(",");

            comc  = new CommCrd();
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkHome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname + "[" + args.length + "]");

            if (args.length > 2) {
                String err1 = "nCrdB000 請輸入 : callseqno";
                String err2 = "";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hBusinessDate = "";
            if (args.length > 0) {
                if (args[0].length() == 8)
                    hBusinessDate = args[0];
            }

            showLogMessage("I", "", "程式參數1=[" + hBusinessDate +"]");

            comr  = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if(comcr.hCallBatchSeqno.length() > 6) {
               if(comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                  comcr.hCallBatchSeqno = "no-call"; }
              }

            comcr.hCallRProgramCode = this.getClass().getName();
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.hCallParameterData = javaProgram;
                for (int i = 0; i < args.length; i++) {
                    comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
                }
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

            selectPtrBusinday();

            showLogMessage("I", "", "本日營業日 : [" + hBusinessDate + "] [" + hBusiChiDate + "]");	
            
//            if (checkPtrHoliday() != 0) {
//				showLogMessage("E", "", "今日為假日,不執行此程式");
//				return 0;
//            }
   
            /*** 取得檔案名稱 ***/
            selectEcsFtpLog();
            
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt2 + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            commitDataBase();

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
      // ************************************************************************

    public void selectPtrBusinday() throws Exception {

        selectSQL = "business_date  , " 
                  + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable  = "PTR_BUSINDAY";
        whereStr  = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        if (hBusinessDate.length() == 0) {
//        	hBusinessDate = getValue("business_date");
        	hBusinessDate = getValue("SYSTEM_DATE");
        }        	
        
        long hLongChiDate = Long.parseLong(hBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);        			
    }
    // ************************************************************************
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
    public int selectCrdFileCtl() throws Exception {
        selectSQL = "count(*) as all_cnt";
        daoTable = "crd_file_ctl";
        whereStr = "WHERE file_name  = ? ";

        setString(1, fileName1);
        int recCnt = selectTable();
        if (debugD == 1)
            showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
        if (getValueInt("all_cnt") > 0) {
            showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + fileName1 + "]");
            return 1;
        }

        return 0;
    }

    /***********************************************************************/
    void selectEcsFtpLog() throws Exception 
    {
        String hTempFilename = "";
        String tmpstr = String.format("%s/media/crd", comc.getECSHOME());

        tmpstr = Normalizer.normalize(tmpstr, java.text.Normalizer.Form.NFKD);

        List<String> listOfFiles = comc.listFS(tmpstr, "", "");

        for (String file : listOfFiles) {
if(debugD == 1) showLogMessage("I", "", " 888 NAME=["+file+"]"+ file.length());
                if (file.length() != 23)    
                	continue;
                if (!file.substring(0,9).equals("noncombo_"))
                    continue;
                hTempFilename = file;
                if (getFileName(hTempFilename) == 1)
                	continue;
if(debugD == 1) showLogMessage("I", "", " 888 read file=["+totalFile+"]"+ hTempFilename);
               
        }
        if (totalFile < 1) {
        	showLogMessage("I", "", "無檔案可處理");
        	
//            comcr.hCallErrorDesc = "無檔案可處理 end";
//            comcr.errRtn("無檔案可處理 end","" , comcr.hCallBatchSeqno);
        }
    }
    /**********************************************************************/
    public int getFileName(String fileNamei) throws Exception {
        String rec = "";
        checkHome = comc.getECSHOME();
        fileName1 = fileNamei;
        fileName2 = checkHome + "/media/crd/" + fileName1;

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

        showLogMessage("I", "", "888 Process file=" + fileName2);

        tmpInt = selectCrdFileCtl(); 
        if (tmpInt > 0) //The file already exists
            return 1;

        int retCode = getEmapBatchno();
        if (retCode == 1) {
            String err1 = "Get Batch No Error !!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        totalFile++;
        showLogMessage("I", "", "888 Cnt_file=[" + totalFile + "]");

        hHeadCnt    = 0;
        totalCnt      = 0;
        int dataHead = 1;
        

        while (true) {                   
        	 if (dataHead == 1) {         
             	rec = readTextFile(fi); //read file data
             	if (endFile[fi].equals("Y")) 
                	break;
             	if (rec.trim().length() == 9) {            		         			
         			if (!rec.substring(1, 2).equals("1")) {
                 		showLogMessage("D", "", " 此檔案首筆資料辨識碼不正確，不可轉入 = [" + fileName1 + "]");
                 		rollbackDataBase();
                 		return 1;
                 	}   
             	}         
             	else if (rec.trim().length() == 8) {            		         			
         			if (!rec.substring(0, 1).equals("1")) {
                 		showLogMessage("D", "", " 此檔案首筆資料辨識碼不正確，不可轉入 = [" + fileName1 + "]");
                 		rollbackDataBase();
                 		return 1;
                 	}
             	}  
             	else {
             		showLogMessage("D", "", " 此檔案首筆資料長度不正確，不可轉入 = [" + fileName1 + "]");
             		rollbackDataBase();
             		return 1;
             	}             		
             }
            dataHead = 0;
            rec = readTextFile(fi);
            if (rec.trim().length() == 6) { //tailer
            	if (!rec.substring(0, 1).equals("3")) {
            		showLogMessage("D", "", " 此檔案尾筆資料辨識碼不正確，不可轉入 = [" + fileName1 + "]");
            		rollbackDataBase();
            		return 1;
            	}
            	hHeadCnt = Integer.parseInt(rec.trim()) - 300000;
            	dataHead = 1;
            	continue;
            }                      
            byte[] bt = rec.getBytes("MS950");

            if (firstDataLength != 8)
            	rec = "2" + rec;            
            if (debugD == 1)
                showLogMessage("I", "", "8888 str=" + rec.length() + " " + rec.substring(0, 15));
            
            totalCnt++;
            totalCnt2++;
            
            temp = 0;
            temp = moveData(processDataRecord(getFieldValue(rec, de1length), dt1));  
            if (temp == 2) { //因附卡註記為Y，故要再新增一筆附卡的資料
            	moveData(processDataRecord(getFieldValue(rec, de1length), dt1));
            	temp = 0;
            }
            if (debugD == 1)
                showLogMessage("I", "", "8888 3333= " + rec.substring(0, 15));
            if (debug == 1) {
                showLogMessage("I", "", "8888 Beg Batch=" + hBatchno);
                showLogMessage("I", "", "           seq=" + hRecno);
            }
            processDisplay(1000);

        }

        closeInputText(fi);

        insertCrdFileCtl();
        
        hHeadCnt = 0;

        renameFile(fileName1);
        
        commitDataBase();

        return 0;
    }

	// ************************************************************************
    public int getEmapBatchno() throws Exception {
        selectSQL = " max(substr(batchno,7,2))  as batchno_dd ";
        daoTable = "crd_emap_tmp a";
        whereStr = "WHERE a.batchno like ? || '%' ";
        setString(1, hBusinessDate.substring(2, 8));
        int recCnt = selectTable();

        String tx2 = "1";
        if (getValueInt("batchno_dd") >= 1) {
            tx2 = Integer.toString(getValueInt("batchno_dd") + 1);
        }
        String tmpX2 = comm.fillZero(tx2, 2);
        hBatchno = hBusinessDate.substring(2, 8) + tmpX2;
        showLogMessage("D", "", " 批號 =" + hBatchno);

        return (0);
    }

    // ************************************************************************
    private int moveData(Map<String, Object> map) throws Exception {
    	String employeeNo = "";
    	String employeeId = "";
    	String staffFlag = "N";
    	String fileIntroduceId = "";
    	String filePromoteEmpNo = "";
    	String fileEmployeeBankNo = "";
    	String filePromoteDept = "";
    	String fileCoPromoteDept = "";
   	
    	dateTime();     

        hRecno++;
        hRegBankNo = "";
        
        setValue("batchno", hBatchno);
        setValueDouble("recno", hRecno);
        setValue("source", "1");
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);                

        tmpChar = (String) map.get("apply_id");
        setValue("apply_id", tmpChar.trim().substring(0, 10));
        setValue("pm_id", tmpChar.trim().substring(0, 10));
        if (debug == 1)
            showLogMessage("D", "", " Data=[" + tmpChar + "]");
        tmpChar = (String) map.get("card_type");
        setValue("card_type", tmpChar.trim());        
        tmpChar = (String) map.get("source_code");
        if (tmpChar.trim().length() < 1)
            tmpChar = "000000";
        setValue("source_code", tmpChar.trim());
        tmpChar = (String) map.get("corp_no");
        setValue("corp_no", tmpChar.trim());
       
        tmpChar1 = (String) map.get("chi_name");
//        tmp_char1 = comc.bankEncode(tmp_char);
//        if (tmp_char1.trim().length() < 1)
//            tmp_char1 = " ";
        setValue("chi_name", tmpChar1.trim().replace("　", ""));
        tmpChar = (String) map.get("eng_name");
        tmpChar = tmpChar.substring(0, 25);
        setValue("eng_name", tmpChar.trim());  
		tmpChar = (String) map.get("group_code");
		hGroupCode = tmpChar.trim();
		setValue("group_code", hGroupCode);
		if (hGroupCode.equals("1230") || hGroupCode.equals("1330"))
	        setValue("fl_flag", "Y");
		else
			setValue("fl_flag", "N");
        tmpChar = (String) map.get("service_year");
        setValue("service_year", tmpChar.trim());
        tmpChar = (String) map.get("education");
        setValue("education", tmpChar.trim());
        tmpChar = (String) map.get("salary");
        tmpChar = String.valueOf(Integer.parseInt(tmpChar.trim()) * 10000);
        setValue("salary", tmpChar.trim());
        tmpChar = (String) map.get("resident_zip");
        setValue("resident_zip", tmpChar.trim());
        tmpChar = (String) map.get("resident_addr1");
        setValue("resident_addr1", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("resident_addr2");
        setValue("resident_addr2", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("resident_addr3");
        setValue("resident_addr3", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("resident_addr4");
        setValue("resident_addr4", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("resident_addr5");
        setValue("resident_addr5", tmpChar.trim().replace("　", ""));

        tmpChar = (String) map.get("mail_zip");
        setValue("mail_zip", tmpChar.trim());
        tmpChar = (String) map.get("mail_addr1");
        setValue("mail_addr1", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("mail_addr2");
        setValue("mail_addr2", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("mail_addr3");
        setValue("mail_addr3", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("mail_addr4");
        setValue("mail_addr4", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("mail_addr5");
        setValue("mail_addr5", tmpChar.trim().replace("　", ""));
        
        tmpChar = (String) map.get("company_zip");
        setValue("company_zip", tmpChar.trim());
        tmpChar = (String) map.get("company_addr1");
        setValue("company_addr1", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("company_addr2");
        setValue("company_addr2", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("company_addr3");
        setValue("company_addr3", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("company_addr4");
        setValue("company_addr4", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("company_addr5");
        setValue("company_addr5", tmpChar.trim().replace("　", ""));


        tmpChar = (String) map.get("company_name"); 
        setValue("company_name", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("job_position");
        setValue("job_position", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("home_area_code1");
        setValue("home_area_code1", tmpChar.trim());
        tmpChar = (String) map.get("home_tel_no1");
        setValue("home_tel_no1", tmpChar.trim());
        tmpChar = (String) map.get("office_area_code1");
        setValue("office_area_code1", tmpChar.trim());
        tmpChar = (String) map.get("office_tel_no1");
        setValue("office_tel_no1", tmpChar.trim());
        tmpChar = (String) map.get("office_tel_ext1");
        setValue("office_tel_ext1", tmpChar.trim());
        tmpChar = (String) map.get("cellar_phone");
        setValue("cellar_phone", tmpChar.trim());
        tmpChar = (String) map.get("e_mail_addr");
        setValue("e_mail_addr", tmpChar.trim());
        tmpChar = (String) map.get("fee_code");
        setValue("fee_code", tmpChar.trim());
        tmpChar = (String) map.get("business_code");
        setValue("business_code", tmpChar.trim());
        
        filePromoteDept = (String) map.get("promote_dept");
        filePromoteDept = filePromoteDept.trim();
        fileCoPromoteDept = (String) map.get("co_promote_dept");
        fileCoPromoteDept = fileCoPromoteDept.trim();
        filePromoteEmpNo = (String) map.get("promote_emp_no");
        filePromoteEmpNo = filePromoteEmpNo.trim();
        fileEmployeeBankNo = (String) map.get("employee_bank_no");
        fileEmployeeBankNo = fileEmployeeBankNo.trim();
        fileIntroduceId = (String) map.get("introduce_id"); 
        fileIntroduceId = fileIntroduceId.trim();
        
        if (fileIntroduceId.length() == 6) {
	        selectSQL = "id,employ_no ";
	        daoTable  = "crd_employee ";
	        whereStr  = "where substr(acct_no, 8) = ? ";
	        setString(1, fileIntroduceId);
	        int recCnt = selectTable();
	        if (recCnt > 0) {
	        	employeeId = getValue("id");
	        	employeeNo = getValue("employ_no");
	        	staffFlag = "Y";
	        	
	        	setValue("clerk_id", employeeId);
	        	setValue("introduce_emp_no", employeeNo);
	        	setValue("introduce_id", "");	        	
	        }
	        else {
	        	setValue("clerk_id", "");	        	
	        	setValue("introduce_emp_no", "");
	        	setValue("introduce_id", fileIntroduceId);
	        }	        	
        }
        else {
        	setValue("clerk_id", "");	        	
        	setValue("introduce_emp_no", "");
        	setValue("introduce_id", fileIntroduceId);
        }
                                                
        if(staffFlag.equals("Y")) {
        	setValue("promote_dept", fileEmployeeBankNo);
        }        		
        else if(fileCoPromoteDept.length() > 0) {
        	setValue("promote_dept", fileCoPromoteDept);
        }
        else {
        	setValue("promote_dept", filePromoteDept);
        }
        
        setValue("promote_emp_no", filePromoteEmpNo);
                
        tmpChar = (String) map.get("incoming_bank_no");
    	setValue("reg_bank_no", tmpChar.trim());
         
        tmpChar = (String) map.get("ur_flag");
        setValue("ur_flag", tmpChar.trim());
        setValue("APR_DATE", hBusinessDate);
        setValue("APR_USER" , "CrdB000");
        
        tmpChar = (String) map.get("key_user1");
        setValue("police_no1", tmpChar.trim());
                
        tmpChar = (String) map.get("key_user2");
        setValue("police_no2", tmpChar.trim());
        
        tmpChar = hBusiChiDate.substring(0, 5);
        if (tmpChar.trim().length() > 0) {
            tmpLong = Long.parseLong(tmpChar) + 191100;
            tmpChar = Long.toString(tmpLong) + "01";
        }
        setValue("valid_fm", tmpChar.trim());
        
        tmpChar = (String) map.get("apply_no");
        setValue("apply_no", tmpChar.trim());        
        tmpPositiveCard = (String) map.get("positive_card"); //正卡註記
        tmpSubCard = (String) map.get("sub_card"); //附卡註記
        
        /*若正卡註記為Y，附卡註記為Y => 新增兩筆資料(一筆為正卡，一筆為附卡)
         *temp = 0，所有資料欄位均放正卡資料
         *if (temp == 3)裡的資料欄位要放附卡的資料，其餘資料欄位不變*/

        if (tmpPositiveCard.equals("Y") && tmpSubCard.equals("Y")) {
        	temp ++;
        	
        	if (temp == 3) {
        		setValue("sup_flag", "1");
            	tmpChar = (String) map.get("sub_apply_id");
            	setValue("apply_id", tmpChar.trim().substring(0, 10));
            	tmpChar = (String) map.get("apply_id");
            	setValue("pm_id", tmpChar.trim().substring(0, 10));            	
            	tmpChar = (String) map.get("sub_group_code");
            	hGroupCode = tmpChar.trim();
                setValue("group_code", tmpChar.trim());
                tmpChar = (String) map.get("sub_sex");
            	setValue("sex", tmpChar.trim());
            	tmpChar = (String) map.get("sub_birthday");
            	setValue("birthday", tmpChar.trim());
            	tmpChar = (String) map.get("sub_eng_name");
            	tmpChar = tmpChar.substring(0, 25);
            	setValue("eng_name", tmpChar.trim());   
            	tmpChar = (String) map.get("sub_chi_name");
            	setValue("chi_name", tmpChar.trim().replace("　", ""));
            	
            	tmpChar = (String) map.get("sub_marriage");
            	if(tmpChar.trim().equals("3")) {
            		tmpChar = "1";
            	}
            	setValue("marriage", tmpChar.trim());
            	
            	tmpChar = (String) map.get("sub_education");
            	setValue("education", tmpChar.trim());
            	tmpChar = (String) map.get("sub_roadside_assist_apply");
            	setValue("roadside_assist_apply", tmpChar.trim());
            	tmpChar = (String) map.get("sub_rel_with_pm");
            	setValue("rel_with_pm", tmpChar.trim());
        	}
        	else {
        		setValue("sup_flag", "0");
            	tmpChar = (String) map.get("sex");
            	setValue("sex", tmpChar.trim());
            	tmpChar = (String) map.get("birthday");
            	setValue("birthday", tmpChar.trim());
            	tmpChar = (String) map.get("eng_name");
            	tmpChar = tmpChar.substring(0, 25);
            	setValue("eng_name", tmpChar.trim());  
            	
            	tmpChar = (String) map.get("marriage");
            	if(tmpChar.trim().equals("3")) {
            		tmpChar = "1";
            	}
            	setValue("marriage", tmpChar.trim());
            	
            	tmpChar = (String) map.get("education");
            	setValue("education", tmpChar.trim());
            	tmpChar = (String) map.get("roadside_assist_apply");
            	setValue("roadside_assist_apply", tmpChar.trim());
            	setValue("rel_with_pm", "");            	            	
        	}
        }
        
        if (tmpPositiveCard.equals("Y") && tmpSubCard.equals("N")) {
        	setValue("sup_flag", "0");
    	    tmpChar = (String) map.get("sex");
    	    setValue("sex", tmpChar.trim());
    	    tmpChar = (String) map.get("birthday");
    	    setValue("birthday", tmpChar.trim());
    	    tmpChar = (String) map.get("eng_name");
    	    tmpChar = tmpChar.substring(0, 25);
    	    setValue("eng_name", tmpChar.trim());
    	    
    	    tmpChar = (String) map.get("marriage");
        	if(tmpChar.trim().equals("3")) {
        		tmpChar = "1";
        	}
    	    setValue("marriage", tmpChar.trim());
    	    
    	    tmpChar = (String) map.get("education");
    	    setValue("education", tmpChar.trim());
    	    tmpChar = (String) map.get("roadside_assist_apply");
    	    setValue("roadside_assist_apply", tmpChar.trim());
    	    setValue("rel_with_pm", "");    	    
        }         	
                    
        if (tmpPositiveCard.equals("N") && tmpSubCard.equals("Y")) {
        	setValue("sup_flag", "1");
        	tmpChar = (String) map.get("sub_apply_id");
        	setValue("apply_id", tmpChar.trim().substring(0, 10));
        	tmpChar = (String) map.get("apply_id");
        	setValue("pm_id", tmpChar.trim().substring(0, 10));
        	tmpChar = (String) map.get("sub_group_code");
        	hGroupCode = tmpChar.trim();
            setValue("group_code", tmpChar.trim());
            tmpChar = (String) map.get("sub_sex");
        	setValue("sex", tmpChar.trim());
        	tmpChar = (String) map.get("sub_birthday");
        	setValue("birthday", tmpChar.trim());
        	tmpChar = (String) map.get("sub_eng_name");
        	tmpChar = tmpChar.substring(0, 25);
        	setValue("eng_name", tmpChar.trim());   
        	tmpChar = (String) map.get("sub_chi_name");
        	setValue("chi_name", tmpChar.trim().replace("　", ""));
        	
        	tmpChar = (String) map.get("sub_marriage");
        	if(tmpChar.trim().equals("3")) {
        		tmpChar = "1";
        	}
        	setValue("marriage", tmpChar.trim());
        	
        	tmpChar = (String) map.get("sub_education");
        	setValue("education", tmpChar.trim());
        	tmpChar = (String) map.get("sub_roadside_assist_apply");
        	setValue("roadside_assist_apply", tmpChar.trim());
        	tmpChar = (String) map.get("sub_rel_with_pm");
        	setValue("rel_with_pm", tmpChar.trim());
        }
        
        tmpChar = (String) map.get("mail_type");
        setValue("mail_type", tmpChar.trim());
        tmpChar = (String) map.get("credit_lmt");
        tmpChar = String.valueOf(Integer.parseInt(tmpChar.trim()) * 10000);
        setValue("credit_lmt", tmpChar.trim());
        
        tmpChar = (String) map.get("son_card_flag");
        setValue("son_card_flag", tmpChar.trim()); 
        if (tmpChar.equals("Y")) {
        	tmpChar = (String) map.get("indiv_crd_lmt");
            tmpChar = String.valueOf(Integer.parseInt(tmpChar.trim()) * 10000);
        	setValue("indiv_crd_lmt", tmpChar.trim());
        }
        else
        	setValue("indiv_crd_lmt", "0");
        
        tmpChar = (String) map.get("online_mark");
        setValue("online_mark", tmpChar.trim());
        tmpChar = (String) map.get("stmt_cycle");
        setValue("stmt_cycle", tmpChar.trim());
        tmpChar = (String) map.get("other_cntry_code");
        setValue("other_cntry_code", tmpChar.trim());
        if (tmpChar.trim().equals("")||tmpChar.trim().equals("TW"))
        	setValue("nation", "1");
        else
        	setValue("nation", "2");
        tmpChar = (String) map.get("passport_no");
        setValue("passport_no", tmpChar.trim());
        setValue("nccc_type", "1");              
        tmpChar = (String) map.get("branch");
        setValue("branch", tmpChar.trim());
        tmpChar = (String) map.get("spouse_id_no");
        setValue("spouse_id_no", tmpChar.trim());
        tmpChar = (String) map.get("spouse_birthday");
        setValue("spouse_birthday", tmpChar.trim());
        tmpChar = (String) map.get("resident_no_expire_date");
        setValue("resident_no_expire_date", tmpChar.trim());
        tmpChar = (String) map.get("passport_date");
        setValue("passport_date", tmpChar.trim()); 
        tmpChar = (String) map.get("bill_apply_flag");
        setValue("bill_apply_flag", tmpChar.trim());         
        tmpChar = (String) map.get("autopay_acct_bank");
        setValue("autopay_acct_bank", tmpChar.trim());
        tmpChar = (String) map.get("inst_flag");
        setValue("inst_flag", tmpChar.trim());
        tmpChar = (String) map.get("fee_code_i");
        setValue("fee_code_i", tmpChar.trim());
        tmpChar = (String) map.get("graduation_elementarty");
        setValue("graduation_elementarty", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("spouse_name"); 
        setValue("spouse_name", tmpChar.trim().replace("　", ""));     
        tmpChar = (String) map.get("curr_change_accout"); 
        setValue("curr_change_accout", tmpChar.trim());
        
        tmpChar = (String) map.get("credit_level_new");
        tmpChar = tmpChar.trim();
        if(tmpChar.length() < 2 && !tmpChar.equals("D")) {
        	tmpChar = "0" + tmpChar;
        }
        setValue("credit_level_new", tmpChar.trim());          
  
        tmpChar = (String) map.get("market_agree_base");
        setValue("market_agree_base", tmpChar.trim());
        tmpChar = (String) map.get("e_news");
        tmpChar = tmpChar.equals("") ? "Y" : tmpChar;
        setValue("e_news", tmpChar.trim());
        tmpChar = (String) map.get("stat_send_internet");
        tmpChar = tmpChar.equals("") ? "Y" : tmpChar;
        setValue("stat_send_internet", tmpChar.trim());
        tmpChar = (String) map.get("act_no_f");
        setValue("act_no_f", tmpChar.trim());
        tmpChar = (String) map.get("act_no_f_ind");
        tmpChar = tmpChar.substring(0, 1);
        switch(tmpChar) {
        case "1" :
        	setValue("act_no_f_ind", "2");
        	break;
        case "2" :
        	setValue("act_no_f_ind", "1");
        	break;
        }        
        tmpChar = (String) map.get("act_no_l");
        setValue("act_no_l", tmpChar.trim());
        tmpChar = (String) map.get("act_no_l_ind");
        tmpChar = tmpChar.substring(0, 1);
        switch(tmpChar) {
        case "0" :
        	setValue("act_no_l_ind", "");
        	break;
        case "1" :
        	setValue("act_no_l_ind", "2");
        	break;
        case "2" :
        	setValue("act_no_l_ind", "1");
        	break;
        }
        tmpChar = (String) map.get("se_id");
        setValue("se_id", tmpChar.trim());
        tmpChar = (String) map.get("service_type");
        setValue("service_type", tmpChar.trim());
        tmpChar = (String) map.get("mno_id");
        setValue("mno_id", tmpChar.trim());
                   
        setValue("unit_code", "");
        setValue("valid_to", "");

        tmpChar = (String) map.get("sms_amt");
        if(tmpChar.trim() == null || tmpChar.trim().equals("")) {
        	tmpChar = "0";
        }
        setValue("sms_amt", tmpChar);
        
        tmpChar = (String) map.get("revolve_int_rate_year_code");
        tmpChar = tmpChar.trim();
        setValue("revolve_int_rate_year_code", tmpChar.trim());
        if(!tmpChar.equals("")) {
        	tmpDoub = Double.parseDouble(tmpChar.trim()) * 0.01;
            tmpChar = Double.toString(tmpDoub);
        }        
        setValue("revolve_int_rate_year", tmpChar.trim());
        tmpChar = (String) map.get("send_pwd_flag");
        if (!tmpChar.equals("Y")) {
            if (tmpChar.equals("1")) {
                tmpChar = "Y";
            } else {
                tmpChar = "N";
            }
        }
        setValue("send_pwd_flag", tmpChar.trim());
        tmpChar = (String) map.get("jcic_score");
        if (tmpChar.trim().length() < 1)
            tmpDoub = 0;
        else
            tmpDoub = Double.parseDouble((String) map.get("jcic_score"));
        setValueDouble("jcic_score", tmpDoub);  
        
        setValue("oth_chk_code" , "0");

        insertCrdEmapTmp();
    	temp ++;
    	
        return temp;
    }
    // ************************************************************************

//    public int selectCrdIdno() throws Exception
//    {
//        hTmpIntroduceId = String.format("%010d", Integer.parseInt(hEmapIntroduceId.trim(),16));
//
//        selectSQL = "id_no ";
//        daoTable  = "crd_idno ";
//        whereStr  = "WHERE id_p_seqno = ? ";
//        setString(1, hTmpIntroduceId);
//        int recCnt = selectTable();
//if(debug == 1)
//   showLogMessage("I", "", "  select idno="+hTmpIntroduceId+","+hEmapIntroduceId);
//        if (notFound.equals("Y")) {
//            String err1 = "select_crd_idno     error!" + hTmpIntroduceId;
//            String err2 = "";
//            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
//        }
//        hEmapIntroduceId = getValue("id_no");
//
//        return 0;
//    }
//
    // ************************************************************************
    private Map processDataRecord(String[] row, String[] dt) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        int j = 0;
        for (String s : dt) {
            map.put(s.trim(), row[i]);
            // if(DEBUG == 1) showLogMessage("D",""," Data=" + s + ":[" + row[i]
            // + "]");
            i++;
        }
        return map;

    }

    // ************************************************************************
    public int insertActCmuAps() throws Exception {

        daoTable = "act_cmu_aps";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_act_cmu_aps  error[dupRecord]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    public int insertCrdFileCtl() throws Exception {
        if (hHeadCnt != totalCnt) {
            showLogMessage("I", "", String.format(" 批號 [%s]", fileName2));
            comcr.errRtn(String.format("實際[%d]筆與header[%d]筆不合\n\n", totalCnt, hHeadCnt), "", comcr.hCallBatchSeqno);
        }
            
        setValue("file_name"     , fileName1);
        setValueInt("head_cnt"   , hHeadCnt);
        setValueInt("record_cnt" , totalCnt);
        setValue("crt_date"      , sysDate);
        setValue("trans_in_date" , sysDate);
        
        if (hHeadCnt == totalCnt)
            setValue("err_proc_code", "");
        else
            setValue("err_proc_code", "E");

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
    public int insertCrdEmapTmp() throws Exception {

        tmpChar = selectPtrGroupCode(hGroupCode);
        if (debug == 1)
            showLogMessage("I", "", "     888888 group=[" + hGroupCode + "]");
        
        setValue("combo_indicator", tmpChar.trim());

        daoTable = "crd_emap_tmp";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_emap_tmp error[dupRecord]";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
    }

    // ************************************************************************
    private String selectPtrGroupCode(String groupCode) throws Exception {
        selectSQL = "case when combo_indicator='' then 'N' else combo_indicator " 
                  + " end as combo_indicator ";
        daoTable = "ptr_group_code";
        whereStr = "WHERE group_code = ? ";

        if (debugD == 1)
            showLogMessage("I", "", "     888888 group=[" + groupCode + "]");

        setString(1, hGroupCode);
        int recCnt = selectTable();
        if (notFound.equals("Y")) {
            String err1 = "select_ptr_group_code error="+groupCode;
            String err2 = groupCode;
//            comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
        }

        tmpChar = getValue("combo_indicator");
        if (debug == 1)
            showLogMessage("I", "", "     7777777RTN  =[" + tmpChar + "]");
        return tmpChar;
    }

    // ************************************************************************
    public String[] getFieldValue(String rec, int[] parm) {
    	int x = 1;
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
//  if(DEBUG == 1) showLogMessage("I", "", "     getFieldValue=[" + y + "]" + i);
        }        
        return ss;
    }

    // ************************************************************************
    public void renameFile(String removeFileName) throws Exception {
        String tmpstr1 = checkHome + "/media/crd/" + removeFileName;
        String tmpstr2 = checkHome + "/media/crd/backup/" + removeFileName;
        
        if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
    }
    // ************************************************************************

} // End of class FetchSample
