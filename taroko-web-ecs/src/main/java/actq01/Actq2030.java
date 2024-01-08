/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 109-04-21  V1.00.01  shiyuqi    updated for project coding standard        *
* 110-01-05  V1.00.04  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
* 110-03-31  V1.00.05  Justin     fix XSS                                    *
* 111-10-27  V1.00.06  Simon      sync codes with mega                       *
******************************************************************************/

package actq01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;

public class Actq2030 extends BaseEdit {
	CommDate commDate = new CommDate();
	String ppIdPSeqno = "";
	String ppPSeqno = "";
	String mDates = "";
	String mDatee = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

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
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
			// updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
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

//		dddw_select();
		initButton();
	}
	
	@Override
	public void initPage() {
		wp.colSet("ex_id_chk", "0");
	}
	
	private boolean getWhereStr() throws Exception {
		if(empty(wp.itemStr("ex_id_no")) && empty(wp.itemStr("ex_card_no"))) {
			alertErr2("身分證字號, 卡號不可均為空白");
			return false;
		}
		
		String lsDate1 = wp.itemStr("ex_s_yyymm");
		String lsDate2 = wp.itemStr("ex_e_yyymm");
		//畫面查詢年月要(減1月)
//		and acct_month>= '201001' and acct_month<= '201010' ;   //畫面=099/02   099/11
//		m_dates = ls_date1;
//		m_datee = ls_date2;
		mDates = commDate.dateAdd(lsDate1,0,-1,0).substring(0,6);
		mDatee = commDate.dateAdd(lsDate2,0,-1,0).substring(0,6);
		if (this.chkStrend(mDates, mDatee) == false) {
			alertErr2("[查詢期間-起迄]  輸入錯誤");
			return false;
		}
		
//		if(empty(wp.item_ss("ex_id_no")) && !empty(wp.item_ss("ex_card_no"))) {
		if(!empty(wp.itemStr("ex_card_no"))) {
			if (chkCard()==false) return false;
		}

		String lsIdPSeqno = getInitParm();
		if (lsIdPSeqno.equals("")) {
			alertErr2("無此身分證字號/卡號");
			return false;
		}
		
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		wp.setQueryMode();
		
		if(wp.itemEmpty("ex_id_no")==false){
			zzVipColor(wp.itemStr2("ex_id_no"));
		}	else if (wp.itemEmpty("ex_card_no")==false){
			zzVipColor(wp.itemStr2("ex_card_no"));
		}
		
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.colSet("trStyle", "display:none"); // hide the total amount column
		wp.pageControl();
		
		if (getWhereStr() == false)
			return;

		// select columns
		wp.selectSQL = " a.acct_type, sum(NVL(b.his_purchase_amt,0) + NVL(b.his_cash_amt,0)) as purchase_amt ";
		// table name
		wp.daoTable = " act_acno a, act_anal_sub b ";
		// where sql
		wp.whereStr = " WHERE a.p_seqno = b.p_seqno "+
						"      and a.id_p_seqno = :id_p_seqno " +
						"      and b.acct_month >= :s_date and b.acct_month <= :e_date " +
						" group by a.acct_type ";
		this.setString("id_p_seqno", ppIdPSeqno);
		this.setString("s_date", mDates);
		this.setString("e_date", mDatee);
		// order column
		wp.whereOrder = " ORDER BY a.acct_type ASC ";
		
//		wp.pageCount_sql ="select count(*) from ( "
//				+ " select count(*) from "
//				+ wp.daoTable
//				+ wp.whereStr
//				+ wp.whereOrder
//				+ " )"
//				;

		pageQuery();
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		wp.setListCount(1);
		wp.setPageValue();

		procSumLine();
		
		String msg = chkVIP();
		if(msg.trim().length() > 0) {
			wp.alertMesg = "<script language='javascript'> alert('"+msg+"')</script>";
			wp.dispMesg = msg;
		}
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
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	private String getInitParm() throws Exception {
		String lsSql = "";
		String lsIdchk = "";
		
		lsSql = " select a.chi_name as cname, a.id_p_seqno, b.p_seqno , a.id_no "				
				+" from crd_idno a, crd_card b " +
				" where a.id_p_seqno = b.id_p_seqno " ;
		
		if(empty(wp.itemStr("ex_id_no")) == false){
			if(empty(wp.itemStr("ex_id_chk")) == false) {
				lsIdchk = wp.itemStr("ex_id_chk");
			}else {
				lsIdchk = "0";
			}
			lsSql += "   and a.id_no = :id_no and a.id_no_code = :id_no_code " ;
			setString("id_no", wp.itemStr("ex_id_no"));
			setString("id_no_code",  lsIdchk);
		}
		if (empty(wp.itemStr("ex_card_no")) == false) {
			lsSql += "   and b.card_no = :card_no " ;
			setString("card_no", wp.itemStr("ex_card_no"));
		}
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("dsp_id_cname", sqlStr("cname"));
			wp.colSet("ex_id_no", sqlStr("id_no"));
			ppIdPSeqno = sqlStr("id_p_seqno");
			ppPSeqno = sqlStr("p_seqno");
			return sqlStr("id_p_seqno");
		}
		return "";
	}

	private boolean chkCard() throws Exception {
		String lsSql = "";
		lsSql = " SELECT sup_flag FROM crd_card WHERE card_no = :card_no ";
		setString("card_no", wp.itemStr("ex_card_no"));
		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			alertErr2("無此卡號！");
			return false;
		} else {
			if (eqIgno(sqlStr("sup_flag"),"0")==false) {
				alertErr2("此卡片不為正卡,請輸入正卡卡號！");
				return false;
			}
		}
		return true;
	}
	
	void procSumLine() throws Exception {
		double totAmt = 0;
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			totAmt += wp.colNum(ii,"purchase_amt");
		}
		
//		wp.colSet("tr","<tr><td nowrap class=\"list_no\">&nbsp;</td>"
//				+ "<td nowrap class=\"list_rr\" style=\"color:blue\">合計：</td>"
//				+ "<td nowrap class=\"list_rr\" style=\"color:blue\">"+numToStr(totAmt,"")+"</td></tr>");
		wp.colSet("trStyle", "");
		wp.colSet("totAmt", numToStr(totAmt,""));
	}

	public String chkVIP() throws Exception {
		Object[] param = null;
		String lsSql = "";

		lsSql = " select MAX(decode(vip_code,'6S','4','5S','3','WW','2','4S','1','0'))  as vip " + 
				"   from act_acno                                                             " +
				"  where id_p_seqno = ?                                                       " +
				"    and (vip_code like '%V%' or vip_code like '%W%' or vip_code like '%S%')  ";


			param = new Object[] { ppIdPSeqno };

		sqlSelect(lsSql, param);
		
		if (empty(sqlStr("vip"))) {
			return "";
		} else {
			if(sqlStr("vip").equals("0"))
				return "此卡友為4S_VIP客戶(V0-V8、WV)";
			if(sqlStr("vip").equals("1"))
				return "此卡友為4S_VIP(4S)客戶";
			if(sqlStr("vip").equals("2"))
				return "此卡友為5S_VIP(WW)客戶";
			if(sqlStr("vip").equals("3"))
				return "此卡友為5S_VIP(5S)客戶";				
			if(sqlStr("vip").equals("4"))
				return "此卡友為6S_VIP(6S)客戶";				
		}
		
		return "";
	}
}
