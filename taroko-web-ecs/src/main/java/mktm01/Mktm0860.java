/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
* 110/8/26   V1.00.04    Wendy Lu    修改與更新程式                                                               *
* 110-11-19  V1.00.05  Yangbo       joint sql replace to parameters way    *
***************************************************************************/
package mktm01;

import mktm01.Mktm0860Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0860 extends BaseEdit {
  private String PROGNAME = "行銷通路活動回饋分析檔處理程式110/8/26 V1.00.04";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0860Func func = null;
  String kk1, kk2;
  String orgTabName = "mkt_channel_anal";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[350];
  String[] uploadFileDat = new String[350];
  String[] logMsg = new String[20];
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
    } else if (eqIgno(wp.buttonCode, "procMethod_aprv")) {/* 確 認 */
      strAction = "U";
      procMethodAprv();
    } else if (eqIgno(wp.buttonCode, "procMethod_unaprv")) {/* 解確認 */
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
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL"))
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
            + sqlCol(wp.itemStr("ex_apr_flag"), "a.apr_flag")
            + sqlStrend(wp.itemStr("ex_crt_date_s"), wp.itemStr("ex_crt_date_e"), "a.crt_date")
            ;
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.apr_flag,"
            + "a.active_code,"
            + "a.active_seq,"
            + "a.crt_date,"
            + "a.bonus_cnt,"
            + "a.fund_cnt,"
            + "a.gift_cnt,"
            + "a.lottery_cnt,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by crt_date desc,active_code,active_seq";

    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commActiveCode("comm_active_code");

    commAprFlag("comm_apr_flag");

    // list_wkdata();
    wp.setPageValue();
  }

//************************************************************************
void listWkdata()  throws Exception
{
 if (wp.itemStr("ex_active_code").length()==0) return;

 String sql1 = "";
 wp.colSet("ex_total_msg1" , "");
 sql1 = " select "
      + "   sum(decode(bonus_pnt,0,0,1)) as bonus_cnt, "
      + "   sum(bonus_pnt) as bonus_pnt, "
      + "   sum(decode(fund_amt,0,0,1)) as fund_cnt, "
      + "   sum(fund_amt)  as fund_amt , "
      + "   sum(decode(gift_int,0,0,1)) as gift_cnt, "
      + "   sum(gift_int)  as gift_int , "
      + "   sum(gift_amt)  as gift_amt  "
      + " from mkt_channel_list "
      + " where 1 = 1 "
//      + " and active_code = '" + wp.itemStr("ex_active_code") +"' "
      + sqlCol(wp.itemStr("ex_active_code"), "active_code")
      ;

 sqlSelect(sql1);

 int totalRow = sqlRowNum;
 if (totalRow == 0) return;

 String tmpstr= "(實際)";
 if (sqlNum("bonus_cnt")>0)
    tmpstr = tmpstr
           + "紅利筆數: " + String.format("%,d",(int)sqlNum("bonus_cnt"))+ "　"
           + "回饋點數：" + String.format("%,d", (int)sqlNum("bonus_pnt"))+ "　";
 if (sqlNum("fund_cnt")>0)
    tmpstr = tmpstr
           + "基金筆數: " + String.format("%,d",(int)sqlNum("fund_cnt"))+ "　"
           + "回饋金額：" + String.format("%,d", (int)sqlNum("fund_amt"))+ "　";
 if (sqlNum("gift_cnt")>0)
    tmpstr = tmpstr
           + "實際贈品筆數: " + String.format("%,d",(int)sqlNum("gift_cnt"))+ "　"
           + "贈品件數：" + String.format("%,d", (int)sqlNum("gift_int"))+ "　"
           + "贈品金額：" + String.format("%,d", (int)sqlNum("gift_amt"))+ "　";

 wp.colSet("ex_total_msg1", tmpstr);

  return;

}
//************************************************************************

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

	kk1 = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (qFrom == 0)
      if (wp.itemStr("kk_active_code").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
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
            + "a.active_code as active_code,"
            + "a.active_seq as active_seq,"
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
            + "a.bonus_cnt,"
            + "a.bonus_pnt,"
            + "a.fund_cnt,"
            + "a.fund_amt,"
            + "a.gift_cnt,"
            + "a.gift_int,"
            + "a.gift_amt,"
            + "a.lottery_cnt,"
            + "a.lottery_int,"
            + "a.apr_date,"
            + "a.apr_user";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_active_code"), "a.active_code")
          + sqlCol(wp.itemStr("kk_active_seq"), "a.active_seq");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(kk1, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + kk1 + "]" + "[" + kk2 + "]");
      return;
    }
    commActiveCode("comm_active_code");
    commAprUser("comm_apr_user");
    checkButtonOff();
  }

