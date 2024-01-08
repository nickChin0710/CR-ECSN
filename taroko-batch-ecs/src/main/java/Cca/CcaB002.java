/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 109-07-03  V1.00.0     Ryan     initial                                    *
* 110-01-21  V1.00.1     Ryan     update tsc2Iso()                           *
* 110/02/24  V1.00.02    Wilson   mark and send_times < 5                    *
* 110/09/24  V1.00.03    Castor   Add HCE SOAP-switching                     *
* 110/10/12  V1.00.04    Justin   get parameters from config files           *
* 110/10/19  V1.00.05    Wilson   http改讀參數                                                                                            *
* 110/10/28  V1.00.06    Wilson   pkiAcdpFileName讀取的檔名、路徑更改                                       *
* 110/11/05  V1.00.07    Justin   System.out->log and continue to run when encounter timeout *
* 110/11/16  V1.00.08    Justin   change to use getEcsAcdpPath()             *
* 110/11/29  V1.00.09    Ryan     update oempay isoField[14]                 *
* 110/11/29  V1.00.10    Justin   load certificates to prevent PXIX Exception*
* 110/12/01  V1.00.10    Ryan     update oempay iso                           *
* 110/12/16  V1.00.11    Ryan     oempay2Iso 新增  token_requestor_id,account_number_ref 欄位     *
* 110/12/17  V1.00.12    Ryan     oempay2Iso account_number_ref --> t_c_identifier     *
* 110/12/29  V1.00.13    Ryan     update visa iso 127                         *  
* 111/02/11  V1.00.14    Justin   throw exception when processing TWMP       *
* 111/02/22  V1.00.15    Ryan     add commitDataBase();       *
* 111/02/23  V1.00.16    Ryan     resp_code <> 00 to resp_code = 99       *
* 111/03/08  V1.00.17    Ryan     add sleep       *
* 111/03/10  V1.00.18    Ryan     add HttpHostConnectException            *
* 111/03/22  V1.00.19    Justin   pass cert verification                   *
* 111/03/29  V1.00.20    Ryan     增加異常參數 處理筆數上限                                                                  *
* 111/03/30  V1.00.21    Ryan     where 條件調整                                                                  *
* 111/04/06  V1.00.22    Ryan     JCB gate.isoField[120] ==>  不帶空白                           *
* 111/07/05  V1.00.23    Ryan     sysGmtDatetime 調整為24小時制                                                  *
* 111/12/13  V1.00.24    Ryan     財金規格調整,oempay送黑名單欄位2由原本帶虛擬卡號改為帶實體卡號                                *
* 112/02/15  V1.00.25    Ryan     取消EAI、CRSHCE06 新增HCEECS02             *
* 112/04/12  V1.00.26    Ryan     合併P2與P3程式，抓取參數判別要走P2或P3邏輯             *
* 112/05/23  V1.00.27    Ryan     http host port 改為抓取資料庫參數             *
* 112/11/10  V1.00.28    Ryan     新增TWMP_ONE_WAY_ENDPOINT_REQUEST_P2_XML             
* 112/11/22  V1.00.29    Ryan     修正P2 cca_outgoing.resp_code = 99問題           *
*****************************************************************************/

package Cca;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.BaseBatch;
import com.CommCrd;
import com.CommFunction;
import com.tcb.ap4.tool.Decryptor;

import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGate;
import bank.AuthIntf.AuthGateway;
import bank.AuthIntf.FhmFormat;
import bank.AuthIntf.NegFormat;

import Dxc.Util.SecurityUtil;



public class CcaB002 extends BaseBatch {
	private static final String TWMP_ONE_WAY_ENDPOINT_REQUEST_XML = "twmpOneWayEndpoint_request.xml";
	private static final String TWMP_ONE_WAY_ENDPOINT_REQUEST_P2_XML = "twmpOneWayEndpoint_request_p2.xml";
	private static final String EAI_ENDPOINT_REQUEST_XML = "eaiEndpoint_request.xml";
	private static final String TWMP = "TWMP";
	private static final String USER_ID = "ecs";
	private String PROGNAME = "每分鐘outgoing失敗重送處理程式 112/11/22 V.00.29";
	AuthGate gate = new AuthGate();
	CommFunction   comm  = new CommFunction();
	CommCrd        comc  = new CommCrd();
	
	private SSLConnectionSocketFactory sslsf = null;
	
	String strBkIca = "3768";
	private String rowid = "";
	private String cardNo = "";
	private String vCardNo = "";
	private String keyValue = "";
	private String bitmap = "";
	private String actCode = "";
	private String reasonCode = "";
	private String vmjRegnData = "";
	private String binType = "";
	private String vipAmt = "";
	private String currentCode = "";
	private String delDate = "";
	private String newEndDate = "";
	private String electronicCardno = "";
	private boolean isDebit = false;
	protected com.CommString zzstr = new com.CommString();
	private String authIp = "";
	private String authPortNo = "";
	public String respCode = "";
	private String tokenRequestorId = "";
	private String tCIdentifier = "";
	String oRespCode = "";
	String respData = "";
	int commit = 1;
	String crtDate = "";
	
	public String respCo = "";
	public String oUTSIR = "";
	public int errorCnt = 0;
	public int wfErrorcnt = 0;
	public String wfErrorCode = "";
	public String wfValueP2 = "";
	int totalCnt2 = 0;
	int updateTotalCnt1 = 0;
	int updateTotalCnt2 = 0;
	String wssHost = "";
	String wssPort = "";
	String wfValue1 = "";
	String wfValue2 = "";
	String wfValue3 = "";
	String wfValue4 = "";
	int errCnt = 0;
	public String mesgType() {
		return gate.mesgType;
	}

	public String getIsoString() {
		return gate.isoString.substring(2);
	}

