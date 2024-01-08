package dbar01;

import java.util.ArrayList;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;

public class Dbar0065 extends BaseAction implements InfaceExcel {
	ArrayList<Object> whereParameterList = null;
	String lsWhere = "";
	@Override
	public void userAction() throws Exception {
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
	        saveFunc();
	        break;
	      case "U":
	        /* 更新功能 */
	        saveFunc();
	        break;
	      case "D":
	        /* 刪除功能 */
	        saveFunc();
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
	      case "C":
	        // -資料處理-
	        procFunc();
	        break;	     
	      case "XLS":
	    	// --列印Excel
			xlsPrint();  
	      default:
	        break;
	    }
	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		
		if(wp.itemEmpty("ex_date1") || wp.itemEmpty("ex_date2")) {
			alertErr2("解圈日期: 不可空白");
			return ;
		}
		
		if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("解圈日期: 起迄錯誤");
			return;
		}
									
		getWhere();
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();	
	}
	
	void getWhere() {
		ArrayList<Object> whereParams = new ArrayList<Object>(); 
		
		lsWhere = " where 1=1 and A.unlock_flag = 'M' ";
		if (wp.itemEmpty("ex_date1") == false) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_date1"), "A.chg_date", ">=");
			whereParams.add(wp.itemStr("ex_date1"));
		}
		
		if (wp.itemEmpty("ex_date2") == false) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_date2"), "A.chg_date", "<=");
			whereParams.add(wp.itemStr("ex_date2"));
		}
		
		if(wp.itemEmpty("ex_user") == false) {
			lsWhere += " and (A.chg_user = ? or A.mod_user = ? ) ";
			whereParams.add(wp.itemStr("ex_user"));
			whereParams.add(wp.itemStr("ex_user"));
		}
		
		whereParameterList = whereParams;
		
	}
	
	@Override
	public void queryRead() throws Exception {
		Object[] paramArr = null ;
		if (whereParameterList != null) {
			paramArr = whereParameterList.toArray();
		}
		wp.pageControl();
		wp.selectSQL = "hex(A.rowid) as rowid ," + " A.auth_seqno ," + " A.card_no ," + " A.tx_date ," + " A.tx_time ,"
				+ " A.eff_date_end ," + " A.mcht_no ," + " A.mcht_name ," + " A.mcc_code ," + " A.pos_mode ,"
				+ " substr(A.pos_mode,1,2) as pos_mode_1_2 ," + " substr(A.pos_mode,3,1) as pos_mode_3 ,"
				+ " A.nt_amt ," + " A.consume_country ," + " A.tx_currency ," + " A.iso_resp_code ,"
				+ " A.auth_status_code ," + " A.iso_adj_code ," + " A.auth_no ," + " A.auth_user ," + " A.vip_code ,"
				+ " A.stand_in ," + " A.class_code ," + " A.auth_unit ," + " A.logic_del ," + " A.auth_remark ,"
				+ " A.trans_type ," + " uf_idno_id2(A.card_no,'') as id_no ,"
				+ " uf_idno_name(A.id_p_seqno) as db_idno_name ," + " A.curr_otb_amt ," + " A.curr_tot_lmt_amt ,"
				+ " A.curr_tot_std_amt ," + " A.curr_tot_tx_amt ," + " A.curr_tot_cash_amt ," + " A.curr_tot_unpaid ,"
				+ " A.fallback ," + " A.roc ," + " A.ibm_bit39_code ," + " A.ibm_bit33_code ," + " A.ec_ind ,"
				+ " A.ucaf ," + " A.mtch_flag, A.cacu_amount," + " A.ec_flag ,"
				+ " uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del ," + " A.v_card_no ,"
				+ " A.online_redeem ," + " uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit ,"
				+ " decode(A.online_redeem,'','','A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)',"
				+ " '3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem ,"
				+ " decode(curr_tot_std_amt,0,0,((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)) * 100 as cond_curr_rate , "
				+ " A.id_p_seqno , " + " iso_resp_code||'-'||auth_status_code||'-'||iso_adj_code as wk_resp , "
				+ " ibm_bit39_code||'-'||ibm_bit33_code as wk_IBM , "
				+ " A.ori_amt , A.trace_no , A.ref_no , A.tx_seq , uf_corp_no(A.corp_p_seqno) as corp_no , "
				+ " A.vd_lock_nt_amt , A.card_acct_idx , A.acno_p_seqno , A.chg_date , A.chg_time , decode(A.chg_user,'',A.mod_user,A.chg_user) as chg_user , "
				+ " A.iso_resp_code||'-'||A.auth_status_code||'-'||iso_adj_code as tt_resp_code ";

		wp.daoTable = " cca_auth_txlog A ";
		wp.whereOrder = " order by A.tx_date desc, A.tx_time Desc ";

		pageQuery(paramArr);

		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}		
		wp.setListCount(0);
		wp.setPageValue();

	}
		
	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

	@Override
	public void xlsPrint() throws Exception {
		try {			
		    log("xlsFunction: started--------");
		    wp.reportId = "dbar0065";		      
		    TarokoExcel xlsx = new TarokoExcel();
		    wp.fileMode = "Y";
		    xlsx.excelTemplate = "dbar0065.xlsx";
		    wp.pageRows = 9999;
		    queryFunc();

		    xlsx.processExcelSheet(wp);
		    xlsx.outputExcel();
		    xlsx = null;
		    log("xlsFunction: ended-------------");
		} catch (Exception ex) {
		    wp.expMethod = "xlsPrint";
		    wp.expHandle(ex);
		}		
		
	}

	@Override
	public void logOnlineApprove() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
