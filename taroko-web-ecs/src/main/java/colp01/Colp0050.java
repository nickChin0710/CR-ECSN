/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/06  V1.00.00    phopho     program initial                          *
*  108/11/27  V1.00.01    phopho     fix act_acno.p_seqno -> acno_p_seqno     *
*  109/01/21  V1.00.02    JustinWu    queried data: all -> acno_flag=1,2
*  109-05-06  V1.00.03    Zhanghuheng   updated for project coding standard 
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
* 112-10-24  V1.00.04   Ryan           增加覆核權限檢核                                                                              *    
* 112-11-15  V1.00.05   Ryan          移除acno_flag                                                                              *    
******************************************************************************/

package colp01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colp0050 extends BaseProc {
	CommString commString = new CommString();
	Colp0050Func func;

	String dataKK1 = "";
	int ilOk = 0;
	int ilErr = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		switch (wp.buttonCode) {
		case "X":
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
			break;
		case "C":
			// -資料處理-
			dataProcess();
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
		// case "A":
		// /* 新增功能 */
		// insertFunc();
		// break;
		// case "U":
		// /* 更新功能 */
		// updateFunc();
		// break;
		// case "D":
		// /* 刪除功能 */
		// deleteFunc();
		// break;
		case "M":
			/* 瀏覽功能 :skip-page */
			queryRead();
			break;
		case "S":
			/* 動態查詢 */
			querySelect();
			break;
		case "L":
			/* 清畫面 */
			strAction = "";
			clearFunc();
			break;
		default:
			break;
		}

		// dddw_select();
		initButton();
	}

	@Override
	public void dddwSelect() {

	}

	private boolean getWhereStr() throws Exception {
		wp.whereStr = "where 1=1 ";

		if (notEmpty(wp.itemStr("exUser"))) {
			wp.whereStr += " and col_acno_t.crt_user = :crt_user ";
			setString("crt_user", wp.itemStr("exUser"));
		}
		// 身分證號/統編的查詢條件
		if (notEmpty(wp.itemStr("exIdno"))) {
			wp.whereStr += "and acct_key like :acct_key ";
//			setString("id_no",wp.item_ss("exIdno")+"%");
			setString("acct_key", wp.itemStr("exIdno") + "%");
		}

		if (wp.itemStr("exType").equals("1")) {
			wp.whereStr += " and col_acno_t.no_delinquent_flag ='Y' ";
		} else if (wp.itemStr("exType").equals("2")) {
			wp.whereStr += " and col_acno_t.no_collection_flag ='Y' ";
		}

		// 2020-01-21 JustinWu: data whose acno_flag is not 3 or Y is not allowed to be
		// queried
		wp.whereStr += "and act_acno.acno_flag not in ('3','Y') ";

		wp.whereOrder = " order by act_acno.acct_type, act_acno.acct_key";
		// -page control-
		wp.queryWhere = wp.whereStr;

		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (getWhereStr() == false)
			return;

		wp.selectSQL = "col_acno_t.p_seqno, " + "act_acno.acct_type, " + "act_acno.acct_key, "
				+ "act_acno.acct_status,act_acno.acno_flag, "
				+ "decode(act_acno.acno_flag,'1',uf_idno_name(act_acno.id_p_seqno),'2',uf_corp_name(act_acno.corp_p_seqno)) as chi_name, "
				+ "crd_idno.id_p_seqno, " + "col_acno_t.no_delinquent_flag, " + "col_acno_t.no_delinquent_s_date, "
				+ "col_acno_t.no_delinquent_e_date, " + "col_acno_t.no_collection_flag, "
				+ "col_acno_t.no_collection_s_date, " + "col_acno_t.no_collection_e_date, " + "col_acno_t.crt_date, "
				+ "col_acno_t.crt_user ,col_acno_t.mod_user ";

		wp.daoTable = "col_acno_t "
				+ "left join act_acno on col_acno_t.p_seqno = act_acno.acno_p_seqno and act_acno.acno_flag != 'Y' "
				+ "left join crd_idno on act_acno.id_p_seqno = crd_idno.id_p_seqno ";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		listWkdata();
		wp.setPageValue();
		apprDisabled("mod_user");
	}

	void listWkdata() {
		String wkData = "";
		String[] cde = new String[] { "1", "2", "3", "4", "5" };
		String[] txt = new String[] { "1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清" };

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, "acct_status");
			wp.colSet(ii, "tt_acct_status", commString.decode(wkData, cde, txt));
		}
	}

	@Override
	public void querySelect() throws Exception {
		dataKK1 = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void dataProcess() throws Exception {
		func = new Colp0050Func(wp);

		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] lsAcctKey = wp.itemBuff("acct_key");
		String[] lsAcctType = wp.itemBuff("acct_type");
//		String[] lsAcnoFlag = wp.itemBuff("acno_flag");
		String[] lsNoDelinquentFlag = wp.itemBuff("no_delinquent_flag");
		String[] lsNoDelinquentSDate = wp.itemBuff("no_delinquent_s_date");
		String[] lsNoDelinquentEDate = wp.itemBuff("no_delinquent_e_date");
		String[] lsNoCollectionFlag = wp.itemBuff("no_collection_flag");
		String[] lsNoCollectionSDate = wp.itemBuff("no_collection_s_date");
		String[] lsNoCollectionEDate = wp.itemBuff("no_collection_e_date");
		String[] opt = wp.itemBuff("opt");
		int rowcntaa = 0;
		if (!(lsPSeqno == null) && !empty(lsPSeqno[0]))
			rowcntaa = lsPSeqno.length;
		wp.listCount[0] = rowcntaa;

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			// 2018.5.8 有換頁的opt處理, 須扣掉(行數 x 頁數)//
			rr = rr - (wp.pageRows * (wp.currPage - 1));
			// 2018.5.8 end//
			if (rr < 0)
				continue;

			wp.colSet(rr, "ok_flag", "-");

			func.varsSet("p_seqno", lsPSeqno[rr]);
			func.varsSet("acct_key", lsAcctKey[rr]);
			func.varsSet("acct_type",lsAcctType[rr]);
//			func.varsSet("acno_flag",lsAcnoFlag[rr]);
			func.varsSet("no_delinquent_flag", lsNoDelinquentFlag[rr]);
			func.varsSet("no_delinquent_s_date", lsNoDelinquentSDate[rr]);
			func.varsSet("no_delinquent_e_date", lsNoDelinquentEDate[rr]);
			func.varsSet("no_collection_flag", lsNoCollectionFlag[rr]);
			func.varsSet("no_collection_s_date", lsNoCollectionSDate[rr]);
			func.varsSet("no_collection_e_date", lsNoCollectionEDate[rr]);

			rc = func.dataProc();
			sqlCommit(rc);
			if (rc == 1) {
				wp.colSet(rr, "ok_flag", "V");
				ilOk++;
				continue;
			}
			ilErr++;
			wp.colSet(rr, "ok_flag", "X");

		}

		// -re-Query-
		// queryRead();
		alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
