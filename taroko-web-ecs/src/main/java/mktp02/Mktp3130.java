/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/05/14  V1.00.01   Allen Ho      Initial                              *
* 111/12/02  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktp02;

import busi.ecs.CommRoutine;
import ecsfunc.EcsCallbatch;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp3130 extends BaseProc
{
 private final String PROGNAME = "媒體檔案上傳作業處理程式111/12/02  V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  Mktp3130Func func = null;
  String kk1;
  String fstAprFlag = "";
  String orgTabName = "mkt_uploadfile_ctl";
  String controlTabName = "";
  int qFrom=0;
  int uploadFlag=0;
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
      uploadFlag=0;
      strAction = "A";
      dataProcess();
     }
  else if (eqIgno(wp.buttonCode, "Z"))
     {/* 不同意轉入 */
      uploadFlag=1;
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
              + sqlCol(wp.itemStr("ex_file_type"), "a.file_type")
              + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user")
              + sqlStrend(wp.itemStr("ex_file_date_s"), wp.itemStr("ex_file_date_e"), "a.file_date")
              + " and group_type  =  '0' "
              + " and apr_flag  =  'N' "
              + " and file_flag  =  'Y' "
              + " and proc_flag='C'     "
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
               + "a.type_name,"
               + "a.trans_seqno,"
               + "a.file_flag,"
               + "a.file_cnt,"
               + "a.file_amt1,"
               + "a.crt_user,"
               + "a.error_desc,"
               + "a.callbatch_pgm,"
               + "a.callbatch_pgm2,"
               + "a.callbatch_parm,"
               + "a.file_type";

  wp.daoTable = controlTabName + " a "
              ;
  wp.whereOrder = " "
                + " order by a.file_type,a.crt_user,a.file_date"
                ;

  pageQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
  buttonOff("btnAdd_disable");
      return;
     }


  commFileFlag("comm_file_flag");

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
               + "a.amt_name1 as amt_name1,"
               + "a.amt_name2 as amt_name2,"
               + "a.file_type,"
               + "a.type_name,"
               + "a.file_date,"
               + "a.file_time,"
               + "a.file_name,"
               + "a.file_flag,"
               + "a.file_cnt,"
               + "a.file_amt1,"
               + "a.error_cnt,"
               + "a.file_amt2,"
               + "a.error_desc,"
               + "a.error_memo,"
               + "a.trans_seqno,"
               + "a.crt_user,"
               + "a.crt_date";

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
      return;
     }
  datareadWkdata();
  commFileFlag("comm_file_flag");
  commCrtUser("comm_crt_user");
  checkButtonOff();
 }
// ************************************************************************
 void datareadWkdata() throws Exception
 {
  if (wp.colStr("amt_name1").length()==0)
     wp.colSet("amt_name1" , "(本欄位未使用)");
  if (wp.colStr("amt_name2").length()==0)
     wp.colSet("amt_name2" , "(本欄位未使用)");
 }
// ************************************************************************
 public int dataReadR4(String transSeqno) throws Exception
 {
  setSelectLimit(0);
  String sqlCmd = " select "
                + " data_column01, "
                + " data_data01, "
                + " table_name "
                + " from mkt_uploadfile_data"
                + " where trans_seqno = '"
                + transSeqno + "'"
                ;

  sqlSelect(sqlCmd);

  if (sqlRowNum<=0) return(1);
  int recCnt =sqlRowNum;
  Mktp3130Func func =new Mktp3130Func(wp);

  for (int ii = 0; ii < recCnt; ii++)
    {
     rc = func.dbInsertA4(sqlStr(ii,"table_name"),
                           sqlStr(ii,"data_column01"),
                           sqlStr(ii,"data_data01"));
     if (rc!=1) return(rc);
    }
  return 1;
 }
// ************************************************************************
 public void uploadfileTableName(String transSeqno) throws Exception
 {
  String sql1 = "";
  sql1 = "select "
       + " delete_flag, "
       + " delete_cond, "
       + " table_name "
       + " from mkt_uploadfile_ctl "
       + " where trans_seqno = '" +transSeqno + "'"
       ;

  sqlSelect(sql1);

  if (sqlRowNum<=0) return;

  if (sqlStr("delete_flag").equals("Y"))
     {
      Mktp3130Func func =new Mktp3130Func(wp);
      func.dbInsertD4(sqlStr("table_name"),sqlStr("delete_cond"));
     }

  return;
 }
