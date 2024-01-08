/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/12/27  V1.00.00    Pino      program initial                           *
*  109/02/25  V1.00.01    Pino      program initial                           *
*  109/05/15  V1.00.02    Wilson    卡異動原因改'C'                               *
*  109/12/17  V1.00.03    shiyuqi       updated for project coding standard   *
*  112/02/07  V1.00.04    Wilson    檔案產生路徑調整                                                                                      *
*  112/03/07  V1.00.05    Wilson    新增procFTP                                *
*  112/03/07  V1.00.06    Wilson    檔案的結束符號改成0D0A                          *
*  112/04/21  V1.00.07    Wilson    調整filename                               *
*  112/06/30  V1.00.08    Wilson    檔案格式增加onlineOpenflag                    *
*  112/07/03  V1.00.09    Wilson    假日不執行                                                                                                 *
*  112/08/26  V1.00.10   Wilson     修正弱掃問題                                                                                             *
*  112/10/20  V1.00.11    Wilson    增加卡片組織別、卡片TYP欄位                                                              *
******************************************************************************/

package Crd;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import com.*;

/*產生*/
public class CrdC004 extends AccessDAO {
    private String progname = "產生COMBO續卡清單檔作業112/10/20  V1.00.11";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommRoutine    comr  = null;
    CommCrdRoutine comcr = null;
    CharFormatConverter cfc = new CharFormatConverter();
    CommFTP commFTP = null;

    int debug = 0;
    long totalCnt = 0;
    int tmpInt = 0;
    String checkHome = "";
    String hCallErrorDesc = "";
    String hBusinessDate = "";
    String pathName1 = "";

    String prgmId = "CrdC004";
    String rptName1 = "";
    int recordCnt = 0;
    int writeCnt = 0;
    int actCnt = 0;
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq1 = 0;
    int errCnt = 0;
    String errmsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hModWs = "";
    String hModLog = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    String hCurpModWs = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String hCallRProgramCode = "";

    String hTempUser = "";
    String hSystemDate = "";
    String tempDd = "";
    String hCombCardNo = "";
    String hCombSavingActno = "";
    String hCombRowid = "";
    String hEmbossCardRefNum = "";
    String hEmbossOldCardNo = "";
    String hEmbossElectronicCode = "";
    String hEmbossEmbossReason = "";
    String hEmbossRegBankNo = "";
    String hEmbossMailBranch = "";
    String hEmbossCrtBankNo = "";
    String hEmbossVdBankNo = "";
    String hCardNote = "";
    
    String hCardSourceCode = "";
    String hThirdDataReissue = "";
    String pMajorCardNo = "";
    String pAcctType = "";
    String pAcctKey = "";
    String hChiName = "";
    String hEmbossSource = "";
    String hEmbossReason = "";
    String hMailZip = "";
    String hMailAddr = "";
    String hMailType = "";
    String hBranch = "";
    String hRegBankNo = "";
    String hIcFlag = "";
	String filename             = "";
    String temstr               = "";
    int hNn = 0;
    int recCnt = 0;
    BufferedWriter fptr1 = null;
    Buf1 comboData = new Buf1();
    private boolean ftd2 = false;
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
            if (args.length > 1) {
                comc.errExit("Usage : CrdC004 callbatch_seqno", "");
            }

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

            commonRtn();
            
            showLogMessage("I", "", String.format("今日營業日 = [%s]", hBusinessDate));
            
            if (checkPtrHoliday() != 0) {
				exceptExit = 0;
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
            }
            
            checkOpen();
            selectCrdCombo();
            
            insertFileCtl();
            
