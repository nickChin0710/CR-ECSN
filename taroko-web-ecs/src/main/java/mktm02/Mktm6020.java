/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-08-12  V1.00.03   JustinWu  GetStr -> getStr
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *   
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            * 
* 110/11/04  V1.00.05  jiangyingdong       sql injection                   * 
***************************************************************************/
package mktm02;

import mktm02.Mktm6020Func;
import ofcapp.AppMsg;

import java.util.ArrayList;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6020 extends BaseEdit {

  private ArrayList<Object> params = new ArrayList<Object>();
  private String PROGNAME = "高階卡友參數維護處理程式108/12/12 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6020Func func = null;
  String rowid, kk2;
  String groupCode, cardType;
  String fstAprFlag = "";
  String orgTabName = "cyc_anul_gp";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_card_type"), "a.card_type", "like%")
        + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "")
        + sqlCol(wp.itemStr("ex_group_code"), "a.group_code", "like%");

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
        + "a.group_code," + "a.card_type," + "a.card_fee," + "a.sup_card_fee," + "a.crt_user,"
        + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by group_code,card_type";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commGroupCode("comm_group_code");
    commCardType("comm_card_type");


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
        + "a.group_code as group_code," + "a.card_type as card_type," + "a.card_fee,"
        + "a.sup_card_fee," + "a.mer_cond," + "a.mer_bl_flag," + "a.mer_ca_flag," + "a.mer_it_flag,"
        + "a.mer_ao_flag," + "a.mer_id_flag," + "a.mer_ot_flag," + "a.major_flag," + "a.sub_flag,"
        + "a.major_sub," + "a.a_merchant_sel," + "a.a_mcht_group_sel," + "a.cnt_cond,"
        + "a.cnt_select," + "a.month_cnt," + "a.accumlate_cnt," + "a.cnt_bl_flag,"
        + "a.cnt_ca_flag," + "a.cnt_it_flag," + "a.cnt_ao_flag," + "a.cnt_id_flag,"
        + "a.cnt_ot_flag," + "a.cnt_major_flag," + "a.cnt_sub_flag," + "a.cnt_major_sub,"
        + "a.b_mcc_code_sel," + "a.b_merchant_sel," + "a.b_mcht_group_sel," + "a.amt_cond,"
        + "a.accumlate_amt," + "a.amt_bl_flag," + "a.amt_ca_flag," + "a.amt_it_flag,"
        + "a.amt_ao_flag," + "a.amt_id_flag," + "a.amt_ot_flag," + "a.amt_major_flag,"
        + "a.amt_sub_flag," + "a.amt_major_sub," + "a.c_mcc_code_sel," + "a.c_merchant_sel,"
        + "a.c_mcht_group_sel," + "a.mcode," + "a.email_nopaper_flag," 
        + "a.miner_half_flag,a.g_cond_flag,a.g_accumlate_amt,a.h_cond_flag,a.h_accumlate_amt," 
        + "a.crt_user," + "a.crt_date," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(groupCode, "a.group_code") + sqlCol(cardType, "a.card_type");
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
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    checkButtonOff();
    groupCode = wp.colStr("group_code");
    cardType = wp.colStr("card_type");
    commfuncAudType("aud_type");
    dataReadR3R();
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " a.aud_type as aud_type, " + "a.group_code as group_code," + "a.card_type as card_type,"
        + "a.card_fee as card_fee," + "a.sup_card_fee as sup_card_fee," + "a.mer_cond as mer_cond,"
        + "a.mer_bl_flag as mer_bl_flag," + "a.mer_ca_flag as mer_ca_flag,"
        + "a.mer_it_flag as mer_it_flag," + "a.mer_ao_flag as mer_ao_flag,"
        + "a.mer_id_flag as mer_id_flag," + "a.mer_ot_flag as mer_ot_flag,"
        + "a.major_flag as major_flag," + "a.sub_flag as sub_flag," + "a.major_sub as major_sub,"
        + "a.a_merchant_sel as a_merchant_sel," + "a.a_mcht_group_sel as a_mcht_group_sel,"
        + "a.cnt_cond as cnt_cond," + "a.cnt_select as cnt_select," + "a.month_cnt as month_cnt,"
        + "a.accumlate_cnt as accumlate_cnt," + "a.cnt_bl_flag as cnt_bl_flag,"
        + "a.cnt_ca_flag as cnt_ca_flag," + "a.cnt_it_flag as cnt_it_flag,"
        + "a.cnt_ao_flag as cnt_ao_flag," + "a.cnt_id_flag as cnt_id_flag,"
        + "a.cnt_ot_flag as cnt_ot_flag," + "a.cnt_major_flag as cnt_major_flag,"
        + "a.cnt_sub_flag as cnt_sub_flag," + "a.cnt_major_sub as cnt_major_sub,"
        + "a.b_mcc_code_sel as b_mcc_code_sel," + "a.b_merchant_sel as b_merchant_sel,"
        + "a.b_mcht_group_sel as b_mcht_group_sel," + "a.amt_cond as amt_cond,"
        + "a.accumlate_amt as accumlate_amt," + "a.amt_bl_flag as amt_bl_flag,"
        + "a.amt_ca_flag as amt_ca_flag," + "a.amt_it_flag as amt_it_flag,"
        + "a.amt_ao_flag as amt_ao_flag," + "a.amt_id_flag as amt_id_flag,"
        + "a.amt_ot_flag as amt_ot_flag," + "a.amt_major_flag as amt_major_flag,"
        + "a.amt_sub_flag as amt_sub_flag," + "a.amt_major_sub as amt_major_sub,"
        + "a.c_mcc_code_sel as c_mcc_code_sel," + "a.c_merchant_sel as c_merchant_sel,"
        + "a.c_mcht_group_sel as c_mcht_group_sel," + "a.mcode as mcode,"
        + "a.email_nopaper_flag as email_nopaper_flag," + "a.crt_user as crt_user,"
        + "a.miner_half_flag as miner_half_flag,"
        + "a.g_cond_flag as g_cond_flag, a.g_accumlate_amt as g_accumlate_amt,"
        + "a.h_cond_flag as h_cond_flag, a.h_accumlate_amt as h_accumlate_amt,"
        + "a.crt_date as crt_date," + "a.apr_user as apr_user," + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(groupCode, "a.group_code") + sqlCol(cardType, "a.card_type");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
    checkButtonOff();
    commfuncAudType("aud_type");
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    groupCode = wp.itemStr("group_code");
    cardType = wp.itemStr("card_type");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      groupCode = wp.itemStr("group_code");
      cardType = wp.itemStr("card_type");
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
    groupCode = wp.itemStr("group_code");
    cardType = wp.itemStr("card_type");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      groupCode = wp.itemStr("group_code");
      cardType = wp.itemStr("card_type");
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

    if ((wp.itemStr("group_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
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
    wp.whereStr = "where 1=1" + " and table_name  =  'CYC_ANUL_GP' ";
    if (wp.respHtml.equals("mktm6020_aaa1"))
      wp.whereStr += " and data_type  = '1' ";
    if (wp.respHtml.equals("mktm6020_bbb2"))
      wp.whereStr += " and data_type  = '4' ";
    if (wp.respHtml.equals("mktm6020_ccc2"))
      wp.whereStr += " and data_type  = '7' ";
    String whereCnt = wp.whereStr;
//    whereCnt += " and  data_key = '" + wp.itemStr("group_code") + wp.itemStr("card_type") + "'";
    whereCnt += " and  data_key = ?";
    params.add(wp.itemStr("group_code") + wp.itemStr("card_type"));
    wp.whereStr += " and  data_key = :data_key ";
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("group_code") + wp.itemStr("card_type"));
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
    if (wp.respHtml.equals("mktm6020_aaa1"))
      commDataType2("comm_data_code2");
    if (wp.respHtml.equals("mktm6020_bbb2"))
      commDataType2("comm_data_code2");
    if (wp.respHtml.equals("mktm6020_ccc2"))
      commDataType2("comm_data_code2");
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception {
    mktm02.Mktm6020Func func = new mktm02.Mktm6020Func(wp);
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
    if (func.dbDeleteD2() < 0) {
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

    if ((wp.itemStr("group_code").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    commGroupCode("comm_group_code");
    commCardType("comm_card_type");
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
    wp.whereStr = "where 1=1" + " and table_name  =  'CYC_ANUL_GP' ";
    if (wp.respHtml.equals("mktm6020_aaa2"))
      wp.whereStr += " and data_type  = '2' ";
    if (wp.respHtml.equals("mktm6020_bbb1"))
      wp.whereStr += " and data_type  = '3' ";
    if (wp.respHtml.equals("mktm6020_bbb3"))
      wp.whereStr += " and data_type  = '5' ";
    if (wp.respHtml.equals("mktm6020_ccc1"))
      wp.whereStr += " and data_type  = '6' ";
    if (wp.respHtml.equals("mktm6020_ccc3"))
      wp.whereStr += " and data_type  = '8' ";
    String whereCnt = wp.whereStr;
//  whereCnt += " and  data_key = '" + wp.itemStr("group_code") + wp.itemStr("card_type") + "'";
  whereCnt += " and  data_key = ?";
    wp.whereStr += " and  data_key = :data_key ";
    wp.whereStr += " and  data_key = :data_key ";
    setString("data_key", wp.itemStr("group_code") + wp.itemStr("card_type"));
    wp.whereStr += " order by 4,5 ";
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
    if (wp.respHtml.equals("mktm6020_aaa2"))
      commMechtGroup("comm_data_code");
    if (wp.respHtml.equals("mktm6020_bbb1"))
      commDataCode07("comm_data_code");
    if (wp.respHtml.equals("mktm6020_bbb3"))
      commMechtGroup("comm_data_code");
    if (wp.respHtml.equals("mktm6020_ccc1"))
      commDataCode07("comm_data_code");
  }

  // ************************************************************************
  public void updateFuncU3() throws Exception {
    mktm02.Mktm6020Func func = new mktm02.Mktm6020Func(wp);
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
    if (func.dbDeleteD3() < 0) {
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

    sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));

    return ((int) sqlNum("bndataCount"));
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm02.Mktm6020Func func = new mktm02.Mktm6020Func(wp);

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
      if ((wp.respHtml.equals("mktm6020_nadd")) || (wp.respHtml.equals("mktm6020_detl"))) {
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("group_code").length() > 0) {
          wp.optionKey = wp.colStr("group_code");
          wp.initOption = "";
        }
        this.dddwList("dddw_group_code", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
        wp.initOption = "";
        wp.optionKey = "";
        if (wp.colStr("card_type").length() > 0) {
          wp.optionKey = wp.colStr("card_type");
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_card_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_card_type");
        }
        this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)",
            " where 1 = 1 ");
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_group_code").length() > 0) {
          wp.optionKey = wp.colStr("ex_group_code");
        }
        this.dddwList("dddw_group_code3", "ptr_group_code", "trim(group_code)", "trim(group_name)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_aaa1"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        this.dddwList("dddw_data_type2", "bil_auto_ica", "trim(bank_no)", "trim(ica_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_aaa2"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_bbb1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_bbb2"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        this.dddwList("dddw_data_type2", "bil_auto_ica", "trim(bank_no)", "trim(ica_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_bbb3"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_ccc1"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_data_code07", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_ccc2"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        this.dddwList("dddw_data_type2", "bil_auto_ica", "trim(bank_no)", "trim(ica_desc)",
            " where 1 = 1 ");
      }
      if ((wp.respHtml.equals("mktm6020_ccc3"))) {
        wp.initOption = "";
        wp.optionKey = "";
        this.dddwList("dddw_mcht_gp", "mkt_mcht_gp", "trim(mcht_group_id)", "trim(mcht_group_desc)",
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
  public void commGroupCode(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      params.clear();
      sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
//          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
			+ " where 1 = 1 " + " and   group_code = ?";
      params.add(wp.colStr(ii, "group_code"));
      if (wp.colStr(ii, "group_code").length() == 0)
        continue;
      sqlSelect(sql1, (String[])params.toArray(new String[params.size()]));

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commCardType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
          + " and   card_type = '" + wp.colStr(ii, "card_type") + "'";
      if (wp.colStr(ii, "card_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commDataType2(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " ica_desc as column_ica_desc " + " from bil_auto_ica " + " where 1 = 1 "
          + " and   bank_no = '" + wp.colStr(ii, "data_code2") + "'";
      if (wp.colStr(ii, "data_code2").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_ica_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commMechtGroup(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " mcht_group_desc as column_mcht_group_desc " + " from mkt_mcht_gp "
          + " where 1 = 1 " + " and   mcht_group_id = '" + wp.colStr(ii, "data_code") + "'";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcht_group_desc");
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
          + " where 1 = 1 " + " and   mcc_code = '" + wp.colStr(ii, "data_code") + "'";
      if (wp.colStr(ii, "data_code").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_mcc_remark");
      wp.colSet(ii, columnData1, columnData);
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

    mktm02.Mktm6020Func func = new mktm02.Mktm6020Func(wp);

    if (sysUploadAlias.equals("aaa1"))
      func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
    if (sysUploadAlias.equals("bbb2"))
      func.dbDeleteD2Bbb2("MKT_BN_DATA_T");
    if (sysUploadAlias.equals("ccc2"))
      func.dbDeleteD2Ccc2("MKT_BN_DATA_T");

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
      if (sysUploadAlias.equals("bbb2")) {
        if (lineCnt <= 0)
          continue;
        if (tmpStr.length() < 2)
          continue;
      }
      if (sysUploadAlias.equals("ccc2")) {
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
      if (sysUploadAlias.equals("bbb2"))
        if (checkUploadfileBbb2(tmpStr) != 0)
          continue;
      if (sysUploadAlias.equals("ccc2"))
        if (checkUploadfileCcc2(tmpStr) != 0)
          continue;

      if (errorCnt == 0) {
        if (sysUploadAlias.equals("aaa1")) {
          if (func.dbInsertI2Aaa1("MKT_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
            llOk++;
          else
            llErr++;
        }
        if (sysUploadAlias.equals("bbb2")) {
          if (func.dbInsertI2Bbb2("MKT_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
            llOk++;
          else
            llErr++;
        }
        if (sysUploadAlias.equals("ccc2")) {
          if (func.dbInsertI2Ccc2("MKT_BN_DATA_T", uploadFileCol, uploadFileDat) == 1)
            llOk++;
          else
            llErr++;
        }
      }
    }

    if (errorCnt > 0) {
      if (sysUploadAlias.equals("aaa1"))
        func.dbDeleteD2Aaa1("MKT_BN_DATA_T");
      if (sysUploadAlias.equals("bbb2"))
        func.dbDeleteD2Bbb2("MKT_BN_DATA_T");
      if (sysUploadAlias.equals("ccc2"))
        func.dbDeleteD2Ccc2("MKT_BN_DATA_T");
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
  int checkUploadfileAaa1(String tmpStr) throws Exception {
    mktm02.Mktm6020Func func = new mktm02.Mktm6020Func(wp);

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
    uploadFileCol[5] = "crt_date";
    uploadFileCol[6] = "crt_user";

    // ==== insert table content default =====
    uploadFileDat[2] = "CYC_ANUL_GP";
    uploadFileDat[3] = wp.itemStr("GROUP_CODE") + wp.itemStr("CARD_TYPE");
    uploadFileDat[4] = "1";
    uploadFileDat[5] = wp.sysDate;
    uploadFileDat[6] = wp.loginUser;

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);

    return 0;
  }

  // ************************************************************************
  int checkUploadfileBbb2(String tmpStr) throws Exception {
    mktm02.Mktm6020Func func = new mktm02.Mktm6020Func(wp);

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
    uploadFileCol[5] = "crt_date";
    uploadFileCol[6] = "crt_user";

    // ==== insert table content default =====
    uploadFileDat[2] = "CYC_ANUL_GP";
    uploadFileDat[3] = wp.itemStr("GROUP_CODE") + wp.itemStr("CARD_TYPE");
    uploadFileDat[4] = "4";
    uploadFileDat[5] = wp.sysDate;
    uploadFileDat[6] = wp.loginUser;

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
      if (uploadFileDat[inti].length() != 0)
        okFlag = 1;
    }
    if (okFlag == 0)
      return (1);

    return 0;
  }

  // ************************************************************************
  int checkUploadfileCcc2(String tmpStr) throws Exception {
    mktm02.Mktm6020Func func = new mktm02.Mktm6020Func(wp);

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
    uploadFileCol[5] = "crt_date";
    uploadFileCol[6] = "crt_user";

    // ==== insert table content default =====
    uploadFileDat[2] = "CYC_ANUL_GP";
    uploadFileDat[3] = wp.itemStr("GROUP_CODE") + wp.itemStr("CARD_TYPE");
    uploadFileDat[4] = "7";
    uploadFileDat[5] = wp.sysDate;
    uploadFileDat[6] = wp.loginUser;

    int okFlag = 0;
    int errFlag = 0;
    int[] begPos = {1};

    for (int inti = 0; inti < 2; inti++) {
      uploadFileDat[inti] = comm.getStr(tmpStr, inti + 1, ",");
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
    if (wp.colStr("a_merchant_sel").length() == 0)
      wp.colSet("a_merchant_sel", "0");

    if (wp.colStr("a_merchant_sel").equals("0")) {
      buttonOff("btnaaa1_disable");
      buttonOff("uplaaa1_disable");
    } else {
      wp.colSet("btnaaa1_disable", "");
      wp.colSet("uplaaa1_disable", "");
    }

    if (wp.colStr("a_mcht_group_sel").length() == 0)
      wp.colSet("a_mcht_group_sel", "0");

    if (wp.colStr("a_mcht_group_sel").equals("0")) {
      buttonOff("btnaaa2_disable");
    } else {
      wp.colSet("btnaaa2_disable", "");
    }

    if (wp.colStr("b_mcc_code_sel").length() == 0)
      wp.colSet("b_mcc_code_sel", "0");

    if (wp.colStr("b_mcc_code_sel").equals("0")) {
      buttonOff("btnbbb1_disable");
    } else {
      wp.colSet("btnbbb1_disable", "");
    }

    if (wp.colStr("b_merchant_sel").length() == 0)
      wp.colSet("b_merchant_sel", "0");

    if (wp.colStr("b_merchant_sel").equals("0")) {
      buttonOff("btnbbb2_disable");
      buttonOff("uplbbb2_disable");
    } else {
      wp.colSet("btnbbb2_disable", "");
      wp.colSet("uplbbb2_disable", "");
    }

    if (wp.colStr("b_mcht_group_sel").length() == 0)
      wp.colSet("b_mcht_group_sel", "0");

    if (wp.colStr("b_mcht_group_sel").equals("0")) {
      buttonOff("btnbbb3_disable");
    } else {
      wp.colSet("btnbbb3_disable", "");
    }

    if (wp.colStr("c_mcc_code_sel").length() == 0)
      wp.colSet("c_mcc_code_sel", "0");

    if (wp.colStr("c_mcc_code_sel").equals("0")) {
      buttonOff("btnccc1_disable");
    } else {
      wp.colSet("btnccc1_disable", "");
    }

    if (wp.colStr("c_merchant_sel").length() == 0)
      wp.colSet("c_merchant_sel", "0");

    if (wp.colStr("c_merchant_sel").equals("0")) {
      buttonOff("btnccc2_disable");
      buttonOff("uplccc2_disable");
    } else {
      wp.colSet("btnccc2_disable", "");
      wp.colSet("uplccc2_disable", "");
    }

    if (wp.colStr("c_mcht_group_sel").length() == 0)
      wp.colSet("c_mcht_group_sel", "0");

    if (wp.colStr("c_mcht_group_sel").equals("0")) {
      buttonOff("btnccc3_disable");
    } else {
      wp.colSet("btnccc3_disable", "");
    }

    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
      buttonOff("uplaaa1_disable");
      buttonOff("uplbbb2_disable");
      buttonOff("uplccc2_disable");
    } else {
    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnaaa1_disable");
    buttonOff("btnaaa2_disable");
    buttonOff("btnbbb1_disable");
    buttonOff("btnbbb2_disable");
    buttonOff("btnbbb3_disable");
    buttonOff("btnccc1_disable");
    buttonOff("btnccc2_disable");
    buttonOff("btnccc3_disable");
    return;
  }
  // ************************************************************************

} // End of class
