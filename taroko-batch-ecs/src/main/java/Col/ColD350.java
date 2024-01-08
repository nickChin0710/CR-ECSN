/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/09/25  V1.00.00    phopho     program initial                          *
*  109/12/16  V1.00.01    shiyuqi       updated for project coding standard   *
*  112/10/22  V1.00.03    sunny      調整產生檔案斷行處理&檔案傳送到卡部目錄                        *
*  112/11/13  V1.00.04    sunny      增加寫入到ptr_batch_rpt報表可線上查詢                    *
******************************************************************************/

package Col;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class ColD350 extends AccessDAO {
	private String progname = "已轉催呆戶尚有未轉催呆之科目報表  112/11/13  V1.00.04 ";
	private static final String CRDATACREA = "CRDATACREA";
	private static final String DATA_FOLDER = "/media/col/";
	private static final String DATA_FORM = "COL_D350";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String prgmId = "ColD350";
	String rptName1 = "已轉催呆戶尚有未轉催呆之科目報表";
	int recordCnt = 0;
	int actCnt = 0;
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String errCode = "";
	String errDesc = "";
	String procDesc = "";
	int rptSeq1 = 0;
	int errCnt = 0;
	String errMsg = "";
	String buf = "";
	String szTmp = "";
	String stderr = "";
	long hModSeqno = 0;
	String ecsServer = "";
	String hModUser = "";
	String hModTime = "";
	String hModPgm = "";
	String hModWs = "";
	String hModLog = "";
	String hCallBatchSeqno = "";
	String iFileName = "";
	String iPostDate = "";

	String hBusiBusinessDate = "";
	String hAcnoAcctStatus = "";
	String hDebtAcctMonth = "";
	String hDebtAcctType = "";
	String hDebtAcctKey = "";
	String hDebtAcctItemEname = "";
	String hPcodChiShortName = "";
	String hDebtItemPostDate = "";
	String hDebtCurrCode = "";
	long hDebtEndBal = 0;
	long hDebtDcEndBal = 0;

	long totalCnt = 0;
	String tmpstr = "";
	String temstr = "";
	String temstr1 = "";
	String hTempAcctStatus = "";
	int printCnt = 0;
	PrtBuf prtData = new PrtBuf();
	
	private int fptr1 = 0;

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
            	exceptExit = 0;
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length > 1) {
                comc.errExit("Usage : ColD350 [business_date/batch_seqno]", "");
            }

			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connectDataBaseerror", "");
			}
			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hBusiBusinessDate = "";
            if ((args.length >= 1) && (args[0].length() == 8))
                hBusiBusinessDate = args[0];
            selectPtrBusinday();
            
            deletePtrBatchRpt();
			
            showLogMessage("I", "", "==============================================");
			showLogMessage("I", "", "開始已轉催呆戶尚有未轉催呆之科目報表");
			totalCnt = 0;
			openFiles();
			writeTextFile(fptr1, String.format("%s","報表名稱：" + rptName1 + "\n"));
			buf = String.format(rptName1);
			lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));
			buf = String.format("程式名稱：ColD350   產生日期:%s", hBusiBusinessDate);
			lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));	
