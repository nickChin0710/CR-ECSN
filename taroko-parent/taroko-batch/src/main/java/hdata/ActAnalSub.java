/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-07-07  V1.00  yanghan       修改了變量名稱和方法名稱*
***************************************************************************/
package hdata;

public class ActAnalSub extends BaseBin {

	public String pSeqno = "";
	public String acctMonth = "";
	public String acctType = "";
	public int hisPurchaseCnt = 0;
	public double hisPurchaseAmt = 0;
	public double hisPurNoM2 = 0;
	public int hisCashCnt = 0;
	public double hisCashAmt = 0;
	public int hisPayPercentage = 0;
	public int hisRcPercentage = 0;
	public double hisPayAmt = 0;
	public int hisPayCnt = 0;
	public double hisAdjDrAmt = 0;
	public double hisAdjCrAmt = 0;
	public int hisAdjDrCnt = 0;
	public int hisAdjCrCnt = 0;
	public int noConsumeMonth = 0;
	public String updateDate = "";
	public String updateUser = "";
	public double hisComboCashAmt = 0;
	public double hisComboCashFee = 0;
	public int comboCashLimit = 0;
	public int unpostInstallment = 0;

	@Override
	public void initData() {
		pSeqno = "";
		acctMonth = "";
		acctType = "";
		hisPurchaseCnt = 0;
		hisPurchaseAmt = 0;
		hisPurNoM2 = 0;
		hisCashCnt = 0;
		hisCashAmt = 0;
		hisPayPercentage = 0;
		hisRcPercentage = 0;
		hisPayAmt = 0;
		hisPayCnt = 0;
		hisAdjDrAmt = 0;
		hisAdjCrAmt = 0;
		hisAdjDrCnt = 0;
		hisAdjCrCnt = 0;
		noConsumeMonth = 0;
		updateDate = "";
		updateUser = "";
		hisComboCashAmt = 0;
		hisComboCashFee = 0;
		comboCashLimit = 0;
		unpostInstallment = 0;
		rowid = "";
	}

}
