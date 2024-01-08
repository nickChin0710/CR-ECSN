/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/01/03  V1.00.01   Allen Ho      Initial                              *
* 109/01/07  V1.00.02   Amber         Update                               *
* 109-04-20  v1.00.03   Andy          Update add throws Exception          *
* 109-09-11  v1.00.04   Amber         Update tot_cnt & sum_exchg_cnt       *
* 110-03-02  V1.00.05   Andy          update PDF隠碼作業->不隠碼寫LOG          *
* 110-08-09  V1.00.06   Amber         update add list_price		           *
* 110-08-26  V1.00.07   Amber         update Mantis:8498	
* 111-12-06  V1.00.08  Machao    sync from mega & updated for project coding standard	           *
* 111-12-14  V1.00.09  Machao         修改ajax調用方式	           *
***************************************************************************/
package mktr01;

import mktr01.Mktr0290Func;
import ofcapp.AppMsg;
import java.util.Arrays;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoExcel2;
import taroko.com.TarokoPDF;
// ************************************************************************
public class Mktr0290 extends BaseEdit
{
 private final String PROGNAME = "紅利積點兌換贈品登錄檔維護作業處理程式111-12-14 V1.00.09";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktr01.Mktr0290Func func = null;
  String kk1;
  String orgTabName = "mkt_gift_bpexchg";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String   batchNo     = "";
  int errorCnt=0,recCnt=0,notifyCnt=0,colNum=0;
  int[]  datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[50];
  String[] uploadFileDat= new String[50];
  String[] logMsg       = new String[20];
  String   upGroupType= "0";

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
  else if (eqIgno(wp.buttonCode, "procMethod_PDFLIST"))	  
     {/* 列印地址名條 */
	  //-check approve-
	  if (!checkApprove(wp.itemStr2("zz_apr_user"),wp.itemStr2("zz_apr_passwd"))){
		wp.respHtml = "TarokoErrorPDF";
		return;
	  }
      strAction = "U";
      procMethodPDFLIST();
     }
  else if (eqIgno(wp.buttonCode, "procMethod_PDFBATCH"))
     {/* 列印大宗掛號 */
	  //-check approve-
	  if (!checkApprove(wp.itemStr2("zz_apr_user"),wp.itemStr2("zz_apr_passwd"))){
		wp.respHtml = "TarokoErrorPDF";
		return;
	  }
      strAction = "U";
      procMethodPDFBATCH();
     }
  else if (eqIgno(wp.buttonCode, "A"))
     {// 新增功能 -/
      strAction = "A";
      insertFunc();
     }
  else if (eqIgno(wp.buttonCode, "U"))
     {/*  更新功能 */
      strAction = "U";
      updateFunc();
     }
  else if (eqIgno(wp.buttonCode, "D"))
     {/* 刪除功能 */
      deleteFunc();
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
  else if (eqIgno(wp.buttonCode, "AJAX"))
  {/* nothing to do */
   strAction = "";
   switch (wp.itemStr("methodName")) {
    case "wf_ajax_func_2":
        wfAjaxFunc2(wr);
        break;
    case "wf_ajax_func_3":
        wfAjaxFunc3(wr);
        break;

    default:
        break;
}
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
              + sqlStrend(wp.itemStr2("ex_tran_date_s"), wp.itemStr2("ex_tran_date_e"), "a.tran_date")
              + sqlCol(wp.itemStr2("ex_from_mark"), "a.from_mark", "like%")
              + sqlChkEx(wp.itemStr2("ex_vendor_no"), "1", "")
              + sqlChkEx(wp.itemStr2("ex_post_flag"), "7", "")
              + sqlCol(wp.itemStr2("ex_gift_no"), "a.gift_no", "like%")
              + " and a.gift_type='1'     "
              + " and a.deduct_flag='Y'     "
              + " and a.air_type=''     "
              + " and a.return_date=''     "
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
               + "a.tran_seqno,"
               + "a.tran_date,"
               + "'' as id_no,"
               + "'' as chi_name,"
               + "a.gift_no,"
               + "a.exchg_cnt,"
               + "a.gift_type,"
               + "a.from_mark,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by tran_date"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }
  //計算總筆數與兌換數量小計
  String lsSql = " SELECT COUNT(*) as tot_ct,sum(exchg_cnt) as sum_exchg_cnt "
          + "FROM ( "
          +"SELECT a.exchg_cnt "  
          +"FROM mkt_gift_bpexchg a "  
          +"WHERE 1=1  "
          + sqlStrend(wp.itemStr2("ex_tran_date_s"), wp.itemStr2("ex_tran_date_e"), "a.tran_date")
          + sqlCol(wp.itemStr2("ex_from_mark"), "a.from_mark", "like%")
          + sqlChkEx(wp.itemStr2("ex_vendor_no"), "1", "")
          + sqlChkEx(wp.itemStr2("ex_post_flag"), "7", "")
          + sqlCol(wp.itemStr2("ex_gift_no"), "a.gift_no", "like%")
          + " and a.gift_type='1' "
          + " and a.deduct_flag='Y' "
          + " and a.air_type='' "
          + " and a.return_date='' "
          +" ) "
          ;
	sqlSelect(lsSql);
	if (sqlRowNum > 0) {
		wp.colSet("tot_ct", sqlStr("tot_ct"));
		wp.colSet("sum_exchg_cnt", sqlStr("sum_exchg_cnt"));
	}

  commIdNo1("comm_id_no");
  commChiName1("comm_chi_name");
  commGiftNo("comm_gift_no");
  commListPrice("comm_list_price");// 20210809 Add (BRD)
  commGiftType("comm_gift_type");
  commFromMark("comm_from_mark");

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
  if (controlTabName.length()==0)
     {
      if (wp.colStr("control_tab_name").length()==0)
         controlTabName=orgTabName;
      else
         controlTabName=wp.colStr("control_tab_name");
     }
  else
     {
      if (wp.colStr("control_tab_name").length()!=0)
         controlTabName=wp.colStr("control_tab_name");
     }
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.gift_no,"
               + "'' as id_no1,"
               + "a.acct_type,"
               + "'' as chi_name1,"
               + "a.card_no,"
               + "a.bonus_type,"
               + "a.proc_flag,"
               + "a.tran_seqno,"
               + "a.gift_type,"
               + "a.from_mark,"
               + "a.exchg_cnt,"
               + "a.exchg_amt,"
               + "a.total_pt,"
               + "a.exchg_pt,"
               + "a.end_tran_bp,"
               + "a.fund_code,"
               + "a.cash_value,"
               + "a.exchg_mark,"
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
               + "a.exg_gl_date,"
               + "a.exg_apr_user,"
               + "a.exg_apr_date,"
               + "a.air_type,"
               + "a.cal_mile,"
               + "a.cal_dfpno,"
               + "a.passport_name,"
               + "a.passport_surname,"
               + "a.tran_date,"
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
               + "a.unpay_cnt,"
               + "a.return_reason,"
               + "a.return_date,"
               + "a.return_mark,"
               + "a.ret_apr_user,"
               + "a.ret_apr_date,"
               + "a.ret_gl_date,"
               + "a.user_pay_amt,"
               + "a.deduct_date,"
               + "a.ecoupon_bno,"
               + "a.ecoupon_date,"
               + "a.ecoupon_date_s,"
               + "a.ecoupon_date_e,"
               + "a.acct_date,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
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
      alertErr("查無資料, key= "+"["+ kk1 + "]");
      return;
     }
  commProcFlag("comm_proc_flag");
  commGiftType("comm_gift_type");
  commFromMark("comm_from_mark");
  commRjctCode("comm_rjct_proc_code");
  commGiftName("comm_gift_no");
  commIdNo("comm_id_no1");
  commAcctType("comm_acct_type");
  commChiName("comm_chi_name1");
  commBonusType1("comm_bonus_type");
  commFundCode("comm_fund_code");
  commAirType("comm_air_type");
  commModReason("comm_return_reason");
  commListPrice("comm_list_price");// 20210809 Add (BRD)
  checkButtonOff();
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktr01.Mktr0290Func func =new mktr01.Mktr0290Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr(func.getMsg());
  log(func.getMsg());
  this.sqlCommit(rc);
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
       if ((wp.respHtml.equals("mktr0290")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_vendor_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_vendor_no");
             }
          lsSql = "";
          if ((wp.itemStr2("ex_tran_date_s").length()!=0)&&
              (wp.itemStr2("ex_tran_date_e").length()!=0))
             {
              lsSql =  procDynamicDddwVendorNo(wp.itemStr2("ex_tran_date_s"),wp.itemStr2("ex_tran_date_e"));

              wp.optionKey = wp.itemStr2("ex_vendor_no");
              dddwList("dddw_vendor_no", lsSql);
              wp.colSet("ex_vendor_no", "");
             }
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_gift_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_gift_no");
             }
          lsSql = "";
          if ((wp.itemStr2("ex_tran_date_s").length()!=0)&&
              (wp.itemStr2("ex_tran_date_e").length()!=0)&&
              (wp.itemStr2("ex_vendor_no").length()!=0))
             {
              lsSql =  procDynamicDddwGiftNo(wp.itemStr2("ex_vendor_no"),
                                                  wp.itemStr2("ex_tran_date_s"),
                                                  wp.itemStr2("ex_tran_date_e"));
              wp.optionKey = wp.itemStr2("ex_gift_no");
              dddwList("dddw_gift_no", lsSql);
              wp.colSet("ex_gift_no", "");
             }
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
  if ((wp.itemStr2("ex_tran_date_s").length()==0)||
      (wp.itemStr2("ex_tran_date_e").length()==0))
     {
      alertErr("兌換日期起迄不可空白");
      return(1);
     }

  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt)
 {
  if (sqCond.equals("1"))
     {
      if (empty(wp.itemStr2("ex_vendor_no"))) return "";
      return " and  a.gift_no in "
             + "    (select gift_no "
             + "     from   mkt_gift "
             + "     where  vendor_no = '"+ wp.itemStr2("ex_vendor_no") +"') "
             ;
     }

  return "";
 }
