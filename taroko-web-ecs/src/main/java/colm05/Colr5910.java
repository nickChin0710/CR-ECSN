package colm05;
/**
 * 19-1212:   Alex  fix MS950
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 2019-0424:  JH    *.csv
 * 109-05-06  V1.00.03  Tanwei       updated for project coding standard
 * * 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
 */

import ofcapp.BaseQuery;
import ofcapp.InfaceExcel;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoFileAccess;

public class Colr5910 extends BaseQuery implements InfaceExcel {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
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
      // xlsPrint();
      dataExport();
    }
    dddwSelect();
    initButton();

  }

  private void dataExport() throws Exception {
    wp.pageRows = 9999;
    queryFunc();
    if (wp.selectCnt <= 0) {
      return;
    }

    String lsFile = "colr5910_u" + wp.loginUser + "_" + wp.sysTime + ".csv";
    TarokoFileAccess oofile = new TarokoFileAccess(wp);

    int liFileNum = oofile.openOutputText(lsFile, "MS950");
    // 帳戶帳號 中文姓名 不可凍結 不可解凍 不可調高信用額度 不可調低信用額度 不可強制停用 不可調高信用額度(預借現金) 原因碼 有無有效卡 欠款金額
    String list = "帳戶帳號,中文姓名,不可凍結,不可解凍,不可調高信用額度,不可調低信用額度,不可強制停用,不可調高信用額度(預借現金),原因碼,有無有效卡,欠款金額";
    oofile.writeTextFile(liFileNum, list + wp.newLine);
    for (int ll = 0; ll < wp.selectCnt; ll++) {
      // {wk_acct_key} {chi_name} {no_block_flag} {no_unblock_flag} {no_adj_loc_high}
      // {no_adj_loc_low}
      // {no_f_stop_flag} {no_adj_loc_high_cash} {spec_reason} {tt_card_00_cnt}
      // {acct_jrnl_bal.(999)}
      list = wp.colStr(ll, "wk_acct_key") + "," + commString.hideIdnoName(wp.colStr(ll, "chi_name")) + ","
          + wp.colStr(ll, "no_block_flag") + "," + wp.colStr(ll, "no_unblock_flag") + ","
          + wp.colStr(ll, "no_adj_loc_high") + "," + wp.colStr(ll, "no_adj_loc_low") + ","
          + wp.colStr(ll, "no_f_stop_flag") + "," + wp.colStr(ll, "no_adj_loc_high_cash") + ","
          + wp.colStr(ll, "spec_reason") + "," + wp.colStr(ll, "tt_card_00_cnt") + ","
          + wp.colStr(ll, "acct_jrnl_bal") + wp.newLine;
      oofile.writeTextFile(liFileNum, list);
    }
    oofile.closeOutputText(liFileNum);
    wp.setDownload(lsFile);
  }

  @Override
  public void queryFunc() throws Exception {
    String lsAcctKey = wp.itemStr("ex_acct_key");
    // if(!empty(ls_acct_key)){
    // ls_acct_key = commString.acct_key(ls_acct_key);
    // if(ls_acct_key.length()!=11){
    // err_alert("身分證ID / 統編 輸入錯誤");
    // return ;
    // }
    // }

    wp.whereStr = " where 1=1 " + sqlCol(lsAcctKey, "B.acct_key", "like%");

    if (wp.itemEq("ex_no_block", "") && wp.itemEq("ex_no_unblock", "")
        && wp.itemEq("ex_no_adj_loc_high", "") && wp.itemEq("ex_no_adj_loc_low", "")
        && wp.itemEq("ex_no_f_stop", "") && wp.itemEq("ex_no_adj_h_cash", "")) {
    } else {
      wp.whereStr += " and (1=2";
      if (wp.itemEq("ex_no_block", "Y"))
        wp.whereStr +=
            " or (A.no_block_flag ='Y' and to_char(sysdate,'yyyymmdd') between B.no_block_s_date and uf_nvl(B.no_block_e_date,'99991231')) ";

      if (wp.itemEq("ex_no_unblock", "Y"))
        wp.whereStr +=
            " or (A.no_unblock_flag ='Y' and to_char(sysdate,'yyyymmdd') between B.no_unblock_s_date and uf_nvl(B.no_unblock_e_date,'99991231')) ";

      if (wp.itemEq("ex_no_adj_loc_high", "Y"))
        wp.whereStr +=
            " or (A.no_adj_loc_high ='Y' and to_char(sysdate,'yyyymmdd') between B.no_adj_loc_high_s_date and uf_nvl(B.no_adj_loc_high_e_date,'99991231'))";

      if (wp.itemEq("ex_no_adj_loc_low", "Y"))
        wp.whereStr +=
            " or (A.no_adj_loc_low ='Y' and to_char(sysdate,'yyyymmdd') between B.no_adj_loc_low_s_date and uf_nvl(B.no_adj_loc_low_e_date,'99991231'))";

      if (wp.itemEq("ex_no_f_stop", "Y"))
        wp.whereStr +=
            " or (A.no_f_stop_flag ='Y' and to_char(sysdate,'yyyymmdd') between B.no_f_stop_s_date and uf_nvl(B.no_f_stop_e_date,'99991231'))";

      if (wp.itemEq("ex_no_adj_h_cash", "Y"))
        wp.whereStr +=
            " or (A.no_adj_h_cash ='Y' and to_char(sysdate,'yyyymmdd') between B.no_adj_h_s_date_cash and uf_nvl(B.no_adj_h_e_date_cash,'99991231'))";
      wp.whereStr += " )";
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " A.acno_p_seqno , " + " B.acct_key , " + " A.acct_type , "
        + " A.no_block_flag , " + " A.no_unblock_flag , " + " A.no_adj_loc_high,"
        + " A.no_adj_loc_low," + " A.no_f_stop_flag," + " A.crt_date," + " A.no_adj_h_cash,"
        + " uf_acno_name(A.acno_p_seqno) as chi_name, "
        // + " uf_hi_cname(uf_acno_name(A.p_seqno)) as hh_chi_name, "
        + " A.acct_type||'-'||B.acct_key as wk_acct_key, " + " A.spec_reason , "
        + " A.card_00_cnt , " + " decode(sign(A.card_00_cnt - 1),-1,'N','Y') as tt_card_00_cnt , "
        + " A.acct_jrnl_bal "
    // + "A.acct_type||'-'||"+wp.sqlID+"uf_hi_idno(substr(B.acct_key,1,10)) as wk_acct_key"
    ;
    wp.daoTable = "rsk_spec_ind A join act_acno B on B.acno_p_seqno=A.acno_p_seqno";
    wp.whereOrder = " order by B.acct_key";

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    // queryAfter();

    wp.setPageValue();
  }

  // void queryAfter() {
  // for(int ii=0;ii<wp.selectCnt;ii++){
  // if(wp.col_num(ii,"card_00_cnt")==0){
  // wp.col_set(ii,"tt_card_00_cnt", "N");
  // } else if(wp.col_num(ii,"card_00_cnt")>0){
  // wp.col_set(ii,"tt_card_00_cnt", "Y");
  // }
  // }
  // }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "colr5910";
      String cond1 = "";
      wp.colSet("cond_1", cond1);
      wp.colSet("user_id", wp.loginUser);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "colr5910.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      wp.setListCount(1);
      // queryFunc();
      // wp.listCount[1] =sql_nrow;
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    wp.colSet("ex_check", "checked");
  }

}
