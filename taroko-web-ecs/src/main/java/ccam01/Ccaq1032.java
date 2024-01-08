package ccam01;
/** 交易紀錄查詢
 * 19-0611:    JH    p_xxx >>acno_p_xxx
 *109-04-19    shiyuqi       updated for project coding standard 
 *109-12-22    Justin         parameterize sql
 *109-12-31  V1.00.03   shiyuqi      修改无意义命名                                                                                      *
 *110-01-06  V1.00.04   tanwei       修改zz開頭變量  
 *110-01-06    Justin         updated for XSS
 *110-01-18    Justin         fix a bug
 *110-01-30    Justin         修改XSS bugs
 *111-03-25    Alex           mcht_name 修改
 * */

import java.util.ArrayList;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;

public class Ccaq1032 extends BaseAction implements InfaceExcel {
	taroko.base.CommDate commDate = new taroko.base.CommDate();

	String cardNo, txDate, txTime, authNo, traceNo ,authSeqno;
	String isIdPSeqno1 = "", isIdPSeqno2 = "", lsWhere = "";
	ArrayList<Object> whereParameterList = null;
	ccam01.Ccaq1032Func func;
	ofcapp.AppMsg appmsg = new ofcapp.AppMsg();

	@Override
	public void userAction() throws Exception {

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
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 動態查詢 */
			dataRead2();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -PDF-
			strAction = "XLS";
			xlsPrint();
		}

	}

	@Override
	public void initPage() {
		wp.colSet("ex_tx_date1", commDate.sysDate());
		wp.colSet("ex_tx_date2", commDate.sysDate());
	}

	@Override
	public void queryFunc() throws Exception {
		
		if(wp.itemEmpty("ex_tx_date1") || wp.itemEmpty("ex_tx_date2")) {
			alertErr("交易日期起迄 不可空白");
			return ;
		}
		
		if (wp.itemEmpty("ex_tx_time1") == false || wp.itemEmpty("ex_tx_time2") == false) {
			if (wp.itemEmpty("ex_tx_date1")) {
				this.alertErr("[交易日期-起] 不可空白");
				return;
			}
		}

		if (!wp.itemEmpty("ex_amt1") && !wp.itemEmpty("ex_amt2")) {
			if (wp.itemNum("EX_AMT1") > wp.itemNum("EX_AMT2")) {
				alertErr2("消費金額起迄錯誤");
				return;
			}
		}

		String lsWhere = getWhere();
						
		wp.colSet("is_same", "");
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		
		//-- lsWhere 回空白目前只有身分證錯誤
		if(lsWhere.isEmpty()) {
			errmsg("身分證ID:輸入錯誤");
			return ;
		}
		
		queryRead();
	}

	String getWhere() {
		ArrayList<Object> whereParams = new ArrayList<Object>(); 
		
		lsWhere = " where 1=1 " ;
		if (!wp.itemEmpty("ex_card_no")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_card_no"), "A.card_no", "like%");
			whereParams.add(wp.itemStr("ex_card_no")+"%");
		}
		if (!wp.itemEmpty("ex_mcht_no")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_mcht_no"), "A.mcht_no");
			whereParams.add(wp.itemStr("ex_mcht_no"));
		}
		if (!wp.itemEmpty("ex_mcc_code")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_mcc_code"), "A.mcc_code");
			whereParams.add(wp.itemStr("ex_mcc_code"));
		}
		if (!wp.itemEmpty("ex_entry_mode")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_entry_mode"), "substr(A.pos_mode,1,2)");
			whereParams.add(wp.itemStr("ex_entry_mode"));
		}
		if (!wp.itemEmpty("ex_stand_in")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_stand_in"), "A.stand_in");
			whereParams.add(wp.itemStr("ex_stand_in"));
		}
		if (!wp.itemEmpty("ex_country")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_country"), "A.consume_country");
			whereParams.add(wp.itemStr("ex_country"));
		}
		if (!wp.itemEmpty("ex_auth_status")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_auth_status"), "A.auth_status_code");
			whereParams.add(wp.itemStr("ex_auth_status"));
		}
		if (!wp.itemEmpty("ex_iso_resp_code")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_iso_resp_code"), "A.iso_resp_code");
			whereParams.add(wp.itemStr("ex_iso_resp_code"));
		}
		if (!wp.itemEmpty("ex_auth_user")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_auth_user"), "A.auth_user");
			whereParams.add(wp.itemStr("ex_auth_user"));
		}
		if (!wp.itemEmpty("ex_eci")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_eci"), "A.ec_flag");
			whereParams.add(wp.itemStr("ex_eci"));
		}
		if (!wp.itemEmpty("ex_ucaf")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_ucaf"), "A.ucaf");
			whereParams.add(wp.itemStr("ex_ucaf"));
		}
		if (!wp.itemEmpty("ex_amt1")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_amt1"), "A.nt_amt", ">=");
			whereParams.add(wp.itemStr("ex_amt1"));
		}
		if (!wp.itemEmpty("ex_amt2")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_amt2"), "A.nt_amt", "<=");
			whereParams.add(wp.itemStr("ex_amt2"));
		}
		if (!wp.itemEmpty("ex_class_code")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_class_code"), "A.class_code");
			whereParams.add(wp.itemStr("ex_class_code"));
		}
		
		if(!wp.itemEmpty("ex_ibm_resp1")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_ibm_resp1"), "A.ibm_bit39_code");
			whereParams.add(wp.itemStr("ex_ibm_resp1"));
		}
		
//        + sqlCol(wp.itemStr("ex_ibm_resp1"), "A.ibm_bit39_code")
//        + sqlCol(wp.itemStr("ex_ibm_resp2"), "A.ibm_bit33_code")

		// --
		if (wp.itemEq("ex_chk_vip", "Y")) {
			lsWhere += " and A.vip_code <> '' ";
		}
		// --
		if (wp.itemNum("ex_std_amt") != 0) {
			int liStdAmt = 0;
			liStdAmt = (int) wp.itemNum("ex_std_amt") * 10000;
			lsWhere += " and A.curr_tot_std_amt > ? ";
			whereParams.add(liStdAmt);
		}

		// --
		if (wp.itemNum("ex_curr_rate") != 0) {
			lsWhere += " and curr_tot_std_amt<> 0 "
					       + " and ((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)*100 > ? ";
			whereParams.add(wp.itemNum("ex_curr_rate"));
		}

		// --交易日期、交易時間
		if (wp.itemEmpty("ex_tx_time1") && wp.itemEmpty("ex_tx_time2")) {
			lsWhere += commSqlStr.strend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2"), "A.tx_date");
			if (wp.itemEmpty("ex_tx_date1") == false) {
				whereParams.add(wp.itemStr("ex_tx_date1"));
			}
			if (wp.itemEmpty("ex_tx_date2") == false) {
				whereParams.add(wp.itemStr("ex_tx_date2"));
			}
			
		} else {
			lsWhere += commSqlStr.strend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2"), "A.tx_date");
			if (wp.itemEmpty("ex_tx_date1") == false) {
				whereParams.add(wp.itemStr("ex_tx_date1"));
			}
			if (wp.itemEmpty("ex_tx_date2") == false) {
				whereParams.add(wp.itemStr("ex_tx_date2"));
			}
			
			lsWhere += commSqlStr.strend(wp.itemStr("ex_tx_time1"), wp.itemStr("ex_tx_time2"), "A.tx_time");
			if (wp.itemEmpty("ex_tx_time1") == false) {
				whereParams.add(wp.itemStr("ex_tx_time1"));
			}
			if (wp.itemEmpty("ex_tx_time2") == false) {
				whereParams.add(wp.itemStr("ex_tx_time2"));
			}
		}

		// --身分證ID
		if (!wp.itemEmpty("ex_idno")) {
			selectIdPseqno();
			if (!empty(isIdPSeqno1) && !empty(isIdPSeqno2)) {
				lsWhere += " and A.card_no in ( select card_no from cca_card_base where ( debit_flag<>'Y'"
						+ commSqlStr.col(isIdPSeqno1, "major_id_p_seqno") 
						+ ") or (debit_flag='Y'"
						+ commSqlStr.col(isIdPSeqno2, "major_id_p_seqno") 
						+ ") )";
				whereParams.add(isIdPSeqno1);
				whereParams.add(isIdPSeqno2);
			} else if (!empty(isIdPSeqno1)) {
				lsWhere += " and A.card_no in (select card_no from cca_card_base where debit_flag<>'Y'"
						+ commSqlStr.col(isIdPSeqno1, "major_id_p_seqno") 
						+ ")";
				whereParams.add(isIdPSeqno1);
			} else if (!empty(isIdPSeqno2)) {
				lsWhere += " and A.card_no in (select card_no from cca_card_base where debit_flag='Y'"
						+ commSqlStr.col(isIdPSeqno2, "major_id_p_seqno") 
						+ ")";
				whereParams.add(isIdPSeqno2);
			} else {
				alertErr2("身分證ID:輸入錯誤");
				return "";
			}
		}

		// --排除MCC
		if (!wp.itemEmpty("ex_excl_mcc1") || !wp.itemEmpty("ex_excl_mcc2") || !wp.itemEmpty("ex_excl_mcc3")) {
			String lsExclMcc = "";
			if (!wp.itemEmpty("ex_excl_mcc1")) {
				if (lsExclMcc.length() == 0) {
					lsExclMcc += " and A.mcc_code not in ( ? ";
					whereParams.add(wp.itemStr("ex_excl_mcc1"));
				}
				else{
					lsExclMcc += ",? ";
					whereParams.add(wp.itemStr("ex_excl_mcc1"));
				}
			}
			if (!wp.itemEmpty("ex_excl_mcc2")) {
				if (lsExclMcc.length() == 0) {
					lsExclMcc += " and A.mcc_code not in ( ? ";
					whereParams.add(wp.itemStr("ex_excl_mcc2"));
				}
				else{
					lsExclMcc += ", ? ";
					whereParams.add(wp.itemStr("ex_excl_mcc2"));
				}
			}
			if (!wp.itemEmpty("ex_excl_mcc3")) {
				if (lsExclMcc.length() == 0) {
					lsExclMcc += " and A.mcc_code not in ( ? ";
					whereParams.add(wp.itemStr("ex_excl_mcc3"));
				}
				else{
					lsExclMcc += ", ? ";
					whereParams.add(wp.itemStr("ex_excl_mcc3"));
				}
			}
			if (!empty(lsExclMcc)) {
				lsExclMcc += ")";
				lsWhere += lsExclMcc;
			}
		}

		// --排除TW
		if (wp.itemEq("ex_no_tw", "Y")) {
			lsWhere += " and A.consume_country not in ('TW','TWN') ";
		}

		// --授權
		if (wp.itemEq("ex_chk_01", "Y")) {
			lsWhere += " and A.auth_unit = 'K' ";
		}
		if (wp.itemEq("ex_chk_02", "Y")) {
			lsWhere += " and A.auth_unit <> 'K' ";
		} else {
			String lsAuthWhere = "";
			if (wp.itemEq("ex_chk_03", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('V'";
				else
					lsAuthWhere += ",'V'";
			}
			if (wp.itemEq("ex_chk_04", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('M'";
				else
					lsAuthWhere += ",'M'";
			}
			if (wp.itemEq("ex_chk_05", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('F'";
				else
					lsAuthWhere += ",'F'";
			}
			if (wp.itemEq("ex_chk_06", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('C'";
				else
					lsAuthWhere += ",'A'";
			}
			if (wp.itemEq("ex_chk_08", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('C'";
				else
					lsAuthWhere += ",'J'";
			}
			if (wp.itemEq("ex_chk_nccc", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('MP'";
				else
					lsAuthWhere += ",'MP'";
			}
			if (wp.itemEq("ex_chk_acq", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('C'";
				else
					lsAuthWhere += ",'C'";
			}
			if (wp.itemEq("ex_chk_tscc", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('T'";
				else
					lsAuthWhere += ",'T'";
			}
			
			if (wp.itemEq("ex_chk_ipass", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('I'";
				else
					lsAuthWhere += ",'I'";
			}
			
			if (wp.itemEq("ex_chk_ich", "Y")) {
				if (empty(lsAuthWhere))
					lsAuthWhere += " and A.auth_unit in ('H'";
				else
					lsAuthWhere += ",'H'";
			}
			
			
			if (!empty(lsAuthWhere)) {
				lsAuthWhere += " )";
				lsWhere += lsAuthWhere;
			}
		}

		// --行動支付
//    if (wp.itemEq("ex_chk_hce", "Y")) {
//      lsWhere += " and A.v_card_no <> '' ";
//    }

		// --交易類別
		if (!wp.itemEmpty("ex_trans_code")) {
			lsWhere += commSqlStr.col(wp.itemStr("ex_trans_code"), "A.trans_code");
			whereParams.add(wp.itemStr("ex_trans_code"));
		}

		// --fall bank
		if (wp.itemEq("ex_chk_fback", "Y")) {
			lsWhere += " and A.fallback = 'Y' ";
		}

		// --entry mode type
		if (!wp.itemEmpty("ex_entry_type")) {
			lsWhere += " and substr(A.pos_mode,1,2) "
					+ " in ( select entry_mode from cca_entry_mode "
					+ " where entry_type = ? )";
			whereParams.add(wp.itemStr("ex_entry_type"));
		}

		// --行員 金控
		if (eqIgno(wp.itemNvl("ex_emp_bank", "N"), "Y")) {
			lsWhere += " and A.id_p_seqno in (select B.id_p_seqno from crd_idno B join crd_employee C on B.id_no = C.id where C.status_id = '1' "
					+ " union all select B.id_p_seqno from dbc_idno B join crd_employee C on B.id_no = C.id where C.status_id = '1' )";
		}
//		if (eqIgno(wp.itemNvl("ex_emp_bank", "N"), "Y") && eqIgno(wp.itemNvl("ex_emp_fhc", "N"), "Y")) {
//			lsWhere += " and A.id_p_seqno in (select B.id_p_seqno from crd_idno B join ecs_employee C on B.id_no = C.id_no where C.status_id = '1' "
//					+ " union all select B.id_p_seqno from dbc_idno B join ecs_employee C on B.id_no = C.id_no where C.status_id = '1' )";
//		} else if (eqIgno(wp.itemNvl("ex_emp_bank", "N"), "Y")) {
//			lsWhere += " and A.id_p_seqno in (select B.id_p_seqno from crd_idno B join ecs_employee C on B.id_no = C.id_no where C.status_id = '1' and C.data_type ='1' "
//					+ " union all select B.id_p_seqno from dbc_idno B join ecs_employee C on B.id_no = C.id_no where C.status_id = '1' and C.data_type ='1' )";
//		} else if (eqIgno(wp.itemNvl("ex_emp_fhc", "N"), "Y")) {
//			lsWhere += " and A.id_p_seqno in (select B.id_p_seqno from crd_idno B join ecs_employee C on B.id_no = C.id_no where C.status_id = '1' and C.data_type ='2' "
//					+ " union all select B.id_p_seqno from dbc_idno B join ecs_employee C on B.id_no = C.id_no where C.status_id = '1' and C.data_type ='2' )";
//		}
		
		whereParameterList = whereParams;

		return lsWhere;
	}

	void selectIdPseqno() {
		String sql1 = " select uf_idno_pseqno(?) as id_p_seqno , uf_vd_idno_pseqno(?) as id_p_seqno2 from dual ";
		sqlSelect(sql1, new Object[] { wp.itemStr("ex_idno"), wp.itemStr("ex_idno") });

		isIdPSeqno1 = sqlStr("id_p_seqno");
		isIdPSeqno2 = sqlStr("id_p_seqno2");

	}

	@Override
	public void queryRead() throws Exception {
		Object[] paramArr = null ;
		if (whereParameterList != null) {
			paramArr = whereParameterList.toArray();
		}

		wp.pageControl();
		wp.selectSQL = "" + "A.trace_no ," + " A.auth_seqno ," + " A.card_no ," + " A.tx_date ," + " A.tx_time ,"
				+ " substr(A.eff_date_end,1,6) as eff_date_end ," + " A.mcht_no ," + " A.mcht_name as mcht_chi_name ," + " A.mcc_code ,"
				+ " A.pos_mode ," + " substr(A.pos_mode,1,2) as pos_mode_1_2 ,"
				+ " substr(A.pos_mode,3,1) as pos_mode_3 ," + " A.nt_amt ," + " A.consume_country ,"
				+ " A.tx_currency ," + " A.iso_resp_code ," + " A.auth_status_code ," + " A.iso_adj_code ,"
				+ " A.auth_no ," + " A.auth_user ," + " A.vip_code ," + " A.stand_in ," + " A.class_code ,"
				+ " A.auth_unit ," + " A.logic_del ," + " A.auth_remark ," + " A.trans_type ,"
//        + " uf_idno_id2(A.card_no,'') as id_no ," + " uf_idno_name(A.id_p_seqno) as db_idno_name ,"
				+ " A.curr_otb_amt ," + " A.curr_tot_lmt_amt ," + " A.curr_tot_std_amt ," + " A.curr_tot_tx_amt ,"
				+ " A.curr_tot_cash_amt ," + " A.curr_tot_unpaid ," + " A.fallback ," + " A.roc ," + " A.ec_ind ,"
				+ " A.ucaf ," + " A.mtch_flag, A.cacu_amount," + " A.ec_flag ,"
//        + " uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del ,"
				+ " A.v_card_no ," + " A.online_redeem ,"
//        + " uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit ,"
//        + " decode(A.online_redeem,'','','A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)',"
//        + " '3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem ,"
				+ " decode(curr_tot_std_amt,0,0,((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)) * 100 as cond_curr_rate , "
				+ " A.id_p_seqno , " + " iso_resp_code||'-'||auth_status_code||'-'||iso_adj_code as wk_resp , A.trans_code ,"
//        + " ,nvl((select sys_data1 from cca_sys_parm3 where sys_data2 = A.trans_code and sys_id = 'TRANCODE'),'其他交易') as tt_trans_code "
				+ " " + " ibm_bit39_code||'-'||ibm_bit33_code as wk_IBM  ";
		if (eqIgno(wp.itemNvl("ex_same", "N"), "Y")) {
			wp.selectSQL += " , (select count(*) from cca_auth_txlog where tx_date =A.tx_date and card_no =A.card_no and mcht_no =A.mcht_no "
					+ " and auth_status_code =A.auth_status_code and auth_no =A.auth_no and substr(pos_mode,1,2) =substr(A.pos_mode,1,2)) as same_cnt ";
		}

		wp.daoTable = " cca_auth_txlog A ";
		wp.whereOrder = " order by A.tx_date Desc, A.tx_time Desc ";

		pageQuery(paramArr);
		wp.setListCount(0);
		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			errmsg("此條件查無資料");
			wp.colSet("tl_tx_cnt", "0 筆");
			return;
		}

		wp.colSet("tl_tx_cnt", wp.totalRows + " 筆");
		wp.setPageValue();
		queryAfter(wp.listCount[0]);
	}

	void queryAfter(int llNrow) {
		// --特店中文名稱
		wp.logSql = false;
//		String sql1a = " select mcht_name,mcht_eng_name from cca_mcht_bill where mcht_no = ? and acq_bank_id = ?  "
//				+ commSqlStr.rownum(1);
//		String sql1b = " select mcht_name,mcht_eng_name from cca_mcht_bill where mcht_no = ?  " + commSqlStr.rownum(1);
		String sql2 = " select entry_type from cca_entry_mode where entry_mode = ? " + commSqlStr.rownum(1);
		
		String sql4 = "select id_no, chi_name as db_idno_name from vcard_idno where card_no =?";
		String sql5 = "select sys_data1 as tt_auth_unit from cca_sys_parm3 where sys_id = 'AUTHUNIT' and sys_key = ? ";
		String sql6 = "select sys_data1 as tt_trans_code from cca_sys_parm3 where sys_key =? and sys_id = 'TRANCODE' ";
		int ilSame = 0;

		for (int ii = 0; ii < llNrow; ii++) {
			String lsCardNo = wp.colStr(ii, "card_no");
			sqlSelect(sql4, lsCardNo);
			if (sqlRowNum > 0) {
				sql2wp(ii, "id_no");
				sql2wp(ii, "db_idno_name");
			}

//			sqlSelect(sql1a, new Object[] { wp.colStr(ii, "mcht_no"), wp.colStr(ii,"stand_in") });
//			if (sqlRowNum > 0) {
//				if (empty(sqlStr("mcht_name"))) {
//					wp.colSet(ii, "mcht_chi_name", sqlStr("mcht_eng_name"));
//				} else {
//					wp.colSet(ii, "mcht_chi_name", sqlStr("mcht_name"));
//				}
//			} else {
//				sqlSelect(sql1b, new Object[] { wp.colStr(ii, "mcht_no") });
//				if (sqlRowNum > 0) {
//					if (empty(sqlStr("mcht_name"))) {
//						wp.colSet(ii, "mcht_chi_name", sqlStr("mcht_eng_name"));
//					} else {
//						wp.colSet(ii, "mcht_chi_name", sqlStr("mcht_name"));
//					}
//				} else {
//					wp.colSet(ii, "mcht_chi_name", wp.colStr(ii, "mcht_name"));
//				}
//			}
			sqlSelect(sql2, new Object[] { wp.colStr(ii, "pos_mode_1_2") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "db_entry_mode_type", sqlStr("entry_type"));
			}

			if (eqIgno(wp.itemNvl("ex_same", "N"), "Y") && wp.colNum(ii, "same_cnt") > 1) {
				wp.colSet(ii, "wk_same", "*");
			}

			// --變色
//			if (!commString.strIn2(wp.colStr(ii, "mtch_flag"), ",Y,U") && !commString.strIn2(wp.colStr(ii, "logic_del"), "|x|B")
			if (!commString.strIn2(wp.colStr(ii, "mtch_flag"), ",Y,U") && !commString.strIn2(wp.colStr(ii, "logic_del"), "|B")
					&& wp.colEq(ii, "cacu_amount", "Y")) {
				wp.colSet(ii, "wk_color", "yellow");
			}

			String lsCompute0035 = "", lsServiceCode = "";
			lsCompute0035 = wp.colStr(ii, "pos_mode_1_2");
			
			wp.colSet(ii, "bk_color", "background-color: rgb(255,255,255)");
			//--cca_auth_bitdata Table 用途變更 lsServiceCode 相關判斷先 mark
//			lsServiceCode = selectSC(wp.colStr(ii, "card_no"), wp.colStr(ii, "auth_seqno"));
			// --bk_color
//			if (eqIgno(lsServiceCode, "101") && pos(",05,95", lsCompute0035) > 0) {
//				wp.colSet(ii, "bk_color", "background-color: rgb(0,0,0)");
//			} else {
//				wp.colSet(ii, "bk_color", "background-color: rgb(255,255,255)");
//			}
			// --font_color
//			if (eqIgno(lsServiceCode, "101") && pos("|05|95", lsCompute0035) > 0) {
//				wp.colSet(ii, "font_color", "color: rgb(255,255,255)");
//			} else 
			if (eqIgno(commString.mid(wp.colStr(ii, "card_no"), 1), "5") && pos("|79|80", lsCompute0035) > 0) {
				wp.colSet(ii, "font_color", "color: rgb(255,0,0)");
			} else if (eqIgno(wp.colStr(ii, "roc"), "1504")) {
				wp.colSet(ii, "font_color", "color: rgb(255,0,0)");
			} else if (eqIgno(wp.colStr(ii, "fallback"), "Y")) {
				wp.colSet(ii, "font_color", "color: rgb(0,0,255)");
			}

			if (wp.colNum(ii, "curr_otb_amt") < 0) {
				wp.colSet(ii, "color_otb", "color: rgb(255,0,0)");
			}
			if (wp.colNum(ii, "nt_amt") < 0) {
				wp.colSet(ii, "color_nt", "color: rgb(255,0,0)");
			}
			
			sqlSelect(sql5,new Object[] {wp.colStr(ii,"auth_unit")});
			if(sqlRowNum>0) {
				sql2wp(ii,"tt_auth_unit");
			}
			
			sqlSelect(sql6,new Object[] {wp.colStr(ii,"trans_code")});
			if(sqlRowNum>0) {
				sql2wp(ii,"tt_trans_code");				
			}	else {
				wp.colSet(ii,"tt_trans_code","其他交易");
			}
		}

		if (wp.itemEmpty("is_same") && eqIgno(wp.itemNvl("ex_same", "N"), "Y")) {
			String sql3 = " select count(*) as db_cnt from cca_auth_txlog A " + lsWhere
					+ " group by A.tx_date , A.card_no , A.mcht_no , A.auth_status_code , A.auth_no , substr(A.pos_mode,1,2) having count(*) > 1 ";

			sqlSelect(sql3, whereParameterList.toArray());
			for (int ll = 0; ll < sqlRowNum; ll++) {
				ilSame += sqlNum(ll, "db_cnt");
			}
			ilSame = ilSame - sqlRowNum;
			wp.colSet("is_same", "Y");
			wp.colSet("tl_same", ilSame);
		}
	}

	String selectSC(String aCardNo, String aAuthSeqno) {

		String sql1 = " select " + " substr(bit35_track_II,23,3) as bit35_track_II " + " from cca_auth_bitdata "
				+ " where card_no = ? " + " and auth_seqno = ? " + commSqlStr.rownum(1);

		sqlSelect(sql1, new Object[] { aCardNo, aAuthSeqno });

		if (sqlRowNum > 0) {
			return sqlStr("bit35_track_II");
		}
		return "";
	}

	@Override
	public void querySelect() throws Exception {
		cardNo = wp.itemStr("data_k1");
		txDate = wp.itemStr("data_k2");
		txTime = wp.itemStr("data_k3");
		authNo = wp.itemStr("data_k4");
		traceNo = wp.itemStr("data_k5");
		authSeqno =  wp.itemStr("data_k6");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (empty(cardNo)) {
			cardNo = wp.itemStr("card_no");
		}
		if (empty(txDate)) {
			txDate = wp.itemStr("tx_date");
		}
		if (empty(txTime)) {
			txTime = wp.itemStr("tx_time");
		}
		if (empty(authNo)) {
			authNo = wp.itemStr("auth_no");
		}
		if (empty(traceNo)) {
			traceNo = wp.itemStr("trace_no");
		}
		if (empty(authSeqno)) {
			authSeqno = wp.itemStr("auth_seqno");
		}
		wp.sqlCmd = "select A.*, " + " uf_idno_id2(A.card_no,'') as db_idno,    "
				+ " substr(A.pos_mode,1,2) as pos_mode_12,"
				// + " substr(A.pos_mode,3,1) as pos_mode_3,"
				+ " nvl((select entry_type from cca_entry_mode where entry_mode = substr(A.pos_mode,1,2) fetch first 1 rows only ),'') as pos_mode_3, "
				+ " '' as db_service_code, " + " uf_idno_name(A.id_p_seqno) as db_idno_name "
				+ ", uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del"
				+ ", (select sys_data1 from cca_sys_parm3 where sys_id = 'AUTHUNIT' and sys_key =A.auth_unit) as tt_auth_unit "
				+ ", decode(A.online_redeem,'A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)'"
				+ "'3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem "
				+ " , vdcard_flag " + " , vd_lock_nt_amt " + " , tx_seq " + " , unlock_flag " + " , mtch_flag "
				+ " , bonus_amt / 100 as bonus_amt " + " , stand_in_reason" + " , risk_type " + " , risk_score "
				+ " , ec_flag " + " , apr_user " + " , bonus_point " + " , unit_price " + " , tot_term "
				+ " , redeem_point " + " , redeem_amt / 100 as redeem_amt "
				+ " , decode(A.vdcard_flag,'C','信用卡','D','VD卡') as tt_vdcard_flag "
//        + " , installment_type "
//        + " , first_price "
				+ " , (select resp_remark from cca_resp_code where resp_code = A.auth_status_code) as resp_remark"
				+ " , nvl((select sys_data1 from cca_sys_parm3 where sys_key = A.trans_code and sys_id = 'TRANCODE'),'其他交易') as tt_trans_code "
				+ " ,TXN_IDF ,TRACE_NO ,MOD_SEQNO " 
				+ ",CURR_NT_AMT ,CURR_ORI_AMT , CURR_RATE , tx_cvc2 , A.mcht_name as mcht_chi_name "
				+ " from cca_auth_txlog A" + " where A.card_no =?"
				+ " and A.tx_date =? and A.tx_time =? and A.auth_no =? and A.trace_no =? and A.auth_seqno =? ";
		Object[] param = new Object[] { cardNo, txDate, txTime, authNo, traceNo ,authSeqno };
		this.pageSelect(param);
		if (sqlRowNum <= 0) {
			alertErr2("授權交易: not find; " + this.sqlErrtext);
		}
		readAfter();
		return;
	}

	void readAfter() {
//		String sql1a = " select mcht_name,mcht_eng_name from cca_mcht_bill where mcht_no = ? and acq_bank_id = ?  "
//				+ commSqlStr.rownum(1);
//		String sql1b = " select mcht_name,mcht_eng_name from cca_mcht_bill where mcht_no = ?  " + commSqlStr.rownum(1);
//		sqlSelect(sql1a, new Object[] { wp.colStr("mcht_no"), "" });
//		if (sqlRowNum > 0) {
//			if (empty(sqlStr("mcht_name"))) {
//				wp.colSet("mcht_chi_name", sqlStr("mcht_eng_name"));
//			} else {
//				wp.colSet("mcht_chi_name", sqlStr("mcht_name"));
//			}
//		} else {
//			sqlSelect(sql1b, new Object[] { wp.colStr("mcht_no") });
//			if (sqlRowNum > 0) {
//				if (empty(sqlStr("mcht_name"))) {
//					wp.colSet("mcht_chi_name", sqlStr("mcht_eng_name"));
//				} else {
//					wp.colSet("mcht_chi_name", sqlStr("mcht_name"));
//				}
//			} else {
//				wp.colSet("mcht_chi_name", wp.colStr("mcht_name"));
//			}
//		}
		//--
		if(wp.colEmpty("tx_seq")==false) {
			wp.colSet("crd_tx_seq", "CRD0"+commString.mid(wp.colStr("tx_seq"), 4));
		}		
		//-- VD 
		if(wp.colEq("vdcard_flag", "D")) {
			wp.colSet("curr_tot_lmt_amt", "--");
			wp.colSet("curr_otb_amt", "--");
			wp.colSet("curr_tot_std_amt", "--");
		}
		
		
	}
	
	void dataRead2() throws Exception {		
		String lsWhere = "";
		String totalShow = "" , 
//				    trFlag = "<tr>" , 
//				    trEndFlag = "</tr>" , 
//				    brFlag = "<br>" , 
				    sql1 = "" , sql2 = "" , sql3 = "" , sql4 = "" , sql5 = "" , sql6 = "" , sql7 = "" , sql8 = "";
//		String tdText = "<td class=\"td_text\" nowrap>" , 
//				    tdData = "<td class=\"td_data\" nowrap align=\"Right\" >" , 
//				    tdEnd = "</td>";
		double tlAprCnt = 0.0 , tlAprAmt = 0.0 ,tlCBCnt = 0.0, tlCBAmt = 0.0 , tlDeclineCnt =0.0 , tlDeclineAmt = 0.0 ;
		double tlPickUpCnt = 0.0 , tlPickUpAmt = 0.0 , tlExpireCardCnt = 0.0 , tlExpireCardAmt = 0.0;
		lsWhere = getWhere();
		sql1 = "select sys_key , sys_data1 from cca_sys_parm3 where sys_id = 'TRANCODE' and sys_data3 = 'Y' order by sys_key ";
		sql2 = "select count(*) as db_cnt1 , sum(A.nt_amt) as db_nt_amt1 from cca_auth_txlog A "+lsWhere + " and A.iso_resp_code in ('00','11','111','001') and A.trans_code = ? ";
		sql3 = "select count(*) as db_cnt2 , sum(A.nt_amt) as db_nt_amt2 from cca_auth_txlog A "+lsWhere + " and A.iso_resp_code in ('01','107') and A.trans_code = ? ";
		sql4 = "select count(*) as db_cnt3 , sum(A.nt_amt) as db_nt_amt3 from cca_auth_txlog A "+lsWhere + " and A.iso_resp_code not in ('00','11','111','001','01','107','07','36','38','41','43','106','200','290','54','101') and A.trans_code = ? ";
		sql5 = "select count(*) as db_cnt4 , sum(A.nt_amt) as db_nt_amt4 from cca_auth_txlog A "+lsWhere + " and A.iso_resp_code in ('07','36','38','41','43','106','200','290') and A.trans_code = ? ";
		sql6 = "select count(*) as db_cnt5 , sum(A.nt_amt) as db_nt_amt5 from cca_auth_txlog A "+lsWhere + " and A.iso_resp_code in ('54','101') and A.trans_code = ? ";
		//--身分驗證特殊處理 CV ID
		sql7 = "select count(*) as db_cnt1 , sum(A.nt_amt) as db_nt_amt1 from cca_auth_txlog A "+lsWhere + " and A.iso_resp_code in ('00','11','111','001','85') and A.trans_code = ? ";
		sql8 = "select count(*) as db_cnt3 , sum(A.nt_amt) as db_nt_amt3 from cca_auth_txlog A "+lsWhere + " and A.iso_resp_code not in ('00','11','111','001','01','107','07','36','38','41','43','106','200','290','54','101','85') and A.trans_code = ? ";
		//--讀取要顯示的 Trans_code
		sqlSelect(sql1);
		int ilRows = sqlRowNum;						
		if(ilRows<=0)	return ;				
		
		for(int ii=0;ii<ilRows;ii++) {
//			totalShow += trFlag + tdText + sqlStr(ii,"sys_data1") + tdEnd;	
			wp.colSet( ii, "sys_data1", sqlStr(ii,"sys_data1"));
			
			ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
			tmpParams.add(sqlStr(ii,"sys_key"));
			Object[] tmpObjParams = tmpParams.toArray();

			//--核准
			if(eqIgno(sqlStr(ii,"sys_key"),"CV")||eqIgno(sqlStr(ii,"sys_key"),"ID")) {	
				sqlSelect(sql7,tmpObjParams);		
				if(sqlRowNum>0) {
					tlAprCnt += sqlNum("db_cnt1");
					tlAprAmt += sqlNum("db_nt_amt1");
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_cnt1")) + tdEnd;				
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_nt_amt1"))+ tdEnd;
					
					wp.colSet(ii, "db_cnt1", String.format("%,14.0f", sqlNum("db_cnt1")));
					wp.colSet(ii, "db_nt_amt1", String.format("%,14.0f", sqlNum("db_nt_amt1")));
				}	else	{
//					totalShow += tdData + "0"+ tdEnd;				
//					totalShow += tdData + "0"+ tdEnd;
					
					wp.colSet(ii, "db_cnt1", 0);
					wp.colSet(ii, "db_nt_amt1", 0);
				}
			}	else	{
				sqlSelect(sql2,tmpObjParams);			
				if(sqlRowNum>0) {
					tlAprCnt += sqlNum("db_cnt1");
					tlAprAmt += sqlNum("db_nt_amt1");
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_cnt1")) + tdEnd;				
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_nt_amt1"))+ tdEnd;
					
					wp.colSet(ii, "db_cnt1", String.format("%,14.0f", sqlNum("db_cnt1")));
					wp.colSet(ii, "db_nt_amt1", String.format("%,14.0f", sqlNum("db_nt_amt1")));
				}	else	{
//					totalShow += tdData + "0"+ tdEnd;				
//					totalShow += tdData + "0"+ tdEnd;
					
					wp.colSet(ii, "db_cnt1", 0);
					wp.colSet(ii, "db_nt_amt1", 0);
				}
			}			
			
			//--Call Bank
			sqlSelect(sql3,tmpObjParams);			
			if(sqlRowNum>0) {
				tlCBCnt += sqlNum("db_cnt2");
				tlCBAmt += sqlNum("db_nt_amt2");
//				totalShow += tdData + String.format("%,14.0f", sqlNum("db_cnt2")) + tdEnd;				
//				totalShow += tdData + String.format("%,14.0f", sqlNum("db_nt_amt2"))+ tdEnd;
				
				wp.colSet(ii, "db_cnt2", String.format("%,14.0f", sqlNum("db_cnt2")));
				wp.colSet(ii, "db_nt_amt2", String.format("%,14.0f", sqlNum("db_nt_amt2")));
			}	else	{
//				totalShow += tdData + "0"+ tdEnd;				
//				totalShow += tdData + "0"+ tdEnd;
				
				wp.colSet(ii, "db_cnt2", 0);
				wp.colSet(ii, "db_nt_amt2", 0);
			}
			//--Decline
			if(eqIgno(sqlStr(ii,"sys_key"),"CV")||eqIgno(sqlStr(ii,"sys_key"),"ID")) {
				sqlSelect(sql8,tmpObjParams);			
				if(sqlRowNum>0) {
					tlDeclineCnt += sqlNum("db_cnt3");
					tlDeclineAmt += sqlNum("db_nt_amt3");
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_cnt3")) + tdEnd;				
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_nt_amt3"))+ tdEnd;
					
					wp.colSet(ii, "db_cnt3", String.format("%,14.0f", sqlNum("db_cnt3")));
					wp.colSet(ii, "db_nt_amt3", String.format("%,14.0f", sqlNum("db_nt_amt3")));
				}	else	{
//					totalShow += tdData + "0"+ tdEnd;				
//					totalShow += tdData + "0"+ tdEnd;
					
					wp.colSet(ii, "db_cnt3", 0);
					wp.colSet(ii, "db_nt_amt3", 0);
				}
			}	else	{
				sqlSelect(sql4,tmpObjParams);			
				if(sqlRowNum>0) {
					tlDeclineCnt += sqlNum("db_cnt3");
					tlDeclineAmt += sqlNum("db_nt_amt3");
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_cnt3")) + tdEnd;				
//					totalShow += tdData + String.format("%,14.0f", sqlNum("db_nt_amt3"))+ tdEnd;
					
					wp.colSet(ii, "db_cnt3", String.format("%,14.0f", sqlNum("db_cnt3")));
					wp.colSet(ii, "db_nt_amt3", String.format("%,14.0f", sqlNum("db_nt_amt3")));
				}	else	{
//					totalShow += tdData + "0"+ tdEnd;				
//					totalShow += tdData + "0"+ tdEnd;
					
					wp.colSet(ii, "db_cnt3", 0);
					wp.colSet(ii, "db_nt_amt3", 0);
				}
			}			
			
			//--Pick Up
			sqlSelect(sql5,tmpObjParams);			
			if(sqlRowNum>0) {				
				tlPickUpCnt += sqlNum("db_cnt4");
				tlPickUpAmt += sqlNum("db_nt_amt4");
//				totalShow += tdData + String.format("%,14.0f", sqlNum("db_cnt4")) + tdEnd;				
//				totalShow += tdData + String.format("%,14.0f", sqlNum("db_nt_amt4"))+ tdEnd;
				
				wp.colSet(ii, "db_cnt4", String.format("%,14.0f", sqlNum("db_cnt4")));
				wp.colSet(ii, "db_nt_amt4", String.format("%,14.0f", sqlNum("db_nt_amt4")));
			}	else	{
//				totalShow += tdData + "0"+ tdEnd;				
//				totalShow += tdData + "0"+ tdEnd;
				
				wp.colSet(ii, "db_cnt4", 0);
				wp.colSet(ii, "db_nt_amt4", 0);
			}
			
			//--Expire Card
			sqlSelect(sql6,tmpObjParams);			
			if(sqlRowNum>0) {				
				tlExpireCardCnt += sqlNum("db_cnt5");
				tlExpireCardAmt += sqlNum("db_nt_amt5");
//				totalShow += tdData + String.format("%,14.0f", sqlNum("db_cnt5")) + tdEnd;				
//				totalShow += tdData + String.format("%,14.0f", sqlNum("db_nt_amt5"))+ tdEnd;
				
				wp.colSet(ii, "db_cnt5", String.format("%,14.0f", sqlNum("db_cnt5")));
				wp.colSet(ii, "db_nt_amt5", String.format("%,14.0f", sqlNum("db_nt_amt5")));
			}	else	{
//				totalShow += tdData + "0"+ tdEnd;				
//				totalShow += tdData + "0"+ tdEnd;
				
				wp.colSet(ii, "db_cnt5", 0);
				wp.colSet(ii, "db_nt_amt5", 0);
			}
//			totalShow += trEndFlag;
		}
		wp.selectCnt =ilRows;
		wp.setListCount(0);
		
		//--合計
//		totalShow += trFlag + tdText + "總計" + tdEnd;
//		totalShow += tdData + String.format("%,14.0f", tlAprCnt) + tdEnd;				
//		totalShow += tdData + String.format("%,14.0f", tlAprAmt)+ tdEnd;
//		totalShow += tdData + String.format("%,14.0f", tlCBCnt) + tdEnd;				
//		totalShow += tdData + String.format("%,14.0f", tlCBAmt)+ tdEnd;
//		totalShow += tdData + String.format("%,14.0f", tlDeclineCnt) + tdEnd;				
//		totalShow += tdData + String.format("%,14.0f", tlDeclineAmt)+ tdEnd;
//		totalShow += tdData + String.format("%,14.0f", tlPickUpCnt) + tdEnd;				
//		totalShow += tdData + String.format("%,14.0f", tlPickUpAmt)+ tdEnd;
//		totalShow += tdData + String.format("%,14.0f", tlExpireCardCnt) + tdEnd;				
//		totalShow += tdData + String.format("%,14.0f", tlExpireCardAmt)+ tdEnd;
		
		wp.colSet("tlAprCnt", String.format("%,14.0f", tlAprCnt));
		wp.colSet("tlAprAmt", String.format("%,14.0f", tlAprAmt));
		wp.colSet("tlCBCnt", String.format("%,14.0f", tlCBCnt));
		wp.colSet("tlCBAmt", String.format("%,14.0f", tlCBAmt));
		wp.colSet("tlDeclineCnt", String.format("%,14.0f", tlDeclineCnt));
		wp.colSet("tlDeclineAmt", String.format("%,14.0f", tlDeclineAmt));
		wp.colSet("tlPickUpCnt", String.format("%,14.0f", tlPickUpCnt));
		wp.colSet("tlPickUpAmt", String.format("%,14.0f", tlPickUpAmt));
		wp.colSet("tlExpireCardCnt", String.format("%,14.0f", tlExpireCardCnt));
		wp.colSet("tlExpireCardAmt", String.format("%,14.0f", tlExpireCardAmt));
		
//		totalShow += trEndFlag;
		wp.colSet("total_show", totalShow);
	}
	
//	void dataRead2() {
//		getWhere();
//		wp.pageRows = 999;
//
//		wp.sqlCmd = " select A.logic_del , count(*) as db_cnt , sum(A.nt_amt) as db_nt_amt from cca_auth_txlog A "
//				+ lsWhere + " and iso_resp_code in ('00','11','111','001') group by logic_del ";
//
//		pageQuery();
//
//		if (sqlRowNum <= 0) {
//			selectOK();
//		}
//
//		int ilSelectCnt = 0, ilCntSum = 0, ilNtAmtSum = 0;
//		ilSelectCnt = sqlRowNum;
//		String lsLogicWhere = "";
//		// -- '0','M','C','B','J','D','A'
//		for (int ii = 0; ii < ilSelectCnt; ii++) {
//			if (wp.colEq(ii, "logic_del", "0")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt01", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt01", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "M")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt02", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt02", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "C")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt03", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt03", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "B")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt04", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt04", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "J")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt05", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt05", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "D")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt06", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt06", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "A")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt07", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt07", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "W")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt08", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt08", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "X")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt09", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt09", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "Y")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt10", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt10", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "F")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt11", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt11", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "R")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt12", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt12", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "V")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt13", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt13", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "Z")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt14", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt14", wp.colNum(ii, "db_nt_amt"));
//			} else if (wp.colEq(ii, "logic_del", "x")) {
//				ilCntSum += wp.colNum(ii, "db_cnt");
//				ilNtAmtSum += wp.colNum(ii, "db_nt_amt");
//				wp.colSet("wk_cnt18", wp.colNum(ii, "db_cnt"));
//				wp.colSet("wk_amt18", wp.colNum(ii, "db_nt_amt"));
//			}
//			/*
//			 * else if(wp.col_eq(ii,"logic_del", "Y")){ il_cnt_sum +=
//			 * wp.col_num(ii,"db_cnt"); il_nt_amt_sum += wp.col_num(ii,"db_nt_amt");
//			 * wp.col_set("wk_cnt15", wp.col_num(ii,"db_cnt")); wp.col_set("wk_amt15",
//			 * wp.col_num(ii,"db_nt_amt")); } else if(wp.col_eq(ii,"logic_del", "N")){
//			 * il_cnt_sum += wp.col_num(ii,"db_cnt"); il_nt_amt_sum +=
//			 * wp.col_num(ii,"db_nt_amt"); wp.col_set("wk_cnt16", wp.col_num(ii,"db_cnt"));
//			 * wp.col_set("wk_amt16", wp.col_num(ii,"db_nt_amt")); } else
//			 * if(wp.col_eq(ii,"logic_del", "U")){ il_cnt_sum += wp.col_num(ii,"db_cnt");
//			 * il_nt_amt_sum += wp.col_num(ii,"db_nt_amt"); wp.col_set("wk_cnt17",
//			 * wp.col_num(ii,"db_cnt")); wp.col_set("wk_amt17", wp.col_num(ii,"db_nt_amt"));
//			 * }
//			 */
//		}
//
//		wp.sqlCmd = " select A.mtch_flag , count(*) as db_cnt , sum(A.nt_amt) as db_nt_amt from cca_auth_txlog A "
//				+ lsWhere + " and iso_resp_code in ('00','11','111','001') "
//				+ " and logic_del not in ('0','M','C','B','J','D','A','W','X','Y','F','R','V','Z','x') "
//				+ " group by mtch_flag ";
//
//		pageQuery();
//
//		if (sqlRowNum <= 0) {
//			selectOK();
//		}
//		int ilSelectCnt1 = 0;
//
//		ilSelectCnt1 = sqlRowNum;
//
//		for (int xx = 0; xx < ilSelectCnt1; xx++) {
//			if (wp.colEq(xx, "mtch_flag", "Y")) {
//				ilCntSum += wp.colNum(xx, "db_cnt");
//				ilNtAmtSum += wp.colNum(xx, "db_nt_amt");
//				wp.colSet("wk_cnt15", wp.colNum(xx, "db_cnt"));
//				wp.colSet("wk_amt15", wp.colNum(xx, "db_nt_amt"));
//			} else if (wp.colEq(xx, "mtch_flag", "N")) {
//				ilCntSum += wp.colNum(xx, "db_cnt");
//				ilNtAmtSum += wp.colNum(xx, "db_nt_amt");
//				wp.colSet("wk_cnt16", wp.colNum(xx, "db_cnt"));
//				wp.colSet("wk_amt16", wp.colNum(xx, "db_nt_amt"));
//			}
//		}
//
//		wp.colSet("wk_cnt_tot", ilCntSum);
//		wp.colSet("wk_amt_tot", ilNtAmtSum);
//
//		wp.sqlCmd = " select A.logic_del , count(*) as db_cnt , sum(A.nt_amt) as db_nt_amt from cca_auth_txlog A "
//				+ lsWhere + " and iso_resp_code in ('01','107') group by logic_del ";
//
//		pageQuery();
//
//		if (sqlRowNum <= 0) {
//			selectOK();
//		}
//
//		int ilSelectCnt2 = 0;
//		ilSelectCnt2 = sqlRowNum;
//
//		for (int ll = 0; ll < ilSelectCnt2; ll++) {
//			if (wp.colEq(ll, "logic_del", "0")) {
//				wp.colSet("wk_01callbank_cnt", wp.colNum(ll, "db_cnt"));
//				wp.colSet("wk_01callback_amt", wp.colNum(ll, "db_nt_amt"));
//			} else if (wp.colEq(ll, "logic_del", "M")) {
//				wp.colSet("wk_02callback_cnt", wp.colNum(ll, "db_cnt"));
//				wp.colSet("wk_02callback_amt", wp.colNum(ll, "db_nt_amt"));
//			} else if (wp.colEq(ll, "logic_del", "C")) {
//				wp.colSet("wk_03callback_cnt", wp.colNum(ll, "db_cnt"));
//				wp.colSet("wk_03callback_amt", wp.colNum(ll, "db_nt_amt"));
//			}
//		}
//		wp.sqlCmd = " select A.logic_del , count(*) as db_cnt , sum(A.nt_amt) as db_nt_amt from cca_auth_txlog A "
//				+ lsWhere
//				+ "and iso_resp_code not in ('00','11','111','001','01','107','07','36','38','41','43','106','200','290','54','101') "
////				+ " and iso_resp_code in ('03','05','06','08','13','14','55','57','89','96','O5','100','110','111','117','121','183','189','911','912') "
//				+ " group by logic_del "				
//				;
//
//		pageQuery();
//
//		if (sqlRowNum <= 0) {
//			selectOK();
//		}
//
//		int ilSelectCnt3 = 0;
//		ilSelectCnt3 = sqlRowNum;
//
//		for (int aa = 0; aa < ilSelectCnt3; aa++) {
//			if (wp.colEq(aa, "logic_del", "0")) {
//				wp.colSet("wk_01decline_cnt", wp.colNum(aa, "db_cnt"));
//				wp.colSet("wk_01decline_amt", wp.colNum(aa, "db_nt_amt"));
//			} else if (wp.colEq(aa, "logic_del", "M")) {
//				wp.colSet("wk_02decline_cnt", wp.colNum(aa, "db_cnt"));
//				wp.colSet("wk_02decline_amt", wp.colNum(aa, "db_nt_amt"));
//			} else if (wp.colEq(aa, "logic_del", "C")) {
//				wp.colSet("wk_03decline_cnt", wp.colNum(aa, "db_cnt"));
//				wp.colSet("wk_03decline_amt", wp.colNum(aa, "db_nt_amt"));
//			}
//		}
//
//		wp.sqlCmd = " select A.logic_del , count(*) as db_cnt , sum(A.nt_amt) as db_nt_amt from cca_auth_txlog A "
//				+ lsWhere + " and iso_resp_code in ('07','36','38','41','43','106','200','290') group by logic_del ";
//
//		pageQuery();
//
//		if (sqlRowNum <= 0) {
//			selectOK();
//		}
//
//		int ilSelectCnt4 = 0;
//		ilSelectCnt4 = sqlRowNum;
//
//		for (int bb = 0; bb < ilSelectCnt4; bb++) {
//			if (wp.colEq(bb, "logic_del", "0")) {
//				wp.colSet("wk_01pickup_cnt", wp.colNum(bb, "db_cnt"));
//				wp.colSet("wk_01pickup_amt", wp.colNum(bb, "db_nt_amt"));
//			} else if (wp.colEq(bb, "logic_del", "M")) {
//				wp.colSet("wk_02pickup_cnt", wp.colNum(bb, "db_cnt"));
//				wp.colSet("wk_02pickup_amt", wp.colNum(bb, "db_nt_amt"));
//			} else if (wp.colEq(bb, "logic_del", "C")) {
//				wp.colSet("wk_03pickup_cnt", wp.colNum(bb, "db_cnt"));
//				wp.colSet("wk_03pickup_amt", wp.colNum(bb, "db_nt_amt"));
//			}
//		}
//
//		wp.sqlCmd = " select A.logic_del , count(*) as db_cnt , sum(A.nt_amt) as db_nt_amt from cca_auth_txlog A "
//				+ lsWhere + " and iso_resp_code in ('54','101') group by logic_del ";
//
//		pageQuery();
//
//		if (sqlRowNum <= 0) {
//			selectOK();
//		}
//
//		int ilSelectCnt5 = 0;
//		ilSelectCnt5 = sqlRowNum;
//
//		for (int cc = 0; cc < ilSelectCnt5; cc++) {
//			if (wp.colEq(cc, "logic_del", "0")) {
//				wp.colSet("wk_01expcard_cnt", wp.colNum(cc, "db_cnt"));
//				wp.colSet("wk_01expcard_amt", wp.colNum(cc, "db_nt_amt"));
//			} else if (wp.colEq(cc, "logic_del", "M")) {
//				wp.colSet("wk_02expcard_cnt", wp.colNum(cc, "db_cnt"));
//				wp.colSet("wk_02expcard_amt", wp.colNum(cc, "db_nt_amt"));
//			} else if (wp.colEq(cc, "logic_del", "C")) {
//				wp.colSet("wk_03expcard_cnt", wp.colNum(cc, "db_cnt"));
//				wp.colSet("wk_03expcard_amt", wp.colNum(cc, "db_nt_amt"));
//			}
//		}
//
//	}

	@Override
	public void dddwSelect() {
		try {
			wp.optionKey = wp.colStr("ex_entry_type");
			dddwList("dddw_entry_type", "select distinct entry_type as db_code , "
					+ "entry_type as db_desc from cca_entry_mode where 1=1 ");

			wp.optionKey = wp.colStr("ex_entry_mode");
			dddwList("dddw_entry_mode", "select distinct entry_mode as db_code , "
					+ "entry_mode as db_desc from cca_entry_mode where 1=1 ");

			wp.optionKey = wp.colStr("ex_trans_code");
			dddwList("dddw_trans_code", "cca_sys_parm3", "sys_key", "sys_data1", " where sys_id = 'TRANCODE' ");

		} catch (Exception ex) {
		}

	}

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = "ccaq1032";
			String date = "";
			date = "日期(起):" + commString.strToYmd(wp.itemStr("ex_tx_date1")) + " 日期(迄):"
					+ commString.strToYmd(wp.itemStr("ex_tx_date2"));
			wp.colSet("cond1", date);
			wp.colSet("report_user", wp.loginUser);
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
			xlsx.excelTemplate = "ccaq1032.xlsx";
			wp.pageRows = 9999;
			queryFunc();
			// wp.setListCount(1);
			xlsx.processExcelSheet(wp);
			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");
		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}

	}

	@Override
	public void logOnlineApprove() throws Exception {
		// TODO Auto-generated method stub

	}

}
