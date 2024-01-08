/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE             Version    AUTHOR      DESCRIPTION                               *
* ---------        --------     ----------     ------------------------------------------ *
* 110-07-07  V1.00.00   suzuwei       新增                                                                                      *    
* 112-06-13  V1.00.01   suzuwei       己覆核資料,需能點下按鈕後進入次頁                                                                                      *    
******************************************************************************/
package mktm01;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Mktm0220 extends BaseAction {
	String itemNo = "", projCode = "", dataType = "", aprFlag = "";

	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
		switch (wp.buttonCode) {
		case "X":
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
			break;
		case "Q":
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
			break;
		case "R":
			// -資料讀取-
			strAction = "R";
			dataRead();
			break;
		case "A":
			/* 新增功能 */
			saveFunc();
			break;
		case "U":
			/* 更新功能 */
			saveFunc();
			break;
		case "D":
			/* 刪除功能 */
			saveFunc();
			break;
		case "M":
			/* 瀏覽功能 :skip-page */
			queryRead();
			break;
		case "S":
			/* 動態查詢 */
			querySelect();
			break;
		case "R2":
			/* 明細頁面查詢 */
			dataReadDetl();
			break;
		case "L":
			/* 清畫面 */
			strAction = "";
			clearFunc();
			break;
		case "C":
			// -異動處理-
			procFunc();
			break;
		case "C1":
			// -覆核處理-
			procApprove();
			break;

//		 case "A2": 
//		 //-新增明細- 
//		  insertDetl(); 
//		  break;

		case "U2":
			// 明細存檔--
			updateDetl();
			break;
//		case "C2":
//			// 明細存檔--
//			detlUpload();
//			break;
		case "AJAX":
			// -AJAX:新增明細--
			ajaxFunc();
			break;
		default:
			break;
		}
	}

	@Override
	public void dddwSelect() {
		try {
			if (eqIgno(wp.respHtml, "mktm0220")) {
			} else if (eqIgno(wp.respHtml, "mktm0220_detl")) {
//				wp.optionKey = wp.colStr("kk_item_no");
//				dddwList("dddw_item_no", "ptr_sys_idtab", "wf_id", "wf_id||'_'||wf_desc",
//						"where wf_type = 'RIGHT_ITEM_NO' and wf_id in ('08','09','10','11')  order by wf_id");
			}

			if ((wp.respHtml.equals("mktm0220_actp"))) {
				wp.initOption = "";
				wp.optionKey = "";
				this.dddwList("dddw_acct_type", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)", " where 1 = 1 ");
			}
			if ((wp.respHtml.equals("mktm0220_caty"))) {
				wp.initOption = "";
				wp.optionKey = "";
				this.dddwList("dddw_card_type", "ptr_card_type", "trim(card_type)", "trim(name)", " where 1 = 1 ");
			}
			if ((wp.respHtml.equals("mktm0220_gpcd"))) {
				wp.initOption = "";
				wp.optionKey = "";
				this.dddwList("dddw_group_code", "ptr_group_code", "trim(group_code)", "trim(group_name)",
						" where 1 = 1 ");
			}
			if ((wp.respHtml.equals("mktm0220_mccc"))) {
				wp.initOption = "";
				wp.optionKey = "";
				this.dddwList("dddw_mcc_code", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
						" where 1 = 1 ");
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {

		String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_proj_code"), "proj_code");

		if (!wp.itemEq("ex_active_status", "0")) {
			lsWhere += sqlCol(wp.itemStr("ex_active_status"), "active_status");
		}

		if (!wp.itemEq("ex_apr_flag", "0")) {
			lsWhere += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
		}

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " hex(rowid) as rowid, apr_flag , " + " proj_code , " + " proj_desc , "
				+ " consume_type , decode(consume_type, '1', '1. 正卡ID', '2', '2.正附卡合併', '3', '3.正附卡分開', consume_type) as consume_type_desc, " // 消費金額累積方式
				+ " proj_date_s , " + " proj_date_e , " + " mod_user , "
				+ " to_char(mod_time,'yyyymmdd') as mod_date , " + " apr_date , " + " apr_user  ";

		wp.daoTable = " mkt_jointly_parm ";
		wp.whereOrder = " order by proj_code ";

		pageQuery();
		wp.setListCount(0);
		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}

		wp.setPageValue();

	}

	@Override
	public void querySelect() throws Exception {
		projCode = wp.itemStr2("data_k1");

		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (empty(projCode)) {
			projCode = itemkk("proj_code");
		}
		wp.selectSQL = " hex(A.rowid) as rowid, A.* ";
		wp.daoTable = " mkt_jointly_parm A ";
		wp.whereStr = " where 1=1 " + sqlCol(projCode, "A.proj_code");
		wp.whereOrder = " order by A.proj_code " + commSqlStr.rownum(1);
		pageSelect();
		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}

//		if (wp.colEq("apr_flag", "N")) {
//			alertMsg("資料待覆核");
//		}

	}

	void dataReadDetl() throws Exception {
		wp.pageRows = 999;

		projCode = wp.itemStr2("proj_code");
		dataType = wp.itemStr2("data_k1");
		aprFlag = wp.itemStr2("apr_flag");
		// -master-
//		 selectCmsRightParm();

		if (empty(dataType))
			dataType = wp.itemStr2("data_type");

		wp.daoTable = " mkt_jointly_parm_detl A";
		wp.whereStr = " where 1=1 " + sqlCol(projCode, "A.proj_code") + sqlCol(dataType, "A.data_type");
		wp.whereOrder = " order by A.data_code ";
		// acty
		if (eqIgno(dataType, "01")) {
			wp.selectSQL = " a.data_type, A.data_code, uf_tt_acct_type(A.data_code) as tt_data_code ";
		} else if (eqIgno(dataType, "02")) {
			// gpcd
			wp.selectSQL = " a.data_type, A.data_code , uf_tt_group_code(A.data_code) as tt_data_code ";
		} else if (eqIgno(dataType, "03")) {
			// caty
			wp.selectSQL = " a.data_type, A.data_code , uf_tt_card_type(A.data_code) as tt_data_code ";
		} else if (eqIgno(dataType, "04")) {
			// mccc
			wp.selectSQL = " a.data_type, A.data_code , uf_tt_mcc_code(A.data_code) as tt_data_code ";
		} else {
			// mrch
			wp.selectSQL = " a.data_type, A.data_code , A.data_code as tt_data_code ";
		}

		pageQuery();
		wp.setListCount(0);
		if (sqlNotFind()) {
			selectOK();
			return;
		}
//   dataReadDetl_After();
	}

//	void selectCmsRightParm() {
//		String sql1 = "select debut_group_cond from cms_right_parm" + " where 1=1" + sqlCol(itemNo, "item_no")
//				+ sqlCol(projCode, "proj_code") + sqlCol(aprFlag, "apr_flag");
//		sqlSelect(sql1);
//		if (sqlRowNum > 0) {
//			wp.colSet("debut_group_cond", sqlStr("debut_group_cond"));
//		}
//	}

	@Override
	public void saveFunc() throws Exception {
		if (isDelete() && wp.itemEq("apr_flag", "Y")) {
			if (checkApproveZz() == false)
				return;
		}

		Mktm0220Func func = new Mktm0220Func();
		func.setConn(wp);

		rc = func.dbSave(strAction);
		sqlCommit(rc);

		if (rc != 1) {
			alertErr2(func.getMsg());
		} else
			saveAfter(true);

	}

//void insertDetl() throws Exception {
//   wp.listCount[0] = wp.itemRows("data_code");
//
//   if (wp.itemEq("apr_flag","Y")) {
//      alertErr("資料己覆核, 不可新增");
//      return;
//   }
//
//   Cmsm4210Func func =new Cmsm4210Func();
//   func.setConn(wp);
//   rc =func.insertDetl();
//   sqlCommit(rc);
//   if (rc ==-1) {
//      alertErr(func.getMsg());
//   }
//}

	void updateDetl() throws Exception {
		int ilOk = 0, ilErr = 0;

		Mktm0220Func func = new Mktm0220Func();
		func.setConn(wp);

		String[] aaOpt = wp.itemBuff("opt");
		wp.listCount[0] = wp.itemRows("data_code");

		// --已覆核不可修改明細
		if (wp.itemEq("apr_flag", "Y")) {
			alertErr2("已覆核不可修改 !");
			return;
		}

//		rc = func.updateRightParm();
		if (rc == -1) {
			sqlCommit(rc);
			alertErr(func.getMsg());
			return;
		}

//   if (opt_2index(aa_opt[0])<0) {
//      alert_err("未點選 [刪除] 資料");
//      return;
//   }
		for (int ii = 0; ii < aaOpt.length; ii++) {
			int rr = optToIndex(aaOpt[ii]);
			if (rr < 0)
				continue;

			optOkflag(rr);
			if (func.deleteDetl(rr) == 1) {
				ilOk++;
				optOkflag(rr, 1);
			} else {
				ilErr++;
				optOkflag(rr, -1);
			}
		}

		if (ilOk > 0)
			sqlCommit(1);

		dataReadDetl();

		alertMsg("處理結果: 成功:" + ilOk + " , 失敗:" + ilErr);
	}

	@Override
	public void procFunc() throws Exception {

		Mktm0220Func func = new Mktm0220Func();
		func.setConn(wp);

		rc = func.dataProc();
		sqlCommit(rc);
		if (rc != 1) {
			errmsg(func.getMsg());
		} else {
			alertMsg("異動處理完成");
			dataRead();
		}
	}

	void procApprove() throws Exception {
		int ilOk = 0, ilErr = 0;

		Mktm0220Func func = new Mktm0220Func();
		func.setConn(wp);

		String[] lsProjCode = wp.itemBuff("proj_code");
		String[] lsAprFlag = wp.itemBuff("apr_flag");
		String[] aaOpt = wp.itemBuff("opt");
		this.optNumKeep(lsProjCode.length, aaOpt);
		wp.listCount[0] = wp.itemRows("proj_code");

		int rr = -1;
		rr = optToIndex(aaOpt[0]);

		if (rr < 0) {
			alertErr2("請點選欲覆核資料");
			return;
		}

//		if (checkApproveZz() == false)
//			return;

		for (int ii = 0; ii < aaOpt.length; ii++) {
			rr = (int) optToIndex(aaOpt[ii]);
			if (rr < 0) {
				continue;
			}
			// 若已覆核，則無法再覆核。
			if (lsAprFlag[rr].equals("Y")) {
				ilErr++;
				wp.colSet(rr, "ok_flag", "X");
				continue;
			}
			wp.colSet(rr, "ok_flag", "-");

			func.varsSet("proj_code", lsProjCode[rr]);

			rc = func.dataApprove();
			sqlCommit(rc);
			if (rc == 1) {
				wp.colSet(rr, "ok_flag", "V");
				ilOk++;
				continue;
			}
			ilErr++;
			wp.colSet(rr, "ok_flag", "X");
		}

		alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

	}

	void ajaxFunc() throws Exception {
		if (wp.itemEq("apr_flag", "Y")) {
			alertErr("資料己覆核, 不可新增");
			return;
		}

		Mktm0220Func func = new Mktm0220Func();
		func.setConn(wp);
		rc = func.insertDetl();
		sqlCommit(rc);
		if (eqIgno(strAction, "AJAX")) {
			wp.addJSON("ax_rc", "" + rc);
			if (rc == 1) {
				wp.addJSON("ax_msg", "");
			} else
				wp.addJSON("ax_msg", func.getMsg());
			return;
		}
		if (rc == -1) {
			alertErr(func.getMsg());
		}
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			btnModeAud();
			btnAddOn(wp.itemEmpty("proj_code"));
			btnUpdateOn(!wp.itemEmpty("proj_code") && !wp.colEq("apr_flag", "Y"));
			btnDeleteOn(!wp.itemEmpty("proj_code") && !wp.colEq("apr_flag", "Y"));
		}
		if (wp.itemEmpty("proj_code") || wp.itemEq("apr_flag", "Y")) {
		    wp.colSet("btnUpdate_off", "disabled");
		}
	}

	@Override
	public void initPage() {
	}

}