	// =****************************************************************************
	public static void main(String[] args) {
		CcaB002 proc = new CcaB002();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

	// =****************************************************************************
	public int mainProcess(String[] args) {
		try {
			long startTime = System.currentTimeMillis() ;
			dateTime();
			
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME + "," + args.length);
			if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
				return (0);
			}

			int liArg = args.length;
			if (liArg > 1) {
				return (0);
			}

			if (liArg == 1) {
				crtDate = args[0];
			}

            
			dbConnect();
			selectPtrSysParm();
			selectPtrSysParmP2();
			if(!selectPtrSysParm2()) {		
				return (0);
			}
			selectCcaoOutgoingCnt();
			selectCcaoOutgoing();
			

//			printf("Total process record:[%s]", totalCnt);
			printf("CCA_OUTGOING TOTAL CNT = [%s]", totalCnt2);
			printf("[1] UPDATE CCA_OUTGOING TOTAL CNT = [%s]", updateTotalCnt1);
			printf("[2] UPDATE CCA_OUTGOING TOTAL CNT = [%s]", updateTotalCnt2);

			commitDataBase();

			long endTime = durationTime(startTime+"")-1;
			if(endTime<30) {
				showLogMessage("I", "", "sleep "+(30-endTime)+" sec");
				TimeUnit.SECONDS.sleep(30-endTime);
			}
	
			endProgram();

			finalProcess();
			return (0);
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	// =****************************************************************************
	@Override
	protected void dataProcess(String[] args) throws Exception {

	}
	
	
	void selectPtrSysParm() throws Exception {
		String strSql = "SELECT WF_VALUE,WF_VALUE2 FROM PTR_SYS_PARM WHERE WF_PARM = 'SYSPARM' AND WF_KEY = 'CCAB002'";
		sqlSelect(strSql);
		if (sqlNrow > 0) {
			wfErrorcnt = colInt("WF_VALUE");
			wfErrorCode = colSs("WF_VALUE2");
		}
		printf("wfErrorcnt = [ " + wfErrorcnt + " ] , wfErrorCode = [ " +wfErrorCode + " ]");
	}
	
	boolean selectPtrSysParm2() throws Exception {
		String strSql = "SELECT WF_VALUE,WF_VALUE2,WF_VALUE3,WF_VALUE4 FROM PTR_SYS_PARM WHERE WF_KEY = 'TWMP'";
		sqlSelect(strSql);
		if (sqlNrow < 0) {
			printf("查無參數 PTR_SYS_PARM WHERE WF_KEY = 'TWMP' ");
			return false;
		}
		wfValue1 = colSs("WF_VALUE");
		wfValue2 = colSs("WF_VALUE2");
		wfValue3 = colSs("WF_VALUE3");
		wfValue4 = colSs("WF_VALUE4");
		wssHost = wfValue1;
		wssPort = wfValue4;
		showLogMessage("I", "", "取得連線參數"); 
		showLogMessage("I", "", "wssHost = [" + wssHost + "]");
		showLogMessage("I", "", "wssPort = [" + wssPort + "]");
		return true;
	}

	/***
	 * WF_VALUE = Y  走P2的邏輯
	 * WF_VALUE = N  走P3的邏輯
	 * @throws Exception
	 */
	void selectPtrSysParmP2() throws Exception {
		String strSql = "SELECT WF_VALUE FROM PTR_SYS_PARM WHERE WF_PARM = 'SYSPARM' AND WF_KEY = 'ROLLBACK_P2'";
		sqlSelect(strSql);
		if (sqlNrow > 0) {
			wfValueP2 = colSs("WF_VALUE");
		}
		printf("wfValueP2 = [ " + wfValueP2 + " ]");
	}
	
	void selectCcaoOutgoingCnt() throws Exception{
		sqlCmd = " select count(*) as outgoing_cnt ";
		sqlCmd += " from cca_outgoing  ";
		sqlCmd += " where decode(resp_code,'','99',resp_code) = '99' ";		
		if(crtDate.trim().length()>0){
			sqlCmd += " and crt_date = '" + crtDate + "' ";
		}
		sqlCmd += " and key_value||key_table != 'FISCCARD_OPEN' ";// 開卡不送財金
		sqlSelect();
	
		totalCnt2 = colInt("outgoing_cnt");
		
	}

	void selectCcaoOutgoing() throws Exception {

		sqlCmd = " select hex(rowid) as rowid ";
		sqlCmd += " ,card_no ";
		sqlCmd += " ,key_value ";
		sqlCmd += " ,bitmap ";
		sqlCmd += " ,act_code ";
		sqlCmd += " ,reason_code ";
		sqlCmd += " ,vmj_regn_data ";
		sqlCmd += " ,bin_type ";
		sqlCmd += " ,vip_amt ";
		sqlCmd += " ,current_code ";
		sqlCmd += " ,del_date ";
		sqlCmd += " ,new_end_date ";
		sqlCmd += " ,electronic_card_no ";
		sqlCmd += " ,v_card_no ";
		sqlCmd += " ,resp_code ";
		sqlCmd += " from cca_outgoing  ";
//		sqlCmd += " where resp_code <> '00' ";
		if(zzstr.empty(wfErrorCode)) {
			sqlCmd += " where decode(resp_code,'','99',resp_code) = '99' ";
		}else {
			String[] whereErrCode =  wfErrorCode.split(",");
			sqlCmd += " where resp_code in ( ";
			for(int n = 0 ; n<whereErrCode.length;n++) {
				sqlCmd += "'" + whereErrCode[n] + "'";
				if(n == whereErrCode.length-1) {
					continue;
				}
				sqlCmd += ",";
			}
			sqlCmd += " ) ";
		}
//		sqlCmd += " and key_value ='TWMP'   ";	
//		sqlCmd += " and send_times < 5 ";		
		if(crtDate.trim().length()>0){
			sqlCmd += " and crt_date = '" + crtDate + "' ";
		}
		sqlCmd += " and key_value||key_table != 'FISCCARD_OPEN' ";// 開卡不送財金
		sqlCmd += " order by crt_date,crt_time ";
		
		this.openCursor();

		while (fetchTable()) {
			rowid = colSs("rowid");
			cardNo = colSs("card_no");
			keyValue = colSs("key_value");
			actCode = colSs("act_code");
			reasonCode = colSs("reason_code");
			vmjRegnData = colSs("vmj_regn_data");
			binType = colSs("bin_type");
			vipAmt = colSs("vip_amt");
			currentCode = colSs("current_code");
			delDate = colSs("del_date");
			newEndDate = colSs("new_end_date");
			electronicCardno = colSs("electronic_card_no");
			vCardNo = colSs("v_card_no");
			oRespCode = colSs("resp_code");

			isoFieldClear();
			switch (keyValue) {
			case "NCCC":
				nccc2Iso();
				break;
			case "FISC":
				fisc2Iso();
				break;
			case "TWMP":
//				twmp2Iso();
				if("Y".equals(wfValueP2))
					hce2TwmpP2();
				else {
					hce2Twmp();
				}	
				break;
			case "MASTER":
				master2Iso2();
				break;
			case "MASTER2":
				master2Iso();
				break;
			case "JCB":
				jcb2Iso();
				break;
			case "VISA":
				visa2Iso();
				break;
			case "TSCC":
				isDebit = this.isDebitcard(cardNo);
				tsc2Iso();
				break;
			case "IPASS":
				ips2Iso();
				break;
			case "ICASH":
				ich2Iso();
				break;
			case "OEMPAY":
				oempay2Iso();
				break;
			default:
				continue;
			}
			
			
			if(!keyValue.equals("TWMP")){
				int authCode = callAutoAuth(keyValue);
				commitDataBase();
				if(!oRespCode.equals(respCode)&&authCode==1) {
					totalCnt++;
					updateCcaOutgoing();
					commitDataBase();
				}
			}

//			if(!keyValue.equals("TWMP")  && callAutoAuth(keyValue)==1){
//				commitDataBase();
//				totalCnt++;
//				updateCcaOutgoing();
//				commitDataBase();
//			}
			
			if(wfErrorcnt > 0 && errorCnt == wfErrorcnt) {
				printf("service connection error , restart the program later ==========================================");
				break;
			}
		}
		this.closeCursor();
	}

	void updateCcaOutgoing() throws Exception {
//		if (!respCode.equals("00"))
//			return;
		sqlCmd = " update cca_outgoing set send_times = send_times+1, resp_code = ? ,bitmap = ? ,mod_time = sysdate , PROC_DATE = to_char(sysdate,'yyyymmdd') , PROC_TIME = to_char(sysdate,'hh24miss') where rowid = ? ";
		sqlExec(new Object[] { respCode,bitmap, hexStrToByteArr(rowid) });
		if (sqlNrow < 0) {
			printf("update cca_outgoing error");
			errExit(sqlNrow);
		}
		updateTotalCnt1 ++;
	}
	
	void updateCcaOutgoing2() throws Exception {
		String respCode = "99";
		if(respCo.equals("200")) {
			respCode = "00";
		}
		sqlCmd = " update cca_outgoing set send_times = send_times+1, resp_code = ? ,mod_time = sysdate , PROC_DATE = to_char(sysdate,'yyyymmdd') , PROC_TIME = to_char(sysdate,'hh24miss') where rowid = ? ";
		sqlExec(new Object[] { respCode, hexStrToByteArr(rowid) });
		if (sqlNrow < 0) {
			printf("update cca_outgoing error");
			errExit(sqlNrow);
		}
		updateTotalCnt2 ++;
	}
	
	void updateCcaOutgoingP2() throws Exception {

		sqlCmd = " update cca_outgoing set send_times = send_times+1, resp_code = ? ,mod_time = sysdate , PROC_DATE = to_char(sysdate,'yyyymmdd') , PROC_TIME = to_char(sysdate,'hh24miss') where rowid = ? ";
		sqlExec(new Object[] { respCo, hexStrToByteArr(rowid) });
		if (sqlNrow < 0) {
			printf("update cca_outgoing error");
			errExit(sqlNrow);
		}
		updateTotalCnt2 ++;
	}

	int callAutoAuth(String atype) throws Exception {
		String isNeg;
		respCode = "XX";
		isoRespData("");
		String strSql = "";
		
		if (atype.equals("NCCC") || atype.equals("FISC")) {
			strSql = "select wf_value, wf_value2, wf_value3, wf_value4 , wf_value6 as db_nocall" + " from ptr_sys_parm"
					+ " where wf_parm ='SYSPARM' and wf_key='NEG'";
			isNeg = "NEG";
		} else {
			strSql = "select wf_value, wf_value2, wf_value3, wf_value4 , wf_value6 as db_nocall" + " from ptr_sys_parm"
					+ " where wf_parm ='SYSPARM' and wf_key='OUTGOING'";
			isNeg = "FHM";
		}
		sqlSelect(strSql);
		if (sqlNrow <= 0) {
			printf("ptr_sysparm.SYSPARM,OUTGOING");
			return -1;
		}

		authIp = colSs("wf_value");
		authPortNo = colSs("wf_value2");
		// TTTT--
		// if (wp.localHost()) {
		// authIp = col_ss("wf_value3");
		// authPortNo = col_ss("wf_value4");
		// }

		if (empty(authIp) || empty(authPortNo)) {
			errmsg("自動授權[IP,Port-No]: 不可空白");
			return rc;
		}
		if (colInt("db_nocall") != 1) {
			AuthData authdata = new AuthData();
			Thread t = null;
			authdata.setFullIsoCommand(getIsoString());
			bitmap = getIsoString();
			printf(atype + "_iso : " + bitmap);
			try {
				try {
					FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
						public Boolean call() throws Exception {
							AuthGateway authway = new AuthGateway();
							authway.isNeg = isNeg;
							respData = authway.startProcess(authdata, authIp, authPortNo);
							return true;
						}
					});
					t = new Thread(task);
					t.start();
					task.get(10000, TimeUnit.MILLISECONDS);
				} catch (Exception ex) {
					respData = "99999999999999";
					printf(t.getState()+"");
				}
				isoRespData(respData);
			} catch (Exception ex) {
				printf("call_auth error; " + ex.getMessage());
				return -1;
			}
			printf(atype + " 回覆碼: " + respCode);
		
			if(zzstr.pos(wfErrorCode, respCode.trim())>0||zzstr.empty(respCode)) {
				errorCnt ++;
			}
	
			return 1;
		}else{
			return 0;
		}
	}

	void isoRespData(String s1) {
//		printf("respCode = [" + respCode + "]");
		respCode = "";
		if (empty(s1)) {
			return;
		}
		respCode = zzstr.mid(s1, 0, 2);
	}

	void nccc2Iso() {
		String mcc = "";
		gate.mesgType = "0300";
		gate.isoField[2] = "2";
		gate.isoField[3] = actCode;
		gate.isoField[4] = cardNo;
		gate.isoField[5] = "";
		gate.isoField[6] = reasonCode;
		if (binType.equals("M")) {
			mcc = "2";
		}
		gate.isoField[7] = mcc;
		gate.isoField[8] = "";
		gate.isoField[9] = mathRound(vipAmt);
		gate.isoField[10] = zzstr.mid(delDate, 2, 4);
		gate.isoField[11] = "";
		gate.isoField[12] = "";
		gate.isoField[13] = "";
		gate.isoField[14] = getTraceNo();
		gate.isoField[15] = "";

		NegFormat bic = new NegFormat(null, gate, null);
		bic.host2Iso();
	}

	void fisc2Iso() {
		String mcc = "";
		gate.mesgType = "0300";
		gate.isoField[2] = "1";
		gate.isoField[3] = actCode;
		gate.isoField[4] = cardNo;
		gate.isoField[5] = "";
		gate.isoField[6] = reasonCode;
		if (binType.equals("M")) {
			mcc = "2";
		}
		gate.isoField[7] = mcc;
		gate.isoField[8] = "";
		gate.isoField[9] = mathRound(vipAmt);
		gate.isoField[10] = zzstr.mid(delDate, 2, 4);
		;
		gate.isoField[11] = "";
		gate.isoField[12] = "";
		gate.isoField[13] = "";
		gate.isoField[14] = getTraceNo();
		gate.isoField[15] = "";

		NegFormat bic = new NegFormat(null, gate, null);
		bic.host2Iso();
	}

	void twmp2Iso() throws Exception {
		String Y37 = zzstr.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(zzstr.mid(sysGmtDatetime(), 0,8));
		String hh37=zzstr.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = vCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		gate.isoField[91] = actCode;
		gate.isoField[101] = "FTD001";

		// 風險卡：R ,暫停使用：U ,註銷：Q ,強制停卡：C ,偽卡：F ,遺失：L ,遭竊：S
		// +發卡行代號
		String ss = "U";
		switch (currentCode) {
			case "0":
				ss = " ";
				break;
			case "1":
				ss = "Q";
				break;
			case "2":
				if (zzstr.pos(",L,41,01", reasonCode) > 0)
					ss = "L";
				if (zzstr.pos(",S,43,02", reasonCode) > 0)
					ss = "S";
				break;
			case "3":
				ss = "C";
				break;
			case "4":
				ss = "Q";
				break;
			case "5":
				ss = "F";
				break;
		}
		gate.isoField[120] = ss + "006";

		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();
	}

	void hce2Twmp() throws Exception {
		String sir = "";
		String strSql = "select sir from hce_card where v_card_no = ? ";
		setString(1, vCardNo);
		sqlSelect(strSql);
		sir = colSs("sir");
		
		respCo = "";
		oUTSIR = "";
//        CcaEndpointRequest("EAI",vCardNo,ss);
//			   if(respCo.equals("00")) {
		String actionCode = reasonCode;
		String reasonCode = ",21,22".indexOf(actionCode)>0?"40":"31".equals(actionCode)?"31":",1,2,3,4,5".indexOf(currentCode)>0?String.format("3%s", currentCode):"31";
				  CcaEndpointRequest("TWMP",vCardNo,actionCode,sir,reasonCode);
//			   }
			   totalCnt++;
			   updateCcaOutgoing2();
			   commitDataBase();
	}
	
	void hce2TwmpP2() throws Exception {
		
		// 風險卡：R ,暫停使用：U ,註銷：Q ,強制停卡：C ,偽卡：F ,遺失：L ,遭竊：S
		// +發卡行代號
		String ss = "U";
		switch (currentCode) {
			case "0":
				ss = " ";
				break;
			case "1":
				ss = "Q";
				break;
			case "2":
//				if (zzstr.pos(",L,41,01", reasonCode) > 0)
					ss = "L";
//				if (zzstr.pos(",S,43,02", reasonCode) > 0)
//					ss = "S";
				break;
			case "3":
				ss = "C";
				break;
			case "4":
				ss = "Q";
				break;
			case "5":
				ss = "F";
				break;
		}
		
		
		respCo = "";
		oUTSIR = "";
        CcaEndpointRequestP2("EAI",vCardNo,ss);
			   if(respCo.equals("00")) {
				  CcaEndpointRequestP2("TWMP",vCardNo,ss);
			   }
			   totalCnt++;
			   updateCcaOutgoingP2();
			   commitDataBase();
	}
	
	void master2Iso() {
		String masterDate = delDate;
		if(empty(masterDate)){
			masterDate = newEndDate;
		}
		gate.mesgType = "0302";
		gate.isoField[2] = cardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[33] = "003741";
		gate.isoField[73] = zzstr.right(masterDate, 6);
		gate.isoField[91] = actCode;
		gate.isoField[101] = "MCC102";
		String ls120 = cardNo + zzstr.space(3);
		if (zzstr.eqAny(actCode, "1") || zzstr.eqAny(actCode,"2")) {
			ls120 += reasonCode + fill('0', 6) + fill('0', 4) + "00";
			if (zzstr.eqAny(reasonCode, "V")) {
				ls120 += zzstr.numFormat(zzstr.ss2Num(vipAmt), "000000000000") + "840";
			} else {
				ls120 += fill('0', 12) + zzstr.space(3);
			}
		}
		gate.isoField[120] = ls120;
		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();

	}

	void master2Iso2() {
		String lscardType3 = "";
		String masterDate = delDate;
		if(empty(masterDate)){
			masterDate = newEndDate; 
		}
		if (binType.length() == 1) {
			// MASTER普卡
			lscardType3 = binType + "CC";
		} else {
			lscardType3 = zzstr.mid(binType, 0, 1) + "C" + zzstr.mid(binType, 2, 1);
		}
		gate.mesgType = "0302";
		gate.isoField[2] = cardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[33] = "003741";
		gate.isoField[73] = zzstr.right(masterDate, 6);;
		gate.isoField[91] = actCode;
		gate.isoField[101] = "MCC103";
		String ss = cardNo + zzstr.space(3) + "00" + strBkIca;
		if (zzstr.eqAny(actCode,"1") || zzstr.eqAny(actCode,"2")) {
			String[] aregnDate = vmjRegnData.split("\\|");
			String lssortDate = sortMasterRegn(aregnDate, "A");
			ss +=  lscardType3 + "04" + reasonCode +zzstr.space(24)+ lssortDate;
		}
		gate.isoField[120] = ss;
		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();

	}

	void jcb2Iso() {
		gate.mesgType = "0302";
		gate.isoField[2] = cardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[32] = "88546000";
		gate.isoField[33] = "88546000";
		//gate.isoField[91] = actCode;

		gate.isoField[101] = "6332";
		if (zzstr.pos(",1,2", actCode) > 0) {
			// JCB Except File 1-Add 2-Update (Format 1)
			gate.isoField[120] = actCode + cardNo + reasonCode + zzstr.mid(delDate, 0, 6) + vmjRegnData;
		} else {
			// JCB Except File 0-Delete 5-Inquiry (Format 3)
//			gate.isoField[120] = actCode + cardNo + zzstr.space(13);
			gate.isoField[120] = actCode + cardNo;
		}
		gate.isoField[127] = "356713";

		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();
	}

	void visa2Iso() {
		String Y37 = zzstr.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(zzstr.mid(sysGmtDatetime(), 0,8));
		String hh37=zzstr.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = cardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		if (actCode.equals("1") || actCode.equals("2")) {
			gate.isoField[73] = zzstr.right(delDate, 6);
		}
		gate.isoField[91] = actCode;
		gate.isoField[92] = "";

		// E2 / TK / PAN
		gate.isoField[101] = "E2";
		if(actCode.equals("3")){
			gate.isoField[127] = "";
		}else{
//			gate.isoField[127] = reasonCode + vmjRegnData + zzstr.space(8);
			gate.isoField[127] = zzstr.rpad(reasonCode,2," ") + zzstr.rpad(vmjRegnData,9," ");
		}

		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();
	}

	void tsc2Iso() {
		String Y37 = zzstr.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(zzstr.mid(sysGmtDatetime(), 0,8));
		String hh37=zzstr.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = electronicCardno;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();

		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		gate.isoField[73] = zzstr.left(delDate, 6);
		gate.isoField[91] = actCode;

		gate.isoField[101] = "FSD001";
		String pCode = "";
		if(isDebit) {
			pCode = "891399";
			if(actCode.equals("3")){
				pCode = "891899";
			}
		}else {
			pCode = "890399";
			if(actCode.equals("3")){
				pCode = "890899";
			}
		}
		// 掛卡代號 掛卡：890399,取消掛卡:890899,連線拒授權名單:890999,卡片效期:YYMM
		gate.isoField[120] = pCode + zzstr.mid(newEndDate, 2, 4);

		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();
	}

	void ips2Iso() {
		String Y37 = zzstr.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(zzstr.mid(sysGmtDatetime(), 0,8));
		String hh37=zzstr.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = electronicCardno;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		gate.isoField[91] = actCode;
		gate.isoField[101] = "FBD001";

		// 掛卡代號 掛卡：910000
		String ss = "910000";
		if(actCode.equals("3")){
			ss = zzstr.space(6);
		}
		
		gate.isoField[120] = ss;

		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();

	}

	void ich2Iso() {
		String Y37 = zzstr.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(zzstr.mid(sysGmtDatetime(), 0,8));
		String hh37=zzstr.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = electronicCardno;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();

		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();

		gate.isoField[91] = actCode;
		gate.isoField[101] = "FID001";

		// 掛卡代號連線掛失：990176,卡片效期 YYMM
		String ss = zzstr.space(6);
		if(!actCode.equals("3"))
			ss = "990176";
		gate.isoField[120] = ss + zzstr.mid(newEndDate, 2, 4);

		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();

	}
	
