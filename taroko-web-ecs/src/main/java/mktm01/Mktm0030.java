/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/10/29  V1.00.09   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                          *
* 112-01-18  V1.00.02  Zuwei Su       新增時禁用按鈕“來源群組明細” ,新增成功增加提示“明細資料, 請於主檔新增後維護!”                                                                         *
***************************************************************************/
package mktm01;

import mktm01.Mktm0030Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0030 extends BaseEdit
{
 private final String PROGNAME = "交易來源群組代碼維護處理程式111-11-28  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0030Func func = null;
  String kk1,kk2;
  String orgTabName = "mkt_bonus_src";
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
  else if (eqIgno(wp.buttonCode, "procMethod_DUP"))
     {/* 重複檢核 */
      strAction = "U";
      procMethodDUP();
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
  else if (eqIgno(wp.buttonCode, "R2"))
     {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
     }
  else if (eqIgno(wp.buttonCode, "U2"))
     {/* 明細更新 */
      strAction = "U2";
      updateFuncU2();
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
               + "a.tran_src_code,"
               + "a.stat_type,"
               + "a.tran_src_desc,"
               + "a.stat_flag,"
               + "'' as tran_pgm_cnt,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by tran_src_code"
                ;

  pageQuery();
  listWkdata();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commCrtUser("comm_crt_user");

  commStatType("comm_stat_type");

  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 void listWkdata()  throws Exception
 {
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       wp.colSet(ii, "tran_pgm_cnt" , listMktBonusSrcdtl("mkt_bonus_srcdtl"
                                 ,wp.colStr(ii,"tran_src_code")
                                 ,wp.colStr(ii, "stat_type"),""));

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
  if (wp.itemStr2("kk_tran_src_code").length()==0)
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
               + "a.tran_src_code as tran_src_code,"
               + "a.stat_type as stat_type,"
               + "a.tran_src_desc,"
               + "a.stat_flag,"
               + "'' as tran_pgm_cnt,"
               + "a.crt_date,"
               + "a.crt_user";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(wp.itemStr2("kk_tran_src_code"), "a.tran_src_code")
                   + sqlCol(wp.itemStr2("kk_stat_type"), "a.stat_type")
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
      alertErr("查無資料, key= "+"["+ kk1+"]"+"["+ kk2+"]");
      return;
     }
  datareadWkdata();
  commStatType("comm_stat_type");
  commCrtUser("comm_crt_user");
  checkButtonOff();
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
  wp.colSet("tran_pgm_cnt" , listMktBonusSrcdtl("mkt_bonus_srcdtl"
                            ,wp.colStr("tran_src_code")
                            ,wp.colStr("stat_type"),""));

 }
// ************************************************************************
 public void dataReadR2() throws Exception
 {
  dataReadR2(0);
 }
// ************************************************************************
 public void dataReadR2(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   commStatType("comm_stat_type");
   this.selectNoLimit();
   bnTable = "mkt_bonus_srcdtl";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "0 as r2_mod_seqno, "
                + "tran_src_code, "
                + "stat_type, "
                + "tran_pgm, "
                + "tran_code, "
                + "sign_flag, "
                + "mod_pgm  as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
                ;
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  tran_src_code = :tran_src_code ";
   setString("tran_src_code", wp.itemStr2("tran_src_code"));
   whereCnt += " and  tran_src_code = '"+ wp.itemStr2("tran_src_code") +  "'";
   wp.whereStr  += " and  stat_type = :stat_type ";
   setString("stat_type", wp.itemStr2("stat_type"));
   whereCnt += " and  stat_type = '"+ wp.itemStr2("stat_type") +  "'";
   wp.whereStr  += " order by 4,5,6,7,8,9 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上戴功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktm0030_mrcd"))
    commTranPgm("comm_tran_pgm");
   if (wp.respHtml.equals("mktm0030_mrcd"))
    commTranCode("comm_tran_code");
   if (wp.respHtml.equals("mktm0030_mrcd"))
    commSignFlag("comm_sign_flag");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   mktm01.Mktm0030Func func =new mktm01.Mktm0030Func(wp);
   int llOk = 0, llErr = 0;

    if (wp.itemStr2("tran_src_desc").length()==0)
       wp.colSet("tran_src_desc", wp.itemStr2("funcdsp_tran_src_desc"));

   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("tran_pgm");
   String[] key2Data = wp.itemBuff("tran_code");
   String[] key3Data = wp.itemBuff("sign_flag");

   wp.listCount[0] = key1Data.length;
   wp.colSet("IND_NUM", "" + key1Data.length);
   //-check duplication-

   int del2Flag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       del2Flag=0;
       wp.colSet(ll, "ok_flag", "");

       for (int intm=ll+1;intm<key1Data.length; intm++)
         if ((key1Data[ll].equals(key1Data[intm])) &&
             (key2Data[ll].equals(key2Data[intm])) &&
             (key3Data[ll].equals(key3Data[intm]))) 
            {
             for (int intx=0;intx<optData.length;intx++) 
              { 
               if (optData[intx].length()!=0) 
               if (((ll+1)==Integer.valueOf(optData[intx]))||
                   ((intm+1)==Integer.valueOf(optData[intx])))
                  {
                   del2Flag=1;
                   break;
                  }
              }
             if (del2Flag==1) break;

             wp.colSet(ll, "ok_flag", "!");
             llErr++;
             continue;
            }
      }

   if (llErr > 0)
      {
       alertErr("資料值重複 : " + llErr);
       return;
      }

   //-delete no-approve-
   if (func.dbDeleteD2() < 0)
      {
       alertErr(func.getMsg());
       return;
      }

   //-insert-
   int deleteFlag=0;
   for (int ll = 0; ll < key1Data.length; ll++)
      {
       deleteFlag=0;
       //KEY 不可同時為空字串
           if ((empty(key1Data[ll])) &&
                  (empty(key2Data[ll])) &&
              (empty(key3Data[ll])))
           continue;

       //-option-ON-
       for (int intm=0;intm<optData.length;intm++)
         {
          if (optData[intm].length()!=0)
          if ((ll+1)==Integer.valueOf(optData[intm]))
             {
              deleteFlag=1;
              break;
             }
          }
       if (deleteFlag==1) continue;

       func.varsSet("tran_pgm", key1Data[ll]); 
       func.varsSet("tran_code", key2Data[ll]); 
       func.varsSet("sign_flag", key3Data[ll]); 

       if (func.dbInsertI2() == 1) llOk++;
       else llErr++;

       //有失敗rollback，無失敗commit
       sqlCommit(llOk > 0 ? 1 : 0);
      }
   alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

   //SAVE後 SELECT
   dataReadR2(1);
 }
