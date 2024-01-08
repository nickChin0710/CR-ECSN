/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/03/31  V1.00.07   Allen Ho      Initial                              *
* 111/11/29  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0243 extends BaseEdit
{
 private final String PROGNAME = "紅利商品庫存量維護作業處理程式111/11/29  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0243Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_gift_stock";
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
  else if (eqIgno(wp.buttonCode, "A"))
     {// 新增功能 -/
      strAction = "A";
      wp.itemSet("aud_type","A");
      insertFunc();
     }
  else if (eqIgno(wp.buttonCode, "U"))
     {/* 更新功能 */
      strAction = "U3";
      updateFunc_U3R();
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
      deleteFunc_D3R();
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
              + sqlStrend(wp.itemStr("ex_create_date_s"), wp.itemStr("ex_create_date_e"), "a.create_date")
              + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "")
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
               + "a.gift_no,"
               + "a.create_date,"
               + "a.adjust_flag,"
               + "a.adjust_count,"
               + "a.web_count,"
               + "A_SUPPLY_CNT-A_USE_CNT-A_WEB_CNT as a_net_cnt,"
               + "a.a_supply_cnt,"
               + "a.a_use_cnt,"
               + "a.a_web_cnt,"
               + "a.crt_user,"
               + "a.apr_user,"
               + "a.apr_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by gift_no,create_date desc,create_time desc"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commGiftName("comm_gift_no");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

  commAdjustFlag("comm_adjust_flag");

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
               + "a.gift_no as gift_no,"
               + "a.adjust_flag,"
               + "a.adjust_count,"
               + "a.web_count,"
               + "a.stock_desc,"
               + "a.stock_comment,"
               + "a.m_supply_cnt,"
               + "a.m_use_cnt,"
               + "a.m_web_cnt,"
               + "a.a_supply_cnt,"
               + "a.a_use_cnt,"
               + "a.a_web_cnt,"
               + "a_supply_cnt-a_use_cnt-a_web_cnt as a_net_cnt,"
               + "a.p_supply_cnt,"
               + "a.p_use_cnt,"
               + "a.p_web_cnt,"
               + "p_supply_cnt-p_use_cnt-p_web_cnt as p_net_cnt,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.gift_no")
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
  dataread_wkdata();
  commGiftName("comm_gift_no");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("gift_no");
  commFuncAudType("aud_type");
  dataRead_R3R();
 }
// ************************************************************************
 void dataread_wkdata() throws Exception
 {

//    wp.colSet("m_net_cnt ",  String.format("%d" ,(int)wp.colNum("m_supply_cnt"))); 
    wp.colSet("m_net_cnt ", String.format("%d" , 
                             (int)wp.colNum("m_supply_cnt")
                           - (int)wp.colNum("m_use_cnt")
                           - (int)wp.colNum("m_web_cnt")));


 }
// ************************************************************************
 public void dataRead_R3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.gift_no as gift_no,"
               + "a.adjust_flag as adjust_flag,"
               + "a.adjust_count as adjust_count,"
               + "a.web_count as web_count,"
               + "a.stock_desc as stock_desc,"
               + "a.stock_comment as stock_comment,"
               + "a.m_supply_cnt as m_supply_cnt,"
               + "a.m_use_cnt as m_use_cnt,"
               + "a.m_web_cnt as m_web_cnt,"
               + "a.a_supply_cnt as a_supply_cnt,"
               + "a.a_use_cnt as a_use_cnt,"
               + "a.a_web_cnt as a_web_cnt,"
               + "a_supply_cnt-a_use_cnt-a_web_cnt as a_net_cnt,"
               + "a.p_supply_cnt as p_supply_cnt,"
               + "a.p_use_cnt as p_use_cnt,"
               + "a.p_web_cnt as p_web_cnt,"
               + "p_supply_cnt-p_use_cnt-p_web_cnt as p_net_cnt,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.gift_no")
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
  commGiftName("comm_gift_no");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commFuncAudType("aud_type");
  dataread_wkdata();
 }
// ************************************************************************
 public void deleteFunc_D3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr("gift_no");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      km1 = wp.itemStr("gift_no");
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
 public void updateFunc_U3R()  throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr("gift_no");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1)
         {
          dataRead_R3R();;
          dataread_wkdata();
         }
     }
  else
     {
      km1 = wp.itemStr("gift_no");
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
  Mktm0243Func func =new Mktm0243Func(wp);

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
       if ((wp.respHtml.equals("mktm0243_nadd"))||
           (wp.respHtml.equals("mktm0243_detl")))
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
                 ," where disable_flag='N' and air_type=''");
         }
       if ((wp.respHtml.equals("mktm0243")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_gift_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_gift_no");
             }
          this.dddwList("dddw_gift_no"
                 ,"mkt_gift"
                 ,"trim(gift_no)"
                 ,"trim(gift_name)"
                 ," where disable_flag!='Y'");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public String sqlChkEx(String exCol, String sqCond, String fileExt) throws Exception
 {
  return "";
 }
// ************************************************************************
  void commFuncAudType(String s1)
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
 public void commGiftName(String s1) throws Exception 
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
 public void commAdjustFlag(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"主檔新增","新增","重設"};
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
 public void wfAjaxFunc1(TarokoCommon wr) throws Exception
 {
  super.wp = wr;


  if (selectAjaxFunc10(
                    wp.itemStr("ax_win_gift_no"))!=0) 
     {
      wp.addJSON("m_supply_cnt","");
      wp.addJSON("m_use_cnt","");
      wp.addJSON("m_web_cnt","");
      wp.addJSON("m_net_cnt","");
      return;
     }

  wp.addJSON("m_supply_cnt",sqlStr("m_supply_cnt"));
  wp.addJSON("m_use_cnt",sqlStr("m_use_cnt"));
  wp.addJSON("m_web_cnt",sqlStr("m_web_cnt"));
  wp.addJSON("m_net_cnt",sqlStr("m_net_cnt"));
 }
// ************************************************************************
 int selectAjaxFunc10(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " decode(gift_type,'3',max_limit_count,supply_count) as m_supply_cnt ,"
             + " decode(gift_type,'3',use_limit_count,use_count) as m_use_cnt ,"
             + " web_sumcnt as m_web_cnt "
             + " from  mkt_gift  "
             + " where gift_no ='"+s1+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("商品代碼["+s1+"]查無資料");
       return 1;
      }

   this.sqlSet(0,"m_supply_cnt",String.format("%,0d",(int)sqlNum("m_supply_cnt")));
   this.sqlSet(0,"m_use_cnt",String.format("%.0f",sqlNum("m_use_cnt")));
   this.sqlSet(0,"m_web_cnt",String.format("%.0f",sqlNum("m_web_cnt")));
   this.sqlSet(0,"m_net_cnt",String.format("%.0f",sqlNum("m_supply_cnt")
                                                   - sqlNum("m_use_cnt")
                                                   - sqlNum("m_web_cnt")));

   return 0;
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
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
// ************************************************************************

}  // End of class
