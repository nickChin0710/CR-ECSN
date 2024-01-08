/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-01-10  V1.00.01  machao     新增功能：合庫金控子公司參數維護作業                    
* 111-03-22  V1.00.02  machao     页面bug处理  
* 111-06-30  V1.00.03  machao     页面bug处理                         
* 112-06-27  V1.00.04  machao     覆核bug处理                         **
******************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktm0530 extends BaseEdit{
	
	  private  String PROGNAME = "合庫金控子公司參數維護處理程式111/01/10 V1.00.01";
	  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	  busi.ecs.CommRoutine comr = null;
	  mktm01.Mktm0530Func func = null;
	  String kk1;
	  String org_tab_name = "mkt_office_m";
	  String control_tab_name = "";
	  int qFrom=0;
	  String tran_seqStr = "";
	  String   batch_no     = "";
	  int error_cnt=0,rec_cnt=0,notify_cnt=0;
	  int[]  datachk_cnt = {0,0,0,0,0,0,0,0,0,0};
	  String[] uploadFileCol= new String[50];
	  String[] uploadFileDat= new String[50];
	  String[] logMsg       = new String[20];
	
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
	              + sqlCol(wp.itemStr("ex_office_m_code"), "a.office_m_code", "like%")
	              ;

	  //-page control-
	  wp.queryWhere = wp.whereStr;
	  wp.setQueryMode();

	  queryRead();
		
	}

	@Override
	public void queryRead() throws Exception {
		if (wp.colStr("org_tab_name").length()>0) {
			 control_tab_name = wp.colStr("org_tab_name");
		}else{
			control_tab_name = org_tab_name;
		}
		wp.pageControl();
		wp.selectSQL = " "
				 + "hex(a.rowid) as rowid, "
	             + "nvl(a.mod_seqno,0) as mod_seqno, "
	             + "a.office_m_code,"
	             + "a.office_m_name,"
	             + "a.corp_no,"
	             + "a.effc_date_beg,"
	             + "a.effc_date_end,"
	             + "a.crt_date,"
	             + "a.crt_user";           
		  wp.daoTable = control_tab_name + " a "		              ;
		  wp.whereOrder = " "
		                + " order by a.office_m_code"
		                ;
		  pageQuery();
		  wp.setListCount(1);
		  if (sqlNotFind())
		     {
			  alertErr(appMsg.errCondNodata);
		      return;
		     }
		  //list_wkdata();
		  wp.setPageValue();	
	}

	@Override
	public void querySelect() throws Exception {
		kk1 = itemKk("data_k1");
		  qFrom=1;
		  dataRead();
		
	}

	@Override
	public void dataRead() throws Exception {
		if (qFrom==0)
			  if (wp.itemStr("ex_office_m_code").length()==0)
			     { 
			      alertErr("查詢鍵必須輸入");
			      return; 
			     } 
			  if (control_tab_name.length()==0)
			     {
			      if (wp.colStr("control_tab_name").length()==0)
			         control_tab_name=org_tab_name;
			      else
			         control_tab_name=wp.colStr("control_tab_name");
			     }
			  else
			     {
			      if (wp.colStr("control_tab_name").length()!=0)
			         control_tab_name=wp.colStr("control_tab_name");
			     }
			  wp.selectSQL = "hex(a.rowid) as rowid,"
			               + " nvl(a.mod_seqno,0) as mod_seqno, "
			               + "a.office_m_code as office_m_code,"
			               + "a.office_m_name,"
			               + "a.corp_no,"
			               + "a.effc_date_beg,"
			               + "a.effc_date_end,"
			               + "a.crt_date,"
			               + "a.crt_user,"
			               + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
			               + "a.mod_user,"
			               + "a.apr_date,"
			               + "a.apr_user";

			  wp.daoTable = control_tab_name + " a "
			              ;
			  wp.whereStr = "where 1=1 ";
			  if (qFrom==0)
			     {
			       wp.whereStr = wp.whereStr
			                   + sqlCol(wp.itemStr2("ex_office_m_code"), "a.office_m_code")
			                   ;
			     }
			  else if (qFrom==1)
			     {
			       wp.whereStr = wp.whereStr
			                   +  sqlRowId(kk1, "a.rowid")
			                   ;
			     }

			  pageSelect();
			  if (sqlNotFind())
			     {
				  alertErr("查無資料, key= "+"["+ kk1+"]");
			      return;
			     }
//			  commApruser("apr_user");
			  check_button_off();		
		
	}

	private void check_button_off() {
		return;
		
	}

	@Override
	public void saveFunc() throws Exception {
		if (!checkApprove(wp.itemStr2("approval_user"), wp.itemStr2("approval_passwd"))) return;

		  mktm01.Mktm0530Func func =new mktm01.Mktm0530Func(wp);

		  rc = func.dbSave(strAction);
		  if (rc!=1) 
			  alertErr2(func.getMsg());
		  log(func.getMsg());
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

}
