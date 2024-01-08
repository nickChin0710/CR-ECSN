/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/11/15  V1.01.01   Allen Ho      Initial                              *
* 111/11/30  V1.00.02   Yang Bo       sync code from mega                     *
* 111/12/13  V1.00.03   Zuwei Su      fix issue 新增資料 , 帶不出基本卡友資料                     *
* 113/01/03  V1.00.04   Ryan          增加選擇卡號的檢核條件                                                           *
* 113/01/05  V1.00.05   Ryan          VD不需要檢核未開卡                                                           *
***************************************************************************/
package mktm01;

import busi.ecs.MktBonus;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0270 extends BaseEdit
{
 private final String PROGNAME = "紅利贈品兌換登錄維護作業處理程式111/12/13 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0270Func func = null;
  String kk1,kk2,kk3;
  String km1,km2,km3;
  String fstAprFlag = "";
  String orgTabName = "mkt_gift_bpexchg";
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
  String cardTable = "";
  String idnoTable = "";
  String acnoTable = "";
  String vdFlag = "";
  String cardNote = "";

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
  else if (eqIgno(wp.buttonCode, "A"))
     {// 新增功能 -/
      strAction = "A";
      wp.itemSet("aud_type","A");
      insertFunc();
     }
  else if (eqIgno(wp.buttonCode, "U"))
     {/* 更新功能 */
      strAction = "U3";
      updateFuncU3R();
     }
  else if (eqIgno(wp.buttonCode, "I"))
     {/* 單獨新鄒功能 */
      strAction = "I";
/*
      kk1 = itemKk("data_k1");
      kk2 = itemKk("data_k2");
      kk3 = itemKk("data_k3");
*/
      clearFunc();
     }
  else if (eqIgno(wp.buttonCode, "D"))
     {/* 刪除功能 */
      deleteFuncD3R();
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
     } else if (eqIgno(wp.buttonCode, "AJAX")) {
        /* nothing to do */
        strAction = "AJAX";
        switch (wp.itemStr("ajaxMethod")) {
            case "wfButtonFunc4":
                wfButtonFunc4(wp);
                break;
            case "wfButtonFunc5":
                wfButtonFunc5(wp);
                break;
            case "wfButtonFunc7":
                wfButtonFunc7(wp);
                break;
            case "wfAjaxFunc1":
            case "wfAjaxFunc3":
                wfAjaxFunc1(wp);
                wfAjaxFunc3(wp);
                break;
            case "wfAjaxFunc2":
                wfAjaxFunc2(wp);
                break;
            case "wfAjaxFunc6":
                wfAjaxFunc6(wp);
                break;
            case "wfAjaxRedeemId":
            	dddOptionForCard(wp);
                break;
            default:
                break;
        }
        funcSelect();
    }
  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 "
              + sqlChkEx(wp.itemStr("ex_id_no"), "3", "")
              + sqlChkEx(wp.itemStr("ex_card_no"), "4", "")
              + sqlCol(wp.itemStr("ex_gift_no"), "a.gift_no", "like%")
              + sqlCol(wp.itemStr("ex_from_mark"), "a.from_mark")
              + sqlStrend(wp.itemStr("ex_tran_date_s"), wp.itemStr("ex_tran_date_e"), "a.tran_date")
              + sqlCol(wp.itemStr("ex_air_type"), "a.air_type")
              + sqlChkEx(wp.itemStr("ex_status_code"), "1", "")
              + sqlChkEx(wp.itemStr("ex_vendor_no"), "6", "")
              + sqlCol(wp.itemStr("ex_tran_seqno"), "a.tran_seqno", "like%")
              + sqlChkEx(wp.itemStr("ex_vip_code"), "7", "")
              + sqlCol(wp.itemStr("ex_apr_flag"), "a.apr_flag", "like%")
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
  if (wp.itemStr("ex_apr_flag").equals("N"))
     controlTabName = orgTabName +"_t";

  wp.pageControl();

  wp.selectSQL = " "
               + "hex(a.rowid) as rowid, "
               + "nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.tran_seqno,"
               + "a.tran_date,"
               + "'' as id_no,"
               + "'' as chi_name,"
               + "a.card_no,"
               + "a.gift_no,"
               + "a.exchg_cnt,"
               + "a.proc_flag,"
               + "a.status_flag,"
               + "a.total_pt,"
               + "a.crt_user,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by tran_date desc,tran_seqno desc"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commIdNo1("comm_id_no");
  commChiName1("comm_chi_name");
  commGiftNo("comm_gift_no");
  commCrtUser("comm_crt_user");

  commProcFlag("comm_proc_flag");
  commStatusFlag("comm_status_flag");

  wp.setPageValue();
 }
// ************************************************************************
 @Override
  public void querySelect() throws Exception
 {
  fstAprFlag = wp.itemStr("ex_apr_flag");
  if (wp.itemStr("ex_apr_flag").equals("N"))
     controlTabName = orgTabName +"_t";

  kk1 = itemKk("data_k1");
  qFrom=1;
  dataRead();
 }
// ************************************************************************
 @Override
 public void dataRead() throws Exception
 {
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
               + "a.p_seqno as p_seqno,"
               + "a.id_p_seqno as id_p_seqno,"
               + "a.tran_time as tran_time,"
               + "a.card_no as card_no,"
               + "a.gift_no as gift_no,"
               + "a.tran_seqno as tran_seqno,"
               + "a.apr_flag,"
               + "a.acct_type,"
               + "a.id_p_seqno,"
               + "a.p_seqno,"
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
               + "a.gift_type,"
               + "a.from_mark,"
               + "a.exchg_cnt,"
               + "a.cash_value,"
               + "a.bonus_type,"
               + "a.exchg_pt,"
               + "a.exchg_amt,"
               + "a.total_pt,"
               + "a.end_tran_bp,"
               + "a.end_tran_bp01,"
               + "a.end_tran_bp90,"
               + "a.deduct_bp01,"
               + "a.deduct_bp90,"
               + "a.exchg_mark,"
               + "a.tran_date,"
               + "a.proc_flag,"
               + "a.status_flag,"
               + "a.deduct_flag,"
               + "a.deduct_date,"
               + "a.exg_apr_user,"
               + "a.exg_apr_date,"
               + "a.exg_gl_date,"
               + "a.ecoupon_bno,"
               + "a.ecoupon_date_s,"
               + "a.ecoupon_date_e,"
               + "a.ecoupon_date,"
               + "a.unpay_cnt,"
               + "a.ecoupon_ret_date,"
               + "a.ecoupon_gl_date,"
               + "a.sms_flag,"
               + "a.sms_date,"
               + "a.sms_resend_desc,"
               + "a.air_type,"
               + "a.cal_dfpno,"
               + "a.air_id_no,"
               + "a.air_birthday,"
               + "a.passport_name,"
               + "a.passport_surname,"
               + "a.send_date,"
               + "a.send_time,"
               + "a.recv_date,"
               + "a.recv_time,"
               + "a.rjct_flag,"
               + "a.rjct_code,"
               + "a.rjct_msg,"
               + "a.rjct_proc_code,"
               + "a.rjct_proc_remark,"
               + "a.air_apr_user,"
               + "a.air_apr_date,"
               + "a.out_date,"
               + "a.out_batchno,"
               + "a.out_mark,"
               + "a.register_no,"
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
               + "a.ret_gl_date,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user,"
               + "a.fund_code";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.card_no")
                   + sqlCol(km2, "a.gift_no")
                   + sqlCol(km3, "a.tran_seqno")
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
   if (qFrom==0)
      {
       wp.colSet("aud_type","Y");
      }
   else
      {
       wp.colSet("aud_type",wp.itemStr("ex_apr_flag"));
       wp.colSet("fst_apr_flag",wp.itemStr("ex_apr_flag"));
      }
  datareadWkdata();
  commAprFlag2("comm_apr_flag");
  commGiftType("comm_gift_type");
  commFromMark("comm_from_mark");
  commProcFlag("comm_proc_flag");
  commStatusFlag("comm_status_flag");
  commDeductFlag("comm_deduct_flag");
  commRjctFlag("comm_rjct_flag");
  commGiftNamev("comm_gift_no");
  commAcctType("comm_acct_type");
  commBonusType1("comm_bonus_type");
  commExgAprUser("comm_exg_apr_user");
  commAirType("comm_air_type");
  commAirAprUser("comm_air_apr_user");
  commOutAprUser("comm_out_apr_user");
  commPayAprUser("comm_pay_apr_user");
  commRetAprUser("comm_ret_apr_user");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("card_no");
  km2 = wp.colStr("gift_no");
  km3 = wp.colStr("tran_seqno");
  commfuncAudType("aud_type");
  dataReadR3R();
 }
 
