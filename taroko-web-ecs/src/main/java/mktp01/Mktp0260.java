/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/11/26  V1.00.10   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0260 extends BaseProc
{
 private final String PROGNAME = "紅利贈品廠商覆核作業處理程式111/12/01  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktp0260Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_vendor_t";
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
              + sqlCol(wp.itemStr("ex_vendor_no"), "a.vendor_no", "like%")
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")
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
               + "a.vendor_no,"
               + "a.vendor_name,"
               + "a.name,"
               + "a.id_no,"
               + "a.tel_no,"
               + "a.contact_id,"
               + "a.crt_user,"
               + "to_char(a.mod_time,'yyyymmddhh24miss') as mod_time,"
               + "a.mod_user,"
               + "a.mod_pgm";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.vendor_no,a.crt_user"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }

  commCrtUser("comm_crt_user");

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
  if (wp.itemStr("kk_vendor_no").length()==0)
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
               + "a.vendor_no as vendor_no,"
               + "a.crt_user,"
               + "a.vendor_name,"
               + "a.disable_flag,"
               + "a.id_no,"
               + "a.name,"
               + "a.sub_cname,"
               + "a.out_days,"
               + "a.tel_no,"
               + "a.contact_id,"
               + "a.contact_tel,"
               + "a.area_code,"
               + "a.address1,"
               + "a.address2,"
               + "a.address3,"
               + "a.address4,"
               + "a.address5";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.vendor_no")
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
  commDusableFlag("comm_disable_flag");
  commCrtUser("comm_crt_user");
  checkButtonOff();
  km1 = wp.colStr("vendor_no");
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
  controlTabName = "mkt_vendor";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.vendor_no as vendor_no,"
               + "a.crt_user as bef_crt_user,"
               + "a.vendor_name as bef_vendor_name,"
               + "a.disable_flag as bef_disable_flag,"
               + "a.id_no as bef_id_no,"
               + "a.name as bef_name,"
               + "a.sub_cname as bef_sub_cname,"
               + "a.out_days as bef_out_days,"
               + "a.tel_no as bef_tel_no,"
               + "a.contact_id as bef_contact_id,"
               + "a.contact_tel as bef_contact_tel,"
               + "a.area_code as bef_area_code,"
               + "a.address1 as bef_address1,"
               + "a.address2 as bef_address2,"
               + "a.address3 as bef_address3,"
               + "a.address4 as bef_address4,"
               + "a.address5 as bef_address5";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.vendor_no")
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
  commDusableFlag("comm_disable_flag");
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
  if (!wp.colStr("vendor_name").equals(wp.colStr("bef_vendor_name")))
     wp.colSet("opt_vendor_name","Y");

  if (!wp.colStr("disable_flag").equals(wp.colStr("bef_disable_flag")))
     wp.colSet("opt_disable_flag","Y");
  commDusableFlag("comm_disable_flag");
  commDusableFlag("comm_bef_disable_flag");

  if (!wp.colStr("id_no").equals(wp.colStr("bef_id_no")))
     wp.colSet("opt_id_no","Y");

  if (!wp.colStr("name").equals(wp.colStr("bef_name")))
     wp.colSet("opt_name","Y");

  if (!wp.colStr("sub_cname").equals(wp.colStr("bef_sub_cname")))
     wp.colSet("opt_sub_cname","Y");

  if (!wp.colStr("out_days").equals(wp.colStr("bef_out_days")))
     wp.colSet("opt_out_days","Y");

  if (!wp.colStr("tel_no").equals(wp.colStr("bef_tel_no")))
     wp.colSet("opt_tel_no","Y");

  if (!wp.colStr("contact_id").equals(wp.colStr("bef_contact_id")))
     wp.colSet("opt_contact_id","Y");

  if (!wp.colStr("contact_tel").equals(wp.colStr("bef_contact_tel")))
     wp.colSet("opt_contact_tel","Y");

  if (!wp.colStr("area_code").equals(wp.colStr("bef_area_code")))
     wp.colSet("opt_area_code","Y");

  if (!wp.colStr("address1").equals(wp.colStr("bef_address1")))
     wp.colSet("opt_address1","Y");

  if (!wp.colStr("address2").equals(wp.colStr("bef_address2")))
     wp.colSet("opt_address2","Y");

  if (!wp.colStr("address3").equals(wp.colStr("bef_address3")))
     wp.colSet("opt_address3","Y");

  if (!wp.colStr("address4").equals(wp.colStr("bef_address4")))
     wp.colSet("opt_address4","Y");

  if (!wp.colStr("address5").equals(wp.colStr("bef_address5")))
     wp.colSet("opt_address5","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("vendor_name","");
       wp.colSet("disable_flag","");
       wp.colSet("id_no","");
       wp.colSet("name","");
       wp.colSet("sub_cname","");
       wp.colSet("out_days","");
       wp.colSet("tel_no","");
       wp.colSet("contact_id","");
       wp.colSet("contact_tel","");
       wp.colSet("area_code","");
       wp.colSet("address1","");
       wp.colSet("address2","");
       wp.colSet("address3","");
       wp.colSet("address4","");
       wp.colSet("address5","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("vendor_name").length()==0)
     wp.colSet("opt_vendor_name","Y");

  if (wp.colStr("disable_flag").length()==0)
     wp.colSet("opt_disable_flag","Y");

  if (wp.colStr("id_no").length()==0)
     wp.colSet("opt_id_no","Y");

  if (wp.colStr("name").length()==0)
     wp.colSet("opt_name","Y");

  if (wp.colStr("sub_cname").length()==0)
     wp.colSet("opt_sub_cname","Y");

  if (wp.colStr("out_days").length()==0)
     wp.colSet("opt_out_days","Y");

  if (wp.colStr("tel_no").length()==0)
     wp.colSet("opt_tel_no","Y");

  if (wp.colStr("contact_id").length()==0)
     wp.colSet("opt_contact_id","Y");

  if (wp.colStr("contact_tel").length()==0)
     wp.colSet("opt_contact_tel","Y");

  if (wp.colStr("area_code").length()==0)
     wp.colSet("opt_area_code","Y");

  if (wp.colStr("address1").length()==0)
     wp.colSet("opt_address1","Y");

  if (wp.colStr("address2").length()==0)
     wp.colSet("opt_address2","Y");

  if (wp.colStr("address3").length()==0)
     wp.colSet("opt_address3","Y");

  if (wp.colStr("address4").length()==0)
     wp.colSet("opt_address4","Y");

  if (wp.colStr("address5").length()==0)
     wp.colSet("opt_address5","Y");

 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  Mktp0260Func func =new Mktp0260Func(wp);

  String[] lsVendorNo = wp.itemBuff("vendor_no");
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

     func.varsSet("vendor_no", lsVendorNo[rr]);
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
       if ((wp.respHtml.equals("mktp0260")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_vendor_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_vendor_no");
             }
          lsSql = "";
          lsSql =  procDynamicDddwVendorNo1(wp.colStr("ex_vendor_no"));
          wp.optionKey = wp.colStr("ex_vendor_no");
          dddwList("dddw_vendor_no_1", lsSql);
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
 public void commDusableFlag(String s1) throws Exception 
 {
  String[] cde = {"Y","N"};
  String[] txt = {"已停用","未停用"};
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
// **************************************************************************************
 String procDynamicDddwCrtUser1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.crt_user as db_code, "
          + " max(b.crt_user||' '||a.usr_cname) as db_desc "
          + " from sec_user a,mkt_vendor_t b "
          + " where a.usr_id = b.crt_user "
          + " and   b.apr_flag = 'N' "
          + " group by b.crt_user "
          ;

   return lsSql;
 }
// **************************************************************************************
 String procDynamicDddwVendorNo1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.vendor_no as db_code, "
          + " max(b.vendor_no||' '||b.vendor_name) as db_desc "
          + " from mkt_vendor_t b "
          + " where   b.apr_flag = 'N' "
          + " group by b.vendor_no "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
