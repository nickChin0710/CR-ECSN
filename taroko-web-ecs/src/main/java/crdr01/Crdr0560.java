/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE            Version     AUTHOR        DESCRIPTION                      *
* ---------       --------      ----------         --------------------------*
* 112-04-26  V1.00.00   Ryan        program initial                     *
* 112-06-05  V1.00.01   Ryan        新增excel                                    
******************************************************************************/
package crdr01;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0560 extends BaseReport {
	private final static String PROG_NAME = "crdr0560";
	private final static String PROG_NAME_EXCEL = "crdr0560_excel";
	String reportSubtitle = "";
	int type1Cnt = 0;
	int type2Cnt = 0;
	int type3Cnt = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;

		strAction = wp.buttonCode;
		log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
		switch (wp.buttonCode) {
		case "X":
			/* 轉換顯示畫面 */
			// is_action="new";
			// clearFunc();
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
		case "S":
			/* 動態查詢 */
			querySelect();
			break;
		case "L":
			/* 清畫面 */
			strAction = "";
			clearFunc();
			break;
		case "XLS":
			// -PDF-
			strAction = "XLS";
			xlsPrint();
			break;
		case "PDF":
			// -PDF-
			strAction = "PDF";
			// wp.setExcelMode();
			pdfPrint();
			break;
		default:
			break;
		}

		// dddw_select();
		// init_button();
	}

	@Override
	public void clearFunc() throws Exception {
		wp.resetInputData();
		wp.resetOutputData();
	}

	@Override
	public void initPage() {

	}

	private String getWhereStr() throws Exception {
		String exReturnDate1 = wp.itemStr("ex_return_date_1");
		StringBuffer buf = new StringBuffer();
		return buf.append("(").append(getType1Sql(exReturnDate1)).append(" UNION ")
				.append(getType2Sql(exReturnDate1)).append(" UNION ")
				.append(getType3Sql(exReturnDate1)).append(")").toString();
	}

	@Override
	public void queryFunc() throws Exception {
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		String sqlCmd = getWhereStr();

		wp.selectSQL = " * ";
		wp.daoTable = sqlCmd;
		wp.whereOrder = "  ORDER BY MAIL_NO  ";

		pageQuery();
		// list_wkdata();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
	}

	String getType1Sql(String exReturnDate1) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_NO, ").append("A.GROUP_CODE, ").append("A.CARD_NO, ").append("B.CHI_NAME, ")
				.append("A.ZIP_CODE, ")
				.append("(A.MAIL_ADDR1 || A.MAIL_ADDR2 || A.MAIL_ADDR3 || A.MAIL_ADDR4 || A.MAIL_ADDR5) AS MAIL_ADDR, ")
				.append("C.ENV_WEIGHT, ").append("C.CARD_WEIGHT, ").append("C.BIGTAIPEI_AREA_POST, ")
				.append("C.OTHER_AREA_POST ").append("FROM CRD_RETURN A,CRD_IDNO B, CRD_POSTAGE C ")
				.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ").append("AND A.MAIL_TYPE = C.MAIL_TYPE ")
				.append("AND A.MAIL_TYPE IN ('1','2') ");
		if (!empty(exReturnDate1)) {
			sbf.append("AND A.MAIL_DATE = '").append(exReturnDate1).append("'");
		}
		return sbf.toString();
	}

	String getType2Sql(String exReturnDate1) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_NO, ").append("A.GROUP_CODE, ").append("A.CARD_NO, ").append("B.CHI_NAME, ")
				.append("A.ZIP_CODE, ")
				.append("(A.MAIL_ADDR1 || A.MAIL_ADDR2 || A.MAIL_ADDR3 || A.MAIL_ADDR4 || A.MAIL_ADDR5) AS MAIL_ADDR, ")
				.append("C.ENV_WEIGHT, ").append("C.CARD_WEIGHT, ").append("C.BIGTAIPEI_AREA_POST, ")
				.append("C.OTHER_AREA_POST ").append("FROM DBC_RETURN A,DBC_IDNO B, CRD_POSTAGE C ")
				.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ").append("AND A.MAIL_TYPE = C.MAIL_TYPE ")
				.append("AND A.MAIL_TYPE IN ('1','2') ");
		if (!empty(exReturnDate1)) {
			sbf.append("AND A.MAIL_DATE >= '").append(exReturnDate1).append("'");
		}
		return sbf.toString();
	}

	String getType3Sql(String exReturnDate1) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_NO, ").append("A.GROUP_CODE, ").append("A.PP_CARD_NO AS CARD_NO, ")
				.append("B.CHI_NAME, ").append("A.ZIP_CODE, ")
				.append("(A.MAIL_ADDR1 || A.MAIL_ADDR2 || A.MAIL_ADDR3 || A.MAIL_ADDR4 || A.MAIL_ADDR5) AS MAIL_ADDR, ")
				.append("C.ENV_WEIGHT, ").append("C.CARD_WEIGHT, ").append("C.BIGTAIPEI_AREA_POST, ")
				.append("C.OTHER_AREA_POST ").append("FROM CRD_RETURN_PP A,CRD_IDNO B, CRD_POSTAGE C ")
				.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ").append("AND A.MAIL_TYPE = C.MAIL_TYPE ")
				.append("AND A.MAIL_TYPE IN ('1','2') ");
		if (!empty(exReturnDate1)) {
			sbf.append("AND A.MAIL_DATE >= '").append(exReturnDate1).append("'");
		}
		return sbf.toString();
	}

	void listWkdata() throws Exception {
		int sumAreaPost = 0;
		for (int i = 0; i < wp.selectCnt; i++) {
			int envWeight = wp.colInt(i, "ENV_WEIGHT");
			int cardWeight = wp.colInt(i, "CARD_WEIGHT");
			wp.colSet(i, "ENV_CARD_WEIGHT", envWeight + cardWeight);

			String mailAddr1 = wp.colStr(i, "MAIL_ADDR1");
			int areaPost = commString.pos(",基隆市,臺北市,新北市", mailAddr1) > 0 ? wp.colInt(i, "BIGTAIPEI_AREA_POST") : wp.colInt(i, "OTHER_AREA_POST");
			wp.colSet("AREA_POST", areaPost);
			sumAreaPost += areaPost;
		}
		wp.colSet("SUM_AREA_POST", sumAreaPost);
	}

	void pdfPrint() throws Exception {
		String exReturnDate1 = wp.itemStr("ex_return_date_1");
		wp.reportId = PROG_NAME;
		// -cond-
		if(!empty(exReturnDate1))
			wp.colSet("cond1", forMatTWDate(exReturnDate1));
		// ===========================
		wp.pageRows = 99999;
		queryFunc();
		// wp.setListCount(1);
		wp.colSet("user_id", wp.loginUser);
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = PROG_NAME + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 48;
		pdf.pageVert = true; // 直印
		pdf.procesPDFreport(wp);

		pdf = null;
	}
	
	 void xlsPrint() {
		    try {
		      log("xlsFunction: started--------");
		      String exReturnDate1 = wp.itemStr("ex_return_date_1");
		      wp.reportId = PROG_NAME_EXCEL;
		      // -cond-
		      if(!empty(exReturnDate1))
				wp.colSet("cond1", forMatTWDate(exReturnDate1));

		      // ===================================
		      TarokoExcel xlsx = new TarokoExcel();
		      wp.fileMode = "Y";
		      xlsx.excelTemplate = PROG_NAME_EXCEL + ".xlsx";

		      // ====================================
		      // -明細-
		      xlsx.sheetName[0] = "明細";
		      queryFunc();
		      wp.setListCount(1);
		      log("Detl: rowcnt:" + wp.listCount[0]);
		      xlsx.processExcelSheet(wp);
		      /*
		       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
		       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
		       */
		      xlsx.outputExcel();
		      xlsx = null;
		      log("xlsFunction: ended-------------");

		    } catch (Exception ex) {
		      wp.expMethod = "xlsPrint";
		      wp.expHandle(ex);
		    }
		  }
	
	private String forMatTWDate(String date ) {
		SimpleDateFormat df = new SimpleDateFormat("yyyymmdd");
		SimpleDateFormat dftw = new SimpleDateFormat("yyy/mm/dd");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(date));
			cal.add(Calendar.YEAR, -1911);
		} catch (ParseException e) {
			return "";
		}
		return dftw.format(cal.getTime());
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

}
