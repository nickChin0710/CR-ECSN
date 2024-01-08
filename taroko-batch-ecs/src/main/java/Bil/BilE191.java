/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/20  V1.00.00    JustinWu     program initial                          *
*  109/05/25  V1.00.01    JustinWu     set  the selected values on id_p_seqno and acno_p_seqno columns
*  109/06/17  V1.00.02    JustinWu     insertForeignFee: add getPtrActcode(), setCurpostFromPtrActcode()
*  109/06/23  V1.00.03    JustinWu     change the method that converts yDDD into yyyyMMdd
*  109/07/23  V1.00.04    shiyuqi      coding standard, rename field method & format                   *  
*  109/08/06  V1.00.05    JeffKung     cca_mcht_bill add new field bin_type
*  109-10-19  V1.00.07    shiyuqi      updated for project coding standard     *
*  109/11/16  V1.00.08    JeffKung     CCA_MCHT_BILL sourceBin為空時, 填入"000000"
*  111/09/22  V1.00.09    JeffKung     補CASH_PAY_AMT欄位及二代帳務檔調整       *
*  111/12/09  V1.00.10    JeffKung     解決YDDD轉日期格式錯誤問題                          *
*  112/03/30  V1.00.11    JeffKung     挑資料時增加batch_date為空的條件               *
*****************************************************************************/
package Bil;

import java.math.BigDecimal;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;
import com.CommTxBill.ForeignFeeData;


public class BilE191 extends AccessDAO {
    private final String progname = "財金請款資料入bil_curpost處理 112/03/30 V1.00.11";
    private  String prgmId = "BilE191";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;
    String tempUser = "";
    String cardExistFlag = "";  //Jeff 2020/7/9
    String acnoExistFlag = ""; //Jeff 2020/7/9

