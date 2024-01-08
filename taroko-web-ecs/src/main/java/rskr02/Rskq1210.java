package rskr02;
/** 2019-0621:  JH    p_xxx >>acno_p_xxx
 * 109-04-28  V1.00.01  Tanwei       updated for project coding standard
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
 * */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Rskq1210 extends BaseAction implements InfacePdf {

  String dataYymm = "", batchNo = "", kk3 = "";

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

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskq1210")) {
        wp.optionKey = wp.colStr("ex_action_code");
        ddlbList("dddw_action_code", wp.itemStr("ex_action_code"),
            "ecsfunc.DeCodeRsk.trialAction");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_corp_no")) && empty(wp.itemStr("ex_data_ym1"))
        && empty(wp.itemStr("ex_data_ym2")) && empty(wp.itemStr("ex_close_date1"))
        && empty(wp.itemStr("ex_close_date2")) && empty(wp.itemStr("ex_batch_no"))) {
      alertErr2("請輸入查詢條件");
      return;
    }


    if (this.chkStrend(wp.itemStr("ex_close_date1"), wp.itemStr("ex_close_date2")) == false) {
      alertErr2("覆審日期起迄：輸入錯誤");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_date_ym1"), wp.itemStr("ex_date_ym2")) == false) {
      alertErr2("資料日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_close_date1"), "A.close_date", ">=")
        + sqlCol(wp.itemStr("ex_close_date2"), "A.close_date", "<=")
        + sqlCol(wp.itemStr("ex_date_ym1"), "C.data_yymm", ">=")
        + sqlCol(wp.itemStr("ex_date_ym2"), "C.data_yymm", "<=")
        + sqlCol(wp.itemStr("ex_corp_no"), "A.corp_no", "like%")
        + sqlCol(wp.itemStr("ex_batch_no"), "C.batch_no", "like%")
        + sqlCol(wp.itemStr("ex_action_code"), "A.action_code")
        + sqlCol(wp.itemStr("ex_risk_group1"), "A.risk_group1")
        + sqlCol(wp.itemStr("ex_risk_group2"), "A.risk_group2");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "A.batch_no," + " A.corp_no||'_'||uf_corp_name(A.corp_no) as wk_corp_no_name,"
        + " A.corp_p_seqno," + " A.charge_idno," + " A.charge_name," + " A.risk_group1,"
        + " A.risk_group2," + " A.jcic_flag," + " B.data_yymm," + " B.corp_no," + " A.action_code,"
        + " A.close_date," + " A.close_apr_date," + " A.corp_no as db_corp_no2";
    wp.daoTable =
        "rsk_trcorp_mast C join rsk_trcorp_bank_stat B on C.DATA_YYMM = B.DATA_YYMM join rsk_trcorp_list A on C.BATCH_NO = A.batch_no and A.corp_no = B.CORP_NO ";
    wp.whereOrder = " order by B.data_yymm desc, A.corp_no";
    pageQuery();
    
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    
    wp.setListCount(1);
    wp.setPageValue();
    
    list_wkdata();
  }

   void list_wkdata(){
	   String[] cardVal = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
	   String[] cardName =			   
			   {"原額用卡", "調降額度-未降足額度者凍結", "調降額度-未降足額度者維護特指", "調整額度", "調降額度-卡戶凍結(個繳)", "調降額度-維護特指",
	            "卡戶凍結[4] [5]", "卡片維護特指", "額度內用卡"};
	   for (int ii=0; ii<wp.selectCnt;ii++){
		   wp.colSet(ii, "tt_action_code",wp.colStr(ii,"action_code")+"."+commString.decode(wp.colStr(ii,"action_code"),cardVal,cardName));
	   }
	   
   }   
   
  @Override
  public void querySelect() throws Exception {
    dataYymm = wp.itemStr("data_k1");
    batchNo = wp.itemStr("data_k2");
    kk3 = wp.itemStr("data_k3");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(dataYymm))
      dataYymm = wp.itemStr("data_yymm");
    if (empty(batchNo))
      batchNo = wp.itemStr("batch_no");
    if (empty(kk3))
      kk3 = wp.itemStr("corp_no");

    wp.selectSQL = "A.corp_no," + " B.data_yymm , " + " uf_corp_name(A.corp_no) as db_corp_cname,"
        + " B.credit_limit," + " A.batch_no," + " B.m1_cnt01_06," + " A.charge_idno,"
        + " B.m2_cnt01_06," + " A.charge_name," + " B.branch_exchg_type ,"
        + " decode(B.branch_exchg_type,'1','授信戶','2','國外匯兌/存款戶','3','無往來戶') as tt_exchg_type,"
        + " B.max_cr_bal_brch ," + " A.group_proc_date1," + " B.max_cr_limit_brch,"
        + " A.risk_group1," + " B.max_depos_brch," + " A.group_proc_date2," + " B.max_exchg_brch,"
        + " A.risk_group2," + " B.inrate_type," + " A.jcic_flag," + " B.inrate_date,"
        + " A.corp_abnor_yn," + " B.inrate_ref_date," + " A.corp12_ovdue_yn,"
        + " B.inrate_final_code," + " A.corp_cr_abnor," + " B.cr_ovdue_bal30,"
        + " B.free_report_npl," + " A.corp_add_note," + " B.tot_depos_bal," + " A.corp_rept_case,"
        + " B.avg_depos_bal01_06," + " A.idno6_late_pay," + " B.avg_depos_bal07_12,"
        + " B.tot_cr_limit," + " A.idno_cr_abnor," + " B.avg_cr_bal01_06," + " B.avg_cr_bal07_12,"
        + " A.idno12_ovdue," + " B.tot_cr_bal," + " B.avg_cr_limit01_06," + " A.idno_unsecu_bal,"
        + " B.avg_cr_limit07_12," + " B.tot_exchg_amt," + " A.trial_remark," + " B.avg_exchg01_06,"
        + " B.avg_exchg07_12," + " A.close_apr_date," + " A.corp_p_seqno," + " A.crt_date , "
        + " A.crt_user , " + " A.mod_user , " + " to_char(A.mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = "rsk_trcorp_bank_stat B left join rsk_trcorp_list A on B.corp_no = A.corp_no";
    wp.whereStr = " where 1=1" + sqlCol(dataYymm, "B.data_yymm") + sqlCol(batchNo, "A.batch_no")
        + sqlCol(kk3, "B.corp_no");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + dataYymm);
      return;
    }

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
	wp.reportId = "rskq1210";
    String tmpStr = getCondString();
    wp.colSet("cond1", tmpStr);
    wp.pageRows = 9999;
    queryFunc();
    
    if(sqlNotFind()) {
    	wp.respHtml = "TarokoErrorPDF";
    	return;
    }
    
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskq1210.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
	
}

