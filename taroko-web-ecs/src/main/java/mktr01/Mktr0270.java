/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/04  V1.00.03   Allen Ho      Initial                              *
* 111-12-06  V1.00.04  Machao    sync from mega & updated for project coding standard                                                                         *
* 111-12-14  V1.00.05  Zuwei Su       修改AJAX調用方式                     *
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
public class Mktr0270 extends BaseAction implements InfaceExcel
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
      else if (eqIgno(wp.buttonCode, "AJAX"))
      {/* nothing to do */
       strAction = "";
       switch (wp.itemStr("methodName")) {
        case "wf_ajax_func_2":
            wfAjaxFunc2(wp);
            break;
        case "wf_ajax_func_4":
            wfAjaxFunc4(wp);
            break;

        default:
            break;
    }
      }

  funcSelect();
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
              + sqlChkEx(wp.itemStr2("ex_vendor_no"), "1", "")
              + sqlCol(wp.itemStr2("ex_gift_no"), "a.gift_no")
              + " and a.gift_type='1'     "
              + " and a.return_date=''     "
              + " and total_pt>0     "
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
               + "sum(EXCHG_CNT) as exchg_cnt,"
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
                       + (int)wp.colNum(ii,"exchg_cnt")
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
       if ((wp.respHtml.equals("mktr0270")))
         {
          wp.initOption ="--";
         wp.optionKey = itemkk("ex_vendor_no");
          if (wp.colStr("ex_vendor_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_vendor_no");
             }
          lsSql = "";
          if ((wp.itemStr2("ex_tran_date_s").length()!=0)&&
              (wp.itemStr2("ex_tran_date_s").length()!=0))
             {
              lsSql =  procDynamicDddwVendorNo(wp.itemStr2("ex_tran_date_s"),wp.itemStr2("ex_tran_date_e"));

              wp.optionKey = wp.itemStr2("ex_vendor_no");
              dddwList("dddw_vendor_no", lsSql);
              wp.colSet("ex_vendor_no", "");
             }
          wp.initOption ="--";
         wp.optionKey = itemkk("ex_gift_no");
          if (wp.colStr("ex_gift_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_gift_no");
             }
          lsSql = "";
          if ((wp.itemStr2("ex_tran_date_s").length()!=0)&&
              (wp.itemStr2("ex_tran_date_e").length()!=0)&&
              (wp.itemStr2("ex_vendor_no").length()!=0))
             {
              lsSql =  procDynamicDddwGiftNo(wp.itemStr2("ex_tran_date_s")
                                                 ,wp.itemStr2("ex_tran_date_e")
                                                 ,wp.itemStr2("ex_vendor_no"));

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
      alertErr("兌換日期不可空白");
      return(1);
     }

  if (wp.itemStr2("ex_vendor_no").length()==0)
     {
      alertErr("供應商代號不可空白");
      return(1);
     }

  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
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
  String ajaxjVendorNo = "";
  String ajaxjGiftNo = "";
  super.wp = wr;


  if (selectAjaxFunc20(
                    wp.itemStr2("ax_win_tran_date_s"),wp.itemStr2("ax_win_tran_date_e"))!=0) 
     {
      wp.addJSON("ajaxj_vendor_no", "");
      wp.addJSON("ajaxj_vendor_name", "");
      return;
     }

  wp.addJSON("ajaxj_vendor_no", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_vendor_no", sqlStr(ii, "vendor_no"));
  wp.addJSON("ajaxj_vendor_name", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_vendor_name", sqlStr(ii, "vendor_name"));
 }
// ************************************************************************
 int selectAjaxFunc20(String s1,String s2) throws Exception

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
             + " and   b.gift_type   = '1' "
             + " and   a.air_type    = '' "
             + " and   a.deduct_flag = 'Y' "
             + " group by b.vendor_no  "
             + " order by b.vendor_no  "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("查無贈品資料");
       return(1);
      }

   return(0);
 }

