/******************************************************************************
*                                                                             *
*                             MODIFICATION LOG                                *
*                                                                             *
*     DATE   Version    AUTHOR                       DESCRIPTION              *
*  --------- --------- ----------- -----------------------------------------  *
*  112/05/15 V1.01.01  lai         program initial                            *
*  112/12/05 V1.01.02  JeffKung    現金回饋補入帳,由行銷自行起帳                                 *
******************************************************************************/
package Bil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*會計起帳(BIL)處理*/
public class BilV004 extends AccessDAO {

	public final boolean DEBUG_MODE = false;

	private String PROGNAME = "統計各種費用的入帳金額,產出會計分錄處理  112/12/05 V1.01.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	final int DEBUG = 0;

	String prgmId = "BilV004";
	String prgmName = "信用卡手續費明細報表";
	String rptName = "信用卡手續費明細報表-信用卡TWD";
	String rptId = "CRD53";
	int rptSeq = 0;
	int pageCnt = 0;
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

	String hModUser = "";
	String hModTime = "";
	String hModPgm = "";
	String pgmName = "";
	String tmpstr = "";
	String buf = "";
	String tmp = "";
	String szTmp = "";

	String hCallBatchSeqno = "";
	String hBusiBusinessDate = "";
	String hTempVouchDate = "";
	String hTempVouchChiDate = "";
	String hBusiVouchDate = "";
	String chiDate = "";

	String hVouchCdKind = "";
	String hGsvhAcNo = "";
	String hGsvhDbcr = "";

	String hAccmMemo3Kind = "";
	String hAccmMemo3Flag = "";
	String hAccmDrFlag = "";
	String hAccmCrFlag = "";

	String hChiShortName = "";
	String hPcceCurrEngName = "";
	String hPcceCurrChiName = "";
	String hPcceCurrCodeGl = "";
	String hPccdGlcode = "";
	String runBillType = "";
	String runAcctCode = "";
	String runTxnCode = "";
	String runVouchCode = "";
	String runMemo = "";

	int hGsvhDbcrSeq = 0;
	double callVoucherAmt = 0;
	double vouchKindCRAmt = 0;
	double vouchKindDRAmt = 0;
	double vouchDRAmt = 0;
	double vouchCRAmt = 0;
	int totalCnt = 0;
	int kindCnt = 0;
	int lineCnt = 0;

	double[] tailKindAmt = new double[10];
	int[] tailKindCnt = new int[10];
	String[] tailKindMemo = new String[10];
	String[] tailKindVouchCD = new String[10];

	int seqCnt = 1;

