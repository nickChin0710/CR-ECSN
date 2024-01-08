/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  108/11/08  V1.01.01  Rou         Initial                                  *
*  109/02/10  V1.12.01  Wilson     nccc_type = '1'                           *
*  109/03/19  V1.12.01  Rou         update data                            
*  109/03/27  V1.12.01  Rou         modify  value                            *
*  109/03/31  V1.13.01  Wilson      寫入crd_emap_tmp欄位調整                                                         *
*  109/12/17  V1.00.02    shiyuqi       updated for project coding standard  *
*  110/08/13  V1.00.03  Wilson     介紹人代號相關欄位調整                                                                          *
*  111/04/18  V1.00.04  Wilson     check_code D13 -> D12                     *
*  111/07/22  V1.00.05  Ryan       檔案格式調整                                                                                              *
*  111/12/22  V1.00.06  Wilson     調整收檔路徑                                                                                              *
*  112/01/16  V1.00.07  Wilson     有中文字的欄位增加replace處理全形空白                                         *
*  112/01/17  V1.00.08  Wilson     推廣人員相關欄位調整                                                                               *
*  112/02/26  V1.00.09  Wilson     婚姻狀態3要轉成1                               *
*  112/03/15  V1.00.10  Wilson     insert crd_file_ctl add check_code = ""   *
*  112/03/20  V1.00.11  Wilson     sms_amt = 空白 -> 要發簡訊(金額0元)              *
*  112/03/22  V1.00.12  Wilson     國籍、婚姻欄位調整                                                                                   *
*  112/03/25  V1.00.13  Wilson     insert crd_emap_tmp add oth_chk_code      *
*  112/07/03  V1.00.14  Wilson     假日不執行                                                                                                   *
*  112/08/25  V1.00.15  Wilson     推廣人員相關欄位調整                                                                                 *
*  112/08/30  V1.00.16  Wilson     尾筆檢核有誤增加rollback                        *
*  112/11/11  V1.00.17  Wilson     mark檢核假日不執行                                                                                 *
*  112/11/12  V1.00.18  Wilson     取消mark檢核假日不執行                                                                         *
*  112/11/17  V1.00.19  Wilson     信評等級補0                                   *
*  112/11/30  V1.00.20  Wilson     認同集團碼、效期迄日放空白                                                                     *
*  112/12/05  V1.00.21  Wilson     改為系統日                                                                                                   *
*****************************************************************************/
package Crd;

import com.*;

import java.text.Normalizer;
import java.util.*;

@SuppressWarnings("unchecked")
public class CrdB001 extends AccessDAO {
	private String progname = "combo資料轉入申請書暫存檔處理 112/12/05  V1.00.21 ";
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
    String hEflgRowid = "";
    String stderr = "";

    int hHeadCnt = 0;
    protected final String dt1Str = "col1, ap1_apply_date, col3, col4, apply_no, act_no, card_ref_num, col8, group_code, col10, eng_name," 
    	  + "bill_apply_flag, act_no_l_ind, autopay_acct_bank, act_no_l, reg_bank_no, branch, crt_bank_no, vd_bank_no,"
    	  + "home_area_code1, home_tel_no1, cellar_phone, office_area_code1, office_tel_no1, office_tel_ext1, business_code,"
    	  + "service_year, salary, apply_id, birthday, sex, marriage, education, spouse_id_no, spouse_birthday, credit_lmt,"
    	  + "stmt_cycle, revolve_int_rate_year, e_mail_addr, introduce_id, send_pwd_flag, resident_no_expire_date, col43,"
    	  + "col44, col45, col46, col47, other_cntry_code, passport_no, passport_date, corp_no, market_agree_base,"
    	  + "promote_dept, promote_emp_no, stat_send_internet, source_code, card_type, unit_code, son_card_flag, indiv_crd_lmt,"
//    	  + "sms_amt, credit_level_new, jcic_score, fee_code, inst_flag, fee_code_i, roadside_assist_apply, ur_flag, e_news, chi_name,"
		  + "sms_amt, credit_level_new, jcic_score, fee_code, inst_flag, fee_code_i, roadside_assist_apply, ur_flag, e_news, col70, col71, chi_name," 
    	  + "resident_zip, resident_addr1, resident_addr2, resident_addr3, resident_addr4, resident_addr5,"
    	  + "mail_zip, mail_addr1, mail_addr2, mail_addr3, mail_addr4, mail_addr5,"
    	  + "company_zip, company_addr1, company_addr2, company_addr3, company_addr4, company_addr5,"
//    	  + "company_name, job_position, spouse_name, graduation_elementarty, filter";
    	  + "company_name, job_position, spouse_name, graduation_elementarty";
        
