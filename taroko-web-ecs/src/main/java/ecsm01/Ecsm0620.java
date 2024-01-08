/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-06-21  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package ecsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0620 extends BaseEdit {
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0620Func func = null;
  String rowid;

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } 

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " 
    		+ sqlCol(wp.itemStr("ex_shell_id"), "shell_id", "%like%")
    		+ sqlCol(wp.itemStr("ex_schedule_job_id"), "schedule_job_id", "%like%")
    		+ sqlCol(wp.itemStr("ex_pgm_id"), "pgm_id", "%like%")
    		;


    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = "hex(rowid) as rowid, " 
        + "shell_id," + "schedule_job_id," + "priority_level," + "pgm_id," + "pgm_desc,"
        + "normal_code," + "call_duty_ind" 
        + ",wait_flag, key_parm1, key_parm2, key_parm3, key_parm4, key_parm5 "
        ;

    wp.daoTable = "ecs_batch_setting";
    wp.whereOrder = "order by shell_id,priority_level";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    rowid = itemKk("data_k1");
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid," 
        + "shell_id," + "schedule_job_id," + "priority_level," + "pgm_id,"
        + "pgm_desc," + "key_parm1," + "key_parm2," + "key_parm3," + "key_parm4,"
        + "key_parm5," + "wait_flag," + "to_char(mod_time,'yyyymmdd') as mod_time,"
        + "mod_user," + "repeat_code," + "normal_code," + "call_duty_ind" ;

    wp.daoTable = "ecs_batch_setting" ;
    wp.whereStr = "where 1=1 ";
    wp.whereStr = wp.whereStr + sqlRowId(rowid, "rowid");
    

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]");
      return;
    }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    ecsm01.Ecsm0620Func func = new ecsm01.Ecsm0620Func(wp);
    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {}

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class
