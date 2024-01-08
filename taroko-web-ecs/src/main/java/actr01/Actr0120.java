/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-21  V1.00.00  Max Lin    program initial                            *
 * 110-07-21  V1.00.01  Danny      update:Mantis7587                          *
 * 111/10/24  V1.00.02  jiangyigndong  updated for project coding standard    *
 * 112/02/04  V1.00.03  Simon      fixed METHOD : pageSelect 系統異常         *
 * 112/10/02  V1.00.04  Simon      客製化 tcb 同業代收款查詢清冊              *
 * 112/12/08  V1.00.05  Simon      調整 Report_id                             *
 ******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.base.CommString;

public class Actr0120 extends BaseReport {

	CommString commString = new CommString();
  InputStream inExcelFile = null;
  String mProgName = "actr0120";

  String hWhereA = "", hWhereB = "", hWhereC = "";
  String exDateS = "";
  String exDateE = "";
  String exBankId = "";
  String hPayType = "";
  String hUnionSql = "";
  String condWhere = "";
  String sumWhere = ""; // 金額合計 (Linda, 20180912)
//String sum_sqlCmd = "";

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
    exDateS = wp.itemStr("ex_date_S");
  //exDateE = wp.itemStr("ex_date_E");
    exBankId = wp.itemStr("ex_bank_id");

    //if (empty(ex_date_S) == true && empty(ex_date_E) == true) {
    //if (empty(ex_date_S) == true ) { //html 已有設定必須輸入
    //		alertErr("請輸入處理日期");
    //		return false;
    //}
/***
    if (empty(exDateS) == false && empty(exDateE) == false) {
      if (exDateS.compareTo(exDateE) > 0) {
        alertErr("起值不可大於迄值");
        return false;
      }
    }
***/

    if (empty(exBankId) == true) {
      alertErr("請輸入單位別代號");
      return false;
    }

    // 固定條件
    hWhereA = " where substr(a.batch_no,9,4)='1003' ";
    hWhereB = " where substr(b.batch_no,9,4)='1003' ";
    hWhereC = " where substr(c.batch_no,9,4)='1003' ";

    if (empty(exDateS) == false) {
      hWhereA += " and substr(a.batch_no,1,8) = :ex_date_S ";
      setString("ex_date_S", exDateS);
      hWhereB += " and substr(b.batch_no,1,8) = :ex_date_S ";
      setString("ex_date_S", exDateS);
    //hWhereC += " and substr(c.batch_no,1,8) = :ex_date_S ";
    //setString("ex_date_S", exDateS);
      hWhereC += " and c.print_date = :ex_date_S ";
      setString("ex_date_S", exDateS);
    }
/***
    if (empty(exDateE) == false) {
      hWhereB += " and substr(b.batch_no,1,8) <= :ex_date_E ";
      setString("ex_date_E", exDateE);
    //hWhereC += " and substr(c.batch_no,1,8) <= :ex_date_E ";
    //setString("ex_date_E", exDateE);
      hWhereC += " and print_date <= :ex_date_E ";
      setString("ex_date_E", exDateE);
    }
***/

    if (empty(exBankId) == false) {
      hWhereA += " and a.def_branch = :ex_bank_id ";
      setString("ex_bank_id", exBankId);
      hWhereB += " and b.def_branch = :ex_bank_id ";
      setString("ex_bank_id", exBankId);
      hWhereC += " and c.bank_no = :ex_bank_id ";
      setString("ex_bank_id", exBankId);
    }

    // 金額合計 (Linda, 20180912)--------------start
    // 固定條件

/***
    sumWhere = " where 1=1 ";
    if (empty(exDateS) == false) {
      sumWhere += sqlCol(exDateS, "print_date", ">=");
    }
    if (empty(exDateE) == false) {
      sumWhere += sqlCol(exDateE, "print_date", "<=");
    }
    if (empty(exBankId) == false) {
      sumWhere += sqlCol(exBankId, "bank_no", "=");
    }
***/
    // 金額合計------------------------------------end

