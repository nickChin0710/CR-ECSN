/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-05  V1.00.00  JustinWu    program initial                             *
* 109-05-22  V1.00.01  JustinWu    change coding standard                      *
* 109-06-29  V1.00.02  Zuwei        fix code scan issue                      *
* 109-08-07  V1.00.03  Sunny        add cmsr4100b order by                      *
* 109-09-09  V1.00.04  JustinWu    where條件mod_pgm轉大寫與資料庫mod_pgm轉大寫比較
******************************************************************************/

package cmsr02;

import java.util.Locale;

import ofcapp.BaseAction;
import taroko.base.CommDate;
import taroko.com.TarokoPDF;

public class Cmsr4100 extends BaseAction {
	String progName = "cmsr4100";
	
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
		case "M":
			/* 瀏覽功能 :skip-page */
			queryRead();
			break;
		case "L":
			/* 清畫面 */
			strAction = "";
			clearFunc();
			break;
		case "PDF":
			// -PDF-
			pdfPrint();
			break;
		default:
			break;
		}

	}

	@Override
	public void queryFunc() throws Exception {

				wp.setQueryMode();
		   
				queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		String where1;
		wp.pageControl();
		
		wp.selectSQL = 
				           " ccl.ID_P_SEQNO, "
				        + " decode(DEBIT_FLAG, 'Y', di.ID_NO, ci.ID_NO) AS ID_NO, "
						+ " ccl.ACCT_TYPE, "
						+ " ccl.P_SEQNO, "
						+ " ccl.CARD_NO, "
						+ " ccl.DEBIT_FLAG, "
						+ " ccl.CHG_TABLE, "
						+ " ccl.CHG_COLUMN, "
						+ " ccl.CHG_USER, "
						+ " ccl.CHG_DATE, "
						+ " ccl.CHG_TIME, "
						+ " ccl.CHG_DATA_OLD, "
						+ " ccl.CHG_DATA, "
						+ " ccl.MOD_TIME, "
						+ " ccl.MOD_PGM, "
						+ " decode(psi1.WF_DESC, NULL, ccl.CHG_TABLE, psi1.WF_DESC) as CHG_TABLE_DESC, "
						+ " decode(psi2.WF_DESC, NULL, ccl.CHG_COLUMN, psi2.WF_DESC) as CHG_COLUMN_DESC "
						;
		
		wp.daoTable = " CMS_CHGCOLUMN_LOG as ccl "
				+ " LEFT JOIN crd_idno ci ON ccl.ID_P_SEQNO = ci.ID_P_SEQNO "
				+ " LEFT JOIN dbc_idno di ON ccl.ID_P_SEQNO = di.ID_P_SEQNO "
				+ " LEFT JOIN ptr_sys_idtab psi1 ON lower(ccl.CHG_TABLE) = lower(psi1.WF_ID) AND lower(psi1.wf_type) ='cmsr4100a' "
				+ " LEFT JOIN ptr_sys_idtab psi2 ON lower(ccl.CHG_COLUMN) = lower(psi2.WF_ID) AND lower(psi2.wf_type) ='cmsr4100b' "
				;
		
		if ( ! isFromDataValid())
			return;
		
		where1 = getWhereString();

		wp.queryWhere = where1;
		wp.whereStr = " where " + wp.queryWhere;
		wp.whereOrder = " order by ccl.MOD_TIME ";
		
		logSql();
		pageQuery();
		
		wp.setListCount(1);
		if (sqlRowNum<= 0) {
			alertErr("此條件查無資料");
			return;
		}

		wp.setPageValue();

	}
	
	/**
	 * validate whether all form data is valid
	 * @return
	 */
	private boolean isFromDataValid() {
		if ( ! isDateValid()) 
			return false;
		
		return true;
	}

	/**
	 * validate if date form data is valid
	 * @return
	 */
	private boolean isDateValid() {
		CommDate dateValidator = new CommDate(); 
		
		String chgTableDateStart = wp.itemStr("ex_chg_table_date_start"),
				     chgTableDateEnd = wp.itemStr("ex_chg_table_date_end");
		
		if (chgTableDateStart == null || chgTableDateStart.trim().length() == 0) {
			alertErr("異動日期起日為必輸欄位");
			return false;
		}
		if (chgTableDateEnd == null || chgTableDateEnd.trim().length() == 0) {
			alertErr("異動日期迄日為必輸欄位");
			return false;
		}
		if ( !dateValidator.isDate(chgTableDateStart)) {
			alertErr("異動日期起日需為日期格式");
			return false;
		}
		if ( !dateValidator.isDate(chgTableDateEnd)) {
			alertErr("異動日期迄日需為日期格式");
			return false;
		}
		if (chgTableDateStart.compareTo(chgTableDateEnd) > 0) {
			alertErr("異動日期起日需小於等於迄日");
			return false;
		}
		
		return true;
	}

	/**
	 * get where script 
	 * @return
	 */
	private String getWhereString() {
		String where1;
		
		int i = 1;
		
		where1 = " 1=1"
				+ " AND ? <= ccl.CHG_DATE "
				+ " AND ccl.CHG_DATE <= ? ";
		setString( i++, wp.itemStr("ex_chg_table_date_start"));
		setString( i++, wp.itemStr("ex_chg_table_date_end"));
		
		
		if ( !wp.itemEmpty("ex_id_no")) {
			where1 += " AND ( "
			           + " (upper(ccl.DEBIT_FLAG)='Y' AND di.ID_NO = ? ) "
			           + " OR (upper(ccl.DEBIT_FLAG)<>'Y' AND ci.ID_NO  = ? )"
			           + "  ) ";
			setString( i++, wp.itemStr("ex_id_no"));
			setString( i++, wp.itemStr("ex_id_no"));
		}	
		
		if ( !wp.itemEmpty("ex_debit_flag")) {
			where1 += " AND decode(upper(ccl.DEBIT_FLAG),'Y','Y','N') = upper(?) ";
			setString( i++, wp.itemStr("ex_debit_flag"));
		}	
		
		if ( !wp.itemEmpty("ex_chg_table")) {
			where1 += " AND lower(ccl.CHG_TABLE) = lower(?) ";
			setString( i++, wp.itemStr("ex_chg_table"));
		}	
		
		if ( !wp.itemEmpty("ex_chg_column")) {
			where1 += " AND lower(ccl.CHG_COLUMN) = lower(?) ";
			setString( i++, wp.itemStr("ex_chg_column"));
		}	
		
		if ( !wp.itemEmpty("ex_mod_pgm")) {
			where1 += " AND upper(ccl.MOD_PGM) = upper(?) ";
			setString( i++, wp.itemStr("ex_mod_pgm"));
		}
		return where1;
	}

	private void pdfPrint() throws Exception {
		wp.reportId = progName;
		// -cond-
		String subTitle = getSubTitle();
		wp.colSet("cond_1", subTitle);
		//===========================
		wp.pageRows = 99999;		
		queryFunc();
	
		// wp.setListCount(1);
		
		wp.colSet("row_ct", wp.selectCnt);
		wp.colSet("user_id", wp.loginUser);
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = progName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 28;
//		pdf.pageVert= true;				//直印
		pdf.procesPDFreport(wp);
	
		pdf = null;
	}

	private String getSubTitle(){
		String exChgTableDateStart = wp.itemStr("ex_chg_table_date_start");
		String exChgTableDateEnd = wp.itemStr("ex_chg_table_date_end");
		String exIdNo = wp.itemStr("ex_id_no");
		String exDebitFlag = wp.itemStr("ex_debit_flag");
		String exChgTable = wp.itemStr("ex_chg_table");	
		String exChgColumn = wp.itemStr("ex_chg_column");
		String exModPgm = wp.itemStr("ex_mod_pgm");
		StringBuilder subTitle = new StringBuilder();
		
		//主檔修改日期
		subTitle.append("主檔修改日期: ")
		.append(exChgTableDateStart).append("起 ~").append(exChgTableDateEnd).append(" 迄");
		
		//身分證字號
		if (notEmpty(exIdNo)) {
			subTitle.append(" 身分證字號: ").append(exIdNo);
		}
		
		//卡片類別
		subTitle.append(" 卡片類別: ");
		switch (exDebitFlag.toUpperCase(Locale.TAIWAN)) {
		case "Y":
			subTitle.append("VD卡");
			break;
		case "N":
			subTitle.append("信用卡");
			break;
		default:
			subTitle.append("全部");
			break;
		}
		
		// 異動主檔
		if (notEmpty(exChgTable)) {
			subTitle.append(" 異動主檔: ").append(exChgTable);
		}
				
		// 異動欄位
		if (notEmpty(exChgColumn)) {
			subTitle.append(" 異動欄位: ").append(exChgColumn);
		}
		
		// 異動程式
		if (notEmpty(exModPgm)) {
			subTitle.append(" 異動程式: ").append(exModPgm);
		}
		
		return subTitle.toString();		
	}

	/**
	 * 
	 * @param initOpt: 第一個option的value
	 * @param htmlColName: html上的欄位名
	 * @param tableName: 資料表名
	 * @param selectedCol: 選取的欄位
	 * @param colDesc: 欄位解說
	 * @param whereCondition 查詢資料表時的where條件
	 */
	private void produceDropDownList(String initOpt, String htmlColName, String tableName, String selectedCol,
			String colDesc, String whereCondition) {
		wp.initOption = initOpt;
		if( !wp.itemEmpty("ex_" +htmlColName + "_text")) {
			wp.optionKey = wp.itemStr("ex_" + htmlColName + "_text");
		}else {
			wp.optionKey = "";
		}
		try {
			this.dropdownList("dddw_" + htmlColName, tableName, selectedCol, colDesc, whereCondition);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void querySelect() throws Exception {
		
	}

	@Override
	public void dataRead() throws Exception {
		
		
	}

	@Override
	public void saveFunc() throws Exception {
	
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void initButton() {
	
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void dddwSelect() {
		produceDropDownList("--", "chg_table", "ptr_sys_idtab", "lower(wf_id)", "wf_desc",
				"where lower(wf_type) ='cmsr4100a'");
	
		produceDropDownList("--", "chg_column", "ptr_sys_idtab", "lower(wf_id)", "wf_desc",
				"where lower(wf_type) ='cmsr4100b' order by wf_desc");
		
	}

}
