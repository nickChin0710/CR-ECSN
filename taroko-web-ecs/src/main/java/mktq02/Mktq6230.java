/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/11  V1.00.07   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktq02;

import mktq02.Mktq6230Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq6230 extends BaseEdit
{
 private final String PROGNAME = "專案回饋金明細查詢處理程式111-11-30  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq6230Func func = null;
  String kk1;
  String orgTabName = "mkt_loan";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String   batch_no     = "";
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
  if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr2("ex_branch_no"), "a.branch_no")
              + sqlCol(wp.itemStr2("ex_id_no"), "a.id_no", "like%")
              + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code")
              + sqlCol(wp.itemStr2("ex_trans_seqno"), "a.trans_seqno", "like%")
              + sqlCol(wp.itemStr2("ex_batch_no"), "a.batch_no", "like%")
              + sqlChkEx(wp.itemStr2("ex_err_code"), "1", "")
              + sqlStrend(wp.itemStr2("ex_apr_date_s"), wp.itemStr2("ex_apr_date_e"), "a.apr_date")
              + " and batch_no!=''     "
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
               + "a.batch_no,"
               + "a.rec,"
               + "a.branch_no,"
               + "a.id_no,"
               + "a.period,"
               + "a.fund_code,"
               + "a.acct_month,"
               + "a.rtn_amt,"
               + "a.err_code,"
               + "a.tran_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by batch_no,rec"
                ;

  pageQuery();
  listWkdata();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commBranchNo("comm_branch_no");
  commFundCode("comm_fund_code");

  commErrCode("comm_err_code");

  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 void listWkdata()  throws Exception
 {
  wp.colSet("ex_total_msg1" , "");
  String sql1 = "";
  sql1 = " select count(*) as tot_cnt, "
       + "        sum(decode(err_code,'0',1,0)) as right_cnt,"
       + "        sum(decode(err_code,'0',0,1)) as fail_cnt,"
       + "        sum(decode(err_code,'0',rtn_amt,0)) as right_amt, "
       + "        sum(decode(err_code,'0',0,rtn_amt)) as fail_amt "
       + " from mkt_loan a "
       + " where 1 = 1 "
       + sqlCol(wp.itemStr2("ex_branch_no"), "a.branch_no")
       + sqlCol(wp.itemStr2("ex_batch_no"), "a.batch_no", "like%")
       + sqlCol(wp.itemStr2("ex_id_no"), "a.id_no", "like%")
       + sqlCol(wp.itemStr2("ex_fund_code"), "a.fund_code")
       + sqlCol(wp.itemStr2("ex_trans_seqno"), "a.trans_seqno", "like%")
       + sqlChkEx(wp.itemStr2("ex_err_code"), "1", "")
       + sqlStrend(wp.itemStr2("ex_apr_date_s"), wp.itemStr2("ex_apr_date_e"), "a.apr_date")
       ;

  sqlSelect(sql1);

  int totalRow = sqlRowNum;
  if (totalRow==0) return;

  wp.colSet("ex_total_msg1", "累計筆數: " + String.format("%,d",(int)sqlNum("tot_cnt"))+ "　"
                            + "成功筆數: " + String.format("%,d",(int)sqlNum("right_cnt"))+ "　"
                            + "回饋金額：" + String.format("%,d",(int)sqlNum("right_amt"))+ "　"
                            + "失敗筆數: " + String.format("%,d",(int)sqlNum("fail_cnt"))+ "　"
                            + "失敗金額：" + String.format("%,d",(int)sqlNum("fail_amt"))+ "　"
                            );

   return;

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
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.batch_no,"
               + "a.rec,"
               + "a.file_name,"
               + "a.acct_month,"
               + "a.acct_type,"
               + "a.fund_code,"
               + "a.id_no,"
               + "a.id_no_code,"
               + "a.chi_name,"
               + "a.branch_no,"
               + "a.in_type,"
               + "a.period,"
               + "a.rtn_amt,"
               + "a.err_code,"
               + "a.process_flag,"
               + "a.file_flag,"
               + "a.pgm_memo,"
               + "a.tran_seqno,"
               + "a.trans_seqno,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user,"
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
  dataread_wkdata();
  commErrCode("comm_err_code");
  commAcctType("comm_acct_type");
  commFundCode("comm_fund_code");
  commIdName("comm_id_no");
  commBranchNo("comm_branch_no");
  checkButtonOff();
 }
