/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-14  V1.00.01  ryan       program initial                            *
* 109-04-20  v1.00.02  Andy       Update add throws Exception                *
* 109-08-05  V1.00.03  Amber      Update add query_After                     *
* 109-08-27  V1.00.04  Amber      Update : apr_user check2    
* 112-02-16  V1.00.05  Machao     sync from mega & updated for project coding standard  
* 112-06-13  V1.00.06  Machao     顯示正確 01帳戶 ,  03帳戶 , 06帳戶, 調整        *
******************************************************************************/
package mktp02;

import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;


public class Mktp4090 extends BaseProc {

	int rr = -1;
	String msg = "",msgok=""; 
	String kk1 = "",kk2="";
	int ilOk = 0;
	int ilErr = 0;
	CommString commString = new CommString();
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
			// } else if (eq_igno(wp.buttonCode, "A")) {
			// /* 新增功能 */
			// insertFunc();
			// } else if (eq_igno(wp.buttonCode, "U")) {
			// /* 更新功能 */
			// updateFunc();
			// } else if (eq_igno(wp.buttonCode, "D")) {
			// /* 刪除功能 */
			// deleteFunc();
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
			/* 複製特店代號*/
			buttonclicked();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {

	}

	@Override
	public void dddwSelect() {
	}

	//for query use only
		private int getWhereStr() throws Exception {
			String lsDate1 = wp.itemStr2("ex_mchtno1");
			String lsDate2 = wp.itemStr2("ex_mchtno2");
			
			if (this.chkStrend(lsDate1, lsDate2) == false) {
				alertErr2("[特店代號-起迄]  輸入錯誤");
				return -1;
			}
			
			lsDate1 = wp.itemStr2("ex_date1");
			lsDate2 = wp.itemStr2("ex_date2");
			
			if (this.chkStrend(lsDate1, lsDate2) == false) {
				alertErr2("[建檔期間-起迄]  輸入錯誤");
				return -1;
			}
			
			wp.whereStr = " where 1=1 ";

			if (empty(wp.itemStr2("ex_date1")) == false) {
				wp.whereStr += " and crt_date >= :ex_date1 ";
				setString("ex_date1", wp.itemStr2("ex_date1"));
			}
			if (empty(wp.itemStr2("ex_date2")) == false) {
				wp.whereStr += " and crt_date <= :ex_date2 ";
				setString("ex_date2", wp.itemStr2("ex_date2"));
			}
			
			if (empty(wp.itemStr2("ex_mchtno1")) == false) {
				wp.whereStr += " and rdm_mchtno >= :ex_mchtno1 ";
				setString("ex_mchtno1", wp.itemStr2("ex_mchtno1"));
			}
			if (empty(wp.itemStr2("ex_mchtno2")) == false) {
				wp.whereStr += " and rdm_mchtno <= :ex_mchtno2 ";
				setString("ex_mchtno2", wp.itemStr2("ex_mchtno2"));
			}
			if (empty(wp.itemStr2("ex_userid")) == false) {
				wp.whereStr += " and crt_user = :ex_userid ";
				setString("ex_userid", wp.itemStr2("ex_userid"));
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
		wp.pageControl();

		wp.selectSQL = " hex(rowid) as rowid,"
				+" rdm_mchtno, "
				+" rdm_seqno, "   
		        +" rdm_strdate, "   
		        +" rdm_enddate, "   
		        +" rdm_destamt, "   
		        +" rdm_discrate, "   
		        +" rdm_discamt, "   
		        +" rdm_unitpoint, "   
		        +" rdm_unitamt, "   
		        +" rdm_binflag, "   
		        +" crt_user, "   
		        +" crt_date, "   
		        +" apr_user, "   
		        +" apr_date, "   
		        +" mod_user, "   
		        +" mod_time, "
		        +" mod_pgm, "
		        +" mod_seqno, "
		        +" mod_audcode, "
		        +" crt_no, "
		        +" rdm_discratefg "
		        ;   
		   
		wp.daoTable = " ptr_redeem_t ";
				//+ " on bil_merchant_kind_t.mcht_no = bil_merchant.mcht_no ";
		wp.whereOrder = "  ";
		if(getWhereStr()!=1)return;
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		//wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
		apprDisabled("mod_user");//20200827 add
	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		String kkRowid =wp.itemStr2("data_k1");

		wp.selectSQL =  " hex(rowid) as rowid,"
				+" rdm_mchtno, "
				+" rdm_seqno, "   
		        +" rdm_strdate, "   
		        +" rdm_enddate, "   
		        +" rdm_destamt, "   
		        +" rdm_discrate, "   
		        +" rdm_discamt, "   
		        +" rdm_unitpoint, "   
		        +" rdm_unitamt, "   
		        +" rdm_binflag, "   
		        +" crt_user, "   
		        +" crt_date, "   
		        +" apr_user, "   
		        +" apr_date, "   
		        +" mod_user, "   
		        +" mod_time, "
		        +" mod_pgm, "
		        +" mod_seqno, "
		        +" mod_audcode, "
		        +" crt_no, "
		        +" rdm_discratefg "
		        ;   
		   
		wp.daoTable = " ptr_redeem_t ";
		wp.whereOrder = "  ";
		wp.whereStr = " where 1=1 ";
		wp.whereStr += " and hex(rowid) = :kk_rowid ";
		setString("kk_rowid", kkRowid);

		pageSelect();
		if (sqlNotFind()) {
			alertErr("資料不存在 !");
			return;
		}
		listWkdata2();
	}

	@Override
	public void dataProcess() throws Exception {
		 //-check approve-
	/*	 if (!check_approve(wp.item_ss("zz_apr_user"),
		 wp.item_ss("zz_apr_passwd")))
		 {
		 return;
		 }*/
		String[] aaRowid = wp.itemBuff("rowid");
		String[] aaOpt = wp.itemBuff("opt");
		
		wp.listCount[0] = aaRowid.length;

		// -update-
		for (rr = 0; rr < aaRowid.length; rr++) {
			if (!checkBoxOptOn(rr, aaOpt)) {
				continue;
			}
			if (wfUpdFile() != 1) {
				wp.colSet(rr, "ok_flag", "!");
				sqlCommit(0);
				return;
			}

		}
	    sqlCommit(1);
	    queryFunc(); 
	    errmsg("資料處理成功!");
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}
	
	void listWkdata() throws Exception{
		String dbType01 = "",dbType03="",dbType06="";  //dbType05="",dbType02=""
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			dbType01 = wfGetActtype(wp.colStr(ii,"rdm_mchtno"),(int)wp.colNum(ii,"rdm_seqno"),"01");
			dbType03 = wfGetActtype(wp.colStr(ii,"rdm_mchtno"),(int)wp.colNum(ii,"rdm_seqno"),"03");
			dbType06 = wfGetActtype(wp.colStr(ii,"rdm_mchtno"),(int)wp.colNum(ii,"rdm_seqno"),"06");
//			dbType05 = wfGetActtype(wp.colStr(ii,"rdm_mchtno"),(int)wp.colNum(ii,"rdm_seqno"),"05");
//			dbType02 = wfGetActtype(wp.colStr(ii,"rdm_mchtno"),(int)wp.colNum(ii,"rdm_seqno"),"02");
			wp.colSet(ii,"db_type01", dbType01);
			wp.colSet(ii,"db_type03", dbType03);
			wp.colSet(ii,"db_type06", dbType06);
//			wp.colSet(ii,"db_type05", dbType05);
//			wp.colSet(ii,"db_type02", dbType02);
		}
	}
	
