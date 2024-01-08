/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/06/24  V1.00.04   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp01;

import ecsfunc.EcsCallbatch;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0810 extends BaseProc
{
 private final String PROGNAME = "IBON專案卡友可兌贈品資料覆核處理程式111/12/01  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktp0810Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "ibn_prog_list_t";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt =0, recCnt =0, notifyCnt =0,colNum=0;
  int[] datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String upGroupType = "0";

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
  else if (eqIgno(wp.buttonCode, "C"))
     {// 資料處理 -/
      strAction = "A";
      dataProcess();
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
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr("ex_prog_code"), "a.prog_code")
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user")
              + " and a.apr_flag='N'     "
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
               + "a.aud_type,"
               + "a.id_no,"
               + "a.card_no,"
               + "a.group_type,"
               + "a.gift_no,"
               + "a.prog_code,"
               + "a.prog_s_date,"
               + "a.gift_cnt,"
               + "a.crt_user,"
               + "a.crt_date,"
               + "a.txn_seqno";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.prog_code,a.crt_user"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }

  commGiftNo("comm_gift_no");
  commCrtUser("comm_crt_user");

  commGroupType("comm_group_type");
  commfuncAudType("aud_type");

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
  if (qFrom==0)
  if (wp.itemStr("kk_txn_seqno").length()==0)
     { 
      alertErr("查詢鍵必須輸入");
      return; 
     } 
  if (controlTabName.length()==0)
     {
      if (wp.colStr("control_tab_name").length()==0)
         controlTabName = orgTabName;
      else
         controlTabName =wp.colStr("control_tab_name");
     }
  else
     {
      if (wp.colStr("control_tab_name").length()!=0)
         controlTabName =wp.colStr("control_tab_name");
     }
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.aud_type,"
               + "a.txn_seqno as txn_seqno,"
               + "a.crt_user,"
               + "a.group_type,"
               + "a.prog_code,"
               + "a.prog_s_date,"
               + "a.prog_e_date,"
               + "a.gift_no,"
               + "a.id_no,"
               + "a.card_no,"
               + "a.gift_cnt";

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
  datareadWkdata();
  commGroupTypes("comm_group_type");
  commCrtUser("comm_crt_user");
  commProgCodes("comm_prog_code");
  commGiftNos("comm_gift_no");
  checkButtonOff();
  km1 = wp.colStr("txn_seqno");
  listWkdataAft();
  if (!wp.colStr("aud_type").equals("A")) dataReadR3R();
  else
    {
     commfuncAudType("aud_type");
     listWkdataSpace();
    }
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
 String columnData="";
 String sql1 = "select "
             + " age_1_s,"
             + " age_1_e,"
             + " age_2_s,"
             + " age_2_e,"
             + " age_3_s,"
             + " age_3_e,"
             + " age_4_s,"
             + " age_4_e,"
             + " age_5_s,"
             + " age_5_e,"
             + " age_6_s,"
             + " age_6_e"
             + " from mkt_insure_parm "
             + " WHERE  effect_month = (select max(effect_month) "
             + "                        from   mkt_insure_parm)  "
             ;
 sqlSelect(sql1);

 if (sqlRowNum>0)
    {
     wp.colSet("age_1_s", sqlStr("age_1_s"));
//     wp.itemSet("age_1_s", sqlStr("age_1_s"));
     wp.colSet("age_1_e", sqlStr("age_1_e"));
//     wp.itemSet("age_1_e", sqlStr("age_1_e"));
     wp.colSet("age_2_s", sqlStr("age_2_s"));
     wp.colSet("age_2_e", sqlStr("age_2_e"));
     wp.colSet("age_3_s", sqlStr("age_3_s"));
     wp.colSet("age_3_e", sqlStr("age_3_e"));
     wp.colSet("age_4_s", sqlStr("age_4_s"));
     wp.colSet("age_4_e", sqlStr("age_4_e"));
     wp.colSet("age_5_s", sqlStr("age_5_s"));
     wp.colSet("age_5_e", sqlStr("age_5_e"));
     wp.colSet("age_6_s", sqlStr("age_6_s"));
     wp.colSet("age_6_e", sqlStr("age_6_e"));
    }
 return;


 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = "IBN_PROG_list";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.txn_seqno as txn_seqno,"
               + "a.crt_user as bef_crt_user,"
               + "a.group_type as bef_group_type,"
               + "a.prog_code as bef_prog_code,"
               + "a.prog_s_date as bef_prog_s_date,"
               + "a.prog_e_date as bef_prog_e_date,"
               + "a.gift_no as bef_gift_no,"
               + "a.id_no as bef_id_no,"
               + "a.card_no as bef_card_no,"
               + "a.gift_cnt as bef_gift_cnt";

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
  wp.colSet("control_tab_name", controlTabName); 

  if (wp.respHtml.indexOf("_detl") > 0) 
     wp.colSet("btnStore_disable","");   
  commCrtUser("comm_crt_user");
  commGroupTypes("comm_group_type");
  commProgCodes("comm_prog_code");
  commGiftNos("comm_gift_no");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  datareadWkdata();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("group_type").equals(wp.colStr("bef_group_type")))
     wp.colSet("opt_group_type","Y");
  commGroupTypes("comm_group_type");
  commGroupTypes("comm_bef_group_type");

  if (!wp.colStr("prog_code").equals(wp.colStr("bef_prog_code")))
     wp.colSet("opt_prog_code","Y");
  commProgCodes("comm_prog_code");
  commProgCodes("comm_bef_prog_code",1);

  if (!wp.colStr("prog_s_date").equals(wp.colStr("bef_prog_s_date")))
     wp.colSet("opt_prog_s_date","Y");

  if (!wp.colStr("prog_e_date").equals(wp.colStr("bef_prog_e_date")))
     wp.colSet("opt_prog_e_date","Y");

  if (!wp.colStr("gift_no").equals(wp.colStr("bef_gift_no")))
     wp.colSet("opt_gift_no","Y");
  commGiftNos("comm_gift_no");
  commGiftNos("comm_bef_gift_no",1);

  if (!wp.colStr("id_no").equals(wp.colStr("bef_id_no")))
     wp.colSet("opt_id_no","Y");

  if (!wp.colStr("card_no").equals(wp.colStr("bef_card_no")))
     wp.colSet("opt_card_no","Y");

  if (!wp.colStr("gift_cnt").equals(wp.colStr("bef_gift_cnt")))
     wp.colSet("opt_gift_cnt","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("group_type","");
       wp.colSet("prog_code","");
       wp.colSet("prog_s_date","");
       wp.colSet("prog_e_date","");
       wp.colSet("gift_no","");
       wp.colSet("id_no","");
       wp.colSet("card_no","");
       wp.colSet("gift_cnt","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("group_type").length()==0)
     wp.colSet("opt_group_type","Y");

  if (wp.colStr("prog_code").length()==0)
     wp.colSet("opt_prog_code","Y");

  if (wp.colStr("prog_s_date").length()==0)
     wp.colSet("opt_prog_s_date","Y");

  if (wp.colStr("prog_e_date").length()==0)
     wp.colSet("opt_prog_e_date","Y");

  if (wp.colStr("gift_no").length()==0)
     wp.colSet("opt_gift_no","Y");

  if (wp.colStr("id_no").length()==0)
     wp.colSet("opt_id_no","Y");

  if (wp.colStr("card_no").length()==0)
     wp.colSet("opt_card_no","Y");

  if (wp.colStr("gift_cnt").length()==0)
     wp.colSet("opt_gift_cnt","Y");

 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  Mktp0810Func func =new Mktp0810Func(wp);

  String[] lsTxnSeqno = wp.itemBuff("txn_seqno");
  String[] lsAudType  = wp.itemBuff("aud_type");
  String[] lsCrtUser  = wp.itemBuff("crt_user");
  String[] lsRowid     = wp.itemBuff("rowid");
  String[] opt =wp.itemBuff("opt");
  wp.listCount[0] = lsAudType.length;

  int rr = -1;
  wp.selectCnt = lsAudType.length;
  for (int ii = 0; ii < opt.length; ii++)
    {
     if (opt[ii].length()==0) continue;
     rr = (int) (this.toNum(opt[ii])%20 - 1);
     if (rr==-1) rr = 19;
     if (rr<0) continue;

     wp.colSet(rr,"ok_flag","-");
     if (lsCrtUser[rr].equals(wp.loginUser))
        {
         ilAuth++;
         wp.colSet(rr,"ok_flag","F");
         continue;
        }

     lsUser=lsCrtUser[rr];
     if (!apprBankUnit(lsUser,wp.loginUser))
        {
         ilAuth++;
         wp.colSet(rr,"ok_flag","B");
         continue;
        }

     func.varsSet("txn_seqno", lsTxnSeqno[rr]);
     func.varsSet("aud_type", lsAudType[rr]);
     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     if (lsAudType[rr].equals("A"))
        rc =func.dbInsertA4();
     else if (lsAudType[rr].equals("U"))
        rc =func.dbUpdateU4();
     else if (lsAudType[rr].equals("D"))
        rc =func.dbDeleteD4();

     if (rc!=1) alertErr2(func.getMsg());
     if (rc == 1)
        {
         commGiftNo("comm_gift_no");
         commCrtUser("comm_crt_user");
         commGroupType("comm_group_type");
         commfuncAudType("aud_type");

         wp.colSet(rr,"ok_flag","V");
         ilOk++;
         func.dbDelete();
         this.sqlCommit(rc);
         continue;
        }
     ilErr++;
     wp.colSet(rr,"ok_flag","X");
     this.sqlCommit(0);
    }

  EcsCallbatch batch = new EcsCallbatch(wp) ;

  rc=batch.callBatch("IbnB030");

  if (rc!=1)
     {
      alertErr2("callbatch[IbnB030] 失敗");
     }
  else
     {
      alertMsg("批次已啟動成功! ");
     }


  alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr+"; 權限問題=" + ilAuth);
  buttonOff("btnAdd_disable");
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
       if ((wp.respHtml.equals("mktp0810")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_prog_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_prog_code");
             }
          lsSql = "";
          lsSql =  procDynamicDddwProgCode1(wp.colStr("ex_prog_code"));
          wp.optionKey = wp.colStr("ex_prog_code");
          dddwList("dddw_prog_code_1", lsSql);
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_crt_user").length()>0)
             {
             wp.optionKey = wp.colStr("ex_crt_user");
             }
          lsSql = "";
          lsSql =  procDynamicDddwCrtUser1(wp.colStr("ex_crt_user"));
          wp.optionKey = wp.colStr("ex_crt_user");
          dddwList("dddw_crt_user_1", lsSql);
         }
      } catch(Exception ex){}
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
  commCrtUser(s1,0);
  return;
 }
