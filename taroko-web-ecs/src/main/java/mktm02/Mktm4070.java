/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/06/11  V1.00.03   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                           *
* 112-05-08  V1.00.02  Zuwei Su       匯入名單資料格式修改,匯入卡號時正確顯示訊息                                                                          *
* 112-05-10  V1.00.03  Zuwei Su       HceCard查詢到再查詢轉出卡號用原本selectCrdCard()設定資料進行設定    
* 112-07-26  V1.00.04  Machao         新增 6.TCB_ID(生日禮) 匯入功能         
* 112-09-07  V1.00.05  Machao         媒體檔轉入資料有誤   存入调整                                             *
***************************************************************************/
package mktm02;

import mktm02.Mktm4070Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm4070 extends BaseEdit
{
 private final String PROGNAME = "專案現金回饋參數檔維護處理程式112-05-08  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm4070Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_loan_parm";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String   batchNo     = "";
  int errorCnt=0,recCnt=0,notifyCnt=0,colNum=0;
  int errFlag = 0;
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
  else if (eqIgno(wp.buttonCode, "R4"))
     {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
     }
  else if (eqIgno(wp.buttonCode, "U4"))
     {/* 明細更新 */
      strAction = "U4";
      updateFuncU4();
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
  else if (eqIgno(wp.buttonCode, "R3"))
     {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
     }
  else if (eqIgno(wp.buttonCode, "U3"))
     {/* 明細更新 */
      strAction = "U3";
      updateFuncU3();
     }
  else if (eqIgno(wp.buttonCode, "M"))
     {/* 瀏覽功能 :skip-page*/
      queryRead();
     }
  else if (eqIgno(wp.buttonCode, "S"))
     {/* 動態查詢 */
      querySelect();
     }
  else if (eqIgno(wp.buttonCode, "UPLOAD2"))
     {/* 匯入檔案 */
      procUploadFile(2);
      checkButtonOff();
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
              + sqlChkEx(wp.itemStr2("ex_fund_name"), "2", "")
              + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code", "like%")
              + sqlChkEx(wp.itemStr2("ex_apr_flag"), "2", "")
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
               + "a.fund_code,"
               + "a.fund_name,"
               + "a.effect_months,"
               + "a.apr_user,"
               + "a.apr_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.fund_code"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commAprUser("comm_apr_user");


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
               + "a.fund_code as fund_code,"
               + "a.apr_flag,"
               + "a.fund_name,"
               + "a.effect_months,"
               + "a.stop_flag,"
               + "a.list_cond,"
               + "a.list_flag,"
               + "'' as list_flag_cnt,"
               + "a.list_feedback_date,"
               + "a.add_vouch_no,"
               + "a.rem_vouch_no,"
               + "a.acct_type_sel,"
               + "'' as acct_type_sel_cnt,"
               + "a.group_code_sel,"
               + "'' as group_code_sel_cnt,"
               + "a.group_oppost_cond,"
               + "a.feedback_lmt,"
               + "a.res_flag,"
               + "a.res_total_cnt,"
               + "a.exec_s_months,"
               + "a.move_cond,"
               + "a.bil_mcht_cond,"
               + "a.merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.issue_a_months,"
               + "a.mcode,"
               + "a.cancel_rate,"
               + "a.cancel_scope,"
               + "a.cancel_event,"
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
                   + sqlCol(km1, "a.fund_code")
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
  commAprFlag2("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("fund_code");
  listWkdata();
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("list_flag_cnt" , listMktImloanList("mkt_imloan_list_t","mkt_imloan_list",wp.colStr("fund_code"),""));
  wp.colSet("acct_type_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"1"));
  wp.colSet("group_code_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"2"));
  wp.colSet("merchant_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"3"));
  wp.colSet("mcht_group_sel_cnt" , listMktParmData("mkt_parm_data_t","MKT_LOAN_PARM",wp.colStr("fund_code"),"4"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  wp.colSet("list_flag_cnt" , listMktImloanList("mkt_imloan_list","mkt_imloan_list",wp.colStr("fund_code"),""));
  wp.colSet("acct_type_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"1"));
  wp.colSet("group_code_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"2"));
  wp.colSet("merchant_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"3"));
  wp.colSet("mcht_group_sel_cnt" , listMktParmData("mkt_parm_data","MKT_LOAN_PARM",wp.colStr("fund_code"),"4"));
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.fund_code as fund_code,"
               + "a.apr_flag as apr_flag,"
               + "a.fund_name as fund_name,"
               + "a.effect_months as effect_months,"
               + "a.stop_flag as stop_flag,"
               + "a.list_cond as list_cond,"
               + "a.list_flag as list_flag,"
               + "'' as list_flag_cnt,"
               + "a.list_feedback_date as list_feedback_date,"
               + "a.add_vouch_no as add_vouch_no,"
               + "a.rem_vouch_no as rem_vouch_no,"
               + "a.acct_type_sel as acct_type_sel,"
               + "'' as acct_type_sel_cnt,"
               + "a.group_code_sel as group_code_sel,"
               + "'' as group_code_sel_cnt,"
               + "a.group_oppost_cond as group_oppost_cond,"
               + "a.feedback_lmt as feedback_lmt,"
               + "a.res_flag as res_flag,"
               + "a.res_total_cnt as res_total_cnt,"
               + "a.exec_s_months as exec_s_months,"
               + "a.move_cond as move_cond,"
               + "a.bil_mcht_cond as bil_mcht_cond,"
               + "a.merchant_sel as merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel as mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.issue_a_months as issue_a_months,"
               + "a.mcode as mcode,"
               + "a.cancel_rate as cancel_rate,"
               + "a.cancel_scope as cancel_scope,"
               + "a.cancel_event as cancel_event,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.fund_code")
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
  listWkdataAft();
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("fund_code");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      km1 = wp.itemStr2("fund_code");
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
   km1 = wp.itemStr2("fund_code");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr2("fund_code");
      strAction = "A";
      wp.itemSet("aud_type","U");
      insertFunc();
      if (rc==1) dataRead();
     }
  wp.colSet("fst_apr_flag",fstAprFlag);
 }
// ************************************************************************
 public void dataReadR4() throws Exception
 {
  dataReadR4(0);
 }
// ************************************************************************
 public void dataReadR4(int fromType) throws Exception
 {
   String bnTable="";

   if ((wp.itemStr2("fund_code").length()==0)||
       (wp.itemStr2("aud_type").length()==0))
      {
       alertErr("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   this.selectNoLimit();
   if ((wp.itemStr2("aud_type").equals("Y"))||
       (wp.itemStr2("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "mkt_imloan_list";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_imloan_list_t";
      }

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "0 as r2_mod_seqno, "
                + "fund_code, "
                + "mod_pgm  as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
                ;
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  fund_code = :fund_code ";
   setString("fund_code", wp.itemStr2("fund_code"));
   whereCnt += " and  fund_code = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5 ";
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
  }
// ************************************************************************
 public void updateFuncU4() throws Exception
 {
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

   if ((wp.itemStr2("fund_code").length()==0)||
       (wp.itemStr2("aud_type").length()==0))
      {
       alertErr("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   this.selectNoLimit();
   if ((wp.itemStr2("aud_type").equals("Y"))||
       (wp.itemStr2("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "mkt_parm_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_parm_data_t";
      }

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'MKT_LOAN_PARM' "
                ;
   if (wp.respHtml.equals("mktm4070_actp"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktm4070_gpcd"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktm4070_aaa1"))
      wp.whereStr  += " and data_type  = '4' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
   wp.whereStr  += " order by 4,5,6 ";
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
   if (wp.respHtml.equals("mktm4070_actp"))
    commAcctType("comm_data_code");
   if (wp.respHtml.equals("mktm4070_gpcd"))
    commGroupCode("comm_data_code");
   if (wp.respHtml.equals("mktm4070_aaa1"))
    commMechtGp("comm_data_code");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);
   int llOk = 0, llErr = 0;

   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("data_code");

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
       if ((empty(key1Data[ll])))
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
 public void dataReadR3() throws Exception
 {
  dataReadR3(0);
 }
// ************************************************************************
 public void dataReadR3(int fromType) throws Exception
 {
   String bnTable="";

   if ((wp.itemStr2("fund_code").length()==0)||
       (wp.itemStr2("aud_type").length()==0))
      {
       alertErr("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   this.selectNoLimit();
   if ((wp.itemStr2("aud_type").equals("Y"))||
       (wp.itemStr2("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "mkt_parm_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_parm_data_t";
      }

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
              + " and table_name  =  'MKT_LOAN_PARM' "
                ;
   if (wp.respHtml.equals("mktm4070_mrch"))
      wp.whereStr  += " and data_type  = '3' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("fund_code"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("fund_code") +  "'";
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
  }
// ************************************************************************
 public void updateFuncU3() throws Exception
 {
   mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);
   int llOk = 0, llErr = 0;

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
         if ((key1Data[ll].equals(key1Data[intm])) &&
             (key2Data[ll].equals(key2Data[intm]))) 
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
   if (func.dbDeleteD3() < 0)
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

       if (func.dbInsertI3() == 1) llOk++;
       else llErr++;

       //有失敗rollback，無失敗commit
       sqlCommit(llOk > 0 ? 1 : 0);
      }
   alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

   //SAVE後 SELECT
   dataReadR3(1);
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
  mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);

  if (wp.respHtml.indexOf("_detl") > 0)
     if (!wp.colStr("aud_type").equals("Y")) listWkdataAft();

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr(func.getMsg());
  else
    {
     if (wp.respHtml.indexOf("_nadd") > 0)
        alertMsg("明細資料, 請於主檔新增後維護!");
    }
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
       if ((wp.respHtml.equals("mktm4070_actp")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_acct_type"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm4070_gpcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm4070_aaa1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 ");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("2"))
     {
      return " and fund_name like  '%"+ wp.itemStr2("ex_fund_name")+"%'";
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
            + " and   acct_type = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
 public void commGroupCode(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " group_name as column_group_name "
            + " from ptr_group_code "
            + " where 1 = 1 "
            + " and   group_code = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
 public void commMechtGp(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcht_group_desc as column_mcht_group_desc "
            + " from mkt_mcht_gp "
            + " where 1 = 1 "
            + " and   mcht_group_id = '"+wp.colStr(ii,"data_code")+"'"
            ;
       if (wp.colStr(ii,"data_code").length()==0)
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
 public void wfAjaxFunc1(TarokoCommon wr) throws Exception
 {
  super.wp = wr;


  if (selectAjaxFunc10(
                    wp.itemStr2("ax_win_fund_code"))!=0) 
     {
      wp.addJSON("payment_type","");
      return;
     }

  wp.addJSON("payment_type",sqlStr("payment_type"));
 }
// ************************************************************************
 int selectAjaxFunc10(String s1) throws Exception
  {
   if (s1.length()<4)
      {
       alertErr("現金回饋代碼長度至少 4碼!");
       return 1;
      }

   wp.sqlCmd = " select "
             + " a.payment_type as payment_type "
             + " from  ptr_payment a "
             + " where a.payment_type = substr('"+s1+"',1,4) "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("現金回饋前4碼在ptrm0030繳款類別參數查無資料!");
       return 1;
      }

   return 0;
 }

// ************************************************************************
public void procUploadFile(int loadType) throws Exception
 {
  if (wp.colStr(0,"ser_num").length()>0)
     wp.listCount[0] = wp.itemBuff("ser_num").length;
  if (wp.itemStr2("zz_file_name").indexOf(".xls")!=-1) 
     {
      alertErr("上傳格式: 不可為 excel 格式");
      return;
     }
  if (empty("zz_file_name"))
     {
      alertErr("上傳檔名: 不可空白");
      return;
     }
  
  String funCode = wp.itemStr("fund_code");
  String[] listData = selectMktLoanParm(funCode);
  if(listData[0].equals("Y") && listData[1].length()!=0) {
	  errmsg("[名單匯入] 該現金回饋代碼已回饋, 不可再異動! ");
      return;
  }

  if (loadType==2)  fileDataImp2();
 }
//************************************************************************
public String[] selectMktLoanParm(String funCode) {
	if (wp.itemStr2("ex_apr_flag").equals("N")) {
		orgTabName = orgTabName +"_t";
	}
	String lsSql = " select list_cond, list_feedback_date "
			+ "from " + orgTabName
			+ " where fund_code = ? ";
    sqlSelect(lsSql, new Object[]{funCode});

    String[] listData = {sqlStr("list_cond"),sqlStr("list_feedback_date")};
    return listData;
}
// ************************************************************************
int fileUpLoad()
 {
  TarokoUpload func = new TarokoUpload();
  try {
       func.actionFunction(wp);
       wp.colSet("zz_file_name", func.fileName);
      }
   catch(Exception ex)
      {
       return -1;
      }

   return func.rc;
}
// ************************************************************************
void fileDataImp2() throws Exception
 {
  TarokoFileAccess tf = new TarokoFileAccess(wp);

  String inputFile = wp.itemStr2("zz_file_name");
  int fi = tf.openInputText(inputFile,"UTF-8");
//  int fi = tf.openInputText(inputFile,"MS950");

  if (fi == -1) return;

  String sysUploadType  = wp.itemStr2("sys_upload_type");
  String sysUploadAlias = wp.itemStr2("sys_upload_alias");

  mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);

  if (sysUploadAlias.equals("list"))
     {
      // if has pre check procudure, write in here 
      func.dbDeleteD2List("MKT_IMLOAN_LIST_T");
     }
  if (sysUploadAlias.equals("gpcd"))
     {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Gpcd("MKT_PARM_DATA_T");
     }
  if (sysUploadAlias.equals("aaa1"))
     {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Aaa1("MKT_PARM_DATA_T");
     }

  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  tranSeqStr = comr.getSeqno("MKT_MODSEQ");

  String ss="";
  int llOk=0, llCnt=0,llErr=0,llChkErr=0;
  int lineCnt =0;
  errorCnt=0;
  while (true)
   {
    ss = tf.readTextFile(fi);
    if (tf.endFile[fi].equals("Y")) break;
    lineCnt++;
    if (sysUploadAlias.equals("list"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("gpcd"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }
    if (sysUploadAlias.equals("aaa1"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }

    llCnt++; 

    for (int inti=0;inti<10;inti++) logMsg[inti]="";
    logMsg[10]=String.format("%02d",lineCnt);

    if (sysUploadAlias.equals("list")) {
    	if(wp.itemStr("list_flag").equals("6")) {
    		if (checkUploadfileList_6(ss)!=0) continue;
    	}else {
    		if (checkUploadfileList(ss)!=0) continue;
    	}
    }
    if (sysUploadAlias.equals("gpcd"))
       if (checkUploadfileGpcd(ss)!=0) continue;
    if (sysUploadAlias.equals("aaa1"))
       if (checkUploadfileAaa1(ss)!=0) continue;
   llOk++;

   if (notifyCnt==0)
      {
       if (sysUploadAlias.equals("list"))
          {
           if (func.dbInsertI2List("MKT_IMLOAN_LIST_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
           if(wp.itemStr2("list_flag").equals("6") && llErr > errFlag)
           {
        	   errFlag += 1;
        	   errorCnt+=1;
        	   	 logMsg[0]               = "資料重複錯誤";           // 原因說明
        	     logMsg[1]               = "2" ;                     // 錯誤類別
        	     logMsg[2]               = "1";                      // 欄位位置
        	     logMsg[3]               = uploadFileDat[1];         // 欄位內容
        	     logMsg[4]               = "本ID資料重複 ";         // 錯誤說明
        	     logMsg[5]               = "身分證號";               // 欄位說明
        	     func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           }
           if(wp.itemStr2("list_flag").equals("2") && llErr > errFlag)
           {
        	   errFlag += 1;
        	   errorCnt+=1;
        	   	 logMsg[0]               = "資料重複錯誤";           // 原因說明
        	     logMsg[1]               = "2" ;                     // 錯誤類別
        	     logMsg[2]               = "1";                      // 欄位位置
        	     logMsg[3]               = uploadFileDat[1];         // 欄位內容
        	     logMsg[4]               = "本卡號資料重複 ";         // 錯誤說明
        	     logMsg[5]               = uploadFileDat[0];               // 欄位說明
        	     func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           }
          }
       if (sysUploadAlias.equals("gpcd"))
          {
           if (func.dbInsertI2Gpcd("MKT_PARM_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
       if (sysUploadAlias.equals("aaa1"))
          {
           if (func.dbInsertI2Aaa1("MKT_PARM_DATA_T",uploadFileCol,uploadFileDat) != 1) llErr++;;
          }
      }
   }

  if (llErr!=0) {
      notifyCnt=1;
  }
  // 匯入卡號(名單類別=2:Card_NO)時,若有屬(名單類別=2:Card_NO)時,全部成功(沒有重複&&沒有失敗)才轉入
  if (sysUploadAlias.equals("list") && wp.itemStr2("list_flag").equals("2") && errorCnt > 0) {
	  llOk = llCnt - errorCnt;
//      notifyCnt=1;
  }
//匯入卡號(名單類別=6:TCB_ID)時,若有屬(名單類別=6:TCB_ID)時,全部成功(沒有重複)才轉入
 if (sysUploadAlias.equals("list") && wp.itemStr2("list_flag").equals("6") && llErr > 0) {
     notifyCnt=1;
     llOk = 0;
 }
 if (sysUploadAlias.equals("list") && wp.itemStr2("list_flag").equals("6")){
	 if(errorCnt>0) {
		 llOk = llCnt - errorCnt;
	 }
 } 
  if (notifyCnt==1)
     {
      if (sysUploadAlias.equals("list"))
    	 func.dbDeleteD2List("MKT_IMLOAN_LIST_T");
      if (sysUploadAlias.equals("gpcd"))
         func.dbDeleteD2Gpcd("MKT_PARM_DATA_T");
      if (sysUploadAlias.equals("aaa1"))
         func.dbDeleteD2Aaa1("MKT_PARM_DATA_T");
      func.dbInsertEcsNotifyLog(tranSeqStr,(llErr+llChkErr));
     }
  else
  {
	  func.dbInsertEcsNotifyLog(tranSeqStr,(llErr+llChkErr));
  }

  sqlCommit(1);  // 1:commit else rollback

  if (notifyCnt==0)
	     alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 轉入");
	  else
	     alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 不轉入");
  
  tf.closeInputText(fi);
  tf.deleteFile(inputFile);


  return;
 }
// ************************************************************************
int  checkUploadfileList(String ss) throws Exception
 {
  mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "list_data";
  uploadFileCol[1]  = "feedback_amt";

  // ========  [I]nsert table column  ========
  uploadFileCol[2]  = "list_flag";
  uploadFileCol[3]  = "fund_code";
  uploadFileCol[4]  = "id_p_seqno";
  uploadFileCol[5]  = "acct_type";
  uploadFileCol[6]  = "p_seqno";
  uploadFileCol[7]  = "card_no";
  uploadFileCol[8]  = "ori_card_no";
  uploadFileCol[9]  = "vd_flag";
  uploadFileCol[16]  = "error_code";

  // ==== insert table content default =====
  uploadFileDat[2]  = wp.itemStr2("list_flag");
  uploadFileDat[3]  = wp.itemStr2("fund_code");
  uploadFileDat[16] = "00";
  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<2;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);
  //******************************************************************
//  errorCnt=0;
  String[] str = ss.split(",");
  if (str.length!=2)
	{
	 errorCnt+=1;
   logMsg[0]               = "格式錯誤";           // 原因說明
   logMsg[1]               = "1" ;                     // 錯誤類別
   logMsg[2]               = "1";                      // 欄位位置
   logMsg[3]               = uploadFileDat[1];         // 欄位內容
   logMsg[4]               = "本卡號格式錯誤 ";         // 錯誤說明
   logMsg[5]               = uploadFileDat[0];               // 欄位說明
   func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
   return(1);
	}
  if (uploadFileDat[1].length()==0) uploadFileDat[1]="0";
  if (!comm.isNumber(uploadFileDat[1]))
     {
      errorCnt+=1;
      logMsg[0]               = "資料檢核錯誤";           // 原因說明
      logMsg[1]               = "3" ;                     // 錯誤類別
      logMsg[2]               = "2";                      // 欄位位置
      logMsg[3]               = uploadFileDat[1];         // 欄位內容
      logMsg[4]               = "回饋金額非數字 ";         // 錯誤說明
      logMsg[5]               = uploadFileDat[0];               // 欄位說明
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
     }
  if (Integer.valueOf((uploadFileDat[1]))==0)
     {
      errorCnt+=1;
      logMsg[0]               = "資料檢核錯誤";           // 原因說明   
      logMsg[1]               = "3" ;                     // 錯誤類別   
      logMsg[2]               = "2";                      // 欄位位置   
      logMsg[3]               = uploadFileDat[1];         // 欄位內容   
      logMsg[4]               = "回饋金額為 0 ";           // 錯誤說明      
      logMsg[5]               = uploadFileDat[0];               // 欄位說明  
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
     }

  if (wp.itemStr2("list_flag").equals("1")) return(0);
/*
     if (select_crd_idno()!=0)
        {
         error_cnt=1;
         logMsg[0]               = "資料檢核錯誤";           // 原因說明
         logMsg[1]               = "3" ;                     // 錯誤類別
         logMsg[2]               = "1";                      // 欄位位置
         logMsg[3]               = uploadFileDat[0];         // 欄位內容
         logMsg[4]               = "信用卡無此卡人";         // 錯誤說明
         logMsg[5]               = "信用卡人檔無此 ID 資料"; // 欄位說明
         func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
         return(0);
        }
*/
  //******************************************************************
/*
  if (wp.item_ss("list_flag").equals("6"))
     if (select_dbc_idno()!=0)
        {
         error_cnt=1;
         logMsg[0]               = "資料檢核錯誤";       // 原因說明
         logMsg[1]               = "3" ;                 // 錯誤類別
         logMsg[2]               = "1";                  // 欄位位置
         logMsg[3]               = uploadFileDat[0];     // 欄位內容
         logMsg[4]               = "VD無此卡人";           // 錯誤說明
         logMsg[5]               = "VD卡人檔無此 ID 資料"; // 欄位說明
         func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
         return(0);
        }
*/
  //******************************************************************
  if (wp.itemStr2("list_flag").equals("2"))
     if (selectCrdCard_2(uploadFileDat[0])!=0 && selectHceCard_2(uploadFileDat[0]) != 0
         && selectHceSirCard_2(uploadFileDat[0]) != 0 )
        {
         errorCnt+=1;
         logMsg[0]               = "資料檢核錯誤";       // 原因說明
         logMsg[1]               = "3" ;                 // 錯誤類別
         logMsg[2]               = "1";                  // 欄位位置
         logMsg[3]               = uploadFileDat[1];     // 欄位內容
         logMsg[4]               = "無此流通卡號 ";
         logMsg[5]               = uploadFileDat[0];
         uploadFileDat[16]       = "02";
         func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
         return(0);
        }
  //******************************************************************
  if (wp.itemStr2("list_flag").equals("3"))
     if (selectIpsCard()!=0)
        {
         errorCnt+=1;
         logMsg[0]               = "資料檢核錯誤";       // 原因說明
         logMsg[1]               = "3" ;                 // 錯誤類別
         logMsg[2]               = "1";                  // 欄位位置
         logMsg[3]               = uploadFileDat[1];     // 欄位內容
         logMsg[4]               = "無此一卡通卡號 ";
         logMsg[5]               = uploadFileDat[0];
         func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
         return(0);
        }
  //******************************************************************
  if (wp.itemStr2("list_flag").equals("4"))
     if (selectTscCard()!=0)
        {
         errorCnt+=1;
         logMsg[0]               = "資料檢核錯誤";       // 原因說明
         logMsg[1]               = "3" ;                 // 錯誤類別
         logMsg[2]               = "1";                  // 欄位位置
         logMsg[3]               = uploadFileDat[1];     // 欄位內容
         logMsg[4]               = "無此悠遊卡號 ";
         logMsg[5]               = uploadFileDat[0];
         func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
         return(0);
        }
  //******************************************************************
  if (wp.itemStr2("list_flag").equals("5"))
     if (selectIchCard()!=0)
        {
         errorCnt+=1;
         logMsg[0]               = "資料檢核錯誤";       // 原因說明
         logMsg[1]               = "3" ;                 // 錯誤類別
         logMsg[2]               = "1";                  // 欄位位置
         logMsg[3]               = uploadFileDat[1];     // 欄位內容
         logMsg[4]               = "無此愛金卡號 ";
         logMsg[5]               = uploadFileDat[0]; 
         func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
         return(0);
        }
  //******************************************************************

  return 0;
 }
//************************************************************************
int  checkUploadfileList_6(String ss) throws Exception
{
 mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);

 for (int inti=0;inti<50;inti++)
   {
    uploadFileCol[inti] = "";
    uploadFileDat[inti] = "";
   }
 
	uploadFileCol[0]  = "list_flag";
	uploadFileCol[1]  = "list_data";
	uploadFileCol[2]  = "feedback_amt";
	uploadFileCol[3]  = "fund_code";
	uploadFileCol[4]  = "id_p_seqno";
	uploadFileCol[5]  = "acct_type";
	uploadFileCol[6]  = "p_seqno";
	uploadFileCol[7]  = "card_no";
	uploadFileCol[8]  = "ori_card_no";
	uploadFileCol[9]  = "vd_flag";
	uploadFileCol[10]  = "birth_day";
	uploadFileCol[11]  = "name";
	uploadFileCol[12]  = "branch_name";
	uploadFileCol[13]  = "branch_onlineno";
	uploadFileCol[14]  = "branch_acctno";
	uploadFileCol[15]  = "title";
	uploadFileCol[16]  = "error_code";
	uploadFileCol[17]  = "bank_acctno";
	uploadFileCol[18]  = "proc_flag";


 // ==== insert table content default =====
	uploadFileDat[0]  = wp.itemStr2("list_flag");
	uploadFileDat[2]  = "1000" ;
	uploadFileDat[3]  = wp.itemStr2("fund_code");
	uploadFileDat[18] = "N";

//	uploadFileDat[11] = uploadFileDat[0];

 int okFlag=0;
 int errFlag=0;
 int[] begPos = {1};

 for (int inti=0;inti<1;inti++)
     {
      uploadFileDat[1] = comm.getStr(ss, inti+1 ,",");
      uploadFileDat[11]  = comm.getStr(ss, inti+3 ,",");
      uploadFileDat[12]  = comm.getStr(ss, inti+4 ,",");
      uploadFileDat[13]  = comm.getStr(ss, inti+5 ,",");
      uploadFileDat[14]  = comm.getStr(ss, inti+6 ,",");
      uploadFileDat[15]  = comm.getStr(ss, inti+7 ,",");

      if (uploadFileDat[inti].length()!=0) okFlag=1;
     }
 if (okFlag==0) return(1);
 //******************************************************************
// errorCnt=0;
 String[] str = ss.split(",");
 if (str.length!=7)
 	{
	 errorCnt+=1;
     logMsg[0]               = "格式錯誤";           // 原因說明
     logMsg[1]               = "1" ;                     // 錯誤類別
     logMsg[2]               = "1";                      // 欄位位置
     logMsg[3]               = uploadFileDat[1];         // 欄位內容
     logMsg[4]               = "本ID格式錯誤 ";         // 錯誤說明
     logMsg[5]               = "身分證號";               // 欄位說明
     func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
     return(1);
 	}
 if (uploadFileDat[1].length()==0) uploadFileDat[1]="0";
 if (selectCrdEmployee_6(uploadFileDat[1])!=0)
    {
     errorCnt+=1;
     logMsg[0]               = "資料檢核錯誤";           // 原因說明
     logMsg[1]               = "3" ;                     // 錯誤類別
     logMsg[2]               = "1";                      // 欄位位置
     logMsg[3]               = uploadFileDat[1];         // 欄位內容
     logMsg[4]               = "本ID不是tcb 員工 ";         // 錯誤說明
     logMsg[5]               = "身分證號";               // 欄位說明
     func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
     return(0);
    }
 if (selectCrdCard_6(uploadFileDat[1])!=0 && selectCrdCard_6(uploadFileDat[1])==1)
    {
	 errorCnt+=1;
     logMsg[0]               = "資料檢核錯誤";           // 原因說明
     logMsg[1]               = "3" ;                     // 錯誤類別
     logMsg[2]               = "1";                      // 欄位位置
     logMsg[3]               = uploadFileDat[1];         // 欄位內容
     logMsg[4]               = "本員工ID未申請信用卡 ";         // 錯誤說明
     logMsg[5]               = "身分證號";               // 欄位說明
     func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
     return(0);
    }
	 if (selectCrdCard_6(uploadFileDat[1])!=0 && selectCrdCard_6(uploadFileDat[1])==2)
	 {
		 errorCnt+=1;
	  logMsg[0]               = "資料檢核錯誤";           // 原因說明
	  logMsg[1]               = "3" ;                     // 錯誤類別
	  logMsg[2]               = "1";                      // 欄位位置
	  logMsg[3]               = uploadFileDat[1];         // 欄位內容
	  logMsg[4]               = "本員工ID無流通卡 ";         // 錯誤說明
	  logMsg[5]               = "身分證號";               // 欄位說明
	  func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
	  return(0);
	 }

     return 0;
}
// ************************************************************************
 int  selectCrdCard_2(String cardNo) throws Exception
 {
  wp.sqlCmd = " select "
            + " id_p_seqno, "
            + " acct_type, "
            + " p_seqno, "
            + " card_no, "
            + " ori_card_no "
            + " from crd_card "
            + " where CURRENT_CODE = '0' and card_no     = '"+cardNo+"' "
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = sqlStr("id_p_seqno");
  uploadFileDat[5] = sqlStr("acct_type");
  uploadFileDat[6] = sqlStr("p_seqno");
  uploadFileDat[7] = sqlStr("card_no");
  uploadFileDat[8] = sqlStr("ori_card_no");
  uploadFileDat[9] = "N";
  return(0);
 }
//************************************************************************
int  selectCrdCard_6(String idNo) throws Exception
{
wp.sqlCmd = " select "
		  + " b.id_no, "
          + " a.id_p_seqno, "
          + " a.p_seqno, "
          + " a.acct_type, "
          + " a.card_no, "
          + " a.ori_card_no, "
          + " a.current_code "
          + " from crd_card a, crd_idno b"
          + " where b.id_no = ? "
          + " and a.id_p_seqno = b.id_p_seqno "
          + " and a.acct_type = '01' order by a.CARD_NO"
          ;
		  setString(idNo);
	this.sqlSelect();
	
	if (sqlRowNum<=0) {
		uploadFileDat[9] = "N";
		uploadFileDat[16]  = "01";
		return(1);
	}
	
	String flag_current ="";
	int ii =0;
	for(int i =0;i<sqlRowNum;i++) {
		if(sqlStr(i,"current_code").equals("0")) {
			flag_current = "Y";
			ii = i;
			break;
		}
	}
	if(flag_current.equals("Y")) {
		uploadFileDat[4] = sqlStr(ii,"id_p_seqno");
		uploadFileDat[5] = sqlStr(ii,"acct_type");
		uploadFileDat[6] = sqlStr(ii,"p_seqno");
		uploadFileDat[7] = sqlStr(ii,"card_no");
		uploadFileDat[8] = sqlStr(ii,"ori_card_no");
		uploadFileDat[9] = "N";
		uploadFileDat[16]  = "00";
		return(0);
	}else {
		uploadFileDat[9] = "N";
		uploadFileDat[16]  = "02";
		return(2);
	}
}
 int selectCrdEmployee_6(String idno) throws Exception{
	wp.sqlCmd = " select EMPLOY_NO ,ID ,ACCT_NO from crd_employee  a   Where  a.ID =  ?";
	  setString(idno);
	this.sqlSelect();
	if (sqlRowNum<=0) {
		uploadFileDat[16] = "03";
		uploadFileDat[17] = " ";
		return(3);
	}else {
		uploadFileDat[16]  = "00";
		uploadFileDat[17]  = sqlStr("ACCT_NO");
		return(0);
	}
}
//************************************************************************
int selectHceCard_2(String v_cardNo) throws Exception {
    wp.sqlCmd = " select "
            + " id_p_seqno, "
            + " card_no "
            + " from hce_card "
            + " where v_card_no     = '"
            + v_cardNo
            + "' ";
    this.sqlSelect();

    if (sqlRowNum <= 0)
        return (1);

    selectCrdCard_2(sqlStr("card_no"));
    return (0);
}

//************************************************************************
int selectHceSirCard_2(String v_sir) throws Exception {
  wp.sqlCmd = " select "
          //+ " id_p_seqno, "
          + " card_no "
          + " from hce_card "
          + " where sir  = '"
          + v_sir
          + "' ";
  this.sqlSelect();

  if (sqlRowNum <= 0)
      return (1);

  selectCrdCard_2(sqlStr("card_no"));
  return (0);
}

// ************************************************************************
 int  selectDbcCard() throws Exception
 {
  wp.sqlCmd = " select "
            + " id_p_seqno, "
            + " acct_type, "
            + " p_seqno, "
            + " card_no, "
            + " ori_card_no "
            + " from dbc_card "
            + " where card_no     = '"+uploadFileDat[0]+"' "
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = sqlStr("id_p_seqno");
  uploadFileDat[5] = sqlStr("acct_type");
  uploadFileDat[6] = sqlStr("p_seqno");
  uploadFileDat[7] = sqlStr("card_no");
  uploadFileDat[8] = sqlStr("ori_card_no");
  uploadFileDat[9] = "Y";
  return(0);
 }
// ************************************************************************
 int  selectCrdIdno() throws Exception
 {
  wp.sqlCmd = " select "
            + " id_p_seqno "
            + " from crd_idno "
            + " where id_no       = '"+uploadFileDat[0]+"' ";
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = sqlStr("id_p_seqno");
  uploadFileDat[9] = "N";
  return(0);
 }
// ************************************************************************
 int  selectDbcIdno() throws Exception
 {
  wp.sqlCmd = " select "
            + " id_p_seqno "
            + " from dbc_idno "
            + " where id_no       = '"+uploadFileDat[0]+"' ";
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = sqlStr("id_p_seqno");
  uploadFileDat[9] = "Y";
  return(0);
 }
// ************************************************************************
 int  selectIchCard() throws Exception
 {
  wp.sqlCmd = " select "
            + " a.id_p_seqno, "
            + " a.acct_type, "
            + " a.p_seqno, "
            + " a.card_no, "
            + " a.ori_card_no "
            + " from crd_card a,ich_card b "
            + " where a.card_no     = b.card_no "
            + " and   ich_card_no   = '"+uploadFileDat[0]+"' "
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = sqlStr("id_p_seqno");
  uploadFileDat[5] = sqlStr("acct_type");
  uploadFileDat[6] = sqlStr("p_seqno");
  uploadFileDat[7] = sqlStr("card_no");
  uploadFileDat[8] = sqlStr("ori_card_no");
  uploadFileDat[9] = "N";
  return(0);
 }
// ************************************************************************
 int  selectIpsCard() throws Exception
 {
  wp.sqlCmd = " select "
            + " a.id_p_seqno, "
            + " a.acct_type, "
            + " a.p_seqno, "
            + " a.card_no, "
            + " a.ori_card_no "
            + " from crd_card a,ips_card b "
            + " where a.card_no     = b.card_no "
            + " and   ips_card_no   = '"+uploadFileDat[0]+"' "
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = sqlStr("id_p_seqno");
  uploadFileDat[5] = sqlStr("acct_type");
  uploadFileDat[6] = sqlStr("p_seqno");
  uploadFileDat[7] = sqlStr("card_no");
  uploadFileDat[8] = sqlStr("ori_card_no");
  uploadFileDat[9] = "N";
  return(0);
 }
// ************************************************************************
 int  selectTscCard() throws Exception
 {
  wp.sqlCmd = " select "
            + " a.id_p_seqno, "
            + " a.acct_type, "
            + " a.p_seqno, "
            + " a.card_no, "
            + " a.ori_card_no "
            + " from crd_card a,tsc_card b "
            + " where a.card_no     = b.card_no "
            + " and   tsc_card_no   = '"+uploadFileDat[0]+"' "
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  uploadFileDat[4] = sqlStr("id_p_seqno");
  uploadFileDat[5] = sqlStr("acct_type");
  uploadFileDat[6] = sqlStr("p_seqno");
  uploadFileDat[7] = sqlStr("card_no");
  uploadFileDat[8] = sqlStr("ori_card_no");
  uploadFileDat[9] = "N";
  return(0);
 }
// ************************************************************************

int  checkUploadfileGpcd(String ss) throws Exception
 {
  mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "data_code";

  // ========  [I]nsert table column  ========
  uploadFileCol[1]  = "table_name";
  uploadFileCol[2]  = "data_key";
  uploadFileCol[3]  = "data_type";
  uploadFileCol[4]  = "crt_date";
  uploadFileCol[5]  = "crt_user";

  // ==== insert table content default =====
  uploadFileDat[1]  = "MKT_LOAN_PARM";
  uploadFileDat[2]  = wp.itemStr2("fund_code");
  uploadFileDat[3]  = "2";
  uploadFileDat[4]  = wp.sysDate;
  uploadFileDat[5]  = wp.loginUser;

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<1;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);
  //******************************************************************
//  errorCnt=0;
  if (uploadFileDat[0].length()!=8) 
     {
      errorCnt+=1;
      logMsg[0]               = "資料檢核錯誤";           // 原因說明
      logMsg[1]               = "3" ;                     // 錯誤類別
      logMsg[2]               = "1";                      // 欄位位置
      logMsg[3]               = uploadFileDat[0];         // 欄位內容
      logMsg[4]               = "團體代號長度不對";       // 錯誤說明
      logMsg[5]               = "團體代號";               // 欄位說明
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(1);
     }

  if (selectPtrGroupCode()!=0)
     {
      errorCnt+=1;
      logMsg[0]               = "資料檢核錯誤";           // 原因說明
      logMsg[1]               = "3" ;                     // 錯誤類別
      logMsg[2]               = "1";                      // 欄位位置
      logMsg[3]               = uploadFileDat[0];         // 欄位內容
      logMsg[4]               = "團體代號長度不對";       // 錯誤說明  
      logMsg[5]               = "團體代號";               // 欄位說明
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(1);
     }

  return 0;
 }
// ************************************************************************
 int  selectPtrGroupCode() throws Exception
 {
  wp.sqlCmd = " select "
            + " group_code "
            + " from ptr_group_code "
            + " where group_code    = '"+uploadFileDat[0]+"' "
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(1);

  return(0);
 }
// ************************************************************************
int  checkUploadfileAaa1(String ss) throws Exception
 {
  mktm02.Mktm4070Func func =new mktm02.Mktm4070Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "data_code";
  uploadFileCol[1]  = "data_code2";

  // ========  [I]nsert table column  ========
  uploadFileCol[2]  = "table_name";
  uploadFileCol[3]  = "data_key";
  uploadFileCol[4]  = "data_type";
  uploadFileCol[5]  = "crt_date";
  uploadFileCol[6]  = "crt_user";

  // ==== insert table content default =====
  uploadFileDat[2]  = "MKT_LOAN_PARM";
  uploadFileDat[3]  = wp.itemStr2("fund_code");
  uploadFileDat[4]  = "3";
  uploadFileDat[5]  = wp.sysDate;
  uploadFileDat[6]  = wp.loginUser;

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<2;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);
  //******************************************************************
  if ((uploadFileDat[1].length()!=0)&&
      (uploadFileDat[1].length()<8))
   uploadFileDat[1] = "00000000".substring(0,8-uploadFileDat[1].length())
                    + uploadFileDat[1];


  return 0;
 }
// ************************************************************************
// ************************************************************************
 public void checkButtonOff() throws Exception
  {
  if (wp.colStr("list_cond").length()==0)
      wp.colSet("list_cond" , "N");

  if (wp.colStr("list_cond").equals("N"))
     {
      buttonOff("btnlist_disable");
      buttonOff("upllist_disable");
     }
  else
     {
      wp.colSet("btnlist_disable","");
      wp.colSet("upllist_disable","");
     }

  if (wp.colStr("acct_type_sel").length()==0)
      wp.colSet("acct_type_sel" , "0");

  if (wp.colStr("acct_type_sel").equals("0"))
     {
      buttonOff("btnactp_disable");
     }
  else
     {
      wp.colSet("btnactp_disable","");
     }

  if (wp.colStr("group_code_sel").length()==0)
      wp.colSet("group_code_sel" , "0");

  if (wp.colStr("group_code_sel").equals("0"))
     {
      buttonOff("btngpcd_disable");
      buttonOff("uplgpcd_disable");
     }
  else
     {
      wp.colSet("btngpcd_disable","");
      wp.colSet("uplgpcd_disable","");
     }

  if (wp.colStr("merchant_sel").length()==0)
      wp.colSet("merchant_sel" , "0");

  if (wp.colStr("merchant_sel").equals("0"))
     {
      buttonOff("btnmrch_disable");
      buttonOff("uplaaa1_disable");
     }
  else
     {
      wp.colSet("btnmrch_disable","");
      wp.colSet("uplaaa1_disable","");
     }

  if (wp.colStr("mcht_group_sel").length()==0)
      wp.colSet("mcht_group_sel" , "0");

  if (wp.colStr("mcht_group_sel").equals("0"))
     {
      buttonOff("btnaaa1_disable");
     }
  else
     {
      wp.colSet("btnaaa1_disable","");
     }

  if ((wp.colStr("aud_type").equals("Y"))||
      (wp.colStr("aud_type").equals("D")))
     {
      buttonOff("upllist_disable");
      buttonOff("uplgpcd_disable");
      buttonOff("uplaaa1_disable");
     }
  else
     {
      wp.colSet("upllist_disable","");
      wp.colSet("uplgpcd_disable","");
      wp.colSet("uplaaa1_disable","");
     }
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {

   if (wp.respHtml.indexOf("_nadd") > 0)
      {
       wp.colSet("res_flag"      , "2");
       wp.colSet("bil_mcht_cond" , "Y");
       //wp.colSet("issue_a_months", "99");   //抵用限制： X個月內未開卡者 (畫面不提供設定, 給定 default value)
       //wp.colSet("cancel_rate", "100.00");   //抵用範圍： 比率  (畫面不提供設定, 給定 default value)
      }

  buttonOff("btnlist_disable");
  buttonOff("btnactp_disable");
  buttonOff("btngpcd_disable");
  buttonOff("btnmrch_disable");
  buttonOff("btnaaa1_disable");
  return;
 }
// ************************************************************************
 String  listMktParmData(String s1,String s2,String s3,String s4) throws Exception
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
 String  listMktImloanList(String s1,String s2,String s3,String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where  fund_code = '" + s3 +"' "
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

   return("0");
 }
// ************************************************************************

// ************************************************************************

}  // End of class
