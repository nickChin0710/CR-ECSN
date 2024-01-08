/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/07/26  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名   
* 110-11-03  V1.00.03  machao     SQL Injection                                                                                  * 
* 111-10-14  V1.00.04  Ryan          modify     * 
***************************************************************************/
package mktq02;

import mktq02.Mktq6290Func;
import ofcapp.AppMsg;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq6290 extends BaseEdit {
  private String PROGNAME = "帳戶紅利明細檔查詢作業處理程式111/10/14 V1.00.04";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq6290Func func = null;
  String rowid;// kk2, kk3, kk4;
  String orgTabName = "mkt_bonus_dtl";
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
    } else if (eqIgno(wp.buttonCode, "T")) {/* 動態查詢 */
      querySelect1();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "T2")) {/* 動態查詢 */
      strAction = "T2";
      querySelect1();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%")
        + sqlChkEx(wp.itemStr("ex_id_no"), "4", "");
    if(wp.itemEq("ex_select_type", "2")) {
  		wp.whereStr += sqlCol(wp.itemStr("ex_tran_date1"), "a.tran_date", ">=")
  				+ sqlCol(wp.itemStr("ex_tran_date2"), "a.tran_date", "<=") + " and a.apr_flag = 'Y'";
      }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    if(wp.itemEq("ex_select_type", "1")) {
    	queryRead();
    }else {
    	queryRead2();
    }
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "a.p_seqno, " + "a.bonus_type, " + "max(acct_type) as acct_type,"
        + "max('') as id_no," + "sum(end_tran_bp+res_tran_bp) as TOT_END_TRAN_BP,"
        + "sum(res_tran_bp) as RES_TRAN_bp," + "sum(end_tran_bp+res_tran_bp) as total_TRAN_bp,"
        + "sum(decode(sign(to_char(add_months(sysdate,-3),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail3_BP,"
        + "sum(decode(sign(to_char(add_months(sysdate,-6),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail6_BP,"
        + "max(tran_date) as tran_date," + "count(*) as data_cnt,"
        + "max(id_p_seqno) as id_p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " group by a.p_seqno,a.bonus_type"
        + " order by a.p_seqno,a.bonus_type,tran_date,bonus_type";

    wp.pageCountSql = "select count(*) from ( " + " select distinct a.p_seqno,a.bonus_type"
        + " from " + wp.daoTable + " " + wp.queryWhere + " )";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commIdNo("comm_id_no");
    commBonusType("comm_bonus_type");


    // list_wkdata();
    wp.setPageValue();
  }
  
  public void queryRead2() throws Exception {
		if (wp.colStr("org_tab_name").length() > 0)
			controlTabName = wp.colStr("org_tab_name");
		else
			controlTabName = orgTabName;

		wp.pageControl();
		wp.selectSQL = "hex(a.rowid) as rowid ,a.acct_type, a.tran_date, a.tran_code, a.beg_tran_bp ,trim(mod_memo) as mod_memo , trim(mod_desc) as mod_desc, a.tran_time ";
		wp.daoTable = controlTabName + " a join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
		wp.whereOrder = " order by a.tran_date desc , a.tran_time desc";
		pageQuery();
		wp.setListCount(2);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		commTranCode("comm_tran_code");
		commModMemo("mod_memo", "mod_desc");

		wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    wp.colSet("p_seqno", itemKk("data_k2"));
    wp.colSet("bonus_type", itemKk("data_k3"));
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  public void querySelect1() throws Exception {
    controlTabName = orgTabName;

    rowid = itemKk("data_k1");
    qFrom = 2;
    dataRead();
    if(strAction.equals("T2")) {
    	wp.colSet("ex_select_type", "2");
    }
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
        + " ROW_NUMBER()OVER() as ser_num, " + "a.bonus_type," + "a.active_code,"
        + "b.acct_key as acct_key," + "c.chi_name as chi_name," + "a.active_name," + "a.acct_type,"
        + "a.tran_date," + "a.tran_time," + "a.tran_code," + "a.tran_pgm," + "a.beg_tran_bp,"
        + "a.end_tran_bp," + "a.res_tran_bp," + "a.res_upd_date," + "a.tax_flag,"
        + "a.effect_e_date," + "a.tran_seqno," + "a.proc_month," + "a.acct_date," + "a.mod_desc,"
        + "a.mod_memo," + "a.mod_reason," + "a.id_p_seqno," + "a.p_seqno," + "a.apr_date,"
        + "a.apr_user," + "a.crt_date," + "a.crt_user,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm";

    wp.daoTable = controlTabName + " a " + "JOIN act_acno b " + "ON a.p_seqno = b.p_seqno "
        + "JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(itemKk("data_k1"), "a.bonus_type")
          + sqlCol(itemKk("data_k2"), "a.acct_type") + sqlCol(itemKk("data_k3"), "b.acct_key");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlCol(wp.colStr("p_seqno"), "a.p_seqno")
          + sqlCol(wp.colStr("bonus_type"), "a.bonus_type")
          + " order by tran_date desc,tran_time desc,tran_seqno desc";
    } else {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    wp.setListCount(1);
    commBonusType("comm_bonus_type");
    commAcctType("comm_acct_type");
    commTranCode("comm_tran_code");
    commModReason("comm_mod_reason");
    wp.colSet("", itemKk("data_kN"));

    if (qFrom != 0) {
      commBonusType("comm_bonus_type");
      commAcctType("comm_acct_type");
      commTranCode("comm_tran_code");
      commTaxFlag("comm_tax_flag");
      commModReason("comm_mod_reason");
    }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq6290Func func = new mktq02.Mktq6290Func(wp);

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
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktq6290"))) {
//        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_acct_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_acct_type");
        }
        this.dddwList("dddw_acct_type1", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where bonus_flag='Y'");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if (wp.itemStr("ex_query_table").equals("2"))
      orgTabName = "mkt_bonus_dtl_hst";
    else
      orgTabName = "mkt_bonus_dtl";

    controlTabName = orgTabName.toUpperCase();
    wp.colSet("control_tab_name", controlTabName);


    if ((wp.itemStr("ex_id_no").length() != 10) && (wp.itemStr("ex_id_no").length() != 11)) {
      alertErr2("統編輸入8碼, 身分證號10碼,帳戶查詢碼11碼");
      return (1);
    }

    String sql1 = "";
    if (wp.itemStr("ex_id_no").length() == 10) {
      sql1 = "select a.id_p_seqno, " + "       a.chi_name " + "from crd_idno a,act_acno b "
//          + "where  id_no  =  '" + wp.itemStr("ex_id_no").toUpperCase() + "'"
          + "where 1 = 1" + sqlCol(wp.itemStr("ex_id_no").toUpperCase(),"id_no")
          + "and    id_no_code   = '0' " + "and    a.id_p_seqno = b.id_p_seqno ";

      if (wp.itemStr("ex_acct_type").length() != 0)
//        sql1 = sql1 + "and   b.acct_type  =  '" + wp.itemStr("ex_acct_type").toUpperCase() + "' ";
      sql1 = sql1 + sqlCol(wp.itemStr("ex_acct_type").toUpperCase(),"b.acct_type");

      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
    }

    if (wp.itemStr("ex_id_no").length() == 11) {
      sql1 = "select a.p_seqno, " + "       a.id_p_seqno, " + "       a.corp_p_seqno, "
          + "       b.card_indicator " + "from act_acno a,ptr_acct_type b "
//          + "where a.acct_key  = '" + wp.itemStr("ex_id_no").toUpperCase() + "' "
          + "where 1 = 1" + sqlCol(wp.itemStr("ex_id_no").toUpperCase(),"a.acct_key")
          + "and   a.acct_type = b.acct_type ";

      if (wp.itemStr("ex_acct_type").length() != 0)
//        sql1 = sql1 + "and   b.acct_type  =  '" + wp.itemStr("ex_acct_type").toUpperCase() + "' ";
    	  sql1 = sql1 + sqlCol(wp.itemStr("ex_acct_type").toUpperCase(),"b.acct_type");


      sqlSelect(sql1);
      if (sqlRowNum > 1) {
        alertErr2(" 查有多身分資料, 請輸入帳戶類別");
        return (1);
      }
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此帳戶查詢碼[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      sql1 = "select chi_name " + "from   crd_idno " 
//      + "where  id_p_seqno = '" + sqlStr("id_p_seqno") + "' ";
      + "where 1 = 1" + sqlCol(sqlStr("id_p_seqno"),"id_p_seqno");
      sqlSelect(sql1);
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
    }
    
    if(wp.itemEq("ex_select_type", "2")) {
    	if (wp.itemEmpty("ex_tran_date1") || wp.itemEmpty("ex_tran_date2")) {
			alertErr2("查詢日期區間起迄不可空白");
			return (1);
		}
		if (chkStrend(wp.itemStr("ex_tran_date1"), wp.itemStr("ex_tran_date2")) == false) {
			alertErr2("查詢日期區間起迄大小值錯誤");
			return (1);
		}
		if(comm.datePeriod(wp.itemStr("ex_tran_date1"),wp.itemStr("ex_tran_date2")) > 365) {
			alertErr2("查詢日期區間限制1年內");
			return (1);
		}
    }

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("4")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      return " and a.id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
    }


    return "";
  }

  // ************************************************************************
  public void commBonusType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "bonus_TYPE") + "'"
