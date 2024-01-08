/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-19  V1.00.00  yash       program initial                            *
* 108-12-02  V1.00.01  Amber	  Update init_button  Authority 			 *
* 109-04-24  V1.00.02  shiyuqi       updated for project coding standard     *   
******************************************************************************/

package bilm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0610 extends BaseEdit {
  String mExIcaNo = "";

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

    // dddw_select();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_ica_no")) == false) {
      wp.whereStr += " and  ica_no like :ica_no ";
      setString("ica_no", wp.itemStr("ex_ica_no") + "%");
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " ica_no" + ", ica_desc" + ", bank_no" + ", hex(rowid) as rowid, mod_seqno";

    wp.daoTable = "bil_auto_ica";
    wp.whereOrder = " order by ica_no";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExIcaNo = wp.itemStr("ica_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {


    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + ", ica_no " + ", xxx" + ", crt_date" + ", crt_user";
    wp.daoTable = "bil_auto_ica";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  ica_no = :ica_no ";
    setString("ica_no", mExIcaNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, ica_no=" + mExIcaNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {


    Bilm0610Func func = new Bilm0610Func(wp);
    int llOk = 0, llErr = 0;
    if (strAction.equals("S2")) {

      String[] aaIcaNo = wp.itemBuff("ica_no");
      String[] aaIcaDesc = wp.itemBuff("ica_desc");
      String[] aaBankNo = wp.itemBuff("bank_no");
      String[] aaRowid = wp.itemBuff("rowid");
      String[] aaModSeqno = wp.itemBuff("mod_seqno");
      String[] aaOpt = wp.itemBuff("opt");

      String[] hIcaNo = wp.itemBuff("h_ica_no");
      String[] hIcaDesc = wp.itemBuff("h_ica_desc");
      String[] hBankNo = wp.itemBuff("h_bank_no");


      wp.listCount[0] = aaIcaNo.length;

      // -check approve-
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }

      for (int ll = 0; ll < aaIcaNo.length; ll++) {

        // check
        if (empty(aaIcaNo[ll]) || empty(aaIcaDesc[ll]) || empty(aaBankNo[ll])) {
          wp.colSet(ll, "ok_flag", "X");
          wp.colSet(ll, "ls_errmsg", "不可空白 !");
          llErr++;
          continue;
        }

        func.varsSet("aa_ica_no", aaIcaNo[ll]);
        func.varsSet("aa_ica_desc", aaIcaDesc[ll]);
        func.varsSet("aa_bank_no", aaBankNo[ll]);
        func.varsSet("aa_rowid", aaRowid[ll]);
        func.varsSet("aa_mod_seqno", aaModSeqno[ll]);

        // delete
        if (checkBoxOptOn(ll, aaOpt)) {
          if (func.dbDelete() == 1) {
            wp.colSet(ll, "ok_flag", "V");
            llOk++;

          } else {
            wp.colSet(ll, "ok_flag", "X");
            wp.colSet(ll, "ls_errmsg", "Delete err !");
            llErr++;


          }
          continue;
        }

        // up
        if (!hIcaNo[ll].equals(aaIcaNo[ll]) || !hIcaDesc[ll].equals(aaIcaDesc[ll])
            || !hBankNo[ll].equals(aaBankNo[ll])) {
          if (func.dbUpdate() == 1) {
            wp.colSet(ll, "ok_flag", "V");
            llOk++;
          } else {
            wp.colSet(ll, "ok_flag", "X");
            wp.colSet(ll, "ls_errmsg", "Update err !");
            llErr++;
          }
        }
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
      alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);

    } else {

      // -check approve-
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }

      rc = func.dbSave(strAction);
      log(func.getMsg());
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
      this.sqlCommit(rc);
    }


  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnUpdateOn(wp.autUpdate());
    btnAddOn(wp.autUpdate());
  }



}
