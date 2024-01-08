/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  111-03-03  V1.00.05   Alex       bug fix                                  *
*  111/02/14  V1.00.04   Ryan      big5 to MS950                            *
*  111-01-17  V1.00.03   Alex       找不到ID時出報表                                                                           *
*  110-02-20  V1.00.02   Alex       bug fix									 *
*  110-02-17  V1.00.01   Alex       program initial                          * 
*****************************************************************************/

package Rsk;

import com.CommCrd;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommString;

public class RskP130 extends BaseBatch {

	private final String progname = "停卡後額度檢視降額 111/03/03 V1.00.05";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	String fileName = "";
	private int iiFileNum = 0;
	String hAcctType = "";
	String hAcctKey = "";
	String hReasonUp = "";
	String hReasonDown = "";
	String hSmsFlag = "";
	double hAftAmt = 0;
	double hBefAmt = 0;
	String hAcnoPSeqno = "";
	String hIdPSeqno = "";
	String hIdNo = "";
	String hBlockReason1 = "";
	String hBlockReason2 = "";
	String hBlockReason3 = "";
	String hBlockReason4 = "";
	String hBlockReason5 = "";
	String hAdjDate1 = "";
	String hAdjDate2 = "";
	double hTempAdjAmt = 0;
	String hAdjLocFlag = "";
	double hCashRate = 0;
	double hMaxCashAmt = 0;
	double hAftCash = 0;
	double hBefCash = 0;
	String hFhFlag = "";
	int hCardAcctIdx = 0;
	double hCardPct = 0;
	double hAdjPct = 0;
	String hAdjArea = "";
	String hAdjQuota = "";
	String hAdjReason = "";
	String hAdjRemark = "";
	String hTempAdjDate = "";
	String hCellarPhone = "";
	String hMsgSeqno = "";
	String hMsgDept = "";
	String hMsgUserid = "";
	String hMsgPgm = "";
	String hMsgId = "";
	String hChiName = "";
	String hSonCardNo = "";
	String fileOutName = "";
	int fileOut = 0 ;
	int totalError = 0 ;
	int writeCnt = 0;
	double hSonIndivCrdLmt = 0;
	double hSonIndivInstLmt = 0;
	boolean ibCashAdj = true;
	boolean ibWrite = false ;
	String oriId = "";
	
	private int tiLimitlog = -1;
	private int tiCreditlog = -1;
	private int tiRskAcnoLogSon = -1;

