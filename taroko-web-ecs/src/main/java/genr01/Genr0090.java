/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *   DATE       Version       AUTHOR               DESCRIPTION                *
 * ---------  ------------  ---------- -------------------------------------- *
 * 112-04-06    V1.00.00      Yang Bo             program initial             *
 * 112-04-07    V1.00.01      Yang Bo             pdf template update         *
 * 112-05-11    V1.00.02      Ryan                增補VD相關數據              
 * 112-05-25    V1.00.03      Machao              凡外幣者, 增 ‘約當台幣’ 欄位的呈現            *
 ******************************************************************************/
package genr01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel2;
import taroko.com.TarokoPDF;

public class Genr0090 extends BaseEdit {
    private final String PROGNAME = "genr0090";
    private final String orgTable = "act_master_bal";
    private String reportSubtitle = "";
    private String currRate = "";
    private String sendDate = "";
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            // is_action="new";
            // clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page */
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            dataRead();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
            strAction = "XLS";
            // wp.setExcelMode();
            xlsPrint();
        } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
            strAction = "PDF";
            // wp.setExcelMode();
            pdfPrint();
        }

        dddwSelect();
        initButton();
    }

    @Override
    public void queryFunc() throws Exception {
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        if (!getWhereStr()) {
            return;
        }

        wp.selectSQL = ""
                + "check_date, "
                + "curr_code ";
//                + "'一般卡+商務卡+政府網路採購卡' as acct_type";
        wp.daoTable = orgTable;
        wp.whereOrder += " order by check_date, curr_code desc ";

        if (strAction.equals("XLS")) {
            selectNoLimit();
        }
        pageQuery();
        wp.setListCount(1);
        if (sqlRowNum <= 0) {
            alertErr("此條件查無資料");
            return;
        }

        wp.listCount[1] = wp.dataCnt;
        wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
        wp.setPageValue();
    }

    @Override
    public void querySelect() throws Exception {
    }

    @Override
    public void dataRead() throws Exception {
    	dataRead1();
    	dataRead2();
    }

    @Override
    public void saveFunc() throws Exception {
    }

    @Override
    public void initButton() {
    }

    @Override
    public void dddwSelect() {
        try {
            wp.initOption = "全部";
            wp.optionKey = "";
            this.dddwList("dddw_curr_code",
                    " select distinct a.curr_code as db_code, b.curr_chi_name as db_desc" +
                            " from act_master_bal a, ptr_currcode b " +
                            " where a.curr_code = b.curr_code " +
                            " order by a.curr_code");
        } catch (Exception ex) {
        }
    }
    
    /***
              * 一般卡+商務卡+政府網路採購卡
     */
    private void dataRead1() {
        String checkDate = itemKk("data_k1");
        String currCode = itemKk("data_k2");

        wp.selectSQL = ""
                + "check_date, "
                + "curr_code, "
//                + "'一般卡+商務卡+政府網路採購卡' as acct_type, "
                + "(bl_bal + ca_bal) as bal_1, "
                + "(cb_bal + ci_bal + cc_bal) bal_2, "
                + "(ri_bal) bal_3, "
                + "(af_bal + pf_bal + lf_bal) bal_4, "
                + "((op_bal + lk_bal) * -1) bal_5, "
                + "(it_bal) bal_6, "
                + "(bl_bal + ca_bal + cb_bal + ci_bal + cc_bal + ri_bal + af_bal + pf_bal + lf_bal + (op_bal + lk_bal)*-1 + it_bal) as bal_tot ";
        wp.daoTable = orgTable;
        wp.whereStr = "where 1 = 1";
        wp.whereStr += sqlCol(checkDate, "check_date");
        wp.whereStr += sqlCol(currCode, "curr_code");
        wp.whereOrder = " order by check_date, curr_code desc ";

        pageSelect();
        if (sqlNotFind()) {
            alertErr("查無資料1");
        }
        if(!currCode.equals("901")) {
        	sendDate = selectPtrBankRateLog(checkDate,currCode);
            if(sendDate == null) {
            	wp.colSet("send_date", "");
                wp.colSet("curr_rate_o", "");
                wp.colSet("rate_1", "");
                wp.colSet("rate_2", "");
                wp.colSet("rate_3", "");
                wp.colSet("rate_4", "");
                wp.colSet("rate_5", "");
                wp.colSet("rate_6", "");
                wp.colSet("rate_tot", "");
            }else {
            	double currRateO = selectPtrBankRate(checkDate,currCode);
                wp.colSet("send_date", sendDate);
                wp.colSet("curr_rate_o", currRateO);
                wp.colSet("rate_1", wp.colNum("bal_1")*currRateO);
                wp.colSet("rate_2", wp.colNum("bal_2")*currRateO);
                wp.colSet("rate_3", wp.colNum("bal_3")*currRateO);
                wp.colSet("rate_4", wp.colNum("bal_4")*currRateO);
                wp.colSet("rate_5", wp.colNum("bal_5")*currRateO);
                wp.colSet("rate_6", wp.colNum("bal_6")*currRateO);
                wp.colSet("rate_tot", wp.colNum("bal_tot")*currRateO);
            }
        }
    }
    
    /***
     * Visa金融卡
     * @throws Exception 
     */
    private void dataRead2() {
        String checkDate = itemKk("data_k1");
        String currCode = itemKk("data_k2");

        wp.selectSQL = ""
                + " distinct a.curr_code, b.curr_chi_name,a.check_date, "
        		+ " (a.vd_bl_bal + a.vd_ca_bal + a.vd_ot_bal) as sum_vd_bal, "
        		+ " a.vd_lf_bal,(a.vd_refund_bal * -1) as vd_refund_bal, "
        		+ " (a.vd_bl_bal + a.vd_ca_bal + a.vd_ot_bal) + a.vd_lf_bal + (a.vd_refund_bal * -1) as bal_tot2 ";
           
        wp.daoTable = orgTable;
        wp.daoTable += " as a,ptr_currcode as b ";
        wp.whereStr = "where 1 = 1 and a.curr_code = b.curr_code ";
        wp.whereStr += sqlCol(checkDate, "check_date");
        wp.whereStr += sqlCol(currCode, "a.curr_code");
        wp.whereOrder = " order by a.check_date, a.curr_code desc ";

        pageSelect();
        if (sqlNotFind()) {
            alertErr("查無資料2");
        }
        String currChiName = selectPtrCurrcode(currCode);
        wp.colSet("curr_chi_name", currChiName);
        
        if(!currCode.equals("901")) {
            String sendDate = selectPtrBankRateLog(checkDate,currCode);
            if(sendDate == null) {
                wp.colSet("sum_vd_rate", "");
                wp.colSet("vd_lf_rate", "");
                wp.colSet("vd_refund_rate", "");
                wp.colSet("rate_tot2", "");
            }else {
                double currRateO = selectPtrBankRate(checkDate,currCode);
                currRate = selectPtrBankRate2(checkDate,currCode);
                String[] str = currRate.split("\\.");
                if(str[1].length()==1) {
                	currRate = currRate + "0000";
                }else if(str[1].length()==2) {
                	currRate = currRate + "000";
                }else if(str[1].length()==3) {
                	currRate = currRate + "00";
                }else if(str[1].length()==4) {
                	currRate = currRate + "0";
                }
                wp.colSet("send_date", sendDate);
                wp.colSet("curr_rate_o", currRate);
                wp.colSet("sum_vd_rate", wp.colNum("sum_vd_rate")*currRateO);
                wp.colSet("vd_lf_rate", wp.colNum("vd_lf_rate")*currRateO);
                wp.colSet("vd_refund_rate", wp.colNum("vd_refund_rate")*currRateO);
                wp.colSet("rate_tot2", wp.colNum("bal_tot2")*currRateO);
            }
        }
    
    }
    
    public String selectPtrCurrcode( String currCode){
    	   String sql1 = "Select curr_chi_name from ptr_currcode Where curr_code = ?";

    	   sqlSelect(sql1, new Object[]{currCode});

    	   return sqlStr("curr_chi_name");
    	 }
    
    public String selectPtrBankRateLog(String checkDate, String currCode){
 	   String sql1 = "Select send_date, curr_code, curr_rate_o from ptr_bank_rate_log Where send_date = ("
 	   		+ " select max(send_date) from ptr_bank_rate_log where send_date < ? ) and curr_code = ? ";

 	   sqlSelect(sql1, new Object[]{checkDate,currCode});
 	  if (sqlRowNum<=0) {
          alertMsg("查無匯率檔 (ptr_bank_rate_log) !!");
          return null;
      }else {
    	   return sqlStr("send_date");
      }
 	 }

    public double selectPtrBankRate(String checkDate, String currCode){
  	   String sql1 = "Select send_date, curr_code, curr_rate_o from ptr_bank_rate_log Where send_date = ("
  	   		+ " select max(send_date) from ptr_bank_rate_log where send_date < ? ) and curr_code = ? ";

  	   sqlSelect(sql1, new Object[]{checkDate,currCode});
  	 return sqlNum("curr_rate_o");
  	 }
    
    public String selectPtrBankRate2(String checkDate, String currCode){
   	   String sql1 = "Select send_date, curr_code, curr_rate_o from ptr_bank_rate_log Where send_date = ("
   	   		+ " select max(send_date) from ptr_bank_rate_log where send_date < ? ) and curr_code = ? ";

   	   sqlSelect(sql1, new Object[]{checkDate,currCode});
   	 return sqlStr("curr_rate_o");
   	 }
    private boolean getWhereStr() {
        String exDateS = wp.itemStr("ex_date_s");
        String exDateE = wp.itemStr("ex_date_e");
        String exCurrCode = wp.itemStr("ex_curr_code");

        String lsWhere = "where 1 = 1  ";

        // 回饋日期
        if (!chkStrend(exDateS, exDateE)) {
            alertErr("日期起迄輸入錯誤!!");
            return false;
        }
        lsWhere += sqlStrend(exDateS, exDateE, "check_date");

        // curr_code幣別
        if (!empty(exCurrCode)) {
            lsWhere += sqlCol(exCurrCode, "curr_code");
        }

        wp.whereStr = lsWhere;
        return true;
    }

    private void xlsPrint() {
        try {
            log("xlsFunction: started--------");
            wp.reportId = PROGNAME;
            // ===================================
            wp.fileMode = "Y";
            dataRead();
            // -cond-
            subTitle();
            wp.colSet("cond_1", reportSubtitle);
            wp.colSet("user_id", wp.loginUser);

            wp.setListCount(1);
            log("Detl: rowcnt:" + wp.listCount[0]);// -明細-
            TarokoExcel2 xlsx = new TarokoExcel2();
            if(itemKk("data_k2").equals("901")) {
            	 xlsx.excelTemplate = PROGNAME + ".xlsx";
            }else {
            	xlsx.excelTemplate = PROGNAME + "_2.xlsx";
            }
            //分頁欄位
            xlsx.breakField[0] = "check_date";
            xlsx.breakField[1] = "curr_code";
            xlsx.pageBreak = "Y";
            xlsx.pageCount = 34;
            xlsx.sheetName[0] = "明細";
            xlsx.processExcelSheet(wp);
            xlsx.outputExcel();
            log("xlsFunction: ended-------------");
        } catch (Exception ex) {
            wp.expMethod = "xlsPrint";
            wp.expHandle(ex);
        }
    }

    private void pdfPrint() throws Exception {
        wp.reportId = PROGNAME;
        // ===========================
        wp.pageRows = 99999;
        dataRead();
        // -cond-
        subTitle();
        wp.colSet("cond_1", reportSubtitle);

        wp.setListCount(1);

        TarokoPDF pdf = new TarokoPDF();
        wp.fileMode = "Y";
        if(itemKk("data_k2").equals("901")) {
        	pdf.excelTemplate = PROGNAME + ".xlsx";
       }else {
    	   pdf.excelTemplate = PROGNAME + "_2.xlsx";
       }
        pdf.sheetNo = 0;
        pdf.pageCount = 44;
        pdf.pageVert = true; // 直印
        pdf.procesPDFreport(wp);
    }

    private void subTitle() {
        String ss = "";
        String date = empty(itemKk("data_k1")) ? wp.colStr("check_date") : itemKk("data_k1");
        if (date.length() == 8) {
            date = date.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6);
        }
        
        if(itemKk("data_k2").equals("901")) {
        	ss += "  核帳日期 : " + date;
            ss += "  結算幣別 : " + (empty(itemKk("data_k2")) ? wp.colStr("curr_code") : itemKk("data_k2"));
        }else {
        	ss += "  核帳日期 : " + date;
            ss += "  結算幣別 : " + (empty(itemKk("data_k2")) ? wp.colStr("curr_code") : itemKk("data_k2"));
            if (sendDate.length() == 8) {
            	sendDate = sendDate.substring(0, 4) + "/" + sendDate.substring(4, 6) + "/" + sendDate.substring(6);
            }
            ss += "  匯兌日期 : " + sendDate; //selectPtrBankRateLog(itemKk("data_k1"),itemKk("data_k2"))
            ss += "  匯率 : " + currRate;   //  selectPtrBankRate2(itemKk("data_k1"),itemKk("data_k2"))
        }		
        		
        reportSubtitle = ss;
    }
}
