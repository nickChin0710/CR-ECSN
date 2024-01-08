/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/07/09  V1.00.03   Allen Ho      Initial                              *
 * 112/03/22  V1.00.04   jiangyingdong      sync code from mega             *
 * 112/03/23  V1.00.04   Zuwei Su      Bug修訂: 點選’團代’資料, 未帶出對應的’卡種’             *
 *                                                                          *
 ***************************************************************************/
package mktm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6243 extends BaseEdit
{
  private final String PROGNAME = "同核卡日卡片優先順序維護處理程式110/07/09 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6243Func func = null;
  String kk1;
  String orgTabName = "mkt_bn_data";
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
    else if (eqIgno(wp.buttonCode, "procMethod_rset"))
    {/* 重設順序 */
      strAction = "U";
      procMethodRset();
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
    else if (eqIgno(wp.buttonCode, "UPLOAD2"))
    {/* 匯入檔案 */
      procUploadFile(2);
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
    else if (eqIgno(wp.buttonCode, "AJAX"))
    {/* nothing to do */
      strAction = "";
      wfAjaxFunc4(wp);
    }

    dddwSelect();
    initButton();
  }
  // ************************************************************************
  @Override
  public void queryFunc() throws Exception
  {
    wp.whereStr = "WHERE 1=1 "
            + " and table_name  =  'MKT_FSTP_PARM_CARD' "
            + " and data_key  =  'FSTP_CARD_PRIORITY' "
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
            + "a.data_type,"
            + "a.data_code,"
            + "a.data_code2,"
            + "a.crt_user,"
            + "a.crt_date";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereOrder = " "
            + " order by data_type,data_code desc,data_code2"
    ;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind())
    {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGroupCode("comm_data_code");
    commCardType("comm_data_code2");
    commCrtUser("comm_crt_user");


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
      if (wp.itemStr("kk_data_type").length()==0)
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
            + "a.data_type as data_type,"
            + "a.data_code,"
            + "a.data_code2,"
            + "a.crt_user,"
            + "a.crt_date";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereStr = "where 1=1 ";
    if (qFrom==0)
    {
      wp.whereStr = wp.whereStr
              + sqlCol(wp.itemStr("kk_data_type"), "a.data_type")
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
      alertErr("查無資料, key= "+"["+ kk1+"]");
      return;
    }
    commCrtUser("comm_crt_user");
    checkButtonOff();
  }
  // ************************************************************************
  public void saveFunc() throws Exception
  {
    mktm02.Mktm6243Func func =new mktm02.Mktm6243Func(wp);

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
  }
  // ************************************************************************
  @Override
  public void dddwSelect()
  {
    String lsSql ="";
    try {
      if ((wp.respHtml.equals("mktm6243_detl")))
      {
        wp.optionKey = "";
        wp.initOption ="--";
        if (wp.colStr("data_code").length()>0)
        {
          wp.optionKey = wp.colStr("data_code");
        }
        if (wp.colStr("data_code").length()>0)
        {
          wp.initOption ="--";
        }
        this.dddwList("dddw_group_code"
                ,"ptr_group_code"
                ,"trim(group_code)"
                ,"trim(group_name)"
                ," where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption ="--";
        if (wp.colStr("data_code2").length()>0)
        {
          wp.optionKey = wp.colStr("data_code2");
        }
        if (wp.colStr("data_code2").length()>0)
        {
          wp.initOption ="--";
        }
        lsSql = "";
        String dataCode ="";
        if (wp.colStr("data_code2").length()>0)
        {
          wp.optionKey = wp.colStr("data_code2");
        }
        if (wp.colStr("data_code2").length()>0)
        {
          wp.initOption ="--";
        }

        lsSql =  procDynamicDddwCardType(wp.colStr("data_code"));
        dddwList("dddw_card_type", lsSql);
        dataCode = "";

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
  public void commGroupCode(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " group_name as column_group_name "
              + " from ptr_group_code "
              + " where 1 = 1 "
              + " and   group_code = '"+wp.colStr(ii,"data_code")+"'"
      ;
      if (wp.colStr(ii,"data_code").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commCardType(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " name as column_name "
              + " from ptr_card_type "
              + " where 1 = 1 "
              + " and   card_type = '"+wp.colStr(ii,"data_code2")+"'"
      ;
      if (wp.colStr(ii,"data_code2").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void wfAjaxFunc4(TarokoCommon wr) throws Exception
  {
    String ajaxjDataCode2 = "";
    super.wp = wr;


    if (selectAjaxFunc40(
            wp.itemStr("ax_win_data_code"))!=0)
    {
      wp.addJSON("ajaxj_data_code2", "");
      wp.addJSON("ajaxj_name", "");
      return;
    }

    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_data_code2", sqlStr(ii, "data_code2"));
    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_name", sqlStr(ii, "name"));
  }
  // ************************************************************************
  int selectAjaxFunc40(String s1) throws Exception
  {
    wp.sqlCmd = " select "
            + " '' as data_code2,  "
            + " '' as name  "
            + " from  ptr_businday "
            + " union "
            + " select "
            + " b.card_type  as data_code2,"
            + " b.name "
            + " from  ptr_group_card a,ptr_card_type b "
            + " where a.card_type  = b.card_type "
            + " and   a.group_code = '"+ s1 +"' "
            + " order by 1 "
    ;

    this.sqlSelect();

    if (sqlRowNum<=0)
    {
      alertErr("團體代號:["+s1+"]查無資料");
      return 1;
    }

    return 0;
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
    if (isEmpty(wp.itemStr("zz_file_name")))
    {
      alertErr("上傳檔名: 不可空白");
      return;
    }

    if (loadType==2) fileDataImp2();
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
  void fileDataImp2() throws Exception
  {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile,"MS950");

    if (fi == -1) return;

    String sysUploadType  = wp.itemStr("sys_upload_type");
    String sysUploadAlias = wp.itemStr("sys_upload_alias");

    mktm02.Mktm6243Func func =new mktm02.Mktm6243Func(wp);

    if (sysUploadAlias.equals("aaa2"))
      func.dbDeleteD2Aaa2("MKT_BN_DATA");

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    tranSeqStr = comr.getSeqno("MKT_MODSEQ");

    String ss="";
    int llOk=0, llCnt=0,llErr=0,llChkErr=0;
    int lineCnt =0;
    while (true)
    {
      ss = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) break;
      lineCnt++;

      llCnt++;

      for (int inti=0;inti<10;inti++) logMsg[inti]="";
      logMsg[10]=String.format("%02d",lineCnt);

      if (sysUploadAlias.equals("aaa2"))
        if (checkUploadfileAaa2(ss)!=0) continue;
      llOk++;

      if (notifyCnt ==0)
      {
        if (sysUploadAlias.equals("aaa2"))
        {
          if (func.dbInsertI2Aaa2("MKT_BN_DATA",uploadFileCol,uploadFileDat) != 1) errorCnt++;
        }
      }
    }

    if (llErr!=0) notifyCnt =1;
    if (notifyCnt ==1)
    {
      if (sysUploadAlias.equals("aaa2"))
        func.dbDeleteD2Aaa2("MKT_BN_DATA");
      func.dbInsertEcsNotifyLog(tranSeqStr,(llErr+llChkErr));
    }

    sqlCommit(1);  // 1:commit else rollback

    if (notifyCnt ==0)
      alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 轉入");
    else
      alertMsg("匯入筆數 : " + llCnt + ", 成功(" + llOk + "),重複("+ llErr + "), 失敗(" + errorCnt + ") 不轉入");

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);


    return;
  }
  // ************************************************************************
  int checkUploadfileAaa2(String ss) throws Exception
  {
    mktm02.Mktm6243Func func =new mktm02.Mktm6243Func(wp);

    for (int inti=0;inti<50;inti++)
    {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // ===========  [M]edia layout =============
    uploadFileCol[0]  = "data_type";
    uploadFileCol[1]  = "data_code";
    uploadFileCol[2]  = "data_code2";

    // ========  [I]nsert table column  ========
    uploadFileCol[3]  = "table_name";
    uploadFileCol[4]  = "data_key";
    uploadFileCol[5]  = "crt_date";
    uploadFileCol[6]  = "crt_user";

    // ==== insert table content default =====
    uploadFileDat[3]  = "MKT_FSTP_PARM_CARD";
    uploadFileDat[4]  = "FSTP_CARD_PRIORITY";
    uploadFileDat[5]  = wp.sysDate;
    uploadFileDat[6]  = wp.loginUser;

    int okFlag=0;
    int err_flag=0;
    int[] begPos = {1};

    for (int inti=0;inti<3;inti++)
    {
      uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
      if (uploadFileDat[inti].length()!=0) okFlag=1;
    }
    if (okFlag==0) return(1);
    if (uploadFileDat[0].length()==0)
    {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";         // 原因說明
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "1";                    // 欄位位置
      logMsg[3]               = uploadFileDat[9];       // 欄位內容
      logMsg[4]               = "優先順序";             // 錯誤說明
      logMsg[5]               = "優先順序空值";     // 欄位說明

      if (errorCnt <100)
        func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
    }

    if (uploadFileDat[1].length()==0)
    {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "2";
      logMsg[3]               = uploadFileDat[1];
      logMsg[4]               = "團代";
      logMsg[5]               = "團代空值";

      if (errorCnt <100)
        func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
    }
    else if (selectPtrGroupCode()!=0)
    {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";
      logMsg[1]               = "2";                    // 錯誤類別
      logMsg[2]               = "2";
      logMsg[3]               = uploadFileDat[1];
      logMsg[4]               = "團代不存在";
      logMsg[5]               = "團代不存在";

      if (errorCnt <100)
        func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
    }
    if (uploadFileDat[2].length()!=0)
      if (selectPtrCardType()!=0)
      {
        errorCnt++;
        logMsg[0]               = "資料內容錯誤";
        logMsg[1]               = "2";                    // 錯誤類別
        logMsg[2]               = "3";
        logMsg[3]               = uploadFileDat[2];
        logMsg[4]               = "卡種";
        logMsg[5]               = "卡種不存在";

        if (errorCnt <100)
          func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
        return(0);
      }

    if (selectMktBnData1()!=0)
    {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";
      logMsg[1]               = "3";
      logMsg[2]               = "1";
      logMsg[3]               = uploadFileDat[0];
      logMsg[4]               = "優先順序";
      logMsg[5]               = "優先順序重複";
      if (errorCnt <100)
        func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
    }
    if (selectMktBnData2()!=0)
    {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";
      logMsg[1]               = "3";
      logMsg[2]               = "1-2";
      logMsg[3]               = "["+uploadFileDat[0]+"]["+uploadFileDat[1]+"]";
      logMsg[4]               = "團代+卡種";
      logMsg[5]               = "團代+卡種重複";
      if (errorCnt <100)
        func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
    }
    if ((selectMktBnData3()!=0)&&
            (uploadFileDat[0].length()==0))
    {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";
      logMsg[1]               = "2";
      logMsg[2]               = "1-2";
      logMsg[3]               = "["+uploadFileDat[0]+"]["+uploadFileDat[1]+"]";
      logMsg[4]               = "團代+卡種";
      logMsg[5]               = "同團代,空白卡種優先順序必須較後";
      if (errorCnt <100)
        func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
    }
    if ((selectMktBnData4()!=0)&&
            (uploadFileDat[0].length()!=0))
    {
      errorCnt++;
      logMsg[0]               = "資料內容錯誤";
      logMsg[1]               = "2";
      logMsg[2]               = "1-2";
      logMsg[3]               = "["+uploadFileDat[0]+"]["+uploadFileDat[1]+"]";
      logMsg[3]               = "團代+卡種";
      logMsg[4]               = "團代+卡種";
      logMsg[5]               = "同團代,非空白卡種優先順序必須較前";
      if (errorCnt <100)
        func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
      return(0);
    }


    return 0;
  }
  // ************************************************************************
// ************************************************************************
  public void procMethodRset() throws Exception
  {
    wp.listCount[0] = wp.itemBuff("data_type").length;

    mktm02.Mktm6243Func func =new mktm02.Mktm6243Func(wp);

    String sql1 = "";
    sql1 = "select  "
            + " data_type, "
            + " data_code, "
            + " data_code2 "
            + " from  mkt_bn_data "
            + " where table_name = 'MKT_FSTP_PARM_CARD'  "
            + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
    ;

    sqlSelect(sql1);
    if (sqlRowNum <= 0)
    {
      alertErr("read mkt_bn_dat error !["+ sql1 +"]");
      return;
    }
    int recCnt = sqlRowNum;
    for (int inti=0;inti<recCnt;inti++)
      func.updateMktBnDataReset(sqlStr(inti,"data_type"),
              sqlStr(inti,"data_code"),
              sqlStr(inti,"data_code2"),
              String.format("%04d",(inti+1)*10));
    alertMsg("優先順序重設完成, 請重新查詢 !");
  }
  // ************************************************************************
  public void checkButtonOff() throws Exception
  {
    return;
  }
  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  int selectMktBnData1() throws Exception
  {
    wp.sqlCmd = " select "
            + " data_code "
            + " from  mkt_bn_data "
            + " where table_name = 'MKT_FSTP_PARM_CARD'  "
            + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
            + " and   data_type   = '"+uploadFileDat[0]+"' "
    ;

    this.sqlSelect();

    if (sqlRowNum<=0) return(0);

    return(1);
  }
  // ************************************************************************
  String procDynamicDddwCardType(String s1)  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " b.card_type as db_code, "
            + " b.card_type||' '||b.name as db_desc "
            + " from  ptr_group_card a,ptr_card_type b "
            + " where a.card_type  = b.card_type "
    ;

    if (s1.length()>0)
      lsSql =  lsSql
              + " and   a.group_code = '"+ s1 +"' "
              ;
    lsSql =  lsSql
            + " order by b.card_type "
    ;

    return lsSql;
  }
  // ************************************************************************
  int selectMktBnData2() throws Exception
  {
    wp.sqlCmd = " select "
            + " data_code "
            + " from  mkt_bn_data "
            + " where table_name = 'MKT_FSTP_PARM_CARD'  "
            + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
            + " and   data_type   = '"+uploadFileDat[0]+"' "
            + " and   data_code   = '"+uploadFileDat[1]+"' "
            + " and   data_code2  = '"+uploadFileCol[2]+"' "
    ;

    this.sqlSelect();

    if (sqlRowNum<=0) return(0);

    return(1);
  }
  // ************************************************************************
  int selectMktBnData3() throws Exception
  {
    wp.sqlCmd = " select "
            + " data_code "
            + " from  mkt_bn_data "
            + " where table_name = 'MKT_FSTP_PARM_CARD'  "
            + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
            + " and   data_type   > '"+uploadFileDat[0]+"' "
            + " and   data_code   = '"+uploadFileDat[1]+"' "
            + " and   data_code2 != ''   "
    ;

    this.sqlSelect();

    if (sqlRowNum<=0) return(0);

    return(1);
  }
  // ************************************************************************
  int selectMktBnData4() throws Exception
  {
    wp.sqlCmd = " select "
            + " data_code "
            + " from  mkt_bn_data "
            + " where table_name = 'MKT_FSTP_PARM_CARD'  "
            + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
            + " and   data_type   < '"+uploadFileDat[0]+"' "
            + " and   data_code   = '"+uploadFileDat[1]+"' "
            + " and   data_code2  = ''   "
    ;

    this.sqlSelect();

    if (sqlRowNum<=0) return(0);

    return(1);
  }
  // ************************************************************************
  int selectPtrGroupCode() throws Exception
  {
    wp.sqlCmd = " select "
            + " group_code "
            + " from  ptr_group_code "
            + " where group_code   = '"+uploadFileDat[1]+"' "
    ;

    this.sqlSelect();

    if (sqlRowNum>0) return(0);

    return(1);
  }
  // ************************************************************************
  int selectPtrCardType() throws Exception
  {
    wp.sqlCmd = " select "
            + " card_type "
            + " from  ptr_card_type "
            + " where card_type   = '"+uploadFileDat[2]+"' "
    ;

    this.sqlSelect();

    if (sqlRowNum>0) return(0);

    return(1);
  }
// ************************************************************************

}  // End of class
