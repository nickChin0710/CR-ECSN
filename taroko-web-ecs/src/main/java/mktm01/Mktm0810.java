/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/04/09  V1.00.06   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm01;

import mktm01.Mktm0810Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0810 extends BaseEdit
{
 private final String PROGNAME = "IBON 點數人工調整維護處理程式111-11-30  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0810Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "ibn_prog_list";
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
              + sqlCol(wp.itemStr2("ex_prog_code"), "a.prog_code")
              + sqlCol(wp.itemStr2("ex_crt_user"), "a.crt_user", "like%")
              + sqlCol(wp.itemStr2("ex_id_no"), "a.id_no", "like%")
              + sqlCol(wp.itemStr2("ex_card_no"), "a.card_no", "like%")
              + sqlChkEx(wp.itemStr2("ex_apr_flag"), "2", "")
              + sqlCol(wp.itemStr2("ex_group_type"), "a.group_type")
              + " and from_type  =  'O' "
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
               + "a.txn_seqno,"
               + "a.id_no,"
               + "a.card_no,"
               + "a.group_type,"
               + "a.gift_no,"
               + "a.prog_code,"
               + "a.prog_s_date,"
               + "a.gift_cnt,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by txn_seqno desc,create_date desc,create_time desc"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commGiftNo("comm_gift_no");
  commProgCode("comm_prog_code");
  commCrtUser("comm_crt_user");

  commGroupType("comm_group_type");

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
               + "a.txn_seqno as txn_seqno,"
               + "a.apr_flag,"
               + "a.group_type,"
               + "a.prog_code,"
               + "a.prog_s_date,"
               + "a.prog_e_date,"
               + "a.gift_no,"
               + "a.id_no,"
               + "a.p_seqno,"
               + "a.id_p_seqno,"
               + "a.card_no,"
               + "a.gift_cnt,"
               + "a.crt_date,"
               + "a.crt_user,"
               + "a.apr_date,"
               + "a.apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.txn_seqno")
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
  commGroupType("comm_group_type");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  km1 = wp.colStr("txn_seqno");
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
               + "a.txn_seqno as txn_seqno,"
               + "a.apr_flag as apr_flag,"
               + "a.group_type as group_type,"
               + "a.prog_code as prog_code,"
               + "a.prog_s_date as prog_s_date,"
               + "a.prog_e_date as prog_e_date,"
               + "a.gift_no as gift_no,"
               + "a.id_no as id_no,"
               + "a.p_seqno as p_seqno,"
               + "a.id_p_seqno as id_p_seqno,"
               + "a.card_no as card_no,"
               + "a.gift_cnt as gift_cnt,"
               + "a.crt_date as crt_date,"
               + "a.crt_user as crt_user,"
               + "a.apr_date as apr_date,"
               + "a.apr_user as apr_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.txn_seqno")
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
  commGroupType("comm_group_type");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
  commfuncAudType("aud_type");
 }
// ************************************************************************
 public void deleteFuncD3R() throws Exception
 {
  qFrom=0; 
   km1 = wp.itemStr2("txn_seqno");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      km1 = wp.itemStr2("txn_seqno");
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
   km1 = wp.itemStr2("txn_seqno");
  fstAprFlag = wp.itemStr2("fst_apr_flag");
  if (!wp.itemStr2("aud_type").equals("Y"))
     {
      strAction = "U";
      updateFunc();
      if (rc==1) dataReadR3R();
     }
  else
     {
      km1 = wp.itemStr2("txn_seqno");
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
  mktm01.Mktm0810Func func =new mktm01.Mktm0810Func(wp);

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
       if ((wp.respHtml.equals("mktm0810_nadd"))||
           (wp.respHtml.equals("mktm0810_detl")))
         {
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("prog_code1").length()>0)
             {
             wp.optionKey = wp.colStr("prog_code1");
             }
          this.dddwList("dddw_prog_code"
                 ,"select prog_code||'-'||prog_s_date as db_code, prog_code||'('||substr(prog_desc,1,4)||')-'||prog_s_date as db_desc  from ibn_prog where prog_flag='Y' and to_char(sysdate,'yyyymmdd')< prog_e_date   order by prog_code,prog_s_date"
                        );
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("gift_no").length()>0)
             {
             wp.optionKey = wp.colStr("gift_no");
             }
          this.dddwList("dddw_gift_no"
                 ,"ibn_prog_gift"
                 ,"trim(gift_no)"
                 ,"trim(gift_name)"
                 ," where prog_code = '"+wp.colStr("prog_code")+"'  group by gift_no,gift_name order by gift_no,gift_name");
         }
       if ((wp.respHtml.equals("mktm0810")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_prog_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_prog_code");
             }
          this.dddwList("dddw_prog_code"
                 ,"ibn_prog"
                 ,"trim(prog_code)"
                 ,"trim(prog_desc)"
                 ," where  prog_flag='Y'");
         }
      } catch(Exception ex){}
 }
