/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/07  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm02;

import mktm02.Mktm3120Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm3120 extends BaseEdit
{
 private final String PROGNAME = "IBON商品資料維護作業處理程式111-11-30  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm3120Func func = null;
  String kk1,kk2,kk3,kk4;
  String km1,km2,km3,km4;
  String fstAprFlag = "";
  String orgTabName = "ibn_prog_gift";
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
      kk1 = item_kk("data_k1");
      kk2 = item_kk("data_k2");
      kk3 = item_kk("data_k3");
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
              + " and a.prog_code = b.prog_code "
              + sqlCol(wp.itemStr2("ex_prog_code"), "a.prog_code", "like%")
              + sqlCol(wp.itemStr2("ex_prog_flag"), "b.prog_flag", "like%")
              + sqlCol(wp.itemStr2("ex_gift_no"), "a.gift_no", "like%")
              + sqlStrend(wp.itemStr2("ex_prog_s_date_s"), wp.itemStr2("ex_prog_s_date_e"), "a.prog_s_date")
              + sqlChkEx(wp.itemStr2("ex_apr_flag"), "2", "")
              + sqlStrend(wp.itemStr2("ex_gift_s_date_s"), wp.itemStr2("ex_gift_s_date_e"), "a.gift_s_date")
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
  if (wp.itemStr2("ex_apr_flag").equals("N"))
     controlTabName = orgTabName +"_t";

  wp.pageControl();

  wp.selectSQL = " "
               + "hex(a.rowid) as rowid, "
               + "nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.prog_code,"
               + "a.prog_s_date,"
               + "a.prog_e_date,"
               + "a.gift_no,"
               + "a.gift_s_date,"
               + "a.gift_e_date,"
               + "a.gift_typeno,"
               + "b.prog_flag";

  wp.daoTable = controlTabName + " a "
              + "LEFT OUTER JOIN ibn_prog b "
              + "ON a.prog_code = b.prog_code "
              ;
  wp.whereOrder = " "
                + " order by prog_code,prog_s_date desc,gift_no"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commProgCode("comm_prog_code");
  commGiftNo("comm_gift_no");


  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 @Override
  public void querySelect() throws Exception
 {
  fstAprFlag= wp.itemStr2("ex_apr_flag");
  if (wp.itemStr2("ex_apr_flag").equals("N"))
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
               + "a.prog_code,"
               + "a.apr_flag,"
               + "a.prog_s_date as prog_s_date,"
               + "a.prog_e_date as prog_e_date,"
               + "a.gift_no as gift_no,"
               + "'' as prog_desc,"
               + "a.gift_s_date,"
               + "a.gift_e_date,"
               + "a.gift_typeno,"
               + "a.gift_name,"
               + "a.prd_price,"
               + "a.exchange_pnt,"
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
                   + sqlCol(km1, "a.prog_code1")
                   + sqlCol(km2, "a.prog_s_date")
                   + sqlCol(km3, "a.prog_e_date")
                   + sqlCol(km4, "a.gift_no")
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
       wp.colSet("aud_type",wp.itemStr2("ex_apr_flag"));
       wp.colSet("fst_apr_flag",wp.itemStr2("ex_apr_flag"));
      }
  datareadWkdata();
  commAprFlag2("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("prog_code1");
  km2 = wp.colStr("prog_s_date");
  km3 = wp.colStr("prog_e_date");
  km4 = wp.colStr("gift_no");
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
 String sql1 = "";

 sql1 = "select "
      + " b.prog_desc "
      + " from  ibn_prog b  "
      + " where b.prog_code   ='"+ wp.colStr("prog_code")   + "' "
      + " and   b.prog_s_date ='"+ wp.colStr("prog_s_date") + "' "
      ;

 sqlSelect(sql1);

 if (sqlRowNum>0)
    wp.colSet("prog_desc"   , sqlStr("prog_desc"));

 return;
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.prog_code as prog_code,"
               + "a.apr_flag as apr_flag,"
               + "a.prog_s_date as prog_s_date,"
               + "a.prog_e_date as prog_e_date,"
               + "a.gift_no as gift_no,"
               + "'' as prog_desc,"
               + "a.gift_s_date as gift_s_date,"
               + "a.gift_e_date as gift_e_date,"
               + "a.gift_typeno as gift_typeno,"
               + "a.gift_name as gift_name,"
               + "a.prd_price as prd_price,"
               + "a.exchange_pnt as exchange_pnt,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.prog_code1")
              + sqlCol(km2, "a.prog_s_date")
              + sqlCol(km3, "a.prog_e_date")
              + sqlCol(km4, "a.gift_no")
              ;

  pageSelect();
  if (sqlNotFind())
     {
      wp.notFound ="";
      return;
     }
  wp.colSet("control_tab_name",controlTabName); 

  if (wp.respHtml.indexOf("_detl") > 0) 
     wp.colSet("btnStore_disable","");   
  commAprFlag2("comm_apr_flag");
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
   km1 = wp.itemStr2("prog_code1");
   km2 = wp.itemStr2("prog_s_date");
   km3 = wp.itemStr2("prog_e_date");
   km4 = wp.itemStr2("gift_no");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      km1 = wp.itemStr2("prog_code1");
      km2 = wp.itemStr2("prog_s_date");
      km3 = wp.itemStr2("prog_e_date");
      km4 = wp.itemStr2("gift_no");
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
  wp.colSet("fst_apr_flag",fstAprFlag);
 }
