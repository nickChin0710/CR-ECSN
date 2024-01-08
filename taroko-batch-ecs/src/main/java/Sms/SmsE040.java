/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/08/02  V1.00.11  Allen Ho   sms_e040                                   *
* 109-11-18  V1.00.01  tanwei     updated for project coding standard         *
* 112/02/06  V1.00.12  Zuwei Su   update naming rule                         *
* 112/08/08  V1.00.13  Sunny      移除不需要的處理邏輯                                                        *
* 112/08/10  V1.00.14  Ryan       產生附件的EMAIL通知檔                                                       *
* 112/09/08  V1.00.15  Sunny      調整簡訊產生處理的邏輯                                                       *
******************************************************************************/
package Sms;

import com.*;
import java.nio.file.Paths;

@SuppressWarnings("unchecked")
public class SmsE040 extends AccessDAO {
	private final String PROGNAME = "電子發票-中獎信用卡發送簡訊及EMAIL通知處理程式112/09/08  V1.00.15";
	private static final String DATA_FOLDER = "/media/sms/";
	private static final String DATA_FORM = "CARDM08";
	private static final String CRDATACREA = "CRDATACREA";
	private final static String COL_SEPERATOR = "|&";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	CommFunction comm = new CommFunction();
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommDate comDate = new CommDate();
	CommString comStr = new CommString();
	String businessDate = "";
	int smsWriteCnt = 0;
	int smsWriteCnt2 = 0;
	int emailWriteCnt = 0;
	int emailWriteCnt2 = 0;
	long totalCnt = 0;
	StringBuffer writeSb = new StringBuffer();

// ************************************************************************
	public static void main(String[] args) throws Exception {
		SmsE040 proc = new SmsE040();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);

			if (comm.isAppActive(javaProgram))
				return (1);

			if (args.length == 1) {
				businessDate = args[0];
			}

			if (!connectDataBase())
				return (1);

			comr = new CommRoutine(getDBconnect(), getDBalias());

			   if (businessDate.length()==0)
				 selectPtrBusinday();
			 
			 showLogMessage("I", "", "本日營業日 : [" + businessDate + "]");

			// 產生檔案名稱
			String datFileName = String.format("%s__%s.dat", DATA_FORM, businessDate);

			String fileFolder = Paths.get(comc.getECSHOME(), DATA_FOLDER).toString();
			
			checkOpen(fileFolder,datFileName);

			showLogMessage("I", "", "=========================================");
			showLogMessage("I", "", "簡訊設定資料檢核");
			if (selectSmsMsgId() == 0) {
				showLogMessage("I", "", "簡訊設定資料不完整，請確認!");
				return (0);
			}			
			showLogMessage("I", "", "=========================================");				
			showLogMessage("I", "", "    中獎通知 [SMSE040]");
			showLogMessage("I", "", "未列印中獎通知 [SMSE040_2]");
			//showLogMessage("I", "", "未列印中獎通知 [SMSE041]");
			showLogMessage("I", "", "=========================================");

			showLogMessage("I", "", "載入資料");
			selectSmsEinvoDtl();
			
			showLogMessage("I", "", "=========================================");
			showLogMessage("I", "", "產生中獎通知        SMS[" + smsWriteCnt  +" ]筆,MAIL[" + emailWriteCnt  + "]筆");
			showLogMessage("I", "", "產生未列印中獎通知  SMS[" + smsWriteCnt2 +" ]筆,MAIL[" + emailWriteCnt2 + "]筆");
			showLogMessage("I", "", "累計新增 [" + totalCnt + "] 筆");
			showLogMessage("I", "", "=========================================");

			int resultCode = generateDatFile();
			if(resultCode == 0) {
				procFTP(datFileName, fileFolder);
				moveFile(datFileName, fileFolder);
			}
			finalProcess();
			return (0);
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess
// ************************************************************************

