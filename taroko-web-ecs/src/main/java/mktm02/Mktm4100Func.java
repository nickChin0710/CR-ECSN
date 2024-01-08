/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  ---------  --------- ----------- ---------------------------------------- *
*  111-03-28  V1.00.01  Justin      分期期數限制只能輸入數字                 *
* ****************************************************************************
*/

package mktm02;

import busi.FuncAction;
import busi.SqlPrepare;

public class Mktm4100Func extends FuncAction {
	String mchtNo = "" , productNo = "" , aprFlag = "";
	int seqNo = 0;
	@Override
	public void dataCheck() {
		if(ibAdd) {
			mchtNo = wp.itemStr("kk_mcht_no");
			productNo = wp.itemStr("kk_product_no");
			seqNo =(int) wp.itemNum("kk_seq_no");
		}	else	{
			mchtNo = wp.itemStr("mcht_no");
			productNo = wp.itemStr("product_no");
			seqNo =(int) wp.itemNum("seq_no");
		}
		
		aprFlag = wp.itemStr("apr_flag");
		
		if(ibDelete == false) {
			//--check mcht_no in bil_merchant
			if(checkMchtNo() == false) {
				errmsg("特店代號不存在");
				return ;
			}
			
			//--檢核日期
			if(chkStrend(wp.itemStr("start_date"), wp.itemStr("end_date"))==-1) {
				errmsg("有效日期起迄錯誤");
				return ;
			}
			
			//--檢核效期是否重疊
			if(checkDupDate() == false) {
				errmsg("有效日期重疊");
				return ;
			}
			
			//--應付費用年百分比
			if(wp.itemNum("year_fees_rate") > 100 || wp.itemNum("year_fees_rate") < 0) {
				errmsg("應付費用年百分率須為 0 ~ 100");
				return ;
			}
			
			if(wp.itemNum("clt_fees_fix_amt") > 0 && wp.itemNum("clt_interest_rate") > 0) {
				if(wp.itemNum("year_fees_rate") <= 0) {
					errmsg("應付費用年百分率 須大於 0");
					return ;
				}
			}
			
			//--消費金額
			if(wp.itemNum("tot_amt") != 0 && wp.itemNum("limit_min") > wp.itemNum("tot_amt")) {
				errmsg("最低消費金額不可大於最高消費金額");
				return ;
			}
			
			//--還款利率 Decimal(6,3)
			if(wp.itemNum("trans_rate") >= 1000) {
				errmsg("還款利率不可大於等於 1000");
				return ;
			}
			
			//--最高消費金額 Decimal(12,2)
			if(wp.itemNum("tot_amt") > 1000000000) {
				errmsg("最高消費金額不可大於等於 1000000000 ");
				return ;
			}
			
			//--最低消費金額 Decimal(12,2)
			if(wp.itemNum("limit_min") >= 1000000000) {
				errmsg("最高消費金額不可大於等於 1000000000 ");
				return ;
			}
			
			//--客戶手續費金額 Decimal(16,3)
			if(wp.itemNum("clt_fees_fix_amt") >= 1000000000) {
				errmsg("客戶手續費金額不可大於等於 1000000000 ");
				return ;
			}
			
			//--客戶手續費費率 Decimal(16,3)
			if(wp.itemNum("clt_interest_rate") >= 1000000000) {
				errmsg("客戶手續費費率不可大於等於 1000000000 ");
				return ;
			}
			
			//--應付費用年百分率 Decimal(5,2)
			if(wp.itemNum("year_fees_rate") > 1000) {
				errmsg("應付費用年百分率不可大於等於 1000");
				return ;
			}
			
			//--每筆特店手續費金額 Decimal(8,3)
			if(wp.itemNum("fees_fix_amt") >= 100000) {
				errmsg("每筆特店手續費金額不可大於等於 100000");
				return ;
			}
			
			//--特店手續費費率 Decimal(5,3)
			if(wp.itemNum("interest_rate") >= 100) {
				errmsg("特店手續費費率不可大於等於 100");
				return ;
			}
			
		}					
		
		if(ibAdd){
			if (empty(mchtNo)) {
				errmsg("特店代號不得為空");
				return ;
			}

			if (empty(productNo)) {
				errmsg("分期期數不得為空");
				return ;
			}
			
			return ;
		}
		
		//--已覆核資料不可刪除
		if(wp.itemEq("apr_flag", "Y") && ibDelete) {
			errmsg("此筆資料已覆核 , 不可進行刪除");
			return ;
		}
		
		//--已覆核異動確認是否已有未覆核資料
		if(wp.itemEq("apr_flag","Y") && checkTemp() == false) {
			errmsg("已有未覆核資料 , 請從未覆核資料異動");
			return ;
		}
		
		//--未覆核資料確認是否有其他人修正
		if(wp.itemEq("apr_flag","N")) {
			sqlWhere = " where mcht_no = ?  and seq_no = ? and product_no = ? and nvl(mod_seqno,0) = ? ";
			Object[] param = new Object[] {mchtNo, seqNo, productNo, wp.modSeqno()};
		    if (isOtherModify("bil_prod_nccc_t", sqlWhere, param)) {		    	
		        errmsg("資料已被異動，請重新查詢 !");
		        return;
		    }
		}
		
	}
	
