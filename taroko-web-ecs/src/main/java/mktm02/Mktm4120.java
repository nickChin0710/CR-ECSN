/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/14  V1.00.01   Ray Ho        Initial                              *
* 108/12/13  V1.00.02   Alex          add initButton                       *
* 112-02-16  V1.00.03  Machao      sync from mega & updated for project coding standard                                                                          *
* 112-03-01  V1.00.04  Zuwei          覆核人變數修改                                                                          *
***************************************************************************/
package mktm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm4120 extends BaseEdit
{
 private final String PROGNAME = "紅利兌換參數檔維護處理程式112-02-16  V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm4120Func func = null;
  String kk1;
  String orgTabName = "bil_redeem";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String   batchNo     = "";
  int errorCnt=0,recCnt=0,notifyCnt=0;
  int[]  datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[50];
  String[] uploadFileDat= new String[50];
  String[] logMsg       = new String[20];

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
      querySelect();
     }
  else if (eqIgno(wp.buttonCode, "A"))
     {// 新增功能 -/
      strAction = "A";
      insertFunc();
     }
  else if (eqIgno(wp.buttonCode, "U"))
     {/* 更新功能 */
      strAction = "U";
      updateFunc();
     }
  else if (eqIgno(wp.buttonCode, "D"))
     {/* 刪除功能 */
      deleteFunc();
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
  controlTabName = orgTabName;

  wp.pageControl();

  wp.selectSQL = " "
               + "a.disc_rate,"
               + "a.dest_amt,"
               + "a.unit_point,"
               + "a.unit_amt,"
               + "a.disc_amt,"
               + "a.sms_flag,"
               + "a.sms_date,"
               + "a.sms_bonus,"
               + "a.edm_flag,"
               + "a.edm_date,"
               + "a.edm_bonus,"
               + "a.line_flag,"
               + "a.line_date,"
               + "a.line_bonus,"
               + "to_char(a.mod_time,'yyyymmddhh24miss') as mod_time,"
               + "a.mod_user,"
               + "a.apr_date,"
               + "a.apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by 1 "
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

  qFrom=0;
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
               + "a.disc_rate,"
               + "a.dest_amt,"
               + "a.unit_point,"
               + "a.unit_amt,"
               + "a.disc_amt,"
               + "a.sms_flag,"
               + "a.sms_date,"
               + "a.sms_bonus,"
               + "a.edm_flag,"
               + "a.edm_date,"
               + "a.edm_bonus,"
               + "a.line_flag,"
               + "a.line_date,"
               + "a.line_bonus,"
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
      alertErr("查無資料, key= "+"["+ kk1 + "]");
      return;
     }
  checkButtonOff();
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  //-check approve-
  if (!checkApprove(wp.itemStr("approval_user"),wp.itemStr("approval_passwd"))) return;

  mktm02.Mktm4120Func func =new mktm02.Mktm4120Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr(func.getMsg());
  log(func.getMsg());
  this.sqlCommit(rc);
 }
// ************************************************************************
 @Override
 public void initButton()
 { 
   this.btnModeAud("XX");
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
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
