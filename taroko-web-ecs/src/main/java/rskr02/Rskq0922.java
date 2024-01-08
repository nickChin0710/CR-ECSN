package rskr02;

/** 
 * 2019-1224:  Alex  queryRead add aft_loc_cash ,bef_loc_cash
 * 2019-0628:  JH    modify
 * 2019-0621:  JH    p_xxx >>acno_p_xxx
 * 109-04-28  V1.00.03  Tanwei       updated for project coding standard
 * 109-12-24  V1.00.04  Justin         parameterize sql
 * *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Rskq0922 extends BaseAction implements InfacePdf {

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
		} else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			procFunc();
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
		try {
			if (eqIgno(wp.respHtml, "rskq0922")) {
				wp.optionKey = wp.colStr(0, "ex_dept_code");
				dddwList("dddw_dept_code", "ptr_dept_code", "dept_code", "dept_code||'_'||dept_name", "where 1=1");
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		if (itemallEmpty("ex_date1,ex_date2,ex_card_no".split(","))) {
			alertErr2("日期起迄, 卡號: 不可全部空白");
			return;
		}

		if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("日期起迄錯誤");
			return;
		}

		String lsWhere = getWhereStr();
		setSqlParmNoClear(true);
		sum(lsWhere);

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	private String getWhereStr() {
		String lsWhere = " where 1=1 " + " and kind_flag ='C' " + " and emend_type ='4' " + " and apr_date <> '' "
				+ sqlCol(wp.itemStr("ex_date1"), "apr_date", ">=") + sqlCol(wp.itemStr("ex_date2"), "apr_date", "<=")
				+ sqlCol(wp.itemStr("ex_card_no"), "card_no") + sqlCol(wp.itemStr("ex_mod_user"), "mod_user", "like%")
				+ sqlCol(wp.itemStr("ex_dept_code"), "user_dept_no");

		if (wp.itemEq("ex_adj_loc_flag", "1")) {
			lsWhere += " and adj_loc_flag ='1' ";
		} else if (wp.itemEq("ex_adj_loc_flag", "2")) {
			lsWhere += " and adj_loc_flag ='2' ";
		} else if (wp.itemEq("ex_adj_loc_flag", "3")) {
			lsWhere += " and card_adj_limit > 0 ";
		} else if (wp.itemEq("ex_adj_loc_flag", "4")) {
			lsWhere += " and son_card_flag <> 'Y' ";
		}
		
		
		return lsWhere;
	}

	void sum(String lsWhere) {
		String sql1 = " select " + " count(*) as tl_cnt , " + " sum(decode(adj_loc_flag,'1',1,0)) as tl_up_cnt , "
				+ " sum(decode(adj_loc_flag,'2',1,0)) as tl_down_cnt , "
				+ " sum(decode(card_adj_limit,0,0,1)) as tl_adj_cnt " + " from rsk_acnolog " + lsWhere;

		sqlSelect(sql1);

		wp.colSet("tl_cnt", "" + sqlNum("tl_cnt"));
		wp.colSet("tl_up_cnt", "" + sqlNum("tl_up_cnt"));
		wp.colSet("tl_down_cnt", "" + sqlNum("tl_down_cnt"));
		wp.colSet("tl_adj_cnt", "" + sqlNum("tl_adj_cnt"));

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " card_no , " + " uf_idno_name(id_p_seqno) as chi_name , "
				+ " uf_idno_id(id_p_seqno) as id_no , " + " bef_loc_amt , " + " aft_loc_amt , " + " bef_loc_cash , "
				+ " aft_loc_cash , " + " decode(adj_loc_flag,'1','高','2','低') as tt_adj_loc_flag , "
				+ " card_adj_limit , " + " card_adj_date1 , " + " card_adj_date2 , " + " mod_user , "
				+ " to_char(mod_time,'yyyymmdd') as mod_date , " + " to_char(mod_time,'hh24miss') as tt_mod_time , "
				+ " apr_user , " + " son_card_flag , " + " apr_date ," + " '' as tt_mod_branch , "
				+ " '' as tt_apr_branch , user_dept_no ";
		wp.daoTable = "rsk_acnolog";
		wp.whereOrder = " order by mod_time";
		pageQuery();

		if (this.sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}

		wp.setListCount(0);
		wp.setPageValue();
		queryAfter();
	}

	void queryAfter() {
		String sql1 = " select A.full_chi_name from gen_brn A left join sec_user B "
				+ " on A.branch = B.bank_unitno where B.usr_id = ? ";
		
		String sql2 = " select dept_name from ptr_dept_code where dept_code = ? ";
		
		String sql3 = " select acno_p_seqno from crd_card where card_no = ? ";
		String sql4 = " select line_of_credit_amt from act_acno where acno_p_seqno = ? ";
		String tempAcnoPSeqno = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "mod_user") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "tt_mod_branch", sqlStr("full_chi_name"));
			}

			sqlSelect(sql1, new Object[] { wp.colStr(ii, "apr_user") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "tt_apr_branch", sqlStr("full_chi_name"));
			}
			wp.colSet(ii, "wk_mod_time",
					commString.strToYmd(wp.colStr(ii, "mod_date")) + " " + commString.mid(wp.colStr(ii, "tt_mod_time"), 0, 2)
							+ ":" + commString.mid(wp.colStr(ii, "tt_mod_time"), 2, 2) + ":"
							+ commString.mid(wp.colStr(ii, "tt_mod_time"), 4, 2));
			wp.colSet(ii, "wk_card_adj_date", commString.strToYmd(wp.colStr(ii, "card_adj_date1")) + " -- "
					+ commString.strToYmd(wp.colStr(ii, "card_adj_date2")));

			wp.colSet(ii, "chi_name", commString.mid(wp.colStr(ii, "chi_name"), 0, 6));
			
			sqlSelect(sql2,new Object[] {wp.colStr(ii,"user_dept_no")});
			if(sqlRowNum >0) {
				wp.colSet(ii,"tt_user_dept_no", sqlStr("dept_name"));
			}		
			
			sqlSelect(sql3,new Object[] {wp.colStr(ii,"card_no")});
			if(sqlRowNum >0) {
				tempAcnoPSeqno = sqlStr("acno_p_seqno");
				sqlSelect(sql4,new Object[] {tempAcnoPSeqno});
				if(sqlRowNum >0) {
					wp.colSet(ii,"line_of_credit_amt", sqlStr("line_of_credit_amt"));
				}
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
	public void pdfPrint() throws Exception {
		wp.reportId = "rskq0922";
		String tmpStr = "";
		wp.colSet("user_id", wp.loginUser);
		tmpStr = "調整日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_date2"));
		wp.colSet("cond1", tmpStr);
		wp.pageRows = 9999;
		queryFunc();

//		ecsfunc.HiData oohh = new ecsfunc.HiData();
//		oohh.hhCardno("card_no");
//		oohh.hhIdno("id_no");
//		oohh.hhIdname("chi_name");
//		oohh.hidataWp(wp);

		TarokoPDFLine pdf = new TarokoPDFLine();
		// pdf.pageCount =30;

		wp.fileMode = "Y";
		pdf.excelTemplate = "rskq0922.xlsx";
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

}
