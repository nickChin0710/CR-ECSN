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

/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  110-01-14  V1.00.02  Justin                    fix parameterize sql bugs
*/

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.base.CommString;
import taroko.com.TarokoExcel;

public class Ccar0020 extends BaseAction implements InfaceExcel {
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
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			strAction = "XLS";
			xlsPrint();
		}

	}

	@Override
	public void dddwSelect() {
		try {
			if (eqIgno(wp.respHtml, "ccar0020")) {
				wp.optionKey = wp.colStr(0, "ex_group_code");
				dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1");
			}
		} catch (Exception ex) {
		}

		try {
			if (eqIgno(wp.respHtml, "ccar0020")) {
				wp.optionKey = wp.colStr(0, "ex_opp_type");
				dddwList("dddw_opptype_list", "cca_sys_parm3", "sys_key", "sys_data1",
						"where sys_id = 'OPPTYPE' and sys_key not in ('0','6') ");
				wp.optionKey = wp.colStr(0, "ex_opp_status");
				dddwList("dddw_opp_status", "cca_opp_type_reason", "opp_status", "opp_status||'_'||opp_remark",
						"where 1=1 ");
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		
		if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_bin_no")
				&& wp.itemEmpty("ex_group_code") && wp.itemEmpty("ex_opp_type") && wp.itemEmpty("ex_opp_status")) {
			alertErr2("請輸入查詢條件");
			return;
		}

		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("停掛日期起迄：輸入錯誤");
			return;
		}

	    String lsWhere = " where 1=1  " 
	        + sqlCol(wp.itemStr("ex_date1"), "A.oppo_date", ">=")
	        + sqlCol(wp.itemStr("ex_date2"), "A.oppo_date", "<=")
	        + sqlCol(wp.itemStr("ex_bin_no"), "A.card_no", "like%")
	        + sqlCol(wp.itemStr("ex_group_code"), "A.group_code")
	        + sqlCol(wp.itemStr("ex_opp_type"), "A.oppo_type")
	        + sqlCol(wp.itemStr("ex_opp_status"), "A.oppo_status");		
	    //--
	    

	    sqlParm.setSqlParmNoClear(true);
		listSum(lsWhere);

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	void listSum(String lsWhere) {
	
		String sql1 = " select " + " count(*) as tl_cnt , " + " sum(decode(A.logic_del,'Y',1,0)) as tl_del_cnt "
				+ " from cca_opposition A " + lsWhere;

		sqlSelect(sql1);

		wp.colSet("tl_cnt", sqlStr("tl_cnt"));
		wp.colSet("tl_del_cnt", sqlStr("tl_del_cnt"));
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " substr(A.card_no,1,6) as bin_no," + " A.group_code," + " A.card_type, " + " A.oppo_type, "
				+ " A.oppo_status," + " count(*) as oppo_cnt," + " sum(decode(logic_del,'Y',1,0)) as oppo_del_cnt";
		wp.daoTable = "cca_opposition A";
		wp.whereOrder = " group by  substr(A.card_no,1,6), group_code, card_type, oppo_type, oppo_status  ";
		if (empty(wp.whereStr)) {
			wp.whereStr = " ORDER BY 1";
		}

		logSql();

		wp.pageCountSql = "" + "select count(*) from ( "
				+ " select distinct substr(A.card_no,1,6) as bin_no , A.group_code , A.card_type , "
				+ " A.oppo_type , oppo_status " 
				+ " from cca_opposition A " 
				+ wp.whereStr + " )";

		pageQuery();

		if (sqlRowNum <= 0) {

			alertErr2("此條件查無資料");
			return;
		}

		wp.setListCount(1);
		wp.setPageValue();
		queryAfter();
	}

	void queryAfter() {
		String sql1 = " select " + " opp_remark " + " from cca_opp_type_reason " + " where 1=1 "
				+ " and opp_status = ? ";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "oppo_status") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "oppo_status", wp.colStr(ii, "oppo_status") + "_" + sqlStr("opp_remark"));
			}
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
	public void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = "ccar0020";
			String cond1 = "停掛日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
					+ commString.strToYmd(wp.itemStr("ex_date2"));
			wp.colSet("cond1", cond1);
			wp.colSet("user_id", wp.loginUser);
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
			xlsx.excelTemplate = "ccar0020.xlsx";
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