    protected final int[] dt1length = { 1, 7, 2, 6, 12, 13, 2, 1, 4, 1, 26,
    		1, 2, 7, 16, 4, 4, 4, 4,
    		4, 10, 10, 4, 10, 6, 4,
    		4, 6, 11, 8, 1, 1, 1, 11, 8, 3,
    		2, 4, 30, 10, 1, 8, 1, 4,
    		3, 16, 20, 2, 20, 8, 8, 1,
    		8, 10, 1, 6, 2, 4, 1, 4,
//    		7, 2, 3, 1, 1, 1, 8, 1, 1, 102,
    		7, 2, 3, 1, 1, 1, 8, 1, 1, 1, 105, 102,
    		6, 10, 10, 12, 12, 56,
    		6, 10, 10, 12, 12, 56,
    		6, 10, 10, 12, 12, 56,
//    		30, 14, 102, 10, 106};
    		30, 14, 102, 10};
    
    protected  String[] dt1 = new String[] {};

    String hBatchno = "";
    double hRecno = 0;
    String hGroupCode = "";
    String hRegBankNo = "";
    String hIntroduceNo = "";
    String tmpchar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;
    int totalTemp;
    int successTemp;
    int errorTemp;
    int hErrorCnt;
    int hSuccessCnt;

    int fi;
    List<String> listArr = new ArrayList<String>();
    List<String> foldArr = new ArrayList<String>();

    // ************************************************************************

