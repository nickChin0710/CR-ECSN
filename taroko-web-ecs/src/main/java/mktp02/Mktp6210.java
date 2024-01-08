/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR        DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/11  V1.00.00   Machao          Initial                              *
* 112/03/13  V1.00.01   Zuwei Su      修改已覆核資料明細丟失                              *
* 112/03/14  V1.00.02   Zuwei Su      交易明細查詢邏輯修改                              *
***************************************************************************/
package mktp02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktp6210 extends BaseProc {

	String orgTabName = "MKT_CHANtype_PARM_T";
	String controlTabName = "";
	int qFrom = 0;
	String typeId ;
	String rowId;

    java.util.Map<String, String> txCodeSelMap = new java.util.HashMap<String, String>() {
        {
            put("1", "指定");
            put("2", "排除");
        }
    };
    java.util.Map<String, String> txDescTypMap = new java.util.HashMap<String, String>() {
        {
            put("A", "中文");
            put("B", "英文");
        }
    };
    
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
	      dataReadDataT();
	    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
	      strAction = "A";
	      dataProcess();
	    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
	      queryRead();
	    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
	      querySelect();
	    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
	      strAction = "";
	      clearFunc();
	    }

	    dddwSelect();
	    initButton();
		
	}
	
	private int getWhereStr() {
		 String lsDate1 = wp.itemStr("ex_crt_date1");
		 String lsDate2 = wp.itemStr("ex_crt_date2");

		 if (this.chkStrend(lsDate1, lsDate2) == false) {
		      alertErr2("[設定日期-起迄]  輸入錯誤");
		      return -1;
		 }
		 
		 wp.whereStr = " where 1=1 ";
		 if (empty(wp.itemStr("ex_channel_type_id")) == false) {
		      wp.whereStr += " and channel_type_id = :channel_type_id ";
		      setString("channel_type_id", wp.itemStr("ex_channel_type_id"));
		    }
		 if (empty(wp.itemStr("ex_crt_date1")) == false) {
		      wp.whereStr += " and crt_date >= :ex_crt_date1 ";
		      setString("ex_crt_date1", wp.itemStr("ex_crt_date1"));
		 }
		 if (empty(wp.itemStr("ex_crt_date2")) == false) {
		      wp.whereStr += " and crt_date <= :ex_crt_date2 ";
		      setString("ex_crt_date2", wp.itemStr("ex_crt_date2"));
		 }
		    
		return 1;
	}

	@Override
	public void queryFunc() throws Exception {
		wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();
	    queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		// TODO Auto-generated method stub
		 wp.pageControl();

         wp.selectSQL = " "
                 + "hex(rowid) as rowid, "
                 + " channel_type_id, "
                 + " channel_type_desc,  "
                 + " aud_type, "
                 + " crt_user,  "
                 + " crt_date ";
		    wp.daoTable = " mkt_chantype_parm_t ";
		    wp.whereOrder = "  ";
		    if (getWhereStr() != 1)
		      return;
		    pageQuery();
		    wp.setListCount(1);
		    if (sqlNotFind()) {
		      alertErr(appMsg.errCondNodata);
		      return;
		    }
		    
		    commfuncAudType("aud_type");
		    wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		typeId = itemKk("data_k2");
		qFrom = 1;
	    dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (qFrom == 0)
		      if (wp.colStr("channel_type_id").length() == 0) {
		        alertErr("查詢鍵必須輸入");
		        return;
		      }
		    if (controlTabName.length() == 0) {
		      if (wp.colStr("control_tab_name").length() == 0)
		        controlTabName = orgTabName;
		      else
		        controlTabName = wp.colStr("control_tab_name");
		    } else {
		      if (wp.colStr("control_tab_name").length() != 0)
		        controlTabName = wp.colStr("control_tab_name");
		    }
		wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " 
		        + "a.channel_type_id," + "a.crt_user," 
		        + "a.channel_type_desc ";

		    wp.daoTable = controlTabName + " a ";
		    wp.whereStr = "where 1=1 ";
		    wp.whereStr = wp.whereStr + sqlCol(typeId, "a.channel_type_id");

		    pageSelect();
		    if (sqlNotFind()) {
		      return;
		    }
		    String Cnt = SelectChTyDataT(typeId);
		    wp.setValue("channel_type_cnt", Cnt);
		    
		    SelectBerData(typeId);
		    String befCnt = SelectChTyData(typeId);
		    wp.setValue("bef_channel_type_cnt", befCnt);
		
	}
	
	public String SelectChTyData(String active) {
		String lsSql = " select count(*) as qua from mkt_chantype_data where channel_type_id = ? ";
	      sqlSelect(lsSql, new Object[]{active});
	      
		return sqlStr("qua");
	}
	
	void SelectBerData(String groupId) throws Exception {
		 controlTabName = "mkt_chantype_parm";
		    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
		        + "a.channel_type_id ," 
		        + "a.channel_type_desc as bef_channel_type_desc";

		    wp.daoTable = controlTabName + " a ";
		    wp.whereStr = "where 1=1 " + sqlCol(groupId, "a.channel_type_id");

		    pageSelect();
		    if (sqlNotFind()) {
		      wp.notFound = "";
		      return;
		    }
	}
	
	public String SelectChTyDataT(String TypeId) {
		String lsSql = " select count(*) as qua from mkt_chantype_data_t where channel_type_id = ? ";
	      sqlSelect(lsSql, new Object[]{TypeId});
	      
		return sqlStr("qua");
	}
	
	 void dataReadDataT() throws Exception {
			// TODO Auto-generated method stub
		 
			 wp.pageControl();
			 
			 String qua = SelectChTyDataT(itemKk("data_k1"));
			 wp.setValue("channel_type_cnt", qua);
			 
	        wp.selectSQL = "hex(a.rowid) as rowid, "
	                + "ROW_NUMBER()OVER() as ser_num, "
	                + "a.mod_seqno as mod_seqno, "
	                + "a.channel_type_id,"
	                + "a.txcode_sel,"
	                + "a.tx_desc_type,"
	                + "a.tx_desc_name,"
	                + "a.mccc_sel,"
	                + "a.mccc_code, "
	                + "b.mcc_remark as mccc_code_desc, "
	                + "a.mod_user as mod_user ";
	        wp.daoTable = "mkt_chantype_data_t " 
	                + " a left join cca_mcc_risk b on a.mccc_code =  b.mcc_code ";
	        wp.whereStr = "where 1=1" 
	                + " and a.channel_type_id = ? ";
	        wp.whereOrder = " order by a.txcode_sel,a.tx_desc_type,a.tx_desc_name,a.mccc_sel,a.mccc_code";

	        pageQuery(new Object[] {itemKk("data_k1")});
            wp.setListCount(1);
			 if (sqlNotFind()) {
			      alertErr(appMsg.errCondNodata);
			      return;
			 }

	        wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
	        for (int ii = 0; ii < wp.selectCnt; ii++) {
	            wp.colSet(ii, "comm_txcode_sel", txCodeSelMap.get(wp.colStr(ii, "txcode_sel")));
	            wp.colSet(ii, "comm_tx_desc_type", txDescTypMap.get(wp.colStr(ii, "tx_desc_type")));
	            wp.colSet(ii, "comm_mccc_sel", txCodeSelMap.get(wp.colStr(ii, "mccc_sel")));
	        }
			 wp.setPageValue();
		}
	 
	 void commfuncAudType(String cde1) {
		 if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"Y", "A", "U", "D"};
		    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      wp.colSet(ii, "comm_func_" + cde1, "");
		      for (int inti = 0; inti < cde.length; inti++)
		        if (wp.colStr(ii, cde1).equals(cde[inti])) {
		          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
		          break;
		        }
		    }
	}


	@Override
	public void dataProcess() throws Exception {
		// TODO Auto-generated method stub
		int ilOk = 0;
	    int ilErr = 0;
	    mktp02.Mktp6210Func func = new mktp02.Mktp6210Func(wp);

	    String[] lsTypeId = wp.itemBuff("channel_type_id");
	    String[] lsTypeDesc = wp.itemBuff("channel_type_desc");
	    String[] lsAudType = wp.itemBuff("aud_type");
	    String[] lsRowid = wp.itemBuff("rowid");
	    String[] opt = wp.itemBuff("opt");
	    wp.listCount[0] = lsAudType.length;

	    int rr = -1;
	    for (int ii = 0; ii < opt.length; ii++) {
	      rr = (int) (this.toNum(opt[ii])%20 - 1);
	      if (rr < 0)
	        continue;
	      wp.log("" + ii + "-ON." + lsRowid[rr]);

	      wp.colSet(rr, "ok_flag", "-");

	      func.varsSet("channel_type_id", lsTypeId[rr]);
	      func.varsSet("channel_type_desc", lsTypeDesc[rr]);
	      func.varsSet("rowid", lsRowid[rr]);
	      wp.itemSet("wprowid", lsRowid[rr]);
	      if (lsAudType[rr].equals("A")) {
	    	  rc = func.dbInsertA4();
		      if (rc == 1)
			     rc = func.dbInsertA4Bndata();
			  if (rc == 1)
			     rc = func.dbDeleteD4TBndata();
	      }else if (lsAudType[rr].equals("U")) {
	          rc = func.dbUpdateU4();
	          if (rc == 1)
	            rc = func.dbDeleteD4Bndata();
	          if (rc == 1)
	            rc = func.dbInsertA4Bndata();
	          if (rc == 1)
	            rc = func.dbDeleteD4TBndata();
	        } else if (lsAudType[rr].equals("D")) {
	          rc = func.dbDeleteD4();
	          if (rc == 1)
	            rc = func.dbDeleteD4Bndata();
	          if (rc == 1)
	            rc = func.dbDeleteD4TBndata();
	        }
	     
	      log(func.getMsg());
	      if (rc != 1)
	        alertErr2(func.getMsg());
	      if (rc == 1) {

	        wp.colSet(rr, "ok_flag", "V");
	        ilOk++;
	        func.dbDelete();
	        this.sqlCommit(rc);
	        continue;
	      }
	      ilErr++;
	      wp.colSet(rr, "ok_flag", "X");
	      this.sqlCommit(0);
	    }

	    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
	}

} // End of class
