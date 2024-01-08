/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108-12-12  V1.00.01   Allen Ho      Initial                              *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change 
* 109-04-29  V1.00.03   Tanwei       updated for project coding standard    
* 109-12-30  V1.00.04  shiyuqi       修改无意义命名                                                                                     *                                                                    *
***************************************************************************/
package smsm01;

import smsm01.Smsm0030Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsm0030 extends BaseEdit {
  private String PROGNAME = "簡訊內容迷戲檔維護處理程式";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  smsm01.Smsm0030Func func = null;
  String rowid;
  String msgSeqno;
  String fstAprFlag = "";
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
  String upGroupType = "0";

  // ************************************************************************
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
        strAction = "A";
        wp.itemSet("aud_type", "A");
        insertFunc();
        break;
      case "U":
        /* 更新功能 */
        strAction = "U3";
        updateFuncU3R();
        break;
      case "I":
        strAction = "I";
        /* 單獨新增功能 */
        /*
         * kk1 = item_kk("data_k1"); kk2 = item_kk("data_k2"); kk3 = item_kk("data_k3");
         */
        clearFunc();
        break;
      case "D":
        /* 刪除功能 */
        deleteFuncD3R();
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
      case "NILL":
        /* nothing to do */
        strAction = "";
        wp.listCount[0] = wp.itemBuff("ser_num").length;
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        switch (wp.getValue("idCode")) {
          case "1":
            wfAjaxFunc1();;
            break;
          case "2":
            wfAjaxFunc2();
            break;
        }
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    String lsIdNo = "";
    lsIdNo = wp.itemStr("ex_id_no");
    wp.whereStr = "WHERE 1=1 " 
        + sqlCol(wp.itemStr("ex_cellar_phone"), "a.cellar_phone", "like%")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")
        + sqlCol(wp.itemStr("ex_msg_dept"), "a.msg_dept", "like%")
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
        + " and add_mode  !=  'B' ";
    
    if(empty(lsIdNo)==false) {
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
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "'' as id_no," + "a.chi_name," + "a.cellar_phone," + "a.msg_dept," + "a.chi_name_flag,"
        + "a.msg_desc," + "a.crt_date," + "a.crt_user," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.cellar_phone,a.crt_user,a.msg_dept,a.crt_date";

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
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.msg_seqno as msg_seqno," + "a.cellar_phone," + "a.msg_dept," + "a.chi_name,"
        + "a.id_p_seqno," + "a.ex_id," + "a.msg_userid," + "a.msg_id," + "a.msg_desc,"
        + "a.chi_name_flag," + "a.create_txt_date," + "a.crt_user," + "a.crt_date," + "a.apr_user,"
        + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(msgSeqno, "a.msg_seqno");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
    } else {
      wp.colSet("aud_type", wp.itemStr("ex_apr_flag"));
      wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }
    checkButtonOff();
    msgSeqno = wp.colStr("msg_seqno");
    commfuncAudType("aud_type");
    dataReadR3R();
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
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.msg_seqno as msg_seqno,"
        + "a.cellar_phone as cellar_phone," + "a.msg_dept as msg_dept," + "a.chi_name as chi_name,"
        + "a.id_p_seqno as id_p_seqno," + "a.ex_id as ex_id," + "a.msg_userid as msg_userid,"
        + "a.msg_id as msg_id," + "a.msg_desc as msg_desc," + "a.chi_name_flag as chi_name_flag,"
        + "a.create_txt_date as create_txt_date," + "a.crt_user as crt_user,"
        + "a.crt_date as crt_date," + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(msgSeqno, "a.msg_seqno");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    checkButtonOff();
    commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    msgSeqno = wp.itemStr("msg_seqno");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      msgSeqno = wp.itemStr("msg_seqno");
      strAction = "D";
      deleteFunc();
      if (fstAprFlag.equals("Y")) {
        qFrom = 0;
        controlTabName = orgTabName;
      }
    } else {
      strAction = "A";
      wp.itemSet("aud_type", "D");
      insertFunc();
    }
    dataRead();
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void updateFuncU3R() throws Exception {
    qFrom = 0;
    msgSeqno = wp.itemStr("msg_seqno");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1) {
        dataReadR3R();;
        datareadWkdata();
      }
    } else {
      msgSeqno = wp.itemStr("msg_seqno");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    smsm01.Smsm0030Func func = new smsm01.Smsm0030Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nadd") > 0)) {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("btnDelete_disable", "");
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("smsm0030_nadd")) || (wp.respHtml.equals("smsm0030_detl"))) {
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("msg_dept").length() > 0) {
          wp.optionKey = wp.colStr("msg_dept");
        }
        this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)",
            " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("ex_id").length() > 0) {
          wp.optionKey = wp.colStr("ex_id");
        }
        this.dddwList("dddw_msg_ex", "sms_msg_ex", "trim(ex_id)", "trim(ex_subject)",
            " where stop_flag!='Y'");
      }
      if ((wp.respHtml.equals("smsm0030"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_msg_dept").length() > 0) {
          wp.optionKey = wp.colStr("ex_msg_dept");
        }
        this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if ((itemKk("ex_id_no").length() == 0) && (itemKk("ex_cellar_phone").length() == 0)
        && (itemKk("ex_apr_flag").equals("Y"))) {
      alertErr2("身份證與行動電話二者不可同時空白");
      return (1);
    }

    String sql1 = "";
    if (wp.itemStr("ex_id_no").length() == 10) {
      sql1 = "select id_p_seqno from crd_idno where id_no = ? and id_no_code = '0' ";

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
  void commfuncAudType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
          break;
        }
    }
  }

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
  public void wfAjaxFunc2() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change


    selectAjaxFunc20(wp.itemStr("ax_win_id_no").toUpperCase());

    if (rc != 1) {
      wp.addJSON("cellar_phone", "");
      wp.addJSON("chi_name", "");
      wp.addJSON("id_p_seqno", "");
      return;
    }

    wp.addJSON("cellar_phone", sqlStr("cellar_phone"));
    wp.addJSON("chi_name", sqlStr("chi_name"));
    wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
  }

  // ************************************************************************
  void selectAjaxFunc20(String idNo) {
	if(empty(idNo))	return ; 
    wp.sqlCmd = " select " + " a.cellar_phone as cellar_phone ," + " a.chi_name as chi_name ,"
        + " a.id_p_seqno as id_p_seqno " + " from  crd_idno a " + " where 1=1 "+sqlCol(idNo,"a.id_no");

    this.sqlSelect();
    if (sqlRowNum <= 0)
      alertErr2("身分證號[" + idNo + "]查無資料");

    return;
  }

  // ************************************************************************
  public void wfAjaxFunc1() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change


    selectAjaxFunc10(wp.itemStr("ax_win_ex_id"));

    if (rc != 1) {
      wp.addJSON("msg_userid", "");
      wp.addJSON("msg_id", "");
      wp.addJSON("msg_desc", "");
      wp.addJSON("chi_name_flag", "");
      return;
    }

    wp.addJSON("msg_userid", sqlStr("msg_userid"));
    wp.addJSON("msg_id", sqlStr("msg_id"));
    wp.addJSON("msg_desc", sqlStr("msg_desc"));
    wp.addJSON("chi_name_flag", sqlStr("chi_name_flag"));
  }

  // ************************************************************************
  void selectAjaxFunc10(String exId) {
	if(empty(exId))	return ;
    wp.sqlCmd = " select " + " a.msg_userid as msg_userid ," + " a.msg_id as msg_id ,"
        + " a.ex_desc as msg_desc ," + " a.chi_name_flag as chi_name_flag " + " from  sms_msg_ex a "
        + " where 1=1 "+sqlCol(exId,"a.ex_id");

    this.sqlSelect();
    if (sqlRowNum <= 0)
      alertErr2("簡訊範例[" + exId + "]查無資料");

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
