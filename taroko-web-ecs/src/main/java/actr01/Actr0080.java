/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-28  V1.00.00  Max Lin    program initial                            *
 * 107-07-25  V1.00.01  Alex       bug fixed                                  *
 * 108-12-11  V1.00.02  Alex       bug fix                                    *
 * 108-12-18  V1.00.03  Alex       bug fix								     *
 * 108-12-23  V1.00.04  Alex       p_seqno ->acno_p_seqno                     *
 * 109-04-15  V1.00.05  Alex       add auth_query							 *
 * 109-06-17  V1.00.06  Andy       update:Mantis3683 && status_code display   *
 * 109-07-17  V1.00.07  Andy       update:Mantis3683 && status_code display   *
 * 109-07-21  V1.00.08  Andy       update:Mantis3795                          *
 * 109-07-29  V1.00.09  Andy       update:Mantis3854                          *
 * 111/10/24  V1.00.10  jiangyigndong  updated for project coding standard    *
 * 111/12/28  V1.00.11  Ryan       *
 * 112/02/22  V1.00.12  Simon      1.sqlCol(x,x,x) 改寫成判斷 empty & setString(x,x)的方式*
 *                                 2.queryFunc()拿掉重複執行 getWhereStr()    *
 * 112/05/24  V1.00.13  Simon      queryRead()排除queryFunc()重複執行 getWhereStr()*
 * 112/07/04  V1.00.14  Ryan       修改存款不足選項、增加自行、花農選項                                                                         
 * 112/09/01  V1.00.15  Ryan       調整報表where 條件                 *
 * 112/10/02  V1.00.16  Ryan       移除存款不足查詢條件
 ******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;

public class Actr0080 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgname = "actr0080";

  String hsWhere = "";
  String condWhere = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
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

  private boolean getWhereStr() throws Exception {
    String exDateS = wp.itemStr("ex_date_S");
    String exDateE = wp.itemStr("ex_date_E");
    String exCurrCode = wp.itemStr("ex_curr_code");
    //String ex_id = wp.itemStr("ex_id");
    String exAcctKey = wp.itemStr("ex_acct_key");

    // if (empty(ex_date_S) == true && empty(ex_date_E) == true) {
    // alertErr("請輸入 IBM 扣帳日期");
    // return false;
    // }

    if (chkStrend(exDateS, exDateE) == false) {
      errmsg("IBM 扣帳日期 : 起迄錯誤");
      return false;
    }

    busi.func.ColFunc func = new busi.func.ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(wp.modPgm(), wp.itemStr("ex_acct_key")) != 1) {
      alertErr(func.getMsg());
      return false;
    }

    // 固定條件
    String lsWhere = " where 1=1 and A.err_type ='2' "
            + " and A.p_seqno = B.acno_p_seqno ";
//            + sqlCol(exDateS, "A.print_date", ">=")
//            + sqlCol(exDateE, "A.print_date", "<=")
//            + sqlCol(exCurrCode, "uf_nvl(A.curr_code,'901')");
    if(!empty(exDateS)) {
    	lsWhere += " and A.print_date >= :exDateS ";
    	setString("exDateS",exDateS);
    }
    
    if(!empty(exDateE)) {
    	lsWhere += " and A.print_date <= :exDateE ";
    	setString("exDateE",exDateE);
    }
    
    if(!empty(exCurrCode)) {
    	lsWhere += " and uf_nvl(A.curr_code,'901') = :exCurrCode ";
    	setString("exCurrCode",exCurrCode);
    }

    //if (wp.item_empty("ex_id") == false) {
    //	ls_where += " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 " + sqlCol(ex_id, "id_no") + ")";
    //}

    if(!wp.itemEmpty("ex_from_mark")) {
    	lsWhere += " and A.from_mark = :ex_from_mark ";
    	setString("ex_from_mark",wp.itemStr("ex_from_mark"));
    }
    
    if (empty(exAcctKey) == false) {
      String lsAcctKey = fillZeroAcctKey(exAcctKey);
      lsWhere += " and B.acct_key =:ex_acct_key ";
      setString("ex_acct_key", lsAcctKey);
    }

    if(wp.itemEmpty("ex_autopay_acct_no") == false) {
      //ls_where += sqlCol(wp.itemStr("ex_autopay_acct_no"),"a.autopay_acct_no","%like%");
      lsWhere += " and a.autopay_acct_no like :autopay_acct_no ";
      setString("autopay_acct_no"   , '%'+wp.itemStr("ex_autopay_acct_no")+'%');
    }
