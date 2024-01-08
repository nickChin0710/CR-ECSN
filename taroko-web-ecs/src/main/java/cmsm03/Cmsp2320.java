/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-08-18  V1.00.00    yanghan     Initial                                 *
* 109-09-17  V1.00.01    shiyuqi    添加判斷條件                                                                                          *
* 109-10-16  V1.00.02    tanwei     updated for project coding standard      *
* 109-11-06  V1.00.03    sunny      fix alertMsg                             *
* 109-12-28  V1.00.04    Justin     zz -> comm
* 109-12-31  V1.00.05    shiyuqi    修改无意义命名                           *   
* 111-01-10  V1.00.06    Justin     增加欄位及改善使用者介面                 *
******************************************************************************/
package cmsm03;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Cmsp2320 extends BaseProc {
	CommString commString = new CommString();
	Cmsp2320Func func;

	//String kk1 = "";
	int ilOk = 0;
	int ilErr = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			dataProcess();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		// dddw_select();
		initButton();
	}

	private boolean getWhereStr() throws Exception {
		String lsDate1 = wp.itemStr("exDateS");
		String lsDate2 = wp.itemStr("exDateE");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[建檔日期-起迄]  輸入錯誤");
			return false;
		}

		wp.whereStr = "where 1=1 ";
		if (empty(wp.itemStr("exDateS")) == false) {
			wp.whereStr += " and cms_card_pwcntreset.MOD_DATE >=:crt_dates ";
			setString("crt_dates", wp.itemStr("exDateS"));
		}
		if (empty(wp.itemStr("exDateE")) == false) {
			wp.whereStr += " and cms_card_pwcntreset.MOD_DATE  <= :crt_datee ";
			setString("crt_datee", wp.itemStr("exDateE"));
		}
		if (empty(wp.itemStr("exUser")) == false) {//異動人員
			wp.whereStr += " and cms_card_pwcntreset.MOD_USER = :crt_user ";
			setString("crt_user", wp.itemStr("exUser"));
		}
		if (wp.itemStr("exAprState").equals("Y")) {//覆合狀態
			wp.whereStr += " and (cms_card_pwcntreset.apr_flag = 'Y') ";
		} else if (wp.itemStr("exAprState").equals("N")) {
			wp.whereStr += " and (cms_card_pwcntreset.apr_flag ='N') ";
		}

//		wp.whereOrder = " order by acct_type, acct_key";
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
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("cms_card_pwcntreset.apr_flag, ")
		  .append("cms_card_pwcntreset.card_no, ")
		  .append("cms_card_pwcntreset.old_passwd_err_count,")
		  .append("cms_card_pwcntreset.old_pw_err_count_resetdate, ")
		  .append("crd_idno.id_no, ")
		  .append("crd_idno.chi_name,")
		  .append("cms_card_pwcntreset.mod_date, ")
		  .append("cms_card_pwcntreset.mod_time, ")
		  .append("cms_card_pwcntreset.mod_user, ")
		  .append("cms_card_pwcntreset.apr_date, ")
		  .append("cms_card_pwcntreset.apr_time, ")
		  .append("cms_card_pwcntreset.apr_user, ")
		  .append("cms_card_pwcntreset.mod_seqno, ")
		  .append("decode(cms_card_pwcntreset.apr_flag, 'Y', 'disabled', '') as isDisable, ")
		  .append("cms_card_pwcntreset.id_p_seqno ");	
		wp.selectSQL = sb.toString();

		wp.daoTable = "cms_card_pwcntreset "
				+ "left join crd_card on crd_card.card_no=cms_card_pwcntreset.card_no "
				+ "left join crd_idno on crd_idno.ID_P_SEQNO=crd_card.ID_P_SEQNO";
		

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

//		listWkdata();
		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {

	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void dataProcess() throws Exception {

		func = new Cmsp2320Func(wp);
		String[] lsAprFlag = wp.itemBuff("apr_flag");
		String[] opt = wp.itemBuff("opt");
        
		wp.selectCnt = wp.itemBuff("ser_num").length;
		wp.sumLine = 0;
		wp.setListCount(1);
        
		// -insert-
		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
		    rc=1;
			rr = (int) this.toNum(opt[ii]) - 1;
			// 有換頁的opt處理, 須扣掉(行數 x 頁數)//
			rr = rr - (wp.pageRows * (wp.currPage - 1));
			if (rr < 0)
				continue;
	             
			if ("Y".equals(lsAprFlag[rr])) // 已覆核跳過
			{
				continue;
			}

			if (wp.itemEq(rr, "mod_user", wp.loginUser)) {
				alertMsg(" [覆核主管/維護經辦] 不可同一人");
				ilErr++;
				wp.colSet(rr, "ok_flag", "X");
				return;
			}
			
			rc = func.dataProcess(rr);
			sqlCommit(rc);
			if (rc == 1) {
				wp.colSet(rr, "ok_flag", "V");
				ilOk++;
				continue;
			}
			
			ilErr++;
			wp.colSet(rr, "ok_flag", "X");
		}
		//alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 不可覆核(覆核主管和異動經辦不可同一人)=" + llErrApr);
		alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
