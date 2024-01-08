/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-11-22  V1.00.01  Alex            init ind_num ,usr_name                     *
* 108-12-05  V1.00.02  Alex            add initButton									  *
* 108-12-17  V1.00.03  Alex            checkbox keep                              *
* 109-07-22  V1.00.04  JustinWu   add error_reason, display ok_flag
* 111-11-06  V1.00.03  machao        頁面bug調整
******************************************************************************/
package cmsm02;

import java.util.Arrays;

import ofcapp.BaseEditMulti;
import taroko.com.TarokoCommon;

public class Cmsm6015 extends BaseEditMulti {

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc=1;

		strAction = wp.buttonCode;
		//ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		
		} else if (eqIgno(wp.buttonCode, "R")) { 
			//-資料讀取- 
			strAction = "R";
			dataRead();		
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();	
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 存檔功能 */
			saveFunc();		
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		initButton();
	}

	
	@Override
	public void dataRead() throws Exception {
		String lsWhere =" where 1=1 "
				+sqlCol(wp.itemStr("ex_case_type"),"case_type")
				;				
		
		wp.pageControl();
		wp.selectSQL = ""
			+ " case_type , "
			+ " case_id,"
			+ " case_desc,"
			+ " mod_user,"
			+ " mod_seqno,"
			+ " to_char(mod_time,'yyyymmdd') as mod_date,"
			+ " to_char(mod_time,'hh24miss') as mod_time,"
			+ " crt_date,"
			+ " crt_user,"
			+ " mod_seqno,hex(rowid) as rowid , "
			+ " case_id||','||case_desc as old_data , "
			+ " (select usr_cname from sec_user where usr_id = cms_casetype.mod_user) as usr_cname "
	      ;
		wp.daoTable = "CMS_CASETYPE";
		wp.whereOrder=" order by case_id Asc ";
		wp.whereStr = lsWhere ;
		logSql();
		pageQuery();
				
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			
			alertErr2("此條件查無資料");
			wp.colSet("IND_NUM",0);
			return;
		}

		wp.setPageValue();
		wp.colSet("IND_NUM",""+wp.selectCnt);
	}

	@Override
	public void saveFunc() throws Exception {			
		
		String userChiName = "";
		 
		int llOk=0, llErr=0;
		//String ls_opt="";
		String[] liModSeqno = wp.itemBuff("mod_seqno");
		String[] lsRowid =wp.itemBuff("rowid");
		String[] lsCi = wp.itemBuff("case_id");
		String[] lsCd = wp.itemBuff("case_desc");
		String[] liMd = wp.itemBuff("mod_date");
		String[] lsMu = wp.itemBuff("mod_user");
		String[] lsCrtDate = wp.itemBuff("crt_date");
		String[] lsCrtUser = wp.itemBuff("crt_user");
		String[] aaOld = wp.itemBuff("old_data");
		String[] aaOpt =wp.itemBuff("opt");
		wp.listCount[0] = wp.itemRows("case_id");
		optNumKeep(wp.itemRows("case_id"));
		wp.colSet("IND_NUM",""+lsCi.length);	
		if (!checkApprove(wp.itemStr("approval_user"),wp.itemStr("approval_passwd"))) {
			return;
		}
		cmsm02.Cmsm6015Func func =new cmsm02.Cmsm6015Func(wp);
		func.varsSet("case_type",wp.itemStr("ex_case_type"));
		func.varsSet("apr_user",wp.itemStr("approval_user"));		
		//-insert-
		for (int ll=0; ll<wp.itemRows("case_id"); ll++) {
			//wp.ddd("ll="+ll);
			
			if (empty(lsCi[ll])) {
				continue;
			}
			//wp.ddd("2");
			String lsNew=lsCi[ll]+","+lsCd[ll];
			func.varsSet("case_id",lsCi[ll]);
			func.varsSet("case_desc",lsCd[ll]);
			func.varsSet("mod_date",liMd[ll]);
			func.varsSet("mod_user",lsMu[ll]);
			func.varsSet("crt_date",lsCrtDate[ll]);
			func.varsSet("crt_user",lsCrtUser[ll]);
			func.varsSet("mod_seqno",liModSeqno[ll]);
			func.varsSet("rowid",lsRowid[ll]);
			func.varsSet("rowNumber", Integer.toString(ll));
			
			//-option-ON-
			if (checkBoxOptOn(ll,aaOpt)) {
				//-call dbdelete-
				if (func.dbDelete()==1) {
					llOk++;
					wp.colSet(ll, "ok_flag", "V");
				}else {
					llErr++;
					wp.colSet(ll, "ok_flag", "X");
					wp.colSet(ll, "error_reason", func.getMsg());
				}
			}
			else {
				//-edit??-
				if (eqAny(lsNew,aaOld[ll])) {
					continue;
				}
				if (func.selectData()==1) {
					llOk++;
					wp.colSet(ll, "ok_flag", "V");
					
					if (userChiName.length() == 0) {
						userChiName = getUserChineseName(wp.loginUser);
					}
					wp.colSet(ll, "usr_cname", userChiName);
				}else{
					llErr++;
					wp.colSet(ll, "ok_flag", "X");
					wp.colSet(ll, "error_reason", func.getMsg());
				}
			}
			//--
		}
		if (llOk>0) {
			sqlCommit(1);
		}
		alertMsg("資料存檔處理完成; OK="+llOk+", ERR="+llErr);
//		dataRead();
	}																
		/*		int ll_ok=0, ll_err=0;
		int ii=0;
		//String ls_opt="";
		
		busi.cmsm02.Cmsm6015Func func =new busi.cmsm02.Cmsm6015Func(wp);
		
		String[] ls_ci = wp.item_buff("case_id");
		String[] ls_cd = wp.item_buff("case_desc");
		String[] li_md = wp.item_buff("mod_date");
		String[] ls_mu = wp.item_buff("mod_user");
		String[] ls_crt_date = wp.item_buff("crt_date");
		String[] ls_crt_user = wp.item_buff("crt_user");
		String[] aa_opt =wp.item_buff("opt");
		wp.listCount[0] = ls_ci.length;
		wp.col_set("IND_NUM",""+ls_ci.length);	
		
		//-check duplication-
		ii = -1;
		func.vars_set("case_type",wp.item_ss("ex_case_type"));
		func.vars_set("apr_user",wp.item_ss("approval_user"));
		for (String ss : ls_ci) {
			ii++;
			wp.col_set(ii,"ok_flag","");
			//-option-ON-
			if (checkBox_opt_on(ii,aa_opt)) {
				ls_ci[ii]="";
				continue;
			}

			if (ii != Arrays.asList(ls_ci).indexOf(ss)) {
				wp.col_set(ii,"ok_flag","!");
				ll_err++;
			}
		}
		if (ll_err>0) {
			alert_err("資料值重複: "+ll_err);
			return;
		}
		
		
		if (func.dbDelete()<0) {
			alert_err(func.getMsg());
			return;
		}
		
		//-insert-
		ii=-1;
		for (String ss : ls_ci) {
			ii++;
			if (empty(ss)) {
				continue;
			}
			//-option-ON-
			if (checkBox_opt_on(ii,aa_opt)) {
				continue;
			}
			
			
			func.vars_set("case_id",ls_ci[ii]);
			func.vars_set("case_desc",ls_cd[ii]);
			func.vars_set("mod_date",li_md[ii]);
			func.vars_set("mod_user",ls_mu[ii]);
			func.vars_set("crt_date",ls_crt_date[ii]);
			func.vars_set("crt_user",ls_crt_user[ii]);
			if (func.dbInsert()==1) {
				ll_ok++;
			}
			else {
				ll_err++;
			}
		}
		if (ll_ok>0) {
			sql_commit(1);
		}
		alert_msg("資料存檔處理完成; OK="+ll_ok+", ERR="+ll_err);
		dataRead();
}*/

	private String getUserChineseName(String loginUser) {			
		
		wp.selectSQL = " usr_cname as userChineseName ";
		wp.daoTable = "sec_user";
		wp.whereStr = " where 1=1 "+ sqlCol(wp.loginUser,"usr_id");

		pageQuery();

		if (sqlRowNum <= 0) {
			alertErr2("找不到使用者中文名");
			return "";
		}else {
			return wp.colStr("userChineseName");
		}
	}


	@Override
	public void deleteFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		btnModeAud("XX");

	}
	
	@Override
	public void initPage() {
		wp.colSet("ind_num", "0");
	}
	
}
