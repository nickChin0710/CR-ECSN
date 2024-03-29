/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-07-207  V1.00  yanghan       修改了變量名稱和方法名稱*
***************************************************************************/
package hdata;

public class ActAcctHst extends BaseBin {

	public String pSeqno = "";
	public String acctMonth = "";
	public String acctType = "";
	public String corpPSeqno = "";
	public String idPSeqno = "";
	public String stmtCycle = "";
	public double acctJrnlBal = 0;
	public double begBalOp = 0;
	public double endBalOp = 0;
	public double begBalLk = 0;
	public double endBalLk = 0;
	public String overpayLockStaDate = "";
	public String overpayLockDueDate = "";
	public double tempUnbillInterest = 0;
	public double unbillBegBalAf = 0;
	public double unbillBegBalLf = 0;
	public double unbillBegBalCf = 0;
	public double unbillBegBalPf = 0;
	public double unbillBegBalBl = 0;
	public double unbillBegBalCa = 0;
	public double unbillBegBalIt = 0;
	public double unbillBegBalId = 0;
	public double unbillBegBalRi = 0;
	public double unbillBegBalPn = 0;
	public double unbillBegBalAo = 0;
	public double unbillBegBalAi = 0;
	public double unbillBegBalSf = 0;
	public double unbillBegBalDp = 0;
	public double unbillBegBalCb = 0;
	public double unbillBegBalCi = 0;
	public double unbillBegBalCc = 0;
	public double unbillBegBalDb = 0;
	public double unbillEndBalAf = 0;
	public double unbillEndBalLf = 0;
	public double unbillEndBalCf = 0;
	public double unbillEndBalPf = 0;
	public double unbillEndBalBl = 0;
	public double unbillEndBalCa = 0;
	public double unbillEndBalIt = 0;
	public double unbillEndBalId = 0;
	public double unbillEndBalRi = 0;
	public double unbillEndBalPn = 0;
	public double unbillEndBalAo = 0;
	public double unbillEndBalAi = 0;
	public double unbillEndBalSf = 0;
	public double unbillEndBalDp = 0;
	public double unbillEndBalCb = 0;
	public double unbillEndBalCi = 0;
	public double unbillEndBalCc = 0;
	public double unbillEndBalDb = 0;
	public double billedBegBalAf = 0;
	public double billedBegBalLf = 0;
	public double billedBegBalCf = 0;
	public double billedBegBalPf = 0;
	public double billedBegBalBl = 0;
	public double billedBegBalCa = 0;
	public double billedBegBalIt = 0;
	public double billedBegBalId = 0;
	public double billedBegBalRi = 0;
	public double billedBegBalPn = 0;
	public double billedBegBalAo = 0;
	public double billedBegBalAi = 0;
	public double billedBegBalSf = 0;
	public double billedBegBalDp = 0;
	public double billedBegBalCb = 0;
	public double billedBegBalCi = 0;
	public double billedBegBalCc = 0;
	public double billedBegBalDb = 0;
	public double billedEndBalAf = 0;
	public double billedEndBalLf = 0;
	public double billedEndBalCf = 0;
	public double billedEndBalPf = 0;
	public double billedEndBalBl = 0;
	public double billedEndBalCa = 0;
	public double billedEndBalIt = 0;
	public double billedEndBalId = 0;
	public double billedEndBalRi = 0;
	public double billedEndBalPn = 0;
	public double billedEndBalAo = 0;
	public double billedEndBalAi = 0;
	public double billedEndBalSf = 0;
	public double billedEndBalDp = 0;
	public double billedEndBalCb = 0;
	public double billedEndBalCi = 0;
	public double billedEndBalCc = 0;
	public double billedEndBalDb = 0;
	public double minPay = 0;
	public double minPayBal = 0;
	public double rcMinPay = 0;
	public double rcMinPayBal = 0;
	public double rcMinPayM0 = 0;
	public double autopayBegAmt = 0;
	public double autopayBal = 0;
	public double payByStageAmt = 0;
	public double payByStageBal = 0;
	public String payByStageDate = "";
	public String paymentStatus = "";
	public String lastPaymentDate = "";
	public String lastMinPayDate = "";
	public String lastCancelDebtDate = "";
	public String stmtPaymentNo = "";
	public String stmtCycleDate = "";
	public String stmtAutoPayBank = "";
	public String stmtAutoPayNo = "";
	public String stmtAutoPayDate = "";
	public double stmtAutoPayAmt = 0;
	public double stmtCreditLimit = 0;
	public double stmtRevolRate = 0;
	public double stmtLastTtl = 0;
	public double stmtPaymentAmt = 0;
	public double stmtAdjustAmt = 0;
	public double stmtNewAmt = 0;
	public double stmtThisTtlAmt = 0;
	public double stmtMp = 0;
	public String stmtLastPayday = "";
	public double stmtLastMonthBp = 0;
	public double stmtNewAddBp = 0;
	public double stmtAdjustBp = 0;
	public double stmtUseBp = 0;
	public double stmtGiveBp = 0;
	public double stmtNetBp = 0;
	public double stmtEraseBp = 0;
	public String stmtEraseDate = "";
	public String stmtGiveReason1 = "";
	public String stmtGiveReason2 = "";
	public String stmtGiveReason3 = "";
	public String stmtGiveReason4 = "";
	public double stmtOverDueAmt = 0;
	public String stmtGoldCard = "";
	public double billInterest = 0;
	public double ttlAmtBal = 0;
	public double adiBegBal = 0;
	public double adiEndBal = 0;
	public double adiDAvail = 0;
	public String delaypayOkFlag = "";
	public double waiveTtlBal = 0;
	public String chgCycleFlag = "";
	public double unbillBegBalOt = 0;
	public double billedBegBalOt = 0;
	public double unbillEndBalOt = 0;
	public double billedEndBalOt = 0;
	public String revolveIntSign = "";
	public double revolveIntRate = 0;
	public String revolveRateSMonth = "";
	public String revolveRateEMonth = "";
	public String runEMonth = "";
	public String comboIndicator = "";
	public String comboAcctNo = "";
	public String revolveIntSign2 = "";
	public double revolveIntRate2 = 0;
	public String revolveRateSMonth2 = "";
	public String revolveRateEMonth2 = "";
	public String runEMonth2 = "";
	public int runEMonthCnt = 0;
	public double unbillBegBalDbB = 0;
	public double unbillBegBalDbC = 0;
	public double unbillBegBalDbI = 0;
	public double billedBegBalDbB = 0;
	public double billedBegBalDbC = 0;
	public double billedBegBalDbI = 0;
	public double unbillEndBalDbB = 0;
	public double unbillEndBalDbC = 0;
	public double unbillEndBalDbI = 0;
	public double billedEndBalDbB = 0;
	public double billedEndBalDbC = 0;
	public double billedEndBalDbI = 0;
	public String aprFlag = "";
	public String aprDate = "";
	public String aprUser = "";
	public String crtDate = "";
	public String crtTime = "";
	public String crtUser = "";

