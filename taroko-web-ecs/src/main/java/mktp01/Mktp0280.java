/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/07/21  V1.00.02   Allen Ho      Initial                              *
 * 111/11/28  V1.00.03  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package mktp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0280 extends BaseEdit
{
  private final String PROGNAME = "紅利積點兌換電子禮券批號設定作業處理程式110/07/21 V1.00.02";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0280Func func = null;
  String kk1;
  String orgTabName = "mkt_gift_bpexchg";
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
    else if (eqIgno(wp.buttonCode, "procMethod_setbno"))
    {/* 設定批號 */
      strAction = "U";
      procMethodSetbno();
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
    else if (eqIgno(wp.buttonCode, "AJAX"))
    {/* nothing to do */
      strAction = "";
      switch (wp.itemStr("methodName")) {
        case "wf_ajax_func_2":
            wfAjaxFunc2(wr);
            break;

        default:
            break;
      }
    }

    funcSelect();
    dddwSelect();
    initButton();
  }
  // ************************************************************************
  @Override
  public void queryFunc() throws Exception
  {
    if (queryCheck()!=0) return;
    wp.whereStr = "WHERE 1=1 "
            + sqlChkEx(wp.itemStr("ex_tran_date"), "6", "")
            + sqlChkEx(wp.itemStr("ex_vendor_no"), "1", "")
            + sqlChkEx(wp.itemStr("ex_cellar_cond"), "5", "")
            + sqlChkEx(wp.itemStr("ex_bef_cond"), "8", "")
            + sqlChkEx(wp.itemStr("ex_sel_cond"), "4", "")
            + " and gift_type='3'     "
            + " and return_date=''     "
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
            + "a.tran_seqno,"
            + "a.tran_date,"
            + "'' as id_no,"
            + "'' as chi_name,"
            + "a.gift_no,"
            + "a.exchg_cnt,"
            + "a.cellar_phone,"
            + "a.ecoupon_bno,"
            + "a.ecoupon_date_s,"
            + "a.id_p_seqno";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereOrder = " "
            + " order by tran_date"
    ;

    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlNotFind())
    {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commIdNo1("comm_id_no");
    commChiName1("comm_chi_name");


    //list_wkdata();
    wp.setPageValue();
  }
  // ************************************************************************
  void listWkdata()  throws Exception
  {
    String sql1 = "";

    sql1 = "select sum(a.exchg_cnt) as com_ecoupon_cnt, "
            + "       count(*) as com_total_cnt "
            + "from mkt_gift_bpexchg a "
            + " where a.gift_type   = '3' "
            + " and   a.return_date = '' "
            + " and   a.cellar_phone != '' ";


    if (wp.itemStr("ex_sel_cond").equals("N"))
      sql1 = sql1
              + " and   ecoupon_bno = '' ";

    if (wp.itemStr("ex_sel_cond").equals("Y"))
      sql1 = sql1
              + " and   ecoupon_bno != '' ";

    if (wp.itemStr("ex_bef_cond").equals("N"))
      sql1 = sql1
              + " and tran_date  between  '"+ wp.itemStr("ex_tran_date_s") +"' "
              + "                and      '"+ wp.itemStr("ex_tran_date_e") +"' "
              ;
    else
      sql1 = sql1
              + " and  ((tran_date <= '" + wp.itemStr("ex_tran_date_s") + "' "
              + "   and ecoupon_bno = '' ) "
              + "  or   ( a.tran_date between '" + wp.itemStr("ex_tran_date_s") + "'  "
              + "                     and     '" + wp.itemStr("ex_tran_date_e") + "'))  ";
    ;


    if (wp.itemStr("ex_vendor_no").length()!=0)
      sql1 = sql1
              + " and  gift_no in (select gift_no "
              + "                  from   mkt_gift "
              + "                  where  vendor_no = '"+ wp.itemStr("ex_vendor_no") +"') "
              ;

    sqlSelect(sql1);
    if (sqlRowNum <= 0) return;
    wp.colSet("ex_total_cnt", String.format("%d",(int)sqlNum("com_total_cnt")));
    wp.itemSet("ex_total_cnt", String.format("%d",(int)sqlNum("com_total_cnt")));
    wp.colSet("ex_ecoupon_cnt", String.format("%d",(int)sqlNum("com_ecoupon_cnt")));
    wp.itemSet("ex_ecoupon_cnt", String.format("%d",(int)sqlNum("com_ecoupon_cnt")));

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
            + "a.tran_seqno,"
            + "a.tran_date,"
            + "'' as id_no,"
            + "'' as chi_name,"
            + "a.gift_no,"
            + "a.exchg_cnt,"
            + "a.ecoupon_bno,"
            + "a.ecoupon_date_s,"
            + "a.cellar_phone,"
            + "a.ecoupon_date,"
            + "a.pay_date,"
            + "'' as vendor_name,"
            + "a.id_p_seqno";

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
    commIdNo1("comm_id_no");
    commChiName1("comm_chi_name");
    commGiftNo("comm_gift_no");
    commVendorName("comm_vendor_name");
    checkButtonOff();
  }
  // ************************************************************************
  public void saveFunc() throws Exception
  {
    mktp01.Mktp0280Func func =new mktp01.Mktp0280Func(wp);

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
      if ((wp.respHtml.equals("mktp0280")))
      {
        wp.initOption ="";
        wp.optionKey = itemKk("ex_vendor_no");
        if (wp.colStr("ex_vendor_no").length()>0)
        {
          wp.optionKey = wp.colStr("ex_vendor_no");
        }
        lsSql = "";
        if ((wp.itemStr("ex_tran_date_s").length()!=0)&&
                (wp.itemStr("ex_tran_date_s").length()!=0))
        {
          lsSql =  procDynamicDddwVendorNo(wp.itemStr("ex_tran_date_s"),wp.itemStr("ex_tran_date_e"));

          wp.optionKey = wp.itemStr("ex_vendor_no");
          dddwList("dddw_vendor_no", lsSql);
          wp.colSet("ex_vendor_no", "");
        }
      }
    } catch(Exception ex){}
  }
  // ************************************************************************
  public int queryCheck() throws Exception
  {

    if ((wp.itemStr("ex_tran_date_s").length()==0)||
            (wp.itemStr("ex_tran_date_e").length()==0))
    {
      alertErr("兌換起迄日期均不可空白");
      buttonOff("btnAdd_disable");
      return(1);
    }
    if (wp.itemStr("ex_vendor_no").length()==0)
    {
      alertErr("供應商代號必須選取");
      buttonOff("btnAdd_disable");
      return(1);
    }

    return(0);
  }
  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) throws Exception
  {
    if (sqCond.equals("1"))
    {
      if (empty(wp.itemStr("ex_vendor_no"))) return "";
      return " and  a.gift_no in "
              + "    (select gift_no "
              + "     from   mkt_gift "
              + "     where  vendor_no = '"+ wp.itemStr("ex_vendor_no") +"') "
              ;
    }

    if (sqCond.equals("4"))
    {
      if (empty(wp.itemStr("ex_sel_cond"))) return "";
      if (wp.itemStr("ex_sel_cond").equals("Y"))
        return " and  ecoupon_bno !='' ";
      else if (wp.itemStr("ex_sel_cond").equals("N"))
        return " and  ecoupon_bno ='' ";
    }

    if (sqCond.equals("5"))
    {
      if (wp.itemStr("ex_cellar_cond").equals("Y"))
        return " and  cellar_phone ='' ";
      else
        return " and  cellar_phone !='' ";
    }

    if (sqCond.equals("6"))
    {
      if (wp.itemStr("ex_bef_cond").equals("N"))
        return sqlStrend(wp.itemStr("ex_tran_date_s"), wp.itemStr("ex_tran_date_e"), "a.tran_date") ;
      else
      {
        return " and  ((tran_date <= '" + wp.itemStr("ex_tran_date_s") + "' "
                + "   and ecoupon_bno = '' ) "
                + "  or   ( a.tran_date between '" + wp.itemStr("ex_tran_date_s") + "'  "
                + "                     and     '" + wp.itemStr("ex_tran_date_e") + "'))  ";
      }

    }

    if (sqCond.equals("7")) return "";

    return "";
  }
  // ************************************************************************
  public void commIdNo1(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " id_no as column_id_no "
              + " from crd_idno "
              + " where 1 = 1 "
              + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
      ;
      if (wp.colStr(ii,"id_p_seqno").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_id_no");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commChiName1(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " chi_name as column_chi_name "
              + " from crd_idno "
              + " where 1 = 1 "
              + " and   id_p_seqno = '"+wp.colStr(ii,"id_p_seqno")+"'"
      ;
      if (wp.colStr(ii,"id_p_seqno").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_chi_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commGiftNo(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " gift_name as column_gift_name "
              + " from mkt_gift "
              + " where 1 = 1 "
              + " and   gift_no = '"+wp.colStr(ii,"gift_no")+"'"
      ;
      if (wp.colStr(ii,"gift_no").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_gift_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commVendorName(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";

      columnData="";
      sql1 = "select "
              + " a.vendor_no||'_'||b.vendor_name as column_vendor_name "
              + " from mkt_gift a,mkt_vendor b "
              + " where 1 = 1 "
              + " and   a.gift_no = '"+wp.colStr(ii,"gift_no")+"'"
              + " and   a.vendor_no = b.vendor_no "
      ;
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_vendor_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void wfAjaxFunc2(TarokoCommon wr) throws Exception
  {
    String ajaxjVendorNo = "";
    super.wp = wr;


    if (selectAjaxFunc20(
            wp.itemStr("ax_win_tran_date_s"),wp.itemStr("ax_win_tran_date_e"))!=0)
    {
      wp.addJSON("ajaxj_vendor_no", "");
      wp.addJSON("ajaxj_vendor_name", "");
      return;
    }

    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_vendor_no", sqlStr(ii, "vendor_no"));
    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_vendor_name", sqlStr(ii, "vendor_name"));
  }
  // ************************************************************************
  int selectAjaxFunc20(String s1, String s2) throws Exception

  {
    if ((s1.length()==0)||(s2.length()==0)) return(0);
    wp.sqlCmd = " select "
            + " b.vendor_no, "
            + " max(c.vendor_name)  as vendor_name "
            + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
            + " where a.tran_date between  '" + s1 +"' "
            + "                      and   '" + s2 +"' "
            + " and   a.gift_no     = b.gift_no "
            + " and   c.vendor_no   = b.vendor_no "
            + " and   b.gift_type   = '3' "
            + " and   a.deduct_flag = 'Y' "
            + " group by b.vendor_no  "
            + " order by b.vendor_no  "
    ;

    this.sqlSelect();
    if (sqlRowNum<=0)
    {
      alertErr("查無贈品資料");
      return(1);
    }

    return(0);
  }

  // ************************************************************************
  public void procMethodSetbno() throws Exception
  {
    wp.listCount[0] = wp.itemBuff("ser_num").length;

    if (wp.itemStr("ex_total_cnt").length()==0) wp.itemSet("ex_total_cnt", "0");
    if (wp.itemNum("ex_total_cnt")==0)
    {
      alertErr("批號設定需先查詢有效資料");
      return;
    }
    if (wp.itemStr("ex_effect_date_s").length()==0)
    {
      alertErr("批號設定商品生效起日必須輸入");
      return;
    }
    if (!wp.itemStr("ex_check_flag").equals("N"))
      if (wp.itemStr("ex_effect_date_s").compareTo(wp.sysDate)<=0)
      {
        alertErr("生效起日必須大於系統日");
        return;
      }
    if (wp.itemStr("ex_sel_cond").equals("Y"))
    {
      alertErr("批號設定選項不可選已設批號");
      return;
    }

    String sql1 = "";
    if (!wp.itemStr("ex_sel_cond").equals("N"))
    {
      sql1 = " select count(*) as bno_count "
              + " from mkt_gift_bpexchg "
              + " where gift_type = '3' "
              + " and   ecoupon_bno != '' ";

      if (wp.itemStr("ex_bef_cond").equals("N"))
        sql1 = sql1
                + " and tran_date  between  '"+ wp.itemStr("ex_tran_date_s") +"' "
                + "                and      '"+ wp.itemStr("ex_tran_date_e") +"' "
                ;
      else
        sql1 = sql1
                + " and  ((tran_date <= '" + wp.itemStr("ex_tran_date_s") + "' "
                + "   and ecoupon_bno = '' ) "
                + "  or   ( tran_date between '" + wp.itemStr("ex_tran_date_s") + "'  "
                + "                     and     '" + wp.itemStr("ex_tran_date_e") + "'))  ";
      ;

      if (wp.itemStr("ex_vendor_no").length()!=0)
        sql1 = sql1
                + " and  gift_no in ( "
                + "      select gift_no "
                + "      from   mkt_gift "
                + "      where  vendor_no = '"+ wp.itemStr("ex_vendor_no") +"') "
                ;
      sqlSelect(sql1);

      if (sqlRowNum<=0)
      {
        alertErr("查詢 mkt_gift_batchno 失敗");
        return;
      }

      if (sqlNum("bno_count")>0)
      {
        alertErr("選擇之資料不可有已設定批號");
        return;
      }
    }

    sql1 = " select nvl(max(ecoupon_bno),'0000000000') as max_bno "
            + " from mkt_gift_batchno "
            + " where gift_group  = '3' "
            + " and   ecoupon_bno like '" + wp.sysDate +"%' ";

    sqlSelect(sql1);

    String maxDatebno = String.format("%02d", Integer.valueOf(sqlStr("max_bno").substring(8))+1);

    qFrom=1;

    mktp01.Mktp0280Func func =new mktp01.Mktp0280Func(wp);

    rc = func.dbupdateMktGiftBpexchg(maxDatebno);
    rc = func.dbinsertMktGiftBatchno(maxDatebno);

    alertMsg("批號設定完成 !");

    return;

  }
  // ************************************************************************
  public void checkButtonOff() throws Exception
  {
    return;
  }
  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnAdd_disable");

    return;
  }
  // ************************************************************************
  public void funcSelect() throws Exception
  {
    return;
  }
  // ************************************************************************
// ************************************************************************
  String procDynamicDddwVendorNo(String s1, String s2)  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " b.vendor_no as db_code, "
            + " max(b.vendor_no||' '||c.vendor_name)  as db_desc "
            + " from mkt_gift_bpexchg a,mkt_gift b,mkt_vendor c "
            + " where a.tran_date between  '" + s1 +"' "
            + "                      and      '" + s2 +"' "
            + " and   a.gift_no     = b.gift_no "
            + " and   c.vendor_no  = b.vendor_no "
            + " and   b.gift_type   = '3' "
            + " and   a.deduct_flag = 'Y' "
            + " group by b.vendor_no  "
            + " order by b.vendor_no  "
    ;

    return lsSql;
  }
  // ************************************************************************
  String procDynamicDddwEcouponBno()  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " a.ecoupon_bno as db_code, "
            + " b.vendor_no||'-'||b.vendor_name||'('||tran_date_s||'~'||tran_date_s||')-'||ecoupon_cnt||'筆'  as db_desc "
            + " from mkt_gift_batchno a,mkt_vendor b "
            + " where a.vendor_no  = b.vendor_no "
            + " and   a.gift_group   = '3' "
            + " and   a.stop_date = '' "
            + " and   a.ecoupon_date = '' "
            + " order by a.ecoupon_bno desc,a.vendor_no  "
    ;

    return lsSql;
  }


// ************************************************************************

}  // End of class