// ************************************************************************
 void datareadWkdata()
 {
	 
  if (wp.colStr("exg_apr_date").length()!=0) 
     buttonOff("buFunc4_disable"); 
  else wp.colSet("buFunc4_disable","");

  if (wp.colStr("send_time").length()==6)
     wp.colSet("send_time" , wp.colStr("send_time").substring(0,2)+":"
                            +  wp.colStr("send_time").substring(2,4)+":"
                            +  wp.colStr("send_time").substring(4,6));
  if (wp.colStr("recv_time").length()==6)
     wp.colSet("recv_time" , wp.colStr("recv_time").substring(0,2)+":"
                            +  wp.colStr("recv_time").substring(2,4)+":"
                            +  wp.colStr("recv_time").substring(4,6));
  String sql1="";

  sql1 = "select "
       + " id_no as redeem_id, "
       + " id_no||'-'||chi_name as id_no "
       + " from crd_idno "
       + " where id_p_seqno = '"+wp.colStr("id_p_seqno")+"'"
       + " union "
       + "select "
       + " id_no as redeem_id, "
       + " id_no||'-'||chi_name as id_no "
       + " from dbc_idno "
       + " where id_p_seqno = '"+wp.colStr("id_p_seqno")+"'"
       ;
   sqlSelect(sql1);

//   wp.itemSet("id_no", sqlStr("id_no"));
   wp.colSet("id_no", sqlStr("id_no"));
   wp.colSet("redeem_id", sqlStr("redeem_id"));
  sql1 = "select "
       + " vip_code "
       + " from act_acno "
       + " where p_seqno = '"+wp.colStr("p_seqno")+"'"
       + " union "
       + "select "
       + " vip_code "
       + " from dba_acno "
       + " where p_seqno = '"+wp.colStr("p_seqno")+"'"
       ;
   sqlSelect(sql1);

   wp.colSet("vip_code", sqlStr("vip_code"));

  sql1 = "select "
       + " fund_code||' - '||fund_name as fund_Code_name "
       + " from vmkt_fund_name "
       + " where fund_code = '"+wp.colStr("fund_code")+"'"
       ;
   sqlSelect(sql1);

   wp.colSet("fund_code_name", sqlStr("fund_code_name"));

   MktBonus comc = new MktBonus();
   comc.setConn(wp);
   int nowPt01 = comc.getEndTranBp01(strMid(wp.colStr("id_no"),0,10));
   int nowPt90 = comc.getEndTranBp90(strMid(wp.colStr("id_no"),0,10));
   int nowPt = nowPt01 + nowPt90;
   int deductBp01 = 0;
   int deductBp90 = 0;
   wp.colSet("end_tran_bp",String.format("%d",nowPt));
   wp.colSet("end_tran_bp01",String.format("%d",nowPt01));
   wp.colSet("end_tran_bp90",String.format("%d",nowPt90));

    sql1 = " select sum(total_pt) as in_total_pt "
         + " from mkt_gift_bpexchg_t "
         + " where  id_p_Seqno = '"+wp.colStr("id_p_seqno")+"' "
//         + " and    tran_seqno != '"+ wp.colStr("tran_seqno") +"' "
         ;
    sqlSelect(sql1);
   if (sqlStr("in_total_pt").length()==0)
      sqlSet(0,"in_total_pt","0");

//   double needPt = wp.colNum("exchg_cnt")*wp.colNum("exchg_pt");
   double needPt = 0;
   sqlSet(0,"total_pt",String.format("%,.0f",needPt));
   int[] deductBp = comc.countDeductBp(nowPt01,nowPt90,(int)needPt);
   if(deductBp != null) {
	   deductBp01 = deductBp[0];
	   deductBp90 = deductBp[1];
   }
   wp.colSet("deduct_bp01",String.format("%d", deductBp01));	 
   wp.colSet("deduct_bp90",String.format("%d", deductBp90));

   String dspStr ="";
   if (sqlNum("in_total_pt")>0)
      dspStr = "(兌換中 "+ String.format("%,.0f",sqlNum("in_total_pt"))+" 點) ";
   if (nowPt - needPt - sqlNum("in_total_pt")>0)
      wp.colSet("diff_pt_msg", dspStr
                 + "兌換後剩餘 "
                 + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                 + " 點");
   else
      wp.colSet("diff_pt_msg", dspStr
                 + "兌換後不足 "
                 + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                 + " 點");
   wp.colSet("exchg_pt",String.format("%,.0f",wp.colNum("exchg_pt")));


 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.card_no as card_no,"
               + "a.gift_no as gift_no,"
               + "a.tran_seqno as tran_seqno,"
               + "a.apr_flag as apr_flag,"
               + "a.acct_type as acct_type,"
               + "a.id_p_seqno as id_p_seqno,"
               + "a.p_seqno as p_seqno,"
               + "a.chi_name as chi_name,"
               + "a.cellar_phone as cellar_phone,"
               + "a.home_area_code1 as home_area_code1,"
               + "a.home_tel_no1 as home_tel_no1,"
               + "a.home_tel_ext1 as home_tel_ext1,"
               + "a.office_area_code1 as office_area_code1,"
               + "a.office_tel_no1 as office_tel_no1,"
               + "a.office_tel_ext1 as office_tel_ext1,"
               + "a.bill_sending_zip as bill_sending_zip,"
               + "a.bill_sending_addr1 as bill_sending_addr1,"
               + "a.bill_sending_addr2 as bill_sending_addr2,"
               + "a.bill_sending_addr3 as bill_sending_addr3,"
               + "a.bill_sending_addr4 as bill_sending_addr4,"
               + "a.bill_sending_addr5 as bill_sending_addr5,"
               + "a.gift_type as gift_type,"
               + "a.from_mark as from_mark,"
               + "a.exchg_cnt as exchg_cnt,"
               + "a.cash_value as cash_value,"
               + "a.bonus_type as bonus_type,"
               + "a.exchg_pt as exchg_pt,"
               + "a.exchg_amt as exchg_amt,"
               + "a.total_pt as total_pt,"
               + "a.end_tran_bp as end_tran_bp,"
               + "a.end_tran_bp01 as end_tran_bp01,"
               + "a.end_tran_bp90 as end_tran_bp90,"
               + "a.deduct_bp01 as deduct_bp01,"
               + "a.deduct_bp90 as deduct_bp90,"
               + "a.exchg_mark as exchg_mark,"
               + "a.tran_date as tran_date,"
               + "a.proc_flag as proc_flag,"
               + "a.status_flag as status_flag,"
               + "a.deduct_flag as deduct_flag,"
               + "a.deduct_date as deduct_date,"
               + "a.exg_apr_user as exg_apr_user,"
               + "a.exg_apr_date as exg_apr_date,"
               + "a.exg_gl_date as exg_gl_date,"
               + "a.ecoupon_bno as ecoupon_bno,"
               + "a.ecoupon_date_s as ecoupon_date_s,"
               + "a.ecoupon_date_e as ecoupon_date_e,"
               + "a.ecoupon_date as ecoupon_date,"
               + "a.unpay_cnt as unpay_cnt,"
               + "a.ecoupon_ret_date as ecoupon_ret_date,"
               + "a.ecoupon_gl_date as ecoupon_gl_date,"
               + "a.sms_flag as sms_flag,"
               + "a.sms_date as sms_date,"
               + "a.sms_resend_desc as sms_resend_desc,"
               + "a.air_type as air_type,"
               + "a.cal_dfpno as cal_dfpno,"
               + "a.air_id_no as air_id_no,"
               + "a.air_birthday as air_birthday,"
               + "a.passport_name as passport_name,"
               + "a.passport_surname as passport_surname,"
               + "a.send_date as send_date,"
               + "a.send_time as send_time,"
               + "a.recv_date as recv_date,"
               + "a.recv_time as recv_time,"
               + "a.rjct_flag as rjct_flag,"
               + "a.rjct_code as rjct_code,"
               + "a.rjct_msg as rjct_msg,"
               + "a.rjct_proc_code as rjct_proc_code,"
               + "a.rjct_proc_remark as rjct_proc_remark,"
               + "a.air_apr_user as air_apr_user,"
               + "a.air_apr_date as air_apr_date,"
               + "a.out_date as out_date,"
               + "a.out_batchno as out_batchno,"
               + "a.out_mark as out_mark,"
               + "a.register_no as register_no,"
               + "a.out_apr_user as out_apr_user,"
               + "a.out_apr_date as out_apr_date,"
               + "a.pay_date as pay_date,"
               + "a.pay_gl_date as pay_gl_date,"
               + "a.pay_apr_user as pay_apr_user,"
               + "a.pay_apr_date as pay_apr_date,"
               + "a.return_date as return_date,"
               + "a.return_reason as return_reason,"
               + "a.return_mark as return_mark,"
               + "a.ret_apr_user as ret_apr_user,"
               + "a.ret_apr_date as ret_apr_date,"
               + "a.ret_gl_date as ret_gl_date,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user,"
               + "a.fund_code as fund_code";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.card_no")
              + sqlCol(km2, "a.gift_no")
              + sqlCol(km3, "a.tran_seqno")
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
  commAprFlag2("comm_apr_flag");
  commGiftNamev("comm_gift_no");
  commAcctType("comm_acct_type");
  commGiftType("comm_gift_type");
  commFromMark("comm_from_mark");
  commBonusType1("comm_bonus_type");
  commProcFlag("comm_proc_flag");
  commStatusFlag("comm_status_flag");
  commDeductFlag("comm_deduct_flag");
  commExgAprUser("comm_exg_apr_user");
  commAirType("comm_air_type");
  commRjctFlag("comm_rjct_flag");
  commAirAprUser("comm_air_apr_user");
  commOutAprUser("comm_out_apr_user");
  commPayAprUser("comm_pay_apr_user");
  commRetAprUser("comm_ret_apr_user");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commfuncAudType("aud_type");
  datareadWkdata();
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr("card_no");
   km2 = wp.itemStr("gift_no");
   km3 = wp.itemStr("tran_seqno");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      km1 = wp.itemStr("card_no");
      km2 = wp.itemStr("gift_no");
      km3 = wp.itemStr("tran_seqno");
      strAction = "D";
      deleteFunc();
      if (fstAprFlag.equals("Y"))
         {
          qFrom=0;
          controlTabName = orgTabName;
         }
     }
  else
     {
      strAction = "A";
      wp.itemSet("aud_type","D");
      insertFunc();
     }
  dataRead();
  wp.colSet("fst_apr_flag", fstAprFlag);
 }
// ************************************************************************
 public void updateFuncU3R()  throws Exception
 {
  
  if (wp.itemStr("exg_apr_date").length()!=0) 
     buttonOff("buFunc4_disable"); 
  else wp.colSet("buFunc4_disable","");
  qFrom=0; 
   km1 = wp.itemStr("card_no");
   km2 = wp.itemStr("gift_no");
   km3 = wp.itemStr("tran_seqno");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1)
         {
          dataReadR3R();;
          datareadWkdata();
         }
     }
  else
     {
      km1 = wp.itemStr("card_no");
      km2 = wp.itemStr("gift_no");
      km3 = wp.itemStr("tran_seqno");
      strAction = "A";
      wp.itemSet("aud_type","U");
      insertFunc();
      if (rc==1) dataRead();
     }
  wp.colSet("fst_apr_flag", fstAprFlag);
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktm01.Mktm0270Func func =new mktm01.Mktm0270Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr2(func.getMsg());
  this.sqlCommit(rc);
 }
