/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-11-02  V1.00.01  Tanwei     MKT_CONTRI_CARD分成2个(mater/detail)调整处理逻辑  *
* 109-11-03  V1.00.02  Tanwei     选单数据原hardcode,改读取DB数据                                                   *
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                    *
* 110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改                      *  *
* 110-03-08  V1.00.05  tanwei       修改金額參數不對bug                           *   
******************************************************************************/
package cmsr03;
/** 卡友貢獻度及卡友權益查詢
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 20-0107:   Ru    modify AJAX
 * */
import java.text.ParseException;
import ofcapp.BaseAction;

public class Cmsq4210 extends BaseAction {
  String lsYm1 = "", lsYm2 = "", adYm = "", lsVipCode = "", lsChiName = "";
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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R";
      dataRead2();
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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }
    // 20200107 modify AJAX
    else if (eqIgno(wp.buttonCode, "AJAX")) {
      if ("1".equals(wp.getValue("ID_CODE"))) {
        wfAjaxIdno();
      } else if ("2".equals(wp.getValue("ID_CODE"))) {
        wfAjaxCard();
      }
    }

  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_acct_code");
      //dddwList("dddw_acct_code", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type = 'RIGHT_ITEM_NO' and wf_id  in ('08','09','10','11')");
      dropdownList("dddw_acct_code", "ptr_sys_idtab", "wf_id", "wf_id" + "||'.'||" + "wf_desc", "where wf_type = 'RIGHT_ITEM_NO' and wf_id  in ('08','09','10','11')");
     
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no"))) {
      alertErr2("身分證 和 卡號 不可同時空白");
      return;
    }

    if (checkCard() == false) {
      alertErr2("卡號 or 身分證ID 無有效卡");
      return;
    }

    queryBefore();

    wp.colSet("wk_net_tot_amt", "");
    wp.colSet("card_tot_amt", "");
    wp.colSet("item_tot_amt", "");

    String lsWhere = " where 1=1 ";
    if (!empty(wp.itemStr("ex_idno"))) {
      lsWhere += " and A.major_id_p_seqno =" + wp.sqlID + "uf_idno_pseqno('"
          + wp.itemStr("ex_idno") + "')";
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "A.card_no");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }


  boolean checkCard() {
    String sql1 = " select count(*) as db_cnt";
    if (!empty(wp.itemStr("ex_idno")))
      sql1 += " from vcard_idno_m where 1=1" + sqlCol(wp.itemStr2("ex_idno"), "id_no");
    else if (!empty(wp.itemStr("ex_card_no")))
      sql1 += " from crd_card where 1=1" + sqlCol(wp.itemStr("ex_card_no"), "card_no");

    sqlSelect(sql1);
    if (sqlNum("db_cnt") <= 0)
      return false;
    return true;
  }

  void queryBefore() {

    if (wp.itemEmpty("ex_idno") == false) {
      selectData1(wp.itemStr2("ex_idno"));
      if (rc != 1)
        return;
      wp.colSet("chi_name", lsChiName);
      wp.colSet("vip_code", lsVipCode);
      if (sqlStr("income_score").length() > 0)
        wp.colSet("wk_score_01", sqlStr("income_score").substring(0, 1));
      else
        wp.colSet("wk_score_01", "");
      wp.colSet("score_yymm", commString.strToYmd(sqlStr("score_yymm")));
      wp.colSet("yy_income_amt", sqlStr("yy_income_amt"));
      wp.colSet("yy_cost_amt", sqlStr("yy_cost_amt"));
      wp.colSet("wk_net_amt", Math.round(sqlNum("yy_income_amt") - sqlNum("yy_cost_amt")));
    } else if (wp.itemEmpty("ex_card_no") == false) {
      selectData2(wp.itemStr2("ex_card_no"));
      if (rc != 1)
        return;
      wp.colSet("chi_name", lsChiName);
      wp.colSet("vip_code", lsVipCode);
      if (sqlStr("income_score").length() > 0)
        wp.colSet("wk_score_01", sqlStr("income_score").substring(0, 1));
      else
        wp.colSet("wk_score_01", "");
      wp.colSet("score_yymm", commString.strToYmd(sqlStr("score_yymm")));
      wp.colSet("yy_income_amt", sqlStr("yy_income_amt"));
      wp.colSet("yy_cost_amt", sqlStr("yy_cost_amt"));
      wp.colSet("wk_net_amt", Math.round(sqlNum("yy_income_amt") - sqlNum("yy_cost_amt")));
    }

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " A.major_card_no ,"
        + " decode(A.group_code,'0000',uf_tt_card_type(A.card_type),uf_tt_group_code(A.group_code)) as tt_group_name ,"
        + " A.sup_flag ," + " A.card_no ," + " 0 as db_purch_amt ," + " 0 as db_income_amt ,"
        + " 0 as db_cost_amt ," + " 0 as db_bill_cost ," + " 0 as db_air_shutt_cnt ,"
        + " 0 as db_air_park_cnt ," + " 0 as db_favor_room_cnt ," + " 0 as db_moore_room_cnt , "
        + " 1 as ctrl_click , " + " 'checked' as opt_on ";
    wp.daoTable = " crd_card A  ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by 1,2,3 ";
    logSql();
    pageQuery();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setPageValue();
    dataRead2();
    tabClick();
  }

  void queryAfter() throws ParseException {
    int ilTlCnt = 0, ilTlPurchAmt = 0, ilTlIncomeAmt = 0, ilTlCostAmt = 0,
        ilTlContriAmt = 0, ilTlBillCost = 0, ilTlAirShuttCnt = 0, ilTlAirParkCnt = 0,
        ilTlFavorRoomCnt = 0, ilTlMooreRoomCnt = 0, ilTlAmt2 = 0;
    busi.func.EcsComm ecs = new busi.func.EcsComm();
    ecs.setConn(wp);
    lsYm1 = commDate.dateAdd(ecs.getBusiDate(), -1, 0, 0);
    lsYm1 = lsYm1.substring(0, 4) + "01";
    lsYm2 = lsYm1.substring(0, 4) + "12";
    adYm = lsYm1.substring(0, 4) + "00";

    ilTlCnt = wp.selectCnt;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String sql1 = " select sum(nvl(consume_bl_amt,0)+nvl(consume_ca_amt,0)+nvl(consume_it_amt,0)"
          + "+nvl(consume_ao_amt,0)+nvl(consume_id_amt,0)+nvl(consume_ot_amt,0)) - "
          + "sum(nvl(sub_bl_amt,0)+nvl(sub_ca_amt,0)+nvl(sub_it_amt,0)+nvl(sub_ao_amt,0)+nvl(sub_id_amt,0)+nvl(sub_ot_amt,0)) as lm_amt1 "
          + " from mkt_card_consume " + " where card_no = ? " + " and acct_month between ? and ? ";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no"), lsYm1, lsYm2});
      wp.colSet(ii, "db_purch_amt", Math.round(sqlNum("lm_amt1")));

      String sql2 = " select "
        + " sum(decode(b.item_no, '01', nvl(b.amount,0), 0)+decode(b.item_no, '02', nvl(b.amount,0), 0)+decode(b.item_no, '03', nvl(b.amount,0), 0)+"
        + " decode(b.item_no, '04', nvl(b.amount,0), 0)+decode(b.item_no, '05', nvl(b.amount,0), 0)+decode(b.item_no, '06', nvl(b.amount,0), 0)+"
        + " decode(b.item_no, '07', nvl(b.amount,0), 0)+decode(b.item_no, '08', nvl(b.amount,0), 0)+decode(b.item_no, '09', nvl(b.amount,0), 0)+"
        + " decode(b.item_no, '10', nvl(b.amount,0), 0)+decode(b.item_no, '11', nvl(b.amount,0), 0)+decode(b.item_no, '12', nvl(b.amount,0), 0)+"
        + " decode(b.item_no, '13', nvl(b.amount,0), 0)+decode(b.item_no, '14', nvl(b.amount,0), 0)+decode(b.item_no, '99', nvl(b.amount,0), 0)"
        + ") as lm_amt22"
        + " from mkt_contri_card a,mkt_contri_card_dtl b  where 1=1 " 
        + " and a.card_no  = b.card_no and a.cost_month = b.cost_month" 
        + " and a.card_no = ? " + " and a.cost_month >= ? "
        + " and a.cost_month <= ? ";
      sqlSelect(sql2, new Object[] {wp.colStr(ii, "card_no"), lsYm1, lsYm2});
      wp.colSet(ii, "db_cost_amt", Math.round(sqlNum("lm_amt22")));

      
      String sql3 = " select "
          + " sum(decode(b.item_no, '08', nvl(b.adv_cnt,0))) as lm_amt32 ,"
          + " sum(decode(b.item_no, '09', nvl(b.day_amt,0))) as lm_amt33 ,"
          + " sum(decode(b.item_no, '11', nvl(b.adv_cnt,0))) as lm_amt34 ,"
          + " sum(decode(b.item_no, '10', nvl(b.adv_cnt,0))) as lm_amt35 "
          + " from mkt_contri_card a, mkt_contri_card_dtl b"
          + " where a.card_no = b.card_no and a.cost_month = b.cost_month and a.card_no = ? " 
          + " and a.cost_month >= ? " + " and a.cost_month <= ? ";
      sqlSelect(sql3, new Object[] {wp.colStr(ii, "card_no"), lsYm1, lsYm2});
      wp.colSet(ii, "db_air_shutt_cnt", Math.round(sqlNum("lm_amt32")));
      wp.colSet(ii, "db_air_park_cnt", Math.round(sqlNum("lm_amt33")));
      wp.colSet(ii, "db_favor_room_cnt", Math.round(sqlNum("lm_amt34")));
      wp.colSet(ii, "db_moore_room_cnt", Math.round(sqlNum("lm_amt35"))); 
      
      String sql4 = " select " + " sum(nvl(a.card_income,0)) as lm_amt21 ,"
          + " sum(nvl(a.adv_bill_code,0)) as lm_amt31"
          + " from mkt_contri_card a  where 1=1 " 
          + " and a.card_no = ? " + " and a.cost_month >= ? "
          + " and a.cost_month <= ? ";
        sqlSelect(sql4, new Object[] {wp.colStr(ii, "card_no"), lsYm1, lsYm2});
        wp.colSet(ii, "db_income_amt", Math.round(sqlNum("lm_amt21")));
        wp.colSet(ii, "db_bill_cost", Math.round(sqlNum("lm_amt31")));
        wp.colSet(ii, "wk_contri_amt", "" + (int) (sqlNum("lm_amt21") - sqlNum("lm_amt22")));
        wp.colSet(ii, "wk_amt_2", "" + (int) (sqlNum("lm_amt1") - sqlNum("lm_amt31")));
        
      ilTlPurchAmt += Math.round(sqlNum("lm_amt1"));
      ilTlIncomeAmt += Math.round(sqlNum("lm_amt21"));
      ilTlCostAmt += Math.round(sqlNum("lm_amt22"));
      ilTlContriAmt += (sqlNum("lm_amt21") - sqlNum("lm_amt22"));
      ilTlBillCost += Math.round(sqlNum("lm_amt31"));
      ilTlAirShuttCnt += Math.round(sqlNum("lm_amt32"));
      ilTlAirParkCnt += Math.round(sqlNum("lm_amt33"));
      ilTlFavorRoomCnt += Math.round(sqlNum("lm_amt34"));
      ilTlMooreRoomCnt += Math.round(sqlNum("lm_amt35"));
      ilTlAmt2 += (sqlNum("lm_amt1") - sqlNum("lm_amt31"));

    }

    wp.colSet("tl_cnt", ilTlCnt);
    wp.colSet("tl_purch_amt", ilTlPurchAmt);
    wp.colSet("tl_income_amt", ilTlIncomeAmt);
    wp.colSet("tl_cost_amt", ilTlCostAmt);
    wp.colSet("tl_contri_amt", ilTlContriAmt);
    wp.colSet("tl_bill_cost", ilTlBillCost);
    wp.colSet("tl_air_shutt_cnt", ilTlAirShuttCnt);
    wp.colSet("tl_air_park_cnt", ilTlAirParkCnt);
    wp.colSet("tl_favor_room_cnt", ilTlFavorRoomCnt);
    wp.colSet("tl_moore_room_cnt", ilTlMooreRoomCnt);
    wp.colSet("tl_amt_2", ilTlAmt2);
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  void dataRead2() throws Exception {
    wp.logSql = true;
    cmsr03.Cmsq4210Func func = new cmsr03.Cmsq4210Func();
    func.setConn(wp);
    if (empty(wp.itemStr("ex_date1"))) {
      errmsg("卡片權益 查詢期間[起] 不可空白");
      return;
    } else if (!empty(wp.itemStr("ex_date2"))
        && this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      errmsg("卡片權益 查詢期間 輸入錯誤");
      return;
    }
    func.dataSelect2();
    wp.listCount[1] = func.iiListCnt;
    if (func.iiListCnt == 0) {
      errmsg("查無 卡片權益資料");
      return;
    }
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
    wp.colSet("ex_date1", commString.mid(getSysDate(), 0, 4) + "0101");
    tabClick();
  }

  // 20200107 modify AJAX
  public void wfAjaxIdno() throws Exception {
    // super.wp = wr;

    // String ls_winid =

    selectData1(wp.itemStr("ax_idno"));
    if (rc != 1) {
      wp.addJSON("chi_name", "");
      wp.addJSON("vip_code", "");
      wp.addJSON("score", "");
      wp.addJSON("score_yymm", "");
      wp.addJSON("yy_income_amt", "");
      wp.addJSON("yy_cost_amt", "");
      return;
    }
    wp.addJSON("chi_name", lsChiName);
    wp.addJSON("vip_code", lsVipCode);
    if (sqlStr("income_score").length() > 0)
      wp.addJSON("score", sqlStr("income_score").substring(0, 1));
    else
      wp.addJSON("score", "");

    wp.addJSON("score_yymm", commString.strToYmd(sqlStr("score_yymm")));
    wp.addJSON("yy_income_amt", sqlStr("yy_income_amt"));
    wp.addJSON("yy_cost_amt", sqlStr("yy_cost_amt"));

  }

  // 20200107 modify AJAX
  public void wfAjaxCard() throws Exception {
    // super.wp = wr;

    // String ls_winid =
    selectData2(wp.itemStr("ax_card"));
    if (rc != 1) {
      wp.addJSON("chi_name", "");
      wp.addJSON("vip_code", "");
      wp.addJSON("score", "");
      wp.addJSON("score_yymm", "");
      wp.addJSON("yy_income_amt", "");
      wp.addJSON("yy_cost_amt", "");
      return;
    }
    wp.addJSON("chi_name", lsChiName);
    wp.addJSON("vip_code", lsVipCode);
    if (sqlStr("income_score").length() > 0)
      wp.addJSON("score", sqlStr("income_score").substring(0, 1));
    else
      wp.addJSON("score", "");

    wp.addJSON("score_yymm", commString.strToYmd(sqlStr("score_yymm")));
    wp.addJSON("yy_income_amt", sqlStr("yy_income_amt"));
    wp.addJSON("yy_cost_amt", sqlStr("yy_cost_amt"));
  }

  void selectData1(String idNo) {
    String lsSql = "select " + " id_p_seqno " + " from crd_idno " + " where id_no = ?";
    this.sqlSelect(lsSql, new Object[] {idNo});

    if (sqlRowNum <= 0) {
      alertErr2("卡人資料不存在");
    }

    String lsSql2 = " select " + " uf_acno_name(acno_p_seqno) as chi_name , "
        + " acct_type||' ['||vip_code||'],' as vip_code " + " from act_acno "
        + " where id_p_seqno = ? " + " order by acct_type ";
    sqlSelect(lsSql2, new Object[] {sqlStr("id_p_seqno")});
    lsChiName = sqlStr("chi_name");
    for (int ii = 0; ii < sqlRowNum; ii++) {
      lsVipCode += sqlStr(ii, "vip_code");
    }

    String lsSql3 = " select " + " income_score , " + " score_yymm , "
        + " nvl(yy_income_amt,0) as yy_income_amt , " + " nvl(yy_cost_amt,0) as yy_cost_amt "
        + " from mkt_contri_idno " + " where id_p_seqno = ? ";
    sqlSelect(lsSql3, new Object[] {sqlStr("id_p_seqno")});

    return;
  }

  void selectData2(String cardNo) {
    String lsSql = "select " + " id_p_seqno " + " from crd_card " + " where card_no = ?";
    this.sqlSelect(lsSql, new Object[] {cardNo});

    if (sqlRowNum <= 0) {
      alertErr2("卡片資料不存在");
    }

    String lsSql2 = " select " + wp.sqlID + "uf_idno_name(id_p_seqno) as chi_name , "
        + " acct_type||' ['||vip_code||'],' as vip_code " + " from act_acno "
        + " where id_p_seqno = ? ";
    sqlSelect(lsSql2, new Object[] {sqlStr("id_p_seqno")});
    lsChiName = sqlStr("chi_name");
    for (int ii = 0; ii < sqlRowNum; ii++) {
      lsVipCode += sqlStr(ii, "vip_code");
    }

    String lsSql3 = " select " + " income_score , " + " score_yymm , "
        + " nvl(yy_income_amt,0) as yy_income_amt , " + " nvl(yy_cost_amt,0) as yy_cost_amt "
        + " from mkt_contri_idno " + " where id_p_seqno = ? ";
    sqlSelect(lsSql3, new Object[] {sqlStr("id_p_seqno")});

    return;
  }

  void tabClick() {
    String lsClick = "";
    lsClick = wp.itemStr2("tab_click");
    if (eqIgno(lsClick, "1")) {
      wp.colSet("a_click_1", "id='tab_active'");
    } else if (eqIgno(lsClick, "2")) {
      wp.colSet("a_click_2", "id='tab_active'");
    } else {
      wp.colSet("a_click_1", "id='tab_active'");
    }
  }

}
