/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108-12-12  V1.00.01   Allen Ho      Initial                              *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change           
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *   
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                 *
* 110/11/11  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktm02;

import mktm02.Mktm6290Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6290 extends BaseEdit {
  private String PROGNAME = "帳戶紅利點數線上調整作業處理程式108/12/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6290Func func = null;
  String rowid;
  String tranSeqno;
  String fstAprFlag = "";
  String orgTabName = "mkt_bonus_dtl";
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
	wp.logSql = true;
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
        /* 單獨新功增能 */
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
        wfAjaxFunc1();
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlChkEx(wp.itemStr("ex_id_no"), "1", "") + sqlChkEx(wp.itemStr("ex_card_no"), "3", "")
        + sqlCol(wp.itemStr("ex_bonus_type"), "a.bonus_type", "like%")
        + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "");

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
        + "a.tran_seqno," + "'' as id_no," + "'' as chi_name," + "a.active_code," + "a.active_name,"
        + "a.tran_code," + "a.beg_tran_bp," + "a.bonus_type," + "a.crt_date," + "a.crt_user,"
        + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by tran_date desc,tran_seqno desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    commBonusType("comm_bonus_type");
    comm_crt_user("comm_crt_user");
    commTranCode("comm_tran_code");

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
        + "a.p_seqno as p_seqno," + "a.id_p_seqno as id_p_seqno," + "a.active_name as active_name,"
        + "a.tran_seqno as tran_seqno," + "a.acct_type," + "c.chi_name as chi_name,"
        + "'' as id_no," + "a.p_seqno," + "a.id_p_seqno," + "a.bonus_type," + "a.active_code,"
        + "a.tran_code," + "a.beg_tran_bp," + "a.tax_flag," + "a.effect_e_date," + "a.mod_reason,"
        + "a.mod_desc," + "a.mod_memo," + "a.apr_date," + "a.apr_user," + "a.crt_date,"
        + "a.crt_user," + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_user";

    wp.daoTable = controlTabName + " a " + "JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(tranSeqno, "a.tran_seqno");
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
    tranSeqno = wp.colStr("tran_seqno");
    commfuncAudType("aud_type");
    comm_crt_user("comm_crt_user");
    comm_apr_user("comm_apr_user");   
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.tran_seqno as tran_seqno," + "a.acct_type as acct_type,"
        + "c.chi_name as chi_name," + "'' as id_no," + "a.p_seqno as p_seqno,"
        + "a.id_p_seqno as id_p_seqno," + "a.bonus_type as bonus_type,"
        + "a.active_code as active_code," + "a.tran_code as tran_code,"
        + "a.beg_tran_bp as beg_tran_bp," + "a.tax_flag as tax_flag,"
        + "a.effect_e_date as effect_e_date," + "a.mod_reason as mod_reason,"
        + "a.mod_desc as mod_desc," + "a.mod_memo as mod_memo," + "a.apr_date as apr_date,"
        + "a.apr_user as apr_user," + "a.crt_date as crt_date," + "a.crt_user as crt_user,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_user as mod_user";

    wp.daoTable = controlTabName + " a " + "JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
    wp.whereStr = "where 1=1 " + sqlCol(tranSeqno, "a.tran_seqno");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    comm_crt_user("comm_crt_user");
    comm_apr_user("comm_apr_user");
    checkButtonOff();
    commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    tranSeqno = wp.itemStr("tran_seqno");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      tranSeqno = wp.itemStr("tran_seqno");
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
    tranSeqno = wp.itemStr("tran_seqno");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      tranSeqno = wp.itemStr("tran_seqno");
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
    mktm02.Mktm6290Func func = new mktm02.Mktm6290Func(wp);

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
      if ((wp.respHtml.equals("mktm6290_nadd")) || (wp.respHtml.equals("mktm6290_detl"))) {
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("acct_type").length() > 0) {
          wp.optionKey = wp.colStr("acct_type");
          wp.initOption = "";
        }
        this.dddwList("dddw_acct_type1", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where bonus_flag='Y'");
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("bonus_type").length() > 0) {
          wp.optionKey = wp.colStr("bonus_type");
        }
        this.dddwList("dddw_bonus_type", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type = 'BONUS_NAME'");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("active_code").length() > 0) {
          wp.optionKey = wp.colStr("active_code");
        }
        this.dddwList("dddw_active_name", "vmkt_bonus_active_name", "trim(active_code)",
            "trim(active_name)", " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("mod_reason").length() > 0) {
          wp.optionKey = wp.colStr("mod_reason");
        }
        this.dddwList("dddw_mod_reason", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='ADJMOD_REASON'");
      }
      if ((wp.respHtml.equals("mktm6290"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
          wp.initOption = "";
        }
        this.dddwList("dddw_active_nameb_b", "vmkt_bonus_active_name", "trim(active_code)",
            "trim(active_name)", " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_bonus_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_bonus_type");
        }
        this.dddwList("dddw_bonus_type_b", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='BONUS_NAME'");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if ((itemKk("ex_card_no").length() == 0) && (itemKk("ex_apr_flag").equals("Y"))
        && (itemKk("ex_id_no").length() == 0)) {
      alertErr2("身份證號與卡號二者不可同時空白");
      return (1);
    }


    if ((wp.itemStr("ex_id_no").length() != 10) && (wp.itemStr("ex_id_no").length() != 0)
        && (wp.itemStr("ex_id_no").length() != 11)) {
      alertErr2("身分證號10碼,帳戶查詢碼11碼");
      return (1);
    }

    String sql1 = "";

    if (wp.itemStr("ex_id_no").length() == 10) {
      sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no  = ? " + "and    id_no_code   = '0' ";

      sqlSelect(sql1, new Object[] { wp.itemStr("ex_id_no").toUpperCase() });
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      return (0);
    }


    if (wp.itemStr("ex_id_no").length() == 11) {
      sql1 = "select id_p_seqno " + "from crd_idno " + "where  id_no      = ? "
    + "and    id_no_code = ? ";


      sqlSelect(sql1, new Object[] { wp.itemStr("ex_id_no").substring(0, 10).toUpperCase(), wp.itemStr("ex_id_no").substring(10, 11).toUpperCase() });
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此帳戶查詢碼[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      return (0);
    }

    if (wp.itemStr("ex_card_no").length() > 0) {
      sql1 = "select id_p_seqno " + "from crd_card " + "where  card_no  = ? ";

      sqlSelect(sql1, new Object[] { wp.itemStr("ex_card_no").toUpperCase() });
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此卡號[ " + wp.itemStr("ex_card_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
    }

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("1")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      return sqlCol(wp.colStr("ex_id_p_seqno"), "id_p_seqno");
//      return " and id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
    }

    if (sqCond.equals("3")) {
      if (empty(wp.itemStr("ex_card_no")))
        return "";
//      return " and id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
      return sqlCol(wp.colStr("ex_id_p_seqno"), "id_p_seqno");
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
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
          + " and   id_p_seqno = ? ";
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "id_p_seqno") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commChiName(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chi_name as column_chi_name " + " from crd_idno " + " where 1 = 1 "
          + " and   id_p_seqno = ? ";
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "id_p_seqno") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chi_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commBonusType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_type = 'BONUS_NAME' " + " and   wf_id = ? ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "bonus_type") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commTranCode(String cde1) throws Exception {
    String[] cde = {"0", "1", "2", "3", "4", "5", "6", "7"};
    String[] txt = {"移轉", "新增", "贈與", "調整", "使用", "匯入", "移除", "扣回"};
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
  public void wfAjaxFunc1() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change


    selectAjaxFunc10(wp.itemStr("ax_win_acct_type"), wp.itemStr("ax_win_id_no").toUpperCase());

    if (rc != 1) {
      wp.addJSON("chi_name", "");
      wp.addJSON("p_seqno", "");
      wp.addJSON("id_p_seqno", "");
      return;
    }

    wp.addJSON("chi_name", sqlStr("chi_name"));
    wp.addJSON("p_seqno", sqlStr("p_seqno"));
    wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
  }

  // ************************************************************************
  void selectAjaxFunc10(String acctType, String corpNo) {
    wp.sqlCmd = " select " + " card_indicator " + " from  ptr_acct_type " + " where acct_type = ? ";

    this.sqlSelect(new Object[] { acctType });
    if (sqlRowNum <= 0) {
      alertErr2("帳戶類別:[" + acctType + "]查無資料");
      return;
    }

    if (corpNo.length() == 0)
      return;
    if (sqlStr("card_indicator").equals("1")) {
      if ((corpNo.length() != 10) && (corpNo.length() != 11)) {
        alertErr2("身分證號:[" + corpNo + "]長度要10碼或11碼");
        return;
      }
      String idNo = "", idNoCode = "0";
      if (corpNo.length() == 10)
        idNo = corpNo;
      else {
        idNo = corpNo.substring(0, 10);
        idNoCode = corpNo.substring(10, 11);
      }

      wp.sqlCmd = " select " + " a.chi_name as chi_name ," + " b.p_seqno as p_seqno ,"
          + " a.id_p_seqno as id_p_seqno " + " from  crd_idno a,act_acno b "
          + " where a.id_p_seqno=b.id_p_seqno " + " and   b.acct_type = ? "
          + " and   a.id_no      = ? " + " and   a.id_no_code = ? ";

      this.sqlSelect(new Object[] { acctType, idNo, idNoCode });
      if (sqlRowNum <= 0) {
        alertErr2("身分證號:[" + corpNo + "]]查無資料");
        return;
      }
    } else {
      if ((corpNo.length() != 8) && (corpNo.length() != 11)) {
        alertErr2("[" + corpNo + "]長度要8碼(統編)或11碼(帳戶查詢碼)");
        return;
      }
      if (corpNo.length() == 8) {
        wp.sqlCmd = " select " + " a.chi_name as chi_name ," + " b.p_seqno as p_seqno ,"
            + " '' as id_p_seqno " + " from  crd_corp a,act_acno b "
            + " where a.corp_p_seqno=b.corp_p_seqno " + " and   b.acct_type = ? "
            + " and   a.corp_no      = ? ";
      } else {
        wp.sqlCmd = " select " + " a.chi_name as chi_name ," + " b.p_seqno as p_seqno ,"
            + " a.id_p_seqno as id_p_seqno " + " from  crd_idno a,act_acno b "
            + " where a.id_p_seqno=b.id_p_seqno " + " and   b.acct_type = ? "
            + " and   b.acct_key  = ? ";
      }
      this.sqlSelect(new Object[] { acctType, corpNo });
      if (sqlRowNum <= 0) {
        alertErr2("商務卡:[" + corpNo + "]8碼(統編)或11碼(帳戶查詢碼)查無資料");
        return;
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
//************************************************************************
public void comm_crt_user(String columnData1) throws Exception 
{
 String columnData="";
 String sql1 = "";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " usr_cname as column_usr_cname "
           + " from sec_user "
           + " where 1 = 1 "
           + " and   usr_id = ? "
           ;
      if (wp.colStr(ii,"crt_user").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"crt_user") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}
//************************************************************************
public void comm_apr_user(String columnData1) throws Exception 
{
 String columnData="";
 String sql1 = "";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " usr_cname as column_usr_cname "
           + " from sec_user "
           + " where 1 = 1 "
           + " and   usr_id = ? "
           ;
      if (wp.colStr(ii,"apr_user").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"apr_user") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}

} // End of class
