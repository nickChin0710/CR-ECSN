/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-01-08  V1.00.04  Simon      apply apr_user.html field names changed    *
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon; 

public class Actm2025 extends BaseEdit {
    
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc=1;

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

	@Override
	public void initPage() {
		wp.colSet("pho_delete_disable", "disabled style='background-color: lightgray;'");
	}

	@Override
	public void queryFunc() throws Exception {
		//-page control-
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL =  " batch_no,      " +
						" batch_tot_cnt, " + 
						" batch_tot_amt, " + 
						" crt_user,      " +
						" crt_date,      " +
						" crt_time,      " +
						" trial_user,    " +
						" trial_date,    " +
						" trial_time,    " +
						" confirm_user,  " +
						" confirm_date,  " +
						" confirm_time   " ;
		
		wp.daoTable = " act_pay_batch ";

		wp.whereStr =" where batch_no = :ex_batch_no " ;
		setString("ex_batch_no", wp.itemStr2("ex_batch_no"));
		wp.queryWhere = wp.whereStr;
		
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

    /***
    wp.ddd("-->Actm2025-dsp01","");
    wp.ddd("--:wp.rootDir[%s]",wp.rootDir);
    ***/
		wp.colSet("pho_delete_disable", "");
		wp.setPageValue();

	}

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
    }

    @Override
    public void saveFunc() throws Exception {
    	ActmBaseFunc func =new ActmBaseFunc(wp);
    	
    	String lsBatch;
    	lsBatch = wp.itemStr2("batch_no");
    	if (empty(lsBatch)) {
    		alertErr("請先查詢資料後再刪除");
			return;
    	}
    	
    	if (!empty(wp.itemStr2("confirm_user"))) {
    		alertErr("主管已放行,不能刪除 !!");
			return;
    	}

    	if (wfApr() != 1) return;
    	
    	StringBuffer sb = new StringBuffer();
		sb.append("delete act_pay_detail where batch_no = '");
		sb.append(lsBatch);
		sb.append("'");
    	
    	func.deleteSql = sb.toString();
    	rc = func.dbDelete();
        if (rc!=1) {
            alertErr("繳款明細檔(act_pay_detail)，刪除失敗 ?!");
        	sqlCommit(0);
			return;
        }
        
        sb.delete(0, sb.length());
        sb.append("delete act_pay_batch where batch_no = '");
		sb.append(lsBatch);
		sb.append("'");
        
		func.deleteSql = sb.toString();
    	rc = func.dbDelete();
        if (rc!=1) {
            alertErr("繳款期限前後發簡訊名單檔(act_pay_batch)，刪除失敗 ?!");
        	sqlCommit(0);
			return;
        }
        
        sb.delete(0, sb.length());
        sb.append("delete act_pay_error where batch_no = '");
		sb.append(lsBatch);
		sb.append("'");
        
		func.deleteSql = sb.toString();
    	rc = func.dbDelete();
        if (rc!=1) {
            alertErr("錯誤等待補入檔(act_pay_error)，刪除失敗 ?!");
        	sqlCommit(0);
			return;
        }
        this.sqlCommit(rc);
    	
//    	func.deleteSql = "delete act_pay_detail where batch_no = '" + wp.item_ss("ex_batch_no")+ "'";
		
//        rc = func.dbDelete();
//        if (rc!=1) {
//            err_alert(func.getMsg());
//        }
//        this.sql_commit(rc);
    }
    
  	int wfApr() throws Exception {
  		if (!checkApprove(wp.itemStr2("approval_user"), wp.itemStr2("approval_passwd")))
  		{
  			return -1;
  		}
//  		func.vars_set("apr_user", wp.item_ss("zz_apr_user"));
//  		func.vars_set("apr_date", wp.sysDate);
//  		func.vars_set("apr_flag", "Y");
  		return 1;
  	}

    @Override
    public void initButton() {
    //if (wp.respHtml.indexOf("_detl") > 0) {
    //    this.btnMode_aud();
    //}
    	String sKey = "1st-page";
    //wp.col_set("btnDelete_disable","");
      this.btnModeAud(sKey);

    }

    @Override
    public void dddwSelect() {
        try {	
			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_batch_no");
			dddwList("dddw_batch_no", "act_pay_batch", "act_pay_batch.batch_no", "", "WHERE ( (substr(act_pay_batch.batch_no,9,1) like '0' OR substr(act_pay_batch.batch_no,9,1) like '1') ) AND ( act_pay_batch.confirm_user ='' ) ORDER BY act_pay_batch.batch_no ASC");
        }
        catch(Exception ex) {}
    }

}

