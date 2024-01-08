/** 
 * 2019-0613:    JH    p_xxx >>acno_pxxx
 * 2019-1118:    Alex  publ_comp_chag_flag > publ_comp_senior_manager
 * 2019-1120:    Alex  變色修改
 * 2019-1203:    Alex  add father_name,mother_name,risk_level,risk_level_date 
 * 2019-1211:    Alex  bug fix
 * 109-04-19     shiyuqi  updated for project coding standard 
 * 109-05-29  V1.00.07  shiyuqi       新增欄位        
 * 109-06-09  V1.00.02  shiyuqi      修改程式名稱          
 * 109-06-11  V1.00.03  tanwei       驗證配偶身份證號                                                                           *、
 * 109-07-17  V1.00.04  shiyuqi      rename tableName &FiledName  *
 * 109-08-21  V1.00.05  JustinWu  add apr_YN and apr_flag into check condition, 修改新增資料修改前後時的mod_date, mod_time
 * 109-09-09  V1.00.10  JustinWu     主檔apr_user=approveUser, mod_user=modifyUser, and update_user=modifyUser
 * 109-10-27  V1.00.11   JustinWu   add N if market_agree_base is null
 * 109-10-28  V1.00.12   JustinWu   remove the columns that have not displayed in the web
 * 109-12-09  V1.00.13   JustinWu   revise JCIC -> updateJCIC
 * 109-12-10   V1.00.14  JustinWu   確認帳單地址註記。若為公司地址，公司地址欄位不能為空
 * 109-12-16   V1.00.15  JustinWu   add check act_acno and act_acno_online
 * 109-12-31   V1.00.16   shiyuqi       修改无意义命名  
 * 110-01-06   V1.00.17  JustinWu   updated for XSS
 * 110-02-26   V1.00.18  JustinWu   fix a bug of checking not approved data
 * 110-08-20   V1.00.19  JustinWu   show an error message when the inserted value is too large
 * 110-10-05   V1.00.20  JustinWu   add zip2
 * 111-02-24   V1.00.21  JustinWu   update C_PHONE_CHG_DATE and C_PHONE_CHG_TIME
 * 111-03-01   V1.00.22  JustinWu   if sms_prim_ch_flag is empty, sms_prim_ch_flag = Y
 * 111-03-02   V1.00.23  JustinWu   新增異動日期及時間在待覆核資訊
 * 111-03-03   V1.00.24  JustinWu   修改selectCmsChgColumnLog()條件
 * 111-09-29   V1.00.25  Sunny      修正純VD卡查詢手機最後一筆放行(異動)日期及時間。
 * 112-07-15   V1.00.26  Zuwei Su   修正method cmsp2010SetModifyData是否異動的判斷條件。
 * 112-08-13   V1.00.27  Sunny      [行員註記及所屬單位]兩欄調整為唯讀不可修改。
 * 112-10-17   V1.00.28  Ryan       updatedbcidno行先disabled
 * 112-10-19   V1.00.29  Ryan       修正insert crd_jcic_idno錯誤問題
 * 112-11-29   V1.00.30  Sunny      取消拒絕行銷拒絕(market_agree_base)的檢核並確認不可透過本功能進行異動
 * */
package cmsm01;

import busi.FuncAction;
import busi.ecs.CommBusiCrd;

public class Cmsm2010Idno extends FuncAction {

  String idPSeqno = "", lsPawd = "" ;
  String pgm  = "cmsm2010";
  boolean ibIdnoExt = false;
  
  // all types of input except checkbox and radio
 String[] aaCol = new String[] {"chi_name             ", "birthday             ",
         "resident_no          ", "other_cntry_code     ", "passport_no          ",
         "other_id             ", "office_area_code1    ", "office_tel_no1       ",
         "office_tel_ext1      ", "office_area_code2    ", "office_tel_no2       ",
         "office_tel_ext2      ", "home_area_code1      ", "home_tel_no1         ",
         "home_tel_ext1        ", "home_area_code2      ", "home_tel_no2         ",
         "home_tel_ext2        ", "company_name         ", "cellar_phone         ",
         "staff_br_no          ",
         "resident_zip         ", "resident_addr1       ", "resident_addr2       ",
         "resident_addr3       ", "resident_addr4       ", "resident_addr5       ",
         "company_zip          ", "company_addr1        ", "company_addr2        ",
         "company_addr3        ", "company_addr4        ", "company_addr5        ",
         "mail_zip             ", "mail_addr1           ", "mail_addr2           ",
         "mail_addr3           ", "mail_addr4           ", "mail_addr5           ",
         "resident_zip2        ", "company_zip2         ", "mail_zip2            ",
         "indigenous_name      ", "graduation_elementarty ",
         "job_position         ", "e_mail_addr          ", "annual_income        ",
          "passport_date        ",
         "holdin_crt_date      ", "holdin_cancel_date   ", 
         "market_agree_act     ", "spec_busi_code",
         "risk_level", "risk_level_date", "resident_country_code", "resident_no_expire_date "
//         , "voice_passwd_flag    ","pp_renew_flag         " , "asset_value          ", "crimea_area" 
         };
 
 // checkbox and radio button
 String[] checkCol = {"sms_prim_ch_flag","publ_comp_senior_manager","ur_flag",
 		"inst_flag","fee_code_i","staff_flag","credit_flag",
 		"salary_code","comm_flag","special_code","e_news","accept_mbullet",
 		"accept_dm","accept_sms","accept_call_sell","salary_holdin_flag","tsc_market_flag"
// 		 //以下欄位需覆核，但已被隱藏		
// 		,"bank_securit_flag","bank_prod_insur_flag","bank_bills_flag",
// 		"bank_life_insur_flag","bank_invest_flag","bank_asset_flag","bank_venture_flag"
//      以下欄位不允許修改
// 		,"market_agree_base" 		
 		};
  
  public int idnoOnline() {
		if (empty(wp.colStr("id_p_seqno"))) {
			errmsg("身分證流水號: 不可空白");
			return -1;
		}

    strSql = "select * , to_char(to_date(CRT_DATE || CRT_TIME, 'yyyy/mm/dd hh24:mi:ss') , 'yyyy/mm/dd hh24:mi:ss') as userModDateTime "
    		+ " from crd_idno_online " 
    		+ " where id_p_seqno =?" 
    		+ " and apr_YN ='Y' "
            + " and apr_flag <>'Y'" 
    		+ " and data_image ='2'"; // update後資料
		this.sqlSelect(strSql, new Object[] { wp.colStr("id_p_seqno") });
		if (sqlRowNum <= 0) {
			wp.showLogMessage("I", "", "查無資料(crd_idno_online)");
			return 0;
		}
		
		// split zipCode into two parts, zip_code1 and zip_code2.  
	    String[] residentZipArr = commString.splitZipCode(colStr("resident_zip"));
	    colSet("resident_zip", residentZipArr[0]);
	    colSet("resident_zip2", residentZipArr[1]);
	    String[] mailZipArr = commString.splitZipCode(colStr("mail_zip"));
	    colSet("mail_zip", mailZipArr[0]);
	    colSet("mail_zip2", mailZipArr[1]);
	    String[] companyZipArr = commString.splitZipCode(colStr("company_zip"));
	    colSet("company_zip", companyZipArr[0]);
	    colSet("company_zip2", companyZipArr[1]);
	    
		wp.colSet("userModDateTime", this.colStr("userModDateTime"));

		return checkModifyAppr();
  }

  int checkModifyAppr() {
    int liMod = 0;
    
        // all types of input except checkbox and radio
		for (int ii = 0; ii < aaCol.length; ii++) {
			String col = aaCol[ii].trim();
			if (this.colEqIgno(col, wp.colStr(col)) == true) {
				wp.colSet("mod_" + col, "");
				continue;
			}

			wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
			wp.colSet(col, colStr(col));

			liMod = 1;
		}
    
		 // checkbox and radio button
		for (int ii = 0; ii < checkCol.length; ii++) {

			String col = checkCol[ii].trim();
			if (this.colNvl(col, "N").equals(wp.colNvl(col, "N"))) {
				wp.colSet("mod_" + col, "");
				continue;
			}

			wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
			wp.colSet(col, colStr(col));
			
			liMod = 1;
		}

		wp.colSet("mod_date", colStr("crt_date"));
		wp.colSet("mod_time", colStr("crt_time"));
		wp.colSet("mod_user", colStr("crt_user"));
		wp.colSet("id_p_seqno", colStr("id_p_seqno"));
		// ddd("A:"+li_mod);
		return liMod;
		
  }

