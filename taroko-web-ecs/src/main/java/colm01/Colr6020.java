/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 112-01-16  V1.00.00  Machao      program initial                          *
 * 112-01-19  V1.00.01  Sunny       從colm05搬到colm01    
 * 112-01-30  V1.00.02  Machao      業績分行權限調整               *
 * 112-05-25  V1.00.03  Ryan        修正分業筆數問題               *
 * 112-06-09  V1.00.04  Sunny       增加最新資料日期               *
 * 112-06-09  V1.00.05  Ryan        增加資料日期帶入最新日期 、增加塞選條件免列報、 增加塞選條件協商狀態             
 * 112-06-15  V1.00.06  Ryan        增加帳戶狀態=空的 acct_status != 4 *
 * 112-06-21  V1.00.07  Ryan        增加協商種類、協商狀態查詢 *
 * 112-06-23  V1.00.08  Sunny       增加統編資料顯示， 調整欄位顯示位置 *
 * 112-06-26  V1.00.09  Ryan        修正協商狀態下拉選單  *
 * 112-07-05  V1.00.10  Ryan        增加產生統計表PDF  *
 * 112-07-21  V1.00.11  Ryan        調整產生統計表PDF  *
 * 112-07-25  V1.00.12  Ryan        業績分行下拉選單改為讀取gen_brn,核卡單位改為輸入                  *
 * 112-08-04  V1.00.13  Ryan        修正SQL與非901未顯示的BUG                       *
 * 112-08-15  V1.00.14  Ryan        增加欄位DC_TTL_AMT_BAL、DC_MIN_PAY_BAL        
 * 112-09-22  V1.00.15  Ryan        調整業績分行控制,調整日期區間但有限制輸入                                         *
 * 112-09-25  V1.00.16  Ryan         免列報 Y顯示紅色粗體                                                                             *
 * 112-09-26  V1.00.17  Ryan        修改where 條件 ,增加curr_code條件,金額總計                                             *
 * 112-09-27  V1.00.18  Ryan        第2區塊條件修正                                             *
 * 112-09-28  V1.00.19  Ryan        queryReadPdf1修改金額DC_                          *
 * 112-10-02  V1.00.20  Ryan        業績分行全部或是3144 則不顯示(A) + (B) 合            計
 * 112-10-04  V1.00.21  Ryan        調整PDF2 where 關聯acno 的條件                                                *
 * 112-10-05  V1.00.22  Ryan        調整PDF1 跳頁筆數                                                                                  *
 * 112-11-20  V1.00.23  Ryan        調整協商註記    11~13  21~23  51~53 71~73  3X 4X均顯示  其他不顯示OR 空白                                                                               *
 ******************************************************************************/