	void listWkdata2() throws Exception{
		String	lsVal="", lsDesc="",sqlSelect="";
		String rdmMchtno = wp.colStr("rdm_mchtno");
		String rdmSeqno = wp.colStr("rdm_seqno");
		wp.pageControl();
		wp.selectSQL = " dtl_kind,dtl_value";
		wp.daoTable = " ptr_redeem_dtl1_t ";
		wp.whereStr = " where 1=1 ";
		wp.whereStr += " and mcht_no = :rdm_mchtno ";
		setString("rdm_mchtno",rdmMchtno);
		wp.whereStr += " and seq_no = :rdm_seqno ";
		setString("rdm_seqno",rdmSeqno);
		wp.whereStr += " and dtl_kind <> 'MCHT-GROUP' ";
		wp.whereOrder = "  ";
		pageQuery();
		wp.setListCount(1);
		if (sqlRowNum<=0) {
			wp.notFound="N";
			wp.alertMesg = "<script language='javascript'> alert('異動資料不存在 !')</script>";
			return;
		}
		sqlSelect=" select mcht_chi_name,mcht_type from bil_merchant where mcht_no = :as_mchtno ";
		setString("as_mchtno",rdmMchtno);
		sqlSelect(sqlSelect);
		wp.colSet("db_mchttype",sqlStr("mcht_type"));
		wp.colSet("db_mchtname",sqlStr("mcht_chi_name"));
		int x = 0;
		int y = 0;
		int z = 0;
		for(int i=0 ;i<wp.selectCnt;i++){
			lsVal = wp.colStr(i,"dtl_value");
			switch(wp.colStr(i,"dtl_kind")){
			case "ACCT-TYPE":
				sqlSelect = "select chin_name FROM ptr_acct_type WHERE acct_type = :ls_val ";
				setString("ls_val",lsVal);
				sqlSelect(sqlSelect);
				lsDesc = sqlStr("chin_name");
				if(x == 0){
					wp.colSet(i,"tt_dtl_kind", "卡種");
				}
				x++;
				break;
			case "GROUP-CODE":
				sqlSelect = "select group_name FROM ptr_group_code WHERE group_code = :ls_val ";
				setString("ls_val",lsVal);
				sqlSelect(sqlSelect);
				lsDesc = sqlStr("group_name");
				if(y == 0){
					wp.colSet(i,"tt_dtl_kind", "團體代號");
				}
				y++;
				break;
			case "CARD-TYPE":
				sqlSelect = "select name FROM ptr_card_type WHERE card_type = :ls_val ";
				setString("ls_val",lsVal);
				sqlSelect(sqlSelect);
				lsDesc = sqlStr("name");
				if(z == 0){
					wp.colSet(i,"tt_dtl_kind", "帳戶類別");
					
				}
				z++;
				break;
			}
			wp.colSet(i,"db_dtldesc", lsDesc);
		}
	}
	
