/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-07-10  V1.00.00  machao       合庫金控子公司推廣單位放行作業                            *
******************************************************************************/
package mktp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktp0540 extends BaseEdit{

	String Msg = null;
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		  rc = 1;

		  strAction = wp.buttonCode;
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
		            strAction = "A";
		            saveFunc();
		            break;
		        case "U":
		            /* 更新功能 */
		            strAction = "U";
		            saveFunc();
		            break;
		        case "D":
		            /* 刪除功能 */
		            strAction = "D";
		            saveFunc();
		            break;
		        case "R2":
		            /* 參數頁面 資料讀取 */
		            strAction = "R2";
		            dataRead();
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
		        	strAction = "S2";
		            saveFunc();
		            break;
		        case "L":
		            /* 清畫面 */
		            strAction = "";
		            clearFunc();
		            break;
		        default:
		            break;
		    }

		  dddwSelect();
		  initButton();
		
	}


	public void queryFunc() throws Exception {
	    getWhereStr();
	    // -page control-
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();
	    queryRead();
		
	}
	
	private boolean getWhereStr() throws Exception {

	    wp.whereStr = " where 1=1  and apr_flag <> 'Y' ";

	    if (empty(wp.itemStr("ex_corp_no")) == false) {
	      wp.whereStr += " and  corp_no = :corp_no ";
	      setString("corp_no", wp.itemStr("ex_corp_no"));
	    }

	    return true;
	  }

	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = " hex(rowid) as rowid, "
					+ "corp_no, " 
					+ "office_code, "
					+ "office_name, "
					+ "brokerage_flag, "
					+ "mod_user ";

		wp.daoTable = "mkt_office_d ";
		wp.whereOrder = "order by corp_no, office_code";
		getWhereStr();
		
		pageQuery();

		wp.setListCount(1);
		  if (sqlNotFind())
		     {
			  alertErr(appMsg.errCondNodata);
		      return;
		     }

		wp.setPageValue();
		
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public void dataProcess() throws Exception {
//
//
//	    String[] opt = wp.itemBuff("opt");
//	    String[] aaCorpNo = wp.itemBuff("corp_no");
//	    String[] aaOfficeCode = wp.itemBuff("office_code");
//	    String[] aaOfficeName = wp.itemBuff("office_name");
//	    String[] aaBrokerageFlag = wp.itemBuff("brokerage_flag");
//
//	    String dsSql = "", isSql = "";
//	    wp.listCount[0] = aaCorpNo.length;
//
//	    // save
//	    int rr = -1;
//	    int llOk = 0, llErr = 0;
//	    for (int ii = 0; ii < opt.length; ii++) {
//	      rr = (int) this.toNum(opt[ii]) - 1;
//	      if (rr < 0) {
//	        continue;
//	      }
//	      wp.colSet(rr, "ok_flag", "");
//
//	      if (sqlRowNum > 0) {
//	        // up
//	        busi.SqlPrepare spu = new SqlPrepare();
//	        spu.sql2Update("mkt_purc_gp");
//	        spu.ppstr("corp_no", aaCorpNo[rr]);
//	        spu.ppstr("office_code", aaOfficeCode[rr]);
//	        spu.ppstr("office_name", aaOfficeName[rr]);
//	        spu.ppstr("brokerage_flag", aaBrokerageFlag[rr]);
//	        spu.ppstr("apr_flag", "Y");
//	        spu.ppstr("apr_date", getSysDate());
//	        spu.ppstr("apr_user", wp.loginUser);
//	        spu.ppstr("mod_pgm", wp.modPgm());
//	        spu.addsql(", mod_seqno =nvl(mod_seqno,0)+1 ", "");
//	        spu.sql2Where(" where corp_no =? ", aaCorpNo[rr]);
//	        sqlExec(spu.sqlStmt(), spu.sqlParm());
//	        if (sqlRowNum <= 0) {
//	          wp.colSet(rr, "ok_flag", "X");
//	          wp.colSet(rr, "ls_errmsg", "up mkt_office_d err");
//	          llErr++;
//	          sqlCommit(0);
//	          continue;
//	        }
//
//	      } else {
//	        // insert
//	        busi.SqlPrepare spi = new SqlPrepare();
//	        spi.sql2Insert("mkt_purc_gp");
//	        spi.ppstr("corp_no", aaCorpNo[rr]);
//	        spi.ppstr("office_code", aaOfficeCode[rr]);
//	        spi.ppstr("office_name", aaOfficeName[rr]);
//	        spi.ppstr("brokerage_flag", aaBrokerageFlag[rr]);
//	        spi.ppstr("apr_flag", "Y");
//	        spi.ppstr("apr_date", getSysDate());
//	        spi.ppstr("apr_user", wp.loginUser);
//	        spi.ppstr("mod_pgm", wp.modPgm());
//	        spi.ppstr("mod_pgm", wp.modPgm());
//	        spi.ppnum("mod_seqno", 1);
//	        sqlExec(spi.sqlStmt(), spi.sqlParm());
//	        if (sqlRowNum <= 0) {
//	          wp.colSet(rr, "ok_flag", "X");
//	          wp.colSet(rr, "ls_errmsg", "insert mkt_office_d err");
//	          llErr++;
//	          sqlCommit(0);
//	          continue;
//
//	        }
//	      }
//	      wp.colSet(rr, "ok_flag", "V");
//	      llOk++;
//	      sqlCommit(1);
//	    }
//
//	      alertErr("執行處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr);
//
//	  }
	

	@Override
	  public void dddwSelect() {
	    try {
	      if ((wp.respHtml.equals("mktp0540"))) {
	        wp.initOption = "--";
	        wp.optionKey = "";
	        if (wp.colStr("ex_corp_no").length() > 0) {
	          wp.optionKey = wp.colStr("ex_corp_no");
	        }
			this.dddwList("dddw_mkt_office_m", "mkt_office_m", "corp_no", "office_m_name", 
					"where 1=1 order by corp_no,office_m_name");
	      }
	    } catch (Exception ex) {
	    }
	  }

	@Override
	public void saveFunc() throws Exception {
		int ll_err = 0;

		mktp01.Mktp0540Func func = new mktp01.Mktp0540Func(wp);
//		if (strAction.equals("S2")) {
//			String[] aa_rowid = wp.itemBuff("rowid");
//			String[] aa_corp_no = wp.itemBuff("corp_no");
//			String[] aa_office_name = wp.itemBuff("office_name");
//			String[] aa_opt = wp.itemBuff("opt");
//			wp.listCount[0] = aa_corp_no.length;
//			// -insert-
//			for (int ll = 0; ll < aa_corp_no.length; ll++) {
//				func.varsSet("aa_rowid", aa_rowid[ll]);
//				func.varsSet("aa_corp_no", aa_corp_no[ll]);
//				func.varsSet("aa_office_name", aa_office_name[ll]);
//				if (checkBoxOptOn(ll, aa_opt)) {
//					if (func.dbUpdate() !=1) {
//						ll_err++;
//						Msg = "放行失敗";
//						wp.colSet(ll, "ok_flag", "!");
//						break;
//					}
//				}				
//			}
//			if(ll_err>0){
//				sqlCommit(0);
//				alertMsg("資料存檔失敗,"+Msg);
//				return;
//			}
//			sqlCommit(1);
//			//queryFunc();
//			alertMsg("放行處理檔完成");
//		}
		rc = func.dbSave(strAction);
		  if (rc!=1) 
			  alertErr2(func.getMsg());
		  log(func.getMsg());
		  this.sqlCommit(rc);
	}


	@Override
	public void initButton() {
		// TODO Auto-generated method stub
		
	}
}
