/*客服D檔調整匯入作業 V.2019-1205 Alex
 * 2019-1205:  Alex  add initButton
 * 2018-0726:	JH		modify
 * 109-04-27 shiyuqi       updated for project coding standard     
 * 111-11-23   machao        執行功能bug調整*  
 * 112-01-04   Zuwei       查詢sql錯誤                                        *  
 * */
package cmsm02;

import ofcapp.BaseAction;

public class Cmsp4040 extends BaseAction {

  @Override
  public void userAction() throws Exception {
    wp.loginDeptNo = this.userDeptNo();

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_appr_no"), "appr_no")
        + " and uf_nvl(acct_post_flag,'N')<>'Y' " + " and uf_nvl(debit_flag,'N')<>'Y' ";

    listSum(lsWhere);
    lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_appr_no"), "appr_no")
    + " and uf_nvl(acct_post_flag,'N')<>'Y' " + " and uf_nvl(debit_flag,'N')<>'Y' ";
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(rowid) as rowid ," + " item_post_date ," + " reference_no ,"
        + " acct_type ," + " uf_acno_key(p_seqno) as acct_key ,"
        + " uf_nvl(curr_code,'901') as curr_code ," + " end_bal ," + " dc_end_bal ,"
        + " d_avail_bal ," + " dc_d_avail_bal ," + " adj_amt ," + " acct_code ," + " adj_remark ,"
        + " source_table ," + " uf_dc_amt(curr_code,end_bal,dc_end_bal) as wk_end_bal ,"
        + " uf_dc2tw_amt(beg_bal,dc_beg_bal,adj_amt) as wk_adj_amt_tw ,"
        + " uf_dc_amt2(d_avail_bal,dc_d_avail_bal) as wk_d_avail_bal";
    wp.daoTable = "cms_acaj ";

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
  }

  void listSum(String lsWhere) {
    String sql1 =
        // "select sum(round((case when curr_code in ('','901') then 1 else beg_bal/dc_beg_bal end *
        // adj_amt),0)) as tl_adj_amt "
        "select sum(round(uf_dc2tw_amt(beg_bal,dc_beg_bal,adj_amt))) as tl_adj_amt "
            + " from cms_acaj " + lsWhere;
    sqlSelect(sql1);
    wp.colSet("tl_adj_amt", sqlNum("tl_adj_amt"));
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
    int llOk = 0, llErr = 0;
    cmsm02.Cmsp4040Func func = new cmsm02.Cmsp4040Func();
    func.setConn(wp);

    String[] lsRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;
    this.optNumKeep(lsRowid.length, aaOpt);
    String optt = aaOpt[0];
    int a = aaOpt.length;
    if(optt.isEmpty() || a==0) {
    	alertErr2("資料錯誤: 請點選欲執行資料");
    	return;
    }

    for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = this.optToIndex(aaOpt[ii]);
      if (rr < 0)
        break;

      func.varsSet("rowid", lsRowid[rr]);
      int liRc = func.dataProc();
      sqlCommit(liRc);
      if (liRc == 1) {
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
        wp.colSet(rr, "opt_on", "");
      } else {
        wp.colSet(rr, "ok_flag", "X");
        llErr++;
      }

    }
    // if (ll_ok > 0) {
    // sql_commit(1);
    // }
    alertMsg("資料 [D檔調整匯入處理] 完成; OK=" + llOk + ", ERR=" + llErr);

  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
