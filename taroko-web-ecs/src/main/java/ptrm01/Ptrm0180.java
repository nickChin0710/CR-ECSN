/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       add initButton , add Online Approve        *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import java.util.Arrays;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0180 extends BaseEdit {
 // String kk1 = "";
  Ptrm0180Func func;

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
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_zip_code"), "zip_code", "like%");
    wp.whereOrder = " order by zip_code ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();



  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno," + "zip_code, " + "zip_city, " + "zip_town";
    wp.daoTable = "ptr_zipcode";
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
  public void dataRead() throws Exception {}

  @Override
  public void insertFunc() throws Exception {
    func = new ptrm01.Ptrm0180Func(wp);
    if (checkApproveZz() == false)
      return;
    func.varsSet("zip_code", wp.itemStr("zip_code"));
    func.varsSet("zip_city", wp.itemStr("zip_city"));
    func.varsSet("zip_town", wp.itemStr("zip_town"));
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
    func = new ptrm01.Ptrm0180Func(wp);
    int llOk = 0, llErr = 0;
    // String lsOpt="";

    String[] aaCode = wp.itemBuff("zip_code");
    String[] aaCity = wp.itemBuff("zip_city");
    String[] aaTown = wp.itemBuff("zip_town");
    String[] aaModseq = wp.itemBuff("mod_seqno");
    String[] aaOld = wp.itemBuff("old_data");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("zip_code");
    if (checkApproveZz() == false)
      return;
    wp.colSet("IND_NUM", "" + aaCode.length);

    // -insert-
    for (int ll = 0; ll < aaCode.length; ll++) {
      if (empty(aaCode[ll])) {
        continue;
      }

      int liRc = 1;
      String lsNew = aaCode[ll] + "," + aaCity[ll] + "," + aaTown[ll];
      func.varsSet("zip_code", aaCode[ll]);
      func.varsSet("zip_city", aaCity[ll]);
      func.varsSet("zip_town", aaTown[ll]);
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
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    } else {
      this.btnModeAud("XX");
    }

  }

}
