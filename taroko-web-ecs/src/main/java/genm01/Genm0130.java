/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-20  V1.00.00  yash       program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/
package genm01;


import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Genm0130 extends BaseEdit {
  Genm0130Func func;

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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1 ";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " hand_user_id, " + " hand_manager_id, " + "r6_user_id," + "r6_manager_id,"
        + " varchar_format(mod_time,'YYYYMMDD-HH24:MI:SS') as mod_date, " + " mod_user ";

    wp.daoTable = "ptr_gen_a002";
    wp.whereOrder = " order by mod_time desc";
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
    // kk1 =wp.item_ss("card_type");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = " hand_user_id, " + " hand_manager_id, " + " r6_user_id, " + " r6_manager_id ";
    wp.daoTable = "ptr_gen_a002";
    wp.whereStr =
        "where 1=1  and hand_user_id = :hand_user_id" + " and hand_manager_id = :hand_manager_id "
            + " and r6_user_id = :r6_user_id " + " and r6_manager_id = :r6_manager_id ";
    setString("hand_user_id", itemKk("data_k1"));
    setString("hand_manager_id", itemKk("data_k2"));
    setString("r6_user_id", itemKk("data_k3"));
    setString("r6_manager_id", itemKk("data_k4"));



    // kk1 =wp.item_ss("KK_card_type");
    // if (empty(kk1)){
    // kk1 = item_kk("data_k1");
    //
    // }

    // if (isEmpty(wp.item_ss("bin_no"))){
    // alert_err("BIN NO : 不可空白");
    // return;
    // }

    // wp.selectSQL ="hex(rowid) as rowid, mod_seqno, "
    // +" card_type, "
    // + "name, "
    // + "bin_no,"
    // + "card_note,"
    // +" rds_pcard,"
    // +" card_note_jcic,"
    // +" top_card_flag,"
    // +" neg_card_type,"
    // +" out_going_type,"
    // +" sort_type,"
    // +" crt_date,"
    // +" crt_user,"
    // +" uf_2ymd(mod_time) as mod_date,"
    // +" mod_user"
    // ;
    // wp.daoTable = "ptr_card_type";
    // wp.whereStr = "where 1=1";
    // wp.whereStr += " and card_type = :card_type ";
    // setString("card_type", kk1);
    //
    //
    pageSelect();
    // wp.optionKey=wp.col_ss("bin_no");
    if (sqlNotFind()) {
      // alert_err("查無資料, card_type=" + kk1);
      alertErr("查無資料");
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Genm0130Func(wp);


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
      // wp.optionKey = wp.col_ss("bin_no");
      // this.dddw_list("dddw_bin_no","ptr_bintable","bin_no","","where 1=1");
      // wp.optionKey = wp.col_ss("card_note");
      // this.dddw_list("dddw_card_note","ptr_sys_idtab","wf_id","wf_desc","where
      // wf_type='CARD_NOTE'");
    } catch (Exception ex) {
    }
  }

}
