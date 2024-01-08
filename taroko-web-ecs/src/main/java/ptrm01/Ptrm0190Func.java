/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-04  V1.00.00  yash       program initial                            *
* 109-01-09  V1.00.01  Ru         add crd_makecard_fee                       *
* 109-04-20  V1.00.02  Tanwei     updated for project coding standard        *
* 109-06-01  V1.00.03  Wilson     註解來源代號前兩碼不可重複                                                                      *
* 109-12-07  V1.00.04  Ryan       指定消費增加必輸條件                                                                      *
* 111-01-20  V1.00.05  Ryan       修正 update insert bug                      *
* 111-02-16  V1.00.06  Ryan       修正 update bug                      *
* 111-12-28  V1.00.07  Ryan       新增rcrate_day欄位 ,新增日利率計算邏輯                                      *
* 112-02-06  V1.00.08  Ryan       新增ibank_apply_flag欄位                                      *
* 112-05-29  V1.00.09  Ryan       新增資料bug修正                                                                                       *
******************************************************************************/
package ptrm01;

import java.math.BigDecimal;

import busi.FuncEdit; 
import taroko.com.TarokoCommon;

public class Ptrm0190Func extends FuncEdit {
  String mKkGroupCode = "";
  static final Double LEGAL_RCRATE_YEAR = 15.0; //法定年利率
  Double rcrateDay = Double.valueOf(0.0D);
  public Ptrm0190Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      mKkGroupCode = wp.itemStr("kk_group_code");

//      strSql = "select count(*) as tot_cnt from Ptr_group_code where group_abbr_code= ?";
//      Object[] param1 = new Object[] {wp.itemStr("group_abbr_code")};
//      sqlSelect(strSql, param1);
//      if (colNum("tot_cnt") > 0) {
//        errmsg("來源代號前兩碼不可重複 ,請重新輸入!");
//        return;
//      }

//      strSql = "select count(*) as tot_cnt from Ptr_group_code where group_order= ?";
//      Object[] param2 = new Object[] {wp.itemStr("group_order")};
//      sqlSelect(strSql, param2);
//      if (colNum("tot_cnt") > 0) {
//        errmsg("優先順序碼不可重複 ,請重新輸入!");
//        return;
//      }

    }
    
    if(!this.ibDelete){
    	if(wp.itemEq("purchase_card_flag","Y")){
    		if(wp.itemEmpty("cca_group_mcht_chk")){
    		     errmsg("採購卡註記為Y時，請指定消費!");
    		     return;
    		}
    	}
    	if(wp.itemEq("member_flag", "Y")) {
    		if(wp.itemEmpty("member_corp_no")){
   		     errmsg("聯名機構卡註記為Y時，聯名機構統一編號不能為空白");
   		     return;
    		}
    	}
    }

    if (this.ibUpdate) {

      mKkGroupCode = wp.itemStr("group_code");

      strSql = "select group_abbr_code,group_order from Ptr_group_code where group_code= ?";
      Object[] param = new Object[] {mKkGroupCode};
      sqlSelect(strSql, param);
//      String groupCode = colStr("group_abbr_code");
      String groupOrder = colStr("group_order");

//      if (!groupCode.equals(wp.itemStr("group_abbr_code"))) {
//        strSql = "select count(*) as tot_cnt from Ptr_group_code where group_abbr_code= ?";
//        Object[] param1 = new Object[] {wp.itemStr("group_abbr_code")};
//        sqlSelect(strSql, param1);
//        if (colNum("tot_cnt") > 0) {
//          errmsg("來源代號前兩碼不可重複 ,請重新輸入!");
//          return;
//        }
//      }

//      if (!groupOrder.equals(wp.itemStr("group_order"))) {
//        strSql = "select count(*) as tot_cnt from Ptr_group_code where group_order= ?";
//        Object[] param2 = new Object[] {wp.itemStr("group_order")};
//        sqlSelect(strSql, param2);
//        if (colNum("tot_cnt") > 0) {
//          errmsg("優先順序碼不可重複 ,請重新輸入!");
//          return;
//        }
//      }


    }

    if(!this.ibDelete && !wp.itemEmpty("revolve_int_rate_year")) {
        // 頁面上來的年利率
        Double rcrateYear = new BigDecimal(wp.itemStr("revolve_int_rate_year")).
                                      setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    	if (checkSmallerLegelRcrateYear(rcrateYear)==false) {
              return;
    	}
        // 轉換為日利率(加碼)
        Double rcrateDayPlus = calculateRcratePlus(rcrateYear);
        
        // 取得DB中最大的rcrate_day
        Double maxRcrateDayPlus = getMaxRcrateDayPlus();
        
        //計算日利率
        rcrateDay = calculateRcrate(rcrateDayPlus, maxRcrateDayPlus);
        if (rcrateDay == null) {
        	return;
        }
    }
    

    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from ptr_group_code where group_code = ? ";
      Object[] param = new Object[] {mKkGroupCode};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    }

    if (this.ibDelete) {
      mKkGroupCode = wp.itemStr("group_code");
    }

    // -other modify-
    sqlWhere = " where group_code= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mKkGroupCode, wp.modSeqno()};
    if (this.isOtherModify("ptr_group_code", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into ptr_group_code (" + "  group_code " + ", group_abbr_code" + ", group_name"
        + ", group_order" + ", combo_indicator" + ", co_member_flag" + ", emboss_data"
        + ", auto_installment" + ", co_member_type" + ", bill_form_seq" + ", assign_installment"
        + ", crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno"
        + ", special_card_rate_flag" + ", revolve_int_rate_year" + ", crd_makecard_fee_flag"
        + ", crd_makecard_fee" + ", cca_group_mcht_chk " + ", purchase_card_flag " + ",rcrate_day"
        + ", member_flag ,member_corp_no,ibank_apply_flag "
        + " ) values (" + " ?,?,?,?,?,?,?,?,?,?,? "//11
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1,?,?,?,?,?,?,?,?,?,?" + " )";//23
    // -set ?value-
    Object[] param = new Object[] {mKkGroupCode // 1
        , wp.itemStr("group_abbr_code"), wp.itemStr("group_name"), wp.itemStr("group_order"),
        wp.itemStr("combo_indicator"), wp.itemStr("co_member_flag").equals("Y") ? "Y" : "N",
        wp.itemStr("emboss_data"), wp.itemStr("auto_installment").equals("Y") ? "Y" : "N",
        wp.itemStr("co_member_type"), wp.itemStr("bill_form_seq"),
        wp.itemStr("assign_installment").equals("Y") ? "Y" : "N", wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemStr("special_card_rate_flag"),
//        wp.itemStr("revolve_int_rate_year").equals("") ? null : wp.itemStr("revolve_int_rate_year"),
        wp.itemEmpty("revolve_int_rate_year") ? "0" : wp.itemStr("revolve_int_rate_year"),
        wp.itemStr("crd_makecard_fee_flag"),
//        wp.itemStr("crd_makecard_fee").equals("") ? null : wp.itemStr("crd_makecard_fee"),
        wp.itemEmpty("crd_makecard_fee") ? "0" : wp.itemStr("crd_makecard_fee"),
        wp.itemStr("cca_group_mcht_chk"),
        wp.itemStr("purchase_card_flag"),
        rcrateDay.doubleValue(),
        wp.itemStr("member_flag"),
        wp.itemStr("member_corp_no"),
        wp.itemEq("ibank_apply_flag", "Y")?"Y":"N"
    };
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "update ptr_group_code set " + "  group_abbr_code =?" + ", group_name =?"
        + ", group_order =?" + ", combo_indicator =?" + ", co_member_flag =?" + ", emboss_data =?"
        + ", auto_installment =?" + ", co_member_type =?" + ", bill_form_seq =?"
        + ", assign_installment =?" + ", mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + ", special_card_rate_flag =?"
        + ", revolve_int_rate_year=?" + ", crd_makecard_fee_flag =?" + ", crd_makecard_fee=?"
        + ", cca_group_mcht_chk = ?"+ ", purchase_card_flag = ? " + ",rcrate_day = ? "
        + ",member_flag = ? ,member_corp_no = ? ,ibank_apply_flag = ?" 
        + sqlWhere;
    Object[] param =
        new Object[] {wp.itemStr("group_abbr_code"), wp.itemStr("group_name"),
            wp.itemStr("group_order"), wp.itemStr("combo_indicator"),
            wp.itemStr("co_member_flag").equals("Y") ? "Y" : "N", wp.itemStr("emboss_data"),
            wp.itemStr("auto_installment").equals("Y") ? "Y" : "N", wp.itemStr("co_member_type"),
            wp.itemStr("bill_form_seq"), wp.itemStr("assign_installment").equals("Y") ? "Y" : "N",
            wp.loginUser, wp.itemStr("mod_pgm"), wp.itemStr("special_card_rate_flag"),
//            wp.itemStr("revolve_int_rate_year").equals("") ? null
//                : wp.itemStr("revolve_int_rate_year"),
            wp.itemEmpty("revolve_int_rate_year") ? "0"
                  : wp.itemStr("revolve_int_rate_year"),
            wp.itemStr("crd_makecard_fee_flag"),
//            wp.itemStr("crd_makecard_fee").equals("") ? null : wp.itemStr("crd_makecard_fee"),
            wp.itemEmpty("crd_makecard_fee") ? "0" : wp.itemStr("crd_makecard_fee"),
//            wp.itemStr("crd_makecard_fee").equals(""),
            wp.itemStr("cca_group_mcht_chk"),
            wp.itemStr("purchase_card_flag"),
            rcrateDay.doubleValue(),
            wp.itemStr("member_flag"),
            wp.itemStr("member_corp_no"),
            wp.itemEq("ibank_apply_flag", "Y")?"Y":"N",
             mKkGroupCode, wp.modSeqno()
            };
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete ptr_group_code " + sqlWhere;
    Object[] param = new Object[] {mKkGroupCode, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }
  
  	/**
	 * 計算日利率_加碼
	 * 
	 * @param rcrateYear
	 * @return
	 */
	private double calculateRcratePlus(Double rcrateYear) {
		BigDecimal rcrateDay = BigDecimal.ZERO;

		rcrateDay = new BigDecimal(rcrateYear).multiply(BigDecimal.valueOf(10000)).divide(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(365), 4, BigDecimal.ROUND_DOWN);
		// 四捨五入
		BigDecimal rcrateDayUp = rcrateDay.setScale(3, BigDecimal.ROUND_HALF_UP);
		// 不四捨五入
		BigDecimal rcrateDayDown = rcrateDay.setScale(3, BigDecimal.ROUND_DOWN);
		// 反推年利率
		rcrateYear = rcrateDayUp.divide(BigDecimal.valueOf(10000)).multiply(BigDecimal.valueOf(365))
				.multiply(BigDecimal.valueOf(100)).doubleValue();
		if (rcrateYear <= LEGAL_RCRATE_YEAR) {
			// 反推年利率 <= 15 四捨五入
			rcrateDay = rcrateDayUp;
		} else {
			// 反推年利率 > 15 不四捨五入
			rcrateDay = rcrateDayDown;
		}
		return rcrateDay.doubleValue();
	}
  
	private boolean checkSmallerLegelRcrateYear(Double rcrateYear) {
		boolean isSmaller = false;
		try {
			isSmaller = rcrateYear <= LEGAL_RCRATE_YEAR;
			if (!isSmaller) {
				errmsg(String.format("年利率不可大於%s", LEGAL_RCRATE_YEAR));
			}
		} catch (Exception ex) {
			errmsg(String.format("年利率格式轉換錯誤"));
		}
		return isSmaller;
	}
	
	private Double getMaxRcrateDayPlus() {
		String lsSql = "select max(rcrate_day_plus) as max from ptr_rcrate ";
		sqlSelect(lsSql);

		if (sqlRowNum <= 0) {
			errmsg("ptr_rcrate查無資料");
			return null;
		}

		return colNum("max");
	}

	/**
	 * 計算日利率
	 * 
	 * @param rcrateDayPlus
	 * @param maxRcrateDayPlus
	 * @return rcrateDay, or null if it did not get maximum number of
	 *         rcrate_day_plus
	 */
	private Double calculateRcrate(Double rcrateDayPlus, Double maxRcrateDayPlus) {
		if (maxRcrateDayPlus == null)
			return null;
		return new BigDecimal(maxRcrateDayPlus).subtract(BigDecimal.valueOf(rcrateDayPlus)).doubleValue();
	}
}
