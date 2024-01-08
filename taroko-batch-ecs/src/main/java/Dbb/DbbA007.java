/*****************************************************************************
*                                                                                                                                             *
*                              MODIFICATION LOG                                                                                  *
*                                                                                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION                                                     *
*  ---------  --------- ----------- --------------------------------------------------------------------------- *
*  109/07/15  V1.00.00    JeffKung      program initial                            *
*  109/09/16  V1.00.01    JeffKung      授權圈存金額大於0,則不另外算手續費                   *
*  109/09/19  V1.00.02    JeffKung      debitcard process '901' only               *
*  109/09/25  V1.00.03    JeffKung      filled miss fields                         *
*  109/11/17  V1.00.04    JeffKung      auth_nt_amt > 0  也要update curpost()      *   
*  109/12/24  V1.00.05    yanghan       修改了變量名稱和方法名稱                                    *                                     
*  111/03/11  V1.00.06    JeffKung      不計算國外交易手續費                                          * 
*  111/03/28  V1.00.07    JeffKung      請款一律入帳,問交記錄僅為參考                          *
*  111/09/22  V1.00.08    JeffKung      中文特店名稱長度限制40長                               *
*  111/11/18  V1.00.09    JeffKung      區分國外易手續費、E-GOV手續費
*                                       及VDFC:選牌費、繳交交通罰款、中華電信資費、換發行照、核定稅款等手續費
*****************************************************************************/
package Dbb;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommTxBill.ForeignFeeData;
import com.CommTxBill;

public class DbbA007 extends AccessDAO {
	private final String progname = "Debit 手續費轉換作業  111/11/18 V1.00.09";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommTxBill commTxBill = null;

    int debug = 0;
    String hCallErrorDesc = "";
    String hTempUser = "";

    int tmpInt = 0;
    int totCnt = 0;

    String prgmId = "DbbA007";
    String rptId   = "";
    String rptName = "DEBIT卡各項手續費統計總表";
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
    String hMCurpFilmNo = "";
    String hMCurpAcqMemberId = "";
    String hMCurpMchtCity = "";
    String hMCurpMchtCategory = "";
    String hMCurpMchtZip = "";
    String hMCurpMchtState = "";
    String hMCurpAuthCode = "";
    String hMCurpPosEntryMode = "";
    String hMCurpMchtChiName = "";
    String hMCurpMchtEngName = "";
    String hMCurpBatchNo = "";
    double hMCurpDestinationAmt = 0;
    String hMCurpSourceCurrency = "";
    String hMCurpDestinationCurrency = "";
    String hMCurpCardSw = "";
    String hMCurpCashAdvState = "";
    String hMCurpCurrCode = "";
    double hMCurpDcDestAmt = 0;
    double hMCurpDcExchangeRate = 0;
    String hMCurpSettleFlag = "";
    String hMCurpEcsCusMchtNo = "";
    String hMCurpTxSeq = "";
    double hMCurpAuthNtAmt = 0;
    double hMCurpVdLockNtAmt = 0;
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
    String hBityExterDesc = "";
    String hCurpBillType = "";
    String hCurpTransactionCode = "";
    String hMCurpMerchantChiName = "";
    String hCurpMerchantChiName = "";
    String hTempReferenceNo = "";
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
    String hCardMajorCardNo    = "";
    String hCardMajorIdPSeqno = "";
    String hCardCurrentCode = "";
    String hCardIssueDate = "";
    String hCardOppostDate = "";
    String hCardPromoteDept = "";
    String hCardProdNo = "";
    String hCardGroupCode = "";
    String hCardSourceCode = "";
    String hCardAcctPSeqno = "";
    String hCardIdPSeqno = "";
    String hCardId = "";
    String hCurpCardNo = "";
    String hPbtbCurrCode = "";
    String hPrintName = "";
    String hRptName = "";
    int hTempCnt = 0;
    String hAcnoAcctType = "";
    String hPSeqno = "";
    String hCurpPurchaseDate = "";
    double hCurpDestinationAmt = 0;
    String hActcUseSorce = "";
    String hActcAcctMonth = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    long   hTempDestinationAmt = 0;
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
    long   hSrcPgmPostseq = 0;
    String hPostNote = "手續費 by batch_no,bill_type,txn_code";

    String[] hMPbtbDcCurrCode = new String[250];
    int ptrBintableCnt = 0;
    int totalCount = 0;
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
            commTxBill = new CommTxBill(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                if (selectTable() > 0)
                    hTempUser = getValue("user_id");
            }

            selectPtrBusinday();
            showLogMessage("I", "", "Process_date = " + hBusiBusinessDate);

            selectPtrBintable0();

            for (int int0 = 0; int0 < ptrBintableCnt; int0++) {
                String dcCurrCode = hMPbtbDcCurrCode[int0];
                
                //showLogMessage("D", "", "  Process curr = " + dcCurrCode);

                totalCount = 0;
                rptId = String.format("%s_%s", "DBB_A007R1", dcCurrCode);
                String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptId, hSystemDateF);

