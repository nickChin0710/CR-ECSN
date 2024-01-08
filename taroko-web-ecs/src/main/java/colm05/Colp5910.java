package colm05;
/** 凍結、解凍、額度調整、強停例外覆核
 * 19-0617:   JH    p_xxx >>acno_p_xxx
2019-0408:  JH    act_dual_acno
109-05-06  V1.00.02  Tanwei       updated for project coding standard
 * */

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Colp5910 extends BaseProc {
 // String kk1, kk2;
  Colp5910Func func;
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
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
      if (eqIgno(wp.respHtml, "colp5910")) {
        wp.optionKey = wp.colStr(0, "ex_user");
        dddwList("dddw_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    String lsAcctKey = "";
    lsAcctKey = wp.itemStr("ex_key");
    lsAcctKey = commString.acctKey(lsAcctKey);
    if (!empty(lsAcctKey)) {
      if (lsAcctKey.length() != 11) {
        errmsg("身分證字號輸入錯誤");
        return;
      }
    }

    wp.whereStr = " where 1=1 and A.func_code='0800'" + sqlCol(wp.itemStr("ex_user"), "A.chg_user")
        + sqlCol(wp.itemStr("ex_key"), "A.acct_key", "like%")
        + sqlCol(wp.loginUser, "A.mod_user", "<>");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.*, hex(A.rowid) as rowid" + ", uf_acno_name(B.acno_p_seqno) as idno_name"
        + ", substr(A.aud_item,1,1) as ex_no_block" + ", substr(A.aud_item,2,1) as ex_no_unblock"
        + ", substr(A.aud_item,3,1) as ex_no_high" + ", substr(A.aud_item,4,1) as ex_no_low"
        + ", substr(A.aud_item,5,1) as ex_no_stop" + ", substr(A.aud_item,6,1) as ex_no_high_cash"
        + ", A.spec_reason as chg_reason"
        + ", uf_tt_idtab('COLM5910',A.spec_reason) as tt_chg_reason"
        + ", B.no_block_flag          as B_no_block_flag         "
        + ", B.no_block_s_date        as B_no_block_s_date       "
        + ", B.no_block_e_date        as B_no_block_e_date       "
        + ", B.no_unblock_flag        as B_no_unblock_flag       "
        + ", B.no_unblock_s_date      as B_no_unblock_s_date     "
        + ", B.no_unblock_e_date      as B_no_unblock_e_date     "
        + ", B.no_adj_loc_high        as B_no_adj_loc_high       "
        + ", B.no_adj_loc_high_s_date as B_no_adj_loc_high_s_date"
        + ", B.no_adj_loc_high_e_date as B_no_adj_loc_high_e_date"
        + ", B.no_adj_loc_low         as B_no_adj_loc_low        "
        + ", B.no_adj_loc_low_s_date  as B_no_adj_loc_low_s_date "
        + ", B.no_adj_loc_low_e_date  as B_no_adj_loc_low_e_date "
        + ", B.no_f_stop_flag         as B_no_f_stop_flag        "
        + ", B.no_f_stop_s_date       as B_no_f_stop_s_date      "
        + ", B.no_f_stop_e_date       as B_no_f_stop_e_date      "
        + ", B.no_adj_h_cash          as B_no_adj_h_cash         "
        + ", B.no_adj_h_s_date_cash   as B_no_adj_h_s_date_cash  "
        + ", B.no_adj_h_e_date_cash   as B_no_adj_h_e_date_cash  " + ", B.acno_p_seqno, B.p_seqno";
    wp.daoTable = "act_dual_acno A join act_acno B on B.acno_p_seqno=A.p_seqno";
    wp.whereOrder = " order by A.acct_key ";

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2(appMsg.errCondNodata);
      return;
    }
    listWkdata();

    wp.setPageValue();
  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "ex_no_block", "N")) {
        wp.colSet(ii, "no_block_flag", wp.colStr(ii, "B_no_block_flag"));
        wp.colSet(ii, "no_block_s_date", wp.colStr(ii, "B_no_block_s_date"));
        wp.colSet(ii, "no_block_e_date", wp.colStr(ii, "B_no_block_e_date"));
      }
      if (wp.colEq(ii, "ex_no_unblock", "N")) {
        wp.colSet(ii, "no_unblock_flag", wp.colStr(ii, "B_no_unblock_flag"));
        wp.colSet(ii, "no_unblock_s_date", wp.colStr(ii, "B_no_unblock_s_date"));
        wp.colSet(ii, "no_unblock_e_date", wp.colStr(ii, "B_no_unblock_e_date"));
      }
      if (wp.colEq(ii, "ex_no_high", "N")) {
        wp.colSet(ii, "no_adj_loc_high", wp.colStr(ii, "B_no_adj_loc_high"));
        wp.colSet(ii, "no_adj_loc_high_s_date", wp.colStr(ii, "B_no_adj_loc_high_s_date"));
        wp.colSet(ii, "no_adj_loc_high_e_date", wp.colStr(ii, "B_no_adj_loc_high_e_date"));
      }
      if (wp.colEq(ii, "ex_no_low", "N")) {
        wp.colSet(ii, "no_adj_loc_low", wp.colStr(ii, "B_no_adj_loc_low"));
        wp.colSet(ii, "no_adj_loc_low_s_date", wp.colStr(ii, "B_no_adj_loc_low_s_date"));
        wp.colSet(ii, "no_adj_loc_low_e_date", wp.colStr(ii, "B_no_adj_loc_low_e_date"));
      }
      if (wp.colEq(ii, "ex_no_high_cash", "N")) {
        wp.colSet(ii, "no_adj_h_cash", wp.colStr(ii, "B_no_adj_h_cash"));
        wp.colSet(ii, "no_adj_h_s_date_cash", wp.colStr(ii, "B_no_adj_h_s_date_cash"));
        wp.colSet(ii, "no_adj_h_e_date_cash", wp.colStr(ii, "B_no_adj_h_e_date_cash"));
      }
      if (wp.colEq(ii, "ex_no_stop", "N")) {
        wp.colSet(ii, "no_f_stop_flag", wp.colStr(ii, "B_no_f_stop_flag"));
        wp.colSet(ii, "no_f_stop_s_date", wp.colStr(ii, "B_no_f_stop_s_date"));
        wp.colSet(ii, "no_f_stop_e_date", wp.colStr(ii, "B_no_f_stop_e_date"));
      }
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
  public void dataProcess() throws Exception {
    func = new Colp5910Func();
    func.setConn(wp);
    String[] aaPseqno = wp.itemBuff("acno_p_seqno");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaPseqno.length;
    this.optNumKeep(aaPseqno.length, aaOpt);

    for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;

      wp.colSet(rr, "ok_flag", "-");

      func.vset("proc_row", rr);
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
    }

    // -re-Query-
    // queryRead();
    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
  }

}
