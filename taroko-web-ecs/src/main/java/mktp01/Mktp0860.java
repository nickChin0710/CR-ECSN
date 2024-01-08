/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110/8/26    V1.00.04    Wendy Lu    修改與更新程式                                                       *
* 110/11/22  V1.00.05  jiangyingdong       sql injection                   *
* 112/08/31  V1.00.06  Zuwei Su       [覆核]失敗，活動代碼變成其他活動代碼                   *
***************************************************************************/
package mktp01;

import mktp01.Mktp0860Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0860 extends BaseEdit {
  private String PROGNAME = "行銷通路活動回饋分析檔處理程式110/8/26 V1.00.04";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp01.Mktp0860Func func = null;
  String kk1, kk2, kk3;
  String orgTabName = "mkt_channel_anal";
  String orgTab2Name = "mkt_channel_parm";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String upGroupType= "0";

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "procMethod_aprv")) {/* 覆核 */
      strAction = "U";
      procMethodAprv();
    } else if (eqIgno(wp.buttonCode, "procMethod_unaprv")) {/* 解覆核 */
      strAction = "U";
      procMethodUnaprv();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "T")) {/* 動態查詢 */
      querySelect1();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    }else if (eqIgno(wp.buttonCode, "NILL"))
    {/* nothing to do */
    	strAction = "";
        wp.listCount[0] = wp.itemBuff("ser_num").length;
       }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 "
            + sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
            + sqlChkEx(wp.itemStr("ex_apr_flag"), "3", "")
            + sqlStrend(wp.itemStr("ex_feedback_conf_date_s"), wp.itemStr("ex_feedback_conf_date_e"), "a.feedback_conf_date")
            + " and feedback_conf_date!='' and feedback_date=''     "
            ;

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    controlTabName = orgTab2Name;

    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_code, "
            + "a.purchase_date_s,"
            + "a.purchase_date_e,"
            + "a.cal_def_date,"
            + "a.feedback_conf_date,"
            + "a.bonus_type_cond,"
            + "a.fund_code_cond,"
            + "a.other_type_cond,"
            + "a.lottery_cond,"
            + "a.feedback_apr_date,"
            + "a.feedback_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by active_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commActiveCode("comm_active_code");


    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    wp.colSet("bb_active_code", itemKk("data_k2"));
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  public void querySelect1() throws Exception {
    controlTabName = orgTabName;

    kk1 = itemKk("data_k1");
    qFrom = 2;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + " ROW_NUMBER()OVER() as ser_num, "
            + "a.active_code,"
            + "a.active_seq,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date,"
            + "a.purchase_amt_s1,"
            + "a.purchase_amt_e1,"
            + "'' as item_1_1,"
            + "a.active_type_1,"
            + "a.feedback_rate_1,"
            + "a.feedback_amt_1,"
            + "'' as item_1_2,"
            + "a.feedback_lmt_cnt_1,"
            + "a.feedback_lmt_amt_1,"
            + "a.feedback_value_1,"
            + "a.rank_cnt_1,"
            + "a.rank_amt_1,"
            + "'' as item_1_3,"
            + "a.purchase_amt_s2,"
            + "a.purchase_amt_e2,"
            + "'' as item_2_1,"
            + "a.active_type_2,"
            + "a.feedback_rate_2,"
            + "a.feedback_amt_2,"
            + "'' as item_2_2,"
            + "a.feedback_lmt_cnt_2,"
            + "a.feedback_lmt_amt_2,"
            + "a.feedback_value_2,"
            + "a.rank_cnt_2,"
            + "a.rank_amt_2,"
            + "'' as item_2_3,"
            + "a.purchase_amt_s3,"
            + "a.purchase_amt_e3,"
            + "'' as item_3_1,"
            + "a.active_type_3,"
            + "a.feedback_rate_3,"
            + "a.feedback_amt_3,"
            + "'' as item_3_2,"
            + "a.feedback_lmt_cnt_3,"
            + "a.feedback_lmt_amt_3,"
            + "a.feedback_value_3,"
            + "a.rank_cnt_3,"
            + "a.rank_amt_3,"
            + "'' as item_3_3,"
            + "a.purchase_amt_s4,"
            + "a.purchase_amt_e4,"
            + "'' as item_4_1,"
            + "a.active_type_4,"
            + "a.feedback_rate_4,"
            + "a.feedback_amt_4,"
            + "'' as item_4_2,"
            + "a.feedback_lmt_cnt_4,"
            + "a.feedback_lmt_amt_4,"
            + "a.feedback_value_4,"
            + "a.rank_cnt_4,"
            + "a.rank_amt_4,"
            + "'' as item_4_3,"
            + "a.purchase_amt_s5,"
            + "a.purchase_amt_e5,"
            + "'' as item_5_1,"
            + "a.active_type_5,"
            + "a.feedback_rate_5,"
            + "a.feedback_amt_5,"
            + "'' as item_5_2,"
            + "a.feedback_lmt_cnt_5,"
            + "a.feedback_lmt_amt_5,"
            + "a.feedback_value_5,"
            + "a.rank_cnt_5,"
            + "a.rank_amt_5,"
            + "'' as item_5_3,"
            + "a.bonus_cnt,"
            + "a.bonus_pnt,"
            + "a.fund_cnt,"
            + "a.fund_amt,"
            + "a.gift_cnt,"
            + "a.gift_int,"
            + "a.gift_amt,"
            + "a.lottery_cnt,"
            + "a.lottery_int";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(itemKk("data_k1"), "a.active_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlCol(wp.colStr("bb_active_code"), "a.active_code");
    } else {
      wp.whereStr = wp.whereStr + sqlRowId(kk1, "a.rowid");
    }

    pageSelect();
    wp.setListCount(1);
    commActiveCode("comm_active_code");

    if (qFrom != 0) {
      commActiveCode("comm_active_code");
      commCrtUser("comm_apr_user");
    }
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    String sql1 = "select feedback_apr_date, "
            + "       purchase_type_sel, "
            + "       feedback_date "
            + "from   mkt_channel_parm "
            + "where  active_code =  ? "
            ;

    sqlSelect(sql1, new Object[] { wp.colStr("active_code") });

    wp.colSet("feedback_apr_date", sqlStr("feedback_apr_date"));
    wp.colSet("feedback_date", sqlStr("feedback_date"));
    
    for (int inti=1;inti<=5;inti++)
    {
     if (Arrays.asList("1","3","5").contains(sqlStr("purchase_type_sel")))
        wp.colSet("item_"+inti+"_1", "元"); 
     else
        wp.colSet("item_"+inti+"_1", "次"); 
     if (wp.colStr("active_type_"+inti).equals("1"))
        {
         wp.colSet("active_type_"+inti, "紅利"); 
         wp.colSet("item_"+inti+"_2", "點"); 
         wp.colSet("item_"+inti+"_3", "點"); 
        }
     else if (wp.colStr("active_type_"+inti).equals("2"))
        {
         wp.colSet("active_type_"+inti, "基金"); 
         wp.colSet("item_"+inti+"_2", "元"); 
         wp.colSet("item_"+inti+"_3", "元"); 
        }
     else if (wp.colStr("active_type_"+inti).equals("3"))
        {
         wp.colSet("active_type_"+inti, "贈品"); 
         wp.colSet("item_"+inti+"_2", "個"); 
         wp.colSet("item_"+inti+"_3", "個"); 
        }
     else if (wp.colStr("active_type_"+inti).equals("4"))
        {
         wp.colSet("active_type_"+inti, "名單"); 
         wp.colSet("item_"+inti+"_2", "筆"); 
         wp.colSet("item_"+inti+"_3", "筆"); 
        }
     else
        {
         wp.colSet("active_type_"+inti, "　　"); 
         wp.colSet("item_"+inti+"_2", "　"); 
         wp.colSet("item_"+inti+"_3", "　"); 
        }
     }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktp01.Mktp0860Func func = new mktp01.Mktp0860Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktp0860"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_active_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_active_code");
        }
        this.dddwList("dddw_active_code", "mkt_channel_parm", "trim(active_code)",
            "trim(active_name)", " order by purchase_date_s desc");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if ((wp.itemStr("ex_feedback_conf_date_s").length() != 0)
        && (wp.itemStr("ex_feedback_conf_date_e").length() == 0))
      wp.itemSet("ex_feedback_conf_date_e", "30001231");

    if ((wp.itemStr("ex_feedback_conf_date_s").length() == 0)
        && (wp.itemStr("ex_feedback_conf_date_e").length() != 0))
      wp.itemSet("ex_feedback_conf_date_s", "00000000");

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("3")) {
      if (empty(wp.itemStr("ex_apr_flag")))
        return "";
      if (wp.itemStr("ex_apr_flag").equals("Y"))
        return " and feedback_apr_date !='' ";
      else
        return " and feedback_apr_date ='' ";
    }

    return "";
  }

  // ************************************************************************
  public void commActiveCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from mkt_channel_parm "
          + " where 1 = 1 " + " and   active_code = ? ";
      if (wp.colStr(ii, "active_code").length() == 0){
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "active_code") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_active_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }
  
