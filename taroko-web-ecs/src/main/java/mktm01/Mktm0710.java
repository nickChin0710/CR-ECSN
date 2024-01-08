/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-11  V1.00.00  Andy       program initial                            *
* 107-07-20  V1.00.01  Andy       Update                                     *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/

package mktm01;

import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm0710 extends BaseEdit {
  String msg = "";

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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 存檔 */
      strAction = "S2";
      saveFunc();

    }

    dddwSelect();
    initButton();
  }

  private int getWhereStr() throws Exception {

    String mchtGroupId1 = wp.itemStr("mcht_group_id1");
    String mchtGroupId2 = wp.itemStr("mcht_group_id2");

    wp.whereStr = " where 1=1 ";
    wp.whereStr += sqlStrend(mchtGroupId1, mchtGroupId2, "mcht_group_id");

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    getWhereStr();
    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno," + "mcht_group_id, " + "mcht_group_desc, " + "crt_date, "
            + "crt_user, " + "apr_date, " + "apr_user, " + "mod_user, " + "mod_time, " + "mod_pgm ";
    wp.daoTable = " mkt_mcht_group ";
    wp.whereOrder = " order by mcht_group_id";
    // System.out.println("select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.setPageValue();
    listWkdata();
  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    int llErr = 0;
    String lsSql = "";
    Mktm0710Func func = new Mktm0710Func(wp);
    if (strAction.equals("S2")) {
      String[] aaRowid = wp.itemBuff("rowid");
      String[] aaMchtGroupId = wp.itemBuff("mcht_group_id");
      String[] aaMchtGroupDesc = wp.itemBuff("mcht_group_desc");
      String[] aaOpt = wp.itemBuff("opt");
      wp.listCount[0] = aaMchtGroupId.length;

      // -check duplication-
      for (int ll = 0; ll < aaMchtGroupId.length; ll++) {

        wp.colSet(ll, "ok_flag", "");

        if (checkBoxOptOn(ll, aaOpt)) {
          continue;
        }

        if (empty(aaMchtGroupId[ll])) {
          alertErr("特店群組代碼不能為空白!");
          wp.colSet(ll, "ok_flag", "!");
          return;
        }

        if (empty(aaMchtGroupDesc[ll])) {
          alertErr("特店群組說明不能為空白!");
          wp.colSet(ll, "ok_flag", "!");
          return;
        }

        if (ll != Arrays.asList(aaMchtGroupId).indexOf(aaMchtGroupId[ll])) {
          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        }
      }

      if (llErr > 0) {
        alertErr("資料值重複 : " + llErr);
        return;
      }

      llErr = 0;
      // -insert-
      for (int ll = 0; ll < aaMchtGroupId.length; ll++) {
        func.varsSet("aa_rowid", aaRowid[ll]);
        func.varsSet("aa_mcht_group_id", aaMchtGroupId[ll]);
        func.varsSet("aa_mcht_group_desc", aaMchtGroupDesc[ll]);
        func.varsSet("aa_apr_user", wp.itemStr("approval_user"));

        if (checkBoxOptOn(ll, aaOpt) && empty(aaRowid[ll])) {
          continue;
        }
        // delete
        if (checkBoxOptOn(ll, aaOpt) && !empty(aaRowid[ll])) {
          lsSql = "select count(*) ll_cnt " + "from	bil_merchant "
              + "where mcht_group_id =:ls_group_id ";
          setString("ls_group_id", aaMchtGroupId[ll]);
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            if (sqlNum("ll_cnt") > 0) {
              llErr++;
              wp.colSet(ll, "ok_flag", "!已被歸屬,不可刪除!!");
              alertErr("特店群組代碼已被歸屬, 不可刪除!!");
              break;
            }
          }
          if (func.dbDelete() != 1) {
            llErr++;
            msg = "delete mkt_mcht_group err";
            wp.colSet(ll, "ok_flag", "!");
            break;
          }
          continue;
        }
        // insert
        if (empty(aaRowid[ll])) {
          if (func.dbInsert() != 1) {
            llErr++;
            msg = "insert mkt_mcht_group err";
            wp.colSet(ll, "ok_flag", "!");
            break;
          }
          continue;
        }
        // update
        if (func.dbUpdate() != 1) {
          llErr++;
          msg = "update mkt_mcht_group err";
          wp.colSet(ll, "ok_flag", "!");
          break;
        }
      }
      if (llErr > 0) {
        sqlCommit(0);
        alertMsg("資料存檔失敗," + msg);
        return;
      }
      sqlCommit(1);
      queryFunc();
      alertMsg("資料存檔完成");
    }
  }

  @Override
  public void initButton() {

    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnModeAud("XX");

  }

  @Override
  public void dddwSelect() {

  }

  void listWkdata() {

  }

}
