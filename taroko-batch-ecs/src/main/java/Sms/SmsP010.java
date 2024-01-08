/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-07-03  V1.001    yanghan       修改了變量名稱和方法名稱*
*  109-07-22     	   yanghan    修改了字段名称            *
*  109-09-03 V1.002    yanghan 	      修改了關閉流的方式
*  109/09/05           yanghan    fix code scan issue    
*  109-10-19 V1.00.05  shiyuqi    updated for project coding standard    *
*  109-11-04 V1.00.06  Alex		  fix busidate								 *
*  109-11-09 v1.00.07  Alex       三竹網址改為讀取參數							 *
*  110-02-03 v1.00.08  Alex       SSL TLSv1.2
*  110-03-03 v1.00.09  Alex       密碼改從檔案中讀取								 *
*  111-01-12 v1.00.10  Alex       hTempSeqno,hTempCode ==null break          *
*  111-06-13 v1.00.11  Alex       有效時間改為空白 , 三竹預設24小時					 *
*  111-12-21 v1.00.12  Alex       弱點修正 , SSL伺服器驗證                                                                *
*  112-07-19 v1.00.13  Alex       加上預約日期、時間                                                                              *
*  112-09-07 v1.00.14  JeffKung   add businday==bookdate條件
 *  112/09/07 V1.00.15  JH       bugFix: changeParm() *
 *  112/09/08 V1.00.16  JH       ++<#0>
******************************************************************************/
package Sms;

import com.*;
import com.tcb.ap4.tool.Decryptor;

import javax.net.ssl.*;
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
public class SmsP010 extends AccessDAO {
	private final String progname = "簡訊批次送三竹程式 112/09/08 V1.00.16";
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
	private String enCode = "UTF-8";

	String hSmsSeqno = "";
	String hCellPhone = "";
	String hReserveTime = "";
	String hValidTime = "";
	String hChiName = "";
	String hRespUrl = ""; // --回報網址目前沒有 2020/04/06
	String hSmsBody = "";
	String hSpaceSymbol = "$$"; // --間隔符號
	String hSmsParm = "";
	String hBookDate = "";
	String hBookTime = "";
	StringBuffer hSendDesc = new StringBuffer();
	int hSendTime = 0; // --程式執行時的時間
	int hMsgHour1 = 0;
	int hMsgHour2 = 0;
	String[] hSmsSeqnoBuff = null;
	String[] hSmsReponseCode = null;

	int ilTotalCnt = 0;

	public static void main(String[] args) throws Exception {
		SmsP010 proc = new SmsP010();
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
			hSendTime = commString.ss2int(sysTime.substring(0, 2)) + 1;

			selectSmsDtl();
			commitDataBase();
			finalProcess();
			return (0);
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	void selectPtrSysParm() throws Exception {
		
		sqlCmd = " select wf_value2 , wf_value3 ";
		sqlCmd += " from ptr_sys_parm ";
		sqlCmd += " where 1=1 and wf_parm = 'SMS_CONNECT' and wf_key = 'SMS_URL' ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_sys_parm not found!", "", "");
		}
		
		smsIP = getValue("wf_value2");		
		user = getValue("wf_value3");
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

