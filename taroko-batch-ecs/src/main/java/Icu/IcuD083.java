/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/11/04  V1.00.00    Ryan     program initial                           *
 ******************************************************************************/

package Icu;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*產生IBM雙幣自動扣繳檔*/
public class IcuD083 extends AccessDAO {
    private final String PROGNAME = "每日拋送前一日票證掛失資料檔給cardlink處理程式   111/11/04  V1.00.00";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommRoutine comr = null;
    CommFTP commFTP = null;
    String fileName = "elec_oppo.txt";
    String buf = "";
    int totalCnt = 0;
    String hOppostDate = "";
    String hWfValue = "";
    String argFlag = "";
    Buf1 sendData = new Buf1();
    int out = -1;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
        	comr = new CommRoutine(getDBconnect(), getDBalias());
        	commFTP = new CommFTP(getDBconnect(), getDBalias());

        	hOppostDate = convDates(sysDate,-1);
            if ((args.length > 0) && (args[0].length() == 8)) {
            	argFlag = "Y";
                String sGArgs0 = "";
                sGArgs0 = args[0];
                sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                hOppostDate = sGArgs0;
            }

            checkOpen();
            int parmCnt = selectPtrSysParm();
            if(parmCnt == 0) {
            	 comc.errExit("PTR_SYS_PARM 參數未設定 ", "");
            }
            showLogMessage("I", "", String.format(" 取得參數 ,WF_VALUE = [%s]", hWfValue));
            writeTextFile();
            
            closeOutputText(out);
            procFTP();
            renameFile();


            showLogMessage("I", "", String.format(" =============================================== "));
            showLogMessage("I", "", String.format(" 寫入總筆數 [%d]", totalCnt));
            showLogMessage("I", "", String.format(" =============================================== "));

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }


    /***********************************************************************/
    int selectPtrSysParm() throws Exception {

        sqlCmd = "SELECT WF_VALUE FROM PTR_SYS_PARM WHERE WF_KEY ='ICUD083' ";
        int recordCnt = selectTable();
        if(recordCnt>0) {
        	hWfValue = getValue("WF_VALUE");
        }
        
        return recordCnt;
    }

    /***********************************************************************/
    int writeTextFile() throws Exception {
    	StringBuffer sqlCmdStr = new StringBuffer();
    	if(!hWfValue.equals("A")) {
    		if(argFlag.equals("Y"))
    			showLogMessage("I", "", String.format(" WF_VALUE<>A 有輸入參數,取得參數日期 = [%s]", hOppostDate));
    		else
    			showLogMessage("I", "", String.format(" WF_VALUE<>A 無輸入參數,取得系統日前一日 = [%s]", hOppostDate));
    		sqlCmdStr.append(" and oppost_date = '");
    		sqlCmdStr.append(hOppostDate);
    		sqlCmdStr.append("'");
    	}
    	
        sqlCmd = " select * from (select '01' as elec_type,tsc_card_no as elec_card_no,oppost_date ";
        sqlCmd += " from tsc_card ";
        sqlCmd += " where current_code = '2' ";
        sqlCmd += sqlCmdStr.toString();
        sqlCmd += " union ";
        sqlCmd += " select '02' as elec_type,ips_card_no as elec_card_no,oppost_date ";
        sqlCmd += " from ips_card ";
        sqlCmd += " where current_code = '2' ";
        sqlCmd += sqlCmdStr.toString();
        sqlCmd += " union ";
        sqlCmd += " select '03' as elec_type,ich_card_no as elec_card_no,oppost_date ";
        sqlCmd += " from ich_card ";
        sqlCmd += " where current_code = '2' ";
        sqlCmd += sqlCmdStr.toString();
        sqlCmd += " union ";
        sqlCmd += " select '04' as elec_type,tsc_card_no as elec_card_no,oppost_date ";
        sqlCmd += " from tsc_vd_card ";
        sqlCmd += " where current_code = '2' ";
        sqlCmd += sqlCmdStr.toString();
        sqlCmd += " ) order by elec_type,oppost_date ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
        	sendData.clearBuf1();
            sendData.elecType= getValue("elec_type");
            sendData.elecCardNo = getValue("elec_card_no");
            sendData.oppostDate = getValue("oppost_date");
            
            buf = sendData.allText();
            writeTextFile(out, buf);
            totalCnt++;
            
            if ((totalCnt % 5000) == 0) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }
        }
        closeCursor(cursorIndex);
        return 0;
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        String temstr = String.format("%s/media/icu/%s", comc.getECSHOME(),fileName);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr, "MS950");
        if (out == -1) {
            comcr.errRtn(temstr, "檔案開啓失敗！","");
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IcuD083 proc = new IcuD083();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    String removeDot(String myStr) {
        return myStr.replaceAll("\\.", "");
    }

    /***********************************************************************/
    /*    	
           欄位名稱	長度	起	迄
	票證註記	2	1	2
	票證卡號	16	3	18
	掛失日期	8	19	26         
	*/
    class Buf1 {
        String elecType; //票證註記 
        String elecCardNo; //票證卡號
        String oppostDate; //掛失日期

        void clearBuf1() throws UnsupportedEncodingException {
        	elecType    = "";
        	elecCardNo  = "";
        	oppostDate  = "";
        }

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(elecType, 2);
            rtn += fixLeft(elecCardNo, 16);
            rtn += fixLeft(oppostDate, 8 );
            rtn += "\r\n";
            return rtn;
        }

    }

    String zroeRight(String str, int len) throws UnsupportedEncodingException {
        String zroe = "";
        for (int i = 0; i < 100; i++)
        	zroe += "0";
        if (str == null)
            str = "";
        str = zroe + str;
        byte[] bytes = str.getBytes("MS950");
        int offset = bytes.length - len;
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, offset, vResult, 0, len);
        return new String(vResult, "MS950");
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
    
    void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
    	commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    	commFTP.hEriaLocalDir = String.format("%s/media/icu", comc.getECSHOME());
    	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    	commFTP.hEflgModPgm = javaProgram;

    	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
    	showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
    	int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fileName);

    	if (errCode != 0) {
    		showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料" + " errcode:" + errCode);
    		insertEcsNotifyLog();
    	}
    }
    
    public int insertEcsNotifyLog() throws Exception {
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

    void renameFile() throws Exception {
    	String tmpstr1 = String.format("%s/media/icu/%s", comc.getECSHOME(), fileName);
    	String tmpstr2 = String.format("%s/media/icu/backup/%s_%s.txt", comc.getECSHOME(), fileName.replaceAll(".txt", ""),sysDate);

    	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
    }

}
