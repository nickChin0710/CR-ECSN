/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package ccar01;

import java.util.ArrayList;

/**
 * 2019-1014   JH    data check
 * 109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard  
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ccar0270 extends BaseAction implements InfacePdf {
	String lsWhere = "", lsDate1 = "", lsDate2 = "";
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
		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("異動日期起迄：輸入錯誤");
			return;
		}
		lsWhere = "";
		lsDate1 = wp.itemStr("ex_date1");
		lsDate2 = wp.itemStr("ex_date2");

		if (wp.itemEq("ex_type", "1")) {
			wp.colSet("pageType", "table1");
			lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_card_no"), "card_no")
					+ sqlCol(wp.itemStr("ex_user"), "oppo_user") + sqlCol(wp.itemStr("ex_oppo_reason"), "oppo_status");
			lsWhere += " and ( (1=1 " + sqlStrend(lsDate1, lsDate2, "crt_date") + ")";
			lsWhere += " or (1=1 " + sqlStrend(lsDate1, lsDate2, "chg_date") + ")";
			lsWhere += " or (1=1 " + sqlStrend(lsDate1, lsDate2, "logic_del_date") + ") )";
			lsWhere += " and uf_ccar0270(card_type,debit_flag,crt_user,vis_reason_code,visa_resp_code,mst_reason_code,neg_resp_code) =0";	
			
			String sumWhere = " where 1=1 ";
		    if(wp.itemEmpty("ex_card_no")==false) {
		    	sumWhere += commSqlStr.col(wp.itemStr("ex_card_no"), "card_no");
		    	whereParams.add(wp.itemStr("ex_card_no"));
		    }
		    if(wp.itemEmpty("ex_user")==false) {
		    	sumWhere += commSqlStr.col(wp.itemStr("ex_user"), "oppo_user");
		    	whereParams.add(wp.itemStr("ex_user"));
		    }
		    if(wp.itemEmpty("ex_oppo_reason")==false) {
		    	sumWhere += commSqlStr.col(wp.itemStr("ex_oppo_reason"), "oppo_status");
		    	whereParams.add(wp.itemStr("ex_oppo_reason"));
		    }
		    sumWhere += " and ( (1=1 " + commSqlStr.strend(lsDate1, lsDate2, "crt_date") + ")";
		    if (lsDate1.isEmpty()==false) {
				whereParams.add(lsDate1);
			}
		    if (lsDate2.isEmpty()==false) {
				whereParams.add(lsDate2);
			}
		    sumWhere += " or (1=1 " + commSqlStr.strend(lsDate1, lsDate2, "chg_date") + ")";
		    if (lsDate1.isEmpty()==false) {
				whereParams.add(lsDate1);
			}
		    if (lsDate2.isEmpty()==false) {
				whereParams.add(lsDate2);
			}
		    sumWhere += " or (1=1 " + commSqlStr.strend(lsDate1, lsDate2, "logic_del_date") + ") )";
			if (lsDate1.isEmpty()==false) {
				whereParams.add(lsDate1);
			}
		    if (lsDate2.isEmpty()==false) {
				whereParams.add(lsDate2);
			}
		    sumWhere += " and uf_ccar0270(card_type,debit_flag,crt_user,vis_reason_code,visa_resp_code,mst_reason_code,neg_resp_code) =0";
		    whereParameterList = whereParams;
			sum1(sumWhere);
		} else {
			wp.colSet("pageType", "table2");
			lsWhere = " where 1=1 " 
					+ sqlCol(wp.itemStr("ex_card_no"), "card_no")
					+ sqlCol(wp.itemStr("ex_spec_status"), "spec_status");
			lsWhere += " and ( (1=1 " + sqlStrend(lsDate1, lsDate2, "crt_date") + ")"; 
			lsWhere += " or (1=1 " + sqlStrend(lsDate1, lsDate2, "chg_date") + ")";
			lsWhere += " or (1=1 " + sqlStrend(lsDate1, lsDate2, "logic_del_date") + ") )";
			lsWhere += " and ( vm_resp_code not in ('','00')";
			lsWhere += " or vm_del_resp_code not in ('','00')";
			lsWhere += " or neg_resp_code not in ('','00')";
			lsWhere += " or neg_del_resp_code not in ('','00') )";

			String lsUser = wp.itemStr2("ex_user");
			if (!empty(wp.itemStr("ex_user"))) {
				lsWhere += " and ? in (crt_user,chg_user)";
				setString(lsUser);
			}
			
			String sumWhere = " where 1=1 ";
		    if(wp.itemEmpty("ex_card_no")==false) {
		    	sumWhere += commSqlStr.col(wp.itemStr("ex_card_no"), "card_no");
		    	whereParams.add(wp.itemStr("ex_card_no"));
		    }
		    if(wp.itemEmpty("ex_spec_status")==false) {
		    	sumWhere += commSqlStr.col(wp.itemStr("ex_spec_status"), "spec_status");
		    	whereParams.add(wp.itemStr("ex_spec_status"));
		    }
		    sumWhere += " and ( (1=1 " + commSqlStr.strend(lsDate1, lsDate2, "crt_date") + ")";
		    if (lsDate1.isEmpty()==false) {
				whereParams.add(lsDate1);
			}
		    if (lsDate2.isEmpty()==false) {
				whereParams.add(lsDate2);
			}
		    sumWhere += " or (1=1 " + commSqlStr.strend(lsDate1, lsDate2, "chg_date") + ")";
		    if (lsDate1.isEmpty()==false) {
				whereParams.add(lsDate1);
			}
		    if (lsDate2.isEmpty()==false) {
				whereParams.add(lsDate2);
			}
		    sumWhere += " or (1=1 " + commSqlStr.strend(lsDate1, lsDate2, "logic_del_date") + ") )";
			if (lsDate1.isEmpty()==false) {
				whereParams.add(lsDate1);
			}
		    if (lsDate2.isEmpty()==false) {
				whereParams.add(lsDate2);
			}
		    sumWhere += " and ( vm_resp_code not in ('','00')";
		    sumWhere += " or vm_del_resp_code not in ('','00')";
		    sumWhere += " or neg_resp_code not in ('','00')";
		    sumWhere += " or neg_del_resp_code not in ('','00') )";
		    if (!empty(wp.itemStr("ex_user"))) {
		    	sumWhere += " and ? in (crt_user,chg_user)";				
				whereParams.add(lsUser);
			}
		    whereParameterList = whereParams;
			sum2(sumWhere);
		}

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		if (wp.colEq("pageType", "table1")) {
			wp.selectSQL = " card_no , debit_flag," + " uf_hi_cardno(card_no) as hh_cardno , " + " crt_date , "
					+ " oppo_date , " + " oppo_type , " + " oppo_status , " + " opp_remark , " + " chg_date , "
					+ " logic_del , " + " logic_del_date , " + " neg_resp_code , " + " visa_resp_code , "
					+ " bin_type , " + " neg_del_date , " + " mst_reason_code , " + " vis_reason_code , "
					+ " crt_user , " + " mcas_resp_code , " + " oppo_user ";
			wp.daoTable = "cca_opposition";
			wp.whereOrder = " order by card_no, crt_date,chg_date,logic_del_date ";

			pageQuery();
			wp.setListCount(1);
			if (sqlRowNum <= 0) {
				alertErr2("此條件查無資料");
				return;
			}

			wp.setPageValue();
		} else {
			wp.selectSQL = " card_no , " + " uf_hi_cardno(card_no) as hh_cardno , " + " crt_date , " + " chg_date , "
					+ " mcas_resp_code , " + " logic_del , " + " neg_del_resp_code , " + " vm_del_resp_code , "
					+ " logic_del_date , " + " vm_resp_code , " + " neg_resp_code , " + " spec_status , "
					+ " bin_type , " + " spec_del_date , " + " spec_outgo_reason , " + " spec_neg_reason , "
					+ " spec_mst_vip_amt , " + " spec_remark , " + " crt_user ,"
					+ " decode(logic_del,'Y',neg_del_resp_code,neg_resp_code) as wk_neg_rsp_code ,"
					+ " decode(logic_del,'Y',vm_del_resp_code,vm_resp_code) as wk_vm_rsp_code ,"
					+ " spec_status||'.'||(select spec_desc from cca_spec_code where spec_code = spec_status) as wk_spec_status , "
					+ " decode(mod_user,'',decode(chg_user,'',crt_user,chg_user),mod_user) as mod_user ";
			wp.daoTable = "cca_special_visa";
			wp.whereOrder = " order by card_no, crt_date,chg_date,logic_del_date ";

			pageQuery();

			wp.setListCount(2);
			if (sqlRowNum <= 0) {
				alertErr2("此條件查無資料");
				return;
			}

			wp.setPageValue();
		}
	}

	void sum1(String lsWhere) {
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = "select count(*) as db_cnt from cca_opposition "+lsWhere;
		sqlSelect(sql1,tmpObjParams);
		if (sqlRowNum < 0) {
			wp.colSet("db_cnt", "0");
			return ;
		}
		
		wp.colSet("db_cnt", sqlStr("db_cnt"));
	}

	void sum2(String lsWhere) {
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = "select count(*) as db_cnt from cca_special_visa "+lsWhere;
		sqlSelect(sql1,tmpObjParams);
		if (sqlRowNum < 0) {
			wp.colSet("db_cnt", "0");
			return ;
		}
		
		wp.colSet("db_cnt", sqlStr("db_cnt"));
	}

	void listWkdata1() {
		for (int ii = 0; ii < wp.selectCnt; ii++) {

		}
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
		wp.reportId = "Ccar0270";

		String cond1 = "異動日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
				+ commString.strToYmd(wp.itemStr("ex_date2"));
		if (wp.itemEq("ex_type", "1")) {
			cond1 += "   傳送類別:1.停掛";
		} else {
			cond1 += "   傳送類別:2.特指";
		}
		wp.colSet("user_id", wp.loginUser);
		wp.colSet("cond1", cond1);
		wp.pageRows = 9999;
		queryFunc();

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		if (wp.itemEq("ex_type", "1")) {
			pdf.setListIndex(1);
			pdf.excelTemplate = "ccar0270-1.xlsx";
		} else {
			pdf.setListIndex(2);
			pdf.excelTemplate = "ccar0270-2.xlsx";
		}

		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

}