	void selectSmsDtl() throws Exception {
		sqlCmd = "";
		sqlCmd += " select ";
		sqlCmd += " A.msg_seqno , A.chi_name , A.msg_desc as msg_parm , A.msg_id , A.cellar_phone ,";
		sqlCmd += " B.msg_desc as msg_body , B.msg_hour1 , B.msg_hour2 , A.booking_date , A.booking_time ";
		sqlCmd += " from sms_msg_dtl A join sms_msg_id B on A.msg_pgm = B.msg_pgm ";
		sqlCmd += " where A.cellphone_check_flag ='Y' and proc_flag <> 'Y' ";
		sqlCmd += " and ? >= B.send_eff_date1 and ? <= B.send_eff_date2 ";
		sqlCmd += " and B.msg_send_flag = 'Y' and substr(A.msg_userid,1,5) <> 'BATCH' ";
//		sqlCmd += " and B.send_eff_date1 >= ? and B.send_eff_date2 <= ? ";
		setString(1, hBusiBusinessDate);
		setString(2, hBusiBusinessDate);
		openCursor();

		while (fetchTable()) {
			// --清空變數
			initParm();
			hSmsSeqno = getValue("msg_seqno");
			hCellPhone = getValue("cellar_phone");
			hMsgHour1 = getValueInt("msg_hour1");
			hMsgHour2 = getValueInt("msg_hour2");
			hBookDate = getValue("booking_date");
			hBookTime = getValue("booking_time");			
			
			
			//if(hBookDate.isEmpty() == false && hBookDate.equals(sysDate) == false)
			if(hBookDate.isEmpty() == false
			  && ( hBookDate.equals(sysDate) == false &&
			       hBookDate.equals(hBusiBusinessDate) == false)
			   )
				continue;
			
			if(hBookDate.isEmpty() == false && hBookTime.isEmpty() == false) {
				hReserveTime = hBookDate+hBookTime;
			}	else	{
				if (hSendTime >= hMsgHour1 && hSendTime <= hMsgHour2) {
					hReserveTime = sysDate + commString.int2Str(hSendTime) + "0000";
				} else {
					hReserveTime =  commDate.dateAdd(sysDate, 0, 0, 1) + commString.int2Str(hMsgHour1) + "0000";
				}
			}			
//			hValidTime =  commDate.dateAdd(hReserveTime, 0, 0, 1); 改空白預設24hr
			hSmsParm = getValue("msg_parm");
			hChiName = getValue("chi_name");
			hSmsBody = getValue("msg_body");			
			
			if (hSmsBody.indexOf("<>#0<>") >= 0) {
				changeParm();
			}
         if (hSmsBody.indexOf("<#0>") >= 0) {
            changeParm_2();
         }
			composeSms();
			ilTotalCnt++;

			if (ilTotalCnt % 500 == 0) {
				sendSms();
				hSendDesc.setLength(0); // --清空 String Buffer
				continue;
			}
		}

		int sbLength = hSendDesc.length();

		if (sbLength > 0) {
			sendSms();
			hSendDesc.setLength(0);
		}

	}

