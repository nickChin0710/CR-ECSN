/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------* 
 *  111/02/13  V1.00.01    Alex      program initial                                  *
 *  111/03/02  V1.00.02    Alex      write file add new line                          *
 *  111/03/08  V1.00.03    Alex      add closeOutPut                                  *
 *  111/04/27  V1.00.04    Alex      未生日的臨調也要產出                                                                                          *
 **************************************************************************************/
package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class RskP140 extends BaseBatch {
	
	private final String progname = "每日撈有效臨調資料給資訊部 111/04/27 V1.00.04";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	final String fileName = "TCBQUAT.txt";
	private int iiFileNum = 0;
	int seqNo = 0;
	String idPSeqno = "";
	String acnoPSeqno = "";
	String adjStartDate = "";
	String adjEndDate = "";
	int totAmtMonth = 0;
	
	public static void main(String[] args) {
		RskP140 proc = new RskP140();
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP140 [business_date]");
			errExit(1);
		}
		
		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}
		
		dateTime();
		
		checkOpen();
		processData();
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		//--放入backup
		renameFile();									
		endProgram();
	}
	
	void processData() throws Exception {		
		//--個人戶臨調
		personAdj();		
	}
	
	void personAdj() throws Exception {
		//--個人戶臨調
		sqlCmd = "select id_p_seqno , acno_p_seqno , tot_amt_month , adj_eff_start_date , adj_eff_end_date from cca_card_acct "
				+ " where ? <= adj_eff_end_date and adj_eff_end_date <> '' "				
				+ " and acno_flag = '1' and debit_flag <> 'Y' ";
		setString(1,hBusiDate);		
				
		openCursor();
				
		while(fetchTable()) {
			initData();			
			idPSeqno = colSs("id_p_seqno");
			acnoPSeqno = colSs("acno_p_seqno");		
			adjStartDate = colSs("adj_eff_start_date");
			adjEndDate = colSs("adj_eff_end_date");
			totAmtMonth = colInt("tot_amt_month");
			selectCrdCard();
		}
		closeCursor();
		closeOutputText(iiFileNum);
	}
	
	void selectCrdCard() throws Exception {
		
		sqlCmd = "select card_no , uf_idno_id(major_id_p_seqno) as id_no from crd_card where acno_p_seqno = ? and son_card_flag <> 'Y' ";
		setString(1,acnoPSeqno);
		
		sqlSelect();
		
		if(sqlNrow <=0)
			return ;
		
		int sqlCnt = sqlNrow ;
		String tempCard = "" , tempIdNo = "";
		for(int ii=0 ; ii<sqlCnt ; ii++) {
			tempCard = colSs(ii,"card_no");
			tempIdNo = colSs(ii,"id_no");
			writeFile(tempCard,tempIdNo);
		}
		
	}
	
	void writeFile(String cardNo , String idNo) throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String tempDate = "" , tempDate1 = "" , tempDate2 = "" , tempSeq = "" , tempAmt = "" , newLine = "\r\n";
		tempDate = adDate2TwDate(hBusiDate);
		tempDate1 = adjStartDate.substring(4,6) + adjStartDate.substring(6,8) + adjStartDate.substring(2,4);
		tempDate2 = adjEndDate.substring(4,6) + adjEndDate.substring(6,8) + adjEndDate.substring(2,4);
		seqNo++;
		
		tempSeq = commString.lpad(commString.int2Str(seqNo), 4, "0");
		tempAmt = commString.lpad(commString.int2Str(totAmtMonth), 9, "0");
		tempBuf.append("2");
		tempBuf.append(comc.fixLeft(tempDate+tempSeq+"  ",13));
		tempBuf.append("11");
		tempBuf.append("3144");
		tempBuf.append(comc.fixLeft(idNo, 10));
		tempBuf.append(comc.fixLeft(cardNo, 16));
		tempBuf.append(comc.fixLeft(tempAmt, 9));
		tempBuf.append(tempDate1);
		tempBuf.append(tempDate2);
		tempBuf.append(commString.space(20));
		tempBuf.append("END");	
		tempBuf.append(newLine);
		writeTextFile(iiFileNum, tempBuf.toString());
	}
	
	String adDate2TwDate(String date) {
		int temp = 0;
		String tempDate = "";
		temp = Integer.parseInt(date.substring(0,4))-1911;
		tempDate = String.format("%03d", temp) + date.substring(4);		
		return tempDate ;
	}
	
	void checkOpen() throws Exception {
		String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);		
		
		iiFileNum = openOutputText(lsFile,"big5");
		if (iiFileNum < 0) {
			showLogMessage("I", "", "TCBQUAT.txt 產檔失敗 !");
			errExit(1);
		}				
		return;
	}
	
	void initData() {
		idPSeqno = "";
		acnoPSeqno = "";
		adjStartDate = "";
		adjEndDate = "";
		totAmtMonth = 0;
	}
	
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileName);
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
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName+"_"+hBusiDate);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");				
	}
	
}
