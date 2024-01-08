/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-30  V1.00.01  Alex       program initial
* 109-04-28  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package rskm03;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Rskm3310 extends BaseAction {
	String cardNo = "";

	@Override
	public void userAction() throws Exception {
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
			wp.colSet("add_type", "1");
			wp.colSet("tt_add_type", "人工建檔");
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "S1")) {
			/* show匯入資料 */
			showImpData();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料存檔-code_type 1:刪除 2:資料匯入
			if (wp.itemEq("code_type", "1")) {
				procFuncDelete();
			} else if (wp.itemEq("code_type", "2")) {
				procFunc();
			}
		}
	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("建檔日期:起迄錯誤");
			return;
		}

		getWhere();

		wp.setQueryMode();

		queryRead();
	}

	void getWhere() {
		sqlParm.clear();
		String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
				+ sqlCol(wp.itemStr("ex_add_type"), "add_type") + sqlBetween("ex_date1", "ex_date2", "crt_date");

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " card_no , add_type , card_remark , decode(add_type,'1','人工建檔','2','批次匯入') as tt_add_type , '' as err_mesg ";
		wp.daoTable = " rsk_block_card ";
		wp.whereOrder = " order by card_no Asc ";
		getWhere();
		pageQuery();
		if (sqlNotFind()) {
			alertErr2("此條件查無資料 !");
			return;
		}

		wp.setListCount(0);
		wp.setPageValue();
		wp.colSet("code_type", "1");
	}

	@Override
	public void querySelect() throws Exception {
		cardNo = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (empty(cardNo))
			cardNo = itemkk("card_no");
		if (empty(cardNo)) {
			alertErr2("黑名單卡號: 不可空白 !");
			return;
		}

		wp.selectSQL = " card_no , add_type , card_remark , decode(add_type,'1','人工建檔','2','批次匯入') as tt_add_type , "
				+ " apr_user , apr_date , crt_user , crt_date , mod_user , to_char(mod_time,'yyyymmdd') as mod_date , "
				+ " hex(rowid) as rowid ";

		wp.daoTable = " rsk_block_card ";
		wp.whereStr = " where 1=1 " + sqlCol(cardNo, "card_no");

		pageSelect();

		if (sqlNotFind()) {
			alertErr2("此條件查無資料 !");
			return;
		}
	}

	@Override
	public void saveFunc() throws Exception {
		rskm03.Rskm3310Func func = new rskm03.Rskm3310Func();
		func.setConn(wp);

		if (checkApproveZz() == false)
			return;

		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if (rc != 1) {
			alertErr2(func.getMsg());
		} else
			saveAfter(false);

	}

	@Override
	public void procFunc() throws Exception {
		int llCnt = 0, llOk = 0, llErr = 0;
		String[] opt = wp.itemBuff("opt");
		String[] lsCardNo = wp.itemBuff("card_no");
		String[] lsCardRemark = wp.itemBuff("card_remark");
		String[] lsErrMesg = wp.itemBuff("err_mesg");
		optNumKeep(wp.itemRows("card_no"));
		wp.listCount[0] = wp.itemRows("card_no");

		if (checkApproveZz() == false)
			return;

		rskm03.Rskm3310Func func = new rskm03.Rskm3310Func();
		func.setConn(wp);

		for (int ii = 0; ii < wp.itemRows("card_no"); ii++) {
			if (checkBoxOptOn(ii, opt) || empty(lsErrMesg[ii]) == false)
				continue;
			llCnt++;
			func.varsSet("card_no", lsCardNo[ii]);
			func.varsSet("card_remark", lsCardRemark[ii]);

			if (func.dataUpload() == 1) {
				wp.colSet(ii, "ok_flag", "V");
				llOk++;
				sqlCommit(1);
			} else {
				wp.colSet(ii, "ok_flag", "V");
				llErr++;
				dbRollback();
			}

		}

		alertMsg("資料存檔完成 , 成功:" + llOk + " 失敗:" + llErr);

	}

	@Override
	public void initButton() {
		if (eqIgno(wp.respHtml, "rskm3310")) {
			btnModeAud(wp.colStr("code_type"));
			if (wp.autUpdate() == false)
				buttonOff("btnUpload");
		} else {
			btnModeAud(wp.colStr("rowid"));
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

	void showImpData() throws Exception {

		TarokoFileAccess tf = new TarokoFileAccess(wp);
		String inputFile = wp.itemStr("zz_file_name");
		if (empty(inputFile)) {
			alertErr2("匯入檔名:不可空白");
			return;
		}
		int fi = tf.openInputText(inputFile, "MS950");
		if (fi == -1) {
			return;
		}
		wp.pageRows = 999;
		int llOk = 0, llCnt = 0;
		while (true) {
			String fiel = tf.readTextFile(fi);
			if (tf.endFile[fi].equals("Y")) {
				break;
			}
			if (fiel.length() < 2) {
				continue;
			}

			String[] tt = new String[2];
			tt[0] = fiel;
			tt = commString.token(tt, ",");
			String lsCardNo = tt[1];
			wp.colSet(llCnt, "card_no", lsCardNo);
			tt = commString.token(tt, ",");
			wp.colSet(llCnt, "card_remark", tt[1]);
			wp.colSet(llCnt, "add_type", "2");
			wp.colSet(llCnt, "tt_add_type", "批次匯入");

			if (lsCardNo.trim().isEmpty() == false) {
				if (checkDup(lsCardNo)) {
					wp.colSet(llCnt, "err_mesg", "卡號已存在黑名單卡號檔 !");
					wp.colSet(llCnt, "opt_on", "checked");
				} else {
					wp.colSet(llCnt, "err_mesg", "");
				}
			} else {
				wp.colSet(llCnt, "err_mesg", "卡號不可空白 !");
				wp.colSet(llCnt, "opt_on", "checked");
			}

			if (llCnt >= 9) {
				wp.colSet(llCnt, "ser_num", commString.intToStr(llCnt + 1));
			} else {
				wp.colSet(llCnt, "ser_num", "0" + commString.intToStr(llCnt + 1));
			}
			llCnt++;
		}

		tf.closeInputText(fi);
		wp.listCount[0] = llCnt;
		wp.colSet("code_type", "2");
		return;
	}

	boolean checkDup(String lsCardNo) {
		String sql1 = " select count(*) as db_cnt from rsk_block_card where card_no = ? ";
		sqlSelect(sql1, new Object[] { lsCardNo });

		if (sqlNum("db_cnt") > 0)
			return true;

		return false;
	}

	void procFuncDelete() throws Exception {
		int llCnt = 0, llOk = 0, llErr = 0;
		String[] opt = wp.itemBuff("opt");
		String[] lsCardNo = wp.itemBuff("card_no");
		String[] lsCardRemark = wp.itemBuff("card_remark");
		String[] lsAddType = wp.itemBuff("add_type");
		optNumKeep(wp.itemRows("card_no"));
		wp.listCount[0] = wp.itemRows("card_no");

		if (checkApproveZz() == false)
			return;

		rskm03.Rskm3310Func func = new rskm03.Rskm3310Func();
		func.setConn(wp);

		int rr = -1;
		rr = optToIndex(opt[0]);
		if (rr < 0) {
			alertErr2("請點選欲刪除資料");
			return;
		}

		for (int ii = 0; ii < opt.length; ii++) {
			rr = optToIndex(opt[ii]);
			if (rr < 0) {
				continue;
			}
			wp.colSet(rr, "ok_flag", "-");

			func.varsSet("card_no", lsCardNo[rr]);
			func.varsSet("card_remark", lsCardRemark[rr]);
			func.varsSet("add_type", lsAddType[rr]);

			rc = func.dataDelete();
			sqlCommit(rc);
			if (rc == 1) {
				wp.colSet(rr, "ok_flag", "V");
				llOk++;
				continue;
			}
			llErr++;
			wp.colSet(rr, "ok_flag", "X");
		}

		alertMsg("存檔完成: 成功:" + llOk + " 失敗筆數:" + llErr);

	}

}
