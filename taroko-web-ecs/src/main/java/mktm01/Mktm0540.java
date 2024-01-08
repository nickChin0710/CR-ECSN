/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-07-05  V1.00.01  machao     新增功能：合庫金控子公司推廣單位維護處理程式              *                         *
* 111-12-14  V1.00.02  Zuwei Su   增加mod_user和mod_time輸出，帶條件查詢異常，重新選取機構代號,按下讀取,出現error，頁面調整             *                         *
* 111-12-19  V1.00.03  Zuwei Su   新增存檔,其中APR_FLAG=N, APR_DATE=空白、 APR_USER=空白             *                         *
******************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm0540 extends BaseEdit{
	private  String PROGNAME = "合庫金控子公司推廣單位維護處理程式111-12-19 V1.00.03";
	  String rowid;
	  String org_tab_name = "mkt_office_d";
	  int qFrom=0;

	@Override
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

	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = "WHERE 1=1 "
	              + sqlCol(wp.itemStr("ex_corp_no"), "a.corp_no", "like%")
	              ;
		if(!wp.itemEmpty("ex_office_code")) {
			wp.whereStr += sqlCol(wp.itemStr("ex_office_code"), "office_code");
		}
	  //-page control-
	  wp.queryWhere = wp.whereStr;
	  wp.setQueryMode();

	  queryRead();
		
	}

	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = " hex(rowid) as rowid, "
					+ "office_m_code, "
					+ "corp_no, " 
					+ "office_code, "
					+ "office_name, "
					+ "apr_date, "
					+ "apr_user ";

		wp.daoTable = "mkt_office_d a";
		wp.whereOrder = "order by office_m_code";
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
		rowid = itemKk("data_k1");
		  qFrom=1;
		  dataRead();
		
	}

	@Override
	public void dataRead() throws Exception {
		String corpNo = wp.itemStr2("ex_corp_no");

			  wp.selectSQL = "hex(a.rowid) as rowid,"
			               + " nvl(a.mod_seqno,0) as mod_seqno, "
			               + "a.office_m_code,"
			               + "a.corp_no,"
			               + "a.office_code,"
			               + "a.office_name,"
			               + "a.brokerage_flag,"
			               + "a.apr_flag,"
			               + "a.crt_date,"
			               + "a.crt_user,"
                           + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
                           + "a.mod_user,"
			               + "a.apr_date,"
			               + "a.apr_user";

			  wp.daoTable = org_tab_name + " a "
			              ;
			  wp.whereStr = "WHERE 1=1 ";
			  if (qFrom == 0) {
			      wp.whereStr = wp.whereStr + sqlCol(corpNo, "a.corp_no");
			    } else if (qFrom == 1) {
			      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
			    }			

			  pageSelect();
			  if (sqlNotFind())
			     {
			      if (qFrom == 0) {
			          alertErr("查無資料, key= "+"["+ corpNo+"]");
			      } else if (qFrom == 1) {
			          alertErr("查無資料, key= "+"["+ rowid+"]");
			      }
			      return;
			     }
			  wp.colSet("ex_corp_no", wp.colStr("corp_no"));
		
	}

	@Override
	public void saveFunc() throws Exception {
//		if (!checkApprove(wp.itemStr2("zz_apr_user"), wp.itemStr2("zz_apr_passwd"))) return;

		  mktm01.Mktm0540Func func =new mktm01.Mktm0540Func(wp);

		  rc = func.dbSave(strAction);
		  if (rc!=1) 
			  alertErr2(func.getMsg());
		  log(func.getMsg());
		  //alertErr("放行處理檔完成");
		  this.sqlCommit(rc);
		
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0)
	     {
	      this.btnModeAud();
	     }
		
	}
	public void initPage()
	 {
	  return;
	 }

	
	@Override
	  public void dddwSelect() {
	    try {
	      if ((wp.respHtml.equals("mktm0540")) || (wp.respHtml.equals("mktm0540_detl"))) {
	        wp.initOption = "--";
	        wp.optionKey = "";
	        if (wp.colStr("ex_corp_no").length() > 0) {
	          wp.optionKey = wp.colStr("ex_corp_no");
	        }
			this.dddwList("dddw_mkt_office_m", "mkt_office_m", "corp_no", "office_m_name", 
					"where 1=1 order by corp_no");
	      }
	    } catch (Exception ex) {
	    }
	  }

}
