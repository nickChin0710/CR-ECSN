/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-18  V1.00.00  David FU   program initial                            *
* 106-07-06  V1.00.01  Andy Liu   program update                             *
* 107-05-15  V1.00.02  Andy Liu   Update UI                                  *
* 109-01-16  V1.00.03  Ru Chen    bug fix                                    *
* 109-04-28  V1.00.04 YangFang  updated for project coding standard        *
* 109-12-07  V1.00.05  Justin        comment 帳單期別 in dddwSelect
* 109-12-11   V1.00.06  Justin        add AJAX and revise the desc of zip in dddwSelect
* 110-10-08  V1.00.07   Justin    add zip2                                   *
* 110-10-21  V1.00.08   Justin    modify the method to get group_code, card_type, unit_code, and  source_code
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0055 extends BaseEdit {
  String mExBatchno = "";
  String mExApplyNo = "";
  String mExApplyId = "";
  String mExCorpNo = "";
  String mExRecno = "";

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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
    	switch (wp.getValue("idCode")) {
		case "1":
			getCardType();
			break;
		case "2":
			getUnitCode();
			break;
		case "3":
			getAddrByAJAX();
		case "4":
			getRegBankNo();
			break;
		default:
			break;
		}
    }

    dddwSelect();
    initButton();
  }


