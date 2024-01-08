package ccam02;

/* ccam5180	特殊指示原因碼維護　spec_code
 * Table: cca_spec_code, cca_sys_parm2.SPEC
 * V01.0		Alex  2018-0625
 * V00.0		Alex	2017-0823
 *  V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 *109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 *111-10-25  V1.00.04   Alex          NEG參數下拉選單改為對FISC的下拉選單                                          *
 * */

import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5180 extends BaseEdit {
	Ccam5180Func func;
	String specCode,dataType1,checkLevel;

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
		} else if (eqIgno(wp.buttonCode, "U2")) {
			/* 更新功能 */
			detl2Save();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page*/
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode,"S2") || eqIgno(wp.buttonCode,"R2")) {	
			//-讀取明細資料-
			strAction = "R2";
			detl2Read(); 
		} else if (eqIgno(wp.buttonCode,"S5")) {	
			countDetail(wp.itemStr("spec_code"));
			readCheckLevel();
			
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		initButton();
//		if (wp.respHtml.equals("ccam5180_detl")) {
//			wp.col_set("level_code","1");
//		}
		
	}

	public void dddwSelect() {
		try {
		if (eqIgno(wp.respHtml,"ccam5180_detl")) {
//			wp.initOption = "--";
			wp.optionKey = wp.colStr(0, "resp_code");
			dddwList("dddw_resp_code","cca_resp_code", "resp_code", "resp_remark", "where 1=1");
			
			//--2022/10/25 參數改為對送財金下拉選單故類別改為 FISC
			wp.optionKey = wp.colStr(0, "neg_reason");
			dddwList("dddw_exept_reason_nccc","cca_sys_parm1", "sys_key", "sys_data1", "where sys_id = 'FISC'");

			wp.optionKey = wp.colStr(0, "visa_reason");
			dddwList("dddw_exept_reason_visa","Vcca_exception", "exc_code", "exc_desc", "where bin_type='VISA'");

			wp.optionKey = wp.colStr(0, "mast_reason");
			dddwList("dddw_exept_reason_mast","Vcca_exception", "exc_code", "exc_desc", "where bin_type='MAST'");

			wp.optionKey = wp.colStr(0, "jcb_reason");
			dddwList("dddw_exept_reason_jcb","Vcca_exception", "exc_code", "exc_desc", "where bin_type='JCB'");

		}
		}
		catch(Exception ex){}
	
	try {
		if (eqIgno(wp.respHtml,"ccam5180_risk")) {
			wp.optionKey = wp.colStr(0, "ex_risk_type");
			dddwList("dw_spec_risk_type","Vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
		}
		
		if (eqIgno(wp.respHtml,"ccam5180_eci")) {
			wp.optionKey = wp.colStr(0, "ex_data_code");
			dddwList("dw_spec_eci","cca_sys_parm1", "sys_key", "sys_data1", "where 1=1 and sys_id ='SPEC-ECI' ");
		}
		
		if (eqIgno(wp.respHtml,"ccam5180_ucaf")) {
			wp.optionKey = wp.colStr(0, "ex_data_code");
			dddwList("dw_spec_ucaf","cca_sys_parm1", "sys_key", "sys_data1", "where 1=1 and sys_id ='SPEC-UCAF' ");
		}
		
		if (eqIgno(wp.respHtml,"ccam5180_entry")) {
			wp.optionKey = wp.colStr(0, "ex_data_code");
			dddwList("dw_country","cca_country", "country_code", "country_remark", "where 1=1 order by country_code ");
			wp.optionKey = wp.colStr(0, "ex_data_code2");
			dddwList("dw_spec_entry","cca_entry_mode", "entry_mode", "entry_mode", "where 1=1 ");
		}
		
		}
		catch(Exception ex){}
	
	try {
		if (eqIgno(wp.respHtml,"ccam5180_mcc")) {
//			wp.initOption = "--";
			wp.optionKey = wp.colStr(0, "mcc_code");
			dddwList("dw_spec_mcc_risk","cca_mcc_risk", "mcc_code", "mcc_remark", "where 1=1");
		}
		}
		catch(Exception ex){}
	
	try {
		if (eqIgno(wp.respHtml,"ccam5180_spec")) {
//			wp.initOption = "--";
			wp.optionKey = wp.colStr(0, "spec_code");
			dddwList("d_spec_code_f1","cca_spec_code", "spec_code", "spec_desc", "where 1=1");
		}
		}
		catch(Exception ex){}
	}

	@Override
	public void queryFunc() throws Exception {
		wp.whereStr =" where 1=1 "
						+sqlCol(wp.itemStr("ex_spec_code"),"spec_code")
						;
		
		if(!wp.itemEq("ex_spec_type", "0")){
			wp.whereStr += sqlCol(wp.itemStr("ex_spec_type"),"spec_type");
		}
		
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
   
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = "spec_code,   "
				  + "spec_desc, "
				  + "resp_code,"
				  + "decode(check_level,'1','直接回覆','0','拒絕條件','3','核准條件','額度100%內可用') as check_level , "
				  + "neg_reason,"
				  + "visa_reason,"
				  + "mast_reason,"
				  + "jcb_reason,"
				  + "send_ibm, "
				  +" spec_type"
				  ;
		wp.daoTable = "CCA_spec_code";
		wp.whereOrder=" order by spec_code";

		pageQuery(); 

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		queryAfter();
		
		wp.setPageValue();
	}
void queryAfter() {
	for(int ii=0; ii<wp.selectCnt; ii++) {
		wp.colSet(ii, "tt_spec_type",commString.decode(wp.colStr(ii,"spec_type")
				, new String[]{"1","2"},new String[]{"凍結","特指"}));
	}
}
	@Override
	public void querySelect() throws Exception {
		specCode=wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if(empty(specCode)){
			specCode=itemKk("spec_code");
		}
		if (empty(specCode)) {
			alertErr("原因代碼: 不可空白");
			return;
		}
		wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
				  + "spec_code,   "
				  + "spec_desc, "
				  + "resp_code, "
				  + "check_level, "
//				  + "decode(check_flag05,'05','2',check_level) as check_level , "
				  + "decode(check_flag01,'01','Y','00','N',check_flag01) as check_flag01 ,"
				  + "decode(check_flag02,'02','Y','00','N',check_flag02) as check_flag02 ,"
				  + "decode(check_flag03,'03','Y','00','N',check_flag03) as check_flag03 ,"
				  + "decode(check_flag04,'04','Y','00','N',check_flag04) as check_flag04 ,"
				  + " check_flag05 , "
				  + "decode(check_flag06,'06','Y','00','N',check_flag06) as check_flag06 ,"				  
				  + "neg_reason, "
				  + "visa_reason, "
				  + "mast_reason, "
				  + "jcb_reason, "
				  + "send_ibm, "
				  + "spec_type, "
				  + "crt_date,"
				  + "mod_user, "
				  + "uf_2ymd(mod_time) as mod_date "
				  +", crt_user";
		wp.daoTable = "CCA_spec_code";
		wp.whereStr = "where 1=1"
				  + sqlCol(specCode, "spec_code")
				  ;
		
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + specCode);			
		}
		
		if (rc==1) {
			if (pos("|A|U",strAction)>0 && (wp.colEq("check_level", "0")||wp.colEq("check_level", "3"))) {
				wp.colSet("check_level_page"," || 1==1 ");
			}
		}				
		countDetail(specCode);
	}
	
	@Override
	public void saveFunc() throws Exception {
		
		func =new Ccam5180Func(wp);
		if(checkApproveZz()==false){
			return ;
		}
		rc = func.dbSave(strAction);
		//ddd(func.getMsg());
		if (rc!=1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc);
		if (rc==1 && this.pos("|A|U",strAction)>0) {
			userAction =true;
			dataRead();
		}

	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl")>0) {
			this.btnModeAud();		
		}
	}

	void detl2Read() throws Exception {
		wp.pageRows = 999;
		specCode =wp.itemStr("data_k1");	//-spec_code-
		dataType1 = wp.itemStr("data_k2");	//-data_type-
		checkLevel = wp.itemStr("data_k3");  //-check_level-
		
		if (empty(specCode)) {
			specCode =wp.itemStr("spec_code");
		}
		
		if (empty(dataType1)) {
			dataType1 =wp.itemStr("data_type");
		}
		
		if(empty(checkLevel)){
			checkLevel = wp.itemStr("check_level");
		}
		
		if (empty(specCode) || empty(dataType1)) {
			alertErr2("[原因代碼, 作業指示] 不可空白");
			return;
		}
		
		wp.colSet("check_level", checkLevel);
		if(eqIgno(checkLevel,"0")){
			wp.colSet("tt_check_level", "拒絕條件");
		}	else if(eqIgno(checkLevel,"3")){
			wp.colSet("tt_check_level", "核准條件");
		}
		
		
		wp.selectSQL = " data_code , "
						 + " data_code2 "
						 ;
		
		if(eqIgno(dataType1,"01")){
			wp.selectSQL += " , data_code||'_'||uf_tt_risk_type(data_code) as tt_data_code ";
		}	else if (eqIgno(dataType1,"02")){
			wp.selectSQL += " , data_code||'_'||uf_tt_mcc_code(data_code) as tt_data_code ";
		}	else if (eqIgno(dataType1,"03")){
			wp.selectSQL += " , data_code||'_'||(select sys_data1 from cca_sys_parm1 where sys_id ='SPEC-ECI' and sys_key = data_code) as tt_data_code " ;
		}	else if (eqIgno(dataType1,"04")){
			wp.selectSQL += " , data_code||'_'||(select sys_data1 from cca_sys_parm1 where sys_id ='SPEC-UCAF' and sys_key = data_code) as tt_data_code " ;
		}	
			
		wp.daoTable = " cca_spec_detl ";
		wp.whereStr = " where 1=1 "
						+ sqlCol(specCode,"spec_code")
						+ sqlCol(dataType1,"data_type")
						;
		pageQuery();
		
		if(sqlRowNum<=0){
			selectOK();
			wp.colSet("IND_NUM",""+0);
		}
		
		wp.setListCount(0);
		wp.colSet("IND_NUM",""+wp.selectCnt);	
	}	
	
	void countDetail(String lsSpecCode){
		String sql1 = " select "
						+ " count(*) as db_cnt "
						+ " from cca_spec_detl "
						+ " where spec_code = ? "
						;
		
		sqlSelect(sql1,new Object[]{lsSpecCode});
		
		wp.colSet("detail_cnt", ""+sqlStr("db_cnt"));
	}
	
	void detl2Save() throws Exception {
		int llOk=0, llErr=0;
		int ii=0;
		//String ls_opt="";
		
		Ccam5180Func func =new Ccam5180Func(wp);

		String[] lsDataCode = wp.itemBuff("data_code");
		String[] lsDataCode2 = wp.itemBuff("data_code2");
		String[] aaOpt =wp.itemBuff("opt");
		wp.listCount[0] = lsDataCode.length;
		wp.colSet("IND_NUM",""+lsDataCode.length);	
		if(checkApproveZz()==false){
			return ;
		}
		//-check duplication-
		if(wp.respHtml.equals("ccam5180_entry") == false) {
			ii = -1;
			for (String parm : lsDataCode) {
				ii++;
				wp.colSet(ii,"ok_flag","");
				//-option-ON-
				if (checkBoxOptOn(ii,aaOpt)) {
					continue;
				}

				if (ii != Arrays.asList(lsDataCode).indexOf(parm)) {
					wp.colSet(ii,"ok_flag","!");
					llErr++;
				}
			}
		}	else	{			
			String[] allCode = new String[wp.itemRows("data_code")];
			for(int ll=0;ll<wp.itemRows("data_code");ll++) {
				allCode[ll] = lsDataCode[ll]+lsDataCode2[ll];
			}
				
			ii = -1;
			for (String parm : allCode) {
				ii++;
				wp.colSet(ii,"ok_flag","");
				//-option-ON-
				if (checkBoxOptOn(ii,aaOpt)) {
					continue;
				}

				if (ii != Arrays.asList(allCode).indexOf(parm)) {
					wp.colSet(ii,"ok_flag","!");
					llErr++;
				}
			}
		}
		
		if (llErr>0) {
			alertErr("資料值重複: "+llErr);
			return;
		}
		
		//-delete no-approve-
		if (func.detlDelete()<0) {
			alertErr(func.getMsg());
			return;
		}
		
		//-insert-
		
		ii=-1;
		String dataCode2="";
		for (String dataCode : lsDataCode) {
			ii++;
			if (checkBoxOptOn(ii,aaOpt)) {
				continue;
			}

			dataCode2 ="";
			if (ii<lsDataCode2.length) {
				dataCode2 =lsDataCode2[ii];
			}
			if (empty(dataCode) && empty(dataCode2)) {
				ii++;
				continue;
			}
			func.varsSet("data_code",dataCode);
			func.varsSet("data_code2",dataCode2);
			
			if (func.detlInsert()==1) {
				llOk++;				
			}
			else {
				llErr++;
			}
		}
		if (llOk>0) {
			sqlCommit(1);
		}
		func.varsSet("db_cnt", ""+llOk);
		func.updateCheckFlag();
		
		alertMsg("資料存檔處理完成; OK="+llOk+", ERR="+llErr);
		detl2Read();
	}
	
	void readCheckLevel(){
		String sql1 = " select "
						+ " decode(check_flag01,'01','Y','00','N',check_flag01) as check_flag01 ,"
						+ " decode(check_flag02,'02','Y','00','N',check_flag02) as check_flag02 ,"
						+ " decode(check_flag03,'03','Y','00','N',check_flag03) as check_flag03 ,"
						+ " decode(check_flag04,'04','Y','00','N',check_flag04) as check_flag04 ,"						
						+ " decode(check_flag06,'06','Y','00','N',check_flag06) as check_flag06 "
						+ " from CCA_spec_code "
						+ " where spec_code = ? "				  
						;
		
		sqlSelect(sql1,new Object[]{wp.itemStr("spec_code")});
		if(sqlRowNum<=0)	return;
		
		wp.colSet("check_flag01", sqlStr("check_flag01"));
		wp.colSet("check_flag02", sqlStr("check_flag02"));
		wp.colSet("check_flag03", sqlStr("check_flag03"));
		wp.colSet("check_flag04", sqlStr("check_flag04"));
		wp.colSet("check_flag06", sqlStr("check_flag06"));
		
	}
	
}