	boolean checkTemp() {
		String sql1 = "select count(*) as temp_cnt from bil_prod_nccc_t where mcht_no = ? and product_no = ? and seq_no = ? ";
		sqlSelect(sql1,new Object[] {mchtNo,productNo,seqNo});
		if(colNum("temp_cnt") >0)
			return false ;
		return true;
	}
	
	boolean checkDupDate() {
		String sql1 = "select count(*) as date_cnt1 from bil_prod_nccc where mcht_no = ? and product_no = ? "
					+ "and seq_no <> ? and ? between start_date and end_date and end_date >= to_char(sysdate,'yyyymmdd') ";
		
		sqlSelect(sql1,new Object[] {mchtNo,productNo,seqNo,wp.itemStr("start_date")});
		if(colNum("date_cnt1") > 0) {
			return false ;
		}
		
		String sql2 = "select count(*) as date_cnt2 from bil_prod_nccc where mcht_no = ? and product_no = ? "
					+ "and seq_no <> ? and ? between start_date and end_date and end_date >= to_char(sysdate,'yyyymmdd') ";
		
		sqlSelect(sql2,new Object[] {mchtNo,productNo,seqNo,wp.itemStr("end_date")});
		if(colNum("date_cnt2") > 0) {
			return false ;
		}
		
		String sql3 = "select count(*) as date_cnt3 from bil_prod_nccc_t where mcht_no = ? and product_no = ? "
					+ "and seq_no <> ? and ? between start_date and end_date and end_date >= to_char(sysdate,'yyyymmdd') ";
	
		sqlSelect(sql3,new Object[] {mchtNo,productNo,seqNo,wp.itemStr("start_date")});
		if(colNum("date_cnt3") > 0) {
			return false ;
		}
	
		String sql4 = "select count(*) as date_cnt4 from bil_prod_nccc_t where mcht_no = ? and product_no = ? "
					+ "and seq_no <> ? and ? between start_date and end_date and end_date >= to_char(sysdate,'yyyymmdd') ";
	
		sqlSelect(sql4,new Object[] {mchtNo,productNo,seqNo,wp.itemStr("end_date")});
		if(colNum("date_cnt4") > 0) {
			return false ;
		}
		
		return true;
	}
	
