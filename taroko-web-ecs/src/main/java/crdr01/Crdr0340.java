/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE            Version     AUTHOR        DESCRIPTION                      *
* ---------       --------      ----------         --------------------------*
* 112-05-02  V1.00.00   Ryan        program initial                          *
* 112-05-30  V1.00.01   Ryan        修正PDF BUG                               *
******************************************************************************/
package crdr01;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Crdr0340 extends BaseReport {
	private final static String PROG_NAME = "crdr0340";
	private final static String EMPLOYEE_CODE = "0560(2F)";
	private final static String NOTEMPLOYEE_CODE = "0560(1F)";
	private final static String LINE_SEPERATOR = "|";
	String reportSubtitle = "";
	int type1Cnt = 0;
	int type2Cnt = 0;
	int type3Cnt = 0;
	HashMap<String,HashMap> employeeMap = new HashMap<String,HashMap>();
	HashMap<String,HashMap> notEmployeeMap = new HashMap<String,HashMap>();
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
		String exToNcccDate1 = wp.itemStr("ex_to_nccc_date1");
		String exToNcccDate2 = wp.itemStr("ex_to_nccc_date2");
		StringBuffer buf = new StringBuffer();
		return buf.append("(").append(getType1Sql(exToNcccDate1,exToNcccDate2)).append(" UNION ")
				.append(getType2Sql(exToNcccDate1,exToNcccDate2)).append(" UNION ")
				.append(getType3Sql(exToNcccDate1,exToNcccDate2)).append(")").toString();
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
		wp.whereOrder = " ORDER BY MAIL_BRANCH,ACCOUNTING_NO,EMBOSS_DATE,TYPE ";

		pageQuery();
		// list_wkdata();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
	}

	String getType1Sql(String exToNcccDate1,String exToNcccDate2) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_BRANCH, ")
				.append("D.DEP_CHI_NAME, ")
				.append("A.TO_NCCC_DATE AS EMBOSS_DATE, ")
				.append("CASE WHEN A.CORP_NO = '' THEN ")
				.append("DECODE(A.COMBO_INDICATOR,'Y','C','GN') ELSE 'B' END AS TYPE, ")
				.append("A.GROUP_CODE, ")
				.append("D.CHI_NAME, ")
				.append("A.CARD_NO, ")
				.append("D.ACCOUNTING_NO ")
				.append("FROM CRD_EMBOSS A LEFT JOIN ")
				.append("(SELECT B.ID,C.DEP_CHI_NAME,B.CHI_NAME,B.ACCOUNTING_NO ")
				.append("FROM CRD_EMPLOYEE B, PTR_ACCOUNTING_NO C ")
				.append("WHERE B.ACCOUNTING_NO = C.ACCOUNTING_NO) D ")
				.append("ON A.APPLY_ID = D.ID ")
				.append("WHERE A.MAIL_BRANCH IN ('0010','0030','0560','3144') ");
		if (!empty(exToNcccDate1)) {
			sbf.append("AND A.TO_NCCC_DATE >= '").append(exToNcccDate1).append("'");
		}
		if (!empty(exToNcccDate2)) {
			sbf.append("AND A.TO_NCCC_DATE <= '").append(exToNcccDate2).append("'");
		}
		return sbf.toString();
	}

	String getType2Sql(String exToNcccDate1,String exToNcccDate2) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_BRANCH, ")
				.append("D.DEP_CHI_NAME, ")
				.append("A.TO_NCCC_DATE AS EMBOSS_DATE, ")
				.append("DECODE(A.APPLY_SOURCE,'P','VP','V') AS TYPE, ")
				.append("A.GROUP_CODE, ")
				.append("D.CHI_NAME, ")
				.append("A.CARD_NO, ")
				.append("D.ACCOUNTING_NO ")
				.append("FROM DBC_EMBOSS A LEFT JOIN  ")
				.append("(SELECT B.ID, ")
				.append("C.DEP_CHI_NAME, ")
				.append("B.CHI_NAME, ")
				.append("B.ACCOUNTING_NO ")
				.append("FROM CRD_EMPLOYEE B, PTR_ACCOUNTING_NO C ")
				.append("WHERE B.ACCOUNTING_NO = C.ACCOUNTING_NO) D ")
				.append("ON A.APPLY_ID = D.ID ")
				.append("WHERE A.MAIL_BRANCH IN ('0010','0030','0560','3144') ");
		if (!empty(exToNcccDate1)) {
			sbf.append("AND A.TO_NCCC_DATE >= '").append(exToNcccDate1).append("'");
		}
		if (!empty(exToNcccDate2)) {
			sbf.append("AND A.TO_NCCC_DATE <= '").append(exToNcccDate2).append("'");
		}
		return sbf.toString();
	}

	String getType3Sql(String exToNcccDate1,String exToNcccDate2) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT A.MAIL_BRANCH, ")
				.append("D.DEP_CHI_NAME, ")
				.append("A.IN_MAIN_DATE AS EMBOSS_DATE, ")
				.append("DECODE(A.VIP_KIND,'1','PP','PPW') AS TYPE, ")
				.append("A.GROUP_CODE, ")
				.append("D.CHI_NAME, ")
				.append("A.PP_CARD_NO AS CARD_NO, ")
				.append("D.ACCOUNTING_NO ")
				.append("FROM CRD_EMBOSS_PP A LEFT JOIN ")
				.append("(SELECT B.ID, ")
				.append("C.DEP_CHI_NAME, ")
				.append("B.CHI_NAME, ")
				.append("B.ACCOUNTING_NO ")
				.append("FROM CRD_EMPLOYEE B, PTR_ACCOUNTING_NO C ")
				.append("WHERE B.ACCOUNTING_NO = C.ACCOUNTING_NO) D ")
				.append("ON A.ID_NO = D.ID ")
				.append("WHERE A.MAIL_BRANCH IN ('0010','0030','0560','3144') ");
		if (!empty(exToNcccDate1)) {
			sbf.append("AND A.IN_MAIN_DATE >= '").append(exToNcccDate1).append("'");
		}
		if (!empty(exToNcccDate2)) {
			sbf.append("AND A.IN_MAIN_DATE <= '").append(exToNcccDate2).append("'");
		}
		return sbf.toString();
	}

	void listWkdata(int selectCnt) throws Exception {
		HashMap<String,String> map = new HashMap<String,String>();
		HashMap<String,String> map1 = new HashMap<String,String>();
		HashMap<String,Integer> map2 = new HashMap<String,Integer>();
		StringBuffer buf = null;

		//處理非員工
		for (int i = 0; i < selectCnt; i++) {
			String embossDate = wp.colStr(i,"EMBOSS_DATE");
			String type = "GN".equals(wp.colStr(i,"TYPE"))?wp.colStr(i,"GROUP_CODE"):wp.colStr(i,"TYPE");
			buf = new StringBuffer();
			if(wp.colEmpty(i,"ACCOUNTING_NO")) {
				String dateType = String.format("%s-%s,", embossDate,type);
				if(map1.get(dateType)==null) {
					map1.put(dateType,buf.append(dateType).append(1).toString());
					map2.put(dateType, 1);
				}else {
					map1.put(dateType,buf.append(dateType).append(map2.get(dateType).intValue() + 1).toString());
					map2.put(dateType, map2.get(dateType).intValue() + 1);
				}
			}
		}
		
		map2 = new HashMap<String,Integer>();
		int r = 1;
		for(String key :map1.keySet()) {
			buf = new StringBuffer();
			String[] type = null;
			String dateType = map1.get(key).toString();
			String[] splits = dateType.split("-");
			if(splits.length>1) {
				type = splits[1].split(",");
				if(type.length>1) {
					if(map.get(type[0]) == null) {
						map.put(type[0], dateType);
						map2.put(type[0], this.toInt(type[1]));
					}else {
						buf.append(map.get(type[0])).append(dateType).append(".");
						if(r%4 == 0) {
							buf.append(LINE_SEPERATOR);
						}
						map.put(type[0],buf.toString());
						map2.put(type[0], map2.get(type[0]) + this.toInt(type[1]));
					}
					
				}
				r++;
			}
		}
		
		for(String key :map.keySet()) {
			map1 = new HashMap<String,String>();
			String dateType = map.get(key).toString();
			map1.put("BRANCH",NOTEMPLOYEE_CODE);
			map1.put("DATA",dateType);
			map1.put("COUNT",map2.get(key).toString());
			notEmployeeMap.put(key,map1);
		}

		
		//處理員工
		int rr = 1;
		map1 = new HashMap<String,String>();
		for (int i = 0; i < selectCnt; i++) {
			String embossDate = wp.colStr(i,"EMBOSS_DATE");
			String type = "GN".equals(wp.colStr(i,"TYPE"))?wp.colStr(i,"GROUP_CODE"):wp.colStr(i,"TYPE");
			String depChiName = wp.colStr(i,"DEP_CHI_NAME");
			String chiName = wp.colStr(i,"CHI_NAME");
			buf = new StringBuffer();
			if(!wp.colEmpty(i,"ACCOUNTING_NO")) {
				String dateType = String.format("%s,%s-", depChiName,embossDate);
				if(map1.get(dateType)==null) {
					map1.put(dateType,String.format("%s△%s%s", dateType,type,chiName));
				}else {
					buf.append(map1.get(dateType)).append(String.format("△%s%s", type,chiName));
					if(rr%4 == 0) {
						buf.append(LINE_SEPERATOR);
					}
					map1.put(dateType,buf.toString());
				}
				rr++;
			}
		}
		
		map = new HashMap<String,String>();
		map2 = new HashMap<String,Integer>();
		for(String key :map1.keySet()) {
			String dateType = map1.get(key).toString();
			int splits = dateType.split("△").length - 1;
			if(splits>=1) {
				map.put(key, String.format("%s,%s.",dateType,splits));
				map2.put(key,splits);
			}
		}

		for(String key :map.keySet()) {
			map1 = new HashMap<String,String>();
			String dateType = map.get(key).toString();
			map1.put("BRANCH",EMPLOYEE_CODE);
			map1.put("DATA",dateType);
			map1.put("COUNT",map2.get(key).toString());
			employeeMap.put(key,map1);
		}

	}
	
	void listWkdataPdf() {
		int i = 0;
		for(String key :notEmployeeMap.keySet()) {
			String[] pdfData =  notEmployeeMap.get(key).get("DATA").toString().split("\\|");
			wp.colSet(i,"PDF_BRANCH", notEmployeeMap.get(key).get("BRANCH").toString());
			wp.colSet(i,"PDF_COUNT", notEmployeeMap.get(key).get("COUNT").toString());
			int x=0;
			for(;x<pdfData.length;x++) {
				wp.colSet(i+x,"PDF_DATA",pdfData[x]);
			}
			i += x;
		}
		for(String key :employeeMap.keySet()) {
			String[] pdfData =  employeeMap.get(key).get("DATA").toString().split("\\|");
			wp.colSet(i,"PDF_BRANCH", employeeMap.get(key).get("BRANCH").toString());
			wp.colSet(i,"PDF_COUNT", employeeMap.get(key).get("COUNT").toString());
			int x=0;
			for(;x<pdfData.length;x++) {
				wp.colSet(i+x,"PDF_DATA",pdfData[x]);
			}
			i += x;
		}
		wp.listCount[0] = i ;
	}
	
	void selectCrdPostage(String cardCnt) {
		String sqlCmd = "SELECT ENV_WEIGHT,CARD_WEIGHT,BIGTAIPEI_AREA_POST,OTHER_AREA_POST ";
		sqlCmd += "FROM CRD_POSTAGE WHERE MAIL_TYPE = '4' AND MIN_CARD_SHEETS < :MIN_CARD_SHEETS AND MAX_CARD_SHEETS > :MAX_CARD_SHEETS ";
		setString("MIN_CARD_SHEETS",cardCnt);
		setString("MAX_CARD_SHEETS",cardCnt);
		sqlSelect(sqlCmd);
	}

	void pdfPrint() throws Exception {
		wp.reportId = PROG_NAME;
		// -cond-
		wp.colSet("cond1", forMatTWDate(wp.sysDate));
		// ===========================
		wp.pageRows = 99999;
		queryFunc();
		listWkdata(wp.selectCnt);
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
