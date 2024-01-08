/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;
/*期中覆審評分類別設定  V.2019-1206
 * 2019-1206:  Alex  add initButton
 * 2018-0119:	JH		modify
 * */
import ofcapp.BaseAction;

public class Rskm1090 extends BaseAction {

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
      wp.optionKey = wp.colStr("ex_trial_type");
      dddwList("dddw_trial_type", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where wf_type='RSK_TRIAL_TYPE'");
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    queryBefore();

    String lsWhere = " where 1=1 ";

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  void queryBefore() {
    String sql1 = " select " + " score_type_desc , " + " trial_type  " + " from rsk_score_type "
        + " where 1=1 " + " and score_type = ? " + commSqlStr.rownum(1);
    sqlSelect(sql1, new Object[] {wp.itemStr("ex_score_type")});
    if (sqlRowNum <= 0) {
      wp.colSet("ex_score_desc", "");
      wp.colSet("ex_trial_type", "");
      return;
    }
    wp.colSet("ex_score_desc", sqlStr("score_type_desc"));
    wp.colSet("ex_trial_type", sqlStr("trial_type"));
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " cond_code , " + " cond_desc , " + " data_from , " + " exec_flag , "
        + " '' as trial_type , " + " '' as trial_desc ";
    wp.daoTable = "rsk_score_parm ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by cond_code Asc ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    listWkdata();
    wp.setListCount(1);
    wp.setPageValue();

  }

  void listWkdata() {
    int tlCkCnt = 0;
    String sql1 = " select " + " score_flag , " + " trial_type ,"
        + " (select wf_desc from ptr_sys_idtab where wf_type='RSK_TRIAL_TYPE' and wf_id =trial_type) as trial_desc "
        + " from rsk_score_type " + " where score_type = ? " + " and cond_code = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if ((wp.colEq(ii, "data_from", "1"))) {
        wp.colSet(ii, "tt_data_from", "本行資料");
      } else if ((wp.colEq(ii, "data_from", "2"))) {
        wp.colSet(ii, "tt_data_from", "JCIC資料");
      }

      sqlSelect(sql1, new Object[] {wp.itemStr("ex_score_type"), wp.colStr(ii, "cond_code")});
      if (sqlRowNum > 0) {
        if (eqIgno(sqlStr("score_flag"), "Y"))
          wp.colSet(ii, "score_flag", "checked");
        wp.colSet(ii, "trial_type", sqlStr("trial_type"));
        wp.colSet(ii, "trial_desc", sqlStr("trial_desc"));
        tlCkCnt++;
      }
    }
    wp.colSet("tl_ck_cnt", "" + tlCkCnt);
    wp.colSet("tl_cnt", "" + wp.selectCnt);
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    String[] lsCc = wp.itemBuff("cond_code");
    String[] lsCd = wp.itemBuff("cond_desc");
    String[] lsDf = wp.itemBuff("data_from");
    String lsScoreType = wp.itemStr("ex_score_type");
    String lsCcoreTypeDesc = wp.itemStr("ex_score_desc");
    String lsTrialType = wp.itemStr("ex_trial_type");
    String lsZzAprUser = wp.itemStr("approval_user");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsCc.length;

    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    if (wp.itemEmpty("ex_score_type")) {
      alertErr2("評分類別不可空白");
      return;
    }


    rskm02.Rskm1090Func func = new rskm02.Rskm1090Func();
    func.setConn(wp);
    func.varsSet("ex_score_type", lsScoreType);
    func.varsSet("ex_score_desc", lsCcoreTypeDesc);
    func.varsSet("ex_trial_type", lsTrialType);
    func.varsSet("approval_user", lsZzAprUser);

    if (func.deleteIdno() != 1) {
      errmsg("資料處理失敗");
      return;
    }
    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;

      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("cond_code", lsCc[rr]);
      func.varsSet("cond_desc", lsCd[rr]);
      func.varsSet("data_from", lsDf[rr]);

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
    }

    if (ilErr > 0) {
      errmsg(func.getMsg());
      return;
    }

    alertMsg("資料處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