// ************************************************************************
 public int selectBndataCount(String bndataTable,String whereStr ) throws Exception
 {
   String sql1 = "select count(*) as bndataCount"
               + " from " + bndataTable
               + " " + whereStr
               ;

   sqlSelect(sql1);

   return((int)sqlNum("bndataCount"));
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktm01.Mktm0030Func func =new mktm01.Mktm0030Func(wp);

  rc = func.dbSave(strAction);
  if (rc!=1) alertErr(func.getMsg());
  else
  {
   if (wp.respHtml.indexOf("_detl") > 0 && this.isAdd())
      alertMsg("明細資料, 請於主檔新增後維護!");
  }
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
  int rr = 0;                       
  rr = wp.listCount[0];             
  wp.colSet(0, "IND_NUM", "" + rr);
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String lsSql ="";
  try {
       if ((wp.respHtml.equals("mktm0030_mrcd")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_tran_pgm"
                 ,"mkt_bonus_srcdtl"
                 ,"trim(tran_pgm)"
                 ,"trim(tran_pgm_desc)"
                 ," where tran_src_code='XXX' and stat_type='X' and tran_code!='Y'");
         }
      } catch(Exception ex){}
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
 public void commTranPgm(String s1) throws Exception 
 {
  String columnData="";
  String sql1 = "";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " tran_pgm_desc as column_tran_pgm_desc "
            + " from mkt_bonus_srcdtl "
            + " where 1 = 1 "
            + " and   TRAN_SRC_CODE = 'XXX' "
            + " and   stat_type = 'X' "
            + " and   tran_pgm = '"+wp.colStr(ii,"tran_pgm")+"'"
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_tran_pgm_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commStatType(String s1) throws Exception 
 {
  String[] cde = {"1","2"};
  String[] txt = {"新增","使用"};
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
 public void commTranCode(String s1) throws Exception 
 {
  String[] cde = {"0","1","2","3","4","5","6","7"};
  String[] txt = {"移轉","新增","贈與","調整","使用","匯入","移除","扣回"};
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
 public void commSignFlag(String s1) throws Exception 
 {
  String[] cde = {"1","2",""};
  String[] txt = {"正值","負值",""};
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
 public void procMethodDUP() throws Exception
  {
  wp.listCount[0] = wp.itemBuff("ser_num").length;

  String sql1 = "";
  sql1 = "select tran_pgm,  "
       + "       tran_code, "
       + "       sign_flag, "
       + "       count(*) as dup_cnt "
       + "from   mkt_bonus_srcdtl "
       + "where  tran_src_code != 'XXX' "
       + "group by tran_pgm,tran_code,sign_flag "
       + "having count(*) > 1 "
       ;

  sqlSelect(sql1);
  if (sqlRowNum <= 0) return;

  String errDesc = "程式代碼:["+ sqlStr("tran_pgm")+"]同時存在於 ";
  sql1 = "select tran_src_code  "
       + "from   mkt_bonus_srcdtl "
       + "where  tran_pgm  = '" + sqlStr("tran_pgm") + "' "
       + "and    tran_code = '" + sqlStr("tran_code") + "' "
       + "and    sign_flag = '" + sqlStr("sign_flag") + "' "
       ;

  sqlSelect(sql1);
  int recCnt = sqlRowNum;

  for (int ii=0;ii<recCnt;ii++)
    {
     errDesc = errDesc 
              + "["+sqlStr(ii,"tran_src_code")+"]";
    }

  errDesc = errDesc  + " 來源群組中";
  alertMsg( errDesc);

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
     buttonOff("btnmrcd_disable");
  return;
 }
// ************************************************************************
 String  listMktBonusSrcdtl(String s1,String s2,String s3,String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where 1 = 1 "
              + " and   tran_src_code = '"+s2+"'"
              + " and   stat_type      = '"+s3+"'"
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

   return("0");
 }
// ************************************************************************

}  // End of class
