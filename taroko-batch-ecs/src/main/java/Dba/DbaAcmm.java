package Dba;

import java.math.BigDecimal;

public class DbaAcmm {
	String pSeqno = "";
	String acctMonth = "";
	String stmtCycle = "";
	String acctNo = "";
	String acctType = "";
	String acctKey = "";
	String idPSeqno = "";
	String cardNo = "";
	String nation = "";
	String chineseName = "";
	String chiTitle = "";
	String birthday = "";
	String eMailAddr = "";
	String zipCode = "";
	String zipCodeChin = "";
	String mailAddr1 = "";
	String mailAddr2 = "";
	String cycleDate = "";
	String bankBranchNo = "";
	String sequentialNo = "";
	String printSequential = "";
	BigDecimal ttlDeductDone = BigDecimal.ZERO;;
	BigDecimal ttlDeductNotyet = BigDecimal.ZERO;
	BigDecimal ttlPurchase = BigDecimal.ZERO;
	BigDecimal ttlPurchCnt = BigDecimal.ZERO;
	int problemTxCnt = 0;
	int statementCount = 0;
	String printFlag = "";
	BigDecimal lastMonthBonus = BigDecimal.ZERO;
	BigDecimal newAddBonus = BigDecimal.ZERO;
	BigDecimal adjustBonus = BigDecimal.ZERO;
	BigDecimal useBonus = BigDecimal.ZERO;
	BigDecimal netBonus = BigDecimal.ZERO;
	BigDecimal eraseBonus = BigDecimal.ZERO;
	String eraseDate = "";
	String modPgm = "";
	int modSeqno = 0;
	String unprintFlagRegular = "";
}
