/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-29  V1.00.02  Tanwei       updated for project coding standard
* 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package smsm01;

import smsm01.Smsp0035Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsp0035 extends BaseProc {
  private String PROGNAME = "簡訊內容重新發送處理處理程式108/11/22 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  smsm01.Smsp0035Func func = null;
  String rowid;
  String orgTabName = "sms_msg_dtl";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
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
    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
      strAction = "A";
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 "
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
        + sqlCol(wp.itemStr("ex_add_mode"), "a.add_mode", "like%")
        + sqlCol(wp.itemStr("ex_msg_dept"), "a.msg_dept", "like%")
        + sqlCol(wp.itemStr("ex_msg_id"), "a.msg_id", "like%")        
        + sqlCol(wp.itemStr("ex_cellar_phone"), "a.cellar_phone", "like%");
    
    if(wp.itemEmpty("ex_id_no")==false) {
    	wp.whereStr += sqlCol(wp.colStr("ex_id_p_seqno"),"id_p_seqno");
    }
    
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL =
        " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, " + "a.resend_flag,"
            + "'' as id_no," + "a.chi_name," + "a.cellar_phone," + "a.msg_dept," + "a.msg_pgm,"
            + "a.msg_userid," + "a.crt_date," + "a.msg_id," + "a.msg_seqno," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by crt_date desc,msg_dept";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");
    commDeptName("comm_msg_dept");


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
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.crt_user,"
        + "a.cellar_phone," + "'' as id_no," + "a.msg_seqno," + "a.chi_name," + "a.msg_userid,"
        + "a.msg_id," + "a.msg_desc," + "a.chi_name_flag," + "a.crt_user," + "a.crt_date,"
        + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr;
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]");
      return;
    }
    checkButtonOff();
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    int ii = 0;
    String sql1 = "";

    if (wp.colStr("id_p_seqno").length() != 0) {
      sql1 = "select " + " id_no as id_no, " + " chi_name as chi_name " + " from crd_idno "
          + " where id_p_seqno = ? ";
    }
    sqlSelect(sql1,new Object[] {wp.colStr("id_p_seqno")});
    wp.colSet("id_no", sqlStr("id_no"));

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    smsm01.Smsp0035Func func = new smsm01.Smsp0035Func(wp);

    String[] lsRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;

    int rr = -1;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + lsRowid[rr]);

      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      rc = func.dataProc();

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commIdNo("comm_id_no");
        commDeptName("comm_msg_dept");

        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        this.sqlCommit(rc);
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 權限問題=" + ilAuth);
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
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("smsp0035"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_msg_dept").length() > 0) {
          wp.optionKey = wp.colStr("ex_msg_dept");
        }
        this.dddwList("dddw_class_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if ((itemKk("ex_crt_date_s").length() == 0) && (itemKk("ex_msg_dept").length() == 0)
        && (itemKk("ex_msg_id").length() == 0) && (itemKk("ex_crt_date_e").length() == 0)
        && (itemKk("ex_cellar_phone").length() == 0) && (itemKk("ex_id_no").length() == 0)) {
      alertErr2("[查詢項目]不可都為空白");
      return (1);
    }

    String sql1 = "";
    if (wp.itemStr("ex_id_no").length() == 10) {
      sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no  =? and    id_no_code   = '0' ";

      sqlSelect(sql1,new Object[] {wp.itemStr("ex_id_no").toUpperCase()});
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      return (0);
    }

    return (0);
  }

  // ************************************************************************

  // ************************************************************************
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
          + " and   id_p_seqno = ? ";
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1,new Object[] {wp.colStr(ii, "id_p_seqno")});

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDeptName(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " dept_name as column_dept_name " + " from ptr_dept_code "
          + " where 1 = 1 " + " and   dept_code = ? ";
      if (wp.colStr(ii, "msg_dept").length() == 0)
        continue;
      sqlSelect(sql1,new Object[] {wp.colStr(ii, "msg_dept")});

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_dept_name");
      wp.colSet(ii, columnData1, columnData);
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