  public void cmsp2010SetModifyData() {
    dateTime();
    if (wp.colEmpty("mod_e_mail_addr") == false) {
      wp.colSet("e_mail_from_mark", "M");
      wp.colSet("e_mail_chg_date", this.sysDate);
    }
    wp.colSet("wk_dm_flag", "");
    if (wp.colEmpty("mod_e_news") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("e_new_from_mark", "M");
      wp.colSet("e_news_chg_date", sysDate);
    }
    if (wp.colEmpty("mod_accept_mbullet") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("mbullet_from_mark", "M");
      wp.colSet("mbullet_chg_date", sysDate);
    }
    if (wp.colEmpty("mod_accept_dm") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("dm_from_mark", "M");
      wp.colSet("dm_chg_date", sysDate);
    }
    if (wp.colEmpty("mod_accept_sms") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("sms_from_mark", "M");
      wp.colSet("sms_chg_date", sysDate);
    }
    if (wp.colEmpty("mod_accept_call_sell") == false) {
      wp.colSet("wk_dm_flag", "1");
      wp.colSet("call_sell_from_mark", "M");
      wp.colSet("call_sell_chg_date", sysDate);
    }
    if (wp.colEmpty("mod_tsc_market_flag") == false) {
      wp.colSet("tsc_market_date", sysDate);
    }
    /*
     * */
  }