	@Override
	public void initData() {

		pSeqno = "";
		acctMonth = "";
		acctType = "";
		corpPSeqno = "";
		idPSeqno = "";
		stmtCycle = "";
		acctJrnlBal = 0;
		begBalOp = 0;
		endBalOp = 0;
		begBalLk = 0;
		endBalLk = 0;
		overpayLockStaDate = "";
		overpayLockDueDate = "";
		tempUnbillInterest = 0;
		unbillBegBalAf = 0;
		unbillBegBalLf = 0;
		unbillBegBalCf = 0;
		unbillBegBalPf = 0;
		unbillBegBalBl = 0;
		unbillBegBalCa = 0;
		unbillBegBalIt = 0;
		unbillBegBalId = 0;
		unbillBegBalRi = 0;
		unbillBegBalPn = 0;
		unbillBegBalAo = 0;
		unbillBegBalAi = 0;
		unbillBegBalSf = 0;
		unbillBegBalDp = 0;
		unbillBegBalCb = 0;
		unbillBegBalCi = 0;
		unbillBegBalCc = 0;
		unbillBegBalDb = 0;
		unbillEndBalAf = 0;
		unbillEndBalLf = 0;
		unbillEndBalCf = 0;
		unbillEndBalPf = 0;
		unbillEndBalBl = 0;
		unbillEndBalCa = 0;
		unbillEndBalIt = 0;
		unbillEndBalId = 0;
		unbillEndBalRi = 0;
		unbillEndBalPn = 0;
		unbillEndBalAo = 0;
		unbillEndBalAi = 0;
		unbillEndBalSf = 0;
		unbillEndBalDp = 0;
		unbillEndBalCb = 0;
		unbillEndBalCi = 0;
		unbillEndBalCc = 0;
		unbillEndBalDb = 0;
		billedBegBalAf = 0;
		billedBegBalLf = 0;
		billedBegBalCf = 0;
		billedBegBalPf = 0;
		billedBegBalBl = 0;
		billedBegBalCa = 0;
		billedBegBalIt = 0;
		billedBegBalId = 0;
		billedBegBalRi = 0;
		billedBegBalPn = 0;
		billedBegBalAo = 0;
		billedBegBalAi = 0;
		billedBegBalSf = 0;
		billedBegBalDp = 0;
		billedBegBalCb = 0;
		billedBegBalCi = 0;
		billedBegBalCc = 0;
		billedBegBalDb = 0;
		billedEndBalAf = 0;
		billedEndBalLf = 0;
		billedEndBalCf = 0;
		billedEndBalPf = 0;
		billedEndBalBl = 0;
		billedEndBalCa = 0;
		billedEndBalIt = 0;
		billedEndBalId = 0;
		billedEndBalRi = 0;
		billedEndBalPn = 0;
		billedEndBalAo = 0;
		billedEndBalAi = 0;
		billedEndBalSf = 0;
		billedEndBalDp = 0;
		billedEndBalCb = 0;
		billedEndBalCi = 0;
		billedEndBalCc = 0;
		billedEndBalDb = 0;
		minPay = 0;
		minPayBal = 0;
		rcMinPay = 0;
		rcMinPayBal = 0;
		rcMinPayM0 = 0;
		autopayBegAmt = 0;
		autopayBal = 0;
		payByStageAmt = 0;
		payByStageBal = 0;
		payByStageDate = "";
		paymentStatus = "";
		lastPaymentDate = "";
		lastMinPayDate = "";
		lastCancelDebtDate = "";
		stmtPaymentNo = "";
		stmtCycleDate = "";
		stmtAutoPayBank = "";
		stmtAutoPayNo = "";
		stmtAutoPayDate = "";
		stmtAutoPayAmt = 0;
		stmtCreditLimit = 0;
		stmtRevolRate = 0;
		stmtLastTtl = 0;
		stmtPaymentAmt = 0;
		stmtAdjustAmt = 0;
		stmtNewAmt = 0;
		stmtThisTtlAmt = 0;
		stmtMp = 0;
		stmtLastPayday = "";
		stmtLastMonthBp = 0;
		stmtNewAddBp = 0;
		stmtAdjustBp = 0;
		stmtUseBp = 0;
		stmtGiveBp = 0;
		stmtNetBp = 0;
		stmtEraseBp = 0;
		stmtEraseDate = "";
		stmtGiveReason1 = "";
		stmtGiveReason2 = "";
		stmtGiveReason3 = "";
		stmtGiveReason4 = "";
		stmtOverDueAmt = 0;
		stmtGoldCard = "";
		billInterest = 0;
		ttlAmtBal = 0;
		adiBegBal = 0;
		adiEndBal = 0;
		adiDAvail = 0;
		delaypayOkFlag = "";
		waiveTtlBal = 0;
		chgCycleFlag = "";
		unbillBegBalOt = 0;
		billedBegBalOt = 0;
		unbillEndBalOt = 0;
		billedEndBalOt = 0;
		revolveIntSign = "";
		revolveIntRate = 0;
		revolveRateSMonth = "";
		revolveRateEMonth = "";
		runEMonth = "";
		comboIndicator = "";
		comboAcctNo = "";
		revolveIntSign2 = "";
		revolveIntRate2 = 0;
		revolveRateSMonth2 = "";
		revolveRateEMonth2 = "";
		runEMonth2 = "";
		runEMonthCnt = 0;
		unbillBegBalDbB = 0;
		unbillBegBalDbC = 0;
		unbillBegBalDbI = 0;
		billedBegBalDbB = 0;
		billedBegBalDbC = 0;
		billedBegBalDbI = 0;
		unbillEndBalDbB = 0;
		unbillEndBalDbC = 0;
		unbillEndBalDbI = 0;
		billedEndBalDbB = 0;
		billedEndBalDbC = 0;
		billedEndBalDbI = 0;
		aprFlag = "";
		aprDate = "";
		aprUser = "";
		crtDate = "";
		crtTime = "";
		crtUser = "";

		rowid = "";

	}

}
