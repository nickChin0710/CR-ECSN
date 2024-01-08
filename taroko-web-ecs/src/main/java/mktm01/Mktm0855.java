/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/10/26  V1.00.10   Allen Ho      Initial                              *
* 112/02/08  V1.00.02   Zuwei Su      naming rule update                   *
*                                                                          *
***************************************************************************/
package mktm01;

import mktm01.Mktm0855Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0855 extends BaseEdit
{
 private final String PROGNAME = "行銷通路活動登錄參數維護處理程式112/02/08 V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0855Func func = null;
  String kk1,kk2;
  String km1,km2;
  String fstAprFlag = "";
  String orgTabName = "mkt_chanrec_parm";
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
  else if (eqIgno(wp.buttonCode, "AJAX"))
  {/* nothing to do */
   strAction = "";
   String method = wp.itemStr("method");
   switch (method) {
    case "wfAjaxFunc2":
        wfAjaxFunc2(wp);
        break;

    default:
        break;
}
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
  if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
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
               + "a.active_code,"
               + "a.active_seq,"
               + "a.record_group_no,"
               + "a.record_date_sel,"
               + "a.pur_date_sel,"
               + "a.week_cond,"
               + "a.month_cond,"
               + "a.purchase_type_sel,"
               + "a.threshold_sel";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.active_code"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commActiveCode("comm_active_code");
  commRecordGroupNo("comm_record_group_no");

  commRecordDateSel("comm_record_date_sel");
  commPdateSel("comm_pur_date_sel");
  commPurcTypeSel("comm_purchase_type_sel");
  commThresholdSel("comm_threshold_sel");

  wp.setPageValue();
 }
// ************************************************************************
 @Override
  public void querySelect()
 {
  fstAprFlag= wp.itemStr("ex_apr_flag");
  if (wp.itemStr("ex_apr_flag").equals("N"))
     controlTabName = orgTabName +"_t";

  kk1 = itemKk("data_k1");
  qFrom=1;
  dataRead();
 }
// ************************************************************************
 @Override
 public void dataRead()
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
               + "a.active_code as active_code,"
               + "a.active_seq as active_seq,"
               + "a.apr_flag,"
               + "a.record_group_no,"
               + "a.record_date_sel,"
               + "a.pur_date_sel,"
               + "a.purchase_date_s,"
               + "a.purchase_date_e,"
               + "a.week_cond,"
               + "'' as week_week_cnt,"
               + "a.month_cond,"
               + "'' as month_month_cnt,"
               + "a.cap_sel,"
               + "a.bl_cond,"
               + "a.ca_cond,"
               + "a.it_cond,"
               + "a.id_cond,"
               + "a.ao_cond,"
               + "a.ot_cond,"
               + "a.purchase_type_sel,"
               + "a.per_amt_cond,"
               + "a.per_amt,"
               + "a.perday_cnt_cond,"
               + "a.perday_cnt,"
               + "a.sum_amt_cond,"
               + "a.sum_amt,"
               + "a.sum_cnt_cond,"
               + "a.sum_cnt,"
               + "a.above_cond,"
               + "a.above_amt,"
               + "a.above_cnt,"
               + "a.max_cnt_cond,"
               + "a.max_cnt,"
               + "a.threshold_sel,"
               + "a.purchase_amt_s1,"
               + "a.purchase_amt_e1,"
               + "a.active_type_1,"
               + "a.feedback_rate_1,"
               + "a.feedback_amt_1,"
               + "a.feedback_lmt_cnt_1,"
               + "a.feedback_lmt_amt_1,"
               + "a.purchase_amt_s2,"
               + "a.purchase_amt_e2,"
               + "a.active_type_2,"
               + "a.feedback_rate_2,"
               + "a.feedback_amt_2,"
               + "a.feedback_lmt_cnt_2,"
               + "a.feedback_lmt_amt_2,"
               + "a.purchase_amt_s3,"
               + "a.purchase_amt_e3,"
               + "a.active_type_3,"
               + "a.feedback_rate_3,"
               + "a.feedback_amt_3,"
               + "a.feedback_lmt_cnt_3,"
               + "a.feedback_lmt_amt_3,"
               + "a.purchase_amt_s4,"
               + "a.purchase_amt_e4,"
               + "a.active_type_4,"
               + "a.feedback_rate_4,"
               + "a.feedback_amt_4,"
               + "a.feedback_lmt_cnt_4,"
               + "a.feedback_lmt_amt_4,"
               + "a.purchase_amt_s5,"
               + "a.purchase_amt_e5,"
               + "a.active_type_5,"
               + "a.feedback_rate_5,"
               + "a.feedback_amt_5,"
               + "a.feedback_lmt_cnt_5,"
               + "a.feedback_lmt_amt_5,"
               + "a.b_feedback_limit,"
               + "a.f_feedback_limit,"
               + "a.s_feedback_limit,"
               + "a.l_feedback_limit,"
               + "a.b_feedback_cnt_limit,"
               + "a.f_feedback_cnt_limit,"
               + "a.s_feedback_cnt_limit,"
               + "a.l_feedback_cnt_limit,"
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
                   + sqlCol(km1, "a.active_code")
                   + sqlCol(km2, "a.active_seq")
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
  km1 = wp.colStr("active_code");
  km2 = wp.colStr("active_seq");
  listWkdata();
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 void listWkdataAft()
 {
  wp.colSet("week_week_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"5"));
  wp.colSet("month_month_cnt" , listMktBnData("mkt_bn_data_t","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"6"));
 }
// ************************************************************************
 void listWkdata()
 {
  wp.colSet("week_week_cnt" , listMktBnData("mkt_bn_data","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"5"));
  wp.colSet("month_month_cnt" , listMktBnData("mkt_bn_data","MKT_CHANREC_PARM",wp.colStr("active_code")+wp.colStr("active_seq"),"6"));
 }
// ************************************************************************
 public void dataReadR3R()
 {
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.active_code as active_code,"
               + "a.active_seq as active_seq,"
               + "a.apr_flag as apr_flag,"
               + "a.record_group_no as record_group_no,"
               + "a.record_date_sel as record_date_sel,"
               + "a.pur_date_sel as pur_date_sel,"
               + "a.purchase_date_s as purchase_date_s,"
               + "a.purchase_date_e as purchase_date_e,"
               + "a.week_cond as week_cond,"
               + "'' as week_week_cnt,"
               + "a.month_cond as month_cond,"
               + "'' as month_month_cnt,"
               + "a.cap_sel as cap_sel,"
               + "a.bl_cond as bl_cond,"
               + "a.ca_cond as ca_cond,"
               + "a.it_cond as it_cond,"
               + "a.id_cond as id_cond,"
               + "a.ao_cond as ao_cond,"
               + "a.ot_cond as ot_cond,"
               + "a.purchase_type_sel as purchase_type_sel,"
               + "a.per_amt_cond as per_amt_cond,"
               + "a.per_amt as per_amt,"
               + "a.perday_cnt_cond as perday_cnt_cond,"
               + "a.perday_cnt as perday_cnt,"
               + "a.sum_amt_cond as sum_amt_cond,"
               + "a.sum_amt as sum_amt,"
               + "a.sum_cnt_cond as sum_cnt_cond,"
               + "a.sum_cnt as sum_cnt,"
               + "a.above_cond as above_cond,"
               + "a.above_amt as above_amt,"
               + "a.above_cnt as above_cnt,"
               + "a.max_cnt_cond as max_cnt_cond,"
               + "a.max_cnt as max_cnt,"
               + "a.threshold_sel as threshold_sel,"
               + "a.purchase_amt_s1 as purchase_amt_s1,"
               + "a.purchase_amt_e1 as purchase_amt_e1,"
               + "a.active_type_1 as active_type_1,"
               + "a.feedback_rate_1 as feedback_rate_1,"
               + "a.feedback_amt_1 as feedback_amt_1,"
               + "a.feedback_lmt_cnt_1 as feedback_lmt_cnt_1,"
               + "a.feedback_lmt_amt_1 as feedback_lmt_amt_1,"
               + "a.purchase_amt_s2 as purchase_amt_s2,"
               + "a.purchase_amt_e2 as purchase_amt_e2,"
               + "a.active_type_2 as active_type_2,"
               + "a.feedback_rate_2 as feedback_rate_2,"
               + "a.feedback_amt_2 as feedback_amt_2,"
               + "a.feedback_lmt_cnt_2 as feedback_lmt_cnt_2,"
               + "a.feedback_lmt_amt_2 as feedback_lmt_amt_2,"
               + "a.purchase_amt_s3 as purchase_amt_s3,"
               + "a.purchase_amt_e3 as purchase_amt_e3,"
               + "a.active_type_3 as active_type_3,"
               + "a.feedback_rate_3 as feedback_rate_3,"
               + "a.feedback_amt_3 as feedback_amt_3,"
               + "a.feedback_lmt_cnt_3 as feedback_lmt_cnt_3,"
               + "a.feedback_lmt_amt_3 as feedback_lmt_amt_3,"
               + "a.purchase_amt_s4 as purchase_amt_s4,"
               + "a.purchase_amt_e4 as purchase_amt_e4,"
               + "a.active_type_4 as active_type_4,"
               + "a.feedback_rate_4 as feedback_rate_4,"
               + "a.feedback_amt_4 as feedback_amt_4,"
               + "a.feedback_lmt_cnt_4 as feedback_lmt_cnt_4,"
               + "a.feedback_lmt_amt_4 as feedback_lmt_amt_4,"
               + "a.purchase_amt_s5 as purchase_amt_s5,"
               + "a.purchase_amt_e5 as purchase_amt_e5,"
               + "a.active_type_5 as active_type_5,"
               + "a.feedback_rate_5 as feedback_rate_5,"
               + "a.feedback_amt_5 as feedback_amt_5,"
               + "a.feedback_lmt_cnt_5 as feedback_lmt_cnt_5,"
               + "a.feedback_lmt_amt_5 as feedback_lmt_amt_5,"
               + "a.b_feedback_limit as b_feedback_limit,"
               + "a.f_feedback_limit as f_feedback_limit,"
               + "a.s_feedback_limit as s_feedback_limit,"
               + "a.l_feedback_limit as l_feedback_limit,"
               + "a.b_feedback_cnt_limit as b_feedback_cnt_limit,"
               + "a.f_feedback_cnt_limit as f_feedback_cnt_limit,"
               + "a.s_feedback_cnt_limit as s_feedback_cnt_limit,"
               + "a.l_feedback_cnt_limit as l_feedback_cnt_limit,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.active_code")
              + sqlCol(km2, "a.active_seq")
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
   km1 = wp.itemStr("active_code");
   km2 = wp.itemStr("active_seq");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      km1 = wp.itemStr("active_code");
      km2 = wp.itemStr("active_seq");
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
 public void updateFuncU3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr("active_code");
   km2 = wp.itemStr("active_seq");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr("active_code");
      km2 = wp.itemStr("active_seq");
      strAction = "A";
      wp.itemSet("aud_type","U");
      insertFunc();
      if (rc==1) dataRead();
     }
  wp.colSet("fst_apr_flag",fstAprFlag);
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

   if ((wp.itemStr("active_code").length()==0)||
       (wp.itemStr("aud_type").length()==0))
      {
       alertErr("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   commActiveCode("comm_active_code");
   this.selectNoLimit();
   if ((wp.itemStr("aud_type").equals("Y"))||
       (wp.itemStr("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "mkt_bn_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_bn_data_t";
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
               + " and table_name  =  'MKT_CHANREC_PARM' "
               ;
   if (wp.respHtml.equals("mktm0855_week"))
      wp.whereStr  += " and data_type  = '5' ";
   if (wp.respHtml.equals("mktm0855_mont"))
      wp.whereStr  += " and data_type  = '6' ";
   String whereCnt = wp.whereStr;
   whereCnt += " and  data_key = '"+wp.itemStr("active_code")+wp.itemStr("active_seq")+"'";
   wp.whereStr  += " and  data_key = :data_key ";
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key",wp.itemStr("active_code")+wp.itemStr("active_seq"));
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
   if (wp.respHtml.equals("mktm0855_week"))
    commDataCodeb("comm_data_code");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   mktm01.Mktm0855Func func =new mktm01.Mktm0855Func(wp);
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
 public int selectBndataCount(String bndataTable,String whereStr )
 {
   String sql1 = "select count(*) as bndataCount"
               + " from " + bndataTable
               + " " + whereStr
               ;

   sqlSelect(sql1);

   return((int)sqlNum("bndataCount"));
 }
// ************************************************************************
 public void saveFunc()
 {
  mktm01.Mktm0855Func func =new mktm01.Mktm0855Func(wp);

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
       if ((wp.respHtml.equals("mktm0855_nadd"))||
           (wp.respHtml.equals("mktm0855_detl")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          if (wp.colStr("active_code").length()>0)
             {
             wp.optionKey = wp.colStr("active_code");
             wp.initOption ="";
             }
          this.dddwList("dddw_active_code"
                 ,"mkt_channel_parm"
                 ,"trim(active_code)"
                 ,"trim(active_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("record_group_no").length()>0)
             {
             wp.optionKey = wp.colStr("record_group_no");
             }
          lsSql = "";
          lsSql =  procDynamicDddwRecordGp(wp.colStr("active_code"));

          dddwList("dddw_record_gp", lsSql);
         }
       if ((wp.respHtml.equals("mktm0855")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_active_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_active_code");
             }
          this.dddwList("dddw_active_code"
                 ,"mkt_channel_parm"
                 ,"trim(active_code)"
                 ,"trim(active_name)"
                 ," where record_cond='Y'");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck()
 {
/*
  String sql1 = "select record_cond "
              + "from  mkt_channel_parm "
              + "where  active_code  =  '"+ wp.item_ss("ex_active_code").toUpperCase() +"'"
              ;

  sqlSelect(sql1);
  if (sql_nrow <= 0)
     {
      err_alert(" 此活動代號[ "+wp.item_ss("ex_active_code").toUpperCase() +"] 登錄資料");
      return(1);
     }
  if (!sql_ss("record_cond").equals("Y")) 
     {
      err_alert(" 此活動代號[ "+wp.item_ss("ex_active_code").toUpperCase() +"] 非設定登錄判斷");
      return(1);
     }
 
*/
  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt)
 {
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
 public void commCrtUser(String s1) 
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
 public void commAprUser(String s1) 
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
 public void commActiveCode(String s1) 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " active_name as column_active_name "
            + " from mkt_channel_parm "
            + " where 1 = 1 "
            + " and   active_code = '"+wp.colStr(ii,"active_code")+"'"
            ;
       if (wp.colStr(ii,"active_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_active_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commRecordGroupNo(String s1) 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " record_group_name as column_record_group_name "
            + " from web_record_group "
            + " where 1 = 1 "
            + " and   record_group_no = '"+wp.colStr(ii,"record_group_no")+"'"
            ;
       if (wp.colStr(ii,"record_group_no").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_record_group_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commAprFlag2(String s1) 
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
 public void commRecordDateSel(String s1) 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"有登錄","登錄前之消費","登錄後之消費"};
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
 public void commPdateSel(String s1) 
 {
  String[] cde = {"1","2"};
  String[] txt = {"依原消費期間","一段期間"};
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
 public void commPurcTypeSel(String s1) 
 {
  String[] cde = {"1","2","3","4","5"};
  String[] txt = {"累積金額","累積筆數","日累積金額","日累積筆數","筆消費金額"};
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
 public void commThresholdSel(String s1) 
 {
  String[] cde = {"1","2"};
  String[] txt = {"級距式","條件式"};
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
 public void commDataCodeb(String s1) 
 {
  String[] cde = {"0","1","2","3","4","5","6"};
  String[] txt = {"星期日","星期一","星期二","星期>三","星期四","星期五","星期六"};
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
  String ajaxjRecordGroupNo = "";
  super.wp = wr;


  if (selectAjaxFunc20(
                    wp.itemStr("ax_win_record_group").toUpperCase())!=0) 
     {
      wp.addJSON("ajaxj_record_group_no", "");
      wp.addJSON("ajaxj_record_group_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_record_group_no", sqlStr(ii, "record_group_no"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_record_group_name", sqlStr(ii, "record_group_name"));
 }
// ************************************************************************
 int selectAjaxFunc20(String s1)
  {
   wp.sqlCmd = " select "
             + " b.record_group_no, "
             + " substr(b.record_group_name,1,30) as record_group_name "
             + " from web_record_group b "
             ;

   if (s1.length()>0)
      wp.sqlCmd = wp.sqlCmd
                + " where record_group_no like  '"+ s1 +"'||'%' ";

   wp.sqlCmd = wp.sqlCmd
             + " order by 1 "
             + " fetch first 300 row only ";

   wp.log("["+wp.sqlCmd +"]");

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("查無登錄群組資料");
       return(1);
      }
   return(0);
  }
// ************************************************************************
 public void checkButtonOff()
  {
  if (wp.colStr("week_cond").length()==0)
      wp.colSet("week_cond" , "N");

  if (wp.colStr("week_cond").equals("N"))
     {
      buttonOff("btnweek_disable");
     }
  else
     {
      wp.colSet("btnweek_disable","");
     }

  if (wp.colStr("month_cond").length()==0)
      wp.colSet("month_cond" , "N");

  if (wp.colStr("month_cond").equals("N"))
     {
      buttonOff("btnmont_disable");
     }
  else
     {
      wp.colSet("btnmont_disable","");
     }

  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
   if (wp.respHtml.equals("mktm0855_nadd"))
      {
       wp.colSet("purchase_type_sel" , "1");
      }


  buttonOff("btnweek_disable");
  buttonOff("btnmont_disable");
  return;
 }
// ************************************************************************
 public void funcSelect()
 {
  return;
 }
// ************************************************************************
// ************************************************************************
 String  listMktBnData(String s1,String s2,String s3,String s4)
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
 String procDynamicDddwRecordGp(String s1) 
 {
   String lsSql = "";

   lsSql = " select "
          + " record_group_no as db_code, "
          + " record_group_no||'~'||substr(record_group_name,1,30)  as db_desc "
          + " from web_record_group "
          + " where (active_date_e >= to_char(sysdate,'yyyymmdd') "
          + "  or   record_group_no in ( "
          + "       select distinct record_group_no from mkt_chanrec_parm "
          + "       where    active_code = '" + s1 +"')) "
          + " order by record_group_no "
          ;
   return lsSql;
 }

// ************************************************************************

}  // End of class
