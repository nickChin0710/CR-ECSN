/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/04/27  V1.00.02   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0243 extends BaseProc
{
 private final String PROGNAME = "紅利商品庫存量覆核作業處理程式111/12/01  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktp0243Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_gift_stock_t";
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
              + sqlCol(wp.itemStr("ex_gift_no"), "a.gift_no", "like%")
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user")
              + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
              + " and apr_flag='N'     "
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
               + "a.gift_no,"
               + "a.adjust_flag,"
               + "a.adjust_count,"
               + "a.web_count,"
               + "'' as p_net_cnt,"
               + "a.p_supply_cnt,"
               + "a.p_use_cnt,"
               + "a.p_web_cnt,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.gift_no,a.crt_user,a.crt_date"
                ;

  pageQuery();
  listWkdataR();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }

  commCrtUser("comm_crt_user");

  commAdjustFlag("comm_adjust_flag");
  commfuncAudType("aud_type");

  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 void listWkdataR()  throws Exception
 {
   String  sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       sql1 = "select "
            + "decode(gift_type,'3',max_limit_count,SUPPLY_COUNT) as supply_count,"
            + "decode(gift_type,'3',use_limit_count,USE_COUNT) as use_count,"
            + "web_sumcnt as web_count,"
            + "decode(gift_type,'3',max_limit_count-use_limit_count,supply_count-use_count-web_sumcnt) as net_count "
            + " from mkt_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
            ;
       sqlSelect(sql1);

       wp.colSet(ii , "p_net_cnt"   ,  sqlStr("net_count"));
       wp.colSet(ii , "p_supply_cnt",  sqlStr("supply_count"));
       wp.colSet(ii , "p_use_cnt"   ,  sqlStr("use_count"));
       wp.colSet(ii , "p_web_cnt"   ,  sqlStr("web_count"));
      }

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
  if (qFrom==0)
  if (wp.itemStr("kk_gift_no").length()==0)
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
               + "a.gift_no as gift_no,"
               + "a.crt_user,"
               + "a.adjust_flag,"
               + "a.adjust_count,"
               + "a.web_count,"
               + "a.stock_desc,"
               + "a.stock_comment,"
               + "a.a_supply_cnt,"
               + "a.a_use_cnt,"
               + "a.a_web_cnt,"
               + "a_supply_cnt-a_use_cnt-a_web_cnt as a_net_cnt,"
               + "a.p_supply_cnt,"
               + "a.p_use_cnt,"
               + "a.p_web_cnt,"
               + "p_supply_cnt-p_use_cnt-p_web_cnt as p_net_cnt,"
               + "a.m_supply_cnt,"
               + "a.m_use_cnt,"
               + "a.m_web_cnt,"
               + "m_supply_cnt-m_use_cnt-m_web_cnt as m_net_cnt";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.gift_no")
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
  commCrtUser("comm_crt_user");
  commGiftName("comm_gift_no");
  checkButtonOff();
  km1 = wp.colStr("gift_no");
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
   String sql1="";

   sql1 = "select "
        + "decode(gift_type,'3',max_limit_count,SUPPLY_COUNT) as supply_count,"
        + "decode(gift_type,'3',use_limit_count,USE_COUNT) as use_count,"
        + "web_sumcnt as web_count "
        + " from mkt_gift "
        + " where 1 = 1 "
        + " and   gift_no = '"+wp.colStr("gift_no")+"'"
        ;
   sqlSelect(sql1);

   wp.colSet("p_supply_cnt", sqlStr("supply_count"));
   wp.colSet("p_use_cnt"   , sqlStr("use_count"));
   wp.colSet("p_web_cnt"   , sqlStr("web_count"));
   wp.colSet("p_net_cnt"   , sqlNum("supply_count")
                            - sqlNum("use_count")
                            - sqlNum("web_count"));

   if (wp.colStr("adjust_flag").equals("1"))
      {
       wp.colSet("a_supply_cnt",  String.format("%d",
                                   (int)sqlNum("supply_count")
                                +  (int)wp.colNum("adjust_count")));
       wp.colSet("a_use_cnt"   ,  sqlStr("use_count"));
       wp.colSet("a_web_cnt"   ,  String.format("%d",
                                   (int)sqlNum("web_count")
                                +  (int)wp.colNum("web_count")));
       wp.colSet("a_net_cnt"   ,  String.format("%d",
    			           (int)wp.colNum("a_supply_cnt")
                                -  (int)wp.colNum("a_use_cnt")
                                -  (int)wp.colNum("a_web_cnt")));
      }
   else
      {
       wp.colSet("a_supply_cnt",  String.format("%d",
                                +  (int)wp.colNum("adjust_count")));
       wp.colSet("a_use_cnt"   ,  "0");
       wp.colSet("a_web_cnt"   ,  String.format("%d",
                                +  (int)wp.colNum("web_count")));
       wp.colSet("a_net_cnt"   ,  String.format("%d",
                                   (int)wp.colNum("adjust_count")
                                -  (int)wp.colNum("web_count")));
      }

 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = "MKT_GIFT_STOCK";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.gift_no as gift_no,"
               + "a.crt_user as bef_crt_user,"
               + "a.adjust_flag as bef_adjust_flag,"
               + "a.adjust_count as bef_adjust_count,"
               + "a.web_count as bef_web_count,"
               + "a.stock_desc as bef_stock_desc,"
               + "a.stock_comment as bef_stock_comment,"
               + "a.a_supply_cnt as bef_a_supply_cnt,"
               + "a.a_use_cnt as bef_a_use_cnt,"
               + "a.a_web_cnt as bef_a_web_cnt,"
               + "a_supply_cnt-a_use_cnt-a_web_cnt as bef_a_net_cnt,"
               + "a.p_supply_cnt as bef_p_supply_cnt,"
               + "a.p_use_cnt as bef_p_use_cnt,"
               + "a.p_web_cnt as bef_p_web_cnt,"
               + "p_supply_cnt-p_use_cnt-p_web_cnt as bef_p_net_cnt,"
               + "a.m_supply_cnt as bef_m_supply_cnt,"
               + "a.m_use_cnt as bef_m_use_cnt,"
               + "a.m_web_cnt as bef_m_web_cnt,"
               + "m_supply_cnt-m_use_cnt-m_web_cnt as bef_m_net_cnt";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.gift_no")
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
  commGiftName("comm_gift_no");
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
  if (!wp.colStr("adjust_flag").equals(wp.colStr("bef_adjust_flag")))
     wp.colSet("opt_adjust_flag","Y");

  if (!wp.colStr("adjust_count").equals(wp.colStr("bef_adjust_count")))
     wp.colSet("opt_adjust_count","Y");

  if (!wp.colStr("web_count").equals(wp.colStr("bef_web_count")))
     wp.colSet("opt_web_count","Y");

  if (!wp.colStr("stock_desc").equals(wp.colStr("bef_stock_desc")))
     wp.colSet("opt_stock_desc","Y");

  if (!wp.colStr("stock_comment").equals(wp.colStr("bef_stock_comment")))
     wp.colSet("opt_stock_comment","Y");

  if (!wp.colStr("a_supply_cnt").equals(wp.colStr("bef_a_supply_cnt")))
     wp.colSet("opt_a_supply_cnt","Y");

  if (!wp.colStr("a_use_cnt").equals(wp.colStr("bef_a_use_cnt")))
     wp.colSet("opt_a_use_cnt","Y");

  if (!wp.colStr("a_web_cnt").equals(wp.colStr("bef_a_web_cnt")))
     wp.colSet("opt_a_web_cnt","Y");

  if (!wp.colStr("a_net_cnt").equals(wp.colStr("bef_a_net_cnt")))
     wp.colSet("opt_a_net_cnt","Y");

  if (!wp.colStr("p_supply_cnt").equals(wp.colStr("bef_p_supply_cnt")))
     wp.colSet("opt_p_supply_cnt","Y");

  if (!wp.colStr("p_use_cnt").equals(wp.colStr("bef_p_use_cnt")))
     wp.colSet("opt_p_use_cnt","Y");

  if (!wp.colStr("p_web_cnt").equals(wp.colStr("bef_p_web_cnt")))
     wp.colSet("opt_p_web_cnt","Y");

  if (!wp.colStr("p_net_cnt").equals(wp.colStr("bef_p_net_cnt")))
     wp.colSet("opt_p_net_cnt","Y");

  if (!wp.colStr("m_supply_cnt").equals(wp.colStr("bef_m_supply_cnt")))
     wp.colSet("opt_m_supply_cnt","Y");

  if (!wp.colStr("m_use_cnt").equals(wp.colStr("bef_m_use_cnt")))
     wp.colSet("opt_m_use_cnt","Y");

  if (!wp.colStr("m_web_cnt").equals(wp.colStr("bef_m_web_cnt")))
     wp.colSet("opt_m_web_cnt","Y");

  if (!wp.colStr("m_net_cnt").equals(wp.colStr("bef_m_net_cnt")))
     wp.colSet("opt_m_net_cnt","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("adjust_flag","");
       wp.colSet("adjust_count","");
       wp.colSet("web_count","");
       wp.colSet("stock_desc","");
       wp.colSet("stock_comment","");
       wp.colSet("a_supply_cnt","");
       wp.colSet("a_use_cnt","");
       wp.colSet("a_web_cnt","");
       wp.colSet("a_net_cnt","");
       wp.colSet("p_supply_cnt","");
       wp.colSet("p_use_cnt","");
       wp.colSet("p_web_cnt","");
       wp.colSet("p_net_cnt","");
       wp.colSet("m_supply_cnt","");
       wp.colSet("m_use_cnt","");
       wp.colSet("m_web_cnt","");
       wp.colSet("m_net_cnt","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("adjust_flag").length()==0)
     wp.colSet("opt_adjust_flag","Y");

  if (wp.colStr("adjust_count").length()==0)
     wp.colSet("opt_adjust_count","Y");

  if (wp.colStr("web_count").length()==0)
     wp.colSet("opt_web_count","Y");

  if (wp.colStr("stock_desc").length()==0)
     wp.colSet("opt_stock_desc","Y");

  if (wp.colStr("stock_comment").length()==0)
     wp.colSet("opt_stock_comment","Y");

  if (wp.colStr("a_supply_cnt").length()==0)
     wp.colSet("opt_a_supply_cnt","Y");

  if (wp.colStr("a_use_cnt").length()==0)
     wp.colSet("opt_a_use_cnt","Y");

  if (wp.colStr("a_web_cnt").length()==0)
     wp.colSet("opt_a_web_cnt","Y");


  if (wp.colStr("p_supply_cnt").length()==0)
     wp.colSet("opt_p_supply_cnt","Y");

  if (wp.colStr("p_use_cnt").length()==0)
     wp.colSet("opt_p_use_cnt","Y");

  if (wp.colStr("p_web_cnt").length()==0)
     wp.colSet("opt_p_web_cnt","Y");


  if (wp.colStr("m_supply_cnt").length()==0)
     wp.colSet("opt_m_supply_cnt","Y");

  if (wp.colStr("m_use_cnt").length()==0)
     wp.colSet("opt_m_use_cnt","Y");

  if (wp.colStr("m_web_cnt").length()==0)
     wp.colSet("opt_m_web_cnt","Y");


 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  Mktp0243Func func =new Mktp0243Func(wp);

  String[] lsGiftNo = wp.itemBuff("gift_no");
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

     func.varsSet("gift_no", lsGiftNo[rr]);
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
         commCrtUser("comm_crt_user");
         commAdjustFlag("comm_adjust_flag");
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
       if ((wp.respHtml.equals("mktp0243")))
         {
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
 public void commGiftName(String s1) throws Exception 
 {
  commGiftName(s1,0);
  return;
 }
// ************************************************************************
 public void commGiftName(String s1, int befType) throws Exception 
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
            + " from mkt_gift "
            + " where 1 = 1 "
            + " and   gift_no = '"+wp.colStr(ii,befStr+"gift_no")+"'"
            ;
       if (wp.colStr(ii,befStr+"gift_no").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_gift_name"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commAdjustFlag(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"主檔新增","新增","重設"};
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
          + " from sec_user a,mkt_gift_stock_t b "
          + " where a.usr_id = b.crt_user "
          + " group by b.crt_user "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
