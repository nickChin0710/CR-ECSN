/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  110/10/28  V1.00.00    Justin      program initial                         *
 *  110/10/28  V1.00.01    Justin      crtSource = A                           *
 ******************************************************************************/

package Icu;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;
import com.CommFTP;

public class IcuD022 extends AccessDAO {
	private static final String FTP_REF_IP_CODE = "NCR2TCB";
	private final String prognmae = "產生送CARDLINK電子票證聯名卡自動加值功能開啟通知資料檔程式 110/10/28 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();

	final static String prgmId = "IcuD022";
	
	private final static String FILE_NAME = "F00600000.ICECOQND";
	private final static String OUTPUT_PATH = "/media/icu/out/";
	private final static String BACKUP_PATH = "/media/icu/backup/";
	int rowCnt = 0;

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + prognmae);
			// =====================================

			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			
			String searchDate = "";		
			if (args.length == 0) {
				searchDate = comm.lastDate(sysDate);
			}else if (args.length == 1) {
				searchDate = args[0];
			}else {
				comc.errExit("Usage : " + prgmId, "不須給定參數 或 可給定指定date[yyyymmdd]");
			}
			
			if (searchDate.length() != 8) {
				comc.errExit("Usage : " + prgmId, String.format("date[%d]超過8碼", searchDate));
			}
			
			showLogMessage("I", "", String.format("查詢[%s]日期的CcaAuthTxlog", searchDate));
			
			List<IcuD022Data> list = selectCcaAuthTxlog(searchDate);
			
			if (list.size() > 0 ) {
				String outFilePath = getOutFilePath();
				String outFileName = Paths.get(outFilePath).getFileName().toString();
				produceFile(outFilePath, list);
				insertFileCtl(outFileName);
			    procFTP(outFileName);
			    renameFile1(outFileName);
			}
			

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束,[" + rowCnt + "]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	/**********************************************************************/

	private void produceFile(String outFilePath, List<IcuD022Data> list) throws Exception {
		rowCnt = 0;
		outFilePath = Paths.get(outFilePath).toString();
		int outFileIndex = openBinaryOutput2(outFilePath);
		for (int i = 0; i < list.size(); i++) {
			IcuD022Data data = list.get(i);
			writeBinFile2(outFileIndex, data.allText().getBytes("ms950"), IcuD022Data.totalBytes);
			rowCnt++;
		}
		closeBinaryOutput2(outFileIndex);
	}

	/**********************************************************************/
	List<IcuD022Data> selectCcaAuthTxlog(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT cat.CARD_NO, ");
		sb.append("       cat.V_CARD_NO, ");
		sb.append("       cat.TX_DATE, ");
		sb.append("       cat.TX_TIME, ");
		sb.append("       cat.CRT_USER ");
		sb.append("FROM CCA_AUTH_TXLOG cat ");
		sb.append("WHERE 1=1 ");
		sb.append("AND cat.TX_DATE = ? ");
		sb.append("AND cat.TRANS_TYPE ='0100'  ");
		sb.append("AND cat.TRANS_CODE ='TN'  ");
		sb.append("AND cat.ISO_RESP_CODE ='00'  ");
		sb.append("AND cat.REVERSAL_FLAG = 'N'  ");
		sb.append("ORDER BY cat.TX_DATE, cat.TX_TIME ");

		sqlCmd  = sb.toString();
		setString(1, searchDate);
		int recordCnt = selectTable();
		
		List<IcuD022Data> list = new ArrayList<IcuD022Data>();
		
		for (int i = 0; i < recordCnt; i++) {
			IcuD022Data data = new IcuD022Data();
			data.cardNo = getValue("CARD_NO", i);
			data.tscCardNo = getValue("V_CARD_NO", i);
			data.txDate = getValue("TX_DATE", i);
			data.txTime = getValue("TX_TIME", i);
			data.crtUser = getValue("CRT_USER", i);
			list.add(data);
		}

		
		return list;
	}

	/***********************************************************************/
	String getOutFilePath() throws Exception {

		String chiDate = new CommDate().toTwDate(sysDate).substring(1);
		
		sqlCmd = "select max(file_name) as maxFileName";
		sqlCmd += " from crd_file_ctl  ";
		sqlCmd += " where file_name like ?";
		sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
		
		setString(1, FILE_NAME + "." + chiDate + "%");
		selectTable();
		
		int nn = 1;
		if (getValue("maxFileName").length() == 0) {
			nn = 1;
		} else {
			nn = Integer.parseInt(getValue("maxFileName").substring(25, 27)) + 1;
		}
		
		String outFileName = String.format("%s.%s%02d", FILE_NAME, chiDate, nn);
		showLogMessage("I", "", "Output Filename = [" + outFileName + "]");

		String outFilePath = String.format("%s%s%s", comc.getECSHOME(), OUTPUT_PATH, outFileName);
		outFilePath = Normalizer.normalize(outFilePath, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFilePath + "]");

		return outFilePath;
	}


	/***********************************************************************/
	void insertFileCtl(String filename) throws Exception {
		setValue("file_name", filename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", rowCnt);
		setValueInt("record_cnt", rowCnt);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, rowCnt);
			setInt(2, rowCnt);
			setString(3, filename);
			updateTable();
			if (notFound.equals("Y")) {
				new CommCrdRoutine(getDBconnect(), getDBalias()).errRtn("update_crd_file_ctl not found!", "", "");
			}
		}
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD022 proc = new IcuD022();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class IcuD022Data {
		String cardNo; // 信用卡卡片號碼
		String tscCardNo; // 電子票證聯名卡外顯卡片號碼
		String txDate; // 開啟日期
		String txTime; // 開啟時間
		final static String crtSource="A"; // 開啟來源(固定放A)
		String crtUser; // 開啟人員
		final static String respCode = "00"; // 回應碼
		final static String filler = ""; //保留欄位
		final static String nextLine = "\r\n"; // 換行符號(0D0A)
		final static int totalBytes = 200;

		String allText() throws UnsupportedEncodingException {
			StringBuffer sb = new StringBuffer();
			sb.append(comc.fixLeft(cardNo, 19));
			sb.append(comc.fixLeft(tscCardNo, 19));
			sb.append(comc.fixLeft(txDate, 8));
			sb.append(comc.fixLeft(txTime, 6));
			sb.append(comc.fixLeft(crtSource, 1));
			sb.append(comc.fixLeft(crtUser, 20));
			sb.append(comc.fixLeft(respCode, 2));
			sb.append(comc.fixLeft(filler, 123));
			sb.append(comc.fixLeft(nextLine, 2));
			return sb.toString();
		}

	}



	/***********************************************************************/
	void procFTP(String fileName) throws Exception {
		  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		  CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
		
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = FTP_REF_IP_CODE; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/out", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
	      int errCode = commFTP.ftplogName(FTP_REF_IP_CODE, "mput " + fileName);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料"+" errcode:"+errCode);
	          insertEcsNotifyLog(fileName, commFTP.hEflgTransSeqno, comr.getObjectOwner("3", javaProgram));          
	      }
	  }
	
	/****************************************************************************/
	  public int insertEcsNotifyLog(String fileName, String transSeqno, String objOwner) throws Exception {
	      setValue("crt_date", sysDate);
	      setValue("crt_time", sysTime);
	      setValue("unit_code", objOwner);
	      setValue("obj_type", "3");
	      setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_name", "媒體檔名:" + fileName);
	      setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_desc2", "");
	      setValue("trans_seqno", transSeqno);
	      setValue("mod_time", sysDate + sysTime);
	      setValue("mod_pgm", javaProgram);
	      daoTable = "ecs_notify_log";

	      insertTable();

	      return (0);
	  }

	  /****************************************************************************/
		void renameFile1(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + OUTPUT_PATH + removeFileName;
			String tmpstr2 = comc.getECSHOME() + BACKUP_PATH + removeFileName + "." + sysDate;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
}
