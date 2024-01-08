/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-06  V1.00.01  Amber      program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 111/10/07  V1.00.05  ryan       移除資料類別篩選條件            *
******************************************************************************/
package mktm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm4205 extends BaseEdit {
  Mktm4205Func func;
  int i = 0;
  String kk1Rowid = "", kk2DbTemp = "", cardType = "", groupCode = "", exKeyType = "",
      exItemNo = "";
  String asKeyData = "", aprUser = "", aprDate = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      // clearFunc();
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
      /* 更新功能 */
      strAction = "S2";
      saveFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {}

  @Override
  public void deleteFunc() throws Exception {
    saveFunc();
    if (rc == 1 && userAction == false) {
      exKeyType = wp.itemStr("key_type");
      clearFunc();
    }
  }

  @Override
  public void insertFunc() throws Exception {
    saveFunc();
    if (rc == 1 && userAction == false) {
      if (addRetrieve)
        dataRead();
      else {
        /*
         * ex_key_type = wp.item_ss("key_type"); ex_item_no = wp.item_ss("item_no"); clearFunc();
         * list_wkdata3();
         */
      }
    }
  }


  @Override
  public void queryFunc() throws Exception {
    String allStr = "";
    String isKeyType = wp.itemStr("ex_key_type");
    String lsCardType = wp.itemStr("ex_card_type");
    String lsGroupCode = wp.itemStr("ex_group_code");

    wp.whereStr = " where 1=1 ";

//    switch (isKeyType) {
//
//      case "2":
//        allStr = "%" + lsCardType;
//        asKeyData = "%" + lsCardType;

//        break;
//      case "5":
        if (empty(lsGroupCode) && empty(lsCardType)) {
          allStr = lsGroupCode + "%";
          asKeyData = lsGroupCode + "%";

        } else if (!empty(lsGroupCode) && empty(lsCardType)) {
          allStr = lsGroupCode + "%";
          asKeyData = lsGroupCode + "%";

        } else if (empty(lsGroupCode) && !empty(lsCardType)) {
          allStr = "%" + lsCardType;
          asKeyData = "%" + lsCardType;

        } else {
          allStr = lsGroupCode + lsCardType;
          asKeyData = lsGroupCode + lsCardType;
        }
//    }
    wp.whereStr +=
//        " and mkt_contri_insu.key_type = :ex_key_type and mkt_contri_insu.key_data like :ss ";
    		" and mkt_contri_insu.key_data like :ss ";
//    setString("ex_key_type", wp.itemStr("ex_key_type"));
    setString("ss", allStr);
    // wp.whereStr += " and (key_data, key_type) "
    // + " not in ( "
    // + " select key_data, key_type "
    // + " from mkt_contri_insu_t "
    // + " where key_type = :ex_key_type ) ";
    // setString("ex_key_type", wp.item_ss("ex_key_type"));
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " hex(mkt_contri_insu_t.rowid) as rowid " + " ,mkt_contri_insu_t.key_data "
        + " ,mkt_contri_insu_t.key_type " + " ,mkt_contri_insu_t.rank_seq "
        + " ,mkt_contri_insu_t.crt_user " + " ,mkt_contri_insu_t.crt_date " + "	,' ' apr_user "
        + "	,' ' apr_date " + " ,'Y' db_temp ";

    wp.daoTable = " mkt_contri_insu_t " 
//    	+ " where mkt_contri_insu_t.key_type = :as_key_type "
		+ " where 1=1 "	
        + " AND mkt_contri_insu_t.key_data like :as_key_data" + " union ";
//    setString("as_key_type", wp.itemStr("ex_key_type"));
    setString("as_key_data", asKeyData);
    wp.daoTable += " select hex(mkt_contri_insu.rowid) as rowid " + " ,mkt_contri_insu.key_data "
        + " ,mkt_contri_insu.key_type " + " ,mkt_contri_insu.rank_seq "
        + " ,mkt_contri_insu.crt_user " + " ,mkt_contri_insu.crt_date "
        + " ,mkt_contri_insu.apr_user " + " ,mkt_contri_insu.apr_date " + " ,'N' db_temp "
        + " from mkt_contri_insu ";
    wp.whereOrder = " ";
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

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    kk1Rowid = itemKk("data_k1");
    if (empty(kk1Rowid)) {
      kk1Rowid = wp.itemStr("rowid");
    }
    kk2DbTemp = itemKk("data_k2");
    if (empty(kk2DbTemp)) {
      kk2DbTemp = wp.itemStr("db_temp");
    }
    if (kk2DbTemp.equals("N")) {
      aprUser = " ,apr_user ";
      aprDate = " ,apr_date ";
    } else {
      aprUser = ",' ' apr_user ";
      aprDate = ",' ' apr_date ";
    }
    wp.selectSQL = "hex(rowid) as rowid,mod_seqno " + " ,key_type " + " ,key_data " + " ,rank_seq "
        + " ,crt_user " + " ,crt_date " + " ,mod_time " + " ,mod_user " + aprUser + aprDate;
    if (kk2DbTemp.equals("Y")) {

      wp.daoTable = " mkt_contri_insu_t ";
    } else {
      wp.daoTable = " mkt_contri_insu ";
    }
    wp.whereStr = " where 1=1 and hex(rowid) = :rowid ";
    setString("rowid", kk1Rowid);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("");
      return;
    }
    listWkdata();
    wp.colSet("db_temp", kk2DbTemp);
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Mktm4205Func(wp);
    String isAction2 = "";
    if (strAction.equals("S2")) {
      if (wp.itemStr("db_temp").equals("Y")) {
        strAction = "U";
        isAction2 = "U";
      } else {
        strAction = "A";
        isAction2 = "A";
      }
    }
    rc = func.dbSave(strAction);
    log(func.getMsg());

    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (isAction2.equals("U") || isAction2.equals("A")) {
      if (rc == 1) {
        alertMsg("存檔完成");
      }
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
      // dddw_group_code
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("group_code");
      } else {
        if (wp.respHtml.indexOf("_add") > 0) {
          wp.optionKey = wp.itemStr("ex_group_code");
        } else {
          wp.initOption = "--";
          wp.optionKey = wp.itemStr("ex_group_code");
        }
      }
      dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 group by group_code,group_name order by group_code");

      // dddw_card_type
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_card_type");
      dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 group by card_type,name order by card_type");

    } catch (Exception ex) {
    }
  }

  void listWkdata() {
    String tmpStr = "", ttCardType = "", ttGroupCode = "";
    String[] item = new String[6];
    for (int i = 0; i < wp.selectCnt; i++) {
      // card_type顯示代號及中文名稱
      tmpStr = wp.colStr(i, "key_data");
      if (tmpStr.length() == 6) {
        tmpStr = wp.colStr(i, "key_data").substring(4, 6);
      }
      String sqlSelect1 =
          " select card_type,card_type||'_'||name as tt_card_type from ptr_card_type where card_type = :key_data order by card_type ";
      setString("key_data", tmpStr);
      sqlSelect(sqlSelect1);
      if (sqlRowNum > 0) {
        ttCardType = sqlStr("tt_card_type");
        cardType = sqlStr("card_type");
      }
      wp.colSet(i, "tt_card_type", ttCardType);
      wp.colSet(i, "card_type", cardType);
      // group_code顯示代號及中文名稱
      tmpStr = wp.colStr(i, "key_data");
      if (tmpStr.length() == 6) {
        tmpStr = wp.colStr(i, "key_data").substring(0, 4);
      }
      String sqlSelect2 =
          " select group_code,group_code||'_'||group_name as tt_group_code from ptr_group_code where group_code = :key_data order by group_code ";
      setString("key_data", tmpStr);
      sqlSelect(sqlSelect2);
      if (sqlRowNum > 0) {
        ttGroupCode = sqlStr("tt_group_code");
        groupCode = sqlStr("group_code");

      }
      wp.colSet(i, "tt_group_code", ttGroupCode);
      wp.colSet(i, "group_code", groupCode);

    }

  }
}