	String wfGetActtype(String asMchtno, int asSeqno, String asType) throws Exception{

		String sqlSelect="select dtl_value01 from ( "
				+ " select '1' as key,dtl_value as dtl_value01 "
				+ " from PTR_REDEEM_DTL1_T where  dtl_kind <> 'MCHT-GROUP' "
				+ " and MCHT_NO=:as_mchtno "
				+ " and seq_no=:as_seqno "
				+ " and dtl_value=:as_type ) as a ";
		setString("as_mchtno",asMchtno);
		setString("as_seqno",Integer.toString(asSeqno));
		setString("as_type",asType);
		sqlSelect(sqlSelect);
		String sActtype = sqlStr("dtl_value01");
		if(sqlRowNum<=0){
			return "";
		}
		if(empty(sActtype)){
			return "";
		}
		return "Y";
	}
	
	void buttonclicked() throws Exception{
		String[] aaRowid = wp.itemBuff("rowid");
		wp.listCount[0] = aaRowid.length;
		wp.selectSQL = " ptr_redeem_dtl1_t.dtl_value,"
				+" bil_merchant.mcht_chi_name "
		        ;   
		   
		wp.daoTable = " bil_merchant,ptr_redeem_dtl1_t ";
		wp.whereStr = " where ( bil_merchant.mcht_no = ptr_redeem_dtl1_t.dtl_value ) and "
				+ " ( ( ptr_redeem_dtl1_t.mcht_no = :as_mcht_no ) AND "
				+ " ( ptr_redeem_dtl1_t.dtl_kind = 'MCHT-GROUP' ) AND "
				+ " ( ptr_redeem_dtl1_t.seq_no = :al_seq_no ) ) ";
		setString("as_mcht_no",wp.itemStr2("data_k2"));
		setString("al_seq_no",wp.itemStr2("data_k3"));
		wp.whereOrder = "  ";
		pageQuery();
		wp.setListCount(2);
		wp.notFound = "N";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wp.colSet(ii,"cbc_mcht",wp.colStr(ii,"dtl_value")+"["+wp.colStr(ii,"mcht_chi_name")+"]");
		}
		wp.colSet("data_k4","Y");
		//wp.totalRows = wp.dataCnt;
		//wp.listCount[1] = wp.dataCnt;
		//wp.setPageValue();
	}
	
	int wfUpdFile() throws Exception{
		busi.SqlPrepare sp = new SqlPrepare();
		String[] sMchtno =wp.itemBuff("rdm_mchtno");
		String[] lSeqno =wp.itemBuff("rdm_seqno");
		String[] isCrtNo =wp.itemBuff("crt_no");
		String[] aaCrtUser =wp.itemBuff("crt_user");
		String[] aaCrtDate =wp.itemBuff("crt_date");
		String[] aaRdmStrdate =wp.itemBuff("rdm_strdate");
		String[] aaRdmEnddate =wp.itemBuff("rdm_enddate");
		String[] aaRdmDestamt =wp.itemBuff("rdm_destamt");
		String[] aaRdmDiscrate =wp.itemBuff("rdm_discrate");
		String[] aaRdmDiscamt =wp.itemBuff("rdm_discamt");
		String[] aaRdmUnitpoint =wp.itemBuff("rdm_unitpoint");
		String[] aaRdmUnitamt =wp.itemBuff("rdm_unitamt");
		String[] aaRdmBinflag =wp.itemBuff("rdm_binflag");
		String[] aaRdmDiscratefg =wp.itemBuff("rdm_discratefg");
		
		if(empty(isCrtNo[rr])){
			isCrtNo[rr] = sMchtno[rr]+"-"+lSeqno[rr];
		}
		//-Delete 原同批參數-
		if(wfMchtGroupDelete(isCrtNo[rr])!=1){
			return -1;
		}
		
		//-Delete 重疊之特店-
		if(wfMchtDuplDelete()!=1){
			return -1;
		}
		
		String sqlSelect = "select count(*) as cnt from ptr_redeem where rdm_mchtno = :s_mchtno and rdm_seqno = :l_seqno ";
		setString("s_mchtno",sMchtno[rr]);
		setString("l_seqno",lSeqno[rr]);
		sqlSelect(sqlSelect);
		if(sqlNum("cnt")<=0){
			sp.sql2Insert("ptr_redeem");
			sp.ppstr("rdm_mchtno",sMchtno[rr]);
			sp.ppstr("rdm_seqno",Integer.toString(this.toInt(lSeqno[rr])));
			sp.ppstr("crt_user",aaCrtUser[rr]);
			sp.ppstr("crt_date",aaCrtDate[rr]);
			sp.ppstr("rdm_strdate", aaRdmStrdate[rr]);
			sp.ppstr("rdm_enddate", aaRdmEnddate[rr]);
			sp.ppstr("rdm_destamt", Integer.toString(this.toInt(aaRdmDestamt[rr])));
			sp.ppnum("rdm_discrate", this.toNum(aaRdmDiscrate[rr]));//this.to_Num(aa_rdm_discrate[rr])
			sp.ppstr("rdm_discamt", Integer.toString(this.toInt(aaRdmDiscamt[rr])));
			sp.ppstr("rdm_unitpoint", Integer.toString(this.toInt(aaRdmUnitpoint[rr])));
			sp.ppstr("rdm_unitamt", Integer.toString(this.toInt(aaRdmUnitamt[rr])));
			sp.ppstr("rdm_binflag", aaRdmBinflag[rr]);
			sp.ppstr("rdm_discratefg", aaRdmDiscratefg[rr]);
			sp.ppstr("crt_no", isCrtNo[rr]);
			sp.ppstr("apr_user", wp.loginUser);
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.addsql(", mod_seqno ",", 1 ");
			sp.addsql(", mod_time ",", sysdate ");
			sp.addsql(", apr_date ",", to_char(sysdate,'YYYYMMDD') ");
		}else{
			sp.sql2Update("ptr_redeem");
			sp.addsql(", mod_seqno =mod_seqno+1", ", mod_time = sysdate");
			sp.addsql(", apr_date = to_char(sysdate,'YYYYMMDD') ");
			sp.ppstr("rdm_strdate", aaRdmStrdate[rr]);
			sp.ppstr("rdm_enddate", aaRdmEnddate[rr]);
			sp.ppstr("rdm_destamt", Integer.toString(this.toInt(aaRdmDestamt[rr])));
			sp.ppnum("rdm_discrate", this.toNum(aaRdmDiscrate[rr]));
			sp.ppstr("rdm_discamt", Integer.toString(this.toInt(aaRdmDiscamt[rr])));
			sp.ppstr("rdm_unitpoint", Integer.toString(this.toInt(aaRdmUnitpoint[rr])));
			sp.ppstr("rdm_unitamt", Integer.toString(this.toInt(aaRdmUnitamt[rr])));
			sp.ppstr("rdm_binflag", aaRdmBinflag[rr]);
			sp.ppstr("rdm_discratefg", aaRdmDiscratefg[rr]);
			sp.ppstr("crt_no", isCrtNo[rr]);
			sp.ppstr("apr_user", wp.loginUser);
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.sql2Where(" where rdm_mchtno=?", sMchtno[rr]);
			sp.sql2Where(" and rdm_seqno=?",lSeqno[rr]);
		}
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if(sqlRowNum<=0){
			alertErr("update PTR_REDEEM error");
			return -1;
		}
		//--Update PTR_REDEEM_DTL1----------------------------------------------
		
		sqlSelect="select count(*) as cnt from ptr_redeem_dtl1 Where MERCHANT_NO = :s_mchtno and seq_no = :l_seqno ";
		setString("s_mchtno",sMchtno[rr]);
		setString("l_seqno",lSeqno[rr]);
		sqlSelect(sqlSelect);
	
		if(sqlNum("cnt")>0){
			String sqlDelete = "Delete From ptr_redeem_dtl1 Where MERCHANT_NO = :s_mchtno and seq_no = :l_seqno ";
			setString("s_mchtno",sMchtno[rr]);
			setString("l_seqno",lSeqno[rr]);
	
			sqlExec(sqlDelete);
			if(sqlCode<0){
				alertErr("Delete ptr_redeem_dtl1 error");
				return -1;
			}
		}

		String sqlInsert = "INSERT INTO ptr_redeem_dtl1  ( MERCHANT_NO, seq_no, dtl_kind, dtl_value ) "
				+ " SELECT mcht_no, seq_no, dtl_kind, dtl_value "
				+ " FROM ptr_redeem_dtl1_t "
				+ " Where mcht_no = :s_mchtno AND seq_no = :l_seqno "
				+ " AND dtl_kind <> 'MCHT-GROUP' ";
		setString("s_mchtno",sMchtno[rr]);
		setString("l_seqno",lSeqno[rr]);
		sqlExec(sqlInsert);
		if(sqlCode<0){
			alertErr("INSERT ptr_redeem_dtl1 error");
			return -1;
		}
		//-Copy 同群組特店-
		if(wfMchtGroupCopy()!=1){
			return -1;
		}
		//--Delete Temp-File-
		String sqlDelete = "Delete From ptr_redeem_t Where rdm_mchtno = :s_mchtno and rdm_seqno = :l_seqno";
		setString("s_mchtno",sMchtno[rr]);
		setString("l_seqno",lSeqno[rr]);
		sqlExec(sqlDelete);
		if(sqlCode<0){
			alertErr("Delete ptr_redeem_t error");
			return -1;
		}
		
		
		sqlDelete = "Delete From ptr_redeem_dtl1_t Where MCHT_NO = :s_mchtno and seq_no = :l_seqno";
		setString("s_mchtno",sMchtno[rr]);
		setString("l_seqno",lSeqno[rr]);
		sqlExec(sqlDelete);
		if(sqlCode<0){
			alertErr("Delete ptr_redeem_dtl1_t error");
			return -1;
		}
		
		return 1;
	}
	
	int wfMchtGroupDelete(String isCrtNo) throws Exception{
		if(empty(isCrtNo)){
			return 1;
		}
		String sqlSelect = "select rdm_mchtno, rdm_seqno from ptr_redeem where crt_no = :is_crt_no ";
		setString("is_crt_no",isCrtNo);
		sqlSelect(sqlSelect);
		if(sqlRowNum<=0){
			return 1;
		}
		int sqlnrow  = sqlRowNum;
		//-Delete ptr_redeem_dtl1-
		for(int i = 0;i<sqlnrow;i++ ){
			String sqlDelete = "Delete From PTR_REDEEM_DTL1 Where MERCHANT_NO = :ls_mcht_no AND seq_no = :ll_seqno ";
			setString("ls_mcht_no",sqlStr(i,"rdm_mchtno"));
			setString("ll_seqno",sqlStr(i,"rdm_seqno"));
			sqlExec(sqlDelete);
			if(sqlRowNum<0){
				alertErr("Delete PTR_REDEEM_DTL1 error");
				return -1;
			}
		}
		//-Delete ptr_redeem-
		String sqlDelete = "Delete From ptr_redeem Where crt_no = :is_crt_no ";
		setString("is_crt_no",isCrtNo);
		sqlExec(sqlDelete);
		if(sqlRowNum<0){
			alertErr("Delete PTR_REDEEM error");
			return -1;
		}

		return 1;
	}
	
	int wfMchtDuplDelete() throws Exception{
		String llSeqno = "",llMchtNo="";
		String[] lsMchtNo =wp.itemBuff("rdm_mchtno");
		String[] lsSeqno =wp.itemBuff("rdm_seqno");
		String[] lsDate1 =wp.itemBuff("rdm_strdate");
		String[] lsDate2 =wp.itemBuff("rdm_enddate");
		String sqlSelect = "select rdm_mchtno, rdm_seqno from ptr_redeem "
				+ " where rdm_mchtno = :ls_mcht_no "
				+ " and (:ls_date1 between rdm_strdate and rdm_enddate "
				+ " or :ls_date2 between rdm_strdate and rdm_enddate ) ";
		setString("ls_mcht_no",lsMchtNo[rr]);
		setString("ls_date1",lsDate1[rr]);
		setString("ls_date2",lsDate2[rr]);
		sqlSelect(sqlSelect);
		//-Delete by master-
		for(int i = 0;i<sqlRowNum;i++ ){
			llSeqno = sqlStr(i,"rdm_seqno");
			String sqlDelete = " delete from ptr_redeem_dtl1 where MERCHANT_NO = :ls_mcht_no and seq_no = :ll_seqno";
			setString("ls_mcht_no",lsMchtNo[rr]);
			setString("ll_seqno",llSeqno);
			sqlExec(sqlDelete);
		
			if(sqlRowNum<0){
				alertErr("Delete PTR_REDEEM_DTL1 error");
				return -1;
			}
			//-Delete ptr_redeem-
			sqlDelete = "Delete From ptr_redeem Where rdm_mchtno = :ls_mcht_no and rdm_seqno = :ll_seqno";
			setString("ls_mcht_no",lsMchtNo[rr]);
			setString("ll_seqno",llSeqno);
			sqlExec(sqlDelete);
			if(sqlRowNum<0){
				alertErr("Delete PTR_REDEEM error");
				return -1;
			}
		}
		//-Delete by detail-
		sqlSelect = "select rdm_mchtno, rdm_seqno from ptr_redeem "
				+ " where rdm_mchtno in (select dtl_value from ptr_redeem_dtl1_t "
				+ " where MCHT_NO = :ls_mcht_no "
				+ " and seq_no = :ls_seqno "
				+ " and dtl_kind='MCHT-GROUP' ) "
				+ " and ( :ls_date1 between rdm_strdate and rdm_enddate "
				+ " or :ls_date2 between rdm_strdate and rdm_enddate ) ";
		setString("ls_mcht_no",lsMchtNo[rr]);
		setString("ls_seqno",lsSeqno[rr]);
		setString("ls_date1",lsDate1[rr]);
		setString("ls_date2",lsDate2[rr]);
		sqlSelect(sqlSelect);
		int sqlnrow = sqlRowNum;
		for(int i = 0;i<sqlnrow;i++ ){
			llMchtNo = sqlStr(i,"rdm_mchtno");
			llSeqno = sqlStr(i,"rdm_seqno");
			
			//-Delete ptr_redeem_dtl1-
			String sqlDelete = " delete from ptr_redeem_dtl1 where MERCHANT_NO = :ll_mcht_no and seq_no = :ll_seqno";
			setString("ll_mcht_no",llMchtNo);
			setString("ll_seqno",llSeqno);
			sqlExec(sqlDelete);
		
			if(sqlRowNum<0){
				alertErr("Delete PTR_REDEEM_DTL1 error");
				return -1;
			}
			
			//-Delete ptr_redeem-
			sqlDelete = "Delete From ptr_redeem Where rdm_mchtno = :ll_mcht_no and rdm_seqno = :ll_seqno";
			setString("ll_mcht_no",llMchtNo);
			setString("ll_seqno",llSeqno);
			sqlExec(sqlDelete);
			if(sqlRowNum<0){
				alertErr("Delete PTR_REDEEM error");
				return -1;
			}
		}
		
		return 1;
	}
	
	int wfMchtGroupCopy() throws Exception{
		String[] lsMchtNo =wp.itemBuff("rdm_mchtno");
		String[] llSeqno =wp.itemBuff("rdm_seqno");
		String lsDtlMcht="";
		double llMaxseq = 0; 
		//-Read mcht-group-
		String sqlSelect = "select ptr_redeem_dtl1_t.dtl_value "
				+ " FROM bil_merchant,ptr_redeem_dtl1_t "
				+ " WHERE ( bil_merchant.mcht_no = ptr_redeem_dtl1_t.dtl_value ) "
				+ " and ( ( ptr_redeem_dtl1_t.mcht_no = :as_mcht_no ) "
				+ " AND ( ptr_redeem_dtl1_t.dtl_kind = 'MCHT-GROUP' ) "
				+ " AND ( ptr_redeem_dtl1_t.seq_no = :al_seq_no ) ) ";
		setString("as_mcht_no",lsMchtNo[rr]);
		setString("al_seq_no",llSeqno[rr]);
		sqlSelect(sqlSelect);
		int sqlnrow = sqlRowNum;
		for(int i = 0;i<sqlnrow;i++ ){
			lsDtlMcht = sqlStr(i,"dtl_value");
			if(empty(lsDtlMcht)){
				continue;
			}
			if(lsDtlMcht.equals(lsMchtNo[rr])){
				continue;
			}
			//-get max-seqno-
			sqlSelect = " select max(rdm_seqno) as ll_maxseq From ptr_redeem where	rdm_mchtno =:ls_dtl_mcht ";
			setString("ls_dtl_mcht",lsDtlMcht);
			sqlSelect(sqlSelect);
			llMaxseq = sqlNum("ll_maxseq");
			llMaxseq += 1;
			//-copy ptr_redeem-
			String sqlInsert="INSERT INTO ptr_redeem ( "
							+ " rdm_mchtno "
							+ " ,rdm_seqno "
							+ " ,rdm_strdate "
							+ " ,rdm_enddate "
							+ " ,rdm_destamt "
							+ " ,rdm_discrate "
							+ " ,rdm_discamt "
							+ " ,rdm_unitpoint "
							+ " ,rdm_unitamt "
							+ " ,rdm_binflag "
							+ " ,rdm_discratefg "
							+ " ,crt_user "
							+ " ,crt_date "
							+ " ,apr_user "
							+ " ,apr_date "
							+ " ,crt_no "
							+ " ,mod_user "
							+ " ,mod_time "
							+ " ,mod_pgm "
							+ " ,mod_seqno ) "
							+ " SELECT :ls_dtl_mcht"
							+ " , :ll_maxseq "
							+ " ,rdm_strdate "
							+ " ,rdm_enddate "
							+ " ,rdm_destamt "
							+ " ,rdm_discrate "
							+ " ,rdm_discamt "
							+ " ,rdm_unitpoint "
							+ " ,rdm_unitamt "
							+ " ,rdm_binflag "
							+ " ,rdm_discratefg "
							+ " ,crt_user "
							+ " ,crt_date "
							+ " ,apr_user "
							+ " ,apr_date "
							+ " ,crt_no "
							+ " ,mod_user "
							+ " ,mod_time "
							+ " ,mod_pgm "
							+ " ,mod_seqno "
							+ " FROM ptr_redeem "
							+ " WHERE rdm_mchtno = :ls_mcht_no "
							+ " AND rdm_seqno = :ll_seqno ";
			setString("ls_dtl_mcht",lsDtlMcht);
			setString("ll_maxseq",Double.toString(llMaxseq));
			setString("ls_mcht_no",lsMchtNo[rr]);
			setString("ll_seqno",llSeqno[rr]);
			sqlExec(sqlInsert);
			if(sqlRowNum<=0){
				alertErr("mcht_group_copy: insert ptr_redeem error");
				return -1;
			}
			
			//-copy ptr_redeem-
			sqlInsert="INSERT INTO ptr_redeem_dtl1 "
					+ "  ( MERCHANT_NO, seq_no, dtl_kind, dtl_value ) "
					+ "  SELECT :ls_dtl_mcht, :ll_maxseq, dtl_kind, dtl_value "
					+ " FROM ptr_redeem_dtl1_t "
					+ " WHERE mcht_no = :ls_mcht_no AND seq_no = :ll_seqno ";
			setString("ls_dtl_mcht",lsDtlMcht);
			setString("ll_maxseq",Double.toString(llMaxseq));
			setString("ls_mcht_no",lsMchtNo[rr]);
			setString("ll_seqno",llSeqno[rr]);
			sqlExec(sqlInsert);
			if(sqlRowNum<=0){
				alertErr("mcht_group_copy: Insert ptr_redeem_dtl1 error");
				return -1;
			}
		}
		
		return 1;
	}
	
}
