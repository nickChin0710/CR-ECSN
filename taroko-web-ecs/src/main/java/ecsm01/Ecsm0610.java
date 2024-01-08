/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110-09-07  V1.00.01   Justin        Initial                              *
* 111-07-27  V1.00.02  Ryan           mod_pgm -> mod_pgm1                  *
***************************************************************************/
package ecsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0610 extends BaseEdit {
  private String progname = "批次排程及執行結果查詢 111/07/27 V1.00.02";
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
	} else if (eqIgno(wp.buttonCode, "AJAX")) {
		// -AJAX-
//		wfAjaxKey(wp);
	}

    dddwSelect();
    initButton();
  }

//************************************************************************

//	private void wfAjaxKey(TarokoCommon wr) {
//		super.wp = wr;
//
//        try {
//        	// get a list of PGM_IDs
//            wp.initOption = "--";
//            wp.optionKey = "";
////            if (wp.colStr("ex_pgm_id").length() > 0) {
////              wp.optionKey = wp.colStr("ex_pgm_id");
////            }
//            setString(wp.itemStr("ex_shell_id"));
//			this.dddwList("dddw_pgm_id", "ECS_BATCH_CTL", "DISTINCT trim(PGM_ID)", "", "where SHELL_ID = ? ");
//			wp.addJSON("ajax_dddw_pgm_id", wp.colStr("dddw_pgm_id"));
//		} catch (Exception e) {
//			
//		}
//        
//
//	}

// ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() == false)
      return;
    
    wp.whereStr = "WHERE 1=1 "
        + sqlStrend(wp.itemStr("ex_batch_date_s"), wp.itemStr("ex_batch_date_e"), "a.batch_date")
        + sqlCol(wp.itemStr("ex_shell_id"), "a.shell_id")
        + sqlCol(wp.itemStr("ex_pgm_id"), "a.pgm_id");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {

    wp.pageControl();
    
    StringBuffer sb = new StringBuffer();
    
    sb.append("hex(a.rowid) as rowid, ");
    sb.append("a.BATCH_DATE, ");
    sb.append("a.BATCH_TIME, ");
    sb.append("a.SHELL_ID, ");
    sb.append("a.PGM_ID, ");
    sb.append("a.PGM_DESC, ");
    sb.append("a.PRIORITY_LEVEL, ");
    sb.append("a.WAIT_FLAG, ");
    sb.append("a.EXECUTE_DATE_S, ");
    sb.append("a.EXECUTE_TIME_S, ");
    sb.append("a.EXECUTE_DATE_E, ");
    sb.append("a.EXECUTE_TIME_E, ");
    sb.append("a.PROC_FLAG, ");
    sb.append("a.PROC_DESC ");

    wp.selectSQL = sb.toString();
    wp.daoTable = "ECS_BATCH_CTL a";
    wp.whereOrder = " order by BATCH_DATE desc, SHELL_ID, priority_level, PGM_ID";

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
	StringBuffer sb = new StringBuffer();
	sb.append("hex(a.rowid) as rowid, ");
	sb.append("a.BATCH_DATE, ");
	sb.append("a.BATCH_TIME, ");
	sb.append("a.SHELL_ID, ");
	sb.append("a.PGM_ID, ");
	sb.append("a.PGM_DESC, ");
	sb.append("a.EXECUTE_DATE_S, ");
	sb.append("a.EXECUTE_TIME_S, ");
	sb.append("a.EXECUTE_DATE_E, ");
	sb.append("a.EXECUTE_TIME_E, ");
	sb.append("a.WAIT_FLAG, ");
	sb.append("a.PROC_FLAG, ");
	sb.append("a.PROC_DESC, ");
	sb.append("a.KEY_PARM1, ");
	sb.append("a.KEY_PARM2, ");
	sb.append("a.KEY_PARM3, ");
	sb.append("a.KEY_PARM4, ");
	sb.append("a.KEY_PARM5, ");
	sb.append("a.NORMAL_CODE, ");
	sb.append("a.CALL_DUTY_IND, ");
	sb.append("to_char(a.MOD_TIME , 'YYYY/MM/DD HH24:MI:SS') as mod_time, "); 
	sb.append("a.MOD_PGM as MOD_PGM1, ");
	sb.append("a.RERUN_PROC ");

    wp.selectSQL = sb.toString();

    wp.daoTable = "ECS_BATCH_CTL a ";
    
    rowid = (rowid == null || rowid.isEmpty()) ? wp.itemStr("rowid") : rowid;
    if (rowid == null || rowid.isEmpty()) {
    	alertErr2("無法查詢資料，無rowid");
        return;
	}
    
    wp.whereStr = "where 1=1 ";
    wp.whereStr += sqlRowId(rowid, "a.rowid");

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key[" + rowid + "]");
      return;
    }
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    //if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
    //  return;

    ecsm01.Ecsm0610Func func = new ecsm01.Ecsm0610Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
    querySelect();
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
  public void dddwSelect() {
    try {
      if ((wp.respHtml.equals("ecsm0610"))) {
    	
    	// get a list of SHELL_IDs
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_shell_id").length() > 0) {
          wp.optionKey = wp.colStr("ex_shell_id");
        }
        this.dddwList("dddw_shell_id", "ECS_BATCH_CTL", "DISTINCT trim(SHELL_ID)", "", "");
        
     // get a list of SHELL_IDs
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_pgm_id").length() > 0) {
          wp.optionKey = wp.colStr("ex_pgm_id");
        }
        this.dddwList("dddw_pgm_id", "ECS_BATCH_CTL", "DISTINCT trim(PGM_ID)", "", "");
        
        
        
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public boolean queryCheck() throws Exception {
    if ((itemKk("ex_shell_id").length() == 0) && 
    	(itemKk("ex_pgm_id").length() == 0) && 
    	(itemKk("ex_batch_date_s").length() == 0)) {
      alertErr2("請輸入查詢條件[批次日期/批次日期/程式代碼]");
      return false;
    }
    return true;
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class
