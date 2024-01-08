/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/12  V1.00.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
* 111-12-26  V1.00.01  Zuwei Su       Ajax調用方式修改                                                                       *
***************************************************************************/
package dbmm01;

import dbmm01.Dbmm0100Func;
import ofcapp.BaseEdit;
import ofcapp.AppMsg;
import java.util.Arrays;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmm0100 extends BaseEdit
{
 private final String PROGNAME = "帳戶DEBT紅利明細檔線上調整作業處理程式111-11-28  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  dbmm01.Dbmm0100Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "dbm_bonus_dtl";
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

    default:
        break;
}
  }

  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 "
              + sqlChkEx(wp.itemStr2("ex_id_no"), "1", "")
              + sqlChkEx(wp.itemStr2("ex_card_no"), "3", "")
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
               + "a.tran_date,"
               + "a.tran_seqno,"
               + "'' as id_no,"
               + "'' as chi_name,"
               + "a.bonus_type,"
               + "a.tran_code,"
               + "a.beg_tran_bp,"
               + "a.crt_user,"
               + "a.crt_date,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by tran_date desc,tran_seqno desc"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commIdNo("comm_id_no");
  commChiName("comm_chi_name");
  commBonusType("comm_bonus_type");
  commCrtUser("comm_crt_user");

  commTranCode("comm_tran_code");

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
               + "a.id_p_seqno as id_p_seqno,"
               + "a.active_name as active_name,"
               + "a.tran_seqno as tran_seqno,"
               + "a.apr_flag,"
               + "a.acct_type,"
               + "a.tran_date,"
               + "decode(tran_time,'','',substr(tran_time,1,2)||':'||substr(tran_time,3,2)||':'||substr(tran_time,5,2)) as tran_time,"
               + "c.chi_name as chi_name,"
               + "id_no,"
               + "a.id_p_seqno,"
               + "a.active_code,"
               + "a.active_name,"
               + "a.tran_code,"
               + "a.beg_tran_bp,"
               + "a.tax_flag,"
               + "a.effect_e_date,"
               + "a.mod_reason,"
               + "a.mod_desc,"
               + "a.mod_memo,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user";

  wp.daoTable = controlTabName + " a "
              + "JOIN dbc_idno c "
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
  commAprFlag2("comm_apr_flag");
  commAprFlag2("comm_apr_flag");
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
               + "a.apr_flag as apr_flag,"
               + "a.acct_type as acct_type,"
               + "a.tran_date as tran_date,"
               + "decode(tran_time,'','',substr(tran_time,1,2)||':'||substr(tran_time,3,2)||':'||substr(tran_time,5,2)) as tran_time,"
               + "c.chi_name as chi_name,"
               + "id_no,"
               + "a.id_p_seqno as id_p_seqno,"
               + "a.active_code as active_code,"
               + "a.active_name as active_name,"
               + "a.tran_code as tran_code,"
               + "a.beg_tran_bp as beg_tran_bp,"
               + "a.tax_flag as tax_flag,"
               + "a.effect_e_date as effect_e_date,"
               + "a.mod_reason as mod_reason,"
               + "a.mod_desc as mod_desc,"
               + "a.mod_memo as mod_memo,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              + "JOIN dbc_idno c "
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
  commAprFlag2("comm_apr_flag");
  commAprFlag2("comm_apr_flag");
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
  dbmm01.Dbmm0100Func func =new dbmm01.Dbmm0100Func(wp);

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
       if ((wp.respHtml.equals("dbmm0100_nadd"))||
           (wp.respHtml.equals("dbmm0100_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("acct_type");
             wp.initOption ="";
             }
          this.dddwList("dddw_acct_type1"
                 ,"dbp_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("active_code").length()>0)
             {
             wp.optionKey = wp.colStr("active_code");
             }
          this.dddwList("dddw_active_name"
                 ,"vdbm_bonus_active_name"
                 ,"trim(active_code)"
                 ,"trim(active_name)"
                 ," where 1 = 1 ");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("mod_reason").length()>0)
             {
             wp.optionKey = wp.colStr("mod_reason");
             }
          this.dddwList("dddw_mod_reason"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
 //                ," where wf_type='ADJMOD_REASON' and substr(wf_dsptype,1,1)='Y' ");
                 ," where wf_type='ADJMOD_REASON' ");          
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
 if ((itemKk("ex_card_no").length()==0)&&
      (itemKk("ex_apr_flag").equals("Y"))&&
      (itemKk("ex_id_no").length()==0))
     {
      alertErr("身份證號與卡號二者不可同時空白");
      return(1);
     }

  if (wp.itemStr2("ex_id_no").length()>0)
     {
      String sql1 = "select id_p_seqno "
                  + "from dbc_idno "
                  + "where  id_no  =  '"+ wp.itemStr2("ex_id_no").toUpperCase() +"'"
                  ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr(" 查無此身分證號[ "+wp.itemStr2("ex_id_no").toUpperCase() +"] 資料");
          return(1);
         }
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      return(0);
     }


  if (wp.itemStr2("ex_card_no").length()>0)
     {
      String sql1 = "select id_p_seqno "
                  + "from dbc_card "
                  + "where  card_no  =  '"+ wp.itemStr2("ex_card_no").toUpperCase() +"'"
                  ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr(" 查無此卡號[ "+wp.itemStr2("ex_card_no").toUpperCase() +"] 資料");
          return(1);
         }
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
     }

  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
     {
      if (empty(wp.itemStr2("ex_id_no"))) return "";
      return " and id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
     }

  if (sqCond.equals("3"))
     {
      if (empty(wp.itemStr2("ex_card_no"))) return "";
      return " and id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
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
 public void commIdNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " id_no as column_id_no "
            + " from dbc_idno "
            + " where 1 = 1 "
            + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_id_no"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commChiName(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chi_name as column_chi_name "
            + " from dbc_idno "
            + " where 1 = 1 "
            + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
            ;
       if (wp.colStr(ii,"id_p_seqno").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_chi_name"); 
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
 public void commTranCode(String s1) throws Exception 
 {
  String[] cde = {"0","1","2","3","4","5","6","7"};
  String[] txt = {"移轉","新增","贈與","調整","使用","匯入","移除","扣回"};
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
                    wp.itemStr2("ax_win_acct_type"),
                    wp.itemStr2("ax_win_id_no").toUpperCase())!=0) 
     {
      wp.addJSON("chi_name","");
      wp.addJSON("id_p_seqno","");
      return;
     }

  wp.addJSON("chi_name",sqlStr("chi_name"));
  wp.addJSON("id_p_seqno",sqlStr("id_p_seqno"));
 }
