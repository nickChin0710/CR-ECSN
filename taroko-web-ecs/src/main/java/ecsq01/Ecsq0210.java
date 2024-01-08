/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/09  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱* 
* 109-12-28  V1.00.03  Justin       parameterize sql
***************************************************************************/
package ecsq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsq0210 extends BaseEdit {
  private String progname = "檔案傳輸紀錄檔查詢處理程式109/12/28 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsq01.Ecsq0210Func func = null;
  String rowid;
  String orgtTabName = "ecs_ftp_log";
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
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 "
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
        + sqlCol(wp.itemStr("ex_ref_ip_code"), "a.ref_ip_code", "like%")
        + sqlChkEx(wp.itemStr("ex_ref_ip_code1"), "1", "");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    controlTabName = orgtTabName;

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.crt_date," + "a.crt_time," + "a.trans_seqno," + "a.system_id," + "a.ref_ip_code,"
        + "a.file_date," + "a.file_name," + "(proc_code||'0'||proc_desc) as proc_desc,"
        + "a.trans_desc," + "a.mod_pgm";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.crt_date desc,a.crt_time desc,a.system_id";

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
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgtTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.crt_date,"
        + "a.crt_time," + "a.ref_ip_code," + "a.ftp_type," + "a.system_id," + "a.group_id,"
        + "a.source_from," + "a.trans_seqno," + "a.trans_total_cnt," + "a.trans_seq,"
        + "a.trans_size," + "a.local_size," + "a.remote_addr," + "a.trans_mode," + "a.file_date,"
        + "a.trans_data," + "a.file_name," + "a.trans_desc," + "a.proc_code," + "a.proc_desc,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm";

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
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    ecsq01.Ecsq0210Func func = new ecsq01.Ecsq0210Func(wp);

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
      if ((wp.respHtml.equals("ecsq0210"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_ref_ip_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_ref_ip_code");
        }
        this.dddwList("dddw_ref_ip", "ecs_ref_ip_addr", "trim(ref_ip_code)", "trim(ref_name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_ref_ip_code1").length() > 0) {
          wp.optionKey = wp.colStr("ex_ref_ip_code1");
        }
        this.dddwList("dddw_system_id",
            "select wf_id as db_code,max(wf_id||' - '||b.ref_name) as db_desc from ptr_sys_idtab a,ecs_ref_ip_addr b where a.wf_id = b.ref_ip_code and  wf_type='FTPSYSTEMID' group by wf_id order by max(a.mod_time),wf_id");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if ((wp.itemStr("ex_ref_ip_code").length() == 0)
        && (wp.itemStr("ex_ref_ip_code1").length() == 0)
        && (wp.itemStr("ex_crt_date_s").length() == 0)) {
      alertErr2("傳輸檔案歸類與產生日期不可同時空白");
      return (1);
    }
    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String ex_col, String sq_cond, String file_ext) {
    if (sq_cond.equals("1")) {
      if (empty(wp.itemStr("ex_ref_ip_code")))
        return "";
      if (empty(wp.itemStr("ex_ref_ip_code1")))
        return "";
      setString(wp.colStr("ex_ref_ip_code1"));
      return " and ref_ip_code = ? ";
    }

    return "";
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
