/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm3030 extends BaseEdit {
  Ptrm3030Func func;

  String stmtCycle = "";
  //String kk2 = "";
  int ilOk = 0;
  int ilErr = 0;
  int ilYet = 0;
  String mProgName = "ptrm3030";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      // dataProcess();
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

    // dddw_select();
    initButton();
  }

  @Override
  public void initPage() {
    if (strAction.equals("new") || empty(strAction)) {
      wp.colSet("min_mp_amt", "0");
      wp.colSet("normal_months", "0");
      wp.colSet("exceed_pay_days", "0");
      // wp.item_set("lgd_type", ls_lgd_type);
      // wp.item_set("from_type", ls_from_type);
      // wp.item_set("lgd_reason", ls_lgd_reason);
    }
  }

  private boolean getWhereStr() throws Exception {
	sqlParm.clear();  
    wp.whereStr = "where 1=1 ";

    if (empty(wp.itemStr("exStmtCycle")) == false) {
      wp.whereStr += " and stmt_cycle = :stmt_cycle ";
      setString("stmt_cycle", wp.itemStr("exStmtCycle"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;

    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "hex(rowid) as rowid, " + "stmt_cycle, " + "other_deduct_flag, "
        + "self_deduct_flag, " + "self_pay_flag ";

    wp.daoTable = "col_m0_parm";

    wp.whereOrder = "order by stmt_cycle";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    stmtCycle = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(stmtCycle)) {
      stmtCycle = itemKk("stmt_cycle");
    }
    if (isEmpty(stmtCycle)) {
      alertErr("資料鍵值 : 不可空白");
      return;
    }

    wp.selectSQL =
        "hex(rowid) as rowid, " + "stmt_cycle, " + "other_deduct_flag, " + "self_deduct_flag, "
            + "self_pay_flag " + "af_flag, " + "lf_flag, " + "cf_flag, " + "pf_flag, " + "ri_flag, "
            + "pn_flag, " + "exceed_pay_days, " + "min_mp_amt, " + "normal_months, " + "crt_date, "
            + "crt_user, " + "apr_user, " + "apr_date, " + "apr_flag, " + "mod_time, " + "mod_pgm ";

    wp.daoTable = "col_m0_parm";

    wp.whereStr = "where stmt_cycle = :stmt_cycle ";
    setString("stmt_cycle", stmtCycle);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + stmtCycle);
    }

  }

  @Override
  public void saveFunc() throws Exception {
    func = new Ptrm3030Func(wp);

    if (ofValidation() < 0)
      return;

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  // 1. 以stmt_cycle為條件，查詢table ptr_workday，若無資料，顯示警告訊息，並return -1。
  // 2. 檢查【normal_months】欄位內容，是否符合限制條件。
  // 3. 進行線上主管審核。通過者，新增此筆資料。
  int ofValidation() throws Exception {
    long llCnt;

    // --chk ptr_workday--
    // String ls_stmt_cycle = wp.item_ss("stmt_cycle");
    String lsStmtCycle = itemKk("stmt_cycle");
    String lsSql = "select count(*) ll_cnt from ptr_workday " + "where stmt_cycle = :stmt_cycle ";
    setString("stmt_cycle", lsStmtCycle);
    sqlSelect(lsSql);
    llCnt = (long) sqlNum("ll_cnt");
    if (llCnt == 0) {
      alertErr("ptr_workday無對帳單週期資料");
      return -1;
    }

    // -check normal_months-
    llCnt = 0;
    llCnt = (long) wp.itemNum("normal_months");
    if ((llCnt < 2 || llCnt > 12) && (llCnt != 0)) {
      alertErr("排除項目 4: 只能輸入 2-12 月 or 0");
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
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
