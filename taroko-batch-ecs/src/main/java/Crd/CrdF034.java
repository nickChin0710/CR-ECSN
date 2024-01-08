/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/02/27  V1.00.00    Rou        program initial                         *
*  109/12/22  V1.00.01   shiyuqi      updated for project coding standard   *
*  112/02/13  V1.00.02   Wilson      select增加crd_emap_tmp的資料                                          *
*  112/03/06  V1.00.03   Wilson      檔案的結束符號改成0D0A                       *
*  112/03/07  V1.00.04   Wilson      新增procFTP                              *
*  112/03/09  V1.00.05   Wilson      hTmpRejectCode邏輯調整                                                           *
*  112/03/17  V1.00.06   Wilson      combo_indicator not in ('','N')          * 
*  112/07/03  V1.00.07   Wilson      假日不執行                                                                                              *
*  112/12/06  V1.00.08   Wilson      crd_item_unit不判斷卡種                                                           *
******************************************************************************/

package Crd;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommFTP;

/*產生給設一科COMBO製卡回饋檔*/
public class CrdF034 extends AccessDAO {
    private String progname = "產生給設一科COMBO製卡回饋檔程式 112/12/06  V1.00.08 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;

    String prgmId = "CrdF034";
    String rptName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    String buf = "";
    String stderr = "";
    String hCallBatchSeqno = "";
    String filePath = "";    
    String hFilename = "";
    
    String hTmpCardNo = "";    
    String hTmpUnitCode = "";
    String hTmpCardType = "";
    String hTmpComboActNo = "";
    String hTmpRowid = "";
    String hTmpInMainDate = "";
    String hTmpInMainError = "";
    String hTmpRejectCode = "";
    String hTmpMakecardSource = "";
    String hTmpCardRefNum = "";
    String hTmpValidTo = "";
    String hEmbossElectronicCode = "";
    String hTmpAp1ApplyDate = "";
    String hTmpType = ""; 
    String hTmpElectronicCode = "";
    String hTmpRespCode = "";

    long hComboCashLimit = 0;

    String hBusinessDate = "";
    int total = 0;

    Buf1 data = new Buf1();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : CrdF034 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            commonRtn();
            
            showLogMessage("I", "", String.format("今日營業日 = [%s]", hBusinessDate));
            
            if (checkPtrHoliday() != 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }

            openTextFile();

            fetchDetail();

            String filename = String.format("%s/media/crd/%s", comc.getECSHOME(), hFilename);
            comc.writeReport(filename, lpar1, "MS950" , false);           

            showLogMessage("I", "", " Export file = " + hFilename);
            
            insertFileCtl();
            
    		commFTP = new CommFTP(getDBconnect(), getDBalias());
    	    comr = new CommRoutine(getDBconnect(), getDBalias());
    	    procFTP();
    	    renameFile1(hFilename);

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束 筆數 [" + total + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
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
        hBusinessDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }

        return;
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
    @SuppressWarnings("unused")
    int openTextFile() throws Exception {
       
    	int fileNo = 0;
        sqlCmd  = "select max(substr(file_name, 23, 2)) file_no";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += " where file_name like ? ";
        sqlCmd += "  and crt_date  = to_char(sysdate, 'yyyymmdd') ";
        setString(1, "%rtn_combo_all_%");
        if (selectTable() > 0) 
        	fileNo = getValueInt("file_no");      
        
        hFilename = String.format("rtn_combo_all_%s%02d.txt", sysDate, fileNo + 1);
        filePath = String.format("%s/media/crd/%s", comc.getECSHOME(), hFilename);
        filePath = Normalizer.normalize(hFilename, java.text.Normalizer.Form.NFKD);
        rptName1 = hFilename;        
        
        if (checkFileCtl() != 0) {
            return (1);
        }       

        return (0);
    }

