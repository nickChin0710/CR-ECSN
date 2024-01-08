/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- * 
*  109-07-03  V1.00.01   shiyuqi       updated for project coding standard   * 
*  109-07-22  V1.00.02   yanghan       修改了字段名称                                                                        *
*  109-09-10  V1.00.03   Alex          無檔案處理時不當掉                                                                *             
*  109-10-15  V1.00.04   Alex          處理完異動檔名並移至backup					 * \
*  109-10-19  V1.00.05    shiyuqi       updated for project coding standard  *
*  110-02-08  V1.00.06   Alex          增加錯誤訊息								 *  
*  110-02-20  V1.00.07   Alex          bug fix								 *     
*  111-01-24  V1.00.08   Alex          增加判斷參數效期和發送旗標                                                  *     
*  111-02-23  V1.00.09   Alex          insert sql 修正                                                               *
*  111-03-02  V1.00.10   Alex          cellar_phone 空值不送簡訊                                           *
*  111-03-23  V1.00.11   Alex          調整前金額為0 不計算預借現金					 *
*  111-07-28  V1.00.12   Alex          改為產出錯誤報表							 *
*  111-09-05  V1.00.13   Alex          錯誤報表調整                                                                            *
*****************************************************************************/

package Rsk;

import com.CommCrd;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommString;

public class RskP100 extends BaseBatch {

	private final String progname = "收徵審系統檔案永調 111/09/05 V1.00.13";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	String fileName = "";
	String fileOutName = "";
	private int iiFileNum = 0;
	private int iiFileOutNum = 0;
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
	String hSendFlag = "";
	String hEffDate1 = "";
	String hEffDate2 = "";
	String hChiName = "";
	String hErrorDesc = "";
	boolean ibCashAdj = true;
	boolean ibNotSend = false ;
	private int tiLimitlog = -1;
	private int tiCreditlog = -1;
	private int tiSmsdtl = -1;
	boolean ibContinue = false ;
	