package colm01;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Colr6020 extends BaseAction implements InfacePdf{
	LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
	int mapIndex = 0;
	int sumCnt = 0;
	int sumCntAB = 0;
	double sumTtlAmt = 0;
	double sumTtlAmtAB = 0;
	double sumMinPayAmt = 0;
	double sumMinPayAmtAB = 0;
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
		    } else if (eqIgno(wp.buttonCode, "PDF1")) { // -PDF-
		      strAction = "PDF1";
		      pdfPrint();
		    } else if (eqIgno(wp.buttonCode, "PDF2")) { // -PDF-
			  strAction = "PDF2";
			  pdfPrint();
		    } else if (eqIgno(wp.buttonCode, "AJAX")) { // -AJAX-
			  strAction = "AJAX";
			  changeOption();
			}

		    dddwSelect();
		    initButton();
		
	}

	@Override
	public void dddwSelect() {
		try {
		      if (eqIgno(wp.respHtml, "colr6020")) {
		          wp.optionKey = wp.colStr(0, "ex_reg_bank_no");
//		          dddwList("dddw_reg_bank_no", "COL_CS_RPT", "reg_bank_no", "reg_bank_name", "where 1=1");
		          dddwList("dddw_reg_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1");
		          // 業績分行
//	                dddwList("dddw_reg_bank_no", "col_cs_rpt", "distinct reg_bank_no",
//	                        "reg_bank_no||'_'||reg_bank_name", " where 1 = 1 ");
	                
	                wp.optionKey = wp.colStr(0, "ex_risk_bank_no");
	  	          // 核卡單位 
//	              dddwList("dddw_risk_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1");
//	                dddwList("dddw_risk_bank_no", "col_cs_rpt", "distinct risk_bank_no",
//	                        "risk_bank_no||'_'||risk_bank_name", " where 1 = 1 ");
//		         --------------------------------------------------- 
		          wp.optionKey = wp.colStr(0, "ex_acct_type");
		          dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
//		         --------------------------------------------------- 
		          wp.optionKey = wp.colStr(0, "ex_curr_code");
		          dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
		      
		      
		      	 String[] negoCde = new String[] {"0","1", "2", "3", "4", "5", "6", "7"};
	        	 String[] negoTxt = new String[] {"全部","債務協商", "前置協商", "更生", "清算", "個別協商", "消金無擔保展延", "前置調解"};
	        	 wp.optionKey = wp.colStr(0, "ex_pay_by_stage_flag1");
	        	 wp.colSet("pay_by_stage_flag_option1", ddlbOption(negoCde,negoTxt)); 
	        	 
		      }
		      changeOption();
		    } catch (Exception ex) {
		    }
		
	}

	@Override
	public void queryFunc() throws Exception {
		
		if (!checkWhereStr()) {
			selectMaxCreateDate();
            return;
        }
		
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();

	    if (eqIgno(wp.buttonCode, "PDF1")){
	    	queryReadExcel();
	    }else if(eqIgno(wp.buttonCode , "PDF2")){
	    	queryReadPdf1("1A");
	    	queryReadPdf2("1B");
	    	queryReadPdf1("2A");
	    	queryReadPdf2("2B");
	    	queryReadPdf1("3A");
	    	queryReadPdf2("3B");
	    	queryReadPdf1("4A");
	    	queryReadPdf2("4B");
	    	printPdfListData();
	    }else {
	    	queryRead();
	    }
	    
//		selectMaxCreateDate();
	    
	}
	
	private boolean checkWhereStr() {
		String crdIdno = wp.itemStr("ex_crd_idno");
        String corpNo = wp.itemStr("ex_corp_no");
        if(!empty(crdIdno)) {
        	if(crdIdno.length()!=10) {
            	alertErr("正卡人證號核驗長度需等於10碼");
                return false;
            }
        }
        
        if(!empty(corpNo)) {
        	if(corpNo.length()>11 || corpNo.length()<8) {
            	alertErr("商務卡統編核驗長度需介於8碼及11碼");
                return false;
            }
        }
        
        if(",PDF1,PDF2".indexOf(strAction)>0 && !wp.itemEq("ex_ctreate_date_s", wp.itemStr("ex_ctreate_date_e"))) {
        	if("PDF1".equals(strAction)) {
        		if(wp.itemEmpty("ex_crd_idno") && wp.itemEmpty("ex_corp_no")) {
        			alertErr("統計查詢之資料日期起日與迄日需為同一日或正卡人證號、商務卡統編不能同時為空白");
        			return false;
        		}
        		return true;
        	}
        	alertErr("統計查詢之資料日期起日與迄日需為同一日");
            return false;
        }
        
        return true;
	}
	
	private boolean getWhereStr() {
		
        wp.whereStr = " where 1=1 ";
		
		wp.whereStr += sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type")
		        + sqlCol(wp.itemStr("ex_crd_idno"), "a.id_no")
		        + sqlCol(wp.itemStr("ex_corp_no"), "a.corp_no")
		        + sqlCol(wp.itemStr("ex_curr_code"), "a.curr_code")
		        + sqlCol(wp.itemStr("ex_reg_bank_no"), "a.reg_bank_no")
		        + sqlCol(wp.itemStr("ex_ctreate_date_e"), "a.create_date", "<=")
		        + sqlCol(wp.itemStr("ex_ctreate_date_s"), "a.create_date", ">=")
		        + sqlCol(wp.itemStr("ex_risk_bank_no"), "a.risk_bank_no")
		        + sqlCol(wp.itemStr("int_rate_mcode_s"), "double(decode(a.int_rate_mcode,'','0',a.int_rate_mcode))", ">=")
		 		+ sqlCol(wp.itemStr("int_rate_mcode_e"), "double(decode(a.int_rate_mcode,'','0',a.int_rate_mcode))", "<=");
		if(wp.itemEq("ex_pay_by_stage_flag", "Y")||wp.itemEmpty("ex_pay_by_stage_flag")) {
			if(!wp.itemEq("ex_pay_by_stage_flag1", "0"))
				wp.whereStr += sqlCol(wp.itemStr("ex_pay_by_stage_flag1"), "substring(a.pay_by_stage_flag,1,1)");
			if(!wp.itemEq("ex_pay_by_stage_flag2", "0"))
				wp.whereStr	+= sqlCol(wp.itemStr("ex_pay_by_stage_flag2"), "substring(a.pay_by_stage_flag,2,1)");
		}

		if(wp.itemEq("ex_pay_by_stage_flag", "Y")) {
			wp.whereStr += " and a.pay_by_stage_flag != '' ";
		}
		if(wp.itemEq("ex_pay_by_stage_flag", "N")) {
			wp.whereStr += " and a.pay_by_stage_flag = '' ";
		}
		if(wp.itemEq("ex_collect_flagx", "Y")) {
			wp.whereStr += " and a.collect_flagx = 'Y' ";
		}
		if(wp.itemEq("ex_collect_flagx", "N")) {
			wp.whereStr += " and a.collect_flagx = 'N' ";
		}
		if(!wp.itemEmpty("ex_acct_status")) {
			wp.whereStr +=sqlCol(wp.itemStr("ex_acct_status"), "a.acct_status");
		}else {
			wp.whereStr += " and a.acct_status != '4'";	
		}
        return true;
	}
	
	void getWhereStrPdf(String type) {
		wp.whereStr = " where 1=1 ";
		
		if(wp.itemEmpty("ex_curr_code") == false) {
			wp.whereStr += sqlCol(wp.itemStr("ex_curr_code"), "a.curr_code");
		}
		
		if(commString.pos(",1A,2A,3A,4A", type)>0) {
			 wp.whereStr += " and a.acct_status = '3'";
		}
		if(commString.pos(",1B,2B,3B,4B", type)>0) {
			 wp.whereStr += " and a.acct_status in ('1','2') and a.ACNO_P_SEQNO=b.P_SEQNO ";
//			 		+ "and a.ACCT_STATUS != '3'";
//			 wp.whereStr += " AND NOT EXISTS ( SELECT 1 FROM act_acno c WHERE c.P_SEQNO=b.P_SEQNO AND c.ACCT_STATUS='3' ";
//			 wp.whereStr += sqlCol(strMid(wp.itemStr("ex_ctreate_date_s"), 0, 6), "substring(b.status_change_date,1,6)" ,"!=");
//			 wp.whereStr += " ) ";
		}
		
		 wp.whereStr += sqlCol(wp.itemStr("ex_ctreate_date_s"), "A.CREATE_DATE");
		 
		 if("1A".equals(type)||"1B".equals(type)) { 
			 if(!wp.itemEq("ex_reg_bank_no","3144") && !wp.itemEmpty("ex_reg_bank_no")) {
				 wp.whereStr += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.risk_bank_no");
				 wp.whereStr += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.reg_bank_no");
			 }
//			 wp.whereStr += " and substring(A.PAY_BY_STAGE_FLAG,2,1) not in ('1','2','3') and A.collect_flagx!='Y' "; 
		 } 
		 if("2A".equals(type)||"2B".equals(type)) {
			 wp.whereStr += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.reg_bank_no");
			 wp.whereStr += " and A.risk_bank_no = '3144' ";
//			 wp.whereStr += " and A.risk_bank_no = '3144' and substring(A.PAY_BY_STAGE_FLAG,2,1) not in ('1','2','3') and A.collect_flagx!='Y' "; 
		 }
		 if("3A".equals(type)||"3B".equals(type)) {
			 wp.whereStr += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.reg_bank_no");
//			 wp.whereStr += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.risk_bank_no");
			 wp.whereStr += " AND substring(A.PAY_BY_STAGE_FLAG,1,1) not in ('3','4') "
//			 		+ " AND (substring(A.PAY_BY_STAGE_FLAG,2,1) in ('1','2','3') OR A.collect_flagx = 'Y') ";
					 + " AND A.collect_flagx = 'Y' ";
		 }
		 if("4A".equals(type)||"4B".equals(type)) {
			 wp.whereStr += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.reg_bank_no");
//			 wp.whereStr += sqlCol(wp.itemStr("ex_reg_bank_no"), "a.risk_bank_no");
			 wp.whereStr += " AND substring(A.PAY_BY_STAGE_FLAG,2,1) in ('1','2','3') AND substring(A.PAY_BY_STAGE_FLAG,1,1) in ('3','4') "; 
		 }
	}
	
	private void selectMaxCreateDate() {
        String lsSql = " select max(create_date) as create_date from col_cs_rpt ";
        sqlSelect(lsSql);

        String createDate = sqlStr("create_date");
        if (empty(createDate)) {
            alertErr("此條件查無資料");
        } else {
            wp.colSet("ex_create_date", createDate);
            wp.colSet("ex_ctreate_date_s", createDate);
            wp.colSet("ex_ctreate_date_e", createDate);
        }
    }

	@Override
	public void queryRead() throws Exception {
		 wp.pageControl();
		 getWhereStr();
		 wp.selectSQL = "create_date, " + "chi_name, "+ "CORP_CHI_NAME, " + "CORP_NO, " + "id_no,acct_type,acct_status,pay_by_stage_flag,collect_flagx, "+ "card_no, " + "home_area_code1, " 
				 + "home_tel_no1, " + "home_tel_ext1, " + "office_area_code1, " + "office_tel_no1, " + "office_tel_ext1, " 
		 		 + "ttl_amt, " + "stmt_over_due_amt	, " + "int_rate_mcode, "+ "delay_day, " + "risk_bank_no, " + "risk_bank_name, " + "reg_bank_no, " + "reg_bank_name, " 
		 		 + "line_of_credit_amt, " + "autopay_acct_no, " + "cellar_phone, " + "curr_code," + "credit_level_old," + "credit_level_new," + "DC_TTL_AMT_BAL, "  + "DC_MIN_PAY_BAL";
		 wp.daoTable = "col_cs_rpt a";
		 wp.whereOrder = "";
		 logSql();
		 pageQuery();

		    if (sqlRowNum <= 0) {
		      alertErr2("此條件查無資料");
		      return;
		    }
		    listWkdataA();
		    listWkdata();
		    wp.setListCount(1);
		    wp.setPageValue();
		
	}
	
	
	public void queryReadExcel() throws Exception {
		 wp.pageControl();
		 getWhereStr();
		 wp.selectSQL = "chi_name, "+ "id_no, "+ "a.acct_type, " + "ptr_acct_type.chin_name as chi_name_p, " + "a.acct_status, " + "card_no, "
		 + "home_area_code1, " + "home_tel_no1, " + "home_tel_ext1, " + "office_area_code1, " + "office_tel_no1, " + "office_tel_ext1, " 
		 + "cellar_phone, "	+ "risk_bank_no, " + "risk_bank_name, "	+ "reg_bank_no, " + "reg_bank_name, " + "pay_by_stage_flag, " +"collect_flagx,"
		 + "CORP_CHI_NAME, " + "CORP_NO	, " + "line_of_credit_amt, "+ "INT_RATE_MCODE, " + "a.CURR_CODE as curr_code, "  + "TTL_AMT, " + "STMT_OVER_DUE_AMT, "
		 		  + "DC_TTL_AMT_BAL, "  + "DC_MIN_PAY_BAL";
		 wp.daoTable = "COL_CS_RPT a left join ptr_acct_type on a.acct_type = ptr_acct_type.acct_type ";
		 logSql();
		 pageQuery();

		    if (sqlRowNum <= 0) {
		      alertErr2("此條件查無資料");
		      return;
		    }
		    
		    listWkdataA();
		    listWkdata();
		    wp.setListCount(1);
		    wp.setPageValue();
		
	}
	
	public void queryReadPdf1(String type) throws Exception {
		 wp.pageControl();
		 daoTid = type + "_";
		 wp.selectSQL = " count(*) AS cnt,sum(DC_TTL_AMT_BAL) sum_ttl_amt,sum(DC_MIN_PAY_BAL) sum_min_pay_amt ";
		 wp.daoTable = " col_cs_rpt a ";
		 wp.whereOrder = "";
		 map.put(fmtColName("line",mapIndex++) ,"");
		 if("1A".equals(type)) { 
			 if(!wp.itemEq("ex_reg_bank_no","3144") && !wp.itemEmpty("ex_reg_bank_no")) {
				 map.put(fmtColName("ITEM_NAME",mapIndex) , "營業單位核卡逾期合計(A)");
			 }else {
				 map.put(fmtColName("ITEM_NAME",mapIndex) , "全部逾期和合計");
			 }
		 } 
		 if("2A".equals(type)) {
			 map.put(fmtColName("ITEM_NAME",mapIndex) , "信用卡部核卡逾期合計(B)");
		 }
		 if("3A".equals(type)) {
			 map.put(fmtColName("ITEM_NAME",mapIndex) , "前置、債務協商免列報逾期帳款合計(C)");
		 }
		 if("4A".equals(type)) {
			 map.put(fmtColName("ITEM_NAME",mapIndex) , "更生清算協商戶(D)");
		 }
		 getWhereStrPdf(type);
		 logSql();
		 pageQuery();
		 
		 map.put(fmtColName("curr_code",mapIndex) , "幣別");
		 map.put(fmtColName("mcode",mapIndex) , "MCODE");
		 map.put(fmtColName("cnt",mapIndex) , "筆數");
		 map.put(fmtColName("sum_ttl_amt",mapIndex) , "帳單應繳餘額");
		 map.put(fmtColName("sum_min_pay_amt",mapIndex) ,"帳單應繳最低餘額");
		 map.put(fmtColName("line",mapIndex++) ,"");

		 sumCnt = wp.colInt(0,type + "_"+"cnt");
		 sumTtlAmt = wp.colNum(0,type + "_"+"sum_ttl_amt");
		 sumMinPayAmt = wp.colNum(0,type + "_"+"sum_min_pay_amt");
		 map.put(fmtColName("curr_code",mapIndex) , wp.itemEmpty("ex_curr_code")?"全部":wp.itemStr("ex_curr_code"));
		 map.put(fmtColName("mcode",mapIndex) , "轉入催收款(含當月)");
		 map.put(fmtColName("cnt",mapIndex) , String.valueOf(sumCnt));
		 map.put(fmtColName("sum_ttl_amt",mapIndex) , String.valueOf(sumTtlAmt));
		 map.put(fmtColName("sum_min_pay_amt",mapIndex) ,String.valueOf(sumMinPayAmt));
		 map.put(fmtColName("line",mapIndex++) ,"");

	}
	
	public void queryReadPdf2(String type) throws Exception {
		 String[] mcodes = {"M0","M1","M2","M3","M4","M5","M6","M7","M8+"};
		 int notCnt901 = 0;
		 wp.pageControl();
		 daoTid = type + "_";
		 wp.selectSQL = "CURR_CODE,decode(a.INT_RATE_MCODE,'0','M0',1,'M1',2,'M2',3,'M3',4,'M4',5,'M5',6,'M6',7,'M7','M8+') AS mcode,count(*) AS cnt, ";
		 wp.selectSQL += " sum(DC_TTL_AMT_BAL) sum_ttl_amt, sum(DC_MIN_PAY_BAL) sum_min_pay_amt ";
		 wp.daoTable = "col_cs_rpt a,act_acno b";
		 wp.whereOrder = "GROUP BY CURR_CODE,decode(a.INT_RATE_MCODE,'0','M0',1,'M1',2,'M2',3,'M3',4,'M4',5,'M5',6,'M6',7,'M7','M8+') ";
		 wp.whereOrder += "ORDER BY CURR_CODE,decode(a.INT_RATE_MCODE,'0','M0',1,'M1',2,'M2',3,'M3',4,'M4',5,'M5',6,'M6',7,'M7','M8+') ";
		 getWhereStrPdf(type);
		 logSql();
		 pageQuery();
		 for(int x = 0;x<mcodes.length;x++) {
			 map.put(fmtColName("curr_code",mapIndex) , wp.itemEmpty("ex_curr_code")?"901":wp.itemStr("ex_curr_code"));
			 map.put(fmtColName("mcode",mapIndex) , mcodes[x]);
			 map.put(fmtColName("cnt",mapIndex) , "0");
			 map.put(fmtColName("sum_ttl_amt",mapIndex) , "0");
			 map.put(fmtColName("sum_min_pay_amt",mapIndex) , "0");

			 for(int i = 0;i<wp.selectCnt;i++) {
				 if("901".equals(wp.colStr(i,type + "_"+"curr_code"))) {
					 if(!mcodes[x].equals(wp.colStr(i,type + "_"+"mcode"))) {
						 continue;
					 }
				 }else {
					if(notCnt901 > i)
						continue;
					notCnt901 ++;
					x = -1;
				 }

				 sumCnt += wp.colInt(i,type + "_"+"cnt");
				 sumTtlAmt += wp.colNum(i,type + "_"+"sum_ttl_amt");
				 sumMinPayAmt += wp.colNum(i,type + "_"+"sum_min_pay_amt");
				 
				 map.put(fmtColName("curr_code",mapIndex) , wp.colStr(i,type + "_"+"curr_code"));
				 map.put(fmtColName("mcode",mapIndex) , wp.colStr(i,type + "_"+"mcode"));
				 map.put(fmtColName("cnt",mapIndex) , wp.colInt(i,type + "_"+"cnt")+"");
				 map.put(fmtColName("sum_ttl_amt",mapIndex) , wp.colNum(i,type + "_"+"sum_ttl_amt")+"");
				 map.put(fmtColName("sum_min_pay_amt",mapIndex) , wp.colNum(i,type + "_"+"sum_min_pay_amt")+"");
				 break;
			 }
			 map.put(fmtColName("line",mapIndex++) ,"");
		 }
		 map.put(fmtColName("ITEM_NAME",mapIndex) , "合            計:");
		 map.put(fmtColName("curr_code",mapIndex) , "");
		 map.put(fmtColName("mcode",mapIndex) , "");
		 map.put(fmtColName("cnt",mapIndex) , String.valueOf(sumCnt));
		 map.put(fmtColName("sum_ttl_amt",mapIndex) , String.valueOf(sumTtlAmt));
		 map.put(fmtColName("sum_min_pay_amt",mapIndex) , String.valueOf(sumMinPayAmt));
		 map.put(fmtColName("line",mapIndex++) ,"");
		 if("1B".equals(type)) {
			 sumCntAB += sumCnt; 
			 sumTtlAmtAB += sumTtlAmt; 
			 sumMinPayAmtAB += sumMinPayAmt; 
		 }
	
		 if("2B".equals(type) && !wp.itemEq("ex_reg_bank_no","3144") && !wp.itemEmpty("ex_reg_bank_no")) {
			 sumCntAB += sumCnt; 
			 sumTtlAmtAB += sumTtlAmt; 
			 sumMinPayAmtAB += sumMinPayAmt; 
			 map.put(fmtColName("ITEM_NAME",mapIndex) , "##BOLD_(A) + (B) 合            計:");
			 map.put(fmtColName("curr_code",mapIndex) , "");
			 map.put(fmtColName("mcode",mapIndex) , "");
			 map.put(fmtColName("cnt",mapIndex) , "##BOLD_"+sumCntAB);
			 map.put(fmtColName("sum_ttl_amt",mapIndex) , "##BOLD_"+sumTtlAmtAB);
			 map.put(fmtColName("sum_min_pay_amt",mapIndex) ,"##BOLD_"+sumMinPayAmtAB);
			 map.put(fmtColName("line",mapIndex++) ,"");
		 }

	}
	
	void printPdfListData() {
		int i = 0;
		String[] keyName = {"cnt","sum_ttl_amt","sum_min_pay_amt"};
		for(String key : map.keySet()) {
			String[] colName = key.split("#");
			if("line".equals(colName[0])) {
				i++;
				continue;
			}
			String value = map.get(key)!=null?map.get(key).toString():"";
			if(Arrays.asList(keyName).indexOf(colName[0]) >= 0 
					&& (value.indexOf("##BOLD_")>=0 || isNumber(value))) {
				String bold = ""; 
				if(value.indexOf("##BOLD_")>=0) {
					value = value.replace("##BOLD_", "");
					bold = "##BOLD_";
				}
				if(keyName[0].equals(colName[0]))
					value = String.format("%s%,d", bold,toInt(value));
				else
					value = String.format("%s%,.2f", bold,toNum(value));
			}
			wp.colSet(i,colName[0]+"_pdf",value);
		}
		wp.selectCnt = i;
		wp.listCount[0] = i;
	}
	
	 String fmtColName(String col ,int index) { 
		 return String.format("%s#%s",col,index);
	 }
	
	  void listWkdataA() {
          String negoType = "";
          String[] negoCde = {};
          String[] negoTxt = {};
          for (int ii = 0; ii < wp.selectCnt; ii++) {
        	  negoType = strMid(wp.colStr(ii, "pay_by_stage_flag"),0,1);
        	  negoCde = new String[] {"1", "2", "3", "4", "5", "6", "7"};
        	  negoTxt = new String[] {"債務協商", "前置協商", "更生", "清算", "個別協商", "消金無擔保展延", "前置調解"};
              wp.colSet(ii, "tt_liab_type", commString.decode(negoType, negoCde, negoTxt));
            if (negoType.equals("1")) {
//              cde = new String[] {"1", "205", "3", "4"};
//              txt = new String[] {"1.停催", "2.復催", "3.協商成功", "4.結案"};
            	negoCde = new String[] {"1", "2", "3", "4", "5","6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
            if (negoType.equals("2")) {
//              negoCde = new String[] {"1", "2", "3", "4", "5"};
//              txt = new String[] {"1.受理申請", "2.停催", "3.簽約成功", "4.結案/復催", "5.結案/結清"};
            	negoCde = new String[] {"1", "2", "3", "4", "5","6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
            if (negoType.equals("3")) {
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6", "7"};
            	negoTxt = new String[] {"更生開始", "更生撤回", "更生認可", "更生履行完畢", "更生裁定免責", "更生調查程序",
                  "更生駁回"};
            }
            if (negoType.equals("4")) {
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6", "7","8"};
            	negoTxt = new String[] {"清算程序開始", "清算程序終止", "清算程序開始同時終止", "清算撤銷免責", "清算調查程序",
                  "清算駁回", "清算撤回", "清算復權"};
            }
            if (negoType.equals("5")) {
//              cde = new String[] {"1", "2", "3", "4"};
//              txt = new String[] {"1.達成個別協商", "2.提前清償", "3.毀諾", "4.毀諾後清償"};
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
//            if (negoType.equals("6")) {
//            	negoCde = new String[] {"1", "2", "3"};
//            	negoTxt = new String[] {"1.受理申請", "2.展延成功", "3.取消或結案"};
//            }
            if (negoType.equals("7")) {
//              cde = new String[] {"1", "3", "4", "5", "6"};
//              txt = new String[] {"1.受理申請", "3.簽約成功", "4.結案/復催", "5.結案/結清", "6.本行無債權"};
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
            String negoType2 = strMid(wp.colStr(ii, "pay_by_stage_flag"),1,1);
            wp.colSet(ii, "tt_pay_by_stage_flag", commString.decode(negoType2, negoCde, negoTxt));
            wp.colSet(ii, "nego_type", negoType + negoType2);
          }
        }

	private void listWkdata() {
		 String wkData;
		 double sumTtlAmt = 0;
		 double sumStmtOverDueAmt = 0;
		 double sumDcTtlAmtBal = 0;
		 double sumDcMinPayBal = 0;
		 CommString comms = new CommString();
	        String[] code = new String[]{"1", "2", "3", "4"};
	        String[] text = new String[]{"1.正常", "2.逾放", "3.催收", "4.呆帳"};
	        for (int ii = 0; ii < wp.selectCnt; ii++) {
	            wp.colSet(ii, "SER_NUM", String.format("%02d", ii + 1));

	            wkData = wp.colStr(ii, "acct_status");
	            wp.colSet(ii, "acct_status", commString.decode(wkData, code, text));

	            wkData = wp.colStr(ii, "int_rate_mcode");
	            if (!empty(wkData)) {
	                if (Integer.parseInt(wkData) > 99) {
	                    wp.colSet(ii, "int_rate_mcode", "M99");
	                } else {
	                    wp.colSet(ii, "int_rate_mcode", "M" + wkData);
	                }
	            }
	            if(wp.colEq(ii,"collect_flagx", "Y")) {
	            	 wp.colSet(ii,"css_font"," col_key");
	            }
	            if(wp.colEq(ii,"collect_flagx", "Y")) {
	            	 wp.colSet(ii,"collect_flagx_pdf","##RED_Y");
	            }else {
	            	wp.colSet(ii,"collect_flagx_pdf","N");
	            }
	            
	            String payByStageFlag = "";
	            if(comms.pos(",11,12,13,21,22,23,51,52,53,71,72,73", wp.colStr(ii,"pay_by_stage_flag"))>0
	            		|| (wp.colNum(ii,"pay_by_stage_flag") >= 30 && wp.colNum(ii,"pay_by_stage_flag") <= 40)) {
	            	payByStageFlag = wp.colStr(ii,"pay_by_stage_flag");
	            }
	           	wp.colSet(ii,"pay_by_stage_flag", payByStageFlag);
	            
	            sumTtlAmt += wp.colNum(ii,"ttl_amt");
	            sumStmtOverDueAmt += wp.colNum(ii,"stmt_over_due_amt");
	            sumDcTtlAmtBal += wp.colNum(ii,"dc_ttl_amt_bal");
	            sumDcMinPayBal += wp.colNum(ii,"dc_min_pay_bal");
	            
	        }	     

	        wp.colSet("sum_ttl_amt", sumTtlAmt);
	        wp.colSet("sum_stmt_over_due_amt", sumStmtOverDueAmt);
	        wp.colSet("sum_dc_ttl_amt_bal", sumDcTtlAmtBal);
	        wp.colSet("sum_dc_min_pay_bal", sumDcMinPayBal);
	}
	
	void changeOption() throws Exception {
		String optionJson1 = "";
		if("AJAX".equals(strAction))
			optionJson1 = wp.itemStr("option_json1");
		else
			optionJson1 = wp.itemStr("ex_pay_by_stage_flag1");
		String[] negoCde = null;
		String[] negoTxt = null;
		switch(optionJson1) {
		case "1" :
         	negoCde = new String[] {"0","1", "2", "3", "4", "5","6"};
         	negoTxt = new String[] {"全部","受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
         	break;
		case "2" :
         	negoCde = new String[] {"0","1", "2", "3", "4", "5","6"};
         	negoTxt = new String[] {"全部","受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
         	break;
		case "3" :
         	negoCde = new String[] {"0","1", "2", "3", "4", "5", "6", "7"};
         	negoTxt = new String[] {"全部","更生開始", "更生撤回", "更生認可", "更生履行完畢", "更生裁定免責", "更生調查程序","更生駁回"};
            break;
        case "4" :
         	negoCde = new String[] {"0","1", "2", "3", "4", "5", "6", "7","8"};
         	negoTxt = new String[] {"全部","清算程序開始", "清算程序終止", "清算程序開始同時終止", "清算撤銷免責", "清算調查程序",
               "清算駁回", "清算撤回", "清算復權"};
         	break;
        case "5" :
         	negoCde = new String[] {"0","1", "2", "3", "4", "5", "6"};
         	negoTxt = new String[] {"全部","受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
         	break;
        case "6" :
        	negoCde = new String[] {"1", "2", "3"};
        	negoTxt = new String[] {"受理申請", "展延成功", "取消或結案"};
         	break;
        case "7" :
         	negoCde = new String[] {"0","1", "2", "3", "4", "5", "6"};
         	negoTxt = new String[] {"全部","受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
         	break;
        default:
        	negoCde = new String[] {"0"};
         	negoTxt = new String[] {"全部"};
         	break;
		}
		
    	 wp.optionKey = wp.colStr(0, "ex_pay_by_stage_flag2");
    	 String option = ddlbOption(negoCde,negoTxt);
    	 if("AJAX".equals(strAction))
    		 wp.addJSON("ddd_option2",option.replaceAll("\n|\r","")); 
    	 else
    		 wp.colSet("pay_by_stage_flag_option2",option.replaceAll("\n|\r","")); 
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
		 if (eqIgno(wp.respHtml, "colr6020")) {
			    selectMaxCreateDate();
			 	selectUserBankNo();
	            wp.colSet("int_rate_mcode_s", "0");
	        }
	}
	
	private void selectUserBankNo() {
        String lsSql = " select bank_unitno from sec_user where usr_id = ? ";
        sqlSelect(lsSql, new Object[]{wp.loginUser});

        wp.colSet("bank_unitno", sqlStr("bank_unitno"));
    }
	
	@Override
	public void pdfPrint() throws Exception {
		 wp.reportId = "PDF2".equals(strAction)?"colr6020_pdf2":"colr6020";
		    wp.pageRows = 999;
		    String cond1;

		    if(!checkWhereStr()) {
		    	wp.errCode = "Y";
		    	wp.respHtml = "TarokoErrorPDF";
		    	return;
		    }
		    
		    cond1 = "統計日期: " + commString.strToYmd(wp.itemStr("ex_ctreate_date_s"));
		    wp.colSet("cond1", cond1);
		    wp.colSet("user_id", wp.loginUser);
		    if(wp.itemEmpty("ex_reg_bank_no")) {
		    	 wp.colSet("ex_reg_bank_no", wp.itemNvl("ex_reg_bank_no","全部"));
		    }else{
		    	String sqlCmd = "select branch||'_'||full_chi_name as branch_chi_name from gen_brn where branch = :branch ";
		    	setString("branch",wp.itemStr("ex_reg_bank_no"));
		    	sqlSelect(sqlCmd);
		    	wp.colSet("ex_reg_bank_no", sqlStr("branch_chi_name"));
		    }   
		    queryFunc();
		    TarokoPDF pdf = new TarokoPDF();
		    wp.fileMode = "Y";
		    pdf.excelTemplate = "PDF2".equals(strAction)?"colr6020_pdf2.xlsx":"colr6020.xlsx";
		    pdf.pageCount = 30;
		    pdf.sheetNo = 0;
		    pdf.procesPDFreport(wp);
		    pdf = null;
		    return;

		
	}

}
