/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-10  V1.00.00  Alex       program initial                            *
* 109-04-29  V1.00.01  Tanwei       updated for project coding standard
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package smsm01;

import ofcapp.BaseAction;

public class Smsm0020 extends BaseAction {
String msgId = "";
@Override
public void userAction() throws Exception {
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
    saveFunc();
  } else if (eqIgno(wp.buttonCode, "U")) {
    /* 更新功能 */
    saveFunc();
  } else if (eqIgno(wp.buttonCode, "D")) {
    /* 刪除功能 */
    saveFunc();
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
  } else if (eqIgno(wp.buttonCode, "C")) {
    // -發送簡訊測試-
    procFunc();
  }

}

@Override
public void dddwSelect() {
  // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {

  String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_msg_id"), "msg_id");

  wp.whereStr = lsWhere;
  wp.queryWhere = wp.whereStr;
  wp.setQueryMode();

  queryRead();
}

@Override
public void queryRead() throws Exception {
  wp.pageControl();
  wp.selectSQL = " msg_id , " + " msg_desc , " + " msg_content , "
      + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_user , " + " apr_date , "
      + " apr_user ";

  wp.daoTable = " sms_msg_content ";
  wp.whereOrder = " order by msg_id Asc ";
  pageQuery();

  if (sqlNotFind()) {
    alertErr2("此條件查無資料");
    return;
  }

  wp.setListCount(0);
  wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
  msgId = wp.itemStr("data_k1");
  dataRead();
}

@Override
public void dataRead() throws Exception {
  if (empty(msgId))
    msgId = itemkk("msg_id");
  if (empty(msgId)) {
    alertErr2("簡訊代碼: 不可空白");
    return;
  }

  wp.selectSQL = " msg_id , " + " msg_desc , " + " msg_content , "
      + " substring(msg_content,1,67) as msg_content1 , "
      + " substring(msg_content,68,67) as msg_content2 , "
      + " substring(msg_content,135,67) as msg_content3 , "
      + " substring(msg_content,202,67) as msg_content4 , "
      + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_user , " + " mod_pgm , "
      + " mod_seqno , " + " apr_date , " + " apr_user , " + " apr_flag , " + " crt_user , "
      + " crt_date , " + " crt_time ," + " hex(rowid) as rowid ";
  wp.daoTable = " sms_msg_content ";
  wp.whereStr = " where 1=1 " + sqlCol(msgId, "msg_id");

  pageSelect();
  if (sqlNotFind()) {
    alertErr2("此條件查無資料");
    return;
  }

}

@Override
public void saveFunc() throws Exception {

  if (checkApproveZz() == false)
    return;

  smsm01.Smsm0020Func func = new smsm01.Smsm0020Func();
  func.setConn(wp);

  rc = func.dbSave(strAction);
  sqlCommit(rc);
  if (rc != 1) {
    alertErr2(func.getMsg());
  } else
    saveAfter(false);

}

@Override
public void procFunc() throws Exception {
  String lsSmsContent = "" , lsUrl = "";
  lsSmsContent = wp.itemStr("msg_content1") + wp.itemStr("msg_content2")
      + wp.itemStr("msg_content3") + wp.itemStr("msg_content4");
  // --各項參數檢核
  if (empty(lsSmsContent)) {
    alertErr2("簡訊內容: 不可空白");
    return;
  }
  if (wp.itemEmpty("ex_cell_phone")) {
    alertErr2("手機號碼: 不可空白");
    return;
  }
  if (wp.itemEmpty("ex_msg_acct")) {
    alertErr2("三竹簡訊帳號: 不可空白");
    return;
  }
  if (wp.itemEmpty("ex_msg_pd")) {
    alertErr2("三竹簡訊密碼: 不可空白");
    return;
  }
  // --取三竹IP
  lsUrl = getUrl();
  if(lsUrl.isEmpty()) {
	  alertErr("三竹IP位置系統參數尚未設定 !");
	  return;
  }
  // --替換參數
  for (int ii = 0; ii < 9; ii++) {
    lsSmsContent = lsSmsContent.replace("<#" + ii + ">", wp.itemStr("ex_msg_" + ii));
  }  
  smsm01.SmsSend24 sms = new smsm01.SmsSend24();
  if (wp.itemEmpty("ex_chi_name") == false)
    sms.setName(wp.itemStr("ex_chi_name"));
  sms.setPhoneNumber(wp.itemStr("ex_cell_phone"));
  sms.setSmsBody(lsSmsContent);
  sms.setUserName(wp.itemStr("ex_msg_acct"));
  sms.setUserPd(wp.itemStr("ex_msg_pd"));
  sms.setUrl(lsUrl);
  if (sms.sendSms() == -1) {
//	wp.log("Error:"+sms.e1.getMessage());
//	wp.log(sms.s1);
//	wp.log(sms.s2);
//	wp.log(sms.s3);
//	wp.log(sms.s4);
//	wp.log(sms.s5);
//	wp.log(sms.s6);
    alertErr2("測試簡訊發送失敗 !");
  } else {	  
//	  wp.log(sms.s1);
//	  wp.log(sms.s2);
//	  wp.log(sms.s3);
//	  wp.log(sms.s4);
//	  wp.log(sms.s5);
//	  wp.log(sms.s6);
    alertMsg("測試簡訊發送成功 , 請至三竹網頁查看 !");
  }

}

String getUrl() {
	
	String sql1 = "select wf_value as url from ptr_sys_parm where 1=1 and wf_parm = 'SMS_CONNECT' and wf_key = 'SMS_URL' ";
	sqlSelect(sql1);
	
	if(sqlRowNum <=0) {
		return "";
	}
	
	return sqlStr("url");
}

@Override
public void initButton() {
  btnModeAud(wp.colStr("rowid"));

}

@Override
public void initPage() {
  // TODO Auto-generated method stub

}

}
