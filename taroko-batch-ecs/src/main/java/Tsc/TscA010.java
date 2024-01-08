/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------  -------------------  ------------------------------------------ *
* 103/12/16  V1.01.00  Lai        RECS-1030425-020 餘額轉置-負值                                              *
* 104/11/04  V1.01.01  Lai        modify select_bil_bill                     *
* 105/08/01  V1.01.02  Lai        modify select_bil_bill                     *
* 109/02/27  V1.01.03  Brian      transfer to java                           *
* 109-11-13  V1.00.04  tanwei    updated for project coding standard         *
*                                                                            *
*****************************************************************************/

package Tsc;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡負值 轉Billing處理*/
public class TscA010 extends AccessDAO {
    private final String progname = "悠遊卡負值 轉Billing處理  109/11/13 V1.00.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    boolean debug = false;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempNotifyDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hPre3SystemDate = "";
    String fixBillType = "";
    String hTempBatchSeq = "";
    String hBiunConfFlag = "";

    long hCgecSeqNo = 0;
    String hCgecReferenceNo = "";
    String hCgecCardNo = "";
    String hCgecTscCardNo = "";
    String hCgecBillType = "";
    String hCgecTransactionCode = "";
    String hCgecTscTxCode = "";
    String hCgecPurchaseDate = "";
    String hCgecPurchaseTime = "";
    String hCgecMerchantNo = "";
    String hCgecMerchantCategory = "";
    String hCgecMerchantChiName = "";
    double hCgecDestinationAmt = 0;
    String hCgecDestinationCurrency = "";
    String hCgecBillDesc = "";
    String hCgecFileName = "";
    String hCgecTscError = "";
    String hCgecTsccDataSeqno = "";
    String hCgecOnlineMark = "";
    String hCgecCreateDate = "";
    String hCgecPostFlag = "";
    String hCgprPayFlag = "";
    String hWdayThisLastpayDate = "";
    String hCardPSeqno = "";
    String hCardAcctType = "";
    String hCardAcctKey = "";
    String hCgecTscNotiDate = "";
    String hCgecTscRespCode = "";
    String hCgecRowid = "";

    String hAcajCreateDate = "";
    String hAcajCreateTime = "";
    String hAcajPSeqno = "";
    String hAcajAcctType = "";
    String hAcajAcctKey = "";
    String hAcajAdjustType = "";
    String hAcajReferenceNo = "";
    String hAcajPostDate = "";
    double hAcajOrginalAmt = 0;
    double hAcajDrAmt = 0;
    double hAcajCrAmt = 0;
    double hAcajBefAmt = 0;
    double hAcajAftAmt = 0;
    double hAcajBefDAmt = 0;
    double hAcajAftDAmt = 0;
    String hAcajAcctItemEname = "";
    String hAcajFunctionCode = "";
    String hAcajCardNo = "";
    String hAcajCashType = "";
    String hAcajValueType = "";
    String hAcajTransAcctType = "";
    String hAcajTransAcctKey = "";
    String hAcajInterestDate = "";
    String hAcajAdjReasonCode = "";
    String hAcajAdjComment = "";
    String hAcajCDebtKey = "";
    String hAcajDebitItem = "";
    String hAcajConfirmFlag = "";
    String hAcajJrnlDate = "";
    String hAcajJrnlTime = "";
    String hAcajPaymentType = "";
    String hAcajJobCode = "";
    String hAcajVouchJobCode = "";
    String hAcajUpdateDate = "";
    String hAcajUpdateUser = "";
    String hAcajMerchantNo = "";
    String hAcajCurrCode = "";
    String hDebtPSeqno = "";
    String hDebtAcctType = "";
    String hDebtAcctKey = "";
    double hDebtBegBal = 0;
    double hDebtEndBal = 0;
    double hDebtDAvailableBal = 0;
    String hDebtAcctItemEname = "";
    String hDebtInterestDate = "";

    double hAcctMinPayBal = 0;

