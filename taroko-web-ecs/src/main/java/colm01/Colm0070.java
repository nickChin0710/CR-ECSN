/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
******************************************************************************/
package colm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Colm0070 extends BaseEdit {
  Colm0070Func func;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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

    initButton();
  }

  @Override
  public void initPage() {
    strAction = "R";
    try {
      dataRead();
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

    if (getWhereStr() == false)
      return;

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", m_code_ttl_e1 " + ", m_code_ttl_s1 "
        + ", exc_ttl_lmt_1 " + ", m_code_owe_s1 " + ", m_code_owe_e1 " + ", exc_owe_lmt_1 "
        + ", m_code_ttl_1 " + ", m_code_owe_1 " + ", m_code_ttl_2 " + ", exc_ttl_lmt_2 "
        + ", m_code_owe_2 " + ", exc_owe_lmt_2 " + ", m_code_ttl_3 " + ", m_code_owe_3 "
        + ", req_debt_lmt " + ", gen_cs_day " + ", trans_col_flag " + ", cycle_n_days "
        + ", trans_col_day " + ", trans_col_day2 " + ", trans_col_day3 " + ", trans_bad_debt_day "
        + ", terminate_amt1 " + ", terminate_year1 " + ", terminate_month1 " + ", terminate_year2 "
        + ", terminate_month2 ";

    wp.daoTable = "col_param";
    wp.whereStr = "where 1=1 ";

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料");
    }

  }

  @Override
  public void saveFunc() throws Exception {
    func = new Colm0070Func(wp);

    if (ofValidation() < 0)
      return;

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  int ofValidation() throws Exception {
    String mCodeS1 = "", mCodeE1 = "", tMonth = "", colDay1 = "", colDay2 = "", colDay3 = "";

    // Mcode區間合理性檢核。(前項數字不能大於後項數字)
    mCodeS1 = wp.itemStr("m_code_ttl_s1");
    mCodeE1 = wp.itemStr("m_code_ttl_e1");
    if (chkStrend(mCodeS1, mCodeE1) == false) {
      alertErr("[M Code 區間]  輸入錯誤");
      return -1;
    }
    mCodeS1 = wp.itemStr("m_code_owe_s1");
    mCodeE1 = wp.itemStr("m_code_owe_e1");
    if (chkStrend(mCodeS1, mCodeE1) == false) {
      alertErr("[M Code 區間]  輸入錯誤");
      return -1;
    }

    // 【trans_col_day】、【trans_col_day2】、【trans_col_day3】三個欄位，不能大於【28】，且若<>0，前項需小於後項。
    colDay1 = wp.itemStr("trans_col_day");
    colDay2 = wp.itemStr("trans_col_day2");
    colDay3 = wp.itemStr("trans_col_day3");
    if ((toNum(colDay1) >= toNum(colDay2) && toNum(colDay2) != 0)
        || (toNum(colDay2) >= toNum(colDay3) && toNum(colDay3) != 0)
        || (toNum(colDay1) >= toNum(colDay3) && toNum(colDay3) != 0) || toNum(colDay1) > 28
        || toNum(colDay2) > 28 || toNum(colDay3) > 28) {
      alertErr("[每月催收日期]  輸入錯誤");
      return -1;
    }

    // 【ID項下特定還款來源最近一次還款日】…的年月，兩個月份的內容，只能填入0-12 的數字。
    tMonth = wp.itemStr("terminate_month1");
    if (toNum(tMonth) > 12) {
      alertErr("[月份]  輸入錯誤");
      return -1;
    }
    tMonth = wp.itemStr("terminate_month2");
    if (toNum(tMonth) > 12) {
      alertErr("[月份]  輸入錯誤");
      return -1;
    }

    // -check approve- 主管覆核
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return -1;
    }

    return 1;
  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    this.btnModeAud();
    // }
  }

}
