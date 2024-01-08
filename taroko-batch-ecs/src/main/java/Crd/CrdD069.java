/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-12-09  V1.00.00     Ryan     initial                                            *
* 112-04-25  V1.00.01     Ryan     磁軌2加密格式修正                                                                                                             *
* 112/05/23  V1.00.02     Ryan     http host port 改為抓取資料庫參數                                                                  *
* 112/07/07  V1.00.03     Wilson   SERVICE_CODE改成讀參數                                                                                         *
* 112/12/06  V1.00.04     Wilson   crd_item_unit不判斷卡種                                                                                       *
 **************************************************************************************/

package Crd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.tcb.ap4.tool.Decryptor;

import Dxc.Util.SecurityUtil;
import bank.Auth.HSM.HsmUtil;

import com.CommDate;

public class CrdD069 extends AccessDAO {
	private final String progname = "HCE續卡資料報送TWMP程式(HCEECS01) 112/12/06 V.00.04";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	CommString comStr = new CommString();
	HceData hceData = null;
	Base64.Decoder decoder = Base64.getDecoder();
	private static final String HCE_ONE_WAY_ENDPOINT_REQUEST_XML = "hceOneWayEndpoint_request.xml";
	private static final String TWMP = "TWMP";
	private static final String USER_ID = "ecs";
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int totalCnt = 0;
	private String skipCntFlag = "";
	private String hProcFlag = "";
	private String respCo = "";
	private String hErrorCode = "";

	private String hPktbMobkek = "";
	private String hPktbMobdek = "";
	private int racalPort1 = 0;
	private String racalServer1 = "";
	private int racalPort2 = 0;
	private String racalServer2 = "";
	private boolean dbugFlag = true;
	private String visaCvka = "";
	private String visaCvkb = "";
	private String masterCvka = "";
	private String masterCvkb = "";
	private String jcbCvka = "";
	private String jcbCvkb = "";
	String wssHost = "";
	String wssPort = "";
	String wfValue1 = "";
	String wfValue2 = "";
	String wfValue3 = "";
	String wfValue4 = "";
	int errCnt = 0;
	Socket socket = null;

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			showLogMessage("I", "", "-->connect DB: " + getDBalias()[0]);

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			modPgm = javaProgram;
			modTime = sysDate + sysTime;
			comr = new CommRoutine(getDBconnect(), getDBalias());
			selectPtrHsmKeys();
			if (connectRacal() != 0) {
				if (socket != null) {
					socket.close();
					socket = null;
				}
				comc.errExit("connect_racal error", "");
			}
			if(!selectPtrSysParm2()) {		
				return (0);
			}
			selectHceApplyData();
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,筆數 = %s", totalCnt));
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	void selectHceApplyData() throws Exception {
		if (skipCntFlag.equals("Y"))
			return;
		sqlCmd = " select card_no ";
		sqlCmd += " ,wallet_id ";
		sqlCmd += " ,v_card_no ";
		sqlCmd += " from hce_apply_data ";
		sqlCmd += " where proc_flag in ('A','X') ";
		this.openCursor();

		while (fetchTable()) {
			initData();
			hceData.hCardNo = getValue("card_no");
			hceData.hWalletId = getValue("wallet_id");
			hceData.hVCardNo = getValue("v_card_no");

			procData();
			commitDataBase();
		}
		this.closeCursor();
	}