// ************************************************************************
 @Override
 public void initButton()
 {
  if ((wp.respHtml.indexOf("_detl") > 0)||
      (wp.respHtml.indexOf("_nadd") > 0))
     {
      wp.colSet("btnUpdate_disable","");
      wp.colSet("btnDelete_disable","");
      this.btnModeAud();
     }
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String lsSql ="";
  try {
       if ((wp.respHtml.equals("mktm0270_nadd"))||
           (wp.respHtml.equals("mktm0270_detl")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("gift_no").length()>0)
             {
             wp.optionKey = wp.colStr("gift_no");
             }
          this.dddwList("dddw_gift_no"
                 ,"mkt_gift"
                 ,"trim(gift_no)"
                 ,"trim(gift_name)"
                 ," where disable_flag='N'");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("bill_sending_zip").length()>0)
             {
             wp.optionKey = wp.colStr("bill_sending_zip");
             }
          this.dddwList("dddw_zipcode"
                 ,"select zip_code as db_code , zip_code||zip_city||zip_town as db_desc from ptr_zipcode order by zip_code"
                        );
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("return_reason").length()>0)
             {
             wp.optionKey = wp.colStr("return_reason");
             }
          this.dddwList("dddw_return_reason"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='ADJMOD_REASON' and substr(wf_dsptype,2,1)='Y'");
         }
       if ((wp.respHtml.equals("mktm0270")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_air_type").length()>0)
             {
             wp.optionKey = wp.colStr("ex_air_type");
             }
          this.dddwList("dddw_air_type_b"
                 ,"mkt_air_parm"
                 ,"trim(air_type)"
                 ,"trim(air_name)"
                 ," where 1 = 1 ");
          wp.initOption ="";
         wp.optionKey = itemKk("ex_vendor_no");
          if (wp.colStr("ex_vendor_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_vendor_no");
             }
          lsSql = "";
          if ((wp.itemStr("ex_tran_date_s").length()!=0)&&
              (wp.itemStr("ex_tran_date_s").length()!=0))
             {
              lsSql =  procDynamicDddwVendorNo(wp.itemStr("ex_tran_date_s"),wp.itemStr("ex_tran_date_e"));

              wp.optionKey = wp.itemStr("ex_vendor_no");
              dddwList("dddw_vendor_no", lsSql);
              wp.colSet("ex_vendor_no", "");
             }
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  if (itemKk("ex_apr_flag").equals("Y"))
  if ((itemKk("ex_card_no").length()==0)&&
      (itemKk("ex_tran_seqno").length()==0)&&
      (itemKk("ex_acct_key").length()==0)&&
      (itemKk("ex_tran_date_s").length()==0)&&
      (itemKk("ex_tran_date_e").length()==0)&&
      (itemKk("ex_exchg_seqno").length()==0))
     {
      wp.itemSet("ex_tran_date_s",comm.nextNDate(wp.sysDate,-90));
      wp.colSet("ex_tran_date_s",comm.nextNDate(wp.sysDate,-90));
      wp.itemSet("ex_tran_date_e",wp.sysDate);
      wp.colSet("ex_tran_date_e",wp.sysDate);
     }

  if (wp.itemStr("ex_id_no").length()>0)
     {
      String idNo = wp.itemStr("ex_id_no");
      String idNoCode = "0";
      if (wp.itemStr("ex_id_no").length()>10)
         {
          idNoCode = wp.itemStr("ex_id_no").substring(10,wp.itemStr("ex_id_no").length());
          idNo = wp.itemStr("ex_id_no").substring(0,10);
         }
      String sql1 = "select id_p_seqno,chi_name "
                  + "from   crd_idno "
                  + "where  id_no      = '"+ idNo.toUpperCase() +"' "
                  + "and    id_no_code = '"+idNoCode+"' "
                  + " union "
                  + "select id_p_seqno,chi_name "
                  + "from   dbc_idno "
                  + "where  id_no      = '"+ idNo.toUpperCase() +"' "
                  + "and    id_no_code = '"+idNoCode+"' "
                  ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr2(" 查無此身分證號[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
          return(1);
         }

      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      return(0);
     }

  if (wp.itemStr("ex_card_no").length()>0)
     {
      String sql1 = "select a.id_p_seqno,a.chi_name "
                  + "from   crd_idno a,crd_card b "
                  + "where  a.id_p_seqno   = b.id_p_seqno "
                  + "and    b.card_no      = '"+wp.itemStr("ex_card_no")+"' "
                  + " union "
                  + "select a.id_p_seqno,a.chi_name "
                  + "from   dbc_idno a,dbc_card b "
                  + "where  a.id_p_seqno   = b.id_p_seqno "
                  + "and    b.card_no      = '"+wp.itemStr("ex_card_no")+"' "
                  ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr2(" 查無此信用卡號[ "+wp.itemStr("ex_card_no").toUpperCase() +"] 資料");
          return(1);
         }

      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name",sqlStr("chi_name"));
      return(0);
     }


  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol, String sqCond, String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
     {
      if (wp.itemStr("ex_status_code").equals("1")) 
         return " and exg_apr_date!='' and deduct_flag='1' ";
      else if (wp.itemStr("ex_status_code").equals("2")) 
         return " and exg_apr_date!='' and deduct_flag in ('Y','R')  and out_apr_date='' ";
      else if (wp.itemStr("ex_status_code").equals("3")) 
         return " and exg_apr_date!='' and deduct_flag='Y'  and out_apr_date!=''  ";
      else if (wp.itemStr("ex_status_code").equals("4")) 
         return " and ret_apr_date!='' ";
      else if (wp.itemStr("ex_status_code").equals("5")) 
         return " and pay_apr_date!='' and ret_apr_date='' ";
     }

  if (sqCond.equals("3"))
     {
      if (empty(wp.itemStr("ex_id_no"))) return "";
      return " and a.id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
     }

  if (sqCond.equals("4"))
     {
      if (empty(wp.itemStr("ex_card_no"))) return "";
      return " and a.id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
     }

  if (sqCond.equals("6"))
     {
      if (empty(wp.itemStr("ex_vendor_no"))) return "";
      return " and  a.gift_no in "
             + "    (select gift_no "
             + "     from   mkt_gift "
             + "     where  vendor_no = '"+ wp.itemStr("ex_vendor_no") +"') "
             ;
     }

  if (sqCond.equals("7"))
     {
      if (empty(wp.itemStr("ex_vip_code"))) return "";
      return " and exists (select p_seqno from act_acno where vip_code !='' and p_seqno = a.p_Seqno"
      		+ " UNION "
      		+ " select p_seqno from dba_acno where vip_code !='' and p_seqno = a.p_Seqno "
      		+ ") ";
     }

  return "";
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
 public void commGiftNamev(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";

       columnData="";
       sql1 = "select "
            + " a.gift_name||'['||a.vendor_no||'-'||b.vendor_name||']' as column_gift_name "
            + " from mkt_gift a,mkt_vendor b "
            + " where a.vendor_no = b.vendor_no "
            + " and   a.gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            ;

       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commAcctType(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chin_name as column_chin_name "
            + " from ptr_acct_type "
            + " where 1 = 1 "
            + " and   acct_type = '"+wp.colStr(ii,"acct_type")+"'"
            ;
       if (wp.colStr(ii,"acct_type").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chin_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commBonusType1(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " wf_desc as column_wf_desc "
            + " from ptr_sys_idtab "
            + " where 1 = 1 "
            + " and   wf_type = 'BONUS_NAME' "
            + " and   wf_id = '"+wp.colStr(ii,"bonus_type")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commExgAprUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"exg_apr_user")+"'"
            ;
       if (wp.colStr(ii,"exg_apr_user").length()==0)
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
 public void commAirType(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " air_name as column_air_name "
            + " from mkt_air_parm "
            + " where 1 = 1 "
            + " and   air_type = '"+wp.colStr(ii,"air_TYPE")+"'"
            ;
       if (wp.colStr(ii,"air_TYPE").length()==0)
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
 public void commAirAprUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"air_apr_user")+"'"
            ;
       if (wp.colStr(ii,"air_apr_user").length()==0)
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
 public void commOutAprUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"out_apr_user")+"'"
            ;
       if (wp.colStr(ii,"out_apr_user").length()==0)
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
 public void commPayAprUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"pay_apr_user")+"'"
            ;
       if (wp.colStr(ii,"pay_apr_user").length()==0)
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
 public void commRetAprUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"ret_apr_user")+"'"
            ;
       if (wp.colStr(ii,"ret_apr_user").length()==0)
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
 public void commCrtUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"crt_user")+"'"
            ;
       if (wp.colStr(ii,"crt_user").length()==0)
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
 public void commAprUser(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,"apr_user")+"'"
            ;
       if (wp.colStr(ii,"apr_user").length()==0)
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
 public void commIdNo1(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " id_no as column_id_no "
            + " from crd_idno "
            + " where 1 = 1 "
            + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
            + " UNION "
            + "select "
            + " id_no as column_id_no "
            + " from dbc_idno "
            + " where 1 = 1 "
            + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_id_no"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commChiName1(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chi_name as column_chi_name "
            + " from crd_idno "
            + " where 1 = 1 "
            + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
            + " UNION "
            + "select "
            + " chi_name as column_chi_name "
            + " from dbc_idno "
            + " where 1 = 1 "
            + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chi_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commGiftNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name "
            + " from mkt_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            ;
       if (wp.colStr(ii,"gift_no").length()==0)
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
 public void commAprFlag2(String s1) throws Exception 
 {
  String[] cde = {"N","U","Y"};
  String[] txt = {"待覆核","暫緩覆核","已覆核"};
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
  String[] cde = {"1","2","3","4"};
  String[] txt = {"人工登錄","語音","網路","其他"};
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
 public void commStatusFlag(String s1) throws Exception 
 {
  String[] cde = {"0","1","2","3","4","5","6","7","8"};
  String[] txt = {"兌換待覆核","兌換失敗","未出貨(一般)","未出貨(航空)","已出貨","已退貨","已請款(部份)","已請款(全部)","已請款(屆期)"};
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
 public void commDeductFlag(String s1) throws Exception 
 {
  String[] cde = {"N","E","R","C","Y"};
  String[] txt = {"尚未覆核","點數不足","點數圈存","取消結案","扣點成功"};
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
 public void commRjctFlag(String s1) throws Exception 
 {
  String[] cde = {"A","R","E","D","S","W"};
  String[] txt = {"接受","拒絕","資料錯誤","重複誦建","暫停處理","等待處理"};
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
 public void wfAjaxFunc3(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr("ax_win_card_no").length()==0) return;
  if (wp.itemStr("ax_win_exchg_cnt").length()==0) return;

  
  
  if (selectAjaxFunc30(
                    wp.itemStr("ax_win_card_no"),
                    wp.itemStr("ax_win_gift_no"),
                    wp.itemStr("ax_win_exchg_cnt"))!=0) 
     {
      wp.addJSON("acct_type","");
      wp.addJSON("acct_type_name","");
      wp.addJSON("vip_code","");
      wp.addJSON("id_p_seqno","");
      wp.addJSON("p_seqno","");
      wp.addJSON("chi_name","");
      wp.addJSON("cellar_phone","");
      wp.addJSON("home_area_code1","");
      wp.addJSON("home_tel_no1","");
      wp.addJSON("home_tel_ext1","");
      wp.addJSON("office_area_code1","");
      wp.addJSON("office_tel_no1","");
      wp.addJSON("office_tel_ext1","");
      wp.addJSON("bill_sending_zip","");
      wp.addJSON("bill_sending_addr1","");
      wp.addJSON("bill_sending_addr2","");
      wp.addJSON("bill_sending_addr3","");
      wp.addJSON("bill_sending_addr4","");
      wp.addJSON("bill_sending_addr5","");
      wp.addJSON("gift_type_name","");
      wp.addJSON("fund_code_name","");
      wp.addJSON("end_tran_bp","");
      wp.addJSON("end_tran_bp01","");
      wp.addJSON("end_tran_bp90","");
      wp.addJSON("deduct_bp01","");
      wp.addJSON("deduct_bp90","");
      wp.addJSON("cal_dfpno","");
      wp.addJSON("air_id_no","");
      wp.addJSON("air_birthday","");
      wp.addJSON("passport_name","");
      wp.addJSON("passport_surname","");
      wp.addJSON("cash_value","");
      wp.addJSON("total_pt","");
      wp.addJSON("diff_pt_msg","");
      return;
     }

  wp.addJSON("acct_type",sqlStr("acct_type"));
  wp.addJSON("acct_type_name",sqlStr("acct_type_name"));
  wp.addJSON("vip_code",sqlStr("vip_code"));
  wp.addJSON("id_p_seqno",sqlStr("id_p_seqno"));
  wp.addJSON("p_seqno",sqlStr("p_seqno"));
  wp.addJSON("chi_name",sqlStr("chi_name"));
  wp.addJSON("cellar_phone",sqlStr("cellar_phone"));
  wp.addJSON("home_area_code1",sqlStr("home_area_code1"));
  wp.addJSON("home_tel_no1",sqlStr("home_tel_no1"));
  wp.addJSON("home_tel_ext1",sqlStr("home_tel_ext1"));
  wp.addJSON("office_area_code1",sqlStr("office_area_code1"));
  wp.addJSON("office_tel_no1",sqlStr("office_tel_no1"));
  wp.addJSON("office_tel_ext1",sqlStr("office_tel_ext1"));
  wp.addJSON("bill_sending_zip",sqlStr("bill_sending_zip"));
  wp.addJSON("bill_sending_addr1",sqlStr("bill_sending_addr1"));
  wp.addJSON("bill_sending_addr2",sqlStr("bill_sending_addr2"));
  wp.addJSON("bill_sending_addr3",sqlStr("bill_sending_addr3"));
  wp.addJSON("bill_sending_addr4",sqlStr("bill_sending_addr4"));
  wp.addJSON("bill_sending_addr5",sqlStr("bill_sending_addr5"));
  wp.addJSON("gift_type_name",sqlStr("gift_type_name"));
  wp.addJSON("fund_code_name",sqlStr("fund_code_name"));
  wp.addJSON("end_tran_bp",sqlStr("end_tran_bp"));
  wp.addJSON("end_tran_bp01",sqlStr("end_tran_bp01"));
  wp.addJSON("end_tran_bp90",sqlStr("end_tran_bp90"));
  wp.addJSON("deduct_bp01",sqlStr("deduct_bp01"));
  wp.addJSON("deduct_bp90",sqlStr("deduct_bp90")); 
  wp.addJSON("cal_dfpno",sqlStr("cal_dfpno"));
  wp.addJSON("air_id_no",sqlStr("air_id_no"));
  wp.addJSON("air_birthday",sqlStr("air_birthday"));
  wp.addJSON("passport_name",sqlStr("passport_name"));
  wp.addJSON("passport_surname",sqlStr("passport_surname"));
  wp.addJSON("cash_value",sqlStr("cash_value"));
  wp.addJSON("total_pt",sqlStr("total_pt"));
  wp.addJSON("diff_pt_msg",sqlStr("diff_pt_msg"));
 }
 
//************************************************************************
 void dddOptionForCard(TarokoCommon wr) throws Exception {
	 super.wp = wr;
	 String redeemId = strMid(wp.itemStr("ax_win_redeem_id"),0,10);
	 wp.sqlCmd = "select '0' as vd_flag ,a.card_no from crd_card a join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
	 wp.sqlCmd += " where a.acct_type = '01' and a.current_code = '0' and a.sup_flag ='0' and b.id_no = :redeem_id ";
	 wp.sqlCmd += " union ";
	 wp.sqlCmd += " select '1' as vd_flag , a.card_no from dbc_card a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
	 wp.sqlCmd += " where a.acct_type ='90' and a.current_code = '0' and b.id_no = :redeem_id ";
	 wp.sqlCmd += " order by vd_flag , card_no ";
	 setString("redeem_id",redeemId);
	 sqlSelect();
	 wp.addJSON("option_card_no", this.dddwOption("card_no", "card_no"));
	 if(sqlRowNum <= 0) {
		 alertErr2("該戶不存在一般信用卡/Visa Debit金融卡,有效流通卡,可進行兌換");
	 }
 }
 
 int getVdFlag(String cardNo){
	   wp.sqlCmd = "select '0' as vd_flag from crd_card a join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
	   wp.sqlCmd += " where a.acct_type = '01' and a.current_code = '0' and a.sup_flag ='0' and a.card_no = :card_no ";
	   wp.sqlCmd += " union ";
	   wp.sqlCmd += " select '1' as vd_flag from dbc_card a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
	   wp.sqlCmd += " where a.acct_type ='90' and a.current_code = '0' and a.card_no = :card_no ";
	   setString("card_no", cardNo);
	   sqlSelect(); 
	   if(sqlRowNum <= 0) {
		  alertErr2("查無卡號,請輸入正確卡號");
		  return 1;
	   }
	   vdFlag = sqlStr("vd_flag");
	   if("0".equals(vdFlag)) {
			cardTable = "crd_card ";
			idnoTable = "crd_idno ";
			acnoTable = "act_acno ";
			cardNote = "a.card_note ";
		}
		 else if("1".equals(vdFlag)) {
			cardTable = "dbc_Card ";
			idnoTable = "dbc_idno ";
			acnoTable = "dba_acno ";
			cardNote = "substring(a.card_type,1,1) ";
		}
	   return 0;
 }
 
// ************************************************************************
 int selectAjaxFunc30(String s1, String s2, String s3) throws Exception
  {
   if(getVdFlag(s1) == 1)
   {
	   return 1;
   }
   
   wp.sqlCmd = " select "
             + " b.id_no,"
             + " b.id_no_code,"
             + " a.acct_type,"
             + " a.id_p_seqno,"
             + " a.major_card_no,"
             + " a.p_seqno,"
             + " a.current_code,"
             + " a.activate_flag "
             + " from "
             + cardTable
             + " a , "
             + idnoTable
             + " b "
             + " where a.card_no ='"+s1+"' "
             + " and   a.id_p_seqno = b.id_p_seqno "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"]查無資料");
       return 1;
       }

   
   
   

   if (!sqlStr("major_card_no").equals(s1))
      {
       alertErr2("只有正卡才可兌換");
       return 1;
      }

   if (!sqlStr("current_code").equals("0"))
      {
       wp.sqlCmd = " select card_no as other_card_no"
                 + " from "
                 + cardTable
                 + " where current_code = '0' "
                 + " and   p_seqno  = '" +sqlStr("p_seqno")+"' "
                 ;
       this.sqlSelect();

       if (sqlRowNum > 0)
          {
           alertErr2("卡號：["+s1+"] 非有效卡(請以其他有效卡兌換)");
           return 1;
          }
       
       wp.sqlCmd = " select "
                 + " wf_id"
                 + " from ptr_sys_idtab "
                 + " where wf_useredit = 'Y' "
                 + " and   wf_type = 'GIFT_SPECID' "
                 + " and   id_code2 >= '" + wp.sysDate +"' "
                 + " and   wf_id = (decode(length(wf_id),10,'"
                 + sqlStr("id_no")
                 +"','"
                 + sqlStr("id_no")+sqlStr("id_no_code")
                 +"')) "
                 ;
       this.sqlSelect();

       if (sqlRowNum<=0)
          { 
           alertErr2("卡號：["+s1+"] 非有效卡(該帳戶無有效卡)");
           return 1;
          }
      }
   if("0".equals(vdFlag)){
	   if (sqlStr("activate_flag").equals("1"))
	   {
		   alertErr2("卡號：["+s1+"] 未開卡");
       	return 1;
       }
   }
   wp.sqlCmd = " select "
             + " id_no||'_'||id_no_code||'-'||chi_name  as id_no ,"
             + " id_no as redeem_id, "
             + " chi_name ,"
             + " cellar_phone ,"
             + " home_area_code1 ,"
             + " home_tel_no1 ,"
             + " home_tel_ext1 ,"
             + " office_area_code1 ,"
             + " office_tel_no1 ,"
             + " office_tel_ext1 ,"
             + " eng_name as passport_name ,"
             + " birthday as air_birthday ,"
             + " id_no as air_id_no "
             + " from "
             + idnoTable
             + " where id_p_seqno ='"+sqlStr("id_p_seqno")+"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"] 卡人資料不存在");
       return 1;
      }

   wp.sqlCmd = " select "
             + " vip_code ,"
             + " bill_sending_zip ,"
             + " bill_sending_addr1 ,"
             + " bill_sending_addr2 ,"
             + " bill_sending_addr3 ,"
             + " bill_sending_addr4 ,"
             + " bill_sending_addr5 "
             + " from "
             + acnoTable
             + " where p_seqno ='"+sqlStr("p_seqno")+"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"] 帳戶資料["+sqlStr("p_seqno")+"]不存在");
       return 1;
      }
   if("0".equals(vdFlag)){
	   wp.sqlCmd = " select "
	             + " chin_name as acct_type_name "
	             + " from ptr_acct_type "
	             + " where  acct_type  ='"+sqlStr("acct_type")+"' "
	             ;
   }

   if("1".equals(vdFlag)){
	   wp.sqlCmd = " select "
	             + " chin_name as acct_type_name "
	             + " from dbp_acct_type "
	             + " where  acct_type  ='"+sqlStr("acct_type")+"' "
	             ;
   }

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"] 帳戶類別["+sqlStr("acct_type")+"]不存在");
       return 1;
      }

  if (s2.length()==0) return 0;

   wp.sqlCmd = " select "
             + " b.gift_type as gift_type ,"
             + " b.air_type as air_type ,"
             + " b.fund_code as fund_code ,"
             + " b.cash_value as cash_value ,"
             + " b.bonus_type as bonus_type "
             + " from  mkt_gift b "
             + " where b.gift_no ='"+s2+"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"] 贈品代碼 ["+sqlStr("gift_no")+"]不存在");
       return 1;
      }

  if (sqlStr("gift_type").equals("2"))
  if (sqlStr("fund_code").length()!=0)
     {
      wp.sqlCmd = " select "
                + " fund_Code||' - '||fund_name as fund_code_name "
                + " from vmkt_fund_name "
                + " where  fund_code  ='"+sqlStr("fund_code")+"' "
                ;

      this.sqlSelect();

      if (sqlRowNum<=0)
         {
          alertErr2("卡號：["+s1+"] 基金代碼["+sqlStr("fund_code")+"]不存在");
          return 1;
         }
     }

  if (sqlStr("air_type").length()==0)
      {
       sqlSet(0,"air_birthday","");
       sqlSet(0,"passport_name","");
       sqlSet(0,"passport_surname","");
       sqlSet(0,"air_id_no","");
       sqlSet(0,"cal_dfpno","");
      }
   else
      {
       wp.sqlCmd = " select "
                 + " passport_name as old_passport_name,"
                 + " passport_surname as old_passport_surname,"
                 + " cal_dfpno as old_cal_dfpno,"
                 + " air_birthday as old_air_birthday,"
                 + " air_id_no as old_air_id_no "
                 + " from  mkt_gift_bpexchg_t "
                 + " where id_p_seqno = '"+sqlStr("id_p_seqno")+"' "
                 + " and   air_type ='"+sqlStr("air_type")+"' "
                 ;
       this.sqlSelect();
       if (sqlRowNum<=0)
          {
           wp.sqlCmd = " select "
                     + " passport_name as old_passport_name,"
                     + " passport_surname as old_passport_surname,"
                     + " cal_dfpno as old_cal_dfpno,"
                     + " air_birthday as old_air_birthday,"
                     + " air_id_no as old_air_id_no "
                     + " from  mkt_gift_bpexchg "
                     + " where id_p_seqno = '"+sqlStr("id_p_seqno")+"' "
                     + " and   air_type ='"+sqlStr("air_type")+"' "
                     + " order by tran_date desc "
                     ;
           this.sqlSelect();
           if (sqlRowNum>0)
              {
               sqlSet(0,"air_birthday",sqlStr("old_air_birthday"));
               sqlSet(0,"passport_name",sqlStr("old_passport_name"));
               sqlSet(0,"passport_surname",sqlStr("old_passport_surname"));
               sqlSet(0,"air_id_no",sqlStr("old_air_id_no"));
               sqlSet(0,"cal_dfpno",sqlStr("old_cal_dfpno"));
              }
          }
       else
          {
           sqlSet(0,"air_birthday",sqlStr("old_air_birthday"));
           sqlSet(0,"passport_name",sqlStr("old_passport_name"));
           sqlSet(0,"passport_surname",sqlStr("old_passport_surname"));
           sqlSet(0,"air_id_no",sqlStr("old_air_id_no"));
           sqlSet(0,"cal_dfpno",sqlStr("old_cal_dfpno"));
          }
      }


   wp.sqlCmd = "select exchange_bp as exchg_pt, "
             + "       exchange_amt as exchg_amt "
             + "from "
             + cardTable
             + " a,mkt_gift_exchgdata b"
             +" where "
             + cardNote
             + " = b.card_note "
             +"and   b.group_code = a.group_code  "
             +"and   b.group_code != ''  "
             +"and   a.card_no = '"+s1+"' "
             +"and   b.gift_no = '"+s2+"' "
             ;
   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       wp.sqlCmd = "select exchange_bp as exchg_pt, "
                 + "       exchange_amt as exchg_amt "
                 + "from "
                 + cardTable
                 + " a,mkt_gift_exchgdata b"
       			 +" where "
       			 + cardNote
       			 + " = b.card_note "
                 +"and   b.group_code = ''  "
                 +"and   a.card_no = '"+s1+"' "
                 +"and   b.gift_no = '"+s2+"' "
                 ;

       this.sqlSelect();
      }

   if (sqlRowNum<=0)
      {
	   wp.sqlCmd = "select exchange_bp as exchg_pt, "
	           + "       exchange_amt as exchg_amt "
	           + " from "
	           + cardTable
	           + " a,mkt_gift_exchgdata b "
	   		   + " where "
	   		   + cardNote
	   		   + " = b.card_note "
	           + " and   b.group_code = ''  "
	           + " and   "
	           + cardNote
	           + "  = ''     " ;   
	   this.sqlSelect();
      }
   if (sqlRowNum<=0)
   {
       this.sqlSet(0,"exchg_pt","0");
       this.sqlSet(0,"exchg_amt","0");
       this.sqlSet(0,"cash_value","0");
       alertErr2("紅利商品["+s2+"]未設定兌換點數方式");
       return 1;
   }
   
   if (s3.length()==0) return 0;

   if (sqlNum("exchg_pt")==0)
      {
       alertErr2("紅利商品["+s2+"]設定兌換點數為 0, 不可兌換");
       return 1;
      }

   MktBonus comc = new MktBonus();
   comc.setConn(wp);
   int nowPt01 = comc.getEndTranBp01(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt90 = comc.getEndTranBp90(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt = nowPt01 + nowPt90;
   int deductBp01 = 0;
   int deductBp90 = 0;
   sqlSet(0,"end_tran_bp",String.format("%d",nowPt));
   sqlSet(0,"end_tran_bp01",String.format("%d",nowPt01));
   sqlSet(0,"end_tran_bp90",String.format("%d",nowPt90));
   
   if("1".equals(vdFlag)&&deductBp01>0) {
	   alertErr2("一般信用卡尚有點數,請選取信用卡卡號,優先兌換,其次Visa Debit卡號");
       return 1;
   }
   
   wp.sqlCmd = "select sum(total_pt) as in_total_pt "
             + "from mkt_gift_bpexchg_t"
             + " where  id_p_Seqno = '"+sqlStr("id_p_seqno")+"' "
             ;
   this.sqlSelect();
   if (sqlStr("in_total_pt").length()==0)
      sqlSet(0,"in_total_pt","0");

   double needPt = Integer.valueOf(s3)*sqlNum("exchg_pt");
   this.sqlSet(0,"total_pt",String.format("%.0f",needPt));
   int[] deductBp = comc.countDeductBp(nowPt01,nowPt90,(int)needPt);
   if(deductBp != null) {
	   deductBp01 = deductBp[0];
	   deductBp90 = deductBp[1];
   }else {
	   alertErr2("點數不足兌換交易");
	   return 0;
   }
   
   if(deductBp01 < 0 ) {
	   if("1".equals(vdFlag)) {
		   alertErr2("請選信用卡號");
		   return 0;
	   }
   }
   
   if(deductBp90 < 0 && deductBp01 == 0) {
	   if("0".equals(vdFlag)) {
		   alertErr2("回存帳戶金額請選Visa Debit卡號");
		   return 0;
	   }
   }
   
   sqlSet(0,"deduct_bp01",String.format("%d", deductBp01));	 
   sqlSet(0,"deduct_bp90",String.format("%d", deductBp90));
		   
   String dspStr ="";
   if (sqlNum("in_total_pt")>0)
      dspStr = "(兌換中 "+ String.format("%,.0f",sqlNum("in_total_pt"))+" 點) ";
   if (nowPt - needPt - sqlNum("in_total_pt")>0)
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後剩餘 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                    + " 點");
   else
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後不足 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                    + " 點");

   this.sqlSet(0,"diff_code", "001");
   wp.sqlCmd = "select 1 as in_exchg "
             + "from mkt_gift_bpexchg_t"
             + " where  p_Seqno = '"+sqlStr("p_seqno")+"' "
             + " and    gift_no ='"+s2+"' "
             ;
   this.sqlSelect();
   if (sqlRowNum>0)
      {
       alertErr2("1紅利商品["+s2+"]兌換待覆核中, 請確認是否重複兌換!");
       return 0;
      }

   wp.sqlCmd = "select max(tran_date) as in_tran_date "
             + "from mkt_gift_bpexchg"
             + " where  p_Seqno = '"+sqlStr("p_seqno")+"' "
             + " and    tran_date >='"+ comm.nextNDate(wp.sysDate,-30)+"' "
             + " and    gift_no ='"+s2+"' "
             ;
   this.sqlSelect();
   if (sqlRowNum>0)
   if (sqlStr("in_tran_date").length()!=0)
      {
       alertErr2("2紅利商品["+s2+"]最近於["+sqlStr("in_tran_date")+"]有兌換, 請確認是否重複兌換!");
       return 0;
      }

   return 0;
 }