  void insertCrdIdnoOnline() {
    busi.SqlPrepare sp = new busi.SqlPrepare();
    getSysDate();
    
    // -before image-
    sp.sql2Insert("crd_idno_online");
    sp.ppstr("crt_date",sysDate);
    sp.ppstr("crt_time",sysTime);
//  //因覆核時，會用crt_date及crt_time來檢查data_image前後資料是否有異動，
//    所以如果抓取db時間而且沒有同時做新增，可能會造成時間差，導致系統誤認資料有異動
//    sp.ppymd("crt_date");
//    sp.pptime("crt_time");
    sp.ppstr("crt_user", modUser);
    sp.ppstr("mod_deptno", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("apr_yn", "Y");
    sp.ppstr("apr_flag", "N");
    sp.ppstr("data_image", "1");
    sp.ppstr("id_p_seqno", idPSeqno);
    sp.ppstr("chi_name", colStr("chi_name"));
    sp.ppstr("birthday", colStr("birthday"));
    sp.ppstr("resident_no", colStr("resident_no"));
    sp.ppstr("other_cntry_code", colStr("other_cntry_code"));
    sp.ppstr("passport_no", colStr("passport_no"));
    sp.ppstr("other_id", colStr("other_id"));
    sp.ppstr("office_area_code1", colStr("office_area_code1"));
    sp.ppstr("office_tel_no1", colStr("office_tel_no1"));
    sp.ppstr("office_tel_ext1", colStr("office_tel_ext1"));
    sp.ppstr("office_area_code2", colStr("office_area_code2"));
    sp.ppstr("office_tel_no2", colStr("office_tel_no2"));
    sp.ppstr("office_tel_ext2", colStr("office_tel_ext2"));
    sp.ppstr("home_area_code1", colStr("home_area_code1"));
    sp.ppstr("home_tel_no1", colStr("home_tel_no1"));
    sp.ppstr("home_tel_ext1", colStr("home_tel_ext1"));
    sp.ppstr("home_area_code2", colStr("home_area_code2"));
    sp.ppstr("home_tel_no2", colStr("home_tel_no2"));
    sp.ppstr("home_tel_ext2", colStr("home_tel_ext2"));
    sp.ppstr("company_name", colStr("company_name"));
    sp.ppstr("cellar_phone", colStr("cellar_phone"));
//    sp.ppstr("staff_flag", colNvl("staff_flag", "N"));
//    sp.ppstr("staff_br_no", colStr("staff_br_no"));
//    sp.ppstr("credit_flag", colNvl("credit_flag", "N"));
    sp.ppstr("comm_flag", colNvl("comm_flag", "N"));
    sp.ppstr("salary_code", colNvl("salary_code", "N"));
    sp.ppnum("asset_value", colNum("asset_value"));
    sp.ppstr("resident_country_code", colStr("resident_country_code"));
    sp.ppstr("ur_flag", colStr("ur_flag"));
    sp.ppstr("inst_flag", colStr("inst_flag"));
    sp.ppstr("fee_code_i", colStr("fee_code_i"));
    sp.ppstr("resident_zip", colStr("resident_zip") + colStr("resident_zip2"));
    sp.ppstr("resident_addr1", colStr("resident_addr1"));
    sp.ppstr("resident_addr2", colStr("resident_addr2"));
    sp.ppstr("resident_addr3", colStr("resident_addr3"));
    sp.ppstr("resident_addr4", colStr("resident_addr4"));
    sp.ppstr("resident_addr5", colStr("resident_addr5"));
    sp.ppstr("company_zip", colStr("company_zip")  + colStr("company_zip2"));
    sp.ppstr("company_addr1", colStr("company_addr1"));
    sp.ppstr("company_addr2", colStr("company_addr2"));
    sp.ppstr("company_addr3", colStr("company_addr3"));
    sp.ppstr("company_addr4", colStr("company_addr4"));
    sp.ppstr("company_addr5", colStr("company_addr5"));
    sp.ppstr("mail_zip",   colStr("mail_zip") + colStr("mail_zip2"));
    sp.ppstr("mail_addr1", colStr("mail_addr1"));
    sp.ppstr("mail_addr2", colStr("mail_addr2"));
    sp.ppstr("mail_addr3", colStr("mail_addr3"));
    sp.ppstr("mail_addr4", colStr("mail_addr4"));
    sp.ppstr("mail_addr5", colStr("mail_addr5"));
    sp.ppstr("indigenous_name", colStr("indigenous_name"));
    sp.ppstr("graduation_elementarty", colStr("graduation_elementarty"));
    sp.ppstr("sms_prim_ch_flag", colNvl("sms_prim_ch_flag", "Y"));
    sp.ppstr("job_position", colStr("job_position"));
    // sp.ppss("business_code", col_ss("business_code"));
    sp.ppstr("e_mail_addr", colStr("e_mail_addr"));
    sp.ppnum("annual_income", colNum("annual_income"));
    sp.ppstr("special_code", colNvl("special_code", "N"));
    sp.ppstr("voice_passwd_flag", colNvl("voice_passwd_flag", "N"));
    sp.ppstr("e_news", colNvl("e_news", "N"));
    sp.ppstr("accept_mbullet", colNvl("accept_mbullet", "N"));
    sp.ppstr("accept_dm", colNvl("accept_dm", "N"));
    sp.ppstr("accept_sms", colNvl("accept_sms", "N"));
    sp.ppstr("accept_call_sell", colNvl("accept_call_sell", "N"));
    sp.ppstr("passport_date", colStr("passport_date"));
    sp.ppstr("salary_holdin_flag", colNvl("salary_holdin_flag", "N"));
    sp.ppstr("holdin_crt_date", colStr("holdin_crt_date"));
    sp.ppstr("holdin_cancel_date", colStr("holdin_cancel_date"));
    sp.ppstr("bank_securit_flag", colNvl("bank_securit_flag", "N"));
    sp.ppstr("bank_prod_insur_flag", colNvl("bank_prod_insur_flag", "N"));
    sp.ppstr("bank_bills_flag", colNvl("bank_bills_flag", "N"));
    sp.ppstr("bank_life_insur_flag", colNvl("bank_life_insur_flag", "N"));
    sp.ppstr("bank_invest_flag", colNvl("bank_invest_flag", "N"));
    sp.ppstr("bank_asset_flag", colNvl("bank_asset_flag", "N"));
    sp.ppstr("bank_venture_flag", colNvl("bank_venture_flag", "N"));
//    sp.ppstr("market_agree_base", colNvl("market_agree_base", "N"));
    sp.ppstr("market_agree_act", colNvl("market_agree_act", "N"));
    sp.ppstr("tsc_market_flag", colNvl("tsc_market_flag", "N"));
    sp.ppstr("pp_renew_flag", colNvl("pp_renew_flag", "N"));
    sp.ppstr("spec_busi_code", wp.itemStr("spec_busi_code"));
    sp.ppstr("crimea_area", wp.itemNvl("crimea_area", "N"));
    sp.ppstr("publ_comp_senior_manager", wp.itemNvl("publ_comp_senior_manager", "N"));
    sp.ppstr("risk_level", wp.itemStr("risk_level"));
    sp.ppstr("risk_level_date", wp.itemStr("risk_level_date"));
    sp.ppstr("resident_no_expire_date", colStr("resident_no_expire_date"));
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    
    if (rc == -1) {
    	errmsg("[crd_idno_online]" + sqlErrtext);
    	return ;
	}

    // -after image-
    sp.sql2Insert("crd_idno_online", wp);
    sp.ppstr("crt_date",sysDate);
    sp.ppstr("crt_time",sysTime);
//    sp.ppymd("crt_date");
//    sp.pptime("crt_time");
    sp.ppstr("crt_user", modUser);
    sp.ppstr("mod_deptno", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("apr_yn", "Y");
    sp.ppstr("apr_flag", "N");
    sp.ppstr("data_image", "2");
    sp.ppstr("id_p_seqno", idPSeqno);
    sp.ppstr("chi_name");
    sp.ppstr("birthday");
    sp.ppstr("resident_no");
    sp.ppstr("other_cntry_code");
    sp.ppstr("passport_no");
    sp.ppstr("other_id");
    sp.ppstr("office_area_code1");
    sp.ppstr("office_tel_no1");
    sp.ppstr("office_tel_ext1");
    sp.ppstr("office_area_code2");
    sp.ppstr("office_tel_no2");
    sp.ppstr("office_tel_ext2");
    sp.ppstr("home_area_code1");
    sp.ppstr("home_tel_no1");
    sp.ppstr("home_tel_ext1");
    sp.ppstr("home_area_code2");
    sp.ppstr("home_tel_no2");
    sp.ppstr("home_tel_ext2");
    sp.ppstr("company_name");
    sp.ppstr("cellar_phone");
//    sp.ppnvl("staff_flag", "N");
//    sp.ppstr("staff_br_no");
    sp.ppnvl("credit_flag", "N");
    sp.ppnvl("comm_flag", "N");
    sp.ppnvl("salary_code", "N");
    sp.ppnum("asset_value");
    sp.ppstr("resident_country_code");
    sp.ppstr("resident_zip", wp.itemStr("resident_zip") + wp.itemStr("resident_zip2"));
    sp.ppstr("resident_addr1");
    sp.ppstr("resident_addr2");
    sp.ppstr("resident_addr3");
    sp.ppstr("resident_addr4");
    sp.ppstr("resident_addr5");
    sp.ppstr("company_zip", wp.itemStr("company_zip") + wp.itemStr("company_zip2"));
    sp.ppstr("company_addr1");
    sp.ppstr("company_addr2");
    sp.ppstr("company_addr3");
    sp.ppstr("company_addr4");
    sp.ppstr("company_addr5");
    sp.ppstr("mail_zip", wp.itemStr("mail_zip") + wp.itemStr("mail_zip2"));
    sp.ppstr("mail_addr1");
    sp.ppstr("mail_addr2");
    sp.ppstr("mail_addr3");
    sp.ppstr("mail_addr4");
    sp.ppstr("mail_addr5");
    sp.ppstr("indigenous_name");
    sp.ppstr("graduation_elementarty");
    sp.ppnvl("sms_prim_ch_flag", "Y");
    sp.ppnvl("ur_flag", "N");
    sp.ppnvl("inst_flag", "N");
    sp.ppnvl("fee_code_i", "N");
    sp.ppstr("job_position");
    // sp.ppss("business_code");
    sp.ppstr("e_mail_addr");
    sp.ppnum("annual_income");
    sp.ppnvl("special_code", "N");
    sp.ppnvl("voice_passwd_flag", "N");
    sp.ppnvl("e_news", "N");
    sp.ppnvl("accept_mbullet", "N");
    sp.ppnvl("accept_dm", "N");
    sp.ppnvl("accept_sms", "N");
    sp.ppnvl("accept_call_sell", "N");
    sp.ppstr("passport_date");
    sp.ppnvl("salary_holdin_flag", "N");
    sp.ppstr("holdin_crt_date");
    sp.ppstr("holdin_cancel_date");
    sp.ppnvl("bank_securit_flag", "N");
    sp.ppnvl("bank_prod_insur_flag", "N");
    sp.ppnvl("bank_bills_flag", "N");
    sp.ppnvl("bank_life_insur_flag", "N");
    sp.ppnvl("bank_invest_flag", "N");
    sp.ppnvl("bank_asset_flag", "N");
    sp.ppnvl("bank_venture_flag", "N");
//    sp.ppnvl("market_agree_base", "N");
    sp.ppnvl("market_agree_act", "N");
    sp.ppnvl("tsc_market_flag", "N");
    sp.ppnvl("pp_renew_flag", "N");
    sp.ppstr("spec_busi_code", wp.itemStr("spec_busi_code"));
    sp.ppstr("crimea_area", wp.itemNvl("crimea_area", "N"));
    sp.ppstr("publ_comp_senior_manager", wp.itemNvl("publ_comp_senior_manager", "N"));
    sp.ppstr("risk_level", wp.itemStr("risk_level"));
    sp.ppstr("risk_level_date", wp.itemStr("risk_level_date"));
    sp.ppstr("resident_no_expire_date");
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    
    if (rc == -1) {
		errmsg("[crd_idno_online]" + sqlErrtext);
	}

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    check();
    idPSeqno = wp.itemStr("id_p_seqno");
    strSql = " select * "
    		    + " from crd_idno" 
                + " where id_p_seqno =?";
    sqlSelect(strSql, new Object[] {idPSeqno});
    if (sqlRowNum <= 0) {
      errmsg("CRD_IDNO not find, kk=" + idPSeqno);
      return rc;
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] residentZipArr = commString.splitZipCode(colStr("resident_zip"));
    colSet("resident_zip", residentZipArr[0]);
    colSet("resident_zip2", residentZipArr[1]);
    String[] mailZipArr = commString.splitZipCode(colStr("mail_zip"));
    colSet("mail_zip", mailZipArr[0]);
    colSet("mail_zip2", mailZipArr[1]);
    String[] companyZipArr = commString.splitZipCode(colStr("company_zip"));
    colSet("company_zip", companyZipArr[0]);
    colSet("company_zip2", companyZipArr[1]);
    
    // 不用覆核的欄位
    // -update CRD_IDNO-
    if (checkModifyIdno() == 1) {
      updateCrdIdno();
		if (sqlRowNum > 0) {
			wp.respMesg = "不需覆核資料異動成功。";
		}
    }else {
    	wp.respMesg = "不需覆核資料無異動。";
    }
    
    if (rc == 1 ) {
    	if (checkModifyAppr() == 1) {
    		delOnline();
    	    insertCrdIdnoOnline();
    	    if (rc == 1) {
    	    	wp.respMesg += "需覆核資料更新成功。";
			}else {
				wp.respMesg += "需覆核資料更新失敗。";
			}
		}else {
			wp.respMesg += "需覆核資料無異動。";
		}
    }
    
    // 目前這些欄位display=none
    if (rc == 1 && checkModifyIdext() == 1) {
      if (ibIdnoExt) {
        updateCrdIdnoExt();
      } else {
        insertCrdIdnoExt();
      }
    }
    return rc;
  }
  
	public void check() {
		String serviceYearYY = wp.itemStr("service_year_YY").trim();
		String serviceYearMM = wp.itemStr("service_year_MM").trim();

		if (serviceYearYY.length() == 0) {
			serviceYearYY = "00";
		} else if (serviceYearYY.length() == 1) {
			serviceYearYY = "0" + serviceYearYY;
		} else if (serviceYearYY.length() > 2) {
			errmsg("無法異動服務年資年份：格式錯誤 ");
			return;
		}

		if (serviceYearMM.length() == 0) {
			serviceYearMM = "00";
		} else if (serviceYearMM.length() == 1) {
			serviceYearMM = "0" + serviceYearMM;
		} else if (serviceYearMM.length() == 2) {
			int serviceYearMMInt = Integer.parseInt(serviceYearMM);
			if (serviceYearMMInt < 0 || serviceYearMMInt > 11) {
				errmsg("無法異動服務年資月份：格式錯誤，需介於0~11 ");
				return;
			}
		}

		wp.colSet("service_year", serviceYearYY + serviceYearMM);
		wp.itemSet("service_year", serviceYearYY + serviceYearMM);

		Boolean error = false;
		String applyId = wp.itemStr("spouse_id_no");
		CommBusiCrd comcrd = new CommBusiCrd();
		// 配偶身份證號碼 為非必填 不爲空時候驗證
		if (applyId != null && !applyId.isEmpty()) {
			error = comcrd.checkId(applyId);
		}
		if (error) {
			errmsg("無法異動配偶身份證號：身份證號格式錯誤");
			rc = -1;
			return;
		}
		
		// 如果公司地址的zip或addr5為空，則需確認是否有act_acno.acno_flag=3的資料(同公司地址)
		if (wp.itemEmpty("company_zip") || wp.itemEmpty("company_addr5")) {
			if ( isBillApplyFlagEq3(wp.itemStr("id_p_seqno"))) {
				errmsg("公司地址不可空白，因客戶帳單註記選擇公司地址");
				return;
			}
		}

	}

	int checkModifyIdno() {

    String[] aaCol = new String[] {"marriage", "sex", "nation", "est_graduate_month", "education",
        "student", "job_position", "vacation_code", "service_year",
        "business_code", "fax_no","msg_flag","father_name", "mother_name", "spouse_name" };

    for (int ii = 0; ii < aaCol.length; ii++) {
      String col = aaCol[ii].trim();
      if (this.colEqIgno(col, wp.itemStr(col)) == true) {
        continue;
      }
      
      return 1;
    }
    
    if (colNum("msg_purchase_amt") == wp.itemNum("msg_purchase_amt")) {
        return 0;
    }

    return 1;
  }

  /**
   * update 不須覆核資料
   */
  void updateCrdIdno() {
    strSql = "update crd_idno set" 
        + " marriage =:marriage ," 
    	+ " sex =:sex ,"
        + " nation =:nation ," 
    	+ " est_graduate_month =:est_graduate_month ,"
        + " education =:education ," 
    	+ " student =:student ," 
        + " job_position =:job_position ,"
        + " vacation_code =:vacation_code ," 
        + " service_year =:service_year ,"
        + " msg_flag =:msg_flag ," 
        + " msg_purchase_amt =:msg_purchase_amt ,"
        + " father_name =:father_name ," 
        + " mother_name =:mother_name ,"
        + " spouse_name =:spouse_name ," 
        + " spouse_birthday =:spouse_birthday ,"
        + " spouse_id_no=:spouse_id_no , "
        + " business_code =:business_code ," 
        + " mod_user =:mod_user ," 
        + " mod_time = sysdate ,";

          // 2022/02/24 Justin: change to update c_phone_chg_date and c_phone_chg_time when the data is approved
//        if (wp.itemEmpty("cellar_phone") == false) {
//          strSql += " c_phone_chg_date = to_char(sysdate,'yyyymmdd') ,c_phone_chg_time = sysdate ,";
//        }
        
    strSql  += " mod_pgm  =:mod_pgm , "
        	 + " mod_seqno =nvl(mod_seqno,0)+1 "
    		 + " where id_p_seqno =:id_p_seqno";

    item2ParmStr("id_p_seqno");
    item2ParmNvl("marriage", "1");
    item2ParmNvl("sex", "1");
    item2ParmNvl("nation", "1");
    item2ParmStr("est_graduate_month");
    item2ParmStr("education");
    item2ParmNvl("student", "N");
    item2ParmStr("job_position");
    item2ParmStr("vacation_code");
    item2ParmStr("service_year");
    item2ParmNvl("msg_flag", "N");
    item2ParmNum("msg_purchase_amt");
    item2ParmStr("father_name");
    item2ParmStr("mother_name");
    item2ParmStr("spouse_name");
    item2ParmStr("spouse_birthday");
    item2ParmStr("spouse_id_no");
    item2ParmStr("business_code");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", pgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

  }

  int checkModifyIdext() {
    ibIdnoExt = true;
    // -returnVal: 1.有異動, 0.資料不存在, -1.未異動-
    strSql = "select * from crd_idno_ext" + " where id_p_seqno =?";
    sqlSelect(strSql, new Object[] {idPSeqno});
    if (sqlRowNum <= 0) {
      ibIdnoExt = false;
      return 1;
    }

    String[] aaCol =
        new String[] {"asig_inq1_cname", "asig_inq1_idno", "asig_inq1_telno", "asig_inq1_chk_data",
            "asig_inq2_cname", "asig_inq2_idno", "asig_inq2_telno", "asig_inq2_chk_data",
            "asig_inq3_cname", "asig_inq3_idno", "asig_inq3_telno", "asig_inq3_chk_data"};

    String col = "";
    for (int ii = 0; ii < aaCol.length; ii++) {
      col = aaCol[ii].trim();
      if (this.colEqIgno(col, wp.itemStr(col)) == true) {
        continue;
      }
      return 1;
    }

    col = "asig_inq1_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq1_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq1_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq2_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_acct";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_card";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;
    col = "asig_inq3_limit";
    if (eqIgno(colNvl(col, "N"), wp.itemNvl(col, "N")) == false)
      return 1;

    return 0;
  }

  void updateCrdIdnoExt() {
    strSql = "update CRD_IDNO_EXT set" + " asig_inq1_cname =:asig_inq1_cname , "
        + " asig_inq1_idno =:asig_inq1_idno , " + " asig_inq1_telno =:asig_inq1_telno , "
        + " asig_inq1_acct =:asig_inq1_acct , " + " asig_inq1_card =:asig_inq1_card , "
        + " asig_inq1_limit =:asig_inq1_limit , " + " asig_inq1_chk_data =:asig_inq1_chk_data , "
        + " asig_inq2_cname =:asig_inq2_cname , " + " asig_inq2_idno =:asig_inq2_idno , "
        + " asig_inq2_telno =:asig_inq2_telno , " + " asig_inq2_acct =:asig_inq2_acct , "
        + " asig_inq2_card =:asig_inq2_card , " + " asig_inq2_limit =:asig_inq2_limit , "
        + " asig_inq2_chk_data =:asig_inq2_chk_data , " + " asig_inq3_cname =:asig_inq3_cname , "
        + " asig_inq3_idno =:asig_inq3_idno , " + " asig_inq3_telno =:asig_inq3_telno , "
        + " asig_inq3_acct =:asig_inq3_acct , " + " asig_inq3_card =:asig_inq3_card , "
        + " asig_inq3_limit =:asig_inq3_limit , " + " asig_inq3_chk_data =:asig_inq3_chk_data , "
        + " acs_email_flag =:acs_email_flag ," + " apr_date = to_char(sysdate,'yyyymmdd') , "
        + " apr_user =:apr_user , " + " mod_user =:mod_user , " + " mod_time =sysdate , "
        + " mod_pgm  =:mod_pgm , " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where id_p_seqno =:id_p_seqno";

    item2ParmStr("id_p_seqno");
    item2ParmStr("asig_inq1_cname");
    item2ParmStr("asig_inq1_idno");
    item2ParmStr("asig_inq1_telno");
    item2ParmNvl("asig_inq1_acct", "N");
    item2ParmNvl("asig_inq1_card", "N");
    item2ParmNvl("asig_inq1_limit", "N");
    item2ParmStr("asig_inq1_chk_data");
    item2ParmStr("asig_inq2_cname");
    item2ParmStr("asig_inq2_idno");
    item2ParmStr("asig_inq2_telno");
    item2ParmNvl("asig_inq2_acct", "N");
    item2ParmNvl("asig_inq2_card", "N");
    item2ParmNvl("asig_inq2_limit", "N");
    item2ParmStr("asig_inq2_chk_data");
    item2ParmStr("asig_inq3_cname");
    item2ParmStr("asig_inq3_idno");
    item2ParmStr("asig_inq3_telno");
    item2ParmNvl("asig_inq3_acct", "N");
    item2ParmNvl("asig_inq3_card", "N");
    item2ParmNvl("asig_inq3_limit", "N");
    item2ParmStr("asig_inq3_chk_data");
    item2ParmNvl("acs_email_flag", "N");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", pgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

  }

  void insertCrdIdnoExt() {
    strSql = "insert into CRD_IDNO_EXT (" + " id_p_seqno , " + " asig_inq1_cname , "
        + " asig_inq1_idno , " + " asig_inq1_telno , " + " asig_inq1_acct , " + " asig_inq1_card , "
        + " asig_inq1_limit , " + " asig_inq1_chk_data , " + " asig_inq2_cname , "
        + " asig_inq2_idno , " + " asig_inq2_telno , " + " asig_inq2_acct , " + " asig_inq2_card , "
        + " asig_inq2_limit , " + " asig_inq2_chk_data , " + " asig_inq3_cname , "
        + " asig_inq3_idno , " + " asig_inq3_telno , " + " asig_inq3_acct , " + " asig_inq3_card , "
        + " asig_inq3_limit , " + " asig_inq3_chk_data , " + " acs_email_flag , " + " apr_date , "
        + " apr_user , " + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno "
        + " ) values (" + " :id_p_seqno , " + " :asig_inq1_cname , " + " :asig_inq1_idno , "
        + " :asig_inq1_telno , " + " :asig_inq1_acct , " + " :asig_inq1_card , "
        + " :asig_inq1_limit , " + " :asig_inq1_chk_data , " + " :asig_inq2_cname , "
        + " :asig_inq2_idno , " + " :asig_inq2_telno , " + " :asig_inq2_acct , "
        + " :asig_inq2_card , " + " :asig_inq2_limit , " + " :asig_inq2_chk_data , "
        + " :asig_inq3_cname , " + " :asig_inq3_idno , " + " :asig_inq3_telno , "
        + " :asig_inq3_acct , " + " :asig_inq3_card , " + " :asig_inq3_limit , "
        + " :asig_inq3_chk_data , " + " :acs_email_flag , " + " to_char(sysdate,'yyyymmdd') , "
        + " :apr_user , " + " :mod_user , " + " sysdate , " + " :mod_pgm , " + " 1 " + " )";

    item2ParmStr("id_p_seqno");
    item2ParmStr("asig_inq1_cname");
    item2ParmStr("asig_inq1_idno");
    item2ParmStr("asig_inq1_telno");
    item2ParmNvl("asig_inq1_acct", "N");
    item2ParmNvl("asig_inq1_card", "N");
    item2ParmNvl("asig_inq1_limit", "N");
    item2ParmStr("asig_inq1_chk_data");
    item2ParmStr("asig_inq2_cname");
    item2ParmStr("asig_inq2_idno");
    item2ParmStr("asig_inq2_telno");
    item2ParmNvl("asig_inq2_acct", "N");
    item2ParmNvl("asig_inq2_card", "N");
    item2ParmNvl("asig_inq2_limit", "N");
    item2ParmStr("asig_inq2_chk_data");
    item2ParmStr("asig_inq3_cname");
    item2ParmStr("asig_inq3_idno");
    item2ParmStr("asig_inq3_telno");
    item2ParmNvl("asig_inq3_acct", "N");
    item2ParmNvl("asig_inq3_card", "N");
    item2ParmNvl("asig_inq3_limit", "N");
    item2ParmStr("asig_inq3_chk_data");
    item2ParmNvl("acs_email_flag", "N");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", pgm);

    wp.log("idno_ext=" + strSql);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("crd_idno_ext=" + sqlErrtext);
    }
  }

  @Override
  public int dbDelete() {

    delOnline();
    return rc;
  }

  void delOnline() {
    strSql = "delete crd_idno_online " + " where id_p_seqno =:id_p_seqno " + " and apr_YN ='Y' "
        + " and apr_flag<>'Y' ";

    item2ParmStr("id_p_seqno");
    sqlExec(strSql);
  }

  // Cmsp2010 覆核用 --2017/11/23 ***************************************************
  @Override
  public int dataProc() {
     dataCheck();
    if (rc != 1)
      return rc;

    updateCrdIdnoApr();
    if (rc != 1) {
      return rc;
    }
    
    updateJCIC();
    if (rc != 1) {
      return rc;
    }

    updateCrdIdnoOnline();
    if (rc != 1) {
      return rc;
    }

    if (eqIgno(colStr("voice_passwd_flag"), "Y")) {
      lsPawd = "";
      log("A:" + colStr("id_no"));
      lsPawd = commString.mid(colStr("id_no"), 6, 4);
      strSql =
                 " update crd_idno set " 
              + " voice_passwd = :voice_passwd ," 
              + " voice_passwd_flag = 'N' , "
              // 2020-09-09 JustinWu
              + " apr_user = :apr_user , "
              + " apr_date = to_char(sysdate, 'yyyyMMdd') , "
              // 2020-09-09 JustinWu
              + " mod_user = :mod_user , "
              + " mod_pgm = :mod_pgm , "
              + " mod_time = sysdate , "
              + " mod_seqno = nvl(mod_seqno, 0) + 1 "
              + " where id_p_seqno = :id_p_seqno " 
        	  + " and voice_passwd_flag ='Y' ";
      setString("voice_passwd", lsPawd);
      setString("apr_user", colStr("online_mod_user"));  // 2020-09-09 JustinWu
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", pgm);
      item2ParmStr("id_p_seqno");
      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg("update voice_passwd error !");
        return -1;
      }

      strSql = " update crd_voice_passwd_log " 
          + " apr_user = :apr_user ,"
          + " apr_date = to_char(sysdate,'yyyymmdd') ,"
          + " apr_time = to_char(sysdate,'hh24miss') ," + " mod_user = :mod_user ,"
          + " mod_time = sysdate , " 
          + " mod_pgm = 'cmsp2010' , "
          + " where id_p_seqno =:id_p_seqno " 
          + " and crt_date =:crt_date "
          + " and crt_time =:crt_time " 
          + " and crt_user =:crt_user "
          + " and decode(apr_date,'','N','','N')='N' " 
          + " and nvl(voice_passwd_flag,'N') = 'Y' ";
      setString("apr_user", wp.loginUser);
      setString("mod_user", wp.loginUser);
      item2ParmStr("id_p_seqno");
      setString("crt_date", wp.itemStr("mod_date"));
      setString("crt_time", wp.itemStr("mod_time"));
      setString("crt_user", wp.itemStr("mod_user"));
      sqlExec(strSql);
      if (sqlRowNum < 0) {
        errmsg("update voice_passwd_log error !");
        rc = -1;
        return rc;
      } else {
        rc = 1;
      }
    }

    //20231017 先disabled
//    if (eqIgno(wp.itemStr("wk_dm_flag"), "1")) {
//      updateDbcIdno();
//    }

    return rc;
  }

