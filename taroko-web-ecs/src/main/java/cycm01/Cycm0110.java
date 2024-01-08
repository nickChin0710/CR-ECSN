/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/07  V1.00.01  Allen Ho       Initial                              *
* 111/10/28  V1.00.02  Yang Bo        sync code from mega                  *
* 112/04/18  V1.00.03  Simon          checkApprove() changed to checkApproveZz()*
***************************************************************************/

package cycm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycm0110 extends BaseEdit
{
 private final String PROGNAME = "掛失費率參數資料維護處理程式110/07/07 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  cycm01.Cycm0110Func func = null;
  String kk1,kk2;
  String orgTabName = "cyc_lostfee";
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
              + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type")
              + sqlCol(wp.itemStr("ex_lost_code"), "a.lost_code")
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
               + "a.acct_type,"
               + "a.lost_code,"
               + "a.onus_bank,"
               + "a.onus_auto_pay,"
               + "a.other_auto_pay,"
               + "a.salary_acct,"
               + "a.credit_acct,"
               + "a.credit_limit,"
               + "a.bonus_sel";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.acct_type,a.lost_code"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commAcctType("comm_acct_type");
  commLostCode("comm_lost_code");


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
  if (wp.itemStr("kk_acct_type").length()==0)
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
               + "a.acct_type as acct_type,"
               + "a.lost_code as lost_code,"
               + "'' as lost_fee_cnt,"
               + "a.onus_bank,"
               + "a.onus_auto_pay,"
               + "a.other_auto_pay,"
               + "a.salary_acct,"
               + "a.credit_acct,"
               + "a.credit_limit,"
               + "a.credit_amt,"
               + "a.bonus_sel,"
               + "a.bonus,"
               + "a.lost_limit,"
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
                   + sqlCol(wp.itemStr("kk_acct_type"), "a.acct_type")
                   + sqlCol(wp.itemStr("kk_lost_code"), "a.lost_code")
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
  commAcctType("comm_acct_type");
  commLostCode("comm_lost_code");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
  String s1=wp.colStr("acct_type"); 
  String s2=wp.colStr("lost_code"); 

  wp.colSet("lost_fee_cnt" , "0");
  if (wp.colStr("acct_type").length()==0)
     s1 = wp.colStr("kk_acct_type");
  if (wp.colStr("lost_code").length()==0)
     s1 = wp.colStr("kk_lost_code");

  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from cyc_lostfee_acct "
              + " where acct_type =  '"+s1+"'"
              + " and   lost_code = '"+s2+"'"
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0) wp.colSet("lost_fee_cnt" , sqlStr("column_data_cnt"));


 }
// ************************************************************************
 public void dataReadR2() throws Exception
 {
  if (wp.colStr("acct_type").length()==0)
     {
      wp.colSet("acct_type",itemKk("data_k3"));
      wp.itemSet("acct_type",itemKk("data_k3"));
     }
  if (wp.colStr("lost_code").length()==0)
     {
      wp.colSet("lost_code",itemKk("data_k4"));
      wp.itemSet("lost_code",itemKk("data_k4"));
     }
  dataReadR2(0);
 }
// ************************************************************************
 public void dataReadR2(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   commAcctType("comm_acct_type");
   commLostCode("comm_lost_code");
   this.selectNoLimit();
   bnTable = "cyc_lostfee_acct";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "acct_type, "
                + "lost_code, "
                + "card_note, "
                + "sup_flag, "
                + "lostfee_amt, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
               ;
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  acct_type = :acct_type ";
   setString("acct_type", wp.itemStr("acct_type"));
   whereCnt += " and  acct_type = '"+ wp.itemStr("acct_type") +  "'";
   wp.whereStr  += " and  lost_code = :lost_code ";
   setString("lost_code", wp.itemStr("lost_code"));
   whereCnt += " and  lost_code = '"+ wp.itemStr("lost_code") +  "'";
   wp.whereStr  += " order by 4,5,6,7,8,9 ";
   int cnt1= selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("明細資料已超過300筆，無法線上單筆新增，請使用整批上載功能");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("cycm0110_note"))
    commCardNote("comm_card_note");
   if (wp.respHtml.equals("cycm0110_note"))
    commSupFlag("comm_sup_flag");
  }
