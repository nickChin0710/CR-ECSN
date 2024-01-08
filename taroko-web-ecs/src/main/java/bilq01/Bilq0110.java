/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-26  V1.00.00  yash           program initial                        *
* 106-12-14            Andy		  update : program name : Bili0110==>Bilq0110*
* 109-04-23  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package bilq01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilq0110 extends BaseEdit {
  String mExReferenceNo = "";

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

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no"))
        && empty(wp.itemStr("ex_mcht_no")) && empty(wp.itemStr("exDateS"))
        && empty(wp.itemStr("exDateE"))) {
      alertErr("至少輸入一個查詢條件");
      return false;
    }
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_idno")) == false) {
      wp.whereStr += " and  i.id_no = :ex_idno ";
      setString("ex_idno", wp.itemStr("ex_idno"));
    }

    if (empty(wp.itemStr("ex_card_no")) == false) {
      wp.whereStr += " and  c.card_no = :ex_card_no ";
      setString("ex_card_no", wp.itemStr("ex_card_no"));
    }

    if (empty(wp.itemStr("ex_mcht_no")) == false) {
      wp.whereStr += " and  c.mcht_no = :ex_mcht_no ";
      setString("ex_mcht_no", wp.itemStr("ex_mcht_no"));
    }

    if (empty(wp.itemStr("exDateS")) == false) {
      wp.whereStr += " and  c.purchase_date >= :exDateS ";
      setString("exDateS", wp.itemStr("exDateS"));
    }

    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and  c.purchase_date <= :exDateE ";
      setString("exDateE", wp.itemStr("exDateE"));
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

    wp.selectSQL =
        " c.purchase_date" + ", c.new_it_flag" + " , i.chi_name as name" + " , i.id_no as idseqno"
            + ", c.card_no" + ", c.mcht_no" + ", c.product_name" + ", c.install_tot_term"
            + ", c.tot_amt" + ", c.clt_fees_amt" + ", c.trans_rate" + ", c.year_fees_rate";

    wp.daoTable = " bil_contract c left join crd_idno i on c.id_p_seqno=i.id_p_seqno";

    wp.whereOrder = " order by c.purchase_date";
    if (getWhereStr() != true)
      return;

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
    mExReferenceNo = wp.itemStr("reference_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExReferenceNo = wp.itemStr("kk_reference_no");
    if (empty(mExReferenceNo)) {
      mExReferenceNo = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ",xxx ";
    wp.daoTable = "bil_xx";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  reference_no = :reference_no ";
    setString("reference_no", mExReferenceNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, reference_no=" + mExReferenceNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // Bili0020_func func = new Bili0020_func(wp);
    //
    // rc = func.dbSave(is_action);
    // ddd(func.getMsg());
    // if (rc != 1) {
    // err_alert(func.getMsg());
    // }
    // this.sql_commit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      // wp.initOption="--";
      // wp.optionKey = wp.item_ss("ex_curr_code");
      // this.dddw_list("dddw_curr_code", "ptr_currcode", "curr_code", "curr_chi_name", "where 1=1
      // order by curr_code");
    } catch (Exception ex) {
    }
  }

}
