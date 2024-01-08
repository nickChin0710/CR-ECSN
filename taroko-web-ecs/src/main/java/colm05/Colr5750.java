package colm05;
/** 符合整批強停條件而不強停明細表
 * 19-1129:   Alex  強停日期起 不可空白
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 2018-0518:	JH		queryfunc() modify
 * 109-05-06  V1.00.03  Tanwei       updated for project coding standard
 * 
 * */
import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;

public class Colr5750 extends BaseQuery {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
    // list_wkdata();
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("5750") > 0) {
        wp.optionKey = wp.colStr("ex_nostop_reason");
        dddwList("dddw_nostop_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='RSK-NOSTOP-WHY'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_stop_date1")) && empty(wp.itemStr("ex_stop_date2"))
        && empty(wp.itemStr("ex_acct_key")) && empty(wp.itemStr("ex_nostop_reason"))) {
      alertErr("條件不可全部空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_stop_date1"), wp.itemStr("ex_stop_date2")) == false) {
      alertErr2("強停日期起迄：輸入錯誤");
      return;
    }
    if (wp.itemEmpty("ex_stop_date1")) {
      alertErr2("強停日期起 不可空白");
      return;
    }

    wp.whereStr = " where 1=1 and A.kind_flag ='A' and A.log_type ='2' "
        + sqlCol(wp.itemStr("ex_stop_date1"), "A.log_date", ">=")
        + sqlCol(wp.itemStr("ex_stop_date2"), "A.log_date", "<=")
        + sqlCol(wp.itemStr("ex_acct_key"), "B.acct_key", "like%");

    if (empty(wp.itemStr("ex_nostop_reason"))) {
      wp.whereStr += " and A.log_not_reason<>'' ";
    } else {
      wp.whereStr += " and A.log_not_reason='" + wp.itemStr("ex_nostop_reason") + "'";
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " A.acno_p_seqno ,    " + " A.log_date , " + " B.acct_key ,  "
        + " A.acct_type ,  " + " A.log_not_reason ,  " + " B.id_p_seqno ,  "
        + " B.payment_rate1 ,  " + " B.last_pay_date," + " B.last_pay_amt," + " B.stmt_cycle,"
        + " B.int_rate_mcode as db_mcode2" + ", '' as xxx";
    wp.daoTable =
        " rsk_acnolog A left join act_acno B on B.p_seqno =A.acno_p_seqno and B.acno_flag<>'Y'";
    wp.whereOrder = " order by A.log_date, A.acct_type ";

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setPageValue();
  }

  void queryAfter() throws Exception {
    String sql1 = " select " + " A.acct_jrnl_bal " + ", B.block_status "
    // +", C.id_no as acct_holder_id"
    // +", C.chi_name"
        + ", uf_idno_id(B.id_p_seqno) as acct_holder_id"
        + ", uf_idno_name(B.id_p_seqno) as chi_name"
        + " from act_acct A join cca_card_acct B on A.p_seqno =B.acno_p_seqno and B.debit_flag<>'Y'"
        // +" join crd_idno C on A.id_p_seqno =C.id_p_seqno"
        + " where A.p_seqno = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.logSql = false;
      // balRead(wp.col_ss(ii,"p_seqno"),ii);
      String lsPSeqno = wp.colStr(ii, "acno_p_seqno");
      sqlSelect(sql1, new Object[] {lsPSeqno});
      if (sqlRowNum <= 0)
        continue;
      wp.colSet(ii, "acct_jrnl_bal", "" + sqlNum("acct_jrnl_bal"));
      wp.colSet(ii, "block_status", sqlStr("block_status"));
      wp.colSet(ii, "acct_holder_id", sqlStr("acct_holder_id"));
      wp.colSet(ii, "chi_name", sqlStr("chi_name"));

      if (ii > wp.pageRows)
        break;
    }
  }

  // void list_wkdata() {
  //
  // double lm_tot_cnt = 0, lm_tot_amt = 0;
  // int li_mcode = 0;
  // busi.func.EcsComm func = new busi.func.EcsComm();
  // func.setConn(wp);
  //
  // for (int ll = 0; ll < wp.selectCnt; ll++) {
  // li_mcode = func.getMcode(wp.col_ss(ll, "p_seqno"));
  // wp.col_set(ll, "db_mcode2", "" + li_mcode);
  // }
  // }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

}
