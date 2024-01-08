/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/02/28  V1.00.01   machao      Initial   
* 112/03/05  V1.00.02   machao      程式調整：bug修訂                          
* 112/03/08  V1.00.03   machao      程式調整：新增活動代碼筆數欄位                          *  *                           *
* 112/05/10  V1.00.04   Zuwei Su    程式調整：bug修訂                  *
* 112/05/11  V1.00.05   Zuwei Su    覆核應該為主管的帳號                  *
* 112-05-16  V1.00.12  Ryan           增一般名單產檔格式、回饋周期 的參數設定
* 112-06-06  V1.00.13   machao      活動群組增 ‘群組月累績最低消費金額’
***************************************************************************/
package mktp01;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktp0851 extends BaseProc{
	
	private final String PROGNAME = "行銷通路活動群組設定檔維護覆核112/03/08 V1.00.03";
	String orgTabName = "MKT_CHANNELGP_PARM_T";
	String controlTabName = "";
	int qFrom = 0;
	String rowid;
	String groupId;
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
	      dataReadActCode();
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

	public void dataReadActCode() throws Exception {
		wp.pageControl();

	    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
	    	+ "a.active_group_id," + "a.active_group_desc,"  + "b.active_code," + "b.feedback_seq," + "c.active_name";

	    wp.daoTable = " mkt_channelgp_parm_t  a " 
	    		+ " left join mkt_channelgp_data_t b on a.active_group_id = b.active_group_id "
	    		+ " left join mkt_channel_parm c on b.active_code = c.active_code";
	    wp.whereStr = "where 1=1 " + sqlCol(wp.colStr("active_group_id"), "a.active_group_id");
	    wp.whereOrder = " " + " order by feedback_seq";
	    
	    pageQuery();
	    
	    wp.setListCount(1);
	    
	    if (sqlNotFind()) {
	      alertErr(appMsg.errCondNodata);
	      return;
	    }
	    String qua = selectChGpDataT(wp.colStr("active_group_id"));
	    wp.setValue("active_code_cnt", qua);

	    wp.setPageValue();
		
	}

	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_group_id"), "a.active_group_id");
		
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		 if (wp.colStr("org_tab_name").length() > 0)
		      controlTabName = wp.colStr("org_tab_name");
		    else
		      controlTabName = orgTabName;

		    wp.pageControl();

		    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
                + "a.aud_type,"
		    	+ "a.active_group_id," + "a.active_group_desc,"  + "a.feedback_type,"  + "a.limit_amt," + "a.crt_user," + "a.crt_date";

		    wp.daoTable = controlTabName + " a ";
		    wp.whereOrder = " " + " order by active_group_id";
		    
		    pageQuery();
		    wp.setListCount(1);
		    if (sqlNotFind()) {
		      alertErr(appMsg.errCondNodata);
		      return;
		    }
		    commfuncAudType("aud_type");

		    wp.setPageValue();
			commFuncFeedbackType("feedback_type");
	}
	// ************************************************************************
	  void commfuncAudType(String s1)
	   {
	    if (s1==null || s1.trim().length()==0) return;
	    String[] cde = {"Y","A","U","D"};
	    String[] txt = {"未異動","新增待覆核","更新待覆核","刪除待覆核"};

	    for (int ii = 0; ii < wp.selectCnt; ii++)
	      {
	        wp.colSet(ii,"comm_func_"+s1, "");
	        for (int inti=0;inti<cde.length;inti++)
	           if (wp.colStr(ii,s1).equals(cde[inti]))
	              {
	               wp.colSet(ii,"commfunc_"+s1, txt[inti]);
	               break;
	              }
	      }
	   }

	public int selectChGpParm(String active) {
		String lsSql = " select count(*) as qua from mkt_channelgp_parm where active_group_id = ? ";
	      sqlSelect(lsSql, new Object[]{active});
	      
		return sqlInt("qua");
	}

	@Override
	public void querySelect() throws Exception {
		rowid = itemKk("data_k1");
	    qFrom = 1;
	    dataRead();
		
	}

	@Override
	public void dataRead() throws Exception {
		if (qFrom == 0)
		      if (wp.colStr("active_group_id").length() == 0) {
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
		        + "a.active_group_id as active_group_id," + "a.crt_user," 
		        + "a.active_group_desc," + "a.sum_amt," + "a.feedback_type," + "a.limit_amt";

		    wp.daoTable = controlTabName + " a ";
		    wp.whereStr = "where 1=1 ";
		    if (qFrom == 0) {
		      wp.whereStr = wp.whereStr + sqlCol(groupId, "a.active_group_id");
		    } else if (qFrom == 1) {
		      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
		    }

		    pageSelect();
		    if (sqlNotFind()) {
		      return;
		    }
			commFuncFeedbackType("feedback_type");
		    String Cnt = selectChGpDataT(wp.colStr("active_group_id"));
		    wp.setValue("active_code_cnt", Cnt);
//		    listWkdataAft();
            if (!wp.colStr("aud_type").equals("A")) {
                dataReadR3R();
            } else {
                commfuncAudType("aud_type");
                listWkdataSpace();
            }
    	}

        // ************************************************************************
        void listWkdata() throws Exception {
            if (!wp.colStr("active_group_desc").equals(wp.colStr("bef_active_group_desc")))
                wp.colSet("opt_active_group_desc", "Y");
            
            if (!wp.colStr("sum_amt").equals(wp.colStr("bef_sum_amt")))
                wp.colSet("opt_sum_amt", "Y");

            if (!wp.colStr("feedback_type").equals(wp.colStr("bef_feedback_type")))
                wp.colSet("opt_feedback_type", "Y");

            if (!wp.colStr("limit_amt").equals(wp.colStr("bef_limit_amt")))
                wp.colSet("opt_limit_amt", "Y");

            if (!wp.colStr("active_code_cnt").equals(wp.colStr("bef_active_code_cnt")))
                wp.colSet("opt_active_code_cnt", "Y");

            if (wp.colStr("aud_type").equals("D")) {
                wp.colSet("active_group_desc", "");
                wp.colSet("feedback_type", "");
                wp.colSet("limit_amt", "");
                wp.colSet("active_code_cnt", "");
            }
        }

        // ************************************************************************
        void listWkdataSpace() throws Exception {
            if (wp.colStr("active_group_desc").length() == 0)
                wp.colSet("opt_active_group_desc", "Y");
            
            if (wp.colStr("sum_amt").length() == 0)
                wp.colSet("opt_sum_amt", "Y");

            if (wp.colStr("feedback_type").length() == 0)
                wp.colSet("opt_feedback_type", "Y");

            if (wp.colStr("limit_amt").length() == 0)
                wp.colSet("opt_limit_amt", "Y");

            if (wp.colStr("active_code_cnt").length() == 0)
                wp.colSet("opt_active_code_cnt", "Y");
        }

        // ************************************************************************
        public void dataReadR3R() throws Exception {
            wp.colSet("control_tab_name", controlTabName);
            controlTabName = "MKT_CHANNELGP_PARM";
            wp.selectSQL = "hex(a.rowid) as rowid,"
                    + " nvl(a.mod_seqno,0) as mod_seqno, "
                    + "a.active_group_id as bef_active_group_id,"
                    + "a.active_group_desc as bef_active_group_desc,"
                    + "a.sum_amt as bef_sum_amt,"
                    + "a.feedback_type as bef_feedback_type,"
                    + "a.limit_amt as bef_limit_amt";

            wp.daoTable = controlTabName + " a ";
            wp.whereStr = "where 1=1 " + sqlCol(wp.colStr("active_group_id"), "a.active_group_id");

            pageSelect();
            if (sqlNotFind()) {
                wp.notFound = "";
                return;
            }
            String befCnt = selectChGpData(wp.colStr("active_group_id"));
            wp.setValue("bef_active_code_cnt", befCnt);
            commFuncFeedbackType("bef_feedback_type");
            wp.colSet("control_tab_name", controlTabName);
//            checkButtonOff();
            commfuncAudType("aud_type");
            listWkdata();
        }

	public String selectChGpDataT(String active) {
		String lsSql = " select count(*) as qua from mkt_channelgp_data_t where active_group_id = ? ";
	      sqlSelect(lsSql, new Object[]{active});
	      
		return sqlStr("qua");
	}
	
	public String selectChGpData(String active) {
		String lsSql = " select count(*) as qua from mkt_channelgp_data where active_group_id = ? ";
	      sqlSelect(lsSql, new Object[]{active});
	      
		return sqlStr("qua");
	}

	@Override
	public void dataProcess() throws Exception {
	    int ilOk = 0;
	    int ilErr = 0;
	    int ilAuth = 0;
	    String lsUser="";
	    mktp01.Mktp0851Func func = new mktp01.Mktp0851Func(wp);

	    String[] lsActiveGroupId = wp.itemBuff("active_group_id");
	    String[] lsActiveGroupDesc = wp.itemBuff("active_group_desc");
	    String[] lsFeedbackType = wp.itemBuff("feedback_type");
	    String[] lsLimitAmt = wp.itemBuff("limit_amt");
	    String[] lsAudType  = wp.itemBuff("aud_type");
	    String[] lsCrtUser = wp.itemBuff("crt_user");
	    String[] lsRowid = wp.itemBuff("rowid");
	    String[] opt = wp.itemBuff("opt");
	    
	    wp.listCount[0] = lsAudType.length;
	    int rr = -1;
	    wp.selectCnt = lsAudType.length;
	    for (int ii = 0; ii < opt.length; ii++) {
	      rr = (int) (this.toNum(opt[ii]) - 1);
	      if (rr < 0)
	        continue;
	      wp.log("" + ii + "-ON." + lsRowid[rr]);

	      wp.colSet(rr, "ok_flag", "-");
          if (lsCrtUser[rr].equals(wp.loginUser)) {
              ilAuth++;
              wp.colSet(rr, "ok_flag", "F");
              continue;
          }

          lsUser = lsCrtUser[rr];
          if (!apprBankUnit(lsUser, wp.loginUser)) {
              ilAuth++;
              wp.colSet(rr, "ok_flag", "B");
              continue;
          }

	      func.varsSet("active_group_id", lsActiveGroupId[rr]);
	      func.varsSet("feedback_type", lsFeedbackType[rr]);
	      func.varsSet("limit_amt", lsLimitAmt[rr]);
	      func.varsSet("active_group_desc", lsActiveGroupDesc[rr]);
	      func.varsSet("rowid", lsRowid[rr]);
	      wp.itemSet("wprowid", lsRowid[rr]);
          if (lsAudType[rr].equals("A")) {
              rc = func.dbInsertA4();
              if (rc == 1)
                  rc = func.dbInsertA4Bndata();
              if (rc == 1)
                  rc = func.dbDeleteD4TBndata();
          } else if (lsAudType[rr].equals("U")) {
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
              alertErr(func.getMsg());
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

	    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 權限問題=" + ilAuth);
		
	}
	 @Override
	  public void initButton() {
	    if (wp.respHtml.indexOf("_detl") > 0) {
	      this.btnModeAud();
	    }
	  }
	 
	// ************************************************************************
	void commFuncFeedbackType(String cde1) {
		if (cde1 == null || cde1.trim().length() == 0)
			return;
		String[] cde = {"1", "2","3"};
		String[] txt = {"擇優回饋", "回饋上限總金額","擇一回饋"};
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wp.colSet(ii, "comm_" + cde1, "");
			for (int inti = 0; inti < cde.length; inti++)
				if (wp.colStr(ii, cde1).equals(cde[inti])) {
					wp.colSet(ii, "comm_" + cde1, txt[inti]);
					break;
				}
		}
	}
}
