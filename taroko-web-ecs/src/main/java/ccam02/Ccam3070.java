
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 111-04-11  V1.00.03  ryan     增加一個特店名稱查詢條件
* 111-12-16  V1.00.04  JeffKung 增加特店英文名稱查詢條件
******************************************************************************/
package ccam02;

import ofcapp.BaseAction;

public class Ccam3070 extends BaseAction {
  String mchtNo = "", bankId = "";

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1" + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%")
        + sqlCol(wp.itemStr("ex_acq_bank"), "acq_bank_id", "like%");
    //20220411 add
    lsWhere	+= sqlCol(wp.itemStr("ex_mcht_name"), "mcht_name", "%like%");
    lsWhere	+= sqlCol(wp.itemStr("ex_mcht_eng_name"), "mcht_eng_name", "%like%");
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " mcht_no , " + " acq_bank_id , " + " mcht_name , " + " zip_code , "
        + " zip_city , " + " mcht_addr , " + " mcht_remark , " + " mod_user , " + " mcht_eng_name, "
        + " to_char(mod_time,'yyyymmdd') as mod_date , bin_type "

    ;
    wp.daoTable = "cca_mcht_bill";
    wp.whereOrder = " order by mcht_no, acq_bank_id ";
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
    mchtNo = wp.itemStr("data_k1");
    bankId = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mchtNo)) {
      mchtNo = itemkk("mcht_no");
    }

    if (empty(bankId)) {
      bankId = itemkk("acq_bank_id");
    }
    
    if(empty(mchtNo) || empty(bankId)) {
    	alertErr("收單行 或 特店代號 不可空白 !");
    	return ;
    }
    
    wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " mcht_no , " + " acq_bank_id , "
        + " mcht_name , " + " zip_code , " + " zip_city , " + " mcht_addr , " + " mcht_remark , "
        + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date," + " crt_user , "
        + " crt_date , " + " mcc_code , " + " mcht_eng_name , bin_type ";
    wp.daoTable = "cca_mcht_bill";
    wp.whereStr = "where 1=1" + sqlCol(mchtNo, "mcht_no") + sqlCol(bankId, "acq_bank_id");
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + mchtNo);
    }

  }

  @Override
  public void saveFunc() throws Exception {
    Ccam3070Func func = new Ccam3070Func();
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
