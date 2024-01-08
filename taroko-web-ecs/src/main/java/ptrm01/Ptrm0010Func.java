/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-02-28   V1.00.01  Zuwei      add OnlineApprove                          *
* 109-04-20   V1.00.02  Tanwei     updated for project coding standard      *
* 109-12-08   V1.00.03  Justin        add RCRATE_DAY_PLUS, and update PTR_ACTGENERAL_N when PTR_RCRATE is changed
* 109-12-10   V1.00.04  Justin        rcrate_year is not allow to be lower than legal_rcrate_year, and add updateAllPtrRcrateDay
* 109-12-11   V1.00.05  Justin        use maxRcrateDayPlus to update ptr_actgeneral_n 
* 110-01-05   V1.00.06  Justin       updated for XSS
* 112-07-31   V1.00.07  Ryan       信評等級不能修改
******************************************************************************/

package ptrm01;

import java.math.BigDecimal;
import busi.FuncEdit;
import taroko.base.CommString;

public class Ptrm0010Func extends FuncEdit {
  CommString commString = new CommString();
  static final Double LEGAL_RCRATE_YEAR = 15.0; //法定年利率

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
    String rating = wp.itemStr("rating");
    String rcrateYear = wp.itemStr("rcrate_year");
    rcrateYear = new BigDecimal(rcrateYear).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString();
    if (wp.itemEmpty("rcrate_year")) {
      errmsg("循環信用利率-年：不可空白");
      return;
    }
    if (this.isAdd()) {
      String creditRating = wp.itemStr("ex_credit_rating");
      String holdingPeriod = wp.itemStr("ex_holding_period");
//      holdingPeriod = commString.decode(holdingPeriod, ",&gt=N,&ltN,X", ",>=N,<N,X");

      if (checkSmallerLegelRcrateYear(rcrateYear)==false) {
          return;
	  }
      if (eqIgno(rating, "0") == true) {
          errmsg("級數：不可空白");
          return;
      }
      if (eqIgno(creditRating, "0") == true) {
        errmsg("信評等級：不可空白");
        rc = -1;
        return;
      }
      if (eqIgno(holdingPeriod, "0") == true) {
        errmsg("持卡年限：不可空白");
        rc = -1;
        return;
      }

      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from ptr_rcrate where credit_rating = ? and holding_period = ?";
      Object[] param = new Object[] {creditRating, holdingPeriod};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在");
        rc = -1;
        return;
      }

