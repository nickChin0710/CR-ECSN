/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-10-03  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package bilm01;

import java.util.HashMap;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Bilm0120 extends BaseEdit {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;
	CommString commString = new CommString();
	HashMap<String, String> columnMap = new HashMap<String, String>();

	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;
		initDataMap();
		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {
			itemChang();
		} else if (eqIgno(wp.buttonCode, "R1")) {// -資料讀取2-
			dataRead1();
		} else if (eqIgno(wp.buttonCode, "R2")) {
			// -install_type change-
		}

		dddwSelect();
		initButton();
	}

	// ************************************************************************
	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = " where 1=1 ";
	    
	    if(wp.itemEmpty("ex_contract_no")&&wp.itemEmpty("ex_card_no")&&wp.itemEmpty("ex_merchant")
	    		&&wp.itemEmpty("ex_product")&&wp.itemEmpty("ex_idno")&&wp.itemEq("ex_op","3")) {
	    	if(wp.itemEmpty("ex_purchase_date1")&&wp.itemEmpty("ex_purchase_date2")) {
	    		alertErr("消費日期不能為空值");
	    		return;
	    	}
	    }
	    
	    if (empty(wp.itemStr("ex_contract_no")) == false) {
	      wp.whereStr += " and  p.contract_no like :ex_contract_no ";
	      setString("ex_contract_no", wp.itemStr("ex_contract_no") + "%");
	    }
	    if (empty(wp.itemStr("ex_card_no")) == false) {
	      wp.whereStr += " and  p.card_no like :ex_card_no ";
	      setString("ex_card_no", wp.itemStr("ex_card_no") + "%");
	    }
	    if (empty(wp.itemStr("ex_merchant")) == false) {
	      wp.whereStr += " and  p.mcht_no like :ex_merchant ";
	      setString("ex_merchant", wp.itemStr("ex_merchant") + "%");
	    }
	    if (empty(wp.itemStr("ex_product")) == false) {
	      wp.whereStr += " and  p.product_no like :ex_product ";
	      setString("ex_product", wp.itemStr("ex_product") + "%");
	    }
	    if (empty(wp.itemStr("ex_idno")) == false) {
	      wp.whereStr += " and c.id_no = :ex_idno ";
	      setString("ex_idno", wp.itemStr("ex_idno"));
	    }
	    if (empty(wp.itemStr("ex_op")) == false) {
	      switch (wp.itemStr("ex_op")) {
	        case "1":
	          wp.whereStr += " and  p.cps_flag = 'Y' ";
	          break;
	        case "2":
	          wp.whereStr += " and  p.cps_flag = 'N' ";
	          break;
	        case "4":
	          wp.whereStr += " and  p.cps_flag = 'C' ";
	          break;
	      }
	    }

	    if (empty(wp.itemStr("ex_purchase_date1")) == false) {
	      wp.whereStr += " and  p.purchase_date >= :ex_purchase_date1 ";
	      setString("ex_purchase_date1", wp.itemStr("ex_purchase_date1"));
	    }

	    if (empty(wp.itemStr("ex_purchase_date2")) == false) {
	      wp.whereStr += " and  p.purchase_date <= :ex_purchase_date2 ";
	      setString("ex_purchase_date2", wp.itemStr("ex_purchase_date2"));
	    }
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = " p.contract_no " + ",p.contract_seq_no "
		        + ",p.card_no " + ",p.product_no " + ",p.product_name " + ",p.cps_flag"
		        + ",p.mcht_no " + ",p.mcht_chi_name " 
		        + ",p.tot_amt " + ",p.apr_flag " + ",p.mod_user "
		        + ",p.unit_price" + ",p.install_tot_term" + ",p.install_curr_term" + ",p.remd_amt"
		        + ",p.purchase_date";

		wp.daoTable = "bil_contract p left join crd_idno c on p.id_p_seqno = c.id_p_seqno";
		wp.whereOrder = " order by p.purchase_date,p.contract_no";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.setPageValue();
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {

		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		
		
		String mKkContractNo = wp.itemStr("kk_contract_no");
	    if (empty(mKkContractNo)) {
	      mKkContractNo = itemKk("data_k1");
	    }

	    String mKkContractSeqNo = wp.itemStr("kk_contract_seq_no");
	    if (empty(mKkContractSeqNo)) {
	      mKkContractSeqNo = itemKk("data_k2");
	    }


	    if (empty(mKkContractNo)) {
	      mKkContractNo = wp.colStr("contract_no");
	    }


	    if (empty(mKkContractSeqNo)) {
	      mKkContractSeqNo = wp.colStr("contract_seq_no");
	    }

	    wp.selectSQL = "hex(c.rowid) as rowid, c.mod_seqno " 
	    	+ ", c.contract_no "
	        + ", c.contract_seq_no " 
	    	+ ", c.contract_kind " 
	    	+ ", c.acno_p_seqno "
	        + ", c.allocate_flag " 
	    	+ ", c.mcht_no "
	        + ", c.mcht_chi_name " 
	    	+ ", c.product_no " 
	        + ", c.product_name " 
	        + ", c.card_no " 
	    	+ ", c.acct_type " 
	        + ", c.limit_end_date " 
	    	+ ", c.vip_code "
	        + ", c.first_post_date " 
	    	+ ", c.post_cycle_dd " 
	        + ", c.purchase_date " 
	    	+ ", c.stmt_cycle "
	        + ", c.install_curr_term " 
	    	+ ", c.all_post_flag " 
	        + ", c.forced_post_flag "
	        + ", c.fee_flag " 
	        + ", c.tot_amt as install_tot_amt" 
	        + ", c.unit_price "
	        + ", c.install_tot_term " 
	        + ", c.install_tot_term as install_tot_term_a" 
	        + ", c.first_remd_amt " 
	        + ", c.remd_amt "
	        + ", c.first_remd_amt+c.remd_amt+c.unit_price as first_install_amt "
	        + ", c.extra_fees " 
	        + ", c.trans_rate "
	        + ", decode(c.first_post_kind,'1','Y','N') as installment_delay  "
	        + ", c.year_fees_rate "
	        + ", c.spec_flag "
	        + ", c.apr_date " 
	        + ", c.apr_flag "
	        + ", c.mod_user "
	        + ", to_char(c.mod_time,'yyyymmdd') as mod_date "
	        + ", UF_IDNO_ID(c.id_p_seqno) as db_id"
	        + ", UF_IDNO_NAME(c.card_no) as db_name" 
	        + ", CASE WHEN m.STMT_INST_FLAG='Y' THEN 'S' WHEN m.TRANS_FLAG='Y' THEN 'K' ELSE 'N' END install_type "
	        + ", i.chi_name" 
	        + ", i.id_no"
	        + ", i.birthday" 
	        + ", t.vip_code" 
	        + ", t.acct_type"
	        + ", t.acct_key";
	    wp.daoTable = "bil_contract c left join bil_merchant m on c.mcht_no=m.mcht_no  "
	        + "               left join crd_idno i on c.id_p_seqno=i.id_p_seqno  "
	        + "               left join bil_contract_acaj a on c.contract_no=a.contract_no and c.contract_seq_no=a.contract_seq_no  "
	        + "               left join act_acno t on c.acno_p_seqno=t.p_seqno   ";
	    wp.whereStr = "where 1=1";
	    wp.whereStr += " and  c.contract_no = :contract_no and c.contract_seq_no = :contract_seq_no ";
	    setString("contract_no", mKkContractNo);
	    setString("contract_seq_no", mKkContractSeqNo);

	    pageSelect();

	    if (sqlNotFind()) {
	      //alertErr("查無資料, contract_no=" + mKkContractNo);
	      alertErr("KEY值未輸入或未輸入完整，無法讀取資料");
	    }
	    
	    String pSeqno = wp.colStr("acno_p_seqno");
	    
	    selectAct(pSeqno);
		getLastMinPayDate(pSeqno);
		getAutopayIndicator(pSeqno);
		getTotAmtMonth(pSeqno);
		
		wp.colSet("p_seqno",pSeqno);
		wp.colSet("autopay_indicator",columnMap.get("autopay_indicator"));
		wp.colSet("last_min_pay_date",columnMap.get("last_min_pay_date"));
		wp.colSet("acct_status",columnMap.get("acct_status"));
		wp.colSet("line_of_credit_amt",columnMap.get("line_of_credit_amt"));
		wp.colSet("this_closing_date",columnMap.get("this_closing_date"));
		wp.colSet("this_lastpay_date",columnMap.get("this_lastpay_date"));
		wp.colSet("ttl_amt_bal",columnMap.get("ttl_amt_bal"));
		wp.colSet("install_tot_amt",columnMap.get("ttl_amt_bal"));
		wp.colSet("min_pay_bal",columnMap.get("min_pay_bal"));
		wp.colSet("tot_amt_month",columnMap.get("tot_amt_month"));
		wp.colSet("stmt_this_ttl_amt",columnMap.get("stmt_this_ttl_amt"));

	}

	// ************************************************************************
	public void dataRead1() throws Exception {
		String idNo = wp.itemStr("id_no");
		String cardNo = wp.itemStr("card_no");
		
		if(empty(idNo) && empty(cardNo)) {
			alertErr("帳戶身分證號及卡號不能同時為空值");
			return;
		}
		
		if (empty(cardNo) == false) {
			String sql = "select UF_ACNO_NAME(p_seqno) as chi_name,p_seqno from crd_card where card_no = ? ";
			setString(1,cardNo);
			sqlSelect(sql);
		
			if(sqlRowNum == 0) {
				alertErr("查無此卡號資料");
				return;
			}
		} else {
			String sql = "select UF_ACNO_NAME(p_seqno) as chi_name,p_seqno from act_acno where acct_key = ? and acct_type = '01'";
			setString(1,idNo+"0");
			sqlSelect(sql);
		
			if(sqlRowNum == 0) {
				alertErr("查無此帳戶身分證號");
				return;
			}
		}
		String pSeqno = sqlStr(0,"p_seqno");
		String chiName = sqlStr(0,"chi_name");
		
		selectAct(pSeqno);
		getLastMinPayDate(pSeqno);
		getAutopayIndicator(pSeqno);
		getTotAmtMonth(pSeqno);
		
		wp.colSet("p_seqno",pSeqno);
		wp.colSet("chi_name",chiName);
		wp.colSet("id_no",idNo);
		wp.colSet("autopay_indicator",columnMap.get("autopay_indicator"));
		wp.colSet("last_min_pay_date",columnMap.get("last_min_pay_date"));
		wp.colSet("acct_status",columnMap.get("acct_status"));
		wp.colSet("line_of_credit_amt",columnMap.get("line_of_credit_amt"));
		wp.colSet("this_closing_date",columnMap.get("this_closing_date"));
		wp.colSet("this_lastpay_date",columnMap.get("this_lastpay_date"));
		wp.colSet("ttl_amt_bal",columnMap.get("ttl_amt_bal"));
		wp.colSet("install_tot_amt",columnMap.get("ttl_amt_bal"));
		wp.colSet("min_pay_bal",columnMap.get("min_pay_bal"));
		wp.colSet("tot_amt_month",columnMap.get("tot_amt_month"));
		wp.colSet("stmt_this_ttl_amt",columnMap.get("stmt_this_ttl_amt"));
		
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		bilm01.Bilm0120Func func = new bilm01.Bilm0120Func(wp);
		
		String lsContractNo = "";
	    String lsContractSeqNo = "";
	    if (strAction.equals("A")) {
	      String lsSqlc =
	          " select substr(to_char(bil_contractseq.nextval,'0000000000'),2,10) as cseq from dual";
	      sqlSelect(lsSqlc);

	      lsContractNo = sqlStr("cseq");
	      System.out.println("ls_contract_no : " + lsContractNo);
	      lsContractSeqNo = "1";
	      wp.colSet("contract_no", lsContractNo);
	      wp.colSet("contract_seq_no", lsContractSeqNo);
	      func.varsSet("aa_contract_no", lsContractNo);
	      func.varsSet("aa_contract_seq_no", lsContractSeqNo);
	    }
		
		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {

		try {
			if (wp.respHtml.indexOf("_detl") > 0 && !strAction.equals("AJAX")) {
				String installType = wp.colStr("install_type");
				if(empty(installType)) {
					installType = "N";
				}
				String lsWhere = "";
				if(installType.equals("N")) {
					lsWhere = " and mcht_no like '106000%' and trans_flag <> 'Y' and stmt_inst_flag <> 'Y' ";
				}
				if(installType.equals("S")) {
					lsWhere = " and stmt_inst_flag = 'Y' ";
				}
				if(installType.equals("K")) {
					lsWhere = " and trans_flag = 'Y'";
				}
				wp.initOption = "--";
				if(!strAction.equals("R2"))
					wp.optionKey = wp.colStr("mcht_no");
				this.dddwList("dddw_mcht_no_parm", "bil_merchant", "mcht_no", "mcht_chi_name",
						"where 1=1 " + lsWhere);
				
				String lsMchtNoParm = "";
				if(!strAction.equals("R2")) {
					wp.optionKey =  wp.colStr("product_no");
					lsMchtNoParm = wp.colStr("mcht_no");
				}
				StringBuffer dddwWhere = new StringBuffer();
				dddwWhere.append(" where 1=1 and mcht_no = '");
				dddwWhere.append(lsMchtNoParm);
				dddwWhere.append("' order by product_no ");
				if (!empty(lsMchtNoParm)) {
					this.dddwList("dddw_product_no", "bil_prod", "product_no",
							"product_no||'_'||product_name||'('||mcht_no||')'", dddwWhere.toString());
				}
			} else {
				wp.initOption = "--";
				wp.optionKey = wp.itemStr("ex_merchant");
				this.dddwList("dddw_merchant", "bil_merchant", "mcht_no", "mcht_chi_name",
						"where 1=1 and mcht_status = '1' group by mcht_no,mcht_chi_name order by mcht_no");
				String exMchtNo = wp.itemStr("ex_merchant");
				wp.initOption = "--";
				wp.optionKey = wp.itemStr("ex_product");
				setString("mcht_no", exMchtNo);
				this.dddwList("dddw_product", "bil_prod", "product_no", "product_name",
						" where  1=1  and mcht_no = :mcht_no order by product_no");
			}
		} catch (Exception e) {

		}

	}

	// ************************************************************************
	@Override
	public void initPage() {
		return;
	}

	// ************************************************************************
	void itemChang() throws Exception {
		String actionCode = wp.itemStr("action_code");
		if(actionCode.equals("AJAX")) {
			itemChangExMcht();
		}
		if(actionCode.equals("AJAX1")) {
			itemChangMcht();
		}
		if(actionCode.equals("AJAX2")) {
			itemChangProduct();
		}
	}
	void itemChangExMcht() throws Exception {
		wp.varRows = 1000;
	    setSelectLimit(0);
	    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
	        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
	        + " order by mcht_no ";
	    setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
	    sqlSelect(lsSql);

	    for (int i = 0; i < sqlRowNum; i++) {
	      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
	      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
	    }
	    wp.addJSON("ex", "mcht_no");
	    
	    return;
	}

	void itemChangMcht() throws Exception {
		String lsMerchantNo = wp.itemStr("data_mcht_no");
		if(empty(lsMerchantNo))
			return;
		String lsMchtNoParm = lsMerchantNo;
		String dddwWhere = "", option = "";
		if (!empty(lsMchtNoParm))
			dddwWhere = " and mcht_no = :ls_mcht_no_parm ";
		String selectBilProd = "select product_no "
				+ " ,product_no||'_'||product_name||'('||mcht_no||')' as product_name " + " from bil_prod "
				+ " where 1=1 " + dddwWhere + " order by product_no ";
		setString("ls_mcht_no_parm", lsMchtNoParm);
		sqlSelect(selectBilProd);
		if (sqlRowNum <= 0) {
			return;
		}
		option += "<option value=\"\">--</option>";
		for (int ii = 0; ii < sqlRowNum; ii++) {
			option += "<option value=\"" + sqlStr(ii, "product_no") + "\" ${tot_term-" + sqlStr(ii, "product_no")
					+ "} >" + sqlStr(ii, "product_name") + "</option>";
		}
		wp.addJSON("dddw_product_no_option", option);
		
		//取installment_delay
		if (!empty(lsMchtNoParm))
			dddwWhere = " and mcht_no = :ls_mcht_no_parm ";
		
		String selectBilMcht = "select installment_delay "
                + " from bil_merchant "
				+ " where 1=1 " + dddwWhere ;
		setString("ls_mcht_no_parm", lsMchtNoParm);
		sqlSelect(selectBilMcht);
		if (sqlRowNum <= 0) {
			return;
		}
		wp.addJSON("json_installment_delay", sqlStr("installment_delay"));
	}
	
	void itemChangProduct() throws Exception {
		String lsProductNo = wp.itemStr("data_product_no");
		String lsMchtNo = wp.itemStr("data_mcht_no");
		String installTotAmt = wp.itemStr("data_install_tot_amt");
		String installTotTerm = wp.itemStr("data_install_tot_term");
		String option = wp.itemStr("dddw_product_no_option");
		wp.addJSON("dddw_product_no_option", option);

		if(empty(installTotAmt)) {
			alertErr("分期金額不能為空值");
			return;
		}
		if(empty(installTotTerm)) {
			alertErr("分期期數不能為空值");
			return;
		}

		calculateInstallAmt(installTotAmt,installTotTerm);
		getTransRate(lsMchtNo,lsProductNo);
		
		wp.addJSON("json_first_install_amt", columnMap.get("first_install_amt"));
		wp.addJSON("json_unit_price", columnMap.get("unit_price"));
		wp.addJSON("json_trans_rate", columnMap.get("trans_rate"));
		wp.addJSON("json_year_fees_rate", columnMap.get("year_fees_rate"));
		wp.addJSON("select_val", lsProductNo);
	}

	private void calculateInstallAmt(String installTotAmt, String productNo) throws Exception {
		if (!(commString.strToInt(productNo) == 0)) {
			int unitPrice = commString.strToInt(installTotAmt) / commString.strToInt(productNo);
			int firstInstallAmt = commString.strToInt(installTotAmt) - (unitPrice * commString.strToInt(productNo))
					            + unitPrice;
			columnMap.put("first_install_amt", commString.intToStr(firstInstallAmt));
			columnMap.put("unit_price", commString.intToStr(unitPrice));
		} else {
			columnMap.put("first_install_amt", "");
			columnMap.put("unit_price", "");
		}
	}

	private void getTransRate(String mchtNo, String productNo) {
		String sql = "select trans_rate,year_fees_rate from bil_prod where mcht_no = ? and product_no = ? ";
		setString(1, mchtNo);
		setString(2, productNo);
		sqlSelect(sql);
		columnMap.put("trans_rate", sqlStr("trans_rate"));
		columnMap.put("year_fees_rate", sqlStr("year_fees_rate"));
	}

	private void getAutopayIndicator(String pSeqno) {
		String sql = "select autopay_indicator from act_acct_curr where p_seqno = ? and curr_code = '901' ";
		setString(1, pSeqno);
		sqlSelect(sql);
		columnMap.put("autopay_indicator", sqlStr("autopay_indicator"));
	}

	private void getLastMinPayDate(String pSeqno) {
		String sql = "select last_min_pay_date,ttl_amt,ttl_amt_bal,min_pay_bal from act_acct where p_seqno = ? ";
		setString(1, pSeqno);
		sqlSelect(sql);
		columnMap.put("last_min_pay_date", sqlStr("last_min_pay_date"));
		columnMap.put("ttl_amt_bal", sqlStr("ttl_amt_bal"));
		columnMap.put("min_pay_bal", sqlStr("min_pay_bal"));
		columnMap.put("stmt_this_ttl_amt", sqlStr("ttl_amt"));
	}
	
	private void getTotAmtMonth(String pSeqno) {
		String sql = "select tot_amt_month from cca_card_acct where p_seqno = ? and acct_type = '01' and to_char(sysdate,'yyyymmdd') between adj_eff_start_date and adj_eff_end_date ";
		setString(1, pSeqno);
		sqlSelect(sql);
		columnMap.put("tot_amt_month", sqlStr(0,"tot_amt_month"));
	}

	private void selectAct(String pSeqno) {
		String sql = "select acct_status,stmt_cycle,line_of_credit_amt from act_acno where acno_p_seqno = ? ";
		setString(1, pSeqno);
		sqlSelect(sql);
		String acctStatus = sqlStr("acct_status");
		String stmtCycle = sqlStr("stmt_cycle");
		String lineOfCreditSmt = sqlStr("line_of_credit_amt");

		sql = "select last_acct_month from ptr_workday where stmt_cycle = ? ";
		setString(1, stmtCycle);
		sqlSelect(sql);
		String lastAcctMonth = sqlStr("last_acct_month");

		//sql = "select stmt_cycle_date,stmt_last_payday,ttl_amt_bal,min_pay_bal,stmt_this_ttl_amt from act_acct_hst where p_seqno = ? and acct_month = ? ";
		sql = "select stmt_cycle_date,stmt_last_payday from act_acct_hst where p_seqno = ? and acct_month = ? ";
		setString(1, pSeqno);
		setString(2, lastAcctMonth);
		sqlSelect(sql);

		columnMap.put("acct_status", acctStatus);
		columnMap.put("line_of_credit_amt", lineOfCreditSmt);
		columnMap.put("this_closing_date", sqlStr("stmt_cycle_date"));
		columnMap.put("this_lastpay_date", sqlStr("stmt_last_payday"));
	}

	void initDataMap() {
		columnMap.put("first_install_amt", "");
		columnMap.put("unit_price", "");
		columnMap.put("trans_rate", "");
		columnMap.put("year_fees_rate", "");
		columnMap.put("autopay_indicator", "");
		columnMap.put("last_min_pay_date", "");
		columnMap.put("acct_status", "");
		columnMap.put("line_of_credit_amt", "");
		columnMap.put("this_closing_date", "");
		columnMap.put("this_lastpay_date", "");
		columnMap.put("ttl_amt_bal", "");
		columnMap.put("min_pay_bal", "");
		columnMap.put("tot_amt_month", "");
		columnMap.put("stmt_this_ttl_amt", "");
	}

} // End of class
