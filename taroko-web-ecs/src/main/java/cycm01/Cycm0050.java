/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/11/16  V1.00.10   Allen Ho      Initial                              *
* 111-10-26  V1.00.03    Machao      sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package cycm01;

import cycm01.Cycm0050Func;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycm0050 extends BaseEdit
{
 private final String PROGNAME = "利息折扣參數維護處理程式111/10/26 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  cycm01.Cycm0050Func func = null;
  String kk1;
  String orgTabName = "ptr_actgeneral_n";
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
      updateFunc_U2();
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
               + "a.acct_type,"
               + "a.rc_rate,"
               + "a.rc_rate_limit,"
               + "a.purch_bal_parm,"
               + "a.waive_penauty,"
               + "a.mod_user,"
               + "a.apr_date,"
               + "a.apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by acct_type"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commAcctType("comm_acct_type");
  commModUser("comm_mod_user");
  commAprUser("comm_apr_user");


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
  if (wp.itemStr2("kk_acct_type").length()==0)
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
               + "a.acct_type as acct_type,"
               + "a.rc_rate,"
               + "a.rc_rate_limit,"
               + "a.purch_bal_parm,"
               + "a.waive_penauty,"
               + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
               + "a.mod_user,"
               + "a.apr_date,"
               + "a.apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(wp.itemStr2("kk_acct_type"), "a.acct_type")
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
      alertErr2("查無資料, key= "+"["+ kk1+"]");
      return;
     }
  commAcctType("comm_acct_type");
  commModUser("comm_mod_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
 }
// ************************************************************************
 public void dataReadR2() throws Exception
 {
  if (wp.colStr("acct_type").length()==0)
     {
      wp.colSet("acct_type",itemKk("data_k3"));
      wp.itemSet("acct_type",itemKk("data_k3"));
     }
  dataReadR2(0);
 }
// ************************************************************************
 public void dataReadR2(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   commAcctType("comm_acct_type");
   this.selectNoLimit();
   bnTable = "ptr_curr_general";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "acct_type, "
                + "curr_code, "
                + "purch_bal_wave, "
                + "total_bal, "
                + "purch_bal_parm, "
                + "min_payment, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
               ;
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  acct_type = :acct_type ";
   setString("acct_type", wp.itemStr2("acct_type"));
   whereCnt += " and  acct_type = '"+ wp.itemStr2("acct_type") +  "'";
   wp.whereStr  += " order by 4,5,6,7,8,9,10 ";
   int cnt1=select_bndata_count(wp.daoTable,whereCnt);
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
   if (wp.respHtml.equals("cycm0050_curr"))
    commCurrCode("comm_curr_code");
  }
// ************************************************************************
 public void updateFunc_U2() throws Exception
 {
  wp.listCount[0] = wp.itemBuff("ser_num").length;
  if (!checkApprove(wp.itemStr2("zz_apr_user"), wp.itemStr2("zz_apr_passwd"))) return;
   cycm01.Cycm0050Func func =new cycm01.Cycm0050Func(wp);
   int llOk = 0, llErr = 0;


   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("curr_code");
   String[] key2Data = wp.itemBuff("purch_bal_wave");
   String[] key3Data = wp.itemBuff("total_bal");
   String[] key4Data = wp.itemBuff("purch_bal_parm");
   String[] key5Data = wp.itemBuff("min_payment");

   wp.listCount[0] = key1Data.length;
   wp.colSet("IND_NUM", "" + key1Data.length);
   //-check duplication-

   int del2Flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       del2Flag=0;
       wp.colSet(ll, "ok_flag", "");

       if (key2Data[ll].length()==0) key2Data[ll]="0";
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
                  (empty(key2Data[ll])) &&
                  (empty(key3Data[ll])) &&
                  (empty(key4Data[ll])) &&
              (empty(key5Data[ll])))
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

       func.varsSet("curr_code", key1Data[ll]); 
       func.varsSet("purch_bal_wave", key2Data[ll]); 
       func.varsSet("total_bal", key3Data[ll]); 
       func.varsSet("purch_bal_parm", key4Data[ll]); 
       func.varsSet("min_payment", key5Data[ll]); 

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
 public int select_bndata_count(String bndataTable,String whereStr ) throws Exception
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
  if (!checkApprove(wp.itemStr2("zz_apr_user"), wp.itemStr2("zz_apr_passwd"))) return;

  cycm01.Cycm0050Func func =new cycm01.Cycm0050Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr2(func.getMsg());
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
  if (wp.respHtml.indexOf("_detl") > 0)
     {
      if (!wp.colStr("acct_type").equals("01"))
         buttonOff("btncurr_disable");
       else
         wp.colSet("btncurr_disable","");
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
       if ((wp.respHtml.equals("cycm0050_detl")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          if (wp.colStr("kk_acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("kk_acct_type");
             wp.initOption ="";
             }
          if (wp.colStr("acct_type").length()>0)
             {
              wp.initOption ="--";
             }
          this.dddwList("dddw_acct_type2"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
       if ((wp.respHtml.equals("cycm0050_curr")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          this.dddwList("dddw_curr_code"
                 ,"ptr_currcode"
                 ,"trim(curr_code)"
                 ,"trim(curr_chi_name)"
                 ," where bill_sort_seq !=''");
         }
      } catch(Exception ex){}
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
 public void commModUser(String s1) throws Exception 
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
            + " and   usr_id = '"+wp.colStr(ii,"mod_user")+"'"
            ;
       if (wp.colStr(ii,"mod_user").length()==0)
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
 public void commCurrCode(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " curr_chi_name as column_curr_chi_name "
            + " from ptr_currcode "
            + " where 1 = 1 "
            + " and   curr_code = '"+wp.colStr(ii,"curr_code")+"'"
            + " and   bill_sort_seq != '' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_curr_chi_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void wfAjaxFunc2(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr2("ax_win_acct_type").length()==0) return;

  if (selectAjaxFunc20(
                    wp.itemStr2("ax_win_curr_code"),
                    wp.itemStr2("ax_win_acct_type"))!=0) 
     {
      return;
     }

 }
// ************************************************************************
 int selectAjaxFunc20(String s1,String s2) throws Exception
  {
   wp.sqlCmd = " select "
             + " 1 as countCnt "
             + " from ptr_curr_general "
             + " where curr_code = '"+s1+"' "
             + " and   acct_type = '"+s2+"' "
             ;

   this.sqlSelect();

   if (sqlRowNum>0)
      {
       wp.sqlCmd = " select "
                 + " curr_chi_name as curr_name "
                 + " from ptr_currcode "
                 + " where curr_code = '"+s1+"' "
                 ;
   
       this.sqlSelect();

       alertErr2("幣別：["+s1+"-"+sqlStr("curr_name")+"] 已存在");
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

}  // End of class
