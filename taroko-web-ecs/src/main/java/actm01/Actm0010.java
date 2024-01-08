/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 107-08-14  V1.00.01  Alex       bug fixed                                  *
* 2018-0816            JH			    modify                                     *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-14  V1.00.04  Simon      1.queryFunc() avoid double getWhereStr() execution*
*                                 2.uf_idno_idcode(x,x) alternative solution *
*                                 3.cancel autopay_indicator='3'             *
*                                 4.update autopay_acct_no data into act_acno & act_acct_curr for bank 006*
* 111-12-28  V1.00.05  Ryan       hWhereQChkno modified autopay_acct_bank into A.autopay_acct_bank
* 111-12-28  V1.00.06  Ryan       增加回覆檔匯入功能*
******************************************************************************/

package actm01;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import busi.SqlPrepare;
import busi.func.SmsMsgDetl;
import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Actm0010 extends BaseEdit {
	CommString commString = new CommString();
	CommDate commd = new CommDate();
	int listCnt = 0;
	String lsMsg = "";
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	String kk1 = "" , kk2 = "" , isPSeqno = "" , hWhereQAcno ="" , hWhereQChkno ="";
	String isStmtCycle = "";
	boolean skipFlag = false;
	String busDate = "";
	int ttlCnt = 0;
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
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			strAction = "D";
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
		} else if (eqIgno(wp.buttonCode, "C1")) {
			/* 動態查詢 */
			sendSmsMesg();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {
			/* 20200107 modify AJAX */
			strAction = "AJAX";
			wfAjaxAcctbank();
		} else if (eqIgno(wp.buttonCode, "B1")) {
	    	strAction = "new";
	  	    clearFunc();
	    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
	        // -資料處理-
	        procFunc();
	    }

		dddwSelect();
		initButton();
	}

	@Override
	public void queryFunc() throws Exception {
		
		if (getWhereStr() == false) {
			return;
		}		
	//wp.whereStr = h_where_q_chkno;
	//wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	private boolean getWhereStr() throws Exception {
		String lsAcctKey = "";

		if(!wp.itemEmpty("ex_acct_key")){
			lsAcctKey = commString.acctKey(wp.itemStr2("ex_acct_key"));
			if(lsAcctKey.length()!=11){
				alertErr2("帳戶帳號:輸入錯誤");
				return false;
			}
		}

	  	if( empty(wp.itemStr2("ex_acct_key")) && empty(wp.itemStr2("ex_idno")) && 
	  	    empty(wp.itemStr2("ex_idno2")) && empty(wp.itemStr2("ex_autopay_acct_bank")) &&
	  	    empty(wp.itemStr2("ex_autopay_acct_no"))
	  	  ) 
	  	{
		  	alertErr2("帳戶帳號、帳戶扣繳歸屬ID、歸戶ID、扣繳行庫、扣繳帳號不可均為空白");
			  return false;
		  }

		String lsAutopayAcctBank3b = "";
		if (empty(wp.itemStr2("ex_autopay_acct_bank")) == false) { 
			lsAutopayAcctBank3b = commString.mid(wp.itemStr2("ex_autopay_acct_bank"), 0, 3);
		}

		hWhereQChkno = " where 1=1 "
							 + " and A.ad_mark <>'D' "
							 + " and A.p_seqno = B.acno_p_seqno "
							 + " and uf_nvl(A.curr_code,'901') = '901' "
							 +sqlCol(wp.itemStr2("ex_acct_type"),"A.acct_type")
						   +sqlCol(lsAcctKey,"acct_key")
						   +sqlCol(lsAutopayAcctBank3b,"A.autopay_acct_bank","like%")     
							 +sqlCol(wp.itemStr2("ex_autopay_acct_no"),"A.autopay_acct_no")
							 +sqlCol(wp.itemStr2("ex_idno"),"A.autopay_id")
							 ;
		
		if(wp.itemEmpty("ex_idno2")==false){
			hWhereQChkno += " and A.id_p_seqno in (select id_p_seqno from crd_idno where id_no = ?) ";
			setString(wp.itemStr2("ex_idno2"));    //原程式SQL Injection 故修改
		}

		hWhereQAcno = " where 1=1 "
							 + " and p_seqno = acno_p_seqno "
							 +sqlCol(wp.itemStr2("ex_acct_type"),"acct_type")
							+sqlCol(lsAcctKey,"acct_key")      
							+sqlCol(lsAutopayAcctBank3b,"autopay_acct_bank","like%")   
							 +sqlCol(wp.itemStr2("ex_autopay_acct_no"),"autopay_acct_no")
							 +sqlCol(wp.itemStr2("ex_idno"),"autopay_id")
							 ;
		
		if(wp.itemEmpty("ex_idno2")==false){
			hWhereQAcno += " and a.id_p_seqno in (select id_p_seqno from crd_idno where id_no = ?) ";
			setString(wp.itemStr2("ex_idno2"));     //原程式SQL Injection 故修改
		}
		
		return true;
	}


	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (!eqIgno(wp.buttonCode, "Q")) {
		  if (getWhereStr() == false) {
			  return;
		  } 		
		}		

	  /*** group by having 寫法 1
		String ls_sub_sqlCmd = ""
 	  				 + " select "
						 + " 'act_chkno' as tt_which_table ,"
						 + " decode(b.acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(b.acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						 + " A.acct_type ,"
						 + " B.acct_key ,"
						 + " A.autopay_acct_bank ,"
						 + " A.autopay_acct_no ,"
						 + " A.card_no ,"
						 + " A.autopay_id || '-' || A.autopay_id_code as tt_autopay_id  ,"
						 + " A.valid_flag ,"
						 + " A.id_p_seqno ,"
						 + " decode(b.acno_flag,'2',d.chi_name,c.chi_name) as chi_name  ,"
						 + " A.autopay_indicator ,"
						 + " A.autopay_fix_amt ,"
						 + " A.autopay_rate ,"
						 + " A.autopay_acct_s_date ,"
						 + " A.autopay_acct_e_date ,"
						 + " A.p_seqno , "
						 + " B.corp_p_seqno "
			  		 + " from act_chkno A , act_acno B "
	      		 + " left join crd_idno c on b.id_p_seqno = c.id_p_seqno "
					   + " left join crd_corp d on b.corp_p_seqno = d.corp_p_seqno "
						 + h_where_q_chkno
						 + " union "
 	  				 + " select "
						 + " 'act_acno' as tt_which_table ,"
						 + " decode(acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						 + " acct_type ,"
						 + " acct_key ,"
						 + " autopay_acct_bank ,"
						 + " autopay_acct_no ,"
						 + " '' as card_no ,"
						 + " autopay_id || '-' || autopay_id_code as tt_autopay_id  ,"
						 + " '' as valid_flag ," //生效日旗標 -- '1':即時生效 '2':cycle date 生效
						 + " a.id_p_seqno ,"
						 + " decode(acno_flag,'2',d.chi_name,c.chi_name) as chi_name  ,"
						 + " decode(autopay_acct_no,'','', autopay_indicator) as autopay_indicator ," //自動扣繳指示碼 -- 1.扣 TTL, 2.扣MP 3.其它
						 + " autopay_fix_amt ,"
						 + " autopay_rate ,"
						 + " autopay_acct_s_date ,"
						 + " autopay_acct_e_date ,"
						 + " p_seqno , "
						 + " a.corp_p_seqno "
			  		 + " from act_acno a "
	      		 + " left join crd_idno c on a.id_p_seqno = c.id_p_seqno "
					   + " left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno "
						 + h_where_q_acno
				   //+ " order by tt_which_table , id_no  "
					   ;

	  wp.sqlCmd = ""
 	  				 + " select "
						 + " count(*) as pseqno_cnt ,"
						 + " min(tt_which_table) as tt_which_table ,"
					 //+ " min(id_no) as id_no ,"
					   + " min(tt_id_no) as tt_id_no ,"
						 + " min(acct_type) as acct_type ,"
						 + " min(acct_key) as acct_key ,"
						 + " min(autopay_acct_bank) as autopay_acct_bank ,"
						 + " min(autopay_acct_no) as autopay_acct_no ,"
						 + " min(card_no) as card_no ,"
					 //+ " min(autopay_id) as autopay_id ,"
					 //+ " min(autopay_id_code) as autopay_id_code ,"
					   + " min(tt_autopay_id) as tt_autopay_id ,"
						 + " min(valid_flag) as valid_flag ,"
						 + " min(id_p_seqno) as id_p_seqno ,"
						 + " min(chi_name) as chi_name ,"
						 + " min(autopay_indicator) as autopay_indicator ,"
						 + " min(autopay_fix_amt) as autopay_fix_amt ,"
						 + " min(autopay_rate) as autopay_rate ,"
						 + " min(autopay_acct_s_date) as autopay_acct_s_date ,"
						 + " min(autopay_acct_e_date) as autopay_acct_e_date ,"
						 + " p_seqno , "
						 + " min(corp_p_seqno) as corp_p_seqno "
			  		 + " from ( "
			  		 + ls_sub_sqlCmd
			  		 + " ) "
				  	 + " group by p_seqno "
			  		 + " having count(*) >= 1 "
				  	 + " order by p_seqno "
				   //+ " order by tt_id_no, p_seqno "
					   ;
	  ***/

		String lsSubSqlCmd = ""
 	  				 + " select "
						 + " 'act_chkno' as tt_which_table ,"
						 + " decode(b.acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(b.acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						 + " A.p_seqno  "
			  		 + " from act_chkno A , act_acno B "
	      		 + " left join crd_idno c on b.id_p_seqno = c.id_p_seqno "
					   + " left join crd_corp d on b.corp_p_seqno = d.corp_p_seqno "
						 + hWhereQChkno
						 + " union "
 	  				 + " select "
						 + " 'act_acno' as tt_which_table ,"
						 + " decode(acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						 + " a.p_seqno  "
			  		 + " from act_acno a "
	      		 + " left join crd_idno c on a.id_p_seqno = c.id_p_seqno "
					   + " left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno "
						 + hWhereQAcno
				   //+ " order by tt_which_table , tt_id_no  "
					   ;

	  wp.sqlCmd = ""
 	  				 + " select "
						 + " count(*) ,"
						 + " max(tt_which_table) as tt_which_table ,"
					   + " max(tt_id_no) as tt_id_no ,"
						 + " p_seqno  "
			  		 + " from ( "
			  		 + lsSubSqlCmd
			  		 + " ) "
				  	 + " group by p_seqno "
			  		 + " having count(*) >= 1 "
				     + " order by p_seqno "
				   //+ " order by tt_id_no, p_seqno "
					   ;

		wp.pageCountSql =""
				+"select count(*) from ( "
				+ wp.sqlCmd 
				+" )"
				;
		
		pageQuery();
		
		if(sqlNotFind()){
			alertErr2("此條件查無資料");
			return ;
		}

		queryAfter();
		wp.setListCount(0);
		wp.setPageValue();
	}
	
	void queryAfter() throws Exception {
		String ss = "";

		String sql1 = " select "
						+ " chi_name "
						+ " from crd_corp "
						+ " where corp_p_seqno = ? "
						;
		
		for(int ii=0 ; ii<wp.selectCnt ; ii++) { 

      ss  = wp.colStr(ii,"p_seqno");
    //若同時存在act_acno、act_chkno，則 max(tt_which_table) 會取 act_chkno 那一筆

/***
      if (wkChkActChkno(ii,ss) == 0)  {
        wp.colSet(ii,"tt_which_table", "act_chkno");
      }
      else  {
        wkChkActAcno(ii,ss);
        wp.colSet(ii,"tt_which_table", "act_acno");
      }  	  
***/
      if ( wp.colStr(ii,"tt_which_table").equals("act_chkno") ) {
         wkChkActChkno(ii,ss);
      }
      else  {
         wkChkActAcno(ii,ss);
      }

			if(wp.colEq(ii,"valid_flag", "1")){
				wp.colSet(ii,"tt_valid_flag", "即時生效");
			}	else if(wp.colEq(ii,"valid_flag", "2")){
				wp.colSet(ii,"tt_valid_flag", "CYCLE生效");
			}
			
			if(wp.colEq(ii,"autopay_indicator", "1")){
				wp.colSet(ii,"tt_autopay_indicator", "扣TTL");
			}	else if(wp.colEq(ii,"autopay_indicator", "2")){
				wp.colSet(ii,"tt_autopay_indicator", "扣MP");
			}	
			
			if(wp.colEq(ii,"acct_type", "02") && eqIgno(commString.mid(wp.colStr(ii,"acct_key"), 8,3),"000")){
				sqlSelect(sql1,new Object[]{wp.colStr(ii,"corp_p_seqno")});
				if(sqlRowNum>0){
					wp.colSet(ii,"chi_name", sqlStr("chi_name"));
				}
			}												
		}
	}

  int wkChkActChkno(int txIi, String txPSeqno) throws Exception  {
   	 
    String sql0 = " select "
						+ " decode(b.acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(b.acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						+ " A.acct_type ,"
						+ " B.acct_key ,"
						+ " a.autopay_acct_bank ,"
						+ " a.autopay_acct_no ,"
						+ " a.card_no ,"
						+ " a.autopay_id ,"
						+ " A.autopay_id || '-' || A.autopay_id_code as tt_autopay_id  ,"
						+ " a.valid_flag ," //生效日旗標 -- '1':即時生效 '2':cycle date 生效
					  + " a.id_p_seqno ,"
						+ " decode(b.acno_flag,'2',d.chi_name,c.chi_name) as chi_name  ,"
						+ " a.autopay_indicator ," //自動扣繳指示碼 -- 1.扣 TTL, 2.扣MP 
					//+ " a.autopay_fix_amt ,"
					//+ " a.autopay_rate ,"
						+ " a.autopay_acct_s_date ,"
						+ " a.autopay_acct_e_date ,"
					//+ " a.p_seqno , "
					  + " b.corp_p_seqno "
			  	  + " from act_chkno A , act_acno B "
	      		+ " left join crd_idno c on b.id_p_seqno = c.id_p_seqno "
					  + " left join crd_corp d on b.corp_p_seqno = d.corp_p_seqno "
   				  + " where a.p_seqno = ? "
						+ " and a.p_seqno = b.acno_p_seqno "
						+ " and uf_nvl(a.curr_code,'901') = '901' "
      	  	+ " and a.ad_mark <> 'D' "
						;
   	 
    sqlSelect(sql0,new Object[]{txPSeqno});
  //sqlSelect(sql_0);
   	 
    if(sqlRowNum<=0) {
       return -1;
    }
   	 
    wp.colSet(txIi,"tt_id_no", sqlStr("tt_id_no"));
    wp.colSet(txIi,"acct_type", sqlStr("acct_type"));
    wp.colSet(txIi,"acct_key", sqlStr("acct_key"));
    wp.colSet(txIi,"autopay_acct_bank", sqlStr("autopay_acct_bank"));
    wp.colSet(txIi,"autopay_acct_no", sqlStr("autopay_acct_no"));
    wp.colSet(txIi,"card_no", sqlStr("card_no"));
  //wp.colSet(txIi,"tt_autopay_id", sqlStr("tt_autopay_id"));
  	if(empty(sqlStr("autopay_id"))) {
      wp.colSet(txIi,"tt_autopay_id", "");
		} else {
      wp.colSet(txIi,"tt_autopay_id", sqlStr("tt_autopay_id"));
		}
		
    wp.colSet(txIi,"valid_flag", sqlStr("valid_flag"));
    wp.colSet(txIi,"id_p_seqno", sqlStr("id_p_seqno"));
    wp.colSet(txIi,"chi_name", sqlStr("chi_name"));
    wp.colSet(txIi,"autopay_indicator", sqlStr("autopay_indicator"));
  //wp.colSet(txIi,"autopay_fix_amt", sqlStr("autopay_fix_amt"));
  //wp.colSet(txIi,"autopay_rate", sqlStr("autopay_rate"));
    wp.colSet(txIi,"autopay_acct_s_date", sqlStr("autopay_acct_s_date"));
    wp.colSet(txIi,"autopay_acct_e_date", sqlStr("autopay_acct_e_date"));
    wp.colSet(txIi,"corp_p_seqno", sqlStr("corp_p_seqno"));
   	 
    return 0;
  }
    
  int wkChkActAcno(int txIi, String txPSeqno) throws Exception  {
   	 
    String sql1 = " select "
						+ " decode(a.acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(a.acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						+ " a.acct_type ,"
						+ " a.acct_key ,"
						+ " a.autopay_acct_bank ,"
						+ " a.autopay_acct_no ,"
						+ " '' card_no ,"
						+ " a.autopay_id ,"
						+ " a.autopay_id || '-' || a.autopay_id_code as tt_autopay_id  ,"
						+ " '' as valid_flag ," //生效日旗標 -- '1':即時生效 '2':cycle date 生效
						+ " a.id_p_seqno ,"
						+ " decode(a.acno_flag,'2',d.chi_name,c.chi_name) as chi_name  ,"
						+ " decode(autopay_acct_no,'','', autopay_indicator) as autopay_indicator ," //自動扣繳指示碼 -- 1.扣 TTL, 2.扣MP
					//+ " a.autopay_fix_amt ,"
					//+ " a.autopay_rate ,"
						+ " a.autopay_acct_s_date ,"
						+ " a.autopay_acct_e_date ,"
					//+ " a.p_seqno , "
					  + " a.corp_p_seqno "
			  		+ " from act_acno a "
	      		+ " left join crd_idno c on a.id_p_seqno = c.id_p_seqno "
					  + " left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno "
   				  + " where a.p_seqno = ? "
						+ " and a.p_seqno = a.acno_p_seqno "
						;
   	 
    sqlSelect(sql1,new Object[]{txPSeqno});
  //sqlSelect(sql_1);
   	 
    if(sqlRowNum<=0) {
       return -1;
    }
   	 
    wp.colSet(txIi,"tt_id_no", sqlStr("tt_id_no"));
    wp.colSet(txIi,"acct_type", sqlStr("acct_type"));
    wp.colSet(txIi,"acct_key", sqlStr("acct_key"));
    wp.colSet(txIi,"autopay_acct_bank", sqlStr("autopay_acct_bank"));
    wp.colSet(txIi,"autopay_acct_no", sqlStr("autopay_acct_no"));
    wp.colSet(txIi,"card_no", sqlStr("card_no"));
    wp.colSet(txIi,"autopay_id", sqlStr("autopay_id"));
  //wp.colSet(txIi,"tt_autopay_id", sqlStr("tt_autopay_id"));
  	if(empty(sqlStr("autopay_id"))) {
      wp.colSet(txIi,"tt_autopay_id", "");
		} else {
      wp.colSet(txIi,"tt_autopay_id", sqlStr("tt_autopay_id"));
		}
		
    wp.colSet(txIi,"valid_flag", sqlStr("valid_flag"));
    if(!wp.colEmpty(txIi,"autopay_acct_no")) {
			if (commString.mid(wp.colStr(txIi,"autopay_acct_bank"), 0, 3).equals("006")) {
			  wp.colSet(txIi,"valid_flag", "1");
			}	
			else {
			  wp.colSet(txIi,"valid_flag", "2");
			}
		}
		
    wp.colSet(txIi,"id_p_seqno", sqlStr("id_p_seqno"));
    wp.colSet(txIi,"chi_name", sqlStr("chi_name"));
    wp.colSet(txIi,"autopay_indicator", sqlStr("autopay_indicator"));
  //wp.colSet(txIi,"autopay_fix_amt", sqlStr("autopay_fix_amt"));
  //wp.colSet(txIi,"autopay_rate", sqlStr("autopay_rate"));
    wp.colSet(txIi,"autopay_acct_s_date", sqlStr("autopay_acct_s_date"));
    wp.colSet(txIi,"autopay_acct_e_date", sqlStr("autopay_acct_e_date"));
    wp.colSet(txIi,"corp_p_seqno", sqlStr("corp_p_seqno"));
   	 
    return 0;
  }
    
	
	@Override
	public void querySelect() throws Exception {
		kk1 = wp.itemStr2("data_k1");
		kk2 = wp.itemStr2("data_k2");
   	wp.colSet("kk_acct_type", kk1);
   	wp.colSet("kk_acct_key", kk2);
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		//--從第一頁面進來時 kk1 = p_seqno 第二頁面直接讀取 kk1 = kk_acct_key , kk2 = kk_acct_type 
		//--取代以上奇怪的方式：從第一頁面進來時 kk1 = acct_type、kk2 = acct_key 
		//  第二頁面直接讀取 kk1 = kk_acct_type , kk2 = kk_acct_key
		if(empty(kk2)) {
		//kk1 = item_kk("acct_type");
		//kk2 = zzstr.acct_key(item_kk("acct_key"));
  		if(empty(wp.itemStr2("kk_acct_type"))) {
	  		wp.itemSet("kk_acct_type", wp.itemStr2("kp_acct_type"));
	  		wp.colSet("kk_acct_type", wp.itemStr2("kp_acct_type"));
		  } 				
  		if(empty(wp.itemStr2("kk_acct_key"))) {
	  		wp.itemSet("kk_acct_key", wp.itemStr2("kp_acct_key"));
	  		wp.colSet("kk_acct_key", wp.itemStr2("kp_acct_key"));
		  } 				

 			kk1 = wp.itemStr2("kk_acct_type");
			kk2 = commString.acctKey(wp.itemStr2("kk_acct_key"));
			if(kk2.length()!=11)	{
				alertErr2("帳戶帳號輸入錯誤");
				return ;
			}

		}	

		isPSeqno = getPseqno(kk1,kk2);
		if(empty(isPSeqno)) {
			alertErr2("帳戶帳號輸入錯誤 !");
			return ;
		} 				

	  wp.colSet("autopay_acct_bank", "");
	  wp.colSet("autopay_acct_no", "");
	  wp.colSet("autopay_indicator", "1");
	//wp.colSet("autopay_fix_amt", "0");
	//wp.colSet("autopay_rate", "0");
	  wp.colSet("autopay_acct_s_date", "");
	  wp.colSet("autopay_acct_e_date", "");
	  wp.colSet("valid_flag", "1");
	  wp.colSet("autopay_id", "");
	  wp.colSet("autopay_id_code", "");
	  wp.colSet("edda_reentry_flag", "");
	  wp.colSet("verify_flag", "N");
	  wp.colSet("verify_date", "");
	  wp.colSet("verify_return_code", "");
		
		wp.selectSQL = ""
						 + " p_seqno ,"
						 + " acct_type ,"
						 + " uf_acno_key(p_seqno) as acct_key ,"
						 + " appl_no ,"
						 + " id_p_seqno ,"
					 //+ " uf_idno_idcode(id_p_seqno, acct_type) as tt_id_no ,"
						 + " (select id_no||id_no_code from crd_idno " 
						 + " where id_p_seqno = act_chkno.id_p_seqno) as tt_id_no ,"
						 + " autopay_acct_bank ,"
						 + " autopay_acct_no , "
						 + " autopay_acct_no as ori_autopay_acct_no ,"
						 + " card_no ,"
						 + " autopay_indicator ,"
					 //+ " autopay_fix_amt ,"
					 //+ " autopay_rate ,"
						 + " issue_date ,"
						 + " autopay_acct_s_date ,"
						 + " autopay_acct_e_date ,"
						 + " valid_flag ,"
						 + " reject_code ,"
						 + " from_mark ,"
						 + " verify_flag ,"
						 + " verify_date ,"
						 + " verify_return_code ,"
						 + " exec_check_flag ,"
						 + " exec_check_flag as kp_exec_check_flag,"
						 + " exec_check_date ,"
						 + " ibm_check_flag ,"
						 + " ibm_check_date ,"
						 + " ibm_return_code ,"
						 + " update_main_flag ,"
						 + " update_main_date ,"
						 + " stmt_cycle ,"
						 + " autopay_id_p_seqno ,"
						 + " autopay_id ,"
						 + " autopay_id_code ,"
						 + " proc_mark ,"
						 + " crt_date ,"
						 + " mod_user ,"
						 + " mod_time ,"
						 + " mod_pgm ,"
						 + " mod_seqno ,"
						 + " '' as cissue_date ,"
						 + " '' as cname ,"
						 + " '' as corpname ,"
						 + " '' as cautopay_acct_s_date ,"
						 + " '' as cautopay_acct_e_date ,"
						 + " crt_user ,"
						 + " crt_time ,"
						 + " old_acct_bank ,"
						 + " old_acct_no ,"
						 + " old_acct_id ,"
						 + " ad_mark ,"
						 + " hex(rowid) as rowid ,"
						 + " sms_send_date ,"
						 + " sms_send_cnt ,"
						 + " effc_flag , "
						 + " edda_reentry_flag , "
						 + " uf_idno_name(id_p_seqno) as chi_name  "				 		 
						 ;
		wp.daoTable = "act_chkno";
		wp.whereStr = " where 1=1 "
						+ " and ad_mark <> 'D' "
						+ " and uf_nvl(curr_code,'901') = '901' "
						+ sqlCol(isPSeqno,"p_seqno")
						;
		
		pageSelect();
		wp.colSet("pending_msg", "異動暫存");
		
		if(sqlNotFind()){
		  wp.colSet("pending_msg", "");
			if(readActAcno()==false){
				alertErr2("此條件查無資料");
				return ;
			}
		}
		
		dataReadAfter();

	  wp.colSet("chk_autopay_acct_bank", wp.colStr("autopay_acct_bank"));
	  wp.colSet("chk_autopay_acct_no", wp.colStr("autopay_acct_no"));
	  wp.colSet("chk_autopay_indicator", wp.colStr("autopay_indicator"));
	//wp.colSet("chk_autopay_fix_amt", wp.colStr("autopay_fix_amt"));
	//wp.colSet("chk_autopay_rate", wp.colStr("autopay_rate"));
	  wp.colSet("chk_autopay_acct_s_date", wp.colStr("autopay_acct_s_date"));
	  wp.colSet("chk_autopay_acct_e_date", wp.colStr("autopay_acct_e_date"));
	  wp.colSet("chk_valid_flag", wp.colStr("valid_flag"));
	  wp.colSet("chk_autopay_id", wp.colStr("autopay_id"));
	  wp.colSet("chk_autopay_id_code", wp.colStr("autopay_id_code"));
	  wp.colSet("chk_edda_reentry_flag", wp.colStr("edda_reentry_flag"));
	//wp.col_set("chk_verify_flag", wp.colStr("verify_flag"));
	  wp.colSet("chk_verify_flag", wp.colStr("verify_flag").equals("Y") ? "Y" : "N");
	  wp.colSet("chk_verify_return_code", wp.colStr("verify_return_code"));
		
  //讀出明細資料後，將鍵值存至 kp_ 並設定防止鍵值輸入屬性
		String s1 = wp.colStr("p_seqno");
    if ( !empty(s1) ) {
      wp.colSet("kp_acct_type", kk1 );
      wp.colSet("kp_acct_key", kk2 );
      wp.colSet("kk_acct_type_attr", "disabled");
      wp.colSet("kk_acct_key_attr", "disabled");
    //btnOn_query(false);
    }

	}
	
	void dataReadAfter() throws Exception{

		String ss = "";
		ss = wp.colStr("verify_return_code");
	
    if ( (!empty(ss)) && (!ss.equals("00")) && (!ss.equals("99")) ) {
       ss = "01";
	     wp.colSet("verify_return_code", ss);
    }

		if(wp.colEmpty("stmt_cycle")) { 
			wp.colSet("stmt_cycle", isStmtCycle);
		}
		
		if(wp.colEq("from_mark", "01")){
			wp.colSet("tt_from_mark", "APS");			
		}	else if(wp.colEq("from_mark", "02")){
			wp.colSet("tt_from_mark", "授權書-新申請");
		}	else if(wp.colEq("from_mark", "03")){
			wp.colSet("tt_from_mark", "授權書-修改帳號");
		}	else if(wp.colEq("from_mark", "04")){
			wp.colSet("tt_from_mark", "官網-eDDA");
		}
		
		if(wp.colEq("ibm_return_code", "0")){
			wp.colSet("tt_ibm_return_code", "正常");
		}	else if(wp.colEq("ibm_return_code", "1")){
			wp.colSet("tt_ibm_return_code", "非本人帳戶");
		}	else if(wp.colEq("ibm_return_code", "2")){
			wp.colSet("tt_ibm_return_code", "無效帳戶");
		}
		
		String sql1 = " select "
						+ " uf_corp_name(corp_p_seqno) as corp_chi_name "
						+ " from act_acno "
						+ " where acno_p_seqno = ? "
						;
		
		sqlSelect(sql1,new Object[]{wp.colStr("p_seqno")});
		
		if(sqlRowNum>0){
			wp.colSet("corp_chi_name", sqlStr("corp_chi_name"));
		}
		
		String lsAcctBank = wp.colStr("autopay_acct_bank");
		if (lsAcctBank.length() == 3)
		{
		  String sql2 = " select "
			  			+ " bank_no as autopay_acct_bank "
				  		+ " from act_ach_bank "
						  + " where substr(bank_no,1,3) = ? "						
					  	;
		
		  sqlSelect(sql2,new Object[]{lsAcctBank});
		
		  if(sqlRowNum>0){
			  wp.colSet("autopay_acct_bank", sqlStr("autopay_acct_bank"));
 	  	}
		}
		
	//if(wp.col_empty("autopay_acct_s_date")){
	//	wp.col_set("autopay_acct_s_date", selectBusinDay());
	//}
		
	}
	
	boolean readActAcno() throws Exception{
		wp.selectSQL = ""
				 		 + " a.p_seqno ,"
				 		 + " a.stmt_cycle ,"
				 		 + " a.autopay_rate ,"
				 		 + " a.autopay_fix_amt ,"
				 	 //+ " b.autopay_acct_bank, "
				 	 //+ " b.autopay_acct_no, "
				 	 //+ " b.autopay_id, "
				 	 //+ " b.autopay_id_code, "
				 		 + " a.autopay_acct_bank, "
				 		 + " a.autopay_acct_no, "
				 		 + " a.autopay_id, "
				 		 + " a.autopay_id_code, "
				 		 + " a.autopay_acct_s_date ,"
				 		 + " a.autopay_acct_e_date ,"
					 //+ " uf_idno_idcode(a.id_p_seqno, a.acct_type) as tt_id_no ,"
						 + " (select id_no||id_no_code from crd_idno " 
						 + " where id_p_seqno = a.id_p_seqno) as tt_id_no ,"
				 	 //+ " a.autopay_indicator ,"
						 + " decode(a.autopay_acct_no,'','1', a.autopay_indicator) as autopay_indicator ," 
					 //若無自扣帳號則預設輸入自動扣繳指示碼為"1" -- 1.扣 TTL, 2.扣MP 
				 		 + " a.id_p_seqno ,"
				 		 + " a.corp_p_seqno ,"
				 		 + " a.acct_type ,"
				 		 + " a.acct_key , "
				 		 + " uf_idno_name(a.id_p_seqno) as chi_name , "
				 		 + " uf_corp_name(a.corp_p_seqno) as corp_chi_name , "
				 	 //+ " b.autopay_acct_no as ori_autopay_acct_no "
				 		 + " a.autopay_acct_no as ori_autopay_acct_no "
				 		 ;
		wp.daoTable = " act_acct_curr b, act_acno a ";
		wp.whereStr = " where 1=1 and b.p_seqno = a.acno_p_seqno "
		            + " and b.curr_code = '901' "
						    +sqlCol(isPSeqno,"a.acno_p_seqno")
						;
		pageSelect();
		if(sqlNotFind()){			
			return false;
		}
    //wp.ddd("-->Actm0010-dsp01","read act_acno, act_acct_curr ");
    //wp.ddd("--:act_acno.autopay_acct_e_date[%s]",wp.col_ss("autopay_acct_e_date"));
		
    if(!wp.colEmpty("autopay_acct_no")) {
			if (commString.mid(wp.colStr("autopay_acct_bank"), 0, 3).equals("006")) {
			  wp.colSet("valid_flag", "1");
			}	
			else {
			  wp.colSet("valid_flag", "2");
			}
		}
		
		readAcnoAfter();
		return true;
	}
	
	String selectBusinDay() throws Exception {
		
		String lsBusinessDate = "";
		
		String sql1 = " select "
						+ " business_date "
						+ " from ptr_businday "
						+ " where 1=1 "
						;
		
		sqlSelect(sql1);
		if(sqlRowNum<=0){			
			return "";
		}
		
		lsBusinessDate = sqlStr("business_date");
		lsBusinessDate = commDate.dateAdd(lsBusinessDate, 0, 0, 1);
		
		String sql2 = " select "
						+ " count(*) as db_cnt "
						+ " from ptr_holiday "
						+ " where holiday = ? "
						;
		
		while(true){
			sqlSelect(sql2,new Object[]{lsBusinessDate});
			
			if(sqlNum("db_cnt")==0)	break;
			
			lsBusinessDate = commDate.dateAdd(lsBusinessDate, 0, 0, 1);
		}
		
		
		return lsBusinessDate;
	}
	
	void readAcnoAfter(){
		if(wp.colEmpty("autopay_id_code")){
			wp.colSet("autopay_id_code", "0");
		}
		
		if(wp.colEmpty("autopay_acct_bank") || wp.colEmpty("autopay_acct_no")){
			wp.colSet("from_mark", "02");
		}	else	{
			wp.colSet("from_mark", "03");
		}
		
	//if(wp.col_empty("autopay_acct_s_date")){
	//	wp.col_set("autopay_acct_s_date", selectBusinDay());
	//}
		
	}
	
	void detlWkdata() {
		String ss = "";

		ss = wp.colStr("from_mark");
		wp.colSet("tt_from_mark", commString.decode(ss, ",01,02,03,04", ",APS,授權書-新申請,授權書-修改帳號,官網eDDA"));

		ss = wp.colStr("verify_return_code");
	
    if ( (!empty(ss)) && (!ss.equals("00")) && (!ss.equals("99")) ) {
       ss = "01";
    }
		wp.colSet("tt_verify_return_code", commString.decode(ss, ",00,01,99", ",成功,失敗,免驗印"));
	}

	@Override
	public void saveFunc() throws Exception {
		if (!funSaveCheck())
			return;
		
		Actm0010Func func = new Actm0010Func(wp);

		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc);
		dddwSelect();
	}

	String getPseqno(String lsAcctType , String lsAcctKey) throws Exception {
		
		String lsSql = " select p_seqno,stmt_cycle from act_acno ";
		lsSql += " where acct_type = :acct_type and acct_key = :acct_key ";
		lsSql += "   and acno_p_seqno = p_seqno ";
		setString("acct_type", lsAcctType);
		setString("acct_key", lsAcctKey);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			isStmtCycle = sqlStr("stmt_cycle");
			return sqlStr("p_seqno");
		}
		return "";
	}

	@Override
	public void initButton() {

    if (wp.respHtml.equals("actm0010"))  {
        this.btnModeAud();
    }

		if (wp.respHtml.indexOf("_detl") > 0) {
      buttonOff("btnSms_disable"); //發送簡訊鍵 default off(disabled)
			this.btnModeAud();//rowid 有值時，新增鍵 off(disabled)，修改鍵、刪除鍵 on
			if  (empty(wp.colStr("p_seqno")) )  {  //p_seqno 無值時(沒有先讀act_acno)，新增鍵 off(disabled)
           btnAddOn(false);
		  }
			if  (!empty(wp.colStr("rowid")) )  {  //rowid 有值時(有讀到act_chkno)，發送簡訊鍵 on
           wp.colSet("btnSms_disable","");
		  }
		}
	}

	@Override
	public void dddwSelect() {
		try {
			
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			
			wp.optionKey = wp.colStr("ex_autopay_acct_bank");
			this.dddwList("dddw_autopay_acct_bank", "act_ach_bank", "bank_no", "bank_name", "where 1=1");

			
			wp.initOption = "";
			wp.optionKey = wp.colStr("kk_acct_type");
			this.dddwList("dddw_acct_type2", "ptr_acct_type", "acct_type", "chin_name",
					"where 1=1 order by acct_type");
		
			wp.optionKey = wp.colStr("autopay_acct_bank");
			this.dddwList("dddw_autopay_acct_bank2", "act_ach_bank", "bank_no", "bank_name", "where 1=1");
		} catch (Exception ex) {
		}
	}

	public void wfAjaxAcctbank() throws Exception {
		String lsAcctbank = "";

	  lsAcctbank = wp.itemStr2("ax_autopay_acct_bank").substring(0, 3);

		if (lsAcctbank.equals("006")) {
	   	 wp.addJSON("valid_flag","1");
		}
		else                           {
	   	 wp.addJSON("valid_flag","2");
		}
		
	}

	private boolean funSaveCheck() throws Exception {

  //檢核輸入明細資料前亦需先搬回鍵值並設定防止鍵值輸入屬性
    wp.colSet("kk_acct_type", wp.itemStr2("kp_acct_type") ); 
    wp.colSet("kk_acct_key", wp.itemStr2("kp_acct_key") );
    wp.colSet("kk_acct_type_attr", "disabled");
    wp.colSet("kk_acct_key_attr", "disabled");
  //btnOn_query(false);

//		String ls_p_seqno = "";
//		String accttype = "",acctkey = "";
		String lsSql = "";
		double liSmsCnt = 0;
		liSmsCnt = wp.itemNum("sms_send_cnt"); 

    wp.colSet("exec_check_flag", wp.itemStr2("kp_exec_check_flag") ); 
		if (wp.itemStr2("kp_exec_check_flag").equals("Y")) {
			alertErr2("主管已覆核不可修改或刪除資料 !");
			return false;
		}
		
		if(this.isDelete())	return true;
		
		//--是否異動--
		String lsEddaReentryFlag = "", lsVerifyFlag = "";
		lsEddaReentryFlag = wp.itemStr2("edda_reentry_flag").equals("Y") ? "Y" : "N";
		lsVerifyFlag       = wp.itemStr2("verify_flag").equals("Y") ? "Y" : "N";
		if(this.isUpdate())	{
		  if (   eqIgno(commString.mid(wp.itemStr2("autopay_acct_bank"), 0, 3), commString.mid(wp.itemStr2("chk_autopay_acct_bank"), 0, 3)) 
				  && eqIgno(wp.itemStr2("autopay_acct_no"),wp.itemStr2("chk_autopay_acct_no"))
				  && eqIgno(wp.itemStr2("autopay_indicator"),wp.itemStr2("chk_autopay_indicator"))
				//&& wp.itemNum("autopay_fix_amt")==wp.itemNum("chk_autopay_fix_amt")
				//&& wp.itemNum("autopay_rate")==wp.itemNum("chk_autopay_rate")                
				  && eqIgno(wp.itemStr2("autopay_acct_s_date"),wp.itemStr2("chk_autopay_acct_s_date"))
				  && eqIgno(wp.itemStr2("autopay_acct_e_date"),wp.itemStr2("chk_autopay_acct_e_date"))
				  && eqIgno(wp.itemStr2("valid_flag"),wp.itemStr2("chk_valid_flag"))
				  && eqIgno(wp.itemStr2("autopay_id"),wp.itemStr2("chk_autopay_id"))
				  && eqIgno(wp.itemStr2("autopay_id_code"),wp.itemStr2("chk_autopay_id_code"))
				  && eqIgno(lsEddaReentryFlag,wp.itemStr2("chk_edda_reentry_flag"))
				  && eqIgno(lsVerifyFlag,wp.itemStr2("chk_verify_flag"))
				  && eqIgno(wp.itemStr2("verify_return_code"),wp.itemStr2("chk_verify_return_code"))
         )
			{
			  alertErr("資料未異動, 不可存檔");
			  return false;
	 	  } 
		}
		
		//--檢核 "扣繳行庫為本行(006), CYCLE當日或前一營業日; 不可異動資料及發送簡訊"
		//if(wfChkCycdate()!=1)	return false; 


/***
		if(eqIgno(wp.itemStr2("autopay_acct_bank").substring(0, 3),"006")){
			//--檢核帳號
			if(checkAcct()==false){
				errmsg("扣繳帳號不合法");
				return false;
			}
			//--檢核活存帳戶
			if(checkLife()==false){
				errmsg("台幣帳號錯誤");
				return false ;
			}
		}
		
		//--檢核台幣帳戶
		if(checkTw()==false){
			errmsg("扣繳帳號非台幣帳戶");
			return false;
		}
***/
		
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysdate = df.format(new Date());

		if (wp.itemStr2("verify_flag").equals("Y")) {
			if(wp.itemEmpty("verify_return_code")){
				alertErr2("驗印完畢必須有原因碼！");
				return false;
			}			
			wp.colSet("verify_date",sysdate);			
			wp.itemSet("verify_date",sysdate);			
		}	else {			
			wp.colSet("verify_return_code","");			
			wp.itemSet("verify_return_code","");
			wp.colSet("verify_date","");			
			wp.itemSet("verify_date","");			
		}

		if (wp.itemStr2("autopay_acct_bank").length() < 7) {
			alertErr2("扣繳行庫長度不足 7 碼 !!");
			return false;
		}

		if(wp.itemStr2("autopay_acct_no").length()!=16){
			alertErr2("扣繳帳號長度不足 16 碼 !!");
			return false;
		}

		//User 1707 reflected following is a incorrect check on 2019/11/21 
		//if(isUpdate()||isAdd()){
		//	if(wp.sss("ori_autopay_acct_no").equals(wp.sss("autopay_acct_no"))){
		//		alertErr2("未更改 自動扣繳帳號, 不可存檔");
		//		return false;
		//	}
		//}				
		
		//User 1707 reflected autopay_id must be 8 or 10 bytes on 2019/11/21 
		//User 1709 reflected autopay_id may not be 8 or 10 bytes on 2021/10/08 
		//if (wp.item_ss("autopay_id").length() != 8 && wp.item_ss("autopay_id").length() != 10) {
		//	alertErr2("扣繳歸屬ID需為 8 or 10 碼 !!");
		//	return false;
		//}
		

		String lsacctbank = "", lsacctno = "", lsid = "", lsacctedate = "";
		//ls_sql = "SELECT  decode(autopay_acct_bank,'',' ', autopay_acct_bank) as autopay_acct_bank "
		//		+ " , decode (autopay_acct_no,'',' ', autopay_acct_no) as autopay_acct_no "
		//		+ " , decode (autopay_id,'', ' ', autopay_id) as autopay_id "
		//		+ " , decode (autopay_acct_e_date,'', ' ', autopay_acct_e_date) as autopay_acct_e_date"

		lsSql = "SELECT  autopay_acct_bank "
				+ " , autopay_acct_no "
				+ " , autopay_id "
				+ " , autopay_acct_e_date"
				+ " FROM act_acno " + " WHERE act_acno.acno_p_seqno = :ls_p_seqno ";
		setString("ls_p_seqno", wp.itemStr2("p_seqno"));

		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsacctbank = empty(sqlStr("autopay_acct_bank")) ? "": sqlStr("autopay_acct_bank").substring(0, 3);
			lsacctno = sqlStr("autopay_acct_no");
			lsid = sqlStr("autopay_id");
			lsacctedate = sqlStr("autopay_acct_e_date");
		}

		if (!strAction.equals("D")) {
			if (lsacctbank.equals(commString.mid(wp.itemStr2("autopay_acct_bank"), 0,3)) && lsacctno.equals(wp.itemStr2("autopay_acct_no"))
					&& lsid.equals(wp.itemStr2("autopay_id")) && lsacctedate.equals(wp.itemStr2("autopay_acct_e_date"))) {
					alertErr2("非帳號取消作業者, 未更改 自動扣繳帳號, 不可存檔 !");
					return false;
			}						
		}

		lsacctbank = wp.itemStr2("autopay_acct_bank").substring(0, 3);
		int ibankacctno = wp.itemStr2("autopay_acct_no").length();
		String ErrMsg = "";
		
		/***
		if (lsacctbank.equals("004") && (ibankacctno != 12 && ibankacctno != 14)) {
			ErrMsg = "台灣銀行帳號為 12 or 14 碼";
		} else if (lsacctbank.equals("005") && (ibankacctno != 12 && ibankacctno != 13)) {
			ErrMsg = "土地銀行帳號為 12 or 13 碼";
		} else if (lsacctbank.equals("006") && ibankacctno != 13) {
			ErrMsg = "合作金庫帳號為13碼";
		} else if (lsacctbank.equals("007") && ibankacctno != 11) {
			ErrMsg = "第一銀行帳號為11碼";
		} else if (lsacctbank.equals("008") && ibankacctno != 12) {
			ErrMsg = "華南銀行帳號為12碼";	
		} else if (lsacctbank.equals("009") && ibankacctno != 14) {
			ErrMsg = "彰化銀行帳號為14碼";			
		}  else if (lsacctbank.equals("011") && ibankacctno != 14) {
			ErrMsg = "上海銀行帳號為14碼";
		} else if (lsacctbank.equals("012") && (ibankacctno != 12 && ibankacctno != 14)) {
			ErrMsg = "台北富邦銀行帳號為 12 or 14 碼";		
		} else if (lsacctbank.equals("013") && (ibankacctno != 11 && ibankacctno != 12 && ibankacctno != 14)) {
			ErrMsg = "國泰世華銀行帳號為 11 or 12 or 14 碼";
		}  else if (lsacctbank.equals("050") && ibankacctno != 11) {
			ErrMsg = "台灣企銀帳號為11碼";
		} else if (lsacctbank.equals("081") && (ibankacctno != 12 && ibankacctno != 14)) {
			ErrMsg = "匯豐銀行帳號為 12 or 14 碼";	
		} else if (lsacctbank.equals("803") && (ibankacctno != 12 && ibankacctno != 14)) {
			ErrMsg = "聯邦銀行帳號為 12 or 14 碼";
		} else if (lsacctbank.equals("805") && ibankacctno != 14) {
			ErrMsg = "遠東國際銀行帳號為14碼";
		} else if (lsacctbank.equals("806") && ibankacctno != 14) {
			ErrMsg = "元大銀行帳號為14碼";
		} else if (lsacctbank.equals("807") && ibankacctno != 14) {
			ErrMsg = "永豐銀行帳號為14碼";
		} else if (lsacctbank.equals("808") && ibankacctno != 13) {
			ErrMsg = "玉山銀行帳號為13碼";
		} else if (lsacctbank.equals("812") && ibankacctno != 14) {
			ErrMsg = "台新銀行帳號為14碼";
		} else if (lsacctbank.equals("822") && (ibankacctno != 12 && ibankacctno != 13 && ibankacctno != 14)) {
			ErrMsg = "中國信託銀行帳號為 12 or 13 or 14 碼";
		} else if (lsacctbank.equals("700") && (ibankacctno != 14 && ibankacctno != 8)) {
			ErrMsg = "郵局扣繳帳號為14碼 或8碼";
		}
		***/

		if (ErrMsg.length() > 0) {
			alertErr2(ErrMsg);
			return false;
		}

		if (!lsacctbank.equals("006") && wp.itemStr2("valid_flag").equals("1")) {
			alertErr2("繳款行庫非 006 者, 生效旗標必為 cycle end");
			return false;
		}

