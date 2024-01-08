/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 110-01-05  V1.00.01  Tanwei       zz開頭變量修改      *
******************************************************************************/
package tscm01;

import ofcapp.BaseAction;

public class Tscp2260 extends BaseAction {
  busi.CommBusi commBusi = new busi.CommBusi();

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 and send_flag ='1' and bill_type=:bill_type "
        + sqlCol(wp.itemStr("ex_send_date"), "fst_send_date");
    setString("bill_type", commBusi.TSCC_bill_type);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " reference_no , " + " reference_seq , " + " bin_type , " + " ctrl_seqno , "
        + " ctrl_seqno2 , " + " card_no , " + " purchase_date , " + " fst_reason_code , "
        + " fst_twd_amt , " + " fst_send_date , " + " fst_send_cnt , " + " send_flag , "
        + " hex(rowid) as rowid , " + " '' as db_tsc_card , " + " '' as db_purch_date , "
        + " '' as db_purch_time , " + " '' as db_traf_name , " + " '' as db_traf_code , "
        + " '' as db_place_code , " + " '' as db_place_name , " + " '' as db_online_mark ,"
        + " '0' as err ";
    wp.daoTable = "rsk_chgback ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    logSql();
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    querryAfter();
    wp.setListCount(1);
    wp.setPageValue();

  }

  void querryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String sql1 = "select tsc_card_no as db_tsc_card , " + " purchase_date as db_purch_date , "
          + " purchase_time as db_purch_time , " + " traffic_cd as db_traf_code , "
          + " traffic_abbr as db_traf_name , " + " addr_cd as db_place_code , "
          + " addr_abbr as db_place_name ," + " online_mark as db_online_mark "
          + " from tsc_cgec_all " + " where reference_no =? ";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "reference_no")});
      if (sqlRowNum <= 0) {
        wp.colSet(ii, "err", "1");
      } else {
        wp.colSet(ii, "db_tsc_card", sqlStr("db_tsc_card"));
        wp.colSet(ii, "db_purch_date", sqlStr("db_purch_date"));
        wp.colSet(ii, "db_purch_time", sqlStr("db_purch_time"));
        wp.colSet(ii, "db_traf_name", sqlStr("db_traf_name"));
        wp.colSet(ii, "db_traf_code", sqlStr("db_traf_code"));
        wp.colSet(ii, "db_place_code", sqlStr("db_place_code"));
        wp.colSet(ii, "db_place_name", sqlStr("db_place_name"));
        wp.colSet(ii, "db_online_mark", sqlStr("db_online_mark"));
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
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0, llCnt = 0;
    int ii = 0;
    Tscp2260Func func = new Tscp2260Func();
    func.setConn(wp);

    String[] aaOpt = wp.itemBuff("opt");
    String[] liErr = wp.itemBuff("err");
    String[] lsCardNo = wp.itemBuff("card_no");
    String[] lsTscCard = wp.itemBuff("db_tsc_card");
    String[] lsPurchDate = wp.itemBuff("db_purch_date");
    String[] lsPurchTime = wp.itemBuff("db_purch_time");
    String[] lsFstTwdAmt = wp.itemBuff("fst_twd_amt");
    String[] lsTrafCode = wp.itemBuff("db_traf_code");
    String[] lsPlaceCode = wp.itemBuff("db_place_code");
    String[] lsTrafName = wp.itemBuff("db_traf_name");
    String[] lsPlaceName = wp.itemBuff("db_place_name");
    String[] lsFstReasonCode = wp.itemBuff("fst_reason_code");
    String[] lsReferenceNo = wp.itemBuff("reference_no");
    String[] lsReferenceSeq = wp.itemBuff("reference_seq");
    String[] lsOnlineMark = wp.itemBuff("db_online_mark");

    wp.listCount[0] = lsCardNo.length;
    if (checkApproveZz() == false) {
      return;
    }
    for (int rr = 0; rr < lsCardNo.length; rr++) {
      if (!checkBoxOptOn(rr, aaOpt)) {
        continue;
      }

      llCnt++;

      if (eqIgno(liErr[rr], "1")) {
        llErr++;
        wp.colSet(rr, "ok_flag", "-");
        continue;
      }
      func.varsSet("tsc_card_no", lsTscCard[rr]);
      func.varsSet("tran_date", lsPurchDate[rr]);
      func.varsSet("tran_time", lsPurchTime[rr]);
      func.varsSet("tran_amt", lsFstTwdAmt[rr]);
      func.varsSet("traff_code", lsTrafCode[rr]);
      func.varsSet("place_code", lsPlaceCode[rr]);
      func.varsSet("traff_subname", lsTrafName[rr]);
      func.varsSet("place_subname", lsPlaceName[rr]);
      func.varsSet("chgback_reason", lsFstReasonCode[rr]);
      func.varsSet("reference_no", lsReferenceNo[rr]);
      func.varsSet("reference_seq", lsReferenceSeq[rr]);
      func.varsSet("online_mark", lsOnlineMark[rr]);

      if (func.dataProc() == 1) {
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
      } else {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
      }
    }

    if (llCnt == 0) {
      alertErr2("請點選要傳送資料");
      return;
    }

    if (llOk > 0) {
      sqlCommit(1);
    }
    alertMsg("傳送完成; OK=" + llOk + ", ERR=" + llErr);
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
