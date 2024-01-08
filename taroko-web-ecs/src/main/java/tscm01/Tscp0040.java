/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-15  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-05-15  V1.00.02  Andy		  update : DeBug                             * 
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        *
 * 109-12-30  V1.00.04  shiyuqi       修改无意义命名                                                                                     *
 * 110-01-22  V1.00.05  Justin          fix sql injection problems
 * 111-04-14  V1.00.06  machao     TSC畫面整合
******************************************************************************/

package tscm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Tscp0040 extends BaseEdit {
  String mExCardType = "";
  String mExGroupCode = "";
  String mExTscBinNo = "";

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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      saveFunc();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    wp.whereStr = "";
    String exCardNo = wp.itemStr("ex_card_no");
    String exTscCardNo = wp.itemStr("ex_tsc_card_no");
    String exDate1 = wp.itemStr("ex_date1");
    String exCharge = wp.itemStr("ex_charge");
    wp.whereStr = " where 1=1 ";
    // 固定條件

    // 自鍵條件
    if (empty(exCardNo) && empty(exTscCardNo) && empty(exDate1)) {
      alertErr("請至少輸入一項查詢條件!!");
      return false;
    }
    if (empty(exCardNo) == false) {
      wp.whereStr += sqlCol(exCardNo, "card_no");
    }
    if (empty(exTscCardNo) == false) {
      wp.whereStr += sqlCol(exTscCardNo, "tsc_card_no");
    }
    if (empty(exDate1) == false) {
      wp.whereStr += sqlCol(exDate1, "create_date");
    }
    switch (exCharge) {
      case "1":
        wp.whereStr += "and appr_date ='' ";
        break;
      case "2":
        wp.whereStr += "and appr_date !='' ";
        break;
    }

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

    wp.selectSQL = "hex (rowid) AS rowid, " + "tsc_card_no, " + "card_no, " + "create_date, "
        + "balance_date_plan, " + "balance_date, " + "balance_date_rtn, " + "appr_user, "
        + "appr_date, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno, "
        + "emboss_kind ";
    wp.daoTable = "tsc_btrq_log";
    wp.whereOrder = " order by create_date,tsc_card_no,card_no ";
    if (getWhereStr() == false)
      return;
    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
    // + wp.whereStr + wp.whereOrder);
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }

  void listWkdata() throws Exception {
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      String embossKind = wp.colStr(ii, "emboss_kind");
      String[] cde1 = new String[] {"E", "1", "2", "3", "4", "5", "6", "7", "8",};
      String[] txt1 = new String[] {"毀損補發", "申請停卡", "掛失", "強制停卡", "效期屆滿且續卡", "偽卡", "效期屆滿且不續卡",
          "爭議，不可歸責", "系統進行餘額轉置"};
      wp.colSet(ii, "db_emboss_kind", commString.decode(embossKind, cde1, txt1));
    }
  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_pp_card_no = wp.item_ss("pp_card_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    // update main table [crd_emap_tmp] use
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] opt = wp.itemBuff("opt");

    String[] aaTscCardNo = wp.itemBuff("tsc_card_no");
    String[] aaApprDate = wp.itemBuff("appr_date");
    String[] aaApprUser = wp.itemBuff("appr_user");

    String mRowid = "", mModSeqno = "";
    //
    wp.listCount[0] = aaTscCardNo.length;
    String lsSql = "", lsNewEndDate = "";
    int rr = -1;
    int llOk = 0, llErr = 0;
    // 勾選刪除資料處理
    // for (int ii = 0; ii < opt.length; ii++) {
    // rr = (int) this.to_Num(opt[ii]) - 1;
    // if (rr < 0) {
    // continue;
    // }
    // }
    // check
    for (int ii = 0; ii < aaTscCardNo.length; ii++) {
      mRowid = aaRowid[ii];
      mModSeqno = aaModSeqno[ii];
      if (!checkBoxOptOn(ii, opt))
        continue;
      if (checkBoxOptOn(ii, opt)) {
        if (!empty(aaApprUser[ii])) {
          llErr++;
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "已放行資料不可再做放行");
          continue;
        }
        String usSql = "update tsc_btrq_log set " + "appr_user =:appr_user , "
            + "appr_date =:appr_date, " + "mod_user =:mod_user, " + "mod_time = sysdate, "
            + "mod_pgm = 'tscp0040', " + "mod_seqno =nvl(mod_seqno,0)+1 " + "where 1=1 "
            + "and hex(rowid) =:rowid " + "and mod_seqno =:mod_seqno";
        setString("appr_user", wp.loginUser);
        setString("appr_date", getSysDate());
        setString("mod_user", wp.loginUser);
        setString("rowid", mRowid);
        setString("mod_seqno", mModSeqno);
        sqlExec(usSql);
        if (sqlRowNum <= 0) {
          llErr++;
          wp.colSet(ii, "ok_flag", "X");
          wp.colSet(ii, "err_msg", "資料更新失敗");
        } else {
          llOk++;
          wp.colSet(ii, "ok_flag", "V");
        }
      }
    }
    if (llErr > 0) {
      sqlCommit(0);
      alertMsg("放行失敗!!");
    } else {
      sqlCommit(1);
      alertMsg("放行成功!!");
    }

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("group_code");
      // this.dddw_list("dddw_group_code", "ptr_group_code", "group_code",
      // "group_name", " where 1=1 order by group_code");
    } catch (Exception ex) {
    }
  }

}
