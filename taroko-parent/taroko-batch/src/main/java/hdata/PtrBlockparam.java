/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package hdata;

public class PtrBlockparam extends BaseBin {

	public String paramType = "";
	public String acctType = "";
	public String validDate = "";
	public String execMode = "";
	public String aprFlag = "";
	public String pauseFlag = "";
	public int execDay = 0;
	public int execCycleNday = 0;
	public String execDate = "";
	public int n0Month = 0;
	public int n1Cycle = 0;
	public int mcodeValue1 = 0;
	public double debtAmt1 = 0;
	public int mcodeValue2 = 0;
	public double debtAmt2 = 0;
	public int mcodeValue3 = 0;
	public double debtAmt3 = 0;
	public int mcodeValue4 = 0;
	public double debtAmt4 = 0;
	public String execFlagM1 = "";
	public String execFlagM2 = "";
	public String execFlagM3 = "";
	public double debtFee3 = 0;
	public String execFlagM14 = "";
	public int mcodeValue14 = 0;
	public double debtAmt14 = 0;
	public String blockReason14 = "";
	public String execFlagM24 = "";
	public int mcodeValue24 = 0;
	public double debtAmt24 = 0;
	public String blockReason24 = "";
	public String execFlagM34 = "";
	public int mcodeValue34 = 0;
	public double debtAmt34 = 0;
	public double debtFee34 = 0;
	public String blockReason34 = "";
	public String aprDate = "";
	public String aprUser = "";

	@Override
	public void initData() {
		paramType = "";
		acctType = "";
		validDate = "";
		execMode = "";
		aprFlag = "";
		pauseFlag = "";
		execDay = 0;
		execCycleNday = 0;
		execDate = "";
		n0Month = 0;
		n1Cycle = 0;
		mcodeValue1 = 0;
		debtAmt1 = 0;
		mcodeValue2 = 0;
		debtAmt2 = 0;
		mcodeValue3 = 0;
		debtAmt3 = 0;
		mcodeValue4 = 0;
		debtAmt4 = 0;
		execFlagM1 = "";
		execFlagM2 = "";
		execFlagM3 = "";
		debtFee3 = 0;
		execFlagM14 = "";
		mcodeValue14 = 0;
		debtAmt14 = 0;
		blockReason14 = "";
		execFlagM24 = "";
		mcodeValue24 = 0;
		debtAmt24 = 0;
		blockReason24 = "";
		execFlagM34 = "";
		mcodeValue34 = 0;
		debtAmt34 = 0;
		debtFee34 = 0;
		blockReason34 = "";
		aprDate = "";
		aprUser = "";

		rowid = "";
	}

}