@Override
  public void initPage() {
    String lsSql = "";
    // 批號
    wp.colSet("batchno", getSysDate().substring(2) + "99");
    // 序號
    lsSql = "select nvl(max(recno)+1,'1') as recno " + "from crd_emap_tmp "
        + "where batchno = :ls_sysDate||'99'";
    setString("ls_sysDate", getSysDate().substring(2));
    sqlSelect(lsSql);
    wp.colSet("recno", sqlNum("recno"));
    // 預借現金註記
    wp.colSet("send_pwd_flag", "N");
    // 是否申請子卡額度
    wp.colSet("son_card_flag", "N");
    // 年費優惠
    wp.colSet("fee_code_i", "N");
    // 同意分期註記
    wp.colSet("inst_flag", "N");
    // 拒絕行銷註記
    wp.colSet("market_agree_base", "N");
    // EDM寄送註記
    wp.colSet("e_news", "N");
    // 鍵檔日期/人員
    wp.colSet("crt_date", getSysDate());
    wp.colSet("crt_user", wp.loginUser);
  }

  private void getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_batchno")) == false) {
      wp.whereStr += " and  batchno = :batchno ";
      setString("batchno", wp.itemStr("ex_batchno"));
    }
    if (empty(wp.itemStr("ex_apply_no")) == false) {
      wp.whereStr += " and  apply_no = :apply_no ";
      setString("apply_no", wp.itemStr("ex_apply_no"));
    }
    if (empty(wp.itemStr("ex_apply_id")) == false) {
      wp.whereStr += " and  apply_id = :apply_id ";
      setString("apply_id", wp.itemStr("ex_apply_id"));
    }
    if (empty(wp.itemStr("ex_corp_no")) == false) {
      wp.whereStr += " and  corp_no = :corp_no ";
      setString("corp_no", wp.itemStr("ex_corp_no"));
    }
    wp.whereStr += " and  right(batchno,2) = '99' ";
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    
    StringBuffer sb = new StringBuffer();
    sb.append(" batchno");
    sb.append(", recno");
    sb.append(", apply_no");
    sb.append(", group_code");
    sb.append(", card_type");
    sb.append(", unit_code");
    sb.append(", source_code");
    sb.append(", online_mark");
    sb.append(", corp_no");
    sb.append(", corp_act_flag"      );
    sb.append(", stmt_cycle");
    sb.append(", apply_id");
    sb.append(", sex");
    sb.append(", birthday");
    sb.append(", chi_name");
    sb.append(", eng_name");
    sb.append(", nation");
    sb.append(", other_cntry_code");
    sb.append(", resident_no_expire_date");
    sb.append(", passport_no");
    sb.append(", passport_date");
    sb.append(", ur_flag");
    sb.append(", marriage");
    sb.append(", education");
    sb.append(", business_code");
    sb.append(", graduation_elementarty");
    sb.append(", cellar_phone");
    sb.append(", e_mail_addr");
    sb.append(", home_area_code1");
    sb.append(", home_tel_no1");
    sb.append(", home_tel_ext1");
    sb.append(", office_area_code1");
    sb.append(", office_tel_no1");
    sb.append(", office_tel_ext1");
    sb.append(", resident_zip");
    sb.append(", resident_addr1");
    sb.append(", resident_addr2");
    sb.append(", resident_addr3");
    sb.append(", resident_addr4");
    sb.append(", resident_addr5");
    sb.append(", mail_zip");
    sb.append(", mail_addr1");
    sb.append(", mail_addr2");
    sb.append(", mail_addr3");
    sb.append(", mail_addr4");
    sb.append(", mail_addr5");
    sb.append(", company_zip");
    sb.append(", company_addr1");
    sb.append(", company_addr2");
    sb.append(", company_addr3");
    sb.append(", company_addr4");
    sb.append(", company_addr5");
    sb.append(", bill_apply_flag");
    sb.append(", branch");
    sb.append(", mail_type");
    sb.append(", company_name");
    sb.append(", job_position");
    sb.append(", service_year");
    sb.append(", salary");
    sb.append(", spouse_id_no");
    sb.append(", spouse_birthday");
    sb.append(", spouse_name");
    sb.append(", act_no_l");
    sb.append(", act_no_l_ind");
    sb.append(", autopay_acct_bank");
    sb.append(", credit_lmt");
    sb.append(", revolve_int_rate" );
    sb.append(", lpad(CAST( revolve_int_rate_year*100 AS decimal(4,0)), 4, '0') as revolve_int_rate_year ");
    sb.append(", sms_amt");
    sb.append(", send_pwd_flag");
    sb.append(", son_card_flag");
    sb.append(", indiv_crd_lmt");
    sb.append(", fee_code");
    sb.append(", fee_code_i");
    sb.append(", inst_flag");
    sb.append(", roadside_assist_apply");
    sb.append(", market_agree_base");
    sb.append(", e_news");
    sb.append(", introduce_emp_no");
    sb.append(", introduce_id");
    sb.append(", reg_bank_no");
    sb.append(", emboss_4th_data");
    sb.append(", promote_emp_no");
    sb.append(", promote_dept");
    sb.append(", credit_level_new");
    sb.append(", jcic_score");
    sb.append(", stat_send_internet");
    sb.append(", crt_date, crt_user ");
    sb.append(", apr_date, apr_user ");
    sb.append(", mod_time, mod_user, mod_pgm, mod_seqno");

    wp.selectSQL = sb.toString();

    wp.daoTable = "crd_emap_tmp";
    // wp.whereOrder=" order by card_item";

    getWhereStr();

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
    mExBatchno = itemKk("data_k1");
    mExApplyNo = itemKk("data_k2");
    mExApplyId = itemKk("data_k3");
    mExCorpNo = itemKk("data_k4");
    mExRecno = itemKk("data_k5");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
	StringBuffer sb = new StringBuffer();
	sb.append("hex(rowid) as rowid")
	.append(", batchno ")
	.append(", recno")
	.append(", apply_no")
	.append(", group_code")
	.append(", card_type")
	.append(", unit_code")
	.append(", source_code")
	.append(", online_mark")
	.append(", corp_no")
	.append(", corp_act_flag")
	.append(", stmt_cycle")
	.append(", apply_id")
	.append(", sex")
	.append(", birthday")
	.append(", chi_name")
	.append(", eng_name")
	.append(", nation")
	.append(", other_cntry_code")
	.append(", resident_no_expire_date")
	.append(", passport_no")
	.append(", passport_date")
	.append(", ur_flag")
	.append(", marriage")
	.append(", education")
	.append(", business_code")
	.append(", graduation_elementarty")
	.append(", cellar_phone")
	.append(", e_mail_addr")
	.append(", home_area_code1")
	.append(", home_tel_no1")
	.append(", home_tel_ext1")
	.append(", office_area_code1")
	.append(", office_tel_no1")
	.append(", office_tel_ext1")
	.append(", resident_zip")
	.append(", resident_addr1")
	.append(", resident_addr2")
	.append(", resident_addr3")
	.append(", resident_addr4")
	.append(", resident_addr5")
	.append(", mail_zip")
	.append(", mail_addr1")
	.append(", mail_addr2")
	.append(", mail_addr3")
	.append(", mail_addr4")
	.append(", mail_addr5")
	.append(", company_zip")
	.append(", company_addr1")
	.append(", company_addr2")
	.append(", company_addr3")
	.append(", company_addr4")
	.append(", company_addr5")
	.append(", bill_apply_flag")
	.append(", branch")
	.append(", mail_type")
	.append(", company_name")
	.append(", job_position")
	.append(", substring(service_year,1,2) as service_year")
	.append(", substring(service_year,3) as service_month")
	.append(", salary")
	.append(", spouse_id_no")
	.append(", spouse_birthday")
	.append(", spouse_name")
	.append(", act_no_l")
	.append(", act_no_l_ind")
	.append(", autopay_acct_bank")
	.append(", credit_lmt")
	.append(", revolve_int_rate" )
	.append(", lpad(CAST( revolve_int_rate_year*100 AS decimal(4,0)), 4, '0') as revolve_int_rate_year ")
	.append(", sms_amt")
	.append(", send_pwd_flag")
	.append(", son_card_flag")
	.append(", indiv_crd_lmt")
	.append(", fee_code")
	.append(", fee_code_i")
	.append(", inst_flag")
	.append(", roadside_assist_apply")
	.append(", market_agree_base")
	.append(", e_news")
	.append(", introduce_emp_no")
	.append(", introduce_id")
	.append(", reg_bank_no ")
	.append(", emboss_4th_data")
	.append(", promote_emp_no")
	.append(", promote_dept")
	.append(", credit_level_new")
	.append(", jcic_score")
	.append(", stat_send_internet")
	.append(", crt_date, crt_user ")
	.append(", apr_date, apr_user ")
	.append(", mod_time, mod_user, mod_pgm, mod_seqno");
	
    wp.selectSQL = sb.toString();
    wp.daoTable = "crd_emap_tmp";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  batchno = :batchno" + " and apply_no = :apply_no"
        + " and apply_id = :apply_id" + " and corp_no = :corp_no" + " and recno = :recno";
    setString("batchno", mExBatchno);
    setString("apply_no", mExApplyNo);
    setString("apply_id", mExApplyId);
    setString("corp_no", mExCorpNo);
    setString("recno", mExRecno);

    pageSelect();
    if (sqlNotFind()) {
      alertErr(
          "查無資料, batchno = " + mExBatchno + " and apply_no = " + mExApplyNo + " and apply_id = "
              + mExApplyId + " and corp_no = " + mExCorpNo + " and recno = " + mExRecno);
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] residentZipArr = commString.splitZipCode(wp.colStr("resident_zip"));
    wp.colSet("resident_zip", residentZipArr[0]);
    wp.colSet("resident_zip2", residentZipArr[1]);
    String[] mailZipArr = commString.splitZipCode(wp.colStr("mail_zip"));
    wp.colSet("mail_zip", mailZipArr[0]);
    wp.colSet("mail_zip2", mailZipArr[1]);
    String[] companyZipArr = commString.splitZipCode(wp.colStr("company_zip"));
    wp.colSet("company_zip", companyZipArr[0]);
    wp.colSet("company_zip2", companyZipArr[1]);

    // 修改日期/人員
    wp.colSet("mod_time", getSysDate());
    wp.colSet("mod_user", wp.loginUser);

    // 推廣人員ID
    if ("".equals(wp.colStr("introduce_emp_no"))) {
      wp.colSet("introduce_emp_no", wp.colStr("introduce_id"));
    }
  }

  @Override
  public void saveFunc() throws Exception {
    Crdm0055Func func = new Crdm0055Func(wp);

    if (ofValidation() != 1) {
      rc = -1;
      sqlCommit(rc);
      return;
    }

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
    // 修改刪除按鈕
    if (!"".equals(wp.colStr("apr_date"))) {
      wp.colSet("btnUpdate_disable", "disabled");
//      wp.colSet("btnDelete_disable", "disabled");
    }
  }

  @Override
  public void dddwSelect() {
    try {
    	if (eqIgno(wp.respHtml, "crdm0055_detl") && eqIgno(wp.buttonCode, "AJAX") == false) {
    		  // 團體代號
    		  String table = "ptr_group_code a, PTR_PROD_TYPE b, PTR_ACCT_TYPE c";
    		  String where = "where a.GROUP_CODE  = b.GROUP_CODE  AND b.ACCT_TYPE = c.ACCT_TYPE AND c.CARD_INDICATOR = '2' ";
    		  String groupCode = wp.colStr("group_code");
    		  wp.optionKey = groupCode;
    	      this.dddwList("dddw_group_code", table, "a.group_code", where);
    	      // 卡種
    	      String cardType = wp.colStr("card_type");
    	      if(isEmpty(cardType) == false) {
//    	    	  this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name", "where 1=1 ");
    	    	  wp.optionKey = cardType;
    	    	  if (isEmpty(groupCode) == false) {
    	    		  where = "where group_code = :groupCode";
          		      setString("groupCode", groupCode);
				  }else {
					  where = "";
				  }
      		      this.dddwList("dddw_card_type", "ptr_group_card", "distinct card_type", "card_type||'_'||name",  where);
      		      
    	      }
    	      // 認同集團碼
    	      String unitCode = wp.colStr("unit_code");
    	      if(isEmpty(unitCode) == false) {
//    	    	  this.dddwList("dddw_unit_code", "crd_item_unit", "unit_code", "where 1=1 order by unit_code");
				  wp.optionKey = unitCode;
				  if (isEmpty(cardType) == false && isEmpty(groupCode) == false) {
					  where = "where card_type = :cardType AND group_code = :groupCode ";
					  setString("cardType", cardType);
					  setString("groupCode", groupCode);
				  }else {
					  where = "";
				  }
				  this.dddwList("dddw_unit_code", "PTR_GROUP_CARD_DTL", "unit_code", where);
    	      }
    	      // 帳單期別
    	      wp.optionKey = wp.colStr("stmt_cycle");
    	      this.dddwList("dddw_stmt_cycle", "ptr_workday", "stmt_cycle", "where 1=1 ");
    	      // 教育程度
    	      wp.optionKey = wp.colStr("education");
    	      this.dddwList("dddw_education", "crd_message", "msg_value", "msg",
    	          "where msg_type='EDUCATION' ");
    	      // 行業別
    	      wp.optionKey = wp.colStr("business_code");
    	      this.dddwList("dddw_business_code", "crd_message", "msg_value", "msg",
    	          "where msg_type='BUS_CODE' ");
    	      // 戶籍ZIP
    	      wp.optionKey = wp.colStr("resident_zip");
    	      this.dddwList("dddw_resident_zip", "ptr_zipcode", "zip_code", " zip_code||' '||zip_city||' '||zip_town" ,"where 1=1 ");
    	      // 居住ZIP
    	      wp.optionKey = wp.colStr("mail_zip");
    	      this.dddwList("dddw_mail_zip", "ptr_zipcode", "zip_code", " zip_code||' '||zip_city||' '||zip_town", "where 1=1 ");
    	      // 公司ZIP
    	      wp.optionKey = wp.colStr("company_zip");
    	      this.dddwList("dddw_company_zip", "ptr_zipcode", "zip_code", " zip_code||' '||zip_city||' '||zip_town" , "where 1=1 ");
    	      // 寄送分行
    	      wp.optionKey = wp.colStr("branch");
    	      this.dddwList("dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    	      // 推廣人員/進件分行
//    	      wp.optionKey = wp.colStr("reg_bank_no");
//    	      this.dddwList("dddw_reg_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
    	      // 信評等級
    	      wp.optionKey = wp.colStr("credit_level_new");
    	      this.dddwList("dddw_credit_level_new", "ptr_rcrate", "distinct credit_rating", "where 1=1 ");
    	      // 國籍代碼
    	      wp.initOption = "--";
    	      wp.optionKey = wp.colStr(0, "other_cntry_code");
    	      dddwList("d_dddw_other_cntry_code", "CCA_COUNTRY", "COUNTRY_CODE", "COUNTRY_REMARK", "where 1=1 ");
		}
      } catch (Exception ex) {
    }
  }

  int ofValidation() {
    String sql = "select corp_no from crd_corp where corp_no = :corp_no";
    setString("corp_no", wp.itemStr("corp_no"));
    sqlSelect(sql);
    if (sqlRowNum <= 0) {
      alertErr("法人統一編號不存在");
      return -1;
    }

    if ("".equals(wp.itemStr("cellar_phone")) && "".equals(wp.itemStr("home_tel_no1"))) {
      alertErr("行動電話、住家電話擇一輸入");
      return -1;
    }

    if ("4".equals(wp.itemStr("mail_type")) && "".equals(wp.itemStr("branch"))) {
      alertErr("寄件別為分行，寄送分行不可為空白");
      return -1;
    }

    if (!"".equals(wp.itemStr("act_no_l")) && "".equals(wp.itemStr("act_no_l_ind"))) {
      alertErr("自動扣款帳號有值，自動扣款註記不可為空白");
      return -1;
    }

    if (!"".equals(wp.itemStr("act_no_l")) && "".equals(wp.itemStr("autopay_acct_bank"))) {
      alertErr("自動扣款帳號有值，ACH委扣銀行不可為空白");
      return -1;
    }

    return 1;
  }
  
  public int getCardType() throws Exception { 
	    String groupCode = wp.itemStr("groupCode");
	    
	    String where = "where group_code = :groupCode";
	    setString("groupCode", groupCode);
	    
	    wp.initOption = "--";
	    this.dddwList("dddw_card_type2", "ptr_group_card", "distinct card_type", "card_type||'_'||name",  where);
	    
	    wp.addJSON("dddw_card_type2", wp.colStr("dddw_card_type2"));
	    wp.addJSON("idCode", "1");

	    return 1;
  }


  public int getUnitCode() throws Exception {
    String cardType = wp.itemStr("cardType");
    String groupCode = wp.itemStr("groupCode");
    
    String where =  "where card_type = :cardType AND group_code = :groupCode ";
    setString("cardType", cardType);
    setString("groupCode", groupCode);

    wp.initOption = "--";
    this.dddwList("dddw_unit_code2", "PTR_GROUP_CARD_DTL", "unit_code", where);
    
    wp.addJSON("dddw_unit_code2", wp.colStr("dddw_unit_code2"));
    wp.addJSON("idCode", "2");

    return 1;
  }
  
	public void getAddrByAJAX() throws Exception {
		selectAddrByZipCode(wp.itemStr("ax_zip_code"));
		wp.addJSON("changeTarget", wp.itemStr("changeTarget"));
		wp.addJSON("idCode", "3");
		if (rc != 1) {
			wp.addJSON("ajax_addr1", "");
			wp.addJSON("ajax_addr2", "");
			return;
		}
		wp.addJSON("ajax_addr1", sqlStr("ajax_addr1"));
		wp.addJSON("ajax_addr2", sqlStr("ajax_addr2"));
		wp.addJSON("ajax_zip_code", wp.itemStr("ax_zip_code"));

	}
	
	public void getRegBankNo() throws Exception {
		wp.addJSON("idCode", "4");
		if(wp.itemEmpty("ajax_corp_no")) {
			wp.addJSON("json_reg_bank_no", "");
			return;
		}
		String sql1 = " select reg_bank_no from act_acno where acno_flag ='2' and corp_p_seqno = (select corp_p_seqno from crd_corp where corp_no = ?) "; 
		sqlSelect(sql1, new Object[] { wp.itemStr("ajax_corp_no") });
		if (rc != 1) {
			wp.addJSON("json_reg_bank_no", "");
			return;
		}
		wp.addJSON("json_reg_bank_no", sqlStr("reg_bank_no"));
	}

	void selectAddrByZipCode(String zipCode) {
		String sql1 = " select " 
	            + " zip_city as ajax_addr1 , " 
				+ " zip_town as ajax_addr2 " 
	            + " from ptr_zipcode "
				+ " where zip_code = ? ";

		sqlSelect(sql1, new Object[] { zipCode });

		if (sqlRowNum <= 0) {
			alertErr2("郵遞區號不存在:" + zipCode);
			return;
		}

	}
}