// ************************************************************************
 public void wfAjaxFunc4(TarokoCommon wr) throws Exception
 {
  String ajaxjVendorNo = "";
  String ajaxjGiftNo = "";
  super.wp = wr;

  if (wp.itemStr2("ax_win_tran_date_s").length()==0) return;
  if (wp.itemStr2("ax_win_tran_date_e").length()==0) return;

  if (selectAjaxFunc40(
                    wp.itemStr2("ax_win_vendor_no"),
                    wp.itemStr2("ax_win_tran_date_s"),
                    wp.itemStr2("ax_win_tran_date_e"))!=0) 
     {
      wp.addJSON("ajaxj_gift_no", "");
      wp.addJSON("ajaxj_gift_name", "");
      return;
     }

  wp.addJSON("ajaxj_gift_no", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_gift_no", sqlStr(ii, "gift_no"));
  wp.addJSON("ajaxj_gift_name", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_gift_name", sqlStr(ii, "gift_name"));
 }
// ************************************************************************
 int selectAjaxFunc40(String s1,String s2,String s3) throws Exception

  {
   if ((s1.length()==0)||(s2.length()==0)) return(0);
   wp.sqlCmd = " select "
             + " a.gift_no, "
             + " max(b.gift_name)  as gift_name "
             + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
             + " where a.tran_date between  '" + s2 +"' "
             + "                      and   '" + s3 +"' "
             + " and   a.gift_no     = b.gift_no "
             + " and   c.vendor_no   = b.vendor_no "
             + " and   c.vendor_no   =  '" + s1 +"' "
             + " and   b.gift_type   = '1' "
             + " and   a.air_type    = '' "
             + " and   a.deduct_flag = 'Y' "
             + " group by a.gift_no  "
             + " order by a.gift_no  "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("查無贈品資料");
       return(1);
      }

   return(0);
 }

// ************************************************************************
 @Override
 public void xlsPrint() throws Exception
  {
   try {
        wp.reportId ="mktr0270";
        String ss = "";
  if ((wp.itemStr2("ex_tran_date_s").length()==0)||
      (wp.itemStr2("ex_tran_date_e").length()==0))
     {
      alertErr("兌換日期不可空白");
      return;
     }

  if (wp.itemStr2("ex_vendor_no").length()==0)
     {
      alertErr("供應商代號不可空白");
      return;
     }


         wp.sqlCmd = " select "
                   + " a.gift_no, "
                   + " max(b.gift_name) as gift_name, "
                   + " max(b.vendor_no||'-'||c.vendor_name) as vendor_no, "
                   + " sum(a.exchg_cnt) as exchg_cnt "
                   + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
                   + " where b.vendor_no = c.vendor_no "
                   + " and   a.gift_no   = b.gift_no "
                   + " and   b.vendor_no = '" + wp.itemStr2("ex_vendor_no") + "' "
                   + " and   a.tran_date between '" + wp.itemStr2("ex_tran_date_s") + "' "
                   + "                   and     '" + wp.itemStr2("ex_tran_date_e") + "' "
                   ;
        if (wp.itemStr2("ex_gift_no").length()>0)
            wp.sqlCmd = wp.sqlCmd  
                      + " and   a.gift_no = '" + wp.itemStr2("ex_gift_no") + "' "
                      ;
        wp.sqlCmd = wp.sqlCmd  
                  + " group by a.gift_no ";

        this.sqlSelect();

        ss = ss + "廠商代號：" + sqlStr("vendor_no");
        ss = ss + "　　兌換期間：" + toDateFormat(wp.itemStr2("ex_tran_date_s"))
                + "-" + toDateFormat(wp.itemStr2("ex_tran_date_e"));;

        wp.colSet("cond1", ss);
        TarokoExcel xlsx = new TarokoExcel();
        wp.fileMode = "Y";
        xlsx.excelTemplate = "mktr0270.xlsx";
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
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
// ************************************************************************
 String procDynamicDddwVendorNo(String s1,String s2)  throws Exception
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
          + " and   b.gift_type   = '1' "
          + " and   a.air_type    = '' "
          + " and   a.deduct_flag = 'Y' "
          + " group by b.vendor_no  "
          + " order by b.vendor_no  "
          ;

   return lsSql;
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
           columnData = columnData + sqlStr("column_gift_name");
           wp.colSet(ii, "comm_gift_name", columnData);
          }
      }
   return;
 }
// ************************************************************************
 String procDynamicDddwGiftNo(String s1,String s2,String s3)  throws Exception
 {
  String lsSql = "";
 
  lsSql = " select "
         + " a.gift_no as db_code, "
         + " max(b.gift_name) as db_desc "
         + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
         + " where a.tran_date between  '" + s1 +"' "
         + "                      and   '" + s2 +"' "
         + " and   a.gift_no     = b.gift_no "
         + " and   c.vendor_no   = b.vendor_no "
         + " and   c.vendor_no   =  '" + s3 +"' "
         + " and   b.gift_type   = '1' "
         + " and   a.air_type    = '' "
         + " and   a.deduct_flag = 'Y' "
         + " group by a.gift_no  "
         + " order by a.gift_no  "
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