//************************************************************************
void datareadWkdata() throws Exception
{
 String sql1 = "select feedback_apr_date, "
             + "       purchase_type_sel, "
             + "       feedback_date "
             + "from   mkt_channel_parm "
//             + "where  active_code =  '"+ wp.colStr("active_code") +"' "
             + "where 1 = 1 " + sqlCol(wp.colStr("active_code"), "active_code")
             ;

 sqlSelect(sql1);

 wp.colSet("feedback_apr_date" , sqlStr("feedback_apr_date"));
 wp.colSet("feedback_date" , sqlStr("feedback_date"));


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
//************************************************************************

  public void saveFunc() throws Exception {
    mktm01.Mktm0860Func func = new mktm01.Mktm0860Func(wp);

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
    String ls_sql = "";
    try {
      if ((wp.respHtml.equals("mktm0860"))) {
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
    if ((wp.itemStr("ex_crt_date_s").length() != 0) && (wp.itemStr("ex_crt_date_e").length() == 0))
      wp.itemSet("ex_crt_date_e", "30001231");

    if ((wp.itemStr("ex_crt_date_s").length() == 0) && (wp.itemStr("ex_crt_date_e").length() != 0))
      wp.itemSet("ex_crt_date_s", "00000000");

    return (0);
  }

  // ************************************************************************
  public void commActiveCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " active_name as column_active_name " + " from MKT_CHANNEL_PARM "
          + " where 1 = 1 "
//              + " and   active_code = '" + wp.colStr(ii, "active_code") + "'";
          + sqlCol(wp.colStr(ii, "active_code"), "active_code");
      if (wp.colStr(ii, "active_code").length() == 0){

    	  wp.colSet(ii, columnData1, columnData);
          continue;
         }

      sqlSelect(sql1);

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_active_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

//************************************************************************
public void commAprUser(String columnData1) throws Exception
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
//           + " and   usr_id = '"+ wp.colStr(ii,"apr_user")+"'"
           + sqlCol(wp.colStr(ii,"apr_user"), "usr_id")
           ;
      if (wp.colStr(ii,"apr_user").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1);
      sqlParm.clear();

      if (sqlRowNum > 0)
         columnData = columnData + sqlStr("column_usr_cname");
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}

  // ************************************************************************
  public void commAprFlag(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"已確認", "未確認"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt1 = cde1.substring(5, cde1.length());
        if (wp.colStr(ii, txt1).equals(cde[inti])) {
          wp.colSet(ii, cde1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void procMethodAprv() throws Exception {

	wp.selectCnt =1;
	commActiveCode("comm_active_code");
	commAprUser("comm_apr_user");

    if (wp.itemStr("apr_date").length() != 0) {
      alertErr2("該資料已確認過 !");
      return;
    }


    mktm01.Mktm0860Func func = new mktm01.Mktm0860Func(wp);

    rc = func.dbUpdateMktChannelAnal(0);

//    String sql1 = "select active_code " + "from   mkt_channel_anal " + "where  active_code =  '"
//        + wp.itemStr("active_code") + "' " + "and    apr_date    = '' ";
    String sql1 = "select active_code " + "from   mkt_channel_anal " + "where 1 = 1 "
            + sqlCol(wp.itemStr("active_code"), "active_code") + "and    apr_date    = '' ";

    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      rc = func.dbUpdateMktChannelParm(0);
      alertMsg("本活動全部確認完成");
    } else {
      alertMsg("本活動[" + wp.itemStr("active_seq") + "]確認完成, 尚有其它序號未覆核 !");
    }

  }

  // ************************************************************************
  public void procMethodUnaprv() throws Exception {

	  wp.selectCnt =1;
	  commActiveCode("comm_active_code");
	  commAprUser("comm_apr_user");

    if (wp.itemStr("apr_date").length() == 0) {
      alertErr2("該資料尚未確認過 !");
      return;
    }

    String sql1 = "select feedback_apr_date " + "from   mkt_channel_parm "
//        + "where  active_code =  '" + wp.itemStr("active_code") + "' ";
        + "where 1 = 1 " + sqlCol(wp.itemStr("active_code"), "active_code");

    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      alertErr2("活動代號不存在 !");
      return;
    }
    if (sqlStr("feedback_apr_date").length() != 0) {
      alertErr2("已覆核資料不可解確認 !");
      return;
    }
    mktm01.Mktm0860Func func = new mktm01.Mktm0860Func(wp);

    rc = func.dbUpdateMktChannelAnal(1);

    if (sqlRowNum > 0) {
      rc = func.dbUpdateMktChannelParm(1);
      alertMsg("本活動[" + wp.itemStr("active_seq") + "]解確認完成 !");
    }

  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
	  if (wp.autUpdate())
	    {
	     wp.colSet("btnaprv_disable","");
	     wp.colSet("btnunaprv_disable","");

	    }
	  else
	    {
	     buttonOff("btnaprv_disable");
	     buttonOff("btnunaprv_disable");
	    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }

  // ************************************************************************
  int selectMktChannelParm() throws Exception {
    String sql1 = "";
//    sql1 = "select " + " active_type " + " from mkt_channel_parm " + " where active_code = '"
//        + wp.itemStr("active_code") + "' ";
    sql1 = "select " + " active_type " + " from mkt_channel_parm " + " where 1 = 1 "
        + sqlCol(wp.itemStr("active_code"), "active_code");

    sqlSelect(sql1);

    if (sqlRowNum <= 0)
      return (-1);

    return (1);
  }

  // ************************************************************************

}  // End of class
