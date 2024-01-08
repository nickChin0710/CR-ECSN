/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-12  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.02 YangFang   updated for project coding standard        *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ipsm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Ipsm0050 extends BaseEdit {
  Ipsm0050Func func;
  CommString commString = new CommString();
  String[] aaDataCode = null;

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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("btnUpdate_disable", "disabled");
    try {
    	dataRead();
    } catch(Exception e) {}
    
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.whereStr = " where 1=1 ";
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = " hex(rowid) as rowid " + " ,parm_type " + " ,seq_no " + " ,parm_desc "
        + " ,mcode_cond " + " ,payment_rate " + " ,mcode_amt " + " ,block_cond " + " ,block_codes "
        + " ,imp_list_cond " + " ,apr_date " + " ,apr_user " + " ,mod_user " + " ,mod_time "
        + " ,mod_pgm " + " ,mod_seqno ";

    wp.daoTable = " ips_comm_parm ";
    wp.whereOrder = " ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and parm_type='REJ_AUTH' and seq_no = 1 ";
    pageSelect();
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    list_wkdata();
    wp.colSet("block_codes_desc", addCommaByEachTwoBytes(wp.colStr("block_codes")));
    wp.colSet("btnUpdate_disable", "");
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ipsm0050Func(wp);
    
    if(checkApproveZz() == false)
    	return ;
    
    if (ofValidation() != 1) {
      return;
    }
    dropComma(wp.itemStr("data_code"));
    rc = func.ipsDelete();
    if (rc < 1) {
      sqlCommit(0);
      alertErr2("Delete IPS_COMM_DATA err");
      return;
    }

    for (int i = 0; i < aaDataCode.length; i++) {
      if (empty(aaDataCode[i])) {
        continue;
      }
      func.varsSet("aa_data_code", aaDataCode[i]);
      rc = func.ipsInsert();
      if (rc != 1) {
        sqlCommit(0);
        alertErr2("insert IPS_COMM_DATA err");
        return;
      }
    }
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();

    }
    this.btnModeAud("XX");
  }
  
  @Override
  public void dddwSelect() {

  }

  void list_wkdata() {
    String tmpStr = "";
    String sqlSelect = "Select hex(rowid) as rowid, data_code " + "from ips_comm_data "
        + "where parm_type='REJ_AUTH' " + "and data_type='01'";
    sqlSelect(sqlSelect);
    for (int i = 0; i < sqlRowNum; i++) {
      tmpStr += sqlStr(i, "data_code");
    }
    wp.colSet("data_code_desc", addCommaByEachTwoBytes2(tmpStr));
  }

  private String addCommaByEachTwoBytes(String data) {
    String buf = "";
    // int cnt = 0;
    for (int i = 0; i < data.length(); i++) {
      if (i % 2 == 0 && i != 0) {
        buf = buf + ",";
        // cnt++;
      }
      // if (i == 0) cnt++;
      buf = buf + data.substring(i, i + 1);
    }

    return buf;
  }

  private String addCommaByEachTwoBytes2(String data) {
    String buf = "";
    // int cnt = 0;
    for (int i = 0; i < data.length(); i++) {
      if (i % 11 == 0 && i != 0) {
        buf = buf + ",";
        // cnt++;
      }
      // if (i == 0) cnt++;
      buf = buf + data.substring(i, i + 1);
    }

    return buf;
  }

  int ofValidation() {
    if (wp.itemStr("mcode_cond").equals("Y")) {
      if (wp.itemNum("payment_rate") <= 0) {
        alertErr("Mcode 須大於 0");
        return -1;
      }      
    }    
    if (wp.itemStr("imp_list_cond").equals("Y")) {
      if (empty(wp.itemStr("data_code_desc"))) {
        alertErr("一卡通卡號 不可空白");
        return -1;
      }
    }

    return 1;
  }

  void dropComma(String data) {
    int i = 0;
    String[] datas = data.split(",");
    aaDataCode = new String[datas.length];
    for (String dat : datas) {
      aaDataCode[i] = dat;
      i++;
    }
  }
}