      lsSql = "select rcrate_year from ptr_rcrate where rating = ? group by rcrate_year ";
      param = new Object[] {rating};
      sqlSelect(lsSql, param);
      if (sqlRowNum > 1) {
        errmsg("相同[級數]下有多個不同的[年利率][日利率]");
        rc = -1;
        return;
      }
      if (sqlRowNum == 1 && !colStr("rcrate_year").equals(rcrateYear)) {
        errmsg("相同[級數]下之[年利率][日利率]要相同");
        rc = -1;
        return;
      }
    }

    if (this.isUpdate()) {
      String creditRating = wp.itemStr("credit_rating");
      String holdingPeriod = wp.itemStr("holding_period");
      Object[] param = new Object[] {creditRating, holdingPeriod};
      if (checkSmallerLegelRcrateYear(rcrateYear)) {
          return;
	  }
      if (sqlRowcount("ptr_rcrate", " where credit_rating = ? and holding_period = ?",
          param) <= 0) {
        errmsg("資料已被異動 or 不存在");
        rc = -1;
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
    String holdingPeriod = wp.itemStr("ex_holding_period");
    holdingPeriod = commString.decode(holdingPeriod, ",&gt=N,&ltN,X", ",>=N,<N,X");
    // 頁面上來的年利率
    Double rcrateYear = new BigDecimal(wp.itemStr("rcrate_year")).
                                  setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    // 轉換為日利率(加碼)
    Double rcrateDayPlus = calculateRcratePlus(rcrateYear);
    
    // 取得DB中最大的rcrate_day
    Double maxRcrateDayPlus = getMaxRcrateDayPlus();
    boolean isMaxRcrateChg = false;
    if (rcrateDayPlus > maxRcrateDayPlus) {
    	isMaxRcrateChg = true;
	}
    
    Double rcrateDay = calculateRcrate(rcrateDayPlus, maxRcrateDayPlus);
    if (rcrateDay == null) return rc;
    
    wp.colSet("rcrate_day", rcrateDayPlus);
    strSql = "insert into ptr_rcrate (" + " credit_rating ," + " holding_period ," + " rating ,"
        + " rcrate_year ," + " rcrate_day ," + " rcrate_day_plus ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values ( " + " :credit_rating ," + " :holding_period ," + " :rating ,"
        + " :rcrate_year , " +  ":rcrate_day ," +  ":rcrate_day_plus ," + " :mod_user ," + " sysdate ," + " :mod_pgm ,"
        + " 1 " + " )";
    setString("credit_rating", wp.itemStr("ex_credit_rating"));
    setString("holding_period", holdingPeriod);
    setString("rating", wp.itemStr("rating"));
    setDouble("rcrate_year",rcrateYear);
    setDouble("rcrate_day",rcrateDay);
    setDouble("rcrate_day_plus",rcrateDayPlus);
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ptrm0010");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }
    
    // 是否新增的值大於最大值
    if (isMaxRcrateChg) {
    	maxRcrateDayPlus = getMaxRcrateDayPlus();
		updateAllPtrRcrateDay(maxRcrateDayPlus);
		if(rc != 1) return rc;
		updatePtrActgeneralN(maxRcrateDayPlus);
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
    // 頁面上來的年利率
    Double rcrateYear = new BigDecimal(wp.itemStr("rcrate_year")).
                                  setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    // 轉換為日利率(加碼)
    Double rcrateDayPlus = calculateRcratePlus(rcrateYear);
    
    // 取得DB中最大的rcrate_day
    Double maxRcrateDayPlus = getMaxRcrateDayPlus();
    boolean isMaxRcrateChg = false;
    if (rcrateDayPlus > maxRcrateDayPlus || wp.itemNum("old_rcrate_day_plus") == maxRcrateDayPlus) {
    	isMaxRcrateChg = true;
	}
    
    Double rcrateDay = calculateRcrate(rcrateDayPlus, maxRcrateDayPlus);
    if (rcrateDay == null) return rc;
    
    String rating = wp.itemStr("db_rating");
    strSql = " update ptr_rcrate set " 
//    + " rating = :rating ," 
    		+ " rcrate_year = :rcrate_year ," 
        + " rcrate_day = :rcrate_day ," + " rcrate_day_plus = :rcrate_day_plus ," + " mod_user =:mod_user ,"
        + " mod_time = sysdate ," + " mod_pgm  = :mod_pgm "
        + " where credit_rating = :credit_rating and holding_period = :holding_period";
//    setString("rating", rating);
    setDouble("rcrate_year", rcrateYear);
    setDouble("rcrate_day", rcrateDay);
    setDouble("rcrate_day_plus", rcrateDayPlus);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ptrm0010");
    setString("credit_rating", wp.itemStr("credit_rating"));
    setString("holding_period", wp.itemStr("holding_period"));

    sqlExec(strSql);
    if (rc < 0) {
      return rc;
    }
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
      return rc;
    }

    /* Update相同[級數]下之[日利率],[年利率] */
    strSql = " update ptr_rcrate set " + " rcrate_year = :rcrate_year ," 
        + " rcrate_day =  :rcrate_day ," + " rcrate_day_plus =  :rcrate_day_plus ," 
    	+ " mod_user = :mod_user ," + " mod_time = sysdate ,"
        + " mod_pgm  = :mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where rating =:rating ";

    setDouble("rcrate_year",rcrateYear);
    setDouble("rcrate_day",rcrateDay);
    setDouble("rcrate_day_plus",rcrateDayPlus);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ptrm0010");
    setString("rating", rating);

    sqlExec(strSql);
    
	if (sqlRowNum <= 0) {
		errmsg(this.sqlErrtext);
		rc = -1;
		return rc;
	}
	
	// 是否最大值有異動
	if (isMaxRcrateChg) {
		maxRcrateDayPlus = getMaxRcrateDayPlus();
		updateAllPtrRcrateDay(maxRcrateDayPlus);
		if(rc != 1) return rc;
		updatePtrActgeneralN(maxRcrateDayPlus);
	}

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    if (rc != 1) {
      return rc;
    }
    strSql =
        "delete ptr_rcrate  where credit_rating =:credit_rating and holding_period =:holding_period ";
    setString("credit_rating", wp.itemStr("credit_rating"));
    setString("holding_period", wp.itemStr("holding_period"));
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    
    Double maxRcrateDayPlus = getMaxRcrateDayPlus();
    // 刪除此筆資料後，如果最大的rcrate_day_plus小於刪除的rcrate_day_plus，
    // 則需要update所有的rcrate_day以及update ptr_actgeneral_n
    if (wp.itemNum("old_rcrate_day_plus") > maxRcrateDayPlus) {
		updateAllPtrRcrateDay(maxRcrateDayPlus);
		if(rc != 1) return rc;
		updatePtrActgeneralN(maxRcrateDayPlus);
	}

    return rc;
  }
  
  /**
   * get max rcrate_day_plus
   * @return rcrate_day_plus, or null if no data is selected
   */
  private Double getMaxRcrateDayPlus(){
      String lsSql = "select max(rcrate_day_plus) as max from ptr_rcrate ";
      
      sqlSelect(lsSql);

      if (sqlRowNum <= 0) {
          errmsg("ptr_rcrate查無資料");
          rc = -1;
          return null;
      }
      
      return colNum("max");
  }
  
  /**
   * 計算日利率
   * @param rcrateDayPlus
   * @param maxRcrateDayPlus 
   * @return rcrateDay, or null if it did not get maximum number of rcrate_day_plus
   */
	private Double calculateRcrate(Double rcrateDayPlus, Double maxRcrateDayPlus) {
		if (maxRcrateDayPlus == null) return null;
		return new BigDecimal(maxRcrateDayPlus).
				         subtract(BigDecimal.valueOf(rcrateDayPlus)).doubleValue();
	}

/**
   * 計算日利率_加碼
   * 
   * @param rcrateYear
   * @return
   */
  private double calculateRcratePlus(Double rcrateYear) {
	BigDecimal rcrateDay = BigDecimal.ZERO;

	rcrateDay = new BigDecimal(rcrateYear).
			                         multiply(BigDecimal.valueOf(10000)).
			                         divide(BigDecimal.valueOf(100)).
			                         divide(BigDecimal.valueOf(365), 4, BigDecimal.ROUND_DOWN)
			                         ;
    // 四捨五入
	BigDecimal rcrateDayUp =
    		rcrateDay.setScale(3, BigDecimal.ROUND_HALF_UP);
    // 不四捨五入
	BigDecimal rcrateDayDown =
			rcrateDay.setScale(3, BigDecimal.ROUND_DOWN);
    // 反推年利率
	rcrateYear = rcrateDayUp.divide(BigDecimal.valueOf(10000)).
			                             multiply(BigDecimal.valueOf(365)).
			                             multiply(BigDecimal.valueOf(100)).doubleValue();
    if (rcrateYear <= LEGAL_RCRATE_YEAR) {
      // 反推年利率 <= 15 四捨五入
      rcrateDay = rcrateDayUp;
    } else {
      // 反推年利率 > 15 不四捨五入
      rcrateDay = rcrateDayDown;
    }
    return rcrateDay.doubleValue();
  }

  /**
   * update ptr_actgeneral_n
   * @param maxRcrateDayPlus
   * @return rc
   */
	private int updatePtrActgeneralN(Double maxRcrateDayPlus) {
		strSql = " update ptr_actgeneral_n set " 
	            + " revolving_interest1 = :revolving_interest1 ," 
				+ " revolving_interest2 =  :revolving_interest2 ,"
				+ " rc_max_rate =  :rc_max_rate ," 
	            + " mod_user = :mod_user ," 
				+ " mod_time = sysdate ,"
				+ " mod_pgm  = :mod_pgm ," 
				+ " mod_seqno =nvl(mod_seqno,0)+1 " ;
		
		setDouble("revolving_interest1", maxRcrateDayPlus);
		setDouble("revolving_interest2", maxRcrateDayPlus);
		setDouble("rc_max_rate", maxRcrateDayPlus);
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", "ptrm0010");

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		
		return rc;

	}

	/**
	 * update all rcrate_day
	 * @return
	 */
	private int updateAllPtrRcrateDay(Double maxRcrateDayPlus) {
		if (maxRcrateDayPlus == null) return rc;
		msgOK();
		strSql = " update ptr_rcrate set " 
				+ " rcrate_day =  :max_rcrate_day_plus - rcrate_day_plus ,"
				+ " mod_user = :mod_user ," 
				+ " mod_time = sysdate ,"
				+ " mod_pgm  = :mod_pgm ," 
				+ " mod_seqno =nvl(mod_seqno,0)+1 " ;
		
		setDouble("max_rcrate_day_plus", maxRcrateDayPlus);
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", "ptrm0010");

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg(String.format("update all ptr_rcrate_day error。Error: %s", this.sqlErrtext));
			rc = -1;
		}
		return rc;
	}

/**
   * check whether rcrate_year is smaller than legal_rcrate_year 
   * @param rcrateYear
   * @return true if rcrate_year is greater or equal to legal_rcrate_year, or return false
   */
private boolean checkSmallerLegelRcrateYear(String rcrateYear) {
	boolean isSmaller = false;
	try {
		isSmaller = Double.parseDouble(rcrateYear) <= LEGAL_RCRATE_YEAR;
		if (!isSmaller) {
			errmsg(String.format("年利率不可大於%s", LEGAL_RCRATE_YEAR));
		}
	} catch (Exception ex) {
		errmsg(String.format("年利率格式轉換"));
	}
	return isSmaller;
}
}
