/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1206  V1.00.01  Alex  add initButton
* 109-04-27  V1.00.02  Tanwei       updated for project coding standard      *
* 110-01-05  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package rskm02;

import ofcapp.BaseAction;

import java.lang.reflect.Array;
import java.util.Arrays;

public class Rskm1085 extends BaseAction {

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
      wp.optionKey = wp.colStr("kk_score_type");
      dddwList("dddw_score_type",
          "select DISTINCT score_type db_code, score_type||'_'||score_type_desc as db_desc"
              + " from rsk_score_type  where 1=1 order by 1");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("kk_score_type"), "score_type");
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.selectSQL = " * ";
    wp.daoTable = "rsk_score_level";
    wp.whereOrder = " order by trial_level ";
    pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
      for (int ii = 0; ii < 20; ii++) {
        if (ii < 9) {
          wp.colSet(ii, "ser_num", "0" + (ii + 1));
        } else {
          wp.colSet(ii, "ser_num", "" + (ii + 1));
        }
        wp.colSet(ii, "trial_level", "" + (ii + 1));
      }
    }

    wp.listCount[0] = 20;
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

    rskm02.Rskm1085Func func = new rskm02.Rskm1085Func();
    func.setConn(wp);
    int[] lsData = new int[20];
    int llErr = 0, llOk = 0, errValue = 0;
    String[] lsTrialScore = wp.itemBuff("trial_score");
    String[] lsOpt = wp.itemBuff("opt");
    wp.listCount[0] = 20;
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    // --檢查是否重複
    int rr = -1;
    for (String zz : lsTrialScore) {
      rr++;
      if (checkBoxOptOn(rr, lsOpt) || empty(lsTrialScore[rr])
          || eqIgno(lsTrialScore[rr], "0")) {
        lsTrialScore[rr] = "";
        continue;
      }
      if (rr != Arrays.asList(lsTrialScore).indexOf(zz)) {
        wp.colSet(rr, "ok_flag", "!");
        llErr++;
      }
    }

    if (llErr > 0) {
      alertMsg("資料值重覆 !");
      return;
    }

    // --取值
    int cnt = -1;
    for (int ii = 0; ii < 20; ii++) {
      cnt++;
      if (checkBoxOptOn(ii, lsOpt) || empty(lsTrialScore[ii])) {
        lsData[cnt] = 0;
        continue;
      }
      lsData[cnt] = commString.strToInt(lsTrialScore[ii]);
    }

    Arrays.sort(lsData);

    if (func.deleteData() < 0) {
      errmsg(func.getMsg());
      return;
    }

    int llLevel = 21;
    for (int cnt1 = 0; cnt1 < 20; cnt1++) {
      llLevel--;
      func.varsSet("trial_level", "" + llLevel);
      func.varsSet("trial_score", "" + lsData[cnt1]);
      if (func.dataProc() != 1) {
        llErr++;
        errValue = lsData[cnt1];
        break;
      } else {
        llOk++;
      }
    }

    if (llErr > 0) {
      errmsg("錯誤區間 : >=" + errValue);
      this.dbRollback();
    } else {
      sqlCommit(1);
      alertMsg("存檔完成");
    }
  }

  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
