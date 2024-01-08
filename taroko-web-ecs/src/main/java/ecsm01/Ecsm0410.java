/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/04/16  V1.00.01   Allen Ho      Initial                              *
 * 111/11/28  V1.00.02  jiangyigndong  updated for project coding standard  *
 * 111/12/14  V1.00.03  Zuwei Su       output mod_user & mod_time  *
 *                                                                          *
 ***************************************************************************/
package ecsm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0410 extends BaseEdit
{
  private final String PROGNAME = "航空公司代碼維護處理程式111/12/14 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  ecsm01.Ecsm0410Func func = null;
  String kk1;
  String orgTabName = "mkt_air_parm";
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
    else if (eqIgno(wp.buttonCode, "procMethod_upld"))
    {/* 重新上傳 */
      strAction = "U";
      procmethodUpld();
    }
    else if (eqIgno(wp.buttonCode, "procMethod_dnld"))
    {/* 重新下載 */
      strAction = "U";
      procMethodDnld();
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
            + "a.air_type,"
            + "a.air_name,"
            + "a.stop_flag,"
            + "a.pwd_type,"
            + "a.out_ref_ip_code,"
            + "a.in_ref_ip_code,"
            + "a.crt_user,"
            + "a.mod_user,"
            + "a.mod_time";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereOrder = " "
            + " order by air_type"
    ;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind())
    {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commRefIpCode("comm_out_ref_ip_code", "out_ref_ip_code");
    commRefIpCode("comm_in_ref_ip_code", "in_ref_ip_code");
    commCrtUser("comm_crt_user");
    commAprUser("comm_apr_user");

    commUseredit("comm_stop_flag");
    commDsptype("comm_pwd_type");

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
            + "a.air_type as air_type,"
            + "a.air_name,"
            + "a.stop_flag,"
            + "a.stop_date,"
            + "a.stop_desc,"
            + "a.pwd_type,"
            + "a.out_ref_ip_code,"
            + "a.out_file_name,"
            + "a.out_zip_file_name,"
            + "a.in_ref_ip_code,"
            + "a.in_zip_file_name,"
            + "a.in_file_name,"
            + "a.crt_date,"
            + "a.crt_user,"
            + "a.mod_user,"
            + "a.mod_time,"
            + "a.hide_ref_code,"
            + "a.in_air_pwd,"
            + "a.out_air_pwd";

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
    checkButtonOff();
  }
  // ************************************************************************
  void datareadWkdata() throws Exception
  {
    if (wp.colStr("in_air_pwd").length()!=0)
    {
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      wp.colSet("t_in_air_pwd"    , comm.hideUnzipData(wp.colStr("in_air_pwd")  ,wp.colStr("hide_ref_code")));
      wp.colSet("t_in_air_pwd_c"  , wp.colStr("t_in_air_pwd") );
    }

    if (wp.colStr("out_air_pwd").length()!=0)
    {
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      wp.colSet("t_out_air_pwd"    , comm.hideUnzipData(wp.colStr("out_air_pwd")  ,wp.colStr("hide_ref_code")));
      wp.colSet("t_out_air_pwd_c"  , wp.colStr("t_out_air_pwd") );
    }

  }
  // ************************************************************************
  public void saveFunc() throws Exception
  {
    ecsm01.Ecsm0410Func func =new ecsm01.Ecsm0410Func(wp);

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
      if ((wp.respHtml.equals("ecsm0410_detl")))
      {
        wp.optionKey = "";
        wp.initOption ="";
        if (wp.colStr("out_ref_ip_code").length()>0)
        {
          wp.optionKey = wp.colStr("out_ref_ip_code");
        }
        if (wp.colStr("out_ref_ip_code").length()>0)
        {
          wp.initOption ="--";
        }
        this.dddwList("dddw_ref_ip_code"
                ,"ecs_ref_ip_addr"
                ,"trim(ref_ip_code)"
                ,"trim(ref_name)"
                ," where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption ="";
        if (wp.colStr("in_ref_ip_code").length()>0)
        {
          wp.optionKey = wp.colStr("in_ref_ip_code");
        }
        if (wp.colStr("in_ref_ip_code").length()>0)
        {
          wp.initOption ="--";
        }
        this.dddwList("dddw_ref_ip_code2"
                ,"ecs_ref_ip_addr"
                ,"trim(ref_ip_code)"
                ,"trim(ref_name)"
                ," where 1 = 1 ");
      }
    } catch(Exception ex){}
  }
  // ************************************************************************
  public void commRefIpCode(String s1, String ipCodeFieldName) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " ref_name as column_ref_name "
              + " from ecs_ref_ip_addr "
              + " where 1 = 1 "
              + " and   ref_ip_code = '"+wp.colStr(ii,ipCodeFieldName)+"'"
      ;
      if (wp.colStr(ii,ipCodeFieldName).length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_ref_name");
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
  public void commUseredit(String s1) throws Exception
  {
    String[] cde = {"Y","N"};
    String[] txt = {"已停用","運作中"};
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
  public void commDsptype(String s1) throws Exception
  {
    String[] cde = {"00","01","02"};
    String[] txt = {"不加密","壓縮檔","PGP加密"};
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
  public void wfAjaxFunc3(TarokoCommon wr) throws Exception
  {
    super.wp = wr;

    if (wp.itemStr("ax_win_t_out_air_pwd").length()==0) return;

    if (selectAjaxFunc30(
            wp.itemStr("ax_win_t_out_air_pwd_c"),
            wp.itemStr("ax_win_t_out_air_pwd"))!=0)
    {
      return;
    }

  }
  // ************************************************************************
  int selectAjaxFunc30(String s1,String s2) throws Exception
  {
    if (!s1.equals(s2))
      alertErr("密碼輸入不一致i,請重新確認");
    else
      alertErr("密碼檢核成功");

    return(0);
  }

  // ************************************************************************
  public void wfAjaxFunc4(TarokoCommon wr) throws Exception
  {
    super.wp = wr;

    if (wp.itemStr("ax_win_t_in_air_pwd").length()==0) return;

    if (selectAjaxFunc40(
            wp.itemStr("ax_win_t_in_air_pwd_c"),
            wp.itemStr("ax_win_t_in_air_pwd"))!=0)
    {
      return;
    }

  }
  // ************************************************************************
  int selectAjaxFunc40(String s1, String s2) throws Exception
  {
    if (!s1.equals(s2))
      alertErr("密碼輸入不一致i,請重新確認");
    else
      alertErr("密碼檢核成功");

    return(0);
  }

  // ************************************************************************
  public void procmethodUpld() throws Exception
  {
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp) ;

    rc=batch.callBatch("MktF011" + " " + wp.itemStr("air_type"));
    if (rc!=1)
    {
      alertErr("callbatch[MktF011] 失敗");
    }
    else
    {
      alertMsg("批次已啟動成功! ");
    }

  }
  // ************************************************************************
  public void procMethodDnld() throws Exception
  {

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp) ;

    rc=batch.callBatch("MktF008" + " " + wp.itemStr("air_type"));

    if (rc!=1)
    {
      alertErr("callbatch[MktF008] 失敗");
    }
    else
    {
      alertMsg("批次已啟動成功! ");
    }
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
// ************************************************************************

}  // End of class
