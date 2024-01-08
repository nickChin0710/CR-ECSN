/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-28  V1.00.01  ryan      program initial                             *
* 108-12-19  V1.00.02  ryan       update :ptr_branch==>gen_brn               *
* 109-01-16  V1.00.03   Justin Wu        PP卡 -> 貴賓卡
* 109-04-28  V1.00.04  YangFang   updated for project coding standard        *
 *  109/12/30  V1.00.05    Zuwei       “卡務中心”改為”信用卡部”   *
 * 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
* 111-09-16  V1.00.06   Ryan      調整寄件別、卡片寄送地址                                                                    *  
* 112-05-30  V1.00.08   Ryan      掛號號碼改成使用者不可輸入,索引欄位增加身份證字號,查詢欄位在最左邊增加退卡編號、掛號條碼 *  
* 112-06-05  V1.00.09   Ryan      增加卡片寄送地址註記欄位、處理邏輯
******************************************************************************/
package crdm01;

import java.text.SimpleDateFormat;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Crdm0170 extends BaseEdit {
	Crdm0170Func func;

	String kkPpCardNo = "";
	String kkWkAddr = "";
	String kk1PpCardNo = "";
	String kk2ReturnDate = "";
	String lsMsg = "";
	String mPSeqno = "";
	String mIdPSeqno = "";
	SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyyMMdd");
	String sdate = nowdate.format(new java.util.Date());
	String[] arrayMsg = null;
	String[] arrayMsgValue = null;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;

		switch (strAction) {
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
			insertFunc();
			break;
		case "U":
			/* 更新功能 */
			updateFunc();
			break;
		case "D":
			/* 刪除功能 */
			deleteFunc();
			break;
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
		case "S2":
			/* 存檔 */
			strAction = "S2";
			saveFunc();
			break;
		case "Q2":
			/* 查詢功能LOG */
			strAction = "Q2";
			queryFunc();
			break;
		case "C":
			/* 查詢 by card_no, mail_no, or barcode_num */
			itemchanged();
			break;
		case "C1":
			strAction = "C1";
			itemChanged1();
			break;
		case "AJAX":
			getChiAddr("AJAX");
			break;
		case "B1":
			/* 清畫面 */
			strAction = "new";
			clearFunc();
			break;
		case "UPLOAD":
			procFunc();
			break;
		}
		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		wp.colSet("return_date", getSysDate());
	}

	@Override
	public void queryFunc() throws Exception {
		if (getWhereStr() == false) {
			return;
		}
		;
		wp.setQueryMode();

		if (strAction.equals("Q")) {
			queryRead();
		} else {
			queryReadLog();
		}
	}

	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = "hex(a.rowid) as rowid, " + " a.pp_card_no, " + " a.return_date, " + " b.id_no, "
				+ " b.id_no_code, " + " a.reason_code, " + " a.mail_type, " + " a.proc_status, "
				// + " a.mod_date, "
				+ " a.mod_time, " + " a.mod_user, " + " a.return_note, " + " a.zip_code, " + " a.mail_addr1, "
				+ " a.mail_addr2, " + " a.mail_addr3, " + " a.mail_addr4, " + " a.mail_addr5, " + " a.return_seqno,  "
				+ " a.barcode_num ";

		wp.daoTable = "crd_return_pp as a  left join crd_idno as b on a.id_p_seqno= b.id_p_seqno ";
		wp.whereOrder = " order by a.pp_card_no";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
	}

	@Override
	public void querySelect() throws Exception {

		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		kk1PpCardNo = itemKk("data_k1");

		if (empty(kk1PpCardNo)) {
			kk1PpCardNo = wp.itemStr("kk_pp_card_no");
		}
		if (empty(kk1PpCardNo)) {
			kk1PpCardNo = wp.itemStr("pp_card_no");
		}

		kk2ReturnDate = itemKk("data_k2");

		if (empty(kk2ReturnDate)) {
			kk2ReturnDate = wp.itemStr("return_date");
		}
		if (empty(kk2ReturnDate)) {
			kk2ReturnDate = wp.itemStr("old_return_date");
		}

		wp.selectSQL = " hex(c.rowid) as rowid, " + " b.id_p_seqno, " + " b.id_no, " + " b.id_no_code, "
				+ " b.chi_name, " + " c.pp_card_no, " + " c.group_code, " + " c.mail_type, " + " c.mail_branch, "
				+ " c.beg_date, " + " c.barcode_num, " + " c.end_date, " + " c.return_date, "
				+ " c.return_date as old_return_date, " + " c.return_type, " + " c.return_seqno, " + " c.reason_code, "
				+ " c.proc_status, " + " c.package_flag, " + " c.package_date, " + " c.return_note, " + " c.mod_time, "
				+ " c.mod_user," + " c.mod_seqno, " + " c.mail_date," + " left(c.mail_no,6) as mail_no, " + " c.zip_code,"
				+ " c.mail_addr1," + " c.mail_addr2," + " c.mail_addr3," + " c.mail_addr4," + " c.mail_addr5 ,c.mail_addr_flag ";

		wp.daoTable = "crd_return_pp as c left join crd_idno as b on c.id_p_seqno= b.id_p_seqno ";
		wp.whereStr = "where 1=1";
		wp.whereStr += " and  c.pp_card_no = :pp_card_no";
		wp.whereStr += " and  c.return_date = :return_date";
		setString("pp_card_no", kk1PpCardNo);
		setString("return_date", kk2ReturnDate);
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無退件資料");
		}

		// db_group_code
		String lsSql2 = "";
		String dbGroupCode = "";
		dbGroupCode = wp.colStr("group_code");
		lsSql2 = "select group_code, group_name from ptr_group_code ";
		lsSql2 += "where 1=1 ";
		lsSql2 += sqlCol(dbGroupCode, "group_code");

		sqlSelect(lsSql2);
		if (sqlRowNum > 0) {
			wp.colSet("db_group_code", dbGroupCode + "[" + sqlStr("group_name") + "]");
		}
		// mail addess
		if (wp.colEmpty("zip_code")) {
			lsSql2 = "select zip_code," + "mail_addr1," + "mail_addr2," + "mail_addr3," + "mail_addr4," + "mail_addr5 "
					+ "from crd_card_pp " + "where 1=1 " + "and pp_card_no = :ls_cardno ";
			setString("ls_cardno", kk1PpCardNo);
			sqlSelect(lsSql2);
			wp.colSet("zip_code", sqlStr("zip_code"));
			wp.colSet("mail_addr1", sqlStr("mail_addr1"));
			wp.colSet("mail_addr2", sqlStr("mail_addr2"));
			wp.colSet("mail_addr3", sqlStr("mail_addr3"));
			wp.colSet("mail_addr4", sqlStr("mail_addr4"));
			wp.colSet("mail_addr5", sqlStr("mail_addr5"));
		}

		// db adress
		String sqlSelect = " select bill_sending_zip, " + " bill_sending_addr1, " + " bill_sending_addr2, "
				+ " bill_sending_addr3, " + " bill_sending_addr4, " + " bill_sending_addr5 " + " from act_acno "
				+ " where id_p_seqno = :s_id_pseqno fetch first 1 rows only ";
		setString("s_id_pseqno", wp.colStr("id_p_seqno"));
		sqlSelect(sqlSelect);
		wp.colSet("db_addr",
				sqlStr("bill_sending_zip") + " " + sqlStr("bill_sending_addr1") + sqlStr("bill_sending_addr2")
						+ sqlStr("bill_sending_addr3") + sqlStr("bill_sending_addr4") + sqlStr("bill_sending_addr5"));

		// 分行地址
		getChiAddr("");

	}

	@Override
	public void saveFunc() throws Exception {
		func = new Crdm0170Func(wp);
		String lsSql = "", returnSeqno = "";
		// 若PK(card_no + return_date)已存在，則不允許新增此筆資料。
		// 若不存在，則繼續使用A(新增)。
		if (strAction.equals("A")) {
			if (empty(wp.itemStr("return_date"))) {
				alertErr2("請輸入退卡日期。");
				return;
			}

			lsSql = "select count(*) as tot_cnt from crd_return_pp where pp_card_no =:pp_card_no and return_date=:return_date ";
			setString("pp_card_no", wp.itemStr("pp_card_no"));
			setString("return_date", wp.itemStr("return_date"));
			sqlSelect(lsSql);

			if (sqlNum("tot_cnt") > 0) {
				alertErr2("此貴賓卡號及退卡日期已存在。若要異動資料，請使用修改。");
				return;
			}

			// 產出退卡編號
			returnSeqno = generateReturnSeqno();
			wp.colSet("return_seqno", returnSeqno);
		}

		// 執行新增或更新
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc);

		// if(empty(wp.item_ss("rowid"))){
		// is_action = "A";
		// }else{
		// is_action = "U";
		// }
		// rc = func.dbSave(is_action);
		// ddd(func.getMsg());
		// if (rc != 1) {
		// err_alert(func.getMsg());
		// }
		// this.sql_commit(rc);
		// if(rc==1){
		// alert_msg("存檔完成");
		// }
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
			// 如果rowid是非空，則代表是從主畫面進入，因此不允許資料讀取。
			if (!empty(wp.colStr("rowid"))) {
				wp.colSet("btnQuery_disable", "disabled");

			}
		}
	}

	@Override
	public void dddwSelect() {

		try {

			wp.optionKey = wp.colStr("zip_code");
			this.dddwList("dddw_zip_code", "ptr_zipcode", "zip_code", "zip_city", "where 1=1 order by zip_code");

			wp.optionKey = wp.colStr("mail_branch");
			this.dddwList("dddw_mail_branch", "gen_brn", "branch", "brief_chi_name", "where 1=1  order by branch");

			wp.optionKey = wp.colStr("mail_type");
			this.dddwList("dddw_mail_type", "crd_message", "msg_value", "msg",
					"where 1=1 and msg_type = 'MAIL_TYPE' order by msg_value");

		} catch (Exception ex) {
		}
	}

	void queryReadLog() throws Exception {
		wp.pageControl();

		wp.selectSQL = "hex(a.rowid) as rowid, " + " a.pp_card_no, " + " a.return_date, " + " b.id_no, "
				+ " b.id_no_code, " + " a.reason_code, " + " a.mail_type, " + " a.proc_status, " + " a.mod_date, "
				+ " a.mod_time, " + " a.mod_user, " + " a.return_note, " + " a.zip_code, " + " a.mail_addr1, "
				+ " a.mail_addr2, " + " a.mail_addr3, " + " a.mail_addr4, " + " a.mail_addr5 ,a.return_seqno ,a.barcode_num ";

		wp.daoTable = "crd_return_pp_log as a  left join crd_idno as b on a.id_p_seqno= b.id_p_seqno ";
		wp.whereOrder = " order by a.pp_card_no, a.mod_date desc, a.mod_time desc";

		pageQuery();

		for (int i = 0; i < wp.selectCnt; i++) {
			kkWkAddr = "";
			String zipCode = wp.colStr(i, "zip_code");
			String mailAddr1 = wp.colStr(i, "mail_addr1");
			String mailAddr2 = wp.colStr(i, "mail_addr2");
			String mailAddr3 = wp.colStr(i, "mail_addr3");
			String mailAddr4 = wp.colStr(i, "mail_addr4");
			String mailAddr5 = wp.colStr(i, "mail_addr5");
			if (!empty(zipCode)) {
				kkWkAddr += zipCode.replaceAll(" ", "") + " ";
			}
			if (!empty(mailAddr1)) {
				kkWkAddr += mailAddr1.replaceAll(" ", "");
			}
			if (!empty(mailAddr2)) {
				kkWkAddr += mailAddr2.replaceAll(" ", "");
			}
			if (!empty(mailAddr3)) {
				kkWkAddr += mailAddr3.replaceAll(" ", "");
			}
			if (!empty(mailAddr4)) {
				kkWkAddr += mailAddr4.replaceAll(" ", "");
			}
			if (!empty(mailAddr5)) {
				kkWkAddr += mailAddr5.replaceAll(" ", "");
			}
			wp.colSet(i, "wk_addr", kkWkAddr);
		}
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		listWkdata();
		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
	}

	void listWkdata() {
		getMsg();
		for (int ii = 0; ii < wp.selectCnt; ii++) {

			kkWkAddr = "";
			String zipCode = wp.colStr(ii, "zip_code");
			String mailAddr1 = wp.colStr(ii, "mail_addr1");
			String mailAddr2 = wp.colStr(ii, "mail_addr2");
			String mailAddr3 = wp.colStr(ii, "mail_addr3");
			String mailAddr4 = wp.colStr(ii, "mail_addr4");
			String mailAddr5 = wp.colStr(ii, "mail_addr5");
			if (!empty(zipCode)) {
				kkWkAddr += zipCode.replaceAll(" ", "") + " ";
			}
			if (!empty(mailAddr1)) {
				kkWkAddr += mailAddr1.replaceAll(" ", "");
			}
			if (!empty(mailAddr2)) {
				kkWkAddr += mailAddr2.replaceAll(" ", "");
			}
			if (!empty(mailAddr3)) {
				kkWkAddr += mailAddr3.replaceAll(" ", "");
			}
			if (!empty(mailAddr4)) {
				kkWkAddr += mailAddr4.replaceAll(" ", "");
			}
			if (!empty(mailAddr5)) {
				kkWkAddr += mailAddr5.replaceAll(" ", "");
			}
			wp.colSet(ii, "wk_addr", kkWkAddr);

			// db_reason_code郵局退回原因
			String reasonCode = wp.colStr(ii, "reason_code");
			;
			String[] cde = new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10" };
			String[] txt = new String[] { "查無此公司", "查無此人", "遷移不明", "地址欠詳", "查無此址", "收件人拒收", "招領逾期", "分行退件", "其他",
					"地址改變" };
			wp.colSet(ii, "tt_reason_code", commString.decode(reasonCode, cde, txt));

			// db_mail_type寄件別
			String mailType = wp.colStr(ii, "mail_type");
			;
			String[] cde1 = arrayMsgValue;
			String[] txt1 = arrayMsg;
			wp.colSet(ii, "tt_mail_type", commString.decode(mailType, cde1, txt1));

			// db_proc_status處理結果
			String procStatus = wp.colStr(ii, "proc_status");
			;
			String[] cde2 = new String[] { "1", "2", "3", "4", "5", "6", "7" };
			String[] txt2 = new String[] { "處理中", "銷毀", "寄出", "申停", "重製", "寄出不封裝", "庫存" };
			wp.colSet(ii, "tt_proc_status", commString.decode(procStatus, cde2, txt2));

		}
	}

	void itemchanged() throws Exception {
		String paramString;
		String col = "";
		switch (wp.itemStr("cond")) {
		case "1":
			col = "pp_card_no";
			break;
		case "2":
			col = "mail_no";
			break;
		case "3":
			col = "barcode_num";
			break;
		}
		paramString = wp.itemStr("kk_cond_val");
		if (wfColNameData(col, paramString) != 1) {
			alertErr2(lsMsg);
			return;
		}

	}

	void itemChanged1() throws Exception {
		String lsSql = "", lsZipCode = "";
		lsZipCode = wp.itemStr("zip_code");
		lsSql = "select zip_city,zip_town from ptr_zipcode " + "where zip_code =:ls_zip_code ";
		setString("ls_zip_code", lsZipCode);
		sqlSelect(lsSql);
		wp.colSet("mail_addr1", sqlStr("zip_city"));
		wp.colSet("mail_addr2", sqlStr("zip_town"));
	}

	int wfColNameData(String colName, String asCardno) throws Exception {
		String lsSql = "", lsGroupCode = "";

		// select by colName
		lsSql = " select " + " b.id_p_seqno id_p_seqno, " + " b.id_no id_no, " + " b.id_no_code id_no_code, "
				+ " b.chi_name chi_name, " + " a.pp_card_no pp_card_no, " + " a.group_code group_code, "
				+ " a.mail_type mail_type, " + " a.mail_branch mail_branch, " + " a.valid_fm as beg_date, "
				+ " a.valid_to as end_date, " + " LEFT(a.mail_no,6) as mail_no, " + " a.barcode_num barcode_num "
				+ "from crd_card_pp as a left join crd_idno as b on a.id_p_seqno= b.id_p_seqno " + "where " + "a."
				+ colName + "=:" + colName;

		setString(colName, asCardno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			String[] cde = new String[] { "pp_card_no", "mail_no", "barcode_num" };
			String[] txt = new String[] { "貴賓卡號", "掛號號碼", "掛號條碼" };
			String colChineseName = commString.decode(colName, cde, txt);
			lsMsg = colChineseName + "不存在,請重新輸入!!";
			return -1;
		} else {
			mIdPSeqno = sqlStr("id_p_seqno");
			lsGroupCode = sqlStr("group_code");
			wp.colSet("id_p_seqno", mIdPSeqno);
			wp.colSet("id_no", sqlStr("id_no"));
			wp.colSet("id_no_code", sqlStr("id_no_code"));
			wp.colSet("chi_name", sqlStr("chi_name"));
			wp.colSet("pp_card_no", sqlStr("pp_card_no"));
			wp.colSet("group_code", lsGroupCode);
			wp.colSet("mail_type", sqlStr("mail_type"));
			wp.colSet("mail_branch", sqlStr("mail_branch"));
			wp.colSet("beg_date", sqlStr("beg_date"));
			wp.colSet("end_date", sqlStr("end_date"));
			wp.colSet("mail_no", sqlStr("mail_no"));
			wp.colSet("barcode_num", sqlStr("barcode_num"));
			wp.colSet("kk_cond_val", asCardno);
		}

		// db_group_code
		String lsSql2 = "";
		lsSql2 = "select group_code, group_name from ptr_group_code ";
		lsSql2 += "where 1=1 and group_code = :group_code ";
		setString("group_code",lsGroupCode);
		sqlSelect(lsSql2);
		if (sqlRowNum > 0) {
			wp.colSet("db_group_code", lsGroupCode + "[" + sqlStr("group_name") + "]");
		}

		// select act_acno
		lsSql = "select bill_sending_zip||" + "bill_sending_addr1||" + "bill_sending_addr2||" + "bill_sending_addr3||"
				+ "bill_sending_addr4||" + "bill_sending_addr5 " + "as db_addr "
				// + "bill_sending_zip,"
				// + "bill_sending_addr1,"
				// + "bill_sending_addr2,"
				// + "bill_sending_addr3,"
				// + "bill_sending_addr4,"
				// + "bill_sending_addr5 "
				+ "from act_acno " + "where id_p_seqno =:ls_id_p_seqno";
		setString("ls_id_p_seqno", mIdPSeqno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			lsMsg = "查無帳號資料!!";
			return -1;
		} else {
			wp.colSet("db_addr", sqlStr("db_addr"));
			// wp.col_set("zip_code", sql_ss("bill_sending_zip"));
			// wp.col_set("mail_addr1", sql_ss("bill_sending_addr1"));
			// wp.col_set("mail_addr2", sql_ss("bill_sending_addr2"));
			// wp.col_set("mail_addr3", sql_ss("bill_sending_addr3"));
			// wp.col_set("mail_addr4", sql_ss("bill_sending_addr4"));
			// wp.col_set("mail_addr5", sql_ss("bill_sending_addr5"));
		}

		return 1;
	}

	// 找出資料庫中今年最大的退卡編號，並回傳此退卡編號+1。
	// 若無找到，則自動產生今年第一筆流水號。
	// 如使用新增功能時是2019年，第一筆退卡編號為201900001。
	private String generateReturnSeqno() {
		String lsSql = "";
		String thisYear, maxNo, returnSeqno;
		thisYear = getSysDate().substring(0, 4);

		// 找尋今年最大的return_seqno
		lsSql = "select max(return_seqno) max_no from crd_return_pp " + "where return_seqno like '" + thisYear + "%'";
		sqlSelect(lsSql);
		maxNo = sqlStr("max_no");
		if (!empty(maxNo)) {
			returnSeqno = Integer.toString(Integer.parseInt(maxNo) + 1);
		} else {
			returnSeqno = thisYear + "00001";
		}
		return returnSeqno;
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
		wp.whereStr = " where 1=1 ";
		String exPpCardNo = wp.itemStr("ex_pp_card_no");
		String exReturnSeqno = wp.itemStr("ex_return_seqno");
		String exBarcodeNum = wp.itemStr("ex_barcode_num");
		String exReturnDate1 = wp.itemStr("ex_return_date1");
		String exReturnDate2 = wp.itemStr("ex_return_date2");
		String exIdNo = wp.itemStr("ex_id_no");
		int theNumOfEmpty = 6;
		if (!empty(exPpCardNo)) {
			wp.whereStr += " and a.pp_card_no = :ls_pp_card_no ";
			setString("ls_pp_card_no", exPpCardNo);
			theNumOfEmpty--;
		}
		if (!empty(exBarcodeNum)) {
			wp.whereStr += " and a.barcode_num = :ls_barcode_num ";
			setString("ls_barcode_num", exBarcodeNum);
			theNumOfEmpty--;
		}
		if (!empty(exReturnSeqno)) {
			wp.whereStr += " and a.return_seqno = :ls_return_seqno ";
			setString("ls_return_seqno", exReturnSeqno);
			theNumOfEmpty--;
		}
		if (!empty(exReturnDate1)) {
			wp.whereStr += " and a.return_date >= :ls_return_date1 ";
			setString("ls_return_date1", exReturnDate1);
			theNumOfEmpty--;
		}
		if (!empty(exReturnDate2)) {
			wp.whereStr += " and a.return_date <= :ls_return_date2 ";
			setString("ls_return_date2", exReturnDate2);
			theNumOfEmpty--;
		}
		if (!empty(exIdNo)) {
			wp.whereStr += " and b.id_no = :ls_id_no ";
			setString("ls_id_no", exIdNo);
			theNumOfEmpty--;
		}
		if (theNumOfEmpty == 6) {
			alertErr("退卡日期、貴賓卡號、退卡編號、身份證字號以及掛號條碼至少填寫一項！");
			return false;
		} else {
			return true;
		}
	}

	void getMsg() {
		String sqlMsg = "select msg_value,msg_value||'.'||msg as db_msg from crd_message where msg_type = 'MAIL_TYPE' ";
		sqlSelect(sqlMsg);
		arrayMsgValue = new String[sqlRowNum];
		arrayMsg = new String[sqlRowNum];
		for (int x = 0; x < sqlRowNum; x++) {
			arrayMsgValue[x] = sqlStr(x, "msg_value");
			arrayMsg[x] = sqlStr(x, "db_msg");
		}
	}

	void getChiAddr(String parameter) throws Exception {
		if(parameter.equals("AJAX")&&!wp.itemEmpty("id_no_json")) {
			if(!wp.itemEmpty("mail_addr_flag_json")&&wp.itemEmpty("mail_branch_json")) {
				String idNoJson = wp.itemStr("id_no_json");
				String mailAddrFlagJson = wp.itemStr("mail_addr_flag_json");
				String sqlCmd = "select ";
				switch(mailAddrFlagJson) {
				case"1":
					sqlCmd += " RESIDENT_ZIP as ZIP ,RESIDENT_ADDR1 as ADDR1,RESIDENT_ADDR2 as ADDR2,RESIDENT_ADDR3 as ADDR3,RESIDENT_ADDR4 as ADDR4,RESIDENT_ADDR5 as ADDR5 ";
					break;
				case"2":
					sqlCmd += " MAIL_ZIP as ZIP ,MAIL_ADDR1 as ADDR1,MAIL_ADDR2 as ADDR2,MAIL_ADDR3 as ADDR3,MAIL_ADDR4 as ADDR4,MAIL_ADDR5 as ADDR5 ";
					break;
				case"3":
					sqlCmd += " COMPANY_ZIP as ZIP ,COMPANY_ADDR1 as ADDR1,COMPANY_ADDR2 as ADDR2,COMPANY_ADDR3 as ADDR3,COMPANY_ADDR4 as ADDR4,COMPANY_ADDR5 as ADDR5 ";
					break;
				}
				sqlCmd += " from crd_idno where id_no = :id_no_json ";
				setString("id_no_json",idNoJson);
				sqlSelect(sqlCmd);
				wp.addJSON("zip_code_json", sqlStr("ZIP"));
				wp.addJSON("mail_addr1_json", sqlStr("ADDR1"));
				wp.addJSON("mail_addr2_json", sqlStr("ADDR2"));
				wp.addJSON("mail_addr3_json", sqlStr("ADDR3"));
				wp.addJSON("mail_addr4_json", sqlStr("ADDR4"));
				wp.addJSON("mail_addr5_json", sqlStr("ADDR5"));
				wp.addJSON("flag_json", "Y");
			}
			return;
		}
		
		
		String lsSql = "select g.chi_addr_1,g.chi_addr_2,g.chi_addr_3,g.chi_addr_4,g.chi_addr_5, ";
		lsSql += " p.zip_code ";
		lsSql += " from gen_brn g left join ptr_zipcode p ";
		lsSql += " on g.chi_addr_1 = p.zip_city ";
		lsSql += " and g.chi_addr_2 = p.zip_town ";
		lsSql += " where 1=1 ";
		lsSql += sqlCol(wp.colStr("mail_branch"), "g.branch");
		lsSql += " fetch first 1 rows only ";
		sqlSelect(lsSql);
		if (parameter.equals("AJAX")) {
			wp.addJSON("ajax_zip_code", sqlStr("zip_code"));
			wp.addJSON("ajax_chi_addr_1", sqlStr("chi_addr_1"));
			wp.addJSON("ajax_chi_addr_2", sqlStr("chi_addr_2"));
			wp.addJSON("ajax_chi_addr_3", sqlStr("chi_addr_3"));
			wp.addJSON("ajax_chi_addr_4", sqlStr("chi_addr_4"));
			wp.addJSON("ajax_chi_addr_5", sqlStr("chi_addr_5"));
			wp.addJSON("flag_ajax", "Y");
		} else {
			wp.colSet("db_zip_code", sqlStr("zip_code"));
			wp.colSet("db_mail_addr1", sqlStr("chi_addr_1"));
			wp.colSet("db_mail_addr2", sqlStr("chi_addr_2"));
			wp.colSet("db_mail_addr3", sqlStr("chi_addr_3"));
			wp.colSet("db_mail_addr4", sqlStr("chi_addr_4"));
			wp.colSet("db_mail_addr5", sqlStr("chi_addr_5"));
		}
	}
	
	//整批匯入
	public void procFunc() throws Exception {
		if (itemIsempty("zz_file_name")) {
			alertErr2("上傳檔名: 不可空白");
			return;
		}
		fileDataImp();
	}
	
	void fileDataImp() throws Exception {
		TarokoFileAccess tf = new TarokoFileAccess(wp);

		String inputFile = wp.itemStr("zz_file_name");

		int fi = tf.openInputText(inputFile, "MS950");
		if (fi == -1) return;

		int llOk = 0, llCnt = 0, errCnt = 0;
		int returnSeqno = toInt(generateReturnSeqno());
		while (true) {
			String line = tf.readTextFile(fi);
			if (tf.endFile[fi].equals("Y")) break;

			llCnt ++;
			
			// split columns
			boolean isValidFormat = false;
			if (line.indexOf(",") != -1) {
				String batchBarcodeNum = "";
				String[] strArr = line.split(",");
				if (strArr.length == 1) {
					batchBarcodeNum = strArr[0].trim();
					isValidFormat = true;
					
					if (empty(batchBarcodeNum)) {
						setProcessResult(llCnt, batchBarcodeNum, "輸入掛號條碼不可為空");
						errCnt ++;
						continue;
					}
					if (batchBarcodeNum.length()>20) {
						setProcessResult(llCnt, batchBarcodeNum, "輸入掛號條碼長度不可大於20碼");
						errCnt ++;
						continue;
					}
					
		
					if(wfColNameData("barcode_num", batchBarcodeNum) == -1) {
						setProcessResult(llCnt, batchBarcodeNum, lsMsg);
						errCnt ++;
						continue;
					}
					
					if(inserrtCrdReturnPp(batchBarcodeNum,returnSeqno) == -1) {
						setProcessResult(llCnt, batchBarcodeNum, lsMsg);
						errCnt ++;
						continue;
					}
					
					setProcessResult(llCnt, batchBarcodeNum, "");
					llOk++;
					returnSeqno++;
				}	
			}

			if (isValidFormat == false) {
				setProcessResult(llCnt, "", String.format("資料格式不符[%s]", line));
				errCnt ++;
				continue;	
			}

		}
		
		wp.selectCnt = llCnt;
		wp.setListCount(1);
		
		String finalResult = String.format("資料匯入處理筆數[%d], 成功筆數[%d], 錯誤筆數[%d] ", llCnt, llOk, errCnt);
		wp.showLogMessage("I", "", finalResult);
		wp.alertMesg(finalResult);
		tf.closeInputText(fi);
		wp.colSet("zz_file_name", "");

	}
	
	private void setProcessResult(int llCnt, String batchBarcodeNum, String batchErrorMsg) {
		wp.colSet(llCnt-1, "batch_ser_num", llCnt);
		wp.colSet(llCnt-1, "batch_barcode_num", batchBarcodeNum);
		wp.colSet(llCnt-1, "batch_error_msg", batchErrorMsg);
	}
	
	private int inserrtCrdReturnPp(String batchBarcodeNum ,int returnSeqno) {
		String sqlSelect = "select count(*) as crd_return_pp_cnt from crd_return_pp where pp_card_no = :pp_card_no and return_date = :return_date ";
		setString("pp_card_no",wp.colStr("pp_card_no"));
		setString("return_date",wp.sysDate);
		sqlSelect(sqlSelect);
		int crdReturnPpCnt = sqlInt("crd_return_pp_cnt");
		if(crdReturnPpCnt > 0) {
			lsMsg = String.format("crd_return_pp 資料已存在 ,pp_card_no = [%s] ,return_date = [%s]", wp.colStr("pp_card_no"),wp.sysDate);
			return -1;
		}
		
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("crd_return_pp");
		sp.ppstr("pp_card_no", wp.colStr("pp_card_no"));
		sp.ppstr("id_p_seqno", mIdPSeqno);
		sp.ppstr("beg_date", wp.colStr("beg_date"));
		sp.ppstr("barcode_num", batchBarcodeNum);
		sp.ppstr("end_date", wp.colStr("end_date"));
		sp.ppstr("return_date", wp.sysDate);
		sp.ppstr("return_type", "");
		sp.ppstr("reason_code", "");
		sp.ppstr("group_code", wp.colStr("group_code"));
		sp.ppstr("mail_type", "");
		sp.ppstr("mail_branch", wp.colStr("mail_branch"));
		sp.ppstr("mail_no", wp.colStr("mail_no"));
	    sp.ppstr("zip_code", "");
	    sp.ppstr("mail_addr1", "");
	    sp.ppstr("mail_addr2", "");
	    sp.ppstr("mail_addr3", "");
	    sp.ppstr("mail_addr4", "");
	    sp.ppstr("mail_addr5", "");
		sp.ppstr("proc_status", "1");
		sp.ppstr("return_note", "");
		sp.ppint("return_seqno", returnSeqno);
		sp.ppstr("package_flag", wp.colStr("package_flag"));
		sp.ppstr("package_date", "");
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppnum("mod_seqno", 1);

		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			lsMsg = "inserrt crd_return_pp error";
			return -1;
		}
		return 1;
	}
}
