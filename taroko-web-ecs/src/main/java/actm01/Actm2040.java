/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-16  V1.00.04  Simon      cancel autopay_indicator='3'               *
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon; 

public class Actm2040 extends BaseEdit {
	CommString commString = new CommString();
	Actm2040Func func;
    String mAccttype="";	
    String mAcctkey="";	
    String mCurrcode = "";
    
    int breakpoint = 0;
    
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
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
            updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
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
        }

        dddwSelect();
        initButton();
  }
    
  @Override
	public void initPage() {
    mCurrcode = "901";
		wp.colSet("ex_curr_code", mCurrcode);
		try {
		wp.colSet("tt_curr_code", wfPtrSysIdtabDesc(mCurrcode));
		}
    catch(Exception ex) {}
	//wp.col_set("pho_update_disable", "disabled");

	}
    
  @Override
  public void dddwSelect() {
        try {	
			wp.optionKey = wp.itemStr2("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
        }
        catch(Exception ex) {}
  }

  @Override
  public void queryFunc() throws Exception {
    	//設定queryRead() SQL條件
    	
        wp.pageRows=999;
        wp.setQueryMode();
        queryRead();
  }

  @Override
  public void queryRead() throws Exception {
        wp.pageControl();
        
        String lsPSeqno = getInitParm();
		if (lsPSeqno.equals("")) {
		//err_alert("無此帳號/卡號");
			alertErr2("此帳號不符");
			return;
		}

		String lsDualNo="", lsDualData="";
		lsDualNo = mAccttype + mAcctkey;
		String lsSql = "select log_data from act_dual "
						  + "where dual_key = :dual_key "
						  + "  and func_code = '0702' ";
		setString("dual_key", lsDualNo);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsDualData=sqlStr("log_data");
			setActDual(lsDualData);
		} else {
			lsSql = "select b.autopay_indicator, "
					+ "b.autopay_acct_bank, "
					+ "b.autopay_acct_no, "
					+ "b.autopay_id, "
				//+ "a.autopay_fix_amt, "
				//+ "a.autopay_rate, "
					+ "a.autopay_acct_s_date, "
					+ "decode(a.autopay_acct_e_date,'','99991231', a.autopay_acct_e_date) autopay_acct_e_date, "
					+ "a.acct_status, "
					+ "'N' as ex_dual_flag "
					+ "from act_acno a, act_acct_curr b "
					+ "where a.acno_p_seqno = b.p_seqno "
					+ "and b.curr_code ='901' "
					+ "and b.p_seqno = :p_seqno ";
			setString("p_seqno", lsPSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				String lsApayInd="", lsApayEdate="";
				lsApayInd = sqlStr("autopay_indicator");
				lsApayEdate = sqlStr("autopay_acct_e_date");
			//if (eqIgno(lsApayInd,"1")==false && eqIgno(lsApayInd,"2")==false) lsApayInd = "3";
				if (eqIgno(lsApayEdate,"99991231")) lsApayEdate = "";
				
				wp.colSet("ex_autopay_acct_bank", sqlStr("autopay_acct_bank"));
				wp.colSet("ex_autopay_acct_no", sqlStr("autopay_acct_no"));
				wp.colSet("ex_autopay_acct_s_date", sqlStr("autopay_acct_s_date"));
				wp.colSet("ex_autopay_acct_e_date", lsApayEdate);
				wp.colSet("ex_autopay_indicator", lsApayInd);
			//wp.colSet("ex_autopay_fix_amt", sqlNum("autopay_fix_amt"));
			//wp.colSet("ex_autopay_rate", sqlStr("autopay_rate"));
				wp.colSet("ex_autopay_id", sqlStr("autopay_id"));
				wp.colSet("ex_acct_status", sqlStr("acct_status"));
				wp.colSet("tt_acct_status", commString.decode(sqlStr("acct_status"), ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
				wp.colSet("ex_dual_flag", sqlStr("ex_dual_flag"));
				
			} else {
				alertErr("查無資料, acct_type="+mAccttype+", acct_key="+mAcctkey);
			}
		}

		wp.colSet("chk_ex_acct_type", wp.itemStr2("ex_acct_type"));
		wp.colSet("chk_ex_acct_key", wp.itemStr2("ex_acct_key"));
		wp.colSet("chk_ex_autopay_acct_e_date", wp.colStr("ex_autopay_acct_e_date"));
	  wp.colSet("chk_ex_autopay_indicator", wp.colStr("ex_autopay_indicator"));
	//wp.colSet("chk_ex_autopay_fix_amt", wp.colStr("ex_autopay_fix_amt"));
	//wp.colSet("chk_ex_autopay_rate", wp.colStr("ex_autopay_rate"));

	}
	
  private String getInitParm() throws Exception {
		String lsSql = "";
		
		mAccttype = wp.itemStr2("ex_acct_type");
    	mAcctkey  = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
		
		lsSql  = " select acct_type, acct_key, p_seqno, uf_acno_name(p_seqno) as acno_cname ";
		lsSql += " from act_acno ";
		lsSql += " where 1=1 ";
		lsSql += "and acno_p_seqno = p_seqno ";
		lsSql += "and acct_type = :acct_type and acct_key = :acct_key ";
		setString("acct_type", mAccttype);
		setString("acct_key",  mAcctkey);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("acct_type", sqlStr("acct_type"));
			wp.colSet("acct_key", sqlStr("acct_key"));
			wp.colSet("p_seqno", sqlStr("p_seqno"));
			wp.colSet("ex_cname", sqlStr("acno_cname"));
			return sqlStr("p_seqno");
		}
		return "";
	}

	void setActDual(String as_data) {
		String lsApayInd = "", lsApayBank = "", lsApayAcctno = "", lsModSelect = "";
		String lsApaySdate = "", lsApayEdate = "", lsApayId = "", lsAcctSts = "";
		double ldAmt = 0;
		int liRate = 0;
    System.out.println("setActDual: as_data="+as_data);
		lsApayInd		= commString.mid(as_data, 0, 1);		// autopay_indicator
	//ldAmt			= commString.strToNum(commString.mid(as_data, 1, 11));	// autopay_fix_amt (11,2)
	//liRate			= commString.strToInt(commString.mid(as_data, 12, 3));	// autopay_rate    (3,0)
		lsApayBank	= commString.mid(as_data, 1, 8);	// autopay_acct_bank
		lsApayAcctno	= commString.mid(as_data, 9, 16);	// autopay_acct_no
		lsApaySdate	= commString.mid(as_data, 25, 8);	// autopay_acct_s_date
		lsApayEdate	= commString.mid(as_data, 33, 8);	// autopay_acct_e_date
		lsApayId		= commString.mid(as_data, 41, 20);	// autopay_id
		lsAcctSts		= commString.mid(as_data, 61, 1);	// acct_status
		lsModSelect	= commString.mid(as_data, 62, 1);	// ex_modify_select
		
	//if (eqIgno(lsApayInd,"1")==false && eqIgno(lsApayInd,"2")==false) lsApayInd = "3";
		if (eqIgno(lsApayEdate,"99991231")) lsApayEdate = "";
		
		wp.colSet("ex_autopay_acct_bank", lsApayBank);
		wp.colSet("ex_autopay_acct_no", lsApayAcctno);
		wp.colSet("ex_autopay_acct_s_date", lsApaySdate);
		wp.colSet("ex_autopay_acct_e_date", lsApayEdate);
		wp.colSet("ex_autopay_indicator", lsApayInd);
	//wp.colSet("ex_autopay_fix_amt", ldAmt);
	//wp.colSet("ex_autopay_rate", liRate);
		wp.colSet("ex_autopay_id",lsApayId);
		wp.colSet("ex_acct_status", lsAcctSts);
		wp.colSet("tt_acct_status", commString.decode(lsAcctSts, ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
		wp.colSet("ex_dual_flag", "Y");
		wp.colSet("ex_modify_select", lsModSelect);

	//wp.col_set("pho_update_disable", "");
	//wp.col_set("pho_delete_disable", "");
  }
    
  String wfPtrSysIdtabDesc(String idcode) throws Exception {
		String rtn = "";
		String lsSql = "select wf_desc from ptr_sys_idtab "
				+ "where wf_type = 'DC_CURRENCY' and wf_id = :wf_id ";
		setString("wf_id", idcode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) rtn = sqlStr("wf_desc");
		
		return rtn;
	}

  @Override
  public void querySelect() throws Exception {
        dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    	alertErr("查無資料");
  }

  @Override
  public void saveFunc() throws Exception {

 /***
  //檢核輸入明細資料前亦需先搬回原值並設定防止輸入屬性
		String ls_modify_select = "", ls_autopay_acct_e_date = "";
		ls_modify_select = wp.item_ss("kp_ex_modify_select");
		ls_autopay_acct_e_date = wp.item_ss("kp_ex_autopay_acct_e_date");

		if (eq_igno(ls_modify_select,"2") ) {
      wp.col_set("ex_autopay_acct_e_date", ls_autopay_acct_e_date );
      wp.col_set("zReq_attr", "");
		}
		else {
      wp.col_set("zReq_attr", "zRequire='Y'");
		}

    wp.col_set("ex_modify_select", ls_modify_select );
    wp.col_set("ex_mod_s1_attr", "disabled");
    wp.col_set("ex_mod_s2_attr", "disabled");
 ***/

    func = new Actm2040Func(wp);
    	
    System.out.println("saveFunc: init");
//	if (of_validation()<0) return;
		if (ofValidation()<0) {
			if (breakpoint==0) { //中斷做confirm時,不清breakpoint_x ; 其他狀況(跳出or完成)清除breakpoint_x
				wp.colSet("breakpoint_1","");
		   	wp.colSet("breakpoint_2","");
			}
			return;
		}
		wp.colSet("breakpoint_1","");
    wp.colSet("breakpoint_2","");
		
		//--是否異動--
		String ls_dual = wp.itemStr2("ex_dual_flag");
		String ls_mod_select = wp.itemStr2("ex_modify_select");

//	if not dw_data.ib_datamodify and ls_dual<>'Y' then
		if ((eqIgno(wp.itemStr2("ex_autopay_acct_e_date"),wp.itemStr2("chk_ex_autopay_acct_e_date")) 
				&& eqIgno(wp.itemStr2("ex_autopay_indicator"),wp.itemStr2("chk_ex_autopay_indicator"))
			//&& wp.itemNum("ex_autopay_fix_amt")==wp.itemNum("chk_ex_autopay_fix_amt")
			//&& wp.itemNum("ex_autopay_rate")==wp.itemNum("chk_ex_autopay_rate"))
			  )
			  && eqIgno(ls_mod_select,"2") 
			  && strAction.equals("D")==false) {
			alertErr("資料未異動, 不可存檔");
			return;
		}
		
		if (strAction.equals("D")){
//			if of_excmsg("是否刪除此筆資料?")<>1 then Return -1
		}
		
		ofUpdate();  //set log_data

    rc = func.dbSave(strAction);
    if (rc!=1) {
        alertErr2(func.getMsg());
    } else {
        wp.colSet("ex_dual_flag","Y");
    }
    this.sqlCommit(rc);
  }
    
  int ofValidation() throws Exception {
    	int llCnt;
    	String lsActype, lsAckey, lsModSelect;
    	String lsPSeqno, lsRcUse, lsApayInd;
    	String lsSDate, lsEDate;
    	double lmAmt, lmRate, lmRate2;
    	
    	lsActype = wp.itemStr2("acct_type");
    	lsAckey = wp.itemStr2("acct_key");
		func.varsSet("dual_no", lsActype + lsAckey);
		lsPSeqno = wp.itemStr2("p_seqno");
		if (empty(lsActype) || empty(lsAckey)) {
			alertErr("帳戶帳號  不可空白");
			return -1;
		}
		if (empty(lsPSeqno)) {
			alertErr("未讀取資料, 不可存檔/刪除");
			return -1;
		}

		if ( !eqIgno(wp.itemStr2("ex_acct_type"),wp.itemStr2("chk_ex_acct_type") ) || 
		     !eqIgno(wp.itemStr2("ex_acct_key"),wp.itemStr2("chk_ex_acct_key") )     )
		{
			alertErr("鍵值已更改, 不可存檔/刪除, 請重新查詢");
			return -1;
		}

		if (eqIgno(strAction,"D")) return 1;
		
		if (empty(wp.itemStr2("ex_autopay_acct_no"))) {
			alertErr("帳戶無自動扣繳帳號, 不可以修改!");
			return -1;
		}

		lsApayInd = wp.itemStr2("ex_autopay_indicator");
/***
		lmAmt = wp.itemNum("ex_autopay_fix_amt");
		lmRate = wp.itemNum("ex_autopay_rate");
		if (eqIgno(lsApayInd,"1") || eqIgno(lsApayInd,"2")) {
			if (lmAmt != 0 || lmRate != 0) {
				alertErr("自動扣繳指示碼為 [扣TTL] 或 [扣MP] 時, 自動扣繳金額及百分比不可以有值!");
				return -1;
			}
		} else {
			if (lmAmt == 0 && lmRate == 0) {
				alertErr("自動扣繳指示碼為 [其他] 時, 自動扣繳金額或百分比不可以空白!");
				return -1;
			} else if (lmAmt != 0 && lmRate != 0) {
				alertErr("自動扣繳指示碼為 [其他] 時, 自動扣繳金額及百分比不可以都有值!");
				return -1;
			} else if (lmRate > 0) {
				lmRate2 = wfGetMinPercentPayment();
				if (lmRate < lmRate2) { 
//					alert_err("扣繳百分比小於系統標準MP百分比,請確認");  //todo JAVA確認
					if (eqIgno(wp.itemStr2("breakpoint_1"),"Y")==false) {
						breakpoint = 1;
			    		wp.colSet("breakpoint", 1);
			    		wp.dispMesg = "檢核中...";
			    		return -1;
			    	}
				}
			}
		}
		
		if (lmRate > 100) {
			alertErr("扣繳百分比不可大於100!");
			return -1;
		}
***/

		String lsSql = "select rc_use_indicator from act_acno "
						+ "where acct_type = :acct_type "
						+ "  and acct_key  = :acct_key ";
		setString("acct_type", lsActype);
		setString("acct_key", lsAckey);
		sqlSelect(lsSql);
		lsRcUse = sqlStr("rc_use_indicator");
		if (eqIgno(lsRcUse,"3") && eqIgno(lsApayInd,"1")==false) {
//			alert_err("此帳戶不準允用RC,請確認");  //todo JAVA確認
			if (eqIgno(wp.itemStr2("breakpoint_2"),"Y")==false) {
	    		breakpoint = 2;
	    		wp.colSet("breakpoint", 2);
	    		wp.dispMesg = "檢核中...";
	    		return -1;
	    	}
		}

		//自扣帳號生效起迄日期檢核
		lsSDate = wp.itemStr2("ex_autopay_acct_s_date");
		lsEDate = wp.itemStr2("ex_autopay_acct_e_date");
		if (chkStrend(lsSDate, lsEDate) == false) {
			alertErr("[自扣帳號生效起日]不可大於[迄日]");
			return -1;
		}

		lsModSelect = wp.itemStr2("ex_modify_select");
		if (eqIgno(lsModSelect,"1") && empty(lsEDate)) {
			alertErr("[自扣帳號生效迄日] 不可空白");
			return -1;
		}

		if (eqIgno(lsModSelect,"1") && 
		   !eqIgno(wp.itemStr2("ex_autopay_indicator"),wp.itemStr2("chk_ex_autopay_indicator"))) {
			alertErr("[取消帳號]不可調整[扣繳指示碼]");
			return -1;
		}

		if (eqIgno(lsModSelect,"2") && 
		   !eqIgno(wp.itemStr2("ex_autopay_acct_e_date"),wp.itemStr2("chk_ex_autopay_acct_e_date"))) {
			alertErr("[修改參數]不可維護生效[迄日]");
			return -1;
		}

		if (empty(lsEDate)) lsEDate="99991231";
		if (chkStrend(wp.sysDate, lsEDate) == false) {
			alertErr("[自扣帳號生效迄日] 不可小於 [系統日]");
			return -1;
		}
		
		//-外幣-
/***
		if (eqIgno(lsModSelect,"1")) {
			lsSql = "select count(*) as ll_cnt from act_acct_curr "
					+ "where p_seqno = :p_seqno "
					+ "  and curr_code <> '901' "
					+ "  and autopay_dc_flag='Y' ";
			setString("p_seqno", lsPSeqno);
			sqlSelect(lsSql);
			llCnt = (int) sqlNum("ll_cnt");
			if (llCnt > 0) {
				alertErr("雙幣帳戶: 外~幣存款不足轉扣台幣, 不可取消自扣帳號");
				return -1;
			}
		}
***/

		return 1;
	}
    
  double wfGetMinPercentPayment() throws Exception {
    	double ldRate = 0;
    	
    	String lsSql = "select min_percent_payment from ptr_actgeneral ";
		sqlSelect(lsSql);
		if (sqlRowNum > 0) ldRate = sqlNum("min_percent_payment");
    	
    	return ldRate;
  }
    
  void ofUpdate() throws Exception {
    	String lsLog="";
    System.out.println("of_update: init");
    	// make log_data string
    /*
    	mid(ls_data,1,1)		// autopay_indicator
    	mid(ls_data,2,11)		// autopay_fix_amt (11,2)
    	mid(ls_data,13,3)		// autopay_rate    (3,0)
    	mid(ls_data,16,4)		// autopay_acct_bank
    	mid(ls_data,20,16)	// autopay_acct_no
    	mid(ls_data,36,8)		// autopay_acct_s_date
    	mid(ls_data,44,8)		// autopay_acct_e_date
    	mid(ls_data,52,20)	// autopay_id
    	mid(ls_data,72,1)	   // acct_status
    	mid(ls_data,73,1)    // ex_modify_select
    */
		lsLog  = fixLength(wp.itemStr2("ex_autopay_indicator"),1);
	//lsLog += String.format("%011.2f",wp.itemNum("ex_autopay_fix_amt"));
	//lsLog += String.format("%03d",(long)wp.itemNum("ex_autopay_rate"));
		lsLog += fixLength(wp.itemStr2("ex_autopay_acct_bank"),8);
		lsLog += fixLength(wp.itemStr2("ex_autopay_acct_no"),16);
		lsLog += fixLength(wp.itemStr2("ex_autopay_acct_s_date"),8);
		lsLog += fixLength(wp.itemStr2("ex_autopay_acct_e_date"),8);
		lsLog += fixLength(wp.itemStr2("ex_autopay_id"),20);
		lsLog += fixLength(wp.itemStr2("ex_acct_status"),1);
		lsLog += fixLength(wp.itemStr2("ex_modify_select"),1);
    System.out.println("of_update: ["+lsLog+"]");
    func.varsSet("log_data", lsLog);
  }

  @Override
  public void initButton() {
    //if (wp.respHtml.indexOf("_detl") > 0) {
    //    this.btnMode_aud();
    //}
    	String sKey = "1st-page";
      if (wp.respHtml.equals("actm2040"))  {
         wp.colSet("btnUpdate_disable","");
         wp.colSet("btnDelete_disable","");
         this.btnModeAud(sKey);
      }
   
  }
    
  String fixLength(String laText, int laWidth) throws Exception {
		String rtn = laText;
		if (empty(rtn)) rtn="";
		int fnum;
		if (laText.length() < laWidth) {
			fnum = laWidth - laText.length();
			rtn += commString.fill(' ',fnum);
		}
		return rtn;
	}

  String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
    
}