    /***********************************************************************/
    int checkFileCtl() throws Exception {
        int hhCount = 0;

        sqlCmd = "select count(*) hh_count ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name = ?  ";
        sqlCmd += "  and crt_date  = to_char(sysdate, 'yyyymmdd') ";
        setString(1, hFilename);
        int recordCnt = selectTable();
        if (recordCnt > 0) 
            hhCount = getValueInt("hh_count");
        
        if (hhCount > 0) {
            stderr = String.format("此檔案已產生不可重複產生(crd_file_ctl)");
            showLogMessage("I", "", stderr);
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void fetchDetail() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "'1' as tmp_type,";
        sqlCmd += "card_no as tmp_card_no,";
        sqlCmd += "unit_code as tmp_unit_code,";
        sqlCmd += "card_type as tmp_card_type,";
        sqlCmd += "act_no as tmp_act_no,";
        sqlCmd += "electronic_code as tmp_electronic_code,";
        sqlCmd += "check_code as tmp_resp_code,";
        sqlCmd += "'' as tmp_in_main_date,";
        sqlCmd += "'' as tmp_in_main_error,";
        sqlCmd += "reject_code as tmp_reject_code,";
        sqlCmd += "source as tmp_source,";
        sqlCmd += "card_ref_num as tmp_card_ref_num,";
        sqlCmd += "valid_to as tmp_valid_to,";
        sqlCmd += "ap1_apply_date as tmp_ap1_apply_date,";
        sqlCmd += "rowid as tmp_rowid ";
        sqlCmd += " from crd_emap_tmp ";
        sqlCmd += "where combo_indicator not in ('','N') ";
        sqlCmd += "  and check_code not in ('','000') ";
        sqlCmd += "  and end_ibm_date = '' ";
        sqlCmd +="union ";
        sqlCmd +="select ";
        sqlCmd += "'2' as tmp_type,";
        sqlCmd += "a.card_no as tmp_card_no,";
        sqlCmd += "b.unit_code as tmp_unit_code,";
        sqlCmd += "b.card_type as tmp_card_type,";
        sqlCmd += "a.saving_actno as tmp_act_no,";
        sqlCmd += "b.electronic_code as tmp_electronic_code,";
        sqlCmd += "'' as tmp_resp_code,";
        sqlCmd += "b.in_main_date as tmp_in_main_date ,";
        sqlCmd += "b.in_main_error as tmp_in_main_error,";
        sqlCmd += "b.reject_code as tmp_reject_code,";
        sqlCmd += "b.emboss_source as tmp_source,";
        sqlCmd += "b.card_ref_num as tmp_card_ref_num,";
        sqlCmd += "b.valid_to as tmp_valid_to,";
        sqlCmd += "b.ap1_apply_date as tmp_ap1_apply_date,";
        sqlCmd += "a.rowid as tmp_rowid ";
        sqlCmd += " from crd_combo a, crd_emboss b ";
        sqlCmd += "where a.card_no = b.card_no ";
        sqlCmd += "  and a.batchno  = b.batchno ";
        sqlCmd += "  and a.recno  = b.recno ";
        sqlCmd += "  and a.rtn_ibm_date <> '' ";
        sqlCmd += "  and a.to_nccc_date <> '' ";
        sqlCmd += "  and a.end_ibm_date = '' ";
        sqlCmd += "order by tmp_ap1_apply_date ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
        	hTmpType            = getValue("tmp_type", i);
            hTmpCardNo          = getValue("tmp_card_no", i);          
            hTmpUnitCode        = getValue("tmp_unit_code", i);            
            hTmpCardType        = getValue("tmp_card_type", i);            
            hTmpComboActNo     = getValue("tmp_act_no", i);     
            hTmpElectronicCode  = getValue("tmp_electronic_code", i);
            hTmpRespCode     = getValue("tmp_resp_code", i);
            hTmpInMainDate   = getValue("tmp_in_main_date", i);
            hTmpInMainError  = getValue("tmp_in_main_error", i);
            hTmpRejectCode    = getValue("tmp_reject_code", i);
            hTmpMakecardSource  = getValue("tmp_source", i);
            hTmpCardRefNum   = getValue("tmp_card_ref_num", i);
            hTmpValidTo       = getValue("tmp_valid_to", i);
            hTmpAp1ApplyDate = getValue("tmp_ap1_apply_date", i);
            hTmpRowid            = getValue("tmp_rowid", i);
            showLogMessage("I", "", " Read Card_no = [" + hTmpCardNo + "],act_no = [" + hTmpComboActNo + "]");
           
            createTextFile();
            total++;
            
            if(hTmpType.equals("1")&&(!hTmpRejectCode.equals(""))) {
            	updateCrdEmapTmp();
            }           

            if(hTmpType.equals("2")) {
            	if((!hTmpInMainDate.equals("") && hTmpInMainError.equals("0")) || !hTmpRejectCode.equals("")) {
            		updateCrdCombo();
            	}
            }            
        }
    }

