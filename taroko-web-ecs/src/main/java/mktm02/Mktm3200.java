/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108-12-12  V1.00.01   Allen Ho      Initial                              *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *                                                                  *
* 110-11-23  V1.00.05  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package mktm02;

import mktm02.Mktm3200Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm3200 extends BaseEdit {
  private String PROGNAME = "高鐵車廂請款明細檔維護處理程式";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm3200Func func = null;
  String rowid;
  String serialNo;
  String fstAprFlag = "";
  String orgTabName = "mkt_thsr_disc";
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
        /* 單獨新增功能 */
        strAction = "I";

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
        wfAjaxFunc2();
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
    wp.whereStr = "WHERE 1=1 "
        + sqlStrend(wp.itemStr("ex_proc_date_s"), wp.itemStr("ex_proc_date_e"), "a.proc_date")
        + sqlCol(wp.itemStr("ex_serial_no"), "a.serial_no", "like%")
        + sqlStrend(wp.itemStr("ex_trans_date_s"), wp.itemStr("ex_trans_date_e"), "a.trans_date")
        + sqlChkEx(wp.itemStr("ex_err_type"), "1", "")
        + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "") + " and proc_flag  not in  ('0','1','Y') ";

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
        + "a.serial_no," + "a.trans_date," + "a.trans_time," + "a.trans_type," + "a.trans_amount,"
        + "a.card_no," + "a.authentication_code," + "b.id_no," + "a.group_code," + "a.proc_date,"
        + "a.proc_flag," + "a.crt_date";

    wp.daoTable =
        controlTabName + " a " + "LEFT OUTER JOIN crd_idno b " + "ON a.id_p_seqno = b.id_p_seqno ";
    wp.whereOrder = " " + " order by a.proc_date,a.serial_no,a.trans_date";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGroupCode("comm_group_code");

    commTransType("comm_trans_type");
    commProcFlag("comm_proc_flag");

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
        + "a.serial_no as serial_no," + "a.trans_date," + "a.trans_time," + "a.card_no,"
        + "a.acct_type," + "a.authentication_code," + "a.pay_cardid," + "a.trans_amount,"
        + "a.trans_type," + "a.group_code," + "a.card_mode," + "a.proc_date," + "a.proc_flag,"
        + "a.error_code," + "a.error_desc," + "a.crt_user," + "a.crt_date," + "a.apr_user,"
        + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(serialNo, "a.serial_no");
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
    commTransType("comm_trans_type");
    commCardMode("comm_card_mode");
    commProcFlag("comm_proc_flag");
    commGroupCode("comm_group_code");
    checkButtonOff();
    serialNo = wp.colStr("serial_no");
    commfuncAudType("aud_type");
    dataReadR3R();
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    String sql1 = " select b.acct_type," + "        b.acct_key," + "        c.chi_name "
        + " from crd_card a,act_acno b,crd_idno c " + " where c.id_p_seqno = a.id_p_seqno "
        + " and   b.p_seqno = a.p_seqno "
//        + " and   a.card_no = '" + wp.colStr("card_no") + "' ";
        + sqlCol(wp.colStr("card_no"), "a.card_no");

    sqlSelect(sql1);

    if (sqlRowNum > 0) {
      wp.colSet("acct_type", sqlStr("acct_type"));
      wp.colSet("acct_key", sqlStr("acct_key"));
      wp.colSet("chi_name", sqlStr("chi_name"));
    }
    return;

  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.serial_no as serial_no," + "a.trans_date as trans_date,"
        + "a.trans_time as trans_time," + "a.card_no as card_no," + "a.acct_type as acct_type,"
        + "a.authentication_code as authentication_code," + "a.pay_cardid as pay_cardid,"
        + "a.trans_amount as trans_amount," + "a.trans_type as trans_type,"
        + "a.group_code as group_code," + "a.card_mode as card_mode," + "a.proc_date as proc_date,"
        + "a.proc_flag as proc_flag," + "a.error_code as error_code,"
        + "a.error_desc as error_desc," + "a.crt_user as crt_user," + "a.crt_date as crt_date,"
        + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(serialNo, "a.serial_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commTransType("comm_trans_type");
    commGroupCode("comm_group_code");
    commCardMode("comm_card_mode");
    commProcFlag("comm_proc_flag");
    checkButtonOff();
    commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    serialNo = wp.itemStr("serial_no");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      serialNo = wp.itemStr("serial_no");
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
    serialNo = wp.itemStr("serial_no");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1) {
        dataReadR3R();;
        datareadWkdata();
      }
    } else {
      serialNo = wp.itemStr("serial_no");
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
    mktm02.Mktm3200Func func = new mktm02.Mktm3200Func(wp);

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
  public void dddwSelect() {}

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("1")) {
      if (wp.itemStr("ex_err_type").length() == 0)
        return "";
      if (wp.itemStr("ex_err_type").equals("1"))
        return " and proc_flag = 'X' and error_code like '9%' ";
    }

    return "";
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
  public void commGroupCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 "
//          + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
          + sqlCol(wp.colStr(ii, "group_code"), "group_code");
      if (wp.colStr(ii, "group_code").length() == 0)
        continue;
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commTransType(String cde1) throws Exception {
    String[] cde = {"P", "R"};
    String[] txt = {"購票", "退票"};
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
  public void commCardMode(String cde1) throws Exception {
    String[] cde = {"1", "2"};
    String[] txt = {"頂級卡", "一般卡"};
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
  public void commProcFlag(String cde1) throws Exception {
    String[] cde = {"1", "2", "Y", "X"};
    String[] txt = {"檢核確認", "扣紅利或金額確認", "已轉入帳單檔", "有問題無法處理"};
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
  public void wfAjaxFunc2() throws Exception {
    // 20200102 updated for archit. change
    // super.wp = wr;
    // super.wp = wr;


    selectAjaxFunc20(wp.itemStr("ax_win_card_no"));

    if (rc != 1) {
      wp.addJSON("acct_type", "");
      wp.addJSON("acct_key", "");
      wp.addJSON("chi_name", "");
      return;
    }

    wp.addJSON("acct_type", sqlStr("acct_type"));
    wp.addJSON("acct_key", sqlStr("acct_key"));
    wp.addJSON("chi_name", sqlStr("chi_name"));
  }

  // ************************************************************************
  void selectAjaxFunc20(String cardNo) {
    wp.sqlCmd = " select b.acct_type," + "        b.acct_key," + "        a.group_code,"
        + "        c.chi_name " + " from crd_card a,act_acno b,crd_idno c "
        + " where c.id_p_seqno = a.id_p_seqno " + " and   b.p_seqno = a.p_seqno "
//        + " and   a.card_no = '" + cardNo + "' ";
        + sqlCol(cardNo, "a.card_no");
    this.sqlSelect();

    if (sqlRowNum <= 0)
      alertErr2("卡號：[" + cardNo + "]查無資料");

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