                selectDbbCurpost(dcCurrCode);
                //printDbbCurpost(dcCurrCode);

                showLogMessage("I", "", String.format("** 幣別:[%s] 筆數=[%d]", hMPbtbDcCurrCode[int0], totalCount));

                /*這支程式改成不出報表
                //如果沒有資料也要寫
                if (totalCount == 0) {
                    hMCurpBatchNo = "";
                    printHeader(dcCurrCode);
                    lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "2", "***本日無資料***"));
                }
                */
                
                //comcr.insertPtrBatchRpt(lpar1);
                //comc.writeReportForTest(filename, lpar1);
                //lpar1.clear();
            }

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCount + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

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
        sqlCmd += "to_char(sysdate,'yyyymmdd') hSystemDate,";
        sqlCmd += "to_char(sysdate,'yyyy/mm/dd hh24:mi:ss') hSystemTime,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') hSystemDateF ";
        sqlCmd += "from ptr_businday ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hSystemDate = getValue("hSystemDate");
            hSystemTime = getValue("hSystemTime");
            hSystemDateF = getValue("hSystemDateF");
        }

        hModUser = comc.commGetUserID();
        hMCurpModUser = hModUser;
    }

    /***********************************************************************/
    void selectPtrBintable0() throws Exception {

        sqlCmd  = "select decode(dc_curr_code,'','901',dc_curr_code) hMPbtbDcCurrCode ";
        sqlCmd += "  from ptr_bintable ";
        sqlCmd += "  where dc_curr_code in ('901','') ";  //debitCard process '901' only 
        sqlCmd += " group by decode(dc_curr_code,'','901',dc_curr_code) ";
        recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMPbtbDcCurrCode[i] = getValue("hMPbtbDcCurrCode", i);
        }

        ptrBintableCnt = recordCnt;

    }

    /**********************************************************************/
    void selectDbbCurpost(String idxCurr) throws Exception {

        sqlCmd = "select ";
        sqlCmd += "card_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "a.txn_code,";
        sqlCmd += "purchase_date,";
        sqlCmd += "reference_no,";
        sqlCmd += "a.mcht_no,";
        sqlCmd += "a.mcht_country,";
        sqlCmd += "a.film_no,";         //20200925 add
        sqlCmd += "a.acq_member_id,";    //20200925 add
        sqlCmd += "a.mcht_city,";         //20200925 add
        sqlCmd += "a.mcht_category,";  //20200925 add
        sqlCmd += "a.mcht_zip,";  //20200925 add
        sqlCmd += "a.mcht_state,";  //20200925 add
        sqlCmd += "a.auth_code,";  //20200925 add
        sqlCmd += "a.pos_entry_mode,";  //20200925 add
        sqlCmd += "a.mcht_chi_name,";
        sqlCmd += "a.mcht_eng_name,";
        sqlCmd += "a.batch_no,";
        sqlCmd += "dest_amt                                    as hMCurpDestinationAmt,";
        sqlCmd += "decode(source_curr,'TWD','901',source_curr) as hMCurpSourceCurrency,";
        sqlCmd += "decode(dest_curr,'TWD','901',dest_curr)     as hMCurpDestinationCurrency,";
        sqlCmd += "upper(cash_adv_state)                       as hMCurpCashAdvState,";
        sqlCmd += "a.bin_type ,";
        sqlCmd += "a.settl_flag ,";
        sqlCmd += "a.ecs_platform_kind,";
        sqlCmd += "a.ecs_cus_mcht_no,";
        sqlCmd += "a.auth_nt_amt, ";
        sqlCmd += "a.vd_lock_nt_amt, ";
        sqlCmd += "a.tx_seq, ";
        sqlCmd += "a.dc_dest_amt,";
        sqlCmd += "a.dc_exchange_rate,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from dbb_curpost a, bil_postcntl b ";
        sqlCmd += "where 1=1  ";
        sqlCmd += " and (decode(fees_state        ,'','N',fees_state)         in ('Y','y') or ";        //其他手續費收取否
        sqlCmd += "       decode(cash_adv_state    ,'','N',cash_adv_state)     in ('Y','y') or ";  //預借現金收取否
        sqlCmd += "       (fees_state <> 'P' and a.auth_nt_amt <> a.vd_lock_nt_amt) ) ";                                       //圈存金額與消費本金不同時
        sqlCmd += " and  decode(entry_acct        ,'','N',entry_acct)         in ('Y','y') ";              //是否轉入當期帳單檔
        sqlCmd += " and (decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n') and ";  //是否做格式查核
        sqlCmd += "       decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('N','n') and ";  //是否做重覆查核
        sqlCmd += "       decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag)    in ('N','n') and ";  //是否做疑異帳單查核
        //V1.00.07
        sqlCmd += "       decode(rsk_type          ,'','N',rsk_type )          <>  '1'  )  ";               //帳單之風管疑異碼 , 排除卡號不存在系統的交易
        //sqlCmd += "       decode(rsk_type          ,'','N',rsk_type )          in ('N','n'))  ";               //帳單之風管疑異碼 
        sqlCmd += " and (curr_post_flag  in ('N','n') or curr_post_flag ='') ";
        sqlCmd += " and decode(confirm_flag_p       ,'','N',confirm_flag_p)  in ('Y','y') ";         //帳單批次: 確認旗標
        sqlCmd += " and decode(manual_upd_flag      ,'','N',manual_upd_flag) != ('Y') ";       //人工更改旗標
        sqlCmd += " and decode(valid_flag           ,'','N',valid_flag)      != ('Y') ";                   //Y:需取授權 W:送至授權 E:授權有誤 P:授權無誤  
        sqlCmd += " and decode(a.curr_code          ,'','901',a.curr_code)    = ? ";
        sqlCmd += " and batch_date  = substr(a.batch_no,1,8) ";
        sqlCmd += " and batch_unit  = substr(a.batch_no,9,2) ";
        sqlCmd += " and batch_seq   = substr(a.batch_no,11,4) ";
        setString(1, idxCurr);
        
		//showLogMessage("D", "", "  888 CURR    = " + idxCurr);
		
        openCursor();
        while (fetchTable()) {
            hMCurpCardNo       = getValue("card_no");
            hMCurpBillType     = getValue("bill_type");
            hMCurpTxnCode     = getValue("txn_code");
            hMCurpPurchaseDate = getValue("purchase_date");
            hMCurpReferenceNo  = getValue("reference_no");
            hMCurpFilmNo            = getValue("film_no");
            hMCurpAcqMemberId   = getValue("acq_member_id");
            hMCurpMerchantNo   = getValue("mcht_no");
            hMCurpMchtCountry   = getValue("mcht_country");
            hMCurpMchtCity         = getValue("mcht_city");
            hMCurpMchtCategory  = getValue("mcht_category");
            hMCurpMchtZip          = getValue("mcht_zip");
            hMCurpMchtState       = getValue("mcht_state");
            hMCurpAuthCode        = getValue("auth_code");
            hMCurpPosEntryMode   = getValue("pos_entry_mode");
            hMCurpMchtChiName  = getValue("mcht_chi_name");
            hMCurpMchtEngName  = getValue("mcht_eng_name");
            hMCurpBatchNo      = getValue("batch_no");
            hMCurpDestinationAmt      = getValueDouble("hMCurpDestinationAmt");
            hMCurpSourceCurrency      = getValue("hMCurpSourceCurrency");
            hMCurpDestinationCurrency = getValue("hMCurpDestinationCurrency");
            hMCurpCardSw              = getValue("bin_type");
            hMCurpCashAdvState       = getValue("hMCurpCashAdvState");
            hMCurpCurrCode            = getValue("curr_code");
            hMCurpBinType             = getValue("bin_type");
            hMCurpDcDestAmt            = getValueDouble("dc_dest_amt");
            hMCurpDcExchangeRate     = getValueDouble("dc_exchange_rate");
            hMCurpSettleFlag          = getValue("settl_flag");
            hMCurpAuthNtAmt        = getValueDouble("auth_nt_amt");
            hMCurpVdLockNtAmt     = getValueDouble("vd_lock_nt_amt");
            hMCurpTxSeq                = getValue("tx_seq");
            hMCurpEcsCusMchtNo    = getValue("ecs_cus_mcht_no");
            hMCurpRowid                = getValue("rowid");

            totalCount++;
            //if (totalCount == 1)
            //    printHeader(idxCurr);

            //Debug
            //showLogMessage("D", "", "  888 Begin ");
            //showLogMessage("D", "", "      card="+hMCurpCardNo+","+hMCurpReferenceNo+",type="+hMCurpBillType+"["+hMCurpDestinationCurrency+"]"+hMCurpSourceCurrency );

            selectDbcCard(); /* 先抓card_type */
            selectPtrBintable();
            selectDbaAcno();

            hTempReferenceNo = "";
            hTempDestinationAmt = 0;

          //授權時有多圈則以授權時的圈存差額為交易手續費
			if (hMCurpAuthNtAmt < hMCurpVdLockNtAmt) {
				hTempDestinationAmt = (long) (hMCurpVdLockNtAmt - hMCurpAuthNtAmt);
				//區分VD交易手續費(OSSG-VD)選牌費、繳交交通罰款、中華電信資費、換發行照、核定稅款等手續費
				//   E政府繳費平台手續費(OSSG-VM)E-GOV手續費 
				//   國外交易手續費(FIFC-05)
				if ("TW".equalsIgnoreCase(comc.getSubString(hMCurpMchtCountry, 0, 2))) {
					if ("5999".equals(hMCurpMchtCategory)) {
						hTempBillType = "OSSG";
						hTempTransactionCode = "VD";
					} else {
						hTempBillType = "OSSG";
						hTempTransactionCode = "VM";
					}
				} else {
					hTempBillType = "FIFC";
					hTempTransactionCode = "05";
				}
				
		        selectPtrBilltype();
	            selectPtrActcode();
	            insertDbbCurpost();
	            updateDbbCurpost();
	            continue;
			}
			
	        //有比對到授權時(amt>0) 則以授權所圈存金額為主,不另外算手續費
			if (hMCurpAuthNtAmt > 0) {
				hTempReferenceNo="";
				updateDbbCurpost();
	            continue;
				}		
			
			
			
            ForeignFeeData foreignFeeData = null;
            
         /* 不能計算, 以圈存金額為主 20220311
         
         // 若清算識別碼=0，則為國外清算
			if( hMCurpSettleFlag.equals("0")) {
				// 計算國外手續費
				foreignFeeData = computeForeignFee(idxCurr);
				hTempDestinationAmt = (long) (foreignFeeData.getForeignFee());
				hTempBillType = "FIFC";
		        hTempTransactionCode = "05";

		        if (hTempDestinationAmt > 0) {
		        	selectPtrBilltype();
		            selectPtrActcode();
		            insertDbbCurpostForeignFee();
		            updateDbbCurpostForeignFee();
		            continue;
		        }
			}
			*/
            
            /*VD手續費都在授權時圈存,不另外計算
            if(comc.getSubString(hMCurpCashAdvState, 0, 1).equals("Y")) {
            	//showLogMessage("D", "", "  888 1 ptr_prepaidfee="+hMCurpCardNo);
                selectPtrPrepaidfee();
            } else {
            	
               if (hMCurpEcsCusMchtNo.length() > 0) {
                   // 新增若特店未設手續費參數跳過該筆不予處理 ...
            	   //showLogMessage("D", "", "  888 2 ptr_prepaidfee_m= ["+hMCurpCardNo + "] ,ecsCusMchtNo = [" + hMCurpEcsCusMchtNo + "]") ;
            	   selectPtrPrepaidfeeM(); 
                }
            }
    		*/
            
            //有需要收費的才寫
            if (hTempDestinationAmt > 0) {
                selectPtrBilltype();
                selectPtrActcode();
            	insertDbbCurpost();
            } else {
            	hTempReferenceNo = ""; 
            }
            
            updateDbbCurpost();
        }
        closeCursor();
    }

    /***********************************************************************/
    void printHeader(String idxCurr) {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: DBB_A007R1", 1);
        buf = comcr.insertStrCenter(buf, "Debit卡  各項手續費統計總表", 80);
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

        buf = "";
        buf = comcr.insertStr(buf, "批    號:", 1);
        buf = comcr.insertStr(buf, hMCurpBatchNo, 11);
        buf = comcr.insertStr(buf, "幣    別:", 62);
        buf = comcr.insertStr(buf, idxCurr, 71);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

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
    void selectDbcCard() throws Exception {
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
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "b.id_no as id ";
        sqlCmd += " from dbc_card a, dbc_idno b  ";
        sqlCmd += "where a.card_no    = ?  ";
        sqlCmd += "  and a.id_p_seqno = b.id_p_seqno ";
        setString(1, hMCurpCardNo);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
            hCardMajorCardNo    = getValue("major_card_no");
            hCardCurrentCode     = getValue("current_code");
            hCardIssueDate       = getValue("issue_date");
            hCardOppostDate      = getValue("oppost_date");
            hCardPromoteDept     = getValue("promote_dept");
            hCardProdNo          = getValue("prod_no");
            hCardGroupCode       = getValue("group_code");
            hCardSourceCode   = getValue("source_code");
            hCardCardType     = getValue("card_type");
            hCardBinNo        = getValue("bin_no");
            hCardBinType      = getValue("bin_type");
            hCardAcctPSeqno  = getValue("p_seqno");
            hCardIdPSeqno     = getValue("id_p_seqno");
            hCardId = getValue("id");
        }

    }

    /***********************************************************************/
    void selectDbaAcno() throws Exception {

        sqlCmd = "select acct_type,";
        sqlCmd += "p_seqno ,";
        sqlCmd += "acct_status,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "pay_by_stage_flag,";
        sqlCmd += "autopay_acct_no ";
        sqlCmd += " from dba_acno  ";
        sqlCmd += "where p_seqno  = ? ";
        setString(1, hCardAcctPSeqno);
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hAcnoAcctType = getValue("acct_type");
            hPSeqno = getValue("p_seqno");
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
		}else {
			foreignFeeData = commTxBill.getForeignFeeData(hMCurpBinType, hMCurpMchtCountry,
					hMCurpDestinationCurrency, hMCurpSourceCurrency , "Y", dcCurr, hMCurpDestinationAmt,
					hMCurpDcDestAmt );
		}
		
		return foreignFeeData;
		
	}

