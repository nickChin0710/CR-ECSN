/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       add initButton                             *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import ofcapp.BaseAction;

public class Ptrm0073 extends BaseAction {

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
    } else if (eqIgno(wp.buttonCode, "A2")) {
      // -新增明細-
      procFunc();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    wp.pageRows = 999;
    wp.selectSQL = " A.* , hex(A.rowid) as rowid , B.atm_rate *100 as atm_rate ";
    wp.daoTable = " ptr_class A left join ptr_class_rate B on A.class_code = B.class_code ";
    wp.whereStr = " where 1=1 ";
    wp.whereOrder = " order by A.class_code Asc ";
    pageQuery();
    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }

    wp.setListCount(0);
  }

  @Override
  public void saveFunc() throws Exception {
    int ilCnt = 0, ilOk = 0, ilErr = 0;
    ptrm01.Ptrm0073Func func = new ptrm01.Ptrm0073Func();
    func.setConn(wp);
    wp.listCount[0] = wp.itemRows("class_code");
    if (checkApproveZz() == false)
      return;

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsRowid = wp.itemBuff("rowid");

    for (int ii = 0; ii < wp.itemRows("class_code"); ii++) {
      if (this.checkBoxOptOn(ii, aaOpt) == false)
        continue;
      ilCnt++;
      func.varsSet("rowid", lsRowid[ii]);
      rc = func.deleteClass();
      if (rc != 1) {
        wp.colSet(ii, "ok_flag", "X");
        dbRollback();
        ilErr++;
        continue;
      } else {
        wp.colSet(ii, "ok_flag", "V");
        sqlCommit(1);
        ilOk++;
        continue;
      }
    }

    if (ilCnt <= 0) {
      errmsg("請勾選欲刪除資料");
      return;
    }

    alertMsg("存檔完成 , 成功:" + ilOk + " 失敗:" + ilErr);

  }

  @Override
  public void procFunc() throws Exception {
    wp.listCount[0] = wp.itemRows("class_code");

    if (checkApproveZz() == false)
      return;

    ptrm01.Ptrm0073Func func = new Ptrm0073Func();
    func.setConn(wp);

    rc = func.insertClass();
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      wp.colSet("ex_class_code", "");
      wp.colSet("ex_beg_credit_lmt", "");
      wp.colSet("ex_end_credit_lmt", "");
      alertMsg("卡人等級新增完成");
    }

  }

  @Override
  public void initButton() {
    btnModeAud("xx");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
