/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/03/30  V1.00.01   Allen Ho      Initial                              *
 * 111/11/28  V1.00.02  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package mktp02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6245 extends BaseProc
{
  private final String PROGNAME = "紅利特殊商品資料覆核處理程式110/03/30 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp6245Func func = null;
  String kk1,kk2;
  String km1,km2;
  String fstAprFlag = "";
  String orgTabName = "mkt_spec_gift_t";
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
    else if (eqIgno(wp.buttonCode, "C"))
    {// 資料處理 -/
      strAction = "A";
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
            + sqlCol(wp.itemStr("ex_gift_no"), "a.gift_no")
            + sqlCol(wp.itemStr("ex_crt_date"), "a.crt_date", "like%")
            + sqlCol(wp.itemStr("ex_gift_group"), "a.gift_group")
            + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user")
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
            + "a.gift_no,"
            + "a.gift_group,"
            + "a.gift_type,"
            + "a.gift_name,"
            + "a.crt_user,"
            + "a.crt_date";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereOrder = " "
            + " order by gift_no"
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

    commGiftGroup("comm_gift_group");
    commGiftType("comm_gift_type");
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
      if (wp.itemStr("kk_gift_no").length()==0)
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
            + "aud_type,"
            + "a.gift_no as gift_no,"
            + "a.gift_group as gift_group,"
            + "a.crt_user,"
            + "a.gift_name,"
            + "a.gift_type,"
            + "a.disable_flag,"
            + "a.cash_value,"
            + "a.effect_months,"
            + "a.vendor_no";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereStr = "where 1=1 ";
    if (qFrom==0)
    {
      wp.whereStr = wp.whereStr
              + sqlCol(km1, "a.gift_no")
              + sqlCol(km2, "a.gift_group")
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
    commGiftGroup("comm_gift_group");
    commGiftType("comm_gift_type");
    commCrtUser("comm_crt_user");
    commVendorNo("comm_vendor_no");
    checkButtonOff();
    km1 = wp.colStr("gift_no");
    km2 = wp.colStr("gift_group");
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
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_SPEC_GIFT";
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.gift_no as gift_no,"
            + "a.gift_group as gift_group,"
            + "a.crt_user as bef_crt_user,"
            + "a.gift_name as bef_gift_name,"
            + "a.gift_type as bef_gift_type,"
            + "a.disable_flag as bef_disable_flag,"
            + "a.cash_value as bef_cash_value,"
            + "a.effect_months as bef_effect_months,"
            + "a.vendor_no as bef_vendor_no";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereStr = "where 1=1 "
            + sqlCol(km1, "a.gift_no")
            + sqlCol(km2, "a.gift_group")
    ;

    pageSelect();
    if (sqlNotFind())
    {
      wp.notFound ="";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);

    if (wp.respHtml.indexOf("_detl") > 0)
      wp.colSet("btnStore_disable","");
    commCrtUser("comm_crt_user");
    commGiftGroup("comm_gift_group");
    commGiftType("comm_gift_type");
    commVendorNo("comm_vendor_no");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }
  // ************************************************************************
  void listWkdataAft() throws Exception
  {
  }
  // ************************************************************************
  void listWkdata() throws Exception
  {
    if (!wp.colStr("gift_name").equals(wp.colStr("bef_gift_name")))
      wp.colSet("opt_gift_name","Y");

    if (!wp.colStr("gift_type").equals(wp.colStr("bef_gift_type")))
      wp.colSet("opt_gift_type","Y");
    commGiftType("comm_gift_type");
    commGiftType("comm_bef_gift_type");

    if (!wp.colStr("disable_flag").equals(wp.colStr("bef_disable_flag")))
      wp.colSet("opt_disable_flag","Y");

    if (!wp.colStr("cash_value").equals(wp.colStr("bef_cash_value")))
      wp.colSet("opt_cash_value","Y");

    if (!wp.colStr("effect_months").equals(wp.colStr("bef_effect_months")))
      wp.colSet("opt_effect_months","Y");

    if (!wp.colStr("vendor_no").equals(wp.colStr("bef_vendor_no")))
      wp.colSet("opt_vendor_no","Y");
    commVendorNo("comm_vendor_no");
    commVendorNo("comm_bef_vendor_no",1);

    if (wp.colStr("aud_type").equals("D"))
    {
      wp.colSet("gift_name","");
      wp.colSet("gift_type","");
      wp.colSet("disable_flag","");
      wp.colSet("cash_value","");
      wp.colSet("effect_months","");
      wp.colSet("vendor_no","");
    }
  }
  // ************************************************************************
  void listWkdataSpace() throws Exception
  {
    if (wp.colStr("gift_name").length()==0)
      wp.colSet("opt_gift_name","Y");

    if (wp.colStr("gift_type").length()==0)
      wp.colSet("opt_gift_type","Y");

    if (wp.colStr("disable_flag").length()==0)
      wp.colSet("opt_disable_flag","Y");

    if (wp.colStr("cash_value").length()==0)
      wp.colSet("opt_cash_value","Y");

    if (wp.colStr("effect_months").length()==0)
      wp.colSet("opt_effect_months","Y");

    if (wp.colStr("vendor_no").length()==0)
      wp.colSet("opt_vendor_no","Y");

  }
  // ************************************************************************
  @Override
  public void dataProcess() throws Exception
  {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    String lsUser="";
    mktp02.Mktp6245Func func =new mktp02.Mktp6245Func(wp);

    String[] lsGiftNo = wp.itemBuff("gift_no");
    String[] lsGiftGroup = wp.itemBuff("gift_group");
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

      func.varsSet("gift_no", lsGiftNo[rr]);
      func.varsSet("gift_group", lsGiftGroup[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A"))
        rc =func.dbInsertA4();
      else if (lsAudType[rr].equals("U"))
        rc =func.dbUpdateU4();
      else if (lsAudType[rr].equals("D"))
        rc =func.dbDeleteD4();

      if (rc!=1) alertErr(func.getMsg());
      if (rc == 1)
      {
        commCrtUser("comm_crt_user");
        commGiftGroup("comm_gift_group");
        commGiftType("comm_gift_type");
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
  }
  // ************************************************************************
  @Override
  public void dddwSelect()
  {
    String lsSql ="";
    try {
      if ((wp.respHtml.equals("mktp6245")))
      {
        wp.initOption ="--";
        wp.optionKey = "";
        if (wp.colStr("ex_gift_no").length()>0)
        {
          wp.optionKey = wp.colStr("ex_gift_no");
        }
        lsSql = "";
        lsSql =  procDynamicDddwGiftNo1(wp.colStr("ex_gift_no"));
        wp.optionKey = wp.colStr("ex_gift_no");
        dddwList("dddw_gift_no_1", lsSql);
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
  public void commVendorNo(String s1) throws Exception
  {
    commVendorNo(s1,0);
    return;
  }
  // ************************************************************************
  public void commVendorNo(String s1, int befType) throws Exception
  {
    String columnData="";
    String sql1 = "";
    String befStr="";
    if (befType==1) befStr="bef_";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " vendor_name as column_vendor_name "
              + " from mkt_vendor "
              + " where 1 = 1 "
              + " and   vendor_no = '"+wp.colStr(ii,befStr+"vendor_no")+"'"
      ;
      if (wp.colStr(ii,befStr+"vendor_no").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_vendor_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commGiftGroup(String s1) throws Exception
  {
    String[] cde = {"1","2"};
    String[] txt = {"首刷禮活動","通路活動"};
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
  public void commGiftType(String s1) throws Exception
  {
    String[] cde = {"1","3"};
    String[] txt = {"一般贈品","電子商品"};
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
  public void initPage() {
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
            + " from sec_user a,mkt_spec_gift_t b "
            + " where a.usr_id = b.crt_user "
            + " group by b.crt_user "
    ;

    return lsSql;
  }
  // ************************************************************************
  String procDynamicDddwGiftNo1(String s1)  throws Exception
  {
    String lsSql = "";

    lsSql = " select "
            + " b.gift_no as db_code, "
            + " max(b.gift_no||' '||b.gift_name) as db_desc "
            + " from  mkt_spec_gift_t b "
            + " where b.apr_flag = 'N' "
            + " group by b.gift_no "
    ;

    return lsSql;
  }

// ************************************************************************

}  // End of class
