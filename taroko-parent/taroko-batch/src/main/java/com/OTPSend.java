/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*  109/09/05  V1.00.06    yanghan     code scan issue                         *
*  110-01-07  V1.00.02    shiyuqi       修改无意义命名                                                                           *
*****************************************************************************/
package com;

import java.util.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import Dxc.Util.SecurityUtil;

public class OTPSend extends AccessDAO {
    CommCrd comc = new CommCrd();
    private String smsIP;
    private String user;
    private String pwd;
    public String mobileNumber;
    public String smsbody;

    public OTPReceive sendService() throws Exception {
       
        getParm();

        OTPReceive recvVO = new OTPReceive();
        Gson gson = new Gson();
        try {
            System.out.println(toParameters());
            //String urlstr = String.format("http://%s/ApiSMSC/Sms/SendSms", SmsIP);
            String urlstr = String.format("http://%s/ApiMEGA/Sms/SendSms", smsIP);
            URL url = new URL(urlstr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            //2020_0615 resolve Unreleased Resource: Streams by yanghan
            try( DataOutputStream wr = new DataOutputStream(con.getOutputStream())){
              wr.write(toParameters().getBytes("utf-8"));
            }
            //2020_0615 resolve Unreleased Resource: Streams by yanghan
            try(BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()))){
              String line;
              StringBuilder sb = new StringBuilder();
              while ((line = rd.readLine()) != null) {
                  sb.append(line);
              }

              try (JsonReader reader = new JsonReader(new StringReader(sb.toString()));) {
	              reader.beginObject(); // throws IOException
	              while (reader.hasNext()) {
	                  String str = reader.nextName();
	                  switch (str) {
	                      case "RowId":
	                          recvVO.RowId = reader.nextString();
	                          break;
	                      case "Cnt":
	                          recvVO.Cnt = reader.nextString(); 
	                          break;
	                      case "ErrorCode":
	                          recvVO.ErrorCode = reader.nextString();
	                          break;
	                  }
	              }
              }
              
              System.out.println(sb.toString());
            }       
        } catch (Exception e) {
            throw (e);
        }
        return recvVO;
    }

    private void getParm() throws IOException {
        String confFile = comc.getECSHOME() + "/conf/SMSParameter.txt";
        confFile = Normalizer.normalize(confFile, Normalizer.Form.NFKC);
        Properties props = new Properties();
        //2020_0615 resolve Unreleased Resource: Streams by yanghan
        try( FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(confFile))){
          props.load(fis);
          fis.close();
        } 
        smsIP = props.getProperty("SmsIP").trim();
        user = props.getProperty("User").trim();
        pwd = props.getProperty("Pwd").trim();
        
        Base64.Encoder encoder = Base64.getEncoder();
        pwd = encoder.encodeToString(pwd.getBytes("utf-8"));
    }

    public String toParameters() {
        //return "User=" + this.User + "&pwd=" + this.Pwd + "&MobileNumber=" + this.mobileNumber + "&Smsbody=" + this.smsbody;
        return "UID=" + this.user + "&PWD=" + this.pwd + "&DA=" + this.mobileNumber + "&SM=" + this.smsbody;
    }

    public class OTPReceive {
        public String RowId;//若ErrorCode非0時，不會回傳此欄位
        public String Cnt;
        /*
        ErrorCode(發送結果說明)：
        0簡訊已發至SMS server
        1傳入參數有誤
        2帳號/密碼錯誤
        3電話號碼格式錯誤
        4帳號已遭暫停使用
        7預約時間錯誤
        9簡訊內容為空白
       10資料庫存取或系統錯誤
       11餘額已為0
       12超過長簡訊發送字數
       13電話號碼為黑名單
       14僅接受POST method
       15指定發送代碼無效
       16發送截止時間錯誤
       19查無資料
       */
        public String ErrorCode;
    }
}