/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 108/01/29  V1.00.01   Ray Ho        Initial                              *
 * 109/04/09  V1.00.02   Andy          update :f_auth_query                 *
 * 109-04-20  v1.00.03  Andy       Update add throws Exception                *
 * 111/10/26  V1.00.04  jiangyigndong  updated for project coding standard    *
 ***************************************************************************/
package cycq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycq2010 extends BaseEdit
{
  private  String PROGNAME = "帳戶利息、利率折扣查詢作業處理程式108/01/29 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  cycq01.Cycq2010Func func = null;
  String mProgName = "cycq2010";
  String kk1;
  //  String org_tab_name = "act_int_hst";
//  String control_tab_name = "";
  int qFrom=0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt =0, recCnt =0, notifyCnt =0;
  int[] datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[50];
  String[] uploadFileDat= new String[50];
  String[] logMsg       = new String[20];

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
    {/* 更新功能 */
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

    dddwSelect();
    initButton();
  }
  // ************************************************************************
  @Override
  public void queryFunc() throws Exception
  {
    if (queryCheck()!=0) return;
    //查詢權限檢查，參考【f_auth_query】
    busi.func.ColFunc func =new busi.func.ColFunc();
    func.setConn(wp);
    String exIdNo = wp.itemStr("ex_id_no");
    if (func.fAuthQuery(mProgName, exIdNo)!=1){
      alertErr(func.getMsg()); return ;
    }
    //
    wp.whereStr = "WHERE 1=1 "
            + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
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
    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.acct_type,"
            + "'' as id_no,"
//               + "a.crt_date,"
            + "a.from_type,"
            + "a.revolve_int_sign,"
            + "a.revolve_int_rate,"
            + "a.revolve_rate_s_month,"
            + "a.revolve_rate_e_month,"
            + "a.n_cycle,"
            + "a.n_sub_int_rate,"
            + "a.min_pay_bal,"
            + "a.bal_sub_int_rate,"
            + "a.p_seqno"
            +", decode(A.update_user,'',A.mod_user,A.update_user) AS mod_user"
            +", decode(A.update_date,'',to_char(A.mod_time,'yyyymmdd'),A.update_date) AS mod_date"
    ;

    wp.daoTable = " act_int_hst a "
    ;
    wp.whereOrder = " "
            + " order by 1 "
    ;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind())
    {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commIdChiName("comm_id_no");


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
//  if (control_tab_name.length()==0)
//     {
//      if (wp.colStr("control_tab_name").length()==0)
//         control_tab_name=org_tab_name;
//      else
//         control_tab_name=wp.colStr("control_tab_name");
//     }
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.acct_type,"
            + "'' as id_no,"
            + "'' as chi_name,"
            + "a.p_seqno,"
            + "a.from_type,"
            + "a.revolve_int_sign,"
            + "a.revolve_int_rate,"
            + "a.revolve_rate_s_month,"
            + "a.revolve_rate_e_month,"
            + "a.n_cycle,"
            + "a.n_sub_int_rate,"
            + "a.min_pay_bal,"
            + "a.bal_sub_int_rate,"
            + "a.revolve_int_sign,"
            + "a.revolve_int_rate,"
            + "a.revolve_rate_s_month,"
            + "a.revolve_rate_e_month,"
//               + "a.crt_date,"
//               + "a.crt_user,"
//               + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
            + "a.mod_pgm"
//          +", decode(A.update_user,'',decode(A.crt_user,'',A.mod_user,A.crt_user),A.update_user) AS mod_user_user"
            +", decode(A.update_user,'',A.mod_user,A.update_user) AS mod_user"
            +", decode(A.update_date,'',to_char(A.mod_time,'yyyymmdd'),A.update_date) AS mod_date"
    ;

    wp.daoTable = " act_int_hst a "
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
    commAcctType("comm_acct_type");
    commIdNo("comm_id_no");
    commChiName("comm_chi_name");
    checkButtonOff();
  }
  // ************************************************************************
  public void saveFunc() throws Exception
  {
    cycq01.Cycq2010Func func =new cycq01.Cycq2010Func(wp);

    rc = func.dbSave(strAction);
    if (rc!=1) alertErr(func.getMsg());
    log(func.getMsg());
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
  }
  // ************************************************************************
  public int queryCheck() throws Exception
  {
    if (wp.itemStr("ex_id_no").length()==0)
    {
      alertErr(" 身分證號為必輸入資料");
      return(1);
    }
    else
    {
      String sql1 = "select b.p_seqno "
              + "from crd_idno a,act_acno b "
              + "where  a.id_no  =  '"+ wp.itemStr("ex_id_no").toUpperCase() +"' "
              + "and    a.id_p_seqno = b.id_p_seqno "
              ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
      {
        alertErr(" 查無此身分證號["+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
        return(1);
      }

    }

    return(0);
  }
  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) throws Exception
  {
    if (sqCond.equals("1"))
    {
      if (empty(wp.itemStr("ex_id_no"))) return "";

      String sql1 = "select b.p_seqno "
              + "from crd_idno a,act_acno b "
              + "where  a.id_no  =  '"+ wp.itemStr("ex_id_no").toUpperCase() +"' "
              + "and    a.id_p_seqno = b.id_p_seqno "
              ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0) return "";

      String andStr = " and (";
      for (int inti=0;inti<sqlRowNum;inti++)
      {
        andStr = andStr + " p_seqno = '"+ sqlStr(inti,"p_seqno")+"' ";
        if (inti!= sqlRowNum-1) andStr = andStr + " or ";
      }

      andStr = andStr + " ) ";
      return andStr;
    }

    return "";
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
      if (wp.colStr(ii,"acct_type").length()==0) continue;
      sqlSelect(sql1);

      if (sqlRowNum>0)
      {
        columnData = columnData + sqlStr("column_chin_name");
        wp.colSet(ii, s1, columnData);
      }
    }
    return;
  }
  // ************************************************************************
  public void commIdNo(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " id_no as column_id_no "
              + " from crd_idno a,act_acno b "
              + " where 1 = 1 "
              + " and   a.id_p_seqno = b.id_p_seqno "
              + " and   b.p_seqno = '"+wp.colStr(ii,"p_seqno")+"'"
      ;
      sqlSelect(sql1);

      if (sqlRowNum>0)
      {
        columnData = columnData + sqlStr("column_id_no");
        wp.colSet(ii, s1, columnData);
      }
    }
    return;
  }
  // ************************************************************************
  public void commChiName(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " chi_name as column_chi_name "
              + " from crd_idno a,act_acno b "
              + " where 1 = 1 "
              + " and   a.id_p_seqno = b.id_p_seqno "
              + " and   b.p_seqno = '"+wp.colStr(ii,"p_seqno")+"'"
      ;
      sqlSelect(sql1);

      if (sqlRowNum>0)
      {
        columnData = columnData + sqlStr("column_chi_name");
        wp.colSet(ii, s1, columnData);
      }
    }
    return;
  }
  // ************************************************************************
  public void commIdChiName(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " id_no||' '||chi_name as column_id_no "
              + " from crd_idno a,act_acno b "
              + " where 1 = 1 "
              + " and   a.id_p_seqno = b.id_p_seqno "
              + " and   b.p_seqno = '"+wp.colStr(ii,"p_seqno")+"'"
      ;
      sqlSelect(sql1);

      if (sqlRowNum>0)
      {
        columnData = columnData + sqlStr("column_id_no");
        wp.colSet(ii, s1, columnData);
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
