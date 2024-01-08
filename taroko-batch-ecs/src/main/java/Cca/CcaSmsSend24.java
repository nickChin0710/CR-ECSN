/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-10  V1.00.00  Alex       program initial                            *
* 109-04-29  V1.00.01  Tanwei       updated for project coding standard
* 109-06-29  V1.00.02  Zuwei        fix code scan issue
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
* 111-12-21  V1.00.04  Alex       弱點修正 , SSL伺服器驗證                                                                *
******************************************************************************/
package Cca;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CcaSmsSend24 {
  int icnt = 0;
  String isCellphone = "";
  String isChiname = "";
  String isMsgDesc = "";
  String isUsrName = "";
  String isUsrPd = "";
  String isSendTime = "";
  String isValidTimd = "";
  String isEncode = "Big5";
  String isMsgId = "";
  String url = "";
  StringBuffer isUrl = new StringBuffer();
  int iiRespStatus = 0;
  int iiAmount = 0;
 // public String s1 = "" , s2 = "" , s3 = "" , s4 = "" , s5 = "" , s6 = "";
  public Exception e1 ;
  public void setName(String lsChiName) throws Exception {
	String lsTempName = "";
	lsTempName = URLEncoder.encode(lsChiName,"Big5");
    isChiname = lsTempName;
  }

  public void setPhoneNumber(String lsPhoneNumber) {
    isCellphone = lsPhoneNumber;
  }

  public void setUserName(String lsUserName) throws Exception {
	String lsTempName = "";
	lsTempName = URLEncoder.encode(lsUserName,"Big5");
    isUsrName = lsTempName;
  }

  public void setUserPd(String lsUserPd) {
    isUsrPd = lsUserPd;
  }

  public void setSmsBody(String lsSmsBody) throws Exception {
	 String lsTempBody = "" ;
     lsTempBody = URLEncoder.encode(lsSmsBody,"Big5");
     isMsgDesc = lsTempBody;
  }
  
  public void setUrl(String lsUrl) throws Exception {
	  url = lsUrl ;
  }
  
  public String getMsgSeqNo() throws Exception {
	  return isMsgId;
  }
  
  public int getReptStatus() throws Exception {
	  return iiRespStatus ;
  }
  
  public void matchURL() {
	isUrl.append(url+"?"); // --目前為測試環境網址 , 必要
//	isUrl.append("https://60.251.36.134:8101/b2c/mtk/SmSend?"); // --目前為測試環境網址 , 必要
//	isUrl.append("http://60.251.36.134:8001/b2c/mtk/SmSend?"); // --目前為測試環境網址 , 必要  
//    isUrl.append("http://stgsmsb2c.mitake.com.tw:8001/b2c/mtk/SmSend?"); // --目前為測試環境網址 , 必要
    isUrl.append("username=" + isUsrName); // --三竹帳號 , 必要
    isUrl.append("&password=" + isUsrPd); // --三竹密碼 , 必要
    isUrl.append("&encoding=" + isEncode); // --指定編碼 , Big5、UTF-8 ...
    isUrl.append("&dstaddr=" + isCellphone); // --客戶手機 , 必要
    if (isChiname.isEmpty() == false)
      isUrl.append("&DestName=" + isChiname); // --客戶姓名
    isUrl.append("&smbody=" + isMsgDesc); // --簡訊內容 , 必要
  }

  public int sendSms() {
    matchURL();
    try {
      procSendSms();
    } catch (Exception e) {
      e1 = e ;
      return -1;
    }
    return 1;
  }
  
  void procSendSms() throws Exception {
	  URL url = new URL(isUrl.toString());	  	
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
      }});	 	 	  	  
	  con.setDoOutput(true);
	  con.setRequestMethod("POST"); 
	  con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//	  HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
//	  urlConnection.setRequestMethod("POST"); 
//	  urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
//	  urlConnection.setDoOutput(true); 
//	  urlConnection.connect();
	    try (BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
	      String sLine;
	      while ((sLine = rd.readLine()) != null) {
	//    	if(icnt==0) {
	//    		s2 = "icnt:"+icnt+" , sLine:"+sLine;
	//    	} else if(icnt==1) {
	//    		s3 = "icnt:"+icnt+" , sLine:"+sLine;
	//    	} else if(icnt==2) {
	//    		s4 = "icnt:"+icnt+" , sLine:"+sLine;
	//    	} else if(icnt==3) {
	//    		s5 = "icnt:"+icnt+" , sLine:"+sLine;
	//    	} else if(icnt==4) {
	//    		s6 = "icnt:"+icnt+" , sLine:"+sLine;
	//    	}
	        icnt = icnt + 1;
	        switch (icnt) {
	          case 2:
	            isMsgId = sLine.substring(6, sLine.length()).trim();
	            break;
	          case 3:
	            iiRespStatus = Integer.parseInt(sLine.substring(11, sLine.length()).trim());
	            break;
	          case 4:
	            iiAmount = Integer.parseInt(sLine.substring(13, sLine.length()).trim());
	            break;
	        }
	      }	  
	} catch (Exception e) {
	  throw new Exception("URL error:" + e.getMessage());
	}	
	  
	  
  }
  
//  void procSendSms() throws Exception {
//	  //--http
//    try {
//      URL url = new URL(isUrl.toString());    
////      s1 = "URL:"+url;
//      HttpURLConnection con = (HttpURLConnection) url.openConnection();
//      con.setRequestMethod("GET");
//      con.setDoOutput(true);
//      try (BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
//	      String sLine;
//	      while ((sLine = rd.readLine()) != null) {
////	    	if(icnt==0) {
////	    		s2 = "icnt:"+icnt+" , sLine:"+sLine;
////	    	} else if(icnt==1) {
////	    		s3 = "icnt:"+icnt+" , sLine:"+sLine;
////	    	} else if(icnt==2) {
////	    		s4 = "icnt:"+icnt+" , sLine:"+sLine;
////	    	} else if(icnt==3) {
////	    		s5 = "icnt:"+icnt+" , sLine:"+sLine;
////	    	} else if(icnt==4) {
////	    		s6 = "icnt:"+icnt+" , sLine:"+sLine;
////	    	}
//	        icnt = icnt + 1;
//	        switch (icnt) {
//	          case 2:
//	            isMsgId = sLine.substring(6, sLine.length()).trim();
//	            break;
//	          case 3:
//	            iiRespStatus = Integer.parseInt(sLine.substring(11, sLine.length()).trim());
//	            break;
//	          case 4:
//	            iiAmount = Integer.parseInt(sLine.substring(13, sLine.length()).trim());
//	            break;
//	        }
//	      }
//      }
//    } catch (Exception e) {
//      throw new Exception("URL error:" + e.getMessage());
//    }
//  }

  private static final TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return new java.security.cert.X509Certificate[]{};
      }

      public void checkClientTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
      }

      public void checkServerTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
      }
  }};
  
  private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
      SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
      try {
//          SSLContext sc = SSLContext.getInstance("TLS");
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