// ************************************************************************
 public void wfAjaxFunc1(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr("ax_win_gift_no").length()==0) return;

  if (selectAjaxFunc10(
                    wp.itemStr("ax_win_gift_no"),
                    wp.itemStr("ax_win_card_no"),
                    wp.itemStr("ax_win_exchg_cnt"))!=0) 
     {
      wp.addJSON("id_no","");
      wp.addJSON("gift_type","");
      wp.addJSON("cash_value","");
      wp.addJSON("bonus_type","");
      wp.addJSON("bonus_type_name","");
      wp.addJSON("gift_type_name","");
      wp.addJSON("fund_code_name","");
      wp.addJSON("exchg_pt","");
      wp.addJSON("exchg_amt","");
      wp.addJSON("total_pt","");
      wp.addJSON("diff_pt_msg","");
      wp.addJSON("cal_dfpno","");
      wp.addJSON("air_id_no","");
      wp.addJSON("air_birthday","");
      wp.addJSON("passport_name","");
      wp.addJSON("passport_surname","");
      wp.addJSON("end_tran_bp","");
      wp.addJSON("end_tran_bp01","");
      wp.addJSON("end_tran_bp90","");
      wp.addJSON("deduct_bp01","");
      wp.addJSON("deduct_bp90","");
      return;
     }

  wp.addJSON("id_no",sqlStr("id_no"));
  wp.addJSON("gift_type",sqlStr("gift_type"));
  wp.addJSON("cash_value",sqlStr("cash_value"));
  wp.addJSON("bonus_type",sqlStr("bonus_type"));
  wp.addJSON("bonus_type_name",sqlStr("bonus_type_name"));
  wp.addJSON("gift_type_name",sqlStr("gift_type_name"));
  wp.addJSON("fund_code_name",sqlStr("fund_code_name"));
  wp.addJSON("exchg_pt",sqlStr("exchg_pt"));
  wp.addJSON("exchg_amt",sqlStr("exchg_amt"));
  wp.addJSON("total_pt",sqlStr("total_pt"));
  wp.addJSON("diff_pt_msg",sqlStr("diff_pt_msg"));
  wp.addJSON("cal_dfpno",sqlStr("cal_dfpno"));
  wp.addJSON("air_id_no",sqlStr("air_id_no"));
  wp.addJSON("air_birthday",sqlStr("air_birthday"));
  wp.addJSON("passport_name",sqlStr("passport_name"));
  wp.addJSON("passport_surname",sqlStr("passport_surname"));
  wp.addJSON("end_tran_bp",sqlStr("end_tran_bp"));
  wp.addJSON("end_tran_bp01",sqlStr("end_tran_bp01"));
  wp.addJSON("end_tran_bp90",sqlStr("end_tran_bp90"));
  wp.addJSON("end_tran_bp01",sqlStr("deduct_bp01"));
  wp.addJSON("end_tran_bp90",sqlStr("deduct_bp90"));

  if (wp.itemStr("ax_win_gift_no").length()==0) return;

  if (selectAjaxFunc11(
                    wp.itemStr("ax_win_gift_no"),
                    wp.itemStr("ax_win_card_no"),
                    wp.itemStr("ax_win_exchg_cnt"))!=0) 
     {
      wp.addJSON("air_type","");
      wp.addJSON("air_type_name","");
      return;
     }

  wp.addJSON("air_type",sqlStr("air_type"));
  wp.addJSON("air_type_name",sqlStr("air_type_name"));
 }
