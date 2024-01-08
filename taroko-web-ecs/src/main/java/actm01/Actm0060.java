/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 108-03-04  V1.00.01  AndyLiu    update ui                                  *
* 109-04-15  V1.00.02  Alex       add auth_query  
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*  
* 112/03/08  V1.00.04  yingdong   Erroneous String Compare Issue             *
* 112/05/02  V1.00.05  Simon      控制商務卡繳評調整，需輸入公司戶帳號       *
******************************************************************************/

package actm01;

import java.util.ArrayList;
import java.util.Arrays;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actm0060 extends BaseEdit {
	CommString commString = new CommString();
	String pPSeqno = "";
	String mAcctMonth = "";
	ArrayList<String> hAcctMonthList = new ArrayList<String>();
	ArrayList<String> acctMonthList = new ArrayList<String>();
	ArrayList<String> paymentRateList = new ArrayList<String>();
	ArrayList<String> apprYnList = new ArrayList<String>();

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
			saveFunc();
//			updateFunc();
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
	public void initPage() {
		wp.colSet("btnUpdate_disable", "disabled");
		wp.colSet("btnDelete_disable", "");
	}

	@Override
	public void queryFunc() throws Exception {
		if(empty(wp.itemStr2("ex_acct_key")) && empty(wp.itemStr2("ex_card_no"))) {
			alertErr2("帳號, 卡號不可均為空白");
			return;
		}

		ColFunc func =new ColFunc();
		func.setConn(wp);
		
		if(wp.itemEmpty("ex_acct_key")==false){
			String lsAcctKey = "";
			lsAcctKey = commString.acctKey(wp.itemStr2("ex_acct_key"));
			if(lsAcctKey.length()!=11){
				alertErr2("帳戶帳號:輸入錯誤");
				return ;
			}
			
			if (func.fAuthQuery(wp.modPgm(), commString.mid(lsAcctKey, 0,10))!=1) { 
	      	alertErr2(func.getMsg()); 
	      	return ; 
	      }
			
		}	else if(wp.itemEmpty("ex_card_no")==false)	{
			if (func.fAuthQuery(wp.modPgm(), wp.itemStr2("ex_card_no"))!=1) { 
	      	alertErr2(func.getMsg()); 
	      	return ; 
	      }
		}
		
		// 設定queryRead() SQL條件
		String lsPSeqno = getInitParm();
		
		if (lsPSeqno.equals("")) {
			alertErr2("無此帳號/卡號");
			return;
		}

		if (!lsPSeqno.equals("")) {
			getDtlData(lsPSeqno);
		}

		pPSeqno = lsPSeqno;

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		Object[] param = null;
		String lsSql = "";
		String qryTbl = "";
		
		lsSql += " select hex(rowid) as rowid, acct_data ";
		lsSql += " from ACT_MODDATA_TMP ";
		lsSql += " where p_seqno = ? and act_modtype='02'";
		param = new Object[] { pPSeqno };

		sqlSelect(lsSql, param);
		if (empty(sqlStr("acct_data"))) {
			qryTbl = "ACT_ACNO";
			wp.pageControl();

			// select columns
			wp.selectSQL =  " acct_type, acct_key, p_seqno, acct_status, stmt_cycle, id_p_seqno,                                     " + 
							" NVL( payment_rate1,' ') as PR1 ,NVL( payment_rate2,' ') as PR2 ,NVL( payment_rate3,' ') as PR3 ,       " + 
							" NVL( payment_rate4,' ') as PR4 ,NVL( payment_rate5,' ') as PR5 ,NVL( payment_rate6,' ') as PR6 ,       " + 
							" NVL( payment_rate7,' ') as PR7 ,NVL( payment_rate8,' ') as PR8 ,NVL( payment_rate9,' ') as PR9 ,       " + 
							" NVL( payment_rate10,' ') as PR10 ,NVL( payment_rate11,' ') as PR11 ,NVL( payment_rate12,' ') as PR12 , " + 
							" NVL( payment_rate13,' ') as PR13 ,NVL( payment_rate14,' ') as PR14 ,NVL( payment_rate15,' ') as PR15 , " + 
							" NVL( payment_rate16,' ') as PR16 ,NVL( payment_rate17,' ') as PR17 ,NVL( payment_rate18,' ') as PR18 , " + 
							" NVL( payment_rate19,' ') as PR19 ,NVL( payment_rate20,' ') as PR20 ,NVL( payment_rate21,' ') as PR21 , " + 
							" NVL( payment_rate22,' ') as PR22 ,NVL( payment_rate23,' ') as PR23 ,NVL( payment_rate24,' ') as PR24 , " + 
							" NVL( payment_rate25,' ') as PR25                                                                       " ;
			// table name
			wp.daoTable = "ACT_ACNO";
			// where sql
			wp.whereStr = " where acno_p_seqno = :p_seqno ";
			this.setString("p_seqno", pPSeqno);
			// order column
			wp.whereOrder = "";

			pageQuery();
			if (sqlNotFind()) {
				alertErr(appMsg.errCondNodata);
				return;
			}
		} else {
			qryTbl = "ACT_MODDATA_TMP";
			wp.pageControl();

			// select columns
			wp.selectSQL = " acct_data ";
			// table name
			wp.daoTable = "ACT_MODDATA_TMP";
			// where sql
			wp.whereStr = " where p_seqno = :p_seqno and act_modtype='02'";
			this.setString("p_seqno", pPSeqno);
			// order column
			wp.whereOrder = "";

			pageQuery();
		}
//		wp.setListCount(1);

//		wp.totalRows = wp.dataCnt;
//		wp.listCount[1] = wp.dataCnt;
//		wp.col_set("ft_cnt", Integer.toString(wp.dataCnt));
//		wp.setPageValue();
		if ("ACT_MODDATA_TMP".equals(qryTbl ))
			ProcModDataTmp();
		if ("ACT_ACNO".equals(qryTbl ))
			ProcActAcno();

		wp.setListCount(1);
		wp.setPageValue();
		wp.colSet("btnUpdate_disable", "");
		btnDeleteOn(true);
		wp.colSet("btnDelete_disable", "");
		
	}

	private String getInitParm() throws Exception {
		String lsSql = "";
		
		//from phopho
		lsSql  = " select acno_flag,acct_type, acct_key, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
		lsSql += " from act_acno ";
		lsSql += " where 1=1 ";
		lsSql += " and acno_p_seqno = p_seqno ";
		
		if (empty(wp.itemStr2("ex_acct_key")) == false) {
			lsSql += "and acct_type = :acct_type and acct_key = :acct_key ";
			setString("acct_type", wp.itemStr2("ex_acct_type"));
			String acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
			setString("acct_key",  acctkey);
		} else {
			lsSql += "and p_seqno in (select p_seqno from crd_card where card_no = :card_no) ";
			setString("card_no", wp.itemStr2("ex_card_no"));
		}
		
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("h_acct_type", sqlStr("acct_type"));
			wp.colSet("h_acct_key", sqlStr("acct_key"));
			wp.colSet("q_p_seqno", sqlStr("p_seqno"));
		//wp.col_set("q_id_cname", sql_ss("acno_cname"));
		//wp.col_set("q_corp_cname", sql_ss("acno_cname"));
			if ( sqlStr("acno_flag").equals("2"))  {
			   wp.colSet("q_corp_cname", sqlStr("acno_cname"));	
		     wp.colSet("q_id_cname", "");
			} else  {
		     wp.colSet("q_id_cname", sqlStr("acno_cname"));
			   wp.colSet("q_corp_cname", "");	
			}
			return sqlStr("p_seqno");
		}
		return "";
		
		/*
		Object[] param = null;
		String ls_sql = "";
//		Oris Mark for Test
//		ls_sql += " select acct_type, acct_key, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
		ls_sql += " select acct_type, acct_key, p_seqno, 'Oris測試' as acno_cname ";
		ls_sql += " from act_acno ";
		ls_sql += " where 1=1 ";

		if (empty(wp.item_ss("ex_acct_type")) == false && empty(wp.item_ss("ex_acct_key")) == false) {
			ls_sql += " and acct_type =?  and acct_key =?  ";
			param = new Object[] { wp.item_ss("ex_acct_type"), wp.item_ss("ex_acct_key") };
		}

//		if (empty(wp.item_ss("ex_acct_key")) == false) {
//			ls_sql += " and acct_type ='01'  and acct_key =?  ";
//			param = new Object[] { wp.item_ss("ex_acct_key") };
//		}

		if (empty(wp.item_ss("ex_card_no")) == false) {
			wp.whereStr += " and p_seqno in (select p_seqno from crd_card where card_no =:card_no) ";
			setString("card_no", wp.item_ss("ex_card_no"));
			param = new Object[] { wp.item_ss("ex_card_no") };
		}

		sqlSelect(ls_sql, param);
		if (empty(sql_ss("p_seqno"))) {
			return "";
		} else {
			//wp.col_set("q_acct_status", sql_ss("p_seqno"));
			wp.col_set("h_acct_type", sql_ss("acct_type"));
			wp.col_set("h_acct_key", sql_ss("acct_key"));
			wp.col_set("q_p_seqno", sql_ss("p_seqno"));
			return sql_ss("p_seqno");
		}
		*/

	}

	private void getDtlData(String pSeqno) throws Exception {
		String sYyymm = "";
		Object[] param = null;
		String lsSql = "";
		lsSql += " SELECT a.acct_status,   a.stmt_cycle, b.this_acct_month " + " FROM act_acno a, ptr_workday b "
				+ " WHERE b.stmt_cycle = a.stmt_cycle " + " and a.acno_p_seqno = ? " + "";
		param = new Object[] { pSeqno };
		sqlSelect(lsSql, param);
		mAcctMonth = "";

		if (empty(sqlStr("acct_status"))) {
			wp.colSet("q_acct_status", "");
			wp.colSet("q_stmt_cycle", "");
			wp.colSet("q_this_acct_month", "");
			wp.colSet("q_p_seqno", pSeqno);
		} else {
			String sStatus = "";
			if (Integer.parseInt(sqlStr("acct_status")) == 1)
				sStatus = "1-正常";
			if (Integer.parseInt(sqlStr("acct_status")) == 2)
				sStatus = "2-逾放";
			if (Integer.parseInt(sqlStr("acct_status")) == 3)
				sStatus = "3-催收";
			if (Integer.parseInt(sqlStr("acct_status")) == 4)
				sStatus = "4-呆帳";
			wp.colSet("q_acct_status", sStatus);
			wp.colSet("q_stmt_cycle", sqlStr("stmt_cycle"));
			sYyymm = sqlStr("this_acct_month");
      /***
			if(!s_yyymm.trim().equals("")) {
				s_yyymm = String.valueOf(Integer.parseInt(s_yyymm) - 191100);
				m_acct_month = s_yyymm;
				s_yyymm = s_yyymm.substring(0, 3) + "/" + s_yyymm.substring(3);
			}
      ***/
			mAcctMonth = sYyymm;
			if(!sYyymm.trim().equals("")) {
				sYyymm = sYyymm.substring(0, 4) + "/" + sYyymm.substring(4);
			}
			wp.colSet("q_this_acct_month", sYyymm);
			wp.colSet("q_p_seqno", pSeqno);
		}

	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		// ProcModDataTmp();
	}

	@Override
	public void saveFunc() throws Exception {

		Actm0060Func func = new Actm0060Func(wp);
		
		String[] aaHAcctMonth = wp.itemBuff("h_acct_month");
		String[] aaAcctMonth = wp.itemBuff("acct_month");  
		String[] aaPaymentRate = wp.itemBuff("payment_rate");  
		
		String colActModtype = "02";
		String colPSeqno = wp.itemStr2("q_p_seqno");
		String colAcctType = wp.itemStr2("h_acct_type");
		String colAcctKey = wp.itemStr2("h_acct_key");

		int rowcntaa = 0;
		if (!(aaAcctMonth==null) && !empty(aaAcctMonth[0])) rowcntaa = aaAcctMonth.length;
		wp.listCount[0] = rowcntaa;
		
		if(wfValidation()!=1)	return ;
		
		if(this.isUpdate()){
			String acctData = "";
			try {				
				//-delete detail-
				if (func.dbDelete() < 0) {
					alertErr(func.getMsg());
					sqlCommit(0);
					return;
				}	
			// check
			acctData = "";
			for (int ii = 0; ii < 25; ii++) {
				acctData += "@" + aaHAcctMonth[ii];
				acctData += "@" + aaPaymentRate[ii];
			}
			acctData = acctData.substring(1);

			func.varsSet("act_modtype", colActModtype);
			func.varsSet("p_seqno", colPSeqno);
			func.varsSet("acct_type", colAcctType);
			func.varsSet("acct_key", colAcctKey);
			func.varsSet("acct_data", acctData);
			func.dbInsert();
			if (rc != 1) {
	            alertErr2(func.getMsg());
	            sqlCommit(-1);
	            return ;
	       }
	        sqlCommit(rc);
	        
			}
			catch(Exception ex){}
		}	else	if(this.isDelete()){
			rc = func.dbDelete();
			sqlCommit(rc);
			if(rc!=1){
				errmsg(func.getMsg());
			}	else	wp.listCount[0] = 0;
		}
		
		
	}

	int wfValidation() throws Exception {
		String lsPSeqno = wp.itemStr2("q_p_seqno");
		Object[] param = null;
		String lsAcnoFlag = "";
		String lsSql = "";
		lsSql += " select acno_flag from act_acno where p_seqno = ? ";
		param = new Object[] { lsPSeqno };
		sqlSelect(lsSql, param);
		if (sqlRowNum > 0) {
			lsAcnoFlag = sqlStr("acno_flag");
    }
		if (lsAcnoFlag.equals("3")) {
		  errmsg("商務卡繳評調整，請輸入公司戶帳號！");			
		  return -1;
		} 

		int jj = 0;
 		String error1="", error2="";
    String[] aaValidPaymentRate = { "0A","0B","0C","0D","0E",
            "01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20",
            "21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40",
            "41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","60",
            "61","62","63","64","65","66","67","68","69","70","71","72","73","74","75","76","77","78","79","80",
            "81","82","83","84","85","86","87","88","89","90","91","92","93","94","95","96","97","98","99"       };
 		
		String[] aaPaymentRate = wp.itemBuff("payment_rate");  
		String[] aaChkPaymentRate = wp.itemBuff("chk_payment_rate");  
		

		for(int ii=0;ii<wp.itemRows("h_acct_month");ii++) { 
	
      /*** 以下 codes 是控制開放尾巴可輸入空值，先點掉，改成原本有值者不可更改為空值
		  if(aa_payment_rate[ii].trim().equals("")) {

			  if (ii == wp.item_rows("h_acct_month") - 1 )  {
         //break;
				   continue ;
        }

		    jj = ii + 1;
		    for(int kk=jj;kk<wp.item_rows("h_acct_month");kk++) {
		      if(!aa_payment_rate[kk].trim().equals("")) { // ii欄 空值，但後續 kk欄有值
		        wp.col_set(ii,"err_flag", "X");
			    	error_1 = "Y";
            break;
          }
		    }
	  		continue ;
		  }
      ***/

      //ddd("ii= " + ii + ", aa_payment_rate[ii]= " + aa_payment_rate[ii]);
      //ddd("ii= " + ii + ", aa_chk_payment_rate[ii]= " + aa_chk_payment_rate[ii]);

		  if(aaPaymentRate[ii].trim().equals("")) {

			  if (!aaChkPaymentRate[ii].trim().equals("") )  {
		        wp.colSet(ii,"err_flag", "X");
			      error1 = "Y";
        }
				continue ;
		  }

		  if(!aaPaymentRate[ii].trim().equals("")) {

			  if (aaChkPaymentRate[ii].trim().equals("") )  {
		        wp.colSet(ii,"err_flag", "X");
			      error1 = "Y";
				   continue ;
        }
		  }

      if (Arrays.asList(aaValidPaymentRate).indexOf(aaPaymentRate[ii]) < 0) {
		    wp.colSet(ii,"err_flag", "X");
				error2 = "Y";
				continue ;
			}
	  }
			
		if(eqIgno(error1,"Y") || eqIgno(error2,"Y")) {
		  errmsg("輸入錯誤！");			
		  return -1;
		}
		
		return 1;
	}
	
	@Override
	public void initButton() {
	//wp.col_set("btnDelete_disable", ""); 給予 "",表示此按鍵 on
		String sKey = "1st-page";

    if (wp.respHtml.equals("actm0060"))  {
        wp.colSet("btnUpdate_disable","");
        wp.colSet("btnDelete_disable","");
        this.btnModeAud(sKey);
    }

	}

	@Override
	public void dddwSelect() {
		try {

//			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

		} catch (Exception ex) {
		}
	}

	//???
