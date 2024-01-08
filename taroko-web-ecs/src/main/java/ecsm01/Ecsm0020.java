/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
* 109-12-28  V1.00.03  Justin      parameterize sql
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package ecsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0020 extends BaseEdit
{
  private String progname = "物件權責歸屬檔維護處理程式109/12/28 V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0020Func func = null;
  String rowid, news;
  String orgTabName = "ecs_object_owner";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_obj_code"), "a.obj_code", "like%")
        + sqlCol(wp.itemStr("ex_unit_code"), "a.unit_code", "like%");

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
        + "a.unit_code, " + "a.obj_code," + "a.obj_type," + "a.obj_name," + "a.unit_code,"
        + "b.dept_name," + "a.owner_code," + "a.owner_name";

    wp.daoTable = controlTabName + " a " + "LEFT OUTER JOIN ptr_dept_code b "
        + "ON a.unit_code = b.dept_code ";
    wp.whereOrder = " " + " order by a.obj_code,a.unit_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commObjType("comm_obj_type");


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
      if (wp.itemStr("kk_obj_code").length() == 0) {
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
        + "a.obj_code as obj_code," + "a.obj_type as obj_type," + "a.obj_name," + "a.unit_code,"
        + "a.owner_code," + "a.owner_name," + "a.owner_tel1," + "a.program_code,"
        + "a.obj_comment1," + "a.obj_comment2," + "a.obj_comment3," + "a.crt_date," + "a.crt_user,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_obj_code"), "a.obj_code")
          + sqlCol(wp.itemStr("kk_obj_type"), "a.obj_type");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]" + "[" + news + "]");
      return;
    }
    commObjType("comm_obj_type");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    ecsm01.Ecsm0020Func func = new ecsm01.Ecsm0020Func(wp);

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
      if ((wp.respHtml.equals("ecsm0020_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("kk_obj_type").length() > 0) {
          wp.optionKey = wp.colStr("kk_obj_type");
          wp.initOption = "";
        }
        if (wp.colStr("obj_type").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_obj_type_1", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='ECSOBJTYPE'");
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("unit_code").length() > 0) {
          wp.optionKey = wp.colStr("unit_code");
          wp.initOption = "";
        }
        if (wp.colStr("unit_code").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("ecsm0020"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_unit_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_unit_code");
        }
        this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void commObjType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_id = ? and   wf_type = 'ECSOBJTYPE' ";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "obj_type")});

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
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
