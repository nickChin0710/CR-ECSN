/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/12  V1.00.01   Ray Ho        Initial                              *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名            
* 110-11-08  V1.00.03  machao     SQL Injection                                                                         *    
***************************************************************************/
package mktp02;

import mktp02.Mktp6255Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6255 extends BaseProc {
  private String PROGNAME = "首刷禮活動卡片明細覆核作業處理程式108/06/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6255Func func = null;
  String rowid;// kk2;
  String caedNo, activeCode;
  String fstAprFlag = "";
  String orgTabName = "mkt_fstp_carddtl_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batch_no = "";
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
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date");

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
        + "a.aud_type," + "a.acct_type," + "'' as id_no," + "'' as chi_name," + "a.card_no,"
        + "a.active_code," + "a.active_type," + "a.mod_user,"
        + "to_char(mod_time,'yyyymmdd') as mod_time," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code,a.crt_date";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    commActiveCode("comm_active_code");

    commActiveType1("comm_active_type");
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
      if (wp.itemStr("kk_card_no").length() == 0) {
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
        + "a.card_no as card_no," + "a.active_code as active_code," + "a.mod_user,"
        + "a.active_type," + "a.bonus_type," + "a.beg_tran_bp," + "a.fund_code," + "a.beg_tran_amt,"
        + "a.group_type," + "a.prog_code," + "a.prog_s_date," + "a.prog_e_date," + "a.gift_no,"
        + "a.tran_pt," + "a.spec_gift_no," + "a.spec_gift_cnt," + "a.mod_desc," + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(caedNo, "a.card_no") + sqlCol(activeCode, "a.active_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commActiveType("comm_active_type");
    commGroupType("comm_group_type");
    commActiveCode("comm_active_code");
    commBonusType("comm_bonus_type");
    commFundCode1("comm_fund_code");
    commProgCode("comm_prog_code");
    commGiftNo("comm_gift_no");
    commSpecGiftNo("comm_spec_gift_no");
    checkButtonOff();
    caedNo = wp.colStr("card_no");
    activeCode = wp.colStr("active_code");
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

    sql1 = "select " + " id_no as id_no, " + " chi_name as chi_name " + " from crd_idno "
//        + " where id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
    	+ " where 1 = 1" + " and id_p_seqno = :id_p_seqno ";
    	setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
    sqlSelect(sql1);
    wp.colSet(ii, "id_no", sqlStr("id_no"));
    wp.colSet(ii, "chi_name", sqlStr("chi_name"));

  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_FSTP_CARDDTL";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.card_no as card_no," + "a.active_code as active_code," + "a.mod_user as bef_mod_user,"
        + "a.active_type as bef_active_type," + "a.bonus_type as bef_bonus_type,"
        + "a.beg_tran_bp as bef_beg_tran_bp," + "a.fund_code as bef_fund_code,"
        + "a.beg_tran_amt as bef_beg_tran_amt," + "a.group_type as bef_group_type,"
        + "a.prog_code as bef_prog_code," + "a.prog_s_date as bef_prog_s_date,"
        + "a.prog_e_date as bef_prog_e_date," + "a.gift_no as bef_gift_no,"
        + "a.tran_pt as bef_tran_pt," + "a.spec_gift_no as bef_spec_gift_no,"
        + "a.spec_gift_cnt as bef_spec_gift_cnt," + "a.mod_desc as bef_mod_desc,"
        + "a.id_p_seqno as bef_id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(caedNo, "a.card_no") + sqlCol(activeCode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commActiveCode("comm_active_code");
    commActiveType("comm_active_type");
    commBonusType("comm_bonus_type");
    commFundCode1("comm_fund_code");
    commGroupType("comm_group_type");
    commProgCode("comm_prog_code");
    commGiftNo("comm_gift_no");
    commSpecGiftNo("comm_spec_gift_no");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("active_type").equals(wp.colStr("bef_active_type")))
      wp.colSet("opt_active_type", "Y");
    commActiveType("comm_active_type");
    commActiveType("comm_bef_active_type");

    if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
      wp.colSet("opt_bonus_type", "Y");
    commBonusType("comm_bonus_type");
    commBonusType("comm_bef_bonus_type", 1);

    if (!wp.colStr("beg_tran_bp").equals(wp.colStr("bef_beg_tran_bp")))
      wp.colSet("opt_beg_tran_bp", "Y");

    if (!wp.colStr("fund_code").equals(wp.colStr("bef_fund_code")))
      wp.colSet("opt_fund_code", "Y");
    commFundCode1("comm_fund_code");
    commFundCode1("comm_bef_fund_code", 1);

    if (!wp.colStr("beg_tran_amt").equals(wp.colStr("bef_beg_tran_amt")))
      wp.colSet("opt_beg_tran_amt", "Y");

    if (!wp.colStr("group_type").equals(wp.colStr("bef_group_type")))
      wp.colSet("opt_group_type", "Y");
    commGroupType("comm_group_type");
    commGroupType("comm_bef_group_type");

    if (!wp.colStr("prog_code").equals(wp.colStr("bef_prog_code")))
      wp.colSet("opt_prog_code", "Y");
    commProgCode("comm_prog_code");
    commProgCode("comm_bef_prog_code", 1);

    if (!wp.colStr("prog_s_date").equals(wp.colStr("bef_prog_s_date")))
      wp.colSet("opt_prog_s_date", "Y");

    if (!wp.colStr("prog_e_date").equals(wp.colStr("bef_prog_e_date")))
      wp.colSet("opt_prog_e_date", "Y");

    if (!wp.colStr("gift_no").equals(wp.colStr("bef_gift_no")))
      wp.colSet("opt_gift_no", "Y");
    commGiftNo("comm_gift_no");
    commGiftNo("comm_bef_gift_no", 1);

    if (!wp.colStr("tran_pt").equals(wp.colStr("bef_tran_pt")))
      wp.colSet("opt_tran_pt", "Y");

    if (!wp.colStr("spec_gift_no").equals(wp.colStr("bef_spec_gift_no")))
      wp.colSet("opt_spec_gift_no", "Y");
    commSpecGiftNo("comm_spec_gift_no");
    commSpecGiftNo("comm_bef_spec_gift_no", 1);

    if (!wp.colStr("spec_gift_cnt").equals(wp.colStr("bef_spec_gift_cnt")))
      wp.colSet("opt_spec_gift_cnt", "Y");

    if (!wp.colStr("mod_desc").equals(wp.colStr("bef_mod_desc")))
      wp.colSet("opt_mod_desc", "Y");

    if (!wp.colStr("id_p_seqno").equals(wp.colStr("bef_id_p_seqno")))
      wp.colSet("opt_id_p_seqno", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("active_type", "");
      wp.colSet("bonus_type", "");
      wp.colSet("beg_tran_bp", "");
      wp.colSet("fund_code", "");
      wp.colSet("beg_tran_amt", "");
      wp.colSet("group_type", "");
      wp.colSet("prog_code", "");
      wp.colSet("prog_s_date", "");
      wp.colSet("prog_e_date", "");
      wp.colSet("gift_no", "");
      wp.colSet("tran_pt", "");
      wp.colSet("spec_gift_no", "");
      wp.colSet("spec_gift_cnt", "");
      wp.colSet("mod_desc", "");
      wp.colSet("id_p_seqno", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("active_type").length() == 0)
      wp.colSet("opt_active_type", "Y");

    if (wp.colStr("bonus_type").length() == 0)
      wp.colSet("opt_bonus_type", "Y");

    if (wp.colStr("beg_tran_bp").length() == 0)
      wp.colSet("opt_beg_tran_bp", "Y");

    if (wp.colStr("fund_code").length() == 0)
      wp.colSet("opt_fund_code", "Y");

    if (wp.colStr("beg_tran_amt").length() == 0)
      wp.colSet("opt_beg_tran_amt", "Y");

    if (wp.colStr("group_type").length() == 0)
      wp.colSet("opt_group_type", "Y");

    if (wp.colStr("prog_code").length() == 0)
      wp.colSet("opt_prog_code", "Y");

    if (wp.colStr("prog_s_date").length() == 0)
      wp.colSet("opt_prog_s_date", "Y");

    if (wp.colStr("prog_e_date").length() == 0)
      wp.colSet("opt_prog_e_date", "Y");

    if (wp.colStr("gift_no").length() == 0)
      wp.colSet("opt_gift_no", "Y");

    if (wp.colStr("tran_pt").length() == 0)
      wp.colSet("opt_tran_pt", "Y");

    if (wp.colStr("spec_gift_no").length() == 0)
      wp.colSet("opt_spec_gift_no", "Y");

    if (wp.colStr("spec_gift_cnt").length() == 0)
      wp.colSet("opt_spec_gift_cnt", "Y");

    if (wp.colStr("mod_desc").length() == 0)
      wp.colSet("opt_mod_desc", "Y");

    if (wp.colStr("id_p_seqno").length() == 0)
      wp.colSet("opt_id_p_seqno", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    mktp02.Mktp6255Func func = new mktp02.Mktp6255Func(wp);

    String[] ls_card_no = wp.itemBuff("card_no");
    String[] ls_active_code = wp.itemBuff("active_code");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] ls_rowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + ls_rowid[rr]);

      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("card_no", ls_card_no[rr]);
      func.varsSet("active_code", ls_active_code[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", ls_rowid[rr]);
      wp.itemSet("wprowid", ls_rowid[rr]);
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
        commAcctType("comm_acct_type");
        commIdNo("comm_id_no");
        commChiName("comm_chi_name");
        commActiveCode("comm_active_code");
        commActiveType1("comm_active_type");
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
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktp6255"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
        }
        this.dddwList("dddw_active_code", "mkt_fstp_parm", "trim(active_code)", "trim(active_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
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
  public void commActiveCode(String code) throws Exception {
    commActiveCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commActiveCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from mkt_fstp_parm "
          + " where 1 = 1 " 
//    	  + " and   active_code = '" + wp.colStr(ii, befStr + "active_code")+ "'"
          + " and active_code = :active_code"
          ;
      	  setString("active_code",wp.colStr(ii, befStr + "active_code"));
      if (wp.colStr(ii, befStr + "active_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_active_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commBonusType(String bonusType) throws Exception {
    commBonusType(bonusType, 0);
    return;
  }

  // ************************************************************************
  public void commBonusType(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, befStr + "bonus_TYPE") + "'"
          + " and wf_id = :bonus_TYPE "
          + " and   wf_type = 'BONUS_NAME' ";
      	  setString("bonus_TYPE",wp.colStr(ii, befStr + "bonus_TYPE"));
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commFundCode1(String code) throws Exception {
    commFundCode1(code, 0);
    return;
  }

  // ************************************************************************
  public void commFundCode1(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " fund_name as column_fund_name " + " from vmkt_fund_name "
          + " where 1 = 1 " 
//    	  + " and   fund_code = '" + wp.colStr(ii, befStr + "fund_code") + "'";
      	  + " and fund_code = :fund_code ";
      	  setString("fund_code",wp.colStr(ii, befStr + "fund_code"));
      if (wp.colStr(ii, befStr + "fund_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_fund_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commProgCode(String code) throws Exception {
    commProgCode(code, 0);
    return;
  }

  // ************************************************************************
  public void commProgCode(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " prog_desc as column_prog_desc " + " from ibn_prog " + " where 1 = 1 "
//          + " and   prog_code = '" + wp.colStr(ii, befStr + "prog_code") + "'"
//          + " and   prog_s_date = '" + wp.colStr(ii, befStr + "prog_s_date") + "'";
      	  + " and prog_code = :prog_code " 
      	  + " and prog_s_date = :prog_s_date ";
      	  setString("prog_code",wp.colStr(ii, befStr + "prog_code"));
      	  setString("prog_s_date",wp.colStr(ii, befStr + "prog_s_date"));
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_prog_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commGiftNo(String giftNo) throws Exception {
    commGiftNo(giftNo, 0);
    return;
  }

  // ************************************************************************
  public void commGiftNo(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " gift_name as column_gift_name " + " from ibn_prog_gift "
          + " where 1 = 1 " 
//    	  + " and   prog_code = '" + wp.colStr(ii, befStr + "prog_code") + "'"
//          + " and   prog_s_date = '" + wp.colStr(ii, befStr + "prog_s_date") + "'"
//          + " and   gift_no = '" + wp.colStr(ii, befStr + "gift_no") + "'";
	      + " and prog_code = :prog_code " 
	  	  + " and prog_s_date = :prog_s_date "
	      + " and gift_no = :gift_no ";
	  	  setString("prog_code",wp.colStr(ii, befStr + "prog_code"));
	  	  setString("prog_s_date",wp.colStr(ii, befStr + "prog_s_date"));
	  	  setString("gift_no",wp.colStr(ii, befStr + "gift_no"));
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_gift_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commSpecGiftNo(String giftNo) throws Exception {
    commSpecGiftNo(giftNo, 0);
    return;
  }

  // ************************************************************************
  public void commSpecGiftNo(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " gift_name as column_gift_name " + " from mkt_spec_gift "
          + " where 1 = 1 " 
//    	  + " and   gift_no = '" + wp.colStr(ii, befStr + "spec_gift_no") + "'"
    	  + " and gift_no = :spec_gift_no "
          + " and   gift_group = '1' ";
      	  setString("spec_gift_no",wp.colStr(ii, befStr + "spec_gift_no"));
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_gift_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commAcctType(String type) throws Exception {
    commAcctType(type, 0);
    return;
  }

  // ************************************************************************
  public void commAcctType(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " CHIN_NAME as column_CHIN_NAME " + " from ptr_acct_type "
          + " where 1 = 1 " 
//    	  + " and   acct_type = '" + wp.colStr(ii, befStr + "acct_type") + "'";
      	  + " and acct_type = :acct_type ";
      	  setString("acct_type",wp.colStr(ii, befStr + "acct_type"));
      if (wp.colStr(ii, befStr + "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_CHIN_NAME");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commIdNo(String idNo) throws Exception {
    commIdNo(idNo, 0);
    return;
  }

  // ************************************************************************
  public void commIdNo(String columnData1, int bef_type) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (bef_type == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, befStr + "id_p_seqno") + "'";
      	  + " and id_p_seqno = :id_p_seqno ";
      	  setString("id_p_seqno",wp.colStr(ii, befStr + "id_p_seqno"));
      if (wp.colStr(ii, befStr + "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_id_no");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commChiName(String name) throws Exception {
    comm_chi_name(name, 0);
    return;
  }

  // ************************************************************************
  public void comm_chi_name(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chi_name as column_chi_name " + " from crd_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, befStr + "id_p_seqno") + "'";
			+ " and id_p_seqno = :id_p_seqno ";
  	  		setString("id_p_seqno",wp.colStr(ii, befStr + "id_p_seqno"));
      if (wp.colStr(ii, befStr + "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_chi_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commActiveType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"1.紅利", "2.基金", "3.豐富點", "4.贈品"};
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
  public void commGroupType(String cde1) throws Exception {
    String[] cde = {"group_type", "1", "2", "3", "4"};
    String[] txt = {"", "限信>用卡兌換(限01,05,06)", "限 VD卡兌換(限90)", "全部任一卡片兌>換(01,05,06,90)", "限特定卡號兌換"};
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
  public void commActiveType1(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"紅利", "基金", "豐富點", "贈品"};
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
