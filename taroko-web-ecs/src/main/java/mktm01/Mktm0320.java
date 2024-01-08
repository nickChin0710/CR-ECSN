/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/09/16  V1.01.04   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
* 111-12-16  V1.00.02  Machao         修改checkApprove欄位名不匹配                                                                         *
***************************************************************************/
package mktm01;

import mktm01.Mktm0320Func;
import ofcapp.AppMsg;
import java.util.Arrays;

import busi.ecs.DbmBonus;
import busi.ecs.MktBonus;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0320 extends BaseEdit
{
 private final String PROGNAME = "紅利媒體轉入參數檔維護處理程式111-11-30  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0320Func func = null;
  String kk1;
  String orgTabName = "mkt_transbp_parm";
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
  else if (eqIgno(wp.buttonCode, "procMethod_ABEN"))
     {/* 媒體作廢 */
      strAction = "U";
      procMethodABEN();
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
              + sqlCol(wp.itemStr2("ex_active_code"), "a.active_code", "like%")
              + sqlChkEx(wp.itemStr2("ex_active_name"), "1", "")
              + sqlCol(wp.itemStr2("ex_trans_type"), "a.trans_type")
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
               + "a.active_code,"
               + "a.trans_type,"
               + "a.active_name,"
               + "a.crt_user,"
               + "a.crt_date,"
               + "a.apr_user,"
               + "a.apr_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.active_code,a.trans_type"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

  commTransType("comm_trans_type");

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
  if (wp.itemStr2("kk_active_code").length()==0)
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
               + "a.active_code as active_code,"
               + "a.active_name,"
               + "a.trans_type,"
               + "a.effect_months,"
               + "a.map_bp,"
               + "a.min_pt,"
               + "a.tax_flag,"
               + "a.bp_amt,"
               + "a.withhold_code,"
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
                   + sqlCol(wp.itemStr2("kk_active_code"), "a.active_code")
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
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
 }
// ************************************************************************
 public void dataReadR4() throws Exception
 {
  if (wp.colStr("active_code").length()==0)
     {
      wp.colSet("active_code",itemKk("data_k3"));
      wp.itemSet("active_code",itemKk("data_k3"));
     }
  dataReadR4(0);
 }
// ************************************************************************
 public void dataReadR4(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   this.selectNoLimit();
   bnTable = "mkt_taxpar_vd";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "program_code, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
               ;
   String whereCnt = wp.whereStr;
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
  wp.listCount[0] = wp.itemBuff("ser_num").length;
  if (!checkApprove(wp.itemStr2("zz_apr_user"), wp.itemStr2("zz_apr_passwd"))) return;
   mktm01.Mktm0320Func func =new mktm01.Mktm0320Func(wp);
   int llOk = 0, llErr = 0;


   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("program_code");

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
   if (func.dbDeleteD4() < 0)
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

       func.varsSet("program_code", key1Data[ll]); 

       if (func.dbInsertI4() == 1) llOk++;
       else llErr++;

       //有失敗rollback，無失敗commit
       sqlCommit(llOk > 0 ? 1 : 0);
      }
   alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

   //SAVE後 SELECT
   dataReadR4(1);
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
  //-check approve-
  if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) return;

  mktm01.Mktm0320Func func =new mktm01.Mktm0320Func(wp);

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
       if ((wp.respHtml.equals("mktm0320_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("withhold_code").length()>0)
             {
             wp.optionKey = wp.colStr("withhold_code");
             }
          if (wp.colStr("withhold_code").length()>0)
             {
              wp.initOption ="--";
             }
          this.dddwList("dddw_withhold_code"
                 ,"mkt_taxpar_vd"
                 ,"trim(program_code)"
                 ,"trim(chi_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("").length()>0)
             {
             wp.optionKey = wp.colStr("");
             }
          if (wp.colStr("").length()>0)
             {
              wp.initOption ="--";
             }
          this.dddwList("3"
                 ,"withhold_code-chi_name"
                        );
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
      return " and active_name like '%"+wp.itemStr2("ex_active_name")+"%' ";

  return "";
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
 public void commTransType(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"一般卡","DEBIT 金融卡"};
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
 public void wfButtonFunc3(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (selectButtonFunc3(
                       )!=0)
     {
      wp.addJSON("withhold_code","");
      wp.addJSON("chi_name","");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_withhold_code", sqlStr(ii, "withhold_code"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_chi_name", sqlStr(ii, "chi_name"));
 }
// ************************************************************************
int selectButtonFunc3()  throws Exception
 {
   wp.sqlCmd = " select "
             + "'' as withhold_code, "
             + "'' as chi_name "
             + "from  SYSIBM.SYSDUMMY1 "
             + " union "
             + " select "
             + "program_code as withhold_code, "
             + "chi_name as chi_name "
             + "from mkt_taxpar_vd "
             + " order by 1  "
             ;

   this.sqlSelect();

   return 0;
 }
// ************************************************************************
 public void procMethodABEN() throws Exception
  {
  wp.selectCnt =1;

  if  (wp.itemStr2("trans_seqno").length()==0)
     {
      alertErr("交易序號未輸入");
      return;
     }

  String sql1 = "";
  sql1 = "select  "
       + " apr_flag "
       + " from mkt_uploadfile_ctl   "
       + " where file_type = 'MKT_TRANSBP_PARM' "
       + " and trans_seqno = '" + wp.itemStr2("trans_seqno") +"' "
       ;

  sqlSelect(sql1);
  if (sqlRowNum <= 0)
     {
      alertErr("無此上傳序號 ["+wp.itemStr2("trans_seqno")+"]");
      return;
     }

  if (sqlStr("apr_flag").equals("N"))
     {
      alertErr("該序號尚未上傳 ["+wp.itemStr2("trans_seqno")+"]");
      return;
     }
  else if (sqlStr("apr_flag").equals("R"))
     {
      alertErr("該序號已作廢 ["+wp.itemStr2("trans_seqno")+"]");
      return;
     }
  else if (!sqlStr("apr_flag").equals("Y"))
     {
      alertErr("該序號尚未給點無法作廢 ["+wp.itemStr2("trans_seqno")+"]");
      return;
     }

  if (!checkApprove(wp.itemStr2("zz_apr_user"), wp.itemStr2("zz_apr_passwd"))) return;

  sql1 = "select  "
       + " active_code, "
       + " tran_seqno, "
       + " vd_flag "
       + " from mkt_transbp_log   "
       + " where trans_seqno = '" + wp.itemStr2("trans_seqno") +"' "
       ;

  sqlSelect(sql1);
  int datCnt = sqlRowNum;

  if (!sqlStr("active_code").equals(wp.itemStr2("active_code")))
     {
      alertErr("該序號紅利活動代碼為 ["+wp.itemStr2("active_code")+"]");
      return;
     }

  DbmBonus comd = new DbmBonus();
  comd.setConn(wp);
  MktBonus comb = new MktBonus();
  comb.setConn(wp);

  mktm01.Mktm0320Func func =new mktm01.Mktm0320Func(wp);
  for (int inti=0;inti<datCnt;inti++)
    {
     if (sqlStr(inti,"vd_flag").equals("Y"))
        {
         comd.bonusReverse(sqlStr(inti,"tran_seqno"));
         func.dbDeleteDbmBonusDtl(sqlStr(inti,"tran_seqno"));
        }
     else
        {
         comb.aprUser = wp.itemStr2("zz_apr_user");
         comb.bonusReverse(sqlStr(inti,"tran_seqno"));
         func.dbDeleteMktBonusDtl(sqlStr(inti,"tran_seqno"));
        }
    }


  func.dbUpdateMktUploadfileCtl();
  
  alertMsg("該交易序號已作廢完成! ");

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
   if (wp.respHtml.indexOf("_detl") > 0)
      {
       wp.colSet("tax_flag"      , "N");
      }

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
