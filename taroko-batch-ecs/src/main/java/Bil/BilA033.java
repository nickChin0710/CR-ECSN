/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00  Edson       program initial                           *
*  107/08/01  V1.03.01  Atom        RECS-s1070416-030 Master DT3 tran_time    *
*  107/08/01  V1.03.01  Brian       transfer to java                          *
*  109/05/01  V1.03.02  陳君暘      RECS-s1090413-041 NMIP move, chg put name *
*  109/11/23  V1.00.01   shiyuqi       updated for project coding standard   *
*  112/05/17  V1.00.02  JeffKung    update trailer amt sum by sourceAmt       *
*  112/06/05  V1.00.03  JeffKung    5000筆一個檔案                            *
******************************************************************************/

package Bil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*回饋*/
public class BilA033 extends AccessDAO {
    private String progname = "回饋 Master/Visa卡 作業  112/06/05 V1.00.03 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    boolean debug = false;
    int debugI = 1;

    String prgmId = "BilA033";
    String prgmName = "回饋 Master/Visa卡 作業";
    String rptName = "ICM51QBD";
    String rptId = "BilA033";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String buf = "";

    String hBusiBusinessDate = "";
    String hBusinssDate = "";
    String hSystemDate = "";
    String hSystemMmddyy = "";
    String hSystemYddd = "";
    String hSystemPrevDate = "";
    String hSystemDateF = "";
    int hMIntFlag = 0;
    String hMBillReferenceNo = "";
    String hMBillRealCardNo = "";
    String hMBillTransactionCode = "";
    String hMBillFilmNo = "";
    double hMBillDestinationAmt = 0;
    String hMBillDestinationCurrency = "";
    double hMBillSourceAmt = 0;
    String hMBillSourceCurrency = "";
    String hMBillMerchantEngName = "";
    String hMBillMerchantCity = "";
    String hMBillMerchantCountry = "";
    String hMBillMerchantCategory = "";
    String hMBillMerchantZip = "";
    String hMBillMerchantState = "";
    String hMBillMerchantNo = "";
    String hMBillMerchantChiName = "";
    String hMBillAuthorization = "";
    String hMBillBilledDate = "";
    String hMBillAcctMonth = "";
    String hMBillBilledFlag = "";
    String hMBillBillType = "";
    String hMBillCashAdvFee = "";
    String hMBillProcessDate = "";
    String hMBillAcquireDate = "";
    String hMBillPurchaseDate = "";
    String hMBillPostDate = "";
    String hMBillAcctCode = "";
    String hMBillAcctItem = "";
    String hMBillAcexterDesc = "";
    String hMBillItemOrderNormal = "";
    String hMBillItemOrderBackDate = "";
    String hMBillItemOrderRefund = "";
    String hMBillItemClassNormal = "";
    String hMBillItemClassBackDate = "";
    String hMBillItemClassRefund = "";
    String hMBillBatchNo = "";
    String hMBillContractNo = "";
    String hMBillContractSeqNo = "";
    double hMBillContractAmt = 0;
    String hMBillInstallTotTerm = "";
    String hMBillInstallCurrTerm = "";
    String hMBillReasonCode = "";
    String hMBillPosTermCapability = "";
    int hMBillPosPinCapability = 0;
    String hMBillPosEntryMode = "";
    String hMBillReimbursementAttr = "";
    String hMBillStmtCycle = "";
    String hMBillProdNo = "";
    String hMBillGroupCode = "";
    String hMBillPromoteDept = "";
    String hMBillIssueDate = "";
    String hMBillCollectionMode = "";
    String hMBillInterestDate = "";
    String hMBillExchangeRate = "";
    String hMBillExchangeDate = "";
    String hMBillBinType = "";
    String hMBillEcInd = "";
    String hMBillTransactionSource = "";
    String hMBillFeesReferenceNo = "";
    String hMBillReferenceNoOriginal = "";
    String hMBillSourceCode = "";
    String hMBillRegBankNo = "";
    String hMBillValidFlag = "";
    String hMBillRskType = "";
    String hMBillRskPost = "";
    String hMBillRskOrgCardno = "";
    String hMBillRskErrNr = "";
    String hMBillRskCtrlSeqno = "";
    String hMBillLimitEndDate = "";
    double hMBillCurrAdjustAmt = 0;
    double hMBillProblemAmt = 0;
    String hMBillModTime = "";
    String hMBillChipConditionCode = "";
    String hMBillAuthResponseCode = "";
    String hMBillTerminalVerResults = "";
    String hMBillCardSeqNum = "";
    String hMBillUnpredicNum = "";
    String hMBillAppTranCount = "";
    String hMBillAppIntPro = "";
    String hMBillCryptogram = "";
    String hMBillDataAuthCode = "";
    String hMBillCryInfoData = "";
    String hMBillLifeCycSupInd = "";
    String hMBillBanknetDate = "";
    String hMBillInterRateDes = "";
    String hMBillExpirDate = "";
    String hMBillPaymentType = "";
    String hMBillServiceCode = "";
    double hMBillAmtMccrNum = 0;
    double hMBillIncludeFeeAmt = 0;
    String hMBillUcaf = "";
    String hMBillIssueSR = "";
    String hAll2702 = "";
    String hAll2703 = "";
    String hAll2704 = "";