	void updateHceApplyData() throws Exception {
		if (skipCntFlag.equals("Y"))
			return;
		if (respCo.equals("200")) {
			hProcFlag = "B";
		} else {
			hProcFlag = "X";
		}
		daoTable = "hce_apply_data";
		updateSQL = " proc_flag = ? ";
		updateSQL += " ,mod_time = sysdate ,mod_pgm = ? ";
		whereStr = " where card_no = ? and wallet_id = ? ";
		setString(1, hProcFlag);
		setString(2, modPgm);
		setString(3, hceData.hCardNo);
		setString(4, hceData.hWalletId);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("W", "", "update hce_apply_data not found");
			skipCntFlag = "Y";
			return;
		}
		totalCnt++;
	}

	private void selectCrdCard() throws Exception {
		if (skipCntFlag.equals("Y"))
			return;
		sqlCmd = "select e.cellar_phone ,e.id_no ,substr(a.new_end_date,3,4) as new_end_date ,a.bin_type ";
		sqlCmd += " ,c.service_id ,c.service_code ,d.co_member_flag ,d.co_member_type ,a.emboss_data ";
		sqlCmd += " from crd_card a, ptr_group_card b, crd_item_unit c, ptr_group_code d,crd_idno e ";
		sqlCmd += " where a.id_p_seqno = e.id_p_seqno and a.group_code = b.group_code ";
		sqlCmd += " and a.card_type = b.card_type and a.unit_code = c.unit_code ";
		sqlCmd += " and b.group_code = d.group_code and card_no = ? ";
		setString(1, hceData.hCardNo);
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("W", "", " SELECT CRD CARD NOT FOUND ");
			skipCntFlag = "Y";
			return;
		}
		hceData.hCellarPhone = getValue("cellar_phone");
		hceData.hIdNo = getValue("id_no");
		hceData.hNewEndDate = getValue("new_end_date");
		hceData.hBinType = getValue("bin_type");
		hceData.hServiceId = getValue("service_id");
		hceData.hServiceCode = getValue("service_code");
		hceData.hCoMemberFlag = getValue("co_member_flag");
		hceData.hcoMemberType = getValue("co_member_type");
		hceData.hEmbossData = getValue("emboss_data");
	}

	private void selectPtrServiceVer() throws Exception {
		if (skipCntFlag.equals("Y"))
			return;
		sqlCmd = "select service_ver from ptr_service_ver where bin_type = ? fetch first 1 rows only ";
		setString(1, hceData.hBinType);
		selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("W", "", " SELECT SERVICE_VER NOT FOUND ");
			skipCntFlag = "Y";
			return;
		}
		hceData.hServiceVer = getValue("service_ver");

	}

	private void getActCodeEnc() throws IOException {
		if (skipCntFlag.equals("Y"))
			return;
		StringBuffer strCode = new StringBuffer();
		strCode.append("2");
		strCode.append("6");
		strCode.append(comStr.right(hceData.hIdNo, 6));
		strCode.append("FFFFFFFF");
		hceData.hActCode = strCode.toString();
		String tmp = callRacal(hPktbMobkek, hceData.hActCode ,"0010");
		hceData.hActCodeEnc = splitRcvData(tmp);
		if (dbugFlag) {
			showLogMessage("I", "", String.format("hActCode = [%s]", hceData.hActCode));
			showLogMessage("I", "", String.format("hActCodeEnc = [%s]", hceData.hActCodeEnc));
		}
	}

	private void getTrack2DataEnc() throws IOException {
		if (skipCntFlag.equals("Y"))
			return;
		String track2Data = "";
		StringBuffer strCode = new StringBuffer();
		strCode.append(hceData.hCardNo);
		strCode.append("D");
		strCode.append(hceData.hNewEndDate);
		strCode.append(hceData.hServiceCode);
		strCode.append("00000");
		strCode.append(hceData.hIcvvCode);// ICVV
		strCode.append("00000F8");//20230425 add 
		track2Data = rightPadding(strCode.toString(), 64, '0');
		showLogMessage("I", "", String.format("track2Data = [%s]", track2Data));
		String tmp = callRacal(hPktbMobdek, track2Data , "0040");
		hceData.hTrack2DataEnc = splitRcvData(tmp);
		if (dbugFlag) {
			showLogMessage("I", "", String.format("hCardNo = [%s]", hceData.hCardNo));
			showLogMessage("I", "", String.format("hTrack2DataEnc = [%s]", hceData.hTrack2DataEnc));
		}
	}

	private String rightPadding(String str, int length, char padChar) {
		if (str == null) {
			str = "";
		}
		if (str.length() > length) {
			return str;
		}
		String pattern = "%-" + length + "s";
		return String.format(pattern, str).replace(' ', padChar);
	}

	private void selectPtrHsmKeys() throws Exception {

		sqlCmd = " select mob_kek,mob_dek,";
		sqlCmd += " hsm_ip_addr1,hsm_port1, ";
		sqlCmd += " hsm_ip_addr2,hsm_port2, ";
		sqlCmd += " visa_cvka,visa_cvkb,master_cvka,master_cvkb,jcb_cvka,jcb_cvkb ";
		sqlCmd += " FROM ptr_hsm_keys";
		sqlCmd += " where hsm_keys_org ='00000000' ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select_ptr_hsm_keys error[not find]");
			skipCntFlag = "Y";
			return;
		}
		if (recordCnt > 0) {
			hPktbMobkek = getValue("mob_kek");
			hPktbMobdek = getValue("mob_dek");
			racalServer1 = getValue("hsm_ip_addr1");
			racalPort1 = getValueInt("hsm_port1");
			racalServer2 = getValue("hsm_ip_addr2");
			racalPort2 = getValueInt("hsm_port2");
			visaCvka = getValue("visa_cvka");
			visaCvkb = getValue("visa_cvkb");
			masterCvka = getValue("master_cvka");
			masterCvkb = getValue("master_cvkb");
			jcbCvka = getValue("jcb_cvka");
			jcbCvkb = getValue("jcb_cvkb");
		}

		showLogMessage("I", "", String.format("RACLA_IP [%s] ,RACLA_PORT [%s] ,mob_kek [%s] ,mob_dek [%s] ",
				racalServer1, racalPort1, hPktbMobkek, hPktbMobdek));
	}
	
	boolean selectPtrSysParm2() throws Exception {
		sqlCmd = "SELECT WF_VALUE,WF_VALUE2,WF_VALUE3,WF_VALUE4 FROM PTR_SYS_PARM WHERE WF_KEY = 'TWMP'";
		int recordCnt = selectTable();
		if (recordCnt <= 0) {
			showLogMessage("I", "", "查無參數 PTR_SYS_PARM WHERE WF_KEY = 'TWMP' ");
			return false;
		}
		wfValue1 = getValue("WF_VALUE");
		wfValue2 = getValue("WF_VALUE2");
		wfValue3 = getValue("WF_VALUE3");
		wfValue4 = getValue("WF_VALUE4");
		wssHost = wfValue1;
		wssPort = wfValue4;
		showLogMessage("I", "", "取得連線參數"); 
		showLogMessage("I", "", "wssHost = [" + wssHost + "]");
		showLogMessage("I", "", "wssPort = [" + wssPort + "]");
		return true;
	}

	int connectRacal() {
		String host = racalServer1;
		int port = racalPort1;
		try {
			socket = new Socket(host, port);
			showLogMessage("I", "", "  888 IP1 OK=[" + host + "]" + port);

		} catch (IOException e) {
			host = racalServer2;
			port = racalPort2;
			try {
				socket = new Socket(host, port);
				showLogMessage("I", "", "  888 IP2 OK=[" + host + "]" + port);
			} catch (IOException ex) {
				showLogMessage("I", "", "RACAL CONNECT error !!");
				return 1;
			}
		}
		showLogMessage("I", "", "RACAL CONNECT ok !!");

		return (0);
	}

	private String callRacal(String sPKey, String sPMsgToBeEncrypted ,String len) throws IOException {
		String slResult = "";

		HsmUtil lHsmUtil = new HsmUtil(racalServer1, racalPort1);
//        String msgData = commChar2hex(decoder.decode(sPMsgToBeEncrypted));
		String msgData = sPMsgToBeEncrypted;
//        String msgLen = Integer.toHexString(msgData.length());
		String msgLen = len;
//        while (msgLen.length() != 4)
//        	msgLen = "0" + msgLen;
//        msgLen = msgLen.toUpperCase();
        if(dbugFlag) {
        	showLogMessage("I", "", "  888 msgData =[" + msgData + "]");
        	showLogMessage("I", "", "  888 msgLen =[" + msgLen + "]");
        }
        String sPKey2 = sPKey.length() == 48 ? "T" + sPKey : "U" + sPKey;
		try {
			slResult = lHsmUtil.hsmCommandM0("00", "1", "1", "00A", sPKey2/* sPKey */, ""/* sPKsnDescriptor */
					, ""/* sPKeySerialNumber */, ""/* sP_IV */, msgLen, /* sPMsgLength */
					msgData/* sPMsgToBeDecrypted */ );
			showLogMessage("I", "", "  888 HsmUtil R=[" + slResult + "]");
			if ("00".equals(slResult.substring(0, 2))) {
				showLogMessage("I", "", "  成功，Result== " + slResult.substring(2, slResult.length()) + "]");
			} else {
				showLogMessage("I", "", "  失敗，Result== " + slResult + "]");
				skipCntFlag = "Y";
//                comcr.errRtn("RECAL " + "M0" + " process error!", "", comcr.hCallBatchSeqno);
			}
		} catch (ConnectException ex1) {
			showLogMessage("I", "", "server1 連線失敗");
			lHsmUtil = new HsmUtil(racalServer2, racalPort2);
			try {
				slResult = lHsmUtil.hsmCommandM0("00", "1", "1", "00A", sPKey2/* sPKey */, ""/* sPKsnDescriptor */
						, ""/* sPKeySerialNumber */, ""/* sP_IV */, msgLen, /* sPMsgLength */
						msgData/* sPMsgToBeDecrypted */ );

				showLogMessage("I", "", "  888 HsmUtil R=[" + slResult + "]");
				if ("00".equals(slResult.substring(0, 2))) {
					showLogMessage("I", "", "  成功，Result== " + slResult.substring(2, slResult.length()) + "]");
				} else {
					showLogMessage("I", "", "  失敗，Result== " + slResult + "]");
					skipCntFlag = "Y";
//                comcr.errRtn("RECAL " + "M0" + " process error!", "", comcr.hCallBatchSeqno);
				}
			} catch (ConnectException ex2) {
				showLogMessage("I", "", "hsmCommand connect err");
				skipCntFlag = "Y";
			}catch (Exception e) {
				showLogMessage("I", "", "  Error HsmUtil !");
				e.printStackTrace();
				skipCntFlag = "Y";
			}
		} catch (Exception e) {
			showLogMessage("I", "", "  Error HsmUtil !");
			e.printStackTrace();
			skipCntFlag = "Y";
		}
		return slResult;
	}

	private void getIcvv() throws UnsupportedEncodingException {
		if (skipCntFlag.equals("Y"))
			return;
		String cvkA = "", cvkB = "";
		switch (hceData.hBinType) {
		case "V":
			cvkA = visaCvka;
			cvkB = visaCvkb;
			break;
		case "M":
			cvkA = masterCvka;
			cvkB = masterCvkb;
			break;
		case "J":
			cvkA = jcbCvka;
			cvkB = jcbCvkb;
			break;
		}
		HsmUtil hsmCy = new HsmUtil(racalServer1, racalPort1);
		String tmp = "";
		try {
			tmp = hsmCy.hsmCommandCW(hceData.hCardNo, hceData.hNewEndDate, hceData.hServiceCode, cvkA, cvkB);
		} catch (ConnectException ex1) {
			hsmCy = new HsmUtil(racalServer2, racalPort2);
			try {
				tmp = hsmCy.hsmCommandCW(hceData.hCardNo, hceData.hNewEndDate, hceData.hServiceCode, cvkA, cvkB);
			} catch (ConnectException ex2) {
				showLogMessage("I", "", "hsmCommand connect err");
				skipCntFlag = "Y";
				return;
			} catch (Exception e) {
				showLogMessage("I", "", "  Error HsmUtil !");
				e.printStackTrace();
				skipCntFlag = "Y";
			}
		} catch (Exception e) {
			showLogMessage("I", "", "  Error HsmUtil !");
			e.printStackTrace();
			skipCntFlag = "Y";
		}
		splitCvvOutbuf(tmp);
		showLogMessage("I", "", String.format("hIcvvCode = [%s]", hceData.hIcvvCode));
	}

	private String splitRcvData(String str) throws UnsupportedEncodingException {
		String encCode = "";
		byte[] bytes = str.getBytes("MS950");
		hErrorCode = comc.subMS950String(bytes, 2, 2);
		encCode = comc.subMS950String(bytes, 6, bytes.length);
		showLogMessage("I", "", String.format("-----------------------splitRcvData = [%s]", str));
		showLogMessage("I", "", String.format("-----------------------hErrorCode = [%s]", hErrorCode));
		if (!hErrorCode.equals("00")) {
			skipCntFlag = "Y";
			showLogMessage("I", "", String.format("密碼加密失敗 ,rspcode = [%s]", encCode));
		}

		return encCode;
	}
	
	void splitCvvOutbuf(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		hceData.hIcvvCode = comc.subMS950String(bytes, 2, 3);
	}

	public void getNewWssHost(int cnt) throws Exception {
		if(cnt > 3) {
			showLogMessage("W", "", "http無法連線");	
			return;
		}
		wssHost = cnt == 1 ? wfValue2 : cnt == 2 ? wfValue3 : wfValue1;
		showLogMessage("I", "", "取得新的連線參數");
		showLogMessage("I", "", "wssHost = [" + wssHost + "]");
		showLogMessage("I", "", "wssPort = [" + wssPort + "]");
		CrdEndpointRequest();
	}

	private void CrdEndpointRequest() throws Exception {
		if (skipCntFlag.equals("Y"))
			return;
		try {
			CommCrd comc = new CommCrd();
			Properties properties = new Properties();
			String pkiAcdpFileName = getEcsAcdpPath();
//			String ecsConfFile = comc.getECSHOME() + "/conf/ecsParameter.properties";

			String wssPwd = getWssPwd(pkiAcdpFileName);

			String userId = USER_ID;
			String password = wssPwd;

			String rarPath = properties.getProperty("winRAR.path");
			String storePath = properties.getProperty("backup.path");
			String sourcePath = properties.getProperty("transfer.path");
			String putXmlPath = "";

			putXmlPath = SecurityUtil.verifyPath(TWMP + "/" + HCE_ONE_WAY_ENDPOINT_REQUEST_XML);

			if (dbugFlag) {
				showLogMessage("I", "", String.format("putXmlPath = [%s]", putXmlPath));
//				showLogMessage("I", "", String.format("ecsConfFile = [%s]", ecsConfFile));
			}
			String urlPath = getUrlPath();

			CrdModEndpointRequest EndpointRequest = new CrdModEndpointRequest(rarPath, storePath, sourcePath,
					putXmlPath);

			if (dbugFlag) {
				showLogMessage("I", "", String.format("urlPath = [%s]", urlPath));
				showLogMessage("I", "", String.format("card_no = [%s]", hceData.hCardNo));
			}

			EndpointRequest.setXMLAttr(userId, password, hceData);

			String postentitystring = EndpointRequest.getXmlResult();
//			if (dbugFlag) {
//				showLogMessage("I", "", String.format("postentitystring = [%s]", postentitystring));
//			}
			HttpEntity infapiclientpostentity = new StringEntity(postentitystring);

//	        showLogMessage("I", "", "ConnectStart-------------------");
			try (CloseableHttpClient httpClient = getCloseableHttpClient()) {
				// Request
				HttpPost infapiclientpost = new HttpPost(urlPath);
				infapiclientpost.setEntity(infapiclientpostentity);
				infapiclientpost.setHeader("Accept", "application/soap+xml");
				infapiclientpost.setHeader("content-type", "application/soap+xml");

				// Response
				HttpResponse infresponse = httpClient.execute(infapiclientpost);
				HttpEntity responseEntity = infresponse.getEntity();
				if (responseEntity != null) {
					String response = EntityUtils.toString(responseEntity, "UTF-8");
					showLogMessage("I", "", response);
					String sresponseCode = "";

//						int index = response.indexOf("<soap:Fault>");
					int indexF = response.indexOf("<soap:Fault>");
					if (indexF < 0) {
						respCo = "200";
						showLogMessage("I", "", "發送成功");
					}else{
						skipCntFlag = "Y";
						showLogMessage("I", "", "發送失敗");
					}
				}
			}
		} catch (org.apache.http.conn.ConnectTimeoutException ex) {
			errCnt ++;
			showLogMessage("W", "", ex.toString()); // continue to run when encountering timeout
			getNewWssHost(errCnt);
		} catch (org.apache.http.conn.HttpHostConnectException ex) {
			errCnt ++;
			showLogMessage("W", "", ex.toString());
			getNewWssHost(errCnt);
		} catch (Exception ex) {
			errCnt ++;
			showLogMessage("I", "", ex.toString());
			getNewWssHost(errCnt);
//			throw ex;
		}
	}

	private String getWssPwd(String pkiAcdpFileName) throws Exception {
		pkiAcdpFileName = Normalizer.normalize(pkiAcdpFileName, Normalizer.Form.NFKC);
		Properties pkiAcdpProps = new Properties();
		try (FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(pkiAcdpFileName));) {
			pkiAcdpProps.load(fis);
			fis.close();
		}
		String wssPwd = pkiAcdpProps.getProperty("ecs.wss").trim();
		// --解密
		wssPwd = new Decryptor().doDecrypt(wssPwd);
		return wssPwd;
	}

	private String getUrlPath() throws IOException, FileNotFoundException {
//		ecsConfFile = Normalizer.normalize(ecsConfFile, Normalizer.Form.NFKC);
//		Properties ecsProps = new Properties();
//		try (FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(ecsConfFile));) {
//			ecsProps.load(fis);
//			fis.close();
//		}
//		String wssHost = ecsProps.getProperty("WSS_HOST").trim();
//		String wssPort = ecsProps.getProperty("WSS_PORT").trim();
		String urlPath = String.format("%s:%s/ecs-ws-bus/ws/ecs?wsdl", wssHost, wssPort);
		return urlPath;
	}

	String commChar2hex(byte[] inputBytes) {
		String hexString = "";

		for (int i = 0; i < inputBytes.length; i++) {
			hexString += toHex(inputBytes[i]);
		}

		return hexString;
	}

	static String toHex(byte b) {
		return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
	}

	private CloseableHttpClient getCloseableHttpClient() throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, KeyManagementException {

		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		}).build();

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, null, null,
				new X509HostnameVerifier() {

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

		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		return httpclient;
	}

	private void procData() throws Exception {
		selectCrdCard();
		selectPtrServiceVer();
		getActCodeEnc();
		getIcvv();
		getTrack2DataEnc();
		CrdEndpointRequest();
		updateHceApplyData();
	}

	/***********************************************************************/
	public void initData() {
		hceData = new HceData();
		skipCntFlag = "";
		respCo = "";
		hErrorCode = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CrdD069 proc = new CrdD069();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}

class HceData {
	String hCardNo = "";
	String hWalletId = "";
	String hVCardNo = "";
	String hCellarPhone = "";
	String hIdNo = "";
	String hNewEndDate = "";
	String hBinType = "";
	String hServiceId = "";
	String hServiceVer = "";
	String hServiceCode = "";
	String hCoMemberFlag = "";
	String hcoMemberType = "";
	String hEmbossData = "";
	String hActCode = "";
	String hActCodeEnc = "";
	String hTrack2DataEnc = "";
	String hIcvvCode = "";
}
