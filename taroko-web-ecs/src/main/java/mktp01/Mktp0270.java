/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/11/15  V1.00.07   Allen Ho      Initial                              *
 * 111/11/28  V1.00.08  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package mktp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0270 extends BaseProc
{
  private final String PROGNAME = "紅利積點兌換贈品登錄覆核處理程式110/11/15 V1.00.07";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0270Func func = null;
  String kk1,kk2,kk3,kk4,kk5,kk6,kk7;
  String km1,km2,km3,km4,km5,km6,km7;
  String fstAprFlag = "";
  String orgTabName = "mkt_gift_bpexchg_t";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt =0, recCnt =0, notifyCnt =0,colNum=0;
  int[] datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String upGroupType = "0";

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception
  {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X"))
    {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    }
    else if (eqIgno(wp.buttonCode, "Q"))
    {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    }
    else if (eqIgno(wp.buttonCode, "R"))
    {//-資料讀取-
      strAction = "R";
      dataRead();
    }
    else if (eqIgno(wp.buttonCode, "C"))
    {// 資料處理 -/
      strAction = "A";
      dataProcess();
    }
    else if (eqIgno(wp.buttonCode, "M"))
    {/* 瀏覽功能 :skip-page*/
      queryRead();
    }
    else if (eqIgno(wp.buttonCode, "S"))
    {/* 動態查詢 */
      querySelect();
    }
    else if (eqIgno(wp.buttonCode, "L"))
    {/* 清畫面 */
      strAction = "";
      clearFunc();
    }
    else if (eqIgno(wp.buttonCode, "NILL"))
    {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    funcSelect();
    dddwSelect();
    initButton();
  }
  // ************************************************************************
  @Override
  public void queryFunc() throws Exception
  {
    wp.whereStr = "WHERE 1=1 "
            + sqlCol(wp.itemStr("ex_gift_no"), "a.gift_no")
            + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user")
            + sqlCol(wp.itemStr("ex_proc_flag"), "a.proc_flag")
            + sqlCol(wp.itemStr("ex_from_mark"), "a.from_mark")
            + sqlCol(wp.itemStr("ex_gift_type"), "a.gift_type")
            + " and a.apr_flag='N'     "
    ;

    //-page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }
  // ************************************************************************
  @Override
  public void queryRead() throws Exception
  {
    if (wp.colStr("org_tab_name").length()>0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.aud_type,"
            + "a.proc_flag,"
            + "decode(nvl(b.acct_key,''),'',c.acct_key,b.acct_key) as acct_key, "
            + "a.card_no,"
            + "a.gift_no,"
            + "a.tran_date,"
            + "a.exchg_cnt,"
            + "a.crt_date,"
            + "a.crt_user,"
            + "a.tran_seqno,"
            + "a.vendor_no";

    wp.daoTable = controlTabName + " a "
            + " left JOIN act_acno b "
            + "ON a.p_seqno = b.p_seqno "
            + " left JOIN dba_acno c "
            + "ON a.p_seqno = c.p_seqno "
    ;
    wp.whereOrder = " "
            + " order by a.gift_no,a.crt_user,a.proc_flag,a.from_mark,a.gift_type"
    ;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind())
    {
      alertErr(appMsg.errCondNodata);
      buttonOff("btnAdd_disable");
      return;
    }

    commGiftNo("comm_gift_no");
    commCrtUser("comm_crt_user");

    commProcFlagb("comm_proc_flag");
    commfuncAudType("aud_type");

    wp.setPageValue();
  }
  // ************************************************************************
  @Override
  public void querySelect() throws Exception
  {

    kk1 = itemKk("data_k1");
    qFrom=1;
    dataRead();
  }
  // ************************************************************************
  @Override
  public void dataRead() throws Exception
  {
    if (qFrom==0)
      if (wp.itemStr("kk_card_no").length()==0)
      {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length()==0)
    {
      if (wp.colStr("control_tab_name").length()==0)
        controlTabName = orgTabName;
      else
        controlTabName =wp.colStr("control_tab_name");
    }
    else
    {
      if (wp.colStr("control_tab_name").length()!=0)
        controlTabName =wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.id_p_seqno as id_p_seqno,"
            + "a.aud_type,"
            + "a.card_no as card_no,"
            + "a.gift_no as gift_no,"
            + "a.vendor_no as vendor_no,"
            + "a.bonus_type as bonus_type,"
            + "a.tran_seqno as tran_seqno,"
            + "a.crt_user,"
            + "a.proc_flag,"
            + "a.gift_type,"
            + "a.cash_value,"
            + "a.total_pt,"
            + "a.exchg_pt,"
            + "a.exchg_amt,"
            + "a.end_tran_bp,"
            + "a.fund_code,"
            + "a.tran_date,"
            + "a.from_mark,"
            + "a.exchg_cnt,"
            + "a.exchg_mark,"
            + "a.exg_gl_date,"
            + "a.exg_apr_user,"
            + "a.exg_apr_date,"
            + "a.chi_name,"
            + "a.cellar_phone,"
            + "a.home_area_code1,"
            + "a.home_tel_no1,"
            + "a.home_tel_ext1,"
            + "a.office_area_code1,"
            + "a.office_tel_no1,"
            + "a.office_tel_ext1,"
            + "a.bill_sending_zip,"
            + "a.bill_sending_addr1,"
            + "a.bill_sending_addr2,"
            + "a.bill_sending_addr3,"
            + "a.bill_sending_addr4,"
            + "a.bill_sending_addr5,"
            + "a.ecoupon_bno,"
            + "a.ecoupon_date_s,"
            + "a.ecoupon_date_e,"
            + "a.ecoupon_date,"
            + "a.ecoupon_ret_date,"
            + "a.ecoupon_gl_date,"
            + "a.sms_flag,"
            + "a.sms_date,"
            + "a.unpay_cnt,"
            + "a.sms_resend_desc,"
            + "a.air_type,"
            + "a.cal_mile,"
            + "a.cal_dfpno,"
            + "a.passport_name,"
            + "a.passport_surname,"
            + "a.send_date,"
            + "a.send_time,"
            + "a.recv_date,"
            + "a.recv_time,"
            + "a.air_birthday,"
            + "a.air_id_no,"
            + "a.air_apr_user,"
            + "a.air_apr_date,"
            + "a.rjct_proc_code,"
            + "a.rjct_proc_remark,"
            + "a.out_date,"
            + "a.out_batchno,"
            + "a.register_no,"
            + "a.out_mark,"
            + "a.out_apr_user,"
            + "a.out_apr_date,"
            + "a.pay_date,"
            + "a.pay_gl_date,"
            + "a.pay_apr_user,"
            + "a.pay_apr_date,"
            + "a.return_date,"
            + "a.return_reason,"
            + "a.return_mark,"
            + "a.ret_apr_user,"
            + "a.ret_apr_date,"
            + "a.ret_gl_date";

    wp.daoTable = controlTabName + " a "
//            + "JOIN crd_idno b "
//            + "ON a.id_p_seqno = b.id_p_seqno "
            
    ;
    wp.whereStr = "where 1=1 ";
    if (qFrom==0)
    {
      wp.whereStr = wp.whereStr
              + sqlCol(km1, "a.card_no")
              + sqlCol(km2, "b.id_no")
              + sqlCol(km3, "b.chi_name")
              + sqlCol(km4, "a.gift_no")
              + sqlCol(km5, "a.vendor_no")
              + sqlCol(km6, "a.bonus_type")
              + sqlCol(km7, "a.tran_seqno")
      ;
    }
    else if (qFrom==1)
    {
      wp.whereStr = wp.whereStr
              +  sqlRowId(kk1, "a.rowid")
      ;
    }

    pageSelect();
    if (sqlNotFind())
    {
      return;
    }
    commProcFlag("comm_proc_flag");
    commGiftType("comm_gift_type");
    commFromMark("comm_from_mark");
    commSmsFlag("comm_sms_flag");
    commRjctCode("comm_rjct_proc_code");
    commCrtUser("comm_crt_user");
    commGiftName("comm_gift_no");
    commVendorName("comm_vendor_no");
    commBonusType1("comm_bonus_type");
    commFundCode("comm_fund_code");
    commAirType("comm_air_type");
    commModReason("comm_return_reason");
    checkButtonOff();
    km1 = wp.colStr("card_no");
    km2 = wp.colStr("id_no");
    km3 = wp.colStr("chi_name");
    km4 = wp.colStr("gift_no");
    km5 = wp.colStr("vendor_no");
    km6 = wp.colStr("bonus_type");
    km7 = wp.colStr("tran_seqno");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A")) dataReadR3R();
    else
    {
      commfuncAudType("aud_type");
      listWkdataSpace();
    }
  }
  // ************************************************************************
  public void dataReadR3R() throws Exception
  {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_GIFT_BPEXCHG";
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.card_no as card_no,"
            + "a.gift_no as gift_no,"
            + "a.vendor_no as vendor_no,"
            + "a.bonus_type as bonus_type,"
            + "a.tran_seqno as tran_seqno,"
            + "a.crt_user as bef_crt_user,"
            + "a.proc_flag as bef_proc_flag,"
            + "a.gift_type as bef_gift_type,"
            + "a.cash_value as bef_cash_value,"
            + "a.total_pt as bef_total_pt,"
            + "a.exchg_pt as bef_exchg_pt,"
            + "a.exchg_amt as bef_exchg_amt,"
            + "a.end_tran_bp as bef_end_tran_bp,"
            + "a.fund_code as bef_fund_code,"
            + "a.tran_date as bef_tran_date,"
            + "a.from_mark as bef_from_mark,"
            + "a.exchg_cnt as bef_exchg_cnt,"
            + "a.exchg_mark as bef_exchg_mark,"
            + "a.exg_gl_date as bef_exg_gl_date,"
            + "a.exg_apr_user as bef_exg_apr_user,"
            + "a.exg_apr_date as bef_exg_apr_date,"
            + "a.chi_name as bef_chi_name,"
            + "a.cellar_phone as bef_cellar_phone,"
            + "a.home_area_code1 as bef_home_area_code1,"
            + "a.home_tel_no1 as bef_home_tel_no1,"
            + "a.home_tel_ext1 as bef_home_tel_ext1,"
            + "a.office_area_code1 as bef_office_area_code1,"
            + "a.office_tel_no1 as bef_office_tel_no1,"
            + "a.office_tel_ext1 as bef_office_tel_ext1,"
            + "a.bill_sending_zip as bef_bill_sending_zip,"
            + "a.bill_sending_addr1 as bef_bill_sending_addr1,"
            + "a.bill_sending_addr2 as bef_bill_sending_addr2,"
            + "a.bill_sending_addr3 as bef_bill_sending_addr3,"
            + "a.bill_sending_addr4 as bef_bill_sending_addr4,"
            + "a.bill_sending_addr5 as bef_bill_sending_addr5,"
            + "a.ecoupon_bno as bef_ecoupon_bno,"
            + "a.ecoupon_date_s as bef_ecoupon_date_s,"
            + "a.ecoupon_date_e as bef_ecoupon_date_e,"
            + "a.ecoupon_date as bef_ecoupon_date,"
            + "a.ecoupon_ret_date as bef_ecoupon_ret_date,"
            + "a.ecoupon_gl_date as bef_ecoupon_gl_date,"
            + "a.sms_flag as bef_sms_flag,"
            + "a.sms_date as bef_sms_date,"
            + "a.unpay_cnt as bef_unpay_cnt,"
            + "a.sms_resend_desc as bef_sms_resend_desc,"
            + "a.air_type as bef_air_type,"
            + "a.cal_mile as bef_cal_mile,"
            + "a.cal_dfpno as bef_cal_dfpno,"
            + "a.passport_name as bef_passport_name,"
            + "a.passport_surname as bef_passport_surname,"
            + "a.send_date as bef_send_date,"
            + "a.send_time as bef_send_time,"
            + "a.recv_date as bef_recv_date,"
            + "a.recv_time as bef_recv_time,"
            + "a.air_birthday as bef_air_birthday,"
            + "a.air_id_no as bef_air_id_no,"
            + "a.air_apr_user as bef_air_apr_user,"
            + "a.air_apr_date as bef_air_apr_date,"
            + "a.rjct_proc_code as bef_rjct_proc_code,"
            + "a.rjct_proc_remark as bef_rjct_proc_remark,"
            + "a.out_date as bef_out_date,"
            + "a.out_batchno as bef_out_batchno,"
            + "a.register_no as bef_register_no,"
            + "a.out_mark as bef_out_mark,"
            + "a.out_apr_user as bef_out_apr_user,"
            + "a.out_apr_date as bef_out_apr_date,"
            + "a.pay_date as bef_pay_date,"
            + "a.pay_gl_date as bef_pay_gl_date,"
            + "a.pay_apr_user as bef_pay_apr_user,"
            + "a.pay_apr_date as bef_pay_apr_date,"
            + "a.return_date as bef_return_date,"
            + "a.return_reason as bef_return_reason,"
            + "a.return_mark as bef_return_mark,"
            + "a.ret_apr_user as bef_ret_apr_user,"
            + "a.ret_apr_date as bef_ret_apr_date,"
            + "a.ret_gl_date as bef_ret_gl_date";

    wp.daoTable = controlTabName + " a "
            + "JOIN crd_idno b "
            + "ON a.id_p_seqno = b.id_p_seqno "
    ;
    wp.whereStr = "where 1=1 "
            + sqlCol(km1, "a.card_no")
            + sqlCol(km4, "a.gift_no")
            + sqlCol(km5, "a.vendor_no")
            + sqlCol(km7, "a.tran_seqno")
    ;

    pageSelect();
    if (sqlNotFind())
    {
      wp.notFound ="";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);

    if (wp.respHtml.indexOf("_detl") > 0)
      wp.colSet("btnStore_disable","");
    commCrtUser("comm_crt_user");
    commGiftName("comm_gift_no");
    commVendorName("comm_vendor_no");
    commBonusType1("comm_bonus_type");
    commProcFlag("comm_proc_flag");
    commGiftType("comm_gift_type");
    commFundCode("comm_fund_code");
    commFromMark("comm_from_mark");
    commSmsFlag("comm_sms_flag");
    commAirType("comm_air_type");
    commRjctCode("comm_rjct_proc_code");
    commModReason("comm_return_reason");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }
  // ************************************************************************
  void listWkdataAft() throws Exception
  {
  }
  // ************************************************************************
  void listWkdata() throws Exception
  {
    if (!wp.colStr("proc_flag").equals(wp.colStr("bef_proc_flag")))
      wp.colSet("opt_proc_flag","Y");
    commProcFlag("comm_proc_flag");
    commProcFlag("comm_bef_proc_flag");

    if (!wp.colStr("gift_type").equals(wp.colStr("bef_gift_type")))
      wp.colSet("opt_gift_type","Y");
    commGiftType("comm_gift_type");
    commGiftType("comm_bef_gift_type");

    if (!wp.colStr("cash_value").equals(wp.colStr("bef_cash_value")))
      wp.colSet("opt_cash_value","Y");

    if (!wp.colStr("total_pt").equals(wp.colStr("bef_total_pt")))
      wp.colSet("opt_total_pt","Y");

    if (!wp.colStr("exchg_pt").equals(wp.colStr("bef_exchg_pt")))
      wp.colSet("opt_exchg_pt","Y");

    if (!wp.colStr("exchg_amt").equals(wp.colStr("bef_exchg_amt")))
      wp.colSet("opt_exchg_amt","Y");

    if (!wp.colStr("end_tran_bp").equals(wp.colStr("bef_end_tran_bp")))
      wp.colSet("opt_end_tran_bp","Y");

    if (!wp.colStr("fund_code").equals(wp.colStr("bef_fund_code")))
      wp.colSet("opt_fund_code","Y");
    commFundCode("comm_fund_code");
    commFundCode("comm_bef_fund_code",1);

    if (!wp.colStr("tran_date").equals(wp.colStr("bef_tran_date")))
      wp.colSet("opt_tran_date","Y");

    if (!wp.colStr("from_mark").equals(wp.colStr("bef_from_mark")))
      wp.colSet("opt_from_mark","Y");
    commFromMark("comm_from_mark");
    commFromMark("comm_bef_from_mark");

    if (!wp.colStr("exchg_cnt").equals(wp.colStr("bef_exchg_cnt")))
      wp.colSet("opt_exchg_cnt","Y");

    if (!wp.colStr("exchg_mark").equals(wp.colStr("bef_exchg_mark")))
      wp.colSet("opt_exchg_mark","Y");

    if (!wp.colStr("exg_gl_date").equals(wp.colStr("bef_exg_gl_date")))
      wp.colSet("opt_exg_gl_date","Y");

    if (!wp.colStr("exg_apr_user").equals(wp.colStr("bef_exg_apr_user")))
      wp.colSet("opt_exg_apr_user","Y");

    if (!wp.colStr("exg_apr_date").equals(wp.colStr("bef_exg_apr_date")))
      wp.colSet("opt_exg_apr_date","Y");

    if (!wp.colStr("chi_name").equals(wp.colStr("bef_chi_name")))
      wp.colSet("opt_chi_name","Y");

    if (!wp.colStr("cellar_phone").equals(wp.colStr("bef_cellar_phone")))
      wp.colSet("opt_cellar_phone","Y");

    if (!wp.colStr("home_area_code1").equals(wp.colStr("bef_home_area_code1")))
      wp.colSet("opt_home_area_code1","Y");

    if (!wp.colStr("home_tel_no1").equals(wp.colStr("bef_home_tel_no1")))
      wp.colSet("opt_home_tel_no1","Y");

    if (!wp.colStr("home_tel_ext1").equals(wp.colStr("bef_home_tel_ext1")))
      wp.colSet("opt_home_tel_ext1","Y");

    if (!wp.colStr("office_area_code1").equals(wp.colStr("bef_office_area_code1")))
      wp.colSet("opt_office_area_code1","Y");

    if (!wp.colStr("office_tel_no1").equals(wp.colStr("bef_office_tel_no1")))
      wp.colSet("opt_office_tel_no1","Y");

    if (!wp.colStr("office_tel_ext1").equals(wp.colStr("bef_office_tel_ext1")))
      wp.colSet("opt_office_tel_ext1","Y");

    if (!wp.colStr("bill_sending_zip").equals(wp.colStr("bef_bill_sending_zip")))
      wp.colSet("opt_bill_sending_zip","Y");

    if (!wp.colStr("bill_sending_addr1").equals(wp.colStr("bef_bill_sending_addr1")))
      wp.colSet("opt_bill_sending_addr1","Y");

    if (!wp.colStr("bill_sending_addr2").equals(wp.colStr("bef_bill_sending_addr2")))
      wp.colSet("opt_bill_sending_addr2","Y");

    if (!wp.colStr("bill_sending_addr3").equals(wp.colStr("bef_bill_sending_addr3")))
      wp.colSet("opt_bill_sending_addr3","Y");

    if (!wp.colStr("bill_sending_addr4").equals(wp.colStr("bef_bill_sending_addr4")))
      wp.colSet("opt_bill_sending_addr4","Y");

    if (!wp.colStr("bill_sending_addr5").equals(wp.colStr("bef_bill_sending_addr5")))
      wp.colSet("opt_bill_sending_addr5","Y");

    if (!wp.colStr("ecoupon_bno").equals(wp.colStr("bef_ecoupon_bno")))
      wp.colSet("opt_ecoupon_bno","Y");

    if (!wp.colStr("ecoupon_date_s").equals(wp.colStr("bef_ecoupon_date_s")))
      wp.colSet("opt_ecoupon_date_s","Y");

    if (!wp.colStr("ecoupon_date_e").equals(wp.colStr("bef_ecoupon_date_e")))
      wp.colSet("opt_ecoupon_date_e","Y");

    if (!wp.colStr("ecoupon_date").equals(wp.colStr("bef_ecoupon_date")))
      wp.colSet("opt_ecoupon_date","Y");

    if (!wp.colStr("ecoupon_ret_date").equals(wp.colStr("bef_ecoupon_ret_date")))
      wp.colSet("opt_ecoupon_ret_date","Y");

    if (!wp.colStr("ecoupon_gl_date").equals(wp.colStr("bef_ecoupon_gl_date")))
      wp.colSet("opt_ecoupon_gl_date","Y");

    if (!wp.colStr("sms_flag").equals(wp.colStr("bef_sms_flag")))
      wp.colSet("opt_sms_flag","Y");
    commSmsFlag("comm_sms_flag");
    commSmsFlag("comm_bef_sms_flag");

    if (!wp.colStr("sms_date").equals(wp.colStr("bef_sms_date")))
      wp.colSet("opt_sms_date","Y");

    if (!wp.colStr("unpay_cnt").equals(wp.colStr("bef_unpay_cnt")))
      wp.colSet("opt_unpay_cnt","Y");

    if (!wp.colStr("sms_resend_desc").equals(wp.colStr("bef_sms_resend_desc")))
      wp.colSet("opt_sms_resend_desc","Y");

    if (!wp.colStr("air_type").equals(wp.colStr("bef_air_type")))
      wp.colSet("opt_air_type","Y");
    commAirType("comm_air_type");
    commAirType("comm_bef_air_type",1);

    if (!wp.colStr("cal_mile").equals(wp.colStr("bef_cal_mile")))
      wp.colSet("opt_cal_mile","Y");

    if (!wp.colStr("cal_dfpno").equals(wp.colStr("bef_cal_dfpno")))
      wp.colSet("opt_cal_dfpno","Y");

    if (!wp.colStr("passport_name").equals(wp.colStr("bef_passport_name")))
      wp.colSet("opt_passport_name","Y");

    if (!wp.colStr("passport_surname").equals(wp.colStr("bef_passport_surname")))
      wp.colSet("opt_passport_surname","Y");

    if (!wp.colStr("send_date").equals(wp.colStr("bef_send_date")))
      wp.colSet("opt_send_date","Y");

    if (!wp.colStr("send_time").equals(wp.colStr("bef_send_time")))
      wp.colSet("opt_send_time","Y");

    if (!wp.colStr("recv_date").equals(wp.colStr("bef_recv_date")))
      wp.colSet("opt_recv_date","Y");

    if (!wp.colStr("recv_time").equals(wp.colStr("bef_recv_time")))
      wp.colSet("opt_recv_time","Y");

    if (!wp.colStr("air_birthday").equals(wp.colStr("bef_air_birthday")))
      wp.colSet("opt_air_birthday","Y");

    if (!wp.colStr("air_id_no").equals(wp.colStr("bef_air_id_no")))
      wp.colSet("opt_air_id_no","Y");

    if (!wp.colStr("air_apr_user").equals(wp.colStr("bef_air_apr_user")))
      wp.colSet("opt_air_apr_user","Y");

    if (!wp.colStr("air_apr_date").equals(wp.colStr("bef_air_apr_date")))
      wp.colSet("opt_air_apr_date","Y");

    if (!wp.colStr("rjct_proc_code").equals(wp.colStr("bef_rjct_proc_code")))
      wp.colSet("opt_rjct_proc_code","Y");
    commRjctCode("comm_rjct_proc_code");
    commRjctCode("comm_bef_rjct_proc_code");

    if (!wp.colStr("rjct_proc_remark").equals(wp.colStr("bef_rjct_proc_remark")))
      wp.colSet("opt_rjct_proc_remark","Y");

    if (!wp.colStr("out_date").equals(wp.colStr("bef_out_date")))
      wp.colSet("opt_out_date","Y");

    if (!wp.colStr("out_batchno").equals(wp.colStr("bef_out_batchno")))
      wp.colSet("opt_out_batchno","Y");

    if (!wp.colStr("register_no").equals(wp.colStr("bef_register_no")))
      wp.colSet("opt_register_no","Y");

    if (!wp.colStr("out_mark").equals(wp.colStr("bef_out_mark")))
      wp.colSet("opt_out_mark","Y");

    if (!wp.colStr("out_apr_user").equals(wp.colStr("bef_out_apr_user")))
      wp.colSet("opt_out_apr_user","Y");

    if (!wp.colStr("out_apr_date").equals(wp.colStr("bef_out_apr_date")))
      wp.colSet("opt_out_apr_date","Y");

    if (!wp.colStr("pay_date").equals(wp.colStr("bef_pay_date")))
      wp.colSet("opt_pay_date","Y");

    if (!wp.colStr("pay_gl_date").equals(wp.colStr("bef_pay_gl_date")))
      wp.colSet("opt_pay_gl_date","Y");

    if (!wp.colStr("pay_apr_user").equals(wp.colStr("bef_pay_apr_user")))
      wp.colSet("opt_pay_apr_user","Y");

    if (!wp.colStr("pay_apr_date").equals(wp.colStr("bef_pay_apr_date")))
      wp.colSet("opt_pay_apr_date","Y");

    if (!wp.colStr("return_date").equals(wp.colStr("bef_return_date")))
      wp.colSet("opt_return_date","Y");

    if (!wp.colStr("return_reason").equals(wp.colStr("bef_return_reason")))
      wp.colSet("opt_return_reason","Y");
    commModReason("comm_return_reason");
    commModReason("comm_bef_return_reason",1);

    if (!wp.colStr("return_mark").equals(wp.colStr("bef_return_mark")))
      wp.colSet("opt_return_mark","Y");

    if (!wp.colStr("ret_apr_user").equals(wp.colStr("bef_ret_apr_user")))
      wp.colSet("opt_ret_apr_user","Y");

    if (!wp.colStr("ret_apr_date").equals(wp.colStr("bef_ret_apr_date")))
      wp.colSet("opt_ret_apr_date","Y");

    if (!wp.colStr("ret_gl_date").equals(wp.colStr("bef_ret_gl_date")))
      wp.colSet("opt_ret_gl_date","Y");

    if (wp.colStr("aud_type").equals("D"))
    {
      wp.colSet("proc_flag","");
      wp.colSet("gift_type","");
      wp.colSet("cash_value","");
      wp.colSet("total_pt","");
      wp.colSet("exchg_pt","");
      wp.colSet("exchg_amt","");
      wp.colSet("end_tran_bp","");
      wp.colSet("fund_code","");
      wp.colSet("tran_date","");
      wp.colSet("from_mark","");
      wp.colSet("exchg_cnt","");
      wp.colSet("exchg_mark","");
      wp.colSet("exg_gl_date","");
      wp.colSet("exg_apr_user","");
      wp.colSet("exg_apr_date","");
      wp.colSet("chi_name","");
      wp.colSet("cellar_phone","");
      wp.colSet("home_area_code1","");
      wp.colSet("home_tel_no1","");
      wp.colSet("home_tel_ext1","");
      wp.colSet("office_area_code1","");
      wp.colSet("office_tel_no1","");
      wp.colSet("office_tel_ext1","");
      wp.colSet("bill_sending_zip","");
      wp.colSet("bill_sending_addr1","");
      wp.colSet("bill_sending_addr2","");
      wp.colSet("bill_sending_addr3","");
      wp.colSet("bill_sending_addr4","");
      wp.colSet("bill_sending_addr5","");
      wp.colSet("ecoupon_bno","");
      wp.colSet("ecoupon_date_s","");
      wp.colSet("ecoupon_date_e","");
      wp.colSet("ecoupon_date","");
      wp.colSet("ecoupon_ret_date","");
      wp.colSet("ecoupon_gl_date","");
      wp.colSet("sms_flag","");
      wp.colSet("sms_date","");
      wp.colSet("unpay_cnt","");
      wp.colSet("sms_resend_desc","");
      wp.colSet("air_type","");
      wp.colSet("cal_mile","");
      wp.colSet("cal_dfpno","");
      wp.colSet("passport_name","");
      wp.colSet("passport_surname","");
      wp.colSet("send_date","");
      wp.colSet("send_time","");
      wp.colSet("recv_date","");
      wp.colSet("recv_time","");
      wp.colSet("air_birthday","");
      wp.colSet("air_id_no","");
      wp.colSet("air_apr_user","");
      wp.colSet("air_apr_date","");
      wp.colSet("rjct_proc_code","");
      wp.colSet("rjct_proc_remark","");
      wp.colSet("out_date","");
      wp.colSet("out_batchno","");
      wp.colSet("register_no","");
      wp.colSet("out_mark","");
      wp.colSet("out_apr_user","");
      wp.colSet("out_apr_date","");
      wp.colSet("pay_date","");
      wp.colSet("pay_gl_date","");
      wp.colSet("pay_apr_user","");
      wp.colSet("pay_apr_date","");
      wp.colSet("return_date","");
      wp.colSet("return_reason","");
      wp.colSet("return_mark","");
      wp.colSet("ret_apr_user","");
      wp.colSet("ret_apr_date","");
      wp.colSet("ret_gl_date","");
    }
  }
  // ************************************************************************
  void listWkdataSpace() throws Exception
  {
    if (wp.colStr("proc_flag").length()==0)
      wp.colSet("opt_proc_flag","Y");

    if (wp.colStr("gift_type").length()==0)
      wp.colSet("opt_gift_type","Y");

    if (wp.colStr("cash_value").length()==0)
      wp.colSet("opt_cash_value","Y");

    if (wp.colStr("total_pt").length()==0)
      wp.colSet("opt_total_pt","Y");

    if (wp.colStr("exchg_pt").length()==0)
      wp.colSet("opt_exchg_pt","Y");

    if (wp.colStr("exchg_amt").length()==0)
      wp.colSet("opt_exchg_amt","Y");

    if (wp.colStr("end_tran_bp").length()==0)
      wp.colSet("opt_end_tran_bp","Y");

    if (wp.colStr("fund_code").length()==0)
      wp.colSet("opt_fund_code","Y");

    if (wp.colStr("tran_date").length()==0)
      wp.colSet("opt_tran_date","Y");

    if (wp.colStr("from_mark").length()==0)
      wp.colSet("opt_from_mark","Y");

    if (wp.colStr("exchg_cnt").length()==0)
      wp.colSet("opt_exchg_cnt","Y");

    if (wp.colStr("exchg_mark").length()==0)
      wp.colSet("opt_exchg_mark","Y");

    if (wp.colStr("exg_gl_date").length()==0)
      wp.colSet("opt_exg_gl_date","Y");

    if (wp.colStr("exg_apr_user").length()==0)
      wp.colSet("opt_exg_apr_user","Y");

    if (wp.colStr("exg_apr_date").length()==0)
      wp.colSet("opt_exg_apr_date","Y");

    if (wp.colStr("chi_name").length()==0)
      wp.colSet("opt_chi_name","Y");

    if (wp.colStr("cellar_phone").length()==0)
      wp.colSet("opt_cellar_phone","Y");

    if (wp.colStr("home_area_code1").length()==0)
      wp.colSet("opt_home_area_code1","Y");

    if (wp.colStr("home_tel_no1").length()==0)
      wp.colSet("opt_home_tel_no1","Y");

    if (wp.colStr("home_tel_ext1").length()==0)
      wp.colSet("opt_home_tel_ext1","Y");

    if (wp.colStr("office_area_code1").length()==0)
      wp.colSet("opt_office_area_code1","Y");

    if (wp.colStr("office_tel_no1").length()==0)
      wp.colSet("opt_office_tel_no1","Y");

    if (wp.colStr("office_tel_ext1").length()==0)
      wp.colSet("opt_office_tel_ext1","Y");

    if (wp.colStr("bill_sending_zip").length()==0)
      wp.colSet("opt_bill_sending_zip","Y");

    if (wp.colStr("bill_sending_addr1").length()==0)
      wp.colSet("opt_bill_sending_addr1","Y");

    if (wp.colStr("bill_sending_addr2").length()==0)
      wp.colSet("opt_bill_sending_addr2","Y");

    if (wp.colStr("bill_sending_addr3").length()==0)
      wp.colSet("opt_bill_sending_addr3","Y");

    if (wp.colStr("bill_sending_addr4").length()==0)
      wp.colSet("opt_bill_sending_addr4","Y");

    if (wp.colStr("bill_sending_addr5").length()==0)
      wp.colSet("opt_bill_sending_addr5","Y");

    if (wp.colStr("ecoupon_bno").length()==0)
      wp.colSet("opt_ecoupon_bno","Y");

    if (wp.colStr("ecoupon_date_s").length()==0)
      wp.colSet("opt_ecoupon_date_s","Y");

    if (wp.colStr("ecoupon_date_e").length()==0)
      wp.colSet("opt_ecoupon_date_e","Y");

    if (wp.colStr("ecoupon_date").length()==0)
      wp.colSet("opt_ecoupon_date","Y");

    if (wp.colStr("ecoupon_ret_date").length()==0)
      wp.colSet("opt_ecoupon_ret_date","Y");

    if (wp.colStr("ecoupon_gl_date").length()==0)
      wp.colSet("opt_ecoupon_gl_date","Y");

    if (wp.colStr("sms_flag").length()==0)
      wp.colSet("opt_sms_flag","Y");

    if (wp.colStr("sms_date").length()==0)
      wp.colSet("opt_sms_date","Y");

    if (wp.colStr("unpay_cnt").length()==0)
      wp.colSet("opt_unpay_cnt","Y");

    if (wp.colStr("sms_resend_desc").length()==0)
      wp.colSet("opt_sms_resend_desc","Y");

    if (wp.colStr("air_type").length()==0)
      wp.colSet("opt_air_type","Y");

    if (wp.colStr("cal_mile").length()==0)
      wp.colSet("opt_cal_mile","Y");

    if (wp.colStr("cal_dfpno").length()==0)
      wp.colSet("opt_cal_dfpno","Y");

    if (wp.colStr("passport_name").length()==0)
      wp.colSet("opt_passport_name","Y");

    if (wp.colStr("passport_surname").length()==0)
      wp.colSet("opt_passport_surname","Y");

    if (wp.colStr("send_date").length()==0)
      wp.colSet("opt_send_date","Y");

    if (wp.colStr("send_time").length()==0)
      wp.colSet("opt_send_time","Y");

    if (wp.colStr("recv_date").length()==0)
      wp.colSet("opt_recv_date","Y");

    if (wp.colStr("recv_time").length()==0)
      wp.colSet("opt_recv_time","Y");

    if (wp.colStr("air_birthday").length()==0)
      wp.colSet("opt_air_birthday","Y");

    if (wp.colStr("air_id_no").length()==0)
      wp.colSet("opt_air_id_no","Y");

    if (wp.colStr("air_apr_user").length()==0)
      wp.colSet("opt_air_apr_user","Y");

    if (wp.colStr("air_apr_date").length()==0)
      wp.colSet("opt_air_apr_date","Y");

    if (wp.colStr("rjct_proc_code").length()==0)
      wp.colSet("opt_rjct_proc_code","Y");

    if (wp.colStr("rjct_proc_remark").length()==0)
      wp.colSet("opt_rjct_proc_remark","Y");

    if (wp.colStr("out_date").length()==0)
      wp.colSet("opt_out_date","Y");

    if (wp.colStr("out_batchno").length()==0)
      wp.colSet("opt_out_batchno","Y");

    if (wp.colStr("register_no").length()==0)
      wp.colSet("opt_register_no","Y");

    if (wp.colStr("out_mark").length()==0)
      wp.colSet("opt_out_mark","Y");

    if (wp.colStr("out_apr_user").length()==0)
      wp.colSet("opt_out_apr_user","Y");

    if (wp.colStr("out_apr_date").length()==0)
      wp.colSet("opt_out_apr_date","Y");

    if (wp.colStr("pay_date").length()==0)
      wp.colSet("opt_pay_date","Y");

    if (wp.colStr("pay_gl_date").length()==0)
      wp.colSet("opt_pay_gl_date","Y");

    if (wp.colStr("pay_apr_user").length()==0)
      wp.colSet("opt_pay_apr_user","Y");

    if (wp.colStr("pay_apr_date").length()==0)
      wp.colSet("opt_pay_apr_date","Y");

    if (wp.colStr("return_date").length()==0)
      wp.colSet("opt_return_date","Y");

    if (wp.colStr("return_reason").length()==0)
      wp.colSet("opt_return_reason","Y");

    if (wp.colStr("return_mark").length()==0)
      wp.colSet("opt_return_mark","Y");

    if (wp.colStr("ret_apr_user").length()==0)
      wp.colSet("opt_ret_apr_user","Y");

    if (wp.colStr("ret_apr_date").length()==0)
      wp.colSet("opt_ret_apr_date","Y");

    if (wp.colStr("ret_gl_date").length()==0)
      wp.colSet("opt_ret_gl_date","Y");

  }
  // ************************************************************************
  @Override
  public void dataProcess() throws Exception
  {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    String lsUser="";
    mktp01.Mktp0270Func func =new mktp01.Mktp0270Func(wp);

    String[] lsCardNo = wp.itemBuff("card_no");
    String[] lsGiftNo = wp.itemBuff("gift_no");
    String[] lsVendorNo = wp.itemBuff("vendor_no");
    String[] lsTranSeqno = wp.itemBuff("tran_seqno");
    String[] lsAudType  = wp.itemBuff("aud_type");
    String[] lsCrtUser  = wp.itemBuff("crt_user");
    String[] lsRowid     = wp.itemBuff("rowid");
    String[] opt =wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++)
    {
      if (opt[ii].length()==0) continue;
      rr = (int) (this.toNum(opt[ii])%20 - 1);
      if (rr==-1) rr = 19;
      if (rr<0) continue;

      wp.colSet(rr,"ok_flag","-");
      if (lsCrtUser[rr].equals(wp.loginUser))
      {
        ilAuth++;
        wp.colSet(rr,"ok_flag","F");
        continue;
      }

      lsUser=lsCrtUser[rr];
      if (!apprBankUnit(lsUser,wp.loginUser))
      {
        ilAuth++;
        wp.colSet(rr,"ok_flag","B");
        continue;
      }

      func.varsSet("card_no", lsCardNo[rr]);
      func.varsSet("gift_no", lsGiftNo[rr]);
      func.varsSet("vendor_no", lsVendorNo[rr]);
      func.varsSet("tran_seqno", lsTranSeqno[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A"))
        rc =func.dbInsertA4();
      else if (lsAudType[rr].equals("U"))
        rc =func.dbUpdateU4();
      else if (lsAudType[rr].equals("D"))
        rc =func.dbDeleteD4();

      if (rc!=1) alertErr(func.getMsg());
      if (rc == 1)
      {
/*         if (ls_proc_flag[rr].equals("A"))
            {
             rc= checkBonus(ls_tran_seqno[rr]);
             if (rc!=-1)
                {
                 this.sqlCommit(rc);
                 alertErr(func.getMsg());
                 break;
                }
            }
*/
        commGiftNo("comm_gift_no");
        commCrtUser("comm_crt_user");
        commProcFlagb("comm_proc_flag");
        commfuncAudType("aud_type");

        wp.colSet(rr,"ok_flag","V");
        ilOk++;
        func.dbDelete();
        this.sqlCommit(rc);
        continue;
      }
      ilErr++;
      wp.colSet(rr,"ok_flag","X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr+"; 權限問題=" + ilAuth);
    buttonOff("btnAdd_disable");
  }
  // ************************************************************************
  @Override
  public void initButton()
  {
    if (wp.respHtml.indexOf("_detl") > 0)
    {
      this.btnModeAud();
    }
  }
  // ************************************************************************
  @Override
  public void dddwSelect()
  {
    String lsSql ="";
    try {
      if ((wp.respHtml.equals("mktp0270")))
      {
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_gift_no").length()>0)
        {
          wp.optionKey = wp.colStr("ex_gift_no");
        }
        lsSql = "";
        lsSql =  procDynamicDddwGiftNo1(wp.colStr("ex_gift_no"));
        wp.optionKey = wp.colStr("ex_gift_no");
        dddwList("dddw_gift_no_1", lsSql);
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_crt_user").length()>0)
        {
          wp.optionKey = wp.colStr("ex_crt_user");
        }
        lsSql = "";
        lsSql =  procDynamicDddwCrtUser1(wp.colStr("ex_crt_user"));
        wp.optionKey = wp.colStr("ex_crt_user");
        dddwList("dddw_crt_user_1", lsSql);
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_proc_flag").length()>0)
        {
          wp.optionKey = wp.colStr("ex_proc_flag");
        }
        lsSql = "";
        lsSql =  procDynamicDddwProcFlag1(wp.colStr("ex_proc_flag"));
        wp.optionKey = wp.colStr("ex_proc_flag");
        dddwList("dddw_proc_flag_1", lsSql);
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_from_mark").length()>0)
        {
          wp.optionKey = wp.colStr("ex_from_mark");
        }
        lsSql = "";
        lsSql =  procDynamicDddwFromMark1(wp.colStr("ex_from_mark"));
        wp.optionKey = wp.colStr("ex_from_mark");
        dddwList("dddw_from_mark_1", lsSql);
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_gift_type").length()>0)
        {
          wp.optionKey = wp.colStr("ex_gift_type");
        }
        lsSql = "";
        lsSql =  procDynamicDddwGiftType1(wp.colStr("ex_gift_type"));
        wp.optionKey = wp.colStr("ex_gift_type");
        dddwList("dddw_gift_type_1", lsSql);
      }
    } catch(Exception ex){}
  }
  // ************************************************************************
  void commfuncAudType(String s1)
  {
    if (s1==null || s1.trim().length()==0) return;
    String[] cde = {"Y","A","U","D"};
    String[] txt = {"未異動","新增待覆核","更新待覆核","刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      wp.colSet(ii,"comm_func_"+s1, "");
      for (int inti=0;inti<cde.length;inti++)
        if (wp.colStr(ii,s1).equals(cde[inti]))
        {
          wp.colSet(ii,"commfunc_"+s1, txt[inti]);
          break;
        }
    }
  }
  // ************************************************************************
  public void commCrtUser(String s1) throws Exception
  {
    commCrtUser(s1,0);
    return;
  }
  // ************************************************************************
  public void commCrtUser(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " usr_cname as column_usr_cname "
              + " from sec_user "
              + " where 1 = 1 "
              + " and   usr_id = '"+wp.colStr(ii,befStr+"crt_user")+"'"
      ;
      if (wp.colStr(ii,befStr+"crt_user").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_usr_cname");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commGiftName(String s1) throws Exception
  {
    commGiftName(s1,0);
    return;
  }
  // ************************************************************************
  public void commGiftName(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " gift_name as column_gift_name "
              + " from mkt_gift "
              + " where 1 = 1 "
              + " and   gift_no = '"+wp.colStr(ii,befStr+"gift_no")+"'"
      ;
      if (wp.colStr(ii,befStr+"gift_no").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_gift_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commVendorName(String s1) throws Exception
  {
    commVendorName(s1,0);
    return;
  }
  // ************************************************************************
  public void commVendorName(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " vendor_name as column_vendor_name "
              + " from mkt_vendor "
              + " where 1 = 1 "
              + " and   vendor_no = '"+wp.colStr(ii,befStr+"vendor_no")+"'"
      ;
      if (wp.colStr(ii,befStr+"vendor_no").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_vendor_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commBonusType1(String s1) throws Exception
  {
    commBonusType1(s1,0);
    return;
  }
  // ************************************************************************
  public void commBonusType1(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " wf_desc as column_wf_desc "
              + " from ptr_sys_idtab "
              + " where 1 = 1 "
              + " and   wf_type = 'BONUS_NAME' "
              + " and   wf_id = '"+wp.colStr(ii,befStr+"bonus_type")+"'"
      ;
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commFundCode(String s1) throws Exception
  {
    commFundCode(s1,0);
    return;
  }
  // ************************************************************************
  public void commFundCode(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " fund_name as column_fund_name "
              + " from ptr_fundp "
              + " where 1 = 1 "
              + " and   fund_code = '"+wp.colStr(ii,befStr+"fund_code")+"'"
      ;
      if (wp.colStr(ii,befStr+"fund_code").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_fund_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commAirType(String s1) throws Exception
  {
    commAirType(s1,0);
    return;
  }
  // ************************************************************************
  public void commAirType(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " air_name as column_air_name "
              + " from mkt_air_parm "
              + " where 1 = 1 "
              + " and   air_type = '"+wp.colStr(ii,befStr+"air_TYPE")+"'"
      ;
      if (wp.colStr(ii,befStr+"air_TYPE").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_air_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commModReason(String s1) throws Exception
  {
    commModReason(s1,0);
    return;
  }
  // ************************************************************************
  public void commModReason(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " wf_desc as column_wf_desc "
              + " from ptr_sys_idtab "
              + " where 1 = 1 "
              + " and   wf_id = '"+wp.colStr(ii,befStr+"return_reason")+"'"
              + " and   wf_type = 'ADJMOD_REASON' "
      ;
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commGiftNo(String s1) throws Exception
  {
    commGiftNo(s1,0);
    return;
  }
  // ************************************************************************
  public void commGiftNo(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " gift_name as column_gift_name "
              + " from mkt_gift "
              + " where 1 = 1 "
              + " and   gift_no = '"+wp.colStr(ii,befStr+"gift_no")+"'"
      ;
      if (wp.colStr(ii,befStr+"gift_no").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_gift_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commProcFlag(String s1) throws Exception
  {
    String[] cde = {"N","A","B","C","D","E","F","G"};
    String[] txt = {"新兌換","航空退件","出貨","退貨","資料異動","航空請款","重發簡訊","一般請款"};
    String columnData="";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      for (int inti=0;inti<cde.length;inti++)
      {
        String s2 = s1.substring(5,s1.length());
        if (wp.colStr(ii,s2).equals(cde[inti]))
        {
          wp.colSet(ii, s1, txt[inti]);
          break;
        }
      }
    }
    return;
  }
  // ************************************************************************
  public void commGiftType(String s1) throws Exception
  {
    String[] cde = {"1","2","3"};
    String[] txt = {"實體商品","基金","電子商品"};
    String columnData="";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      for (int inti=0;inti<cde.length;inti++)
      {
        String s2 = s1.substring(5,s1.length());
        if (wp.colStr(ii,s2).equals(cde[inti]))
        {
          wp.colSet(ii, s1, txt[inti]);
          break;
        }
      }
    }
    return;
  }
  // ************************************************************************
  public void commFromMark(String s1) throws Exception
  {
    String[] cde = {"1","2","3"};
    String[] txt = {"1.人工登錄","2.語音","3.網路"};
    String columnData="";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      for (int inti=0;inti<cde.length;inti++)
      {
        String s2 = s1.substring(5,s1.length());
        if (wp.colStr(ii,s2).equals(cde[inti]))
        {
          wp.colSet(ii, s1, txt[inti]);
          break;
        }
      }
    }
    return;
  }
  // ************************************************************************
  public void commSmsFlag(String s1) throws Exception
  {
    String[] cde = {"N","Y","R"};
    String[] txt = {"未發送簡訊","已發送簡訊","重發送簡訊"};
    String columnData="";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      for (int inti=0;inti<cde.length;inti++)
      {
        String s2 = s1.substring(5,s1.length());
        if (wp.colStr(ii,s2).equals(cde[inti]))
        {
          wp.colSet(ii, s1, txt[inti]);
          break;
        }
      }
    }
    return;
  }
  // ************************************************************************
  public void commRjctCode(String s1) throws Exception
  {
    String[] cde = {"2","C"};
    String[] txt = {"修改重送","結案取消"};
    String columnData="";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      for (int inti=0;inti<cde.length;inti++)
      {
        String s2 = s1.substring(5,s1.length());
        if (wp.colStr(ii,s2).equals(cde[inti]))
        {
          wp.colSet(ii, s1, txt[inti]);
          break;
        }
      }
    }
    return;
  }
  // ************************************************************************
  public void commProcFlagb(String s1) throws Exception
  {
    String[] cde = {"N","A","B","C","D","E","F","G"};
    String[] txt = {"新兌換","航空退件","出貨","退貨","資料異動","航空請款","重發簡訊","一般請款"};
    String columnData="";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      for (int inti=0;inti<cde.length;inti++)
      {
        String s2 = s1.substring(5,s1.length());
        if (wp.colStr(ii,s2).equals(cde[inti]))
        {
          wp.colSet(ii, s1, txt[inti]);
          break;
        }
      }
    }
    return;
  }
  // ************************************************************************
  public void checkButtonOff() throws Exception
  {
    return;
  }
  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnAdd_disable");
    return;
  }
  // ************************************************************************
  public void funcSelect() throws Exception
  {
    return;
  }
  // ************************************************************************
// ************************************************************************
  String procDynamicDddwCrtUser1(String s1)  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " b.crt_user as db_code, "
            + " max(b.crt_user||' '||a.usr_cname) as db_desc "
            + " from sec_user a,mkt_gift_bpexchg_t b "
            + " where a.usr_id = b.crt_user "
            + " and   b.apr_flag = 'N' "
            + " group by b.crt_user "
    ;

    return lsSql;
  }
  // ************************************************************************
  String procDynamicDddwGiftNo1(String s1)  throws Exception
  {
    String ls_sql = "";

    ls_sql = " select "
            + " b.gift_no as db_code, "
            + " max(b.gift_no||' '||a.gift_name) as db_desc "
            + " from mkt_gift a,mkt_gift_bpexchg_t b "
            + " where a.gift_no = b.gift_no "
            + " and   b.apr_flag = 'N' "
            + " group by b.gift_no "
    ;

    return ls_sql;
  }
  // ************************************************************************
  String procDynamicDddwProcFlag1(String s1)  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " b.proc_flag as db_code, "
            + " max(b.proc_flag||' '||decode(b.proc_flag,"
            + "    'N','新兌換','A','航空退件','B','出貨','C','退>貨','D','資料異動', "
            + "     'E','航空請款','F','重發簡訊')) as db_desc "
            + " from mkt_gift_bpexchg_t b "
            + " where   b.apr_flag = 'N' "
            + " group by b.proc_flag "
    ;
    return lsSql;
  }
  // ************************************************************************
  String procDynamicDddwFromMark1(String s1)  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " b.from_mark as db_code, "
            + " max(b.from_mark||' '||decode(b.from_mark,"
            + "    '1','人工登錄','2','語音','3','網路')) as db_desc "
            + " from mkt_gift_bpexchg_t b "
            + " where   b.apr_flag = 'N' "
            + " group by b.from_mark "
    ;
    return lsSql;
  }
  // ************************************************************************
  String procDynamicDddwGiftType1(String s1)  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " b.gift_type as db_code, "
            + " max(b.gift_type||' '||decode(b.gift_type,"
            + "    '1','實體商品','2','基金','3','電子商品')) as db_desc "
            + " from mkt_gift_bpexchg_t b "
            + " where   b.apr_flag = 'N' "
            + " group by b.gift_type "
    ;
    return lsSql;
  }

// ************************************************************************

}  // End of class
