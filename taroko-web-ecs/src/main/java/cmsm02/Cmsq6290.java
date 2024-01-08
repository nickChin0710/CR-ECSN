/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *                                                                           * 
* 112-09-05  V1.00.00  Ryan          合併 Mktq6290 ,Dbmq0100     * 
* 112-10-31  V1.00.01  Ryan          信用卡,VisaDebit金融卡-最新點數餘額  增加判別有無流通卡後計算處理   * 
***************************************************************************/
package cmsm02;

import java.io.UnsupportedEncodingException;

import busi.ecs.MktBonus;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cmsq6290 extends BaseEdit {
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  String rowid, kk1,kk2, kk3, kk4;
  private final static String MKT_BONUS_DTL = "MKT_BONUS_DTL";
  private final static String MKT_BONUS_DTL_HST = "MKT_BONUS_DTL_HST";
  private final static String DBM_BONUS_DTL = "DBM_BONUS_DTL";
  private final static String DBM_BONUS_DTL_HST = "DBM_BONUS_DTL_HST";
  int qFrom = 0;
  String tranSeqStr = "";
  String controlTabName1 = "";
  String controlTabName2 = "";
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
    } else if (eqIgno(wp.buttonCode, "S1")) {/* 動態查詢 */
      strAction = "S1";
      querySelect01();
    } else if (eqIgno(wp.buttonCode, "S2")) {/* 動態查詢 */
      strAction = "S2";
      querySelect01();
    } 

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
	  wp.queryWhere = wp.whereStr;
	  wp.setQueryMode();
	  queryRead();
  }
  
  public String getSqlWhere() throws Exception {
	  String sqlWhere = "";
		if(wp.colEmpty("ex_id_p_seqno") == false) {
			sqlWhere += " and a.id_p_seqno = :id_p_seqno ";
			setString("id_p_seqno",wp.colStr("ex_id_p_seqno"));
		}
		if (wp.itemEq("ex_select_type", "2")) {
			if(wp.itemEmpty("ex_tran_date1") == false) {
				sqlWhere += " and a.tran_date >= :ex_tran_date1 ";
				setString("ex_tran_date1",wp.itemStr("ex_tran_date1"));
			}
			if(wp.itemEmpty("ex_tran_date2") == false) {
				sqlWhere += " and a.tran_date <= :ex_tran_date2 ";
				setString("ex_tran_date2",wp.itemStr("ex_tran_date2"));
			}
			sqlWhere += " and a.apr_flag = 'Y'";
		}
		return sqlWhere;
  }
 

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
	 
		if (wp.itemEq("ex_query_table", "1")) {
			controlTabName1 = MKT_BONUS_DTL;
			controlTabName2 = DBM_BONUS_DTL;
		}

		if (wp.itemEq("ex_query_table", "2")) {
			controlTabName1 = MKT_BONUS_DTL_HST;
			controlTabName2 = DBM_BONUS_DTL_HST;
		}
		
		if (queryCheck() != 0)
			return;

		if (wp.itemEq("ex_select_type", "1")) {
			queryReadType1();
		} else {
			queryReadType2();
		}
	    getTotEndTranBp01();
	    getTotEndTranBp90();
	    wp.colSet("sum_end_tran_bp", wp.colNum("end_tran_bp90") + wp.colNum("end_tran_bp01"));
  }
  
  private void queryReadType1() throws Exception {
	    wp.pageControl();

		wp.sqlCmd = " (select " + " 'mkt' table_type, " + "a.id_p_seqno, " + "a.bonus_type, " + "acct_type," + "max('') as id_no,"
				+ "sum(end_tran_bp+res_tran_bp) as TOT_END_TRAN_BP," 
				+ "sum(res_tran_bp) as RES_TRAN_bp,"
				+ "sum(decode(sign(to_char(add_months(sysdate,-3),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail3_BP,"
				+ "sum(decode(sign(to_char(add_months(sysdate,-6),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail6_BP,"
				+ "max(tran_date) as tran_date," + "count(*) as data_cnt ";
		wp.sqlCmd += " from " + controlTabName1 + " a ";
		wp.sqlCmd += " WHERE 1=1 and a.acct_type = '01' " ;
		wp.sqlCmd += getSqlWhere();
		wp.sqlCmd += " group by a.acct_type,a.id_p_seqno,a.bonus_type " 
				  + " order by a.id_p_seqno,tran_date) ";
		wp.sqlCmd += " union all ";
		wp.sqlCmd += " (select " + " 'dbm' table_type, " + "a.id_p_seqno, " + "a.bonus_type, " + "a.acct_type, " + "max('') as id_no," 
				+ "sum(END_TRAN_BP) as TOT_END_TRAN_BP,"
				+ " 0 as RES_TRAN_bp, "
				+ "sum(decode(sign(to_char(add_months(sysdate,-3),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail3_BP,"
				+ "sum(decode(sign(to_char(add_months(sysdate,-6),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail6_BP,"
				+ "max(tran_date) as tran_date," + "count(*) as data_cnt ";
		wp.sqlCmd += " from " + controlTabName2 + " a ";
		wp.sqlCmd += " WHERE 1=1 and a.acct_type = '90' " ;
		wp.sqlCmd += getSqlWhere();
		wp.sqlCmd += " group by a.acct_type,a.id_p_seqno,a.bonus_type "
				+ " order by a.id_p_seqno,tran_date) ";

	    wp.pageCountSql = "select count(*) from ( " + wp.sqlCmd + " )";

	    pageQuery();
	    wp.setListCount(1);
	    if (sqlNotFind()) {
	      alertErr(appMsg.errCondNodata);
	      return;
	    }

	    commAcctType("comm_acct_type");
	    commIdNo("comm_id_no");
	    commBonusType("comm_bonus_type");

	    wp.setPageValue();
  }
  
  
  public void queryReadType2() throws Exception {
		wp.pageControl();
		wp.sqlCmd = " (select hex(a.rowid) as rowid, " + " 'mkt' table_type " + " ,end_tran_bp "
				+ " ,a.acct_type, a.tran_date, a.tran_code, a.beg_tran_bp ,trim(mod_memo) as mod_memo , trim(mod_desc) as mod_desc, a.tran_time ";
		wp.sqlCmd += " from " + controlTabName1 + " a join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
		wp.sqlCmd += " WHERE 1=1 and a.acct_type = '01' " ;
		wp.sqlCmd += getSqlWhere();
		wp.sqlCmd += " order by a.tran_date desc , a.tran_time desc) ";
		wp.sqlCmd += " union all ";
		wp.sqlCmd += " (select hex(a.rowid) as rowid, " + " 'dbm' table_type " + " ,end_tran_bp "
				+ " ,a.acct_type, a.tran_date, a.tran_code, a.beg_tran_bp ,trim(mod_memo) as mod_memo , trim(mod_desc) as mod_desc, a.tran_time ";
		wp.sqlCmd += " from " + controlTabName2 + " a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
		wp.sqlCmd += " WHERE 1=1 and a.acct_type = '90' " ;
		wp.sqlCmd += getSqlWhere();
		wp.sqlCmd += " order by a.tran_date desc , a.tran_time desc) ";
		
	    wp.pageCountSql = "select count(*) from ( " + wp.sqlCmd + " )";
	    
		pageQuery();
		wp.setListCount(2);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		
		commAcctType("comm_acct_type");
		commTranCode("comm_tran_code");
		commModMemo("mod_memo", "mod_desc");

		wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

  }

	public void querySelect01() throws Exception {
		kk1 = itemKk("data_k1");
		if ("S1".equals(strAction)) {
			kk2 = itemKk("data_k2");
			kk3 = itemKk("data_k3");
			kk4 = itemKk("data_k4");
			qFrom = 1;
		} else {
			rowid = itemKk("data_k2");
			qFrom = 2;
		}
		if ("mkt".equals(kk1)) {
			if (wp.itemEq("ex_query_table", "1")) {
				controlTabName1 = MKT_BONUS_DTL;
				wp.colSet("ex_query_table", "1");
			}
			if (wp.itemEq("ex_query_table", "2")) {
				controlTabName1 = MKT_BONUS_DTL_HST;
				wp.colSet("ex_query_table", "2");
			}
			dataRead01();
		}
		if ("dbm".equals(kk1)) {
			if (wp.itemEq("ex_query_table", "1")) {
				wp.colSet("ex_query_table", "1");
				controlTabName2 = DBM_BONUS_DTL;
			}

			if (wp.itemEq("ex_query_table", "2")) {
				wp.colSet("ex_query_table", "2");
				controlTabName2 = DBM_BONUS_DTL_HST;
			}
			dataRead90();
		}
	}

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
  }

  public void dataRead01() throws Exception {

	    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + " 'mkt' table_type, "
	        + " ROW_NUMBER()OVER() as ser_num, " + "a.bonus_type," + "a.active_code,"
	        + "b.acct_key as acct_key," + "c.chi_name as chi_name," + "a.active_name," + "a.acct_type,"
	        + "a.tran_date," + "a.tran_time," + "a.tran_code," + "a.tran_pgm," + "a.beg_tran_bp,"
	        + "a.end_tran_bp," + "a.res_tran_bp," + "a.res_upd_date," + "a.tax_flag,"
	        + "a.effect_e_date," + "a.tran_seqno," + "a.proc_month," + "a.acct_date," + "a.mod_desc,"
	        + "a.mod_memo," + "a.mod_reason," + "a.id_p_seqno," + "a.p_seqno," + "a.apr_date,"
	        + "a.apr_user," + "a.crt_date," + "a.crt_user,"
	        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.mod_pgm";

	    wp.daoTable = controlTabName1 + " a " + "JOIN act_acno b " + "ON a.p_seqno = b.p_seqno "
	        + "JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
	    wp.whereStr = "where 1=1 ";
	    if (qFrom == 1) {
			wp.whereStr += " and a.acct_type = :kk2 ";
			wp.whereStr += " and a.id_p_seqno = :kk3 ";
			wp.whereStr += " and a.bonus_type = :kk4 ";
			setString("kk2",kk2);
			setString("kk3",kk3);
			setString("kk4",kk4);
			wp.whereOrder = " order by tran_date desc,tran_time desc,tran_seqno desc ";
	    } else {
			wp.whereStr += " and hex(a.rowid) = :rowid ";
			setString("rowid",rowid);
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
  
  public void dataRead90() throws Exception {
		wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + " 'dbm' table_type, "
				+ " ROW_NUMBER()OVER() as ser_num, " + "a.tran_seqno," + "a.bonus_type," + "a.active_code,"
				+ "a.active_name," + "c.id_no as id_no," + "c.chi_name as chi_name," + "a.acct_type," + "a.tran_date,"
				+ "a.tran_time," + "a.tran_code," + "a.tran_pgm," + "a.beg_tran_bp," + "a.end_tran_bp," + "a.tax_flag,"
				+ "a.effect_e_date," + "a.mod_reason," + "a.mod_desc," + "a.mod_memo," + "a.id_p_seqno," + "a.crt_user,"
				+ "a.crt_date," + "a.mod_user," + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.apr_user,"
				+ "a.apr_date";

		wp.daoTable = controlTabName2 + " a " + "JOIN dbc_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
		wp.whereStr = "where 1=1 ";
		if (qFrom == 1) {
			wp.whereStr += " and a.acct_type = :kk2 ";
			wp.whereStr += " and a.id_p_seqno = :kk3 ";
			wp.whereStr += " and a.bonus_type = :kk4 ";
			setString("kk2",kk2);
			setString("kk3",kk3);
			setString("kk4",kk4);
			wp.whereOrder = " order by tran_date desc,tran_time desc,tran_seqno desc fetch first 500 rows only";
		} else {
			wp.whereStr += " and hex(a.rowid) = :rowid ";
			setString("rowid",rowid);
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

    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {

	if (wp.itemEmpty("ex_id_no")) {
		alertErr2("身份證號不可空白");
		return (1);
	}

    String sql1 = "";
	sql1 = " (select a.id_p_seqno, " + " a.chi_name " + "from crd_idno a  " + "where 1 = 1 and a.id_no = :id_no "
			+ "and  id_no_code   = '0') ";
	sql1 += " union ";
	sql1 += " (select a.id_p_seqno, " + " a.chi_name " + "from dbc_idno a  " + "where 1 = 1 and a.id_no = :id_no "
			+ "and  id_no_code   = '0') ";
	setString("id_no", wp.itemStr("ex_id_no"));
	sqlSelect(sql1);

	if (sqlRowNum <= 0) {
		alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no") + "] 資料");
		return (1);
	}
	wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
	wp.colSet("ex_chi_name", sqlStr("chi_name"));
    
    
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
  
  void getTotEndTranBp01() {
	
	 String sqlStr = "select "
		        + "sum(end_tran_bp + res_tran_bp) as end_tran_bp01 "
		        + "FROM mkt_bonus_dtl a , crd_idno b "
		        + "where  a.id_p_seqno = b.id_p_seqno "
		        + "AND a.acct_type = '01' "
		        + "and a.id_p_seqno = :ex_id_p_seqno "
		        + "AND a.BONUS_TYPE ='BONU' and a.APR_FLAG = 'Y' "
		    	+ "and decode(effect_e_date,'','99999999',effect_e_date) >= to_char(sysdate,'yyyymmdd') ";
	 setString("ex_id_p_seqno",wp.colStr("ex_id_p_seqno"));
	 sqlSelect(sqlStr);
	 int endTranBp01 = sqlInt("end_tran_bp01");
	 
	 MktBonus comc = new MktBonus();
	 comc.setConn(wp);
	 int crdCnt = comc.checkCurrentCode0(wp.itemStr("ex_id_no"), "01");
	 if(crdCnt == 0) {
		 if(endTranBp01 >=0 )
			 endTranBp01 = 0;
	 }
		 
	 wp.colSet("end_tran_bp01", endTranBp01);
	 wp.colSet("crd_current_code_0", crdCnt > 0 ? "有":"無");
  }

  
  void getTotEndTranBp90() {
		 String sqlStr = "select "
			        + "sum(end_tran_bp) as end_tran_bp90 "
			        + "FROM dbm_bonus_dtl a , dbc_idno b "
			        + "where  a.id_p_seqno = b.id_p_seqno "
			        + "AND a.acct_type = '90' "
			        + "and a.id_p_seqno = :ex_id_p_seqno "
			        + "AND a.BONUS_TYPE ='BONU' and a.APR_FLAG = 'Y' "
			    	+ "and decode(effect_e_date,'','99999999',effect_e_date) >= to_char(sysdate,'yyyymmdd') ";
		 setString("ex_id_p_seqno",wp.colStr("ex_id_p_seqno"));
		 sqlSelect(sqlStr);
		 int endTranBp90 = sqlInt("end_tran_bp90");
		 
		 MktBonus comc = new MktBonus();
		 comc.setConn(wp);
		 int dbcCnt = comc.checkCurrentCode0(wp.itemStr("ex_id_no"), "90");
		 
		 if(dbcCnt == 0) {
			 if(endTranBp90 >= 0)
				 endTranBp90 = 0;
		 }
		 
		 wp.colSet("end_tran_bp90", endTranBp90);
		 wp.colSet("dbc_current_code_0", dbcCnt > 0 ? "有":"無");
  }

  // ************************************************************************
  public void commBonusType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
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
      if (wp.colEmpty(ii, "acct_type"))
          continue;
      columnData = "";
      sql1 = " (select " + " chin_name as column_chin_name " + " from ptr_acct_type";
      sql1 += " where 1 = 1 and acct_type = :acct_type) " ;
      sql1 += " union ";
      sql1 += " (select " + " chin_name as column_chin_name " + " from dbp_acct_type";
      sql1 += " where 1 = 1 and acct_type = :acct_type)" ;
      setString("acct_type",wp.colStr(ii, "acct_type"));
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
      if (wp.colEmpty(ii, "id_p_seqno"))
         continue;
      columnData = "";
      sql1 = " (select " + " id_no as column_id_no " + " from crd_idno "; 
      sql1 += " where 1 = 1 and id_p_seqno = :id_p_seqno) ";
      sql1 += " union ";
      sql1 += " (select " + " id_no as column_id_no " + " from dbc_idno ";
      sql1 += " where 1 = 1 and id_p_seqno = :id_p_seqno) ";
      setString("id_p_seqno",wp.colStr(ii, "id_p_seqno"));
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

@Override
public void saveFunc() throws Exception {
	// TODO Auto-generated method stub
	
}

} // End of class
