/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/02  V1.00.02   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                            *
***************************************************************************/
package mktm02;

import mktm02.Mktm4075Func;
import ofcapp.AppMsg;
import java.util.Arrays;

import ecsfunc.EcsCallbatch;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm4075 extends BaseEdit
{
 private final String PROGNAME = "專案基金匯入外部單位代碼維護處理程式111-11-28  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm4075Func func = null;
  String kk1;
  String orgTabName = "mkt_extern_unit";
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
  else if (eqIgno(wp.buttonCode, "procMethod_H005"))
     {/* 啟動FTP接收 */
      strAction = "U";
      procMethodH005();
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
  else if (eqIgno(wp.buttonCode, "R2"))
     {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
     }
  else if (eqIgno(wp.buttonCode, "U2"))
     {/* 明細更新 */
      strAction = "U2";
      updateFuncU2();
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
              + sqlCol(wp.itemStr2("ex_extern_id"), "a.extern_id", "like%")
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
               + "a.extern_id,"
               + "a.extern_name,"
               + "a.disable_flag,"
               + "a.in_ref_ip_code";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by extern_id"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commRefIp("comm_in_ref_ip_code");

  commDisableFlag("comm_disable_flag");

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
  if (wp.itemStr2("kk_extern_id").length()==0)
     { 
      alertErr("查詢鍵必須輸入");
      return; 
     } 
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
               + "a.extern_id as extern_id,"
               + "a.extern_name,"
               + "a.disable_flag,"
               + "a.in_ref_ip_code,"
               + "a.out_ref_ip_code,"
               + "'' as fund_cnt,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(wp.itemStr2("kk_extern_id"), "a.extern_id")
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
      alertErr("查無資料, key= "+"["+ kk1+"]");
      return;
     }
  datareadWkdata();
  commCrtUser("comm_crt_user");
  checkButtonOff();
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
  wp.colSet("fund_cnt" , listMktBnData("mkt_bn_data","MKT_EXTERN_UNIT",wp.colStr("extern_id"),"1"));

 }
// ************************************************************************
 public void dataReadR2() throws Exception
 {
  if (wp.colStr("extern_id").length()==0)
     {
      wp.colSet("extern_id",itemKk("data_k3"));
      wp.itemSet("extern_id",itemKk("data_k3"));
     }
  dataReadR2(0);
 }
// ************************************************************************
 public void dataReadR2(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   this.selectNoLimit();
   bnTable = "mkt_bn_data";

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
               + " and table_name  =  'MKT_EXTERN_UNIT' "
               ;
   if (wp.respHtml.equals("mktm4075_fund"))
      wp.whereStr  += " and data_type  = '1' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("extern_id"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("extern_id") +  "'";
   wp.whereStr  += " order by 4,5,6,7 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm4075_fund"))
    commDataCode2("comm_data_code2");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   mktm02.Mktm4075Func func =new mktm02.Mktm4075Func(wp);
   int llOk = 0, llErr = 0;

    if (wp.itemStr2("EXTERN_NAME").length()==0)
       wp.colSet("EXTERN_NAME", wp.itemStr2("funcdsp_EXTERN_NAME"));

   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("data_code");
   String[] key2Data = wp.itemBuff("data_code2");

   wp.listCount[0] = key1Data.length;
   wp.colSet("IND_NUM", "" + key1Data.length);
   //-check duplication-

   int del2Flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       del2Flag=0;
       wp.colSet(ll, "ok_flag", "");

       for (int intm=ll+1;intm<key1Data.length; intm++)
         if ((key1Data[ll].equals(key1Data[intm]))) 
            {
             for (int intx=0;intx<optData.length;intx++) 
              { 
               if (optData[intx].length()!=0) 
               if (((ll+1)==Integer.valueOf(optData[intx]))||
                   ((intm+1)==Integer.valueOf(optData[intx])))
                  {
                   del2Flag=1;
                   break;
                  }
              }
             if (del2Flag==1) break;

             wp.colSet(ll, "ok_flag", "!");
             llErr++;
             continue;
            }
      }

   if (llErr > 0)
      {
       alertErr("資料值重複 : " + llErr);
       return;
      }

   //-delete no-approve-
   if (func.dbDeleteD2() < 0)
      {
       alertErr(func.getMsg());
       return;
      }

   //-insert-
   int deleteFlag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       deleteFlag=0;
       //KEY 不可同時為空字串
           if ((empty(key1Data[ll])) &&
              (empty(key2Data[ll])))
           continue;

       //-option-ON-
       for (int intm=0;intm<optData.length;intm++)
         {
          if (optData[intm].length()!=0)
          if ((ll+1)==Integer.valueOf(optData[intm]))
             {
              deleteFlag=1;
              break;
             }
          }
       if (deleteFlag==1) continue;

       func.varsSet("data_code", key1Data[ll]); 
       func.varsSet("data_code2", key2Data[ll]); 

       if (func.dbInsertI2() == 1) llOk++;
       else llErr++;

       //有失敗rollback，無失敗commit
       sqlCommit(llOk > 0 ? 1 : 0);
      }
   alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

   //SAVE後 SELECT
   dataReadR2(1);
 }