    int hPostTotRecord = 0;
    double hPostTotAmt = 0;
    int totalCnt = 0;
    int insertCnt = 0;

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
                comc.errExit("Usage : TscA010 [batch_seq]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            hTempNotifyDate = "";
            selectPtrBusinday();
            showLogMessage("I", "", String.format(" prev 3 month=[%s]", hPre3SystemDate));

            fixBillType = "TSCC";
            hCgecBillType = fixBillType;

            hPostTotRecord = 0;
            hPostTotAmt = 0;
            selectBilPostcntl();

            selectTscCgecPre();

            if (totalCnt > 0)
                insertBilPostcntl();

            showLogMessage("I", "", String.format("Total process record[%d]", totalCnt));
            showLogMessage("I", "", String.format("\n程式執行結束"));
            // ==============================================
            // 固定要做的
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***************************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        hPre3SystemDate = "";
        sqlCmd = " select business_date, ";
        sqlCmd += "        decode(cast(? as varchar(8)),'', ";
        sqlCmd += "          to_char(decode(sign(substr(to_char(sysdate,'hh24miss'),1,4)-'1530'),1 ";
        sqlCmd += "                 ,sysdate,sysdate-1),'yyyymmdd'), cast(? as varchar(8))) as h_temp_notify_date, ";
        sqlCmd += "        to_char(add_months(sysdate,-3),'yyyymmdd') as h_pre3_system_date";
        sqlCmd += "  from ptr_businday ";
        sqlCmd += "  fetch first 1 rows only ";
        setString(1, hTempNotifyDate);
        if (selectTable() > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTempSystemDate = sysDate;
            hTempNotifyTime = sysTime;
            hPre3SystemDate = getValue("h_pre3_system_date");
        } else {
            comcr.errRtn("select_ptr_businday error", "", hCallBatchSeqno);
        }
    }

    /*************************************************************************/
    void updateTscCgecPre(int idx) throws Exception {
        daoTable = "tsc_cgec_pre";
        updateSQL += "   over_flag    = decode(cast(? as integer), '1' , 'Y' , over_flag), ";
        updateSQL += "   post_flag    = decode(cast(? as integer), '2' , 'Y' , post_flag), ";
        updateSQL += "   pay_flag     = decode(cast(? as integer), '3' , 'Y' , pay_flag ), ";
        updateSQL += "   pay_del_flag = decode(cast(? as integer), '4' , 'Y' , pay_del_flag ), ";
        updateSQL += "   del_date     = decode(cast(? as integer), '4' , cast(? as varchar(8)) , del_date ) ";
        whereStr = "  where rowid        = ? ";
        setInt(1, idx);
        setInt(2, idx);
        setInt(3, idx);
        setInt(4, idx);
        setInt(5, idx);
        setString(6, hBusiBusinessDate);
        setRowId(7, hCgecRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_cgec_pre error", "", hCallBatchSeqno);
        }
    }