//    if (wp.itemEq("ex_prt", "0")) {
//      lsWhere += " and A.TRANSACTION_AMT = 0 ";
//    } 

    if (wp.itemEq("ex_vip", "N")) {
      lsWhere += " and B.vip_code  ='' ";
    } else if (wp.itemEq("ex_vip", "Y")) {
      lsWhere += " and B.vip_code  <>'' ";
    } else if (wp.itemEq("ex_vip", "T")) {
      lsWhere += " and exists ( select 1 from crd_card where p_seqno = B.acno_p_seqno and card_note='I' and current_code ='0') ";
    }

    //list_sum(ls_where);
    hsWhere = lsWhere;
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;

    listSum(hsWhere);//執行 pageQuery()或sqlSelect(sql) 後，setString 設定的參數會清掉，之後要用需再執行 setString
    getWhereStr();

    wp.whereStr = hsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  void listSum(String ls_where) throws Exception {
    String sql1 = " select "
            + " count(*) as tl_rows , "
            + " sum(A.transaction_amt) as tl_amt "
            + " from act_a005r1 A, act_acno B "
            + ls_where;

    sqlSelect(sql1);

    wp.colSet("tl_rows", sqlStr("tl_rows"));
    wp.colSet("tl_amt", sqlStr("tl_amt"));

  }

  @Override
  public void queryRead() throws Exception {

    if (!strAction.equals("Q")) {
      if (getWhereStr() == false)
        return;
    }

    wp.pageControl();

    wp.selectSQL = ""
            + "A.print_date, "
            + "A.acct_type, "
            // + "A.acct_key, "
            + "B.acct_key, "
            + "A.autopay_acct_no, "
            + "A.autopay_id, "
            + "A.transaction_amt, "
            + "A.from_mark, "
            + "A.status_code, "
            + "A.err_type, "
            + "A.autopay_id_code, "
            //+ " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = A.id_p_seqno) as id, "
            + "(case when b.acno_flag = '2' then substr(b.acct_key,1,8) "
            + " when b.acno_flag = '3' then (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = a.id_p_seqno) "
            + " else substr(b.acct_key,1,10)||'-'||substr(b.acct_key,11,1) end) as id, "
            //+ "A.chi_name, "
            + "(case when b.acno_flag = '2' then uf_corp_name(b.corp_p_seqno) "
            + " else uf_idno_name(a.id_p_seqno) end) as chi_name, "
            + "A.cellphone_no, "
            + "uf_nvl(A.curr_code,'901') as curr_code, "
            + "A.p_seqno, "
            + "A.act_no_l, "
            + "A.amt_1, "
            + "A.curr_rate, "
            + "A.deduct_amt_1, "
            + "A.deduct_amt_2, "
            + "B.stmt_cycle, "
            + "B.vip_code, "
            + "decode(B.vip_code,'','','*') as vip, "
            + "decode(A.cellphone_no,'','N',decode(length(A.cellphone_no),10,'Y','N')) as wk_sms_flag ";
    //wp.daoTable = " act_a005r1 A left join act_acno B on A.p_seqno = B.acno_p_seqno ";
    wp.daoTable = " act_a005r1 A, act_acno B ";
    wp.whereOrder = " order by A.print_date, A.acct_type , B.stmt_cycle ,id ";

    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() throws Exception {
    double lsDoubleAmt = 0;
    String lsSql="",wkStatusCode="";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // if(wp.colStr(ii,"mod_pgm").equals("act_a105")) {
      if ((wp.colStr(ii, "curr_code").equals("840") && wp.colNum(ii, "curr_rate") > 1000) ||
              (wp.colStr(ii, "curr_code").equals("392") && wp.colNum(ii, "curr_rate") > 10)) {
        lsDoubleAmt = wp.colNum(ii, "amt_1") * 100.0;
        lsDoubleAmt = convAmt(lsDoubleAmt);
        wp.colSet(ii, "amt_1", lsDoubleAmt);

        lsDoubleAmt = wp.colNum(ii, "curr_rate") / 100;
        lsDoubleAmt = convAmt(lsDoubleAmt);
        wp.colSet(ii, "curr_rate", lsDoubleAmt);
      }

      //20200617 退件原因改用ptr_sys_idtab參數對照
//			if (wp.col_eq(ii, "status_code", "00")) {
//				wp.colSet(ii, "tt_status_code", "00.入/ 扣帳成功");
//			} else if (wp.col_eq(ii, "status_code", "01")) {
//				wp.colSet(ii, "tt_status_code", "01.存款不足");
//			} else if (wp.col_eq(ii, "status_code", "02")) {
//				wp.colSet(ii, "tt_status_code", "02.非委託代繳/ 代發戶");
//			} else if (wp.col_eq(ii, "status_code", "03")) {
//				wp.colSet(ii, "tt_status_code", "03.中止委託代繳/ 代發戶");
//			} else if (wp.col_eq(ii, "status_code", "04")) {
//				wp.colSet(ii, "tt_status_code", "04.存戶查核資料錯誤");
//			} else if (wp.col_eq(ii, "status_code", "05")) {
//				wp.colSet(ii, "tt_status_code", "05.無此帳號");
//			} else if (wp.col_eq(ii, "status_code", "06")) {
//				wp.colSet(ii, "tt_status_code", "06.帳號結清銷戶");
//			} else if (wp.col_eq(ii, "status_code", "07")) {
//				wp.colSet(ii, "tt_status_code", "07.存款遭強制執行無法代繳");
//			} else if (wp.col_eq(ii, "status_code", "98")) {
//				wp.colSet(ii, "tt_status_code", "98.其他");
//			} else if (wp.col_empty(ii, "status_code")) {
//				wp.colSet(ii, "tt_status_code", "  _尚未回寫扣款結果");
//			} else if (wp.col_eq(ii, "status_code", "0000")) {
//				wp.colSet(ii, "tt_status_code", "0000.扣款金額等於應繳金額");
//			} else if (wp.col_eq(ii, "status_code", "0001")) {
//				wp.colSet(ii, "tt_status_code", "0001.扣款金額小於應繳金額大於mp");
//			} else if (wp.col_eq(ii, "status_code", "0002")) {
//				wp.colSet(ii, "tt_status_code", "0002.扣款金額小於mp但不同意兌換");
//			} else if (wp.col_eq(ii, "status_code", "0003")) {
//				wp.colSet(ii, "tt_status_code", "0003.經兌換扣足mp");
//			} else if (wp.col_eq(ii, "status_code", "0004")) {
//				wp.colSet(ii, "tt_status_code", "0004.經兌換仍不足mp");
//			} else if (wp.col_eq(ii, "status_code", "0005")) {
//				wp.colSet(ii, "tt_status_code", "0005.扣款金額小於mp且兌換不成功");
//			} else if (wp.col_eq(ii, "status_code", "1001")) {
//				wp.colSet(ii, "tt_status_code", "1001.身分證號空白");
//			} else if (wp.col_eq(ii, "status_code", "1002")) {
//				wp.colSet(ii, "tt_status_code", "1002.生日不為數字或為零");
//			} else if (wp.col_eq(ii, "status_code", "1003")) {
//				wp.colSet(ii, "tt_status_code", "1003.幣別錯誤或為空白");
//			} else if (wp.col_eq(ii, "status_code", "1004")) {
//				wp.colSet(ii, "tt_status_code", "1004.外幣扣帳帳號空白或 APCODE 不是 05,53,57,58");
//			} else if (wp.col_eq(ii, "status_code", "1005")) {
//				wp.colSet(ii, "tt_status_code", "1005.應繳金額不為數字或0");
//			} else if (wp.col_eq(ii, "status_code", "1006")) {
//				wp.colSet(ii, "tt_status_code", "1006.mp不為數字(格式有誤)");
//			} else if (wp.col_eq(ii, "status_code", "1007")) {
//				wp.colSet(ii, "tt_status_code", "1007.台幣帳號有誤, APCODE 為 05,53,57,58");
//			} else if (wp.col_eq(ii, "status_code", "1008")) {
//				wp.colSet(ii, "tt_status_code", "1008.扣款日期錯誤");
//			} else if (wp.col_eq(ii, "status_code", "1009")) {
//				wp.colSet(ii, "tt_status_code", "1009.外幣ID/帳號不一致");
//			} else if (wp.col_eq(ii, "status_code", "1010")) {
//				wp.colSet(ii, "tt_status_code", "1010.台幣ID/帳號不一致");
//			} else if (wp.col_eq(ii, "status_code", "2002")) {
//				wp.colSet(ii, "tt_status_code", "2002.扣款金額為0且不同意兌換");
//			} else if (wp.col_eq(ii, "status_code", "2005")) {
//				wp.colSet(ii, "tt_status_code", "2005.扣款金額為0且兌換失敗");
//			} else if (wp.col_eq(ii, "status_code", "0006")) {
//				wp.colSet(ii, "tt_status_code", "0006.扣款金額小於mp且兌換大於NT50萬");
//			} else if (wp.col_eq(ii, "status_code", "2006")) {
//				wp.colSet(ii, "tt_status_code", "2006.扣款金額為0且兌換大於NT50萬");
//			}
      if(empty(wp.colStr(ii, "status_code"))){
        wp.colSet(ii, "tt_status_code","");
      } else {
        wkStatusCode = wp.colStr(ii, "status_code");
        lsSql = "select wf_desc "
                + "from ptr_sys_idtab "
                + "where wf_type = 'ACT_STATUS' "
                + "and wf_id =:wf_id ";
        setString("wf_id",wkStatusCode);
        sqlSelect(lsSql);
        if(sqlRowNum > 0){
          wp.colSet(ii, "tt_status_code",sqlStr("wf_desc"));
        } else {
          wp.colSet(ii, "tt_status_code",wkStatusCode);
        }
      }
    }
  }

  /***********************************************************************/
  public double convAmt(double cvt_amt) throws Exception {
    long cvtLong = (long) Math.round(cvt_amt * 100.0 + 0.000001);
    double cvtDouble = ((double) cvtLong) / 100;
    return cvtDouble;
  }

  void xlsPrint() {
    // try {
    // log("xlsFunction: started--------");
    // wp.reportId = m_progName;
    //
    // // -cond-
    // String ex_date_S = wp.itemStr("ex_date_S");
    // String ex_date_E = wp.itemStr("ex_date_E");
    // String ex_prt = wp.itemStr("ex_prt");
    // String ex_vip = wp.itemStr("ex_vip");
    // String ex_curr_code = wp.itemStr("ex_curr_code");
    // String ex_amt_ok = wp.itemStr("ex_amt_ok");
    // String ex_id = wp.itemStr("ex_id");
    //
    // String cond_1 = "IBM 扣帳日期: " + ex_date_S + " ~ " + ex_date_E + "
    // 存款不足:" + ex_prt + " 身份別:" + ex_vip;
    // String cond_2 = "雙幣幣別: " + ex_curr_code + " OK報表:" + ex_amt_ok + "
    // 身分證號:" + ex_id;
    // wp.colSet("cond_1", cond_1);
    // wp.colSet("cond_2", cond_2);
    //
    // // ===================================
    // TarokoExcel xlsx = new TarokoExcel();
    // wp.fileMode = "N";
    // xlsx.excelTemplate = m_progName + ".xlsx";
    //
    // //====================================
    // xlsx.sheetName[0] ="自行自動扣繳成功失敗明細表";
    // queryFunc();
    // wp.setListCount(1);
    // log("Summ: rowcnt:" + wp.listCount[1]);
    // xlsx.processExcelSheet(wp);
    //
    // xlsx.outputExcel();
    // xlsx = null;
    // log("xlsFunction: ended-------------");
    //
    // } catch (Exception ex) {
    // wp.expMethod = "xlsPrint";
    // wp.expHandle(ex);
    // }
  }

  void pdfPrint() throws Exception {
    // wp.reportId = m_progName;
    // // -cond-
    // String ss = "PDFTEST: ";
    // wp.colSet("cond_1", ss);
    // wp.pageRows = 9999;
    //
    // queryFunc();
    // // wp.setListCount(1);
    //
    // TarokoPDF pdf = new TarokoPDF();
    // wp.fileMode = "N";
    // pdf.excelTemplate = m_progName + ".xlsx";
    // pdf.sheetNo = 0;
    // pdf.pageCount = 30;
    // pdf.procesPDFreport(wp);
    //
    // pdf = null;
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

  String fillZeroAcctKey(String acctkey) throws Exception {
    String rtn = acctkey;
    if (acctkey.trim().length() == 8)
      rtn += "000";
    if (acctkey.trim().length() == 10)
      rtn += "0";

    return rtn;
  }

}
