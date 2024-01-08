/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package cmsr03;
/** 新貴通卡撥款明細表
 * 2019-1225   JustinWu     增加貴賓卡選單
 * 2019-0617   JH               p_xxx >>acno_p_xxx
 * 2020-0902  JustinWu      cms_lounge_visit table相關查詢改為cms_ppcard_visit
 * 2020-0908  JustinWu      modify the display of excel
 * 111-06-28  V1.00.03  machao      bug修改       
 * */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr3110 extends BaseAction implements InfacePdf {
  int tlVisit = 0, tlCost = 0, tlVistAmt = 0;
  String tableName = "cms_ppcard_visit";

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
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "UPLOAD":
        procFunc();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;

      case "XLS":
        // Excel
        strAction = "XLS";
        break;
      case "PDF":
        // PDF
        strAction = "PDF";
        pdfPrint();
        break;
    }

  }

  @Override
  public void dddwSelect() {

  }

  @Override
  public void queryFunc() throws Exception {

    if (empty(wp.itemStr("ex_date1")) || empty(wp.itemStr("ex_date2"))) {
      alertErr2("使用日期: 不可空白");
      return;
    }
    if (empty(wp.itemStr("ex_vip_kind"))) {
      alertErr2("貴賓卡: 不可空白");
      return;
    }

    if (!empty(wp.itemStr("ex_idno")) && wp.itemStr("ex_idno").length() <= 5) {
      alertErr2("身分證ID 需至少5碼 ");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_txn_date1"), wp.itemStr("ex_txn_date2")) == false) {
      alertErr2("查詢期間起迄：輸入錯誤");
      return;
    }

//    //2020-09-01 JustinWu
//    switch (wp.itemStr("ex_vip_kind")) {
//      // 龍騰卡
//      case "2":
//        tableName = "cms_lounge_visit";
//        break;
//      // 新貴通
//      case "1":
//        tableName = "cms_ppcard_visit";
//        break;
//    }
//    wp.colSet("tableName", tableName);
    
    String date1 = wp.itemStr("ex_date1");
    String date2 = wp.itemStr("ex_date2");

    String lsWhere = " where 1=1 " 
//    		+sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"),
//                    "visit_date")
        + sqlBetween("ex_date1", "ex_date2", "visit_date")
        + sqlCol(wp.itemStr("ex_vip_kind"), " vip_kind")
        + sqlCol(wp.itemStr("ex_bin_type"), " bin_type")
        + sqlCol(wp.itemStr("ex_idno"), "id_no", "like%")
        + sqlCol(wp.itemStr("ex_ppcard_no"), "pp_card_no", "like%");
    
    if (wp.itemEq("ex_free_flag", "1")) {
      lsWhere += " and ch_visits+guests_count > free_use_cnt ";
    } else if (wp.itemEq("ex_free_flag", "2")) {
      lsWhere += " and ch_visits+guests_count <= free_use_cnt ";
    }

    sum(lsWhere);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
	  
//    //2020-09-01 JustinWu	  
//    if (empty(tableName)) {
//      tableName = wp.itemStr("tableName");
//    }

    wp.pageControl();

    wp.selectSQL = " " 
    	+ " vip_kind, "
        + " crt_date ," 
    	+ " bin_type ," 
        + " visit_date ," 
    	+ " pp_card_no ,"
        + " iso_conty ," 
    	+ " ch_visits ," 
        + " guests_count ," 
    	+ " use_city ,"
        + " id_no as holder_id," 
    	+ " free_use_cnt ," 
        + " ch_cost_amt ," 
    	+ " guest_cost_amt ,"
        + " card_no ," 
    	+ " mcht_no ," 
        + " uf_idno_name(id_p_seqno) as chi_name ,"
        + " uf_mcht_name_bil(mcht_no) as mcht_chi_name ,"
        + " ch_visits + guests_count as wk_tot_visit ,"
        + " ch_visits + guests_count - free_use_cnt as wk_cost_cnt , "
        + " (ch_visits - free_use_cnt) * ch_cost_amt + guests_count * guest_cost_amt as wk_vist_amt ";

    wp.daoTable = tableName;
    wp.whereOrder = " order by visit_date Asc, bin_type Asc, pp_card_no Asc ";

    pageQuery(sqlParm.getConvParm());
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

//    wp.setListCount(1);

    wp.setPageValue();
    queryAfter();

  }

  void sum(String lsWhere) {
    String sql1 = "select sum(ch_visits + guests_count) as tl_visit ,"
        + " sum(ch_visits + guests_count - free_use_cnt) as tl_cost ,"
        + " sum((ch_visits - free_use_cnt) * ch_cost_amt + guests_count * guest_cost_amt) as tl_vist_amt ,"
        + " count(*) as db_cnt " + " from " + tableName + lsWhere;
    sqlSelect(sql1);
    wp.colSet("tl_visit", sqlStr("tl_visit"));
    wp.colSet("tl_cost", sqlStr("tl_cost"));
    wp.colSet("tl_vist_amt", sqlStr("tl_vist_amt"));
    wp.colSet("db_cnt", sqlStr("db_cnt"));
  }

  void queryAfter() {
    String exVipKind = wp.itemStr("ex_vip_kind"), vipKindDesc = "";
    vipKindDesc = getVipKindDesc(exVipKind);
    
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "vip_kind", exVipKind);
      wp.colSet(ii, "vip_kind_desc", vipKindDesc);

      wp.colSet(ii, "wk_mcht_no_name",wp.colStr(ii, "mcht_no") + "_" + wp.colStr(ii, "mcht_chi_name"));
      wp.colSet(ii, "wk_id_name", wp.colStr(ii, "holder_id") + "_" + wp.colStr(ii, "chi_name"));
      if (this.eqIgno(wp.colStr(ii, "bin_type"), "V")) {
        wp.colSet(ii, "wk_city", wp.colStr(ii, "use_city"));
      } else {
        wp.colSet(ii, "wk_city", wp.colStr(ii, "iso_conty"));
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
    wp.reportId = "cmsr3110";
    wp.pageRows = 9999;
    
    StringBuilder sb1 = new StringBuilder();
    sb1.append("使用日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "+ commString.strToYmd(wp.itemStr("ex_date2")));
    sb1.append("  ");
    sb1.append("貴賓卡: " + getVipKindDesc(wp.itemStr("ex_vip_kind")));
    
    StringBuilder sb2 = new StringBuilder();
    if( ! wp.itemEmpty("ex_idno")) {
    	sb2.append("身分證ID: " + wp.itemStr("ex_idno"));
    	sb2.append("  ");
    }
    if( ! wp.itemEmpty("ex_ppcard_no")) {
    	sb2.append("貴賓卡卡號: " + wp.itemStr("ex_ppcard_no"));
    	sb2.append("  ");
    }
    if( ! wp.itemEmpty("ex_bin_type")) {
    	sb2.append("卡別: " + getBinTypeDesc(wp.itemStr("ex_bin_type")));
    	sb2.append("  ");
    }
    if( ! wp.itemEmpty("ex_free_flag")) {
    	sb2.append("卡友收費類別: " + getFreeFlagDesc(wp.itemStr("ex_free_flag")));
    }
    
    wp.colSet("cond1", sb1.toString());
    wp.colSet("cond2", sb2.toString());
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr3110.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

private String getVipKindDesc(String exVipKind) {
	String vipKindDesc = "";
	switch (exVipKind) {
	// 龍騰卡
	case "2":
		vipKindDesc = "2.龍騰卡";
		break;
	// 新貴通
	case "1":
		vipKindDesc = "1.新貴通";
		break;
	}
	return vipKindDesc;
}

private String getBinTypeDesc(String exBinType) {
	String binTypeDesc = "";
	switch (exBinType) {
	// VISA
	case "V":
		binTypeDesc = "VISA";
		break;
	// MasterCard
	case "M":
		binTypeDesc = "MasterCard";
		break;
	// JCB
	case "J":
		binTypeDesc = "JCB";
		break;
	}
	return binTypeDesc;
}

private String getFreeFlagDesc(String exFreeFlag) {
	String freeFlagDesc = "";
	switch (exFreeFlag) {
	// 全部
	case "0":
		freeFlagDesc = "全部";
		break;
	// 自費
	case "1":
		freeFlagDesc = "自費";
		break;
	// 免費
	case "2":
		freeFlagDesc = "免費";
		break;
	}
	return freeFlagDesc;
}

}
