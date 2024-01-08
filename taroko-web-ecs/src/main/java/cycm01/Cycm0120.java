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
*                                 2.checkApprove() changed to checkApproveZz()*
******************************************************************************/

package cycm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Cycm0120 extends BaseEdit {
    String mExAcctType = "";

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
            //-資料讀取- 
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
            /* 瀏覽功能 :skip-page*/
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        }

        dddwSelect();
        initButton();
    }

    //for query use only
    private boolean getWhereStr() throws Exception {
        wp.whereStr =" where  a.acct_type = b.acct_type ";
        
        if(empty(wp.itemStr("ex_acct_type")) == false){
            wp.whereStr += " and  a.acct_type = :acct_type ";
            setString("acct_type", wp.itemStr("ex_acct_type"));
        }
          return true;
    }

    @Override
    public void queryFunc() throws Exception {
        getWhereStr();
        
        //-page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        wp.selectSQL = " a.acct_type"
	              + ", b.chin_name"
	              + ", a.apr_user"
	              + ", a.apr_date"
	              + ", a.crt_user"
	              + ", a.crt_date"
	              + ", a.mod_user"
	              + ", a.mod_time"
	              ;

        wp.daoTable = "ptr_acct_type b left join cyc_print a  on a.acct_type=b.acct_type";
        wp.whereOrder=" order by a.acct_type";
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
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        mExAcctType = wp.itemStr("kk_acct_type");
 
        if (empty(mExAcctType)){
            mExAcctType = itemKk("data_k1");
            wp.colSet("kk_acct_type", mExAcctType);
        }
        if(empty(mExAcctType)){
        	 mExAcctType = wp.colStr("acct_type");
        }


        wp.selectSQL = "hex(rowid) as rowid, mod_seqno "
	              + ", acct_type "
	              + ", collection_flag"
	              + ", debit_flag"
	              + ", network_flag"
	              + ", email_flag"
	              + ", ttl_zero_flag"
	              + ", bonus_flag"
	              + ", problem_tx"
	              + ", overpay_amt"
	              + ", run_print_cond"
	              + ", run_print_mm"
	              + ", run_print_end_bal"
	              + ", apr_user"
	              + ", apr_date"
	              + ", mod_user"
	              + ", mod_time"
	              + ", crt_date"
	              + ", crt_user"
	              + ", overpay_one "
	              ;
        wp.daoTable = "cyc_print";
        wp.whereStr = "where 1=1";
        wp.whereStr += " and  acct_type = :acct_type ";
        setString("acct_type", mExAcctType);

        pageSelect();
        if (sqlNotFind()) {
            alertErr("查無資料, 帳戶類別=" + mExAcctType);
        }
        wp.colSet("ddl_set", "disabled style='background: lightgray;'");
    }

    @Override
    public void saveFunc() throws Exception {
    	
    	//-check approve-
    //if (!checkApprove(wp.itemStr("zz_apr_user"),
    //      wp.itemStr("zz_apr_passwd")))
    	if (checkApproveZz()==false)
    	   {
    		  return;
    	   }

        Cycm0120Func func = new Cycm0120Func(wp);

        rc = func.dbSave(strAction);
        log(func.getMsg());
        if (rc != 1) {
            alertErr(func.getMsg());
        }
        this.sqlCommit(rc);
    }

    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
    }

    @Override
    public void dddwSelect() {
        try {
            if (wp.respHtml.indexOf("_detl") > 0){
            	wp.initOption="--";
                wp.optionKey = wp.colStr("kk_acct_type");
            }else{
            	wp.initOption="--";
                wp.optionKey = wp.itemStr("ex_acct_type");
            }
            this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1  order by acct_type");
        }
        catch(Exception ex) {}
    }

}
