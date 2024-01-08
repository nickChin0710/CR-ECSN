/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/07  V1.00.01   Allen Ho      Initial                              *
* 111/11/14  V1.00.02   Machao        欄位名稱調整                           *
* 112/06/05  V1.00.03   Grace Huang   comm_megalite_flag 更名為 comm_banklite_flag         *
***************************************************************************/
package mktm02;

import mktm02.Mktm6255Func;
import ofcapp.AppMsg;
import ofcapp.BaseEdit;

import java.util.Arrays;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6255 extends BaseEdit
{
 private  String PROGNAME = "首刷禮活動兌換維護處理程式109/12/07 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6255Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_fstp_carddtl";
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

  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr2("ex_active_code"), "a.active_code", "like%")
              + sqlChkEx(wp.itemStr2("ex_error_flag"), "3", "")
              + sqlChkEx(wp.itemStr2("ex_id_no"), "1", "")
              + sqlCol(wp.itemStr2("ex_card_no"), "a.card_no", "like%")
              + sqlChkEx(wp.itemStr2("ex_apr_flag"), "2", "")
              + sqlStrend(wp.itemStr2("ex_issue_date_s"), wp.itemStr2("ex_issue_date_e"), "a.issue_date")
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
               + "a.acct_type,"
               + "'' as id_no,"
               + "'' as chi_name,"
               + "a.card_no,"
               + "a.active_code,"
               + "a.active_type,"
               + "decode(active_type,'1',beg_tran_bp,'2',beg_tran_amt,'3',beg_tran_amt,0) as feedback_amt,"
               + "a.feedback_date,"
               + "a.active_seq,"
               + "a.error_code,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by active_code,card_no"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commAcctType("comm_acct_type");
  commIdNo("comm_id_no");
  commChiName("comm_chi_name");
  commActiveCode("comm_active_code");

  commActiveType("comm_active_type");
  commErrorCode("comm_error_code");

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
               + "a.prog_code as prog_code,"
               + "a.prog_s_date as prog_s_date,"
               + "a.prog_e_date as prog_e_date,"
               + "a.gift_no as gift_no,"
               + "a.card_no as card_no,"
               + "a.apr_flag,"
               + "a.active_code,"
               + "c.id_no as id_no,"
               + "c.chi_name as chi_name,"
               + "a.acct_type,"
               + "a.error_code,"
               + "a.linebc_flag,"
               + "a.banklite_flag,"
               + "a.selfdeduct_flag,"
               + "a.feedback_date,"
               + "a.execute_date,"
               + "a.active_type,"
               + "a.mod_date,"
               + "a.bonus_type,"
               + "a.beg_tran_bp,"
               + "a.fund_code,"
               + "a.beg_tran_amt,"
               + "(group_type||'-'||prog_code||'-'||prog_s_date||'-'||gift_no) as gift_no1,"
               + "a.tran_pt,"
               + "a.spec_gift_no,"
               + "a.spec_gift_cnt,"
               + "a.mod_desc,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.card_no")
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
  commErrorCode("comm_error_code");
  commLinebc("comm_linebc_flag");
  commBanklite("comm_banklite_flag");
  commSelfdeduct("comm_selfdeduct_flag");
  commActiveCode("comm_active_code");
  commAcctType("comm_acct_type");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("card_no");
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
               + "a.card_no as card_no,"
               + "a.apr_flag as apr_flag,"
               + "a.active_code as active_code,"
               + "c.id_no as id_no,"
               + "c.chi_name as chi_name,"
               + "a.acct_type as acct_type,"
               + "a.error_code as error_code,"
               + "a.linebc_flag as linebc_flag,"
               + "a.banklite_flag as banklite_flag,"
               + "a.selfdeduct_flag as selfdeduct_flag,"
               + "a.feedback_date as feedback_date,"
               + "a.execute_date as execute_date,"
               + "a.active_type as active_type,"
               + "a.mod_date as mod_date,"
               + "a.bonus_type as bonus_type,"
               + "a.beg_tran_bp as beg_tran_bp,"
               + "a.fund_code as fund_code,"
               + "a.beg_tran_amt as beg_tran_amt,"
               + "(group_type||'-'||prog_code||'-'||prog_s_date||'-'||gift_no) as gift_no1,"
               + "a.tran_pt as tran_pt,"
               + "a.spec_gift_no as spec_gift_no,"
               + "a.spec_gift_cnt as spec_gift_cnt,"
               + "a.mod_desc as mod_desc,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.card_no")
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
  commActiveCode("comm_active_code");
  commAcctType("comm_acct_type");
  commErrorCode("comm_error_code");
  commLinebc("comm_linebc_flag");
  commBanklite("comm_banklite_flag");
  commSelfdeduct("comm_selfdeduct_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commfuncAudType("aud_type");
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("card_no");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      km1 = wp.itemStr2("card_no");
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
   km1 = wp.itemStr2("card_no");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr2("card_no");
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
  Mktm6255Func func =new Mktm6255Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr2(func.getMsg());
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
       if ((wp.respHtml.equals("mktm6255_nadd"))||
           (wp.respHtml.equals("mktm6255_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("bonus_type").length()>0)
             {
             wp.optionKey = wp.colStr("bonus_type");
             }
          lsSql = "";
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql =  procDynamicDddwBonusType(wp.colStr("active_code"));

          if (wp.colStr("active_type").equals("1"))
             {
              if (wp.itemStr2("bonus_type").length()>0)
                 wp.optionKey = wp.itemStr2("bonus_type");
              else if (wp.colStr("bonus_type").length()>0)
                 wp.optionKey = wp.colStr("bonus_type");
             }
          dddwList("dddw_newbonus_type", lsSql);

          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("fund_code").length()>0)
             {
             wp.optionKey = wp.colStr("fund_code");
             }
          lsSql = "";
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql =  procDynamicDddwFundCode(wp.colStr("active_code"));

          if (wp.colStr("active_type").equals("2"))
             {
              if (wp.itemStr2("fund_code").length()>0)
                 wp.optionKey = wp.itemStr2("fund_code");
              else if (wp.colStr("fund_code").length()>0)
                 wp.optionKey = wp.colStr("fund_code");
             }
          dddwList("dddw_newfund_code", lsSql);

          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("(group_type||'-'||prog_code||'-'||prog_s_date||'-'||gift_no)").length()>0)
             {
             wp.optionKey = wp.colStr("(group_type||'-'||prog_code||'-'||prog_s_date||'-'||gift_no)");
             }
          lsSql = "";
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql =  procDynamicDddwGiftNo(wp.colStr("active_code"));

          if (wp.colStr("active_type").equals("3"))
             {
              if (wp.itemStr2("gift_no1").length()>0)
                 wp.optionKey = wp.itemStr2("gift_no1");
              else if (wp.colStr("gift_no1").length()>0)
                 wp.optionKey = wp.colStr("gift_no1");
             }
          dddwList("dddw_newgift_no", lsSql);

          wp.optionKey = "";
          wp.initOption ="";
          if (wp.colStr("spec_gift_no").length()>0)
             {
             wp.optionKey = wp.colStr("spec_gift_no");
             }
          lsSql = "";
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql =  procDynamicDddwSpecGift(wp.colStr("active_code"));

          if (wp.colStr("active_type").equals("4"))
             {
              if (wp.itemStr2("spec_gift_no").length()>0)
                 wp.optionKey = wp.itemStr2("spec_gift_no");
              else if (wp.colStr("spec_gift_no").length()>0)
                 wp.optionKey = wp.colStr("spec_gift_no");
             }
          dddwList("dddw_newspec_gift", lsSql);

         }
       if ((wp.respHtml.equals("mktm6255")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_active_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_active_code");
             }
          this.dddwList("dddw_active_code"
                 ,"mkt_fstp_parm"
                 ,"trim(active_code)"
                 ,"trim(active_name)"
                 ," where 1 = 1 ");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
  if ((itemKk("ex_active_code").length()==0)&&
      (itemKk("ex_id_no").length()==0)&&
      (itemKk("ex_card_no").length()==0))
     {
      alertErr2("身份證號與活動代碼,卡號三者不可同時空白");
      return(1);
     }

  String sql1 = "";
  sql1 = "select a.id_p_seqno, "
       + "       a.chi_name "
       + "from crd_idno a "
       + "where  id_no  =  '"+ wp.itemStr2("ex_id_no").toUpperCase() +"'"
       + "and    id_no_code   = '0' "
       ;
  sqlSelect(sql1);
  if (sqlRowNum > 1)
     {
      alertErr2(" 查無資料 !");
      return(1);
     }
  wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));




  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
     {
      if (empty(wp.itemStr2("ex_id_no"))) return "";
      return " and a.id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
     }
  if (sqCond.equals("3"))
     {
      if (empty(wp.itemStr2("ex_error_flag"))) return "";
      if (wp.itemStr2("ex_error_flag").equals("1"))
          return " and proc_flag ='Y' and error_code ='00' ";
      else if (wp.itemStr2("ex_error_flag").equals("2"))
          return " and proc_flag ='Y' and error_code between '31' and '39'  ";
      else if (wp.itemStr2("ex_error_flag").equals("3"))
          return " and proc_flag ='N' and error_code ='00' ";
      else if (wp.itemStr2("ex_error_flag").equals("4"))
          return " and proc_flag ='N' and error_code between '01' and '19' ";
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
 public void commActiveCode(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " active_name as column_active_name "
            + " from mkt_fstp_parm "
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
 public void commIdNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " id_no as column_id_no "
            + " from crd_idno "
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
            + " from crd_idno "
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
 public void commErrorCode(String s1) throws Exception 
 {
  String[] cde = {"00","01","02","03","04","05","06","07","08","09","10","11","12","31","32","33","34"};
  String[] txt = {"符合條件","非新卡有","帳戶類別","團 體代號",">來源代號","卡種","通路代號","非新卡友","深呆戶_無等級","深呆戶_有消費","深呆戶_有消費","深呆戶_卡人等>級","已有首刷","消費金額","LINEBC","非自扣","MEGA_LITE"};
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
 public void commLinebc(String s1) throws Exception 
 {
  String[] cde = {"X","N","Y"};
  String[] txt = {"不需檢核","檢核失敗","檢核成功"};
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
 public void commBanklite(String s1) throws Exception 
 {
  String[] cde = {"X","N","Y"};
  String[] txt = {"不需檢核","檢核失敗","檢核成功"};
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
 public void commSelfdeduct(String s1) throws Exception 
 {
  String[] cde = {"X","N","Y"};
  String[] txt = {"不需檢核","檢核失敗","檢核成功"};
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
 public void commActiveType(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"紅利","基金","豐富點","贈品"};
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
 String procDynamicDddwFundCode(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " a.fund_code as db_code, "
          + " a.fund_code||' '||a.fund_name as db_desc "
          + " from mkt_loan_parm a,mkt_fstp_parm b "
          + " where a.fund_code = b.fund_code "
          + " and   b.fund_code!='' "
          + " and   b.active_type='2' "
          + " and   b.active_code = '" + s1 +"' "
          + " union "
          + " select "
          + " a.fund_code as db_code, "
          + " a.fund_code||' '||a.fund_name as db_desc "
          + " from mkt_loan_parm a,mkt_fstp_parmseq b "
          + " where a.fund_code = b.fund_code "
          + " and   b.active_type='2' "
          + " and   b.fund_code!='' "
          + " and   b.active_code = '" + s1 +"' "
          ;

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwBonusType(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " a.wf_id as db_code, "
          + " a.wf_id||' '||a.wf_desc as db_desc "
          + " from ptr_sys_idtab a,mkt_fstp_parm b "
          + " where a.wf_id = b.bonus_type "
          + " and   b.active_type='1' "
          + " and   b.bonus_type!='' "
          + " and   b.active_code = '" + s1 +"' "
          + " union "
          + " select "
          + " a.wf_id as db_code, "
          + " a.wf_id||' '||a.wf_desc as db_desc "
          + " from ptr_sys_idtab a,mkt_fstp_parmseq b "
          + " where a.wf_id = b.bonus_type "
          + " and   b.active_type='1' "
          + " and   b.bonus_type!='' "
          + " and   b.active_code = '" + s1 +"' "
          ;

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwSpecGift(String s1)  throws Exception
 { 
   String lsSql = "";
   
   lsSql = " select "
          + " a.gift_no as db_code, "
          + " a.gift_no||' '||a.gift_name as db_desc " 
          + " from mkt_spec_gift a,mkt_fstp_parm b "
          + " where a.gift_no = b.spec_gift_no "
          + " and   b.active_type='4' "
          + " and   b.spec_gift_no!='' " 
          + " and   a.gift_group='1' " 
          + " and   b.active_code = '" + s1 +"' "
          + " union "
          + " select "
          + " a.gift_no as db_code, "
          + " a.gift_no||' '||a.gift_name as db_desc "
          + " from mkt_spec_gift a,mkt_fstp_parmseq b "
          + " where a.gift_no = b.spec_gift_no "
          + " and   b.active_type='4' "
          + " and   a.gift_group='1' " 
          + " and   b.spec_gift_no!='' " 
          + " and   b.active_code = '" + s1 +"' "
          ;
   
   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwGiftNo(String s1)  throws Exception
 {
   String lsSql = "";
   
   lsSql = " select "
          + " b.group_type||'-'||a.prog_code||'-'||a.prog_s_date||'-'||a.gift_no as db_code, "
          + " b.group_type||'-'||a.prog_code||'-'||a.prog_s_date||'-'||a.gift_no||' '||a.gift_name as db_desc "
          + " from  ibn_prog_gift a,mkt_fstp_parm b "
          + " where a.gift_no     = b.gift_no "
          + " and   a.prog_s_date = b.prog_s_date "
          + " and   a.prog_code   = b.prog_code "
          + " and   b.active_type='3' "
          + " and   b.gift_no   !='' "
          + " and   b.active_code = '" + s1 +"' "
          + " union "
          + " select "
          + " b.group_type||'-'||a.prog_code||'-'||a.prog_s_date||'-'||a.gift_no as db_code, "
          + " b.group_type||'-'||a.prog_code||'-'||a.prog_s_date||'-'||a.gift_no||' '||a.gift_name as db_desc "
          + " from  ibn_prog_gift a,mkt_fstp_parmseq b "
          + " where a.gift_no = b.gift_no "
          + " and   a.prog_s_date = b.prog_s_date "
          + " and   a.prog_code = b.prog_code "
          + " and   b.active_type='3' "
          + " and   b.gift_no!='' "
          + " and   b.active_code = '" + s1 +"' "
          ; 

   return lsSql;
 }
// ************************************************************************
// ************************************************************************

}  // End of class