//			writeTextFile(fptr1, String.format("%s",buf + "\n"));
//			writeTextFile(fptr1, String.format("%s","說明：1. 帳戶狀態 3為催收戶，4為呆帳戶；幣別狀態 901為台幣 840為美金 392為日幣"+ "\n"));
//			writeTextFile(fptr1, String.format("%s","      2. 催收戶排除科目:CB、CI、CC、AI、AF、CF、PF、DP"+ "\n"));
//			writeTextFile(fptr1, String.format("%s","      3. 呆帳戶排除科目:DB、AI、AF、CF、PF、DP、SF"+ "\n"));
            buf = String.format("說明：1. 帳戶狀態 3為催收戶，4為呆帳戶；幣別狀態 901為台幣 840為美金 392為日幣"+ "\n");
			writeTextFile(fptr1, String.format("%s",buf));
			lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));	
			buf = String.format("%s","      2. 催收戶排除科目:CB、CI、CC、AI、DP"+ "\n");
			writeTextFile(fptr1, String.format("%s",buf));
			lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));	
			buf = String.format("%s","      3. 呆帳戶排除科目:DB、AI、DP"+ "\n");
			writeTextFile(fptr1, String.format("%s",buf));
			lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));	
			
			buf = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", "帳戶狀態",
					"帳務年月", "帳戶類別", "身份證字號", "科目代號", "科目中文名稱", "入帳日期", "幣別",
					"台幣期末金額", "原幣期末金額"+ "\n");
			writeTextFile(fptr1, String.format("%s",buf+ "\n"));		
			lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));
			selectOutfile();	
			closeFiles();
			comcr.insertPtrBatchRpt(lpar1); /* 寫入ptr_batch_rpt online報表 */
			showLogMessage("I", "", "     累計產生 [" + totalCnt + "] 筆");
			
			// ==============================================
			// 複製檔案-傳送到卡部目錄
			String datFileName = String.format("%s_%s.csv", DATA_FORM, hBusiBusinessDate);
            String fileFolder = Paths.get(comc.getECSHOME(), DATA_FOLDER).toString();

			// 產生檔案名稱
				procFTP(datFileName, fileFolder); 
			  
			// ==============================================
			// 固定要做的
			comcr.callbatch(1, 0, 0);
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
		sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
	}

	/***********************************************************************/
	void openFiles() throws Exception {
		//temstr1 = String.format("%s/reports/COL_D350_%s.csv", comc.getECSHOME(), hBusiBusinessDate.substring(6));
		temstr1 = String.format("%s/media/col/COL_D350_%s.csv", comc.getECSHOME(), hBusiBusinessDate.substring(0));
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        fptr1 = openOutputText(temstr1, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }
	}

	/***********************************************************************/
	void selectOutfile() throws Exception {

		
		sqlCmd = "select * FROM ( ";//第一段(催收)
		sqlCmd += "select ";
		sqlCmd += "b.acct_status, ";
		sqlCmd += "a.acct_month, ";
		sqlCmd += "a.acct_type, ";
//		sqlCmd += "b.acct_key, ";
		sqlCmd += "d.id_no as acct_key, ";
		sqlCmd += "a.acct_code, ";
		sqlCmd += "c.chi_short_name, ";
		sqlCmd += "a.post_date, ";
		sqlCmd += "a.curr_code, ";
		sqlCmd += "a.end_bal, ";
		sqlCmd += "a.dc_end_bal ";
		sqlCmd += "from act_debt a, act_acno b, ptr_actcode c,crd_idno d ";
//		sqlCmd += "where a.p_seqno=b.p_seqno ";
		sqlCmd += "where a.p_seqno = b.acno_p_seqno ";
		sqlCmd += "and b.id_p_seqno = d.id_p_seqno ";
		sqlCmd += "and a.acct_code = c.acct_code ";
//		sqlCmd += "and a.acct_code not in ('CB','CI','CC','AI','AF','CF','PF','DP') ";
		sqlCmd += "and a.acct_code not in ('CB','CI','CC','AI','DP') ";
		sqlCmd += "and a.end_bal>0 ";
		sqlCmd += "and b.acct_status ='3' ";
		sqlCmd += "union ";//第一段(呆帳)
		sqlCmd += "select b.acct_status, ";
		sqlCmd += "a.acct_month, ";
		sqlCmd += "a.acct_type, ";
//		sqlCmd += "b.acct_key, ";
		sqlCmd += "d.id_no as acct_key, ";
		sqlCmd += "a.acct_code, ";
		sqlCmd += "c.chi_short_name, ";
		sqlCmd += "a.post_date, ";
		sqlCmd += "a.curr_code, ";
		sqlCmd += "a.end_bal, ";
		sqlCmd += "a.dc_end_bal ";
		sqlCmd += "from act_debt a, act_acno b, ptr_actcode c,crd_idno d ";
//		sqlCmd += "where a.p_seqno=b.p_seqno ";
		sqlCmd += "where a.p_seqno = b.acno_p_seqno ";
		sqlCmd += "and a.acct_code = c.acct_code ";
		sqlCmd += "and b.id_p_seqno = d.id_p_seqno ";
		//sqlCmd += "and a.acct_code not in ('DB','AI','AF','CF','PF','DP') ";
		sqlCmd += "and a.acct_code not in ('DB','AI','DP') "; //排除法訴費
		sqlCmd += "and a.end_bal>0 ";
		sqlCmd += "and b.acct_status='4' ";
		sqlCmd += ")order by acct_code ";
		
		openCursor();
        while (fetchTable()) {
			hAcnoAcctStatus = getValue("acct_status");
			hDebtAcctMonth = getValue("acct_month");
			hDebtAcctType = getValue("acct_type");
			hDebtAcctKey = getValue("acct_key");
			hDebtAcctItemEname = getValue("acct_code");
			hPcodChiShortName = getValue("chi_short_name");
			hDebtItemPostDate = getValue("post_date");
			hDebtCurrCode = getValue("curr_code");
			hDebtEndBal = getValueLong("end_bal");
			hDebtDcEndBal = getValueLong("dc_end_bal");

			genFile();

			totalCnt++;
			if (totalCnt % 1000 == 0)
				showLogMessage("I", "", "    目前處理筆數 [" + totalCnt + "]");

		}
        closeCursor();
	}

	/***********************************************************************/
	void genFile() throws Exception {

		prtData.acctStatus = hAcnoAcctStatus;
		prtData.acctMonth = hDebtAcctMonth;
		prtData.acctType = hDebtAcctType;
		prtData.acctKey = hDebtAcctKey;
		prtData.acctItemEname = hDebtAcctItemEname;
		prtData.chiShortName = hPcodChiShortName;
		prtData.itemPostDate = hDebtItemPostDate;
		prtData.currCode = hDebtCurrCode;
		prtData.endBal = String.format("%d", hDebtEndBal);
		prtData.dcEndBal = String.format("%d", hDebtDcEndBal);
		
//		/*新增*/
//		buf = String.format("%6s,%8s,%4s,%12s,%4s,%42s,%10s,%5s,%11s,%11s", 
//				prtData.acctStatus,
//				prtData.acctMonth, prtData.acctType, prtData.acctKey, prtData.acctItemEname, 
//				prtData.chiShortName, prtData.itemPostDate, prtData.currCode,
//				prtData.endBal, prtData.dcEndBal + "\n");
//		
//		lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));

		
		tmpstr = prtData.allText();
		
		writeTextFile(fptr1, String.format("%s",tmpstr));
		writeTextFile(fptr1, String.format("\n"));		

		buf = String.format("%s",tmpstr+"\n");		
		lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));
		
		printCnt++;
	}

	/***********************************************************************/
	void closeFiles() throws Exception {
		if (printCnt == 0) {
			buf = "本日無資料";
			showLogMessage("I", "", buf);			
			lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));
		}
		buf = " 本次產生 [" + printCnt + "] 筆";
		lpar1.add(comcr.putReport(prgmId, rptName1, sysDate, ++rptSeq1, "0", buf));
		closeOutputText(fptr1);
		showLogMessage("I", "", buf);		
		
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ColD350 proc = new ColD350();
		int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
	}

	/***********************************************************************/
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
	/***********************************************************************/
	 int deletePtrBatchRpt() throws Exception
	 {
	  daoTable  = "ptr_batch_rpt";
	  whereStr  = "where program_code like 'ColD350%' "
	            + "and   start_date = ? ";

	  setString(1 , sysDate);

	  int recCnt = deleteTable();

	  showLogMessage("I","","delete ptr_batch_rpt cnt :["+ recCnt +"]");

	  return(0);
	 }		
	/***********************************************************************/
	class PrtBuf {
		String acctStatus;
		String acctMonth;
		String acctType;
		String acctKey;
		String acctItemEname;
		String chiShortName;
		String itemPostDate;
		String currCode;
		String endBal;
		String dcEndBal;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixRight(acctStatus, 5)+",";
			rtn += fixRight(acctMonth, 7)+",";
			rtn += fixRight(acctType, 3)+",";
			rtn += fixLeft(acctKey, 12)+",";
			rtn += fixRight(acctItemEname, 3)+",";
			rtn += fixLeft(chiShortName, 21)+",";
			rtn += fixRight(itemPostDate, 9)+",";
			rtn += fixRight(currCode, 4)+",";
			rtn += fixRight(endBal, 15)+",";
			rtn += fixRight(dcEndBal, 15);
						
			return rtn;
		}

		String fixRight(String str, int len)
				throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = spc + str;
			byte[] bytes = str.getBytes("MS950");
			int offset = bytes.length - len;
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, offset, vResult, 0, len);
			return new String(vResult, "MS950");
		}
		
		/***********************************************************************/
		 String fixLeft(String str, int len) throws UnsupportedEncodingException 
		  {
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
		 
		 /***********************************************************************/		
	}

}
