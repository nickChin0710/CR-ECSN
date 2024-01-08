/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                   *
* 110-01-05  V1.00.02  Tanwei       zzDate,zzStr,zzComm,zzCurr變量更改                  *  *
* 110-03-22  V1.00.03  Tanwei       MKT_CONTRI_CARD表拆分邏輯改動                                             *
* 110-03-23  V1.00.04  Tanwei       收入金額不對bug修改                                                                          *
******************************************************************************/
package cmsr03;
/** 卡友權益費用D檔查詢
 * 2019-0705   JH    cond-require
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 20-0107:   Ru    modify AJAX
 */

import java.text.ParseException;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Cmsq4220 extends BaseAction {
  String lsVipCode = "", lsChiName = "", lsYm1 = "", lsYm2 = "";
  int liPurAmt = 0, liIncomAmt = 0, liCostAmt = 0, liContriAmt = 0;
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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (itemallEmpty("ex_idno,ex_card_no".split(","))) {
      alertErr2("身分證ID,卡號: 不可同時空白");
      return;
    }
    if (empty(wp.itemStr("ex_date1"))) {
      alertErr2("刪除費用 查詢期間[起] 不可空白");
      return;
    } else if (!empty(wp.itemStr("ex_date2"))
        && chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("刪除費用 查詢期間 輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 ";
    if (!empty(wp.itemStr("ex_idno"))) {
      lsWhere += sqlCol(wp.itemStr("id_p_seqno"), "major_id_p_seqno");
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "card_no");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " A.major_card_no ,"
        + " decode(A.group_code,'0000',uf_tt_card_type(A.card_type),uf_tt_group_code(A.group_code)) as group_name ,"
        + " A.sup_flag ," + " A.card_no ," + " 0 db_purch_amt ," + " 0 db_income_amt ,"
        + " 0 db_cost_amt ";
    wp.daoTable = " crd_card A  ";
    wp.whereOrder = " order by 1,2,3 ";
    logSql();
    pageQuery();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.colSet("ft_cnt", "" + sqlRowNum);
    queryAfter();
    wp.setPageValue();
    dataRead2();

  }

  void queryAfter() throws ParseException {
    busi.func.EcsComm ecs = new busi.func.EcsComm();
    ecs.setConn(wp);

    lsYm1 = commDate.dateAdd(ecs.getBusiDate(), -1, 0, 0);
    lsYm1 = lsYm1.substring(0, 4) + "01";
    lsYm2 = lsYm1.substring(0, 4) + "12";

    for (int ii = 0; ii < wp.selectCnt; ii++) {

      String sql1 = " select "
          + " sum(consume_bl_amt+consume_ca_amt+consume_it_amt+consume_ao_amt+consume_id_amt+consume_ot_amt) "
          + " - " + " sum(sub_bl_amt+sub_ca_amt+sub_it_amt+sub_ao_amt+sub_id_amt+sub_ot_amt) "
          + " as lm_amt1 " + " from mkt_card_consume " + " where card_no = ?"
          + " and acct_month between ? and ? ";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no"), lsYm1, lsYm2});
      liPurAmt += sqlNum("lm_amt1");
      wp.colSet(ii, "db_purch_amt", "" + sqlNum("lm_amt1"));

      // *-- 原sqL注釋
      /*
       * String sql2 = " select  " + " sum(card_income) as lm_amt21 ," +
       * " sum(bonu_amt+cash_amt+card_cost+ " + " safe_risks+plat_secr+portr_license+ " +
       * " bill_cost+air_shutt+air_park+ " + " moore_room+city_park+movice_offer+ " +
       * " favor_room+road_service+other_cost) " + " as lm_amt22 " + " from mkt_contri_card " +
       * " where card_no = ? " + " and cost_month between ? and ? ";
       */
      
      String sql2 = " select " + " sum(nvl(a.card_income,0)) as lm_amt21"
          + " from mkt_contri_card a  where 1=1 " 
          + " and a.card_no = ? " + " and a.cost_month >= ? "
          + " and a.cost_month <= ? ";
        sqlSelect(sql2, new Object[] {wp.colStr(ii, "card_no"), lsYm1, lsYm2});
        liIncomAmt += sqlNum("lm_amt21");
        wp.colSet(ii, "db_income_amt", "" + sqlNum("lm_amt21"));
      
      String sql3 = " select "
          + " sum(decode(b.item_no, '01', nvl(b.amount,0), 0)+decode(b.item_no, '02', nvl(b.amount,0), 0)+"
          + " decode(b.item_no, '03', nvl(b.amount,0), 0)+decode(b.item_no, '04', nvl(b.amount,0), 0)+"
          + " decode(b.item_no, '05', nvl(b.amount,0), 0)+decode(b.item_no, '06', nvl(b.amount,0), 0)+"
          + " decode(b.item_no, '07', nvl(b.amount,0), 0)+decode(b.item_no, '08', nvl(b.amount,0), 0)+"
          + " decode(b.item_no, '09', nvl(b.amount,0), 0)+decode(b.item_no, '10', nvl(b.amount,0), 0)+"
          + " decode(b.item_no, '11', nvl(b.amount,0), 0)+decode(b.item_no, '12', nvl(b.amount,0), 0)+"
          + " decode(b.item_no, '13', nvl(b.amount,0), 0)+decode(b.item_no, '14', nvl(b.amount,0), 0)+"
          + " decode(b.item_no, '99', nvl(b.amount,0), 0)) as lm_amt22 " 
          + " from mkt_contri_card a,mkt_contri_card_dtl b  where 1=1 " 
          + " and a.card_no  = b.card_no and a.cost_month = b.cost_month" 
          + " and a.card_no = ? " 
          + " and a.cost_month >= ? " + " and a.cost_month <= ? "
          + " and b.item_no in ('01','02','03','04','05','06','07','08','09','10','11','12','13','14','99')";
        sqlSelect(sql3, new Object[] {wp.colStr(ii, "card_no"), lsYm1, lsYm2});
       
      liCostAmt += sqlNum("lm_amt22");
      wp.colSet(ii, "db_cost_amt", "" + sqlNum("lm_amt22"));
      wp.colSet(ii, "wk_contri_amt", "" + (int) (sqlNum("lm_amt21") - sqlNum("lm_amt22")));
      liContriAmt += (int) (sqlNum("lm_amt21") - sqlNum("lm_amt22"));
    }

    wp.colSet("sum_purch_amr", "" + liPurAmt);
    wp.colSet("sum_incom_amt", "" + liIncomAmt);
    wp.colSet("sum_cost_amt", "" + liCostAmt);
    wp.colSet("sum_contri_amt", "" + liContriAmt);

  }

  void dataRead2() {
    cmsr03.Cmsq4220Func func = new cmsr03.Cmsq4220Func();
    func.setConn(wp);
    func.dataSelect();
    wp.listCount[1] = func.listCnt;
    if (func.listCnt == 0) {
      errmsg("查無 費用刪除資料");
      return;
    }
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
    wp.colSet("ex_date1", commString.mid(getSysDate(), 0, 4) + "0101");

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
      wp.addJSON("id_p_seqno", "");
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
    wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));

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
      wp.addJSON("id_p_seqno", "");
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
    wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
  }

  void selectData1(String idNo) {
    String lsSql = "select " + " id_p_seqno " + " from crd_idno " + " where id_no ='" + idNo + "'";
    this.sqlSelect(lsSql);

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
    String lsSql = "select " + " id_p_seqno " + " from crd_card " + " where card_no ='" + cardNo + "'";
    this.sqlSelect(lsSql);

    if (sqlRowNum <= 0) {
      alertErr2("卡片資料不存在");
    }

    String lsSql2 = " select " + " uf_idno_name(id_p_seqno) as chi_name , "
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

}