// ************************************************************************
 public int selectBndataCount(String bndataTable,String whereStr ) throws Exception
 {
   String sql1 = "select count(*) as bndataCount"
               + " from " + bndataTable
               + " " + whereStr
               ;

   sqlSelect(sql1);

   return((int)sqlNum("bndataCount"));
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktm02.Mktm4075Func func =new mktm02.Mktm4075Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr(func.getMsg());
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
       if ((wp.respHtml.equals("mktm4075_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("in_ref_ip_code").length()>0)
             {
             wp.optionKey = wp.colStr("in_ref_ip_code");
             }
          if (wp.colStr("in_ref_ip_code").length()>0)
             {
              wp.initOption ="--";
             }
          this.dddwList("dddw_ref_ip1"
                 ,"ecs_ref_ip_addr"
                 ,"trim(ref_ip_code)"
                 ,"trim(ref_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("out_ref_ip_code").length()>0)
             {
             wp.optionKey = wp.colStr("out_ref_ip_code");
             }
          if (wp.colStr("out_ref_ip_code").length()>0)
             {
              wp.initOption ="--";
             }
          this.dddwList("dddw_ref_ip2"
                 ,"ecs_ref_ip_addr"
                 ,"trim(ref_ip_code)"
                 ,"trim(ref_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm4075_fund")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql = "";
          lsSql =  procDynamicDddwDataCode(wp.itemStr2("sel_data_code"));
          wp.colSet("sel_data_code" , wp.itemStr2("sel_data_code"));
          dddwList("dddw_data_code", lsSql);

         }
      } catch(Exception ex){}
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
 public void commRefIp(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " ref_name as column_ref_name "
            + " from ecs_ref_ip_addr "
            + " where 1 = 1 "
            + " and   ref_ip_code = '"+wp.colStr(ii,"in_ref_ip_code")+"'"
            ;
       if (wp.colStr(ii,"in_ref_ip_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_ref_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode2(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " fund_name as column_fund_name "
            + " from mkt_loan_parm "
            + " where 1 = 1 "
            + " and   fund_code = '"+wp.colStr(ii,"data_code2")+"'"
            ;
       if (wp.colStr(ii,"data_code2").length()==0)
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
 public void commDisableFlag(String s1) throws Exception 
 {
  String[] cde = {"Y","N"};
  String[] txt = {"已停用","未停用"};
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
  String ajaxjDataCode2 = "";
  super.wp = wr;


  if (selectAjaxFunc30(
                    wp.itemStr2("ax_win_data_code").toUpperCase())!=0) 
     {
      wp.addJSON("ajaxj_data_code2", "");
      wp.addJSON("ajaxj_fund_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_data_code2", sqlStr(ii, "data_code2"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_fund_name", sqlStr(ii, "fund_name"));
 }
// ************************************************************************
 int selectAjaxFunc30(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " '' as data_code,  "
             + " '' as data_code2,  "
             + " '' as fund_name  "
             + " from  ptr_businday "
             + " union "
             + " select "
             + " fund_code as data_code,"
             + " fund_code as data_code2,"
             + " fund_name as fund_name "
             + " from  mkt_loan_parm "
             ;
   if (s1.length()>0)
      wp.sqlCmd = wp.sqlCmd
                + " where substr(fund_code,3,2) =  upper('"+ s1 +"') ";

   wp.sqlCmd = wp.sqlCmd
             + " order by 1 ";
   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("來源代碼:["+s1+"]查無資料");
       return 1;
      }

   return 0;
 }

// ************************************************************************
 public void procMethodH005() throws Exception
  {
    EcsCallbatch batch = new EcsCallbatch(wp) ;

    rc=batch.callBatch("MktH005 " + wp.itemStr2("extern_id"));
    if (rc!=1)
       {
        alertErr(wp.itemStr2("extern_id") + " : callbatch[MktH005] 啟動失敗");
       }
    else
       {
        alertMsg("批次已啟動成功! ");
       }

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
 String procDynamicDddwFundCode()  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " payment_type as db_code, "
          + " payment_type||' '||bill_desc  as db_desc "
          + " from ptr_payment "
          + " where payment_type in ( "
          + "       select distinct substr(fund_code,1,4) "
          + "       from mkt_loan_parm ) "
          + " and   fund_flag = 'Y' "
          ;
   return lsSql;
 }
// ************************************************************************
 String  listMktBnData(String s1,String s2,String s3,String s4) throws Exception
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
 String procDynamicDddwDataCode(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " fund_code as db_code, "
          + " fund_code||' '||fund_name as db_desc "
          + " from mkt_loan_parm ";

   if (s1.length()>0)
       lsSql =  lsSql
              + " where substr(fund_code,3,2) ='" + s1 +"' "
              ;
   lsSql =  lsSql
          + " order by fund_code "
          + " fetch first 999 rows only ";

   return lsSql;
 }

// ************************************************************************

}  // End of class
