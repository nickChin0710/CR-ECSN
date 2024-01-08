/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-07-03  V1.001 yanghan       修改了變量名稱和方法名稱							 *
* 109-07-08  V1.002 Zuwei         merge from development					 *
* 109-07-22  V1.003 yanghan       修改了字段名称            							 *
* 109-09-10  V1.004 Alex          無檔案處理時不當掉                                                                		 *
* 109-10-15  V1.005 Alex		     處理完異動檔名並移至backup						 *
* 109-10-19  V1.006 shiyuqi       updated for project coding standard        *
* 109-10-29  V1.007 Alex          專款專用作法變更								 *
* 110-02-08  V1.008 Alex          錯誤訊息更正									 *
* 110-02-26  V1.009 Alex          專款專用P類跳過不處理							 *
* 111-01-24  V1.010 Alex          增加判斷參數效期和發送旗標                                                               *  
* 111-03-02  V1.011 Alex          cellar_phone 空值不送簡訊                                                        *
* 111-09-05  V1.012 Alex          改為產出錯誤報表                                                                                  *
******************************************************************************/
package Rsk;

import com.CommCrd;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommDate;
import com.CommFTP;
import com.CommString;

public class RskP110 extends BaseBatch {

	private final String progname = "收徵審系統檔案臨調 111/09/05 V1.012";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	private int iiFileNum = 0;
	private int iiFileOutNum = 0;
	String fileName = "";
	String fileOutName = "";
	String hAcctType = "";
	String hAcctKey = "";
	String hIdNo = "";
	String hIdPSeqno = "";
	String hEffStartDate = "";
	String hEffEndDate = "";
	String hAdjReason = "";
	String hSmsFlag = "";
	double hTotAmt = 0;
	double hTotPct = 0;
	String hAdjRemark = "";
	String hHighRisk = "";
	String hAdjArea = "";
	String hSpecialFlag = "";
	String hMcc1 = "";
	double hMccAmt1 = 0;
	double hMccPct1 = 0;
	double hMonthCnt1 = 0;
	double hDayCnt1 = 0;
	String hMccStartDate1 = "";
	String hMccEndDate1 = "";
	String hMcc2 = "";
	double hMccAmt2 = 0;
	double hMccPct2 = 0;
	double hMonthCnt2 = 0;
	double hDayCnt2 = 0;
	String hMccStartDate2 = "";
	String hMccEndDate2 = "";
	String hMcc3 = "";
	double hMccAmt3 = 0;
	double hMccPct3 = 0;
	double hMonthCnt3 = 0;
	double hDayCnt3 = 0;
	String hMccStartDate3 = "";
	String hMccEndDate3 = "";
	String hAcnoPSeqno = "";
	String hClassCode = "";
	double hLineOfCreditAmt = 0;
	int hCardAcctIdx = 0;
	double hTotAmtMonth = 0;
	double hAdjInstPct = 0;
	String hCardNote = "";
	String hFhFlag = "";
	String hCellarPhone = "";
	String hMsgSeqno = "";
	String hMsgDept = "";
	String hMsgUserid = "";
	String hMsgPgm = "";
	String hMsgId = "";
	String hSendFlag = "";
	String hEffDate1 = "";
	String hEffDate2 = "";
	boolean ibNotSend = false;
	String hChiName = "";
	double hOriAmt = 0.0;
	String hOriDate1 = "";
	String hOriDate2 = "";
	String hAdjRiskType = "";

	private int tiLimitlog = -1;
	private int tiAdjparm = -1;
	private int tiSmsdtl = -1;
	
	String hErrorDesc = "";
	boolean ibContinue = false ;
	
	
	public static void main(String[] args) {
		RskP110 proc = new RskP110();
//		proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);

		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : RskP110 [business_date]");
			errExit(1);
		}

