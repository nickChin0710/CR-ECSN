/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package rskm02;

/*rskp2040;覆審查詢資料統計處理 V.2018-0824.JH
 * 2018-0824:	JH		call-batch
 * 2018-0822:	JH		call-batch
 * 
 * */
import ofcapp.BaseAction;

public class Rskp2040 extends BaseAction {

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
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
      // -資料統計流程處理-
      procFunc();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_send_date1"), wp.itemStr("ex_send_date2")) == false) {
      alertErr2("傳送JCIC日期起迄：輸入錯誤");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_resp_date1"), wp.itemStr("ex_resp_date2")) == false) {
      alertErr2("JCIC 回覆日期起迄：輸入錯誤");
      return;
    }

    String lsWhere =
        " where trial_batch_no <>'' " + sqlCol(wp.itemStr("ex_batch_no"), "trial_batch_no", "like%")
            + sqlCol(wp.itemStr("ex_send_date1"), "send_date", ">=")
            + sqlCol(wp.itemStr("ex_send_date2"), "send_date", "<=")
            + sqlCol(wp.itemStr("ex_resp_date1"), "resp_date", ">=")
            + sqlCol(wp.itemStr("ex_resp_date2"), "resp_date", "<=");
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "trial_batch_no," + " send_date ," + " send_file ," + " resp_date ,"
        + " trial_proc_date ," + " resp_file ," + " 0 as db_unclose_cnt ," + " 0 as db_ecs_date,"
        + " 0 as db_group_date ";
    wp.daoTable = "col_jcic_file_log";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    pageQuery();
    wp.setListCount(1);
    listWkdata(wp.selectCnt);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  void listWkdata(int listNrow) {
    for (int ii = 0; ii < listNrow; ii++) {
      // --db_unclose_cnt--
      if (empty(wp.colStr(ii, "send_file"))) {
        countUncloseB(wp.colStr(ii, "trial_batch_no"));
        wp.colSet(ii, "db_unclose_cnt", sqlStr("db_cnt"));
      } else {
        countUncloseA(wp.colStr(ii, "trial_batch_no"), wp.colStr(ii, "send_file"),
            wp.colStr(ii, "send_date"));
        wp.colSet(ii, "db_unclose_cnt", sqlStr("db_cnt"));
      }
      // --ECS最近統計日期--
      selectEcsDate(wp.colStr(ii, "trial_batch_no"));
      wp.colSet(ii, "db_ecs_date", sqlStr("last_ecs_date"));
      // --最近分群日期--
      selectGroupDate(wp.colStr(ii, "trial_batch_no"));
      wp.colSet(ii, "db_group_date", sqlStr("last_group_date"));
    }
  }

  void countUncloseA(String batchNo, String lsSendFile, String lsSendDate) {
    String sql1 = "select count(*) as db_cnt " + " from rsk_trial_list A, col_jcic_query_req B"
        + " where A.batch_no =?" + " and nvl(A.close_flag,'N')<>'Y' " + " and A.id_no = B.id_no "
        + " and B.req_file=? " + " and B.send_date=?";
    sqlSelect(sql1, new Object[] {batchNo, lsSendFile, lsSendDate});
  }

  void countUncloseB(String batchNo) {
    String sql1 = "select count(*) as db_cnt " + " from rsk_trial_list" + " where batch_no =? "
        + " and nvl(close_flag,'N')<>'Y' ";
    sqlSelect(sql1, new Object[] {batchNo});
  }

  void selectEcsDate(String batchNo) {
    String sql1 = "select max(proc_date) as last_ecs_date " + " from rsk_trial_data_ecs"
        + " where batch_no =? ";
    sqlSelect(sql1, new Object[] {batchNo});
  }

  void selectGroupDate(String batchNo) {
    String sql1 = "select max(risk_group_date) as last_group_date " + " from rsk_trial_list"
        + " where batch_no =? " + " and risk_group <>'' ";
    sqlSelect(sql1, new Object[] {batchNo});
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

    String allStr = wp.itemStr2("ex_callbatch");
    if (eqIgno(allStr, "b110")) {
      procB110();
    } else if (eqIgno(allStr, "b003")) {
      procB003();
    } else if (eqIgno(allStr, "b008")) {
      procB008();
    } else if (eqIgno(allStr, "b013")) {
      procB013();
    } else if (eqIgno(allStr, "b004")) {
      procB004();
    } else {
      alertErr2("處理項目, 無指定批次程式");
    }

  }

  void procB110() throws Exception {
    listKeep();
    if (rc != 1)
      return;

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    String[] aaBatchNo = wp.itemBuff("trial_batch_no");
    int rr = optToIndex(wp.itemStr2("opt"));

    // --callbatch
    rc = batch.callBatch("RskB110 " + aaBatchNo[rr]);
    if (rc != 1) {
      alertErr2("期中覆審統計流程處理: callbatch 失敗");
      return;
    } else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
    }
    alertMsg("期中覆審統計流程處理中, 請稍待...");
  }

  void procB003() throws Exception {
    listKeep();
    if (rc != 1)
      return;

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    String[] aaBatchNo = wp.itemBuff("trial_batch_no");
    int rr = optToIndex(wp.itemStr2("opt"));

    if (empty(wp.itemStr(rr, "send_file"))) {
      errmsg("未產生送 JCIC 查詢媒體檔");
      return;
    }

    if (wp.itemEmpty(rr, "resp_file")) {
      errmsg("查詢媒體檔, JCIC 未回覆");
      return;
    }

    if (empty(aaBatchNo[rr])) {
      errmsg("查詢批號為空白, 不是期中覆審查詢資料");
      return;
    }

    if (checkJcicFileLog(aaBatchNo[rr]) == false) {
      if (!eqIgno(wp.itemStr("conf_flag1"), "Y")) {
        wp.colSet("conf_mesg1", "||1==1");
        return;
      }
    }

    if (!wp.itemEmpty(rr, "trial_proc_date")) {
      if (!wp.itemEq("conf_flag2", "Y")) {
        wp.colSet("conf_mesg2", "||1==1");
        return;
      }
    } else {
      if (!wp.itemEq("conf_flag3", "Y")) {
        wp.colSet("conf_mesg3", "||1==1");
        return;
      }
    }

    // --callbatch
    // batch.debug =true;
    rc = batch.callBatch("RskB003 " + aaBatchNo[rr]);
    if (rc != 1) {
      alertErr2("覆審資料統計處理: callbatch 失敗");
      return;
    } else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
    }

    alertMsg("覆審JCIC資料統計處理中, 請稍待...");
  }

  boolean checkJcicFileLog(String lsBatchNo) {
    String sql1 = " select " + " count(*) as db_cnt " + " from col_jcic_file_log "
        + " where trial_batch_no = ? " + " and resp_file = '' ";

    sqlSelect(sql1, new Object[] {lsBatchNo});

    if (sqlNum("db_cnt") > 0)
      return false;

    return true;
  }

  void procB008() throws Exception {
    listKeep();
    if (rc != 1)
      return;

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    String[] aaBatchNo = wp.itemBuff("trial_batch_no");
    int rr = optToIndex(wp.itemStr2("opt"));

    // --callbatch
    rc = batch.callBatch("RskB008 " + aaBatchNo[rr]);
    if (rc != 1) {
      alertErr2("本行資料統計處理: callbatch 失敗");
      return;
    } else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
    }
    alertMsg("覆審資料統計處理中, 請稍待...");
  }

  public void procB004() throws Exception {
    listKeep();
    if (rc != 1)
      return;

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    String[] aaBatchNo = wp.itemBuff("trial_batch_no");
    int rr = optToIndex(wp.itemStr2("opt"));

    // --callbatch
    rc = batch.callBatch("RskB004 " + aaBatchNo[rr]);
    if (rc != 1) {
      alertErr2("風險族群評估處理: callbatch 失敗");
      return;
    } else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
    }
    alertMsg("設定風險族群處理中, 請稍待...");
  }

  void procB013() throws Exception {
    listKeep();
    if (rc != 1)
      return;

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    // batch.debug =true;
    String[] aaBatchNo = wp.itemBuff("trial_batch_no");
    int rr = optToIndex(wp.itemStr2("opt"));
    // --callbatch
    rc = batch.callBatch("RskB013 " + aaBatchNo[rr]);
    if (rc != 1) {
      alertErr2("風險族群評估處理: callbatch 失敗");
      return;
    } else {
      alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
    }

    alertMsg("設定風險族群處理中, 請稍待...");
  }

  void listKeep() {
    wp.listCount[0] = wp.itemRows("trial_batch_no");
    String[] opt = wp.itemBuff("opt");
    this.optNumKeep(wp.listCount[0], opt);
    if (optToIndex(opt[0]) < 0) {
      alertErr2("請選取欲處理之批號");
    }
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