// ************************************************************************
 public void updateFuncU2() throws Exception
 {
   wp.listCount[0] = wp.itemBuff("ser_num").length;
 //if (!checkApprove(wp.itemStr("zz_apr_user"), wp.itemStr("zz_apr_passwd"))) return;
	 if(checkApproveZz()==false)	return ;
   Cycm0110Func func =new Cycm0110Func(wp);
   int llOk = 0, llErr = 0;


   String[] optData  = wp.itemBuff("opt");
   String[] key1Data = wp.itemBuff("card_note");
   String[] key2Data = wp.itemBuff("sup_flag");
   String[] key3Data = wp.itemBuff("lostfee_amt");

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
             (key2Data[ll].equals(key2Data[intm]))) 
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

       func.varsSet("card_note", key1Data[ll]); 
       func.varsSet("sup_flag", key2Data[ll]); 
       func.varsSet("lostfee_amt", key3Data[ll]); 

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
 public int selectBndataCount(String bndataTable, String whereStr ) throws Exception
 {
   String sql1 = "select count(*) as bndataCount"
               + " from " + bndataTable
               + " " + whereStr
               ;

   sqlSelect(sql1);

   return((int)sqlNum("bndataCount"));
 }
// ************************************************************************
 @Override
 public void saveFunc() throws Exception
 {
  //-check approve-
//if (!checkApprove(wp.itemStr("zz_apr_user"), wp.itemStr("zz_apr_passwd"))) return;
	if(checkApproveZz()==false)	return ;

  Cycm0110Func func =new Cycm0110Func(wp);

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
  int rr = 0;                       
  rr = wp.listCount[0];             
  wp.colSet(0, "IND_NUM", "" + rr);
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String ls_sql ="";
  try {
       if ((wp.respHtml.equals("cycm0110_detl")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          if (wp.colStr("kk_acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("kk_acct_type");
             wp.initOption ="";
             }
          if (wp.colStr("acct_type").length()>0)
             {
              wp.initOption ="--";
             }
          this.dddwList("dddw_acct_type2"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
          wp.initOption ="";
          wp.optionKey = "";
          if (wp.colStr("kk_lost_code").length()>0)
             {
             wp.optionKey = wp.colStr("kk_lost_code");
             wp.initOption ="";
             }
          if (wp.colStr("lost_code").length()>0)
             {
              wp.initOption ="--";
             }
          this.dddwList("dddw_lost_code3"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type= 'CRD_OPPOST'");
         }
       if ((wp.respHtml.equals("cycm0110")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_acct_type").length()>0)
             {
             wp.optionKey = wp.colStr("ex_acct_type");
             }
          this.dddwList("dddw_acct_type1"
                 ,"ptr_acct_type"
                 ,"trim(acct_type)"
                 ,"trim(chin_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_lost_code").length()>0)
             {
             wp.optionKey = wp.colStr("ex_lost_code");
             }
          this.dddwList("dddw_lost_code1"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type= 'CRD_OPPOST'");
         }
       if ((wp.respHtml.equals("cycm0110_note")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          if (wp.colStr("kk_card_note").length()>0)
             {
             wp.optionKey = wp.colStr("kk_card_note");
             wp.initOption ="";
             }
          this.dddwList("dddw_lost_code4"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type= 'CARD_NOTE'");
         }
      } catch(Exception ex){}
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
 public void commLostCode(String s1) throws Exception 
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
            + " and   wf_id = '"+wp.colStr(ii,"lost_code")+"'"
            + " and   wf_type = 'CRD_OPPOST' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
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
 public void commSupFlag(String s1) throws Exception 
 {
  String[] cde = {"0","1"};
  String[] txt = {"正卡","附卡"};
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
  return;
 }
// ************************************************************************

}  // End of class
