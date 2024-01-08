/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 112-01-16  V1.00.00  Machao      program initial                          *
 * 112-01-19  V1.00.01  Sunny       從colm05搬到colm01  
 * 112-01-30  V1.00.02  Machao      若curr_code空白設值為‘901’              *
 * 112-02-28  V1.00.03  Zuwei Su    查無資料--sql不正確                       *
 ******************************************************************************/
package colm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Colr6010 extends BaseAction implements InfacePdf{

	@Override
	public void userAction() throws Exception {
		 if (eqIgno(wp.buttonCode, "X")) {
		      /* 轉換顯示畫面 */
		      // is_action="new";
		      // clearFunc();
		    } else if (eqIgno(wp.buttonCode, "Q")) {
		      /* 查詢功能 */
		      strAction = "Q";
		      queryFunc();
		    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
		      strAction = "R";
		      // dataRead();
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
		    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
		      strAction = "XLS";
		      // xlsPrint();
		    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
		      strAction = "PDF";
		      pdfPrint();
		    }

		    dddwSelect();
		    initButton();
		
	}

	@Override
	public void dddwSelect() {
		try {
		      if (eqIgno(wp.respHtml, "colr6010")) {
		          wp.optionKey = wp.colStr(0, "ex_acct_type");
		          dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
//		         --------------------------------------------------- 
		          wp.optionKey = wp.colStr(0, "ex_curr_code");
		          dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
		      }

		    } catch (Exception ex) {
		    }
		
	}

	@Override
	public void queryFunc() throws Exception {
		
		if (!getWhereStr()) {
            return;
        }
		
		String lsWhere = " where 1=1 "
		        + sqlCol(wp.itemStr("ex_crt_date_s"), "col_cs_cslog.crt_date", ">=")
		        + sqlCol(wp.itemStr("ex_crt_date_e"), "col_cs_cslog.crt_date", "<=")
                + sqlCol(wp.itemStr("ex_acct_type"), "col_cs_cslog.acct_type");
		if (!wp.itemEmpty("ex_id_p_seqno")) {
		    lsWhere += " and exists(select 1 from crd_idno where crd_idno.id_p_seqno = col_cs_cslog.id_p_seqno " 
		            + sqlCol(wp.itemStr("ex_id_p_seqno"), "crd_idno.id_no") 
		            + ")";
		}
		if (!wp.itemEmpty("ex_corp_p_seqno")) {
	            lsWhere += " and exists(select 1 from crd_corp where crd_corp.corp_p_seqno = col_cs_cslog.corp_p_seqno " 
	                    + sqlCol(wp.itemStr("ex_corp_p_seqno"), "crd_corp.corp_no") 
	                    + ")";
		}
		if(wp.itemEmpty("ex_curr_code")) {
			lsWhere += " and col_cs_cslog.curr_code = '901' ";
		} else {
            lsWhere += sqlCol(wp.itemStr("ex_curr_code"), "col_cs_cslog.curr_code");
		}
		
		wp.whereStr = lsWhere;
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();

	    queryRead();
	}

	private boolean getWhereStr() {
		String idPSeqno = wp.itemStr("ex_id_p_seqno");
        String corpPSeqno = wp.itemStr("ex_corp_p_seqno");
        if (empty(idPSeqno) && empty(corpPSeqno)) {
            alertErr("正卡人證號及商務卡統編請至少輸一項查詢條件");
            return false;
        } 
        if(!empty(idPSeqno)) {
        	if(idPSeqno.length()!=10) {
            	alertErr("正卡人證號核驗長度需等於10碼");
                return false;
            }
        }
        
        if(!empty(corpPSeqno)) {
        	if(corpPSeqno.length()>11 || corpPSeqno.length()<8) {
            	alertErr("商務卡統編核驗長度需介於8碼及11碼");
                return false;
            }
        }
        
        return true;
	}

	@Override
	public void queryRead() throws Exception {
		 wp.pageControl();

		 wp.selectSQL = " col_cs_cslog.crt_date, " + "crt_time, " + "id_p_seqno,"+ "corp_p_seqno,"
		 + "uf_idno_id(id_p_seqno) as id_no," + "uf_corp_no(corp_p_seqno) as corp_no," 
//		 + "crt_user," + "crt_user_dept," 
		 + "col_cs_cslog.acct_type," + "chin_name, "
		 + "col_cs_cslog.curr_code," + "curr_chi_name, " + "proc_code," + "proc_code_desc, " + "callout_tel,"
			        + "proc_desc";
	    wp.daoTable = "col_cs_cslog left join ptr_currcode on col_cs_cslog.curr_code = ptr_currcode.curr_code "
	    		+ " left join ptr_acct_type on col_cs_cslog.acct_type = ptr_acct_type.acct_type ";
		logSql();
		pageSelect();
		if (sqlRowNum <= 0) {
		      alertErr2("此條件查無資料");
		      return;
		    }
		 for (int ii = 0; ii < wp.selectCnt; ii++) {
	            wp.colSet(ii, "SER_NUM", String.format("%02d", ii + 1));
		 }
		    
		wp.setListCount(1);
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

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub
		
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
	public void pdfPrint() throws Exception {
		 wp.reportId = "colr6010";
		    wp.pageRows = 999;
		    String cond1;

		    cond1 = "統計期間: " + commString.strToYmd(wp.itemStr("ex_crt_date_s")) + " -- "
		        + commString.strToYmd(wp.itemStr("ex_crt_date_e"));
		    wp.colSet("cond1", cond1);
		    wp.colSet("user_id", wp.loginUser);
		    queryFunc();
		    TarokoPDF pdf = new TarokoPDF();
		    wp.fileMode = "Y";
		    pdf.excelTemplate = "colr6010.xlsx";
		    pdf.pageCount = 30;
		    pdf.sheetNo = 0;
		    pdf.procesPDFreport(wp);
		    pdf = null;
		    return;

		
	}

}
