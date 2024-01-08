/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/04  V1.00.01   Allen Ho      Initial                              *
* 111/11/30  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0260 extends BaseEdit
{
 private final String PROGNAME = "紅利贈品廠商維護作業處理程式111/11/30  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0260Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_vendor";
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
     }

  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr("ex_vendor_no"), "a.vendor_no", "like%")
              + sqlChkEx(wp.itemStr("ex_vendor_name"), "1", "")
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
               + "a.vendor_no,"
               + "a.vendor_name,"
               + "a.name,"
               + "a.id_no,"
               + "a.tel_no,"
               + "a.contact_id,"
               + "a.apr_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by vendor_no"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }



  //list_wkdata();
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
               + "a.vendor_no as vendor_no,"
               + "a.apr_flag,"
               + "a.vendor_name,"
               + "a.disable_flag,"
               + "a.id_no,"
               + "a.name,"
               + "a.sub_cname,"
               + "a.out_days,"
               + "a.tel_no,"
               + "a.contact_id,"
               + "a.contact_tel,"
               + "a.area_code,"
               + "a.address1,"
               + "a.address2,"
               + "a.address3,"
               + "a.address4,"
               + "a.address5,"
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
                   + sqlCol(km1, "a.vendor_no")
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
  commAprFlag2("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("vendor_no");
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.vendor_no as vendor_no,"
               + "a.apr_flag as apr_flag,"
               + "a.vendor_name as vendor_name,"
               + "a.disable_flag as disable_flag,"
               + "a.id_no as id_no,"
               + "a.name as name,"
               + "a.sub_cname as sub_cname,"
               + "a.out_days as out_days,"
               + "a.tel_no as tel_no,"
               + "a.contact_id as contact_id,"
               + "a.contact_tel as contact_tel,"
               + "a.area_code as area_code,"
               + "a.address1 as address1,"
               + "a.address2 as address2,"
               + "a.address3 as address3,"
               + "a.address4 as address4,"
               + "a.address5 as address5,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.vendor_no")
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
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commfuncAudType("aud_type");
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr("vendor_no");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      km1 = wp.itemStr("vendor_no");
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
  qFrom=0; 
   km1 = wp.itemStr("vendor_no");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr("vendor_no");
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
  mktm01.Mktm0260Func func =new mktm01.Mktm0260Func(wp);

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
       if ((wp.respHtml.equals("mktm0260_nadd"))||
           (wp.respHtml.equals("mktm0260_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("area_code").length()>0)
             {
             wp.optionKey = wp.colStr("area_code");
             }
          this.dddwList("dddw_zipcode"
                 ,"select zip_code as db_code , zip_code||'-'||zip_city||zip_town as db_desc from ptr_zipcode order by zip_code"
                        );
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public String sqlChkEx(String exCol, String sqCond, String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
      return " and vendor_name like '%"+wp.itemStr("ex_vendor_name")+"%' ";

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
 public void wfAjaxFunc2(TarokoCommon wr) throws Exception
 {
  super.wp = wr;


  if (selectAjaxFunc20(
                    wp.itemStr("ax_win_area_code"))!=0) 
     {
      wp.addJSON("address1","");
      wp.addJSON("address2","");
      return;
     }

  wp.addJSON("address1",sqlStr("address1"));
  wp.addJSON("address2",sqlStr("address2"));
 }
// ************************************************************************
 int selectAjaxFunc20(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " b.zip_city as address1 ,"
             + " b.zip_town as address2 "
             + " from  ptr_zipcode b "
             + " where b.zip_code ='"+s1+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      { 
       if (s1.length()==0)
          {
           alertErr2("聯絡人地址未輸入資料");
          }
       else
          {
           alertErr2("聯絡人地址["+s1+"]查無資料");
          }
       return 1; 
      } 

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

}  // End of class
