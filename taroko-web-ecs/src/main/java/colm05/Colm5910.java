/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package colm05;
/**凍結、解凍、額度調整、強停例外維護
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 2019-0408:     JH    act_dual_acno
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Colm5910 extends BaseEdit {
  Colm5910Func func = null;
  String acctKey = "", dataKK2 = "";

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
      // clearFunc();
      // read_ctfg_mast();
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
      // dataRead();
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
    // listTab_proc();

  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("5910") > 0) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (wp.respHtml.indexOf("5910") > 0) {
        wp.optionKey = wp.colStr("chg_reason");
        dddwList("dddw_chg_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='COLM5910'");
      }
    } catch (Exception ex) {
    }
  }


  @Override
  public void queryFunc() throws Exception {

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
    String lsAcctKey = "", lsAcctType = "";
    lsAcctType = wp.itemStr("ex_acct_type");
    lsAcctKey = wp.itemStr("ex_acct_key");
    lsAcctKey = commString.acctKey(lsAcctKey);

    if (empty(lsAcctKey)) {
      alertErr2("帳務帳號:輸入錯誤");
      return;
    }

    if (lsAcctKey.length() != 11) {
      alertErr2("帳戶帳號:輸入錯誤");
      return;
    }
    wp.selectSQL = "hex(A.rowid) as rowid, A.mod_seqno, " + "A.acno_p_seqno, A.p_seqno,   "
        + "A.acct_type, " + "A.acct_key, " + "uf_acno_name(A.p_seqno) as chi_name,"
        + "A.acct_status," + "A.no_block_flag ," + "A.no_block_s_date ," + "A.no_block_e_date ,"
        + "A.no_unblock_flag ," + "A.no_unblock_s_date ," + "A.no_unblock_e_date ,"
        + "A.no_adj_loc_high ," + "A.no_adj_loc_high_s_date ," + "A.no_adj_loc_high_e_date ,"
        + "A.no_adj_loc_low ," + "A.no_adj_loc_low_s_date ," + "A.no_adj_loc_low_e_date ,"
        + "A.no_f_stop_flag ," + "A.no_f_stop_s_date ," + "A.no_f_stop_e_date ,"
        + "A.no_adj_h_cash ," + "A.no_adj_h_s_date_cash ," + "A.no_adj_h_e_date_cash ,"
        + "B.spec_reason as chg_reason," + "B.spec_remark as chg_remark,"
        + " uf_tt_acct_type(A.acct_type) as tt_acct_type," + "'N' as ex_dual_flag"
        + ", 'N' as ex_no_block" + ", 'N' as ex_no_unblock" + ", 'N' as ex_no_high"
        + ", 'N' as ex_no_low" + ", 'N' as ex_no_stop" + ", 'N' as ex_no_high_cash"
        + ", to_char(sysdate,'yyyymmdd') as sys_date ";
    wp.daoTable = "act_acno A left join act_acno_ext B on B.p_seqno=A.acno_p_seqno ";
    wp.whereStr =
        " where 1=1" + sqlCol(lsAcctKey, "A.acct_key") + sqlCol(lsAcctType, "A.acct_type");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + acctKey);
      wp.colSet("acct_type", "");
      wp.colClear(0, "acct_key");
      wp.colClear(0, "p_seqno");
      wp.colClear(0, "acct_p_seqno");
      return;
    }

    selectActDualAcno();
  }

  void selectActDualAcno() {

    wp.sqlCmd = "select A.*, hex(A.rowid) as rowid" + ", A.spec_reason as chg_reason"
        + ", substr(A.aud_item,1,1) as ex_no_block" + ", substr(A.aud_item,2,1) as ex_no_unblock"
        + ", substr(A.aud_item,3,1) as ex_no_high" + ", substr(A.aud_item,4,1) as ex_no_low"
        + ", substr(A.aud_item,5,1) as ex_no_stop" + ", substr(A.aud_item,6,1) as ex_no_high_cash"
        + ", chg_date as mod_date" + " from act_dual_acno A" + " where 1=1"
        + sqlCol(wp.colStr("p_seqno"), "A.p_seqno") + " and A.func_code ='0800'";
    sqlSelect(wp.sqlCmd);
    if (sqlRowNum <= 0) {
      return;
    }
    wp.colSet("ex_dual_flag", "Y");
    alertMsg("此筆資料未覆核.....");
    if (eqIgno(sqlStr("ex_no_block"), "Y")) {
      wp.colSet("ex_no_block", "Y");
      wp.colSet("no_block_flag", sqlStr("no_block_flag"));
      wp.colSet("no_block_s_date", sqlStr("no_block_s_date"));
      wp.colSet("no_block_e_date", sqlStr("no_block_e_date"));
    }
    if (eqIgno(sqlStr("ex_no_unblock"), "Y")) {
      wp.colSet("ex_no_unblock", "Y");
      wp.colSet("no_unblock_flag", sqlStr("no_unblock_flag"));
      wp.colSet("no_unblock_s_date", sqlStr("no_unblock_s_date"));
      wp.colSet("no_unblock_e_date", sqlStr("no_unblock_e_date"));
    }
    if (eqIgno(sqlStr("ex_no_high"), "Y")) {
      wp.colSet("ex_no_high", "Y");
      wp.colSet("no_adj_loc_high", sqlStr("no_adj_loc_high"));
      wp.colSet("no_adj_loc_high_s_date", sqlStr("no_adj_loc_high_s_date"));
      wp.colSet("no_adj_loc_high_e_date", sqlStr("no_adj_loc_high_e_date"));
    }
    if (eqIgno(sqlStr("ex_no_low"), "Y")) {
      wp.colSet("ex_no_low", "Y");
      wp.colSet("no_adj_loc_low", sqlStr("no_adj_loc_low"));
      wp.colSet("no_adj_loc_low_s_date", sqlStr("no_adj_loc_low_s_date"));
      wp.colSet("no_adj_loc_low_e_date", sqlStr("no_adj_loc_low_e_date"));
    }
    if (eqIgno(sqlStr("ex_no_stop"), "Y")) {
      wp.colSet("ex_no_stop", "Y");
      wp.colSet("no_f_stop_flag", sqlStr("no_f_stop_flag"));
      wp.colSet("no_f_stop_s_date", sqlStr("no_f_stop_s_date"));
      wp.colSet("no_f_stop_e_date", sqlStr("no_f_stop_e_date"));
    }
    if (eqIgno(sqlStr("ex_no_high_cash"), "Y")) {
      wp.colSet("ex_no_high_cash", "Y");
      wp.colSet("no_adj_h_cash", sqlStr("no_adj_h_cash"));
      wp.colSet("no_adj_h_s_date_cash", sqlStr("no_adj_h_s_date_cash"));
      wp.colSet("no_adj_h_e_date_cash", sqlStr("no_adj_h_e_date_cash"));
    }
    // --
    wp.colSet("chg_reason", sqlStr("chg_reason"));
    wp.colSet("chg_remark", sqlStr("chg_remark"));
    wp.colSet("mod_date", sqlStr("chg_date"));
    wp.colSet("mod_user", sqlStr("chg_user"));
    wp.colSet("mod_seqno", sqlStr("mod_seqno"));
  }

  @Override
  public void saveFunc() throws Exception {
    func = new colm05.Colm5910Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);

    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (rc == 1 && this.isUpdate())
      dataRead();
  }

  @Override
  public void initButton() {
    if (eqIgno(wp.colStr("ex_dual_flag"), "N")) {
      this.btnOnAud(false, true, false);
    } else if (eqIgno(wp.colStr("ex_dual_flag"), "Y")) {
      this.btnOnAud(false, true, true);
    } else {
      this.btnOnAud(false, false, false);
    }
  }

}