// ************************************************************************
 int selectAjaxFunc10(String s1,String s2) throws Exception
  {
   String idNoCode = "0";
   if (s2.length()>10)
      {
       idNoCode = s2.substring(10,11); 
       s2 =s2.substring(0,10);
      }
   wp.sqlCmd = " select "
             + " a.chi_name as chi_name ,"
             + " b.id_p_seqno as id_p_seqno "
             + " from  dbc_idno a,dba_acno b "
             + " where a.id_p_seqno=b.id_p_seqno "
             + " and   b.acct_type  ='"+s1+"' "
             + " and   a.id_no      ='"+s2+"' "
             + " and   a.id_no_code ='"+idNoCode+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("帳戶類別:["+s1+"]["+s2+"]查無資料");
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
  int inta=selectDbmSysparm();
  if (inta>0)
     {
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      wp.colSet("effect_e_date",comm.nextMonthDate(wp.sysDate,inta));
     }
   if ((wp.respHtml.equals("dbmm0100_nadd"))||
       (wp.respHtml.equals("dbmm0100_detl")))
      {
       wp.colSet("tax_flag" , "N");
       wp.colSet("tran_code" , "3");
      }


  return;
 }
// ************************************************************************
 int selectDbmSysparm() 
 {
  String  sql1="";
  sql1 = "select "
       + " effect_months "
       + " from dbm_sysparm "
       + " where parm_type = '01' "
       + " and   apr_date !='' "
       ;

  sqlSelect(sql1);

  if (sqlRowNum<=0) return(-1);

  return((int)sqlNum("effect_months"));
 }

// ************************************************************************

}  // End of class