//          + sqlCol(wp.colStr(ii, "bonus_TYPE"),"wf_id")
		  + " and wf_id = ? "
          + " and   wf_type = 'BONUS_NAME' ";
      setString(1,wp.colStr(ii, "bonus_type"));
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 " 
//    		  + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
      		+ sqlCol(wp.colStr(ii, "acct_type"),"acct_type");
      if (wp.colStr(ii, "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_chin_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commModReason(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "mod_reason") + "'"
//          + sqlCol(wp.colStr(ii, "mod_reason"),"wf_id")
		  + " and wf_id = ? "
          + " and   wf_type = 'ADJMOD_REASON' ";
      setString(1,wp.colStr(ii, "mod_reason"));
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from crd_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
      	  + sqlCol(wp.colStr(ii, "id_p_seqno"),"id_p_seqno");
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
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
    String[] cde = {"Y", "N"};
    String[] txt = {"應稅", "免稅"};
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
  
  //************************************************************************
  public void commModMemo(String columnData1,String columnData2) throws UnsupportedEncodingException {
	  String modMemo = "";
	  for (int ii = 0; ii < wp.selectCnt; ii++) {
		  modMemo = wp.colStr(ii,columnData1);
		  if(empty(modMemo)) {
			  modMemo = wp.colStr(ii,columnData2);
			  if(commString.left(modMemo,2).equals("轉置")) {
				  modMemo = "";
			  }
		  }
		  modMemo = subString(modMemo,40);
		  wp.colSet(ii,columnData1,modMemo);
	  }
  }
  
  //************************************************************************
  public String subString(String columnData, int length) {
	  int len = 0;
	  String str = "";
		for(int i=0;i<columnData.length();i++){
			int acsii = columnData.charAt(i); 
			len += (acsii<0 || acsii > 128)?2:1;
			if(len > length) {
				break;
			}
			str += columnData.charAt(i); 
		}
	  return str;
  }

  // ************************************************************************
  public void check_button_off() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class