// ************************************************************************
 int selectAjaxFunc10(String s1, String s2, String s3) throws Exception
  {
	if(getVdFlag(s2)==1)
	{
		return 1;
	}
	 
	 
   wp.sqlCmd = " select "
             + " a.gift_type ,"
             + " a.vendor_no ,"
             + " decode(a.gift_type,'1','商品','2','基金') as gift_type_name ,"
             + " a.fund_code ,"
             + " a.cash_value ,"
             + " a.air_type ,"
             + " a.cal_mile ,"
             + " a.bonus_type "
             + " from  mkt_gift a "
             + " where a.gift_no ='"+s1+"' "
             ;
/*
   wp.sqlCmd = " select "
             + " gift_type ,"
             + " decode(gift_type,'1','商品','2','基金') as gift_type_name ,"
             + " fund_code ,"
             + " cash_value ,"
             + " air_type ,"
             + " cal_mile ,"
             + " bonus_type "
             + " from  mkt_gift "
             + " where gift_no ='"+s1+"' "
             ;
*/

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("贈品代碼：["+s1+"]查無資料");
       return 1;
      }
 
   wp.sqlCmd = " select "
            + " wf_desc as bonus_type_name "
            + " from ptr_sys_idtab "
            + " where 1 = 1 "
            + " and   wf_type = 'BONUS_NAME' "
            + " and   wf_id = '"+ sqlStr("bonus_type") +"'"
            ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("紅利類別：["+sqlStr("bonus_type")+"]查無資料");
       return 1;
      }

  if (sqlStr("gift_type").equals("2"))
  if (sqlStr("fund_code").length()!=0)
     {
      wp.sqlCmd = " select "
                + " fund_code||' - '||fund_name as fund_code_name "
                + " from vmkt_fund_name "
                + " where  fund_code  ='"+sqlStr("fund_code")+"' "
                ;

      this.sqlSelect();

      if (sqlRowNum<=0)
         {
          alertErr2("卡號：["+s1+"] 基金代碼["+sqlStr("fund_code")+"]不存在");
          return 1;
         }
     }


   if (sqlStr("air_type").length()>0)
      {
       wp.sqlCmd = " select "
                + " air_name as air_type_name "
                + " from mkt_air_parm "
                + " where 1 = 1 "
                + " and  air_type = '"+ sqlStr("air_type") +"'"
                ;

       this.sqlSelect();
       if (sqlRowNum<=0)
          {
           alertErr2("航空公司：["+sqlStr("air_type")+"]查無資料");
           return 1;
          }
      }

   if (s2.length()==0) return 0;

   wp.sqlCmd = " select "
             + " p_seqno, "
             + " a.id_p_seqno, "
             + " d.major_card_no, "
             + " a.eng_name as passport_name ,"
             + " a.birthday as air_birthday ,"
             + " a.id_no as air_id_no, "
             + " a.id_no as redeem_id, "
             + " a.id_no||'-'||chi_name as id_no "
             + " from "
             + cardTable
             + " d, "
             + idnoTable
             + " a "
             + " where a.id_p_seqno=d.id_p_seqno "
             + " and   d.card_no ='"+s2+"' "
             ;
   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("1111：["+sqlStr("air_type")+"]查無資料");
       return 1;
      }

   if (!sqlStr("major_card_no").equals(s2))
      {
       alertErr2("只有正卡才可兌換");
       return 1;
      }
   if (sqlStr("air_type").length()==0)
      {
       sqlSet(0,"air_birthday","");
       sqlSet(0,"passport_name","");
       sqlSet(0,"passport_surname","");
       sqlSet(0,"air_id_no","");
       sqlSet(0,"air_cal_mile","");
      }
   else
      {
       wp.sqlCmd = " select "
                 + " passport_name as old_passport_name,"
                 + " passport_surname as old_passport_surname,"
                 + " cal_dfpno as old_cal_dfpno,"
                 + " air_birthday as old_air_birthday,"
                 + " air_id_no as old_air_id_no "
                 + " from  mkt_gift_bpexchg_t "
                 + " where id_p_seqno = '"+sqlStr("id_p_seqno")+"' "
                 + " and   air_type ='"+sqlStr("air_type")+"' "
                 ;
       this.sqlSelect();
       if (sqlRowNum<=0)
          {
           wp.sqlCmd = " select "
                     + " passport_name as old_passport_name,"
                     + " passport_surname as old_passport_surname,"
                     + " cal_dfpno as old_cal_dfpno,"
                     + " air_birthday as old_air_birthday,"
                     + " air_id_no as old_air_id_no "
                     + " from  mkt_gift_bpexchg "
                     + " where id_p_seqno = '"+sqlStr("id_p_seqno")+"' "
                     + " and   air_type ='"+sqlStr("air_type")+"' "
                     + " order by tran_date desc "
                     ;
           this.sqlSelect();
           if (sqlRowNum>0)
              {
               sqlSet(0,"air_birthday",sqlStr("old_air_birthday"));
               sqlSet(0,"passport_name",sqlStr("old_passport_name"));
               sqlSet(0,"passport_surname",sqlStr("old_passport_surname"));
               sqlSet(0,"air_id_no",sqlStr("old_air_id_no"));
               sqlSet(0,"cal_dfpno",sqlStr("old_cal_dfpno"));
              }
          }
       else
          {
           sqlSet(0,"air_birthday",sqlStr("old_air_birthday"));
           sqlSet(0,"passport_name",sqlStr("old_passport_name"));
           sqlSet(0,"passport_surname",sqlStr("old_passport_surname"));
           sqlSet(0,"air_id_no",sqlStr("old_air_id_no"));
           sqlSet(0,"cal_dfpno",sqlStr("old_cal_dfpno"));
          }
      }

   wp.sqlCmd = "select exchange_bp as exchg_pt, "
             + "       exchange_amt as exchg_amt "
             + "from "
             + cardTable
             + " a,mkt_gift_exchgdata b "
             + " where "
             + cardNote
             + " = b.card_note "
             +"and   b.group_code = a.group_code  "
             +"and   b.group_code != ''  "
             +"and   a.card_no = '"+s2+"' "
             +"and   b.gift_no = '"+s1+"' "
             ;
   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       wp.sqlCmd = "select exchange_bp as exchg_pt, "
                 + "       exchange_amt as exchg_amt "
                 + " from "
                 + cardTable
                 + " a,mkt_gift_exchgdata b "
                 + " where "
                 + cardNote
                 + " = b.card_note "
                 + " and   b.group_code = ''  "
                 + " and   a.card_no = '"+s2+"' "
                 + " and   b.gift_no = '"+s1+"' "
                 ;

       this.sqlSelect();
      }

   if (sqlRowNum<=0)
      {
	   
	   wp.sqlCmd = "select exchange_bp as exchg_pt, "
	           + "       exchange_amt as exchg_amt "
	           + " from "
	           + cardTable
	           + " a,mkt_gift_exchgdata b "
	           + " where "
	           + cardNote
	           + " = b.card_note "
	           + " and   b.group_code = ''  "
	           + " and   "
	           + cardNote
	           + "  = ''     " ;   
	   this.sqlSelect();
      }
   if (sqlRowNum<=0)
   {
       this.sqlSet(0,"exchg_pt","0");
       this.sqlSet(0,"exchg_amt","0");
       this.sqlSet(0,"cash_value","0");
       alertErr2("紅利商品["+s1+"]未設定兌換點數方式");
       return 1;
   }
   if (s3.length()==0) return 0;

   if (sqlNum("exchg_pt")==0)
      {
       alertErr2("1紅利商品["+s1+"]設定兌換點數為 0, 不可兌換");
       return 1;
      }

