/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/10/30  V1.00.01   Allen Ho      Initial                              *
* 112-02-18  V1.00.02  Machao     sync from mega & updated for project coding standard          *                                                                                                     *
***************************************************************************/
package mktp01;

import mktp01.Mktp0660Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0660 extends BaseProc
{
 private final String PROGNAME = "WEB登錄代碼群組覆核處理程式112-02-18  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0660Func func = null;
  String kk1;
  String km1;
  String fstAprFlag = "";
  String orgTabName = "web_record_group_t";
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
  else if (eqIgno(wp.buttonCode, "R4"))
     {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
     }
  else if (eqIgno(wp.buttonCode, "R2"))
     {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
     }
  else if (eqIgno(wp.buttonCode, "R3"))
     {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
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
              + sqlCol(wp.itemStr2("ex_record_group_no"), "a.record_group_no", "like%")
              + sqlCol(wp.itemStr2("ex_crt_user"), "a.crt_user", "like%")
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
               + "a.record_group_no,"
               + "a.record_group_name,"
               + "a.voice_record_sel,"
               + "a.web_record_sel,"
               + "a.record_cnt_cond,"
               + "a.record_cnt,"
               + "a.record_id_cond,"
               + "a.crt_user,"
               + "a.crt_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by record_group_no"
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

  commVoiceSel("comm_voice_record_sel");
  commWebSel("comm_web_record_sel");
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
  if (wp.itemStr2("kk_record_group_no").length()==0)
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
               + "a.aud_type,"
               + "a.record_group_no as record_group_no,"
               + "a.crt_user,"
               + "a.record_group_name,"
               + "a.active_date_s,"
               + "a.active_date_e,"
               + "a.voice_record_sel,"
               + "a.web_record_sel,"
               + "a.merchant_sel,"
               + "a.mcht_group_sel,"
               + "a.record_cnt_cond,"
               + "a.record_cnt,"
               + "a.record_id_cond,"
               + "a.purchase_cond,"
               + "a.sup_cond";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 ";
  if (qFrom==0)
     {
       wp.whereStr = wp.whereStr
                   + sqlCol(km1, "a.record_group_no")
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
  commVoiceRecord("comm_voice_record_sel");
  commWebRecord("comm_web_record_sel");
  commMerchantSel("comm_merchant_sel");
  mchtGroupSel("comm_mcht_group_sel");
  commCrtUser("comm_crt_user");
  checkButtonOff();
  km1 = wp.colStr("record_group_no");
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
  controlTabName = "WEB_RECORD_GROUP";
  wp.selectSQL = "hex(a.rowid) as rowid,"
               + " nvl(a.mod_seqno,0) as mod_seqno, "
               + "a.record_group_no as record_group_no,"
               + "a.crt_user as bef_crt_user,"
               + "a.record_group_name as bef_record_group_name,"
               + "a.active_date_s as bef_active_date_s,"
               + "a.active_date_e as bef_active_date_e,"
               + "a.voice_record_sel as bef_voice_record_sel,"
               + "a.web_record_sel as bef_web_record_sel,"
               + "a.merchant_sel as bef_merchant_sel,"
               + "a.mcht_group_sel as bef_mcht_group_sel,"
               + "a.record_cnt_cond as bef_record_cnt_cond,"
               + "a.record_cnt as bef_record_cnt,"
               + "a.record_id_cond as bef_record_id_cond,"
               + "a.purchase_cond as bef_purchase_cond,"
               + "a.sup_cond as bef_sup_cond";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereStr = "where 1=1 "
              + sqlCol(km1, "a.record_group_no")
              ;

  pageSelect();
  if (sqlNotFind())
     {
      wp.notFound ="";
      return;
     }
  wp.colSet("control_tab_name",controlTabName); 
  commCrtUser("comm_crt_user");
  commVoiceRecord("comm_voice_record_sel");
  commWebRecord("comm_web_record_sel");
  commMerchantSel("comm_merchant_sel");
  mchtGroupSel("comm_mcht_group_sel");
  checkButtonOff();
  commfuncAudType("aud_type");
  listWkdata();
  listWkdataAft();
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
  wp.colSet("voice_record_sel_cnt" , listMktBnData("mkt_bn_data_t","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"1"));
  wp.colSet("web_record_sel_cnt" , listMktBnData("mkt_bn_data_t","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"2"));
  wp.colSet("merchant_sel_cnt" , listMktBnData("mkt_bn_data_t","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"3"));
  wp.colSet("mcht_group_sel_cnt" , listMktBnData("mkt_bn_data_t","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"4"));
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
  if (!wp.colStr("record_group_name").equals(wp.colStr("bef_record_group_name")))
     wp.colSet("opt_record_group_name","Y");

  if (!wp.colStr("active_date_s").equals(wp.colStr("bef_active_date_s")))
     wp.colSet("opt_active_date_s","Y");

  if (!wp.colStr("active_date_e").equals(wp.colStr("bef_active_date_e")))
     wp.colSet("opt_active_date_e","Y");

  if (!wp.colStr("voice_record_sel").equals(wp.colStr("bef_voice_record_sel")))
     wp.colSet("opt_voice_record_sel","Y");
  commVoiceRecord("comm_voice_record_sel");
  commVoiceRecord("comm_bef_voice_record_sel");

  wp.colSet("bef_voice_record_sel_cnt" , listMktBnData("mkt_bn_data","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"1"));
  if (!wp.colStr("voice_record_sel_cnt").equals(wp.colStr("bef_voice_record_sel_cnt")))
     wp.colSet("opt_voice_record_sel_cnt","Y");

  if (!wp.colStr("web_record_sel").equals(wp.colStr("bef_web_record_sel")))
     wp.colSet("opt_web_record_sel","Y");
  commWebRecord("comm_web_record_sel");
  commWebRecord("comm_bef_web_record_sel");

  wp.colSet("bef_web_record_sel_cnt" , listMktBnData("mkt_bn_data","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"2"));
  if (!wp.colStr("web_record_sel_cnt").equals(wp.colStr("bef_web_record_sel_cnt")))
     wp.colSet("opt_web_record_sel_cnt","Y");

  if (!wp.colStr("merchant_sel").equals(wp.colStr("bef_merchant_sel")))
     wp.colSet("opt_merchant_sel","Y");
  commMerchantSel("comm_merchant_sel");
  commMerchantSel("comm_bef_merchant_sel");

  wp.colSet("bef_merchant_sel_cnt" , listMktBnData("mkt_bn_data","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"3"));
  if (!wp.colStr("merchant_sel_cnt").equals(wp.colStr("bef_merchant_sel_cnt")))
     wp.colSet("opt_merchant_sel_cnt","Y");

  if (!wp.colStr("mcht_group_sel").equals(wp.colStr("bef_mcht_group_sel")))
     wp.colSet("opt_mcht_group_sel","Y");
  mchtGroupSel("comm_mcht_group_sel");
  mchtGroupSel("comm_bef_mcht_group_sel");

  wp.colSet("bef_mcht_group_sel_cnt" , listMktBnData("mkt_bn_data","WEB_RECORD_GROUP",wp.colStr("record_group_no"),"4"));
  if (!wp.colStr("mcht_group_sel_cnt").equals(wp.colStr("bef_mcht_group_sel_cnt")))
     wp.colSet("opt_mcht_group_sel_cnt","Y");

  if (!wp.colStr("record_cnt_cond").equals(wp.colStr("bef_record_cnt_cond")))
     wp.colSet("opt_record_cnt_cond","Y");

  if (!wp.colStr("record_cnt").equals(wp.colStr("bef_record_cnt")))
     wp.colSet("opt_record_cnt","Y");

  if (!wp.colStr("record_id_cond").equals(wp.colStr("bef_record_id_cond")))
     wp.colSet("opt_record_id_cond","Y");

  if (!wp.colStr("purchase_cond").equals(wp.colStr("bef_purchase_cond")))
     wp.colSet("opt_purchase_cond","Y");

  if (!wp.colStr("sup_cond").equals(wp.colStr("bef_sup_cond")))
     wp.colSet("opt_sup_cond","Y");

   if (wp.colStr("aud_type").equals("D"))
      {
       wp.colSet("record_group_name","");
       wp.colSet("active_date_s","");
       wp.colSet("active_date_e","");
       wp.colSet("voice_record_sel","");
       wp.colSet("voice_record_sel_cnt","");
       wp.colSet("web_record_sel","");
       wp.colSet("web_record_sel_cnt","");
       wp.colSet("merchant_sel","");
       wp.colSet("merchant_sel_cnt","");
       wp.colSet("mcht_group_sel","");
       wp.colSet("mcht_group_sel_cnt","");
       wp.colSet("record_cnt_cond","");
       wp.colSet("record_cnt","");
       wp.colSet("record_id_cond","");
       wp.colSet("purchase_cond","");
       wp.colSet("sup_cond","");
      }
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
  if (wp.colStr("record_group_name").length()==0)
     wp.colSet("opt_record_group_name","Y");

  if (wp.colStr("active_date_s").length()==0)
     wp.colSet("opt_active_date_s","Y");

  if (wp.colStr("active_date_e").length()==0)
     wp.colSet("opt_active_date_e","Y");

  if (wp.colStr("voice_record_sel").length()==0)
     wp.colSet("opt_voice_record_sel","Y");


  if (wp.colStr("web_record_sel").length()==0)
     wp.colSet("opt_web_record_sel","Y");


  if (wp.colStr("merchant_sel").length()==0)
     wp.colSet("opt_merchant_sel","Y");


  if (wp.colStr("mcht_group_sel").length()==0)
     wp.colSet("opt_mcht_group_sel","Y");


  if (wp.colStr("record_cnt_cond").length()==0)
     wp.colSet("opt_record_cnt_cond","Y");

  if (wp.colStr("record_cnt").length()==0)
     wp.colSet("opt_record_cnt","Y");

  if (wp.colStr("record_id_cond").length()==0)
     wp.colSet("opt_record_id_cond","Y");

  if (wp.colStr("purchase_cond").length()==0)
     wp.colSet("opt_purchase_cond","Y");

  if (wp.colStr("sup_cond").length()==0)
     wp.colSet("opt_sup_cond","Y");

 }
// ************************************************************************
 public void dataReadR4() throws Exception
 {
  dataReadR4(0);
 }
// ************************************************************************
 public void dataReadR4(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   this.selectNoLimit();
   bnTable = "mkt_bn_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "data_code2, "
                + "data_code3, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'WEB_RECORD_GROUP' "
                ;
   if (wp.respHtml.equals("mktp0660_vore"))
      wp.whereStr  += " and data_type  = '1' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("record_group_no"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("record_group_no") +  "'";
   wp.whereStr  += " order by 4,5,6,7,8 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktp0660_vore"))
    commDtaCode01("comm_data_code");
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
   this.selectNoLimit();
   bnTable = "mkt_bn_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'WEB_RECORD_GROUP' "
                ;
   if (wp.respHtml.equals("mktp0660_were"))
      wp.whereStr  += " and data_type  = '2' ";
   if (wp.respHtml.equals("mktp0660_aaa1"))
      wp.whereStr  += " and data_type  = '4' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("record_group_no"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("record_group_no") +  "'";
   wp.whereStr  += " order by 4,5,6 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
   if (wp.respHtml.equals("mktp0660_were"))
    commWebRecord1("comm_data_code");
   if (wp.respHtml.equals("mktp0660_aaa1"))
    commDataCode34("comm_data_code");
  }
// ************************************************************************
 public void dataReadR3() throws Exception
 {
  dataReadR3(0);
 }
// ************************************************************************
 public void dataReadR3(int fromType) throws Exception
 {
   String bnTable="";

   wp.selectCnt=1;
   this.selectNoLimit();
   bnTable = "mkt_bn_data_t";

   wp.selectSQL = "hex(rowid) as r2_rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "mod_seqno as r2_mod_seqno, "
                + "data_key, "
                + "data_code, "
                + "data_code2, "
                + "mod_user as r2_mod_user "
                ;
   wp.daoTable = bnTable ;
   wp.whereStr = "where 1=1"
              + " and table_name  =  'WEB_RECORD_GROUP' "
                ;
   if (wp.respHtml.equals("mktp0660_mrch"))
      wp.whereStr  += " and data_type  = '3' ";
   String whereCnt = wp.whereStr;
   wp.whereStr  += " and  data_key = :data_key ";
   setString("data_key", wp.itemStr2("record_group_no"));
   whereCnt += " and  data_key = '"+ wp.itemStr2("record_group_no") +  "'";
   wp.whereStr  += " order by 4,5,6,7 ";
   int cnt1=selectBndataCount(wp.daoTable,whereCnt);
   if (cnt1>300)
      {
       alertErr("資料筆數 ["+ cnt1 +"] 無法查詢, 請用(mktq7000)查詢");
       buttonOff("btnUpdate_disable");
       buttonOff("newDetail_disable");
       return;
      }

   pageQuery();
   wp.setListCount(1);
   wp.notFound = "";

   wp.colSet("ex_total_cnt", String.format("%d",wp.selectCnt));
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
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  mktp01.Mktp0660Func func =new mktp01.Mktp0660Func(wp);

  String[] lsRecordGroupNo = wp.itemBuff("record_group_no");
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

     func.varsSet("record_group_no", lsRecordGroupNo[rr]);
     func.varsSet("aud_type", lsAudType[rr]);
     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     if (lsAudType[rr].equals("A"))
        {
        rc =func.dbInsertA4();
        if (rc==1) rc = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }
     else if (lsAudType[rr].equals("U"))
        {
        rc =func.dbUpdateU4();
        if (rc==1) rc  = func.dbDeleteD4Bndata();
        if (rc==1) rc  = func.dbInsertA4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }
     else if (lsAudType[rr].equals("D"))
        {
         rc =func.dbDeleteD4();
        if (rc==1) rc = func.dbDeleteD4Bndata();
        if (rc==1) rc = func.dbDeleteD4TBndata();
        }

     if (rc!=1) alertErr(func.getMsg());
     if (rc == 1)
        {
         commCrtUser("comm_crt_user");
         commVoiceSel("comm_voice_record_sel");
         commWebSel("comm_web_record_sel");
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
       if ((wp.respHtml.equals("mktp0660")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_record_group_no").length()>0)
             {
             wp.optionKey = wp.colStr("ex_record_group_no");
             }
          this.dddwList("dddw_record_gp"
                 ,"web_record_group_t"
                 ,"trim(record_group_no)"
                 ,"trim(substr(record_group_name,1,30))"
                 ," where apr_flag='N'");
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
       if ((wp.respHtml.equals("mktp0660_were")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          if (wp.colStr("kk_data_code").length()>0)
             {
             wp.optionKey = wp.colStr("kk_data_code");
             wp.initOption ="";
             }
          this.dddwList("dddw_web_record"
                 ,"web_activity_parm"
                 ,"trim(WEB_RECORD_NO)"
                 ,"trim(RECORD_NAME)"
                 ," where decode(web_date_e,'','30001231',web_date_e)>=to_char(sysdate,'yyyymmdd') order by web_record_no,WEB_DATE_E desc");
         }
       if ((wp.respHtml.equals("mktp0660_vore")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          if (wp.colStr("kk_data_code").length()>0)
             {
             wp.optionKey = wp.colStr("kk_data_code");
             wp.initOption ="";
             }
          this.dddwList("dddw_voice_record"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='VOICE_LIST'");
         }
       if ((wp.respHtml.equals("mktp0660_aaa1")))
         {
          wp.initOption ="";
          wp.optionKey = "";
          this.dddwList("dddw_mcht_gp"
                 ,"mkt_mcht_gp"
                 ,"trim(mcht_group_id)"
                 ,"trim(mcht_group_desc)"
                 ," where 1 = 1 ");
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
 public void commWebRecord1(String s1) throws Exception 
 {
  commWebRecord1(s1,0);
  return;
 }
// ************************************************************************
 public void commWebRecord1(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " RECORD_NAME as column_RECORD_NAME "
            + " from web_activity_parm "
            + " where 1 = 1 "
            + " and   WEB_RECORD_NO = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_RECORD_NAME"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDtaCode01(String s1) throws Exception 
 {
  commDtaCode01(s1,0);
  return;
 }
// ************************************************************************
 public void commDtaCode01(String s1,int befType) throws Exception 
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
            + " and   wf_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
            + " and   wf_type = 'VOICE_LIST' "
            ;
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_wf_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commDataCode34(String s1) throws Exception 
 {
  commDataCode34(s1,0);
  return;
 }
// ************************************************************************
 public void commDataCode34(String s1,int befType) throws Exception 
 {
  String columnData="";
  String sql1 = "";
  String befStr="";
  if (befType==1) befStr="bef_";
   for (int ii = 0; ii < wp.selectCnt; ii++)
      {
       columnData="";
       sql1 = "select "
            + " mcht_group_desc as column_mcht_group_desc "
            + " from mkt_mcht_gp "
            + " where 1 = 1 "
            + " and   mcht_group_id = '"+wp.colStr(ii,befStr+"data_code")+"'"
            ;
       if (wp.colStr(ii,befStr+"data_code").length()==0)
          {
           wp.colSet(ii, s1, columnData);
           continue;
          }
       sqlSelect(sql1);

       if (sqlRowNum>0)
          columnData = columnData + sqlStr("column_mcht_group_desc"); 
       wp.colSet(ii, s1, columnData);
      }
   return;
 }
// ************************************************************************
 public void commVoiceRecord(String s1) throws Exception 
 {
  String[] cde = {"0","1"};
  String[] txt = {"不選取","指定"};
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
 public void commWebRecord(String s1) throws Exception 
 {
  String[] cde = {"0","1"};
  String[] txt = {"不選取","指定"};
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
 public void commMerchantSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 public void mchtGroupSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 public void commVoiceSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 public void commWebSel(String s1) throws Exception 
 {
  String[] cde = {"0","1","2"};
  String[] txt = {"全部","指定","排除"};
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
 String  listMktBnData(String s1,String s2,String s3,String s4) throws Exception
 {
  String sql1 = "select "
              + " count(*) as column_data_cnt "
              + " from "+ s1 + " "
              + " where 1 = 1 "
              + " and   table_name = '"+s2+"'"
              + " and   data_key   = '"+s3+"'"
              + " and   data_type  = '"+s4+"'"
              ;
  sqlSelect(sql1);

  if (sqlRowNum>0) return(sqlStr("column_data_cnt"));

   return("0");
 }
// ************************************************************************
 String procDynamicDddwCrtUser1(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.crt_user as db_code, "
          + " max(b.crt_user||' '||a.usr_cname) as db_desc "
          + " from sec_user a,web_record_group_t b "
          + " where a.usr_id = b.crt_user "
          + " and   b.apr_flag = 'N' "
          + " group by b.crt_user "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
