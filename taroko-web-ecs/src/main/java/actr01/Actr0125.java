/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 112-11-07  V1.00.00   Ryan      program initial                            *
 ******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.base.CommString;

public class Actr0125 extends BaseReport {

	CommString commString = new CommString();
  InputStream inExcelFile = null;
  String mProgName = "actr0125";

  String hWhereA = "", hWhereB = "", hWhereC = "";
  String hUnionSql = "";

  @Override
  public void actionFunction(final TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);

    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // strAction="new";
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

  @Override
  public void initPage() {
    // 載入當日日期
    String lsSql = "";
    lsSql = "select business_date, "
            + "to_char(sysdate,'yyyymmdd') as db_sysDate, "
            + "to_char(add_days(to_date(business_date,'yyyymmdd'),-1),'yyyymmdd') as busi_pre_1_days "
            + "from ptr_businday "
            + "fetch first 1 row only ";
    try {
      sqlSelect(lsSql);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    wp.colSet("ex_date_S", sqlStr("busi_pre_1_days"));
  }

  private boolean getWhereStr() throws Exception {
    String exDateS = wp.itemStr("ex_date_S");
    String exBatchNo = wp.itemStr("ex_batch_no");

    if(empty(exDateS) || empty(exBatchNo)) {
        alertErr("處理日期 & 繳款來源 不能為空值");
    	return false;
    }

    hWhereA = " where a.batch_no like :ex_batch_no ";
    hWhereB = " where b.batch_no like :ex_batch_no ";
    hWhereC = " where c.batch_no like :ex_batch_no ";
    setString("ex_batch_no" ,exDateS + exBatchNo + "%");

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.colSet("tot_cnt", "0");
    wp.colSet("tot_amt", "0");
    wp.colSet("success_cnt", "0");
    wp.colSet("success_amt", "0");
    wp.colSet("fail_cnt", "0");
    wp.colSet("fail_amt", "0");
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();


      if (getWhereStr() == false) {
        return;
        
      }

      hUnionSql = " SELECT "
			      	+ " a.batch_no, "
			     	+ " a.serial_no, "
			      	+ " a.payment_no as consumer_no, "
			      	+ " a.pay_amt, "
			      	+ " uf_idno_id(a.id_p_seqno) as id_no, "
			      	+ " a.pay_date, "
			      	+ " left(a.batch_no,8) as porc_date, "
			      	+ " '00' as status_code, "
			      	+ " '成功' as status_desc, "
			      	+ " a.payment_type "
				    + " FROM ACT_PAY_DETAIL a "
				    + hWhereA
			      	+ " union all "
			      	+ " SELECT "
			      	+ " b.batch_no, "
			      	+ " b.serial_no, "
			      	+ " b.payment_no as consumer_no, "
			      	+ " b.pay_amt, "
			      	+ " uf_idno_id(b.id_p_seqno) as id_no, "
			      	+ " b.pay_date, "
			    	+ " left(b.batch_no,8) as porc_date, "
			      	+ " '00' as status_code, "
			      	+ " '成功' as status_desc, "
			      	+ " b.payment_type "
				    + " FROM ACT_PAY_HST b "
				    + hWhereB
			      	+ " union all "
			      	+ " SELECT "
			      	+ " c.batch_no, "
			      	+ " c.serial_no, "
			      	+ " c.pay_card_no as consumer_no, "
			      	+ " c.pay_amt, "
			      	+ " c.id_no, "
			      	+ " c.pay_date, "
			    	+ " left(c.batch_no,8) as porc_date, "
			      	+ " c.error_reason as status_code, "
			      	+ " c.error_remark as status_desc, "
			      	+ " c.payment_type "
				    + " FROM ACT_PAY_ERROR c "
				    + hWhereC
				    ;
   
    hUnionSql += " order by batch_no,serial_no ";
    wp.pageCountSql = "select count(*) from (" + hUnionSql + ")";
    
	wp.sqlCmd  = hUnionSql; 
    if (strAction.equals("PDF")) {
      selectNoLimit();
    }

    pageQuery();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
    }

    wp.setPageValue();

    sumProc();

  }

  void sumProc() throws Exception {
    // 金額合計------------------------------------end
    if (getWhereStr() == false)
      return;

    wp.sqlCmd   = "select "
                + "count(*) as tot_cnt, "
                + "sum(pay_amt) as tot_amt, "
                + "sum(decode(status_code,'00',1,0)) as success_cnt, "
                + "sum(decode(status_code,'00',pay_amt,0)) as success_amt, "
                + "sum(decode(status_code,'00',0,1)) as fail_cnt, "
                + "sum(decode(status_code,'00',0,pay_amt)) as fail_amt "
                + "from ( "
                + hUnionSql
                + " ) "
                ;

		pageSelect();

		if(sqlNotFind()){			
			return;
		}

  }


  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;

      // -cond-
      String exDateS = wp.itemStr("ex_date_S");
      String exBatchNo = wp.itemStr("ex_batch_no");

      CommString commStr = new CommString();
      String cond1 = "處理日期: " + commStr.formatYmd(exDateS) + "  繳款來源: " 
      + commStr.decode(exBatchNo, ",5555,5556,5557,5558"
    		  ,",5555-還額檔繳款,5556-全國繳費網繳款,5557-人工繳款上傳,5558-QRCode繳款");
      
      wp.colSet("cond_1", cond1);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      xlsx.sheetName[0] = "同業代收款查詢清冊";
      queryFunc();
      wp.setListCount(1);
      log("Summ: rowcnt:" + wp.listCount[1]);
      xlsx.processExcelSheet(wp);

      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (final Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    String exDateS = wp.itemStr("ex_date_S");
    String exBatchNo = wp.itemStr("ex_batch_no");

    CommString commStr = new CommString();
    String cond1 = "處理日期: " + commStr.formatYmd(exDateS) + "  繳款來源: " 
    + commStr.decode(exBatchNo, ",5555,5556,5557,5558"
    		,",5555-還額檔繳款,5556-全國繳費網繳款,5557-人工繳款上傳,5558-QRCode繳款");
    
    wp.colSet("cond_1", cond1);
    
    wp.pageRows = 9999;

    queryFunc();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 25;
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

    } catch (final Exception ex) {
    }
  }

}