	void selectPtrBusinday() throws Exception {
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

    	businessDate = getValue("BUSINESS_DATE");
		//showLogMessage("I", "", "本日營業日 : [" + businessDate + "]");
	}

// ************************************************************************
	void selectSmsEinvoDtl() throws Exception {
		String rowOfDAT = "";
		selectSQL = "p_seqno," + "acct_type," + "id_p_seqno," + "substr(card_no, 13, 4) as card_no,"
				+ "to_char(win_stage-19110000) as win_stage," + "sms_flag," + "vd_flag," + "crt_user,"+ "rowid as rowid";
		daoTable = "sms_einvo_dtl";
		whereStr = "WHERE proc_date = ? " + "AND   sms_flag = 'N' " 
				+ "and   id_p_seqno !='' ";

		setString(1, businessDate);

		openCursor();

		while (fetchTable()) {
			totalCnt++;

//			int smsNum = 0;

			if (getValue("vd_flag").equals("N"))

			{
				if (selectCrdIdno() != 0)
					continue;
			} else {
//				smsNum = 1;
				if (selectDbcIdno() != 0)
					continue;
			}

			setValue("smsd.cellphone_check_flag", "Y");
			
			//如果沒有crt_user就不處理
			if (getValue("crt_user").equals("")) continue; 
				
			if (getValue("idno.cellar_phone").length() != 10)
				setValue("smsd.cellphone_check_flag", "N");

			if (!getValue("idno.cellar_phone").matches("[0-9]+")) // or matches("\\d+")
				setValue("cellphone_check_flag", "N");
			
//			String hMsgId="SmsE040";
//			
//			if (getValue("crt_user").equals("SmsE030_02"))
//				 hMsgId="SmsE040_2";
			
			int smsNum = 0; 
			
			if (getValue("crt_user").equals("SmsE030_02")){
				smsNum = 1; 
			}
			else
			{
				smsNum = 0;  
			}
			
			//sms簡訊變數
			String tmpstr = getValue("msid.msg_userid",smsNum) + "," + getValue("msid.msg_id",smsNum) + ","
//			String tmpstr = getValue("msid.msg_userid") + "," + hMsgId + ","
					+ getValue("idno.cellar_phone") + "," + getValue("win_stage").substring(0, 3) + "," // #變數1-年
					+ getValue("win_stage").substring(3, 5) + "," // #變數2-月份
					+ getValue("win_stage").substring(5, 7) + "," // #變數3-月份
					+ getValue("card_no"); // #變數4-卡號末4碼

			setValue("smsd.msg_desc", tmpstr);

			
			//selectSmsMsgId(hMsgId);
		
			insertSmsMsgDtl(smsNum);
						
			if (smsNum==1){
				//未列印中獎通知
				smsWriteCnt2++;
			}
			else
			{
				//中獎通知
				smsWriteCnt++;
			}			

			updateSmsEinvoDtl(getValue("sms_flag"));

			//處理email
			
			if(!comStr.empty(getValue("idno.e_mail_addr")) & getValue("crt_user").equals("SmsE030_01")) {
				emailWriteCnt++;
				rowOfDAT += getRowOfDetail00();
				rowOfDAT += getRowOfDetail01();
			}
			
			if(!comStr.empty(getValue("idno.e_mail_addr")) & getValue("crt_user").equals("SmsE030_02")) {
				emailWriteCnt2++;
				rowOfDAT += getRowOfDetail200();
				rowOfDAT += getRowOfDetail201();
			}
			
			processDisplay(10); // every 10 display message

		}
		writeSb.append(rowOfDAT);
		closeCursor();
	}

// ************************************************************************
	void updateSmsEinvoDtl(String smsFlag) throws Exception {
		updateSQL = "sms_flag  = 'Y', " + "proc_date = ?," + "mod_time  = sysdate," + "mod_pgm   = ?, ";

		if (smsFlag.equals("N"))
			updateSQL = updateSQL + "sms_date  = ?";
		else
			updateSQL = updateSQL + "resms_date  = ?";

		daoTable = "sms_einvo_dtl";
		whereStr = "where rowid   = ? ";

		//setString(1, sysDate);
		setString(1, businessDate);
		setString(2, javaProgram);
		setString(3, businessDate);
		setRowId(4, getValue("rowid"));

		int cnt = updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "UPDATE sms_msg_dtl_1 error " + getValue("rowid"));
			exitProgram(1);
		}

		return;
	}