    String hArgv = "";
    int fo = 0;
    int headerFlag = 1;  //初始化時header on
    int procFile = 0;
    int fileSeq = 20;  //每天從20+1開始編序號
    int totCnt = 0;
    int realCnt = 0;
    String installFlag = "";
    String tBitMap = "";
    int txnCnt = 0;
    int recCnt = 0;
    double txnAmt = 0;
    int cnt05 = 0;
    int cnt06 = 0;
    int cnt07 = 0;
    long tempLong = 0;
    double amt05 = 0;
    double amt06 = 0;
    double amt07 = 0;
    String tempStr = "";
    String tempX15 = "";
    int chkDig = 0;

    String filename = "";
    // ****************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 2) {
                comc.errExit("Usage : BilA033 (P/T) (post_date yyyymmdd)", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            hArgv = "PROD";
            //hBusinssDate = hSystemPrevDate;
            if (args.length == 2) {
                hArgv = args[0];
                hBusinssDate = args[1];
            }

            showLogMessage("I", "", String.format("****  Process date=[%s][%s]\n", hBusinssDate, hSystemYddd));

            //開啟第一個open file
            procFile ++;
			filename = String.format("%s.%s.%s%02d","M00600000","ICM51QBD" , (comc.str2int(hBusinssDate.substring(0,4)) - 2011)+hBusinssDate.substring(4),(fileSeq+procFile));
			String filename1 = String.format("%s/media/bil/%s", comc.getECSHOME(), filename);
			
			fo = openOutputText(filename1,"MS950");
			if (fo == -1) {
				showLogMessage("E", "", "檔案" + filename1 + "無法開啟寫入 error!");
				return 0;
			}
			
            /*寫出檔頭資料*/
            buf = String.format("H0060000095000000%-4.4s%-8.8s%s%02d%-409.409s\r\n"
                    , hArgv, hBusinssDate, "ICM51QBD", (fileSeq+procFile), " ");
			writeTextFile(fo, buf);
			headerFlag = 0;
            //lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
            
            selectBilBill();
            showLogMessage("I", "", String.format("\n** bil_bill 總筆數=[%d],run=[%d]\n", totCnt, realCnt));

            ftpMput(filename);
            
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /**********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }

        hBusiBusinessDate = hBusinssDate;
        hSystemMmddyy = "";
        hSystemPrevDate = "";

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'mmddyy') h_system_mmddyy,";
        sqlCmd += "to_char(sysdate,'YDDD') h_system_yddd,";
        sqlCmd += "to_char(sysdate-1 days,'yyyymmdd') h_system_prev_date,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dual not found!", "", "");
        }
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemMmddyy = getValue("h_system_mmddyy");
            hSystemYddd = getValue("h_system_yddd");
            hSystemPrevDate = getValue("h_system_prev_date");
            hSystemDateF = getValue("h_system_date_f");
        }
    }

    /***********************************************************************/
    void selectBilBill() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "1 h_m_int_flag,";
        sqlCmd += "reference_no,";
        sqlCmd += "card_no,";
        sqlCmd += "txn_code,";
        sqlCmd += "film_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "dest_curr,";
        sqlCmd += "decode(source_amt,0,dest_amt,source_amt) h_m_bill_source_amt,";
        sqlCmd += "source_curr,";
        sqlCmd += "mcht_no as h_m_bill_merchant_eng_name,";
        sqlCmd += "mcht_city as h_m_bill_merchant_city,";
        sqlCmd += "substr(decode(mcht_country,'','TW',mcht_country),1,2) h_m_bill_merchant_country,";
        sqlCmd += "mcht_category as h_m_bill_merchant_category,";
        sqlCmd += "mcht_zip as h_m_bill_merchant_zip,";
        sqlCmd += "mcht_state,";
        sqlCmd += "mcht_no as h_m_bill_merchant_no,";
        sqlCmd += "mcht_chi_name,";
        sqlCmd += "auth_code,";
        sqlCmd += "billed_date,";
        sqlCmd += "acct_month,";
        sqlCmd += "billed_flag,";
        sqlCmd += "bill_type,";
        sqlCmd += "cash_adv_fee,";
        sqlCmd += "process_date,";
        sqlCmd += "acquire_date,";
        sqlCmd += "purchase_date,";
        sqlCmd += "post_date,";
        sqlCmd += "acct_code,";
        sqlCmd += "acct_item,";
        sqlCmd += "acexter_desc,";
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "terminal_id,"; 
        sqlCmd += "batch_no,";
        sqlCmd += "contract_no,";
        sqlCmd += "contract_seq_no,";
        sqlCmd += "contract_amt,";
        sqlCmd += "install_tot_term,";
        sqlCmd += "install_curr_term,";
        sqlCmd += "decode(pos_entry_mode,'','01',pos_entry_mode) h_m_bill_pos_entry_mode,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "prod_no,";
        sqlCmd += "group_code,";
        sqlCmd += "promote_dept,";
        sqlCmd += "issue_date,";
        sqlCmd += "collection_mode,";
        sqlCmd += "interest_date,";
        sqlCmd += "bin_type,";
        sqlCmd += "fees_reference_no,";
        sqlCmd += "reference_no_original,";
        sqlCmd += "source_code,";
        sqlCmd += "reg_bank_no,";
        sqlCmd += "valid_flag,";
        sqlCmd += "rsk_type,";
        sqlCmd += "rsk_post,";
        sqlCmd += "rsk_org_cardno,";
        sqlCmd += "rsk_err_nr,";
        sqlCmd += "rsk_ctrl_seqno,";
        sqlCmd += "limit_end_date,";
        sqlCmd += "curr_adjust_amt,";
        sqlCmd += "problem_amt,";
        sqlCmd += "to_char(mod_time,'hh24miss') h_m_bill_mod_time, ";
        sqlCmd += "expir_date,";
        sqlCmd += "payment_type,";
        sqlCmd += "include_fee_amt ";
        sqlCmd += "from bil_bill ";
        sqlCmd += "Where post_date = ? ";
        sqlCmd += "and acct_code in ('BL','CA') ";
        sqlCmd += "and ( (bill_type = 'FISC' and mcht_category = '9311' )";
        sqlCmd += "   or (bill_type = 'FISC' and mcht_category = '6011' and settl_flag = '9' )  ";
        sqlCmd += "   or (bill_type = 'CHUP') ) ";
        sqlCmd += "and bin_type in ( 'M' , 'V') ";
        sqlCmd += "and substr(mcht_country,1,2) in ('','TW') ";
        sqlCmd += "and dest_amt > 0 ";
        sqlCmd += "and decode(rsk_type,'','N',rsk_type) not in ('1','2','3') ";
        setString(1, hBusinssDate);

        openCursor();
        
		while (fetchTable()) {
			
			if (headerFlag == 1) {
				//第一個檔案結束需要將檔案關閉
				if (procFile > 0) {
					buf = String.format("T%08d%08d%013.0f00%-416.416s\r\n",
		                    txnCnt, recCnt, txnAmt, " ");
				    writeTextFile(fo, buf);
				    
				    ftpMput(filename);
					closeOutputText(fo);
					txnCnt=0 ; recCnt=0; txnAmt=0;
				}
			
				procFile ++;
				filename = String.format("%s.%s.%s%02d","M00600000","ICM51QBD" , (comc.str2int(hBusinssDate.substring(0,4)) - 2011)+hBusinssDate.substring(4),(fileSeq+procFile));
				String filename1 = String.format("%s/media/bil/%s", comc.getECSHOME(), filename);
				
				fo = openOutputText(filename1,"MS950");
				if (fo == -1) {
					showLogMessage("I", "", "檔案" + filename1 + "無法開啟寫入 error!");
					return;
				}
				
	            /*寫出檔頭資料*/
	            buf = String.format("H0060000095000000%-4.4s%-8.8s%s%02d%-409.409s\r\n"
	                    , hArgv, hBusinssDate, "ICM51QBD", (fileSeq+procFile), " ");
				writeTextFile(fo, buf);
				headerFlag = 0; 
			}
			
            hMIntFlag = getValueInt("h_m_int_flag");
            hMBillReferenceNo = getValue("reference_no");
            hMBillRealCardNo = getValue("card_no");
            hMBillTransactionCode = getValue("txn_code");
            hMBillFilmNo = getValue("film_no");
            hMBillDestinationAmt = getValueDouble("dest_amt");
            hMBillDestinationCurrency = getValue("dest_curr");
            hMBillSourceAmt = getValueDouble("h_m_bill_source_amt");
            hMBillSourceCurrency = getValue("source_curr");
            hMBillMerchantEngName = getValue("h_m_bill_merchant_eng_name");
            hMBillMerchantCity = getValue("h_m_bill_merchant_city");
            hMBillMerchantCountry = getValue("h_m_bill_merchant_country");
            hMBillMerchantCategory = getValue("h_m_bill_merchant_category");
            hMBillMerchantZip = getValue("h_m_bill_merchant_zip");
            hMBillMerchantState = getValue("mcht_state");
            hMBillMerchantNo = getValue("h_m_bill_merchant_no");
            hMBillMerchantChiName = getValue("mcht_chi_name");
            hMBillAuthorization = getValue("auth_code");
            hMBillBilledDate = getValue("billed_date");
            hMBillAcctMonth = getValue("acct_month");
            hMBillBilledFlag = getValue("billed_flag");
            hMBillBillType = getValue("bill_type");
            hMBillCashAdvFee = getValue("cash_adv_fee");
            hMBillProcessDate = getValue("process_date");
            hMBillAcquireDate = getValue("acquire_date");
            hMBillPurchaseDate = getValue("purchase_date");
            hMBillPostDate = getValue("post_date");
            hMBillAcctCode = getValue("acct_code");
            hMBillAcctItem = getValue("acct_item");
            hMBillAcexterDesc = getValue("acexter_desc");
            hMBillItemOrderNormal = getValue("item_order_normal");
            hMBillItemOrderBackDate = getValue("item_order_back_date");
            hMBillItemOrderRefund = getValue("item_order_refund");
            hMBillItemClassNormal = getValue("item_class_normal");
            hMBillItemClassBackDate = getValue("item_class_back_date");
            hMBillItemClassRefund = getValue("item_class_refund");
            hMBillBatchNo = getValue("batch_no");
            hMBillContractNo = getValue("contract_no");
            hMBillContractSeqNo = getValue("contract_seq_no");
            hMBillContractAmt = getValueDouble("contract_amt");
            hMBillInstallTotTerm = getValue("install_tot_term");
            hMBillInstallCurrTerm = getValue("install_curr_term");
            hMBillPosEntryMode = getValue("h_m_bill_pos_entry_mode");
            hMBillStmtCycle = getValue("stmt_cycle");
            hMBillProdNo = getValue("prod_no");
            hMBillGroupCode = getValue("group_code");
            hMBillPromoteDept = getValue("promote_dept");
            hMBillIssueDate = getValue("issue_date");
            hMBillCollectionMode = getValue("collection_mode");
            hMBillInterestDate = getValue("interest_date");
            hMBillBinType = getValue("bin_type");
            hMBillFeesReferenceNo = getValue("fees_reference_no");
            hMBillReferenceNoOriginal = getValue("reference_no_original");
            hMBillSourceCode = getValue("source_code");
            hMBillRegBankNo = getValue("reg_bank_no");
            hMBillValidFlag = getValue("valid_flag");
            hMBillRskType = getValue("rsk_type");
            hMBillRskPost = getValue("rsk_post");
            hMBillRskOrgCardno = getValue("rsk_org_cardno");
            hMBillRskErrNr = getValue("rsk_err_nr");
            hMBillRskCtrlSeqno = getValue("rsk_ctrl_seqno");
            hMBillLimitEndDate = getValue("limit_end_date");
            hMBillCurrAdjustAmt = getValueDouble("curr_adjust_amt");
            hMBillProblemAmt = getValueDouble("problem_amt");
            hMBillModTime = getValue("h_m_bill_mod_time");
            hMBillExpirDate = getValue("expir_date");
            hMBillPaymentType = getValue("payment_type");
            hMBillIncludeFeeAmt = getValueDouble("include_fee_amt");

            //getDataFormBilNccc300Dtl(hMBillReferenceNo);
            
            if (debug) {
                showLogMessage("I", "", String.format("[card type : %S][reference no : %s]", hMBillBinType, hMBillReferenceNo));
            }
            
            totCnt++;
            
            /*
            String hSendMFlag = "";
            String hSendVFlag = "";
            sqlCmd = "select decode(send_m_flag,'','N',send_m_flag) h_send_m_flag,";
            sqlCmd += "decode(send_v_flag,'','N',send_v_flag) h_send_v_flag ";
            sqlCmd += " from ptr_billtype  ";
            sqlCmd += "where bill_type = ?  ";
            sqlCmd += "  and txn_code = ? ";
            setString(1, hMBillBillType);
            setString(2, hMBillTransactionCode);
            int recordCnt1 = selectTable();
            if (debug) {
                showLogMessage("I", "", String.format("[bill_type : %S][txn_code : %s]", hMBillBillType, hMBillTransactionCode) + recordCnt1);
            }
            if (recordCnt1  > 0) {
                hSendMFlag = getValue("h_send_m_flag");
                hSendVFlag = getValue("h_send_v_flag");
            }

            if (hMBillBinType.equals("M") &&
                comc.getSubString(hSendMFlag, 0, 1).equals("Y") == false)
                continue;
            if (hMBillBinType.equals("V") &&
                comc.getSubString(hSendVFlag, 0, 1).equals("Y") == false)
                continue;

            */
            
            realCnt++;
            txnCnt++;

            installFlag = "N";
            if (hMIntFlag == 2 || hMIntFlag == 3) {
                installFlag = "Y";
            }

            if (hMIntFlag == 2) {
                hMBillTransactionCode = "05";
            }

            if (hMIntFlag == 3) {
                hMBillTransactionCode = "06";
            }

            if (hMBillTransactionCode.equals("05")) {
                cnt05 = cnt05++;
                amt05 = amt05 + hMBillDestinationAmt;
            }
            if (hMBillTransactionCode.equals("06")) {
                cnt06 = cnt06++;
                amt06 = amt06 + hMBillDestinationAmt;
            }
            if (hMBillTransactionCode.equals("07")) {
                cnt07 = cnt07++;
                amt07 = amt07 + hMBillDestinationAmt;
            }
            
            txnAmt = txnAmt + hMBillDestinationAmt ;   //表尾以sourceAmt加總

            recCnt++;
            fiscRecord1Rtn();
            
            recCnt++;
            fiscRecord4Rtn();
            
            //每5000筆交易一個檔
            if (txnCnt == 5000) {
            	headerFlag = 1;    
            }

        }
		closeCursor();

		/*寫出檔尾資料*/
		buf = String.format("T%08d%08d%013.0f00%-416.416s\r\n",
                    txnCnt, recCnt, txnAmt, " ");
		writeTextFile(fo, buf);
        //lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", buf));

        /*產出檔案*/
        //String filename1 = String.format("%s/media/bil/%s", comc.getECSHOME(), filename);
        //comc.writeReport(filename1, lpar1, "MS950", false);
    }

