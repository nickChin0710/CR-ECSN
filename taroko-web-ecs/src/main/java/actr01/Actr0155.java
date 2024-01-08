/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-10-20  V1.00.00         		program initial                           *
 * 107-07-26  V1.00.01    Alex      bug fixed                                 *
 * 110-03-04  v1.00.02  Andy       Update PDF隠碼作業                                                                      *
 * 111/10/24  V1.00.03  jiangyigndong  updated for project coding standard    *
 ******************************************************************************
 *   ����۰ʦ�ú���M�U                                                                                                                                        *
 ******************************************************************************/
package actr01;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0155 extends BaseReport {

  String exDateS = "", exDateE = "", ex_bank = "";
  String lsWhere1 = "" , lsWhere2 = "";
  String enterAcctDate = "";
  String wkAcctKey = "", wkIdno;
  String chiName = "";
  String stmtCycle = "";
  String acctBank = "";
  String wkAcctNo = "", wkAutopayId = "";
  double transactionAmt = 0;
  String errType = "";
  String gpCurrCnt, gpAcctCnt = "", gpTotCnt = "";
  String WkCurrSort;

  InputStream inExcelFile = null;
  String mProgName = "actr0155";

  String condWhere = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp="
            + wp.respCode + ",rHtml=" + wp.respHtml);
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
  public void initPage() {
    //設定初始搜尋條件值
    wp.colSet("ex_date_S", getSysDate());
    wp.colSet("ex_date_E", getSysDate());
  }

  @Override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
  }

  private boolean getWhereStr() throws Exception {
    String exDateS = wp.itemStr2("ex_date_S");
    String exDateE = wp.itemStr2("ex_date_E");
    String exBank  = wp.itemStr2("ex_bank_id");

    if (empty(exDateS) == true && empty(exDateE) == true) {
      alertErr2("請輸入提出/扣款日期");
      return false;
    }

    if(this.chkStrend(exDateS, exDateE)==false){
      alertErr2("提出/扣款 日期 : 起迄錯誤 !");
      return false;
    }

    //固定條件

    lsWhere1 = " where A.err_type < '2' "
            + sqlCol(exDateS,"A.enter_acct_date",">=")
            + sqlCol(exDateE,"A.enter_acct_date","<=")
    ;

    //ls_where2 = " where A.status_code = '99' "
    lsWhere2 = " where A.status_code in ('99','XX') "
            + sqlCol(exDateS,"A.enter_acct_date",">=")
            + sqlCol(exDateE,"A.enter_acct_date","<=")
    ;

    if(eqIgno(commString.mid(exBank, 0,3),"700")){
      lsWhere1 += " and A.acct_bank like '700%' ";
      lsWhere2 += " and A.acct_bank like '700%' ";
    }	else	{
      //ls_where1 += sql_col(exBank,"A.acct_bank");
      //ls_where2 += sql_col(exBank,"A.acct_bank");
      lsWhere1 += sqlCol(commString.mid(exBank, 0,3),"A.acct_bank","like%");
      lsWhere2 += sqlCol(commString.mid(exBank, 0,3),"A.acct_bank","like%");
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false) {
      return;
    }
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false) {
      return;
    }

    wp.sqlCmd = ""
            + " select "
            + " A.acct_bank , "
            + " A.enter_acct_date , "
            + " A.p_seqno , "
            //+ " A.acct_type||'-'||A.acct_key as wk_acct_key ,"
            //+ " A.acct_type||'-'||uf_acno_key(A.p_seqno) as wk_acct_key ,"
            + " A.acct_type||'-'||A1.acct_key as wk_acct_key ,"
            + " A.acct_type||'-'||uf_hi_idno(substr(A1.acct_key,1,10))||substr(A1.acct_key,11,1) as db_hi_acct_key ,"
            + " A.stmt_cycle ,"
            //+ " uf_idno_id(A.id_p_seqno) as id_no ,"
            + " C1.id_no||'-'||C1.id_no_code as id_no ,"
            + " uf_hi_idno(C1.id_no)||'-'||C1.id_no_code as db_hi_id_no ,"
            + " A.chi_name ,"
            + " uf_hi_cname(A.chi_name) as db_hi_chi_name ,"
            + " A.autopay_acct_no ,"
            + " uf_hi_acctno(A.autopay_acct_no) as db_hi_autopay_acct_no ,"
            + " A.transaction_amt ,"
            + " A.autopay_id ,"
            + " uf_hi_idno(A.autopay_id) as db_hi_autopay_id ,"
            //+ " (select bank_name from act_ach_bank where substr(bank_no,1,3) =substr(A.acct_bank,1,3)) as acct_bank_name ,"
            //+ " B.bank_name as acct_bank_name, " //for UI
            + " decode(B.bank_name,NULL,A.acct_bank,B.bank_name) as acct_bank_name, " //for UI
            + " B.bank_no||' '||B.bank_name as bank_id_name, " //for PDF
            + " decode(A.err_type,'0','提回銷帳檔多','1','已updated') as err_type "
            + " from act_b002r1 A "
            //+ " left join act_ach_bank as B on substr(A.acct_bank,1,3) = substr(B.bank_no,1,3) "
            + " left join act_ach_bank as B on decode(A.acct_bank,'700','7000000',A.acct_bank) = B.bank_no "
            + " left join act_acno as A1 on A1.acno_p_seqno = A.p_seqno "
            + " left join crd_idno as C1 on C1.id_p_seqno = A.id_p_seqno "
            + lsWhere1
            + " union all "
            + " select "
            + " A.acct_bank , "
            + " A.enter_acct_date ,"
            + " A.p_seqno ,"
            //+ " A.acct_type||'-'||uf_acno_key(A.p_seqno) as wk_acct_key ,"
            + " A.acct_type||'-'||A1.acct_key as wk_acct_key ,"
            + " A.acct_type||'-'||uf_hi_idno(substr(A1.acct_key,1,10))||substr(A1.acct_key,11,1) as db_hi_acct_key ,"
            + " A.stmt_cycle ,"
            //+ " uf_idno_id(A.id_p_seqno) as id_no ,"
            + " C1.id_no||'-'||C1.id_no_code as id_no ,"
            + " uf_hi_idno(C1.id_no)||'-'||C1.id_no_code as db_hi_id_no ,"
            + " A.chi_name ,"
            + " uf_hi_cname(A.chi_name) as db_hi_chi_name ,"
            + " A.autopay_acct_no ,"
            + " uf_hi_acctno(A.autopay_acct_no) as db_hi_autopay_acct_no ,"
            + " A.transaction_amt ,"
            + " A.autopay_id ,"
            + " uf_hi_idno(A.autopay_id) as db_hi_autopay_id ,"
            //+ " (select bank_name from act_ach_bank where substr(bank_no,1,3) =substr(A.acct_bank,1,3)) as acct_bank_name ,"
            //+ " B.bank_name as acct_bank_name, " //for UI
            + " decode(B.bank_name,NULL,A.acct_bank,B.bank_name) as acct_bank_name, " //for UI
            + " B.bank_no||' '||B.bank_name as bank_id_name, " //for PDF
            + " '提出扣款檔多' as err_type "
            + " from act_other_apay A "
            //+ " left join act_ach_bank as B on substr(A.acct_bank,1,3) = substr(B.bank_no,1,3) "
            + " left join act_ach_bank as B on decode(A.acct_bank,'700','7000000',A.acct_bank) = B.bank_no "
            + " left join act_acno as A1 on A1.acno_p_seqno = A.p_seqno "
            + " left join crd_idno as C1 on C1.id_p_seqno = A.id_p_seqno "
            + lsWhere2
            + " order by bank_id_name , 2 , 3 "
    ;

    /***
     wp.pageCount_sql =""
     +"select count(*) from ( "
     +" select distinct hex(rowid) "
     +" from act_b002r1 A "
     +ls_where1
     +" union all "
     +" select distinct hex(rowid) "
     +" from act_other_apay A "
     +ls_where2
     +" )"
     ;
     ***/
    wp.pageCountSql =""
            +"select count(*) from ( "
            + wp.sqlCmd
            +" )"
    ;

    pageQuery();

    if(this.sqlNotFind()){
      alertErr2("此條件查無資料");
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
//				+ "chi_name, " + "stmt_cycle, " + "wk_acct_no, "
//				+ "wk_autopay_id, " + "transaction_amt, " + "err_type, acct_bank, "
//				+ "Wk_curr_sort,acct_type ";
//
//		wp.daoTable = " ("
//				+ " select enter_acct_date as enter_acct_date, acct_type||'-'||acct_key as wk_acct_key, stmt_cycle as stmt_cycle,"
//				+ " id_no||'-'||id_code as wk_idno, chi_name as chi_name,"
//				+ " autopay_acct_no as wk_acct_no, autopay_id as wk_autopay_id, transaction_amt as transaction_amt,"
//				+ " err_type as err_type, acct_bank, 1 as Wk_curr_sort,acct_type from act_b002r1 where 1 =1 and err_type < '2' "
//				+ " union all "
//				+ " select enter_acct_date as enter_acct_date, uf_acno_key(p_seqno) as wk_acct_key, stmt_cycle as stmt_cycle,"
//				+ " UF_IDNO_ID(id_p_seqno) as wk_idno, chi_name as chi_name,"
//				+ " autopay_acct_no as wk_acct_no, autopay_id as wk_autopay_id, transaction_amt as transaction_amt,"
//				+ " '2' as err_type, acct_bank, 1 as Wk_curr_sort,acct_type from act_other_apay where 1 =1 and status_code='99' "
//				+ ") ";
//
//		wp.whereOrder = " order by acct_bank,enter_acct_date, wk_acct_key ";
//		if (is_action.equals("XLS")) {
//			select_noLimit();
//		}
////		pageQuery();
////		wp.setListCount(1);
//		if (sql_nrow <= 0) {
//			err_alert("此條件查無資料");
//			return;
//		}
//		wp.listCount[1] = wp.dataCnt;
//		wp.setPageValue();
//		list_wkdata();
//	}

//	void list_wkdata() throws Exception {
//		int row_ct = wp.selectCnt;
//		ArrayList<String> acct_bank_List = new ArrayList<String>();
//		ArrayList<String> enter_acct_date_List = new ArrayList<String>();
//		ArrayList<String> wk_acct_key_List = new ArrayList<String>();
//		ArrayList<String> stmt_cycle_List = new ArrayList<String>();
//		ArrayList<String> wk_idno_List = new ArrayList<String>();
//		ArrayList<String> chi_name_List = new ArrayList<String>();
//		ArrayList<String> wk_acct_no_List = new ArrayList<String>();
//		ArrayList<String> wk_autopay_id_List = new ArrayList<String>();
//		ArrayList<Double> transaction_amt_List = new ArrayList<Double>();
//		ArrayList<String> err_type_List = new ArrayList<String>();
//		ArrayList<Integer> Wk_curr_sort_List = new ArrayList<Integer>();
//		Map<String, Map<String, Double>> currPart = new HashMap<String, Map<String, Double>>();
//		Map<String, Map<String, Double>> accNoPart = new HashMap<String, Map<String, Double>>();
//		String keyTemp = "";
//		String acc_type = "";
//		Double valueTemp = 0D;
//		Double sum = 0D;
//		Wk_curr_sort = "";
//		Map<String, Double> tempMap = null;
//		// �N������ƥ�ArrayList���s�_��
//		for (int ii = 0; ii < row_ct; ii++) {
//			acct_bank = wp.col_ss(ii, "acct_bank");
//			enter_acct_date = wp.col_ss(ii, "enter_acct_date");
//			acc_type = wp.col_ss(ii, "wk_acct_key").split("-")[0];
//			Wk_curr_sort = wp.col_ss(ii, "Wk_curr_sort");
//			wk_acct_no = wp.col_ss(ii, "wk_acct_no");
//			transaction_amt = Double.parseDouble(wp.col_ss(ii,
//					"transaction_amt"));
//			sum += transaction_amt;
//			// �Ȧ�O�p�p
//			keyTemp = acct_bank;
//			tempMap = currPart.get(keyTemp);
//			if (tempMap == null) {
//				tempMap = new HashMap<String, Double>();
//			}
//			valueTemp = tempMap.get(acct_bank);
//			if (valueTemp == null) {
//				valueTemp = 0D;
//			}
//			tempMap.put(acct_bank, valueTemp + transaction_amt);
//			currPart.put(keyTemp, tempMap);
//
//			// �b��p�p
//			/*
//			keyTemp = enter_acct_date + "_" + acc_type;
//			tempMap = accNoPart.get(keyTemp);
//			if (tempMap == null) {
//				tempMap = new HashMap<String, Double>();
//			}
//			valueTemp = tempMap.get(wk_acct_no);
//			if (valueTemp == null) {
//				valueTemp = 0D;
//			}
//			tempMap.put(wk_acct_no, valueTemp + transaction_amt);
//			accNoPart.put(keyTemp, tempMap);
//			*/
//			acct_bank_List.add(acct_bank);
//			enter_acct_date_List.add(enter_acct_date);
//			wk_acct_key_List.add(wp.col_ss(ii, "wk_acct_key"));
//			wk_idno_List.add(wp.col_ss(ii, "wk_idno"));
//			chi_name_List.add(wp.col_ss(ii, "chi_name"));
//			wk_acct_no_List.add(wk_acct_no);
//			wk_autopay_id_List.add(wp.col_ss(ii, "wk_autopay_id"));
//			transaction_amt_List.add(transaction_amt);
//			err_type_List.add(wp.col_ss(ii, "err_type"));
//			Wk_curr_sort_List.add(Integer.parseInt(Wk_curr_sort, 10));
//		}
//		for (int ii = 0; ii < row_ct; ii++) {
//			wp.col_set(ii, "acct_bank", acct_bank_List.get(ii));
//			wp.col_set(ii, "enter_acct_date", enter_acct_date_List.get(ii));
//			wp.col_set(ii, "wk_acct_key", wk_acct_key_List.get(ii));
//			wp.col_set(ii, "wk_idno", wk_idno_List.get(ii));
//			wp.col_set(ii, "chi_name", chi_name_List.get(ii));
//			wp.col_set(ii, "wk_acct_no", wk_acct_no_List.get(ii)); //autopay_no
//			wp.col_set(ii, "wk_autopay_id", wk_autopay_id_List.get(ii));
//			wp.col_set(
//					ii,
//					"transaction_amt",
//					num_2str(transaction_amt_List.get(ii), "###,###,###,##0.00"));
//			wp.col_set(ii, "err_type", err_type_List.get(ii));
//		}
//		wp.col_set("ft_cnt", int_2Str(row_ct));
//		wp.col_set("totalRank", "�`�p  ����:" + int_2Str(row_ct) + " �p�p "
//				+ num_2str(sum, "###,###,###,##0.00"));
//
//	}

  void xlsPrint() {
    // System.out.println("xlsPrint in ");
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      exDateS = wp.itemStr2("exDateS");
      exDateE = wp.itemStr2("exDateE");
      ex_bank = wp.itemStr2("exBank");
      String ss = "���X/���ڤ�G " + exDateS + "~" + exDateE + "  ��ú�Ȧ� :"
              + ex_bank;
      wp.colSet("cond_1", ss);
      // System.out.println("ss "+ss);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -����-
      xlsx.sheetName[0] = "����";
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

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
    //String ss = "PDFTEST: ";
    //wp.col_set("cond_1", ss);
    String exDateS = wp.itemStr2("ex_date_S");
    String exDateE = wp.itemStr2("ex_date_E");
    String exBank    = wp.itemStr2("ex_bank_id");
    String cond1 = "提出/扣款日期: " + exDateS + " ~ " + exDateE;
    String cond2 = "銀行代號: " + exBank;
    wp.colSet("cond_1", cond1);
    wp.colSet("cond_2", cond2);
    wp.pageRows = 9999;
    queryFunc();
    wp.colSet("loginUser", wp.loginUser);
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
      // 扣繳銀行
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_bank_id");
      dddwList("dddw_bank_id", "act_ach_bank", "bank_no", "bank_name", "where 1=1 order by bank_no");
    } catch (Exception ex) {
    }
  }

}
