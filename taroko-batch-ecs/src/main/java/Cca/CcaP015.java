/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  111-01-22  V1.00.00   Alex       initial                                  *
*  111/02/14  V1.00.01    Ryan      big5 to MS950                                           * 
*****************************************************************************/
package Cca;

import java.text.NumberFormat;

import com.BaseBatch;
import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CcaP015 extends BaseBatch {
	
	private final String progname = "產生授權交易記錄檔-fallback使用 111/02/14  V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	private int ilFileNum;
	String prgmId = "CcaP015";
	String fileName = "";
	String temstr = "";
	String hCallBatchSeqno = "";
	String hCardNo = "";
	String hUserExpireDate = "";
	String hTxDate = "";
	String hTxTime = "";
	String hTxGmtDate = "";
	String hTxGmtTime = "";
	Double hNtAmt = 0.0;
	Double hOriAmt = 0.0;
	String hTxCurrency = "";
	String hBankCountry = "";
	String hBankCountryNum = "";
	String hStandIn = "";
	String hMchtNo = "";
	String hMccCode = "";
	String hAuthStatusCode = "";
	String hAuthNo = "";
	String hTraceNo = "";
	String hRefNo = "";
	String hPoMode = "";
	String hAuthSource = "";
	String hTxnIdf = "";
	String hVCardNo = "";
	String hIsoRespCode = "";
	String hChgDate = "";
	String hChgTime = "";
	String hAuthType = "";
	String hLastCard = "";
	Double hSumAmt = 0.0;
	Double hSumCash = 0.0;
	String hAcnoPSeqno = "";
	String hDebitFlag = "";
	String hVdcardFlag = "";
	String hLastDebit = "";
	String hProcCode = "";
	String hTransCode = "";
	Double hCardAcctIdx = 0.0;
	String hTransType = "";
	String hOriAuthSeqno = "";
	String hReversalFlag = "";
	String hCacuAmount = "";
	String hCacuCash = "";	
	//--	
	String hCorpPSeqno = "";
	String hAcctType = "";
	String hCorpAcnoPSeqno = "";
	Double hCorpCardAcctIdx = 0.0;	
	String hLastAcctType = "";
	String hLastCorpPSeqno = "";
	String hOriTxnIdf = "";
	boolean ibReversal = false ;
	boolean ibExclude = false ;
	
	public static void main(String[] args) {
		CcaP015 proc = new CcaP015();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 2) {
			printf("Usage : Ccap015 [exclude trans_type 0120 Y/N] [business_date]");
			errExit(1);
		}

		dbConnect();
		
		//--參數 1 排除代行 , 若為 Y 則排除代行 ; 參數 2 為指定日期 , 若為空值則以系統日執行
		if(liArg == 1) {
			if("Y".equals(args[0]))
				ibExclude = true;
			else
				ibExclude = false;
			
			hBusiDate = sysDate ;
		}	else if(liArg == 2) {
			if("Y".equals(args[0]))
				ibExclude = true;
			else
				ibExclude = false;
			
			setBusiDate(args[1]);
		}	else	{
			hBusiDate = sysDate ;
		}
		
		fileName = "AUTHLOG_" + hBusiDate + ".TXT";
		checkOpen();
		selectAuthTxlog();
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		endProgram();
	}
	
	void checkOpen() throws Exception {
		temstr = String.format("%s/media/cca/%s", comc.getECSHOME(), fileName);
		ilFileNum = openOutputText(temstr, "MS950");
		if (ilFileNum < 0) {
			printf("開啟檔案失敗 ! ");
			errExit(1);
		}
	}
	
	void selectAuthTxlog() throws Exception {
		
		//--不含沖正當日授權交易 , 若當日授權交易有沖正時則在原交易的更正日期時間有值 , 沖正交易不寫入只寫入原交易
		//--含沖正非當日授權交易 , 資料內容同原交易僅更正日期時間不同 , 授權來源碼為 R
		//--logic_del=B 退貨交易不寫授權交易記錄檔 , 等請款資料來時才還額
		
		sqlCmd = " select ";
		sqlCmd += " card_no , user_expire_date , tx_date , tx_time , ";
		sqlCmd += " to_char(tx_datetime,'yyyymmdd') as tx_gmt_date , to_char(tx_datetime,'hh24miss') as tx_gmt_time , ";
		sqlCmd += " decode(chg_date,'','0000',chg_date) as chg_date , decode(chg_time,'','000000',chg_time) as chg_time ,";
		sqlCmd += " nt_amt , ori_amt , tx_currency , bank_country , stand_in , mcht_no , mcc_code , ";
		sqlCmd += " auth_status_code , auth_no , trace_no , ref_no , pos_mode , auth_source , ori_auth_seqno , ";
		sqlCmd += " txn_idf , v_card_no , iso_resp_code , auth_type , vdcard_flag , proc_code , trans_code , trans_type , reversal_flag , ";
		sqlCmd += " cacu_amount , cacu_cash , corp_p_seqno , acct_type , ori_txn_idf ";
		sqlCmd += " from cca_auth_txlog where 1=1 and tx_date = ? and logic_del <> 'B' and mod_pgm not like 'Cnv%'  ";
		
		if(ibExclude) {
			sqlCmd += " and trans_type <> '0120' ";
		}
		
		sqlCmd += " order by card_no ";
		setString(1, hBusiDate);

		openCursor();

		while (fetchTable()) {
			initData();
			totalCnt++;
			hCardNo = getValue("card_no");
			hUserExpireDate = getValue("user_expire_date");
			hTxDate = getValue("tx_date");
			hTxTime = getValue("tx_time");
			hTxGmtDate = getValue("tx_gmt_date");
			hTxGmtTime = getValue("tx_gmt_time");
			hChgDate = getValue("chg_date");
			hChgTime = getValue("chg_time");
			hNtAmt = getValueDouble("nt_amt");
			hOriAmt = getValueDouble("ori_amt");
			hTxCurrency = getValue("tx_currency");
			hBankCountry = getValue("bank_country");
			hStandIn = getValue("stand_in");
			hMchtNo = getValue("mcht_no");
			hMccCode = getValue("mcc_code");
			hAuthStatusCode = getValue("auth_status_code");
			hIsoRespCode = getValue("iso_resp_code");
			hAuthNo = getValue("auth_no");
			hTraceNo = getValue("trace_no");
			hRefNo = getValue("ref_no");
			hPoMode = getValue("pos_mode");
			hAuthSource = getValue("auth_source");
			hTxnIdf = getValue("txn_idf");
			hVCardNo = getValue("v_card_no");
			hAuthType = getValue("auth_type");
			hVdcardFlag = getValue("vdcard_flag");
			hProcCode = getValue("proc_code");
			hTransCode = getValue("trans_code");
			hTransType = getValue("trans_type");
			hOriAuthSeqno = getValue("ori_auth_seqno");
			hReversalFlag = getValue("reversal_flag");
			hCacuAmount = getValue("cacu_amount");
			hCacuCash = getValue("cacu_cash");
			hCorpPSeqno = getValue("corp_p_seqno");
			hAcctType = getValue("acct_type");
			hOriTxnIdf = getValue("ori_txn_idf");
			if (eqIgno(hVdcardFlag, "D")) {
				hDebitFlag = "Y";
			} else {
				hDebitFlag = "N";
			}			
			//--trans_type = 0420 為沖正交易 
			if(hTransType.equals("0420") && selectOriTxn()==false) continue;
			
			hBankCountryNum = getBankCountryNum(hBankCountry);
			writeText();			

		}
		closeCursor();				
		closeOutputText(ilFileNum);						
	}
	
	void initData() {
		hCardNo = "";
		hUserExpireDate = "";
		hTxDate = "";
		hTxTime = "";
		hTxGmtDate = "";
		hTxGmtTime = "";
		hNtAmt = 0.0;
		hOriAmt = 0.0;
		hTxCurrency = "";
		hBankCountry = "";
		hStandIn = "";
		hMchtNo = "";
		hMccCode = "";
		hAuthStatusCode = "";
		hAuthNo = "";
		hTraceNo = "";
		hRefNo = "";
		hPoMode = "";
		hAuthSource = "";
		hTxnIdf = "";
		hVCardNo = "";
		hIsoRespCode = "";
		hChgDate = "";
		hChgTime = "";
		hAuthType = "";
		hDebitFlag = "";
		hVdcardFlag = "";
		hProcCode = "";
		hBankCountryNum = "";
		hCardAcctIdx = 0.0;
		hTransCode = "";
		hTransType = "";
		hOriAuthSeqno = "";
		hReversalFlag = "";
		ibReversal = false ;		
		hCorpPSeqno = "";
		hAcctType = "";
	}

	String double2String(Double ldAmount) {

		Double doubleObj = new Double(ldAmount);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		String doubleString = nf.format(doubleObj);

		return doubleString;
	}
	
	boolean selectOriTxn() throws Exception {
		//--reversal_flag = 'Y' 才有比對到
		if(hReversalFlag.equals("Y")==false)	return false;
		if(hOriAuthSeqno.isEmpty())	return false ;
		
		//--不含沖正當日授權交易 , 若當日授權交易有沖正時則在原交易的更正日期時間有值 , 沖正交易不寫入只寫入原交易
		//--含沖正非當日授權交易 , 資料內容同原交易僅更正日期時間不同 , 授權來源碼為 R
		
		String sql1 = "";
		sql1 = " select ";
		sql1 += " card_no , user_expire_date , tx_date , tx_time , ";
		sql1 += " to_char(tx_datetime,'yyyymmdd') as tx_gmt_date , to_char(tx_datetime,'hh24miss') as tx_gmt_time , ";
		sql1 += " decode(chg_date,'','0000',chg_date) as chg_date , decode(chg_time,'','000000',chg_time) as chg_time ,";
		sql1 += " nt_amt , ori_amt , tx_currency , bank_country , stand_in , mcht_no , mcc_code , ";
		sql1 += " auth_status_code , auth_no , trace_no , ref_no , pos_mode , auth_source , ori_auth_seqno , ";
		sql1 += " txn_idf , v_card_no , iso_resp_code , auth_type , vdcard_flag , proc_code , trans_code , trans_type ";
		sql1 += " from cca_auth_txlog where 1=1 and auth_seqno = ? ";		
		
		sqlSelect(sql1,new Object[] {hOriAuthSeqno});
		
		if(sqlNrow<=0)	return false ;
		
		if(colEq("tx_date",hTxDate))	return false ;
		
		hCardNo = colSs("card_no");
		hUserExpireDate = colSs("user_expire_date");
		hTxDate = colSs("tx_date");
		hTxTime = colSs("tx_time");
		hTxGmtDate = colSs("tx_gmt_date");
		hTxGmtTime = colSs("tx_gmt_time");
		hChgDate = colSs("chg_date");
		hChgTime = colSs("chg_time");
		hNtAmt = colNum("nt_amt");
		hOriAmt = colNum("ori_amt");
		hTxCurrency = colSs("tx_currency");
		hBankCountry = colSs("bank_country");
		hStandIn = colSs("stand_in");
		hMchtNo = colSs("mcht_no");
		hMccCode = colSs("mcc_code");
		hAuthStatusCode = colSs("auth_status_code");
		hIsoRespCode = colSs("iso_resp_code");
		hAuthNo = colSs("auth_no");
		hTraceNo = colSs("trace_no");
		hRefNo = colSs("ref_no");
		hPoMode = colSs("pos_mode");
		hAuthSource = "R";
		hTxnIdf = colSs("txn_idf");
		hVCardNo = colSs("v_card_no");
		hAuthType = colSs("auth_type");
		hVdcardFlag = colSs("vdcard_flag");
		hProcCode = colSs("proc_code");
		hTransCode = colSs("trans_code");
		hTransType = colSs("trans_type");
		hOriAuthSeqno = colSs("ori_auth_seqno");
		ibReversal = true;
		
		return true ;
	}
	
	String getBankCountryNum(String BankCode) throws Exception {
		if (BankCode.isEmpty())
			return "";
		String sql1 = "select country_no from cca_country where ? in (country_code,bin_country) ";
		sqlSelect(sql1, new Object[] { BankCode });

		if (sqlNrow > 0) {
			return colSs("country_no");
		}

		return BankCode;
	}
	
	void writeText() throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String tempStr = "", newLine = "\r\n";
		tempBuf.append(comc.fixLeft(hCardNo, 19)); // --含預留銀聯卡卡號長度 共19
		if (hUserExpireDate.length() == 4) { // --目前資料庫此欄位有 4 碼 和 6 碼 , 檔案需要 MMYY
			tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 2, 2), 2));
			tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 0, 2), 2));
