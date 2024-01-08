/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard  
* 111-04-19  V1.00.01  machao     TSC畫面整合    *
* 112-09-22  V1.00.02  Ryan        調整黑名單類別中文    *
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;
import taroko.base.CommString;

public class Tscq0050 extends BaseAction {

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

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_tsc_card")) && empty(wp.itemStr("ex_from_mark"))
        && empty(wp.itemStr("ex_crt_date1")) && empty(wp.itemStr("ex_crt_date2"))) {
      alertErr2("條件不可全部空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("產生日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
        + sqlCol(wp.itemStr("ex_tsc_card"), "tsc_card_no", "like%");

    if (!wp.itemEq("ex_from_mark", "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_from_mark"), "from_mark");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " media_crt_date , " + " media_crt_time , " + " tsc_card_no , "
        + " oppost_date , " + " current_code , " + " from_mark , " + " proc_flag , "
        + " crt_date , " + " crt_time  ";
    wp.daoTable = "tsc_bkec_log ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by media_crt_date Asc, media_crt_time Asc, tsc_card_no Asc ";
    logSql();
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
    commFromMark("from_mark");
  }
  
  void commFromMark(String commStr) {
	  CommString comms = new CommString();
	  for(int i=0;i<wp.selectCnt;i++) {
		  String val = wp.colStr(i,commStr);
		  wp.colSet(i,"comm_"+commStr, comms.decode(val, ",1,2,3,4,5,6,7,8",",例外檔(人工),問題交易,強停、系統停用、凍結碼38,逾期,凍結碼,卡特指,掛失轉卡、毁損補發,其他停用碼"));
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
    // TODO Auto-generated method stub

  }

}
