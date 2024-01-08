/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-07-27  V1.00.01  Ryan       Initial                                  *
***************************************************************************/
package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Crdm9999 extends BaseEdit {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;

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
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.sqlCmd = " SELECT * from(SELECT TSC_CARD_NO,CURRENT_CODE,CARD_NO,NEW_END_DATE FROM TSC_CARD WHERE TSC_CARD_NO IN ( ";
		wp.sqlCmd += " SELECT TSC_CARD_NO FROM(SELECT * FROM CRD_CARD A, (SELECT * FROM ( ";
		wp.sqlCmd += " SELECT ROW_NUMBER() OVER (PARTITION BY CARD_NO ORDER BY TSC_CARD_NO DESC) AS ROW_NO,CARD_NO,TSC_CARD_NO,CURRENT_CODE,MOD_USER FROM TSC_CARD) ";
		wp.sqlCmd += " WHERE CURRENT_CODE<>'0' AND ROW_NO='1' ) B ";
		wp.sqlCmd += " WHERE A.CARD_NO = B.CARD_NO AND A.CURRENT_CODE = '0' AND B.CURRENT_CODE <> '0' AND A.ISSUE_DATE <= '20220311' AND B.MOD_USER='SYSCNV'))) ";
		wp.sqlCmd += " WHERE 1=1 ";
		wp.sqlCmd += sqlCol(wp.itemStr("ex_tsc_card_no"), "tsc_card_no");

		wp.pageCountSql = "select count(*) ct from ( ";
		wp.pageCountSql += wp.sqlCmd;
		wp.pageCountSql += ")";
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
		
		String tscCardNo = itemKk("data_k1");
		if(empty(tscCardNo)) {
			tscCardNo = wp.itemStr("tsc_card_no");
		}
		
		wp.selectSQL = " hex(rowid) as rowid,TSC_CARD_NO,CURRENT_CODE,CARD_NO,NEW_END_DATE,to_char(mod_time,'yyyymmdd') as mod_date,mod_user ";
		wp.daoTable = "TSC_CARD";
		wp.whereStr = "where 1=1 ";
		wp.whereStr += sqlCol(tscCardNo, "tsc_card_no");

		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料, key= " + "[" + tscCardNo + "]");
			return;
		}
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		crdm01.Crdm9999Func func = new crdm01.Crdm9999Func(wp);
		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
		if(rc == 1 ) dataRead();
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
	}

	// ************************************************************************
	@Override
	public void initPage() {
		return;
	}
	// ************************************************************************

} // End of class
