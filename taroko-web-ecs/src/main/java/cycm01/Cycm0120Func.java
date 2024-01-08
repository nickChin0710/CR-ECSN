/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-25  V1.00.00  yash       program initial                            *
* 109-07-23  V1.00.01  Andy       Upadte : Mantis3822                        *
* 111/10/28  V1.00.02  Yang Bo    sync code from mega                        *
* 112/05/12  V1.00.03  Simon      1.remove ttl_minus_flag、hellow_word、combo_min_cond*
******************************************************************************/

package cycm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Cycm0120Func extends FuncEdit {
    String mKkAcctType = "";  

    public Cycm0120Func(TarokoCommon wr) {
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
            mKkAcctType = wp.itemStr("kk_acct_type");
        }else{
            mKkAcctType = wp.itemStr("acct_type");
        }
        
        if(!empty(wp.itemStr("run_print_cond"))){
        	if(empty(wp.itemStr("run_print_mm")) || wp.itemNum("run_print_mm") <= 0){
        		errmsg("連續列印月數需大於零!!");
                return;
        	}
        }
        
        
        
        if (this.isAdd())
        {
        	//檢查新增
            if(empty(mKkAcctType)){
            	errmsg("帳戶類別不可空白!!!");
                return;
            }
        	
            //檢查新增資料是否重複
            String lsSql = "select count(*) as tot_cnt from cyc_print where acct_type = ?";
            Object[] param = new Object[] {mKkAcctType};
            sqlSelect(lsSql, param);
            if (colNum("tot_cnt") > 0)
            {
                errmsg("資料已存在，無法新增");
                return;
            }
            
        }
        else
        {
            //-other modify-
            sqlWhere = " where acct_type = ?  and nvl(mod_seqno,0) = ?";
            Object[] param = new Object[] {mKkAcctType, wp.modSeqno() };
            isOtherModify("cyc_print", sqlWhere, param);
        }
    }

    @Override
    public int dbInsert() {
        actionInit("A");
        dataCheck();
        if (rc != 1){
            return rc;
        }
        strSql = "insert into cyc_print ("
	            + " acct_type "
	            + ", collection_flag "
	            + ", debit_flag "
	            + ", network_flag "
	            + ", email_flag "
	            + ", ttl_zero_flag "
	            + ", bonus_flag "
	            + ", problem_tx "
	            + ", overpay_amt "
	            + ", run_print_cond "
	            + ", run_print_mm "
	            + ", run_print_end_bal "
	            + ", crt_date, crt_user "
	            + ", overpay_one "
	            + ", mod_time, mod_user, mod_pgm, mod_seqno"
	            + " ) values ("
	            + "  ?,?,?,?,? "
	            + ", ?,?,?,?,? "
	            + ", ?,? "
	            + ", to_char(sysdate,'yyyymmdd'), ?,?"
	            + ", sysdate,?,?,1"  
	            + " )";
        //-set ?value-
        Object[] param = new Object[] {
                mKkAcctType // 1
            , wp.itemStr("collection_flag").equals("Y")?wp.itemStr("collection_flag"):"N"
            , wp.itemStr("debit_flag").equals("Y")?wp.itemStr("debit_flag"):"N"
            , wp.itemStr("network_flag").equals("Y")?wp.itemStr("network_flag"):"N"
            , wp.itemStr("email_flag").equals("Y")?wp.itemStr("email_flag"):"N"
            , wp.itemStr("ttl_zero_flag").equals("Y")?wp.itemStr("ttl_zero_flag"):"N"
            , wp.itemStr("bonus_flag").equals("Y")?wp.itemStr("bonus_flag"):"N"
            , wp.itemStr("problem_tx").equals("Y")?wp.itemStr("problem_tx"):"N"
            , wp.itemNum("overpay_amt")
            , wp.itemStr("run_print_cond").equals("Y")?wp.itemStr("run_print_cond"):"N"
            , wp.itemNum("run_print_mm")
            , wp.itemNum("run_print_end_bal")
            , wp.loginUser
            , wp.itemStr("overpay_one")
            , wp.loginUser
            , wp.itemStr("mod_pgm") 
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
        if(rc != 1){
            return rc;
        }

        strSql = "update cyc_print set "
	            + " collection_flag =? "
	            + " , debit_flag =? "
	            + " , network_flag =? "
	            + " , email_flag =? "
	            + " , ttl_zero_flag =? "
	            + " , bonus_flag =? "
	            + " , problem_tx =? "
	            + " , overpay_amt =? "
	            + " , run_print_cond =? "
	            + " , run_print_mm =? "
	            + " , run_print_end_bal =? "
	            + " , overpay_one =?"
	            + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
	            + " , mod_seqno =nvl(mod_seqno,0)+1 "
	            + sqlWhere;
        Object[] param = new Object[] { 
               wp.itemStr("collection_flag").equals("Y")?wp.itemStr("collection_flag"):"N"
             , wp.itemStr("debit_flag").equals("Y")?wp.itemStr("debit_flag"):"N"
             , wp.itemStr("network_flag").equals("Y")?wp.itemStr("network_flag"):"N"
             , wp.itemStr("email_flag").equals("Y")?wp.itemStr("email_flag"):"N"
             , wp.itemStr("ttl_zero_flag").equals("Y")?wp.itemStr("ttl_zero_flag"):"N"
             , wp.itemStr("bonus_flag").equals("Y")?wp.itemStr("bonus_flag"):"N"
             , wp.itemStr("problem_tx").equals("Y")?wp.itemStr("problem_tx"):"N"
             , wp.itemNum("overpay_amt")
             , wp.itemStr("run_print_cond").equals("Y")?wp.itemStr("run_print_cond"):"N"
             , wp.itemNum("run_print_mm")
             , wp.itemNum("run_print_end_bal")
             , wp.itemStr("overpay_one")
             , wp.loginUser
             , wp.itemStr("mod_pgm") 
             , mKkAcctType, wp.modSeqno()
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
        if(rc != 1){
            return rc;
        }
        strSql = "delete cyc_print " + sqlWhere;
        Object[] param = new Object[] {mKkAcctType, wp.modSeqno() };
        rc = sqlExec(strSql, param);
        if (sqlRowNum <= 0) {
	        errmsg(this.sqlErrtext);
        }
        return rc;
    }

}