// ************************************************************************
 void listWkdataAft() throws Exception
 {
 }
// ************************************************************************
 void listWkdata() throws Exception
 {
 }
// ************************************************************************
 void listWkdataSpace() throws Exception
 {
 }
// ************************************************************************
 @Override
 public void dataProcess() throws Exception
 {
  int ilOk = 0;
  int ilErr = 0;
  int ilAuth = 0;
  String lsUser="";
  Mktp3130Func func =new Mktp3130Func(wp);
  CommRoutine comr = new CommRoutine();
  comr.setConn(wp);
  String[] rcvStr={"","","","","","","","","",""};

  String[] lsTransSeqno  = wp.itemBuff("trans_seqno");
  String[] lsFileType    = wp.itemBuff("file_type");
  String[] lsCallbatchPgm  = wp.itemBuff("callbatch_pgm");
  String[] lsCallbatchParm  = wp.itemBuff("callbatch_parm");
 if (uploadFlag==1)
    lsCallbatchPgm  = wp.itemBuff("callbatch_pgm2");
  String[] lsCrtUser  = wp.itemBuff("crt_user");
  String[] lsRowid     = wp.itemBuff("rowid");
  String[] opt =wp.itemBuff("opt");
  wp.listCount[0] = lsRowid.length;

  int rr = -1;
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

     func.varsSet("rowid", lsRowid[rr]);
     wp.itemSet("wprowid", lsRowid[rr]);
     rc =func.dbUpdateU4(lsTransSeqno[rr],uploadFlag);
     if (uploadFlag==0)
        {
         if (rc!=1) alertErr2(func.getMsg());
         else
           {
            uploadfileTableName(lsTransSeqno[rr]);
            rc = dataReadR4(lsTransSeqno[rr]);
           }
        }

     if (rc!=1) alertErr2(func.getMsg());
     if (rc == 1)
        {
         commFileFlag("comm_file_flag");

         wp.colSet(rr,"ok_flag","V");
         ilOk++;
         func.dbDelete(lsTransSeqno[rr]);
         this.sqlCommit(rc);
         if (lsCallbatchPgm[rr].length()>0)
            {
             EcsCallbatch batch = new EcsCallbatch(wp) ;

             rc=batch.callBatch(lsCallbatchPgm[rr] + " " + lsCallbatchParm[rr] + " "  + wp.loginUser);
             if (rc!=1)
                {
                 alertErr2(lsFileType[rr]+ " : callbatch["+ lsCallbatchPgm[rr] +"] 失敗");
                }
             else
                {
                 alertMsg("批次已啟動成功! ");
                }
            }
         else
            {
             if (lsCallbatchParm[rr].length()>0)
                func.dbUpdateMktUploadfileCtlProcFlag(lsCallbatchParm[rr]);
            }
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
       if ((wp.respHtml.equals("mktp3130")))
         {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("ex_file_type").length()>0)
             {
             wp.optionKey = wp.colStr("ex_file_type");
             }
          lsSql = "";
          lsSql =  procDynamicDddwFileType(wp.colStr("ex_file_type"));
          wp.optionKey = wp.colStr("ex_file_type");
          dddwList("dddw_file_type", lsSql);
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
          + " from sec_user a,mkt_uploadfile_ctl b "
          + " where a.usr_id = b.crt_user "
          + " and   b.group_type = '0'  "
          + " and   b.apr_flag   = 'N'  "
          + " and   b.file_flag  = 'Y'  "
          + " and   b.proc_flag  = 'C'  "
          + " group by b.crt_user "
          ;

   return lsSql;
 }
// ************************************************************************
 String procDynamicDddwFileType(String s1)  throws Exception
 {
   String lsSql = "";

   lsSql = " select "
          + " b.file_type as db_code, "
          + " max(b.file_type||' '||b.type_name) as db_desc "
          + " from  mkt_uploadfile_ctl b "
          + " where b.group_type = '0'  "
          + " and   b.apr_flag   = 'N'  "
          + " and   b.proc_flag  = 'C'  "
          + " group by b.file_type "
          ;

   return lsSql;
 }

// ************************************************************************

}  // End of class
