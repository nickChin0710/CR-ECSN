/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-01  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ptrm0100 extends BaseEdit {
  Ptrm0100Func func;

  String sourceCode = "";
 // String kk2 = "";

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
    }

    dddwSelect();
    initButton();
  }


  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("ex_source_code"), "source_code", "like%");
    // if(empty(wp.item_ss("ex_source_code"))==false){
    // wp.whereStr += " and source_code like :source_code ";
    // setString("source_code", wp.item_ss("ex_source_code")+"%");
    // }
    //

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

    wp.selectSQL = " source_code, " + " source_name, " + "third_data_reissue,"
        + "not_auto_installment," + " crt_date, " + " crt_user, " + " mod_time, " + " mod_user ";

    wp.daoTable = "ptr_src_code";
    wp.whereOrder = " order by source_code";
    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    sourceCode = wp.itemStr("card_type");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    sourceCode = wp.itemStr("KK_source_code");
    if (empty(sourceCode)) {
      sourceCode = itemKk("data_k1");

    }

    if (empty(sourceCode)) {
      sourceCode = wp.colStr("source_code");

    }


    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + " source_code, " + " source_name, "
        + " third_data_reissue," + " not_auto_installment," + " channel_code," + " crt_date,"
        + " crt_user," + " uf_2ymd(mod_time) as mod_date," + " mod_user";
    wp.daoTable = "ptr_src_code";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  source_code = :source_code ";
    setString("source_code", sourceCode);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + sourceCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    if (strAction.equals("A")) {
      if (empty(wp.itemStr("kk_source_code"))) {
        errmsg("來源代碼不可空白!");
        return;

      }

      String lsSql = "select * from ptr_group_code where group_abbr_code =:group_abbr_code ";
      setString("group_abbr_code", wp.itemStr("kk_source_code").substring(0, 2));
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        errmsg("無此團體代號簡碼!");
        return;
      }
    }


    func = new Ptrm0100Func(wp);


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
    try {
      // wp.optionKey = wp.item_ss("bin_no");
      // this.dddw_list("dddw_bin_no","ptr_bintable","bin_no","","where 1=1");
    } catch (Exception ex) {
    }
  }

}