	/***********************************************************************/
	public int mainProcess(String[] args) {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================
			if (args.length > 1) {
				comc.errExit("Usage : BilV004, this program need only one parameter  ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			String runDate = "";
			if (args.length > 0) {
				runDate = "";
				if (args[0].length() == 8) {
					runDate = args[0];
				} else {
					String ErrMsg = String.format("指定營業日[%s]", args[0]);
					comcr.errRtn(ErrMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
				}
			}

			selectPtrBusinday(runDate);

			showLogMessage("I", "", String.format("BilV004程式開始......."));
			comcr.vouchPageCnt = 0;
			comcr.rptSeq = 0;
			pgmName = String.format("BilV004");

			for (int k = 0; k < 10; k++) {
				tailKindAmt[k] = 0;
				tailKindCnt[k] = 0;
				switch (k) {
				case 0:
					tailKindVouchCD[k] = "C001";
					tailKindMemo[k] = "年費(商務卡年費)";
					break;
				case 1:
					tailKindVouchCD[k] = "C003";
					tailKindMemo[k] = "預借現金手續費";
					break;
				case 2:
					tailKindVouchCD[k] = "C004";
					tailKindMemo[k] = "掛失處理費";
					break;
				case 3:
					tailKindVouchCD[k] = "C005";
					tailKindMemo[k] = "違約金";
					break;
				case 4:
					tailKindVouchCD[k] = "C006";
					tailKindMemo[k] = "法訴費";
					break;
				case 5:
					tailKindVouchCD[k] = "B001";
					tailKindMemo[k] = "循環息";
					break;
				case 6:
					tailKindVouchCD[k] = "C002";
					tailKindMemo[k] = "國外交易手續費";
					break;
				case 7:
					tailKindVouchCD[k] = "C002";
					tailKindMemo[k] = "台灣菸酒購貨手續費";
					break;
				case 8:
					tailKindVouchCD[k] = "F006";
					tailKindMemo[k] = "減免台灣菸酒購貨手續費";
					break;
				case 9:
					tailKindVouchCD[k] = "C002";
					tailKindMemo[k] = "持卡人手續費";
					break;
				}
			}

			processData();

			if (vouchDRAmt > 0 || vouchCRAmt > 0) {

				buf = "";
				for (int i = 0; i < 132; i++)
					buf += "-";
				lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

				lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
				printHeader();

				printFooter();
				String ftpName = String.format("%s_BilV004.%s_%s", rptId, sysDate, hBusiBusinessDate);
				String filename = String.format("%s/reports/%s_BilV004.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusiBusinessDate);
				// comcr.insertPtrBatchRpt(lpar1);
				comc.writeReport(filename, lpar1);
				
				ftpMput(ftpName);
			}

			// 出會計分錄
			for (int k = 0; k < 10; k++) {

				//法訴費不出分錄,由人工出
				if (k!=4) {
					procVouchData(k, tailKindVouchCD[k], tailKindMemo[k]);
				}

			}

			comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatchEnd();
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday(String runDate) throws Exception {
		hBusiBusinessDate = "";
		hTempVouchDate = "";
		hTempVouchChiDate = "";
		sqlCmd = "select business_date,";
		sqlCmd += " vouch_date,";
		sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'0000000'),2,7) h_temp_vouch_chi_date,";
		sqlCmd += " substr(to_char(to_number(vouch_date) - 19110000,'00000000'),4,6) h_busi_vouch_date ";
		sqlCmd += " from ptr_businday ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
			hTempVouchDate = getValue("vouch_date");
			hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
			hBusiVouchDate = getValue("h_busi_vouch_date");
		}

		showLogMessage("I", "", String.format("本日營業日期=[%s]", hBusiBusinessDate));

		if (runDate.length() == 8) {
			hBusiBusinessDate = runDate;
		}

		showLogMessage("I", "", String.format("程式處理日期=[%s]", hBusiBusinessDate));

	}

	/***********************************************************************/
	void processData() throws Exception {

		sqlCmd = "select a.purchase_date,a.card_no,a.dest_amt,a.curr_code,a.txn_code ";
		sqlCmd += "       ,a.acct_code,a.bill_type,a.sign_flag ";
		sqlCmd += "  from bil_curpost a ";
		sqlCmd += " where a.this_close_date = ? ";
		sqlCmd += "   and a.acct_code in ('AF','CF','LF','PF','PN','RI','SF') ";
		sqlCmd += "   and a.bill_type in ('OKOL','FIFC','OSSG') ";
		sqlCmd += "   and a.tx_convt_flag <> 'R' ";
		sqlCmd += " order by a.bill_type,a.txn_code ";

		setString(1, hBusiBusinessDate);

		int idx = 0;

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			runBillType = getValue("bill_type");
			runAcctCode = getValue("acct_code");
			runTxnCode = getValue("txn_code");
			runMemo = "";

			/*
			 * AF 第一類 C001 年費(商務卡年費) 
			 * CF 第二類 C003 預借現金手續費 
			 * LF 第三類 C004 掛失處理費 
			 * PN 第四類 C005 違約金
			 * SF 第五類 C006 法訴費 
			 * RI 第六類 B001 循環息 
			 * PF 第七類 C002 國外交易手續費 FIFC-PF 取正向 
			 * PF 第八類 C002 台灣菸酒購貨手續費 OSSG-TL 
			 * PF 第九類 F006 減免台灣菸酒購貨手續費 OSSG-17 PF(負向) 
			 * PF 第十類 C002 持卡人手續費  (PF其他費用類)
			 */

			if ("AF".equals(getValue("acct_code"))) {
				runVouchCode = "C001";
				runMemo = "年費(商務卡年費)";
				idx = 0;
				tailKindVouchCD[idx] = "C001";
			} else if ("CF".equals(getValue("acct_code"))) {
				runVouchCode = "C003";
				runMemo = "預借現金手續費";
				idx = 1;
			} else if ("LF".equals(getValue("acct_code"))) {
				runVouchCode = "C004";
				runMemo = "掛失處理費";
				idx = 2;
			} else if ("PN".equals(getValue("acct_code"))) {
				runVouchCode = "C005";
				runMemo = "違約金";
				idx = 3;
			} else if ("SF".equals(getValue("acct_code"))) {
				runVouchCode = "C006";
				runMemo = "法訴費";
				idx = 4;
			} else if ("RI".equals(getValue("acct_code")) && "OSSG".equals(getValue("bill_type"))) {
				runVouchCode = "B001";
				runMemo = "循環息";
				idx = 5;
			} else if ("PF".equals(getValue("acct_code")) && "FIFC".equals(getValue("bill_type"))) {
				runVouchCode = "C002";
				runMemo = "國外交易手續費";
				idx = 6;
			} else if ("PF".equals(getValue("acct_code")) && "OSSG".equals(getValue("bill_type"))
					&& "TL".equals(getValue("txn_code"))) {
				runVouchCode = "C002";
				runMemo = "台灣菸酒購貨手續費";
				idx = 7;
			} else if ("PF".equals(getValue("acct_code")) && "OSSG".equals(getValue("bill_type"))
					&& "17".equals(getValue("txn_code"))) {
				runVouchCode = "F006";
				runMemo = "減免台灣菸酒購貨手續費";
				idx = 8;
			} else if ("PF".equals(getValue("acct_code")) && "OKOL".equals(getValue("bill_type"))
					&& "HC".equals(getValue("txn_code"))) {
				continue;    //現金回饋補入帳,由行銷自行起帳(20231205)
			} else if ("PF".equals(getValue("acct_code"))) {
				runVouchCode = "C002";
				runMemo = "持卡人手續費";
				idx = 9;
			} else {
				continue;
			}

			if ("-".equals(getValue("sign_flag"))) {
				vouchCRAmt += getValueDouble("dest_amt");
			} else {
				vouchDRAmt += getValueDouble("dest_amt");
			}

			kindCnt++;

			tailKindAmt[idx] += getValueDouble("dest_amt");
			tailKindCnt[idx]++;

			totalCnt++;

			if (lineCnt == 0) {
				printHeader();
				lineCnt++;
			}

			if (lineCnt > 45) {
				lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
				printHeader();
				lineCnt = 0;
			}

			printDetail();
		}
		closeCursor(cursorIndex);

	}

	/***********************************************************************/
	void procVouchData(int idx, String stdVouchCode, String memo) throws Exception {

		// 會科套號
		hVouchCdKind = stdVouchCode;

		String currCode = "901";
		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(stdVouchCode);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=[" + stdVouchCode + "]");
			return;
		}

		comcr.hGsvhCurr = "00";

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilV004_%s.%s_%s", "C02", hVouchCdKind, hPcceCurrCodeGl);
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BILV04R01";

		int drIdx = 0;
		int crIdx = 0;

		for (int i = 0; i < itemCnt; i++) {
			hGsvhAcNo = getValue("ac_no", i);
			hGsvhDbcrSeq = getValueInt("dbcr_seq", i);
			hGsvhDbcr = getValue("dbcr", i);
			hAccmMemo3Kind = getValue("memo3_kind", i);
			hAccmMemo3Flag = getValue("h_accm_memo3_flag", i);
			hAccmDrFlag = getValue("h_accm_dr_flag", i);
			hAccmCrFlag = getValue("h_accm_cr_flag", i);

			/* Memo 1, Memo 2, Memo3 */
			comcr.hGsvhMemo1 = memo;
			comcr.hGsvhMemo2 = "";
			comcr.hGsvhMemo3 = "";

			callVoucherAmt = tailKindAmt[idx];

			if (callVoucherAmt != 0) {
				if (comcr.detailVouch(hGsvhAcNo, hGsvhDbcrSeq, callVoucherAmt, hPcceCurrCodeGl) != 0) {
					showLogMessage("E", "", "call detail_vouch error, AcNo=[" + hGsvhAcNo + "]");
					return;
				}
			}
		}
	}