/***
		String lsInd = wp.itemStr2("autopay_indicator");
	  float lffixamt = empty(wp.itemStr2("autopay_fix_amt")) ? 0 : Float.parseFloat(wp.itemStr2("autopay_fix_amt"));
	  float lfpayrate = empty(wp.itemStr2("autopay_rate")) ? 0 : Float.parseFloat(wp.itemStr2("autopay_rate"));
		if (lsInd.equals(""))
			lsInd = "1";

		if (lsInd.equals("1") || lsInd.equals("2")) {
			if (lffixamt != 0 || lfpayrate != 0 ) {
				alertErr("自動扣繳指示碼為 [扣TTL] 或 [扣MP] 時, 自動扣繳金額及百分比不可以有值!");
				return false;
			}
		} else {
			if (lffixamt == 0 && lfpayrate == 0) {
				alertErr("自動扣繳指示碼為 [其他] 時, 自動扣繳金額或百分比不可以空白!");
				return false;
			} else if (lffixamt != 0 && lfpayrate != 0) {
				alertErr("自動扣繳指示碼為 [其他] 時, 自動扣繳金額及百分比不可以都有值!");
				return false;
			} else if (lfpayrate > 100) {
				alertErr("自動扣繳指示碼為 [其他] 時, 自動扣繳百分比不可以大於100");
			  return false;
			}
		}
***/
		
		String lsrc = "";
		lsSql = "select rc_use_indicator from act_acno where acno_p_seqno = :ls_p_seqno";
		setString("ls_p_seqno", wp.itemStr2("p_seqno"));

		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsrc = sqlStr("rc_use_indicator");
		}

		if (lsrc.equals("3") && !wp.itemStr2("autopay_indicator").equals("1")) {
			alertErr2("此帳戶不準允用RC,請確認");
			return false;
		}
		
	//User 1707 said : 應以驗印成功回覆日期為有效起始日，因為當初key-in的日期非真正生效日，以免持卡人來電詢問時，回覆錯誤
	//if(empty(wp.item_ss("autopay_acct_s_date"))) {
	//	alertErr2("請輸入自動扣繳生效起日!");
	//	return false;
	//}

		try {
			String lsAcctSDate = "", lsAcctEDate = "";
			int isysdate = 0;
			int isdate = 0;
			int iedate = 0;
      //wp.ddd("-->Actm0010-dsp03","");
      //wp.ddd("--:wp.item_ss('autopay_acct_s_date')[%s]",wp.item_ss("autopay_acct_s_date"));
      //wp.ddd("--:wp.item_ss('autopay_acct_e_date')[%s]",wp.item_ss("autopay_acct_e_date"));
      if (wp.itemEmpty("autopay_acct_s_date"))  {
      	lsAcctSDate = "0";
      }
      else {
      	lsAcctSDate = wp.itemStr2("autopay_acct_s_date");
      }

      if (wp.itemEmpty("autopay_acct_e_date"))  {
      	lsAcctEDate = "0";
      }
      else {
      	lsAcctEDate = wp.itemStr2("autopay_acct_e_date");
      }

			try {
				isysdate = Integer.parseInt(sysdate);
      //wp.ddd("-->Actm0010-dsp04","");
      //wp.ddd("--:isysdate[%s]",isysdate);
			//isdate = Integer.parseInt(wp.item_ss("autopay_acct_s_date"));
			//iedate = Integer.parseInt(wp.item_ss("autopay_acct_e_date"));空值執行 Integer.parseInt時，會發生 Exception
				isdate = Integer.parseInt(lsAcctSDate);
				iedate = Integer.parseInt(lsAcctEDate);
      //wp.ddd("-->Actm0010-dsp05","");

			} catch (Exception ex) {
				isdate = 0;
				iedate = 0;
      //wp.ddd("-->Actm0010-dsp06","");
			}

			if (isdate > 0 && isdate < isysdate) {
				alertErr2("自動扣繳生效起日不可小於系統日!");
				return false;
			}
			if (iedate > 0 && iedate < isysdate) {
				alertErr2("自動扣繳生效迄日不可小於系統日!");
				return false;
			}
			if (iedate > 0 && isdate > iedate) {
				alertErr2("自動扣繳生效日輸入錯誤 !");
				return false;
			}
		} catch (Exception e) {

		}
		
	  if(lsacctbank.equals("006")) {
			if(!wp.itemStr2("verify_flag").equals("Y")) {
			  alertErr2("扣繳行庫為006，請勾選「驗印完畢」");
				return false;
			}
		}	else {
			if(wp.itemStr2("verify_flag").equals("Y")) {
			  alertErr2("除扣繳行庫為006外，不得勾選「驗印完畢」");
				return false;
			}
		}
		
		//if(lsacctbank.equals("700")){
		//	if(wp.item_ss("verify_flag").equals("Y")) {
		//		alertErr2("扣繳行庫為700，不可勾選「驗印完畢」");
		//		return false;
		//	}
		//}
		
		//--發送簡訊
		if(wp.itemEq("sms_send_flag", "Y")){
			if(wfCheckSmsSend("1")==-1)	return false;
		}
		
		
		return true;
	}
	
	boolean checkAcct() {
		String lsAutopayAcctNo = "";
		double ldAcctNo1 = 0 , ldAcctNo2 = 0 , ldAcctNo3 = 0 , ldAcctNo4 = 0 , ldAcctNo5 = 0 ;
		double ldAcctNo6 = 0 , ldAcctNo7 = 0 , ldAcctNo8 = 0 , ldAcctNo9 = 0 , ldAcctNo10 = 0 , ldAcctNo11 = 0 ;
		double ldTotal =0 , ldAmt = 0;
		
		lsAutopayAcctNo = wp.itemStr2("autopay_acct_no");
		ldAcctNo1 = commString.strToNum(commString.mid(lsAutopayAcctNo, 0,1));
		ldAcctNo2 = commString.strToNum(commString.mid(lsAutopayAcctNo, 1,1));
		ldAcctNo3 = commString.strToNum(commString.mid(lsAutopayAcctNo, 2,1));
		ldAcctNo4 = commString.strToNum(commString.mid(lsAutopayAcctNo, 3,1));
		ldAcctNo5 = commString.strToNum(commString.mid(lsAutopayAcctNo, 4,1));
		ldAcctNo6 = commString.strToNum(commString.mid(lsAutopayAcctNo, 5,1));
		ldAcctNo7 = commString.strToNum(commString.mid(lsAutopayAcctNo, 6,1));
		ldAcctNo8 = commString.strToNum(commString.mid(lsAutopayAcctNo, 7,1));
		ldAcctNo9 = commString.strToNum(commString.mid(lsAutopayAcctNo, 8,1));
		ldAcctNo10 = commString.strToNum(commString.mid(lsAutopayAcctNo, 9,1));
		ldAcctNo11 = commString.strToNum(commString.mid(lsAutopayAcctNo, 10,1));
		
		ldTotal = ldAcctNo1*4 + ldAcctNo2*3 + ldAcctNo3*2 + ldAcctNo4*8 + ldAcctNo5*7 + ldAcctNo6*6 + ldAcctNo7*5 + ldAcctNo8*4 + ldAcctNo9*3 + ldAcctNo10*2;  
		
		ldAmt = ldTotal % 11;
		
		if(ldAmt == 10)	ldAmt =0;
		
		if(ldAmt != ldAcctNo11){
			return false; 
		}
		
		return true;
	}
	
	boolean checkTW(){
		String lsAutopayAcctNo = "" , lsAcctNo45 = "" , lsChk = "";
		lsAutopayAcctNo = wp.itemStr2("autopay_acct_no");
	//lsAcctNo45 = commString.mid(lsAutopayAcctNo, 3,2);
 		lsAcctNo45 = commString.mid(lsAutopayAcctNo, 6,2);//因前面有補3個零
		lsChk = "|03|05|15|16|17|18|31|32|39|45|46|47|48|49|50|53|54|57|58|66";
		
		if(commString.pos(lsChk,lsAcctNo45)>0){
			return true;
		}		
		return false ;
	}
	
	boolean checkLife(){
		String lsAutopayAcctNo = "" , lsAcctNo45 = "" , lsChk = "";
		lsAutopayAcctNo = wp.itemStr2("autopay_acct_no");
	//lsAcctNo45 = commString.mid(lsAutopayAcctNo, 3,2);
 		lsAcctNo45 = commString.mid(lsAutopayAcctNo, 6,2);//因前面有補3個零
		lsChk = "|01|02|06|07|08|09|10|11|12|13|14|19|20|27|60|61|62|63|64|65";
		
		if(commString.pos(lsChk,lsAcctNo45)>0){
			return true;
		}		
		return false ;
	}
	
	
	void sendSmsMesg() throws Exception{
		//if(wfChkCycdate()!=1)	return; 
		
		if(wfCheckSmsSend("2")==1){
		//alert_msg("發送簡訊成功 , 須重新讀取");
		//alert_err("簡訊產生成功 , 須重新讀取"	);
		alertMsg("簡訊產生成功！");
    //檢核輸入明細資料前亦需先搬回鍵值並設定防止鍵值輸入屬性
      wp.colSet("kk_acct_type", wp.itemStr2("kp_acct_type") ); 
      wp.colSet("kk_acct_key", wp.itemStr2("kp_acct_key") );
      wp.colSet("kk_acct_type_attr", "disabled");
      wp.colSet("kk_acct_key_attr", "disabled");
    //btnOn_query(false);
			
      kk1 = wp.itemStr2("kp_acct_type"); 
      kk2 = wp.itemStr2("kp_acct_key");
			dataRead();
		}		
	}
	
	int wfChkCycdate() throws Exception{
		String lsPSeqno = "" , lsBank = "" , lsBusDate = "" , lsCycleDate = "";
		lsPSeqno = wp.itemStr2("p_seqno");
		lsBank = commString.mid(wp.itemStr2("autopay_acct_bank"), 0,3);
		if(!eqIgno(lsBank,"006"))	return 1;
		
		String sql1 = " select "
						+ " business_date "
						+ " from ptr_businday "
						+ " where 1=1 "
						;
		
		sqlSelect(sql1);
		
		lsBusDate = sqlStr("business_date");
		lsCycleDate = wfCycleDatePrev(lsPSeqno);
		
		if(empty(lsCycleDate)){
			errmsg("無法取得 CYCLE當日或前一營業日 之日期");
			return -1;
		}
		
		//if(lsBusDate.compareTo(lsCycleDate)>=0){
		//	errmsg("扣繳行庫為本行(017), CYCLE當日或前一營業日; 不可異動資料及發送簡訊");
		//	return -1;
		//}
		
		return 1;
	}
	
	String wfCycleDatePrev(String lsPSeqno) throws Exception{
		String lsCycleDate = ""; double llCnt = 0;
		String sql1 = " select "
						+ " uf_date_add(a.next_close_date,0,0,-1) as ls_cycle_date "
						+ " from ptr_workday a, act_acno b "
						+ " where a.stmt_cycle = b.stmt_cycle "
						+ " and b.acno_p_seqno = ? "
						;
		
		sqlSelect(sql1,new Object[]{lsPSeqno});
		if(sqlRowNum<=0)	return "";
		
		lsCycleDate = sqlStr("ls_cycle_date");
		
		String sql2 = " select "
						+ " count(*) as ll_cnt "
						+ " from ptr_holiday "
						+ " where holiday = ? "
						;
		
		while(true){
			sqlSelect(sql2,new Object[]{lsCycleDate});
			if(sqlRowNum<=0){
				return "";
			}
			llCnt = sqlNum("ll_cnt");
			if(llCnt==0)	break;
			
			lsCycleDate = commDate.dateAdd(lsCycleDate, 0, 0, -1);
		}
		
		return lsCycleDate;
	}
	
	int wfCheckSmsSend(String asType) throws Exception{
		String lsPSeqno = "" , lsRowid = "" , lsBank = "";
		
		SmsMsgDetl oosms = new SmsMsgDetl();
		oosms.setConn(wp);
		
		lsPSeqno = wp.itemStr2("p_seqno");
		lsBank = commString.mid(wp.itemStr2("autopay_acct_bank"), 0,3);
		lsRowid = wp.itemStr2("rowid");
		int liRc=1;
		if(eqIgno(lsBank,"006") || eqIgno(lsBank,"700")){
			liRc =oosms.actM0010(lsPSeqno, "A");				
		}	else	{
			liRc =oosms.actM0010(lsPSeqno, "A");				
		}
		if (liRc==-1){
			sqlCommit(liRc);
			alertErr2(oosms.getMsg());
			return -1;
		}
		
		if(eqIgno(asType,"1")){
			wp.colSet("sms_send_date", getSysDate());
		  wp.colSet("sms_send_cnt", wp.itemNum("sms_send_cnt")+1+"");
			wp.itemSet("sms_send_date", getSysDate());
		  wp.itemSet("sms_send_cnt", wp.itemNum("sms_send_cnt")+1+"");
		}	else	{
			Actm0010Func func = new Actm0010Func(wp);
			func.varsSet("rowid", lsRowid);
			liRc =func.updateSms();
			if (liRc==-1) {
				sqlCommit(liRc);
				alertErr2(func.getMsg());
				return -1;
			}
		}
		
		sqlCommit(1);
		return 1;
	}
	
	  public void procFunc() throws Exception {
		    // TODO Auto-generated method stub
				if (itemIsempty("zz_file_name")) {
					alertErr2("上傳檔名: 不可空白");
					return;
				}
				
				if(wp.itemEmpty("ex_file_name")) {
					alertErr2("回覆檔來源: 不能為空");
					return;
				}
				
				if(wp.itemStr("zz_file_name").indexOf(wp.itemStr("ex_file_name"))<0) {
					alertErr2("上傳檔名: 錯誤");
					return;
				}
				if(wp.itemEq("ex_file_name", "ACHR02")){
					procAchr02();
				}
				if(wp.itemEq("ex_file_name", "POST0004")){
					procPost0004();
				}
		  }
		  
		  void procAchr02() throws Exception {
				TarokoFileAccess tf = new TarokoFileAccess(wp);
				String inputFile = wp.itemStr("zz_file_name");

				int fi = tf.openInputText(inputFile, "MS950");
				if (fi == -1) return;

				int llOk = 0, llCnt = 0, errCnt = 0 ,skipCnt = 0,rc = 1;

				while (true) {
					String line = tf.readTextFile(fi);
					if (tf.endFile[fi].equals("Y")) break;
					llCnt++;
					byte[] bytes = line.getBytes("MS950");
					if (bytes.length < 120) {
						setProcessResult(line, "資料長度不符120");
						errCnt++;
						continue;
					}
					skipFlag = false;
		
					//明細
					Actm0010Data actm0010Data = getAchr02Data(bytes);
					
					//頭尾跳過
					if(skipFlag == true)
						continue;
	
					//回覆碼 != R0 , R4  跳過
					if(commString.pos(",R0,R4", actm0010Data.respCode)<0) {
						skipCnt++;
						setProcessResult(line, "此筆資料SKIP,回覆碼非R0,R4");
						continue;
					}
					//新申請註記 != A 跳過
					if("A".equals(actm0010Data.adMark) == false) {
						skipCnt++;
						setProcessResult(line, "此筆資料SKIP,新申請註記不為A");
						continue;
					}
			
					if(selectCrdIdno(actm0010Data) == 0){
						setProcessResult(line, lsMsg);
						errCnt++;
						continue;
					}
					if(selectCrdCard(actm0010Data) == 0){
						setProcessResult(line, lsMsg);
						errCnt++;
						continue;
					}
					
					if (inserrtActChkno(actm0010Data) == -1) {
						setProcessResult(line, lsMsg);
						errCnt++;
						continue;
					}
	
					llOk++;
					
				}
				
				wp.selectCnt = listCnt;

				String finalResult = String.format("資料匯入處理筆數[%d], 成功筆數[%d], 錯誤筆數[%d] ,skip筆數[%d]", llCnt-2, llOk, errCnt ,skipCnt);
				if((llCnt-2) != ttlCnt ) {
					finalResult = String.format("資料筆數[%d]與尾筆筆數[%d]不符合",llCnt-2,ttlCnt);
					wp.selectCnt = 0;
					rc = 0;
				}
				wp.setListCount(1);
				sqlCommit(rc);
				wp.showLogMessage("I", "", finalResult);
				wp.alertMesg(finalResult);
				tf.closeInputText(fi);
				wp.colSet("zz_file_name", "");
			}
		  
		  
		  
		  void procPost0004() throws Exception {
				TarokoFileAccess tf = new TarokoFileAccess(wp);
				String inputFile = wp.itemStr("zz_file_name");

				int fi = tf.openInputText(inputFile, "MS950");
				if (fi == -1) return;

				int llOk = 0, llCnt = 0, errCnt = 0 ,skipCnt = 0,rc = 1;

				while (true) {
					String line = tf.readTextFile(fi);
					if (tf.endFile[fi].equals("Y")) break;
					byte[] bytes = line.getBytes("MS950");
					llCnt++;
					if (bytes.length < 100) {
						setProcessResult(line, "資料長度不符100");
						errCnt++;
						continue;
					}
					skipFlag = false;
		
					//明細
					Actm0010Data actm0010Data = getPost0004Data(bytes);
					
					//尾跳過
					if(skipFlag == true)
						continue;
	
					//申請代號 =’1’ , ’4’
					if(commString.pos(",1,4",actm0010Data.adMark)<0) {
						skipCnt++;
						setProcessResult(line, "此筆資料SKIP,申請代號非1,4");
						continue;
					}
					//狀況代號 =’’  空白
					if(empty(actm0010Data.rtnStatus) == false) {
						skipCnt++;
						setProcessResult(line, "此筆資料SKIP,狀況代號不為空白");
						continue;
					}
					//核對註記 =’’  空白
					if(empty(actm0010Data.checkFlag) == false) {
						skipCnt++;
						setProcessResult(line, "此筆資料SKIP,核對註記不為空白");
						continue;
					}
			
					if(selectCrdIdno(actm0010Data) == 0){
						setProcessResult(line, lsMsg);
						errCnt++;
						continue;
					}
					if(selectCrdCard(actm0010Data) == 0){
						setProcessResult(line, lsMsg);
						errCnt++;
						continue;
					}
					
					if (inserrtActChkno(actm0010Data) == -1) {
						setProcessResult(line, lsMsg);
						errCnt++;
						continue;
					}
	
					llOk++;
					
				}
				
				wp.selectCnt = listCnt;

				String finalResult = String.format("資料匯入處理筆數[%d], 成功筆數[%d], 錯誤筆數[%d] ,skip筆數[%d]", llCnt-1, llOk, errCnt ,skipCnt);
				if((llCnt-1) != ttlCnt ) {
					finalResult = String.format("資料筆數[%d]與尾筆筆數[%d]不符合",llCnt-1,ttlCnt);
					wp.selectCnt = 0;
					rc = 0;
				}
				wp.setListCount(1);
				sqlCommit(rc);
				wp.showLogMessage("I", "", finalResult);
				wp.alertMesg(finalResult);
				tf.closeInputText(fi);
				wp.colSet("zz_file_name", "");
			}

			private Actm0010Data getAchr02Data(byte[] bytes) throws Exception {
				Actm0010Data actm0010Data = new Actm0010Data();
				String col0 = subMS950String(bytes, 0, 1);
				busDate = getBusDate();
				if ("B".equals(col0)) {
					skipFlag = true;
					return actm0010Data;
				}

				if ("E".equals(col0)) {
					ttlCnt = commString.strToInt(subMS950String(bytes, 3, 8));
					skipFlag = true;
					return actm0010Data;
				}
				actm0010Data.achBankNo = subMS950String(bytes, 19, 7);
				actm0010Data.achAutopayAcctNo = commString.lpad(subMS950String(bytes, 26, 14),16,"0");
				actm0010Data.achAutopayId = subMS950String(bytes, 40, 10);
				actm0010Data.idNo = subMS950String(bytes, 50, 20);
				actm0010Data.adMark = subMS950String(bytes, 70, 1);
				actm0010Data.sendDate = commd.twToAdDate(subMS950String(bytes, 71, 8));
				actm0010Data.sendUnti = subMS950String(bytes, 79, 7);
				actm0010Data.autopayCode = subMS950String(bytes, 86, 20);
				actm0010Data.respCode = subMS950String(bytes, 106, 2);
				return actm0010Data;
			}
			
			private Actm0010Data getPost0004Data(byte[] bytes) throws Exception {
				Actm0010Data actm0010Data = new Actm0010Data();
				String col0 = subMS950String(bytes, 0, 1);
				busDate = getBusDate();
				if ("2".equals(col0)) {
					ttlCnt = commString.strToInt(subMS950String(bytes, 20, 6));
					skipFlag = true;
					return actm0010Data;
				}
				actm0010Data.sendUnti = subMS950String(bytes, 1, 3);
				actm0010Data.achBankNo = "7000000";
				actm0010Data.sendDate = subMS950String(bytes, 8, 8);
				actm0010Data.adMark = subMS950String(bytes, 25, 1);
				actm0010Data.achAutopayAcctNo = commString.lpad(subMS950String(bytes, 27, 14), 16, "0");
				actm0010Data.autopayCode = subMS950String(bytes, 49, 2);
				actm0010Data.idNo = subMS950String(bytes, 51, 10);
				actm0010Data.achAutopayId = subMS950String(bytes, 61, 10);
				actm0010Data.rtnStatus = subMS950String(bytes, 71, 2);
				actm0010Data.checkFlag = subMS950String(bytes, 73, 1);
				return actm0010Data;
			}


		  private int selectCrdIdno(Actm0010Data actm0010Data) {
			  String sqlCmd = "select id_p_seqno from crd_idno where id_no = :id_no ";
			  setString("id_no",actm0010Data.idNo);
			  sqlSelect(sqlCmd);
			  if(sqlRowNum > 0)
				  actm0010Data.idPSeqno = sqlStr("id_p_seqno");
			  else			   
				  lsMsg = String.format(" select crd_idno not found ,ID不存在[%s]", actm0010Data.idNo);
			  return sqlRowNum;
		  }
		  
		  private int selectCrdCard(Actm0010Data actm0010Data) {
			  String sqlCmd = "select p_seqno from crd_card where id_p_seqno = :id_p_seqno and acct_type='01'and sup_flag='0' ";
			  sqlCmd += " fetch first 1 rows only ";
			  setString("id_p_seqno",actm0010Data.idPSeqno);
			  sqlSelect(sqlCmd);
			  if(sqlRowNum > 0)
				  actm0010Data.pSeqno = sqlStr("p_seqno");
			  else 
				  lsMsg = String.format(" select crd_card not found ,ID不存在[%s]", actm0010Data.idNo);;
			  return sqlRowNum;
		  }
		  
		  private String getBusDate() {
			  String sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
			  sqlSelect(sqlCmd);
			  return sqlStr("BUSINESS_DATE");
		  }
		  
			private void setProcessResult(String batchData, String batchErrorMsg) {
				wp.colSet(listCnt, "batch_ser_num", listCnt+1);
				wp.colSet(listCnt, "batch_data", batchData);
				wp.colSet(listCnt, "batch_error_msg", batchErrorMsg);
				listCnt++;
			}
			
			private int inserrtActChkno(Actm0010Data actm0010Data) {
				busi.SqlPrepare sp = new SqlPrepare();
				sp.sql2Insert("ACT_CHKNO");
				sp.ppstr("P_SEQNO",actm0010Data.pSeqno);
				sp.ppstr("ACCT_TYPE", "01");
				sp.ppstr("ID_P_SEQNO", actm0010Data.idPSeqno);
				sp.ppstr("AUTOPAY_ACCT_BANK", actm0010Data.achBankNo);
				sp.ppstr("AUTOPAY_ACCT_NO", actm0010Data.achAutopayAcctNo);
				sp.ppstr("AUTOPAY_INDICATOR", commString.decode(actm0010Data.autopayCode, ",10,20",",2,1"));
				sp.ppint("AUTOPAY_FIX_AMT", 0);
				sp.ppint("AUTOPAY_RATE", 0);
				sp.ppstr("ISSUE_DATE", actm0010Data.sendDate);
				sp.ppstr("AUTOPAY_ACCT_S_DATE", busDate);
				sp.ppstr("AUTOPAY_ACCT_E_DATE", "");
				sp.ppstr("VALID_FLAG", "1");
				sp.ppstr("REJECT_CODE", "0");
				sp.ppstr("FROM_MARK", "02");
				sp.ppstr("EDDA_REENTRY_FLAG", "N");
				sp.ppstr("VERIFY_FLAG", "Y");
				sp.ppstr("VERIFY_DATE", busDate);
				sp.ppstr("VERIFY_RETURN_CODE", "00");
				sp.ppstr("STMT_CYCLE", "01");
				sp.ppstr("AUTOPAY_ID", actm0010Data.achAutopayId);
				sp.ppstr("AUTOPAY_ID_CODE", "0");
				sp.ppstr("ACH_SEND_DATE", actm0010Data.sendDate);
				sp.ppstr("ACH_RTN_DATE", busDate);
				sp.ppstr("AD_MARK", "A");
				sp.ppstr("CURR_CODE", "901");
				sp.ppstr("CRT_USER", wp.loginUser);
				sp.ppstr("CRT_DATE", wp.sysDate);
				sp.ppstr("CRT_TIME", wp.sysDate + wp.sysTime);
				sp.addsql(", mod_time ", ", sysdate ");
				sp.ppstr("mod_user", wp.loginUser);
				sp.ppstr("mod_pgm", wp.modPgm());
				sqlExec(sp.sqlStmt(), sp.sqlParm());
				if (sqlRowNum <= 0) {
					lsMsg = "匯入失敗";
					return -1;
				}
				return 1;
			}
			
			private String subMS950String(byte[] bytes, int offset, int length) throws UnsupportedEncodingException {
				if (bytes.length < offset)
					return "";
				int len = bytes.length >= offset + length ? length : bytes.length - offset;
				byte[] vResult = new byte[length];
				System.arraycopy(bytes, offset, vResult, 0, len);
				return new String(vResult, "MS950");
			}
	
}

class Actm0010Data {
	String achBankNo = "";
	String achAutopayAcctNo = "";
	String achAutopayId = "";
	String idNo = "";
	String adMark = "";
	String sendDate = "";
	String sendUnti = "";
	String autopayCode = "";
	String respCode = "";
	String idPSeqno = "";
	String pSeqno = "";
	String rtnStatus = "";
	String checkFlag = ""; 

}
