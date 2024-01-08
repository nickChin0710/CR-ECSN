/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-09-07  V2.00.01  Ryan       program initial                            * 
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                               
* 110-10-26  V1.00.04   Justin        中止->終止                                                       *    
* 111-01-18  V1.00.05   Ryan      status_code 4.重複取消->4.申請中           *    
* 111-06-16  V1.00.06  Justin     3.中止(實體卡停卡) => 3.中止(已停用)       * 
******************************************************************************/
package cmsm03;

import ofcapp.BaseAction;

public class Cmsm2311 extends BaseAction {
  String cardNo = "", vCardNo = "";

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

    String lsWhere = " ";
    if (!empty(wp.itemStr("ex_vcard_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_vcard_no"), "A.v_card_no");
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "A.card_no");
    }
    if (!empty(wp.itemStr("ex_idno"))) {
      lsWhere += " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
          + sqlCol(wp.itemStr("ex_idno"), "id_no") + ")";
    }
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }


  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.sqlCmd = "SELECT  " + " A.v_card_no ," + " A.card_no ," 
    	+ " decode(A.status_code,'0','0.正常','1','1.暫停','2','2.終止(人工)','3','3.終止(已停用)','4','4.申請中','5','5.終止(已過效期)',A.status_code) as tt_status_code ,"
    	+ " A.change_date ,"
        + " A.new_end_date ," 
    	//+ " A.sir_status ," 
        + " uf_idno_id(A.id_p_seqno) as db_idno ,"
        + " A.id_p_seqno ," 
        + " '1.主檔' as db_tab_name "
        + " FROM oempay_card A join crd_card B on A.card_no =B.card_no " 
        + " where 1=1 " + wp.queryWhere;

    wp.pageCountSql = "select count(*) from (" + " select hex(A.rowid) " + " FROM oempay_card A join crd_card B on A.card_no =B.card_no "
        + " where 1=1 " + wp.queryWhere 

        + " )";

    this.pageQuery();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    vCardNo = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNo)) {
      wp.selectSQL = "A.v_card_no ," + " A.card_no ," 
    	  + " A.status_code ," 
    	  + " decode(A.status_code,'0','0.正常','1','1.暫停','2','2.終止(人工)','3','3.終止(已停用)','4','4.申請中','5','5.終止(已過效期)',A.status_code) as tt_status_code ,"
    	  + " '' as change_code ," 
    	  + " A.change_date ,"
          + " A.new_end_date ," 
    	  //+ " A.sir_status ," 
          + " A.id_p_seqno ,"
          + " B.current_code as db_current_code , "
          + " B.bin_type , "
          + " B.oppost_date , "
          + " B.oppost_reason " 
          //+ " 'N' as proc_flag "

      ;
      wp.daoTable = " oempay_card A join crd_card B on A.card_no =B.card_no ";
      wp.whereStr = " where 1=1 " + sqlCol(vCardNo, "A.v_card_no");
      this.logSql();
      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + vCardNo);
        return;
      }
    } 

  }

  @Override
  public void saveFunc() throws Exception {
    cmsm03.Cmsm2311Func func = new cmsm03.Cmsm2311Func();
    func.setConn(wp);
//    if (empty(wp.itemStr("rowid"))) {
//      rc = func.dbInsert();
//    } else {
      rc = func.dbSave(strAction);
//    }
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      this.saveAfter(false);
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.autUpdate()) {
      if (wp.colEmpty("rowid")) {
        btnOnAud(false, true, false);
      } else {
        btnOnAud(false, true, true);
      }
    } else {
      btnOnAud(false, false, false);
    }
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
