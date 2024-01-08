/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  110/01/05  V1.00.00    Wendy Lu                     program initial        *
 *  110/01/06  V1.00.01    tanwei                       zz開頭變量修改                                   *
 *  112/03/03  V1.00.02    Wilson    數字型態欄位右靠左補滿零、調整「持卡人中文/英文姓名」於最後    *
 *  112/03/13  V1.00.03    Wilson    檔案格式調整                                                                                             *
 *  112/03/15  V1.00.04    Wilson    autopay_indicator調整                                                             *
 *  112/03/18  V1.00.05    Wilson    產檔邏輯調整                                                                                             *
 *  112/04/22  V1.00.06    Wilson    持卡人中文姓名取14碼                                                                             *
 *  112/04/26  V1.00.07    Wilson    持卡人中文姓名取13碼                                                                             *
 *  112/05/01  V1.00.08    Wilson    法人改讀crd_corp                             *
 ******************************************************************************/

package Inf;

import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.BaseBatch;
import com.CommString;

public class InfC000 extends BaseBatch {
	private final String progname = "產生送CRDB 00新增帳戶資料檔程式  112/05/01 V1.00.08";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommCrdRoutine comcr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;
	String isFileName = "";
	private int ilFile00;
	String hSysDate = "";
	String hDataType = "";	
	String hIdNo = "";
	double hRevolveIntRate = 0.0;
	String hAmt = "";
	String hOrg306Amt = "0";
	String hOrg506Amt = "0";
	String hActAcno = "";
	String hIndicator = "";
	String hPercentage = "";
	String hChiName = "";
	String hEngName = "";
	String hStatUnprintFlag = "";
	String hStatSendInternet = "";
	String hBillApplyFlag = "";	
	String hBirthday = "";
	String hAcctStatus = "";
	String hCardIndicator = "";
	int hAcctJrnlBal = 0;
	String hPaymentRate1 = "";
	String tmpRate = "";
	int crdRelaCnt = 0;
	String hCorpPSeqno = "";
	String tmpRelaFlag  = "";
	String fileBillApplyFlag  = "";
	String fileAcctJrnlBal  = "";
	String filePaymentRateFlag  = "";
	String tmpSupType  = "";

	public static void main(String[] args) {
		InfC000 proc = new InfC000();
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfCrb00 [business_date]");
			errExit(1);
		}
		dbConnect();
		if (liArg == 1) {
			hSysDate = args[0];
		}

		if (empty(hSysDate))
			hSysDate = commDate.dateAdd(sysDate, 0, 0, -1);
		
//		System.out.print(hSysDate);

		isFileName = "CRU23B1_TYPE_00_" + hSysDate + ".txt";
		
		comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
						
