/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  106/06/01  V1.00.00    Edson     program initial                          *
 *  109/11/23  V1.00.01   shiyuqi       updated for project coding standard   *  
 *  109/12/01  V1.00.02    JeffKung    updated for TCB.                              *
 *  110/09/22  V1.00.03    JeffKung    updated for fee_state update if not setting mcht fee  *
 *  111/12/09  V1.00.04    JeffKung  台灣菸酒手續費                                                       *
 *  112/03/30  V1.00.05    JeffKung  改成不在這支程式出報表                                          *
 *****************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommTxBill.ForeignFeeData;
import com.CommTxBill;

/*手續費轉換作業*/
public class BilA007 extends AccessDAO {
    private String progname = "手續費轉換作業   112/03/30 V1.00.05 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommTxBill commTxBill = null;

    String hCallErrorDesc = "";
    String hTempUser = "";

    int tmpInt = 0;
    int totCnt = 0;

    String prgmId = "BilA007";
    String rptId   = "";
    String rptName = "信用卡各項手續費統計總表";
    int recordCnt = 0;
    int actCnt = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq = 0;
    int errCnt = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String iFileName = "";
    String iPostDate = "";
    String hMCurpModPgm = "";
    String hMCurpModTime = "";
    String hMCurpModUser = "";
    long hMCurpModSeqno = 0;

    String hBusiBusinessDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hSystemDateF = "";
    String idxCurr = "";
    String hMCurpCardNo = "";
    String hTempCurrCode = "";
    String hMCurpAcctCode = "";
    String hMCurpBillType = "";
    String hMCurpTxnCode = "";
    String hMCurpPurchaseDate = "";
    String hMCurpReferenceNo = "";
    String hMCurpMerchantNo = "";
    String hMCurpMchtCountry = "";
    String hMCurpBatchNo = "";
    double hMCurpDestinationAmt = 0;
    String hMCurpSourceCurrency = "";
    String hMCurpDestinationCurrency = "";
    String hMCurpCardSw = "";
    String hMCurpCashAdvState = "";
    String hMCurpCurrCode = "";
    double hMCurpDcAmount = 0;
    double hMCurpDcExchangeRate = 0;
    String hMCurpRowid = "";
    String hMCurpAcctType = "";
    String hCurpBatchNo = "";
    String hCurpAcctType = "";
    String hMCurpTransactionCode = "";
    String hMCardCardType = "";
    int hMTempCnt = 0;
    String hMCurpTxConvtFlag = "";
    String hMCurpAcctitemConvtFlag = "";
    String hMCurpCurrPostFlag = "";
    String hMCurpFeesTxnCode = "";
    String hMCurpProcessDate = "";
    String hMCurpBinType = "";
    String hMCurpSettleFlag = ""; 
    String hMCurpEcsCusMchtNo = "";
    String hMCurpMchtCategory = "";
    String hMCurpGroupCode    = "";
    String hMCurpSignFlag     = "";
    
    String hBityExterDesc = "";
    String hCurpBillType = "";
    String hCurpTransactionCode = "";
    String hMCurpMerchantChiName = "";
    int newAoFlag = 0;
    String hCurpMerchantChiName = "";
    String hCurpMerchantEngName = "";
    String hTempReferenceNo = "";
    String hTempForeignFeeReferenceNo = "";
    String hCurpRowid = "";
    String hPpfeFeesBillType = "";
    String hPpfeFeesTxnCode = "";
    double hPpfeDomFixAmt = 0;
    double hPpfeDomPercent = 0;
    double hPpfeDomMinAmt = 0;
    double hPpfeDomMaxAmt = 0;
    double hPpfeIntFixAmt = 0;
    double hPpfeIntPercent = 0;
    double hPpfeIntMinAmt = 0;
    double hPpfeIntMaxAmt = 0;
    double hPpfeSwapFixAmt = 0;
    double hPpfeSwapPercent = 0;
    String hCardCardType = "";
    String hCardBinNo = "";
    String hCardBinType = "";
    String hCurpCurrCode = "";
    String hPremFeesBillType = "";
    String hPremFeesTxnCode = "";
    double hPremDomFixAmt = 0;
    double hPremDomPercent = 0;
    double hPremDomMinAmt = 0;
    double hPremDomMaxAmt = 0;
    double hPremIntFixAmt = 0;
    double hPremIntPercent = 0;
    double hPremIntMinAmt = 0;
    double hPremIntMaxAmt = 0;
    double hPremNorAmt = 0;
    double hPremNorFixAmt = 0;
    double hPremNorPercent = 0;
    double hPremSpeFixAmt = 0;
    double hPremSpePercent = 0;
    double hPremSpeAmt = 0;
    String hPremTxDateF = "";
    String hPremTxDateE = "";
    String hCurpMerchantNo = "";
    String hBitySignFlag = "";
    String hBityAcctCode = "";
    String hBityAcctItem = "";
    double hBityFeesFixAmt = 0;
    double hBityFeesPercent = 0;
    double hBityFeesMin = 0;
    double hBityFeesMax = 0;
    String hBityFeesBillType = "";
    String hBityFeesTxnCode = "";
    String hBityInterestMode = "";
    String hBityAdvWkday = "";
    String hBityBalanceState = "";
    String hBityCashAdvState = "";
    String hBityEntryAcct = "";
    double hBityMerchFee = 0;
    String hTempBillType = "";
    String hTempTransactionCode = "";
    String hPcodChiShortName = "";
    String hPcodEngShortName = "";
    String hPcodItemOrderNormal = "";
    String hPcodItemOrderBackDate = "";
    String hPcodItemOrderRefund = "";
    String hPcodItemClassNormal = "";
    String hPcodItemClassBackDate = "";
    String hPcodItemClassRefund = "";
    String hPcodAcctMethod = "";
    String hPcodQueryType = "";
    String hCardMajorCardNo = "";
    String hCardMajorIdPSeqno = "";
    String hCardCurrentCode = "";
    String hCardIssueDate = "";
    String hCardOppostDate = "";
    String hCardPromoteDept = "";
    String hCardProdNo = "";
    String hCardGroupCode = "";
    String hCardSourceCode = "";
    String hCardAcctPSeqno = "";
    String hCardPSeqno = "";
    String hCardComboAcctNo = "";
    String hCardId = "";
    String hCardIdPSeqno = "";
    double hIdnoExceptionVvFee = 0;
    String hCurpCardNo = "";
    String hPbtbCurrCode = "";
    String hPrintName = "";
    String hRptName = "";
    int hTempCnt = 0;
    String hAcnoAcctType = "";
    String hAcnoPSeqno = "";
    String hCurpPurchaseDate = "";
    double hCurpDestinationAmt = 0;
    String hActcUseSorce = "";
    String hActcAcctMonth = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    long hTempDestinationAmt = 0;
    double hTempDCAmt = 0;
    String hCurpCardSw = "";
    String hTempX08 = "";
    String hCurpPurchaseTime = "";
    String hCurpDestinationCurrency = "";
    String hCurpProcessDate = "";
    String hCurpFeesTxnCode = "";
    String hCurpReferenceNo = "";
    String hCurpTxConvtFlag = "";
    String hCurpAcctitemConvtFlag = "";
    String hCurpCurrPostFlag = "";
    double hTempDcAmount = 0;
    String hCurpDcExchangeRate = "";
    double hPcomDayFeePercent = 0;
    double hPcomDayFeeFixAmt = 0;
    double hPcomDayFeeHighestAmt = 0;
    double hPcomDayFeeLowestAmt = 0;
    int hPcomDayFeeCnt = 0;
    String hPcomFeeFlag = "";
    String hPcomProgramName = "";
    String hAcomProgramCode = "";
    String hAcomCommuteBank = "";
    String hAcomCommuteSeqno = "";
    String hPcomSpecDiscFlag = "";
    double hPcomSpeDisc = 0;
    long hTempGroupCode = 0;
    long hTempSourceCode = 0;
    String hAcomRowid = "";
    String hPbalBankName = "";

