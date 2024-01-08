package cmsm01;
/** FANCY卡帳務明細查詢 V.2018-0821
* 109-04-27  shiyuqi       updated for project coding standard     *  
** 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      * 
 * */
import java.io.IOException;

import busi.FuncAction;

public class Cmsq0020Func extends FuncAction {
  // --Cond Data
  String isAcctType = "", isAcctKey = "", isCardNo = "", isComboAcctNo = "", isChiName = "",
      isAprUser = "";
  // --Socket parm
  // String s_parm = "";
  // --Socket Data
  // String s_Rc = "", s_rcvdata = "";
  double liTempMath = 0.0;

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // --baseData
    isAcctType = wp.itemStr("ex_acct_type");
    isAcctKey = wp.itemStr("ex_acct_key");
    isCardNo = wp.itemStr("ex_card_no");
    isComboAcctNo = wp.itemStr("ex_combo_acct_no");
    isChiName = wp.itemStr("ex_acno_name");
    isAprUser = wp.itemStr("approval_user");
    // --Socket
    String sParm = "MNF1";
    sParm += "03";
    sParm += commString.rpad(isComboAcctNo, 11);
    sParm += commString.repeat("0", 16); // "0000000000000000";
    sParm += commString.rpad(modUser, 5);
    sParm += commString.rpad(isAprUser, 5);

    // -call_cpisend-一次收完--
    ecsfunc.EcsCallbatch ooAppc = new ecsfunc.EcsCallbatch(wp);
    int liRecv = 0;
    String lsRcvdata = "";
    try {
      lsRcvdata = ooAppc.commAppc(sParm, liRecv);
      if (liRecv == 0) {
        wfAddLog("1", "OK but 0 bytes recvd");
        errmsg("資訊處未回應");
        return rc;
      }
    } catch (IOException ex) {
      errmsg("call APPC fail");
      return rc;
    }

    parseDataMnf1(lsRcvdata);
    return rc;
  }

  public int wfAddLog(String parm1, String parm2) {

    String lsData = "";
    if (!empty(wp.itemStr("ex_acct_key"))) {
      lsData = wp.itemStr("ex_acct_key");
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      lsData = wp.itemStr("ex_card_no");
    }

    sql2Insert("cms_mnf1_log");
    addsqlYmd(" mod_date");
    addsqlTime(", mod_time");
    addsqlParm(",?", ", mod_user", modUser);
    addsqlParm(",?", ", mod_dept", wp.loginDeptNo);
    addsqlParm(",?", ", apr_user", wp.itemStr2("approval_user"));
    addsqlParm(",?", ", trans_type", "03");
    addsqlParm(",?", ", card_no", lsData);
    addsqlParm(",?", ", acct_no", wp.itemStr2("ex_combo_acct_no"));
    addsqlParm(",?", ", chi_name", wp.itemStr2("ex_acno_name"));
    addsqlParm(",?", ", err_type", parm1);
    addsqlParm(",?", ", err_reason", parm2);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("查詢記錄新增失敗");
    }

    return rc;
  }

  // --
  public int parseDataMnf1(String sRcvdata) {
    String[] aaData = new String[2];
    aaData[0] = sRcvdata;
    String lsRc = commString.token2(aaData, 4);
    String cntryFlag = "";

    if (eqIgno(lsRc, "0000")) {
      cntryFlag = commString.token2(aaData, 11).trim();
      wp.colSet(0, "acct_no", cntryFlag);
      cntryFlag = commString.token2(aaData, 8).trim();
      wp.colSet(0, "card_no", cntryFlag);
      cntryFlag = commString.token2(aaData, 1).trim();
      wp.colSet(0, "card_status", cntryFlag);
      cntryFlag = commString.token2(aaData, 2).trim();
      wp.colSet(0, "fancy_card_status", cntryFlag);
      cntryFlag = commString.token2(aaData, 13).trim();
      wp.colSet(0, "fancy_limit", commString.strToNum(cntryFlag) / 100);
      cntryFlag = commString.token2(aaData, 13).trim();
      wp.colSet(0, "fancy_limit_used", commString.strToNum(cntryFlag) / 100);
      cntryFlag = commString.token2(aaData, 1).trim();
      wp.colSet(0, "bill_flag", cntryFlag);
      cntryFlag = commString.token2(aaData, 1).trim();
      wp.colSet(0, "s_sign", cntryFlag);
      cntryFlag = commString.token2(aaData, 13).trim();
      liTempMath = commString.strToNum(cntryFlag) / 100;
      if (eqIgno(wp.colStr("s_sign"), "+")) {
        wp.colSet(0, "balance", liTempMath);
      } else if (eqIgno(wp.colStr("s_sign"), "-")) {
        liTempMath = 0 - liTempMath;
        wp.colSet(0, "balance", liTempMath);
      }
      cntryFlag = commString.token2(aaData, 26).trim();
      // -跨國交易-
      cntryFlag = commString.token2(aaData, 1).trim();
      wp.colSet(0, "cntry_flag", cntryFlag);

      wfAddLog("0", "");
      return 1;
    }

    // -- error

    wfAddLog("2", lsRc);

    if (eqIgno(lsRc, "A001")) {
      errmsg("");
    } else if (eqIgno(lsRc, "A001")) {
      errmsg("A001:功能別錯誤");
    } else if (eqIgno(lsRc, "A002")) {
      errmsg("A002:查詢期間為 空白");
    } else if (eqIgno(lsRc, "A003")) {
      errmsg("A003:查詢期間 起迄輸入錯誤");
    } else if (eqIgno(lsRc, "A004")) {
      errmsg("A004:查詢超過前六月歷史資料");
    } else if (eqIgno(lsRc, "A005")) {
      errmsg("A005:非歡喜卡客戶");
    } else if (eqIgno(lsRc, "A088")) {
      errmsg("A088: RECEIVE DATA ERROR");
    } else if (eqIgno(lsRc, "A089")) {
      errmsg("A089: 讀取檔案錯誤");
    } else if (eqIgno(lsRc, "A090")) {
      errmsg("A090: 查無資料");
    } else {
      if (empty(sRcvdata.trim())) {
        errmsg("資訊處未回應");
      } else {
        errmsg("ERROR: " + sRcvdata);
      }
    }

    return 1;
  }
}
