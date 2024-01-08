/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  110-12-23 V1.00.01  Alex       initial     								 *
*  111-01-12 V1.00.02  Alex       bug fix                                    *
*  111-01-21 V1.00.03  Alex       執行前確認參數存在 , 存在執行 , 不存在直接結束                       	 *
*  111-03-23 V1.00.04  Alex       修正複數次查詢時需重組URL                        *
*  111-09-20 V1.00.05  Alex       update 不到時不abend                         *
*  111-12-21 V1.00.06  Alex       弱點修正 , SSL伺服器驗證                                                                *
******************************************************************************/
package Sms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;
import com.tcb.ap4.tool.Decryptor;

public class SmsP020 extends AccessDAO {
	private final String progname = "查詢三竹送簡訊結果 111/12/21 V1.00.06";
	CommFunction comm = new CommFunction();
	CommCrdRoutine comcr = null;
	CommCrd comc = new CommCrd();
	CommDate  commDate = new CommDate();
	CommString commString = new CommString();
	
	String hBusiBusinessDate = "";
	private String smsIP = "";
	private String user = "";
	private String pawd = "";
	private String strUrl = "";	
	
	String hSendMsgId = "";
	String partString = "\t";	
	String hCcaMsgLogMsgId = "";
	String hBatchSmsMsgId = "";
	
	String[] hSmsMsgIdBuff = null;
	String[] hSmsReponseCode = null;
	String[] hSmsReponseDate = null;
	String[] hSmsReponseTime = null;
	String[] hSmsErrorSeqno = null;
	String[] hSmsErrorStatus = null;
	
	public static void main(String[] args) throws Exception {
		SmsP020 proc = new SmsP020();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

	public int mainProcess(String[] args) {
		try {
			dateTime();
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程序啟動中, 不執行..");
				return (0);
			}

			if (args.length > 1) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [business_date]");
				return (1);
			}

			if (args.length == 1) {
				hBusiBusinessDate = args[0];
			}

			if (!connectDataBase())
				return (1);

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			if (hBusiBusinessDate.isEmpty()) {
				selectPtrBusinday();
			}
			
			//--檢查三竹參數是否存在 , 存在就去查詢 , 不存在則直接結束
			if(checkParm()) {
				procConsumeSms();	
			}							
			
			commitDataBase();
			finalProcess();
			return (0);
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	boolean checkParm() throws Exception {
		
		String tempUrl = "" , tempUser = "";
		
		sqlCmd = " select wf_value4 , wf_value3 ";
		sqlCmd += " from ptr_sys_parm ";
		sqlCmd += " where 1=1 and wf_parm = 'SMS_CONNECT' and wf_key = 'SMS_URL' ";
		selectTable();
		if (notFound.equals("Y")) {
			return false;
		}
		
		tempUrl = getValue("wf_value4");		
		tempUser = getValue("wf_value3");
		
		if(tempUrl.isEmpty()) {
			showLogMessage("I", "", "至三竹查詢URL參數未設定 , ptrm8010 SMS_CONNECT SMS_URL wf_value4 不執行程式");
			return false ;
		}
		
		if(tempUser.isEmpty()) {
			showLogMessage("I", "", "三竹使用者帳號未設定 , ptrm8010 SMS_CONNECT SMS_URL wf_value3 不執行程式");
			return false ;
		}
		
		return true;
	}
	
	void procBatchSms() throws Exception {
		initParm();
		sqlCmd = "select resp_msg_id from sms_msg_dtl where resp_code in ('0','1','2') order by resp_msg_id Asc ";
		openCursor();
		
		int msgCnt = 0 ;
		while(fetchTable()) {
			hBatchSmsMsgId = "";
			hBatchSmsMsgId = getValue("resp_msg_id");
			if(hSendMsgId.isEmpty()) {
				hSendMsgId = hBatchSmsMsgId ;
				msgCnt ++;
			}	else	{
				hSendMsgId += ","+hBatchSmsMsgId;
				msgCnt ++;
			}
			
			if(msgCnt % 100 ==0) {
				//--查詢結果一次最多100筆
				sendSmsQuery();
				updateMsgDtl();
				commitDataBase();
				msgCnt = 0;
				continue;
			}		
			
		}
	}
	
	void updateMsgDtl() throws Exception {
		String hTempMsgId = "", hTempCode = "" , hTempDate = "" , hTempTime = "";
		
		for (int ii = 0; ii < hSmsMsgIdBuff.length; ii++) {
			hTempMsgId = hSmsMsgIdBuff[ii];
			hTempCode = hSmsReponseCode[ii];
			hTempDate = hSmsReponseDate[ii];
			hTempTime = hSmsReponseTime[ii];
			
			if (hTempMsgId == null || hTempCode == null)
				break;
			
			daoTable = "sms_msg_dtl";
			updateSQL = "resp_code = ? , ";
			updateSQL += "msg_resp_date = ? , ";
			updateSQL += "msg_resp_time = ? ";
			whereStr = "where resp_msg_id = ? ";
			
			setString(1,hTempCode);
			setString(2,hTempDate);
			setString(3,hTempTime);
			setString(4,hTempMsgId);
			
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update sms_msg_dtl not found!", "", hTempMsgId);
			}			
		}
		
	}
	