// ************************************************************************
 void dataread_wkdata() throws Exception
 {
   if (wp.colStr("err_code").equals("3"))
      wp.colSet("id_p_seqno" , "");

 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktq02.Mktq6230Func func =new mktq02.Mktq6230Func(wp);

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
       if ((wp.respHtml.equals("mktq6230")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_branch_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_branch_no");
             }
          this.dddwList("dddw_extern_id"
                 ,"mkt_extern_unit"
                 ,"trim(extern_id)"
                 ,"trim(extern_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_fund_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_fund_code");
             }
          lsSql = "";
          if (wp.colStr("ex_branch_no").length()!=0)
             {
              lsSql =  procDynamicDddwFundCode(wp.colStr("ex_branch_no"));

              wp.optionKey = wp.colStr("ex_fund_code");
              dddwList("dddw_fund_code", lsSql);
             }
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
  if ((itemKk("ex_fund_code").length()==0)&&
      (itemKk("ex_id_no").length()==0)&&
      (itemKk("ex_apr_date_s").length()==0)&&
      (itemKk("ex_apr_date_e").length()==0))
     {
      alertErr("基金代碼與匯入日期,身分證號 不可同時空白");
      return(1);
     }
  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
     {
      if (wp.itemStr2("ex_err_code").length()==0) return ""; 
      if (wp.itemStr2("ex_err_code").equals("Y"))  
          return "and err_code = '0' ";
      else
          return "and err_code != '0' ";
    }

  return "";
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
 public void commIdName(String s1) throws Exception 
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
 public void commBranchNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " extern_name as column_extern_name "
            + " from mkt_extern_unit "
            + " where 1 = 1 "
            + " and   extern_id = '"+wp.colStr(ii,"branch_no")+"'"
            ;
       if (wp.colStr(ii,"branch_no").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_extern_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commErrCode(String s1) throws Exception 
 {
  String[] cde = {"0","A","3"};
  String[] txt = {"正常轉入","無有效卡","非本行卡友"};
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
                    wp.itemStr2("ax_win_branch_no"))!=0) 
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
   if (s1.length()==0) return(0);
   wp.sqlCmd = " select "
             + " a.fund_code , "
             + " a.fund_name "
             + " from mkt_loan_parm a,"
             + "      (select extern_id,extern_name,d.data_code2 "
             + "       from mkt_extern_unit c,mkt_bn_data d"
             + "       where d.table_name = 'MKT_EXTERN_UNIT' "
             + "       and   c.extern_id  = d.data_key  "
             + "       and   c.extern_id  = '" + s1 + "' "
             + "       and   d.data_type    = '1') b "
             + " where a.fund_code = b.data_code2 "
             + " order by fund_code "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("單位代號["+s1+"]查無資料");
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
  return;
 }
// ************************************************************************
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
// ************************************************************************
 String procDynamicDddwFundCode(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " a.fund_code as db_code, "
          + " a.fund_code||' '||a.fund_name as db_desc "
          + " from mkt_loan_parm a,"
          + "      (select extern_id,extern_name,d.data_code2 "
          + "       from mkt_extern_unit c,mkt_bn_data d"
          + "       where d.table_name = 'MKT_EXTERN_UNIT' "
          + "       and   c.extern_id  = d.data_key  "
          + "       and   c.extern_id  = '" + s1 + "' "
          + "       and   c.disable_flag != 'Y'  "
          + "       and   d.data_type    = '1') b "
          + " where a.fund_code = b.data_code2 "
          + " and   a.stop_flag != 'Y' "
          + " order by b.extern_id,a.fund_code "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
