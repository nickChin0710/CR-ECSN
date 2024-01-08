/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change           
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 110/11/11  V1.00.04  jiangyingdong       sql injection                   *
* 112/04/21  V1.00.05  Ryan         增加名單匯入功能 ,增加LIST_COND,LIST_FLAG,LIST_USE_SEL欄位維護                 *
***************************************************************************/
package mktm02;

import mktm02.Mktm6250Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;
// ************************************************************************
public class Mktm6250 extends BaseEdit {
  private ArrayList<Object> params = new ArrayList<Object>();
  private String PROGNAME = "首刷禮活動回饋參數處理程式108/12/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6250Func func = null;
  String rowid, kk2;
  String activeCode, activeSeq;
  String fstAprFlag = "";
  String orgTabName = "mkt_fstp_parmseq";
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
      case "R3":
    	  strAction = "R3";
          dataRead_R3();
          break;
      case "U3":
    	  strAction = "U3";
          updateFuncU3();  
          break;
      case "R2":
    	  strAction = "R2";
          dataReadR2();
          break;
      case "U2":
    	  strAction = "U2";
          updateFuncU2();
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
      case "UPLOAD2":
          procUploadFile(2);
          checkButtonOff();
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
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
        + "a.active_code," + "a.active_seq," + "a.level_seq," + "a.active_type," + "a.record_cond,"
        + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by active_code,active_Seq";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commFundCode("comm_active_code");
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    commActiveType("comm_active_type");

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
        + "a.active_code as active_code," + "a.active_seq as active_seq," + "a.level_seq,"
        + "a.record_cond," + "a.record_group_no," + "a.active_type," + "a.bonus_type,"
        + "a.tax_flag," + "a.fund_code," + "a.group_type," + "a.prog_code," + "a.prog_s_date,"
        + "a.prog_e_date," + "a.gift_no," + "a.spec_gift_no," + "a.per_amt_cond," + "a.per_amt,"
        + "a.perday_cnt_cond," + "a.perday_cnt," + "a.sum_amt_cond," + "a.sum_amt,"
        + "a.sum_cnt_cond," + "a.sum_cnt," + "a.threshold_sel," + "a.purchase_type_sel,"
        + "a.purchase_amt_s1," + "a.purchase_amt_e1," + "a.feedback_amt_1," + "a.purchase_amt_s2,"
        + "a.purchase_amt_e2," + "a.feedback_amt_2," + "a.purchase_amt_s3," + "a.purchase_amt_e3,"
        + "a.feedback_amt_3," + "a.purchase_amt_s4," + "a.purchase_amt_e4," + "a.feedback_amt_4,"
        + "a.purchase_amt_s5," + "a.purchase_amt_e5," + "a.feedback_amt_5," + "a.feedback_limit,"
        + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date,"
        + "a.stop_date,"+ "a.stop_desc,"
        + "a.purchase_days,"+ "a.merchant_sel,"
        + "'' as merchant_sel_cnt,"+ "a.mcht_group_sel,"
        + "'' as mcht_group_sel_cnt,"+ "a.pur_date_sel,"
        + "a.stop_flag, "
        + "a.list_cond,"
        + "a.list_flag,"
        + "a.list_use_sel,"
        + "'' as list_flag_cnt"
        ;

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(activeCode, "a.active_code") + sqlCol(activeSeq, "a.active_seq");
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
    commGroupType("comm_group_type");
    listWkdata(); 
    checkButtonOff();
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    activeCode = wp.colStr("active_code");
    activeSeq = wp.colStr("active_seq");
    commfuncAudType("aud_type");
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.active_code as active_code,"
        + "a.active_seq as active_seq," + "a.level_seq as level_seq,"
        + "a.record_cond as record_cond," + "a.record_group_no as record_group_no,"
        + "a.active_type as active_type," + "a.bonus_type as bonus_type,"
        + "a.tax_flag as tax_flag," + "a.fund_code as fund_code," + "a.group_type as group_type,"
        + "a.prog_code as prog_code," + "a.prog_s_date as prog_s_date,"
        + "a.prog_e_date as prog_e_date," + "a.gift_no as gift_no,"
        + "a.spec_gift_no as spec_gift_no," + "a.per_amt_cond as per_amt_cond,"
        + "a.per_amt as per_amt," + "a.perday_cnt_cond as perday_cnt_cond,"
        + "a.perday_cnt as perday_cnt," + "a.sum_amt_cond as sum_amt_cond,"
        + "a.sum_amt as sum_amt," + "a.sum_cnt_cond as sum_cnt_cond," + "a.sum_cnt as sum_cnt,"
        + "a.threshold_sel as threshold_sel," + "a.purchase_type_sel as purchase_type_sel,"
        + "a.purchase_amt_s1 as purchase_amt_s1," + "a.purchase_amt_e1 as purchase_amt_e1,"
        + "a.feedback_amt_1 as feedback_amt_1," + "a.purchase_amt_s2 as purchase_amt_s2,"
        + "a.purchase_amt_e2 as purchase_amt_e2," + "a.feedback_amt_2 as feedback_amt_2,"
        + "a.purchase_amt_s3 as purchase_amt_s3," + "a.purchase_amt_e3 as purchase_amt_e3,"
        + "a.feedback_amt_3 as feedback_amt_3," + "a.purchase_amt_s4 as purchase_amt_s4,"
        + "a.purchase_amt_e4 as purchase_amt_e4," + "a.feedback_amt_4 as feedback_amt_4,"
        + "a.purchase_amt_s5 as purchase_amt_s5," + "a.purchase_amt_e5 as purchase_amt_e5,"
        + "a.feedback_amt_5 as feedback_amt_5," + "a.feedback_limit as feedback_limit,"
        + "a.crt_user as crt_user," + "a.crt_date as crt_date," + "a.apr_user as apr_user,"
        + "a.apr_date as apr_date,"
        + "a.stop_date as stop_date,"
        + "a.stop_desc as stop_desc,"
        + "a.purchase_days as purchase_days,"+ "a.merchant_sel as merchant_sel,"
        + "'' as merchant_sel_cnt,"
        + "a.mcht_group_sel as mcht_group_sel,"
        + "'' as mcht_group_sel_cnt,"+ "a.pur_date_sel as pur_date_sel,"
        + "a.stop_flag as stop_flag, "
        + "a.list_cond as list_cond,"
        + "a.list_flag as list_flag,"
        + "a.list_use_sel as list_use_sel,"
        + "'' as list_flag_cnt";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code") + sqlCol(activeSeq, "a.active_seq");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    commGroupType("comm_group_type");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdataAft();
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    activeCode = wp.itemStr("active_code");
    activeSeq = wp.itemStr("active_seq");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      activeCode = wp.itemStr("active_code");
      activeSeq = wp.itemStr("active_seq");
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
    activeCode = wp.itemStr("active_code");
    activeSeq = wp.itemStr("active_seq");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      activeCode = wp.itemStr("active_code");
      activeSeq = wp.itemStr("active_seq");
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
    mktm02.Mktm6250Func func = new mktm02.Mktm6250Func(wp);
    if (wp.respHtml.indexOf("_detl") > 0)
        if (!wp.colStr("aud_type").equals("Y")) listWkdataAft();
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
      if ((wp.respHtml.equals("mktm6250_nadd")) || (wp.respHtml.equals("mktm6250_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("active_code").length() > 0) {
          wp.optionKey = wp.colStr("active_code");
          wp.initOption = "";
        }
        this.dddwList("dddw_active_code", "mkt_fstp_parm", "trim(active_code)", "trim(active_name)",
            " where stop_flag='N'");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("record_group_no").length() > 0) {
          wp.optionKey = wp.colStr("record_group_no");
        }
        this.dddwList("dddw_record_gp", "web_record_group", "trim(record_group_no)",
            "trim(record_group_name)", " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("bonus_type").length() > 0) {
          wp.optionKey = wp.colStr("bonus_type");
        }
        this.dddwList("dddw_bonus_type", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='BONUS_NAME'");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("fund_code").length() > 0) {
          wp.optionKey = wp.colStr("fund_code");
        }
        this.dddwList("dddw_func_code", "mkt_loan_parm", "trim(fund_code)", "trim(fund_name)",
            " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("prog_code1").length() > 0) {
          wp.optionKey = wp.colStr("prog_code1");
        }
        this.dddwList("dddw_prog_code",
            "select prog_code||'-'||prog_s_date as db_code, prog_code||'('||substr(prog_desc,1,4)||')-'||prog_s_date as db_desc  from ibn_prog where prog_flag='Y' and to_char(sysdate,'yyyymmdd')< prog_e_date   order by prog_code,prog_s_date");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("gift_no").length() > 0) {
          wp.optionKey = wp.colStr("gift_no");
        }
        this.dddwList("dddw_gift_no", "ibn_prog_gift", "trim(gift_no)", "trim(gift_name)",
            " where 1=1 " + sqlCol(wp.colStr("prog_code"), "prog_code")
//            " where prog_code = '" + wp.colStr("prog_code")
                + "  group by gift_no,gift_name order by gift_no,gift_name");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("spec_gift_no").length() > 0) {
          wp.optionKey = wp.colStr("spec_gift_no");
        }
        this.dddwList("dddw_spec_gift_no", "mkt_spec_gift", "trim(gift_no)", "trim(gift_name)",
            " where disable_flag='N' and gift_group='1'");
      }
      if ((wp.respHtml.equals("mktm6250"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
        }
        this.dddwList("dddw_active_code", "mkt_fstp_parm", "trim(active_code)", "trim(active_name)",
            " where stop_flag='N'");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
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
  public void commFundCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from mkt_fstp_parm "
          + " where 1 = 1 " + " and   active_code = ? ";
      if (wp.colStr(ii, "active_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"active_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_active_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commGroupType(String cde1) throws Exception {
    String[] cde = {"group_type", "1", "2", "3", "4"};
    String[] txt = {"", "限信>用卡兌換(限01,05,06)", "限 VD卡兌換(限90)", "全部任一卡片兌換(01,05,06,90)", "限特定卡號兌換"};
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
  public void commActiveType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"紅利", "現金回饋", "豐富點", "特殊贈品"};
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
    // super.wp = wr; // 20200102 updated for archit. change
    String ajaxjGiftNo = "";
    // super.wp = wr; // 20200102 updated for archit. change

    if (wp.itemStr("ax_win_prog_code").length() == 0)
      return;
    if (wp.itemStr("ax_win_prog_s_date").length() == 0)
      return;

    selectAjaxFunc20(wp.itemStr("ax_win_prog_code1"), wp.itemStr("ax_win_prog_code"),
        wp.itemStr("ax_win_prog_s_date"));

    if (rc != 1) {
      wp.addJSON("prog_code", "");
      wp.addJSON("prog_s_date", "");
      wp.addJSON("prog_e_date", "");
      return;
    }

    wp.addJSON("prog_code", sqlStr("prog_code"));
    wp.addJSON("prog_s_date", sqlStr("prog_s_date"));
    wp.addJSON("prog_e_date", sqlStr("prog_e_date"));

    if (wp.itemStr("ax_win_prog_code").length() == 0)
      return;
    if (wp.itemStr("ax_win_prog_s_date").length() == 0)
      return;

    selectAjaxFunc21(wp.itemStr("ax_win_prog_code1"), wp.itemStr("ax_win_prog_code"),
        wp.itemStr("ax_win_prog_s_date"));

    if (rc != 1) {
      wp.addJSON("ajaxj_gift_no", "");
      wp.addJSON("ajaxj_gift_name", "");
      return;
    }

    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_gift_no", sqlStr(ii, "gift_no"));
    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_gift_name", sqlStr(ii, "gift_name"));
  }

  // ************************************************************************
  void selectAjaxFunc20(String progCode, String progSDate, String progEDate) {
    wp.sqlCmd = " select " + " b.prog_code as prog_code ," + " b.prog_s_date as prog_s_date ,"
        + " b.prog_e_date as prog_e_date " + " from  ibn_prog b " + " where b.prog_code = ? "
		+ " and b.prog_s_date = ? ";

    this.sqlSelect(new Object[] { comm.getStr(progCode, 1, "-"), comm.getStr(progCode, 2, "-") });
    if (sqlRowNum <= 0)
      alertErr2("活動代碼選擇[" + progCode + "]查無資料");

    return;
  }

  // ************************************************************************
  void selectAjaxFunc21(String giftNo, String giftName, String giftSDate) {
    wp.sqlCmd = " select " + " gift_no," + " gift_name," + " gift_s_date," + " gift_e_date"
        + " from  ibn_prog_gift " + " where prog_code = ? "
        + " and   prog_s_date = ? ";

    this.sqlSelect(new Object[] { sqlStr("prog_code"), sqlStr("prog_s_date") });
    if (sqlRowNum <= 0)
      alertErr2("贈品代碼選擇:[" + giftNo + "]查無資料");

    return;
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
	  if (wp.colStr("merchant_sel").length()==0)
	      wp.colSet("merchant_sel" , "0");
  
	  if (wp.colStr("merchant_sel").equals("0"))
	     {
	      buttonOff("btnmrcd_disable");
	      buttonOff("uplaaa1_disable");
	     }
	  else
	     {
	      wp.colSet("btnmrcd_disable","");
	      wp.colSet("uplaaa1_disable","");
	     }

	  if (wp.colStr("mcht_group_sel").length()==0)
	      wp.colSet("mcht_group_sel" , "0");

	  if (wp.colStr("mcht_group_sel").equals("0"))
	     {
	      buttonOff("btnaaa1_disable");
	     }
	  else
	     {
	      wp.colSet("btnaaa1_disable","");
	     }

	  if ((wp.colStr("aud_type").equals("Y"))||
	      (wp.colStr("aud_type").equals("D")))
	     {
	      buttonOff("uplaaa1_disable");
	     }
	  else
	     {
	      wp.colSet("uplaaa1_disable","");
	     }	  
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************
//************************************************************************
public void commCrtuser(String s1) throws Exception 
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
          wp.colSet(ii, s1, columnData);
          continue;
         }
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"crt_user") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, s1, columnData);
     }
  return;
}
//************************************************************************
public void commApruser(String s1) throws Exception 
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
          wp.colSet(ii, s1, columnData);
          continue;
         }
      sqlSelect(sql1, wp.colStr(ii,"apr_user"));

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, s1, columnData);
     }
  return;
}

