package colm05;
/** 帳戶強停統計查詢
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 109-05-06  V1.00.01  Tanwei       updated for project coding standard
 * */
import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Colq5710 extends BaseQuery {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
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
    if (empty(wp.itemStr("ex_stop_date1"))) {
      alertErr("強停日期起不可空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_stop_date1"), wp.itemStr("ex_stop_date2")) == false) {
      alertErr2("強停日期起迄：輸入錯誤");
      return;
    }

    wp.whereStr = " where 1=1 ";

    // sum_Read();
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " acct_type ,    " + " count(*) as db_count,"
        + " nvl(sum(acct_jrnl_bal),0) as db_acct_jrnl_bal ";
    wp.daoTable = "act_acct";
    wp.whereStr = "where p_seqno in ( select acno_p_seqno from rsk_acnolog"
        + " where log_date between ? and ?"
        + " and log_type='2' and kind_flag ='A' and log_not_reason ='' )";
    wp.whereOrder = " group by acct_type" + " order by acct_type";

    setString2(1, wp.itemStr2("ex_stop_date1"));
    setString2(2, wp.itemNvl("ex_stop_date2", "99991231"));

    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
  }

  void listWkdata() {

    double lmTotCnt = 0, lmTotAmt = 0;
    double lmCnt = 0, lmBal = 0;

    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";

    colm05.Colq5710Func func = new colm05.Colq5710Func();
    func.setConn(wp);

    // func.vars_set("ex_stop_date1", wp.item_ss("ex_stop_date1"));
    // func.vars_set("ex_stop_date2", wp.item_ss("ex_stop_date2"));
    for (int ll = 0; ll < wp.selectCnt; ll++) {

      sqlSelect(sql1, new Object[] {wp.colStr(ll, "acct_type")});
      if (sqlRowNum > 0) {
        wp.colSet(ll, "tt_acct_type", sqlStr("chin_name"));
      }


      func.varsSet("ex_acct_type", wp.colStr(ll, "acct_type"));
      lmCnt = wp.colNum(ll, "db_count");
      lmBal = wp.colNum(ll, "db_acct_jrnl_bal");
      if (func.dataSelect() == 1) {
        lmCnt = lmCnt - func.varsNum("db_cnt");
        lmBal = lmBal - func.varsNum("db_bal");
      }
      wp.colSet(ll, "db_count", lmCnt);
      wp.colSet(ll, "db_acct_jrnl_bal", lmBal);
      lmTotCnt += lmCnt;
      lmTotAmt += lmBal;
    }

    wp.colSet("sum_tot_cnt", lmTotCnt);
    wp.colSet("sum_tot_amt", lmTotAmt);
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

}
