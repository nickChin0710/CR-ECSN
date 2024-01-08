/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-21  V1.00.00  Andy       program initial                            *
* 109-04-15  V1.00.01  Alex       add auth_query									  *
* 109-09-02  V1.00.02  Amber      Update Mantis:0004032
* 111-10-20  V1.00.03  Machao      sync from mega & updated for project coding standard                      *
******************************************************************************/

package actm01;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actm2090 extends BaseEdit {

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			// clearFunc();
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
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			// deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			// queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			// querySelect();
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
		// 設定初始搜尋條件值
		String sysdate1 = "";
		sysdate1 = strMid(getSysDate(), 0, 6);
		wp.colSet("stat_unprint_s_month", strMid(sysdate1, 0, 6));
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
		wp.whereStr = " where 1=1 ";

		if (empty(wp.itemStr2("ex_acct_type")) == false) {
			wp.whereStr += " and  acct_type = :ex_acct_type ";
			setString("ex_acct_type", wp.itemStr2("ex_acct_type"));
		}

		if (empty(wp.itemStr2("ex_acct_key")) == false) {
			String lsAcctKey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
			wp.whereStr += " and  acct_key = :ex_acct_key ";
			setString("ex_acct_key", lsAcctKey);
		}

		return true;
	}

	@Override
	public void queryFunc() throws Exception {
	}

	@Override
	public void queryRead() throws Exception {
	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		
		if(wp.itemEmpty("ex_acct_key")){
			alertErr2("帳戶帳號: 不可空白");
			return ;
		}
		
		String lsAcctKey = "";
		lsAcctKey = commString.acctKey(wp.itemStr2("ex_acct_key"));
		if(lsAcctKey.length()!=11){
			alertErr2("帳戶帳號: 輸入錯誤");
			return ;
		}

		ColFunc func =new ColFunc();
		func.setConn(wp);	   
	    
		if (func.fAuthQuery(wp.modPgm(), commString.mid(lsAcctKey, 0,10))!=1) { 
	     	alertErr2(func.getMsg()); 
	     	return ; 
	   }
		
		wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
				+ "stat_unprint_flag, "
				+ "stat_unprint_s_month, "
				+ "stat_unprint_e_month, "
				+ "acct_status, "
				+ "decode(acct_status,'1','1.正常','2','2.逾放','3','3.催收','4','4.呆帳','5','5.結清',acct_status) as accstatus, "
				+ "UF_IDNO_NAME(id_p_seqno) as iname, "
				+ "UF_CORP_NAME(corp_p_seqno) as cname ";
		wp.daoTable = "act_acno";

		getWhereStr();
		// System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
		// + wp.whereStr + wp.whereOrder);

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料");
		}
		listWkdata();
	}

	void listWkdata() throws Exception {
		String statUnprintFlag = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			statUnprintFlag = wp.colStr("stat_unprint_flag");
			if (!statUnprintFlag.equals("Y")) {
				wp.colSet(ii, "stat_unprint_flag", "N");
			}
		}
	}

	@Override
	public void saveFunc() throws Exception {
		Actm2090Func func = new Actm2090Func(wp);
		String sMonth = "", eMonth = "", nMonth = "";
		String statUnprintFlag = wp.colStr("stat_unprint_flag");
		sMonth = wp.itemStr2("stat_unprint_s_month");
		eMonth = wp.itemStr2("stat_unprint_e_month");
		if(empty(wp.itemStr2("rowid"))){
			alertErr("請先輸入帳戶帳號讀取資料!!");
			return;
		}
		//2020/09/02 Mantis:0004032 不需設定迄日
		if (statUnprintFlag.equals("Y")) {
			if(empty(sMonth)){
				alertErr("生效年月(起)不可空白!!");
				return;
			}
			if(!empty(sMonth) & !empty(eMonth)){
				if(eMonth.compareTo(sMonth) < 0){
					alertErr("生效日期起迄輸入錯誤!!");
					return ;
				}
			}
			nMonth = strMid(getSysDate(), 0, 6);
			//20190513 user-1709 說不設此判斷限制
//			if (chk_strend(n_month, s_month) == false || chk_strend(n_month, e_month) == false) {
//				alert_err("起迄年月不可小於系統年月!!");
//				return;
//			}
		}

		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc);
		dataRead();
	}

	@Override
	public void initButton() {
	//if (wp.respHtml.indexOf("_detl") > 0) {
	//	this.btnMode_aud();
	//}
	  String sKey = "1st-page";
    if (wp.respHtml.equals("actm2090"))  {
       wp.colSet("btnUpdate_disable","");
       this.btnModeAud(sKey);
    }
	
	}

	@Override
	public void dddwSelect() {
		try {
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_actype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

		} catch (Exception ex) {
		}
	}
	
	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
}
