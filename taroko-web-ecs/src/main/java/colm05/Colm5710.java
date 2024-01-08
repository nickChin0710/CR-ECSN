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
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm5710 extends BaseEdit {
  Colm5710Func func;
//  String kk1 = "1",
  String  acctType = "", validDate = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
      if (eqIgno(wp.respHtml, "colm5710")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type||'_'||chin_name",
            "where 1=1 order by acct_type Asc");
      }

      if (eqIgno(wp.respHtml, "colm5710_detl")) {
        wp.optionKey = wp.colStr(0, "kk_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type||'_'||chin_name",
            "where 1=1 order by acct_type Asc");
      }

    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where param_type='1'" + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
        + sqlCol(wp.itemStr("ex_valid_date"), "valid_date");
    wp.whereOrder = " order by acct_type , valid_date ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "acct_type, " + "valid_date," + "apr_flag, " + "pause_flag," + "exec_mode,"
        + "exec_cycle_nday," + "mcode_value," + "debt_amt," + "exec_date";
    wp.daoTable = "ptr_stopparam";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
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

      if (wp.colEq(ii, "pause_flag", "Y")) {
        wp.colSet(ii, "tt_pause_flag", "執行中");
      } else if (wp.colEq(ii, "pause_flag", "N")) {
        wp.colSet(ii, "tt_pause_flag", "暫停執行");
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    acctType = wp.itemStr("data_k2");
    validDate = wp.itemStr("data_k3");
    dataRead();


  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acctType)) {
      acctType = itemKk("acct_type");
    }
    if (empty(validDate)) {
      validDate = itemKk("valid_date");
    }
    
    if (empty(acctType)) {
    	alertErr("帳戶類別: 不可空白");
    	return ;
    }
    
    if (empty(validDate)) {
    	alertErr("生效日期: 不可空白");
    	return ;
    }
    
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "param_type,   " + "acct_type, "
        + "valid_date, " + "apr_flag," + "exec_mode," + "exec_day," + "exec_cycle_nday,"
        + "exec_date," + "n0_month," + "n1_cycle," + "mcode_value," + "debt_amt," + "pause_flag,"
        + "non_af," + "non_ri," + "non_pn," + "non_pf," + "non_lf," + "mod_user,"
        + "to_char(mod_time,'yyyymmdd') as mod_date ," + "mod_pgm," + "mod_seqno";
    wp.daoTable = "ptr_stopparam";
    wp.whereStr = " where param_type='1'" + sqlCol(acctType, "acct_type") + sqlCol(validDate, "valid_date");
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
    String[] qwe = new String[] {"1"};
    String[] rty = new String[] {"一般卡"};
    wp.colSet("tt_param_type", commString.decode(paramType, qwe, rty));

    if (wp.colEq("pause_flag", "Y")) {
      wp.colSet("tt_pause_flag", "執行中");
    } else if (wp.colEq("pause_flag", "N")) {
      wp.colSet("tt_pause_flag", "暫停執行");
    }

    if (wp.colEq("apr_flag", "Y")) {
      wp.colSet("tt_apr_flag", "放行");
    } else if (wp.colEq("apr_flag", "N")) {
      wp.colSet("tt_apr_flag", "取消放行");
    }

    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_acct_type", sqlStr("chin_name"));
    }

  }

  @Override
  public void saveFunc() throws Exception {
    func = new colm05.Colm5710Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }
}