//111/12/13  V1.00.24    Ryan     財金規格調整,oempay送黑名單欄位2由原本帶虛擬卡號改為帶實體卡號
	void oempay2Iso() throws Exception {
		String Y37 = zzstr.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(zzstr.mid(sysGmtDatetime(), 0,8));
		String hh37=zzstr.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
//		gate.isoField[2] = cardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[12] = commDate.sysTime();;
		gate.isoField[13] = zzstr.right(commDate.sysDate(),4);
		gate.isoField[14] = zzstr.mid(newEndDate,2,4);
		gate.isoField[15] = zzstr.right(commDate.sysDate(),4);
//		gate.isoField[91] = actCode; 
		gate.isoField[91] = "2";
		if(binType.equals("V")){
			getTokenId(vCardNo);
			gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
			if(currentCode.equals("1")) {
				reasonCode = "3701";
			};
			gate.isoField[2] = cardNo;
			gate.isoField[58] = "7004"+reasonCode;
//			gate.isoField[92] = "*A";	
			gate.isoField[101] = "TK";
			gate.isoField[124] = "0300410110"+toHexString(vCardNo)+"030B"+toHexString(zzstr.rpad(tokenRequestorId,11," "))+"0520"+toHexString(zzstr.rpad(tCIdentifier, 32," "));
			gate.isoField[127] = "";
		}
		if(binType.equals("M")){
			String statusCode ="S";
			if(currentCode.equals("0")){
				statusCode = "C";
			}
			if(zzstr.pos(",1,2,3,4,5", currentCode)>0){
				statusCode = "D";
			}
			gate.isoField[2] = cardNo;
			gate.isoField[33] = "003741";
			gate.isoField[101] = "MCC106";
			//PAN
			gate.isoField[120] = "M"+statusCode+"0"+vCardNo;
		}

		FhmFormat bic = new FhmFormat(null, gate, null);
		bic.host2Iso();
	}


	void isoFieldClear() {
		for (int x = 0; x < gate.isoField.length; x++) {
			gate.isoField[x] = "";
		}
	}

	public String sysGmtDatetime() {
		 Date currDate = new Date();
//		 SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddhhmmss");
		 SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmss");
		 form1.setTimeZone(TimeZone.getTimeZone("GMT") );
		 return form1.format(currDate);
	}
	
		
	String getGmtTime() {
		return sysGmtDatetime().substring(4);
	}

	public String getDaysInYear(String ymd) {
		int year = zzstr.ss2int(zzstr.mid(ymd, 0,4));
		int month = zzstr.ss2int(zzstr.mid(ymd, 4,2));
		int day = zzstr.ss2int(zzstr.mid(ymd, 6,2));
        int totalDays = 0;
        int days28 = 28;
        int days29 = 29;
        int days30 = 30;
        int days31 = 31;
        switch (month) {
            case 12:
                totalDays += days30;
            case 11:
                totalDays += days31;
            case 10:
                totalDays += days30;
            case 9:
                totalDays += days31;
            case 8:
                totalDays += days31;
            case 7:
                totalDays += days30;
            case 6:
                totalDays += days31;
            case 5:
                totalDays += days30;
            case 4:
                totalDays += days31;
            case 3:
                if (((year / 4 == 0) && (year / 100 != 0)) || (year / 400 == 0)) {
                    totalDays += days29;
                } else {
                    totalDays += days28;
                }
            case 2:
                totalDays += days31;
            case 1: 
                totalDays += day;
        }
        return String.format("%03d",totalDays);
    }
	
	String getTraceNo() {
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			// random = new Random(new Date().getTime());
			throw new RuntimeException("init SecureRandom failed.", e);
		}
		return zzstr.numFormat(random.nextDouble() * 1000000, "000000");
	}

	String sortMasterRegn(String[] aregnDate, String aSort) {
		String lsRtn = "";
		if (aregnDate == null) {
			return "";
		}
		for (int ii = 0; ii < aregnDate.length; ii++) {
			if (aregnDate[ii] == null)
				aregnDate[ii] = "";
		}

		if (aSort.equalsIgnoreCase("A"))
			Arrays.sort(aregnDate);
		else
			Arrays.sort(aregnDate, Collections.reverseOrder());

		for (int ii = 0; ii < aregnDate.length; ii++) {
			if (zzstr.empty(aregnDate[ii]))
				continue;
			lsRtn += aregnDate[ii];
		}

		return lsRtn;
	}

	String fill(char c1, int ll) {
		String ss = "";
		for (int ii = 0; ii < ll; ii++)
			ss += c1;
		return ss;
	}
	
	String mathRound(String str) {
		return Integer.toString((int)Math.round(zzstr.ss2Num(str)));
	}
	
	boolean isDebitcard(String cardNo) throws Exception {
		String lsCardNo = this.nvl(cardNo,"");
		if (lsCardNo.length() < 6)
			return false;

		String strSql = "select count(*) as xx_cnt" + " from ptr_bintable"
				+ " where ? between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')"
				+ " and debit_flag ='Y'";
		setString(1, cardNo);
		sqlSelect(strSql);
		if (sqlNrow <= 0)
			return false;

		if (colInt("xx_cnt") > 0)
			return true;

		return false;
	}
	
	public void getNewWssHost(String wsType,String tPan,String actionCode ,String sir ,String reasonCode,int cnt) throws Exception {
		if(cnt > 3) {
			showLogMessage("W", "", "http無法連線");	
			return;
		}
		wssHost = cnt == 1 ? wfValue2 : cnt == 2 ? wfValue3 : wfValue1;
		showLogMessage("I", "", "取得新的連線參數");
		showLogMessage("I", "", "wssHost = [" + wssHost + "]");
		showLogMessage("I", "", "wssPort = [" + wssPort + "]");
		CcaEndpointRequest(wsType, tPan, actionCode, sir, reasonCode);
	}
	

	public  void CcaEndpointRequest(String wsType,String tPan,String actionCode ,String sir ,String reasonCode ) throws Exception{
		try {
			CommCrd comc = new CommCrd();
			Properties properties = new Properties();
//			String pkiAcdpFileName = comc.getECSHOME() + "/conf/ecsAcdp.properties";
			String pkiAcdpFileName = getEcsAcdpPath();  // Justin 2021/11/16
//			String ecsConfFile = comc.getECSHOME() + "/conf/ecsParameter.properties";
			
			String wssPwd = getWssPwd(pkiAcdpFileName);
	     
//			String userId = properties.getProperty("userid");
//			String password = properties.getProperty("password");
			String userId = USER_ID;
			String password = wssPwd;
			
//	        Decryptor decryptor = new Decryptor();
//	        String password = decryptor.doDecrypt(attributes.getProperty("cr.credit.aid"));

			String rarPath = properties.getProperty("winRAR.path");
			String storePath = properties.getProperty("backup.path");
			String sourcePath = properties.getProperty("transfer.path");
//			String TWMPPath = CcaB002.class.getClassLoader().getResource(TWMP).getPath();
			String putXmlPath = "";
			if (wsType.equals("EAI")) {
//			   putXmlPath = properties.getProperty("eaiEndpoint_request.xml");
				putXmlPath = SecurityUtil.verifyPath(TWMP + "/" + EAI_ENDPOINT_REQUEST_XML);
			}else {
//			   putXmlPath = properties.getProperty("twmpOneWayEndpoint_request.xml");
			   putXmlPath = SecurityUtil.verifyPath(TWMP + "/" + TWMP_ONE_WAY_ENDPOINT_REQUEST_XML);
			}
			
//			String urlPath = getUrlPath(ecsConfFile);
			String urlPath = getUrlPath();	
			
			CcaModEndpointRequest EndpointRequest = new CcaModEndpointRequest(rarPath, storePath, sourcePath, putXmlPath);
			
			
//			if (wsType.equals("EAI")) {
//				 os="";
//			}else{
//				os=oUTSIR;
//			}
			EndpointRequest.setXMLAttr(userId,password,tPan,actionCode ,sir ,reasonCode);			
			
			// Justin 2021/11/29 Replace HttpClient with CloseableHttpClient
//	        HttpClient infapiclient = HttpClientBuilder.create().useSystemProperties().build();
	          
	        //Entity
	        // fix issue "Path Manipulation" 2020/09/16 Zuwei
//	        String eaiEndpoint = "";
//	        if (wsType.equals("EAI")) {
////	           eaiEndpoint = properties.getProperty("eaiEndpoint_request.xml");
//	           eaiEndpoint = SecurityUtil.verifyPath(TWMP + "/" + EAI_ENDPOINT_REQUEST_XML);
//	        }else {
////	        	eaiEndpoint = properties.getProperty("twmpOneWayEndpoint_request.xml");
//	           eaiEndpoint = SecurityUtil.verifyPath(TWMP + "/" + TWMP_ONE_WAY_ENDPOINT_REQUEST_XML);
//	        }
	        	
//	        String postentitystring = new Scanner(new File(eaiEndpoint)).useDelimiter("\\A").next(); 
			
	        String postentitystring = EndpointRequest.getXmlResult(); 
	        HttpEntity infapiclientpostentity = new StringEntity(postentitystring);
	        
	        // 2021/11/29 Justin load certificates 
//	        showLogMessage("I", "", "ConnectStart-------------------");
	        try(CloseableHttpClient httpClient = getCloseableHttpClient()){
	        	//Request
		        HttpPost infapiclientpost =  new HttpPost (urlPath);
		        infapiclientpost.setEntity(infapiclientpostentity);
		        infapiclientpost.setHeader("Accept", "application/soap+xml");
		        infapiclientpost.setHeader("content-type", "application/soap+xml");
		        
		        //Response
		        HttpResponse infresponse = httpClient.execute(infapiclientpost);
		        HttpEntity responseEntity = infresponse.getEntity();
		        if(responseEntity != null) {
		        	String response = EntityUtils.toString(responseEntity,"UTF-8");
	        	    showLogMessage("I", "", response);

					    String sRspCode ="";
					    String sOutsir ="";			 	 		   
	                    String sresponseCode="";
					    
						showLogMessage("I", "", "VcardNo:" + " " + tPan);
						showLogMessage("I", "", "ReasonCode:" + " " + reasonCode);
						int index = response.indexOf("<soap:Fault>");
						if(index < 0 ) {
						
						    if (wsType.equals("EAI")) {
							    int indexR = response.indexOf("<RspCode>");
							    sRspCode = response.substring(indexR + 9 );
						 	    indexR = sRspCode.indexOf("</RspCode>");
						 	    sRspCode = sRspCode.substring(0,indexR);
						 	    showLogMessage("I", "", "RspCode:" + " " +sRspCode);
						 	    respCo = sRspCode;			 	    
						 	   
						 	    int indexO = response.indexOf("<OUTSIR>");
							    sOutsir = response.substring(indexO + 8 );
						 	    indexO = sOutsir.indexOf("</OUTSIR>");
						 	    sOutsir = sOutsir.substring(0,indexO);
						 	    showLogMessage("I", "", "OUTSIR:" + " " +sOutsir);
						 	    oUTSIR = sOutsir;
						    }
						    if(wsType.equals("TWMP")) {
						 	    showLogMessage("I", "", "發送成功");
						 	    respCo = "200";	
						 	    oUTSIR = "";
						    }
						    
						}   	    
						else{
							   showLogMessage("I", "", "發送失敗");
						 	   respCo = "";	
						 	   oUTSIR = "";
						}        	   
		        }
	        }
		}catch(org.apache.http.conn.ConnectTimeoutException ex) {
			errCnt ++;
			showLogMessage("W", "", ex.toString()); // continue to run when encountering timeout
			getNewWssHost(wsType,tPan,actionCode,sir,reasonCode,errCnt);
		}catch(org.apache.http.conn.HttpHostConnectException ex) {
			errCnt ++;
			showLogMessage("W", "", ex.toString()); 
			getNewWssHost(wsType,tPan,actionCode,sir,reasonCode,errCnt);
        }catch(Exception ex) {
        	errCnt ++;
			showLogMessage("W", "", ex.toString());
			getNewWssHost(wsType,tPan,actionCode,sir,reasonCode,errCnt);
//			throw ex;
		}
	}
	
	public  void CcaEndpointRequestP2(String wsType,String tPan,String reasonCode) throws Exception{
		
		try {
			CommCrd comc = new CommCrd();
			Properties properties = new Properties();
//			String pkiAcdpFileName = comc.getECSHOME() + "/conf/ecsAcdp.properties";
			String pkiAcdpFileName = getEcsAcdpPath();  // Justin 2021/11/16
			String ecsConfFile = comc.getECSHOME() + "/conf/ecsParameter.properties";
			
			String wssPwd = getWssPwd(pkiAcdpFileName);
	     
//			String userId = properties.getProperty("userid");
//			String password = properties.getProperty("password");
			String userId = USER_ID;
			String password = wssPwd;
			
//	        Decryptor decryptor = new Decryptor();
//	        String password = decryptor.doDecrypt(attributes.getProperty("cr.credit.aid"));

			String rarPath = properties.getProperty("winRAR.path");
			String storePath = properties.getProperty("backup.path");
			String sourcePath = properties.getProperty("transfer.path");
//			String TWMPPath = CcaB002.class.getClassLoader().getResource(TWMP).getPath();
			String putXmlPath = "";
			if (wsType.equals("EAI")) {
//			   putXmlPath = properties.getProperty("eaiEndpoint_request.xml");
				putXmlPath = SecurityUtil.verifyPath(TWMP + "/" + EAI_ENDPOINT_REQUEST_XML);
			}else {
//			   putXmlPath = properties.getProperty("twmpOneWayEndpoint_request.xml");
			   putXmlPath = SecurityUtil.verifyPath(TWMP + "/" + TWMP_ONE_WAY_ENDPOINT_REQUEST_P2_XML);
			}
			
//			String urlPath = getUrlPath(ecsConfFile);
			String urlPath = getUrlPath();	
			
			CcaModEndpointRequest EndpointRequest = new CcaModEndpointRequest(rarPath, storePath, sourcePath, putXmlPath);
			
			
			String os="";
			if (wsType.equals("EAI")) {
				 os="";
			}else{
				os=oUTSIR;
			}
			EndpointRequest.setXMLAttrP2(userId,password,tPan,reasonCode,os);			
			
			// Justin 2021/11/29 Replace HttpClient with CloseableHttpClient
//	        HttpClient infapiclient = HttpClientBuilder.create().useSystemProperties().build();
	          
	        //Entity
	        // fix issue "Path Manipulation" 2020/09/16 Zuwei
//	        String eaiEndpoint = "";
//	        if (wsType.equals("EAI")) {
////	           eaiEndpoint = properties.getProperty("eaiEndpoint_request.xml");
//	           eaiEndpoint = SecurityUtil.verifyPath(TWMP + "/" + EAI_ENDPOINT_REQUEST_XML);
//	        }else {
////	        	eaiEndpoint = properties.getProperty("twmpOneWayEndpoint_request.xml");
//	           eaiEndpoint = SecurityUtil.verifyPath(TWMP + "/" + TWMP_ONE_WAY_ENDPOINT_REQUEST_XML);
//	        }
	        	
//	        String postentitystring = new Scanner(new File(eaiEndpoint)).useDelimiter("\\A").next(); 
	        String postentitystring = EndpointRequest.getXmlResult(); 
	        HttpEntity infapiclientpostentity = new StringEntity(postentitystring);
	        
	        // 2021/11/29 Justin load certificates 
//	        showLogMessage("I", "", "ConnectStart-------------------");
	        try(CloseableHttpClient httpClient = getCloseableHttpClient()){
	        	//Request
		        HttpPost infapiclientpost =  new HttpPost (urlPath);
		        infapiclientpost.setEntity(infapiclientpostentity);
		        infapiclientpost.setHeader("Accept", "application/soap+xml");
		        infapiclientpost.setHeader("content-type", "application/soap+xml");
		        
		        //Response
		        HttpResponse infresponse = httpClient.execute(infapiclientpost);
		        HttpEntity responseEntity = infresponse.getEntity();
		        if(responseEntity != null) {
		        	String response = EntityUtils.toString(responseEntity,"UTF-8");
	        	    showLogMessage("I", "", response);

					    String sRspCode ="";
					    String sOutsir ="";			 	 		   
	                    String sresponseCode="";
					    
						showLogMessage("I", "", "VcardNo:" + " " + tPan);
						showLogMessage("I", "", "ReasonCode:" + " " + reasonCode);
						
						int index = response.indexOf("<soap:Fault>");
						if(index < 0 ) {
						
						    if (wsType.equals("EAI")) {
							    int indexR = response.indexOf("<RspCode>");
							    sRspCode = response.substring(indexR + 9 );
						 	    indexR = sRspCode.indexOf("</RspCode>");
						 	    sRspCode = sRspCode.substring(0,indexR);
						 	    showLogMessage("I", "", "RspCode:" + " " +sRspCode);
						 	    respCo = sRspCode;			 	    
						 	   
						 	    int indexO = response.indexOf("<OUTSIR>");
							    sOutsir = response.substring(indexO + 8 );
						 	    indexO = sOutsir.indexOf("</OUTSIR>");
						 	    sOutsir = sOutsir.substring(0,indexO);
						 	    showLogMessage("I", "", "OUTSIR:" + " " +sOutsir);
						 	    oUTSIR = sOutsir;
						    }
						}   	    
						else{
							 int indexF = response.indexOf("<ResponseCode>");
							 sresponseCode = response.substring(indexF + 14 );
						 	 indexF = sresponseCode.indexOf("</ResponseCode>");
						 	 sresponseCode = sresponseCode.substring(0,indexF);
						 	    showLogMessage("I", "", "Soap Fault ResponseCode:" + " " +sresponseCode);
						 	    respCo = sresponseCode;	
						 	    oUTSIR = "";
						}        	   

		        }
	        }
		}catch(org.apache.http.conn.ConnectTimeoutException ex) {
			showLogMessage("W", "", ex.toString()); // continue to run when encountering timeout
		}catch(org.apache.http.conn.HttpHostConnectException ex) {
			showLogMessage("W", "", ex.toString()); 
        }catch(Exception ex) {
			showLogMessage("I", "", ex.toString());
			throw ex;
		}
	}
	
	private CloseableHttpClient getCloseableHttpClient() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException {
//		if (this.sslsf == null) {
//			String webCertPath = "/cr/cert/tcbweb.cer";
//			String rootCertPath = "/cr/cert/TCBCA-IndR.cer";
//	    	Certificate rootCert = null;
//	    	Certificate webCert = null;
//	    	
//	    	boolean isLoadRootCertOk = false; 
//	    	boolean isLoadWebCertOk = false; 
//			try {
//				try (FileInputStream fi = new FileInputStream(rootCertPath)) {
//					rootCert = CertificateFactory.getInstance("X.509").generateCertificate(fi);
//					isLoadRootCertOk = true;
//					showLogMessage("I", "", "Loading root certificate is successful");
//				} catch (FileNotFoundException e) {
//					showLogMessage("W", "", e.toString());
//				}
//				
//				try(FileInputStream fi2 = new FileInputStream(webCertPath)){
//					webCert = CertificateFactory.getInstance("X.509").generateCertificate(fi2);
//					isLoadWebCertOk = true;
//					showLogMessage("I", "", "Loading web certificate is successful");
//				} catch (FileNotFoundException e) {
//					showLogMessage("W", "", e.toString());
//				}
//			} catch (CertificateException e) {
//				e.printStackTrace();
//				showLogMessage("I", "", e.toString());
//			}
//	    	
//	    	SSLContext sslContext = SSLContext.getInstance("TLS");
//	    	if (isLoadRootCertOk || isLoadWebCertOk) {
//	    		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//	    		keyStore.load(null, null);
//	    		if (isLoadRootCertOk) {
//	    			keyStore.setCertificateEntry("rootCert", rootCert);
//				}
//	    		if (isLoadWebCertOk) {
//	    			keyStore.setCertificateEntry("webCert", webCert);	
//				}
//	    		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//	    		trustManagerFactory.init(keyStore);
//	    		sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());
//			}else {
//				sslContext.init(null, null, new java.security.SecureRandom());
//			}
//
//			this.sslsf = new SSLConnectionSocketFactory(sslContext);
//		}
//		CloseableHttpClient httpclient = HttpClients.custom()
//				.useSystemProperties()
//				.setSSLSocketFactory(this.sslsf)
//				.build();
		
		
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null,
    	        new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				}).build();
		
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
		        null, null, new X509HostnameVerifier() {
					
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
					
					@Override
					public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
					}
					
					@Override
					public void verify(String host, X509Certificate cert) throws SSLException {
					}
					
					@Override
					public void verify(String host, SSLSocket ssl) throws IOException {
					}
				});
		
		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.build();
		
		return httpclient;
	}

	private String getWssPwd(String pkiAcdpFileName) throws Exception{
		pkiAcdpFileName = Normalizer.normalize(pkiAcdpFileName, Normalizer.Form.NFKC);
		Properties pkiAcdpProps = new Properties();
		try (FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(pkiAcdpFileName));) {
			pkiAcdpProps.load(fis);
			fis.close();
		}
		String wssPwd = pkiAcdpProps.getProperty("ecs.wss").trim();
		//--解密
		wssPwd = new Decryptor().doDecrypt(wssPwd);
		return wssPwd;
	}

	private String getUrlPath() throws Exception {
//		ecsConfFile = Normalizer.normalize(ecsConfFile, Normalizer.Form.NFKC);
//		Properties ecsProps = new Properties();
//		try (FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(ecsConfFile));) {
//			ecsProps.load(fis);
//			fis.close();
//		}
////			String urlPath = properties.getProperty("url.path");
//		String wssHost = ecsProps.getProperty("WSS_HOST").trim();
//		String wssPort = ecsProps.getProperty("WSS_PORT").trim();
//		String urlPath = String.format("%s:%s/ecs-ws-bus/ws/ecs?wsdl", wssHost, wssPort);
		String urlPath = String.format("%s:%s/ecs-ws-bus/ws/ecs?wsdl", wssHost, wssPort);
		return urlPath;
	}
	
	private static File[] getStartWithNameFile(String path, String tempName) {
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
		File src = new File(verifyPath(path));
		File[] listFiles  = new File[]{};
		if(src.isDirectory()){	
			listFiles = src.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
				
					return new File(dir, name).isFile() && name.matches(tempName);
				}
				
			}
					);
		}
		return listFiles;
	}
    
    // check file name 
    public static String verifyPath(String path) {
        String tempStr = path;
        while (tempStr.indexOf("..\\") >= 0 || tempStr.indexOf("../") >= 0) {
        	tempStr = tempStr.replace("..\\", ".\\");
        	tempStr = tempStr.replace("../", "./");
        }
        
        return tempStr;
	}
    
	public static String toHexString(String inputStr) throws Exception {
		String str = null;
		byte[] byteArray = inputStr.getBytes("Cp1047");
		if (byteArray != null && byteArray.length > 0) {
			StringBuffer stringBuffer = new StringBuffer(byteArray.length);
			for (byte byteChar : byteArray) {
				stringBuffer.append(String.format("%02X", byteChar));
			}
			str = stringBuffer.toString();
		}
		return str;
	}
  
	void getTokenId(String vCardNo) throws Exception {
		tokenRequestorId = "";
		tCIdentifier = "";
		if (empty(vCardNo)) {
			return;
		}
		String strSql = "select token_requestor_id,t_c_identifier from oempay_card " 
		+ " where v_card_no = ? ";
		setString(1, vCardNo);
		sqlSelect(strSql);
		if (sqlNrow > 0) {
			tokenRequestorId = colSs("token_requestor_id");
			tCIdentifier = colSs("t_c_identifier");
		}
	}
  
	
}