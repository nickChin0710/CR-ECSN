/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/04/22  V1.00.00    Alex      program initial							 * 
*  109-07-03  V1.00.01    shiyuqi       updated for project coding standard  *  
*  109-07-22  V1.00.02    yanghan       修改了字段名称                                                                           *                          
*  109-10-19  V1.00.03    shiyuqi       updated for project coding standard  *
*  109-11-11  V1.00.04    tanwei        updated for project coding standard  *
*  110-01-30  V1.00.05    Alex          改用營業日								 *  
*  111/02/14  V1.00.06    Ryan      big5 to MS950                            *
*  112/03/29  V1.00.07    Ryan         比對參數日期若相同讀出全檔資料                                                 *
*  2023-1206  V1.00.08    JH        fileName: 日期-1天                                                                      *
*  113/01/03  V1.00.09    Wilson    調整讀取全檔邏輯                                                                                    *
*****************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;
import com.BaseBatch;

public class InfC029 extends BaseBatch {
	private final String progname = "產生送CRDB 29 控管碼原因異動  113/01/03 V1.00.09";
	CommCrd comc = new CommCrd();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	String isFileName = "";
	private int ilFile29;
	String is_procDate = "";
	String hIdNo = "";
	String hOppoStatus = "";
	String hCardNo = "";
	String hAcnoPSeqno = "";
	String hSonCardFlag = "";
	String hAcctType = "";
	int hBefAmt = 0;
	int hAftAmt = 0;
	int hAftCash = 0;
	int hCardAcctIdx = 0;
	int hCardAdjLimit = 0;
	double hCalBefAmt = 0.0;
	double hCalAftAmt = 0.0;
	double hCalBefCash = 0.0;
	double hCalAftCash = 0.0;
	String hCardAdjDate1 = "";
	String wfValue = "";
	int totCnt = 0;

	public static void main(String[] args) {
		InfC029 proc = new InfC029();
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfC029 [business_date]");
			okExit(0);
		}

		dbConnect();
		selectPtrSysParm();
		printf("PTR_SYS_PARM讀取參數日期=[%s]", wfValue);

		if (liArg == 1) {
		   if (commDate.isDate(args[0])) {
            is_procDate = args[0];
         }
		}
		if (empty(is_procDate)) {
         is_procDate = commDate.dateAdd(hBusiDate,0,0,-1);
      }
		isFileName = "CRU23B1_TYPE_29_" +is_procDate+ ".txt";
      printf(" Process: busi_date[%s], proc_date[%s], proc_file[%s]"
          ,hBusiDate, is_procDate, isFileName);

		checkOpen();
		selectDataType29();
		closeOutputText(ilFile29);
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile();
		//------
		sqlCommit();
		endProgram();
	}
	
	void selectPtrSysParm() throws Exception {
		extendField = "PARM.";
		sqlCmd = "SELECT WF_VALUE FROM PTR_SYS_PARM WHERE WF_KEY = 'INFC029'";
		selectTable();
		wfValue = getValue("PARM.WF_VALUE");
	}

	void selectDataType29() throws Exception {
		
		if(!eq(wfValue, is_procDate)) {
			
			sqlCmd = "";
			sqlCmd += " select card_no , oppo_status , debit_flag from cca_opposition ";
			sqlCmd += " where oppo_date = ? ";
			sqlCmd += " union ";
			sqlCmd += " select card_no , '' as oppo_status , debit_flag from cca_opposition ";
			sqlCmd += " where logic_del_date = ? ";
			setString(1, is_procDate);
			setString(2, is_procDate);

		}else {
			printf("參數日期[%s]比對相同，讀全檔資料", wfValue);   
			
			sqlCmd = "";
			sqlCmd += " select card_no , oppost_reason as oppo_status, 'N' as debit_flag from crd_card ";
			sqlCmd += " union ";
			sqlCmd += " select card_no , oppost_reason as oppo_status, 'Y' as debit_flag from dbc_card ";

		}
		
		openCursor();
		while (fetchTable()) {
			
			initData();		    
			totCnt++;
		      		      
			if(totCnt % 50000 == 0 || totCnt == 1)		    
				showLogMessage("I","",String.format(" Process 1 record=[%d]\n", totCnt));

			hCardNo = colSs("card_no");
			hOppoStatus = colSs("oppo_status");
			hIdNo = getMajorId(hCardNo);
			writeTextFile29();
		}

		closeCursor();
	}

	void writeTextFile29() throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String newLine = "\r\n";
		tempBuf.append("29"); // --代碼 固定 29
		tempBuf.append(comc.fixLeft(hCardNo, 16)); // --卡號
		tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
		tempBuf.append(hOppoStatus); // --停掛原因碼
		tempBuf.append(comc.fixLeft("", 119)); // --保留 119
		tempBuf.append(newLine);
		totalCnt++;
		this.writeTextFile(ilFile29, tempBuf.toString());
	}

	String getMajorId(String cardNo) throws Exception {
		sqlCmd = " select uf_idno_id2(major_id_p_seqno,debit_flag) as id_no from cca_card_base where card_no = ? ";
		setString(1, cardNo);
		sqlSelect();
		if (sqlNrow > 0)
			return colSs("id_no");
		return "";
	}

	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		ilFile29 = openOutputText(lsTemp, "MS950");
		if (ilFile29 < 0) {
			printf("CRU23B1-TYPE-29 產檔失敗 ! ");
			okExit(0);
		}
	}

	void initData() {
		hIdNo = "";
		hOppoStatus = "";
		hCardNo = "";
		hSonCardFlag = "";
		hAcctType = "";
		hAcnoPSeqno = "";
		hCardAcctIdx = 0;
		hBefAmt = 0;
		hAftAmt = 0;
		hAftCash = 0;
		hCalBefAmt = 0.0;
		hCalAftAmt = 0.0;
		hCalBefCash = 0.0;
		hCalAftCash = 0.0;
		hCardAdjDate1 = "";
		hCardAdjLimit = 0;
	}

	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
		}
	}

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

	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/crdb/%s", getEcsHome(), isFileName);
		String tmpstr2 = String.format("%s/media/crdb/backup/%s", getEcsHome(), isFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + isFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}
