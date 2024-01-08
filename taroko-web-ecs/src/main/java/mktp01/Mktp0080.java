/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/10/30  V1.00.01   Allen Ho      Initial                              *
* 111/12/08  V1.00.02   Yang Bo       update naming rule                   *
***************************************************************************/
package mktp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0080 extends BaseProc
{
 private final String PROGNAME = "紅利利率轉換檔覆核作業處理程式111/12/08  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktp0080Func func = null;
  String kk1,kk2,kk3,kk4;
  String km1,km2,km3,km4;
  String fstAprFlag = "";
  String orgTabName = "cyc_bpid_t";
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
  else if (eqIgno(wp.buttonCode, "R3"))
     {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
     }
  else if (eqIgno(wp.buttonCode, "R2"))
     {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
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

  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")
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
               + "a.years,"
               + "a.bonus_type,"
               + "a.acct_type,"
               + "a.item_code,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.crt_user"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }

  commBonusType("comm_bonus_type");
  commAcctType("comm_acct_type");
  commCrtUser("comm_crt_user");

  commItemCode("comm_item_code");
  commfuncAudType("aud_type");

  //list_wkdata();
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
  if (wp.itemStr("kk_years").length()==0)
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
               + "a.aud_type,"
               + "a.years as years,"
               + "a.bonus_type as bonus_type,"
               + "a.acct_type as acct_type,"
               + "a.item_code as item_code,"
               + "a.crt_user,"
               + "a.other_item,"
               + "a.effect_months,"
               + "a.merchant_sel,"
               + "a.mcht_group_sel,"
               + "a.platform_kind_sel,"
               + "a.group_card_sel,"
               + "a.group_merchant_sel,"
               + "a.limit_1_beg,"
               + "a.limit_1_end,"
               + "a.exchange_1,"
               + "a.limit_2_beg,"
               + "a.limit_2_end,"
               + "a.exchange_2,"
               + "a.limit_3_beg,"
               + "a.limit_3_end,"
               + "a.exchange_3,"
               + "a.limit_4_beg,"
               + "a.limit_4_end,"
               + "a.exchange_4,"
               + "a.limit_5_beg,"
               + "a.limit_5_end,"
               + "a.exchange_5,"
               + "a.limit_6_beg,"
               + "a.limit_6_end,"
               + "a.exchange_6";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.years")
                   + sqlCol(km2, "a.bonus_type")
                   + sqlCol(km3, "a.acct_type")
                   + sqlCol(km4, "a.item_code")
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
  commItemCode("comm_item_code");
  commMerchant("comm_merchant_sel");
  commMchtFp("comm_mcht_group_sel");
  commMchtFp("comm_platform_kind_sel");
  commGroupCard("comm_group_card_sel");
  commGroupMcht("comm_group_merchant_sel");
  commCrtUser("comm_crt_user");
  commBonusType("comm_bonus_type");
  commAcctType("comm_acct_type");
  checkButtonOff();
  km1 = wp.colStr("years");
  km2 = wp.colStr("bonus_type");
  km3 = wp.colStr("acct_type");
  km4 = wp.colStr("item_code");
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
  controlTabName = "cyc_bpid";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.years as years,"
               + "a.bonus_type as bonus_type,"
               + "a.acct_type as acct_type,"
               + "a.item_code as item_code,"
               + "a.crt_user as bef_crt_user,"
               + "a.other_item as bef_other_item,"
               + "a.effect_months as bef_effect_months,"
               + "a.merchant_sel as bef_merchant_sel,"
               + "a.mcht_group_sel as bef_mcht_group_sel,"
               + "a.platform_kind_sel as bef_platform_kind_sel,"
               + "a.group_card_sel as bef_group_card_sel,"
               + "a.group_merchant_sel as bef_group_merchant_sel,"
               + "a.limit_1_beg as bef_limit_1_beg,"
               + "a.limit_1_end as bef_limit_1_end,"
               + "a.exchange_1 as bef_exchange_1,"
               + "a.limit_2_beg as bef_limit_2_beg,"
               + "a.limit_2_end as bef_limit_2_end,"
               + "a.exchange_2 as bef_exchange_2,"
               + "a.limit_3_beg as bef_limit_3_beg,"
               + "a.limit_3_end as bef_limit_3_end,"
               + "a.exchange_3 as bef_exchange_3,"
               + "a.limit_4_beg as bef_limit_4_beg,"
               + "a.limit_4_end as bef_limit_4_end,"
               + "a.exchange_4 as bef_exchange_4,"
               + "a.limit_5_beg as bef_limit_5_beg,"
               + "a.limit_5_end as bef_limit_5_end,"
               + "a.exchange_5 as bef_exchange_5,"
               + "a.limit_6_beg as bef_limit_6_beg,"
               + "a.limit_6_end as bef_limit_6_end,"
               + "a.exchange_6 as bef_exchange_6";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.years")
              + sqlCol(km2, "a.bonus_type")
              + sqlCol(km3, "a.acct_type")
              + sqlCol(km4, "a.item_code")
              ;

  pageSelect();
  if (sqlNotFind())
     {
      wp.notFound ="";
      return;
     }
  wp.colSet("control_tab_name", controlTabName); 
  commCrtUser("comm_crt_user");
  commBonusType("comm_bonus_type");
  commAcctType("comm_acct_type");
  commItemCode("comm_item_code");
  commMerchant("comm_merchant_sel");
  commMchtFp("comm_mcht_group_sel");
  commMchtFp("comm_platform_kind_sel");
  commGroupCard("comm_group_card_sel");
  commGroupMcht("comm_group_merchant_sel");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  listWkdataAft();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("merchant_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"1"));
  wp.colSet("mcht_group_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"4"));
  wp.colSet("platform_kind_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"P"));
  wp.colSet("group_card_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"2"));
  wp.colSet("group_merchant_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"3"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("other_item").equals(wp.colStr("bef_other_item")))
     wp.colSet("opt_other_item","Y");

  if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
     wp.colSet("opt_effect_months","Y");

  if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
     wp.colSet("opt_merchant_sel","Y");
  commMerchant("comm_merchant_sel");
  commMerchant("comm_bef_merchant_sel");

  wp.colSet("bef_merchant_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"1"));
  if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
     wp.colSet("opt_merchant_sel_cnt","Y");

  if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
     wp.colSet("opt_mcht_group_sel","Y");
  commMchtFp("comm_mcht_group_sel");
  commMchtFp("comm_bef_mcht_group_sel");

  wp.colSet("bef_mcht_group_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"4"));
  if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
     wp.colSet("opt_mcht_group_sel_cnt","Y");
  
  if (!wp.colStr("platform_kind_sel").equals(wp.colStr("bef_platform_kind_sel")))
	   wp.colSet("opt_platform_kind_sel","Y");
	  commMchtFp("comm_platform_kind_sel");
	  commMchtFp("comm_bef_platform_kind_sel");

  wp.colSet("bef_platform_kind_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"P"));
  if (!wp.colStr("platform_kind_sel_cnt").equals(wp.colStr("bef_platform_kind_sel_cnt")))
	  wp.colSet("opt_platform_kind_sel_cnt","Y");

  if (!wp.colStr("group_card_sel").equals(wp.colStr("bef_group_card_sel")))
     wp.colSet("opt_group_card_sel","Y");
  commGroupCard("comm_group_card_sel");
  commGroupCard("comm_bef_group_card_sel");

  wp.colSet("bef_group_card_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"2"));
  if (!wp.colStr("group_card_sel_cnt").equals(wp.colStr("bef_group_card_sel_cnt")))
     wp.colSet("opt_group_card_sel_cnt","Y");

  if (!wp.colStr("group_merchant_sel").equals(wp.colStr("bef_group_merchant_sel")))
     wp.colSet("opt_group_merchant_sel","Y");
  commGroupMcht("comm_group_merchant_sel");
  commGroupMcht("comm_bef_group_merchant_sel");

  wp.colSet("bef_group_merchant_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"3"));
  if (!wp.colStr("group_merchant_sel_cnt").equals(wp.colStr("bef_group_merchant_sel_cnt")))
     wp.colSet("opt_group_merchant_sel_cnt","Y");

  if (!wp.colStr("limit_1_beg").equals(wp.colStr("bef_limit_1_beg")))
     wp.colSet("opt_limit_1_beg","Y");

  if (!wp.colStr("limit_1_end").equals(wp.colStr("bef_limit_1_end")))
     wp.colSet("opt_limit_1_end","Y");

  if (!wp.colStr("exchange_1").equals(wp.colStr("bef_exchange_1")))
     wp.colSet("opt_exchange_1","Y");

  if (!wp.colStr("limit_2_beg").equals(wp.colStr("bef_limit_2_beg")))
     wp.colSet("opt_limit_2_beg","Y");

  if (!wp.colStr("limit_2_end").equals(wp.colStr("bef_limit_2_end")))
     wp.colSet("opt_limit_2_end","Y");

  if (!wp.colStr("exchange_2").equals(wp.colStr("bef_exchange_2")))
     wp.colSet("opt_exchange_2","Y");

  if (!wp.colStr("limit_3_beg").equals(wp.colStr("bef_limit_3_beg")))
     wp.colSet("opt_limit_3_beg","Y");

  if (!wp.colStr("limit_3_end").equals(wp.colStr("bef_limit_3_end")))
     wp.colSet("opt_limit_3_end","Y");

  if (!wp.colStr("exchange_3").equals(wp.colStr("bef_exchange_3")))
     wp.colSet("opt_exchange_3","Y");

  if (!wp.colStr("limit_4_beg").equals(wp.colStr("bef_limit_4_beg")))
     wp.colSet("opt_limit_4_beg","Y");

  if (!wp.colStr("limit_4_end").equals(wp.colStr("bef_limit_4_end")))
     wp.colSet("opt_limit_4_end","Y");

  if (!wp.colStr("exchange_4").equals(wp.colStr("bef_exchange_4")))
     wp.colSet("opt_exchange_4","Y");

  if (!wp.colStr("limit_5_beg").equals(wp.colStr("bef_limit_5_beg")))
     wp.colSet("opt_limit_5_beg","Y");

  if (!wp.colStr("limit_5_end").equals(wp.colStr("bef_limit_5_end")))
     wp.colSet("opt_limit_5_end","Y");

  if (!wp.colStr("exchange_5").equals(wp.colStr("bef_exchange_5")))
     wp.colSet("opt_exchange_5","Y");

  if (!wp.colStr("limit_6_beg").equals(wp.colStr("bef_limit_6_beg")))
     wp.colSet("opt_limit_6_beg","Y");

  if (!wp.colStr("limit_6_end").equals(wp.colStr("bef_limit_6_end")))
     wp.colSet("opt_limit_6_end","Y");

  if (!wp.colStr("exchange_6").equals(wp.colStr("bef_exchange_6")))
     wp.colSet("opt_exchange_6","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("other_item","");
       wp.colSet("effect_months","");
       wp.colSet("merchant_sel","");
       wp.colSet("merchant_sel_cnt","");
       wp.colSet("mcht_group_sel","");
       wp.colSet("mcht_group_sel_cnt","");
       wp.colSet("platform_kind_sel","");
       wp.colSet("platform_kind_sel_cnt","");
       wp.colSet("group_card_sel","");
       wp.colSet("group_card_sel_cnt","");
       wp.colSet("group_merchant_sel","");
       wp.colSet("group_merchant_sel_cnt","");
       wp.colSet("limit_1_beg","");
       wp.colSet("limit_1_end","");
       wp.colSet("exchange_1","");
       wp.colSet("limit_2_beg","");
       wp.colSet("limit_2_end","");
       wp.colSet("exchange_2","");
       wp.colSet("limit_3_beg","");
       wp.colSet("limit_3_end","");
       wp.colSet("exchange_3","");
       wp.colSet("limit_4_beg","");
       wp.colSet("limit_4_end","");
       wp.colSet("exchange_4","");
       wp.colSet("limit_5_beg","");
       wp.colSet("limit_5_end","");
       wp.colSet("exchange_5","");
       wp.colSet("limit_6_beg","");
       wp.colSet("limit_6_end","");
       wp.colSet("exchange_6","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("other_item").length()==0)
     wp.colSet("opt_other_item","Y");

  if (wp.colStr("effect_months").length()==0)
     wp.colSet("opt_effect_months","Y");

  if (wp.colStr("merchant_sel").length()==0)
     wp.colSet("opt_merchant_sel","Y");


  if (wp.colStr("mcht_group_sel").length()==0)
     wp.colSet("opt_mcht_group_sel","Y");

  if (wp.colStr("platform_kind_sel").length()==0)
     wp.colSet("opt_platform_kind_sel","Y");

  if (wp.colStr("group_card_sel").length()==0)
     wp.colSet("opt_group_card_sel","Y");


  if (wp.colStr("group_merchant_sel").length()==0)
     wp.colSet("opt_group_merchant_sel","Y");


  if (wp.colStr("limit_1_beg").length()==0)
     wp.colSet("opt_limit_1_beg","Y");

  if (wp.colStr("limit_1_end").length()==0)
     wp.colSet("opt_limit_1_end","Y");

  if (wp.colStr("exchange_1").length()==0)
     wp.colSet("opt_exchange_1","Y");

  if (wp.colStr("limit_2_beg").length()==0)
     wp.colSet("opt_limit_2_beg","Y");

  if (wp.colStr("limit_2_end").length()==0)
     wp.colSet("opt_limit_2_end","Y");

  if (wp.colStr("exchange_2").length()==0)
     wp.colSet("opt_exchange_2","Y");

  if (wp.colStr("limit_3_beg").length()==0)
     wp.colSet("opt_limit_3_beg","Y");

  if (wp.colStr("limit_3_end").length()==0)
     wp.colSet("opt_limit_3_end","Y");

  if (wp.colStr("exchange_3").length()==0)
     wp.colSet("opt_exchange_3","Y");

  if (wp.colStr("limit_4_beg").length()==0)
     wp.colSet("opt_limit_4_beg","Y");

  if (wp.colStr("limit_4_end").length()==0)
     wp.colSet("opt_limit_4_end","Y");

  if (wp.colStr("exchange_4").length()==0)
     wp.colSet("opt_exchange_4","Y");

  if (wp.colStr("limit_5_beg").length()==0)
     wp.colSet("opt_limit_5_beg","Y");

  if (wp.colStr("limit_5_end").length()==0)
     wp.colSet("opt_limit_5_end","Y");

  if (wp.colStr("exchange_5").length()==0)
     wp.colSet("opt_exchange_5","Y");

  if (wp.colStr("limit_6_beg").length()==0)
     wp.colSet("opt_limit_6_beg","Y");

  if (wp.colStr("limit_6_end").length()==0)
     wp.colSet("opt_limit_6_end","Y");

  if (wp.colStr("exchange_6").length()==0)
     wp.colSet("opt_exchange_6","Y");

 }
// ************************************************************************
 public void dataReadR3() throws Exception
 {
  dataReadR3(0);
 }
// ************************************************************************
 public void dataReadR3(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   commBonusType("comm_bonus_type");
   commAcctType("comm_acct_type");
   commItemCode("comm_item_code");
   this.selectNoLimit();
   bnTable = "cyc_bn_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "data_code2, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'CYC_BPID' "
                ;
   if (wp.respHtml.equals("mktp0080_mccd"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktp0080_gpcd"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktp0080_gpmc"))
      wp.whereStr  += " and data_type  = '3' ";
   String whereCnt = wp.whereStr;
   whereCnt += " and  data_key = '"+wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code")+"'";
   wp.whereStr  += " and  data_key = :data_key ";
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key",wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code"));
   wp.whereStr  += " order by 4,5,6 ";
   int cnt1= selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr2("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktp0080_gpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktp0080_gpcd"))
    commDataCode02("comm_data_code2");
   if (wp.respHtml.equals("mktp0080_gpmc"))
    commDataCode04("comm_data_code");
  }
// ************************************************************************
 public void dataReadR2() throws Exception
 {
  dataReadR2(0);
 }
// ************************************************************************
 public void dataReadR2(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   commBonusType("comm_bonus_type");
   commAcctType("comm_acct_type");
   commItemCode("comm_item_code");
   this.selectNoLimit();
   bnTable = "cyc_bn_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'CYC_BPID' "
                ;
   if (wp.respHtml.equals("mktp0080_mcgp"))
      wp.whereStr  += " and data_type  = '4' ";
   if (wp.respHtml.equals("mktp0080_mcgp2"))
	      wp.whereStr  += " and data_type  = 'P' ";
   String whereCnt = wp.whereStr;
   whereCnt += " and  data_key = '"+wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code")+"'";
   wp.whereStr  += " and  data_key = :data_key ";
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key",wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code"));
   wp.whereStr  += " order by 4,5 ";
   int cnt1= selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr2("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktp0080_mcgp"))
    commDataCode34("comm_data_code","1");
   if (wp.respHtml.equals("mktp0080_mcgp2"))
	commDataCode34("comm_data_code","2");
  }
// ************************************************************************
 public int selectBndataCount(String bndataTable, String whereStr ) throws Exception
 {
   String sql1 = "select count(*) as bndataCount"
               + " from " + bndataTable
               + " " + whereStr
               ;

   sqlSelect(sql1);

   return((int)sqlNum("bndataCount"));
 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  Mktp0080Func func =new Mktp0080Func(wp);

  String[] lsYears = wp.itemBuff("years");
  String[] lsBonusType = wp.itemBuff("bonus_type");
  String[] lsAcctType = wp.itemBuff("acct_type");
  String[] lsItemCode = wp.itemBuff("item_code");
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

     func.varsSet("years", lsYears[rr]);
     func.varsSet("bonus_type", lsBonusType[rr]);
     func.varsSet("acct_type", lsAcctType[rr]);
     func.varsSet("item_code", lsItemCode[rr]);
     func.varsSet("aud_type", lsAudType[rr]);
     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     if (lsAudType[rr].equals("A"))
        {
        rc =func.dbInsertA4();
        if (rc==1) rc = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }
     else if (lsAudType[rr].equals("U"))
        {
        rc =func.dbUpdateU4();
        if (rc==1) rc  = func.dbDeleteD4Bndata();
        if (rc==1) rc  = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }
     else if (lsAudType[rr].equals("D"))
        {
         rc =func.dbDeleteD4();
        if (rc==1) rc = func.dbDeleteD4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }

     if (rc!=1) alertErr2(func.getMsg());
     if (rc == 1)
        {
         commBonusType("comm_bonus_type");
         commAcctType("comm_acct_type");
         commCrtUser("comm_crt_user");
         commItemCode("comm_item_code");
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
  int rr = 0;                       
  rr = wp.listCount[0];             
  wp.colSet(0, "IND_NUM", "" + rr);
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String lsSql ="";
  try {
       if ((wp.respHtml.equals("mktp0080")))
         {
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
         }
       if ((wp.respHtml.equals("mktp0080_gpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_card_type"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0080_gpmc")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktp0080_mcgp")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_Code34"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 and platform_flag != '2' ");
         }
       if ((wp.respHtml.equals("mktp0080_mcgp2")))
       {
        wp.initOption ="";
        wp.optionKey = "";
        this.dddwList("dddw_data_Code34"
               ,"mkt_mcht_gp"
               ,"trim(mcht_group_id)"
               ,"trim(mcht_group_desc)"
               ," where 1 = 1 and platform_flag = '2' ");
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
 public void commBonusType(String s1) throws Exception 
 {
  commBonusType(s1,0);
  return;
 }
// ************************************************************************
 public void commBonusType(String s1, int befType) throws Exception 
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
 public void commAcctType(String s1) throws Exception 
 {
  commAcctType(s1,0);
  return;
 }
// ************************************************************************
 public void commAcctType(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chin_name as column_chin_name "
            + " from ptr_acct_type "
            + " where 1 = 1 "
            + " and   acct_type = '"+wp.colStr(ii,befStr+"acct_type")+"'"
            ;
       if (wp.colStr(ii,befStr+"acct_type").length()==0)
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
 public void commDataCode04(String s1) throws Exception 
 {
  commDataCode04(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode04(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " group_name as column_group_name "
            + " from ptr_group_code "
            + " where 1 = 1 "
            + " and   group_code = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_group_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode02(String s1) throws Exception 
 {
  commDataCode02(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode02(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " name as column_name "
            + " from ptr_card_type "
            + " where 1 = 1 "
            + " and   card_type = '"+wp.colStr(ii,befStr+"data_code2")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code2").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode34(String s1,String s2) throws Exception 
 {
  if(s2.equals("1")) {
	  commDataCode34(s1,0);
  }else {
	  commDataCode342(s1,0);
  }
  return;
 }
// ************************************************************************
 public void commDataCode34(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcht_group_desc as column_mcht_group_desc "
            + " from mkt_mcht_gp "
            + " where 1 = 1 and platform_flag != '2'"
            + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcht_group_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
 
 public void commDataCode342(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcht_group_desc as column_mcht_group_desc "
            + " from mkt_mcht_gp "
            + " where 1 = 1 and platform_flag = '2'"
            + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcht_group_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commItemCode(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"除預借現金之消費","預借現>金","循環利息"};
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
 public void commMerchant(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 public void commMchtFp(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 public void commGroupCard(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 public void commGroupMcht(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 public void initPage()
 {
  buttonOff("btnAdd_disable");
  return;
 }
// ************************************************************************
 String listCycBnData(String s1, String s2, String s3, String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where 1 = 1 "
              + " and   table_name = '"+s2+"'"
              + " and   data_key   = '"+s3+"'"
              + " and   data_type  = '"+s4+"'"
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

   return("0");
 }
// ************************************************************************
 String procDynamicDddwCrtUser1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.crt_user as db_code, "
          + " max(b.crt_user||' '||a.usr_cname) as db_desc "
          + " from sec_user a,cyc_bpid_t b "
          + " where a.usr_id = b.crt_user "
          + " and   b.apr_flag = 'N' "
          + " group by b.crt_user "
          ;

   return lsSql;
 }
// ************************************************************************

}  // End of class
