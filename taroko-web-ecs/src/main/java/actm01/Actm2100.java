/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-22  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.02  shiyuqi    updated for project coding standard        *
* 111-10-27  V1.00.03  Simon      sync codes with mega                       *
******************************************************************************/
package actm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actm2100 extends BaseEdit {
  Actm2100Func func;
	String mProgName = "actm2100";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      if (wp.itemStr("ex_dual_flag").equals("Y")) {
        strAction = "U";
        updateFunc();
      } else if (wp.itemStr("ex_dual_flag").equals("N")) {
        strAction = "A";
        insertFunc();
      }
    }

    dddwSelect();
    initButton();
  }

  void getWhereStr1() {
    wp.whereStr = " where dual_key = :ls_dual_no and func_code = '0708' ";
    setString("ls_dual_no", wp.itemStr("ex_acct_type") + wp.itemStr("ex_acct_key"));
  }

  void getWhereStr2() {
    wp.whereStr = "where acct_type = :ex_acct_type and acct_key = :ex_acct_key";
    setString("ex_acct_type", wp.itemStr("ex_acct_type"));
    setString("ex_acct_key", wp.itemStr("ex_acct_key"));
  }

  @Override
  public void queryFunc() throws Exception {
		//查詢權限檢查，參考【fAuthQuery】
		String lsAcctKey = "";

		busi.func.ColFunc func = new busi.func.ColFunc();
		func.setConn(wp);
		
		if(wp.itemEmpty("ex_acct_key")==false) {
			lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));
			if(lsAcctKey.length()!=11){
				alertErr2("帳戶帳號:輸入錯誤");
				return ;
			}
			
			if (func.fAuthQuery(mProgName, commString.mid(lsAcctKey, 0,10))!=1) { 
	      	alertErr2(func.getMsg()); 
	      	return ; 
	      }
			
		}	
		
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
    wfChkCname();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " log_data,mod_seqno ";
    wp.daoTable = " act_dual ";
    wp.whereOrder = " ";
    getWhereStr1();
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      wp.selectSQL = " rc_use_b_adj ex_rc_use_b_adj " + " ,rc_use_indicator ex_rc_use_indicator "
      // + " ,rc_use_b_adj ex_rc_use_b_adj "
          + " ,rc_use_s_date ex_rc_use_s_date " + " ,rc_use_e_date ex_rc_use_e_date "
          + " ,acct_status ex_acct_status ";

      wp.daoTable = " act_acno ";
      wp.whereOrder = " ";
      getWhereStr2();
      pageQuery();
      wp.setListCount(1);
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }
      listWkdataB();
    } else {
      listWkdataA();
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    func = new Actm2100Func(wp);

    String lsDualNo = wp.itemStr("ex_acct_type") + wp.itemStr("ex_acct_key").trim();
    func.varsSet("ls_dual_no", lsDualNo);

    if (strAction.equals("A") || strAction.equals("U")) {
      if (ofValidation() != 1) {
        return;
      }

    }
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      if (strAction.equals("U")) {
        alertMsg("Err,資料修改失敗!");
      } else if (strAction.equals("A")) {
        alertMsg("Err,資料新增到 act_dual 失敗!");
      }
      alertErr2(func.getMsg());
    } else {
      if (strAction.equals("U")) {
        alertMsg("OK,資料修改成功!");
      } else if (strAction.equals("A")) {
        alertMsg("OK,資料新增到 act_dual 成功!");
      }
    }
    this.sqlCommit(rc);

  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }

    String sKey = "1st-page";
    if (wp.respHtml.equals("actm2100")) {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("btnDelete_disable", "");
      this.btnModeAud(sKey);
    }

  }

  @Override
  public void dddwSelect() {

    try {
      wp.optionKey = wp.itemStr("ex_acct_type");
      this.dddwList("dddw_ex_acct_type", "ptr_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");

    } catch (Exception ex) {
    }
  }

  void wfChkCname() throws Exception {
    String asCname = "", asCorpCname = "";

    String sqlSelect = "select id_p_seqno,corp_p_seqno " + " from  act_acno "
        + " where acct_type = :acct_type " + " and acct_key = :acct_key ";
    setString("acct_type", wp.itemStr("ex_acct_type"));
    setString("acct_key", wp.itemStr("ex_acct_key"));
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      return;
    }
    String isIdPSeqno = sqlStr("id_p_seqno");
    String isCorpPSeqno = sqlStr("corp_p_seqno");

    String sqlSelect2 = " select chi_name from  crd_idno where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", isIdPSeqno);
    sqlSelect(sqlSelect2);
    if (sqlRowNum > 0) {
      asCname = sqlStr("chi_name");
    }
    wp.colSet("ex_cname", asCname);

    String sqlSelect3 = " select chi_name from  crd_corp where corp_p_seqno = :corp_p_seqno ";
    setString("corp_p_seqno", isCorpPSeqno);
    sqlSelect(sqlSelect3);
    if (sqlRowNum > 0) {
      asCorpCname = sqlStr("chi_name");
    }
    wp.colSet("ex_corp_name", asCorpCname);
    wp.colSet("acct_type", wp.itemStr("ex_acct_type"));
    wp.colSet("acct_key", wp.itemStr("ex_acct_key"));
  }

  int ofValidation() throws Exception {
    String lsKey1 = "", lsKey2 = "", lsChaFlag = "";
    if (empty(wp.itemStr("ex_acct_key").trim())) {
      return -1;
    }
    lsKey1 = wp.itemStr("ex_acct_type");
    lsKey2 = wp.itemStr("ex_acct_key");
    if (!lsKey1.equals(wp.itemStr("acct_type"))) {
      alertErr("帳戶帳號不能修改");
      return -1;
    }
    if (!lsKey2.equals(wp.itemStr("acct_key"))) {
      alertErr("帳戶帳號不能修改");
      return -1;
    }

    // 商務卡不可修改
    String sqlSelect =
        "select rc_use_flag " + " from ptr_acct_type " + " where acct_type = :ls_key1 ";
    setString("ls_key1", lsKey1);
    sqlSelect(sqlSelect);
    String lsRcUseFlag = sqlStr("rc_use_flag");
    if (lsRcUseFlag.equals("3")) {
      alertErr("錯誤~此類別不可調整,exclamation!");
      return -1;
    }
    // 檢核起迄日期--
    // 讀取 act_acno 原始資料,以判斷此筆是新增或修改
    String sqlSelect2 = " select rc_use_indicator,rc_use_s_date,rc_use_e_date " + " from act_acno "
        + " where acct_type = :ls_key1 " + " and acct_key = :ls_key2 ";
    setString("ls_key1", lsKey1);
    setString("ls_key2", lsKey2);
    sqlSelect(sqlSelect2);
    String lsVal2 = sqlStr("rc_use_s_date");
    if (!empty(lsVal2)) {
      lsChaFlag = "Y"; // 表修改--
    } else {
      lsChaFlag = "N"; // 表新增--
    }
    // 起迄日期檢核(共同規則)
    if ((wp.itemStr("ex_rc_use_indicator").equals("2")
        || wp.itemStr("ex_rc_use_indicator").equals("3"))
        && empty(wp.itemStr("ex_rc_use_s_date"))) {
      alertErr("錯誤~[允用RC碼設定] 時, [起始日期] 必須要有值,exclamation!");
      return -1;
    }
    if (!empty(wp.itemStr("ex_rc_use_e_date"))) {
      if (wp.itemStr("ex_rc_use_s_date").compareTo(wp.itemStr("ex_rc_use_e_date")) > 0) {
        alertErr("錯誤~[起日]不可大於[迄日],exclamation");
        return -1;
      }
    }
    // 起迄日期檢核(新增)
    if (lsChaFlag.equals("N")) {
      if (!empty(wp.itemStr("ex_rc_use_s_date"))) {
        if (wp.itemStr("ex_rc_use_s_date").compareTo(wp.sysDate) < 0) {
          alertErr("錯誤~新增![起始日] 不可小於 [系統日],exclamation!");
          return -1;
        }
      }
    }
    // 起迄日期檢核(修改)
    if (lsChaFlag.equals("Y")) {
      if (!empty(wp.itemStr("ex_rc_use_s_date"))) {
        if (lsVal2.compareTo(wp.sysDate) < 0) {
          if (wp.itemStr("ex_rc_use_s_date").compareTo(lsVal2) < 0) {
            alertErr("錯誤~修改1![起始日] 不可小於 [原系統日],exclamation!");
            return -1;
          }
        } else {
          if (wp.itemStr("ex_rc_use_s_date").compareTo(wp.sysDate) < 0) {
            alertErr("錯誤~修改2![起始日] 不可小於 [系統日],exclamation!");
            return -1;
          }
        }
      }

    }

    if (wp.itemStr("ex_rc_use_indicator").equals(wp.itemStr("rc_use_indicator"))) {
      if (wp.itemStr("ex_rc_use_s_date").equals(wp.itemStr("rc_use_s_date"))) {
        if (wp.itemStr("ex_rc_use_e_date").equals(wp.itemStr("rc_use_e_date"))) {
          alertErr("資料未異動, 不可修改");
          return -1;
        }
      }
    }


    return 1;
  }

  void listWkdataA() {
    String exRcUseBAdj = "", exRcUseIndicator = "", exRcUseSDate = "",
        exRcUseEDate = "", exAcctStatus = "", lsDualData = "";
    wp.colSet("ex_dual_flag", "Y");
    // deblock log_data
    lsDualData = wp.colStr("log_data");
    if (lsDualData.length() >= 1) {
      exRcUseBAdj = lsDualData.substring(0, 1);
    }
    if (lsDualData.length() >= 2) {
      exRcUseIndicator = lsDualData.substring(1, 2);
    }
    if (lsDualData.length() >= 10) {
      exRcUseSDate = lsDualData.substring(2, 10);
    }
    if (lsDualData.length() >= 18) {
      exRcUseEDate = lsDualData.substring(10, 18);
    }
    if (lsDualData.length() >= 19) {
      exAcctStatus = lsDualData.substring(18, 19);
    }
    if (exRcUseEDate.equals("99991231")) {
      exRcUseEDate = "";
    }
    wp.colSet("ex_rc_use_b_adj", exRcUseBAdj);
    wp.colSet("ex_rc_use_indicator", exRcUseIndicator);
    wp.colSet("ex_rc_use_s_date", exRcUseSDate);
    wp.colSet("ex_rc_use_e_date", exRcUseEDate);
    wp.colSet("ex_acct_status", exAcctStatus);
    wp.colSet("tt_ex_acct_status",
        commString.decode(exAcctStatus, ",1,2,3,4,5", ",1-正常,2-逾放,3-催收,4-呆帳,5-結清"));
  }

  void listWkdataB() {
    wp.colSet("ex_dual_flag", "N");
    String exRcUseEDate = wp.colStr("ex_rc_use_e_date");
    if (exRcUseEDate.equals("99991231")) {
      exRcUseEDate = "";
    }
    String exAcctStatus = wp.colStr("ex_acct_status");
    wp.colSet("ex_rc_use_e_date", exRcUseEDate);
    wp.colSet("tt_ex_acct_status",
        commString.decode(exAcctStatus, ",1,2,3,4,5", ",1-正常,2-逾放,3-催收,4-呆帳,5-結清"));
  }


}
