/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-30  V1.00.00             program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
* 112-01-04  V1.00.02  Zuwei Su   查詢時getWhereStr()被重複調用，order by被重複設置，新增存檔按鈕不可用        *
******************************************************************************/

package genm01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Genm0100 extends BaseProc {
  String mExStdVouchCd = "";
  String mExCurr = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      strAction = "S";
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 存檔 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("dddw_curr2", "<option value=''>--</option>");
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_std_vouch_cd")) == false) {
      wp.whereStr += " and  std_vouch_cd = :std_vouch_cd ";
      setString("std_vouch_cd", wp.itemStr("ex_std_vouch_cd"));
    }

    if (empty(wp.itemStr("ex_std_vouch_desc")) == false) {
      wp.whereStr += " and  std_vouch_desc like :ex_std_vouch_desc ";
      setString("ex_std_vouch_desc", wp.itemStr("%" + "ex_std_vouch_desc") + "%");
    }

    wp.whereStr += "group by std_vouch_cd,curr,std_vouch_desc ";
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " std_vouch_cd" + ", std_vouch_desc" + ", curr";

    wp.daoTable = "gen_std_vouch";
    wp.whereOrder = " order by std_vouch_cd,curr";
    getWhereStr();

    wp.pageCountSql = "select count(std_vouch_cd)  from ( " + " select std_vouch_cd,std_vouch_desc "
        + "   from gen_std_vouch " + wp.whereStr + ")";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    if (!empty(wp.itemStr("dddw_curr2"))) {
      wp.colSet("dddw_curr2", wp.itemStr("dddw_curr2"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    mExStdVouchCd = wp.itemStr("std_vouch_cd");
    mExCurr = wp.itemStr("curr");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExStdVouchCd = wp.itemStr("kk_std_vouch_cd");
    mExCurr = wp.itemStr("kk_curr");
    if (empty(mExStdVouchCd)) {
      mExStdVouchCd = itemKk("data_k1");
    }
    if (empty(mExCurr)) {
      mExCurr = itemKk("data_k3");
    }

    wp.selectSQL = "hex(s.rowid) as rowid, s.mod_seqno " + ", s.std_vouch_cd "
        + ", s.std_vouch_desc " + ", s.curr" + ", s.dbcr"
        + ", decode(a.ac_brief_name,'',a.ac_full_name,ac_brief_name) as db_brief" + ", s.dbcr_seq"
        + ", s.ac_no" + ", s.memo1" + ", s.memo2" + ", s.memo3"
        + ", decode(s.memo3_kind,'1','1:卡號','2','2:ID+身份證號碼','3','3:YYYYMMDDUBOO',s.memo3_kind) as memo3_kind "
        + ", s.mod_user" + ", s.mod_time" + ", s.mod_pgm" + ", s.mod_seqno";
    wp.daoTable = "gen_std_vouch s left join gen_acct_m a on s.ac_no = a.ac_no";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  s.std_vouch_cd = :std_vouch_cd ";
    setString("std_vouch_cd", mExStdVouchCd);
    wp.whereStr += " and  s.curr = :curr ";
    setString("curr", mExCurr);

    pageQuery();

    wp.setListCount(1);
    wp.notFound = "";

    if (sqlNotFind()) {
      alertErr("查無資料, std_vouch_cd=" + mExStdVouchCd + ":" + mExCurr);
    }
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
      this.btnUpdateOn(true);
    }
  }

  @Override
  public void dddwSelect() {
    try {

      if (wp.respHtml.indexOf("_detl") > 0) {
        // 幣別
        wp.initOption = "--";

        if (strAction.equals("new") || strAction.equals("S")) {
          wp.optionKey = empty(itemKk("data_k3")) ? "00" : itemKk("data_k3");
        } else if (strAction.equals("S2")) {
          wp.optionKey = wp.itemStr("kk_curr");
        }
        // wp.optionKey =
        // empty(item_kk("data_k3"))?"00":item_kk("data_k3");
      }

      dddwList("dddw_curr", "ptr_currcode", "curr_code_gl", "curr_chi_name",
          "where 1=1 group by curr_code_gl,curr_chi_name  order by curr_code_gl ");

      // 標準分錄代碼
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_std_vouch_cd");
      dddwList("dddw_ex_std_cd", "gen_std_vouch", "std_vouch_cd", "std_vouch_desc",
          "where 1=1 group by std_vouch_cd,std_vouch_desc order by std_vouch_cd ");

    } catch (Exception ex) {
    }
  }

  @Override
  public void dataProcess() throws Exception {

    wp.colSet("std_vouch_cd", wp.itemStr("kk_std_vouch_cd"));
    wp.colSet("std_vouch_desc", wp.itemStr("kk_std_vouch_desc"));
    String[] aaDbcr = wp.itemBuff("dbcr");
    String[] aaAcNo = wp.itemBuff("ac_no");
    String[] aaMemo1 = wp.itemBuff("memo1");
    String[] aaMemo2 = wp.itemBuff("memo2");
    String[] aaMemo3 = wp.itemBuff("memo3");
    String[] aaOpt = wp.itemBuff("opt");
    String lsSql = "";
    String lsMemo3Kind = "";
    wp.listCount[0] = aaDbcr.length;

    Genm0100Func func = new Genm0100Func(wp);
    if (strAction.equals("S2")) {

      if (empty(wp.itemStr("kk_std_vouch_desc"))) {
        alertMsg("請輸入標準分錄說明!!");
        return;
      }

      String[] aa_rowid = wp.itemBuff("rowid");
      // if (empty(aa_rowid[0])) {
      // // 檢查新增資料是否重複
      // ls_sql = "select count(*) as tot_cnt from gen_std_vouch where std_vouch_cd = ? ";
      // Object[] param = new Object[] { wp.item_ss("kk_std_vouch_cd") };
      // sqlSelect(ls_sql, param);
      // if (sql_num("tot_cnt") > 0) {
      // errmsg("此分錄代碼已被使用，無法新增");
      // return;
      // }
      // }

      // -check duplication-
      func.dbDelete();
      for (int ll = 0; ll < aaAcNo.length; ll++) {
        wp.colSet(ll, "ok_flag", "");
        if (checkBoxOptOn(ll, aaOpt)) {
          continue;
        }
        if (!empty(aaAcNo[ll])) {
          // ls_sql = "select count(*) as tot_cnt from gen_acct_m where ac_no = ? ";
          // Object[] param = new Object[] { aa_ac_no[ll] };
          // sqlSelect(ls_sql, param);
          // if (sql_num("tot_cnt") <= 0) {
          // errmsg("科目代號不存在!");
          // wp.col_set(ll, "ok_flag", "!");
          // return;
          // }

          lsSql = "select ac_no," + "ac_full_name, " + "memo3_flag, " + "memo3_kind "
              + "from gen_acct_m " + "where ac_no =:ac_no ";
          setString("ac_no", aaAcNo[ll]);
          sqlSelect(lsSql);
          if (sqlRowNum <= 0) {
            alertErr("科目代號不存在!");
            wp.colSet(ll, "ok_flag", "!");
            return;
          } else {
            lsMemo3Kind = sqlStr("memo3_kind");
            wp.colSet(ll, "db_brief", sqlStr("ac_brief_name"));
          }
        }
        func.varsSet("aa_dbcr", aaDbcr[ll]);
        func.varsSet("aa_dbcr_seq", String.valueOf(ll));
        func.varsSet("aa_ac_no", aaAcNo[ll]);
        func.varsSet("aa_memo1", aaMemo1[ll]);
        func.varsSet("aa_memo2", aaMemo2[ll]);
        func.varsSet("aa_memo3", aaMemo3[ll]);
        func.varsSet("aa_memo3_kind", lsMemo3Kind);
        if (func.dbInsert() != 1) {
          alertErr(func.getMsg());
          wp.colSet(ll, "ok_flag", "!");
          sqlCommit(0);
          return;
        }
      }

      // -delete no-approve-

      // if (rc < 0) {
      // alert_err(func.getMsg());
      // sql_commit(0);
      // return;
      // }else{
      // sql_commit(1);
      // }

      // -insert-
      // for (int ll = 0; ll < aa_dbcr.length; ll++) {
      // // -option-ON-
      // if (checkBox_opt_on(ll, aa_opt)) {
      // continue;
      // }
      //
      // }
      // sql_commit(1);
      alertMsg("資料存檔處理完成");

    }

  }

}
