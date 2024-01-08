/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109-10-19  V1.00.01    shiyuqi       updated for project coding standard     *
 *  109-12-30  V1.00.02    Zuwei       “icbcecs”改為”system”     *
 *  111/04/01  V1.00.03    JeffKung for TCB業務邏輯                       *
 *  111/10/25  V1.00.04    JeffKung  退貨回存交易恢復處理 (DE19)                 *
 *  112/11/12  V1.00.05    JeffKung  非營業日不處理
 ******************************************************************************/

package Dba;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*扣款銷帳科目表處理程式*/
public class DbaA005 extends AccessDAO {
    private String progname = "DEBIT卡帳務調整處理程式 112/11/12 V1.00.05";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    boolean debugMode = false;

    String rptNameMemo3        = "";
    String buf                  = "";
    String szTmp                = "";
    String hGsvhAcNo         = "";
    int    hGsvhDbcrSeq      = 0;
    String hRealAcNo         = "";
    String hGsvhDbcr          = "";
    String hVoucMemo1         = "";
    String hVoucMemo2         = "";
    String hVoucMemo3         = "";
    String hGsvhStdVouchCd  = "";
    String hVoucAcNo         = "";
    double hVoucAmt           = 0;
    String hAccmMemo3Kind    = "";
    String hAccmMemo3Flag    = "";
    String hAccmDrFlag       = "";
    String hAccmCrFlag       = "";
    String hTempAcBriefName = "";
    String hMemo3FlagCom     = "";

    String hDaajModUser      = "";
    String hDaajModPgm       = "";
    String swPrint             = "";
    String hBusiBusinessDate = "";
    String temstr               = "";

    String hDaajPSeqno          = "";
    String hDaajAcctType        = "";
    String hDaajAcctNo          = "";
    String hDaajAdjustType      = "";
    String hDaajReferenceNo     = "";
    String hDaajPostDate        = "";
    double hDaajDeductAmt       = 0;
    double hDaajOrginalAmt      = 0;
    double hDaajDrAmt           = 0;
    double hDaajCrAmt           = 0;
    double hDaajBefAmt          = 0;
    double hDaajAftAmt          = 0;
    double hDaajBefDAmt        = 0;
    double hDaajAftDAmt        = 0;
    String hDaajAcctItemEname  = "";
    String hDaajFunctionCode    = "";
    String hDaajCardNo          = "";
    String hDaajCashType        = "";
    String hDaajValueType       = "";
    String hDaajAdjReasonCode  = "";
    String hDaajAdjComment      = "";
    String hDaajCDebtKey       = "";
    String hDaajDebitItem       = "";
    String hDaajConfirmFlag     = "";
    String hDaajJrnlDate        = "";
    String hDaajJobCode         = "";
    String hDaajVouchJobCode   = "";
    String hDaajTransactionCode = "";
    String hDaajPurchaseDate    = "";
    String hDaajItemPostDate   = "";
    String hTempAdjustCode      = "";
    String hDaaoIdPSeqno       = "";
    String hDaaoAcctHolderId   = "";
    String hDaajVouchFlag       = "";
    String hDaajDeductDate      = "";
    String hDaajDeductProcDate = "";
    String hDaajRowid            = "";

    double[] tTotalAmt               = new double[13];
    double[] dTotalAmt               = new double[13];
    int      inta                      = 0;
    String   hGsvhMemo2              = "";
    String   hGsvhMemo3              = "";
    String   hTempMemo3              = "";
    String   hVoucRefno              = "";
    String   hVoucIdNo              = "";
    long     totCount                 = 0;
    int      debtFlag                 = 0;
    String   hCallBatchSeqno        = "";
    double   tmpAmt                   = 0;
    String   vocTmp                   = "";
    String   hVouchCdKind           = "";
    String   hGsvhModPgm            = "";
    String   hTempTransactionCode   = "";
    int      tempInt                  = 0;
    String   hGsvhMemo1              = "";
    String   hDbilReferenceNoFeeF = "";
    long     tempLong                 = 0;
    double   hBillDestinationAmt    = 0;

