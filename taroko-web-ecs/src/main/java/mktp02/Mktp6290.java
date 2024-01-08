/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/14  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名       
* 110-11-08  V1.00.03  machao     SQL Injection                                                                              *    
***************************************************************************/
package mktp02;

import mktp02.Mktp6290Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6290 extends BaseProc {
  private String PROGNAME = "帳戶紅利點數線上調整作業處理程式108/10/14 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6290Func func = null;
  String rowid;
  String tranSeqno;
  String fstAprFlag = "";
  String orgTabName = "mkt_bonus_dtl_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlCol(wp.itemStr("ex_bonus_type"), "a.bonus_type", "like%")
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
        + "a.aud_type," + "a.acct_type," + "'' as id_no," + "c.chi_name," + "a.tran_seqno,"
        + "a.tran_code," + "a.beg_tran_bp," + "a.active_code," + "a.active_name," + "a.mod_reason,"
        + "a.crt_user," + "a.crt_date," + "a.bonus_type," + "a.id_p_seqno," + "a.p_seqno";

    wp.daoTable = controlTabName + " a " + "JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " " + " order by tran_date desc,tran_time desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commIdNob("comm_id_no");
    commModReason("comm_mod_reason");
    commBonusType("comm_bonus_type");

    commTranCode("comm_tran_code");
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
        + " id_p_seqno as id_p_seqno," + "a.aud_type," + "a.tran_seqno as tran_seqno,"
        + "a.crt_user," + "a.acct_type," + "a.bonus_type," + "a.active_code," + "a.active_name,"
        + "a.tran_code," + "a.beg_tran_bp," + "a.tax_flag," + "a.effect_e_date," + "a.mod_reason,"
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
    commTranCode("comm_tran_code");
    commTaxFlag("comm_tax_flag");
    commAcctType("comm_acct_type");
    commBonusType("comm_bonus_type");
    commModReason("comm_mod_reason");
    checkButtonOff();
    tranSeqno = wp.colStr("tran_seqno");
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
//        + " where id_p_seqno = '" + wp.colStr("id_p_seqno") + "'";
    	+ " where 1 = 1 " + " and id_p_seqno = :id_p_seqno";
    	setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
    sqlSelect(sql1);
    wp.colSet("id_no", sqlStr("id_no"));
    wp.itemSet("id_no", sqlStr("id_no"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.itemSet("chi_name", sqlStr("chi_name"));


  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_BONUS_DTL";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.tran_seqno as tran_seqno," + "a.crt_user as bef_crt_user,"
        + "a.acct_type as bef_acct_type," + "a.bonus_type as bef_bonus_type,"
        + "a.active_code as bef_active_code," + "a.active_name as bef_active_name,"
        + "a.tran_code as bef_tran_code," + "a.beg_tran_bp as bef_beg_tran_bp,"
        + "a.tax_flag as bef_tax_flag," + "a.effect_e_date as bef_effect_e_date,"
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
    commBonusType("comm_bonus_type");
    commTranCode("comm_tran_code");
    commTaxFlag("comm_tax_flag");
    commModReason("comm_mod_reason");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("acct_type").equals(wp.colStr("bef_acct_type")))
      wp.colSet("opt_acct_type", "Y");
    commAcctType("comm_acct_type");
    commAcctType("comm_bef_acct_type", 1);

    if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
      wp.colSet("opt_bonus_type", "Y");
    commBonusType("comm_bonus_type");
    commBonusType("comm_bef_bonus_type", 1);

    if (!wp.colStr("active_code").equals(wp.colStr("bef_active_code")))
      wp.colSet("opt_active_code", "Y");

    if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
      wp.colSet("opt_active_name", "Y");

    if (!wp.colStr("id_no").equals(wp.colStr("bef_id_no")))
      wp.colSet("opt_id_no", "Y");

    if (!wp.colStr("chi_name").equals(wp.colStr("bef_chi_name")))
      wp.colSet("opt_chi_name", "Y");

    if (!wp.colStr("tran_code").equals(wp.colStr("bef_tran_code")))
      wp.colSet("opt_tran_code", "Y");
    commTranCode("comm_tran_code");
    commTranCode("comm_bef_tran_code");

    if (!wp.colStr("beg_tran_bp").equals(wp.colStr("bef_beg_tran_bp")))
      wp.colSet("opt_beg_tran_bp", "Y");

    if (!wp.colStr("tax_flag").equals(wp.colStr("bef_tax_flag")))
      wp.colSet("opt_tax_flag", "Y");
    commTaxFlag("comm_tax_flag");
    commTaxFlag("comm_bef_tax_flag");

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
      wp.colSet("acct_type", "");
      wp.colSet("bonus_type", "");
      wp.colSet("active_code", "");
      wp.colSet("active_name", "");
      wp.colSet("id_no", "");
      wp.colSet("chi_name", "");
      wp.colSet("tran_code", "");
      wp.colSet("beg_tran_bp", "");
      wp.colSet("tax_flag", "");
      wp.colSet("effect_e_date", "");
      wp.colSet("mod_reason", "");
      wp.colSet("mod_desc", "");
      wp.colSet("mod_memo", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("acct_type").length() == 0)
      wp.colSet("opt_acct_type", "Y");

    if (wp.colStr("bonus_type").length() == 0)
      wp.colSet("opt_bonus_type", "Y");

    if (wp.colStr("active_code").length() == 0)
      wp.colSet("opt_active_code", "Y");

    if (wp.colStr("active_name").length() == 0)
      wp.colSet("opt_active_name", "Y");



    if (wp.colStr("tran_code").length() == 0)
      wp.colSet("opt_tran_code", "Y");

    if (wp.colStr("beg_tran_bp").length() == 0)
      wp.colSet("opt_beg_tran_bp", "Y");

    if (wp.colStr("tax_flag").length() == 0)
      wp.colSet("opt_tax_flag", "Y");

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
    mktp02.Mktp6290Func func = new mktp02.Mktp6290Func(wp);

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
      //if (lsCrtUser[rr].equals(wp.loginUser)) {
      //  ilAuth++;
      //  wp.colSet(rr, "ok_flag", "F");
       // continue;
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
        commModReason("comm_mod_reason");
        commBonusType("comm_bonus_type");
        commTranCode("comm_tran_code");
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
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktp6290"))) {
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
        
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_crt_user").length()>0)
           {
           wp.optionKey = wp.colStr("ex_crt_user");
           }
        lsSql = "";
        lsSql =  procDynamicDddwCrtuser1(wp.colStr("ex_crt_user"));
        wp.optionKey = wp.colStr("ex_crt_user");
        dddwList("dddw_crt_user_1", lsSql);        
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
  public void commBonusType(String type) throws Exception {
    commBonusType(type, 0);
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
          + " and   wf_type = 'BONUS_NAME' " 
//    	  + " and   wf_id = '" + wp.colStr(ii, befStr + "bonus_type") + "'";
      	  + " and wf_id = :bonus_type ";
      	  setString("bonus_type",wp.colStr(ii, befStr + "bonus_type"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
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
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " 
//    		  + " where id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
		      + " where 1 = 1 " + " and id_p_seqno = :id_p_seqno";
			  setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
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
  public void commTaxFlag(String cde1) throws Exception {
    String[] cde = {"N", "Y"};
    String[] txt = {"免稅", "應稅"};
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
//************************************************************************
public String procDynamicDddwCrtuser1(String string)  throws Exception
{
  String lsSql = "";

  lsSql = " select "
         + " b.crt_user as db_code, "
         + " max(b.crt_user||' '||a.usr_cname) as db_desc "
         + " from sec_user a,mkt_bonus_dtl_t b "
         + " where a.usr_id = b.crt_user "
         + " group by b.crt_user "
         ;

  return lsSql;
}

}  // End of class
