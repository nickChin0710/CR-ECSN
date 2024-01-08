/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import ofcapp.*;
import taroko.com.TarokoCommon;



/**
 * @author Administrator
 *
 */
public class Ptrm0060 extends BaseEdit {

	String mKkInstallmentType = "";
	
	/* (non-Javadoc)
	 * @see ofcapp.BaseEdit#actionFunction(taroko.com.TarokoCommon)
	 */
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		//ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
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
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 修改功能 */
			strAction = "U";
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

		//dddw_select();
		initButton();

	}

	//for query use only
	private void getWhereStr() throws Exception {
		sqlParm.clear();
    	wp.whereStr = "WHERE 1=1 and installment_type like :installment_type";
		setString("installment_type", wp.itemStr("ex_installment_type") + "%");
    }
	
	/* (non-Javadoc)
	 * @see ofcapp.BaseEdit#queryFunc()
	 */
	@Override
	public void queryFunc() throws Exception {
		getWhereStr();
        //-page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
		queryRead();
	}

	/* (non-Javadoc)
	 * @see ofcapp.BaseEdit#queryRead()
	 */
	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = ""
					+ "installment_type, "
					+ "installment_limit, "
					+ "confirm_over_day, "
					+" inst_m_code, "
					+" crt_user, "
					+" crt_date ";

		wp.daoTable = "ptr_instlmt";
		wp.whereOrder=" order by installment_type";
		
		getWhereStr();
		//-page control-
		
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		//list_wkdata();
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
	}

	/* (non-Javadoc)
	 * @see ofcapp.BaseEdit#querySelect()
	 */
	@Override
	public void querySelect() throws Exception {
	  mKkInstallmentType = wp.itemStr("data_k1");
		dataRead();
	}

	/* (non-Javadoc)
	 * @see ofcapp.BaseEdit#dataRead()
	 */
	@Override
	public void dataRead() throws Exception {
		if (empty(mKkInstallmentType)){
			//mKkInstallmentType = item_kk("installment_type");
		  mKkInstallmentType = itemKk("kk_installment_type");
		}
		if (isEmpty(mKkInstallmentType)){
			alertErr("分期付款種類不可空白");
			return;
		}
		
		wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
				  + "installment_type,   "
				  + "installment_limit, "
				  + "confirm_over_day, "
				  + "inst_m_code, "
				  + "crt_date, "
				  + "crt_user, "
				  + "mod_user, "
				  + "uf_2ymd(mod_time) as mod_date ";
		wp.daoTable = "ptr_instlmt";
		wp.whereStr = "where 1=1";
		wp.whereStr += " and installment_type = :installment_type";
		setString("installment_type", mKkInstallmentType);

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, installment_type=" + mKkInstallmentType);
		}
	}

	/* (non-Javadoc)
	 * @see ofcapp.BaseEdit#saveFunc()
	 */
	@Override
	public void saveFunc() throws Exception {
		Ptrm0060Func func =new Ptrm0060Func(wp);
		
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc!=1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc); 
	}

	/* (non-Javadoc)
	 * @see ofcapp.BaseEdit#init_button()
	 */
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			btnModeAud();	//rowid
		}
	}
	
	/*
	@Override
	public void initPage() {
		if (pos_any(wp.respHtml,"_detl")>0) {
			//wp.col_set("proc_code", "UNIT");
			wp.col_set("method_name", "showScreen");
		}
	}
	*/

}