// ************************************************************************
 public void commGiftName(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name"
            + " from mkt_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            ;
       if (wp.colStr(ii,"gift_no").length()==0) continue;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commIdNo(String s1) throws Exception 
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
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0) continue;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_id_no"); 
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
       if (wp.colStr(ii,"acct_type").length()==0) continue;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chin_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commChiName(String s1) throws Exception 
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
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0) continue;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chi_name"); 
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
 public void commFundCode(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " fund_name as column_fund_name "
            + " from ptr_fundp "
            + " where 1 = 1 "
            + " and   fund_code = '"+wp.colStr(ii,"fund_code")+"'"
            ;
       if (wp.colStr(ii,"fund_code").length()==0) continue;
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
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " wf_desc as column_wf_desc "
            + " from ptr_sys_idtab "
            + " where 1 = 1 "
            + " and   wf_id = '"+wp.colStr(ii,"air_type")+"'"
            + " and   wf_type = 'GIFT_MILE' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commModReason(String s1) throws Exception 
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
            + " and   wf_id = '"+wp.colStr(ii,"return_reason")+"'"
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
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0) continue;
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
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0) continue;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chi_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commListPrice(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " list_price,cash_value "
            + " from mkt_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            ;
       if (wp.colStr(ii,"gift_no").length()==0) continue;
       sqlSelect(sql1);
       /*20210826 #1512慧婷  增加
                  贈品價格(cash_value)*兌換數量(exchg_cnt),大於等於list_price,就顯示(vlaue_cnt),其餘顯示0 */
       int cashValue = Integer.parseInt(sqlStr("cash_value"));
       int listPrice = Integer.parseInt(sqlStr("list_price"));
       int exchgCnt  = Integer.parseInt(wp.colStr(ii,"exchg_cnt"));
       int vlaueCnt  = cashValue * exchgCnt ;
       
       if (sqlRowNum>0)
    	   if(vlaueCnt >= listPrice) {
    		   columnData = columnData + vlaueCnt; 
    	   }else {
    		   columnData = "0"; 
    	   }
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
//************************************************************************
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
     if (wp.colStr(ii,"gift_no").length()==0) continue;
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
  String[] cde = {"N","A","P","B","C","D"};
  String[] txt = {"新兌>換","航空退件","航空請款","出貨","退貨","資料異動"};
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
  String[] txt = {"商品","基金","電子商品"};
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
  String[] txt = {"人工登錄","語音","網路"};
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
 public void wfAjaxFunc2(TarokoCommon wr) throws Exception
 {
  String ajaxjVendorNo = "";
  String ajaxjGiftNo = "";
  super.wp = wr;


  selectAjaxFunc20(
                    wp.itemStr2("ax_win_tran_date_s"),wp.itemStr2("ax_win_tran_date_e"));

  if (rc!=1)
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
 void selectAjaxFunc20(String s1,String s2) throws Exception
  {
   if ((s1.length()==0)||(s2.length()==0)) return;
   wp.sqlCmd = " select "
             + " '' as vendor_no, "
             + " max('--') as vendor_name "
             + " from SYSIBM.SYSDUMMY1 "
             + " union "
             + " select "
             + " b.vendor_no, "
             + " max(c.vendor_name)  as vendor_name "
             + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
             + " where a.tran_date between  '" + s1 +"' "
             + "                      and   '" + s2 +"' "
             + " and   a.gift_no     = b.gift_no "
             + " and   c.vendor_no   = b.vendor_no "
             + " and   b.gift_type   = '1' "
             + " and   a.air_type    = '' "
             + " and   a.return_date = '' "
             + " and   a.deduct_flag = 'Y' "
             + " group by b.vendor_no  "
             + " order by 1  "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      alertErr("查無廠商資料");

   return;
 }

// ************************************************************************
 public void wfAjaxFunc3(TarokoCommon wr) throws Exception
 {
  String ajaxjVendorNo = "";
  String ajaxjGiftNo = "";
  super.wp = wr;

  if (wp.itemStr2("ax_win_tran_date_s").length()==0) return;
  if (wp.itemStr2("ax_win_tran_date_e").length()==0) return;

  selectAjaxFunc30(
                    wp.itemStr2("ax_win_vendor_no"),
                    wp.itemStr2("ax_win_tran_date_s"),
                    wp.itemStr2("ax_win_tran_date_e"));

  if (rc!=1)
     {
      wp.addJSON("ajaxj_gift_no", "");
      wp.addJSON("ajaxj_gift_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_gift_no", sqlStr(ii, "gift_no"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_gift_name", sqlStr(ii, "gift_name"));
 }
// ************************************************************************
 void selectAjaxFunc30(String s1,String s2,String s3) throws Exception
  {
   if ((s1.length()==0)||(s2.length()==0)) return;
   wp.sqlCmd = " select "
             + " '' as gift_no, "
             + " max('--') as gift_name "
             + " from SYSIBM.SYSDUMMY1 "
             + " union "
             + " select "
             + " b.gift_no, "
             + " max(b.gift_name)  as gift_name "
             + " from mkt_gift_bpexchg a,mkt_gift b "
             + " where a.tran_date between  '" + s2 +"' "
             + "                   and      '" + s3 +"' "
             + " and   a.gift_no     = b.gift_no "
             + " and   b.vendor_no   = '" + s1 +"' "
             + " and   b.gift_type   = '1' "
             + " and   a.return_date = '' "
             + " and   a.deduct_flag = 'Y' "
             + " and   a.air_type    = '' "
             + " group by b.gift_no  "
             + " order by 1  "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      alertErr("查無贈品資料");

   return;
 }

// ************************************************************************
 public void procMethodPDFLIST() throws Exception
  {
//	  wp.reportId ="mktr0290";
//	  LIST_print(wp.item_ss("ex_tran_date_s"),
//	             wp.item_ss("ex_tran_date_e"),
//	             wp.item_ss("ex_vendor_no"),
//	             wp.item_ss("ex_gift_no"),
//	             wp.item_ss("ex_from_mark"));
//	  wp.pageRows =9999;
//	  TarokoPDF   pdf = new TarokoPDF();
//	  wp.fileMode = "Y";
//	  pdf.pageVert =true;
//	  pdf.excelTemplate = "mktr0290.xlsx";
//	  pdf.pageCount =1;
//	  pdf.sheetNo = 0;
//	  pdf.procesPDFreport(wp);
//	  pdf = null;			
//	  return;
	//寫入Log紀錄檔 20210303 add
	if(excelLog() != 1){
		wp.respHtml = "TarokoErrorPDF";
		return;
	}
	log("xlsFunction: started--------");
	wp.reportId ="mktr0290";
	LISTPrint(wp.itemStr2("ex_tran_date_s"),
			wp.itemStr2("ex_tran_date_e"),
            wp.itemStr2("ex_vendor_no"),
            wp.itemStr2("ex_gift_no"),
            wp.itemStr2("ex_from_mark"));
	wp.pageRows =99999;
	TarokoExcel xlsx = new TarokoExcel();
	wp.fileMode = "Y";
//   pdf.pageVert =true;
	xlsx.excelTemplate = "mktr0290.xlsx";
	xlsx.pageCount =1;
	xlsx.sheetName[0] ="明細";
	xlsx.processExcelSheet(wp);
	xlsx.outputExcel();
	xlsx = null;	
	log("xlsFunction: ended-------------");
	return;

  }
// ************************************************************************
 public void procMethodPDFBATCH() throws Exception
  {

//	 wp.reportId ="mktr0290";
//	  BATCH_print(wp.item_ss("ex_tran_date_s"),
//	             wp.item_ss("ex_tran_date_e"),
//	             wp.item_ss("ex_vendor_no"),
//	             wp.item_ss("ex_gift_no"),
//	             wp.item_ss("ex_from_mark"));
//	  wp.pageRows =9999;
//	  TarokoPDF   pdf = new TarokoPDF();
//	  wp.fileMode = "Y";
//	  pdf.pageVert =true;
//	  pdf.excelTemplate = "mktr0290_02.xlsx";
//	  pdf.pageCount =20;
//	  pdf.sheetNo = 0;
//	  pdf.procesPDFreport(wp);
//	  pdf = null;
//	  return;
//寫入Log紀錄檔 20210303 add
  if(excelLog1() != 1){
	wp.respHtml = "TarokoErrorPDF";
	return;
  }	 
  wp.reportId ="mktr0290";
  BATCHPrint(wp.itemStr2("ex_tran_date_s"),
             wp.itemStr2("ex_tran_date_e"),
             wp.itemStr2("ex_vendor_no"),
             wp.itemStr2("ex_gift_no"),
             wp.itemStr2("ex_from_mark"));
  TarokoExcel xlsx = new TarokoExcel();
  
  xlsx.pageBreak = "Y";
  xlsx.pageCount =20;
  wp.fileMode = "Y";
  xlsx.excelTemplate = "mktr0290_02.xlsx";
  
  xlsx.sheetName[0] ="明細";
  xlsx.processExcelSheet(wp);
  xlsx.outputExcel();
  xlsx = null;
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
  return;
 }
// ************************************************************************
 String procDynamicDddwVendorNo(String s1,String s2)  throws Exception
 {
   String lsSql = "";

 lsSql   = " select "
          + " b.vendor_no as db_code, "
          + " max(b.vendor_no||' '||c.vendor_name)  as db_desc "
          + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
          + " where a.tran_date between  '" + s1 +"' "
          + "                   and      '" + s2 +"' "
          + " and   a.gift_no     = b.gift_no "
          + " and   c.vendor_no  = b.vendor_no "
          + " and   b.gift_type   = '1' "
          + " and   a.air_type    = '' "
          + " and   a.return_date = '' "
          + " and   a.deduct_flag = 'Y' "
          + " group by b.vendor_no  "
          + " order by b.vendor_no  "
          ;

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwGiftNo(String s1,String s2,String s3)  throws Exception
 {
   String lsSql = "";

 lsSql   = " select "
          + " b.gift_no as db_code, "
          + " max(b.gift_no||' '||b.gift_name)  as db_desc "
          + " from mkt_gift_bpexchg a,mkt_gift b "
          + " where a.tran_date between  '" + s2 +"' "
          + "                   and      '" + s3 +"' "
          + " and   a.gift_no     = b.gift_no "
          + " and   b.vendor_no   = '" + s1 +"' "
          + " and   b.gift_type   = '1' "
          + " and   a.air_type    = '' "
          + " and   a.deduct_flag = 'Y' "
          + " and   a.return_date = '' "
          + " group by b.gift_no  "
          + " order by b.gift_no  "
          ;

   return lsSql;
 }
// ************************************************************************
 void LISTPrint(String s1,String s2,String s3,String s4,String s5) throws Exception
  {
   int llRows = 0 ;
   setSelectLimit(0);
   String sql1 = " select "
               + " bill_sending_zip , "
               + " bill_sending_addr1||"
               + " bill_sending_addr2||"
               + " bill_sending_addr3||"
               + " bill_sending_addr4  as  addr_14 , "
               + " bill_sending_addr5, "
               + " a.chi_name , "
               + " '先生/小姐' as idno_sex , "
               + " '' as remark_1 , "
               + " b.vendor_no||'-'||b.gift_no as remark_2 , "
               + " '' as remark_3 , "
               + " substr(c.id_no,1,7) as remark_4 "
               + " from mkt_gift_bpexchg a,mkt_gift b,crd_idno c "
               + " where a.tran_date between '" + s1 +"' "
               + "                   and     '" + s2 +"' "
               + " and a.deduct_flag =  'Y' " 
               + " and a.gift_type   =  '1' " 
               + " and   a.return_date = '' "
               + " and a.air_type    =  '' " 
               + " and a.gift_no     =  b.gift_no " 
               + " and a.id_p_seqno  =  c.id_p_seqno " 
               ;

   if (wp.itemStr2("ex_vendor_no").length()!=0)
      sql1 = sql1 + " and b.vendor_no = '" + s3 +"' ";

   if (wp.itemStr2("ex_gift_no").length()!=0)
      sql1 = sql1 + " and a.gift_no = '" + s4 +"' ";

   if (wp.itemStr2("ex_from_mark").length()!=0)
      sql1 = sql1 + " and a.from_mark = '" + s5 +"' ";

   sql1 = sql1 + " order by a.tran_seqno";

   int ss = -1 , liPage=0;

   sqlSelect(sql1);

   llRows = sqlRowNum;
   if (llRows%16!=0)
      {
       liPage= (llRows/16)+1;
      }
   else
      {
       liPage= (llRows/16);
      }
   int bb=0;

   for (int ll=0;ll<liPage;ll++)
     {
      for (int zz=1;zz<=16;zz++)
        {
         wp.colSet(ll,"ex_addr_zip_"+zz, sqlStr(bb,"bill_sending_zip"));
         wp.colSet(ll,"ex_addr_14_"+zz, sqlStr(bb,"addr_14"));
         wp.colSet(ll,"ex_addr_5_"+zz, sqlStr(bb,"bill_sending_addr5"));
         wp.colSet(ll,"ex_chi_name_"+zz, sqlStr(bb,"chi_name"));

         wp.colSet(ll,"ex_remark_2_"+zz, sqlStr(bb,"remark_2"));
         wp.colSet(ll,"ex_remark_4_"+zz, sqlStr(bb,"remark_4"));
         wp.colSet(ll,"ex_idno_sex_"+zz, "先生/小姐");

         bb++;
         if(bb==llRows) break;
        }
     }

   wp.listCount[0] = liPage;
  }                
// ************************************************************************
 void BATCHPrint(String s1,String s2,String s3,String s4,String s5) throws Exception
  {
   int llRows = 0 ;
   setSelectLimit(0);
   String sql1 = " select "
               + " bill_sending_zip , "
               + " bill_sending_addr1||"
               + " bill_sending_addr2||"
               + " bill_sending_addr3||"
               + " bill_sending_addr4||"
               + " bill_sending_addr5 as addr, "
               + " a.chi_name , "
               + " '先生/小姐' as idno_sex , "
               + " '' as remark_1 , "
               + " b.vendor_no||'-'||b.gift_no as remark_2 , "
               + " '' as ex_remark_1 , "
               + " '' as remark_3 , "
               + " substr(c.id_no,1,7) as remark_4 "
               + " from mkt_gift_bpexchg a,mkt_gift b,crd_idno c "
               + " where a.tran_date between '" + s1 +"' "
               + "                   and     '" + s2 +"' "
               + " and a.deduct_flag =  'Y' "
               + " and a.gift_type   =  '1' "
               + " and a.air_type    =  '' "
               + " and   a.return_date = '' "
               + " and a.gift_no     =  b.gift_no "
               + " and a.id_p_seqno  =  c.id_p_seqno "
               ;


   if (wp.itemStr2("ex_vendor_no").length()!=0)
      sql1 = sql1 + " and b.vendor_no = '" + s3 +"' ";

   if (wp.itemStr2("ex_gift_no").length()!=0)
      sql1 = sql1 + " and a.gift_no = '" + s4 +"' ";

   if (wp.itemStr2("ex_from_mark").length()!=0)
      sql1 = sql1 + " and a.from_mark = '" + s5 +"' ";

   sql1 = sql1 + " order by a.tran_seqno";

   int ss = -1 , liPage=0;

   sqlSelect(sql1);

   llRows = sqlRowNum;

   int bb=0;
   
   if (wp.itemStr2("ex_post_flag").equals("1"))
       wp.colSet("ex_remark_1", "普掛");
   else
       wp.colSet("ex_remark_1", "限掛");   

   for (int ll=0;ll<llRows;ll++)
     {
//      bb = ll + 1;    //Amber修正，印出結果會少一筆
      wp.colSet(ll,"ex_addr", sqlStr(bb,"addr"));
      wp.colSet(ll,"ex_chi_name", sqlStr(bb,"chi_name"));

      wp.colSet(ll,"ex_remark_2", sqlStr(bb,"remark_2"));
      bb++;
     }
   
   wp.listCount[0] = llRows;
  }

// ************************************************************************
    //名條PDF檔LOG
	int excelLog() throws Exception {

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("LOG_ONLINE_APPROVE");
		sp.ppstr("program_id", "mktr0290");
		sp.ppstr("file_name", "mktr0290名條地址.xlsx");
		sp.ppstr("crt_date", wp.sysDate+wp.sysTime);
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("apr_flag", "Y");
		sp.ppstr("apr_date", wp.sysDate);
		sp.ppstr("apr_user", wp.itemStr2("zz_apr_user"));		
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if(sqlRowNum <= 0){
			alertErr("Log紀錄檔寫入失敗!");
			return -1;
		} else {
			sqlCommit(1);
		}
		return 1;
	}
	//大宗掛號PDF檔LOG
	int excelLog1() throws Exception {
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("LOG_ONLINE_APPROVE");
		sp.ppstr("program_id", "mktr0290");
		sp.ppstr("file_name", "mktr0290大宗掛號.xlsx");
		sp.ppstr("crt_date", wp.sysDate+wp.sysTime);
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("apr_flag", "Y");
		sp.ppstr("apr_date", wp.sysDate);
		sp.ppstr("apr_user", wp.itemStr2("zz_apr_user"));		
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if(sqlRowNum <= 0){
			alertErr("Log紀錄檔寫入失敗!");
			return -1;
		} else {
			sqlCommit(1);
		}
		return 1;
	}
}  // End of class
