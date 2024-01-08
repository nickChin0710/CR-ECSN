/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/05/15  V1.00.01    Pino      for TCB layout                            *
 *  109/07/03  V1.00.02    Zuwei     coding standard, rename field method & format                   *
 *  109/07/22  V1.00.04    shiyuqi   coding standard,                          *
 *  109/10/19  V1.00.05    JeffKung  ref_ip_code set to NCR2TCB                *
 *  109-10-19  V1.00.06    shiyuqi   updated for project coding standard       *
 *  109-11-17  V1.00.07    Justin    isnull -> nvl                             *
 *  109-12-30  V1.00.08    Zuwei     “icbcecs”改為”system”                      *
 *  111/10/25  V1.00.09    JeffKung  phaseIII 財金請款檔VD回存資料處理                     *
 *  111/11/21  V1.00.10    JeffKung  非營業日不處理的條件變更                                        *
 ******************************************************************************/

package Dba;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCpi;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*產生送IBMDEBIT回存檔處理程式*/
public class DbaA009 extends AccessDAO {
  private final String progname = "產生VD回存檔處理程式  111/11/21 V1.00.10";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCpi comcpi = new CommCpi();
  CommCrdRoutine comcr = null;
  CommFTP commFTP = null;
  CommRoutine comr = null;

  final boolean debug = true;
  String rptNameMemo3 = "";
  String buf = "";
  String hCallBatchSeqno = "";

  String hBusiBusinessDate = "";
  String hTempBusinessDate = "";
  String hVouchChiDate = "";
  String hDaajReferenceNo = "";
  String hDaajCardNo = "";
  String hDaajRowid = "";
  String hDaajPSeqno = "";
  String hDaajAcctType = "";
  String hDaajAcctNo = "";
  String hDaajAdjustType = "";
  String hDaajPostDate = "";
  double hDaajOrginalAmt = 0;
  double hDaajDrAmt = 0;
  double hDaajCrAmt = 0;
  double hDaajBefAmt = 0;
  double hDaajAftAmt = 0;
  double hDaajBefDAmt = 0;
  double hDaajAftDAmt = 0;
  String hDaajAcctCode = "";
  String hDaajFuncCode = "";
  String hDaajCashType = "";
  String hDaajValueType = "";
  String hDaajAdjReasonCode = "";
  String hDaajAdjComment = "";
  String hDaajCDebtKey = "";
  String hDaajDebitItem = "";
  String hDaajAprFlag = "";
  String hDaajJrnlDate = "";
  String hDaajJobCode = "";
  String hDaajVouchJobCode = "";
  String hDaajTxnCode = "";
  String hDaajPurchaseDate = "";
  String hDaaoAcctHolderId = "";
  String hDaajVouchFlag = "";
  String hDbilMchtChiName = "";
  String dbilMchtChiName = "";
  String dbilMchtEngName = "";
  String hDbcElectronicCode = "";
  String hElectronicCardNo = "";
  int hTempCnt = 0;
  String hDetlDeductDate = "";
  String hDpteSummaryCode = "";
  String hDpteCommentCode = "";
  String hDpteTxnComment = "";
  int hDetlSendCnt = 0;
  double hDetlSendAmt = 0;
  int seqno = 0;
  String hIndex = "";
  String hDaajDeductSeq = "";
  // String h_dadt_reference_seq = "";
  double hTempDrAmt = 0;
  String hDadtRowid = "";
  String hDadtItemPostDate = "";
  String hDadtBillType = "";
  String hDadtAcctMonth = "";
  String hDadtStmtCycle = "";
  String hDadtIdPSeqno = "";
  double hDadtBegBal = 0;
  double hDadtEndBal = 0;
  double hDadtDAvailableBal = 0;
  String hDadtAcctCode = "";
  String hDajlCreateDate = "";
  String hDajlCreateTime = "";
  String hDadtInterestDate = "";
  String hRealAcNo = "";
  String hGsvhDbcr = "";
  String hVoucMemo1 = "";
  String hVoucMemo2 = "";
  String hVoucMemo3 = "";
  // String h_gsvh_std_vouch_cd = "";
  String hVoucAcNo = "";
  int hGsvhDbcrSeq = 0;
  double hVoucAmt = 0;
  String hTempAcBriefName = "";
  String hMemo3FlagCom = "";
  String hBusinssChiDate = "";
  String hPrintName = "";
  String hRptName = "";
  String hDarsRebatesCode = "";
  String hDarsMemo2Data = "";
  double hDarsRebatesAmt = 0;

