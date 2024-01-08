/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110/11/11  V1.00.05  jiangyingdong       sql injection                   *
***************************************************************************/
package mktm02;

import mktm02.Mktm6260Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6260 extends BaseEdit {
  private ArrayList<Object> params = new ArrayList<Object>();
  private String PROGNAME = "特店活動回饋參數檔處理程式108/12/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6260Func func = null;
  String rowid;
  String activeCode;
  String fstAprFlag = "";
  String orgTabName = "mkt_mcht_parm";
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
        + "a.active_code," + "a.active_name," + "a.purchase_date_s," + "a.purchase_date_e,"
        + "a.issue_date_s," + "a.issue_date_e," + "a.active_type," + "a.stop_flag,"
        + "a.record_cond," + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.active_code as active_code," + "a.active_name," + "a.stop_flag," + "a.stop_date,"
        + "a.stop_desc," + "a.active_type," + "a.bonus_type," + "a.tax_flag," + "a.fund_code,"
        + "a.effect_months," + "a.purchase_date_s," + "a.purchase_date_e," + "a.feedback_date,"
        + "a.feedback_key_sel," + "a.issue_date_cond," + "a.issue_date_s," + "a.issue_date_e,"
        + "a.new_hldr_sel," + "a.new_hldr_days," + "a.new_group_cond," + "a.new_hldr_card,"
        + "a.new_hldr_sup," + "a.acct_type_sel," + "a.group_code_sel," + "a.record_cond,"
        + "a.record_group_no," + "a.record_purc_flag," + "a.record_n1_days," + "a.record_n2_days,"
        + "a.bl_cond," + "a.it_cond," + "a.merchant_sel," + "a.mcht_group_sel,"
        + "a.in_merchant_sel," + "a.in_mcht_group_sel," + "a.mcht_in_cond," + "a.mcht_in_per_amt,"
        + "a.mcht_in_cnt," + "a.mcht_in_amt," + "a.mcc_code_sel," + "a.pos_entry_sel,"
        + "a.per_amt_cond," + "a.per_amt," + "a.sum_cnt_cond," + "a.sum_cnt," + "a.sum_amt_cond,"
        + "a.sum_amt," + "a.feedback_rate," + "a.feedback_add_amt," + "a.exchange_amt,"
        + "a.feedback_lmtamt_cond," + "a.feedback_lmt_amt," + "a.feedback_lmtcnt_cond,"
        + "a.feedback_lmt_cnt," + "a.day_lmtamt_cond," + "a.day_lmt_amt," + "a.day_lmtcnt_cond,"
        + "a.day_lmt_cnt," + "a.times_lmtamt_cond," + "a.times_lmt_amt," + "a.crt_user,"
        + "a.crt_date," + "a.apr_user," + "a.apr_date";

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
    checkButtonOff();
    activeCode = wp.colStr("active_code");
    commfuncAudType("aud_type");
    dataRead_R3R();
  }

  // ************************************************************************
  public void dataRead_R3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.active_code as active_code,"
        + "a.active_name as active_name," + "a.stop_flag as stop_flag,"
        + "a.stop_date as stop_date," + "a.stop_desc as stop_desc,"
        + "a.active_type as active_type," + "a.bonus_type as bonus_type,"
        + "a.tax_flag as tax_flag," + "a.fund_code as fund_code,"
        + "a.effect_months as effect_months," + "a.purchase_date_s as purchase_date_s,"
        + "a.purchase_date_e as purchase_date_e," + "a.feedback_date as feedback_date,"
        + "a.feedback_key_sel as feedback_key_sel," + "a.issue_date_cond as issue_date_cond,"
        + "a.issue_date_s as issue_date_s," + "a.issue_date_e as issue_date_e,"
        + "a.new_hldr_sel as new_hldr_sel," + "a.new_hldr_days as new_hldr_days,"
        + "a.new_group_cond as new_group_cond," + "a.new_hldr_card as new_hldr_card,"
        + "a.new_hldr_sup as new_hldr_sup," + "a.acct_type_sel as acct_type_sel,"
        + "a.group_code_sel as group_code_sel," + "a.record_cond as record_cond,"
        + "a.record_group_no as record_group_no," + "a.record_purc_flag as record_purc_flag,"
        + "a.record_n1_days as record_n1_days," + "a.record_n2_days as record_n2_days,"
        + "a.bl_cond as bl_cond," + "a.it_cond as it_cond," + "a.merchant_sel as merchant_sel,"
        + "a.mcht_group_sel as mcht_group_sel," + "a.in_merchant_sel as in_merchant_sel,"
        + "a.in_mcht_group_sel as in_mcht_group_sel," + "a.mcht_in_cond as mcht_in_cond,"
        + "a.mcht_in_per_amt as mcht_in_per_amt," + "a.mcht_in_cnt as mcht_in_cnt,"
        + "a.mcht_in_amt as mcht_in_amt," + "a.mcc_code_sel as mcc_code_sel,"
        + "a.pos_entry_sel as pos_entry_sel," + "a.per_amt_cond as per_amt_cond,"
        + "a.per_amt as per_amt," + "a.sum_cnt_cond as sum_cnt_cond," + "a.sum_cnt as sum_cnt,"
        + "a.sum_amt_cond as sum_amt_cond," + "a.sum_amt as sum_amt,"
        + "a.feedback_rate as feedback_rate," + "a.feedback_add_amt as feedback_add_amt,"
        + "a.exchange_amt as exchange_amt," + "a.feedback_lmtamt_cond as feedback_lmtamt_cond,"
        + "a.feedback_lmt_amt as feedback_lmt_amt,"
        + "a.feedback_lmtcnt_cond as feedback_lmtcnt_cond,"
        + "a.feedback_lmt_cnt as feedback_lmt_cnt," + "a.day_lmtamt_cond as day_lmtamt_cond,"
        + "a.day_lmt_amt as day_lmt_amt," + "a.day_lmtcnt_cond as day_lmtcnt_cond,"
        + "a.day_lmt_cnt as day_lmt_cnt," + "a.times_lmtamt_cond as times_lmtamt_cond,"
        + "a.times_lmt_amt as times_lmt_amt," + "a.crt_user as crt_user,"
        + "a.crt_date as crt_date," + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    checkButtonOff();
    commfuncAudType("aud_type");
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
      if (rc == 1)
        dataRead_R3R();
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
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_MCHT_PARM' ";
    if (wp.respHtml.equals("mktm6260_gncd"))
      wp.whereStr += " and data_type  = 'F' ";
    if (wp.respHtml.equals("mktm6260_actp"))
      wp.whereStr += " and data_type  = '1' ";
    if (wp.respHtml.equals("mktm6260_gpcd"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("mktm6260_aaa1"))
      wp.whereStr += " and data_type  = '8' ";
    if (wp.respHtml.equals("mktm6260_aaat"))
      wp.whereStr += " and data_type  = '10' ";
    if (wp.respHtml.equals("mktm6260_mccd"))
      wp.whereStr += " and data_type  = '6' ";
    if (wp.respHtml.equals("mktm6260_posn"))
      wp.whereStr += " and data_type  = '11' ";
    String whereCnt = wp.whereStr;
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    whereCnt += " and  data_key = ? ";
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
    if (wp.respHtml.equals("mktm6260_gncd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6260_actp"))
      commDataCode01("comm_data_code");
    if (wp.respHtml.equals("mktm6260_gpcd"))
      commDataCode04("comm_data_code");
    if (wp.respHtml.equals("mktm6260_aaa1"))
      commDataCode34("comm_data_code");
    if (wp.respHtml.equals("mktm6260_aaat"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm6260_mccd"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm6260_posn"))
      commEntryMode("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    mktm02.Mktm6260Func func = new mktm02.Mktm6260Func(wp);
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

      if (func.dbInsertI2() == 1)
        llOk++;
      else
        llErr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

    // SAVE後 SELECT
    dataReadR2();
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
    wp.whereStr = "where 1=1" + " and table_name  =  'MKT_MCHT_PARM' ";
    if (wp.respHtml.equals("mktm6260_mrcd"))
      wp.whereStr += " and data_type  = '7' ";
    if (wp.respHtml.equals("mktm6260_inmc"))
      wp.whereStr += " and data_type  = '9' ";
    String whereCnt = wp.whereStr;
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("active_code"));
    whereCnt += " and  data_key = ? ";
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
    if (wp.respHtml.equals("mktm6260_inmc"))
      commDataCode34("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    mktm02.Mktm6260Func func = new mktm02.Mktm6260Func(wp);
    int llok = 0, llerr = 0;

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
          llerr++;
          continue;
        }
    }

    if (llerr > 0) {
      alertErr("資料值重複 : " + llerr);
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
        llok++;
      else
        llerr++;

      // 有失敗rollback，無失敗commit
      sqlCommit(llok > 0 ? 1 : 0);
    }
    alertMsg("資料存檔處理完成  成功(" + llok + "), 失敗(" + llerr + ")");

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
    mktm02.Mktm6260Func func = new mktm02.Mktm6260Func(wp);

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
      if ((wp.respHtml.equals("mktm6260_nadd")) || (wp.respHtml.equals("mktm6260_detl"))) {
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
        if (wp.colStr("record_group_no").length() > 0) {
          wp.optionKey = wp.colStr("record_group_no");
        }
        this.dddwList("dddw_record_gp", "web_record_group", "trim(record_group_no)",
            "trim(record_group_name)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_actp"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_acct_type", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_gpcd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_posn"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_entry_mode", "cca_entry_mode", "trim(entry_mode)", "trim(mode_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_mccd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_aaa1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_Code34", "mkt_mcht_gp", "trim(mcht_group_id)",
            "trim(mcht_group_desc)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_inmc"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_Code34", "mkt_mcht_gp", "trim(mcht_group_id)",
            "trim(mcht_group_desc)", " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_aaat"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_gncd"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6260_gnce"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
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
  public void commDataCode01(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 " + " and   acct_type = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataCode04(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
          + " where 1 = 1 " + " and   group_code = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
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
          + " where 1 = 1 " + " and   entry_mode = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mode_desc");
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
          + " where 1 = 1 " + " and   mcc_code = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code") });

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
          + " where 1 = 1 " + " and   mcht_group_id = ? ";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code") });

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcht_group_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commActiveType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3"};
    String[] txt = {"紅利", "基金", "贈品"};
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
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "MS950");

    if (fi == -1)
      return;

    String sysUploadType = wp.itemStr("sys_upload_type");
    String sysUploadAlias = wp.itemStr("sys_upload_alias");

    if (sysUploadAlias.equals("aaa1")) {
      // if has pre check procudure, write in here
    }
    mktm02.Mktm6260Func func = new mktm02.Mktm6260Func(wp);

    if (sysUploadAlias.equals("aaa1"))
      func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
    if (sysUploadAlias.equals("aaa3"))
      func.dbDeleteD2Aaa3("MKT_BN_DATA_T");

    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    tranSeqStr = comr.getSeqno("MKT_MODSEQ");

    String tmpStr = "";
    int llOk = 0, llCnt = 0, llErr = 0;
    int lineCnt = 0;
    while (true) {
      tmpStr = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y"))
        break;
      lineCnt++;
      if (sysUploadAlias.equals("aaa1")) {
        if (lineCnt <= 0)
          continue;
        if (tmpStr.length() < 2)
          continue;
      }
      if (sysUploadAlias.equals("aaa3")) {
        if (lineCnt <= 0)
          continue;
        if (tmpStr.length() < 2)
          continue;
      }

      llCnt++;

      for (int inti = 0; inti < 10; inti++)
        logMsg[inti] = "";
      logMsg[10] = String.format("%02d", lineCnt);

      if (sysUploadAlias.equals("aaa1"))
        if (checkUploadfileAaa1(tmpStr) != 0)
          continue;
      if (sysUploadAlias.equals("aaa3"))
        if (checkUploadfileAaa3(tmpStr) != 0)
          continue;

      if (errorCnt == 0) {
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
  int checkUploadfileAaa1(String string) throws Exception {
    mktm02.Mktm6260Func func = new mktm02.Mktm6260Func(wp);

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
    uploadFileDat[2] = "MKT_MCHT_PARM";
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
    mktm02.Mktm6260Func func = new mktm02.Mktm6260Func(wp);

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
    uploadFileDat[2] = "MKT_MCHT_PARM";
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

    return 0;
  }

  // ************************************************************************
  // ************************************************************************
  public void checkButtonOff() throws Exception {
    if (wp.colStr("new_hldr_sel").length() == 0)
      wp.colSet("new_hldr_sel", "0");

    if (wp.colStr("new_hldr_sel").equals("0")) {
      buttonOff("btngncd_disable");
    } else {
      wp.colSet("btngncd_disable", "");
    }

    if (wp.colStr("new_group_cond").length() == 0)
      wp.colSet("new_group_cond", "0");

    if (wp.colStr("new_group_cond").equals("0")) {
      buttonOff("btngncd_disable");
    } else {
      wp.colSet("btngncd_disable", "");
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

    if (wp.colStr("mcc_code_sel").length() == 0)
      wp.colSet("mcc_code_sel", "0");

    if (wp.colStr("mcc_code_sel").equals("0")) {
      buttonOff("btnmccd_disable");
    } else {
      wp.colSet("btnmccd_disable", "");
    }

    if (wp.colStr("pos_entry_sel").length() == 0)
      wp.colSet("pos_entry_sel", "0");

    if (wp.colStr("pos_entry_sel").equals("0")) {
      buttonOff("btnposn_disable");
    } else {
      wp.colSet("btnposn_disable", "");
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
    wp.colSet("exchange_amt", "25");
    buttonOff("btngncd_disable");
    buttonOff("btngncd_disable");
    buttonOff("btnactp_disable");
    buttonOff("btngpcd_disable");
    buttonOff("btnmrcd_disable");
    buttonOff("btnaaa1_disable");
    buttonOff("btninmc_disable");
    buttonOff("btnaaat_disable");
    buttonOff("btnmccd_disable");
    buttonOff("btnposn_disable");
    return;
  }
  // ************************************************************************

} // End of class