//************************************************************************
public void listWkdataAft() throws Exception
{
wp.colSet("merchant_sel_cnt" , listMktbndata("mkt_bn_data_t","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"7"));
wp.colSet("mcht_group_sel_cnt" , listMktbndata("mkt_bn_data_t","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"8"));
wp.colSet("list_flag_cnt", listMktImfstpList("mkt_imfstp_list_t", "mkt_imfstp_list_t",
        wp.colStr("active_code"), wp.colStr("active_seq")));
}
//************************************************************************
public void listWkdata() throws Exception
{
wp.colSet("merchant_sel_cnt" , listMktbndata("mkt_bn_data","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"7"));
wp.colSet("mcht_group_sel_cnt" , listMktbndata("mkt_bn_data","MKT_FSTP_PARMSEQ",wp.colStr("active_code")+wp.colStr("active_seq"),"8"));
wp.colSet("list_flag_cnt", listMktImfstpList("mkt_imfstp_list", "mkt_imfstp_list",
        wp.colStr("active_code"), wp.colStr("active_seq")));
}

//************************************************************************
public String  listMktbndata(String s1,String s2,String s3,String s4) throws Exception
{
String sql1 = "select "
           + " count(*) as column_data_cnt "
           + " from "+ s1 + " "
           + " where 1 = 1 "
           + " and   table_name = ? "
           + " and   data_key   = ? "
           + " and   data_type  = ? "
           ;
sqlSelect(sql1, new Object[] { s2, s3, s4 });

if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

return("0");
}

//************************************************************************
public void dataRead_R3() throws Exception
{
dataReadR3(0);
}
//************************************************************************
public void dataReadR3(int fromType) throws Exception
{
String bnTable="";

if ((wp.itemStr("active_code").length()==0)||
    (wp.itemStr("aud_type").length()==0))
   {
	alertErr("鍵值為空白或主檔未新增 ");
    return;
   }
wp.selectCnt=1;
this.selectNoLimit();
if ((wp.itemStr("aud_type").equals("Y"))||
    (wp.itemStr("aud_type").equals("D")))
   {
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    bnTable = "mkt_bn_data";
   }
else
   {
    wp.colSet("btnUpdate_disable","");
    wp.colSet("newDetail_disable","");
    bnTable = "mkt_bn_data_t";
   }

wp.selectSQL = "hex(rowid) as r2_rowid, "
             + "ROW_NUMBER()OVER() as ser_num, "
             + "mod_seqno as r2_mod_seqno, "
             + "data_key, "
             + "data_code, "
             + "data_code2, "
             + "mod_user as r2_mod_user "
             ;
wp.daoTable = bnTable ;
wp.whereStr = "where 1=1"
           + " and table_name  =  'MKT_FSTP_PARMSEQ' "
             ;
if (wp.respHtml.equals("mktm6250_mrcd"))
   wp.whereStr  += " and data_type  = '7' ";
String whereCnt = wp.whereStr;
whereCnt += " and  data_key = ?";
params.add(wp.itemStr("active_code")+wp.itemStr("active_seq")+"");
wp.whereStr  += " and  data_key = :data_key ";
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key",wp.itemStr("active_code")+wp.itemStr("active_seq"));
wp.whereStr  += " order by 4,5,6 ";
int cnt1=selectBndatacount(wp.daoTable,whereCnt);
if (cnt1>300)
   {
	alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    return;
   }

pageQuery();
wp.setListCount(1);
wp.notFound = "";

wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
}

//************************************************************************
public int selectBndatacount(String bndata_table,String whereStr ) throws Exception
{
String sql1 = "select count(*) as bndataCount"
            + " from " + bndata_table
            + " " + whereStr
            ;

sqlSelect(sql1, params.toArray(new Object[params.size()]));
params.clear();

return((int)sqlNum("bndataCount"));
}

//************************************************************************
public void updateFuncU3() throws Exception
{
mktm02.Mktm6250Func func =new mktm02.Mktm6250Func(wp);
int ll_ok = 0, ll_err = 0;

String[] optData  = wp.itemBuff("opt");
String[] key1Data = wp.itemBuff("data_code");
String[] key2Data = wp.itemBuff("data_code2");

wp.listCount[0] = key1Data.length;
wp.colSet("IND_NUM", "" + key1Data.length);
//-check duplication-

int del2_flag=0;
for (int ll = 0; ll < key1Data.length; ll++)
   {
    del2_flag=0;
    wp.colSet(ll, "ok_flag", "");

    for (int intm=ll+1;intm<key1Data.length; intm++)
      if ((key1Data[ll].equals(key1Data[intm])) &&
          (key2Data[ll].equals(key2Data[intm]))) 
         {
          for (int intx=0;intx<optData.length;intx++) 
           { 
            if (optData[intx].length()!=0) 
            if (((ll+1)==Integer.valueOf(optData[intx]))||
                ((intm+1)==Integer.valueOf(optData[intx])))
               {
                del2_flag=1;
                break;
               }
           }
          if (del2_flag==1) break;

          wp.colSet(ll, "ok_flag", "!");
          ll_err++;
          continue;
         }
   }

if (ll_err > 0)
   {
    alertMsg("資料值重複 : " + ll_err);
    return;
   }

//-delete no-approve-
if (func.dbDeleteD3() < 0)
   {
    alertMsg(func.getMsg());
    return;
   }

//-insert-
int delete_flag=0;
for (int ll = 0; ll < key1Data.length; ll++)
   {
    delete_flag=0;
    //KEY 不可同時為空字串
        if ((empty(key1Data[ll])) &&
           (empty(key2Data[ll])))
        continue;

    //-option-ON-
    for (int intm=0;intm<optData.length;intm++)
      {
       if (optData[intm].length()!=0)
       if ((ll+1)==Integer.valueOf(optData[intm]))
          {
           delete_flag=1;
           break;
          }
       }
    if (delete_flag==1) continue;

    func.varsSet("data_code", key1Data[ll]); 
    func.varsSet("data_code2", key2Data[ll]); 

    if (func.dbInsertI3() == 1) ll_ok++;
    else ll_err++;

    //有失敗rollback，無失敗commit
    sqlCommit(ll_ok > 0 ? 1 : 0);
   }
alertMsg("資料存檔處理完成  成功(" + ll_ok + "), 失敗(" + ll_err + ")");

//SAVE後 SELECT
dataReadR3(1);
}

//************************************************************************
public void dataReadR2() throws Exception
{
dataReadR2(0);
}
//************************************************************************
public void dataReadR2(int fromType) throws Exception
{
String bnTable="";

if ((wp.itemStr("active_code").length()==0)||
    (wp.itemStr("aud_type").length()==0))
   {
	alertMsg("鍵值為空白或主檔未新增 ");
    return;
   }
wp.selectCnt=1;
this.selectNoLimit();
if ((wp.itemStr("aud_type").equals("Y"))||
    (wp.itemStr("aud_type").equals("D")))
   {
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    bnTable = "mkt_bn_data";
   }
else
   {
    wp.colSet("btnUpdate_disable","");
    wp.colSet("newDetail_disable","");
    bnTable = "mkt_bn_data_t";
   }

wp.selectSQL = "hex(rowid) as r2_rowid, "
             + "ROW_NUMBER()OVER() as ser_num, "
             + "mod_seqno as r2_mod_seqno, "
             + "data_key, "
             + "data_code, "
             + "mod_user as r2_mod_user "
             ;
wp.daoTable = bnTable ;
wp.whereStr = "where 1=1"
           + " and table_name  =  'MKT_FSTP_PARMSEQ' "
             ;
if (wp.respHtml.equals("mktm6250_aaa1"))
   wp.whereStr  += " and data_type  = '8' ";
String whereCnt = wp.whereStr;
whereCnt += " and  data_key = ? ";
params.add(wp.itemStr("active_code")+wp.itemStr("active_seq")+"");
wp.whereStr  += " and  data_key = :data_key ";
wp.whereStr  += " and  data_key = :data_key ";
setString("data_key",wp.itemStr("active_code")+wp.itemStr("active_seq"));
wp.whereStr  += " order by 4,5 ";
int cnt1=selectBndatacount(wp.daoTable,whereCnt);
if (cnt1>300)
   {
	alertMsg("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
    buttonOff("btnUpdate_disable");
    buttonOff("newDetail_disable");
    return;
   }

pageQuery();
wp.setListCount(1);
wp.notFound = "";

wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
if (wp.respHtml.equals("mktm6250_aaa1"))
	commDatacode34("comm_data_code");
}

//************************************************************************
public void commDatacode34(String s1) throws Exception 
{
String columnData="";
String sql1 = "";
for (int ii = 0; ii < wp.selectCnt; ii++)
   {
    columnData="";
    sql1 = "select "
         + " mcht_group_desc as column_mcht_group_desc "
         + " from mkt_mcht_gp "
         + " where 1 = 1 "
         + " and   mcht_group_id = ? "
         ;
    if (wp.colStr(ii,"data_code").length()==0)
       {
        wp.colSet(ii, s1, columnData);
        continue;
       }
    sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code") });

    if (sqlRowNum>0)
       columnData = columnData + sqlStr("column_mcht_group_desc"); 
    wp.colSet(ii, s1, columnData);
   }
return;
}


// ************************************************************************
public void procUploadFile(int loadType) throws Exception {
  if (wp.colStr(0, "ser_num").length() > 0)
    wp.listCount[0] = wp.itemBuff("ser_num").length;
  if (itemIsempty("zz_file_name")) {
    alertErr2("上傳檔名: 不可空白");
    return;
  }

  if (loadType == 2)
    fileDataImp2();
}

// ************************************************************************
void fileDataImp2() throws Exception {
    Mktm6250Func func = new Mktm6250Func(wp);
  TarokoFileAccess tf = new TarokoFileAccess(wp);

  String inputFile = wp.itemStr("zz_file_name");
  int fi = tf.openInputText(inputFile, "MS950");

  if (fi == -1)
    return;

  String sysUploadType = wp.itemStr("sys_upload_type");
  String sysUploadAlias = wp.itemStr("sys_upload_alias");

  if (sysUploadAlias.equals("list"))
     {
      // if has pre check procudure, write in here
      func.dbDeleteD2List("MKT_IMFSTP_LIST_T");
     }
  if (sysUploadAlias.equals("aaa1")) {
    // if has pre check procudure, write in here
  }

  if (sysUploadAlias.equals("aaa1"))
    func.dbDeleteD2Aaa1("MKT_BN_DATA_T");

  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  tranSeqStr = comr.getSeqno("MKT_MODSEQ");

  String string = "";
  int llOk = 0, llCnt = 0, llErr = 0;
  int lineCnt = 0;
  while (true) {
    string = tf.readTextFile(fi);
    if (tf.endFile[fi].equals("Y"))
      break;
    lineCnt++;
    if (sysUploadAlias.equals("list"))
    {
     if (lineCnt<=0) continue;
     if (string.length() < 2) continue;
    }
    if (sysUploadAlias.equals("aaa1")) {
      if (lineCnt <= 0)
        continue;
      if (string.length() < 2)
        continue;
    }

    llCnt++;

    for (int inti = 0; inti < 10; inti++)
      logMsg[inti] = "";
    logMsg[10] = String.format("%02d", lineCnt);

    if (sysUploadAlias.equals("list"))
       if (checkUploadfileList(string)!=0) 
           continue;
    if (sysUploadAlias.equals("aaa1"))
      if (checUploadfileAaa1(string) != 0)
        continue;

    if (errorCnt == 0) {
        if (sysUploadAlias.equals("list"))
        {
         if (func.dbInsertI2List("MKT_IMFSTP_LIST_T",uploadFileCol,uploadFileDat) != 1) 
             llErr++;
         else 
             llOk++;
        }
      if (sysUploadAlias.equals("aaa1")) {
        if (func.dbInsertI2Aaa1("MKT_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
          llOk++;
        else
          llErr++;
      }
    }
  }

  if (errorCnt > 0) {
      if (sysUploadAlias.equals("list"))
          func.dbDeleteD2List("MKT_IMFSTP_LIST_T");
    if (sysUploadAlias.equals("aaa1"))
      func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
    func.dbInsertEcsNotifyLog(tranSeqStr, errorCnt);
  }

  sqlCommit(1); // 1:commit else rollback

  alertMsg("資料匯入處理筆數 : " + llCnt + ", 成功(" + llOk + "), 重複(" + llErr + "), 失敗("
      + (llCnt - llOk - llErr) + ")");

  tf.closeInputText(fi);
  tf.deleteFile(inputFile);


  return;
}

int checkUploadfileList(String tmpStr) throws Exception {
    Mktm6250Func func = new Mktm6250Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "list_data";

    // ======== [I]nsert table column ========
    uploadFileCol[1]  = "active_code";
    uploadFileCol[2]  = "list_flag";
    uploadFileCol[3]  = "id_p_seqno";
    uploadFileCol[4]  = "acct_type";
    uploadFileCol[5]  = "p_seqno";
    uploadFileCol[6]  = "card_no";
    uploadFileCol[7]  = "ori_card_no";
    uploadFileCol[8]  = "vd_flag";
    uploadFileCol[9]  = "active_seq";

    // ==== insert table content default =====
    uploadFileDat[1]  = wp.itemStr("active_code");
    uploadFileDat[2]  = wp.itemStr("list_flag");
    uploadFileDat[9]  = wp.itemStr("active_seq");

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti=0;inti<1;inti++)
    {
     uploadFileDat[inti] = comm.getStr(tmpStr, inti+1 ,",");
     if (uploadFileDat[inti].length()!=0) okFlag=1;
    }
    if (okFlag == 0)
      return (1);
    //******************************************************************
    errorCnt=0;
    if (wp.itemStr("list_flag").equals("1")) return(0);
  /*
       if (select_crd_idno()!=0)
          {
           error_cnt=1;
           logMsg[0]               = "資料檢核錯誤";           // 原因說明
           logMsg[1]               = "3" ;                     // 錯誤類別
           logMsg[2]               = "1";                      // 欄位位置
           logMsg[3]               = uploadFileDat[0];         // 欄位內容
           logMsg[4]               = "信用卡無此卡人";         // 錯誤說明
           logMsg[5]               = "信用卡人檔無此 ID 資料"; // 欄位說明
           func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
           return(0);
          }
  */
    //******************************************************************
  /*
    if (wp.item_ss("list_flag").equals("6"))
       if (select_dbc_idno()!=0)
          {
           error_cnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3" ;                 // 錯誤類別
           logMsg[2]               = "1";                  // 欄位位置
           logMsg[3]               = uploadFileDat[0];     // 欄位內容
           logMsg[4]               = "VD無此卡人";           // 錯誤說明
           logMsg[5]               = "VD卡人檔無此 ID 資料"; // 欄位說明
           func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
           return(0);
          }
  */

    //******************************************************************
    if (wp.itemStr("list_flag").equals("2"))
       if (selectCrdCard()!=0)
       if (selectDbcCard()!=0)
          {
           errorCnt=1;
           logMsg[0]  = "資料內容錯誤";         // 原因說明
           logMsg[1]  = "2";                    // 錯誤類別
           logMsg[2]  = "1";                    // 欄位位置
           logMsg[3]  = uploadFileDat[0];       // 欄位內容
           logMsg[4]  = "VD無此卡號";          // 錯誤說明
           logMsg[5]  = "卡片檔無此卡號資料";     // 欄位說明

           logMsg[3]  = uploadFileDat[0];     // 欄位內容
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("3"))
       if (selectIpsCard()!=0)
          {
           errorCnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3";                    // 錯誤類別
           logMsg[2]               = "1";                    // 欄位位置
           logMsg[3]               = uploadFileDat[0];       // 欄位內容
           logMsg[4]               = "無此一卡通卡號";          // 錯誤說明
           logMsg[5]               = "一卡通卡片檔無此卡號資料";     // 欄位說明

           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("4"))
       if (selectTscCard()!=0)
          {
           errorCnt=1;
           logMsg[0] = "資料檢核錯誤";       // 原因說明
           logMsg[1] = "3" ;                 // 錯誤類別
           logMsg[2] = "1";                  // 欄位位置
           logMsg[3] = uploadFileDat[0];     // 欄位內容
           logMsg[4] = "無此悠遊卡號";
           logMsg[5] = "悠遊卡片檔無此卡號資料";
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("5"))
       if (selectIchCard()!=0)
          {
           errorCnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3" ;                 // 錯誤類別
           logMsg[2]               = "1";                  // 欄位位置
           logMsg[3]               = uploadFileDat[0];     // 欄位內容
           logMsg[4]               = "無此愛金卡號";
           logMsg[5]               = "愛金卡片檔無此卡號資料";
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************

    return 0;
   }

// ************************************************************************
int selectCrdCard() throws Exception {
    wp.sqlCmd = " select "
            + " id_p_seqno, "
            + " acct_type, "
            + " p_seqno, "
            + " card_no, "
            + " ori_card_no "
            + " from crd_card "
            + " where card_no     = '"
            + uploadFileDat[0]
            + "' ";
    this.sqlSelect();

    if (sqlRowNum <= 0)
        return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[4] = sqlStr("acct_type");
    uploadFileDat[5] = sqlStr("p_seqno");
    uploadFileDat[6] = sqlStr("card_no");
    uploadFileDat[7] = sqlStr("ori_card_no");
    uploadFileDat[8] = "N";
    return (0);
}

// ************************************************************************
int selectDbcCard() throws Exception {
    wp.sqlCmd = " select "
            + " id_p_seqno, "
            + " acct_type, "
            + " p_seqno, "
            + " card_no, "
            + " ori_card_no "
            + " from dbc_card "
            + " where card_no     = '"
            + uploadFileDat[0]
            + "' ";
    this.sqlSelect();

    if (sqlRowNum <= 0)
        return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[4] = sqlStr("acct_type");
    uploadFileDat[5] = sqlStr("p_seqno");
    uploadFileDat[6] = sqlStr("card_no");
    uploadFileDat[7] = sqlStr("ori_card_no");
    uploadFileDat[8] = "Y";
    return (0);
}

// ************************************************************************
int selectIchCard() throws Exception {
    wp.sqlCmd = " select "
            + " a.id_p_seqno, "
            + " a.acct_type, "
            + " a.p_seqno, "
            + " a.card_no, "
            + " a.ori_card_no "
            + " from crd_card a,ich_card b "
            + " where a.card_no     = b.card_no "
            + " and   ich_card_no   = '"
            + uploadFileDat[0]
            + "' ";
    this.sqlSelect();

    if (sqlRowNum <= 0)
        return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[4] = sqlStr("acct_type");
    uploadFileDat[5] = sqlStr("p_seqno");
    uploadFileDat[6] = sqlStr("card_no");
    uploadFileDat[7] = sqlStr("ori_card_no");
    uploadFileDat[8] = "N";
    return (0);
}

// ************************************************************************
int selectIpsCard() throws Exception {
    wp.sqlCmd = " select "
            + " a.id_p_seqno, "
            + " a.acct_type, "
            + " a.p_seqno, "
            + " a.card_no, "
            + " a.ori_card_no "
            + " from crd_card a,ips_card b "
            + " where a.card_no     = b.card_no "
            + " and   ips_card_no   = '"
            + uploadFileDat[0]
            + "' ";
    this.sqlSelect();

    if (sqlRowNum <= 0)
        return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[4] = sqlStr("acct_type");
    uploadFileDat[5] = sqlStr("p_seqno");
    uploadFileDat[6] = sqlStr("card_no");
    uploadFileDat[7] = sqlStr("ori_card_no");
    uploadFileDat[8] = "N";
    return (0);
}

// ************************************************************************
int selectTscCard() throws Exception {
    wp.sqlCmd = " select "
            + " a.id_p_seqno, "
            + " a.acct_type, "
            + " a.p_seqno, "
            + " a.card_no, "
            + " a.ori_card_no "
            + " from crd_card a,tsc_card b "
            + " where a.card_no     = b.card_no "
            + " and   tsc_card_no   = '"
            + uploadFileDat[0]
            + "' ";
    this.sqlSelect();

    if (sqlRowNum <= 0)
        return (1);

    uploadFileDat[3] = sqlStr("id_p_seqno");
    uploadFileDat[4] = sqlStr("acct_type");
    uploadFileDat[5] = sqlStr("p_seqno");
    uploadFileDat[6] = sqlStr("card_no");
    uploadFileDat[7] = sqlStr("ori_card_no");
    uploadFileDat[8] = "N";
    return (0);
}

//************************************************************************
int checUploadfileAaa1(String string) throws Exception {
 mktm02.Mktm6250Func func = new mktm02.Mktm6250Func(wp);

 for (int inti = 0; inti < 50; inti++) {
   uploadFileCol[inti] = "";
   uploadFileDat[inti] = "";
 }
 // =========== [M]edia layout =============
 uploadFileCol[0] = "data_code";
 uploadFileCol[1] = "data_code2";

 // ======== [I]nsert table column ========
 uploadFileCol[2] = "table_name";
 uploadFileCol[3] = "data_key";
 uploadFileCol[4] = "data_type";

 // ==== insert table content default =====
 uploadFileDat[2] = "MKT_FSTP_PARM";
 uploadFileDat[3] = wp.itemStr("active_code")+wp.itemStr("active_seq");
 uploadFileDat[4] = "7";

 int okFlag = 0;
 int errFlag = 0;
 int[] begPos = {1};

 for (int inti = 0; inti < 2; inti++) {
   uploadFileDat[inti] = comm.getStr(string, inti + 1, ",");
   if (uploadFileDat[inti].length() != 0)
     okFlag = 1;
 }
 if (okFlag == 0)
   return (1);
 // ******************************************************************
 if ((uploadFileDat[1].length() != 0) && (uploadFileDat[1].length() < 8))

   if (uploadFileDat[1].length() != 0)
     uploadFileDat[1] =
         "00000000".substring(0, 8 - uploadFileDat[1].length()) + uploadFileDat[1];


 return 0;
}

// ************************************************************************
String listMktImfstpList(String s1, String s2, String s3, String s4) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + s1 + " "
    // + " where active_code = '" + s3 +"' "
            + " where 1 = 1 "
            + sqlCol(s3, "active_code")
    		+ sqlCol(s4, "active_seq");
    sqlSelect(sql1);

    if (sqlRowNum > 0)
        return (sqlStr("column_data_cnt"));

    return ("0");
}

//************************************************************************
public void updateFuncU2() throws Exception
{
mktm02.Mktm6250Func func =new mktm02.Mktm6250Func(wp);
int ll_ok = 0, ll_err = 0;

String[] optData  = wp.itemBuff("opt");
String[] key1Data = wp.itemBuff("data_code");

wp.listCount[0] = key1Data.length;
wp.colSet("IND_NUM", "" + key1Data.length);
//-check duplication-

int del2_flag=0;
for (int ll = 0; ll < key1Data.length; ll++)
   {
    del2_flag=0;
    wp.colSet(ll, "ok_flag", "");

    for (int intm=ll+1;intm<key1Data.length; intm++)
      if ((key1Data[ll].equals(key1Data[intm]))) 
         {
          for (int intx=0;intx<optData.length;intx++) 
           { 
            if (optData[intx].length()!=0) 
            if (((ll+1)==Integer.valueOf(optData[intx]))||
                ((intm+1)==Integer.valueOf(optData[intx])))
               {
                del2_flag=1;
                break;
               }
           }
          if (del2_flag==1) break;

          wp.colSet(ll, "ok_flag", "!");
          ll_err++;
          continue;
         }
   }

if (ll_err > 0)
   {
    alertErr("資料值重複 : " + ll_err);
    return;
   }

//-delete no-approve-
if (func.dbDeleteD2() < 0)
   {
    alertErr(func.getMsg());
    return;
   }

//-insert-
int delete_flag=0;
for (int ll = 0; ll < key1Data.length; ll++)
   {
    delete_flag=0;
    //KEY 不可同時為空字串
    if ((empty(key1Data[ll])))
        continue;

    //-option-ON-
    for (int intm=0;intm<optData.length;intm++)
      {
       if (optData[intm].length()!=0)
       if ((ll+1)==Integer.valueOf(optData[intm]))
          {
           delete_flag=1;
           break;
          }
       }
    if (delete_flag==1) continue;

    func.varsSet("data_code", key1Data[ll]); 

    if (func.dbInsertI2() == 1) ll_ok++;
    else ll_err++;

    //有失敗rollback，無失敗commit
    sqlCommit(ll_ok > 0 ? 1 : 0);
   }
alertMsg("資料存檔處理完成  成功(" + ll_ok + "), 失敗(" + ll_err + ")");

//SAVE後 SELECT
dataReadR2(1);
}

} // End of class
