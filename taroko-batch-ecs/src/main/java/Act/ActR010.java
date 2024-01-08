/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/12/29  V1.00.01    Brian     error correction                          *
*  109/07/09  V1.00.02    Pino      program initial                           *
*  111/07/22  V1.00.03    Sunny     使用comc.fileMove能覆蓋同檔名                  *
*  111/07/28  V1.00.04    Sunny     調整錯誤訊息，無檔案不當掉                        *
*  112/03/17  V1.00.05    Nick      調整新檔案規格                        		  *
*  112/05/18  V1.00.06    Sunny     有雙幣卡的匯率才寫入歷史檔                   *
******************************************************************************/

package Act;

import java.util.List;

import com.AccessDAO;
import com.CommCpi;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*銀行幣別匯率檔處理程式*/
public class ActR010 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;
    private static final String FILE_NAME = "FX_DCRATE.TXT";
    private String PROGNAME = "銀行幣別匯率檔處理程式  112/05/18  V1.00.06";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommCpi comcpi = new CommCpi();

    String prgmId = "ActR010";
    String h_call_batch_seqno = "";

    String fileDate = "";
    String h_busi_business_date = "";
	String getFileName;
	int totalInputFile;
    String procCode = "";
    String ecsFtpLogRowId = "";
    String h_pcre_curr_code_gl = "";
    double h_pcre_exchange_rate_in = 0;
    double h_pcre_exchange_rate = 0;
    String h_pcre_curr_code = "";
    String h_pcce_bill_curr_code = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : ActR010 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, h_call_batch_seqno);
			
            select_ptr_businday();
            
			if (args.length == 0) {
				fileDate = h_busi_business_date;
			} else if (args.length == 1) {
				fileDate = args[0];
			}
			if (fileDate.length() != 8) {
				comc.errExit("Usage : " + prgmId, "business_date[yyyymmdd]");
			}

			//確認檔案收取處理情況
			String retProcCode = select_ecs_ftp_log();
			if (retProcCode.equals("0")) {
				openFile();
				
				//若ptr_curr_rate檔案中已經有901台幣的資料，就不必再insert或update
				
				if (select_ptr_curr_rate() ==0)
				{
					h_pcce_bill_curr_code = "TWD";
					h_pcre_exchange_rate_in = 1;
					h_pcre_exchange_rate = 1;
					
					select_ptr_currcode();			
					
					if (update_ptr_curr_rate() != 0)
						insert_ptr_curr_rate();		
				}
			}
			else {
				if (retProcCode.equals("Y"))
				{
					showLogMessage("I", "", " Warning: 今日檔案已處理完畢! ");
				}
				else
				{
					showLogMessage("I", "", " Warning: 今日無檔案可處理! ");
				}
			}
			
			
			
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
    void select_ptr_businday() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", h_call_batch_seqno);
        }
        h_busi_business_date = getValue("business_date");
        showLogMessage("I", "", " business_date =[" + h_busi_business_date + "]");
    }
    /***********************************************************************/
    String select_ecs_ftp_log() throws Exception {
        sqlCmd = "select proc_code,rowid as rowid ";
        sqlCmd += "from ecs_ftp_log ";
        sqlCmd += "where trans_resp_code = 'Y' ";
        sqlCmd += "and system_id = 'ACT_FTP_GET' "; 
        sqlCmd += "and proc_code in ('0','1','9','Y') ";
        sqlCmd += "and file_name = ? ";
        sqlCmd += "and file_date = ? ";       
        setString(1, FILE_NAME);
        setString(2, fileDate);
        selectTable();
        showLogMessage("I", "", " file_name =[" + FILE_NAME + "]");
        showLogMessage("I", "", " file_date =[" + fileDate + "]");
        if (notFound.equals("Y")) {
            //comcr.errRtn("select_ecs_ftp_log not found!", "", h_call_batch_seqno)
        	//showLogMessage("I","","ERROR:select_ecs_ftp_log not found!");
        	return "X";
        }
        procCode = getValue("proc_code");
 
        ecsFtpLogRowId = getValue("rowid");
		showLogMessage("I", "", " proc_code =[" + procCode + "]");
		return procCode;
    }
	/***********************************************************************/
	int openFile() throws Exception {
		
		showLogMessage("I", "", " open file process");
		
		int fileCount = 0;

		String tmpstr = String.format("%s/media/act", comc.getECSHOME());
		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		for (String file : listOfFiles) {
			getFileName = file;
			if (!getFileName.equals(FILE_NAME))
				continue;
			fileCount++;
			
			initialField();
			readFile(getFileName);
		}
		if (fileCount < 1) {
			//comcr.hCallErrorDesc = "Error : 無檔案可處理";
			//comcr.errRtn("Error : 檔案不存在", "" , h_call_batch_seqno);
			showLogMessage("I","","ERROR:檔案不存在於指定目錄!");
		}
		return (0);
	}
	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = comc.getECSHOME() + "/media/act/" + fileName;

		int f = openInputText(fileName2);
		if (f == -1) {
			return 1;
		}
		closeInputText(f);

		setConsoleMode("N");
		fi = openInputText(fileName2, "big5");
		setConsoleMode("Y");
		if (fi == -1) {
			return 1;
		}

		showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/act ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
			if ((Integer.parseInt(rec.substring(0, 8)))==Integer.parseInt(fileDate)) {
				totalInputFile++;
	            proc_curr_rate(rec);
			}else {
				continue;
			}
			processDisplay(1000);
		}
		if(totalInputFile<1) {
			showLogMessage("I","","ERROR:今日無匯率異動資料，請確認匯率檔內容是否正確!");
			//exitProgram(1);
		}
		closeInputText(fi);
		
		updateEcsFtpLog();

		renameFile(fileName);

		return 0;
	}
	/****************************************************************************/
	void updateEcsFtpLog() throws Exception {
		daoTable = "ecs_ftp_log";
		updateSQL = "proc_code = 'Y',";
	    updateSQL += "mod_time = sysdate,";
	    updateSQL += "mod_pgm = 'ActR010'";
	    whereStr = "where rowid = ? ";
	    setRowId(1, ecsFtpLogRowId);
		updateTable();
		if (notFound.equals("Y")) {
			//comcr.errRtn("update_ecs_ftp_log not found!", "", h_call_batch_seqno);
			showLogMessage("I","","ERROR:update_ecs_ftp_log not found!");
		}
	}
	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/act/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/act/backup/" + removeFileName + "." + fileDate;
				
		// 使用comc.fileMove能覆蓋同檔名
		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
    /***********************************************************************/
    void proc_curr_rate(String rec) throws Exception {
        
    	h_pcce_bill_curr_code = rec.substring(8, 11);    	

		h_pcre_exchange_rate_in = 0;

		h_pcre_exchange_rate = Double.parseDouble(rec.substring(14, 24)) / 10000;

		showLogMessage("I", "", String.format("[read file] %3.3s   %9.5f  %9.4f", h_pcce_bill_curr_code,
				h_pcre_exchange_rate_in, h_pcre_exchange_rate));

		//insert_ptr_bank_rate_log();
		
		if (select_ptr_currcode() == 0)
		{
			if (update_ptr_curr_rate() != 0)
				insert_ptr_curr_rate();
			
			insert_ptr_bank_rate_log();
		}		
	
    }
    /***********************************************************************/
    void insert_ptr_bank_rate_log() throws Exception {
        daoTable = "ptr_bank_rate_log";
        extendField = daoTable + ".";
        //setValue(extendField + "curr_code", h_pcce_bill_curr_code);
        setValue(extendField + "curr_code", h_pcre_curr_code);     //20230518 sunny 直接存雙幣卡幣別-數字，如840、392。
        setValueDouble(extendField + "curr_rate_i", h_pcre_exchange_rate_in);
        setValueDouble(extendField + "curr_rate_o", h_pcre_exchange_rate);
        setValue(extendField + "send_date", fileDate);
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
        
//    	showLogMessage("I", "", String.format("[insert] %3.3s   %9.5f  %9.4f", h_pcce_bill_curr_code,
//				h_pcre_exchange_rate_in, h_pcre_exchange_rate));
    	
        if (dupRecord.equals("Y")) {
            //comcr.errRtn("insert_ptr_bank_rate_log duplicate!", "", h_call_batch_seqno);
        	showLogMessage("I","","ERROR:insert_ptr_bank_rate_log duplicate!");
        }
    }
    /***********************************************************************/
    int select_ptr_currcode() throws Exception {
        sqlCmd = "select curr_code,";
        sqlCmd += " curr_code_gl ";
        sqlCmd += " from ptr_currcode  ";
        sqlCmd += "where bill_curr_code = ? ";
        sqlCmd += "and bill_curr_code<>'' ";
        setString(1, h_pcce_bill_curr_code);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            h_pcre_curr_code = getValue("curr_code");
            h_pcre_curr_code_gl = getValue("curr_code_gl");
        } else
            return (1);
        
