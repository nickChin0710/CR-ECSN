package cmsm03;
/** 2019-0614:  JH    p_xxx >>acno_pxxx
 *  2020-0107:  Ru    modify AJAX
 *  2020-0207:  Ru    增加貴賓卡欄位
 ** 109-04-20  shiyuqi       updated for project coding standard     *
 ** 109-09-01  tanwei        查詢/更新中添加貴賓卡字段   
 ** 109-09-03  tanwei        添加字段 
 *  109-09-04  tanwei        ajax中添加貴賓卡字段 實現動態選擇
 *  109-09-07  tanwei        替換left join表連接方式                                                             *
 * * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Cmsm3140 extends BaseAction {
	String rowid = ""; 
	@Override
	public void userAction() throws Exception {
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
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
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
		} else if (eqIgno(wp.buttonCode, "C")) {
			//-資料處理-
			procFunc();
		} 
		//20200107 modify AJAX
		else if (eqIgno(wp.buttonCode, "AJAX")) {
			if ("1".equals(wp.getValue("ID_CODE"))) {
				wfAjaxWinid();
			}
		}

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		if(chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date1"))==false){
			errmsg("使用日期 起迄錯誤");
			return ;
		}
		
		String lsWhere = " where 1=1 "
							 +sqlCol(wp.itemStr("ex_date1"),"visit_date",">=")
							 +sqlCol(wp.itemStr("ex_date2"),"visit_date","<=")
							 +sqlCol(wp.itemStr("ex_bin_type"),"bin_type")
							 +sqlCol(wp.itemStr("ex_ppcard_no"),"pp_card_no")
							 +sqlCol(wp.itemStr("ex_idno"),"id_no")
							 +sqlCol(wp.itemStr("ex_vip_kind"),"vip_kind")
							 ;
						
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
   
		queryRead();

		
	}

	@Override
	public void queryRead() throws Exception {
		
		wp.selectSQL = ""
						 + " crt_date ,"
		                 + " vip_kind ,"
						 + " bin_type ,"
						 + " data_seqno ,"
						 + " from_type ,"
						 + " pp_card_no ,"
						 + " ch_ename ,"
						 + " visit_date ,"
						 + " ch_visits ,"
						 + " guests_count ,"
						 + " use_city ,"
						 + " id_no ,"
						 + " free_use_cnt ,"
						 + " ch_cost_amt ,"
						 + " guest_cost_amt ,"
						 + " crt_user ,"
						 + " hex(rowid) as rowid ,"
						 + " iso_conty ,"
						 + " card_no ,"
						 + " mcht_no , "
						 + " decode(from_type,'1','人工','2','批次') as tt_from_type "
						 ;
		wp.daoTable = " cms_ppcard_visit ";
		
		pageQuery();
		String vipKind = wp.colStr("vip_kind");
		if(sqlRowNum <= 0){
			alertErr2("此條件查無資料");
			return ;
		}else {
         for (int i=0; i<sqlRowNum; i++) {
            if ("1".equals(vipKind)) {
                wp.colSet(i,"vip_kind", "1_新貴通卡");
            } else if ("2".equals(vipKind)) {
                wp.colSet(i,"vip_kind", "2_龍騰卡");
            }
        }
    }
		
		wp.setListCount(0);
		wp.setPageValue();

	}

	@Override
	public void querySelect() throws Exception {
		rowid = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		
		if(empty(rowid))	rowid = wp.itemStr("rowid");
		wp.selectSQL = ""
						 + " crt_date ,"
						 + " vip_kind ,"
						 + " bin_type ,"
						 + " data_seqno ,"
						 + " from_type ,"
						 + " pp_card_no ,"
						 + " ch_ename ,"
						 + " visit_date ,"
						 + " iso_conty ,"
						 + " ch_visits ,"
						 + " guests_count ,"
						 + " use_city ,"
						 + " id_no ,"
						 + " uf_idno_name(id_p_seqno) as chi_name ,"
						 + " free_use_cnt ,"
						 + " ch_cost_amt ,"
						 + " guest_cost_amt ,"
						 + " card_no ,"
						 + " mcht_no ,"
						 + " user_remark ,"
						 + " crt_user ,"
						 + " mod_user ,"
						 + " mod_time ,"
						 + " mod_pgm ,"
						 + " mod_seqno ,"
						 + " hex(rowid) as rowid ,"
						 + " free_use_cnt as db_free_use ,"
						 + " (ch_visits - free_use_cnt) * ch_cost_amt as wk_ch_amt , "
						 + " guests_count * guest_cost_amt as wk_guest_amt , "
						 + " (ch_visits - free_use_cnt) * ch_cost_amt + guests_count * guest_cost_amt as wk_tot_amt , "
						 + " decode(from_type,'1','人工','2','批次') as tt_from_type "
						 ;
		
		wp.daoTable = " cms_ppcard_visit ";
		wp.whereStr = " where 1=1 "
						+commSqlStr.whereRowid(rowid)
						;
		pageSelect();
		String vipKind = wp.colStr("vip_kind");
		if(sqlRowNum<=0){
			alertErr2("查無資料");
			return ;
          } else { 
                if ("1".equals(vipKind)) { 
                  wp.colSet("vip_kind", "1_新貴通卡");
                  wp.colSet("kk_vip_kind", "1"); 
                } else if ("2".equals(vipKind)) {
                  wp.colSet("vip_kind", "2_龍騰卡"); 
                  wp.colSet("kk_vip_kind", "2"); 
                  } 
                }
             
	}

	@Override
	public void saveFunc() throws Exception {

		cmsm03.Cmsm3140Func func = new cmsm03.Cmsm3140Func();
		func.setConn(wp);
		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if(rc!=1){
			errmsg(func.getMsg());			
		}	else	this.saveAfter(false);

	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		if(eqIgno(wp.respHtml, "cmsm3140_detl")){
			this.btnModeAud();
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}
	
	//20200107 modify AJAX
	public void wfAjaxWinid() throws Exception {
		//super.wp = wr;
		
		//String ls_winid =
		selectSecWindow(wp.itemStr("ax_winid"));
		if (rc!=1) {
			wp.addJSON("bin_type","");
			wp.addJSON("vip_kind","");
			wp.addJSON("id_no","");
			wp.addJSON("id_p_seqno","");
			wp.addJSON("eng_name","");
			wp.addJSON("ch_cost_amt","");
			wp.addJSON("guest_cost_amt","");
			wp.addJSON("chi_name","");
			wp.addJSON("card_no","");
			return;
		}
		wp.addJSON("bin_type",sqlStr("bin_type"));
		wp.addJSON("vip_kind",sqlStr("vip_kind"));
		wp.addJSON("id_no",sqlStr("id_no"));
		wp.addJSON("id_p_seqno",sqlStr("id_p_seqno"));
		wp.addJSON("eng_name",sqlStr("eng_name"));
		wp.addJSON("ch_cost_amt",sqlStr("ch_cost_amt"));
		wp.addJSON("guest_cost_amt",sqlStr("guest_cost_amt"));
		wp.addJSON("chi_name",sqlStr("chi_name"));
		wp.addJSON("card_no",sqlStr("card_no"));
		
	}
	
	void selectSecWindow(String ppCardNo) throws Exception {
		
		wp.sqlCmd = " select "
			   	 + " A.bin_type , "
		         + " A.vip_kind , "
		         + " A.id_p_seqno , "
			   	 + " uf_idno_id(A.id_p_seqno) as id_no , "
			   	 + " uf_idno_name(A.id_p_seqno) as chi_name , "
			   	 + " A.eng_name , "
			   	 + " nvl(B.holder_amt,0) as ch_cost_amt , "
			   	 + " nvl(B.toget_amt,0) as guest_cost_amt "
			   	 + " from crd_card_pp A , mkt_ppcard_issue B "
			   	 + " where pp_card_no =:s1 "
			   	 + " and A.bin_type = B.bin_type "
			   	 +commSqlStr.rownum(1)
					 ;
		setString("s1",ppCardNo);
		sqlSelect();
		if (sqlRowNum<=0) {
			alertErr2("查無資料: PP_Card_No="+ppCardNo);
			return ;
		}
		
		wp.sqlCmd = " select "
					 + " A.card_no "
					 + " from crd_card A , mkt_ppcard_apply B "
					 + " where A.card_type = B.card_type "
					 + " and uf_nvl(A.group_code,'0000') = B.group_code "
					 + " and A.id_p_seqno in (select id_p_seqno from crd_idno where id_no =:id_no) "
					 + " order by A.current_code Asc , A.acct_type , A.oppost_date "
					 ;
		setString("id_no",sqlStr("id_no"));
		sqlSelect();
		if (sqlRowNum<=0) {
			alertErr2("查無資料: PP_Card_No="+ppCardNo);
		}
		
		return;
	}
	
}