	public static void main(String[] args) {
		RskP130 proc = new RskP130();
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP130 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (liArg == 0) {
			//--因 TCB排程 CCR111FD, CCR300FD 有時候會跑到3點多會超過ECS更換營業日的排程
			//--所以將此程式排程安排在更換營業日後 , 在此需要 -1 天			
			hBusiDate = commDate.dateAdd(hBusiDate, 0, 0, -1);
		}		

		dateTime();

		fileName = "CUST_CRLIMIT_" + hBusiDate + ".TXT";
		fileOutName = "CUST_CRLIMIT_" +hBusiDate + "_ERROR.TXT";
		
		checkOpen();
		processData();
		
		//--FTP 錯誤檔給卡部
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		//--放入backup
		renameFile();									
		endProgram();
	}
	
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileOutName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fileOutName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileOutName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileOutName);
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
	
	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
		
		String tmpstr3 = String.format("%s/media/rsk/%s", getEcsHome(), fileOutName);
		String tmpstr4 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileOutName);
		
		if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileOutName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileOutName + "] 已移至 [" + tmpstr4 + "]");		
		
	}
	
	void processData() throws Exception {

		while (true) {
			String fileData = readTextFile(iiFileNum);
			if (endFile[iiFileNum].equals("Y")) {
				break;
			}

			if (empty(fileData))
				break;
			totalCnt++;
			initData();			
			splitData(fileData);

			selectAcnoData();
			
			if(ibWrite) {
				//--查無此ID資料 出表並中斷調整
				writeFile();
				continue ;
			}
			
			calculateCash();
			updateActAcno();
			selectCcaCardAcctBlockReason();
			selectCrdCorrelate();
			insertRskAcnoLog();
			//--判斷是否有臨調
			if (selectCcaCardAcctAdj() == true) {
				changeCcaCardAcct();
			}
			//--處理子卡
			procSonCard();
		}
		
		closeInputText(iiFileNum);
		closeOutputText(fileOut);
		
	}
	
	void writeFile() throws Exception {
		String outData = "";
		if(writeCnt ==0)
			writeTitle();
		
		outData = comc.fixLeft(oriId, 11);
		outData += commString.space(3);
		outData += "not found !!";
		outData += newLine;
		writeTextFile(fileOut, outData);
		writeCnt ++ ;
		
	}
	
	void writeTitle() throws Exception {
		String titleItem = "";
		titleItem = "身分證" + commString.space(10);
		titleItem += "錯誤原因" + commString.space(10);		
		titleItem += newLine;
		writeTextFile(fileOut, titleItem);
		titleItem = commString.repeat("=", 60);
		titleItem += newLine;
		writeTextFile(fileOut, titleItem);
	}
	
	void procSonCard() throws Exception {
		
		sqlCmd = " select card_no , indiv_crd_lmt , indiv_inst_lmt from crd_card where current_code ='0' and son_card_flag = 'Y' and indiv_crd_lmt >= ? and acno_p_seqno = ? ";
		setDouble(1,hAftAmt);
		setString(2,hAcnoPSeqno);
		
		int readCnt = selectTable();
		if(readCnt <=0)
			return ;
		
		for(int ii = 0; ii < readCnt ; ii++) {
			hSonCardNo = colSs(ii,"card_no");
			hSonIndivCrdLmt = colNum(ii,"indiv_crd_lmt");
			hSonIndivInstLmt = colNum(ii,"indiv_inst_lmt");
			updateCrdCardForSonCard();
			updateCrdBaseForSonCard();
			insertRskAcnoLogForSonCard();
		}
		
		
	}
	
	void insertRskAcnoLogForSonCard() throws Exception {
		if (tiRskAcnoLogSon <= 0) {
			sqlCmd = " insert into rsk_acnolog ( ";
			sqlCmd += " kind_flag , ";
			sqlCmd += " card_no , ";
			sqlCmd += " acno_p_seqno , ";
			sqlCmd += " acct_type , ";
			sqlCmd += " id_p_seqno , ";			
			sqlCmd += " log_date , ";
			sqlCmd += " log_mode , ";
			sqlCmd += " log_type , ";
			sqlCmd += " log_reason , ";
			sqlCmd += " bef_loc_amt , ";
			sqlCmd += " aft_loc_amt , ";
			sqlCmd += " bef_loc_cash , ";
			sqlCmd += " aft_loc_cash , ";
			sqlCmd += " adj_loc_flag , ";
			sqlCmd += " emend_type , ";
			sqlCmd += " sms_flag , ";
			sqlCmd += " card_adj_limit , ";
			sqlCmd += " card_adj_date1 , ";
			sqlCmd += " card_adj_date2 , ";
			sqlCmd += " user_dept_no , ";
			sqlCmd += " son_card_flag , ";
			sqlCmd += " apr_flag , ";
			sqlCmd += " apr_user , ";
			sqlCmd += " apr_date , ";
			sqlCmd += " mod_user , ";
			sqlCmd += " mod_time , ";
			sqlCmd += " mod_pgm , ";
			sqlCmd += " mod_seqno ";
			sqlCmd += " ) values ( ";
			sqlCmd += " 'C', ";
			sqlCmd += " ? , ";
			sqlCmd += " ? ,  ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";			
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " '2' , ";
			sqlCmd += " '1' , ";
			sqlCmd += " 'J' , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " '2' , ";
			sqlCmd += " '4' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " 0 , ";
			sqlCmd += " '' , ";
			sqlCmd += " '' , ";
			sqlCmd += " '' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " 'Y' , ";
			sqlCmd += " 'system' , ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " 'batch' , ";
			sqlCmd += " sysdate , ";
			sqlCmd += " 'RskP130' , ";
			sqlCmd += " 1 ";
			sqlCmd += " ) ";

			daoTable = "rsk_acno_log-A";
			tiRskAcnoLogSon = ppStmtCrt("", "");
		}
		
		setString(1,hSonCardNo);
		setString(2,hAcnoPSeqno);
		setString(3,hAcctType);
		setString(4,hIdPSeqno);
		setDouble(5,hSonIndivCrdLmt);
		setDouble(6,hAftAmt);
		setDouble(7,hSonIndivInstLmt);
		setDouble(8,hSonIndivInstLmt);
		
		sqlExec(tiRskAcnoLogSon);
		if (sqlNrow <= 0) {
			errmsg("insert cca_credit_log error ");
			errExit(1);
		}
	}
	
	void updateCrdBaseForSonCard() throws Exception {
		int updateCnt = 0;

		daoTable = "cca_card_base";
		updateSQL += " card_adj_limit = 0 , ";
		updateSQL += " card_adj_date1 = '' , ";		
		updateSQL += " card_adj_date2 = '' , ";
		updateSQL += " adj_chg_user = 'RskP130' , ";
		updateSQL += " mod_user = 'system' , mod_time = sysdate , mod_pgm = 'RskP130' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where card_no = ? ";
			
		setString(1,hSonCardNo);

		updateCnt = updateTable();

		if (updateCnt == 0) {
			errmsg("update_cca_card_base error, kk=[%s] , id=[%s]", hSonCardNo,hIdNo);
			errExit(1);
		}
	}
	
	void updateCrdCardForSonCard() throws Exception {
		int updateCnt = 0;

		daoTable = "crd_card";
		updateSQL += " son_card_flag = 'N' , ";
		updateSQL += " indiv_crd_lmt = 0 , ";		
		updateSQL += " mod_user = 'system' , mod_time = sysdate , mod_pgm = 'RskP130' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where card_no = ? ";

//		setDouble(1, hAftAmt);		
		setString(1,hSonCardNo);

		updateCnt = updateTable();

		if (updateCnt == 0) {
			errmsg("update_crd_card error, kk=[%s] , id=[%s]", hSonCardNo,hIdNo);
			errExit(1);
		}
	}
	
	void changeCcaCardAcct() throws Exception {
		// --h_adj_loc_flag =1 : 調高 , h_adj_loc_flag =2 :調低

		String lsLogType = "";
		if (eqIgno(hAdjLocFlag, "1")) {
			if (hAftAmt > hTempAdjAmt) {
				lsLogType = "1"; // --永調 > 臨調 : 終止臨調
			} else {
				lsLogType = "2"; // --永調 < 臨調 : 臨調續用
				if (hAftAmt > hAdjPct) {
					hAdjPct = hAftAmt; // --永調 > 分期額度 : 分期額度 = 永調額度 調高分期額度
					lsLogType = "5";
				}
			}
		} else if (eqIgno(hAdjLocFlag, "2")) {
			if (hAftAmt < hTempAdjAmt) {
				lsLogType = "4"; // -- 永調 < 臨調 : 終止臨調
			} else
				lsLogType = "3";

		}

		if (commString.ssIn(lsLogType, ",1,4")) {
			// --終止臨調
			hTempAdjDate =  commDate.dateAdd(sysDate, 0, 0, -1);
		} else {
			hTempAdjDate = hAdjDate2;
		}

		updateCcaCardAcct();

		if (commString.ssIn(lsLogType, ",1,4,5")) {
			insertCcaLimitAdjLog();
		}

		insertCcaCreditLog(lsLogType);

	}

	void insertCcaCreditLog(String logType) throws Exception {

		double ldTotAmtMonth = 0;
		if (eqIgno(logType, "1") || eqIgno(logType, "4")) {
			ldTotAmtMonth = hAftAmt;
		} else {
			ldTotAmtMonth = hTempAdjAmt;
		}

		if (tiCreditlog <= 0) {
			sqlCmd = " insert into cca_credit_log ( ";
			sqlCmd += " tx_date , ";
			sqlCmd += " tx_time , ";
			sqlCmd += " card_acct_idx , ";
			sqlCmd += " acct_type , ";
			sqlCmd += " org_credit_cash , ";
			sqlCmd += " line_credit_cash , ";
			sqlCmd += " org_credit_amt , ";
			sqlCmd += " line_credit_amt , ";
			sqlCmd += " adj_quota , ";
			sqlCmd += " adj_eff_start_date , ";
			sqlCmd += " adj_eff_end_date , ";
			sqlCmd += " adj_reason , ";
			sqlCmd += " org_amt_month , ";
			sqlCmd += " tot_amt_month , ";
			sqlCmd += " org_inst_pct , ";
			sqlCmd += " adj_inst_pct , ";
			sqlCmd += " adj_user , ";
			sqlCmd += " log_type ,";
			sqlCmd += " mod_pgm ,";
			sqlCmd += " mod_time ";
			sqlCmd += " ) values ( ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " to_char(sysdate,'hh24miss') ,";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " 'System' ,";
			sqlCmd += " ? , ";
			sqlCmd += " 'RskP130' ,";
			sqlCmd += " sysdate ";
			sqlCmd += " ) ";

			daoTable = "cca_credit_log-A";
			tiCreditlog = ppStmtCrt("", "");
		}

		setInt(1, hCardAcctIdx);
		setString(2, hAcctType);
		setDouble(3, hBefCash);
		setDouble(4, hAftCash);
		setDouble(5, hBefAmt);
		setDouble(6, hAftAmt);
		setString(7, hAdjQuota);
		setString(8, hAdjDate1);
		setString(9, hTempAdjDate);
		setString(10, hAdjReason);
		setDouble(11, hTempAdjAmt);
		setDouble(12, ldTotAmtMonth);
		setDouble(13, hCardPct);
		setDouble(14, hAdjPct);
		setString(15, logType);

		sqlExec(tiCreditlog);
		if (sqlNrow <= 0) {
			errmsg("insert cca_credit_log error ");
			errExit(1);
		}

	}

	void insertCcaLimitAdjLog() throws Exception {

		if (tiLimitlog <= 0) {
			sqlCmd = " insert into cca_limit_adj_log ( ";
			sqlCmd += " log_date , ";
			sqlCmd += " log_time , ";
			sqlCmd += " aud_code , ";
			sqlCmd += " card_acct_idx ,";
			sqlCmd += " debit_flag ,";
			sqlCmd += " mod_type ,";
			sqlCmd += " lmt_tot_consume ,";
			sqlCmd += " tot_amt_month_b ,";
			sqlCmd += " tot_amt_month ,";
			sqlCmd += " adj_inst_pct_b ,";
			sqlCmd += " adj_inst_pct ,";
			sqlCmd += " adj_eff_date1 ,";
			sqlCmd += " adj_eff_date2 ,";
			sqlCmd += " adj_reason ,";
			sqlCmd += " adj_remark ,";
			sqlCmd += " adj_area ,";
			sqlCmd += " ecs_adj_rate ,";
			sqlCmd += " adj_user ,";
			sqlCmd += " adj_date ,";
			sqlCmd += " adj_time ,";
			sqlCmd += " apr_user ";
			sqlCmd += " ) values ( ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " to_char(sysdate,'hh24miss') , ";
			sqlCmd += " 'U' , ";
			sqlCmd += " ? , ";
			sqlCmd += " 'N' ,";
			sqlCmd += " '0' , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " '永久額度調整' , ";
			sqlCmd += " ? , ";
			sqlCmd += " '0' , ";
			sqlCmd += " 'system' , ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " to_char(sysdate,'hh24miss') , ";
			sqlCmd += " 'system' ";
			sqlCmd += " ) ";
			daoTable = "cca_limit_adj_log-A";
			tiLimitlog = ppStmtCrt("", "");
		}

		setInt(1, hCardAcctIdx);
		setDouble(2, hBefAmt);
		setDouble(3, hTempAdjAmt);
		setDouble(4, hTempAdjAmt);
		setDouble(5, hCardPct);
		setDouble(6, hAdjPct);
		setString(7, hAdjDate1);
		setString(8, hTempAdjDate);
		setString(9, hAdjReason);
		setString(10, hAdjArea);

		sqlExec(tiLimitlog);
		if (sqlNrow <= 0) {
			errmsg("insert cca_limit_adj_log error ");
			errExit(1);
		}

	}

	void updateCcaCardAcct() throws Exception {

		int updateCnt = 0;

		daoTable = "cca_card_acct";
		updateSQL += " adj_eff_end_date = ? , ";
		updateSQL += " adj_inst_pct = ? , ";
		updateSQL += " mod_user = 'system' , mod_time = sysdate , mod_pgm = 'RskP130' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where card_acct_idx = ? ";

		setString(1, hTempAdjDate);
		setDouble(2, hAdjPct);
		setInt(3, hCardAcctIdx);

		updateCnt = updateTable();

		if (updateCnt == 0) {
			errmsg("update_cca_card_acct error, kk=[%s] , id=[%s]", hCardAcctIdx,hIdNo);
			errExit(1);
		}

	}

	void insertRskAcnoLog() throws Exception {
		daoTable = "rsk_acnolog";
		setValue("kind_flag", "A");
		setValue("acno_p_seqno", hAcnoPSeqno);
		setValue("acct_type", hAcctType);
		setValue("id_p_seqno", hIdPSeqno);
		setValue("param_no", "1");
		setValue("log_date", sysDate);
		setValue("log_mode", "2");
		setValue("log_type", "1");
		if (eqIgno(hAdjLocFlag, "1")) {
			setValue("log_reason", hReasonUp);
		} else {
			setValue("log_reason", hReasonDown);
		}
		setValueDouble("bef_loc_amt", hBefAmt);
		setValueDouble("aft_loc_amt", hAftAmt);
		setValue("adj_loc_flag", hAdjLocFlag);
		setValue("print_comp_yn", "N");
		setValue("mail_comp_yn", "N");
		setValue("emend_type", "1");
		setValue("fh_flag", hFhFlag);
		setValueDouble("bef_loc_cash", hBefCash);
		setValueDouble("aft_loc_cash", hAftCash);
		setValue("send_ibm_flag", "N");
		setValue("sms_flag", hSmsFlag);
		setValue("block_reason", hBlockReason1);
		setValue("block_reason2", hBlockReason2);
		setValue("block_reason3", hBlockReason3);
		setValue("block_reason4", hBlockReason4);
		setValue("block_reason5", hBlockReason5);
		setValue("fit_cond", "N");
		setValue("apr_user", "System");
		setValue("apr_flag", "Y");
		setValue("apr_date", sysDate);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", "System");
		setValue("mod_pgm", "RskP130");
		setValueInt("mod_seqno", 1);

		insertTable();

		return;
	}

	void updateActAcno() throws Exception {
		int updateCnt = 0;

		daoTable = "act_acno";
		updateSQL += " adj_before_loc_amt = ? , ";
		updateSQL += " line_of_credit_amt = ? ,";
		if (eqIgno(hAdjLocFlag, "1")) {
			updateSQL += " h_adj_loc_high_date = to_char(sysdate,'yyyymmdd') , ";
			updateSQL += " adj_loc_high_t = ? , ";
		} else {
			updateSQL += " h_adj_loc_low_date = to_char(sysdate,'yyyymmdd') , ";
			updateSQL += " adj_loc_low_t = ? , ";
		}

		if (ibCashAdj) {
			updateSQL += " line_of_credit_amt_cash = ? , ";
		}

		updateSQL += " mod_user = 'system' , mod_time = sysdate , mod_pgm = 'RskP130' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where acct_type = ? and acct_key = ? ";

		setDouble(1, hBefAmt);
		setDouble(2, hAftAmt);
		if (eqIgno(hAdjLocFlag, "1")) {
			setString(3, hReasonUp);
		} else {
			setString(3, hReasonDown);
		}

		if (ibCashAdj) {
			setDouble(4, hAftCash);
			setString(5, hAcctType);
			setString(6, hAcctKey);
		} else {
			setString(4, hAcctType);
			setString(5, hAcctKey);
		}

		updateCnt = updateTable();

		if (updateCnt == 0) {
			errmsg("update_act_acno error, kk=[%s]", hAcctKey);
			errExit(1);
		}

	}

	void selectAcnoData() throws Exception {

		sqlCmd = "";
		sqlCmd += " select A.acno_p_seqno , A.id_p_seqno , uf_idno_id(A.id_p_seqno) as id_no , A.line_of_credit_amt , A.line_of_credit_amt_cash , ";
		sqlCmd += " decode(A.new_acct_flag,'Y',B.cashadv_loc_rate,B.cashadv_loc_rate_old) as cash_loc_rate , B.cashadv_loc_maxamt as cash_loc_max ";
		sqlCmd += " from act_acno A join ptr_acct_type B on A.acct_type =B.acct_type where A.acct_type = ? and A.acct_key = ? ";

		setString(1, hAcctType);
		setString(2, hAcctKey);

		sqlSelect();

		if (sqlNrow <= 0) {
			printf("查無此人資料 [%s]", hAcctKey);
			ibWrite = true;
//			errExit(1);
			return ;
		}

		hAcnoPSeqno = colSs("acno_p_seqno");
		hIdPSeqno = colSs("id_p_seqno");
		hIdNo = colSs("id_no");
		hBefAmt = colNum("line_of_credit_amt");
		hCashRate = colNum("cash_loc_rate");
		hMaxCashAmt = colNum("cash_loc_max");
		hBefCash = colNum("line_of_credit_amt_cash");

		if (hBefAmt > hAftAmt) {
			// --調低
			hAdjLocFlag = "2";
		} else {
			// --調高
			hAdjLocFlag = "1";
		}

	}

	void selectCcaCardAcctBlockReason() throws Exception {

		sqlCmd = "";
		sqlCmd += " select block_reason1 , block_reason2 , block_reason3 , block_reason4 , block_reason5 ";
		sqlCmd += " from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";

		setString(1, hAcnoPSeqno);

		sqlSelect();

		if (sqlNrow <= 0) {
			printf("查無此人授權帳戶資料 [%s]", hAcctKey);
			errExit(1);
		}

		hBlockReason1 = colSs("block_reason1");
		hBlockReason2 = colSs("block_reason2");
		hBlockReason3 = colSs("block_reason3");
		hBlockReason4 = colSs("block_reason4");
		hBlockReason5 = colSs("block_reason5");

	}

	boolean selectCcaCardAcctAdj() throws Exception {

		sqlCmd = "";

		sqlCmd = " select card_acct_idx , tot_amt_month , ";
		sqlCmd += " adj_inst_pct , adj_eff_start_date , adj_eff_end_date , adj_area , ";
		sqlCmd += " adj_quota , adj_reason , adj_remark ";
		sqlCmd += " from cca_card_acct where acno_p_seqno =? and debit_flag<>'Y' ";
		sqlCmd += " and adj_eff_start_date <= ? ";
		sqlCmd += " and adj_eff_end_date >= ? ";

		setString(1, hAcnoPSeqno);
		setString(2, hBusiDate);
		setString(3, hBusiDate);

		sqlSelect();

		if (sqlNrow <= 0)
			return false;

		hCardAcctIdx = colInt("card_acct_idx");
		hTempAdjAmt = colNum("tot_amt_month");
		hAdjPct = colNum("adj_inst_pct");
		hCardPct = colNum("adj_inst_pct");
		hAdjDate1 = colSs("adj_eff_start_date");
		hAdjDate2 = colSs("adj_eff_end_date");
		hAdjArea = colSs("adj_area");
		hAdjQuota = colSs("adj_quota");
		hAdjReason = colSs("adj_reason");
		hAdjRemark = colSs("adj_remark");

		return true;
	}

	void selectCrdCorrelate() throws Exception {

		if(hIdNo.isEmpty())
			return ;
		
		sqlCmd = "";
		sqlCmd += "select fh_flag from crd_correlate where correlate_id = ? ";
		setString(1, hIdNo);

		sqlSelect();
		if (sqlNrow <= 0)
			return;

		hFhFlag = colSs("fh_flag");

	}

	void calculateCash() {

		double hTempCashRate = 0;
		hTempCashRate = hBefCash / hBefAmt * 100;
		if (hTempCashRate == hCashRate) {
			hAftCash = hAftAmt * hCashRate / 100;
		} else {
			ibCashAdj = false;
			hAftCash = hBefCash;
		}

		if (hAftCash > hMaxCashAmt)
			hAftCash = hMaxCashAmt;
		hAftCash = commString.numScale(hAftCash, 0);
	}

	void splitData(String fileData) throws Exception {
		hAcctType = "01"; // --默認為 01

		byte[] bytes = fileData.getBytes("MS950");
//		hAcctKey = comc.subMS950String(bytes, 0, 11).trim() + "0";
		hAcctKey = comc.subMS950String(bytes, 0, 11).trim();
		oriId = hAcctKey;
		if(hAcctKey.length() == 10)
			hAcctKey += "0";				
		
		if("R".equals(commString.right(hAcctKey, 1))) {
			//--最後一碼為 R 判定為 106/599
			hAcctType ="06";
		}
		
		hAftAmt = commString.ss2Num(comc.subMS950String(bytes, 11, 12));
		hReasonUp = comc.subMS950String(bytes, 23, 1);
		hReasonDown = comc.subMS950String(bytes, 24, 1);		
		if (empty(hReasonUp))
			hReasonUp = "8";
		if (empty(hReasonDown))
			hReasonDown = "9";
	}

	void initData() {
		hAcctType = "";
		hAcctKey = "";
		hReasonUp = "";
		hReasonDown = "";
		hSmsFlag = "";
		hAftAmt = 0;
		hBefAmt = 0;
		hIdPSeqno = "";
		hAcnoPSeqno = "";
		hIdNo = "";
		hBlockReason1 = "";
		hBlockReason2 = "";
		hBlockReason3 = "";
		hBlockReason4 = "";
		hBlockReason5 = "";
		hAdjDate1 = "";
		hAdjDate2 = "";
		hTempAdjAmt = 0;
		hAdjLocFlag = "";
		hAftCash = 0;
		hCashRate = 0;
		hMaxCashAmt = 0;
		hFhFlag = "";
		hCardAcctIdx = 0;
		hAdjPct = 0;
		hAdjArea = "";
		hAdjQuota = "";
		hAdjReason = "";
		hAdjRemark = "";
		hTempAdjDate = "";
		hCardPct = 0;
		hCellarPhone = "";
		hMsgSeqno = "";
		hMsgDept = "";
		hMsgUserid = "";
		hMsgPgm = "";
		hMsgId = "";
		hChiName = "";
		oriId = "";
		ibCashAdj = true;
		ibWrite = false ;
	}

	void checkOpen() throws Exception {
		String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);
		String filePathOut = String.format("%s/media/rsk/%s", this.getEcsHome(), fileOutName);
		
		iiFileNum = openInputText(lsFile,"MS950");
		if (iiFileNum == -1) {
			showLogMessage("I", "", "無檔案可處理 !");
			okExit(0);	
		}
		
		filePathOut = String.format("%s/media/rsk/%s", this.getEcsHome(), fileOutName);
		fileOut = openOutputText(filePathOut,"MS950");
		if (fileOut < 0) {
			printf("CUST_CRLIMIT_YYYYMMDD_ERROR 產檔失敗 ! ");
			errExit(1);
		}
		
		return;
	}
}
