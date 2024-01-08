/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名     
* 110-11-08  V1.00.03  machao     SQL Injection                                                                                *    
***************************************************************************/
package mktp02;

import mktp02.Mktp6280Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6280 extends BaseProc {
  private String PROGNAME = "帳戶基金(現金回饋)明細檔覆核處理程式108/10/22 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6280Func func = null;
  String rowid;
  String tranSeqno;
  String fstAprFlag = "";
  String orgTabName = "mkt_cashback_dtl_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_fund_code"), "a.fund_code", "like%")
        + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
        + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%");

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
        + "a.aud_type," + "a.acct_type," + "'' as id_no," + "'' as chi_name," + "a.tran_seqno,"
        + "a.tran_code," + "a.end_tran_amt," + "a.fund_code," + "a.fund_name," + "a.crt_user,"
        + "a.crt_date," + "a.id_p_seqno," + "a.p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by mod_time";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commIdNob("comm_id_no");
    commChiNameb("comm_chi_name");

    commTransCode("comm_tran_code");
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
      if (wp.itemStr("kk_tran_seqno").length() == 0) {
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
        + " p_seqno as p_seqno," + " id_p_seqno as id_p_seqno," + "a.aud_type,"
        + "a.tran_seqno as tran_seqno," + "a.crt_user," + "a.fund_code," + "a.fund_name,"
        + "a.acct_type," + "a.tran_code," + "a.beg_tran_amt," + "a.effect_e_date," + "a.mod_reason,"
        + "a.mod_desc," + "a.mod_memo";

    wp.daoTable = controlTabName + " a ";
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
    commTransCode("comm_tran_code");
    commAcctType("comm_acct_type");
    commModReason("comm_mod_reason");
    checkButtonOff();
    tranSeqno = wp.colStr("tran_seqno");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A"))
      dataRead_R3R();
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
//          + " where id_p_seqno = '" + wp.colStr("id_p_seqno") + "'";
      		+ " where 1 = 1 " + " and id_p_seqno = :id_p_seqno";
	  		setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
    } else {
      sql1 = "select " + " corp_no  as id_no, " + " chi_name as chi_name "
          + " from crd_corp a,act_acno b " + " where  a.corp_p_seqno = b.corp_p_seqno "
//          + " and    b.p_seqno = '" + wp.colStr("p_seqno") + "' ";
      	  + " and b.p_seqno = :p_seqno";
      	  setString("p_seqno",wp.colStr(ii, "p_seqno"));
    }
    sqlSelect(sql1);
    wp.colSet("id_no", sqlStr("id_no"));
    wp.itemSet("id_no", sqlStr("id_no"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.itemSet("chi_name", sqlStr("chi_name"));

    sql1 = "select " + " nvl(sum(end_tran_amt),0) as rem_amt " + " from  mkt_cashback_dtl "
//        + " where fund_code ='" + wp.colStr("fund_code") + "' " 
//    	+ " and   p_seqno = '"+ wp.colStr("p_seqno") + "' ";
    	+ " where 1 = 1 " + " and fund_code = :fund_code "
    	+ " and p_seqno = :p_seqno ";
    	setString("fund_code",wp.colStr("fund_code"));
    	setString("p_seqno",wp.colStr("p_seqno"));
    sqlSelect(sql1);
    wp.colSet("total_amt", sqlStr("rem_amt"));
    wp.itemSet("total_amt", sqlStr("rem_amt"));

  }

  // ************************************************************************
  public void dataRead_R3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_CASHBACK_DTL";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.tran_seqno as tran_seqno," + "a.crt_user as bef_crt_user,"
        + "a.fund_code as bef_fund_code," + "a.fund_name as bef_fund_name,"
        + "a.acct_type as bef_acct_type," + "a.tran_code as bef_tran_code,"
        + "a.beg_tran_amt as bef_beg_tran_amt," + "a.effect_e_date as bef_effect_e_date,"
        + "a.mod_reason as bef_mod_reason," + "a.mod_desc as bef_mod_desc,"
        + "a.mod_memo as bef_mod_memo";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(tranSeqno, "a.tran_seqno");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAcctType("comm_acct_type");
    commTransCode("comm_tran_code");
    commModReason("comm_mod_reason");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("fund_code").equals(wp.colStr("bef_fund_code")))
      wp.colSet("opt_fund_code", "Y");

    if (!wp.colStr("fund_name").equals(wp.colStr("bef_fund_name")))
      wp.colSet("opt_fund_name", "Y");

    if (!wp.colStr("acct_type").equals(wp.colStr("bef_acct_type")))
      wp.colSet("opt_acct_type", "Y");
    commAcctType("comm_acct_type");
    commAcctType("comm_bef_acct_type", 1);

    if (!wp.colStr("id_no").equals(wp.colStr("bef_id_no")))
      wp.colSet("opt_id_no", "Y");

    if (!wp.colStr("chi_name").equals(wp.colStr("bef_chi_name")))
      wp.colSet("opt_chi_name", "Y");

    if (!wp.colStr("tran_code").equals(wp.colStr("bef_tran_code")))
      wp.colSet("opt_tran_code", "Y");
    commTransCode("comm_tran_code");
    commTransCode("comm_bef_tran_code");

    if (!wp.colStr("total_amt").equals(wp.colStr("bef_total_amt")))
      wp.colSet("opt_total_amt", "Y");

    if (!wp.colStr("beg_tran_amt").equals(wp.colStr("bef_beg_tran_amt")))
      wp.colSet("opt_beg_tran_amt", "Y");

    if (!wp.colStr("effect_e_date").equals(wp.colStr("bef_effect_e_date")))
      wp.colSet("opt_effect_e_date", "Y");

    if (!wp.colStr("mod_reason").equals(wp.colStr("bef_mod_reason")))
      wp.colSet("opt_mod_reason", "Y");
    commModReason("comm_mod_reason");
    commModReason("comm_bef_mod_reason", 1);

    if (!wp.colStr("mod_desc").equals(wp.colStr("bef_mod_desc")))
      wp.colSet("opt_mod_desc", "Y");

    if (!wp.colStr("mod_memo").equals(wp.colStr("bef_mod_memo")))
      wp.colSet("opt_mod_memo", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("fund_code", "");
      wp.colSet("fund_name", "");
      wp.colSet("acct_type", "");
      wp.colSet("id_no", "");
      wp.colSet("chi_name", "");
      wp.colSet("tran_code", "");
      wp.colSet("total_amt", "");
      wp.colSet("beg_tran_amt", "");
      wp.colSet("effect_e_date", "");
      wp.colSet("mod_reason", "");
      wp.colSet("mod_desc", "");
      wp.colSet("mod_memo", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("fund_code").length() == 0)
      wp.colSet("opt_fund_code", "Y");

    if (wp.colStr("fund_name").length() == 0)
      wp.colSet("opt_fund_name", "Y");

    if (wp.colStr("acct_type").length() == 0)
      wp.colSet("opt_acct_type", "Y");



    if (wp.colStr("tran_code").length() == 0)
      wp.colSet("opt_tran_code", "Y");


    if (wp.colStr("beg_tran_amt").length() == 0)
      wp.colSet("opt_beg_tran_amt", "Y");

    if (wp.colStr("effect_e_date").length() == 0)
      wp.colSet("opt_effect_e_date", "Y");

    if (wp.colStr("mod_reason").length() == 0)
      wp.colSet("opt_mod_reason", "Y");

    if (wp.colStr("mod_desc").length() == 0)
      wp.colSet("opt_mod_desc", "Y");

    if (wp.colStr("mod_memo").length() == 0)
      wp.colSet("opt_mod_memo", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    mktp02.Mktp6280Func func = new mktp02.Mktp6280Func(wp);

    String[] lsTranSeqno = wp.itemBuff("tran_seqno");
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
     // if (lsCrtUser[rr].equals(wp.loginUser)) {
     //   ilAuth++;
     //   wp.colSet(rr, "ok_flag", "F");
     //   continue;
     // }

      func.varsSet("tran_seqno", lsTranSeqno[rr]);
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
        commAcctType("comm_acct_type");
        commIdNob("comm_id_no");
        commChiNameb("comm_chi_name");
        commTransCode("comm_tran_code");
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
  public void dddwSelect() {
    String ls_sql = "";
    try {
      if ((wp.respHtml.equals("mktp6280"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_fund_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_fund_code");
          wp.initOption = "";
        }
        this.dddwList("dddw_fund_nameb", "vmkt_fund_name", "trim(fund_code)", "trim(fund_name)",
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
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 " 
//    	  + " and   acct_type = '" + wp.colStr(ii, befStr + "acct_type") + "'";
      	  + " and acct_type = :acct_type ";
      	  setString("acct_type",wp.colStr(ii, befStr + "acct_type"));
      if (wp.colStr(ii, befStr + "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commModReason(String reason) throws Exception {
    commModReason(reason, 0);
    return;
  }

  // ************************************************************************
  public void commModReason(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, befStr + "mod_reason") + "'"
          + " and wf_id = :mod_reason "
          + " and   wf_type = 'ADJMOD_REASON' ";
      	  setString("mod_reason",wp.colStr(ii, befStr + "mod_reason"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commIdNob(String idno) throws Exception {
    commIdNob(idno, 0);
    return;
  }

  // ************************************************************************
  public void commIdNob(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      if (wp.colStr(ii, "id_p_seqno").length() != 0) {
        sql1 = "select " + " id_no as column_id_no " + " from crd_idno " 
//        	+ " where id_p_seqno = '"+ wp.colStr(ii, "id_p_seqno") + "'";
        	+ " where 1 = 1 " + " and id_p_seqno = :id_p_seqno";
        	setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
      } else {
        sql1 = "select " + " corp_no as column_id_no " + " from crd_corp a,act_acno b "
            + " where  a.corp_p_seqno = b.corp_p_seqno " 
//        	+ " and    b.p_seqno = '" + wp.colStr(ii, "p_seqno") + "' ";
        	+ " and b.p_seqno = :p_seqno";
        	setString("p_seqno",wp.colStr(ii, "p_seqno"));
      }
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commChiNameb(String name) throws Exception {
    commChiNameb(name, 0);
    return;
  }

  // ************************************************************************
  public void commChiNameb(String columnData1, int befType) throws Exception {
    String columnData = "";
    String sql1 = "";
    String befStr = "";
    if (befType == 1)
      befStr = "bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      if (wp.colStr(ii, "id_p_seqno").length() != 0) {
        sql1 = "select " + " chi_name as column_chi_name " + " from crd_idno "
//            + " where id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
        	+ " where 1 = 1 " + " and id_p_seqno = :id_p_seqno";
        	setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
      } else {
        sql1 = "select " + " chi_name as column_chi_name " + " from crd_corp a,act_acno b "
            + " where  a.corp_p_seqno = b.corp_p_seqno " 
//        	+ " and    b.p_seqno = '" + wp.colStr(ii, "p_seqno") + "' ";
        	+ " and b.p_seqno = :p_seqno";
        	setString("p_seqno",wp.colStr(ii, "p_seqno"));
      }
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chi_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commTransCode(String cde1) throws Exception {
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
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

}  // End of class