    // ********************************************************************************
    void initBilNccc300Dtl() {
        hMBillReasonCode = "";
        hMBillExchangeRate = "";
        hMBillExchangeDate = "";
        hMBillEcInd = "";
        hMBillTransactionSource = "";
        hMBillChipConditionCode = "";
        hMBillAuthResponseCode = "";
        hMBillTerminalVerResults = "";
        hMBillCardSeqNum = "";
        hMBillUnpredicNum = "";
        hMBillAppTranCount = "";
        hMBillAppIntPro = "";
        hMBillCryptogram = "";
        hMBillDataAuthCode = "";
        hMBillCryInfoData = "";
        hMBillLifeCycSupInd = "";
        hMBillBanknetDate = "";
        hMBillInterRateDes = "";
        hMBillServiceCode = "";
        hMBillAmtMccrNum = 0;
        hMBillUcaf = "";
        hMBillIssueSR = "";
        hMBillPosTermCapability = "";
        hMBillPosPinCapability = 0;
        hMBillReimbursementAttr = "";
    }

    void getDataFormBilNccc300Dtl(String refNo) throws Exception {
        initBilNccc300Dtl();
        sqlCmd = "select reason_code";
        sqlCmd += ",exchange_rate";
        sqlCmd += ",exchange_date";
        sqlCmd += ",ec_ind";
        sqlCmd += ",transaction_source";
        sqlCmd += ",chip_condition_code";
        sqlCmd += ",auth_response_code";
        sqlCmd += ",terminal_ver_results";
        sqlCmd += ",card_seq_num";
        sqlCmd += ",unpredic_num";
        sqlCmd += ",app_tran_count";
        sqlCmd += ",app_int_pro";
        sqlCmd += ",cryptogram";
        sqlCmd += ",data_auth_code";
        sqlCmd += ",cry_info_data";
        sqlCmd += ",life_cyc_sup_ind";
        sqlCmd += ",banknet_date";
        sqlCmd += ",inter_rate_des";
        sqlCmd += ",service_code";
        sqlCmd += ",amt_mccr_num";
        sqlCmd += ",ucaf";
        sqlCmd += ",issue_s_r";
        sqlCmd += ",pos_term_capability";
        sqlCmd += ",pos_pin_capability";
        sqlCmd += ",decode(reimbursement_attr,'','0',reimbursement_attr) as h_m_bill_reimbursement_attr ";
        sqlCmd += " from bil_nccc300_dtl";
        sqlCmd += " where REFERENCE_NO = ?";
        setString(1, refNo);
        if (selectTable() > 0) {
            hMBillReasonCode = getValue("reason_code");
            hMBillExchangeRate = getValue("exchange_rate");
            hMBillExchangeDate = getValue("exchange_date");
            hMBillEcInd = getValue("ec_ind");
            hMBillTransactionSource = getValue("transaction_source");
            hMBillChipConditionCode = getValue("chip_condition_code");
            hMBillAuthResponseCode = getValue("auth_response_code");
            hMBillTerminalVerResults = getValue("terminal_ver_results");
            hMBillCardSeqNum = getValue("card_seq_num");
            hMBillUnpredicNum = getValue("unpredic_num");
            hMBillAppTranCount = getValue("app_tran_count");
            hMBillAppIntPro = getValue("app_int_pro");
            hMBillCryptogram = getValue("cryptogram");
            hMBillDataAuthCode = getValue("data_auth_code");
            hMBillCryInfoData = getValue("cry_info_data");
            hMBillLifeCycSupInd = getValue("life_cyc_sup_ind");
            hMBillBanknetDate = getValue("banknet_date");
            hMBillInterRateDes = getValue("inter_rate_des");
            hMBillServiceCode = getValue("service_code");
            hMBillAmtMccrNum = getValueDouble("amt_mccr_num");
            hMBillUcaf = getValue("ucaf");
            hMBillIssueSR = getValue("issue_s_r");
            hMBillPosTermCapability = getValue("pos_term_capability");
            hMBillPosPinCapability = getValueInt("pos_pin_capability");
            hMBillReimbursementAttr = getValue("h_m_bill_reimbursement_attr");
        }
    }
    
