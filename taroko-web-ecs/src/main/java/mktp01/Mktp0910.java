/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/30  V1.00.00   Machao      Initial                                *
***************************************************************************/
package mktp01;

import ofcapp.BaseEdit;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Mktp0910 extends BaseEdit{

	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;
	mktp01.Mktp0910Func func = null;
	  String orgTabName = "mkt_tax_fbdata";
	  String controlTabName = "";
	  String activeCode ="";
	  String activeType ="";
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
	    }  else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
	    	datacheck();
	      } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
	        strAction = "U";
	        updateFunc();
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
		 wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_active_code"), "a.active_code", "like%")
	        ;
	    // -page control-
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();

	    queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.selectSQL = " "
	            + "hex(a.rowid) as rowid, "
	            + "nvl(a.mod_seqno,0) as mod_seqno, "
	            + "a.active_code,"
	            + "a.active_type,"
	            + "a.pay_yyyy,"
	            + "a.staff_flag,"
	            + "a.feedback_id_type,"
	            + "a.gift_type,"
	            + "a.feedback_date";

	    wp.daoTable =  " mkt_tax_fbdata a ";
	    wp.whereOrder = " " + " order by  active_code";

	    pageQuery();
	    wp.setListCount(1);
	    if (sqlNotFind()) {
	      alertErr(appMsg.errCondNodata);
	      buttonOff("btnAdd_disable");
	      return;
	    }
	    commActCode("comm_active_name");
	    commfuncActType("active_type");
	    commfuncStaFlag("staff_flag");
	    commfuncIdType("feedback_id_type");
	    commfuncGiftType("gift_type");
	    
	    // list_wkdata();
	    wp.setPageValue();
	}

	public void datacheck() throws Exception {
		if(empty(wp.itemStr("ex_active_code"))) {
			alertErr2("繳稅活動代碼請選擇 ");
            return;
		}else {
			dataRead();
		}
	}
	
	
	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub
		wp.selectSQL = "hex(a.rowid) as rowid,"
                + " nvl(a.mod_seqno,0) as mod_seqno, "
                + "a.active_code,"
                + "a.active_name,"
                + "a.active_type,"
                + "a.purchase_date_s,"
                + "a.purchase_date_e,"
                + "a.feedback_all_totcnt,"
                + "a.feedback_emp_totcnt,"
                + "a.feedback_nonemp_totcnt,"
                + "a.feedback_peremp_cnt,"
                + "a.feedback_pernonemp_cnt,"
                + "a.purchase_amt_s,"
                + "a.purchase_amt_e,"
                + "a.feedback_id_type,"   
                + "a.gift_type,"
                + "a.cal_def_date"
                ;

        wp.daoTable = " mkt_tax_parm a ";
        wp.whereStr = "where 1=1 and a.active_code = ? ";
        setString(wp.itemStr("ex_active_code"));
        pageSelect();
        sqlParm.clear();
        if (sqlNotFind()) {
            return;
        }
        wp.colSet("active_code",wp.getValue("active_code"));
        wp.colSet("active_type",wp.getValue("active_type"));
        wp.colSet("feedback_id_type",wp.getValue("feedback_id_type"));
        selectBilData(wp.getValue("active_type"));
        
	}
	
    public void selectBilData(String actType) throws Exception {
    	String mchtNo = "";
    	String chiName = "";
    	if(actType.equals("1")) {
    		 mchtNo ="95004001";
    		 chiName = "%綜所稅%";
    	}else if(actType.equals("2")) {
    		 mchtNo ="95004002";
    		 chiName = "%地價稅%";
    	}else if(actType.equals("3")) {
    		 mchtNo ="95004003";
    		 chiName = "%牌照稅%";
    	}else if(actType.equals("4")) {
    		 mchtNo ="95004004";
    		 chiName = "%房屋稅%";
    	}
    	String sql = " select a.PURCHASE_DATE, a.CARD_NO, a.id_p_seqno, a.MCHT_NO, a.ECS_PLATFORM_KIND, "
    			+ " a.MCHT_CATEGORY, a.MCHT_CHI_NAME, a.PAYMENT_TYPE, a.CASH_PAY_AMT,b.id_no , b.staff_flag "
    			+ " FROM ECSCRDB.bil_bill a left join ECSCRDB.crd_idno b on a.id_p_seqno = b.id_p_seqno "
    			+ " WHERE a.acct_type = '01' and "
    			+ " ( mcht_no = ? or ( MCHT_CATEGORY='9311' AND MCHT_CHI_NAME LIKE ? ) "
    			+ "OR PAYMENT_TYPE='Q' ) "
    			+ "ORDER BY PURCHASE_DATE asc, CASH_PAY_AMT desc " ;
    	setString(1,mchtNo);
    	setString(2,chiName);
    	sqlParm.clear();
    	sqlSelect(sql, new Object[] {mchtNo,chiName});
    	if (sqlRowNum <= 0) {
    	      alertErr2("此條件查無資料");
    	      return;
    	    }
    	int amt = 0;
    	int amtY = 0;
    	int amtN = 0;
    	int StaffYcnt = 0;
    	int StaffNcnt = 0;
    	int allTotcnt = Integer.parseInt(wp.getValue("feedback_all_totcnt"));
    	int empTotcnt = Integer.parseInt(wp.getValue("feedback_emp_totcnt"));
    	int nonempTotcnt = Integer.parseInt(wp.getValue("feedback_nonemp_totcnt"));
    	int perempCnt = Integer.parseInt(wp.getValue("feedback_peremp_cnt"));
    	int pernonempCnt = Integer.parseInt(wp.getValue("feedback_pernonemp_cnt"));
    	for(int i =0;i < sqlRowNum;i++) {
    	int dateS = Integer.parseInt(wp.getValue("purchase_date_s")); 
    	int dateE = Integer.parseInt(wp.getValue("purchase_date_e")); 
    	int purdate = Integer.parseInt(sqlStr(i,"purchase_date")); 
    	String staFlag = sqlStr(i,"staff_flag");
    	int puramt = sqlInt("CASH_PAY_AMT");
    	if(dateS <= purdate && dateE >= purdate) {
    		if(staFlag.equals("Y")) {
    			amtY += puramt;
    			StaffYcnt +=1;
    			amt = amtY + amtN;
    			if(empTotcnt > 0 && empTotcnt >= amtY && perempCnt >= StaffYcnt && allTotcnt >= amt ) {
    				wp.colSet("staff_flag",sqlStr(i,"staff_flag"));
    				wp.colSet("purchase_date",sqlStr(i,"purchase_date"));
    				wp.colSet("purchase_amt",sqlInt(i,"cash_pay_amt"));
    				wp.colSet("id_no",sqlStr(i,"id_no"));
    				wp.colSet("id_p_seqno",sqlStr(i,"id_p_seqno"));
    				wp.colSet("card_no",sqlStr(i,"card_no"));
    				 strAction = "A";
    			     insertFunc();
    			}
    		}else {
    			amtN += puramt;
    			StaffNcnt +=1;
    			amt = amtY + amtN;
    			if(nonempTotcnt > 0 && nonempTotcnt >= amtN && pernonempCnt >= StaffNcnt && allTotcnt >= amt) {
    				wp.colSet("staff_flag",sqlStr(i,"staff_flag"));
    				wp.colSet("purchase_date",sqlStr(i,"purchase_date"));
    				wp.colSet("purchase_amt",sqlInt(i,"cash_pay_amt"));
    				wp.colSet("id_no",sqlStr(i,"id_no"));
    				wp.colSet("id_p_seqno",sqlStr(i,"id_p_seqno"));
    				wp.colSet("card_no",sqlStr(i,"card_no"));
    				 strAction = "A";
    			     insertFunc();
    		  }	
    		}
    	  }else {
    		  alertErr2("数据日期不合格");
    	      return;
    	  }
    	}
	}

	@Override
	 public void dddwSelect(){
		 String ls_sql = "";
		    try {
		      if ((wp.respHtml.equals("mktp0910"))) {
		        wp.initOption = "--";
		        wp.optionKey = "";
		        if (wp.colStr("ex_active_code").length() > 0) {
		          wp.optionKey = wp.colStr("ex_active_code");
		        }
		        this.dddwList("dddw_active_code", "mkt_tax_parm", "trim(active_code)",
		            "trim(active_name)", " where 1 = 1 order by purchase_date_s desc");
		        sqlParm.clear();
		      }
		    } catch (Exception ex) {
		    }
	 }

	 // ************************************************************************
	  public void commActCode(String columnData1) throws Exception {
		  String columnData = "";
		    String sql1 = "";
		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      columnData = "";
		      sql1 = "select " + " active_name as column_active_name " + " from mkt_tax_parm "
		          + " where 1 = 1 " + " and   active_code = ? ";
		      if (wp.colStr(ii, "active_code").length() == 0)
		        continue;
		      sqlSelect(sql1,new Object[] {wp.colStr(ii, "active_code")});

		      if (sqlRowNum > 0) {
		        columnData = columnData + sqlStr("column_active_name");
		        wp.colSet(ii, columnData1, columnData);
		      }
		    }
		    return;
	  }

	  // ************************************************************************
	 void commfuncActType(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"1", "2", "3", "4"};
		    String[] txt = {"綜所稅", "地價稅", "牌照稅", "房屋稅"};

		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      wp.colSet(ii, "comm_func_" + cde1, "");
		      for (int inti = 0; inti < cde.length; inti++)
		        if (wp.colStr(ii, cde1).equals(cde[inti])) {
		          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
		          break;
		        }
		    }
		  }
	 
	  void commfuncStaFlag(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"Y", "N"};
		    String[] txt = {"員工", "非員工"};

		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      wp.colSet(ii, "comm_func_" + cde1, "");
		      for (int inti = 0; inti < cde.length; inti++)
		        if (wp.colStr(ii, cde1).equals(cde[inti])) {
		          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
		          break;
		        }
		    }
		  }

	  void commfuncIdType(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"1", "2"};
		    String[] txt = {"ID身分證號", "card_no卡號"};

		    for (int ii = 0; ii < wp.selectCnt; ii++) {
		      wp.colSet(ii, "comm_func_" + cde1, "");
		      for (int inti = 0; inti < cde.length; inti++)
		        if (wp.colStr(ii, cde1).equals(cde[inti])) {
		          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
		          break;
		        }
		    }
		  }
	  void commfuncGiftType(String cde1) {
		    if (cde1 == null || cde1.trim().length() == 0)
		      return;
		    String[] cde = {"1"};
		    String[] txt = {"50元電子現金抵用券"};

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
	public void saveFunc() throws Exception {
		mktp01.Mktp0910Func func = new mktp01.Mktp0910Func(wp);

	    rc = func.dbSave(strAction);
	    if (rc != 1)
	      alertErr2(func.getMsg());
	    log(func.getMsg());
	    this.sqlCommit(rc);
	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub
		
	}
	
	
	 
}
