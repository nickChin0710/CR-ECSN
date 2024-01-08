/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-03  V1.00.00  Andy       program initial                            *
* 107-08-09  V1.00.01  Andy       Update                                     *
* 108-06-25  V1.00.02  Andy       Update add mkt_purcgp_ext_t 特店群組設定                *
* 109-01-03  V1.00.03  Justin Wu    updated for archit.  change
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110-09-06  V1.00.04  jiangyingdong       change 排除特店代碼設定 and POS終端機設定 
* 111-02-28  V1.00.04  machao       页面调整       
* 111-03-22  V1.00.05  machao       己覆核資料需可異動.程式需增列相關處理邏輯.*
* 112-01-12  V1.00.06  Zuwei Su     團體代號不為空時查詢失敗，有修改資料,按下修改按鈕,檢核APR_FLAG=N,直接存入異動資料,顯示”資料異動成功”訊息 *
******************************************************************************/

package mktm01;

import java.util.Arrays;

import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;
import ofcapp.BaseAction;

public class Mktm0210 extends BaseAction {
	String mExGroupCode = "";
	String mProgramCode = "";
	String mMainTable = "";
	String mGroupCode = "";
	int lsCt1 = 0, lsCt2 = 0, lsCt3 = 0;

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
		case "Q":
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
			break;
		case "R":
			// -資料讀取-
			strAction = "R";
			querySelect();
			break;
		case "R1":
			/* 查詢功能 */
			strAction = "R1";
			querySelect();
			break;
		case "R2":
			/* 查詢功能 */
			strAction = "R2";
			querySelect();
			break;
		case "R3":
			/* 查詢功能 */
			strAction = "R3";
			querySelect();
			break;
		case "R_POS":
			/* 查詢 POS終端機代號設定 */
			strAction = "R_POS";
			querySelect();
			break;
		case "A":
			/* 新增功能 */
			strAction = "A";
			saveFunc();
			break;
		case "U":
			/* 更新功能 */
			strAction = "U";
			saveFunc();
			break;
		case "D":
			/* 刪除功能 */
			strAction = "D";
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
		case "L":
			/* 清畫面 */
			strAction = "";
			clearFunc();
			break;
		case "S2":
			/* 更新功能 */
			strAction = "S2";
			saveFunc();
			break;
		case "S3":
			/* 清畫面 */
			strAction = "S3";
			insertFunc1();
			break;
		case "S4":
			/* 清畫面 */
			strAction = "S4";
			insertFunc2();
			break;
		case "UPLOAD":
			procFunc();
			break;
		case "AJAX":
			strAction = "AJAX";
			processAjaxOption();
			break;
		case "mcht_group":
			/* 清畫面 */
			strAction = "mcht_group";
			insertFunc3();
			break;
		case "mcht_group_out":
			/* 清畫面 */
			strAction = "mcht_group_out";
			insertFunc1();
			break;
		case "mcht_out":
			/* 清畫面 */
			strAction = "mcht_out";
			insertFunc2();
			break;
		case "mcht_pos":
			/* POS終端機代碼設定 */
			strAction = "mcht_pos";
			insertPOS();
			break;
		default:
			break;
		}
		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		// 設定初始搜尋條件值
		if (wp.respHtml.indexOf("_detl") > 0) {
			if (strAction.equals("new")) {
				wp.colSet("reward_type", "1");
				wp.colSet("present_type", "1");
				wp.colSet("item_ename_bl", "1");
				wp.colSet("item_ename_ca", "1");
				wp.colSet("item_ename_id", "1");
				wp.colSet("item_ename_ao", "1");
				wp.colSet("item_ename_it", "1");
				wp.colSet("item_ename_ot", "1");
				wp.colSet("purch_date_type", "0");
				wp.colSet("base_amt_1", "0");
				wp.colSet("purch_amt_s_a1", "0");
				wp.colSet("purch_amt_e_a1", "0");
				wp.colSet("rate_a1", "0.0000");
				wp.colSet("rate_a12", "0.0000");
				wp.colSet("purch_amt_s_a2", "0");
				wp.colSet("purch_amt_e_a2", "0");
				wp.colSet("rate_a2", "0.0000");
				wp.colSet("rate_a22", "0.0000");
				wp.colSet("purch_amt_s_a3", "0");
				wp.colSet("purch_amt_e_a3", "0");
				wp.colSet("rate_a3", "0.0000");
				wp.colSet("rate_a32", "0.0000");
				wp.colSet("purch_amt_s_a4", "0");
				wp.colSet("purch_amt_e_a4", "0");
				wp.colSet("rate_a4", "0.0000");
				wp.colSet("rate_a42", "0.0000");
				wp.colSet("purch_amt_s_a5", "0");
				wp.colSet("purch_amt_e_a5", "0");
				wp.colSet("rate_a5", "0.0000");
				wp.colSet("rate_a52", "0.0000");
				wp.colSet("int_amt_s_1", "0");
				wp.colSet("int_amt_e_1", "0");
				wp.colSet("int_rate_1", "0.0000");
				wp.colSet("int_rate_12", "0.0000");
				wp.colSet("int_amt_s_2", "0");
				wp.colSet("int_amt_e_2", "0");
				wp.colSet("int_rate_2", "0.0000");
				wp.colSet("int_rate_22", "0.0000");
				wp.colSet("int_amt_s_3", "0");
				wp.colSet("int_amt_e_3", "0");
				wp.colSet("int_rate_3", "0.0000");
				wp.colSet("int_rate_32", "0.0000");
				wp.colSet("int_amt_s_4", "0");
				wp.colSet("int_amt_e_4", "0");
				wp.colSet("int_rate_4", "0.0000");
				wp.colSet("int_rate_42", "0.0000");
				wp.colSet("int_amt_s_5", "0");
				wp.colSet("int_amt_e_5", "0");
				wp.colSet("int_rate_5", "0.0000");
				wp.colSet("int_rate_52", "0.0000");
				wp.colSet("out_amt_s_1", "0");
				wp.colSet("out_amt_e_1", "0");
				wp.colSet("out_rate_1", "0.0000");
				wp.colSet("out_rate_12", "0.0000");
				wp.colSet("out_amt_s_2", "0");
				wp.colSet("out_amt_e_2", "0");
				wp.colSet("out_rate_2", "0.0000");
				wp.colSet("out_rate_22", "0.0000");
				wp.colSet("out_amt_s_3", "0");
				wp.colSet("out_amt_e_3", "0");
				wp.colSet("out_rate_3", "0.0000");
				wp.colSet("out_rate_32", "0.0000");
				wp.colSet("out_amt_s_4", "0");
				wp.colSet("out_amt_e_4", "0");
				wp.colSet("out_rate_4", "0.0000");
				wp.colSet("out_rate_42", "0.0000");
				wp.colSet("out_amt_s_5", "0");
				wp.colSet("out_amt_e_5", "0");
				wp.colSet("out_rate_5", "0.0000");
				wp.colSet("out_rate_52", "0.0000");
			}

		}
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
		String exGrouCode = wp.itemStr("ex_group_code");
		wp.whereStr = " where 1=1 ";
		wp.whereStr += sqlCol(exGrouCode, "group_code");

		return true;
	}

	@Override
	public void queryFunc() throws Exception {
//		getWhereStr();
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		if (wp.itemStr("ex_apr_flag").equals("Y")) {
			mMainTable = "mkt_purc_gp";
		} else {
			mMainTable = "mkt_purc_gp_t";
		}
		wp.selectSQL = "" + "group_code, " + "description, " + "reward_type, " + "crt_date, " + "crt_user, "
				+ "apr_flag, " + "program_code ";
		wp.daoTable = mMainTable;
		wp.whereOrder = " order by group_code";
		getWhereStr();

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		commGroupCode("comm_group_code");
		wp.setPageValue();
		listWkdata();
	}

	void listWkdata() throws Exception {
		int selCt = wp.selectCnt;
		for (int ii = 0; ii < selCt; ii++) {
			String tmpStr = wp.colStr(ii, "reward_type");
			String[] cde = new String[] { "0", "1", "2" };
			String[] txt = new String[] { "不產生回饋", "按消費金額", "依店內店外消費" };
			wp.colSet(ii, "db_reward_type", commString.decode(tmpStr, cde, txt));
		}
	}

	@Override
	public void querySelect() throws Exception {
		if (strAction.equals("R")) {
			dataRead();
		}
		if (strAction.equals("R1")) { // 查詢 已排除特店群組設定
			dataRead1();
		}
		if (strAction.equals("R2")) { // 查詢 特店群組/代號設定
			dataRead2();
		}
		if (strAction.equals("R3")) { // 查詢 已排除特店代碼設定
			dataReadOut();
		}
		if (strAction.equals("R_POS")) { // 查詢 POS終端機代號設定
			dataReadPOS();
		}
	}

	@Override
	public void dataRead() throws Exception {
		mExGroupCode = wp.itemStr("kk_group_code");
		mProgramCode = itemkk("data_k2");
		if (empty(mExGroupCode)) {
			mExGroupCode = itemkk("data_k1");
		}
		if (wp.itemStr("ex_apr_flag").equals("Y") || wp.itemStr("apr_flag").equals("Y")) {
			mMainTable = "mkt_purc_gp";
		} else {
			mMainTable = "mkt_purc_gp_t";
		}
		wp.selectSQL = "hex(rowid) as rowid, " + "group_code, " + "description, " + "reward_type, " + "base_amt_1, "
				+ "purch_amt_s_a1, " + "purch_amt_e_a1, " + "rate_a1, " + "purch_amt_s_a2, " + "purch_amt_e_a2, "
				+ "rate_a2, " + "purch_amt_s_a3, " + "purch_amt_e_a3, " + "rate_a3, " + "purch_amt_s_a4, "
				+ "purch_amt_e_a4, " + "rate_a4, " + "purch_amt_s_a5, " + "purch_amt_e_a5, " + "rate_a5, "
				+ "mcht_no_1, " + "mcht_no_2, " + "mcht_no_3, " + "mcht_no_4, " + "mcht_no_5, " + "mcht_no_6, "
				+ "mcht_no_7, " + "mcht_no_8, " + "mcht_no_9, " + "mcht_no_10, " + "int_amt_s_1, " + "int_amt_e_1, "
				+ "int_rate_1, " + "int_amt_s_2, " + "int_amt_e_2, " + "int_rate_2, " + "int_amt_s_3, "
				+ "int_amt_e_3, " + "int_rate_3, " + "int_amt_s_4, " + "int_amt_e_4, " + "int_rate_4, "
				+ "int_amt_s_5, " + "int_amt_e_5, " + "int_rate_5, " + "out_amt_s_1, " + "out_amt_e_1, "
				+ "out_rate_1, " + "out_amt_s_2, " + "out_amt_e_2, " + "out_rate_2, " + "out_amt_s_3, "
				+ "out_amt_e_3, " + "out_rate_3, " + "out_amt_s_4, " + "out_amt_e_4, " + "out_rate_4, "
				+ "out_amt_s_5, " + "out_amt_e_5, " + "out_rate_5, " + "item_ename_bl, " + "item_ename_bl_in, "
				+ "item_ename_bl_out, " + "item_ename_it, " + "item_ename_it_in, " + "item_ename_it_out, "
				+ "item_ename_ca, " + "item_ename_id, " + "item_ename_ao, " + "item_ename_ot, " + "present_type, "
				+ "rate_a12, " + "rate_a22, " + "rate_a32, " + "rate_a42, " + "rate_a52, " + "int_rate_12, "
				+ "int_rate_22, " + "int_rate_32, " + "int_rate_42, " + "int_rate_52, " + "out_rate_12, "
				+ "out_rate_22, " + "out_rate_32, " + "out_rate_42, " + "out_rate_52, " + "program_code, "
				+ "purch_date_type, " + "run_time_dd, " + "crt_user, " + "crt_date, " + "apr_flag, " + "apr_user, "
				+ "apr_date ";
		if (mMainTable.equals("mkt_purc_gp_t")) {
			wp.selectSQL += ", 'U.更新待覆核' as app_flag";
		} else if (mMainTable.equals("mkt_purc_gp")) {
			wp.selectSQL += ", 'Y.未異動' as app_flag";

		}
		wp.daoTable = mMainTable;
		wp.whereStr = " where 1=1 ";
		wp.whereStr += " and  group_code = :group_code ";
		setString("group_code", mExGroupCode);

		// System.out.println("group_code : "+m_ex_group_code);
		// System.out.println("select " + wp.selectSQL + " from "
		// +wp.daoTable+wp.whereStr+wp.whereOrder);

		pageSelect();
		if (mMainTable.equals("mkt_purc_gp")) {
			wp.colSet("d_disable", "disabled style='background-color: lightgray;'");
		}

		if (sqlNotFind()) {
			alertErr("查無資料, group_code =" + mExGroupCode);
		}
		// list_wkdata();
		commGroupCode("comm_group_code");
	}

	// 查詢 排除特店群組設定 from ptr_bn_data_t
	public void dataRead1() throws Exception {
		this.selectNoLimit();
		if (wp.itemStr("ex_apr_flag").equals("Y") || wp.itemStr("apr_flag").equals("Y")) {
			mMainTable = "ptr_bn_data";
		} else {
			mMainTable = "ptr_bn_data_t";
		}
		mProgramCode = wp.itemStr("program_code");
		if (empty(mProgramCode)) {
			mProgramCode = itemkk("data_m1");
		}
		// if (empty(m_program_code)) {
		// m_program_code = wp.item_ss("kk_program_code");
		// }
		// wp.col_set("kk_program_code", m_program_code);

		if (empty(mProgramCode)) {
			alertErr("請先點選團體代號!!");
			return;
		}
		wp.selectSQL = "hex(rowid) as rowid1, " + "program_code , " + "data_type , " + "data_code ";
		wp.daoTable = mMainTable;
		wp.whereStr = "where 1=1 and data_type='1' ";
		wp.whereStr += "and program_code =:program_code ";
		setString("program_code", mProgramCode);

		pageQuery();
		wp.setListCount(1);
		wp.notFound = "";
		if (sqlNotFind()) {
			alertErr("查無排除特店資料");
		}
		wp.colSet("row_ct", wp.selectCnt);
	}

	// 查詢 排除特店代號設定 from ptr_bn_data_t
	public void dataReadOut() throws Exception {
		this.selectNoLimit();
		if (wp.itemStr("ex_apr_flag").equals("Y") || wp.itemStr("apr_flag").equals("Y")) {
			mMainTable = "ptr_bn_data";
		} else {
			mMainTable = "ptr_bn_data_t";
		}
		mProgramCode = wp.itemStr("program_code");
		if (empty(mProgramCode)) {
			mProgramCode = itemkk("data_m1");
		}
		// if (empty(m_program_code)) {
		// m_program_code = wp.item_ss("kk_program_code");
		// }
		// wp.col_set("kk_program_code", m_program_code);

		if (empty(mProgramCode)) {
			alertErr("請先點選團體代號!!");
			return;
		}

		wp.selectSQL = "hex(rowid) as rowid1, " + "program_code , " + "data_type , " + "data_code ";
		wp.daoTable = mMainTable;
		wp.whereStr = "where 1=1 and data_type='2' ";
		wp.whereStr += "and program_code =:program_code ";
		setString("program_code", mProgramCode);

		pageQuery();
		wp.setListCount(1);
		wp.notFound = "";
		if (sqlNotFind()) {
			alertErr("查無排除特店資料");
		}
		wp.colSet("row_ct", wp.selectCnt);
		wp.colSet("program_code", mProgramCode);
	}

	// 查詢 POS終端機代號設定 from ptr_bn_data_t
	public void dataReadPOS() throws Exception {		
		this.selectNoLimit();
		if (wp.itemStr("ex_apr_flag").equals("Y") || wp.itemStr("apr_flag").equals("Y")) {
			mMainTable = "ptr_bn_data";
		} else {
			mMainTable = "ptr_bn_data_t";
		}
		mProgramCode = wp.itemStr("group_code");
		if (empty(mProgramCode)) {
			mProgramCode = itemkk("data_m1");
		}

		if (empty(mProgramCode)) {
			alertErr("請先點選團體代號!!");
			return;
		}
		wp.selectSQL = "hex(rowid) as rowid1, " + "program_code , " + "data_type , " + "data_code ";
		wp.daoTable = mMainTable;
		wp.whereStr = "where 1=1 and data_type='5' ";
		wp.whereStr += "and program_code =:program_code ";
		setString("program_code", mProgramCode);

		pageQuery();
		wp.setListCount(1);
		wp.notFound = "";
		if (sqlNotFind()) {
			alertErr("查無排除特店資料");
		}
		wp.colSet("row_ct", wp.selectCnt);
		wp.colSet("program_code", mProgramCode);
	}

	// 查詢 特店群組/代號設定 from mkt_purcgp_ext_t
	public void dataRead2() throws Exception {
		this.selectNoLimit();
		if (wp.itemStr("ex_apr_flag").equals("Y") || wp.itemStr("apr_flag").equals("Y")) {
			mMainTable = "mkt_purcgp_ext";
		} else {
			mMainTable = "mkt_purcgp_ext_t";
		}
		mGroupCode = wp.itemStr("group_code");
		if (empty(mGroupCode)) {
			mGroupCode = itemkk("data_n1");
		}
		wp.colSet("group_code", mGroupCode);
		if (empty(mGroupCode)) {
			alertErr("請先點選團體代號!!");
			return;
		}
		commGroupCode("comm_group_code");
		wp.selectSQL = "hex(rowid) as rowid2, " + "GROUP_CODE , " + "MCHT_GROUP_ID as data_code ";
		wp.daoTable = mMainTable;
		wp.whereStr = "where 1=1 and group_code=:group_code ";
		setString("group_code", mGroupCode);
		pageQuery();
		wp.setListCount(1);
		wp.notFound = "";
		lsCt2 = wp.selectCnt;
		wp.colSet("row_ct", lsCt2);
	}

	@Override
	public void saveFunc() throws Exception {
		Mktm0210Func func = new Mktm0210Func(wp);
		String lsSql = "", dsSql = "";
		String lsProgramCode = wp.itemStr("program_code");
		if (strAction.equals("S2")) {
			if (wp.itemStr("app_flag").equals("Y.未異動")) {
				strAction = "A";
			} else if (wp.itemStr("app_flag").equals("U.更新待覆核")) {
				strAction = "U";
			}
		}
		if (strAction.equals("D")) {
			if (wp.itemStr("apr_flag").equals("Y")) {
				alertErr("已覆核資料不可刪除!!");
				return;
			}
		}		
		//己覆核不可修改
		if (strAction.equals("U")) {
//			if (wp.itemStr("apr_flag").equals("Y")) {
//				alertErr("資料已存在.是否要異動");
//				return;
//			}
			//已复核数据重新保存到MKT_PURC_GP_T
			if(wp.itemStr("apr_flag").equals("Y") && wp.itemStr("conf_2").equals("Y")) {
				wp.colSet("apr_flag", "N");
				wp.colSet("aud_type", "U");
				strAction = "A";
			}
		}
		
		if (ofValidation() != 1) {
			return;
		}

		if (strAction.equals("A")) {
			lsSql = "select count(*) as tot_cnt from mkt_purc_gp_t " + "where group_code =:group_code ";
			setString("group_code", wp.colStr("group_code"));
			sqlSelect(lsSql);
			if (sqlNum("tot_cnt") > 0) {
				alertErr("異動資料已存在無法新增!!");
				return;
			}else {
				//检查【MKT_PURC_GP 联名机构消费回馈参数档
				lsSql = "select count(*) as tot_cnt from mkt_purc_gp " + "where group_code =:group_code ";
				setString("group_code", wp.colStr("group_code"));
				sqlSelect(lsSql);
				//如果数据重复
				if (sqlNum("tot_cnt") > 0) {
					if((wp.itemEmpty("conf_1") || wp.itemStr("conf_1").equals("false")) && wp.itemEmpty("conf_2")) { 
						wp.colSet("conf_1","true");
						return;		
					}
					if(wp.itemStr("conf_1").equals("true")) {
						if (wp.itemStr2("conf_2").equals("Y")) {
							wp.colSet("apr_flag", "N");
							wp.colSet("aud_type", "A");
							insertFunc1();
							insertFunc2();
							insertFunc3();
							insertPOS();
						} else {
							wp.colSet("conf_1","false");
							wp.colSet("conf_2","");
							return;
						}
					}
					wp.colSet("conf_1","false");
					wp.colSet("conf_2","");
				}
			}
		}
		
		rc = func.dbSave(strAction);
		// ddd(func.getMsg());
		// if (rc!=1) {
		// err_alert(func.getMsg());
		// }
		this.sqlCommit(rc);

		// 刪除時一併刪除ptr_bn_data_t 及 mkt_purcgp_ext_t
		if (strAction.equals("D")) {
			// delete ptr_bn_data_t
			dsSql = "delete ptr_bn_data_t " + "where program_code =:program_code ";
			setString("program_code", lsProgramCode);
			sqlExec(dsSql);
			// delete mkt_purcgp_ext_t
			dsSql = "delete mkt_purcgp_ext_t " + "where group_code =:group_code ";
			setString("group_code", wp.itemStr("group_code"));
			sqlExec(dsSql);
		}
		
		if(wp.itemStr("apr_flag").equals("N")) {
		    //資料異動成功
		    okMsg("資料異動成功");
		}

	}

	int ofValidation() {
		String lsSql = "", dsSql = "";
		// ***判斷mkt_purc_gp+mkt_purc_gp_t筆數是否大於250
		double llCnt = 0, llCnt1 = 0, llCnt2 = 0;
		lsSql = "select  count(*) llCnt from mkt_purc_gp "
				+ "where group_code not in (select group_code from mkt_purc_gp_t) ";
		sqlSelect(lsSql);
		llCnt1 = sqlInt("llCnt");
		lsSql = "select  count(*) llCnt2 from mkt_purc_gp_t ";
		sqlSelect(lsSql);
		llCnt2 = sqlInt("llCnt2");
		llCnt = llCnt1 + llCnt2;
		if (llCnt >= 250) {
			errmsg("筆數已大於250筆,禁止新增");
			return -1;
		}

		// -check item_ename_bl
		if (!wp.itemStr("item_ename_bl").equals("1") && !wp.itemStr("item_ename_ca").equals("1")
				&& !wp.itemStr("item_ename_id").equals("1") && !wp.itemStr("item_ename_ao").equals("1")
				&& !wp.itemStr("item_ename_it").equals("1") && !wp.itemStr("item_ename_ot").equals("1")) {
			alertErr("未指定消費金額累計科目");
			return -1;
		}

		if (!wp.itemStr("item_ename_bl").equals("1"))
			wp.itemSet("item_ename_bl", "0");
		if (!wp.itemStr("item_ename_ca").equals("1"))
			wp.itemSet("item_ename_ca", "0");
		if (!wp.itemStr("item_ename_id").equals("1"))
			wp.itemSet("item_ename_id", "0");
		if (!wp.itemStr("item_ename_ao").equals("1"))
			wp.itemSet("item_ename_ao", "0");
		if (!wp.itemStr("item_ename_it").equals("1"))
			wp.itemSet("item_ename_it", "0");
		if (!wp.itemStr("item_ename_ot").equals("1"))
			wp.itemSet("item_ename_ot", "0");
		if (wp.itemStr("purch_date_type").equals("1")) {
			if (empty(wp.itemStr("run_time_dd")) || wp.itemStr("run_time_dd").equals("0")) {
				alertErr("依消費日計算請指定執行日期!!");
				return -1;
			}
		}

		if (!wp.itemStr("purch_date_type").equals("1")) {
			wp.itemSet("run_time_dd", "0");
		}
		if (empty(wp.itemStr("base_amt_1"))) {
			wp.itemSet("base_amt_1", "0");
		}

		// 店內外
		if (wp.itemStr("reward_type").equals("2")) {
			if (wp.itemStr("item_ename_bl").equals("1")) {
				if (!wp.itemStr("item_ename_bl_in").equals("1") && !wp.itemStr("item_ename_bl_out").equals("1")) {
					alertErr("簽帳款 需指定店內或店外~");
					return -1;
				}
			}
			if (wp.itemStr("item_ename_it").equals("1")) {
				if (!wp.itemStr("item_ename_it_in").equals("1") && !wp.itemStr("item_ename_it_out").equals("1")) {
					alertErr("分期付款 需指定店內或店外~");
					return -1;
				}
			}
		}
		if (wfCheckrange() != 1)
			return -1;

		return 1;
	}

	int wfCheckrange() {
		String lsScol = "", lsEcol = "";
		String[] lsMsg;
		double ldcSval = 0, ldcEval = 0, ldcTol = 0;
		int i = 0, li_rc = 1;
		lsMsg = new String[] { "一", "二", "三", "四", "五" };
		if (wp.itemStr("reward_type").equals("1")) {
			for (i = 1; i <= 5; i++) {
				lsScol = "purch_amt_s_a" + (i);
				lsEcol = "purch_amt_e_a" + (i);
				ldcSval = wp.itemNum(lsScol);
				ldcEval = wp.itemNum(lsEcol);
				if (ldcSval > ldcEval) {
					li_rc = -1;
					alertErr("區間範圍 " + lsMsg[i] + " 輸入錯誤~");
					return li_rc;
				}
				ldcTol += ldcSval + ldcEval;
			}
			for (i = 1; i <= 4; i++) {
				lsScol = "int_amt_e_" + (i);
				lsEcol = "int_amt_s_" + (i + 1);
				ldcEval = wp.itemNum(lsEcol);
				if (ldcEval == 0) {
					continue;
				}
				ldcSval = wp.itemNum(lsScol);
				if (ldcSval > ldcEval) {
					li_rc = -1;
					alertErr("區間範圍 " + lsMsg[i] + " 輸入錯誤~");
					return li_rc;
				}
				ldcTol += ldcSval + ldcEval;
			}
			if (ldcTol == 0) {
				alertErr("未設定金額區間範圍");
				return -1;
			}
		}

		if (wp.itemStr("reward_type").equals("2")) {
			// 店內
			for (i = 1; i <= 5; i++) {
				lsScol = "int_amt_s_" + (i);
				lsEcol = "int_amt_e_" + (i);
				ldcSval = wp.itemNum(lsScol);
				ldcEval = wp.itemNum(lsEcol);
				if (ldcSval > ldcEval) {
					li_rc = -1;
					alertErr("區間範圍 " + lsMsg[i] + " 輸入錯誤~");
					return li_rc;
				}
				ldcTol += ldcSval + ldcEval;
			}
			for (i = 1; i <= 4; i++) {
				lsScol = "int_amt_e_" + (i);
				lsEcol = "int_amt_s_" + (i + 1);
				ldcEval = wp.itemNum(lsEcol);
				if (ldcEval == 0) {
					continue;
				}
				ldcSval = wp.itemNum(lsScol);
				if (ldcSval > ldcEval) {
					li_rc = -1;
					alertErr("區間範圍 " + lsMsg[i] + " 輸入錯誤~");
					return li_rc;
				}
				ldcTol += ldcSval + ldcEval;
			}
			// 店外
			for (i = 1; i <= 5; i++) {
				lsScol = "out_amt_s_" + (i);
				lsEcol = "out_amt_e_" + (i);
				ldcSval = wp.itemNum(lsScol);
				ldcEval = wp.itemNum(lsEcol);
				if (ldcSval > ldcEval) {
					li_rc = -1;
					alertErr("區間範圍 " + lsMsg[i] + " 輸入錯誤~");
					return li_rc;
				}
				ldcTol += ldcSval + ldcEval;
			}
			for (i = 1; i <= 4; i++) {
				lsScol = "out_amt_e_" + (i);
				lsEcol = "out_amt_s_" + (i + 1);
				ldcEval = wp.itemNum(lsEcol);
				if (ldcEval == 0) {
					continue;
				}
				ldcSval = wp.itemNum(lsScol);
				if (ldcSval > ldcEval) {
					li_rc = -1;
					alertErr("區間範圍 " + lsMsg[i] + " 輸入錯誤~");
					return li_rc;
				}
				ldcTol += ldcSval + ldcEval;
			}
			if (ldcTol == 0) {
				alertErr("未設定店內/外~金額區間範圍");
				return -1;
			}
		}
		return 1;
	}

	// 排除特店群組/代號設定 from ptr_bn_data_t
	public void insertFunc1() throws Exception {
		// 排除特店群組/代號維護
		setSelectLimit(0);
		String lsSql = "", isSql = "", dsSql = "";
		int llOk = 0, llErr = 0, rc = 0;
		String cc_program_code = "mktm0210_" + wp.itemStr("group_code");
		String[] cc_data_code = wp.itemBuff("data_code"); // 特店群組數組
		String[] cc_opt = wp.itemBuff("opt");
		wp.listCount[0] = cc_data_code.length;
		// 特店群組維護
		dsSql = "delete ptr_bn_data_t where program_code =:program_code and data_type = '1' ";
		setString("program_code", cc_program_code);
		sqlExec(dsSql);

		for (int ll = 0; ll < cc_data_code.length; ll++) {
			if (checkBoxOptOn(ll, cc_opt)) {
				continue;
			}
			if (empty(cc_data_code[ll])) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "特店群組資料空白 !!");
				llErr++;
				continue;
			}
			// -check duplication-
			if (ll != Arrays.asList(cc_data_code).indexOf(cc_data_code[ll])) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "特店群組資料值重複 !!");
				llErr++;
				continue;
			}
			isSql = " insert into ptr_bn_data_t ( " + "program_code, data_type, data_code, mod_user, mod_time, mod_pgm "
					+ ") values ( " + ":ls_key, '1', :ls_code, :ls_mod_user, sysdate, 'mktm0210' ) ";
			setString("ls_key", cc_program_code);
			setString("ls_code", cc_data_code[ll]);
			setString("ls_mod_user", wp.loginUser);
			sqlExec(isSql);
			if (sqlRowNum <= 0) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "特店群組資料異動失敗!!");
				llErr++;
				sqlCommit(0);
				continue;
			} else {
				llOk++;
				wp.colSet(ll, "ok_flag", "V");
			}
		}
		sqlCommit(1);
		alertMsg("資料更新成功!");

		// 特店代號維護
		// 刪除原特店代號資料
		dsSql = "delete ptr_bn_data_t " + "where program_code =:program_code " + "and data_type = '2' ";
		setString("program_code", cc_program_code);
		sqlExec(dsSql);
		// 讀取特店群組資料 from ptr_bn_data_t
		setSelectLimit(0);
		lsSql = "select program_code, " + "data_type, " + "data_code " + "from ptr_bn_data_t " + "where 1=1 "
				+ "and program_code =:program_code " + "and data_type = '1' ";
		setString("program_code", cc_program_code);
		sqlSelect(lsSql);
		int s_row = sqlRowNum; // 查詢結果條數
		String ls_data_key = ""; // 特店群組code
		if (s_row > 0) {
			for (int ii = 0; ii < s_row; ii++) {
				ls_data_key = sqlStr(ii, "data_code");
				// 讀取特店群組內特店代號資料 from mkt_mchtgp_data
				setSelectLimit(0);
				lsSql = "select table_name, " + "data_key, " + "data_type, " + "data_code " + "from mkt_mchtgp_data "
						+ "where 1=1 " + "and table_name ='MKT_MCHT_GP' " + "and data_key =:data_key "
						+ "and data_type ='1' " + "group by table_name,data_key,data_type,data_code ";
				setString("data_key", ls_data_key);
				sqlSelect(lsSql);
				int m_row = sqlRowNum;
				if (m_row > 0) {
					for (int jj = 0; jj < m_row; jj++) {
						// 新增特店代號至ptr_bn_data_t
						isSql = " insert into ptr_bn_data_t ( "
								+ "program_code, data_type, data_code, mod_user, mod_time, mod_pgm " + ") values ( "
								+ ":ls_key, '2', :ls_code, :ls_mod_user, sysdate, 'mktm0210' ) ";
						setString("ls_key", cc_program_code);
						setString("ls_code", sqlStr(jj, "data_code"));
						setString("ls_mod_user", wp.loginUser);
						sqlExec(isSql);
						if (sqlRowNum <= 0) {
							alertErr("新增特店代號資料失敗!!");
							return;
						}
					}
				} else {
					alertErr("查無特店資料!!");
					return;
				}
			}
		} else {
			alertErr("查無特店群資料!!");
			return;
		}
		sqlCommit(1);
		alertMsg("資料更新成功!");

	}

	// 排除特店代號設定 from ptr_bn_data_t
	public void insertFunc2() throws Exception {
		// 店內/店外特店
		String isSql = "", dsSql = "";
		int llOk = 0, llErr = 0, rc = 0;
		String cc_program_code = wp.itemStr("kk_program_code");
//		String cc_program_code = "mktm0210_" + wp.itemStr("group_code");
		String[] cc_data_code = wp.itemBuff("data_code");
		String[] cc_opt = wp.itemBuff("opt");
		wp.listCount[0] = cc_data_code.length;
		dsSql = "delete ptr_bn_data_t where program_code =:program_code and data_type = '2'";
		setString("program_code", cc_program_code);
		sqlExec(dsSql);
		for (int ll = 0; ll < cc_data_code.length; ll++) {
			if (checkBoxOptOn(ll, cc_opt)) {
				continue;
			}
			if (empty(cc_data_code[ll])) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "特店資料空白 !!");
				llErr++;
				continue;
			}
			// -check duplication-
			if (ll != Arrays.asList(cc_data_code).indexOf(cc_data_code[ll])) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "特店資料值重複 !!");
				llErr++;
				continue;
			}
			isSql = " insert into ptr_bn_data_t ( " + "program_code, data_type, data_code, mod_user, mod_time, mod_pgm "
					+ ") values ( " + ":ls_key, '2', :ls_code, :ls_mod_user, sysdate, 'mktm0210' ) ";
			setString("ls_key", cc_program_code);
			setString("ls_code", cc_data_code[ll]);
			setString("ls_mod_user", wp.loginUser);
			sqlExec(isSql);
			if (sqlRowNum <= 0) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "Insert ptr_bn_data_t error ");
				llErr++;
				continue;
			} else {
				llOk++;
				wp.colSet(ll, "ok_flag", "V");
			}
		}
		sqlCommit(1);
		alertMsg("資料更新成功!");
	}

	// POS終端機代號設定 from ptr_bn_data_t
	public void insertPOS() throws Exception {
		String isSql = "", dsSql = "";
		int llOk = 0, llErr = 0, rc = 0;
		String cc_program_code = wp.itemStr("kk_program_code");
//		String cc_program_code = "mktm0210_" + wp.itemStr("group_code");
		String[] cc_data_code = wp.itemBuff("data_code");
		String[] cc_opt = wp.itemBuff("opt");
		wp.listCount[0] = cc_data_code.length;
		dsSql = "delete ptr_bn_data_t where program_code =:program_code and data_type = '5'";
		setString("program_code", cc_program_code);
		sqlExec(dsSql);
		for (int ll = 0; ll < cc_data_code.length; ll++) {
			if (checkBoxOptOn(ll, cc_opt)) {
				continue;
			}
			if (empty(cc_data_code[ll])) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "POS資料空白 !!");
				llErr++;
				continue;
			}
			// -check duplication-
			if (ll != Arrays.asList(cc_data_code).indexOf(cc_data_code[ll])) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "POS資料值重複 !!");
				llErr++;
				continue;
			}
			isSql = " insert into ptr_bn_data_t ( " + "program_code, data_type, data_code, mod_user, mod_time, mod_pgm "
					+ ") values ( " + ":ls_key, '5', :ls_code, :ls_mod_user, sysdate, 'mktm0210' ) ";
			setString("ls_key", cc_program_code);
			setString("ls_code", cc_data_code[ll]);
			setString("ls_mod_user", wp.loginUser);
			sqlExec(isSql);
			if (sqlRowNum <= 0) {
				wp.colSet(ll, "ok_flag", "X");
				wp.colSet(ll, "err_mesg", "Insert ptr_bn_data_t error ");
				llErr++;
				continue;
			} else {
				llOk++;
				wp.colSet(ll, "ok_flag", "V");
			}
		}
		sqlCommit(1);
		alertMsg("資料更新成功!");
	}

	// 特店群組設定 form mkt_purcgp_ext_t
	public void insertFunc3() throws Exception {
		// 店內/店外消費特店群組
		String group_code = wp.itemStr("group_code");
		String[] cc_data_code = wp.itemBuff("data_code");
		String[] cc_opt = wp.itemBuff("opt");
		wp.listCount[0] = cc_data_code.length;
		// wf_dupl_mchtno
		int l = 0, ll = 0, ll_dupl = 0;
		for (ll = 0; ll < cc_data_code.length; ll++) {
			if (checkBoxOptOn(l, cc_opt))
				continue;
			// -check duplication-
			if (ll != Arrays.asList(cc_data_code).indexOf(cc_data_code[ll])) {
				wp.colSet(ll, "ok_flag", "X");
				ll_dupl++;
				continue;
			}
		}
		if (ll_dupl > 0) {
			alertErr2("特店群組代號有重複");
			return;
		}

		// insert & delete
		String isSql = "", dsSql = "";
		dsSql = "delete mkt_purcgp_ext_t " + "where 1=1 " + "and group_code =:group_code ";
		setString("group_code", group_code);
		sqlExec(dsSql);
		if (sqlRowNum < 0) {
			alertErr2("特店群組資料處理失敗1!!");
			sqlCommit(0);
			return;
		}

		for (l = 0; l < cc_data_code.length; l++) {
			if (!checkBoxOptOn(l, cc_opt)) {
				if (empty(cc_data_code[l]))
					continue;
				isSql = " insert into mkt_purcgp_ext_t ( " + "GROUP_CODE, MCHT_GROUP_ID, MOD_USER, MOD_TIME, MOD_PGM "
						+ ") values ( " + " :group_code, :mcht_group_id,:mod_user, sysdate, :mod_pgm ) ";
				setString("group_code", group_code);
				setString("mcht_group_id", cc_data_code[l]);
				setString("mod_user", wp.loginUser);
				setString("mod_pgm", "mktm0210");
				sqlExec(isSql);
				if (sqlRowNum <= 0) {
					alertErr2("特店群組資料處理失敗2!!");
					sqlCommit(0);
					return;
				} else {
					sqlCommit(1);
					alertMsg("特店群組資料處理成功!!");
				}
			}
		}
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}
	public void commGroupCode(String columnData1) throws Exception {
		String columnData = "";
		String sql1 = "";
		  for (int ii = 0; ii < wp.selectCnt; ii++) {
		     columnData = "";
		     sql1 = "select " + " group_name as column_group_name " + " from ptr_group_code "
		          + " where 1 = 1 " + " and   group_code = '" + wp.colStr(ii, "group_code") + "'";
		  if (wp.colStr(ii, "group_code").length() == 0) {
		        continue;
		      }
	  sqlSelect(sql1);

		 if (sqlRowNum > 0) {
		     columnData = columnData + sqlStr("column_group_name");
		     wp.colSet(ii, columnData1, columnData);
		      }
		    }
		    return;
		  }

	@Override
	public void dddwSelect() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			try {
				// dddw_group_code
				wp.initOption = "--";
				wp.optionKey = itemkk("kk_group_code");
				dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
						"where 1=1 group by group_code,group_name order by group_code");
			} catch (Exception ex) {
			}

		}
		if (wp.respHtml.indexOf("_mcht_group") > 0) {
			try {
				// dddw_mcht_group
				wp.initOption = "--";
				wp.optionKey = itemkk("dddw_mcht_group");
				dddwList("dddw_mcht_group", "mkt_mcht_gp", "mcht_group_id", "mcht_group_desc",
						"where 1=1 order by mcht_group_id");

			} catch (Exception ex) {
			}
		}
		if (wp.respHtml.indexOf("_mcht_group_out") > 0) {
			try {
				// dddw_mcht_group
				wp.initOption = "--";
				wp.optionKey = itemkk("dddw_mcht_group");
				dddwList("dddw_mcht_group", "mkt_mcht_gp", "mcht_group_id", "mcht_group_desc",
						"where 1=1 order by mcht_group_id");

			} catch (Exception ex) {
			}
		}
	}

	@Override
	public void userAction() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void procFunc() throws Exception {
		if (itemIsempty("zz_file_name")) {
			alertErr2("上傳檔名: 不可空白");
			return;
		}
		file_dataImp();
	}

	void file_dataImp() throws Exception {
		TarokoFileAccess tf = new TarokoFileAccess(wp);

		String inputFile = wp.itemStr("zz_file_name");
		// int fi = tf.openInputText(inputFile,"UTF-8"); //決定上傳檔內碼
		int fi = tf.openInputText(inputFile, "MS950");
		if (fi == -1) {
			return;
		}
		String cc_program_code = wp.itemStr("program_code");
		Mktm0210Func func = new Mktm0210Func(wp);
		func.setConn(wp);

		wp.logSql = false;
		String lsSql = "";
		int llOk = 0, llErr = 0, llCnt = 0;
		while (true) {
			String tmpStr = tf.readTextFile(fi);
			if (tf.endFile[fi].equals("Y")) {
				break;
			}
			if (tmpStr.length() < 2) {
				continue;
			}

			llCnt++;
			String[] split_line = tmpStr.split(",");

			try {
				String cc_data_code = split_line[0];// data_code特店代號

				// lsSql = "select data_code "
				// + "from ptr_bn_data_t "
				// + "where 1=1 "
				// + "and date_type = '2' ";
				// lsSql += sql_col(cc_program_code, "program_code");
				// lsSql += sql_col(cc_data_code, "data_code");
				// sqlSelect(lsSql);
				// if (sql_nrow > 0) {
				// llErr++;
				// continue;
				// }
				// } else {
				// wp.item_set("data_code", cc_data_code);
				// }
				func.varsSet("aa_program_code", cc_program_code);
				func.varsSet("aa_data_code", cc_data_code);
				func.varsSet("aa_data_type", itemkk("data_k1"));
				// server debug message ==>只會顯示最後一筆訊息
				// wp.alertMesg = "<script
				// language='javascript'>alert('"+cc_program_code+" --
				// "+cc_data_code+"--"+item_kk("data_k1")+"')</script>";
				if (func.dbInsert1() != 1) {
					llErr++;
					continue;
				}

				if (rc < 0) {
					llErr++;
				} else {
					llOk++;
				}
				// 固定長度上傳檔
				// wp.item_set("id_no",commString.mid_big5(ss,0,10));
				// wp.item_set("data_flag1",commString.mid_big5(ss,10,1));
				// wp.item_set("data_flag2",commString.mid_big5(ss,11,1));

			} catch (Exception e) {
				alertMsg("匯入資料異常!!");
				return;
			}

			// llCnt++;
			// int rr=llCnt-1;
			// this.set_rowNum(rr, llCnt);

		}
		// wp.listCount[0]=llCnt; //--->開啟上傳檔檢視
		tf.closeInputText(fi);
		tf.deleteFile(inputFile);
		alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk + ", 失敗筆數=" + llErr);
		// queryRead();

	}

	public void processAjaxOption() throws Exception {
		wp.varRows = 1000;
		wp.selectSQL = "mcht_no,mcht_chi_name ";
		wp.daoTable = "bil_merchant ";
		wp.whereStr = "where mcht_status = '1' and mcht_no like :mcht_no ";
		wp.orderField = "mcht_no ";
		setString("mcht_no", wp.getValue("kk_merchant", 0) + "%");

		pageQuery();

		for (int i = 0; i < wp.selectCnt; i++) {

			wp.addJSON("OPTION_TEXT", wp.colStr(i, "mcht_no") + "_" + wp.colStr(i, "mcht_chi_name"));
			wp.addJSON("OPTION_VALUE", wp.colStr(i, "mcht_no"));
		}
		return;
	}

	
}

