/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-07  V1.00.00  Andy       program initial                            *
* 111-10-20  V1.00.03  Machao      sync from mega & updated for project coding standard                                                                           *
******************************************************************************/

package actm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Actm2090Func extends FuncEdit {

    public Actm2090Func(TarokoCommon wr) {
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
    	if(wp.itemStr2("stat_unprint_flag").equals("Y")){
    		if(empty(wp.itemStr2("stat_unprint_s_month")) & empty(wp.itemStr2("stat_unprint_e_month"))){
    			errmsg("請輸入生效起迄年月 !!");
    			return;
    		}
    		if(wp.itemStr2("stat_unprint_s_month").compareTo(wp.itemStr2("stat_unprint_e_month")) == 1){
    			errmsg("起迄年月輸入錯誤!!");
    			return;
    		}
    	}   	

        if (this.isAdd())
        {
            return;
        }
        else
        {
            //-other modify-
            sqlWhere = " where acct_type = ? and acct_key = ?  and nvl(mod_seqno,0) = ?";
    		String wkAcctKey="";
			try {
				wkAcctKey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            Object[] param = new Object[] { wp.itemStr2("ex_acct_type"),wkAcctKey,wp.modSeqno() };
            if (this.isOtherModify("act_acno", sqlWhere, param)) {
    			errmsg("請重新查詢 !");
    			return;
    		}	
        }
    }

    @Override
    public int dbInsert() {
        actionInit("A");
        dataCheck();
        if (rc != 1){
            return rc;
        }
//        is_sql = "insert into act_acno ("
//	            + " parm_type "
//	              + ", reset_dd"
//	              + ", total_cnt"
//	              + ", max_amt"
//	              + ", excl_block_flag"
//	              + ", excl_block_reason"
//	              + ", excl_spec_flag"
//	              + ", excl_spec_reason"
//	            + ", apr_date, apr_user "
//	            + ", mod_time, mod_user, mod_pgm, mod_seqno"
//	            + " ) values ("
//	            + " 'EXCL_BLOCK', ?, ?, ?, ?, ?, ?, ? "
//	            + ", to_char(sysdate,'yyyymmdd'), ?"
//	            + ", sysdate,?,?,1"  
//	            + " )";
        //-set ?value-
//        Object[] param = new Object[] { 
//        	 wp.item_num("reset_dd")
//            , wp.item_num("total_cnt")
//            , wp.item_num("max_amt")
//            , wp.item_nvl("excl_block_flag", "")
//            , dropComma(wp.item_ss("excl_block_reason"))
//            , wp.item_nvl("excl_spec_flag", "")
//            , dropComma(wp.item_ss("excl_spec_reason"))
//            , wp.item_ss("zz_apr_user")
//            , wp.loginUser
//            , wp.item_ss("mod_pgm") 
//        };
//        sqlExec(is_sql, param);
        if (sqlRowNum <= 0) {
            errmsg(sqlErrtext);
        }

        return rc;
    }

    @Override
    public int dbUpdate() {
        actionInit("U");
        dataCheck();
        if(rc != 1){
            return rc;
        }
        String wkAcctKey="";
		try {
			wkAcctKey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        strSql = "update act_acno set "
        		  + "stat_unprint_flag=?"
	              + ", stat_unprint_s_month=?"
	              + ", stat_unprint_e_month=?"
	            + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
	            + " , mod_seqno =nvl(mod_seqno,0)+1 "
	            + sqlWhere;
        Object[] param = new Object[] { 
        		  wp.itemStr2("stat_unprint_flag")
                , wp.itemStr2("stat_unprint_s_month")
                , wp.itemStr2("stat_unprint_e_month")
            , wp.loginUser
            , wp.itemStr2("mod_pgm") 
            , wp.itemStr2("ex_acct_type")
            , wkAcctKey
            , wp.modSeqno()
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
        return rc;
    }
    
    String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}

}
