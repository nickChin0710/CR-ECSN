/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-21  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 107-03-14  V1.00.01  Andy       Update dddw_list merchant UI               * 	 
* 109-04-27  V1.00.02  shiyuqi       updated for project coding standard     *  
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
* 111-05-26  V1.00.06   Ryan       移除重複getWhereStr()                        *       
* 112/03/08  V1.00.07  yingdong  Erroneous String Compare Issue              *
******************************************************************************/
package bilr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF2;

public class Bilr0050 extends BaseReport {

  InputStream inExcelFile = null;
  String mProgName = "bilr0050";

  String condWhere = "";
  String reportSubtitle = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
        + wp.respHtml);
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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
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
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String mchtNo = wp.itemStr("ex_merchant");
    String op = wp.itemStr("ex_op");
    String exKind = wp.itemStr("ex_kind");


    String lsWhere = " where 1=1 ";
    // ls_where += "and a.unit_price * (a.install_tot_term - install_curr_term) > 0 ";
    if (empty(exDateS) == false) {
      lsWhere += " and a.first_post_date >= :exDateS ";
      setString("exDateS", exDateS);
    }

    if (empty(exDateE) == false) {
      lsWhere += " and a.first_post_date <= :exDateE ";
      setString("exDateE", exDateE);
    }

    switch (op) {
      case "1":
        lsWhere += " and a.cps_flag = 'Y' ";
        break;
      case "2":
        lsWhere += " and a.cps_flag = 'N' ";
        break;
      case "4":
        lsWhere += " and a.cps_flag = 'C' ";
        break;
    }

    if (empty(mchtNo) == false) {
      lsWhere += " and a.mcht_no = :mcht_no ";
      setString("mcht_no", mchtNo);
    }

    switch (exKind) {
      case "1":
        lsWhere += " and a.contract_kind = '1' ";
        break;
      case "2":
        lsWhere += " and a.contract_kind = '1' and a.redeem_kind = '2' ";
        break;
      case "3":
        lsWhere += " and a.contract_kind = '1' and a.redeem_kind = '1' ";
        break;
      case "4":
        lsWhere += " and a.contract_kind = '1' and a.redeem_kind = '0' ";
        break;
      case "5":
        lsWhere += " and a.contract_kind = '2'";
        break;
      case "6":
        lsWhere += " and a.contract_kind = '2' and a.redeem_kind = '2' ";
        break;
      case "7":
        lsWhere += " and a.contract_kind = '2' and a.redeem_kind = '1' ";
        break;
      case "8":
        lsWhere += " and a.contract_kind = '2' and a.redeem_kind = '0' ";
        break;
    }

    wp.whereStr = lsWhere;
    setParameter();
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (getWhereStr() == false)
      return;
    // System.out.println("getWhereStr == true");
    // cond_where = wp.whereStr + "";
    // wp.whereStr =cond_where;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
    // System.out.println("queryRead OK!!");
  }

  private void setParameter() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

