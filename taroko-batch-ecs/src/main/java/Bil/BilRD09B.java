/******************************************************************************
*                                                                             *
*                             MODIFICATION LOG                                *
*                                                                             *
*     DATE   Version    AUTHOR                       DESCRIPTION              *
*  --------- --------- ----------- -----------------------------------------  *
*  112/06/08 V1.00.01  JeffKung    program initial                            *
*                                                                             *
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
import com.CommFunction;

/*會計起帳(BIL)處理*/
public class BilRD09B extends AccessDAO {

	public final boolean DEBUG_MODE = false;

	private String PROGNAME = "紅利點數線上折抵刷卡消費價差明細表(請款)  112/06/08 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	final int DEBUG = 0;

	String prgmId = "BilRD09B";
	String prgmName = "紅利點數線上折抵刷卡消費價差明細表(請款)";
	String rptName = "紅利點數線上折抵刷卡消費價差明細表(請款)";
	String rptId = "CRD02A";
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

	double totalDestAmtCR = 0;
	double totalDestAmtDR = 0;
	double totalCashPayAmtCR = 0;
	double totalCashPayAmtDR = 0;
	double totalBpAmtCR = 0;
	double totalBpAmtDR = 0;

	private int maxvouchCRAmtLength = 10;
	double[] vouchCRAmt = new double[maxvouchCRAmtLength];
	double[] vouchDRAmt = new double[maxvouchCRAmtLength];

	int hGsvhDbcrSeq = 0;
	double callVoucherAmt = 0;
	double vouchKindAmt = 0;
	int totalCnt = 0;
	int kindCnt = 0;
	int lineCnt = 0;

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
				comc.errExit("Usage : BilRD09B, this program need only one parameter  ", "");
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

			showLogMessage("I", "", String.format("BilRD09B程式處理開始......."));
			comcr.vouchPageCnt = 0;
			comcr.rptSeq = 0;
			pgmName = String.format("BilRD09B");

			processData();

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

		sqlCmd = "select card_no,payment_type,purchase_date,this_close_date, ";
		sqlCmd += "       dest_amt,deduct_bp,cash_pay_amt,sign_flag, ";
		sqlCmd += "       (dest_amt - cash_pay_amt) bp_amt,txn_code ";
		sqlCmd += "  from bil_curpost ";
		sqlCmd += " where this_close_date = ? ";
		sqlCmd += "   and payment_type in ('1','2') ";
		sqlCmd += "   and bill_type = 'FISC' ";
		sqlCmd += "   and tx_convt_flag <> 'R' ";
		sqlCmd += " order by txn_code ";

		setString(1, hBusiBusinessDate);

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			totalCnt++;

			if (lineCnt == 0) {
				printHeader();
				lineCnt++;
			}

			if (lineCnt > 25) {
				lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
				printHeader();
				lineCnt = 0;
			}

