
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package hdata;

public class EcsRefIpAddr extends BaseBin {

	public String refIp = "";
	public String refIpCode = "";
	public String refName = "";
	public String userId = "";
	public String userHidewd = "";
	public String transType = "";
	public String remoteDir = "";
	public String localDir = "";
	public String portNo = "";
	public String fileZipHidewd = "";
	public String fileUnzipHidewd = "";
	public String crtDate = "";
	public String crtUser = "";
	public String aprDate = "";
	public String aprUser = "";

	@Override
	public void initData() {
		refIp = "";
		refIpCode = "";
		refName = "";
		userId = "";
		userHidewd = "";
		transType = "";
		remoteDir = "";
		localDir = "";
		portNo = "";
		fileZipHidewd = "";
		fileUnzipHidewd = "";
		crtDate = "";
		crtUser = "";
		aprDate = "";
		aprUser = "";
		rowid = "";
	}

}