		checkOpen();
		selectActAcno(hSysDate);
		closeOutputText(ilFile00);
		procFTP();
		renameFile();
		endProgram();
	}
        
	void selectActAcno(String hSysDate) throws Exception {
		sqlCmd = "select '0' as data_type, "; //信用卡正卡人
		sqlCmd += "       a.id_no, ";
		sqlCmd += "       b.revolve_int_rate as tmp_revolve_int_rate, ";
		sqlCmd += "       b.line_of_credit_amt as tmp_line_of_credit_amt, ";
		sqlCmd += "       b.autopay_acct_no as tmp_autopay_acct_no, ";
		sqlCmd += "       b.autopay_indicator as tmp_autopay_indicator, ";
		sqlCmd += "       lpad(cast(cast((round((b.line_of_credit_amt_cash/b.line_of_credit_amt),2)) * 100 as decimal(2,0)) as varchar), 2, '0')  as credit_rate, ";
		sqlCmd += "       '' as tmp_corp_p_seqno, ";
		sqlCmd += "       b.stat_unprint_flag, ";
		sqlCmd += "       b.stat_send_internet, ";
		sqlCmd += "       b.bill_apply_flag, ";
		sqlCmd += "       a.birthday, ";
		sqlCmd += "       c.acct_jrnl_bal as tmp_acct_jrnl_bal, ";
		sqlCmd += "       b.payment_rate1 as tmp_payment_rate1, ";
		sqlCmd += "       b.acct_status as tmp_acct_status, ";
		sqlCmd += "       a.eng_name, ";
		sqlCmd += "       a.chi_name  ";
		sqlCmd += "  from crd_idno a,act_acno b,act_acct c ";
		sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
		sqlCmd += "   and b.p_seqno = c.p_seqno "; 
		sqlCmd += "   and b.acno_flag <> '2' ";  
		sqlCmd += "   and b.crt_date = ? ";
		sqlCmd += "union ";
		sqlCmd += "select '1' as data_type, "; //信用卡附卡人
		sqlCmd += "       a.id_no, ";
		sqlCmd += "       0 as tmp_revolve_int_rate, ";
		sqlCmd += "       0 as tmp_line_of_credit_amt, ";
		sqlCmd += "       '' as tmp_autopay_acct_no, ";
		sqlCmd += "       '' as tmp_autopay_indicator, ";
		sqlCmd += "       '' as credit_rate, ";
		sqlCmd += "       '' as tmp_corp_p_seqno, ";
		sqlCmd += "       c.stat_unprint_flag, ";
		sqlCmd += "       c.stat_send_internet, ";
		sqlCmd += "       c.bill_apply_flag, ";
		sqlCmd += "       a.birthday, ";
		sqlCmd += "       0 as tmp_acct_jrnl_bal, ";
		sqlCmd += "       '' as tmp_payment_rate1, ";
		sqlCmd += "       '' as tmp_acct_status, ";
		sqlCmd += "       a.eng_name, ";
		sqlCmd += "       a.chi_name  ";
		sqlCmd += "  from crd_idno a,crd_card b,act_acno c ";
		sqlCmd += " where a.id_p_seqno = b.id_p_seqno "; 
		sqlCmd += "   and b.major_id_p_seqno = c.id_p_seqno "; 
		sqlCmd += "   and a.id_p_seqno not in (select id_p_seqno from act_acno) ";
		sqlCmd += "   and c.acno_flag = '1' ";
		sqlCmd += "   and a.crt_date = ? ";  
		sqlCmd += "union ";
		sqlCmd += "select '2' as data_type, "; //法人
		sqlCmd += "       a.corp_no as id_no, ";
		sqlCmd += "       b.revolve_int_rate as tmp_revolve_int_rate, ";
		sqlCmd += "       b.line_of_credit_amt as tmp_line_of_credit_amt, ";
		sqlCmd += "       b.autopay_acct_no as tmp_autopay_acct_no, ";
		sqlCmd += "       b.autopay_indicator as tmp_autopay_indicator, ";
		sqlCmd += "       lpad(cast(cast((round((b.line_of_credit_amt_cash/b.line_of_credit_amt),2)) * 100 as decimal(2,0)) as varchar), 2, '0')  as credit_rate, ";
		sqlCmd += "       b.corp_p_seqno as tmp_corp_p_seqno, ";
		sqlCmd += "       b.stat_unprint_flag, ";
		sqlCmd += "       b.stat_send_internet, ";
		sqlCmd += "       b.bill_apply_flag, ";
		sqlCmd += "       '' as birthday, ";
		sqlCmd += "       c.acct_jrnl_bal as tmp_acct_jrnl_bal, ";
		sqlCmd += "       b.payment_rate1 as tmp_payment_rate1, ";
		sqlCmd += "       b.acct_status as tmp_acct_status, ";
		sqlCmd += "       a.eng_name, ";
		sqlCmd += "       a.chi_name  ";
		sqlCmd += "  from crd_corp a,act_acno b,act_acct c ";
		sqlCmd += " where a.corp_p_seqno = b.corp_p_seqno ";
		sqlCmd += "   and b.p_seqno = c.p_seqno ";
		sqlCmd += "   and b.acno_flag = '2' ";
		sqlCmd += "   and b.crt_date = ? ";
		sqlCmd += "union ";
		sqlCmd += "select '3' as data_type, "; //VD卡人
		sqlCmd += "       a.id_no, ";
		sqlCmd += "       0 as tmp_revolve_int_rate, ";
		sqlCmd += "       0 as tmp_line_of_credit_amt, ";
		sqlCmd += "       '' as tmp_autopay_acct_no, ";
		sqlCmd += "       '' as tmp_autopay_indicator, ";
		sqlCmd += "       '' as credit_rate, ";
		sqlCmd += "       '' as tmp_corp_p_seqno, ";
		sqlCmd += "       b.stat_unprint_flag, ";
		sqlCmd += "       b.stat_send_internet, ";
		sqlCmd += "       b.bill_apply_flag, ";
		sqlCmd += "       a.birthday, ";
		sqlCmd += "       0 as tmp_acct_jrnl_bal, ";
		sqlCmd += "       '' as tmp_payment_rate1, ";
		sqlCmd += "       '' as tmp_acct_status, ";
		sqlCmd += "       a.eng_name, ";
		sqlCmd += "       a.chi_name  ";
		sqlCmd += "  from dbc_idno a,dba_acno b ";
		sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
		sqlCmd += "   and a.crt_date = ? ";
		  
		setString(1, hSysDate);
		setString(2, hSysDate);
		setString(3, hSysDate);
		setString(4, hSysDate);
		
		int llCnt = selectTable();
		for (int ii = 0; ii < llCnt; ii++) {
			hDataType = colSs(ii, "data_type");
			hIdNo = colSs(ii, "id_no");
			hRevolveIntRate = colNum(ii, "tmp_revolve_int_rate");
			hAmt = colSs(ii, "tmp_line_of_credit_amt");
			hAmt = String.format("%09d", Long.parseLong(hAmt));
			hActAcno = colSs(ii, "tmp_autopay_acct_no");
			hIndicator = colSs(ii, "tmp_autopay_indicator");
			hPercentage = colSs(ii, "credit_rate");
			hCorpPSeqno = colSs(ii, "tmp_corp_p_seqno");
			hStatUnprintFlag = colSs(ii, "stat_unprint_flag");
			hStatSendInternet = colSs(ii, "stat_send_internet");
			hBillApplyFlag = colSs(ii, "bill_apply_flag");
			
			hBirthday = colSs(ii, "birthday");
			if(!hBirthday.equals("")) {
				hBirthday = String.format("%07d", Long.parseLong(commDate.toTwDate(hBirthday)));
			}
			
			hAcctJrnlBal = colInt(ii, "tmp_acct_jrnl_bal");
			hPaymentRate1 = colSs(ii, "tmp_payment_rate1");
			hAcctStatus = colSs(ii, "acct_status");
			hEngName = colSs(ii, "eng_name");
			hChiName = colSs(ii, "chi_name");
			
			if(hDataType.equals("1")||hDataType.equals("3")) {
				selectPtrRcrate();
			}
			
			selectCrdRela();
			
			if(hDataType.equals("2")) {
				selectActACno();
			}			
			
			if(hStatUnprintFlag.equals("Y")) {
				fileBillApplyFlag = "0005";
			}
			else if(hStatSendInternet.equals("Y")) {
				fileBillApplyFlag = "0004";
			}
			else {
				if(hBillApplyFlag.equals("1")) {
					fileBillApplyFlag = "0001";
				}
				else if(hBillApplyFlag.equals("2")) {
					fileBillApplyFlag = "0002";
				}
				else if(hBillApplyFlag.equals("3")) {
					fileBillApplyFlag = "0003";
				}
				else {
					fileBillApplyFlag = "0002";
				}
			}
			
			if(hAcctJrnlBal > 0) {
				fileAcctJrnlBal = String.format("%09d", hAcctJrnlBal);
			}
			else {
				fileAcctJrnlBal = String.format("%09d", 0);
			}
			
			if(!hPaymentRate1.equals("")&&!hPaymentRate1.equals("0A")&&!hPaymentRate1.equals("0B")&&
				!hPaymentRate1.equals("0C")&&!hPaymentRate1.equals("0D")&&!hPaymentRate1.equals("0E")) {
				filePaymentRateFlag = "1";
			}
			else {
				filePaymentRateFlag = "";
			}
			
			if(hDataType.equals("1")||hDataType.equals("3")) {
				if(hAcctStatus.equals("4")) {
					tmpSupType = "4";
				}
				
				tmpSupType = hDataType;
			}
			else {
				tmpSupType = hDataType;
			}
			
			writeTextFile();
		}
	}


	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		ilFile00 = openOutputText(lsTemp, "MS950");
		if (ilFile00 < 0) {
			printf("CRU23B1-TYPE-00 產檔失敗 ! ");
			errExit(1);
		}
	}
	
	void selectPtrRcrate() throws Exception {
		tmpRate = "";
		
	    sqlCmd = "select rcrate_day, rcrate_year, lpad(cast(cast(rcrate_year * 100 as decimal(4,0)) as varchar), 4, '0') as rate";
	    sqlCmd += " from ptr_rcrate  ";
	    sqlCmd += " where rcrate_day = ? ";
	    setDouble(1, hRevolveIntRate);
	    int tmpInt = selectTable();
	    if (tmpInt > 0) {
	    	tmpRate = getValue("rate");
	    }
	}	
	
	void selectCrdRela() throws Exception {
		crdRelaCnt = 0;
		
	    sqlCmd = "select count(*) as cnt";
	    sqlCmd += " from crd_rela  ";
	    sqlCmd += " where rela_id = ? ";
	    setString(1, hIdNo);
	    int tmpInt = selectTable();
	    if (tmpInt > 0) {
	    	crdRelaCnt = getValueInt("cnt");
	    }
	    
	    if(crdRelaCnt > 0) {
	    	tmpRelaFlag = "Y";
	    }
	    else {
	    	tmpRelaFlag = "N";
	    }
	}	
	
	void selectActACno() throws Exception {

		hOrg306Amt = "0";
		
	    sqlCmd = "select line_of_credit_amt as corp_line_of_credit_amt ";
	    sqlCmd += " from act_acno  ";
	    sqlCmd += " where corp_p_seqno = ? ";
	    sqlCmd += " and acno_flag = '2' ";
	    setString(1, hCorpPSeqno);
	    int tmpInt = selectTable();
	    if (tmpInt > 0) {
	    	hOrg306Amt = getValue("corp_line_of_credit_amt");
	    }
	}
	
	void writeTextFile() throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String tempStr = "", newLine = "\r\n";
			tempBuf.append("00"); // --代碼 固定 00
			tempBuf.append(comc.fixLeft("", 16)); //卡號
			tempBuf.append(comc.fixLeft(hIdNo, 11)); //主身分證 11 碼
			tempBuf.append(comc.fixLeft("", 1)); //債協註記
			tempBuf.append(comc.fixLeft(tmpRate, 4)); //循環利用利率
			tempBuf.append(comc.fixLeft(hAmt, 9)); //卡人信用額度
			tempBuf.append(comc.fixLeft(hActAcno, 13)); //委託扣繳帳號
			tempBuf.append(comc.fixLeft(commString.empty(hActAcno)?"0":"1", 1)); //自動轉帳註記
			tempBuf.append(comc.fixLeft(!commString.empty(hActAcno)&&"1".equals(hIndicator)?"10"
					:!commString.empty(hActAcno)&&"2".equals(hIndicator)?"20":"00", 2));  //扣繳額度
			tempBuf.append(comc.fixLeft(hPercentage, 2)); //預借現金成數
			hOrg306Amt = String.format("%09d", Long.parseLong(hOrg306Amt));			
			tempBuf.append(comc.fixLeft(hOrg306Amt, 9)); //ORG306額度
			hOrg506Amt = String.format("%09d", Long.parseLong(hOrg506Amt));
			tempBuf.append(comc.fixLeft(hOrg506Amt, 9)); //ORG506額度
			tempBuf.append(comc.fixLeft("", 1)); //個人E購卡註記
			tempBuf.append(comc.fixLeft("", 2)); //前置協商註記
			tempBuf.append(comc.fixLeft("00", 2)); //提供擔保註記
			tempBuf.append(comc.fixLeft(tmpRelaFlag, 1)); //保證人註記
			tempBuf.append(comc.fixLeft(fileBillApplyFlag, 4)); //帳單寄送註記
			tempBuf.append(comc.fixLeft(hBirthday, 7)); //客戶生日
			tempBuf.append(comc.fixLeft("", 1)); //弱勢展延註記
			tempBuf.append(comc.fixRight(fileAcctJrnlBal, 9)); //現欠金額
			tempBuf.append(comc.fixLeft(filePaymentRateFlag, 1)); //繳款註記
			tempBuf.append(comc.fixLeft(tmpSupType , 1)); //正附卡註記
			tempBuf.append(comc.fixLeft(commString.left(hEngName,26), 26)); //持卡人英文姓名
			tempBuf.append(comc.fixLeft(commString.left(hChiName,6), 12)); //持卡人中文姓名
			tempBuf.append(comc.fixLeft("", 2));
			tempBuf.append(newLine);
			totalCnt++;		
		this.writeTextFile(ilFile00, tempBuf.toString());
	}

	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
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
		String tmpstr1 = String.format("%s/media/crdb/%s", getEcsHome(), isFileName);
		String tmpstr2 = String.format("%s/media/crdb/backup/%s", getEcsHome(), isFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + isFileName + "] 已移至 [" + tmpstr2 + "]");
	}

}
