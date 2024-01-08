/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-22  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/

package ptrm02;



import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0777 extends BaseEdit {
  String mExProgramCode = "";

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
  public void initPage() {
    wp.colSet("ex_user_id", wp.loginUser);
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";


    if (empty(wp.itemStr("ex_program_code")) == false) {
      wp.whereStr += " and  program_code like :program_code ";
      setString("program_code", wp.itemStr("ex_program_code") + "%");
    }

    if (empty(wp.itemStr("ex_user_id")) == false) {
      wp.whereStr += " and  user_id = :user_id ";
      setString("user_id", wp.itemStr("ex_user_id"));
    }

    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and  start_date >= :date1 ";
      setString("date1", wp.itemStr("ex_date1"));
    }

    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and  start_date <= :date2 ";
      setString("date2", wp.itemStr("ex_date2"));
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

    wp.selectSQL = " batch_seqno" + ", program_code" + ", r_program_code"
        + ", decode(r_program_code,'','010101',execute_time_e) as temp_time_e " + ", start_date"
        + ", start_time" + ", execute_date_s" + ", execute_time_s" + ", execute_date_e"
        + ", execute_time_e" + ", error_code" + ", error_desc" + ", proc_desc" + ", proc_mark"
        + ", proc_mark1" + ", proc_mark2" + ", user_id" + ", workstation_name" + ", client_program"
        + ", parameter1" + ", parameter2" + ", parameter_data";

    wp.daoTable = "ptr_callbatch";
    wp.whereOrder = " order by start_date desc,start_time desc,temp_time_e desc ,program_code desc";
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
    mExProgramCode = wp.itemStr("program_code");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExProgramCode = wp.itemStr("kk_program_code");
    if (empty(mExProgramCode)) {
      mExProgramCode = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", program_code " + ", xxx" + ", crt_date"
        + ", crt_user";
    wp.daoTable = "ptr_callbatch";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  program_code = :program_code ";
    setString("program_code", mExProgramCode);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, program_code=" + mExProgramCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // Ptrm0777_func func = new Ptrm0777_func(wp);
    //
    // rc = func.dbSave(is_action);
    // ddd(func.getMsg());
    // if (rc != 1) {
    // err_alert(func.getMsg());
    // }
    // this.sql_commit(rc);
  }

  @Override
  public void initButton() {

    if (empty(strAction)) {
      wp.colSet("ex_date1", wp.sysDate);
    }

    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }


  @Override
  public void dddwSelect() {
    try {

      // wp.initOption="--";
      // wp.optionKey = wp.item_ss("ex_program_code");
      // this.dddw_list("dddw_program_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where
      // WF_TYPE='CALLBATCH' order by wf_id");
      //
      // wp.initOption="--";
      // wp.optionKey = wp.item_ss("ex_user_id");
      // this.dddw_list("dddw_user_id", "sec_user ", "usr_id", "usr_cname", "where 1=1 order by
      // usr_id");
    } catch (Exception ex) {
    }
  }

}
