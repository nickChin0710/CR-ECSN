package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*/

import java.util.ArrayList;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar0180 extends BaseAction implements InfacePdf {
	ArrayList<Object> whereParameterList = null;

	@Override
	public void userAction() throws Exception {
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			procFunc();
		} else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
			strAction = "PDF";
			pdfPrint();
		}
	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		ArrayList<Object> whereParams = new ArrayList<Object>();
		
		if(wp.itemEmpty("ex_date1") || wp.itemEmpty("ex_date2")) {
			alertErr("日期: 不可空白");
			return ;
		}
		
		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("日期起迄：輸入錯誤");
			return;
		}
		String lsWhere = " where 1=1 and auth_unit = 'K' " + sqlCol(wp.itemStr("ex_date1"), "tx_date", ">=")
				+ sqlCol(wp.itemStr("ex_date2"), "tx_date", "<=") + sqlCol(wp.itemStr("ex_card_no"), "card_no")
				+ sqlCol(wp.itemStr("ex_user"), "auth_user");
		
		if(wp.itemEq("ex_auth", "Y")) {
			lsWhere += " and trans_type = '0200' ";
		}	else if(wp.itemEq("ex_auth", "N")) {
			lsWhere += " and trans_type = '0420' ";
		}		
		
		String sumWhere = " where 1=1 and auth_unit = 'K' ";
		
		if(wp.itemEq("ex_auth", "Y")) {
	    	sumWhere += " and trans_type = '0200' ";
		}	else if(wp.itemEq("ex_auth", "N")) {
			sumWhere += " and trans_type = '0420' ";
		}
		
	    if(wp.itemEmpty("ex_date1")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_date1"), "tx_date",">=");
	    	whereParams.add(wp.itemStr("ex_date1"));
	    }	    	    
	    
	    if(wp.itemEmpty("ex_date2")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_date2"), "tx_date","<=");
	    	whereParams.add(wp.itemStr("ex_date2"));
	    }	    	    
	    
	    if(wp.itemEmpty("ex_card_no")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_card_no"), "card_no");
	    	whereParams.add(wp.itemStr("ex_card_no"));
	    }
	    if(wp.itemEmpty("ex_user")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_user"), "auth_user");
	    	whereParams.add(wp.itemStr("ex_user"));
	    }
	    whereParameterList = whereParams;	    
		sumData1(sumWhere);
		sumData2(sumWhere);
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " iso_resp_code ," + " cacu_amount ," + " card_no ," + " tx_date ," + " tx_time ,"
				+ " eff_date_end ," + " mcc_code ," + " class_code ," + " stand_in ," + " mcht_no ," + " nt_amt ,"
				+ " mcc_code ," + " risk_type ," + " auth_status_code ," + " auth_no ," + " curr_otb_amt ,"
				+ " 0 as over_pct ," + " auth_user ," + " apr_user ," + " auth_remark," + " curr_tot_std_amt ,"
				+ " acno_p_seqno , " + " acct_type , decode(trans_type,'0200','人工授權','0420','取消授權',trans_type) as tt_trans_type ";
		wp.daoTable = "cca_auth_txlog";
		wp.whereOrder = "  order by auth_user, card_no, auth_no, tx_date, tx_time ";
		if (empty(wp.whereStr)) {
			wp.whereStr = " ORDER BY 1";
		}

		logSql();
		pageQuery();

		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			set0();
			alertErr2("此條件查無資料");
			return;
		}
		listWkdata();
		wp.setPageValue();

	}

	void listWkdata() {
		String sql1 = " select " + " int_rate_mcode " + " from act_acno " + " where acno_p_seqno = ? ";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			double lsOverPct = 0;
			if (wp.colNum(ii, "curr_tot_std_amt") == 0)
				wp.colSet(ii, "over_pct", "0");
			else {
				lsOverPct = (double) ((double) wp.colNum(ii, "curr_otb_amt") * 100 / wp.colNum(ii, "curr_tot_std_amt"));
				wp.colSet(ii, "over_pct", "" + lsOverPct);
			}

			if (!wp.colEq(ii, "acct_type", "90")) {
				sqlSelect(sql1, new Object[] { wp.colStr(ii, "acno_p_seqno") });
				if (sqlRowNum > 0) {					
					wp.colSet(ii, "mcode", sqlStr("int_rate_mcode"));
				}
			}

		}
	}

	void set0() {
		wp.colSet("db_cnt1", "0");
		wp.colSet("db_nt_amt1", "0");
		wp.colSet("db_cnt2", "0");
		wp.colSet("db_nt_amt2", "0");
	}

	void sumData1(String lsWhere) {
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = "select count(*) as db_cnt1, sum(nt_amt) as db_nt_amt1 from cca_auth_txlog "+lsWhere+" and iso_resp_code = '00' " ;
		sqlSelect(sql1,tmpObjParams);
		if (sqlRowNum < 0) {
			wp.colSet("db_cnt1", "0");
			wp.colSet("db_nt_amt1", "0");
			return ;
		}
		
		wp.colSet("db_cnt1", sqlStr("db_cnt1"));
		wp.colSet("db_nt_amt1", sqlStr("db_nt_amt1"));
		
	}

	void sumData2(String lsWhere) {
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = "select count(*) as db_cnt2, sum(nt_amt) as db_nt_amt2 from cca_auth_txlog "+lsWhere+" and iso_resp_code <> '00' " ;
		sqlSelect(sql1,tmpObjParams);
		if (sqlRowNum < 0) {
			wp.colSet("db_cnt2", "0");
			wp.colSet("db_nt_amt2", "0");
			return ;
		}
		
		wp.colSet("db_cnt2", sqlStr("db_cnt2"));
		wp.colSet("db_nt_amt2", sqlStr("db_nt_amt2"));
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
	public void pdfPrint() throws Exception {
		wp.reportId = "Ccar0180";

		String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
				+ commString.strToYmd(wp.itemStr("ex_date2"));
		wp.colSet("cond1", cond1);
		wp.colSet("user_id", wp.loginUser);
		wp.pageRows = 9999;
		queryFunc();

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar0180.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

}
