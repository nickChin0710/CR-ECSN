/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110/11/22  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktp01;

import mktp01.Mktp0670Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0670 extends BaseProc {
  private String PROGNAME = "媒體檔案上傳作業處理程式108/09/03 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0670Func func = null;
  String rowid;
  String fstAprFlag = "";
  String orgTabName = "mkt_uploadfile_ctl";
  String controlTabName = "";
  int qFrom = 0;
  int uploadFlag = 0;
  String tranSeqStr = "";
  String batch_no = "";
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
      uploadFlag = 0;
      strAction = "A";
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Z")) {/* 不同意轉入 */
      uploadFlag = 1;
      dataProcess();
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
        + sqlStrend(wp.itemStr("ex_file_date_s"), wp.itemStr("ex_file_date_e"), "a.file_date")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%") + " and apr_flag  =  'N' "
        + " and file_flag  =  'Y' " + " and group_type  =  '1' ";

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
        + "a.file_date," + "a.file_time," + "a.file_type," + "a.trans_seqno," + "a.file_cnt,"
        + "a.file_amt1," + "a.crt_user," + "a.error_desc," + "a.error_memo," + "a.callbatch_pgm,"
        + "a.callbatch_parm";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.file_date,a.crt_user";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commFileType("comm_file_type");


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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.file_type,"
        + "a.file_date," + "a.file_time," + "a.file_name," + "a.file_flag," + "a.file_cnt,"
        + "a.file_amt1," + "a.proc_flag," + "a.error_desc," + "a.error_memo," + "a.callbatch_pgm,"
        + "a.callbatch_parm," + "a.trans_seqno," + "a.proc_date," + "a.crt_date," + "a.crt_user,"
        + "a.apr_date," + "a.apr_user," + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
        + "a.mod_pgm";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr;
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commFileType("comm_file_type");
    checkButtonOff();
  }

  // ************************************************************************
  public int dataReadR4(String trans_seqno) {
    setSelectLimit(0);
    String sqlCmd = " select " + " data_column01, " + " data_data01, " + " table_name "
        + " from mkt_uploadfile_data" + " where trans_seqno = ? ";

    sqlSelect(sqlCmd, new Object[] { trans_seqno });

    if (sqlRowNum <= 0)
      return (1);
    int recCnt = sqlRowNum;
    mktp01.Mktp0670Func func = new mktp01.Mktp0670Func(wp);
    for (int ii = 0; ii < recCnt; ii++) {
      rc = func.dbInsertA4(sqlStr(ii, "table_name"), sqlStr(ii, "data_column01"),
          sqlStr(ii, "data_data01"));
      if (rc != 1)
        return (rc);
    }
    return 1;
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {}

  // ************************************************************************
  void listWkdataSpace() throws Exception {}

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp01.Mktp0670Func func = new mktp01.Mktp0670Func(wp);
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    String[] rcvStr = {"", "", "", "", "", "", "", "", "", ""};

    String[] lsTransSeqno = wp.itemBuff("trans_seqno");
    String[] lsFileType = wp.itemBuff("file_type");
    String[] lsCallbatchPgm = wp.itemBuff("callbatch_pgm");
    String[] lsCallbatchParm = wp.itemBuff("callbatch_parm");
    String[] lsCrtUser = wp.itemBuff("crt_user");
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
      if (lsCrtUser[rr].equals(wp.loginUser)) {
        ilErr++;
        wp.colSet(rr, "ok_flag", "X");
        continue;
      }

      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      rc = func.dbUpdateU4(lsTransSeqno[rr], uploadFlag);
      if (uploadFlag == 0) {
        if (rc != 1)
          alertErr2(func.getMsg());
        else
          rc = dataReadR4(lsTransSeqno[rr]);
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commFileType("comm_file_type");

        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        func.dbDelete(lsTransSeqno[rr]);
        this.sqlCommit(rc);
        if (lsCallbatchPgm[rr].length() > 0) {
          ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);

          rc = batch.callBatch(lsCallbatchPgm[rr] + " " + lsCallbatchParm[rr]);
          if (rc != 1) {
            alertErr2(lsFileType[rr] + " : callbatch[" + lsCallbatchPgm[rr] + " 失敗");
          } else {
            alertMsg(lsFileType[rr] + " 處理中...");
          }
        }
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
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
  public void commFileType(String type) throws Exception {
    commFileType(type, 0);
    return;
  }

  // ************************************************************************
  public void commFileType(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_type = 'MKT_UPLOADFILE_CTL' " + " and   wf_id = ? ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii, befStr + "file_type") });

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
