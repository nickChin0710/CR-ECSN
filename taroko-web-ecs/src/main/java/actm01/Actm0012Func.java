/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 107-08-16  V1.00.01  Alex       bug fixed                                  *
* 2022-0520  V1.01.00  JH	        mt-9406                                    *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-12  V1.00.04  Simon      1.Add column CURR_CHANGE_ACCOUT maintenance*
*                                 2.update autopay_acct_no data into act_acct_curr*
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import busi.func.SmsMsgDetl;
import taroko.com.TarokoCommon;

public class Actm0012Func extends FuncEdit {
    String mAccttype="";
    String mAcctkey="";
    String mCurrcode = "";
    String kk1 = "";
    String hNextCloseDate = "";
    taroko.base.CommDate commDate = new taroko.base.CommDate();

    String isOldBank="", isOldAcctno="", isOldIdno="";

	public Actm0012Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {
		
		String lsDbData = "" , lsScData = "" , lsApayId = "", lsFromMark = "", lsAutopayDcFlag="";
		
		// check PK
		mAccttype = wp.itemStr2("acct_type");
		mAcctkey  = wp.itemStr2("acct_key");		
		mCurrcode = wp.itemStr2("curr_code");
		kk1 = wp.itemStr2("rowid");
		lsFromMark = wp.itemStr2("from_mark");
						
		if (this.isAdd()){
			//檢查新增資料是否重複
			kk1 = wp.itemStr2("p_seqno");
			String lsSql = "select count(*) as tot_cnt from act_chkno where p_seqno = ? and curr_code = ? and from_mark = ? "
			             + " and ad_mark <> 'D' and proc_mark <> 'Y' ";
			Object[] param = new Object[] { kk1, mCurrcode, lsFromMark };
			sqlSelect(lsSql, param);
	
			if (colNum("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增");
			}			
		} else {
			//-other modify-
			sqlWhere = "where hex(rowid) = ? "
					  + "and nvl(mod_seqno,0) = ? ";
			Object[] param = new Object[] { kk1, wp.modSeqno() };
			isOtherModify("act_chkno", sqlWhere, param);
		}
		
		if(isUpdate() || isDelete()){
			if(wp.itemEq("exec_check_flag", "Y")){
				errmsg("主管已覆核不可修改或刪除資料 !");
				return;
			}
		}
		
		if(isDelete())	return ;
		
		if(!eqIgno(commString.mid(wp.itemStr2("autopay_acct_bank"), 0,3),"006")){			
			errmsg("外幣自扣帳號 須為本行帳號");
			return ;
		}
		
		if(wp.itemStr2("autopay_acct_bank").length()<7){
			errmsg("扣繳行庫長度不足 7 碼 !!");
			return ;
		}
		
		if(wp.itemStr2("autopay_acct_no").length()!=16){
			errmsg("外幣-扣繳帳號長度不足16碼!!");
			return ;
		}
		
  //wfChkBank()為檢核扣繳行庫為本行, 則CYCLE當日或前一營業日; 不可異動資料及發送簡訊
	//if(wfChkBank()!=1);
		
		lsDbData = selectActAcctCurr();
		if(empty(lsDbData)){
			errmsg("select act_acct_curr check error");
			return ;
		}
		
    if ( wp.itemStr2("autopay_dc_flag").equals("Y") ) {  //checkbox value converted
    	lsAutopayDcFlag = "Y";
    }
    else {
    	lsAutopayDcFlag = "N";
    }
   
	//ls_sc_data = wp.item_ss("autopay_acct_bank").substring(0, 3)+
		lsScData = wp.itemStr2("autopay_acct_bank")+
      				 wp.itemStr2("autopay_acct_no")+
      				 wp.itemStr2("autopay_indicator")+
      				 wp.itemStr2("autopay_id")+
      				 wp.itemStr2("autopay_id_code")+
      			 //wp.item_ss("autopay_dc_flag")+
      				 lsAutopayDcFlag+
      				 wp.itemStr2("exchange_acct_no")+
      				 wp.itemStr2("autopay_dc_indicator");
		
		if(eqIgno(lsDbData,lsScData)){
			errmsg("非帳號取消作業者, 未更改 自動扣繳帳號, 不可存檔 !");
			return ;
		}
		
//		//--check 帳號--
//		if iuc_acno.of_chk_bankno(dw_data.item(1,'autopay_acct_no'))<>1 then
//			f_errmsg("扣繳帳號輸入錯誤~ !")
//			Return -1
//		end if
		
		lsApayId = wp.itemStr2("autopay_id");
		String sql1 = "";
		if(lsApayId.length()==8){
			sql1 = " select "
				  + " count(*) as db_cnt "
				  + " from act_acno A , crd_corp B "
				  + " where B.corp_no = ? "
				  + " and A.corp_p_seqno = B.corp_p_seqno "
				  + " and A.acct_type = ? "
				  + " and A.acct_key = ? "
				  ;
			
			sqlSelect(sql1,new Object[]{lsApayId,mAccttype,mAcctkey});
			
		}	else {
			sql1 = " select "
				  + " count(*) as db_cnt "
				  + " from act_acno A , crd_idno B "
				  + " where B.id_no = ? "
				  + " and A.id_p_seqno = B.id_p_seqno "
				  + " and A.acct_type = ? "
				  + " and A.acct_key = ? "
				  ;
			
			sqlSelect(sql1,new Object[]{lsApayId,mAccttype,mAcctkey});
		}
		
		if(colNum("db_cnt")==0){
			errmsg("外幣帳號歸屬ID 非本人, 不可存檔");
			return ;
		}
		
		if(wp.itemEq("autopay_dc_flag", "Y")){
			if(wp.itemEmpty("exchange_acct_no")){
				errmsg("台幣換匯帳號為空白, 不可點選 [外幣存款不足轉扣台幣]");
				return;
			}
			
	  	if(wp.itemStr2("exchange_acct_no").length()!=16){
		  	errmsg("外幣-台幣換匯帳號長度不足16碼!!");
			  return ;
		  }
			
			//if(eqIgno(commString.mid(wp.itemStr2("autopay_acct_bank"), 0,3),"017")==false){
			//	errmsg("台幣自扣帳號非本行, 不可點選 [外幣存款不足轉扣台幣]");
			//	return ;
			//}
			
			//if(!wp.itemEq("acno_autopay_id", wp.itemStr2("id_no"))){
			//	errmsg("台幣帳號非本人, 不可點選 [外幣存款不足轉扣台幣]");
			//	return ;
			//}
			
			if(wp.itemEq("autopay_indicator", "2") && wp.itemEq("autopay_dc_indicator", "1")){
				errmsg("自動扣繳指示碼選MP者, 其外幣存款不足轉扣台幣指示碼不可點選 [TTL]");
				return ;
			}									
		}
		
		if(wp.itemEq("sms_send_flag", "Y")){
			try {
				if (wfCheckSmsSend("1")==-1) return ;
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}
		
	}
	
	int wfChkBank() {
		String lsPSeqno = "" , lsBank = "" , lsBusDate = "" , lsCycleDatePrev = "";
		lsPSeqno = wp.itemStr2("p_seqno");
		lsBank = commString.mid(wp.itemStr2("autopay_acct_bank"), 0,3);
		if(!eqIgno(lsBank,"006"))	return 1;
		
		String sql1 = " select "
						+ " business_date "
						+ " from ptr_businday "
						+ " where 1=1 "
						;
		
		sqlSelect(sql1);
		
		lsBusDate = colStr("business_date");
		lsCycleDatePrev = wfCycleDatePrev(lsPSeqno);
		
		if(empty(lsCycleDatePrev)){
			errmsg("無法取得 CYCLE當日或前一營業日 之日期");
			return -1;
		}
				
	//if(ls_bus_date.compareTo(ls_cycle_date_prev)>=0){
	//if ((ls_bus_date.compareTo(ls_cycle_date_prev)=0) || (ls_bus_date.compareTo(h_next_close_date)=0))  {
	  if ( (lsBusDate.equals(lsCycleDatePrev)) || (lsBusDate.equals(hNextCloseDate)) ) {
			errmsg("扣繳行庫為本行(006), CYCLE當日或前一營業日; 不可異動資料及發送簡訊");
			return -1;
		}
		
		return 1;
	}
	
	String wfCycleDatePrev(String lsPSeqno) {
		String llCycleDatePrev = ""; double llCnt = 0;
		String sql1 = " select "
						+ " a.next_close_date, "
						+ " uf_date_add(a.next_close_date,0,0,-1) as ll_cycle_date_prev "
						+ " from ptr_workday a, act_acno b "
						+ " where a.stmt_cycle = b.stmt_cycle "
						+ " and b.acno_p_seqno = ? "
						;
		
		sqlSelect(sql1,new Object[]{lsPSeqno});
		if(sqlRowNum<=0)	return "";
		
		hNextCloseDate  = colStr("next_close_date");
		llCycleDatePrev = colStr("ll_cycle_date_prev");
		
		String sql2 = " select "
						+ " count(*) as ll_cnt "
						+ " from ptr_holiday "
						+ " where holiday = ? "
						;
		
		while(true){
			sqlSelect(sql2,new Object[]{llCycleDatePrev});
			if(sqlRowNum<=0){
				return "";
			}
			llCnt = colNum("ll_cnt");
			if(llCnt==0)	break;
			
			llCycleDatePrev = commDate.dateAdd(llCycleDatePrev, 0, 0, -1);
		}
		
		return llCycleDatePrev;
	}
	
	int wfCheckSmsSend(String asType) throws Exception{
		String lsPSeqno = "" , lsBank = "";
		
		SmsMsgDetl oosms = new SmsMsgDetl();
		oosms.setConn(wp);
		
		lsPSeqno = wp.itemStr2("p_seqno");
		lsBank = commString.mid(wp.itemStr2("autopay_acct_bank"), 0,3);
		
		int liRc=1;
		
		if(!eqIgno(lsBank,"006")){
			errmsg("外幣自扣帳號 須為本行帳號");
			return -1;
		}
		
		liRc =oosms.actM0010(lsPSeqno, "A");
		if (liRc==-1){
			sqlCommit(liRc);
			errmsg(oosms.getMsg());
			return -1;
		}
		
		if(eqIgno(asType,"1")){
			wp.colSet("sms_send_date", getSysDate());
			wp.colSet("sms_send_cnt", wp.itemNum("sms_send_cnt")+1);
			wp.itemSet("sms_send_date", getSysDate());
			wp.itemSet("sms_send_cnt", wp.itemNum("sms_send_cnt")+1+"");
		}	else	{			
			liRc =updateSms();
			if (liRc==-1) {
				sqlCommit(liRc);
				errmsg(getMsg());
				return -1;
			}
		}
		
		sqlCommit(1);
		return 1;
	}
	
	String selectActAcctCurr() {

		isOldBank="";
		isOldAcctno="";
		isOldIdno="";
		String sql1 = "";

     //舊資料扣繳銀行代碼只有3碼
		   sql1 = " select "
					//+ "autopay_acct_bank||"
						+ "act_ach_bank.bank_no||"
						+ "autopay_acct_no||"
						+ "autopay_indicator||"
						+ "autopay_id||"
						+ "autopay_id_code||"
						+ "autopay_dc_flag||"
						+ "curr_change_accout||"
						+ "autopay_dc_indicator as ls_db_data "
					+", autopay_acct_no as old_auto_acctno, autopay_acct_bank as old_auto_bank, autopay_id as old_auto_idno"
						+ " from act_acct_curr, act_ach_bank  "
						+ " where p_seqno = ? "
						+ " and curr_code = ? "						
						+ " and substr(autopay_acct_bank,1,3) = substr(act_ach_bank.bank_no,1,3) "						
						;

		sqlSelect(sql1,new Object[]{wp.itemStr2("p_seqno"),wp.itemStr2("curr_code")});
		
		if(sqlRowNum<=0)	return "";

		isOldAcctno =colStr("old_auto_acctno");
		isOldBank =colStr("old_auto_bank");
		isOldIdno =colStr("old_auto_idno");

	  return colStr("ls_db_data");
	}
	
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc!=1) return rc;

