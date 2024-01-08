package cmsr02;
/** 19-0614:   JH    p_xxx >>acno_p_xxx
 *109-04-28   shiyuqi       updated for project coding standard     * 
 *110-01-05  V1.00.03  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
 * */
import ofcapp.BaseAction;

public class Cmsr4020 extends BaseAction {
  String lsDate1 = "", lsDate2 = "";
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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

    if (this.chkStrend(wp.itemStr("ex_chg_date1"), wp.itemStr("ex_chg_date2")) == false) {
      alertErr2("改址日期起迄：輸入錯誤");
      return;
    }

    lsDate1 = wp.itemStr("ex_chg_date1");
    lsDate2 = wp.itemStr("ex_chg_date2");
    if (!empty(lsDate2)) {
      if (commDate.monthsBetween(lsDate2, lsDate1) > 6) {
        alertErr2("起迄相差最多半年");
        return;
      }
    }


    if (empty(lsDate2)) {
      lsDate2 = commDate.dateAdd(lsDate1, 0, 6, 0);
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    // wp.pageControl();
    wp.sqlCmd = " select " + " substr(mod_time,1,8) as mod_date ," + " acct_key ," + " acct_type ,"
        + " bill_sending_zip as wk_addr_zip ,"
        + " bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5 as wk_addr_ori , "
        + " update_date ," + " update_user ," + " acct_holder_id " + " from act_acno_hist "
        + " where 1=1 " + sqlCol(lsDate1, "mod_time", ">=") + sqlCol(lsDate2, "mod_time", "<=")
        //+ " and acct_type not in ('02','03') " + " and id_p_seqno in "
        + "and id_p_seqno in "
        + " ( select id_p_seqno from  "
        + " ( select distinct id_p_seqno , bill_sending_zip as wk_addr_zip , "
        + " bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5 as wk_addr_ori "
        + " from act_acno_hist where 1=1 " + sqlCol(lsDate1, "mod_time", ">=")
        + sqlCol(lsDate2, "mod_time", "<=");
    if (!wp.itemEmpty("ex_idno")) {
      wp.sqlCmd += sqlCol(wp.itemStr("ex_idno"), "acct_key", "like%");
    } else if (!wp.itemEmpty("ex_card_no")) {
      wp.sqlCmd += " and acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 "
          + sqlCol(wp.itemStr2("ex_card_no"), "card_no");
    }
    wp.sqlCmd += " ) group by id_p_seqno having count(*) >=" + wp.itemNum("ex_chg_number")
        + ") order by acct_type , acct_key , 1 Desc ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    // wp.setPageValue();


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
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub
  }

  @Override
  public void initPage() {
    wp.colSet("ex_chg_number", "3");

  }

}
