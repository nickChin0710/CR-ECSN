/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/12  V1.00.02   Allen Ho      Initial                              *
* 111/12/08  V1.00.02   Yang Bo       update naming rule                   *
***************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0080 extends BaseEdit
{
 private final String PROGNAME = "紅利利率轉換檔維護作業處理程式111/12/08  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktm0080Func func = null;
  String kk1,kk2,kk3,kk4;
  String km1,km2,km3,km4;
  String fstAprFlag = "";
  String orgTabName = "cyc_bpid";
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
              + sqlCol(wp.itemStr("ex_years"), "a.years", "like%")
              + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%")
              + sqlCol(wp.itemStr("ex_item_code"), "a.item_code", "like%")
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
               + "a.years,"
               + "a.bonus_type,"
               + "a.acct_type,"
               + "a.item_code,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.years desc,a.bonus_type,a.acct_type,a.item_code"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commBonusType("comm_bonus_type");
  commAcctType("comm_acct_type");
  commCrtUser("comm_crt_user");

  commItemCode("comm_item_code");

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
               + "a.years as years,"
               + "a.bonus_type as bonus_type,"
               + "a.acct_type as acct_type,"
               + "a.item_code as item_code,"
               + "a.apr_flag,"
               + "a.other_item,"
               + "a.effect_months,"
               + "a.merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.group_card_sel,"
               + "'' as group_card_sel_cnt,"
               + "a.group_merchant_sel,"
               + "'' as group_merchant_sel_cnt,"
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
               + "a.exchange_6,"
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
  commItemCode("comm_item_code");
  commBonusType1("comm_bonus_type");
  commAcctType("comm_acct_type");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("years");
  km2 = wp.colStr("bonus_type");
  km3 = wp.colStr("acct_type");
  km4 = wp.colStr("item_code");
  listWkdata();
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("merchant_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"1"));
  wp.colSet("mcht_group_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"4"));
  wp.colSet("group_card_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"2"));
  wp.colSet("group_merchant_sel_cnt" , listCycBnData("cyc_bn_data_t","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"3"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  wp.colSet("merchant_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"1"));
  wp.colSet("mcht_group_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"4"));
  wp.colSet("group_card_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"2"));
  wp.colSet("group_merchant_sel_cnt" , listCycBnData("cyc_bn_data","CYC_BPID",wp.colStr("years")+wp.colStr("bonus_type")+wp.colStr("acct_type")+wp.colStr("item_code"),"3"));
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.years as years,"
               + "a.bonus_type as bonus_type,"
               + "a.acct_type as acct_type,"
               + "a.item_code as item_code,"
               + "a.apr_flag as apr_flag,"
               + "a.other_item as other_item,"
               + "a.effect_months as effect_months,"
               + "a.merchant_sel as merchant_sel,"
               + "'' as merchant_sel_cnt,"
               + "a.mcht_group_sel as mcht_group_sel,"
               + "'' as mcht_group_sel_cnt,"
               + "a.group_card_sel as group_card_sel,"
               + "'' as group_card_sel_cnt,"
               + "a.group_merchant_sel as group_merchant_sel,"
               + "'' as group_merchant_sel_cnt,"
               + "a.limit_1_beg as limit_1_beg,"
               + "a.limit_1_end as limit_1_end,"
               + "a.exchange_1 as exchange_1,"
               + "a.limit_2_beg as limit_2_beg,"
               + "a.limit_2_end as limit_2_end,"
               + "a.exchange_2 as exchange_2,"
               + "a.limit_3_beg as limit_3_beg,"
               + "a.limit_3_end as limit_3_end,"
               + "a.exchange_3 as exchange_3,"
               + "a.limit_4_beg as limit_4_beg,"
               + "a.limit_4_end as limit_4_end,"
               + "a.exchange_4 as exchange_4,"
               + "a.limit_5_beg as limit_5_beg,"
               + "a.limit_5_end as limit_5_end,"
               + "a.exchange_5 as exchange_5,"
               + "a.limit_6_beg as limit_6_beg,"
               + "a.limit_6_end as limit_6_end,"
               + "a.exchange_6 as exchange_6,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

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

  if (wp.respHtml.indexOf("_detl") > 0) 
     wp.colSet("btnStore_disable","");   
  commAprFlag2("comm_apr_flag");
  commBonusType1("comm_bonus_type");
  commAcctType("comm_acct_type");
  commItemCode("comm_item_code");
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
   km1 = wp.itemStr("years");
   km2 = wp.itemStr("bonus_type");
   km3 = wp.itemStr("acct_type");
   km4 = wp.itemStr("item_code");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      km1 = wp.itemStr("years");
      km2 = wp.itemStr("bonus_type");
      km3 = wp.itemStr("acct_type");
      km4 = wp.itemStr("item_code");
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
   km1 = wp.itemStr("years");
   km2 = wp.itemStr("bonus_type");
   km3 = wp.itemStr("acct_type");
   km4 = wp.itemStr("item_code");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr("years");
      km2 = wp.itemStr("bonus_type");
      km3 = wp.itemStr("acct_type");
      km4 = wp.itemStr("item_code");
      strAction = "A";
      wp.itemSet("aud_type","U");
      insertFunc();
      if (rc==1) dataRead();
     }
  wp.colSet("fst_apr_flag", fstAprFlag);
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

   if ((wp.itemStr("years").length()==0)||
       (wp.itemStr("aud_type").length()==0))
      {
       alertErr2("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   commBonusType("comm_bonus_type");
   commAcctType("comm_acct_type");
   commItemCode("comm_item_code");
   this.selectNoLimit();
   if ((wp.itemStr("aud_type").equals("Y"))||
       (wp.itemStr("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "cyc_bn_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "cyc_bn_data_t";
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
              + " and table_name  =  'CYC_BPID' "
                ;
   if (wp.respHtml.equals("mktm0080_mccd"))
      wp.whereStr  += " and data_type  = '1' ";
   if (wp.respHtml.equals("mktm0080_gpcd"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktm0080_gpmc"))
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
       alertErr2("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm0080_gpcd"))
    commDataCode04("comm_data_code");
   if (wp.respHtml.equals("mktm0080_gpcd"))
    commDataCode02("comm_data_code2");
   if (wp.respHtml.equals("mktm0080_gpmc"))
    commDataCode04("comm_data_code");
  }
// ************************************************************************
 public void updateFuncU3() throws Exception
 {
   Mktm0080Func func =new Mktm0080Func(wp);
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
 public void dataReadR2() throws Exception
 {
  dataReadR2(0);
 }
// ************************************************************************
 public void dataReadR2(int fromType) throws Exception
 {
   String bnTable="";

   if ((wp.itemStr("years").length()==0)||
       (wp.itemStr("aud_type").length()==0))
      {
       alertErr2("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   commBonusType("comm_bonus_type");
   commAcctType("comm_acct_type");
   commItemCode("comm_item_code");
   this.selectNoLimit();
   if ((wp.itemStr("aud_type").equals("Y"))||
       (wp.itemStr("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "cyc_bn_data";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "cyc_bn_data_t";
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
              + " and table_name  =  'CYC_BPID' "
                ;
   if (wp.respHtml.equals("mktm0080_mcgp"))
      wp.whereStr  += " and data_type  = '4' ";
   String whereCnt = wp.whereStr;
   whereCnt += " and  data_key = '"+wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code")+"'";
   wp.whereStr  += " and  data_key = :data_key ";
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key",wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code"));
   wp.whereStr  += " order by 4,5 ";
   int cnt1= selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr2("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm0080_mcgp"))
    commDataCode34("comm_data_code");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   Mktm0080Func func =new Mktm0080Func(wp);
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
   int delete_flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       delete_flag=0;
       //KEY 不可同時為空字串
       if ((empty(key1Data[ll])))
           continue;

       //-option-ON-
       for (int intm=0;intm<optData.length;intm++)
         {
          if (optData[intm].length()!=0)
          if ((ll+1)==Integer.valueOf(optData[intm]))
             {
              delete_flag=1;
              break;
             }
          }
       if (delete_flag==1) continue;

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
 public void saveFunc() throws Exception
 {
  Mktm0080Func func =new Mktm0080Func(wp);

  if (wp.respHtml.indexOf("_detl") > 0)
     if (!wp.colStr("aud_type").equals("Y")) listWkdataAft();

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr2(func.getMsg());
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
  String ls_sql ="";
  try {
       if ((wp.respHtml.equals("mktm0080_nadd"))||
           (wp.respHtml.equals("mktm0080_detl")))
         {
          wp.initOption ="";
          if (wp.colStr("bonus_type").length()>0)
             {
             wp.optionKey = wp.colStr("bonus_type");
             }
          this.dddwList("dddw_bonus_type"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='BONUS_NAME'");
          if (wp.colStr("acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("acct_type");
             wp.initOption ="";
             }
          this.dddwList("dddw_acct_type"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0080")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("ex_acct_type");
             }
          this.dddwList("dddw_acct_type1"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0080_gpcd")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_card_type"
                 ,"ptr_card_type"
                 ,"trim(card_type)"
                 ,"trim(name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0080_gpmc")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0080_mcgp")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_data_Code34"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 ");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public String sqlChkEx(String exCol, String sqCond, String fileExt) throws Exception
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
       if (wp.colStr(ii,"acct_type").length()==0)
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
 public void commBonusType(String s1) throws Exception 
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
 public void commDataCode04(String s1) throws Exception 
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
 public void commDataCode02(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " name as column_name "
            + " from ptr_card_type "
            + " where 1 = 1 "
            + " and   card_type = '"+wp.colStr(ii,"data_code2")+"'"
            ;
       if (wp.colStr(ii,"data_code2").length()==0)
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
 public void commDataCode34(String s1) throws Exception 
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
 public void commItemCode(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"除預借現金以外之消費款","預借現金","循環利息"};
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
 public void wfAjaxFunc4(TarokoCommon wr) throws Exception
 {
  String ajaxjDataCode2 = "";
  super.wp = wr;


  if (selectAjaxFunc40(
                    wp.itemStr("ax_win_data_code"))!=0) 
     {
      wp.addJSON("ajaxj_data_code2", "");
      wp.addJSON("ajaxj_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_data_code2", sqlStr(ii, "data_code2"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_name", sqlStr(ii, "name"));
 }
// ************************************************************************
 int selectAjaxFunc40(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " '' as data_code2,  "
             + " '' as name  "
             + " from  ptr_businday "
             + " union "
             + " select "
             + " b.card_type  as data_code2,"
             + " b.name "
             + " from  ptr_group_card a,ptr_card_type b "
             + " where a.card_type  = b.card_type "
             + " and   a.group_code = '"+ s1 +"' "
             ;

   this.sqlSelect();

   if (sqlRowNum<=0)
      {
       alertErr2("團體代號:["+s1+"]查無資料");
       return 1;
      }

   return 0;
 }

// ************************************************************************
public void procUploadFile(int loadType) throws Exception
 {
  if (wp.colStr(0,"ser_num").length()>0)
     wp.listCount[0] = wp.itemBuff("ser_num").length;
  if (wp.itemStr("zz_file_name").indexOf(".xls")!=-1) 
     {
      alertErr2("上傳格式: 不可為 excel 格式");
      return;
     }
  if (itemIsempty("zz_file_name"))
     {
      alertErr2("上傳檔名: 不可空白");
      return;
     }

  if (loadType==2) fileDataImp2();
 }
// ************************************************************************
int fileUpload()
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

  String inputFile = wp.itemStr("zz_file_name");
  int fi = tf.openInputText(inputFile,"MS950");

  if (fi == -1) return;

  String sysUploadType  = wp.itemStr("sys_upload_type");
  String sysUploadAlias = wp.itemStr("sys_upload_alias");

  Mktm0080Func func =new Mktm0080Func(wp);

  if (sysUploadAlias.equals("aaa1"))
     {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Aaa1("CYC_BN_DATA_T");
     }

  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  tranSeqStr = comr.getSeqno("MKT_MODSEQ");

  String ss="";
  int llOk=0, llCnt=0,llErr=0,llChkErr=0;
  int lineCnt =0;
  while (true)
   {
    ss = tf.readTextFile(fi);
    if (tf.endFile[fi].equals("Y")) break;
    errorCnt =0;
    lineCnt++;
    if (sysUploadAlias.equals("aaa1"))
       {
        if (lineCnt<=0) continue;
        if (ss.length() < 2) continue;
       }

    llCnt++; 

    for (int inti=0;inti<10;inti++) logMsg[inti]="";
    logMsg[10]=String.format("%02d",lineCnt);

    if (sysUploadAlias.equals("aaa1"))
       if (checkUploadfileAaa1(ss)!=0) continue;

   if (errorCnt ==0)
      {
       if (sysUploadAlias.equals("aaa1"))
          {
           if (func.dbInsertI2Aaa1("CYC_BN_DATA_T",uploadFileCol,uploadFileDat) == 1) llOk++;
           else llErr++;
          }
      }
    else llChkErr++;
   }

  if (llErr+llChkErr>0)
     {
      if (sysUploadAlias.equals("aaa1"))
         func.dbDeleteD2Aaa1("CYC_BN_DATA_T");
      func.dbInsertEcsNotifyLog(tranSeqStr,(llErr+llChkErr));
     }

  sqlCommit(1);  // 1:commit else rollback

  alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 檢核失敗(" + llChkErr + "),累計失敗(" + (llErr+llChkErr) + ")");

  tf.closeInputText(fi);
  tf.deleteFile(inputFile);


  return;
 }
// ************************************************************************
int checkUploadfileAaa1(String ss) throws Exception
 {
  Mktm0080Func func =new Mktm0080Func(wp);

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

  // ==== insert table content default =====
  uploadFileDat[2]  = "CYC_BPID";
  uploadFileDat[3]  = wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code");
  uploadFileDat[4]  = "1";

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

  if (uploadFileDat[1].length()!=0)
      uploadFileDat[1] = "00000000".substring(0,8-uploadFileDat[1].length())
                       + uploadFileDat[1];


  return 0;
 }
// ************************************************************************
// ************************************************************************
 public void checkButtonOff() throws Exception
  {
  if (wp.colStr("merchant_sel").length()==0)
      wp.colSet("merchant_sel" , "0");

  if (wp.colStr("merchant_sel").equals("0"))
     {
      buttonOff("btnmccd_disable");
      buttonOff("uplaaa1_disable");
     }
  else
     {
      wp.colSet("btnmccd_disable","");
      wp.colSet("uplaaa1_disable","");
     }

  if (wp.colStr("mcht_group_sel").length()==0)
      wp.colSet("mcht_group_sel" , "0");

  if (wp.colStr("mcht_group_sel").equals("0"))
     {
      buttonOff("btnmcgp_disable");
     }
  else
     {
      wp.colSet("btnmcgp_disable","");
     }

  if (wp.colStr("group_card_sel").length()==0)
      wp.colSet("group_card_sel" , "0");

  if (wp.colStr("group_card_sel").equals("0"))
     {
      buttonOff("btngpcd_disable");
     }
  else
     {
      wp.colSet("btngpcd_disable","");
     }

  if (wp.colStr("group_merchant_sel").length()==0)
      wp.colSet("group_merchant_sel" , "0");

  if (wp.colStr("group_merchant_sel").equals("0"))
     {
      buttonOff("btngpmc_disable");
     }
  else
     {
      wp.colSet("btngpmc_disable","");
     }

  if ((wp.colStr("aud_type").equals("Y"))||
      (wp.colStr("aud_type").equals("D")))
     {
      buttonOff("uplaaa1_disable");
     }
  else
     {
      wp.colSet("uplaaa1_disable","");
     }
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
/* 20210105 企劃部--賴俐吟副組長 
   if (wp.respHtml.equals("mktm0080"))
      {
       wp.colSet("ex_apr_flag" , "N");
      }
*/

  buttonOff("btnmccd_disable");
  buttonOff("btnmcgp_disable");
  buttonOff("btngpcd_disable");
  buttonOff("btngpmc_disable");
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

}  // End of class