//      tempBuf.append(comc.fixLeft(hUserExpireDate,4));
		} else if (hUserExpireDate.length() == 6) {
			tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 4, 2), 2));
			tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 2, 2), 2));
//      tempBuf.append(comc.fixLeft(commString.bbMid(hUserExpireDate, 2, 4),4));
		} else {
			tempBuf.append(comc.fixLeft(" ", 4));
		}
		tempBuf.append(comc.fixLeft(hTxDate, 8));
		tempBuf.append(comc.fixLeft(hTxTime, 6));
		tempBuf.append(comc.fixLeft(commString.bbMid(hTxGmtDate, 2, 6), 6));
		tempBuf.append(comc.fixLeft(hTxGmtTime, 6));
		tempStr = double2String(hNtAmt * 100);
		tempBuf.append(comc.fixLeft(commString.lpad(tempStr, 12, "0"), 12));
		// tempBuf.append(commString.bb_fixlen(tempStr, 10));
		tempStr = "";
		tempStr = double2String(hOriAmt * 100);
		tempBuf.append(comc.fixLeft(commString.lpad(tempStr, 12, "0"), 12));
		// tempBuf.append(commString.bb_fixlen(tempStr, 10));
		tempStr = "";
		tempBuf.append(comc.fixLeft(hTxCurrency, 3));
		tempBuf.append(comc.fixLeft(hBankCountryNum, 3));
		tempBuf.append(comc.fixLeft(hStandIn, 11));
		tempBuf.append(comc.fixLeft(hMchtNo, 15));
		tempBuf.append(comc.fixLeft(hMccCode, 4));
		tempBuf.append(comc.fixLeft("00" + hIsoRespCode, 4));
