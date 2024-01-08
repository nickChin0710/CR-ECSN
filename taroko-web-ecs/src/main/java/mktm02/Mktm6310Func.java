/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/23  V1.00.00   Machao      Initial                              *
* 112/04/18  V1.00.01   Zuwei Su      新增錯誤                              *
* 112-05-09  V1.00.03   Ryan    新增國內外消費欄位、ATM手續費回饋加碼欄位，特店中文名稱、特店英文名稱參數維護                          
* 112-07-28  V1.00.04   Ryan    新增只計算加碼回饋欄位維護                                                                        *
***************************************************************************/
package mktm02;

import busi.FuncEdit;

import java.text.SimpleDateFormat;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6310Func extends FuncEdit {
  private String PROGNAME = "COMBO現金回饋參數檔維護(覆核)程式112/03/23  V1.00.00";
  String fundCode;
  String aprDate;
  String orgControlTabName = "ptr_combo_fundp";
  String controlTabName = "ptr_combo_fundp_t";

  public Mktm6310Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    String procTabName = "";
    procTabName = wp.itemStr("control_tab_name");
    strSql= " select " + " fund_code, " + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, " 
    		+ " stop_date, " + " stop_desc, " + " effect_months, " + " acct_type_sel, " + " merchant_sel, " 
    		+ " mcht_group_sel, "  + " platform_kind_sel, " + " group_card_sel, " + " group_code_sel, " 
    		+ " bl_cond, " + " ca_cond, " + " id_cond, " + " ao_cond, " + " it_cond, " + " ot_cond, " 
    		+ " fund_feed_flag, "  + " threshold_sel, " + " purchase_type_sel, " 
    		+ " purchse_s_amt_1, " + " purchse_e_amt_1, " + " purchse_rate_1, " 
    		+ " purchse_s_amt_2, " + " purchse_e_amt_2, " + " purchse_rate_2, " 
    		+ " purchse_s_amt_3, " + " purchse_e_amt_3, "  + " purchse_rate_3, " 
    		+ " purchse_s_amt_4, " + " purchse_e_amt_4, " + " purchse_rate_4, " 
    		+ " purchse_s_amt_5, " + " purchse_e_amt_5, " + " purchse_rate_5, " 
    		+ " save_s_amt_1, " + " save_e_amt_1, " + " save_rate_1, "  
    		+ " save_s_amt_2, " + " save_e_amt_2, " + " save_rate_2, " 
    		+ " save_s_amt_3, " + " save_e_amt_3, " + " save_rate_3, " 
    		+ " save_s_amt_4, " + " save_e_amt_4, " + " save_rate_4, " 
    		+ " save_s_amt_5, "  + " save_e_amt_5, " + " save_rate_5, " 
    		+ " feedback_lmt, " + " feedback_type, " + " card_feed_run_day, " + " cancel_period, " + " cancel_s_month, " 
    		+ " cancel_unbill_type, " + " cancel_unbill_rate, " + " cancel_event, "  + " apr_date, " 
    		+ " apr_flag, " + " apr_user, " + " crt_date, " + " crt_user, " 
    		+ " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, "
    		+ " foreign_code, "
            + " mcht_cname_sel, "
            + " mcht_ename_sel,"
            + " atmibwf_cond, "
            + " onlyaddon_calcond "
    		+ " from " + procTabName  + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
	  if (this.ibAdd) {
		  if (empty(wp.itemStr2("fund_code"))){
	          errmsg("請填入刷卡金代碼 !");
	          return;
	         }   
	     }
	  
    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("fund_name").length() == 0) {
        errmsg("刷卡金名稱：刷卡金名稱 不可空白!");
        return;
      }
      if (wp.itemStr("fund_crt_date_s").length() == 0) {
        errmsg("活動期間：活動期間-起 不可空白!");
        return;
      }
      if (!wp.itemEmpty("fund_crt_date_s")) {
          if (!dateStrIsValid(wp.itemStr("fund_crt_date_s"))) {
              errmsg("活動期間期間日期格式錯誤需為YYYYMMDD");
              return;
          }
      }
      if (!wp.itemEmpty("fund_crt_date_e")) {
          if (!dateStrIsValid(wp.itemStr("fund_crt_date_e"))) {
              errmsg("活動期間期間日期格式錯誤需為YYYYMMDD");
              return;
          }
      }

      if (!wp.itemEmpty("fund_crt_date_s") && !wp.itemEmpty("fund_crt_date_e")) {
          if (Integer.parseInt(wp.itemStr("fund_crt_date_s")) > Integer.parseInt(wp.itemStr("fund_crt_date_e"))) {
              errmsg("活動期間(迄日) >= (起日)");
              return;
          }
      }
      if (wp.itemStr("stop_flag").equals("Y")) {
			if (wp.itemEmpty("stop_date")) {
				errmsg("取消日期stop_date必輸入");
               return;
	             }
	         }else if (!wp.itemEmpty("stop_date")){
	        	 if (!dateStrIsValid(wp.itemStr("stop_date"))) {
	                 errmsg("取消日期格式錯誤需為 yyyymmdd");
	                 return;
	         }
		}
    }
    
    if(this.ibUpdate) {
        if ((wp.itemStr("mcht_cname_sel").equals("1"))||
                (wp.itemStr("mcht_cname_sel").equals("2")))
               {
                if (listParmDataCnt("MKT_PARM_CDATA_T"
                                      ,"PTR_COMBO_FUNDP"
                                      ,wp.colStr("fund_code")
                                      ,"A")==0)
                   {
                    errmsg("[特店中文名稱] 明細沒有設定, 筆數不可為 0  !");
                    return;
                   }
               }
            if ((wp.itemStr("mcht_ename_sel").equals("1"))||
                (wp.itemStr("mcht_ename_sel").equals("2")))
               {
                if (listParmDataCnt("MKT_PARM_CDATA_T"
                                      ,"PTR_COMBO_FUNDP"
                                      ,wp.colStr("fund_code")
                                      ,"B")==0)
                   {
                    errmsg("[特店英文名稱] 明細沒有設定, 筆數不可為 0  !");
                    return;
                   }
               }
    }

    if ((!wp.itemStr("control_tab_name").equals(orgControlTabName))
        && (wp.itemStr("aud_type").equals("A"))) {
      strSql = "select type_name " + " from vmkt_fund_name " + " where fund_code =  ? ";
      Object[] param = new Object[] {wp.itemStr("kk_fund_code")};
      sqlSelect(strSql, param);

      if (sqlRowNum > 0) {
        errmsg("[" + colStr("type_name") + "] 已使用本刷卡金代碼!");
        return;
      }
    }

