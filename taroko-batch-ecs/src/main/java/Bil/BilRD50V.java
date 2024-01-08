/******************************************************************************
*                                                                             *
*                             MODIFICATION LOG                                *
*                                                                             *
*     DATE   Version    AUTHOR                       DESCRIPTION              *
*  --------- --------- ----------- -----------------------------------------  *
*  112/08/28 V1.00.01  JeffKung    program initial                            *
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

import org.json.JSONObject;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class BilRD50V extends AccessDAO {

	public final boolean DEBUG_MODE = false;

	private String PROGNAME = "國際信用卡清算統計表-VISA金融卡  112/08/28 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	JSONObject     rptData = null;

	final int DEBUG = 0;

	String prgmId = "BilRD50V";
	String rptName = "國際信用卡清算統計表-VISA金融卡";
	String rptId = "CRD50V";
	int rptSeq = 0;
	int pageCnt = 0;
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

	String hModUser = "";
	String hModTime = "";
	String hModPgm = "BilRD50V";
	String pgmName = "";

	String tmpstr = "";
	String buf = "";
	String tmp = "";
	String szTmp = "";

	String hCallBatchSeqno = "";
	String hBusinessDate = "";
	String hChiDate = "";
	String hLastDate = "";
	String hRptYYYYMM = "";

	long totIntlPositiveAmt = 0;
	int  totIntlPositiveCnt = 0;
	long totIntlNegativeAmt = 0;
	int  totIntlNegativeCnt = 0;
	long totFiscPositiveAmt = 0;
	int  totFiscPositiveCnt = 0;
	long totFiscNegativeAmt = 0;
	int  totFiscNegativeCnt = 0;
	long totNcccPositiveAmt = 0;
	int  totNcccPositiveCnt = 0;
	long totNcccNegativeAmt = 0;
	int  totNcccNegativeCnt = 0;
	long totOnusPositiveAmt = 0;
	int  totOnusPositiveCnt = 0;
	long totOnusNegativeAmt = 0;
	int  totOnusNegativeCnt = 0;
	long totNatPositiveAmt  = 0;
	int  totNatPositiveCnt  = 0;
	long totNatNegativeAmt  = 0;
	int  totNatNegativeCnt  = 0;

	long intlPositiveAmt = 0;
	int  intlPositiveCnt = 0;
	long intlNegativeAmt = 0;
	int  intlNegativeCnt = 0;
	long fiscPositiveAmt = 0;
	int  fiscPositiveCnt = 0;
	long fiscNegativeAmt = 0;
	int  fiscNegativeCnt = 0;
	long ncccPositiveAmt = 0;
	int  ncccPositiveCnt = 0;
	long ncccNegativeAmt = 0;
	int  ncccNegativeCnt = 0;
	long onusPositiveAmt = 0;
	int  onusPositiveCnt = 0;
	long onusNegativeAmt = 0;
	int  onusNegativeCnt = 0;
	long natPositiveAmt  = 0;
	int  natPositiveCnt  = 0;
	long natNegativeAmt  = 0;
	int  natNegativeCnt  = 0;

	long totMonthAmt = 0;
	
	int totalCnt = 0;
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
				comc.errExit("Usage : BilRD50V, this program need only one parameter  ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			rptData = new JSONObject();
			
			String runDate = "";
			if (args.length > 0) {
				if (args[0].length() == 8) {
					runDate = args[0];
				} else {
					String ErrMsg = String.format("指定營業日期[%s]", args[0]);
					comcr.errRtn(ErrMsg, "營業日期長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
				}
			}

			showLogMessage("I", "", String.format("程式參數處理YYYYMMDD=[%s]", runDate));

			selectPtrBusinday(runDate);

			showLogMessage("I", "", String.format("\n VD交易處理 ......."));
			procDbbCurrpost();
			
			//若是月底要寫入統計報表來源資料table
		    if (hBusinessDate.equals(hLastDate)) {
		   	 	insertMisReportData();
		    }

			comcr.hCallErrorDesc = "程式執行結束";
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

		hBusinessDate = "";
		hLastDate = "";
		hRptYYYYMM = "";
		hChiDate = "";
		sqlCmd = "select business_date ";
		sqlCmd += "     , substr(to_char(to_number(business_date) - 19110000,'0000000'),2,7) h_chi_date ";
		sqlCmd += "     , to_char(last_day(to_date(business_date,'yyyymmdd')),'yyyymmdd')    h_last_date ";
		sqlCmd += " from ptr_businday ";

		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		if (recordCnt > 0) {
			hBusinessDate = getValue("business_date");
			hChiDate = getValue("h_chi_date");
			hLastDate = getValue("h_last_date");
		}

		if (runDate.length() == 8) {
			hBusinessDate = runDate;
			hChiDate = String.format("%7d",comc.str2int(runDate)-19110000);
			hLastDate = comm.lastdateOfmonth(hBusinessDate);
		}
		
		hRptYYYYMM = hBusinessDate.substring(0, 6);

		showLogMessage("I", "", String.format("本日營業日期=[%s][%s]", hBusinessDate, hRptYYYYMM));

	}

	/***********************************************************************/
	void procDbbCurrpost() throws Exception {

		String keepDate = "";

		sqlCmd = "select this_close_date,settl_flag,sign_flag,sum(dest_amt) as sub_amt,count(*) as sub_cnt ";
		sqlCmd += "  from dbb_curpost ";
		sqlCmd += " where this_close_date like ?  ";
		sqlCmd += "   and bill_type = 'FISC'      ";
		sqlCmd += "   and settl_flag in ('0','6','8','9') ";
		sqlCmd += "   and ecs_platform_kind not in ('b1','G2') ";
		sqlCmd += " group by this_close_date,settl_flag,sign_flag ";
		sqlCmd += " order by this_close_date,settl_flag,sign_flag ";
		setString(1, hRptYYYYMM + "%");

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			if (keepDate.equals(getValue("this_close_date")) == false) {
				if (totalCnt == 0) {
					printHeader();
				} else {
					printDetail(keepDate);
				}

				keepDate = getValue("this_close_date");
				intlPositiveAmt = 0;
				intlPositiveCnt = 0;
				intlNegativeAmt = 0;
				intlNegativeCnt = 0;
				fiscPositiveAmt = 0;
				fiscPositiveCnt = 0;
				fiscNegativeAmt = 0;
				fiscNegativeCnt = 0;
				ncccPositiveAmt = 0;
				ncccPositiveCnt = 0;
				ncccNegativeAmt = 0;
				ncccNegativeCnt = 0;
				onusPositiveAmt = 0;
				onusPositiveCnt = 0;
				onusNegativeAmt = 0;
				onusNegativeCnt = 0;
				natPositiveAmt  = 0;
				natPositiveCnt  = 0;
				natNegativeAmt  = 0;
				natNegativeCnt  = 0;

			}
			
			totalCnt ++;

			if ("0".equals(getValue("settl_flag"))) {
				if ("+".equals(getValue("sign_flag"))) {
					intlPositiveAmt = getValueLong("sub_amt");
					intlPositiveCnt = getValueInt("sub_cnt");
				} else {
					intlNegativeAmt = getValueLong("sub_amt");
					intlNegativeCnt = getValueInt("sub_cnt");
				}
			} else if ("6".equals(getValue("settl_flag"))) {
				if ("+".equals(getValue("sign_flag"))) {
					fiscPositiveAmt = getValueLong("sub_amt");
					fiscPositiveCnt = getValueInt("sub_cnt");
					natPositiveAmt += getValueLong("sub_amt");
					natPositiveCnt += getValueInt("sub_cnt");
				} else {
					fiscNegativeAmt = getValueLong("sub_amt");
					fiscNegativeCnt = getValueInt("sub_cnt");
					natNegativeAmt += getValueLong("sub_amt");
					natNegativeCnt += getValueInt("sub_cnt");
				}
			} else if ("8".equals(getValue("settl_flag"))) {
				if ("+".equals(getValue("sign_flag"))) {
					ncccPositiveAmt = getValueLong("sub_amt");
					ncccPositiveCnt = getValueInt("sub_cnt");
					natPositiveAmt += getValueLong("sub_amt");
					natPositiveCnt += getValueInt("sub_cnt");
				} else {
					ncccNegativeAmt = getValueLong("sub_amt");
					ncccNegativeCnt = getValueInt("sub_cnt");
					natNegativeAmt += getValueLong("sub_amt");
					natNegativeCnt += getValueInt("sub_cnt");
				}
			} else if ("9".equals(getValue("settl_flag"))) {
				if ("+".equals(getValue("sign_flag"))) {
					onusPositiveAmt = getValueLong("sub_amt");
					onusPositiveCnt = getValueInt("sub_cnt");
					natPositiveAmt += getValueLong("sub_amt");
					natPositiveCnt += getValueInt("sub_cnt");
				} else {
					onusNegativeAmt = getValueLong("sub_amt");
					onusNegativeCnt = getValueInt("sub_cnt");
					natNegativeAmt += getValueLong("sub_amt");
					natNegativeCnt += getValueInt("sub_cnt");
				}
			}

		}
		if (totalCnt > 0) {
			printDetail(keepDate);
			printFooter();
            String ftpName = String.format("%s.%s_%s", rptId, sysDate, hBusinessDate);
            String filename = String.format("%s/reports/%s.%s_%s", comc.getECSHOME(), rptId, sysDate, hBusinessDate);

            comc.writeReport(filename, lpar1);
			comcr.insertPtrBatchRpt(lpar1);
            ftpMput(ftpName);

		}
		closeCursor(cursorIndex);
	}

	/***********************************************************************/
	void printHeader() {
		pageCnt++;

		buf = "";
		buf = comcr.insertStr(buf, "分行代號: " + "3144 信用卡部", 1);
		buf = comcr.insertStrCenter(buf, rptName, 130);
		buf = comcr.insertStr(buf, "保存年限: 二年", 110);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf + "\r"));

		buf = "";
		tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiDate.substring(0, 3), hChiDate.substring(3, 5),
				hChiDate.substring(5));
		buf = comcr.insertStr(buf, "報表代號:" + rptId, 1);
		buf = comcr.insertStrCenter(buf, "中華民國 " + tmp, 130);
		buf = comcr.insertStr(buf, "頁    次:", 110);
		szTmp = String.format("%4d", pageCnt);
		buf = comcr.insertStr(buf, szTmp, 120);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf,
				"                       金資帳款               NCCC帳款               國外帳款               自行帳款                 總計帳款       ", 1);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf,
				"入帳日期            金額     筆數          金額     筆數          金額     筆數          金額     筆數            金額     筆數     ", 1);
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		for (int i = 0; i < 130; i++)
			buf += "=";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
	}

	/***********************************************************************/
	void printFooter() {
		buf = "";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		for (int i = 0; i < 130; i++)
			buf += "=";
		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "合計", 1);
		szTmp = comcr.commFormat("#,3#,3#,3#", (totFiscPositiveAmt-totFiscNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 12);
		szTmp = comcr.commFormat("6#", (totFiscPositiveCnt + totFiscNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 27);
		szTmp = comcr.commFormat("#,3#,3#,3#", (totNcccPositiveAmt-totNcccNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 35);
		szTmp = comcr.commFormat("6#", (totNcccPositiveCnt + totNcccNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 50);
		szTmp = comcr.commFormat("#,3#,3#,3#", (totIntlPositiveAmt-totIntlNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 58);
		szTmp = comcr.commFormat("6#", (totIntlPositiveCnt + totIntlNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 73);
		szTmp = comcr.commFormat("#,3#,3#,3#", (totOnusPositiveAmt-totOnusNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 81);
		szTmp = comcr.commFormat("6#", (totOnusPositiveCnt + totOnusNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 96);
		szTmp = comcr.commFormat("#,3#,3#,3#", (totNatPositiveAmt + totIntlPositiveAmt - totNatNegativeAmt - totIntlNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 106);
		szTmp = comcr.commFormat("6#", (totNatPositiveCnt + totNatNegativeCnt + totIntlPositiveCnt + totIntlNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 121);

		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		rptData.put("NAT_VD_RTL_AMT", (totNatPositiveAmt - totNatNegativeAmt));
		rptData.put("NAT_VD_RTL_CNT", (totNatPositiveCnt + totNatNegativeCnt));
		rptData.put("INT_VD_RTL_AMT", (totIntlPositiveAmt - totIntlNegativeAmt));
		rptData.put("INT_VD_RTL_CNT", (totIntlPositiveCnt + totIntlNegativeCnt));
		
		totMonthAmt = totNatPositiveAmt + totIntlPositiveAmt - totNatNegativeAmt - totIntlNegativeAmt;

	}

	/***********************************************************************/
	void printDetail(String keepDate) throws Exception {

		lineCnt++;

		buf = "";
		buf = comcr.insertStr(buf, keepDate.substring(4, 6) + "/" + keepDate.substring(6, 8) , 1);
		szTmp = comcr.commFormat("#,3#,3#,3#", (fiscPositiveAmt-fiscNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 12);
		szTmp = comcr.commFormat("6#", (fiscPositiveCnt + fiscNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 27);
		szTmp = comcr.commFormat("#,3#,3#,3#", (ncccPositiveAmt-ncccNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 35);
		szTmp = comcr.commFormat("6#", (ncccPositiveCnt + ncccNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 50);
		szTmp = comcr.commFormat("#,3#,3#,3#", (intlPositiveAmt-intlNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 58);
		szTmp = comcr.commFormat("6#", (intlPositiveCnt + intlNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 73);
		szTmp = comcr.commFormat("#,3#,3#,3#", (onusPositiveAmt-onusNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 81);
		szTmp = comcr.commFormat("6#", (onusPositiveCnt + onusNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 96);
		szTmp = comcr.commFormat("#,3#,3#,3#", (natPositiveAmt + intlPositiveAmt - natNegativeAmt - intlNegativeAmt));
		buf = comcr.insertStr(buf, szTmp, 106);
		szTmp = comcr.commFormat("6#", (natPositiveCnt + natNegativeCnt + intlPositiveCnt + intlNegativeCnt));
		buf = comcr.insertStr(buf, szTmp, 121);

		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

		totFiscPositiveAmt += fiscPositiveAmt;
		totFiscPositiveCnt += fiscPositiveCnt;
		totFiscNegativeAmt += fiscNegativeAmt;
		totFiscNegativeCnt += fiscNegativeCnt;
		totNcccPositiveAmt += ncccPositiveAmt;
		totNcccPositiveCnt += ncccPositiveCnt;
		totNcccNegativeAmt += ncccNegativeAmt;
		totNcccNegativeCnt += ncccNegativeCnt;
		totOnusPositiveAmt += onusPositiveAmt;
		totOnusPositiveCnt += onusPositiveCnt;
		totOnusNegativeAmt += onusNegativeAmt;
		totOnusNegativeCnt += onusNegativeCnt;
		totIntlPositiveAmt += intlPositiveAmt;
		totIntlPositiveCnt += intlPositiveCnt;
		totIntlNegativeAmt += intlNegativeAmt;
		totIntlNegativeCnt += intlNegativeCnt;
		totNatPositiveAmt += natPositiveAmt;
		totNatPositiveCnt += natPositiveCnt;
		totNatNegativeAmt += natNegativeAmt;
		totNatNegativeCnt += natNegativeCnt;

	}

	void deleteExistRptRecord() throws Exception {
		
		daoTable  = " mis_report_data ";
		whereStr  = " where 1=1 "; 
		whereStr += " and data_month = ? ";
		whereStr += " and data_from = 'CRD50V' ";
		
		setString(1, comc.getSubString(hBusinessDate,0,6));

		deleteTable();
		
	}

	void insertMisReportData() throws Exception {
		
		//重跑時要先刪除上一次產生的資料
		deleteExistRptRecord();
		commitDataBase();
		
	    setValue("DATA_MONTH", comc.getSubString(hBusinessDate,0,6));
	    setValue("DATA_FROM", "CRD50V");
	    setValue("DATA_DATE", hBusinessDate);
	    setValueDouble("SUM_FIELD1", totMonthAmt); //年度一般消費
	    setValueDouble("SUM_FIELD2", 0);
	    setValueDouble("SUM_FIELD3", 0);
	    setValueDouble("SUM_FIELD4", 0);
	    setValueDouble("SUM_FIELD5", 0);
	    setValue("DATA_CONTENT", rptData.toString());
	    setValue("MOD_TIME", sysDate+sysTime);
	    setValue("MOD_PGM", javaProgram);

	    daoTable = "mis_report_data";
	    insertTable();

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

		BilRD50V proc = new BilRD50V();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