  @Override
  public void dataCheck() {
    String sql1 = "select count(*) as ll_cnt "
    		+ " from crd_idno_online " 
    		+ " where id_p_seqno = ? "
            + " and crt_date = ? " 
    		+ " and crt_time = ? " 
            + " and crt_user = ? "
            + " and apr_YN = 'Y' "
            + " and nvl(apr_flag,'N') <> 'Y' "
            ;
    sqlSelect(sql1, new Object[] {wp.itemStr("id_p_seqno"), wp.itemStr("mod_date"),
        wp.itemStr("mod_time"), wp.itemStr("mod_user")});
    log("A:" + wp.itemStr("id_p_seqno"));
    log("B:" + wp.itemStr("mod_date"));
    log("C:" + wp.itemStr("mod_time"));
    log("D:" + wp.itemStr("mod_user"));
    if (colNum("ll_cnt") != 2) {
      errmsg("此筆資料已有人修改, 請結束再覆核 !");
      rc = -1;
      return;
    }
  }

  void updateJCIC() {
	    // check whether annual_income is changed
		boolean isNotChg = (wp.itemNum("annual_income") == wp.itemNum("old_annual_income"));
		if (isNotChg) return;
		
		// delete JCIC
		delJCIC();
		if (rc != 1) {
			return;
		}
		
		// insert JCIC
		insertJCIC();
		if (rc != 1) {
			return;
		}
  }

