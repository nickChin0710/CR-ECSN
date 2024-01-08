/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-29  V1.00.02  Tanwei       updated for project coding standard
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
***************************************************************************/
package smsm01;

import smsm01.Smsp0030Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsp0030 extends BaseProc {
  private String PROGNAME = "簡訊內容明細檔覆核處理程式108/11/22 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  smsm01.Smsp0030Func func = null;
  String rowid, kk2;
  String km1, msgSeqno;
  String fstAprFlag = "";
  String orgTabName = "sms_msg_dtl_t";
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
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%");
    
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

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.aud_type," + "'' as id_no," + "a.chi_name," + "a.cellar_phone," + "a.msg_dept,"
        + "a.chi_name_flag," + "a.crt_date," + "a.crt_user," + "a.msg_seqno," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.crt_date,a.crt_user";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");
    commDeptName("comm_msg_dept");

    commfuncAudType("aud_type");

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
      if (wp.itemStr("kk_id_no").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.msg_seqno as msg_seqno," + "a.crt_user," + "a.cellar_phone," + "a.chi_name,"
        + "a.msg_dept," + "a.msg_userid," + "a.msg_id," + "a.msg_desc," + "a.chi_name_flag,"
        + "a.id_p_seqno," + "a.add_mode," + "a.cellphone_check_flag";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
//    if (qFrom == 0) {
//      wp.whereStr = wp.whereStr + sqlCol(km1, "a.''") + sqlCol(km2, "a.msg_seqno");
//    } else 
    if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commDeptName("comm_msg_dept");
    checkButtonOff();
    km1 = wp.colStr("''");
    msgSeqno = wp.colStr("msg_seqno");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A"))
      dataReadR3R();
    else {
      commfuncAudType("aud_type");
      listWkdataSpace();
    }
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    int ii = 0;
    String sql1 = "";

    if (wp.colStr("id_p_seqno").length() != 0) {
      sql1 = "select " + " id_no as id_no, " + " chi_name as chi_name " + " from crd_idno "
          + " where id_p_seqno = ?";
    }
    sqlSelect(sql1,new Object[] {wp.colStr("id_p_seqno")});
    wp.colSet("id_no", sqlStr("id_no"));

  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "sms_msg_dtl";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.msg_seqno as msg_seqno," + "a.crt_user as bef_crt_user,"
        + "a.cellar_phone as bef_cellar_phone," + "a.chi_name as bef_chi_name,"
        + "a.msg_dept as bef_msg_dept," + "a.msg_userid as bef_msg_userid,"
        + "a.msg_id as bef_msg_id," + "a.msg_desc as bef_msg_desc,"
        + "a.chi_name_flag as bef_chi_name_flag," + "a.id_p_seqno as bef_id_p_seqno,"
        + "a.add_mode as bef_add_mode," + "a.cellphone_check_flag as bef_cellphone_check_flag";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(msgSeqno, "a.msg_seqno");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commDeptName("comm_msg_dept");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("cellar_phone").equals(wp.colStr("bef_cellar_phone")))
      wp.colSet("opt_cellar_phone", "Y");

    if (!wp.colStr("chi_name").equals(wp.colStr("bef_chi_name")))
      wp.colSet("opt_chi_name", "Y");

    if (!wp.colStr("msg_dept").equals(wp.colStr("bef_msg_dept")))
      wp.colSet("opt_msg_dept", "Y");
    commDeptName("comm_msg_dept");
    commDeptName("comm_bef_msg_dept", 1);

    if (!wp.colStr("msg_userid").equals(wp.colStr("bef_msg_userid")))
      wp.colSet("opt_msg_userid", "Y");

    if (!wp.colStr("msg_id").equals(wp.colStr("bef_msg_id")))
      wp.colSet("opt_msg_id", "Y");

    if (!wp.colStr("msg_desc").equals(wp.colStr("bef_msg_desc")))
      wp.colSet("opt_msg_desc", "Y");

    if (!wp.colStr("chi_name_flag").equals(wp.colStr("bef_chi_name_flag")))
      wp.colSet("opt_chi_name_flag", "Y");

    if (!wp.colStr("id_p_seqno").equals(wp.colStr("bef_id_p_seqno")))
      wp.colSet("opt_id_p_seqno", "Y");

    if (!wp.colStr("add_mode").equals(wp.colStr("bef_add_mode")))
      wp.colSet("opt_add_mode", "Y");

    if (!wp.colStr("cellphone_check_flag").equals(wp.colStr("bef_cellphone_check_flag")))
      wp.colSet("opt_cellphone_check_flag", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("cellar_phone", "");
      wp.colSet("chi_name", "");
      wp.colSet("msg_dept", "");
      wp.colSet("msg_userid", "");
      wp.colSet("msg_id", "");
      wp.colSet("msg_desc", "");
      wp.colSet("chi_name_flag", "");
      wp.colSet("id_p_seqno", "");
      wp.colSet("add_mode", "");
      wp.colSet("cellphone_check_flag", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("cellar_phone").length() == 0)
      wp.colSet("opt_cellar_phone", "Y");

    if (wp.colStr("chi_name").length() == 0)
      wp.colSet("opt_chi_name", "Y");

    if (wp.colStr("msg_dept").length() == 0)
      wp.colSet("opt_msg_dept", "Y");

    if (wp.colStr("msg_userid").length() == 0)
      wp.colSet("opt_msg_userid", "Y");

    if (wp.colStr("msg_id").length() == 0)
      wp.colSet("opt_msg_id", "Y");

    if (wp.colStr("msg_desc").length() == 0)
      wp.colSet("opt_msg_desc", "Y");

    if (wp.colStr("chi_name_flag").length() == 0)
      wp.colSet("opt_chi_name_flag", "Y");

    if (wp.colStr("id_p_seqno").length() == 0)
      wp.colSet("opt_id_p_seqno", "Y");

    if (wp.colStr("add_mode").length() == 0)
      wp.colSet("opt_add_mode", "Y");

    if (wp.colStr("cellphone_check_flag").length() == 0)
      wp.colSet("opt_cellphone_check_flag", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    smsm01.Smsp0030Func func = new smsm01.Smsp0030Func(wp);

    String[] lsIdNo = wp.itemBuff("id_no");
    String[] lsMsgSeqno = wp.itemBuff("msg_seqno");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] lsCrtUser = wp.itemBuff("crt_user");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + lsRowid[rr]);

      wp.colSet(rr, "ok_flag", "-");
      if (lsCrtUser[rr].equals(wp.loginUser)) {
        ilAuth++;
        wp.colSet(rr, "ok_flag", "F");
        continue;
      }

      func.varsSet("id_no", lsIdNo[rr]);
      func.varsSet("msg_seqno", lsMsgSeqno[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A"))
        rc = func.dbInsertA4();
      else if (lsAudType[rr].equals("U"))
        rc = func.dbUpdateU4();
      else if (lsAudType[rr].equals("D"))
        rc = func.dbDeleteD4();

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commIdNo("comm_id_no");
        commDeptName("comm_msg_dept");
        commfuncAudType("aud_type");

        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        func.dbDelete();
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
  public void dddwSelect() {}

  // ************************************************************************
  public int queryCheck() throws Exception {
    String sql1 = "";
    if (wp.itemStr("ex_id_no").length() == 10) {
      sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no  = ? and    id_no_code   = '0' ";

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
  public void commDeptName(String name) throws Exception {
    commDeptName(name, 0);
    return;
  }

  // ************************************************************************
  public void commDeptName(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " dept_name as column_dept_name " + " from ptr_dept_code "
          + " where 1 = 1 " + " and   dept_code = ? ";
      if (wp.colStr(ii, befStr + "msg_dept").length() == 0)
        continue;
      sqlSelect(sql1,new Object[] {wp.colStr(ii, befStr + "msg_dept")});

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_dept_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commIdNo(String idno) throws Exception {
    commIdNo(idno, 0);
    return;
  }

  // ************************************************************************
  public void commIdNo(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
          + " and   id_p_seqno = ? ";
      if (wp.colStr(ii, befStr + "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1,new Object[] {wp.colStr(ii, befStr + "id_p_seqno")});

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
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
