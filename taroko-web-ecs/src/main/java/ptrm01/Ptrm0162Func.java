/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-01-03  V1.00.00  Zuwei       program initial                           *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
* 109-10-12  V1.00.02  Tanwei       添加繳費稅交易相關字段                                                                       *
*                                                                            *
******************************************************************************/

package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0162Func extends FuncEdit {

  private String platformKind;
  private String platformTab;
  private String platformDesc;
  private String billType;
  private String custMchtNo;
  private String feeAttributeFlag;
  private String feeAttribute1;
  private String merchantNo1Flag;
  private String merchantNo1DataType;
  private String feeAttribute2;
  private String merchantNo2Flag;
  private String merchantNo2DataType;
  private String mccCodeFlag;
  private String mccCode;
  private String terminalIdFlag;
  private String reimbCodeFlag;
  private String terminalIdDataType;
  private String publItemFlag;
  private String publItem;

  public Ptrm0162Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    return 0;
  }

  @Override
  public int dataSelect() {
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.isAdd() || this.isDelete()) {
      platformKind = wp.itemStr("platform_kind");
      // 檢查新增資料是否重複
      String lsSql = "select count(1) as tot_cnt from bil_platform where platform_kind = ?";
      Object[] param = new Object[] {platformKind};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        if (this.isAdd()) {
          errmsg("資料已存在，無法新增");
          return;
        }
      } else if (this.isDelete()) {
        errmsg("資料不存在，無法刪除");
        return;
      }
    }

    if (this.isAdd() || this.isUpdate()) {
      if (platformTab.equals("F")) {
        if (!empty(feeAttributeFlag) && "Y".equals(feeAttributeFlag) && empty(feeAttribute1)
            && empty(feeAttribute2)) {
          errmsg("交易處理費屬性值必須至少輸入一個");
          return;
        }
        if ("Y".equals(merchantNo1Flag) && empty(wp.getValue("mh1_data_value"))) {
          errmsg("特店代號前二碼不能爲空");
          return;
        }
        if ("Y".equals(merchantNo2Flag) && empty(wp.getValue("mh2_data_value"))) {
          errmsg("特店代號前二碼不能爲空");
          return;
        }

        if ("Y".equals(terminalIdFlag)) {
          String terDataValue = wp.getValue("ter_data_value");
          if (empty(terDataValue)) {
            errmsg("端末機代號不能爲空");
            return;
          }
        }
        
        if ("Y".equals(reimbCodeFlag)) {
          String taxDataValue = wp.getValue("tax_data_value");
          if (empty(taxDataValue)) {
            errmsg("繳費稅種類代號不能爲空");
            return;
          }
        }

        if ("Y".equals(mccCodeFlag) && empty(mccCode)) {
          errmsg("MCC Code不能爲空");
          return;
        }
      }
      if (platformTab.equals("P") && empty(publItem)) {
        errmsg("委托代繳項目不能爲空");
        return;
      }
      if (platformTab.equals("I") && empty(wp.getValue("ins_data_value"))) {
        errmsg("端末機代號部分比對不能爲空");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    fetchInputData();
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into bil_platform (" + "platform_kind" + ", platform_tab" + ", platform_desc"
        + ", bill_type" + ", cust_mcht_no" + ", fee_attribute_flag" + ", fee_attribute1"
        + ", merchant_no1_flag" + ", merchant_no1_data_type" + ", fee_attribute2"
        + ", merchant_no2_flag" + ", merchant_no2_data_type" + ", mcc_code_flag" + ", mcc_code"
        + ", terminal_id_flag" + ", terminal_id_data_type" + ", publ_item_flag" + ", publ_item"
        + ", crt_date" + ", crt_user" + ", apr_date" + ", apr_user" + ", mod_user" + ", mod_time"
        + ", mod_pgm" + " ) values (" + " ?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,to_char(sysdate,'yyyymmdd'),?" + ",to_char(sysdate,'yyyymmdd'),?,?,?,? "
        + " )";
    // -set ?value-
    Object[] param = new Object[] {platformKind, platformTab, platformDesc, billType, custMchtNo,
        feeAttributeFlag, feeAttribute1, merchantNo1Flag, merchantNo1DataType, feeAttribute2,
        merchantNo2Flag, merchantNo2DataType, mccCodeFlag, mccCode, terminalIdFlag,
        terminalIdDataType, publItemFlag, publItem, wp.loginUser, wp.itemStr("approval_user") // 主管覆核人員
        , "" // 變更人員
        , null 
        , wp.itemStr("mod_pgm") // 異動程式
    };
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    } else {
      rc = insertPlatformData();
      if (rc <= 0) {
        return rc;
      }
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    fetchInputData();
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update bil_platform set " + "  platform_tab = ?" + ", platform_desc = ?"
        + ", bill_type = ?" + ", cust_mcht_no = ?" + ", fee_attribute_flag = ?"
        + ", fee_attribute1 = ?" + ", merchant_no1_flag = ?" + ", merchant_no1_data_type = ?"
        + ", fee_attribute2 = ?" + ", merchant_no2_flag = ?" + ", merchant_no2_data_type = ?"
        + ", mcc_code_flag = ?" + ", mcc_code = ?" + ", terminal_id_flag = ?"
        + ", terminal_id_data_type = ?" + ", publ_item_flag = ?" + ", publ_item = ?"
        + ", apr_date = to_char(sysdate,'yyyymmdd')" + ", apr_user = ?" + ", mod_user = ?"
        + ", mod_time = sysdate" + ", mod_pgm = ?" + " where platform_kind = ?";
    // -set ?value-
    Object[] param = new Object[] {platformTab, platformDesc, billType, custMchtNo,
        feeAttributeFlag, feeAttribute1, merchantNo1Flag, merchantNo1DataType, feeAttribute2,
        merchantNo2Flag, merchantNo2DataType, mccCodeFlag, mccCode, terminalIdFlag,
        terminalIdDataType, publItemFlag, publItem, wp.itemStr("approval_user") // 主管覆核人員
        , wp.loginUser, wp.itemStr("mod_pgm") // 異動程式
        , platformKind};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    } else {
      this.deletePlatformData(platformKind);
      rc = insertPlatformData();
      if (rc <= 0) {
        return rc;
      }
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    String platformKind = wp.itemStr("platform_kind");
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    // 先刪除此platformKind下的所有bil_platform_data數據
    rc = deletePlatformData(platformKind);
    if (rc < 0) {
      return rc;
    }

    strSql = "delete bil_platform " + "where platform_kind = ?";
    Object[] param = new Object[] {platformKind};
    rc = sqlExec(strSql, param);
    if (rc <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  /**
   * 讀取輸入的數據，只有新增和修改使用
   */
  private void fetchInputData() {
    platformKind = wp.itemStr("platform_kind");
    platformTab = wp.itemStr("platform_tab");
    platformDesc = empty(wp.itemStr("platform_desc")) ? "" : wp.itemStr("platform_desc");
    billType = wp.itemStr("bill_type");
    custMchtNo = wp.itemStr("cust_mcht_no");
    feeAttributeFlag =
        empty(wp.itemStr("fee_attribute_flag")) ? "N" : wp.itemStr("fee_attribute_flag");
    feeAttribute1 = empty(wp.itemStr("fee_attribute1")) ? "" : wp.itemStr("fee_attribute1");
    merchantNo1Flag =
        empty(wp.itemStr("merchant_no1_flag")) ? "N" : wp.itemStr("merchant_no1_flag");
    merchantNo1DataType = merchantNo1Flag.equals("Y") ? "MH1" : "";
    feeAttribute2 = empty(wp.itemStr("fee_attribute2")) ? "" : wp.itemStr("fee_attribute2");
    merchantNo2Flag =
        empty(wp.itemStr("merchant_no2_flag")) ? "N" : wp.itemStr("merchant_no2_flag");
    merchantNo2DataType = merchantNo2Flag.equals("Y") ? "MH2" : "";
    mccCodeFlag = empty(wp.itemStr("mcc_code_flag")) ? "N" : wp.itemStr("mcc_code_flag");
    mccCode = empty(wp.itemStr("mcc_code")) ? "" : wp.itemStr("mcc_code");
    terminalIdFlag = empty(wp.itemStr("terminal_id_flag")) ? "N" : wp.itemStr("terminal_id_flag");
    terminalIdFlag = platformTab.equals("I") ? "Y" : terminalIdFlag;
    reimbCodeFlag = empty(wp.itemStr("reimb_code_flag")) ? "N" : wp.itemStr("reimb_code_flag");
    terminalIdDataType = terminalIdFlag.equals("Y") ? "TER" : "";
    publItemFlag = platformTab.equals("P") ? "Y" : "N";
    publItem = empty(wp.itemStr("publ_item")) ? "" : wp.itemStr("publ_item");

    if (platformTab.equals("P") || platformTab.equals("I")) {
      feeAttributeFlag = "N";
      feeAttribute1 = "";
      merchantNo1Flag = "N";
      merchantNo1DataType = "";
      feeAttribute2 = "";
      merchantNo2Flag = "N";
      merchantNo2DataType = "";
      mccCodeFlag = "N";
      mccCode = "";

      if (platformTab.equals("P")) {
        terminalIdFlag = "N";
        terminalIdDataType = "";
      } else {
        publItemFlag = "N";
        publItem = "";
      }
    } else {
      publItemFlag = "N";
      publItem = "";
      if (!"Y".equals(feeAttributeFlag)) {
        feeAttribute1 = "";
        merchantNo1Flag = "N";
        merchantNo1DataType = "";
        feeAttribute2 = "";
        merchantNo2Flag = "N";
        merchantNo2DataType = "";
      }
      // 如果屬性值爲空，後面的特店代號也置爲空
      if (empty(feeAttribute1)) {
        merchantNo1Flag = "N";
        merchantNo1DataType = "";
      }
      if (empty(feeAttribute2)) {
        merchantNo2Flag = "N";
        merchantNo2DataType = "";
      }
    }
  }

  private int insertPlatformData() {
    String dataType = "";
    String typeDesc = "";
    String dataFitFlag = "N";
    String dataValue = "";
    if (platformTab.equals("F") && merchantNo1Flag.equals("Y")) {
      dataType = "MH1";
      if (empty(wp.itemStr("mh1_data_fit_flag")) || "Y".equals(wp.itemStr("mh1_data_fit_flag"))) {
        dataFitFlag = "Y";
        typeDesc = "指定特店代號前二碼";
      } else {
        typeDesc = "排除特店代號前二碼";
      }
      dataValue = wp.itemStr("mh1_data_value");
      if (!empty(dataValue)) {
        String[] values = dataValue.split("、");
        for (String value : values) {
          rc = insertPlatformData(platformKind, dataType, dataFitFlag, value, typeDesc);
          if (sqlRowNum <= 0) {
            return rc;
          }
        }
      }
    }

    if (platformTab.equals("F") && merchantNo2Flag.equals("Y")) {
      dataType = "MH2";
      if (empty(wp.itemStr("mh2_data_fit_flag")) || "Y".equals(wp.itemStr("mh2_data_fit_flag"))) {
        dataFitFlag = "Y";
        typeDesc = "指定特店代號前二碼";
      } else {
        typeDesc = "排除特店代號前二碼";
      }
      dataValue = wp.itemStr("mh2_data_value");
      if (!empty(dataValue)) {
        String[] values = dataValue.split("、");
        for (String value : values) {
          rc = insertPlatformData(platformKind, dataType, dataFitFlag, value, typeDesc);
          if (sqlRowNum <= 0) {
            return rc;
          }
        }
      }
    }

    if (platformTab.equals("F") && terminalIdFlag.equals("Y")) {
      dataType = "TER";
      dataFitFlag = "Y";
      typeDesc = "指定端末機代碼(8碼)";
      dataValue = wp.itemStr("ter_data_value");
      if (!empty(dataValue)) {
        String[] values = dataValue.split("、");
        for (String value : values) {
          rc = insertPlatformData(platformKind, dataType, dataFitFlag, value, typeDesc);
          if (sqlRowNum <= 0) {
            return rc;
          }
        }
      }
    }
    
    if (platformTab.equals("F") && reimbCodeFlag.equals("Y")) {
      dataType = "TAX";
      dataFitFlag = "Y";
      typeDesc = "指定繳費稅種類代碼(2碼)";
      dataValue = wp.itemStr("tax_data_value");
      if (!empty(dataValue)) {
        String[] values = dataValue.split("、");
        for (String value : values) {
          rc = insertPlatformData(platformKind, dataType, dataFitFlag, value, typeDesc);
          if (sqlRowNum <= 0) {
            return rc;
          }
        }
      }
    }

    if (platformTab.equals("I")) {
      dataType = "TER";
      dataFitFlag = "Y";
      typeDesc = "部份比對端末機代碼(16碼onus)";
      dataValue = wp.itemStr("ins_data_value");
      if (!empty(dataValue)) {
        rc = insertPlatformData(platformKind, dataType, dataFitFlag, dataValue, typeDesc);
        if (sqlRowNum <= 0) {
          return rc;
        }
      }
    }

    return rc;
  }

  /**
   * 新增bil_platform_data
   * 
   * @param platformKind
   * @param dataType
   * @param dataFitFlag
   * @param dataValue
   */
  private int insertPlatformData(String platformKind, String dataType, String dataFitFlag,
      String dataValue, String typeDesc) {
    strSql = "insert into bil_platform_data (" + "platform_kind" + ", data_type" + ", data_fit_flag"
        + ", data_value" + ", type_desc" + ", crt_date" + ", crt_user" + ", apr_date" + ", apr_user"
        + ", mod_pgm" + " ) values ("
        + " ?,?,?,?,?,to_char(sysdate,'yyyymmdd'),?,to_char(sysdate,'yyyymmdd'),?,?" + " )";
    dataFitFlag = empty(dataFitFlag) ? "N" : dataFitFlag;
    Object[] param = new Object[] {platformKind, dataType, dataFitFlag, dataValue, typeDesc,
        wp.loginUser, wp.itemStr("approval_user") // 主管覆核人員
        , wp.itemStr("mod_pgm") // 異動程式
    };
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  /**
   * 刪除bil_platform_data數據
   * 
   * @param platformKind
   * @param dataType
   * @return
   */
  private int deletePlatformData(String platformKind) {
    Object[] param = new Object[] {platformKind};
    strSql = "delete bil_platform_data " + "where platform_kind = ?";

    rc = sqlExec(strSql, param);
    if (rc <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
