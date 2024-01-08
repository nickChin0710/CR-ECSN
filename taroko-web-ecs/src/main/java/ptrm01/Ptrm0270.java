/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       add initButton , Online Approve            *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0270 extends BaseEdit {
  //String kk1 = "";
  Ptrm0270Func func;

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

    dddwSelect();
    initButton();
  }


  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_yymm"), "holiday", "like%");
    wp.whereOrder = " order by holiday ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();



  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno," + "holiday, "
        + "to_char(mod_time,'yyyymmdd') as mod_date, " + "mod_user";
    wp.daoTable = "ptr_holiday";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void insertFunc() throws Exception {
    func = new ptrm01.Ptrm0270Func(wp);

    if (checkApproveZz() == false)
      return;
    func.varsSet("holiday", wp.itemStr("holiday"));

    rc = func.dbInsert();
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    sqlCommit(rc);
    if (rc != 1)
      return;

    clearFunc();
  }

  @Override
  public void saveFunc() throws Exception {
    func = new ptrm01.Ptrm0270Func(wp);
    int llOk = 0, llErr = 0;
    // String ls_opt="";

    String[] aaCode = wp.itemBuff("holiday");
    String[] aaModseq = wp.itemBuff("mod_seqno");
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaOld = wp.itemBuff("old_data");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("holiday");
    if (checkApproveZz() == false)
      return;
    // -insert-
    for (int ll = 0; ll < wp.itemRows("holiday"); ll++) {
      if (empty(aaCode[ll])) {
        continue;
      }

      String lsNew = aaCode[ll];
      func.varsSet("rowid", aaRowid[ll]);
      func.varsSet("holiday", aaCode[ll]);
      func.varsSet("mod_seqno", aaModseq[ll]);
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
        // -call dbdelete-
        if (func.dbDelete() == 1) {
          llOk++;
          wp.colSet(ll, "ok_flag", "V");
        } else {
          llErr++;
          wp.colSet(ll, "ok_flag", "X");
        }
      } else {
        // -edit??-
        if (eqAny(lsNew, aaOld[ll])) {
          continue;
        }
        log("old=" + aaOld[ll] + "; new=" + lsNew);
        if (func.dbUpdate() == 1) {
          llOk++;
          wp.colSet(ll, "ok_flag", "V");
        } else {
          llErr++;
          wp.colSet(ll, "ok_flag", "X");
        }
      }
      // --
    }
    if (llOk > 0) {
      sqlCommit(1);
    }
    alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr);
    dataRead();
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
    } else
      btnModeAud("xx");

  }

}
