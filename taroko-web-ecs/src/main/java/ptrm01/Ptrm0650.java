/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 108-12-03  V1.00.01  Amber	  Update     								 *
* 109-06-03  V1.00.02  Andy	      Update : Mantis3575 
* 111-12-09  V1.00.03  Machao    sync from mega & updated for project coding standard & 覆核bug調整  					 *
******************************************************************************/
package ptrm01;


import ofcapp.*;
import taroko.com.TarokoCommon;


/**
 * @author Administrator
 *
 */
public class Ptrm0650 extends BaseEdit {

	String mKkBinType = "";
	
	/* (non-Javadoc)
	 * @see ofcapp.baseEdit#actionFunction(taroko.com.TarokoCommon)
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

		dddwSelect();
		initButton();

	}

	//for query use only
	private void getWhereStr() throws Exception {
		wp.whereStr = "WHERE 1=1 ";
		if(empty(wp.itemStr2("ex_bin_type")) == false){
			wp.whereStr  += " and  bin_type = :bin_type ";
			setString("bin_type", wp.itemStr2("ex_bin_type"));
		}
    }
	
	/* (non-Javadoc)
	 * @see ofcapp.baseEdit#queryFunc()
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
	 * @see ofcapp.baseEdit#queryRead()
	 */
	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = ""
					+ "bin_type, "
					+ "decode(bin_type,'V','V:Visa卡','M','M:Master卡','J','J:Jcb卡','C','C:銀聯卡','A','A:AE卡',bin_type) db_bin_type, "
					+ "SUBSTR(service_ver, 1, LOCATE('.',service_ver) - 1) as db_v1, "
					+ "SUBSTR(service_ver, LOCATE('.',service_ver) + 1, LOCATE('.',service_ver, LOCATE('.',service_ver) + 1) - LOCATE('.',service_ver) - 1) as db_v2, "
					+ "SUBSTR(service_ver, LOCATE('.',service_ver, LOCATE('.',service_ver)+1)+1) as db_v3 ";

		wp.daoTable = "ptr_service_ver";
		wp.whereOrder=" order by bin_type";
		
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

	/* (non-Javadoc)
	 * @see ofcapp.baseEdit#querySelect()
	 */
	@Override
	public void querySelect() throws Exception {
		mKkBinType = wp.itemStr2("data_k1");
		dataRead();
	}

	/* (non-Javadoc)
	 * @see ofcapp.baseEdit#dataRead()
	 */
	@Override
	public void dataRead() throws Exception {
		if (empty(mKkBinType)){
			//m_kk_installment_type = item_kk("installment_type");
			mKkBinType = itemKk("kk_bin_type");
		}
		if (isEmpty(mKkBinType)){
			alertErr("國際組織別不可空白");
			return;
		}
		
		wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
				  + "bin_type,   "
				  + "SUBSTR(service_ver, 1, LOCATE('.',service_ver) - 1) as db_v1, "
			      + "SUBSTR(service_ver, LOCATE('.',service_ver) + 1, LOCATE('.',service_ver, LOCATE('.',service_ver) + 1) - LOCATE('.',service_ver) - 1) as db_v2, "
				  + "SUBSTR(service_ver, LOCATE('.',service_ver, LOCATE('.',service_ver)+1)+1) as db_v3, "
				  + "mod_user, "+"uf_2ymd(mod_time) as mod_date ";
		wp.daoTable = "ptr_service_ver";
		wp.whereStr = "where 1=1";
		wp.whereStr  += " and  bin_type = :bin_type ";
		setString("bin_type", mKkBinType);

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, m_kk_bin_type=" + mKkBinType);
		}
	}

	/* (non-Javadoc)
	 * @see ofcapp.baseEdit#saveFunc()
	 */
	@Override
	public void saveFunc() throws Exception {
		
		//-check approve-
		if (!checkApprove(wp.itemStr2("approval_user"),
				wp.itemStr2("approval_passwd"))){
			return;
		}
				
		Ptrm0650Func func =new Ptrm0650Func(wp);
		
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc!=1) {
			alertErr(func.getMsg());
		}
		this.sqlCommit(rc); 
	}

	/* (non-Javadoc)
	 * @see ofcapp.baseEdit#init_button()
	 */
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			btnModeAud();	//rowid
		}
	}
	
	@Override
	public void dddwSelect() {
		try {
//			wp.optionKey = wp.item_ss("kk_bin_type");
//			this.dddw_list("dddw_bin_type","ptr_bintable","bin_type","","group by bin_type order by bin_type");
		}
		catch(Exception ex) {}
	}

}
