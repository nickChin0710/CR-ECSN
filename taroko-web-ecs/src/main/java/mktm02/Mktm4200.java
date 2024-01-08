/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-28  V1.00.01  ryan      program initial                             *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package mktm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm4200 extends BaseEdit {
  Mktm4200Func func;
  int i = 0;
  String kk1Rowid = "", kk2DbTemp = "", cardType = "", groupCode = "", exKeyType = "",
      exItemNo = "";

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
      if (listWkdata2() != 1) {
        return;
      }
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
      wp.colSet("kk_card_type_none", "none");
      wp.colSet("kk_group_code_none", "none");
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
      wp.colSet("kk_card_type_none", "none");
      wp.colSet("kk_group_code_none", "none");
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 更新功能 */
      strAction = "S2";
      wp.colSet("kk_card_type_none", "none");
      wp.colSet("kk_group_code_none", "none");
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
      exItemNo = wp.itemStr("item_no");
      clearFunc();
      listWkdata3();
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
    String isItemNo = wp.itemStr("ex_item_no");

    if (isItemNo.equals("03") && !isKeyType.equals("5")) {
      alertErr("資料類別須為 [團體代號+卡種]");
      return;
    }
    if ((isItemNo.equals("01") || isItemNo.equals("02")) && isKeyType.equals("2")) {
      alertErr("資料類別須為 [團體代號]或[團體代號+卡種]");
      return;
    }
    /*
     * if(is_key_type.equals("1")&&empty(ls_group_code)){ alert_err("團體代號不可空白"); return; }
     * if(is_key_type.equals("2")&&empty(ls_card_type)){ alert_err("卡種不可空白"); return; }
     * if(is_key_type.equals("5")&&(empty(ls_group_code)||empty(ls_card_type))){
     * alert_err("團體代號及卡種不可空白"); return; }
     */
    wp.whereStr = " where 1=1 ";
    switch (isKeyType) {
      case "1":
        allStr = lsGroupCode;
        break;
      case "2":
        allStr = lsCardType;
        break;
      case "5":
        if (empty(lsGroupCode) && empty(lsCardType)) {
          allStr = "%";
        } else if (!empty(lsGroupCode) && empty(lsCardType)) {
          allStr = lsGroupCode + "%";
        } else if (empty(lsGroupCode) && !empty(lsCardType)) {
          allStr = "%" + lsCardType;
        } else {
          allStr = lsGroupCode + lsCardType;
        }
    }
    wp.whereStr +=
        " and mkt_contri_parm.item_no = :ex_item_no and mkt_contri_parm.key_type = :ex_key_type and mkt_contri_parm.key_data like :ss ";
    setString("ex_item_no", wp.itemStr("ex_item_no"));
    setString("ex_key_type", wp.itemStr("ex_key_type"));
    setString("ss", allStr);
    wp.whereStr += " and (item_no, key_data, key_type) " + " not in ( "
        + " select item_no, key_data, key_type " + " from mkt_contri_parm_t "
        + " where item_no = :ex_item_no " + " and key_type = :ex_key_type ) ";
    setString("ex_item_no", wp.itemStr("ex_item_no"));
    setString("ex_key_type", wp.itemStr("ex_key_type"));
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " hex(mkt_contri_parm_t.rowid) as rowid " + " ,mkt_contri_parm_t.item_no "
        + " ,mkt_contri_parm_t.key_data " + " ,mkt_contri_parm_t.key_type "
        + " ,mkt_contri_parm_t.bonu_cash_flag " + " ,mkt_contri_parm_t.cost_amt "
        + " ,mkt_contri_parm_t.item_ename_incl " + " ,mkt_contri_parm_t.item_ename_excl "
        + " ,mkt_contri_parm_t.crt_user " + " ,mkt_contri_parm_t.crt_date "
        + " ,mkt_contri_parm_t.cost_month" + " ,'Y' db_temp ";

    wp.daoTable = " mkt_contri_parm_t " + " where mkt_contri_parm_t.item_no = :as_item_no "
        + " and mkt_contri_parm_t.key_type = :as_key_type " + " union ";
    setString("as_item_no", wp.itemStr("ex_item_no"));
    setString("as_key_type", wp.itemStr("ex_key_type"));
    wp.daoTable += " select hex(mkt_contri_parm.rowid) as rowid " + " ,mkt_contri_parm.item_no "
        + " ,mkt_contri_parm.key_data " + " ,mkt_contri_parm.key_type "
        + " ,mkt_contri_parm.bonu_cash_flag " + " ,mkt_contri_parm.cost_amt "
        + " ,mkt_contri_parm.item_ename_incl " + " ,mkt_contri_parm.item_ename_excl "
        + " ,mkt_contri_parm.crt_user " + " ,mkt_contri_parm.crt_date "
        + " ,mkt_contri_parm.cost_month" + " ,'N' db_temp " + " from mkt_contri_parm ";
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
    wp.selectSQL = "hex(rowid) as rowid,mod_seqno " + " ,item_no " + " ,key_type " + " ,key_data "
        + " ,cost_amt " + " ,item_ename_incl " + " ,item_ename_excl " + " ,crt_user "
        + " ,crt_date " + " ,cost_month ";
    if (kk2DbTemp.equals("Y")) {
      wp.daoTable = " mkt_contri_parm_t ";
    } else {
      wp.daoTable = " mkt_contri_parm ";
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
    func = new Mktm4200Func(wp);
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
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnModeAud("XX");
  }

  @Override
  public void dddwSelect() {

    try {
      wp.optionKey = wp.itemStr("kk_card_type");
      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 order by card_type");
      wp.optionKey = wp.itemStr("kk_group_code");
      this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 order by group_code");
    } catch (Exception ex) {
    }
  }

  void listWkdata() {
    String keyData = "", ttCardType = "", ttGroupCode = "";
    String[] item = new String[6];
    for (int i = 0; i < wp.selectCnt; i++) {
      // card_type顯示代號及中文名稱
      keyData = wp.colStr(i, "key_data");
      if (keyData.length() == 6) {
        keyData = wp.colStr(i, "key_data").substring(4, 6);
      }
      String sqlSelect1 =
          " select card_type,card_type||'_'||name as tt_card_type from ptr_card_type where card_type = :key_data order by card_type ";
      setString("key_data", keyData);
      sqlSelect(sqlSelect1);
      if (sqlRowNum > 0) {
        ttCardType = sqlStr("tt_card_type");
        cardType = sqlStr("card_type");
      }
      wp.colSet(i, "tt_card_type", ttCardType);
      wp.colSet(i, "card_type", cardType);
      // group_code顯示代號及中文名稱
      keyData = wp.colStr(i, "key_data");
      if (keyData.length() == 6) {
        keyData = wp.colStr(i, "key_data").substring(0, 4);
      }
      String sqlSelect2 =
          " select group_code,group_code||'_'||group_name as tt_group_code from ptr_group_code where group_code = :key_data order by group_code ";
      setString("key_data", keyData);
      sqlSelect(sqlSelect2);
      if (sqlRowNum > 0) {
        ttGroupCode = sqlStr("tt_group_code");
        groupCode = sqlStr("group_code");
      }
      wp.colSet(i, "tt_group_code", ttGroupCode);
      wp.colSet(i, "group_code", groupCode);
      keyData = wp.colStr(i, "item_ename_incl");
      if (!empty(keyData) && keyData.length() >= 6) {
        item[0] = keyData.substring(0, 1);
        item[1] = keyData.substring(1, 2);
        item[2] = keyData.substring(2, 3);
        item[3] = keyData.substring(3, 4);
        item[4] = keyData.substring(4, 5);
        item[5] = keyData.substring(5, 6);
        if (item[0].equals("Y")) {
          wp.colSet(i, "incl_bl", "Y");
        }
        if (item[1].equals("Y")) {
          wp.colSet(i, "incl_it", "Y");
        }
        if (item[2].equals("Y")) {
          wp.colSet(i, "incl_ca", "Y");
        }
        if (item[3].equals("Y")) {
          wp.colSet(i, "incl_id", "Y");
        }
        if (item[4].equals("Y")) {
          wp.colSet(i, "incl_ao", "Y");
        }
        if (item[5].equals("Y")) {
          wp.colSet(i, "incl_ot", "Y");
        }
      }
      keyData = wp.colStr(i, "item_ename_excl");
      if (!empty(keyData) && keyData.length() >= 6) {
        item[0] = keyData.substring(0, 1);
        item[1] = keyData.substring(1, 2);
        item[2] = keyData.substring(2, 3);
        item[3] = keyData.substring(3, 4);
        item[4] = keyData.substring(4, 5);
        item[5] = keyData.substring(5, 6);
        if (item[0].equals("Y")) {
          wp.colSet(i, "excl_bl", "Y");
        }
        if (item[1].equals("Y")) {
          wp.colSet(i, "excl_it", "Y");
        }
        if (item[2].equals("Y")) {
          wp.colSet(i, "excl_ca", "Y");
        }
        if (item[3].equals("Y")) {
          wp.colSet(i, "excl_id", "Y");
        }
        if (item[4].equals("Y")) {
          wp.colSet(i, "excl_ao", "Y");
        }
        if (item[5].equals("Y")) {
          wp.colSet(i, "excl_ot", "Y");
        }
      }
      keyData = wp.colStr(i, "item_no");
      wp.colSet(i, "tt_item_no",
          commString.decode(keyData, ",01,02,03,04,05", ",紅利積點,現金回饋,卡片成本,公共運輸旅平險,國際組織白金秘書"));
    }
    if (wp.colStr("key_type").equals("1")) {
      wp.colSet("kk_card_type_none", "none");
    }
    if (wp.colStr("key_type").equals("2")) {
      wp.colSet("kk_group_code_none", "none");
    }
    if (wp.colStr("item_no").equals("01") || wp.colStr("item_no").equals("02")) {
      wp.colSet("cost_amt_none", "none");
    } else {
      wp.colSet("item_ename_none", "none");
    }
  }

  int listWkdata2() {
    String exItemNo = wp.itemStr("ex_item_no");
    String isKeyType = wp.itemStr("ex_key_type");
    String isItemNo = wp.itemStr("ex_item_no");
    if (isItemNo.equals("03") && !isKeyType.equals("5")) {
      alertErr("資料類別須為 [團體代號+卡種]");
      return -1;
    }
    if ((isItemNo.equals("01") || isItemNo.equals("02")) && isKeyType.equals("2")) {
      alertErr("資料類別須為 [團體代號]或[團體代號+卡種]");
      return -1;
    }
    wp.colSet("tt_item_no",
        commString.decode(exItemNo, ",01,02,03,04,05", ",紅利積點,現金回饋,卡片成本,公共運輸旅平險,國際組織白金秘書"));
    wp.colSet("item_no", exItemNo);
    exItemNo = wp.itemStr("ex_key_type");
    wp.colSet("key_type", exItemNo);
    wp.colSet("temp_flag", "Y");
    wp.colSet("cost_amt", "0");
    exKeyType = wp.itemStr("ex_key_type");
    exItemNo = wp.itemStr("ex_item_no");
    listWkdata3();
    return 1;
  }

  void listWkdata3() {

    if (exKeyType.equals("1")) {
      wp.colSet("kk_card_type_none", "none");
    }
    if (exKeyType.equals("2")) {
      wp.colSet("kk_group_code_none", "none");
    }
    if (exItemNo.equals("01") || exItemNo.equals("02")) {
      wp.colSet("cost_amt_none", "none");
    } else {
      wp.colSet("item_ename_none", "none");
    }
  }

}
