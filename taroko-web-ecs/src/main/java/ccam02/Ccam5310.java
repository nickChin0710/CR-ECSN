/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import ofcapp.BaseAction;

public class Ccam5310 extends BaseAction {
  String groupCode = "", acqBankId = "", mchtNo = "";

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
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "ccam5310")) {
        wp.optionKey = wp.colStr(0, "ex_group_code");
        dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
            "where group_code<>'0000'");
      }
    } catch (Exception ex) {
    }
    try {
      if (eqIgno(wp.respHtml, "ccam5310_detl")) {
        wp.optionKey = wp.colStr(0, "kk_group_code");
        dddwList("dddw_group_code1", "ptr_group_code", "group_code", "group_name",
            "where group_code<>'0000'");
      }
    } catch (Exception ex) {
    }


  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1" + sqlCol(wp.itemStr("ex_group_code"), "group_code")
        + sqlCol(wp.itemStr("ex_acq_bank"), "acq_bank_id")
        + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%")

    ;


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        " group_code ," + " acq_bank_id , " + " mcht_no , " + " crt_date , " + " crt_user  "

    ;
    wp.daoTable = "cca_group_mcht";
    wp.whereOrder = " order by group_code, acq_bank_id, mcht_no ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
    pageQuery();


    wp.setListCount(1);
    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();


  }

  @Override
  public void querySelect() throws Exception {
    groupCode = wp.itemStr("data_k1");
    acqBankId = wp.itemStr("data_k2");
    mchtNo = wp.itemStr("data_k3");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(groupCode)) {
      groupCode = itemkk("group_code");
    }
    if (empty(acqBankId)) {
      acqBankId = itemkk("acq_bank_id");
    }
    if (empty(mchtNo)) {
      mchtNo = itemkk("mcht_no");
    }

    wp.selectSQL = "group_code ," + " acq_bank_id ," + " mcht_no , "
    // + " uf_mcht_bill(mcht_no,acq_bank_id) as mcht_name , "
        + " apr_date , " + " apr_user ," + " crt_user , " + " crt_date , " + " mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " hex(rowid) as rowid , mod_seqno";
    wp.daoTable = "cca_group_mcht A ";
    wp.whereStr = "where 1=1" + sqlCol(groupCode, "group_code") + sqlCol(acqBankId, "acq_bank_id")
        + sqlCol(mchtNo, "mcht_no");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + groupCode);
      return;
    }
    // dataRead_after();
  }

  // --2019/05/29 畫面取消該欄位
  // void dataRead_after(){
  // if(wp.col_empty("mcht_name")){
  // String sql1 = " select mcht_name from cca_mcht_base where acq_bank_id = ? and mcht_no = ? ";
  // sqlSelect(sql1,new Object[]{wp.col_ss("acq_bank_id"),wp.col_ss("mcht_no")});
  // if(sql_nrow>0){
  // wp.col_set("mcht_name", sql_ss("mcht_name"));
  // }
  // }
  // }

  @Override
  public void saveFunc() throws Exception {
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    ccam02.Ccam5310Func func = new ccam02.Ccam5310Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      this.saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