    String hDadtItemPostDate  = "";
    String hDadtBillType       = "";
    String hDadtAcctMonth      = "";
    String hDadtStmtCycle      = "";
    String hDadtIdPSeqno      = "";
    double hDadtBegBal         = 0;
    double hDadtEndBal         = 0;
    double hDadtDAvailableBal = 0;
    String hDadtAcctCode = "";
    String hDadtInterestDate   = "";
    String hDadtRowid           = "";
    String hDajlCreateDate     = "";
    String hDajlCreateTime     = "";
    int    enqNo                 = 0;
    String hTAcNo              = "";
    int    hTSeqno              = 0;
    String hTMemo3Kind         = "";
    String hTMemo3Flag         = "";
    String hTDbcr               = "";
    String hTCrFlag            = "";
    String hTDrFlag            = "";
    String hTMemo3              = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : DbaA005 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hDaajModUser = comc.commGetUserID();
            hDaajModPgm = javaProgram;
			selectPtrBusinday();

			// 檢查是否要執行
			String runFlag = runCheck(args);
			if ("Y".equals(runFlag)) {
				selectDbaAcaj();
			}

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

    /***************************************************************************/
    void selectDbaAcaj() throws Exception {

        sqlCmd = "  SELECT b.p_seqno, ";
        sqlCmd += "         b.acct_type, ";
        sqlCmd += "         b.acct_no, ";
        sqlCmd += "         b.adjust_type, ";
        sqlCmd += "         b.reference_no, ";
        sqlCmd += "         b.post_date, ";
        sqlCmd += "         b.deduct_amt, ";
        sqlCmd += "         b.orginal_amt, ";
        sqlCmd += "         b.dr_amt, ";
        sqlCmd += "         b.cr_amt, ";
        sqlCmd += "         b.bef_amt, ";
        sqlCmd += "         b.aft_amt, ";
        sqlCmd += "         b.bef_d_amt, ";
        sqlCmd += "         b.aft_d_amt, ";
        sqlCmd += "         b.acct_code, ";
        sqlCmd += "         b.func_code, ";
        sqlCmd += "         b.card_no, ";
        sqlCmd += "         b.cash_type, ";
        sqlCmd += "         b.value_type, ";
        sqlCmd += "         b.adj_reason_code, ";
        sqlCmd += "         b.adj_comment, ";
        sqlCmd += "         b.c_debt_key, ";
        sqlCmd += "         b.debit_item, ";
        sqlCmd += "         b.apr_flag, ";
        sqlCmd += "         b.jrnl_date, ";
        sqlCmd += "         b.job_code, ";
        sqlCmd += "         decode(b.vouch_job_code,'','00',b.vouch_job_code) h_daaj_vouch_job_code, ";
        sqlCmd += "         b.txn_code, ";
        sqlCmd += "         b.purchase_date, ";
        sqlCmd += "         b.item_post_date, ";
        sqlCmd += "         a.id_p_seqno,      ";
        sqlCmd += "         a.acct_holder_id, ";
        sqlCmd += "         b.vouch_flag, ";
        //sqlCmd += "         substr(to_char(to_number(b.deduct_date)- 19110000,'0000000'),2,7) h_daaj_deduct_date, ";
        //sqlCmd += "         to_char(to_date(b.deduct_date,'yyyymmdd')+1,'yyyymmdd') h_daaj_deduct_proc_date, ";
        sqlCmd += "         b.rowid rowid ";
        sqlCmd += "  FROM   dba_acno a,dba_acaj b ";
        sqlCmd += "  WHERE  apr_flag     = 'Y' ";
        sqlCmd += "  and    a.p_seqno        = b.p_seqno ";
        sqlCmd += "  and    deduct_proc_code = '00' ";
        sqlCmd += "  and    b.adjust_type   not in ('RE10','SH10') ";
        sqlCmd += "  and    proc_flag     = 'N' ";

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hDaajPSeqno = getValue("p_seqno");
            hDaajAcctType = getValue("acct_type");
            hDaajAcctNo = getValue("acct_no");
            hDaajAdjustType = getValue("adjust_type");
            hDaajReferenceNo = getValue("reference_no");
            hDaajPostDate = getValue("post_date");

            hDaajOrginalAmt = getValueDouble("orginal_amt");
            hDaajDrAmt = getValueDouble("dr_amt");
            hDaajCrAmt = getValueDouble("cr_amt");
            
            //hDaajDeductAmt = getValueDouble("deduct_amt");
            if (hDaajDrAmt==0) {
            	hDaajDeductAmt = hDaajCrAmt;
            } else {
            	hDaajDeductAmt = hDaajDrAmt;
            }
             
            hDaajBefAmt = getValueDouble("bef_amt");
            hDaajAftAmt = getValueDouble("aft_amt");
            hDaajBefDAmt = getValueDouble("bef_d_amt");
            hDaajAftDAmt = getValueDouble("aft_d_amt");
            hDaajAcctItemEname = getValue("acct_code");
            hDaajFunctionCode = getValue("func_code");
            hDaajCardNo = getValue("card_no");
            hDaajCashType = getValue("cash_type");
            hDaajValueType = getValue("value_type");
            hDaajAdjReasonCode = getValue("adj_reason_code");
            hDaajAdjComment = getValue("adj_comment");
            hDaajCDebtKey = getValue("c_debt_key");
            hDaajDebitItem = getValue("debit_item");
            hDaajConfirmFlag = getValue("apr_flag");
            hDaajJrnlDate = getValue("jrnl_date");
            hDaajJobCode = getValue("job_code");
            hDaajVouchJobCode = getValue("h_daaj_vouch_job_code");
            hDaajTransactionCode = getValue("txn_code");
            hDaajPurchaseDate = getValue("purchase_date");
            hDaajItemPostDate = getValue("item_post_date");
            
            if ("DE19".equals(hDaajAdjustType)) {
            	hTempAdjustCode = "3"; 
			} else if ("DP01".equals(hDaajAdjustType)) {
				hTempAdjustCode = "1"; 
			} else if ("FD10".equals(hDaajAdjustType)) {
				hTempAdjustCode = "3"; 
			} else if (comc.getSubString(hDaajAdjustType,0,2).equals("VD")) {
				hTempAdjustCode = "3"; 
			} else {
				hTempAdjustCode = "0"; 
			}
            hDaaoIdPSeqno = getValue("id_p_seqno");
            hDaaoAcctHolderId = getValue("acct_holder_id");
            hDaajVouchFlag = getValue("vouch_flag");
            //hDaajDeductDate = getValue("h_daaj_deduct_date");
            //hDaajDeductProcDate = getValue("h_daaj_deduct_proc_date");
            hDaajRowid = getValue("rowid");

            sqlCmd = " SELECT UF_IDNO_ID(id_p_seqno) id ";
            sqlCmd += "   FROM dbc_card ";
            sqlCmd += "  WHERE card_no = ? ";
            setString(1, hDaajCardNo);
            selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_dbc_card not found!", "", "card_no= ["+hDaajCardNo+"]");
            }
            hDaaoAcctHolderId = getValue("id");