//	public void forinitinfo(TarokoCommon wr) throws Exception {
//
//		super.wp = wr;
//		String accttyp = "", acctkey = "", cardno = "";
//		accttyp = wp.item_ss("accttyp");
//		acctkey = wp.item_ss("acctkey");
//		cardno = wp.item_ss("cardno");
//
//		if (empty(acctkey) && empty(cardno)) {
//			return;
//		}
//
//		if (empty(cardno)) {
//			if (!(empty(accttyp) && empty(acctkey))) {
//				return;
//			}
//		}
//
//		Object[] param = null;
//		String ls_sql = "";
//		ls_sql += " select acct_type, acct_key, p_seqno, "
//				+ " uf_acno_name(p_seqno) as acno_cname "
//				+ " from act_acno "
//				+ " where 1=1 ";
//		if (!empty(accttyp)) {
//			ls_sql += "and acct_type =?  and acct_key =? ";
//			param = new Object[] { accttyp, acctkey };
//		} else {
//			ls_sql += "and p_seqno in (select p_seqno from crd_card where card_no =? ) ";
//			param = new Object[] { cardno };
//		}
//		sqlSelect(ls_sql, param);
//
//		if (empty(sql_ss("acct_status"))) {
//			return;
//		} else {
//			wp.addJSON("q_id_cname", sql_ss("acno_cname"));
//			wp.addJSON("q_corp_cname", sql_ss("acno_cname"));
//			
//			wp.col_set("q_id_cname", sql_ss("acno_cname"));
//			wp.col_set("q_corp_cname", sql_ss("acno_cname"));
//		}
//
//		return;
//	}

	void ProcModDataTmp() throws Exception {
		int rowCt = wp.selectCnt;
		if(rowCt <= 0) return;
		
		String strTmpData = "";
		//ArrayList<String> h_acct_month_List = new ArrayList<String>();
		//ArrayList<String> acct_month_List = new ArrayList<String>();
		//ArrayList<String> payment_rate_List = new ArrayList<String>();
		//ArrayList<String> appr_yn_List = new ArrayList<String>();
		String syyymm = "";
		String syy = "", smm="";

		strTmpData = wp.colStr("acct_data") + "@END@";

		String[] tmpCols = strTmpData.equals("") || strTmpData == null ? null : strTmpData.split("@");
		// 將頁面資料用ArrayList先存起來
		
		
		
		for (int ii = 0; ii < 25; ii++) {			
      /***
			syyymm = String.valueOf(Integer.parseInt(tmpCols[ii * 2]) - 191100);
			syyymm = zzStr.right("0"+syyymm,5);
			syy = syyymm.substring(0,3);
			smm = syyymm.substring(3);
      ***/
			syyymm = tmpCols[ii * 2];
			syy = syyymm.substring(0,4);
			smm = syyymm.substring(4);
			
			hAcctMonthList.add(tmpCols[ii * 2]);
			acctMonthList.add(syy + "/" + smm);
			paymentRateList.add(tmpCols[(ii * 2) + 1]);
			apprYnList.add("Y");
		}
		//**異動欄位提示  Andy 20190304
		String lsSql = "select "
		    + "a.payment_rate1,a.payment_rate2,a.payment_rate3,a.payment_rate4,a.payment_rate5,"
				+ "a.payment_rate6,a.payment_rate7,a.payment_rate8,a.payment_rate9,a.payment_rate10,"
				+ "a.payment_rate11,a.payment_rate12,a.payment_rate13,a.payment_rate14,a.payment_rate15,"
				+ "a.payment_rate16,a.payment_rate17,a.payment_rate18,a.payment_rate19,a.payment_rate20,"
				+ "a.payment_rate21,a.payment_rate22,a.payment_rate23,a.payment_rate24,a.payment_rate25, "
				+ "uf_date_add(C.this_acct_month,0,-1,0) as mh1 ," 
				+ "uf_date_add(C.this_acct_month,0,-2,0) as mh2 ,"
				+ "uf_date_add(C.this_acct_month,0,-3,0) as mh3 ,"
				+ "uf_date_add(C.this_acct_month,0,-4,0) as mh4 ,"
				+ "uf_date_add(C.this_acct_month,0,-5,0) as mh5 ,"
				+ "uf_date_add(C.this_acct_month,0,-6,0) as mh6 ,"
				+ "uf_date_add(C.this_acct_month,0,-7,0) as mh7 ,"
				+ "uf_date_add(C.this_acct_month,0,-8,0) as mh8 ,"
				+ "uf_date_add(C.this_acct_month,0,-9,0) as mh9 ,"
				+ "uf_date_add(C.this_acct_month,0,-10,0) as mh10 ,"
				+ "uf_date_add(C.this_acct_month,0,-11,0) as mh11 ,"
				+ "uf_date_add(C.this_acct_month,0,-12,0) as mh12 ,"
				+ "uf_date_add(C.this_acct_month,0,-13,0) as mh13 ,"
				+ "uf_date_add(C.this_acct_month,0,-14,0) as mh14 ,"
				+ "uf_date_add(C.this_acct_month,0,-15,0) as mh15 ,"
				+ "uf_date_add(C.this_acct_month,0,-16,0) as mh16 ,"
				+ "uf_date_add(C.this_acct_month,0,-17,0) as mh17 ,"
				+ "uf_date_add(C.this_acct_month,0,-18,0) as mh18 ,"
				+ "uf_date_add(C.this_acct_month,0,-19,0) as mh19 ,"
				+ "uf_date_add(C.this_acct_month,0,-20,0) as mh20 ,"
				+ "uf_date_add(C.this_acct_month,0,-21,0) as mh21 ,"
				+ "uf_date_add(C.this_acct_month,0,-22,0) as mh22 ,"
				+ "uf_date_add(C.this_acct_month,0,-23,0) as mh23 ,"
				+ "uf_date_add(C.this_acct_month,0,-24,0) as mh24 ,"
				+ "uf_date_add(C.this_acct_month,0,-25,0) as mh25  "
				+ "from act_acno a, ptr_workday c "
				+ "where a.acno_p_seqno =:p_seqno and c.stmt_cycle = a.stmt_cycle ";
		setString("p_seqno",pPSeqno);
		sqlSelect(lsSql);
		//**
		for (int ii = 0; ii < 25; ii++) {
			int jj = ii+1;
			wp.colSet(ii, "h_acct_month", sqlStr("mh" + jj));
			syyymm = sqlStr("mh" + jj);
			syy = syyymm.substring(0,4);
			smm = syyymm.substring(4);
			wp.colSet(ii, "acct_month", syy + "/" + smm);
			wp.colSet(ii, "payment_rate", sqlStr("payment_rate" + jj));
			wp.colSet(ii, "chk_payment_rate", sqlStr("payment_rate" + jj));
			
			String wkHAcctMonth = wp.colStr(ii,"h_acct_month");
			String wkPaymentRate = matchPaymentRate(wkHAcctMonth);

			if(wkPaymentRate.equals(wp.colStr(ii, "payment_rate")) || empty(wkPaymentRate)) 
			{
				wp.colSet(ii, "appr_yn", "N");
			}else{
				wp.colSet(ii, "payment_rate", wkPaymentRate);
				wp.colSet(ii, "appr_yn", "Y");
				wp.colSet(ii,"color","style='color: red'");
			}
			
			int j = ii+1;
			if (j < 10) {
				wp.serNum = "0" + j;
				wp.setValue("SER_NUM", "0"+j, ii);
			} else {
				wp.serNum = ("" + j);
				wp.setValue("SER_NUM", ""+j, ii);
			}
		}
		wp.colSet("temp_yn", "Y");
		wp.selectCnt =25;

	}

  String matchPaymentRate(String txHAcctMonth) throws Exception  {
		String lsPaymentRate = "";
		for (int mm = 0; mm < 25; mm++) {
			if (hAcctMonthList.get(mm).equals(txHAcctMonth)) {
				 lsPaymentRate = paymentRateList.get(mm);
		     break;
			}
		}
		return lsPaymentRate;
	}

	void ProcActAcno() throws Exception {
		int rowCt = wp.selectCnt;

		if(rowCt <= 0) return;
		
		//ArrayList<String> h_acct_month_List = new ArrayList<String>();
		//ArrayList<String> acct_month_List = new ArrayList<String>();
		//ArrayList<String> payment_rate_List = new ArrayList<String>();
		//ArrayList<String> appr_yn_List = new ArrayList<String>();
		String syy = "", smm="";
		
		if(!mAcctMonth.trim().equals("")) {
		//syy = m_acct_month.substring(0,3);
		//smm = m_acct_month.substring(3);
			syy = mAcctMonth.substring(0,4);
			smm = mAcctMonth.substring(4);
		}

		// 將頁面資料用ArrayList先存起來
		for (int ii = 1; ii <= 25; ii++) {
			if(Integer.parseInt(smm) - 1 <=0) {
				smm = "12";
				syy = commString.numFormat(Double.parseDouble(syy) - 1, "0000");
			}else {
				smm = commString.numFormat(Double.parseDouble(smm) - 1, "00");
			}
			
		//h_acct_month_List.add(String.valueOf(Integer.parseInt(syy) + 1911) + smm);
			hAcctMonthList.add(syy + smm);
			acctMonthList.add(syy + "/" + smm);
			paymentRateList.add(wp.colStr(0, "PR" + String.valueOf(ii)));
			apprYnList.add("N");

		}

		for (int ii = 0; ii < 25; ii++) {
			wp.colSet(ii, "h_acct_month", hAcctMonthList.get(ii));
			wp.colSet(ii, "acct_month", acctMonthList.get(ii));
			wp.colSet(ii, "payment_rate", paymentRateList.get(ii));
			wp.colSet(ii, "chk_payment_rate", paymentRateList.get(ii));
			wp.colSet(ii, "appr_yn", apprYnList.get(ii));
			
			int j = ii+1;
			if (j < 10) {
				wp.serNum = "0" + j;
				wp.setValue("SER_NUM", "0"+j, ii);
			} else {
				wp.serNum = ("" + j);
				wp.setValue("SER_NUM", ""+j, ii);
			}
		}
		wp.colSet("temp_yn", "N");
		wp.selectCnt =25;

	}

	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
	
	public void deleteFunc() throws Exception{
		Actm0060Func func = new Actm0060Func(wp);
		String wkPSeqno = wp.itemStr2("q_p_seqno");

		if(empty(wkPSeqno)){
			alertErr("請先查詢資料!!");
			return;
		}
		String[] aaAcctMonth = wp.itemBuff("acct_month");
		String[] aaPaymentRate = wp.itemBuff("payment_rate");
		String[] aaApprYn = wp.itemBuff("appr_yn");
		int chk = 0;
		wp.listCount[0] = aaAcctMonth.length;
		for (int rr = 0; rr < aaAcctMonth.length; rr++) {
			if(aaApprYn[rr].equals("Y")){
				chk++;
			}			
		}
		//if(chk == 0){
		//	alert_err("帳號資料無待確認資料,無資料刪除!!");
		//	return;
		//}		
		Object[] param = null;
		String lsSql = "";
		
		lsSql += " select hex(rowid) as rowid, acct_data ";
		lsSql += " from ACT_MODDATA_TMP ";
		lsSql += " where p_seqno = ? and act_modtype='02'";
		param = new Object[] { wkPSeqno };

		sqlSelect(lsSql, param);
		if (empty(sqlStr("acct_data"))) {
			  alertErr("帳號資料無待確認資料,無資料刪除!!");
			  return;
		}

		if (func.dbDelete() < 0) {
			alertErr(func.getMsg());
			sqlCommit(0);
			return;
		}
		
	}
}
