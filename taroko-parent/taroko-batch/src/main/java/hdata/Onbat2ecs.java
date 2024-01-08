/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package hdata;

public class Onbat2ecs extends hdata.BaseBin {
	public String transType = "";
	public int toWhich = 0;
	public String dog = "";
	public String dop = "";
	public String procMode = "";
	public int procStatus = 0;
	public String cardIndicator = "";
	public String paymentType = "";
	public String acctType = "";
	public String cardHldrId = "";
	public String idPSeqno = "";
	public String cardAcctId = "";
	public String pSeqno = "";
	public String cardNo = "";
	public String oldCardNo = "";
	public double creditLimit = 0;
	public String transDate = "";
	public double transAmt = 0;
	public String mccCode = "";
	public String isoRespCode = "";
	public String icbcRespCode = "";
	public String icbcRespDesc = "";
	public String cardValidFrom = "";
	public String cardValidTo = "";
	public String oppType = "";
	public String oppReason = "";
	public String oppDate = "";
	public String isRenew = "";
	public String isEm = "";
	public String isRc = "";
	public int cycleCreditDate = 0;
	public int currTotLostAmt = 0;
	public String procDate = "";
	public String cardLaunchType = "";
	public String cardLaunchDate = "";
	public String cvc2Code = "";
	public String activePin = "";
	public String voicePin = "";
	public String authCode = "";
	public String transCode = "";
	public String refeNo = "";
	public String matchFlag = "";
	public String matchDate = "";
	public String teleNo = "";
	public String contractNo = "";
	public String blockReason1 = "";
	public String blockReason2 = "";
	public String blockReason3 = "";
	public String blockReason4 = "";
	public String blockReason5 = "";
	public double ibmReceiveAmt = 0;
	public double creditLimitCash = 0;
	public String acctNo = "";
	public String acctNoOld = "";
	public String mailBranch = "";
	public String lostFeeFlag = "";
	public String debitFlag = "";

	@Override
	public void initData() {
		transType = "";
		toWhich = 0;
		dog = "";
		dop = "";
		procMode = "";
		procStatus = 0;
		cardIndicator = "";
		paymentType = "";
		acctType = "";
		cardHldrId = "";
		idPSeqno = "";
		cardAcctId = "";
		pSeqno = "";
		cardNo = "";
		oldCardNo = "";
		creditLimit = 0;
		transDate = "";
		transAmt = 0;
		mccCode = "";
		isoRespCode = "";
		icbcRespCode = "";
		icbcRespDesc = "";
		cardValidFrom = "";
		cardValidTo = "";
		oppType = "";
		oppReason = "";
		oppDate = "";
		isRenew = "";
		isEm = "";
		isRc = "";
		cycleCreditDate = 0;
		currTotLostAmt = 0;
		procDate = "";
		cardLaunchType = "";
		cardLaunchDate = "";
		cvc2Code = "";
		activePin = "";
		voicePin = "";
		authCode = "";
		transCode = "";
		refeNo = "";
		matchFlag = "";
		matchDate = "";
		teleNo = "";
		contractNo = "";
		blockReason1 = "";
		blockReason2 = "";
		blockReason3 = "";
		blockReason4 = "";
		blockReason5 = "";
		ibmReceiveAmt = 0;
		creditLimitCash = 0;
		acctNo = "";
		acctNoOld = "";
		mailBranch = "";
		lostFeeFlag = "";
		debitFlag = "";
		rowid = "";
	}

}