//    tempBuf.append(comc.fixLeft(hIsoRespCode + hAuthStatusCode, 4));
		tempBuf.append(comc.fixLeft(hAuthNo, 6));
		tempBuf.append(comc.fixLeft(hTraceNo, 6));
		tempBuf.append(comc.fixLeft(hRefNo, 12));
		if (hAuthType.equals("Z")) {
			// --Auth_type :Z 為人工授權交易
			tempBuf.append(comc.fixLeft("Y", 1));
		} else {
			tempBuf.append(comc.fixLeft(" ", 1));
		}
		tempBuf.append(comc.fixLeft(hChgDate, 4));
		tempBuf.append(comc.fixLeft(hChgTime, 6));
		tempBuf.append(comc.fixLeft(hPoMode, 2));
		tempBuf.append(comc.fixLeft(hAuthSource, 1));
		tempBuf.append(comc.fixLeft(hTxnIdf, 15)); // --交易識別碼 , 此欄位待確認
		tempBuf.append(comc.fixLeft(hVCardNo, 19)); // --對應卡片號碼 , 此欄位待確認
		tempBuf.append(comc.fixLeft(" ", 88)); // --保留欄位
		tempBuf.append(comc.fixLeft(hOriTxnIdf, 15)); //--原始交易識別碼
		tempBuf.append(comc.fixLeft(hVCardNo, 19));
		tempBuf.append(comc.fixLeft(" ", 87)); // --保留欄位
		tempBuf.append(newLine);
		if (!writeTextFile(ilFileNum, tempBuf.toString())) {
			printf("寫入檔案失敗 !");
			errExit(1);
		}
	}
	
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/cca", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileName);
		}
	}
	
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
	
}
