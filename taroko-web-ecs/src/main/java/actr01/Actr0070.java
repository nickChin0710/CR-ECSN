/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-08-17  V1.00.00  Andy Liu      program initial                         *
 * 107-07-26  V1.00.01  Alex          bug fixed                               *
 * 109-07-27  V1.00.02  Andy       update:Mantis3836                          *
 * 109-07-28  V1.00.03  Andy       update:Mantis3850                          *
 * 110-03-04  v1.00.04  Andy       Update PDF隠碼作業                                                                      *
 * 111/10/24  V1.00.05  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/

package actr01;

import java.io.InputStream;
import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0070 extends BaseReport {
  String lsWhere1 = "" , lsWhere2 = "";
  String exDateS = "", exDateE = "", exCurrCode = "";
  String Errmsg = "";
  String enterAcctDate = "";
  String wkAcctKey = "", wkIdno;
  String chiName = "";
  String currCode = "";
  String wkAcctNo = "", wkAutopayId = "";
  double transactionAmt = 0;
  String errType = "";
  String gpCurrCnt, gpAcctCnt = "", gpTotCnt = "";
  String wkCurrSort;
  String reportSubtitle ="";

  InputStream inExcelFile = null;
  String mProgName = "actr0070";

  String condWhere = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp="
            + wp.respCode + ",rHtml=" + wp.respHtml);
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
  public void queryFunc() throws Exception {

    if(getWhere()==false){
      alertErr(Errmsg);
      return;
    }

    listSum();

    wp.setQueryMode();
    queryRead();
  }

  boolean getWhere(){
    String exDate1 = "" , exDate2 , exCurrCode ="" ;
    exDate1 = wp.itemStr("ex_date_S");
    exDate2 = wp.itemStr("ex_date_E");
    exCurrCode = wp.itemStr("ex_curr_code");

    if(this.chkStrend(exDate1, exDate2)==false){
      Errmsg = "IBM 扣帳日期 : 起迄錯誤 !";
      return false ;
    }

    lsWhere1 = " where 1=1 "
            + " and err_type <'2' "
            +sqlCol(exDate1,"print_date",">=")
            +sqlCol(exDate2,"print_date","<=")
            +sqlCol(exCurrCode,"uf_nvl(curr_code,'901')")
    ;

    lsWhere2 = " where 1=1 "
            + " and status_code = '99' "
            +sqlCol(exDate1,"enter_acct_date",">=")
            +sqlCol(exDate2,"enter_acct_date","<=")
            +sqlCol(exCurrCode,"uf_nvl(curr_code,'901')")
    ;
    return true;
  }


  void listSum() throws Exception{
    String sql1 = " select "
            + " count(*) as tl_rows , "
            + " sum(db_amt) as tl_amt "
            + " from "
            + " (select transaction_amt db_amt from act_a005r1 "+ lsWhere1
            + " union all "
            + " select decode(curr_code,'901',transaction_amt,'',transaction_amt,dc_transaction_amt) as db_amt "
            + " from act_chkautopay "+ lsWhere2
            + " ) "
            ;

    sqlSelect(sql1);

    wp.colSet("tl_rows", sqlStr("tl_rows"));
    wp.colSet("tl_amt", sqlStr("tl_amt"));

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if(getWhere()==false){
      alertErr(Errmsg);
      return;
    }

    wp.sqlCmd = "select enter_acct_date,"
            + "acct_type,"
            //+ "curr_code,"
            + "decode(curr_code,'901','台幣','840','美金','392','日幣',curr_code) as curr_code,"
            + "wk_acct_key,"
            + "db_hi_acct_key, "
            + "(enter_acct_date||'_'||acct_type||'_'||curr_code) as group_1, "
            + "(enter_acct_date||'_'||acct_type) as group_2, "
            + "id_no,"
            + "db_hi_id_no,"
            + "chi_name,"
            + "uf_hi_cname(chi_name) as db_hi_chi_name,"
            + "autopay_acct_no,"
            + "uf_hi_acctno(autopay_acct_no) db_hi_autopay_acct_no,"
            + "autopay_id,"
            + "uf_hi_idno(autopay_id) as db_hi_autopay_id,"
            + "transaction_amt,"
            + "err_type,"
            + "group_ct "
            + "from ("
            + " select "
            + " enter_acct_date ,"
            + " acct_type ,"
            + " uf_nvl(curr_code,'901') curr_code ,"
            + " acct_type||'-'||uf_acno_key(p_seqno)  as wk_acct_key , "
            + " acct_type||'-'||uf_hi_idno(substr(uf_acno_key(p_seqno),1,10))||substr(uf_acno_key(p_seqno),11,1)  as db_hi_acct_key , "
            //+ " uf_idno_id(id_p_seqno) as id_no ,"
            + " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = act_a005r1.id_p_seqno) as id_no, "
            + " (select uf_hi_idno(id_no)||'-'||id_no_code from crd_idno where id_p_seqno = act_a005r1.id_p_seqno) as db_hi_id_no, "
            + " chi_name ,"
            + " substr(autopay_acct_no,4,11) as autopay_acct_no ,"
            + " autopay_id ,"
            + " transaction_amt ,"
            + " decode(err_type,'0','IBM 多','1','已 update','2','R6 多') as err_type, "
            + " 1 group_ct "
            + " from act_a005r1 "
            + lsWhere1
            + " union all "
            + " select "
            + " enter_acct_date ,"
            + " acct_type ,"
            + " uf_nvl(curr_code,'901') curr_code ,"
            + " acct_type||'-'||uf_acno_key(p_seqno) as wk_acct_key ,"
            + " acct_type||'-'||uf_hi_idno(substr(uf_acno_key(p_seqno),1,10))||substr(uf_acno_key(p_seqno),11,1)  as db_hi_acct_key , "
            // + " uf_idno_id(id_p_seqno) as id_no ,"
            + " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = act_chkautopay.id_p_seqno) as id_no, "
            + " (select uf_hi_idno(id_no)||'-'||id_no_code from crd_idno where id_p_seqno = act_chkautopay.id_p_seqno) as db_hi_id_no, "
            + " chi_name ,"
            + " decode(curr_code,'901',substr(autopay_acct_no,4,11),'',substr(autopay_acct_no,4,11),substr(dc_autopay_acct_no,4,11)) as autopay_acct_no ,"
            + " decode(curr_code,'901',autopay_id,'',autopay_id,dc_autopay_id) as autopay_id ,"
            + " decode(curr_code,'901',transaction_amt,'',transaction_amt,dc_transaction_amt) as transaction_amt ,"
            + " 'R6 多' as err_type, "
            + " 1 group_ct "
            + " from act_chkautopay "
            + lsWhere2
            +") "
            + " order by 1 asc, 2 asc, 3 desc ,4 asc "
    ;

    wp.pageCountSql =""
            +"select count(*) from ( "
            +" select distinct hex(rowid) "
            +" from act_a005r1 "
            + lsWhere1
            +" union all "
            +" select distinct hex(rowid) "
            +" from act_chkautopay "
            + lsWhere2
            +" )"
    ;

    pageQuery();

    if(this.sqlNotFind()){
      alertErr("此條件查無資料");
      return ;
    }

    wp.setListCount(0);
    wp.setPageValue();
  }

  //	@Override