// ************************************************************************
 public void updateFuncU3R()  throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("prog_code1");
   km2 = wp.itemStr2("prog_s_date");
   km3 = wp.itemStr2("prog_e_date");
   km4 = wp.itemStr2("gift_no");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
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
      km1 = wp.itemStr2("prog_code1");
      km2 = wp.itemStr2("prog_s_date");
      km3 = wp.itemStr2("prog_e_date");
      km4 = wp.itemStr2("gift_no");
      strAction = "A";
      wp.itemSet("aud_type","U");
      insertFunc();
      if (rc==1) dataRead();
     }
  wp.colSet("fst_apr_flag",fstAprFlag);
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktm02.Mktm3120Func func =new mktm02.Mktm3120Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr(func.getMsg());
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
       if ((wp.respHtml.equals("mktm3120_nadd"))||
           (wp.respHtml.equals("mktm3120_detl")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("prog_code1").length()>0)
             {
             wp.optionKey = wp.colStr("prog_code1");
             }
          this.dddwList("dddw_prog_code"
                 ,"select prog_code||'-'||prog_s_date as db_code, prog_code||'('||substr(prog_desc,1,16)||')-'||prog_s_date as db_desc  from ibn_prog where to_char(sysdate,'yyyymmdd') <= prog_e_date  order by prog_code,prog_s_date"
                        );
         }
       if ((wp.respHtml.equals("mktm3120")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_prog_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_prog_code");
             }
          this.dddwList("dddw_prog_code1"
                 ,"ibn_prog"
                 ,"trim(prog_code)"
                 ,"trim(prog_desc)"
                 ," where 1 = 1 ");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
     {
//      if (wp.item_ss("ex_prog_flag").equals("0")) return "";
      return " and b.prog_flag = '"+ wp.itemStr2("ex_prog_flag")+"'";
     }
  if (sqCond.equals("2"))
     {
      if (wp.itemStr2("ex_apr_flag").equals("0")) return "";
      return " and a.apr_flag = '"+ wp.itemStr2("ex_apr_flag")+"'";
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
 public void commProgCode(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " prog_desc as column_prog_desc "
            + " from ibn_prog "
            + " where 1 = 1 "
            + " and   prog_code = '"+wp.colStr(ii,"prog_code")+"'"
            + " and   prog_s_date = '"+wp.colStr(ii,"prog_s_date")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_prog_desc"); 
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
            + " from ibn_prog_gift "
            + " where 1 = 1 "
            + " and   prog_code = '"+wp.colStr(ii,"prog_code")+"'"
            + " and   prog_s_date = '"+wp.colStr(ii,"prog_s_date")+"'"
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            ;
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
 public void wfAjaxFunc2(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr2("ax_win_prog_code1").length()==0) return;

  if (selectAjaxFunc20(
                    wp.itemStr2("ax_win_prog_code1"),
                    wp.itemStr2("ax_win_prog_code"),
                    wp.itemStr2("ax_win_prog_s_date"))!=0) 
     {
      wp.addJSON("prog_code","");
      wp.addJSON("prog_s_date","");
      wp.addJSON("prog_e_date","");
      wp.addJSON("gift_s_date","");
      wp.addJSON("gift_e_date","");
      return;
     }

  wp.addJSON("prog_code",sqlStr("prog_code"));
  wp.addJSON("prog_s_date",sqlStr("prog_s_date"));
  wp.addJSON("prog_e_date",sqlStr("prog_e_date"));
  wp.addJSON("gift_s_date",sqlStr("gift_s_date"));
  wp.addJSON("gift_e_date",sqlStr("gift_e_date"));
 }
// ************************************************************************
 int selectAjaxFunc20(String s1,String s2,String s3) throws Exception
 {
   wp.sqlCmd = " select "
             + " b.prog_code as prog_code ,"
             + " b.prog_desc ,"
             + " b.prog_s_date as prog_s_date ,"
             + " b.prog_e_date as prog_e_date ,"
             + " prog_s_date as gift_s_date ,"
             + " prog_e_date as gift_e_date "
             + " from  ibn_prog b  "
             + " where b.prog_code ='"+comm.getStr(s1,1 ,"-")+"' "
             + " and   b.prog_s_date ='"+comm.getStr(s1,2 ,"-")+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("活動代碼選擇:["+s1+"]查無資料");
       return 1;
      }

   sqlSet(0 , "prog_s_date", toDateFormat(sqlStr("prog_s_date")));
   sqlSet(0 , "prog_e_date", toDateFormat(sqlStr("prog_e_date")));
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
  public String toDateFormat(String date)
  {
   return date.substring(0,4) + "/"+date.substring(4,6)+"/"+date.substring(6,8);
  }

// ************************************************************************

}  // End of class
