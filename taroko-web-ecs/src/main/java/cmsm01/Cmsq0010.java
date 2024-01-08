package cmsm01;
/** 歸戶餘額查詢
 * 2019-0711   JH    modify
 * V.2018-0809
 * */

import ofcapp.BaseAction;
import taroko.base.CommString;

public class Cmsq0010 extends BaseAction {

  @Override
  public void userAction() throws Exception {
    wp.pgmVersion("V.18-0809");
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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
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

  // public int ccaM2090_read(String a_idno) throws Exception {
  // wp.item_set("ex_mast_id",a_idno);
  // busi.cmsm01.Cmsq0010Func func = new busi.cmsm01.Cmsq0010Func();
  // func.setConn(wp);
  // rc =func.readData();
  // return rc;
  // }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    cmsm01.Cmsq0010Func func = new cmsm01.Cmsq0010Func();
    func.setConn(wp);
    rc = func.readData(wp.itemStr2("ex_mast_id"));
    sqlCommit(1);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
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
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
