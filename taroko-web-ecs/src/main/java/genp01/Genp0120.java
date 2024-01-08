/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-29  V1.00.00  David FU   program initial                            *
* 107-03-28  V1.00.01  ryan       update genp0120detl.html                   *
* 107-04-13  V1.00.02  Andy       update SQL CMD                             *
* 108-12-17  V1.00.03  Amber      update SQL								 *
* 109-04-21  V1.00.04  YangFang   updated for project coding standard 
* 111-08-18  V1.00.05  Machao     updated  查詢頁面bug處理       *
* 112-02-02  V1.00.06  Zuwei Su   查詢後, 頁數、筆數不符明細內容       *
* 112-09-27  V1.00.07  Zuwei Su   分列借貸方欄位        * 
* 112-12-11  V1.00.08  Zuwei Su   增gen_user_log寫檔        * 
******************************************************************************/

package genp01;



import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Genp0120 extends BaseProc {
  String mExDate1 = "";
  String mExDate2 = "";
  private static String progranCd = "GenA002";

  @Override
  public void initPage() {
    if (empty(strAction)) {
      wp.colSet("ex_date2", wp.sysDate);
    }
  }

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 取消或放行 */
      // is_action = "new";
      // clearFunc();
      changeState();
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
      // insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      // deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      dataProcess();
    }

    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 and decode(post_flag, '' , 'N', post_flag)='N'"
        + " and decode(jrn_status, '', '0', jrn_status) <> '1' "
        + " and decode(mod_log, '', '0', mod_log) not in ('D','U') ";
    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and  tx_date >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }
    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and  tx_date <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }

    if (wp.itemStr("ex_jrn_status").equals("1"))
      wp.whereStr += " and  decode(jrn_status, '', '2', jrn_status) = '2' ";
    else
      wp.whereStr += " and  decode(jrn_status, '', '2', jrn_status) = '3' ";

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "tx_date, " + "refno, " + "brno, " + "dept, " + "depno, " + "curr, "
        + "sum(amt)/2 as amt, " + "crt_user, "
        + "decode (jrn_status,'2','未放行','3','已放行',jrn_status) AS jrn_status, " + "ifrs_flag ";
    wp.daoTable = "gen_vouch";
    wp.whereOrder = " order by tx_date";
    getWhereStr();
    wp.whereStr += " group by tx_date,refno, brno,dept,depno,curr,crt_user,jrn_status,ifrs_flag ";
    wp.pageCountSql = "select count(*) from ( select "
            + wp.selectSQL
            + " from "
            + wp.daoTable
            + wp.whereStr
            + ") a";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  private void changeState() {}

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.showLogMessage("I", "", "It's dataRead()...");
    setSelectLimit(0);
    wp.pageControl();
    String txDate = itemKk("data_k1");
    String refno = itemKk("data_k2");
    String curr = itemKk("data_k3");
    wp.selectSQL =
        " hex (a.rowid) AS rowid, " + "a.tx_date, " + "a.refno, " + "a.curr, " + "a.seqno, "
                + "decode(a.dbcr,'C','貸','') db_dbcr1," 
                + "decode(a.dbcr,'D','借', '') db_dbcr2," 
            + "decode(a.dbcr,'C','貸','D','借') db_dbcr," + "a.ac_no, " + "a.amt, " + "a.memo1, "
            + "a.memo2, " + "a.memo3, " + "nvl(b.ac_brief_name,b.ac_full_name) as db_brief, "
            + "a.key_value, " + "a.id_no, " + "a.ifrs_flag ";

    // 20191218修改
    // + "a.brno, "
    // + "a.dept, "
    // + "a.depno, "
    // + "a.voucher_cnt, "
    // + "a.dbcr, "
    // + "a.sign_flag, "
    // + "a.crt_user, "
    // + "a.apr_user, "
    // + "a.jrn_status, "
    // + "a.post_flag, "
    // + "a.mod_user, "
    // + "a.mod_time, "
    // + "a.mod_pgm, "
    // + "a.mod_seqno, "
    // + "a.rowid, "
    // + "'0' db_optcode, "
    // + "lpad (' ', 30) db_brief, "
    // + "b.cr_flag as db_cr_flag, "
    // + "b.dr_flag as db_dr_flag, "
    // + "b.memo3_flag as db_memo3_flag, "
    // + "b.memo3_kind as db_memo3_kind, "
    // + "decode (b.brn_rpt_flag, 'Y', '1', '0') as db_insplist, "
    // + "b.brn_rpt_flag as db_brn_rpt_flag, "
    // + "a.sys_rem, "
    // + "'O' db_nocode, "
    // + "' ' db_old_memo_chg, "

    wp.daoTable = "gen_vouch a LEFT JOIN gen_acct_m b ON a.ac_no = b.ac_no ";
    wp.whereStr =
        " where 1=1 and a.tx_date = :tx_date " + " and a.refno = :refno " + " and a.curr = :curr ";
    setString("tx_date", txDate);
    setString("refno", refno);
    setString("curr", curr);

    wp.whereOrder = " order by a.seqno";

    wp.showLogMessage("I", "", "tx_date :" + txDate + " , refno :" + refno + " , curr :" + curr);
    // getWhereStr();

    pageQuery();

    // String ls_sql = "";
    // for (int ii = 0; ii < wp.selectCnt; ii++) {
    // ls_sql = "select ac_no, "
    // + "ac_brief_name, "
    // + "ac_full_name, "
    // + "memo3_flag, "
    // + "cr_flag, "
    // + "dr_flag, "
    // + "memo3_kind, "
    // + "brn_rpt_flag "
    // + "from gen_acct_m ";
    // ls_sql += "where 1=1 ";
    // ls_sql += sql_col(wp.col_ss(ii,"ac_no"), "ac_no");
    // sqlSelect(ls_sql);
    // if (sql_nrow > 0) {
    //
    // wp.col_set(ii, "db_brief",
    // empty(sql_ss("ac_brief_name"))?sql_ss("ac_full_name"):sql_ss("ac_brief_name"));
    // wp.col_set(ii, "db_memo3_flag", sql_ss("memo3_flag"));
    // wp.col_set(ii, "db_cr_flag", sql_ss("cr_flag"));
    // wp.col_set(ii, "db_dr_flag", sql_ss("dr_flag"));
    // wp.col_set(ii, "db_memo3_kind", sql_ss("memo3_kind"));
    // wp.col_set(ii, "db_brn_rpt_flag", sql_ss("brn_rpt_flag"));
    // }
    // if (sql_ss("brn_rpt_flag").equals("Y")) {
    // wp.col_set(ii, "db_insplist", "1");
    // } else {
    // wp.col_set(ii, "db_insplist", "0");
    // }
    // }

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.setPageValue();
  }


  @Override
  public void initButton() {}

  // void list_wkdata() throws Exception {
  //
  // }

  @Override
  public void dataProcess() throws Exception {

    int llOk = 0, llErr = 0;
    String[] aaOpt = wp.itemBuff("opt");
    String[] aaTxDate = wp.itemBuff("tx_date");
    String[] aaRefno = wp.itemBuff("refno");
    wp.listCount[0] = aaRefno.length;


    for (int ll = 0; ll < aaRefno.length; ll++) {

      if (checkBoxOptOn(ll, aaOpt)) {

        String sqlUp = " update gen_vouch set ";

        if (wp.itemStr("ex_jrn_status").equals("1")) {
          // 放行
          sqlUp += " jrn_status ='3', ";

        } else {
          // 放行取消
          sqlUp += " jrn_status ='2', ";
        }
        sqlUp += " mod_user =?, mod_time=sysdate, mod_pgm =?, ";
        sqlUp += "  mod_seqno =nvl(mod_seqno,0)+1 ";
        sqlUp += " where tx_date = ? ";
        sqlUp += " and refno = ? ";
        Object[] param =
            new Object[] {wp.loginUser, wp.itemStr("mod_pgm"), aaTxDate[ll], aaRefno[ll]};
        sqlExec(sqlUp, param);
        if (sqlRowNum <= 0) {
          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        } else {
          wp.colSet(ll, "ok_flag", "V");
          llOk++;
        }
      }
      
      // 增gen_user_log寫檔
      upateGenUseLog(wp.loginUser, progranCd);

      this.sqlCommit(rc);
      alertMsg("放行處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");
    }


  }
  
  /**
   * gen_user_log寫檔
   * 
   * @param exUser
   * @param progranCd
   */
  private void upateGenUseLog(String exUser, String progranCd) {
      // ========================================
      String sql = "update gen_user_log set " + " CRT_USER =? "
          + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
          + " where PROGRAN_CD = ?";
      Object[] param = new Object[] {exUser, wp.loginUser, wp.modPgm(), progranCd};
      sqlExec(sql, param);
  }
}
