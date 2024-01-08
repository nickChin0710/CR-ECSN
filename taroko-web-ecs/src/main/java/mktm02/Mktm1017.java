/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-05-30  V1.00.00  machao     Initial        
* 112-06-27  V1.00.01  machao     删除bug调整                        *
******************************************************************************/
package mktm02;

import ofcapp.BaseAction;

public class Mktm1017 extends BaseAction {
	String idPseqno = "";

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
	      // -資料讀取-
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
	      /* 瀏覽功能 :skip-page */
	      queryRead();
	    } else if (eqIgno(wp.buttonCode, "S")) {
	      /* 動態查詢 */
	      querySelect();
	    } else if (eqIgno(wp.buttonCode, "L")) {
	      /* 清畫面 */
	      strAction = "";
	      clearFunc();
	    } else if (eqIgno(wp.buttonCode, "C")) {
	      // -資料處理-
	    	procAppr();
	    } else if (eqIgno(wp.buttonCode, "A2")) {
	      // -新增明細-
	      procFunc();
	    }
	  }


	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queryFunc() throws Exception {
		// TODO Auto-generated method stub
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		// TODO Auto-generated method stub
		 wp.pageControl();

		    wp.selectSQL = " aud_type" + ", id_no" + ", card_no" + ", create_date" + ", mod_user" ;
		    wp.daoTable = "bil_dodo_dtl_temp ";
		    wp.whereStr = "where 1=1 and data_from = '1' and send_date = '' ";
		    wp.whereStr += sqlCol(wp.itemStr("major_id"),"id_no");
		    wp.whereStr += sqlCol(wp.itemStr("ex_aud_type"),"aud_type");
		    wp.whereStr += sqlCol(wp.itemStr("ex_card_no"),"card_no");
		    wp.whereOrder = " order by id_no";

		    pageQuery();

		    wp.setListCount(1);
		    if (sqlNotFind()) {
		      alertErr(appMsg.errCondNodata);
		      return;
		    }

		    commfuncAudType("aud_type");
		    wp.listCount[1] = wp.dataCnt;
		    wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub
		dataCheck();
		
		 wp.pageRows = 999;
		 wp.selectSQL = " a.id_no, a.id_p_seqno, b.group_code, b.major_id_p_seqno, b.card_no, b.current_code, sup_flag ";
		 wp.daoTable = " crd_idno a, crd_card b";
		 wp.whereStr = " where a.id_p_seqno = b.id_p_seqno and b.current_code ='0' and b.major_id_p_seqno = ? ";
		 setString(idPseqno);
		 pageQuery();
		    if (sqlRowNum <= 0) {
		      errmsg("此條件查無資料");
		      return;
		    }
		    commfuncCurrCode("current_code");
		    commfuncSupFlag("sup_flag");
		    wp.setListCount(0);
	}
	
	public void dataCheck() {
		// TODO Auto-generated method stub
	    idPseqno = SelctCrdIdno(wp.itemStr("major_id"));
		if(empty(idPseqno)) {
			alertErr("ID不存在");
			return;
		}else {
			String majorIdPSeqno = SelectCrdCard(idPseqno);
			if(!majorIdPSeqno.equals(idPseqno)) {
				alertErr("資料錯誤!!非正卡ID");
				return;
			}
		}
		
	}

	public String SelectCrdCard(String idPseqno) {
		// TODO Auto-generated method stub
		String strSql = "select major_id_p_seqno, id_p_seqno, card_no, current_code from crd_card where major_id_p_seqno= ?";
	    Object[] param1 = new Object[] {idPseqno};
	    sqlSelect(strSql, param1);
		return sqlStr("major_id_p_seqno");
	}


	public String SelctCrdIdno(String idNo) {
		// TODO Auto-generated method stub
		String strSql = "select id_p_seqno, id_no, chi_name from crd_idno where id_no= ?";
	    Object[] param1 = new Object[] {idNo};
	    sqlSelect(strSql, param1);
		return sqlStr("id_p_seqno");
	}



	@Override
	public void saveFunc() throws Exception {
	    mktm02.Mktm1017Func func = new mktm02.Mktm1017Func();
	    func.setConn(wp);
	    String[] lsIdNo = wp.itemBuff("id_no");
	    String[] lsAudType = wp.itemBuff("aud_type");
	    String[] lsCardNo = wp.itemBuff("card_no");
	    String[] aaOpt = wp.itemBuff("opt");
	    wp.listCount[0] = lsAudType.length;
		
		if(strAction.equals("A")) {
			if(wp.itemEmpty("ex_aud_type")) {
				alertErr("異動檔交易別必選其一");
				return;
			}
			rc = func.insertClass();
		    if (rc != 1) {
		      errmsg(func.getMsg());
		    }
		    queryRead();
		}
		
		if(strAction.equals("D")) {
			for(String str : aaOpt)
			if(empty(str)) {
				alertErr("至少要選取一筆");
				return;
			}
			
			for (int ii = 0; ii < lsCardNo.length; ii++) {
			      if (this.checkBoxOptOn(ii, aaOpt) == false)
			        continue;
			      func.varsSet("card_no", lsCardNo[ii]);
			      rc = func.deleteClass();
			      if (rc != 1) {
			        wp.colSet(ii, "ok_flag", "X");
			        dbRollback();
			        continue;
			      } else {
			        wp.colSet(ii, "ok_flag", "V");
			        sqlCommit(1);
			        continue;
			      }
			    }
			queryRead();
		}
		
	}

	public void procAppr() {
		wp.colSet("id_no",wp.itemStr("data_k1"));
		wp.colSet("ex_card_no",wp.itemStr("data_k2"));
		wp.colSet("card_selected", " || true ");
	}

	  void commfuncAudType(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"A", "D"};
		    String[] txt = {"新增", "删除"};

		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      wp.colSet(ii, "comm_func_" + cde1, "");
		      for (int inti = 0; inti < cde.length; inti++)
		        if (wp.colStr(ii, cde1).equals(cde[inti])) {
		          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
		          break;
		        }
		    }
		  }
//	  0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡
	  
	  void commfuncCurrCode(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"0","1", "2","3","4","5"};
		    String[] txt = {"正常", "一般停用", "掛失", "強停", "其他", "偽卡"};

		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      wp.colSet(ii, "comm_func_" + cde1, "");
		      for (int inti = 0; inti < cde.length; inti++)
		        if (wp.colStr(ii, cde1).equals(cde[inti])) {
		          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
		          break;
		        }
		    }
		  }
	  
	  void commfuncSupFlag(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"0", "1"};
		    String[] txt = {"正卡", "附卡"};

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
	public void initButton() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub
	}


	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub
		
	}

 
}
