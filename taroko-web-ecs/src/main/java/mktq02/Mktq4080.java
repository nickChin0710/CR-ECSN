/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/15  V1.00.00   Allen Ho      Initial    
* 112/08/15  V1.00.01   MAchao        TCB format Naming Rule 處理       
* 112/08/29  V1.00.02   MAchao        瀏覽查询调整           *
***************************************************************************/
package mktq02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq4080 extends BaseEdit
{
 private  String PROGNAME = "通路活動匯入名單查詢作業處理程式112/08/15  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq4080Func func = null;
  String kk1;
  String orgTabName = "mkt_imloan_list";
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
  else if (eqIgno(wp.buttonCode, "M"))
     {/* 瀏覽功能 :skip-page*/
	  queryRead2();
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
              + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code", "like%")
              + sqlCol(wp.itemStr2("ex_list_data"), "a.list_data", "like%")
              + sqlCol(wp.itemStr2("ex_list_flag"), "a.list_flag", "like%")
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
               + "a.fund_code,"
               + "a.list_flag,"
               + "a.list_data,"
               + "a.feedback_amt,"
               + "a.proc_date,"
               + "a.proc_flag,"
               + "to_char(MOD_TIME,'yyyy/mm/dd hh24:mi:ss') as mod_time,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  
  wp.whereOrder = " "
                + " order by fund_code,list_data"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commFundCode("comm_fund_code");

  commListFlag("comm_list_flag");
  commProcFlag("comm_proc_flag");

  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 public void queryRead2() throws Exception
 {
  if (queryCheck()!=0) return;
  if (wp.colStr("org_tab_name").length()>0)
     controlTabName = wp.colStr("org_tab_name");
  else
     controlTabName = orgTabName;

  wp.pageControl();

  wp.selectSQL = " "
               + "hex(a.rowid) as rowid, "
               + "a.fund_code,"
               + "a.list_flag,"
               + "a.list_data,"
               + "a.feedback_amt,"
               + "a.proc_date,"
               + "a.proc_flag,"
               + "to_char(MOD_TIME,'yyyy/mm/dd hh24:mi:ss') as mod_time,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "WHERE 1=1 "
          + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code", "like%")
          + sqlCol(wp.itemStr2("ex_list_data"), "a.list_data", "like%")
          + sqlCol(wp.itemStr2("ex_list_flag"), "a.list_flag", "like%")
          ;
  wp.whereOrder = " "
                + " order by fund_code,list_data"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commFundCode("comm_fund_code");

  commListFlag("comm_list_flag");
  commProcFlag("comm_proc_flag");

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
               + "a.fund_code,"
               + "a.list_data,"
               + "a.list_flag,"
               + "'' as id_no,"
               + "'' as chi_name,"
               + "a.acct_type,"
               + "a.card_no,"
               + "a.feedback_amt,"
               + "a.tran_seqno,"
               + "a.birth_day,"
               + "a.name,"
               + "a.branch_name,"
               + "a.branch_onlineno,"
               + "a.branch_acctno,"
               + "a.title,"
               + "a.proc_date,"
               + "a.proc_flag,"
               + "to_char(mod_time,'yyyy/mm/dd hh24:mi:ss') as mod_time,"
               + "a.mod_pgm,"
               + "a.id_p_seqno";

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
  commListFlag("comm_list_flag");
  commProcFlag("comm_proc_flag");
  commFundCode("comm_fund_code");
  commIdNo("comm_id_no");
  commChiName("comm_chi_name");
  commAcctType("comm_acct_type");
  checkButtonOff();
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktq02.Mktq4080Func func =new mktq02.Mktq4080Func(wp);

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
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String lsSql ="";
  try {
       if ((wp.respHtml.equals("mktq4080")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_fund_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_fund_code");
             }
          lsSql = "";

          if (wp.itemStr2("ex_query_table").equals("1"))
             lsSql =  procDynamicDddwFundCode("mkt_loan_parm");
          else
             lsSql =  procDynamicDddwFundCode("mkt_loan_parm_t");

          wp.optionKey = wp.colStr("ex_fund_code");
          dddwList("dddw_fund_code", lsSql);
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
   if (itemKk("ex_query_table").equals("1"))
      orgTabName = "mkt_imloan_list";
   else
      orgTabName = "mkt_imloan_list_t";

   controlTabName = orgTabName.toUpperCase();
   wp.colSet("control_tab_name",controlTabName);

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

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr(" 查無此身分證號[ "+wp.itemStr2("ex_id_no").toUpperCase() +"] 資料");
          return(1);
         }
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      wp.colSet("ex_p_seqno"   ,"");
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

      sqlSelect(sql1);
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
      wp.colSet("ex_p_seqno"   ,"");
         
      return(0);
     }

  return(0);
 }
// ************************************************************************
 public void commFundCode(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " fund_name as column_fund_name "
            + " from mkt_loan_parm "
            + " where 1 = 1 "
            + " and   fund_code = '"+wp.colStr(ii,"fund_code")+"'"
            ;
       if (wp.colStr(ii,"fund_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_fund_name"); 
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
 public void commListFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4","5","6"};
  String[] txt = {"身份證號","卡號","一卡通卡號","悠遊卡號","愛金卡號","生日禮"};
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
 public void commProcFlag(String s1) throws Exception 
 {
  String[] cde = {"N","Y","1","2","3"};
  String[] txt = {"尚未處理","回饋成功","帳戶類別不符","團體代號不符","回饋條件不符"};
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
  String ajaxjFundCode = "";
  super.wp = wr;


  if (selectAjaxFunc20(
                    wp.itemStr2("ax_win_query_table"))!=0) 
     {
      wp.addJSON("ajaxj_fund_code", "");
      wp.addJSON("ajaxj_fund_name", "");
      return;
     }

  wp.addJSON("ajaxj_fund_code", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_fund_code", sqlStr(ii, "fund_code"));
  wp.addJSON("ajaxj_fund_name", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_fund_name", sqlStr(ii, "fund_name"));
 }
// ************************************************************************
 int selectAjaxFunc20(String s1) throws Exception
  {
   if (s1.equals("1"))
      {
       wp.sqlCmd = " select "
                 + " fund_code, "
                 + " fund_name "
                 + " from mkt_loan_parm "
                 + " where list_cond = 'Y' "
                 + " order by fund_code "
                 ;
      }
   else
      {
       wp.sqlCmd = " select "
                 + " fund_code, "
                 + " fund_name "
                 + " from mkt_loan_parm_t "
                 + " where list_cond = 'Y' "
                 + " order by fund_code "
                 ;
      }

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("資料["+s1+"]");
       return(1);
      }

   return(0);
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
   wp.colSet("ex_query_table", "2");
   wp.itemSet("ex_query_table", "2");

  return;
 }
// ************************************************************************
 String procDynamicDddwFundCode(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.fund_code as db_code, "
          + " max(b.fund_code||' '||b.fund_name) as db_desc "
          + " from " + s1 +" b "
          + " where   b.list_cond = 'Y' "
          + " group by b.fund_code "
          + " order by b.fund_code "
          ;

   return lsSql;
 }


// ************************************************************************

}  // End of class