    void fiscRecord1Rtn() throws Exception {

        //selectBilNccc300();

        String rtn = "";
        rtn += comc.fixLeft(("62"+hMBillTransactionCode), 4); //交易代號Transaction Type
        rtn += comc.fixLeft("1", 1);  //Record 1 固放定1
        rtn += comc.fixLeft(hMBillBinType, 1);  //卡片類型Card Plan
        if (hMBillBinType.equals("M")) {
        	rtn += comc.fixLeft("543768", 11);  //Source BIN/ICA
        } else {
        	rtn += comc.fixLeft("490706", 11);  //Source BIN/ICA
        }
        rtn += comc.fixLeft("000000", 11);  //Destination BIN/ICA
        rtn += comc.fixLeft(" ", 2); //Mastercard DE61 Indicator1 
        rtn += comc.fixLeft("00000000", 8); //代理單位代號Acquirer's Business ID
        rtn += comc.fixLeft(hMBillRealCardNo, 19);  //卡片號碼Card Number
        rtn += comc.fixLeft(String.format("%010.0f00", hMBillDestinationAmt), 12); //來源端金額Source Amount
        rtn += comc.fixLeft("901", 3);  //來源端幣別 
        rtn += comc.fixLeft(String.format("%010.0f00", hMBillDestinationAmt), 12); //目的端金額
        rtn += comc.fixLeft(hMBillDestinationCurrency, 3); //目的端幣別
        rtn += comc.fixLeft(String.format("%010d00", 0), 12); //清算金額 (國內交易放0)
        rtn += comc.fixLeft(" ", 3); //清算幣別 (國內交易放空白)
        rtn += comc.fixLeft(" ", 8);  //清算匯率(國內交易放空白)
        if ("".equals(hMBillMerchantNo)) {
        	rtn += comc.fixLeft("006996295245500", 20);  //特約商店代號 (非繳稅交易放固定值)
        } else {
        	rtn += comc.fixLeft(hMBillMerchantNo, 20);  //特約商店代號 (非繳稅交易放固定值)
        }
        if ("".equals(hMBillMerchantCategory) || "0000".equals(hMBillMerchantCategory)) {
        	rtn += comc.fixLeft("6011", 4);  //特約商店行業類別碼MCC
        } else {
        	rtn += comc.fixLeft(hMBillMerchantCategory, 4);  //特約商店行業類別碼MCC
        }
        rtn += comc.fixLeft(hMBillMerchantChiName, 40);  //特店中文名稱
        if ("".equals(hMBillMerchantEngName)) {
        	rtn += comc.fixLeft("CBOT", 25);  //特約商店英文名稱
        } else {
        	rtn += comc.fixLeft(hMBillMerchantEngName, 25);  //特約商店英文名稱
        }
        if ("".equals(hMBillMerchantCity)) {
        	rtn += comc.fixLeft("TAIPEI", 13);  //特約商店英文所在地/城市
        } else {
        	rtn += comc.fixLeft(hMBillMerchantCity, 13);  //特約商店英文所在地/城市
        }
        rtn += comc.fixLeft("TW", 3); //特約商店英文所在省份
        if ("".equals(hMBillMerchantZip) || "00000".equals(hMBillMerchantZip)) {
        	rtn += comc.fixLeft("10684", 5);  //特約商店郵遞區號
        } else {
        	rtn += comc.fixLeft(hMBillMerchantZip, 5);  //特約商店郵遞區號
        }
        rtn += comc.fixLeft(hMBillMerchantCountry, 3);  //特約商店國家代號
        rtn += comc.fixLeft("", 2);  //特殊條件識別碼
        rtn += comc.fixLeft("", 15);  //次特店代號
        rtn += comc.fixLeft((comc.getSubString(hMBillPurchaseDate,4)+ comc.getSubString(hMBillPurchaseDate,2,4)), 6); //購貨日期(MMDDYY)
        rtn += comc.fixLeft("183000", 6); //購貨時間
        rtn += comc.fixLeft(hMBillAuthorization, 6); //授權碼
        rtn += comc.fixLeft("", 1); //電話/郵購識別碼
        rtn += comc.fixLeft("", 1); //Mastercard DE61 Indicator2
        rtn += comc.fixLeft("", 1); //自助端末機識別碼
        if ("".equals(getValue("terminal_id"))) {
        	rtn += comc.fixLeft("00000001", 15); //端末機代碼
        } else {
        	rtn += comc.fixLeft(getValue("terminal_id"), 15); //端末機代碼
        }
        rtn += comc.fixLeft("", 6); //端末機傳送資料日期(RRMMDD)民國年
        rtn += comc.fixLeft("", 4); //資料批次號碼
        rtn += comc.fixLeft("", 6); //端末機交易序號
        rtn += comc.fixLeft("", 1); //POS 端末機性能
        if ("".equals(hMBillPosEntryMode)) {
        	rtn += comc.fixLeft("00", 2); //POS Entry Mode
        } else {
        	rtn += comc.fixLeft(hMBillPosEntryMode, 2); //POS Entry Mode
        }
        
        rtn += comc.fixLeft(hSystemYddd, 4); //資料處理日期
        
        //hMBillFilmNo = filmNoRtn(hMBillFilmNo);  --bug
        
        if ("".equals(hMBillFilmNo)) {
        	if (hMBillBinType.equals("M")) {
        		rtn += comc.fixLeft("85437686223101647735551", 23); //微縮影片代號
            } else {
            	rtn += comc.fixLeft("74907066223101647773974", 23); //微縮影片代號
            }
        } else {
        	String tempX23 = ""; 
        			         
        	if (hMBillBinType.equals("M")) {
        		tempX23 = comc.getSubString(hMBillFilmNo,0,1) + "543768" +
        				  comc.getSubString(hMBillFilmNo,7,23);
        	} else {
        		
        		tempX23 = comc.getSubString(hMBillFilmNo,0,1) + "490706" +
      				  comc.getSubString(hMBillFilmNo,7,23);	
        	}

       		rtn += comc.fixLeft(filmNoRtn(tempX23), 23); //微縮影片代號
        }
        rtn += comc.fixLeft("1", 1); //使用碼 
        rtn += comc.fixLeft("", 4); //沖正駁回理由碼
        rtn += comc.fixLeft("", 10); //沖正參考號碼
        rtn += comc.fixLeft("", 1); //特殊沖正識別碼
        rtn += comc.fixLeft("", 1); //附寄文件識別碼
        rtn += comc.fixLeft("", 3); //Service Code
        rtn += comc.fixLeft("", 1); //UCAF
        rtn += comc.fixLeft("", 45); //訂單編號/銷帳編號
        rtn += comc.fixLeft("", 2); //繳費平台交易種類代碼
        rtn += comc.fixLeft("c", 1); //交易處理屬性   //c: collection only
        rtn += comc.fixLeft("0", 1); //清算識別碼
        rtn += comc.fixLeft("", 4); //原沖正交易訊息理由碼
        rtn += comc.fixLeft("", 2); //保留欄位
        rtn += comc.fixLeft("", 1); //支付型態
        rtn += comc.fixLeft("", 1); //跨境電子支付平台代碼
        rtn += comc.fixLeft("", 1); //跨境電子支付平台交易狀態
        rtn += comc.fixLeft("", 10); //跨境電子支付申報性質別
        rtn += comc.fixLeft("", 2); //帳單類別
        rtn += comc.fixLeft("", 2); //特店類型
        rtn += comc.fixLeft("", 5); //繳費項目/非促銷商品金額
        rtn += comc.fixLeft("", 16); //MerchantPAN/信用卡被掃TPAN
        rtn += comc.fixLeft("", 1); //電子化繳費稅註記
        rtn += comc.fixLeft("", 11); //保留欄位

        buf = rtn + "\r\n";
        writeTextFile(fo, buf);
        //lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", buf));

    }


