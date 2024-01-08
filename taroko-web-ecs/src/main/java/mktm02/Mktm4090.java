/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-02  V1.00.01  ryan       program initial                            *
* 109-04-20  v1.00.02  Andy       Update add throws Exception                *
* 109-09-10  v1.00.03  Andy       Update Mantis4106  
* 112-02-16  V1.00.04  Machao     sync from mega & updated for project coding standard                        *
******************************************************************************/
package mktm02;


import java.util.Arrays;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Mktm4090 extends BaseEdit {
	Mktm4090Func func;
	String mProgName = "mktm4090";
	int i = 0,iiUnit=0;
	String  kkSeqNo = "",kkRdmSeqno="",kkDbTemp="",kkMchtNo="",kkDtlKind="",tableName="",whereorder="",tableNameTel ="ptr_redeem_dtl1",mchtNo = " merchant_no ";
	long llOpt=0, llCnt=0, llDupl=0;
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		// log("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
		// wp.respCode + ",rHtml=" + wp.respHtml);
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
			updateFunc();
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
		} else if (eqIgno(wp.buttonCode, "S2")) {
			strAction = "S2";
        	saveFunc();
		} else if (eqIgno(wp.buttonCode, "SCG")) {
			/* 團體代號、卡種查詢 */
			strAction = "SCG";
			selectCG();
		} else if (eqIgno(wp.buttonCode, "UCG")) {
			/* 團體代號、卡種存檔*/
			strAction = "UCG";
			updateCG();
		} else if (eqIgno(wp.buttonCode, "SD")) {
			/* 刪除功能 */
			strAction = "SD";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "SA")) {
			/* 帳戶類別查詢 */
			strAction = "SA";
			selectA();
		} else if (eqIgno(wp.buttonCode, "UA")) {
			/* 帳戶類別存檔 */
			strAction = "UA";
			updateA();
		} else if (eqIgno(wp.buttonCode, "SM")) {
			/* 同群組特店查詢 */
			strAction = "SM";
			selectM();
		} else if (eqIgno(wp.buttonCode, "UM")) {
			/* 同群組特店存檔 */
			strAction = "UM";
			updateM();
		} else if (eqIgno(wp.buttonCode, "PDF")) {
			strAction = "PDF";
			pdfPrint();
		} 

		dddwSelect();
		initButton();
	}
	
	@Override
	public void initPage(){
		wp.colSet("rdm_seqno", "0");
		wp.colSet("rdm_discrate", "0");
		wp.colSet("rdm_discamt", "0");
		wp.colSet("rdm_unitpoint", "0");
		wp.colSet("rdm_unitamt", "0");
		wp.colSet("rdm_destamt", "0");
		wp.colSet("crt_date", wp.sysDate);
		wp.colSet("crt_user", wp.loginUser);
	}
	
	int getWhereStr(){
		
		String lsDate1 = wp.itemStr2("ex_mchtno1");
		String lsDate2 = wp.itemStr2("ex_mchtno2");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[特店代號-起迄]  輸入錯誤");
			return -1;
		}
		lsDate1 = wp.itemStr2("ex_strdate1");
		lsDate2 = wp.itemStr2("ex_strdate2");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[活動期間(起)-起迄]  輸入錯誤");
			return -1;
		}
		lsDate1 = wp.itemStr2("ex_enddate1");
		lsDate2 = wp.itemStr2("ex_enddate2");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[活動期間(迄)-起迄]  輸入錯誤");
			return -1;
		}
		wp.whereStr = " where 1=1 ";
		/*table_name = "ptr_redeem_t";
		whereorder =  " UNION "
				+ " SELECT hex(rowid) as rowid, "
				+ " rdm_mchtno, "
				+ " rdm_seqno, "
				+ " rdm_strdate, "
				+ " rdm_enddate, "
				+ " rdm_destamt, "
				+ " rdm_discrate, "
				+ " rdm_discamt, "
				+ " rdm_unitpoint, "
				+ " rdm_unitamt, "			
				+ " lpad(' ',100,' ') db_cname, "
				+ " crt_no, "
				+ "  'N' db_temp "
				+ " from ptr_redeem ";*/
		if (empty(wp.itemStr2("ex_mchtno1")) == false) {
			wp.whereStr += " and  a.rdm_mchtno >= :ex_mchtno1 ";
			setString("ex_mchtno1", wp.itemStr2("ex_mchtno1"));
		}
		if (empty(wp.itemStr2("ex_mchtno2")) == false) {
			wp.whereStr += " and  a.rdm_mchtno <= :ex_mchtno2 ";
			setString("ex_mchtno2", wp.itemStr2("ex_mchtno2"));
		}
		
		if (empty(wp.itemStr2("ex_strdate1")) == false) {
			wp.whereStr += " and  a.rdm_strdate >= :ex_strdate1 ";
			setString("ex_strdate1", wp.itemStr2("ex_strdate1"));
		}
		if (empty(wp.itemStr2("ex_strdate2")) == false) {
			wp.whereStr += " and a.rdm_strdate <= :ex_strdate2 ";
			setString("ex_strdate2", wp.itemStr2("ex_strdate2"));
		}
		
		if (empty(wp.itemStr2("ex_enddate1")) == false) {
			wp.whereStr += " and  a.rdm_enddate >= :ex_enddate1 ";
			setString("ex_enddate1", wp.itemStr2("ex_enddate1"));
		}
		if (empty(wp.itemStr2("ex_enddate2")) == false) {
			wp.whereStr += " and  a.rdm_enddate <= :ex_enddate2 ";
			setString("ex_enddate2", wp.itemStr2("ex_enddate2"));
		}
		if (empty(wp.itemStr2("ex_crt_user")) == false) {
			wp.whereStr += " and  a.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr2("ex_crt_user"));
		}
		
		if (empty(wp.itemStr2("ex_crt_date")) == false) {
			wp.whereStr += " and  a.crt_date = :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr2("ex_crt_date"));
		}
		
		if (wp.itemStr2("ex_apprflag").equals("N")) {
			tableName = "ptr_redeem_t";
			whereorder="order by a.rdm_mchtno,a.rdm_seqno ";
		}
		if (wp.itemStr2("ex_apprflag").equals("Y")) {
			tableName = "ptr_redeem";
			whereorder="order by a.rdm_mchtno,a.rdm_seqno ";
		}
		return 1;
	}
	
	@Override
	public void queryFunc() throws Exception {

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		if(getWhereStr()!=1)return;
		wp.pageControl();
		
		wp.selectSQL = " hex(a.rowid) as rowid, "
					+ " a.rdm_mchtno, "
					+ " a.rdm_seqno, "
					+ " a.rdm_strdate, "
					+ " a.rdm_enddate, "
					+ " a.rdm_destamt, "
					+ " a.rdm_discrate, "
					+ " a.rdm_discamt,"
					+ " a.rdm_unitpoint, "
					+ " a.rdm_unitamt, "
					+ " decode(a.rdm_discratefg,'<','<=',a.rdm_discratefg) rdm_discratefg, "
					+ " a.crt_no,"
					+ " b.mcht_chi_name as db_mchtname"
					;
		if(tableName.equals("ptr_redeem")){
			wp.selectSQL +=  " ,'N' db_temp ";
		}
		if(tableName.equals("ptr_redeem_t")){
			wp.selectSQL +=  " ,'Y' db_temp ";
		}
		wp.daoTable = tableName+" as a left join bil_merchant as b on a.rdm_mchtno = b.mcht_no ";
		wp.whereOrder = whereorder;
		if (strAction.equals("XLS")) {
			selectNoLimit();
		}
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

	    //list_wkdata();
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wp.colSet(ii,"tt_db_temp", commString.decode(wp.colStr(ii,"db_temp"), ",Y,N", ",未覆核,已覆核"));
		}
		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		//dddw_select();

	}

	@Override
	public void querySelect() throws Exception {
		kkMchtNo = itemKk("data_k1");
		kkRdmSeqno = itemKk("data_k2");
		if (empty(kkMchtNo)){
		       	 kkMchtNo = wp.itemStr2("rdm_mchtno");  
		}
		if (empty(kkRdmSeqno)){
			kkRdmSeqno = wp.itemStr2("rdm_seqno");  
		}
		int totCnt = mainTablecnt(kkMchtNo,kkRdmSeqno);
		if (totCnt > 0) {
			tableName = "ptr_redeem_t";
			wp.alertMesg = "<script language='javascript'> alert('此筆資料待覆核')</script>";
		}else{
			tableName = "ptr_redeem";
		}
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

		wp.selectSQL = " hex(rowid) as rowid, "
				+ " rdm_mchtno, "
				+ " rdm_seqno, "
				+ " rdm_strdate, "
				+ " rdm_enddate, "
				+ " rdm_destamt, "
				+ " rdm_discrate, "
				+ " rdm_discamt,"
				+ " rdm_unitpoint, "
				+ " rdm_unitamt, "
				+ " decode(rdm_discratefg,'<=','1',decode(rdm_discratefg,'<','1','2')) as rdm_discratefg, "
				+ " lpad(' ',100,' ') db_cname, "
				+ " crt_no,"
				+ " crt_user, "
				+ " crt_date, "
				+ " mod_seqno "
				;
		if(tableName.equals("ptr_redeem")){
			wp.selectSQL +=  " ,'N' db_temp ";
		}
		if(tableName.equals("ptr_redeem_t")){
			wp.selectSQL +=  " ,'Y' db_temp ";
		}
		wp.daoTable = tableName;
		wp.whereStr = " where 1=1 ";
		wp.whereStr +=" and rdm_mchtno = :kk_mcht_no and rdm_seqno = :kk_rdm_seqno ";
		setString("kk_mcht_no",kkMchtNo);
		setString("kk_rdm_seqno",kkRdmSeqno);
		pageSelect();
		listWkdata();
	}

	@Override
	public void saveFunc() throws Exception {
		func = new Mktm4090Func(wp);
		String lsAction="";
		if(!strAction.equals("SD")){
			if(ofValidation()!=1){
				return;
			}
		}
		if(strAction.equals("S2")){
			lsAction="S2";
    		if(wp.itemStr2("db_temp").equals("N")){
    			strAction = "A";
        	}else if(wp.itemStr2("db_temp").equals("Y")){
        		//--check detail ----------------------------------------------
        		if(wfChkDetail()!=1){
        			return;
        		}
        		strAction = "U";
        	}
    	}else if(strAction.equals("SD")){
    		strAction = "D";
    		lsAction="D";
    	}
		rc = func.dbSave(strAction);
        log(func.getMsg());
        if (rc != 1) {
            alertErr2(func.getMsg());
        }
        this.sqlCommit(rc);
        if (rc == 1){
        	if(lsAction.equals("S2")){
        		alertMsg("存檔完成");
        	}else if(lsAction.equals("D")){
        		alertMsg("刪除完成");
        	}
        }
        if(!empty(lsAction))
            querySelect();
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud("XX");
		}
		if (wp.respHtml.indexOf("_add") > 0) {
			this.btnModeAud();
		}
	}

	@Override
	public void dddwSelect() {

		try {
			wp.optionKey = wp.itemStr2("dtl_value");
			this.dddwList("dddw_group", "ptr_group_code", "group_code", "group_name", " where  1=1  order by group_code");

			wp.optionKey = wp.itemStr2("dtl_value");
			this.dddwList("dddw_card", "ptr_card_type", "card_type", "name", " where  1=1  order by card_type");
			
		} catch (Exception ex) {
		}
	}
	
	void listWkdata() throws Exception {
		String ss = "";
		ss = wp.colStr("db_temp");
		wp.colSet("tt_db_temp", commString.decode(ss, ",Y,N", ",未覆核,已覆核"));
		ss = wp.colStr("rdm_mchtno");
		String sqlSelect = "select mcht_chi_name,mcht_type,mcht_group_id FROM bil_merchant  WHERE mcht_no = :ss ";
		setString("ss", ss);
		sqlSelect(sqlSelect);
		if (sqlRowNum > 0) {
			wp.colSet("db_mchtname", sqlStr("mcht_chi_name"));
			wp.colSet("db_mchttype", empty(sqlStr("mcht_type")) ? "1" : sqlStr("mcht_type"));
			wp.colSet("db_mcht_group", sqlStr("mcht_group_id"));
		}
	}
	
	
	
	public void selectCG() throws Exception {
    	this.selectNoLimit();
    	kkMchtNo = itemKk("mcht_no_kk");
    	if(empty(kkMchtNo)){
    		kkMchtNo = wp.itemStr2("mcht_no");
    	}
    	kkDtlKind = itemKk("dtl_kind_kk");
    	if(empty(kkDtlKind)){
    		kkDtlKind = wp.itemStr2("dtl_kind");
    	}
    	kkSeqNo = itemKk("seq_no_kk");
		if (empty(kkSeqNo)) {
			kkSeqNo = wp.itemStr2("rdm_seqno");
		}
		
		int totCnt = mainTablecnt(kkMchtNo,kkSeqNo);
		if (totCnt > 0) {
			tableNameTel = "ptr_redeem_dtl1_t";
			mchtNo = " mcht_no ";
			wp.colSet("btnUp_disable", "");
		}
		
        wp.selectSQL ="hex(rowid) as rowid "
	              + ", dtl_value "
	              + ", "
	              + mchtNo
	              + " as mcht_no "
	              + ", seq_no ";
        wp.daoTable = tableNameTel;
        wp.whereOrder = " order by dtl_value ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr  += " and  dtl_kind=:dtl_kind and "
        		+ mchtNo
        		+ " = :mcht_no and seq_no = :kk_seq_no ";
        setString("dtl_kind",kkDtlKind);
        setString("mcht_no", kkMchtNo);
        setString("kk_seq_no", kkSeqNo);
        pageQuery();
        wp.setListCount(1);
		wp.notFound = "";
		listWkdataCG();
    }
	
	void selectA() throws Exception{
		this.selectNoLimit();
		 kkMchtNo = itemKk("mcht_no_kk");
	    	if(empty(kkMchtNo)){
	    		kkMchtNo = wp.itemStr2("mcht_no");
	    }
		kkSeqNo = itemKk("seq_no_kk");
		if (empty(kkSeqNo)) {
			kkSeqNo = wp.itemStr2("rdm_seqno");
		}
        wp.selectSQL =" acct_type "
	              + ", chin_name ";
        wp.daoTable = " ptr_acct_type ";
        wp.whereOrder = " order by acct_type ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr  += "  ";

        pageQuery();
        wp.setListCount(1);
		wp.notFound = "";
		
		listWkdataA();
	}

	void selectM() throws Exception{
		wp.pageRows =999;
		this.selectNoLimit();
		String kkCrtNo="",kkDbMchtGroup="";
		 kkMchtNo = itemKk("mcht_no_kk");
	    	if(empty(kkMchtNo)){
	    		kkMchtNo = wp.itemStr2("mcht_no");
	    }
		kkSeqNo = itemKk("seq_no_kk");
		if (empty(kkSeqNo)) {
			kkSeqNo = wp.itemStr2("rdm_seqno");
		}
		kkCrtNo = itemKk("crt_no_kk");
		if (empty(kkCrtNo)) {
			kkCrtNo = wp.itemStr2("crt_no");
		} 
		kkDbMchtGroup = itemKk("db_mcht_group_kk");
		if (empty(kkDbMchtGroup)) {
			kkDbMchtGroup = wp.itemStr2("db_mcht_group");
		} 
		wp.colSet("mcht_no", kkMchtNo);
		wp.colSet("rdm_seqno", kkSeqNo);
		wp.colSet("db_mcht_group", kkDbMchtGroup);
		wp.colSet("crt_no", kkCrtNo);
		if(kkSeqNo.equals("0")&&!empty(kkDbMchtGroup)){
			wp.selectSQL =" mcht_no as mcht_no2 "
	              + " , mcht_chi_name "
	              ;
			wp.daoTable = " bil_merchant ";
			wp.whereOrder = " order by mcht_no ";
			wp.whereStr = " where 1=1 ";
			wp.whereStr  += " and  mcht_no <> :kk_mcht_no "
        		 	 	+ " and mcht_group_id = :kk_db_mcht_group ";
			setString("kk_mcht_no",kkMchtNo);
			setString("kk_db_mcht_group",kkDbMchtGroup);
			pageQuery();
			wp.setListCount(1);
			wp.notFound = "";
			return; 
		}
		int totCnt = mainTablecnt(kkMchtNo,kkSeqNo);
		if (totCnt > 0) {
			tableNameTel = "ptr_redeem_dtl1_t";
			mchtNo = " mcht_no ";
			wp.colSet("btnUp_disable", "");
		}
        //-get ptr_redeem_dtl1(_t)-
        wp.selectSQL =" mcht_no as mcht_no2 "
  	              + " , mcht_chi_name "
  	              ;
        wp.daoTable = " bil_merchant ";
        wp.whereOrder = " order by mcht_no ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr  += " and  mcht_no in ( "
          			+ " select dtl_value "
          			+ " from "
          			+ tableNameTel
          			+ " where "
          			+ mchtNo
          			+ " = :kk_mcht_no "
          			+ " and dtl_kind='MCHT-GROUP' "
          			+ " and seq_no = :kk_seq_no "
          			+ " ) ";
        setString("kk_mcht_no",kkMchtNo);
        setString("kk_seq_no",kkSeqNo);
        pageQuery();
        wp.setListCount(1);
        wp.colSet("selectCnt",wp.selectCnt+"");
        if(sqlNotFind()) {
        	//-read ptr_redeem-
           	wp.selectSQL =" mcht_no as mcht_no2 "
    	              + " , mcht_chi_name "
    	              ;
            wp.daoTable = " bil_merchant ";
            wp.whereOrder = " order by mcht_no ";
            wp.whereStr = " where 1=1 ";
            wp.whereStr  += " and  mcht_no <> :kk_mcht_no "
            		+ " and mcht_no in("
            		+ " select rdm_mchtno "
            		+ " from ptr_redeem "
            		+ " where crt_no = :kk_crt_no "
            		+ " ) ";
            setString("kk_mcht_no",kkMchtNo);
            setString("kk_crt_no",kkCrtNo);
            pageQuery();
            wp.setListCount(1);
            wp.colSet("selectCnt",wp.selectCnt+"");
            if(sqlNotFind()) {
            	return;
            }else{
            	listWkdataM(kkDbMchtGroup);
            }
          }else{
        	  listWkdataM(kkDbMchtGroup);
        }
	}
	
	 void listWkdataCG() throws Exception{
		String ss="",sqlSelect="";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss = wp.colStr(ii,"dtl_value");
			if (kkDtlKind.equals("GROUP-CODE")) {
				sqlSelect = "select group_code||'_'||group_name as tt_dtl_value from ptr_group_code where group_code = :tt_dtl_value ";
				setString("tt_dtl_value", ss);
				sqlSelect(sqlSelect);
				if (sqlRowNum > 0) {
					ss = sqlStr("tt_dtl_value");
				}
				wp.colSet(ii,"tt_dtl_value",ss );
			}
			if (kkDtlKind.equals("CARD-TYPE")) {
				sqlSelect = "select card_type||'_'||name as tt_dtl_value from ptr_card_type where card_type = :tt_dtl_value ";
				setString("tt_dtl_value", ss);
				sqlSelect(sqlSelect);
				if (sqlRowNum > 0) {
					ss = sqlStr("tt_dtl_value");
				}
				wp.colSet(ii,"tt_dtl_value",ss );
			}
		}
		wp.colSet("mcht_no", kkMchtNo);
		wp.colSet("rdm_seqno", kkSeqNo);
	 }
	 void listWkdataA() throws Exception{
		wp.colSet("mcht_no", kkMchtNo);
		wp.colSet("rdm_seqno", kkSeqNo);
		int totCnt = mainTablecnt(kkMchtNo,kkSeqNo);
		
		if (totCnt > 0) {
			tableNameTel = "ptr_redeem_dtl1_t";
			mchtNo = " mcht_no ";
			wp.colSet("btnUp_disable", "");
		}
		String ss = "",ss2="";
		String sqlSelect = "select "
					+ mchtNo
					+ " as mcht_no "
					+ " ,dtl_kind ,seq_no ,dtl_value "
					+ " from "
					+ tableNameTel
					+ " where "
					+ mchtNo
					+ " = :mcht_no "
					+ " and seq_no = :seq_no "
					+ " and dtl_kind ='ACCT-TYPE' ";
		setString("mcht_no",kkMchtNo);
		setString("seq_no",kkSeqNo);
		sqlSelect(sqlSelect);
		for(int ii = 0 ; ii<wp.selectCnt;ii++){
			ss = wp.colStr(ii,"acct_type");
			for(int i = 0;i<sqlRowNum;i++){
				ss2 = sqlStr(i,"dtl_value");
				if(ss.equals(ss2)){
					wp.colSet(ii,"opt", "checked");
				}
			}
		}
	 }

	 void listWkdataM(String lsMchtGroup) throws Exception{
			if(empty(lsMchtGroup)){
				String ss = "",ss2="";
				String sqlSelect = "select "
						+ mchtNo
						+ " as mcht_no "
						+ " ,dtl_kind ,seq_no ,dtl_value "
						+ " from "
						+ tableNameTel
						+ " where "
						+ mchtNo
						+ " = :mcht_no "
						+ " and seq_no = :seq_no "
						+ " and dtl_kind ='MCHT-GROUP' ";
				setString("mcht_no",kkMchtNo);
				setString("seq_no",kkSeqNo);
				sqlSelect(sqlSelect);
				for(int ii = 0 ; ii<wp.selectCnt;ii++){
					ss = wp.colStr(ii,"mcht_no2");
					for(int i = 0;i<sqlRowNum;i++){
						ss2 = sqlStr(i,"dtl_value");
						if(ss.equals(ss2)){
							wp.colSet(ii,"opt", "checked");
							break;
						}
					}
				}
				wp.colSet("selectCnt",wp.selectCnt+"");
				return;
			}
			int selectCnt=0;
			String ss = "",ss2="";
			String sqlSelect="select mcht_no,mcht_chi_name "
					+ " from bil_merchant "
					+ " where mcht_no <> :kk_mcht_no "
					+ " and mcht_group_id = :ls_mcht_group "
					+ " order by mcht_no ";
			setString("kk_mcht_no",kkMchtNo);
			setString("ls_mcht_group",lsMchtGroup);
			sqlSelect(sqlSelect);
			String[] lsMchtNo = new String[sqlRowNum];
			for(int ii = 0;ii<sqlRowNum;ii++){
				ss2 = sqlStr(ii,"mcht_no");
				/*for(int i = 0 ; i<wp.selectCnt;i++){
				ss = wp.colStr(i,"mcht_no2");
					if(ss.equals(ss2)){
						break;
					}
				}
			
			/*	if(ss.equals(ss2)){
					continue;
				}*/
				lsMchtNo[selectCnt] = ss2;
				wp.colSet(selectCnt,"ser_num", String.format("%02d",selectCnt+1));
				wp.colSet(selectCnt,"mcht_no2", ss2);
				wp.colSet(selectCnt,"mcht_chi_name", sqlStr(ii,"mcht_chi_name"));
				selectCnt++;
			}
			wp.colSet("selectCnt",selectCnt);
			wp.listCount[0] = selectCnt;
			String sqlSelect2 = "select "
					+ mchtNo
					+ " as mcht_no "
					+ " ,dtl_kind ,seq_no ,dtl_value "
					+ " from "
					+tableNameTel
					+ " where "
					+ mchtNo
					+ " = :mcht_no "
					+ " and seq_no = :seq_no "
					+ " and dtl_kind ='MCHT-GROUP' ";
			setString("mcht_no",kkMchtNo);
			setString("seq_no",kkSeqNo);
			sqlSelect(sqlSelect2);
			for(int ii = 0 ; ii<selectCnt;ii++){
				ss = lsMchtNo[ii];
				for(int i = 0;i<sqlRowNum;i++){
					ss2 = sqlStr(i,"dtl_value");
					if(ss.equals(ss2)){
						wp.colSet(ii,"opt", "checked");
						break;
					}
				}
			}
		}
	 
	public void updateCG() throws Exception {
		Mktm4090Func func = new Mktm4090Func(wp);

		int llOk = 0, llErr = 0;
		String[] aaValue = wp.itemBuff("dtl_value");
		String[] aaOpt = wp.itemBuff("opt");
		String aaSeqNo = wp.itemStr2("rdm_seqno");
		String dtlKind = wp.itemStr2("dtl_kind");
		wp.listCount[0] = aaValue.length;
		func.varsSet("aa_seq_no", aaSeqNo);
		func.varsSet("aa_kind", dtlKind);
		// delete
		func.dbDelete2(dtlKind);

		for (int ll = 0; ll < aaValue.length; ll++) {
			
			func.varsSet("aa_value", aaValue[ll]);

			if (checkBoxOptOn(ll, aaOpt)) {
				llOk++;
				continue;
			}

			if (ll != Arrays.asList(aaValue).indexOf(aaValue[ll])) {
				continue;
			}

			if (func.dbInsert2() == 1) {
				llOk++;
			} else {
				llErr++;
			}
		}
		sqlCommit(llErr > 0 ? 0 : 1);
		alertMsg("存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
		selectCG();
	}
	
	public void updateA() throws Exception {
		Mktm4090Func func = new Mktm4090Func(wp);

		int  llErr = 0;
		String[] aaValue = wp.itemBuff("acct_type");
		String[] aaOpt = wp.itemBuff("opt");
		String aaSeqNo = wp.itemStr2("rdm_seqno");
		String dtlKind = wp.itemStr2("dtl_kind");
		wp.listCount[0] = aaValue.length;
		// delete
		func.varsSet("aa_kind", dtlKind);
		func.varsSet("aa_seq_no", aaSeqNo);
		func.dbDelete2(dtlKind);

		for (int ll = 0; ll < aaValue.length; ll++) {
			if (checkBoxOptOn(ll, aaOpt)) {
				func.varsSet("aa_value", aaValue[ll]);
				if (func.dbInsert2() != 1) {
					llErr++;
				}
			}
		
		}
		sqlCommit(llErr > 0 ? 0 : 1);
		alertMsg("存檔處理完成");
		if(llErr>0){
			alertMsg("存檔處理失敗");
		}
		selectA();
	}
	
	public void updateM() throws Exception {
		Mktm4090Func func = new Mktm4090Func(wp);
		String ss = "";
		int llErr = 0;
		String[] aaValue = wp.itemBuff("mcht_no2");
		String[] aaOpt = wp.itemBuff("opt");
		String aaSeqNo = wp.itemStr2("rdm_seqno");
		String dtlKind = wp.itemStr2("dtl_kind");
		wp.listCount[0] = aaValue.length;
		// delete
		func.varsSet("aa_kind", dtlKind);
		func.varsSet("aa_seq_no", aaSeqNo);
		func.dbDelete2(dtlKind);

		for (int ll = 0; ll < aaValue.length; ll++) {
			if (checkBoxOptOn(ll, aaOpt)) {
				func.varsSet("aa_value", aaValue[ll]);
				wfChkDwMcht(aaValue[ll]);
				//-check 複製-
				if (func.dbInsert2() != 1) {
					llErr++;
				}
			}
		}
		//-Select-
		if(llOpt==0){
			ss = "本活動不含同群組特店, 是否繼~續";
		}else{
			ss = "本活動含同群組特店, 是否繼~續";
		}
		/*javax.swing.JOptionPane.getRootFrame().setAlwaysOnTop(true);
		if(JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
			ss, "訊息",JOptionPane.YES_NO_OPTION)!=0){
			sql_commit(0);
			selectM();
			return;
		}*/
		//-重疊特店-
		if(llDupl>0){
			alertErr("活動期間有重疊之特店, 請重新輸入");
			sqlCommit(0);
			selectM();
			return;
		}
		
		sqlCommit(llErr > 0 ? 0 : 1);
		alertMsg("複製同群組特店成功");
		if(llErr>0){
			alertMsg("複製同群組特店失敗");
		}
		selectM();
	}
	int wfGetMcht2(String lsRdmMchtno) throws Exception{
		String rdmMchtnoKk =lsRdmMchtno;
		if(empty(rdmMchtnoKk)){
			alertErr("特店代號 不可空白");
			return -1;
		}
		String sqlSelect = "select mcht_chi_name,mcht_type,mcht_group_id FROM bil_merchant  WHERE mcht_no = :rdm_mchtno_kk ";
		setString("rdm_mchtno_kk", rdmMchtnoKk);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			alertErr("特店代號 不存在");
			return -1;
		}
		if(sqlStr("mcht_type").equals("1")){
			alertErr("ECS郵購特店, 不可用線上紅利扣抵");
			return -1;
		}
		wp.colSet("db_mchtname", sqlStr("mcht_chi_name"));
		wp.colSet("db_mchttype", empty(sqlStr("mcht_type")) ? "1" : sqlStr("mcht_type"));
		wp.colSet("db_mcht_group", sqlStr("mcht_group_id"));
		return 1;
	}
	
	public int wfGetMcht(TarokoCommon wr)throws Exception{
		super.wp = wr;
		String rdmMchtnoKk = wp.itemStr2("rdm_mchtno_kk");
		if(empty(rdmMchtnoKk)){
			alertErr("特店代號 不可空白");
			wp.addJSON("wf_get_mcht", "-1");
			return -1;
		}
		String sqlSelect = "select mcht_chi_name,mcht_type,mcht_group_id FROM bil_merchant  WHERE mcht_no = :rdm_mchtno_kk ";
		setString("rdm_mchtno_kk", rdmMchtnoKk);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			alertErr("特店代號 不存在");
			wp.addJSON("wf_get_mcht", "-1");
			return -1;
		}
		if(sqlStr("mcht_type").equals("1")){
			alertErr("ECS郵購特店, 不可用線上紅利扣抵");
			wp.addJSON("wf_get_mcht", "-1");
			return -1;
		}
		wp.addJSON("db_mchtname", sqlStr("mcht_chi_name"));
		wp.addJSON("db_mchttype", empty(sqlStr("mcht_type")) ? "1" : sqlStr("mcht_type"));
		wp.addJSON("db_mcht_group", sqlStr("mcht_group_id"));
		wp.addJSON("wf_get_mcht", "");
		return 1;
	}
	
	int ofValidation () throws Exception{
		String ss="", ss2="", lsCrtNo="";
		lsCrtNo = wp.itemStr2("crt_no");
		if(wfGetMcht2(wp.itemStr2("rdm_mchtno"))!=1){
			return -1;
		}
		if(!empty(lsCrtNo)){
			ss = wp.itemStr2("rdm_mchtno")+"-"+wp.itemStr2("rdm_seqno");
			if(!lsCrtNo.equals(ss)){
				alertErr("此為同群組特店複製參數, 請以原特店修改 ("+lsCrtNo+")");
				return -1;
			}
		}
		if(wp.itemStr2("wf_get_mcht").equals("-1")){
			return -1;
		}
		ss = wp.itemStr2("rdm_strdate");
		ss2 = wp.itemStr2("rdm_enddate");
		if (this.chkStrend(ss, ss2) == false) {
			alertErr("[活動期間-起迄]  輸入錯誤");
			return -1;
		}
		if(ss2.compareTo(wp.sysDate)<0){
			alertErr("活動期間 迄日須大於或等於系統日期");
			return -1;
		}
		
		if(wfDateRange()!=1){
			return -1;
		}
		if(wp.itemNum("rdm_discrate")==0&&wp.itemNum("rdm_discamt")==0){
			alertErr("最高比例/金額 不可同時為 0");
			return -1;
		}
		
		return 1;
	}
	
	int wfDateRange() throws Exception{
		String	sMchtno = "", sDate1 = "", sDate2 = "",lSeqno="";
		long lCnt=0;
		sMchtno = wp.itemStr2("rdm_mchtno");
		lSeqno = wp.itemStr2("rdm_seqno");
		sDate1 = wp.itemStr2("rdm_strdate");
		sDate2 = wp.itemStr2("rdm_enddate");
		String sqlSelect = "select count(*) as l_cnt from ptr_redeem "
				+ " where rdm_mchtno = :s_mchtno "
				+ " and rdm_seqno  <> :l_seqno "
				+ " and :s_date1 between rdm_strdate and rdm_enddate ";
		setString("s_mchtno",sMchtno);
		setString("l_seqno",lSeqno);
		setString("s_date1",sDate1);
		sqlSelect(sqlSelect);
		lCnt = longParseLong(sqlStr("l_cnt"));
		if(lCnt>0){
			alertErr("活動期間有重疊之特店, 請重新輸入");
			return -1;
		}
		
		sqlSelect = "select count(*) as l_cnt from ptr_redeem "
				+ " where rdm_mchtno = :s_mchtno "
				+ " and rdm_seqno  <> :l_seqno "
				+ " and :s_date2 between rdm_strdate and rdm_enddate ";
		setString("s_mchtno",sMchtno);
		setString("l_seqno",lSeqno);
		setString("s_date2",sDate2);
		sqlSelect(sqlSelect);
		lCnt = longParseLong(sqlStr("l_cnt"));
		if(lCnt>0){
			alertErr("活動期間有重疊之特店, 請重新輸入");
			return -1;
		}
		
		sqlSelect = "select count(*) as l_cnt from ptr_redeem_t "
				+ " where rdm_mchtno = :s_mchtno "
				+ " and rdm_seqno  <> :l_seqno "
				+ " and :s_date1 between rdm_strdate and rdm_enddate ";
		setString("s_mchtno",sMchtno);
		setString("l_seqno",lSeqno);
		setString("s_date1",sDate1);
		sqlSelect(sqlSelect);
		lCnt = longParseLong(sqlStr("l_cnt"));
		if(lCnt>0){
			alertErr("活動期間有重疊之特店, 請重新輸入");
			return -1;
		}
		
		sqlSelect = "select count(*) as l_cnt from ptr_redeem_t "
				+ " where rdm_mchtno = :s_mchtno "
				+ " and rdm_seqno  <> :l_seqno "
				+ " and :s_date2 between rdm_strdate and rdm_enddate ";
		setString("s_mchtno",sMchtno);
		setString("l_seqno",lSeqno);
		setString("s_date2",sDate2);
		sqlSelect(sqlSelect);
		lCnt = longParseLong(sqlStr("l_cnt"));
		if(lCnt>0){
			alertErr("活動期間有重疊之特店, 請重新輸入");
			return -1;
		}
		
		return 1;
	}
	void wfChkDwMcht(String lsMchtNo) throws Exception{
		String	lsCrtNo="",lsDate1="", lsDate2="";
		
		lsCrtNo = wp.itemStr2("crt_no");
		if(empty(lsCrtNo)){
			lsCrtNo = wp.itemStr2("rdm_mchtno")+"-00";
		}
		lsDate1 = wp.itemStr2("rdm_strdate");
		lsDate2 = wp.itemStr2("rdm_enddate");
		llOpt++;
		//-有效期間重疊--
		String sqlSelect = "select count(*) as ll_cnt "
				+ " from ptr_redeem "
				+ " where rdm_mchtno = :ls_mcht_no "
				+ " and crt_no <> :ls_crt_no "
				+ " and :ls_date1 between rdm_strdate and rdm_enddate ";
		setString("ls_mcht_no",lsMchtNo);
		setString("ls_crt_no",lsCrtNo);
		setString("ls_date1",lsDate1);
		sqlSelect(sqlSelect);
		if(this.toNum(sqlStr("ll_cnt"))>0){
			llDupl++;
		}
		
		sqlSelect = "select count(*) as ll_cnt "
				+ " from ptr_redeem "
				+ " where rdm_mchtno = :ls_mcht_no "
				+ " and crt_no <> :ls_crt_no "
				+ " and :ls_date2 between rdm_strdate and rdm_enddate ";
		setString("ls_mcht_no",lsMchtNo);
		setString("ls_crt_no",lsCrtNo);
		setString("ls_date2",lsDate2);
		sqlSelect(sqlSelect);
		if(this.toNum(sqlStr("ll_cnt"))>0){
			llDupl++;
		}
		
		sqlSelect = "select count(*) as ll_cnt "
				+ " from ptr_redeem_t "
				+ " where rdm_mchtno = :ls_mcht_no "
				+ " and crt_no <> :ls_crt_no "
				+ " and :ls_date1 between rdm_strdate and rdm_enddate ";
		setString("ls_mcht_no",lsMchtNo);
		setString("ls_crt_no",lsCrtNo);
		setString("ls_date1",lsDate1);
		sqlSelect(sqlSelect);
		if(this.toNum(sqlStr("ll_cnt"))>0){
			llDupl++;
		}
		
		sqlSelect = "select count(*) as ll_cnt "
				+ " from ptr_redeem_t "
				+ " where rdm_mchtno = :ls_mcht_no "
				+ " and crt_no <> :ls_crt_no "
				+ " and :ls_date2 between rdm_strdate and rdm_enddate ";
		setString("ls_mcht_no",lsMchtNo);
		setString("ls_crt_no",lsCrtNo);
		setString("ls_date2",lsDate2);
		sqlSelect(sqlSelect);
		if(this.toNum(sqlStr("ll_cnt"))>0){
			llDupl++;
		}
		return;
	}
	
	int wfChkDetail() throws Exception{
		String ss = "";
		String sqlSelect="select count(*) as l_cnt  from ptr_redeem_dtl1_t where mcht_no = :mcht_no and seq_no = :seq_no and dtl_kind = 'ACCT-TYPE' ";
		setString("mcht_no",wp.itemStr2("rdm_mchtno"));
		setString("seq_no",wp.itemStr2("rdm_seqno"));
		sqlSelect(sqlSelect);
		if(this.toNum(sqlStr("l_cnt"))<=0){
			ss+="N";
		}
		sqlSelect="select count(*) as l_cnt  from ptr_redeem_dtl1_t where mcht_no = :mcht_no and seq_no = :seq_no and dtl_kind = 'CARD-TYPE' ";
		setString("mcht_no",wp.itemStr2("rdm_mchtno"));
		setString("seq_no",wp.itemStr2("rdm_seqno"));
		sqlSelect(sqlSelect);
		if(this.toNum(sqlStr("l_cnt"))<=0){
			ss+="N";
		}
		sqlSelect="select count(*) as l_cnt  from ptr_redeem_dtl1_t where mcht_no = :mcht_no and seq_no = :seq_no and dtl_kind = 'GROUP-CODE' ";
		setString("mcht_no",wp.itemStr2("rdm_mchtno"));
		setString("seq_no",wp.itemStr2("rdm_seqno"));
		sqlSelect(sqlSelect);
		if(this.toNum(sqlStr("l_cnt"))<=0){
			ss+="N";
		}
		if(ss.equals("NNN")){
			alertErr("帳戶類別, 團代及卡種 不可全部空白");
			return -1;
		}
		
		return 1;
	}
	
	long longParseLong(String ss){
		long val = 0;
		try{
			val = Long.parseLong(ss);
		}catch (Exception ex) {
			val = 0;
		}
		return val;
	}
	
	void pdfPrint() throws Exception {
		wp.reportId = mProgName;
		//-cond-
		/*String ss = "生效年月: " + zzStr.ss_2ymd(wp.item_ss("ex_yymm1"))
		  + " -- " + zzStr.ss_2ymd(wp.item_ss("ex_yymm2"));
		wp.col_set("cond_1", ss);*/
		/*String ss2 = "回報日期: " + zzStr.ss_2ymd(wp.item_ss("ex_send_date1"))
		  + " -- " + zzStr.ss_2ymd(wp.item_ss("ex_send_date1"));
		wp.col_set("cond_2", ss2);*/
		wp.colSet("IdUser", wp.loginUser);
		wp.pageRows =99999;
		queryFunc();
	//	wp.setListCount(1);
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "N";
		pdf.excelTemplate = mProgName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 20;
		//pdf.pageVert= true;				//直印
		pdf.procesPDFreport(wp);
		pdf = null;
	}
	
	int mainTablecnt(String kk1,String kk2) throws Exception{
		wp.colSet("btnUp_disable", "style='background: lightgray;' disabled");
		String lsSql = "select count(*) as tot_cnt from ptr_redeem_t where rdm_mchtno = ? and rdm_seqno = ? ";
		Object[] param = new Object[] {kk1,kk2};
		sqlSelect(lsSql, param);
		
		return sqlInt("tot_cnt");
	}
}