	boolean checkMchtNo() {
		String sql1 = "select count(*) as db_cnt from bil_merchant where mcht_no = ? ";
		sqlSelect(sql1,new Object[] {mchtNo});
		if(sqlRowNum <=0 || colNum("db_cnt")<=0)
			return false;
		return true;
	}
	
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc!=1)	
			return rc;
		
		insertNcccTemp();
		
		return rc;
	}
	
	void insertNcccTemp() {
		msgOK();
		
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("bil_prod_nccc_t");
	    sp.ppstr("mcht_no", mchtNo);
	    sp.ppstr("product_no", productNo);
	    sp.ppint("seq_no", seqNo);
	    sp.ppstr("product_name", wp.itemStr("product_name"));
	    sp.ppstr("start_date", wp.itemStr("start_date"));
	    sp.ppstr("end_date", wp.itemStr("end_date"));
	    sp.ppnum("tot_amt", wp.itemNum("tot_amt"));
	    sp.ppnum("limit_min", wp.itemNum("limit_min"));
	    sp.ppnum("clt_fees_fix_amt", wp.itemNum("clt_fees_fix_amt"));
	    sp.ppnum("clt_interest_rate", wp.itemNum("clt_interest_rate"));
	    sp.ppnum("trans_rate", wp.itemNum("trans_rate"));
	    sp.ppstr("installment_flag", wp.itemStr("installment_flag"));
	    sp.ppnum("year_fees_rate", wp.itemNum("year_fees_rate"));
	    sp.ppnum("fees_fix_amt", wp.itemNum("fees_fix_amt"));
	    sp.ppnum("interest_rate", wp.itemNum("interest_rate"));
	    sp.ppstr("mod_audcode", "A");
	    sp.ppstr("confirm_flag", "N");
	    sp.ppnum("tot_term", commString.strToNum(productNo));
	    sp.ppstr("crt_user", wp.loginUser);
	    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
	    sp.ppstr("mod_user", wp.loginUser);
	    sp.addsql(", mod_time ", ", sysdate ");
	    sp.ppstr("mod_pgm", wp.modPgm());
	    sp.ppnum("mod_seqno", 1);
	    sqlExec(sp.sqlStmt(), sp.sqlParm());
	    
	    if (sqlRowNum <= 0) {
	      errmsg(sqlErrtext);	    
	    }				
		
		return ;
	}
	
	void copyBinTemp() {
		String sql1 = "select mcht_no , product_no , seq_no , bin_no , dtl_kind , dtl_value "
					+ "from bil_prod_nccc_bin where mcht_no = ? and product_no = ? and seq_no = ? ";
		
		sqlSelect(sql1,new Object[] {mchtNo,productNo,seqNo});
		int binCnt = sqlRowNum ;		
		if(binCnt <=0)
			return ;
		
		for(int ii = 0 ; ii < binCnt ; ii++) {
			strSql = " insert into bil_prod_nccc_bin_t (mcht_no,product_no,seq_no,bin_no,dtl_kind,dtl_value)"
				   + " values (:mcht_no,:product_no,:seq_no,:bin_no,:dtl_kind,:dtl_value) ";
			
			setString("mcht_no",colStr(ii,"mcht_no"));
			setString("product_no",colStr(ii,"product_no"));
			setInt("seq_no",colInt(ii,"seq_no"));
			setString("bin_no",colStr(ii,"bin_no"));
			setString("dtl_kind",colStr(ii,"dtl_kind"));
			setString("dtl_value",colStr(ii,"dtl_value"));
			
			sqlExec(strSql);
			if(sqlRowNum <=0) {
				errmsg("明細檔寫入失敗");
				return ;
			}
		}
		
	}
	
	void updateNcccTemp() {
		msgOK();

		busi.SqlPrepare sp = new SqlPrepare();
	    sp.sql2Update("bil_prod_nccc_t");	    
	    sp.ppstr("product_name", wp.itemStr("product_name"));
	    sp.ppstr("start_date", wp.itemStr("start_date"));
	    sp.ppstr("end_date", wp.itemStr("end_date"));
	    sp.ppnum("tot_amt", wp.itemNum("tot_amt"));
	    sp.ppnum("limit_min", wp.itemNum("limit_min"));
	    sp.ppnum("clt_fees_fix_amt", wp.itemNum("clt_fees_fix_amt"));
	    sp.ppnum("clt_interest_rate", wp.itemNum("clt_interest_rate"));
	    sp.ppnum("trans_rate", wp.itemNum("trans_rate"));
	    sp.ppstr("installment_flag", wp.itemStr("installment_flag"));
	    sp.ppnum("year_fees_rate", wp.itemNum("year_fees_rate"));
	    sp.ppnum("fees_fix_amt", wp.itemNum("fees_fix_amt"));
	    sp.ppnum("interest_rate", wp.itemNum("interest_rate"));
	    sp.ppstr("mod_audcode", "U");
	    sp.ppstr("confirm_flag", "N");
	    sp.ppnum("tot_term", commString.strToNum(productNo));
	    sp.addsql(", mod_time =sysdate", "");
	    sp.ppstr("mod_user", wp.loginUser);
	    sp.ppstr("mod_pgm", wp.modPgm());
	    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
	    sp.sql2Where("where mcht_no=?", mchtNo);
	    sp.sql2Where("and seq_no=?", seqNo);
	    sp.sql2Where("and product_no=?", productNo);
	    sp.sql2Where("and mod_seqno=?", wp.itemNum("mod_seqno"));
	    sqlExec(sp.sqlStmt(), sp.sqlParm());
	    if (sqlRowNum <= 0) {
	      errmsg(sqlErrtext);
	    }
	    return ;		
	}
	
	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1)	
			return rc;
				
		if(wp.itemEq("apr_flag","Y")) {
			//--已覆核資料新增一筆異動檔和複製所有明細檔
			insertNcccTemp();
			if(rc!=1)
				return rc;
			copyBinTemp();
		} else {
			//--未覆核只修正主檔
			updateNcccTemp();
		}
		
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1)	
			return rc;
		deleteNcccTemp();
		if(rc!=1)
			return rc;
		deleteBinTemp();		
		return rc;
	}
	
	void deleteNcccTemp() {
		msgOK();
		strSql = "delete bil_prod_nccc_t where mcht_no =:mcht_no and product_no =:product_no and seq_no =:seq_no and mod_seqno =:mod_seqno ";
		setString("mcht_no",mchtNo);
		setString("product_no",productNo);
		setInt("seq_no",seqNo);
		setDouble("mod_seqno",wp.itemNum("mod_seqno"));
		sqlExec(strSql);
		if(sqlRowNum <= 0 ) {
			errmsg("刪除異動檔失敗");
			return ;
		}				
	}
	
	void deleteBinTemp() {
		msgOK();
		strSql = "delete bil_prod_nccc_bin_t where mcht_no =:mcht_no and product_no =:product_no and seq_no =:seq_no";
		setString("mcht_no",mchtNo);
		setString("product_no",productNo);
		setInt("seq_no",seqNo);
		setDouble("mod_seqno",wp.itemNum("mod_seqno"));
		sqlExec(strSql);
		if(sqlRowNum < 0 ) {
			errmsg("刪除明細檔失敗");
			return ;
		}				
	}
	
	int deleteBinTempKind() {
		msgOK();
		strSql = "delete bil_prod_nccc_bin_t where mcht_no =:mcht_no and product_no =:product_no and seq_no =:seq_no and dtl_kind =:dtl_kind ";
		var2ParmStr("mcht_no");
		var2ParmStr("product_no");
		var2ParmInt("seq_no");
		var2ParmStr("dtl_kind");
		sqlExec(strSql);
		if(sqlRowNum < 0 ) {
			errmsg("刪除明細檔失敗");
			return rc;
		}		
		return rc;
	}
	
	int insertBinTempKind() {
		msgOK();
		strSql = " insert into bil_prod_nccc_bin_t (mcht_no,product_no,seq_no,bin_no,dtl_kind,dtl_value)"
			   + " values (:mcht_no,:product_no,:seq_no,'N',:dtl_kind,:dtl_value) ";
		var2ParmStr("mcht_no");
		var2ParmStr("product_no");
		var2ParmInt("seq_no");
		var2ParmStr("dtl_kind");
		var2ParmStr("dtl_value");
		sqlExec(strSql);
		if(sqlRowNum <= 0 ) {
			errmsg("新增明細失敗");			
		}
		return rc;
	}
	
	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

}
