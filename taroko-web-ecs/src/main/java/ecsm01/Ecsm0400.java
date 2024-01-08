/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/07/08  V1.00.02   Allen Ho      Initial                              *
 * 111/11/28  V1.00.03  jiangyigndong  updated for project coding standard  *
 * 111/12/14  V1.00.04  Zuwei Su       add dddwselect dddw_ref_ip_code  *
 *                                                                          *
 ***************************************************************************/
package ecsm01;

import busi.ecs.CommRoutine;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0400 extends BaseEdit
{
  private final String PROGNAME = "PGP參數為覆處理程式111/12/14 V1.00.04";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0400Func func = null;
  String kk1;
  String orgTabName = "mkt_pgp_parm";
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

    dddwSelect();
    initButton();
  }
  // ************************************************************************
  @Override
  public void queryFunc() throws Exception
  {
    wp.whereStr = "WHERE 1=1 "
            + sqlCol(wp.itemStr("ex_air_type"), "a.air_type")
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
            + "a.air_type,"
            + "a.pgp_name,"
            + "a.public_key_name_i,"
            + "a.private_key_name_i,"
            + "a.public_key_name_o,"
            + "a.crt_date,"
            + "a.crt_user";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereOrder = " "
            + " order by a.air_type"
    ;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind())
    {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAirType("comm_air_type");


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
      if (wp.itemStr("kk_air_type").length()==0)
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
            + "a.air_type as air_type,"
            + "a.pgp_name,"
            + "a.public_key_name_i,"
            + "a.private_key_name_i,"
            + "a.public_key_name_o,"
            + "a.crt_date,"
            + "a.crt_user,"
            + "a.hide_ref_code,"
            + "a.passphase_i";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereStr = "where 1=1 ";
    if (qFrom==0)
    {
      wp.whereStr = wp.whereStr
              + sqlCol(wp.itemStr("kk_air_type"), "a.air_type")
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
    datareadWkdata();
    commAirType("comm_air_type");
    checkButtonOff();
  }
  // ************************************************************************
  void datareadWkdata() throws Exception
  {
    wp.sqlCmd = "select max(to_char(mod_time,'yyyy/mm/dd hh24:mi:ss'))  as mod_time "
            + "from mkt_pgp_keydtl "
            + "where ref_ip_code = '" + wp.colStr("ref_ip_code") + "' "
            + "and   key_type   = '1'   ";
    ;
    this.sqlSelect();
    if (sqlStr("mod_time").length()==0)
      wp.colSet("chk_imp_pubi" , "尚未匯入");
    else
      wp.colSet("chk_imp_pubi" , "[" + sqlStr("mod_time") + "] 匯入");

    wp.sqlCmd = "select max(to_char(mod_time,'yyyy/mm/dd hh24:mi:ss'))  as mod_time "
            + "from mkt_pgp_keydtl "
            + "where ref_ip_code = '" + wp.colStr("ref_ip_code") + "' "
            + "and   key_type   = '2'   ";
    ;
    this.sqlSelect();
    if (sqlStr("mod_time").length()==0)
      wp.colSet("chk_imp_prii" , "尚未匯入");
    else
      wp.colSet("chk_imp_prii" , "[" + sqlStr("mod_time") + "] 匯入");

    wp.sqlCmd = "select max(to_char(mod_time,'yyyy/mm/dd hh24:mi:ss'))  as mod_time "
            + "from mkt_pgp_keydtl "
            + "where ref_ip_code = '" + wp.colStr("ref_ip_code") + "' "
            + "and   key_type   = '3'   ";
    ;
    this.sqlSelect();
    if (sqlStr("mod_time").length()==0)
      wp.colSet("chk_imp_pubo" , "尚未匯入");
    else
      wp.colSet("chk_imp_pubo" , "[" + sqlStr("mod_time") + "] 匯入");

    wp.sqlCmd = "select max(to_char(mod_time,'yyyy/mm/dd hh24:mi:ss'))  as mod_time "
            + "from mkt_pgp_keydtl "
            + "where ref_ip_code = '" + wp.colStr("ref_ip_code") + "' "
            + "and   key_type   = '4'   ";
    ;
    this.sqlSelect();
    if (sqlStr("mod_time").length()==0)
      wp.colSet("chk_imp_prio" , "尚未匯入");
    else
      wp.colSet("chk_imp_prio" , "[" + sqlStr("mod_time") + "] 匯入");

    if (wp.colStr("passphase_i").length()!=0)
    {
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      wp.colSet("t_passphase_i"    , comm.hideUnzipData(wp.colStr("passphase_i")  ,wp.colStr("hide_ref_code")));
      wp.colSet("t_passphase_i_c"  , wp.colStr("t_passphase_i") );
    }

  }
  // ************************************************************************
  public void saveFunc() throws Exception
  {
    ecsm01.Ecsm0400Func func =new ecsm01.Ecsm0400Func(wp);

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
      if ((wp.respHtml.equals("ecsm0400_detl")))
      {
        wp.initOption ="";
        wp.optionKey = "";
        if (wp.colStr("kk_air_type").length()>0)
        {
          wp.optionKey = wp.colStr("kk_air_type");
        }
        if (wp.colStr("air_type").length()>0)
        {
          wp.initOption ="--";
        }
        this.dddwList("dddw_air_type_b"
                ,"mkt_air_parm"
                ,"trim(air_type)"
                ,"trim(air_name)"
                ," where pwd_type = '02'");
        
        wp.optionKey = "";
        wp.initOption ="";
        if (wp.colStr("kk_ref_ip_code").length()>0)
        {
          wp.optionKey = wp.colStr("kk_ref_ip_code");
        }
        if (wp.colStr("ref_ip_code").length()>0)
        {
          wp.initOption ="--";
        }
        this.dddwList("dddw_ref_ip_code"
                ,"ecs_ref_ip_addr"
                ,"trim(ref_ip_code)"
                ,"trim(ref_name)"
                ," where 1 = 1 ");
      }
      if ((wp.respHtml.equals("ecsm0400")))
      {
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_air_type").length()>0)
        {
          wp.optionKey = wp.colStr("ex_air_type");
        }
        this.dddwList("dddw_air_type_b"
                ,"mkt_air_parm"
                ,"trim(air_type)"
                ,"trim(air_name)"
                ," where pwd_type = '02'");
      }
    } catch(Exception ex){}
  }
  // ************************************************************************
  public void commAirType(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " air_name as column_air_name "
              + " from mkt_air_parm "
              + " where 1 = 1 "
              + " and   air_type = '"+wp.colStr(ii,"air_type")+"'"
      ;
      if (wp.colStr(ii,"air_type").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_air_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void wfAjaxFunc3(TarokoCommon wr) throws Exception
  {
    super.wp = wr;

    if (wp.itemStr("ax_win_t_passphase_i").length()==0) return;

    if (selectAjaxFunc30(
            wp.itemStr("ax_win_t_passphase_i_c"),
            wp.itemStr("ax_win_t_passphase_i"))!=0)
    {
      return;
    }

  }
  // ************************************************************************
  int selectAjaxFunc30(String s1, String s2) throws Exception
  {
    if (!s1.equals(s2))
      alertErr("密碼輸入不一致i,請重新確認");
    else
      alertErr("密碼檢核成功");

    return(0);
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
    if (isEmpty("zz_file_name"))
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

    Ecsm0400Func func =new Ecsm0400Func(wp);

    if (sysUploadAlias.equals("pubi"))
    {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Pubi("MKT_PGP_KEYDTL");
      if (wp.itemStr("public_key_name_i").length()==0)
        wp.colSet("public_key_name_i" , wp.itemStr("zz_file_name"));
    }
    if (sysUploadAlias.equals("prii"))
    {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Prii("MKT_PGP_KEYDTL");
      if (wp.itemStr("private_key_name_i").length()==0)
        wp.colSet("private_key_name_i" , wp.itemStr("zz_file_name"));
    }
    if (sysUploadAlias.equals("pubo"))
    {
      // if has pre check procudure, write in here 
      func.dbDeleteD2Pubo("MKT_PGP_KEYDTL");
      if (wp.itemStr("public_key_name_o").length()==0)
        wp.colSet("public_key_name_o" , wp.itemStr("zz_file_name"));
    }

    CommRoutine comr = new CommRoutine();
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
      if (sysUploadAlias.equals("pubi"))
      {
        if (lineCnt<=0) continue;
      }
      if (sysUploadAlias.equals("prii"))
      {
        if (lineCnt<=0) continue;
      }
      if (sysUploadAlias.equals("pubo"))
      {
        if (lineCnt<=0) continue;
      }

      llCnt++;

      for (int inti=0;inti<10;inti++) logMsg[inti]="";
      logMsg[10]=String.format("%02d",lineCnt);

      if (sysUploadAlias.equals("pubi"))
        if (checkUploadfilePubi(ss)!=0) continue;
      if (sysUploadAlias.equals("prii"))
        if (checkUploadfilePrii(ss)!=0) continue;
      if (sysUploadAlias.equals("pubo"))
        if (checkUploadfilePubo(ss)!=0) continue;
      llOk++;

      if (notifyCnt ==0)
      {
        if (sysUploadAlias.equals("pubi"))
        {
          if (func.dbInsertI2Pubi("MKT_PGP_KEYDTL",uploadFileCol,uploadFileDat) != 1) llErr++;;
        }
        if (sysUploadAlias.equals("prii"))
        {
          if (func.dbInsertI2Prii("MKT_PGP_KEYDTL",uploadFileCol,uploadFileDat) != 1) llErr++;;
        }
        if (sysUploadAlias.equals("pubo"))
        {
          if (func.dbInsertI2Pubo("MKT_PGP_KEYDTL",uploadFileCol,uploadFileDat) != 1) llErr++;;
        }
      }
    }

    if (llErr!=0) notifyCnt =1;
    if (notifyCnt ==1)
    {
      if (sysUploadAlias.equals("pubi"))
        func.dbDeleteD2Pubi("MKT_PGP_KEYDTL");
      if (sysUploadAlias.equals("prii"))
        func.dbDeleteD2Prii("MKT_PGP_KEYDTL");
      if (sysUploadAlias.equals("pubo"))
        func.dbDeleteD2Pubo("MKT_PGP_KEYDTL");
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
  int checkUploadfilePubi(String ss) throws Exception
  {
    ecsm01.Ecsm0400Func func =new ecsm01.Ecsm0400Func(wp);

    for (int inti=0;inti<50;inti++)
    {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // ===========  [M]edia layout =============
    uploadFileCol[0]  = "key_data";

    // ========  [I]nsert table column  ========
    uploadFileCol[1]  = "air_type";
    uploadFileCol[2]  = "ref_ip_code";
    uploadFileCol[3]  = "key_type";
    uploadFileCol[4]  = "key_file_name";
    uploadFileCol[5]  = "key_seq";
    uploadFileCol[6]  = "crt_date";
    uploadFileCol[7]  = "crt_user";
    uploadFileCol[8]  = "trans_seqno";

    // ==== insert table content default =====
    uploadFileDat[1]  = wp.itemStr("air_type");
    uploadFileDat[2]  = wp.itemStr("ref_ip_code");
    uploadFileDat[3]  = "1";
    uploadFileDat[4]  = wp.itemStr("public_key_name_i");
    uploadFileDat[6]  = wp.sysDate;
    uploadFileDat[7]  = wp.loginUser;
    uploadFileDat[8]  = tranSeqStr;

    int ok_flag=0;
    int errFlag=0;
    int[] begPos = {1};

    for (int inti=0;inti<1;inti++)
    {
      uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
      if (uploadFileDat[inti].length()!=0) ok_flag=1;
    }
    recCnt++;
    uploadFileDat[5]=String.format("%d" , recCnt);

    if (uploadFileDat[1].length()==0)
    {
      uploadFileDat[1] =  wp.itemStr("kk_air_type");
      uploadFileDat[2] =  wp.itemStr("kk_ref_ip_code");
    }


    return 0;
  }
  // ************************************************************************
  int checkUploadfilePrii(String ss) throws Exception
  {
    ecsm01.Ecsm0400Func func =new ecsm01.Ecsm0400Func(wp);

    for (int inti=0;inti<50;inti++)
    {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // ===========  [M]edia layout =============
    uploadFileCol[0]  = "key_data";

    // ========  [I]nsert table column  ========
    uploadFileCol[1]  = "air_type";
    uploadFileCol[2]  = "ref_ip_code";
    uploadFileCol[3]  = "key_type";
    uploadFileCol[4]  = "key_file_name";
    uploadFileCol[5]  = "key_seq";
    uploadFileCol[6]  = "crt_date";
    uploadFileCol[7]  = "crt_user";
    uploadFileCol[8]  = "trans_seqno";

    // ==== insert table content default =====
    uploadFileDat[1]  = wp.itemStr("air_type");
    uploadFileDat[2]  = wp.itemStr("ref_ip_code");
    uploadFileDat[3]  = "2";
    uploadFileDat[4]  = wp.itemStr("private_key_name_i");
    uploadFileDat[6]  = wp.sysDate;
    uploadFileDat[7]  = wp.loginUser;
    uploadFileDat[8]  = tranSeqStr;

    int ok_flag=0;
    int errFlag=0;
    int[] begPos = {1};

    for (int inti=0;inti<1;inti++)
    {
      uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
      if (uploadFileDat[inti].length()!=0) ok_flag=1;
    }
    recCnt++;
    uploadFileDat[5]=String.format("%d" , recCnt);

    if (uploadFileDat[1].length()==0)
    {
      uploadFileDat[1] =  wp.itemStr("kk_air_type");
      uploadFileDat[2] =  wp.itemStr("kk_ref_ip_code");
    }

    return 0;
  }
  // ************************************************************************
  int checkUploadfilePubo(String ss) throws Exception
  {
    ecsm01.Ecsm0400Func func =new ecsm01.Ecsm0400Func(wp);

    for (int inti=0;inti<50;inti++)
    {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // ===========  [M]edia layout =============
    uploadFileCol[0]  = "key_data";

    // ========  [I]nsert table column  ========
    uploadFileCol[1]  = "air_type";
    uploadFileCol[2]  = "ref_ip_code";
    uploadFileCol[3]  = "key_type";
    uploadFileCol[4]  = "key_file_name";
    uploadFileCol[5]  = "key_seq";
    uploadFileCol[6]  = "crt_date";
    uploadFileCol[7]  = "crt_user";
    uploadFileCol[8]  = "trans_seqno";

    // ==== insert table content default =====
    uploadFileDat[1]  = wp.itemStr("air_type");
    uploadFileDat[2]  = wp.itemStr("ref_ip_code");
    uploadFileDat[3]  = "3";
    uploadFileDat[4]  = wp.itemStr("public_key_name_o");
    uploadFileDat[6]  = wp.sysDate;
    uploadFileDat[7]  = wp.loginUser;
    uploadFileDat[8]  = tranSeqStr;

    int okFlag=0;
    int errFlag=0;
    int[] begPos = {1};

    for (int inti=0;inti<1;inti++)
    {
      uploadFileDat[inti] = comm.getStr(ss, inti+1 ,",");
      if (uploadFileDat[inti].length()!=0) okFlag=1;
    }
    recCnt++;
    uploadFileDat[5]=String.format("%d" , recCnt);

    if (uploadFileDat[1].length()==0)
    {
      uploadFileDat[1] =  wp.itemStr("kk_air_type");
      uploadFileDat[2] =  wp.itemStr("kk_ref_ip_code");
    }


    return 0;
  }
  // ************************************************************************
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
// ************************************************************************

}  // End of class