	public int mainProcess(String[] args) {
		 BilFiscdtl bilFiscdtl = null;
		 ForeignFeeData foreignFeeData = null;
		 int successBatchCnt = 0;
		 int totalRecordEachBatch = 0;
		 int totalF01Cnt = 0;
		 int f01OffUsCnt = 0;
		 int f01OnUsCnt = 0;
		 int f01CorSponseCnt = 0;
		 int totalF02Cnt = 0;
		 int f02OffUsCnt = 0;
		 int f02OnUsCnt = 0;
		 BigDecimal totalAmount = BigDecimal.ZERO;
		 String previousFctlNO = ""; // 前一個ECS批次號碼
		 String confFlag = "";    // 覆核註記
		 String batchNo = "";
		 String ecsTxCode = "";
		 String busiDate = "";
	     String callBatchSeqno = "";
	     
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commTxBill = new CommTxBill(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);
            //=====================================
            
            busiDate = getBusiDate();
            
			confFlag = getConfFlagFromPtrBillunit();
			
			// 設定SQL: 讀取bil_fiscdtl,條件為Batch_flag為空的請款交易且sort by fctl_no
			setSqlSelectFromBilFiscdtl(); 
			
			openCursor();
			while (fetchTable()) {
				
				cardExistFlag = "";  //Jeff 2020/7/9
				acnoExistFlag = "";  //Jeff 2020/7/9
				String binNo = "";
				
				bilFiscdtl = getBilFiscdtl();
				ecsTxCode = bilFiscdtl.ecsTxCode;
				
				// 若previousFctlNO為空(因為最一開始previousFctlNO="")，則要
				// 1. assign bilFiscdtl.ecsFctlNo to previousFctlNO
				// 2. get new batch number
				if(commTxBill.isEmpty(previousFctlNO)) {
					previousFctlNO = bilFiscdtl.ecsFctlNo;
					batchNo = getBatchNo(busiDate);
					insertBilPostcntl(batchNo, confFlag);
					successBatchCnt++;
				}
				// 若前一個fctlNO與現在的fctlNO不同，則要
				// 1. update前一批的bil_postcntl的total資料，並將totalRecord和totalAmount歸零
				// 2. assign new ecsFctlNo to previousFctlNO
				// 3. get new batch number
				// 4. insert new bil_postcntl
				if( ! previousFctlNO.equals(bilFiscdtl.ecsFctlNo) ) {
					updateBilPostcntl(batchNo, totalRecordEachBatch, totalAmount.doubleValue());
					successBatchCnt++;
					totalRecordEachBatch = 0;
					totalAmount = BigDecimal.ZERO;
					previousFctlNO = bilFiscdtl.ecsFctlNo;
					batchNo = getBatchNo(busiDate);
					insertBilPostcntl(batchNo, confFlag);
				}
				
				
				if (bilFiscdtl.mediaName.length() > 17 ) { 
					//F01&F02為收單檔案
					//F01:只取共同供應契約的資料 (原來的ICACQQND範圍 , 只取特店代號="969799976011")
					//F02:ATM預借現金, 只取自行的CardBin (原來ICEMVQBD序號91)
					if ("F00600000.ICF01QBD".equals(comc.getSubString(bilFiscdtl.mediaName,0,18)) ) {
						totalF01Cnt++;
						
						if ("006969799976011".equals(bilFiscdtl.mchtNo) == false) {
							updateBilFiscdtlSetBatchFlag(bilFiscdtl.ecsReferenceNo,batchNo);
							continue;
						}
						
						f01CorSponseCnt++;
						
						//非本行的Card Bin
						binNo = comc.getSubString(bilFiscdtl.cardNo,0,6);
						if (chkFromPtrBintable(binNo)==false) {
							f01OffUsCnt++;
							updateBilFiscdtlSetBatchFlag(bilFiscdtl.ecsReferenceNo,batchNo);
							continue;
						}
						
						f01OnUsCnt++;
						
					} else if ("F00600000.ICF02QBD".equals(comc.getSubString(bilFiscdtl.mediaName,0,18)) ) {
						totalF02Cnt++;
						//非本行的Card Bin
						binNo = comc.getSubString(bilFiscdtl.cardNo,0,6);
						if (chkFromPtrBintable(binNo)==false) {
							f02OffUsCnt++;
							updateBilFiscdtlSetBatchFlag(bilFiscdtl.ecsReferenceNo,batchNo);
							continue;
						}
						
						f02OnUsCnt++;
					}
				}

				switch (ecsTxCode) {
				// ================沖正交易 BEGIN================================
				// 交易別ECS_TX_CODE in(15,16,17,18,19,35,36,37,38,39)
				case "15":
				case "16":
				case "17":
				case "18":
				case "19":
				case "35":
				case "36":
				case "37":
				case "38":
				case "39":
					insertBilNccc300Dtl(bilFiscdtl, batchNo);
					break;
				// ================沖正交易 END================================
				// ================再提示帳單 BEGIN================================
				// 交易別ECS_TX_CODE in(65,66,67,68,69,85,86,87,88,89)
				case "65":
				case "66":
				case "67":
				case "68":
				case "69":
				case "85":
				case "86":
				case "87":
				case "88":
				case "89":
					insertRskBillLog(bilFiscdtl);
					insertBilNccc300Dtl(bilFiscdtl, batchNo);
					break;
				// ================再提示帳單 END================================
				// ================調單 BEGIN================================
				// 交易別ECS_TX_CODE in(51,52,71,72)
				case "51":
				case "52":
				case "71":
				case "72":
					insertBilNccc300Dtl(bilFiscdtl, batchNo);
					break;
				// ================調單 END================================
				// ================雜項費用 BEGIN================================
				// 交易別ECS_TX_CODE in(10,20)
				case "10":
				case "20":
					insertBilNccc300Dtl(bilFiscdtl, batchNo);
					break;
				// ================雜項費用 END================================	
				// ================不為原始交易或不為原始更正交易 BEGIN================================
				default:
					// 即交易別ECS_TX_CODE not in (05,06,07,08,09,25,26,27,28,29)且沖正碼為空白
					if( ! ( isOriginalTransaction( bilFiscdtl.ecsCbCode, ecsTxCode ) || 
							    isReversalTransaction( bilFiscdtl.ecsCbCode, ecsTxCode) 
						    )
					   ) {
						insertBilNccc300Dtl(bilFiscdtl, batchNo);
					}
					// ================不為原始交易或不為原始更正交易 END================================
					// ================原始交易或原始更正交易 BEGIN================================
					else {
						Curpost curpost = new Curpost();
						String foreignFeeReferenceNo = "";
						
						setCurpostFromBilFiscdtl(curpost, bilFiscdtl);

						// 補齊cca_mcht_bill table內缺的特店資料，若有dupRecord，則可不處理
						insertCcaMchtBill(bilFiscdtl);

						// 依請款卡號及debit_flag取得卡片資料
						getCardData( bilFiscdtl.ecsRealCardNo, bilFiscdtl.ecsDebitFlag);
						if (cardExistFlag.equals("Y")) {
							setCurpostFromCardData(curpost);
							// 依p_seqno及debit_flag取得歸戶資料
							getAcnoData(getValue("p_seqno"), bilFiscdtl.ecsDebitFlag);
							if (cardExistFlag.equals("Y")) {
								setCurpostFromAcnoData(curpost);
							}
						}
						
						//雙幣卡欄位處理
						if (isDesinationCurrencyTaiwan(bilFiscdtl.destCurr)) {
							curpost.destAmt = commTxBill.round(bilFiscdtl.destAmt, 0);  //財金台幣有小數點需四捨五入
							curpost.dcAmount = commTxBill.round(bilFiscdtl.destAmt, 0); //財金台幣有小數點需四捨五入
						} else {
							curpost.destAmt = bilFiscdtl.destAmt;    //設初始值
							curpost.dcAmount = bilFiscdtl.destAmt; //設初始值
						}
						
						curpost.destCurr = bilFiscdtl.destCurr;   //設初始值
						curpost.currCode = "901";                    //設初始值
						curpost.dcExchangeRate = 1.0;             //設初始值
						
						// 確認是否為雙幣卡
						if(bilFiscdtl.ecsDcFlag.equalsIgnoreCase("Y") && ! isDesinationCurrencyTaiwan(bilFiscdtl.destCurr)) {
							// 將雙幣卡金額換算成台幣再放入curpost，並將原來的雙幣卡金額放到curpost.dc_amt
							curpost = computeTWDByDualCurrencyAndSetCurpost(curpost, bilFiscdtl.ecsDcCurr, bilFiscdtl.destAmt);	
						}
						
						// 以bil_fiscdtl.ECS_BILL_TYPE及bil_fiscdtl.ECS_TX_CODE讀取ptr_billtype 取得帳單交易別參數
						getPtrBilltype(bilFiscdtl.ecsBillType, bilFiscdtl.ecsTxCode); 
						setCurpostFromPtrBilltype(curpost, bilFiscdtl.ecsDebitFlag);
						
						// 以ptr_billtype.acct_code 讀取ptr_actcode取得帳務科目參數
						getPtrActcode(curpost.acctCode);
						setCurpostFromPtrActcode(curpost);		
						
						/*2020/7/14國外交易手續費計算與特店手續費計算移至入bill之前再計算 (update by Jeff Kung)
						 *               這邊只留下空值
						// 若清算識別碼=0，則為國外清算
						if( bilFiscdtl.settlFlag.equals("0")) {
							// 計算國外手續費
							foreignFeeData = computeForeignFee(bilFiscdtl, curpost.destAmt);		
						}else {
							foreignFeeData = commTxBill.new ForeignFeeData();
						}
						
						*/
						
						foreignFeeData = commTxBill.new ForeignFeeData();
						
						if(bilFiscdtl.ecsDcFlag.equalsIgnoreCase("Y") && isDesinationCurrencyTaiwan(bilFiscdtl.destCurr)) {
							foreignFeeData.setForeignFee(0);
						}
						
						if( ! bilFiscdtl.ecsDebitFlag.equalsIgnoreCase("Y") && isForeignFeeGreaterThanZero(foreignFeeData) &&
								isOriginalTransaction(bilFiscdtl.ecsCbCode, bilFiscdtl.ecsTxCode)) {
							foreignFeeReferenceNo = commTxBill.getReferenceNo();
							insertForeignFee(bilFiscdtl, curpost, foreignFeeData, batchNo, foreignFeeReferenceNo, busiDate);
						}
						
						insertCurpost(bilFiscdtl, curpost, foreignFeeData,  batchNo, foreignFeeReferenceNo, busiDate);
						
						insertBilNccc300Dtl(bilFiscdtl, batchNo);
						
						// record筆數+1
						totalRecordEachBatch = totalRecordEachBatch + 1;
						
						// 若為負向交易，則負向交易總額 + curpost.destAmt
						// 反之若為正向交易，則正向交易總額 + curpost.destAmt
						totalAmount = computeTotalAmount( totalAmount, curpost.destAmt, bilFiscdtl.ecsSignCode);
						
					} // else end
					break;
					// ================原始交易或原始更正交易 END================================
				} // switch end
				
				updateBilFiscdtlSetBatchFlag(bilFiscdtl.ecsReferenceNo,batchNo);
				
			} //while fetchTable
			
			
			
			if(successBatchCnt == 0) {
				showLogMessage("I", "", String.format("%s：沒有須要新增的檔案", progname,successBatchCnt));
			}else {
				// 更新最後一批資料至BilPostcntl
				updateBilPostcntl(batchNo, totalRecordEachBatch, totalAmount.doubleValue()); 
				showLogMessage("I", "", String.format("%s：新增%s組批號檔案成功", progname,successBatchCnt));
			}
			
			showLogMessage("I", "", "F01處理結果- totalCnt=["+totalF01Cnt+"],offusCnt=["+ f01OffUsCnt+"],corSponseCnt=["+f01CorSponseCnt +"],onUsCnt=["+f01OnUsCnt +"]");
			showLogMessage("I", "", "F02處理結果- totalCnt=["+totalF02Cnt+"],offusCnt=["+ f02OffUsCnt+"],onUsCnt=["+f02OnUsCnt +"]");
			
			showLogMessage("I", "", "執行結束");
			comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";  
            expHandle(e); 
            return  exceptExit; 
		} finally {
			finalProcess();
		}
	}

	private void updateBilFiscdtlSetBatchFlag(String ecsReferenceNo, String batchNo) throws Exception {
		
		daoTable   = "BIL_FISCDTL";
        
        updateSQL  = "batch_flag     = ?,";
        updateSQL += "batch_date        = ?, ";
        updateSQL += "batch_no        = ?";
        whereStr   = "where ecs_reference_no = ? ";
        setString(1, "Y");
        setString(2, businessDate);
        setString(3, batchNo);
        setString(4, ecsReferenceNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update BIL_FISCDTL not found!", "", comcr.hCallBatchSeqno);
        }
        commTxBill.printDebugString("更新 BIL_FISCDTL 成功");
		
	}

	/**
	 * 
	 * @param batchNo
	 * @param totalRecordEachBatch
	 * @param totalAmount
	 * @throws Exception
	 */
	private void updateBilPostcntl(String batchNo, int totalRecordEachBatch, Double totalAmount) throws Exception {

         daoTable   = "bil_postcntl";
         
         updateSQL  = "tot_record     = ?,";
         updateSQL += "tot_amt        = ?";
         whereStr   = "where batch_no = ? ";
         setLong(1, totalRecordEachBatch);
         setDouble(2, totalAmount);
         setString(3, batchNo);
         updateTable();
         if (notFound.equals("Y")) {
             comcr.errRtn("update_bil_postcntl not found!", "", comcr.hCallBatchSeqno);
         }
         commTxBill.printDebugString("更新 bil_postcntl 成功");
		
	}


	private void insertBilPostcntl(String batchNo, String confFlag) throws Exception {
		String batchBusiDate = comc.getSubString(batchNo,0,8); 
		String batchUnit = comc.getSubString(batchNo,8,10); 
		String batchSeq = comc.getSubString(batchNo,10);
		String confFlagP = "";
		
        if (!confFlag.equalsIgnoreCase("Y")) {
        	confFlagP = "Y";
        }else {
        	confFlagP = "N";
        }
		
		// ============================
		
		daoTable = "bil_postcntl";
		
		// ============================
		
		setValue("BATCH_DATE", batchBusiDate);
		setValue("BATCH_UNIT",  batchUnit);
		setValue("BATCH_SEQ", batchSeq);
		setValue("BATCH_NO", batchNo);
		setValueInt("TOT_RECORD", 0 );
		setValueDouble("TOT_AMT", 0.0);
		setValue("CONFIRM_FLAG_P", confFlagP);
		setValue("CONFIRM_FLAG", confFlag);
		setValue("THIS_CLOSE_DATE", batchBusiDate);
		setValue("MOD_USER", "ecs");
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_PGM", javaProgram);
		
		// ============================
		
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_bil_postcntl duplicate", "", comcr.hCallBatchSeqno);
		}
		commTxBill.printDebugString("新增bil_postcntl成功!!!!!!!!!!!!");

	}

	/**
	 * insert into ForeignFee
	 * @param bilFiscdtl
	 * @param curpost
	 * @param foreignFeeData
	 * @param batchNo
	 * @param foreignFeeReferenceNo
	 * @param busiDate
	 * @throws Exception
	 */
	private void insertForeignFee(BilFiscdtl bilFiscdtl, Curpost curpost, ForeignFeeData foreignFeeData,
			String batchNo, String foreignFeeReferenceNo, String busiDate) throws Exception {
		Curpost curpostForeignFee = (Curpost) curpost.clone();
		
		String processDate = "";
		String acctCode = "";
		String billType = "FIFC";
		
		getPtrBilltype(billType, bilFiscdtl.ecsTxCode);
		acctCode = getValue("acct_code");
		getPtrActcode(acctCode);
		setCurpostFromPtrActcode(curpostForeignFee);
		
		//============= turn yDDD into yyyyMMdd===================
		
		if (bilFiscdtl.processDay.length() > 0 )
			processDate = julDate(bilFiscdtl.processDay);
		
		// ================================================
		
		daoTable = "bil_curpost";
		
		// ==============bilFiscdtl BEGIN=========================
	
		setValue("REFERENCE_NO", foreignFeeReferenceNo);
		setValue("ACQ_MEMBER_ID", bilFiscdtl.acqBusinessId);
		setValue("AUTH_CODE", bilFiscdtl.authCode);
		setValue("EC_IND", bilFiscdtl.ecInd);
		setValue("CUS_MCHT_NO", bilFiscdtl.ecsCusMchtNo);
		setValue("PLATFORM_KIND", bilFiscdtl.ecsPlatformKind);
		setValue("CARD_NO", bilFiscdtl.ecsRealCardNo);
		setValue("SIGN_FLAG", bilFiscdtl.ecsSignCode);
		setValue("TXN_CODE", bilFiscdtl.ecsTxCode);
		setValue("FILM_NO", bilFiscdtl.filmNo);
		setValue("FISC_TXN_CODE", bilFiscdtl.fiscTxCode);
		setValue("MCHT_CATEGORY", bilFiscdtl.mccCode);
		setValue("MCHT_CHI_NAME", bilFiscdtl.mchtChiName);
		setValue("MCHT_CITY", bilFiscdtl.mchtCity);
		setValue("MCHT_COUNTRY", bilFiscdtl.mchtCountry);
		setValue("MCHT_ENG_NAME", bilFiscdtl.mchtEngName);
		setValue("MCHT_NO", bilFiscdtl.mchtNo);
		setValue("MCHT_STATE", bilFiscdtl.mchtState);
		setValue("MCHT_ZIP", bilFiscdtl.mchtZip);
		setValue("MCHT_TYPE", bilFiscdtl.ncccMchtType);
		setValue("PAYMENT_TYPE", bilFiscdtl.paymentType);
		setValue("POS_ENTRY_MODE", bilFiscdtl.posEntryMode);
		setValue("PURCHASE_DATE", bilFiscdtl.purchaseDate);
		setValue("SETTL_AMT", bilFiscdtl.setlAmt);
		setValue("TERMINAL_ID", bilFiscdtl.terminalId);
		setValue("UCAF", bilFiscdtl.ucaf);
		setValue("V_CARD_NO", bilFiscdtl.ecsVCardNo);
		setValue("ONUS_TER_NO", bilFiscdtl.onusTerNo);
		setValue("ONUS_ORDER_NO", bilFiscdtl.onusOrderNo);
		
		// ================bilFiscdtl END========================
		// ==============curpostForeignFee BEGIN=========================
		
		setValue("ACCT_TYPE", curpostForeignFee.acctType);
		setValue("STMT_CYCLE", curpostForeignFee.stmtCycle);
		setValue("BIN_TYPE", curpostForeignFee.binType);
		setValue("GROUP_CODE", curpostForeignFee.groupCode);
		setValue("ID_P_SEQNO", curpostForeignFee.idPSeqno);
		setValue("ISSUE_DATE", curpostForeignFee.issueDate);
		setValue("MAJOR_CARD_NO", curpostForeignFee.majorCardNo);
		setValue("MAJOR_ID_P_SEQNO", curpostForeignFee.majorIdPSeqno);
		setValue("ACNO_P_SEQNO", curpostForeignFee.acnoPSeqno);
		setValue("P_SEQNO", curpostForeignFee.pSeqno);
		setValue("PROD_NO", curpostForeignFee.prodNo);
		setValue("PROMOTE_DEPT", curpostForeignFee.promoteDept);
		setValue("SOURCE_CODE", curpostForeignFee.sourceCode);
		setValue("ERR_CHK_OK_FLAG", curpostForeignFee.errChkOkFlag);
		setValue("DOUBLE_CHK_OK_FLAG", curpostForeignFee.doubleChkOkFlag);
		setValue("ACCT_CHI_SHORT_NAME", curpostForeignFee.acctChiShortName);
		setValue("ACCT_ENG_SHORT_NAME", curpostForeignFee.acctEngShortName);
		setValue("ITEM_CLASS_BACK_DATE", curpostForeignFee.itemClassBackDate);
		setValue("ITEM_CLASS_NORMAL", curpostForeignFee.itemClassNormal);
		setValue("ITEM_CLASS_REFUND", curpostForeignFee.itemClassRefund);
		setValue("ITEM_ORDER_BACK_DATE", curpostForeignFee.itemOrderBackDate);
		setValue("ITEM_ORDER_NORMAL", curpostForeignFee.itemOrderNormal);
		setValue("ITEM_ORDER_REFUND", curpostForeignFee.itemOrderRefund);
		setValue("ACCT_ITEM", curpostForeignFee.acctItem);
		setValue("CASH_ADV_STATE", curpostForeignFee.cashAdvState);
		setValue("ENTRY_ACCT", curpostForeignFee.entryAcct);
		setValue("ACEXTER_DESC", curpostForeignFee.acexterDesc);
		setValue("FEES_STATE", curpostForeignFee.feesState);
		setValue("FORMAT_CHK_OK_FLAG", curpostForeignFee.formatChkOkFlag);
		
	
		// ================curpostForeignFee END========================
		// ================其他=============================
		
		setValue("ACCT_CODE", acctCode);
		setValue("BILL_TYPE", billType);
		setValueInt("INCLUDE_FEE_AMT", 0);
		setValueDouble("ISSUE_FEE", 0.0);
		setValue("ACCTITEM_CONVT_FLAG", "Y");
		setValue("TX_CONVT_FLAG", "Y");
		setValue("REFERENCE_NO_ORIGINAL", bilFiscdtl.ecsReferenceNo);
		setValue("PROCESS_DATE", processDate);
		setValue("FEES_REFERENCE_NO", "");
		setValue("REFERENCE_NO_FEE_F", "");
		setValue("BATCH_NO", batchNo);
		setValueDouble("CASH_PAY_AMT", foreignFeeData.getForeignFee());
		setValueDouble("DEST_AMT", foreignFeeData.getForeignFee());
		setValue("DEST_CURR", foreignFeeData.getForeignFeeCurr());		
		if(bilFiscdtl.ecsDcFlag.equalsIgnoreCase("Y")) {
			setValueDouble("DC_AMOUNT", foreignFeeData.getDcForeignFee());
			setValueDouble("SOURCE_AMT", foreignFeeData.getDcForeignFee());
			setValue("SOURCE_CURR", foreignFeeData.getDcForeignFeeCurr());
			setValue("CURR_CODE", foreignFeeData.getDcForeignFeeCurr());
			setValueDouble("DC_EXCHANGE_RATE", curpostForeignFee.dcExchangeRate);
		}else {
			setValueDouble("SOURCE_AMT", foreignFeeData.getForeignFee());
			setValue("SOURCE_CURR", foreignFeeData.getForeignFeeCurr());
			setValue("CURR_CODE", "901");
			setValueDouble("DC_EXCHANGE_RATE", 0.0);
		}				

		// ===========================================
		
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_USER", tempUser);
		setValue("MOD_PGM", javaProgram);
		setValue("THIS_CLOSE_DATE", busiDate);
		
		// ===========================================
		
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert bil_curpost_fee duplicate", "", comcr.hCallBatchSeqno);
        }
        commTxBill.printDebugString("新增bil_curpost_fee成功!!!!!!!!!!!!");
		
		
	}

	private void insertCurpost(BilFiscdtl bilFiscdtl, Curpost curpost,ForeignFeeData foreignFeeData, 
			String batchNo, String foreignFeeReferenceNo, String busiDate) throws Exception {
		if(bilFiscdtl.ecsDebitFlag.equalsIgnoreCase("Y")) {
			insertDbbCurpost( bilFiscdtl,  curpost, foreignFeeData, batchNo,  foreignFeeReferenceNo,  busiDate);
		}else {
			insertBilCurpost( bilFiscdtl,  curpost, foreignFeeData, batchNo,  foreignFeeReferenceNo,  busiDate);
		}
	}

	private void insertDbbCurpost(BilFiscdtl bilFiscdtl, Curpost curpost, ForeignFeeData foreignFeeData, String batchNo,
			String foreignFeeReferenceNo, String busiDate) throws Exception {
		
		String processDate = "";

		// ============= turn yDDD into yyyyMMdd===================
		
		if (bilFiscdtl.processDay.length() > 0 )	
			processDate = julDate(bilFiscdtl.processDay);

		// ================================================

		daoTable = "dbb_curpost";

		// ==============bilFiscdtl BEGIN=========================
		
		setValue("FISC_TXN_CODE", bilFiscdtl.fiscTxCode);
		setValue("ACQ_MEMBER_ID", bilFiscdtl.acqBusinessId);
		setValue("AUTH_CODE", bilFiscdtl.authCode);
		if (comc.str2double(bilFiscdtl.bonusPayCash) > 0) {
			setValueDouble("CASH_PAY_AMT", comc.str2double(bilFiscdtl.bonusPayCash));
		} else {
			setValueDouble("CASH_PAY_AMT", curpost.destAmt);
		}
		
		setValue("DEDUCT_BP", bilFiscdtl.bonusTransBp);
		setValue("EC_IND", bilFiscdtl.ecInd);
		setValue("BILL_TYPE", bilFiscdtl.ecsBillType);
		setValue("CUS_MCHT_NO", bilFiscdtl.ecsCusMchtNo);
		setValue("PLATFORM_KIND", bilFiscdtl.ecsPlatformKind);
		setValue("CARD_NO", bilFiscdtl.ecsRealCardNo);
		setValue("REFERENCE_NO", bilFiscdtl.ecsReferenceNo);
		setValue("SIGN_FLAG", bilFiscdtl.ecsSignCode);
		setValue("TXN_CODE", bilFiscdtl.ecsTxCode);
		setValue("V_CARD_NO", bilFiscdtl.ecsVCardNo);
		setValue("FILM_NO", bilFiscdtl.filmNo);
		setValue("INSTALL_FEE", bilFiscdtl.installCharges);
		setValue("INSTALL_FIRST_AMT", bilFiscdtl.installFirstAmt);
		setValue("INSTALL_PER_AMT", bilFiscdtl.installPerAmt);
		setValue("INSTALL_TOT_TERM1", bilFiscdtl.installTotTerm);
		setValue("MCHT_CATEGORY", bilFiscdtl.mccCode);
		setValue("MCHT_CHI_NAME", bilFiscdtl.mchtChiName);
		setValue("MCHT_CITY", bilFiscdtl.mchtCity);
		setValue("MCHT_COUNTRY", bilFiscdtl.mchtCountry);
		setValue("MCHT_ENG_NAME", bilFiscdtl.mchtEngName);
		setValue("MCHT_NO", bilFiscdtl.mchtNo);
		setValue("MCHT_STATE", bilFiscdtl.mchtState);
		setValue("MCHT_ZIP", bilFiscdtl.mchtZip);
		setValue("MCS_NUM", bilFiscdtl.mutiClearingSeq);
		setValue("MCHT_TYPE", bilFiscdtl.ncccMchtType);
		setValue("PAYMENT_TYPE", bilFiscdtl.paymentType);
		setValue("POS_ENTRY_MODE", bilFiscdtl.posEntryMode);
		setValue("PURCHASE_DATE", bilFiscdtl.purchaseDate);
		setValue("SETTL_AMT", bilFiscdtl.setlAmt);
		setValue("SOURCE_AMT", bilFiscdtl.sourceAmt);
		setValue("SOURCE_CURR", bilFiscdtl.sourceCurr);
		setValue("UCAF", bilFiscdtl.ucaf);
		setValue("ONUS_TER_NO", bilFiscdtl.onusTerNo);
		setValue("ONUS_ORDER_NO", bilFiscdtl.onusOrderNo);
		setValue("SETTL_FLAG", bilFiscdtl.settlFlag);
		setValue("ECS_PLATFORM_KIND", bilFiscdtl.ecsPlatformKind);
		setValue("ECS_CUS_MCHT_NO", bilFiscdtl.ecsCusMchtNo);
				
		
		// ==============bilFiscdtl END=========================
		// ==============curpost BEGIN=========================
		
		setValue("ACCT_TYPE", curpost.acctType);
		setValue("STMT_CYCLE", curpost.stmtCycle);
		setValue("BIN_TYPE", curpost.binType);
		setValue("GROUP_CODE", curpost.groupCode);
		setValue("ID_P_SEQNO", curpost.idPSeqno);
		setValue("ISSUE_DATE", curpost.issueDate);
		setValue("MAJOR_CARD_NO", curpost.majorCardNo);
		setValue("MAJOR_ID_P_SEQNO", curpost.majorIdPSeqno);
		setValue("P_SEQNO", curpost.pSeqno);
		setValue("PROD_NO", curpost.prodNo);
		setValue("PROMOTE_DEPT", curpost.promoteDept);
		setValue("SOURCE_CODE", curpost.sourceCode);
		setValue("ERR_CHK_OK_FLAG", curpost.errChkOkFlag);
		setValue("DOUBLE_CHK_OK_FLAG", curpost.doubleChkOkFlag);
		setValue("ACCT_CHI_SHORT_NAME", curpost.acctChiShortName);
		setValue("ACCT_ENG_SHORT_NAME", curpost.acctEngShortName);
		setValue("ITEM_CLASS_BACK_DATE", curpost.itemClassBackDate);
		setValue("ITEM_CLASS_NORMAL", curpost.itemClassNormal);
		setValue("ITEM_CLASS_REFUND", curpost.itemClassRefund);
		setValue("ITEM_ORDER_BACK_DATE", curpost.itemOrderBackDate);
		setValue("ITEM_ORDER_NORMAL", curpost.itemOrderNormal);
		setValue("ITEM_ORDER_REFUND", curpost.itemOrderRefund);
		setValue("ACCT_CODE", curpost.acctCode);
		setValue("ACCT_ITEM", curpost.acctItem);
		setValue("ENTRY_ACCT", curpost.entryAcct);
		setValue("ACEXTER_DESC", curpost.acexterDesc);
		setValue("FORMAT_CHK_OK_FLAG", curpost.formatChkOkFlag);
		
		// ==============curpost END=========================
		// ==============其他==============================

		setValueDouble("DC_EXCHANGE_RATE", curpost.dcExchangeRate);
		setValue("BATCH_NO", batchNo);
		setValue("CASH_ADV_STATE", curpost.cashAdvState);
		
		//需要收取特店手續費及國外交易手續費的交易將fee_state改為'Y'
		if (bilFiscdtl.settlFlag.equals("0") || bilFiscdtl.ecsCusMchtNo.length()>0)    
		{
			setValue("FEES_STATE", "Y");  
		} else {
			setValue("FEES_STATE", curpost.feesState);
		}
		
		setValue("ACCTITEM_CONVT_FLAG", "Y");
		setValue("TX_CONVT_FLAG", "Y");
		setValue("PROCESS_DATE", processDate);
		setValue("REFERENCE_NO_ORIGINAL", "");
		setValue("FEES_REFERENCE_NO", "");
		if (isOriginalTransaction(bilFiscdtl.ecsCbCode, bilFiscdtl.ecsTxCode)) {
			setValue("REFERENCE_NO_FEE_F", foreignFeeReferenceNo);
		} else {
			setValue("REFERENCE_NO_FEE_F", "");
		}
		setValue("DEST_CURR", curpost.destCurr);
		setValueDouble("DEST_AMT", curpost.destAmt);
		setValueDouble("ORI_AMT", curpost.destAmt);
		setValueDouble("DC_DEST_AMT", curpost.dcAmount);
		setValueDouble("DC_EXCHANGE_RATE", curpost.dcExchangeRate);
		setValue("CURR_CODE", curpost.currCode);
		
		// ===========================================
		
		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_USER", tempUser);
		setValue("MOD_PGM", javaProgram);
		setValue("THIS_CLOSE_DATE", busiDate);

		// ===========================================
		
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_dbb_curpost duplicate", "", comcr.hCallBatchSeqno);
		}
		commTxBill.printDebugString("新增bil_curpost成功!!!!!!!!!!!!!!!!!!");
		
	}

	private void insertBilCurpost(BilFiscdtl bilFiscdtl, Curpost curpost, ForeignFeeData foreignFeeData, String batchNo,
			String foreignFeeReferenceNo, String busiDate) throws Exception {
		String processDate = "";

		// ============= turn yDDD into yyyyMMdd===================

		if (bilFiscdtl.processDay.length() > 0 )
			processDate = julDate(bilFiscdtl.processDay);
	
		// ================================================

		daoTable = "bil_curpost";

		// ==============bilFiscdtl BEGIN=========================

		setValue("FISC_TXN_CODE", bilFiscdtl.fiscTxCode);
		setValue("ACQ_MEMBER_ID", bilFiscdtl.acqBusinessId);
		setValue("AUTH_CODE", bilFiscdtl.authCode);
		//showLogMessage("I","","bonusPayCash="+bilFiscdtl.bonusPayCash);
		//showLogMessage("I","","bonusPayCash (double) ="+comc.str2double(bilFiscdtl.bonusPayCash));
		if (comc.str2double(bilFiscdtl.bonusPayCash) > 0) {
			setValueDouble("CASH_PAY_AMT", comc.str2double(bilFiscdtl.bonusPayCash));
		} else {
			setValueDouble("CASH_PAY_AMT", curpost.destAmt);
		}
		setValue("DEDUCT_BP", bilFiscdtl.bonusTransBp);
		setValue("EC_IND", bilFiscdtl.ecInd);
		setValue("BILL_TYPE", bilFiscdtl.ecsBillType);
		setValue("CUS_MCHT_NO", bilFiscdtl.ecsCusMchtNo);
		setValue("PLATFORM_KIND", bilFiscdtl.ecsPlatformKind);
		setValue("CARD_NO", bilFiscdtl.ecsRealCardNo);
		setValue("REFERENCE_NO", bilFiscdtl.ecsReferenceNo);
		setValue("SIGN_FLAG", bilFiscdtl.ecsSignCode);
		setValue("TXN_CODE", bilFiscdtl.ecsTxCode);
		setValue("FILM_NO", bilFiscdtl.filmNo);
		setValue("AMT_ICCR", bilFiscdtl.iccr);
		setValue("INSTALL_FEE", bilFiscdtl.installCharges);
		setValue("INSTALL_FIRST_AMT", bilFiscdtl.installFirstAmt);
		setValue("INSTALL_PER_AMT", bilFiscdtl.installPerAmt);
		setValue("INSTALL_TOT_TERM", bilFiscdtl.installTotTerm);
		setValue("AMT_MCCR", bilFiscdtl.mCca);
		setValue("MCHT_CATEGORY", bilFiscdtl.mccCode);
		setValue("MCHT_CHI_NAME", bilFiscdtl.mchtChiName);
		setValue("MCHT_CITY", bilFiscdtl.mchtCity);
		setValue("MCHT_COUNTRY", bilFiscdtl.mchtCountry);
		setValue("MCHT_ENG_NAME", bilFiscdtl.mchtEngName);
		setValue("MCHT_NO", bilFiscdtl.mchtNo);
		setValue("MCHT_STATE", bilFiscdtl.mchtState);
		setValue("MCHT_ZIP", bilFiscdtl.mchtZip);
		setValue("MCS_NUM", bilFiscdtl.mutiClearingSeq);
		setValue("MCHT_TYPE", bilFiscdtl.ncccMchtType);
		setValue("PAYMENT_TYPE", bilFiscdtl.paymentType);
		setValue("POS_ENTRY_MODE", bilFiscdtl.posEntryMode);
		setValue("PURCHASE_DATE", bilFiscdtl.purchaseDate);
		setValue("SETTL_AMT", bilFiscdtl.setlAmt);
		setValue("SOURCE_AMT", bilFiscdtl.sourceAmt);
		setValue("SOURCE_CURR", bilFiscdtl.sourceCurr);
		setValue("TERMINAL_ID", bilFiscdtl.terminalId);
		setValue("UCAF", bilFiscdtl.ucaf);
		setValue("V_CARD_NO", bilFiscdtl.ecsVCardNo);
		setValue("ONUS_TER_NO", bilFiscdtl.onusTerNo);
		setValue("ONUS_ORDER_NO", bilFiscdtl.onusOrderNo);
		setValue("SETTL_FLAG", bilFiscdtl.settlFlag);
		setValue("ECS_PLATFORM_KIND", bilFiscdtl.ecsPlatformKind);
		setValue("ECS_CUS_MCHT_NO", bilFiscdtl.ecsCusMchtNo);

		// ==============bilFiscdtl END=========================
		// ==============curpost BEGIN=========================

		setValue("ACCT_TYPE", curpost.acctType);
		setValue("STMT_CYCLE", curpost.stmtCycle);
		setValue("BIN_TYPE", curpost.binType);
		setValue("GROUP_CODE", curpost.groupCode);
		setValue("ID_P_SEQNO", curpost.idPSeqno);
		setValue("ISSUE_DATE", curpost.issueDate);
		setValue("MAJOR_CARD_NO", curpost.majorCardNo);
		setValue("MAJOR_ID_P_SEQNO", curpost.majorIdPSeqno);
		setValue("ACNO_P_SEQNO", curpost.acnoPSeqno);
		setValue("P_SEQNO", curpost.pSeqno);
		setValue("PROD_NO", curpost.prodNo);
		setValue("PROMOTE_DEPT", curpost.promoteDept);
		setValue("SOURCE_CODE", curpost.sourceCode);
		setValue("ERR_CHK_OK_FLAG", curpost.errChkOkFlag);
		setValue("DOUBLE_CHK_OK_FLAG", curpost.doubleChkOkFlag);
		setValue("ACCT_CHI_SHORT_NAME", curpost.acctChiShortName);
		setValue("ACCT_ENG_SHORT_NAME", curpost.acctEngShortName);
		setValue("ITEM_CLASS_BACK_DATE", curpost.itemClassBackDate);
		setValue("ITEM_CLASS_NORMAL", curpost.itemClassNormal);
		setValue("ITEM_CLASS_REFUND", curpost.itemClassRefund);
		setValue("ITEM_ORDER_BACK_DATE", curpost.itemOrderBackDate);
		setValue("ITEM_ORDER_NORMAL", curpost.itemOrderNormal);
		setValue("ITEM_ORDER_REFUND", curpost.itemOrderRefund);
		setValue("ACCT_CODE", curpost.acctCode);
		setValue("ACCT_ITEM", curpost.acctItem);
		setValue("CASH_ADV_STATE", curpost.cashAdvState);
		setValue("ENTRY_ACCT", curpost.entryAcct);
		setValue("ACEXTER_DESC", curpost.acexterDesc);

		//需要收取特店手續費及國外交易手續費的交易將fee_state改為'Y'
		//台酒卡需要收取手續費
		if (bilFiscdtl.settlFlag.equals("0") || bilFiscdtl.ecsCusMchtNo.length()>0 ||
		    ( "1299".equals(curpost.groupCode) || "3782".equals(curpost.groupCode) ) )    
		{
			setValue("FEES_STATE", "Y");  
		} else {
			setValue("FEES_STATE", curpost.feesState);
		}
		setValue("FORMAT_CHK_OK_FLAG", curpost.formatChkOkFlag);

		// ==============curpost END=========================
		// ==============其他==============================

		setValue("BATCH_NO", batchNo);
		setValue("ACCTITEM_CONVT_FLAG", "Y");
		setValue("TX_CONVT_FLAG", "Y");
		setValue("PROCESS_DATE", processDate);
		setValue("CONTRACT_NO", "");
		setValue("FEES_REFERENCE_NO", "");
		setValue("REFERENCE_NO_ORIGINAL", "");
		if (isOriginalTransaction(bilFiscdtl.ecsCbCode, bilFiscdtl.ecsTxCode)) {
			setValue("REFERENCE_NO_FEE_F", foreignFeeReferenceNo);
		} else {
			setValue("REFERENCE_NO_FEE_F", "");
		}
		setValue("DEST_CURR", curpost.destCurr);
		setValueDouble("DEST_AMT", curpost.destAmt);
		setValueDouble("DC_AMOUNT", curpost.dcAmount);
		setValue("CURR_CODE", curpost.currCode);
		setValueDouble("DC_EXCHANGE_RATE", curpost.dcExchangeRate);
		
		// ===========================================

		setValue("MOD_TIME", sysDate + sysTime);
		setValue("MOD_USER", tempUser);
		setValue("MOD_PGM", javaProgram);
		setValue("THIS_CLOSE_DATE", busiDate);

		// ===========================================

		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert bil_curpost duplicate", "", comcr.hCallBatchSeqno);
		}
		
		commTxBill.printDebugString("新增bil_curpost成功!!!!!!!!!!!!!!!!!!");

	}

	/**
	 * insert into bil_nccc300_dtl
	 * @param bilFiscdtl
	 * @param batchNo
	 * @throws Exception 
	 */
	private void insertBilNccc300Dtl(BilFiscdtl bilFiscdtl, String batchNo) throws Exception {
		String qrFlag = "";	    //QR-CODE註記
		
		// bil_fiscdtl.PAYMENT_TYPE = Q，則放Y
		if(bilFiscdtl.paymentType.equalsIgnoreCase("Q") ) {
			qrFlag = "Y";
		}
		
		// ====================================================
		
		daoTable = "bil_nccc300_dtl";
				
		// ====================================================
		
		setValue("BATCH_NO", batchNo);
		setValue("QR_FLAG", qrFlag);
				
		// ====================================================
				
		setValue("REFERENCE_NO", bilFiscdtl.ecsReferenceNo);
		setValue("CRYPTOGRAM", bilFiscdtl.ac);
		setValue("TRANSACTION_TYPE", bilFiscdtl.acTxType);
		setValue("UNPREDIC_NUM", bilFiscdtl.acUnpredNum);
		setValue("ADD_ACCT_TYPE", bilFiscdtl.addnAcctType);
		setValue("ADD_AMT", bilFiscdtl.addnAmt);
		setValue("ADD_CURCY_CODE", bilFiscdtl.addnAmtCurr);
		setValue("ADD_AMT_SIGN", bilFiscdtl.addnAmtSign);
//		setValue("ADD_AMT_TYPE", bilFiscdtl. );
		setValue("APP_INT_PRO", bilFiscdtl.apProfile);
		setValue("APP_TRAN_COUNT", bilFiscdtl.apTxNum);
		setValue("AUTH_RESPONSE_CODE", bilFiscdtl.authRespCode);
		setValue("DATA_AUTH_CODE", bilFiscdtl.authValidCode);
		setValue("CHIP_CONDITION_CODE", bilFiscdtl.chipCondCode);
		setValue("CRY_INFO_DATA", bilFiscdtl.cryptogramInfo);
		setValue("EC_IND", bilFiscdtl.ecInd);
		setValue("ELECTRONIC_TERM_IND", bilFiscdtl.ecInd);
		setValue("CARD_NO", bilFiscdtl.ecsRealCardNo);
//		setValue("V_CARD_NO", bilFiscdtl. );
		setValue("IAD_RESULT", bilFiscdtl.iad);
		setValue("INTER_RATE_DES", bilFiscdtl.ird);
		setValue("MCS_NUM", bilFiscdtl.mutiClearingSeq);
		setValue("TRANSACTION_SOURCE", bilFiscdtl.ncccBillType);
		setValue("CARD_SEQ_NUM", bilFiscdtl.panSeqNum);
		setValue("POS_ENTRY_MODE", bilFiscdtl.posEntryMode);
		setValue("POS_TERM_CAPABILITY", bilFiscdtl.posTeCap);
		setValue("ISSUE_S_R", bilFiscdtl.postIssueResult);
		setValue("REASON_CODE", bilFiscdtl.reasonCode);
		setValue("REIMBURSEMENT_ATTR", bilFiscdtl.reimbAttr);
		setValue("SERVICE_CODE", bilFiscdtl.serviceCode);
		setValue("SETTLEMENT_AMT", bilFiscdtl.setlAmt);
		setValue("EXCHANGE_RATE", bilFiscdtl.setlRate);
		setValue("SETTLEMENT_FLAG", bilFiscdtl.settlFlag);
		setValue("TERMINAL_CAP_PRO", bilFiscdtl.teProfile);
		setValue("TERMINAL_ID", bilFiscdtl.terminalId);
		setValue("TERMINAL_VER_RESULTS", bilFiscdtl.tvr);
		setValue("USAGE_CODE", bilFiscdtl.usageCode);

		// ====================================================

		setValue("MOD_PGM", javaProgram);
		setValue("MOD_TIME", sysDate + sysTime);
		
		// ====================================================
		
		
        if (insertTable() ==0) {
            comcr.errRtn("insert bil_nccc300_dtl ", "", comcr.hCallBatchSeqno);
        }
		commTxBill.printDebugString("新增bil_nccc300_dtl成功!!!!!!!!!!!!!!!!!!");
		
	}

	/**
	 * insert into rsk_bill_log
	 * @param bilFiscdtl
	 * @param batchNO
	 * @throws Exception 
	 */
	private void insertRskBillLog(BilFiscdtl bilFiscdtl) throws Exception {
		
		daoTable = "rsk_bill_log";
		
		setValue("crt_date", sysDate);
        setValue("card_no", bilFiscdtl.ecsRealCardNo);
        setValue("film_no", bilFiscdtl.filmNo);
        setValue("process_date", sysDate);
//        setValueDouble("dc_dest_amt", h_m_curp_destination_amt);
        setValue("curr_code", bilFiscdtl.ecsDcCurr);
        setValue("ctrl_seqno", "");
        setValue("mod_pgm", prgmId);
        setValue("mod_time", sysDate + sysTime);
        
        if (insertTable() == 0) {
        	 comcr.errRtn("insert rsk_bill_log error", "", comcr.hCallBatchSeqno);
        }
        commTxBill.printDebugString("新增rsk_bill_log成功!!!!!!!!!!!!!!!!!!");
		
	}

	/**
	 * 補齊cca_mcht_bill table內缺的特店資料，若有dupRecord，則可不處理
	 * @param bilFiscdtl 
	 * @throws Exception 
	 */
	private void insertCcaMchtBill(BilFiscdtl bilFiscdtl) throws Exception {
		
		daoTable = "cca_mcht_bill";
		
		setValue("mcht_no"      , bilFiscdtl.mchtNo);
		if (bilFiscdtl.sourceBin.length()==0)   //sourceBin為空時, 填入"000000"
		{
			setValue("acq_bank_id"  , "000000"); 
		}
		else {
			setValue("acq_bank_id"  , bilFiscdtl.sourceBin);
		}
        setValue("mcht_name"    , bilFiscdtl.mchtChiName);
        setValue("zip_code"     , bilFiscdtl.mchtZip);
        setValue("zip_city"     , bilFiscdtl.mchtCity);
        setValue("mcc_code"     , bilFiscdtl.mccCode);
        setValue("mcht_eng_name", bilFiscdtl.mchtEngName);
        setValue("bin_type", bilFiscdtl.ecsBinType);  //Jeff 2020/8/6
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("crt_user", prgmId);
        setValue("mod_pgm" , prgmId);
        setValue("mod_time", sysDate + sysTime);

        insertTable();

        commTxBill.printDebugString("新增cca_mcht_bill成功!!!!!!!!!!!!!!!!!!");
		
	}


	/**
		 * 依請款卡號及debit_flag取得卡片資料
		 * @param curpost 
		 * @param cardNo
		 * @param debitFlag
		 * @return 
		 * @throws Exception
		 */
		private void  getCardData(String cardNo, String debitFlag) throws Exception {
			// 是否為debit card
			if(debitFlag.equalsIgnoreCase("Y")) {
				sqlCmd = "select major_card_no,";
		        sqlCmd += "major_id_p_seqno,";
		        sqlCmd += "current_code,";
		        sqlCmd += "decode(ori_issue_date,'',issue_date,ori_issue_date)  as issue_date, ";
		        sqlCmd += "oppost_date,";
		        sqlCmd += "promote_dept,";
		        sqlCmd += "prod_no,";
		        sqlCmd += "group_code,";
		        sqlCmd += "source_code,";
		        sqlCmd += "card_type,";
		        sqlCmd += "p_seqno,";
		        sqlCmd += "bin_no,";
		        sqlCmd += "bin_type,";
		        sqlCmd += "id_p_seqno,";
		        sqlCmd += "nvl(uf_idno_id(id_p_seqno), '') as id ";
		        sqlCmd += " from dbc_card  ";
		        sqlCmd += "where card_no  = ? ";
			}else {
				sqlCmd = "select major_card_no,";
		        sqlCmd += "major_id_p_seqno,";
		        sqlCmd += "current_code,";
		        sqlCmd += "decode(ori_issue_date,'',issue_date,ori_issue_date) as issue_date, ";
		        sqlCmd += "oppost_date,";
		        sqlCmd += "promote_dept,";
		        sqlCmd += "prod_no,";
		        sqlCmd += "group_code,";
		        sqlCmd += "source_code,";
		        sqlCmd += "card_type,";
		        sqlCmd += "acno_p_seqno,";
		        sqlCmd += "id_p_seqno,";
		        sqlCmd += "p_seqno,";
		        sqlCmd += "bin_no,";
		        sqlCmd += "bin_type,";
		        sqlCmd += "nvl(uf_idno_id(id_p_seqno),'') as id ";
		        sqlCmd += " from crd_card  ";
		        sqlCmd += "where card_no  = ? ";		
			}
			
	        setString(1, cardNo);
	
	        if (selectTable() <= 0) {
	        	showLogMessage("I", "", "select card_no error=[" +  cardNo + "]");
	            //throw new Exception("select card_no error=[" +  cardNo + "]");
	        } else {
	        	cardExistFlag = "Y"; //Jeff 2020/7/9
	        }
		}

	/**
	 * 依p_seqno及debit_flag取得歸戶資料
	 * @param pSeqno
	 * @param debitFlag
	 * @return 
	 * @throws Exception 
	 */
	private void getAcnoData(String pSeqno, String debitFlag) throws Exception {
		// 是否為debit card
		if(debitFlag.equalsIgnoreCase("Y")) {
			sqlCmd  = "select acct_type,";
            sqlCmd += "acct_key,";
            sqlCmd += "acct_status,";
            sqlCmd += "stmt_cycle,";
            sqlCmd += "pay_by_stage_flag,";
            sqlCmd += "autopay_acct_no ";
            sqlCmd += " from dba_acno  ";
            sqlCmd += "where p_seqno  = ? ";
		}else {
			sqlCmd = "select acct_type,";
            sqlCmd += "acct_key,";
            sqlCmd += "acct_status,";
            sqlCmd += "stmt_cycle,";
            sqlCmd += "pay_by_stage_flag,";
            sqlCmd += "auto_installment,";
            sqlCmd += "autopay_acct_no ";
            sqlCmd += "from act_acno  ";
            sqlCmd += "where acno_p_seqno  = ? ";
		}
		setString(1, pSeqno);
        int recordCnt = selectTable();
        if (recordCnt <= 0) {
        	showLogMessage("I", "", "select p_seqno error=[" +  pSeqno + "]");
            //throw new Exception("select p_seqno error=[" +  pSeqno + "]");
        } else {
        	acnoExistFlag = "Y"; //Jeff 2020/7/9
        }
 	}

	/**
	 * 以bil_fiscdtl.ECS_BILL_TYPE及bil_fiscdtl.ECS_TX_CODE讀取ptr_billtype 取得帳單交易別參數
	 * @param ecsBillType 帳單類別
	 * @param ecsTxCode ECS交易別
	 * @return 
	 * @throws Exception 
	 */
	private void getPtrBilltype(String ecsBillType, String ecsTxCode) throws Exception {
		sqlCmd = "select bill_type,";
	    sqlCmd += "txn_code,";
	    sqlCmd += "sign_flag,";
	    sqlCmd += "acct_code,";
	    sqlCmd += "acct_item,";
	    sqlCmd += "fees_state,";
	    sqlCmd += "fees_fix_amt,";
	    sqlCmd += "fees_percent,";
	    sqlCmd += "fees_min,";
	    sqlCmd += "fees_max,";
	    sqlCmd += "fees_bill_type,";
	    sqlCmd += "fees_txn_code,";
	    sqlCmd += "inter_desc,";
	    sqlCmd += "exter_desc,";
	    sqlCmd += "interest_mode,";
	    sqlCmd += "adv_wkday,";
	    sqlCmd += "balance_state,";
	    // sqlCmd += "collection_mode,";
	    // sqlCmd += "charge_back_month,";
	    sqlCmd += "cash_adv_state,";
	    sqlCmd += "entry_acct,";
	    sqlCmd += "chk_err_bill,";
	    sqlCmd += "double_chk,";
	    sqlCmd += "format_chk,";
	    sqlCmd += "merch_fee ";
	    sqlCmd += " from ptr_billtype  ";
	    sqlCmd += "where bill_type = ?  ";
	    sqlCmd += "  and txn_code  = ? ";
	    setString(1, ecsBillType);
	    setString(2, ecsTxCode);
	
	    if ( selectTable() <= 0 || notFound.equals("Y") ) {
	        comcr.errRtn("select_ptr_billtype not found", "", comcr.hCallBatchSeqno);
	    }
		
	}

	/**
	 * 以ptr_billtype.acct_code 讀取ptr_actcode取得帳務科目參數
	 * @param acctCode acctCode was gotten from getBilltype() 
	 * @return 
	 * @throws Exception 
	 */
	private void getPtrActcode(String acctCode) throws Exception {  
		
		sqlCmd = "select acct_code,";
	    sqlCmd += "chi_short_name,";
	    sqlCmd += "chi_long_name,";
	    sqlCmd += "eng_short_name,";
	    sqlCmd += "eng_long_name,";
	    sqlCmd += "item_order_normal,";
	    sqlCmd += "item_order_back_date,";
	    sqlCmd += "item_order_refund,";
	    sqlCmd += "item_class_normal,";
	    sqlCmd += "item_class_back_date,";
	    sqlCmd += "item_class_refund,";
	    sqlCmd += "inter_rate_code,";
	    sqlCmd += "part_rev,";
	    sqlCmd += "revolve,";
	    sqlCmd += "acct_method,";
	    sqlCmd += "interest_method,";
	    sqlCmd += "urge_1st,";
	    sqlCmd += "urge_2st,";
	    sqlCmd += "urge_3st,";
	    sqlCmd += "occupy,";
	    sqlCmd += "receivables,";
	    sqlCmd += "query_type ";
	    sqlCmd += " from ptr_actcode  ";
	    sqlCmd += "where acct_code = ? ";
	    setString(1, acctCode); 
	
	    if ( selectTable() <= 0) {
	    	 comcr.errRtn("select_ptr_actcode not found="+acctCode, acctCode, comcr.hCallBatchSeqno);
	    }
		
	}

	/**
	 * 從查詢結果取得BilFiscdtl物件
	 * @return
	 * @throws Exception
	 */
	private BilFiscdtl getBilFiscdtl() throws Exception {
		try {
			BilFiscdtl bilFiscdtl = new BilFiscdtl();
			bilFiscdtl.ac = getValue("AC");
			bilFiscdtl.acCryptoAmt = getValue("AC_CRYPTO_AMT");
			bilFiscdtl.acTxType = getValue("AC_TX_TYPE");
			bilFiscdtl.acUnpredNum = getValue("AC_UNPRED_NUM");
			bilFiscdtl.acctNumType = getValue("ACCT_NUM_TYPE");
			bilFiscdtl.acqBusinessId = getValue("ACQ_BUSINESS_ID");
			bilFiscdtl.acqRespCode = getValue("ACQ_RESP_CODE");
			bilFiscdtl.addnAcctType = getValue("ADDN_ACCT_TYPE");
			bilFiscdtl.addnAmt = getValue("ADDN_AMT");
			bilFiscdtl.addnAmtCurr = getValue("ADDN_AMT_CURR");
			bilFiscdtl.addnAmtSign = getValue("ADDN_AMT_SIGN");
			bilFiscdtl.addnBillCurr = getValue("ADDN_BILL_CURR");
			bilFiscdtl.apExpireDate = getValue("AP_EXPIRE_DATE");
			bilFiscdtl.apProfile = getValue("AP_PROFILE");
			bilFiscdtl.apTxNum = getValue("AP_TX_NUM");
			bilFiscdtl.authCode = getValue("AUTH_CODE");
			bilFiscdtl.authRespCode = getValue("AUTH_RESP_CODE");
			bilFiscdtl.authValidCode = getValue("AUTH_VALID_CODE");
			bilFiscdtl.batchDate = getValue("BATCH_DATE");
			bilFiscdtl.batchFlag = getValue("BATCH_FLAG");
			bilFiscdtl.batchNo = getValue("BATCH_NO");
			bilFiscdtl.bcToDcRate = getValue("BC_TO_DC_RATE");
			bilFiscdtl.bonusPayCash = getValue("BONUS_PAY_CASH");
			bilFiscdtl.bonusTransBp = getValue("BONUS_TRANS_BP");
			bilFiscdtl.busFormatCode = getValue("BUS_FORMAT_CODE");
			bilFiscdtl.cardNo = getValue("CARD_NO");
			bilFiscdtl.cardPlan = getValue("CARD_PLAN");
			bilFiscdtl.cardProductId = getValue("CARD_PRODUCT_ID");
			bilFiscdtl.cardVerfyResult = getValue("CARD_VERFY_RESULT");
			bilFiscdtl.catInd = getValue("CAT_IND");
			bilFiscdtl.cbRefNo = getValue("CB_REF_NO");
			bilFiscdtl.chargeInd = getValue("CHARGE_IND");
			bilFiscdtl.chipCondCode = getValue("CHIP_COND_CODE");
			bilFiscdtl.crossPayCode = getValue("CROSS_PAY_CODE");
			bilFiscdtl.crossPayStatus = getValue("CROSS_PAY_STATUS");
			bilFiscdtl.crossPayType = getValue("CROSS_PAY_TYPE");
			bilFiscdtl.cryptogramInfo = getValue("CRYPTOGRAM_INFO");
			bilFiscdtl.cvmResult = getValue("CVM_RESULT");
			bilFiscdtl.cwbInd = getValue("CWB_IND");
			bilFiscdtl.dccInd = getValue("DCC_IND");
			bilFiscdtl.de22CaptureCap = getValue("DE22_CAPTURE_CAP");
			bilFiscdtl.de22CardData = getValue("DE22_CARD_DATA");
			bilFiscdtl.de22CardInCap = getValue("DE22_CARD_IN_CAP");
			bilFiscdtl.de22CardOutCap = getValue("DE22_CARD_OUT_CAP");
			bilFiscdtl.de22ChAuthCap = getValue("DE22_CH_AUTH_CAP");
			bilFiscdtl.de22ChAuthEntity = getValue("DE22_CH_AUTH_ENTITY");
			bilFiscdtl.de22ChAuthMethod = getValue("DE22_CH_AUTH_METHOD");
			bilFiscdtl.de22ChData = getValue("DE22_CH_DATA");
			bilFiscdtl.de22InputMode = getValue("DE22_INPUT_MODE");
			bilFiscdtl.de22PinCaptureCap = getValue("DE22_PIN_CAPTURE_CAP");
			bilFiscdtl.de22TeOpEnv = getValue("DE22_TE_OP_ENV");
			bilFiscdtl.de22TeOutCap = getValue("DE22_TE_OUT_CAP");
			bilFiscdtl.destAmt = getValueDouble("DEST_AMT");
			bilFiscdtl.destBin = getValue("DEST_BIN");
			bilFiscdtl.destCurr = getValue("DEST_CURR");
			bilFiscdtl.docInd = getValue("DOC_IND");
			bilFiscdtl.ecInd = getValue("EC_IND");
			bilFiscdtl.ecsAcctCode = getValue("ECS_ACCT_CODE");
			bilFiscdtl.ecsBillType = getValue("ECS_BILL_TYPE");
			bilFiscdtl.ecsBinType = getValue("ECS_BIN_TYPE");
			bilFiscdtl.ecsCbCode = getValue("ECS_CB_CODE");
			bilFiscdtl.ecsCusMchtNo = getValue("ECS_CUS_MCHT_NO");
			bilFiscdtl.ecsDcCurr = getValue("ECS_DC_CURR");
			bilFiscdtl.ecsDcFlag = getValue("ECS_DC_FLAG");
			bilFiscdtl.ecsDebitFlag = getValue("ECS_DEBIT_FLAG");
			bilFiscdtl.ecsFctlNo = getValue("ECS_FCTL_NO");
			bilFiscdtl.ecsPlatformKind = getValue("ECS_PLATFORM_KIND");
			bilFiscdtl.ecsRealCardNo = getValue("ECS_REAL_CARD_NO");
			bilFiscdtl.ecsReferenceNo = getValue("ECS_REFERENCE_NO");
			bilFiscdtl.ecsSignCode = getValue("ECS_SIGN_CODE");
			bilFiscdtl.ecsTxChiName = getValue("ECS_TX_CHI_NAME");
			bilFiscdtl.ecsTxCode = getValue("ECS_TX_CODE");
			bilFiscdtl.ecsTxEngName = getValue("ECS_TX_ENG_NAME");
			bilFiscdtl.ecsVCardNo = getValue("ECS_V_CARD_NO");
			bilFiscdtl.emvAcqData = getValue("EMV_ACQ_DATA");
			bilFiscdtl.emvAcqTeId = getValue("EMV_ACQ_TE_ID");
			bilFiscdtl.emvBankNum = getValue("EMV_BANK_NUM");
			bilFiscdtl.emvCashbackAmt = getValue("EMV_CASHBACK_AMT");
			bilFiscdtl.emvCashbackAmt2 = getValue("EMV_CASHBACK_AMT2");
			bilFiscdtl.emvErrorCode = getValue("EMV_ERROR_CODE");
			bilFiscdtl.emvErrorCode2 = getValue("EMV_ERROR_CODE2");
			bilFiscdtl.emvIad2 = getValue("EMV_IAD2");
			bilFiscdtl.emvInputSeq = getValue("EMV_INPUT_SEQ");
			bilFiscdtl.emvJcbCbDate = getValue("EMV_JCB_CB_DATE");
			bilFiscdtl.emvJcbChequeFlag = getValue("EMV_JCB_CHEQUE_FLAG");
			bilFiscdtl.emvJcbDestCurr = getValue("EMV_JCB_DEST_CURR");
			bilFiscdtl.emvJcbExRate = getValue("EMV_JCB_EX_RATE");
			bilFiscdtl.emvJcbInstallTerm = getValue("EMV_JCB_INSTALL_TERM");
			bilFiscdtl.emvJcbMchtNo = getValue("EMV_JCB_MCHT_NO");
			bilFiscdtl.emvJcbMchtState = getValue("EMV_JCB_MCHT_STATE");
			bilFiscdtl.emvJcbReasonCode = getValue("EMV_JCB_REASON_CODE");
			bilFiscdtl.emvJcbTeId = getValue("EMV_JCB_TE_ID");
			bilFiscdtl.emvLocalDate = getValue("EMV_LOCAL_DATE");
			bilFiscdtl.emvLocalTime = getValue("EMV_LOCAL_TIME");
			bilFiscdtl.emvReversalInd = getValue("EMV_REVERSAL_IND");
			bilFiscdtl.eventDate = getValue("EVENT_DATE");
			bilFiscdtl.feeCtrlNum = getValue("FEE_CTRL_NUM");
			bilFiscdtl.feeReasonCode = getValue("FEE_REASON_CODE");
			bilFiscdtl.ffi = getValue("FFI");
			bilFiscdtl.filmNo = getValue("FILM_NO");
			bilFiscdtl.fiscTxCode = getValue("FISC_TX_CODE");
			bilFiscdtl.floorLimitInd = getValue("FLOOR_LIMIT_IND");
			bilFiscdtl.fpi = getValue("FPI");
			bilFiscdtl.fraudCbCnt = getValue("FRAUD_CB_CNT");
			bilFiscdtl.fraudNotifyDate = getValue("FRAUD_NOTIFY_DATE");
			bilFiscdtl.fxjExDate = getValue("FXJ_EX_DATE");
			bilFiscdtl.fxjTeSource = getValue("FXJ_TE_SOURCE");
			bilFiscdtl.iad = getValue("IAD");
			bilFiscdtl.iccr = getValue("ICCR");
			bilFiscdtl.installAtmNo = getValue("INSTALL_ATM_NO");
			bilFiscdtl.installCharges = getValue("INSTALL_CHARGES");
			bilFiscdtl.installFirstAmt = getValue("INSTALL_FIRST_AMT");
			bilFiscdtl.installLastAmt = getValue("INSTALL_LAST_AMT");
			bilFiscdtl.installPerAmt = getValue("INSTALL_PER_AMT");
			bilFiscdtl.installProjNo = getValue("INSTALL_PROJ_NO");
			bilFiscdtl.installSupplyNo = getValue("INSTALL_SUPPLY_NO");
			bilFiscdtl.installTotTerm = getValue("INSTALL_TOT_TERM");
			bilFiscdtl.installType = getValue("INSTALL_TYPE");
			bilFiscdtl.interfaceDevNum = getValue("INTERFACE_DEV_NUM");
			bilFiscdtl.interfaceTraceNum = getValue("INTERFACE_TRACE_NUM");
			bilFiscdtl.ird = getValue("IRD");
			bilFiscdtl.issueCtrlNum = getValue("ISSUE_CTRL_NUM");
			bilFiscdtl.mCca = getValue("M_CCA");
			bilFiscdtl.mCrossInd = getValue("M_CROSS_IND");
			bilFiscdtl.mCurrInd = getValue("M_CURR_IND");
			bilFiscdtl.mccCode = getValue("MCC_CODE");
			bilFiscdtl.mchtChiName = getValue("MCHT_CHI_NAME");
			bilFiscdtl.mchtCity = getValue("MCHT_CITY");
			bilFiscdtl.mchtCountry = getValue("MCHT_COUNTRY");
			bilFiscdtl.mchtEngName = getValue("MCHT_ENG_NAME");
			bilFiscdtl.mchtNo = getValue("MCHT_NO");
			bilFiscdtl.mchtState = getValue("MCHT_STATE");
			bilFiscdtl.mchtZip = getValue("MCHT_ZIP");
			bilFiscdtl.mediaName = getValue("MEDIA_NAME");
			bilFiscdtl.messageText = getValue("MESSAGE_TEXT");
			bilFiscdtl.modPgm = getValue("MOD_PGM");
			bilFiscdtl.modTime = getValue("MOD_TIME");
			bilFiscdtl.modUser = getValue("MOD_USER");
			bilFiscdtl.mutiClearingSeq = getValue("MUTI_CLEARING_SEQ");
			bilFiscdtl.ncccBillType = getValue("NCCC_BILL_TYPE");
			bilFiscdtl.ncccItem = getValue("NCCC_ITEM");
			bilFiscdtl.ncccMchtType = getValue("NCCC_MCHT_TYPE");
			bilFiscdtl.onusOrderNo = getValue("ONUS_ORDER_NO");
			bilFiscdtl.onusTerNo = getValue("ONUS_TER_NO");
			bilFiscdtl.origTxAmt = getValue("ORIG_TX_AMT");
			bilFiscdtl.origTxType = getValue("ORIG_TX_TYPE");
			bilFiscdtl.panSeqNum = getValue("PAN_SEQ_NUM");
			bilFiscdtl.panToken = getValue("PAN_TOKEN");
			bilFiscdtl.par = getValue("PAR");
			bilFiscdtl.paymentFaId = getValue("PAYMENT_FA_ID");
			bilFiscdtl.paymentType = getValue("PAYMENT_TYPE");
			bilFiscdtl.pcasInd = getValue("PCAS_IND");
			bilFiscdtl.posEntryMode = getValue("POS_ENTRY_MODE");
			bilFiscdtl.posEnvironment = getValue("POS_ENVIRONMENT");
			bilFiscdtl.posTeCap = getValue("POS_TE_CAP");
			bilFiscdtl.postIssueResult = getValue("POST_ISSUE_RESULT");
			bilFiscdtl.prepaidCardInd = getValue("PREPAID_CARD_IND");
			bilFiscdtl.processDay = getValue("PROCESS_DAY");
			bilFiscdtl.procureBankFee = getValue("PROCURE_BANK_FEE");
			bilFiscdtl.procureChtFee = getValue("PROCURE_CHT_FEE");
			bilFiscdtl.procureLevel1 = getValue("PROCURE_LEVEL_1");
			bilFiscdtl.procureLevel2 = getValue("PROCURE_LEVEL_2");
			bilFiscdtl.procureName = getValue("PROCURE_NAME");
			bilFiscdtl.procureOrigAmt = getValue("PROCURE_ORIG_AMT");
			bilFiscdtl.procureOrigCurr = getValue("PROCURE_ORIG_CURR");
			bilFiscdtl.procurePayAmt = getValue("PROCURE_PAY_AMT");
			bilFiscdtl.procurePlan = getValue("PROCURE_PLAN");
			bilFiscdtl.procureReceiptNo = getValue("PROCURE_RECEIPT_NO");
			bilFiscdtl.procureTotTerm = getValue("PROCURE_TOT_TERM");
			bilFiscdtl.procureTxNum = getValue("PROCURE_TX_NUM");
			bilFiscdtl.procureUniform = getValue("PROCURE_UNIFORM");
			bilFiscdtl.procureVoucherNo = getValue("PROCURE_VOUCHER_NO");
			bilFiscdtl.progName = getValue("PROG_NAME");
			bilFiscdtl.purchaseDate = getValue("PURCHASE_DATE");
			bilFiscdtl.purchaseTime = getValue("PURCHASE_TIME");
			bilFiscdtl.qpsCbInd = getValue("QPS_CB_IND");
			bilFiscdtl.reasonCode = getValue("REASON_CODE");
			bilFiscdtl.reimbAttr = getValue("REIMB_ATTR");
			bilFiscdtl.reimbCode = getValue("REIMB_CODE");
			bilFiscdtl.reimbInfo = getValue("REIMB_INFO");
			bilFiscdtl.retrievalReqId = getValue("RETRIEVAL_REQ_ID");
			bilFiscdtl.rtnReasonCode = getValue("RTN_REASON_CODE");
			bilFiscdtl.scToBcRate = getValue("SC_TO_BC_RATE");
			bilFiscdtl.serviceCode = getValue("SERVICE_CODE");
			bilFiscdtl.setlAmt = getValue("SETL_AMT");
			bilFiscdtl.setlCurr = getValue("SETL_CURR");
			bilFiscdtl.setlRate = getValue("SETL_RATE");
			bilFiscdtl.settlFlag = getValue("SETTL_FLAG");
			bilFiscdtl.sourceAmt = getValue("SOURCE_AMT");
			bilFiscdtl.sourceBin = getValue("SOURCE_BIN");
			bilFiscdtl.sourceCurr = getValue("SOURCE_CURR");
			bilFiscdtl.specialCondInd = getValue("SPECIAL_COND_IND");
			bilFiscdtl.submerchantId = getValue("SUBMERCHANT_ID");
			bilFiscdtl.teBatchNo = getValue("TE_BATCH_NO");
			bilFiscdtl.teCountry = getValue("TE_COUNTRY");
			bilFiscdtl.teEntryCap = getValue("TE_ENTRY_CAP");
			bilFiscdtl.teProfile = getValue("TE_PROFILE");
			bilFiscdtl.teSendDate = getValue("TE_SEND_DATE");
			bilFiscdtl.teTxCurr = getValue("TE_TX_CURR");
			bilFiscdtl.teTxDate = getValue("TE_TX_DATE");
			bilFiscdtl.teTxNum = getValue("TE_TX_NUM");
			bilFiscdtl.terminalId = getValue("TERMINAL_ID");
			bilFiscdtl.tokenAssureLevel = getValue("TOKEN_ASSURE_LEVEL");
			bilFiscdtl.tokenRequestorId = getValue("TOKEN_REQUESTOR_ID");
			bilFiscdtl.transactionId = getValue("TRANSACTION_ID");
			bilFiscdtl.tvr = getValue("TVR");
			bilFiscdtl.ucaf = getValue("UCAF");
			bilFiscdtl.usageCode = getValue("USAGE_CODE");
			bilFiscdtl.vcind = getValue("VCIND");
			bilFiscdtl.vcrfsInd = getValue("VCRFS_IND");
			bilFiscdtl.vrolCaseNum = getValue("VROL_CASE_NUM");
			
			//共同供應契約的中文特店名稱要特別處理
			if ("".equals(bilFiscdtl.mchtChiName) == true && "".equals(bilFiscdtl.procureName)== false ) {
				bilFiscdtl.mchtChiName = bilFiscdtl.procureName;
			}
			
			return bilFiscdtl;
		} catch (Exception e) {
			throw new Exception("取得BilFiscdtl資料錯誤");
		}
	}
	
    private String julDate(String juliYddd) throws Exception  {
        String hJuliYddd;
        String hWestDate;
        String westDate = "";

        hJuliYddd = String.format("%4.4s", juliYddd);
        sqlCmd = "select case when  to_date(?,'yddd') > sysdate"
                + " then  to_char(add_months(to_date(?,'yddd'),-120),'yyyymmdd')"
                + " else  to_char(to_date(?,'yddd'),'yyyymmdd') end as h_west_date FROM   DUAL";
        setString(1, hJuliYddd);
        setString(2, hJuliYddd);
        setString(3, hJuliYddd);
        try {
        	selectTable();
            hWestDate = getValue("h_west_date");
            westDate = String.format("%8.8s", hWestDate);
        } catch (Exception e){
        	
        	sqlCmd="";
        	showLogMessage("E", "", "Conver julDate Error: ["+ juliYddd +"]");
        	westDate = sysDate;
        }

        return westDate;
    }

	/**
	 * select batch_seq from bil_postcntl
	 * and then return  batch number
	 * @param busiDate
	 * @return batch number
	 * @throws Exception
	 */
	private String getBatchNo(String busiDate) throws Exception {
		String batchNo = "";   // batch number
		String billUnit = "FI";
		
		selectSQL = "substr(to_char(nvl(max(batch_seq),0) + 1,'0000'),2,4) as batch_seq ";
        daoTable = " bil_postcntl ";
        whereStr  = " where batch_unit =  ? "
        	                    + " and batch_date = ? " ;   
        setString(1, billUnit);
        setString(2, busiDate);
        
		if (selectTable() <= 0) {
			throw new Exception("select from bil_postcntl where batch_unit ='FI' and batch_date = busiDate：查無資料");
		}else {
			batchNo =  busiDate + "FI" + getValue("batch_seq");
		}

		// return batch number
		return batchNo;
		
	}

	/**
	 * 讀取ptr_billunit判斷bill_unit ='FI'是否存在,並回傳conf_flag是否需主管放行的註記
	 * @return String:覆核註記
	 * @throws Exception
	 */
	private String getConfFlagFromPtrBillunit() throws Exception {
        selectSQL = "conf_flag ";
        daoTable = " ptr_billunit ";
        whereStr  = " where 1=1 "
        	                    + " and bill_unit ='FI' " ;   
		
		if (selectTable() <= 0) {
			throw new Exception("select from ptr_billunit where bill_unit ='FI'：查無資料");
		}else {
			return getValue("conf_flag");
		}
		
	}

	private void setCurpostFromBilFiscdtl(Curpost curpost, BilFiscdtl bilFiscdtl) {
		curpost.fiscTxnCode = bilFiscdtl.fiscTxCode;
		curpost.destAmt = bilFiscdtl.destAmt;
		curpost.acqMemberId = bilFiscdtl.acqBusinessId;
		curpost.authCode = bilFiscdtl.authCode;
		curpost.cashPayAmt = bilFiscdtl.bonusPayCash;
		curpost.deductBp = bilFiscdtl.bonusTransBp;
		curpost.ecInd = bilFiscdtl.ecInd;
		curpost.billType = bilFiscdtl.ecsBillType;
		curpost.cusMchtNo = bilFiscdtl.ecsCusMchtNo;
		curpost.platformKind = bilFiscdtl.ecsPlatformKind;
		curpost.cardNo = bilFiscdtl.ecsRealCardNo;
		curpost.referenceNo = bilFiscdtl.ecsReferenceNo;
		curpost.signFlag = bilFiscdtl.ecsSignCode;
		curpost.txnCode = bilFiscdtl.ecsTxCode;
		curpost.filmNo = bilFiscdtl.filmNo;
		curpost.amtIccr = bilFiscdtl.iccr;
		curpost.installFee = bilFiscdtl.installCharges;
		curpost.installFirstAmt = bilFiscdtl.installFirstAmt;
		curpost.installPerAmt = bilFiscdtl.installPerAmt;
		curpost.installTotTerm = bilFiscdtl.installTotTerm;
		curpost.amtMccr = bilFiscdtl.mCca;
		curpost.mchtCategory = bilFiscdtl.mccCode;
		curpost.mchtChiName = bilFiscdtl.mchtChiName;
		
		curpost.mchtCity = bilFiscdtl.mchtCity;
		curpost.mchtCountry = bilFiscdtl.mchtCountry;
		curpost.mchtEngName = bilFiscdtl.mchtEngName;
		curpost.mchtNo = bilFiscdtl.mchtNo;
		curpost.mchtState = bilFiscdtl.mchtState;
		curpost.mchtZip = bilFiscdtl.mchtZip;
		curpost.mcsNum = bilFiscdtl.mutiClearingSeq;
		curpost.mchtType = bilFiscdtl.ncccMchtType;
		curpost.paymentType = bilFiscdtl.paymentType;
		curpost.posEntryMode = bilFiscdtl.posEntryMode;
		curpost.purchaseDate = bilFiscdtl.purchaseDate;
		curpost.settlAmt = bilFiscdtl.setlAmt;
		curpost.sourceAmt = bilFiscdtl.sourceAmt;
		curpost.sourceCurr = bilFiscdtl.sourceCurr;
		curpost.terminalId = bilFiscdtl.terminalId;
		curpost.ucaf = bilFiscdtl.ucaf;
		curpost.vCardNo = bilFiscdtl.ecsVCardNo;
		
	}

	private void setCurpostFromPtrActcode(Curpost curpost) throws Exception {
		curpost.acctCode = getValue("acct_code");
		curpost.acctChiShortName = getValue("chi_short_name");
		curpost.acctEngShortName = getValue("eng_short_name");
		curpost.itemOrderNormal = getValue("item_order_normal");
		curpost.itemOrderBackDate = getValue("item_order_back_date");
		curpost.itemOrderRefund = getValue("item_order_refund");
		curpost.itemClassNormal = getValue("item_class_normal");
		curpost.itemClassBackDate = getValue("item_class_back_date");
		curpost.itemClassRefund = getValue("item_class_refund");
		
	}

	private void setCurpostFromPtrBilltype(Curpost curpost, String ecsDebitFlag) throws Exception {
		curpost.billType = getValue("bill_type");
		curpost.txnCode = getValue("txn_code");
		curpost.signFlag = getValue("sign_flag");
		curpost.acctCode = getValue("acct_code");
		curpost.acctItem = getValue("acct_item");
		curpost.feesState = getValue("fees_state");
		curpost.acexterDesc = getValue("exter_desc");
		curpost.interestDate = getValue("interest_mode");
	    // h_bity_collection_mode = getValue("collection_mode");
	    // h_bity_charge_back_month = getValue("charge_back_month");
		curpost.cashAdvState = getValue("cash_adv_state");
		curpost.entryAcct = getValue("entry_acct");
		curpost.errChkOkFlag = getValue("chk_err_bill");
		curpost.doubleChkOkFlag = getValue("double_chk");
		curpost.formatChkOkFlag = getValue("format_chk"); 
	    if (ecsDebitFlag.equalsIgnoreCase("Y")) {
	    	curpost.feesState = "N";
	        curpost.cashAdvState = "N";
	     }
		
	}

	private void setCurpostFromAcnoData(Curpost curpost) throws Exception {
		curpost.acctType         = getValue("acct_type");
		curpost.stmtCycle        = getValue("stmt_cycle");
		
	}

	private void setCurpostFromCardData(Curpost curpost) throws Exception {
		curpost.majorCardNo    = getValue("major_card_no");
		curpost.majorIdPSeqno = getValue("major_id_p_seqno");
		//curpost.currCode = getValue("current_code");  //2020/07/09 Jeff 
		curpost.issueDate   = getValue("issue_date");
		curpost.promoteDept  = getValue("promote_dept");
		curpost.prodNo      = getValue("prod_no");
		curpost.groupCode   = getValue("group_code");
		curpost.sourceCode  = getValue("source_code");
		curpost.binType     = getValue("bin_type");
		curpost.pSeqno = getValue("p_seqno");
		curpost.idPSeqno      = getValue("id_p_seqno");
		curpost.acnoPSeqno = getValue("acno_p_seqno");  //2020/7/9 Jeff
	}

	/**
	 * 設定SQL: 讀取bil_fiscdtl,條件為Batch_flag為空的請款交易且sort by fctl_no
	 * @throws Exception 
	 */
	private void setSqlSelectFromBilFiscdtl() throws Exception {
	    selectSQL = " * ";
	    daoTable = " bil_fiscdtl ";
	    whereStr = " where 1=1 "
	    		+ " and batch_date = '' "
	    		+ " and batch_flag = '' "
	    		+ " order by ecs_fctl_no " ;   
	
	}

	private boolean chkFromPtrBintable(String binNo) throws Exception {
		selectSQL = " bin_type ";
		daoTable = " ptr_bintable ";
		whereStr = " where 1=1 " 
		        + " and bin_no = ? " ;
		
		setString(1, binNo);
		
		 if (selectTable() <= 0) {
        	return false;
         }
		 
		 return true;
	}
	
	/**
	 *  若為負向交易，則負向交易總額 + curpost.destAmt
	 *  反之若為正向交易，則正向交易總額 + curpost.destAmt
	 * @param totalAmount
	 * @param destAmt
	 * @param ecsSignCode
	 * @return
	 */
	private BigDecimal computeTotalAmount(BigDecimal totalAmount, Double destAmt, String ecsSignCode) {
		
		if(ecsSignCode.equals("-")) {
			return totalAmount.subtract(new BigDecimal(destAmt.toString())) ;
		}else {
			return totalAmount.add(new BigDecimal(destAmt.toString())) ;
		}
	}

	/**
	 * 計算國外手續費= foreignFee + markupFee
	 * @param bilFiscdtl
	 * @param destAmt 
	 * @throws Exception 
	 */
	private ForeignFeeData computeForeignFee(BilFiscdtl bilFiscdtl, Double destAmt) throws Exception {
		ForeignFeeData foreignFeeData = commTxBill.new ForeignFeeData();
		Double markUpFee = 0.0;
		
		// 若為負向交易，則不收手續費
		// 否則計算海外交易手續費
		if(  ! bilFiscdtl.ecsTxCode.equals("05") && ! bilFiscdtl.ecsTxCode.equals("07")  ) {
			foreignFeeData.setForeignFee(0.0);	
		}else {
			foreignFeeData = commTxBill.getForeignFeeData(bilFiscdtl.ecsBinType, bilFiscdtl.mchtCountry,
					bilFiscdtl.destCurr, bilFiscdtl.sourceCurr, bilFiscdtl.ecsDebitFlag, bilFiscdtl.ecsDcCurr, destAmt,
					bilFiscdtl.destAmt );
		}
		
		// 若此筆交易為VD的國外ATM預借現金(特店國家<> TW && debitFlag = Y && 轉換過後的交易別in(07,09,27,29))，
		// 則foreign_fee要再加上 markupFee
		if (   !bilFiscdtl.mchtCountry.equalsIgnoreCase("TW") && 
			     bilFiscdtl.ecsDebitFlag.equalsIgnoreCase("Y") &&
			    (bilFiscdtl.ecsTxCode.equals("07") || 
				 bilFiscdtl.ecsTxCode.equals("09") || 
				 bilFiscdtl.ecsTxCode.equals("27") || 
				 bilFiscdtl.ecsTxCode.equals("29") ) ) {
			markUpFee = commTxBill.getVD09Fee(destAmt);
			foreignFeeData.setForeignFee(foreignFeeData.getForeignFee() + markUpFee);
			}
		
		return foreignFeeData;
		
	}

	/**
	 * 將原來的雙幣卡金額(請款檔destinationAmount)放到dc_amt，再將雙幣卡金額換算成台幣放入destAmt
	 * @param dualCurrencyCode 雙幣卡清算幣別
	 * @param destinationAmount 當地金額
	 * @throws Exception
	 */
	private Curpost computeTWDByDualCurrencyAndSetCurpost(Curpost curpost, String dualCurrencyCode, Double destinationAmount) throws Exception {
		Double dualCurrencyAmount = destinationAmount;
		Double exchangeRate = commTxBill.getDCExchangeRate(dualCurrencyCode);
	
		curpost.dcExchangeRate = exchangeRate;
		
		curpost.dcDestAmt = dualCurrencyAmount; // 將原來的雙幣卡金額放到dc_amt
		
		curpost.destAmt = commTxBill.round(exchangeRate * destinationAmount, 0); // 用原來的雙幣卡金額換算為台幣金額
		curpost.destCurr = "901";  //台幣
		curpost.currCode = dualCurrencyCode; //雙幣的原幣別
	
		return curpost;
	}

	/**
	 * 為原始交易
	 * 即交易別ECS_TX_CODE  in (05,06,07,08,09)且沖正碼為空白
	 * @param chargebackCode
	 * @param transactionCode
	 * @return
	 */
	private boolean isOriginalTransaction(String chargebackCode, String transactionCode) {
		return chargebackCode.isEmpty() && 
		(       transactionCode.equals("05") || 
				transactionCode.equals("06") ||
				transactionCode.equals("07") ||
				transactionCode.equals("08") ||
				transactionCode.equals("09")  
		);
	}

	/**
	 * 為原始更正交易
	 * 即交易別ECS_TX_CODE  in (25,26,27,28,29)且沖正碼為空白
	 * @param chargebackCode
	 * @param transactionCode
	 * @return
	 */
	private boolean isReversalTransaction(String chargebackCode, String transactionCode) {
		return chargebackCode.isEmpty() &&
		(       
				transactionCode.equals("25") || 
				transactionCode.equals("26") || 
				transactionCode.equals("27") || 
				transactionCode.equals("28") || 
				transactionCode.equals("29") 
		);
	}

	private boolean isDesinationCurrencyTaiwan(String destinatinoCurrency) {
		return destinatinoCurrency.equalsIgnoreCase("TWD") ||  destinatinoCurrency.equalsIgnoreCase("901");
	}

	private boolean isForeignFeeGreaterThanZero(ForeignFeeData foreignFeeData) {
		return foreignFeeData.getForeignFee() > 0 && foreignFeeData.getDcForeignFee() > 0;
	}

	public static void main(String[] args) {
		BilE191 proc = new BilE191();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}
	
	/**
	 * FISC媒體明細檔
	 */
	class BilFiscdtl{
		String ac = "";   //交易驗證資料(AC)
		String acCryptoAmt = "";   //產生AC時的授權金額
		String acTxType = "";   //產生AC時的交易類別
		String acUnpredNum = "";   //產生AC時所需亂數
		String acctNumType = "";   //Account Number Type
		String acqBusinessId = "";   //代理單位代號
		String acqRespCode = "";   //代理單位回覆碼
		String addnAcctType = "";   //Additional Amount,Account Type
		String addnAmt = "";   //ADDN/Surcharge/找回現金(V:整數10位,小數二位;M/J:依幣別決定小數位)
		String addnAmtCurr = "";   //Additional Amount 幣別
		String addnAmtSign = "";   //ADDN/Surcharge正負Sign
		String addnBillCurr = "";   //ADDN/Surcharge(持卡人billing幣別)
		String apExpireDate = "";   //晶片卡應用程式失效日期(YYYYMMDD)
		String apProfile = "";   //卡片應用程式功能表
		String apTxNum = "";   //卡片交易序號
		String authCode = "";   //授權碼
		String authRespCode = "";   //授權回應碼(含離線回應碼)
		String authValidCode = "";   //授權欄位驗證碼
		String batchDate = "";   //入帳日期
		String batchFlag = "";   //程式處理註記
		String batchNo = "";   //入帳批號
		String bcToDcRate = "";   //Base幣別對原始幣別匯率
		String bonusPayCash = "";   //紅利折抵後之支付金額
		String bonusTransBp = "";   //紅利折抵點數
		String busFormatCode = "";   //Bussiness Format Code
		String cardNo = "";   //卡號
		String cardPlan = "";   //卡片類型(V/M/J/E)
		String cardProductId = "";   //卡片級別識別碼
		String cardVerfyResult = "";   //Card 紀錄所有交易行為
		String catInd = "";   //自助端末機識別碼
		String cbRefNo = "";   //沖正參考號碼/跨境支付手續費整數8位小數2位
		String chargeInd = "";   //Charge Indicator
		String chipCondCode = "";   //Chip condition code
		String crossPayCode = "";   //跨境電子支付平台代碼
		String crossPayStatus = "";   //跨境電子支付平台交易狀態
		String crossPayType = "";   //跨境電子支付申報性質別
		String cryptogramInfo = "";   //回傳Approve/Online/Denied的type
		String cvmResult = "";   //持卡人認證結果
		String cwbInd = "";   //黑名單識別碼
		String dccInd = "";   //DCC識別碼
		String de22CaptureCap = "";   //DE22:Card Capture Capability
		String de22CardData = "";   //DE22:Card present data
		String de22CardInCap = "";   //DE22:Card Data Input Capability
		String de22CardOutCap = "";   //DE22:Card Data OUT Capability
		String de22ChAuthCap = "";   //DE22:持卡人認證Capability
		String de22ChAuthEntity = "";   //DE22:持卡人認證Entity
		String de22ChAuthMethod = "";   //DE22:持卡人認證方式
		String de22ChData = "";   //DE22:持卡人present data
		String de22InputMode = "";   //DE22:Input Mode
		String de22PinCaptureCap = "";   //DE22:PIN Capture Capability
		String de22TeOpEnv = "";   //DE22:端末機OP Environment
		String de22TeOutCap = "";   //DE22:端末機資料OUT Capability
		Double destAmt = 0.0;   //當地金額
		String destBin = "";   //接收端BIN/ICA
		String destCurr = "";   //Destination curr
		String docInd = "";   //附寄文件識別碼
		String ecInd = "";   //MAIL/PHONE ECI識別碼
		String ecsAcctCode = "";   //帳務科目
		String ecsBillType = "";   //帳單類別
		String ecsBinType = "";   //國際組織卡別
		String ecsCbCode = "";   //沖正碼(1:第1次,2:第二次,其他:空白)
		String ecsCusMchtNo = "";   //客制特店代號(依bil_platform判別,不符合的則為空白)
		String ecsDcCurr = "";   //雙幣卡清算幣別
		String ecsDcFlag = "";   //是否為雙幣卡(Y/N)
		String ecsDebitFlag = "";   //是否為Debit卡(Y/N)
		String ecsFctlNo = "";   //批次號碼
		String ecsPlatformKind = "";   //交易平台種類(依bil_platform判別,不符合的則為空白)
		String ecsRealCardNo = "";   //實體卡號
		String ecsReferenceNo = "";   //帳單流水號
		String ecsSignCode = "";   //正負向(+/-)
		String ecsTxChiName = "";   //交易種類中文名稱
		String ecsTxCode = "";   //ECS交易別
		String ecsTxEngName = "";   //交易種類英文名稱
		String ecsVCardNo = "";   //虛擬卡號
		String emvAcqData = "";   //收單相關資料
		String emvAcqTeId = "";   //收單端末機代號
		String emvBankNum = "";   //參加單位代號
		String emvCashbackAmt = "";   //找回現金(V:小數二位,M:依幣別決定小數位)
		String emvCashbackAmt2 = "";   //must be all zeros
		String emvErrorCode = "";   //錯誤回覆碼
		String emvErrorCode2 = "";   //錯誤回覆碼(Record2)
		String emvIad2 = "";   //發卡行產生AC的資訊2
		String emvInputSeq = "";   //輸入序號(財金編流水號)
		String emvJcbCbDate = "";   //JCB_更正/沖正/駁回日期
		String emvJcbChequeFlag = "";   //JCB_旅行支票標示
		String emvJcbDestCurr = "";   //JCB_當地幣別
		String emvJcbExRate = "";   //JCB_交換匯率
		String emvJcbInstallTerm = "";   //JCB_分期付款期數
		String emvJcbMchtNo = "";   //JCB_特店代號
		String emvJcbMchtState = "";   //JCB_特店省份
		String emvJcbReasonCode = "";   //JCB_沖正/駁回理由碼
		String emvJcbTeId = "";   //JCB_端末機代碼
		String emvLocalDate = "";   //當地接收日期(西元YYYYMMDD)
		String emvLocalTime = "";   //當地接收時間(hhmmss)
		String emvReversalInd = "";   //更正識別碼
		String eventDate = "";   //費用發生日
		String feeCtrlNum = "";   //費用控制碼
		String feeReasonCode = "";   //費用/訊息原因碼
		String ffi = "";   //識別交易載具
		String filmNo = "";   //微縮影編號
		String fiscTxCode = "";   //財金交易代號
		String floorLimitInd = "";   //特店限額識別碼
		String fpi = "";   //Fee Program Indicator
		String fraudCbCnt = "";   //偽冒卡chargeback次數
		String fraudNotifyDate = "";   //FRAUD NOTIFICATION DATE(YDDD)
		String fxjExDate = "";   //JCB_交換日期(YYYYMMDD)
		String fxjTeSource = "";   //端末來源代碼
		String iad = "";   //發卡行產生AC的資訊
		String iccr = "";   //Issuer 幣別轉換匯率
		String installAtmNo = "";   //分期付款:櫃員機台代碼
		String installCharges = "";   //分期付款:分期管理費
		String installFirstAmt = "";   //分期付款:首期金額
		String installLastAmt = "";   //分期付款:末期金額
		String installPerAmt = "";   //分期付款:每期金額
		String installProjNo = "";   //分期付款:專案代號
		String installSupplyNo = "";   //分期付款:來源供應商代碼
		String installTotTerm = "";   //分期付款:分期期數
		String installType = "";   //分期付款:交易類型
		String interfaceDevNum = "";   //端末機唯一序號
		String interfaceTraceNum = "";   //介面追蹤號碼
		String ird = "";   //Interchange Rate Designator
		String issueCtrlNum = "";   //發卡單位控制碼
		String mCca = "";   //M/C CCA
		String mCrossInd = "";   //M/C Cross-Border Indicator
		String mCurrInd = "";   //M/C 幣別 Indicator
		String mccCode = "";   //特店行業別
		String mchtChiName = "";   //特店中文名稱
		String mchtCity = "";   //特店city
		String mchtCountry = "";   //特店國家代號
		String mchtEngName = "";   //特店英文名稱
		String mchtNo = "";   //特店代號
		String mchtState = "";   //特店省份
		String mchtZip = "";   //特店ZIP
		String mediaName = "";   //媒體檔名
		String messageText = "";   //訊息(若FXG:則為原訊息/特名)
		String modPgm = "";   //異動程式
		String modTime = "";   //異動時間
		String modUser = "";   //異動使用者
		String mutiClearingSeq = "";   //多筆清算交易對應序號
		String ncccBillType = "";   //NCCC ON-US繳費平台帳單類別
		String ncccItem = "";   //NCCC ON-US繳費平台繳費項目
		String ncccMchtType = "";   //NCCC ON-US繳費平台特店類型
		String onusOrderNo = "";   //自行交易訂單號碼
		String onusTerNo = "";   //自行交易16碼端末機代號
		String origTxAmt = "";   //原始交易金額
		String origTxType = "";   //交易退回時原交易代號
		String panSeqNum = "";   //晶片卡卡片序號
		String panToken = "";   //PAN TOKEN
		String par = "";   //Payment Account Ref.
		String paymentFaId = "";   //Payment Facilitator ID
		String paymentType = "";   //支付型態
		String pcasInd = "";   //PCAS識別碼
		String posEntryMode = "";   //POS輸入型態
		String posEnvironment = "";   //POS Environment
		String posTeCap = "";   //POS端末機性能
		String postIssueResult = "";   //POST ISSUANCE結果
		String prepaidCardInd = "";   //購買預付卡識別碼(Only VISA)
		String processDay = "";   //VISA/Master處理日(西元YDDD)
		String procureBankFee = "";   //共同供應契約:銀行手續費
		String procureChtFee = "";   //共同供應契約:中華電信手續費
		String procureLevel1 = "";   //共同供應契約:一級用途別
		String procureLevel2 = "";   //共同供應契約:二級用途別
		String procureName = "";   //共同供應契約:立約商名稱
		String procureOrigAmt = "";   //共同供應契約:原始金額
		String procureOrigCurr = "";   //共同供應契約:原始幣別
		String procurePayAmt = "";   //共同供應契約:撥付金額
		String procurePlan = "";   //共同供應契約:工作計劃/購案編號
		String procureReceiptNo = "";   //共同供應契約:發票號碼
		String procureTotTerm = "";   //共同供應契約:訂單支付期數
		String procureTxNum = "";   //共同供應契約:交易編號
		String procureUniform = "";   //共同供應契約:立約商統編
		String procureVoucherNo = "";   //共同供應契約:傳票號碼
		String progName = "";   //入帳程式
		String purchaseDate = "";   //購貨日期(西元YYYYMMDD)
		String purchaseTime = "";   //購貨時間(HHMMSS)
		String qpsCbInd = "";   //QPS/PayPass交易後續是否可沖正
		String reasonCode = "";   //沖正駁回/調單理由碼
		String reimbAttr = "";   //交易處理費屬性
		String reimbCode = "";   //繳費平台/繳稅種類代碼
		String reimbInfo = "";   //跨境/繳費平台/繳稅交易資訊
		String retrievalReqId = "";   //Retrieval Request ID
		String rtnReasonCode = "";   //交易退回理由碼
		String scToBcRate = "";   //原始幣別對Base幣別匯率
		String serviceCode = "";   //Service Code
		String setlAmt = "";   //清算金額
		String setlCurr = "";   //清算幣別
		String setlRate = "";   //清算匯率
		String settlFlag = "";   //清算識別碼
		String sourceAmt = "";   //購買地金額
		String sourceBin = "";   //傳送端BIN/ICA
		String sourceCurr = "";   //購買地幣別
		String specialCondInd = "";   //特殊條件識別碼
		String submerchantId = "";   //次特約商代碼
		String teBatchNo = "";   //端末機上傳資料批次號碼
		String teCountry = "";   //端末機國別碼
		String teEntryCap = "";   //端末機功能(0:Non-EMV,5:EMV)
		String teProfile = "";   //端末機功能表
		String teSendDate = "";   //端末機傳送資料日期(西元YYYYMMDD)
		String teTxCurr = "";   //端末機交易幣別碼
		String teTxDate = "";   //端末機交易日期(YYYYMMDD)
		String teTxNum = "";   //端末機交易序號
		String terminalId = "";   //端末機代碼
		String tokenAssureLevel = "";   //Token Assurance Level
		String tokenRequestorId = "";   //Token Requestor ID
		String transactionId = "";   //交易識別碼
		String tvr = "";   //端末機所有交易行為
		String ucaf = "";   //UCAF
		String usageCode = "";   //使用碼
		String vcind = "";   //VCIND
		String vcrfsInd = "";   //特殊沖正/VCRFS識別碼
		String vrolCaseNum = "";   //VROL Case Number

	}
	
	class Curpost implements Cloneable{
		
		String acctChiShortName = "";   //科目中文名稱                  
		String acctCode = "";   //對應科目                      
		String acctEngShortName = "";   //科目英文名稱                  
		String acctItem = "";   //會計科目                      
		String acctType = "";   //帳戶帳號類別碼                
		String acctitemConvtFlag = "";   //科目轉換旗標                  
		String acexterDesc = "";   //外部說明                      
		String acqMemberId = "";   //清算會員id                    
		String acquireDate = "";   //清算日期                      
		String authCode = "";   //授權碼                        
		String batchNo = "";   //批號                          
		String billType = "";   //帳單類別                      
		String binType = "";   //卡別                          
		String cardNo = "";   //消費卡號                      
		String cashAdvState = "";   //預借現金                      
		String cashPayAmt = "";   //實 付金額                     
		String collectionMode = "";   //託收性質                      
		String contractAmt = "";   //合約總金額                    
		String contractNo = "";   //合約編號                      
		String contractSeqNo = "";   //合約序號sub                   
		String currCode = "";   //幣別                          
		String currPostFlag = "";   //當日入帳旗標                  
		String currTxAmount = "";   //異動金額                      
		String cusMchtNo = "";   //客制特店代號(依bil_platform判別,不符合的則為空白)
		Double  dcExchangeRate = 0.0;//外幣對台幣匯率                
		String deductBp = "";   //折抵點數                      
		Double destAmt = 0.0;   //目的地金額                    
		String destCurr = "";   //目的地幣別                    
		String doubleChkOkFlag = "";   //重覆查核旗標                  
		String doubtType = "";   //疑異原因碼                    
		String duplicatedFlag = "";   //重覆旗標                      
		String ecInd = "";   //mail_phone_ec 辨別碼          
		String electronicTermInd = "";   //edc辨別碼                     
		String errChkOkFlag = "";   //疑異查核旗標                  
		String expirDate = "";   //到期日                        
		String feesReferenceNo = "";   //手續費帳單流水號碼            
		String feesState = "";   //手續費收取                    
		String filmNo = "";   //微縮影編號                    
		String fiscTxnCode = "";   //財金交易代號
		String formatChkOkFlag = "";   //格式查核旗標                  
		String groupCode = "";   //團體代號                      
		String idPSeqno = "";   //卡人流水號碼                  
		String includeFeeAmt = "";   //包含服務金額                  
		String installFee = "";   //分期付款手續費                
		String installFirstAmt = "";   //分期付款首期金額              
		String installPerAmt = "";   //分期付款每期金額              
		String installTotTerm = "";   //分期付款總期數1               
		String installmentKind = "";   //分期付款種類                  
		String installmentSource = "";   //分期付款來源 C:cps ,N:Nccc, I:Icbc 
		String issueDate = "";   //發卡日期                      
		String issueFee = "";   //自行附加設定費                
		String itemClassBackDate = "";   //科目沖銷類別_回饋             
		String itemClassNormal = "";   //科目沖銷類別_正常             
		String itemClassRefund = "";   //科目沖銷類別_退還             
		String itemOrderBackDate = "";   //科目沖銷順序_回饋             
		String itemOrderNormal = "";   //科目沖銷順序_正常             
		String itemOrderRefund = "";   //科目沖銷順序_退還             
		String majorCardNo = "";   //正卡卡號                      
		String majorIdPSeqno = "";   //正卡人流水號                  
		String manualUpdFlag = "";   //人工更改旗標                  
		String mchtCategory = "";   //特店業別                      
		String mchtChiName = "";   //特店中文名稱                  
		String mchtCity = "";   //特店城市                      
		String mchtCountry = "";   //特店國別                      
		String mchtEngName = "";   //特店英文名稱                  
		String mchtNo = "";   //特店代號                      
		String mchtState = "";   //特店州別                      
		String mchtType = "";   //特店類別                      
		String mchtZip = "";   //特店郵區                      
		String mchtZipTw = "";   //特店郵區-台灣                 
		String mcsCnt = "";   //MCS_CNT                       
		String mcsNum = "";   //MCS_NUM                       
		String mergeFlag = "";   //合併註記                      
		String modLog = "";   //LOG註記                       
		String modPgm = "";   //異動程式                      
		String modSeqno = "";   //異動註記                      
		String modTime = "";   //異動時間                      
		String modUser = "";   //異動使用者                    
		String pSeqno = "";   //總繳戶流水號碼                
		String paymentType = "";   //付款方式                      
		String platformKind = "";   //交易平台種類(依bil_platform判別,不符合的則為空白)
		String posEntryMode = "";   //進入模式                      
		String processDate = "";   //處理日期                      
		String prodNo = "";   //推廣專案代號                  
		String promoteDept = "";   //推廣部門                      
		String ptrMerchantNo = "";   //分期付款特店代號              
		String purchaseDate = "";   //消費日期                      
		String referenceNo = "";   //帳單流水號碼                  
		String referenceNoFeeF = "";   //國外交易手續費帳單流水號碼    
		String referenceNoOriginal = "";   //原始帳單流水號碼              
		String rskType = "";   //帳單之風管疑異碼              
		String settlAmt = "";   //清算金額                      
		String signFlag = "";   //負項註記 +:正項 -:負項   
		String sourceAmt = "";   //原始金額                      
		String sourceCode = "";   //來源別                        
		String sourceCurr = "";   //原始幣別                      
		String stmtCycle = "";   //帳單週期                      
		String term = "";   //分期付款目前期數              
		String thisCloseDate = "";   //資料入帳日期                  
		String totalTerm = "";   //分期付款總期數                
		String txConvtFlag = "";   //風管轉移旗標                  
		String txnCode = "";   //交易碼                        
		String ucaf = "";   //網路交易授權代碼              
		String vCardNo = "";   //虛擬卡號                      
		String validFlag = "";   //Y:需取授權 W:送至授權 E:授權有誤 P:授權無誤 
		//=========BilCurpost==========
		String acnoPSeqno = "";  //帳戶流水號           
		String amtIccr = "";  //ICCR 金額            
		String amtMccr = "";  //MCCR 金額            
		String ccasDate = "";  //分期查核旗標         
		String ccasFlag = "";  //分期查核旗標         
		String contractFlag = "";  //分期查核旗標         
		Double dcAmount = 0.0;  //外幣清算幣別消費金額 
		String entryAcct = "";  //是否入帳             
		String postAmt = "";  //合約過帳總金額       
		String rskRsn = "";  //帳單之風管理由碼 
		String terminalId = "";   //Terminal ID  
		//========BilCurpost=========
		//========DbbCurpost========
		String acctMonth = "";   //帳務年月            
		String billedDate = "";   //帳單日期            
		String billedFlag = "";   //帳單通知旗標        
		String cashAdvFee = "";   //預付現金費用        
		String currAdjustAmt = "";   //未清算調整金額      
		String dcCurrAdjustAmt = "";   //外幣清算幣別調整金額
		Double dcDestAmt = 0.0;   //外幣清算幣別消費金額
		String interestDate = "";   //起息日              
		String oriAmt = "";   //原始金額            
		String postDate = "";   //入帳日期            
		String regBankNo = "";   //受理行              
		String rskTypeSpecial = "";   //帳單之風管特殊疑異碼
		//========DbbCurpost========
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}
	}
}