//   wp.sqlCmd = "select int_rate_mcode  "
//             + " from  "
//             + acnoTable
//             + " where  p_Seqno = '"+sqlStr("p_seqno")+"' "
//             ;
//
//   this.sqlSelect();
//   if (sqlNum("int_rate_mcode") >=2) 
//      {
//       alertErr2("該帳戶已逾期["+(int)sqlNum("int_rate_mcode")+"]個月, 不可兌換(繳清後隔日即可兌換)");
//       return 1;
//      }


   busi.ecs.MktBonus comc = new busi.ecs.MktBonus();
   comc.setConn(wp);

   int nowPt01 = comc.getEndTranBp01(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt90 = comc.getEndTranBp90(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt = nowPt01 + nowPt90;
   int deductBp01 = 0;
   int deductBp90 = 0;
   sqlSet(0,"end_tran_bp",String.format("%d",nowPt));
   sqlSet(0,"end_tran_bp01",String.format("%d",nowPt01));
   sqlSet(0,"end_tran_bp90",String.format("%d",nowPt90));
   
   wp.sqlCmd = "select sum(total_pt) as in_total_pt "
             + "from mkt_gift_bpexchg_t"
             + " where  id_p_Seqno = '"+sqlStr("id_p_seqno")+"' "
             ;
   this.sqlSelect();
   if (sqlStr("in_total_pt").length()==0)
      sqlSet(0,"in_total_pt","0");

   double needPt = Integer.valueOf(s3)*sqlNum("exchg_pt");
   this.sqlSet(0,"total_pt",String.format("%,.0f",needPt));
   
   int[] deductBp = comc.countDeductBp(nowPt01,nowPt90,(int)needPt);
   if(deductBp != null) {
	   deductBp01 = deductBp[0];
	   deductBp90 = deductBp[1];
   }else {
	   alertErr2("點數不足兌換交易");
	   return 1;
   }
   sqlSet(0,"deduct_bp01",String.format("%d", deductBp01));	 
   sqlSet(0,"deduct_bp90",String.format("%d", deductBp90));
   
   if(deductBp01 < 0 ) {
	   if("1".equals(vdFlag)) {
		   alertErr2("請選信用卡號");
		   return 0;
	   }
   }
   
   if(deductBp90 < 0 && deductBp01 == 0) {
	   if("0".equals(vdFlag)) {
		   alertErr2("回存帳戶金額請選Visa Debit卡號");
		   return 0;
	   }
   }
   
   String dspStr =""; 
   if (sqlNum("in_total_pt")>0)
      dspStr = "(兌換中 "+ String.format("%,.0f",sqlNum("in_total_pt"))+" 點) ";
   if (nowPt - needPt - sqlNum("in_total_pt")>0)
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後剩餘 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                    + " 點");
   else
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後不足 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt"))) 
                    + " 點");

   this.sqlSet(0,"exchg_pt",String.format("%,.0f",sqlNum("exchg_pt")));
   this.sqlSet(0,"exchg_amt",String.format("%,.0f",sqlNum("exchg_amt")));

   int[]  chkFlag = new int [10];
   wp.sqlCmd = "select 1 as in_exchg "
             + "from mkt_gift_bpexchg_t"
             + " where  p_Seqno = '"+sqlStr("p_seqno")+"' "
             + " and    gift_no ='"+s1+"' "
             ;
   this.sqlSelect();
   if (sqlRowNum>0) 
      {
       chkFlag[1]=1;
      }
    else
      {
       wp.sqlCmd = "select max(tran_date) as in_tran_date "
                 + "from mkt_gift_bpexchg"
                 + " where  p_Seqno = '"+sqlStr("p_seqno")+"' "
                 + " and    tran_date >='"+ comm.nextNDate(wp.sysDate,-30)+"' "
                 + " and    gift_no ='"+s1+"' "
                 ;
       this.sqlSelect();
       if (sqlRowNum>0)
       if (sqlStr("in_tran_date").length()!=0)
          {
           chkFlag[2]=1;
          }
      }

   String[] chkMsg = new String[10];
   chkMsg[1] = "1紅利商品["+s1+"]兌換待覆核中, 請確認是否重複兌換!";
   chkMsg[2] = "1紅利商品["+s1+"]最近於["+sqlStr("in_tran_date")+"]有兌換, 請確認是否重複兌換!";

   for (int inti=0;inti<3;inti++)
   if (chkFlag[inti]==1)
      {
       alertErr2(chkMsg[inti]);
      }

   return 0;
 }

// ************************************************************************
 int selectAjaxFunc11(String s1, String s2, String s3) throws Exception
  {
   wp.sqlCmd = " select "
             + "b. air_type as air_type ,"
             + " c.air_name as air_type_name "
             + " from  mkt_gift b,mkt_air_parm c "
             + " where b.air_type=c.air_type "
             + " and   b.gift_no ='"+s1+"' "
             ;

   this.sqlSelect();

   return 0;
 }

