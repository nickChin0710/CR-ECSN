/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-09-26  V1.00.01  Ryan       Initial                                  *
* 112-05-30  V1.00.02  Ryan       增加貴賓卡查詢掛號號碼維護、barcode_num維護                        *
***************************************************************************/
package crdm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Crdm0150 extends BaseEdit {
	CommString commString = new CommString();

	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q1")) {/* 轉換編列掛號畫面 */
			strAction = "Q1";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "S1")) {/* 編列掛號畫面 */
			strAction = "S1";
			dataProcess();
		}

		dddwSelect();
		initButton();
	}

	// ************************************************************************
	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = "" ;

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {

		wp.pageControl();
		
		if(wp.itemEmpty("ex_card_no")&&wp.itemEmpty("ex_mail_no")&&wp.itemEmpty("ex_apply_id"))
			if(wp.itemEmpty("ex_to_nccc_date1")&&(wp.itemEmpty("ex_to_nccc_date2"))){
				alertErr("製卡日起迄不可全部為空白");
				return;
			}
		
		switch(wp.itemStr("ex_card_type")) {
		case "0":
			queryRead1();
			break;
		case "C":
			queryRead2();
			break;
		case "V":
			queryRead3();
			break;
		case "P":
			queryRead4();
			break;
		}
		if(!strAction.equals("Q1")) {
			wp.sqlCmd += " ORDER BY A.MAIL_BRANCH,A.TO_NCCC_DATE,A.GROUP_CODE ";
		}
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " ORDER BY A.MAIL_BRANCH ";
			wp.pageRows = 999;
		}
		
	    wp.pageCountSql = "select count(*) as ct from (";
	    wp.pageCountSql += wp.sqlCmd;
	    wp.pageCountSql += ")";
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		
		if(strAction.equals("Q1")) {
			wp.colSet("ex_to_nccc_date1", wp.itemStr("ex_to_nccc_date1"));
			wp.colSet("ex_to_nccc_date2", wp.itemStr("ex_to_nccc_date2"));
			wp.colSet("ex_card_no", wp.itemStr("ex_card_no"));
			wp.colSet("ex_mail_no", wp.itemStr("ex_mail_no"));
		}
		wp.setPageValue();
		
	}
	
	void queryRead1() {
		if("Q1".equals(strAction)) {
			wp.sqlCmd = "select A.MAIL_BRANCH,A.FULL_CHI_NAME,sum(A.data_cnt) as data_cnt, ";
			wp.sqlCmd += " SUBSTR(xmlserialize(xmlagg(xmltext(CONCAT( ', ',A.card_type))) as VARCHAR(20)), 3) AS card_type from ( ";
		}else {
			wp.sqlCmd = "select A.* from ( ";
		}
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " SELECT '信用卡' as card_type, A.MAIL_BRANCH, B.FULL_CHI_NAME, count(*) as data_cnt ";
		}else {
			wp.sqlCmd += " SELECT A.batchno,A.recno,A.TO_NCCC_DATE,A.GROUP_CODE,A.CARD_NO,A.APPLY_ID,A.CHI_NAME,A.MAIL_BRANCH,B.FULL_CHI_NAME,A.MAIL_NO,'CRD_EMBOSS' as TABLE_NAME ";
		}
		wp.sqlCmd += " FROM CRD_EMBOSS A  join GEN_BRN B on A.MAIL_BRANCH = B.BRANCH ";
		wp.sqlCmd += " WHERE 1=1 AND A.MAIL_TYPE ='4' ";
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date1"), "A.TO_NCCC_DATE",">=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date2"), "A.TO_NCCC_DATE","<=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_branch"), "A.MAIL_BRANCH");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_card_no"), "A.CARD_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_no"), "A.MAIL_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_apply_id"), "A.APPLY_ID");
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " group by A.MAIL_BRANCH ,B.FULL_CHI_NAME ";
			wp.sqlCmd += " UNION ";
			wp.sqlCmd += " SELECT 'VD卡' as card_type, A.MAIL_BRANCH, B.FULL_CHI_NAME, count(*) as data_cnt ";
		}else {
			wp.sqlCmd += " UNION ";
			wp.sqlCmd += "SELECT A.batchno,A.recno,A.TO_NCCC_DATE,A.GROUP_CODE,A.CARD_NO,A.APPLY_ID,A.CHI_NAME,A.MAIL_BRANCH,B.FULL_CHI_NAME,A.MAIL_NO,'DBC_EMBOSS' as TABLE_NAME ";
		}
		
		wp.sqlCmd += " FROM DBC_EMBOSS A  join GEN_BRN B on A.MAIL_BRANCH = B.BRANCH ";
		wp.sqlCmd += " WHERE 1=1 AND A.MAIL_TYPE ='4' ";
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date1"), "A.TO_NCCC_DATE",">=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date2"), "A.TO_NCCC_DATE","<=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_branch"), "A.MAIL_BRANCH");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_card_no"), "A.CARD_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_no"), "A.MAIL_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_apply_id"), "A.APPLY_ID");
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " group by A.MAIL_BRANCH ,B.FULL_CHI_NAME ";
			wp.sqlCmd += " UNION ";
			wp.sqlCmd += " SELECT '貴賓卡' as card_type, A.MAIL_BRANCH, B.FULL_CHI_NAME, count(*) as data_cnt ";
		}else {
			wp.sqlCmd += " UNION ";
			wp.sqlCmd += "SELECT A.batchno,A.recno,A.TO_VENDOR_DATE AS TO_NCCC_DATE,A.GROUP_CODE,A.PP_CARD_NO AS CARD_NO,A.ID_NO AS APPLY_ID,C.CHI_NAME,A.MAIL_BRANCH,B.FULL_CHI_NAME,A.MAIL_NO,'CRD_EMBOSS_PP' as TABLE_NAME ";
		}
		
		wp.sqlCmd += " FROM CRD_EMBOSS_PP A  join GEN_BRN B on A.MAIL_BRANCH = B.BRANCH left join crd_idno C on A.id_no = C.id_no ";
		wp.sqlCmd += " WHERE 1=1 AND A.MAIL_TYPE ='4' ";
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date1"), "A.TO_VENDOR_DATE",">=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date2"), "A.TO_VENDOR_DATE","<=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_branch"), "A.MAIL_BRANCH");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_card_no"), "A.PP_CARD_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_no"), "A.MAIL_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_apply_id"), "A.ID_NO");
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " group by A.MAIL_BRANCH ,B.FULL_CHI_NAME ";
		}
		wp.sqlCmd += " ) A ";
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " group by A.MAIL_BRANCH ,A.FULL_CHI_NAME";
		}
	}
	
	void queryRead2() {
		if(strAction.equals("Q1")) {
			wp.sqlCmd = "SELECT '信用卡' as card_type, A.MAIL_BRANCH, B.FULL_CHI_NAME ,count(*) as data_cnt  ";
		}else{
			wp.sqlCmd = "SELECT hex(A.rowid) as rowid, A.batchno,A.recno,A.TO_NCCC_DATE,A.GROUP_CODE,A.CARD_NO,A.APPLY_ID,A.CHI_NAME,A.MAIL_BRANCH,B.FULL_CHI_NAME,A.MAIL_NO,'CRD_EMBOSS' as TABLE_NAME ";
		}		
		wp.sqlCmd += " FROM CRD_EMBOSS A  join GEN_BRN B on A.MAIL_BRANCH = B.BRANCH ";
		wp.sqlCmd += " WHERE 1=1 AND A.MAIL_TYPE ='4' ";
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date1"), "A.TO_NCCC_DATE",">=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date2"), "A.TO_NCCC_DATE","<=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_branch"), "A.MAIL_BRANCH");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_card_no"), "A.CARD_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_no"), "A.MAIL_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_apply_id"), "A.APPLY_ID");
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " group by A.MAIL_BRANCH ,B.FULL_CHI_NAME ";
		}
	}
	
	void queryRead3() {
		if(strAction.equals("Q1")) {
			wp.sqlCmd = "SELECT 'VD卡' as card_type, A.MAIL_BRANCH, B.FULL_CHI_NAME ,count(*) as data_cnt ";
		}else {
			wp.sqlCmd = "SELECT hex(A.rowid) as rowid, A.batchno,A.recno,A.TO_NCCC_DATE,A.GROUP_CODE,A.CARD_NO,A.APPLY_ID,A.CHI_NAME,A.MAIL_BRANCH,B.FULL_CHI_NAME,A.MAIL_NO,'DBC_EMBOSS' as TABLE_NAME ";
		}
		wp.sqlCmd += " FROM DBC_EMBOSS A  join GEN_BRN B on A.MAIL_BRANCH = B.BRANCH ";
		wp.sqlCmd += " WHERE 1=1 AND A.MAIL_TYPE ='4' ";
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date1"), "A.TO_NCCC_DATE",">=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date2"), "A.TO_NCCC_DATE","<=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_branch"), "A.MAIL_BRANCH");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_card_no"), "A.CARD_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_no"), "A.MAIL_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_apply_id"), "A.APPLY_ID");
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " group by A.MAIL_BRANCH ,B.FULL_CHI_NAME ";
		}
	}
	
	void queryRead4() {
		wp.sqlCmd = "select A.* from ( ";
		if(strAction.equals("Q1")) {
			wp.sqlCmd += "SELECT '貴賓卡' as card_type, A.MAIL_BRANCH, B.FULL_CHI_NAME ,count(*) as data_cnt  ";
		}else{
			wp.sqlCmd += "SELECT hex(A.rowid) as rowid, A.batchno,A.recno,A.TO_VENDOR_DATE AS TO_NCCC_DATE,A.GROUP_CODE,A.PP_CARD_NO AS CARD_NO,A.ID_NO AS APPLY_ID,C.CHI_NAME,A.MAIL_BRANCH,B.FULL_CHI_NAME,A.MAIL_NO,'CRD_EMBOSS_PP' as TABLE_NAME ";
		}		
		wp.sqlCmd += " FROM CRD_EMBOSS_PP A  join GEN_BRN B on A.MAIL_BRANCH = B.BRANCH left join crd_idno C on A.id_no = C.id_no ";
		wp.sqlCmd += " WHERE 1=1 AND A.MAIL_TYPE ='4' ";
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date1"), "A.TO_VENDOR_DATE",">=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_to_nccc_date2"), "A.TO_VENDOR_DATE","<=");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_branch"), "A.MAIL_BRANCH");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_card_no"), "A.PP_CARD_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_mail_no"), "A.MAIL_NO");
		wp.sqlCmd += sqlCol(wp.itemStr("ex_apply_id"), "A.ID_NO");
		if(strAction.equals("Q1")) {
			wp.sqlCmd += " group by A.MAIL_BRANCH ,B.FULL_CHI_NAME ";
		}
		wp.sqlCmd += " ) A where 1=1 ";
	}
	
	int getUsedMaxMailNo(String[] opt , String[] cnt) {
		int dataCnt = 0;
		String sqlStr = "SELECT USED_MAX_MAIL_NO,MAX_MAIL_NO FROM CRD_MAILNO_RANGE WHERE MAIL_TYPE = '4' AND INUSE_FLAG = 'Y' ";
		sqlSelect(sqlStr);

		if(sqlRowNum <= 0) {
			alertErr("無設定使用中的掛號號碼區間");
			return -1;
		}
		
		int usedMaxMailNo = sqlInt("USED_MAX_MAIL_NO");
		int maxMailNo = sqlInt("MAX_MAIL_NO");
		
		for (int rr = 0; rr < cnt.length; rr++) {
			if (checkBoxOptOn(rr, opt)) {
				continue;
			}
			dataCnt += commString.strToInt(cnt[rr]);
		}
		
		if((usedMaxMailNo+dataCnt)>maxMailNo) {
			alertErr("待編列的掛號號碼數量超出使用中的掛號號碼區間");
			return -1;
		}

		return usedMaxMailNo;
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		String kkBatchno = itemKk("data_k1"); 
		String kkRecno = itemKk("data_k2"); 
		String kkTableName = itemKk("data_k3"); 
		
		if(kkTableName.equals("CRD_EMBOSS")) {
			queryRead2();
		}
		
		if(kkTableName.equals("DBC_EMBOSS")) {
			queryRead3();
		}
		
		if(kkTableName.equals("CRD_EMBOSS_PP")) {
			queryRead4();
		}
		wp.sqlCmd += sqlCol(kkBatchno, "A.batchno");
		wp.sqlCmd += sqlCol(kkRecno, "A.recno");


		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料");
			return;
		}
	}


	// ************************************************************************
	public void saveFunc() throws Exception {
		crdm01.Crdm0150Func func = new crdm01.Crdm0150Func(wp);

		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
	}
	
	void dataProcess() {
		crdm01.Crdm0150Func func = new crdm01.Crdm0150Func(wp);
		String[] aaCardType = wp.itemBuff("card_type");
	    String[] aaMailBranch = wp.itemBuff("mail_branch");
	    String[] aaDataCnt = wp.itemBuff("data_cnt");
	    String[] aaOpt = wp.itemBuff("opt");
	    String exToNcccDate1 = wp.itemStr("ex_to_nccc_date1");
	    String exToNcccDate2 = wp.itemStr("ex_to_nccc_date2");
	    String exCardNo = wp.itemStr("ex_card_no");
	    String exMailNo = wp.itemStr("ex_mail_no");

	    int aaMaxUsedMailNo = getUsedMaxMailNo(aaOpt,aaDataCnt);
	  
	    wp.listCount[0] = aaCardType.length;
	    
	    if(aaMaxUsedMailNo<=0) {
	    	return;
	    }
	    
	    func.varsSet("ex_to_nccc_date1", exToNcccDate1);
	    func.varsSet("ex_to_nccc_date2", exToNcccDate2);
	    func.varsSet("ex_card_no", exCardNo);
	    func.varsSet("ex_mail_no", exMailNo);
	    
	    for (int rr = 0; rr < aaCardType.length; rr++) {
	        if (checkBoxOptOn(rr, aaOpt)) {
	    	    continue;
	    	}
	        aaMaxUsedMailNo++;
			func.varsSet("aa_card_type", aaCardType[rr]);
			func.varsSet("aa_mail_branch", aaMailBranch[rr]);
			func.varsSet("aa_mail_no", String.format("%06d", aaMaxUsedMailNo));
			rc = func.dbUpdate2();
			if (rc != 1) {
				wp.colSet(rr, "ok_flag", "!");
				wp.colSet(rr,"process_result", func.getMsg());
				sqlCommit(rc);
				continue;
			}
			wp.colSet(rr,"ok_flag", "V");
			wp.colSet(rr,"process_result", "編列成功");
			sqlCommit(rc);
		}
	    func.varsSet("aa_mail_no", String.format("%06d", aaMaxUsedMailNo));
	 	rc = func.dbUpdateCrdMailnoRange();	
	    sqlCommit(rc);
	    errmsg("編列掛號號碼完成");
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {
		try {
		      wp.initOption = "--";
		      wp.optionKey = wp.colStr("ex_mail_branch");
		      this.dddwList("dddw_mail_branch", "gen_brn", "branch", "brief_chi_name",
		          "where 1=1  order by branch");
		      
		} catch (Exception e) {
		}

	}

	// ************************************************************************
	@Override
	public void initPage() {
		return;
	}
	// ************************************************************************
	
	public String sqlCol(String strName, String col, String cond1) {
		StringBuffer strBuf = new StringBuffer();  
		String sqlStr = "";
		if(!commString.empty(strName) && !commString.empty(col)) {
			if(commString.empty(cond1)) {
				cond1 = "=";
			}
			strBuf.append(" and ");
			strBuf.append(col);
			strBuf.append(" ");
			strBuf.append(cond1);
			strBuf.append(" '");
			strBuf.append(strName);
			strBuf.append("' ");
			sqlStr = strBuf.toString();
		}	
		return sqlStr;
	}
	
	public String sqlCol(String strName, String col) {
		StringBuffer strBuf = new StringBuffer();  
		String sqlStr = "";
		if(!commString.empty(strName) && !commString.empty(col)) {
			strBuf.append(" and ");
			strBuf.append(col);
			strBuf.append(" =");
			strBuf.append(" '");
			strBuf.append(strName);
			strBuf.append("' ");
			sqlStr = strBuf.toString();
		}	
		return sqlStr;
	}
} // End of class
