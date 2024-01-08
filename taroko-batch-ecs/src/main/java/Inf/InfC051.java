/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/12/24  V1.00.00    Wendy Lu                     program initial        *
 *  112/04/14  V1.00.01    Nick                         增加呆帳的資訊處理           *
 *  112/04/27  V1.00.02    Sunny                        調整呆帳的資訊處理(個人及法人) *
 ******************************************************************************/

package Inf;

import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommString;

public class InfC051 extends BaseBatch {
	private final String progname = "產生送CRDB 51異動正附卡註記資料檔程式  112/04/27  V1.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	String isFileName = "";
	private int ilFile51;
	String hSysDate = "";
	String hIdNo = "";
	String hCardNo = "";
	String hMark = "";

	public static void main(String[] args) {
		InfC051 proc = new InfC051();
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfC051 [business_date]");
			errExit(1);
		}
		dbConnect();
		if (liArg == 1) {
			hSysDate = args[0];
		}	

		if (empty(hSysDate))
			hSysDate =  commDate.dateAdd(sysDate, 0, 0, -1);
		
		isFileName = "CRU23B1_TYPE_51_" + hSysDate + ".txt";
		
				
		checkOpen();
		selectIdNo(hSysDate);
		closeOutputText(ilFile51);
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile();
		endProgram();
	}


	void selectIdNo(String hSysDate) throws Exception {
		sqlCmd = "with ";
		sqlCmd += "temp as( ";
		sqlCmd += "select card_no, "; 
		sqlCmd += "id_p_seqno, ";
		sqlCmd += "'' as corp_p_seqno, "; 
		sqlCmd += "sup_flag, "; 
		sqlCmd += "acno_flag, ";
		sqlCmd += "case when acno_flag <> '1' then '2' ";
		sqlCmd += "when sup_flag = '0' then '0' ";
		sqlCmd += "else '1' ";
		sqlCmd += "end as mark ";
		sqlCmd += "from crd_card ";
		sqlCmd += "where crt_date = ? ";
		sqlCmd += "union all ";
		sqlCmd += "select card_no, ";
		sqlCmd += "id_p_seqno, ";
		sqlCmd += "'' as corp_p_seqno, "; 
		sqlCmd += "'' as sup_flag, "; 
		sqlCmd += "'' as acno_flag, ";
		sqlCmd += "'3' as mark ";
		sqlCmd += "from dbc_card ";
		sqlCmd += "where crt_date = ? ";
		
		//呆帳的資訊處理
		sqlCmd += "union all ";
		sqlCmd += "select '' as card_no,";
		sqlCmd += "id_p_seqno,";
		sqlCmd += "corp_p_seqno,";
		sqlCmd += "'' as sup_flag,";
		sqlCmd += "acno_flag,";
		sqlCmd += "'4' as mark ";
		sqlCmd += "from act_acno ";
		sqlCmd += "where status_change_date = ? AND ACCT_STATUS='4'";
	
		sqlCmd += ") ";
		sqlCmd += "select a.card_no, b.id_no, a.mark ";
		sqlCmd += "from temp a ";
		sqlCmd += "inner join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
		sqlCmd += "where a.mark = '3' ";
		sqlCmd += "union all ";
		sqlCmd += "select a.card_no, b.id_no, a.mark ";
		sqlCmd += "from temp a ";
		sqlCmd += "inner join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
		sqlCmd += "where a.mark = '4' and acno_flag='1'"; //呆帳--個人
		sqlCmd += "union all ";
		sqlCmd += "select a.card_no, b.corp_no, a.mark ";
		sqlCmd += "from temp a ";
		sqlCmd += "inner join crd_corp b on a.corp_p_seqno = b.corp_p_seqno ";
		sqlCmd += "where a.mark = '4' and acno_flag='2'"; //呆帳--公司
		
		setString(1, hSysDate);
		setString(2, hSysDate);
		setString(3, hSysDate);
		
		int llCnt = selectTable();
		for (int ii = 0; ii < llCnt; ii++) {
			hCardNo = colSs(ii, "card_no");
			hIdNo = colSs(ii, "id_no");
			hMark = colSs(ii, "mark");
			writeTextFile();
		}
	}


	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		ilFile51 = openOutputText(lsTemp , "big5" );
		if (ilFile51 < 0) {
			printf("CRU23B1-TYPE-51 產檔失敗 ! ");
			errExit(1);
		}
	}
	
	void writeTextFile() throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String newLine = "\r\n";
			tempBuf.append("51"); // --代碼 固定 51
			tempBuf.append(comc.fixLeft(hCardNo, 16)); 
			tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
			tempBuf.append(comc.fixLeft(hMark, 1));
			tempBuf.append(comc.fixLeft("", 120)); 
			tempBuf.append(newLine);
			totalCnt++;		
		this.writeTextFile(ilFile51, tempBuf.toString());
	}

	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

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