String getCondString() {
	
	String tmpStr = "";
	
	if(wp.itemEmpty("ex_corp_no") == false)
		tmpStr += "公司統編: " + wp.itemStr("ex_corp_no");
	if(wp.itemEmpty("ex_data_ym1") == false || wp.itemEmpty("ex_data_ym2") == false)
		tmpStr += " 資料月份: " + commString.strToYmd(wp.itemStr("ex_data_ym1"))+ " -- "+ commString.strToYmd(wp.itemStr("ex_data_ym2"));
	if(wp.itemEmpty("ex_close_date1") == false || wp.itemEmpty("ex_close_date2") == false)
		tmpStr += " 覆審日期: " + commString.strToYmd(wp.itemStr("ex_close_date1"))+ " -- "+ commString.strToYmd(wp.itemStr("ex_close_date2"));
	if(wp.itemEmpty("ex_risk_group1") == false || wp.itemEmpty("ex_risk_group2") == false)
		tmpStr += " 分群: " +wp.itemStr("ex_risk_group1")+" "+wp.itemStr("ex_risk_group2");
	if(wp.itemEmpty("ex_batch_no") == false)
		tmpStr += " 覆審批號:"+wp.itemStr("ex_batch_no");
	
	if(wp.itemEmpty("ex_action_code") == false) {
		String[] cardVal = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
	    String[] cardName =
	        {"原額用卡", "調降額度-未降足額度者凍結", "調降額度-未降足額度者維護特指", "調整額度", "調降額度-卡戶凍結(個繳)", "調降額度-維護特指",
	            "卡戶凍結[4] [5]", "卡片維護特指", "額度內用卡"};
	    
	    tmpStr += " Action Code:"+wp.itemStr("ex_action_code")+"."+commString.decode(wp.itemStr("ex_action_code"), cardVal, cardName);
	}		
	
	return tmpStr;
}

}
