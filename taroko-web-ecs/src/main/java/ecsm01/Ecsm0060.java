/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0060 extends BaseEdit
{
  private String progname = "MIS報表參數說明維護處理程式109/12/28 V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0060Func func = null;
  String rowid;
  String orgTabName = "ptr_rpt_parm";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];

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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_parm_pgm"), "a.parm_pgm", "like%");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.parm_pgm," + "a.main_dept," + "b.dept_name," + "a.parm_dept," + "a.parm_name,"
        + "a.ps," + "a.crt_date," + "a.crt_user";

    wp.daoTable = controlTabName + " a " + "LEFT OUTER JOIN ptr_dept_code b "
        + "ON a.main_dept = b.dept_code ";
    wp.whereOrder = " " + " order by a.parm_pgm";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }



    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    rowid = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (qFrom == 0)
      if (wp.itemStr("kk_parm_pgm").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.parm_pgm as parm_pgm," + "a.main_dept," + "a.parm_dept," + "a.q1," + "a.a1,a.pwd1,"
        + "a.q2," + "a.a2,a.pwd2," + "a.q3," + "a.a3,a.pwd3," + "a.q4," + "a.a4,a.pwd4," + "a.q5,"
        + "a.a5,a.pwd5," + "a.q6," + "a.a6,a.pwd6," + "a.q7," + "a.a7,a.pwd7," + "a.q8,"
        + "a.a8,a.pwd8," + "a.q9," + "a.a9,a.pwd9," + "a.q10," + "a.a10,a.pwd10," + "a.q11,"
        + "a.a11,a.pwd11," + "a.q12," + "a.a12,a.pwd12," + "a.q13," + "a.a13,a.pwd13," + "a.q14,"
        + "a.a14,a.pwd14," + "a.q15," + "a.a15,a.pwd15," + "a.ps," + "a.crt_date," + "a.crt_user,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_user," + "a.apr_date,"
        + "a.apr_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_parm_pgm"), "a.parm_pgm");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]");
      return;
    }
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    ecsm01.Ecsm0060Func func = new ecsm01.Ecsm0060Func(wp);

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
  public void dddwSelect() {
    try {
      if ((wp.respHtml.equals("ecsm0060_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_parm_pgm").length() > 0) {
          wp.optionKey = wp.colStr("kk_parm_pgm");
          wp.initOption = "";
        }
        if (wp.colStr("parm_pgm").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_parm_pgme", "ptr_rpt_parm", "trim(parm_pgm)", "trim(parm_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("ecsm0060"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_parm_pgm").length() > 0) {
          wp.optionKey = wp.colStr("ex_parm_pgm");
        }
        this.dddwList("dddw_parm_pgme", "ptr_rpt_parm", "trim(parm_pgm)", "trim(parm_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    if (wp.colStr("pwd1").equals("Y")) {
      wp.colSet("pwd_a1", "password");
    }

    if (wp.colStr("pwd2").equals("Y")) {
      wp.colSet("pwd_a2", "password");
    }

    if (wp.colStr("pwd3").equals("Y")) {
      wp.colSet("pwd_a3", "password");
    }

    if (wp.colStr("pwd4").equals("Y")) {
      wp.colSet("pwd_a4", "password");
    }

    if (wp.colStr("pwd5").equals("Y")) {
      wp.colSet("pwd_a5", "password");
    }

    if (wp.colStr("pwd6").equals("Y")) {
      wp.colSet("pwd_a6", "password");
    }

    if (wp.colStr("pwd7").equals("Y")) {
      wp.colSet("pwd_a7", "password");
    }

    if (wp.colStr("pwd8").equals("Y")) {
      wp.colSet("pwd_a8", "password");
    }

    if (wp.colStr("pwd9").equals("Y")) {
      wp.colSet("pwd_a9", "password");
    }

    if (wp.colStr("pwd10").equals("Y")) {
      wp.colSet("pwd_a10", "password");
    }

    if (wp.colStr("pwd11").equals("Y")) {
      wp.colSet("pwd_a11", "password");
    }

    if (wp.colStr("pwd12").equals("Y")) {
      wp.colSet("pwd_a12", "password");
    }

    if (wp.colStr("pwd13").equals("Y")) {
      wp.colSet("pwd_a13", "password");
    }

    if (wp.colStr("pwd14").equals("Y")) {
      wp.colSet("pwd_a14", "password");
    }

    if (wp.colStr("pwd15").equals("Y")) {
      wp.colSet("pwd_a15", "password");
    }

    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class
