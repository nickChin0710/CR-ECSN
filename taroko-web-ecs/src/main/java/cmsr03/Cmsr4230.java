/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package cmsr03;
/*
 * * 20-0115:   Ru    add vip_kind
 * */
import ofcapp.BaseAction;

public class Cmsr4230 extends BaseAction {

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
    if (this.chkStrend(wp.itemStr("ex_oppo_date1"), wp.itemStr("ex_oppo_date2")) == false) {
      alertErr2("停用日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_oppo_date1"), "oppost_date", ">=")
        + sqlCol(wp.itemStr("ex_oppo_date2"), "oppost_date", "<=")
        + sqlCol(wp.itemStr("ex_ppcard_no"), "pp_card_no")
        + sqlCol(wp.itemStr("ex_bin_type"), "bin_type")
        + sqlCol(wp.itemStr("ex_vip_kind"), "vip_kind");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " vip_kind ," + " pp_card_no ," + " current_code ," + " bin_type ,"
        + " oppost_date ," + " valid_to ," + " issue_date ," + " eng_name ," + " valid_fm ,"
        + " oppost_reason ," + " new_end_date , "
        + " decode(oppost_reason,'P1','Lost(L遺失)','P2','Stolen(S)','P3','Cancelled(CA取消)','P4','Not Received(NR)','B1','屆期停用',oppost_reason) as tt_oppost_reason ";
    wp.daoTable = "crd_card_pp ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by 1 ";
    logSql();
    pageQuery();
    if (sqlRowNum < 0) {
      alertErr2("此條件查無資料");
      return;
    } else {
      for (int i = 0; i < sqlRowNum; i++) {
        // 貴賓卡
        if ("1".equals(wp.colStr(i, "vip_kind"))) {
          wp.colSet(i, "vip_kind", "1_新貴通");
        } else if ("2".equals(wp.colStr("vip_kind"))) {
          wp.colSet(i, "vip_kind", "2_龍騰卡");
        }
      }
    }
    // queryAfter();
    wp.setListCount(1);

    wp.setPageValue();


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
