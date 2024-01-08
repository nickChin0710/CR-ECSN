/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-03   V1.00.01  Ryan       program initial                           *
* 109-05-06   V1.00.02  Aoyulan       updated for project coding standard    *
* * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm1230 extends BaseEdit {
  CommString commString = new CommString();
  Colm1230Func func;

  String rowid = "";// kk2 = "";

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
      querySelect();
      // dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
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
    if (empty(wp.itemStr("exIdCorpNo")) && empty(wp.itemStr("ex_crt_user"))
        && empty(wp.itemStr("exSendYm")) && wp.itemStr("exSendFlag").equals("0")) {
      alertErr("至少輸入一個查詢條件!");
      return -1;
    }

    wp.whereStr = "where 1=1 ";

    switch (wp.itemStr("exSendFlag")) {
      case "Y":
        wp.whereStr += " and send_flag = 'Y' ";
        break;
      case "N":
        wp.whereStr += " and send_flag <> 'Y' ";
        break;
    }

    if (!empty(wp.itemStr("exIdCorpNo"))) {
      wp.whereStr += " and id_corp_no like :exIdCorpNo ";
      setString("exIdCorpNo", wp.itemStr("exIdCorpNo") + "%");
    }

    if (!empty(wp.itemStr("ex_crt_user"))) {
      wp.whereStr += " and crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }

    if (!empty(wp.itemStr("exSendYm"))) {
      wp.whereStr += " and send_ym like :exSendYm ";
      setString("exSendYm", wp.itemStr("exSendYm") + "%");
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

    wp.selectSQL = "hex(rowid) as rowid," + "id_corp_no, " + "aud_code, " + "lgd_seqno, "
        + "send_ym, " + "early_ym, " + "close_reason, " + "send_flag," + "send_date, "
        + "from_type, " + "crt_date," + "crt_user, " + "apr_date";


    wp.daoTable = "col_lgd_902";
    wp.whereStr = " ORDER BY id_corp_no";
    if (getWhereStr() != 1)
      return;
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
    String wkdata = "";
    String[] cde = new String[] {"A1", "A2", "A3", "B1", "B2", "B3"};
    String[] txt = new String[] {"借戶治癒", "協議後依約正常還款", "其他", "最小單位下債務全數清償結案、借戶自行買回或自行處分擔保品",
        "其他違約滿兩年, 回收無望案件 ", "其他"};


    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkdata = wp.colStr(ii, "aud_code");
      wp.colSet(ii, "tt_aud_code", commString.decode(wkdata, ",A,C,D", ",A.新增,C.異動,D.刪除"));

      wkdata = wp.colStr(ii, "lgd_type");
      wp.colSet(ii, "tt_lgd_type", commString.decode(wkdata, ",F", ",F.個人信用卡"));

      wkdata = wp.colStr(ii, "assure_type");
      wp.colSet(ii, "tt_assure_type", commString.decode(wkdata, ",C", ",C.無擔保(純信用)"));

      wkdata = wp.colStr(ii, "send_flag");
      wp.colSet(ii, "tt_send_flag", commString.decode(wkdata, ",Y,N", ",Y.媒體已傳送,N.媒體未傳送"));



      // cde=new String[]{"A1","A2","B2"};
      // txt=new String[]{"借戶治癒","協議後正常還款","其他違約滿兩年,回收無望案件"};
      wkdata = wp.colStr(ii, "close_reason");
      wp.colSet(ii, "tt_close_reason", commString.decode(wkdata, cde, txt));

      wkdata = wp.colStr(ii, "from_type");
      wp.colSet(ii, "tt_from_type", commString.decode(wkdata, ",1,2", ",人工,批次"));



    }
  }

  @Override
  public void querySelect() throws Exception {
    // 檢查是否為相同id_corp_no欄位值下的最新一筆資料
    String lsSendYm = itemKk("data_k2");
    if (empty(lsSendYm)) {
      lsSendYm = wp.itemStr("send_ym");
    }
    String idCorpNo = itemKk("data_k3");
    if (empty(idCorpNo)) {
      idCorpNo = wp.itemStr("id_corp_no");
    }
    if (empty(lsSendYm)) {
      lsSendYm = "999912";
    }
    String lsSql =
        "SELECT count (*) as count  FROM col_lgd_902 WHERE id_corp_no = ? AND ? < decode (send_ym, '', '999912',send_ym)";
    Object[] param = new Object[] {idCorpNo, lsSendYm};
    sqlSelect(lsSql, param);
    if (sqlNum("count") > 0) {
      wp.alertMesg = "<script language='javascript'> alert('不是 [LGD表二(902)] 最新一筆, 不可修改')</script>";
      wp.colSet("display_none", "style='display:none'");
    }

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    rowid = itemKk("data_k1");
    if (empty(rowid)) {
      rowid = wp.itemStr("rowid");
    }
    wp.selectSQL = "hex(rowid) as rowid,mod_seqno," + "id_corp_no, " + "id_corp_p_seqno, " // phopho
                                                                                           // add
        + "lgd_seqno, " + "send_flag, " + "aud_code, " + "send_date, " + "send_ym," + "from_type,"
        + "lgd_type," + "crt_date," + "early_ym," + "crt_user," + "risk_amt," + "apr_date,"
        + "overdue_ym," + "apr_user," + "overdue_amt," + "coll_ym," + "coll_amt," + "crdt_charact,"
        + "assure_type," + "crdt_use_type," + "syn_loan_yn," + "syn_loan_date," + "fina_commit_yn,"
        + "ecic_case_type," + "fina_commit_prct," + "card_rela_type," + "recv_trade_amt,"
        + "recv_collat_amt," + "recv_fina_amt," + "recv_self_amt," + "recv_rela_amt,"
        + "recv_oth_amt," + "costs_amt," + "costs_ym," + "revol_rate," + "close_reason,"
        + "collat_yn";

    wp.daoTable = "col_lgd_902";
    wp.whereStr = "where 1=1 ";
    wp.whereStr += "and  hex(rowid)= :rowid";
    setString("rowid", rowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowid);
    }
    listWkdata();

  }

  @Override
  public void saveFunc() throws Exception {
    if (strAction.equals("D")) {
      if (!wp.itemStr("from_type").equals("1") || wp.itemStr("send_flag").equals("Y")) {
        alertErr("資料非人工建檔 or 已報送 不可刪除");
        return;
      }
    }
    // 如果資料的send_flag欄位值為'Y'，is_action ='A'(A表示新增)
    if (strAction.equals("S2")) {
      if (wp.itemStr("send_flag").equals("Y")) {
        strAction = "A";
      } else {
        strAction = "U";
      }
    }
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    func = new Colm1230Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    if (strAction.equals("A") || strAction.equals("U")) {
      if (rc != 1) {
        alertErr("存檔失敗");
      } else {
        if (strAction.equals("A")) {
          alertMsg("新增成功");
          clearFunc();
        }
        if (strAction.equals("U")) {
          alertMsg("修改成功");
          querySelect();
        }
      }

    }

    this.sqlCommit(rc);


  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

}
