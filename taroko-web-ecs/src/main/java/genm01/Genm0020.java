/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-21  V1.00.00  Andy       program initial                            *
* 106-11-16            Andy       update                                     *
* 107-01-05  V1.00.02  ryan       Update  							         *
* 109-04-21  V1.00.03  YangFang   updated for project coding standard        * 
******************************************************************************/

package genm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon; 

public class Genm0020 extends BaseEdit {
  String mExAcNo = ""; // 變數--摘要代碼
  String controlTabName = "";
  String kkAprFlag = "";

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
      strAction = "S2";
      saveFunc();
    }


    dddwSelect();
    initButton();
  }

  private void getWhereStr() throws Exception {
    // 判斷摘要代碼是否為空值
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_ac_no")) == false) {
      wp.whereStr += " and  ac_no like :ac_no ";
      setString("ac_no", wp.itemStr("ex_ac_no") + "%");
    }
    if (wp.itemStr("ex_apr_flag").equals("Y")) {
      controlTabName = "gen_acct_m";
    } else {
      controlTabName = "gen_acct_m_t";
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    // select columns
    wp.selectSQL = " ac_no" + " , ac_full_name" + " , ac_brief_name" + " , memo3_flag"
        + " , memo3_kind" + " , dr_flag" + " , cr_flag" + " , brn_rpt_flag"
        + " , uf_2ymd(mod_time) as mod_date" + " , mod_user ";
    getWhereStr();
    wp.daoTable = controlTabName;
    // order column
    wp.whereOrder = " order by ac_no";
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

    mExAcNo = itemKk("data_k1");
    if (empty(mExAcNo)) {
      mExAcNo = wp.itemStr("ac_no");
    }
    String lsSql = "select count(*) as tot_cnt from gen_acct_m_t where ac_no = ?";
    Object[] param = new Object[] {mExAcNo};
    sqlSelect(lsSql, param);
    if (sqlNum("tot_cnt") > 0) {
      controlTabName = "gen_acct_m_t";
    } else {
      controlTabName = "gen_acct_m";
    }

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + " , ac_no " + " , ac_full_name"
        + " , ac_brief_name" + " , memo3_flag" + " , memo3_kind" + " , dr_flag" + " , cr_flag"
        + " , brn_rpt_flag" + " , uf_2ymd(mod_time) as mod_date" + " , mod_user ";
    wp.daoTable = controlTabName;
    // wp.daoTable = "gen_acct_m";
    // ***20171116 update gen_acct_m ==> gen_acct_m_t
    if (controlTabName.equals("gen_acct_m_t")) {
      wp.selectSQL += ", 'U.更新待覆核' apr_flag";
    } else if (controlTabName.equals("gen_acct_m")) {
      wp.selectSQL += ", 'Y.未異動' apr_flag";
    }
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  ac_no = :ac_no ";
    setString("ac_no", mExAcNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, ac_no=" + mExAcNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check -

    if (wp.itemStr("memo3_flag").equals("Y")) {

      if (wp.itemStr("dr_flag").equals("Y") && wp.itemStr("cr_flag").equals("Y")) {
        alertErr2("借貸方待冲傳票任一須為Y");
        return;
      }

      if (!wp.itemStr("dr_flag").equals("Y") && !wp.itemStr("cr_flag").equals("Y")) {
        alertErr2("借貸方待冲傳票任一須為Y");
        return;
      }

      if (empty(wp.itemStr("memo3_kind"))) {
        alertErr2("須勾選銷帳健值種類");
        return;
      }

    }


    String ls_action = "";
    Genm0020Func func = new Genm0020Func(wp);
    if (strAction.equals("S2")) {
      ls_action = "S2";
      if (wp.itemStr("apr_flag").equals("Y.未異動")) {
        strAction = "A";
      } else if (wp.itemStr("apr_flag").equals("U.更新待覆核")) {
        strAction = "U";
      }
    }

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (ls_action.equals("S2")) {
      querySelect();
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
      // this.dddw_list("dddw_ac_no", "gen_digest", "ac_no", "", "where 1=1 group by ac_no order by
      // ac_no");
      // this.dddw_list("dddw_group_abbr_code", "ptr_group_code", "group_abbr_code", "", "where 1=1
      // group by group_abbr_code order by group_abbr_code");
      // 提供Detel頁下拉指標到...
      // wp.optionKey = wp.col_ss("memo3_kind");
      // this.dddw_list("dddw_bill_form", "cyc_bill_form", "bill_form", "bill_form_name", "where 1=1
      // order by bill_form");
    } catch (Exception ex) {
    }
  }

}
