/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/30  V1.00.00   Machao      Initial                                *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktp0910Func extends FuncEdit{
	
	String activeCode;
	 String controlTabName = "mkt_tax_fbdata";

	public Mktp0910Func(TarokoCommon wr) {
		// TODO Auto-generated constructor stub
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
	    return 1;
	  }

	  // ************************************************************************
	  @Override
	  public void dataCheck() {
		  if(empty(wp.itemStr("ex_active_code"))) {
			  errmsg("請在繳稅活動代碼處選擇需存檔的活動代碼 ");
	            return;
			}
	}

	// ************************************************************************
	  @Override
	  public int dbInsert() {
		  actionInit("A");
//			dataCheck();
//			if(rc!=1)return -1;
			strSql = " insert into ECSCRDB.mkt_tax_fbdata ( " 
					+ " active_code, " 
					+ " active_type, " 
					+ " pay_yyyy, "
					+ " staff_flag, "
					+ " purchase_date, "
					+ " purchase_amt, "
					+ " feedback_id_type, "
					+ " id_no, "
					+ " id_p_seqno, "
					+ " card_no, "
					
					+ " gift_type, "
					+ " cal_def_date, "
//					+ " feedback_date, "
					+ " feedback_seqno, " 
					+ " crt_date, crt_user, mod_time, mod_user, mod_pgm, mod_seqno " 
					+ " ) " 
					+ " values( " 
					+ " ?,?,to_char(sysdate,'yyyy'),?,?,?,?,?,?,?, " 
					+ " '1' ,to_char(sysdate,'yyyymmdd')," 
					+ "?," + "to_char(sysdate,'yyyymmdd'),?, sysdate, ?, ?, ? "
					+ " ) ";

			Object[] param = new Object[] {
					wp.colStr("active_code"), 
					wp.colStr("active_type"),
					wp.colStr("staff_flag"),
					wp.colStr("purchase_date"),
					wp.colStr("purchase_amt"),
					wp.colStr("feedback_id_type"),
					wp.colStr("id_no"),
					wp.colStr("id_p_seqno"),
					wp.colStr("card_no"),
					wp.colNum("feedback_seqno"),
					wp.loginUser,
					wp.loginUser,
					wp.modPgm(),
					wp.modSeqno()
			};

			rc = sqlExec(strSql, param);
			if (sqlRowNum <= 0) {
				errmsg("新增 "+ controlTabName +" 錯誤");
			}
			return rc;
	  }

	  // ************************************************************************
	  @Override
	  public int dbUpdate() {
		  actionInit("U");
			msgOK();
			dataCheck();
			if (rc!=1) return rc;
			strSql = " update mkt_tax_fbdata set " 
					+ " feedback_date = ? "
//					+ " ,crt_user = ? "
//					+ " ,crt_date = ? "
					+ " ,mod_user = ? "
					+ " ,mod_time = sysdate "
					+ " ,mod_pgm = ?"
					+ " ,mod_seqno = nvl(mod_seqno,0)+1 "
					+ " where 1 =  1 " 
					+ " and active_code = ? "
					;

			Object[] param = new Object[] {
					getSysDate(), 
//					colStr("crt_user"),
//					colStr("crt_date"),
					wp.loginUser,
					wp.modPgm(),
					wp.itemStr("ex_active_code")
			};

			sqlExec(strSql, param);
			if (sqlRowNum <= 0) {
				errmsg("資料確認存檔失敗");
			}
			return rc;
	  }

	  // ************************************************************************
	  @Override
	  public int dbDelete() {
	    return 1;
	  }

	  // ************************************************************************
	  // ************************************************************************
}