  //wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;

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

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (eqIgno(wp.buttonCode, "M")) {
      if (getWhereStr() == false) {
        return;
      }
    }

		hUnionSql = " SELECT "
			      	+ " a.batch_no, "
			      	+ " lpad(a.collection_store_no,8,'0') as store_no, "
			      	+ " a.payment_no as consumer_no, "
			      	+ " a.pay_amt, "
			      	+ " uf_idno_id(a.id_p_seqno) as id_no, "
			      	+ " a.pay_date, "
			      	+ " a.crt_date as acct_date, "
			      	+ " '00' as status_code, "
			      	+ " '成功' as status_desc, "
			      	+ " a.serial_no "
				      + " FROM act_pay_detail a "
				      + hWhereA
			      	+ " union "
			      	+ " SELECT "
			      	+ " b.batch_no, "
			      	+ " lpad(b.collection_store_no,8,'0') as store_no, "
			      	+ " b.payment_no as consumer_no, "
			      	+ " b.pay_amt, "
			      	+ " uf_idno_id(b.id_p_seqno) as id_no, "
			      	+ " b.pay_date, "
			      	+ " b.update_date as acct_date, "
			      	+ " '00' as status_code, "
			      	+ " '成功' as status_desc, "
			      	+ " b.serial_no "
				      + " FROM act_pay_hst b "
				      + hWhereB
			      	+ " union "
			      	+ " SELECT "
			      	+ " c.batch_no, "
			      	+ " lpad(c.allot_account,8,'0') as store_no, "
			      	+ " c.consumer_no, "
			      	+ " c.trans_amt as pay_amt, "
			      	+ " '' as id_no, "
			      	+ " c.trans_date as pay_date, "
			      	+ " c.telephone_no as acct_date, "
			      	+ " 'XX' status_code, "
			      	+ " 'Barcode檢查碼不正確' status_desc, "
			      	+ " '7777777' as serial_no "
				      + " FROM act_b003r1 c "
				      + hWhereC
				      ;

		wp.sqlCmd = hUnionSql
				      + " order by batch_no,serial_no "
				      ;


    wp.pageCountSql = "select count(*) from (" + hUnionSql + ")";

    // sum_sqlCmd = wp.sqlCmd;//wp.sqlCmd 在 pageQuery 執行後會清空

    if (strAction.equals("PDF")) {
      selectNoLimit();
    }

    pageQuery();
    // list_wkdata();
    wp.setListCount(1);

