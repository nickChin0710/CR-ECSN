/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/02/06  V1.00.00    Rou        initial(Reference CrdF014 program)       *
*  109/12/23  V1.00.01   shiyuqi       updated for project coding standard   *
*  112/04/24  V1.00.02   Wilson       調整產生檔案路徑                                                                                *
*  112/05/19  V1.00.03   Wilson       調整檔案格式                                                                                        *
*  112/05/23  V1.00.04   Wilson       效期、出生日月順序調整                                                                     *
*  112/08/20  V1.00.05   Wilson       調整為舊的格式                                                                                     *
******************************************************************************/

package Crd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class CrdM067 extends AccessDAO {
    private String progname = "產生龍騰卡製卡回饋檔程式    112/08/20  V1.00.05 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommRoutine    comr  = null;
    CommFTP commFTP = null;

    int debug = 1;
    String                    prgmId             = "CrdM067";
    String                    rptName1           = "";
    List<Map<String, Object>> lpar1              = new ArrayList<Map<String, Object>>();
    int                       rptSeq1            = 0;
    String                    buf                = "";
    String                    stderr             = "";
    String                    hCallBatchSeqno = "";

    String hFileName             = "";
    int    total                   = 0;
    int    fileSeqno              = 0;
    int    totalAll               = 0;
    int    errCode                = 0;
    String hCddModAudcode = "";
    String hCddDpCardNo = "";
    String hCddOldDpCardNo = "";
    String hIdnoChiName = "";
	String hCcpEngName = "";
	String hIdnoBirthday = "";
	String hCcpValidTo = "";
    
    String hCddRowid = "";
    
    Buf           data       = new Buf();
    private String hModUser = "";

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
                comc.errExit("Usage : CrdM067", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    		commFTP = new CommFTP(getDBconnect(), getDBalias());
    	    comr = new CommRoutine(getDBconnect(), getDBalias());

            hModUser = comc.commGetUserID();
            total = 0;

            openTextFile();

            process();

            String filename = String.format("%s/media/crd/%s", comc.getECSHOME(), hFileName);
            stderr = String.format("FILENAME [%s] temstr=[%s]\n", hFileName, filename);
            showLogMessage("I", "", stderr);
            comc.writeReport(filename, lpar1, "MS950");

            if (total > 0) {
            	insertFileCtl();
            	procFTP();
                renameFile1(hFileName);
            } else {
                showLogMessage("I", "", String.format("NO DATA RM FILE[%s]", filename));
                comc.fileDelete(filename);
            }

            showLogMessage("I", "", String.format("程式執行結束 , 總筆數:[%d],寫檔=[%d]\n", totalAll, total));

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void openTextFile() throws Exception {
        String filename = "";
        String pFilename = "";
        hFileName = "";
        
        filename = String.format("R68861403_DRAGON_%s.TXT", sysDate);
        hFileName = filename;
        rptName1 = filename;
    }    

    /***********************************************************************/
    void process() throws Exception {
        sqlCmd  = "select a.mod_audcode, ";
        sqlCmd += "a.dp_card_no,";
        sqlCmd += "a.old_dp_card_no,";
        sqlCmd += "b.chi_name,";
        sqlCmd += "c.eng_name,";
        sqlCmd += "b.birthday,";
        sqlCmd += "c.valid_to,";
        sqlCmd += "a.rowid  as rowid";
        sqlCmd += " from crd_dp_dragon a, crd_idno b, crd_card_pp c ";
        sqlCmd += "where a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += "  and a.dp_card_no = c.pp_card_no ";
        sqlCmd += "  and a.post_flag = 'N' ";
        int cursorIndex = openCursor();

        while (fetchTable(cursorIndex)) {
        	hCddModAudcode    = getValue("mod_audcode");
        	hCddDpCardNo     = getValue("dp_card_no");
        	hCddOldDpCardNo = getValue("old_dp_card_no");
        	hIdnoChiName      = getValue("chi_name");
        	hCcpEngName      = getValue("eng_name");
        	hIdnoBirthday      = getValue("birthday");
        	hCcpValidTo      = getValue("valid_to");
        	hCddRowid      	 = getValue("rowid");

            totalAll++;
if(debug == 1)
   showLogMessage("I",""," 888 read=["+hCddDpCardNo+"]");

            createJcicFile();
        	updateCrdDpDragon();
            total++;
        }
        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void insertFileCtl() throws Exception {
        setValue("file_name", hFileName);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", 1);
        setValueInt("record_cnt", total);
        setValue("check_code", "1");
        setValue("send_nccc_date", sysDate);
        daoTable = "crd_file_ctl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_crd_file_ctl duplicate!", "", hCallBatchSeqno);
        }
    }  

    /***********************************************************************/
    void updateCrdDpDragon() throws Exception {

        daoTable   = "crd_dp_dragon";
        updateSQL  = " post_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " post_flag  = 'Y',";
        updateSQL += " mod_time   = sysdate,";
        updateSQL += " mod_user   = 'batch',";
        updateSQL += " mod_pgm    = 'CrdM067' ";
        whereStr   = "where rowid = ? ";
        setRowId(1, hCddRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_dp_dragon not found!", "", hCallBatchSeqno);
        }
    }    
    
    /***********************************************************************/
    void createJcicFile() throws Exception {

    	data.modAudcode = hCddModAudcode;
    	data.dpCardNo = hCddDpCardNo;
    	
    	if (hCddModAudcode.equals("U") || hCddModAudcode.equals("L"))   		
    		data.oldDpCardNo = hCddOldDpCardNo;
    	else {
    		hCddDpCardNo = String.format("%1$-16s", "");
    		data.oldDpCardNo = hCddDpCardNo;
    	} 
    	hIdnoChiName = hIdnoChiName.replaceFirst(hIdnoChiName.substring(1, 2), "*");
    	if (hIdnoChiName.length() != 20) {
    		for (int i = hIdnoChiName.length() ; i <= 20 ; i ++)
    			hIdnoChiName += " ";
    	    data.chiName = hIdnoChiName;
    	}   	

    	data.engName = hCcpEngName;
    	data.validTo = comc.getSubString(hCcpValidTo, 4, 6) + comc.getSubString(hCcpValidTo, 2, 4);
    	data.birthday = comc.getSubString(hIdnoBirthday, 6, 8) + comc.getSubString(hIdnoBirthday, 4, 6);
    	
        buf = data.allText();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        return;
    }

    /**
     * @throws Exception
     *********************************************************************/
    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + hFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("NCR2TCB", "mput " + hFileName);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + hFileName + " 資料"+" errcode:"+errCode);
            insertEcsNotifyLog(hFileName);          
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

    /***********************************************************************/
   	void renameFile1(String removeFileName) throws Exception {
   		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + removeFileName;
   		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + removeFileName;
   		
   		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
   			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
   			return;
   		}
   		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
   	}
   	        
   	/****************************************************************************/        
    public static void main(String[] args) throws Exception {
        CrdM067 proc = new CrdM067();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    class Buf {
    	String modAudcode;
    	String dpCardNo;
    	String oldDpCardNo;
    	String chiName;
    	String engName;
    	String validTo;
    	String birthday;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += modAudcode + ",";
            rtn += dpCardNo + ",";
            rtn += oldDpCardNo + ",";
            rtn += chiName + ",";     
//            rtn += engName + ",";
            rtn += validTo ;
//            rtn += birthday;
            return rtn;
        }
 
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
