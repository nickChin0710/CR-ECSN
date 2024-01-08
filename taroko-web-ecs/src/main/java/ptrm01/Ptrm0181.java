/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-09-13  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package ptrm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Ptrm0181 extends BaseEdit {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;
	ecsm01.Ecsm0620Func func = null;

	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		initButton();
	}

	// ************************************************************************
	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = "WHERE 1=1 and B.msg_type ='MAIL_TYPE'" + sqlCol(wp.itemStr("ex_mail_type"), "A.mail_type")
				+ sqlCol(wp.itemStr("ex_min_card_sheets"), "A.min_card_sheets")
				+ sqlCol(wp.itemStr("ex_max_card_sheets"), "A.max_card_sheets");

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = "hex(A.rowid) as rowid, " + "A.mail_type," + "A.min_card_sheets," + "A.max_card_sheets,"
				+ "A.bigtaipei_area_post," + "A.other_area_post," + "A.env_weight," + "A.card_weight," + "B.msg";

		wp.daoTable = "crd_postage A left join crd_message B on A.mail_type = B.msg_value ";
		wp.whereOrder = "order by A.mail_type,A.min_card_sheets";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.setPageValue();
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		String kkMailType = itemKk("data_k1");
		String kkMinCardSheets = itemKk("data_k2");
		String kkMaxCardSheets = itemKk("data_k3");
		if(empty(kkMailType))
			kkMailType = wp.itemStr("kk_mail_type");
		if(empty(kkMinCardSheets))
			kkMinCardSheets = wp.itemStr("kk_min_card_sheets");
		if(empty(kkMaxCardSheets))
			kkMaxCardSheets = wp.itemStr("kk_max_card_sheets");
		
		wp.selectSQL = "hex(A.rowid) as rowid, " + "A.mail_type," + "A.min_card_sheets," + "A.max_card_sheets,"
				+ "A.bigtaipei_area_post," + "A.other_area_post," + "A.env_weight," + "A.card_weight," + "A.mod_user,"
				+ "to_char(A.mod_time,'yyyymmdd') as mod_date," + "B.msg";

		wp.daoTable = "crd_postage A left join crd_message B on A.mail_type = B.msg_value ";
		wp.whereStr = "where 1=1 and B.msg_type ='MAIL_TYPE' ";
		wp.whereStr += sqlCol(kkMailType, "A.mail_type");
		wp.whereStr += sqlCol(kkMinCardSheets, "A.min_card_sheets");
		wp.whereStr += sqlCol(kkMaxCardSheets, "A.max_card_sheets");

		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料");
			return;
		}
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		ptrm01.Ptrm0181Func func = new ptrm01.Ptrm0181Func(wp);
		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_mail_type");
			if (wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = wp.itemStr("kk_mail_type");
			}
			dddwList("dddw_mail_type", "crd_message", "msg_value", "msg", "where 1=1 and msg_type = 'MAIL_TYPE'");

		} catch (Exception e) {
		}

	}

	// ************************************************************************
	@Override
	public void initPage() {
		return;
	}
	// ************************************************************************

} // End of class
