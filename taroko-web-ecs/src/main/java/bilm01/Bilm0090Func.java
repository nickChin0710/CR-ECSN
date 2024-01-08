/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-07  V1.00.00  yash       program initial                            *
* 109-04-23  V1.00.01  shiyuqi    updated for project coding standard        *
* 109-11-20   V1.00.02  tanwei    默認db中獲取值， 尾數改bug                        *  
* 109-11-26   V1.00.03  Justin     fix lots of bugs                                     *
* 109-11-27   V1.00.04  Justin     新增錯誤訊息
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/

package bilm01;


import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0090Func extends FuncEdit {
  String mchtNo = "", productNo = "";

  public Bilm0090Func(TarokoCommon wr) {
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
	  mchtNo = wp.itemStr("mcht_no");
      productNo = wp.itemStr("product_no");
      
		if (empty(mchtNo)) {
			errmsg("特店代號不可空白!");
			return;
		}

		if (empty(productNo)) {
			errmsg("商品代號不可空白!");
			return;
		}

		if (mchtNo.length() < 10) {
			errmsg("特店代號必須輸滿 10 碼!");
			return;
		}
		
//		=============== add and update ================
	if (this.isAdd() || this.ibUpdate) {
		if (empty(wp.itemStr("product_name"))) {
			errmsg("商品名稱不可空白!");
			return;
		}

		// select bil_merchant
		String sqlSelect = " select mcht_chi_name, NVL(mcht_type,'1') as mcht_type  ,NVL(trans_flag,'N') as trans_flag from bil_merchant where mcht_no = :mcht_no ";
		setString("mcht_no", mchtNo);
		sqlSelect(sqlSelect);
		String mchtType = colStr("mcht_type");
		String transFlag = colStr("trans_flag");
		if (sqlRowNum <= 0) {
			errmsg("特店代號 不存在");
			return;
		}

		wp.colSet("db_merchant_type", mchtType);
		wp.colSet("db_trans_flag", transFlag);
		wp.itemSet("db_merchant_type", mchtType);
		wp.itemSet("db_trans_flag", transFlag);

		if (mchtType.equals("2")) {
			errmsg("NCCC 特店, 不可用此程式");
			return;
		}

		if (transFlag.equals("Y")) {
			if (wp.itemNum("trans_rate") <= 0) {
				errmsg("利率不可小於或等於 0");
				return;
			}

		}

		if (wp.itemNum("unit_price") <= 0) {
			errmsg("每期金額(單價) 必須大於一元 ");
			return;
		}

		Double amount = wp.colNum("unit_price") * wp.colNum("tot_term");
		if (wp.colNum("tot_amt") < amount) {
			errmsg("總金額不可小於每期金額*期數 ");
			return;
		}

		if (wp.itemNum("year_fees_rate") > 100 || wp.itemNum("year_fees_rate") < 0) {
			errmsg("應付費用年百分率 值須為 0 ~ 100 ");
			return;
		}

		if (wp.itemNum("clt_fees_fix_amt") > 0 || wp.itemNum("clt_interest_rate") > 0) {
			if (wp.itemNum("year_fees_rate") <= 0) {
				errmsg("應付費用年百分率  不可為 0");
				return;
			}
		}

		if (!empty(wp.itemStr("risk_code"))) {
			// System.out.println( wp.item_ss("risk_code").subSequence(0, 1));
			if (!((String) wp.itemStr("risk_code").subSequence(0, 1)).matches("[a-zA-Z]*")) {
				errmsg("風險分類第一碼須為 A...Z");
				return;
			}

			if (!((String) wp.itemStr("risk_code").subSequence(1, 2)).matches("[0-9]*")) {
				errmsg("風險分類第二碼須為 1...9");
				return;
			}

			String lsSql = "select nvl(risk_code,' ') as ls_risk_code, nvl(active_flag,'N') as ls_active_flag "
					+ " from rsk_HiRisk_prod"
					+ " where mcht_no =:mcht_no and	tot_term=:tot_term and	nvl(apr_flag,'N')='Y'  ";
			setString("mcht_no", mchtNo);
			setString("tot_term", wp.itemStr("tot_term"));
			sqlSelect(lsSql);
			String lsRiskCode = colStr("ls_risk_code");
//		      String lsActiveFlag = colStr("ls_active_flag");
			if (sqlRowNum > 0) {
				if (!wp.itemStr("risk_code").equals(lsRiskCode)) {
					errmsg("風險分類與高風險產品~ [" + lsRiskCode + "] 不同");
					return;
				}
			}
		}

		// check 帳戶,團代,卡種不可全部空白
		if (varsInt("insertCnt") == 0) {
			errmsg("帳號類別,團代及卡種不可全部空白");
			return;
		}
		
	}
//	=============== delete ================
	else {
		
	}
	
	if (this.ibUpdate || this.ibDelete) {
		// -other modify-
	    sqlWhere = " where mcht_no = ? and product_no=?  and nvl(mod_seqno,0) = ?";
	    Object[] param = new Object[] {mchtNo, productNo, wp.modSeqno()};
	    
	    if (isOtherModify("bil_prod_t", sqlWhere, param)) {
	      errmsg("資料已被修改，請重新查詢");
	      return;
	    }
	}
    

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("bil_prod_t");
    sp.ppstr("mcht_no", mchtNo);
    sp.ppstr("product_no", productNo);
    sp.ppstr("product_name", wp.itemStr("product_name"));
    sp.ppnum("trans_rate", wp.itemNum("trans_rate"));
    sp.ppstr("risk_code", wp.itemStr("risk_code"));
    sp.ppnum("tot_amt", wp.itemNum("tot_amt"));
    sp.ppnum("unit_price", wp.itemNum("unit_price"));
    sp.ppnum("tot_term", wp.itemNum("tot_term"));
    sp.ppnum("redeem_point", wp.itemNum("redeem_point"));
    sp.ppnum("redeem_amt", wp.itemNum("redeem_amt"));
    sp.ppnum("remd_amt", wp.itemNum("remd_amt"));
    sp.ppnum("extra_fees", wp.itemNum("extra_fees"));
    sp.ppnum("against_num", wp.itemNum("against_num"));
    sp.ppnum("clt_fees_fix_amt", wp.itemNum("clt_fees_fix_amt"));
    sp.ppstr("installment_flag", wp.itemStr("installment_flag"));
    sp.ppnum("clt_fees_min_amt", wp.itemNum("clt_fees_min_amt"));
    sp.ppnum("clt_fees_max_amt", wp.itemNum("clt_fees_max_amt"));
    sp.ppnum("year_fees_rate", wp.itemNum("year_fees_rate"));
    sp.ppnum("fees_fix_amt", wp.itemNum("fees_fix_amt"));
    sp.ppnum("interest_rate", wp.itemNum("interest_rate"));
    sp.ppnum("clt_interest_rate", wp.itemNum("clt_interest_rate"));
    sp.ppnum("fees_min_amt", wp.itemNum("fees_min_amt"));
    sp.ppnum("fees_max_amt", wp.itemNum("fees_max_amt"));
    sp.ppstr("auto_delv_flag", wp.itemStr("auto_delv_flag"));
    sp.ppstr("auto_print_flag", wp.itemStr("auto_print_flag"));
    sp.ppstr("refund_flag", wp.itemStr("refund_flag"));
    sp.ppstr("confirm_flag", "N");
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
    	errmsg(String.format("新增bil_prod_t錯誤。%s", sqlErrtext));
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
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("bil_prod_t");
    sp.ppstr("product_name", wp.itemStr("product_name"));
    sp.ppnum("trans_rate", wp.itemNum("trans_rate"));
    sp.ppstr("risk_code", wp.itemStr("risk_code"));
    sp.ppnum("tot_amt", wp.itemNum("tot_amt"));
    sp.ppnum("unit_price", wp.itemNum("unit_price"));
    sp.ppnum("tot_term", wp.itemNum("tot_term"));
    sp.ppnum("redeem_point", wp.itemNum("redeem_point"));
    sp.ppnum("redeem_amt", wp.itemNum("redeem_amt"));
    sp.ppnum("extra_fees", wp.itemNum("extra_fees"));
    sp.ppnum("against_num", wp.itemNum("against_num"));
    sp.ppnum("clt_fees_fix_amt", wp.itemNum("clt_fees_fix_amt"));
    sp.ppnum("interest_rate", wp.itemNum("interest_rate"));
    sp.ppnum("clt_interest_rate", wp.itemNum("clt_interest_rate"));
    sp.ppstr("installment_flag", wp.itemStr("installment_flag"));
    sp.ppnum("clt_fees_min_amt", wp.itemNum("clt_fees_min_amt"));
    sp.ppnum("clt_fees_max_amt", wp.itemNum("clt_fees_max_amt"));
    sp.ppnum("year_fees_rate", wp.itemNum("year_fees_rate"));
    sp.ppnum("fees_fix_amt", wp.itemNum("fees_fix_amt"));
    sp.ppnum("fees_min_amt", wp.itemNum("fees_min_amt"));
    sp.ppnum("fees_max_amt", wp.itemNum("fees_max_amt"));
    sp.ppstr("auto_delv_flag", wp.itemStr("auto_delv_flag"));
    sp.ppstr("auto_print_flag", wp.itemStr("auto_print_flag"));
    sp.ppstr("refund_flag", wp.itemStr("refund_flag"));
    sp.ppstr("confirm_flag", "N");
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where mcht_no=?", mchtNo);
    sp.sql2Where("and product_no=?", productNo);
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(String.format("修改bil_prod_t錯誤。%s", sqlErrtext));
    }
    return rc;

  }
  
  public int updateRemdAmt(){
    strSql = " update bil_prod set " + " remd_amt =:remd_amt " + " where mcht_no =:mcht_no and product_no =:product_no";
    setDouble("remd_amt",wp.colNum("remd_amt"));
    setString("mcht_no",wp.colStr("mchtNo"));
    setString("product_no",wp.colStr("productNo"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(String.format("BIL_PROD無法更新成功，[mcht_no=%s] [product_no=%s]，errorText:%s", wp.colStr("mchtNo"), wp.colStr("product_no"), this.sqlErrtext));
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

    // delete dtl
    dbDelete2();

    strSql = "delete bil_prod_t " + sqlWhere;
    Object[] param = new Object[] {mchtNo, productNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
    	 errmsg(String.format("刪除bil_prod_t錯誤。%s", sqlErrtext));
    }

    return rc;
  }

  public int dbInsert2() {
    actionInit("A");
    msgOK();
    strSql = "insert into bil_prod_dtl_t (" + "  mcht_no" + ", product_no" + ", dtl_kind "
        + ", dtl_value " + " ) values (" + " ?,?,?,? " + " )";
    // -set ?value-
    Object[] param = new Object[] {
        !empty(wp.itemStr("mcht_no")) ? wp.itemStr("mcht_no") : wp.itemStr("kk_mcht_no")// 1
        , !empty(wp.itemStr("product_no")) ? wp.itemStr("product_no") : wp.itemStr("kk_product_no"),
        varsStr("aa_kind"), varsStr("aa_value")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(String.format("新增bil_prod_dtl_t錯誤。%s", sqlErrtext));
    }

    return rc;

  }

/**
 * delete all data in bil_prod_dtl_t, but show error message when an exception occurs or it did not delte any data
 * @return
 */
	public int dbDelete2() {
		actionInit("D");
		msgOK();
		dbDeleteDtlT();
		if (sqlRowNum <= 0) {
			errmsg(String.format("未成功刪除bil_prod_dtl_t欄位。%s", this.sqlErrtext));
		}
		return rc;
	}
  
	/**
	 * delete all data in bil_prod_dtl_t, but show error message only if an exception occurs
	 * @return
	 */
	public int dbDeleteDtlTForInsert() {
		actionInit("D");
		msgOK();
		dbDeleteDtlT();
		if (rc != 1) {
			errmsg(String.format("刪除bil_prod_dtl_t錯誤。%s", this.sqlErrtext));
		}
		return rc;
	}
  
  public int dbDeleteDtlT() {
	    strSql = "delete bil_prod_dtl_t where mcht_no = ? and product_no=? ";
	    Object[] param = new Object[] {wp.itemStr("mcht_no"), wp.itemStr("product_no")};
	    rc = sqlExec(strSql, param);
	    return rc;
  }

/**
   * return true if data is not exist, otherwise return false
   * @return
   */
public boolean checkBilProdT() {
	mchtNo = wp.itemStr("mcht_no");
	productNo = wp.itemStr("product_no");
	// 檢查新增資料是否重複
	String lsSql = "select count(*) as tot_cnt from bil_prod_t where mcht_no = ? and product_no=? ";
	Object[] param = new Object[] { mchtNo, productNo };
	sqlSelect(lsSql, param);
	if (colNum("tot_cnt") > 0) {
		errmsg(String.format("特店代號[%s]，商品代號[%s]為尚未覆核狀態，因此無法新增，或請改用修改", mchtNo, productNo));
		return false;
	}
	return true;
}

/**
 * return true if data is not exist, otherwise return false
 * @return
 */
public boolean checkBilProd() {
	mchtNo = wp.itemStr("mcht_no");
	productNo = wp.itemStr("product_no");
	// 檢查新增資料是否重複
	String lsSql = "select count(*) as tot_cnt from bil_prod where mcht_no = ? and product_no=? ";
	Object[] param = new Object[] { mchtNo, productNo };
	sqlSelect(lsSql, param);
	if (colNum("tot_cnt") > 0) {
		errmsg(String.format("特店代號[%s]，商品代號[%s]已存在，因此無法新增", mchtNo, productNo));
		return false;
	}
	return true;
}


}
