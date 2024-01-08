/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/09/08  V1.00.01    Ryan         合併mktq6210,mktq6270                 *                                                   *  
***************************************************************************/
package mktq02;

import java.math.BigDecimal;
import java.util.HashMap;

import com.ibm.db2.jcc.am.w;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq6310 extends BaseEdit {
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq6270Func func = null;
  private final static String CYC_DC_FUND_DTL = "CYC_DC_FUND_DTL";
  private final static String MKT_CASHBACK_DTL = "MKT_CASHBACK_DTL";
  private final static String CYC_DC_FUND_DTL_HST = "CYC_DC_FUND_DTL_HST";
  private final static String MKT_CASHBACK_DTL_HST = "MKT_CASHBACK_DTL_HST";
  String kk1,kk2,kk3,kk4,kk5;
  String controlTabName1 = "";
  String controlTabName2 = "";

  int qFrom = 0;
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
      dataRead();
    } else if (eqIgno(wp.buttonCode, "S2")) {/* 動態查詢 */
      strAction = "S2";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "Q2")) {/* 動態查詢 */
      strAction = "Q2";
      querySelect();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }
  
  private String getWhere(String type) {
		String lsWhere = " where 1=1 ";
		if (wp.colEmpty("ex_id_no") == false) {
			lsWhere += " and a.id_p_seqno = :ex_id_p_seqno ";
			setString("ex_id_p_seqno", wp.colStr("ex_id_p_seqno"));
		}
		if (wp.itemEmpty("ex_tran_date") == false) {
			lsWhere += " and left(a.tran_date,6) = :ex_tran_date ";
			setString("ex_tran_date", wp.colStr("ex_tran_date"));
		}

		if("1".equals(type))
			return lsWhere;
		
		if (wp.itemEmpty("ex_curr_code") == false) {
			lsWhere += " and a.curr_code = :ex_curr_code ";
			setString("ex_curr_code", wp.itemStr("ex_curr_code"));
		}
		
		return lsWhere;
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
	  wp.pageControl();
	  if (queryCheck() != 0)
	      return;
	  wp.sqlCmd = "";
	  if(wp.itemEmpty("ex_curr_code") == false) {
		  if(wp.itemEq("ex_curr_code","901")) {
			  wp.sqlCmd += sqlCmdMkt();
		  }
		  if(!wp.itemEq("ex_curr_code","901")) {
			  wp.sqlCmd += sqlCmdCyc();
		  }
	  }else {
		  wp.sqlCmd += sqlCmdMkt();
		  wp.sqlCmd += " union all ";
		  wp.sqlCmd += sqlCmdCyc();
	  }
	  
	  wp.sqlCmd  += " order by tran_date,curr_code ";
	  wp.sqlCmd  += wp.itemEq("ex_select_type", "2")?",acct_type":"";
	  
	  wp.pageCountSql = " select count(*) cnt from ( " + wp.sqlCmd + " )";
	  pageQuery();
	    
	  wp.setListCount((int)wp.itemNum("ex_select_type"));
	  if (sqlNotFind()) {
	    alertErr(appMsg.errCondNodata);
	    return;
	  }
	  wp.setPageValue();
	    
	 commAcctType("comm_acct_type");
	 commCurrCode("comm_curr_code");
//	 getEntTranAmt();//本日入帳金額
	 getCntTranAmt();//本日計算可用餘額
	 getDataCnt();//彙總筆數
	 if(wp.itemEq("ex_select_type", "1")) {
		 sumAmt("curr_code","ent_tran_amt");
//		 sumAmt("curr_code","end_tran_amt");
		 sumAmt("curr_code","beg_tran_amt");
		 sumAmt("curr_code","cnt_tran_amt");
		 getEndTranAmt1();
	 }
	 if(wp.itemEq("ex_select_type", "2")) {
		 sumAmt("curr_code","acct_type","ent_tran_amt");
//		 sumAmt("curr_code","acct_type","end_tran_amt");
		 sumAmt("curr_code","acct_type","beg_tran_amt");
		 sumAmt("curr_code","acct_type","cnt_tran_amt");
		 getEndTranAmt2();
	 }
  }
  
  private String sqlCmdMkt() throws Exception {
	    String sqlCmd = "";
	    sqlCmd = " " 
	    	+ " (select a.tran_date,'901' as curr_code, 0 as ent_tran_amt, " ;
		sqlCmd += wp.itemEq("ex_select_type", "2")?" a.acct_type, ":"";
		sqlCmd += " 'MKT' table_type, "
	        + " sum(end_tran_amt) as end_tran_amt, "
	        + " sum(decode(tran_pgm,'ActE030',0,beg_tran_amt)) as beg_tran_amt, ";
		sqlCmd += " sum(decode(tran_pgm  in ('ActE010' , 'CycE010' ,'ActE030' ),true ,beg_tran_amt,0)) as ent_tran_amt ";
//	        + " count(*) as data_cnt ";
	    sqlCmd += " from " + controlTabName1 + " a ";
	    sqlCmd += getWhere("1");
	    sqlCmd += " group by a.tran_date ";
	    sqlCmd += wp.itemEq("ex_select_type", "2")?",a.acct_type)":")";
	    return sqlCmd;
  }
  
  private String sqlCmdCyc() throws Exception {
	    String sqlCmd = "";
	    sqlCmd = " " 
	    	+ " (select a.tran_date,a.curr_code, 0 as ent_tran_amt, ";
	    sqlCmd += wp.itemEq("ex_select_type", "2")?" a.acct_type, ":"";
	    sqlCmd += " 'CYC' table_type, "
	        + " sum(end_tran_amt) as end_tran_amt, "
	        + " sum(decode(tran_pgm,'ActE030',0,beg_tran_amt)) as beg_tran_amt, ";
//	        + " count(*) as data_cnt ";
	    sqlCmd += " sum(decode(tran_pgm  in ('CycC210' , 'CycE010' ,'ActE030' ),true,beg_tran_amt,0)) as ent_tran_amt ";
	    sqlCmd += " from " + controlTabName2 + " a ";
	    sqlCmd += getWhere("2");
	    sqlCmd += " group by a.tran_date,a.curr_code ";
	    sqlCmd += wp.itemEq("ex_select_type", "2")?",a.acct_type)":")";
	    return sqlCmd;
}

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
		getTable();
		kk1 = itemKk("data_k1");
		kk2 = itemKk("data_k2");
		kk3 = itemKk("data_k3");
		kk4 = itemKk("data_k4");
		kk5 = itemKk("data_k5");
		
		if(empty(kk1)) 
			kk1 = wp.itemStr("table_type");
		if(empty(kk2)) 
			kk2 = wp.itemStr("list_tran_date");
		if(empty(kk3)) 
			kk3 = wp.itemStr("ex_curr_code");
		if(empty(kk4)) 
			kk4 = wp.itemStr("ex_acct_type");
		if(empty(kk5)) 
			kk5 = wp.itemStr("ex_id_no2");
		
		
		wp.selectCnt = 1;
		wp.colSet("table_type", kk1);
		wp.colSet("list_tran_date",kk2);
		wp.colSet("ex_curr_code", kk3);
		wp.colSet("ex_acct_type", kk4);
		
		commCurrCode("list_curr_code",kk3);
		commAcctType("list_acct_type",kk4);
		
		if ("MKT".equals(kk1))
			queryReadMktq6210();
		if ("CYC".equals(kk1))
			queryReadMktq6270();
  }


  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
		getTable();
		kk1 = itemKk("data_k1");
		kk2 = itemKk("data_k2");
		kk3 = itemKk("data_k3");
		kk4 = itemKk("data_k4");
		kk5 = itemKk("data_k5");
		if("S1".equals(strAction))
			qFrom = 1;
		if("S2".equals(strAction))
			qFrom = 2;
		if ("MKT".equals(kk1))
			dataReadMktq6210();
		if ("CYC".equals(kk1))
			dataReadMktq6270();
  }
  
  private void getTable() {
		String exQueryTable = wp.itemStr("ex_query_table");
		if ("2".equals(exQueryTable)) {
			controlTabName1 = MKT_CASHBACK_DTL_HST;
			controlTabName2 = CYC_DC_FUND_DTL_HST;
		} else {
			controlTabName1 = MKT_CASHBACK_DTL;
			controlTabName2 = CYC_DC_FUND_DTL;
		}
		wp.colSet("ex_query_table", exQueryTable);
		wp.colSet("ex_select_type_tmp", wp.itemEq("ex_select_type","1")?"日期/幣別 彙總":"日期/幣別/帳戶類別 彙總");
		wp.colSet("ex_select_type", wp.itemStr("ex_select_type"));
		wp.colSet("ex_query_table", exQueryTable);
		if(wp.itemEq("ex_select_type","1")) {
			wp.colSet("display" , "style=display:none");
		}
  }
  
  private void queryReadMktq6210() throws Exception {
		wp.selectSQL = "ROW_NUMBER()OVER() as ser_num, a.id_p_seqno, " +" '901' as curr_code," + "a.fund_code, " + "a.acct_type," 
			  	+ "UF_IDNO_id(a.id_p_seqno) as id_no," 
//								+ "(select id_no from crd_idno where id_p_seqno = a.id_p_seqno) as id_no,"  
			  	+ " 'MKT' table_type, "
				+ "max(tran_date) as tran_date," 
				+ "sum(beg_tran_amt) as BEG_TRAN_AMT,"
				+ "sum(end_tran_amt+res_tran_amt) as total_TRAN_AMT,"
				+ "sum(decode(sign(to_char(add_months(sysdate,-3),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_amt,0)) as TOT_fail3_AMT,"
				+ "sum(decode(sign(to_char(add_months(sysdate,-6),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_amt,0)) as TOT_fail6_AMT,"
				+ "max(tran_date) as last_tran_date," + "count(*) as data_cnt, "
			    + "(select count(*) crd_cnt from crd_card where sup_flag='0' and current_code = '0' and id_p_seqno = a.id_p_seqno) as crd_cnt ";

		wp.daoTable = controlTabName1 + " a ";
		wp.whereStr = " where a.tran_date = :kk2 ";
		wp.whereOrder = " group by a.id_p_seqno,a.fund_code,a.acct_type " 
					+ " order by a.fund_code,a.id_p_seqno,a.acct_type  fetch first 1000 rows only ";

		setString("kk2",kk2);
		if(wp.itemEq("ex_select_type", "2")) {
			wp.whereStr += " and a.acct_type = :kk4 ";
			setString("kk4",kk4);
		}
		if(empty(kk5) == false) {
			wp.whereStr += " and UF_IDNO_id(a.id_p_seqno) like :kk5 ";
//			wp.whereStr += " and (select id_no from crd_idno where id_p_seqno = a.id_p_seqno) like :kk5 ";
			setString("kk5",kk5 + "%");
		}
		pageSelect();
		wp.setListCount(1);
		if (sqlNotFind()) {
			if(wp.itemEq("ex_select_type", "2"))
				alertErr(appMsg.errCondNodata + " ,tran_date = ["+ kk2 + "] ,acct_type = [" + kk4 + "]");
			else 
				alertErr(appMsg.errCondNodata + " ,tran_date = ["+ kk2 + "]");
			return;
		}
		if(wp.selectCnt > 999) {
			alertMsg("標示本頁面最多顯示1000筆 , 超出1000筆請限縮ID篩選條件");
		}
		commAcctType("comm_acct_type");
		commFundCode("comm_fund_code");
		commCurrCode("comm_curr_code");
		selectCrdCard();
  }
  
  private void queryReadMktq6270() throws Exception {
	    wp.selectSQL = "ROW_NUMBER()OVER() as ser_num, " + "a.id_p_seqno, " + "a.curr_code, "  + "a.fund_code, " + "a.acct_type,"
			  	+ "UF_IDNO_id(a.id_p_seqno) as id_no," 
//				+ "(select id_no from crd_idno where id_p_seqno = a.id_p_seqno) as id_no," 
	    		+ " 'CYC' table_type, "
		        + "max(tran_date) as tran_date,"
		        + "sum(end_tran_amt) as total_tran_amt,"
		        + "sum(beg_tran_amt) as beg_tran_amt,"
		        + "sum(decode(sign(to_char(add_months(sysdate,-3),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_amt,0)) as TOT_fail3_AMT,"
		        + "sum(decode(sign(to_char(add_months(sysdate,-6),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_amt,0)) as TOT_fail6_AMT,"
		        + "max(tran_date) as last_tran_date," + "count(*) as data_cnt,"
		        + "max(id_p_seqno) as id_p_seqno," + "max(p_seqno) as p_seqno, "
		        + "(select count(*) crd_cnt from crd_card where sup_flag='0' and current_code = '0' and id_p_seqno = a.id_p_seqno) as crd_cnt ";

		wp.daoTable = controlTabName2 + " a ";
		wp.whereStr = " where a.tran_date = :kk2 and a.curr_code = :kk3 ";
		wp.whereOrder = " group by a.id_p_seqno,a.curr_code,a.fund_code,a.acct_type " 
				+ " order by a.fund_code,a.id_p_seqno,a.acct_type fetch first 1000 rows only ";

		setString("kk2",kk2);
		setString("kk3",kk3);
		if(wp.itemEq("ex_select_type", "2")) {
			wp.whereStr += " and a.acct_type = :kk4 ";
			setString("kk4",kk4);
		}
		if(empty(kk5) == false) {
			wp.whereStr += " and UF_IDNO_id(a.id_p_seqno) like :kk5 ";
//			wp.whereStr += " and (select id_no from crd_idno where id_p_seqno = a.id_p_seqno) like :kk5 ";
			setString("kk5",kk5 + "%");
		}
		pageSelect();
		wp.setListCount(1);
		if (sqlNotFind()) {
			if(wp.itemEq("ex_select_type", "2"))
				alertErr(appMsg.errCondNodata + " ,tran_date = ["+ kk2 + "] ,acct_type = [" + kk4 + "]");
			else 
				alertErr(appMsg.errCondNodata + " ,tran_date = ["+ kk2 + "]");
			return;
		}
		if(wp.selectCnt > 999) {
			alertMsg("標示本頁面最多顯示1000筆 , 超出1000筆請限縮ID篩選條件");
		}
		commAcctType("comm_acct_type");
		commFundCode("comm_fund_code");
		commCurrCode("comm_curr_code");
		selectCrdCard();
  }
  
  private void dataReadMktq6210() throws Exception {
	       wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
	            + " ROW_NUMBER()OVER() as ser_num, " + "b.acct_key as acct_key," + "a.fund_code,"
	            + "'' as id_no," + "'' as chi_name," + "a.acct_type," + "a.tran_date," + "a.tran_time,"
	            + "a.id_p_seqno," + "a.p_seqno," + "a.tran_code," + "a.tran_pgm," + "a.beg_tran_amt,"
	            + "a.end_tran_amt," + "a.res_tran_amt," + "a.res_total_cnt," + "a.res_tran_cnt,"
	            + "a.res_upd_date," + "a.effect_e_date," + "a.tran_seqno," + "a.proc_month,"
	            + "a.acct_date," + "a.mod_desc," + "a.mod_memo," + "a.mod_reason," + "a.crt_user,"
	            + "a.crt_date," + "a.apr_user," + "a.apr_date," + "a.mod_pgm,"
	            + "to_char(a.mod_time,'yyyymmdd') as mod_time, "
	            + "c.id_no,c.chi_name, "
	            + " (a.end_tran_amt + a.res_tran_amt) as total_tran_amt, "
	            + " 'MKT' table_type ";

	        wp.daoTable = controlTabName1 + " a " + " JOIN act_acno b " + "ON a.p_seqno = b.p_seqno "
	        		+ " LEFT JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
	        wp.whereStr = "where 1=1 ";
	        if (qFrom == 1) {
	          wp.whereStr += " and a.id_p_seqno = :kk2 ";
	          wp.whereStr += " and a.tran_date = :kk3 ";
	          wp.whereStr += " and a.fund_code = :kk4 ";
	          setString("kk2",kk2);
	          setString("kk3",kk3);
	          setString("kk4",kk4);
	          wp.whereOrder = " order by tran_date desc,tran_time desc,tran_seqno desc ";
	        } else {
	          wp.whereStr += " and hex(a.rowid) = :kk2 ";
	          wp.whereStr += " and a.tran_date = :kk3 ";
	          setString("kk2",kk2);
	          setString("kk3",kk3);
	        }

	        pageSelect();
			if (sqlNotFind()) {
				if(qFrom == 1)
					alertErr(appMsg.errCondNodata  + " ,id_p_seqno = [" + kk2 + "] ,tran_date = [" + kk3 + "] ,fund_code = [" + kk4 + "]");
				return;
			}	        
	        wp.setListCount(1);
	        commFundCode("comm_fund_code");
	        commAcctType("comm_acct_type");
	        commTransType("comm_tran_code");
	        commModReason("comm_mod_reason");
  }
  
  private void dataReadMktq6270() throws Exception {
	    	wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
	            + " ROW_NUMBER()OVER() as ser_num, " + "a.fund_code," + "a.fund_name,"
	            + "b.acct_key as acct_key," + "c.chi_name as chi_name," + "a.curr_code," + "a.acct_type,"
	            + "a.tran_date," + "a.tran_time," + "a.id_p_seqno," + "a.p_seqno," + "a.tran_code,"
	            + "a.tran_pgm," + "a.beg_tran_amt," + "a.end_tran_amt," + "a.effect_e_date,"
	            + "a.tran_seqno," + "a.proc_month," + "a.acct_date," + "a.mod_reason," + "a.mod_desc,"
	            + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date," + "a.mod_pgm,"
	            + "to_char(a.mod_time,'yyyymmdd') as mod_time, "
	            + "c.id_no, "
	            + " 'CYC' table_type ";

	        wp.daoTable = controlTabName2 + " a " + "JOIN act_acno b " + "ON a.p_seqno = b.p_seqno "
	            + " LEFT JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
	        wp.whereStr = "where 1=1 ";
	  
	        if (qFrom == 1) {
	        	wp.whereStr += " and a.id_p_seqno = :kk2 ";
	        	wp.whereStr += " and a.tran_date = :kk3 ";
	        	wp.whereStr += " and a.curr_code = :kk5 ";
	        	setString("kk2",kk2);
	        	setString("kk3",kk3);
	        	setString("kk5",kk5);
	        	wp.whereOrder = " order by tran_date desc,tran_time desc,tran_code";
	        } else {
	        	wp.whereStr += " and hex(a.rowid) = :kk2 ";
	            wp.whereStr += " and a.tran_date = :kk3 ";
	        	setString("kk2",kk2);
	          	setString("kk3",kk3);
	        }

	        pageSelect();
			if (sqlNotFind()) {
				if(qFrom == 1)
					alertErr(appMsg.errCondNodata  + " ,id_p_seqno = [" + kk2 + "] ,tran_date = [" + kk3 + "] ,curr_code = [" + kk5 + "]");
				return;
			}
	        wp.setListCount(1);
	        commFundCode("comm_fund_code");
	        commAcctType("comm_acct_type");
	        commTransType("comm_tran_code");
	        commModReason("comm_mod_reason");
	        commCurrCode("comm_curr_code");
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
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_curr_code");
        this.dddwList("dddw_curr_code", "ptr_currcode", "trim(curr_code)", "trim(curr_chi_name)",
            " where bill_sort_seq!='' ");
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  private int queryCheck() throws Exception {

		getTable();
		wp.colSet("ex_id_p_seqno", "");
		wp.colSet("ex_chi_name", "");
		String sql1 = "";
		if (wp.itemStr("ex_id_no").length() == 10) {
			sql1 = " select a.id_p_seqno, " + " a.chi_name " + " from crd_idno a "
					+ " where 1 = 1 and id_no = :ex_id_no ";
			setString("ex_id_no", wp.itemStr("ex_id_no"));
			sqlSelect(sql1);
			if (sqlRowNum <= 0) {
				alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
				return (1);
			}
			wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
			wp.colSet("ex_chi_name", sqlStr("chi_name"));
			return (0);
		}
		return (0);
  }

  // ************************************************************************
  private void commAcctType(String columnData1) throws Exception {
	  commAcctType(columnData1,"");
  }
  private void commAcctType(String columnData1 ,String value) throws Exception {
	    HashMap<String,String> acctTypeMap = new HashMap<String,String>();
		String columnData = "";
		String sql1 = "select acct_type, chin_name as column_chin_name from ptr_acct_type ";
		sqlSelect(sql1);
		for (int ii = 0; ii < sqlRowNum; ii++) {
			acctTypeMap.put(sqlStr(ii,"acct_type"), sqlStr(ii,"column_chin_name"));
		}
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			columnData = "";
			String acctTypeValue = "";
			if("comm_acct_type".equals(columnData1)) {
				if (wp.colStr(ii, "acct_type").length() == 0)
					continue;
				acctTypeValue = acctTypeMap.get(wp.colStr(ii, "acct_type"))!=null
						?acctTypeMap.get(wp.colStr(ii, "acct_type")).toString():"";
			}
			else
				acctTypeValue = acctTypeMap.get(value)!=null
				?acctTypeMap.get(value).toString():"";

			if (sqlRowNum > 0) {
				columnData = columnData + acctTypeValue;
				if("comm_acct_type".equals(columnData1))
					wp.colSet(ii, columnData1, columnData);
				else
					wp.colSet(columnData1, value + columnData);
			}
		}
		return;
	}

	private void commFundCode(String columnData1) throws Exception {
		HashMap<String,String> fundCodeMap = new HashMap<String,String>();
		String columnData = "";
		String 	sql1 = "select fund_code, fund_name as column_fund_name from vmkt_fund_name ";
		sqlSelect(sql1);
		for (int ii = 0; ii < sqlRowNum; ii++) {
			fundCodeMap.put(sqlStr(ii,"fund_code"), sqlStr(ii,"column_fund_name"));
		}
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			columnData = "";
			String fundCodeValue = "";
			if (wp.colStr(ii, "fund_code").length() == 0)
				continue;
			fundCodeValue = fundCodeMap.get(wp.colStr(ii, "fund_code"))!=null
					?fundCodeMap.get(wp.colStr(ii, "fund_code")).toString():"";

			if (sqlRowNum > 0) {
				columnData = columnData + fundCodeValue;
				wp.colSet(ii, columnData1, columnData);
			}
		}
		return;
	}

	private void commTransType(String cde1) throws Exception {
		String[] cde = { "0", "1", "2", "3", "4", "5", "6", "7" };
		String[] txt = { "移轉", "新增", "贈與", "調整", "使用", "匯入", "移除", "扣回" };
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
	
	private void commModReason(String columnData1) throws Exception {
		HashMap<String,String> modReasonMap = new HashMap<String,String>();
		String columnData = "";
		String sql1 = "select wf_id, wf_desc as column_wf_desc from ptr_sys_idtab  where "
				    + " wf_type = 'ADJMOD_REASON' ";
		sqlSelect(sql1);
		for (int ii = 0; ii < sqlRowNum; ii++) {
			modReasonMap.put(sqlStr(ii,"wf_id"), sqlStr(ii,"column_wf_desc"));
		}
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			columnData = "";
			if (wp.colEmpty(ii, "mod_reason")) {
				wp.colSet(ii, columnData1, columnData);
				continue;
			}

			if (sqlRowNum > 0) {
				columnData = columnData + (modReasonMap.get(wp.colStr(ii, "mod_reason"))!=null
						?modReasonMap.get(wp.colStr(ii, "mod_reason")).toString():"");
				wp.colSet(ii, columnData1, columnData);
			}
		}
		return;
	}
	
	private void commCurrCode(String columnData1) throws Exception {
		commCurrCode(columnData1,"");
	}
	
	private void commCurrCode(String columnData1 ,String value) throws Exception {
		HashMap<String,String> currCodeMap = new HashMap<String,String>();
		String columnData = "";
		String sql1 = "select curr_code, curr_chi_name as column_curr_chi_name " 
				+ " from ptr_currcode where  bill_sort_seq != '' ";
		sqlSelect(sql1);
		for (int ii = 0; ii < sqlRowNum; ii++) {
			currCodeMap.put(sqlStr(ii,"curr_code"), sqlStr(ii,"column_curr_chi_name"));
		}
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			columnData = "";
			String currCodeValue = "";
			if("comm_curr_code".equals(columnData1)) {
				if (wp.colStr(ii, "curr_code").length() == 0)
					continue;
				currCodeValue = currCodeMap.get(wp.colStr(ii, "curr_code"))!=null
						?currCodeMap.get(wp.colStr(ii, "curr_code")).toString():"";
			}
			else {
				currCodeValue = currCodeMap.get(value)!=null
						?currCodeMap.get(value).toString():"";
			}
			if (sqlRowNum > 0) {
				columnData = columnData + currCodeValue;
				if("comm_curr_code".equals(columnData1))
					wp.colSet(ii, columnData1, columnData);
				else
					wp.colSet(columnData1, value + columnData);
			}
		}
		return;
	}
	
	private void selectCrdCard() {
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			  wp.colSet(ii,"crd_current_code_0", wp.colInt(ii,"crd_cnt") > 0 ? "有":"無");
		}
	 }
	
	private void getEntTranAmt() {
		String sqlCmdMkt = " select sum(a.beg_tran_amt) as ent_tran_amt ";
		sqlCmdMkt += " from mkt_cashback_dtl a ";
		sqlCmdMkt += " where a.TRAN_PGM  in ('ActE010' , 'CycE010' ,'ActE030' ) ";
//		sqlCmdMkt += " and a.acct_date = :acct_date ";
		sqlCmdMkt += " and a.tran_date = :tran_date ";
		
		String sqlCmdCyc = " select sum(a.beg_tran_amt) as ent_tran_amt ";
		sqlCmdCyc += " from cyc_dc_fund_dtl a ";
		sqlCmdCyc += " where a.tran_pgm  in ('CycC210' , 'CycE010' ,'ActE030' ) ";
//		sqlCmdCyc += " and a.acct_date = :acct_date and a.curr_code = :curr_code ";
		sqlCmdCyc += " and a.tran_date = :tran_date and a.curr_code = :curr_code ";
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if(wp.colEq(ii,"table_type", "MKT")) {
				setString("tran_date",wp.colStr(ii,"tran_date"));
				if(wp.itemEq("ex_select_type", "2")) {
					sqlCmdMkt += " and a.acct_type = :acct_type ";
					setString("acct_type",wp.colStr(ii,"acct_type"));
				}
				sqlSelect(sqlCmdMkt);
			}
			if(wp.colEq(ii,"table_type", "CYC")) {
				setString("tran_date",wp.colStr(ii,"tran_date"));
				setString("curr_code",wp.colStr(ii,"curr_code"));
				if(wp.itemEq("ex_select_type", "2")) {
					sqlCmdMkt += " and a.acct_type = :acct_type ";
					setString("acct_type",wp.colStr(ii,"acct_type"));
				}
				sqlSelect(sqlCmdCyc);
			}
			if(sqlRowNum > 0) {
				wp.colSet(ii,"ent_tran_amt", sqlNum("ent_tran_amt"));
			}else {
				wp.colSet(ii,"ent_tran_amt", 0);
			}
		}	
	}
	
	void getCntTranAmt() {
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			double cntTranAmt = add(wp.colNum(ii,"beg_tran_amt"),wp.colNum(ii,"ent_tran_amt"));
			wp.colSet(ii,"cnt_tran_amt", cntTranAmt);
		}
	}
	
	void getEndTranAmt1() {
		String sqlCmd = " select sum( end_tran_amt ) as sum_end_tran_amt1 from " + MKT_CASHBACK_DTL ;
//		sqlCmd += " where substring(tran_date,1,6) = :tran_date ";
//		setString("tran_date",wp.itemStr("ex_tran_date"));
		sqlSelect(sqlCmd);
		wp.colSet("end_tran_amt_901", sqlNum("sum_end_tran_amt1"));
		
		sqlCmd = " select curr_code ,sum(end_tran_amt ) as sum_end_tran_amt2 from " + CYC_DC_FUND_DTL;
//		sqlCmd += " where substring(tran_date,1,6) = :tran_date ";
		sqlCmd += " group by curr_code ";
//		setString("tran_date",wp.itemStr("ex_tran_date"));
		sqlSelect(sqlCmd);
		for(int i = 0;i<sqlRowNum;i++) {
			wp.colSet("end_tran_amt_" + sqlStr(i,"curr_code"), sqlNum(i,"sum_end_tran_amt2"));
		}
	}
	
	void getEndTranAmt2() {
		String sqlCmd = " select sum( end_tran_amt ) as sum_end_tran_amt1,acct_type from " + MKT_CASHBACK_DTL;
//		sqlCmd += " where substring(tran_date,1,6) = :tran_date ";
		sqlCmd += " group by acct_type ";
//		setString("tran_date",wp.itemStr("ex_tran_date"));
		sqlSelect(sqlCmd);
		for(int i = 0;i<sqlRowNum;i++) {
			wp.colSet("end_tran_amt_901" + sqlStr(i,"acct_type"), sqlNum(i,"sum_end_tran_amt1"));
		}
		
		sqlCmd = " select curr_code ,sum(end_tran_amt ) as sum_end_tran_amt2,acct_type from " + CYC_DC_FUND_DTL;
//		sqlCmd += " where substring(tran_date,1,6) = :tran_date ";
		sqlCmd += " group by curr_code,acct_type ";
//		setString("tran_date",wp.itemStr("ex_tran_date"));
		sqlSelect(sqlCmd);
		for(int i = 0;i<sqlRowNum;i++) {
			wp.colSet("end_tran_amt_" + sqlStr(i,"curr_code") + sqlStr(i,"acct_type"), sqlNum(i,"sum_end_tran_amt2"));
		}
	}
	
	void getDataCnt(){
		for(int i =0; i< wp.selectCnt ;i++) {
			String sqlCmd = "";
			if(wp.colEq(i,"table_type", "MKT")) {
				sqlCmd += " select count(*) as data_cnt from (select 1 from " + controlTabName1;
				sqlCmd += " where tran_date = :tran_date  ";
				if(wp.itemEq("ex_select_type","2")) {
					sqlCmd += " and acct_type = :acct_type ";
					setString("acct_type",wp.colStr(i,"acct_type"));
				}
				sqlCmd += " group by id_p_seqno,fund_code,acct_type)  ";
				setString("tran_date",wp.colStr(i,"tran_date"));
			}
			if(wp.colEq(i,"table_type", "CYC")) {
				sqlCmd += " select count(*) as data_cnt from (select 1 from " + controlTabName2;
				sqlCmd += " where tran_date = :tran_date and curr_code = :curr_code ";
				if(wp.itemEq("ex_select_type","2")) {
					sqlCmd += " and acct_type = :acct_type ";
					setString("acct_type",wp.colStr(i,"acct_type"));
				}
				sqlCmd += " group by id_p_seqno,fund_code,curr_code,acct_type)  ";
				setString("tran_date",wp.colStr(i,"tran_date"));
				setString("curr_code",wp.colStr(i,"curr_code"));
				setString("acct_type",wp.colStr(i,"acct_type"));
			}
			sqlSelect(sqlCmd);
			wp.colSet(i,"data_cnt", sqlInt("data_cnt"));
		}
	}
	
	private void sumAmt(String currCode,String amt) {
		double sumAmt901 = 0;
		double sumAmt840 = 0;
		double sumAmt392 = 0;
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if(wp.colEq(ii,currCode,"901")) {
				sumAmt901 = add(sumAmt901,wp.colNum(ii,amt));
			}
			if(wp.colEq(ii,currCode,"840")) {
				sumAmt840 = add(sumAmt840,wp.colNum(ii,amt));
			}
			if(wp.colEq(ii,currCode,"392")) {
				sumAmt392 = add(sumAmt392,wp.colNum(ii,amt));
			}
		}
		wp.colSet(amt + "_901", sumAmt901);
		wp.colSet(amt + "_840", sumAmt840);
		wp.colSet(amt + "_392", sumAmt392);
	}
	
	private void sumAmt(String currCode,String acctType,String amt) {
		double sumAmt90101 = 0;
		double sumAmt90103 = 0;
		double sumAmt90106 = 0;
		double sumAmt84001 = 0;
		double sumAmt39201 = 0;
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if("90101".equals(wp.colStr(ii,currCode) + wp.colStr(ii,acctType))) {
				sumAmt90101 = add(sumAmt90101,wp.colNum(ii,amt));
			}
			if("90103".equals(wp.colStr(ii,currCode) + wp.colStr(ii,acctType))) {
				sumAmt90103 = add(sumAmt90103,wp.colNum(ii,amt));
			}
			if("90106".equals(wp.colStr(ii,currCode) + wp.colStr(ii,acctType))) {
				sumAmt90106 = add(sumAmt90106,wp.colNum(ii,amt));
			}
			if("84001".equals(wp.colStr(ii,currCode) + wp.colStr(ii,acctType))) {
				sumAmt84001 = add(sumAmt84001,wp.colNum(ii,amt));
			}
			if("39201".equals(wp.colStr(ii,currCode) + wp.colStr(ii,acctType))) {
				sumAmt39201 = add(sumAmt39201,wp.colNum(ii,amt));
			}
		}
		wp.colSet(amt + "_90101", sumAmt90101);
		wp.colSet(amt + "_90103", sumAmt90103);
		wp.colSet(amt + "_90106", sumAmt90106);
		wp.colSet(amt + "_84001", sumAmt84001);
		wp.colSet(amt + "_39201", sumAmt39201);
	}
	
	
		private Double add(Double v1, Double v2) {
			BigDecimal b1 = new BigDecimal(v1.toString());
			BigDecimal b2 = new BigDecimal(v2.toString());

			return b1.add(b2).doubleValue();

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

}  // End of class