  void delJCIC() {
    msgOK();
    strSql = "delete crd_jcic_idno where id_p_seqno =:id_p_seqno and to_jcic_date='' ";
    item2ParmStr("id_p_seqno");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete JCIC error !");
    } 
  }

  void insertJCIC() {
    String sql1 = "select " + " id_no ," + " id_no_code ," + " business_code ," + " id_p_seqno ,"
        + " chi_name ," + " card_since ," + " birthday ," + " education ," + " marriage ,"
        + " job_position ," + " service_year ," + " annual_income ," + " company_name ,"
        + " business_id ,"
        + " rtrim(home_area_code1)||rtrim(home_tel_no1)||rtrim(home_tel_ext1) as home_tel_no ,"
        + " case when length(rtrim(office_area_code1)||rtrim(office_tel_no1)||rtrim(office_tel_ext1)) > 16 then rtrim(office_area_code1)||rtrim(office_tel_no1) "
        + " else rtrim(office_area_code1)||rtrim(office_tel_no1)||rtrim(office_tel_ext1) end as offi_telno ,"
        + " rtrim(resident_zip) as resid_zip ,"
        + " rtrim(resident_addr1)||rtrim(resident_addr2)||rtrim(resident_addr3)||rtrim(resident_addr4)||rtrim(resident_addr5) as resid_addr ,"
        + " cellar_phone ," + " annual_income ," + " sex ," + " passport_no ," + " passport_date ,"
        + " other_cntry_code " 
        + " from crd_idno " 
        + " where id_p_seqno = ?";
    sqlSelect(sql1, new Object[] {wp.itemStr("id_p_seqno")});

    String sql2 = "select " + " bill_sending_zip as mail_zip ,"
        + " bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5 as mail_addr "
        + " from act_acno " 
        + " where id_p_seqno = ? " 
        + " fetch first 1 rows only ";

    sqlSelect(sql2, new Object[] {wp.itemStr("id_p_seqno")});

    msgOK();

    strSql = "insert into crd_jcic_idno (" + " crt_date ," + " trans_type ," + " account_style ,"
        + " id_p_seqno ," + " old_id ," + " old_id_code ," + " chi_name ," + " old_eng_name ,"
        + " eng_name ," + " birthday ," + " mail_zip ," + " mail_addr ," + " old_mail_zip ,"
        + " old_mail_addr ," + " resident_addr ," + " resident_flag ," + " tel_no ,"
        + " cellar_phone ," + " business_id ," + " business_code ," + " company_name ,"
        + " office_tel_no ," + " job_position ," + " service_year ," + " salary ," + " education ,"
        + " sex ," + " cntry_code ," + " passport_no ," + " passport_date ," + " update_date ,"
        + " apr_date ," + " apr_user ," + " crt_user ," + " mod_pgm ," + " mod_time "
        + " ) values (" + " to_char(sysdate,'yyyymmdd') ," + " 'C' ," + " 'M' ," + " :id_p_seqno ,"// 1
        + " :old_id ," + " :old_id_code ," + " :chi_name ," + " :old_eng_name ,"// 5
        + " :eng_name ," + " :birthday ," + " :mail_zip ," + " :mail_addr ," + " :old_mail_zip ,"// 10
        + " :old_mail_addr ," + " :resident_addr ," + " 'N' ," + " :tel_no ," + " :cellar_phone ,"
        + " :business_id ,"// 15
        + " :business_code ," + " :company_name ," + " :office_tel_no ," + " :job_position ,"
        + " :service_year ,"// 20
        + " :salary ," + " :education ," + " :sex ," + " :cntry_code ," + " :passport_no ,"// 25
        + " :passport_date ," + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :crt_user ,"// 28
        + " 'cmsp2010' ," + " sysdate " + " )";

    item2ParmStr("id_p_seqno");// 1
    setString("old_id", colStr("id_no"));
    setString("old_id_code", colStr("id_no_code"));
    setString("chi_name", colStr("chi_name"));
    setString("old_eng_name", colStr("old_eng_name"));// 5
    setString("eng_name", colStr("eng_name"));
    setString("birthday", colStr("birthday"));
    setString("mail_zip", colStr("mail_zip"));
    setString("mail_addr", colStr("mail_addr"));
    setString("old_mail_zip", colStr("mail_zip"));// 10
    setString("old_mail_addr", colStr("mail_addr"));
    setString("resident_addr", colStr("resid_addr"));
    setString("tel_no", colStr("home_tel_no"));
    setString("cellar_phone", colStr("cellar_phone"));
    setString("business_id", colStr("business_id"));// 15
    setString("business_code", colStr("business_code"));
    setString("company_name", colStr("company_name"));
    setString("office_tel_no", colStr("offi_telno"));
    setString("job_position", colStr("job_position"));
    setString("service_year", colStr("service_year"));// 20
    setString("salary", wp.itemStr("annual_income"));
    setString("education", colStr("education"));
    setString("sex", colStr("sex"));
    setString("cntry_code", wp.itemStr("other_cntry_code"));
    setString("passport_no", wp.itemStr("passport_no"));// 25
    setString("passport_date", wp.itemStr("passport_date"));
    setString("apr_user", wp.loginUser);
    setString("crt_user", wp.loginUser);// 28

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert JCIC error !");
    }
  }

  void updateCrdIdnoApr() {
    String sql1 = "select  cid.* , cid.crt_user as online_mod_user, ci.CELLAR_PHONE as crd_idno_cellar_phone " 
                + " from crd_idno_online cid JOIN CRD_IDNO ci  ON cid.ID_P_SEQNO = ci.ID_P_SEQNO  " 
    		    + " where cid.id_p_seqno =?"
                + " and cid.apr_YN ='Y' " 
    		    + " and cid.apr_flag <>'Y'" 
                + " and cid.data_image ='2'";
    sqlSelect(sql1, new Object[] {wp.itemStr("id_p_seqno")});
    log("A:" + wp.itemStr("id_p_seqno"));
    msgOK();
    
    StringBuilder sb = new StringBuilder(); 
    sb.append(" update crd_idno set " );
    sb.append(" chi_name =:chi_name ,"); 
	sb.append(" birthday =:birthday ,");
    sb.append(" annual_income =:annual_income ,"); 
//	sb.append(" staff_flag =:staff_flag ,");
//    sb.append(" staff_br_no =:staff_br_no ,"); 
	sb.append(" credit_flag =:credit_flag ,");
    sb.append(" salary_code =:salary_code ,"); 
	sb.append(" resident_no =:resident_no ,");
    sb.append(" other_cntry_code =:other_cntry_code ,"); 
	sb.append(" passport_no =:passport_no ,");
    sb.append(" other_id =:other_id ,"); 
	sb.append(" company_name =:company_name , " );
    // sb.append(" business_code =:business_code ,");
    sb.append(" comm_flag =:comm_flag ,"); 
    sb.append(" office_area_code1 =:office_area_code1 ,");
    sb.append(" office_tel_no1 =:office_tel_no1 ,"); 
    sb.append(" office_tel_ext1 =:office_tel_ext1 ,");
    sb.append(" office_area_code2 =:office_area_code2 ,"); 
    sb.append(" office_tel_no2 =:office_tel_no2 ,");
    sb.append(" office_tel_ext2 =:office_tel_ext2 ,"); 
    sb.append(" home_area_code1 =:home_area_code1 ,");
    sb.append(" home_tel_no1 =:home_tel_no1 ,"); 
    sb.append(" home_tel_ext1 =:home_tel_ext1 ,");
    sb.append(" home_area_code2 =:home_area_code2 ,"); 
    sb.append(" home_tel_no2 =:home_tel_no2 ,");
    sb.append(" home_tel_ext2 =:home_tel_ext2 ,"); 
    sb.append(" resident_country_code =:resident_country_code ,");
    sb.append(" resident_zip =:resident_zip ,"); 
    sb.append(" resident_addr1 =:resident_addr1 ,");
    sb.append(" resident_addr2 =:resident_addr2 ,"); 
    sb.append(" resident_addr3 =:resident_addr3 ,");
    sb.append(" resident_addr4 =:resident_addr4 ,"); 
    sb.append(" resident_addr5 =:resident_addr5 ,");
    sb.append(" company_zip =:company_zip ,"); 
    sb.append(" company_addr1 =:company_addr1 ,");
    sb.append(" company_addr2 =:company_addr2 ,"); 
    sb.append(" company_addr3 =:company_addr3 ,");
    sb.append(" company_addr4 =:company_addr4 ,"); 
    sb.append("company_addr5 =:company_addr5 ,");
    sb.append(" mail_zip =:mail_zip ,"); 
    sb.append(" mail_addr1 =:mail_addr1 ,");
    sb.append(" mail_addr2 =:mail_addr2 ,"); 
    sb.append(" mail_addr3 =:mail_addr3 ,");
    sb.append(" mail_addr4 =:mail_addr4 ,"); 
    sb.append(" mail_addr5 =:mail_addr5 ,");
    sb.append(" cellar_phone =:cellar_phone ,"); ;
    // 2022/02/24 Justin: update 
    if (colStr("crd_idno_cellar_phone").trim().equals(colStr("cellar_phone").trim()) == false ) {
    	sb.append(" c_phone_chg_date = to_char(sysdate,'yyyymmdd'), " );
        sb.append(" c_phone_chg_time = sysdate, ");
	}
    sb.append(" e_mail_addr =:e_mail_addr ,");
    sb.append(" voice_passwd_flag =:voice_passwd_flag ,"); 
    sb.append(" special_code =:special_code ,");
    sb.append(" e_mail_from_mark =:e_mail_from_mark ,"); 
    sb.append(" e_mail_chg_date =:e_mail_chg_date ,");
    sb.append(" e_news =:e_news ,"); 
    sb.append(" e_news_from_mark =:e_news_from_mark ,");
    sb.append(" e_news_chg_date =:e_news_chg_date ,"); 
    sb.append(" accept_mbullet =:accept_mbullet ,");
    sb.append(" mbullet_from_mark =:mbullet_from_mark ,"); 
    sb.append(" mbullet_chg_date =:mbullet_chg_date ,");
    sb.append(" accept_dm =:accept_dm ,"); 
    sb.append(" dm_from_mark =:dm_from_mark ,");
    sb.append(" dm_chg_date =:dm_chg_date ,"); 
    sb.append(" accept_sms =:accept_sms ,");
    sb.append(" sms_from_mark =:sms_from_mark ,"); 
    sb.append(" sms_chg_date =:sms_chg_date , ");
    sb.append(" sms_prim_ch_flag =:sms_prim_ch_flag, ");
    sb.append(" accept_call_sell =:accept_call_sell ,"); 
    sb.append(" call_sell_from_mark =:call_sell_from_mark ,");
    sb.append(" call_sell_chg_date =:call_sell_chg_date ,");;
    if (wp.itemNum("annual_income") !=wp.itemNum("old_annual_income")) {
        sb.append(" annual_date =to_char(sysdate,'yyyymmdd') ,");
    }
    sb.append(" passport_date =:passport_date ,"); 
    sb.append(" salary_holdin_flag =:salary_holdin_flag ,");
    sb.append(" holdin_crt_date =:holdin_crt_date ,"); 
    sb.append(" holdin_cancel_date =:holdin_cancel_date ,");
    sb.append(" bank_securit_flag =:bank_securit_flag ,");
    sb.append(" bank_prod_insur_flag =:bank_prod_insur_flag ,"); 
    sb.append(" bank_bills_flag =:bank_bills_flag ,");
    sb.append(" bank_life_insur_flag =:bank_life_insur_flag ,");
    sb.append(" bank_invest_flag =:bank_invest_flag ,"); 
    sb.append(" bank_asset_flag =:bank_asset_flag ,");
    sb.append(" bank_venture_flag =:bank_venture_flag ,"); 
//    sb.append(" market_agree_base =:market_agree_base ,");
    sb.append(" market_agree_act =:market_agree_act ,"); 
    sb.append(" tsc_market_flag =:tsc_market_flag ,");
    sb.append(" tsc_market_date =:tsc_market_date ,"); 
    sb.append(" pp_renew_flag =:pp_renew_flag ,");
    // 2020-08-21 JustinWu
    sb.append(" indigenous_name =:indigenous_name ,");
    sb.append(" resident_no_expire_date =:resident_no_expire_date ,");
    sb.append(" fee_code_i =:fee_code_i ,");
    sb.append(" inst_flag =:inst_flag ,");
    sb.append(" ur_flag =:ur_flag ,");
    sb.append(" graduation_elementarty =:graduation_elementarty ,");
 // 2020-08-21 JustinWu  
    sb.append(" apr_date = to_char(sysdate,'yyyymmdd') ,"); 
    sb.append(" apr_user =:apr_user ,");
    sb.append(" mod_user =:mod_user ,"); 
    sb.append(" mod_time = sysdate ,"); 
    sb.append(" mod_pgm =:mod_pgm ,");
    sb.append(" mod_seqno = nvl(mod_seqno, 0)+1 ") ; 
    sb.append(" where id_p_seqno =:id_p_seqno") ;
    strSql = sb.toString();
    setString("apr_user", wp.loginUser);
    setString("mod_user", colStr("online_mod_user"));
    setString("mod_pgm", "cmsp2010");
    setString("chi_name", colStr("chi_name"));
    setString("birthday", colStr("birthday"));
    setString("annual_income", colStr("annual_income"));
//    setString("staff_flag", colStr("staff_flag"));
//    setString("staff_br_no", colStr("staff_br_no"));
    setString("credit_flag", colStr("credit_flag"));
    setString("salary_code", colStr("salary_code"));
    setString("resident_no", colStr("resident_no"));
    setString("other_cntry_code", colStr("other_cntry_code"));
    setString("passport_no", colStr("passport_no"));
    setString("other_id", colStr("other_id"));
    setString("company_name", colStr("company_name"));
    // setString("business_code", col_ss("business_code"));
    setString("comm_flag", colStr("comm_flag"));
    setString("office_area_code1", colStr("office_area_code1"));
    setString("office_tel_no1", colStr("office_tel_no1"));
    setString("office_tel_ext1", colStr("office_tel_ext1"));
    setString("office_area_code2", colStr("office_area_code2"));
    setString("office_tel_no2", colStr("office_tel_no2"));
    setString("office_tel_ext2", colStr("office_tel_ext2"));
    setString("home_area_code1", colStr("home_area_code1"));
    setString("home_tel_no1", colStr("home_tel_no1"));
    setString("home_tel_ext1", colStr("home_tel_ext1"));
    setString("home_area_code2", colStr("home_area_code2"));
    setString("home_tel_no2", colStr("home_tel_no2"));
    setString("home_tel_ext2", colStr("home_tel_ext2"));
    setString("resident_country_code", colStr("resident_country_code"));
    setString("resident_zip", colStr("resident_zip") + colStr("resident_zip2"));
    setString("resident_addr1", colStr("resident_addr1"));
    setString("resident_addr2", colStr("resident_addr2"));
    setString("resident_addr3", colStr("resident_addr3"));
    setString("resident_addr4", colStr("resident_addr4"));
    setString("resident_addr5", colStr("resident_addr5"));
    setString("company_zip", colStr("company_zip") + colStr("company_zip2"));
    setString("company_addr1", colStr("company_addr1"));
    setString("company_addr2", colStr("company_addr2"));
    setString("company_addr3", colStr("company_addr3"));
    setString("company_addr4", colStr("company_addr4"));
    setString("company_addr5", colStr("company_addr5"));
    setString("company_addr5", colStr("company_addr5"));
    setString("mail_zip", colStr("mail_zip") + colStr("mail_zip2") );
    setString("mail_addr1", colStr("mail_addr1"));
    setString("mail_addr2", colStr("mail_addr2"));
    setString("mail_addr3", colStr("mail_addr3"));
    setString("mail_addr4", colStr("mail_addr4"));
    setString("mail_addr5", colStr("mail_addr5"));
    setString("cellar_phone", colStr("cellar_phone"));
    setString("e_mail_addr", colStr("e_mail_addr"));
    setString("voice_passwd_flag", colStr("voice_passwd_flag"));
    setString("special_code", colStr("special_code"));
    setString("e_news", colStr("e_news"));
    setString("accept_mbullet", colStr("accept_mbullet"));
    setString("accept_dm", colStr("accept_dm"));
    setString("accept_sms", colStr("accept_sms"));
    setString("accept_call_sell", colStr("accept_call_sell"));
    setString("passport_date", colStr("passport_date"));
    setString("salary_holdin_flag", colStr("salary_holdin_flag"));
    setString("holdin_crt_date", colStr("holdin_crt_date"));
    setString("holdin_cancel_date", colStr("holdin_cancel_date"));
    setString("bank_securit_flag", colStr("bank_securit_flag"));
    setString("bank_prod_insur_flag", colStr("bank_prod_insur_flag"));
    setString("bank_bills_flag", colStr("bank_bills_flag"));
    setString("bank_life_insur_flag", colStr("bank_life_insur_flag"));
    setString("bank_invest_flag", colStr("bank_invest_flag"));
    setString("bank_asset_flag", colStr("bank_asset_flag"));
    setString("bank_venture_flag", colStr("bank_venture_flag"));
//    setString("market_agree_base", colNvl("market_agree_base","N"));
    setString("market_agree_act", colStr("market_agree_act"));
    setString("tsc_market_flag", colStr("tsc_market_flag"));
    setString("pp_renew_flag", colStr("pp_renew_flag"));
    // 2020-08-21 JustinWu
    setString("indigenous_name", colStr("indigenous_name"));
    setString("resident_no_expire_date", colStr("resident_no_expire_date"));
    setString("fee_code_i", colNvl("fee_code_i","N"));
    setString("inst_flag", colNvl("inst_flag","N"));
    setString("ur_flag", colNvl("ur_flag","N"));
    setString("graduation_elementarty", colStr("graduation_elementarty"));
    // 2020-08-21 JustinWu
    item2ParmStr("e_mail_from_mark");
    item2ParmStr("e_mail_chg_date");
    item2ParmStr("e_news_from_mark");
    item2ParmStr("e_news_chg_date");
    item2ParmStr("mbullet_from_mark");
    item2ParmStr("mbullet_chg_date");
    item2ParmStr("dm_from_mark");
    item2ParmStr("dm_chg_date");
    item2ParmStr("sms_from_mark");
    item2ParmStr("sms_chg_date");
    item2ParmNvl("sms_prim_ch_flag", "Y");
    item2ParmStr("call_sell_from_mark");
    item2ParmStr("call_sell_chg_date");
    item2ParmStr("tsc_market_date");
    item2ParmStr("id_p_seqno");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update crd_idno error!");
      return;
    }

    // --update crd_idno_ext

    strSql = " update crd_idno_ext set " 
        + " spec_busi_code =:spec_busi_code , "
        + " crimea_area =:crimea_area , "
        + " publ_comp_senior_manager =:publ_comp_senior_manager , " 
        + " risk_level =:risk_level ,"
        + " risk_level_date =:risk_level_date " 
        + " where id_p_seqno =:id_p_seqno ";

    setString("spec_busi_code", colStr("spec_busi_code"));
    setString("crimea_area", colStr("crimea_area"));
    setString("publ_comp_senior_manager", colStr("publ_comp_senior_manager"));
    setString("risk_level", colStr("risk_level"));
    setString("risk_level_date", colStr("risk_level_date"));
    item2ParmStr("id_p_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update crd_idno_ext error!");
      return;
    }
  }

  void updateCrdIdnoOnline() {
    msgOK();
    strSql = " update crd_idno_online set " 
        + " apr_flag = 'Y' , " 
    	+ " apr_user = :apr_user , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " 
    	+ " apr_time = to_char(sysdate,'hh24miss')  "
        + " where id_p_seqno = :id_p_seqno " 
        + " and crt_date = :mod_date "
        + " and crt_time = :mod_time " 
        + " and crt_user = :mod_user " 
        + " and apr_yn = 'Y' "
        + " and nvl(apr_flag,'N') <> 'Y' ";
    setString("apr_user", wp.loginUser);
    item2ParmStr("id_p_seqno");
    item2ParmStr("mod_date");
    item2ParmStr("mod_time");
    item2ParmStr("mod_user");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update crd_idno_online error !");
      return;
    }
  }

  void updateDbcIdno() {
    msgOK();
    strSql = " update dbc_idno set " 
        + " e_news =:e_news , "
        + " e_news_from_mark =:e_news_from_mark , " 
        + " e_news_chg_date =:e_news_chg_date , "
        + " accept_mbullet =:accept_mbullet , " 
        + " mbullet_from_mark =:mbullet_from_mark , "
        + " mbullet_chg_date =:mbullet_chg_date , " 
        + " accept_dm =:accept_dm , "
        + " dm_from_mark =:dm_from_mark , " 
        + " dm_chg_date =:dm_chg_date , "
        + " accept_sms =:accept_sms , " 
        + " sms_from_mark =:sms_from_mark , "
        + " sms_chg_date =:sms_chg_date , " 
        + " accept_call_sell =:accept_call_sell , "
        + " call_sell_from_mark =:call_sell_from_mark , "
        + " call_sell_chg_date =:call_sell_chg_date , " 
        + " mod_time = sysdate , "
        + " mod_pgm ='cmsp2010' , "
        + " mod_user = :mod_user , "
        // 2020-09-09 JustinWu
        + " apr_user = :apr_user , "
        + " apr_date = to_char(sysdate, 'yyyyMMdd'),"
        // 2020-09-09 JustinWu        
        + " mod_seqno = nvl(mod_seqno, 0) + 1 " 
        + " where id_no =:id_no ";
    setString("e_news", colStr("e_news"));
    item2ParmStr("e_news_from_mark");
    item2ParmStr("e_news_chg_date");
    setString("accept_mbullet", colStr("accept_mbullet"));
    item2ParmStr("mbullet_from_mark");
    item2ParmStr("mbullet_chg_date");
    setString("accept_dm", colStr("accept_dm"));
    item2ParmStr("dm_from_mark");
    item2ParmStr("dm_chg_date");
    setString("accept_sms", colStr("accept_sms"));
    item2ParmStr("sms_from_mark");
    item2ParmStr("sms_chg_date");
    setString("accept_call_sell", colStr("accept_call_sell"));
    item2ParmStr("call_sell_from_mark");
    item2ParmStr("call_sell_chg_date");
    setString("mod_user", colStr("crt_user"));
    setString("apr_user", wp.loginUser);
    item2ParmStr("id_no");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("update DBC_IDNO error !");
      rc = -1;
      return;
    } else {
      rc = 1;
    }

  }
  
  
  /**
	 * 確認客戶手機最後一筆放行(異動)日期及時間
	 * @param chgTable 更新的主檔名稱,String chgColumn 更新的欄位名稱, String idno 身份證字號
	 * @return maxchgdate 最後放行(異動)日期 ,maxchgtime 最後放行(異動)時間
	 */
  
  String[] selectCmsChgColumnLog(String chgTable,String chgColumn, String idno){
  String sql1  = " select  chg_date as maxchgdate ,  chg_time as maxchgtime "
 	         // + " from  cms_chgcolumn_log  where chg_table= ?  and chg_column= ?  and id_p_seqno = uf_idno_pseqno(?) "
 	         // 20220929 Sunny 修正純VD卡查詢手機最後一筆放行(異動)日期及時間。
 	         + " from  cms_chgcolumn_log  where chg_table= ?  and chg_column= ?  and id_p_seqno = (SELECT ID_P_SEQNO FROM CRD_IDNO_SEQNO WHERE ID_NO= ? limit 1 ) "
 	          + " order by mod_time desc limit 1 ";
 	this.sqlSelect(sql1, new Object[] {chgTable,chgColumn,idno});
		  return new String[]{colStr("maxchgdate"), colStr("maxchgtime")};	  
}

