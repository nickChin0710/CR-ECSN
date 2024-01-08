/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/20  V1.00.02   Allen Ho      Initial                              *
* 111-12-06  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktr01;

import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktr0280 extends BaseAction implements InfaceExcel
{
 private final String PROGNAME = "紅利兌換商品電子禮券採購報表處理程式111-12-06  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  String kk1;
  String orgTabName = "mkt_gift_bpexchg";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String   batchNo     = "";
  int errorCnt=0,recCnt=0,notifyCnt=0,colNum=0;
  int[]  datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String   upGroupType= "0";
private String strAction;

// ************************************************************************
 @Override
  public void userAction() throws Exception
 {
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
      checkButtonOff();
     }
  else if (eqIgno(wp.buttonCode, "R"))
     {//-資料讀取-
      strAction = "R";
      dataRead();
     }
  else if (eqIgno(wp.buttonCode, "M"))
     {/* 瀏覽功能 :skip-page*/
      queryRead();
     }
  else if (eqIgno(wp.buttonCode, "S"))
     {/* 動態查詢 */
      querySelect();
     }
  else if (eqIgno(wp.buttonCode, "XLS"))
     {/* Excel-   */
      strAction = "XLS";
      xlsPrint();
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
  if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr2("ex_ecoupon_bno"), "a.ecoupon_bno")
              + " and a.gift_type='3'     "
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
               + "a.gift_no,"
               + "max(ecoupon_DATE_S) as ecoupon_date_s,"
               + "max(ecoupon_DATE_E) as ecoupon_date_e,"
               + "sum(EXCHG_CNT) as ecoupon_cnt,"
               + "b.vendor_no";

  wp.daoTable = controlTabName + " a "
              + "JOIN mkt_gift b "
              + "ON a.gift_no = b.gift_no "
              ;
  wp.whereOrder = " group by b.vendor_no,a.gift_no"
                + " order by a.gift_no"
                ;

  wp.pageCountSql = "select count(*) from ( "
                   + " select distinct b.vendor_no,a.gift_no"
                   + " from "+ wp.daoTable +" "+wp.queryWhere
                   + " )";

  pageQuery();
  listWkdata();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commGiftNo1("comm_gift_no");


  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 void listWkdata()  throws Exception
 {

   int totalGiftCnt=wp.selectCnt,totalExchgCnt=0;
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       totalExchgCnt = totalExchgCnt
                       + (int)wp.colNum(ii,"ecoupon_cnt")
                       ;
      }
   
  wp.colSet("ex_total_msg1", "累計商品品項:　" + String.format("%,d", totalGiftCnt) +"　項,　商品件數　"
                                          + String.format("%,d", totalExchgCnt) +"　件"
                                          );


  commGiftName();
 }
// ************************************************************************
 @Override
  public void querySelect() throws Exception
 {

  kk1 = itemkk("data_k1");
  qFrom=1;
  dataRead();
 }
// ************************************************************************
 @Override
 public void dataRead() throws Exception
 {
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
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
       if ((wp.respHtml.equals("mktr0280")))
         {
          wp.initOption ="--";
         wp.optionKey = itemkk("ex_ecoupon_bno");
          if (wp.colStr("ex_ecoupon_bno").length()>0)
             {
             wp.optionKey = wp.colStr("ex_ecoupon_bno");
             }
          lsSql = "";
          lsSql =  procDynamicDddwEcouponBno();

          wp.optionKey = wp.itemStr2("ex_ecoupon_bno");
          dddwList("dddw_ecoupon_bno", lsSql);
          wp.colSet("ex_ecoupon_bno", "");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
  if (wp.itemStr2("ex_ecoupon_bno").length()==0)
     {
      alertErr("電子商品批號不可空白");
      return(1);
     }


  return(0);
 }
// ************************************************************************
 public void commGiftNo1(String s1) throws Exception 
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
 public void wfAjaxFunc2(TarokoCommon wr) throws Exception
 {
  super.wp = wr;


  if (selectAjaxFunc20(
                    wp.itemStr2("ax_win_ecoupon_bno"))!=0) 
     {
      return;
     }

 }
// ************************************************************************
 int selectAjaxFunc20(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " vendor_no, "
             + " tran_date_s, "
             + " tran_date_e, "
             + " apr_date, "
             + " ecoupon_cnt "
             + " from mkt_gift_batchno   "
             + " where ecoupon_bno  = '" + s1 + "' "
             + " and   ecoupon_date = '' "
             + " and   gift_group   = '3' "
             + " and   stop_date    = '' "
             ;

   
   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("電子商品批號["+s1+"]查無資料");
       return(1);
      }
             
   wp.sqlCmd = " select "
             + " vendor_no||'-'||vendor_name as vendor_name "
             + " from mkt_vendor "
             + " where vendor_no  = '" + sqlStr("vendor_no") + "' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("供應商代號["+sqlStr("vendor_no")+"]查無資料");
       return(1);
      }

   wp.sqlCmd = " select "
             + " sum(ecoupon_cnt) as vendor_cnt "
             + " from mkt_gift_batchmap "
             + " where ecoupon_bno  = '" + s1 + "' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("批號["+s1+"]廠商查無資料");
       return(1);
      }


   return(0);
 }
