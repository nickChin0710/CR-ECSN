/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-17  V1.00.00  yash       program initial                            *
* 109-01-07  V1.00.01  ryan       update mantis 0002302                      *
* 111-10-25  v1.00.02  Yang Bo    Sync code from mega                        *
* 112-01-08  V1.00.03  Simon      批號繳款來源說明更新                       *
* 112-06-29  V1.00.04  Simon      批號繳款來源他行代償、還額檔繳款、全國繳費網繳款、花農卡自扣說明更新*
******************************************************************************/

package actp01;

import java.text.SimpleDateFormat;
import java.util.Date;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Actp2020 extends BaseProc {
    String doit="";
    String mExBatchNo="";
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;

        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) { 
            //-資料讀取- 
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {
            /* 新增功能 */
           // insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
            //updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
           // deleteFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page*/
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        }else if (eqIgno(wp.buttonCode, "S2")) {
            /* 存檔*/
            strAction = "S2";
            dataProcess();
        }

        dddwSelect();
        initButton();
    }

    //for query use only
    private boolean getWhereStr() throws Exception {

        wp.whereStr =" where 1=1 " ;
        
        if(empty(wp.itemStr2("ex_bank")) == false){
            wp.whereStr += " and  substr(batch_no,9,4) = :ex_bank ";
            setString("ex_bank", wp.itemStr2("ex_bank"));
        }
        
        
        if(empty(wp.itemStr2("ex_doit")) == false){
        	if(wp.itemStr2("ex_doit").equals("1")){
        		 wp.whereStr += " and  confirm_user = '' ";
        		 doit="0";
        	
        	}else if(wp.itemStr2("ex_doit").equals("2")){
        		wp.whereStr += " and  confirm_user > '' ";
        		doit="1";

        	}
        }
        
        if(empty(wp.itemStr2("ex_apuser")) == false){
            wp.whereStr += " and  crt_user = :ex_apuser ";
            setString("ex_apuser", wp.itemStr2("ex_apuser"));
        }
        
        if(empty(wp.itemStr2("ex_curr_code")) == false){
            wp.whereStr += " and  decode(curr_code,'','901',curr_code) = :ex_curr_code ";
            setString("ex_curr_code", wp.itemStr2("ex_curr_code"));
        }

        return true;
    }

    @Override
    public void queryFunc() throws Exception {
        getWhereStr();
        //-page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {

        getWhereStr();
        wp.pageControl();

        wp.selectSQL += " batch_no,  "
	              + " batch_tot_cnt,"
	              + " batch_tot_amt, "
	              + " confirm_user,  "
	              + " confirm_date, "
	              + " confirm_time,  "
	              + "  uf_dc_amt(curr_code, batch_tot_amt,dc_pay_amt) as wk_dc_pay_amt, "
	              + " crt_user,  "
	              + " decode(substring(batch_no,9,4),'1001','他行代償',              "
	              + "                                '1002','他行自動繳款回饋',      "
	              + "                                '1003','同業代收繳款',          "
	              + "                                '1005','債務協商入帳',          "
	              + "                                '1006','前置協商入帳',          "
	              + "                                '5555','還額檔繳款',            "
	              + "                                '5556','全國繳費網繳款',        "
	              + "                                '9001','TCB 自動繳款回饋',      "
	              + "                                '9002','花農卡 自動繳款回饋',   "
	              + "                                '9007','退貨',                  "
	              + "                                '9999','Dummy Record') as bank_name , "
	              + " decode(curr_code,'','901',curr_code) curr_code,"
	              + " '' as wf_desc,"
	              + " dc_pay_amt,"
	              + " from_desc,"
	              + " mod_user,"
	              + " mod_time,"
	              + " mod_pgm,"
	              + " mod_seqno,"
	              + " hex(rowid) as rowid"
	              ;
		if(doit.equals("0")){
			 wp.selectSQL +=" ,'0' as doit, '待放行' as doitname ";
    		 
		}else if(doit.equals("1")){
   		     wp.selectSQL +=" ,'1' as doit, '解放行' as doitname ";
		}
        
        wp.daoTable = "act_pay_batch";
        wp.whereOrder=" order by crt_user ASC ,batch_no ASC";
      //getWhereStr();

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        wp.listCount[1] = wp.dataCnt;
        wp.setPageValue();
        apprDisabled("crt_user");

        for(int j=0; j<wp.selectCnt;j++){
        	
        	String lsSql2="  select wf_desc from ptr_sys_idtab where wf_type ='DC_CURRENCY' and wf_id = :wf_id ";
          	setString("wf_id",wp.colStr(j,"curr_code"));
        	sqlSelect(lsSql2);
          	wp.colSet(j,"wf_desc", sqlStr("wf_desc"));
        	
/***
         if(wp.colStr(j,"batch_no").length() > 15){
        	if(wp.colStr(j,"batch_no").substring(8,12).equals("1002")){
        		String lsSql=" select bc_abname from ptr_bankcode where bc_bankcode = :bc_bankcode ";
    			setString("bc_bankcode",wp.colStr(j,"batch_no").substring(12,15));
    	    	sqlSelect(lsSql);
    	    	wp.colSet(j,"bank_name", sqlStr("bc_abname"));
    	    	
        	}    
         }
***/        	
        }
        

    }

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
    	
    	 mExBatchNo = itemKk("data_k1");
	

    	 wp.selectSQL ="hex(d.rowid) as rowid, d.mod_seqno, "
				  +" act_pay_batch.batch_no,   "
				  + "act_pay_batch.batch_tot_cnt, "
				  + "act_pay_batch.batch_tot_amt,"
				  + "d.batch_no,"
				  +" d.serial_no,"
				  +" d.acno_p_seqno,"
				  +" d.acct_type,"
				  +" act_acno.acct_key,"
				  +" d.id_p_seqno,"
				  +" UF_IDNO_ID(d.id_p_seqno) as idno,"
				  +" d.pay_card_no,"
				  +" d.pay_amt,"
				  +" case when d.dc_pay_amt > 0 then   d.dc_pay_amt else d.pay_amt end as amt,"
				  +" UF_CURR_NAME(decode(d.curr_code,'','901',d.curr_code))as curr_code,   "
				  +" d.pay_date,"
				  +" d.payment_type,"
				  +" d.duplicate_mark,"
				  +" d.proc_mark,"
				  +" d.mod_user,"
				  +"  uf_2ymd(d.mod_time) as mod_date,"
                  +"  varchar_format(d.mod_time,'hh24miss') as mod_time,"
				  +" act_pay_batch.crt_user,"
				  +" act_pay_batch.crt_date,"
				  +" d.debit_item,"
				  +" d.debt_key,"
				  +" act_acno.payment_no,"
				  +" UF_ACNO_NAME(act_acno.acno_p_seqno) as chi_name,"
				  +" d.dc_pay_amt"
				  ;
		wp.daoTable = "act_acno,act_pay_detail d,act_pay_batch";
		wp.whereStr = "where 1=1";
		wp.whereStr  += " and  act_pay_batch.batch_no = d.batch_no  ";
		wp.whereStr  += " and  decode(d.acno_p_seqno,'',d.p_seqno,d.acno_p_seqno) = act_acno.acno_p_seqno ";
		wp.whereStr  += " and  act_pay_batch.batch_no = :as_batch_no ";
		setString("as_batch_no", mExBatchNo);
		wp.whereOrder=" order by act_pay_batch.crt_user ASC ,act_pay_batch.crt_date ASC,d.serial_no ASC";

		wp.pageRows=999;
		pageQuery();


		if (sqlNotFind()) {
			alertErr("查無資料, batch_no=" + mExBatchNo);
			 return;
		}
		
		wp.setListCount(1);
		
		wp.sqlCmd = "select "
				+ "   count(*) as qty "
				+ " , sum( case when act_pay_detail.dc_pay_amt > 0 then   act_pay_detail.dc_pay_amt else act_pay_detail.pay_amt end) as ls_amt "
			//+ " from act_acno,act_pay_detail,act_pay_batch "
				+ " from act_pay_detail,act_pay_batch "
				+ " where 1=1 "
				+ " and  act_pay_batch.batch_no = act_pay_detail.batch_no "
			//+ " and  act_pay_detail.acno_p_seqno = act_acno.acno_p_seqno  "
		    + " and  act_pay_batch.batch_no = :ls_batch_no ";
		setString("ls_batch_no", mExBatchNo);
		sqlSelect(wp.sqlCmd);
		
		wp.colSet("ls_qty", sqlStr("qty"));
		wp.colSet("ls_amt", sqlStr("ls_amt"));
		
    }

   
    @Override
	public void dataProcess() throws Exception {
    	

		String[] aaRowid = wp.itemBuff("rowid");
		String[] aaModSeqno = wp.itemBuff("mod_seqno");
		String[] opt = wp.itemBuff("opt");
		String[] aaBatchNo = wp.itemBuff("batch_no");
		String[] aaDoit = wp.itemBuff("doit");


		
		wp.listCount[0] = aaBatchNo.length;
		
		// check
		int rr = -1;
		int llOk = 0, llErr = 0;
		String date9001 = getSysDate()+"9001";
		Date currDate = new Date();
		SimpleDateFormat form1 = new SimpleDateFormat("HHmmss");
		String dateStr = form1.format(currDate);
		for (int ii = 0; ii < opt.length; ii++) {
			rr = optToIndex(opt[ii]);

			if (rr < 0) {
				continue;
			}
			
			if(aaBatchNo[rr].substring(0,12).equals(date9001)){
				wp.colSet(rr, "ok_flag", "!");
				llErr++;
				errmsg("該批次為TCB自扣回饋檔案, 不允執行解放行動作!!");
			}
			
			String lsSql = "select mod_seqno from act_pay_batch where batch_no =:batch_no ";
	    	setString("batch_no", aaBatchNo[rr]);
	    	sqlSelect(lsSql);
			String lsModSeqno = sqlStr("mod_seqno");
			
			if(!aaModSeqno[rr].equals(lsModSeqno)){
				wp.colSet(rr, "ok_flag", "!");
				llErr++;
				errmsg("批次明細資料有所異動, 請查明後再放行!!");
			}
			

		}
	

        //save
	    
		if (llErr == 0) {
			// -update-
			rr = -1;
			for (int ii = 0; ii < opt.length; ii++) {
				rr = optToIndex(opt[ii]);
				if (rr < 0) {
					continue;
				}
				//update
				String usSq =" update act_pay_batch set "
					       +"  confirm_user=:confirm_user "
					       +" ,confirm_date=:confirm_date "
					       +" ,confirm_time=:confirm_time "
					       +" ,mod_time=sysdate "
					       +" ,mod_pgm=:mod_pgm "
					       +" ,mod_seqno= nvl(mod_seqno,0)+1 "
					       +" where  hex(rowid) = :rowid  and mod_seqno = :mod_seqno ";
				
				if(aaDoit[rr].equals("0")){
					//待放行
					setString("confirm_user",loginUser());
					setString("confirm_date",getSysDate());
					setString("confirm_time",dateStr);
				}else if(aaDoit[rr].equals("1")){
					//解放行
					setString("confirm_user","");
					setString("confirm_date","");
					setString("confirm_time","");
				}
				

				setString("mod_pgm",wp.itemStr2("mod_pgm"));
				setString("rowid",aaRowid[rr]);
				setString("mod_seqno",aaModSeqno[rr]);
				sqlExec(usSq);
				if (sqlRowNum <= 0) {
					wp.colSet(rr, "ok_flag", "!");
		            llErr++;
		            sqlCommit(0);
		           
				}else{
					wp.colSet(rr, "ok_flag", "V");
		            llOk++;
		            sqlCommit(1);
		           
				}


		  }
		}	
			

			alertMsg("處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";" );
		

	}

    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
    }

    @Override
    public void dddwSelect() {
        try {
           
        	wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_bank");
			this.dddwList("dddw_bank_no", "act_pay_batch ", "substr(batch_no,9,4) ","", "where 1=1 group by substr(batch_no,9,4)");
			
			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_apuser");
			this.dddwList("dddw_apuser", "sec_user", "usr_id", "usr_cname", "where 1=1  order by usr_id");
			
			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_curr_code");
			this.dddwList("dddw_curr", "ptr_sys_idtab", "wf_id ", "wf_desc ", "where 1=1 and wf_type ='DC_CURRENCY' ");

			
        }
        catch(Exception ex) {}
    }

    
}
