/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-27  V1.00.00  yash       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 110-03-16            Kirin      代號'13','新貴通貴賓室'改為11 ,'新貴通貴賓室'
******************************************************************************/

package mktm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm4220 extends BaseEdit {
  String mExItemNo = "";

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
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_item_no")) == false) {
      wp.whereStr += " and  item_no = :item_no ";
      setString("item_no", wp.itemStr("ex_item_no"));
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

    wp.selectSQL = " item_no"
        + ", decode(item_no,'08','機場接送','09','機場周邊停車','10','龍騰卡貴賓室','11','新貴通貴賓室',item_no) as hard_code"
        + ", apr_user" + ", apr_date";

    wp.daoTable = "mkt_contri_cnt2amt_parm";
    wp.whereOrder = " order by item_no";
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
    mExItemNo = wp.itemStr("item_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExItemNo = wp.itemStr("kk_item_no");
    if (empty(mExItemNo)) {
      mExItemNo = itemKk("data_k1");
    }
    if (empty(mExItemNo)) {
      mExItemNo = wp.itemStr("item_no");
    }


    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", item_no " + ", use_cnt_n1"
        + ", use_cnt_n2" + ", use_cnt_n3" + ", use_cnt_n4" + ", use_cnt_n5" + ", use_cnt_n6"
        + ", tot_amt_n1" + ", tot_amt_n2" + ", tot_amt_n3" + ", tot_amt_n4" + ", tot_amt_n5"
        + ", tot_amt_n6" + ", use_cnt_o1" + ", use_cnt_o2" + ", use_cnt_o3" + ", use_cnt_o4"
        + ", use_cnt_o5" + ", use_cnt_o6" + ", tot_amt_o1" + ", tot_amt_o2" + ", tot_amt_o3"
        + ", tot_amt_o4" + ", tot_amt_o5" + ", tot_amt_o6" + ", apr_user" + ", apr_date"
        + ", crt_date" + ", crt_user";
    wp.daoTable = "mkt_contri_cnt2amt_parm";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  item_no = :item_no ";
    setString("item_no", mExItemNo);

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, item_no=" + mExItemNo);
    }


    // use_cnt_n1_1
    if (wp.colNum("use_cnt_n2", 0) == 0) {
      wp.colSet("use_cnt_n1_1", "99999");
    } else {
      wp.colSet("use_cnt_n1_1", numToStr(wp.colNum("use_cnt_n2", 0) - 1, ""));
    }


    // Use_cnt_n2_1
    if (wp.colNum("use_cnt_n2", 0) > 0) {
      if (wp.colNum("use_cnt_n3", 0) == 0) {
        wp.colSet("use_cnt_n2_1", "99999");
      } else {
        wp.colSet("use_cnt_n2_1", numToStr(wp.colNum("use_cnt_n3", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_n2_1", "0");
    }

    // Use_cnt_n3_1
    if (wp.colNum("use_cnt_n3", 0) > 0) {
      if (wp.colNum("use_cnt_n4", 0) == 0) {
        wp.colSet("use_cnt_n3_1", "99999");
      } else {
        wp.colSet("use_cnt_n3_1", numToStr(wp.colNum("use_cnt_n4", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_n3_1", "0");
    }

    // Use_cnt_n4_1
    if (wp.colNum("use_cnt_n4", 0) > 0) {
      if (wp.colNum("use_cnt_n5", 0) == 0) {
        wp.colSet("use_cnt_n4_1", "99999");
      } else {
        wp.colSet("use_cnt_n4_1", numToStr(wp.colNum("use_cnt_n5", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_n4_1", "0");
    }


    // Use_cnt_n5_1
    if (wp.colNum("use_cnt_n5", 0) > 0) {
      if (wp.colNum("use_cnt_n6", 0) == 0) {
        wp.colSet("use_cnt_n5_1", "99999");
      } else {
        wp.colSet("use_cnt_n5_1", numToStr(wp.colNum("use_cnt_n6", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_n5_1", "0");
    }

    // Use_cnt_n6_1
    if (wp.colNum("use_cnt_n6", 0) > 0) {
      wp.colSet("use_cnt_n6_1", "99999");
    } else {
      wp.colSet("use_cnt_n6_1", "0");
    }

    // use_cnt_o1_1
    if (wp.colNum("use_cnt_o2", 0) == 0) {
      wp.colSet("use_cnt_o1_1", "99999");
    } else {
      wp.colSet("use_cnt_o1_1", numToStr(wp.colNum("use_cnt_o2", 0) - 1, ""));
    }


    // Use_cnt_o2_1
    if (wp.colNum("use_cnt_o2", 0) > 0) {
      if (wp.colNum("use_cnt_o3", 0) == 0) {
        wp.colSet("use_cnt_o2_1", "99999");
      } else {
        wp.colSet("use_cnt_o2_1", numToStr(wp.colNum("use_cnt_o3", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_o2_1", "0");
    }

    // Use_cnt_n3_1
    if (wp.colNum("use_cnt_o3", 0) > 0) {
      if (wp.colNum("use_cnt_o4", 0) == 0) {
        wp.colSet("use_cnt_o3_1", "99999");
      } else {
        wp.colSet("use_cnt_o3_1", numToStr(wp.colNum("use_cnt_o4", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_o3_1", "0");
    }

    // Use_cnt_n4_1
    if (wp.colNum("use_cnt_o4", 0) > 0) {
      if (wp.colNum("use_cnt_o5", 0) == 0) {
        wp.colSet("use_cnt_o4_1", "99999");
      } else {
        wp.colSet("use_cnt_o4_1", numToStr(wp.colNum("use_cnt_o5", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_o4_1", "0");
    }


    // Use_cnt_n5_1
    if (wp.colNum("use_cnt_o5", 0) > 0) {
      if (wp.colNum("use_cnt_o6", 0) == 0) {
        wp.colSet("use_cnt_o5_1", "99999");
      } else {
        wp.colSet("use_cnt_o5_1", numToStr(wp.colNum("use_cnt_o6", 0) - 1, ""));
      }
    } else {
      wp.colSet("use_cnt_o5_1", "0");
    }

    // Use_cnt_n6_1
    if (wp.colNum("use_cnt_o6", 0) > 0) {
      wp.colSet("use_cnt_o6_1", "99999");
    } else {
      wp.colSet("use_cnt_o6_1", "0");
    }



  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Mktm4220Func func = new Mktm4220Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    if (strAction.equals("U")) {
      dataRead();
    }

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

      // wp.optionKey = wp.item_ss("ex_item_no");
      // this.dddw_list("dddw_item_no", "mkt_contri_cnt2amt_parm", "item_no", "", "where 1=1 group
      // by item_no order by item_no");
    } catch (Exception ex) {
    }
  }

}
