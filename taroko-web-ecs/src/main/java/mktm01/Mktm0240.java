/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/09  V1.00.01   Allen Ho      Initial                              *
* 111/12/05  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0240 extends BaseEdit
{
 private final String PROGNAME = "紅利贈品資料檔維護作業處理程式111/12/05  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0240Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_gift";
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
              + sqlCol(wp.itemStr("ex_gift_no"), "a.gift_no", "like%")
              + sqlChkEx(wp.itemStr("ex_gift_name"), "1", "")
              + sqlCol(wp.itemStr("ex_air_type"), "a.air_type")
              + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
              + sqlCol(wp.itemStr("ex_disable_flag"), "a.disable_flag")
              + sqlCol(wp.itemStr("ex_gift_typeno"), "a.gift_typeno")
              + sqlCol(wp.itemStr("ex_gift_type"), "a.gift_type")
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
               + "a.gift_name,"
               + "a.gift_type,"
               + "a.cash_value,"
               + "decode(gift_type,'3',max_limit_count,SUPPLY_COUNT) as supply_count,"
               + "decode(gift_type,'3',use_limit_count,USE_COUNT) as use_count,"
               + "a.web_sumcnt,"
               + "decode(gift_type,'3',max_limit_count-use_limit_count-web_sumcnt,supply_count-use_count-web_sumcnt) as net_count,"
               + "a.air_type,"
               + "a.disable_flag,"
               + "a.vendor_no";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by disable_flag,gift_no"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commAirType("comm_air_type");

  commGiftType("comm_gift_type");
  commDisableFlag("comm_disable_flag");

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
               + "a.gift_no as gift_no,"
               + "a.apr_flag,"
               + "a.bonus_type,"
               + "a.gift_typeno,"
               + "a.gift_name,"
               + "a.disable_flag,"
               + "a.cash_value,"
               + "a.gift_type,"
               + "a.list_price,"
               + "a.effect_months,"
               + "a.redem_days,"
               + "a.max_limit_count,"
               + "a.use_limit_count,"
               + "decode(gift_type,'3',max_limit_count-use_limit_count-web_sumcnt,0) as net_limit_count,"
               + "a.fund_code,"
               + "a.air_type,"
               + "a.cal_mile,"
               + "a.supply_count,"
               + "a.use_count,"
               + "decode(gift_type,'3',0,supply_count-use_count-web_sumcnt) as checkt_count,"
               + "a.web_sumcnt,"
               + "a.limit_last_date,"
               + "a.vendor_no,"
               + "'' as exchg_cnt,"
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
  commAprFlag2("comm_apr_flag");
  commBonusType("comm_bonus_type");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("gift_no");
  listWkdata();
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("exchg_cnt" , listMktGiftExchgdata("mkt_gift_exchgdata_t","",wp.colStr("gift_no"),""));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  wp.colSet("exchg_cnt" , listMktGiftExchgdata("mkt_gift_exchgdata","",wp.colStr("gift_no"),""));
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.gift_no as gift_no,"
               + "a.apr_flag as apr_flag,"
               + "a.bonus_type as bonus_type,"
               + "a.gift_typeno as gift_typeno,"
               + "a.gift_name as gift_name,"
               + "a.disable_flag as disable_flag,"
               + "a.cash_value as cash_value,"
               + "a.gift_type as gift_type,"
               + "a.list_price as list_price,"
               + "a.effect_months as effect_months,"
               + "a.redem_days as redem_days,"
               + "a.max_limit_count as max_limit_count,"
               + "a.use_limit_count as use_limit_count,"
               + "decode(gift_type,'3',max_limit_count-use_limit_count-web_sumcnt,0) as net_limit_count,"
               + "a.fund_code as fund_code,"
               + "a.air_type as air_type,"
               + "a.cal_mile as cal_mile,"
               + "a.supply_count as supply_count,"
               + "a.use_count as use_count,"
               + "decode(gift_type,'3',0,supply_count-use_count-web_sumcnt) as checkt_count,"
               + "a.web_sumcnt as web_sumcnt,"
               + "a.limit_last_date as limit_last_date,"
               + "a.vendor_no as vendor_no,"
               + "'' as exchg_cnt,"
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
  commAprFlag2("comm_apr_flag");
  commBonusType("comm_bonus_type");
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
 public void updateFuncU3R()  throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr("gift_no");
  fstAprFlag = wp.itemStr("fst_apr_flag");
  if (!wp.itemStr("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
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
 public void dataReadR3() throws Exception
 {
  dataReadR3(0);
 }
// ************************************************************************
 public void dataReadR3(int fromType) throws Exception
 {
   String bnTable="";

   if ((wp.itemStr("gift_no").length()==0)||
       (wp.itemStr("aud_type").length()==0))
      {
       alertErr2("鍵值為空白或主檔未新增 ");
       return;
      }
   wp.selectCnt=1;
   this.selectNoLimit();
   if ((wp.itemStr("aud_type").equals("Y"))||
       (wp.itemStr("aud_type").equals("D")))
      {
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       bnTable = "mkt_gift_exchgdata";
      }
   else
      {
       wp.colSet("btnUpdate_disable","");
       wp.colSet("newDetail_disable","");
       bnTable = "mkt_gift_exchgdata_t";
      }

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "gift_no, "
                + "card_note, "
                + "group_code, "
                + "exchange_bp, "
                + "exchange_amt, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
               ;
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  gift_no = :gift_no ";
   setString("gift_no", wp.itemStr("gift_no"));
   whereCnt += " and  gift_no = '"+ wp.itemStr("gift_no") +  "'";
   wp.whereStr  += " order by 4,5,6,7,8,9 ";
   int cnt1= selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr2("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm0240_mgex"))
    commCardNote("comm_card_note");
   if (wp.respHtml.equals("mktm0240_mgex"))
    commGroupCode("comm_group_code");
  }
// ************************************************************************
 public void updateFuncU3() throws Exception
 {
   Mktm0240Func func =new Mktm0240Func(wp);
   int llOk = 0, llErr = 0;

   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("card_note");
   String[] key2Data = wp.itemBuff("group_code");
   String[] key3Data = wp.itemBuff("exchange_bp");
   String[] key4Data = wp.itemBuff("exchange_amt");

   wp.listCount[0] = key1Data.length;
   wp.colSet("IND_NUM", "" + key1Data.length);
   //-check duplication-

   int del2Flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       del2Flag=0;
       wp.colSet(ll, "ok_flag", "");

       if (key3Data[ll].length()==0) key3Data[ll]="0";
       if (key4Data[ll].length()==0) key4Data[ll]="0";
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
                  (empty(key2Data[ll])) &&
                  (empty(key3Data[ll])) &&
              (empty(key4Data[ll])))
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

       func.varsSet("card_note", key1Data[ll]); 
       func.varsSet("group_code", key2Data[ll]); 
       func.varsSet("exchange_bp", key3Data[ll]); 
       func.varsSet("exchange_amt", key4Data[ll]); 

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
  Mktm0240Func func =new Mktm0240Func(wp);

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
  String lsSql ="";
  try {
       if ((wp.respHtml.equals("mktm0240_nadd"))||
           (wp.respHtml.equals("mktm0240_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("bonus_type").length()>0)
             {
             wp.optionKey = wp.colStr("bonus_type");
             wp.initOption ="";
             }
          this.dddwList("dddw_bonus_type_b"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='BONUS_NAME'");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("gift_typeno").length()>0)
             {
             wp.optionKey = wp.colStr("gift_typeno");
             }
          this.dddwList("dddw_gift_typeno"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='GIFT_TYPENO' and WF_USEREDIT='Y' ");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("fund_code").length()>0)
             {
             wp.optionKey = wp.colStr("fund_code");
             }
          this.dddwList("dddw_fund_nameb"
                 ,"ptr_fundp"
                 ,"trim(fund_code)"
                 ,"trim(fund_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("air_type").length()>0)
             {
             wp.optionKey = wp.colStr("air_type");
             }
          this.dddwList("dddw_air_type"
                 ,"mkt_air_parm"
                 ,"trim(air_type)"
                 ,"trim(air_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("vendor_no").length()>0)
             {
             wp.optionKey = wp.colStr("vendor_no");
             }
          this.dddwList("daddw_vendor_no"
                 ,"mkt_vendor"
                 ,"trim(vendor_no)"
                 ,"trim(vendor_name)"
                 ," where disable_flag!='Y'");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("vendor_no").length()>0)
             {
             wp.optionKey = wp.colStr("vendor_no");
             }
//          this.dddwList("daddw_vendor_no2"
//                 ,"mkt_vendor"
//                 ,"trim(vendor_no)"
//                 ,"trim(vendor_name)"
//                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("mktm0240")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_air_type").length()>0)
             {
             wp.optionKey = wp.colStr("ex_air_type");
             }
          this.dddwList("dddw_air_type_b"
                 ,"mkt_air_parm"
                 ,"trim(air_type)"
                 ,"trim(air_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_gift_typeno").length()>0)
             {
             wp.optionKey = wp.colStr("ex_gift_typeno");
             }
          this.dddwList("dddw_gift_typeno"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='GIFT_TYPENO'");
         }
       if ((wp.respHtml.equals("mktm0240_mgex")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_lost_code4"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type= 'CARD_NOTE'");
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_group_code3"
                 ,"ptr_group_code"
                 ,"trim(group_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public String sqlChkEx(String exCol, String sqCond, String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
      return " and gift_name like '%"+wp.itemStr("ex_gift_name")+"%' ";

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
 public void commAirType(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " air_name as column_air_name "
            + " from mkt_air_parm "
            + " where 1 = 1 "
            + " and   air_type = '"+wp.colStr(ii,"air_TYPE")+"'"
            ;
       if (wp.colStr(ii,"air_TYPE").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_air_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commCardNote(String s1) throws Exception 
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
            + " and   wf_id = '"+wp.colStr(ii,"card_note")+"'"
            + " and   wf_type = 'CARD_NOTE' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       else {
    	   if("V".equals(wp.colStr(ii,"card_note"))){
    		   columnData = columnData + "VD_金融卡"; 
    	   }
       }
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
            + " and   group_code = '"+wp.colStr(ii,"group_code")+"'"
            ;
       if (wp.colStr(ii,"group_code").length()==0)
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
 public void wfAjaxFunc4(TarokoCommon wr) throws Exception
 {
  String ajaxjGroupCode = "";
  super.wp = wr;


  if (selectAjaxFunc40(
                    wp.itemStr("ax_win_card_note"))!=0) 
     {
      wp.addJSON("ajaxj_group_code", "");
      wp.addJSON("ajaxj_name", "");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_group_code", sqlStr(ii, "group_code"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_name", sqlStr(ii, "name"));
 }
// ************************************************************************
 int selectAjaxFunc40(String s1) throws Exception
  {
   if (s1.length()==0) 
      {
       wp.sqlCmd = " select "
                 + " c.group_code,"
                 + " group_name as name "
                 + " from  ptr_group_code c "
                 + " order by  c.group_code  "
                 ;
      }  
   else
      {
       wp.sqlCmd = " select "
                 + " '' as group_code,  "
                 + " '' as name  "
                 + " from  ptr_businday "
                 + " union "
                 + " select "
                 + " c.group_code,"
                 + " max(group_name) as name "
                 + " from  ptr_group_card a,ptr_card_type b,ptr_group_code c "
                 + " where card_note    = '"+ s1 +"' "
                 + " and   a.card_type  = b.card_type  "
                 + " and   a.group_code = c.group_code  "
                 + " group by  c.group_code  "
                 + " order by  1  "
                 ;
      }  

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("卡片等級:["+s1+"]查無資料");
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
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
// ************************************************************************
  String listMktGiftExchgdata(String s1, String s2, String s3, String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where 1 = 1 "
              + " and   gift_no = '"+s3+"'"
              ;
  wp.log("@@@@@@@@@@@@["+sql1 +"]");
  sqlSelect(sql1);

  if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

   return("0");
 }
// ************************************************************************
 
// ************************************************************************

}  // End of class