	public static void main(String[] args) {
		RskP100 proc = new RskP100();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP100 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (empty(hBusiDate))
			hBusiDate = comc.getBusiDate();

		dateTime();

		fileName = "creditlimit_adj_" + hBusiDate + ".txt";
		fileOutName = "creditlimit_adj_error_"+ hBusiDate + ".txt";
		
		checkOpen();
		processData();
		if (totalCnt > 0) {
			closeInputText(iiFileNum);
			renameFile();
		}
		
		//--錯誤檔處理
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameErrorFile();		
		
		endProgram();
	}
	
	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileName);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
	void renameErrorFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileOutName);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileOutName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileOutName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileOutName + "] 已移至 [" + tmpstr2 + "]");
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
			fileData = removeBom(fileData);
			splitData(fileData);

			selectAcnoData();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
			
			calculateCash();
			updateActAcno();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
			selectCcaCardAcctBlockReason();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
			selectCrdCorrelate();
			insertRskAcnoLog();

			// --判斷是否有臨調
			if (selectCcaCardAcctAdj() == true) {
				changeCcaCardAcct();
				if(ibContinue) {
					writeErrorText();
					sqlRollback();
					continue;
				}					
			}

			// --送簡訊
			if (eqIgno(hSmsFlag, "Y")) {
				procSms();
				if(ibContinue) {
					writeErrorText();
					sqlRollback();
					continue;
				}	
			}
			sqlCommit(1);
		}
		closeOutputText(iiFileOutNum);
	}

	void procSms() throws Exception {

		// --取得送簡訊資料
		getSmsData();
		
		if(ibContinue)
			return ;
		
		if(ibNotSend==false) {
			if (eqIgno(hAdjLocFlag, "1")) {
				showLogMessage("I", "", "RSKM0920-1 參數設定為不發送或是發送日期不在有效期限內 , 不發送簡訊");
				return ;
			}	else	{
				showLogMessage("I", "", "RSKM0920-2 參數設定為不發送或是發送日期不在有效期限內 , 不發送簡訊");
				return ;
			}
		}
		
		if(hCellarPhone.isEmpty()) {
			showLogMessage("I", "", "查無手機號碼 , 不發送簡訊 , ID =["+hIdNo+"]");
			return ;
		}
		
		// --組合 msg_desc , 格式 :msg_userid , msg_id , cellar_phone , 參數1 , 參數 2 ...
		// --只先組前 3 個，待簡訊內容確定後再組完整
		String lsTempDesc = "";
		lsTempDesc = hMsgUserid + "," + hMsgId + "," + hCellarPhone;

		// --Insert 簡訊資料 待批次處理發送
		if (tiSmsdtl <= 0) {
			sqlCmd = " insert into sms_msg_dtl ( ";
			sqlCmd += " msg_seqno , ";
			sqlCmd += " msg_dept , ";
			sqlCmd += " msg_userid , ";
			sqlCmd += " msg_pgm , ";
			sqlCmd += " id_p_seqno ,";
			sqlCmd += " p_seqno , ";
			sqlCmd += " id_no ,";
			sqlCmd += " acct_type ,";
			sqlCmd += " msg_id , ";
			sqlCmd += " cellar_phone , ";
			sqlCmd += " cellphone_check_flag , ";
			sqlCmd += " chi_name , ";
			sqlCmd += " msg_desc , ";
			sqlCmd += " add_mode , ";
			sqlCmd += " resend_flag , ";
			sqlCmd += " send_flag , ";
			sqlCmd += " proc_flag , ";
			sqlCmd += " crt_date , ";
			sqlCmd += " crt_user , ";
			sqlCmd += " apr_date , ";
			sqlCmd += " apr_user , ";
			sqlCmd += " apr_flag , ";
			sqlCmd += " mod_user , ";
			sqlCmd += " mod_time , ";
			sqlCmd += " mod_pgm , ";
			sqlCmd += " mod_seqno ";
			sqlCmd += " ) values ( ";
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
			sqlCmd += " 'Y' ,";
			sqlCmd += " ? , ";
			sqlCmd += " ? , ";
			sqlCmd += " 'B' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " 'N' , ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " 'SYSTEM' , ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " 'SYSTEM' , ";
			sqlCmd += " 'Y' , ";
			sqlCmd += " 'SYSTEM' , ";
			sqlCmd += " sysdate , ";
			sqlCmd += " 'RskP100' , ";
			sqlCmd += " 1 ";
			sqlCmd += " ) ";

			daoTable = "sms_msg_dtl-A";
			tiSmsdtl = ppStmtCrt("", "");
		}

		setString(1, hMsgSeqno);
		setString(2, hMsgDept);
		setString(3, hMsgUserid);
		setString(4, hMsgPgm);
		setString(5, hIdPSeqno);
		setString(6, hAcnoPSeqno);
		setString(7, hIdNo);
		setString(8, hAcctType);
		setString(9, hMsgId);
		setString(10, hCellarPhone);
		setString(11, hChiName);
		setString(12, lsTempDesc);

		sqlExec(tiSmsdtl);
		if (sqlNrow <= 0) {
//			errmsg("insert sms_msg_dtl error ");
			hErrorDesc = "寫入發送簡訊檔失敗";
			ibContinue = true;
			return ;
//			errExit(1);
		}

	}

	void getSmsData() throws Exception {

		if (eqIgno(hAdjLocFlag, "1")) {
			hMsgPgm = "RSKM0920-1";
		} else {
			hMsgPgm = "RSKM0920-2";
		}

		// --查詢簡訊設定檔

		sqlCmd = "";
		sqlCmd += " select msg_dept , msg_id , msg_userid , msg_send_flag , send_eff_date1 , send_eff_date2 from sms_msg_id where msg_pgm = ? ";
		setString(1, hMsgPgm);

		sqlSelect();
		if (sqlNrow <= 0) {
			hErrorDesc = "簡訊參數檔未設定";
			ibContinue = true;
			return ;
//			errmsg("尚未設定簡訊參數");
//			errExit(1);
		}

		hMsgDept = colSs("msg_dept");
		hMsgId = colSs("msg_id");
		hMsgUserid = colSs("msg_userid");
		hSendFlag = colSs("msg_send_flag");
		hEffDate1 = colSs("send_eff_date1");
		hEffDate2 = colSs("send_eff_date2");
		
		//--不發送旗標
		if("Y".equals(hSendFlag) == false) {
			ibNotSend = false;
			return ;
		}
		
		//--不在效期內
		if(hEffDate1.isEmpty() == false) {
			if(sysDate.compareTo(hEffDate1) <0) {
				ibNotSend = false ;
				return ;
			}
		}		
		
		if(hEffDate2.isEmpty() == false) {
			if(sysDate.compareTo(hEffDate2) >0) {
				ibNotSend = false ;
				return ;
			}
		}				
		
		ibNotSend = true ;
		
		// --查詢客戶資料

		sqlCmd = "";
		sqlCmd += " select cellar_phone , chi_name from crd_idno where id_no = ? ";
		setString(1, hIdNo);

		sqlSelect();
		if (sqlNrow <= 0) {			
			hErrorDesc = "查無客戶卡人資料";
			ibContinue = true;
			return ;			
//			errmsg("查無客戶資料 ["+hIdNo+"]");
//			errExit(1);
		}

		hCellarPhone = colSs("cellar_phone");
		hChiName = colSs("chi_name");

		// --查詢簡訊序號

		sqlCmd = "";
		sqlCmd += " select lpad(to_char(ecs_modseq.nextval),10,'0') as msg_seqno from dual where 1=1 ";

		sqlSelect();
		if (sqlNrow <= 0) {
			hErrorDesc = "取得簡訊序號失敗";
			ibContinue = true;
			return ;
//			errmsg("無法取得簡訊序號");
//			errExit(1);
		}

		hMsgSeqno = colSs("msg_seqno");

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
		if(ibContinue)
			return ;

		if (commString.ssIn(lsLogType, ",1,4,5")) {
			insertCcaLimitAdjLog();
			if(ibContinue)
				return ;
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
			sqlCmd += " 'RskP100' ,";
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
//			errmsg("insert cca_credit_log error ");
			hErrorDesc = "寫入調額紀錄檔 cca_credit_log 失敗";
			ibContinue = true;
			return ;
//			errExit(1);
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
			hErrorDesc = "寫入 cca_limit_adj_log 失敗";
			ibContinue = true;
			return ;
//			errmsg("insert cca_limit_adj_log error ");
//			errExit(1);
		}

	}

	void updateCcaCardAcct() throws Exception {

		int updateCnt = 0;

		daoTable = "cca_card_acct";
		updateSQL += " adj_eff_end_date = ? , ";
		updateSQL += " adj_inst_pct = ? , ";
		updateSQL += " mod_user = 'system' , mod_time = sysdate , mod_pgm = 'RskP100' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where card_acct_idx = ? ";

		setString(1, hTempAdjDate);
		setDouble(2, hAdjPct);
		setInt(3, hCardAcctIdx);

		updateCnt = updateTable();

		if (updateCnt == 0) {
//			errmsg("update_cca_card_acct error, kk=[%s] , id=[%s]", hCardAcctIdx,hIdNo);
			printf("update_cca_card_acct error, kk=[%s] , id=[%s]", hCardAcctIdx,hIdNo);
			hErrorDesc = "異動授權帳戶檔失敗";
			ibContinue = true;
			return ;
//			errExit(1);
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
		setValue("mod_pgm", "RskP100");
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

		updateSQL += " mod_user = 'system' , mod_time = sysdate , mod_pgm = 'RskP100' , mod_seqno = nvl(mod_seqno,0)+1 ";
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
//			errmsg("update_act_acno error, kk=[%s]", hAcctKey);			
			printf("update_act_acno error, kk=[%s]", hAcctKey);
			hErrorDesc = "更新帳戶檔資料失敗";
			ibContinue = true ;
//			errExit(1);
			return ;
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
			hErrorDesc = "查無帳戶資料";
			ibContinue = true;
			return ;
//			errExit(1);
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
			hErrorDesc = "查無授權帳戶資料";
			ibContinue = true;
//			errExit(1);
			return ;
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
		if(hBefAmt ==0)
			hTempCashRate = 0 ;
		else	
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
		hAcctKey = comc.subMS950String(bytes, 0, 11).trim() + "0";
		hAftAmt = commString.ss2Num(comc.subMS950String(bytes, 11, 12));
		hReasonUp = comc.subMS950String(bytes, 23, 1);
		hReasonDown = comc.subMS950String(bytes, 24, 1);
		hSmsFlag = comc.subMS950String(bytes, 25, 1);
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
		ibCashAdj = true;
		ibContinue = false;
		hErrorDesc = "";
	}

	void checkOpen() throws Exception {
		String lsFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

		iiFileNum = openInputText(lsFile,"UTF-8");
		if (iiFileNum == -1) {
			this.showLogMessage("I", "", "無檔案可處理 !");
			okExit(0);	
		}
		
		String lsFileOut = String.format("%s/media/rsk/%s", this.getEcsHome(), fileOutName);
		iiFileOutNum = openOutputText(lsFileOut,"UTF-8");
		if (iiFileNum == -1) {
			errmsg("在程式執行目錄下沒有權限讀寫資料 [%s]", fileOutName);
			errExit(1);			
		}
		
		return;
	}

	String removeBom(String oriData) {
		String proData = "", bomString = "\uFEFF";
		if (oriData.startsWith(bomString)) {
			proData = oriData.replace(bomString, "");
		} else {
			proData = oriData;
		}

		return proData;
	}
	
	void writeErrorText() throws Exception {
		String newLine = "\r\n";
		StringBuilder tempBuf = new StringBuilder();
		tempBuf.append(comc.fixLeft(hAcctKey, 10));
		tempBuf.append("-");
		tempBuf.append(hErrorDesc);
		tempBuf.append(newLine);
		writeTextFile(iiFileOutNum, tempBuf.toString());
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
	
}
