/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version     AUTHOR              DESCRIPTION                     *
*  --------  ---------- ---------  ------------------------------------------*
* 111/05/31  V1.00.00     Justin     initial                                 *
******************************************************************************/
package Dba;

import java.math.BigDecimal;

class DbaAbem {
	String pSeqno = ""; // P_SEQNO
	String acctMonth = ""; // ACCT_MONTH
	String printType = ""; // PRINT_TYPE
	int printSeq = 0; // PRINT_SEQ
	String acctCode = ""; // ACCT_CODE
	String tranClass = ""; // TRAN_CLASS
	String referenceNo = ""; // REFERENCE_NO
	String cardNo = ""; // CARD_NO
	String purchaseDate = ""; // PURCHASE_DATE
	String acctDate = ""; // ACCT_DATE
	BigDecimal transactionAmt = BigDecimal.ZERO; // TRANSACTION_AMT
	String description = ""; // DESCRIPTION
	String txnCode = ""; // TXN_CODE
	String exchangeDate = ""; // EXCHANGE_DATE
	String sourceCurr = ""; // SOURCE_CURR
	BigDecimal sourceAmt = BigDecimal.ZERO; // SOURCE_AMT
	String mchtCountry = ""; // MCHT_COUNTRY
	String modPgm = ""; // MOD_PGM
	int modSeqno = 0; // MOD_SEQNO
	String signFlag = "+";
}
