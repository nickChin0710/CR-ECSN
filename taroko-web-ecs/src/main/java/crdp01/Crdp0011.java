/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-16  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 108-12-31	 V1.00.02  Andy       update : crd_nccc_card => crd_card_item    *
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        * 	
 * 109-12-30  V1.00.09  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/

package crdp01;

import ofcapp.BaseEdit;
import taroko.base.SqlParm;
import taroko.com.TarokoCommon;
import java.util.concurrent.TimeUnit;

import busi.SqlPrepare;

public class Crdp0011 extends BaseEdit {
  String mExBatchno = "";
  String mExEmbossSource = "";
  String mExEmbossReason = "";

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
    String exWarehouseNo = wp.itemStr("ex_warehouse_no");
    String exCardItem = wp.itemStr("ex_card_item");
    String exPlace = wp.itemStr("ex_place");
    String exTnsType = wp.itemStr("ex_tns_type");
    String exTransReason1 = wp.itemStr("ex_trans_reason1");
    String exTransReason2 = wp.itemStr("ex_trans_reason2");
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");

    wp.whereStr = " where 1=1 and apr_flag ='' ";
    // 異動編號
    if (empty(exWarehouseNo) == false) {
      wp.whereStr += sqlCol(exWarehouseNo, "a.warehouse_no");
    }
    // 卡樣代碼
    if (empty(exCardItem) == false) {
      wp.whereStr += sqlCol(exCardItem, "a.card_item");
    }
    // 庫存地方
    if (exPlace.equals("0") == false) {
      wp.whereStr += sqlCol(exPlace, "a.place");
    }
    // 庫存種類
    if (exTnsType.equals("0") == false) {
      wp.whereStr += sqlCol(exTnsType, "a.tns_type");
    }
    // 出入庫原因
    switch (exTnsType) {
      case "1":
        if (empty(exTransReason1) == false) {
          wp.whereStr += sqlCol(exTransReason1, "a.trans_reason");
        }
        break;
      case "2":
        if (empty(exTransReason2) == false) {
          wp.whereStr += sqlCol(exTransReason2, "a.trans_reason");
        }
        break;
    }
    // 起帳日期
    wp.whereStr += sqlStrend(exDateS, exDateE, "a.warehouse_date");

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    if (getWhereStr() == false)
      return;
    wp.pageControl();

    wp.selectSQL = "" + "a.warehouse_no, " + "a.card_item, "
        + "(select name from crd_card_item where card_item = a.card_item ) db_card_item_name, "
        + "a.warehouse_date, " + "a.card_type, " + "a.unit_code, " + "a.tns_type, " + "a.place, "
        + "a.prev_total, " + "a.use_total, " + "a.crt_date, " + "a.mod_user, " + "a.mod_time, "
        + "a.mod_pgm, " + "a.mod_seqno, " + "a.crt_user, " + "a.trans_reason, "
        + "nvl(b.ic_flag,'N') ic_flag, " + "a.item_amt ";
    wp.daoTable = "crd_whtrans a left join crd_item_unit b on  a.card_item = b.card_item ";
    wp.whereOrder = " order by a.warehouse_no";
//    getWhereStr();
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
    int rowCt = 0;
    String lsSql = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wp.colSet(ii, "group_ct", "1");

      // db_place庫位
      String place = wp.colStr(ii, "place");
      String[] cde = new String[] {"1", "2", "3"};
      String[] txt = new String[] {"1.本行", "2.台銘", "3.宏通"};
      wp.colSet(ii, "db_place", commString.decode(place, cde, txt));

      // db_tns_type庫存種類
      String tnsType = wp.colStr(ii, "tns_type");
      String[] cde1 = new String[] {"1", "2"};
      String[] txt1 = new String[] {"1.入庫", "2.出庫"};
      wp.colSet(ii, "db_tns_type", commString.decode(tnsType, cde1, txt1));

      // db_trans_reason處理結果
      String wkTransReason = wp.colStr(ii, "trans_reason");
      String dbTransReason = "";
      String wfType = "";
      if (!empty(wkTransReason)) {
        switch (tnsType) {
          case "1":
        	wfType = "TRNS_IN_RSN";
            break;
          case "2":
        	wfType = "TRNS_RSN1";
            break;
        }
        lsSql = "select wf_type,wf_id,wf_desc from ptr_sys_idtab where wf_type = :wf_type and wf_id = :wf_id";
        setString("wf_type",wfType);
        setString("wf_id",wp.colStr(ii, "trans_reason"));
        sqlSelect(lsSql);

        if (sqlRowNum > 0) {
          dbTransReason = wp.colStr(ii, "trans_reason") + "[" + sqlStr("wf_desc") + "]";
        }
      }
      wp.colSet(ii, "db_trans_reason", dbTransReason);
    }
  }

  @Override
  public void querySelect() throws Exception {
    // m_ex_mcht_no = wp.item_ss("mcht_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

    String[] opt = wp.itemBuff("opt");

    String[] aaBatchno = wp.itemBuff("batchno");
    String[] aaWarehouseNo = wp.itemBuff("warehouse_no");


    wp.listCount[0] = aaWarehouseNo.length;
    // check && update
    int llOk = 0, llErr = 0;
    for (int ii = 0; ii < aaWarehouseNo.length; ii++) {
      if (checkBoxOptOn(ii, opt)) {
        busi.SqlPrepare sp = new SqlPrepare();
        sp.sql2Update("crd_whtrans");
        sp.ppstr("apr_flag", "Y");
        sp.ppstr("apr_user", wp.loginUser);
        sp.ppstr("apr_date", getSysDate());
        sp.ppstr("mod_user", wp.loginUser);
        sp.ppstr("mod_pgm", wp.modPgm());
        sp.addsql(", mod_time = sysdate", "");
        sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
        sp.sql2Where(" where warehouse_no=?", aaWarehouseNo[ii]);
        sqlExec(sp.sqlStmt(), sp.sqlParm());
      }
    }
    alertMsg("覆核處理成功!!");

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
      wp.optionKey = wp.itemStr("ex_card_item");
      this.dddwList("dddw_card_item", "crd_card_item", "card_item", "name", "where 1=1 ");

      wp.optionKey = wp.itemStr("ex_trans_reason1");
      this.dddwList("dddw_trans_reason1", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'TRNS_IN_RSN' ");

      wp.optionKey = wp.itemStr("ex_trans_reason2");
      this.dddwList("dddw_trans_reason2", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'TRNS_RSN1' ");

      wp.optionKey = wp.itemStr("ex_place");
      this.dddwList("dddw_place", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'WH_LOC' ");

    } catch (Exception ex) {
    }
  }

}
