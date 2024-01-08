/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-08-11  V1.00.02  JustinWu     即使查無資料也要進入參數明細維護  *
* 109-08-18  V1.00.03  JustinWu     hide 帳戶卡人等級, 帳戶PD Rating違約預測評等
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm02;
/**
 * 2019-1226   JH    apr_user, mod_user不可同一人
 * 108-12-12  V1.00.01  Alex       add err_desc                               *
 * */

import ofcapp.BaseAction;

public class Ptrp0470 extends BaseAction {
  String seqNo = "", lsDataType = "";

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
    if (this.chkStrend(wp.itemStr("ex_mod_date1"), wp.itemStr("ex_mod_date2")) == false) {
      alertErr2("異動日期 : 起迄錯誤 !");
      return;
    }

    String lsWhere = " where apr_flag <> 'Y' " + sqlCol(wp.itemStr("ex_vip_code"), "vip_code", ">=")
        + sqlCol(wp.itemStr("ex_mod_user"), "mod_user")
        + sqlCol(wp.itemStr("ex_mod_date1"), "to_char(mod_time,'yyyymmdd')", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "to_char(mod_time,'yyyymmdd')", "<=");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " vip_code , " + " seq_no , " + " vip_desc , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_user " + ", hex(rowid) as rowid";
    wp.daoTable = " ptr_vip_code ";
    wp.whereOrder = " order by vip_code ";
    logSql();
    pageQuery();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    seqNo = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(seqNo)) {
      seqNo = wp.itemStr("seq_no");
    }

    wp.selectSQL = "A.*, hex(rowid) as rowid," + "substrb(incl_cond,1,1) as db_incl_class_code,"
        + "substrb(incl_cond,2,1) as db_incl_pd_rate,"
        + "substrb(incl_cond,3,1) as db_incl_pay_rate,"
        + "substrb(incl_cond,4,1) as db_incl_card_since,"
        + "substrb(incl_cond,5,1) as db_incl_limit_amt,"
        + "substrb(incl_cond,6,1) as db_incl_purch_amt,"
        + "substrb(incl_cond,7,1) as db_incl_bank_rela," + "substrb(incl_cond,8,1) as db_incl_list,"
        + "substrb(incl_cond,9,1) as db_incl_group_code,"
        + "substrb(incl_cond,10,1) as db_incl_reason_down,"
        + "substrb(excl_cond,1,1) as db_excl_block," + "substrb(excl_cond,2,1) as db_excl_hi_risk,"
        + "substrb(excl_cond,3,1) as db_excl_overdue," + "substrb(excl_cond,4,1) as db_excl_rc_use,"
        + "substrb(excl_cond,5,1) as db_excl_pre_cash,"
        + "substrb(excl_cond,6,1) as db_excl_limit_use,"
        + "substrb(excl_cond,7,1) as db_excl_action_code ,"
        + "substrb(excl_cond,8,1) as db_excl_list," + "substrb(excl_cond,9,1) as db_excl_exblock" //
    ;
    wp.daoTable = "ptr_vip_code A";
    wp.whereStr = " where 1=1 and apr_flag ='N' " + sqlCol(seqNo, "seq_no")

    ;
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + seqNo);
      return;
    }
    // --
    dataReadAfter();
  }

  void dataReadAfter() {
    // --
    String sql1 = "select sum(decode(data_type,'I01',1,0)) as cnt_I01,"
        + " sum(decode(data_type,'I02',1,0)) as cnt_I02,"
        + " sum(decode(data_type,'I03',1,0)) as cnt_I03,"
        + " sum(decode(data_type,'I04',1,0)) as cnt_I04,"
        + " sum(decode(data_type,'I05',1,0)) as cnt_I05,"
        + " sum(decode(data_type,'I06',1,0)) as cnt_I06,"
        + " sum(decode(data_type,'I07',1,0)) as cnt_I07,"
        + " sum(decode(data_type,'I08',1,0)) as cnt_I08,"
        + " sum(decode(data_type,'I09',1,0)) as cnt_I09,"
        + " sum(decode(data_type,'E01',1,0)) as cnt_E01,"
        + " sum(decode(data_type,'E02',1,0)) as cnt_E02,"
        + " sum(decode(data_type,'E03',1,0)) as cnt_E03,"
        + " sum(decode(data_type,'E04',1,0)) as cnt_E04,"
        + " sum(decode(data_type,'E05',1,0)) as cnt_E05," + " count(*) as db_cnt"
        + " from ptr_vip_data" + " where 1=1 and apr_flag ='N' " + sqlCol(seqNo, "seq_no");
    this.sqlSelect(sql1);
    wp.colSet("cnt_I01", this.sqlInt("cnt_I01"));
    wp.colSet("cnt_I02", this.sqlInt("cnt_I02"));
    wp.colSet("cnt_I03", this.sqlInt("cnt_I03"));
    wp.colSet("cnt_I04", this.sqlInt("cnt_I04"));
    wp.colSet("cnt_I05", this.sqlInt("cnt_I05"));
    wp.colSet("cnt_I06", this.sqlInt("cnt_I06"));
    wp.colSet("cnt_I07", this.sqlInt("cnt_I07"));
    wp.colSet("cnt_I08", this.sqlInt("cnt_I08"));
    wp.colSet("cnt_I09", this.sqlInt("cnt_I09"));
    // --
    wp.colSet("cnt_E01", this.sqlInt("cnt_E01"));
    wp.colSet("cnt_E02", this.sqlInt("cnt_E02"));
    wp.colSet("cnt_E03", this.sqlInt("cnt_E03"));
    wp.colSet("cnt_E04", this.sqlInt("cnt_E04"));
    wp.colSet("cnt_E05", this.sqlInt("cnt_E05"));
  }

  void dataReadDetl() throws Exception {
    resetType();
    seqNo = wp.itemStr("data_k1");
    lsDataType = wp.itemStr("data_k2");
    if (eqIgno(lsDataType, "1")) {
      lsDataType = "I01";
      wp.colSet("title_type", "卡人等級");
      wp.colSet("type1", "disabled");
    } else if (eqIgno(lsDataType, "2")) {
      lsDataType = "I02";
      wp.colSet("title_type", "近 N 個月違約預測評等");
      wp.colSet("type2", "disabled");
    } else if (eqIgno(lsDataType, "3")) {
      lsDataType = "I03";
      wp.colSet("title_type", "近期違約預測評等");
      wp.colSet("type3", "disabled");
    } else if (eqIgno(lsDataType, "4")) {
      lsDataType = "I04";
      wp.colSet("title_type", "近 N 個月繳款評等");
      wp.colSet("type4", "disabled");
    } else if (eqIgno(lsDataType, "5")) {
      lsDataType = "I05";
      wp.colSet("title_type", "近期繳款評等");
      wp.colSet("type5", "disabled");
    } else if (eqIgno(lsDataType, "6")) {
      lsDataType = "I06";
      wp.colSet("title_type", "指定名單");
      wp.colSet("type6", "disabled");
    } else if (eqIgno(lsDataType, "7")) {
      lsDataType = "I07";
      wp.colSet("title_type", "指定-團體代號");
      wp.colSet("type7", "disabled");
    } else if (eqIgno(lsDataType, "8")) {
      lsDataType = "I09";
      wp.colSet("title_type", "指定調降額度理由碼");
      wp.colSet("type8", "disabled");
    } else if (eqIgno(lsDataType, "9")) {
      lsDataType = "E01";
      wp.colSet("title_type", "凍結碼 ");
      wp.colSet("type9", "disabled");
    } else if (eqIgno(lsDataType, "10")) {
      lsDataType = "E02";
      wp.colSet("title_type", "風險分類 ");
      wp.colSet("type10", "disabled");
    } else if (eqIgno(lsDataType, "11")) {
      lsDataType = "E03";
      wp.colSet("title_type", "action_code ");
      wp.colSet("type11", "disabled");
    } else if (eqIgno(lsDataType, "12")) {
      lsDataType = "E04";
      wp.colSet("title_type", "排除名單 ");
      wp.colSet("type12", "disabled");
    } else if (eqIgno(lsDataType, "13")) {
      lsDataType = "E05";
      wp.colSet("title_type", "例外凍結碼 ");
      wp.colSet("type13", "disabled");
    }

    if (empty(seqNo))
      seqNo = wp.itemStr("seq_no");
    if (empty(lsDataType)) {
//    	// hide 帳戶卡人等級, 帳戶PD Rating違約預測評等
//      lsDataType = "I01";
//      wp.colSet("title_type", "卡人等級");
//      wp.colSet("type1", "disabled");
    	lsDataType = "I04";
        wp.colSet("title_type", "近 N 個月繳款評等");
        wp.colSet("type4", "disabled");
    }
    wp.colSet("data_type", lsDataType);

    wp.selectSQL = " data_code ";
    wp.daoTable = " ptr_vip_data ";
    wp.whereStr  = " where 1=1 " 
                               + " and table_name = 'PTR_VIP_CODE' " 
    		                   + " and apr_flag = 'N' "
        + sqlCol(seqNo, "seq_no") + sqlCol(lsDataType, "data_type");
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    logSql();
    pageQuery();
    wp.notFound = "N"; //即使查無資料(wp.notFound="Y")，也要進入下一個畫面，因此設定wp.notFound = "N"
    wp.setListCount(1);

  }

  void resetType() {
    wp.colSet("type1", "class=btAdd_detl");
    wp.colSet("type2", "class=btAdd_detl");
    wp.colSet("type3", "class=btAdd_detl");
    wp.colSet("type4", "class=btAdd_detl");
    wp.colSet("type5", "class=btAdd_detl");
    wp.colSet("type6", "class=btAdd_detl");
    wp.colSet("type7", "class=btAdd_detl");
    wp.colSet("type8", "class=btAdd_detl");
    wp.colSet("type9", "class=btAdd_detl");
    wp.colSet("type10", "class=btAdd_detl");
    wp.colSet("type11", "class=btAdd_detl");
    wp.colSet("type12", "class=btAdd_detl");
    wp.colSet("type13", "class=btAdd_detl");
  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;


    ptrm02.Ptrp0470Func func = new ptrm02.Ptrp0470Func();
    func.setConn(wp);

    String[] lsSeqNo = wp.itemBuff("seq_no");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("rowid");
    int rr = optToIndex(aaOpt[0]);
    if (rr < 0) {
      alertErr("請點選覆核資料");
      return;
    }

    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;

      optOkflag(rr);
      String lsChgUser = wp.itemStr(rr, "mod_user");
      if (eqIgno(lsChgUser, wp.loginUser)) {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "err_desc", "覆核及異動經辦不可同一人");
        continue;
      }

      func.varsSet("seq_no", lsSeqNo[rr]);
      if (func.dataProc() == 1) {
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
        continue;
      } else {
        dbRollback();
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        wp.colSet(rr, "err_desc", func.getMsg());
        continue;
      }
    }

    if (llOk > 0)
      sqlCommit(1);

    alertMsg("覆核完成, 成功:" + llOk + " , 失敗:" + llErr);

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
