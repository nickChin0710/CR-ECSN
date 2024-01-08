/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package colm05;

import ofcapp.AppMsg;
import ofcapp.BaseQuery;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm5730 extends BaseQuery {
  //String dataKK1 = "1", 
  String   acctType = "", validDate = "", execDate = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
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
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "colm5730")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type||'_'||chin_name",
            "where 1=1 order by acct_type Asc");
      }

    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where param_type='1'" + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
        + sqlCol(wp.itemStr("ex_exec_date"), "exec_date", "like%");
    wp.whereOrder = " order by exec_date ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "param_type, " + "acct_type," + "valid_date, " + "exec_date," + "exec_mode,"
        + "exec_times," + "exec_msg," + "exec_date_e," + "rowid";
    wp.daoTable = "rsk_stopexec";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by exec_date Desc ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    listWkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  void listWkdata() {
    String wkData = "";
    String[] cde = new String[] {"1", "2", "3", "9"};
    String[] txt = new String[] {"每月固定一天", "CYCLE後N日", "每天", "暫不執行"};
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "exec_mode");
      wp.colSet(ii, "tt_exec_mode", commString.decode(wkData, cde, txt));
    }
  }

  @Override
  public void querySelect() throws Exception {
    acctType = wp.itemStr("data_k2");
    validDate = wp.itemStr("data_k3");
    execDate = wp.itemStr("data_k4");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acctType)) {
      acctType = wp.itemStr("acct_type");
    }
    if (empty(validDate)) {
      validDate = wp.itemStr("valid_date");
    }
    if (empty(execDate)) {
      execDate = wp.itemStr("exec_date");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "param_type,   " + "acct_type, "
        + "valid_date, " + "exec_date," + "exec_mode," + "exec_times," + "exec_date_s,"
        + "exec_date_e," + "t_acct_cnt," + "t_stop_cnt," + "t_stopnot1_cnt," + "t_stopnot2_cnt,"
        + "t_stopnot3_cnt," + "t_stopnot4_cnt," + "t_stopnot5_cnt," + "t_stopnot6_cnt,"
        + "exec_msg";
    wp.daoTable = "rsk_stopexec";
    wp.whereStr = " where param_type='1'" + sqlCol(acctType, "acct_type") + sqlCol(validDate, "valid_date")
        + sqlCol(execDate, "exec_date");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + acctType + "," + validDate);
      return;
    }
    detlWkdata();
  }

  void detlWkdata() {

    String execMode = wp.colStr("exec_mode");
    String[] cde = new String[] {"1", "2", "3", "9"};
    String[] txt = new String[] {"每月固定一天", "CYCLE後N日", "每天", "暫不執行"};
    wp.colSet("tt_exec_mode", commString.decode(execMode, cde, txt));

    String paramType = wp.colStr("param_type");
    if (eqAny(paramType, "1")) {
      wp.colSet("tt_param_type", "一般卡");
    } else
      wp.colSet("tt_param_type", "");

    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }

  }
}
