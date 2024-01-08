/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-22  V1.00.01  ryan       program initial                            *
* 109-07-03  V1.00.02  Andy       update:Mantis3715                          *
* 111-10-24  V1.00.03  Yang Bo    sync code from mega                        *
******************************************************************************/
package actm01;

import java.text.SimpleDateFormat;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actm2130 extends BaseEdit {
	Actm2130Func func;
	String kk1PSeqno = "";
	SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyyMMdd");
	String sdate = nowdate.format(new java.util.Date());

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		// ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
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
			if(empty(wp.itemStr("rowid"))){
				strAction = "A";
				insertFunc();
			}else{
				strAction = "U";
				updateFunc();
			}	
		} else if (eqIgno(wp.buttonCode, "item_changed")) {
			itemChanged();
		}

		dddwSelect();
		initButton();
	}
	void getWhereStr(){
		wp.whereStr = " where 1=1 and act_class_ex.p_seqno=act_acno.acno_p_seqno ";

//		if (empty(wp.item_ss("ex_acct_type")) == false && empty(wp.item_ss("ex_acct_no")) == false) {
//			wp.whereStr += " and  act_class_ex.acct_type = :ex_acct_type "
//					    + " and act_acno.acct_key = :ex_acct_no ";
//			setString("ex_acct_type", wp.item_ss("ex_acct_type"));
//			setString("ex_acct_no", wp.item_ss("ex_acct_no"));
//		}
		wp.whereStr +=sqlCol(wp.itemStr("ex_acct_type"),"act_class_ex.acct_type");
		wp.whereStr +=sqlCol(wp.itemStr("ex_acct_no"),"act_acno.acct_key");
		
	}
	
	@Override
	public void queryFunc() throws Exception {
		getWhereStr();
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " hex(act_class_ex.rowid) as rowid "
					+ " ,act_class_ex.p_seqno "
					+ " ,act_class_ex.acct_type "
					+ " ,act_acno.acct_key "
					+ " ,act_class_ex.class_code "
					+ " ,act_class_ex.value_s_date "
					+ " ,act_class_ex.value_e_date "
					+ " ,act_class_ex.mod_user "
					+ " ,act_class_ex.mod_time "
					+ " ,act_class_ex.mod_pgm "
					+ " ,act_class_ex.mod_seqno "
					;
					
		wp.daoTable = " act_class_ex,act_acno ";
		wp.whereOrder = " ";
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

	   // list_wkdata();
		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();

	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
		wfChkCname();
	}

	@Override
	public void dataRead() throws Exception {
		
		kk1PSeqno = itemKk("data_k1");
		wp.colSet("kk_acct_type", itemKk("data_k2"));
		wp.colSet("kk_acct_key", itemKk("data_k3"));
		
		wp.selectSQL = " hex(act_class_ex.rowid) as rowid "
				+ " ,act_class_ex.p_seqno "
				+ " ,act_class_ex.acct_type "
				+ " ,act_acno.acct_key "
				+ " ,act_class_ex.class_code "
				+ " ,act_class_ex.value_s_date "
				+ " ,act_class_ex.value_e_date "
				+ " ,act_class_ex.mod_user "
				+ " ,act_class_ex.mod_time "
				+ " ,act_class_ex.mod_pgm "
				+ " ,act_class_ex.mod_seqno "
				;

		wp.daoTable = " act_class_ex,act_acno ";
		wp.whereStr = " where 1=1 and act_class_ex.p_seqno=act_acno.acno_p_seqno ";
		if(!empty(kk1PSeqno)){
			wp.whereStr += " and  act_class_ex.p_seqno = :p_seqno ";
			setString("p_seqno", kk1PSeqno);
		}else{
			wp.whereStr += " and  act_class_ex.acct_type = :acct_type "
				    + " and act_acno.acct_key = :acct_key ";
			setString("acct_type", wp.itemStr("kk_acct_type"));
			setString("acct_key", wp.itemStr("kk_acct_key"));
		}
		pageSelect();
	}

	@Override
	public void saveFunc() throws Exception {
		func = new Actm2130Func(wp);
		
		if(ofValidation()!=1){
			return;
		}
		
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr2(func.getMsg());
		}else{
			alertMsg("修改完成");
		}
		this.sqlCommit(rc);
		
	}

	@Override
	public void initButton() {

	  if (wp.respHtml.indexOf("_detl")   > 0)   { 
			 this.btnModeAud("XX");
		}

	}

	@Override
	public void dddwSelect() {

		try {
			wp.optionKey = wp.itemStr("ex_acct_type");
			this.dddwList("dddw_ex_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
			
			wp.optionKey = wp.itemStr("kk_acct_type");
			this.dddwList("dddw_kk_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
			
		} catch (Exception ex) {
		}
	}
	
	void wfChkCname() throws Exception{
		String asCname ="",asCorpCname=""; 
		String sqlSelect=" select acct_type,acct_key,id_p_seqno,corp_p_seqno from  act_acno where acno_p_seqno = :as_p_seqno ";
		setString("as_p_seqno",wp.colStr("p_seqno"));
		sqlSelect(sqlSelect);
		if(sqlRowNum<=0){
			return ;
		}
		String isIdPSeqno = sqlStr("id_p_seqno");
		String isCorpPSeqno = sqlStr("corp_p_seqno");
		
		String sqlSelect2=" select chi_name from  crd_idno where id_p_seqno = :id_p_seqno ";
		setString("id_p_seqno",isIdPSeqno);
		sqlSelect(sqlSelect2);
		if(sqlRowNum>0){
			asCname = sqlStr("chi_name");
		}
		wp.colSet("chi_name", asCname);
		
		String sqlSelect3=" select chi_name from  crd_corp where corp_p_seqno = :corp_p_seqno ";
		setString("corp_p_seqno",isCorpPSeqno);
		sqlSelect(sqlSelect3);
		if(sqlRowNum>0){
			asCorpCname = sqlStr("chi_name");
		}
		wp.colSet("corp_name", asCorpCname);
		
		
	}
	int ofValidation() throws Exception{
		String lsVal1= "",lsVal2="",lsVal3="",lsVal4="",lsVal5="",lsVal6="",lsPSeqno="";

		lsVal1 = wp.itemStr("acct_type");
		lsVal2 = wp.itemStr("acct_key");
		if(empty(wp.itemStr("rowid"))){
			lsVal1 = wp.itemStr("kk_acct_type");
			lsVal2 = wp.itemStr("kk_acct_key");
		}
		
		//check 是否存在於 acno ---
		String sqlSelect="select acno_p_seqno, card_indicator,corp_act_flag,stmt_cycle "
						+ "from act_acno "
						+ "where acct_type = :ls_val1 "
						+ "and acct_key = :ls_val2 ";
		setString("ls_val1",lsVal1);
		setString("ls_val2",lsVal2);
		sqlSelect(sqlSelect);
		lsPSeqno = sqlStr("acno_p_seqno");
		lsVal3 = sqlStr("card_indicator");
		lsVal4 = sqlStr("corp_act_flag");
		lsVal5 = sqlStr("stmt_cycle");
		if(sqlRowNum<=0){
			alertErr("帳戶帳號不存在於帳戶主檔 !");
			return -1;
		}
		func.varsSet("ls_p_seqno", lsPSeqno);
		String sqlSelect2="select next_close_date from ptr_workday where  stmt_cycle = :ls_val5 ";
		setString("ls_val5",lsVal5);
		sqlSelect(sqlSelect2);
		if(sqlRowNum<=0){
			alertErr("Cycle不存在 !");
			return -1;
		}
		if(!empty(lsVal2)){
			lsVal6 = lsVal2.substring(lsVal2.length()-3,lsVal2.length());
		}
		if(lsVal3.equals("2")&&lsVal4.equals("Y")
				&&lsVal6.equals("000")){
			alertErr("此功能商務卡總繳只能定在總繳戶(000)上 !");
			return -1;
		}
		//檢核起迄日期--
		if(!empty(wp.itemStr("value_e_date"))){
			if(wp.itemStr("value_s_date").compareTo(wp.itemStr("value_e_date"))>0){
				alertErr("錯誤~,[起日]不可大於[迄日]");
				return -1;
			}
		}
		if(wp.itemStr("value_s_date").compareTo(wp.sysDate)<0){
			alertErr("錯誤~,[起始日期] 不可小於 [系統日期]");
			return -1;
		}
		//-check approve-
		 if (!checkApprove(wp.itemStr("zz_apr_user"),wp.itemStr("zz_apr_passwd")))
		 {
			 return -1;
		 }
		return 1;
	}
	
	void itemChanged() throws Exception{
		String isAcctType = wp.itemStr("kk_acct_type");
		String isAcctKey = wp.itemStr("kk_acct_key");
		String isAcnoPSeqno = "";
		String isIdPSeqno = "";
		String isCorpPSeqno = "";
		
		String lsSql = "select acno_p_seqno,corp_p_seqno,id_p_seqno from act_acno "
				+ "where acct_type =:acct_type "
				+ "and acct_key =:acct_key ";
		setString("acct_type",isAcctType);
		setString("acct_key",isAcctKey);
		sqlSelect(lsSql);
		if(sqlRowNum >0){
			wp.colSet("key_desp","");
			isAcnoPSeqno = sqlStr("acno_p_seqno");
			isIdPSeqno = sqlStr("id_p_seqno");
			isCorpPSeqno = sqlStr("corp_p_seqno");
		} else {
			wp.colSet("key_desp","查無帳戶帳號資料");
			wp.colSet("chi_name","");
			wp.colSet("corp_name","");
			return;
		}
		
		String sqlSelect2=" select chi_name from  crd_idno where id_p_seqno = :id_p_seqno ";
		setString("id_p_seqno",isIdPSeqno);
		sqlSelect(sqlSelect2);
		if(sqlRowNum>0){
			wp.colSet("chi_name", sqlStr("chi_name"));
		}
		
		if(!empty(isCorpPSeqno)){
			String sqlSelect3=" select chi_name from  crd_corp where corp_p_seqno = :corp_p_seqno ";
			setString("corp_p_seqno",isCorpPSeqno);
			sqlSelect(sqlSelect3);
			if(sqlRowNum>0){
				wp.colSet("corp_name", sqlStr("chi_name"));
			}
			
		}		
		
	}	

}
