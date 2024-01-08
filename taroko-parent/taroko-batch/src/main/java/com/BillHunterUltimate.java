/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE    Version    AUTHOR                       DESCRIPTION              *
*  --------  -------------------  ------------------------------------------  *
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  109/09/04  V1.00.06    Zuwei     code scan issue    
*                                                                             *
******************************************************************************/
package com;

import java.util.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;

import com.google.gson.Gson;

import Dxc.Util.SecurityUtil;

public class BillHunterUltimate extends AccessDAO {
  CommCrd comc = new CommCrd();
  private String hBHUIP = "";
  private String hProjectCategoryCode; /* 帳單類別代號 X(5) */
  public String hUserName; /* 收件者姓名 X(50) */
  public String hUserEmail; /* 收件者Email X(100) */
  public String hUserCustId; /* 客戶身份字號 X(20) */
  public String hUserKey; /* 客戶 Key 值 X(30) */
  public String hUserMainhtml; /* 信件HTML內文 X(MAX) */

  public String sendService() throws Exception {

    getParm();
    // Gson gson = new Gson();
    StringBuilder sb = new StringBuilder();

    try {

      System.out.println("[BHU] " + toParameters());
      String urlstr = String.format("http://%s/billhunter_app/sendnow.aspx", hBHUIP);
      URL url = new URL(urlstr);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      // 2020_0615 resolve Unreleased Resource: Streams by yanghan
      try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
        wr.write(toParameters().getBytes("utf-8"));
      }
      // 按照給的修改原則 本來只需要加上try塊即可 但是此處之前有一個try catch塊 為防止新加的try塊影響到原有的try塊 故而在新加的try塊中補全了catch
      // 2020_0615 resolve Unreleased Resource: Streams by yanghan
      try (BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
        String line;
        while ((line = rd.readLine()) != null) {
          sb.append(line);
        }
        System.out.println("[BHU] " + sb.toString());
      }
    } catch (Exception e) {
      throw (e);
    }
    return sb.toString();
  }

  private void getParm() throws IOException {
    String confFile = comc.getECSHOME() + "/conf/BHUParameter.txt";
    confFile = SecurityUtil.verifyPath(confFile);
    Properties props = new Properties();
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (FileInputStream fis = new FileInputStream(confFile)) {
      props.load(fis);
      fis.close();
      hBHUIP = props.getProperty("BHUIP").trim();
      hProjectCategoryCode = props.getProperty("project_category_code").trim();
    }
    // User = props.getProperty("User").trim();
    // Pwd = props.getProperty("Pwd").trim();
  }

  public String toParameters() {
    return "project_category_code="
        + this.hProjectCategoryCode
        + "&user_name="
        + this.hUserName
        + "&user_email="
        + this.hUserEmail
        + "&user_cust_id="
        + this.hUserCustId
        + "&user_key="
        + this.hUserKey
        + "&user_mainhtml="
        + this.hUserMainhtml;
  }


}
