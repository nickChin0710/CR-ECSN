/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/09  V1.00.00    JustinWu     program initial                          *
*  109/03/13  V1.00.01    JustinWu     add convertStrDateFormat
*  109/03/19  V1.00.02    JustinWu     modify getCodeName: fiscTxCodeInt, getPlatformDataByFISC, and FiscData  *
*  109/03/31  V1.00.03    JustinWu     extract the same methods used in BilE1XX, and add some objects and methods
*  109/04/21  V1.00.04    JustinWu     change the signature of convertStrDateFormat() and subByteToStr() to static
*  109/05/21  V1.00.05    JustinWu     modify the comments of some methods
*  109/05/22  V1.00.06    JustinWu     fix a bug of checkRepeatProcess method and modify the log
*  109/05/26  V1.00.07    JustinWu     add a new method getBigDecimalDivided
*  109/07/15  V1.00.08    JeffKung     for FiscPlatform assign to add bill_type='FISC' condition
*  109/09/14  V1.00.09    JeffKung     for bin_table notfound to showlog instead throw exceptions
*  109/09/16  V1.00.10    JeffKung     for reimb_attr can't compare in upper
*  110-01-07  V1.00.11    shiyuqi      修改无意义命名                                  
*  111-01-18  V1.00.12    Justin       fix Erroneous String Compare
*  111-02-15  V1.00.13    Justin       big5 -> MS950, and print error messages
*  111-09-22  V1.00.14    JeffKung     for TCB phase 3及財金二代帳務檔處理
*  111-11-28  V1.00.15    JeffKung     交易種類分類調整
*  112-03-19  V1.00.16    JeffKung     交易種類"10"問題調整
*  112-11-09  V1.00.17    JeffKung     海外交易手續費計算以BigDecimal轉換計算
*****************************************************************************/
package com;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.ArrayUtils;

public class CommTxBill extends AccessDAO{
	  private BigDecimal bigDecimal100 = new BigDecimal(100);
	  String[] DBNAME = new String[10];
	  boolean DEBUG = false; //若為true，則會print額外資訊
	  
	  /**
	   * 
	   * @param conn
	   * @param dbAlias
	   * @param debug 是否開啟debug功能
	   * @throws Exception
	   */
	 public CommTxBill(Connection conn[],String[] dbAlias, boolean debug) throws Exception
	 {
		 
	   super.conn  = conn;
	   setDBalias(dbAlias);
	   setSubParm(dbAlias);
	   DBNAME[0]=dbAlias[0];
	   DEBUG = debug;

	 }
	 
	 public CommTxBill(Connection conn[],String[] dbAlias) throws Exception
	 {
		 
	   super.conn  = conn;
	   setDBalias(dbAlias);
	   setSubParm(dbAlias);
	   DBNAME[0]=dbAlias[0];
	   DEBUG = false;

	 }
	
	 /***
	  *
	  * 是否為美元匯率交易
	  * @param transactionType 交易代號
	  * @return
	  */
	public boolean isCurrRateTx(String transactionType){
		return transactionType.equals("6256") || transactionType.equals("7256");
		
	}
	
	/**
	 * 判斷字串是否為空值
	 * @param str
	 * @return
	 */
	public boolean isEmpty(String str) {
		if (str == null) {
			return true;
		}
		if (str.trim().length() == 0) {
			return true;
		}

		return false;
	}

	/**
	 * 取得部分字串值
	 * @param str 處理目標字串
	 * @param beginIndex 字串位置起點
	 * @param endIndex 字串位置終點
	 * @return String 返回一個字串
	 */
	public String getSubString(String str, int beginIndex, int endIndex) {
		if (beginIndex < 0)
			return "";
		if (endIndex < 0)
			return "";
		if (beginIndex >= endIndex)
			return "";
		if (str.length() > beginIndex) {
			int eIndex = Math.min(endIndex, str.length());
			return str.substring(beginIndex, eIndex);
		}
		return "";
	}

	/**
	 * 取得部分字串值
	 * @param str 處理目標字串
	 * @param beginIndex 字串位置起點
	 * @return String 返回一個字串
	 */
	public String getSubString(String str, int beginIndex) {
		if (str.length() > beginIndex) {
			return str.substring(beginIndex);
		}
		return "";
	}

	/**
	 * 取得唯一的reference_no值
	 * @return
	 * @throws Exception
	 */
	public String getReferenceNo() throws Exception {
		String strX08 = "";
		String strX10 = "";
		dateTime();

        sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) as h_temp_x08 from dual";
        selectTable();
        if (notFound.equals("Y")) {
//        	showLogMessage("E", "", "select_bil_postcntl error" );
        	throw new Exception("select_bil_postcntl error");
        }
        strX08 = getValue("h_temp_x08");

        strX10 = String.format("%2.2s%s", getSubString(sysDate,2), strX08);
        
