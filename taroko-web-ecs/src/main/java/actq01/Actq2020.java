/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-05  V1.00.00  OrisChang  program initial                            *
 *                                                                            *
 * 111/10/24  V1.00.02  jiangyigndong  updated for project coding standard    *
 * 112-06-29  V1.00.03  Simon      批號繳款來源他行代償、還額檔繳款、全國繳費網繳款、花農卡自扣說明更新*
 ******************************************************************************/

package actq01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Actq2020 extends BaseEdit {
  CommString commString = new CommString();
  String mProgName = "actq2020";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
//			updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      /***
       wp.initOption = "--";
       wp.optionKey = wp.itemStr("ex_batch_no");
       this.dddw_list("dddw_batch_no", "act_pay_batch", "distinct substr(batch_no,9,4)",
       "substr(batch_no,9,4)||' ['||decode(substr(batch_no,9,4),'0000','支票繳款','0700','郵局劃撥','1002','它行自動繳款回饋'," +
       "'1003','它行臨櫃繳款','1005','債務協商入帳','1006','前置協商入帳','1007','更生統一收付','9001','ICBC 自動繳款回饋','9004','IBM 傳來繳款資料'," +
       "'9005','ATM 繳款手續費轉Payment','9006','誤入帳補正','9007','退貨或Reversal轉Payment','9008','各種基金轉Payment','9999','Dummy Record','')||']'",
       "where 1=1 order by 1 ");
       ***/

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_curr_code");
      this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
    } catch (Exception ex) {}
  }

  private boolean getWhereStr() throws Exception {

    wp.whereStr = " WHERE 1=1 ";
    if (empty(wp.itemStr("ex_bank")) == false) {
      wp.whereStr += " and substr(batch_no,9,4) = :ex_bank ";
      this.setString("ex_bank", wp.itemStr("ex_bank"));
    }
    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and decode(curr_code,'','901',curr_code) = :curr_code ";
      this.setString("curr_code", wp.itemStr("ex_curr_code"));
    }
    if (empty(wp.itemStr("ex_apr_typ")) == false) {
      if(wp.itemStr("ex_apr_typ").trim().equals("1"))
        wp.whereStr += " and decode(confirm_user,'',' ',confirm_user) <>' ' ";
      if(wp.itemStr("ex_apr_typ").trim().equals("2"))
        wp.whereStr += " and decode(confirm_user,'',' ',confirm_user) = ' ' ";
    }

    wp.whereOrder = " ORDER BY from_desc ,batch_no ASC ";
    //-page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = " batch_no,         " +
            " from_desc, "+
            " batch_tot_cnt,      " +
            " batch_tot_amt,      " +
	          " decode(substring(batch_no,9,4),'1001','他行代償',              " +
	          "                                '1002','他行自動繳款回饋',      " +
	          "                                '1003','同業代收繳款',          " +
	          "                                '1005','債務協商入帳',          " +
	          "                                '1006','前置協商入帳',          " +
	          "                                '5555','還額檔繳款',            " +
	          "                                '5556','全國繳費網繳款',        " +
	          "                                '9001','TCB 自動繳款回饋',      " +
	          "                                '9002','花農卡 自動繳款回饋',   " +
	          "                                '9007','退貨',                  " +
	          "                                '9999','Dummy Record') as bank_name , " +
            " trial_user,         " +
            " trial_date,         " +
            " trial_time,         " +
            " confirm_user,       " +
            " confirm_date,       " +
            " confirm_time,       " +
//					 " lpad(' ',20) as bank_name,        " +
            " dc_pay_amt,         " +
            " (case when dc_pay_amt > 0 then dc_pay_amt else batch_tot_amt end) wk_dc_amt, " +
            " decode(curr_code,'','901',curr_code) as curr_code ";

    wp.daoTable = "act_pay_batch";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String ss = "";
    double totCnt = 0, totAmt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      ss = wp.colStr(ii,"curr_code");
      wp.colSet(ii, "tt_curr_code", wfPtrsysidtabdesc(ss));

    //ss = wp.colStr(ii,"batch_no");
    //wp.colSet(ii, "bank_name", wfGetbankname(ss));

      totCnt += wp.colNum(ii,"batch_tot_cnt");
      totAmt += wp.colNum(ii,"wk_dc_amt");
    }
    wp.colSet("tot_cnt",totCnt+"");
    wp.colSet("tot_amt",totAmt+"");
  }

  String wfPtrsysidtabdesc(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select wf_desc from ptr_sys_idtab "
            + "where wf_type = 'DC_CURRENCY' and wf_id = :wf_id ";
    setString("wf_id", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) rtn = sqlStr("wf_desc");

    return rtn;
  }

  String wfGetbankname(String idcode) throws Exception {
    String rtn = "";
    String lsSource = "", lsBank = "";

    lsSource = commString.mid(idcode,8,4);
    lsBank   = commString.mid(idcode,12,3);

    if (eqIgno(lsSource,"1002")==false) return rtn;

    String ls_sql = "select bc_abname from ptr_bankcode "
            + "where bc_bankcode = :bankcode ";
    setString("bankcode", lsBank);
    sqlSelect(ls_sql);
    if (sqlRowNum > 0) rtn = sqlStr("bc_abname");

    return rtn;
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {
  }

  void pdfPrint() throws Exception {
    if (getWhereStr() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.reportId = mProgName;
    wp.pageRows =9999;

    String ss = "";
    String[] cde=new String[]{"1001","1002","1003","1005","1006","5555","5556",
            "9001","9002","9007","9999"};
    String[] txt=new String[]{"1001 [他行代償]","1002 [他行自動繳款回饋]","1003 [同業代收繳款]",
            "1005 [債務協商入帳]","1006 [前置協商入帳]",
            "5555 [還額檔繳款]","5556 [全國繳費網繳款]","9001 [TCB 自動繳款回饋]",
            "9002 [花農卡 自動繳款回饋]","9007 [退貨]","9999 [Dummy Record]"};

    if (empty(wp.itemStr("ex_bank")) == false) {
      ss += "繳款來源："+commString.decode(wp.itemStr("ex_bank"), cde, txt);
      ss += "        ";
    }
    if (empty(wp.itemStr("ex_curr_code")) == false) {
      ss += "結算幣別："+ wfPtrsysidtabdesc(wp.itemStr("ex_curr_code"))+" ("+wp.itemStr("ex_curr_code")+")";
    }
    wp.colSet("cond_1", ss);
    wp.colSet("reportName", mProgName.toUpperCase());
    wp.colSet("loginUser", wp.loginUser);
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
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
