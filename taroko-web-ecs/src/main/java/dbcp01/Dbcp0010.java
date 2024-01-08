/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-25  V1.00.00  yash            program initial                            *
* 109-01-03  V1.00.01  Justin Wu  updated for archit.  change *
*  109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*         
*  109-12-23  V1.00.03   Justin       parameterize sql               
******************************************************************************/

package dbcp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Dbcp0010 extends BaseEdit {
  String cardCode = "";
  String digitalFlag = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        insertFunc();
        break;
      case "U":
        /* 更新功能 */
        updateFunc();
        break;
      case "D":
        /* 刪除功能 */
        deleteFunc();
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
      case "AJAX":
        // AJAX 20200102 updated for archit. change
        itemchanged();
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_card_code")) == false) {
      wp.whereStr += " and  card_code = :ex_card_code ";
      setString("ex_card_code", wp.itemStr("ex_card_code"));
    }

    if (empty(wp.itemStr("ex_digital_flag")) == false) {
      wp.whereStr += " and  digital_flag = :ex_digital_flag ";
      setString("ex_digital_flag", wp.itemStr("ex_digital_flag"));
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

    wp.selectSQL = " card_code" + ", card_type" + ", group_code" + ", name" + ", source_code"
        + ", unit_code" + ", digital_flag";

    wp.daoTable = "dbc_card_type";
    wp.whereOrder = " order by card_code";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    cardCode = wp.itemStr("card_code");
    digitalFlag = wp.itemStr("digital_flag");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    cardCode = wp.itemStr("kk_card_code");
    digitalFlag = wp.itemStr("kk_digital_flag");
    if (empty(cardCode)) {
      cardCode = itemKk("data_k1");
    }

    if (empty(cardCode)) {
      cardCode = wp.colStr("card_code");
    }

    if (empty(digitalFlag)) {
      digitalFlag = itemKk("data_k2");
    }

    if (empty(digitalFlag)) {
      digitalFlag = wp.colStr("digital_flag");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", card_code " + ", card_type "
        + ", digital_flag" + ", group_code" + ", name" + ", source_code" + ", unit_code"
        + ", digital_flag" + ", crt_date" + ", crt_user";
    wp.daoTable = "dbc_card_type";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  card_code = :card_code ";
    setString("card_code", cardCode);
    wp.whereStr += " and  digital_flag = :digital_flag ";
    setString("digital_flag", digitalFlag);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, card_code=" + cardCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    Dbcp0010Func func = new Dbcp0010Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    setSelectLimit(0);
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("card_type");
        this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
            "where  1=1  order by card_type");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("group_code");;
        this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
            " where  1=1  order by group_code");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("source_code");
        dddwList("dddw_source_code", "ptr_src_code", "source_code", "source_name",
            "where 1=1 order by source_code ");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("unit_code");
        setString(wp.colStr("card_type"));
        setString( wp.colStr("group_code"));
        dddwList("dddw_unit_code", "ptr_group_card_dtl", "unit_code", "",
            "where 1=1 and card_type= ? and group_code= ?  order by unit_code");
      }

    } catch (Exception ex) {
    }
  }


  public int itemchanged() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    String idCode = "";
    String cardType = "", groupCode = "";
    String dddwWhere = "";
    String option = "";

    idCode = wp.itemStr("idCode");

    switch (idCode) {
      case "1":
        cardType = wp.itemStr("a_cardType");
        dddwWhere = " and card_type = :card_type ";
        if (!empty(wp.itemStr("group_code"))) {
          dddwWhere += " and group_code = :group_code ";
          setString("group_code", wp.itemStr("group_code"));
        }

        String ls_sql = "select unit_code " + " from ptr_group_card_dtl " + " where 1=1 "
            + dddwWhere + " order by unit_code ";
        setString("card_type", cardType);

        sqlSelect(ls_sql);
        if (sqlRowNum <= 0) {
          break;
        }
        option += "<option value=\"\">--</option>";
        for (int ii = 0; ii < sqlRowNum; ii++) {
          option += "<option value=\"" + sqlStr(ii, "unit_code") + "\">" + sqlStr(ii, "unit_code")
              + "</option>";
        }
        wp.addJSON("dddw_unit_code", option);
        break;

      case "2":
        groupCode = wp.itemStr("a_groupCode");
        dddwWhere = " and group_code = :group_code ";
        if (!empty(wp.itemStr("card_type"))) {
          dddwWhere += " and card_type = :card_type ";
          setString("card_type", wp.itemStr("card_type"));
        }

        String ls_sql2 = "select unit_code " + " from ptr_group_card_dtl " + " where 1=1 "
            + dddwWhere + " order by unit_code ";
        setString("group_code", groupCode);

        sqlSelect(ls_sql2);
        if (sqlRowNum <= 0) {
          break;
        }
        option += "<option value=\"\">--</option>";
        for (int ii = 0; ii < sqlRowNum; ii++) {
          option += "<option value=\"" + sqlStr(ii, "unit_code") + "\">" + sqlStr(ii, "unit_code")
              + "</option>";
        }
        wp.addJSON("dddw_unit_code", option);
        break;

    }
    return 1;
  }

}
