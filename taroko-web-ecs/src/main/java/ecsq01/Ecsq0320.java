/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-12-07  V1.00.00  DM 參數維護                                           *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                           *
******************************************************************************/

package ecsq01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import it.sauronsoftware.ftp4j.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"unchecked","deprecation"})
public class Ecsq0320 extends BaseAction {

  String groupCheck = "", dummyCode = "";
  int seqNo = 0, parmSeq = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;
    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "Q":
        queryFunc();
        break; /* 查詢功能 */
      case "S":
        querySelect();
        break; /* 動態查詢 */
      default:
        break;
    }

    initButton();
  }

  /* 查詢功能 */
  @Override
  public void queryFunc() throws Exception {

    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();
    String[] interName = {"01,聯信中心授權系統,socket,client,10020", "02,財金收單授權系統,socket,client,9010",
        "03,一卡通系統,socket,client,9020", "04,悠遊卡系統,socket,client,9030", "05,網路商城系統,http,server,9040",
        "06,I-cash 愛金卡系統,socket,client,9050", "07,新語音系統,socket,client,9060",
        "08,語音系統(IVR),socket,client,9070", "09,WEB BATCH 連線,socket,server,9080",
        "10,新網路銀行系統,http,server,8010", "11,網路銀行系統(E_BANK),http,server,8020",
        "12,行動支付系統,http,client,8030", "13,簡訊(SMS)系統,http,client, ,", "14,IBON 系統,http,server,8040",
        "15,ACS 3D OTP 連線,http,server,8050", "16,QRCode Payment 連線,http,server,8060",
        "17,WEB service 系統連線,http,client,80", "18,Line BC 連線,http,server,8080",
        "19,IBM SNA APPC 連線,api,client, ,", "20,IBM SNA APPC 連線,api,server, ,",
        "21,FTP 傳檔,api,client/server,21,", "22,IBM MQ 連線,api,client,1414",
        "23,IBM MQ 連線,api,server,1414", "24,Email  連線,api,client, ,"};

    SecureRandom random = null;
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      // random = new Random(new Date().getTime());
      throw new RuntimeException("init SecureRandom failed.", e);
    }
    int min = 11, max = 60;
    int randomNum = 0;
    for (int i = 0; i < interName.length; i++) {

      String[] intfData = interName[i].split(",");
      if ((i + 1) <= 9) {
        wp.setValue("SER_NO", "0" + (i + 1), i);
      } else {
        wp.setValue("SER_NO", "" + (i + 1), i);
      }
      wp.setValue("SER_NUM", "" + (i + 1), i);
      wp.setValue("intf_no", intfData[0], i);
      wp.setValue("interface_name", intfData[1], i);
      wp.setValue("interface_type", intfData[2], i);
      wp.setValue("interface_role", intfData[3], i);
      wp.setValue("interface_port", intfData[4], i);
      wp.setValue("monitor_date", wp.sysDate, i);
      randomNum = random.nextInt(max + 1 - min) + min; // ThreadLocalRandom.current().nextInt(min,
                                                       // max + 1);
      wp.setValue("monitor_time", wp.sysTime.substring(0, 4) + randomNum, i);
      wp.setValue("status_code", "00", i);
      if (i == 1) {
        wp.setValue("status_code", "C2", i);
      }
      randomNum = random.nextInt(max + 1 - min) + min; // ThreadLocalRandom.current().nextInt(min,
                                                       // max + 1);
      wp.setValue("duration_time", "0." + randomNum, i);
      wp.setValue("STATUS_COLOR", "#e6ffcc", i);
      if (!wp.getValue("status_code", i).equals("00")) {
        wp.setValue("STATUS_COLOR", "#ffcccc", i);
      }
    }
    wp.selectCnt = interName.length;
    wp.listCount[0] = interName.length;
    wp.setPageValue();
    return;
  }

  @Override
  public void initPage() {

  }

  @Override
  public void initButton() {}

  @Override
  public void procFunc() {}

  @Override
  public void userAction() {}

  @Override
  public void dddwSelect() {}

  /* 動態查詢 */
  @Override
  public void querySelect() throws Exception {

    wp.pageControl();
    String[] interName =
        {"01,456399XXXXXX2781,0101001249,01000001", "02,532199XXXXXX4537,0101001248,01000002",
            "03,356699XXXXXX9342,0101001266,01000003", "04,456398XXXXXX3425,0101001221,01000004",
            "05,456398XXXXXX1298,0101001289,01000005", "06,456397XXXXXX9109,0101001212,01000006",
            "07,456396XXXXXX3499,0101001200,01000007", "08,321399XXXXXX1011,0101001299,01000008",
            "09,546399XXXXXX8109,0101001257,01000009", "10,533399XXXXXX3312,0101001234,01000010",
            "11,495559XXXXXX2109,0101001211,01000011", "12,567733XXXXXX2789,0101001290,01000012",
            "13,456388XXXXXX2000,0101001276,01000013", "14,456377XXXXXX1200,0101001296,01000014",
            "15,556399XXXXXX5434,0101001200,01000015", "16,346399XXXXXX6965,0101001202,01000016"};

    SecureRandom random = null;
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      // random = new Random(new Date().getTime());
      throw new RuntimeException("init SecureRandom failed.", e);
    }
    int min = 0, max = 0;
    int randomNum = 0;
    for (int i = 0; i < interName.length; i++) {

      String[] intfData = interName[i].split(",");
      if ((i + 1) <= 9) {
        wp.setValue("SER_NO", "0" + (i + 1), i);
      } else {
        wp.setValue("SER_NO", "" + (i + 1), i);
      }
      wp.setValue("SER_NUM", "" + (i + 1), i);
      wp.setValue("channel_no", intfData[0], i);
      wp.setValue("card_no", intfData[1], i);
      wp.setValue("mcht_no", intfData[2], i);
      wp.setValue("term_no", intfData[3], i);
      wp.setValue("tx_date", wp.sysDate, i);
      min = 11;
      max = 60;
      randomNum = random.nextInt(max + 1 - min) + min; // ThreadLocalRandom.current().nextInt(min,
                                                       // max + 1);
      wp.setValue("tx_time", wp.sysTime.substring(0, 4) + randomNum, i);
      wp.setValue("status_code", "00", i);
      if (i == 7 || i == 9) {
        wp.setValue("status_code", "X1", i);
      }
      min = 2;
      max = 90;
      randomNum = random.nextInt(max + 1 - min) + min; // ThreadLocalRandom.current().nextInt(min,
                                                       // max + 1);
      wp.setValue("duration_time", "0.0" + randomNum, i);
      wp.setValue("STATUS_COLOR", "#e6ffcc", i);
      if (!wp.getValue("status_code", i).equals("00")) {
        wp.setValue("STATUS_COLOR", "#ffcccc", i);
      }

      min = 300;
      max = 1500;
      randomNum = random.nextInt(max + 1 - min) + min; // ThreadLocalRandom.current().nextInt(min,
                                                       // max + 1);
      wp.setValue("tx_amt", "" + randomNum, i);

      min = 300;
      max = 500;
      randomNum = random.nextInt(max + 1 - min) + min; // ThreadLocalRandom.current().nextInt(min,
                                                       // max + 1);
      wp.setValue("total_cnt", "" + randomNum, i);

      min = 0;
      max = 7;
      randomNum = random.nextInt(max + 1 - min) + min; // ThreadLocalRandom.current().nextInt(min,
                                                       // max + 1);
      if (i == 7 || i == 9) {
        wp.setValue("time_out_cnt", "" + randomNum, i);
      } else {
        wp.setValue("time_out_cnt", "0", i);
      }
    }

    wp.selectCnt = interName.length;
    wp.listCount[0] = interName.length;
    wp.setPageValue();

    return;
  }

  /* 資料讀取 */
  @Override
  public void dataRead() throws Exception {

    return;
  }

  @Override
  public void saveFunc() throws Exception {

    return;
  }

} // end of class
