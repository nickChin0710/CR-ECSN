/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/07/03  V1.00.01   machao                   Initial              *
***************************************************************************/
package mktp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktp0930 extends BaseProc{
	private final String PROGNAME = "數位帳戶VD卡活動回饋參數覆核112/07/03 V1.00.01";
	String orgTabName = "mkt_digacctvd_parm_t";
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
//	      dataReadActCode();
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

	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code");
		
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
		
	}

	@Override
	public void queryRead() throws Exception {
	      controlTabName = orgTabName;

			    wp.pageControl();

			    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
			    	+ "a.active_code," + "a.active_name,"  + "a.active_date_s," 
			    	+ "a.crt_user," + "a.crt_date";

			    wp.daoTable = controlTabName + " a ";
			    wp.whereOrder = " " + " order by active_code";
			    
			    pageQuery();
			    wp.setListCount(1);
			    int sun = wp.selectCnt;
			    if (sqlNotFind()) {
			      alertErr(appMsg.errCondNodata);
			      return;
			    }
			    commfuncAudType("aud_type");
			    commfuncfeedCycle("feedback_cycle");
			    wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		rowid = itemKk("data_k1");
	    qFrom = 1;
	    dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub
		if (qFrom == 0)
		      if (wp.colStr("active_code").length() == 0) {
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
		    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
		        + "a.crt_user," + "a.active_code as active_code,"
		        + "a.active_name,"  + "a.active_date_s,"
		        + "a.active_date_e," + "a.feedback_cycle," + "a.feedback_dd";

		    wp.daoTable = controlTabName + " a ";
		    wp.whereStr = "where 1=1 ";
		    if (qFrom == 0) {
		      wp.whereStr = wp.whereStr + sqlCol(groupId, "a.active_code");
		    } else if (qFrom == 1) {
		      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
		    }

		    pageSelect();
		    if (sqlNotFind()) {
		      return;
		    }
		    commfuncfeedCycle("feedback_cycle");
		    if (!wp.colStr("aud_type").equals("A")) dataReadR3R();
	}

	 public void dataReadR3R() throws Exception
	 {
	  wp.colSet("control_tab_name",controlTabName); 
	  controlTabName = "mkt_digacctvd_parm";
	  wp.selectSQL = "hex(a.rowid) as rowid,"
	               + " nvl(a.mod_seqno,0) as mod_seqno, "
	               + "a.active_code as active_code,"
	               + "a.crt_user as bef_crt_user,"
	               + "a.active_name as bef_active_name,"
	               + "a.active_date_s as bef_active_date_s,"
	               + "a.active_date_e as bef_active_date_e,"
	               + "a.feedback_cycle as bef_feedback_cycle,"
	               + "a.feedback_dd as bef_feedback_dd";

	  wp.daoTable = controlTabName + " a "
	              ;
	  wp.whereStr = "where 1=1 ";
	  wp.whereStr = wp.whereStr + sqlCol(wp.colStr("active_code"), "a.active_code");

	  pageSelect();
	  if (sqlNotFind())
	     {
	      wp.notFound ="";
	      return;
	     }
	  wp.colSet("control_tab_name",controlTabName); 

	  if (wp.respHtml.indexOf("_detl") > 0) 
	     wp.colSet("btnStore_disable","");   
	  commfuncAudType("aud_type");
	  commfuncfeedCycle("bef_feedback_cycle");
	    listWkdata();
	 }

     // ************************************************************************
     void listWkdata() throws Exception {
         if (!wp.colStr("active_name").equals(wp.colStr("bef_active_name")))
             wp.colSet("opt_active_name", "Y");

         if (!wp.colStr("active_date_s").equals(wp.colStr("bef_active_date_s")))
             wp.colSet("opt_active_date_s", "Y");

         if (!wp.colStr("active_date_e").equals(wp.colStr("bef_active_date_e")))
             wp.colSet("opt_active_date_e", "Y");

         if (!wp.colStr("feedback_cycle").equals(wp.colStr("bef_feedback_cycle")))
             wp.colSet("opt_feedback_cycle", "Y");
         commfuncfeedCycle("feedback_cycle");
         commfuncfeedCycle("bef_feedback_cycle");

         if (!wp.colStr("feedback_dd").equals(wp.colStr("bef_feedback_dd")))
             wp.colSet("opt_feedback_dd", "Y");

         if (wp.colStr("aud_type").equals("D")) {
             wp.colSet("active_name", "");
             wp.colSet("active_date_s", "");
             wp.colSet("active_date_e", "");
             wp.colSet("feedback_cycle", "");
             wp.colSet("feedback_dd", "");
         }
     }
	
	@Override
	public void dataProcess() throws Exception {
		// TODO Auto-generated method stub
		 	int ilOk = 0;
		    int ilErr = 0;
		    int ilAuth = 0;
		    String lsUser="";
		    mktp01.Mktp0930Func func = new mktp01.Mktp0930Func(wp);

		    String[] lsAudType = wp.itemBuff("aud_type");
		    String[] lsActiveCode = wp.itemBuff("active_code");
		    String[] lsActiveName = wp.itemBuff("active_name");
		    String[] lsCrtUser = wp.itemBuff("crt_user");
		    String[] lsRowid = wp.itemBuff("rowid");
		    String[] opt = wp.itemBuff("opt");
		    wp.listCount[0] = lsAudType.length;

		    int rr = -1;
		    wp.selectCnt = lsAudType.length;
		    for (int ii = 0; ii < opt.length; ii++) {
		        if (opt[ii].length()==0) {
		            continue;
		        }
		      rr = (int) (this.toNum(opt[ii])%20 - 1);
		        if (rr==-1) {
		            rr = 19;
		        }
		      if (rr < 0) {
		        continue;
		      }

		      wp.colSet(rr, "ok_flag", "-");
		      if (lsCrtUser[rr].equals(wp.loginUser)) {
		        ilAuth++;
		        wp.colSet(rr, "ok_flag", "F");
		        continue;
		      }
		      
		      lsUser=lsCrtUser[rr];
		      if (!apprBankUnit(lsUser,wp.loginUser))
		         {
		          ilAuth++;
		          wp.colSet(rr,"ok_flag","B");
		          continue;
		         }

		      func.varsSet("active_code", lsActiveCode[rr]);
		      func.varsSet("active_name", lsActiveName[rr]);
		      func.varsSet("rowid", lsRowid[rr]);
		      wp.itemSet("wprowid", lsRowid[rr]);
		      if(lsAudType[rr].equals("A")) {
	    		  rc = func.dbInsertA4();
	    	  } else if (lsAudType[rr].equals("U")) {
	    		  rc = func.dbUpdateU4();
	    	  } else if (lsAudType[rr].equals("D")) {
		          rc = func.dbDeleteD4();
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

		    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 權限問題=" + ilAuth);
	}
	 // ************************************************************************
	  @Override
	  public void dddwSelect() {
	    String lsSql = "";
	    try {
	      if ((wp.respHtml.equals("mktp0930"))) {
	        wp.initOption = "--";
	        wp.optionKey = "";
	        if (wp.colStr("ex_active_code").length() > 0) {
	          wp.optionKey = wp.colStr("ex_active_code");
	        }
	        this.dddwList("dddw_active_code", "mkt_digacctvd_parm_t", "trim(active_code)",
	            "trim(active_name)", " order by active_date_s desc");
	      }
	    } catch (Exception ex) {
	    }
	  }

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
	  // ************************************************************************
	  void commfuncfeedCycle(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"M"};
		    String[] txt = {"按月"};

		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      wp.colSet(ii, "comm_func_" + cde1, "");
		      for (int inti = 0; inti < cde.length; inti++)
		        if (wp.colStr(ii, cde1).equals(cde[inti])) {
		          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
		          break;
		        }
		    }
		  } 
}