            for (inta = 0; inta < 13; inta++)
                tTotalAmt[inta] = 0;
            for (inta = 0; inta < 13; inta++)
                dTotalAmt[inta] = 0;
            hGsvhMemo2 = "";
            totCount++;

            if (hTempAdjustCode.equals("0") || hTempAdjustCode.equals("1")) {
                debtFlag = 0;
                if (selectDbaDebt() != 0) {
                    if (selectDbaDebtHst() != 0) {
                        updateDbaAcaj(1);
                        showLogMessage("I", "", String.format("ERROR : 無法處理D檔資料![%s]", hDaajReferenceNo));
                        continue;
                    } else {
                        debtFlag = 1;
                    }
                }
            } else {
                selectDbbBill();
            }

            insertDbaJrnl();
            
            //"3"==DE19 (退貨交易) ; "1"=DP01 (爭議款回存)
            if (hTempAdjustCode.equals("3") || hTempAdjustCode.equals("1"))  {
            	;
            } else {
            	updateDbaAcaj(0);
            }
            
            if (hTempAdjustCode.equals("0") || hTempAdjustCode.equals("1"))  {
            	if (debtFlag==0  ) {
            		updateDbaDebt();
            	} else {
            		updateDbaDebtHst();
            	}
            }
        }
        closeCursor(cursorIndex);
    }

    /*****************************************************************************/
    void updateDbaDebt() throws Exception {
        daoTable = "dba_debt";
        updateSQL = "   d_avail_bal   = d_avail_bal - ?, ";
        updateSQL += "  end_bal   = ?,  ";
        updateSQL += "   mod_time          = sysdate, ";
        updateSQL += "   mod_user          = 'system', ";
        updateSQL += "   mod_pgm           = 'DbaA005' ";
        whereStr = "   where  rowid             = ? ";
        setDouble(1, hDaajDeductAmt);
        setDouble(2, hDaajAftDAmt);
        setRowId(3, hDadtRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dba_debt not found!", "", hCallBatchSeqno);
        }
    }

    /*****************************************************************************/
    void updateDbaDebtHst() throws Exception {
        daoTable = "dba_debt_hst";
        updateSQL = "  d_avail_bal   = d_avail_bal - ?,  ";
        updateSQL += "  mod_time          = sysdate, ";
        updateSQL += "  mod_user          = 'system', ";
        updateSQL += "  mod_pgm           = 'DbaA005'    ";
        whereStr = "  where  rowid             = ? ";
        setDouble(1, hDaajDeductAmt);
        setRowId(2, hDadtRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dba_debt_hst not found!", "", hCallBatchSeqno);
        }
    }

    /*****************************************************************************/
    int selectDbbBill() throws Exception {
        hDadtItemPostDate = "";
        hDadtBillType = "";
        hDadtAcctMonth = "";
        hDadtStmtCycle = "";
        hDadtInterestDate = "";
        hDadtAcctCode = "";
        hDbilReferenceNoFeeF = "";

        sqlCmd = " SELECT post_date, ";
        sqlCmd += "        bill_type, ";
        sqlCmd += "        acct_month, ";
        sqlCmd += "        stmt_cycle,  ";
        sqlCmd += "        acct_code, ";
        sqlCmd += "        reference_no_fee_f ";
        sqlCmd += " FROM   dbb_bill     ";
        sqlCmd += " WHERE  reference_no = ?  ";
        setString(1, hDaajReferenceNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDadtItemPostDate = getValue("item_post_date");
            hDadtBillType = getValue("bill_type");
            hDadtAcctMonth = getValue("acct_month");
            hDadtStmtCycle = getValue("stmt_cycle");
            hDadtAcctCode = getValue("acct_code");
            hDbilReferenceNoFeeF = getValue("reference_no_fee_f");
        } else
            return (1);
        return (0);
    }

    /*****************************************************************************/
    int selectDbaDebt() throws Exception {
        hDadtItemPostDate = "";
        hDadtBillType = "";
        hDadtAcctMonth = "";
        hDadtStmtCycle = "";
        hDadtIdPSeqno = "";
        hDadtBegBal = 0;
        hDadtEndBal = 0;
        hDadtDAvailableBal = 0;
        hDadtAcctCode = "";
        hDadtRowid = "";

        sqlCmd += " SELECT item_post_date, ";
        sqlCmd += "        bill_type, ";
        sqlCmd += "        acct_month, ";
        sqlCmd += "        stmt_cycle,  ";
        sqlCmd += "        id_p_seqno, ";
        sqlCmd += "        beg_bal, ";
        sqlCmd += "        end_bal, ";
        sqlCmd += "        d_avail_bal, ";
        sqlCmd += "        acct_code, ";
        sqlCmd += "        rowid rowid ";
        sqlCmd += " FROM   dba_debt     ";
        sqlCmd += " WHERE  reference_no = ?  ";
        setString(1, hDaajReferenceNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDadtItemPostDate = getValue("item_post_date");
            hDadtBillType = getValue("bill_type");
            hDadtAcctMonth = getValue("acct_month");
            hDadtStmtCycle = getValue("stmt_cycle");
            hDadtIdPSeqno = getValue("id_p_seqno");
            hDadtBegBal = getValueDouble("beg_bal");
            hDadtEndBal = getValueDouble("end_bal");
            hDadtDAvailableBal = getValueDouble("d_avail_bal");
            hDadtAcctCode = getValue("acct_code");
            hDadtRowid = getValue("rowid");
        } else
            return (1);
        return (0);
    }

    /*****************************************************************************/
    int selectDbaDebtHst() throws Exception {
        hDadtItemPostDate = "";
        hDadtBillType = "";
        hDadtAcctMonth = "";
        hDadtStmtCycle = "";
        hDadtIdPSeqno = "";
        hDadtBegBal = 0;
        hDadtEndBal = 0;
        hDadtDAvailableBal = 0;
        hDadtAcctCode = "";
        hDadtRowid = "";

        sqlCmd = " SELECT item_post_date, ";
        sqlCmd += "        bill_type, ";
        sqlCmd += "        acct_month, ";
        sqlCmd += "        stmt_cycle,  ";
        sqlCmd += "        id_p_seqno, ";
        sqlCmd += "        beg_bal, ";
        sqlCmd += "        end_bal, ";
        sqlCmd += "        d_avail_bal, ";
        sqlCmd += "        acct_code, ";
        sqlCmd += "        rowid rowid ";
        sqlCmd += " FROM   dba_debt_hst     ";
        sqlCmd += " WHERE  reference_no = ? ";
        setString(1, hDaajReferenceNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDadtItemPostDate = getValue("item_post_date");
            hDadtBillType = getValue("bill_type");
            hDadtAcctMonth = getValue("acct_month");
            hDadtStmtCycle = getValue("stmt_cycle");
            hDadtIdPSeqno = getValue("id_p_seqno");
            hDadtBegBal = getValueDouble("beg_bal");
            hDadtEndBal = getValueDouble("end_bal");
            hDadtDAvailableBal = getValueDouble("d_avail_bal");
            hDadtAcctCode = getValue("acct_code");
            hDadtRowid = getValue("rowid");
        } else
            return (1);
        return (0);
    }

    /*****************************************************************************/
    void insertDbaJrnl() throws Exception {
        hDajlCreateDate = sysDate;
        hDajlCreateTime = sysTime;
        enqNo++;

        daoTable = "dba_jrnl";
        setValue("crt_date", hDajlCreateDate);
        setValue("crt_time", hDajlCreateTime);
        setValueInt("enq_seqno", enqNo);
        setValue("p_seqno", hDaajPSeqno);
        setValue("acct_type", hDaajAcctType);
        setValue("acct_no", hDaajAcctNo);
        setValue("id_p_seqno", hDaaoIdPSeqno);
        setValue("acct_date", hBusiBusinessDate);
        setValue("tran_class", "C");
        String tmpstr = "";
        switch (hTempAdjustCode) {
        case "0":
            tmpstr = "DE";
            break;
        case "1":
            tmpstr = "DP";
            break;
        case "2":
            tmpstr = "RE";
            break;
        case "3":
            tmpstr = "DB";
            break;
        default:
            tmpstr = "TR";
            break;
        }
        setValue("tran_type", tmpstr);
        setValue("acct_code", hDadtAcctCode);
        setValue("dr_cr", "D");
        setValueDouble("transaction_amt", hDaajDrAmt);
        setValueDouble("jrnl_bal", 0);
        setValueDouble("item_bal", 0);
        double tmpdouble = 0;
        switch (hTempAdjustCode) {
        case "0":
            tmpdouble = hDadtDAvailableBal;
            break;
        case "1":
            tmpdouble = hDadtDAvailableBal;
            break;
        default:
            tmpdouble = hDaajDrAmt;
            break;
        }
        setValueDouble("item_d_bal", tmpdouble);
        setValue("item_date", hDadtItemPostDate);
        setValue("interest_date", hDadtInterestDate);
        setValue("adj_reason_code", hDaajAdjReasonCode);
        setValue("adj_comment", hDaajAdjComment);
        setValue("reference_no", hDaajReferenceNo);
        setValue("stmt_cycle", hDadtStmtCycle);
        setValue("c_debt_key", hDaajCDebtKey);
        setValue("debit_item", hDaajDebitItem);
        setValue("value_type", hDaajValueType);
        setValue("purchase_date", hDaajPurchaseDate);
        setValue("item_post_date", hDaajItemPostDate);
        setValue("card_no", hDaajCardNo);
        setValue("pay_id", hDaajCardNo);
        setValue("mod_user", "system");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }
    }

    /*****************************************************************************/
    void updateDbaAcaj(int hIndex) throws Exception {
        daoTable = "dba_acaj";
        updateSQL = " proc_flag = decode( ?,0,'Y','4'), ";
        updateSQL += "         mod_pgm = 'DbaA005', ";
        updateSQL += "         mod_time = sysdate ";
        whereStr = "  where  rowid    = ? ";
        setInt(1, hIndex);
        setRowId(2, hDaajRowid);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_dba_acaj not found!", "", hCallBatchSeqno);
        }
    }

    /*************************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = " select business_date ";
        sqlCmd += " from   ptr_businday ";
        sqlCmd += " fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
    }

    /*****************************************************************************/
    int selectDbbBill1() throws Exception {
        tempInt = 0;
        hTempTransactionCode = "";
        sqlCmd = " select txn_code, ";
        sqlCmd += "        1 temp_int ";
        sqlCmd += "   from dbb_bill ";
        sqlCmd += "  where reference_no     = ? ";
        sqlCmd += "    and bill_type       in ('FIFC') ";
        setString(1, hDaajReferenceNo);
        if (selectTable() > 0) {
            hTempTransactionCode = getValue("txn_code");
            tempInt = getValueInt("temp_int");
        }
        return 0;

    }

    /*************************************************************************/
    void selectDbbBillFee() throws Exception {
        hBillDestinationAmt = 0;
        sqlCmd = " SELECT dest_amt ";
        sqlCmd += "   FROM dbb_bill ";
        sqlCmd += "  WHERE reference_no  = ? ";
        setString(1, hDbilReferenceNoFeeF);

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbb_bill not found!", "", hCallBatchSeqno);
        }
        hBillDestinationAmt = getValueDouble("dest_amt");
    }
    
    /***********************************************************************
	 * 檢查是否符合執行的條件
	 */
	String runCheck(String[] args) throws Exception {
		String runFlag = "Y";

		if (args.length == 0) {
			;
		} else {
				hBusiBusinessDate = args[0];
		}
		
		if(checkHoliday()) {
			showLogMessage("I", "", "今日["+ hBusiBusinessDate +"]非營業日,本程式不處理");
			runFlag = "N";
			return runFlag;
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
    public static void main(String[] args) throws Exception {
        DbaA005 proc = new DbaA005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