	/**************************************************************************/
	int selectGenSysVouch(String stdVouchCode) throws Exception {

		sqlCmd = "select ";
		sqlCmd += " gen_sys_vouch.ac_no,";
		sqlCmd += " gen_sys_vouch.dbcr_seq,";
		sqlCmd += " gen_sys_vouch.dbcr,";
		sqlCmd += " gen_acct_m.memo3_kind,";
		sqlCmd += " decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) h_accm_memo3_flag,";
		sqlCmd += " decode(gen_acct_m.dr_flag,'','N',gen_acct_m.dr_flag) h_accm_dr_flag,";
		sqlCmd += " decode(gen_acct_m.cr_flag,'','N',gen_acct_m.cr_flag) h_accm_cr_flag ";
		sqlCmd += " from gen_sys_vouch,gen_acct_m ";
		sqlCmd += "where std_vouch_cd = ? ";
		sqlCmd += "  and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
		sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";

		setString(1, stdVouchCode);
		int recordCnt1 = selectTable();

		return recordCnt1;
	}

	/***********************************************************************/
	void selectPtrCurrcode(String currCode) throws Exception {
		hPcceCurrEngName = "";
		hPcceCurrChiName = "";
		hPcceCurrCodeGl = "";
		sqlCmd = "select curr_eng_name,";
		sqlCmd += "       curr_chi_name,";
		sqlCmd += "       curr_code_gl ";
		sqlCmd += " from ptr_currcode  ";
		sqlCmd += "where curr_code = ? ";
		setString(1, currCode);
		int recordCnt1 = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_currcode not found!", "", "");
		}
		if (recordCnt1 > 0) {
			hPcceCurrEngName = getValue("curr_eng_name");
			hPcceCurrChiName = getValue("curr_chi_name");
			hPcceCurrCodeGl = getValue("curr_code_gl");
		}

	}

	/***********************************************************************/
	void printHeader() {
		pageCnt++;

		buf = "";
		buf = comcr.insertStr(buf, rptId, 1);
		buf = comcr.insertStrCenter(buf, rptName, 132);
		buf = comcr.insertStr(buf, "頁次:", 110);
		szTmp = String.format("%4d", pageCnt);
		buf = comcr.insertStr(buf, szTmp, 118);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "印表日期:", 1);
		buf = comcr.insertStr(buf, sysDate, 10);
		buf = comcr.insertStr(buf, "入帳日 :", 20);
		buf = comcr.insertStr(buf, hBusiBusinessDate, 30);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "交易日期", 1);
		buf = comcr.insertStr(buf, "交易摘要", 11);
		buf = comcr.insertStr(buf, "卡     號", 50);
		buf = comcr.insertStr(buf, "     交易金額", 79);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		for (int i = 0; i < 132; i++)
			buf += "-";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
	}

	/***********************************************************************/
	void printFooter() {

		buf = "";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "借方合計", 56);
		buf = comcr.insertStr(buf, "貸方合計", 80);
		buf = comcr.insertStr(buf, "筆數", 100);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		for (int k = 0; k < 10; k++) {

			if (k == 8) {
				vouchKindDRAmt = 0;
				vouchKindCRAmt = tailKindAmt[k];
			} else {
				vouchKindDRAmt = tailKindAmt[k];
				vouchKindCRAmt = 0;
			}
			buf = "";
			buf = comcr.insertStr(buf, tailKindMemo[k], 21);
			szTmp = comcr.commFormat("3$,3$,3$,3$", vouchKindDRAmt);
			buf = comcr.insertStr(buf, szTmp, 48);
			szTmp = comcr.commFormat("3$,3$,3$,3$", vouchKindCRAmt);
			buf = comcr.insertStr(buf, szTmp, 72);
			szTmp = comcr.commFormat("3z,3z,3z", tailKindCnt[k]);
			buf = comcr.insertStr(buf, szTmp, 92);
			lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
		}

		buf = "";
		buf = comcr.insertStr(buf, "合  計:", 21);
		szTmp = comcr.commFormat("3$,3$,3$,3$", vouchDRAmt);
		buf = comcr.insertStr(buf, szTmp, 48);
		szTmp = comcr.commFormat("3$,3$,3$,3$", vouchCRAmt);
		buf = comcr.insertStr(buf, szTmp, 72);
		szTmp = comcr.commFormat("3z,3z,3z", kindCnt);
		buf = comcr.insertStr(buf, szTmp, 92);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
	}

	/***********************************************************************/
	void printDetail() throws Exception {
		lineCnt++;

		buf = "";
		buf = comcr.insertStr(buf, getValue("purchase_date"), 1);
		buf = comcr.insertStr(buf, (runBillType + "/" + runTxnCode + "-" + runMemo), 11);
		buf = comcr.insertStr(buf, getValue("card_no"), 50);
		szTmp = comcr.commFormat("3$,3$,3$,3$", getValueDouble("dest_amt"));
		buf = comcr.insertStr(buf, szTmp, 76);

		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

	}
	
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "CREDITCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "CREDITCARD";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }

	/***********************************************************************/
	public static void main(String[] args) throws Exception {

		BilV004 proc = new BilV004();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
