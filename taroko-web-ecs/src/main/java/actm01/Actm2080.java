/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 109-04-15  V1.00.01  Alex       add auth_query		                         *
* 110-11-16  V1.00.02  Andy       Update Mantis9045                          *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-20  V1.00.04  Simon      remove mobile_msg_xxx data update          *
******************************************************************************/

package actm01;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actm2080 extends BaseEdit {
	CommString commString = new CommString();
	Actm2080Func func;

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
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void queryFunc() throws Exception {
		if(empty(wp.itemStr2("ex_id_no")) && empty(wp.itemStr2("ex_corp_no"))) {
			alertErr2("身份證號, 統一編號不可均為空白");
			return;
		}

		if(!empty(wp.itemStr2("ex_id_no")) && !empty(wp.itemStr2("ex_corp_no"))) {
			alertErr2("身份證號, 統一編號不可同時輸入");
			return;
		}
			
		ColFunc func =new ColFunc();
		func.setConn(wp);
		String lsAcctKey = "";
		if(wp.itemEmpty("ex_id_no")==false){			
			lsAcctKey = commString.acctKey(wp.itemStr2("ex_id_no"));
			if(lsAcctKey.length()!=11){
				alertErr2("身分證號碼:輸入錯誤");
				return ;
			}			
			
			if (func.fAuthQuery(wp.modPgm(), commString.mid(lsAcctKey, 0,10))!=1) { 
	      	alertErr2(func.getMsg()); 
	      	return ; 
	    }
		}	else if(wp.itemEmpty("ex_corp_no")==false){
		  lsAcctKey = wp.itemStr2("ex_corp_no");
			if(lsAcctKey.length()!=8 && lsAcctKey.length()!=11){
				alertErr2("統編帳號:輸入錯誤");
				return ;
			}			
			if (func.fAuthQuery(wp.modPgm(), lsAcctKey)!=1) { 
	      	alertErr2(func.getMsg()); 
	      	return ; 
	    }
		}

		
		
		wp.setQueryMode();
    	wp.colSet("paper_flag-Y", "");  //disabled checkbox 手動清空
    	wp.colSet("internet_flag-Y", "");

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
 		selectNoLimit();

		wp.pageControl();

		wp.selectSQL = "a.acno_p_seqno, uf_nvl(c.chi_name,'') as corp_name, uf_nvl(b.chi_name,'') as chi_name, a.mod_seqno, "
				//+ "decode(a.card_indicator,'2',c.e_mail_addr,b.e_mail_addr) as e_mail_addr, "
				//+ "decode(a.card_indicator,'2',c.e_mail_addr2,'') as e_mail_addr2, "
				//+ "decode(a.card_indicator,'2',c.e_mail_addr3,'') as e_mail_addr3, "
					+ "decode(a.acno_flag,'2',c.e_mail_addr,b.e_mail_addr) as e_mail_addr, "
				  //+ "decode(a.acno_flag,'2',c.e_mail_addr2,'') as e_mail_addr2, "
					//+ "decode(a.acno_flag,'2',c.e_mail_addr3,'') as e_mail_addr3, "
					+ "uf_nvl(b.cellar_phone,'') as cell_phone, "
					+ "decode(a.card_indicator,'2',c.e_mail_from_mark,b.e_mail_from_mark) as e_mail_from_mark, "
					+ "decode(a.card_indicator,'2',c.e_mail_chg_date,b.e_mail_chg_date) as e_mail_chg_date, "
					+ "a.acno_flag, "
					+ "a.stmt_cycle, "
					+ "decode(a.stat_send_paper,'','N',a.stat_send_paper) as paper_flag, "
					+ "a.stat_send_s_month, "
					+ "a.stat_send_e_month, "
					+ "decode(a.stat_send_internet,'','N',a.stat_send_internet) as internet_flag, "
					+ "a.stat_send_s_month2, "
					+ "a.stat_send_e_month2, "
					+ "decode(h.SUBSCRIBE,null,'N','Y') as mobile_line_flag, "
				//+ "decode(a.mobile_msg,'','N',a.mobile_msg) as mobile_msg_flag, "
				//+ "a.mobile_msg_smonth, "
				//+ "a.mobile_msg_emonth, "
					+ "a.paper_upd_date, a.paper_upd_user, "
					+ "a.internet_upd_date, a.internet_upd_user, "
				//+ "a.mobile_msg_upd_date, a.mobile_msg_upd_user "
					+ "a.e_mail_ebill, "
					+ "a.e_mail_ebill_date "
					;
		wp.daoTable = "act_acno a "
					+ "left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
					+ "left join crd_corp c on a.corp_p_seqno = c.corp_p_seqno "
					+ "left join (select d.id_no as id_no, e.SUBSCRIBE as SUBSCRIBE "
					+ "from mkt_line_cust d,mkt_line_subscribe e "
					+ "where d.LINE_ID = e.LINE_ID and e.SUBSCRIBE = 'MobileBill' ) h "
					+ "on  uf_idno_id(a.id_p_seqno) = h.id_no ";
	
		wp.whereStr = "where a.acct_type = :acct_type ";
		setString("acct_type", wp.itemStr2("ex_acct_type"));
 	
		String lsAcctKey = "";

		if (empty(wp.itemStr2("ex_id_no")) == false){
			/***
			String lsidno = wp.item_ss("ex_id_no");
			String lsidnocode = "";
			if (lsidno.length() ==10) {
				lsidnocode = "0";
			} else if (lsidno.length() ==11) {
				lsidnocode = lsidno.substring(10);
				lsidno = lsidno.substring(0, 10);
			}
			wp.whereStr += " and b.id_no = :id_no ";
			wp.whereStr += " and b.id_no_code = :id_no_code ";
			setString("id_no", lsidno);
			setString("id_no_code", lsidnocode);
			***/
			lsAcctKey = commString.acctKey(wp.itemStr2("ex_id_no"));
			wp.whereStr += " and a.acct_key = :ps_acct_key ";
			setString("ps_acct_key", lsAcctKey);
		}
		if (empty(wp.itemStr2("ex_corp_no")) == false){
			//wp.whereStr += " and c.corp_no = :corp_no ";
			//setString("corp_no", wp.item_ss("ex_corp_no"));
			lsAcctKey = commString.acctKey(wp.itemStr2("ex_corp_no"));
			wp.whereStr += " and a.acct_key = :ps_acct_key ";
			setString("ps_acct_key", lsAcctKey);
		}
	//wp.whereStr += sql_rownum(1); 
    wp.whereStr += "order by a.acct_key fetch first 1 row only ";

	//pageQuery();
		pageSelect();
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		getNextAcctMonth();
		listWkdata();
	}
	
	private void getNextAcctMonth() throws Exception {
		String lsSql = "";
		lsSql  = " select next_acct_month, this_acct_month ";
		lsSql += " from ptr_workday ";
		lsSql += " where stmt_cycle = :ls_cycle ";
		setString("ls_cycle", wp.colStr("stmt_cycle"));
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("next_acct_month", sqlStr("next_acct_month"));
			wp.colSet("this_acct_month", sqlStr("this_acct_month"));
		}
		
  //預設郵寄 改 網路, 對帳單並行0月份、網路 改 郵寄, 對帳單並行1月份 
		wp.colSet("paper2email", "0");
		wp.colSet("email2paper", "1");
		lsSql  = " select paper2email_mm, email2paper_mm ";
		lsSql += " from ptr_stat_coexist ";
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("paper2email", sqlStr("paper2email_mm"));
			wp.colSet("email2paper", sqlStr("email2paper_mm"));
		}
	}
	
	void listWkdata() {
    	String ss="";
    	
    	ss =wp.colStr("e_mail_from_mark");
    	wp.colSet("tt_e_mail_from_mark", commString.decode(ss, ",1,M,W,A", ",IBM,ECS(人工修改),Web(信用卡網站),AFS"));
    	
    	ss = wp.colStr("paper_flag");
		  if ( !eqIgno(ss,"Y")) 
		     { ss = "N"; }
    	wp.colSet("chk_paper_flag", ss);
    //wp.col_set("stat_paper", ss);
      wp.colSet("stat_paper", "");
    	ss = wp.colStr("internet_flag");
		  if ( !eqIgno(ss,"Y")) 
		     { ss = "N"; }
      wp.colSet("chk_internet_flag", ss);
    //wp.col_set("stat_internet", ss);
      wp.colSet("stat_internet", "");

      /***
    	ss = wp.col_ss("stat_send_s_month");
      wp.col_set("stat_paper_ym_s", ss);
    	ss = wp.col_ss("stat_send_e_month");
      wp.col_set("stat_paper_ym_e", ss);
    	ss = wp.col_ss("stat_send_s_month2");
    	wp.col_set("stat_internet_ym_s", ss);
    	ss = wp.col_ss("stat_send_e_month2");
    	wp.col_set("stat_internet_ym_e", ss);
      ***/
      wp.colSet("stat_paper_ym_s", "");
      wp.colSet("stat_paper_ym_e", "");
    	wp.colSet("stat_internet_ym_s", "");
    	wp.colSet("stat_internet_ym_e", "");

/***
    	ss = wp.colStr("mobile_msg_flag");
		  if ( !eqIgno(ss,"Y")) 
		     { ss = "N"; }
    	wp.colSet("chk_mobile_msg_flag", ss);
      wp.colSet("stat_mobile_msg", "");
		  if (!eqIgno(ss,"Y"))
		  {
         wp.colSet("mobile_msg_flag-Y", "");
		  }
***/

    	ss = wp.colStr("mobile_line_flag");
    	wp.colSet("chk_mobile_line_flag", ss);
			if (!eqIgno(ss,"Y"))
			{
    	   wp.colSet("mobile_line_flag-Y", "");
			}

      /***
    	ss = wp.col_ss("mobile_msg_smonth");
    	wp.col_set("stat_mobile_msg_ym_s", ss);
    	ss = wp.col_ss("mobile_msg_emonth");
    	wp.col_set("stat_mobile_msg_ym_e", ss);
      ***/
    //wp.colSet("stat_mobile_msg_ym_s", "");
    //wp.colSet("stat_mobile_msg_ym_e", "");
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
		func = new Actm2080Func(wp);
    	
		if (ofValidation()<0) return;
		
		rc = func.dbSave(strAction);
        if (rc!=1) {
            alertErr2(func.getMsg());
        }
        this.sqlCommit(rc);
	}
	
	int ofValidation() throws Exception {
		String lsAcnoPSeqno, lsStatPaper, lsStatInternet, lsStatMobileMsg,
		       lsStatMobileLine, lsCellPhone;
		String lsSysym,lsPYms,lsPYme,lsIYms,lsIYme,lsEmail,lsMmYms,lsMmYme;
		String lsPaper1, lsPaper2, lsInternet1, lsInternet2, lsMmUpdDate, lsMmUpdUser;
		String updStatPaper, updStatSendSMonth, updStatSendEMonth, 
		       updStatInternet, updStatSendSMonth2, updStatSendEMonth2,
		       updMobileMsg, updMobileMsgSmonth, updMobileMsgEmonth;
		boolean lbEndYM;
		int liPos;

		lsAcnoPSeqno = wp.itemStr2("acno_p_seqno");
		if (empty(lsAcnoPSeqno)) {
			alertErr("未讀取資料 不可異動 ");
			return -1;
		}
		
		//--是否異動--
//		if not this.ib_datamodify then
		lsStatPaper = wp.itemStr2("stat_paper");
		if (empty(lsStatPaper)) lsStatPaper="N";
		lsStatInternet = wp.itemStr2("stat_internet");
		if (empty(lsStatInternet)) lsStatInternet="N";

	//lsStatMobileMsg = wp.itemStr2("stat_mobile_msg");
	//if (empty(lsStatMobileMsg)) lsStatMobileMsg="N";

		lsStatMobileLine = wp.itemStr2("chk_mobile_line_flag");

		lsPYms = wp.itemStr2("stat_paper_ym_s");
		lsPYme = wp.itemStr2("stat_paper_ym_e");
		lsIYms = wp.itemStr2("stat_internet_ym_s");
		lsIYme = wp.itemStr2("stat_internet_ym_e");
	//lsMmYms = wp.itemStr2("stat_mobile_msg_ym_s");
	//lsMmYme = wp.itemStr2("stat_mobile_msg_ym_e");
		if ( eqIgno(lsStatPaper,wp.itemStr2("chk_paper_flag")) 
				&& eqIgno(lsPYms,wp.itemStr2("stat_send_s_month"))
				&& eqIgno(lsPYme,wp.itemStr2("stat_send_e_month"))
				&& eqIgno(lsStatInternet,wp.itemStr2("chk_internet_flag"))
				&& eqIgno(lsIYms,wp.itemStr2("stat_send_s_month2"))
				&& eqIgno(lsIYme,wp.itemStr2("stat_send_e_month2")) 
			//&& eqIgno(lsStatMobileMsg,wp.itemStr2("chk_mobile_msg_flag"))
			//&& eqIgno(lsMmYms,wp.itemStr2("mobile_msg_smonth"))
			//&& eqIgno(lsMmYme,wp.itemStr2("mobile_msg_emonth")) 
			 ) {
			alertErr("資料未異動, 不可修改");
			return -1;
		}
		
		
		if (eqIgno(lsStatPaper,"Y")==false && eqIgno(lsStatInternet,"Y")==false 
		  //&& eqIgno(lsStatMobileMsg,"Y")==false
		   ) 
		{
			alertErr("請指定帳單寄送方式 ");
			return -1;
		} 

		if ( ( eqIgno(wp.itemStr2("acno_flag"),"Y")==false ) &&
		     ( empty(lsPYms) && empty(lsPYme) && empty(lsIYms) && empty(lsIYme) 
		     //&& empty(lsMmYms) && empty(lsMmYme) 
		     ) 
		   ) 
		{
			alertErr("請指定 有效年月");
			return -1;
		}

		lsSysym = wp.itemStr2("next_acct_month");
		//if ((eq_igno(ls_stat_paper,"Y") && eq_igno(ls_p_yms,ls_sysym)==false)
		//		|| (eq_igno(ls_stat_internet,"Y") && eq_igno(ls_i_yms,ls_sysym)==false) ) {
		//	alert_err("有效年月[起] 須等於下次關帳年月");
		//	return -1;
		//}
		
		if (eqIgno(lsStatPaper,"Y") && eqIgno(lsPYms,lsSysym)==false
		    && !eqIgno(wp.itemStr2("chk_paper_flag"),"Y") )
		{
			alertErr("郵寄: 有效年月[起] 須等於下次關帳年月");
			return -1;
		}
		if (eqIgno(lsStatInternet,"Y") && eqIgno(lsIYms,lsSysym)==false  
		    && !eqIgno(wp.itemStr2("chk_internet_flag"),"Y") )
		{
			alertErr("網路: 有效年月[起] 須等於下次關帳年月");
			return -1;
		}
		
		lbEndYM = false;
		if (eqIgno(lsStatPaper,"Y")) {
			if (chkStrend(lsPYms, lsPYme) == false) {
				alertErr("郵寄: 有效年月起不可大於迄");
				return -1;
			}
			if (empty(lsPYme)) lbEndYM = true;
		}

		if (eqIgno(lsStatInternet,"Y")) {
			lsEmail = wp.itemStr2("e_mail_addr");
			if (empty(lsEmail)) {
				alertErr("無 E-mail 資料, 不可指定[網路]寄送");
				return -1;
			}
			liPos = commString.pos(lsEmail,"@");
			liPos++;
			if (liPos==0 || liPos==1 || liPos==lsEmail.length()) {
				alertErr("E-mail格式錯誤, 不可指定[網路]寄送");
				return -1;
			}
			if (chkStrend(lsIYms, lsIYme) == false) {
				alertErr("網路: 有效年月起不可大於迄");
				return -1;
			}
			if (empty(lsIYme)) lbEndYM = true;
		}
		
/***
		if (eqIgno(lsStatMobileMsg,"Y")) {
			lsCellPhone = wp.itemStr2("cell_phone");
			if (empty(lsCellPhone)) {
				alertErr("無 手機號碼 資料, 不可指定[行動帳單_簡訊]寄送");
				return -1;
			}

			if (empty(lsMmYms) && !empty(lsMmYme) && !empty(wp.itemStr2("mobile_msg_smonth")) ) {
				lsMmYms = wp.itemStr2("mobile_msg_smonth");
			}
			if (empty(lsMmYms)) {
				alertErr("行動帳單_簡訊: 生效起始年月不可空白");
				return -1;
			}
			if ((chkStrend(lsSysym, lsMmYms) == false) && !eqIgno(wp.itemStr2("chk_mobile_msg_flag"),"Y") ) 
			{
				alertErr("行動帳單_簡訊: 有效年月[起] 須大於等於下次關帳年月");
				return -1;
			}

			if (chkStrend(lsMmYms, lsMmYme) == false) {
				alertErr("行動帳單_簡訊: 有效年月起不可大於迄");
				return -1;
			}
		//if (empty(ls_mm_yme)) lb_end_YM = true;
			lbEndYM = true;//紙本寄送或網路寄送才須控制有效年月[迄]須有一為空白
		}
***/
		
		if (!lbEndYM && eqIgno(wp.itemStr2("acno_flag"),"Y")==false ) {
			alertErr("寄送方式 有效年月[迄]須有一為空白");
			return -1;
		}
		
		//set update value//
		if (eqIgno(lsStatPaper,"Y")==false) {
			lsPYms="";
			lsPYme="";
		}
		if (eqIgno(lsStatInternet,"Y")==false) {
			lsIYms="";
			lsIYme="";
		}
	//if (eqIgno(lsStatMobileMsg,"Y")==false) {
	//	lsMmYms="";
	//	lsMmYme="";
	//}
		

		lsPaper1 = wp.itemStr2("paper_upd_date");
		lsPaper2 = wp.itemStr2("paper_upd_user");
		lsInternet1 = wp.itemStr2("internet_upd_date");
		lsInternet2 = wp.itemStr2("internet_upd_user");
	//lsMmUpdDate = wp.itemStr2("mobile_msg_upd_date");
	//lsMmUpdUser = wp.itemStr2("mobile_msg_upd_user");

    /***
	  upd_stat_paper = wp.item_ss("chk_paper_flag");
		upd_stat_send_s_month = wp.item_ss("stat_send_s_month");
		upd_stat_send_e_month = wp.item_ss("stat_send_e_month");
	  upd_stat_internet = wp.item_ss("chk_internet_flag");
		upd_stat_send_s_month2 = wp.item_ss("stat_send_s_month2");
		upd_stat_send_e_month2 = wp.item_ss("stat_send_e_month2");
		upd_mobile_msg = wp.item_ss("chk_mobile_msg_flag");
		upd_mobile_msg_smonth = wp.item_ss("mobile_msg_smonth");
		upd_mobile_msg_emonth = wp.item_ss("mobile_msg_emonth");
    ***/

		//ddd("actm2080_A,upd_stat_paper: "+upd_stat_paper);
		//ddd("actm2080_B,upd_stat_internet: "+upd_stat_internet);

		if (eqIgno(lsStatPaper,wp.itemStr2("chk_paper_flag"))==false 
				|| eqIgno(lsPYms,wp.itemStr2("stat_send_s_month"))==false
				|| eqIgno(lsPYme,wp.itemStr2("stat_send_e_month"))==false) {
			lsPaper1 = wp.sysDate;
			lsPaper2 = wp.loginUser;
		}
		if (eqIgno(lsStatInternet,wp.itemStr2("chk_internet_flag"))==false 
				|| eqIgno(lsIYms,wp.itemStr2("stat_send_s_month2"))==false
				|| eqIgno(lsIYme,wp.itemStr2("stat_send_e_month2"))==false) {
			lsInternet1 = wp.sysDate;
			lsInternet2 = wp.loginUser;
		}
	//if (eqIgno(lsStatMobileMsg,wp.itemStr2("chk_mobile_msg_flag"))==false 
	//		|| eqIgno(lsMmYms,wp.itemStr2("mobile_msg_smonth"))==false
	//		|| eqIgno(lsMmYme,wp.itemStr2("mobile_msg_emonth"))==false) {
	//	lsMmUpdDate = wp.sysDate;
	//	lsMmUpdUser = wp.loginUser;
	//}
		
    /***
		if (eq_igno(ls_stat_paper,"Y")==true ) {
		  if (eq_igno(wp.item_ss("chk_paper_flag"),"Y")==false ) {
			  ls_paper1 = wp.sysDate;
			  ls_paper2 = wp.loginUser;
		    upd_stat_paper = ls_stat_paper;
		  }
			if (eq_igno(ls_p_yms,wp.item_ss("stat_send_s_month"))==false) {
			  ls_paper1 = wp.sysDate;
			  ls_paper2 = wp.loginUser;
    		upd_stat_send_s_month = ls_p_yms;
		  }
			if (eq_igno(ls_p_yme,wp.item_ss("stat_send_e_month"))==false) {
			  ls_paper1 = wp.sysDate;
			  ls_paper2 = wp.loginUser;
		    upd_stat_send_e_month = ls_p_yme;
		  }
		}
		if (eq_igno(ls_stat_internet,"Y")==true ) {
		  if (eq_igno(wp.item_ss("chk_internet_flag"),"Y")==false) {
			  ls_paper1 = wp.sysDate;
			  ls_paper2 = wp.loginUser;
		    upd_stat_internet = ls_stat_internet;
		  }
			if (eq_igno(ls_i_yms,wp.item_ss("stat_send_s_month2"))==false) {
			  ls_internet1 = wp.sysDate;
			  ls_internet2 = wp.loginUser;
	   	  upd_stat_send_s_month2 = ls_i_yms;
		  }
			if (eq_igno(ls_i_yme,wp.item_ss("stat_send_e_month2"))==false) {
			  ls_internet1 = wp.sysDate;
			  ls_internet2 = wp.loginUser;
		    upd_stat_send_e_month2 = ls_i_yme;
		  }
		}
		if (eq_igno(ls_stat_mobile_msg,"Y")==true ) {
		  if (eq_igno(wp.item_ss("chk_mobile_msg_flag"),"Y")==false ) {
			  ls_paper1 = wp.sysDate;
			  ls_paper2 = wp.loginUser;
	  	  upd_mobile_msg = ls_stat_mobile_msg;
		  }
			if (eq_igno(ls_mm_yms,wp.item_ss("mobile_msg_smonth"))==false) { 
			  ls_mm_upd_date = wp.sysDate;
			  ls_mm_upd_user = wp.loginUser;
		    upd_mobile_msg_smonth = ls_mm_yms;
		  }
			if (eq_igno(ls_mm_yme,wp.item_ss("mobile_msg_emonth"))==false) {
			  ls_mm_upd_date = wp.sysDate;
			  ls_mm_upd_user = wp.loginUser;
		    upd_mobile_msg_emonth = ls_mm_yme;
		  }
		}
    ***/
		
		func.varsSet("acno_p_seqno", lsAcnoPSeqno);
    
		func.varsSet("stat_send_paper", lsStatPaper);
		func.varsSet("stat_send_s_month", lsPYms);
		func.varsSet("stat_send_e_month", lsPYme);
		func.varsSet("paper_upd_date", lsPaper1);
		func.varsSet("paper_upd_user", lsPaper2);
		func.varsSet("stat_send_internet", lsStatInternet);
		func.varsSet("stat_send_s_month2", lsIYms);
		func.varsSet("stat_send_e_month2", lsIYme);
		func.varsSet("internet_upd_date", lsInternet1);
		func.varsSet("internet_upd_user", lsInternet2);
/***
		func.varsSet("mobile_msg", lsStatMobileMsg);
		func.varsSet("mobile_msg_smonth", lsMmYms);
		func.varsSet("mobile_msg_emonth", lsMmYme);
		func.varsSet("mobile_msg_upd_date", lsMmUpdDate);
		func.varsSet("mobile_msg_upd_user", lsMmUpdUser);
***/    
    /***
		func.vars_set("stat_send_paper", upd_stat_paper);
		func.vars_set("stat_send_s_month", upd_stat_send_s_month);
		func.vars_set("stat_send_e_month", upd_stat_send_e_month);
		func.vars_set("paper_upd_date", ls_paper1);
		func.vars_set("paper_upd_user", ls_paper2);
		func.vars_set("stat_send_internet", upd_stat_internet);
		func.vars_set("stat_send_s_month2", upd_stat_send_s_month2);
		func.vars_set("stat_send_e_month2", upd_stat_send_e_month2);
		func.vars_set("internet_upd_date", ls_internet1);
		func.vars_set("internet_upd_user", ls_internet2);
		func.vars_set("mobile_msg", upd_mobile_msg);
		func.vars_set("mobile_msg_smonth", upd_mobile_msg_smonth);
		func.vars_set("mobile_msg_emonth", upd_mobile_msg_emonth);
		func.vars_set("mobile_msg_upd_date", ls_mm_upd_date);
		func.vars_set("mobile_msg_upd_user", ls_mm_upd_user);
    ***/

		
		return 1;
	}

	@Override
	public void initButton() {
	//if (wp.respHtml.indexOf("_detl") > 0) {
	//	 this.btnMode_aud();
	//}
	
	  String sKey = "1st-page";
    if (wp.respHtml.equals("actm2080"))  {
     //wp.col_set("btnUpdate_disable","");多餘
       this.btnModeAud(sKey);
    }
	
	}

	@Override
	public void dddwSelect() {
		try {
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
		} catch (Exception ex) {}
	}

}
