/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-10  V1.00.00  YangHan   program initial                             *
******************************************************************************/
package mktr01;

import java.io.InputStream;
import java.util.Calendar;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Mktr6010 extends BaseReport {
  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      // /* 瀏覽功能 :skip-page */
       queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      // /* 動態查詢 */
      // querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // wp.setExcelMode();
      // xlsPrint();
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

  @Override
  public void queryFunc() throws Exception {
    // 判斷前端表單傳入的數據是否爲空
 	if (wp.itemEmpty("ex_date1")) {
 		alertErr2("篩選年費年月:不可空白!");
 		return;
 	}

    wp.whereStr = " where 1=1 ";

 	if (!empty(wp.itemStr("ex_date1"))) {
 	      wp.whereStr += "and A.card_fee_date = :ex_date1 ";
 	      setString("ex_date1", wp.itemStr("ex_date1"));
 	}

    if (!empty(wp.itemStr("ex_reg_bank_no"))) {
      wp.whereStr += "and A.reg_bank_no = :reg_bank_no ";
      setString("reg_bank_no", wp.itemStr("ex_reg_bank_no"));
    }
    
    if (!empty(wp.itemStr("ex_group_code"))) {
        wp.whereStr += "and A.group_code = :ex_group_code ";
        setString("ex_group_code", wp.itemStr("ex_group_code"));
    }
    
    if (!empty(wp.itemStr("ex_card_type"))) {
        wp.whereStr += "and A.card_type = :ex_card_type ";
        setString("ex_card_type", wp.itemStr("ex_card_type"));
    }

    if (!empty(wp.itemStr("ex_id_no"))) {
        wp.whereStr += "and A.id_p_seqno = (select id_p_seqno from crd_idno where id_no = :ex_id_no ) ";
        setString("ex_id_no", wp.itemStr("ex_id_no"));
    }
    
    if (!empty(wp.itemStr("ex_card_no"))) {
        wp.whereStr += "and A.card_no = :ex_card_no ";
        setString("ex_card_no", wp.itemStr("ex_card_no"));
    }
    
    String exType = wp.itemStr("ex_type");
    switch (exType) {
      case "N":
    	  wp.whereStr += "and A.rcv_annual_fee > 0 ";
        break;
      case "Y":
    	  wp.whereStr += "and A.rcv_annual_fee = 0 ";
        break;
    }
    
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = ""
			+ "A.card_fee_date, "
			+ "A.reg_bank_no, "
			+ "A.issue_date, "
			+ "A.org_annual_fee, "
			+ "A.rcv_annual_fee, "
			+ "A.group_code, "
			+ "A.card_no, "
			+ "I.id_no, "
			+ "A.card_type, "
			+ "I.chi_name, "
			+ "A.reason_code, "
			+ "A.purch_review_month_beg||'~'||A.purch_review_month_end as acct_month_between, "
			+ "A.card_pur_cnt, "
			+ "A.card_pur_amt, "
			+ "A.sum_pur_cnt, "
			+ "A.sum_pur_amt, "
			+ "CONCAT(CONCAT(I.home_area_code1, I.home_tel_no1), I.home_tel_ext1) home_phone, "
			+ "CONCAT(CONCAT(I.office_area_code1, I.office_tel_no1), I.office_tel_ext1) office_phone, "
			+ "I.cellar_phone ";
    wp.daoTable = " cyc_afee A left join crd_idno I ON A.id_p_seqno = I.id_p_seqno";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    
    wp.colSet("count", intToStr(wp.selectCnt));
    wp.setPageValue();
    
  }

  void pdfPrint() throws Exception {
    wp.reportId = "Mktr6010";

    // ===========================
    wp.pageRows = 999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "mktr6010.xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 20;
    // pdf.pageVert= true; //直印
    pdf.procesPDFreport(wp);
    // System.out.println("到這了222");
    pdf = null;
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_reg_bank_no");
      this.dddwList("dddw_branch_name", "gen_brn", "branch", "full_chi_name",
          "where 1=1 order by branch");
    } catch (Exception ex) {
    }
  }
}

