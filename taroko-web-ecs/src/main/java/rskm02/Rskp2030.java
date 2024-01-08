/*****************************************************************************
*                                                                            *

*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-05-07 	                   Alex              日期預設系統日期
* 107-08-01 	                   JH		              test
* 109-01-06   V1.00.02   Justin Wu    updated for archit.  change
* 109-04-27  V1.00.03  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;

import ofcapp.BaseAction;

public class Rskp2030 extends BaseAction {

  @Override
  public void userAction() throws Exception {
    wp.pgmVersion("V.20-0106");

    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // * 查詢功能 */
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        procDelete();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -產生名單-
        procFunc();
        break;
      case "C2":
        // -轉入JCIC查詢檔-
        procJcic();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxConfirm();
        break;
      default:
        break;
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_query_date1"), wp.itemStr("ex_query_date2")) == false) {
      alertErr2("查詢日期起迄：輸入錯誤");
      return;
    }


    String lsWhere =
        " where 1=1 and apr_flag='Y' " + sqlCol(wp.itemStr("ex_batch_no"), "batch_no", "like%")
            + sqlCol(wp.itemStr("ex_query_date1"), "query_date", ">=")
            + sqlCol(wp.itemStr("ex_query_date2"), "query_date", "<=");
    if (wp.itemEq("ex_regist_type", "0") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_regist_type"), "regist_type");
    }

    if (wp.itemEq("ex_trans_type", "A") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_trans_type"), "trans_type");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "batch_no," + " query_date ," + " regist_type ,"
        + " decode(regist_type,'1','人工','2','批次') as tt_regist_type ," + " list_crt_date ,"
        + " list_crt_rows ," + " imp_jcic_date ," + " imp_jcic_rows ," + " 0 as db_unimp_row,"
        + " trial_reason ," + " trans_type ," + " imp_file_name";
    wp.daoTable = "rsk_trial_parm";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();

  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      countJcicRows(wp.colStr(ii, "batch_no"));
      wp.colSet(ii, "db_unimp_row", sqlStr("jcic_rows"));
    }
  }

  void countJcicRows(String batchNo) {

    String sql1 = "select count(*) as jcic_rows " + " from rsk_trial_list" + " where batch_no =? "
        + " and send_file = ''";
    sqlSelect(sql1, new Object[] {batchNo});
  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dataRead() throws Exception {}

  @Override
  public void saveFunc() throws Exception {}

  @Override
  public void procFunc() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    // batch.debug =true;

    rskm02.Rskp2030Func func = new rskm02.Rskp2030Func();
    func.setConn(wp);

    String[] lsBatchNo = wp.itemBuff("batch_no");
    String[] lsRegistType = wp.itemBuff("regist_type");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsBatchNo.length;
    this.optNumKeep(lsBatchNo.length, opt);
    if (opt.length <= 0 || empty(opt[0])) {
      errmsg("請選取欲處理之批號");
      return;
    }

    for (int ii = 0; ii < opt.length; ii++) {
      int rr = optToIndex(opt[ii]);
      if (rr < 0)
        break;

      wp.colSet(rr, "ok_flag", "!");
      if (!eqIgno(lsRegistType[rr], "2")) {
        errmsg("非參數設定產生名單");
        wp.colSet(rr, "ok_flag", "X");
        break;
      }

      if (checkList(lsBatchNo[rr])) {
        errmsg("名單已產生, 請刪除再產生");
        wp.colSet(rr, "ok_flag", "X");
        break;
      }

      // --callbatch
      rc = batch.callBatch("RskB001 " + lsBatchNo[rr]);
      if (rc != 1) {
        errmsg("名單產生處理: callbatch 失敗; " + batch.getMesg());
        wp.colSet(rr, "ok_flag", "X");
        break;
      } else {
        alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
      }

      func.varsSet("batch_seqno", batch.batchSeqno());
      func.varsSet("batch_no", lsBatchNo[rr]);
      int liRc = func.updateTrialParm();
      sqlCommit(liRc);
      if (liRc == -1) {
        errmsg(func.getMsg());
        wp.colSet(rr, "ok_flag", "X");
        break;
      }

      ilOk++;
      wp.colSet(rr, "ok_flag", "V");
    }
    // --
    if (ilOk > 0)
      alertMsg("名單產生處理中, 請稍待...");
  }

  boolean checkList(String lsBatchNo) {
    String sql1 =
        " select " + " count(*) as ll_cnt " + " from rsk_trial_list " + " where batch_no = ? ";
    sqlSelect(sql1, new Object[] {lsBatchNo});

    if (sqlNum("ll_cnt") <= 0)
      return false;
    return true;
  }

  public void procJcic() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    // batch.debug =true;

    rskm02.Rskp2030Func func = new rskm02.Rskp2030Func();
    func.setConn(wp);

    String[] lsBatchNo = wp.itemBuff("batch_no");
    String[] lsRegistType = wp.itemBuff("regist_type");
    String[] lsListCrtRows = wp.itemBuff("list_crt_rows");
    String[] lsImpJcicRows = wp.itemBuff("imp_jcic_rows");
    // String[] ls_trans_type = wp.item_buff("trans_type");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsBatchNo.length;
    this.optNumKeep(wp.listCount[0], opt);
    if (optToIndex(opt[0]) < 0) {
      errmsg("請選取欲處理之批號");
      return;
    }

    for (int ii = 0; ii < opt.length; ii++) {
      int rr = this.optToIndex(opt[ii]);
      if (rr < 0) {
        errmsg("請選取欲處理之批號");
        break;
      }

      wp.colSet(rr, "ok_flag", "!");
      if (commString.strToInt(lsListCrtRows[rr]) == 0) {
        errmsg("未產生名單, 無法轉入 JCIC 查詢");
        wp.colSet(rr, "ok_flag", "X");
        break;
      }

      if (commString.strToInt(lsListCrtRows[rr]) <= commString.strToInt(lsImpJcicRows[rr])) {
        errmsg("資料已全部轉入 JCIC 查詢");
        wp.colSet(rr, "ok_flag", "X");
        break;
      }

      String lsTransType = wp.itemStr2("ex_trans_type");
      if (!eqIgno(lsTransType, "M"))
        lsTransType = "F";

      func.varsSet("trans_type", lsTransType);
      func.varsSet("batch_no", lsBatchNo[rr]);
      int liRc = func.updateTrialParmType();
      if (liRc == -1) {
        errmsg(func.getMsg());
        this.sqlCommit(-1);
        wp.colSet(rr, "ok_flag", "X");
        break;
      }

      // --callbatch
      liRc = batch.callBatch("RskB002 " + lsBatchNo[rr] + " " + this.userDeptNo());
      if (liRc != 1) {
        alertErr("覆審名單轉入JCJC處理: callbatch 失敗; " + batch.getMesg());
        wp.colSet(rr, "ok_flag", "X");
        break;
      } else {
        alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
      }

      func.varsSet("batch_seqno", batch.batchSeqno());
      func.varsSet("batch_no", lsBatchNo[rr]);
      liRc = func.updateTrialParm();
      if (liRc == -1) {
        this.dbRollback();
        errmsg(func.getMsg());
        wp.colSet(rr, "ok_flag", "X");
        break;
      }
      ilOk++;
      wp.colSet(rr, "ok_flag", "V");
    }

    if (ilOk > 0)
      alertMsg("覆審名單轉入JCJC處理中, 請稍待...");
  }

  public void procDelete() throws Exception {
    int ilOk = 0;
    int ilErr = 0;

    rskm02.Rskp2030Func func = new rskm02.Rskp2030Func();
    func.setConn(wp);

    String[] lsBatchNo = wp.itemBuff("batch_no");
    String[] lsRegistType = wp.itemBuff("regist_type");
    String[] lsImpJcicRows = wp.itemBuff("imp_jcic_rows");
    String[] lsListCrtRows = wp.itemBuff("list_crt_rows");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsBatchNo.length;
    this.optNumKeep(lsBatchNo.length, opt);

    if (this.optToIndex(opt[0]) < 0) {
      errmsg("請點選欲刪除之批號");
      return;
    }

    for (int ii = 0; ii < opt.length; ii++) {
      int rr = optToIndex(opt[ii]);
      if (rr < 0) {
        break;
      }

      wp.colSet(rr, "ok_flag", "!");
      func.varsSet("batch_no", lsBatchNo[rr]);
      func.varsSet("imp_jcic_rows", lsImpJcicRows[rr]);
      func.varsSet("regist_type", lsRegistType[rr]);

      int liRc = func.dbDelete();
      log("rc:" + liRc);
      sqlCommit(liRc);
      if (liRc == 1) {
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }
      // -error--
      ilErr++;
      alertErr2(func.getMsg());
      wp.colSet(rr, "ok_flag", "X");
    }

    if (ilErr == 0) {
      alertMsg("刪除名單處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
    }
  }

  public void wfAjaxConfirm() throws Exception {
    // wp =wr; // 20200102 updated for archit. change

    String lsBatchNo = wp.itemStr("ax_batch_no");
    // wp.ddd("ax_batch_no="+ls_batch_no);

    wp.jsonCode = "Y";

    if (count(lsBatchNo)) {
      wp.addJSON("conf_flag", "Y1");
    } else {
      wp.addJSON("conf_flag", "Y2");
    }
  }

  boolean count(String batchNo) {
    String sql1 = "select count(*) as db_cnt " + " from rsk_trial_list" + " where batch_no =? ";
    sqlSelect(sql1, new Object[] {batchNo});
    if (sqlNum("db_cnt") > 0)
      return true;
    return false;
  }


  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    wp.colSet("ex_query_date1", this.getSysDate());

  }

}