		return strX10;
	}
	
	/***
	 * 依據輸入的參數,取得TransactionInformation,以供判斷是何種交易
	 * @param fiscTxCode 請款檔內的交易代號
	 * @param usageCode 使用碼
	 * @param cardNo 財金請款卡號
	 * @return TransactionInformation
	 * @throws Exception
	 */
	private TransactionInformation getTransactionInformation(String fiscTxCode, String usageCode, String cardNo) throws Exception {
		TransactionInformation transactionInformation = new TransactionInformation();
		String txEngName = "", txChiName = "";
		String cbCode = "";
		String binType = "";
		String txCode = getSubString(fiscTxCode,2,4);
		String binNo = getSubString(cardNo,0,6);
		
		binType = getBinTypeFromPtrBintable(binNo);
		
		 int fiscTxCodeInt = Integer.parseInt(fiscTxCode);
		 
		 if ("10".equals(txCode) || "20".equals(txCode) || 
		     "51".equals(txCode) || "52".equals(txCode) ||
		     "50".equals(txCode) || "56".equals(txCode) ||
		     "01".equals(txCode) || "02".equals(txCode) || "03".equals(txCode)) {
		 
			 switch(getSubString(fiscTxCode,2,4)) {
		 		case "10":
		 			txEngName = "FEE_COLLECTION";
		 			txChiName = "費用收取(Fee Collection)";
		 			break;
		 		case "20":
		 			txEngName = "FUNDS_DISBURSEMENT";
		 			txChiName = "費用支出(Funds Disbursement)";
		 			break;
		 		case "51":
		 			txEngName = "REQUEST_FOR_ORIGINAL";
		 			txChiName = "調閱原始簽帳單(Request for Original)";
		 			break;
		 		case "52":
		 			txEngName = "REQUEST_FOR_COPY";
		 			txChiName = "調閱複印本簽帳單(Request for Copy)";
		 			break;
		 		case "56":
		 			txEngName = "USD_CONVERSION_REATE";
		 			txChiName = "美元匯率(Currency Conversion Rate)";
		 			break;
		 		case "50":
		 			txEngName = "TEXT_MESSAGE";
		 			txChiName = "訊息通知(Text Message)";
		 			break;
		 		case "01":
		 			txEngName = "RETURNED_CREDIT";
		 			txChiName = "入帳類交易退回(Returned Credit)";
		 			break;
		 		case "02":
		 			txEngName = "RETURNED_DEBIT";
		 			txChiName = "扣帳類交易退回(Returned Debit)";
		 			break;
		 		case "03":
		 			txEngName = "RETURN_NON_FINANCIAL";
		 			txChiName = "非帳務交易退回(Returned Non-Financial)";
		 			break;
			 }		
		 
			 transactionInformation.txCode = txCode;
			 transactionInformation.cbCode = cbCode;
			 transactionInformation.txEngName = txEngName;
			 transactionInformation.txChiName = txChiName;

			 return transactionInformation;
		 }
		
		 //真正要處理的帳務交易才往下走

		 //V,M usageCode =1 正常交易
		 //J usageCode =0 正常交易
		 //V,M usageCode =2 再提示或第二次沖正
		 //J usageCode =1 第一次再提示或第一次沖正
		 //J usageCode =2 第二次再提示或第二次沖正
		 if ( ("V".equalsIgnoreCase(binType) || "M".equalsIgnoreCase(binType) ) && usageCode.equals("1") ) {
			 cbCode = "";
		 } else if ( "J".equalsIgnoreCase(binType)  && usageCode.equals("0") ) {
			 cbCode = "";
		 } else if ( ("V".equalsIgnoreCase(binType) || "M".equalsIgnoreCase(binType) ) && usageCode.equals("2") ) {
			 cbCode = "1";
		 } else if ( "J".equalsIgnoreCase(binType)  && usageCode.equals("1") ) {
			 cbCode = "1";
		 } else if ( "J".equalsIgnoreCase(binType)  && usageCode.equals("2") ) {
			 cbCode = "2";
		 }
		 
		 if( "".equals(cbCode) ) {
			 switch(getSubString(fiscTxCode,2,4)) {
			 	case "05":
			 		txEngName = "SALE_DRAFT";
			 		txChiName = "購貨(含電話授權購貨簽帳單)";
			 		break;
				case "06":
					txEngName = "CREDIT_VOUCHER";
					txChiName = "退貨(含電話授權退貨簽帳單)";
					break;
				case "07":
					txEngName = "CASH_ADVANCE";
					txChiName = "預借現金(含銀行櫃台電話授權預借現金簽帳單)";
					break;
				case "25":
					txEngName = "SALE_REVERSAL";
					txChiName = "購貨原始更正";
					break;
				case "26":
					txEngName = "RFND_REVERSAL";
					txChiName = "退貨原始更正";
					break;
				case "27":
					txEngName = "CASH_REVERSAL";
					txChiName = "預借現金原始更正";
					break;
				case "15":
					txEngName = "SALE_CHARGEBACK1";
					txChiName = "購貨第一次沖正";
					break;
				case "16":
					txEngName = "RFND_CHARGEBACK1";
					txChiName = "退貨第一次沖正";
					break;
				case "17":
					txEngName = "CASH_CHARGEBACK1";
					txChiName = "預借現金第一次沖正";
					break;
			    case "35":
			    	txEngName = "SALE_CHARGEBACK1_REVERSAL";
			    	txChiName = "購貨第一次沖正更正";
			    	break;
				case "36":
					txEngName = "RFND_CHARGEBACK1_REVERSAL";
					txChiName = "退貨第一次沖正更正";
					break;
				case "37":
					txEngName = "CASH_CHARGEBACK1_REVERSAL";
					txChiName = "預借現金第一次沖正更正";
					break;
			 }
		 } else if( "1".equals(cbCode) ) {
			 switch(getSubString(fiscTxCode,2,4)) {
			 	case "05":
			 		txEngName = "SALE_DRAFT_REPRESENT1";
			 		txCode = "65";
			 		txChiName = "購貨沖正駁回(含電話授權購貨簽帳單)";
			 		break;
				case "06":
					txEngName = "CREDIT_VOUCHER_REPRESENT1";
					txCode = "66";
					txChiName = "退貨沖正駁回(含電話授權退貨簽帳單)";
					break;
				case "07":
					txEngName = "CASH_ADVANCE_REPRESENT1";
					txCode = "67";
					txChiName = "預借現金沖正駁回(含銀行櫃台電話授權預借現金簽帳單)";
					break;
				case "25":
					txEngName = "SALE_DRAFT_REPRESENT1_REVERSAL";
					txCode = "85";
					txChiName = "購貨沖正駁回更正";
					break;
				case "26":
					txEngName = "CREDIT_VOUCHER_REPRESENT1_REVERSAL";
					txCode = "86";
					txChiName = "退貨沖正駁回更正";
					break;
				case "27":
					txEngName = "CASH_ADVANCE_REPRESENT1_REVERSAL";
					txCode = "87";
					txChiName = "預借現金沖正駁回更正";
					break;

			 }
		 } else if( "2".equals(cbCode) ) {
			 switch(getSubString(fiscTxCode,2,4)) {
			 	case "05":
			 		txEngName = "SALE_DRAFT_REPRESENT2";
			 		txCode = "65";
			 		txChiName = "購貨第二次沖正駁回(含電話授權購貨簽帳單)";
			 		break;
				case "06":
					txEngName = "CREDIT_VOUCHER_REPRESENT2";
					txCode = "66";
					txChiName = "退貨第二次沖正駁回(含電話授權退貨簽帳單)";
					break;
				case "07":
					txEngName = "CASH_ADVANCE_REPRESENT2";
					txCode = "67";
					txChiName = "預借現金第二次沖正駁回(含銀行櫃台電話授權預借現金簽帳單)";
					break;
				case "25":
					txEngName = "SALE_DRAFT_REPRESENT2_REVERSAL";
					txCode = "85";
					txChiName = "購貨第二次沖正駁回更正";
					break;
				case "26":
					txEngName = "CREDIT_VOUCHER_REPRESENT2_REVERSAL";
					txCode = "86";
					txChiName = "退貨第二次沖正駁回更正";
					break;
				case "27":
					txEngName = "CASH_ADVANCE_REPRESENT2_REVERSAL";
					txCode = "87";
					txChiName = "預借現金第二次沖正駁回更正";
					break;
			 }
		 } else {
			 txEngName = "ErrorTxnCode";
			 txChiName = "無法判定交易碼";
			 showLogMessage("E", "", "FiscTxCode查無符合資料,cardNo=["+cardNo+"],fiscTxCode=["+fiscTxCode+"],usage_code,["+usageCode+"]");
			 //throw new Exception("查無符合資料");  //只顯示, 不abend
		 }
		 
		 transactionInformation.txCode = txCode;
		 transactionInformation.cbCode = cbCode;
		 transactionInformation.txEngName = txEngName;
		 transactionInformation.txChiName = txChiName;

		return transactionInformation;
	}
	

	private TransactionInformation getTransactionInformation(String fiscTxCode) throws Exception {
			String txCode = getSubString(fiscTxCode,2,4);	
			TransactionInformation transactionInformation = new TransactionInformation();

			// 因自行交易沒有使用碼可供判斷.故僅能就交易代號來判斷
			// 交易代號的第三碼若為0,則視為原始交易類
			// 交易代號的第三碼若為2,則視為原始交易Reversal
			// 交易代號的第三碼若為1,則視為第一次chargeback類
			// 交易代號的第三碼若為3,則視為第一次chargeback的Reversal
			int fiscTxCodeInteger = Integer.parseInt(fiscTxCode);
			 switch(getSubString(fiscTxCode,2,3)) {
			 case "0":
				 transactionInformation = getTxEngAndChiNameOfOriginalTransaction(transactionInformation, fiscTxCodeInteger);
				 transactionInformation.cbCode = "";
				 break;
			 case "2":
				 transactionInformation = getTxEngAndChiNameOfReversalTransaction(transactionInformation, fiscTxCodeInteger);
				 transactionInformation.cbCode = "";
				 break;
			 case "1":
				 transactionInformation = getTxEngAndChiNameOfFirstChargeBack(transactionInformation, fiscTxCodeInteger);
				 transactionInformation.cbCode = "1";
				 break;
		     case "3":
		    	 transactionInformation = getTxEngAndChiNameOfFirstReversalChargeBack(transactionInformation, fiscTxCodeInteger);
		    	 transactionInformation.cbCode = "1";
				 break;
			 }	 
			 
			 transactionInformation.txCode = txCode;
	
			return transactionInformation;
		}
	
	/**
	 * get 原始交易類 TxEng and ChiName
	 * @param transactionInformation
	 * @param fiscTxCodeInteger
	 * @return
	 */
	private TransactionInformation getTxEngAndChiNameOfOriginalTransaction(
			TransactionInformation transactionInformation, int fiscTxCodeInteger) {
		String txEngName = "", txChiName = "";
		switch(fiscTxCodeInteger) {
	    case 6205:
		case 7205:
		txEngName = "SALE_DRAFT";
		txChiName = "購貨(含電話授權購貨簽帳單)";
		break;
		case 6206:
		case 7206:
		txEngName = "CREDIT_VOUCHER";
		txChiName = "退貨(含電話授權退貨簽帳單)";
		break;
		case 6207:
		case 7207:
		case 6307:
		case 7307:
		txEngName = "CASH_ADVANCE";
		txChiName = "預借現金(含銀行櫃台電話授權預借現金簽帳單)";
		break;
		case 6305:
		case 7305:
		txEngName = "RETAIL_PAYMENT";
		txChiName = "代繳費稅[含電信資費、監理資費及信用卡繳稅業務]";
		break;
	 }
		transactionInformation.txEngName = txEngName;
		transactionInformation.txChiName = txChiName;
		
		return transactionInformation;
	}

	/**
	 * get 原始交易Reversal TxEng and ChiName
	 * @param transactionInformation
	 * @param fiscTxCodeInteger
	 * @return
	 */
	private TransactionInformation getTxEngAndChiNameOfReversalTransaction(
			TransactionInformation transactionInformation, int fiscTxCodeInteger) {
		String txEngName = "", txChiName = "";
		switch(fiscTxCodeInteger) {
	    case 6225:
		case 7225:
		txEngName = "SALE_REVERSAL";
		txChiName = "購貨原始更正";
		break;
		case 6226:
		case 7226:
		txEngName = "RFND_REVERSAL";
		txChiName = "退貨原始更正";
		break;
		case 6227:
		case 7227:
		case 6327:
		case 7327:
		txEngName = "CASH_REVERSAL";
		txChiName = "預借現金原始更正";
		break;
		case 6325:
		txEngName = "RETAIL_PAYMENT_REVERSAL";
		txChiName = "代繳費稅更正(僅信用卡繳稅業務適用)";
		break;
	    }
		transactionInformation.txEngName = txEngName;
		transactionInformation.txChiName = txChiName;
		
		return transactionInformation;
	}

	/**
	 * get 第一次chargeback TxEng and ChiName
	 * @param transactionInformation
	 * @param fiscTxCodeInteger
	 * @return
	 */
	private TransactionInformation getTxEngAndChiNameOfFirstChargeBack(
			TransactionInformation transactionInformation, int fiscTxCodeInteger) {
		String txEngName = "", txChiName = "";
		switch(fiscTxCodeInteger) {
	    case 6215:
		case 7215:
		txEngName = "SALE_CHARGEBACK1";
		txChiName = "購貨第一次沖正";
		break;
		case 6216:
		case 7216:
		txEngName = "RFND_CHARGEBACK1";
		txChiName = "退貨第一次沖正";
		break;
		case 6217:
		case 7217:
		case 6317:
		case 7317:
		txEngName = "CASH_CHARGEBACK1";
		txChiName = "預借現金第一次沖正";
		break;
	     }
		transactionInformation.txEngName = txEngName;
		transactionInformation.txChiName = txChiName;
		
		return transactionInformation;
	}

	/**
	 * get 第一次chargeback的Reversal TxEng and ChiName
	 * @param transactionInformation
	 * @param fiscTxCodeInteger
	 * @return
	 */
	private TransactionInformation getTxEngAndChiNameOfFirstReversalChargeBack(
			TransactionInformation transactionInformation, int fiscTxCodeInteger) {
		String txEngName = "", txChiName = "";
		switch(fiscTxCodeInteger) {
	    case 6235:
		case 7235:
		txEngName = "SALE_CHARGEBACK1_REVERSAL";
		txChiName = "購貨第一次沖正更正";
		break;
		case 6236:
		case 7236:
		txEngName = "RFND_CHARGEBACK1_REVERSAL";
		txChiName = "退貨第一次沖正更正";
		case 6237:
		case 7237:
		case 6337:
		case 7337:
		txEngName = "CASH_CHARGEBACK1_REVERSAL";
		txChiName = "預借現金第一次沖正更正";
		break;
	     }
		transactionInformation.txEngName = txEngName;
		transactionInformation.txChiName = txChiName;
		
		return transactionInformation;
	}
	
	private String getBinTypeFromPtrBintable(String binNo) throws Exception {
		selectSQL = " bin_type ";
		daoTable = " ptr_bintable ";
		whereStr = " where 1=1 " 
		        + " and bin_no = ? " ;
		
		setString(1, binNo);
		
		 if (selectTable() <= 0) {
        	//showLogMessage("E", "", String.format("無法找到卡號%s的bin type:", binNo));
        	setValue("bin_type","V");
        	//throw new Exception(String.format("無法找到卡號%s的bin type:", binNo));
         }
		 
		 return getValue("bin_type");
		
	}

	/**
	 * 依財金請款參數取得PlatformData
	 * @param fiscTxCode 交易代號
	 * @param reimbursementAttribute 財金請款的交易處理費屬性
	 * @param acceptorID 特店代號
	 * @param mccCode MCC Code
	 * @param reimbCode 
	 * @param settlementFlag
	 * @return
	 * @throws Exception
	 */
	public PlatformData getPlatformDataByFISC(String fiscTxCode, String reimbursementAttribute, 
			String acceptorID, String mccCode, String reimbCode, String settlementFlag ) throws Exception {

		PlatformData platformData = new PlatformData();
	
		
		//財金清算資料
		if ("6".equals(settlementFlag)) {
			
			//電信資費
			if (acceptorID.length() > 0) {
				if ("95002001".equals(acceptorID)) {
					platformData.setPlatformKind("V5");
		   		 	platformData.setPlatformDesc("電信資費");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006V500001");
		   		 	return platformData;
				}
			}
			
			//繳交交通罰款
			if (reimbCode.length() > 0) {
				if ("00".equals(reimbCode)) {
					platformData.setPlatformKind("V1");
		   		 	platformData.setPlatformDesc("繳交交通罰款");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006V100001");
		   		 	return platformData;
				}
			}
			
			//汽機車換發行照費-規費
			if (reimbCode.length() > 0) {
				if ("06".equals(reimbCode)) {
					platformData.setPlatformKind("V6");
		   		 	platformData.setPlatformDesc("汽機車換發行照費-規費");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006V600001");
		   		 	return platformData;
				}
			}
			
			//汽機車燃料費
			if (reimbCode.length() > 0) {
				if ("01".equals(reimbCode)) {
					platformData.setPlatformKind("V4");
		   		 	platformData.setPlatformDesc("汽機車燃料費");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006V400001");
		   		 	return platformData;
				}
			}
			
			//選牌
			if (reimbCode.length() > 0) {
				if ("17".equals(reimbCode)) {
					platformData.setPlatformKind("V2");
		   		 	platformData.setPlatformDesc("選牌");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006V200001");
		   		 	return platformData;
				}
			}
			
			//標牌
			if (reimbCode.length() > 0) {
				if ("16".equals(reimbCode)) {
					platformData.setPlatformKind("V3");
		   		 	platformData.setPlatformDesc("標牌");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006V300001");
		   		 	return platformData;
				}
			}

			
			if (reimbCode.length() > 0) {
				String[] arrayFeeCode= new String[] {"70","71","72","73","74","75","76","77","78","79","80","81","82","83","84","85","86","87","88"};
				if (ArrayUtils.contains(arrayFeeCode, reimbCode)) {
					platformData.setPlatformKind("G1");
		   		 	platformData.setPlatformDesc("財金-E-GOV之繳費交易");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006G100001");
		   		 	return platformData;
				}
			}
			
			if (reimbCode.length() > 0) {
				if ("89".equals(reimbCode)) {
					platformData.setPlatformKind("G2");
		   		 	platformData.setPlatformDesc("財金-E-GOV繳學雜費");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006G200001");
		   		 	return platformData;
				}
			}
			
			//自繳稅
			if (reimbCode.length() > 0) {
				if ("10".equals(reimbCode)) {
					platformData.setPlatformKind("10");
		   		 	platformData.setPlatformDesc("財金-自繳稅");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("0061000001");
		   		 	return platformData;
				}
			}
			
			//查核定稅
			if (reimbCode.length() > 0) {
				if ("20".equals(reimbCode)) {
					platformData.setPlatformKind("20");
		   		 	platformData.setPlatformDesc("財金-查核定稅");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("0062000001");
		   		 	return platformData;
				}
			}

		}
		
		//自行收單清算資料
		if ("9".equals(settlementFlag)) {
			
			//富邦人壽
			if (acceptorID.length() > 0) {
				if ("006279350734001".equals(acceptorID)) {
					platformData.setPlatformKind("FL");
		   		 	platformData.setPlatformDesc("富邦人壽");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006FL00001");
		   		 	return platformData;
				}
			}
			
			//中國人壽
			if (acceptorID.length() > 0) {
				if ("00603430164002".equals(acceptorID)) {
					platformData.setPlatformKind("CL");
		   		 	platformData.setPlatformDesc("中國人壽");
		   		 	platformData.setBillType("FISC");
		   		 	platformData.setCusMchtNo("006CL00001");
		   		 	return platformData;
				}
			}
		}
		
		//財金-電子化繳費稅平台
		if ("f".equals(reimbursementAttribute)) {
			platformData.setPlatformKind("f1");
   		 	platformData.setPlatformDesc("財金-電子化繳費稅平台");
   		 	platformData.setBillType("FISC");
   		 	platformData.setCusMchtNo("006f100001");
   		 	return platformData;
		}
		
		//NCCC-ONUS繳費平台
		if ("d".equals(reimbursementAttribute)) {
			platformData.setPlatformKind("d1");
   		 	platformData.setPlatformDesc("NCCC-ONUS繳費平台");
   		 	platformData.setBillType("FISC");
   		 	platformData.setCusMchtNo("006d100001");
   		 	return platformData;
		}
		
		//NCCC-信用卡小額支付平台
		if ("M".equals(reimbursementAttribute)) {
			platformData.setPlatformKind("M1");
   		 	platformData.setPlatformDesc("NCCC-信用卡小額支付平台");
   		 	platformData.setBillType("FISC");
   		 	platformData.setCusMchtNo("006M100001");
   		 	return platformData;
		}
		
		//中信繳學費平台
		if ("b".equals(reimbursementAttribute)) {
			platformData.setPlatformKind("b1");
   		 	platformData.setPlatformDesc("中信繳學費平台");
   		 	platformData.setBillType("FISC");
   		 	platformData.setCusMchtNo("006b100001");
   		 	return platformData;
		}
		
		//公部門及公立療機構
		if ("e".equals(reimbursementAttribute)) {
			platformData.setPlatformKind("e1");
			platformData.setPlatformDesc("公部門及公立療機構");
			platformData.setBillType("FISC");
			platformData.setCusMchtNo("006e100001");
			return platformData;
		}
		
		//VISA QR Code掃碼支付
		if ("v".equals(reimbursementAttribute)) {
			platformData.setPlatformKind("v1");
			platformData.setPlatformDesc("VISA QR Code掃碼支付");
			platformData.setBillType("FISC");
			platformData.setCusMchtNo("006v100001");
			return platformData;
		}
		
		//信用卡被掃交易
		if ("t".equals(reimbursementAttribute)) {
			platformData.setPlatformKind("t1");
			platformData.setPlatformDesc("信用卡被掃交易");
			platformData.setBillType("FISC");
			platformData.setCusMchtNo("006t100001");
			return platformData;
		}

		
		return platformData;
	}
	
	
	/**
	 * 依財金請款參數取得PlatformData
	 * @param fiscTxCode 交易代號
	 * @param reimbursementAttribute 財金請款的交易處理費屬性
	 * @param firstTwoMchtNo 特店代號前二碼
	 * @param mccCode MCC Code
	 * @param terminalId 端末機代碼
	 * @return
	 * @throws Exception
	 */
	public PlatformData getPlatformDataByFISC(String fiscTxCode, String reimbursementAttribute, 
			String firstTwoMchtNo, String mccCode, String terminalId  ) throws Exception {
		PlatformData platformData = new PlatformData();
		
		int i = 1 ;
		selectSQL = " distinct bp.platform_kind, bp.platform_desc, bp.bill_type, bp.cust_mcht_no ";
		daoTable = " bil_platform as bp left join bil_platform_data as bpd on bp.platform_kind = bpd.platform_kind ";
		whereStr = " where bp.platform_tab = 'F' and bp.bill_type = 'FISC' ";
		
		if(! isEmpty(reimbursementAttribute)) {
			whereStr += " and ( (bp.fee_attribute_flag = 'Y' and ( bp.fee_attribute1 = ?   or bp.fee_attribute2 = ? )) or ";
			whereStr += "          (bp.fee_attribute_flag <> 'Y' ) ) ";
			setString( i++, reimbursementAttribute);
			setString( i++, reimbursementAttribute);
		}
		
		if(! isEmpty(firstTwoMchtNo)) {
			whereStr += " and ( ( ( bpd.data_type in ('MH1','MH2') and bpd.data_fit_flag = 'Y' and bpd.data_value = ?  ) ";
			whereStr += " or    ( bpd.data_type in ('MH1','MH2') and bpd.data_fit_flag = 'N' and bpd.data_value != ?  ) )  or ";
			whereStr += "        ( bp.merchant_no1_flag <> 'Y' and bp.merchant_no2_flag <> 'Y' ) )  ";
			setString( i++, firstTwoMchtNo);
			setString( i++, firstTwoMchtNo);
		}
		
		if(! isEmpty(mccCode)) {
			whereStr += " and ( ( bp.mcc_code_flag = 'Y' and bp.mcc_code = ? ) or ";
			whereStr += "          ( bp.mcc_code_flag <> 'Y' ) ) ";
			setString( i++, mccCode);
		}
		
		if(! isEmpty(terminalId)) {
			whereStr += " and ( ( bpd.data_type = 'TER' and bpd.data_value = ? ) or ";
			whereStr += "          ( bp.terminal_id_flag <> 'Y' ) ) ";
			setString( i++, terminalId);
		}
		
		 if (selectTable() > 0) {
    		 platformData.setPlatformKind(getValue("platform_kind"));
    		 platformData.setPlatformDesc(getValue("platform_desc"));
    		 platformData.setBillType(getValue("bill_type"));
    		 platformData.setCusMchtNo(getValue("cust_mcht_no"));	
         }

		return platformData;
	}
	
	/**
	 * 依壽險參數取得PlatformData。
	 * @param terminalId16Digits  端末機代號_16(16碼)
	 * @return PlatformData
	 * @throws Exception 
	 */
	public PlatformData getPlatformDataByInsure( String terminalId16Digits) throws Exception{
		PlatformData platformData = new PlatformData();
		
		if(isEmpty(terminalId16Digits)) {
			platformData.setBillType("FISC");
			return platformData;
		}

		selectSQL = " distinct bp.platform_kind, bp.platform_desc, bp.bill_type, bp.cust_mcht_no ";
		daoTable = " bil_platform as bp left join bil_platform_data as bpd on bp.platform_kind = bpd.platform_kind ";
		whereStr = " where bp.platform_tab = 'I' "
				           + " and bpd.data_type = 'TER' "
				           + " and ? like bpd.data_value || '%'  ";

			setString( 1, terminalId16Digits);
		
		 if (selectTable() > 0) {
    		 platformData.setPlatformKind(getValue("platform_kind"));
    		 platformData.setPlatformDesc(getValue("platform_desc"));
    		 platformData.setBillType(getValue("bill_type"));
    		 platformData.setCusMchtNo(getValue("cust_mcht_no"));	
         }
		 
		return platformData;
	}
	
	/**
	 * get acctCode and signFlag from ptr_billtype
	 * @param billType
	 * @param txCode
	 * @return
	 * @throws Exception
	 */
	private PtrBilltype getPtrBilltype(String billType, String txCode) throws Exception {
		PtrBilltype ptrBilltype = new PtrBilltype();
		sqlCmd =    "select ";
		sqlCmd += " acct_code, ";
		sqlCmd += " sign_flag ";
        sqlCmd += " from ptr_billtype  ";
        sqlCmd += " where bill_type = ?  ";
        sqlCmd += " and txn_code  = ? ";
        setString(1, billType);
        setString(2, txCode);
        int recordCnt = selectTable();
        if (recordCnt <= 0) {
        	showLogMessage("I", "", "select ptr_billtype error, bill_type/txn_code:[" +billType + "],[" + txCode + "]" );    //debug
        	//throw new Exception("select ptr_billtype error");
        } else {
        	ptrBilltype.acctCode = getValue("acct_code");
        	ptrBilltype.signFlag = getValue("sign_flag");
        }
        
        return ptrBilltype;
		
	}

	private String getCardNoFromHceCard(String cardNo) throws Exception {
		sqlCmd  = "select ";
        sqlCmd += " card_no as hce_card_no ";
        sqlCmd += " from hce_card as hc";
        sqlCmd += " where 1=1  ";
        sqlCmd += " and v_card_no = ?  ";
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	return getValue("hce_card_no");
        }else {
        	return null;
        }

	}

	private String getCardNoFromOempayCard(String cardNo) throws Exception {
		sqlCmd  = "select ";
        sqlCmd += " card_no as oempay_card_no ";
        sqlCmd += " from oempay_card";
        sqlCmd += " where 1=1  ";
        sqlCmd += " and v_card_no = ?  ";
        setString(1, cardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	return getValue("oempay_card_no");
        }else {
        	return null;
        }

	}
	
	/**
	 * get dualCardCurrencyCode, binType, and debitFlag from ptr_bintable
	 * @param cardNo
	 * @return
	 * @throws Exception
	 */
	private PtrBintable getPtrBintable(String cardNo) throws Exception {
		
		PtrBintable ptrBintable = new PtrBintable();
		
		sqlCmd  =   "select ";
        sqlCmd += " a.bin_type,";
        sqlCmd += " decode(debit_flag  ,'','N'  ,debit_flag)   as h_debit_flag,";
        sqlCmd += " decode(dc_curr_code,'','901',dc_curr_code) as h_dc_curr_code ";
        sqlCmd += " from ptr_bintable a ";
        sqlCmd += " where 1=1  ";
        sqlCmd += " and a.bin_no || a.bin_no_2_fm || '0000' <= ?  ";
        sqlCmd += " and a.bin_no || a.bin_no_2_to || '9999' >= ?  ";
        setString(1, cardNo);
        setString(2, cardNo);

        int recordCnt = selectTable();
        if (recordCnt <= 0) {
        	//showLogMessage("E", "", String.format("找不到指定卡號%s的 ptr_bintable", cardNo) );
        	ptrBintable.binType = "";
        	ptrBintable.debitFlag = "";
        	ptrBintable.dualCardCurrencyCode = "901";
        	//throw new Exception(String.format("找不到指定卡號%s的 ptr_bintable", cardNo));
        }  else {
        	ptrBintable.binType = getValue("bin_type");
        	ptrBintable.debitFlag = getValue("h_debit_flag");
        	ptrBintable.dualCardCurrencyCode = getValue("h_dc_curr_code");
        }
        
		return ptrBintable;
	}

	/**
	 * 依據輸入的參數,建立一個資料型態為FiscTxData的交易資訊物件
	 * @param fiscTxCode 財金交易代號
	 * @param usageCode 使用碼
	 * @param cardNo 財金請款卡號
	 * @param reimbursementAttribute 財金請款的交易處理費屬性
	 * @param firstTwoMchtNo 特店代號前二碼
	 * @param mccCode MCC Code
	 * @param reimbCode 
	 * @param settlementFlag 
	 * @return
	 * @throws Exception
	 */
	public FiscTxData getFiscTxData(String fiscTxCode, String usageCode, String cardNo, 
		String reimbursementAttribute, String acceptorID, String mccCode, String reimbCode, String settlementFlag) throws Exception {
		FiscTxData fiscTxData = new FiscTxData();
		String currCode = null;
		String dcFlag = null;
		String realCardNo = null;
		String vCardNo = null;
		String txCode = null;
		
		 // =========================
		
		String  hceCardNo = getCardNoFromHceCard(cardNo);
		String  oempayCardNo = getCardNoFromOempayCard(cardNo);  //V20220501
		if(hceCardNo != null) {
			realCardNo =  hceCardNo;
	    	vCardNo = cardNo;
		}else if (oempayCardNo != null) { 
			realCardNo =  oempayCardNo;
		    vCardNo = cardNo;
		} else {
			realCardNo = cardNo;
	    	vCardNo = "";
		}

		 // =========================
		
		// get dualCardCurrencyCode, binType, and debitFlag from ptr_bintable
		PtrBintable ptrBintable = getPtrBintable(realCardNo);
		currCode = ptrBintable.dualCardCurrencyCode;
		
		if( ! currCode.equals("901")) {
			dcFlag = "Y";
		}else {
			dcFlag = "N";
		}
		
		
	    // =========================
	    
		TransactionInformation transactionInformation = getTransactionInformation(fiscTxCode, usageCode, cardNo);
		txCode = transactionInformation.txCode;
		
		// =========================
		PlatformData platformData = new PlatformData();
		
		//若是特殊交易billType="FIRP"
		if ("1".equals(transactionInformation.cbCode) || "2".equals(transactionInformation.cbCode)) {
			platformData.setBillType("FIRP");
		} else {
			platformData = 
				getPlatformDataByFISC(fiscTxCode, reimbursementAttribute, acceptorID, mccCode, reimbCode, settlementFlag);
		}
		
		if(isEmpty(platformData.getBillType())) {
			platformData.setBillType("FISC");
		}
		
		// =========================
		
		// get acctCode and signFlag from ptr_billtype
		PtrBilltype ptrBilltype = getPtrBilltype(platformData.getBillType(), txCode);
		
	    //==========================
		
		
		fiscTxData.setFiscTxCode(fiscTxCode);
		fiscTxData.setTxEngName(transactionInformation.txEngName);
		fiscTxData.setTxChiName(transactionInformation.txChiName);
		fiscTxData.setUsageCode(usageCode);
		fiscTxData.setBinType(ptrBintable.binType);
		fiscTxData.setDebitFlag(ptrBintable.debitFlag);
		fiscTxData.setDcFlag(dcFlag);
		fiscTxData.setDcCurr(currCode);
		fiscTxData.setTxCode (txCode);
		fiscTxData.setCbCode(transactionInformation.cbCode);
		fiscTxData.setBillType(platformData.getBillType());
		fiscTxData.setAcctCode(ptrBilltype.acctCode);
		fiscTxData.setSignCode(ptrBilltype.signFlag);
		fiscTxData.setRealCardNo(realCardNo);
		fiscTxData.setvCardNo(vCardNo);
		fiscTxData.setPlatformData(platformData);
		
		return fiscTxData;
	}

	/**
	 * 
	 * @param fiscTxCode 交易代號
	 * @param cardNo 卡號
	 * @param reimbAttr 信用卡繳費註記
	 * @param mchtNo 特店代碼
	 * @param terminalId16Digits 端末機代號_16(16碼)
	 * @param terminalId8Digits 端末機代碼_8(8碼)
	 * @return
	 * @throws Exception 
	 */
	public FiscTxData getONUSTxData(String fiscTxCode, String cardNo, String reimbAttr, 
			String mchtNo, String reimbCode, String settlementFlag) throws Exception {
		FiscTxData fiscTxData = new FiscTxData();
		String currCode = null;
		String dcFlag = null;
		String realCardNo = null;
		String vCardNo = null;
		String txCode = null;
		
		 // =========================
		
		// get dualCardCurrencyCode, binType, and debitFlag from ptr_bintable
		PtrBintable ptrBintable = getPtrBintable(cardNo);
		currCode = ptrBintable.dualCardCurrencyCode;
		
		if( ! currCode.equals("901")) {
			dcFlag = "Y";
		}else {
			dcFlag = "N";
		}
		
		 // =========================
	
		String  hceCardNo = getCardNoFromHceCard(cardNo);
		String  oempayCardNo = getCardNoFromOempayCard(cardNo);  //V20220501
		if(hceCardNo != null) {
			realCardNo =  hceCardNo;
	    	vCardNo = cardNo;
		}else if (oempayCardNo != null) { 
			realCardNo =  oempayCardNo;
		    vCardNo = cardNo;
		} else {
			realCardNo = cardNo;
	    	vCardNo = "";
		}
	    // =========================
	    
		TransactionInformation transactionInformation = getTransactionInformation(fiscTxCode);
		txCode = transactionInformation.txCode;
		
		// =========================
		 
		PlatformData platformData = getPlatformDataByFISC(fiscTxCode, reimbAttr, mchtNo, "", reimbCode, settlementFlag);
		if(isEmpty(platformData.getBillType())) {
			platformData.setBillType("FISC");
		}
		// =========================
		
		// get acctCode and signFlag from ptr_billtype
		PtrBilltype ptrBilltype = getPtrBilltype(platformData.getBillType(), txCode);
		
	    //==========================
		
		
		fiscTxData.setFiscTxCode(fiscTxCode);
		fiscTxData.setTxEngName(transactionInformation.txEngName);
		fiscTxData.setTxChiName(transactionInformation.txChiName);
		fiscTxData.setUsageCode("1");
		fiscTxData.setBinType(ptrBintable.binType);
		fiscTxData.setDebitFlag(ptrBintable.debitFlag);
		fiscTxData.setDcFlag(dcFlag);
		fiscTxData.setDcCurr(currCode);
		fiscTxData.setTxCode (txCode);
		fiscTxData.setCbCode(transactionInformation.cbCode);
		fiscTxData.setBillType(platformData.getBillType());
		fiscTxData.setAcctCode(ptrBilltype.acctCode);
		fiscTxData.setSignCode(ptrBilltype.signFlag);
		fiscTxData.setRealCardNo(realCardNo);
		fiscTxData.setvCardNo(vCardNo);
		fiscTxData.setPlatformData(platformData);
		
		return fiscTxData;
	}
	
	/***
	 * 取得雙幣卡的匯率
	 * @param currCode 雙幣卡約定清算幣別
	 * @return
	 * @throws Exception
	 */
	public Double getDCExchangeRate(String currCode) throws Exception {
		sqlCmd  = "select ";
        sqlCmd += " exchange_rate ";
        sqlCmd += " from ptr_curr_rate ";
        sqlCmd += " where 1=1  ";
        sqlCmd += " and curr_code = ?  ";
        
        setString(1, currCode);
        if (selectTable() > 0) {
        	return  getValueDouble("exchange_rate");
        }else {
        	throw new Exception("select ptr_billtype error");
        }
	
	}
	
	/**
	 * 取得VD在國外ATM預借現金的手續費，
	 * 若沒查詢到資料，則回傳0
	 * @param destinationAmt 台幣金額
	 * @return
	 * @throws Exception
	 */
	public double  getVD09Fee(Double  destinationAmt) throws Exception {
		sqlCmd  = "select ";
        sqlCmd += " fees_fix_amt, fix_rate ";
        sqlCmd += " from dbb_markup ";
        
        if (selectTable() > 0) {
        	return  getValueDouble("fees_fix_amt") + destinationAmt * getValueDouble("fix_rate");
        }else {
        	return 0.0;
        }
	}
	
	/***
	 * 計算此筆海外交易手續費
	 * @param binType 國際組織卡別(V/M/C)
	 * @param merchantCountry mcht_country(特店國家代號)
	 * @param destinationCurrency destination_currency(目的地幣別)
	 * @param sourceCurrency source_currency(購買地幣別)
	 * @param debitFlag debit_flag(是否為VD)
	 * @param dcCurrCode 雙幣卡約定清算的幣別
	 * @param destAmt 台幣
	 * @param destinationAmount 原請款檔內的destination_amt
	 * @return ForeignFeeData
	 * @throws Exception 
	 */
	public ForeignFeeData getForeignFeeData(String binType, String merchantCountry, String destinationCurrency, String sourceCurrency,
			String debitFlag, String dcCurrCode, Double destAmt, Double destinationAmount) throws Exception{
		Double foreignRate = 0.0;
		ForeignFeeData foreignFeeData = new ForeignFeeData();
		
		sqlCmd =  "select ";
		switch(binType) {
		case "V":
			sqlCmd +=" decode("
	                +" substr(cast(? as varchar(15)),1,2), 'TW', "
	                +" decode(cast(? as varchar(15)), cast(? as varchar(15)),0,v_currency_diff_rate), "
	                +" decode(cast(? as varchar(15)), cast(? as varchar(15)),v_country_diff_rate, v_both_diff_rate) "
	                +" ) ";
			break;
		case "M":
			sqlCmd += " decode("
	                +" substr(cast(? as varchar(15)),1,2), 'TW', "
	                +" decode(cast(? as varchar(15)), cast(? as varchar(15)),0,m_currency_diff_rate), " 
	                +" decode(cast(? as varchar(15)), cast(? as varchar(15)),m_country_diff_rate, m_both_diff_rate) "
	                +" ) ";
			break;
		case "J":
			sqlCmd += " decode("
	                +" substr(cast(? as varchar(15)),1,2), 'TW', "
	                +" decode(cast(? as varchar(15)), cast(? as varchar(15)),0,j_currency_diff_rate), " 
	                +" decode(cast(? as varchar(15)), cast(? as varchar(15)),j_country_diff_rate, j_both_diff_rate) "
	                +"  ) ";
			break;
		default:
			
			break;
		}	
		sqlCmd += " as h_foreign_rate "
                +" from ptr_foreign_fee  "
                +" where card_property = decode(cast(? as varchar(15)) ,'Y','D','C') "
                +" and curr_code     = ? ";
		
        setString(1, merchantCountry);
        setString(2, destinationCurrency);
        setString(3, sourceCurrency);
        setString(4, destinationCurrency);
        setString(5, sourceCurrency);
        setString(6, debitFlag);
        dcCurrCode = "".equals(dcCurrCode) ? "901" : dcCurrCode;
        setString(7, dcCurrCode);
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	throw new Exception("select_ptr_foreign_fee not Found");
        }
        if (recordCnt > 0) {
        	foreignRate = getValueDouble("h_foreign_rate");
        }
        
        BigDecimal inputVal = null;
        BigDecimal inputRate = null;
        inputVal = new BigDecimal(destAmt).setScale(2, RoundingMode.HALF_UP);
        inputRate = new BigDecimal(foreignRate).setScale(2, RoundingMode.HALF_UP);
        double foreignFee = inputVal.multiply(inputRate).divide(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).doubleValue();
        
        inputVal = new BigDecimal(destinationAmount).setScale(2, RoundingMode.HALF_UP);
        inputRate = new BigDecimal(foreignRate).setScale(2, RoundingMode.HALF_UP);
        double dcForeignFee = inputVal.multiply(inputRate).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).doubleValue();

        //double foreignFee = round((destAmt * foreignRate) / 100, 0);
        //double dcForeignFee = round((destinationAmount * foreignRate) / 100, 2);
        
        foreignFeeData.setForeignFee(foreignFee);
        foreignFeeData.setDcForeignFee(dcForeignFee);
        
        // ==============================================
        
        foreignFeeData.setDcForeignFeeCurr(dcCurrCode);
        
       // ==============================================
        
		return foreignFeeData;
	} 
	
	/**
	 * get the string of selecting FISC's bank number from PTR_SYS_PARM
	 * @return
	 * @throws Exception
	 */
	public  String getFiscBankNoFromPtrSysParm() throws Exception {
        selectSQL = " wf_value as bankNo ";
        daoTable = " ptr_sys_parm ";
        whereStr = " where 1=1 "
        		+ " and wf_parm='BIL_FISC_PARM' " 
        		+ " and wf_key='FISC_BANK_NO' "
        		;   
        if (selectTable() > 0) {
        	return  getValue("bankNo");
        }else {
        	throw new Exception("the bank number does not exist");
        }
	}

	public  int getMaxSeqNo(String busiDate) throws Exception {
		
		selectSQL = " max(fctl_seq) as maxSeqNo ";
		daoTable = " bil_fiscctl ";
		whereStr = " where 1=1 " 
		        + " and fctl_date= ? " 
				+ " and fctl_type='FISC' ";
		
		setString(1, busiDate);
		
		selectTable();
		
		// getValueInt: if the value is null, then return 0, otherwise return this value.
		return getValueInt("maxSeqNo");
	
	}

	/**
		 * return fctl_no, if this method cannot find fctl_no, then return ""
		 * @param fileNameD
		 * @return
		 * @throws Exception
		 */
		public String getFctlNoFromBilFiscctl(String fileNameD) throws Exception{
			selectSQL = " fctl_no ";
			daoTable = " bil_fiscctl ";
			whereStr = " where 1=1 " 
	//		        + " and fctl_date= ? " 
					+ " and fctl_type='FISC' "
					+ " and media_name = ?"
					+ " and proc_code <> 'Y' ";
			
	//		setString(1, busiDate);
	//		setString(2, fileNameD);
			setString(1, fileNameD);
	
			if (selectTable() <= 0) {
				// 找不到fctl_no
				return "";
			}else {
				return getValue("fctl_no");
			}
		}

	/**
	 * @param fctlNo 
	 * @throws Exception 
	  * 
	  **/ 
	public void deleteBilFiscdtl(String fctlNo) throws Exception {
		
		daoTable = " bil_fiscdtl ";
		whereStr = " where 1=1 " 
		        + " and ecs_fctl_no = ? ";
		
		setString(1, fctlNo);
	
		deleteTable();
		
	}

	public void deleteBilFiscctl(String fctlNo) throws Exception {
	
		daoTable = " bil_fiscctl ";
		whereStr = " where 1=1 " 
		        + " and fctl_no = ? "
				+ " and proc_code <> 'Y' ";
		
		setString(1, fctlNo);
	
		deleteTable();
	}

	/**
	  * update bil_fiscctl set proc_code='Y'
	  * @param fileNameD
	  * @return
	  * @throws Exception
	  */
	public void updateProcCodeFromBilFiscctl(String fctlNo ) throws Exception {
		
		daoTable  = " bil_fiscctl ";
		updateSQL = " proc_code = ? ";
		whereStr   = " where 1=1 " 
		                     + " and fctl_no = ? "
				             + " and proc_code <> 'Y' ";
	
		setString(1, "Y");
		setString(2, fctlNo);
	
		if (updateTable() == 0) {
			throw(new Exception("fail to update procCode From bil_fiscctl"));
		} 
	}

	/**
	 * set Y on proc_code when  the file has already processed BilE1X2 -> BilE1X3
	 * @param javaProgram
	 * @param fileNameArr[]
	 * @return
	 * @throws Exception
	 */
	public void updateProcCodeFromecsFtpLog(String javaProgram, String ... fileNameArr) throws Exception {
		// F00600000.XXXXXXXD.XXXXXXXX -> F00600000.XXXXXXXD.XXXXXXXX
		daoTable  = " ecs_ftp_log ";
		updateSQL = " proc_code = 'Y', "
				                + " mod_pgm = ?, "
				                + " mod_time = sysdate ";
		setString(1, javaProgram);
		
		whereStr   = " where file_name in ( ";
		for(int i = 0 ; i < fileNameArr.length; i++) {
			if( i == fileNameArr.length-1 ) {
				whereStr   += " ? ) " ;
			}else {
				whereStr   += " ?, " ;
			}
			setString(i+2, fileNameArr[i]);
		}
	
		int returnInt = updateTable();
		if ( returnInt == 0) {
			throw new Exception("fail to update proc_code");
		} 
		
	}

	/**
	 * 
	 * @param decimal
	 * @param scale
	 * @return
	 */
	public double round(double decimal, int scale) {
        if (scale < 0) {
            scale = 0;
        }
        BigDecimal bd = new BigDecimal(decimal);
        
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
	
	/**
	 * 取得給定數字字串(valueString)除以100的BigDecimal型態物件
	 * @param valueString:數字字串
	 * @return
	 */
	public BigDecimal getBigDecimalDividedBy100(String valueString) {
		return getBigDecimalDivided(valueString, 100);
	}
	
	/**
	 * 取得給定數字字串(valueString)除以divisor的BigDecimal型態物件
	 * @param valueString:數字字串
	 * @param divisor:除數
	 * @return
	 */
	public BigDecimal getBigDecimalDivided(String valueString, int divisor) {
		if (isEmpty(valueString))  return BigDecimal.ZERO;
		
		if (divisor == 100) {
			return new BigDecimal(valueString).divide(bigDecimal100);
		}
		
		return new BigDecimal(valueString).divide(new BigDecimal(divisor));
	} 
	
	/**
	 * 依begin及end，切割位元組陣列，最後將切割出的位元組陣列使用big5編碼轉成string。<br>
	 * 此方法與String.substring相似。
	 * @param bytesArr 給定byte[] array
	 * @param begin 切割起始位置(從0開始)
	 * @param end 切割終點位置
	 * @return String
	 */
	public static String subByteToStr(byte[] bytesArr, int begin, int end) {
		int  length = end - begin;
		byte[] subArr = new byte[length];
		System.arraycopy(bytesArr, begin,subArr , 0, length);		
		try {
			return new String(subArr, "MS950");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(String.format("subByteToStr error! begin[%d], end[%d]", begin, end));
		}
		return null;
	}
	
	/**
	 * 
	 * @param stringDate
	 * @param originalFormat
	 * @param targetFormat
	 * @return
	 */
	public static String convertStrDateFormat(String stringDate, String originalFormat, String targetFormat ) {
		try {
			return new SimpleDateFormat(targetFormat).format(
					new SimpleDateFormat(originalFormat).parse(stringDate));
		} catch (ParseException e) {
			return ""; 
		}
	}
	
	/**
	 * check whether s1 is greater than s2
	 * @param str
	 * @param str1
	 * @return
	 */
    public  boolean compareStr(String str, String str1) {
    	if (isEmpty(str) || isEmpty(str1))
    		return true;
    	if (nvl(str).compareTo(nvl(str1)) < 0)
    		return false;

    	return true;
    }
    
    protected String nvl(String param) {
    	if (param == null) {
    		return "";
    	}
    	return param.trim();
    }
    
	public void printDebugString(String outString) {
		if (DEBUG)
			System.out.println(outString);
	}
	
	/**
	  * check whether this data has already been processed
	  **/
	public boolean checkRepeatProcess(String fileNameD) throws Exception {
		String busiDate= getBusiDate();
		
	        selectSQL = " count(*) as counts";
	        daoTable = " bil_fiscctl ";
	        whereStr = " where 1=1 "
//	        		+ " and fctl_date= ? "
	        		+ " and fctl_type= 'FISC' " 
	        		+ " and media_name = ? "
	        		+ " and proc_code = 'Y' "
	        		;   

//	        setString(1, busiDate);
//	        setString(2, fileNameD);
	        setString(1, fileNameD);
	        selectTable();
	        if ( getValueInt("counts") > 0) {
	        	showLogMessage("E", ""
	           			, String.format("檔名:{%s, %s}已經執行處理過", "FISC", fileNameD) );
	           	return true;
           }else {
        	   return false;
           }	       
		}
	
	/***
	 * 財金請款交易
	 */
	public class FiscTxData{
		
		private	String 	fiscTxCode;	//交易代號(4位)
		private	String	    txEngName;	//交易英文名稱
		private	String	    txChiName;	//交易中文名稱
		private	String 	usageCode;	//使用碼
		private	String 	binType;	//國際組織卡別
		private	String 	debitFlag;	//是否為debit卡(Y/N)
		private	String	    dcFlag;	//是否為雙幣卡
		private	String	    dcCurr;	//雙幣卡海外清算幣別
		private	String	    txCode ;	//ECS交易別(2位)
		private	String	    cbCode	;	//沖正碼(1:第1次,2:第二次,其他:空白)
		private	String 	billType	;	//帳單類別
		private	String 	acctCode;	//帳務科目
		private	String 	signCode;	//正負向(+、-)
		private	String	    realCardNo;	//實體卡號
		private	String 	vCardNo;	//虛擬卡號
		private  PlatformData platformData; //交易平台
		

		public String getTxEngName() {
			return txEngName;
		}
		public void setTxEngName(String txEngName) {
			this.txEngName = txEngName;
		}
		public String getTxChiName() {
			return txChiName;
		}
		public void setTxChiName(String txChiName) {
			this.txChiName = txChiName;
		}
		public String getUsageCode() {
			return usageCode;
		}
		public void setUsageCode(String usageCode) {
			this.usageCode = usageCode;
		}
		public String getBinType() {
			return binType;
		}
		public void setBinType(String binType) {
			this.binType = binType;
		}
		public String getDebitFlag() {
			return debitFlag;
		}
		public void setDebitFlag(String debitFlag) {
			this.debitFlag = debitFlag;
		}
		public String getDcFlag() {
			return dcFlag;
		}
		public void setDcFlag(String dcFlag) {
			this.dcFlag = dcFlag;
		}
		public String getDcCurr() {
			return dcCurr;
		}
		public void setDcCurr(String dcCurr) {
			this.dcCurr = dcCurr;
		}
		public String getTxCode() {
			return txCode;
		}
		public void setTxCode(String txCode) {
			this.txCode = txCode;
		}
		public String getCbCode() {
			return cbCode;
		}
		public void setCbCode(String cbCode) {
			this.cbCode = cbCode;
		}
		public String getBillType() {
			return billType;
		}
		public void setBillType(String billType) {
			this.billType = billType;
		}
		public String getAcctCode() {
			return acctCode;
		}
		public void setAcctCode(String acctCode) {
			this.acctCode = acctCode;
		}
		public String getSignCode() {
			return signCode;
		}
		public void setSignCode(String signCode) {
			this.signCode = signCode;
		}
		public String getRealCardNo() {
			return realCardNo;
		}
		public void setRealCardNo(String realCardNo) {
			this.realCardNo = realCardNo;
		}
		public String getvCardNo() {
			return vCardNo;
		}
		public void setvCardNo(String vCardNo) {
			this.vCardNo = vCardNo;
		}
		public PlatformData getPlatformData() {
			return platformData;
		}
		public void setPlatformData(PlatformData platformData) {
			this.platformData = platformData;
		}
		public String getFiscTxCode() {
			return fiscTxCode;
		}
		public void setFiscTxCode(String fiscTxCode) {
			this.fiscTxCode = fiscTxCode;
		}
	
	}
/***
 * 交易平台
 */
	public class PlatformData{
		private String platformKind =""; // 交易平台種類代碼
		private String platformDesc = ""; // 交易平台說明
		private String billType = ""; // 帳單類別
		private String cusMchtNo = ""; // 客制特店代號

		public String getPlatformKind() {
			return platformKind;
		}

		public void setPlatformKind(String platformKind) {
			this.platformKind = platformKind;
		}

		public String getPlatformDesc() {
			return platformDesc;
		}

		public void setPlatformDesc(String platformDesc) {
			this.platformDesc = platformDesc;
		}

		public String getBillType() {
			return billType;
		}

		public void setBillType(String billType) {
			this.billType = billType;
		}

		public String getCusMchtNo() {
			return cusMchtNo;
		}

		public void setCusMchtNo(String cusMchtNo) {
			this.cusMchtNo = cusMchtNo;
		}
	}
	
	public class ForeignFeeData {
		private String foreignFeeCurr = "901";
		private double foreignFee = 0.0;             //destination_amt x 匯率 +VD(以台幣計算)
		private String dcForeignFeeCurr = ""; //FiscTxData.dcCurr
		private double dcForeignFee = 0.0;;       //雙幣卡國外手續費(以原始請款的目的地金額計算)
		
		public double getForeignFee() {
			return foreignFee;
		}
		public void setForeignFee(double foreignFee) {
			this.foreignFee = foreignFee;
		}
		public double getDcForeignFee() {
			return dcForeignFee;
		}
		public void setDcForeignFee(double dcForeignFee) {
			this.dcForeignFee = dcForeignFee;
		}
		public String getForeignFeeCurr() {
			return foreignFeeCurr;
		}
//		public void setForeignFeeCurr(String foreignFeeCurr) {
//			this.foreignFeeCurr = foreignFeeCurr;
//		}
		public String getDcForeignFeeCurr() {
			return dcForeignFeeCurr;
		}
		public void setDcForeignFeeCurr(String dcForeignFeeCurr) {
			this.dcForeignFeeCurr = dcForeignFeeCurr;
		}

	}
	
	private class PtrBilltype{
		private String acctCode = "";
		private String signFlag = "";
	}
	
	private class PtrBintable{
		private String binType = ""; 
		private String debitFlag = "";
		private String dualCardCurrencyCode = "";
	}
	
	private class TransactionInformation{
		private String txCode = "";   //ecs transaction code
		private String cbCode = ""; // charge back code
		private String txEngName = ""; // transaction English name
		private String txChiName = ""; // transaction Chinese name
	}

}