    if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
    }

    listWkdata();

    wp.setPageValue();

  //wp.listCount[1] = wp.dataCnt;
  //wp.colSet("tot_cnt", Integer.toString(wp.dataCnt));
    sumProc();

  }

  // 金額合計 (Linda, 20180912)

  void sumProc() throws Exception {
    // 金額合計------------------------------------end
    // String sql1 ="select sum(trans_amt) as tot_amt from act_b003r1 "
    // + sum_where ;
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

/***
    wp.colSet("tot_cnt", sqlStr("tot_cnt"));
    wp.colSet("tot_amt", sqlStr("tot_amt"));
    wp.colSet("success_cnt", sqlStr("success_cnt"));
    wp.colSet("success_amt", sqlStr("success_amt"));
    wp.colSet("fail_cnt", sqlStr("fail_cnt"));
    wp.colSet("fail_amt", sqlStr("fail_amt"));
***/

  }

  void listWkdata() throws Exception {
    String tStr1 = "", tStr2 = "", tStr3 = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      tStr1 = commString.mid(wp.colStr(ii, "pay_date"), 0, 4);
      tStr2 = commString.mid(wp.colStr(ii, "pay_date"), 4, 2);
      tStr3 = commString.mid(wp.colStr(ii, "pay_date"), 6, 2);
			wp.colSet(ii,"pay_date", tStr1+"/"+tStr2+"/"+tStr3);

      tStr1 = commString.mid(wp.colStr(ii, "acct_date"), 0, 4);
      tStr2 = commString.mid(wp.colStr(ii, "acct_date"), 4, 2);
      tStr3 = commString.mid(wp.colStr(ii, "acct_date"), 6, 2);
			wp.colSet(ii,"acct_date", tStr1+"/"+tStr2+"/"+tStr3);
    }

  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;

      // -cond-
      exDateS = wp.itemStr("ex_date_S");
    //exDateE = wp.itemStr("ex_date_E");
      exBankId = wp.itemStr("ex_bank_id");

    //final String cond1 = "處理日期: " + exDateS + " ~ " + exDateE + "  單位別代號: " + exBankId;
      final String cond1 = "處理日期: " + exDateS + "  單位別代號: " + exBankId;
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
    exDateS = wp.itemStr("ex_date_S");
  //exDateE = wp.itemStr("ex_date_E");
    exBankId = wp.itemStr("ex_bank_id");

  //final String cond1 = "處理日期: " + exDateS + " ~ " + exDateE + "  銀行代號: " + exBankId;
  //final String cond1 = "處理日期: " + exDateS + "  銀行代號: " + exBankId;
  //wp.colSet("cond_1", cond1);
    String tStr1 = "", tStr2 = "", tStr3 = "";
    tStr1 = commString.mid(exDateS, 0, 4);
    tStr2 = commString.mid(exDateS, 4, 2);
    tStr3 = commString.mid(exDateS, 6, 2);
    wp.colSet("file_date", tStr1+"/"+tStr2+"/"+tStr3);

 		if (exBankId.equals("710")) {
      wp.colSet("report_id", "CRD86");
      wp.colSet("unit_chi_name", "統一超商代收信用卡帳款明細表");
 		} else if (exBankId.equals("71A")) {
      wp.colSet("report_id", "CRD86-AP");
      wp.colSet("unit_chi_name", "統一超商App代收信用卡帳款明細表");
 		} else if (exBankId.equals("720")) {
      wp.colSet("report_id", "CRD86A");
      wp.colSet("unit_chi_name", "全家超商代收信用卡帳款明細表");
 		} else if (exBankId.equals("72A")) {
      wp.colSet("report_id", "CRD86A-AP");
      wp.colSet("unit_chi_name", "全家超商App代收信用卡帳款明細表");
 		} else if (exBankId.equals("730")) {
      wp.colSet("report_id", "CRD86B");
      wp.colSet("unit_chi_name", "萊爾富超商代收信用卡帳款明細表");
 		} else if (exBankId.equals("73A")) {
      wp.colSet("report_id", "CRD86B-AP");
      wp.colSet("unit_chi_name", "萊爾富超商App代收信用卡帳款明細表");
 		} else if (exBankId.equals("770")) {
      wp.colSet("report_id", "CRD100");
      wp.colSet("unit_chi_name", "農業金庫代收信用卡帳款明細表");
 		} else if (exBankId.equals("780")) {
      wp.colSet("report_id", "CRD100A");
      wp.colSet("unit_chi_name", "信聯社代收信用卡帳款明細表");
 		} else {
      wp.colSet("report_id", "CRDxxx");
      wp.colSet("unit_chi_name", "其他代收信用卡帳款明細表");
 		}

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

      // marked by (Linda , 20180608)
//			wp.optionKey = wp.col_ss("ex_bank_id");
//			wp.initOption = "--";
//			wp.optionKey = wp.col_ss("ex_bank_id");
//			dddw_list("dddw_bank_id", "ptr_bank_allot", "bank_id", "bank_name", "where 1=1 and left(bank_id,3) in ('003','700','710','711','712','714','715') order by bank_id");

    } catch (final Exception ex) {
    }
  }

}