// ************************************************************************
 public int queryCheck() throws Exception
 {
/*  if ((item_kk("ex_card_no").length()==0)&&
      (item_kk("ex_apr_flag").equals("Y"))&&
      (item_kk("ex_id_no").length()==0))
     {
      alertErr("身份證號與卡號二者不可同時空白");
      return(1);
     }
*/
  String sql1 = "";
  if (wp.itemStr2("ex_id_no").length()!=0)
     {
      sql1 = "select id_p_seqno "
           + "from  ibn_prog_list "
           + "where  id_no  =  '"+ wp.itemStr2("ex_id_no").toUpperCase() +"' "
           + "fetch first 1 rows only "
           ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr(" 查無此身分證號["+wp.itemStr2("ex_id_no").toUpperCase() +"] 資料");
          return(1);
         }
     }


  if (wp.itemStr2("ex_card_no").length()!=0)
     {
      sql1 = "select id_p_seqno "
           + "from ibn_prog_list "
           + "where  card_no  =  '"+ wp.itemStr2("ex_card_no").toUpperCase() +"' "
           + "fetch first 1 rows only "
           ;
      
      sqlSelect(sql1);
      if (sqlRowNum <= 0)
         {
          alertErr(" 查無此卡號["+wp.itemStr2("ex_card_no").toUpperCase() +"] 資料");
          return(1);
         }
     }

  return(0);
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
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
 public void commGiftNo(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name "
            + " from ibn_prog_gift "
            + " where 1 = 1 "
            + " and   prog_code = '"+wp.colStr(ii,"prog_code")+"'"
            + " and   prog_s_date = '"+wp.colStr(ii,"prog_s_date")+"'"
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commProgCode(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " prog_desc as column_prog_desc "
            + " from ibn_prog "
            + " where 1 = 1 "
            + " and   prog_code = '"+wp.colStr(ii,"prog_code")+"'"
            + " and   prog_s_date = '"+wp.colStr(ii,"prog_s_date")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_prog_desc"); 
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
 public void commGroupType(String s1) throws Exception 
 {
  String[] cde = {"group_type","1","2","3","4"};
  String[] txt = {"","限信>用卡兌換(限01,05,06)","限 VD卡兌換(限90)","全部任一卡片兌換(01,05,06,90)","限特定卡號兌換"};
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
  String ajaxjGiftNo = "";
  super.wp = wr;


  if (selectAjaxFunc10(
                    wp.itemStr2("ax_win_group_type"),
                    wp.itemStr2("ax_win_id_no").toUpperCase(),
                    wp.itemStr2("ax_win_card_no"))!=0) 
     {
      wp.addJSON("chi_name","");
      return;
     }

  wp.addJSON("chi_name",sqlStr("chi_name"));
 }
// ************************************************************************
 int selectAjaxFunc10(String s1,String s2,String s3) throws Exception
// void select_ajax_func_1(String s1,String s2,String s3)
  {
   if ((s1.equals("4"))&&(s3.length()==0)) return 0;
   if ((!s1.equals("4"))&&(s2.length()==0)) return 0;
   if (s1.equals("1"))
      {
       wp.sqlCmd = " select "
                 + " chi_name "
                 + " from  crd_idno a,crd_card d "
                 + " where a.id_p_seqno=d.id_p_seqno "
                 + " and   a.id_no ='"+s2+"' "
                 + " order by current_code "
                 ;
       this.sqlSelect();
      }
   else if (s1.equals("2"))
      {
       wp.sqlCmd = " select "
                 + " chi_name "
                 + " from dbc_idno a,dbc_card d "
                 + " where a.id_p_seqno=d.id_p_seqno "
                 + " and   a.id_no ='"+s2+"' "
                 + " order by current_code "
                 ;
       this.sqlSelect();
      }
   else if (s1.equals("3"))
      {
       wp.sqlCmd = " select "
                 + " chi_name "
                 + " from  crd_idno a,crd_card d "
                 + " where a.id_p_seqno=d.id_p_seqno "
                 + " and   a.id_no ='"+s2+"' "
                 + " order by current_code "
                 ;
       this.sqlSelect();
       if (sqlRowNum<=0)
          {
           wp.sqlCmd = " select "
                     + " chi_name "
                     + " from dbc_idno a,dbc_card d "
                     + " where a.id_p_seqno=d.id_p_seqno "
                     + " and   c.id_no ='"+s2+"' "
                     + " order by current_code "
                     ;
           this.sqlSelect();
          }
      }
   else if (s1.equals("4"))
      {
       wp.sqlCmd = " select " 
                 + " chi_name "
                 + " from  crd_idno a,crd_card d "
                 + " where a.id_p_seqno=d.id_p_seqno "
                 + " and   d.card_no ='"+s3+"' "
                 + " order by current_code "
                 ;
       this.sqlSelect();
       if (sqlRowNum<=0)
          {
           wp.sqlCmd = " select " 
                     + " chi_name "
                     + " from dbc_idno a,dbc_card d "
                     + " where a.id_p_seqno=d.id_p_seqno "
                     + " and   d.card_no ='"+s3+"' "
                     + " order by current_code "
                     ;
           this.sqlSelect();
          }

      }

   if (sqlRowNum<=0)
      {
       if (s1.equals("4"))
           alertErr("卡號：["+s3+"]查無資料或無有效卡");
       else
           alertErr("身份證號碼：["+s2+"]查無資料");
       return 1;
      }

   return 0;
 }
// ************************************************************************

// ************************************************************************
 public void wfAjaxFunc2(TarokoCommon wr) throws Exception
 {
  String ajaxjGiftNo = "";
  super.wp = wr;

  if (wp.itemStr2("ax_win_prog_code1").length()==0) return;

  if (selectAjaxFunc20(
                    wp.itemStr2("ax_win_prog_code1"))!=0) 
     {
      wp.addJSON("prog_code","");
      wp.addJSON("prog_s_date","");
      wp.addJSON("prog_e_date","");
      return;
     }

  wp.addJSON("prog_code",sqlStr("prog_code"));
  wp.addJSON("prog_s_date",sqlStr("prog_s_date"));
  wp.addJSON("prog_e_date",sqlStr("prog_e_date"));

  if (wp.itemStr2("ax_win_prog_code1").length()==0) return;

  if (selectAjaxFunc21(
                    wp.itemStr2("ax_win_prog_code1"))!=0) 
     {
      wp.addJSON("ajaxj_gift_no", "");
      wp.addJSON("ajaxj_gift_name", "");
      wp.addJSON("chi_name","");
      return;
     }

  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_gift_no", sqlStr(ii, "gift_no"));
  for (int ii = 0; ii < sqlRowNum; ii++)
    wp.addJSON("ajaxj_gift_name", sqlStr(ii, "gift_name"));
  wp.addJSON("chi_name",sqlStr("chi_name"));
 }
// ************************************************************************
 int selectAjaxFunc20(String s1) throws Exception
  {
   wp.sqlCmd = " select "
             + " b.prog_code as prog_code ,"
             + " b.prog_s_date as prog_s_date ,"
             + " b.prog_e_date as prog_e_date "
             + " from  ibn_prog b "
             + " where b.prog_code ='"+comm.getStr(s1,1 ,"-")+"' "
             + " and   b.prog_s_date ='"+comm.getStr(s1,2 ,"-")+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("活動代碼選擇["+s1+"]查無資料");
       return 1;
      }

   return 0;
 }

// ************************************************************************
 int selectAjaxFunc21(String s1) throws Exception
 {
   wp.sqlCmd = " select "
             + " gift_no,"
             + " gift_name,"
             + " gift_s_date,"
             + " gift_e_date"
             + " from  ibn_prog_gift "
             + " where prog_code ='"+sqlStr("prog_code")+"' "
             + " and   prog_s_date ='"+sqlStr("prog_s_date")+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {
       alertErr("贈品代碼選擇:["+s1+"]查無資料");
       return 1;
      }

   return 0;
  }

// ************************************************************************
 public void wfButtonFunc4(TarokoCommon wr) throws Exception
 {
  super.wp = wr;

  if (selectButtonFunc4(
                       wp.itemStr2("ax_win_group_type"),
                       wp.itemStr2("ax_win_prog_code"),
                       wp.itemStr2("ax_win_prog_s_date"),
                       wp.itemStr2("ax_win_id_no"),
                       wp.itemStr2("ax_win_card_no"))!=0)
     {
      wp.addJSON("tot_gift_cnt","");
      wp.addJSON("rem_gift_cnt","");
      return;
     }

  wp.addJSON("tot_gift_cnt",sqlStr("tot_gift_cnt"));
  wp.addJSON("rem_gift_cnt",sqlStr("rem_gift_cnt"));
 }
// ************************************************************************
int selectButtonFunc4(String s1,String s2,String s3,String s4,String s5)  throws Exception
 {

  if (s1.length()==0)
     {
      alertErr("群組代號必須選取");
      return 1;
     }
  if (s2.length()==0)
     {
      alertErr("活動代碼必須選取");
      return 1;
     }
   String data_type ="",data_id="";
   if (s1.equals("4"))
      {
       data_type = "3";
       data_id   =  s5;
       if (data_id.length()==0)
          {
           alertErr("卡號必須輸入");
           return 1;
          }
      }
   else
      {
       data_type = "1";
       data_id   =  s4.toUpperCase();
       if (data_id.length()==0)
          {
           alertErr("身分證號必須輸入");
           return 1;
          }
      }
   wp.sqlCmd = " select "
             + " tot_gift_cnt ,"
             + " rem_gift_cnt "
             + " from ibn_prog_dtl " 
             + " where data_type   = '"+data_type+"' "
             + " and   data_id     = '"+data_id+"' "
             + " and   group_type  = '"+s1+"' "
             + " and   prog_code   = '"+s2+"' "
             + " and   prog_s_date = '"+s3+"' "
             ;

   this.sqlSelect();
   if (sqlRowNum<=0)
      {

      alertErr("分入[" + data_id +"]");
       wp.addJSON("tot_gift_cnt","0");
       wp.addJSON("rem_gift_cnt","0");
       return 1;
      }
   wp.addJSON("tot_gift_cnt",sqlStr("tot_gift_cnt"));
   wp.addJSON("rem_gift_cnt",sqlStr("rem_gift_cnt"));


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

}  // End of class
