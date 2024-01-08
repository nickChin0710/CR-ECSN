/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-27  V1.00.00  Max Lin    program initial                            *
 * 107-07-25  V1.00.01  Alex       bug fixed                                  *
 * 109-09-29  V1.00.02  Andy       Mantis4276                                 *
 * 111/10/24  V1.00.06  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0030 extends BaseReport {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  InputStream inExcelFile = null;
  String mProgName = "actr0030";

  String condWhere = "";
  String kk1 = "" , kk2 = "" , kk3 = "" , kk4 = "";
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
    String exAcctType = wp.itemStr("ex_acct_type");
    String exUsrId = wp.itemStr("ex_usr_id");
    String exDateS = wp.itemStr("ex_date_S");
    String exDateE = wp.itemStr("ex_date_E");
    String exAppr = wp.itemStr("ex_appr");
    String exAcctKey = wp.itemStr("ex_acct_key");

    if(empty(exAcctType)&&
            empty(exUsrId)&&
            empty(exDateS)&&
            empty(exDateE)&&
            empty(exAcctKey)){
      errmsg("條件不可全部空白");
      return false;
    }

    if(chkStrend(exDateS, exDateE)==false){
      errmsg("異動日期: 起迄錯誤");
      return false;
    }



    //固定條件
    String lsWhere = " where 1=1  ";

    if(eqIgno(exAppr,"Y")){
      lsWhere += " and old_payment_rate <> payment_rate "
              +	sqlCol(exDateS,"apr_date",">=")
              +  sqlCol(exDateE,"apr_date","<=")
              + sqlCol(exAcctType,"acct_type")
              + sqlCol(exAcctKey,"acct_key","like%")
              + sqlCol(exUsrId,"update_user")
      ;
    }	else	{
      lsWhere += " and A.act_modtype = '02' "
              +  " and A.acct_data <> '' "
              +  sqlCol(exDateS,"A.update_date",">=")
              +  sqlCol(exDateE,"A.update_date","<=")
              +  sqlCol(exAcctType,"A.acct_type")
              +  sqlCol(exUsrId,"A.update_user")
              + sqlCol(exAcctKey,"B.acct_key","like%")
      ;
    }

    wp.whereStr = lsWhere;
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false){
      return;
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    if(wp.itemEq("ex_appr", "Y")){
      wp.pageControl();
      wp.selectSQL = " distinct "
              + " p_seqno ,"
              + " acct_key ,"
              + " acct_type , "
              + " update_user ,"
//							 + " apr_user as update_user ,"
              + " apr_user ,"
              + " apr_date ,"
              + " 'Y' as apr_flag , "
              + " uf_acno_name(p_seqno) as chi_name "
      ;
      wp.daoTable = " act_jcic_txn ";
      pageQuery();
      if (sqlRowNum <= 0) {
        alertErr("此條件查無資料");
        return;
      }
      wp.setListCount(1);
      wp.setPageValue();
    }	else	{
//			selectTmp();
      wp.pageControl();
      wp.selectSQL = ""
              + " A.act_modtype ,"
              + " A.p_seqno ,"
              + " A.acct_type ,"
              + " B.acct_key ,"
              + " A.update_user ,"
              + " A.update_date ,"
              + " A.mod_user ,"
              + " A.mod_time ,"
              + " A.mod_pgm ,"
              + " A.mod_seqno , "
              + " 'N' as apr_flag ,"
              + " decode(B.id_p_seqno,'',uf_corp_name(B.corp_p_seqno),uf_idno_name(B.id_p_seqno)) as chi_name ,"
              + " decode(B.id_p_seqno,'',uf_corp_no(B.corp_p_seqno),uf_idno_id(B.id_p_seqno)) as id_no "
      ;
      wp.daoTable = " act_moddata_tmp A join act_acno B on A.p_seqno = B.p_seqno ";
      pageQuery();
      if (sqlRowNum <= 0) {
        alertErr("此條件查無資料");
        return;
      }
      wp.setListCount(1);
      wp.setPageValue();
    }

  }

  void selectTmp() throws Exception{

    int ilTmpCnt = 0;
    daoTid = "tmp.";
    String sql1 = " select "
            + " A.act_modtype ,"
            + " A.p_seqno ,"
            + " A.acct_type ,"
            + " uf_acno_key(A.p_seqno) as acct_key ,"
            + " A.acct_data ,"
            + " A.update_user ,"
            + " A.update_date ,"
            + " A.mod_user ,"
            + " A.mod_time ,"
            + " A.mod_pgm ,"
            + " A.mod_seqno ,"
            + " substr(A.acct_data,1,6) as ls_ft_date ,"
            + " B.stmt_cycle ,"
            + " B.payment_rate1 ,"
            + " B.payment_rate2 ,"
            + " B.payment_rate3 ,"
            + " B.payment_rate4 ,"
            + " B.payment_rate5 ,"
            + " B.payment_rate6 ,"
            + " B.payment_rate7 ,"
            + " B.payment_rate8 ,"
            + " B.payment_rate9 ,"
            + " B.payment_rate10 ,"
            + " B.payment_rate11 ,"
            + " B.payment_rate12 ,"
            + " B.payment_rate13 ,"
            + " B.payment_rate14 ,"
            + " B.payment_rate15 ,"
            + " B.payment_rate16 ,"
            + " B.payment_rate17 ,"
            + " B.payment_rate18 ,"
            + " B.payment_rate19 ,"
            + " B.payment_rate20 ,"
            + " B.payment_rate21 ,"
            + " B.payment_rate22 ,"
            + " B.payment_rate23 ,"
            + " B.payment_rate24 ,"
            + " B.payment_rate25 ,"
            + " decode(B.id_p_seqno,'',uf_corp_name(B.corp_p_seqno),uf_idno_name(B.id_p_seqno)) as chi_name ,"
            + " decode(B.id_p_seqno,'',uf_corp_no(B.corp_p_seqno),uf_idno_id(B.id_p_seqno)) as id_no "
            + " from act_moddata_tmp A join act_acno B on A.p_seqno = B.p_seqno "
            + " where 1=1 and A.act_modtype = '02' "
            + " and A.acct_data <> '' "
            +sqlCol(kk1,"A.p_seqno")
            +sqlCol(kk3,"A.act_modtype")
            ;

    sqlSelect(sql1);

    daoTid = "work.";
    String sql2 = " select "
            + " this_acct_month "
            + " from ptr_workday "
            + " where stmt_cycle = ? "
            ;
    ilTmpCnt = sqlRowNum;
    String[] lsAcctData = new String[2];
    String lsDate = "", ls_pr_acct_month = ""; int llYy = 0 ;
    String[] lsTmpRate = new String[25];
    String[] lsAcnoRate = new String[25];
    String[] isVal = new String[25];
    int aa=0 , bb=1;
    for(int ii=0 ; ii<ilTmpCnt;ii++){
      sqlSelect(sql2,new Object[]{sqlStr(ii,"tmp.stmt_cycle")});
      if(sqlRowNum<=0)	continue;
      lsAcctData[0] = sqlStr(ii,"tmp.acct_data");
      lsDate = sqlStr(ii,"tmp.ls_ft_date");
      llYy = commDate.monthsBetween(sqlStr("work.this_acct_month"), lsDate);
      int rr = -1 ;
      for(int ll=1 ;ll<=25;ll++){
        rr++;
        lsTmpRate[rr] = "x";
        lsAcnoRate[rr] = "x";
        if(ll>=llYy){
          lsAcctData = commString.token(lsAcctData, "@");
          isVal[rr] = lsAcctData[1];
          lsAcctData = commString.token(lsAcctData, "@");
          lsTmpRate[rr] = lsAcctData[1];
        }
        lsAcnoRate[rr] = sqlStr(ii,"tmp.payment_rate"+ll);
      }

      for(int xx=0;xx<25;xx++){
        if(eqIgno(lsTmpRate[xx],lsAcnoRate[xx]))	continue;
        wp.colSet(aa,"acct_type", sqlStr(ii,"tmp.acct_type"));
        wp.colSet(aa,"chi_name", sqlStr(ii,"tmp.chi_name"));
        wp.colSet(aa,"id_no", sqlStr(ii,"tmp.id_no"));
        //wp.colSet(aa,"update_date", is_val[xx]);
        //ls_pr_acct_month = commDate.monthAdd(is_val[xx], 1);
        //wp.colSet(aa,"this_acct_month", ls_pr_acct_month);
        wp.colSet(aa,"this_acct_month", isVal[xx]);
        wp.colSet(aa,"old_payment_rate", lsAcnoRate[xx]);
        wp.colSet(aa,"payment_rate", lsTmpRate[xx]);
        wp.colSet(aa,"apr_flag", "N");
        wp.colSet(aa,"apr_user", "");
        //wp.colSet(aa,"apr_date", sqlStr(ii,"tmp.update_date3"));
        wp.colSet(aa,"apr_date", "");
        wp.colSet(aa,"update_user", sqlStr(ii,"tmp.update_user"));
        if(bb<10){
          wp.colSet(aa,"ser_num", "0"+bb);
        }	else	{
          wp.colSet(aa,"ser_num", ""+bb);
        }
        bb++;
        aa++;
      }

    }
    wp.listCount[0] = aa;
    if(aa==0){
      alertErr("查無資料");
    }
  }

  void xlsPrint() {
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;

      // -cond-
      String exAcctType = wp.itemStr("ex_acct_type");
      String exUsrId = wp.itemStr("ex_usr_id");
      String exDateS = wp.itemStr("ex_date_S");
      String exDateE = wp.itemStr("ex_date_E");
      String itemStr = wp.itemStr("ex_appr");

      String cond1 = "帳戶帳號: " + exAcctType + "  經辦代號: " + exUsrId;
      String cond2 = "異動日期: " + exDateS + " ~ " + exDateE + "  已放行: " + itemStr;
      wp.colSet("cond_1", cond1);
      wp.colSet("cond_1", cond2);

      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "N";
      xlsx.excelTemplate = mProgName + ".xlsx";

      //====================================
      xlsx.sheetName[0] ="報送 JCIC 帳戶繳款評等異動清單";
      queryFunc();
      wp.setListCount(1);
      log("Summ: rowcnt:" + wp.listCount[1]);
      xlsx.processExcelSheet(wp);

      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");

    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }

  void pdfPrint() throws Exception {
    wp.reportId = mProgName;
    // -cond-
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
    kk1 = wp.itemStr("data_k1");
    kk2 = wp.itemStr("data_k2");
    kk3 = wp.itemStr("data_k3");
    kk4 = wp.itemStr("data_k4");
    dataRead();
  }

  void dataRead() throws Exception {

    if(empty(kk4)){
      //--read tmp
      selectTmp();
    }	else	{
      wp.selectSQL = ""
              + " A.p_seqno ,"
              + " A.business_date ,"
              + " A.payment_num ,"
              + " A.acct_key ,"
              + " A.acct_type ,"
              + " A.this_acct_month ,"
              + " A.proc_flag ,"
              + " A.proc_date ,"
              + " A.mod_user ,"
              + " A.mod_time ,"
              + " A.mod_pgm ,"
              + " A.mod_seqno ,"
              + " A.update_user ,"
              + " A.update_date ,"
              + " A.apr_user ,"
              + " A.apr_date ,"
              + " 'Y' as apr_flag ,"
              + " A.old_payment_rate ,"
              + " A.payment_rate ,"
              + " B.id_p_seqno ,"
              + " B.corp_p_seqno ,"
              + " decode(B.id_p_seqno,'',uf_corp_name(B.corp_p_seqno),uf_idno_name(B.id_p_seqno)) as chi_name ,"
              + " decode(B.id_p_seqno,'',uf_corp_no(B.corp_p_seqno),uf_idno_id(B.id_p_seqno)) as id_no "
      ;
      wp.daoTable = " act_jcic_txn A join act_acno B on A.p_seqno = B.p_seqno ";
      wp.whereStr = " where 1=1 and old_payment_rate <> payment_rate "
              +sqlCol(kk1,"A.p_seqno")
              +sqlCol(kk2,"A.apr_date")
      ;

      pageQuery();
      if(sqlNotFind()){
        alertErr("查無資料");
        return ;
      }

      wp.setListCount(1);
      listWkdata();

    }

  }

  void listWkdata() throws Exception {

    //log("dispA-->wp.listCount[0]:" + wp.listCount[0]);
    String lsPrAcctMonth = "";
    int    liPaymentNum = 0;

    for (int ii = 0; ii < wp.listCount[0]; ii++) {
      //li_payment_num = 1 - wp.col_int(ii,"payment_num");
      liPaymentNum = 0 - wp.colInt(ii,"payment_num");
      lsPrAcctMonth = commDate.monthAdd(wp.colStr(ii,"this_acct_month"), liPaymentNum);
      wp.colSet(ii,"this_acct_month", lsPrAcctMonth);
    }

  }

  @Override
  public void dddwSelect() {
    try {
      // 帳戶類別
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_acct_type");
      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

      // 經辦代號
      wp.initOption = "--";
      wp.optionKey = wp.colStr("ex_usr_id");
      dddwList("dddw_usr_id", "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id");
    } catch (Exception ex) {
    }
  }

}

