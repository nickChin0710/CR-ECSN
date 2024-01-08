/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/24  V1.00.01   Allen Ho      Initial                              *
* 111/12/02  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp3120 extends BaseProc
{
 private final String PROGNAME = "IBON商品資料覆核作業處理程式111/12/02  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktp3120Func func = null;
  String kk1,kk2,kk3;
  String km1,km2,km3;
  String fstAprFlag = "";
  String orgTabName = "ibn_prog_gift_t";
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
              + " and a.prog_code = b.prog_code "
              + sqlCol(wp.itemStr("ex_prog_code"), "a.prog_code", "like%")
              + sqlCol(wp.itemStr("ex_prog_flag"), "b.prog_flag", "like%")
              + sqlCol(wp.itemStr("ex_gift_no"), "a.gift_no", "like%")
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")
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
               + "a.prog_code,"
               + "a.prog_s_date,"
               + "a.prog_e_date,"
               + "GIFT_NO||'-'||gift_name as gift_no,"
               + "a.gift_s_date,"
               + "a.gift_e_date,"
               + "b.prog_flag,"
               + "a.prd_price,"
               + "a.exchange_pnt,"
               + "a.crt_user";

  wp.daoTable = controlTabName + " a "
              + "LEFT OUTER JOIN ibn_prog b "
              + "ON a.prog_code = b.prog_code "
              ;
  wp.whereOrder = " "
                + " order by a.prog_code,b.prog_flag,a.gift_no,a.crt_user"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }


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
  if (wp.itemStr("kk_prog_code").length()==0)
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
               + "a.prog_code as prog_code,"
               + "a.gift_no as gift_no,"
               + "a.prog_s_date as prog_s_date,"
               + "a.crt_user,"
               + "b.prog_flag as prog_flag,"
               + "a.prog_e_date,"
               + "a.gift_name,"
               + "a.gift_s_date,"
               + "a.gift_e_date,"
               + "a.gift_typeno,"
               + "a.prd_price,"
               + "a.exchange_pnt";

  wp.daoTable = controlTabName + " a "
              + "LEFT OUTER JOIN ibn_prog b "
              + "ON a.prog_code = b.prog_code "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.prog_code")
                   + sqlCol(km2, "a.gift_no")
                   + sqlCol(km3, "a.prog_s_date")
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
  commProgCode("comm_prog_code");
  checkButtonOff();
  km1 = wp.colStr("prog_code");
  km2 = wp.colStr("gift_no");
  km3 = wp.colStr("prog_s_date");
  listWkdataAft();
  if (!wp.colStr("aud_type").equals("A")) dataReadR3R();
  else
    {
     commfuncAudType("aud_type");
     listWkdataSpace();
    }
 }
