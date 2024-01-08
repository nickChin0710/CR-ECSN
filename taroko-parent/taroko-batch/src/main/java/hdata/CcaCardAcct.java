                                                                               
/***************************************************************************** 
*                                                                            * 
*                              MODIFICATION LOG                              * 
*                                                                            * 
* DATE       Version   AUTHOR      DESCRIPTION                               * 
* ---------  --------  ---------- ------------------------------------------ * 
* 109-07-08  V1.00  yanghan  修改了變量名稱和方法名稱*                                     
******************************************************************************/
package hdata;

public class CcaCardAcct extends BaseBin {

	public String acnoPSeqno = "";
	public String pSeqno = "";
	public String debitFlag = "";
	public String acnoFlag = "";
	public String acctType = "";
	public String idPSeqno = "";
	public String corpPSeqno = "";
	public double cardAcctIdx = 0;
	public String ccasMcode = "";
	public String mcodeValidDate = "";
	public String mcodeChgDate = "";
	public String mcodeChgTime = "";
	public String mcodeChgUser = "";
	public String blockDate = "";
	public String unblockDate = "";
	public String blockStatus = "";
	public String blockReason1 = "";
	public String blockReason2 = "";
	public String blockReason3 = "";
	public String blockReason4 = "";
	public String blockReason5 = "";
	public String specStatus = "";
	public String specDate = "";
	public String specUser = "";
	public String specDelDate = "";
	public double specMstVipAmt = 0;
	public String specRemark = "";
	public String adjQuota = "";
	public String adjEffStartDate = "";
	public String adjEffEndDate = "";
	public String adjReason = "";
	public String adjRemark = "";
	public String adjArea = "";
	public String adjDate = "";
	public String adjTime = "";
	public String adjUser = "";
	public String adjAprUser = "";
	public double adjInstPct = 0;
	public String adjMemo = "";
	public double totAmtMonth = 0;
	public double totAmtConsume = 0;
	public double totAmtPrecash = 0;
	public double txTotAmtMonth = 0;
	public double txTotCntMonth = 0;
	public double txTotAmtDay = 0;
	public double txTotCntDay = 0;
	public double fnTotAmtMonth = 0;
	public double fnTotCntMonth = 0;
	public double fnTotAmtDay = 0;
	public double fnTotCntDay = 0;
	public double fcTotAmtMonth = 0;
	public double fcTotCntMonth = 0;
	public double fcTotAmtDay = 0;
	public double fcTotCntDay = 0;
	public double rejAuthCntMonth = 0;
	public String lastConsumeDate = "";
	public double trainTotAmtMonth = 0;
	public double trainTotAmtDay = 0;
	public String lastAuthCode = "";
	public String authRemark = "";
	public String organId = "";
	public String noticeFlag = "";
	public String noticeSndDate = "";
	public String nocancelCreditFlag = "";
	public String smsCellPhone = "";
	public String adjRiskFlag = "";
	public String ccasClassCode = "";
	public String classValidDate = "";
	public String classChgDate = "";
	public String classChgTime = "";
	public String classChgUser = "";
	public String crtDate = "";
	public String crtUser = "";
	public String aprDate = "";
	public String aprUser = "";

	@Override
	public void initData() {
		acnoPSeqno = "";
		pSeqno = "";
		debitFlag = "";
		acnoFlag = "";
		acctType = "";
		idPSeqno = "";
		corpPSeqno = "";
		cardAcctIdx = 0;
		ccasMcode = "";
		mcodeValidDate = "";
		mcodeChgDate = "";
		mcodeChgTime = "";
		mcodeChgUser = "";
		blockDate = "";
		unblockDate = "";
		blockStatus = "";
		blockReason1 = "";
		blockReason2 = "";
		blockReason3 = "";
		blockReason4 = "";
		blockReason5 = "";
		specStatus = "";
		specDate = "";
		specUser = "";
		specDelDate = "";
		specMstVipAmt = 0;
		specRemark = "";
		adjQuota = "";
		adjEffStartDate = "";
		adjEffEndDate = "";
		adjReason = "";
		adjRemark = "";
		adjArea = "";
		adjDate = "";
		adjTime = "";
		adjUser = "";
		adjAprUser = "";
		adjInstPct = 0;
		adjMemo = "";
		totAmtMonth = 0;
		totAmtConsume = 0;
		totAmtPrecash = 0;
		txTotAmtMonth = 0;
		txTotCntMonth = 0;
		txTotAmtDay = 0;
		txTotCntDay = 0;
		fnTotAmtMonth = 0;
		fnTotCntMonth = 0;
		fnTotAmtDay = 0;
		fnTotCntDay = 0;
		fcTotAmtMonth = 0;
		fcTotCntMonth = 0;
		fcTotAmtDay = 0;
		fcTotCntDay = 0;
		rejAuthCntMonth = 0;
		lastConsumeDate = "";
		trainTotAmtMonth = 0;
		trainTotAmtDay = 0;
		lastAuthCode = "";
		authRemark = "";
		organId = "";
		noticeFlag = "";
		noticeSndDate = "";
		nocancelCreditFlag = "";
		smsCellPhone = "";
		adjRiskFlag = "";
		ccasClassCode = "";
		classValidDate = "";
		classChgDate = "";
		classChgTime = "";
		classChgUser = "";
		crtDate = "";
		crtUser = "";
		aprDate = "";
		aprUser = "";

		rowid = "";
	}

	public String blockCode() {
		return blockReason1 + blockReason2 + blockReason3 + blockReason4 + blockReason5;
	}

}