// ************************************************************************
 @Override
 public void xlsPrint() throws Exception
  {
   try {
        wp.reportId ="mktr0280";
        String ss = "";
         wp.sqlCmd = " select "
                   + " a.ecoupon_bno, "
                   + " b.vendor_no||'-'||b.vendor_name as vendor_no, "
                   + " a.tran_date_s, "
                   + " a.tran_date_e "
                   + " from mkt_gift_batchno a,mkt_vendor b "
                   + " where a.vendor_no  = b.vendor_no "
                   + " and   a.ecoupon_bno = '" + wp.itemStr2("ex_ecoupon_bno") + "' "
                   ;


        this.sqlSelect();

        ss = ss + "廠商代號：" + sqlStr("vendor_no");
        ss = ss + "　　兌換期間：" + toDateFormat(sqlStr("tran_date_s"))+"-"+toDateFormat(sqlStr("tran_date_e"));;

        wp.colSet("cond1", ss);
        TarokoExcel xlsx = new TarokoExcel();
        wp.fileMode = "Y";
        xlsx.excelTemplate = "mktr0280.xlsx";
        wp.pageRows =9999;
        queryFunc();
        wp.setListCount(1);
        queryFunc();
        wp.listCount[1] =sqlRowNum;
        xlsx.processExcelSheet(wp);
        xlsx.outputExcel();
        xlsx = null;
       } catch (Exception ex)
         {
          wp.expMethod = "xlsPrint";
          wp.expHandle(ex);
         }
  }
// ************************************************************************
 @Override
 public void procFunc() throws Exception
  {
   // TODO Auto-generated method stub
  }
 @Override
 public void logOnlineApprove() throws Exception
  {
   // TODO Auto-generated method stub
  }
// ************************************************************************
 public void checkButtonOff() throws Exception
  {
      wp.colSet("img_display","style=\"cursor:hand;\" onClick=\"top.submitControl('XLS');\"");
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
  return;
 }
// ************************************************************************
 public void commGiftName() throws Exception
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name, "
            + " effect_months "
            + " from mkt_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            + " and   gift_type = '3' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          {
           if (sqlNum("effect_months")==0) sqlSet(0,"effect_months","3");
           columnData = columnData + sqlStr("column_gift_name");
           wp.colSet(ii, "comm_gift_name", columnData);
           columnData=comm.nextMonthDate(wp.colStr(ii,"ecoupon_date_s"),
                                         (int)sqlNum("effect_months"));
           wp.colSet(ii,"ecoupon_date_e", comm.lastdateOfmonth(columnData));
          }
      }
   return;
 }
// ************************************************************************
 String procDynamicDddwEcouponBno()  throws Exception
 {
   String lsSql = "";

 lsSql = " select "
          + " a.ecoupon_bno as db_code, "
          + " b.vendor_no||'-'||b.vendor_name||'('||tran_date_s||'~'||tran_date_e||')-'||ecoupon_cnt||'筆'  as db_desc "
          + " from mkt_gift_batchno a,mkt_vendor b "
          + " where a.vendor_no  = b.vendor_no "
          + " and   a.gift_group   = '3' "
          + " and   a.stop_date = '' "
          + " and   a.ecoupon_date = '' "
          + " order by a.vendor_no  "
          ;

   return lsSql;
 }
// ************************************************************************
  public String toDateFormat(String date)
  {
   return date.substring(0,4) + "/"+date.substring(4,6)+"/"+date.substring(6,8);
  }



// ************************************************************************

}  // End of class