//    if (getWhereStr() == false)
//      return;

    wp.selectSQL =
        "" + "(a.mcht_no||a.install_tot_term) as group_key, " + "a.mcht_no," + "a.mcht_chi_name, "
            + "a.contract_no, " + "a.card_no, " + "uf_hi_cardno (a.card_no) db_card_no, "// 轉碼:交易卡號
            + "a.product_no, " + "a.product_name, " + "a.unit_price, "
            + "coalesce(a.qty,0) as qty, " + "coalesce(a.install_tot_term,0) as install_tot_term, "
            + "coalesce(a.remd_amt,0) as remd_amt, " + "coalesce(a.tot_amt,0) as tot_amt, "
            + "coalesce(a.fees_rate,0) as fees_rate, " + "coalesce(a.extra_fees,0) as extra_fees, "
            + "a.auto_delv_flag, " + "a.contract_seq_no, "
            + "coalesce(a.exchange_amt,0) as exchange_amt, "
            + "coalesce(a.refund_apr_flag,'N') as refund_apr_flag, "
            + "coalesce(a.refund_qty,0) as refund_qty, " + "a.delv_confirm_date, "
            + "a.against_num, " + "coalesce(b.fees_min_amt,0) as fees_min_amt, "
            + "coalesce(b.fees_max_amt,0) as fees_max_amt, " + "a.fh_flag, " + "a.cps_flag, "
            + "coalesce(a.refund_apr_date,'N') as refund_apr_date, "
            + "coalesce(a.redeem_amt,0) as redeem_amt, "
            + "coalesce(a.redeem_point,0) as redeem_point, " + "a.first_post_date, "
            + "coalesce(a.fees_fix_amt,0) as fees_fix_amt, "
            + "sum(coalesce(c.redeem_amt,0)) as copmute_0030, "
            + "sum(coalesce(c.redeem_point,0)) as copmute_0031 ";
    // + "coalesce(c.redeem_amt,0) as copmute_0030, "
    // + "coalesce(c.redeem_point,0) as copmut_0031 ";
    wp.daoTable =
        " bil_contract a left JOIN bil_prod b on a.product_no = b.product_no and a.mcht_no = b.mcht_no ";
    wp.daoTable +=
        " left join bil_back_log c on a.contract_no = c.contract_no and a.contract_seq_no = c.contract_seq_no ";
    wp.whereOrder = " group by " + "a.mcht_no," + "a.mcht_chi_name, " + "a.contract_no, "
        + "a.card_no, " + "a.product_no, " + "a.product_name, " + "a.unit_price, " + "a.qty, "
        + "a.install_tot_term, " + "a.remd_amt, " + "a.tot_amt, " + "a.fees_rate, "
        + "a.extra_fees, " + "a.auto_delv_flag, " + "a.contract_seq_no, " + "a.exchange_amt, "
        + "a.refund_apr_flag, " + "a.refund_qty, " + "a.delv_confirm_date, " + "a.against_num, "
        + "b.fees_min_amt, " + "b.fees_max_amt, " + "a.fh_flag, " + "a.cps_flag, "
        + "a.refund_apr_date, " + "a.redeem_amt, " + "a.redeem_point, " + "a.first_post_date, "
        + "a.fees_fix_amt ";
    wp.whereOrder += " order by a.mcht_no,a.install_tot_term,a.first_post_date,a.contract_no ";

    // setParameter();
    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable + wp.whereStr
    // +wp.whereOrder);
    // wp.daoTable);

    if (strAction.equals("XLS")) {
      selectNoLimit();
      // System.out.println("select_noLimit OK!!");
    }
    pageQuery();
    // System.out.println("pageQuery OK!!");
    // list_wkdata();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
    wp.setPageValue();
    // System.out.println("wp.setPageValue OK!!");
    listWkdata();
  }

  void listWkdata() {
    int mchtCt = 0, qAmt = 0, wkCurrFlagAmt = 0, amtRedeem = 0, pointRedeem = 0, amtTemp = 0;
    int wkCurrFlagCnt = 0, wkCurrExtraFees = 0, wkCurrRedeemAmt = 0;
    int wkCurrRedeemPoint = 0, wkCurrFlagFee = 0, wkCurrExchangeAmt = 0, wkCurrAmtTemp = 0;
    int sumCnt = 0, sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0, sum5 = 0, sum6 = 0;
    double amtAll = 0, sum7 = 0;
    String postDate, aprDate, wkCurrFlag = "N";
    int selCt = wp.selectCnt;

    for (int ii = 0; ii < selCt; ii++) {
      // 計算欄位
      mchtCt = 1;
      wp.colSet(ii, "mcht_ct", intToStr(mchtCt));

      if ("Y".equals(wp.colStr(ii, "refund_apr_flag"))) {
        amtRedeem = Integer.parseInt(wp.colStr(ii, "redeem_amt"))
            + Integer.parseInt(wp.colStr(ii, "copmute_0030"));
        pointRedeem = Integer.parseInt(wp.colStr(ii, "redeem_point"))
            + Integer.parseInt(wp.colStr(ii, "copmute_0031"));
        postDate = wp.colStr(ii, "first_post_date");
        aprDate = wp.colStr(ii, "refund_apr_date");
        if (postDate.equals(aprDate)) {
          wkCurrFlag = "Y";
          wkCurrFlagAmt = Integer.parseInt(wp.colStr(ii, "refund_qty"))
              * Integer.parseInt(wp.colStr(ii, "tot_amt"));
          wkCurrRedeemAmt = Integer.parseInt(wp.colStr(ii, "compute_0030"));
          wkCurrRedeemPoint = Integer.parseInt(wp.colStr(ii, "compute_0031"));
          if (Integer.parseInt(wp.colStr(ii, "qty")) == Integer
              .parseInt(wp.colStr(ii, "refund_qty"))) {
            wkCurrFlagCnt = 1;
            wkCurrExtraFees = Integer.parseInt(wp.colStr(ii, "extra_fees"));
            wkCurrExchangeAmt = Integer.parseInt(wp.colStr(ii, "exchange_amt"));
          }

          int tempA = 0;
          tempA = Math.round(Integer.parseInt(wp.colStr(ii, "tot_amt"))
              * Integer.parseInt(wp.colStr(ii, "fees_rate")) / 100
              + Integer.parseInt(wp.colStr(ii, "fees_fix_amt")));
          if (tempA > Integer.parseInt(wp.colStr(ii, "fees_max_amt"))) {
            wkCurrFlagFee = Integer.parseInt(wp.colStr(ii, "fees_max_amt"))
                * Integer.parseInt(wp.colStr(ii, "refund_qty"));
          } else if (tempA < Integer.parseInt(wp.colStr(ii, "fees_min_amt"))) {
            wkCurrFlagFee = Integer.parseInt(wp.colStr(ii, "fees_min_amt"))
                * Integer.parseInt(wp.colStr(ii, "refund_qty"));
          } else {
            wkCurrFlagFee = tempA * Integer.parseInt(wp.colStr(ii, "refund_qty"));
          }

          if ((wkCurrFlagAmt - wkCurrRedeemAmt - wkCurrExchangeAmt) > 0) {
            wkCurrAmtTemp = wkCurrFlagAmt - wkCurrRedeemAmt - wkCurrExchangeAmt + wkCurrExtraFees;
          }
        }
      }
      wp.colSet(ii, "amt_redeem", intToStr(amtRedeem));
      wp.colSet(ii, "point_redeem", intToStr(pointRedeem));
      wp.colSet(ii, "wk_curr_flag", wkCurrFlag);
      wp.colSet(ii, "wk_curr_flag_amt", intToStr(wkCurrFlagAmt));
      wp.colSet(ii, "wk_curr_flag_cnt", intToStr(wkCurrFlagCnt));
      wp.colSet(ii, "wk_curr_extra_fees", intToStr(wkCurrExtraFees));
      wp.colSet(ii, "wk_curr_exchange_amt", intToStr(wkCurrExchangeAmt));
      wp.colSet(ii, "wk_curr_flag_fee", intToStr(wkCurrFlagFee));
      wp.colSet(ii, "wk_curr_amt_temp", intToStr(wkCurrAmtTemp));

      // amt_temp
      // PB if( qty * ( tot_amt) - if(isNull(amt_redeem),0,amt_redeem) -
      // if(isNull(exchange_amt),0,exchange_amt)<0,0,
      // qty * ( tot_amt) - if(isNull(amt_redeem),0,amt_redeem) -
      // if(isNull(exchange_amt),0,exchange_amt))+ if(isNull(extra_fees),0,extra_fees)
      int temp1 = 0, temp2 = 0, temp3 = 0;
      temp1 = Integer.parseInt(wp.colStr(ii, "qty")) * Integer.parseInt(wp.colStr(ii, "tot_amt"));
      temp2 = amtRedeem + Integer.parseInt(wp.colStr(ii, "exchange_amt"));
      temp3 = temp1 - amtRedeem - Integer.parseInt(wp.colStr(ii, "exchange_amt"))
          + Integer.parseInt(wp.colStr(ii, "extra_fees"));
      if ((temp1 - temp2) > 0) {
        amtTemp = temp3;
      }
      wp.colSet(ii, "amt_temp", intToStr(amtTemp));

      // amt_all
      // PB if(round( (tot_amt * fees_rate / 100 + fees_fix_amt) , 0 ) > bil_prod_fees_max_amt ,
      // bil_prod_fees_max_amt
      // ,if(round( (tot_amt * fees_rate / 100 + fees_fix_amt) , 0 ) < bil_prod_fees_min_amt ,
      // bil_prod_fees_min_amt
      // , round( (tot_amt * fees_rate / 100 + fees_fix_amt) , 0 ) ) ) * qty
      double temp4 = 0, temp5 = 0;
      temp4 = toNum(wp.colStr(ii, "tot_amt")) * toNum(wp.colStr(ii, "fees_rate"));
      if (temp4 > 0) {
        temp5 = Math.round(temp4 / 100 + toNum(wp.colStr(ii, "fees_fix_amt")));
        if (temp5 > toNum(wp.colStr(ii, "fees_max_amt"))) {
          amtAll = toNum(wp.colStr(ii, "fees_max_amt"));
        } else if (temp5 < toNum(wp.colStr(ii, "fees_min_amt"))) {
          amtAll = toNum(wp.colStr(ii, "fees_min_amt"));
        } else {
          amtAll = temp5 * toNum(wp.colStr(ii, "qty"));
        }
      }
      wp.colSet(ii, "amt_all", Double.toString(amtAll));

      // q_amt
      qAmt = Integer.parseInt(wp.colStr(ii, "qty")) * Integer.parseInt(wp.colStr(ii, "tot_amt"));
      wp.colSet(ii, "q_amt", intToStr(qAmt));

      // total 合計數
      sumCnt = mchtCt - wkCurrFlagCnt;
      sum1 = qAmt - wkCurrFlagAmt;
      sum2 = Integer.parseInt(wp.colStr(ii, "extra_fees")) - wkCurrExtraFees;
      sum3 = amtRedeem - wkCurrRedeemAmt;
      sum4 = pointRedeem - wkCurrRedeemPoint;
      sum5 = Integer.parseInt(wp.colStr(ii, "exchange_amt")) - wkCurrExchangeAmt;
      sum6 = amtTemp - wkCurrAmtTemp;
      sum7 = amtAll - wkCurrExtraFees;
      wp.colSet(ii, "sum_cnt", intToStr(sumCnt));
      wp.colSet(ii, "sum1", intToStr(sum1));
      wp.colSet(ii, "sum2", intToStr(sum2));
      wp.colSet(ii, "sum3", intToStr(sum3));
      wp.colSet(ii, "sum4", intToStr(sum4));
      wp.colSet(ii, "sum5", intToStr(sum5));
      wp.colSet(ii, "sum6", intToStr(sum6));
      wp.colSet(ii, "sum7", Double.toString(sum7));
    }
  }

  void subTitle() {
    String exDateS = wp.itemStr("exDateS");
    String exDateE = wp.itemStr("exDateE");
    String mchtNo = wp.itemStr("ex_mcht_no");
    String op = wp.itemStr("ex_op");
    String exKind = wp.itemStr("ex_kind");
    String title = "";

    if (empty(exDateS) == false || empty(exDateE) == false) {
      title += "首期入帳日期 : ";
      if (empty(exDateS) == false) {
        title += exDateS + " 起 ";
      }
      if (empty(exDateE) == false) {
        title += " ~ " + exDateE + " 迄 ";
      }
      title += "  ";
    }
    if (empty(mchtNo) == false) {
      title += "特店代號 : " + mchtNo;
      title += "  ";
    }
    if (empty(op) == false) {
      title += "選項 : ";
      title += commString.decode(op, ",1,4,2,3,", ",ONUS,NCCC,人工,全部");
      title += "  ";
    }
    if (empty(exKind) == false) {
      title += "類別 : ";
      title += commString.decode(exKind, ",1,2,3,4,5,6,7,8,9",
          ",分期-全部,分期-折抵,分期-全折,分期-無折,郵購-全部,郵購-折抵,郵購-全折,郵購-無折,全部");
    }
    reportSubtitle = title;
  }

  void xlsPrint() {
    // System.out.println("xlsPrint in ");
    try {
      log("xlsFunction: started--------");
      wp.reportId = mProgName;
      // -cond-
      subTitle();
      wp.colSet("cond_1", reportSubtitle);
      // System.out.println("ss "+ss);
      // ===================================
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = mProgName + ".xlsx";

      // ====================================
      // -明細-
      xlsx.sheetName[0] = "明細";
      // System.out.println("before xls queryFunc OK!");
      queryFunc();
      // System.out.println("xls queryFunc OK!");
      wp.setListCount(1);
      log("Detl: rowcnt:" + wp.listCount[0]);
      xlsx.processExcelSheet(wp);
      /*
       * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where); wp.listCount[1] =sql_nrow;
       * ddd("Summ: rowcnt:" + wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
       */
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
    subTitle();
    wp.colSet("cond_1", reportSubtitle);
    wp.pageRows = 9999;

    queryFunc();
    wp.colSet("user_id", wp.loginUser);

    TarokoPDF2 pdf = new TarokoPDF2();
    wp.fileMode = "Y";
    pdf.excelTemplate = mProgName + ".xlsx";
    // 表頭固定欄位
    pdf.fixHeader[0] = "user_id";
    pdf.fixHeader[1] = "cond_1";

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
      // dddw_mcht_no
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss("ex_mcht_no");
      // dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name", "where 1=1 and
      // loan_flag = 'N' order by mcht_no");

    } catch (Exception ex) {
    }
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }

}
