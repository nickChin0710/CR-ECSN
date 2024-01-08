/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm02;
/*VIP Code 取消參數維護-主管覆核 V.2018-0118
 * 2018-0118:	JH		modify
 * */
import ofcapp.BaseAction;
import taroko.base.CommSqlStr;

public class Ptrp0480 extends BaseAction {
  String seqNo = "", lsType = "";

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
    } else if (eqIgno(wp.buttonCode, "S1")) {
      /* 動態查詢 */
      dataReadDetl();
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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = "A.*, hex(rowid) as rowid," + "substrb(cancel_cond,1,1) as db_cond_vip, "
        + "substrb(cancel_cond,2,1) as db_cond_block, "
        + "substrb(cancel_cond,3,1) as db_cond_hirisk, "
        + "substrb(cancel_cond,4,1) as db_cond_overdue, "
        + "substrb(cancel_cond,6,1) as db_cond_precash, "
        + "substrb(cancel_cond,8,1) as db_cond_action_code, "
        + "substrb(cancel_cond,9,1) as db_cond_excllist, "
        + "to_char(mod_time,'yyyymmdd') as mod_date," + "'' as xxx";
    wp.daoTable = "ptr_vip_cancel A";
    wp.whereStr = " where 1=1" + " and apr_flag = 'N' ";

    pageSelect();
    if (sqlNotFind()) {
      wp.alertMesg("查無可覆核資料");
      return;
    }
    dataReadAfter();
  }

  void dataReadAfter() {
    // --
    String sql1 = "select sum(decode(data_type,'A01',1,0)) as cnt_A01,"
        + " sum(decode(data_type,'A02',1,0)) as cnt_A02,"
        + " sum(decode(data_type,'A03',1,0)) as cnt_A03,"
        + " sum(decode(data_type,'A04',1,0)) as cnt_A04,"
        + " sum(decode(data_type,'A05',1,0)) as cnt_A05," + " count(*) as db_cnt"
        + " from ptr_vip_data" + " where 1=1" + sqlCol(wp.colNvl("apr_flag", "N"), "apr_flag");
    this.sqlSelect(sql1);
    wp.colSet("cnt_A01", this.sqlInt("cnt_A01"));
    wp.colSet("cnt_A02", this.sqlInt("cnt_A02"));
    wp.colSet("cnt_A03", this.sqlInt("cnt_A03"));
    wp.colSet("cnt_A04", this.sqlInt("cnt_A04"));
    wp.colSet("cnt_A05", this.sqlInt("cnt_A05"));
  }

  void dataReadDetl() throws Exception {
    resetType();
    seqNo = wp.itemStr("data_k1");
    lsType = wp.itemStr("data_k2");
    if (empty(seqNo))
      seqNo = wp.itemStr("seq_no");
    if (empty(lsType)) {
      lsType = "A01";
      wp.colSet("title_type", "VIP Code");
      wp.colSet("type1", "disabled");
    } else if (eqIgno(lsType, "1")) {
      lsType = "A01";
      wp.colSet("title_type", "VIP Code");
      wp.colSet("type1", "disabled");
    } else if (eqIgno(lsType, "2")) {
      lsType = "A02";
      wp.colSet("title_type", "凍結碼");
      wp.colSet("type2", "disabled");
    } else if (eqIgno(lsType, "3")) {
      lsType = "A03";
      wp.colSet("title_type", "風險分類");
      wp.colSet("type3", "disabled");
    } else if (eqIgno(lsType, "4")) {
      lsType = "A04";
      wp.colSet("title_type", "ACTION CODE");
      wp.colSet("type4", "disabled");
    } else if (eqIgno(lsType, "5")) {
      lsType = "A05";
      wp.colSet("title_type", "排除名單-身分證ID");
      wp.colSet("type5", "disabled");
    }

    wp.selectSQL = " data_code ";
    wp.daoTable = " ptr_vip_data ";
    wp.whereStr = " where 1=1 " 
                              + " and table_name = 'PTR_VIP_CANCEL' " 
    		                  + " and apr_flag = 'N' "
                              + sqlCol(lsType, "data_type");
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    logSql();
    pageQuery();
    wp.setListCount(1);

    wp.colSet("data_type", lsType);
  }

  void resetType() {
    wp.colSet("type1", "class=btAdd_detl");
    wp.colSet("type2", "class=btAdd_detl");
    wp.colSet("type3", "class=btAdd_detl");
    wp.colSet("type4", "class=btAdd_detl");
    wp.colSet("type5", "class=btAdd_detl");
  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    ptrm02.Ptrp0480Func func = new ptrm02.Ptrp0480Func();
    func.setConn(wp);
    if (func.dataProc() == 1) {
      sqlCommit(1);
      alertMsg("覆核成功");
    } else {
      this.dbRollback();
      errmsg(func.getMsg());
    }

  }

  @Override
  public void initButton() {
    if (wp.colEq("rowid", "")) {
      this.btnUpdateOn(false);
    }

  }

  @Override
  public void initPage() {
    try {
      dataRead();
    } catch (Exception ex) {
    }

  }

}
