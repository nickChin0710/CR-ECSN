/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package colq01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colq1230 extends BaseEdit {
  CommString commString = new CommString();

  String tscCardNo = "";

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

  private boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exCrtDate1");
    String lsDate2 = wp.itemStr("exCrtDate2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔日期-起迄]  輸入錯誤");
      return false;
    }

    wp.whereStr = "where 1=1 ";
    // if (empty(wp.item_ss("exCrtDate1"))==false) {
    // wp.whereStr +=" and crt_date >='"+wp.item_ss("exCrtDate1")+"' ";
    // }
    // if (empty(wp.item_ss("exCrtDate2"))==false) {
    // wp.whereStr +=" and crt_date <='"+wp.item_ss("exCrtDate2")+"' ";
    // }
    if (wp.itemStr("exSendFlag").equals("0") == false) {
      wp.whereStr += " and send_flag ='" + wp.itemStr("exSendFlag") + "' ";
    }
    wp.whereStr += sqlCol(wp.itemStr("exIdCorpNo"), "id_corp_no", "like%")
        + sqlCol(wp.itemStr("exCloseReason"), "close_reason")
        + sqlCol(wp.itemStr("exSendYm"), "send_ym") + sqlStrend(lsDate1, lsDate2, "crt_date")
        + sqlCol(wp.itemStr("exAudCode"), "aud_code");
    // + sql_col(ls_userid, "add_user", "like%")
    // + " and misc_status ='10'";

    wp.whereOrder = " order by lgd_seqno";
    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "lgd_seqno, " + "id_corp_no, " + "id_corp_type, " + "aud_code, "
    // + "deCode(aud_code,'A','新增','C','修改','D','刪除','') AS tt_aud_code, "
        + "send_ym, " + "early_ym, " + "risk_amt, " + "overdue_ym, " + "overdue_amt, " + "coll_ym, "
        + "coll_amt, " + "recv_self_amt, " + "recv_rela_amt, " + "recv_oth_amt, " + "costs_amt, "
        + "costs_ym, " + "revol_rate, " + "card_rela_type, " + "close_reason, "
        // + "deCode(close_reason,'A1','借戶治癒','A2','協議後正常還款','B2','其他違約滿兩年,回收無望案件','') AS
        // tt_close_reason, "
        + "send_date, " + "from_type, "
        // + "deCode(from_type,'1','人工','2','批次','') AS tt_from_type, "
        + "crt_user, " + "crt_date ";

    wp.daoTable = "col_lgd_902";

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
    String[] cde = new String[] {"A1", "A2", "B2"};
    String[] txt = new String[] {"借戶治癒", "協議後正常還款", "其他違約滿兩年,回收無望案件"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "aud_code");
      wp.colSet(ii, "tt_aud_code", commString.decode(wkData, ",A,C,D", ",新增,修改,刪除"));

      // cde=new String[]{"A1","A2","B2"};
      // txt=new String[]{"借戶治癒","協議後正常還款","其他違約滿兩年,回收無望案件"};
      wkData = wp.colStr(ii, "close_reason");
      wp.colSet(ii, "tt_close_reason", commString.decode(wkData, cde, txt));

      wkData = wp.colStr(ii, "from_type");
      wp.colSet(ii, "tt_from_type", commString.decode(wkData, ",1,2", ",人工,批次"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    tscCardNo = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(tscCardNo)) {
      tscCardNo = itemKk("tsc_card_no");
    }

    if (isEmpty(tscCardNo)) {
      alertErr("TSC卡號 : 不可空白");
      return;
    }

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno, " + "tsc_card_no, " + "card_no, " + "new_beg_date, "
            + "new_end_date, " + "crt_date, " + "mod_user, " + "uf_2ymd(mod_time) as mod_date ";

    wp.daoTable = "tsc_card";
    wp.whereStr = "where 1=1" + sqlCol(tscCardNo, "tsc_card_no");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + tscCardNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    /*
     * func =new busi.col01.Colq1230_func(wp);
     * 
     * rc = func.dbSave(is_action); ddd(func.getMsg()); if (rc!=1) { err_alert(func.getMsg()); }
     * this.sql_commit(rc);
     */
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

}