// ************************************************************************
	int selectCrdIdno() throws Exception {
		extendField = "idno.";
		selectSQL = "chi_name," + "cellar_phone," + "id_no," + "e_mail_addr";
		daoTable = "crd_idno";
		whereStr = "where id_p_seqno = ?";

		setString(1, getValue("id_p_seqno"));

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select crd_idno error!");
			showLogMessage("I", "", "id_p_seqno[" + getValue("id_p_seqno") + "]");
			return (1);
		}
		return (0);
	}

// ************************************************************************
	int selectDbcIdno() throws Exception {
		extendField = "idno.";
		selectSQL = "chi_name," + "cellar_phone," + "id_no," + "e_mail_addr";
		daoTable = "dbc_idno";
		whereStr = "where id_p_seqno = ?";

		setString(1, getValue("id_p_seqno"));

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select dbc_idno error!");
			showLogMessage("I", "", "id_p_seqno[" + getValue("id_p_seqno") + "]");
			return (1);
		}
		return (0);
	}

// ************************************************************************
	//in ('SMSE040','SMSE041')
	int selectSmsMsgId() throws Exception {
		extendField = "msid.";
		selectSQL = "msg_pgm," +"msg_id," + "msg_dept," + "msg_userid";
		daoTable = "sms_msg_id";
		//whereStr = "WHERE msg_pgm in ('SMSE040','SMSE041') "
		whereStr = "WHERE msg_pgm in ('SMSE040','SMSE040_2') "
				//whereStr = "WHERE msg_pgm in ('SMSE040') "
		         + "AND msg_send_flag ='Y' " + "ORDER BY msg_pgm ";
		//whereStr = "WHERE msg_pgm  ='SMSE040' " + "AND   msg_send_flag ='Y' " + "ORDER BY msg_pgm ";

		int recCnt = selectTable();

		//showLogMessage("I", "", "recCnt="+recCnt);
		
		if (recCnt == 2)
		{
			return (1);
		}

		return (0);
	}
	
	
	// ************************************************************************
		//in ('SMSE040','SMSE041')
		int selectSmsMsgId(String msg_pgm) throws Exception {
			extendField = "msid.";
			selectSQL = "msg_id," + "msg_dept," + "msg_userid";
			daoTable = "sms_msg_id";
			whereStr = "WHERE msg_pgm = ? "
			         + "AND msg_send_flag ='Y' " + "ORDER BY msg_pgm ";

			setString(1,msg_pgm);
			  
			int recCnt = selectTable();

			if (recCnt != 2)
				return (1);

			return (0);
		}


// ************************************************************************
	//void insertSmsMsgDtl(int smsNum) throws Exception {
//  setValue("smsd.msg_dept"             , getValue("msid.msg_dept",smsNum)); 
//  setValue("smsd.msg_userid"           , getValue("msid.msg_userid",smsNum)); 
//  setValue("smsd.msg_id"               , getValue("msid.msg_id",smsNum)); 
// TCB不論一般卡或VD均為同一種簡訊版型
	
	void insertSmsMsgDtl(int smsNum) throws Exception {
		extendField = "smsd.";
		setValue("smsd.msg_seqno", comr.getSeqno("ECS_MODSEQ"));
//		setValue("smsd.msg_pgm", javaProgram);
		setValue("smsd.msg_pgm" , getValue("msid.msg_pgm",smsNum)); 
		setValue("smsd.msg_dept" , getValue("msid.msg_dept",smsNum)); 
		setValue("smsd.msg_userid" , getValue("msid.msg_userid",smsNum)); 
		setValue("smsd.msg_id"     , getValue("msid.msg_id",smsNum)); 
		setValue("smsd.cellar_phone", getValue("idno.cellar_phone"));
		setValue("smsd.id_no", getValue("idno.id_no"));
		setValue("smsd.chi_name", getValue("idno.chi_name"));
		setValue("smsd.p_seqno", getValue("p_seqno"));
		setValue("smsd.acct_type", getValue("acct_type"));
		setValue("smsd.id_p_seqno", getValue("id_p_seqno"));
		setValue("smsd.card_no", getValue("card_no"));
		setValue("smsd.add_mode", "B");
		setValue("smsd.crt_date", sysDate);
		setValue("smsd.crt_user", "ecs");
		setValue("smsd.apr_date", sysDate);
		setValue("smsd.apr_user", "ecs");
		setValue("smsd.apr_flag", "Y");
		setValue("smsd.mod_user", "ecs");
		setValue("smsd.mod_time", sysDate + sysTime);
		setValue("smsd.mod_pgm", javaProgram);

		daoTable = "SMS_MSG_DTL";

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", "insert_sms_msg_dtl  error[dupRecord]");
			exitProgram(1);
		}		
				
		return;
	}