/***********************************************************************/
void selectPtrPrepaidfee() throws Exception 
{
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
   sqlCmd += "dom_fix_amt  as hPpfeDomFixAmt,";
   sqlCmd += "dom_percent  as hPpfeDomPercent,";
   sqlCmd += "dom_min_amt  as hPpfeDomMinAmt,";
   sqlCmd += "dom_max_amt  as hPpfeDomMaxAmt,";
   sqlCmd += "int_fix_amt  as hPpfeIntFixAmt,";
   sqlCmd += "int_percent  as hPpfeIntPercent,";
   sqlCmd += "int_min_amt  as hPpfeIntMinAmt,";
   sqlCmd += "int_max_amt  as hPpfeIntMaxAmt,";
   sqlCmd += "swap_fix_amt as hPpfeSwapFixAmt,";
   sqlCmd += "swap_percent as hPpfeSwapPercent ";
   sqlCmd += " from ptr_prepaidfee  ";
   sqlCmd += "where card_type = ?  ";
   sqlCmd += "  and decode(curr_code,'','901',curr_code) = ";
   sqlCmd += "      decode(cast(? as varchar(10)) ,'','901',?) ";
   setString(1, hCardCardType);
   setString(2, hMCurpCurrCode);
   setString(3, hMCurpCurrCode);
   tmpInt = selectTable();
   if (notFound.equals("Y")) {
	   showLogMessage("E", "", "  預借現金手續費參數PTR_PREPAIDFEE未建檔!! ");
	   return;
       
   }
   if (tmpInt > 0) {
       hPpfeFeesBillType = getValue("fees_bill_type");
       hPpfeFeesTxnCode  = getValue("fees_txn_code");
       hPpfeDomFixAmt  = getValueDouble("hPpfeDomFixAmt");
       hPpfeDomPercent  = getValueDouble("hPpfeDomPercent");
       hPpfeDomMinAmt  = getValueDouble("hPpfeDomMinAmt");
       hPpfeDomMaxAmt  = getValueDouble("hPpfeDomMaxAmt");
       hPpfeIntFixAmt  = getValueDouble("hPpfeIntFixAmt");
       hPpfeIntPercent  = getValueDouble("hPpfeIntPercent");
       hPpfeIntMinAmt  = getValueDouble("hPpfeIntMinAmt");
       hPpfeIntMaxAmt  = getValueDouble("hPpfeIntMaxAmt");
       hPpfeSwapFixAmt = getValueDouble("hPpfeSwapFixAmt");
       hPpfeSwapPercent = getValueDouble("hPpfeSwapPercent");
   }

   hTempBillType        = hPpfeFeesBillType;
   hTempTransactionCode = hPpfeFeesTxnCode;

   if(hMCurpDcDestAmt > 0 && hMCurpCurrCode.equals("901") == false) 
     {
      // 外幣
      tempDouble = (hPpfeIntPercent * hMCurpDcDestAmt) / 100;
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
    	   // 線上結匯
    	  /*
           if(hCurpMerchantNo.equals("730460000000023") || hCurpMerchantNo.equals("730460000000032") )
           {
           
               hTempDestinationAmt = (long)(hPpfeSwapFixAmt + hPpfeSwapPercent * hCurpDestinationAmt/100);
           }
           else
           {
         */

            // 一般預現
            hTempDestinationAmt = (long)(hPpfeDomFixAmt + hPpfeDomPercent * hMCurpDestinationAmt/100);
            if(hTempDestinationAmt > hPpfeDomMaxAmt)
               hTempDestinationAmt = (long) hPpfeDomMaxAmt;
            if(hTempDestinationAmt < hPpfeDomMinAmt)
               hTempDestinationAmt = (long) hPpfeDomMinAmt;
        } 
      else
        {
         // 國外預現
         hTempDestinationAmt = (long)(hPpfeIntFixAmt + hPpfeIntPercent * hMCurpDestinationAmt/100);
         if(hTempDestinationAmt > hPpfeIntMaxAmt)
            hTempDestinationAmt = (long) hPpfeIntMaxAmt;
         if(hTempDestinationAmt < hPpfeIntMinAmt)
            hTempDestinationAmt = (long) hPpfeIntMinAmt;
        }
     }
}
/***********************************************************************/
boolean selectPtrPrepaidfeeM() throws Exception {

        sqlCmd = "select fees_bill_type,";
        sqlCmd += "fees_txn_code,";
        sqlCmd += "dom_fix_amt hPremDomFixAmt,";
        sqlCmd += "dom_percent hPremDomPercent,";
        sqlCmd += "dom_min_amt hPremDomMinAmt,";
        sqlCmd += "dom_max_amt hPremDomMaxAmt,";
        sqlCmd += "int_fix_amt hPremIntFixAmt,";
        sqlCmd += "int_percent hPremIntPercent,";
        sqlCmd += "int_min_amt hPremIntMinAmt,";
        sqlCmd += "int_max_amt hPremIntMaxAmt,";
        sqlCmd += "nor_amt hPremNorAmt,";
        sqlCmd += "nor_fix_amt hPremNorFixAmt,";
        sqlCmd += "nor_percent hPremNorPercent,";
        sqlCmd += "spe_fix_amt hPremSpeFixAmt,";
        sqlCmd += "spe_percent hPremSpePercent,";
        sqlCmd += "spe_amt hPremSpeAmt,";
        sqlCmd += "tx_date_f,";
        sqlCmd += "tx_date_e ";
        sqlCmd += " from ptr_prepaidfee_m  ";
        sqlCmd += "where merchant_no = ? ";
        setString(1, hMCurpEcsCusMchtNo);
        tmpInt = selectTable();
        //showLogMessage("D", "", "  888 prepaidfee_m 1="+hMCurpEcsCusMchtNo+","+tmpInt);
        /* 20040115新增未設特店手續費時該筆跳過不處理，但仍然有錯誤訊息 ...Wincard */
        if (notFound.equals("Y")) {
            showLogMessage("I", "", "selectPtrPrepaidfeeM not Found, mchtNo=["  +  hMCurpEcsCusMchtNo + "]" );
            return false;
        }
        if (tmpInt > 0) {
            hPremFeesBillType = getValue("fees_bill_type");
            hPremFeesTxnCode = getValue("fees_txn_code");
            hPremDomFixAmt = getValueDouble("hPremDomFixAmt");
            hPremDomPercent = getValueDouble("hPremDomPercent");
            hPremDomMinAmt = getValueDouble("hPremDomMinAmt");
            hPremDomMaxAmt = getValueDouble("hPremDomMaxAmt");
            hPremIntFixAmt = getValueDouble("hPremIntFixAmt");
            hPremIntPercent = getValueDouble("hPremIntPercent");
            hPremIntMinAmt = getValueDouble("hPremIntMinAmt");
            hPremIntMaxAmt = getValueDouble("hPremIntMaxAmt");
            hPremNorAmt = getValueDouble("hPremNorAmt");
            hPremNorFixAmt = getValueDouble("hPremNorFixAmt");
            hPremNorPercent = getValueDouble("hPremNorPercent");
            hPremSpeFixAmt = getValueDouble("hPremSpeFixAmt");
            hPremSpePercent = getValueDouble("hPremSpePercent");
            hPremSpeAmt = getValueDouble("hPremSpeAmt");
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
    void insertDbbCurpost() throws Exception {
        hTempX08 = "";
        sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) hTempX08 ";
        sqlCmd += "from dual ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hTempX08 = getValue("hTempX08");
        }
        hTempReferenceNo = String.format("%2.2s%s", comc.getSubString(hBusiBusinessDate, 2), hTempX08);


        if (hMCurpCurrCode.equals("901") || hMCurpCurrCode.length() == 0) {
            hMCurpCurrCode = "901";
            hTempDcAmount = hTempDestinationAmt;
        }


        //showLogMessage("D", "", "  insert ref=["+hTempReferenceNo + "]"+hTempDestinationAmt+",["+hMCurpDestinationCurrency+"]");

        daoTable = "dbb_curpost ";
        setValue("reference_no"    , hTempReferenceNo);
        setValue("bill_type"       , hTempBillType);
        setValue("txn_code"        , hTempTransactionCode);
        setValue("card_no"         , hMCurpCardNo);
        setValue("purchase_date"   , hMCurpPurchaseDate);
        setValueDouble("dest_amt"  , hTempDestinationAmt);
        setValue("dest_curr"       , hMCurpDestinationCurrency);
        setValueDouble("source_amt", hTempDestinationAmt);
        setValue("source_curr"     , hMCurpDestinationCurrency);
        setValue("process_date"    , hMCurpProcessDate);
        setValue("mcht_no"         , hMCurpMerchantNo);
        //特店中文若為空, 抓英文名稱
        if (hMCurpMchtChiName.length() == 0 ) {  
        	hMCurpMchtChiName = hMCurpMchtEngName;
        }
        setValue("mcht_chi_name"   , String.format("%15.15s-%24.24s",hBityExterDesc,hMCurpMchtChiName));
        setValue("mcht_eng_name"   ,hMCurpMchtEngName);
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
        setValueDouble("dc_dest_amt"       , hTempDcAmount);
        setValueDouble("dc_exchange_rate", hMCurpDcExchangeRate);
        setValue("tx_seq"                           , hMCurpTxSeq);
        setValue("this_close_date"              , hBusiBusinessDate);
        setValue("mod_pgm"               , javaProgram);
        setValueDouble("auth_nt_amt"        , 0);
        setValueDouble("vd_lock_nt_amt"    , 0);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insertDbbCurpost duplicate", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateDbbCurpost() throws Exception {

        int cnt = 1;
        daoTable   = "dbb_curpost";
        updateSQL  = " fees_state     = 'P',";
        updateSQL += " cash_adv_state = 'P',";
        updateSQL += " fees_reference_no = ? ,";
        updateSQL += " mod_pgm           = 'DbbA007' ";
        whereStr   = "where rowid        = ? ";

        setString(cnt++, hTempReferenceNo);
        setRowId(cnt++, hMCurpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update" + daoTable + "not found!", "","ori_reference_no = [" + hMCurpReferenceNo + "]");
        }

    }
    
    /***********************************************************************/
    void insertDbbCurpostForeignFee() throws Exception {
        hTempX08 = "";
        sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) hTempX08 ";
        sqlCmd += "from dual ";
        tmpInt = selectTable();
        if (tmpInt > 0) {
            hTempX08 = getValue("hTempX08");
        }
        hTempReferenceNo = String.format("%2.2s%s", comc.getSubString(hBusiBusinessDate, 2), hTempX08);


        if (hMCurpCurrCode.equals("901") || hMCurpCurrCode.length() == 0) {
            hMCurpCurrCode = "901";
            hTempDcAmount = hTempDestinationAmt;
        }


        //showLogMessage("D", "", "  insert ref=["+hTempReferenceNo + "]"+hTempDestinationAmt+",["+hMCurpDestinationCurrency+"]");

        daoTable = "dbb_curpost ";
        setValue("reference_no"    , hTempReferenceNo);
        setValue("bill_type"       , hTempBillType);
        setValue("txn_code"        , hTempTransactionCode);
        setValue("card_no"         , hMCurpCardNo);
        setValue("purchase_date"   , hMCurpPurchaseDate);
        setValueDouble("dest_amt"  , hTempDestinationAmt);
        setValue("dest_curr"       , hMCurpDestinationCurrency);
        setValueDouble("source_amt", hTempDestinationAmt);
        setValue("source_curr"     , hMCurpDestinationCurrency);
        setValue("process_date"    , hMCurpProcessDate);
        setValue("mcht_no"         , hMCurpMerchantNo);
        //特店中文若為空, 抓英文名稱
        if (hMCurpMchtChiName.length() == 0 ) {  
        	hMCurpMchtChiName = hMCurpMchtEngName;
        }
        setValue("mcht_chi_name"   , String.format("%15.15s - %24.24s",hBityExterDesc,hMCurpMchtChiName));
        setValue("mcht_eng_name"   ,hMCurpMchtEngName);
        setValue("film_no"                ,hMCurpFilmNo);
        setValue("acq_member_id"    ,hMCurpAcqMemberId);
        setValue("mcht_city"             ,hMCurpMchtCity);
        setValue("mcht_category"      ,hMCurpMchtCategory);
        setValue("mcht_zip"              ,hMCurpMchtZip);
        setValue("mcht_state"           ,hMCurpMchtState);
        setValue("auth_code"            ,hMCurpAuthCode);
        setValue("pos_entry_mode"   ,hMCurpPosEntryMode);
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
        setValueDouble("dc_dest_amt"       , hTempDcAmount);
        setValueDouble("dc_exchange_rate", hMCurpDcExchangeRate);
        setValue("tx_seq"                           , hMCurpTxSeq);
        setValue("this_close_date"              , hBusiBusinessDate);
        setValue("mod_pgm"               , javaProgram);
        setValueDouble("auth_nt_amt"        , 0);
        setValueDouble("vd_lock_nt_amt"    , 0);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insertDbbCurpost duplicate", "", "reference_no = [" + hTempReferenceNo + "]");
        }
    }

    /***********************************************************************/
    void updateDbbCurpostForeignFee() throws Exception {
        
    	int cnt = 1;
        daoTable   = "dbb_curpost";
        updateSQL  = " fees_state     = 'P',";
        updateSQL += " reference_no_fee_f = ? ,";
        updateSQL += " mod_pgm           = 'DbbA007' ";
        whereStr   = "where rowid        = ? ";

        setString(cnt++, hTempReferenceNo);
        setRowId(cnt++, hMCurpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update" + daoTable + "not found!", "", "ori_reference_no = [" + hMCurpReferenceNo + "]");
        }

    }

    /***********************************************************************/
    void printDbbCurpost(String idxCurr) throws Exception {

        subtotalCount = 0;
        subtotalAmt = 0;

        sqlCmd = "select ";
        sqlCmd += "batch_no,";
        sqlCmd += "acct_type ";
        sqlCmd += "from dbb_curpost ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "and mod_pgm = 'DbbA007' ";
        sqlCmd += "and decode(curr_code,'','901',curr_code) = ? ";
        sqlCmd += "and dbb_curpost.mod_time = to_date(?,'yyyymmddhh24miss') ";
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
            printDbbCurpost1(idxCurr);
            printSummary();
        }

    }

    /***********************************************************************/
    void printDbbCurpost1(String idxCurr) throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "b.card_type,";
        sqlCmd += "count(*) hMTempCnt,";
        sqlCmd += "max(a.acct_code) hMCurpAcctCode,";
        sqlCmd += "sum(decode(cast(? as varchar(3)) , '901',dest_amt,dc_dest_amt)) hMCurpDestinationAmt ";
        sqlCmd += "from dbc_card b,dbb_curpost a ";
        sqlCmd += "where a.mod_pgm   = 'DbbA007' ";
        sqlCmd += "  and batch_no    = ? ";
        sqlCmd += "  and decode(a.curr_code,'','901',a.curr_code) = ? ";
        sqlCmd += "  and a.acct_type = ? ";
        sqlCmd += "  and b.card_no   = a.card_no ";
        sqlCmd += "  and a.mod_time  = to_date(?,'yyyymmddhh24miss') ";
        sqlCmd += "group by b.card_type,a.bill_type,txn_code ";
        sqlCmd += "order by a.bill_type,decode(txn_code,'CF','1',txn_code),b.card_type ";

        setString(1, idxCurr);
        setString(2, hMCurpBatchNo);
        setString(3, idxCurr);
        setString(4, hMCurpAcctType);
        setString(5, hSystemDateF);
        tmpInt = selectTable();
        
        //showLogMessage("D", "", "  8888888 tmpInt=" + tmpInt);
        
        for (int i = 0; i < tmpInt; i++) {
            hMCurpBillType = getValue("bill_type", i);
            hMCurpTransactionCode = getValue("txn_code", i);
            hMCardCardType = getValue("card_type", i);
            hMTempCnt = getValueInt("hMTempCnt", i);
            hMCurpDestinationAmt = getValueDouble("hMCurpDestinationAmt", i);
            hMCurpAcctCode = getValue("hMCurpAcctCode", i);

            lineCnt++;
            if (lineCnt == 1 && hMCurpTransactionCode.equals("CF")) {
                swSubtotal = "Y";
            }
            
            hTempCurrCode = idxCurr;
            hThisBusiAddAmt = hMCurpDestinationAmt;
            if ( (Arrays.asList("AF","CF","PF","AI").contains(hMCurpAcctCode)) && 
                 (hThisBusiAddAmt  != 0) ) 
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
        selectSQL   = " nvl(max(SRC_PGM_POSTSEQ), 0) + 1 as hSrcPgmPostseq";
        whereStr  = " where BUSINESS_DATE    = ? ";
        whereStr += "  and CURR_CODE         = ? ";      
        whereStr += "  and ACCT_CODE         = ? ";      
        whereStr += "  and SRC_PGM           = ? ";      
        setString(1, hBusiBusinessDate);
        setString(2, hTempCurrCode);
        setString(3, hMCurpAcctCode);
        setString(4, javaProgram);
        int m = selectTable();
        hSrcPgmPostseq = getValueLong("hSrcPgmPostseq");

        daoTable    = "act_post_log";
        extendField = "post.";
        setValue("post.BUSINESS_DATE",hBusiBusinessDate);
        setValue("post.CURR_CODE",hTempCurrCode);
        setValue("post.ACCT_CODE",hMCurpAcctCode);
        setValue("post.SRC_PGM",javaProgram);
        setValueLong("post.SRC_PGM_POSTSEQ", hSrcPgmPostseq );
        setValue("post.POST_TYPE","A1");
        
        hThisBusiAddAmt = convAmt(hThisBusiAddAmt);
        setValueDouble("post.POST_TYPE_AMT",hThisBusiAddAmt);
        setValue("post.POST_NOTE",hPostNote);
        setValue("post.BILL_TYPE",hMCurpBillType);
        setValue("post.TXN_CODE",hMCurpTransactionCode);
        setValue("post.ACCT_TYPE",hMCurpAcctType);
        setValue("post.MOD_TIME",sysDate + sysTime);	
        setValue("post.MOD_PGM",javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) 
           { 
            comcr.errRtn("insert_this act_post_log ERROR ", hBusiBusinessDate+" "+
            hTempCurrCode+" "+hMCurpAcctCode+" "+javaProgram+hSrcPgmPostseq, comcr.hCallBatchSeqno);
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
        if (!hMCurpTransactionCode.equals("CF") && comc.getSubString(swSubtotal, 0,1).equals("Y")) {
            printSubtotal();
            swSubtotal = "N";
        }
        if (hMCurpTransactionCode.equals("CF")) {
            tempX20 = "預借現金手續費";
        } else if (hMCurpTransactionCode.equals("VF")) {
            tempX20 = "其他手續費-燃料費";
        } else if (hMCurpTransactionCode.equals("VP")) {
            tempX20 = "其他手續費-所得稅";
        } else if (hMCurpTransactionCode.equals("VX")) {
            tempX20 = "其他手續費-罰單";
        } else if (hMCurpTransactionCode.equals("VT")) {
            tempX20 = "其他手續費-電信費";
        } else if (hMCurpTransactionCode.equals("TL")) {
            tempX20 = "其他手續費-代償";
        } else if (hMCurpTransactionCode.equals("VR")) {
            tempX20 = "其他手續費-規費";
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
        buf = comcr.insertStr(buf, hMCurpCardSw, 1);
        buf = comcr.insertStr(buf, tempX20, 4);
        szTmp = comcr.commFormat("3#,3#", hMTempCnt);
        buf = comcr.insertStr(buf, szTmp, 26);
        szTmp = comcr.commFormat("3$,3$,3$.2$", hMCurpDestinationAmt);
        buf = comcr.insertStr(buf, szTmp, 36);
        
        //showLogMessage("D", "", "  8888888 11 buf =" + buf);
        
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
        DbbA007 proc = new DbbA007();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
