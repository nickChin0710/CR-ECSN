package ccam02;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Ccam5273 extends BaseAction {
	String smsPriority = "" , dataType = "";
	@Override
	public void userAction() throws Exception {
		switch (wp.buttonCode) {
	      case "X":
	        /* 轉換顯示畫面 */
	        strAction = "new";
	        clearFunc();
	        break;
	      case "Q":
	        /* 查詢功能 */
	        strAction = "Q";
	        queryFunc();
	        break;
	      case "R":
	        // -資料讀取-
	        strAction = "R";
	        dataRead();
	        break;
	      case "A":
	        /* 新增功能 */
	        saveFunc();
	        break;
	      case "U":
	        /* 更新功能 */
	        saveFunc();
	        break;
	      case "D":
	        /* 刪除功能 */
	        saveFunc();
	        break;
	      case "D2":
	    	detl2Delete();
	    	break;
	      case "M":
	        /* 瀏覽功能 :skip-page */
	        queryRead();
	        break;
	      case "S":
	        /* 動態查詢 */
	        querySelect();
	        break;
	      case "S2":
	    	strAction = "R2";
	        detl2Read();
	      	break;
	      case "R2":
	    	strAction = "R2";
		    detl2Read();
		    break;
	      case "L":
	        /* 清畫面 */
	        strAction = "";
	        clearFunc();
	        break;
	      case "C":
	        // -資料處理-
	        procFunc();
	        break;
	      case "AJAX":
	    	strAction = "AJAX";
	    	if(wp.respHtml.equals("ccam5273_mcht")) {
	    		if(wp.itemEq("do_code", "Q")) {
		    		processAjaxOption();
		    	}	else if(wp.itemEq("do_code", "I")) {
		    		detl2Insert();	    		
		    	}	    	
	    	}	else	{
	    		detl2Insert();
	    	}
	    	
	    	break;
	      case "UPLOAD":
	    	procFunc();
	    	break;
	      default:
	        break;
	    }		
	}

	@Override
	public void dddwSelect() {
		if("ccam5273_country".equals(wp.respHtml)) {
			try {
		    	wp.optionKey = wp.colStr("ex_data_code");		      	 
		        dddwList("dddw_country", "cca_country", "country_code", "country_code||'_'||country_no||'_'||country_remark","where ccas_link_type = 'FISC'");
		      } catch (Exception ex) {}
		}
		
		if("ccam5273_bin".equals(wp.respHtml)) {
			try {
		    	wp.optionKey = wp.colStr("ex_data_code");		      	 
//		        dddwList("dddw_bin", "ptr_bintable", "bin_no", "bin_no","where 1=1");
		        dddwList("dddw_bin","select distinct bin_no as db_code , bin_no as db_desc from ptr_bintable where 1=1");
		      } catch (Exception ex) {}
		}
		
		if("ccam5273_curr".equals(wp.respHtml)) {
			try {
		    	wp.optionKey = wp.colStr("ex_data_code");		      	 
		        dddwList("dddw_curr", "ptr_currcode", "curr_code", "curr_chi_name","where 1=1");
		      } catch (Exception ex) {}
		}
		
		if("ccam5273_mcc".equals(wp.respHtml)) {
			try {
		    	wp.optionKey = wp.colStr("ex_data_code");		      	 
		        dddwList("dddw_mcc", "cca_mcc_risk", "mcc_code", "mcc_remark","where 1=1");
		      } catch (Exception ex) {}
		}
		
//		if("ccam5273_resp".equals(wp.respHtml)) {
//			try {
//		    	wp.optionKey = wp.colStr("ex_data_code");		      	 
//		        dddwList("dddw_resp", "cca_resp_code", "resp_code", "resp_remark","where 1=1");
//		      } catch (Exception ex) {}
//		}
		
		if("ccam5273_trans".equals(wp.respHtml)) {
			try {
		    	wp.optionKey = wp.colStr("ex_data_code");		      	 
		        dddwList("dddw_trans", "cca_sys_parm3", "sys_key", "sys_data1","where 1=1 and sys_id ='TRANCODE'");
		      } catch (Exception ex) {}
		}
		
		if("ccam5273_group".equals(wp.respHtml)) {
			try {
		    	wp.optionKey = wp.colStr("ex_data_code");		      	 
		        dddwList("dddw_group", "ptr_group_code", "group_code", "group_name","where 1=1");
		      } catch (Exception ex) {}
		}
		
	}

	@Override
	public void queryFunc() throws Exception {
		if(chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr("建檔日期: 起迄錯誤");
			return ;
		}
		
		String lsWhere = " where 1=1 "
				+sqlCol(wp.itemStr("ex_date1"),"crt_date",">=")
				+sqlCol(wp.itemStr("ex_date2"),"crt_date","<=")
				+sqlCol(wp.itemStr("ex_sms_priority"),"sms_priority")
				+sqlCol(wp.itemStr("ex_area_type"),"area_type")
				;
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr ;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL =  " sms_priority , msg_id , spec_list , area_type , cond_country ,"
				+ " cond_curr , cond_bin , cond_mcht , cond_mcc , cond_pos , cond_trans_type ,"
				+ " cond_resp_code , cond_amt , cond_cnt1 , cond_cnt2 , crt_date , crt_user , "
				+ " decode(area_type,'1','國外','2','國內','0','不檢核') as tt_area_type , sms_remark , cond_group , "
				+ " cond_success "
				;
		wp.daoTable = "sms_msg_parm";
		wp.whereOrder = " order by sms_priority Asc ";
		pageQuery();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}		
		wp.setListCount(0);
		wp.setPageValue();		
	}

	@Override
	public void querySelect() throws Exception {
		smsPriority = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if(smsPriority.isEmpty())
			smsPriority = itemkk("sms_priority");
		if(smsPriority.isEmpty()) {
			alertErr("優先序: 不可空白");
			return ;
		}
		
		wp.selectSQL = "sms_priority , msg_id , spec_list , area_type , cond_country , cond_curr , cond_bin ,"
				+ "cond_mcht , cond_mcc , cond_pos , cond_trans_type , cond_resp_code , cond_amt , tx_amt , "
				+ "cond_cnt1 , tx_day , tx_dat_cnt , cond_cnt2 , tx_hour , tx_hour_cnt , crt_date , crt_user , "
				+ "to_char(mod_time,'yyyymmdd') as mod_date , mod_user , mod_seqno , hex(rowid) as rowid , sms_remark , cond_group , "
				+ "cond_success , cond_or_and1 , cond1_amt , cond_or_and2 , cond2_amt "
				;
		wp.daoTable = "sms_msg_parm";
		wp.whereStr = " where 1=1 "
				+sqlCol(smsPriority,"sms_priority");
		
		pageSelect();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
	}
	
	void detl2Read() throws Exception {
		smsPriority = wp.itemStr("data_k1");
		dataType = wp.itemStr("data_k2");
		
		if(smsPriority.isEmpty())
			smsPriority = wp.itemStr("sms_priority");
		if(dataType.isEmpty())
			dataType = wp.itemStr("data_type");
		
		if(smsPriority.isEmpty() || dataType.isEmpty()) {
			alertErr("優先序 或 資料類別: 不可空白");
			return ;
		}
		
		wp.selectSQL = "data_code1";
		wp.daoTable = "sms_msg_parm_detl";
		wp.whereStr = "where 1=1 "+sqlCol(smsPriority,"sms_priority") +sqlCol(dataType,"data_type");
		pageQuery();
		
		if(sqlNotFind()) {			
			selectOK();
			return ;
		}
		
		wp.setListCount(0);
		detl2ReadAfter();		
	}
	
	void detl2ReadAfter() {
		int llCnt = wp.selectCnt;
		String sql1 = "";
		if("COUNTRY".equals(dataType)) {
			sql1 = "select country_code||'_'||country_no||'_'||country_remark as tt_data_code from cca_country "
				+ " where ccas_link_type = 'FISC' and country_code = ? ";
		} else if("CURR".equals(dataType)) {
			sql1 = "select curr_code||'_'||curr_chi_name as tt_data_code from ptr_currcode where curr_code = ? ";
		} else if("MCHT".equals(dataType)) {
			sql1 = "select mcht_no||'_'||decode(mcht_name,'',mcht_eng_name,mcht_name) as tt_data_code from cca_mcht_bill where mcht_no = ? ";
		} else if("MCC".equals(dataType)) {
			sql1 = "select mcc_code||'_'||mcc_remark as tt_data_code from cca_mcc_risk where mcc_code = ? ";		
		} else if("GROUP".equals(dataType)) {
			sql1 = "select group_code||'_'||group_name as tt_data_code from ptr_group_code where group_code = ? ";
		} else if("TRANS_TYPE".equals(dataType)) {
			sql1 = "select sys_key||'_'||sys_data1 as tt_data_code from cca_sys_parm3 where sys_id ='TRANCODE' and sys_key = ? ";
		}
						
		for(int ii=0;ii<llCnt;ii++) {			
			if(sql1.isEmpty()) {
				if("POS".equals(dataType)) {
					wp.colSet(ii,"tt_data_code",  wp.colStr(ii,"data_code1")+"_"+getPosDetl(wp.colStr(ii,"data_code1")));
				}	else if("RESP".equals(dataType)) {
					wp.colSet(ii,"tt_data_code",  wp.colStr(ii,"data_code1")+"_"+getRespDetl(wp.colStr(ii,"data_code1")));
				}	else {
					wp.colSet(ii,"tt_data_code", wp.colStr(ii,"data_code1"));
				}				
				continue;
			}
			
			sqlSelect(sql1,new Object[] {wp.colStr(ii,"data_code1")});
			if(sqlRowNum >0) {
				wp.colSet(ii,"tt_data_code", sqlStr("tt_data_code"));
			}	else	{
				wp.colSet(ii,"tt_data_code", wp.colStr(ii,"data_code1"));
			}
		}		
	}
	
	String getRespDetl(String resp) {
		switch (resp) {
			case "00":
				return "Approve_Approved or completed successfully.";
			case "01":
				return "Call Issuer_Refer to card issuer";
			case "03":
				return "Decline_Invalid merchant";
			case "04":
				return "Capture_Capture card.";
			case "05":
				return "Decline_Do not honor.";
			case "12":
				return "Decline_Invalid transaction.";
			case "13":
				return "Decline_Invalid amount.";
			case "14":
				return "Decline_Invalid card number.";
			case "15":
				return "Decline_Invalid issuer";
			case "30":
				return "Decline_Format error.";
			case "41":
				return "Decline_Lost card.";
			case "43":
				return "Decline_Stolen card.";
			case "51":
				return "Decline_Insufficient funds/over credit limit.";
			case "54":
				return "Decline_Expired card.";
			case "55":
				return "Decline_Invalid PIN.";
			case "57":
				return "Decline_Transaction not permitted to issuer/cardholder.";
			case "61":
				return "Decline_Exceeds withdrawal amount limit.";
			case "62":
				return "Decline_Restricted card";
			case "68":
				return "Decline_Response Receive Too Late. Sent by STIP";
			case "70":
				return "Decline_FCARD沖銷交易";
			case "71":
				return "Decline_FCARD沖銷交易";
			case "75":
				return "Decline_Allowable number of PIN tries exceeded.";
			case "78":
				return "Decline_Invalid/non-existent account specified (general)";
			case "79":
				return "Decline_Invalid business date.";
			case "80":
				return "Decline_System not available.";
			case "85":
				return "Approve_核准－代碼持卡人驗證訊息";
			case "89":
				return "Decline_Bad terminal ID.";
			case "91":
				return "Decline_Destination processor (CPS or INF) not available.";
			case "92":
				return "Decline_Unable to route transaction.";
			case "96":
				return "Decline_System error.";
			default :
				return "";
		}
	}
	
	String getPosDetl(String pos) {
		
		switch (pos) {
			case "00":
				return "Unknown";
			case "01":
				return "PAN manual entry";
			case "03":
				return "Consumer presented QR Code, chip information included.(銀聯卡專用)";
			case "05":
				return "Chip card transaction, and CVV or iCVV is reliable";
			case "07":
				return "PAN auto-entry via contactless with chip";
			case "09":
				return "PAN entry via electronic commerce, including remote chip";
			case "10":
				return "COF (國際組織 VISA 專用)";
			case "80":
				return "Fallback transaction";
			case "81":
				return "PAN entry via electronic commerce";
			case "82":
				return "PAN auto entry via server";
			case "90":
				return "PAN auto-entry via magnetic stripe";
			case "91":
				return "PAN auto-entry via contactless with magnetic stripe";
			case "92":
				return "Magnetic stripe read; service code begins with '2' or '6'; last transaction was an unsuccessful IC read";
			case "95":
				return "Chip card transaction, but CVV or iCVV not reliabl";
			default :
				return "";
		}			
	}
	
	@Override
	public void saveFunc() throws Exception {

		ccam02.Ccam5273Func func = new ccam02.Ccam5273Func();
		func.setConn(wp);
		
		if(checkApproveZz()==false)
			return ;
		
		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if(rc !=1 ) {
			alertErr2(func.getMsg());
		}	else saveAfter(false);		
	}

	@Override
	public void procFunc() throws Exception {
		
		wp.listCount[0] = wp.itemRows("data_code1");
		
		if (itemIsempty("zz_file_name")) {			
		    alertErr2("上傳檔名: 不可空白");
		    return;
		}

		if (checkApproveZz() == false)			
		    return;
		
		fileDataImp();		
	}

	@Override
	public void initButton() {
		if("ccam5273_detl".equals(wp.respHtml)) {
			btnModeAud();
		}
		
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub
		
	}
	
	void detl2Insert() throws Exception {		
		ccam02.Ccam5273Func func = new ccam02.Ccam5273Func();
		func.setConn(wp);
		
		if(checkApproveZz()==false) {			
			if (eqIgno(strAction, "AJAX")) {
				wp.addJSON("ax_errmsg", "覆核失敗");
			}
			return ;
		}
		
		if (!eqIgno(strAction, "AJAX")) {
		  wp.listCount[0] = wp.itemRows("data_code");
		}
		rc = func.detlInsert();		
		sqlCommit(rc);
		if (eqIgno(strAction, "AJAX")) {			
			wp.addJSON("ax_rc", "" + rc);
		    if (rc == 1)
		      wp.addJSON("ax_errmsg", "新增成功");
		    else
		      wp.addJSON("ax_errmsg", func.getMsg());
		}	else {
		    if (rc != 1) {
		      alertErr2(func.getMsg());
		    } else {
		      wp.colSet("ex_data_code", "");
		      alertMsg("明細新增完成");
		    }
		}
	}
	
	void detl2Delete() throws Exception {
		ccam02.Ccam5273Func func = new ccam02.Ccam5273Func();
		func.setConn(wp);		
		if(checkApproveZz()==false)			
			return ;		
		
		int ilCnt = 0, ilOk = 0, ilErr = 0;

	    String[] aaOpt = wp.itemBuff("opt");
	    String[] lsDataCode = wp.itemBuff("data_code1");
	    wp.listCount[0] = wp.itemRows("data_code1");
	    func.varsSet("sms_priority", wp.itemStr2("sms_priority"));
	    func.varsSet("data_type", wp.itemStr2("data_type"));
		
	    for (int ii = 0; ii < wp.itemRows("data_code1"); ii++) {	    	
	        if (checkBoxOptOn(ii, aaOpt) == false)	        	
	        	continue;
	        ilCnt++;
	        func.varsSet("data_code1", lsDataCode[ii]);
	        if (func.dbDeleteDetl() != 1) {	        	
	        	ilErr++;
	        	wp.colSet(ii, "ok_flag", "X");
	        	dbRollback();
	        	continue;
	        } else {
	        	ilOk++;
	        	wp.colSet(ii, "ok_flag", "V");
	        	sqlCommit(1);
	        	continue;
	        }
	    }
	    
	    if (ilCnt == 0) {	    	
	        alertErr2("請選擇要刪除的資料");
	        return;
	    }
	    alertMsg("刪除明細完成,成功:" + ilOk + " 失敗:" + ilErr);	    
	}
	
	void fileDataImp() throws Exception {		
		TarokoFileAccess tf = new TarokoFileAccess(wp);
		String inputFile = wp.itemStr("zz_file_name");
		int fi = tf.openInputText(inputFile, "MS950");
		if (fi == -1) {
		  return;
		}
						
		Ccam5273Func func = new Ccam5273Func();
		func.setConn(wp);

		wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));		  		
		int llOk = 0, llCnt = 0 , llErr =0;
		
		if(func.dbDeleteDetlAll() !=1)
			return ;
		
		while (true) {			
			String tmpStr = tf.readTextFile(fi);
		    if (tf.endFile[fi].equals("Y")) 		    	  
		    	break;		    
		    if (tmpStr.length() <= 0)
		    	continue;		    
		    llCnt++;
		    String dataCode1 = tmpStr;
		    wp.itemSet("data_code1", dataCode1);
		    
		    if(func.dbFileInputDetl() !=1) {
		    	llErr++;
		    	dbRollback();
		    	continue;
		    }	else	{
		    	llOk++;
		    	dbCommit();
		    	continue;
		    }		    			    
		}

		tf.closeInputText(fi);
		tf.deleteFile(inputFile);
		wp.colSet("zz_file_name", "");
		alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk+" , 失敗筆數="+llErr);

		return;
	}
	
	public void processAjaxOption() throws Exception {
		selectNoLimit();
		wp.varRows = 200;
		wp.selectSQL = "mcht_no , mcht_name ";
		wp.daoTable = "cca_mcht_bill";
		wp.whereStr = "where 1=1 " + sqlCol(wp.itemStr("ex_data_code2"), "mcht_no", "like%");	        
		wp.whereOrder = "fetch first 200 rows only";
		pageQuery();
		for (int i = 0; i < wp.selectCnt; i++) {
			wp.addJSON("OPTION_TEXT", wp.colStr(i, "mcht_no") + "_" + wp.colStr(i, "mcht_name"));
			wp.addJSON("OPTION_VALUE", wp.colStr(i, "mcht_no"));
		}
		return;
	}
	
}