    		commFTP = new CommFTP(getDBconnect(), getDBalias());
    	    comr = new CommRoutine(getDBconnect(), getDBalias());
    	    procFTP();
    	    renameFile1(filename);

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "][" + recCnt + "]";
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
    	checkFileCtl();
    	filename = String.format("rqst_combo_t_%8s%02d.txt", hSystemDate, hNn);
    	temstr = String.format("%s/media/crd/%s", comc.getECSHOME(), filename);
        showLogMessage("I", "", "  Open file=[" + temstr + "]"); 
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        try {
            comc.mkdirsFromFilenameWithPath(temstr);
            fptr1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temstr), "MS950"));
        } catch (Exception ex) {
            comcr.errRtn(String.format("開啟檔案[%s]失敗[%s]", temstr, ex.getMessage()), "", hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void checkFileCtl() throws Exception {
    	String likeFilename = "";
    	String hFileName = "";
    	likeFilename = String.format("rqst_combo_t_%8s", hSystemDate)+"%";
        sqlCmd = "select file_name ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        sqlCmd += " and crt_date  = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += " order by file_name desc  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, likeFilename);
        int tmpInt = selectTable();
        if (notFound.equals("Y")) {
        	hNn++;
        }else {
            hFileName = getValue("file_name");
            hNn = Integer.valueOf(hFileName.substring(21, 23))+1;
        }

    }
    /***********************************************************************/
    int selectCrdCombo() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.card_no,"; 
        sqlCmd += "a.apply_id,";     
        sqlCmd += "a.saving_actno,";
        sqlCmd += "a.to_ibm_date,";               
        sqlCmd += "b.emboss_source, ";
        sqlCmd += "b.card_ref_num, ";
        sqlCmd += "b.old_card_no, "; 
        sqlCmd += "b.electronic_code, "; 
        sqlCmd += "b.emboss_reason, ";
        sqlCmd += "b.reg_bank_no, ";
        sqlCmd += "b.mail_branch, ";
        sqlCmd += "b.crt_bank_no, ";
        sqlCmd += "b.vd_bank_no, ";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += "from crd_combo a, crd_emboss b ";
        sqlCmd += "where a.card_no = b.card_no ";
        sqlCmd += "and b.emboss_source in ('3','4') ";
        sqlCmd += "and a.to_ibm_date = '' ";
        sqlCmd += "order by card_no, apply_id  ";
        recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            hCombCardNo = getValue("card_no", i);
            hCombSavingActno = getValue("saving_actno", i);
            hCombRowid = getValue("rowid", i);
            hEmbossCardRefNum = getValue("card_ref_num", i);
            hEmbossOldCardNo = getValue("old_card_no", i);
            hEmbossElectronicCode = getValue("electronic_code", i);
            hEmbossEmbossReason = getValue("emboss_reason", i);
            hEmbossRegBankNo = getValue("reg_bank_no", i);
            hEmbossMailBranch = getValue("mail_branch", i);
            hEmbossCrtBankNo = getValue("crt_bank_no", i);
            hEmbossVdBankNo = getValue("vd_bank_no", i);
            recCnt++;
            if (recCnt == 1 || recCnt % 1000 == 0)
                showLogMessage("I", "", String.format(" Current  process count = [%d]", recCnt));
            totalCnt++;
            if (debug == 1)
                showLogMessage("I", "", "Read card=[" + hCombCardNo + "]" + totalCnt);

            writeRtn();

            updateCrdCombo();
        }
        if (fptr1 != null) {
            fptr1.close();
            fptr1 = null;
        }
		return recordCnt;
    }

    // ************************************************************************
  int writeRtn() throws Exception {
        comboData = new Buf1();
        
        comboData.savingActno = hCombSavingActno;
        comboData.filler = ";";
        comboData.cardRefNum = hEmbossCardRefNum;
        comboData.oldCardNo = hEmbossOldCardNo;
        comboData.cardNo = hCombCardNo;
        comboData.chr2typ = "C";
        
        comboData.chr2crt = getCardNote();
        
        if (hEmbossElectronicCode.equals("01")) {
        	comboData.easyCard = "Y";
        	comboData.otherCard = "C";
        }
        else {        	
        	comboData.easyCard = "N";
        	comboData.otherCard = "";
        }
        
        comboData.embossReason = "9";
        
        comboData.regBankNo = hEmbossRegBankNo;
        comboData.mailBranch = hEmbossMailBranch;
        comboData.crtBankNo = hEmbossCrtBankNo;
        comboData.vdBankNo = hEmbossVdBankNo;
        comboData.vmjType = "";
        comboData.cardType = "";
        comboData.onlineOpenflag = "N";
        comboData.filler12          	 = ";;;;;;;;;;;;";
        
        buf = comboData.allText();
        fptr1.write(buf + "\r\n");

        return (0);
    }

    /***********************************************************************/
    String getCardNote()throws Exception {

    	sqlCmd  = "select a.card_note ";
    	sqlCmd += "from ptr_card_type a, crd_emboss b ";
        sqlCmd += "where a.card_type = b.card_type ";
        sqlCmd += "and b.old_card_no = ? ";
        setString(1, hEmbossOldCardNo);
        selectTable();
        hCardNote = getValue("card_note");
        switch(hCardNote) {
        case "C":
        	return "1";
        case "G":
        	return "2";
        case "I":
        case "P":
        case "S":
        	return "3";
        }    	
    	return  "0";
    }
    
    /***********************************************************************/
    void updateCrdCombo() throws Exception {
        daoTable   = "crd_combo";
        updateSQL  = "to_ibm_date = to_char(sysdate,'yyyymmdd'),";
        updateSQL += "mod_time    = sysdate,";
        updateSQL += "mod_pgm     = ?";
        whereStr   = "where rowid = ? ";

        setString(1, javaProgram);
        setRowId(2, hCombRowid);

        actCnt = updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_combo not found!", "", comcr.hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void insertFileCtl() throws Exception {
        daoTable = "crd_file_ctl";
        setValue("file_name", filename);
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
            setString(3, filename);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
            }
        }
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
    	CrdC004 proc = new CrdC004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

