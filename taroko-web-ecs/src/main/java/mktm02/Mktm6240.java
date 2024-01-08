/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/09  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-07-17  V1.00.03   shiyuqi        rename tableName &FiledName         *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 110/11/11  V1.00.04  jiangyingdong       sql injection   
* 111/11/01  V1.00.05   machao       程式頁面，欄位調整                *
* 112/02/03  V1.00.06   Grace Huag   基金更名為現金回饋                *
* 112/03/24  V1.00.07   Zuwei Su     增匯入名單功能                *
* 112/03/27  V1.00.08   Zuwei Su     出現’修改完成’訊息, 上載button旁的筆數未出現                *
* 112/04/04  V1.00.09   Ryan         修改名單匯入時預設活動序號帶00                *
* 112/06/01  V1.00.10   Zuwei Su     增加欄位mcht_in_flag，暫時註解                *
* 112/06/05  V1.00.11   Grace Huang  mcht_in_flag更名為mcht_in_cond              *
* 112/06/21  V1.00.12   Zuwei Su     欄位new_hldr_sel沒有寫入db                *
***************************************************************************/
package mktm02;

import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6240 extends BaseEdit {
  private ArrayList<Object> params = new ArrayList<Object>();
  private final String PROGNAME = "首刷禮活動回饋參數處理程式111/11/1 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  String rowid;
  String activeCode;
  String fstAprFlag = "";
  String orgTabName = "mkt_fstp_parm";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];
  String upGroupType = "0";

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
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      wp.itemSet("aud_type", "A");
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U3";
      updateFuncU3R();
    } else if (eqIgno(wp.buttonCode, "I")) {/* 單獨新鄒功能 */
      strAction = "I";
      /*
       * kk1 = item_kk("data_k1"); kk2 = item_kk("data_k2"); kk3 = item_kk("data_k3");
       */
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFuncD3R();
    } else if (eqIgno(wp.buttonCode, "R2")) {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
    } else if (eqIgno(wp.buttonCode, "U2")) {/* 明細更新 */
      strAction = "U2";
      updateFuncU2();
    } else if (eqIgno(wp.buttonCode, "R4")) {// 明細查詢 -/
      strAction = "R4";
      dataReadR4();
    } else if (eqIgno(wp.buttonCode, "U4")) {/* 明細更新 */
      strAction = "U4";
      updateFuncU4();
    } else if (eqIgno(wp.buttonCode, "R3")) {// 明細查詢 -/
      strAction = "R3";
      dataReadR3();
    } else if (eqIgno(wp.buttonCode, "U3")) {/* 明細更新 */
      strAction = "U3";
      updateFuncU3();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD2")) {/* 匯入檔案 */
      procUploadFile(2);
      checkButtonOff();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if(eqIgno(wp.buttonCode, "AJAX")) {
    	wfAjaxFunc2();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
        + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "");

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
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.active_code," + "a.active_name," + "a.issue_date_s," + "a.issue_date_e,"
        + "a.active_type," + "a.stop_date," + "a.record_cond," + "a.add_value_cond,"
        + "a.add_value," + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");
    commActiveType("comm_active_type");

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    rowid = itemKk("data_k1");
    qFrom = 1;
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
            + "a.active_code as active_code,"
            + "a.active_name,"
            + "a.stop_flag,"
            + "a.stop_date,"
            + "a.stop_desc,"
            + "a.issue_date_s,"
            + "a.issue_date_e,"
            + "a.effect_months,"
            + "a.purchase_days,"
            + "a.n1_days,"
            + "a.achieve_cond,"
            + "a.new_hldr_cond,"
            + "a.new_hldr_sel,"
            + "a.new_hldr_days,"
            + "a.new_group_cond,"
            + "a.acct_type_sel,"
            + "a.group_code_sel,"
            + "a.source_code_sel,"
            + "a.card_type_sel,"
            + "a.promote_dept_sel,"
            + "a.list_cond,"
            + "a.list_flag,"
            + "a.list_use_sel,"
            + "'' as list_flag_cnt,"
            + "a.mcc_code_sel,"
            + "a.merchant_sel,"
            + "a.mcht_group_sel,"
//            + "a.mcht_in_flag,"
            + "a.mcht_in_cond,"            
            + "a.mcht_in_amt,"
            + "a.in_merchant_sel,"
            + "a.in_mcht_group_sel,"
            + "a.pos_entry_sel,"
            + "a.ucaf_sel,"
            + "a.eci_sel,"
            + "a.bl_cond,"
            + "a.ca_cond,"
            + "a.it_cond,"
            + "a.it_flag,"
            + "a.id_cond,"
            + "a.ao_cond,"
            + "a.ot_cond,"
            + "a.linebc_cond,"
            + "a.banklite_cond,"
            + "a.selfdeduct_cond,"
            + "a.anulfee_cond,"
            + "a.anulfee_days,"
            + "a.action_pay_cond,"
            + "a.action_pay_times,"
            + "a.sms_nopurc_cond,"
            + "a.sms_nopurc_days,"
            + "a.nopurc_msg_id_g,"
            + "a.nopurc_msg_id_c,"
            + "a.sms_half_cond,"
            + "a.sms_half_days,"
            + "a.half_cnt_cond,"
            + "a.half_cnt,"
            + "a.half_andor_cond,"
            + "a.half_amt_cond,"
            + "a.half_amt,"
            + "a.half_msg_id_g,"
            + "a.half_msg_id_c,"
            + "a.sms_addvalue_cond,"
            + "a.sms_addvalue_days,"
            + "a.addvalue_msg_id_g,"
            + "a.addvalue_msg_id_c,"
            + "a.sms_send_cond,"
            + "a.send_msg_id,"
            + "a.sms_send_days,"
            + "a.multi_fb_type,"
            + "a.record_cond,"
            + "a.record_group_no,"
            + "a.active_type,"
            + "a.bonus_type,"
            + "a.tax_flag,"
            + "a.fund_code,"
            + "a.group_type,"
            + "a.prog_code,"
            + "a.prog_s_date,"
            + "a.prog_e_date,"
            + "a.gift_no,"
            + "a.spec_gift_no,"
            + "a.per_amt_cond,"
            + "a.per_amt,"
            + "a.perday_cnt_cond,"
            + "a.perday_cnt,"
            + "a.sum_amt_cond,"
            + "a.sum_amt,"
            + "a.sum_cnt_cond,"
            + "a.sum_cnt,"
            + "a.threshold_sel,"
            + "a.purchase_type_sel,"
            + "a.purchase_amt_s1,"
            + "a.purchase_amt_e1,"
            + "a.feedback_amt_1,"
            + "a.purchase_amt_s2,"
            + "a.purchase_amt_e2,"
            + "a.feedback_amt_2,"
            + "a.purchase_amt_s3,"
            + "a.purchase_amt_e3,"
            + "a.feedback_amt_3,"
            + "a.purchase_amt_s4,"
            + "a.purchase_amt_e4,"
            + "a.feedback_amt_4,"
            + "a.purchase_amt_s5,"
            + "a.purchase_amt_e5,"
            + "a.feedback_amt_5,"
            + "a.feedback_limit,"
            + "a.add_value_cond,"
            + "a.add_value,"
            + "a.mkt_fstp_gift_cond,"
            + "a.crt_user,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date,"
            + "a.nopurc_msg_pgm,"
            + "a.half_msg_pgm,"
            + "a.c_record_cond,"
            + "a.c_record_group_no,"
            + "a.new_hldr_flag,"
            + "a.mcht_seq_flag,"
            + "a.nopurc_g_cond,"
            + "a.half_g_cond,"
            + "a.send_msg_pgm ";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(activeCode, "a.active_code");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
    } else {
      wp.colSet("aud_type", wp.itemStr("ex_apr_flag"));
      wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }
    commGroupType("comm_group_type");
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");   
    listWkdata(); 
    checkButtonOff();
    activeCode = wp.colStr("active_code");
    commfuncAudType("aud_type");
    dataReadR3R();
    datareadWkdata();
  }

  // ************************************************************************
  void datareadWkdata() {
    wp.colSet("prog_code1", wp.colStr("prog_code") + "-" + wp.colStr("prog_s_date"));

    String sql1 = "";
//    sql1 = "select " + " prog_desc " + " from ibn_prog " + " where prog_code   = '"
//        + wp.colStr("prog_code") + "'" + " and   prog_s_date = '" + wp.colStr("prog_s_date") + "'"
    sql1 = "select " + " prog_desc " + " from ibn_prog " + " where prog_code   = ? and   prog_s_date = ? "

    ;
    sqlSelect(sql1, new Object[] { wp.colStr("prog_code"), wp.colStr("prog_s_date") });

    if (sqlRowNum > 0) {
      wp.colSet("prog_desc", sqlStr("prog_desc"));
    }

  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
      wp.colSet("list_flag_cnt", listMktImfstpList("mkt_imfstp_list_t", "mkt_imfstp_list",
              wp.colStr("active_code"), ""));
//      wp.colSet("acct_type_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "1"));
//      wp.colSet("group_code_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "2"));
//      wp.colSet("mcc_code_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "5"));
      wp.colSet("merchant_sel_cnt",
              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "3"));