/**
	 * 確認客戶帳單註記是否選擇公司地址
	 * @param idPSeqno
	 * @return false if 此客戶有選擇公司地址的客戶帳單註記，or return true
	 */
	private boolean isBillApplyFlagEq3(String idPSeqno) {
		
		strSql = " select count(*) as actAcnoOnlineCnt "
	             + " from act_acno_online as a , act_acno as b " 
				 + " where b.id_p_seqno =? "
				 + " and  a.acno_p_seqno =b.p_seqno "
				 + " and  a.acct_type = b.acct_type "
				 + " and  a.apr_flag='N' "
				 + " and  a.bill_apply_flag='3' "
				 ;
		
		sqlSelect(strSql, new Object[] { idPSeqno });
		if (colNum("actAcnoOnlineCnt") > 0) {
			return true;
		}
		
		strSql = " select count(*) as actAcnoCnt " 
	             + " from act_acno " 
				 + " where id_p_seqno =? "
				 + " and bill_apply_flag='3' ";
		
		sqlSelect(strSql, new Object[] { idPSeqno });
		if (colNum("actAcnoCnt") > 0) {
			return true;
		}
		
		return false;
	}
	
	public void cleanModCol() {
		for (int i = 0; i < aaCol.length; i++) {
			wp.colSet("mod_"+aaCol[i], "");
		}
		
		for (int i = 0; i < checkCol.length; i++) {
			wp.colSet("mod_"+checkCol[i], "");
		}

	}

}