    void fiscRecord4Rtn() throws Exception {

        //selectBilNccc300();

        String rtn = "";
        rtn += comc.fixLeft(("62"+hMBillTransactionCode), 4); //交易代號Transaction Type
        rtn += comc.fixLeft("4", 1);  //Record 4 固放定4
        rtn += comc.fixLeft(hMBillBinType, 1);  //卡片類型Card Plan
        rtn += comc.fixLeft("", 2);  //Ingerchange Rate Designator
        rtn += comc.fixLeft("", 1);  //購買預付卡識別碼
        rtn += comc.fixLeft("", 1); //黑名單識別碼 
        rtn += comc.fixLeft("", 4); //Fraud Notification Date
        rtn += comc.fixLeft("", 2);  //Fraud Notification Service Chargeback Counter
        rtn += comc.fixLeft("", 1); //特約商店限額識別碼
        rtn += comc.fixLeft("", 4);  //匯率轉換日期 
        rtn += comc.fixLeft("", 2); //保留欄位
        rtn += comc.fixLeft("", 15); //交易識別碼
        rtn += comc.fixLeft("", 2); //卡片級別識別碼
        rtn += comc.fixLeft("", 4); //授權欄位驗證碼
        rtn += comc.fixLeft("", 2);  //Multiple Clearing Sequence Number
        rtn += comc.fixLeft("", 1);  //QPS/PayPass Chargeback Eligibility
        rtn += comc.fixLeft("", 2);  //Additional Amount，Account Type
        rtn += comc.fixLeft("", 2);  //Additional Amount，Amount Type
        rtn += comc.fixLeft("", 3);  //Additional Amount，Currency Code
        rtn += comc.fixLeft("", 1);  //Additional Amount，Amount Sign
        rtn += comc.fixLeft("", 12); //Additional Amount，Amount
        rtn += comc.fixLeft("", 8);  //Surcharge Amount in cardholder billing currency
        rtn += comc.fixLeft("", 2);  //Token Assurance Level
        rtn += comc.fixLeft("", 11);  //Token Requestor ID
        rtn += comc.fixLeft("", 11);  //Payment Facilitator ID
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Card Data Input Capability
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Cardholder Authentication Capability
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Card Capture Capability
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Terminal Operating Environment
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Cardholder Present Data
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Card Present Data
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Input Mode
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Cardholder Authentication Method
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Cardholder Authentication Entity
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Card Data Output Capability
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)Terminal Data Output Capability
        rtn += comc.fixLeft("", 1); //DE22(Point of Service Data Code)PIN Capture Capability
        rtn += comc.fixLeft("", 1); //PCAS 識別碼Positive Cardholder
        rtn += comc.fixLeft("", 5); //VCIND(VISA Checkout Indicator)
        rtn += comc.fixLeft("", 12); //原始交易金額
        rtn += comc.fixLeft("", 19); //PAN TOKEN/Account Number
        rtn += comc.fixLeft("", 1); //Mastercard Cross-Border Indicator
        rtn += comc.fixLeft("", 1); //Mastercard Currency Indicator
        rtn += comc.fixLeft("", 12); //Mastercard Currency Conversion Assessment(CCA)
        rtn += comc.fixLeft("", 8); //保留欄位
        rtn += comc.fixLeft("", 3); //Fee Program Indicator(FPI)
        rtn += comc.fixLeft("", 8); //Source Currency to Base Currency
        rtn += comc.fixLeft("", 8); //Base Currency to Destination
        rtn += comc.fixLeft("", 1); //Charge Indicator
        rtn += comc.fixLeft("", 2); //保留欄位
        rtn += comc.fixLeft("", 2); //Account Number Type
        rtn += comc.fixLeft("", 29); //Payment Account Reference (PAR)
        rtn += comc.fixLeft("", 10); //VROL Case Number
        rtn += comc.fixLeft("", 1); //Acceptance Data
        rtn += comc.fixLeft("", 6); //Clearing Currency Conversion Identifier—Currency Conversion Date
        rtn += comc.fixLeft("", 1); //Clearing Currency Conversion Identifier—Currency Conversion Indicator
        rtn += comc.fixLeft("", 1); //Business Activity—Digital Wallet Interchange Override Indicator
        rtn += comc.fixLeft("", 1); //VISA Authorization Source Code
        rtn += comc.fixLeft("", 1); //VISA Persistent FX Applied Indicator
        rtn += comc.fixLeft("", 5); //VISA Rate Table ID
        rtn += comc.fixLeft("", 11); //Resubmission Code
        rtn += comc.fixLeft("", 4); //Mastercard CIT/MIT 註記
        rtn += comc.fixLeft("", 1); //VISA Additional Token Response Information
        rtn += comc.fixLeft("", 183); //保留欄位

