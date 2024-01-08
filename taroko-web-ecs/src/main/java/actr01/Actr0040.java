/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR     DESCRIPTION                                *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-07-19  V1.00.01  Alex       bug fix                                    *
 * 106-12-12  V1.00.00  Max Lin    program initial                            *
 * 110-03-04  v1.00.02  Andy       Update PDF隠碼作業                                                                      *
 * 111/10/24  V1.00.03  jiangyigndong  updated for project coding standard    *
 ******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Actr0040 extends BaseReport {

  taroko.base.CommDate commDate = new taroko.base.CommDate();

  InputStream inExcelFile = null;
  String mProgName = "actr0040";
  int selectCnt = 0;
  String hsWhere1 = "" , hsWhere2 = "";
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
      //xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF1")) { // -PDF-
      strAction = "PDF1";
      // wp.setExcelMode();
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "PDF2")) { // -PDF-
      strAction = "PDF2";
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
    String exDateS = wp.itemStr("ex_date_S");
    String exDateE = wp.itemStr("ex_date_E");
    //int li_day_cnt = 0;
    if (empty(exDateS) || empty(exDateE)) {
      alertErr("請輸入處理日期");
      return ;
    }

    if(this.chkStrend(exDateS, exDateE)==false){
      alertErr("處理日期: 起迄錯誤");
      return ;
    }

    //li_day_cnt = commDate.daysBetween(ex_date_S, ex_date_E);
    //if(li_day_cnt >30){
    //	alertErr("處理日期: 一次只可查詢 30 天 !");
    //	return ;
    //}

    //log("A:"+li_day_cnt);

    hsWhere1 = " where 1=1 and A.p_seqno=B.acno_p_seqno"
            + " and A.reject_code <> '0' "
            + sqlCol(wp.itemStr("ex_acct_type"),"A.acct_type")
            + sqlCol(wp.itemStr("ex_acct_key"),"B.acct_key","like%")
            + sqlCol(exDateS,"A.print_date",">=")
            + sqlCol(exDateE,"A.print_date","<=")
            + sqlCol(wp.itemStr("ex_curr_code"),"uf_nvl(A.curr_code,'901')")
    ;

    hsWhere2 = " where 1=1 and A.p_seqno=B.acno_p_seqno"
            + " and A.reject_code ='0' "
            + sqlCol(wp.itemStr("ex_acct_type"),"A.acct_type")
            + sqlCol(wp.itemStr("ex_acct_key"),"B.acct_key","like%")
            + sqlCol(exDateS,"A.print_date",">=")
            + sqlCol(exDateE,"A.print_date","<=")
            + sqlCol(wp.itemStr("ex_curr_code"),"uf_nvl(A.curr_code,'901')")
    ;

    if(wp.itemEq("ex_from_mark", "1")){
      hsWhere1 += " and A.from_mark in ('1','') ";
      hsWhere2 += " and A.from_mark in ('1','') ";
    }	else	if (wp.itemEq("ex_from_mark", "2")){
      hsWhere1 += " and A.from_mark in ('02') ";
      hsWhere2 += " and A.from_mark in ('02') ";
    }	else	if (wp.itemEq("ex_from_mark", "3")){
      hsWhere1 += " and A.from_mark in ('03') ";
      hsWhere2 += " and A.from_mark in ('03') ";
    }	else	if (wp.itemEq("ex_from_mark", "4")){
      hsWhere1 += " and A.from_mark in ('04') ";
      hsWhere2 += " and A.from_mark in ('04') ";
    }	else	if (wp.itemEq("ex_from_mark", "5")){
      hsWhere1 += " and A.from_mark in ('W') ";
      hsWhere2 += " and A.from_mark in ('W') ";
    }


    if(wp.itemEq("ex_vip", "1")){
      hsWhere1 += " and B.vip_code =''";
      hsWhere2 += " and B.vip_code =''";
    }	else if(wp.itemEq("ex_vip", "2")){
      hsWhere1 += " and B.vip_code <>''";
      hsWhere2 += " and B.vip_code <>''";
    }

    //先取得實際總筆數
    String lsSql = "";
    int liSelErrTotcnt = 0, liSelOkTotcnt = 0;

    lsSql  = " select ";
    lsSql += " count(*) colh_sel_totcnt ";
    lsSql += " from act_acno B, act_a002r1 A left join ptr_sys_idtab P on uf_nvl(A.curr_code,'901') = P.wf_id and P.wf_type = 'DC_CURRENCY' ";
    lsSql += hsWhere1;
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      liSelErrTotcnt = sqlInt("colh_sel_totcnt");
    } else {
      liSelErrTotcnt = 0;
    }
    wp.colSet("real_error_cnt", liSelErrTotcnt);

    lsSql  = " select ";
    lsSql += " count(*) colh_sel_totcnt ";
    lsSql += " from act_acno B, act_a002r1 A left join ptr_sys_idtab P on uf_nvl(A.curr_code,'901') = P.wf_id and P.wf_type = 'DC_CURRENCY' ";
    lsSql += hsWhere2;
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      liSelOkTotcnt = sqlInt("colh_sel_totcnt");
    } else {
      liSelOkTotcnt = 0;
    }
    wp.colSet("real_ok_cnt", liSelOkTotcnt);

    //if (li_sel_err_totcnt == 0 && li_sel_ok_totcnt == 0) {
    //	alertErr("此條件查無資料 ");
    //	return;
    //}

    wp.pageRows=1499;
    wp.setQueryMode();
    queryRead();
  }


  @Override
  public void queryRead() throws Exception {
    daoTid = "A1_";
    wp.selectSQL = ""
            + " A.print_date ,"
            + " A.appl_no ,"
            + " A.chi_name ,"
            + " uf_hi_cname(A.chi_name) as hh_chi_name ,"
            + " A.autopay_id ,"
            + " A.autopay_id||'-'||A.autopay_id_code as tt_autopay_id ,"
            + " uf_hi_idno(A.autopay_id)||'-'||A.autopay_id_code as mk_autopay_id ,"
            + " A.autopay_acct_no ,"
            + " uf_hi_acctno(A.autopay_acct_no) db_hi_autopay_acct_no,"
            + " A.home_area_code1 ,"
            + " A.home_tel_no1 ,"
            + " A.reject_code ,"
            //+ " A.reject_code||'.'||decode(A.reject_code,'0','無誤','1','非本人帳戶','2','無效帳戶') as tt_reject_code ,"
            + " decode(A.reject_code,'0','無誤','1','非本人帳戶','2','無效帳戶') as tt_reject_code ,"
            + " A.from_mark ,"
            //+ " A.from_mark||'.'||decode(A.from_mark,'','新製卡','1','新製卡','02','新申請','03','修改帳號','04','eDDA') as tt_from_mark ,"
            + " decode(A.from_mark,'','新製卡','1','新製卡','02','新申請','03','修改帳號',A.from_mark) as tt_from_mark ,"
            + " A.autopay_id_code ,"
            + " A.acct_type ,"
            + " B.acct_key ,"
            + " A.acct_type||'-'||B.acct_key as wk_acct_key ,"
            //+ " A.acct_type||'-'||uf_hi_idno(B.acct_key) as hh_acct_key ,"
            //+ " A.acct_type||'-'||uf_hi_idno(substr(B.acct_key,1,10))||substr(B.acct_key,11,1) as hh_acct_key ,"同以下結果
            + " A.acct_type||'-'||substr(B.acct_key,1,3)||'XXXX'||substr(B.acct_key,8,4) as mk_acct_key ,"
            + " A.p_seqno ,"
            + " B.vip_code ,"
            + " A.mod_user ,"
            + " uf_nvl(A.curr_code,'901') curr_code ,"
            //+ " P.curr_chi_name as tt_curr_code ,"
            + " P.wf_desc as tt_curr_code ,"
            + " A.autopay_dc_flag "
    ;
    //wp.daoTable = " act_a002r1 A left join act_acno B on A.p_seqno = B.p_seqno and B.acno_flag != 'Y' ";
    //wp.daoTable = " act_acno B, act_a002r1 A left join ptr_currcode P on uf_nvl(A.curr_code,'901') = P.curr_code ";
    wp.daoTable = " act_acno B, act_a002r1 A left join ptr_sys_idtab P on uf_nvl(A.curr_code,'901') = P.wf_id and P.wf_type = 'DC_CURRENCY' ";
//	wp.daoTable = " act_a002r1 A , act_acno B ";
    wp.whereStr = hsWhere1;
    wp.whereOrder = " order by A.print_date Asc ";

    pageQuery();
    selectCnt = wp.selectCnt;

    wp.colSet("error_cnt", wp.selectCnt);

    wp.setListCount(1);
    queryReadTab2();
  }

  void queryReadTab2() throws Exception {
    daoTid = "A2_";
    wp.selectSQL = ""
            + " A.print_date ,"
            + " A.appl_no ,"
            + " A.chi_name ,"
            + " uf_hi_cname(A.chi_name) as hh_chi_name ,"
            + " A.autopay_id ,"
            + " A.autopay_id||'-'||A.autopay_id_code as tt_autopay_id ,"
            + " substr(A.autopay_id,1,3)||'XXXX'||substr(A.autopay_id,8,3)||'-'||A.autopay_id_code as mk_autopay_id ,"
            + " A.autopay_acct_no ,"
            + " uf_hi_acctno(A.autopay_acct_no) db_hi_autopay_acct_no ,"
            + " A.issue_date ,"
            + " A.from_mark ,"
            //+ " A.from_mark||'.'||decode(A.from_mark,'','新製卡','1','新製卡','02','新申請','03','修改帳號','04','eDDA') as tt_from_mark ,"
            + " decode(A.from_mark,'','新製卡','1','新製卡','02','新申請','03','修改帳號',A.from_mark) as tt_from_mark ,"
            + " A.reject_code ,"
            //+ " A.reject_code||'.'||decode(A.reject_code,'0','無誤','1','非本人帳戶','2','無效帳戶') as tt_reject_code ,"
            + " decode(A.reject_code,'0','無誤','1','非本人帳戶','2','無效帳戶') as tt_reject_code ,"
            + " A.autopay_id_code ,"
            + " A.acct_type ,"
            + " B.acct_key ,"
            + " A.acct_type||'-'||B.acct_key as wk_acct_key ,"
            //+ " A.acct_type||'-'||uf_hi_idno(B.acct_key) as hh_acct_key ,"
            //+ " A.acct_type||'-'||uf_hi_idno(substr(B.acct_key,1,10))||substr(B.acct_key,11,1) as hh_acct_key ,"同以下結果
            + " A.acct_type||'-'||substr(B.acct_key,1,3)||'XXXX'||substr(B.acct_key,8,4) as mk_acct_key ,"
            + " A.p_seqno ,"
            + " A.mod_user ,"
            + " uf_nvl(A.curr_code,'901') curr_code ,"
            //+ " P.curr_chi_name as tt_curr_code ,"
            + " P.wf_desc as tt_curr_code ,"
            + " A.autopay_dc_flag ,"
            + " c.autopay_indicator "
    //+ " decode(c.autopay_indicator,null,B.autopay_indicator,c.autopay_indicator) as autopay_indicator ,"
    //+ " decode(c.autopay_indicator,'1','扣TTL','2','扣MP','3','其他') as tt_autopay_indicator "
    ;
    //wp.daoTable = " act_a002r1 A left join act_acno B on A.p_seqno = B.p_seqno and B.acno_flag != 'Y' ";
    //wp.daoTable = " act_acno B, act_a002r1 A left join ptr_currcode P on uf_nvl(A.curr_code,'901') = P.curr_code ";
    wp.daoTable  = " act_acno B, act_a002r1 A ";
    wp.daoTable += " left join act_acct_curr c on c.p_seqno = a.p_seqno "
            + " and c.autopay_acct_no != '' and uf_nvl(A.curr_code,'901') = c.curr_code ";
    wp.daoTable += " left join ptr_sys_idtab P on uf_nvl(A.curr_code,'901') = P.wf_id and P.wf_type = 'DC_CURRENCY' ";
    wp.whereStr = hsWhere2;
    wp.whereOrder = " order by A.print_date Asc ";

    pageQuery();
    selectCnt += wp.selectCnt;
    if(this.sqlNotFind()){
      if(selectCnt!=0){
        selectOK();
      }
    }
    wp.colSet("ok_cnt", wp.selectCnt);
    wp.colSet("loginUser", wp.loginUser);
    wp.setListCount(2);
    listOkWkdata();

  }

  void listOkWkdata() throws Exception {

    log("dispA-->wp.listCount[1]:" + wp.listCount[1]);
    String lsPrintDateB1 = "", lsPrintDateA1 = "";
    String	lsSql1 = "select autopay_indicator as ac_autopay_indicator "
            + "from act_chkno where p_seqno = :ps_p_seqno "
            + "and autopay_acct_no = :ps_autopay_acct_no "
            + "and decode(curr_code,'','901',curr_code) = :ps_curr_code "
            + "and EXEC_CHECK_DATE >= :ps_print_date_b1 "
            + "and EXEC_CHECK_DATE <= :ps_print_date_a1 "
            + "order by EXEC_CHECK_DATE desc "
            + "fetch first 1 row only ";

    for (int ii = 0; ii < wp.listCount[1]; ii++) {

      if(wp.colStr(ii,"A2_autopay_indicator").length() > 0) {
        //ss=wp.colStr(ii,"autopay_indicator");
      } else {
        lsPrintDateB1 = commDate.dateAdd(wp.colStr("A2_print_date"), 0, 0, -1);
        lsPrintDateA1 = commDate.dateAdd(wp.colStr("A2_print_date"), 0, 0, 1);
        setString("ps_p_seqno", wp.colStr(ii,"A2_p_seqno"));
        setString("ps_autopay_acct_no", wp.colStr(ii,"A2_autopay_acct_no"));
        setString("ps_curr_code", wp.colStr(ii,"A2_curr_code"));
        setString("ps_print_date_b1", lsPrintDateB1);
        setString("ps_print_date_a1", lsPrintDateA1);
        sqlSelect(lsSql1);
        if (sqlRowNum > 0) {
          wp.colSet(ii,"A2_autopay_indicator", sqlStr("ac_autopay_indicator"));
        } else {
          wp.colSet(ii,"A2_autopay_indicator", "1");
        }
      }

      if(wp.colEq(ii,"A2_autopay_indicator", "1")) {
        wp.colSet(ii,"A2_tt_autopay_indicator", "扣TTL");
      }	else if(wp.colEq(ii,"A2_autopay_indicator", "2")){
        wp.colSet(ii,"A2_tt_autopay_indicator", "扣MP");
      }	else if(wp.colEq(ii,"A2_autopay_indicator", "3")){
        wp.colSet(ii,"A2_tt_autopay_indicator", "其他");
      }

    }

  }
  /***
   void xlsPrint() {
   try {
   log("xlsFunction: started--------");
   wp.reportId = m_progName;

   // -cond-
   String ex_date_S = wp.itemStr("ex_date_S");
   String ex_date_E = wp.itemStr("ex_date_E");

   String cond_1 = "處理日期:" + ex_date_S + " ~ " + ex_date_E;
   wp.colSet("cond_1", cond_1);

   // ===================================
   TarokoExcel xlsx = new TarokoExcel();
   wp.fileMode = "N";
   xlsx.excelTemplate = m_progName + ".xlsx";

   //====================================
   xlsx.sheetName[0] ="信用卡申請人扣繳帳號 IBM 查核報表";
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
   ***/

  void pdfPrint() throws Exception {
    String exDateS = wp.itemStr("ex_date_S");
    String exDateE = wp.itemStr("ex_date_E");
    //int li_day_cnt = 0;
    if (empty(exDateS) || empty(exDateE)) {
      wp.respHtml = "TarokoErrorPDF";
      alertErr("請輸入處理日期");
      return ;
    }

    if(this.chkStrend(exDateS, exDateE)==false){
      wp.respHtml = "TarokoErrorPDF";
      alertErr("處理日期: 起迄錯誤");
      return ;
    }

    //li_day_cnt = commDate.daysBetween(ex_date_S, ex_date_E);
    //if(li_day_cnt >30){
    //	wp.respHtml = "TarokoErrorPDF";
    //	alertErr("處理日期: 一次只可查詢 30 天 !");
    //	return ;
    //}

    String  tt ;
    tt="處理日期: "+commString.strToYmd(wp.itemStr("ex_date_S"))+" -- "+commString.strToYmd(wp.itemStr("ex_date_E"));
    if(wp.itemEq("ex_vip", "0")){
      tt+= "  全部卡友 ";
    }	else if(wp.itemEq("ex_vip", "1")){
      tt+= "  一般卡友 ";
    }	else if(wp.itemEq("ex_vip", "2")){
      tt+= "  VIP卡友 ";
    }

    if(eqIgno(strAction,"PDF1")){
      wp.reportId ="Actr0040R1";
      wp.pageRows =99999;
      //tt += "  雙幣幣別:"+wp.itemStr("ex_curr_code");
      //wp.colSet("cond_1", tt);
      queryFunc();
      if(wp.itemEmpty("ex_curr_code")){
        tt += "  雙幣幣別:"+ "";
      } else  {
        tt += "  雙幣幣別:"+wp.colStr(0,"A1_tt_curr_code");
      }
      wp.colSet("cond_1", tt);

      TarokoPDF   pdf = new TarokoPDF();
      wp.fileMode = "Y";
      pdf.excelTemplate = "actr0040_1.xlsx";
      pdf.pageCount =28;
      pdf.sheetNo = 0;
      pdf.procesPDFreport(wp);
      pdf = null;
      return;
    }	else if (eqIgno(strAction,"PDF2")){
      wp.reportId ="Actr0040R2";
      wp.pageRows =99999;
      /***
       String  tt ;
       tt="處理日期: "+commString.ss_2ymd(wp.itemStr("ex_date_S"))+" -- "+commString.ss_2ymd(wp.itemStr("ex_date_E"))
       + "  雙幣幣別:"+wp.itemStr("ex_curr_code");
       wp.colSet("cond_1", tt);
       ***/
      queryFunc();
      if(wp.itemEmpty("ex_curr_code")){
        tt += "  雙幣幣別:"+ "";
      } else  {
        tt += "  雙幣幣別:"+wp.colStr(0,"A2_tt_curr_code");
      }
      wp.colSet("cond_1", tt);

      wp.setListCount(1);
      TarokoPDF   pdf = new TarokoPDF();
      wp.fileMode = "Y";
      pdf.excelTemplate = "actr0040_2.xlsx";
      pdf.pageCount =28;
      pdf.sheetNo = 0;
      pdf.procesPDFreport(wp);
      pdf = null;
      return;
    }

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      // 雙幣幣別
      wp.optionKey = wp.colStr("ex_curr_code");
      dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc","where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id desc");
      wp.optionKey = wp.colStr(0, "ex_acct_type");
      dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");

    } catch (Exception ex) {
    }
  }

}

