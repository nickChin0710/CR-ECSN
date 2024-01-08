/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-05-22  V1.00.01  Amber      program initial                            *                            
* 108-11-06  V1.00.02  Andy       Update UI && data_structure                *
* 109-04-21  V1.00.03  YangFang   updated for project coding standard        *
******************************************************************************/

package genr01;


import ofcapp.BaseReport;
import taroko.com.TarokoCommon;

public class Genr0150 extends BaseReport {
  String kkVouchDate = "";
  String kkCurrCode = "";
  String kkAcNo = "";
  String genPostLogName = "";

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
  }

  @Override
  public void initPage() {

  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";


    if (empty(wp.itemStr("ex_vouch_date")) == false) {
      wp.whereStr += " and  vouch_date = :ex_vouch_date ";
      setString("ex_vouch_date", wp.itemStr("ex_vouch_date"));
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and  curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
    }

    if (empty(wp.itemStr("ex_ac_no")) == false) {
      wp.whereStr += " and  a.ac_no like :ex_ac_no ";
      setString("ex_ac_no", wp.itemStr("ex_ac_no") + "%");
    }
    
    genPostLogName = "gen_post_log";

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
    if (getWhereStr() == false)
      return;

    wp.selectSQL = " a.vouch_date" + " , a.curr_code" + " , a.ac_no" 
    	+ " , decode(nvl(b.chi_short_name,''),'',decode(a.ac_no,'OP','溢付款',a.ac_no),b.chi_short_name) as chi_short_name "
        + " , a.pre_master_bal" + " , a.this_vouch_dr_amt" + " , a.this_vouch_cr_amt"
        + " , a.adj_vouch_dr_amt" + " , a.adj_vouch_cr_amt" + " , a.this_master_bal"
        + " , (a.this_master_bal - (a.pre_master_bal+ a.this_vouch_dr_amt - a.this_vouch_cr_amt+ a.adj_vouch_dr_amt -  a.adj_vouch_cr_amt)) as diff_bal";
    getWhereStr();
    wp.daoTable = genPostLogName + " as a left join ptr_actcode as b on decode(length(a.ac_no),2,a.ac_no,substr(a.ac_no,4,2)) = b.acct_code";
    wp.whereOrder = " order by a.vouch_date,a.curr_code,a.ac_no";

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
	  queryFunc();
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_curr_code");
      this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type like 'DC_CURRENCY' order by wf_id");

    } catch (Exception ex) {
    }
  }

}