    public static void main(String[] args) throws Exception {
		CrdB001 proc = new CrdB001();
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
                String err1 = "nCrdB001 請輸入 : callseqno";
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
            
            if (checkPtrHoliday() != 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }

            /*** 取得檔案名稱 ***/
            selectEcsFtpLog();
            
            comcr.hCallErrorDesc = String.format("程式執行結束, 筆數 = [ %d ] ", totalCnt2);
        	showLogMessage("I", "", "程式執行結束,筆數 = [ " + totalCnt2 + " ]");
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
        whereStr  = "FETCH FIRST 1 ROW ONLY"; /*只取前一行*/

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
    private String selectAddMonth(String inDate, int idx) throws Exception {
        selectSQL = "to_char(add_months(to_date( ? ,'yyyymmdd'), ? ),'yyyymmdd')  as out_date";
        daoTable = "sysibm.dual";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        setString(1, inDate);
        setInt(2, idx);
        selectTable();

        return getValue("out_date");
    }

    // ************************************************************************
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
                if (file.length() != 20)    continue; 
                if (!file.substring(0, 6).equals("combo_"))
                    continue;
                hTempFilename = file;
                if (getFilName(hTempFilename) == 1)
                	continue;
if(debugD == 1) showLogMessage("I", "", " 888 read file=["+totalFile+"]"+ hTempFilename);

//                if (totalCnt > 0) {
//                	comcr.h_call_error_desc = String.format("程式執行結束, 筆數 = [ %d ] ", totalCnt);
//                	showLogMessage("I", "", "程式執行結束,筆數 = [ " + totalCnt + " ]");
//                }
//                commitDataBase();
        }
        if (totalFile < 1) {
        	showLogMessage("I", "", "無檔案可處理");
        	
//            comcr.hCallErrorDesc = "無檔案可處理 end";
//            comcr.errRtn("無檔案可處理 end","" , comcr.hCallBatchSeqno);
        }
    }
    /**********************************************************************/
    public int getFilName(String fileNameI) throws Exception {
        String rec = "";
        checkHome = comc.getECSHOME();
        fileName1 = fileNameI;
        fileName2 = checkHome + "/media/crd/" + fileName1;
if(debug == 1) showLogMessage("I", "", "Process file=" + fileName2);

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

        tmpInt = selectCrdFileCtl();
        if (tmpInt > 0)
            return 1;

        int retCode = getEmapBatchno();
        if (retCode == 1) {
            String err1 = "Get Batch No Error !!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        totalFile++;
        showLogMessage("I", "", "888 Cnt_file=[" + totalFile + "]");

        hHeadCnt = 0;  
        totalCnt = 0;
        int dataHead = 1;
        
        while (true) {
        	if (dataHead == 1) {          	
             	rec = readTextFile(fi); //read file data   
             	if (endFile[fi].equals("Y")) 
                	break;

             	if (rec.length() == 1100) {            		         			
         			if (!rec.substring(0, 1).equals("0")) {
                 		showLogMessage("D", "", "Error : 此檔案 =  " + fileName1 + " 首筆資料辨識碼不正確，不可轉入");
                 		rollbackDataBase();
                 		return 1;
                 	}   
             	}         
             	else {
             		showLogMessage("D", "", "Error : 此檔案 =  " + fileName1 + " 首筆資料長度不正確，不可轉入");
             		rollbackDataBase();
             		return 1;
             	}             		
             }
            dataHead = 0;
            rec = readTextFile(fi);
            if (rec.trim().length() == 29) { //tailer total count
            	if (!rec.substring(0, 1).equals("9")) {
            		showLogMessage("D", "", " Error : 此檔案 =  " + fileName1 + " 尾筆資料辨識碼不正確，不可轉入");
            		rollbackDataBase();
            		return 1;
            	}
            	hHeadCnt = Integer.parseInt(rec.substring(11, 17)) - 000000;
            	hSuccessCnt = Integer.parseInt(rec.substring(17, 23)) - 000000;
            	hErrorCnt = Integer.parseInt(rec.substring(23, 29)) - 000000;
            	dataHead = 1;
            	continue;
            }                             
            if (rec.trim().length() > 29){
            	if (!rec.substring(0, 1).equals("1")) {
            		showLogMessage("D", "", " Error : 此檔案 =  " + fileName1 + " 明細資料辨識碼不正確，不可轉入");
            		rollbackDataBase();
            		return 1;
            	}
            	else {
            		byte[] bt = rec.getBytes("MS950");

                    if (debugD == 1)
                        showLogMessage("I", "", "8888 str=" + rec.length() + " " + rec.substring(0, 15));
                    
                    totalCnt ++;
                    totalCnt2 ++;
                    totalTemp ++;

                    if (moveData(processDataRecord(getFieldValue(rec, dt1length), dt1)) == 1)            
                    	return 1;

                    processDisplay(1000);
            	}	            	
            }           
            else {
            	showLogMessage("D", "", " Error : 此檔案 =  " + fileName1 + " 資料長度不正確，不可轉入");
            	rollbackDataBase();
        		return 1;
            }
        }

        closeInputText(fi);       

        if (inserCrdFileCtl() == 1)
        	return 1;
        
        hHeadCnt = 0;
    	hSuccessCnt = 0;
    	hErrorCnt = 0;
    	successTemp = 0;
    	errorTemp = 0;
    	totalTemp = 0;

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

        String tX2 = "1";
        if (getValueInt("batchno_dd") >= 1) {
            tX2 = Integer.toString(getValueInt("batchno_dd") + 1);
        }
        String tmpX2 = comm.fillZero(tX2, 2);
        hBatchno = hBusinessDate.substring(2, 8) + tmpX2;
        showLogMessage("D", "", " 批號 =" + hBatchno);

        return (0);
    }

    // ************************************************************************
    int moveData(Map<String, Object> map) throws Exception {
    	String employeeNo = "";
    	String employeeId = "";
    	String employeeBankNo = "";
    	String staffFlag = "N";
    	String fileIntroduceId = "";
    	String filePromoteEmpNo = "";
    	String filePromoteDept = "";
    	
        dateTime();                
        
        hRecno ++;
        setValue("batchno", hBatchno);
        setValueDouble("recno", hRecno);
        setValue("source", "1");
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);       
        setValue("apr_date", hBusinessDate);
        setValue("apr_user" , "CrdB001");
        
if(debug == 1)        
        showLogMessage("D", "", " 8888 01=[" + hBatchno + "]");
                 
		tmpChar = (String) map.get("apply_no");
		setValue("apply_no", tmpChar.trim());
		tmpChar = (String) map.get("act_no");
        setValue("act_no", tmpChar.trim());
        tmpChar = (String) map.get("card_ref_num");
        setValue("card_ref_num", tmpChar.trim());
        
        tmpChar = (String) map.get("col8");        
        if (!tmpChar.trim().equals("C"))
        	setValue("check_code", "D12");
        else {
        	tmpChar = (String) map.get("col45");
        	if(!tmpChar.trim().equals("000"))
        		setValue("check_code", tmpChar.trim());   
        	else
            	setValue("check_code", "");  
        }
        
        tmpChar = (String) map.get("group_code");
        hGroupCode = tmpChar.trim();
        setValue("group_code", tmpChar.trim());   
        tmpChar = (String) map.get("eng_name");
        tmpChar = tmpChar.substring(0, 25);
        setValue("eng_name", tmpChar.trim());
        showLogMessage("I", "", " eng_name = [ " + tmpChar.trim() + " ]");
        tmpChar = (String) map.get("bill_apply_flag");
        setValue("bill_apply_flag", tmpChar.trim());        
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
        setValue("act_no_l_ind", tmpChar.trim());
        tmpChar = (String) map.get("autopay_acct_bank");
        setValue("autopay_acct_bank", tmpChar.trim());
        tmpChar = (String) map.get("act_no_l");
        setValue("act_no_l", tmpChar.trim());
        tmpChar = (String) map.get("reg_bank_no");
    	setValue("reg_bank_no", tmpChar.trim());
    	tmpChar = (String) map.get("branch");
        setValue("branch", tmpChar.trim());  
        tmpChar = (String) map.get("crt_bank_no");
        setValue("crt_bank_no", tmpChar.trim());
        tmpChar = (String) map.get("vd_bank_no");
        setValue("vd_bank_no", tmpChar.trim());
        tmpChar = (String) map.get("home_area_code1");
        setValue("home_area_code1", tmpChar.trim());
        tmpChar = (String) map.get("home_tel_no1");
        setValue("home_tel_no1", tmpChar.trim());
        tmpChar = (String) map.get("cellar_phone");
        setValue("cellar_phone", tmpChar.trim());
        tmpChar = (String) map.get("office_area_code1");
        setValue("office_area_code1", tmpChar.trim());
        tmpChar = (String) map.get("office_tel_no1");
        setValue("office_tel_no1", tmpChar.trim());
        tmpChar = (String) map.get("office_tel_ext1");
        setValue("office_tel_ext1", tmpChar.trim());
        tmpChar = (String) map.get("business_code");
        setValue("business_code", tmpChar.trim()); 
        tmpChar = (String) map.get("service_year");
        setValue("service_year", tmpChar.trim());
        tmpChar = (String) map.get("salary");
        tmpChar = tmpChar.trim();
        tmpInt = Integer.parseInt(tmpChar) * 10000;
        setValueInt("salary", tmpInt);
        tmpChar = (String) map.get("apply_id");
        setValue("apply_id", tmpChar.trim());
        setValue("pm_id", tmpChar.trim());
        showLogMessage("D", "", "apply_id = [" + tmpChar + " ]");
        tmpChar = (String) map.get("birthday");
        tmpLong = Long.parseLong(tmpChar) + 19110000;
        tmpChar = Long.toString(tmpLong);
        setValue("birthday", tmpChar.trim());
        setValue("sup_flag", "0");
        tmpChar = (String) map.get("sex");
        setValue("sex", tmpChar.trim());
        
        tmpChar = (String) map.get("marriage");
    	if(tmpChar.trim().equals("3")) {
    		tmpChar = "1";
    	}
        setValue("marriage", tmpChar.trim());
        
        tmpChar = (String) map.get("education");
        switch(tmpChar) {
        case "1": 
        	setValue("education", "5");
        	break;
        case "2":
        	setValue("education", "3");
        	break;
        case "3":
        	setValue("education", "2");
        	break;
        default :
        	setValue("education", "6");
        	break;
        }

        tmpChar = (String) map.get("spouse_id_no");
        setValue("spouse_id_no", tmpChar.trim());
        
        tmpChar = (String) map.get("spouse_birthday");
        if(!tmpChar.trim().equals("")) {
            tmpLong = Long.parseLong(tmpChar) + 19110000;
            tmpChar = Long.toString(tmpLong);
        }
        setValue("spouse_birthday", tmpChar.trim());
        
        tmpChar = (String) map.get("credit_lmt");
        tmpChar = String.valueOf(Integer.parseInt(tmpChar.trim()) * 10000);
        setValue("credit_lmt", tmpChar.trim());       
        tmpChar = (String) map.get("stmt_cycle");
        setValue("stmt_cycle", tmpChar.trim());
        tmpChar = (String) map.get("revolve_int_rate_year");    
        tmpDoub = Double.parseDouble(tmpChar) * 0.01;
        String result = String .format("%.2f", tmpDoub);
        setValueDouble("revolve_int_rate_year", tmpDoub);                      
        tmpChar = (String) map.get("e_mail_addr");
        setValue("e_mail_addr", tmpChar.trim());  
        
        filePromoteDept = (String) map.get("promote_dept");
        filePromoteDept = filePromoteDept.trim();
        filePromoteEmpNo = (String) map.get("promote_emp_no");
        filePromoteEmpNo = filePromoteEmpNo.trim();
        fileIntroduceId = (String) map.get("introduce_id"); 
        fileIntroduceId = fileIntroduceId.trim();
        
        if (fileIntroduceId.length() == 6) {
	        selectSQL = "id,employ_no,unit_no ";
	        daoTable  = "crd_employee ";
	        whereStr  = "WHERE substr(acct_no, 8) = ? ";
	        setString(1, fileIntroduceId);       
	        if (selectTable() > 0) {
	        	employeeId = getValue("id");
	        	employeeNo = getValue("employ_no");	        	
	        	employeeBankNo = getValue("unit_no");
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
        	setValue("promote_dept", employeeBankNo);
        }
        else {
        	setValue("promote_dept", filePromoteDept);
        }
                
        setValue("promote_emp_no", filePromoteEmpNo);
        
        tmpChar = (String) map.get("send_pwd_flag");        
        setValue("send_pwd_flag", tmpChar.trim());
        tmpChar = (String) map.get("resident_no_expire_date");
        setValue("resident_no_expire_date", tmpChar.trim());
        
        tmpChar = (String) map.get("other_cntry_code");
        setValue("other_cntry_code", tmpChar.trim());
        if (tmpChar.trim().equals("")||tmpChar.trim().equals("TW"))
        	setValue("nation", "1");
        else
        	setValue("nation", "2");
        
        tmpChar = (String) map.get("passport_no");
        setValue("passport_no", tmpChar.trim());
        tmpChar = (String) map.get("passport_date");
        setValue("passport_date", tmpChar.trim()); 
        tmpChar = (String) map.get("corp_no");
        setValue("corp_no", tmpChar.trim()); 
        tmpChar = (String) map.get("market_agree_base");
        setValue("market_agree_base", tmpChar.trim());
        tmpChar = (String) map.get("stat_send_internet");
        setValue("stat_send_internet", tmpChar.trim());
        tmpChar = (String) map.get("source_code");        
        setValue("source_code", tmpChar.trim());
        tmpChar = (String) map.get("card_type");
        setValue("card_type", tmpChar.trim());
        tmpChar = (String) map.get("ap1_apply_date");
        tmpLong = Long.parseLong(tmpChar) + 19110000;
        tmpChar = Long.toString(tmpLong);
        setValue("ap1_apply_date", tmpChar.trim());
        setValue("mail_type", "4");
        setValue("nccc_type", "1");
        setValue("fl_flag", "N");
        
        
        tmpChar = hBusiChiDate.substring(0, 5);
        if (tmpChar.trim().length() > 0) {
            tmpLong = Long.parseLong(tmpChar) + 191100;
            tmpChar = Long.toString(tmpLong) + "01";
        }
        setValue("valid_fm", tmpChar.trim());

        setValue("unit_code", "");
        setValue("valid_to", "");
        
        tmpChar = (String) map.get("son_card_flag");
        setValue("son_card_flag", tmpChar.trim()); 
        if (tmpChar.equals("Y")) {
        	tmpChar = (String) map.get("indiv_crd_lmt");
        	tmpChar = tmpChar.trim();
        	tmpInt = Integer.parseInt(tmpChar) * 10000;
        	setValueInt("indiv_crd_lmt", tmpInt);
        }
        else
        	setValueInt("indiv_crd_lmt", 0);     
        
        tmpChar = (String) map.get("sms_amt");
        if(tmpChar.trim() == null || tmpChar.trim().equals("")) {
        	tmpChar = "0";
        }
        setValue("sms_amt", tmpChar.trim());
        
        tmpChar = (String) map.get("credit_level_new");
        tmpChar = tmpChar.trim();
        if(tmpChar.length() < 2 && !tmpChar.equals("D")) {
        	tmpChar = "0" + tmpChar;
        }
        setValue("credit_level_new", tmpChar.trim()); 
        
        tmpChar = (String) map.get("jcic_score");
        setValue("jcic_score", tmpChar.trim());
        tmpChar = (String) map.get("fee_code");
        setValue("fee_code", tmpChar.trim());
        tmpChar = (String) map.get("inst_flag");
        setValue("inst_flag", tmpChar.trim());
        tmpChar = (String) map.get("fee_code_i");
        setValue("fee_code_i", tmpChar.trim());
        tmpChar = (String) map.get("roadside_assist_apply");
        setValue("roadside_assist_apply", tmpChar.trim());
        tmpChar = (String) map.get("ur_flag");
        setValue("ur_flag", tmpChar.trim());
        tmpChar = (String) map.get("e_news");
        setValue("e_news", tmpChar.trim());
        tmpChar = (String) map.get("chi_name");
        setValue("chi_name", tmpChar.trim().replace("　", ""));        
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
        setValue("company_zip", tmpChar.trim().replace("　", ""));
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
        tmpChar = (String) map.get("spouse_name");
        setValue("spouse_name", tmpChar.trim().replace("　", ""));
        tmpChar = (String) map.get("graduation_elementarty");
        setValue("graduation_elementarty", tmpChar.trim().replace("　", ""));    
        
        setValue("oth_chk_code" , "0");

        if (debug == 1)
            showLogMessage("D", "", " Data=[" + tmpChar + "]");

        if (insertCrdEmapTmp() == 1)
        	return 1;

        return (0);
    }

    // ************************************************************************
    private Map processDataRecord(String[] row, String[] dt) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        int j = 0;
        for (String s : dt) {
            map.put(s.trim(), row[i]);
            i++;
        }
        return map;
        
    }

    // ************************************************************************
    public int inserCrdFileCtl() throws Exception {
        if (hHeadCnt != totalTemp) {
            showLogMessage("D", "", String.format(" Error : 檔案 [%s] => 實際[%d]筆與trailer[%d]筆不合 ", fileName1, totalTemp, hHeadCnt));
            return 1;
        }
        
        if (hSuccessCnt != successTemp) {
            showLogMessage("D", "", String.format(" Error : 檔案 [%s] => 實際成功[%d]筆與trailer[%d]筆不合 ", fileName1, successTemp, hSuccessCnt));
            return 1;
        }
        
        if (hErrorCnt != errorTemp) {
            showLogMessage("D", "", String.format(" Error : 檔案 [%s] => 實際失敗[%d]筆與trailer[%d]筆不合 ", fileName1, errorTemp, hErrorCnt));
            return 1;
        }
            
        setValue("file_name"     , fileName1);
        setValueInt("head_cnt"   , hHeadCnt);
        setValueInt("record_cnt" , totalCnt);
        setValue("crt_date"      , sysDate);
        setValue("trans_in_date" , sysDate);
        setValue("check_code"    , "");
        
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
            errorTemp ++;
            showLogMessage(err1, err2, comcr.hCallBatchSeqno);
//            comcr.err_rtn(err1, err2, comcr.h_call_batch_seqno);
        }
        else
        	successTemp ++;

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

        setString(1, groupCode);
        int recCnt = selectTable();
        if (notFound.equals("Y")) {
            String err1 = "select_ptr_group_code error="+groupCode;
            String err2 = groupCode;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        tmpChar = getValue("combo_indicator");
        if (debug == 1)
            showLogMessage("I", "", "     7777777RTN  =[" + tmpChar + "]");
        return tmpChar;
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
