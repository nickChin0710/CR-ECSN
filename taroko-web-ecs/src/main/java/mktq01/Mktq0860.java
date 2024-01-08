/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/01/04  V1.00.01   Allen Ho      Initial                              *
* 111/02/26  V1.00.02   Zuwei Su      naming rule update                   *
* 111/02/27  V1.00.03   Zuwei Su      dddwSelect 執行前參數未clear         *
* 112/03/02  V1.00.04   Zuwei Su      產生媒體檔筆數不正確，下載圖片顯示問題*
* 112/03/03  V1.00.04   Zuwei Su      update queryread & mediafileProcess sql,以身分證號查詢, 出現err    *
* 112/04/24  V1.00.05   Grace Huang   commLotteryType()增'3.一般名單'    
* 112-04-25  V1.00.06   machao       明細增’活動說明’ 欄位                                                                  *
* 112/07/06  V1.00.07   Grace Huang   isnull(), 改為NVL()                  *    
* 112/08/25  V1.00.08   Zuwei Su      "ID_P_SEQNO" is ambiguous                  *    
* 112/10/17  V1.00.09   Zuwei Su      明細增持卡人相關欄位 (卡號、ID、姓名)       
* 112/12/25  V1.00.10   Ryan          修改 left join MKT_CHANNEL_BILL  *   
***************************************************************************/
package mktq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0860 extends BaseEdit
{
 private final String PROGNAME = "通路活動卡人回饋查詢作業處理程式112/10/17 V1.00.09";
    busi.DataSet ds1 =new busi.DataSet(); 
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq01.Mktq0860Func func = null;
  String kk1;
  String orgTabName = "mkt_channel_list";
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
  static String sqlStr = "";
  String  newLine="\n";
  int fo=0;

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
  else if (eqIgno(wp.buttonCode, "MEDIAFILE"))
     {/* 產生媒體檔 */
      strAction = "U";
      mediafileProcess();
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
              + sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
              + sqlCol(wp.itemStr("ex_proc_flag"), "a.proc_flag")
              + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
              + sqlChkEx(wp.itemStr("ex_feedback_type"), "3", "")
              + sqlCol(wp.itemStr("ex_sms_flag"), "a.sms_flag")
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
              + "a.active_code, "
              + "d.active_name, "
              + "NVL(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
              + "NVL(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
              + "a.bonus_pnt,"
              + "a.bonus_date,"
              + "a.fund_amt,"
              + "a.fund_date,"
              + "a.gift_int,"
              + "a.lottery_int, "
              + "a.lottery_type, "
              + "a.proc_date, "
              + "a.acct_type,"
              + "a.vd_flag,"
              + "a.id_p_seqno,"
              + "e.card_no as crd_card_no,"
              + "g.id_no as crd_id_no,"
              + "g.chi_name as crd_chi_name ";

  wp.daoTable = controlTabName + " a "
          + " left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
          + " left join dbc_idno c on a.id_p_seqno = c.id_p_seqno "
          + " left join MKT_CHANNEL_PARM d on a.active_code = d.active_code "
//          + " left join MKT_CHANNEL_BILL e on e.ACTIVE_CODE=a.ACTIVE_CODE and e.p_seqno = a.p_seqno and e.error_code='00' "
          + " left join (select card_no,ACTIVE_CODE,p_seqno,max(REFERENCE_NO) from MKT_CHANNEL_BILL where error_code='00' group by card_no,ACTIVE_CODE,p_seqno) e "
          + " on e.ACTIVE_CODE=a.ACTIVE_CODE and e.p_seqno = a.p_seqno  "
          + " left join crd_card f on f.CARD_NO=e.CARD_NO "
          + " left join crd_idno g on g.ID_P_SEQNO=f.ID_P_SEQNO ";
  wp.whereOrder = " "
                + " order by a.active_code,decode(a.acct_type,'90',c.id_no,b.id_no)"
                ;

  pageQuery();
  listWkdataQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }


  commLotteryType("comm_lottery_type");

  wp.setPageValue();
 }
