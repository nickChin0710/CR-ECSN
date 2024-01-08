/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/06/30  V1.00.00    Pino      program initial                           *
*  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *     
*  109-07-22    yanghan       修改了字段名称            *                                                   *
*  109/08/26  V1.01.04    Wilson    mark delete tsc_orgdata_log               *
*  109-10-19  V1.00.05    shiyuqi       updated for project coding standard     *
*  109/10/30  V1.00.06    Wilson    檔名日期改營業日                                                                                      *
*  111/02/14  V1.00.07    Ryan      big5 to MS950                                           *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊VD卡鎖卡資料檔(DCLK)媒體接收處理程式*/
public class TscD034 extends AccessDAO {

	private final String progname = "悠遊VD卡鎖卡資料檔(DCLK)媒體接收處理程式  111/02/14 V1.00.07";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommIps comips = new CommIps();

	String hCallBatchSeqno = "";
	String hTempNotifyDate = "";
	String hBusiBusinessDate = "";
	String hTempNotifyTime = "";
	String hTnlgPerformFlag = "";
	String hTnlgNotifyDate = "";
	String hTnlgCheckCode = "";
	String hTnlgProcFlag = "";
	String hTnlgRowid = "";
	String hTnlgFileName = "";
	String hTardRowid = "";
	String hLkecTscCardNo = "";
	String hLkecLockDate = "";
	String hLkecLockTime = "";
	String hOrgdTsccDataSeqno = "";
	String hLkecTrafficCd = "";
	String hLkecAddrCd = "";
	String hLkecTrafficAbbr = "";
	String hLkecAddrAbbr = "";
	String hOrgdOrgData = "";
	String hOrgdRptRespCode = "";

	int hTnlgRecordCnt = 0;
	int forceFlag = 0;
	int totalCnt = 0;
	int succCnt = 0;
	int rptCnt = 0;
	String tmpstr = "";
	String tmpstr1 = "";
	String tmpstr2 = "";
	String fileSeq = "";
	String temstr1 = "";
	String hSign = "";
	String hSignS = "";

	Buf1 dtl = new Buf1();

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (comm.isAppActive(javaProgram)) {
				comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
			}
//			if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
//				comc.errExit("Usage : TscD034 [[notify_date][force_flag]] [force_flag]", "");
//			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			// h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
			// "";
			// comcr.callbatch(0, 0, 0);
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			hTempNotifyDate = "";
			fileSeq = "01";
			forceFlag = 0;
			
			selectPtrBusinday();
			
