/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-07-207  V1.00  yanghan       修改了變量名稱和方法名稱*
***************************************************************************/
package hdata;

public abstract class BaseBin {

	public String modUser = "";
	public String modTime = "";
	public String modPgm = "";
	public double modSeqno = 0;
	public String rowid = "";

	public abstract void initData();

	public void initMod() {
		modUser = "";
		modTime = "";
		modPgm = "";
		modSeqno = 0;
		rowid = "";
	}

}
