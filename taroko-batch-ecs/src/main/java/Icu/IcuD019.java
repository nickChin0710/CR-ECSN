/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/06/29  V1.00.00    Pino      program initial                           *
 *  109/07/03  V1.00.01  Wilson      FBBBBBBBB -> F00600000                    *
 *  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *                                                      *
 *   109/07/03  V1.01.02    JustinWu                  change the program name*
 *  109-07-22    yanghan       修改了字段名称            *
 *  109/08/14  V1.01.06   Wilson     資料夾名稱修改為小寫                                                                             *
 *  109/09/04  V1.01.07   Wilson     換行符號 -> "\r\n"(0D0A)                     *
 *  109/09/14  V1.01.08   Wilson     新增procFTP                                *
 *  109/09/15  V1.01.09   Wilson     chiDate調整                                                                                        *
 *  109/09/26  v1.01.10   Wilson     新增電子發票載具                                                                                     *
 *  109/10/14  V1.01.11   Wilson     LOCAL_FTP_PUT -> NCR2TCB                  *
 *  109-10-19  V1.00.12   shiyuqi       updated for project coding standard     *
 *  110-10-27  V1.00.13   Justin     fix the bug about selecting the max fileName *
 *  111/02/14  V1.01.14    Ryan      big5 to MS950                                           *
 ******************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommFTP;

public class IcuD019 extends AccessDAO {
	private final String prognmae = "產生送CARDLINK信用卡開卡資料檔程式 111/02/14 V1.00.14";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;

	int debug = 0;

	String prgmId = "IcuD019";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;

	int rptSeq1 = 0;
	String buf = "";
	String hModUser = "";
	String hOpenDate = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	String tmpCardNo = "";
	String tmpNewEndDate = "";
	String tmpOpenDate = "";
	String tmpOpenTime = "";
	String tmpType = "";
	String tmpCardCode = "";
	int nn = 0;
	int hRecCnt = 0;

	String getFileName;
	String outFileName;

