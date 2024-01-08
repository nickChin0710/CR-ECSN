/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-03  V1.00.00  Andy Liu   program initial                            *
* 108-12-10  V1.00.01  Alex            add initButton
* 109-01-03  V1.00.02  Justin Wu  updated for archit.  change
* 109-04-21  V1.00.03  yanghan  修改了變量名稱和方法名稱*
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/

package dbbm01;

import java.util.Arrays;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Dbbm0100 extends BaseAction {

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        wfAjaxMcht();
        break;
      default:
        break;
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {

    if (wp.itemEmpty("ex_rsk_group") && wp.itemEmpty("ex_mcht_no")) {
      alertErr2("群組代號 or 特店代號 不可皆為空白 !");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_rsk_group"), "rsk_group", "like%")
        + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " hex(rowid) as rowid ," + " rsk_group ," + " group_name ," + " mcht_no ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno, "
        + " mcht_no||rsk_group||group_name as old_data ";
    wp.daoTable = "dbb_rsk_merchant";

    pageQuery();
    if (sqlNotFind()) {
      alertErr2("查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
    wp.colSet("ind_num", wp.selectCnt * wp.currPage);
    if (!wp.itemEmpty("ex_rsk_group")) {
      wp.colSet("ex_rsk_group", wp.colStr("rsk_group"));
      wp.colSet("ex_group_name", wp.colStr("group_name"));
    }

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
    int lsOk = 0, isError = 0, isError2 = 0;
    boolean listEmpty = false;
    String[] lsRskGroup = wp.itemBuff("rsk_group");
    String[] lsGroupName = wp.itemBuff("group_name");
    String[] lsMchtNo = wp.itemBuff("mcht_no");
    String[] lsOldData = wp.itemBuff("old_data");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    String exRskGroup = wp.itemStr("ex_rsk_group");
    String exGroupName = wp.itemStr("ex_group_name");
    wp.listCount[0] = wp.itemRows("mcht_no");

    if (empty(exRskGroup)) {
      alertErr("群組代碼錯誤: 不能為空白 !!");
      return;
    }

    if (checkApproveZz() == false) {
      return;
    }

    Dbbm0100Func func = new Dbbm0100Func();
    func.setConn(wp);

    for (int ii = 0; ii < wp.itemRows("mcht_no"); ii++) {
      if (empty(lsRskGroup[ii])) {
        listEmpty = true;
        break;
      }
    }

    if (listEmpty && wp.itemEmpty("ex_rsk_group")) {
      alertErr2("群組代號: 不可空白 !");
      return;
    }


    int ll = -1;
    for (String param : lsMchtNo) {
      ll++;
      if (checkBoxOptOn(ll, aaOpt))
        continue;

      if (ll != Arrays.asList(lsMchtNo).indexOf(param)) {
        wp.colSet(ll, "ok_flag", "!");
        isError++;
      }
    }

    if (isError > 0) {
      alertErr("資料值重複: " + isError);
      return;
    }

    int xx = -1;
    for (String param : lsMchtNo) {
      xx++;
      if (checkBoxOptOn(xx, aaOpt))
        continue;
      if (!empty(lsOldData[xx]))
        continue;
      if (checkMcht(param) == false) {
        wp.colSet(xx, "ok_flag", "X");
        isError2++;
      }
    }

    if (isError2 > 0) {
      alertErr("特店代號 已存在: " + isError2);
      return;
    }

    for (int ii = 0; ii < wp.itemRows("mcht_no"); ii++) {
      if (checkBoxOptOn(ii, aaOpt)) {
        func.varsSet("rowid", lsRowid[ii]);
        if (func.deleteDetl() == 1) {
          lsOk++;
          wp.colSet(ii, "ok_flag", "V");
          continue;
        } else {
          isError++;
          wp.colSet(ii, "ok_flag", "X");
          continue;
        }
      } else if (empty(lsOldData[ii])) {
        func.varsSet("mcht_no", lsMchtNo[ii]);
        func.varsSet("rsk_group", exRskGroup);
        func.varsSet("group_name", exGroupName);
        if (func.insertDetl() == 1) {
          lsOk++;
          wp.colSet(ii, "ok_flag", "V");
          continue;
        } else {
          isError++;
          wp.colSet(ii, "ok_flag", "X");
          continue;
        }
      } else if (!eqIgno(lsMchtNo[ii] + lsRskGroup[ii] + lsGroupName[ii], lsOldData[ii])) {
        func.varsSet("mcht_no", lsMchtNo[ii]);
        func.varsSet("rowid", lsRowid[ii]);
        if (func.updateDetl() == 1) {
          lsOk++;
          wp.colSet(ii, "ok_flag", "V");
          continue;
        } else {
          isError++;
          wp.colSet(ii, "ok_flag", "X");
          continue;
        }
      } else
        continue;
    }

    if (lsOk > 0) {
      sqlCommit(1);
    }

    alertMsg("存檔完成 , 成功:" + lsOk + " 失敗:" + isError);

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub
    this.btnModeAud("XX");
  }

  @Override
  public void initPage() {
    wp.colSet("ind_num", "0");

  }

  public void wfAjaxMcht() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    // String ls_winid =
    selectMcht(wp.itemStr("ax_mcht_no"));
    if (rc != 1) {
      return;
    }

    wp.addJSON("db_cnt", sqlStr("db_cnt"));
  }

  void selectMcht(String mchtNo) {
    String sql1 = " select count(*) as db_cnt from dbb_rsk_merchant where mcht_no = ? ";
    sqlSelect(sql1, new Object[] {mchtNo});
    return;
  }

  boolean checkMcht(String mchtNo) {
    String sql1 = " select count(*) as db_cnt from dbb_rsk_merchant where mcht_no = ? ";
    sqlSelect(sql1, new Object[] {mchtNo});

    if (sqlNum("db_cnt") > 0)
      return false;

    return true;
  }

}
