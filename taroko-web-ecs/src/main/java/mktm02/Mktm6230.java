/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/01/24  V1.01.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                           *
***************************************************************************/
package mktm02;

import mktm02.Mktm6230Func;
import ofcapp.AppMsg;
import java.util.Arrays;

import ecsfunc.EcsCallbatch;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6230 extends BaseEdit
{
 private final String PROGNAME = "專案回饋金媒體檔案上傳作業處理程式111-11-30  V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6230Func func = null;
  String kk1;
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
  else if (eqIgno(wp.buttonCode, "procMethod_CALL"))
     {/* 啟動批次 */
      strAction = "U";
      procMethodCALL();
     }
  else if (eqIgno(wp.buttonCode, "procMethod_NDEL"))
     {/* 無效批號刪除(已覆核) */
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
              + sqlStrend(wp.itemStr2("ex_file_date_s"), wp.itemStr2("ex_file_date_e"), "a.file_date")
              + " and a.file_type  =  'MKT_LOAN' "
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
               + "substr(FILE_NAME,1,30) as file_name,"
               + "a.trans_seqno,"
               + "a.file_flag,"
               + "a.file_cnt,"
               + "a.error_cnt,"
               + "a.apr_flag,"
               + "a.proc_date,"
               + "a.crt_user,"
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
               + "a.file_cnt,"
               + "a.file_amt1,"
               + "a.error_cnt,"
               + "a.error_desc,"
               + "a.error_memo,"
               + "a.proc_flag,"
               + "a.proc_date,"
               + "a.trans_seqno,"
               + "a.crt_user,"
               + "a.crt_date,"
               + "a.apr_user,"
               + "a.apr_date,"
               + "a.apr_flag,"
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
                   +  sqlRowId(kk1, "a.rowid")
                   ;
     }

  pageSelect();
  if (sqlNotFind())
     {
      alertErr("查無資料, key= "+"["+ kk1 + "]");
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
  mktm02.Mktm6230Func func =new mktm02.Mktm6230Func(wp);

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
  buttonOff("btncall_disable");

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
      wp.colSet("btncall_disable","");
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
      buttonOff("btncall_disable");
     }



  if (wp.autUpdate())
     wp.colSet("img_display","src=\"images/uperLoad.gif\" onclick=\"return upload_click('','')\" style=\"cursor:hand\"");
  else
     wp.colSet("img_display","");
 }