    /*************************************************************************/
    void selectTscCgecPre() throws Exception {

        sqlCmd = "  SELECT                                           ";
        sqlCmd += "         a.reference_no,                          ";
        sqlCmd += "         a.card_no,                               ";
        sqlCmd += "         a.tsc_card_no,                           ";
        sqlCmd += "         a.bill_type,                             ";
        sqlCmd += "         a.txn_code,                              ";
        sqlCmd += "         a.tsc_tx_code,                           ";
        sqlCmd += "         a.purchase_date,                         ";
        sqlCmd += "         a.purchase_time,                         ";
        sqlCmd += "         a.mcht_no,                               ";
        sqlCmd += "         a.mcht_category,                         ";
        sqlCmd += "         a.mcht_chi_name,                         ";
        sqlCmd += "         a.dest_amt,                              ";
        sqlCmd += "         a.dest_curr,                             ";
        sqlCmd += "         a.bill_desc,                             ";
        sqlCmd += "         a.file_name,                             ";
        sqlCmd += "         a.tsc_error,                             ";
        sqlCmd += "         a.tscc_data_seqno,                       ";
        sqlCmd += "         a.online_mark,                           ";
        sqlCmd += "         a.crt_date,                              ";
        sqlCmd += "         a.post_flag,                             ";
        sqlCmd += "         a.pay_flag,                              ";
        sqlCmd += "         c.this_lastpay_date,                     ";
        sqlCmd += "         b.acno_p_seqno,                          ";
        sqlCmd += "         b.acct_type,                             ";
        sqlCmd += "         UF_ACNO_KEY(b.acno_p_seqno) as acct_key, ";
        sqlCmd += "         a.tsc_noti_date,                         ";
        sqlCmd += "         a.tsc_resp_code,                         ";
        sqlCmd += "         a.rowid as rowid                         ";
        sqlCmd += "    FROM crd_card b, tsc_cgec_pre a, ptr_workday c ";
        sqlCmd += "   where b.card_no    = a.card_no ";
        sqlCmd += "     AND b.stmt_cycle = c.stmt_cycle ";
        /* and post_flag = 'N' 下面須判斷 */
        sqlCmd += "     and over_flag    = 'N' ";
        sqlCmd += "     and del_flag     = 'N' ";
        sqlCmd += "     and pay_del_flag = 'N' ";
        sqlCmd += "     and ( tsc_error ='' or tsc_error  = '0000') ";
        /*
         * post_flag : N未做 Y已做(入bil_curpost) over_flag : 連續三期未出帳單者 pay_flag : 已出帳單逾期未繳款者
         * del_flag : 卡友客訴人工D檔者 pay_del_flag : 已出帳單逾期未繳款三個月系統D檔者
         */

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            initTscCgecAll();

            hCgecReferenceNo = getValue("reference_no");
            hCgecCardNo = getValue("card_no");
            hCgecTscCardNo = getValue("tsc_card_no");
            hCgecBillType = getValue("bill_type");
            hCgecTransactionCode = getValue("txn_code");
            hCgecTscTxCode = getValue("tsc_tx_code");
            hCgecPurchaseDate = getValue("purchase_date");
            hCgecPurchaseTime = getValue("purchase_time");
            hCgecMerchantNo = getValue("mcht_no");
            hCgecMerchantCategory = getValue("mcht_category");
            hCgecMerchantChiName = getValue("mcht_chi_name");
            hCgecDestinationAmt = getValueDouble("dest_amt");
            hCgecDestinationCurrency = getValue("dest_curr");
            hCgecBillDesc = getValue("bill_desc");
            hCgecFileName = getValue("file_name");
            hCgecTscError = getValue("tsc_error");
            hCgecTsccDataSeqno = getValue("tscc_data_seqno");
            hCgecOnlineMark = getValue("online_mark");
            hCgecCreateDate = getValue("crt_date");
            hCgecPostFlag = getValue("post_flag");
            hCgprPayFlag = getValue("pay_flag");
            hWdayThisLastpayDate = getValue("this_lastpay_date");
            hCardPSeqno = getValue("acno_p_seqno");
            hCardAcctType = getValue("acct_type");
            hCardAcctKey = getValue("acct_key");
            hCgecTscNotiDate = getValue("tsc_noti_date");
            hCgecTscRespCode = getValue("tsc_resp_code");
            hCgecRowid = getValue("rowid");

            totalCnt++;

            if ((totalCnt % 1000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            if (hCgecPostFlag.equals("Y")) {
                selectActAcct();
            }

            if (comc.str2long(hCgecCreateDate) < comc.str2long(hPre3SystemDate)) {
                if (hCgecPostFlag.equals("Y")) {
                    if (hCgprPayFlag.equals("Y") && hAcctMinPayBal > 0) /* 已出帳單逾期未繳款者且超過三個月 */
                    {
                        insertActAcaj();
                        updateTscCgecPre(4);
                        continue;
                    }
                } else {
                    updateTscCgecPre(1); /* 連續三個月無請款交易即不再處理 */
                    continue;
                }
            }

            if (hCgecPostFlag.equals("N")) {
                int rtn = selectBilBill();
                if (rtn == 0)
                    continue;
                else /* 有消費請款才billing */
                {
                    updateTscCgecPre(2);
                    insertTscCgecAll();
                }
            } else {
                if (comc.str2long(hBusiBusinessDate) > comc.str2long(hWdayThisLastpayDate)
                        && hAcctMinPayBal > 0) {
                    updateTscCgecPre(3);
                }
            }

        }
        closeCursor(cursorIndex);

    }

    /*******************************************************************/
    void selectBilPostcntl() throws Exception {
        hTempBatchSeq = "";

        sqlCmd = " SELECT substr(to_char(nvl(max(batch_seq),0)+1,'0000'),2,4) as h_temp_batch_seq";
        sqlCmd += "          from bil_postcntl ";
        sqlCmd += "         where batch_unit = substr(?,1,2) ";
        sqlCmd += "           and batch_date = ? ";
        setString(1, fixBillType);
        setString(2, hBusiBusinessDate);
        if (selectTable() > 0)
            hTempBatchSeq = getValue("h_temp_batch_seq");
        else

        {
            comcr.errRtn("select_bil_postcntl error", String.format("bill_type[%s]", fixBillType),
                    hCallBatchSeqno);
        }
    }

    /*************************************************************************/
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";

        sqlCmd = " SELECT conf_flag ";
        sqlCmd += "   FROM ptr_billunit ";
        sqlCmd += "  WHERE bill_unit = substr(?,1,2) ";
        setString(1, fixBillType);
        if (selectTable() > 0)
            hBiunConfFlag = getValue("conf_flag");
        else

        {
            comcr.errRtn("select_ptr_billunit error", String.format("bill_type[%s]", hCgecBillType),
                    hCallBatchSeqno);
        }
    }

