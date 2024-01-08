/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-04-17  V1.00.01  Alex       新增VD悠遊卡問題交易結案主管覆核程式                                          *
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;

public class Tscp2270 extends BaseAction {

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
      if (eqIgno(wp.respHtml, "tscp2270")) {
        this.ddlbList("ddlb_close_reason", wp.colStr("ex_close_reason"),
            "ecsfunc.DeCodeTscc.closeReason");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_tran_date1"), wp.itemStr("ex_tran_date2")) == false) {
      alertErr2("交易日期起迄：輸入錯誤");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_close_date1"), wp.itemStr("ex_close_date2")) == false) {
      alertErr2("結案日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 and close_date <>'' and apr_date='' "
        + sqlCol(wp.itemStr("ex_tran_date1"), "tran_date", ">=")
        + sqlCol(wp.itemStr("ex_tran_date2"), "tran_date", "<=")
        + sqlCol(wp.itemStr("ex_close_date1"), "close_date", ">=")
        + sqlCol(wp.itemStr("ex_close_date2"), "close_date", "<=")
        + sqlCol(wp.itemStr("ex_close_reason"), "close_reason")
        + sqlCol(wp.itemStr("ex_close_user"), "close_user");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " tsc_card_no , " + " tran_date , " + " tran_time , " + " tran_amt , "
        + " traff_code , " + " place_code , " + " traff_subname , " + " place_subname , "
        + " close_reason , " + " close_remark , " + " close_user , " + " close_date , "
        + " notify_date , " + " online_mark , " + " hex(rowid) as rowid , mod_seqno "

    ;
    wp.daoTable = "tsc_dcpr_log ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by crt_date, crt_time ";
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
    int llOk = 0, llErr = 0, llAmt = 0, llCnt = 0;
    int ii = 0;
    Tscp2270Func func = new Tscp2270Func();
    func.setConn(wp);
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsTscCardNo = wp.itemBuff("tsc_card_no");
    String[] lsTranDate = wp.itemBuff("tran_date");
    String[] lsTranTime = wp.itemBuff("tran_time");
    String[] lsTranAmt = wp.itemBuff("tran_amt");
    String[] lsTraffCode = wp.itemBuff("traff_code");
    String[] lsPlaceCode = wp.itemBuff("place_code");
    String[] lsTraffSubname = wp.itemBuff("traff_subname");
    String[] lsPlaceSubname = wp.itemBuff("place_subname");
    String[] lsCloseReason = wp.itemBuff("close_reason");
    String[] lsCloseRemark = wp.itemBuff("close_remark");
    String[] lsCloseUser = wp.itemBuff("close_user");
    String[] lsCloseDate = wp.itemBuff("close_date");
    String[] lsNotifyDate = wp.itemBuff("notify_date");
    String[] lsOnlineMark = wp.itemBuff("online_mark");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] liModSeqno = wp.itemBuff("mod_seqno");
    wp.listCount[0] = lsRowid.length;


    for (int rr = 0; rr < lsRowid.length; rr++) {
      if (!checkBoxOptOn(rr, aaOpt)) {
        continue;
      }
      llCnt++;
      func.varsSet("tsc_card_no", lsTscCardNo[rr]);
      func.varsSet("tran_date", lsTranDate[rr]);
      func.varsSet("tran_time", lsTranTime[rr]);
      func.varsSet("tran_amt", lsTranAmt[rr]);
      func.varsSet("traff_code", lsTraffCode[rr]);
      func.varsSet("place_code", lsPlaceCode[rr]);
      func.varsSet("traff_subname", lsTraffSubname[rr]);
      func.varsSet("place_subname", lsPlaceSubname[rr]);
      func.varsSet("close_reason", lsCloseReason[rr]);
      func.varsSet("close_remark", lsCloseRemark[rr]);
      func.varsSet("close_user", lsCloseUser[rr]);
      func.varsSet("close_date", lsCloseDate[rr]);
      func.varsSet("notify_date", lsNotifyDate[rr]);
      func.varsSet("online_mark", lsOnlineMark[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("mod_seqno", liModSeqno[rr]);
      func.varsSet("seq_no", "" + rr + 1);
      if (func.dataProc() == 1) {
        llOk++;
        llAmt += Integer.parseInt(lsTranAmt[rr]);
        wp.colSet(rr, "ok_flag", "V");
        sqlCommit(1);
        continue;
      } else {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        this.dbRollback();
        continue;
      }
    }

    if (llCnt == 0) {
      alertErr2("請勾選要覆核資料");
      return;
    }

    if (llOk > 0) {
      func.varsSet("ll_ok", "" + llOk);
      func.varsSet("tot_amt", "" + llAmt);
      rc = func.insertBilPostcntl();
      sqlCommit(rc);
    }


    alertMsg("資料覆核處理完成 , 成功 :" + llOk + " 失敗:" + llErr);


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