//      wp.colSet("mcht_group_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "6"));
//      wp.colSet("mcht_cname_sel_cnt",
//              listMktBnCdata("mkt_bn_cdata_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "A"));
//      wp.colSet("mcht_ename_sel_cnt",
//              listMktBnCdata("mkt_bn_cdata_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "B"));
//      wp.colSet("it_term_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "4"));
//      wp.colSet("terminal_id_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "7"));
//      wp.colSet("pos_entry_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "8"));
//      wp.colSet("platform_kind_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "9"));
//      wp.colSet("platform_group_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "10"));
//      wp.colSet("channel_type_sel_cnt",
//              listMktBnData("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"), "15"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
       wp.colSet("list_flag_cnt" ,
               listMktImfstpList("mkt_imfstp_list","mkt_imfstp_list",wp.colStr("active_code"),""));
      // wp.colSet("acct_type_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"1"));
      // wp.colSet("group_code_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"2"));
      // wp.colSet("mcc_code_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"5"));
       wp.colSet("merchant_sel_cnt" ,
       listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"3"));
      // wp.colSet("mcht_group_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"6"));
      // wp.colSet("mcht_cname_sel_cnt" ,
      // listMktBnCdata("mkt_bn_cdata","MKT_FSTP_PARM",wp.colStr("active_code"),"A"));
      // wp.colSet("mcht_ename_sel_cnt" ,
      // listMktBnCdata("mkt_bn_cdata","MKT_FSTP_PARM",wp.colStr("active_code"),"B"));
      // wp.colSet("it_term_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"4"));
      // wp.colSet("terminal_id_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"7"));
      // wp.colSet("pos_entry_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"8"));
      // wp.colSet("platform_kind_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"9"));
      // wp.colSet("platform_group_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"10"));
      // wp.colSet("channel_type_sel_cnt" ,
      // listMktBnData("mkt_bn_data","MKT_FSTP_PARM",wp.colStr("active_code"),"15"));
  }


  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + " a.aud_type as aud_type, "
            + "a.active_code as active_code,"
            + "a.active_name as active_name,"
            + "a.stop_flag as stop_flag,"
            + "a.stop_date as stop_date,"
            + "a.stop_desc as stop_desc,"
            + "a.issue_date_s as issue_date_s,"
            + "a.issue_date_e as issue_date_e,"
            + "a.effect_months as effect_months,"
            + "a.purchase_days as purchase_days,"
            + "a.n1_days as n1_days,"
            + "a.achieve_cond as achieve_cond,"
            + "a.new_hldr_cond as new_hldr_cond,"
            + "a.new_hldr_sel as new_hldr_sel,"
            + "a.new_hldr_days as new_hldr_days,"
            + "a.new_group_cond as new_group_cond,"
            + "a.acct_type_sel as acct_type_sel,"
            + "a.group_code_sel as group_code_sel,"
            + "a.source_code_sel as source_code_sel,"
            + "a.card_type_sel as card_type_sel,"
            + "a.promote_dept_sel as promote_dept_sel,"
            + "a.list_cond as list_cond,"
            + "a.list_flag as list_flag,"
            + "a.list_use_sel as list_use_sel,"
            + "'' as list_flag_cnt,"
            + "a.mcc_code_sel as mcc_code_sel,"
            + "a.merchant_sel as merchant_sel,"
            + "a.mcht_group_sel as mcht_group_sel,"
