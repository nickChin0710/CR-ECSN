package ccar01;
/** 不當代行報表
 * 2019-1016   JH    ++describe
 * 2019-1014   JH    data check
 *  V.2018-0730.jh
 * 109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard   
 * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *    
 * */

import java.util.ArrayList;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ccar0150 extends BaseAction implements InfacePdf {
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
		}
		// else if (eq_igno(wp.buttonCode, "A")) {
		// /* 新增功能 */
		// saveFunc();
		// }
		// else if (eq_igno(wp.buttonCode, "U")) {
		// /* 更新功能 */
		// saveFunc();
		// }
		// else if (eq_igno(wp.buttonCode, "D")) {
		// /* 刪除功能 */
		// saveFunc();
		// }
		else if (eqIgno(wp.buttonCode, "M")) {
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
		String lsTime1 = "", lsTime2 = "";
		ArrayList<Object> whereParams = new ArrayList<Object>();
		if (!empty(wp.itemStr("ex_time1")) || !empty(wp.itemStr("ex_time2"))) {
			if (eqIgno(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
				alertErr2("時間欄位有值，日期區間只能選擇同一天。");
				return;
			}

			if (empty(wp.itemStr("ex_time1")) || empty(wp.itemStr("ex_time2"))) {
				alertErr2("時間欄位:不可空白");
				return;
			}

			if (wp.itemNum("ex_time1") > wp.itemNum("ex_time2")) {
				alertErr2("時間起迄:輸入錯誤");
				return;
			}

		}
		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("日期起迄：輸入錯誤");
			return;
		}
		// String ls_where = " where 1=1 and B.bin_type =substr(A.card_no,1,6) and
		// stand_in_onuscode in
		// (select resp_code from cca_resp_code where abnorm_flag='Y') "
		String lsWhere = " where 1=1 and stand_in_onuscode in (select resp_code from cca_resp_code where abnorm_flag='Y') "
//				+ " and ((mcht_no <> '6666600000' and auth_unit <> 'A') or (mcht_no = '6666600000' and auth_unit = 'T')) "
				+ " and trans_type = '0120' "
				+ " and iso_resp_code in ('000','001','00') " + sqlCol(wp.itemStr("ex_card_no"), "card_no")
				+ sqlCol(wp.itemStr("ex_resp_code"), "stand_in_onuscode");

		if (!(wp.itemEmpty("ex_time1"))) {
			lsTime1 = wp.itemStr("ex_time1");
			lsTime1 = commString.rpad(lsTime1, 6, "0");
		}

		if (!(wp.itemEmpty("ex_time2"))) {
			lsTime2 = wp.itemStr("ex_time2");
			lsTime2 = commString.rpad(lsTime2, 6, "0");
		}

		if (empty(wp.itemStr("ex_time1")) && empty(wp.itemStr("ex_time2"))) {
			lsWhere += sqlCol(wp.itemStr("ex_date1"), "A.tx_date", ">=");
			lsWhere += sqlCol(wp.itemStr("ex_date2"), "A.tx_date", "<=");
		} else {
			lsWhere += sqlCol(wp.itemStr("ex_date1"), "tx_date") + sqlStrend(lsTime1, lsTime2, "tx_time");
		}

		String sumWhere = " where 1=1 and stand_in_onuscode in (select resp_code from cca_resp_code where abnorm_flag='Y') "
//				+ " and ((mcht_no <> '6666600000' and auth_unit <> 'A') or (mcht_no = '6666600000' and auth_unit = 'T')) "
				+ " and trans_type = '0120' "
				+ " and iso_resp_code in ('000','001','00') ";
		if (wp.itemEmpty("ex_card_no") == false) {
			sumWhere += commSqlStr.col(wp.itemStr("ex_card_no"), "card_no");
			whereParams.add(wp.itemStr("ex_card_no"));
		}
		
		if (wp.itemEmpty("ex_resp_code") == false) {
			sumWhere += commSqlStr.col(wp.itemStr("ex_resp_code"), "stand_in_onuscode");
			whereParams.add(wp.itemStr("ex_resp_code"));
		}
		
		if (empty(wp.itemStr("ex_time1")) && empty(wp.itemStr("ex_time2"))) {
			if(wp.itemEmpty("ex_date1")==false) {
				sumWhere += commSqlStr.col(wp.itemStr("ex_date1"), "A.tx_date", ">=");
				whereParams.add(wp.itemStr("ex_date1"));
			}
			if(wp.itemEmpty("ex_date2")==false) {
				sumWhere += commSqlStr.col(wp.itemStr("ex_date2"), "A.tx_date", "<=");
				whereParams.add(wp.itemStr("ex_date2"));
			}			
		} else {
			if(wp.itemEmpty("ex_date1")==false) {
				sumWhere += commSqlStr.col(wp.itemStr("ex_date1"), "A.tx_date");
				whereParams.add(wp.itemStr("ex_date1"));
			}
			if(lsTime1.isEmpty()==false) {
				sumWhere += commSqlStr.col(lsTime1, "A.tx_time",">=");
				whereParams.add(lsTime1);
			}
			if(lsTime2.isEmpty()==false) {
				sumWhere += commSqlStr.col(lsTime2, "A.tx_time","<=");
				whereParams.add(lsTime2);
			}			
		}
		whereParameterList = whereParams;
		listSum(sumWhere);

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	void listSum(String lsWhere) {
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = " select " + " count(*) as tot_cnt , " + " sum(A.nt_amt) as tot_amt " + " from cca_auth_txlog A"
				+ lsWhere;

		sqlSelect(sql1,tmpObjParams);

		wp.colSet("tot_cnt", sqlNum("tot_cnt"));
		wp.colSet("tot_amt", sqlNum("tot_amt"));
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " A.card_no , " + " A.tx_date , " + " A.tx_time , " + " A.nt_amt , " + " A.auth_no , "
				+ " A.stand_in_onuscode , "
				// + " A.auth_status_code , "
				+ " A.mcht_no , " + " A.mcc_code , " + " A.consume_country , " + " A.curr_otb_amt , "
				+ " A.class_code , " + " A.eff_date_end , " + " A.pos_mode , " + " A.pos_term_id , "
				+ " '' as message_head5 , " + " '' as message_head6 , " + " A.trans_type , " + " A.stand_in , "
				+ " A.auth_unit , " + " substr(uf_tt_resp_code(A.stand_in_onuscode),1,10) as tt_resp_code,"
				+ " uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit," + " '' as xxx" // "
																								// B.bin_type
																								// "
		;
		// wp.daoTable = "cca_auth_txlog A, ptr_bintable B";
		wp.daoTable = "cca_auth_txlog A";

		pageQuery();

		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}

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
	public void pdfPrint() throws Exception {
		wp.reportId = "Ccar0150";

		String cond1 = "";
		wp.colSet("cond1", cond1);
		wp.colSet("user_id", wp.loginUser);
		wp.pageRows = 9999;
		queryFunc();

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar0150.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

}
