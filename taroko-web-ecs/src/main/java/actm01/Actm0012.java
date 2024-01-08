/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 107-08-16  V1.00.01  Alex       bug fixed                                  *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-12  V1.00.04  Simon      1.Add column CURR_CHANGE_ACCOUT maintenance*
*                                 2.update autopay_acct_no data into act_acct_curr*
******************************************************************************/

package actm01;

import busi.func.SmsMsgDetl;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon; 

public class Actm0012 extends BaseEdit {
	  CommString commString = new CommString();
	  taroko.base.CommDate commDate = new taroko.base.CommDate();
    String mAccttype="";	
    String mAcctkey="";	
    String mCurrcode = "";
	  String hWhereQAcctCurr ="" , hWhereQChkno ="";
    
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc=1;

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
            //-資料讀取- 
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
            /* 瀏覽功能 :skip-page*/
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
		    } else if (eqIgno(wp.buttonCode, "C1")) {
			      /* 發送簡訊 */
			      sendSmsMesg();
        }

		dddwSelect();
		initButton();
    }

    @Override
    public void queryFunc() throws Exception {
    	
	  	if( empty(wp.itemStr2("ex_acct_key")) && empty(wp.itemStr2("ex_autopay_id")) ) {
		  	alertErr2("帳戶帳號, 帳戶扣繳歸屬ID不可均為空白");
			  return;
		  }
    	
      /*
    	if(!empty(wp.item_ss("ex_acct_key")) && (wp.item_ss("ex_acct_key").length() < 6)) {
    		err_alert("帳戶帳號輸入至少要6碼");
			return;
    	}

    	if(!empty(wp.item_ss("ex_autopay_id")) && (wp.item_ss("ex_autopay_id").length() < 6)) {
    		err_alert("扣繳帳戶ID至少要6碼");
			return;
    	}
      */

    	//設定queryRead() SQL條件
        /***
        wp.whereStr =" where 1=1 "
        		+ "and act_acct_curr.p_seqno = act_acno.acno_p_seqno "
        		+ "and act_acno.id_p_seqno = crd_idno.id_p_seqno "
        		+ "and uf_nvl(act_acct_curr.curr_code,'901')<>'901' ";
        	//+ "and from_mark in ('02','03') "
        	//+ "and ad_mark <> 'D' ";
        
        if(empty(wp.item_ss("ex_acct_key"))==false){
            wp.whereStr  += " and act_acno.acct_key = :ex_acct_key ";
            String acctkey = fillZeroAcctKey(wp.item_ss("ex_acct_key"));
    	      setString("ex_acct_key", acctkey);

        }
        
        if(empty(wp.item_ss("ex_autopay_id"))==false){
            wp.whereStr  += " and act_acct_curr.autopay_id = :ex_autopay_id ";
        	  setString("ex_autopay_id", wp.item_ss("ex_autopay_id"));
        }
        ***/

        String acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
		    hWhereQChkno = " where 1=1 "
				    			 + " and A.ad_mark <>'D' "
				    			 + " and A.proc_mark <>'Y' "
            	     + " and A.from_mark in ('02','03') "
						    	 + " and A.p_seqno = B.acno_p_seqno "
							     + " and uf_nvl(A.curr_code,'901') <> '901' "
						       +sqlCol(acctkey,"B.acct_key")
							     +sqlCol(wp.itemStr2("ex_autopay_id"),"A.autopay_id")
							     ;
		
		    hWhereQAcctCurr = " where 1=1 "
				    			 + " and A.p_seqno = B.acno_p_seqno "
							     + " and uf_nvl(A.curr_code,'901') <> '901' "
						       +sqlCol(acctkey,"B.acct_key")
							     +sqlCol(wp.itemStr2("ex_autopay_id"),"A.autopay_id")
							     ;

        
        //-page control-
      //wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();
        
        //select columns
        /***
        wp.selectSQL = " act_acct_curr.acct_type , " + 
        		 " act_acct_curr.p_seqno, " + 
        		 " act_acno.acct_key, " + 
        		 " crd_idno.id_no, " + 
        		 " crd_idno.id_no_code, " + 
        		 " act_acct_curr.autopay_acct_bank , " + 
        		 " act_acct_curr.autopay_acct_no , " + 
        		 " act_acct_curr.autopay_id , " + 
        		 " act_acct_curr.autopay_id_code , " + 
        		 " '1' as valid_flag , " + 
        		 " act_acno.id_p_seqno , " + 
        		 " act_acct_curr.autopay_indicator , " + 
        		 " act_acct_curr.curr_code , " + 
        		 " (act_acct_curr.curr_code || '-' || ptr_currcode.curr_chi_name) as tt_curr_code, " + 
        		 " act_acct_curr.autopay_dc_flag , " + 
        		 " uf_acno_name(act_acct_curr.p_seqno) db_chi_name, " + 
        		 " act_acct_curr.autopay_dc_indicator " ;
        
        //table name
        wp.daoTable = " act_acno, crd_idno, act_acct_curr left join ptr_currcode on act_acct_curr.curr_code = ptr_currcode.curr_code ";
        //order column
        wp.whereOrder=" ORDER BY CRD_IDNO.id_no  ASC , crd_idno.id_no_code ASC ";
        ***/

		String lsSubSqlCmd = ""
 	  				 + " select "
						 + " 'act_chkno' as tt_which_table ,"
					 //+ " decode(b.acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(b.acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						 + " c.id_no || '-' || c.id_no_code as tt_id_no ," //系統無外幣商務卡
						 + " A.p_seqno , "
						 + " A.curr_code  "
			  		 + " from act_chkno A , act_acno B "
	      		 + " left join crd_idno c on b.id_p_seqno = c.id_p_seqno "
					 //+ " left join crd_corp d on b.corp_p_seqno = d.corp_p_seqno " //系統無外幣商務卡
						 + hWhereQChkno
						 + " union "
 	  				 + " select "
						 + " 'act_acct_curr' as tt_which_table ,"
					 //+ " decode(acno_flag,'2',d.corp_no,c.id_no) || '-' || decode(acno_flag,'2','0',c.id_no_code) as tt_id_no  ,"
						 + " c.id_no || '-' || c.id_no_code as tt_id_no ," //系統無外幣商務卡
						 + " a.p_seqno , "
						 + " A.curr_code  "
			  		 + " from act_acct_curr A, act_acno B "
	      		 + " left join crd_idno c on b.id_p_seqno = c.id_p_seqno "
					 //+ " left join crd_corp d on a.corp_p_seqno = d.corp_p_seqno " //系統無外幣商務卡
						 + hWhereQAcctCurr
				   //+ " order by tt_which_table , tt_id_no  "
					   ;

	  wp.sqlCmd = ""
 	  				 + " select "
						 + " count(*) ,"
						 + " max(tt_which_table) as tt_which_table ,"
						 + " p_seqno , "
					   + " curr_code "
			  		 + " from ( "
			  		 + lsSubSqlCmd
			  		 + " ) "
				  	 + " group by p_seqno, curr_code "
			  		 + " having count(*) >= 1 "
				     + " order by p_seqno, curr_code "
				   //+ " order by tt_id_no, p_seqno, curr_code "
					   ;

		wp.pageCountSql =""
				+"select count(*) from ( "
				+ wp.sqlCmd 
				+" )"
				;
		
        pageQuery();

        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        listWkdata();
        wp.setListCount(1);
        wp.totalRows = wp.dataCnt;
        wp.listCount[1] = wp.dataCnt;
        wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
        wp.setPageValue();

    }
    
    void listWkdata() throws Exception {
		String ss = "",ss2 = "";

		for (int ii = 0; ii < wp.selectCnt; ii++) {

      ss  = wp.colStr(ii,"p_seqno");
      ss2 = wp.colStr(ii,"curr_code");
    //前面執行 select union & group by having,order by 等等...還不能確保一定抓 act_chkno為優先，所以須再執行以下片段 
    //check if act_chkno data exists ? if it exists, replace act_chkno into act_acct_curr.  
      if (wkChkActChkno(ii,ss,ss2) == 0)  {
          wp.colSet(ii,"tt_which_table", "act_chkno");
      }
      else  {
        wkChkActAcctCurr(ii,ss,ss2);
        wp.colSet(ii,"tt_which_table", "act_acct_curr");
      }  	  

			ss =wp.colStr(ii,"acct_type");
			ss2=wp.colStr(ii,"acct_key");
			wp.colSet(ii,"wk_acct_key", ss+" - "+ss2);
			
			ss =wp.colStr(ii,"id_no");
			ss2=wp.colStr(ii,"id_no_code");
			wp.colSet(ii,"wk_idcode", ss+"-"+ss2);

			ss =wp.colStr(ii,"autopay_id");
			ss2=wp.colStr(ii,"autopay_id_code");
			wp.colSet(ii,"wk_apayid", ss+"-"+ss2);
			
			ss =wp.colStr(ii,"valid_flag");
	    wp.colSet(ii,"tt_valid_flag", commString.decode(ss, ",1,2", ",即時生效,CYCLE 生效"));
	    	
	    ss =wp.colStr(ii,"autopay_indicator");
	    wp.colSet(ii,"tt_autopay_indicator", commString.decode(ss, ",1,2,3", ",扣TTL,扣MP,其他"));
	    	
	    ss =wp.colStr(ii,"autopay_dc_indicator");
	    wp.colSet(ii,"tt_autopay_dc_indicator", commString.decode(ss, ",1,2,3", ",扣TTL,扣MP,其他"));

			if(!wp.colEq(ii,"autopay_dc_flag", "Y")){
				wp.colSet(ii,"tt_autopay_dc_indicator", "");
			}	

		}

	}

    int wkChkActChkno(int txIi, String txPSeqno,String txCurrCode) throws Exception  {
   	 
   	  String sql0 = " select "
   	 	 			  + " a.acct_type , "
   	 	 			  + " b.acct_key , "
   	 	 			  + " c.id_no , "
   	 	 			  + " c.id_no_code , "
   	 	 			  + " c.chi_name as db_chi_name , "
         		  + " (a.curr_code || '-' || p.curr_chi_name) as tt_curr_code, "
  	 	 			  + " a.autopay_acct_bank , "
   	 	 			  + " a.autopay_acct_no , "
   	 	 			  + " a.autopay_id , "
   	 	 			  + " a.autopay_id_code , "
   	 	 			  + " a.valid_flag , "
   	 	 			  + " a.autopay_indicator , "
   	 		 		  + " a.autopay_acct_s_date , "
   	 		 		  + " a.autopay_acct_e_date , "
   	 		 		  + " a.autopay_dc_flag , "
   	 			 	  + " a.autopay_dc_indicator "
			  	    + " from act_chkno A join act_acno B on a.p_seqno = b.acno_p_seqno"
	      		  + " left join crd_idno c on b.id_p_seqno = c.id_p_seqno "
	      		  + " left join ptr_currcode p on p.curr_code = a.curr_code "
   	 				  + " where a.p_seqno = ? "
   	 				  + " and a.curr_code = ? "
        	    + " and A.from_mark in ('02','03') "
        	  	+ " and A.proc_mark <> 'Y' "
        	  	+ " and A.ad_mark <> 'D' "
   			 		  ;
   	 
   	  sqlSelect(sql0,new Object[]{txPSeqno,txCurrCode});
   	 
   	  if(sqlRowNum<=0) {
         return -1;
   	  }
   	 
   	  wp.colSet(txIi,"acct_type", sqlStr("acct_type"));
   	  wp.colSet(txIi,"acct_key", sqlStr("acct_key"));
   	  wp.colSet(txIi,"id_no", sqlStr("id_no"));
   	  wp.colSet(txIi,"id_no_code", sqlStr("id_no_code"));
   	  wp.colSet(txIi,"db_chi_name", sqlStr("db_chi_name"));
   	  wp.colSet(txIi,"tt_curr_code", sqlStr("tt_curr_code"));
   	  wp.colSet(txIi,"autopay_acct_bank", sqlStr("autopay_acct_bank"));
   	  wp.colSet(txIi,"autopay_acct_no", sqlStr("autopay_acct_no"));
   	  wp.colSet(txIi,"autopay_id", sqlStr("autopay_id"));
   	  wp.colSet(txIi,"autopay_id_code", sqlStr("autopay_id_code"));
   	  wp.colSet(txIi,"valid_flag", sqlStr("valid_flag"));
   	  wp.colSet(txIi,"autopay_indicator", sqlStr("autopay_indicator"));
   	  wp.colSet(txIi,"autopay_acct_s_date", sqlStr("autopay_acct_s_date"));
   	  wp.colSet(txIi,"autopay_acct_e_date", sqlStr("autopay_acct_e_date"));
   	  wp.colSet(txIi,"autopay_dc_flag", sqlStr("autopay_dc_flag"));
   	  wp.colSet(txIi,"autopay_dc_indicator", sqlStr("autopay_dc_indicator"));
   	 
   	  return 0;
    }
    
    int wkChkActAcctCurr(int txIi, String txPSeqno,String txCurrCode) throws Exception  {
   	 
   	  String sql1 = " select "
   	 	 			  + " a.acct_type , "
   	 	 			  + " b.acct_key , "
   	 	 			  + " c.id_no , "
   	 	 			  + " c.id_no_code , "
   	 	 			  + " c.chi_name as db_chi_name , "
         		  + " (a.curr_code || '-' || p.curr_chi_name) as tt_curr_code, " 
  	 	 			  + " a.autopay_acct_bank , "
   	 	 			  + " a.autopay_acct_no , "
   	 	 			  + " a.autopay_id , "
   	 	 			  + " a.autopay_id_code , "
   	 	 			  + " '1' as valid_flag , "
   	 	 			  + " a.autopay_indicator , "
   	 		 		  + " '' as autopay_acct_s_date , "
   	 		 		  + " '' as autopay_acct_e_date , "
   	 		 		  + " a.autopay_dc_flag , "
   	 			 	  + " a.autopay_dc_indicator "
			  	    + " from act_acct_curr A join act_acno B on a.p_seqno = b.acno_p_seqno "
	      		  + " left join crd_idno c on b.id_p_seqno = c.id_p_seqno "
	      		  + " left join ptr_currcode p on p.curr_code = a.curr_code "
   	 				  + " where a.p_seqno = ? "
   	 				  + " and a.curr_code = ? "
   			 		  ;
   	 
   	  sqlSelect(sql1,new Object[]{txPSeqno,txCurrCode});
   	  if(sqlRowNum<=0) {
         return -1;
   	  }
   	 
   	  wp.colSet(txIi,"acct_type", sqlStr("acct_type"));
   	  wp.colSet(txIi,"acct_key", sqlStr("acct_key"));
   	  wp.colSet(txIi,"id_no", sqlStr("id_no"));
   	  wp.colSet(txIi,"id_no_code", sqlStr("id_no_code"));
   	  wp.colSet(txIi,"db_chi_name", sqlStr("db_chi_name"));
   	  wp.colSet(txIi,"tt_curr_code", sqlStr("tt_curr_code"));
   	  wp.colSet(txIi,"autopay_acct_bank", sqlStr("autopay_acct_bank"));
   	  wp.colSet(txIi,"autopay_acct_no", sqlStr("autopay_acct_no"));
   	  wp.colSet(txIi,"autopay_id", sqlStr("autopay_id"));
   	  wp.colSet(txIi,"autopay_id_code", sqlStr("autopay_id_code"));
   	  wp.colSet(txIi,"valid_flag", sqlStr("valid_flag"));
   	  wp.colSet(txIi,"autopay_indicator", sqlStr("autopay_indicator"));
   	  wp.colSet(txIi,"autopay_acct_s_date", sqlStr("autopay_acct_s_date"));
   	  wp.colSet(txIi,"autopay_acct_e_date", sqlStr("autopay_acct_e_date"));
   	  wp.colSet(txIi,"autopay_dc_flag", sqlStr("autopay_dc_flag"));
   	  wp.colSet(txIi,"autopay_dc_indicator", sqlStr("autopay_dc_indicator"));
   	 
   	  return 0;
    }
    
    @Override
    public void querySelect() throws Exception {
    	mAccttype = wp.itemStr2("data_k1");
    	mAcctkey  = wp.itemStr2("data_k2");
    	mCurrcode = wp.itemStr2("data_k3");
   	  wp.colSet("kk_acct_type", mAccttype);
   	  wp.colSet("kk_acct_key", mAcctkey);
   	  wp.colSet("kk_curr_code", mCurrcode);
      dataRead();
    }

    @Override
    public void dataRead() throws Exception {

      /***
      wp.ddd("-->Actm0012-dsp01","");
      wp.ddd("--:item_kk('acct_type')[%s]",item_kk("acct_type"));
      wp.ddd("--:item_kk('acct_key')[%s]",item_kk("acct_key"));
      wp.ddd("--:item_kk('curr_code')[%s]",item_kk("curr_code"));
      wp.ddd("--:wp.item_ss('kk_acct_type')[%s]",wp.item_ss("kk_acct_type"));
      wp.ddd("--:wp.item_ss('kk_acct_key')[%s]",wp.item_ss("kk_acct_key"));
      wp.ddd("--:wp.item_ss('kk_curr_code')[%s]",wp.item_ss("kk_curr_code"));
      ***/

    	if (empty(mAcctkey)){
    	//m_accttype = item_kk("acct_type");
    	//m_acctkey  = item_kk("acct_key");
    	//m_currcode = item_kk("curr_code");
    		if(empty(wp.itemStr2("kk_acct_type"))) {
	    		wp.itemSet("kk_acct_type", wp.itemStr2("kp_acct_type"));
	  	  	wp.colSet("kk_acct_type", wp.itemStr2("kp_acct_type"));
		    } 				
  		  if(empty(wp.itemStr2("kk_acct_key"))) {
	  		  wp.itemSet("kk_acct_key", wp.itemStr2("kp_acct_key"));
	  		  wp.colSet("kk_acct_key", wp.itemStr2("kp_acct_key"));
		    } 				
  		  if(empty(wp.itemStr2("kk_curr_code"))) {
	  		  wp.itemSet("kk_curr_code", wp.itemStr2("kp_curr_code"));
	  		  wp.colSet("kk_curr_code", wp.itemStr2("kp_curr_code"));
		    } 				
    		mAccttype = wp.itemStr2("kk_acct_type");
    		mAcctkey  = wp.itemStr2("kk_acct_key");
    		mCurrcode = wp.itemStr2("kk_curr_code");
		  }
		  if (isEmpty(mAccttype) || isEmpty(mAcctkey)){
			  alertErr("帳戶帳號 : 不可空白");
			  return;
		  }
		  if (isEmpty(mCurrcode)){
			  alertErr("雙幣幣別 : 不可空白");
			  return;
		  }

		mAcctkey = commString.acctKey(mAcctkey);
		if(mAcctkey.length()!=11){
			alertErr2("帳戶帳號輸入錯誤");
			return ;
		}
		
		
		wp.selectSQL = "hex(act_chkno.rowid) as rowid, act_chkno.mod_seqno, " +
			    	" act_chkno.acct_type , " + 
        		" act_chkno.p_seqno, " + 
        		" act_acno.acct_key, " + 
        		" crd_idno.id_no, " + 
        		" crd_idno.id_no_code, " + 
        		" act_chkno.appl_no , " + 
        		" act_chkno.autopay_acct_bank , " + 
        		" act_chkno.autopay_acct_no , " + 
        		" act_chkno.curr_change_accout as exchange_acct_no, " + 
        		" act_chkno.card_no , " + 
        		" act_chkno.autopay_id , " + 
        		" act_chkno.autopay_id_code , " + 
        		" act_chkno.valid_flag , " + 
						" act_chkno.from_mark ," +
						" act_chkno.verify_flag ," +
						" act_chkno.verify_date ," +
						" act_chkno.verify_return_code ," +
						" act_chkno.ibm_check_date ," +
						" act_chkno.ibm_return_code ," +
						" act_chkno.sms_send_date ," +
						" act_chkno.sms_send_cnt ," +
        		" act_chkno.id_p_seqno , " + 
        		" act_chkno.autopay_indicator , " + 
        		" act_acno.autopay_indicator as acno_autopay_indicator , " + 
        		" act_chkno.autopay_fix_amt , " + 
        		" act_chkno.autopay_rate , " + 
        		" act_chkno.crt_date , " + 
        		" act_chkno.autopay_acct_s_date , " + 
        		" act_chkno.autopay_acct_e_date , " + 
        		" act_chkno.curr_code , " + 
        		" (act_chkno.curr_code || '-' || ptr_currcode.curr_chi_name) as tt_curr_code, " + 
        		" act_chkno.autopay_dc_flag , " + 
        		" uf_acno_name(act_chkno.p_seqno) db_holder_name, " + 
        		" act_chkno.autopay_dc_indicator, " +
        		" act_acno.stmt_cycle, " +
        		" act_chkno.exec_check_date , " +
        		" act_chkno.exec_check_flag "
        		;

        wp.daoTable = " act_acno, crd_idno, act_chkno left join ptr_currcode on act_chkno.curr_code = ptr_currcode.curr_code ";
        
        wp.whereStr = "where 1=1" +
				" and act_chkno.curr_code <> '901' " +   
				" and from_mark in ('02','03') " +
				" and ad_mark <> 'D' " + 
				" and proc_mark <> 'Y' " + 
				" and ACT_CHKNO.ID_P_SEQNO = CRD_IDNO.ID_P_SEQNO " + 
				" and ACT_CHKNO.P_SEQNO = ACT_ACNO.ACNO_P_SEQNO " +
				" and act_chkno.acct_type = :m_accttype " +
				" and act_acno.acct_key = :m_acctkey " +
				" and act_chkno.curr_code = :m_currcode " ;
		    setString("m_accttype", mAccttype);
		    setString("m_acctkey", mAcctkey);
		    setString("m_currcode", mCurrcode);

        wp.whereOrder=" ORDER BY CRD_IDNO.id_no  ASC , crd_idno.id_no_code ASC ";

        pageSelect();
      //wp.ddd("-->Actm0012-dsp01","read act_chkno, act_acno, crd_idno ");
      //wp.ddd("--:act_acno.stmt_cyle[%s]",wp.col_ss("stmt_cycle"));
				wp.colSet("pending_msg", "異動暫存");

        if (sqlNotFind()) {
		  		wp.colSet("pending_msg", "");
      	  this.selectOK();
      	  int wkChkNum = wkChkAcnoCurry();
      	  if(wkChkNum==-1){
//      		  wp.resetOutputData();
      		  wp.colSet("kk_acct_type", mAccttype);
      		  wp.colSet("kk_acct_key", mAcctkey);
      		  wp.colSet("kk_curr_code", mCurrcode);
      		  wp.colSet("p_seqno", "");
      		  alertErr2("帳戶帳號 輸入錯誤");      		  
      	  }	else if (wkChkNum==-2){      		 
//      		  wp.resetOutputData();
      		  wp.colSet("kk_acct_type", mAccttype);
      		  wp.colSet("kk_acct_key", mAcctkey);
      		  wp.colSet("kk_curr_code", mCurrcode);
      		  wp.colSet("p_seqno", "");
      		  alertErr2("帳戶帳號 無雙幣卡帳戶資料");
      	  }
      	  wp.colSet("acct_type", mAccttype);
      	  wp.colSet("acct_key", mAcctkey);
      	  wp.colSet("curr_code", mCurrcode);
      	  wp.colSet("valid_flag", "1");
        }
        else {
        	wkGetAcctCurr901();
        }

//        dddw_select();
  		String ls1 = "", ls2 = "";
			ls1 =wp.colStr("id_no");
			ls2 =wp.colStr("id_no_code");
			wp.colSet("tt_id_no", ls1+"-"+ls2);

		//wp.colSet("autopay_acct_bank", "0172015");			
      if ( empty(wp.colStr("autopay_acct_bank")) ) {
		     wp.colSet("autopay_acct_bank", "0060567");			
      }

		//wp.colSet("verify_flag", "Y");			

		  if(wp.colEq("from_mark", "01")){
			  wp.colSet("tt_from_mark", "APS");			
		  }	else if(wp.colEq("from_mark", "02")){
			  wp.colSet("tt_from_mark", "授權書-新申請");
		  }	else if(wp.colEq("from_mark", "03")){
			  wp.colSet("tt_from_mark", "授權書-修改帳號");
		  }	else if(wp.colEq("from_mark", "04")){
			  wp.colSet("tt_from_mark", "官網-eDDA");
		  }

			if(wp.colEq("acno_autopay_indicator", "1")){
				wp.colSet("tt_acno_autopay_indicator", "扣TTL");
			}	else if(wp.colEq("acno_autopay_indicator", "2")){
				wp.colSet("tt_acno_autopay_indicator", "扣MP");
			}	else if(wp.colEq("acno_autopay_indicator", "3")){
				wp.colSet("tt_acno_autopay_indicator", "其他");
			}
			
  		String ss = "";
	  	ss = wp.colStr("verify_return_code");
	
      if ( (!empty(ss)) && (!ss.equals("00")) && (!ss.equals("99")) ) {
         ss = "01";
	       wp.colSet("verify_return_code", ss);
      }


	  wp.colSet("chk_autopay_acct_bank", wp.colStr("autopay_acct_bank"));
	  wp.colSet("chk_autopay_acct_no", wp.colStr("autopay_acct_no"));
	  wp.colSet("chk_exchange_acct_no", wp.colStr("exchange_acct_no"));
	  wp.colSet("chk_autopay_indicator", wp.colStr("autopay_indicator"));
	  wp.colSet("chk_valid_flag", wp.colStr("valid_flag"));
	  wp.colSet("chk_autopay_id", wp.colStr("autopay_id"));
	  wp.colSet("chk_autopay_id_code", wp.colStr("autopay_id_code"));
	  wp.colSet("chk_autopay_dc_flag", wp.colStr("autopay_dc_flag"));
	  wp.colSet("chk_autopay_dc_indicator", wp.colStr("autopay_dc_indicator"));
	//wp.col_set("chk_verify_flag", wp.colStr("verify_flag"));
	  wp.colSet("chk_verify_flag", wp.colStr("verify_flag").equals("Y") ? "Y" : "N");
	  wp.colSet("chk_verify_return_code", wp.colStr("verify_return_code"));
		
  //讀出明細資料後，將鍵值存至 kp_ 並設定防止鍵值輸入屬性
		String s1 = wp.colStr("p_seqno");
    if ( !empty(s1) ) {
      wp.colSet("kp_acct_type", mAccttype );
      wp.colSet("kp_acct_key", mAcctkey );
      wp.colSet("kp_curr_code", mCurrcode );
      wp.colSet("kk_acct_type_attr", "disabled");
      wp.colSet("kk_acct_key_attr", "disabled");
      wp.colSet("kk_curr_code_attr", "disabled");
    //btnOn_query(false);
    }

    }

    int wkChkAcnoCurry() throws Exception{
   	 String sql1 = " select "
   	 				 + " a.p_seqno , "
   	 				 + " a.stmt_cycle , "
   	 			 //+ " uf_idno_id(a.id_p_seqno) as id_no , "
   	 				 + " c.id_no , "
   	 				 + " c.id_no_code , "
   	 				 + " b.autopay_indicator , "
   	 				 + " a.autopay_rate , "
   	 				 + " a.autopay_fix_amt , "
   	 				 + " b.autopay_acct_bank , "
   	 				 + " b.autopay_acct_no , "
   	 				 + " b.autopay_id , "
   	 				 + " b.autopay_id_code , "
   	 				 + " a.autopay_acct_s_date , "
   	 				 + " a.autopay_acct_e_date , "
   	 				 + " a.rc_use_indicator , "
   	 			 //+ " uf_acno_name(a.p_seqno) as chi_name , "
   	 				 + " c.chi_name , "
   	 				 + " a.acct_type , "
   	 				 + " a.acct_key "
   	 				 + " from act_acno a, act_acct_curr b, crd_idno c "
   	 				 + " where b.p_seqno = a.acno_p_seqno "
   	 				 + " and a.id_p_seqno = c.id_p_seqno "
   	 				 + " and b.curr_code = '901' "
   	 				 + " and a.acct_type = ? "
   	 				 + " and a.acct_key = ? "
   			 		 ;
   	 
   	 sqlSelect(sql1,new Object[]{mAccttype,mAcctkey});
   	 if(sqlRowNum<=0){
   		 return -1;
   	 }
   	 
   	 wp.colSet("p_seqno", sqlStr("p_seqno"));
   	 wp.colSet("stmt_cycle", sqlStr("stmt_cycle"));
   	 wp.colSet("db_holder_name", sqlStr("chi_name"));
   	 wp.colSet("id_no", sqlStr("id_no"));
   	 wp.colSet("id_no_code", sqlStr("id_no_code"));
   	 wp.colSet("acno_autopay_acct_bank", sqlStr("autopay_acct_bank"));
   	 wp.colSet("acno_autopay_acct_no", sqlStr("autopay_acct_no"));
   	 wp.colSet("acno_autopay_indicator", sqlStr("autopay_indicator"));
   	 wp.colSet("acno_autopay_fix_amt", sqlStr("autopay_fix_amt"));
   	 wp.colSet("acno_autopay_acct_s_date", sqlStr("autopay_acct_s_date"));
   	 wp.colSet("acno_autopay_acct_e_date", sqlStr("autopay_acct_e_date"));
   	 wp.colSet("acno_autopay_rate", sqlStr("autopay_rate"));
   	 wp.colSet("acno_autopay_id", sqlStr("autopay_id"));
   	 wp.colSet("acno_autopay_id_code", sqlStr("autopay_id_code"));
   	 
   	 String sql2 = " select "
   	 				 + " a.autopay_indicator , "
   	 				 + " a.autopay_acct_bank , "
   	 				 + " a.autopay_acct_no , "
        		 + " a.curr_change_accout as exchange_acct_no, " 
   	 				 + " a.autopay_id , "
   	 				 + " a.autopay_id_code , "
        		 + " (a.curr_code || '-' || p.curr_chi_name) as tt_curr_code, "
   	 				 + " a.autopay_dc_flag , "
   	 				 + " a.autopay_dc_indicator "
   	 				 + " from act_acct_curr a left join ptr_currcode p on a.curr_code = p.curr_code "
   	 				 + " where a.p_seqno = ? "
   	 				 + " and a.curr_code = ? "
   			 		 ;
   	 
   	 sqlSelect(sql2,new Object[]{sqlStr("p_seqno"),mCurrcode});
   	 
   	 if(sqlRowNum<=0){
   		 wp.colSet("autopay_acct_bank", "");
      	 wp.colSet("autopay_acct_no", "");
      	 wp.colSet("autopay_indicator", "");
      	 wp.colSet("autopay_id", "");
      	 wp.colSet("autopay_id_code", "");
      	 wp.colSet("autopay_dc_flag", "");
      	 wp.colSet("autopay_dc_indicator", "");      	 
   		 return -2;
   	 }
   	 
   	 wp.colSet("autopay_acct_bank", sqlStr("autopay_acct_bank"));
   	 wp.colSet("autopay_acct_no", sqlStr("autopay_acct_no"));
   	 wp.colSet("exchange_acct_no", sqlStr("exchange_acct_no"));
   	 wp.colSet("autopay_indicator", sqlStr("autopay_indicator"));
   	 wp.colSet("autopay_id", sqlStr("autopay_id"));
   	 wp.colSet("autopay_id_code", sqlStr("autopay_id_code"));
   	 wp.colSet("autopay_dc_flag", sqlStr("autopay_dc_flag"));
   	 wp.colSet("autopay_dc_indicator", sqlStr("autopay_dc_indicator"));
   	 wp.colSet("tt_curr_code", sqlStr("tt_curr_code"));
   	 
	 	 if(wp.colEmpty("autopay_acct_bank") || wp.colEmpty("autopay_acct_no")){
			 wp.colSet("from_mark", "02");
		 }	else	{
			 wp.colSet("from_mark", "03");
		 }
		
   	 return 0;
    }
    
    int wkGetAcctCurr901() throws Exception {

   	 String sql1 = " select "
   	 				 + " b.autopay_indicator , "
   	 				 + " a.autopay_rate , "
   	 				 + " a.autopay_fix_amt , "
   	 				 + " b.autopay_acct_bank , "
   	 				 + " b.autopay_acct_no , "
   	 				 + " b.autopay_id , "
   	 				 + " b.autopay_id_code , "
   	 				 + " a.autopay_acct_s_date , "
   	 				 + " a.autopay_acct_e_date , "
   	 				 + " a.rc_use_indicator "
   	 				 + " from act_acno a, act_acct_curr b "
   	 				 + " where b.p_seqno = a.acno_p_seqno "
   	 				 + " and b.curr_code = '901' "
   	 				 + " and a.acct_type = ? "
   	 				 + " and a.acct_key = ? "
   			 		 ;
   	 
   	 sqlSelect(sql1,new Object[]{mAccttype,mAcctkey});
   	 if(sqlRowNum<=0){
   		 return -1;
   	 }
   	 
   	 wp.colSet("acno_autopay_acct_bank", sqlStr("autopay_acct_bank"));
   	 wp.colSet("acno_autopay_acct_no", sqlStr("autopay_acct_no"));
   	 wp.colSet("acno_autopay_indicator", sqlStr("autopay_indicator"));
   	 wp.colSet("acno_autopay_fix_amt", sqlStr("autopay_fix_amt"));
   	 wp.colSet("acno_autopay_acct_s_date", sqlStr("autopay_acct_s_date"));
   	 wp.colSet("acno_autopay_acct_e_date", sqlStr("autopay_acct_e_date"));
   	 wp.colSet("acno_autopay_rate", sqlStr("autopay_rate"));
   	 wp.colSet("acno_autopay_id", sqlStr("autopay_id"));
   	 wp.colSet("acno_autopay_id_code", sqlStr("autopay_id_code"));
   	 
   	 return 0;
    }
    
    @Override
    public void saveFunc() throws Exception {
    	
  //檢核輸入明細資料前亦需先搬回鍵值並設定防止鍵值輸入屬性
      wp.colSet("kk_acct_type", wp.itemStr2("kp_acct_type") ); 
      wp.colSet("kk_acct_key", wp.itemStr2("kp_acct_key") );
      wp.colSet("kk_curr_code", wp.itemStr2("kp_curr_code") );
      wp.colSet("kk_acct_type_attr", "disabled");
      wp.colSet("kk_acct_key_attr", "disabled");
      wp.colSet("kk_curr_code_attr", "disabled");
    //btnOn_query(false);

	 	  if(wp.colEmpty("autopay_id_code") ) {
			  wp.itemSet("autopay_id_code", "0");
			  wp.colSet("autopay_id_code", "0");
		  }	

      //wp.ddd("-->Actm0012-dsp01","");
      //wp.ddd("--:wp.item_ss('autopay_dc_flag')[%s]",wp.item_ss("autopay_dc_flag"));
		
		//--是否異動--
		String lsAutopayDcFlag = "", lsVerifyFlag = "", lsAutopayDcIndicator = "", lsAutopayDcIndicatorChk = "";
		lsAutopayDcFlag = wp.itemStr2("autopay_dc_flag").equals("Y") ? "Y" : "N";
		lsVerifyFlag     = wp.itemStr2("verify_flag").equals("Y") ? "Y" : "N";
		if(lsAutopayDcFlag.equals("N"))	{
		  lsAutopayDcIndicator = ""; 
		} else {		  
		  lsAutopayDcIndicator = wp.itemStr2("autopay_dc_indicator"); 
		}		  

		if(wp.itemStr2("chk_autopay_dc_flag").equals("N"))	{
		  lsAutopayDcIndicatorChk = ""; 
		} else {		  
		  lsAutopayDcIndicatorChk = wp.itemStr2("chk_autopay_dc_indicator");  
		}		  

		if(this.isUpdate())	{
		  if (   eqIgno(commString.mid(wp.itemStr2("autopay_acct_bank"), 0, 3), commString.mid(wp.itemStr2("chk_autopay_acct_bank"), 0, 3)) 
				  && eqIgno(wp.itemStr2("autopay_acct_no"),wp.itemStr2("chk_autopay_acct_no"))
				  && eqIgno(wp.itemStr2("autopay_indicator"),wp.itemStr2("chk_autopay_indicator"))
				  && eqIgno(wp.itemStr2("valid_flag"),wp.itemStr2("chk_valid_flag"))
				  && eqIgno(wp.itemStr2("autopay_id"),wp.itemStr2("chk_autopay_id"))
				  && eqIgno(wp.itemStr2("autopay_id_code"),wp.itemStr2("chk_autopay_id_code"))
				  && eqIgno(lsAutopayDcFlag,wp.itemStr2("chk_autopay_dc_flag"))
				  && eqIgno(wp.itemStr2("exchange_acct_no"),wp.itemStr2("chk_exchange_acct_no"))
				  && eqIgno(lsAutopayDcIndicator,lsAutopayDcIndicatorChk)
				  && eqIgno(lsVerifyFlag,wp.itemStr2("chk_verify_flag"))
				  && eqIgno(wp.itemStr2("verify_return_code"),wp.itemStr2("chk_verify_return_code"))
         )
			{
			  alertErr("資料未異動, 不可存檔");
			  return ;
	 	  } 
		}
		
    	
    	if(isAdd() || isUpdate())  {
/***
    		if(checkAcct()==false) {
				alertErr2("扣繳帳號輸入錯誤！");
   			return ;
   		  }
    		
    		if(checkTW()==false){
				alertErr2("外幣帳號輸入錯誤！");
   			return ;
   		  }
***/    		
      //if(!wp.item_ss("verify_flag").equals("Y")) {
			//  err_alert("扣繳行庫為017，請勾選「驗印完畢」");
			//	return ;
		  //}

		    if (wp.itemStr2("verify_flag").equals("Y")) {
			    if(wp.itemEmpty("verify_return_code")){
				    alertErr2("驗印完畢必須有驗印結果！");
				    return ;
			    }			
			    wp.colSet("verify_date",wp.sysDate);			
			    wp.itemSet("verify_date",wp.sysDate);			
		    }	else {			
			    wp.colSet("verify_return_code","");			
			    wp.itemSet("verify_return_code","");
			    wp.colSet("verify_date","");			
			    wp.itemSet("verify_date","");			
		    }

    	}
    	
     	Actm0012Func func =new Actm0012Func(wp);

      rc = func.dbSave(strAction);
      log(func.getMsg());
      if (rc!=1) {
      alertErr2(func.getMsg());
      }
      this.sqlCommit(rc);

    	//if(isUpdate())  {
      //  mAccttype = wp.itemStr2("kp_acct_type"); 
      //  mAcctkey = wp.itemStr2("kp_acct_key");
      //  mCurrcode = wp.itemStr2("kp_curr_code");
			//  dataRead();
      //}

      dddwSelect();
 
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
    
	
	void sendSmsMesg() throws Exception{
		if(wfChkCycdate()!=1)	return; 
		
		if(wfCheckSmsSend("2")==1){
		  alertMsg("簡訊產生成功！");
    //檢核輸入明細資料前亦需先搬回鍵值並設定防止鍵值輸入屬性
      wp.colSet("kk_acct_type", wp.itemStr2("kp_acct_type") ); 
      wp.colSet("kk_acct_key", wp.itemStr2("kp_acct_key") );
      wp.colSet("kk_curr_code", wp.itemStr2("kp_curr_code") );
      wp.colSet("kk_acct_type_attr", "disabled");
      wp.colSet("kk_acct_key_attr", "disabled");
      wp.colSet("kk_curr_code_attr", "disabled");
    //btnOn_query(false);
			
      mAccttype = wp.itemStr2("kp_acct_type"); 
      mAcctkey = wp.itemStr2("kp_acct_key");
      mCurrcode = wp.itemStr2("kp_curr_code");
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
	
	int wfCheckSmsSend(String asType) throws Exception {
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
			Actm0012Func func = new Actm0012Func(wp);
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
	

  String getPseqno() throws Exception {
		String lsSql  = " select p_seqno from act_acno ";
			   lsSql += " where acct_type = :acct_type and acct_key = :acct_key ";
			   lsSql += " and acno_p_seqno = p_seqno ";
		setString("acct_type", wp.itemStr2("kk_acct_type"));
		setString("acct_key",  wp.itemStr2("kk_acct_key"));
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			return sqlStr("p_seqno");
		}
		return "";
	}

    @Override
    public void initButton() {
      //if (wp.respHtml.indexOf("_detl") > 0) {
      //    this.btnMode_aud();
      //}

      if (wp.respHtml.equals("actm0012"))  {
       //wp.col_set("btnAdd_disable","");
         this.btnModeAud();
      }

		  String s_key = "2nd-page";
	    if (wp.respHtml.indexOf("_detl")   > 0)   { 
          buttonOff("btnSms_disable"); //發送簡訊鍵 default off(disabled)
	        this.btnModeAud();//rowid 有值時，新增鍵 off(disabled)，修改鍵、刪除鍵 on
			  if  (empty(wp.colStr("p_seqno")) )  {  //p_seqno 無值時(沒有先讀act_acct_curr)，新增鍵 off(disabled)
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
//			wp.optionKey = wp.item_ss("acct_type");
			wp.optionKey = wp.colStr("kk_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			wp.optionKey = wp.colStr("kk_curr_code");
			this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' and wf_id<>'901' order by wf_id");
			
			wp.initOption = "--";
			wp.optionKey = wp.colStr("autopay_acct_bank");
			this.dddwList("dddw_acct_bank", "act_ach_bank", "bank_no", "bank_name", "where bank_no like '006%' order by bank_no");
//			this.dddw_list("dddw_acct_bank", "act_ach_bank", "bank_no", "bank_name", "where 1=1 order by bank_no");  //for test
        }
        catch(Exception ex) {}
    }

    String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}

}

