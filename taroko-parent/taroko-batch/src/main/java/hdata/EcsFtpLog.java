
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package hdata;

public class EcsFtpLog extends BaseBin {
	public String crtDate = "";
	public String crtTime = "";
	public String ftptype = "";
	public String refIpCode = "";
	public String fileName = "";
	public String transType = "";
	public String remoteAddr = "";
	public String transMode = "";
	public String transData = "";
	public int transSeq = 0;
	public double localSize = 0;
	public double transSize = 0;
	public String transDesc = "";
	public double transTotalCnt = 0;
	public String systemId = "";
	public String groupId = "";
	public String sourceFrom = "";
	public String procSeqno = "";
	public String fileDate = "";
	public String transSeqno = "";
	public String transRespCode = "";
	public String localDir = "";
	public String fileZipHidewd = "";
	public String fileUnzipHidewd = "";
	public String procCode = "";
	public String procDesc = "";
	public String aprDate = "";
	public String aprUser = "";

	@Override
	public void initData() {
		crtDate = "";
		crtTime = "";
		ftptype = "";
		refIpCode = "";
		fileName = "";
		transType = "";
		remoteAddr = "";
		transMode = "";
		transData = "";
		transSeq = 0;
		localSize = 0;
		transSize = 0;
		transDesc = "";
		transTotalCnt = 0;
		systemId = "";
		groupId = "";
		sourceFrom = "";
		procSeqno = "";
		fileDate = "";
		transSeqno = "";
		transRespCode = "";
		localDir = "";
		fileZipHidewd = "";
		fileUnzipHidewd = "";
		procCode = "";
		procDesc = "";
		aprDate = "";
		aprUser = "";
		rowid = "";
	}

}
