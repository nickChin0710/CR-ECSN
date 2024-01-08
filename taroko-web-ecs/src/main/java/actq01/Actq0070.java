/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-05  V1.00.00  OrisChang  program initial                            *
 * 111/10/23  V1.00.01  jiangyigndong  updated for project coding standard    *
 * 112/01/17  V1.00.02  Simon      java程式寫html不work，取消幣別小計         *
 ******************************************************************************/

package actq01;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;

public class Actq0070 extends BaseEdit {
  CommDate commDate = new CommDate();
  String mAccttype = "";
  String mAcctkey = "";
  String pPSeqno = "";
  String mDates = "";
  String mDatee = "";
  String mProgName = "actq0070";

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
      updateFunc();
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.itemStr("ex_acct_type");
      this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_curr_code");
      this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
    } catch (Exception ex) {}
  }

  @Override
  public void queryFunc() throws Exception {
    if(empty(wp.itemStr("ex_acct_key")) && empty(wp.itemStr("ex_card_no"))) {
      alertErr("帳號, 卡號不可均為空白");
      return;
    }

    //start-查詢權限檢查，參考【f_auth_query】
    String lmQryKey = "";
    ColFunc colfunc =new ColFunc();
    colfunc.setConn(wp);
    if (!empty(wp.itemStr("ex_acct_key"))) {
      lmQryKey = wp.itemStr("ex_acct_key");
    }
    else if (!empty(wp.itemStr("ex_card_no"))) {
      lmQryKey = wp.itemStr("ex_card_no");
    }
    else {
      lmQryKey = "";
    }

    if (colfunc.fAuthQuery(mProgName, lmQryKey)!=1) {
      alertErr(colfunc.getMsg());
      return;
    }
    //end-查詢權限檢查，參考【f_auth_query】

    // 設定queryRead() SQL條件
    String lsPSeqno = getInitParm();

    if (lsPSeqno.equals("")) {
      alertErr("無此帳號/卡號");
      return;
    }

    if (!lsPSeqno.equals("")) {
      getDtlData(lsPSeqno);
    }

    pPSeqno = lsPSeqno;
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (!empty(wp.itemStr("ex_date_s"))) mDates = wp.itemStr("ex_date_s");
    if (!empty(wp.itemStr("ex_date_e"))) mDatee = wp.itemStr("ex_date_e");

//		wp.selectSQL = " p_seqno,         " +
//					" acct_type,          " +
////					" acct_key,           " +  //no column
//					" uf_acno_key(p_seqno) as acct_key,           " +
//					" post_date,          " +
//					" acct_month,         " +
//					" intr_org_captial as tw_intr_org_captial,    " +
//					" intr_s_date,        " +
//					" intr_e_date,        " +
//					" interest_sign,      " +
//					" reason_code,        " +
//					" interest_amt as tw_interest_amt,       " +
//					" inte_d_amt as tw_inte_d_amt,           " +
//					" reference_no,       " +
//					" interest_rate,      " +
//					" crt_date,           " +
//					" crt_time,           " +
//					" nvl(curr_code,'901') curr_code,        " +
//					" decode(curr_code,null,inte_d_amt,dc_inte_d_amt) inte_d_amt,                    " +
//					" decode(curr_code,null,intr_org_captial,dc_intr_org_captial) intr_org_captial,  " +
//					" decode(curr_code,null,interest_amt,dc_interest_amt) interest_amt,              " +
//					" decode(curr_code,'901',1,'TWD',1,'840',2,'USD',2,'392',3,'978',4,9) curr_sort  ";
//
//		wp.daoTable = "act_intr";
//
//		wp.whereStr = "WHERE p_seqno = :p_seqno " +
//					" AND acct_month >= :syyyymm AND acct_month <= :eyyyymm " +
//					" AND reason_code in ('DB00','DB01','CY0A','DB0D','CY01','CY02','CY03','CY04','CY05', " +
//					"     'CY06','CY07','CY08','CY0B','CY0D','FL29','06AO','07AO') ";
//		this.setString("p_seqno", p_p_seqno);
//		this.setString("syyyymm", m_dates);
//		this.setString("eyyyymm", m_datee);
//		if (empty(wp.itemStr("ex_curr_code")) == false) {
//			wp.whereStr += " and nvl(curr_code,'901') = :curr_code ";
//			this.setString("curr_code", wp.itemStr("ex_curr_code"));
//		}
//
//		wp.whereOrder = " order by curr_sort, crt_date, reference_no, intr_org_captial, intr_s_date ";

    //Mega-ECS-CR 1143: 新增[原始起息日][原始消費日][原始消費金額][利息本金合計]欄位  2019.8.7
    wp.selectSQL= " a.p_seqno, "
            + " a.acct_type, "
            + " uf_acno_key(a.p_seqno) as acct_key, "
            + " a.post_date,  "
            + " a.acct_month, "
            + " a.intr_org_captial as intr_org_captial_tw, "
            + " a.intr_s_date, "
            + " a.intr_e_date, "
            + " a.interest_sign, "
            + " a.reason_code, "
            + " a.interest_amt as interest_amt_tw, "
            + " a.inte_d_amt as inte_d_amt_tw, "
            + " (a.interest_amt - a.inte_d_amt) as inte_waive_amt_tw, "
            + " a.reference_no, "
            + " a.interest_rate, "
            + " a.crt_date, "
            + " a.crt_time, "
            + " decode(a.curr_code,'','901',a.curr_code) curr_code, "
            + " decode(a.curr_code,'',a.inte_d_amt,a.dc_inte_d_amt) inte_d_amt, "
            + " decode(a.curr_code,'',a.intr_org_captial,a.dc_intr_org_captial) intr_org_captial, "
            + " decode(a.curr_code,'',a.interest_amt,a.dc_interest_amt) interest_amt, "
            + " decode(a.curr_code,'',a.interest_amt-a.inte_d_amt,a.dc_interest_amt-a.dc_inte_d_amt) inte_waive_amt, "
            + " uf_curr_sort(a.curr_code) curr_sort, "
            //+ " nvl(b.purchase_date, nvl(c.purchase_date,' ')) purchase_date, "
            //+ " nvl(b.interest_date, nvl(c.interest_date,' ')) interest_date, "
            //+ " nvl(b.beg_bal,nvl(c.beg_bal,0)) destination_amt ";
            + " uf_nvl(b.purchase_date, c.purchase_date) purchase_date, "
            + " uf_nvl(b.interest_date, c.interest_date) interest_date, "
            + " decode(nvl(b.beg_bal,0),0,nvl(c.beg_bal,0),b.beg_bal) destination_amt ";

    wp.daoTable = "act_intr a "
            + "left join act_debt b on a.p_seqno = b.p_seqno and a.reference_no = b.reference_no "
            + "left join act_debt_hst c on a.p_seqno = c.p_seqno and a.reference_no = c.reference_no ";

    wp.whereStr = "WHERE a.p_seqno = :p_seqno " +
            " AND a.acct_month >= :syyyymm AND a.acct_month <= :eyyyymm " +
            " AND a.reason_code in ('DB00','DB01','CY0A','DB0D','CY01','CY02','CY03','CY04','CY05', " +
            "     'CY06','CY07','CY08','CY0B','CY0D','FL29','06AO','07AO') ";
    this.setString("p_seqno", pPSeqno);
    this.setString("syyyymm", mDates);
    this.setString("eyyyymm", mDatee);
    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and decode(a.curr_code,'','901',a.curr_code) = :curr_code ";
      this.setString("curr_code", wp.itemStr("ex_curr_code"));
    }

    wp.whereOrder = " order by curr_sort, crt_date, reference_no, intr_org_captial, intr_s_date ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
  }

  void listWkdata() throws Exception {
    String ss = "";
    double gp1PAmt = 0, gp1MAmt = 0, gp1TAmt = 0, gp1PIntrAmt = 0;  //新增利息本金小計 2019.2.22
    double totPAmt = 0, totMAmt = 0, totTAmt = 0, totPIntrAmt = 0;
    double gp1PDaAmt = 0, totPDaAmt = 0, gp1PWaiveAmt = 0, totPWaiveAmt = 0;
    wp.colSet(wp.selectCnt,"curr_code","XXX"); //for last group break use

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      ss = wp.colStr(ii, "interest_sign");

      if (eqIgno(ss,"+")) {
        gp1PAmt += wp.colNum(ii,"interest_amt");
        gp1TAmt += wp.colNum(ii,"interest_amt");
        gp1PIntrAmt += wp.colNum(ii,"intr_org_captial");
        gp1PDaAmt += wp.colNum(ii,"inte_d_amt");
        gp1PWaiveAmt += wp.colNum(ii,"interest_amt") - wp.colNum(ii,"inte_d_amt");
      }
      if (eqIgno(ss,"-")) {
        gp1MAmt -= wp.colNum(ii,"interest_amt");
        gp1TAmt -= wp.colNum(ii,"interest_amt");
      }
      //gp1_intr_amt += wp.colNum(ii,"intr_org_captial");

/***
      if (!wp.colStr(ii,"curr_code").equals(wp.colStr(ii+1,"curr_code"))) {
//				wp.colSet(ii,"tr","<tr><td nowrap colspan=\"3\" class=\"list_rr\" style=\"color:#CC0000\">幣別&nbsp;"+wp.colStr(ii,"curr_code")+"&nbsp;小計：</td>"
        wp.colSet(ii,"tr","<tr><td nowrap class=\"list_rr\" style=\"color:#CC0000\">幣別&nbsp;"+wp.colStr(ii,"curr_code")+"&nbsp;小計：</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:blue\">利息本金合計：</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:#CC0000\">"+numToStr(gp1PIntrAmt,"#,##0.00")+"</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:blue\">正項利息：</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:#CC0000\">"+numToStr(gp1PAmt,"#,##0.00")+"</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:blue\">負項利息：</td>"
                + "<td nowrap class=\"list_rr\" colspan=\"2\" style=\"color:#CC0000\">"+numToStr(gp1MAmt,"#,##0.00")+"</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:blue\">計息合計：</td>"
                + "<td nowrap class=\"list_rr\" colspan=\"1\" style=\"color:#CC0000\">"+numToStr(gp1TAmt,"#,##0.00")+"</td>"
                + "<td nowrap class=\"list_rr\" colspan=\"2\" style=\"color:blue\">可D數金額合計：</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:#CC0000\">"+numToStr(gp1PDaAmt,"#,##0.00")+"</td>"
                + "<td nowrap class=\"list_rr\" colspan=\"1\" style=\"color:blue\">減免金額合計：</td>"
                + "<td nowrap class=\"list_rr\" style=\"color:#CC0000\">"+numToStr(gp1PWaiveAmt,"#,##0.00")+"</td>"
                //+ "<td nowrap class=\"list_ll\">&nbsp;</td></tr>");
                + "</tr>");
        gp1PAmt=0;
        gp1MAmt=0;
        gp1TAmt=0;
        gp1PIntrAmt=0;
        gp1PDaAmt=0;
        gp1PWaiveAmt=0;
      }
***/
      if (eqIgno(ss,"+")) {
        totPAmt += wp.colNum(ii,"interest_amt_tw");
        totTAmt += wp.colNum(ii,"interest_amt_tw");
        totPIntrAmt += wp.colNum(ii,"intr_org_captial_tw");
        totPDaAmt += wp.colNum(ii,"inte_d_amt_tw");
        totPWaiveAmt += wp.colNum(ii,"interest_amt_tw") - wp.colNum(ii,"inte_d_amt_tw");
      }
      if (eqIgno(ss,"-")) {
        totMAmt -= wp.colNum(ii,"interest_amt_tw");
        totTAmt -= wp.colNum(ii,"interest_amt_tw");
      }
      //tot_intr_amt += wp.colNum(ii,"intr_org_captial");
    }
    wp.colSet("tot_p_amt",totPAmt+"");
    wp.colSet("tot_m_amt",totMAmt+"");
    wp.colSet("tot_t_amt",totTAmt+"");
    wp.colSet("tot_intr_amt",totPIntrAmt+"");
    wp.colSet("tot_inte_d_amt",totPDaAmt+"");
    wp.colSet("tot_inte_waive_amt",totPWaiveAmt+"");
  }

  private String getInitParm() throws Exception {
    String lsSql = "";

    lsSql  = " select acct_type, acct_key, p_seqno, ";
    lsSql += " uf_corp_name(corp_p_seqno) as acno_cname, uf_idno_name(id_p_seqno) as acno_iname  ";
    lsSql += " from act_acno ";
    lsSql += " where 1=1 ";

    mAccttype = wp.itemStr("ex_acct_type");
    mAcctkey = fillZeroAcctKey(wp.itemStr("ex_acct_key"));

    if (empty(mAcctkey) == false) {
      lsSql += "and acct_type = :acct_type and acct_key = :acct_key ";
      setString("acct_type", mAccttype);
      setString("acct_key", mAcctkey);
    } else {
      lsSql += "and acno_p_seqno in (select acno_p_seqno from crd_card where card_no = :card_no) ";
      setString("card_no", wp.itemStr("ex_card_no"));
    }

    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("acct_type", sqlStr("acct_type"));
      wp.colSet("acct_key", sqlStr("acct_key"));
      wp.colSet("p_seqno", sqlStr("p_seqno"));
      wp.colSet("dsp_id_cname", sqlStr("acno_iname"));
      wp.colSet("dsp_corp_cname", sqlStr("acno_cname"));
      wp.colSet("ex_acct_type", sqlStr("acct_type"));
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
      return sqlStr("p_seqno");
    }
    return "";
  }

  private void getDtlData(String pSeqno) throws Exception {
    String lsSql = "";
    lsSql =" SELECT a.acct_status,              " +
            "        a.stmt_cycle,               " +
            "        b.this_acct_month           " +
            "   FROM act_acno a, ptr_workday b   " +
            "  WHERE b.stmt_cycle = a.stmt_cycle " +
            "    and a.acno_p_seqno = :p_seqno ";
    setString("p_seqno", pSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("dsp_cycle", sqlStr("stmt_cycle"));
//			wp.colSet("ex_date_s", sqlStr("this_acct_month"));
//			wp.colSet("ex_date_e", commDate.dateAdd(sqlStr("this_acct_month"), 0, 1, 0));
      mDates = sqlStr("this_acct_month");
      mDatee = commDate.dateAdd(mDates,0,1,0).substring(0,6);

      if (empty(wp.itemStr("ex_date_s")))  wp.colSet("ex_date_s", mDates);
      if (empty(wp.itemStr("ex_date_e"))) {wp.colSet("ex_date_e", mDatee); wp.itemSet("ex_date_e", mDatee);}

      if (empty(wp.itemStr("ex_date_s"))) {
        if (wp.colStr("ex_date_s").compareTo(wp.colStr("ex_date_e")) > 0) {
          wp.itemSet("ex_date_s", wp.colStr("ex_date_e"));
          wp.colSet("ex_date_s", wp.colStr("ex_date_e"));
        }
      }

      if (wp.colStr("ex_date_s").compareTo(wp.colStr("ex_date_e")) > 0) {
        alertErr("關帳年月輸入錯誤！");
        return;
      }


      //ls_sql =" SELECT beg_bal          " +
      lsSql =" SELECT sum(beg_bal) as sum_beg_bal     " +
              "   FROM act_debt         " +
              "  WHERE p_seqno = :p_seqno " +
              "    AND acct_month >= :acct_month_s " +
              "    AND acct_month <= :acct_month_e " +
              "    AND acct_code = 'RI' " ;
      setString("p_seqno", pSeqno);
      //setString("acct_month", sqlStr("this_acct_month"));
      setString("acct_month_s", wp.colStr("ex_date_s"));
      setString("acct_month_e", wp.colStr("ex_date_e"));
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        wp.colSet("dsp_interest", sqlStr("sum_beg_bal"));
      }
    }
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

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  String fillZeroAcctKey(String acctkey) throws Exception {
    String rtn = acctkey;
    if (acctkey.trim().length()==8) rtn += "000";
    if (acctkey.trim().length()==10) rtn += "0";

    return rtn;
  }

}