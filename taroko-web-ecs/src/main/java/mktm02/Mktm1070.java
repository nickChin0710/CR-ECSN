/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/07  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110-11-23  V1.00.05  Yangbo       joint sql replace to parameters way    *
* 111-11-28  V1.00.06  Zuwei        Sync from mega                          *
* 111-11-30  V1.00.07  Zuwei        資料匯入處理筆數為0的校驗，匯入檔案後自動查詢並刷新列表                          *
***************************************************************************/
package mktm02;

import mktm02.Mktm1070Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1070 extends BaseEdit
{
 private final String PROGNAME = "高鐵車廂升等分行分潤名單上傳處理程式111/11/30 V1.00.07";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1070Func func = null;
  String rowid;
  String orgTabName = "mkt_uploadfile_ctl";
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
  String[] hideStr = new String[11];

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
  else if (eqIgno(wp.buttonCode, "procMethod_DELE"))
     {/* 刪除 */
      strAction = "U";
      procMethodDELE();
     }
  else if (eqIgno(wp.buttonCode, "procMethod_CONF"))
     {/* 檢核確認 */
      strAction = "U";
      procMethodCONF();
     }
  else if (eqIgno(wp.buttonCode, "procMethod_UCON"))
     {/* 解除確認 */
      strAction = "U";
      procMethodUCON();
     }
  else if (eqIgno(wp.buttonCode, "procMethod_NDEL"))
     {/* 交易序號刪除(已覆核) */
      strAction = "U";
      procMethodNDEL();
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
  else if (eqIgno(wp.buttonCode, "UPLOAD0"))
     {/* 匯入檔案 */
      procUploadFile(0);
      checkButtonOff();
      wp.sqlCmd="";
      queryFunc();
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
              + sqlChkEx(wp.itemStr("ex_file_name"), "2", "")
              + sqlStrend(wp.itemStr("ex_file_date_s"), wp.itemStr("ex_file_date_e"), "a.file_date")
              + " and a.file_type  =  'MKT_THSR_UPIDNO_2' "
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
               + "a.file_date,"
               + "a.file_time,"
               + "a.file_name,"
               + "a.trans_seqno,"
               + "a.file_flag,"
               + "a.file_cnt,"
               + "a.error_cnt,"
               + "a.apr_flag,"
               + "a.proc_date,"
               + "a.crt_user,"
               + "a.crt_date,"
               + "a.apr_user,"
               + "a.apr_date";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.file_date desc,file_time desc"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

  commFileFlag("comm_file_flag");
  commAprFlag("comm_apr_flag");

  //list_wkdata();
  wp.setPageValue();
 }
// ************************************************************************
 @Override
  public void querySelect() throws Exception
 {

  rowid = itemKk("data_k1");
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
               + "a.file_type,"
               + "a.type_name,"
               + "a.file_date,"
               + "substr(file_time,1,2)||':'||substr(file_time,3,2)||':'||substr(file_time,5,2) as file_time,"
               + "a.file_name,"
               + "a.file_flag,"
               + "a.error_desc,"
               + "a.file_cnt,"
               + "a.file_amt1,"
               + "a.error_cnt,"
               + "a.proc_flag,"
               + "a.trans_seqno,"
               + "a.proc_date,"
               + "a.crt_user,"
               + "a.apr_flag,"
               + "a.crt_date,"
               + "a.apr_user,"
               + "a.apr_date,"
               + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
               + "a.mod_pgm";

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
                   +  sqlRowId(rowid, "a.rowid")
                   ;
     }

  pageSelect();
  if (sqlNotFind())
     {
      alertErr("查無資料, key= "+"["+ rowid + "]");
      return;
     }
  commFileFlag("comm_file_flag");
  commProcFlag("comm_proc_flag");
  commAprFlag("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  checkButtonOff();
 }
// ************************************************************************
 public void saveFunc() throws Exception
 {
  mktm02.Mktm1070Func func =new mktm02.Mktm1070Func(wp);

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

  wp.colSet("btnUpdate_disable","");
  wp.colSet("btnconf_disable","");
  wp.colSet("btnucon_disable","");
  wp.colSet("btnndel_disable","");

  if (!wp.colStr("file_flag").equals("Y"))
     {
      buttonOff("btnconf_disable");
      buttonOff("btnucon_disable");
      buttonOff("btnndel_disable");
     }
  else if (wp.colStr("proc_flag").equals("N"))
     {
      buttonOff("btnucon_disable");
     }
  else if (wp.colStr("proc_flag").equals("C"))
     {
      buttonOff("btnUpdate_disable");
      buttonOff("btnconf_disable");
     }
  if (wp.colStr("apr_flag").equals("Y"))
     {
      buttonOff("btnUpdate_disable");
      buttonOff("btnconf_disable");
      buttonOff("btnucon_disable");
      wp.colSet("btnndel_disable","");
     }
  else
     {
      buttonOff("btnndel_disable");
     }
  


  if (wp.autUpdate())
     wp.colSet("img_display","");
  else
     wp.colSet("img_display","none");
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
 }
// ************************************************************************
 public String sqlChkEx(String exCol,String sqCond,String fileExt) throws Exception
 {
  if (sqCond.equals("2"))
     {
      return " and file_name like  '%"+ wp.itemStr("ex_file_name")+"%'";
     }

  return "";
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
 public void commFileFlag(String s1) throws Exception 
 {
  String[] cde = {"Y","N"};
  String[] txt = {"成功","失敗"};
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
 public void commProcFlag(String s1) throws Exception 
 {
  String[] cde = {"Y","N","C"};
  String[] txt = {"已處理","待確認","已確認"};
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
 public void commAprFlag(String s1) throws Exception 
 {
  String[] cde = {"Y","N","X","T"};
  String[] txt = {"已覆核","待覆核","不同意匯入","失敗"};
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
public void procUploadFile(int loadType) throws Exception
 {
  if (wp.colStr(0,"ser_num").length()>0)
     wp.listCount[0] = wp.itemBuff("ser_num").length;
  if (wp.itemStr("zz_file_name").indexOf(".xls")!=-1) 
     {
      alertErr("上傳格式: 不可為 excel 格式");
      return;
     }
  if (itemIsempty("zz_file_name"))
     {
      alertErr("上傳檔名: 不可空白");
      return;
     }

  if (loadType==0) fileDataImp0();
 }
// ************************************************************************
int fileUpLoad()
 {
  TarokoUpload func = new TarokoUpload();
  try {
       func.actionFunction(wp);
       wp.colSet("zz_file_name", func.fileName);
      }
   catch(Exception ex)
      {
       return -1;
      }

   return func.rc;
}
// ************************************************************************
void fileDataImp0() throws Exception
 {
  TarokoFileAccess tf = new TarokoFileAccess(wp);
  mktm02.Mktm1070Func func =new mktm02.Mktm1070Func(wp);
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);

  String inputFile = wp.itemStr("zz_file_name");
  int fi = tf.openInputText(inputFile,"MS950");

  if (fi == -1) return;

  tranSeqStr = comr.getSeqno("MKT_MODSEQ");
  for (int inti=0;inti<10;inti++) hideStr[inti]="";
  if (selectMktUploadfileCtl()>0)
     {
      alertErr("同日不可上傳同檔名資料: 重複上傳");
      return;
     }
  func.dbDeleteMktUploadfileDataP(upGroupType);

  String ss="";
  int llOk=0, llCnt=0,llErr=0;
  int lineCnt =0;
  while (true)
   {
    ss = tf.readTextFile(fi);
    if (tf.endFile[fi].equals("Y")) break;
    lineCnt++;
    if (lineCnt<2) continue;

    if (ss.length() < 2) continue;
    llCnt++; 

    for (int inti=0;inti<10;inti++) logMsg[inti]="";
    logMsg[10]=String.format("%02d",lineCnt);

   if (checkUploadfile(ss,llCnt)!=0) continue;

   if (errorCnt==0)
      {
       llOk++;
       rc = func.dbInsertMktUploadfileData(tranSeqStr,
                                              uploadFileCol,
                                              uploadFileDat,
                                              colNum, 
                                              upGroupType,
                                              lineCnt);
       if (rc!=1) llErr++;
      }
   }
  
  if (llCnt == 0) {
      alertErr("資料匯入處理筆數: 0");
      return;
  }

  if ((errorCnt>0)||(llErr>0))
     {
      func.dbDeleteMktUploadfileData(tranSeqStr);
      func.dbInsertEcsNotifyLog(tranSeqStr,errorCnt);
     }
  else if (notifyCnt==1)
     {
      func.dbInsertEcsNotifyLog(tranSeqStr,errorCnt);
     }
  if (llErr>0)
     {
      alertErr("資料異常系統無法新增, 請洽資訊人員! ");
      return;
     }
  ss ="資料匯入處理筆數: " + (llOk+errorCnt) + ", 成功 = " + llOk + ", 錯誤 = " + errorCnt;
  if (errorCnt>0)
     {
      alertErr(ss+" 請查詢 ecsq0040 確認錯誤原因! ");
      return;
     }

  func.dbInsertMktUploadfileCtl(tranSeqStr,llOk,errorCnt,hideStr,datachkCnt);

  sqlCommit(1);  // 1:commit else rollback

  alertMsg(ss);

  tf.closeInputText(fi);
  tf.deleteFile(inputFile);

  return;
 }
// ************************************************************************

int  checkUploadfile(String ss,int llCnt) throws Exception
 {
  mktm02.Mktm1070Func func =new mktm02.Mktm1070Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "file_date";
  uploadFileCol[1]  = "file_type";
  uploadFileCol[2]  = "id_no";
  uploadFileCol[3]  = "branch_code";
  uploadFileCol[4]  = "free_date_s";
  uploadFileCol[5]  = "free_date_e";
  uploadFileCol[6]  = "free_cnt";

  // ========  [I]nsert table column  ========
  uploadFileCol[7]  = "crt_user";
  uploadFileCol[8]  = "crt_date";
  uploadFileCol[9]  = "file_name";
  uploadFileCol[10]  = "trans_seqno";
  uploadFileCol[11]  = "crt_time";
  uploadFileCol[12]  = "upidno_seqno";

  colNum = 13;

  // ==== insert table content default =====
  uploadFileDat[7]  = wp.loginUser;
  uploadFileDat[8]  = wp.sysDate;
  uploadFileDat[9]  = wp.itemStr("zz_file_name");
  uploadFileDat[10]  = tranSeqStr;
  uploadFileDat[11]  = wp.sysTime;

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  for (int inti=0;inti<7;inti++)
      {
       uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);

  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  uploadFileDat[12] =  comr.getSeqno("MKT_MODSEQ");

  if (uploadFileDat[0].length()==0) return(1);;
  if (uploadFileDat[1].equals("Y"))
     uploadFileDat[1] = "1";
  else
     uploadFileDat[1] = "2";

  if (selectGenBrn()!=0)
     {
      errorCnt++;            
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "4";                    // 欄位位置
      logMsg[3]               = uploadFileDat[3];       // 欄位內容
      logMsg[4]               = "分行別代碼錯誤";          // 錯誤說明
      logMsg[5]               = "分行代碼不存在資料庫";     // 欄位說明

      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
     }
  if (hideStr[2].length()==0)
     {
      if (uploadFileDat[1].equals("1"))
          hideStr[1] = "財管";
      else
          hideStr[1] = "分行:"+uploadFileDat[3]+"-"+sqlStr("full_chi_name");
      hideStr[2]=uploadFileDat[3];
     }
  else
    {
     if (hideStr[3].length()==0)
        {
         if (!hideStr[2].equals(uploadFileDat[3]))
            {
             hideStr[3]="MANY";
             hideStr[1] = "為混合不同來源之檔案資料";
            }
        }
    }

  if ( !comm.isNumber(uploadFileDat[6]))
     {
      errorCnt++;
      logMsg[0]               = "資料檢核錯誤";         // 原因說明
      logMsg[1]               = "3";                    // 錯誤類別
      logMsg[2]               = "7";                    // 欄位位置
      logMsg[3]               = uploadFileDat[5];       // 欄位內容
      logMsg[4]               = "免費次數非數值";          // 錯誤說明
      logMsg[5]               = "免費次數必須為數值";     // 欄位說明

      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
     }
      
  if ((!comm.checkDateFormat(uploadFileDat[0],"yyyyMMdd"))||    // YYYYMMDD 是錯誤的,
      (uploadFileDat[0].length()!=8))    
     {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "1";                    // 欄位位置
      logMsg[3]               = uploadFileDat[0];   // 欄位內容      
      logMsg[4]               = "資料日期錯誤";    // 錯誤說明
      logMsg[5]               = "資料日期格式-YYYYMMD";   // 欄位說明

      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
     }
  if ((!comm.checkDateFormat(uploadFileDat[4],"yyyyMMdd"))||    // YYYYMMDD 是錯誤的,
      (uploadFileDat[4].length()!=8))    
     {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "5";                    // 欄位位置
      logMsg[3]               = uploadFileDat[4];       // 欄位內容
      logMsg[4]               = "免費日期-起錯誤";    // 錯誤說明
      logMsg[5]               = "免費日期格式非YYYYMMD";  // 欄位說明 
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
     }
  if ((!comm.checkDateFormat(uploadFileDat[5],"yyyyMMdd"))||    // YYYYMMDD 是錯誤的,
      (uploadFileDat[5].length()!=8))    
     {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "6";                    // 欄位位置
      logMsg[3]               = uploadFileDat[5];       // 欄位內容
      logMsg[4]               = "免費日期-迄錯誤";    // 錯誤說明
      logMsg[5]               = "免費日期格式非YYYYMMD";  // 欄位說明 
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
     }
  if (selectMktUploadfileData()!=0)
     {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "3";                    // 欄位位置
      logMsg[3]               = uploadFileDat[2];       // 欄位內容
      logMsg[4]               = "身分證號錯誤";         // 錯誤說明
      logMsg[5]               = "身分證號資料重複";    // 欄位說明
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);

     }
  datachkCnt[3]=datachkCnt[3] + Integer.valueOf(uploadFileDat[6]);

  return 0;
 }
// ************************************************************************
// ************************************************************************
 int  selectMktUploadfileCtl() throws Exception
 {
  wp.sqlCmd = " select "
              + " 1 as rowdata "
              + " from mkt_uploadfile_ctl "
              + " where file_type   = 'MKT_THSR_UPIDNO' "
              + " and   file_date   = '"+wp.sysDate+"' "
              + " and   file_flag   = 'Y' "
              + " and   file_name   = '"+wp.itemStr("zz_file_name")+"' ";
              ;
  this.sqlSelect();

  if (sqlRowNum>0) return(1);

  return(0);
 }
// ************************************************************************
 int  selectMktUploadfileData() throws Exception
 {
  wp.sqlCmd = " select "
            + " data_data01 "
            + " from mkt_uploadfile_data "
            + " where trans_seqno  = '" + tranSeqStr + "' "
            ;
  this.sqlSelect();

  if (sqlRowNum<=0) return(0);
  String fileType="",idNo="",branchCode="";
  for (int inti=0;inti<sqlRowNum;inti++)
    {
//      wp.ddd("STEP 2  data01 [" + sql_ss(inti,"data_data01") +"]");
      fileType   = comm.getStr(sqlStr(inti,"data_data01"), 2  ,"|");
      idNo       = comm.getStr(sqlStr(inti,"data_data01"), 3  ,"|");
      branchCode = comm.getStr(sqlStr(inti,"data_data01"), 4  ,"|");

      wp.log("file_type [ " + fileType +"][" + uploadFileDat[1]+"]");
      wp.log("id_no     [ " + idNo     +"][" + uploadFileDat[2]+"]");
      wp.log("branch    [ " + branchCode +"][" + uploadFileDat[3]+"]");
      if ((fileType.equals(uploadFileDat[1]))&&
          (idNo.equals(uploadFileDat[2]))&&
          (branchCode.equals(uploadFileDat[3]))) return(1);
    }

  return(0);
 }
// ************************************************************************
 int  selectGenBrn() throws Exception
 {
  wp.sqlCmd = " select "
            + " full_chi_name "
            + " from gen_brn "
            + " where branch  = '"+uploadFileDat[3]+"' ";
            ;
  this.sqlSelect();
  
  if (sqlRowNum<=0) return(1);
  
  return(0);
 }
// ************************************************************************


// ************************************************************************
 public void procMethodDELE() throws Exception
  {
  wp.selectCnt =1;
  commFileFlag("comm_file_flag");
  commProcFlag("comm_proc_flag");
  commAprFlag("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

 if (wp.itemStr("proc_flag").equals("Y"))
     {
      alertErr("已完成覆核及處理, 不可刪除");
      return;
     }
 if (wp.itemStr("proc_flag").equals("C"))
     {
      alertErr("已完成檢核確認, 不可刪除");
      return;
     }

  mktm02.Mktm1070Func func =new mktm02.Mktm1070Func(wp);
  func.dbDeleteMktUploadfileCtl();

  super.errmsg("資料已刪除完成 !",true);
  clearFunc();

  }
// ************************************************************************
 public void procMethodCONF() throws Exception
  {
  wp.selectCnt =1;
  commFileFlag("comm_file_flag");
  commProcFlag("comm_proc_flag");
  commAprFlag("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

  if (!wp.itemStr("file_flag").equals("Y"))
     {
      alertErr("上傳失敗, 不可確認/解確認");
      return;
     }
  if ((wp.itemStr("apr_flag").equals("Y"))&&
      (!wp.itemStr("proc_flag").equals("C")))
     {
      alertErr("已覆核, 不可確認/解確認");
      return;
     }

  if (wp.itemStr("proc_flag").equals("C"))
     {
      alertErr("已完成檢核確認, 不可再處理");
      return;
     }
  if (wp.itemStr("proc_flag").equals("Y"))
     {
      alertErr("已完成覆核及處理, 不可再確認");
      return;
     }

  mktm02.Mktm1070Func func =new mktm02.Mktm1070Func(wp);
  func.dbupdateMktUploadfileCtl("Y","C");
  wp.colSet("proc_flag" , "C");
  wp.colSet("comm_proc_flag" , "已確認");
  commProcFlag("comm_proc_flag");
  wp.colSet("apr_flag" , "N");
  wp.colSet("apr_date" , "");
  wp.colSet("apr_user" , "");
  commAprFlag("comm_apr_flag");

  errmsg("資料已檢核確認完成 !",true);

  }
// ************************************************************************
 public void procMethodUCON() throws Exception
  {
  wp.selectCnt =1;
  commFileFlag("comm_file_flag");
  commProcFlag("comm_proc_flag");
  commAprFlag("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

  if (!wp.itemStr("file_flag").equals("Y"))
     {
      alertErr("上傳失敗, 不可確認/解確認");
      return;
     }

  if ((wp.itemStr("apr_flag").equals("Y"))&&
      (!wp.itemStr("proc_flag").equals("C")))
     {
      alertErr("已覆核, 不可確認/解確認");
      return;
     }
  if (wp.itemStr("proc_flag").equals("Y"))
     {
      alertErr("已完成覆核及處理, 不可再處理");
      return;
     }
  if (!wp.itemStr("proc_flag").equals("C"))
     {
      alertErr("未完成檢核確認, 不可再處理");
      return;
     }

  mktm02.Mktm1070Func func =new mktm02.Mktm1070Func(wp);
  func.dbupdateMktUploadfileCtl("B","N");
  wp.colSet("proc_flag" , "N");
  wp.colSet("comm_proc_flag" , "待確認");
  commProcFlag("comm_proc_flag");
  wp.colSet("apr_flag" , "N");
  wp.colSet("apr_date" , "");
  wp.colSet("apr_user" , "");
  commAprFlag("comm_apr_flag");

  errmsg("資料已解除確認完成 !",true);

  }
// ************************************************************************
 public void procMethodNDEL() throws Exception
  {
  wp.selectCnt =1;
  commFileFlag("comm_file_flag");
  commProcFlag("comm_proc_flag");
  commAprFlag("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

  if (!wp.itemStr("apr_flag").equals("Y"))
     {
      alertErr("未完成覆核處理, 不可刪除");
      return;
     }

  String sql1 = "";

  sql1 = "select  "
       + " count(*) as tran_cnt "
       + " from mkt_thsr_upidno_mon a,mkt_thsr_upidno b  "
       + " where b.trans_seqno  = '" + wp.itemStr("trans_seqno") + "' "
       + " and   a.upidno_seqno = b.upidno_seqno "
       ;

  sqlSelect(sql1);
  if (sqlNum("tran_cnt")>0)
     {
      alertErr("該批上傳序號["+ wp.itemStr("trans_seqno")+"]資料已有分潤不可刪除");
      return;
     }
  mktm02.Mktm1070Func func =new mktm02.Mktm1070Func(wp);
  func.dbDeleteMktUploadfileCtl(wp.itemStr("trans_seqno"));
  func.dbDeleteMktThsrUpidno(wp.itemStr("trans_seqno"));

  errmsg("該批上傳序號資料已刪除完成 !",true);
  clearFunc();

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
  if (wp.autUpdate())
     wp.colSet("img_display","");
  else
     wp.colSet("img_display","none");
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
