package secm01;
/** 群組(子系統+使用者層級)權限設定覆核處理
 * 2019-0808   JH    modify
 *  V.2018-0824.jh
 *  109-04-20  shiyuqi       updated for project coding standard  
 *  109-12-24  Justin          parameterize sql 
 *  110-01-20  Justin          fix a query bug
 *  110-01-22  Justin          disabled rows when  crt user equals to loginUser 
 *  110-01-26  Justin          fix the bug causing sql error
 * */

import ofcapp.BaseAction;

public class Secp2070 extends BaseAction {

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
    try {
      if (eqIgno(wp.respHtml, "secp2070")) {
        wp.optionKey = wp.colStr(0, "ex_group_id");
        dddwList("dddw_group_id", "sec_workgroup", "group_id", "group_name", "where 1=1 and apr_date<>''");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "secp2070")) {
        wp.optionKey = wp.colStr(0, "ex_user_level");
        dddwList("dddw_user_level", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SEC_USRLVL'");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemNum("ex_apr_cnt") > 500) {
      alertErr("覆核筆數: 不可超過 500");
      return;
    }

    wp.colSet("kk_group_id", wp.itemStr2("ex_group_id"));
    wp.colSet("kk_user_level", wp.itemStr2("ex_user_level"));

    StringBuffer sb = new StringBuffer();
    
    sb.append(" where A.apr_flag <> 'Y' " );
    
    if (wp.itemEmpty("ex_group_id") == false) {
		sb.append(" and A.group_id = :ex_group_id ");
		setString("ex_group_id", wp.itemStr("ex_group_id"));
	}
    
    if (wp.itemEmpty("ex_user_level") == false) {
		sb.append(" and A.user_level = :ex_user_level ");
		setString("ex_user_level", wp.itemStr("ex_user_level"));
	}
    
//    if (empty(wp.loginUser)) {
//		sb.append(" and A.crt_user = :login_user ");
//		setString("login_user", wp.loginUser);
//	}
    
    String lsWhere = sb.toString();
    setSqlParmNoClear(true);
    selectTotCnt(lsWhere);
    wp.whereStr = lsWhere;
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.crt_date, A.crt_user," + " A.wf_winid , " + " A.apr_flag , "
        + " A.aut_query , " + " A.aut_update , " + " A.aut_approve , " + " A.mod_audcode,"
        + " hex(A.rowid) as log_rowid " + ", B.wf_name"
        + ", decode(A.mod_audcode,'A','指定','D','取消','U','異動') as tt_mod_audcode"
        + ", uf_same_dept(A.crt_user, :login_user ) as db_same_dept";
    wp.selectSQL += String.format(" , decode( A.crt_user , '%s' , 'disabled', '') as canApprove ", wp.loginUser) ;
    wp.daoTable = "sec_authority_log A join sec_window B on A.wf_winid =B.wf_winid";
    wp.whereOrder = " order by A.wf_winid " + commSqlStr.rownum((int) wp.itemNum("ex_apr_cnt"));
    setString("login_user", wp.loginUser);
    // select_noLimit();
    this.pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      errmsg("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  void selectTotCnt(String aWhere) {
    String sql1 = "select count(*) as xx_cnt"
        + " from sec_authority_log A join sec_window B on A.wf_winid =B.wf_winid" + aWhere
    // " where A.apr_flag <> 'Y' "
    // + sql_col(wp.item_ss("ex_group_id"), "A.group_id")
    // + sql_col(wp.item_ss("ex_user_level"), "A.user_level")
    // +sql_col(wp.loginUser,"A.crt_user","<>")
    ;

    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      wp.colSet("kk_tot_cnt", sqlNum("xx_cnt"));
    } else
      wp.colSet("kk_tot_cnt", 0);
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

    Secp2070Func func = new Secp2070Func();
    func.setConn(wp);

    String[] aaWinid = wp.itemBuff("wf_winid");
    String[] aaOpt = wp.itemBuff("opt");
    this.optNumKeep(aaWinid.length, "opt", "on_opt");

    wp.listCount[0] = wp.itemRows("wf_winid");

    func.dataCheck();
    if (func.isOK() == false) {
      alertErr2(func.getMsg());
      return;
    }

    int llOk = 0, llErr = 0;
    for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;
      optOkflag(rr);

      if (wp.colInt(rr, "db_same_dept") != 1) {
        optOkflag(rr, -1);
        llErr++;
        continue;
      }
      String lsUser = wp.itemStr(rr, "crt_user");
      if (!apprBankUnit(lsUser, wp.loginUser)) {
        optOkflag(rr, -1);
        continue;
      }

      func.iiRowNum = rr;
      rc = func.dataProc();
      sqlCommit(rc);
      optOkflag(rr, rc);
      if (rc == -1) {
        llErr++;
      } else {
        llOk++;
      }
    }

    alertMsg("覆核完成; OK=" + llOk + ", ERR=" + llErr);
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    wp.colSet("ex_apr_cnt", 300);
  }

}
