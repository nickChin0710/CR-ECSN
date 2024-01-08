/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/16  V1.00.01   Ray Ho        Initial                              *
* 109-04-20  V1.00.02  shiyuqi       updated for project coding standard   * 
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名
* 110-11-03  V1.00.03  machao     SQL Injection                                                                                     *  
* 112-01-13  V1.00.04  Zuwei Su        異動原因有空值異動碼頭, 會帶出錯誤預設值                                                                                     *  
***************************************************************************/
package mktq02;

import mktq02.Mktq6210Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq6210 extends BaseEdit {
  private String PROGNAME = "帳戶基金(現金回饋)明細檔維護作業處理程式108/08/16 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq6210Func func = null;
  String rowid;// dataKK2, dataKK3, dataKK4, dataKK5;
  String orgTabName = "mkt_cashback_dtl";
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
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 " + sqlChkEx(wp.itemStr("ex_id_no"), "4", "");

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

    wp.selectSQL = " " + "a.p_seqno, " + "a.fund_code, " + "max(acct_type) as acct_type,"
        + "max('') as id_no," + "max(tran_date) as tran_date,"
        + "sum(end_tran_amt) as END_TRAN_AMT," + "sum(res_tran_amt) as RES_TRAN_AMT,"
        + "sum(end_tran_amt+res_tran_amt) as total_TRAN_AMT,"
        + "sum(decode(sign(to_char(add_months(sysdate,-3),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_amt,0)) as TOT_fail3_AMT,"
        + "sum(decode(sign(to_char(add_months(sysdate,-6),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_amt,0)) as TOT_fail6_AMT,"
        + "max(tran_date) as last_tran_date," + "count(*) as data_cnt,"
        + "max(id_p_seqno) as id_p_seqno," + "max(p_seqno) as p_seqno";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder =
        " group by a.p_seqno,a.fund_code" + " order by a.p_seqno,tran_date desc,fund_code";

    wp.pageCountSql = "select count(*) from ( " + " select distinct a.p_seqno,a.fund_code"
        + " from " + wp.daoTable + " " + wp.queryWhere + " )";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commIdNob("comm_id_no");
    commFundCode("comm_fund_code");


    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    wp.colSet("p_seqno", itemKk("data_k2"));
    wp.colSet("fund_code", itemKk("data_k3"));
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  public void querySelect1() throws Exception {
    controlTabName = orgTabName;

    rowid = itemKk("data_k1");
    qFrom = 2;
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
        + " ROW_NUMBER()OVER() as ser_num, " + "b.acct_key as acct_key," + "a.fund_code,"
        + "'' as id_no," + "'' as chi_name," + "a.acct_type," + "a.tran_date," + "a.tran_time,"
        + "a.id_p_seqno," + "a.p_seqno," + "a.tran_code," + "a.tran_pgm," + "a.beg_tran_amt,"
        + "a.end_tran_amt," + "a.res_tran_amt," + "a.res_total_cnt," + "a.res_tran_cnt,"
        + "a.res_upd_date," + "a.effect_e_date," + "a.tran_seqno," + "a.proc_month,"
        + "a.acct_date," + "a.mod_desc," + "a.mod_memo," + "a.mod_reason," + "a.crt_user,"
        + "a.crt_date," + "a.apr_user," + "a.apr_date," + "a.mod_pgm,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time";

    wp.daoTable = controlTabName + " a " + "JOIN act_acno b " + "ON a.p_seqno = b.p_seqno ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr =
          wp.whereStr + sqlCol(itemKk("data_k1"), "a.''") + sqlCol(itemKk("data_k2"), "a.''")
              + sqlCol(itemKk("data_k3"), "a.acct_type") + sqlCol(itemKk("data_k4"), "b.acct_key");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlCol(wp.colStr("p_seqno"), "a.p_seqno")
          + sqlCol(wp.colStr("fund_code"), "a.fund_code")
          + " order by tran_date desc,tran_time desc,tran_seqno desc";
    } else {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    wp.setListCount(1);
    commFundCode("comm_fund_code");
    commAcctType("comm_acct_type");
    commTransType("comm_tran_code");
    commModReason("comm_mod_reason");
    wp.colSet("", itemKk("data_kN"));

    if (qFrom != 0) {
      commFundCode("comm_fund_code");
      commAcctType("comm_acct_type");
      commTransType("comm_tran_code");
      commModReason("comm_mod_reason");
    }
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    int ii = 0;
    String sql1 = "";

    if (wp.colStr(ii, "id_p_seqno").length() != 0) {
      sql1 = "select " + " id_no as id_no, " + " chi_name as chi_name " + " from crd_idno "
//        + " where id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
//     	  + " where 1 = 1" + sqlCol(wp.itemStr("id_p_seqno").toUpperCase(),"id_p_seqno");
          + " where 1 = 1 "
//add
          + " and id_p_seqno = ?  " ;
//update
          sqlSelect(sql1,new Object[] {wp.colStr(ii,"id_p_seqno")});
          
    } else {
      sql1 = "select " + " corp_no  as id_no, " + " chi_name as chi_name "
          + " from crd_corp a,act_acno b " + " where  a.corp_p_seqno = b.corp_p_seqno "
//        + " and    b.p_seqno = '" + wp.colStr(ii, "p_seqno") + "' ";
//        + sqlCol(wp.itemStr("p_seqno").toUpperCase(),"b.p_seqno");
//add
          + " and b.p_seqno = ?  " ;
//        + sqlCol(wp.itemStr("p_seqno").toUpperCase(),"b.p_seqno");
//update
          sqlSelect(sql1,new Object[] {wp.colStr(ii,"p_seqno")});	

    }
//  sqlSelect(sql1);
    wp.colSet(ii, "id_no", sqlStr("id_no"));
    wp.colSet(ii, "chi_name", sqlStr("chi_name"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq6210Func func = new mktq02.Mktq6210Func(wp);

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
      if ((wp.respHtml.equals("mktq6210"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_acct_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_acct_type");
        }
        this.dddwList("dddw_acct_type1", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if (itemKk("ex_query_table").equals("2"))
      orgTabName = "mkt_cashback_dtl_hst";
    else
      orgTabName = "mkt_cashback_dtl";

    controlTabName = orgTabName.toUpperCase();
    wp.colSet("control_tab_name", controlTabName);

    if ((wp.itemStr("ex_id_no").length() != 8) && (wp.itemStr("ex_id_no").length() != 10)
        && (wp.itemStr("ex_id_no").length() != 11)) {
      alertErr2("統編輸入8碼, 身分證號10碼,帳戶查詢碼11碼");
      return (1);
    }

    String sql1 = "";
    if (wp.itemStr("ex_id_no").length() == 8) {
      sql1 = "select p_seqno, " + "       chi_name " + "from   crd_corp a,act_acno b "
          + "where  a.corp_p_seqno = b.corp_p_seqno " + "and    b.id_p_seqno   = '' "
//          + "and    corp_no        =  '" + wp.itemStr("ex_id_no").toUpperCase() + "' ";
      	  + sqlCol(wp.itemStr("ex_id_no").toUpperCase(),"corp_no");


      if (wp.itemStr("ex_acct_type").length() != 0)
        sql1 = sql1 + "and   b.acct_type  =  '" + wp.itemStr("ex_acct_type").toUpperCase() + "' ";

      sqlSelect(sql1);
      if (sqlRowNum > 1) {
        alertErr2(" 查有多身分資料, 請輸入帳戶類別");
        return (1);
      }

      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此統編[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      wp.colSet("ex_id_p_seqno", "");
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      return (0);
    }

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
      wp.colSet("ex_p_seqno", "");
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      return (0);
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
      if (sqlStr("card_indicator").equals("2")) {
        sql1 = "select chi_name " + "from   crd_corp " 
//      + "where  corp_p_seqno = '"+ sqlStr("corp_p_seqno") + "' ";
        + "where 1 = 1" + sqlCol(sqlStr("corp_p_seqno"),"corp_p_seqno");
        sqlSelect(sql1);
        wp.colSet("ex_chi_name", sqlStr("chi_name"));
        wp.colSet("ex_id_p_seqno", "");
        wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      } else {
        sql1 = "select chi_name " + "from   crd_idno " 
//      + "where  id_p_seqno = '" + sqlStr("id_p_seqno") + "' ";
        + "where 1 = 1" + sqlCol(sqlStr("id_p_seqno"),"id_p_seqno");
        sqlSelect(sql1);
        wp.colSet("ex_chi_name", sqlStr("chi_name"));
        wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
        wp.colSet("ex_p_seqno", "");
      }
      return (0);
    }


    /*
     * if (wp.item_ss("ex_id_no").length()>0) { String sql1 = "select id_p_seqno,chi_name " +
     * "from crd_idno " + "where  id_no  =  '"+ wp.item_ss("ex_id_no").toUpperCase() +"'" ;
     * 
     * sqlSelect(sql1); if (sql_nrow <= 0) {
     * err_alert(" 查無此身分證號[ "+wp.item_ss("ex_id_no").toUpperCase() +"] 資料"); return(1); }
     * 
     * wp.col_set("ex_id_p_seqno",sql_ss("id_p_seqno"));
     * wp.col_set("ex_chi_name",sql_ss("chi_name")); return(0); }
     * 
     * if (wp.item_ss("ex_acct_key").length()>0) { String sql1 = "select p_seqno,b.chi_name  " +
     * "from act_acno a,crd_idno b " + "where  acct_key  =  '"+
     * wp.item_ss("ex_acct_key").toUpperCase() +"' " + "and    a.id_p_seqno  =   b.id_p_seqno " +
     * "and    acct_type  =  '"+ wp.item_ss("ex_acct_type").toUpperCase() +"' " ;
     * 
     * sqlSelect(sql1); if (sql_nrow <= 0) {
     * err_alert(" 查無此帳戶查詢碼[ "+wp.item_ss("ex_acct_key").toUpperCase() +"] 資料"); return(1); }
     * 
     * wp.col_set("ex_p_seqno",sql_ss("p_seqno")); wp.col_set("ex_chi_name",sql_ss("chi_name")); }
     * 
     */
    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("1")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      return " and a.id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
    }

    if (sqCond.equals("3")) {
      if (empty(wp.itemStr("ex_acct_key")))
        return "";
      return " and a.p_seqno ='" + wp.colStr("ex_p_seqno") + "' ";
    }

    if (sqCond.equals("4")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      if (wp.colStr("ex_id_p_seqno").length() != 0)
        return " and id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
      else
        return " and p_seqno ='" + wp.colStr("ex_p_seqno") + "' ";
    }

    return "";
  }

  // ************************************************************************
  public void commFundCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    //System.out.println("wp.selectCnt="+ wp.selectCnt);
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " fund_name as column_fund_name " + " from vmkt_fund_name "
          + " where 1 = 1 " 
//    		  + " and   fund_code = '" + wp.colStr(ii, "fund_code") + "'";
//        + sqlCol(wp.colStr(ii, "fund_code"),"fund_code");
          + " and fund_code = ?  " ;
      
      
      if (wp.colStr(ii, "fund_code").length() == 0)
        continue;
//    sqlSelect(sql1);
      sqlSelect(sql1,new Object[] {wp.colStr(ii,"fund_code")});
      

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_fund_name");
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
//        + sqlCol(wp.colStr(ii, "acct_type"),"acct_type");
          + " and acct_type = ?  " ;
      
      if (wp.colStr(ii, "acct_type").length() == 0)
        continue;
//    sqlSelect(sql1);
      sqlSelect(sql1,new Object[] {wp.colStr(ii,"acct_type")});
      

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
      if (wp.colEmpty(ii, "mod_reason")) {
          wp.colSet(ii, columnData1, columnData);
          continue;
      }
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "mod_reason") + "'"
          + sqlCol(wp.colStr(ii, "mod_reason"),"wf_id")
          + " and   wf_type = 'ADJMOD_REASON' ";
      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commIdNob(String columnData1) throws Exception {
	    String columnData = "";
	    String sql1 = "";
	    for (int ii = 0; ii < wp.selectCnt; ii++) {
	      columnData = "";
	      if (wp.colStr(ii, "id_p_seqno").length() != 0) {
	        sql1 = "select " + " id_no as column_id_no " + " from crd_idno " 
//	      + " where id_p_seqno = '"+ wp.colStr(ii, "id_p_seqno") + "'";
//	      + " where 1 = 1" + sqlCol(wp.itemStr("id_p_seqno").toUpperCase(),"id_p_seqno");
	           + " where 1 = 1 "
	//add
	           + " and id_p_seqno = ?  " ;
	//update
	        sqlSelect(sql1,new Object[] {wp.colStr(ii,"id_p_seqno")});
	      } else {
	        sql1 = "select " + " corp_no as column_id_no " + " from crd_corp a,act_acno b "
	            + " where  a.corp_p_seqno = b.corp_p_seqno " 
//	        		+ " and    b.p_seqno = '" + wp.colStr(ii, "p_seqno") + "' ";
	//add
	                + " and b.p_seqno = ?  " ;
//	        + sqlCol(wp.itemStr("p_seqno").toUpperCase(),"b.p_seqno");
	//add
	        sqlSelect(sql1,new Object[] {wp.colStr(ii,"p_seqno")});		
	      }
//	      sqlSelect(sql1);

	      if (sqlRowNum > 0) {
	        columnData = columnData + sqlStr("column_id_no");
	        wp.colSet(ii, columnData1, columnData);
	      }
	    }
	    return;
	  }

  // ************************************************************************
  public void commTransType(String cde1) throws Exception {
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

} // End of class