//            + "a.mcht_in_flag as mcht_in_flag,"
            + "a.mcht_in_cond as mcht_in_cond,"
            + "a.mcht_in_amt as mcht_in_amt,"
            + "a.in_merchant_sel as in_merchant_sel,"
            + "a.in_mcht_group_sel as in_mcht_group_sel,"
            + "a.pos_entry_sel as pos_entry_sel,"
            + "a.ucaf_sel as ucaf_sel,"
            + "a.eci_sel as eci_sel,"
            + "a.bl_cond as bl_cond,"
            + "a.ca_cond as ca_cond,"
            + "a.it_cond as it_cond,"
            + "a.it_flag as it_flag,"
            + "a.id_cond as id_cond,"
            + "a.ao_cond as ao_cond,"
            + "a.ot_cond as ot_cond,"
            + "a.linebc_cond as linebc_cond,"
            + "a.banklite_cond as banklite_cond,"
            + "a.selfdeduct_cond as selfdeduct_cond,"
            + "a.anulfee_cond as anulfee_cond,"
            + "a.anulfee_days as anulfee_days,"
            + "a.action_pay_cond as action_pay_cond,"
            + "a.action_pay_times as action_pay_times,"
            + "a.sms_nopurc_cond as sms_nopurc_cond,"
            + "a.sms_nopurc_days as sms_nopurc_days,"
            + "a.nopurc_msg_id_g as nopurc_msg_id_g,"
            + "a.nopurc_msg_id_c as nopurc_msg_id_c,"
            + "a.sms_half_cond as sms_half_cond,"
            + "a.sms_half_days as sms_half_days,"
            + "a.half_cnt_cond as half_cnt_cond,"
            + "a.half_cnt as half_cnt,"
            + "a.half_andor_cond as half_andor_cond,"
            + "a.half_amt_cond as half_amt_cond,"
            + "a.half_amt as half_amt,"
            + "a.half_msg_id_g as half_msg_id_g,"
            + "a.half_msg_id_c as half_msg_id_c,"
            + "a.sms_addvalue_cond as sms_addvalue_cond,"
            + "a.sms_addvalue_days as sms_addvalue_days,"
            + "a.addvalue_msg_id_g as addvalue_msg_id_g,"
            + "a.addvalue_msg_id_c as addvalue_msg_id_c,"
            + "a.sms_send_cond as sms_send_cond,"
            + "a.send_msg_id as send_msg_id,"
            + "a.sms_send_days as sms_send_days,"
            + "a.multi_fb_type as multi_fb_type,"
            + "a.record_cond as record_cond,"
            + "a.record_group_no as record_group_no,"
            + "a.active_type as active_type,"
            + "a.bonus_type as bonus_type,"
            + "a.tax_flag as tax_flag,"
            + "a.fund_code as fund_code,"
            + "a.group_type as group_type,"
            + "a.prog_code as prog_code,"
            + "a.prog_s_date as prog_s_date,"
            + "a.prog_e_date as prog_e_date,"
            + "a.gift_no as gift_no,"
            + "a.spec_gift_no as spec_gift_no,"
            + "a.per_amt_cond as per_amt_cond,"
            + "a.per_amt as per_amt,"
            + "a.perday_cnt_cond as perday_cnt_cond,"
            + "a.perday_cnt as perday_cnt,"
            + "a.sum_amt_cond as sum_amt_cond,"
            + "a.sum_amt as sum_amt,"
            + "a.sum_cnt_cond as sum_cnt_cond,"
            + "a.sum_cnt as sum_cnt,"
            + "a.threshold_sel as threshold_sel,"
            + "a.purchase_type_sel as purchase_type_sel,"
            + "a.purchase_amt_s1 as purchase_amt_s1,"
            + "a.purchase_amt_e1 as purchase_amt_e1,"
            + "a.feedback_amt_1 as feedback_amt_1,"
            + "a.purchase_amt_s2 as purchase_amt_s2,"
            + "a.purchase_amt_e2 as purchase_amt_e2,"
            + "a.feedback_amt_2 as feedback_amt_2,"
            + "a.purchase_amt_s3 as purchase_amt_s3,"
            + "a.purchase_amt_e3 as purchase_amt_e3,"
            + "a.feedback_amt_3 as feedback_amt_3,"
            + "a.purchase_amt_s4 as purchase_amt_s4,"
            + "a.purchase_amt_e4 as purchase_amt_e4,"
            + "a.feedback_amt_4 as feedback_amt_4,"
            + "a.purchase_amt_s5 as purchase_amt_s5,"
            + "a.purchase_amt_e5 as purchase_amt_e5,"
            + "a.feedback_amt_5 as feedback_amt_5,"
            + "a.feedback_limit as feedback_limit,"
            + "a.add_value_cond as add_value_cond,"
            + "a.add_value as add_value,"
            + "a.mkt_fstp_gift_cond as mkt_fstp_gift_cond,"
            + "a.crt_user as crt_user,"
            + "a.crt_date as crt_date,"
            + "a.apr_user as apr_user,"
            + "a.apr_date as apr_date,"
            + "a.nopurc_msg_pgm as nopurc_msg_pgm,"
            + "a.half_msg_pgm as half_msg_pgm,"
            + "a.c_record_cond as c_record_cond,"
            + "a.c_record_group_no as c_record_group_no,"
            + "a.new_hldr_flag as new_hldr_flag,"
            + "a.mcht_seq_flag as mcht_seq_flag,"
            + "a.nopurc_g_cond as nopurc_g_cond,"
            + "a.half_g_cond as half_g_cond,"
            + "a.send_msg_pgm as send_msg_pgm ";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commGroupType("comm_group_type");
    commCrtuser("comm_crt_user");
    commApruser("comm_apr_user");   
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdataAft();
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    activeCode = wp.itemStr("active_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      activeCode = wp.itemStr("active_code");
      strAction = "D";
      deleteFunc();
      if (fstAprFlag.equals("Y")) {
        qFrom = 0;
        controlTabName = orgTabName;
      }
    } else {
      strAction = "A";
      wp.itemSet("aud_type", "D");
      insertFunc();
    }
    dataRead();
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void updateFuncU3R() throws Exception {
    qFrom = 0;
    activeCode = wp.itemStr("active_code");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1) {
        dataReadR3R();;
        datareadWkdata();
      }
    } else {
      activeCode = wp.itemStr("active_code");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void dataReadR2() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_FSTP_PARM' ";
    if (wp.respHtml.equals("mktm6240_gncd"))
      wp.whereStr += " and data_type  = 'G' ";
    if (wp.respHtml.equals("mktm6240_fgcd"))
      wp.whereStr += " and data_type  = 'F' ";
    if (wp.respHtml.equals("mktm6240_actp"))
      wp.whereStr += " and data_type  = '1' ";
    if (wp.respHtml.equals("mktm6240_srcd"))
      wp.whereStr += " and data_type  = '3' ";
    if (wp.respHtml.equals("mktm6240_cdtp"))
      wp.whereStr += " and data_type  = '4' ";
    if (wp.respHtml.equals("mktm6240_pmdp"))
      wp.whereStr += " and data_type  = '5' ";
    if (wp.respHtml.equals("mktm6240_mccd"))
      wp.whereStr += " and data_type  = '6' ";
    if (wp.respHtml.equals("mktm6240_aaa1"))
      wp.whereStr += " and data_type  = '8' ";
    if (wp.respHtml.equals("mktm6240_aaat"))
      wp.whereStr += " and data_type  = '10' ";
    if (wp.respHtml.equals("mktm6240_ucaf"))
      wp.whereStr += " and data_type  = '12' ";
    if (wp.respHtml.equals("mktm6240_deci"))
      wp.whereStr += " and data_type  = '13' ";
    if (wp.respHtml.equals("mktm6240_apay"))
      wp.whereStr += " and data_type  = 'H' ";
    if (wp.respHtml.equals("mktm6240_smsa"))
      wp.whereStr += " and data_type  = 'A' ";
    if (wp.respHtml.equals("mktm6240_smsc"))
      wp.whereStr += " and data_type  = 'C' ";
    String whereCnt = wp.whereStr;
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
//    whereCnt += " and  data_key = '" + wp.itemStr("active_code") + "'";
    whereCnt += " and  data_key = ?";
    params.add(wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5,6 ";
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm6240_gncd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6240_fgcd"))
      commFstpGift("comm_data_code");
    if (wp.respHtml.equals("mktm6240_actp"))
      commDataCode01("comm_data_code");
    if (wp.respHtml.equals("mktm6240_srcd"))
      commDataCode05("comm_data_code");
    if (wp.respHtml.equals("mktm6240_cdtp"))
      commDataCode024("comm_data_code");
    if (wp.respHtml.equals("mktm6240_pmdp"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm6240_mccd"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm6240_aaa1"))
      commDataCode34("comm_data_code");
    if (wp.respHtml.equals("mktm6240_aaat"))
      commDataCode34("comm_data_code");
    if (wp.respHtml.equals("mktm6240_apay"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6240_smsa"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6240_smsc"))
      commDataCode04("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    Mktm6240Func func = new Mktm6240Func(wp);
    int llOk = 0, llErr = 0;

    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-

    int del2Flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2Flag = 0;
      wp.colSet(ll, "ok_flag", "");

      for (int intm = ll + 1; intm < key1Data.length; intm++)
        if ((key1Data[ll].equals(key1Data[intm]))) {
          for (int intx = 0; intx < optData.length; intx++) {
            if (optData[intx].length() != 0)
              if (((ll + 1) == Integer.valueOf(optData[intx]))
                  || ((intm + 1) == Integer.valueOf(optData[intx]))) {
                del2Flag = 1;
                break;
              }
          }
          if (del2Flag == 1)
            break;

          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        }
    }

    if (llErr > 0) {
      alertErr("資料值重複 : " + llErr);
      return;
    }

    // -delete no-approve-
    if (func.dbDeleteD2() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    int deleteFlag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      deleteFlag = 0;
      // KEY 不可同時為空字串
      if ((empty(key1Data[ll])))
        continue;

      // -option-ON-
      for (int intm = 0; intm < optData.length; intm++) {
        if (optData[intm].length() != 0)
          if ((ll + 1) == Integer.valueOf(optData[intm])) {
            deleteFlag = 1;
            break;
          }
      }
      if (deleteFlag == 1)
        continue;

      func.varsSet("data_code", key1Data[ll]);

      int re = func.dbInsertI2();
      if (re == 1) {
        llOk++;
      } else if (re == 99) {
        llErr++;
        alertErr2("團體代號" + key1Data[ll] + "無法新增");
      } else {
        llErr++;
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR2();
  }

  // ************************************************************************
  public void dataReadR4() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "data_code3, " + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_FSTP_PARM' ";
    if (wp.respHtml.equals("mktm6240_gpcd"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("mktm6240_posn"))
      wp.whereStr += " and data_type  = '11' ";
    String whereCnt = wp.whereStr;
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
//    whereCnt += " and  data_key = '" + wp.itemStr("active_code") + "'";
    whereCnt += " and  data_key = ?";
    params.add(wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5,6,7,8 ";
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm6240_gpcd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6240_posn"))
      commEntryMode("comm_data_code2");
  }

  // ************************************************************************
  public void updateFuncU4() throws Exception {
    Mktm6240Func func = new Mktm6240Func(wp);
    int llOk = 0, llErr = 0;

    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");
    String[] key2Data = wp.itemBuff("data_code2");
    String[] key3Data = wp.itemBuff("data_code3");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-

    int del2Flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2Flag = 0;
      wp.colSet(ll, "ok_flag", "");

      for (int intm = ll + 1; intm < key1Data.length; intm++)
        if ((key1Data[ll].equals(key1Data[intm])) && (key2Data[ll].equals(key2Data[intm]))
            && (key3Data[ll].equals(key3Data[intm]))) {
          for (int intx = 0; intx < optData.length; intx++) {
            if (optData[intx].length() != 0)
              if (((ll + 1) == Integer.valueOf(optData[intx]))
                  || ((intm + 1) == Integer.valueOf(optData[intx]))) {
                del2Flag = 1;
                break;
              }
          }
          if (del2Flag == 1)
            break;

          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        }
    }

    if (llErr > 0) {
      alertErr("資料值重複 : " + llErr);
      return;
    }

    // -delete no-approve-
    if (func.dbDeleteD4() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    int deleteFlag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      deleteFlag = 0;
      // KEY 不可同時為空字串
      if ((empty(key1Data[ll])) && (empty(key2Data[ll])) && (empty(key3Data[ll])))
        continue;

      // -option-ON-
      for (int intm = 0; intm < optData.length; intm++) {
        if (optData[intm].length() != 0)
          if ((ll + 1) == Integer.valueOf(optData[intm])) {
            deleteFlag = 1;
            break;
          }
      }
      if (deleteFlag == 1)
        continue;

      func.varsSet("data_code", key1Data[ll]);
      func.varsSet("data_code2", key2Data[ll]);
      func.varsSet("data_code3", key3Data[ll]);

      if (func.dbInsertI4() == 1)
        llOk++;
      else
        llErr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR4();
  }

  // ************************************************************************
  public void dataReadR3() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_bn_data";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_bn_data_t";
    }

    wp.selectSQL = "hex(rowid) as r2_rowid, " + "ROW_NUMBER()OVER() as ser_num, "
        + "mod_seqno as r2_mod_seqno, " + "data_key, " + "data_code, " + "data_code2, "
        + "mod_user as r2_mod_user ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_FSTP_PARM' ";
    if (wp.respHtml.equals("mktm6240_mrcd"))
      wp.whereStr += " and data_type  = '7' ";
    if (wp.respHtml.equals("mktm6240_inmc"))
      wp.whereStr += " and data_type  = '9' ";
    if (wp.respHtml.equals("mktm6240_smsb"))
      wp.whereStr += " and data_type  = 'B' ";
    if (wp.respHtml.equals("mktm6240_smsd"))
      wp.whereStr += " and data_type  = 'D' ";
    String whereCnt = wp.whereStr;
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
//    whereCnt += " and  data_key = '" + wp.itemStr("active_code") + "'";
    whereCnt += " and  data_key = ?";
    params.add(wp.itemStr("active_code"));
    wp.whereStr += " order by 4,5,6,7 ";
    int cnt1 = selectBndataCount(wp.daoTable, whereCnt);
    if (cnt1 > 300) {
      alertErr2("資料筆數 [" + cnt1 + "] 無法線上新增, 請用上傳匯入處理");
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      return;
    }

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
    if (wp.respHtml.equals("mktm6240_smsb"))
      commCardNote("comm_data_code");
    if (wp.respHtml.equals("mktm6240_smsb"))
      commDataCode02("comm_data_code2");
    if (wp.respHtml.equals("mktm6240_smsd"))
      commCardNote("comm_data_code");
    if (wp.respHtml.equals("mktm6240_smsd"))
      commDataCode02("comm_data_code2");
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    Mktm6240Func func = new Mktm6240Func(wp);
    int llOk = 0, llErr = 0;

    String[] optData = wp.itemBuff("opt");
    String[] key1Data = wp.itemBuff("data_code");
    String[] key2Data = wp.itemBuff("data_code2");

    wp.listCount[0] = key1Data.length;
    wp.colSet("IND_NUM", "" + key1Data.length);
    // -check duplication-

    int del2Flag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      del2Flag = 0;
      wp.colSet(ll, "ok_flag", "");

      for (int intm = ll + 1; intm < key1Data.length; intm++)
        if ((key1Data[ll].equals(key1Data[intm])) && (key2Data[ll].equals(key2Data[intm]))) {
          for (int intx = 0; intx < optData.length; intx++) {
            if (optData[intx].length() != 0)
              if (((ll + 1) == Integer.valueOf(optData[intx]))
                  || ((intm + 1) == Integer.valueOf(optData[intx]))) {
                del2Flag = 1;
                break;
              }
          }
          if (del2Flag == 1)
            break;

          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        }
    }

    if (llErr > 0) {
      alertErr("資料值重複 : " + llErr);
      return;
    }

    // -delete no-approve-
    if (func.dbDeleteD3() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    int deleteFlag = 0;
    for (int ll = 0; ll < key1Data.length; ll++) {
      deleteFlag = 0;
      // KEY 不可同時為空字串
      if ((empty(key1Data[ll])) && (empty(key2Data[ll])))
        continue;

      // -option-ON-
      for (int intm = 0; intm < optData.length; intm++) {
        if (optData[intm].length() != 0)
          if ((ll + 1) == Integer.valueOf(optData[intm])) {
            deleteFlag = 1;
            break;
          }
      }
      if (deleteFlag == 1)
        continue;

      func.varsSet("data_code", key1Data[ll]);
      func.varsSet("data_code2", key2Data[ll]);

      if (func.dbInsertI3() == 1)
        llOk++;
      else
        llErr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR3();
  }

  // ************************************************************************
  public int selectBndataCount(String bndataTable, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndataTable + " " + whereStr;

    sqlSelect(sql1, params.toArray(new Object[params.size()]));
    params.clear();

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    Mktm6240Func func = new Mktm6240Func(wp);

    if (wp.respHtml.indexOf("_detl") > 0)
        if (!wp.colStr("aud_type").equals("Y")) listWkdataAft();

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nadd") > 0)) {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("btnDelete_disable", "");
      this.btnModeAud();
    }
    int rr = 0;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktm6240_nadd")) || (wp.respHtml.equals("mktm6240_detl"))) {
    	  
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("nopurc_msg_pgm").length()>0)
             {
             wp.optionKey = wp.colStr("nopurc_msg_pgm");
             }
          this.dddwList("dddw_nopurc_mgm_pgm"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='SMS_MSG_PGM' and wf_id like 'MktC450N_%'");
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("half_msg_pgm").length()>0)
             {
             wp.optionKey = wp.colStr("half_msg_pgm");
             }
          this.dddwList("dddw_half_mgm_pgm"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='SMS_MSG_PGM' and wf_id like 'MktC450H_%'");          
   
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("send_msg_pgm").length()>0)
             {
             wp.optionKey = wp.colStr("send_msg_pgm");
             }
          this.dddwList("dddw_send_mgm_pgm"
                 ,"ptr_sys_idtab"
                 ,"trim(wf_id)"
                 ,"trim(wf_desc)"
                 ," where wf_type='SMS_MSG_PGM' and wf_id like 'MktC450S_%'");          
          wp.optionKey = "";
          wp.initOption ="--";
          if (wp.colStr("c_record_group_no").length()>0)
             {
             wp.optionKey = wp.colStr("c_record_group_no");
             }
          this.dddwList("dddw_c_record_gp"
                 ,"web_record_group"
                 ,"trim(record_group_no)"
                 ,"trim(record_group_name)"
                 ," where decode(active_date_e,'', '99999999',active_date_e) > to_char(sysdate,'yyyymmdd')");
          
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("record_group_no").length() > 0) {
          wp.optionKey = wp.colStr("record_group_no");
        }
        this.dddwList("dddw_record_gp", "web_record_group", "trim(record_group_no)",
            "trim(record_group_name)", " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("bonus_type").length() > 0) {
          wp.optionKey = wp.colStr("bonus_type");
        }
        this.dddwList("dddw_bonus_type", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='BONUS_NAME'");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("fund_code").length() > 0) {
          wp.optionKey = wp.colStr("fund_code");
        }
        this.dddwList("dddw_func_code", "mkt_loan_parm", "trim(fund_code)", "trim(fund_name)",
            " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("prog_code1").length() > 0) {
          wp.optionKey = wp.colStr("prog_code1");
        }
        this.dddwList("dddw_prog_code",
            "select prog_code||'-'||prog_s_date as db_code, prog_code||'-'||prog_s_date as db_desc  from ibn_prog where prog_flag='Y' and to_char(sysdate,'yyyymmdd')< prog_e_date   order by prog_code,prog_s_date");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("gift_no").length() > 0) {
          wp.optionKey = wp.colStr("gift_no");
        }
        setString(wp.colStr("prog_code"));
        this.dddwList("dddw_gift_no", "ibn_prog_gift", "trim(gift_no)", "trim(gift_name)",
//            " where prog_code = '" + wp.colStr("prog_code")
//            	+ "'  group by gift_no,gift_name order by gift_no,gift_name");
            " where prog_code = ?  group by gift_no,gift_name order by gift_no,gift_name");
        wp.optionKey = "";
        wp.initOption = "--";
        if (wp.colStr("spec_gift_no").length() > 0) {
          wp.optionKey = wp.colStr("spec_gift_no");
        }
        this.dddwList("dddw_spec_gift_no", "mkt_spec_gift", "trim(gift_no)", "trim(gift_name)",
            " where gift_group='1' and disable_flag='N'");
      }
      if ((wp.respHtml.equals("mktm6240_gncd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_fgcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_apay"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_actp"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_acct_type", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_gpcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_srcd"))) {
          wp.initOption ="--";
          wp.optionKey = "";
          if (wp.colStr("sel_data_codea").length()>0)
             {
             wp.optionKey = wp.colStr("sel_data_codea");
             wp.initOption ="";
             }
          this.dddwList("dddw_ptr_group_code"
                 ,"ptr_group_code"
                 ,"trim(group_abbr_code)"
                 ,"trim(group_name)"
                 ," where 1 = 1 ");
          wp.initOption ="--";
          wp.optionKey = "";
          lsSql = "";
          lsSql =  procDynamicDddwDataCodea(wp.itemStr("sel_data_codea"));
          wp.colSet("sel_data_codea" , wp.itemStr("sel_data_codea"));
          dddwList("dddw_data_codea", lsSql);   	  
       // wp.initOption = "";
       // wp.optionKey = "";
       // this.dddwList("dddw_source_code03", "ptr_src_code", "trim(source_code)",
        //    "trim(source_name)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_cdtp"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_datc_code02", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_pmdp"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_mccd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_aaa1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_Code34", "mkt_mcht_gp", "trim(mcht_group_id)",
            "trim(mcht_group_desc)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_aaat"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_Code34", "mkt_mcht_gp", "trim(mcht_group_id)",
            "trim(mcht_group_desc)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_posn"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_bin_typeB", "ptr_bintable", "trim(bin_type)", "", " group by bin_type");
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_entry_modeB", "cca_entry_mode", "trim(entry_mode)", "trim(mode_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_smsa"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_smsb"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        this.dddwList("dddw_lost_code4", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type= 'CARD_NOTE'");
        wp.initOption = "--";
        wp.optionKey = "";
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_smsc"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6240_smsd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_lost_code4", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type= 'CARD_NOTE'");
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    return "";
  }

  // ************************************************************************
  void commfuncAudType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
          break;
        }
    }
  }

  // ************************************************************************
  public void commDataCode04(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
//          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "data_code") + "'";
      	  + " where 1 = 1 " + " and   group_code = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode01(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
//          + " where 1 = 1 " + " and   acct_type = '" + wp.colStr(ii, "data_code") + "'";
      	  + " where 1 = 1 " + " and   acct_type = ?";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode05(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " source_name as column_source_name " + " from ptr_src_code "
//          + " where 1 = 1 " + " and   source_code = '" + wp.colStr(ii, "data_code") + "'";
			+ " where 1 = 1 " + " and   source_code = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_source_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode024(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
//          + " and   card_type = '" + wp.colStr(ii, "data_code") + "'";
      	  + " and   card_type = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode07(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mcc_remark as column_mcc_remark " + " from cca_mcc_risk "
//          + " where 1 = 1 " + " and   mcc_code = '" + wp.colStr(ii, "data_code") + "'";
      + " where 1 = 1 " + " and   mcc_code = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcc_remark");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode34(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mcht_group_desc as column_mcht_group_desc " + " from mkt_mcht_gp "
//          + " where 1 = 1 " + " and   mcht_group_id = '" + wp.colStr(ii, "data_code") + "'";
      + " where 1 = 1 " + " and   mcht_group_id = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcht_group_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commEntryMode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mode_desc as column_mode_desc " + " from cca_entry_mode "
//          + " where 1 = 1 " + " and   entry_mode = '" + wp.colStr(ii, "data_code2") + "'";
      + " where 1 = 1 " + " and   entry_mode = ? ";
      if (wp.colStr(ii, "data_code2").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code2") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mode_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commCardNote(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "data_code") + "'" + " and   wf_type = 'CARD_NOTE' ";
      + " and   wf_id = ? " + " and   wf_type = 'CARD_NOTE' ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode02(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
          + " and   card_type = ? ";
//      + " and   card_type = '" + wp.colStr(ii, "data_code2") + "'";
      if (wp.colStr(ii, "data_code2").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code2") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commGroupType(String cde1) throws Exception {
    String[] cde = {"group_type", "1", "2", "3", "4"};
    String[] txt = {"", "限信用卡兌換(限01,05,06)", "限 VD卡兌換(限90)", "全部任一卡片兌換(01,05,06,90)", "限特定卡號兌換"};
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
  public void commActiveType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4"};
    String[] txt = {"紅利", "現金回饋", "豐富點數", "贈品"};
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
  public void commFstpGift(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 " + " and   group_code = ? ";
//      + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "data_code") + "'";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii, "data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void wfAjaxFunc2() throws Exception {
   // super.wp = wr;
   // String ajaxjGiftNo = "";
   // super.wp = wr;

   // if (wp.itemStr("ax_win_prog_code").length() == 0)
   //   return;
   // if (wp.itemStr("ax_win_prog_s_date").length() == 0)
    //  return;

    selectAjaxFunc20(wp.itemStr("ax_win_prog_code1"), wp.itemStr("ax_win_prog_code"),
        wp.itemStr("ax_win_prog_s_date"));

    if (rc != 1) {
      wp.addJSON("prog_desc", "");
      wp.addJSON("prog_code", "");
      wp.addJSON("prog_s_date", "");
      wp.addJSON("prog_e_date", "");
      return;
    }

    wp.addJSON("prog_desc", sqlStr("prog_desc"));
    wp.addJSON("prog_code", sqlStr("prog_code"));
    wp.addJSON("prog_s_date", sqlStr("prog_s_date"));
    wp.addJSON("prog_e_date", sqlStr("prog_e_date"));

   // if (wp.itemStr("ax_win_prog_code").length() == 0)
   //   return;
   // if (wp.itemStr("ax_win_prog_s_date").length() == 0)
   //   return;

    selectAjaxFunc21(wp.itemStr("ax_win_prog_code1"), wp.itemStr("ax_win_prog_code"),
        wp.itemStr("ax_win_prog_s_date"));

    if (rc != 1) {
      wp.addJSON("ajaxj_gift_no", "");
      wp.addJSON("ajaxj_gift_name", "");
      return;
    }

    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_gift_no", sqlStr(ii, "gift_no"));
    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_gift_name", sqlStr(ii, "gift_name"));
  }

  // ************************************************************************
  void selectAjaxFunc20(String progSDate, String string1, String string2) {
    wp.sqlCmd = " select " + " b.prog_desc as prog_desc ," + " b.prog_code as prog_code ,"
        + " b.prog_s_date as prog_s_date ," + " b.prog_e_date as prog_e_date "
        + " from  ibn_prog b " + " where b.prog_code = ? "
        + " and   b.prog_s_date = ? ";
//    + " from  ibn_prog b " + " where b.prog_code ='" + comm.getStr(progSDate, 1, "-") + "' "
//    + " and   b.prog_s_date ='" + comm.getStr(progSDate, 2, "-") + "' ";
  
    this.sqlSelect(new Object[] { comm.getStr(progSDate, 1, "-"), comm.getStr(progSDate, 2, "-") });
    if (sqlRowNum <= 0)
    {
      alertErr2("活動代碼選擇[" + progSDate + "]查無資料");
    } 
    return;
  }

  // ************************************************************************
  void selectAjaxFunc21(String giftNo, String giftName, String giftSDate) {
    wp.sqlCmd = " select " + " gift_no," + " gift_name," + " gift_s_date," + " gift_e_date"
        + " from  ibn_prog_gift " + " where prog_code = ? "
        + " and   prog_s_date = ? ";
//    + " from  ibn_prog_gift " + " where prog_code ='" + sqlStr("prog_code") + "' "
//    + " and   prog_s_date ='" + sqlStr("prog_s_date") + "' ";
    
  

    this.sqlSelect(new Object[] { sqlStr("prog_code"), sqlStr("prog_s_date") });
    if (sqlRowNum <= 0)
      alertErr2("贈品代碼選擇:[" + giftNo + "]查無資料");

    return;
  }

  // ************************************************************************
  public void wfAjaxFunc4(TarokoCommon wr) throws Exception {
    super.wp = wr;
    String ajaxjDataCode2 = "";
    super.wp = wr;


    selectAjaxFunc40(wp.itemStr("ax_win_data_code"));

    if (rc != 1) {
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
  void selectAjaxFunc40(String cardNote) {
    wp.sqlCmd = " select " + " '' as data_code2,  " + " '' as name  " + " from  ptr_businday "
        + " union " + " select " + " card_type as data_code2," + " name " + " from  ptr_card_type ";
    if (cardNote.length() > 0)
      wp.sqlCmd = wp.sqlCmd + " where card_note  = ? ";
//    wp.sqlCmd = wp.sqlCmd + " where card_note  = '" + cardNote + "' ";

    this.sqlSelect(new Object[] { cardNote });
    if (sqlRowNum <= 0)
      alertErr2("卡片等級:[" + cardNote + "]查無資料");

    return;
  }

  // ************************************************************************
  public void procUploadFile(int loadType) throws Exception {
    if (wp.colStr(0, "ser_num").length() > 0)
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }

    if (loadType == 2)
      fileDataImp2();
  }

  // ************************************************************************
  int fileUpLoad() {
    TarokoUpload func = new TarokoUpload();
    try {
      func.actionFunction(wp);
      wp.colSet("zz_file_name", func.fileName);
    } catch (Exception ex) {
      wp.log("file_upLoad: error=" + ex.getMessage());
      return -1;
    }

    return func.rc;
  }

  // ************************************************************************
  void fileDataImp2() throws Exception {
      Mktm6240Func func = new Mktm6240Func(wp);
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");

    if (fi == -1)
      return;

    String sysUploadType = wp.itemStr("sys_upload_type");
    String sysUploadAlias = wp.itemStr("sys_upload_alias");

    if (sysUploadAlias.equals("list"))
       {
        // if has pre check procudure, write in here
        func.dbDeleteD2List("MKT_IMFSTP_LIST_T");
       }
    if (sysUploadAlias.equals("aaa1")) {
      // if has pre check procudure, write in here
    }
    if (sysUploadAlias.equals("aaa3")) {
      // if has pre check procudure, write in here
    }

    if (sysUploadAlias.equals("aaa1"))
      func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
    if (sysUploadAlias.equals("aaa3"))
      func.dbDeleteD2Aaa3("MKT_BN_DATA_T");

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    tranSeqStr = comr.getSeqno("MKT_MODSEQ");

    String string = "";
    int llOk = 0, llCnt = 0, llErr = 0;
    int lineCnt = 0;
    while (true) {
      string = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y"))
        break;
      lineCnt++;
      if (sysUploadAlias.equals("list"))
      {
       if (lineCnt<=0) continue;
       if (string.length() < 2) continue;
      }
      if (sysUploadAlias.equals("aaa1")) {
        if (lineCnt <= 0)
          continue;
        if (string.length() < 2)
          continue;
      }
      if (sysUploadAlias.equals("aaa3")) {
        if (lineCnt <= 0)
          continue;
        if (string.length() < 2)
          continue;
      }

      llCnt++;

      for (int inti = 0; inti < 10; inti++)
        logMsg[inti] = "";
      logMsg[10] = String.format("%02d", lineCnt);

      if (sysUploadAlias.equals("list"))
         if (checkUploadfileList(string)!=0) 
             continue;
      if (sysUploadAlias.equals("aaa1"))
        if (checUploadfileAaa1(string) != 0)
          continue;
      if (sysUploadAlias.equals("aaa3"))
        if (checkUploadfileAaa3(string) != 0)
          continue;

      if (errorCnt == 0) {
          if (sysUploadAlias.equals("list"))
          {
           if (func.dbInsertI2List("MKT_IMFSTP_LIST_T",uploadFileCol,uploadFileDat) != 1) 
               llErr++;
           else 
               llOk++;
          }
        if (sysUploadAlias.equals("aaa1")) {
          if (func.dbInsertI2Aaa1("MKT_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
            llOk++;
          else
            llErr++;
        }
        if (sysUploadAlias.equals("aaa3")) {
          if (func.dbInsertI2Aaa3("MKT_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
            llOk++;
          else
            llErr++;
        }
      }
    }

    if (errorCnt > 0) {
        if (sysUploadAlias.equals("list"))
            func.dbDeleteD2List("MKT_IMFSTP_LIST_T");
      if (sysUploadAlias.equals("aaa1"))
        func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
      if (sysUploadAlias.equals("aaa3"))
        func.dbDeleteD2Aaa3("MKT_BN_DATA_T");
      func.dbInsertEcsNotifyLog(tranSeqStr, errorCnt);
    }

    sqlCommit(1); // 1:commit else rollback

    alertMsg("資料匯入處理筆數 : " + llCnt + ", 成功(" + llOk + "), 重複(" + llErr + "), 失敗("
        + (llCnt - llOk - llErr) + ")");

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);


    return;
  }

  // ************************************************************************
  int checkUploadfileList(String tmpStr) throws Exception {
    Mktm6240Func func = new Mktm6240Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "list_data";

    // ======== [I]nsert table column ========
    uploadFileCol[1]  = "active_code";
    uploadFileCol[2]  = "list_flag";
    uploadFileCol[3]  = "id_p_seqno";
    uploadFileCol[4]  = "acct_type";
    uploadFileCol[5]  = "p_seqno";
    uploadFileCol[6]  = "card_no";
    uploadFileCol[7]  = "ori_card_no";
    uploadFileCol[8]  = "vd_flag";
    uploadFileCol[9]  = "active_seq";

    // ==== insert table content default =====
    uploadFileDat[1]  = wp.itemStr("active_code");
    uploadFileDat[2]  = wp.itemStr("list_flag");
    uploadFileDat[9]  = "00";

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti=0;inti<1;inti++)
    {
     uploadFileDat[inti] = comm.getStr(tmpStr, inti+1 ,",");
     if (uploadFileDat[inti].length()!=0) okFlag=1;
    }
    if (okFlag == 0)
      return (1);
    //******************************************************************
    errorCnt=0;
    if (wp.itemStr("list_flag").equals("1")) return(0);
  /*
       if (select_crd_idno()!=0)
          {
           error_cnt=1;
           logMsg[0]               = "資料檢核錯誤";           // 原因說明
           logMsg[1]               = "3" ;                     // 錯誤類別
           logMsg[2]               = "1";                      // 欄位位置
           logMsg[3]               = uploadFileDat[0];         // 欄位內容
           logMsg[4]               = "信用卡無此卡人";         // 錯誤說明
           logMsg[5]               = "信用卡人檔無此 ID 資料"; // 欄位說明
           func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
           return(0);
          }
  */
    //******************************************************************
  /*
    if (wp.item_ss("list_flag").equals("6"))
       if (select_dbc_idno()!=0)
          {
           error_cnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3" ;                 // 錯誤類別
           logMsg[2]               = "1";                  // 欄位位置
           logMsg[3]               = uploadFileDat[0];     // 欄位內容
           logMsg[4]               = "VD無此卡人";           // 錯誤說明
           logMsg[5]               = "VD卡人檔無此 ID 資料"; // 欄位說明
           func.dbInsert_ecs_media_errlog(tran_seqStr,logMsg);
           return(0);
          }
  */

    //******************************************************************
    if (wp.itemStr("list_flag").equals("2"))
       if (selectCrdCard()!=0)
       if (selectDbcCard()!=0)
          {
           errorCnt=1;
           logMsg[0]  = "資料內容錯誤";         // 原因說明
           logMsg[1]  = "2";                    // 錯誤類別
           logMsg[2]  = "1";                    // 欄位位置
           logMsg[3]  = uploadFileDat[0];       // 欄位內容
           logMsg[4]  = "VD無此卡號";          // 錯誤說明
           logMsg[5]  = "卡片檔無此卡號資料";     // 欄位說明

           logMsg[3]  = uploadFileDat[0];     // 欄位內容
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("3"))
       if (selectIpsCard()!=0)
          {
           errorCnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3";                    // 錯誤類別
           logMsg[2]               = "1";                    // 欄位位置
           logMsg[3]               = uploadFileDat[0];       // 欄位內容
           logMsg[4]               = "無此一卡通卡號";          // 錯誤說明
           logMsg[5]               = "一卡通卡片檔無此卡號資料";     // 欄位說明

           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("4"))
       if (selectTscCard()!=0)
          {
           errorCnt=1;
           logMsg[0] = "資料檢核錯誤";       // 原因說明
           logMsg[1] = "3" ;                 // 錯誤類別
           logMsg[2] = "1";                  // 欄位位置
           logMsg[3] = uploadFileDat[0];     // 欄位內容
           logMsg[4] = "無此悠遊卡號";
           logMsg[5] = "悠遊卡片檔無此卡號資料";
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************
    if (wp.itemStr("list_flag").equals("5"))
       if (selectIchCard()!=0)
          {
           errorCnt=1;
           logMsg[0]               = "資料檢核錯誤";       // 原因說明
           logMsg[1]               = "3" ;                 // 錯誤類別
           logMsg[2]               = "1";                  // 欄位位置
           logMsg[3]               = uploadFileDat[0];     // 欄位內容
           logMsg[4]               = "無此愛金卡號";
           logMsg[5]               = "愛金卡片檔無此卡號資料";
           func.dbInsertEcsMediaErrlog(tranSeqStr,logMsg);
           return(0);
          }
    //******************************************************************

    return 0;
   }

   // ************************************************************************
   int selectCrdCard() throws Exception {
       wp.sqlCmd = " select "
               + " id_p_seqno, "
               + " acct_type, "
               + " p_seqno, "
               + " card_no, "
               + " ori_card_no "
               + " from crd_card "
               + " where card_no     = '"
               + uploadFileDat[0]
               + "' ";
       this.sqlSelect();

       if (sqlRowNum <= 0)
           return (1);

       uploadFileDat[3] = sqlStr("id_p_seqno");
       uploadFileDat[4] = sqlStr("acct_type");
       uploadFileDat[5] = sqlStr("p_seqno");
       uploadFileDat[6] = sqlStr("card_no");
       uploadFileDat[7] = sqlStr("ori_card_no");
       uploadFileDat[8] = "N";
       return (0);
   }

   // ************************************************************************
   int selectDbcCard() throws Exception {
       wp.sqlCmd = " select "
               + " id_p_seqno, "
               + " acct_type, "
               + " p_seqno, "
               + " card_no, "
               + " ori_card_no "
               + " from dbc_card "
               + " where card_no     = '"
               + uploadFileDat[0]
               + "' ";
       this.sqlSelect();

       if (sqlRowNum <= 0)
           return (1);

       uploadFileDat[3] = sqlStr("id_p_seqno");
       uploadFileDat[4] = sqlStr("acct_type");
       uploadFileDat[5] = sqlStr("p_seqno");
       uploadFileDat[6] = sqlStr("card_no");
       uploadFileDat[7] = sqlStr("ori_card_no");
       uploadFileDat[8] = "Y";
       return (0);
   }

   // ************************************************************************
   int selectIchCard() throws Exception {
       wp.sqlCmd = " select "
               + " a.id_p_seqno, "
               + " a.acct_type, "
               + " a.p_seqno, "
               + " a.card_no, "
               + " a.ori_card_no "
               + " from crd_card a,ich_card b "
               + " where a.card_no     = b.card_no "
               + " and   ich_card_no   = '"
               + uploadFileDat[0]
               + "' ";
       this.sqlSelect();

       if (sqlRowNum <= 0)
           return (1);

       uploadFileDat[3] = sqlStr("id_p_seqno");
       uploadFileDat[4] = sqlStr("acct_type");
       uploadFileDat[5] = sqlStr("p_seqno");
       uploadFileDat[6] = sqlStr("card_no");
       uploadFileDat[7] = sqlStr("ori_card_no");
       uploadFileDat[8] = "N";
       return (0);
   }

   // ************************************************************************
   int selectIpsCard() throws Exception {
       wp.sqlCmd = " select "
               + " a.id_p_seqno, "
               + " a.acct_type, "
               + " a.p_seqno, "
               + " a.card_no, "
               + " a.ori_card_no "
               + " from crd_card a,ips_card b "
               + " where a.card_no     = b.card_no "
               + " and   ips_card_no   = '"
               + uploadFileDat[0]
               + "' ";
       this.sqlSelect();

       if (sqlRowNum <= 0)
           return (1);

       uploadFileDat[3] = sqlStr("id_p_seqno");
       uploadFileDat[4] = sqlStr("acct_type");
       uploadFileDat[5] = sqlStr("p_seqno");
       uploadFileDat[6] = sqlStr("card_no");
       uploadFileDat[7] = sqlStr("ori_card_no");
       uploadFileDat[8] = "N";
       return (0);
   }

   // ************************************************************************
   int selectTscCard() throws Exception {
       wp.sqlCmd = " select "
               + " a.id_p_seqno, "
               + " a.acct_type, "
               + " a.p_seqno, "
               + " a.card_no, "
               + " a.ori_card_no "
               + " from crd_card a,tsc_card b "
               + " where a.card_no     = b.card_no "
               + " and   tsc_card_no   = '"
               + uploadFileDat[0]
               + "' ";
       this.sqlSelect();

       if (sqlRowNum <= 0)
           return (1);

       uploadFileDat[3] = sqlStr("id_p_seqno");
       uploadFileDat[4] = sqlStr("acct_type");
       uploadFileDat[5] = sqlStr("p_seqno");
       uploadFileDat[6] = sqlStr("card_no");
       uploadFileDat[7] = sqlStr("ori_card_no");
       uploadFileDat[8] = "N";
       return (0);
   }

  // ************************************************************************
  int checUploadfileAaa1(String string) throws Exception {
    mktm02.Mktm6240Func func = new mktm02.Mktm6240Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "data_code";
    uploadFileCol[1] = "data_code2";

    // ======== [I]nsert table column ========
    uploadFileCol[2] = "table_name";
    uploadFileCol[3] = "data_key";
    uploadFileCol[4] = "data_type";

    // ==== insert table content default =====
    uploadFileDat[2] = "MKT_FSTP_PARM";
    uploadFileDat[3] = wp.itemStr("active_code");
    uploadFileDat[4] = "7";

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(string, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);
    // ******************************************************************
    if ((uploadFileDat[1].length() != 0) && (uploadFileDat[1].length() < 8))

      if (uploadFileDat[1].length() != 0)
        uploadFileDat[1] =
            "00000000".substring(0, 8 - uploadFileDat[1].length()) + uploadFileDat[1];


    return 0;
  }

  // ************************************************************************
  int checkUploadfileAaa3(String string) throws Exception {
    mktm02.Mktm6240Func func = new mktm02.Mktm6240Func(wp);

    for (int inti = 0; inti < 50; inti++) {
      uploadFileCol[inti] = "";
      uploadFileDat[inti] = "";
    }
    // =========== [M]edia layout =============
    uploadFileCol[0] = "data_code";
    uploadFileCol[1] = "data_code2";

    // ======== [I]nsert table column ========
    uploadFileCol[2] = "table_name";
    uploadFileCol[3] = "data_key";
    uploadFileCol[4] = "data_type";

    // ==== insert table content default =====
    uploadFileDat[2] = "MKT_FSTP_PARM";
    uploadFileDat[3] = wp.itemStr("active_code");
    uploadFileDat[4] = "9";

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(string, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);
    // ******************************************************************
    if ((uploadFileDat[1].length() != 0) && (uploadFileDat[1].length() < 8))

      if (uploadFileDat[1].length() != 0)
        uploadFileDat[1] =
            "00000000".substring(0, 8 - uploadFileDat[1].length()) + uploadFileDat[1];


    return 0;
  }

  // ************************************************************************
  // ************************************************************************
  public void checkButtonOff() throws Exception {
	if (wp.colStr("half_g_cond").length()==0)
	{
		wp.colSet("half_g_cond" , "N");
	}
	if (wp.colStr("half_g_cond").equals("N"))
	{
	      buttonOff("btnsmsc_disable");
    }	  
	if (wp.colStr("nopurc_g_cond").length()==0)
	{
		wp.colSet("nopurc_g_cond" , "N");	  
	}
	if (wp.colStr("nopurc_g_cond").equals("N"))
	{
	      buttonOff("btnsmsa_disable");
	}  
    if (wp.colStr("new_group_cond").length() == 0)
      wp.colSet("new_group_cond", "N");

    if (wp.colStr("new_group_cond").equals("N")) {
      buttonOff("btngncd_disable");
    } else {
      wp.colSet("btngncd_disable", "");
    }

    if (wp.colStr("mkt_fstp_gift_cond").length() == 0)
      wp.colSet("mkt_fstp_gift_cond", "N");

    if (wp.colStr("mkt_fstp_gift_cond").equals("N")) {
      buttonOff("btnfgcd_disable");
    } else {
      wp.colSet("btnfgcd_disable", "");
    }

    if (wp.colStr("acct_type_sel").length() == 0)
      wp.colSet("acct_type_sel", "0");

    if (wp.colStr("acct_type_sel").equals("0")) {
      buttonOff("btnactp_disable");
    } else {
      wp.colSet("btnactp_disable", "");
    }

    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("group_code_sel", "0");

    if (wp.colStr("group_code_sel").equals("0")) {
      buttonOff("btngpcd_disable");
    } else {
      wp.colSet("btngpcd_disable", "");
    }

    if (wp.colStr("source_code_sel").length() == 0)
      wp.colSet("source_code_sel", "0");

    if (wp.colStr("source_code_sel").equals("0")) {
      buttonOff("btnsrcd_disable");
    } else {
      wp.colSet("btnsrcd_disable", "");
    }

    if (wp.colStr("card_type_sel").length() == 0)
      wp.colSet("card_type_sel", "0");

    if (wp.colStr("card_type_sel").equals("0")) {
      buttonOff("btncdtp_disable");
    } else {
      wp.colSet("btncdtp_disable", "");
    }

    if (wp.colStr("promote_dept_sel").length() == 0)
      wp.colSet("promote_dept_sel", "0");

    if (wp.colStr("promote_dept_sel").equals("0")) {
      buttonOff("btnpmdp_disable");
    } else {
      wp.colSet("btnpmdp_disable", "");
    }

    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("mcc_code_sel", "0");

    if (wp.colStr("mcc_code_sel").equals("0")) {
      buttonOff("btnmccd_disable");
    } else {
      wp.colSet("btnmccd_disable", "");
    }

    if (wp.colStr("merchant_sel").length() == 0)
      wp.colSet("merchant_sel", "0");

    if (wp.colStr("merchant_sel").equals("0")) {
      buttonOff("btnmrcd_disable");
      buttonOff("uplaaa1_disable");
    } else {
      wp.colSet("btnmrcd_disable", "");
      wp.colSet("uplaaa1_disable", "");
    }

    if (wp.colStr("mcht_group_sel").length() == 0)
      wp.colSet("mcht_group_sel", "0");

    if (wp.colStr("mcht_group_sel").equals("0")) {
      buttonOff("btnaaa1_disable");
    } else {
      wp.colSet("btnaaa1_disable", "");
    }

    if (wp.colStr("in_merchant_sel").length() == 0)
      wp.colSet("in_merchant_sel", "0");

    if (wp.colStr("in_merchant_sel").equals("0")) {
      buttonOff("btninmc_disable");
      buttonOff("uplaaa3_disable");
    } else {
      wp.colSet("btninmc_disable", "");
      wp.colSet("uplaaa3_disable", "");
    }

    if (wp.colStr("in_mcht_group_sel").length() == 0)
      wp.colSet("in_mcht_group_sel", "0");

    if (wp.colStr("in_mcht_group_sel").equals("0")) {
      buttonOff("btnaaat_disable");
    } else {
      wp.colSet("btnaaat_disable", "");
    }

    if (wp.colStr("pos_entry_sel").length() == 0)
      wp.colSet("pos_entry_sel", "0");

    if (wp.colStr("pos_entry_sel").equals("0")) {
      buttonOff("btnposn_disable");
    } else {
      wp.colSet("btnposn_disable", "");
    }

    if (wp.colStr("ucaf_sel").length() == 0)
      wp.colSet("ucaf_sel", "0");

    if (wp.colStr("ucaf_sel").equals("0")) {
      buttonOff("btnucaf_disable");
    } else {
      wp.colSet("btnucaf_disable", "");
    }

    if (wp.colStr("eci_sel").length() == 0)
      wp.colSet("eci_sel", "0");

    if (wp.colStr("eci_sel").equals("0")) {
      buttonOff("btndeci_disable");
    } else {
      wp.colSet("btndeci_disable", "");
    }

    if (wp.colStr("action_pay_cond").length() == 0)
      wp.colSet("action_pay_cond", "N");

    if (wp.colStr("action_pay_cond").equals("N")) {
      buttonOff("btnapay_disable");
    } else {
      wp.colSet("btnapay_disable", "");
    }

    if (wp.colStr("sms_nopurc_cond").length() == 0)
      wp.colSet("sms_nopurc_cond", "N");

    if (wp.colStr("sms_nopurc_cond").equals("N")) {
      buttonOff("btnsmsa_disable");
      buttonOff("btnsmsb_disable");
    } else {
      wp.colSet("btnsmsa_disable", "");
      wp.colSet("btnsmsb_disable", "");
    }

    if (wp.colStr("sms_half_cond").length() == 0)
      wp.colSet("sms_half_cond", "N");

    if (wp.colStr("sms_half_cond").equals("N")) {
      buttonOff("btnsmsc_disable");
      buttonOff("btnsmsd_disable");
    } else {
      wp.colSet("btnsmsc_disable", "");
      wp.colSet("btnsmsd_disable", "");
    }

    if (wp.colStr("sms_addvalue_cond").length() == 0)
      wp.colSet("sms_addvalue_cond", "N");

    if (wp.colStr("sms_addvalue_cond").equals("N")) {
      buttonOff("btnsmse_disable");
      buttonOff("btnsmsf_disable");
    } else {
      wp.colSet("btnsmse_disable", "");
      wp.colSet("btnsmsf_disable", "");
    }

    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
      buttonOff("uplaaa1_disable");
      buttonOff("uplaaa3_disable");
    } else {
    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btngncd_disable");
    buttonOff("btnfgcd_disable");
    buttonOff("btnactp_disable");
    buttonOff("btngpcd_disable");
    buttonOff("btnsrcd_disable");
    buttonOff("btncdtp_disable");
    buttonOff("btnpmdp_disable");
    buttonOff("btnmccd_disable");
    buttonOff("btnmrcd_disable");
    buttonOff("btnaaa1_disable");
    buttonOff("btninmc_disable");
    buttonOff("btnaaat_disable");
    buttonOff("btnposn_disable");
    buttonOff("btnucaf_disable");
    buttonOff("btndeci_disable");
    buttonOff("btnapay_disable");
    buttonOff("btnsmsa_disable");
    buttonOff("btnsmsb_disable");
    buttonOff("btnsmsc_disable");
    buttonOff("btnsmsd_disable");
    buttonOff("btnsmse_disable");
    buttonOff("btnsmsf_disable");
    return;
  }
  // ************************************************************************
//************************************************************************
public void commCrtuser(String columnData1) throws Exception 
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
//                   + " and   usr_id = '"+wp.colStr(ii,"crt_user")+"'"
           ;
      if (wp.colStr(ii,"crt_user").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"crt_user") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}
//************************************************************************
public void commApruser(String columnData1) throws Exception 
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
//                   + " and   usr_id = '"+wp.colStr(ii,"apr_user")+"'"
           ;
      if (wp.colStr(ii,"apr_user").length()==0)
         {
          wp.colSet(ii, columnData1, columnData);
          continue;
         }
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"apr_user") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
} 

//************************************************************************
public String procDynamicDddwDataCodea(String sourceCode)  throws Exception
{
String ls_sql = "";

ls_sql = " select "
       + " source_code as db_code, "
       + " source_code||' '||source_name as db_desc "
       + " from ptr_src_code ";

if (sourceCode.length()>0)
    ls_sql =  ls_sql
           + " where 1=1 " + sqlCol(sourceCode, "source_code", "link%")
//                   + " where source_code like  '" + sourceCode +"%' "
       ;
ls_sql =  ls_sql
       + " order by source_code "
       + " fetch first 999 rows only ";

return ls_sql;
}

    // ************************************************************************
    String listMktBnData(String s1, String s2, String s3, String s4) throws Exception {
        String sql1 = "select "
                + " count(*) as column_data_cnt "
                + " from "
                + s1
                + " "
                + " where 1 = 1 "
                + " and   table_name = '"
                + s2
                + "'"
                + " and   data_key   = '"
                + s3
                + "'"
                + " and   data_type  = '"
                + s4
                + "'";
        sqlSelect(sql1);
    
        if (sqlRowNum > 0)
            return (sqlStr("column_data_cnt"));
    
        return ("0");
    }

    // ************************************************************************
    String listMktImfstpList(String s1, String s2, String s3, String s4) throws Exception {
        String sql1 = "select " + " count(*) as column_data_cnt " + " from " + s1 + " "
        // + " where active_code = '" + s3 +"' "
                + " where 1 = 1 and active_seq = '00' "
                + sqlCol(s3, "active_code");
        sqlSelect(sql1);
    
        if (sqlRowNum > 0)
            return (sqlStr("column_data_cnt"));
    
        return ("0");
    }

} // End of class
