/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/08  V1.00.01   Allen Ho      Initial                              *
* 111/07/20  V1.00.02   machao        新增程式mktq3225                                                                        *
***************************************************************************/
package mktq02;

import mktq02.Mktq3225Func;
import ofcapp.BaseEdit;
import ofcapp.AppMsg;
import java.util.Arrays;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq3225 extends BaseEdit
{
 private  String PROGNAME = "高鐵車廂升等每月對帳查詢處理程式111/07/20 V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq3225Func func = null;
  String kk1;
  String org_tab_name = "mkt_thsr_redem";
  String control_tab_name = "";
  int qFrom=0;
  String tran_seqStr = "";
  String   batch_no     = "";
  int error_cnt=0,rec_cnt=0,notify_cnt=0,colNum=0;
  int[]  datachk_cnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String   up_group_type= "0";

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

  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 "
              + sql_chk_ex(wp.itemStr2("ex_crt_date"), "2", "")
              + sqlCol(wp.itemStr2("ex_pay_type"), "a.pay_type")
              + sql_chk_ex(wp.itemStr2("ex_group_type"), "5", "")
              + sql_chk_ex(wp.itemStr2("ex_error_code"), "3", "")
              + sql_chk_ex(wp.itemStr2("ex_trans_seqno"), "1", "")
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
     control_tab_name = wp.colStr("org_tab_name");
  else
     control_tab_name = org_tab_name;

  wp.pageControl();

  wp.selectSQL = " "
               + "hex(a.rowid) as rowid, "
               + "a.trans_date,"
               + "a.serial_no,"
               + "a.trans_type,"
               + "a.card_no,"
               + "a.pay_type,"
               + "decode(pay_type,'1',DEDUCT_BP,0) as deduct_bp,"
               + "decode(pay_type,'1',0,deduct_amt) as deduct_amt,"
               + "a.error_desc,"
               + "a.id_p_seqno";

  wp.daoTable = control_tab_name + " a "
              ;
  wp.whereOrder = " "
                + " order by trans_date,serial_no"
                ;

  pageQuery();
  list_wkdata();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }


  comm_trans_type("comm_trans_type");
  comm_pay_type("comm_pay_type");

  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 void list_wkdata()  throws Exception
 {
  int total_cnt=0,total_row=wp.selectCnt;

  wp.colSet("ex_total_msg1" , "");
  wp.colSet("ex_total_msg2" , "");
  wp.colSet("ex_total_msg3" , "");
  String  sql1 = "";
  sql1 = " select sum(decode(trans_type,'P',decode(pay_type,'1', 1,0),0)) as total_p_pnt_row,"
       + "        sum(decode(trans_type,'P',decode(pay_type,'2', 1,0),0)) as total_p_amt_row,"
       + "        sum(decode(trans_type,'P',decode(pay_type,'3', 1,0),0)) as total_p_sub_row,"
       + "        sum(decode(trans_type,'P',decode(pay_type,'3', 1,0),decode(pay_type,'3',- 1,0))) as total_sub_row,"
       + "        sum(decode(trans_type,'P', 1,0)) as total_p_row,"
       + "        sum(decode(trans_type,'R',decode(pay_type,'1', 1,0),0)) as total_r_pnt_row,"
       + "        sum(decode(trans_type,'R',decode(pay_type,'2', 1,0),0)) as total_r_amt_row,"
       + "        sum(decode(trans_type,'R',decode(pay_type,'3', 1,0),0)) as total_r_sub_row,"
       + "        sum(decode(trans_type,'R', 1,0)) as total_r_row,"
       + "        sum(decode(trans_type,'R', -1,1)) as total_row,"
       + "        sum(decode(trans_type,'P',decode(pay_type,'2',0,deduct_bp),0)) as p_deduct_bp, "
       + "        sum(decode(trans_type,'R',decode(pay_type,'2',0,deduct_bp),0)) as r_deduct_bp, "
       + "        sum(decode(trans_type,'R',decode(pay_type,'2',0,deduct_bp*-1),"
       + "                                  decode(pay_type,'2',0,deduct_bp))) as deduct_bp, "
       + "        sum(decode(trans_type,'P',decode(pay_type,'2',deduct_amt,0),0)) as p_deduct_amt, "
       + "        sum(decode(trans_type,'R',decode(pay_type,'2',deduct_amt,0),0)) as r_deduct_amt, "
       + "        sum(decode(trans_type,'R',decode(pay_type,'2',deduct_amt*-1,0),"
       + "                                  decode(pay_type,'2',deduct_amt,0))) as deduct_amt, "
       + "        sum(decode(trans_type,'P',decode(pay_type,'1',trans_amount,0),0)) as total_p_pnt_amt, "
       + "        sum(decode(trans_type,'P',decode(pay_type,'2',trans_amount,0),0)) as total_p_amt_amt, "
       + "        sum(decode(trans_type,'P',decode(pay_type,'3',trans_amount,0),0)) as total_p_sub_amt, "
       + "        sum(decode(trans_type,'P',trans_amount,0)) as total_p_amt, "
       + "        sum(decode(trans_type,'R',decode(pay_type,'1',trans_amount*-1,0),0)) as total_r_pnt_amt, "
       + "        sum(decode(trans_type,'R',decode(pay_type,'2',trans_amount*-1,0),0)) as total_r_amt_amt, "
       + "        sum(decode(trans_type,'R',decode(pay_type,'3',trans_amount*-1,0),0)) as total_r_sub_amt, "
       + "        sum(decode(trans_type,'R',trans_amount*-1,0)) as total_r_amt, "
       + "        sum(trans_amount) as total_amt, "
       + "        sum(decode(error_code,'00',0,1)) as error_cnt "
       + " from mkt_thsr_redem a  "
       + " where 1 = 1 "
       + sql_chk_ex(wp.itemStr2("ex_crt_date"), "2", "")
       + sqlCol(wp.itemStr2("ex_pay_type"), "a.pay_type", "like%")
       + sql_chk_ex(wp.itemStr2("ex_group_type"), "5", "")
       + sql_chk_ex(wp.itemStr2("ex_error_code"), "3", "")
       + sql_chk_ex(wp.itemStr2("ex_trans_seqno"), "1", "")
       ;

  sqlSelect(sql1);

  total_row = sqlRowNum;
  if (total_row==0) return;

  wp.colSet("ex_total_msg1",  
              "購票 筆數: " +  toNosb(String.format("%d",(int)sqlNum("total_p_row")),5)+ "　," 
            + "車票金額: " + toNosb(String.format("%,d", (int)sqlNum("total_p_amt")),9)+  "　,"
            + "扣點: " + toNosb(String.format("%,d", (int)sqlNum("p_deduct_bp")),9)+  "　,"
            + "加檔: " + toNosb(String.format("%,d", (int)sqlNum("p_deduct_amt")),9)+  "　,"
            + "減免筆數: " +  toNosb(String.format("%d", (int)sqlNum("total_p_sub_row")),5)
            );
  wp.colSet("ex_total_msg2",  
              "退票 筆數: " +  toNosb(String.format("%d",(int)sqlNum("total_r_row")),5)+ "　," 
            + "車票金額: " + toNosb(String.format("%,d", (int)sqlNum("total_r_amt")),9)+  "　,"
            + "扣點: " + toNosb(String.format("%,d", (int)sqlNum("r_deduct_bp")),9)+  "　,"
            + "加檔: " + toNosb(String.format("%,d", (int)sqlNum("r_deduct_amt")),9)+  "　,"
            + "減免筆數: " +  toNosb(String.format("%d", (int)sqlNum("total_r_sub_row")),5)
            );
  wp.colSet("ex_total_msg3",  
              "累計 筆數: " +  toNosb(String.format("%d",(int)sqlNum("total_row")),5)+ "　," 
            + "車票金額: " + toNosb(String.format("%,d", (int)sqlNum("total_amt")),9)+  "　,"
            + "扣點: " + toNosb(String.format("%,d", (int)sqlNum("deduct_bp")),9)+  "　,"
            + "加檔: " + toNosb(String.format("%,d", (int)sqlNum("deduct_amt")),9)+  "　,"
            + "減免筆數: " +  toNosb(String.format("%d", (int)sqlNum("total_sub_row")),5)
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
  if (control_tab_name.length()==0)
     {
      if (wp.colStr("control_tab_name").length()==0)
         control_tab_name=org_tab_name;
      else
         control_tab_name=wp.colStr("control_tab_name");
     }
  else
     {
      if (wp.colStr("control_tab_name").length()!=0)
         control_tab_name=wp.colStr("control_tab_name");
     }
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + "a.serial_no,"
               + "a.file_date,"
               + "a.trans_type,"
               + "a.trans_date,"
               + "a.org_trans_date,"
               + "a.id_p_seqno,"
               + "'' as id_no,"
               + "a.major_card_no,"
               + "a.group_code,"
               + "a.acct_type,"
               + "a.authentication_code,"
               + "a.pnr,"
               + "a.trans_amount,"
               + "a.depart_date,"
               + "a.pay_type,"
               + "a.deduct_bp,"
               + "a.error_desc,"
               + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
               + "a.mod_pgm";

  wp.daoTable = control_tab_name + " a "
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
      alertErr2("查無資料, key= "+"["+ kk1 + "]");
      return;
     }
  comm_trans_type("comm_trans_type");
  comm_pay_type("comm_pay_type");
  comm_id_no("comm_id_no");
  comm_group_code("comm_group_code");
  comm_acct_type("comm_acct_type");
  check_button_off();
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  Mktq3225Func func =new mktq02.Mktq3225Func(wp);

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
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String ls_sql ="";
  try {
       if ((wp.respHtml.equals("mktq3225")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_trans_seqno").length()>0)
             {
             wp.optionKey = wp.colStr("ex_trans_seqno");
             }
          ls_sql = "";

          if (wp.itemStr2("ex_crt_date_s").length()==0)
             {
              if (wp.itemStr2("ex_crt_date_e").length()!=0)
                 wp.colSet("ex_crt_date_s" , comm.nextMonthDate(wp.itemStr2("ex_crt_date_e"),-12));
              else
                 wp.colSet("ex_crt_date_s" , comm.nextMonthDate(wp.sysDate,-12));
              }
          if (wp.itemStr2("ex_crt_date_e").length()==0)
             wp.colSet("ex_crt_date_e" , wp.sysDate);
             
          ls_sql =  proc_dynamic_dddw_trans_seqno(wp.colStr("ex_crt_date_s"),wp.colStr("ex_crt_date_e"));

          wp.optionKey = wp.itemStr2("ex_trans_seqno");
          dddwList("dddw_trans_seqno", ls_sql);
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
  if (wp.itemStr2("ex_trans_seqno").length()==0)
     {
      alertErr2("請款資料必須選擇");
      return(1);
     }

  return(0);
 }
// ************************************************************************
 public String sql_chk_ex(String ex_col,String sq_cond,String file_ext) throws Exception
 {
  if (sq_cond.equals("1"))
     {
      if (empty(wp.itemStr2("ex_trans_seqno"))) return "";
      return " and trans_seqno = '"+wp.itemStr2("ex_trans_seqno")+"'";
     }

  if (sq_cond.equals("3"))
     {
      if (empty(wp.itemStr2("ex_error_code"))) return "";
      if (wp.itemStr2("ex_error_code").equals("Y"))
         return " and error_code = '00' ";
      else
         return " and error_code != '00' ";
     }
  if (sq_cond.equals("2"))
     {
      if ((wp.itemStr2("ex_crt_date_s").length()==0)&&
          (wp.itemStr2("ex_crt_date_e").length()==0)) return "";

      if (wp.itemStr2("ex_crt_date_s").length()==0)
         {
          if (wp.itemStr2("ex_crt_date_e").length()!=0)
             wp.colSet("ex_crt_date_s" , comm.nextMonthDate(wp.itemStr2("ex_crt_date_e"),-12));
          else
             wp.colSet("ex_crt_date_s" , comm.nextMonthDate(wp.sysDate,-12));
         }
      if (wp.itemStr2("ex_crt_date_e").length()==0)
         wp.colSet("ex_crt_date_e" , wp.sysDate);

      return " and crt_date between '"
             + wp.colStr("ex_crt_date_s")
             + "' and '"
             + wp.colStr("ex_crt_date_e")
             + "' ";
     }

  if (sq_cond.equals("5"))
     {
      if (empty(wp.itemStr2("ex_group_type"))) return "";
      if (wp.itemStr2("ex_group_type").equals("1"))
         return " and card_mode in ("
              + "     select card_mode "
              + "     from mkt_thsr_upmode "
              + "     where   add_file_flag = 'Y') ";
      else
         return " and card_mode not in ("
              + "     select card_mode "
              + "     from mkt_thsr_upmode "
              + "     where  add_file_flag = 'Y') ";
     }



  return "";
 }
// ************************************************************************
 public void comm_id_no(String s1) throws Exception 
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
 public void comm_group_code(String s1) throws Exception 
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
 public void comm_acct_type(String s1) throws Exception 
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
 public void comm_trans_type(String s1) throws Exception 
 {
  String[] cde = {"P","R"};
  String[] txt = {"購票","退票"};
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
 public void comm_pay_type(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"扣點","扣款","減免"};
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
 public void wf_ajax_func_2(TarokoCommon wr) throws Exception
 {
  String ajaxj_trans_seqno = "";
  super.wp = wr;


  if (select_ajax_func_2_0(
                    wp.itemStr2("ax_win_crt_date_s"),wp.itemStr2("ax_win_crt_date_e"))!=0) 
     {
      wp.addJSON("ajaxj_trans_seqno", "");
      wp.addJSON("ajaxj_type_name", "");
      return;
     }

  wp.addJSON("ajaxj_trans_seqno", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_trans_seqno", sqlStr(ii, "trans_seqno"));
  wp.addJSON("ajaxj_type_name", "");
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_type_name", sqlStr(ii, "type_name"));
 }
// ************************************************************************
 int select_ajax_func_2_0(String s1,String s2) throws Exception
  {
   if ((s1.length()==0)||(s2.length()==0)) return(0);
   wp.sqlCmd = " select "
             + " trans_seqno, "
             + " error_desc as type_name "
             + " from mkt_uploadfile_ctl "
             + " where file_type = 'MKT_THSR_REDEM' "
             + " and   crt_date between '" + s1 + "' and '" + s2 +"' " 
             + " and   apr_flag ='Y' " 
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr2("查無請款上傳資料["+wp.sqlCmd+"]");
       return(1);
      }

   return(0);
 }

// ************************************************************************
 public void check_button_off() throws Exception
  {
  return;
 }
// ************************************************************************
 @Override
 public void initPage()
 {
  return;
 }
  public String toDateFormat(String date)
  {
   if (date.length()==6)
       return date.substring(0,4) + "/"+date.substring(4,6);
   else if (date.length()==8)
       return date.substring(0,4) + "/"+date.substring(4,6)+"/"+date.substring(6,8);
   else return date;
  }
// ************************************************************************
 public String toNosb(String stra,int stralen)
 {
  if (stra.length()>=stralen) return stra;
  String retStra="";
  for (int inti=0;inti<(stralen-stra.length());inti++)
     retStra = retStra + "&nbsp;";
  return  retStra + stra;
 }
// ************************************************************************
 String proc_dynamic_dddw_trans_seqno(String s1,String s2)  throws Exception
 {
   String ls_sql = "";

 ls_sql = " select "
          + " trans_seqno as db_code, "
          + " trans_seqno as db_desc "
          + " from mkt_uploadfile_ctl "
          + " where crt_date between  '" + s1 +"' "
          + "                and      '" + s2 +"' "
          + " and   file_type  = 'MKT_THSR_REDEM' "
          + " and   apr_Date !='' "
          + " order by crt_date,trans_seqno  "
          ;

   return ls_sql;
 }
// ************************************************************************



// ************************************************************************

}  // End of class
