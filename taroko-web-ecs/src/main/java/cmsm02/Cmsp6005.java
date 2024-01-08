package cmsm02;
/* 
 * 2019-1204:  Alex  add error_desc 
 * 2019-0422:  JH    mod_user<>apr_user
 * 109-04-27   shiyuqi       updated for project coding standard     
 * 111-11-23   machao        覆核功能bug調整
 * 2022-11-25   Machao      不能覆核自己的案件 代碼調整         *  
* */
import ofcapp.BaseAction;

public class Cmsp6005 extends BaseAction {
  Cmsp6005Func func;

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
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // saveFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // saveFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // saveFunc();
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

    dddwSelect();
    initButton();
  }

  @Override
  public void queryFunc() throws Exception {

    String lsWhere = " where 1=1 "
    		+ "and apr_flag<>'Y' "
        + sqlCol(wp.itemStr("ex_mod_date"), "to_char(mod_time,'yyyymmdd')", ">=")
        + sqlCol(wp.itemStr("ex_case_type"), "case_type")
        + sqlCol(wp.itemStr("ex_mod_user"), "mod_user", "like%")
        + sqlCol(wp.itemStr("ex_dept_no"), "dept_no", "like%");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "case_type," + " case_id , " + " case_desc," + " send_code," + " dept_no,"
        + " dept_no2," + " dept_no3," + " crt_date," + " crt_user," + " apr_flag," + " apr_date,"
        + " apr_user," + " conf_mark," + " to_char(mod_time,'yyyymmdd') as mod_date" + ", mod_user";

    wp.daoTable = "CMS_CASETYPE ";
    wp.whereOrder = " order by case_id Asc ";

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
    apprDisabled("crt_user",wp.loginUser);
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
    func = new Cmsp6005Func();
    func.setConn(wp);
    int ilOk = 0, ilErr = 0, llErrApr = 0;
    String[] lsCt = wp.itemBuff("case_type");
    String[] lsCi = wp.itemBuff("case_id");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsCt.length;
    String optt = opt[0];
    int a = opt.length;
    if(optt.isEmpty() || a==0) {
    	alertErr2("資料錯誤: 請點選欲覆核資料");
    	return;
    }

    for (int ii = 0; ii < opt.length; ii++) {
      int rr = optToIndex(opt[ii]);
      if (rr < 0) {
        continue;
      }

      optOkflag(rr);
      if (checkAprUser(rr, "mod_user")) {
        llErrApr++;
        wp.colSet(rr, "error_desc", "覆核主管和異動經辦不可同一人");
        continue;
      }

      func.varsSet("case_type", lsCt[rr]);
      func.varsSet("case_id", lsCi[rr]);

      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
        ilOk++;
      } else {
        ilErr++;
        wp.colSet(rr, "error_desc", func.getMsg());
      }
      optOkflag(rr, rc);
    }

    // -re-Query-
    // queryRead();
    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 不可覆核=" + llErrApr);
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

}