//        showLogMessage("I", "", "select : [ bill_curr_code="+ h_pcce_bill_curr_code + "]");        
//        showLogMessage("I", "", "select : [ curr_code_gl="+ h_pcre_curr_code_gl +"; curr_code="+ h_pcre_curr_code + "]");
        return (0);
    }
    
    /***********************************************************************/
    /*查901是否已存在，已存在為1，不存在為0*/
    
    int select_ptr_curr_rate() throws Exception {
        sqlCmd = "select curr_code";
        sqlCmd += " from ptr_curr_rate  ";
        sqlCmd += "where curr_code = '901'";
         int recordCnt = selectTable();
        if (recordCnt > 0) {
           return (1);
        }          
        return (0);
    }
    /***********************************************************************/
    int update_ptr_curr_rate() throws Exception {
        daoTable   = "ptr_curr_rate";
        updateSQL  = " curr_code_gl     = ?, ";
        updateSQL += " exchange_rate_in = ?, ";
        updateSQL += " exchange_rate    = ?, ";
        updateSQL += " mod_pgm          = 'ActR010', ";
        updateSQL += " mod_time         = sysdate  ";
        whereStr   = "where curr_code   = ? " ;
        setString(1, h_pcre_curr_code_gl);
        setDouble(2, h_pcre_exchange_rate_in);
        setDouble(3, h_pcre_exchange_rate);
        setString(4, h_pcre_curr_code);
        updateTable();
        if (notFound.equals("Y")) {
            return (1);
        }
        
        showLogMessage("I", "", String.format("[update] %3.3s   %3.3s   %9.5f   %9.4f", h_pcre_curr_code_gl, h_pcce_bill_curr_code,
				h_pcre_exchange_rate_in, h_pcre_exchange_rate));
       // showLogMessage("I", "", "update : [ curr_code_gl="+ h_pcre_curr_code_gl +"; curr_code="+ h_pcre_curr_code + "]");
        return (0);
    }
    /***********************************************************************/
    void insert_ptr_curr_rate() throws Exception {
        daoTable = "ptr_curr_rate";
        extendField = daoTable + ".";
        setValue(extendField + "curr_code_gl", h_pcre_curr_code_gl);
        setValueDouble(extendField + "exchange_rate_in", h_pcre_exchange_rate_in);
        setValueDouble(extendField + "exchange_rate", h_pcre_exchange_rate);
        setValue(extendField + "curr_code", h_pcre_curr_code);
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
       
        showLogMessage("I", "", String.format("[insert] %3.3s   %3.3s   %9.5f   %9.4f", h_pcre_curr_code_gl, h_pcre_curr_code,
				h_pcre_exchange_rate_in, h_pcre_exchange_rate));
       // showLogMessage("I", "", "[ curr_code_gl="+ h_pcre_curr_code_gl +"; curr_code="+ h_pcre_curr_code + "]");
    }
    
    void  initialField() {
    	h_pcre_curr_code_gl="";
		h_pcre_curr_code="";
		h_pcre_exchange_rate_in = 0;
		h_pcre_exchange_rate = 0;
		h_pcce_bill_curr_code = "";		
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ActR010 proc = new ActR010();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
