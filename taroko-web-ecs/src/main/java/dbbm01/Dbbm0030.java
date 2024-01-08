/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-12  V1.00.00  ryan       program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                           *
******************************************************************************/
package dbbm01;

import busi.SqlPrepare;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Dbbm0030 extends BaseEdit {
  int ll = 0;
  int isError = 0;
  String msg = "";

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
      /* 存檔 */
      strAction = "S2";
      saveFunc();

    }

    dddwSelect();
    initButton();
  }

  private int getWhereStr() throws Exception {
    String batchNo = wp.itemStr("ex_batch_no");
    String referenceNo = wp.itemStr("ex_reference_no");
    String batchUnit = wp.itemStr("ex_batch_unit");

    wp.whereStr = "where 1=1 ";
    wp.whereStr +=
        " and ((curr_post_flag <> 'Y' and doubt_type like '000%' or manual_upd_flag = 'P') or manual_upd_flag = 'Y') ";

    if (empty(referenceNo) == false) {
      wp.whereStr += " and reference_no like :reference_no ";
      setString("reference_no", referenceNo + "%");
    }
    if (empty(batchNo) == false) {
      wp.whereStr += " and batch_no like :batch_no ";
      setString("batch_no", batchNo + "%");
    }
    if (empty(batchUnit) == false) {
      wp.whereStr += " and batch_no like :batch_unit ";
      setString("batch_unit", "%" + batchUnit + "%");
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL =
        "card_no, " + "batch_no, " + "purchase_date, " + "purchase_date as purchase_date_s, "
            + "film_no, " + "film_no as film_no_s, " + "dest_curr, " + "dest_curr as dest_curr_s, "
            + "dest_amt, " + "source_amt, " + "source_curr, " + "doubt_type, " + "reference_no, "
            + "curr_post_flag, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_log, "
            + "mod_seqno, " + "rsk_type, " + "format_chk_ok_flag, " + "manual_upd_flag ";

    wp.daoTable = "dbb_curpost";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
    int lisOk = 0, isError = 0;
    // queryFunc();
    if (strAction.equals("S2")) {
      busi.SqlPrepare sp = new SqlPrepare();
      String[] referenceNo = wp.itemBuff("reference_no");
      String[] optArray = wp.itemBuff("opt");
      String[] purchaseDateArray = wp.itemBuff("purchase_date");
      String[] purchaseDatesArray = wp.itemBuff("purchase_date_s");
      String[] filmNo = wp.itemBuff("film_no");
      String[] filmNos = wp.itemBuff("film_no_s");
      String[] destCurr = wp.itemBuff("dest_curr");
      String[] destCurrs = wp.itemBuff("dest_curr_s");
      String[] aaModSeqno = wp.itemBuff("mod_seqno");

      wp.listCount[0] = referenceNo.length;

      for (ll = 0; ll < referenceNo.length; ll++) {

        if (checkBoxOptOn(ll, optArray) || !purchaseDateArray[ll].equals(purchaseDatesArray[ll])
            || !filmNo[ll].equals(filmNos[ll]) || !destCurr[ll].equals(destCurrs[ll])) {
          sp.sql2Update("dbb_curpost");
          sp.ppstr("rsk_type", "");
          sp.ppstr("format_chk_ok_flag", "Y");
          sp.ppstr("doubt_type", "");
          sp.ppstr("manual_upd_flag", "Y");

          if (checkBoxOptOn(ll, optArray)) {
            sp.ppstr("mod_log", "D");
          } else {
            if (validation() != 1) {
              sqlCommit(0);
              isError++;
              wp.colSet(ll, "ok_flag", "!");
              continue;
            }
            sp.ppstr("purchase_date", purchaseDateArray[ll]);
            sp.ppstr("film_no", filmNo[ll]);
            sp.ppstr("dest_curr", destCurr[ll]);
            sp.ppstr("mod_log", "0");
          }
          sp.sql2Where(" where reference_no=?", referenceNo[ll]);
          sp.sql2Where(" and mod_seqno=?", aaModSeqno[ll]);
          sqlExec(sp.sqlStmt(), sp.sqlParm());
          if (sqlRowNum <= 0) {
            sqlCommit(0);
            isError++;
            wp.colSet(ll, "ok_flag", "!");
            wp.colSet(ll, "errmsg", "update dbb_curpost err");
            continue;
          }
        }
        lisOk++;
        sqlCommit(1);
        wp.colSet(ll, "ok_flag", "V");
      }
      alertMsg("資料存檔處理,成功:" + lisOk + "筆 ,失敗:" + isError + "筆");
    }
  }


  @Override
  public void initButton() {

    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
    this.btnModeAud("XX");
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_batch_unit");
      this.dddwList("dddw_liab_type", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");
    } catch (Exception ex) {
    }
  }

  int validation() {
    String[] aaPurchaseDate = wp.itemBuff("purchase_date");
    String[] filmNo = wp.itemBuff("film_no");
    String[] destCurr = wp.itemBuff("dest_curr");
    String[] destAmt = wp.itemBuff("dest_amt");

    // -check duplication-

    if (empty(destAmt[ll]) || this.toNum(destAmt[ll]) == 0) {
      wp.colSet(ll, "errmsg", "金額錯誤: 金額為空白或0");
      return -1;
    }

    if (empty(aaPurchaseDate[ll]) == true) {
      wp.colSet(ll, "errmsg", "消費日期不能為空白!");
      return -1;
    }

    /*
     * String sql_select = "select to_date(:ls_temp_date,'yyyymmdd') as ls_temp_date_1 from dual";
     * sqlSelect(sql_select); if(sql_nrow<=0){ alert_err( "營業日錯誤 !!"); return -1; }
     */
    String lsSql = "select business_date from ptr_businday ";
    sqlSelect(lsSql);
    String lsDate = sqlStr("business_date");
    if (this.chkStrend(aaPurchaseDate[ll], lsDate) == false) {
      wp.colSet(ll, "errmsg", "[消費日期大於營業日!");
      return -1;
    }

    if (empty(filmNo[ll])) {
      wp.colSet(ll, "errmsg", "微縮影編號不能為空白!");
      return -1;
    }

    if (!destCurr[ll].equals("901") && !destCurr[ll].equals("TWD")) {
      wp.colSet(ll, "errmsg", "Currency Code 錯誤 !!");
      return -1;
    }

    return 1;
  }

}
