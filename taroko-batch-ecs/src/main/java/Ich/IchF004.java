/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/01/02  V1.01.00  Brian       program initial                           *
*  109/11/29  V1.01.02  yanghan       修改了變量名稱和方法名稱                                                                                       *
******************************************************************************/

package Ich;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchF004 extends AccessDAO {
	private String progname = "遞送特殊處理卡號(B04B)產生  109/11/29 V1.01.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
	String rptId = "";
	String rptName = "";
	int rptSeq = 0;

	int debug = 1;
	String hTempUser = "";
	String hTnlgNotifyDate = "";
	String hBusiBusinessDate = "";
	String hIcdrMediaCreateDate = "";
	String hIcdrMediaCreateTime = "";
	String hIcdrRowid = "";
	String hTnlgFileName = "";
	String hTnlgMediaCreateDate = "";
	String hTnlgFtpSendDate = "";
	String tmpstr1 = "";
	String hTfinFileIden = "B04B";

	String hIcdrIchCardNo = "";
	String hIcdrProcType = "";

	int forceFlag = 0;
	int totCnt = 0;
	int hTnlgRecordCnt = 0;
	String hHash = "";
	String allData = "";

	buf1 detailSt = new buf1();
	String out = "";

//************************************************************************************
	public int mainProcess(String[] args) {
		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0 && args.length != 1 && args.length != 2) {
				comc.errExit("Usage : IchF004 [notify_date] [force_flag (Y/N)]", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

			String checkHome = comc.getECSHOME();
			if (comcr.hCallBatchSeqno.length() > 6) {
				if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
					comcr.hCallBatchSeqno = "no-call";
				}
			}

			comcr.hCallRProgramCode = this.getClass().getName();
			String hTempUser = "";
			if (comcr.hCallBatchSeqno.length() == 20) {
				comcr.callbatch(0, 0, 1);
				selectSQL = " user_id ";
				daoTable = "ptr_callbatch";
				whereStr = "WHERE batch_seqno   = ?  ";

				setString(1, comcr.hCallBatchSeqno);
				int recCnt = selectTable();
				hTempUser = getValue("user_id");
			}
			if (hTempUser.length() == 0) {
				hTempUser = comc.commGetUserID();
			}

			hTnlgNotifyDate = "";
			forceFlag = 0;
			if (args.length == 1) {
				if ((args[0].length() == 1) && (args[0].equals("Y")))
					forceFlag = 1;
				if (args[0].length() == 8)
					hTnlgNotifyDate = args[0];
			}
			if (args.length == 2) {
				hTnlgNotifyDate = args[0];
				if (args[1].equals("Y"))
					forceFlag = 1;
			}
			selectPtrBusinday();

			tmpstr1 = String.format("BRQA_%3.3s_%8.8s_%4.4s", comc.ICH_BANK_ID3, hTnlgNotifyDate, hTfinFileIden);
			showLogMessage("I", "", "Process date=[" + forceFlag + "]" + hTnlgNotifyDate + "," + tmpstr1);

			hTnlgFileName = tmpstr1;

			if (forceFlag == 0) {
				if (selectIchNotifyLoga() != 0) {
					String errMsg = String.format("select_ich_notify_log_a error !");
					comcr.errRtn(errMsg, "", comcr.hCallBatchSeqno);
				}
			} else {
				updateIchRefuseLog0();
			}

			fileOpen();

			selectIchRefuseLog();
			selectIchB04bSpecial();

			if (totCnt > 0) {
				hHash = comc.encryptSHA(allData, "SHA-1", "big5");
				tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%-40.40s\r\n", hTfinFileIden, "01", "0001",
						comc.ICH_BANK_ID3, String.format("%08d", totCnt) + "B", hHash);

				lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
			}

			hTnlgRecordCnt = totCnt;

			fileClose();

			comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
			showLogMessage("I", "", comcr.hCallErrorDesc);

			if (comcr.hCallBatchSeqno.length() == 20)
				comcr.callbatch(1, 0, 1); // 1: 結束

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
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_icdr_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_icdr_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate     = getValue("business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hIcdrMediaCreateDate = getValue("h_icdr_media_create_date");
            hIcdrMediaCreateTime = getValue("h_icdr_media_create_time");
        }

    }
	/***********************************************************************/
	int selectIchNotifyLoga() throws Exception {
		hTnlgMediaCreateDate = "";
		hTnlgFtpSendDate = "";

		sqlCmd = "select media_create_date,";
		sqlCmd += " ftp_send_date ";
		sqlCmd += "  from ich_notify_log  ";
		sqlCmd += " where file_name = ? ";
		setString(1, hTnlgFileName);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			return 0;
		}
		if (recordCnt > 0) {
			hTnlgMediaCreateDate = getValue("media_create_date");
			hTnlgFtpSendDate = getValue("ftp_send_date");
		}

		if (hTnlgFtpSendDate.length() != 0) {
			String stderr = String.format("通知檔 [%s] 已FTP至ICH , 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
			showLogMessage("I", "", stderr);
			return (1);
		}
		if (hTnlgMediaCreateDate.length() != 0) {
			String stderr = String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
			showLogMessage("I", "", stderr);
			return (1);
		}
		return (0);
	}

	/***********************************************************************/
	void updateIchRefuseLog0() throws Exception {
		daoTable = "ich_refuse_log";
		updateSQL = "send_date       = ''";
		whereStr = "where mod_pgm = ? ";
		setString(1, hTnlgFileName);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_ich_refuse_log not found!", hTnlgFileName, comcr.hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void fileOpen() throws Exception {
		String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		out = temstr1;

		selectSQL = " count(*)   as all_cnt ";
		daoTable = " ich_refuse_log a ";
		whereStr = " where 1=1               ";
		whereStr += "   and a.send_date = '' ";

		int recCnt = selectTable();

		hHash = "0000000000000000000000000000000000000000";
		tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%-40.40s\r\n", hTfinFileIden, "01", "0001",
				comc.ICH_BANK_ID3, "00000000B", hHash);

		lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));
	}

	/***********************************************************************/
	void fileClose() throws Exception {
		comc.writeReport(out, lpar, "big5", false);
	}

	/***********************************************************************/
	void selectIchB04bSpecial() throws Exception {
		sqlCmd = "select ";
		sqlCmd += "a.ich_card_no,";
		sqlCmd += "a.proc_type,";
		sqlCmd += "a.rowid as rowid1 ";
		sqlCmd += "from  ich_b04b_special a ";
		sqlCmd += "where (decode(a.proc_flag,'','N',a.proc_flag) = 'N' or a.proc_flag = '')  ";
		sqlCmd += "  and (decode(a.effc_send_date,'','N',a.effc_send_date) = 'N' or a.effc_send_date <= to_char(sysdate,'yyyymmdd'))  ";
		openCursor();
		while (fetchTable()) {
			hIcdrIchCardNo = getValue("ich_card_no");
			hIcdrProcType = getValue("proc_type");
			hIcdrRowid = getValue("rowid1");

			totCnt++;
			if (totCnt % 1000 == 0 || totCnt == 1)
				showLogMessage("I", "", String.format("Process 22 records =[%d]\n", totCnt));

			writeRtn();
			updateIchB04bSpecial();
			updateIchCard();

		}
		closeCursor();
	}

	/***********************************************************************/
	void selectIchRefuseLog() throws Exception {
		sqlCmd = "select ";
		sqlCmd += "a.ich_card_no,";
		sqlCmd += "a.refuse_type,";
		sqlCmd += "a.rowid as rowid1 ";
		sqlCmd += "from  ich_refuse_log a ";
		sqlCmd += "where  (a.send_date = ''  or a.send_date = '')  ";
		openCursor();
		while (fetchTable()) {
			hIcdrIchCardNo = getValue("ich_card_no");
			hIcdrProcType = getValue("refuse_type");
			hIcdrRowid = getValue("rowid1");

			totCnt++;
			if (totCnt % 1000 == 0 || totCnt == 1)
				showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

			writeRtn();
			updateIchRefuseLog();
			insertIchB04bSpecial();
			updateIchCard();

		}
		closeCursor();
	}

	/***********************************************************************/
	void updateIchCard() throws Exception {
		daoTable = "ich_card";
		updateSQL = " refuse_type = ? ";
		if (hIcdrProcType.equals("R"))
			updateSQL += " ,REFUSE_SEND_DATE = to_char(sysdate, 'yyyymmdd')  ";
		else if (hIcdrProcType.equals("Q"))
			updateSQL += " ,REFUSE_CANCEL_DATE = to_char(sysdate, 'yyyymmdd')  ";
		whereStr = " where ich_card_no = ? ";
		setString(1, hIcdrProcType);
		setString(2, hIcdrIchCardNo);
		updateTable();
	}

	/***********************************************************************/
	void writeRtn() throws Exception {

		String tmpstr = "";

		detailSt = new buf1();

		detailSt.type = "D";

		tmpstr = String.format("%-16.16s", hIcdrIchCardNo);
		detailSt.ichCardNo = tmpstr;

		/* 5：聯名卡停卡 R：拒絕代行授權 Q：取消拒絕代行授權 */
		tmpstr = String.format("%-1.1s", hIcdrProcType);
		detailSt.procType = tmpstr;

		detailSt.space = String.format("%-31.31s", " ");

		tmpstr = String.format("%-14.14s", sysDate + sysTime);
		detailSt.sysDatetime = tmpstr;

		detailSt.fillerEnd = "\r\n";

		String buf = detailSt.allText();
		lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		allData += buf;

		if (debug == 1)
			showLogMessage("I", "",
					"  Write =[" + buf + "]" + buf.getBytes("big5").length + "," + allData.getBytes("big5").length);

		return;
	}

	/***********************************************************************/
	void updateIchB04bSpecial() throws Exception {
		daoTable = "ich_b04b_special";
		updateSQL += " proc_flag         = 'Y',";
		updateSQL += " mod_pgm           = ?,";
		updateSQL += " mod_time          = sysdate";
		whereStr = "where rowid        = ? ";
		setString(1, javaProgram);
		setRowId(2, hIcdrRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_ich_b04b_special not found!", "", comcr.hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void updateIchRefuseLog() throws Exception {
		daoTable = "ich_refuse_log";
		updateSQL = " send_date         = to_char(sysdate,'yyyymmdd') , ";
		// updateSQL += " proc_flag = 'Y',";
		updateSQL += " mod_pgm           = ?,";
		updateSQL += " mod_time          = sysdate";
		whereStr = "where rowid        = ? ";
		setString(1, javaProgram);
		setRowId(2, hIcdrRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_ich_refuse_log not found!", "", comcr.hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertIchB04bSpecial() throws Exception {
		setValue("ich_card_no", hIcdrIchCardNo);
		setValue("proc_type", hIcdrProcType);
		setValue("file_name", hTnlgFileName);
		setValue("sys_date", sysDate);
		setValue("sys_time", sysTime);
		setValue("proc_flag", "Y"); // N -> Y 已送
		setValue("ok_flag", "N");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", "IchF004");

		daoTable = "ich_b04b_special";

		insertTable();
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IchF004 proc = new IchF004();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class buf1 {
		String type;
		String ichCardNo;
		String procType;
		String space;
		String sysDatetime;
		String fillerEnd;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(type, 1);
			rtn += fixLeft(ichCardNo, 16);
			rtn += fixLeft(procType, 1);
			rtn += fixLeft(space, 31);
			rtn += fixLeft(sysDatetime, 14);
			rtn += fixLeft(fillerEnd, 2);
			return rtn;
		}

		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			int size = (Math.floorDiv(len, 100) + 1) * 100;
			String spc = "";
			for (int i = 0; i < size; i++)
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

}