// ************************************************************************
 public void dataReadR3R() throws Exception
 {
  wp.colSet("control_tab_name", controlTabName); 
  controlTabName = "ibn_prog_gift";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.prog_code as prog_code,"
               + "a.gift_no as gift_no,"
               + "a.prog_s_date as prog_s_date,"
               + "a.crt_user as bef_crt_user,"
               + "b.prog_flag as bef_prog_flag,"
               + "a.prog_e_date as bef_prog_e_date,"
               + "a.gift_name as bef_gift_name,"
               + "a.gift_s_date as bef_gift_s_date,"
               + "a.gift_e_date as bef_gift_e_date,"
               + "a.gift_typeno as bef_gift_typeno,"
               + "a.prd_price as bef_prd_price,"
               + "a.exchange_pnt as bef_exchange_pnt";

  wp.daoTable = controlTabName + " a "
              + "LEFT OUTER JOIN ibn_prog b "
              + "ON a.prog_code = b.prog_code "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.prog_code")
              + sqlCol(km2, "a.gift_no")
              + sqlCol(km3, "a.prog_s_date")
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
  commProgCode("comm_prog_code");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("gift_name").equals(wp.colStr("bef_gift_name")))
     wp.colSet("opt_gift_name","Y");

  if (!wp.colStr("gift_s_date").equals(wp.colStr("bef_gift_s_date")))
     wp.colSet("opt_gift_s_date","Y");

  if (!wp.colStr("gift_e_date").equals(wp.colStr("bef_gift_e_date")))
     wp.colSet("opt_gift_e_date","Y");

  if (!wp.colStr("gift_typeno").equals(wp.colStr("bef_gift_typeno")))
     wp.colSet("opt_gift_typeno","Y");

  if (!wp.colStr("prd_price").equals(wp.colStr("bef_prd_price")))
     wp.colSet("opt_prd_price","Y");

  if (!wp.colStr("exchange_pnt").equals(wp.colStr("bef_exchange_pnt")))
     wp.colSet("opt_exchange_pnt","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("gift_name","");
       wp.colSet("gift_s_date","");
       wp.colSet("gift_e_date","");
       wp.colSet("gift_typeno","");
       wp.colSet("prd_price","");
       wp.colSet("exchange_pnt","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("gift_name").length()==0)
     wp.colSet("opt_gift_name","Y");

  if (wp.colStr("gift_s_date").length()==0)
     wp.colSet("opt_gift_s_date","Y");

  if (wp.colStr("gift_e_date").length()==0)
     wp.colSet("opt_gift_e_date","Y");

  if (wp.colStr("gift_typeno").length()==0)
     wp.colSet("opt_gift_typeno","Y");

  if (wp.colStr("prd_price").length()==0)
     wp.colSet("opt_prd_price","Y");

  if (wp.colStr("exchange_pnt").length()==0)
     wp.colSet("opt_exchange_pnt","Y");

 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  Mktp3120Func func =new Mktp3120Func(wp);

  String[] lsProgCode = wp.itemBuff("prog_code");
  String[] lsGiftNo = wp.itemBuff("gift_no");
  String[] lsProgSDate = wp.itemBuff("prog_s_date");
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

     func.varsSet("prog_code", lsProgCode[rr]);
     func.varsSet("gift_no", lsGiftNo[rr]);
     func.varsSet("prog_s_date", lsProgSDate[rr]);
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
       if ((wp.respHtml.equals("mktp3120")))
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
          if (wp.colStr("ex_gift_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_gift_no");
             }
          lsSql = "";
          lsSql =  procDynamicDddwGiftNo1(wp.colStr("ex_gift_no"));
          wp.optionKey = wp.colStr("ex_gift_no");
          dddwList("dddw_gift_no_1", lsSql);
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
 public void commProgCode(String s1) throws Exception 
 {
  commProgCode(s1,0);
  return;
 }
// ************************************************************************
 public void commProgCode(String s1, int befType) throws Exception 
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
// **************************************************************************************
 String procDynamicDddwCrtUser1(String s1)  throws Exception
 { 
   String lsSql = "";
   
   lsSql = " select "
          + " b.crt_user as db_code, "
          + " max(b.crt_user||' '||a.usr_cname) as db_desc "
          + " from sec_user a,ibn_prog_gift_t b "
          + " where a.usr_id = b.crt_user "
          + " and   b.apr_flag = 'N' "
          + " group by b.crt_user "
          ;
   
   return lsSql;
 }
// **************************************************************************************
 String procDynamicDddwGiftNo1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.gift_no as db_code, "
          + " max(b.gift_no||' '||b.gift_name||'('||gift_s_date||'~'||gift_e_date||')') as db_desc "
          + " from ibn_prog_gift_t b "
          + " where   b.apr_flag = 'N' "
          + " group by b.gift_no "
          ;
   return lsSql;
 }
// **************************************************************************************
 String procDynamicDddwProgCode1(String s1)  throws Exception
 {
   String lsSql = "";


   lsSql = " select "
          + " b.prog_code as db_code, "
          + " max(b.prog_code||' '||a.prog_desc||'('||b.prog_s_date||'~'||b.prog_e_date||')') as db_desc "
          + " from ibn_prog a,ibn_prog_gift_t b "
          + " where a.prog_code = b.prog_code "  
          + " and   b.prog_s_date= a.prog_s_date "
          + " and   b.apr_flag = 'N' "
          + " group by b.prog_code "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
