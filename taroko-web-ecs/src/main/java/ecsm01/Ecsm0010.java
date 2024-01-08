/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/26  V1.00.01   Allen Ho      Initial                              *
*  109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                           
 * 109-12-30  V1.00.05  shiyuqi       修改无意义命名                                                                                     *                                             *
***************************************************************************/
package ecsm01;

import ecsm01.Ecsm0010Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0010 extends BaseEdit {
  private String progname = "系統參考IP位址維護處理程式108/12/26 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0010Func func = null;
  String rowid;
  String orgTabName = "ecs_ref_ip_addr";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_ref_ip_code"), "a.ref_ip_code", "like%")
        + sqlCol(wp.itemStr("ex_ref_ip"), "a.ref_ip", "like%");

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
        + "a.ref_ip_code," + "a.ref_ip," + "a.ref_name," + "a.trans_type," + "a.remote_dir,"
        + "a.local_dir," + "a.port_no," + "a.crt_date," + "a.crt_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.ref_ip_code,a.ref_ip";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commTransType("comm_trans_type");

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
      if (wp.itemStr("kk_ref_ip_code").length() == 0) {
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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.ref_ip_code as ref_ip_code," + "a.ref_ip," + "a.ref_name," + "a.ftp_type,"
        + "a.user_id," + "a.trans_type," + "a.remote_dir," + "a.local_dir," + "a.port_no,"
        + "a.crt_date," + "a.crt_user," + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
        + "a.mod_user," + "a.apr_date," + "a.apr_user," + "a.hide_ref_code," + "a.user_hidewd,"
        + "a.file_zip_hidewd," + "a.file_unzip_hidewd";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_ref_ip_code"), "a.ref_ip_code");
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

    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

    if (wp.colStr("user_hidewd").length() != 0) {
      wp.colSet("t_user_hidewd",
          comm.hideUnzipData(wp.colStr("user_hidewd"), wp.colStr("hide_ref_code")));
      wp.colSet(0, "apr_pwd", wp.colStr("t_user_hidewd"));
    }

    if (wp.colStr("file_zip_hidewd").length() != 0) {
      wp.colSet("t_file_zip_hidewd",
          comm.hideUnzipData(wp.colStr("file_zip_hidewd"), wp.colStr("hide_ref_code")));
      wp.colSet(0, "apr_1_pwd", wp.colStr("t_file_zip_hidewd"));
    }

    if (wp.colStr("file_unzip_hidewd").length() != 0) {
      wp.colSet("t_file_unzip_hidewd",
          comm.hideUnzipData(wp.colStr("file_unzip_hidewd"), wp.colStr("hide_ref_code")));
      wp.colSet(0, "apr_2_pwd", wp.colStr("t_file_unzip_hidewd"));
    }



  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    ecsm01.Ecsm0010Func func = new ecsm01.Ecsm0010Func(wp);

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
  public void commTransType(String cde1) throws Exception {
    String[] cde = {"B", "A"};
    String[] txt = {"BINARY", "ASCII"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt1 = cde1.substring(5, cde1.length());
        if (wp.colStr(ii, txt1).equals(cde[inti])) {
          wp.colSet(ii, cde1, txt[inti]);
          break;
        }
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