// ************************************************************************
 public void commCrtUser(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " usr_cname as column_usr_cname "
            + " from sec_user "
            + " where 1 = 1 "
            + " and   usr_id = '"+wp.colStr(ii,befStr+"crt_user")+"'"
            ;
       if (wp.colStr(ii,befStr+"crt_user").length()==0)
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
 public void commProgCodes(String s1) throws Exception 
 {
  commProgCodes(s1,0);
  return;
 }
// ************************************************************************
 public void commProgCodes(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " prog_desc as column_prog_desc "
            + " from ibn_prog "
            + " where 1 = 1 "
            + " and   prog_code = '"+wp.colStr(ii,befStr+"prog_code")+"'"
            + " and   prog_s_date = '"+wp.colStr(ii,befStr+"prog_s_date")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_prog_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commGiftNos(String s1) throws Exception 
 {
  commGiftNos(s1,0);
  return;
 }
// ************************************************************************
 public void commGiftNos(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name "
            + " from ibn_prog_gift "
            + " where 1 = 1 "
            + " and   prog_code = '"+wp.colStr(ii,befStr+"prog_code")+"'"
            + " and   prog_s_date = '"+wp.colStr(ii,befStr+"prog_s_date")+"'"
            + " and   gift_no = '"+wp.colStr(ii,befStr+"gift_no")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commGiftNo(String s1) throws Exception 
 {
  commGiftNo(s1,0);
  return;
 }
// ************************************************************************
 public void commGiftNo(String s1, int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " gift_name as column_gift_name "
            + " from ibn_prog_gift "
            + " where 1 = 1 "
            + " and   prog_code = '"+wp.colStr(ii,befStr+"prog_code")+"'"
            + " and   prog_s_date = '"+wp.colStr(ii,befStr+"prog_s_date")+"'"
            + " and   gift_no = '"+wp.colStr(ii,befStr+"gift_no")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commGroupTypes(String s1) throws Exception 
 {
  String[] cde = {"group_type","1","2","3","4"};
  String[] txt = {"","限信用卡兌換(限01,05,06)","限 VD卡兌換(限90)","全部任一卡片兌換(01,05,06,90)","限特定卡號兌換"};
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
  String[] txt = {"","限信用卡兌換(限01,05,06)","限 VD卡兌換(限90)","全部任一卡 片兌換(01,05,06,90)","限特定卡號兌換"};
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
  buttonOff("btnAdd_disable");
  return;
 }
// ************************************************************************
 String procDynamicDddwCrtUser1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.crt_user as db_code, "
          + " max(b.crt_user||' '||a.usr_cname) as db_desc "
          + " from sec_user a,ibn_prog_list_t b "
          + " where a.usr_id = b.crt_user "
          + " and   b.apr_flag = 'N' "
          + " group by b.crt_user "
          ;

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwProgCode1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.prog_code as db_code, "
          + " max(b.prog_code||' '||a.prog_desc) as db_desc "
          + " from ibn_prog a,ibn_prog_list_t b "
          + " where a.prog_code = b.prog_code "
          + " and   b.apr_flag = 'N' "
          + " group by b.prog_code "
          ;

   return lsSql;
 }
// ************************************************************************

}  // End of class