	void procConsumeSms() throws Exception {
		initParm();
		sqlCmd = "select appr_pwd from cca_msg_log where proc_code in ('0','1','2') and appr_pwd <> '' order by appr_pwd Asc ";
		
		openCursor();
		int msgCnt = 0;
		while(fetchTable()) {
			hCcaMsgLogMsgId = "";
			hCcaMsgLogMsgId = getValue("appr_pwd");
			if(hSendMsgId.isEmpty()) {
				hSendMsgId = hCcaMsgLogMsgId ;
				msgCnt ++;
			}	else	{
				hSendMsgId += ","+hCcaMsgLogMsgId;
				msgCnt ++;
			}
			
			if(msgCnt % 100 ==0) {
				//--查詢結果一次最多100筆
				sendSmsQuery();
				updateMsgLog();
				msgCnt = 0;
				hSendMsgId = "";
				continue;
			}			
		}
		
		if(hSendMsgId.isEmpty() == false) {
			//--不滿 100 筆
			sendSmsQuery();
			updateMsgLog();
			msgCnt = 0;
			hSendMsgId = "";
		}
		
		closeCursor();
		
	}
	
	/**
	* @ClassName: SmsP020
	* @Description: updateMsgLog :查詢三竹簡訊狀態時 , 三竹回覆的簡訊序號有時不存在於 DB 內 , 當三竹回覆錯誤序號時無法更新狀態跟狀態時間不讓程式 abend
	* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
	* @Company: DXC Team.
	* @author Alex
	* @version V1.00.05, Sep 20, 2022
	*/

	
	void updateMsgLog() throws Exception {
		String hTempMsgId = "", hTempCode = "" , hTempDate = "" , hTempTime = "";
		
		for (int ii = 0; ii < hSmsMsgIdBuff.length; ii++) {
			hTempMsgId = hSmsMsgIdBuff[ii];
			hTempCode = hSmsReponseCode[ii];
			hTempDate = hSmsReponseDate[ii];
			hTempTime = hSmsReponseTime[ii];
									
			if (hTempMsgId ==null || hTempCode == null)
				break;
			
			daoTable = "cca_msg_log";
			updateSQL = "proc_code = ? , ";
			updateSQL += "msg_resp_date = ? , ";
			updateSQL += "msg_resp_time = ? ";
			whereStr = "where appr_pwd = ? and proc_code in ('0','1','2') ";
			
			setString(1,hTempCode);
			setString(2,hTempDate);
			setString(3,hTempTime);
			setString(4,hTempMsgId);
			
			updateTable();
			if (notFound.equals("Y")) {
//				comcr.errRtn("update cca_msg_log not found!", "", hTempMsgId);
			}			
		}
//		hSmsErrorSeqno = new String[100];
//		hSmsErrorStatus = new String[100];
		
		//--update 查無資料
		for(int zz = 0; zz < hSmsErrorSeqno.length ; zz++) {
			hTempMsgId = hSmsErrorSeqno[zz];
			hTempCode = hSmsErrorStatus[zz];
			
			if (hTempMsgId ==null || hTempCode == null)
				break;
			
			daoTable = "cca_msg_log";
			updateSQL = "proc_code = ? , ";
			updateSQL += "msg_resp_date = '' , ";
			updateSQL += "msg_resp_time = '' ";
			whereStr = "where appr_pwd = ? and proc_code in ('0','1','2') ";
			
			setString(1,hTempCode);
			setString(2,hTempMsgId);
			
			updateTable();
			if (notFound.equals("Y")) {
//				comcr.errRtn("update cca_msg_log not found!", "", hTempMsgId);
			}			
			
		}
		
		
	}
	
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("business_date");
	}
	
	void sendSmsQuery() throws Exception {
		getParm();
		getUrl();
		
		hSmsMsgIdBuff = new String[100];
		hSmsReponseCode = new String[100];
		hSmsReponseDate = new String[100];
		hSmsReponseTime = new String[100];
		hSmsErrorSeqno = new String[100];
		hSmsErrorStatus = new String[100];
		int liNotFound = 0;
		boolean lbNotFound = false;
		try {
			URL url = new URL(strUrl);
			String host = url.getHost();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			SSLSocketFactory oldSocketFactory = null;
			HostnameVerifier oldHostnameVerifier = null;
			HttpsURLConnection https = (HttpsURLConnection) con;
			oldSocketFactory = trustAllHosts(https);
			oldHostnameVerifier = https.getHostnameVerifier();
			https.setHostnameVerifier( new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession sslsession) { 
                    if(host.equals(hostname)){//判断域名是否和證書域名相等
                        return true;
                    } else {
                        return false;
                    }
                }
            });
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));) {
				String sLine; 
				int seqNoCnt = -1, reponseCodeCnt = -1, startSeqno = 0, endSeqno = 0;
				String tempDateTime = "";
				while ((sLine = rd.readLine()) != null) {
					
					//--查詢異常
					if(sLine.indexOf("statuscode") >=0) {
						lbNotFound = true ; 
						break ;
					}
					
					// --取Seqno;
					startSeqno = 0;
					endSeqno = 0;
					startSeqno = sLine.indexOf(partString);
					endSeqno = sLine.indexOf(partString,startSeqno+1);					
					if (startSeqno >= 0) {
						seqNoCnt++;
						hSmsMsgIdBuff[seqNoCnt] = commString.bbMid(sLine, 0, startSeqno);						
						hSmsReponseCode[seqNoCnt] = commString.bbMid(sLine, startSeqno+1, (endSeqno-(startSeqno+1)));	
						tempDateTime = commString.bbMid(sLine, endSeqno+1, (sLine.length()-(endSeqno+1)));
						hSmsReponseDate[seqNoCnt] = commString.bbMid(tempDateTime, 0, 8);
						hSmsReponseTime[seqNoCnt] = commString.bbMid(tempDateTime,8,6);
					}
				}
			}
			con.disconnect();
			
			//--不斷嘗試直到查詢資料
			if(lbNotFound) {
				String tempUrl = "" , tempMsgId = "" , sendUrl = "";
				tempUrl = smsIP +"?username=" +user+ "&password=" + pawd + "&msgid="; 
				tempMsgId = hSendMsgId;
				while(lbNotFound) {
					
					if(tempMsgId.isEmpty() || lbNotFound == false)
						break;
					
					sendUrl = tempUrl + tempMsgId;
					url = new URL(sendUrl);
					String host2 = url.getHost();
					con = (HttpURLConnection) url.openConnection();
					oldSocketFactory = null;
					oldHostnameVerifier = null;
					https = (HttpsURLConnection) con;
					oldSocketFactory = trustAllHosts(https);
					oldHostnameVerifier = https.getHostnameVerifier();
					https.setHostnameVerifier( new HostnameVerifier() {
						@Override
		                public boolean verify(String hostname, SSLSession sslsession) {		 
		                    if(host2.equals(hostname)){//判断域名是否和證書域名相等
		                        return true;
		                    } else {
		                        return false;
		                    }
		                }});
					con.setRequestMethod("POST");
					con.setDoOutput(true);
					con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					
					try (BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));) {
						String sLine; 
						int seqNoCnt = -1, reponseCodeCnt = -1, startSeqno = 0, endSeqno = 0;
						String tempDateTime = "";
						while ((sLine = rd.readLine()) != null) {							
							//--查詢異常
							if(sLine.indexOf("statuscode") >=0) {
								lbNotFound = true ; 
								int tempToken = tempMsgId.indexOf(",");
								if(tempToken > 0) {
									hSmsErrorSeqno[liNotFound] = commString.bbMid(tempMsgId, 0,tempToken);
									tempMsgId = commString.bbMid(tempMsgId, tempToken+1,tempMsgId.length());
								}	else	{
									hSmsErrorSeqno[liNotFound] = commString.bbMid(tempMsgId, 0,tempMsgId.length());
									tempMsgId = "";
								}
								
								tempToken = sLine.indexOf("=");
								hSmsErrorStatus[liNotFound] = commString.bbMid(sLine, tempToken + 1);
								liNotFound++;
								con.disconnect();
								break ;
							}
							lbNotFound = false ;
							// --取Seqno;
							startSeqno = 0;
							endSeqno = 0;
							startSeqno = sLine.indexOf(partString);
							endSeqno = sLine.indexOf(partString,startSeqno+1);					
							if (startSeqno >= 0) {
								seqNoCnt++;
								hSmsMsgIdBuff[seqNoCnt] = commString.bbMid(sLine, 0, startSeqno);						
								hSmsReponseCode[seqNoCnt] = commString.bbMid(sLine, startSeqno+1, (endSeqno-(startSeqno+1)));	
								tempDateTime = commString.bbMid(sLine, endSeqno+1, (sLine.length()-(endSeqno+1)));
								hSmsReponseDate[seqNoCnt] = commString.bbMid(tempDateTime, 0, 8);
								hSmsReponseTime[seqNoCnt] = commString.bbMid(tempDateTime,8,6);
							}
						}
					}				
				}
				con.disconnect();
			}
			
		} catch (Exception e) {}
		
	}
	
	void selectPtrSysParm() throws Exception {
		
		sqlCmd = " select wf_value4 , wf_value3 ";
		sqlCmd += " from ptr_sys_parm ";
		sqlCmd += " where 1=1 and wf_parm = 'SMS_CONNECT' and wf_key = 'SMS_URL' ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_sys_parm not found!", "", "");
		}
		
		smsIP = getValue("wf_value4");		
		user = getValue("wf_value3");
	}
	
	private void getParm() throws Exception {		
		if (smsIP.isEmpty() == false)
			return;
//		String confFile = comc.getECSHOME() + "/conf/SMSParameter.txt";
//		String confFile = "/PKI/acdp.properties";
		String confFile = getEcsAcdpPath();
		confFile = Normalizer.normalize(confFile, Normalizer.Form.NFKC);
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(confFile);) {
			props.load(fis);
			fis.close();
		}
		pawd = props.getProperty("cr.sms").trim();
		//--解密
		Decryptor decrptor = new Decryptor();
		pawd = decrptor.doDecrypt(pawd);
		
		//--smsIP,user
		selectPtrSysParm();
	}

	private void getUrl() throws Exception {
//		if (strUrl.isEmpty() == false)
//			return;
		strUrl = String.format("%s?", smsIP);
		strUrl += "username=" + user + "&password=" + pawd + "&msgid=" + hSendMsgId;		
	}		

	void initParm() {
		hSendMsgId = "";
	}

	private static final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[] {};
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	} };

	private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
		SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
		try {
//			SSLContext sc = SSLContext.getInstance("TLS");
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory newFactory = sc.getSocketFactory();
			connection.setSSLSocketFactory(newFactory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oldFactory;
	}
	
}
