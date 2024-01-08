/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/17  V1.00.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.02  Machao    sync from mega & updated for project coding standard                                                                         *
* 111-12-19  V1.00.03  Zuwei          修改AJAX調用方式                                                                    *
***************************************************************************/
package mktm02;

import mktm02.Mktm1010Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1010 extends BaseEdit
{
 private final String PROGNAME = "紅利積點移轉異動維護處理程式111-12-19 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1010Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_tr_bonus";
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
  else if (eqIgno(wp.buttonCode, "AJAX"))
  {/* nothing to do */
   strAction = "";
   switch (wp.itemStr("methodName")) {
    case "wf_ajax_func_1":
        wfAjaxFunc1(wp);
        break;
    case "wf_ajax_func_2":
//        wfAjaxFunc2(wp);
        break;
    case "wf_button_func_3":
        wfButtonFunc3(wp);
        break;

    default:
        break;
}
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
              + sqlChkEx(wp.itemStr2("ex_id_no"), "4", "")
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
               + "a.trans_date,"
               + "a.acct_type,"
               + "c.id_no,"
               + "a.to_acct_type,"
               + "a.bonus_pnt,"
               + "a.fee_amt,"
               + "a.tran_seqno,"
               + "a.bonus_type,"
               + "a.method,"
               + "a.card_no";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereOrder = " "
                + " order by trans_Date desc,tran_seqno desc"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commAcctType("comm_acct_type");
  commAcctType2("comm_to_acct_type");
  commBonusType("comm_bonus_type");

  commMethod("comm_method");

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
               + "a.tran_seqno as tran_seqno,"
               + "a.acct_type,"
               + "c.chi_name as chi_name,"
               + "id_no,"
               + "a.bonus_type,"
               + "a.to_acct_type,"
               + "a.bonus_pnt,"
               + "a.fee_amt,"
               + "a.proc_code,"
               + "a.proc_date,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user,"
               + "a.p_seqno,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.tran_seqno")
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
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("tran_seqno");
  commfuncAudType("aud_type");
  dataReadR3R();
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = orgTabName +"_t";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + " a.aud_type as aud_type, "
               + "a.tran_seqno as tran_seqno,"
               + "a.acct_type as acct_type,"
               + "c.chi_name as chi_name,"
               + "id_no,"
               + "a.bonus_type as bonus_type,"
               + "a.to_acct_type as to_acct_type,"
               + "a.bonus_pnt as bonus_pnt,"
               + "a.fee_amt as fee_amt,"
               + "a.proc_code as proc_code,"
               + "a.proc_date as proc_date,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user,"
               + "a.p_seqno as p_seqno,"
               + "a.id_p_seqno as id_p_seqno";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.tran_seqno")
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
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commfuncAudType("aud_type");
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("tran_seqno");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      km1 = wp.itemStr2("tran_seqno");
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
   km1 = wp.itemStr2("tran_seqno");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr2("tran_seqno");
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
  mktm02.Mktm1010Func func =new mktm02.Mktm1010Func(wp);

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
       if ((wp.respHtml.equals("mktm1010_nadd"))||
           (wp.respHtml.equals("mktm1010_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("acct_type");
             wp.initOption ="";
             }
          this.dddwList("dddw_acct_type2"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
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
          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("to_acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("to_acct_type");
             wp.initOption ="";
             }
          this.dddwList("dddw_acct_type1"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
/*
  if ((wp.item_ss("ex_id_no").length()!=10)&&
      (wp.item_ss("ex_id_no").length()!=11))
     {
      alertErr("統編輸入8碼, 身分證號10碼,帳戶查詢碼11碼");
      return(1);
     }
*/
  String sql1 = "";
  if (wp.itemStr2("ex_id_no").length()==10)
     {
      sql1 = "select a.id_p_seqno, "
           + "       a.chi_name "
           + "from crd_idno a,act_acno b "
           + "where  id_no  =  '"+ wp.itemStr2("ex_id_no").toUpperCase() +"'"
           + "and    id_no_code   = '0' "
           + "and    a.id_p_seqno = b.id_p_seqno "
           ;

      if (wp.itemStr2("ex_acct_type").length()!=0)
          sql1 = sql1
                + "and   b.acct_type  =  '"+ wp.itemStr2("ex_acct_type").toUpperCase() +"' ";

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr(" 查無此身分證號[ "+wp.itemStr2("ex_id_no").toUpperCase() +"] 資料");
          return(1);
         }
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name",sqlStr("chi_name"));
      return(0);
     }

  if (wp.itemStr2("ex_id_no").length()==11)
     {
      sql1 = "select a.p_seqno, "
           + "       a.id_p_seqno, "
           + "       a.corp_p_seqno, "
           + "       b.card_indicator "
           + "from act_acno a,ptr_acct_type b "
           + "where a.acct_key  = '"+ wp.itemStr2("ex_id_no").toUpperCase() +"' "
           + "and   a.acct_type = b.acct_type "
           ;

      if (wp.itemStr2("ex_acct_type").length()!=0)
          sql1 = sql1
                + "and   b.acct_type  =  '"+ wp.itemStr2("ex_acct_type").toUpperCase() +"' ";

      sqlSelect(sql1);
      if (sqlRowNum > 1)
         {
          alertErr(" 查有多身分資料, 請輸入帳戶類別");
          return(1);
         }
      if (sqlRowNum <= 0)
         {
          alertErr(" 查無此帳戶查詢碼[ "+wp.itemStr2("ex_id_no").toUpperCase() +"] 資料");
          return(1);
         }
      sql1 = "select chi_name "
           + "from   crd_idno "
           + "where  id_p_seqno = '"+ sqlStr("id_p_seqno") +"' "
           ;
      sqlSelect(sql1);
      wp.colSet("ex_chi_name",sqlStr("chi_name"));
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));

      return(0);
     }


  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("4"))
     {
      if (empty(wp.itemStr2("ex_id_no"))) return "";
      return " and a.id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
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
 public void commAcctType2(String s1) throws Exception 
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
            + " and   acct_type = '"+wp.colStr(ii,"to_acct_type")+"'"
            ;
       if (wp.colStr(ii,"to_acct_type").length()==0)
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
            + " and   wf_id = '"+wp.colStr(ii,"bonus_TYPE")+"'"
            + " and   wf_type = 'BONUS_NAME' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commMethod(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"線上","語音"};
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

  if (wp.itemStr2("ax_win_id_no").length()==0) return;

  if (selectAjaxFunc10(
                    wp.itemStr2("ax_win_acct_type"),
                    wp.itemStr2("ax_win_id_no").toUpperCase())!=0) 
     {
      wp.addJSON("chi_name","");
      wp.addJSON("p_seqno","");
      wp.addJSON("id_p_seqno","");
      return;
     }

  wp.addJSON("chi_name",sqlStr("chi_name"));
  wp.addJSON("p_seqno",sqlStr("p_seqno"));
  wp.addJSON("id_p_seqno",sqlStr("id_p_seqno"));
 }
// ************************************************************************
 int selectAjaxFunc10(String s1,String s2) throws Exception
  {
   wp.sqlCmd = " select "
             + " a.chi_name as chi_name ,"
             + " b.p_seqno as p_seqno ,"
             + " a.id_p_seqno as id_p_seqno "
             + " from  crd_idno a,act_acno b "
             + " where a.id_p_seqno=b.id_p_seqno "
             + " and   b.acct_type ='"+s1+"' "
             + " and   a.id_no ='"+s2+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      { 
       if (s1.length()==0)
          {
           alertErr("轉出帳戶類別未輸入資料");
          }
       else
          {
           alertErr("轉出帳戶類別["+s1+"]查無資料");
          }
       return 1; 
      } 

   return 0;
 }
// ************************************************************************
 public void wfButtonFunc3(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (wp.itemStr2("ax_win_acct_type").length()==0) return;
  if (wp.itemStr2("ax_win_id_no").length()==0) return;
  if (wp.itemStr2("ax_win_bonus_type").length()==0) return;
  if (selectButtonFunc3(
                       wp.itemStr2("ax_win_acct_type"),
                       wp.itemStr2("ax_win_id_no"),
                       wp.itemStr2("ax_win_bonus_type"))!=0)
     {
      wp.addJSON("end_tran_bp_notax","");
      wp.addJSON("end_tran_bp_tax","");
      return;
     }

  wp.addJSON("end_tran_bp_notax",sqlStr("end_tran_bp_notax"));
  wp.addJSON("end_tran_bp_tax",sqlStr("end_tran_bp_tax"));
 }
// ************************************************************************
int selectButtonFunc3(String s1,String s2,String s3)  throws Exception
 {
   this.sqlSet(0,"end_tran_bp_notax","0");
   this.sqlSet(0,"end_tran_bp_tax","0");
   wp.sqlCmd = " select "
             + " a.chi_name as chi_name ,"
             + " b.p_seqno as p_seqno ,"
             + " a.id_p_seqno as id_p_seqno "
             + " from  crd_idno a,act_acno b "
             + " where a.id_p_seqno=b.id_p_seqno "
             + " and   b.acct_type ='"+s1+"' "
             + " and   a.id_no ='"+s2.toUpperCase()+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("轉出帳戶類別：["+s1+"-"+s2+"]查無資料");
       return(1);
      }

   wp.sqlCmd = " select "
             + " sum(decode(tax_flag,'Y',0,end_tran_bp)) as end_tran_bp_notax ,"
             + " sum(decode(tax_flag,'Y',end_tran_bp,0)) as end_tran_bp_tax "
             + " from  mkt_bonus_dtl "
             + " where p_seqno  = '"+sqlStr("p_seqno")+"' "
             + " and bonus_type = 'BONU' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0) return(1);

   this.sqlSet(0,"end_tran_bp_notax",String.format("%,.0f",sqlNum("end_tran_bp_notax")));
   this.sqlSet(0,"end_tran_bp_tax"  ,String.format("%,.0f",sqlNum("end_tran_bp_tax")));


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