        buf = rtn + "\r\n";
        writeTextFile(fo, buf);
        //lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", buf));

    }

    /***********************************************************************/
    String filmNoRtn(String tempX23) throws Exception {
        String tempX01 = "";
        int int2 = 0;
        int chkDig = 0;
        int num1 = 0;
        int num2 = 0;
        int tempInt = 0;
        int tempNum1 = 0;

        for (int2 = 0; int2 < 22; int2++) {
            tempX01 = String.format("%c", tempX23.charAt(int2));
            tempInt = comcr.str2int(tempX01);
            if (int2 % 2 != 0) {
                tempNum1 = 2 * tempInt;
                if (tempNum1 < 10)
                    num1 = num1 + tempNum1;
                else
                    num1 = num1 + 1 + (tempNum1 - 10);
            } else {
                num2 = num2 + tempInt;
            }
        }

        num1 = num2 + num1;
        chkDig = 10 - num1 % 10;
        if (chkDig == 10)
            chkDig = 0;

        tempX23 = String.format("%22.22s%1d", tempX23, chkDig);

        return tempX23;

    }
    
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "BilA033"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/bil", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "FISC_FTP_PUT";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
        	showLogMessage("E", "", String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode));
        }
        
        return (0);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA033 proc = new BilA033();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
// ****************************************************************************************
}
