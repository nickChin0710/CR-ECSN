/** 
 * 2019-1205:  Alex  add initButton
 109-04-27    shiyuqi       updated for project coding standard     * 
 * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
 * */
package cmsm03;

import ofcapp.BaseAction;

public class Cmsm4230 extends BaseAction {
  CmsRight ooRight = new CmsRight();

  @Override
  public void userAction() throws Exception {
    ooRight.setConn(wp);

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
    if (itemallEmpty("ex_idno,ex_card_no")) {
      alertErr2("請輸入查詢條件");
      return;
    }

    getIdPseqno();
    if (rc != 1)
      return;

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    int ilWpRows = 0, ilCardCnt = 0, ilCalCnt = 0;

    String lsIdPSeqno = "", lsYyyy = "";
    lsYyyy = wp.sysDate.substring(0, 4);

    wp.sqlCmd = "select A.* " + ", C.group_code, C.card_type, C.sup_flag"
        + ", uf_tt_idtab('RIGHT_ITEM_NO',A.item_no) as tt_item_no" + ", 0 as cal_cnt"
        + " from cms_right_cal A left join crd_card C on C.card_no=A.card_no" + " where 1=1"
        + sqlCol(lsYyyy, "A.curr_year") + sqlCol(wp.colStr("id_p_seqno"), "A.id_p_seqno");

    pageQuery();
    if (sqlRowNum <= 0) {
      return;
    }

    wp.listCount[0] = sqlRowNum;
    queryReadAfter();
  }

  void queryReadAfter() {
    if (wp.colEq(0, "card_hldr_flag", "1")) {
      wp.colSet("tt_card_hldr_flag", "行員");
    } else if (wp.colEq(0, "card_hldr_flag", "2")) {
      wp.colSet("tt_card_hldr_flag", "非行員");
    }

    String sql1 = "select max(last_year_consume) as wk_last_consume"
        + ", max(curr_year_consume) as wk_this_consume"
        + ", sum(decode(item_no,'08',free_cnt,0)) as wk_cnt08"
        + ", sum(decode(item_no,'08',use_cnt,0)) as wk_use08"
        + ", sum(decode(item_no,'09',free_cnt,0)) as wk_cnt09"
        + ", sum(decode(item_no,'09',use_cnt,0)) as wk_use09"
        + ", sum(decode(item_no,'10',free_cnt,0)) as wk_cnt10"
        + ", sum(decode(item_no,'10',use_cnt,0)) as wk_use10"
        + ", sum(decode(item_no,'13',free_cnt,0)) as wk_cnt13"
        + ", sum(decode(item_no,'13',use_cnt,0)) as wk_use13"
        + ", sum(decode(free_type,'2',0,use_cnt * free_per_amt)) as wk_last_use"
        + ", sum(decode(free_type,'2',use_cnt * free_per_amt,0)) as wk_this_use"
        + " from cms_right_cal" + " where id_p_seqno =?";
    sqlSelect(wp, sql1, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum > 0) {
      double lmAmt = wp.colNum("wk_last_consume") - wp.colNum("wk_last_use");
      wp.colSet("wk_last_net", lmAmt);
      lmAmt = wp.colNum("wk_this_consume") - wp.colNum("wk_this_use");
      wp.colSet("wk_this_net", lmAmt);
    }

    String[] aaCode = new String[] {"1", "2", "3", "4", "6"};
    String[] aaText = new String[] {"不計消費", "當年消費", "前年消費", "前年加贈", "特殊消費"};
    for (int ii = 0; ii < wp.listCount[0]; ii++) {
      String freeType = wp.colStr(ii, "free_type");
      wp.colSet(ii, "tt_free_type", commString.decode(freeType, aaCode, aaText));
    }
  }

  void getIdPseqno() {
    String sql1 = "";
    if (wp.itemEmpty("ex_idno") == false) {
      sql1 = " select id_p_seqno, chi_name from crd_idno where id_no = ? ";
      sqlSelect(sql1, new Object[] {wp.itemStr2("ex_idno")});
      if (sqlRowNum <= 0) {
        alertErr("身分證ID: 輸入錯誤");
        return;
      }
    }

    if (wp.itemEmpty("ex_card_no") == false) {
      sql1 = " select B.id_p_seqno, B.chi_name from crd_card A join crd_idno B"
          + " on B.id_p_seqno=A.major_id_p_seqno where A.card_no = ? ";
      sqlSelect(sql1, new Object[] {wp.itemStr2("ex_card_no")});
      if (sqlRowNum <= 0) {
        alertErr("卡號: 輸入錯誤");
        return;
      }
    }
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
    wp.colSet("ex_chi_name", sqlStr("chi_name"));
    return;
  }

  boolean checkParmCal(String cardno, String itemno) {

    String lsYyyy = "";
    lsYyyy = commString.mid(getSysDate(), 0, 4);
    String sql1 =
        " select count(*) as db_cnt from cms_right_cal where curr_year = ? and card_no = ? and item_no = ? ";
    sqlSelect(sql1, new Object[] {lsYyyy, cardno, itemno});
    if (sqlNum("db_cnt") > 0)
      return true;

    return false;
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
    String lsIdPSeqno = "";
    boolean lbMega = false;

    if (itemallEmpty("ex_idno,ex_card_no")) {
      alertErr("請輸入計算條件");
      return;
    }

    // --以卡號輸入
    if (wp.itemEmpty("ex_card_no") == false) {
      lbMega = ooRight.bankEmployee(wp.itemStr2("ex_card_no"));
    } else if (wp.itemEmpty("ex_idno") == false) {
      lbMega = ooRight.bankEmployee(wp.itemStr2("ex_idno"));
    } else {
      alertErr2("請輸入計算條件");
      return;
    }

    getIdPseqno();
    lsIdPSeqno = wp.colStr("id_p_seqno");
    if (empty(lsIdPSeqno)) {
      alertErr("非本行卡友");
      return;
    }

    // --以正卡人為主以 major_id_p_seqno 串所有卡號
    daoTid = "card.";
    String sql1 = "select card_no from crd_card where major_id_p_seqno =? and current_code='0'";
    sqlSelect(sql1, new Object[] {lsIdPSeqno});
    if (sqlRowNum <= 0) {
      alertErr2("卡號輸入錯誤 !");
      return;
    }
    int ilSelectCnt = sqlRowNum;

    for (int ii = 0; ii < ilSelectCnt; ii++) {
      String lsCardNo = sqlStr(ii, "card.card_no");
      // --權益 08-機場接送
      // if (checkParmCal(ls_card_no, "08") == false) {
      ooRight.checkCardRight(lsCardNo, "08");
      // }
      // --權益 09-機場停車
      // if (checkParmCal(ls_card_no, "09") == false) {
      ooRight.checkCardRight(lsCardNo, "09");
      // }
      // --權益 10-摩爾貴賓室
      // if (checkParmCal(ls_card_no, "10") == false) {
      ooRight.checkCardRight(lsCardNo, "10");
      // }
      // --權益 13-新貴通貴賓室
      // if (checkParmCal(ls_card_no, "13") == false) {
      ooRight.checkCardRight(lsCardNo, "13");
      // }
    }

    if (ooRight.iiSuccessCnt > 0) {
      sqlCommit(1);
      wp.respMesg = "權益計算完成，請點選查詢";
    } else {
      alertErr2(ooRight.getMsg());
    }

  }

  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
