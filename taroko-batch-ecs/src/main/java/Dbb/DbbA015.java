/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/07/21  V1.00.00    Brian     program initial                           *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  111/03/11  V1.00.04    JeffKung 增加auth_nt_amt >0的條件         *
*  111/03/28  V1.00.05    JeffKung 請款一律入帳,問交記錄僅為參考     *
******************************************************************************/

package Dbb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*Debit Card 授權請款差異明細表*/
public class DbbA015 extends AccessDAO {
	private final String progname = "Debit Card 授權請款差異明細表 111/03/28  V1.00.05";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	int debug = 0;

	String rptId = "DBB_A015R1";
	String rptName = "Debit Card 授權請款差異明細表";
	int rptSeq = 0;
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String hCallBatchSeqno = "";
	String hBusinessDate = "";
	String buf = "";
	int totalCnt = 0;
	int pageCnt = 0;

	String hCardNo = "";
	String hPurchaseDate = "";
	String hThisCloseDate = "";
	String hBillType = "";
	String hTxnCode = "";
	String hSourceCurr = "";
	double hSourceAmt = 0;
	String hDestCurr = "";
	double hDestAmt = 0;
	double hAuthNtAmt = 0;
	double hDiffAmt = 0;
	String hAuthCode = "";
	String hTxnDesc = "";
	String hMchtCountry = "";

	double hSumSourceAmt = 0;
	double hSumDestAmt = 0;
	double hSumAuthNtAmt = 0;
	double hSumDiffAmt = 0;

	// ***********************************************************

	public int mainProcess(String[] args) {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname + ", args=" + args.length);
			// =====================================
			if (args.length > 2) {
				comc.errExit("Usage : DbbA015 [businessDate] [batchSeqno]", "");
			}
			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			if (args.length > 0 && args[0].length() == 8)
				hBusinessDate = args[0];

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

			selectPtrBusinday();
			selectDbbCurpost();
			// ==============================================
			// 固定要做的

			comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
			showLogMessage("I", "", comcr.hCallErrorDesc);
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
	void selectPtrBusinday() throws Exception {

		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += "fetch first 1 rows only ";
		if (selectTable() > 0) {
			hBusinessDate = hBusinessDate.length() == 0 ? getValue("business_date") : hBusinessDate;
		}
	}

	/***********************************************************************/
	void selectDbbCurpost() throws Exception {
		int lineCnt = 0;
		sqlCmd = " SELECT card_no, "; // 卡號
		sqlCmd += "        PURCHASE_DATE, "; // 消費日期
		sqlCmd += "        THIS_CLOSE_DATE, "; // 入帳日期
		sqlCmd += "        bill_type,TXN_CODE, "; // 帳單類別
		sqlCmd += "        SOURCE_CURR,SOURCE_AMT,  "; // 交易幣別/交易金額
		sqlCmd += "        DEST_CURR,DEST_AMT, "; // 帳單幣別/帳單金額
		sqlCmd += "        AUTH_NT_AMT, "; // 授權金額
		sqlCmd += "        (auth_nt_amt - DEST_AMT) AS diff_amt, "; // 差異金額
		sqlCmd += "        AUTH_CODE, "; // 授權碼
		sqlCmd += "        decode(MCHT_CHI_NAME,'',MCHT_ENG_NAME,MCHT_CHI_NAME) AS txn_desc, "; // 交易說明
		sqlCmd += "        MCHT_COUNTRY "; // 特店國家碼
		sqlCmd += "   FROM dbb_curpost ";
		sqlCmd += "  WHERE CURR_POST_FLAG = 'Y' ";
		//sqlCmd += "    AND rsk_type in ('','4') ";   //V1.00.05
		sqlCmd += "    AND rsk_type <> '1' ";     //V1.00.05--除了找不到卡號rsk_type = '1'以外都處理
		sqlCmd += "    AND bill_type = 'FISC' ";
		sqlCmd += "    AND txn_code IN ('05','07')  ";
		sqlCmd += "    AND this_close_date = ? ";
		sqlCmd += "    AND auth_nt_amt > 0 ";
		sqlCmd += "    AND (auth_nt_amt - DEST_AMT) <> 0 ";
		setString(1, hBusinessDate);
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {
			hCardNo = getValue("card_no");
			hPurchaseDate = getValue("purchase_date");
			hThisCloseDate = getValue("this_close_date");
			hBillType = getValue("bill_type");
			hTxnCode = getValue("txn_code");
			hSourceCurr = getValue("source_curr");
			hSourceAmt = getValueDouble("source_amt");
			hDestCurr = getValue("dest_curr");
			hDestAmt = getValueDouble("dest_amt");
			hAuthNtAmt = getValueDouble("auth_nt_amt");
			hDiffAmt = getValueDouble("diff_amt");
			hAuthCode = getValue("auth_code");
			hTxnDesc = getValue("txn_desc");
			hMchtCountry = getValue("mcht_country");

			hSumSourceAmt += hSourceAmt;
			hSumDestAmt += hDestAmt;
			hSumAuthNtAmt += hAuthNtAmt;
			hSumDiffAmt += hDiffAmt;

			totalCnt++;
			if (totalCnt == 1 || totalCnt % 58 == 0) {
				lineCnt = 0;
				printHeader();
			}
			lineCnt++;
			buf = String.format("%3d %16s  %8s  %8s  %4s/%2s   %3s   %9.2f  %3s   %9.2f  %9.2f  %9.2f  %6s  %s",
					lineCnt, hCardNo, hPurchaseDate, hThisCloseDate, hBillType, hTxnCode, hSourceCurr, hSourceAmt,
					hDestCurr, hDestAmt, hAuthNtAmt, hDiffAmt, hAuthCode, comc.fixLeft(hTxnDesc, 20));

			lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
		}
		closeCursor(cursorIndex);

		if (totalCnt > 0) {
			printFooter();
			comcr.insertPtrBatchRpt(lpar1);
		}

	}

	/***********************************************************************/
	void printHeader() {
		pageCnt++;
		buf = "";
		buf = comcr.insertStr(buf, "報表名稱: " + rptId, 1);
		buf = comcr.insertStrCenter(buf, "Debit Card 授權請款差異明細表", 136);
		buf = comcr.insertStr(buf, "頁    次:", 118);
		buf = comcr.insertStr(buf, String.format("%4d", pageCnt), 132);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "印表日期:", 1);
		buf = comcr.insertStr(buf, chinDate, 11);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf,
				"  # 卡號              消費日期  入帳日期  帳單類別  交易  交易金額   帳單  帳單金額   授權金額   差異金額  授權碼  交易說明", 1);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
		buf = "";
		buf = comcr.insertStr(buf,
				"                                                    幣別             幣別                                                  ", 1);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		for (int i = 0; i < 136; i++)
			buf += "=";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
	}

	/***********************************************************************/
	void printFooter() {

		buf = "";
		for (int i = 0; i < 136; i++)
			buf += "=";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
		buf = "";
		buf = comcr.insertStr(buf, "總計 :", 1);
		buf = comcr.insertStr(buf, totalCnt + "筆", 17);
		buf = comcr.insertStr(buf, String.format("%9.2f",hSumSourceAmt) , 59);
		buf = comcr.insertStr(buf, String.format("%9.2f",hSumDestAmt), 76);
		buf = comcr.insertStr(buf, String.format("%9.2f",hSumAuthNtAmt), 87);
		buf = comcr.insertStr(buf, String.format("%9.2f",hSumDiffAmt), 98);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbbA015 proc = new DbbA015();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