// ************************************************************************
 public void wfButtonFunc5(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr("ax_win_card_no").length()==0) return;
  if (selectButtonFunc5(
                       wp.itemStr("ax_win_card_no"))!=0)
     {
      wp.addJSON("chi_name","");
      wp.addJSON("cellar_phone","");
      wp.addJSON("home_area_code1","");
      wp.addJSON("home_tel_no1","");
      wp.addJSON("home_tel_ext1","");
      wp.addJSON("office_area_code1","");
      wp.addJSON("office_tel_no1","");
      wp.addJSON("office_tel_ext1","");
      wp.addJSON("bill_sending_zip","");
      wp.addJSON("bill_sending_addr1","");
      wp.addJSON("bill_sending_addr2","");
      wp.addJSON("bill_sending_addr3","");
      wp.addJSON("bill_sending_addr4","");
      wp.addJSON("bill_sending_addr5","");
      return;
     }

  wp.addJSON("chi_name",sqlStr("chi_name"));
  wp.addJSON("cellar_phone",sqlStr("cellar_phone"));
  wp.addJSON("home_area_code1",sqlStr("home_area_code1"));
  wp.addJSON("home_tel_no1",sqlStr("home_tel_no1"));
  wp.addJSON("home_tel_ext1",sqlStr("home_tel_ext1"));
  wp.addJSON("office_area_code1",sqlStr("office_area_code1"));
  wp.addJSON("office_tel_no1",sqlStr("office_tel_no1"));
  wp.addJSON("office_tel_ext1",sqlStr("office_tel_ext1"));
  wp.addJSON("bill_sending_zip",sqlStr("bill_sending_zip"));
  wp.addJSON("bill_sending_addr1",sqlStr("bill_sending_addr1"));
  wp.addJSON("bill_sending_addr2",sqlStr("bill_sending_addr2"));
  wp.addJSON("bill_sending_addr3",sqlStr("bill_sending_addr3"));
  wp.addJSON("bill_sending_addr4",sqlStr("bill_sending_addr4"));
  wp.addJSON("bill_sending_addr5",sqlStr("bill_sending_addr5"));
 }
// ************************************************************************
int selectButtonFunc5(String s1)  throws Exception
 {
  wp.sqlCmd = " select "
            + " chi_name, "
            + " cellar_phone ," 
            + " home_area_code1 ," 
            + " home_tel_no1 ," 
            + " home_tel_ext1 ," 
            + " office_area_code1 ," 
            + " office_tel_no1 ," 
            + " office_tel_ext1 ," 
            + " bill_sending_zip ," 
            + " bill_sending_addr1 ," 
            + " bill_sending_addr2 ," 
            + " bill_sending_addr3 ," 
            + " bill_sending_addr4 ," 
            + " bill_sending_addr5 " 
            + " from  mkt_gift_bpexchg_t "
            + " where card_no ='"+s1+"' "
            + " and   tran_seqno = (select max(tran_seqno) "
            + "                      from mkt_gift_bpexchg_t "
            + "                      where card_no ='"+s1+"') "
            ;

  this.sqlSelect();
  if (sqlRowNum<=0)
     {
       wp.sqlCmd = " select "
                 + " chi_name, "
                 + " cellar_phone ,"
                 + " home_area_code1 ,"
                 + " home_tel_no1 ,"
                 + " home_tel_ext1 ,"
                 + " office_area_code1 ,"
                 + " office_tel_no1 ,"
                 + " office_tel_ext1 ,"
                 + " bill_sending_zip ,"
                 + " bill_sending_addr1 ,"
                 + " bill_sending_addr2 ,"
                 + " bill_sending_addr3 ,"
                 + " bill_sending_addr4 ,"
                 + " bill_sending_addr5 "
                 + " from  mkt_gift_bpexchg "
                 + " where card_no ='"+s1+"' "
                 + " and   tran_seqno = (select max(tran_seqno) " 
                 + "                      from mkt_gift_bpexchg "
                 + "                      where card_no ='"+s1+"') "
                 ;
      this.sqlSelect();
      if (sqlRowNum<=0)
         {
          alertErr2("卡號：["+s1+"]無兌換紀錄");
          return 1;
         }
     }

   return 0;
 }
// ************************************************************************
 public void wfButtonFunc7(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr("ax_win_card_no").length()==0) return;
  if (selectButtonFunc7(
                       wp.itemStr("ax_win_card_no"))!=0)
     {
      wp.addJSON("chi_name","");
      wp.addJSON("cellar_phone","");
      wp.addJSON("home_area_code1","");
      wp.addJSON("home_tel_no1","");
      wp.addJSON("home_tel_ext1","");
      wp.addJSON("office_area_code1","");
      wp.addJSON("office_tel_no1","");
      wp.addJSON("office_tel_ext1","");
      wp.addJSON("bill_sending_zip","");
      wp.addJSON("bill_sending_addr1","");
      wp.addJSON("bill_sending_addr2","");
      wp.addJSON("bill_sending_addr3","");
      wp.addJSON("bill_sending_addr4","");
      wp.addJSON("bill_sending_addr5","");
      return;
     }

  wp.addJSON("chi_name",sqlStr("chi_name"));
  wp.addJSON("cellar_phone",sqlStr("cellar_phone"));
  wp.addJSON("home_area_code1",sqlStr("home_area_code1"));
  wp.addJSON("home_tel_no1",sqlStr("home_tel_no1"));
  wp.addJSON("home_tel_ext1",sqlStr("home_tel_ext1"));
  wp.addJSON("office_area_code1",sqlStr("office_area_code1"));
  wp.addJSON("office_tel_no1",sqlStr("office_tel_no1"));
  wp.addJSON("office_tel_ext1",sqlStr("office_tel_ext1"));
  wp.addJSON("bill_sending_zip",sqlStr("bill_sending_zip"));
  wp.addJSON("bill_sending_addr1",sqlStr("bill_sending_addr1"));
  wp.addJSON("bill_sending_addr2",sqlStr("bill_sending_addr2"));
  wp.addJSON("bill_sending_addr3",sqlStr("bill_sending_addr3"));
  wp.addJSON("bill_sending_addr4",sqlStr("bill_sending_addr4"));
  wp.addJSON("bill_sending_addr5",sqlStr("bill_sending_addr5"));
 }
// ************************************************************************
int selectButtonFunc7(String s1)  throws Exception
 {
	
	if(getVdFlag(s1)==1)
	{
		return 1;
	}
	
   wp.sqlCmd = " select "
             + " id_p_seqno,"
             + " p_seqno "
             + " from "
             + cardTable
             + " where card_no ='"+s1+"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"]查無資料");
       return 1;
       }

   wp.sqlCmd = " select "
             + " chi_name ,"
             + " cellar_phone ,"
             + " home_area_code1 ,"
             + " home_tel_no1 ,"
             + " home_tel_ext1 ,"
             + " office_area_code1 ,"
             + " office_tel_no1 ,"
             + " office_tel_ext1 "
             + " from  "
             + idnoTable
             + " where id_p_seqno ='"+sqlStr("id_p_seqno")+"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"] 卡人資料不存在");
       return 1;
      }

   wp.sqlCmd = " select "
             + " bill_sending_zip ,"
             + " bill_sending_addr1 ,"
             + " bill_sending_addr2 ,"
             + " bill_sending_addr3 ,"
             + " bill_sending_addr4 ,"
             + " bill_sending_addr5 "
             + " from  "
             + acnoTable
             + " where p_seqno ='"+sqlStr("p_seqno")+"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s1+"] 帳戶資料["+sqlStr("p_seqno")+"]不存在");
       return 1;
      }

   return 0;
 }
// ************************************************************************
 public void wfAjaxFunc2(TarokoCommon wr) throws Exception
 {
  super.wp = wr;


  if (selectAjaxFunc20(
                    wp.itemStr("ax_win_bill_sending_zip"))!=0) 
     {
      wp.addJSON("bill_sending_addr1","");
      wp.addJSON("bill_sending_addr2","");
      return;
     }

  wp.addJSON("bill_sending_addr1",sqlStr("bill_sending_addr1"));
  wp.addJSON("bill_sending_addr2",sqlStr("bill_sending_addr2"));
 }
// ************************************************************************
 int selectAjaxFunc20(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " b.zip_city as bill_sending_addr1 ,"
             + " b.zip_town as bill_sending_addr2 "
             + " from  ptr_zipcode b "
             + " where b.zip_code ='"+s1+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      { 
       if (s1.length()==0)
          {
           alertErr2("收件人地址未輸入資料");
          }
       else
          {
           alertErr2("收件人地址["+s1+"]查無資料");
          }
       return 1; 
      } 

   return 0;
 }
// ************************************************************************
 public void wfAjaxFunc6(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr("ax_win_card_no").length()==0) return;
  if (wp.itemStr("ax_win_gift_no").length()==0) return;

  if (selectAjaxFunc60(
                    wp.itemStr("ax_win_exchg_cnt"),
                    wp.itemStr("ax_win_card_no"),
                    wp.itemStr("ax_win_gift_no"),
                    wp.itemStr("ax_win_tran_seqno"))!=0) 
     {
      wp.addJSON("exchg_pt","");
      wp.addJSON("exchg_amt","");
      wp.addJSON("total_pt","");
      wp.addJSON("diff_pt_msg","");
      return;
     }

  wp.addJSON("exchg_pt",sqlStr("exchg_pt"));
  wp.addJSON("exchg_amt",sqlStr("exchg_amt"));
  wp.addJSON("total_pt",sqlStr("total_pt"));
  wp.addJSON("diff_pt_msg",sqlStr("diff_pt_msg"));
 }
