package ccam01;
/** 卡戶交易記錄查詢(auth_txlog)
 * 19-0611:    JH    p_xxx >>acno_p_xxx
 * V.2018-1023.jh
 *109-04-19    shiyuqi       updated for project coding standard 
 *109-12-22    Justin         concat sql -> parameterize sql
 *109-01-06    Justin         updated for XSS
 */

import java.util.Arrays;

public class Ccaq1032Func extends busi.FuncAction {

  busi.DataSet dsLog = null;
  busi.DataSet dsEntryMode = null;
  int ilSame = 0;
  String isEntryType = "";
  public int iiCnt = 0;
  // taroko.com.TarokoCommon wp;

  // public Ccaq1030Func(TarokoCommon wr) {
  // wp = wr;
  // this.conn = wp.getConn();
  // }

  @Override
  public int querySelect() {
    dsLog = new busi.DataSet();

    String lsWhere = "";
//    lsWhere += commSqlStr.col(wp.itemStr("ex_card_no"), "card_no", "like%");
    

    if (wp.itemEmpty("ex_idno") == false) {
      this.daoTid = "wk.";
      strSql = "select uf_idno_pseqno(?) as id_pseqno" 
          + ", uf_vd_idno_pseqno(?) as id_pseqno2"
          + " from " 
          + this.sqlDual;
      this.sqlSelect(strSql, new Object[] {wp.itemStr("ex_idno"), wp.itemStr("ex_idno")});
      if (sqlRowNum <= 0) {
        errmsg("卡人ID: 不存在");
        return -1;
      }
      if (this.colEmpty("wp.id_pseqno") && colEmpty("wk.id_pseqno2")) {
        errmsg("卡人ID: 不存在");
        return -1;
      }
      lsWhere += " and A.id_p_seqno in ('? ,? )";
      setString(colStr("wk.id_pseqno"));
      setString(colStr("wk.id_pseqno2"));
    }
    
    if (wp.itemEmpty("ex_card_no")) {
    	lsWhere += " and card_no like ? ";
    	setString(wp.itemStr("ex_card_no"));
	}

    if (wp.itemEmpty("ex_tx_time1") && wp.itemEmpty("ex_tx_time2")) {
		if (wp.itemEmpty("ex_tx_date1")) {
			lsWhere += " and A.tx_date >= ? ";
			setString(wp.itemStr("ex_tx_date1"));
		}
		if (wp.itemEmpty("ex_tx_date2")) {
			lsWhere += " and A.tx_date <= ? ";
			setString(wp.itemStr("ex_tx_date2"));
		}
//      lsWhere += commSqlStr.strend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2"), "A.tx_date");
    } else {
    	if (wp.itemEmpty("ex_tx_date1")) {
			lsWhere += " and A.tx_date >= ? ";
			setString(wp.itemStr("ex_tx_date1"));
		}
		if (wp.itemEmpty("ex_tx_date2")) {
			lsWhere += " and A.tx_date <= ? ";
			setString(wp.itemStr("ex_tx_date2"));
		}
//      lsWhere += commSqlStr.strend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2"), "A.tx_date");
		if (wp.itemEmpty("ex_tx_time1")) {
			lsWhere += " and A.tx_time >= ? ";
			setString(wp.itemStr("ex_tx_time1"));
		}
		if (wp.itemEmpty("ex_tx_time2")) {
			lsWhere += " and A.tx_time <= ? ";
			setString(wp.itemStr("ex_tx_time2"));
		}
//      lsWhere += commSqlStr.strend(wp.itemStr("ex_tx_time1"), wp.itemStr("ex_tx_time2"), "A.tx_time");
    }

    strSql = "select A.auth_seqno " + ", A.card_no " + ", A.tx_date " + ", A.tx_time "
        + ", A.eff_date_end " + ", A.mcht_no " + ", A.mcht_name " + ", A.mcc_code "
        + ", A.pos_mode " + ", substr(A.pos_mode,1,2) as pos_mode_1_2 "
        + ", substr(A.pos_mode,3,1) as pos_mode_3 " + ", A.nt_amt " + ", A.consume_country "
        + ", A.tx_currency " + ", A.iso_resp_code " + ", A.auth_status_code " + ", A.iso_adj_code "
        + ", A.auth_no " + ", A.auth_user " + ", A.vip_code " + ", A.stand_in " + ", A.class_code "
        + ", A.auth_unit " + ", A.logic_del " + ", A.auth_remark " + ", A.trans_type "
        + ", uf_idno_id2(A.card_no,'') as id_no " + ", uf_idno_name(A.id_p_seqno) as db_idno_name "
        + ", A.curr_otb_amt " + ", A.curr_tot_lmt_amt " + ", A.curr_tot_std_amt "
        + ", A.curr_tot_tx_amt " + ", A.curr_tot_cash_amt " + ", A.curr_tot_unpaid "
        + ", A.fallback " + ", A.roc " + ", A.ibm_bit39_code " + ", A.ibm_bit33_code "
        + ", A.ec_ind " + ", A.ucaf " + ", A.mtch_flag " + ", A.ec_flag "
        + ", uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del " + ", A.v_card_no "
        + ", A.online_redeem "
        // + ", (select mcht_name from cca_mcht_bill where mcht_no =A.mcht_no fetch first 1 rows
        // only) as mcht_chi_name "
        // + ", (select sys_data1 from cca_sys_parm3 where sys_id = 'AUTHUNIT' and sys_key
        // =A.auth_unit) as tt_auth_unit "
        + ", uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit"
        + ", decode(A.online_redeem,'','','A','分期 (A)','I','分期 (I)','E','分期 (E)','Z','分期 (Z)','0','紅利 (0)','1','紅利 (1)','2','紅利 (2)'"
        + "'3','紅利 (3)','4','紅利 (4)','5','紅利 (5)','6','紅利 (6)','7','紅利 (7)','') as tt_online_redeem "
        + ", decode(curr_tot_std_amt,0,0,((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)) * 100 as cond_curr_rate "
        + " from cca_auth_txlog A" + " where 1=1" + lsWhere
        + " order by A.tx_date desc, A.tx_time Desc" + commSqlStr.rownum(1000);

    dsLog.colList = this.sqlQuery(strSql, null);
    wp.selectCnt = 0;
    iiCnt = dsLog.listRows();

    for (int ll = 0; ll < dsLog.listRows(); ll++) {
      wp.logSql = false;

      dsLog.listToCol(ll);
      if (condFilter() != 1) {
        continue;
      }
      wp.selectCnt++;
      dataQueryList();
      setDataTab2();
    }
    wp.logSql = true;

    wp.colSet("tl_same", ilSame);
    wp.colSet("tl_tx_cnt", wp.selectCnt + " (可能大於1000筆資料)");

    wp.listCount[0] = wp.selectCnt;
    return 1;
  }