			printDetail();
		}

		closeCursor(cursorIndex);

		// 紅利線上折抵(J006)
		showLogMessage("I", "", String.format("紅利線上折抵(J006)......."));
		procVouchData(totalBpAmtDR, "J006", "紅利點數線上折抵現金");

		// 紅利線上折抵退貨(J007)
		showLogMessage("I", "", String.format("紅利線上折抵退貨(J007)...."));
		procVouchData(totalBpAmtCR, "J007", "紅利點數線上折抵現金退貨");


		if (totalBpAmtCR > 0 || totalBpAmtDR > 0) {
			printFooter();
			String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptId, sysDate);
			// 改為線上報表
			comcr.insertPtrBatchRpt(lpar1);
			if (DEBUG == 1)
				comc.writeReport(filename, lpar1);
		}
	}

	/***********************************************************************/
	void procVouchData(double sumAmt, String vouchCdKind, String memo) throws Exception {

		// initial amt fields
		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = 0; // 貸方科目金額
			vouchDRAmt[int1] = 0; // 借方科目金額
		}

		vouchDRAmt[1] = sumAmt;
		
		vouchCRAmt[1] = sumAmt;
		
		// 會科套號
		hVouchCdKind = vouchCdKind;
		String currCode = "901";

		selectPtrCurrcode(currCode);

		int itemCnt = selectGenSysVouch(hVouchCdKind);

		if (itemCnt <= 0) {
			showLogMessage("E", "", "gen_sys_vouch not defined, stdVouchCode=["+vouchCdKind+"]");
			return;
		}

		for (int int1 = 1; int1 < maxvouchCRAmtLength; int1++) {
			vouchCRAmt[int1] = comcr.commCurrAmt(currCode, vouchCRAmt[int1], 0);
			vouchDRAmt[int1] = comcr.commCurrAmt(currCode, vouchDRAmt[int1], 0);
		}

		comcr.hGsvhCurr = hPcceCurrCodeGl;

		chiDate = String.format("%07d", comcr.str2long(hTempVouchDate) - 19110000);

		comcr.startVouch("1", hVouchCdKind); // 在進入routin前指定hVouchCdKind

		tmpstr = String.format("BilRD09B.%s_%s",hVouchCdKind, hPcceCurrCodeGl);  
		comcr.hGsvhModPgm = tmpstr;
		comcr.hGsvhModWs = "BilRD09B";

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

			if ("D".equals(hGsvhDbcr)) {
				drIdx++;
				callVoucherAmt = vouchDRAmt[drIdx];
			} else {
				crIdx++;
				callVoucherAmt = vouchCRAmt[crIdx];
			}
		
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
		buf = comcr.insertStr(buf, "卡     號", 40); // 16靠左
		buf = comcr.insertStr(buf, "       交易金額", 59); // 15靠右
		buf = comcr.insertStr(buf, " 折抵點數", 77); // 9靠右
		buf = comcr.insertStr(buf, "   實際入帳金額", 89); // 15靠右
		buf = comcr.insertStr(buf, "   折抵價差金額", 107); // 15靠右

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
		
		for (int i = 0; i < 132; i++)
			buf += "-";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "小計:購貨交易金額:", 1);
		szTmp = comcr.commFormat("3$,3$,3$,3$", totalDestAmtDR);  //16
		buf = comcr.insertStr(buf, szTmp, 20);
	
		buf = comcr.insertStr(buf, "實際購貨入帳金額:", 40);
		szTmp = comcr.commFormat("3$,3$,3$,3$", totalCashPayAmtDR);  //16
		buf = comcr.insertStr(buf, szTmp, 58);
		
		buf = comcr.insertStr(buf, "折抵價差金額:", 80);
		szTmp = comcr.commFormat("3$,3$,3$,3$", totalBpAmtDR);  //16
		buf = comcr.insertStr(buf, szTmp, 95);

		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "     退貨交易金額:", 1);
		szTmp = comcr.commFormat("3$,3$,3$,3$", totalDestAmtCR);  //16
		buf = comcr.insertStr(buf, szTmp, 20);
	
		buf = comcr.insertStr(buf, "實際退貨入帳金額:", 40);
		szTmp = comcr.commFormat("3$,3$,3$,3$", totalCashPayAmtCR);  //16
		buf = comcr.insertStr(buf, szTmp, 58);
		
		buf = comcr.insertStr(buf, "折抵價差金額:", 80);
		szTmp = comcr.commFormat("3$,3$,3$,3$", totalBpAmtCR);  //16
		buf = comcr.insertStr(buf, szTmp, 95);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
	}

	/***********************************************************************/
	void printDetail() throws Exception {
		lineCnt++;

		buf = "";
		buf = comcr.insertStr(buf, getValue("purchase_date"), 1);

		if ("05".equals(getValue("txn_code"))) {
			buf = comcr.insertStr(buf, "05-購貨", 10);
		} else if ("06".equals(getValue("txn_code"))) {
			buf = comcr.insertStr(buf, "06-退貨", 10);
		} else if ("07".equals(getValue("txn_code"))) {
			buf = comcr.insertStr(buf, "07-預現", 10);
		} else if ("25".equals(getValue("txn_code"))) {
			buf = comcr.insertStr(buf, "25-購貨沖銷", 10);
		} else if ("26".equals(getValue("txn_code"))) {
			buf = comcr.insertStr(buf, "26-退貨沖銷", 10);
		} else if ("27".equals(getValue("txn_code"))) {
			buf = comcr.insertStr(buf, "27-預現沖銷", 10);
		}

		buf = comcr.insertStr(buf, getValue("card_no"), 40);

		szTmp = comcr.commFormat("3$,3$,3$,3$", getValueDouble("dest_amt"));
		buf = comcr.insertStr(buf, szTmp, 58);

		szTmp = String.format("%,7d", getValueInt("deduct_bp"));
		buf = comcr.insertStr(buf, szTmp, 79);

		szTmp = comcr.commFormat("3$,3$,3$,3$", getValueDouble("cash_pay_amt"));
		buf = comcr.insertStr(buf, szTmp, 88);

		szTmp = comcr.commFormat("3$,3$,3$,3$", getValueDouble("bp_amt"));
		buf = comcr.insertStr(buf, szTmp, 106);

		if ("-".equals(getValue("sign_flag"))) {
			totalDestAmtCR = totalDestAmtCR + getValueDouble("dest_amt");
			totalCashPayAmtCR = totalCashPayAmtCR + getValueDouble("cash_pay_amt");
			totalBpAmtCR = totalBpAmtCR + getValueDouble("bp_amt");
		} else {
			totalDestAmtDR = totalDestAmtDR + getValueDouble("dest_amt");
			totalCashPayAmtDR = totalCashPayAmtDR + getValueDouble("cash_pay_amt");
			totalBpAmtDR = totalBpAmtDR + getValueDouble("bp_amt");
		}

		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {

		BilRD09B proc = new BilRD09B();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
