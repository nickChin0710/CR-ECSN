/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-26  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-07-02  V1.00.02  Andy		  update : Mantis3707                        *
* 109-07-13  V1.00.03  Andy		  update : Mantis3743                        *
* 109-07-17  V1.00.04  Andy		  update : Mantis3772                        *
******************************************************************************/
package bilr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDF2;

public class Bilr0070 extends BaseReport {

	InputStream inExcelFile = null;
	String m_progName = "bilr0070" ;

	String cond_where = "";
	String report_subtitle = "";
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;

		strAction = wp.buttonCode;
		log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			// is_action="new";
			// clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
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
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			strAction = "XLS";
			// wp.setExcelMode();
			xlsPrint();
		} else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
			strAction = "PDF";
			// wp.setExcelMode();
			pdfPrint();
		}

		dddwSelect();
		// init_button();
	}

	@Override
	public void clearFunc() throws Exception {
		wp.resetInputData();
		wp.resetOutputData();
	}

	private boolean getWhereStr() throws Exception {
		String ex_office = wp.itemStr("ex_office");
		String ex_user = wp.itemStr("ex_user");
		String exDateS = wp.itemStr("exDateS");
		String exDateE = wp.itemStr("exDateE");

		String ls_where = "where 1=1  ";		

		if (empty(ex_office) == false) {
			ls_where += " and a.office_m_code = :ex_office";
			setString("ex_office", ex_office);
		}
		
		if (empty(ex_user) == false) {
			ls_where += " and a.mod_user = :ex_user";
			setString("ex_user", ex_user);
		}

		if (empty(exDateS) == false){
			ls_where += " and to_char(a.mod_time,'yyyymmdd') >= :exDateS ";
			setString("exDateS", exDateS);
		}		
		
		if (empty(exDateE) == false){
			ls_where += " and to_char(a.mod_time,'yyyymmdd') <= :exDateE ";
			setString("exDateE", exDateE);
		}

		wp.whereStr = ls_where;
		setParameter();
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		if (getWhereStr() == false)
			return;
		// cond_where = wp.whereStr + "";
		// wp.whereStr =cond_where;

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	private void setParameter() throws Exception {

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (getWhereStr() == false)
			return;

		wp.selectSQL = ""
				+ "c.chi_name, "
				+ "uf_hi_cname(c.chi_name) db_cname , "//編碼:中文姓名
				+ "a.card_no, "
				+ "uf_hi_cardno (a.card_no) db_card_no, "//轉碼:卡號
				+ "a.office_m_code, "
				+ "d.office_m_name, "
				+ "(a.office_m_code||' '||d.office_m_name) as db_office_m_code, "
				+ "a.office_code, "
				+ "a.transaction_type, "
				+ "a.telephone_no, "
				+ "a.auth_batch_no, "
				+ "a.city_x160, "
				+ "b.id_p_seqno, "				
				+ "a.mod_time, "
				+ "a.mod_seqno, "
				+ "a.computer_no, "
				+ "1 wk_count,"
				+ "decode(transaction_type,'1',1,0) wk_count_1, "
				+ "decode(transaction_type,'2',1,0) wk_count_2, "
				+ "decode(transaction_type,'3',1,0) wk_count_3 ";

		wp.daoTable = " bil_chtmain   a left join crd_card b on a.card_no = b.card_no ";
		wp.daoTable += "                left join crd_idno c on b.id_p_seqno = c.id_p_seqno ";
		wp.daoTable += "                left join bil_office_m d on a.office_m_code = d.office_m_code ";
		wp.whereOrder = " order by a.office_m_code ";

		// setParameter();
		// System.out.println("select " + wp.selectSQL + " from " +wp.daoTable+wp.whereStr);
		// wp.daoTable);

		if (strAction.equals("XLS")) {
			selectNoLimit();
		}
		pageQuery();
		// list_wkdata();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr("此條件查無資料");
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
		wp.setPageValue();
		list_wkdata();
	}
	void list_wkdata() throws Exception {
		int row_ct = 0;
		int sel_ct = wp.selectCnt;
		String ls_city ="",db_city="";
		for (int ii = 0; ii < sel_ct; ii++) {
			//計算欄位
			row_ct += 1;
			String ss4=wp.colStr(ii,"transaction_type");
			String[] cde4=new String[]{"1","2","3","4"};
			String[] txt4=new String[]{"1:新增","2:修改","3:終止","4:刪除"};
			wp.colSet(ii,"transaction_type", commString.decode(ss4, cde4, txt4));
			//wp.colSet(ii,"row_ct", int_2Str(row_ct));
			//20190524 Andy 增加縣市別
			String ls_sql = "";
			ls_city = wp.colStr(ii,"city_x160");
			String[] split_line = ls_city.split(",");
			db_city="";
			for(int jj=0; jj < split_line.length; jj++){
//				String[] cde=new String[]{"0000000","10047779","10927578","21101573","26645553","36724726"};
//				String[] txt=new String[]{"高雄市統編,","新北市統編,","台中停管統編,","台北市統編,","桃園市統編,","台南市統編,"};
//				db_city += zzstr.decode(split_line[jj], cde, txt);		
				ls_sql = " select wf_desc "
						+ "from ptr_sys_idtab "
						+ "where 1=1  "
						+ "and wf_type= 'PARK_CITY' "
						+ "and wf_id =:wf_id ";
				setString("wf_id",split_line[jj]);
				sqlSelect(ls_sql);
				db_city += sqlStr("wf_desc")+",";				
			}
			db_city = strMid(db_city,0,db_city.length()-1);
			wp.colSet(ii,"db_city",db_city);
		}
		wp.colSet("row_ct", intToStr(row_ct));
	}
	void subTitle() throws Exception{
		String ex_office = wp.itemStr("ex_office");
		String ex_user = wp.itemStr("ex_user");
		String exDateS = wp.itemStr("exDateS");
		String exDateE = wp.itemStr("exDateE");
		String ss ="";
		String ls_sql="";
		if(!empty(ex_office)){
			ls_sql = "select office_m_name from bil_office_m where office_m_code =:office_m_code ";
			setString("office_m_code",ex_office);
			sqlSelect(ls_sql);			
			ss += " 機構代號 : "+ex_office+" "+sqlStr("office_m_name");
		}
		if(!empty(ex_user)){
			ss += " 登錄人員 : "+ex_user;
		}		
		if(empty(exDateS)==false || empty(exDateE)==false){
			ss += " 登錄日期 : ";
			if(empty(exDateS)==false){
				ss += exDateS;				
			}
			if(empty(exDateE)==false){
				ss += " ~ "+exDateE;				
			}
		}		
		report_subtitle =ss;
	}
	void xlsPrint() {
		try {
			log("xlsFunction: started--------");
			wp.reportId = m_progName;
			// -cond-
			String ex_office = wp.itemStr("ex_office");
			String ex_user = wp.itemStr("ex_user");
			String exDateS = wp.itemStr("exDateS");
			String exDateE = wp.itemStr("exDateE");

			String ss = "機構代號: " + ex_office + " 登錄人員: " + ex_user + " 登錄日期:" + exDateS +" ~ "+exDateE; 			 	  	
			wp.colSet("cond_1", ss);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
			xlsx.excelTemplate = m_progName + ".xlsx";

			//====================================
			//-明細-
			xlsx.sheetName[0] ="明細";
			queryFunc();
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
     		xlsx.processExcelSheet(wp);
			/*
			//-合計-
			xlsx.sheetName[1] ="合計";
			query_Summary(cond_where);
			wp.listCount[1] =sqlRowNum;
			log("Summ: rowcnt:" + wp.listCount[1]);
			//xlsx.sheetNo = 1;
			xlsx.processExcelSheet(wp);
			*/
			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");

		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}

	void pdfPrint() throws Exception {
		wp.reportId = m_progName;
		// -cond-
		subTitle();
		wp.colSet("cond_1", report_subtitle);
		wp.pageRows = 9999;

		queryFunc();
		// wp.setListCount(1);
		wp.colSet("user_id", wp.loginUser);
		TarokoPDF pdf = new TarokoPDF();
		//表頭固定欄位
//		pdf.fixHeader[0] ="user_id";
//		pdf.fixHeader[1]="cond_1";
		
		wp.fileMode = "Y";
		pdf.excelTemplate = m_progName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 28;
		//pdf.pageVert= true;				//直印
		pdf.procesPDFreport(wp);		
		pdf = null;

		
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dddwSelect() {
		try {
			// dddw_office_m_code
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_office");
			dddwList("dddw_office_m_code", "bil_office_m", "office_m_code", "office_m_name", "where 1=1 group by office_m_code,office_m_name order by office_m_code");

		} catch (Exception ex) {
		}
	}

}