    double hThisBusiAddAmt = 0;
    long hSrcPgmPostseq = 0;
    String hPostNote = "手續費 by batch_no,bill_type,txn_code";

    String[] hMPbtbDcCurrCode = new String[250];
    int ptrBintableCnt = 0;
    int totalCount = 0;
    int srcFreeFlag = 0;
    int pageCnt = 0;
    double tempDouble = 0;
    long tempLong = 0;
    String swSubtotal = "";
    int subtotalCount = 0;
    double subtotalAmt = 0;
    int sumCount = 0;
    double sumAmt = 0;
    int lineCnt = 0;
    String tempX20 = "";
    int indexCnt = 0;

    // *********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commTxBill = new CommTxBill(getDBconnect(),getDBalias());

            selectPtrBusinday();
            showLogMessage("I", "", "Process_date = " + hBusiBusinessDate);

            selectPtrBintable0();

            for (int int0 = 0; int0 < ptrBintableCnt; int0++) {
                String dcCurrCode = hMPbtbDcCurrCode[int0];
                
                /* debug
                    showLogMessage("D", "", "  Process curr = " + dcCurrCode);
                */

                totalCount = 0;
                //rptId = String.format("%s_%s", "BIL_A007R1", dcCurrCode);
                
                //String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptId, hSystemDateF);

                selectBilCurpost(dcCurrCode);
                //printBilCurpost(dcCurrCode);

                showLogMessage("I", "", String.format("** 幣別:[%s] 筆數=[%d]", hMPbtbDcCurrCode[int0], totalCount));
                
                /*不寫報表
                //如果沒有資料也要寫
                if (totalCount == 0) {
                    hMCurpBatchNo = "";
                    printHeader(dcCurrCode);
                    lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "2", "***本日無資料***"));
                }
                */

                //comcr.insertPtrBatchRpt(lpar1);
                //comc.writeReport(filename, lpar1);
                //lpar1.clear();
            }

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCount + "]");

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    // ***********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'yyyy/mm/dd hh24:mi:ss') h_system_time,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += "from ptr_businday ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
            hSystemTime = getValue("h_system_time");
            hSystemDateF = getValue("h_system_date_f");
        }

        hModUser = comc.commGetUserID();
        hMCurpModUser = hModUser;
    }

    /***********************************************************************/
    void selectPtrBintable0() throws Exception {

        sqlCmd  = "select decode(dc_curr_code,'','901',dc_curr_code) h_m_pbtb_dc_curr_code ";
        sqlCmd += "  from ptr_bintable ";
        sqlCmd += " group by decode(dc_curr_code,'','901',dc_curr_code) ";
        recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMPbtbDcCurrCode[i] = getValue("h_m_pbtb_dc_curr_code", i);
        }

        ptrBintableCnt = recordCnt;

    }

    /**********************************************************************/
    void selectBilCurpost(String idxCurr) throws Exception {

        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "a.txn_code,";
        sqlCmd += "purchase_date,";
        sqlCmd += "reference_no,";
        sqlCmd += "a.mcht_no,";
        sqlCmd += "a.batch_no,";
        sqlCmd += "dest_amt                                    as h_m_curp_destination_amt,";
        sqlCmd += "decode(source_curr,'TWD','901',source_curr) as h_m_curp_source_currency,";
        sqlCmd += "decode(dest_curr,'TWD','901',dest_curr)     as h_m_curp_destination_currency,";
        sqlCmd += "upper(cash_adv_state)                       as h_m_curp_cash_adv_state,";
        sqlCmd += "a.bin_type ,";
        sqlCmd += "a.mcht_country,";
        sqlCmd += "a.mcht_eng_name, "; 
        sqlCmd += "a.settl_flag ,";
        sqlCmd += "a.ecs_platform_kind,";
        sqlCmd += "a.ecs_cus_mcht_no,";
        sqlCmd += "a.group_code, a.mcht_category, a.sign_flag, ";
        sqlCmd += "a.curr_code,";
        sqlCmd += "a.dc_amount,";
        sqlCmd += "a.dc_exchange_rate,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from bil_curpost a, bil_postcntl b ";
        sqlCmd += "where (decode(fees_state        ,'','N',fees_state)         in ('Y','y') or ";
        sqlCmd += "       decode(cash_adv_state    ,'','N',cash_adv_state)     in ('Y','y') ) ";
        sqlCmd += " and   decode(entry_acct        ,'','N',entry_acct)         in ('Y','y') ";
        sqlCmd += " and   decode(contract_flag     ,'','N',contract_flag )     in ('P','p','N','n') "; // 分期查核旗標
        sqlCmd += " and ( decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n') and ";
        sqlCmd += "       decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('N','n') and ";
        sqlCmd += "       decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag)    in ('N','n') and ";
        sqlCmd += "       decode(rsk_type          ,'','N',rsk_type )          not in ('1','2','3') ) "; //rskType=1,2,3 落問交的不用處理
        sqlCmd += " and ( curr_post_flag  in ('N','n') or curr_post_flag ='') ";
        sqlCmd += " and   decode(confirm_flag_p    ,'','N',confirm_flag_p)     in ('Y','y') ";
        sqlCmd += " and   decode(manual_upd_flag   ,'','N',manual_upd_flag)    != ('Y') ";
        sqlCmd += " and   decode(valid_flag        ,'','N',valid_flag)         != ('Y') ";
        sqlCmd += " and   decode(a.curr_code       ,'','901',a.curr_code)      = ? ";
        sqlCmd += " and   batch_date  = substr(a.batch_no,1,8) ";
        sqlCmd += " and   batch_unit  = substr(a.batch_no,9,2) ";
        sqlCmd += " and   batch_seq   = substr(a.batch_no,11,4) ";
        setString(1, idxCurr);
        
        /* debug
            showLogMessage("D", "", "  888 CURR    = " + idxCurr);
        */
        
        openCursor();
        while (fetchTable()) {
            hMCurpCardNo = getValue("card_no");
            hMCurpBillType = getValue("bill_type");
            hMCurpTxnCode     = getValue("txn_code");
            hMCurpPurchaseDate = getValue("purchase_date");
            hMCurpReferenceNo = getValue("reference_no");
            hMCurpMerchantNo = getValue("mcht_no");
            hMCurpMchtCountry   = getValue("mcht_country");
            hMCurpBatchNo = getValue("batch_no");
            hMCurpDestinationAmt = getValueDouble("h_m_curp_destination_amt");
            hMCurpSourceCurrency = getValue("h_m_curp_source_currency");
            hMCurpDestinationCurrency = getValue("h_m_curp_destination_currency");
            hMCurpCardSw = getValue("bin_type");
            hMCurpCashAdvState = getValue("h_m_curp_cash_adv_state");
            hMCurpCurrCode = getValue("curr_code");
            hMCurpBinType = getValue("bin_type");
            hMCurpDcAmount = getValueDouble("dc_amount");
            hMCurpDcExchangeRate = getValueDouble("dc_exchange_rate");
            hMCurpSettleFlag          = getValue("settl_flag");
            hMCurpEcsCusMchtNo    = getValue("ecs_cus_mcht_no");
            hCurpMerchantEngName  = getValue("mcht_eng_name");
            hMCurpMchtCategory = getValue("mcht_category");
            hMCurpGroupCode    = getValue("group_code");
            hMCurpSignFlag     = getValue("sign_flag");
            hMCurpRowid = getValue("rowid");

            srcFreeFlag = 0;

            totalCount++;
            
            /* debug
                showLogMessage("D", "", "  888 Begin ");
                showLogMessage("D", "", "      card="+ hMCurpCardNo +","+ hMCurpReferenceNo +",type="+ hMCurpBillType +"["+ hMCurpDestinationCurrency +"]"+ hMCurpSourceCurrency);
            */

            selectCrdCard(); /* 先抓card_type */
            selectPtrBintable();
            selectActAcno();
            
			hTempReferenceNo = "";
			hTempForeignFeeReferenceNo = "";

			// 若清算識別碼=0，則為國外清算
			ForeignFeeData foreignFeeData = null;

			if (hMCurpSettleFlag.equals("0")) {
				// 計算國外手續費
				foreignFeeData = computeForeignFee(idxCurr);
				hTempDestinationAmt = (long) (foreignFeeData.getForeignFee());
				hTempDCAmt =  foreignFeeData.getDcForeignFee();
				
				//如果是日幣或台幣取整數
				if (hMCurpDcExchangeRate <= 1) {
					hTempDCAmt = (int) (foreignFeeData.getDcForeignFee() + 0.5);
		        }
				
				hTempBillType = "FIFC";
				hTempTransactionCode = "05";

				if (hTempDestinationAmt > 0 || hTempDCAmt > 0) {
					selectPtrBilltype();
					selectPtrActcode();
					insertBilCurpost("ForeignFee");
				}
			}
            
            newAoFlag = 0;  //判斷是不是代償交易

            if (newAoFlag == 0) {
            	
            	//OSSG-TL 台灣菸酒手續費 ; OSSG-17 退貨-退還交易手續費
            	//台酒卡 1299 & 3782 ; //自行沒有帶MCC=5921為台酒特店消費
                if( "FISC".equals(hMCurpBillType) && ("1299".equals(hMCurpGroupCode) || "3782".equals(hMCurpGroupCode)) ) {
                	//正向
                	if ("-".equals(hMCurpSignFlag) == false) {
                		hTempBillType = "OSSG";
        				hTempTransactionCode = "TL";
                	} else {  //負向退手續費
                		hTempBillType = "OSSG";
        				hTempTransactionCode = "17";
                	}
                	
                	hTempDestinationAmt = (long)( Math.round(hMCurpDestinationAmt*0.005));
                	
                //OSSG-PU 公用事業代繳手續費用
                //group_code = 3700 & 3720 & 金額>=30000; 
            	} else if( "CHUP".equals(hMCurpBillType) 
            			&& ("3700".equals(hMCurpGroupCode) || "3720".equals(hMCurpGroupCode)) 
            			&& hMCurpDestinationAmt >= 30000 ) {
                	//正向
                	if ("-".equals(hMCurpSignFlag) == false) {
                		hTempBillType = "OSSG";
        				hTempTransactionCode = "PU";
                	} else {  //負向退手續費
                		hTempBillType = "OSSG";
        				hTempTransactionCode = "17";
                	}
                	
                	hTempDestinationAmt = (long)( Math.round(hMCurpDestinationAmt*0.0015));
                	
                
            	} else if(hMCurpCashAdvState.substring(0, 1).equals("Y")) {
                    
                    selectPtrPrepaidfee();
                    
                } else {
                    /* 新增若特店未設手續費參數跳過該筆不予處理 ... Wincard */
                	
                    /* debug
                    	showLogMessage("D", "", "  888 2 ptr_prepaidfee_m="+ hMCurpCardNo);
                    */
                    
                    
                    if (selectPtrPrepaidfeeM() == false) {
                    	updateBilCurpost();  //也要異動fee_state, 不然無法入帳
                        continue;
                    }
                }
            }
            
            //算出來的手續費==0不用寫bil_curpost
            if (hTempDestinationAmt == 0) {
            	updateBilCurpost();  //也要異動fee_state, 不然無法入帳
                continue;
            }
            
            selectPtrBilltype();
            selectPtrActcode();

            if (srcFreeFlag == 0) {
                insertBilCurpost("");
            } else {
            	//免預借現金手續費記錄檔
                insertBilAdvfreeLog();
            }
            updateBilCurpost();
        }
        closeCursor();
    }

    /***********************************************************************/
    void printHeader(String idxCurr) {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: BIL_A007R1", 1);
        buf = comcr.insertStrCenter(buf, "信用卡  各項手續費統計總表", 80);
        buf = comcr.insertStr(buf, "頁    次:", 62);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 74);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日期:", 1);
        buf = comcr.insertStr(buf, chinDate, 11);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "入帳日期:", 1);
        szTmp = String.format("%8d", comcr.str2long(hBusiBusinessDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 11);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        /* 搬到明細
        buf = "";
        buf = comcr.insertStr(buf, "批    號:", 1);
        buf = comcr.insertStr(buf, hMCurpBatchNo, 11);
        buf = comcr.insertStr(buf, "幣    別:", 62);
        buf = comcr.insertStr(buf, idxCurr, 71);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
        */

        buf = "";
        buf = comcr.insertStr(buf, "卡        種             筆    數     金       額 ", 1);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        for (int i = 0; i < 80; i++)
            buf += "=";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /*********************************************************************/
    void selectPtrBintable() throws Exception {
        hPbtbCurrCode = "";

        sqlCmd = "select a.dc_curr_code ";
        sqlCmd += "  from ptr_bintable a ";
        sqlCmd += " where 1=1          ";
        sqlCmd += "   and bin_no || bin_no_2_fm || '0000' <= ?  ";
        sqlCmd += "   and bin_no || bin_no_2_to || '9999' >= ?  ";
        setString(1, hMCurpCardNo);
        setString(2, hMCurpCardNo);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hPbtbCurrCode = getValue("curr_code");
        }
    }
    /**********************************************************************/
    void selectCrdCard() throws Exception {
        sqlCmd = "select a.major_card_no,";
        sqlCmd += "a.current_code,";
        sqlCmd += "a.issue_date,";
        sqlCmd += "a.oppost_date,";
        sqlCmd += "a.promote_dept,";
        sqlCmd += "a.prod_no,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.source_code,";
        sqlCmd += "a.card_type,";
        sqlCmd += "a.bin_no,";
        sqlCmd += "a.bin_type,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.major_id_p_seqno,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.combo_acct_no,";
        sqlCmd += "b.id_no as id,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "decode(b.exception_vv_fee,0 , 0,b.exception_vv_fee) h_idno_exception_vv_fee ";   //中信繳學費手續費(ID層指定)
        sqlCmd += " from crd_card a, crd_idno b  ";
        sqlCmd += "where a.card_no    = ?  ";
        sqlCmd += "  and a.id_p_seqno = b.id_p_seqno ";
        setString(1, hMCurpCardNo);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
            hCardMajorCardNo = getValue("major_card_no");
            hCardCurrentCode = getValue("current_code");
            hCardIssueDate = getValue("issue_date");
            hCardOppostDate = getValue("oppost_date");
            hCardPromoteDept = getValue("promote_dept");
            hCardProdNo = getValue("prod_no");
            hCardGroupCode = getValue("group_code");
            hCardSourceCode = getValue("source_code");
            hCardCardType = getValue("card_type");
            hCardBinNo = getValue("bin_no");
            hCardBinType = getValue("bin_type");
            hCardPSeqno = getValue("acno_p_seqno");
            hCardAcctPSeqno = getValue("p_seqno");
            hCardComboAcctNo = getValue("combo_acct_no");
            hCardId = getValue("id");
            hCardIdPSeqno = getValue("id_p_seqno");
            hIdnoExceptionVvFee = getValueDouble("h_idno_exception_vv_fee)");
        }

    }

    /***********************************************************************/
    void selectActAcno() throws Exception {

        sqlCmd = "select acct_type,";
        sqlCmd += "acno_p_seqno ,";
        sqlCmd += "acct_status,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "pay_by_stage_flag,";
        sqlCmd += "autopay_acct_no ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno  = ? ";
        setString(1, hCardAcctPSeqno);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hAcnoAcctType = getValue("acct_type");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
        }

    }

    /**
	 * 計算國外手續費= foreignFee + markupFee
	 * @param bilFiscdtl
	 * @param destAmt 
	 * @throws Exception 
	 */
	private ForeignFeeData computeForeignFee(String dcCurr ) throws Exception {
		ForeignFeeData foreignFeeData = commTxBill.new ForeignFeeData();
		
		// 若為負向交易，則不收手續費
		// 否則計算海外交易手續費
		if(  !hMCurpTxnCode.equals("05") && ! hMCurpTxnCode.equals("07")  ) {
			foreignFeeData.setForeignFee(0.0);
			foreignFeeData.setDcForeignFee(0.0);
		}else {
			foreignFeeData = commTxBill.getForeignFeeData(hMCurpBinType, hMCurpMchtCountry,
					hMCurpDestinationCurrency, hMCurpSourceCurrency , "N", dcCurr, hMCurpDestinationAmt,
					hMCurpDcAmount );
		}
		
		return foreignFeeData;
		
	}
    /***********************************************************************/
    void selectPtrPrepaidfee() throws Exception
    {
    	hTempDcAmount = 0;
        hPpfeDomFixAmt = 0;
        hPpfeDomPercent = 0;
        hPpfeDomMinAmt = 0;
        hPpfeDomMaxAmt = 0;
        hPpfeIntFixAmt = 0;
        hPpfeIntPercent = 0;
        hPpfeIntMinAmt = 0;
        hPpfeIntMaxAmt = 0;
        hPpfeSwapFixAmt = 0;
        hPpfeSwapPercent = 0;

        sqlCmd = "select fees_bill_type,";
        sqlCmd += "fees_txn_code,";
        sqlCmd += "dom_fix_amt  as h_ppfe_dom_fix_amt,";
        sqlCmd += "dom_percent  as h_ppfe_dom_percent,";
        sqlCmd += "dom_min_amt  as h_ppfe_dom_min_amt,";
        sqlCmd += "dom_max_amt  as h_ppfe_dom_max_amt,";
        sqlCmd += "int_fix_amt  as h_ppfe_int_fix_amt,";
        sqlCmd += "int_percent  as h_ppfe_int_percent,";
        sqlCmd += "int_min_amt  as h_ppfe_int_min_amt,";
        sqlCmd += "int_max_amt  as h_ppfe_int_max_amt,";
        sqlCmd += "swap_fix_amt as h_ppfe_swap_fix_amt,";
        sqlCmd += "swap_percent as h_ppfe_swap_percent ";
        sqlCmd += " from ptr_prepaidfee  ";
        sqlCmd += "where card_type = ?  ";
        sqlCmd += "  and decode(curr_code,'','901',curr_code) = ";
        sqlCmd += "      decode(cast(? as varchar(10)) ,'','901',?) ";
        setString(1, hCardCardType);
        setString(2, hMCurpCurrCode);
        setString(3, hMCurpCurrCode);
        tmpInt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_prepaidfee not found!", "", comcr.hCallBatchSeqno);
        }
        if (tmpInt > 0) {
            hPpfeFeesBillType = getValue("fees_bill_type");
            hPpfeFeesTxnCode = getValue("fees_txn_code");
            hPpfeDomFixAmt = getValueDouble("h_ppfe_dom_fix_amt");
            hPpfeDomPercent = getValueDouble("h_ppfe_dom_percent");
            hPpfeDomMinAmt = getValueDouble("h_ppfe_dom_min_amt");
            hPpfeDomMaxAmt = getValueDouble("h_ppfe_dom_max_amt");
            hPpfeIntFixAmt = getValueDouble("h_ppfe_int_fix_amt");
            hPpfeIntPercent = getValueDouble("h_ppfe_int_percent");
            hPpfeIntMinAmt = getValueDouble("h_ppfe_int_min_amt");
            hPpfeIntMaxAmt = getValueDouble("h_ppfe_int_max_amt");
            hPpfeSwapFixAmt = getValueDouble("h_ppfe_swap_fix_amt");
            hPpfeSwapPercent = getValueDouble("h_ppfe_swap_percent");
        }

        hTempBillType = hPpfeFeesBillType;
        hTempTransactionCode = hPpfeFeesTxnCode;

        if(hMCurpDcAmount > 0 && hMCurpCurrCode.equals("901") == false)
        {
            // 外幣
            tempDouble = (hPpfeIntPercent * hMCurpDcAmount) / 100;
            tempDouble = hPpfeIntFixAmt + tempDouble;
            hTempDcAmount = comcr.commCurrAmt(hMCurpCurrCode, tempDouble, 0);
            if(hTempDcAmount > hPpfeIntMaxAmt)
                hTempDcAmount = hPpfeIntMaxAmt;
            if(hTempDcAmount < hPpfeIntMinAmt)
                hTempDcAmount = hPpfeIntMinAmt;
            tempLong = (long) (hTempDcAmount * hMCurpDcExchangeRate + 0.5);
            hTempDestinationAmt = tempLong;
        }
        else
        {
            if(hMCurpSourceCurrency.equals(hPbtbCurrCode))
            {
            	 // 一般預現
                hTempDestinationAmt = (long)(hPpfeDomFixAmt + hPpfeDomPercent * hMCurpDestinationAmt /100);
                if(hTempDestinationAmt > hPpfeDomMaxAmt)
                    hTempDestinationAmt = (long) hPpfeDomMaxAmt;
                if(hTempDestinationAmt < hPpfeDomMinAmt)
                    hTempDestinationAmt = (long) hPpfeDomMinAmt;
               
                /* 線上結匯是別的銀行的功能先不判斷
                if(hCurpMerchantNo.equals("730460000000023") || hCurpMerchantNo.equals("730460000000032") )
                {
                    // 線上結匯
                    hTempDestinationAmt = (long)(hPpfeSwapFixAmt + hPpfeSwapPercent * hCurpDestinationAmt /100);
                }
                else
                {
                    // 一般預現
                    hTempDestinationAmt = (long)(hPpfeDomFixAmt + hPpfeDomPercent * hMCurpDestinationAmt /100);
                    if(hTempDestinationAmt > hPpfeDomMaxAmt)
                        hTempDestinationAmt = (long) hPpfeDomMaxAmt;
                    if(hTempDestinationAmt < hPpfeDomMinAmt)
                        hTempDestinationAmt = (long) hPpfeDomMinAmt;
                }
                */
                
                
            }
            else
            {
                // 國外預現
                hTempDestinationAmt = (long)(hPpfeIntFixAmt + hPpfeIntPercent * hMCurpDestinationAmt /100);
                if(hTempDestinationAmt > hPpfeIntMaxAmt)
                    hTempDestinationAmt = (long) hPpfeIntMaxAmt;
                if(hTempDestinationAmt < hPpfeIntMinAmt)
                    hTempDestinationAmt = (long) hPpfeIntMinAmt;
            }
        }
    }
    /***********************************************************************/
    boolean selectPtrPrepaidfeeM() throws Exception {
    	
    	hTempDcAmount = 0;

        sqlCmd = "select fees_bill_type,";
        sqlCmd += "fees_txn_code,";
        sqlCmd += "dom_fix_amt h_prem_dom_fix_amt,";
        sqlCmd += "dom_percent h_prem_dom_percent,";
        sqlCmd += "dom_min_amt h_prem_dom_min_amt,";
        sqlCmd += "dom_max_amt h_prem_dom_max_amt,";
        sqlCmd += "int_fix_amt h_prem_int_fix_amt,";
        sqlCmd += "int_percent h_prem_int_percent,";
        sqlCmd += "int_min_amt h_prem_int_min_amt,";
        sqlCmd += "int_max_amt h_prem_int_max_amt,";
        sqlCmd += "nor_amt h_prem_nor_amt,";
        sqlCmd += "nor_fix_amt h_prem_nor_fix_amt,";
        sqlCmd += "nor_percent h_prem_nor_percent,";
        sqlCmd += "spe_fix_amt h_prem_spe_fix_amt,";
        sqlCmd += "spe_percent h_prem_spe_percent,";
        sqlCmd += "spe_amt h_prem_spe_amt,";
        sqlCmd += "tx_date_f,";
        sqlCmd += "tx_date_e ";
        sqlCmd += " from ptr_prepaidfee_m  ";
        sqlCmd += "where merchant_no = ? ";
        setString(1, hMCurpEcsCusMchtNo);  //TCB要使用轉換過自行編的特店代號
        tmpInt = selectTable();
        
        /* debug
            showLogMessage("D", "", "  888 prepaidfee_m 1="+ hMCurpMerchantNo +","+ tmpInt);
        */
        
        /* 20040115新增未設特店手續費時該筆跳過不處理 */
        if (notFound.equals("Y")) {
            //showLogMessage("I", "", "select_ptr_prepaidfee_m not Found mcht_no : " + hMCurpMerchantNo);
            return false;
        }
        if (tmpInt > 0) {
            hPremFeesBillType = getValue("fees_bill_type");
            hPremFeesTxnCode = getValue("fees_txn_code");
            hPremDomFixAmt = getValueDouble("h_prem_dom_fix_amt");
            hPremDomPercent = getValueDouble("h_prem_dom_percent");
            hPremDomMinAmt = getValueDouble("h_prem_dom_min_amt");
            hPremDomMaxAmt = getValueDouble("h_prem_dom_max_amt");
            hPremIntFixAmt = getValueDouble("h_prem_int_fix_amt");
            hPremIntPercent = getValueDouble("h_prem_int_percent");
            hPremIntMinAmt = getValueDouble("h_prem_int_min_amt");
            hPremIntMaxAmt = getValueDouble("h_prem_int_max_amt");
            hPremNorAmt = getValueDouble("h_prem_nor_amt");
            hPremNorFixAmt = getValueDouble("h_prem_nor_fix_amt");
            hPremNorPercent = getValueDouble("h_prem_nor_percent");
            hPremSpeFixAmt = getValueDouble("h_prem_spe_fix_amt");
            hPremSpePercent = getValueDouble("h_prem_spe_percent");
            hPremSpeAmt = getValueDouble("h_prem_spe_amt");
            hPremTxDateF = getValue("tx_date_f");
            hPremTxDateE = getValue("tx_date_e");
        }

        hTempBillType = hPremFeesBillType;
        hTempTransactionCode = hPremFeesTxnCode;

        if (hMCurpPurchaseDate.compareTo(hPremTxDateF) >= 0 &&
                hMCurpPurchaseDate.compareTo(hPremTxDateE) <= 0) {
            if (hMCurpDestinationAmt <= hPremSpeAmt) {
                hTempDestinationAmt = (long) (hPremSpeFixAmt + hPremSpePercent * hMCurpDestinationAmt / 100);
                if (hTempDestinationAmt > hPremIntMaxAmt)
                    hTempDestinationAmt = (long) hPremIntMaxAmt;
                if (hTempDestinationAmt < hPremIntMinAmt)
                    hTempDestinationAmt = (long) hPremIntMinAmt;
            } else {
                hTempDestinationAmt = (long) (hPremIntFixAmt + hPremIntPercent * hMCurpDestinationAmt / 100);
                if (hTempDestinationAmt > hPremIntMaxAmt)
                    hTempDestinationAmt = (long) hPremIntMaxAmt;
                if (hTempDestinationAmt < hPremIntMinAmt)
                    hTempDestinationAmt = (long) hPremIntMinAmt;
            }
        } else {
            if (hMCurpDestinationAmt <= hPremNorAmt) {
                hTempDestinationAmt = (long) (hPremNorFixAmt + hPremNorPercent * hMCurpDestinationAmt / 100);
                if (hTempDestinationAmt > hPremDomMaxAmt)
                    hTempDestinationAmt = (long) hPremDomMaxAmt;
                if (hTempDestinationAmt < hPremDomMinAmt)
                    hTempDestinationAmt = (long) hPremDomMinAmt;
            } else {
                hTempDestinationAmt = (long) (hPremDomFixAmt + hPremDomPercent * hMCurpDestinationAmt / 100);
                if (hTempDestinationAmt > hPremDomMaxAmt)
                    hTempDestinationAmt = (long) hPremDomMaxAmt;
                if (hTempDestinationAmt < hPremDomMinAmt)
                    hTempDestinationAmt = (long) hPremDomMinAmt;
            }
        }
        return true;
    }

    /**********************************************************************/
    void selectPtrBilltype() throws Exception {

        sqlCmd = "select acct_code,";
        sqlCmd += "acct_item,";
        sqlCmd += "sign_flag,";
        sqlCmd += "fees_fix_amt,";
        sqlCmd += "fees_percent,";
        sqlCmd += "fees_min,";
        sqlCmd += "fees_max,";
        sqlCmd += "fees_bill_type,";
        sqlCmd += "fees_txn_code,";
        sqlCmd += "exter_desc,";
        sqlCmd += "interest_mode,";
        sqlCmd += "adv_wkday,";
        sqlCmd += "balance_state,";
        sqlCmd += "cash_adv_state,";
        sqlCmd += "entry_acct,";
        sqlCmd += "merch_fee ";
        sqlCmd += " from ptr_billtype  ";
        sqlCmd += "where bill_type = ?  ";
        sqlCmd += "  and txn_code  = ? ";
        setString(1, hTempBillType);
        setString(2, hTempTransactionCode);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hBitySignFlag = getValue("sign_flag");
            hBityAcctCode = getValue("acct_code");
            hBityAcctItem = getValue("acct_item");
            hBityFeesFixAmt = getValueDouble("fees_fix_amt");
            hBityFeesPercent = getValueDouble("fees_percent");
            hBityFeesMin = getValueDouble("fees_min");
            hBityFeesMax = getValueDouble("fees_max");
            hBityFeesBillType = getValue("fees_bill_type");
            hBityFeesTxnCode = getValue("fees_txn_code");
            hBityExterDesc = getValue("exter_desc");
            hBityInterestMode = getValue("interest_mode");
            hBityAdvWkday = getValue("adv_wkday");
            hBityBalanceState = getValue("balance_state");
            hBityCashAdvState = getValue("cash_adv_state");
            hBityEntryAcct = getValue("entry_acct");
            hBityMerchFee = getValueDouble("merch_fee");
        }

    }

    /**********************************************************************/
    void selectPtrActcode() throws Exception {
        sqlCmd = "select chi_short_name,";
        sqlCmd += "eng_short_name,";
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "acct_method,";
        sqlCmd += "query_type ";
        sqlCmd += "from ptr_actcode  ";
        sqlCmd += "where acct_code = ? ";
        setString(1, hBityAcctCode);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hPcodChiShortName = getValue("chi_short_name");
            hPcodEngShortName = getValue("eng_short_name");
            hPcodItemOrderNormal = getValue("item_order_normal");
            hPcodItemOrderBackDate = getValue("item_order_back_date");
            hPcodItemOrderRefund = getValue("item_order_refund");
            hPcodItemClassNormal = getValue("item_class_normal");
            hPcodItemClassBackDate = getValue("item_class_back_date");
            hPcodItemClassRefund = getValue("item_class_refund");
            hPcodAcctMethod = getValue("acct_method");
            hPcodQueryType = getValue("query_type");
        }

    }

    /***********************************************************************/
    void insertBilCurpost(String feeType) throws Exception {
        hTempX08 = "";
        sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_temp_x08 ";
        sqlCmd += "from dual ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hTempX08 = getValue("h_temp_x08");
        }
        
        if ("ForeignFee".equals(feeType)) {
        	hTempForeignFeeReferenceNo = String.format("%2.2s%s", hBusiBusinessDate.substring(2), hTempX08);
        } else {
        	hTempReferenceNo = String.format("%2.2s%s", hBusiBusinessDate.substring(2), hTempX08);
        }
 
        String tempSourceCurrCode = "";
        double tempSourceAmt = 0;
        if (hMCurpCurrCode.equals("901") || hMCurpCurrCode.length() == 0) {
            hMCurpCurrCode = "901";
            tempSourceCurrCode = hMCurpDestinationCurrency;
            tempSourceAmt = hTempDestinationAmt;
            hTempDcAmount = hTempDestinationAmt;
        } else {  //雙幣
        	tempSourceCurrCode = hMCurpCurrCode;
            tempSourceAmt = hTempDCAmt;
            hTempDcAmount = hTempDCAmt;
        }
        
        /* debug
            showLogMessage("D", "", "  insert ref=["+ hTempReferenceNo + "]"+ hTempDestinationAmt +",["+ hMCurpDestinationCurrency +"]");
        */

        daoTable = "bil_curpost ";
        
        String tmpMchtChiName = "";
        tmpMchtChiName = hBityExterDesc;
        if ("ForeignFee".equals(feeType)) {
        	setValue("reference_no"    , hTempForeignFeeReferenceNo);
        	tmpMchtChiName = String.format("%10.10s-%14.14s", hCurpMerchantEngName, hBityExterDesc);
        } else {
        	setValue("reference_no"    , hTempReferenceNo);
        }
        
        setValue("this_close_date" , hBusiBusinessDate);
        setValue("bill_type"       , hTempBillType);
        setValue("txn_code"        , hTempTransactionCode);
        setValue("card_no"         , hMCurpCardNo);
        setValue("purchase_date"   , hMCurpPurchaseDate);
        setValueDouble("dest_amt"  , hTempDestinationAmt);
        setValue("dest_curr"       , hMCurpDestinationCurrency);
        setValueDouble("source_amt", tempSourceAmt);
        setValue("source_curr"     , tempSourceCurrCode);
        setValue("process_date"    , hMCurpProcessDate);
        setValue("mcht_no"         , hMCurpMerchantNo);
        setValue("mcht_chi_name"   , tmpMchtChiName);
        setValue("batch_no" , hMCurpBatchNo);
        setValue("sign_flag", hBitySignFlag);
        setValue("acct_code", hBityAcctCode);
        setValue("acct_item", hBityAcctItem);
        setValue("acct_eng_short_name", hPcodEngShortName);
        setValue("acct_chi_short_name", hPcodChiShortName);
        setValue("item_order_normal", hPcodItemOrderNormal);
        setValue("item_order_back_date", hPcodItemOrderBackDate);
        setValue("item_order_refund", hPcodItemOrderRefund);
        setValue("acexter_desc", hBityExterDesc);
        setValue("entry_acct", hBityEntryAcct);
        setValue("item_class_normal", hPcodItemClassNormal);
        setValue("item_class_back_date", hPcodItemClassBackDate);
        setValue("item_class_refund"   , hPcodItemClassRefund);
        setValue("fees_state"          , "N");
        setValue("cash_adv_state"      , hBityCashAdvState);
        setValue("acct_type"           , hAcnoAcctType);
        setValue("stmt_cycle"          , hAcnoStmtCycle);
        setValue("major_card_no"       , hCardMajorCardNo);
        setValue("major_id_p_seqno"    , hCardMajorIdPSeqno);
        setValue("issue_date"          , hCardIssueDate);
        setValue("promote_dept"        , hCardPromoteDept);
        setValue("prod_no", hCardProdNo);
        setValue("group_code", hCardGroupCode);
        setValue("acno_p_seqno"   , hCardPSeqno);
        setValue("p_seqno"     , hCardAcctPSeqno);
        setValue("id_p_seqno", hCardIdPSeqno);
        setValue("reference_no_original" , hMCurpReferenceNo);
        setValue("tx_convt_flag"         , hMCurpTxConvtFlag);
        setValue("acctitem_convt_flag"   , hMCurpAcctitemConvtFlag);
        setValue("format_chk_ok_flag"    , "N");
        setValue("double_chk_ok_flag"    , "N");
        setValue("err_chk_ok_flag"       , "N");
        setValue("contract_flag"         , "P");
        setValue("curr_post_flag"        , hMCurpCurrPostFlag);
        setValue("source_code"           , hCardSourceCode);
        setValue("mod_time"              , hSystemDateF);
        setValue("bin_type"              , hMCurpBinType);
        setValue("curr_code"             , hMCurpCurrCode);
        setValueDouble("dc_amount"       , hTempDcAmount);
        setValueDouble("dc_exchange_rate", hMCurpDcExchangeRate);
        setValueDouble("cash_pay_amt"    , hTempDestinationAmt);
        setValue("mod_pgm"               , javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_curpost duplicate", "", "[" + hTempReferenceNo +"],[" +  hTempForeignFeeReferenceNo+"]");
        }
    }

    /**********************************************************************/
    void insertBilAdvfreeLog() throws Exception {
        sqlCmd = "insert into bil_advfree_log ";
        sqlCmd += "(bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "card_no,";
        sqlCmd += "source_amt,";
        sqlCmd += "destination_amt,";
        sqlCmd += "purchase_date,";
        sqlCmd += "batch_no,";
        sqlCmd += "acct_type,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "major_card_no,";
        sqlCmd += "group_code,";
        sqlCmd += "ACNO_P_SEQNO,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "use_source,";
        sqlCmd += "acct_no,";
        sqlCmd += "acct_month,";
        sqlCmd += "card_type,";
        sqlCmd += "issue_date,";
        sqlCmd += "crt_date,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " values ";
        sqlCmd += "(?,?,?,?,?,?,?,?,?," + " ?,?,?,nvl(uf_idno_pseqno(?),''),?,?,?,?,?,to_char(sysdate,'yyyymmdd'),"
                + "sysdate,?)";
        setString(1, hTempBillType);
        setString(2, hTempTransactionCode);
        setString(3, hMCurpCardNo);
        setDouble(4, hTempDestinationAmt);
        setDouble(5, hTempDestinationAmt);
        setString(6, hMCurpPurchaseDate);
        setString(7, hMCurpBatchNo);
        setString(8, hAcnoAcctType);
        setString(9, hAcnoStmtCycle);
        setString(10, hCardMajorCardNo);
        setString(11, hCardGroupCode);
        setString(12, hCardAcctPSeqno);
        setString(13, hCardId);
        setString(14, hActcUseSorce);
        setString(15, hCardComboAcctNo);
        setString(16, hActcAcctMonth);
        setString(17, hMCurpCardSw);
        setString(18, hCardIssueDate);
        setString(19, prgmId);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_insert_bil_advfree_log duplicate", "", comcr.hCallBatchSeqno);
        }
    }


    /***********************************************************************/
    void updateBilCurpost() throws Exception {
        String tmpStr1 = String.format("%16.16s", hPcomProgramName);
        String tmpStr = String.format("%s %s", tmpStr1, hPbalBankName);

        /* debug
            showLogMessage("D", "", "  update =[" + tmpStr + "]");
        */

        hMCurpMerchantChiName = String.format("%40.40s", tmpStr);

        int cnt = 1;
        daoTable   = "bil_curpost";
        updateSQL  = " fees_state     = 'P',";
        updateSQL += " cash_adv_state = 'P',";
        updateSQL += " fees_reference_no = ? ,";
        updateSQL += " reference_no_fee_f = ? ,";
        updateSQL += " mod_pgm           = 'BilA007' ";
        whereStr   = "where rowid        = ? ";

        setString(cnt++, hTempReferenceNo);
        setString(cnt++, hTempForeignFeeReferenceNo);
        setRowId(cnt++, hMCurpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update" + daoTable + "not found!", "", hTempReferenceNo);
        }

    }

    /***********************************************************************/
    void printBilCurpost(String idxCurr) throws Exception {

        subtotalCount = 0;
        subtotalAmt = 0;

        sqlCmd = "select ";
        sqlCmd += "batch_no,";
        sqlCmd += "acct_type ";
        sqlCmd += "from bil_curpost ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "and mod_pgm = 'BilA007' ";
        sqlCmd += "and decode(curr_code,'','901',curr_code) = ? ";
        sqlCmd += "and bil_curpost.mod_time = to_date(?,'yyyymmddhh24miss') ";
        sqlCmd += "group by batch_no,acct_type ";
        sqlCmd += "order by batch_no,acct_type ";
        setString(1, idxCurr);
        setString(2, hSystemDateF);

        int tmpInt0 = selectTable();
        
        for (int i = 0; i < tmpInt0; i++) {
            hMCurpBatchNo = getValue("batch_no", i);
            hMCurpAcctType = getValue("acct_type", i);

            swSubtotal = "N";
            subtotalCount = 0;
            subtotalAmt = 0;
            sumCount = 0;
            sumAmt = 0;
            lineCnt = 0;

            //第一筆印表頭
            if (i==0) {
            	printHeader(idxCurr);
            }
            
            buf = "";
            buf = comcr.insertStr(buf, "批    號:", 1);
            buf = comcr.insertStr(buf, hMCurpBatchNo, 11);
            buf = comcr.insertStr(buf, "幣    別:", 30);
            buf = comcr.insertStr(buf, idxCurr, 41);
            buf = comcr.insertStr(buf, "帳戶類別:", 50);
            buf = comcr.insertStr(buf, hMCurpAcctType, 63);
            lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
            
            printBilCurpost1(idxCurr);
            printSummary();
        }

    }

    /***********************************************************************/
    void printBilCurpost1(String idxCurr) throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "substr(b.card_type,1,1) card_type ,";
        sqlCmd += "count(*) h_m_temp_cnt,";
        sqlCmd += "max(a.acct_code) h_m_curp_acct_code,";
        sqlCmd += "sum(decode(cast(? as varchar(3)) , '901',dest_amt,dc_amount)) h_m_curp_destination_amt ";
        sqlCmd += "from crd_card b,bil_curpost a ";
        sqlCmd += "where a.mod_pgm   = 'BilA007' ";
        sqlCmd += "  and batch_no    = ? ";
        sqlCmd += "  and decode(a.curr_code,'','901',a.curr_code) = ? ";
        sqlCmd += "  and a.acct_type = ? ";
        sqlCmd += "  and b.card_no   = a.card_no ";
        sqlCmd += "  and a.mod_time  = to_date(?,'yyyymmddhh24miss') ";
        sqlCmd += "group by substr(b.card_type,1,1),a.bill_type,txn_code ";
        sqlCmd += "order by a.bill_type,decode(txn_code,'CF','1',txn_code),substr(b.card_type,1,1) ";

        setString(1, idxCurr);
        setString(2, hMCurpBatchNo);
        setString(3, idxCurr);
        setString(4, hMCurpAcctType);
        setString(5, hSystemDateF);
        tmpInt = selectTable();
        
        /* debug
            showLogMessage("D", "", "  8888888 tmp_int=" + tmpInt);
        */
        
        for (int i = 0; i < tmpInt; i++) {
            hMCurpBillType = getValue("bill_type", i);
            hMCurpTransactionCode = getValue("txn_code", i);
            hMCardCardType = getValue("card_type", i);
            hMTempCnt = getValueInt("h_m_temp_cnt", i);
            hMCurpDestinationAmt = getValueDouble("h_m_curp_destination_amt", i);
            hMCurpAcctCode = getValue("h_m_curp_acct_code", i);

            lineCnt++;
            if (lineCnt == 1) {
            	if (hMCurpTransactionCode.equals("CF") ) {
            		swSubtotal = "Y";
            	}
            }

            hTempCurrCode = idxCurr;
            hThisBusiAddAmt = hMCurpDestinationAmt;
            if ( (Arrays.asList("AF","CF","PF","AI").contains(hMCurpAcctCode)) &&
                    (!Arrays.asList("06","25","27","29").contains(hMCurpTransactionCode)) &&
                    (hThisBusiAddAmt != 0) )
                insertThisActPostLog();

            printDetail();
            subtotalCount = subtotalCount + hMTempCnt;
            subtotalAmt = subtotalAmt + hMCurpDestinationAmt;
            sumCount = sumCount + hMTempCnt;
            sumAmt = sumAmt + hMCurpDestinationAmt;
        }

    }

    /***********************************************************************/
    void insertThisActPostLog() throws Exception {

        hSrcPgmPostseq = 0;
        daoTable    = "act_post_log";
        selectSQL   = " nvl(max(SRC_PGM_POSTSEQ), 0) + 1 as h_src_pgm_postseq";
        whereStr  = " where BUSINESS_DATE    = ? ";
        whereStr += "  and CURR_CODE         = ? ";
        whereStr += "  and ACCT_CODE         = ? ";
        whereStr += "  and SRC_PGM           = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hTempCurrCode);
        setString(3, hMCurpAcctCode);
        setString(4, javaProgram);
        int m = selectTable();
        hSrcPgmPostseq = getValueLong("h_src_pgm_postseq");

        daoTable    = "act_post_log";
        extendField = "post.";
        setValue("post.BUSINESS_DATE", hBusiBusinessDate);
        setValue("post.CURR_CODE", hTempCurrCode);
        setValue("post.ACCT_CODE", hMCurpAcctCode);
        setValue("post.SRC_PGM",javaProgram);
        setValueLong("post.SRC_PGM_POSTSEQ", hSrcPgmPostseq);
        setValue("post.POST_TYPE","A1");

        hThisBusiAddAmt = convAmt(hThisBusiAddAmt);
        setValueDouble("post.POST_TYPE_AMT", hThisBusiAddAmt);
        setValue("post.POST_NOTE", hPostNote);
        setValue("post.BILL_TYPE", hMCurpBillType);
        setValue("post.TXN_CODE", hMCurpTransactionCode);
        setValue("post.ACCT_TYPE", hMCurpAcctType);
        setValue("post.MOD_TIME",sysDate + sysTime);
        setValue("post.MOD_PGM",javaProgram);
        insertTable();
        if (dupRecord.equals("Y"))
        {
            comcr.errRtn("insert_this act_post_log ERROR ", hBusiBusinessDate +" "+
                    hTempCurrCode +" "+ hMCurpAcctCode +" "+javaProgram+ hSrcPgmPostseq, comcr.hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public double  convAmt(double cvtAmt) throws Exception
    {
        long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
        double cvtDouble =  ((double) cvtLong) / 100;
        return cvtDouble;
    }

	/***********************************************************************/
	void printDetail() throws Exception {
		indexCnt++;
		if (!hMCurpTransactionCode.equals("CF") && swSubtotal.equals("Y")) {
			printSubtotal();
			swSubtotal = "N";
		}

		hBityExterDesc = "";
		sqlCmd = "select exter_desc ";
		sqlCmd += "from ptr_billtype  ";
		sqlCmd += "where bill_type = ?  ";
		sqlCmd += "  and txn_code  = ? ";
		setString(1, hMCurpBillType);
		setString(2, hMCurpTransactionCode);
		int tmpInt1 = selectTable();
		if (tmpInt1 > 0) {
			hBityExterDesc = getValue("exter_desc");
		}

		tempX20 = hBityExterDesc;

		buf = "";
		buf = comcr.insertStr(buf, hMCardCardType, 1);
		buf = comcr.insertStr(buf, tempX20, 4);
		szTmp = comcr.commFormat("3#,3#", hMTempCnt);
		buf = comcr.insertStr(buf, szTmp, 26);
		szTmp = comcr.commFormat("3$,3$,3$.2$", hMCurpDestinationAmt);
		buf = comcr.insertStr(buf, szTmp, 36);

		/* debug 
		   	showLogMessage("D", "", "  8888888 11 buf =" + buf);
		 */

		lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "2", buf));
	}

    /***********************************************************************/
    void printSubtotal() {
        buf = "";
        buf = comcr.insertStr(buf, "小計 :", 4);
        szTmp = comcr.commFormat("3#,3#", subtotalCount);
        buf = comcr.insertStr(buf, szTmp, 26);
        szTmp = comcr.commFormat("3$,3$,3$.2$", subtotalAmt);
        buf = comcr.insertStr(buf, szTmp, 36);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "2", buf));
        subtotalCount = 0;
        subtotalAmt = 0;
    }

    /***********************************************************************/
    void printSummary() {

        printSubtotal();

        buf = "";
        buf = comcr.insertStr(buf, "總額 :", 4);
        szTmp = comcr.commFormat("3#,3#", sumCount);
        buf = comcr.insertStr(buf, szTmp, 26);
        szTmp = comcr.commFormat("3$,3$,3$.2$", sumAmt);
        buf = comcr.insertStr(buf, szTmp, 36);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", buf));
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA007 proc = new BilA007();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