// ************************************************************************
 int selectAjaxFunc60(String s1, String s2, String s3, String s4) throws Exception
  {
	 
   if(getVdFlag(s2) == 1) {
	   return 1;
   }
	 
   wp.sqlCmd = " select "
             + " gift_type ,"
             + " fund_code ,"
             + " cash_value ,"
             + " air_type ,"
             + " bonus_type "
             + " from  mkt_gift "
             + " where gift_no ='"+s3+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("贈品代碼：["+s3+"]查無資料");
       return 1;
      }

   wp.sqlCmd = " select "
             + " p_seqno "
             + " from  "
             + cardTable
             + " where   card_no ='"+s2+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("卡號：["+s2+"]查無資料");
       return 1;
       }

   wp.sqlCmd = "select exchange_bp as exchg_pt, "
             + "       exchange_amt as exchg_amt "
             + " from "
             + cardTable
             + " a,mkt_gift_exchgdata b"
             + " where "
             + cardNote
             + " = b.card_note "
             +"and   b.group_code = a.group_code  "
             +"and   b.group_code != ''  "
             +"and   a.card_no = '"+s2+"' "
             +"and   b.gift_no = '"+s3+"' "
             ;
   this.sqlSelect();

   if (sqlRowNum<=0)

      {
       wp.sqlCmd = "select exchange_bp as exchg_pt, "
                 + "       exchange_amt as exchg_amt "
                 + "from "
                 + cardTable
                 + " a,mkt_gift_exchgdata b "
                 + " where "
                 + cardNote
                 + " = b.card_note "
                 +"and   b.group_code = ''  "
                 +"and   a.card_no = '"+s2+"' "
                 +"and   b.gift_no = '"+s3+"' "
                 ;

       this.sqlSelect();
      }

   if (sqlRowNum<=0)
      {
	   wp.sqlCmd = "select exchange_bp as exchg_pt, "
	           + "       exchange_amt as exchg_amt "
	           + " from "
	           + cardTable
	           + " a,mkt_gift_exchgdata b "
	           + " where "
	           + cardNote
	           + " = b.card_note "
	           + " and   b.group_code = ''  "
	           + " and   "
	           + cardNote
	           + "  = ''     " ;   
	   this.sqlSelect();
      }
   
   if (sqlRowNum<=0)
   {
       this.sqlSet(0,"exchg_pt","0");
       this.sqlSet(0,"exchg_amt","0");
       this.sqlSet(0,"cash_value","0");
       alertErr2("紅利商品["+s3+"]未設定兌換點數方式");
       return 1;
   }

   if (s1.length()==0) return 0;

   if (sqlNum("exchg_pt")==0)
      {
       alertErr2("紅利商品["+s1+"]設定兌換點數為 0, 不可兌換");
       return 1;
      }

   busi.ecs.MktBonus comc = new busi.ecs.MktBonus();
   comc.setConn(wp);

   int nowPt01 = comc.getEndTranBp01(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt90 = comc.getEndTranBp90(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt = nowPt01 + nowPt90;
   int deductBp01 = 0;
   int deductBp90 = 0;
   sqlSet(0,"end_tran_bp",String.format("%d",nowPt));
   sqlSet(0,"end_tran_bp01",String.format("%d",nowPt01));
   sqlSet(0,"end_tran_bp90",String.format("%d",nowPt90));

    sqlSet(0,"in_total_pt","0");
    if (s4.length()!=0)
       {
        wp.sqlCmd = " select sum(total_pt) as in_total_pt "
                  + " from mkt_gift_bpexchg_t "
                  + " where  id_p_Seqno = '"+sqlStr("id_p_seqno")+"' "
                  + " and    tran_seqno != '"+s4+"' "
                  ;
        this.sqlSelect();
        if (sqlStr("in_total_pt").length()==0)
           sqlSet(0,"in_total_pt","0");
       }

   double needPt = Integer.valueOf(s1)*sqlNum("exchg_pt");
   this.sqlSet(0,"total_pt",String.format("%,.0f",needPt));
   
   int[] deductBp = comc.countDeductBp(nowPt01,nowPt90,(int)needPt);
   if(deductBp != null) {
	   deductBp01 = deductBp[0];
	   deductBp90 = deductBp[1];
   }else {
	   alertErr2("點數不足兌換交易");
	   return 1;
   }
   sqlSet(0,"deduct_bp01",String.format("%d", deductBp01));	 
   sqlSet(0,"deduct_bp90",String.format("%d", deductBp90));
   
   String dspStr ="";
   if (sqlNum("in_total_pt")>0)
      dspStr = "(兌換中 "+ String.format("%,.0f",sqlNum("in_total_pt"))+" 點) ";
   if (nowPt - needPt - sqlNum("in_total_pt")>0)
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後剩餘 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                    + " 點");
   else
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後不足 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                    + " 點");
   this.sqlSet(0,"exchg_pt",String.format("%,.0f",sqlNum("exchg_pt")));
   this.sqlSet(0,"exchg_amt",String.format("%,.0f",sqlNum("exchg_amt")));
   return 0;
 }

// ************************************************************************
 public void wfButtonFunc4(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr("ax_win_gift_no").length()==0) return;
  if (wp.itemStr("ax_win_card_no").length()==0) return;
  if (wp.itemStr("ax_win_exchg_cnt").length()==0) return;
  if (wp.itemStr("ax_win_exchg_pt").length()==0) return;
  if (selectButtonFunc4(
                       wp.itemStr("ax_win_gift_no"),
                       wp.itemStr("ax_win_card_no"),
                       wp.itemStr("ax_win_exchg_cnt"),
                       wp.itemStr("ax_win_exchg_pt"),
                       wp.itemStr("ax_win_tran_seqno"))!=0)
     {
      wp.addJSON("end_tran_bp","");
      wp.addJSON("end_tran_bp01","");
      wp.addJSON("end_tran_bp90","");
      wp.addJSON("deduct_bp01","");
      wp.addJSON("deduct_bp90","");
      wp.addJSON("diff_pt_msg","");
      return;
     }

  wp.addJSON("end_tran_bp",sqlStr("end_tran_bp"));
  wp.addJSON("end_tran_bp01",sqlStr("end_tran_bp01"));
  wp.addJSON("end_tran_bp90",sqlStr("end_tran_bp90"));
  wp.addJSON("deduct_bp01",sqlStr("deduct_bp01"));
  wp.addJSON("deduct_bp90",sqlStr("deduct_bp90"));
  wp.addJSON("diff_pt_msg",sqlStr("diff_pt_msg"));
 }
// ************************************************************************
int selectButtonFunc4(String s1, String s2, String s3, String s4, String s5)  throws Exception
 {
   if(getVdFlag(s2)==1) {
	   return 1;
   }
	
	
   wp.sqlCmd = " select "
             + " id_p_seqno,"
             + " p_seqno "
             + " from "
             + cardTable
             + " where card_no ='"+s2+"' "
             ;

   this.sqlSelect();

   wp.sqlCmd = " select "
             + " bonus_type "
             + " from  mkt_gift "
             + " where gift_no ='"+s1+"' "
             ;

   this.sqlSelect();


   busi.ecs.MktBonus comc = new busi.ecs.MktBonus();
   comc.setConn(wp);
   
   int nowPt01 = comc.getEndTranBp01(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt90 = comc.getEndTranBp90(strMid(wp.itemStr("ax_win_redeem_id"),0,10));
   int nowPt = nowPt01 + nowPt90;
   int deductBp01 = 0;
   int deductBp90 = 0;
   wp.addJSON("end_tran_bp",String.format("%d",nowPt));
   wp.addJSON("end_tran_bp01",String.format("%d",nowPt01));
   wp.addJSON("end_tran_bp90",String.format("%d",nowPt90));
   
   this.sqlSet(0,"diff_pt_msg",""); 

   if (s3.length()==0) return 0;

   wp.sqlCmd = "select sum(total_pt) as in_total_pt "
             + "from mkt_gift_bpexchg_t"
             + " where  id_p_Seqno = '"+sqlStr("id_p_seqno")+"' "
             + " and   tran_Seqno != '"+s5+"' "
             ;
   this.sqlSelect();
   if (sqlStr("in_total_pt").length()==0)
      sqlSet(0,"in_total_pt","0");

   double needPt = Integer.valueOf(s3)*Integer.valueOf(s4.replace(",",""));
   String dspStr ="";
   
   int[] deductBp = comc.countDeductBp(nowPt01,nowPt90,(int)needPt);
   if(deductBp != null) {
	   deductBp01 = deductBp[0];
	   deductBp90 = deductBp[1];
   }else {
	   alertErr2("點數不足兌換交易");
	   return 1;
   }
   wp.addJSON("deduct_bp01",String.format("%d", deductBp01));	 
   wp.addJSON("deduct_bp90",String.format("%d", deductBp90));
   
   
   if (sqlNum("in_total_pt")>0)
      dspStr = "(兌換中 "+ String.format("%,.0f",sqlNum("in_total_pt"))+" 點) ";
   if (nowPt - needPt - sqlNum("in_total_pt")>0)
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後剩餘 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                    + " 點");
   else
      this.sqlSet(0,"diff_pt_msg", dspStr
                    + "兌換後不足 "
                    + String.format("%,.0f",(nowPt - needPt - sqlNum("in_total_pt")))
                    + " 點");

   return 0;
 }
// ************************************************************************
 public void wfAjaxFunc8(TarokoCommon wr) throws Exception
 {
  String ajaxjVendorNo = "";
  super.wp = wr;


  if (selectAjaxFunc80(
                    wp.itemStr("ax_win_tran_date_s"),wp.itemStr("ax_win_tran_date_e"))!=0) 
     {
      wp.addJSON("ajaxj_vendor_no", "");
      wp.addJSON("ajaxj_vendor_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_vendor_no", sqlStr(ii, "vendor_no"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_vendor_name", sqlStr(ii, "vendor_name"));
 }
// ************************************************************************
 int selectAjaxFunc80(String s1, String s2) throws Exception
  {
   if ((s1.length()==0)||(s2.length()==0)) return(0);
   wp.sqlCmd = " select "
             + " b.vendor_no, "
             + " max(c.vendor_name)  as vendor_name "
             + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
             + " where a.tran_date between  '" + s1 +"' "
             + "                      and   '" + s2 +"' "
             + " and   a.gift_no     = b.gift_no "
             + " and   c.vendor_no   = b.vendor_no "
             + " and   a.deduct_flag = 'Y' "
             + " group by b.vendor_no  "
             + " union  "
             + " select  "
             + " '' as vendor_noe, "
             + " '--'  as vendor_name "
             + " from SYSIBM.SYSDUMMY1 "
             + " order by 1  "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("查無贈品資料");
       return(1);
      }

   return(0);
 }

// ************************************************************************
 public void checkButtonOff() throws Exception
  {
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
  wp.colSet("exchg_cnt","1");


  return;
 }
// ************************************************************************
 public void funcSelect() throws Exception
 {
  wp.selectCnt = 1;
  commAprFlag2("comm_apr_flag");
  commGiftType("comm_gift_type");
  commFromMark("comm_from_mark");
  commProcFlag("comm_proc_flag");
  commStatusFlag("comm_status_flag");
  commDeductFlag("comm_deduct_flag");
  commRjctFlag("comm_rjct_flag");
  commGiftNamev("comm_gift_no");
  commAcctType("comm_acct_type");
  commBonusType1("comm_bonus_type");
  commExgAprUser("comm_exg_apr_user");
  commAirType("comm_air_type");
  commAirAprUser("comm_air_apr_user");
  commOutAprUser("comm_out_apr_user");
  commPayAprUser("comm_pay_apr_user");
  commRetAprUser("comm_ret_apr_user");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

  return;
 }
// ************************************************************************
// ************************************************************************
 String procDynamicDddwVendorNo(String s1, String s2)  throws Exception
 {
   String lsSql = "";

 lsSql = " select "
          + " b.vendor_no as db_code, "
          + " max(b.vendor_no||' '||c.vendor_name)  as db_desc "
          + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
          + " where a.tran_date between  '" + s1 +"' "
          + "                      and      '" + s2 +"' "
          + " and   a.gift_no     = b.gift_no "
          + " and   c.vendor_no  = b.vendor_no "
          + " and   a.deduct_flag = 'Y' "
          + " group by b.vendor_no  "
          + " union  "
          + " select  "
          + " '' as db_code, "
          + " '--'  as db_desc "
          + " from SYSIBM.SYSDUMMY1 "
          + " order by 1   "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
