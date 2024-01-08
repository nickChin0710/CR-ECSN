/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/17  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktp02;

import mktp02.Mktp1010Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1010 extends BaseProc
{
 private final String PROGNAME = "紅利基點異動明細檔覆核處理程式111-11-30  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp1010Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "mkt_tr_bonus_t";
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

  funcSelect();
  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
  wp.whereStr = "WHERE 1=1 "
              + sqlCol(wp.itemStr2("ex_bonus_type"), "a.bonus_type")
              + sqlCol(wp.itemStr2("ex_crt_user"), "a.crt_user")
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
               + "a.tran_seqno,"
               + "a.aud_type,"
               + "a.trans_date,"
               + "c.id_no,"
               + "a.acct_type,"
               + "a.to_acct_type,"
               + "a.bonus_pnt,"
               + "a.fee_amt,"
               + "a.method,"
               + "a.crt_user,"
               + "a.p_seqno";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereOrder = " "
                + " order by a.bonus_type,a.crt_user"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }

  commAcctType("comm_acct_type");
  commAcctType2("comm_to_acct_type");
  commCrtUser("comm_crt_user");

  commMethod("comm_method");
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
  if (wp.itemStr2("kk_tran_seqno").length()==0)
     { 
      alertErr("查詢鍵必須輸入");
      return; 
     } 
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
               + "a.to_p_seqno as to_p_seqno,"
               + "a.to_id_p_seqno as to_id_p_seqno,"
               + "a.aud_type,"
               + "a.tran_seqno as tran_seqno,"
               + "a.crt_user,"
               + "a.trans_date,"
               + "a.acct_type,"
               + "a.bonus_type,"
               + "a.to_acct_type,"
               + "a.bonus_pnt,"
               + "a.fee_amt,"
               + "a.id_p_seqno";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.tran_seqno")
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
  commCrtUser("comm_crt_user");
  commAcctType("comm_acct_type");
  commChiName("comm_id_no");
  commBonusType("comm_bonus_type");
  commAcctTypeto("comm_to_acct_type");
  checkButtonOff();
  km1 = wp.colStr("tran_seqno");
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
  wp.colSet("control_tab_name",controlTabName); 
  controlTabName = "MKT_TR_BONUS";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.tran_seqno as tran_seqno,"
               + "a.crt_user as bef_crt_user,"
               + "a.trans_date as bef_trans_date,"
               + "a.acct_type as bef_acct_type,"
               + "a.bonus_type as bef_bonus_type,"
               + "a.to_acct_type as bef_to_acct_type,"
               + "a.bonus_pnt as bef_bonus_pnt,"
               + "a.fee_amt as bef_fee_amt,"
               + "a.id_p_seqno as bef_id_p_seqno";

  wp.daoTable = controlTabName + " a "
              + "JOIN crd_idno c "
              + "ON a.id_p_seqno = c.id_p_seqno "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.tran_seqno")
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
  commCrtUser("comm_crt_user");
  commAcctType("comm_acct_type");
  commChiName("comm_id_no");
  commBonusType("comm_bonus_type");
  commAcctTypeto("comm_to_acct_type");
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
  if (!wp.colStr("trans_date").equals(wp.colStr("bef_trans_date")))
     wp.colSet("opt_trans_date","Y");

  if (!wp.colStr("acct_type").equals(wp.colStr("bef_acct_type")))
     wp.colSet("opt_acct_type","Y");
  commAcctType("comm_acct_type");
  commAcctType("comm_bef_acct_type",1);

  if (!wp.colStr("id_no").equals(wp.colStr("bef_id_no")))
     wp.colSet("opt_id_no","Y");
  commChiName("comm_id_no");
  commChiName("comm_bef_id_no",1);

  if (!wp.colStr("bonus_type").equals(wp.colStr("bef_bonus_type")))
     wp.colSet("opt_bonus_type","Y");
  commBonusType("comm_bonus_type");
  commBonusType("comm_bef_bonus_type",1);

  if (!wp.colStr("to_acct_type").equals(wp.colStr("bef_to_acct_type")))
     wp.colSet("opt_to_acct_type","Y");
  commAcctTypeto("comm_to_acct_type");
  commAcctTypeto("comm_bef_to_acct_type",1);

  if (!wp.colStr("bonus_pnt").equals(wp.colStr("bef_bonus_pnt")))
     wp.colSet("opt_bonus_pnt","Y");

  if (!wp.colStr("fee_amt").equals(wp.colStr("bef_fee_amt")))
     wp.colSet("opt_fee_amt","Y");

  if (!wp.colStr("id_p_seqno").equals(wp.colStr("bef_id_p_seqno")))
     wp.colSet("opt_id_p_seqno","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("trans_date","");
       wp.colSet("acct_type","");
       wp.colSet("id_no","");
       wp.colSet("bonus_type","");
       wp.colSet("to_acct_type","");
       wp.colSet("bonus_pnt","");
       wp.colSet("fee_amt","");
       wp.colSet("id_p_seqno","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("trans_date").length()==0)
     wp.colSet("opt_trans_date","Y");

  if (wp.colStr("acct_type").length()==0)
     wp.colSet("opt_acct_type","Y");

  if (wp.colStr("id_no").length()==0)
     wp.colSet("opt_id_no","Y");

  if (wp.colStr("bonus_type").length()==0)
     wp.colSet("opt_bonus_type","Y");

  if (wp.colStr("to_acct_type").length()==0)
     wp.colSet("opt_to_acct_type","Y");

  if (wp.colStr("bonus_pnt").length()==0)
     wp.colSet("opt_bonus_pnt","Y");

  if (wp.colStr("fee_amt").length()==0)
     wp.colSet("opt_fee_amt","Y");

  if (wp.colStr("id_p_seqno").length()==0)
     wp.colSet("opt_id_p_seqno","Y");

 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  mktp02.Mktp1010Func func =new mktp02.Mktp1010Func(wp);

  String[] lsTranSeqno = wp.itemBuff("tran_seqno");
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

     func.varsSet("tran_seqno", lsTranSeqno[rr]);
     func.varsSet("aud_type", lsAudType[rr]);
     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     if (lsAudType[rr].equals("A"))
        rc =func.dbInsertA4();
     else if (lsAudType[rr].equals("U"))
        rc =func.dbUpdateU4();
     else if (lsAudType[rr].equals("D"))
        rc =func.dbDeleteD4();

     if (rc!=1) alertErr(func.getMsg());
     if (rc == 1)
        {
         commAcctType("comm_acct_type");
         commAcctType2("comm_to_acct_type");
         commCrtUser("comm_crt_user");
         commMethod("comm_method");
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
       if ((wp.respHtml.equals("mktp1010")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_bonus_type").length()>0)
             {
             wp.optionKey = wp.colStr("ex_bonus_type");
             }
          this.dddwList("dddw_bonus_type"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='BONUS_NAME'");
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
 public void commCrtUser(String s1,int befType) throws Exception 
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
 public void commAcctType(String s1) throws Exception 
 {
  commAcctType(s1,0);
  return;
 }
// ************************************************************************
 public void commAcctType(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chin_name as column_chin_name "
            + " from ptr_acct_type "
            + " where 1 = 1 "
            + " and   acct_type = '"+wp.colStr(ii,befStr+"acct_type")+"'"
            ;
       if (wp.colStr(ii,befStr+"acct_type").length()==0)
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
 public void commChiName(String s1) throws Exception 
 {
  commChiName(s1,0);
  return;
 }
// ************************************************************************
 public void commChiName(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chi_name as column_chi_name "
            + " from crd_idno "
            + " where 1 = 1 "
            + " and   id_p_seqno = '"+wp.colStr(ii,befStr+"id_p_seqno")+"'"
            ;
       if (wp.colStr(ii,befStr+"id_p_seqno").length()==0)
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
 public void commBonusType(String s1) throws Exception 
 {
  commBonusType(s1,0);
  return;
 }
// ************************************************************************
 public void commBonusType(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " wf_desc as column_wf_desc "
            + " from ptr_sys_idtab "
            + " where 1 = 1 "
            + " and   wf_type = 'BONUS_NAME' "
            + " and   wf_id = '"+wp.colStr(ii,befStr+"bonus_type")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commAcctTypeto(String s1) throws Exception 
 {
  commAcctTypeto(s1,0);
  return;
 }
// ************************************************************************
 public void commAcctTypeto(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chin_name as column_chin_name "
            + " from ptr_acct_type "
            + " where 1 = 1 "
            + " and   acct_type = '"+wp.colStr(ii,befStr+"to_acct_type")+"'"
            ;
       if (wp.colStr(ii,befStr+"to_acct_type").length()==0)
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
 public void commAcctType2(String s1) throws Exception 
 {
  commAcctType2(s1,0);
  return;
 }
// ************************************************************************
 public void commAcctType2(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " chin_name as column_chin_name "
            + " from ptr_acct_type "
            + " where 1 = 1 "
            + " and   acct_type = '"+wp.colStr(ii,befStr+"to_acct_type")+"'"
            ;
       if (wp.colStr(ii,befStr+"to_acct_type").length()==0)
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
 public void commMethod(String s1) throws Exception 
 {
  String[] cde = {"0","1"};
  String[] txt = {"線上","語音"};
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
 public void funcSelect() throws Exception
 {
  return;
 }
// ************************************************************************
// ************************************************************************
 String procDynamicDddwCrtUser1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.crt_user as db_code, "
          + " max(b.crt_user||' '||a.usr_cname) as db_desc "
          + " from sec_user a,mkt_tr_bonus_t b "
          + " where a.usr_id = b.crt_user "
          + " group by b.crt_user "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