// ************************************************************************
 @Override
 public void dddwSelect()
 {
  String lsSql ="";
  try {
       if ((wp.respHtml.equals("mktm6230")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_extern_id").length()>0)
             {
             wp.optionKey = wp.colStr("ex_extern_id");
             }
          this.dddwList("dddw_extern_id"
                 ,"mkt_extern_unit"
                 ,"trim(extern_id)"
                 ,"trim(extern_name)"
                 ," where 1 = 1 ");
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
  String[] txt = {"已覆核","待覆核","不同意匯入",">失敗"};
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
  if (wp.itemStr2("zz_file_name").indexOf(".xls")!=-1) 
     {
      alertErr("上傳格式: 不可為 excel 格式");
      return;
     }
  if (empty("zz_file_name"))
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
  mktm02.Mktm6230Func func =new mktm02.Mktm6230Func(wp);
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);

  String inputFile = wp.itemStr2("zz_file_name");
  int fi = tf.openInputText(inputFile,"MS950");

  if (fi == -1) return;

  tranSeqStr = comr.getSeqno("MKT_MODSEQ");
  for (int inti=0;inti<10;inti++) hideStr[inti]="";
  if (selectMktUploadfileCtl()>0)
     {
      alertErr("同日不可上傳同檔名資料: 重複上傳");
      return;
     }
  selectMktExternUnit();
  hideStr[3] ="MktH020";
  hideStr[1] = "單位代碼:"+wp.itemStr2("ex_extern_id")+"-"+sqlStr("extern_name");
  
  selectMktLoan();
  func.dbDeleteMktUploadfileDataP(upGroupType);

  String ss="";
  int llOk=0, llCnt=0,llErr=0;
  int lineCnt =0;
  while (true)
   {
    ss = tf.readTextFile(fi);
    if (tf.endFile[fi].equals("Y")) break;
    lineCnt++;

    if (ss.length() < 2) continue;
    llCnt++; 

    for (int inti=0;inti<10;inti++) logMsg[inti]="";
    logMsg[10]=String.format("%02d",lineCnt);

   if (checkUploadfile(ss,llCnt)!=0) continue;

   if (errorCnt==0)
      {
       rc = func.dbInsertMktUploadfileData(tranSeqStr,
                                              uploadFileCol,
                                              uploadFileDat,
                                              colNum, 
                                              upGroupType,
                                              lineCnt);
       if (rc!=1) llErr++;
      }
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

  hideStr[2] = "無異常資料, 可執行確認";
  if (datachkCnt[0]+datachkCnt[1]+datachkCnt[2]+datachkCnt[4]+datachkCnt[5]!=0)
     {
      hideStr[2] = "資料有錯不可轉入,";
      if (datachkCnt[0]!=0) hideStr[2]=hideStr[2]+hideStr[5];
      if (datachkCnt[1]!=0) hideStr[2]=hideStr[2]+"金額錯誤:"+datachkCnt[1]+"筆 ";
      if (datachkCnt[2]!=0) hideStr[2]=hideStr[2]+"帳戶類別:"+datachkCnt[2]+"筆 ";
      if (datachkCnt[4]!=0) hideStr[2]=hideStr[2]+"id小寫:"+datachkCnt[4]+"筆 ";
      if (datachkCnt[5]!=0) hideStr[2]=hideStr[2]+"金額格式錯誤:"+datachkCnt[5]+"筆 ";

      datachkCnt[0]=1;
     }

  llOk = llCnt-llErr-errorCnt;
  func.dbInsertMktUploadfileCtl(tranSeqStr,llOk,(errorCnt+llErr),hideStr,datachkCnt);

  sqlCommit(1);  // 1:commit else rollback

  ss ="資料匯入筆數: " + (llCnt) + ", 成功 = " + llOk + ", 錯誤 = " + (llErr+errorCnt);
  if (errorCnt+llErr>0)
     alertErr(ss+" 請查詢 ecsq0040 確認錯誤原因! ");
  else alertMsg(ss);

  tf.closeInputText(fi);
  tf.deleteFile(inputFile);

  return;
 }
// ************************************************************************

int  checkUploadfile(String ss,int ll_cnt) throws Exception
 {
  mktm02.Mktm6230Func func =new mktm02.Mktm6230Func(wp);

  for (int inti=0;inti<50;inti++)
    {
     uploadFileCol[inti] = "";
     uploadFileDat[inti] = "";
    }
  // ===========  [M]edia layout =============
  uploadFileCol[0]  = "branch_no";
  uploadFileCol[1]  = "in_type";
  uploadFileCol[2]  = "id_no";
  uploadFileCol[3]  = "id_no_code";
  uploadFileCol[4]  = "chi_name";
  uploadFileCol[5]  = "period";
  uploadFileCol[6]  = "interest";
  uploadFileCol[7]  = "rtn_rate";
  uploadFileCol[8]  = "rtn_amt";
  uploadFileCol[9]  = "pay_type";
  uploadFileCol[10]  = "acct_no";
  uploadFileCol[11]  = "acct_type";

  // ========  [I]nsert table column  ========
  uploadFileCol[12]  = "file_name";
  uploadFileCol[13]  = "batch_no";
  uploadFileCol[14]  = "rec";
  uploadFileCol[15]  = "fund_code";
  uploadFileCol[16]  = "crt_date";
  uploadFileCol[17]  = "crt_user";
  uploadFileCol[18]  = "process_flag";
  uploadFileCol[19]  = "trans_seqno";

  colNum = 20;

  // ==== insert table content default =====
  uploadFileDat[12]  = wp.itemStr2("zz_file_name");
  uploadFileDat[16]  = wp.sysDate;
  uploadFileDat[17]  = wp.loginUser;
  uploadFileDat[18]  = "N";
  uploadFileDat[19]  = tranSeqStr;

  int okFlag=0;
  int errFlag=0;
  int[] begPos = {1};

  int newpos = 0;
  int[] dataLen= {3,2,10,1,20,2,10,10,10,1,14,2};

  for (int inti=0;inti<dataLen.length;inti++)
      {
       uploadFileDat[inti] = commString.midBig5(ss,newpos,dataLen[inti]).trim();
       newpos = newpos + dataLen[inti];
       if (uploadFileDat[inti].length()!=0) okFlag=1;
      }
  if (okFlag==0) return(1);
  //******************************************************************
  if (!comm.isNumber(uploadFileDat[5])) uploadFileDat[5] = "0";
  if (!comm.isNumber(uploadFileDat[6])) uploadFileDat[6] = "0";
  if (!comm.isNumber(uploadFileDat[7])) uploadFileDat[7] = "0";
  if (!comm.isNumber(uploadFileDat[8].replace("-",""))) uploadFileDat[8] = "0";
  int intm = uploadFileDat[8].indexOf("-");
  if ((intm!=-1)&&(!uploadFileDat[8].substring(0,1).equals("-"))) 
     {
      errorCnt++;
      datachkCnt[5]++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "9";                   // 欄位位置
      logMsg[3]               = uploadFileDat[8];      // 欄位內容
      logMsg[4]               = "回饋金額";             // 錯誤說明
      logMsg[5]               = "回饋金額減號位置未誤";     // 欄位說明
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      notifyCnt=1;
      return(1);
     }


  wp.log("rtn_amt=["+ uploadFileDat[8] +"]");
  uploadFileDat[13] = "";
  uploadFileDat[14] = String.format("%d",(recCnt+1));
  recCnt++;

  if ( datachkCnt[0]!=0) return(0);
  if (!uploadFileDat[0].equals(wp.itemStr2("ex_extern_id")))
     {
      hideStr[5] = uploadFileDat[0];
      datachkCnt[0]=8;
      alertErr("媒體單位代碼["+hideStr[5]+"]與選擇之["+wp.itemStr2("ex_extern_id")+"]不符");
      return(0);
     }
  if (recCnt==1)
     {
      if (selectMktExternUnit1()!=0)
         {
          hideStr[5] = "來源代碼["+uploadFileDat[1]+"]無對應之基金代碼";
          datachkCnt[0]=9;
          return(0);
         }
      else
         {
          if (selectMktLoanParm(sqlStr("fund_code"))!=0)
             {
              if (sqlStr("fund_code").length()==4)
                 {
                  if (selectMktLoanParm(sqlStr("fund_code")+uploadFileDat[1]+"0001")!=0)
                     {
                      hideStr[5] = "來源代碼1["+uploadFileDat[1]+"]無對應之基金代碼";
                      datachkCnt[0]=9;
                      return(0);
                     }
                  else
                     {
                      hideStr[1] = "基金代碼:"+sqlStr("fund_code")+"-"+sqlStr("fund_name");
                     }
                 }
              else
                 {
                  hideStr[5] = "來源代碼2["+uploadFileDat[1]+"]無對應之基金代碼";
                  datachkCnt[0]=9;
                  return(0);
                 }
             }
          else
             {
              hideStr[1] = "基金代碼:"+sqlStr("fund_code")+"-"+sqlStr("fund_name");
             }
         }
     }
 
  datachkCnt[3] = datachkCnt[3] + Integer.valueOf(uploadFileDat[8]);

  if (selectPtrAcctType()!=0)
     {
      errorCnt++;
      datachkCnt[2]++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "12";                   // 欄位位置
      logMsg[3]               = uploadFileDat[11];      // 欄位內容
      logMsg[4]               = "帳戶類別";             // 錯誤說明
      logMsg[5]               = "帳戶類別必須有值";     // 欄位說明
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      notifyCnt=1;
      return(1);
     }

  if (!uploadFileDat[2].equals(uploadFileDat[2].toUpperCase()))
     {
      errorCnt++;
      datachkCnt[4]++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "3";                   // 欄位位置
      logMsg[3]               = uploadFileDat[2];      // 欄位內容
      logMsg[4]               = "身分證號";            // 錯誤說明
      logMsg[5]               = "身分證號小寫";        // 欄位說明
      func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      notifyCnt=1;
      return(1);
     }

  uploadFileDat[15] = sqlStr("fund_code"); 
  uploadFileDat[13] = hideStr[4];
  uploadFileDat[14] = String.format("%d",recCnt);;
//  if (Integer.valueOf(uploadFileDat[8])<=0)
//     datachk_cnt[1]++; // mantis 8855

  return 0;
 }
// ************************************************************************
// ************************************************************************
 int  selectMktExternUnit1() throws Exception 
 {
  wp.sqlCmd = " select "
            + " extern_id,"
            + " extern_name,"
            + " d.data_code2 as fund_code "
            + " from mkt_extern_unit c,mkt_bn_data d"
            + " where d.table_name = 'MKT_EXTERN_UNIT' "
            + " and   c.extern_id  = d.data_key  "
            + " and   c.extern_id  = '" + wp.itemStr2("ex_extern_id") + "' "
            + " and   c.disable_flag != 'Y'  "
            + " and   d.data_code   = '" + uploadFileDat[1] +"' "
            + " and   d.data_type    = '1' "
            ;

  this.sqlSelect();
  if (sqlRowNum<=0)
     {
      alertErr("來源代碼["+uploadFileDat[1]+"]查無基金資料");
      return(1);
     }

  return(0);
 }
// ************************************************************************
 int  selectMktLoanParm(String fundCode) throws Exception
 {
  wp.sqlCmd = " select "
            + " a.fund_code , "
            + " a.fund_name "
            + " from mkt_loan_parm a "
            + " where a.fund_code like '" + fundCode +"%' "
            + " and   a.stop_flag != 'Y' "
            ;

  this.sqlSelect();
  if (sqlRowNum<=0) return(1);

  return(0);
 }
// ************************************************************************
 int  selectMktExternUnit() throws Exception 
 {
  wp.dateTime();
  wp.sqlCmd = " select "
              + " extern_name "
              + " from mkt_extern_unit "
              + " where extern_id = '" + wp.itemStr2("ex_extern_id") +"' " 
              ;
  this.sqlSelect();

  return(0);
 }
// ************************************************************************
 int  selectMktUploadfileCtl() throws Exception 
 {
  wp.sqlCmd = " select "
              + " 1 as rowdata "
              + " from mkt_uploadfile_ctl "
              + " where file_type   = 'MKT_LOAN' "
              + " and   apr_flag    = 'Y' "
              + " and   file_date   = '"+wp.sysDate+"' "
              + " and   file_name   = '"+wp.itemStr2("zz_file_name")+"' ";
              ;
  this.sqlSelect();

  if (sqlRowNum>0) return(1);

  return(0);
 }
// ************************************************************************
 int  selectPtrAcctType() throws Exception
 {
  wp.sqlCmd = " select "
              + " acct_type  "
              + " from ptr_acct_type "
              + " where acct_type   = '"+ uploadFileDat[11] +"' "
              ;
  this.sqlSelect();
  
  if (sqlRowNum==0) return(1);
  
  return(0);
 }
// ************************************************************************
 int  selectMktLoanParm() throws Exception
 {
  wp.sqlCmd = " select "
              + " fund_name "
              + " from mkt_loan_parm "
              + " where fund_code   = '"+ wp.itemStr2("ex_fund_code") +"' "
              ;
  this.sqlSelect();

  if (sqlRowNum>0) return(1);

  return(0);
 }
// ************************************************************************
 int  selectMktLoan() throws Exception
 {
  wp.sqlCmd = " select "
              + " max(batch_no) as batch_no "
              + " from mkt_loan "
              + " where batch_no like '"+ wp.sysDate +"%' "
              ;
  this.sqlSelect();

  if (sqlStr("batch_no").length()==0)
      hideStr[4] =wp.sysDate + "01";
   else
     {
      hideStr[4] =wp.sysDate + String.format("%02d",Integer.valueOf(sqlStr("batch_no").substring(8))+1);
     }

  return(0);
 }
// ************************************************************************
 public void procMethodDELE() throws Exception
  {
  wp.selectCnt =1;
  commFileFlag("comm_file_flag");
  commProcFlag("comm_proc_flag");
  commAprFlag("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");

 if (wp.itemStr2("proc_flag").equals("Y"))
     {
      alertErr("已完成覆核及處理, 不可刪除");
      return;
     }
 if (wp.itemStr2("proc_flag").equals("C"))
     {
      alertErr("已完成檢核確認, 不可刪除");
      return;
     }

  mktm02.Mktm6230Func func =new mktm02.Mktm6230Func(wp);
  func.dbDeleteMktUploadfileCtl();

  alertMsg("資料已刪除完成 !");
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

  if (!wp.itemStr2("file_flag").equals("Y"))
     {
      alertErr("上傳失敗, 不可確認/解確認");
      return;
     }

  if (wp.itemStr2("proc_flag").equals("C"))
     {
      alertErr("已完成檢核確認, 不可再處理");
      return;
     }
  if (wp.itemStr2("proc_flag").equals("Y"))
     {
      alertErr("已完成覆核及處理, 不可再確認");
      return;
     }

  mktm02.Mktm6230Func func =new mktm02.Mktm6230Func(wp);
  func.dbupdateMktUploadfileCtl("Y","C");
  wp.colSet("proc_flag" , "C");
  wp.colSet("comm_proc_flag" , "已確認");
  commProcFlag("comm_proc_flag");
  wp.colSet("apr_flag" , "N");
  wp.colSet("apr_date" , "");
  wp.colSet("apr_user" , "");
  commAprFlag("comm_apr_flag");

  alertMsg("資料已檢核確認完成 !");

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

  if (!wp.itemStr2("file_flag").equals("Y"))
     {
      alertErr("上傳失敗, 不可確認/解確認");
      return;
     }

  if (wp.itemStr2("proc_flag").equals("Y"))
     {
      alertErr("已完成覆核及處理, 不可再處理");
      return;
     }
  if (!wp.itemStr2("proc_flag").equals("C"))
     {
      alertErr("未完成檢核確認, 不可再處理");
      return;
     }
  if (wp.itemStr2("apr_flag").equals("Y"))
     {
      alertErr("已覆核, 不可解確認");
      return;
     }


  mktm02.Mktm6230Func func =new mktm02.Mktm6230Func(wp);
  func.dbupdateMktUploadfileCtl("B","N");
  wp.colSet("proc_flag" , "N");
  wp.colSet("comm_proc_flag" , "待確認");
  commProcFlag("comm_proc_flag");
  wp.colSet("apr_flag" , "N");
  wp.colSet("apr_date" , "");
  wp.colSet("apr_user" , "");
  commAprFlag("comm_apr_flag");

  alertMsg("資料已解除確認完成 !");

  }
// ************************************************************************
 public void procMethodCALL() throws Exception
  {
  wp.selectCnt =1;
  commFileFlag("comm_file_flag");
  commProcFlag("comm_proc_flag");
  commAprFlag("comm_apr_flag");
  commCrtUser("comm_crt_user");
  commAprUser("comm_apr_user");
  EcsCallbatch batch = new EcsCallbatch(wp) ;

  rc=batch.callBatch("MktH020" + " " + wp.itemStr2("trans_seqno")+ " " + wp.itemStr2("apr_user"));
  if (rc!=1)
     {
      alertErr("callbatch[MktH020] 交易序號["+wp.itemStr2("trans_seqno")+"] 失敗");
     }
  else
     {
      alertMsg("批次已啟動成功! ");
     }

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

  if (!wp.itemStr2("apr_flag").equals("Y"))
     {
      alertErr("未完成覆核處理, 不可刪除");
      return;
     }

  String sql1 = "";

  sql1 = "select  "
       + " count(*) as tran_cnt "
       + " from mkt_loan   "
       + " where trans_seqno  = '" + wp.itemStr2("trans_seqno") + "' "
       + " and   tran_seqno != '' "
       ;

  sqlSelect(sql1);
  if (sqlNum("tran_cnt")>0)
     {
      alertErr("該批上傳序號["+ wp.itemStr2("trans_seqno")+"]資料已有回饋不可刪除");
      return;
     }
  mktm02.Mktm6230Func func =new mktm02.Mktm6230Func(wp);
  func.dbDeleteMktUploadfileCtl(wp.itemStr2("trans_seqno"));
  func.dbDeleteMktLoan(wp.itemStr2("trans_seqno"));

  alertMsg("該批上傳序號資料已刪除完成 !");

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
     wp.colSet("img_display","src=\"images/uperLoad.gif\" onclick=\"return upload_click('','')\" style=\"cursor:hand\"");
  else
     wp.colSet("img_display","");
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