    /***********************************************************************/
    void createTextFile() throws Exception {
        
        data.cardKind = "C";
        
        switch(hTmpMakecardSource) {
        case "1":
        case "2":
        	data.cardSource = "N";
        	break;
        case "3":
        case "4":
        	data.cardSource = "T";
        	break;
        case "5":
        	data.cardSource = "S";
        	break;
        }
        
        data.savingActno = hTmpComboActNo;
        data.cardNo = hTmpCardNo;
        data.cardRefNum = hTmpCardRefNum;
        data.validTo = hTmpValidTo.substring(2, 6);
        
        if (hTmpType.equals("2")&&hTmpElectronicCode.equals("01")) {
        	data.tscCardNo = getTscCardNo();
        } 
        else {
        	data.tscCardNo = "";
        }
        
        data.responseDate = sysDate;
        
        if(hTmpType.equals("1")) {
        	data.responseCode = hTmpRejectCode;
        }
        else if(hTmpType.equals("2")) {
            if (!hTmpInMainDate.equals("") && hTmpInMainError.equals("0") && hTmpRejectCode.equals("")) {
            	data.responseCode = "000";
            }           	
            else {
            	data.responseCode = hTmpRejectCode;
            }           	
        }
        else {
        	data.responseCode = hTmpRejectCode;
        }
        
        data.ap1ApplyDate = hTmpAp1ApplyDate;
        
        data.extn_yy = getExtnYy();
        
        if(!hTmpRejectCode.equals("")) {
        	data.card_del_flag = "Y";
        }
        else {
        	data.card_del_flag = "";
        }        
        		        
        buf = data.allText();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        return;
    }
    /***********************************************************************/
    String getTscCardNo() throws Exception{
    	
    	String hTscCardNo = "";
    	
    	sqlCmd = "select tsc_card_no ";
        sqlCmd += " from tsc_cdrp_log  ";
        sqlCmd += "where card_no        = ?  ";
        sqlCmd += "  and vendor_date_to = ''  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTmpCardNo);
        selectTable();        
        hTscCardNo = getValue("tsc_card_no");
        return hTscCardNo;
    }

    /***********************************************************************/
    String getExtnYy() throws Exception{
    	
    	String hExtnYy = "";
    	String tmpNewExtnMm = "";
    	String tmpExtnYear = "";
    	
    	sqlCmd = "select new_extn_mm,";
        sqlCmd += "extn_year ";
        sqlCmd += " from crd_item_unit  ";
        sqlCmd += "where unit_code = ?  ";
        setString(1, hTmpUnitCode);
        selectTable();        
        tmpNewExtnMm = getValue("new_extn_mm");
        tmpExtnYear = getValue("extn_year");
        
        if(hTmpMakecardSource.equals("3")||hTmpMakecardSource.equals("4")) {
        	hExtnYy = tmpExtnYear;
        }else {
        	hExtnYy = tmpNewExtnMm;
        }
        
        return hExtnYy;
    }

    /***********************************************************************/
    void updateCrdEmapTmp() throws Exception {
        daoTable   = "crd_emap_tmp";
        updateSQL  = " end_ibm_date = ?, ";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_user     = ?,";
        updateSQL += " mod_pgm      = ?";
        whereStr   = "where rowid   = ? ";
        setString(1, sysDate);
        setString(2, comc.commGetUserID());
        setString(3, javaProgram);
        setRowId(4, hTmpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_emap_tmp not found!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void updateCrdCombo() throws Exception {
        daoTable   = "crd_combo";
        updateSQL  = " end_ibm_date = ?, ";
        updateSQL += " mod_time     = sysdate,";
        updateSQL += " mod_user     = ?,";
        updateSQL += " mod_pgm      = ?";
        whereStr   = "where rowid   = ? ";
        setString(1, sysDate);
        setString(2, comc.commGetUserID());
        setString(3, javaProgram);
        setRowId(4, hTmpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_combo not found!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void insertFileCtl() throws Exception {
        setValue("file_name", hFilename);
        setValue("crt_date", sysDate);
        setValue("trans_in_date", sysDate);
        daoTable = "crd_file_ctl";
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable = "crd_file_ctl";
            updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name = ? ";
            setString(1, hFilename);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
            }
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF034 proc = new CrdF034();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String cardKind = "";
        String cardSource = "";
        String savingActno = "";
        String cardNo = "";
        String cardRefNum = "";
        String validTo = "";
        String tscCardNo = "";
        String responseDate = "";
        String responseCode = "";
        String ap1ApplyDate = "";
        String extn_yy = "";
        String card_del_flag = "";
        String filler = "";

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(cardKind      , 1);
            rtn += comc.fixLeft(cardSource    , 1);
            rtn += comc.fixLeft(savingActno   , 13);
            rtn += comc.fixLeft(cardNo    , 16);
            rtn += comc.fixLeft(cardRefNum   , 2);
            rtn += comc.fixLeft(validTo   , 4);
            rtn += comc.fixLeft(tscCardNo    , 20);
            rtn += comc.fixLeft(responseDate  , 8);
            rtn += comc.fixLeft(responseCode  , 3);
            rtn += comc.fixLeft(ap1ApplyDate , 8); 
            rtn += comc.fixLeft(extn_yy , 1); 
            rtn += comc.fixLeft(card_del_flag , 1);
            rtn += comc.fixLeft(filler , 22);
            rtn += comc.fixLeft("\r\n" , 2);
            return rtn;
        }
    }
    /***********************************************************************/
    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + hFilename + " 開始傳送....");
        int errCode = commFTP.ftplogName("NCR2TCB", "mput " + hFilename);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + hFilename + " 資料"+" errcode:"+errCode);
            insertEcsNotifyLog(hFilename);          
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
  		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + removeFileName;
  		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + removeFileName;
  		
  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
  			return;
  		}
  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
  	}    
}