  String filename = "";
  String tmpstr = "";
  String tempDate = "";
  String temstr1 = "";
  String vocTmp = "";
  String swPrint = "";
  String chiDate = "";
  String hTempId = "";
  String hTempMemo3 = "";
  String hVouchCdKind = "";
  String hAccmAcNo = "";
  String hAccmMemo3Flag = "";
  String hAccmMemo3Kind = "";
  String hAccmDrFlag = "";
  String hAccmCrFlag = "";
  String hTAcNo = "";
  String hTMemo3Kind = "";
  String hTMemo3Flag = "";
  String hTDbcr = "";
  String hTCrFlag = "";
  String hTDrFlag = "";
  String hTMemo3 = "";
  String currDate = "";
  String currTime = "";
  int vouchPageCnt = 0;
  int intCmd = 0;
  int debtFlag = 0;
  int enqNo = 0;
  long tmpAmt;
  int totCnt = 0;
  int vouchCnt = 0;
  int hTSeqno = 0;
  int inCnt = 0;
  double[] tTotalAmt = new double[12];

  VDRefund vdRefund = new VDRefund();
  // int out = -1;

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0 && args.length != 1) {
				comc.errExit("Usage : DbaA009 ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();

			// 檢查是否要執行
			String runFlag = runCheck(args);
			if ("Y".equals(runFlag)) {

				hDetlSendCnt = 0;
				hDetlSendAmt = 0;

				checkOpen();

				selectDbaAcaj();

				checkClose();

				if (hDetlSendCnt > 0)
					insertDbaDeductCtl();

				showLogMessage("I", "", " =============================================== ");
				showLogMessage("I", "", "  DEBIT 回存檔案:");
				showLogMessage("I", "", "      首筆 1筆, 尾筆 1筆");
				showLogMessage("I", "", String.format("      本日總筆數 [%d]", hDetlSendCnt));
				showLogMessage("I", "", String.format("      本日總金額 [%f]", hDetlSendAmt));
				showLogMessage("I", "", " =============================================== ");
			}

			// ==============================================
			// 固定要做的
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
    hBusiBusinessDate = "";
    hTempBusinessDate = "";

    sqlCmd = "select business_date,";
    sqlCmd += "to_char(to_date(business_date,'yyyymmdd')+1 days,'yyyymmdd') h_temp_business_date,";
    sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) h_vouch_chi_date ";
    sqlCmd += " from ptr_businday  ";
    sqlCmd += " fetch first 1 rows only ";
    selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    hBusiBusinessDate = getValue("business_date");
    hTempBusinessDate = getValue("h_temp_business_date");
    hVouchChiDate = getValue("h_vouch_chi_date");

  }

	/***********************************************************************
	 * 檢查是否符合執行的條件
	 */
	String runCheck(String[] args) throws Exception {
		String runFlag = "Y";

		/*  //20221121改成今日非營業日不處理
		if (args.length == 0) {
			tempDate = comcr.increaseDays(hBusiBusinessDate, 1);
			if (tempDate.equals(hTempBusinessDate) == false) {
				showLogMessage("I", "", "明日非營業日,本程式不處理");
				runFlag = "N";
				return runFlag;
			}
		} else {
			hBusiBusinessDate = args[0];
			hTempBusinessDate = args[0];
		}
		*/
		
		if (args.length == 0) {
			if(checkHoliday()) {
				showLogMessage("I", "", "今日["+ hBusiBusinessDate +"]非營業日,本程式不處理");
				runFlag = "N";
				return runFlag;
			} 
		} else {
				hBusiBusinessDate = args[0];
				hTempBusinessDate = args[0];
		}

		if (selectDbaDeductCtl1() != 0) {
			showLogMessage("I", "", "本日[ " + hTempBusinessDate + "]扣款資料已送出, 不可再送扣款資料 error");
			runFlag = "N";
		} else if (selectDbaDeductCtl() != 0) {
			showLogMessage("I", "", "[" + hDetlDeductDate + "]扣款資料尚未送回, 不可再送扣款資料 error");
			runFlag = "N";
		}

		return runFlag;
	}

	/***********************************************************************/
	boolean checkHoliday() throws Exception {

		sqlCmd = "select holiday ";
		sqlCmd += " from ptr_holiday  ";
		sqlCmd += "where holiday = ? ";
		setString(1, hBusiBusinessDate);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			return true;
		}
		return false;
	}

	/***********************************************************************/
	int selectDbaDeductCtl1() throws Exception {
		hTempCnt = 0;
		sqlCmd = "select count(*) h_temp_cnt ";
		sqlCmd += " from dba_deduct_ctl  ";
		sqlCmd += "where deduct_date = ?  ";
		sqlCmd += "and proc_type = 'B' ";
		setString(1, hTempBusinessDate);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTempCnt = getValueInt("h_temp_cnt");
		}

		return (hTempCnt);
	}

	/***********************************************************************/
	int selectDbaDeductCtl() throws Exception {
		hDetlDeductDate = "";
		sqlCmd = "select deduct_date ";
		sqlCmd += " from dba_deduct_ctl  ";
		sqlCmd += "where receive_date =''  ";
		sqlCmd += "and proc_type = 'B'  ";
		sqlCmd += "fetch first 1 rows only ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hDetlDeductDate = getValue("deduct_date");
		} else
			return (0);
		return (1);
	}


	/***********************************************************************/
	void selectDbaAcaj() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "a.p_seqno, a.acct_type, a.acct_no,";
		sqlCmd += "a.adjust_type,a.reference_no,a.post_date,";
		sqlCmd += "a.orginal_amt,a.dr_amt,a.cr_amt,";
		sqlCmd += "a.bef_amt,a.aft_amt,a.bef_d_amt,";
		sqlCmd += "a.aft_d_amt,a.acct_code,a.func_code,";
		sqlCmd += "a.card_no,a.cash_type,a.value_type,";
		sqlCmd += "a.adj_reason_code,a.adj_comment,a.c_debt_key,";
		sqlCmd += "a.debit_item,a.apr_flag,a.jrnl_date,a.job_code,";
		sqlCmd += "decode(a.vouch_job_code,'','00',a.vouch_job_code) h_daaj_vouch_job_code,";
		sqlCmd += "a.txn_code,a.purchase_date,a.vouch_flag,";
		sqlCmd += "decode(b.corp_p_seqno,'',b.acct_holder_id,nvl(uf_corp_no(b.corp_p_seqno),'')) h_daao_acct_holder_id,";
		sqlCmd += "a.rowid as rowid ";
		sqlCmd += "from dba_acno b,dba_acaj a ";
		sqlCmd += "where apr_flag = 'Y' ";
		sqlCmd += "and a.p_seqno = b.p_seqno ";
		//sqlCmd += "and a.acct_no = b.acct_no ";   //取消這個條件
		sqlCmd += "and a.acct_no <> '' ";
		sqlCmd += "and a.adjust_type in ('DP01','DE19','VDUC','VDUD','VDUL','VDUR','FD10') ";
		sqlCmd += "and a.proc_flag = 'N' ";

		openCursor();
		while (fetchTable()) {

			hDaajPSeqno = getValue("p_seqno");
			hDaajAcctType = getValue("acct_type");
			hDaajAcctNo = getValue("acct_no");
			hDaajAdjustType = getValue("adjust_type");
			hDaajReferenceNo = getValue("reference_no");
			hDaajPostDate = getValue("post_date");
			hDaajOrginalAmt = getValueDouble("orginal_amt");
			hDaajDrAmt = getValueDouble("dr_amt");
			hDaajCrAmt = getValueDouble("cr_amt");
			hDaajBefAmt = getValueDouble("bef_amt");
			hDaajAftAmt = getValueDouble("aft_amt");
			hDaajBefDAmt = getValueDouble("bef_d_amt");
			hDaajAftDAmt = getValueDouble("aft_d_amt");
			hDaajAcctCode = getValue("acct_code");
			hDaajFuncCode = getValue("func_code");
			hDaajCardNo = getValue("card_no");
			hDaajCashType = getValue("cash_type");
			hDaajValueType = getValue("value_type");
			hDaajAdjReasonCode = getValue("adj_reason_code");
			hDaajAdjComment = getValue("adj_comment");
			hDaajCDebtKey = getValue("c_debt_key");
			hDaajDebitItem = getValue("debit_item");
			hDaajAprFlag = getValue("apr_flag");
			hDaajJrnlDate = getValue("jrnl_date");
			hDaajJobCode = getValue("job_code");
			hDaajVouchJobCode = getValue("h_daaj_vouch_job_code");
			hDaajTxnCode = getValue("txn_code");
			hDaajPurchaseDate = getValue("purchase_date");
			hDaaoAcctHolderId = getValue("h_daao_acct_holder_id");
			hDaajVouchFlag = getValue("vouch_flag");
			hDaajRowid = getValue("rowid");

			if ("DE19".equals(hDaajAdjustType)) {
				hDpteSummaryCode = "VDRD";  //退貨回存
			} else if ("FD10".equals(hDaajAdjustType)) {
				hDpteSummaryCode = "VDDS";  //現金回饋
			} else if (comc.getSubString(hDaajAdjustType,0,2).equals("VD")) {
				hDpteSummaryCode = hDaajAdjustType;
			} else {
				hDpteSummaryCode = "VDRD"; //問交回存或爭議款回存
			}
			
			/*摘要代碼直接寫死對應
			if (selectDbpTxnCode() != 0) {
				showLogMessage("I", "",
						String.format("tx_code error reference_no[%s] tx_code[%s]", hDaajReferenceNo, hDaajTxnCode));
				continue;
			}
			*/
			
			updateDbaAcaj(0);
			writeMediaFile();
		}
		closeCursor();
	}
  /***********************************************************************/
  int selectDbpTxnCode() throws Exception {
    hDpteSummaryCode = "";
    hDpteCommentCode = "";
    hDpteTxnComment = "";

    sqlCmd = "select summary_code,";
    sqlCmd += "comment_code,";
    sqlCmd += "txn_comment ";
    sqlCmd += " from dbp_txn_code  ";
    sqlCmd += "where txn_code = ? ";
    setString(1, hDaajTxnCode);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDpteSummaryCode = getValue("summary_code");
      hDpteCommentCode = getValue("comment_code");
      hDpteTxnComment = getValue("txn_comment");
    } else
      return (1);
    return (0);
  }

  /***********************************************************************/
  void updateDbaAcaj(int hIndex) throws Exception {
    String tmpstr = "";

    if (hIndex == 0) {
      tmpstr = String.format("%010.0f", getDebitSeq());
      hDaajDeductSeq = tmpstr;
    }

    daoTable = "dba_acaj";
    updateSQL = "deduct_seq  = ?,";
    updateSQL += " deduct_amt  = ?,";
    updateSQL +=
        " proc_flag = decode(cast(? as int), 0, '0', 1, '1', 2, '2', 9, '9', 10, 'A', '3'),";
    updateSQL += " deduct_date  = ?,";
    updateSQL += " deduct_proc_code = '99',";
    updateSQL += " mod_pgm   = ?,";
    updateSQL += " mod_time   = sysdate";
    whereStr = "where rowid   = ? ";
    setString(1, hIndex == 0 ? hDaajDeductSeq : "");
    setDouble(2, hIndex == 0 ? hDaajDrAmt : 0);
    setInt(3, hIndex);
    setString(4, hBusiBusinessDate);
    setString(5, javaProgram);
    setRowId(6, hDaajRowid);
    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_dba_acaj not found!", "", hCallBatchSeqno);
    }

  }

  /***********************************************************************/
  @SuppressWarnings("unused")
  void writeMediaFile() throws Exception {
    String tmpstr1 = "";
    String tmpstr2 = "";
    String tmpstr3 = "";
    int intS = 0;
    int intd = 0;

    hDetlSendCnt++;
    hDetlSendAmt = hDetlSendAmt + hDaajDrAmt;

    selectDbbBill();
    selectElectronicCardNo();

    if (hDetlSendCnt == 1) {
      tmpstr = String.format("1006%8.8s%188.188s", hBusiBusinessDate, " ");

      tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
      writeBinFile(tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    }
    vdRefund.type = "2"; // 固定值: 2
    vdRefund.bank = "006"; // 固定值: 006
    // C01:退貨回存 C02:現金回饋回存 C03:調整回存 C04:帳戶入帳
    if (hDaajAdjustType.equals("DE19")) {
      vdRefund.adjustType = "C01";
    } else if (hDaajAdjustType.equals("FD10") || hDaajAdjustType.equals("SH10")
        || hDaajAdjustType.equals("TO10")) {
      vdRefund.adjustType = "C02";
    } else if (comc.getSubString(hDaajAdjustType,0,2).equals("VD")) {
      vdRefund.adjustType = "C04";
    } else {
      vdRefund.adjustType = "C03";
    }
    vdRefund.purchaseDate = hDaajPurchaseDate;
    vdRefund.amt = hDaajDrAmt;
    vdRefund.acctNo = hDaajAcctNo;
    vdRefund.cardNo = hDaajCardNo;
    vdRefund.deductSeq = hDaajDeductSeq;
    vdRefund.acctHolderId = hDaaoAcctHolderId;
    vdRefund.respondCode = "";
    vdRefund.summaryCode = hDpteSummaryCode;
    vdRefund.electronicCardNo = hElectronicCardNo;
    vdRefund.engDesc = dbilMchtEngName;
    vdRefund.chiDesc = comcpi.commTransChinese(dbilMchtChiName);
    tmpstr = vdRefund.allText() + "\r\n";
    writeBinFile(tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);

  }

  /***********************************************************************/
  void selectElectronicCardNo() throws Exception {
    hDbcElectronicCode = "";
    hElectronicCardNo = "";

    sqlCmd = "select electronic_code ";
    sqlCmd += " from dbc_card ";
    sqlCmd += " where card_no = ? ";
    setString(1, hDaajCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDbcElectronicCode = getValue("electronic_code");
    }

    if (hDbcElectronicCode.equals("01")) {
      sqlCmd = "select nvl(tsc.tsc_card_no, '') as electronic_card_no ";
      sqlCmd += " from dbc_card as c INNER JOIN tsc_vd_card as tsc ";
      sqlCmd += " on c.electronic_code='01' and  c.card_no = tsc.vd_card_no ";
      sqlCmd += " where c.card_no = ? ";
      setString(1, hDaajCardNo);
      recordCnt = selectTable();
      if (recordCnt > 0) {
        hElectronicCardNo = getValue("electronic_card_no");
      }
    }

  }

  /***********************************************************************/
  void selectDbbBill() throws Exception {
    hDbilMchtChiName = "";
    dbilMchtChiName = "";
    dbilMchtEngName = "";

    sqlCmd =
        "select decode(dest_curr,source_curr,mcht_chi_name,mcht_eng_name) h_dbil_mcht_chi_name,";
    sqlCmd += " mcht_chi_name, ";
    sqlCmd += " mcht_eng_name ";
    sqlCmd += " from dbb_bill  ";
    sqlCmd += "where reference_no = ? ";
    setString(1, hDaajReferenceNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hDbilMchtChiName = getValue("h_dbil_mcht_chi_name");
      dbilMchtChiName = getValue("mcht_chi_name");
      dbilMchtEngName = getValue("mcht_eng_name");
    }

  }
  /***************************************************************************/
  void checkClose() throws Exception {
    if (hDetlSendCnt > 0) {
      String tmpstr = String.format("3006%8.8s%010d%014.0f%164.164s", hBusiBusinessDate,
          hDetlSendCnt, hDetlSendAmt, " ");

      tmpstr = comc.fixLeft(tmpstr, 200) + "\r\n";
      writeBinFile(tmpstr.getBytes("MS950"), tmpstr.getBytes("MS950").length);
    } else {
      showLogMessage("I", "", "本日沒有回存資料\n");
    }

    closeBinaryOutput();
    
    //傳送檔案至指定路徑
    if (hDetlSendCnt > 0) {
    	procFTP(String.format("VDREFUND_REQ.%8.8s", hBusiBusinessDate)); 
    }
    
  }

  /***********************************************************************/
  void checkOpen() throws Exception {
    filename =
        String.format("%s/media/dba/VDREFUND_REQ.%8.8s", comc.getECSHOME(), hBusiBusinessDate);
    filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
    if (openBinaryOutput(filename) == false) {
      comcr.errRtn(filename, "檔案開啓失敗！", hCallBatchSeqno);
    }

    swPrint = "N";
  }

  /***********************************************************************/
  void insertDbaDeductCtl() throws Exception {
    setValue("proc_type", "B");
    setValue("deduct_date", hBusiBusinessDate);
    setValue("crt_date", hBusiBusinessDate);
    setValue("crt_time", sysTime);
    setValueInt("send_cnt", hDetlSendCnt);
    setValueDouble("send_amt", hDetlSendAmt);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", javaProgram);
    daoTable = "dba_deduct_ctl";
    insertTable();
    if (dupRecord.equals("Y")) {
      comcr.errRtn("insert_dba_deduct_ctl duplicate!", "", hCallBatchSeqno);
    }

  }

  void procFTP(String isFileName) throws Exception {
	  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEriaLocalDir = String.format("%s/media/dba", comc.getECSHOME());
      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEflgModPgm = javaProgram;
      

      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
      showLogMessage("I", "", "put " + isFileName + " 開始傳送....");
      int errCode = commFTP.ftplogName("NCR2TCB", "put " + isFileName);
      
      if (errCode != 0) {
          showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料"+" errcode:"+errCode);
          insertEcsNotifyLog(isFileName);          
      } else {
    	  comc.fileRename2(String.format("%s/media/dba/", comc.getECSHOME())+isFileName,String.format("%s/media/dba/backup/", comc.getECSHOME())+isFileName);
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
 
  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbaA009 proc = new DbaA009();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

  /** *********************************************************************/

  double getDebitSeq() throws Exception {
    double seqno = 0;

    sqlCmd = "select dba_txnseq.nextval as nextval from dual";
    selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("GetDebitSeq() not found!", "", hCallBatchSeqno);
    }

    seqno = getValueDouble("nextval");
    hDaajDeductSeq = String.format("%010.0f", seqno);

    return (seqno);
  }

  class VDRefund {
    String type; // 明細資料
    String bank; // 銀行代碼
    String adjustType; // 交易代碼
    String purchaseDate; // 交易日期
    double amt; // 金額
    String acctNo; // 金融帳號
    String cardNo; // 卡號
    String deductSeq; // 回存流水號
    String acctHolderId; // 法人戶統一編號 / 帳戶歸屬的ID
    String respondCode; // 處理回覆碼
    String summaryCode; // 存摺摘要代碼
    String electronicCardNo; // 票證外顯卡號
    String space; // 保留
    String engDesc; // 交易英文說明
    String chiDesc; // 交易中文說明

    String allText() throws UnsupportedEncodingException {
      String rtn = "";
      rtn += comc.fixLeft(type, 1);
      rtn += comc.fixLeft(bank, 3);
      rtn += comc.fixLeft(adjustType, 3);
      rtn += comc.fixLeft(purchaseDate, 8);
      rtn += String.format("%012.0f", amt);
      rtn += comc.fixLeft(acctNo, 13);
      rtn += comc.fixLeft(cardNo, 16);
      rtn += comc.fixLeft(deductSeq, 15);
      rtn += comc.fixLeft(acctHolderId, 10);
      rtn += comc.fixLeft(respondCode, 2);
      rtn += comc.fixLeft(summaryCode, 4);
      rtn += comc.fixLeft(electronicCardNo, 16);
      rtn += comc.fixLeft(space, 65);
      rtn += comc.fixLeft(engDesc, 16);
      rtn += fixLeftAll(chiDesc, 16);

      return rtn;
    }
  }

  String fixLeftAll(String str, int len) throws UnsupportedEncodingException {
    int size = (Math.floorDiv(len, 100) + 1) * 100;
    String spc = "";
    for (int i = 0; i < size; i++)
      spc += "　";
    if (str == null)
      str = "";
    str = str + spc;
    byte[] bytes = str.getBytes("MS950");
    byte[] vResult = new byte[len];
    System.arraycopy(bytes, 0, vResult, 0, len);

    return new String(vResult, "MS950");
  }
}