   /*** java 程式執行的系統作業環境時間(例如192.168.30.20) 和 DB2 所在的系統作業環境時間[sysdate]可能會不一致
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysDate = df.format(new Date());
		
		df = new SimpleDateFormat("HHmmss");
		String sysTime = df.format(new Date());
    ***/ 

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_chkno");
		sp.ppstr("p_seqno", wp.itemStr2("p_seqno"));
		sp.ppstr("acct_type", mAccttype);
		sp.addsql(", id_p_seqno ",", uf_idno_pseqno('"+ mAcctkey.substring(0, 10) +"') ");
		sp.ppstr("autopay_acct_bank", wp.itemStr2("autopay_acct_bank"));
		sp.ppstr("autopay_acct_no", wp.itemStr2("autopay_acct_no"));
		sp.ppstr("autopay_indicator", wp.itemStr2("autopay_indicator"));
		sp.ppstr("valid_flag", wp.itemStr2("valid_flag"));
		sp.ppstr("autopay_id", wp.itemStr2("autopay_id"));
		sp.ppstr("autopay_id_code", wp.itemStr2("autopay_id_code"));
		sp.ppstr("autopay_dc_flag", wp.itemStr2("autopay_dc_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("autopay_dc_indicator", wp.itemStr2("autopay_dc_indicator"));
		sp.ppstr("curr_change_accout", wp.itemStr2("exchange_acct_no"));
		sp.ppstr("from_mark", wp.itemStr2("from_mark"));
		sp.ppstr("verify_flag", wp.itemStr2("verify_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("verify_date", wp.itemStr2("verify_date"));
		sp.ppstr("verify_return_code", wp.itemStr2("verify_return_code"));
		sp.ppstr("stmt_cycle", wp.itemStr2("stmt_cycle"));
		sp.ppstr("sms_send_date", wp.itemStr2("sms_send_date"));
		sp.ppnum("sms_send_cnt", wp.itemNum("sms_send_cnt"));
		sp.ppstr("ad_mark", "A");
		sp.ppstr("proc_mark", "N");
		sp.ppstr("curr_code", mCurrcode);
		//-JH:22.0520-
		sp.ppstr("old_acct_bank", isOldBank);
		sp.ppstr("old_acct_no", isOldAcctno);
		sp.ppstr("old_acct_id", isOldIdno);
	//sp.ppss("crt_date", sysDate);
		sp.addsql(", crt_date ",", to_char(sysdate,'yyyymmdd') ");
	//sp.ppss("crt_time", sysTime);
		sp.addsql(", crt_time ",", to_char(sysdate,'hh24miss') ");
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_seqno", "1");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return 1;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1) return rc;
		
   /*** java 程式執行的系統作業環境時間(例如192.168.30.20) 和 DB2 所在的系統作業環境時間[sysdate]可能會不一致
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysDate = df.format(new Date());
		
		df = new SimpleDateFormat("HHmmss");
		String sysTime = df.format(new Date());
    ***/ 

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_chkno");
		sp.ppstr("autopay_acct_bank", wp.itemStr2("autopay_acct_bank"));
		sp.ppstr("autopay_acct_no", wp.itemStr2("autopay_acct_no"));
		sp.ppstr("autopay_indicator", wp.itemStr2("autopay_indicator"));
		sp.ppstr("valid_flag", wp.itemStr2("valid_flag"));
		sp.ppstr("autopay_id", wp.itemStr2("autopay_id"));
		sp.ppstr("autopay_id_code", wp.itemStr2("autopay_id_code"));
		sp.ppstr("autopay_dc_flag", wp.itemStr2("autopay_dc_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("autopay_dc_indicator", wp.itemStr2("autopay_dc_indicator"));
		sp.ppstr("curr_change_accout", wp.itemStr2("exchange_acct_no"));
		sp.ppstr("verify_flag", wp.itemStr2("verify_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("verify_date", wp.itemStr2("verify_date"));
		sp.ppstr("verify_return_code", wp.itemStr2("verify_return_code"));
		sp.ppstr("stmt_cycle", wp.itemStr2("stmt_cycle"));
		sp.ppstr("sms_send_date", wp.itemStr2("sms_send_date"));
		sp.ppnum("sms_send_cnt", wp.itemNum("sms_send_cnt"));
		//-jh:22.0520-
		sp.ppstr("old_acct_bank", isOldBank);
		sp.ppstr("old_acct_no", isOldAcctno);
		sp.ppstr("old_acct_id", isOldIdno);
		//sp.ppss("crt_date", sysDate);
		sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd') ", "");
	//sp.ppss("crt_time", sysTime);
		sp.addsql(", crt_time = to_char(sysdate,'hh24miss') ", "");
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where hex(rowid) = ?", kk1);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
		
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1) return rc;
		
		strSql = "delete act_chkno "
				+ sqlWhere;
		Object[] param = new Object[] { kk1, wp.modSeqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	public int updateSms() throws Exception{
		msgOK();
		strSql = " update act_chkno set "
				 + " sms_send_date =to_char(sysdate,'yyyymmdd') , "
				 + " sms_send_cnt = nvl(sms_send_cnt,0)+1 , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_seqno =nvl(mod_seqno,0)+1 "
				 + " where mod_seqno =:mod_seqno "
			 //+commSqlStr.whereRowid(wp.itemStr2("rowid"))
				 + " and hex(rowid) = :hex_rowid "
				 ;
		
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		item2ParmNum("mod_seqno");
		setString("hex_rowid",wp.itemStr2("rowid"));
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("資料已被其他人修改, 請重新讀取");
		}
		
		return rc;
	}
	
}
