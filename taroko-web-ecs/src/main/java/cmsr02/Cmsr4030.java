package cmsr02;
/** 
 * 20-0103:   Alex  usr_cname , dddw order by
 * 19-1022:   Ru    增加查詢條件、匯出PDF
 * 19-0614:   JH    p_xxx >>acno_p_xxx
 *109-04-28   shiyuqi       updated for project coding standard     * 
 *109-04-28   sunny       fix apr_date的判斷 * 
* 110-01-06  V1.00.03  shiyuqi       修改无意义命名 
* 112-10-22   sunny  fix  表格下方總計呈現的處理&身分證ID查詢條件修正。                                                                                  *    
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr4030 extends BaseAction implements InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsr4030")) {
        wp.optionKey = wp.colStr(0, "ex_curr_code");
        dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type = 'DC_CURRENCY' order by 1 Desc ");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_crt_date1")) && empty(wp.itemStr("ex_crt_date2"))
        && empty(wp.itemStr("ex_crt_user")) && empty(wp.itemStr("ex_acct_key"))
        && empty(wp.itemStr("ex_appr_no"))) {
      alertErr2("請輸入查詢條件");
      return;
    }


    // if(wp.item_ss("ex_acct_key").length()<8){
    // err_alert("身分證ID 至少8碼");
    // return;
    // }



    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("登錄日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = getWhereStr();
    sum(lsWhere);
    lsWhere = getWhereStr();
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " crt_user as create_id ," + " card_no ," + " acct_type ,"
        + " uf_acno_key2(p_seqno,debit_flag) as acct_key ," + " acct_code ,"
        + " (select chi_long_name from ptr_actcode where ptr_actcode.acct_code = cms_acaj.acct_code) as tt_acct_code , "
        + " reference_no as reference_seq ," + " adj_remark ,"
        + " uf_card_name(card_no) as db_cname ," + " p_seqno ," + " appr_no ,"
        + " apr_user as appr_id ," + " adj_check_memo ," + " uf_nvl(curr_code,'901') as curr_code ,"
        + " adj_amt ," + " uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as d_available_bal ,"
        + " d_avail_bal as tw_d_available_bal ";
    wp.daoTable = "cms_acaj ";
    wp.whereOrder = " order by crt_date, crt_time ";

    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();

  }

    private String getWhereStr() {
        String lsWhere = " where 1=1 "
                + sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
                + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
                + sqlCol(wp.itemStr("ex_appr_no"), "appr_no", "like%")
                + sqlCol(wp.itemStr("ex_crt_user"), "crt_user", "like%");

        if (wp.itemEq("ex_debit_flag", "N")) {
            lsWhere += " and debit_flag <>'Y' ";
            lsWhere += " and p_seqno in (select p_seqno from act_acno where 1=1 " + sqlCol(wp.itemStr("ex_acct_key"), "acct_key", "like%") + ") ";
        } else if (wp.itemEq("ex_debit_flag", "Y")) {
            lsWhere += " and debit_flag ='Y' ";
            lsWhere += " and p_seqno in (select p_seqno from dba_acno where 1=1 " + sqlCol(wp.itemStr("ex_acct_key"), "acct_key", "like%") + ") ";
        }

        if (wp.itemEq("ex_apr_flag", "Y")) {
            lsWhere += " and nvl(apr_date,'') <>''";
        } else if (wp.itemEq("ex_apr_flag", "N")) {
            lsWhere += " and nvl(apr_date,'')=''";
        }

        if (!empty(wp.itemStr("ex_curr_code"))) {
            lsWhere += " and uf_nvl(curr_code,'901') ='" + wp.itemStr("ex_curr_code") + "'";
        }
        if (!wp.itemEq("ex_acct_post_flag", "0")) {
            lsWhere += " and uf_nvl(acct_post_flag,'') ='" + wp.itemStr("ex_acct_post_flag") + "'";
        }
        
//        if (wp.itemEq("ex_acct_code01", "Y")) {
//            lsWhere += " and acct_code = 'RI'";
//        }
//        if (wp.itemEq("ex_acct_code02", "Y")) {
//            lsWhere += " and acct_code = 'PN'";
//        }
//        if (wp.itemEq("ex_acct_code03", "Y")) {
//            lsWhere += " and acct_code not in ('RI','PN')";
//        }
        
        if (wp.itemEq("ex_acct_code01", "Y")) {
        	lsWhere += " and acct_code = 'RI'";		
        }
        if (wp.itemEq("ex_acct_code02", "Y")) {
        	if (wp.itemEq("ex_acct_code01", "Y"))
            	lsWhere += " or acct_code = 'PN'";	
            	else
            	lsWhere += " and acct_code = 'PN'";	
        }
        if (wp.itemEq("ex_acct_code03", "Y")) {
        	if ((wp.itemEq("ex_acct_code01", "Y"))||(wp.itemEq("ex_acct_code02", "Y")))
            	lsWhere += " or acct_code not in ('RI','PN')";
            	else
            	lsWhere += " and acct_code not in ('RI','PN')";
        }
        return lsWhere;
    }

  void queryAfter() {
    String sql1 = "select usr_cname from sec_user where usr_id = ?";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "_" + wp.colStr(ii, "acct_key"));
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "create_id")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_create_id", sqlStr("usr_cname"));
      }
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "appr_id")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_appr_id", sqlStr("usr_cname"));
      }
    }
  }

  void sum(String lsWhere) {
    String[] cde = {"901", "392", "840"};
    String[] txt = {"台幣", "日幣", "美金"};

    int liSelectCnt = 0;
    String lsDesc = "", lsCurr = "", lsCnt = "", lsAmt = "", lsDesc1 = "", lsDesc2 = "";
    String sql1 = " select " + " adj_remark , " + " curr_code , " + " sum(adj_amt) as tl_adj_amt ,"
        + " count(*) as db_cnt " + " from cms_acaj " + lsWhere + " group by adj_remark , curr_code "
        + " order by adj_remark , curr_code Desc ";
    sqlSelect(sql1);
    liSelectCnt = sqlRowNum;
    if (liSelectCnt < 0)
      return;

    for (int ii = 0; ii < liSelectCnt; ii++) {
      if (ii > 0) {
        if ("PDF".equals(strAction)) {
          lsDesc += "\n";
          lsCurr += "\n";
          lsCnt += "\n";
          lsAmt += "\n";
          lsDesc1 += "\n";
          lsDesc2 += "\n";
        } else {
          lsDesc += "<br>";
          lsCurr += "<br>";
          lsCnt += "<br>";
          lsAmt += "<br>";
          lsDesc1 += "<br>";
          lsDesc2 += "<br>";
        }
      }
      lsDesc += "D檔原因: " + sqlStr(ii, "adj_remark");
      lsCurr += "幣別: " + commString.decode(sqlStr(ii, "curr_code"), cde, txt);
      lsDesc1 += "筆數: ";
      lsCnt += String.format("%,14.0f", sqlNum(ii, "db_cnt"));
      lsDesc2 += "總金額: ";
      lsAmt += String.format("%,14.2f", sqlNum(ii, "tl_adj_amt"));
    }
    wp.colSet("total_desc", lsDesc);
    wp.colSet("total_desc1", lsDesc1);
    wp.colSet("total_desc2", lsDesc2);
    wp.colSet("total_curr", lsCurr);
    wp.colSet("total_cnt", lsCnt);
    wp.colSet("total_amt", lsAmt);
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
    wp.reportId = "cmsr4030";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "登入日期: " + commString.strToYmd(wp.itemStr("ex_crt_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_crt_date2"));
    wp.colSet("cond1", cond1);
    wp.fileMode = "Y";
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    pdf.excelTemplate = "cmsr4030.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
