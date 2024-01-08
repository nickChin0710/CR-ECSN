/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/09/14  V1.00.06   Allen Ho      Initial                              *
* 112-02-18  V1.00.07  Machao     sync from mega & updated for project coding standard                        * 
* 112/06/05  V1.00.08   Grace Huang   comm_megalite_flag 更名為comm_banklite_flag                              *
***************************************************************************/
package mktq02;

import mktq02.Mktq6240Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq6240 extends BaseEdit
{
 private final String PROGNAME = "首刷禮活動回饋卡人明細查詢處理程式112-02-18  V1.00.07";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq6240Func func = null;
  String kk1;
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
              + sqlCol(wp.itemStr2("ex_active_code"), "a.active_code", "like%")
              + sqlCol(wp.itemStr2("ex_active_type"), "a.active_type", "like%")
              + sqlChkEx(wp.itemStr2("ex_id_no"), "1", "")
              + sqlCol(wp.itemStr2("ex_card_no"), "a.card_no", "like%")
              + sqlCol(wp.itemStr2("ex_purchase_flag"), "a.purchase_flag", "like%")
              + sqlStrend(wp.itemStr2("ex_issue_date_s"), wp.itemStr2("ex_issue_date_e"), "a.issue_date")
              + sqlChkEx(wp.itemStr2("ex_error_flag"), "3", "")
              + sqlStrend(wp.itemStr2("ex_feedback_date_s"), wp.itemStr2("ex_feedback_date_e"), "a.feedback_date")
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
               + "'' as id_no,"
               + "'' as chi_name,"
               + "a.card_no,"
               + "a.issue_date,"
               + "a.group_code,"
               + "a.active_code,"
               + "a.match_active_seq,"
               + "a.active_type,"
               + "decode(active_type,'1',beg_tran_bp,'2',beg_tran_amt,'3',tran_pt,spec_gift_cnt) as feedback_amt,"
               + "decode(PURCHASE_FLAG,'Y','1','X','2','',' ',decode(proc_FLAG,'Y','3','4')) as purchase_flag,"
               + "'' as error_flag,"
               + "decode(error_code,'00','',ERROR_CODE||'-'||error_desc)  as error_desc,"
               + "a.feedback_date,"
               + "a.id_p_seqno,"
               + "a.proc_flag,"
               + "a.error_code";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by active_code,issue_date,id_p_seqno,acct_type"
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
  commGroupCode("comm_group_code");
  commActiveCode("comm_active_code");

  commActiveType1("comm_active_type");
  commCheckFlag("comm_purchase_flag");
  commErrorFlag("comm_error_flag");

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
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.id_p_seqno as id_p_seqno,"
               + "a.p_seqno as p_seqno,"
               + "a.active_code,"
               + "c.id_no as id_no,"
               + "c.chi_name as chi_name,"
               + "a.acct_type,"
               + "a.card_no,"
               + "a.group_code,"
               + "a.card_type,"
               + "a.card_note,"
               + "a.issue_date,"
               + "a.execute_date,"
               + "a.last_execute_date,"
               + "a.feedback_date,"
               + "a.match_active_seq,"
               + "a.active_type,"
               + "a.bonus_type,"
               + "a.beg_tran_bp,"
               + "a.group_type,"
               + "a.prog_code,"
               + "a.prog_s_date,"
               + "a.prog_e_date,"
               + "a.gift_no,"
               + "a.tran_pt,"
               + "a.fund_code,"
               + "a.beg_tran_amt,"
               + "a.spec_gift_no,"
               + "a.spec_gift_cnt,"
               + "a.sms_nopurc_date,"
               + "a.sms_nopurc_flag,"
               + "a.sms_half_date,"
               + "a.sms_half_flag,"
               + "a.sms_send_date,"
               + "a.sms_send_flag,"
               + "a.multi_fb_type,"
               + "a.record_flag,"
               + "a.active_seq,"
               + "a.record_group_no,"
               + "a.record_no,"
               + "a.error_code,"
               + "a.error_desc,"
               + "a.proc_flag,"
               + "a.proc_date,"
               + "a.purchase_flag,"
//               + "a.megalite_flag,"      //A.MEGALITE_FLAG" is not valid in the context where it is used.
               + "a.selfdeduct_flag,"
               + "a.linebc_flag,"
               + "a.mod_date,"
               + "a.mod_desc,"
               + "a.crt_user,"
               + "a.crt_date,"
               + "a.mod_pgm,"
               + "to_char(a.mod_time,'yyyymmdd') as mod_time";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
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
  commActiveType("comm_active_type");
  commNopurc("comm_sms_nopurc_flag");
  commHalf("comm_sms_half_flag");
  commSend("comm_sms_send_flag");
  commFbType("comm_multi_fb_type");
  commProgFlag("comm_proc_flag");
  commCheckFlag1("comm_purchase_flag");
  commCheckFlag2("comm_banklite_flag");
  commCheckFlag3("comm_selfdeduct_flag");
  commCheckFlag4("comm_linebc_flag");
  commActiveCode("comm_active_code");
  commAcctType("comm_acct_type");
  commGroupCode("comm_group_code");
  commCardType("comm_card_type");
  commCardNote("comm_card_note");
  commBonusType("comm_bonus_type");
  commGiftNo("comm_gift_no");
  commFundCode("comm_fund_code");
  commSpecGiftNo("comm_spec_gift_no");
  commGroupNo("comm_record_group_no");
  checkButtonOff();
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktq02.Mktq6240Func func =new mktq02.Mktq6240Func(wp);

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
       if ((wp.respHtml.equals("mktq6240")))
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
      alertErr("身份證號與活動代碼,卡號三者不可同時空白");
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
      alertErr(" 查無資料 !");
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
      return " and id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
     }
  if (sqCond.equals("3"))
     {
      if (empty(wp.itemStr2("ex_error_flag"))) return "";
      if (wp.itemStr2("ex_error_flag").equals("1"))
          return " and proc_flag ='Y' and error_code ='00' ";
      else if (wp.itemStr2("ex_error_flag").equals("2"))
          return " and proc_flag ='Y' and error_code !='00' " ;
      else if (wp.itemStr2("ex_error_flag").equals("3"))
          return " and proc_flag ='N' and error_code ='00' ";
      else if (wp.itemStr2("ex_error_flag").equals("4"))
          return " and proc_flag ='X' " ;
     }

  return "";
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
 public void commCardType(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " name as column_name "
            + " from ptr_card_type "
            + " where 1 = 1 "
            + " and   card_type = '"+wp.colStr(ii,"card_type")+"'"
            ;
       if (wp.colStr(ii,"card_type").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_name"); 
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
            + " and   wf_id = '"+wp.colStr(ii,"bonus_type")+"'"
            ;
       if (wp.colStr(ii,"bonus_type").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commGiftNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name "
            + " from mkt_spec_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            + " and   gift_group='1'   "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
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
            + " from vmkt_fund_name "
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
 public void commSpecGiftNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name "
            + " from mkt_spec_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,"spec_gift_no")+"'"
            + " and   gift_group = '1' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commGroupNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " record_group_name as column_record_group_name "
            + " from web_record_group "
            + " where 1 = 1 "
            + " and   record_group_no = '"+wp.colStr(ii,"record_group_no")+"'"
            ;
       if (wp.colStr(ii,"record_group_no").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_record_group_name"); 
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
 public void commActiveType(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"紅利點數","現金回饋代碼","豐富點數","贈　　品"};
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
 public void commNopurc(String s1) throws Exception 
 {
  String[] cde = {"Y","A","N","X","F"};
  String[] txt = {"已發送簡訊","有消費不發送","待處理","參數未設定","電話號碼錯誤"};
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
 public void commHalf(String s1) throws Exception 
 {
  String[] cde = {"Y","B","N","X","F"};
  String[] txt = {"已發送簡訊","消費未過半","待處理","參數未設定","電話號碼錯誤"};
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
 public void commSend(String s1) throws Exception 
 {
  String[] cde = {"Y","N","X","F"};
  String[] txt = {"已發送簡訊","待處理","參數未設定","電話號碼錯誤"};
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
 public void commFbType(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"1.單一贈品","2.多贈品回饋"};
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
 public void commProgFlag(String s1) throws Exception 
 {
  String[] cde = {"Y","N","X"};
  String[] txt = {"已處理","尚未回饋處理","不回饋"};
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
 public void commCheckFlag1(String s1) throws Exception 
 {
  String[] cde = {"Y","N","X"};
  String[] txt = {"符合","不符合","不檢核"};
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
 public void commCheckFlag2(String s1) throws Exception 
 {
  String[] cde = {"Y","N","X"};
  String[] txt = {"符合","不符合","不檢核"};
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
 public void commCheckFlag3(String s1) throws Exception 
 {
  String[] cde = {"Y","N","X"};
  String[] txt = {"符合","不符合","不檢核"};
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
 public void commCheckFlag4(String s1) throws Exception 
 {
  String[] cde = {"Y","N","X"};
  String[] txt = {"符合","不符合","不檢核"};
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
 public void commActiveType1(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"紅利","現金回饋","豐富點","贈品"};
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
 public void commCheckFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"已達標","不檢核","不符合","尚未符合"};
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
 public void commErrorFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2","3","4"};
  String[] txt = {"1.已回饋","2.不回饋","3.尚未回饋","4.資格不符"};
  String columnData="";
  String s2 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       if (wp.colStr(ii,"proc_flag").equals("Y"))
          {
           if (wp.colStr(ii,"error_code").equals("00")) 
              s2="1";
           else
              s2="2";
          }
       else if (wp.colStr(ii,"proc_flag").equals("N"))
          {
           if (wp.colStr(ii,"error_code").equals("00")) 
              s2="3";
          }
       else s2="4";
       for (int inti=0;inti<cde.length;inti++)
         {
          if (s2.equals(cde[inti]))
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

}  // End of class
