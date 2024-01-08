/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 108-12-04  V1.00.01  Amber	  Update     								 *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm01;



import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0410 extends BaseEdit {
  String mExHeadOffice = "";

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
    }

    initButton();
  }

  // for query use only
  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_head_office")) == false) {
      wp.whereStr += " and  head_office = :head_office ";
      setString("head_office", wp.itemStr("ex_head_office"));
    }
  }

  @Override
  public void queryFunc() throws Exception {
	sqlParm.clear();  
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " head_office" + ", head_office_name" + ", crt_date" + ", crt_user";

    wp.daoTable = "ptr_head_office";
    wp.whereOrder = " order by head_office";

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
    mExHeadOffice = wp.itemStr("head_office");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExHeadOffice = wp.itemStr("kk_head_office");
    if (empty(mExHeadOffice)) {
      mExHeadOffice = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", head_office " + ", head_office_name"
        + ", crt_date" + ", crt_user";
    wp.daoTable = "ptr_head_office";
    wp.whereStr = "where 1=1 and head_office = :head_office";
    setString("head_office", mExHeadOffice);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, head_office=" + mExHeadOffice);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0410Func func = new Ptrm0410Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