	void changeParm() {
		// --h_sms_parm:user_id,sms_id,cellar_phone,parm1,parm2,....
//		StringTokenizer st = new StringTokenizer(hSmsParm, ",");
//		String temp = "";
//		String[] lsParm = new String[9];
//		int i = 0, p = 0;
//		while (st.hasMoreTokens()) {
//			temp = st.nextToken();
//			i++;
//			if (i < 4)
//				continue;
//			lsParm[p] = temp;
//			p++;
//			continue;
//		}

      String[] laParm = new String[10];
      String[] tt=new String[]{hSmsParm,","};
      int pp=0, ll=0;
      while (tt[0].length()>0) {
         String ss=commString.token(tt);
         ll++;
         if (ll<4) continue;
         laParm[pp] =ss;
         pp++;
      }
      //,2400,0925180259,強制停卡--
		// --替換 h_sms_body
		for (int ii = 0; ii < pp; ii++) {
			hSmsBody = hSmsBody.replace("<>#" + ii + "<>", laParm[ii]);
		}
	}
void changeParm_2() {

   String[] laParm = new String[10];
   String[] tt=new String[]{hSmsParm,","};
   int pp=0, ll=0;
   while (tt[0].length()>0) {
      String ss=commString.token(tt);
      ll++;
      if (ll<4) continue;
      laParm[pp] =ss;
      pp++;
   }
   for (int ii = 0; ii < pp; ii++) {
      hSmsBody = hSmsBody.replace("<#" + ii + ">", laParm[ii]);
   }
}
	void sendSms() throws Exception {
		getParm();
		getUrl();
		hSmsSeqnoBuff = new String[500];
		hSmsReponseCode = new String[500];
		// --開啟連線
		try {
			URL url = new URL(strUrl);
			String host = url.getHost();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			SSLSocketFactory oldSocketFactory = null;
			HostnameVerifier oldHostnameVerifier = null;
			HttpsURLConnection https = (HttpsURLConnection) con;
			oldSocketFactory = trustAllHosts(https);
			oldHostnameVerifier = https.getHostnameVerifier();
			https.setHostnameVerifier(new HostnameVerifier() {				
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
			try (DataOutputStream out = new DataOutputStream(con.getOutputStream());) {
				out.write(hSendDesc.toString().getBytes("UTF-8"));
			}
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));) {
				String sLine;
				int seqNoCnt = -1, reponseCodeCnt = -1, startSeqno = 0, endSeqno = 0;
				while ((sLine = rd.readLine()) != null) {
					// --取Seqno;
					startSeqno = 0;
					endSeqno = 0;
					startSeqno = sLine.indexOf("[");
					endSeqno = sLine.indexOf("]");
					if (startSeqno >= 0) {
						seqNoCnt++;
						hSmsSeqnoBuff[seqNoCnt] = commString.bbMid(sLine, startSeqno + 1, endSeqno - 1);
						continue;
					}
					// --取Reponse
					startSeqno = 0;
					endSeqno = 0;
					startSeqno = sLine.indexOf("statuscode");
					if (startSeqno >= 0) {
						reponseCodeCnt++;
						startSeqno = 0;
						startSeqno = sLine.indexOf("=");
						hSmsReponseCode[reponseCodeCnt] = commString.bbMid(sLine, startSeqno + 1);
					}

				}
			}
			con.disconnect();
			updateSmsDetl();
		} catch (Exception e) {
		}

	}

	void updateSmsDetl() throws Exception {
		String hTempSeqno = "", hTempCode = "";

		for (int ii = 0; ii < hSmsSeqnoBuff.length; ii++) {
			hTempSeqno = hSmsSeqnoBuff[ii];
			hTempCode = hSmsReponseCode[ii];
			if (hTempSeqno ==null || hTempCode == null)
				break;
			if (commString.ssIn(hTempCode, "|0|1|2|4") == false)
				continue;
			daoTable = "sms_msg_dtl";
			updateSQL = "proc_flag = 'Y' , ";
			updateSQL += " send_flag = 'Y' ,";
			updateSQL += " mod_time  = sysdate,";
			updateSQL += " mod_user  = 'SYSTEM',";
			updateSQL += " mod_pgm   = 'SmsP010',";
			updateSQL += " mod_seqno = nvl(mod_seqno,0)+1 ";
			whereStr = "where msg_seqno = ? ";
			setString(1, hTempSeqno);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update sms_msg_dtl not found!", "", hTempSeqno);
			}
		}
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
		if (strUrl.isEmpty() == false)
			return;
		strUrl = String.format("%s?", smsIP);
//		strUrl = String.format("http://%s/b2c/mtk/SmBulkSend?", smsIP);
		strUrl += "username=" + user + "&password=" + pawd + "&Encoding_PostIn=" + enCode;
	}

	void initParm() {
		hSmsSeqno = "";
		hCellPhone = "";
		hReserveTime = "";
		hValidTime = "";
		hChiName = "";
		hSmsBody = "";
		hSmsParm = "";
		hMsgHour1 = 0;
		hMsgHour2 = 0;
		hBookDate = "";
		hBookTime = "";
	}

	void composeSms() {
		// --格式: 流水號$$手機$$預約時間$$有效時間$$客戶名稱$$主動回報網址$$簡訊內容
		String lsTemp = "";
		lsTemp = hSmsSeqno + "$$" + hCellPhone + "$$" + hReserveTime + "$$" + hValidTime + "$$" + hChiName + "$$"
				+ hRespUrl + "$$" + hSmsBody;
		hSendDesc.append(lsTemp).append("\r\n");
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
