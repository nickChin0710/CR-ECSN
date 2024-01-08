/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1213  V1.00.01  Alex        add initButton
* 109-04-27  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package mktm05;

import java.util.Arrays;

import ofcapp.BaseAction;

public class Mktm5030 extends BaseAction {
  String dataKK1 = "", validDate = "", datakk3 = "", paramType = "";

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "mktm5030")) {
        wp.optionKey = wp.colStr(0, "ex_group_code");
        dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
            "where group_code<>'0000'");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(validDate)) {
      validDate = "00000000";
    }
    if (empty(paramType)) {
      paramType = "1";
    }
    wp.selectSQL = " valid_date , " + " promote_type , " + " new_card_days , "
        + " add_card_point , " + " sup_card_point , " + " add_card_max , " + " sup_card_max , "
        + " apr_flag , " + " apr_date , " + " apr_user , " + " crt_user , " + " crt_date , "
        + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_pgm , "
        + " mod_seqno , " + " hex(rowid) as rowid , " + " no_match_point ";

    wp.daoTable = " mkt_ds_parm1 ";
    wp.whereStr = " where 1=1 " + sqlCol(validDate, "valid_date") + sqlCol(paramType, "param_type");
    wp.whereOrder = " order by apr_flag ";

    pageSelect();

    if (sqlNotFind()) {
      selectOK();
      return;
    }

    selectDetail();

  }

  void selectDetail() throws Exception {
    wp.pageRows = 999;
    wp.selectSQL = " data_code1 , " + " data_code2 , " + " data_code1||data_code2 as data_code12 , "
        + " data_remark1 , " + " data_remark2 , " + " add_point1 , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " promote_type ";

    wp.daoTable = " mkt_ds_parm1_detl ";
    wp.whereStr = " where 1=1 " + sqlCol(validDate, "valid_date") + sqlCol(paramType, "param_type")
        + sqlCol(wp.colStr("apr_flag"), "apr_flag");
    wp.whereOrder = " order by promote_type , data_code1 , data_code2 ";
    pageQuery();

    if (sqlNotFind()) {
      selectOK();
      wp.colSet("ind_num", 0);
      return;
    }

    wp.setListCount(0);
    wp.colSet("ind_num", wp.selectCnt);

  }

  @Override
  public void saveFunc() throws Exception {

    Mktm5030Func func = new Mktm5030Func();
    func.setConn(wp);
    wp.listCount[0] = wp.itemRows("promote_type");

    if (checkApproveZz() == false) {
      return;
    }

    if (wp.itemEmpty("rowid")) {
      rc = func.dbInsert();
    } else {
      rc = func.dbUpdate();
    }

    if (rc != 1) {
      errmsg(func.getMsg());
      return;
    }

    procFunc();

    saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsDataCode1 = wp.itemBuff("data_code1");
    String[] lsDataCode2 = wp.itemBuff("data_code2");
    String[] lsDataCode12 = wp.itemBuff("data_code12");
    String[] lsDataRemark1 = wp.itemBuff("data_remark1");
    String[] lsDataRemark2 = wp.itemBuff("data_remark2");
    String[] lsDddPoint1 = wp.itemBuff("add_point1");
    String lsValidDate = "";
    String lsParamType = wp.itemStr("param_type");
    String[] lsPromoteType = wp.itemBuff("promote_type");
    this.optNumKeep(wp.itemRows("promote_type"), aaOpt);
    lsValidDate = wp.itemStr("valid_date");

    wp.listCount[0] = wp.itemRows("promote_type");

    Mktm5030Func func = new Mktm5030Func();
    func.setConn(wp);

    int ll = -1;
    for (String tmpStr : lsDataCode12) {
      ll++;
      wp.colSet(ll, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }

      if (ll != Arrays.asList(lsDataCode12).indexOf(tmpStr)) {
        wp.colSet(ll, "ok_flag", "!");
        llErr++;
      }
    }
    if (llErr > 0) {
      sqlCommit(-1);
      alertErr("資料值重複: " + llErr);
      return;
    }

    func.varsSet("valid_date", lsValidDate);
    func.varsSet("param_type", lsParamType);


    if (func.deleteAllDetl() != 1) {
      errmsg(func.getMsg());
      sqlCommit(-1);
      return;
    }

    for (int ii = 0; ii < wp.itemRows("data_code1"); ii++) {
      if (checkBoxOptOn(ii, aaOpt))
        continue;

      func.varsSet("data_code1", lsDataCode1[ii]);
      func.varsSet("data_code2", lsDataCode2[ii]);
      func.varsSet("data_remark1", lsDataRemark1[ii]);
      func.varsSet("data_remark2", lsDataRemark2[ii]);
      func.varsSet("add_point1", lsDddPoint1[ii]);
      func.varsSet("promote_type", lsPromoteType[ii]);
      if (func.insertDetl() == 1) {
        llOk++;
        wp.colSet(ii, "ok_flag", "V");
        continue;
      } else {
        llErr++;
        wp.colSet(ii, "ok_flag", "X");
      }

    }

    if (llErr > 0) {
      sqlCommit(-1);
    } else {
      sqlCommit(1);
    }

    if (llErr > 0) {
      alertMsg("存檔失敗");
      return;
    }

    alertMsg("存檔完成");


    wp.colSet("ind_num", wp.itemRows("data_code1"));
  }

  @Override
  public void initButton() {
    btnModeAud("XX");
  }

  @Override
  public void initPage() {
    if (wp.colEmpty("ind_num"))
      wp.colSet("ind_num", 0);
    try {
      dataRead();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