//    if ((this.ibAdd) || (this.ibUpdate)) {
//      if (wp.itemStr("purchase_s_amt_1").length() == 0)
//        wp.itemSet("purchase_s_amt_1", "0");
//      if (wp.itemStr("purchase_e_amt_1").length() == 0)
//        wp.itemSet("purchase_e_amt_1", "0");
//      if (Double.parseDouble(wp.itemStr("purchase_s_amt_1")) > Double
//          .parseDouble(wp.itemStr("purchase_e_amt_1"))
//          && (Double.parseDouble(wp.itemStr("purchase_e_amt_1")) != 0)) {
//        errmsg("1.(" + wp.itemStr("purchase_s_amt_1") + ")>purchase_e_amt_1("
//            + wp.itemStr("purchase_e_amt_1") + ") 起迄值錯誤!");
//        return;
//      }
//    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_e_amt_1").length() == 0)
        wp.itemSet("purchase_e_amt_1", "0");
      if (wp.itemStr("purchase_s_amt_2").length() == 0)
        wp.itemSet("purchase_s_amt_2", "0");
      if (Double.parseDouble(wp.itemStr("purchase_e_amt_1")) >= Double
          .parseDouble(wp.itemStr("purchase_s_amt_2"))
          && (Double.parseDouble(wp.itemStr("purchase_s_amt_2")) != 0)) {
        errmsg("purchase_e_amt_1(" + wp.itemStr("purchase_e_amt_1") + ")>=purchase_s_amt_2("
            + wp.itemStr("purchase_s_amt_2") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_s_amt_2").length() == 0)
        wp.itemSet("purchase_s_amt_2", "0");
      if (wp.itemStr("purchase_e_amt_2").length() == 0)
        wp.itemSet("purchase_e_amt_2", "0");
      if (Double.parseDouble(wp.itemStr("purchase_s_amt_2")) > Double
          .parseDouble(wp.itemStr("purchase_e_amt_2"))
          && (Double.parseDouble(wp.itemStr("purchase_e_amt_2")) != 0)) {
        errmsg("2.(" + wp.itemStr("purchase_s_amt_2") + ")>purchase_e_amt_2("
            + wp.itemStr("purchase_e_amt_2") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_e_amt_2").length() == 0)
        wp.itemSet("purchase_e_amt_2", "0");
      if (wp.itemStr("purchase_s_amt_3").length() == 0)
        wp.itemSet("purchase_s_amt_3", "0");
      if (Double.parseDouble(wp.itemStr("purchase_e_amt_2")) >= Double
          .parseDouble(wp.itemStr("purchase_s_amt_3"))
          && (Double.parseDouble(wp.itemStr("purchase_s_amt_3")) != 0)) {
        errmsg("purchase_e_amt_2(" + wp.itemStr("purchase_e_amt_2") + ")>=purchase_s_amt_3("
            + wp.itemStr("purchase_s_amt_3") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_s_amt_3").length() == 0)
        wp.itemSet("purchase_s_amt_3", "0");
      if (wp.itemStr("purchase_e_amt_3").length() == 0)
        wp.itemSet("purchase_e_amt_3", "0");
      if (Double.parseDouble(wp.itemStr("purchase_s_amt_3")) > Double
          .parseDouble(wp.itemStr("purchase_e_amt_3"))
          && (Double.parseDouble(wp.itemStr("purchase_e_amt_3")) != 0)) {
        errmsg("3.(" + wp.itemStr("purchase_s_amt_3") + ")>purchase_e_amt_3("
            + wp.itemStr("purchase_e_amt_3") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_e_amt_3").length() == 0)
        wp.itemSet("purchase_e_amt_3", "0");
      if (wp.itemStr("purchase_s_amt_4").length() == 0)
        wp.itemSet("purchase_s_amt_4", "0");
      if (Double.parseDouble(wp.itemStr("purchase_e_amt_3")) >= Double
          .parseDouble(wp.itemStr("purchase_s_amt_4"))
          && (Double.parseDouble(wp.itemStr("purchase_s_amt_4")) != 0)) {
        errmsg("purchase_e_amt_3(" + wp.itemStr("purchase_e_amt_3") + ")>=purchase_s_amt_4("
            + wp.itemStr("purchase_s_amt_4") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_s_amt_4").length() == 0)
        wp.itemSet("purchase_s_amt_4", "0");
      if (wp.itemStr("purchase_e_amt_4").length() == 0)
        wp.itemSet("purchase_e_amt_4", "0");
      if (Double.parseDouble(wp.itemStr("purchase_s_amt_4")) > Double
          .parseDouble(wp.itemStr("purchase_e_amt_4"))
          && (Double.parseDouble(wp.itemStr("purchase_e_amt_4")) != 0)) {
        errmsg("4.(" + wp.itemStr("purchase_s_amt_4") + ")>purchase_e_amt_4("
            + wp.itemStr("purchase_e_amt_4") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_e_amt_4").length() == 0)
        wp.itemSet("purchase_e_amt_4", "0");
      if (wp.itemStr("purchase_s_amt_5").length() == 0)
        wp.itemSet("purchase_s_amt_5", "0");
      if (Double.parseDouble(wp.itemStr("purchase_e_amt_4")) >= Double
          .parseDouble(wp.itemStr("purchase_s_amt_5"))
          && (Double.parseDouble(wp.itemStr("purchase_s_amt_5")) != 0)) {
        errmsg("purchase_e_amt_4(" + wp.itemStr("purchase_e_amt_4") + ")>=purchase_s_amt_5("
            + wp.itemStr("purchase_s_amt_5") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_s_amt_5").length() == 0)
        wp.itemSet("purchase_s_amt_5", "0");
      if (wp.itemStr("purchase_e_amt_5").length() == 0)
        wp.itemSet("purchase_e_amt_5", "0");
      if (Double.parseDouble(wp.itemStr("purchase_s_amt_5")) > Double
          .parseDouble(wp.itemStr("purchase_e_amt_5"))
          && (Double.parseDouble(wp.itemStr("purchase_e_amt_5")) != 0)) {
        errmsg("5.(" + wp.itemStr("purchase_s_amt_5") + ")>purchase_e_amt_5("
            + wp.itemStr("purchase_e_amt_5") + ") 起迄值錯誤!");
        return;
      }
    }
    
    if ((this.ibAdd) || (this.ibUpdate)) {
    	if(wp.itemEq("feedback_type","1")) {
    		if(wp.itemEmpty("card_feed_run_day")) {
    			errmsg("回饋執行日期不可空值");
    			return;
    		}
    		String sqlCmd = "select count(*) workday_cnt from ptr_workday where stmt_cycle = ? ";
       	 	Object[] param = new Object[] {wp.itemStr("card_feed_run_day")};
       	 	sqlSelect(sqlCmd,param);
       	 	if(colInt("workday_cnt")>0) {
       	 		errmsg("回饋執行日期不可等於cycle day");
       	 		return;
       	 	}
    	}
    	
    }

//    if (checkDecnum(wp.itemStr("purchase_s_amt_1"), 11, 3) != 0) {
//      errmsg("1. 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_e_amt_1"), 11, 3) != 0) {
//      errmsg(" 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_rate_1"), 3, 2) != 0) {
//      errmsg(" 格式超出範圍 : [3][2]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_s_amt_2"), 11, 3) != 0) {
//      errmsg("2. 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_e_amt_2"), 11, 3) != 0) {
//      errmsg(" 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_rate_2"), 3, 2) != 0) {
//      errmsg(" 格式超出範圍 : [3][2]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_s_amt_3"), 11, 3) != 0) {
//      errmsg("3. 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_e_amt_3"), 11, 3) != 0) {
//      errmsg(" 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_rate_3"), 3, 2) != 0) {
//      errmsg(" 格式超出範圍 : [3][2]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_s_amt_4"), 11, 3) != 0) {
//      errmsg("4. 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_e_amt_4"), 11, 3) != 0) {
//      errmsg(" 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_rate_4"), 3, 2) != 0) {
//      errmsg(" 格式超出範圍 : [3][2]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_s_amt_5"), 11, 3) != 0) {
//      errmsg("5. 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_e_amt_5"), 11, 3) != 0) {
//      errmsg(" 格式超出範圍 : [11][3]");
//      return;
//    }
//
//    if (checkDecnum(wp.itemStr("purchase_rate_5"), 3, 2) != 0) {
//      errmsg(" 格式超出範圍 : [3][2]");
//      return;
//    }

    if (checkDecnum(wp.itemStr("feedback_lmt"), 12, 2) != 0) {
      errmsg("回饋上限： 格式超出範圍 : [12][2]");
      return;
    }
    
    if (!wp.itemStr2("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
	if (!wp.itemStr2("bl_cond").equals("Y")) wp.itemSet("bl_cond","N");
	if (!wp.itemStr2("ca_cond").equals("Y")) wp.itemSet("ca_cond","N");
	if (!wp.itemStr2("id_cond").equals("Y")) wp.itemSet("id_cond","N");
	if (!wp.itemStr2("ao_cond").equals("Y")) wp.itemSet("ao_cond","N");
	if (!wp.itemStr2("it_cond").equals("Y")) wp.itemSet("it_cond","N");
    if (!wp.itemStr2("ot_cond").equals("Y")) wp.itemSet("ot_cond","N");
    if (!wp.itemStr2("atmibwf_cond").equals("Y")) wp.itemSet("atmibwf_cond","N");
    if (!wp.itemStr2("onlyaddon_calcond").equals("Y")) wp.itemSet("onlyaddon_calcond","N");
    
    if ((this.ibAdd) || (this.ibUpdate)) {
        aprDate =  wp.itemStr2("apr_date");
//        	String[] aprdate = aprDate.split("/");
//        	aprDate = aprdate[0] + aprdate[1] + aprdate[2];
        if (aprDate == null) {
            aprDate = "";
        } else {
            aprDate = aprDate.replaceAll("/", "");
        }
    }

    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD2T();
    dbInsertI2T();
    dbInsertD5T();
    dbInsertI5T();

    strSql= " insert into  " + controlTabName+ " (" + " aud_type, "
    		+ " fund_code, " + " fund_name, " + " fund_crt_date_s, " + " fund_crt_date_e, " 
    		+ " stop_flag, " + " stop_date, " + " stop_desc, " + " effect_months, " 
    		+ " acct_type_sel, " + " merchant_sel, " + " mcht_group_sel, "  + " platform_kind_sel, " 
    		+ " group_card_sel, " + " group_code_sel, " + " mcc_code_sel, " //16
    		+ " bl_cond, " + " ca_cond, " + " id_cond, " + " ao_cond, " + " it_cond, " + " ot_cond, " 
    		+ " fund_feed_flag, "  + " threshold_sel, " + " purchase_type_sel, " //25
//    		+ " purchse_s_amt_1, " + " purchse_e_amt_1, " + " purchse_rate_1, " 
//    		+ " purchse_s_amt_2, " + " purchse_e_amt_2, " + " purchse_rate_2, " 
//    		+ " purchse_s_amt_3, " + " purchse_e_amt_3, "  + " purchse_rate_3, " 
//    		+ " purchse_s_amt_4, " + " purchse_e_amt_4, " + " purchse_rate_4, " 
//    		+ " purchse_s_amt_5, " + " purchse_e_amt_5, " + " purchse_rate_5, " //40
    		+ " save_s_amt_1, " + " save_e_amt_1, " + " save_rate_1, "  
    		+ " save_s_amt_2, " + " save_e_amt_2, " + " save_rate_2, " 
    		+ " save_s_amt_3, " + " save_e_amt_3, " + " save_rate_3, " 
    		+ " save_s_amt_4, " + " save_e_amt_4, " + " save_rate_4, " 
    		+ " save_s_amt_5, "  + " save_e_amt_5, " + " save_rate_5, " //55
    		+ " feedback_lmt, " + " feedback_type, " + " card_feed_run_day, " 
    		+ " cancel_period, " + " cancel_s_month, " + " cancel_unbill_type, " 
    		+ " cancel_unbill_rate, " + " cancel_event, "  //63
    		+ " apr_date, " + " apr_flag, " 
    		+ " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, " + " mod_user, " 
    		+ " mod_time, " + " mod_pgm, " 
            + " foreign_code,"
            + " mcht_cname_sel, "
            + " mcht_ename_sel,"
            + " atmibwf_cond, "
            + " onlyaddon_calcond "//76
    		+ " ) values (" 
    		+ "?,?,?,?,?,?,?,?,?,?,?,?," //12
    		+ "?,?,?,?,?,?,?,?,?,?,?," //11
    		+ "'2','1',"
//    		+ "?,?,?,?,?,?,?,?," //10
//    		+ "?,?,?,?,?,?,?,"
    		+ "?,?,?," //10
    		+ "?,?,?,?,?,?,?,?,?,?," //10
    		+ "?,?,?,?,?,?,?,?,?,?," //10
    		+ "?,'N',?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?," + "sysdate,?,?,?,?,?,?)";//13

	  Object[] param =new Object[]
	       {
	    	wp.itemStr2("aud_type"),	   
	        wp.itemStr2("fund_code"),
	        wp.itemStr2("fund_name"),
	        wp.itemStr2("fund_crt_date_s"),
	        wp.itemStr2("fund_crt_date_e"),
	        wp.itemStr2("stop_flag"),
	        wp.itemStr2("stop_date"),
	        wp.itemStr2("stop_desc"),
	        wp.itemNum("effect_months"),
	        wp.itemStr2("acct_type_sel"),//10
	        wp.itemStr2("merchant_sel"),
	        wp.itemStr2("mcht_group_sel"),
	        wp.itemStr2("platform_kind_sel"),
	        wp.itemStr2("group_card_sel"),
	        wp.itemStr2("group_code_sel"),
	        wp.itemStr2("mcc_code_sel"),
	        wp.itemStr2("bl_cond"),
	        wp.itemStr2("ca_cond"),
	        wp.itemStr2("id_cond"),
	        wp.itemStr2("ao_cond"),
	        wp.itemStr2("it_cond"),
	        wp.itemStr2("ot_cond"),//22
	        wp.itemStr2("fund_feed_flag"),
	        //24
	        //25
//	        wp.itemNum("purchase_s_amt_1"),
//	        wp.itemNum("purchase_e_amt_1"),
//	        wp.itemNum("purchase_rate_1"),
//	        wp.itemNum("purchase_s_amt_2"),
//	        wp.itemNum("purchase_e_amt_2"),
//	        wp.itemNum("purchase_rate_2"),
//	        wp.itemNum("purchase_s_amt_3"),
//	        wp.itemNum("purchase_e_amt_3"),
//	        wp.itemNum("purchase_rate_3"), 
//	        wp.itemNum("purchase_s_amt_4"),
//	        wp.itemNum("purchase_e_amt_4"),
//	        wp.itemNum("purchase_rate_4"), 
//	        wp.itemNum("purchase_s_amt_5"),
//	        wp.itemNum("purchase_e_amt_5"),
//	        wp.itemNum("purchase_rate_5"), //40
	        wp.itemNum("save_s_amt_1"),
	        wp.itemNum("save_e_amt_1"),
	        wp.itemNum("save_rate_1"),
	        wp.itemNum("save_s_amt_2"),
	        wp.itemNum("save_e_amt_2"),
	        wp.itemNum("save_rate_2"),
	        wp.itemNum("save_s_amt_3"),
	        wp.itemNum("save_e_amt_3"),
	        wp.itemNum("save_rate_3"),
	        wp.itemNum("save_s_amt_4"),
	        wp.itemNum("save_e_amt_4"),
	        wp.itemNum("save_rate_4"),
	        wp.itemNum("save_s_amt_5"),
	        wp.itemNum("save_e_amt_5"),
	        wp.itemNum("save_rate_5"),//55
	        wp.itemNum("feedback_lmt"),
	        wp.itemStr2("feedback_type"),
	        wp.itemNum("card_feed_run_day"),
	        wp.itemStr2("cancel_period"),
	        wp.itemStr2("cancel_s_month"),
	        wp.itemStr2("cancel_unbill_type"),
	        wp.itemNum("cancel_unbill_rate"),
	        wp.itemStr2("cancel_event"),//63
	        aprDate,
	        //65
	        wp.itemStr2("apr_user"),
	        //67
	        wp.loginUser,
	        wp.modSeqno(),
	        wp.loginUser,
	        //71
	        wp.modPgm(),
	        wp.itemStr2("foreign_code"),
	        wp.itemStr2("mcht_cname_sel"),
	        wp.itemStr2("mcht_ename_sel"),
	        wp.itemStr2("atmibwf_cond"),//76
	        wp.itemStr2("onlyaddon_calcond")
	       };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into MKT_PARM_DATA_T " + "select * " + "from MKT_PARM_DATA "
        + "where table_name  =  'PTR_COMBO_FUNDP' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("fund_code"),};

    sqlExec(strSql, param);


    return 1;
  }
  
//************************************************************************
public int dbInsertI5T()
{
 msgOK();

strSql = "insert into MKT_PARM_CDATA_T "
       + "select * "
       + "from MKT_PARM_CDATA "
       + "where table_name  =  'PTR_COMBO_FUNDP' "
       + "and   data_key = ? "
       + "";

 Object[] param =new Object[]
   {
    wp.itemStr2("fund_code"),
   };

wp.dupRecord = "Y";
sqlExec(strSql, param , false);


 return 1;
}

  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "aud_type = ?, " + "fund_name = ?, " + "fund_crt_date_s = ?, "
        + "fund_crt_date_e = ?, " + "stop_flag = ?, " + "stop_date = ?, " + "stop_desc = ?, " + "effect_months = ?, " 
        + "acct_type_sel = ?, " + "merchant_sel = ?, " + "mcht_group_sel = ?, " + "platform_kind_sel = ?, "
        + "group_card_sel = ?, " + "group_code_sel = ?, " + "mcc_code_sel = ?, "
        + "bl_cond = ?, " + "ca_cond = ?, " + "id_cond = ?, " + "ao_cond = ?, " + "it_cond = ?, " + "ot_cond = ?, "
        + "fund_feed_flag = ?, " + "threshold_sel = ?, " + "purchase_type_sel = ?, "
//        + "purchse_s_amt_1 = ?, " + "purchse_e_amt_1 = ?, " + "purchse_rate_1 = ?, " 
//        + "purchse_s_amt_2 = ?, " + "purchse_e_amt_2 = ?, " + "purchse_rate_2 = ?, " 
//        + "purchse_s_amt_3 = ?, " + "purchse_e_amt_3 = ?, " + "purchse_rate_3 = ?, "
//        + "purchse_s_amt_4 = ?, " + "purchse_e_amt_4 = ?, " + "purchse_rate_4 = ?, " 
//        + "purchse_s_amt_5 = ?, " + "purchse_e_amt_5 = ?, " + "purchse_rate_5 = ?, " 
        + "save_s_amt_1 = ?, " + "save_e_amt_1 = ?, " + "save_rate_1 = ?, "
        + "save_s_amt_2 = ?, " + "save_e_amt_2 = ?, " + "save_rate_2 = ?, "
        + "save_s_amt_3 = ?, " + "save_e_amt_3 = ?, " + "save_rate_3 = ?, "
        + "save_s_amt_4 = ?, " + "save_e_amt_4 = ?, " + "save_rate_4 = ?, "
        + "save_s_amt_5 = ?, " + "save_e_amt_5 = ?, " + "save_rate_5 = ?, "
        + "feedback_lmt = ?, " + "feedback_type = ?, " + "card_feed_run_day = ?, " + "cancel_period = ?, " 
        + "cancel_s_month = ?, " + "cancel_unbill_type = ?, " + "cancel_unbill_rate = ?, " + "cancel_event = ?, " 
        + "crt_user  = ?, "+ "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ?, "
        + "group_oppost_cond = ?, "
        + "foreign_code = ?, "
        + "mcht_cname_sel = ?, "
        + "mcht_ename_sel = ?,"
        + "atmibwf_cond = ?, "
        + "onlyaddon_calcond = ? "
        + "where fund_code = ? ";

    Object[] param = new Object[] {wp.itemStr("aud_type"),wp.itemStr("fund_name"), wp.itemStr("fund_crt_date_s"),
        wp.itemStr("fund_crt_date_e"), wp.itemStr("stop_flag"), wp.itemStr("stop_date"),
        wp.itemStr("stop_desc"), wp.itemStr("effect_months"), 
        wp.itemStr("acct_type_sel"), wp.itemStr("merchant_sel"),wp.itemStr("mcht_group_sel"),
        wp.itemStr("platform_kind_sel"), wp.itemStr("group_card_sel"), wp.itemStr("group_code_sel"),wp.itemStr("mcc_code_sel"),
        wp.itemStr2("bl_cond"),wp.itemStr2("ca_cond"), wp.itemStr2("id_cond"),
        wp.itemStr2("ao_cond"), wp.itemStr2("it_cond"), wp.itemStr2("ot_cond"),
        wp.itemStr("fund_feed_flag"), wp.itemStr("threshold_sel"), wp.itemStr("purchase_type_sel"),
//        wp.itemNum("purchase_s_amt_1"), wp.itemNum("purchase_e_amt_1"), wp.itemNum("purchase_rate_1"),
//        wp.itemNum("purchase_s_amt_2"), wp.itemNum("purchase_e_amt_2"), wp.itemNum("purchase_rate_2"),
//        wp.itemNum("purchase_s_amt_3"), wp.itemNum("purchase_e_amt_3"), wp.itemNum("purchase_rate_3"),
//        wp.itemNum("purchase_s_amt_4"), wp.itemNum("purchase_e_amt_4"), wp.itemNum("purchase_rate_4"),
//        wp.itemNum("purchase_s_amt_5"), wp.itemNum("purchase_e_amt_5"), wp.itemNum("purchase_rate_5"),
        wp.itemNum("save_s_amt_1"), wp.itemNum("save_e_amt_1"), wp.itemNum("save_rate_1"),
        wp.itemNum("save_s_amt_2"), wp.itemNum("save_e_amt_2"), wp.itemNum("save_rate_2"),
        wp.itemNum("save_s_amt_3"), wp.itemNum("save_e_amt_3"), wp.itemNum("save_rate_3"),
        wp.itemNum("save_s_amt_4"), wp.itemNum("save_e_amt_4"), wp.itemNum("save_rate_4"),
        wp.itemNum("save_s_amt_5"), wp.itemNum("save_e_amt_5"), wp.itemNum("save_rate_5"),
        
        wp.itemNum("feedback_lmt"), wp.itemStr("feedback_type"), wp.itemStr("card_feed_run_day"),
        wp.itemStr("cancel_period"), wp.itemStr("cancel_s_month"), wp.itemStr("cancel_unbill_type"),
        wp.itemNum("cancel_unbill_rate"), wp.itemStr("cancel_event"), wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm"),wp.itemStr("group_oppost_cond"), 
        wp.itemStr2("foreign_code"),
        wp.itemStr("mcht_cname_sel"),
        wp.itemStr("mcht_ename_sel"),
        wp.itemStr("atmibwf_cond"),
        wp.itemStr("onlyaddon_calcond"),
        wp.itemStr("fund_code")
        };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    dbInsertD2T();

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }

  // ************************************************************************
  public int dbInsertD2T() {
    msgOK();

    strSql = "delete MKT_PARM_DATA_T " + " where table_name  =  'PTR_COMBO_FUNDP' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("fund_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_PARM_DATA_T 錯誤");

    return rc;

  }
//************************************************************************
public int dbInsertD5T()
{
 msgOK();

 strSql = "delete MKT_PARM_CDATA_T "
       + " where table_name  =  'PTR_COMBO_FUNDP' "
        + "and   data_key = ? "
        + "";
 //如果沒有資料回傳成功1
 Object[] param = new Object[]
   {
    wp.itemStr2("fund_code"),
   };

 sqlExec(strSql,param,false);
 if (sqlRowNum <= 0) rc=0;else rc=1;

 if (rc!=1) errmsg("刪除 MKT_PARM_CDATA_T 錯誤");

 return rc;

}
  // ************************************************************************
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6310_acty"))
      dataType = "3";
    if (wp.respHtml.equals("mktm6310_mrch"))
      dataType = "4";
    if (wp.respHtml.equals("mktm6310_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm6310_aaa2"))
      dataType = "P";
    if (wp.respHtml.equals("mktm6310_gpcd"))
        dataType = "1";
    if (wp.respHtml.equals("mktm6310_grcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6310_mccd"))
        dataType = "8";
    strSql = "insert into MKT_PARM_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'PTR_COMBO_FUNDP', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("fund_code"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_PARM_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6310_acty"))
        dataType = "3";
      if (wp.respHtml.equals("mktm6310_mrch"))
        dataType = "4";
      if (wp.respHtml.equals("mktm6310_aaa1"))
        dataType = "6";
      if (wp.respHtml.equals("mktm6310_aaa2"))
        dataType = "P";
      if (wp.respHtml.equals("mktm6310_gpcd"))
          dataType = "1";
      if (wp.respHtml.equals("mktm6310_grcd"))
        dataType = "2";
//      if (wp.respHtml.equals("mktm6310_mccd"))
//          dataType = "8";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("fund_code")};
    if (sqlRowcount("MKT_PARM_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'PTR_COMBO_FUNDP' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_PARM_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'PTR_COMBO_FUNDP'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6310_acty"))
        dataType = "3";
      if (wp.respHtml.equals("mktm6310_mrch"))
        dataType = "4";
      if (wp.respHtml.equals("mktm6310_aaa1"))
        dataType = "6";
      if (wp.respHtml.equals("mktm6310_aaa2"))
        dataType = "P";
      if (wp.respHtml.equals("mktm6310_gpcd"))
          dataType = "1";
      if (wp.respHtml.equals("mktm6310_grcd"))
        dataType = "2";
      if (wp.respHtml.equals("mktm6310_mccd"))
          dataType = "8";
    strSql = "insert into MKT_PARM_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'PTR_COMBO_FUNDP', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("fund_code"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_PARM_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6310_acty"))
        dataType = "3";
      if (wp.respHtml.equals("mktm6310_mrch"))
        dataType = "4";
      if (wp.respHtml.equals("mktm6310_aaa1"))
        dataType = "6";
      if (wp.respHtml.equals("mktm6310_aaa2"))
        dataType = "P";
      if (wp.respHtml.equals("mktm6310_gpcd"))
          dataType = "1";
      if (wp.respHtml.equals("mktm6310_grcd"))
        dataType = "2";
//      if (wp.respHtml.equals("mktm6310_mccd"))
//          dataType = "8";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("fund_code")};
    if (sqlRowcount("MKT_PARM_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'PTR_COMBO_FUNDP' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_PARM_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'PTR_COMBO_FUNDP'  ";
    sqlExec(strSql, param);


    return 1;

  }
//************************************************************************
public int dbInsertI5() throws Exception
{
 msgOK();

 String dataType="";
 if (wp.respHtml.equals("mktm6310_namc"))
    dataType = "A" ;
 if (wp.respHtml.equals("mktm6310_name"))
    dataType = "B" ;
strSql = "insert into MKT_PARM_CDATA_T ( "
        + "table_name, "
        + "data_type, "
        + "data_key,"
        + "data_code,"
        + "crt_date, "
        + "crt_user, "
        + " mod_time, "
        + " mod_user, "
        + " mod_pgm "
        + ") values ("
        + "'PTR_COMBO_FUNDP', "
        + "?, "
        + "?,?," 
        + "to_char(sysdate,'yyyymmdd'),"
        + "?,"
        + " sysdate, "
        + "?,"
        + " ? "
        + ")";

 Object[] param =new Object[]
   {
    dataType, 
    wp.itemStr("fund_code"),
    varsStr("data_code"),
    wp.loginUser,
      wp.loginUser,
    wp.modPgm()
   };

 wp.dupRecord = "Y";
 sqlExec(strSql, param , false);
 if (sqlRowNum <= 0) rc=0;else rc=1;

 if (rc!=1) errmsg("新增8 MKT_PARM_CDATA_T 錯誤");
 else dbUpdateMainU5();

 return rc;
}
//************************************************************************
public int dbUpdateMainU5() throws Exception
{
// TODO Auto-update main 
return rc;
}
//************************************************************************
public int dbDeleteD5()
{
msgOK();

String dataType="";
if (wp.respHtml.equals("mktm6310_namc"))
   dataType = "A" ;
if (wp.respHtml.equals("mktm6310_name"))
   dataType = "B" ;
//如果沒有資料回傳成功2
Object[] param = new Object[]
  {
   dataType, 
   wp.itemStr("fund_code")
  };
if (sqlRowcount("MKT_PARM_CDATA_T" 
                 , "where data_type = ? "
                + "and   data_key = ? "
                 + "and   table_name = 'PTR_COMBO_FUNDP' "
                 , param) <= 0)
    return 1;

strSql = "delete MKT_PARM_CDATA_T "
       + "where data_type = ? "
       + "and   data_key = ?  "
       + "and   table_name = 'PTR_COMBO_FUNDP'  "
       ;
sqlExec(strSql,param,false);


return 1;

}
  // ************************************************************************
  public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long list_cnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < list_cnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < list_cnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < list_cnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa1(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"PTR_COMBO_FUNDP", wp.itemStr("fund_code"), "4"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg) {
    dateTime();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    comr.setConn(wp);

    if (!comm.isNumber(errMsg[10]))
      errMsg[10] = "0";
    if (!comm.isNumber(errMsg[1]))
      errMsg[1] = "0";
    if (!comm.isNumber(errMsg[2]))
      errMsg[2] = "0";

    strSql = " insert into ecs_media_errlog (" + " crt_date, " + " crt_time, " + " file_name, "
        + " unit_code, " + " main_desc, " + " error_seq, " + " error_desc, " + " line_seq, "
        + " column_seq, " + " column_data, " + " trans_seqno, " + " column_desc, "
        + " program_code, " + " mod_time, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?," // 10
                                                                                                   // record
        + "?,?,?," // 4 trvotfd
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, wp.itemStr("zz_file_name"),
        comr.getObjectOwner("3", wp.modPgm()), errMsg[0], Integer.valueOf(errMsg[1]), errMsg[4],
        Integer.valueOf(errMsg[10]), Integer.valueOf(errMsg[2]), errMsg[3], tranSeqStr, errMsg[5],
        wp.modPgm(), wp.sysDate + wp.sysTime, wp.modPgm()};

    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_media_errlog 錯誤");

    return rc;
  }

  // ************************************************************************
  public boolean dateStrIsValid(String dateString) {
      if (dateString == null || dateString.length() != 8) {
          return false;
      }
      SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd");
      Date date;
      try {
          date = dateTimeFormatter.parse(dateString);
          return dateString.equals(dateTimeFormatter.format(date));
      } catch (Exception e) {
          return false;
      }
  }	 
	// ************************************************************************
  public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt) {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    dateTime();
    strSql = " insert into ecs_notify_log (" + " crt_date, " + " crt_time, " + " unit_code, "
        + " obj_type, " + " notify_head, " + " notify_name, " + " notify_desc1, "
        + " notify_desc2, " + " trans_seqno, " + " mod_time, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?," // 9 record
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, comr.getObjectOwner("3", wp.modPgm()),
        "3", "媒體檔轉入資料有誤(只記錄前100筆)", "媒體檔名:" + wp.itemStr("zz_file_name"),
        "程式 " + wp.modPgm() + " 轉 " + wp.itemStr("zz_file_name") + " 有" + errorCnt + " 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤", tranSeqStr, wp.sysDate + wp.sysTime, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_modify_log 錯誤");
    return rc;
  }
//************************************************************************
int listParmDataCnt(String s1,String s2,String s3,String s4) 
{
 String isSql = "select count(*) as data_cnt "
               + "from  " + s1 +" "
               + " where table_name = ? "
               + " and   data_key   = ? "
               + " and   data_type  = ? "
               ;
 Object[] param = new Object[] {s2,s3,s4};
 sqlSelect(isSql,param);

 return(Integer.parseInt(colStr("data_cnt")));
}
// ************************************************************************

}  // End of class