	Buf1 ncccData1 = new Buf1();

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + prognmae);
			// =====================================
			if (args.length > 1) {
				comc.errExit("Usage : " + prgmId, "open_date[yyyymmdd]");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());
			if (args.length == 0) {
				hOpenDate = comm.lastDate(sysDate);
			} else if (args.length == 1) {
				hOpenDate = args[0];
			}
			if (hOpenDate.length() != 8) {
				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
			}
			hModUser = comc.commGetUserID();
			selectCcaCardOpen();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束,[" + hRecCnt + "]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/**********************************************************************/
	int selectCcaCardOpen() throws Exception {
		sqlCmd  = "SELECT '1' as tmp_type, ";
		sqlCmd += "card_no, ";
		sqlCmd += "new_end_date, ";
		sqlCmd += "open_date, ";
		sqlCmd += "open_time, ";
		sqlCmd += "'0' as card_code ";
		sqlCmd += "FROM cca_card_open ";
		sqlCmd += "WHERE open_date  = ? ";
		sqlCmd += "UNION ";
		sqlCmd += "SELECT '2' as tmp_type, ";
		sqlCmd += "a.card_no, ";
		sqlCmd += "b.new_end_date, ";
		sqlCmd += "a.tx_date as open_date, ";
		sqlCmd += "a.tx_time as open_time, ";
		sqlCmd += "decode(a.trans_code,'EA','A','B') as card_code ";
		sqlCmd += "FROM cca_auth_txlog a,crd_card b ";
		sqlCmd += "WHERE a.card_no = b.card_no ";
		sqlCmd += "AND a.trans_code in ('EA','EC') ";
		sqlCmd += "AND a.iso_resp_code  = '00' ";
		sqlCmd += "AND a.tx_date  = ? ";
		sqlCmd += "ORDER BY open_date,open_time ";
		setString(1, hOpenDate);
		setString(2, hOpenDate);
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			tmpType = getValue("tmp_type", i);
			tmpCardNo = getValue("card_no", i);
			tmpNewEndDate = getValue("new_end_date", i);
			tmpOpenDate = getValue("open_date", i);
			tmpOpenTime = getValue("open_time", i);
			tmpCardCode = getValue("card_code", i);
			createFile();
		}

		outPutTextFile();
		comc.writeReport(outFileName, lpar1, "MS950");
		insertFileCtl(hNcccFilename);
		lpar1.clear();

		return 0;
	}

	/***********************************************************************/
	int outPutTextFile() throws Exception {

		String chiDate = String.format("%3d", Integer.valueOf(sysDate.substring(0, 4)) - 1911) + sysDate.substring(4, 6)
				+ sysDate.substring(6, 8);
		
		chiDate = chiDate.substring(1);
		
		sqlCmd = "select max(file_name) as maxFileName";
		sqlCmd += " from crd_file_ctl  ";
		sqlCmd += " where file_name like ?";
		sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
		setString(1, "F00600000.ICCRSQND." + chiDate + "%");
		int recordCnt = selectTable();
		if (getValue("maxFileName").length() == 0) {
			nn = 1;
		} else {
			nn = Integer.parseInt(getValue("maxFileName").substring(25, 27)) + 1;
		}
		
		hNcccFilename = String.format("F00600000.ICCRSQND.%s%02d", chiDate, nn);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%s/media/icu/out/%s", comc.getECSHOME(), hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	/***********************************************************************/
	void createFile() throws Exception {
		ncccData1 = new Buf1();
		
		if(tmpType.equals("1"))
		{
			ncccData1.type = "1";
			ncccData1.modType = "1";
		}
		else 
		{
			ncccData1.type = "4";
			ncccData1.modType = "";
		}
				
		ncccData1.cardNo = tmpCardNo;
		ncccData1.validDate = tmpNewEndDate.substring(2, 6);
		ncccData1.currentCode = tmpCardCode;
		ncccData1.actionCode = "";
		ncccData1.modDate = tmpOpenDate;
		ncccData1.modTime = tmpOpenTime;
		ncccData1.endDate = "";
		ncccData1.modCode = "2";
		ncccData1.fileDestination = "";
		ncccData1.fileType = "";
		ncccData1.blacklistArea = "";
		
		
		
		ncccData1.modUser = "";
		ncccData1.respCode = "00";

		buf = ncccData1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		hRecCnt++;
		return;
	}

	/***********************************************************************/
	void insertFileCtl(String filename) throws Exception {
		setValue("file_name", filename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", hRecCnt);
		setValueInt("record_cnt", hRecCnt);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, hRecCnt);
			setInt(2, hRecCnt);
			setString(3, filename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD019 proc = new IcuD019();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String type; // 狀態類別
		String cardNo; // 卡號
		String validDate; // 有效期限
		String currentCode; // 卡片狀態
		String actionCode; // 國際檔
		String modDate; // 異動日期(掛檔為回應日期)
		String modTime; // 異動時間(掛檔為回應時間)
		String endDate; // 結束日期
		String modCode; // 異動碼
		String fileDestination; // 掛檔目的地
		String fileType; // 掛檔型態
		String blacklistArea; // 黑名單地區別
		String modType; // 異動方式
		String modUser; // 異動人員
		String respCode; // 回應碼

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			String newLine = "\r";
			rtn += fixLeft(type, 1);
			rtn += fixLeft(cardNo, 16);
			rtn += fixLeft(validDate, 4);
			rtn += fixLeft(currentCode, 1);
			rtn += fixLeft(actionCode, 2);
			rtn += fixLeft(modDate, 8);
			rtn += fixLeft(modTime, 6);
			rtn += fixLeft(endDate, 8);
			rtn += fixLeft(modCode, 1);
			rtn += fixLeft(fileDestination, 1);
			rtn += fixLeft(fileType, 1);
			rtn += fixLeft(blacklistArea, 9);
			rtn += fixLeft(modType, 1);
			rtn += fixLeft(modUser, 20);
			rtn += fixLeft(respCode, 2);
			rtn += newLine;
			return rtn;
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
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData1.type = comc.subMS950String(bytes, 0, 1);
		ncccData1.cardNo = comc.subMS950String(bytes, 1, 17);
		ncccData1.validDate = comc.subMS950String(bytes, 17, 21);
		ncccData1.currentCode = comc.subMS950String(bytes, 21, 22);
		ncccData1.actionCode = comc.subMS950String(bytes, 22, 24);
		ncccData1.modDate = comc.subMS950String(bytes, 24, 32);
		ncccData1.modTime = comc.subMS950String(bytes, 32, 38);
		ncccData1.endDate = comc.subMS950String(bytes, 38, 46);
		ncccData1.modCode = comc.subMS950String(bytes, 46, 47);
		ncccData1.fileDestination = comc.subMS950String(bytes, 47, 48);
		ncccData1.fileType = comc.subMS950String(bytes, 48, 49);
		ncccData1.blacklistArea = comc.subMS950String(bytes, 49, 58);
		ncccData1.modType = comc.subMS950String(bytes, 58, 59);
		ncccData1.modUser = comc.subMS950String(bytes, 59, 79);
		ncccData1.respCode = comc.subMS950String(bytes, 79, 81);
	}

	/****************************************************************************/
	String fixAllLeft(String str, int len) throws UnsupportedEncodingException {
		String spc = "";
		for (int i = 0; i < 100; i++)
			spc += "　";
		if (str == null)
			str = "";
		str = str + spc;
		byte[] bytes = str.getBytes("MS950");
		byte[] vResult = new byte[len];
		System.arraycopy(bytes, 0, vResult, 0, len);
		return new String(vResult, "MS950");
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/out", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + hNcccFilename + " 開始傳送....");
	      int errCode = commFTP.ftplogName("NCR2TCB", "mput " + hNcccFilename);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + hNcccFilename + " 資料"+" errcode:"+errCode);
	          insertEcsNotifyLog(hNcccFilename);          
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

	  /****************************************************************************/
		void renameFile1(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/icu/out/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
}