    /***************************************************************************/
    void insertBilPostcntl() throws Exception {
        selectPtrBillunit();

        daoTable = "bil_postcntl";
        extendField = daoTable + ".";
        setValue(extendField + "batch_date", hBusiBusinessDate);
        setValue(extendField + "batch_unit", hCgecBillType.substring(0, 2));
        setValueInt(extendField + "batch_seq", comc.str2int(hTempBatchSeq));
        setValue(extendField + "batch_no ", hBusiBusinessDate + hCgecBillType.substring(0, 2) + hTempBatchSeq);
        setValueInt(extendField + "tot_record", hPostTotRecord);
        setValueDouble(extendField + "tot_amt", hPostTotAmt);
        setValue(extendField + "confirm_flag_p", hBiunConfFlag.equals("N") ? "Y" : "N");
        setValue(extendField + "confirm_flag", hBiunConfFlag);
        setValue(extendField + "apr_user", "");
        setValue(extendField + "apr_date", "");
        setValue(extendField + "this_close_date", hTempNotifyDate);
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate!", "", hCallBatchSeqno);
        }
    }

    /*******************************************************************/
    void insertTscCgecAll() throws Exception {
        insertCnt++;
        hCgecSeqNo = insertCnt;

        hPostTotRecord++;
        hPostTotAmt = hPostTotAmt + hCgecDestinationAmt;
        daoTable = "tsc_cgec_all";
        extendField = daoTable + ".";
        setValue(extendField + "batch_no", hBusiBusinessDate + hCgecBillType.substring(0, 2) + hTempBatchSeq);
        setValueInt(extendField + "seq_no", (int) hCgecSeqNo);
        setValue(extendField + "card_no", hCgecCardNo);
        setValue(extendField + "tsc_card_no", hCgecTscCardNo);
        setValue(extendField + "bill_type", hCgecBillType);
        setValue(extendField + "txn_code", hCgecTransactionCode);
        setValue(extendField + "tsc_tx_code", hCgecTscTxCode);
        setValue(extendField + "purchase_date", hCgecPurchaseDate);
        setValue(extendField + "purchase_time", hCgecPurchaseTime);
        setValue(extendField + "mcht_no", hCgecMerchantNo);
        setValue(extendField + "mcht_category", hCgecMerchantCategory);
        setValue(extendField + "mcht_chi_name", hCgecMerchantChiName);
        setValueDouble(extendField + "dest_amt", hCgecDestinationAmt);
        setValue(extendField + "dest_curr", hCgecDestinationCurrency);
        setValue(extendField + "bill_desc", hCgecBillDesc);
        setValue(extendField + "post_flag", "N");
        setValue(extendField + "file_name", hCgecFileName);
        setValue(extendField + "tsc_error", hCgecTscError);
        setValue(extendField + "crt_date", hCgecCreateDate);
        setValue(extendField + "tsc_noti_date", hCgecTscNotiDate);
        setValue(extendField + "tsc_resp_code", hCgecTscRespCode);
        setValue(extendField + "tscc_data_seqno", hCgecTsccDataSeqno);
        setValue(extendField + "online_mark", hCgecOnlineMark);
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_cgec_all duplicate!", "", hCallBatchSeqno);
        }

    }

    /*******************************************************************/
    int selectBilBill() throws Exception {
        int tempInt = 0;

        sqlCmd = " SELECT count(*) as temp_int ";
        sqlCmd += " from bil_bill     ";
        /* where real_card_no = :h_cgec_card_no */
        sqlCmd += "where acno_p_seqno in  (select acno_p_seqno from crd_card where card_no = ?) ";
        sqlCmd += "                                    and acct_code in ('BL','CA','IT','ID','AO') ";
        sqlCmd += "  and to_char(mod_time, 'yyyymmdd') >= ? ";
        setString(1, hCgecCardNo);
        setString(2, hPre3SystemDate);
        if (selectTable() > 0)
            tempInt = getValueInt("temp_int");
        else

        {
            comcr.errRtn("select_bil_bill error", String.format("card_no[%s]", hCgecCardNo), hCallBatchSeqno);
        }

        return (tempInt);
    }

    /*******************************************************************/
    int selectActAcct() throws Exception {
        hAcctMinPayBal = 0;
        /* min pay 餘額 , 有餘額即欠錢 */

        sqlCmd = " SELECT min_pay_bal ";
        sqlCmd += "   from act_acct     ";
        sqlCmd += "  where p_seqno   in  (select p_seqno from crd_card where card_no = ?) ";
        setString(1, hCgecCardNo);

        if (selectTable() > 0)
            hAcctMinPayBal = getValueDouble("min_pay_bal");
        else

        {
            comcr.errRtn("select_act_acct error", String.format("card_no[%s]", hCgecCardNo), hCallBatchSeqno);
        }

        return (0);
    }

    /***************************************************************************/
    void initTscCgecAll() {
        hCgecReferenceNo = "";
        hCgecCardNo = "";
        hCgecTscCardNo = "";
        hCgecBillType = fixBillType;
        hCgecTransactionCode = "06";
        hCgecTscTxCode = "";
        hCgecPurchaseDate = "";
        hCgecPurchaseTime = "";
        hCgecMerchantNo = "EASY8003";
        hCgecMerchantCategory = "";
        hCgecMerchantChiName = "";
        hCgecDestinationAmt = 0;
        hCgecDestinationCurrency = "901";
        hCgecBillDesc = "";
        hCgecFileName = "";
        hCgecTscError = "";
        hCgecTscNotiDate = "";
        hCgecTscRespCode = "";
//        h_cgec_return_source =  "";
        hCgecTsccDataSeqno = "";
        hCgecOnlineMark = "";
        hCgecCreateDate = "";
        hCgecPostFlag = "";
        hCgprPayFlag = "";

        hWdayThisLastpayDate = "";
        hCardPSeqno = "";
        hCardAcctType = "";
        hCardAcctKey = "";
    }

    /*******************************************************************/
    void insertActAcaj() throws Exception {
        if (hCgecReferenceNo.length() < 10)
            return;

        selectActDebt();

        hAcajFunctionCode = "";
        hAcajCashType = "";
        hAcajTransAcctType = "";
        hAcajTransAcctKey = "";
        hAcajInterestDate = "";
        hAcajAdjComment = "";
        hAcajCDebtKey = "";
        hAcajJrnlDate = "";
        hAcajJrnlTime = "";
        hAcajPaymentType = "";

        hAcajDebitItem = "14817000";
        hAcajReferenceNo = hCgecReferenceNo;
        hAcajPostDate = hBusiBusinessDate;
        hAcajAdjustType = "DE08";
        hAcajValueType = "2";
        hAcajAdjReasonCode = "1";
        hAcajAdjComment = "D除悠遊卡餘額負值";

        hAcajCreateDate = hTempSystemDate;
        hAcajCreateTime = hTempNotifyTime;
        hAcajUpdateDate = hTempSystemDate;
        hAcajUpdateUser = "tsc_a010";
        hAcajJobCode = "OP";
        hAcajVouchJobCode = "01";
        hAcajCurrCode = "901";

        hAcajPSeqno = hCardPSeqno;
        hAcajAcctType = hCardAcctType;
        hAcajAcctKey = hCardAcctKey;
        hAcajOrginalAmt = hCgecDestinationAmt;
        if (debug)
            showLogMessage("I", "", String.format("  888 insert amt=[%f]", hCgecDestinationAmt));
        hAcajDrAmt = hCgecDestinationAmt;
        hAcajBefAmt = hDebtEndBal;
        hAcajAftAmt = hDebtEndBal - hCgecDestinationAmt;
        hAcajBefDAmt = hDebtDAvailableBal;
        hAcajAftDAmt = hDebtDAvailableBal - hCgecDestinationAmt;

        hAcajAcctItemEname = hDebtAcctItemEname;
        hAcajInterestDate = hDebtInterestDate;
        hAcajFunctionCode = "U";
        hAcajCardNo = hCgecCardNo;
        hAcajValueType = "2";
        hAcajConfirmFlag = "Y";
        hAcajMerchantNo = hCgecMerchantNo;

        daoTable = "act_acaj";
        extendField = daoTable + ".";
        setValue(extendField + "crt_date", hAcajCreateDate);
        setValue(extendField + "crt_time", hAcajCreateTime);
        setValue(extendField + "p_seqno", hAcajPSeqno);
        setValue(extendField + "acct_type", hAcajAcctType);
//        setValue(extendField + "acct_key", h_acaj_acct_key);
        setValue(extendField + "adjust_type", hAcajAdjustType);
        setValue(extendField + "reference_no", hAcajReferenceNo);
        setValue(extendField + "post_date", hAcajPostDate);
        setValueDouble(extendField + "orginal_amt", hAcajOrginalAmt);
        setValueDouble(extendField + "dr_amt", hAcajDrAmt);
        setValueDouble(extendField + "cr_amt", hAcajCrAmt);
        setValueDouble(extendField + "bef_amt", hAcajBefAmt);
        setValueDouble(extendField + "aft_amt", hAcajAftAmt);
        setValueDouble(extendField + "bef_d_amt", hAcajBefDAmt);
        setValueDouble(extendField + "aft_d_amt", hAcajAftDAmt);
        setValue(extendField + "acct_code", hAcajAcctItemEname);
        setValue(extendField + "function_code", hAcajFunctionCode);
        setValue(extendField + "card_no", hAcajCardNo);
        setValue(extendField + "cash_type", hAcajCashType);
        setValue(extendField + "value_type", hAcajValueType);
        setValue(extendField + "trans_acct_type", hAcajTransAcctType);
        setValue(extendField + "trans_acct_key", hAcajTransAcctKey);
        setValue(extendField + "interest_date", hAcajInterestDate);
        setValue(extendField + "adj_reason_code", hAcajAdjReasonCode);
        setValue(extendField + "adj_comment", hAcajAdjComment);
        setValue(extendField + "c_debt_key", hAcajCDebtKey);
        setValue(extendField + "debit_item", hAcajDebitItem);
        setValue(extendField + "apr_flag", hAcajConfirmFlag);
        setValue(extendField + "jrnl_date", hAcajJrnlDate);
        setValue(extendField + "jrnl_time", hAcajJrnlTime);
        setValue(extendField + "payment_type", hAcajPaymentType);
        setValue(extendField + "update_date", hAcajUpdateDate);
        setValue(extendField + "update_user", hAcajUpdateUser);
        setValue(extendField + "mcht_no", hAcajMerchantNo);
        setValue(extendField + "job_code", hAcajJobCode);
        setValue(extendField + "vouch_job_code", hAcajVouchJobCode);
        setValue(extendField + "curr_code", hAcajCurrCode);
        setValue(extendField + "mod_time", sysDate + sysTime);
        setValue(extendField + "mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_act_acj duplicate!", "", hCallBatchSeqno);
        }
    }

    /***************************************************************************/
    void selectActDebt() throws Exception {
        hDebtPSeqno = "";
        hDebtAcctType = "";
        hDebtAcctKey = "";
        hDebtBegBal = 0;
        hDebtEndBal = 0;
        hDebtDAvailableBal = 0;
        hDebtAcctItemEname = "";
        hDebtInterestDate = "";

        sqlCmd += "  SELECT ";
        sqlCmd += "       acno_p_seqno,  ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       UF_ACNO_KEY(acno_p_seqno) as acct_key, ";
        sqlCmd += "       beg_bal, ";
        sqlCmd += "       end_bal, ";
        sqlCmd += "       D_AVAIL_BAL, ";
        sqlCmd += "       acct_code, ";
        sqlCmd += "       interest_date ";
        sqlCmd += "  FROM act_debt ";
        sqlCmd += " where reference_no   = ? ";
        sqlCmd += "   and D_AVAIL_BAL = beg_bal ";
        setString(1, hCgecReferenceNo);

        if (selectTable() > 0)
            hAcctMinPayBal = getValueDouble("min_pay_bal");
        hDebtPSeqno = getValue("acno_p_seqno");
        hDebtAcctType = getValue("acct_type");
        hDebtAcctKey = getValue("acct_key");
        hDebtBegBal = getValueDouble("beg_bal");
        hDebtEndBal = getValueDouble("end_bal");
        hDebtDAvailableBal = getValueDouble("D_AVAIL_BAL");
        hDebtAcctItemEname = getValue("acct_code");
        hDebtInterestDate = getValue("interest_date");
        if (debug)
            showLogMessage("I", "", String.format("reference_no=[%s]item_ename=[%s]acct_key=[%s]", hCgecReferenceNo,
                    hDebtAcctItemEname, hDebtAcctKey));

        else

        {
            selectActDebtHst();
        }

    }

    /***************************************************************************/
    void selectActDebtHst() throws Exception {
        hDebtPSeqno = "";
        hDebtAcctType = "";
        hDebtAcctKey = "";
        hDebtBegBal = 0;
        hDebtEndBal = 0;
        hDebtDAvailableBal = 0;
        hDebtAcctItemEname = "";
        hDebtInterestDate = "";

        sqlCmd += "  SELECT ";
        sqlCmd += "       acno_p_seqno,  ";
        sqlCmd += "       acct_type, ";
        sqlCmd += "       UF_ACNO_KEY(acno_p_seqno) as acct_key, ";
        sqlCmd += "       beg_bal, ";
        sqlCmd += "       end_bal, ";
        sqlCmd += "       D_AVAIL_BAL, ";
        sqlCmd += "       acct_code, ";
        sqlCmd += "       interest_date ";
        sqlCmd += "  FROM act_debt_hst ";
        sqlCmd += " where reference_no   = ? ";
        sqlCmd += "   and D_AVAIL_BAL = beg_bal ";
        setString(1, hCgecReferenceNo);

        if (selectTable() > 0)
            hAcctMinPayBal = getValueDouble("min_pay_bal");
        hDebtPSeqno = getValue("acno_p_seqno");
        hDebtAcctType = getValue("acct_type");
        hDebtAcctKey = getValue("acct_key");
        hDebtBegBal = getValueDouble("beg_bal");
        hDebtEndBal = getValueDouble("end_bal");
        hDebtDAvailableBal = getValueDouble("D_AVAIL_BAL");
        hDebtAcctItemEname = getValue("acct_code");
        hDebtInterestDate = getValue("interest_date");

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscA010 proc = new TscA010();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
