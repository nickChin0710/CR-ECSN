package cmsr02;
/** 19-0614:   JH    p_xxx >>acno_p_xxx
 *  19-1126:   Alex  notfind read name
 ** 109-04-27   shiyuqi       updated for project coding standard     * 
 * */
import ofcapp.BaseAction;

public class Cmsq3220 extends BaseAction {

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
    try {
      if (eqIgno(wp.respHtml, "cmsq3220")) {
        wp.optionKey = wp.colStr(0, "ex_win_stage");
        dddwList("dddw_win_stage",
            "SELECT DISTINCT win_stage as db_code, win_stage as db_desc FROM sms_einvo_dtl ORDER BY win_stage desc");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere =
        " where to_char(to_date (proc_date, 'yyyymmdd')+ 99 days ,'yyyymmdd') > to_char(sysdate,'yyyymmdd') "
            + sqlCol(wp.itemStr("ex_win_stage"), "win_stage");

    if (!empty(wp.itemStr("ex_idno"))) {
      String lsIdno = wp.itemStr2("ex_idno");
      lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1"
          //+ commSqlStr.col(lsIdno, "id_no") + " union select id_p_seqno from dbc_idno where 1=1"
          + sqlCol(lsIdno, "id_no") + " union select id_p_seqno from dbc_idno where 1=1"
          + sqlCol(lsIdno, "id_no") + " )";
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "card_no");
    }

    if (wp.itemEmpty("ex_idno") == false || wp.itemEmpty("ex_card_no") == false) {
      if (!empty(wp.itemStr("ex_idno"))) {
        selectByIdnoCrd();
        if (sqlRowNum <= 0)
          selectByIdnoDbc();
      } else if (!empty(wp.itemStr("ex_card_no"))) {
        selectByCardNo();
      }
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " card_no , " + " substr(card_no,13,4) as wk_card_no4 , " + " win_stage , "
        + " substr(win_stage,1,4) as wk_stage4 ," + " vd_flag , " + " id_p_seqno , "
        + " award_info ";
    wp.daoTable = " sms_einvo_dtl ";
    wp.whereOrder = " ORDER BY card_no ";

    pageQuery();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);

    wp.setPageValue();
  }

  void queryAfter() {

    int liDate = 0;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      liDate = (int) (wp.colNum(ii, "wk_stage4") - 1911);
      wp.colSet(ii, "wk_win_stage",
          "民國年" + liDate + "/" + wp.colStr(ii, "win_stage").substring(4, 6) + "-"
              + wp.colStr(ii, "win_stage").substring(6, 8));
    }
  }

  void selectByIdnoCrd() {
    String sql1 = "select chi_name as ex_chi_name " + " from crd_idno " + " where id_no =?";
    sqlSelect(sql1, new Object[] {wp.itemStr("ex_idno")});
    wp.colSet("ex_chi_name", sqlStr("ex_chi_name"));
  }

  void selectByIdnoDbc() {
    String sql1 = "select chi_name as ex_chi_name " + " from dbc_idno " + " where id_no =?";
    sqlSelect(sql1, new Object[] {wp.itemStr("ex_idno")});
    wp.colSet("ex_chi_name", sqlStr("ex_chi_name"));
  }

  void selectByCardNo() {
    String sql1 = "select " + wp.sqlID + " uf_card_name(?) as ex_chi_name " + " from dual ";
    sqlSelect(sql1, new Object[] {wp.itemStr("ex_card_no")});
    wp.colSet("ex_chi_name", sqlStr("ex_chi_name"));
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
    // TODO Auto-generated method stub

  }

}
