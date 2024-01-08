/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/09/06   V1.00.01   Wilson      program initial                         *
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
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class CrdR052G2 extends AccessDAO {
    private String progname = "產生台灣PAY綁卡數明細資料檔程式    112/09/06  V1.00.01 ";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommDate       commDate = new CommDate();
    CommCrdRoutine comcr    = null;
    CommRoutine    comr  = null;
    CommFTP commFTP = null;

    int debug = 0;
    String                    prgmId             = "CrdR052G2";
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
    String hTmpCardNo = "";
    String hTmpVCardNo = "";
    String hTmpIdNo = "";
    String hTmpIssueDate = "";
    String hBusiBusinessDate = "";
    String hChiYymmdd =  "";
    String hBegDate =  "";
    String hEndDate =  "";
    String hBegDateBil =  "";
    String hEndDateBil =  "";
    String hFirstDay =  "";

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
            if (args.length > 1) {
                comc.errExit("Usage : CrdR052G2", "");
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
            
            if (args.length >  0) {
                hBusiBusinessDate = "";
                if(args[0].length() == 8) {
                   hBusiBusinessDate = args[0];
                  } else {
                   String errMsg = String.format("指定營業日[%s]", args[0]);
                   comcr.errRtn(errMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
                  }
            }
            
            selectPtrBusinday();
            
            if (!hBusiBusinessDate.equals(hFirstDay)) {
        		showLogMessage("E", "", "今日不為該月第一天,不執行此程式");
        		return 0;
            }

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
    public int selectPtrBusinday() throws Exception 
    {

       sqlCmd  = "select to_char(sysdate,'yyyymmdd') as business_date";
       sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
       sqlCmd += " from ptr_businday ";
       int recordCnt = selectTable();
       if (notFound.equals("Y")) {
           comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
       }
       if (recordCnt > 0) {
           hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? comcr.increaseDays(getValue("business_date"),1)
                               : hBusiBusinessDate;
       }
       
       hFirstDay = hBusiBusinessDate.substring(0, 6) + "01";

       sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm')||'01' h_beg_date_bil ";
       sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date_bil ";
       sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_date ";
       sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date ";
       sqlCmd += " from dual ";
       setString(1, hBusiBusinessDate);
       setString(2, hBusiBusinessDate);
       setString(3, hBusiBusinessDate);
       setString(4, hBusiBusinessDate);

       recordCnt = selectTable();
       if(recordCnt > 0) {
          hBegDateBil = getValue("h_beg_date_bil");
          hEndDateBil = getValue("h_end_date_bil");	   
          hBegDate = getValue("h_beg_date");
          hEndDate = getValue("h_end_date");
         }

       hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
       showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , hBusiBusinessDate
               , hChiYymmdd, hBegDateBil, hEndDateBil, hBegDate, hEndDate));
       return 0;
    }
    /***********************************************************************/
    void openTextFile() throws Exception {
        String filename = "";
        String pFilename = "";
        hFileName = "";
        
        filename = String.format("HCECARD_%s.TXT", hBusiBusinessDate);
        hFileName = filename;
        rptName1 = filename;
    }    

    /***********************************************************************/
    void process() throws Exception {
        sqlCmd  = " select a.card_no, ";
        sqlCmd += "        a.v_card_no, ";
        sqlCmd += "        b.id_no, ";
        sqlCmd += "        a.crt_date ";
        sqlCmd += "   from hce_card a,crd_idno b ";
        sqlCmd += "  where a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += "    and a.crt_date between ? and ? ";
        setString(1, hBegDate); 
        setString(2, hEndDate); 
        int cursorIndex = openCursor();

        while (fetchTable(cursorIndex)) {
        	hTmpCardNo    = getValue("card_no");
        	hTmpVCardNo     = getValue("v_card_no");
        	hTmpIdNo = getValue("id_no");
        	hTmpIssueDate = getValue("crt_date");

            totalAll++;
if(debug == 1)
   showLogMessage("I",""," 888 read card_no=[" + hTmpCardNo + "]" + " v_card_no =[" + hTmpVCardNo + "]");

            createFile();
            
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
    void createFile() throws Exception {

    	data.cardNo = hTmpCardNo;
    	data.vCardNo = hTmpVCardNo;
    	data.idNo = hTmpIdNo;
    	data.issueDate = hTmpIssueDate;
    	
        buf = data.allText();
        lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));

        return;
    }

    /**
     * @throws Exception
     *********************************************************************/
    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + hFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("CREDITCARD", "mput " + hFileName);
        
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
        CrdR052G2 proc = new CrdR052G2();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    class Buf {
    	String cardNo;
    	String vCardNo;
    	String idNo;
    	String issueDate;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += cardNo + ",";
            rtn += vCardNo + ",";
            rtn += idNo + ",";
            rtn += issueDate;     
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