// ************************************************************************

	/************************************************************************/
	/**
	 * DETAIL-DATA
	 * 
	 * @return String
	 * @throws Exception
	 */
	private String getRowOfDetail00() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("00", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(getValue("idno.id_no"), 11));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("02", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(getValue("idno.e_mail_addr"), 30)); 
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(String.format("合作金庫商業銀行%s年%s－%s月份期別電子發票「中獎通知」",
				comStr.left(getValue("win_stage"), 3),
				comStr.mid(getValue("win_stage") , 3,2),
				comStr.mid(getValue("win_stage"), 5,2)), 88));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("親愛的合庫卡友您好！", 28));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(" ", 193));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/*****************************************************************************/
	private String getRowOfDetail01() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("01", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(String.format(
				"合作金庫商業銀行轉知財政部電子發票整合服務平台通知，您於民國%s年%s-%s月期別索取的無實體電子發票，經本平台自動對獎結果為「中獎」。<BR>中獎信用卡卡號末４碼%s，您若需查詢中獎金額、中獎發票號碼等資訊，請自行至<a href=\"https://www.einvoice.nat.gov.tw/\">財政部電子發票整合服務平台</a> 或7-11、全家、萊爾富及ＯＫ等便利商店的ＫＩＯＳＫ查詢。",
				comStr.left(getValue("win_stage"), 3),
				comStr.mid(getValue("win_stage") , 3,2),
				comStr.mid(getValue("win_stage"), 5,2),
				getValue("card_no")), 342));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	/************************************************************************/
	/**
	 * DETAIL-DATA
	 * 
	 * @return String
	 * @throws Exception
	 */
	private String getRowOfDetail200() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("00", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(getValue("idno.id_no"), 11));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("02", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(getValue("idno.e_mail_addr"), 30)); 
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(String.format("合作金庫商業銀行%s年%s－%s月份電子發票「尚未列印中獎發票證明聯」通知",
				comStr.left(getValue("win_stage"), 3),
				comStr.mid(getValue("win_stage") , 3,2),
				comStr.mid(getValue("win_stage"), 5,2)), 88));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("親愛的合庫卡友您好！", 28));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(" ", 193));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/*****************************************************************************/
	private String getRowOfDetail201() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("01", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(String.format(
				"合作金庫商業銀行轉知財政部電子發票整合服務平台通知，您於民國%s年%s-%s月期期電子發票，「尚未列印中獎電子發票證明聯」。<BR>中獎信用卡卡號末４碼%s，請即至7-11、全家、萊爾富及ＯＫ等便利商店的ＫＩＯＳＫ列印。",
				comStr.left(getValue("win_stage"), 3),
				comStr.mid(getValue("win_stage") , 3,2),
				comStr.mid(getValue("win_stage"), 5,2),
				getValue("card_no")), 342));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	private int checkOpen(String fileFolder , String datFileName) throws Exception {
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		return 0;
	}
	
	private int generateDatFile() throws Exception{
		if(emailWriteCnt == 0) {
			showLogMessage("I", "", "無EMAIL通知資料可寫入檔案");
			return 1;
		}
		showLogMessage("I", "", String.format("將[%d]筆資料寫入EMAIL通知檔", emailWriteCnt+emailWriteCnt2));
		try {
			byte[] tmpBytes = writeSb.toString().getBytes("MS950");
			writeBinFile(tmpBytes, tmpBytes.length);
			showLogMessage("I", "", String.format("產生檔案完成！，共產生%d筆資料", emailWriteCnt+emailWriteCnt2));
			return 0;
		}catch (Exception ex) {
			return 1;
		}finally {
			closeBinaryOutput();
		}
	}
	
	/*****************************************************************************/

	void procFTP(String hdrFileName, String fileFolder) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = CRDATACREA; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("mput %s", hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(CRDATACREA, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void moveFile(String datFileName1, String fileFolder1) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1, "/backup", datFileName1).toString();

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

} // End of class FetchSample