//	public void queryRead() throws Exception {
//		wp.pageControl();
//		if (getWhereStr() == false) {
//			return;
//		}
//		wp.selectSQL = "" + "enter_acct_date, " + "wk_acct_key, " + "wk_idno, "
//				+ "chi_name, " + "curr_code, " + "wk_acct_no, "
//				+ "wk_autopay_id, " + "transaction_amt, " + "err_type, "
//				+ "Wk_curr_sort,acct_type ";
//
//		wp.daoTable = " (select enter_acct_date as enter_acct_date, acct_type||'-'||acct_key as wk_acct_key,"
//				+ " id||'-'||id_code as wk_idno, chi_name as chi_name, nvl(curr_code,'901') as curr_code,"
//				+ " substr(autopay_acct_no,4,11) as wk_acct_no, autopay_id as wk_autopay_id, transaction_amt as transaction_amt,"
//				+ " err_type as err_type, 1 as Wk_curr_sort,acct_type from act_a005r1"
//				+ " union all "
//				+ " select enter_acct_date as enter_acct_date, uf_acno_key(p_seqno) as wk_acct_key,"
//				+ " UF_IDNO_ID(id_p_seqno) as wk_idno, chi_name as chi_name, curr_code as curr_code,"
//				+ " decode(curr_code,nvl(curr_code,''),autopay_acct_no,'901',autopay_acct_no,'TWD',autopay_acct_no,dc_autopay_id ) as wk_acct_no,"
//				+ " decode(curr_code,nvl(curr_code,''),autopay_id,'901',autopay_id,'TWD',autopay_id,dc_autopay_id) as wk_autopay_id,"
//				+ " decode(curr_code,nvl(curr_code,''),transaction_amt,'901',transaction_amt,'TWD',transaction_amt,dc_transaction_amt) as transaction_amt,"
//				+ " '2' as err_type,  1 as Wk_curr_sort,acct_type from act_chkautopay where status_code='99' "
//				+ ") ";
//
//		wp.whereOrder = " order by enter_acct_date, wk_acct_key,Wk_curr_sort ";
//		if (strAction.equals("XLS")) {
//			select_noLimit();
//		}
//		pageQuery();
//		wp.setListCount(1);
//		if (sql_nrow <= 0) {
//			alertErr("此條件查無資料");
//			return;
//		}
//		wp.listCount[1] = wp.dataCnt;
//		wp.setPageValue();
//		list_wkdata();
//	}
/*
	void list_wkdata() throws Exception {
		int row_ct = wp.selectCnt;
		ArrayList<String> enter_acct_date_List = new ArrayList<String>();
		ArrayList<String> wk_acct_key_List = new ArrayList<String>();
		ArrayList<String> wk_idno_List = new ArrayList<String>();
		ArrayList<String> chi_name_List = new ArrayList<String>();
		ArrayList<String> curr_code_List = new ArrayList<String>();
		ArrayList<String> wk_acct_no_List = new ArrayList<String>();
		ArrayList<String> wk_autopay_id_List = new ArrayList<String>();
		ArrayList<Double> transaction_amt_List = new ArrayList<Double>();
		ArrayList<String> err_type_List = new ArrayList<String>();
		ArrayList<Integer> Wk_curr_sort_List = new ArrayList<Integer>();
		Map<String, Map<String, Double>> currPart = new HashMap<String, Map<String, Double>>();
		Map<String, Map<String, Double>> accNoPart = new HashMap<String, Map<String, Double>>();
		String keyTemp = "";
		String acc_type = "";
		Double valueTemp = 0D;
		Double sum = 0D;
		Wk_curr_sort = "";
		Map<String, Double> tempMap = null;
		// 將頁面資料用ArrayList先存起來
		for (int ii = 0; ii < row_ct; ii++) {
			wp.colSet(ii,"group_ct","1");
			enter_acct_date = wp.colStr(ii, "enter_acct_date");
			acc_type = wp.colStr(ii, "wk_acct_key").split("-")[0];
			Wk_curr_sort = wp.colStr(ii, "Wk_curr_sort");
			curr_code = wp.colStr(ii, "curr_code");
			wk_acct_no = wp.colStr(ii, "wk_acct_no");
			transaction_amt = Double.parseDouble(wp.colStr(ii,
					"transaction_amt"));
			sum += transaction_amt;
			// 幣別小計
			keyTemp = enter_acct_date + "_" + acc_type + "_" + Wk_curr_sort
					+ "_" + curr_code;
			tempMap = currPart.get(keyTemp);
			if (tempMap == null) {
				tempMap = new HashMap<String, Double>();
			}
			valueTemp = tempMap.get(wk_acct_no);
			if (valueTemp == null) {
				valueTemp = 0D;
			}
			tempMap.put(wk_acct_no, valueTemp + transaction_amt);
			currPart.put(keyTemp, tempMap);
			// 帳戶小計
			keyTemp = enter_acct_date + "_" + acc_type;
			tempMap = accNoPart.get(keyTemp);
			if (tempMap == null) {
				tempMap = new HashMap<String, Double>();
			}
			valueTemp = tempMap.get(wk_acct_no);
			if (valueTemp == null) {
				valueTemp = 0D;
			}
			tempMap.put(wk_acct_no, valueTemp + transaction_amt);
			accNoPart.put(keyTemp, tempMap);
			enter_acct_date_List.add(enter_acct_date);
			wk_acct_key_List.add(wp.colStr(ii, "wk_acct_key"));
			wk_idno_List.add(wp.colStr(ii, "wk_idno"));
			chi_name_List.add(wp.colStr(ii, "chi_name"));
			curr_code_List.add(curr_code);
			wk_acct_no_List.add(wk_acct_no);
			wk_autopay_id_List.add(wp.colStr(ii, "wk_autopay_id"));
			transaction_amt_List.add(transaction_amt);
			err_type_List.add(wp.colStr(ii, "err_type"));
			Wk_curr_sort_List.add(Integer.parseInt(Wk_curr_sort, 10));
		}
		for (int ii = 0; ii < row_ct; ii++) {
			wp.colSet(ii, "enter_acct_date", enter_acct_date_List.get(ii));
			wp.colSet(ii, "wk_acct_key", wk_acct_key_List.get(ii));
			wp.colSet(ii, "wk_idno", wk_idno_List.get(ii));
			wp.colSet(ii, "chi_name", chi_name_List.get(ii));
			wp.colSet(ii, "curr_code", curr_code_List.get(ii));
			wp.colSet(ii, "wk_acct_no", wk_acct_no_List.get(ii));
			wp.colSet(ii, "wk_autopay_id", wk_autopay_id_List.get(ii));
			wp.colSet(
					ii,
					"transaction_amt",
					num_2str(transaction_amt_List.get(ii), "###,###,###,##0.00"));
			wp.colSet(ii, "err_type", err_type_List.get(ii));
		}
		wp.colSet("ft_cnt", int_2Str(row_ct));
		wp.colSet("totalRank", "總計  筆數:" + int_2Str(row_ct) + " 小計 "
				+ num_2str(sum, "###,###,###,##0.00"));

	}
*/
  void xlsPrint() {
    // System.out.println("xlsPrint in ");
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      exDateS = wp.itemStr("exDateS");
      exDateE = wp.itemStr("exDateE");
      exCurrCode = wp.itemStr("curr_code");
      String ss = "IBM扣帳日： " + exDateS + "~" + exDateE + " 雙幣幣別 :"
              + exCurrCode;
      wp.colSet("cond_1", ss);
      // System.out.println("ss "+ss);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      queryFunc();
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
      System.out.println("ex:" + ex);
    }
  }
  void subTitle() {
    String exDate1 = "" , exDate2="" , exCurrCode ="" ;
    exDate1 = wp.itemStr("ex_date_S");
    exDate2 = wp.itemStr("ex_date_E");
    exCurrCode = wp.itemStr("ex_curr_code");
    if(exCurrCode.equals("901")) exCurrCode = "台幣";
    if(exCurrCode.equals("840")) exCurrCode = "美金";
    if(exCurrCode.equals("392")) exCurrCode = "日幣";
    String ss = "";
    ss += " IBM 扣帳日期 : ";
    ss += exDate1  ;
    ss += " - "+exDate2 ;
    ss += " 雙幣幣別 : " + exCurrCode;

    reportSubtitle = ss;

  }
  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    wp.pageRows = 9999;
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
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
      // 雙幣幣別
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_curr_code");
      dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
              "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id desc ");
    } catch (Exception ex) {
    }
  }

}
