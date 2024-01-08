/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-22  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *                                                                            *
******************************************************************************/

package ptrm02;



import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrq4010 extends BaseEdit {

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
    wp.colSet("ex_create_date2", getSysDate());
    wp.colSet("ex_send_date2", getSysDate());
  }

  // for query use only
  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_create_date1");
    String lsDate2 = wp.itemStr("ex_create_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔日期-起迄]  輸入錯誤");
      return -1;
    }

    lsDate1 = wp.itemStr("ex_send_date1");
    lsDate2 = wp.itemStr("ex_send_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[發送日期-起迄]  輸入錯誤");
      return -1;
    }

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_create_date1")) == false) {
      wp.whereStr += " and  to_char(create_time,'YYYYMMDD') >= :ex_create_date1 ";
      setString("ex_create_date1", wp.itemStr("ex_create_date1"));
    }

    if (empty(wp.itemStr("ex_create_date2")) == false) {
      wp.whereStr += " and  to_char(create_time,'YYYYMMDD') <= :ex_create_date2 ";
      setString("ex_create_date2", wp.itemStr("ex_create_date2"));
    }

    if (empty(wp.itemStr("ex_send_date1")) == false) {
      wp.whereStr += " and  to_char(send_time,'YYYYMMDD') >= :ex_send_date1 ";
      setString("ex_send_date1", wp.itemStr("ex_send_date1"));
    }

    if (empty(wp.itemStr("ex_send_date2")) == false) {
      wp.whereStr += " and  to_char(send_time,'YYYYMMDD') <= :ex_send_date2 ";
      setString("ex_send_date2", wp.itemStr("ex_send_date2"));
    }

    if (empty(wp.itemStr("ex_dept_no")) == false) {
      wp.whereStr += " and  sender_dept_no = :ex_dept_no ";
      setString("ex_dept_no", wp.itemStr("ex_dept_no"));
    }

    if (empty(wp.itemStr("ex_group_id")) == false) {
      wp.whereStr += " and  group_id = :ex_group_id ";
      setString("ex_group_id", wp.itemStr("ex_group_id"));
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
    if (getWhereStr() != 1) {
      return;
    }
    wp.pageControl();

    wp.selectSQL = " system_name" + ", pgm_name" + ", to_char(create_time,'yyyymmdd') create_time"
        + ", sender_dept_no " + ", group_id" + ", subject" + ", contents" + ", attach_filename"
        + ", to_char(send_time,'yyyymmdd') send_time" + ", status_code" + ", status_message";

    wp.daoTable = "ptr_message_hst";
    wp.whereOrder = " ";
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

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

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