			if (args.length == 0) {
		        hTempNotifyDate = hBusiBusinessDate;
		    }
			else if (args.length == 1) {
				if ((args[0].length() == 1) && (args[0].equals("Y")))
					forceFlag = 1;
				if (args[0].length() == 8) {
					String sGArgs0 = "";
					sGArgs0 = args[0];
					sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
					hTempNotifyDate = sGArgs0;
				}
				if (args[0].length() == 2) {
					showLogMessage("I", "", String.format("參數(一) 不可兩碼"));
				}
			}
			else if (args.length == 2) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hTempNotifyDate = sGArgs0;
				if ((args[1].length() == 1) && (args[1].equals("Y")))
					forceFlag = 1;
				if (args[1].length() == 2) {
					String sGArgs1 = "";
					sGArgs1 = args[1];
					sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
					fileSeq = sGArgs1;
				}
				if (args[1].length() != 1 && args[1].length() != 2) {
					showLogMessage("I", "", String.format("參數(二) 為[force_flag] or [seq(nn)] "));
				}
			}
			else if (args.length == 3) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hTempNotifyDate = sGArgs0;
				if (args[1].equals("Y"))
					forceFlag = 1;
				if (args[2].length() != 2) {
					showLogMessage("I", "", String.format("file seq 必須兩碼"));
				}
				String sGArgs2 = "";
				sGArgs2 = args[2];
				sGArgs2 = Normalizer.normalize(sGArgs2, java.text.Normalizer.Form.NFKD);
				fileSeq = sGArgs2;
			}
			else {
				comc.errExit("Usage : TscD034 [[notify_date][force_flag]] [force_flag]", "");
			}
			
			tmpstr1 = String.format("DCLK.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
			hTnlgFileName = tmpstr1;
			showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

			/*
			 * if(force_flag==0) { if(select_tsc_notify_log_a()!=0) {
			 * update_tsc_notify_log_a(); backup_rtn(); db_commit(); return(0); } }
			 */
			fileOpen();
//第二階段上線須解除mark
//            update_tsc_notify_log_a();

//			deleteTscOrgdataLog();

			backupRtn();

			showLogMessage("I", "",
					String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
			// ==============================================
			// 固定要做的
			// comcr.callbatch(1, 0, 0);
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		sqlCmd = "select business_date,";
		sqlCmd += " decode( cast(? as varchar(8))" + ", ''"
				+ ", to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13')" + ", 1" + ", sysdate"
				+ ", sysdate - 1 days)" + ", 'yyyymmdd')" + ", ?) h_temp_notify_date,";
		sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += "fetch first 1 rows only";
		setString(1, hTempNotifyDate);
		setString(2, hTempNotifyDate);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
			hTempNotifyDate = getValue("h_temp_notify_date");
			hTempNotifyTime = getValue("h_temp_notify_time");
		}
	}

	/***********************************************************************/
	void fileOpen() throws Exception {
		String str600 = "";
		tmpstr1 = String.format("%s", hTnlgFileName);
		temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		int f = openInputText(temstr1);
		if (f == -1) {
			comcr.errRtn(String.format("[%s]檔案不存在", temstr1), "", hCallBatchSeqno);
		}

		int br = openInputText(temstr1, "MS950");
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			if (!comc.getSubString(str600, 0, 1).equals("D"))
				continue;

			totalCnt++;
			splitBuf1(str600);
			if ((totalCnt % 3000) == 0)
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

			hOrgdTsccDataSeqno = comcr.getTSCCSeq();

			hOrgdOrgData = str600;
			if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
				hOrgdRptRespCode = "0205";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.tscCardNo));
			if (comc.commTSCCCardNoCheck(tmpstr1) != 0) {
				showLogMessage("I", "",
						String.format("Line[%d] tsc_card_no[%s] tsc_card_no check error", totalCnt, tmpstr1));
				hOrgdRptRespCode = "0201";
				insertTscOrgdataLog();
				continue;
			}
			hLkecTscCardNo = tmpstr1;
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.lockDate));
			if (!comc.commDateCheck(tmpstr1)) {
				hOrgdRptRespCode = "0203";
				insertTscOrgdataLog();
				continue;
			}
			hLkecLockDate = tmpstr1;
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.lockTime));
			if (!comc.commTimeCheck(tmpstr1)) {
				hOrgdRptRespCode = "0204";
				insertTscOrgdataLog();
				continue;
			}
			hLkecLockTime = tmpstr1;
			/*************************************************************************/
			/* 20150303 */
			hLkecTrafficCd = comc.rtrim(dtl.trafficCd);
			hLkecTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
			hLkecAddrCd = comc.rtrim(dtl.addrCd);
			hLkecAddrAbbr = comc.rtrim(dtl.addrAbbr);
			/*************************************************************************/
			/* 原80改120 */
			tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 120);
			tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
			if (!comc.subMS950String(str600.getBytes("MS950"), 120, 120 + 16).equals(tmpstr2)) {
				hOrgdRptRespCode = "0205";
				showLogMessage("I", "", String.format("HASH values error [%s]", hOrgdRptRespCode));
				/*
				 * insert_tsc_orgdata_log(); continue;
				 */
			}
			/*************************************************************************/
			if (selectTscVdCard() != 0) {
				hOrgdRptRespCode = "0301";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			hOrgdRptRespCode = "0000";
			insertTscOrgdataLog();
			insertTscLkecLog();
			updateTscVdCard();
			succCnt++;
		}
		if (br != -1)
			closeInputText(br);
	}

	/***********************************************************************/
	int selectTscVdCard() throws Exception {
		sqlCmd = "select rowid rowid ";
		sqlCmd += " from tsc_vd_card  ";
		sqlCmd += "where tsc_card_no = ? ";
		setString(1, hLkecTscCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTardRowid = getValue("rowid");
		} else
			return (1);
		return (0);
	}

	/***********************************************************************/
	void insertTscOrgdataLog() throws Exception {
		setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
		setValue("file_iden", "DCLK");
		setValue("notify_date", hTempNotifyDate);
		setValue("file_name", hTnlgFileName);
		setValue("org_data", hOrgdOrgData);
		setValue("rpt_resp_code", hOrgdRptRespCode);
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_orgdata_log";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_orgdata_log duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertTscLkecLog() throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
		setValue("tsc_card_no", hLkecTscCardNo);
		setValue("lock_date", hLkecLockDate);
		setValue("lock_time", hLkecLockTime);
		setValue("traffic_cd", hLkecTrafficCd);
		setValue("addr_cd", hLkecAddrCd);
		setValue("traffic_abbr", hLkecTrafficAbbr);
		setValue("addr_abbr", hLkecAddrAbbr);
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_lkec_log";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_lkec_log duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void updateTscVdCard() throws Exception {
		daoTable = "tsc_vd_card";
		updateSQL = " lock_flag      = 'Y',";
		updateSQL += " lock_date      = ?,";
		updateSQL += " lock_time      = ?,";
		updateSQL += " blacklt_e_date = decode(blacklt_flag,";
		updateSQL += " 'Y',";
		updateSQL += " to_char(sysdate, 'YYYYMMDD'),";
		updateSQL += " blacklt_e_date),";
		updateSQL += " mod_pgm        = ?,";
		updateSQL += " mod_time       = sysdate";
		whereStr = "where rowid     = ? ";
		setString(1, hLkecLockDate);
		setString(2, hLkecLockTime);
		setString(3, javaProgram);
		setRowId(4, hTardRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void backupRtn() throws Exception {
		tmpstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
		tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
		tmpstr2 = String.format("%s/media/tsc/backup/%s", comc.getECSHOME(), hTnlgFileName);
		comc.fileRename(temstr1, tmpstr2);
	}

	/***********************************************************************/
	void updateTscNotifyLogA() throws Exception {
		daoTable = " tsc_notify_log";
		updateSQL = " proc_flag  = '2',";
		updateSQL += " proc_date  = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += " proc_time  = to_char(sysdate, 'hh24miss'),";
		updateSQL += " mod_pgm    = ?,";
		updateSQL += " mod_time   = sysdate";
		whereStr = " where file_name  = ? ";
		setString(1, javaProgram);
		setString(2, hTnlgFileName);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
//	int deleteTscOrgdataLog() throws Exception {

//		daoTable = "tsc_orgdata_log";
//		whereStr = "where file_name  = ? ";
//		setString(1, hTnlgFileName);
//		deleteTable();

//		return (0);
//	}

	/***********************************************************************/

	class Buf1 {
		String type;
		String attri;
		String tscCardNo;
		String lockDate;
		String lockTime;
		String trafficCd; /* 交易業者代碼 */
		String addrCd; /* 交易地點代碼 */
		String trafficAbbr; /* 交易業者簡稱 */
		String addrAbbr; /* 交易地點簡稱 */
		String filler1;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(type, 1);
			rtn += comc.fixLeft(attri, 2);
			rtn += comc.fixLeft(tscCardNo, 20);
			rtn += comc.fixLeft(lockDate, 8);
			rtn += comc.fixLeft(lockTime, 6);
			rtn += comc.fixLeft(trafficCd, 8);
			rtn += comc.fixLeft(addrCd, 10);
			rtn += comc.fixLeft(trafficAbbr, 20);
			rtn += comc.fixLeft(addrAbbr, 20);
			rtn += comc.fixLeft(filler1, 200);
			return rtn;
		}
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		dtl.type = comc.subMS950String(bytes, 0, 1);
		dtl.attri = comc.subMS950String(bytes, 1, 2);
		dtl.tscCardNo = comc.subMS950String(bytes, 3, 20);
		dtl.lockDate = comc.subMS950String(bytes, 23, 8);
		dtl.lockTime = comc.subMS950String(bytes, 31, 6);
		dtl.trafficCd = comc.subMS950String(bytes, 37, 8);
		dtl.addrCd = comc.subMS950String(bytes, 45, 10);
		dtl.trafficAbbr = comc.subMS950String(bytes, 55, 20);
		dtl.addrAbbr = comc.subMS950String(bytes, 75, 20);
		dtl.filler1 = comc.subMS950String(bytes, 95, 200);
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TscD034 proc = new TscD034();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