		dbConnect();
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}

		if (empty(hBusiDate))
			hBusiDate = comc.getBusiDate();

		dateTime();

		fileName = "creditlimit_tmp_" + hBusiDate + ".txt";
		fileOutName = "creditlimit_tmp_err_" + hBusiDate + ".txt";
		
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
	
	void renameErrorFile() throws Exception {
		String tmpstr1 = String.format("%s/media/rsk/%s", getEcsHome(), fileOutName);
		String tmpstr2 = String.format("%s/media/rsk/backup/%s", getEcsHome(), fileOutName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileOutName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileOutName + "] 已移至 [" + tmpstr2 + "]");
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
			selectCrdCorrelate();
			selectActAcno();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
						
			selectCcaCardAcct();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
//			//--判斷原額度
//			hOriAmt = hLineOfCreditAmt ;		
//			if(hOriDate1.compareTo(hBusiDate)<=0 && hBusiDate.compareTo(hOriDate2)<=0) {
//				hOriAmt = hTotAmtMonth;
//			}
			getCardNote();

			updateCcaCardAcct();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
			
			insertCcaLimitLog();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
			
			deleteCcaAdjParm();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
			
			insertCcaAdjParm();
			if(ibContinue) {
				writeErrorText();
				sqlRollback();
				continue;
			}
			
			
			if (hSmsFlag.equals("Y")) {
				procSms();
				if(ibContinue) {
					writeErrorText();
					sqlRollback();
					continue;
				}
			}
			sqlCommit();
		}
		closeOutputText(iiFileOutNum);
	}

	void procSms() throws Exception {

		// --取得送簡訊資料
		getSmsData();
		
		if(ibNotSend==false) {
			showLogMessage("I", "", "CCAM2050-ADJ 參數設定為不發送或是發送日期不在有效期限內 , 不發送簡訊");
			return ;
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
			sqlCmd += " 'RskP110' , ";
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
//			errExit(1);
			printf("新增調額簡訊失敗 [%s]", hAcctKey);
			hErrorDesc = "新增調額簡訊失敗";
			ibContinue = true;
			return ;
		}

	}

	void getSmsData() throws Exception {

		hMsgPgm = "CCAM2050-ADJ";

		// --查詢簡訊設定檔

		sqlCmd = "";
		sqlCmd += " select msg_dept , msg_id , msg_userid , msg_send_flag , send_eff_date1 , send_eff_date2 from sms_msg_id where msg_pgm = ? ";
		setString(1, hMsgPgm);

		sqlSelect();
		if (sqlNrow <= 0) {
//			errmsg("尚未設定簡訊參數");
//			errExit(1);
			printf("尚未設定簡訊參數 [%s]", hAcctKey);
			hErrorDesc = "尚未設定簡訊參數";
			ibContinue = true;
			return ;
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
//			errmsg("查無客戶資料 [%s] ",hIdNo);
//			errExit(1);
			printf("查無客戶基本資料 [%s]", hAcctKey);
			hErrorDesc = "查無客戶基本資料";
			ibContinue = true;
			return ;
		}

		hCellarPhone = colSs("cellar_phone");
		hChiName = colSs("chi_name");

		// --查詢簡訊序號

		sqlCmd = "";
		sqlCmd += " select lpad(to_char(ecs_modseq.nextval),10,'0') as msg_seqno from dual where 1=1 ";

		sqlSelect();
		if (sqlNrow <= 0) {
//			errmsg("無法取得簡訊序號");
//			errExit(1);
			printf("取得簡訊序號失敗 [%s]", hAcctKey);
			hErrorDesc = "取得簡訊序號失敗";
			ibContinue = true;
			return ;
		}

		hMsgSeqno = colSs("msg_seqno");

	}

	void insertCcaAdjParm() throws Exception {

//		sqlCmd = "";
//		sqlCmd += " select risk_type , lmt_amt_time_pct , lmt_cnt_month as month_cnt , lmt_cnt_day as day_cnt ";
//		sqlCmd += " from cca_risk_consume_parm where area_type ='T' and card_note = ? and risk_level = ? ";
//		
//		setString(1,hCardNote);
//		setString(2,hClassCode);
//		
//		sqlSelect();
//		
//		if(sqlNrow<=0) {
//			errmsg("select cca_risk_consume_parm error !");
//			errExit(1);
//		}

		int ii = 3;
		String[] lsRiskType = new String[ii];
		lsRiskType[0] = hMcc1;
		lsRiskType[1] = hMcc2;
		lsRiskType[2] = hMcc3;
		
//		Double[] ldAmtPct = new Double[ii];
//		Double[] ldMonthCnt = new Double[ii];
//		Double[] ldDayCnt = new Double[ii];

//		for(int ll=0;ll<ii;ll++) {
//			lsRiskType[ll] = colSs(ll,"risk_type");
//			ldAmtPct[ll] = colNum(ll,"lmt_amt_time_pct");
//			ldMonthCnt[ll] = colNum(ll,"month_cnt");
//			ldDayCnt[ll] = colNum(ll,"day_cnt");
//		}

		String lsTempType = "", hTempStartDate = "", hTempEndDate = "";
		double ldMonthAmt = 0, ldDayAmt = 0, ldTempMonth = 0, ldTempDay = 0;

		// --月限額 = 調額 , 日限額 = 調額 * ld_amt_pct / 100, 月限次 = ld_month_cnt , 日限次 =
		// ld_day_cnt

		for (int rr = 0; rr < lsRiskType.length; rr++) {
			// --清值
			lsTempType = "";
			hTempStartDate = "";
			hTempEndDate = "";
			ldMonthAmt = 0;
			ldDayAmt = 0;
			ldTempMonth = 0;
			ldTempDay = 0;
			// --開始
			//--P 類視為除專款專用調整之風險分類之外的所有風險分類調整，且為一般臨調，非專款專用，所以跳過
			lsTempType = lsRiskType[rr];
			if(lsTempType.isEmpty() || "P".equals(lsTempType))	continue;
			if (hHighRisk.equals("Y") && checkHighRisk(lsTempType)) {
				ldMonthAmt = 0;
				ldDayAmt = 0;
				ldTempMonth = 0;
				ldTempDay = 0;
				hTempStartDate = hEffStartDate;
				hTempEndDate = hEffEndDate;
			} else if (lsTempType.equals(hMcc1)) {
				ldMonthAmt = hMccAmt1;
				ldDayAmt = hMccPct1;
				ldTempMonth = hMonthCnt1;
				ldTempDay = hDayCnt1;
				hTempStartDate = hMccStartDate1;
				hTempEndDate = hMccEndDate1;
			} else if (lsTempType.equals(hMcc2)) {
				ldMonthAmt = hMccAmt2;
				ldDayAmt = hMccPct2;
				ldTempMonth = hMonthCnt2;
				ldTempDay = hDayCnt2;
				hTempStartDate = hMccStartDate2;
				hTempEndDate = hMccEndDate2;
			} else if (lsTempType.equals(hMcc3)) {
				ldMonthAmt = hMccAmt3;
				ldDayAmt = hMccPct3;
				ldTempMonth = hMonthCnt3;
				ldTempDay = hDayCnt3;
				hTempStartDate = hMccStartDate3;
				hTempEndDate = hMccEndDate3;
			}
//			} else {
//				if(hSpecialFlag.equals("Y") && hTotAmt > hOriAmt) {
//					ldMonthAmt = hOriAmt;
//					ldDayAmt = hOriAmt * ldAmtPct[rr] / 100;
//				}	else	{
//					ldMonthAmt = hTotAmt;
//					ldDayAmt = hTotAmt * ldAmtPct[rr] / 100;
//				}				 
//				ldTempMonth = ldMonthCnt[rr];
//				ldTempDay = ldDayCnt[rr];
//				hTempStartDate = hEffStartDate;
//				hTempEndDate = hEffEndDate;
//			}

			if (tiAdjparm <= 0) {
				sqlCmd = " insert into cca_adj_parm ( ";
				sqlCmd += " card_acct_idx , ";
				sqlCmd += " risk_type , ";
				sqlCmd += " adj_month_amt , ";
				sqlCmd += " adj_month_cnt , ";
				sqlCmd += " adj_day_amt , ";
				sqlCmd += " adj_day_cnt , ";
				sqlCmd += " adj_eff_start_date , ";
				sqlCmd += " adj_eff_end_date , ";
				sqlCmd += " card_note , ";
				sqlCmd += " risk_level , ";
				sqlCmd += " spec_flag , ";
				sqlCmd += " crt_date , ";
				sqlCmd += " crt_time , ";
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
				sqlCmd += " 'Y' , ";
				sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
				sqlCmd += " to_char(sysdate,'hh24miss') , ";
				sqlCmd += " 'SYSTEM' , ";
				sqlCmd += " sysdate , ";
				sqlCmd += " 'RskP110' , ";
				sqlCmd += " 1 ";
				sqlCmd += " ) ";
				daoTable = "cca_adj_parm-A";
				tiAdjparm = ppStmtCrt("", "");
			}

			setInt(1, hCardAcctIdx);
			setString(2, lsTempType);
			setDouble(3, ldMonthAmt);
			setDouble(4, ldTempMonth);
			setDouble(5, ldDayAmt);
			setDouble(6, ldTempDay);
			setString(7, hTempStartDate);
			setString(8, hTempEndDate);
			setString(9, hCardNote);
			setString(10, hClassCode);

			sqlExec(tiAdjparm);
			if (sqlNrow <= 0) {
//				errmsg("insert cca_adj_parm error ");
//				errExit(1);
				printf("新增調額參數失敗 [%s]", hAcctKey);
				hErrorDesc = "新增調額參數失敗";
				ibContinue = true;
				return ;
			}

		}

	}

	boolean checkHighRisk(String riskType) throws Exception {

		sqlCmd = " select sys_data3 from cca_sys_parm1 where sys_id = 'RISK' and sys_key = ? ";

		setString(1, riskType);

		sqlSelect();

		if (sqlNrow <= 0) {
//			errmsg("Risk type not found ! type = [%s]", riskType);
//			errExit(1);
			printf("查無風險類別 [%s]", hAcctKey);
			hErrorDesc = "查無風險類別";
			ibContinue = true;
			return false;			
		}

		if (colEq("sys_data3", "Y"))
			return true;

		return false;
	}

	void deleteCcaAdjParm() throws Exception {

		sqlCmd = " delete cca_adj_parm where card_acct_idx = ? ";
		setInt(1, hCardAcctIdx);

		sqlExec("");

		if (sqlNrow < 0) {
//			sqlerr("delete cca_adj_parm error");
//			errExit(1);			
			printf("刪除調額參數失敗 [%s]", hAcctKey);
			hErrorDesc = "刪除調額參數失敗";
			ibContinue = true;
			return ;
		}

	}

	void insertCcaLimitLog() throws Exception {

		if (tiLimitlog <= 0) {
			sqlCmd = " insert into cca_limit_adj_log ( ";
			sqlCmd += " log_date , ";
			sqlCmd += " log_time , ";
			sqlCmd += " aud_code , ";
			sqlCmd += " card_acct_idx ,";
			sqlCmd += " debit_flag ,";
			sqlCmd += " mod_type ,";
			sqlCmd += " rela_flag ,";
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
			sqlCmd += " adj_user ,";
			sqlCmd += " adj_date ,";
			sqlCmd += " adj_time ,";
			sqlCmd += " apr_user ";
			sqlCmd += " ) values ( ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " to_char(sysdate,'hh24miss') , ";
			sqlCmd += " 'A' , ";
			sqlCmd += " ? , ";
			sqlCmd += " 'N' ,";
			sqlCmd += " '1' , ";
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
			sqlCmd += " 'system' , ";
			sqlCmd += " to_char(sysdate,'yyyymmdd') , ";
			sqlCmd += " to_char(sysdate,'hh24miss') , ";
			sqlCmd += " 'system'  ";
			sqlCmd += " ) ";
			daoTable = "cca_limit_adj_log-A";
			tiLimitlog = ppStmtCrt("", "");
		}

		setInt(1, hCardAcctIdx);
		setString(2, hFhFlag);
		setDouble(3, hLineOfCreditAmt);
		setDouble(4, hTotAmtMonth);
		setDouble(5, hTotAmt);
		setDouble(6, hAdjInstPct);
		setDouble(7, hTotPct);
		setString(8, hEffStartDate);
		setString(9, hEffEndDate);
		setString(10, hAdjReason);
		setString(11, hAdjRemark);
		setString(12, hAdjArea);

		sqlExec(tiLimitlog);
		if (sqlNrow <= 0) {
//			errmsg("insert cca_limit_log error ");
//			errExit(1);
			printf("寫入調額記錄檔失敗 [%s]", hAcctKey);
			hErrorDesc = "寫入調額記錄檔失敗";
			ibContinue = true;
			return ;
			
		}

	}

	void updateCcaCardAcct() throws Exception {
		int updateCnt = 0;

		daoTable = "cca_card_acct";
		updateSQL += " adj_eff_start_date = ? , ";
		updateSQL += " adj_eff_end_date = ? , ";
		updateSQL += " adj_reason = ? , ";
		updateSQL += " adj_remark = ? , ";
		updateSQL += " tot_amt_month = ? , ";
		updateSQL += " adj_inst_pct = ? , ";
		updateSQL += " adj_area = ? , ";
		updateSQL += " adj_quota ='Y' , ";
		updateSQL += " adj_date =to_char(sysdate,'yyyymmdd') , ";
		updateSQL += " adj_time =to_char(sysdate,'hh24miss') , ";
		updateSQL += " adj_user ='SYSTEM' , ";
		updateSQL += " adj_risk_flag = ? , ";
		updateSQL += " notice_flag = 'N' , ";
		updateSQL += " adj_sms_flag = ? , ";
		updateSQL += " adj_risk_type = ? , ";
		updateSQL += " mod_user = 'SYSTEM' , mod_time = sysdate , mod_pgm = 'RskP110' , mod_seqno = nvl(mod_seqno,0)+1 ";

		whereStr = " where card_acct_idx = ? ";

		setString(1, hEffStartDate);
		setString(2, hEffEndDate);
		setString(3, hAdjReason);
		setString(4, hAdjRemark);
		setDouble(5, hTotAmt);
		setDouble(6, hTotPct);
		setString(7, hAdjArea);
		setString(8, hHighRisk);
		setString(9, hSmsFlag);
		setString(10, hAdjRiskType);
		setInt(11, hCardAcctIdx);

		updateCnt = updateTable();

		if (updateCnt == 0) {
//			errmsg("update cca_card_acct error, kk=[%s]", hAcctKey);
//			errExit(1);
			printf("異動授權帳戶檔失敗 = [%s]", hAcctKey);
			hErrorDesc = "異動授權帳戶檔失敗";
			ibContinue = true;
			return ;
		}

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

	void getCardNote() throws Exception {

		sqlCmd = "";
		sqlCmd += " select A.card_note , count(*) as cnt_parm ";
		sqlCmd += " from crd_card A join cca_risk_consume_parm B on B.card_note = A.card_note and B.apr_date <> '' ";
		sqlCmd += " where A.current_code ='0' and A.acno_p_seqno = ? group by A.card_note ";
		sqlCmd += " order by decode(A.card_note,'C',9,'G',8,'P',7,'S',6,'I',5,10) ";

		setString(1, hAcnoPSeqno);

		sqlSelect();

		int rr = sqlNrow;
		for (int i = 0; i < rr; i++) {
			if (colInt(i, "cnt_parm") > 0) {
				hCardNote = colSs(i, "card_note");
				break;
			}
		}

		if (empty(hCardNote))
			hCardNote = "*";

	}

	void selectCcaCardAcct() throws Exception {

		sqlCmd = "";
		sqlCmd += " select card_acct_idx , tot_amt_month , adj_inst_pct , adj_eff_start_date , adj_eff_end_date ";
		sqlCmd += " from cca_card_acct where debit_flag <> 'Y' and acno_p_seqno = ? ";
		setString(1, hAcnoPSeqno);

		sqlSelect();

		if (sqlNrow <= 0) {
//			errmsg("查無帳戶帳號資料 [%s,%s]", hAcnoPSeqno,hIdNo);
//			errExit(1);
			printf("查無授權帳戶帳號資料 [%s,%s]", hAcnoPSeqno,hIdNo);
			hErrorDesc = "查無授權帳戶資料";
			ibContinue = true;
			return ;
		}

		hCardAcctIdx = colInt("card_acct_idx");
		hTotAmtMonth = colNum("tot_amt_month");
		hAdjInstPct = colNum("adj_inst_pct");
		hOriDate1 = colSs("adj_eff_start_date");
		hOriDate2 = colSs("adj_eff_end_date");

	}

	void selectActAcno() throws Exception {

		sqlCmd = "";
		sqlCmd += " select acno_p_seqno , id_p_seqno , class_code , line_of_credit_amt ";
		sqlCmd += " from act_acno where acct_type = ? and acct_key = ? ";

		setString(1, hAcctType);
		setString(2, hAcctKey);
		sqlSelect();
		if (sqlNrow <= 0) {
			printf("查無此人資料 [%s]", hAcctKey);
			hErrorDesc = "查無帳戶資料";
			ibContinue = true;
			return ;
		}

		hAcnoPSeqno = colSs("acno_p_seqno");
		hIdPSeqno = colSs("id_p_seqno");
		hClassCode = colSs("class_code");
		hLineOfCreditAmt = colNum("line_of_credit_amt");

	}

	void splitData(String fileData) throws Exception {
		hAcctType = "01"; // --默認為 01

		byte[] bytes = fileData.getBytes("MS950");
		hIdNo = comc.subMS950String(bytes, 0, 11).trim();
		hAcctKey = hIdNo + "0";
		hEffStartDate = comc.subMS950String(bytes, 11, 8).trim();
		hEffEndDate = comc.subMS950String(bytes, 19, 8).trim();
		hAdjReason = comc.subMS950String(bytes, 27, 2).trim();
		hSmsFlag = comc.subMS950String(bytes, 29, 1);
		hTotAmt = commString.ss2Num(comc.subMS950String(bytes, 30, 12));
		hTotPct = commString.ss2Num(comc.subMS950String(bytes, 42, 12));
		hAdjRemark = comc.subMS950String(bytes, 54, 60).trim();
		hHighRisk = comc.subMS950String(bytes, 114, 1);
		hAdjArea = comc.subMS950String(bytes, 115, 1);
		hSpecialFlag = comc.subMS950String(bytes, 116, 1);
		hMcc1 = comc.subMS950String(bytes, 117, 2).trim();
		hMccAmt1 = commString.ss2Num(comc.subMS950String(bytes, 119, 12));
		hMccPct1 = commString.ss2Num(comc.subMS950String(bytes, 131, 12));
		hMonthCnt1 = commString.ss2Num(comc.subMS950String(bytes, 143, 3));
		hDayCnt1 = commString.ss2Num(comc.subMS950String(bytes, 146, 3));
		hMccStartDate1 = comc.subMS950String(bytes, 149, 8).trim();
		hMccEndDate1 = comc.subMS950String(bytes, 157, 8).trim();
		hMcc2 = comc.subMS950String(bytes, 165, 2).trim();
		hMccAmt2 = commString.ss2Num(comc.subMS950String(bytes, 167, 12));
		hMccPct2 = commString.ss2Num(comc.subMS950String(bytes, 179, 12));
		hMonthCnt2 = commString.ss2Num(comc.subMS950String(bytes, 191, 3));
		hDayCnt2 = commString.ss2Num(comc.subMS950String(bytes, 194, 3));
		hMccStartDate2 = comc.subMS950String(bytes, 197, 8).trim();
		hMccEndDate2 = comc.subMS950String(bytes, 205, 8).trim();
		hMcc3 = comc.subMS950String(bytes, 213, 2).trim();
		hMccAmt3 = commString.ss2Num(comc.subMS950String(bytes, 215, 12));
		hMccPct3 = commString.ss2Num(comc.subMS950String(bytes, 227, 12));
		hMonthCnt3 = commString.ss2Num(comc.subMS950String(bytes, 239, 3));
		hDayCnt3 = commString.ss2Num(comc.subMS950String(bytes, 242, 3));
		hMccStartDate3 = comc.subMS950String(bytes, 245, 8).trim();
		hMccEndDate3 = comc.subMS950String(bytes, 253, 8).trim();
		// --TCB 風險分類沒有調整後月限額 , 調整後次限額 , 月限次 , 日限次
		// --檔案放 0 自動修正月限額、次限額 = 帳戶調整後月限額 , 月限次、日限次 = 999
		if (empty(hSmsFlag)) {
			hSmsFlag = "N";
		}

		if (hTotPct == 0)
			hTotPct = hTotAmt;

		if (empty(hMcc1) == false) {
			if (hMccAmt1 == 0)
				hMccAmt1 = hTotAmt;
			if (hMccPct1 == 0)
				hMccPct1 = hMccAmt1;
			if (hMonthCnt1 == 0)
				hMonthCnt1 = 999;
			if (hDayCnt1 == 0)
				hDayCnt1 = 999;
			if (empty(hAdjRiskType))
				hAdjRiskType = hMcc1;
			else
				hAdjRiskType += "," + hMcc1;
		}

		if (empty(hMcc2) == false) {
			if (hMccAmt2 == 0)
				hMccAmt2 = hTotAmt;
			if (hMccPct2 == 0)
				hMccPct2 = hMccAmt2;
			if (hMonthCnt2 == 0)
				hMonthCnt2 = 999;
			if (hDayCnt2 == 0)
				hDayCnt2 = 999;
			if (empty(hAdjRiskType))
				hAdjRiskType = hMcc2;
			else
				hAdjRiskType += "," + hMcc2;
		}

		if (empty(hMcc3) == false) {
			if (hMccAmt3 == 0)
				hMccAmt3 = hTotAmt;
			if (hMccPct3 == 0)
				hMccPct3 = hMccAmt3;
			if (hMonthCnt3 == 0)
				hMonthCnt3 = 999;
			if (hDayCnt3 == 0)
				hDayCnt3 = 999;
			if (empty(hAdjRiskType))
				hAdjRiskType = hMcc3;
			else
				hAdjRiskType += "," + hMcc3;
		}
//        displayData();
	}

	void initData() throws Exception {
		hAcctType = "";
		hAcctKey = "";
		hIdNo = "";
		hIdPSeqno = "";
		hEffStartDate = "";
		hEffEndDate = "";
		hAdjReason = "";
		hSmsFlag = "";
		hTotAmt = 0;
		hTotPct = 0;
		hAdjRemark = "";
		hHighRisk = "";
		hAdjArea = "";
		hSpecialFlag = "";
		hMcc1 = "";
		hMccAmt1 = 0;
		hMccPct1 = 0;
		hMonthCnt1 = 0;
		hDayCnt1 = 0;
		hMccStartDate1 = "";
		hMccEndDate1 = "";
		hMcc2 = "";
		hMccAmt2 = 0;
		hMccPct2 = 0;
		hMonthCnt2 = 0;
		hDayCnt2 = 0;
		hMccStartDate2 = "";
		hMccEndDate2 = "";
		hMcc3 = "";
		hMccAmt3 = 0;
		hMccPct3 = 0;
		hMonthCnt3 = 0;
		hDayCnt3 = 0;
		hMccStartDate3 = "";
		hMccEndDate3 = "";
		hAcnoPSeqno = "";
		hClassCode = "";
		hLineOfCreditAmt = 0;
		hCardAcctIdx = 0;
		hTotAmtMonth = 0;
		hAdjInstPct = 0;
		hCardNote = "";
		hFhFlag = "";
		hCellarPhone = "";
		hMsgSeqno = "";
		hMsgDept = "";
		hMsgUserid = "";
		hMsgPgm = "";
		hMsgId = "";
		hChiName = "";
		hOriAmt = 0.0;
		hOriDate1 = "";
		hOriDate2 = "";
		hAdjRiskType = "";
		ibContinue = false;
		hErrorDesc = "";
	}

	void checkOpen() throws Exception {
//		String ls_file = String.format("C:/EcsWeb/media/rsk/%s", file_name);
		String lsfFile = String.format("%s/media/rsk/%s", this.getEcsHome(), fileName);

		iiFileNum = openInputText(lsfFile, "UTF-8");
		if (iiFileNum == -1) {
			this.showLogMessage("I", "", "無檔案可處理 !");
//			errmsg("在程式執行目錄下沒有權限讀寫資料 [%s]", fileName);
//			errExit(1);			
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
