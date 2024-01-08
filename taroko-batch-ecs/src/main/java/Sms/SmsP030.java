/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  111-01-11  V1.00.01   Alex       init		                             *
*  111-01-24  V1.00.02   Alex       增加判斷參數效期和發送旗標                                                          *
*****************************************************************************/
package Sms;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class SmsP030 extends BaseBatch {
	private final String progname = "收徵審系統婉拒名單檔寫入批次簡訊檔  111/01/11 V1.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	private final String fileNameIn = "TLIMIT.TXT";
	private final String fileNameOut = "TLIMIT_R.TXT";
	private final String newLine = "\r\n";
	int fileIn = 0 , fileOut = 0 ;		
	
	String projectNo = "";
	String idNo = "";
	String cellarPhone = "";
	String approveFlag = "";
	String idPSeqno = "";
	String chiName = "";
	//--門檻參數
	String smsMsgSeqno = "";
	String smsMsgId = "";
	String smsUsrId = "";
	String smsDept = "";
	String smsEffDate1 = "";
	String smsEffDate2 = "";
	String smsSendFlag = "";
	boolean notSendFlag = false;
		
	int ilTotalCnt = 0;	
	
	private int tiSmsdtl = -1;
	
	public static void main(String[] args) {
		SmsP030 proc = new SmsP030();
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : SmsP030 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}
		
		dateTime();		
				
		//--取得簡訊發送門檻
		getSmsParm();
		if(notSendFlag == false) {
			showLogMessage("I", "", "沒有有效婉拒簡訊參數 (sms_msg_id SMSP030) 或是日期不在發送效期內或是參數設定為不發送簡訊 所以不執行發送程式");
			okExit(0);
			return ;
		}
								
		checkOpen();	
		processData();
		
		//--FTP
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		//--BackUp File
		renameFile();
						
		endProgram();
	}
	
	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileNameIn);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileNameIn+hBusiDate);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameIn + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameIn + "] 已移至 [" + tmpstr2 + "]");
		
		String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileNameOut);
		String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileNameOut+hBusiDate);
		
		if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameOut + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameOut + "] 已移至 [" + tmpstr4 + "]");		
	}
	
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileNameOut + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fileNameOut);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileNameOut + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileNameOut);
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
	
	void processData() throws Exception {
		while (true) {
			String fileData = readTextFile(fileIn);
			if (endFile[fileIn].equals("Y")) {
				break;
			}
			if (empty(fileData))
				break;						
			totalCnt ++;
			initData();
			fileData = removeBom(fileData);
			splitData(fileData);
			//--婉拒案件才送簡訊處理			
			getData();
			if(cellarPhone.isEmpty() || "N".equals(approveFlag) == false ) {
				//--查無手機號碼或核准案件直接寫檔不新增簡訊檔
				writeFile();
				continue;
			}					
			procSms();
			writeFile();			
		}
		
		closeInputText(fileIn);
		closeOutputText(fileOut);
	}
	
	void writeFile() throws Exception {
		//--案件編號(11位)客戶ID(10位)手機電話(10位)
		String outData = "";				
		outData  = comc.fixLeft(projectNo, 11);
		outData += comc.fixLeft(idNo, 10);
		outData += comc.fixLeft(cellarPhone,10);		
		outData += newLine;
		writeTextFile(fileOut, outData);		
	}
	
	void procSms() throws Exception {		
		// --組合 msg_desc , 格式 :msg_userid , msg_id , cellar_phone , 參數1 , 參數 2 ...	
		String lsTempDesc = "";
		lsTempDesc = smsUsrId + "," + smsMsgId + "," + cellarPhone;

		// --Insert 簡訊資料 待批次處理發送
		if (tiSmsdtl <= 0) {
			sqlCmd = " insert into sms_msg_dtl ( ";
			sqlCmd += " msg_seqno , ";
			sqlCmd += " msg_dept , ";
			sqlCmd += " msg_userid , ";
			sqlCmd += " msg_pgm , ";
			sqlCmd += " id_p_seqno ,";
			sqlCmd += " p_seqno , ";
			sqlCmd += " id_no ,";
			sqlCmd += " acct_type ,";
			sqlCmd += " msg_id , ";
			sqlCmd += " cellar_phone , ";
			sqlCmd += " cellphone_check_flag , ";
			sqlCmd += " chi_name , ";
			sqlCmd += " msg_desc , ";
			sqlCmd += " add_mode , ";
			sqlCmd += " resend_flag , ";
			sqlCmd += " send_flag , ";
			sqlCmd += " proc_flag , ";
			sqlCmd += " crt_date , ";
			sqlCmd += " crt_user , ";
			sqlCmd += " apr_date , ";
			sqlCmd += " apr_user , ";
			sqlCmd += " apr_flag , ";
			sqlCmd += " mod_user , ";
			sqlCmd += " mod_time , ";
			sqlCmd += " mod_pgm , ";
			sqlCmd += " mod_seqno ";
			sqlCmd += " ) values ( ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " 'Y' ,";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " 'B' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " 'SYSTEM' , ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " 'SYSTEM' , ";
			sqlCmd += " 'Y' , ";
			sqlCmd += " 'SYSTEM' , ";
			sqlCmd += " sysdate , ";
			sqlCmd += " 'SmsP030' , ";
			sqlCmd += " 1 ";
			sqlCmd += " ) ";

			daoTable = "sms_msg_dtl-A";
			tiSmsdtl = ppStmtCrt("", "");
		}

		setString(1, smsMsgSeqno);
		setString(2, smsDept);
		setString(3, smsUsrId);
		setString(4, "SMSP030");
		setString(5, idPSeqno);
		setString(6, "");
		setString(7, idNo);
		setString(8, "");
		setString(9, smsMsgId);
		setString(10, cellarPhone);
		setString(11, chiName);
		setString(12, lsTempDesc);

		sqlExec(tiSmsdtl);
		if (sqlNrow <= 0) {
			errmsg("insert sms_msg_dtl error ");
			errExit(1);
		}

	}
	
	void getData() throws Exception {
		
		if(idNo.isEmpty())
			return ;
		
		sqlCmd = " select cellar_phone , chi_name , id_p_seqno from crd_idno where id_no = ? ";
		setString(1,idNo);
		sqlSelect();
		if(sqlNrow > 0) {
			cellarPhone = colSs("cellar_phone");
			chiName = colSs("chi_name");
			smsMsgSeqno = getMsgSeqno();
			idPSeqno = colSs("id_p_seqno");
			return ;
		}
		
		//--信用卡找不到手機再找 VD 卡
		
		sqlCmd = " select cellar_phone , chi_name , id_p_seqno from dbc_idno where id_no = ? ";
		setString(1,idNo);
		sqlSelect();
		if(sqlNrow > 0) {
			cellarPhone = colSs("cellar_phone");
			chiName = colSs("chi_name");
			smsMsgSeqno = getMsgSeqno();
			idPSeqno = colSs("id_p_seqno");
			return ;
		}				
	}
	
	String getMsgSeqno() throws Exception {
		String tempSeqno = "";
		
		sqlCmd = "select lpad(to_char(ecs_modseq.nextval),10,'0') as sms_seqno from dual ";
		sqlSelect();
		
		tempSeqno = colSs("sms_seqno");
		
		return tempSeqno ;
	}
	
	void checkOpen() throws Exception {
		String filePathIn = "" , filePathOut = "";
		filePathIn = String.format("%s/media/rsk/%s", this.getEcsHome(), fileNameIn);
		fileIn = openInputText(filePathIn, "UTF-8");
		if (fileIn < 0) {
			this.showLogMessage("I", "", "無檔案可處理 !");		
			okExit(0);
		}
		
		filePathOut = String.format("%s/media/rsk/%s", this.getEcsHome(), fileNameOut);
		fileOut = openOutputText(filePathOut,"UTF-8");
		if (fileOut < 0) {
			printf("TLIMIT_R.TXT 產檔失敗 ! ");
			errExit(1);
		}
	}
	
	void splitData(String fileData) throws Exception {
		//--案件編號(11位)客戶ID(10位)婉拒件 (01位)(帶N就是婉拒) 
		byte[] bytes = fileData.getBytes("MS950");
		projectNo = comc.subMS950String(bytes, 0, 11).trim();
		idNo = comc.subMS950String(bytes, 11, 10);
		approveFlag = comc.subMS950String(bytes, 21, 1).trim();		
	}
	
	void initData() {
		projectNo = "";
		idNo = "";
		cellarPhone = "";
		approveFlag = "";
		idPSeqno = "";
		smsMsgSeqno = "";
	}
	
	void getSmsParm() throws Exception {
		
		sqlCmd = "select msg_id , msg_userid , msg_dept , msg_send_flag , send_eff_date1 , send_eff_date2 from sms_msg_id where 1=1 ";
		sqlCmd += " and msg_pgm = 'SMSP030'";				
		
		sqlSelect();
		
		if(sqlNrow > 0) {			
			smsMsgId = colSs("msg_id");	
			smsUsrId = colSs("msg_userid");
			smsDept = colSs("msg_dept");
			smsEffDate1 = colSs("send_eff_date1");
			smsEffDate2 = colSs("send_eff_date2");
			smsSendFlag = colSs("msg_send_flag");
			//--不發送旗標
			if("Y".equals(smsSendFlag) == false) {
				notSendFlag = false;
				return ;
			}
			
			//--不在效期內
			if(smsEffDate1.isEmpty() == false) {
				if(sysDate.compareTo(smsEffDate1) <0) {
					notSendFlag = false ;
					return ;
				}
			}			
			
			if(smsEffDate2.isEmpty() == false) {
				if(sysDate.compareTo(smsEffDate2) >0) {
					notSendFlag = false ;
					return ;
				}
			}			
			
			notSendFlag = true;
		}	else	{
			notSendFlag = false ;
			return ;
		}
		
	}
	
	String removeBom(String oriData) {
		String proData = "", bomString = "\uFEFF";
		if (oriData.startsWith(bomString)) {
			proData = oriData.replace(bomString, "");
		} else {
			proData = oriData;
		}

		return proData;
	}
	
}
