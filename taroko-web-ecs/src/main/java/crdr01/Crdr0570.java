/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE            Version     AUTHOR        DESCRIPTION                      *
* ---------       --------      ----------         --------------------------*
* 112-04-27  V1.00.00   Ryan        program initial                          *
* 112-05-30  V1.00.01   Ryan        郵資判斷增加台北市                                                                                *
* 112-06-05  V1.00.02   Ryan        新增excel                                                                                *
******************************************************************************/
package crdr01;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Crdr0570 extends BaseReport {
	private final static String PROG_NAME = "crdr0570";
	private final static String PROG_NAME_EXCEL = "crdr0570_excel";
	String reportSubtitle = "";
	int type1Cnt = 0;
	int type2Cnt = 0;
	int type3Cnt = 0;
	LinkedHashMap<String,HashMap<String,String>> cardCntMap = new LinkedHashMap<String,HashMap<String,String>>();
	LinkedHashMap<String,HashMap<String,String>> postageMap = new LinkedHashMap<String,HashMap<String,String>>();
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

		 dddwSelect();
		 initButton();
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
		String exMailBranch = wp.itemStr("ex_mail_branch");
		StringBuffer buf = new StringBuffer();
		return buf.append("(").append(getType1Sql(exReturnDate1,exMailBranch)).append(" UNION ")
				.append(getType2Sql(exReturnDate1,exMailBranch)).append(" UNION ")
				.append(getType3Sql(exReturnDate1,exMailBranch)).append(")").toString();
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

		wp.selectSQL = " * ,(select branch||'_'||brief_chi_name from gen_brn where branch = mail_branch) as branch_chi_name ";
		wp.daoTable = sqlCmd;
		wp.whereOrder = " ORDER BY MAIL_NO ";

		pageQuery();
		// list_wkdata();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata(wp.selectCnt);
	}

	String getType1Sql(String exReturnDate1,String exMailBranch) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_NO, ").append("C.ZIP_CODE, ").append("A.MAIL_BRANCH, ").append("C.FULL_CHI_NAME, ")
				.append("A.GROUP_CODE, ").append("A.CARD_NO, ").append("B.CHI_NAME, ").append("C.CHI_ADDR_1 ")
				.append("FROM CRD_RETURN A,CRD_IDNO B, GEN_BRN C ")
				.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ").append("AND A.MAIL_BRANCH = C.BRANCH ")
				.append("AND A.MAIL_TYPE = '4' ");
		if (!empty(exReturnDate1)) {
			sbf.append("AND A.MAIL_DATE = '").append(exReturnDate1).append("'");
		}
		if (!empty(exMailBranch)) {
			sbf.append("AND A.MAIL_BRANCH = '").append(exMailBranch).append("'");
		}
		return sbf.toString();
	}

	String getType2Sql(String exReturnDate1,String exMailBranch) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_NO, ").append("C.ZIP_CODE, ").append("A.MAIL_BRANCH, ").append("C.FULL_CHI_NAME, ")
				.append("A.GROUP_CODE, ").append("A.CARD_NO, ").append("B.CHI_NAME, ").append("C.CHI_ADDR_1 ")
				.append("FROM DBC_RETURN A,DBC_IDNO B, GEN_BRN C ")
				.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ").append("AND A.MAIL_BRANCH = C.BRANCH ")
				.append("AND A.MAIL_TYPE = '4' ");
		if (!empty(exReturnDate1)) {
			sbf.append("AND A.MAIL_DATE >= '").append(exReturnDate1).append("'");
		}
		if (!empty(exMailBranch)) {
			sbf.append("AND A.MAIL_BRANCH = '").append(exMailBranch).append("'");
		}
		return sbf.toString();
	}

	String getType3Sql(String exReturnDate1,String exMailBranch) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_NO, ").append("C.ZIP_CODE, ").append("A.MAIL_BRANCH, ").append("C.FULL_CHI_NAME, ")
				.append("A.GROUP_CODE, ").append("A.PP_CARD_NO AS CARD_NO, ").append("B.CHI_NAME, ").append("C.CHI_ADDR_1 ")
				.append("FROM CRD_RETURN_PP A,CRD_IDNO B, GEN_BRN C ")
				.append("WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ").append("AND A.MAIL_BRANCH = C.BRANCH ")
				.append("AND A.MAIL_TYPE = '4' ");
		if (!empty(exReturnDate1)) {
			sbf.append("AND A.MAIL_DATE >= '").append(exReturnDate1).append("'");
		}
		if (!empty(exMailBranch)) {
			sbf.append("AND A.MAIL_BRANCH = '").append(exMailBranch).append("'");
		}
		return sbf.toString();
	}

	void listWkdata(int selectCnt) throws Exception {
		HashMap<String,String> map = null;
		int cardCnt = 0;
		for (int i = 0; i < selectCnt; i++) {
			map = new HashMap<String,String>();
			String mailNo = wp.colStr(i,"MAIL_NO");
			cardCnt = cardCntMap.get(mailNo) == null ? 0 :cardCnt;
			cardCnt++;
			map.put("CARD_CNT", this.intToStr(cardCnt));
			map.put("CHI_NAME", wp.colStr(i, "CHI_NAME"));
			map.put("FULL_CHI_NAME", String.format("(%s)%s%s", wp.colStr(i, "ZIP_CODE") ,wp.colStr(i,"MAIL_BRANCH"), wp.colStr(i, "FULL_CHI_NAME")));
			map.put("CHI_ADDR_1", wp.colStr(i, "CHI_ADDR_1"));
			cardCntMap.put(mailNo,map);
		}
		
		for(String mailNo :cardCntMap.keySet()) {
			map = new HashMap<String,String>();
			String cnt = cardCntMap.get(mailNo).get("CARD_CNT").toString();
			String chiAddr1 = cardCntMap.get(mailNo).get("CHI_ADDR_1").toString();
			selectCrdPostage(cnt);
			int envWeight = sqlInt("ENV_WEIGHT");
			int cardWeight = sqlInt("CARD_WEIGHT");
			int bigtaipeiAreaPost = sqlInt("BIGTAIPEI_AREA_POST");
			int otherAreaPost = sqlInt("OTHER_AREA_POST");
			
			map.put("WEIGHT", String.valueOf((envWeight + cardWeight) * toInt(cnt)));
			map.put("AREA_POST", String.valueOf(commString.pos(",基隆市,臺北市,新北市,台北市", chiAddr1) > 0 ? bigtaipeiAreaPost : otherAreaPost));
			postageMap.put(mailNo,map);

		}

		for (int i = 0; i < selectCnt; i++) {
			String mailNo = wp.colStr(i,"MAIL_NO");
			wp.colSet(i,"WEIGHT",postageMap.get(mailNo).get("WEIGHT").toString());
			wp.colSet(i,"AREA_POST",postageMap.get(mailNo).get("AREA_POST").toString());
			
		}
	}
	
	void listWkdataPdf() {
		int i = 0;
		int sumAreaPost = 0;
		for(String mailNo :cardCntMap.keySet()) {
			wp.colSet(i,"PDF_MAIL_NO", mailNo);
			wp.colSet(i,"PDF_CHI_NAME", cardCntMap.get(mailNo).get("CHI_NAME").toString());
			wp.colSet(i,"PDF_FULL_CHI_NAME", cardCntMap.get(mailNo).get("FULL_CHI_NAME").toString());
			wp.colSet(i,"PDF_WEIGHT", postageMap.get(mailNo).get("WEIGHT").toString());
			wp.colSet(i,"PDF_AREA_POST", postageMap.get(mailNo).get("AREA_POST").toString());
			sumAreaPost += toInt(postageMap.get(mailNo).get("AREA_POST").toString());
			i++;
		}
		wp.listCount[0] = i;
		wp.colSet("sum_area_post", sumAreaPost);
		wp.colSet("sum_rowcount", wp.listCount[0]);
	}
	
	void selectCrdPostage(String cardCnt) {
		String sqlCmd = "SELECT ENV_WEIGHT,CARD_WEIGHT,BIGTAIPEI_AREA_POST,OTHER_AREA_POST ";
		sqlCmd += "FROM CRD_POSTAGE WHERE MAIL_TYPE = '4' AND MIN_CARD_SHEETS < :MIN_CARD_SHEETS AND MAX_CARD_SHEETS > :MAX_CARD_SHEETS ";
		setString("MIN_CARD_SHEETS",cardCnt);
		setString("MAX_CARD_SHEETS",cardCnt);
		sqlSelect(sqlCmd);
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
		listWkdataPdf();
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
		      listWkdataPdf();
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
		SimpleDateFormat dftw = new SimpleDateFormat("yyyy年mm月dd日");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(date));
		} catch (ParseException e) {
			return "";
		}
		return dftw.format(cal.getTime());
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void dddwSelect() {
		try {
		      wp.initOption = "--";
		      wp.optionKey = wp.colStr("ex_mail_branch");
		      this.dddwList("dddw_mail_branch", "gen_brn", "branch", "brief_chi_name",
		          "where 1=1  order by branch");
		      
		} catch (Exception e) {
		}

	}

}