  int condFilter() {
    isEntryType = "";
    if (eqIgno(wp.itemNvl("ex_chk_vip", "N"), "Y")) {
      if (empty(dsLog.colStr("vip_code")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_mcht_no"))) {
      if (!eqIgno(wp.itemStr("ex_mcht_no"), dsLog.colStr("mcht_no")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_mcc_code"))) {
      if (!eqIgno(wp.itemStr("ex_mcc_code"), dsLog.colStr("mcc_code")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_entry_mode"))) {
      if (dsLog.colStr("pos_mode_1_2").indexOf(wp.itemStr("ex_entry_mode")) != 0)
        return -1;
    }

    if (!empty(wp.itemStr("ex_stand_in"))) {
      if (!eqIgno(wp.itemStr("ex_stand_in"), dsLog.colStr("stand_in")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_excl_mcc1"))) {
      if (eqIgno(wp.itemStr("ex_excl_mcc1"), dsLog.colStr("mcc_code")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_excl_mcc2"))) {
      if (eqIgno(wp.itemStr("ex_excl_mcc2"), dsLog.colStr("mcc_code")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_excl_mcc3"))) {
      if (eqIgno(wp.itemStr("ex_excl_mcc3"), dsLog.colStr("mcc_code")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_country"))) {
      if (!eqIgno(wp.itemStr("ex_country"), dsLog.colStr("consume_country")))
        return -1;
      // if(ds_log.col_ss("consume_country").indexOf(wp.item_ss("ex_country"))==0) return -1;
    }

    if (eqIgno(wp.itemNvl("ex_no_tw", "N"), "Y")) {
      if (eqIgno(dsLog.colStr("consume_country"), "TW"))
        return -1;
    }

    if (!empty(wp.itemStr("ex_auth_status"))) {
      if (!eqIgno(wp.itemStr("ex_auth_status"), dsLog.colStr("auth_status_code")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_iso_resp_code"))) {
      if (!eqIgno(wp.itemStr("ex_iso_resp_code"), dsLog.colStr("iso_resp_code")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_auth_user"))) {
      if (!eqIgno(wp.itemStr("ex_auth_user"), dsLog.colStr("auth_user")))
        return -1;
    }

    if (!wp.itemEmpty("ex_ibm_resp1")) {
      if (!eqIgno(wp.itemStr("ex_ibm_resp1"), dsLog.colStr("ibm_bit39_code")))
        return -1;
    }

    if (!wp.itemEmpty("ex_ibm_resp2")) {
      if (!eqIgno(wp.itemStr("ex_ibm_resp2"), dsLog.colStr("ibm_bit33_code")))
        return -1;
    }

    if (!wp.itemEmpty("ex_eci")) {
      if (!eqIgno(wp.itemStr("ex_eci"), dsLog.colStr("ec_ind")))
        return -1;
    }

    if (!wp.itemEmpty("ex_ucaf")) {
      if (!eqIgno(wp.itemStr("ex_ucaf"), dsLog.colStr("ucaf")))
        return -1;
    }

    if (!wp.itemEmpty("ex_curr_rate")) {
      if (wp.itemNum("ex_curr_rate") > dsLog.colNum("cond_curr_rate"))
        return -1;
    }

    if (!wp.itemEmpty("ex_std_amt")) {
      if (wp.itemNum("ex_std_amt") * 10000 > dsLog.colNum("curr_tot_std_amt"))
        return -1;
    }

    if (wp.itemNum("ex_amt1") != 0) {
      if (wp.itemNum("ex_amt1") == wp.itemNum("ex_amt2")) {
        if (wp.itemNum("ex_amt1") != dsLog.colNum("nt_amt"))
          return -1;
      } else {
        if (wp.itemNum("ex_amt1") > dsLog.colNum("nt_amt"))
          return -1;
      }
    }

    if (wp.itemNum("ex_amt2") != 0) {
      if (wp.itemNum("ex_amt1") == wp.itemNum("ex_amt2")) {
        if (wp.itemNum("ex_amt2") != dsLog.colNum("nt_amt"))
          return -1;
      } else {
        if (wp.itemNum("ex_amt2") < dsLog.colNum("nt_amt"))
          return -1;
      }
    }

    if (!wp.itemEmpty("ex_amt1")) {
      if ((wp.itemNum("ex_amt1") >= dsLog.colNum("nt_amt")) == false) {
        return -1;
      }
    }

    if (!wp.itemEmpty("ex_amt2")) {
      if ((wp.itemNum("ex_amt2") <= dsLog.colNum("nt_amt")) == false) {
        return -1;
      }
    }

    // --行員 金控
    if (eqIgno(wp.itemNvl("ex_emp_bank", "N"), "Y") || eqIgno(wp.itemNvl("ex_emp_fhc", "N"), "Y")) {
      if (checkBankFhc(dsLog.colStr("id_no")) == -1)
        return -1;
    }

    // --ck box

    String lsCkBox = "";
    if (eqIgno(wp.itemNvl("ex_chk_01", "N"), "Y")) { // 人工授權
      if (!eqIgno("K", dsLog.colStr("auth_unit")))
        return -1;
    }

    if (eqIgno(wp.itemNvl("ex_chk_02", "N"), "Y")) { // 自動授權
      if (eqIgno("K", dsLog.colStr("auth_unit")))
        return -1;
    } else {
      if (eqIgno(wp.itemNvl("ex_chk_03", "N"), "Y")) {
        lsCkBox += "|V";
      }
      if (eqIgno(wp.itemNvl("ex_chk_04", "N"), "Y")) {
        lsCkBox += "|M-";
      }
      if (eqIgno(wp.itemNvl("ex_chk_05", "N"), "Y")) {
        lsCkBox += "|N";
      }
      if (eqIgno(wp.itemNvl("ex_chk_06", "N"), "Y")) {
        lsCkBox += "|A";
      }
      if (eqIgno(wp.itemNvl("ex_chk_08", "N"), "Y")) {
        lsCkBox += "|J";
      }
      if (eqIgno(wp.itemNvl("ex_chk_06", "N"), "Y")) {
        lsCkBox += "|A";
      }
      if (eqIgno(wp.itemNvl("ex_chk_nccc", "N"), "Y")) {
        lsCkBox += "|MP";
      }
      if (eqIgno(wp.itemNvl("ex_chk_acq", "N"), "Y")) {
        lsCkBox += "|C";
      }
      if (eqIgno(wp.itemNvl("ex_chk_tscc", "N"), "Y")) {
        lsCkBox += "|T";
      }
      // if(eq_igno(wp.item_nvl("ex_chk_fback", "N"),"Y")){
      // ls_ck_box += "|Y";
      // }

    }
    String lsUnit = dsLog.colStr("auth_unit");
    if (eqIgno(lsUnit, "M"))
      lsUnit = "M-";
    if (!empty(lsCkBox)) {
      if (pos(lsCkBox, lsUnit) < 0)
        return -1;

    }

    if (eqIgno(wp.itemNvl("ex_chk_hce", "N"), "Y")) {
      if (empty(dsLog.colStr("v_card_no")))
        return -1;
    }

    if (!empty(wp.itemStr("ex_class_code"))) {
      if (!eqIgno(wp.itemStr("ex_class_code"), dsLog.colStr("class_code")))
        return -1;
    }

    //
    // ss =trim(em_curr_rate.text)
    // if ss='' then ss="0"
    // lm_num =dec(ss)
    // if lm_num>0 then
    // sqlcmd +=" and
    // decode(nvl(curr_tot_std_amt,0),0,0,((nvl(curr_tot_unpaid,0)+decode(cacu_amount,'Y',nt_amt,0))
    // / curr_tot_std_amt)) * 100 > "+ss
    // end if
    //

    if (wp.itemNum("ex_std_amt") != 0) {
      if (dsLog.colNum("curr_tot_std_amt") <= (wp.itemNum("ex_std_amt") * 10000))
        return -1;
    }

    if (eqIgno(wp.itemNvl("ex_chk_fback", "N"), "Y")) {
      if (!eqIgno(dsLog.colStr("fallback"), "Y"))
        return -1;
    }

    isEntryType = entryType(dsLog.colStr("pos_mode_1_2"));

    if (!wp.itemEmpty("ex_entry_type")) {
      if (!wp.itemEq("ex_entry_type", isEntryType))
        return -1;
    }

    return 1;
  }

  String entryType(String aEntryMode) {
    if (dsEntryMode == null) {
      dsEntryMode = new busi.DataSet();
      String sql1 = "select entry_type, entry_mode from cca_entry_mode where 1=1"; // entry_mode = ?
                                                                                   // ";
      dsEntryMode.colList = sqlQuery(sql1, null);
    }
    for (int ii = 0; ii < dsEntryMode.listRows(); ii++) {
      if (eqIgno(aEntryMode, dsEntryMode.listStr(ii, "entry_mode"))) {
        return dsEntryMode.listStr(ii, "entry_type");
      }
    }
    return "";
  }

  int checkBankFhc(String lsIdno) {
    String lsEmpBank = "", lsEmpFhc = "";
    lsEmpBank = wp.itemNvl("ex_emp_bank", "N");
    lsEmpFhc = wp.itemNvl("ex_emp_fhc", "N");

    String sql1 = " select " + " count(*) as db_cntBF " 
        + " from ecs_employee "
        + " where id_no = ? " 
        + " and status_id = '1' ";

    if (eqIgno(lsEmpBank, "Y") && eqIgno(lsEmpFhc, "N"))
      sql1 += " and data_type = '1' ";
    if (eqIgno(lsEmpBank, "N") && eqIgno(lsEmpFhc, "Y"))
      sql1 += " and data_type = '2' ";
    sqlSelect(sql1, new Object[] {lsIdno});

    if (colNum("db_cntBF") > 0)
      return 1;

    return -1;
  }

  String selectSC(String aCardNo, String aAuthSeqno) {

    String sql1 =
        " select " + " substr(bit35_track_II,23,3) as bit35_track_II " + " from cca_auth_bitdata "
            + " where card_no = ? " + " and auth_seqno = ? " + commSqlStr.rownum(1);

    sqlSelect(sql1, new Object[] {aCardNo, aAuthSeqno});

    if (sqlRowNum > 0) {
      return colStr("bit35_track_II");
    }
    return "";
  }

  void dataQueryList() {
    int ll = (wp.selectCnt - 1);
    String lsCompute0035 = "", lsServiceCode = "";

    if (eqIgno(wp.itemNvl("ex_same", "N"), "Y")) {
      if (checkSame(dsLog.colStr("tx_date"), dsLog.colStr("card_no"), dsLog.colStr("mcht_no"),
          dsLog.colStr("iso_resp_code"), dsLog.colStr("auth_no"),
          dsLog.colStr("pos_mode_1_2")) == 1) {
        wp.colSet(ll, "wk_same", "*");
        ilSame++;
      }
    }
    // --wk_color
    if (!eqIgno(dsLog.colStr("mtch_flag"), "Y") && !eqIgno(dsLog.colStr("logic_del"), "x")
        && !eqIgno(dsLog.colStr("logic_del"), "B")) {
      wp.colSet(ll, "wk_color", "yellow");
    }
    lsServiceCode = selectSC(dsLog.colStr("card_no"), dsLog.colStr("auth_seqno"));
    lsCompute0035 = dsLog.colStr("pos_mode_1_2");
    // --bk_color
    if (eqIgno(lsServiceCode, "101")
        && (eqIgno(lsCompute0035, "05") || eqIgno(lsCompute0035, "95"))) {
      wp.colSet(ll, "bk_color", "background-color: rgb(0,0,0)");
    } else {
      wp.colSet(ll, "bk_color", "background-color: rgb(255,255,255)");
    }
    // --font_color
    if (eqIgno(lsServiceCode, "101") && pos("|05|95", lsCompute0035) > 0) {
      wp.colSet(ll, "font_color", "color: rgb(255,255,255)");
    } else if (eqIgno(commString.mid(dsLog.colStr("card_no"), 1), "5")
        && pos("|79|80", lsCompute0035) > 0) {
      wp.colSet(ll, "font_color", "color: rgb(255,0,0)");
    } else if (eqIgno(dsLog.colStr("roc"), "1504")) {
      wp.colSet(ll, "font_color", "color: rgb(255,0,0)");
    } else if (eqIgno(dsLog.colStr("fallback"), "Y")) {
      wp.colSet(ll, "font_color", "color: rgb(0,0,255)");
    }

    if (dsLog.colNum("curr_otb_amt") < 0) {
      wp.colSet(ll, "color_otb", "color: rgb(255,0,0)");
    }
    if (dsLog.colNum("nt_amt") < 0) {
      wp.colSet(ll, "color_nt", "color: rgb(255,0,0)");
    }

    wp.colSet(ll, "card_no", dsLog.colStr("card_no"));
    wp.colSet(ll, "eff_date_end", dsLog.colStr("eff_date_end"));
    wp.colSet(ll, "tx_date", dsLog.colStr("tx_date"));
    wp.colSet(ll, "tx_time", dsLog.colStr("tx_time"));
    wp.colSet(ll, "mcht_no", dsLog.colStr("mcht_no"));
    wp.colSet(ll, "mcht_name", dsLog.colStr("mcht_name"));
    wp.colSet(ll, "mcc_code", dsLog.colStr("mcc_code"));
    wp.colSet(ll, "pos_mod_1_2", dsLog.colStr("pos_mode_1_2"));
    wp.colSet(ll, "db_entry_mode_type", isEntryType);
    wp.colSet(ll, "nt_amt", dsLog.colStr("nt_amt"));
    wp.colSet(ll, "consume_country", dsLog.colStr("consume_country"));
    wp.colSet(ll, "tx_currency", dsLog.colStr("tx_currency"));
    wp.colSet(ll, "iso_resp_code", dsLog.colStr("iso_resp_code"));
    wp.colSet(ll, "auth_status_code", dsLog.colStr("auth_status_code"));
    wp.colSet(ll, "iso_adj_code", dsLog.colStr("iso_adj_code"));
    wp.colSet(ll, "auth_no", dsLog.colStr("auth_no"));
    wp.colSet(ll, "auth_user", dsLog.colStr("auth_user"));
    wp.colSet(ll, "vip_code", dsLog.colStr("vip_code"));
    wp.colSet(ll, "db_idno_name", dsLog.colStr("db_idno_name"));
    wp.colSet(ll, "stand_in", dsLog.colStr("stand_in"));
    wp.colSet(ll, "class_code", dsLog.colStr("class_code"));
    wp.colSet(ll, "auth_unit", dsLog.colStr("auth_unit"));
    wp.colSet(ll, "tt_auth_unit", dsLog.colStr("tt_auth_unit"));
    wp.colSet(ll, "logic_del", dsLog.colStr("logic_del"));
    wp.colSet(ll, "tt_logic_del", dsLog.colStr("tt_logic_del"));
    wp.colSet(ll, "curr_otb_amt", dsLog.colStr("curr_otb_amt"));
    wp.colSet(ll, "curr_tot_lmt_amt", dsLog.colStr("curr_tot_lmt_amt"));
    wp.colSet(ll, "curr_tot_std_amt", dsLog.colStr("curr_tot_std_amt"));
    wp.colSet(ll, "fallback", dsLog.colStr("fallback"));
    wp.colSet(ll, "ibm_bit39_code", dsLog.colStr("ibm_bit39_code"));
    wp.colSet(ll, "ibm_bit33_code", dsLog.colStr("ibm_bit33_code"));
    wp.colSet(ll, "ec_ind", dsLog.colStr("ec_ind"));
    wp.colSet(ll, "ucaf", dsLog.colStr("ucaf"));
    wp.colSet(ll, "auth_remark", dsLog.colStr("auth_remark"));
    wp.colSet(ll, "online_redeem", dsLog.colStr("online_redeem"));
    wp.colSet(ll, "ec_flag", dsLog.colStr("ec_flag"));
    wp.colSet(ll, "wk_resp", dsLog.colStr("iso_resp_code") + "-"
        + dsLog.colStr("auth_status_code") + "-" + dsLog.colStr("iso_adj_code"));
    wp.colSet(ll, "wk_ibm",
        dsLog.colStr("ibm_bit39_code") + "-" + dsLog.colStr("ibm_bit33_code"));
    // ---
    // + ", (select mcht_name from cca_mcht_bill where mcht_no =A.mcht_no fetch first 1 rows only)
    // as mcht_chi_name "
    strSql =
        "select mcht_name as mcht_chi_name from cca_mcht_bill where mcht_no =:mcht_no fetch first 1 rows only";
    setString2("mcht_no", dsLog.colStr("mcht_no"));
    wp.logSql = false;
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      log("-->mcht_cname[%s,%s]", dsLog.colStr("mcht_no"), colStr("mcht_chi_name"));
      wp.colSet(ll, "mcht_chi_name", colStr("mcht_chi_name"));
    }

    // is_sql ="select sys_data1 as tt_auth_unit from cca_sys_parm3 where sys_id = 'AUTHUNIT' and
    // sys_key =:auth_unit";
    // ppp("auth_unit",ds_log.col_ss("auth_unit"));
    // sqlSelect(is_sql);
    // if (sql_nrow >0) {
    // wp.col_set(ll, "tt_auth_unit",col_ss("tt_auth_unit"));
    // }

  }



  int checkSame(String lsDate, String lsCardNo, String lsMchtNo, String lsRespCode,
      String lsAuthNo, String lsEntryMode) {
    String sql1 = " select " + " count(*) as db_cnt " + " from cca_auth_txlog "
        + " where tx_date = ? " + " and card_no = ? " + " and mcht_no = ? "
        + " and iso_resp_code = ? " + " and auth_no = ? " + " and substr(pos_mode,1,2) = ? ";

    sqlSelect(sql1,
        new Object[] {lsDate, lsCardNo, lsMchtNo, lsRespCode, lsAuthNo, lsEntryMode});
    if (colNum("db_cnt") > 1)
      return 1;

    return -1;
  }

  void setDataTab2() {

    String lsRespCode = dsLog.colStr("iso_resp_code");
    String lsLogic = dsLog.colStr("logic_del");
    if ("|00,11,000,001".indexOf(lsRespCode) > 0) {
      itemAdd("wk_cnt_tot", 1);
      itemAdd("wk_amt_tot", dsLog.colNum("nt_amt"));

      if (eqIgno(lsLogic, "0")) {
        itemAdd("wk_cnt01", 1);
        itemAdd("wk_amt01", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "M")) {
        itemAdd("wk_cnt02", 1);
        itemAdd("wk_amt02", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "C")) {
        itemAdd("wk_cnt03", 1);
        itemAdd("wk_amt03", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "B")) {
        itemAdd("wk_cnt04", 1);
        itemAdd("wk_amt04", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "J")) {
        itemAdd("wk_cnt05", 1);
        itemAdd("wk_amt05", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "D")) {
        itemAdd("wk_cnt06", 1);
        itemAdd("wk_amt06", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "A")) {
        itemAdd("wk_cnt07", 1);
        itemAdd("wk_amt07", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "W")) {
        itemAdd("wk_cnt08", 1);
        itemAdd("wk_amt08", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "X")) {
        itemAdd("wk_cnt09", 1);
        itemAdd("wk_amt09", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "Y")) {
        itemAdd("wk_cnt10", 1);
        itemAdd("wk_amt10", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "F")) {
        itemAdd("wk_cnt11", 1);
        itemAdd("wk_amt11", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "R")) {
        itemAdd("wk_cnt12", 1);
        itemAdd("wk_amt12", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "V")) {
        itemAdd("wk_cnt13", 1);
        itemAdd("wk_amt13", dsLog.colNum("nt_amt"));
      } else if (eqIgno(lsLogic, "Z")) {
        itemAdd("wk_cnt14", 1);
        itemAdd("wk_amt14", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "x")) {
        itemAdd("wk_cnt18", 1);
        itemAdd("wk_amt18", dsLog.colNum("nt_amt"));
      } else if (eqAny(dsLog.colStr("mtch_flag"), "Y")) {
        itemAdd("wk_cnt15", 1);
        itemAdd("wk_amt15", dsLog.colNum("nt_amt"));
      } else if (eqAny(dsLog.colStr("mtch_flag"), "N")) {
        itemAdd("wk_cnt16", 1);
        itemAdd("wk_amt16", dsLog.colNum("nt_amt"));
      } else if (eqAny(dsLog.colStr("mtch_flag"), "U")) {
        itemAdd("wk_cnt17", 1);
        itemAdd("wk_amt17", dsLog.colNum("nt_amt"));
      }
    }

    if (Arrays.asList(new String[] {"01", "107"}).indexOf(lsRespCode) >= 0) {
      if (eqAny(lsLogic, "0")) {
        itemAdd("wk_01callback_cnt", 1);
        itemAdd("wk_01callback_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "M")) {
        itemAdd("wk_02callback_cnt", 1);
        itemAdd("wk_02callback_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "C")) {
        itemAdd("wk_03callback_cnt", 1);
        itemAdd("wk_03callback_amt", dsLog.colNum("nt_amt"));
      }
      return;
    }
    // -Decline-
    if (Arrays
        .asList(new String[] {"03", "05", "06", "08", "13", "14", "55", "57", "89", "96", "O5",
            "100", "110", "111", "117", "121", "183", "189", "911", "912"})
        .indexOf(lsRespCode) >= 0) {
      if (eqAny(lsLogic, "0")) {
        itemAdd("wk_01decline_cnt", 1);
        itemAdd("wk_01decline_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "M")) {
        itemAdd("wk_02decline_cnt", 1);
        itemAdd("wk_02decline_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "C")) {
        itemAdd("wk_03decline_cnt", 1);
        itemAdd("wk_03decline_amt", dsLog.colNum("nt_amt"));
      }
      return;
    }
    if (Arrays.asList(new String[] {"07", "36", "38", "41", "43", "106", "200", "290"})
        .indexOf(lsRespCode) >= 0) {
      if (eqAny(lsLogic, "0")) {
        itemAdd("wk_01pickup_cnt", 1);
        itemAdd("wk_01pickup_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "M")) {
        itemAdd("wk_02pickup_cnt", 1);
        itemAdd("wk_02pickup_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "C")) {
        itemAdd("wk_03pickup_cnt", 1);
        itemAdd("wk_03pickup_amt", dsLog.colNum("nt_amt"));
      }
      return;
    }
    if (Arrays.asList(new String[] {"54", "101"}).indexOf(lsRespCode) >= 0) {
      if (eqAny(lsLogic, "0")) {
        itemAdd("wk_01expcard_cnt", 1);
        itemAdd("wk_01expcard_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "M")) {
        itemAdd("wk_02expcard_cnt", 1);
        itemAdd("wk_02expcard_amt", dsLog.colNum("nt_amt"));
      } else if (eqAny(lsLogic, "C")) {
        itemAdd("wk_03expcard_cnt", 1);
        itemAdd("wk_03expcard_amt", dsLog.colNum("nt_amt"));
      }
      return;
    }
  }

  void itemAdd(String col, double num1) {
    double lmVal = wp.colNum(0, col) + num1;
    wp.colSet(col, "" + lmVal);
  }

  @Override
  public void dataCheck() {
    // throw new UnsupportedOperationException("Not supported yet."); // Templates.
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