//************************************************************************
public void commCrtUser(String columnData1) throws Exception 
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
           + " and   usr_id = ? "
           ;
      if (wp.colStr(ii,"apr_user").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
       sqlSelect(sql1, new Object[] { wp.colStr(ii,"apr_user") });

      if (sqlRowNum > 0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}
//************************************************************************

  // ************************************************************************
  public void procMethodAprv() throws Exception {

    qFrom = 1;
    wp.colSet("bb_active_code", wp.itemStr("active_code"));
    String sql1 ="select a.feedback_apr_date, "
            + "       a.feedback_date, "
            + "       a.feedback_conf_date, "
            + "       a.lottery_cond, "
            + "       a.lottery_type, "
            + "       b.apr_user "
            + "from   mkt_channel_parm a,mkt_channel_anal b  "
            + "where  a.active_code =  ? "
            + "and    a.active_code =  b.active_code "
            ;

    sqlSelect(sql1, new Object[] { wp.itemStr("active_code") });
    if (sqlRowNum <= 0) {
      alertErr2("活動代號不存在 !");
      dataRead();
      return;
    }
    if (sqlStr("apr_user").equals(wp.loginUser))
    {
     alertErr2("承辦人員不可同覆核人員 !");
     dataRead();
     return;
    }
    if (sqlStr("feedback_conf_date").length()==0)
    {
     alertErr2("該資料承辦人員尚未確認 !");
     dataRead();
     return;
    }
    if (sqlStr("feedback_apr_date").length() != 0) {
      alertErr2("該資料已覆核過 !");
      dataRead();
      return;
    }
    if ((sqlStr("lottery_cond").equals("Y"))&&
    	      (sqlStr("lottery_type").equals("1")))
    	     {
    	      sql1 = "select draw_no "
    	           + "from   mkt_draw_main  "
    	           + "where  draw_no =  ? "
    	           ;
             sqlSelect(sql1, new Object[] { "CHAN" + wp.itemStr("active_code") });
    	      if (sqlRowNum <= 0)
    	         {
    	    	  alertErr2("抽獎活動未設定[CHAN"+ wp.itemStr("active_code") + "] !");
    	          dataRead();
    	          return;
    	         }
    	     }
    mktp01.Mktp0860Func func = new mktp01.Mktp0860Func(wp);

    rc = func.dbUpdateMktChannelParm(0);

    controlTabName = orgTabName;

    //alertErr2("該資料覆核完成 !");
    alertMsg("該資料覆核完成 !");
    dataRead();

  }

  // ************************************************************************
  public void procMethodUnaprv() throws Exception {

    qFrom = 1;
    wp.colSet("bb_active_code",wp.itemStr("active_code"));
    String sql1 = "select a.feedback_apr_date, "
                + "       a.feedback_date, "
                + "       a.feedback_conf_date, "
                + "       b.apr_user "
                + "from   mkt_channel_parm a,mkt_channel_anal b  "
                + "where  a.active_code =  ? "
                + "and    a.active_code =  b.active_code "
                ;

    sqlSelect(sql1, new Object[] { wp.itemStr("active_code") });
    if (sqlRowNum <= 0) {
      alertErr2("活動代號不存在 !");
      dataRead();
      return;
    }
    if (sqlStr("feedback_apr_date").length() == 0) {
      alertErr2("該資料未覆核過 !");
      dataRead();
      return;
    }
    if (sqlStr("apr_user").equals(wp.loginUser))
    {
     alertErr2("承辦人員不可同覆核人員 !");
     dataRead();
     return;
    }
    if (sqlStr("feedback_date").length() != 0) {
      alertErr2("該資料已覆核回饋完成, 不可解覆核 !");
      dataRead();
      return;
    }
    mktp01.Mktp0860Func func = new mktp01.Mktp0860Func(wp);

    rc = func.dbUpdateMktChannelParm(1);

    //alertErr2("該資料已解覆核完成 !");
    alertMsg("該資料已解覆核完成 !");
    dataRead();
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage()
  {
	  return;
	 }
	 String toNosb(String stra,int stralen)
	 {
	  if (stra.length()>=stralen) return stra;
	  String retStra="";
	  for (int inti=0;inti<(stralen-stra.length());inti++)
	     retStra = retStra + "&nbsp;";
	  return  retStra + stra;
	 }
  // ************************************************************************

} // End of class
