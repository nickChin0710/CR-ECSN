/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-18  V1.00.00  ryan       program initial                            *
* 108-06-27  V1.00.01  Andy       Update mch_no PUP add                      *
* 108-12-13  V1.00.02  Andy       Fix bug                                    *
* 109-04-24  V1.00.03  shiyuqi       updated for project coding standard     * 
* 109-11-19  V1.00.04  Ryan       移除畫面部分欄位與邏輯                                                                  *    
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *   
* 111-05-27  V1.00.05   Ryan       移除重複getWhereStr()      
* 111-08-11  V1.00.05  machao     特店代號之後增加”分期期數” 欄位                    *   
******************************************************************************/

package bilm01;

import java.util.regex.Pattern;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Bilm0710 extends BaseEdit {
  String kk1 = "", lsType = "", lsDesc = "", lsCode1 = "", lsCode2 = "" ,kk2= "";
  String actionCodeKk1 = "", paymetRateKk1 = "";
  String actionCodeKk2 = "";

  Pattern pattern = Pattern.compile("[0-9]*");

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
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
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S1")) {
      /* 查詢 */
      strAction = "S1";
      selectDate();
      // flagDate();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 查詢 paymet_rate */
      strAction = "S2";
      selectPaymetRate();
    } else if (eqIgno(wp.buttonCode, "S1U")) {
      /* 存檔 */
      strAction = "S1U";
      lsType = "01";
      lsDesc = "指定繳款評等";
      updatePaymetRate();
    } else if (eqIgno(wp.buttonCode, "S2U")) {
      /* 存檔 */
      strAction = "S2U";
      lsType = "02";
      lsDesc = "指定違約評等";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S3U")) {
      /* 存檔 */
      strAction = "S3U";
      lsType = "03";
      lsDesc = "指定覆審結果";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S4U")) {
      /* 存檔 */
      strAction = "S4U";
      lsType = "04";
      lsDesc = "指定風險族群";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S5U")) {
      /* 存檔 */
      strAction = "S5U";
      lsType = "05";
      lsDesc = "排除MccCode";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S6U")) {
      /* 存檔 */
      strAction = "S6U";
      lsType = "06";
      lsDesc = "不排除之凍結碼";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S7U")) {
      /* 存檔 */
      strAction = "S7U";
      lsType = "07";
      lsDesc = "不排除之特指戶";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S8U")) {
      /* 存檔 */
      strAction = "S8U";
      lsType = "08";
      lsDesc = "指定覆審結果2";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S9U")) {
      /* 存檔 */
      strAction = "S9U";
      lsType = "09";
      lsDesc = "指定風險族群2";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "S10U")) {
      /* 存檔 */
      strAction = "S10U";
      lsType = "10";
      lsDesc = "排除特店代號";
      updateDate();
    } else if (eqIgno(wp.buttonCode, "UPLOAD1")) {
      /* mcht匯入 */
      strAction = "UPLOAD1";
      lsType = "10";
      lsDesc = "排除特店代號";
      procUploadFile();
    }else if (eqIgno(wp.buttonCode, "TOTTERM")) {
        /* 查詢 */
        strAction = "TOTTERM";
//        changeTotterm();
      } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    }

    dddwSelect();
    initButton();
  }


  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (!empty(wp.itemStr("ex_merchant"))) {
      // wp.whereStr += " and mcht_no = :ex_merchant ";
      // setString("ex_merchant", wp.item_ss("ex_merchant"));
      wp.whereStr += sqlCol(wp.itemStr("ex_merchant"), "mcht_no", "like%");
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    String lsDate1 = wp.itemStr("ex_crt_date1");
    String lsDate2 = wp.itemStr("ex_crt_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return;
    }

    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " mcht_no" + ", product_no" + ", action_desc" + ", effc_date_b" + ", effc_date_e";

    wp.daoTable = "bil_auto_parm";
    wp.whereOrder = " order by mcht_no";
//    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    kk1 = wp.itemStr("kk_mcht_no");
    kk2 = wp.itemStr("product_no");
  
    if (empty(kk1)) {
      kk1 = itemKk("data_k1");
    }
    if (empty(kk2)) {
      kk2 =  itemKk("data_k2");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", mcht_no" + ", mod_user" + ", mod_time"
        + ", mod_pgm" + ", action_desc" + ", effc_date_b" + ", effc_date_e"
        + ", destination_amt_flag" + ", destination_amt" + ", payment_rate_flag" + ", payment_rate"
        + ", rc_rate_flag"
        + ", rc_rate" + ", credit_amt_rate" + ", mcc_code_flag" + ", over_credit_amt_flag"
        + ", block_reason_flag" + ", spec_status_flag" 
        + ", product_no"
        + ", (select product_name from bil_prod where product_no = bil_auto_parm.product_no fetch first 1 rows only) as product_name "
        + ", mcht_flag";

    wp.daoTable = "bil_auto_parm";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  mcht_no = :mcht_no ";
    wp.whereStr += " and  product_no = :product_no ";
    setString("mcht_no", kk1);
    setString("product_no", kk2);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, mcht_no=" + kk1);
      wp.colSet("mcht_no", "");
    }
  }

  @Override
  public void saveFunc() throws Exception {
    String lsSql = "";
    if (strAction.equals("A") && empty(wp.itemStr("kk_mcht_no"))) {
      alertErr("特店代號不可空白!!");
      return;
    }
    // 20191213 add bil_merchant check
    if (strAction.equals("A")) {
      lsSql = "select count(*)ct from bil_merchant where mcht_no =:ex_mcht_no ";
      setString("ex_mcht_no", wp.itemStr("kk_mcht_no"));
      sqlSelect(lsSql);
      if (sqlNum("ct") <= 0) {
        alertErr("無此特店代號!!");
        return;
      }

    }
    // -check approve-
//    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
//      return;
//    }
    Bilm0710Func func = new Bilm0710Func(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {

      wp.initOption = "--";
      this.dddwList("dddw_merchant1", "bil_merchant", "mcht_no", "mcht_chi_name",
          "where 1=1 and mcht_status = '1' and mcht_type = '1' ");

      wp.initOption = "--";
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("mcht_no");
      } else {
        wp.optionKey = kk1;
      }
      this.dddwList("dddw_merchant2", "bil_merchant", "mcht_no", "mcht_chi_name",
          "where 1=1 and mcht_status = '1' and mcht_type = '1' ");
      
      
//      wp.optionKey = "--";
//      if (wp.respHtml.indexOf("_detl") > 0) {
//        this.dddwList("dddw_productno_in", "bil_prod", "product_no",
//            "product_no||'_'||product_name||'('||mcht_no||')'", " where 1=1 " );
//      }
//		if (wp.respHtml.indexOf("_detl") > 0) {
//			String lsMchtNoParm = wp.colStr("mcht_no");
//			if(empty(lsMchtNoParm)) {
//				lsMchtNoParm = wp.colStr("kk_mcht_no");
//			}
//			wp.optionKey = wp.colStr("product_no");
//			StringBuffer dddwWhere = new StringBuffer();
//			dddwWhere.append(" where 1=1 and mcht_no = '");
//			dddwWhere.append(lsMchtNoParm);
//			dddwWhere.append("' order by product_no ");
//			if (!empty(lsMchtNoParm)) {
//				this.dddwList("dddw_productno_in", "bil_prod", "product_no",
//						"product_no||'_'||product_name||'('||mcht_no||')'", dddwWhere.toString());
//			}
//		}
    } catch (Exception ex) {
    }
  }

  public void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      lsCode1 = wp.colStr(ii, "data_code");
      lsCode2 = wp.colStr(ii, "data_code2");
      if (!lsCode2.equals("Y")) {
        lsCode2 = "N";
      }
      switch (lsCode1) {
        case "NULL":
          wp.colSet("ex_null", lsCode2);
          break;
        case "0A":
          wp.colSet("ex_0a", lsCode2);
          break;
        case "0B":
          wp.colSet("ex_0b", lsCode2);
          break;
        case "0C":
          wp.colSet("ex_0c", lsCode2);
          break;
        case "0D":
          wp.colSet("ex_0d", lsCode2);
          break;
        case "0E":
          wp.colSet("ex_0e", lsCode2);
          break;
        case "01":
          wp.colSet("ex_01", lsCode2);
          break;
        default:
          if (pattern.matcher(lsCode1).matches()) {
            if (this.toNum(lsCode1) >= 2) {
              wp.colSet("ex_02", lsCode2);
            }
          }
      }
    }
  }

  public void selectPaymetRate() throws Exception {
    this.selectNoLimit();

    actionCodeKk1 = wp.itemStr("action_code");
    if (empty(actionCodeKk1))
      actionCodeKk1 = itemKk("data_k1");
    wp.colSet("action_code", actionCodeKk1);

    actionCodeKk2 = wp.itemStr("action_code2");
    if (empty(actionCodeKk2))
      actionCodeKk2 = itemKk("data_k2");
    
    String productNo = itemKk("data_k3");
    wp.colSet("action_code2", actionCodeKk2);
    wp.selectSQL = "hex(rowid) as rowid1 " + ", mcht_no" + ", data_type" + ", data_code"
        + ", data_code2" + ", apr_flag" + ", type_desc";
    wp.daoTable = "bil_auto_parm_data";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  mcht_no = :mcht_no and type_desc = :ls_desc and product_no = :product_no ";
    setString("mcht_no", actionCodeKk1);
    setString("ls_desc", actionCodeKk2);
    setString("product_no", productNo);
    // pageSelect();
    pageQuery();
    listWkdata();
    wp.setListCount(1);
    wp.notFound = "";
    wp.colSet("product_no", productNo);

  }

  public void selectDate() throws Exception {
    this.selectNoLimit();

    actionCodeKk1 = wp.itemStr("action_code");
    if (empty(actionCodeKk1))
      actionCodeKk1 = itemKk("data_k1");
    wp.colSet("action_code", actionCodeKk1);

    actionCodeKk2 = wp.itemStr("action_code2");
    if (empty(actionCodeKk2))
      actionCodeKk2 = itemKk("data_k2");
    wp.colSet("action_code2", actionCodeKk2);
    
    String productNo = itemKk("data_k3");
    
    wp.selectSQL = "hex(rowid) as rowid1 " + ", mcht_no" + ", data_type" + ", data_code"
        + ", data_code2" + ", apr_flag" + ", type_desc";
    wp.daoTable = "bil_auto_parm_data";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  mcht_no = :mcht_no and type_desc = :ls_desc and product_no = :product_no ";
    setString("mcht_no", actionCodeKk1);
    setString("ls_desc", actionCodeKk2);
    setString("product_no", productNo);
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
    wp.colSet("product_no", productNo);


  }

  public void updatePaymetRate() {
    Bilm0710Func func = new Bilm0710Func(wp);
    int llOK = 0, llErr = 0;
    String dataCode2 = "";
    String[] aaDataCode = {"NULL", "0A", "0B", "0C", "0D", "0E", "01", "02"};
    String[] aaDataCode2 =
        {wp.itemStr("ex_null"), wp.itemStr("ex_0A"), wp.itemStr("ex_0B"), wp.itemStr("ex_0C"),
            wp.itemStr("ex_0D"), wp.itemStr("ex_0E"), wp.itemStr("ex_01"), wp.itemStr("ex_02")};

    wp.listCount[0] = aaDataCode.length;

    // -delete no-approve-
    if (func.dbDelete3() < 0) {
      alertErr(func.getMsg());
      return;
    }

    for (int ll = 0; ll < aaDataCode.length; ll++) {
      dataCode2 = aaDataCode2[ll];
      if (!dataCode2.equals("Y")) {
        dataCode2 = "N";
      }
      func.varsSet("data_code", aaDataCode[ll]);
      func.varsSet("data_code2", dataCode2);
      func.varsSet("data_type", lsType);
      func.varsSet("type_desc", lsDesc);
      func.varsSet("product_no", wp.itemStr("product_no"));
      // -insert-
      if (func.dbInsert3() == 1) {
        llOK++;
      } else {
        llErr++;
      }
    }
    // 有失敗rollback，無失敗commit
    sqlCommit(llOK > 0 ? 1 : 0);
    alertMsg("資料存檔處理完成; OK = " + llOK + ", ERR = " + llErr);
    // SAVE後 SELECT

  }

  public void updateDate() throws Exception {
    Bilm0710Func func = new Bilm0710Func(wp);

    int llOk = 0, llErr = 0;
    String[] aaDataCode = wp.itemBuff("data_code");
    String[] aaRowid1 = wp.itemBuff("rowid1");
    String[] aaOpt = wp.itemBuff("opt");

    wp.listCount[0] = aaDataCode.length;
    // wp.col_set("IND_NUM", "" + aa_data_code.length);
    // -insert-

    for (int ll = 0; ll < aaDataCode.length; ll++) {

      func.varsSet("data_code", aaDataCode[ll]);
      func.varsSet("rowid1", aaRowid1[ll]);
      func.varsSet("data_type", lsType);
      func.varsSet("type_desc", lsDesc);
      func.varsSet("product_no", wp.itemStr("product_no"));

      // -delete no-approve-
      if (func.dbDelete2() < 0) {
        alertErr(func.getMsg());
        return;
      }

      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {

        continue;
      }

      if (func.dbInsert2() == 1) {
        llOk++;
      } else {
        llErr++;
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }

    alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
    // SAVE後 SELECT
    selectDate();

  }

  public void procUploadFile() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }
    fileDataImp1();

  }

  void fileDataImp1() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "UTF-8"); // 決定上傳檔內碼
    // int fi = tf.openInputText(inputFile,"MS950");
    if (fi == -1) {
      return;
    }

    Bilm0710Func func = new Bilm0710Func(wp);

    String file = "";
    int llOk = 0, llCnt = 0, llErr = 0;

    func.varsSet("data_mcht", wp.itemStr("action_code"));
    func.varsSet("data_type", lsType);
    func.varsSet("type_desc", lsDesc);
    // -delete no-approve-
    if (func.dbDelete4() < 0) {
      alertErr(func.getMsg());
      sqlCommit(0);
      return;
    }


    while (true) {
      file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y"))
        break;

      if (file.length() < 2)
        continue;

      // wp.alertMesg = "<script language='javascript'> alert('"+ss+"')</script>";

      llCnt++;


      func.varsSet("data_code", file);


      // -option-ON-
      if (func.dbInsert2() == 1) {
        llOk++;
        sqlCommit(1);
      } else {
        llErr++;
        sqlCommit(0);
      }

    }

    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功 = " + llOk + ", 重複 = " + llErr);

    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    selectDate();

    return;
  }

	public void processAjaxOption() throws Exception {
		wp.varRows = 1000;
		setSelectLimit(0);
		if (wp.itemEq("action_code", "AJAX1")) {
			itemChangMcht();
		} else {
			String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
					+ " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
					+ " order by mcht_no ";
			if (wp.respHtml.indexOf("_detl") > 0) {
				setString("mcht_no", wp.getValue("kk_mcht_no", 0) + "%");
			} else {
				setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
			}
			sqlSelect(lsSql);

			for (int i = 0; i < sqlRowNum; i++) {
				wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
				wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
			}
			wp.addJSON("json_action_code", "AJAX");
		}
		return;
	}
	
	void itemChangMcht() throws Exception {
		String lsMerchantNo = wp.itemStr("data_mcht_no");
		if(empty(lsMerchantNo))
			return;
		String lsMchtNoParm = lsMerchantNo;
		String dddwWhere = "", option = "";
		if (!empty(lsMchtNoParm))
			dddwWhere = " and mcht_no = :ls_mcht_no_parm ";
		String selectBilProd = "select product_no "
				+ " ,product_no||'_'||product_name||'('||mcht_no||')' as product_name " + " from bil_prod "
				+ " where 1=1 " + dddwWhere + " order by product_no ";
		setString("ls_mcht_no_parm", lsMchtNoParm);
		sqlSelect(selectBilProd);
		if (sqlRowNum <= 0) {
			return;
		}
		option += "<option value=\"\">--</option>";
		for (int ii = 0; ii < sqlRowNum; ii++) {
			option += "<option value=\"" + sqlStr(ii, "product_no") + "\" ${tot_term-" + sqlStr(ii, "product_no")
					+ "} >" + sqlStr(ii, "product_name") + "</option>";
		}
		wp.addJSON("dddw_tot_term_option", option);
		wp.addJSON("json_action_code", "AJAX1");
	}
  
//  void changeTotterm() throws Exception {
//	    String lsMerchantNo = wp.itemStr("kk_mcht_no");
//
//	    String lsx02 = wp.itemStr("product_no");
//
//	    String sql =
//	        "select count(*) as li_cnt from bil_prod  where product_no  = lpad(:ls_x02 , 2 ,'0') and mcht_no = :ls_merchant_no";
//	    setString("ls_x02", lsx02);
//	    setString("ls_merchant_no", lsMerchantNo);
//
//	    sqlSelect(sql);
//
//	    if (this.toNum(sqlStr("li_cnt")) < 1) {
//	      alertErr("期數 錯誤~ !!");
//	      return;
//	    }
//  }
}