/***********************************************************************/
class Buf1 {
	String savingActno;
	String filler;
	String cardRefNum;
	String oldCardNo;
	String cardNo;
	String chr2typ;
	String chr2crt;
	String easyCard;
	String embossReason;
	String otherCard;
	String regBankNo;
	String mailBranch;
	String crtBankNo;
	String vdBankNo;
	String vmjType;
	String cardType;
	String onlineOpenflag;
	String filler12;
	
	String allText() throws UnsupportedEncodingException {
        String rtn = "";        
        rtn += fixLeft(savingActno, 13);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(cardRefNum, 2);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(oldCardNo, 16);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(cardNo, 16);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(chr2typ, 1);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(chr2crt, 1);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(easyCard, 1);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(embossReason, 1);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(otherCard, 1);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(regBankNo, 4);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(mailBranch, 4);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(crtBankNo, 4);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(vdBankNo, 4);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(vmjType, 1);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(cardType, 3);
        rtn += fixLeft(filler, 1);
        rtn += fixLeft(onlineOpenflag, 1);
        rtn += fixLeft(filler12, 12);
        return rtn;
	}
	
	String fixLeft(String str, int len) throws UnsupportedEncodingException {
	    int size = (Math.floorDiv(len, 100) + 1) * 100;
	    String spc = "";
	    for (int i = 0; i < size; i++)    spc += " ";
	    if (str == null)                  str  = "";
	    str = str + spc;
	    byte[] bytes = str.getBytes("MS950");
	    byte[] vResult = new byte[len];
	    System.arraycopy(bytes, 0, vResult, 0, len);

	    return new String(vResult, "MS950");
	}	
}

/***********************************************************************/
void splitBuf1(String str) throws UnsupportedEncodingException {
    byte[] bytes = str.getBytes("MS950");
    comboData.savingActno = comc.subMS950String(bytes, 0, 13);
    comboData.filler          		 = comc.subMS950String(bytes, 13, 1);
    comboData.cardRefNum = comc.subMS950String(bytes, 14, 2);
    comboData.filler          		 = comc.subMS950String(bytes, 16, 1);
    comboData.oldCardNo = comc.subMS950String(bytes, 17, 16);
    comboData.filler          		 = comc.subMS950String(bytes, 33, 1);
    comboData.cardNo = comc.subMS950String(bytes, 34, 16);
    comboData.filler          		 = comc.subMS950String(bytes, 50, 1);
    comboData.chr2typ          	 = comc.subMS950String(bytes, 51, 1);
    comboData.filler          		 = comc.subMS950String(bytes, 52, 1);
    comboData.chr2crt          	 = comc.subMS950String(bytes, 53, 1);
    comboData.filler          		 = comc.subMS950String(bytes, 54, 1);
    comboData.easyCard = comc.subMS950String(bytes, 55, 1);
    comboData.filler          		 = comc.subMS950String(bytes, 56, 1);
    comboData.embossReason = comc.subMS950String(bytes, 57, 1);
    comboData.filler          		 = comc.subMS950String(bytes, 58, 1);
    comboData.otherCard = comc.subMS950String(bytes, 59, 1);
    comboData.filler          		 = comc.subMS950String(bytes, 60, 1);
    comboData.regBankNo = comc.subMS950String(bytes, 61, 4);
    comboData.filler          		 = comc.subMS950String(bytes, 65, 1);
    comboData.mailBranch = comc.subMS950String(bytes, 66, 4);
    comboData.filler          		 = comc.subMS950String(bytes, 70, 1);
    comboData.crtBankNo = comc.subMS950String(bytes, 71, 4);
    comboData.filler          		 = comc.subMS950String(bytes, 75, 1);
    comboData.vdBankNo = comc.subMS950String(bytes, 76, 4);
    comboData.filler          		 = comc.subMS950String(bytes, 80, 1);
    comboData.vmjType = comc.subMS950String(bytes, 81, 1);
    comboData.filler          		 = comc.subMS950String(bytes, 82, 1);
    comboData.cardType = comc.subMS950String(bytes, 83, 3);
    comboData.filler          		 = comc.subMS950String(bytes, 86, 1);
    comboData.onlineOpenflag = comc.subMS950String(bytes, 87, 1);    
    comboData.filler12              = comc.subMS950String(bytes, 88, 12);
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
    showLogMessage("I", "", "mput " + filename + " 開始傳送....");
    int errCode = commFTP.ftplogName("NCR2TCB", "mput " + filename);
    
    if (errCode != 0) {
        showLogMessage("I", "", "ERROR:無法傳送 " + filename + " 資料"+" errcode:"+errCode);
        insertEcsNotifyLog(filename);          
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
