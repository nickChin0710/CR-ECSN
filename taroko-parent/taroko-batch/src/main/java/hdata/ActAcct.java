/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-07-07  V1.00  yanghan       修改了變量名稱和方法名稱*
***************************************************************************/
package hdata;

public class ActAcct extends BaseBin {
	public String pSeqno = "";
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
	public double tempUnbillPenalty = 0;
	public double tempAdiInterest = 0;
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
	public double payAmt = 0;
	public int payCnt = 0;
	public double adjustDrAmt = 0;
	public double adjustCrAmt = 0;
	public int adjustDrCnt = 0;
	public int adjustCrCnt = 0;
	public String lastPaymentDate = "";
	public int paymentPercentage = 0;
	public String lastPostedDate = "";
	public String lastMinPayDate = "";
	public String lastCancelDebtDate = "";
	public double lastTtlAmt = 0;
	public double ttlAmt = 0;
	public double ttlAmtBal = 0;
	public double adiBegBal = 0;
	public double adiEndBal = 0;
	public double adiDAvail = 0;
	public double aoFeeBal = 0;
	public String delaypayOkFlag = "";
	public String crtDate = "";
	public String crtUser = "";
	public String aprFlag = "";
	public String aprDate = "";
	public String aprUser = "";
	public String crtTime = "";
	public String updateDate = "";
	public String updateUser = "";

	@Override
	public void initData() {

		pSeqno = "";
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
		tempUnbillPenalty = 0;
		tempAdiInterest = 0;
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
		payAmt = 0;
		payCnt = 0;
		adjustDrAmt = 0;
		adjustCrAmt = 0;
		adjustDrCnt = 0;
		adjustCrCnt = 0;
		lastPaymentDate = "";
		paymentPercentage = 0;
		lastPostedDate = "";
		lastMinPayDate = "";
		lastCancelDebtDate = "";
		lastTtlAmt = 0;
		ttlAmt = 0;
		ttlAmtBal = 0;
		adiBegBal = 0;
		adiEndBal = 0;
		adiDAvail = 0;
		aoFeeBal = 0;
		delaypayOkFlag = "";
		crtDate = "";
		crtUser = "";
		aprFlag = "";
		aprDate = "";
		aprUser = "";
		crtTime = "";
		updateDate = "";
		updateUser = "";

		rowid = "";

	}

}