// ************************************************************************
 void listWkdataQuery()  throws Exception
 {
   wp.itemSet("bb_down_file_name" ,  wp.itemStr("ex_active_code")+"_"+wp.sysDate+wp.sysTime+".txt");
   wp.colSet("bb_down_file_name" ,  wp.itemStr("ex_active_code")+"_"+wp.sysDate+wp.sysTime+".txt");

//   String sql1 = "";
//   for (int ii = 0; ii < wp.selectCnt; ii++)
//      {
//       if (wp.colStr(ii , "acct_type").equals("90"))
//          {
//           sql1 = "select "
//                + " id_no, "
//                + " chi_name "
//                + " from dbc_idno "
//                + " where id_p_seqno ='" + wp.colStr(ii , "id_p_seqno") + "' "
//                + "order by id_no_code "
//                ;
//          }
//       else
//          {
//           sql1 = "select "
//                + " id_no, "
//                + " chi_name "
//                + " from crd_idno "
//                + " where id_p_seqno ='" + wp.colStr(ii , "id_p_seqno") + "' "
//                + "order by id_no_code "
//                ;
//          }
//
//       sqlSelect(sql1);
//       if (sqlRowNum <= 0) continue;
//
//       wp.log("acct_type=[" + wp.colStr(ii , "acct_type") +"]["+ sql1 +"]");
//       wp.colSet(ii, "id_no"    , sqlStr("id_no"));
//       wp.colSet(ii, "chi_name" , sqlStr("chi_name"));
//      }
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
               + "a.id_p_seqno as id_p_seqno,"
               + "a.vd_flag as vd_flag,"
               + "a.active_code,"
               + "a.vd_flag,"
               + "a.acct_type,"
               + "'' as id_no,"
               + "'' as chi_name,"
               + "a.acct_no,"
               + "a.stmt_cycle,"
               + "a.bonus_type,"
               + "a.bonus_pnt,"
               + "a.bonus_date,"
               + "a.fund_code,"
               + "a.fund_amt,"
               + "a.fund_date,"
               + "a.spec_gift_no,"
               + "a.gift_int,"
               + "a.gift_amt,"
               + "a.lottery_type,"
               + "a.lottery_int,"
               + "a.sms_flag,"
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
  datareadWkdata();
  commVdFlag("comm_vd_flag");
  commLotteryType("comm_lottery_type");
  commProcFlag1("comm_sms_flag");
  commActiveCode("comm_active_code");
  commAcctType("comm_acct_type");
  commBonusType("comm_bonus_type");
  commFuncCode("comm_fund_code");
  commSpecGiftNo("comm_spec_gift_no");
  checkButtonOff();
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
   int ii=0;
   String sql1="";

   if (wp.colStr("acct_type").equals("90"))
      {
       sql1 = "select "
            + " id_no as id_no, "
            + " chi_name as chi_name "
            + " from dbc_idno "
            + " where id_p_seqno = '"+wp.colStr("id_p_seqno")+"'"
            ;
      }
   else
      {
       sql1 = "select "
            + " id_no as id_no, "
            + " chi_name as chi_name "
            + " from crd_idno "
            + " where id_p_seqno = '"+wp.colStr("id_p_seqno")+"'"
            ;
      }
   sqlSelect(sql1);
   wp.colSet( "id_no"    , sqlStr("id_no"));
   wp.colSet( "chi_name" , sqlStr("chi_name"));

 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktq01.Mktq0860Func func =new mktq01.Mktq0860Func(wp);

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
       if ((wp.respHtml.equals("mktq0860")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_active_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_active_code");
             }
          this.dddwList("dddw_active_code"
                 ,"mkt_channel_parm"
                 ,"trim(active_code)"
                 ,"trim(active_name)"
                 ," where active_code in (select active_code from mkt_channel_anal)");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
  if ((itemKk("ex_active_code").length()==0)&&
      (itemKk("ex_id_no").length()==0))
     {
      alertErr("身份證號與活動代號二者不可同時空白");
      return(1);
     }

  if (wp.itemStr("ex_id_no").length()>0)
     {
      String sql1 = "";

      sql1 = "select id_p_seqno "
           + "from crd_idno "
           + "where  id_no  =  '"+ wp.itemStr("ex_id_no").toUpperCase() +"'"
           ;
      sqlSelect(sql1);

      if (sqlRowNum <= 0)
          wp.colSet("ex_id_p_seqno","");
      else
         wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));

      sql1 = "select id_p_seqno as vd_id_p_seqno  "
           + "from dbc_idno "
           + "where  id_no  =  '"+ wp.itemStr("ex_id_no").toUpperCase() +"'"
           ;
      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         wp.colSet("ex_vd_id_p_seqno","");
      else
         wp.colSet("ex_vd_id_p_seqno",sqlStr("vd_id_p_seqno"));

      if ((sqlStr("vd_id_p_seqno").length()==0)&&
          (sqlStr("id_p_seqno").length()==0))
         {
          alertErr(" 查無此身分證號[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
          return(1);
         }

     }


  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("1"))
     {
      if (empty(wp.itemStr("ex_id_no"))) return "";
      if (wp.colStr("ex_id_p_seqno").length()==0)
          return " and a.id_p_seqno ='"+wp.colStr("ex_vd_id_p_seqno")+"' ";
      if (wp.colStr("ex_vd_id_p_seqno").length()==0)
          return " and a.id_p_seqno ='"+wp.colStr("ex_id_p_seqno")+"' ";
      return " and a.id_p_seqno in ('"+wp.colStr("ex_id_p_seqno")+"','"+wp.colStr("ex_vd_id_p_seqno")+"')";
     }

  if (sqCond.equals("3"))
     {
      if (empty(wp.itemStr("ex_feedback_type"))) return "";
      if (wp.itemStr("ex_feedback_type").equals("1"))
          return " and a.bonus_pnt != 0 ";
      else if (wp.itemStr("ex_feedback_type").equals("2"))
          return " and a.fund_amt != 0 ";
      else if (wp.itemStr("ex_feedback_type").equals("3"))
          return " and a.gift_int != 0 and spec_gift_no in (select gift_no from mkt_spec_gift where gift_type!='3' and gift_group = '2') ";
      else if (wp.itemStr("ex_feedback_type").equals("4"))
          return " and a.gift_int != 0 and spec_gift_no in (select gift_no from mkt_spec_gift where gift_type='3' and gift_group = '2') ";
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
            + " from mkt_channel_parm "
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
            + " from vmkt_acct_type "
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
 public void commFuncCode(String s1) throws Exception 
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
            + " and   gift_group = '2' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commVdFlag(String s1) throws Exception 
 {
  String[] cde = {"N","Y"};
  String[] txt = {"信用卡","Debit卡"};
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
 public void commLotteryType(String s1) throws Exception 
 {
  String[] cde = {"1","2","3"};
  String[] txt = {"抽獎名單","豐富點數","一般名單"};
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
 public void commProcFlag1(String s1) throws Exception 
 {
  String[] cde = {"N","Y","D","N"};
  String[] txt = {"","正常發送","電話錯誤","尚未發送"};
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
 public void mediafileProcess() throws Exception
  {
  wp.listCount[0] = wp.itemBuff("ser_num").length;
  
  if (wp.itemStr("bb_down_file_name").length()==0)
     {
      alertErr("尚未產生資料, 無法產生檔案");
      return;
     }
  
  if (!checkApprove(wp.itemStr("approval_user"),wp.itemStr("approval_passwd"))) return;
  
  wp.dateTime();
  String oriFileName = wp.itemStr("bb_down_file_name");
  String fileName="";
  int intk = oriFileName.lastIndexOf('.');
  if (intk >= 0)
      fileName = oriFileName.substring(0,intk)+ "_"+wp.sysDate+wp.sysTime+ oriFileName.substring(intk);
  else
      fileName = wp.itemStr("bb_down_file_name")+"_"+wp.sysDate+wp.sysTime;
  fileName = fileName + ".csv";
  wp.colSet("zz_media_file",fileName);
  TarokoFileAccess tf = new TarokoFileAccess(wp);
  fo = tf.openOutputText(fileName,"MS950");

  if (fo == -1) return;

  String outData="";

  setSelectLimit(99999);
  String  sqlStr = "";
  sqlStr = "select  "
          + "a.active_code, "
          + "NVL(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
          + "NVL(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
          + "a.bonus_pnt,"
          + "a.bonus_date,"
          + "a.fund_amt,"
          + "a.fund_date,"
          + "a.gift_int,"
          + "a.lottery_int, "
          + "a.lottery_type, "
          + "a.proc_date "
          + "from mkt_channel_list a "
          + "left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
          + "left join dbc_idno c on a.id_p_seqno = c.id_p_seqno "
         + "WHERE 1=1 "
         + sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
         + " order by a.active_code,decode(a.acct_type,'90',c.id_no,b.id_no) "
         ;

  busi.FuncBase fB = new busi.FuncBase();
  fB.setConn(wp);
  fB.setSelectLimit(0);
  ds1.colList =fB.sqlQuery(sqlStr, new Object[]{wp.itemStr("ex_active_code")});
  sqlParm.clear();

  for (int inti=0; inti<ds1.listRows(); inti++)
   {
     ds1.listFetch(inti);

     if (inti==0)
        {
         outData = "";
         outData = outData + "活動代號,";
         outData = outData + "身分證號,";
         outData = outData + "姓名,";
         outData = outData + "紅利點數,";
         outData = outData + "紅利回饋日期,";
         outData = outData + "現金回饋金額,";
         outData = outData + "現金回饋日期,";
         outData = outData + "贈品件數,";
         outData = outData + "名單筆數,";
         outData = outData + "名單類型,";
         outData = outData + "贈品/名單回饋日期";
         tf.writeTextFile(fo, outData + newLine);
        }
     outData = "";
     outData = "";
     outData = outData + checkColumn("active_code");
     outData = outData + checkColumn("id_no");
     outData = outData + checkColumn("chi_name");
     outData = outData + checkColumn("bonus_pnt");
     outData = outData + checkColumn("bonus_date");
     outData = outData + checkColumn("fund_amt");
     outData = outData + checkColumn("fund_date");
     outData = outData + checkColumn("gift_int");
     outData = outData + checkColumn("lottery_int");
     outData = outData + checkColumn("lottery_type");
     outData = outData + checkColumn("proc_date");
     tf.writeTextFile(fo, outData + newLine);
   }
  tf.closeOutputText(fo);
  alertMsg("檔案 ["+fileName+"] 已經產生,累計下載 "+ ds1.listRows() + " 筆!");

  wp.colSet("zz_full_media_file","href=./WebData/work/"+fileName+"");
  wp.colSet("img_display"," height=\"30\" src=images/downLoad.gif ");
  return;
  }
// ************************************************************************
 public String checkColumn(String s1) throws Exception
 {
   return ds1.colStr(s1)+",";
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
