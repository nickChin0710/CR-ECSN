/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-01-02  V1.00.00  Zuwei      Create                                     *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-10-12  V1.00.02  Tanwei       添加繳費稅交易相關字段                                                                       *
*                                                                            *
******************************************************************************/

package ptrm01;

import java.text.SimpleDateFormat;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

/**
 * 交易平台參數維護
 * 
 * @author zsu4
 *
 */
public class Ptrm0162 extends BaseEdit {

  /**
   * 程式主入口
   */
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示新增畫面 */
      strAction = "new";
      /* 清空頁面控制信息 */
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢列表 */
      strAction = "Q";
      queryFunc();
      // } else if (eq_igno(wp.buttonCode, "R")) {
      // // -資料讀取-
      // is_action = "R";
      // dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
        /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清空畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      String action = wp.getValue("action");
      String custMchtNo = wp.getValue("cust_mcht_no");
      if ("query_cust_mcht_no".equals(action)) {
        strAction = "AJAX";
        queryCustMchtNo(custMchtNo);
      }
    }

    dddwSelect();
    initButton();
  }

  /**
   * 查詢列表
   */
  @Override
  public void queryFunc() throws Exception {
	whereStr();
	// -page control-
	wp.queryWhere = wp.whereStr;
	wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " platform_kind" + ", platform_desc" + ", bill_type" + ", cust_mcht_no";

    wp.daoTable = "bil_platform";
    wp.whereOrder = " order by platform_kind";
    whereStr();
    
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  /**
   * 構造列表查詢條件
   * 
   * @return
   * @throws Exception
   */
  private void whereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    String sPlatformKind = wp.itemStr("s_platform_kind");
    String sCustMchtNo = wp.itemStr("s_cust_mcht_no");

    if (empty(sPlatformKind) == false) {
      wp.whereStr += " and platform_kind = :platform_kind ";
      setString("platform_kind", sPlatformKind);
    }
    if (empty(sCustMchtNo) == false) {
      wp.whereStr += " and cust_mcht_no = :cust_mcht_no ";
      setString("cust_mcht_no", sCustMchtNo);
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  /**
   * 讀取記錄詳情
   */
  @Override
  public void dataRead() throws Exception {
    String platformKind = wp.itemStr("platform_kind");
    if (empty(platformKind)) {
      alertErr("交易平台種類不能爲空");
      return;
    }

    // rowid為時間戳，標注是新增還是查詢出的記錄，非表字段
    wp.selectSQL = "hex(rowid) as rowid, platform_kind" + ", platform_tab" + ", platform_desc"
        + ", bill_type" + ", cust_mcht_no" + ", fee_attribute_flag" + ", fee_attribute1"
        + ", merchant_no1_flag" + ", merchant_no1_data_type" + ", fee_attribute2"
        + ", merchant_no2_flag" + ", merchant_no2_data_type" + ", mcc_code_flag" + ", mcc_code"
        + ", terminal_id_flag" + ", terminal_id_data_type" + ", publ_item_flag" + ", publ_item"
        + ", crt_date" + ", crt_user" + ", apr_date" + ", apr_user" + ", mod_user" + ", mod_time"
        + ", mod_pgm";
    wp.daoTable = "bil_platform";
    wp.whereStr = "where 1=1 and platform_kind = :platform_kind";
    setString("platform_kind", platformKind);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, platform_kind=" + platformKind);
      return;
    }
    String modTime = wp.getValue("mod_time");
    if (!empty(modTime)) {
      modTime = new SimpleDateFormat("yyyyMMdd")
          .format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(modTime));
      wp.setValue("mod_time", modTime);
    }

    // 查詢bil_platform_data表數據
    String tab = wp.getValue("platform_tab");
    //queryPlatformDataDetail(platformKind, tab);
  }

  /**
   * 保存數據
   */
  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    Ptrm0162Func func = new Ptrm0162Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    // 設置保存後是否重新獲取記錄詳情並展示
    this.addRetrieve = true;
    this.updateRetrieve = true;
  }

  /**
   * 初始化頁面按鈕(新增/刪除/修改/清空)
   */
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  /**
   * 查詢下拉框所需數據
   */
  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("bill_type");
        this.dddwList("dddw_bill_type", "ptr_billtype", "bill_type", "",
            "where 1=1 group by bill_type order by bill_type");
      } else {
        // wp.initOption="--";
        wp.optionKey = wp.colStr("s_platform_kind");
        this.dddwList("dddw_platform_kind", "bil_platform", "platform_kind", "",
            "where 1=1 order by platform_kind");

        // wp.initOption="--";
        wp.optionKey = wp.colStr("s_cust_mcht_no");
        this.dddwList("dddw_cust_mcht_no", "bil_platform", "cust_mcht_no", "",
            "where 1=1 group by cust_mcht_no order by cust_mcht_no");

      }
    } catch (Exception ex) {
    }
  }

  /**
   * 客制特店代碼搜索-處理Ajax請求
   * 
   * @throws Exception
   */
  private void queryCustMchtNo(String strNo) throws Exception {
    wp.varRows = 100;
    setSelectLimit(0);
    String lsSql = "select mcht_no, mcht_no||'_'||mcht_chi_name as mcht_desc from bil_merchant "
        + "where mcht_status = '1' and mcht_no like :mcht_no order by mcht_no ";
    setString("mcht_no", strNo + "%");

    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "mcht_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }

  /**
   * 查詢bil_platform_data數據詳情
   * 
   * @param platformKind
   * @param tab
   */
  private void queryPlatformDataDetail(String platformKind, String tab) {
    wp.selectSQL = "data_type" + ", data_fit_flag" + ", data_value";
    wp.daoTable = "bil_platform_data";
    wp.whereStr = "where 1=1 and platform_kind = :platform_kind";
    setString("platform_kind", platformKind);

    pageSelect();
    wp.notFound = "N";

    String mh1DataValue = "";
    String mh2DataValue = "";
    String terDataValue = "";
    String taxDataValue = "";
    for (int i = 0; i < sqlRowNum; i++) {
      String dataType = wp.getValue("data_type", i);
      if (dataType.equals("MH1")) {
        wp.setValue("mh1_data_fit_flag", wp.getValue("data_fit_flag", i));
        // wp.setValue("mh1_data_value", wp.getValue("data_value", i));
        mh1DataValue += "、" + wp.getValue("data_value", i);
      }
      if (dataType.equals("MH2")) {
        wp.setValue("mh2_data_fit_flag", wp.getValue("data_fit_flag", i));
        // wp.setValue("mh2_data_value", wp.getValue("data_value", i));
        mh2DataValue += "、" + wp.getValue("data_value", i);
      }
      if (dataType.equals("TER")) {
        if (tab.equals("F")) {
          wp.setValue("ter_data_fit_flag", wp.getValue("data_fit_flag"));
          // wp.setValue("ter_data_value", wp.getValue("data_value", i));
          terDataValue += "、" + wp.getValue("data_value", i);
        } else if (tab.equals("I")) {
          wp.setValue("ins_data_fit_flag", wp.getValue("data_fit_flag"));
          wp.setValue("ins_data_value", wp.getValue("data_value", i));
        }
      }
      
      if (dataType.equals("TAX")) {
        if (tab.equals("F")) {
          wp.setValue("tax_data_fit_flag", wp.getValue("data_fit_flag"));
          taxDataValue += "、" + wp.getValue("data_value", i);
        } 
      }
    }
    if (mh1DataValue.length() > 0) {
      wp.setValue("mh1_data_value", mh1DataValue.substring(1));
    }
    if (mh2DataValue.length() > 0) {
      wp.setValue("mh2_data_value", mh2DataValue.substring(1));
    }
    if (terDataValue.length() > 0) {
      wp.setValue("ter_data_value", terDataValue.substring(1));
    }
    if (taxDataValue.length() > 0) {
      wp.setValue("tax_data_value", taxDataValue.substring(1));
    }
  }
}
