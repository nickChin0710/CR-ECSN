/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  111-04-27  V1.00.04   Alex       只比一般戶                                                                                       *
*  111-02-17  V1.00.03   Alex       add error msg                            *
*  110-02-20  V1.00.02   Alex       bug fix									 *
*  110-02-17  V1.00.01   Alex       program initial                          * 
*****************************************************************************/
package Rsk;

import com.BaseBatch;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class RskP120 extends BaseBatch {
	
	private final String progname = "CARDLINK 和 ECS 額度比對作業  111/04/27 V1.00.04";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	//--CUST_ALL_CRLIMIT_YYYYMMDD.TXT 
	String fileNameIn = "";
	String fileNameOut = "";
	int fileIn = 0 , fileOut = 0 ;
	String hAcctType = "";
	String hAcctKey = "";
	String hIdNo = "";		
	String hCycle = "";
	int hCardLinkLimit = 0;
	int hEcsLimit = 0;
	boolean ibCorp = false ;
	boolean ibUnMatch = false ;
	boolean ibNotFound = false ;
	boolean ibNoCard = false ;
	boolean ibNoPerson = false; 
	int printCnt = 0;
	private final String newLine = "\r\n";
	
	public static void main(String[] args) {
		RskP120 proc = new RskP120();
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP120 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (liArg == 0) {
			//--因 TCB排程 CCR111FD, CCR300FD 有時候會跑到3點多會超過ECS更換營業日的排程
			//--所以將此程式排程安排在更換營業日後 , 在此需要 -1 天			
			hBusiDate = commDate.dateAdd(hBusiDate, 0, 0, -1);
		}			

		dateTime();

		fileNameIn = "CUST_ALL_CRLIMIT_" + hBusiDate + ".TXT";
		fileNameOut = "CARDLINK_ECS_QUOTA_UNMATCH_" + hBusiDate + ".TXT";
		
		checkOpen();		
		processData();
		
		//--FTP
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		//--BackUp File
		renameFile();
		
		showLogMessage("I", "", "額度不符筆數:" + printCnt);
		endProgram();
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
	
	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileNameIn);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileNameIn);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameIn + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameIn + "] 已移至 [" + tmpstr2 + "]");
		
		String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileNameOut);
		String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileNameOut);
		
		if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameOut + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameOut + "] 已移至 [" + tmpstr4 + "]");		
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
			if(totalCnt % 100000 == 0)
				showLogMessage("I", "", "處理筆數 = ["+totalCnt+"]");
			initData();			
			splitData(fileData);
//			checkCard();
//			if(ibNoPerson||ibNoCard) {
//				writeFile();
//				continue;
//			}
			
			selectActAcno();
			
			//--找不到帳戶不寫檔
			if(ibNotFound)
				continue ;
					
			//--額度相同則不寫檔
			if(ibUnMatch == false)
				continue;
			writeFile();			
		}
		
		closeInputText(fileIn);
		closeOutputText(fileOut);
	}
	
	void checkCard() throws Exception {
		String idPSeqno = "";
		if(hIdNo.length() !=10)
			return ;
		
		sqlCmd = "select id_p_seqno from crd_idno where id_no = ?";
		setString(1,hIdNo);
		sqlSelect();
		
		if(sqlNrow <=0) {
			ibNoPerson = true ;
			return ;
		}
		
		idPSeqno = colSs("id_p_seqno");
		
		sqlCmd = "select count(*) as db_cnt from crd_card where id_p_seqno = ? ";
		setString(1,idPSeqno);
		sqlSelect();
		
		int temp = 0;
		temp = colInt("db_cnt");
		
		if(temp <=0) {
			ibNoCard = true;
			return ;
		}				
		
	}
	
	void writeFile() throws Exception {
		String outData = "";
		if(printCnt == 0) 			
			printTitle();						
		outData  = comc.fixLeft(hIdNo, 11);
		outData += commString.space(10);
		outData += comc.fixLeft(commString.int2Str(hCardLinkLimit), 15);
		outData += commString.space(10);
		outData += comc.fixLeft(commString.int2Str(hEcsLimit), 15);
		outData += commString.space(10);
		outData += newLine;
		writeTextFile(fileOut, outData);
		printCnt ++ ;
	}
	
	void printTitle() throws Exception {
		String titleItem = "";
		titleItem = "身分證/統編" + commString.space(10);
		titleItem += "CARDLINK 額度" + commString.space(10);
		titleItem += "ECS 額度" + commString.space(10);
		titleItem += newLine;
		writeTextFile(fileOut, titleItem);
		titleItem = commString.repeat("=", 60);
		titleItem += newLine;
		writeTextFile(fileOut, titleItem);
	}
	
	void selectActAcno() throws Exception {
		
		if(ibCorp == false) {
			sqlCmd = "select line_of_credit_amt from act_acno where acct_type = ? and acct_key = ? ";
			setString(1,hAcctType);
			setString(2,hAcctKey);	
		}	else	{
			sqlCmd = "select line_of_credit_amt from act_acno where acct_key = ? and stmt_cycle = ? ";
			setString(1,hAcctKey);
			setString(2,hCycle);
		}
			
		sqlSelect();
		if(sqlNrow <=0) {
			ibUnMatch = true ;
			ibNotFound = true ;
			return ;
		}
		
		hEcsLimit = colInt("line_of_credit_amt");
		
		if(hCardLinkLimit != hEcsLimit)
			ibUnMatch = true;
		
		return ;		
	}
	
	void splitData(String fileData) throws Exception {
		byte[] bytes = fileData.getBytes("MS950");
		hIdNo = comc.subMS950String(bytes, 0, 11).trim();
		hCardLinkLimit = commString.ss2int(comc.subMS950String(bytes, 11, 12));
		hCycle = comc.subMS950String(bytes, 23, 2).trim();
//		showLogMessage("I", "", "ID:"+hIdNo+" limit:"+hCardLinkLimit+" Cycle:"+hCycle);
		if(checkCrdIdNo(hIdNo)) {
			ibCorp = false ;
			hAcctType = "01";
			if(hIdNo.length()==10) {
				hAcctKey = hIdNo + "0";
			}	else	{
				hAcctKey = hIdNo ;
			}
		}	else	{
			ibCorp = true ;
			hAcctKey = hIdNo;
		}		
	}
	
	boolean checkCrdIdNo(String idNo) throws Exception {
		
		sqlCmd = " select id_p_seqno from crd_idno where id_no = ? ";
		setString(1,idNo);
		
		sqlSelect();
		if(sqlNrow >0) 
			return true ;		
		
		return false;
	}
	
	void checkOpen() throws Exception {
		String filePathIn = "" , filePathOut = "";
		filePathIn = String.format("%s/media/rsk/%s", this.getEcsHome(), fileNameIn);
		fileIn = openInputText(filePathIn, "MS950");
		if (fileIn < 0) {
			this.showLogMessage("I", "", "無檔案可處理 !");		
			okExit(0);
		}
		
		filePathOut = String.format("%s/media/rsk/%s", this.getEcsHome(), fileNameOut);
		fileOut = openOutputText(filePathOut,"MS950");
		if (fileOut < 0) {
			printf("CARDLINK_ECS_QUOTA_UNMATCH 產檔失敗 ! ");
			errExit(1);
		}
	}
	
	void initData() {
		hAcctType = "";
		hIdNo = "";	
		hCycle = "";
		hCardLinkLimit = 0;
		hEcsLimit = 0;
		hAcctKey = "";
		ibCorp = false ;
		ibUnMatch = false ;
		ibNotFound = false ;
		ibNoCard = false ;
		ibNoPerson = false ;
	}
	
}
