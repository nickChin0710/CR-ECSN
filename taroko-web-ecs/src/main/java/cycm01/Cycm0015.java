/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-24  V1.00.01  ryan       program initial                            *
* 111-10-26  V1.00.03  Machao     sync from mega & updated for project coding standard                                                                           *
* 112-03-23  V1.00.04  Simon      1.新增帳戶類別 "90"                        *
*                                 2.訊息類別全體卡友、...等改為 "Record04..."、...等*
* 112-04-06  V1.00.05  Simon      訊息類別更改為 4.VD-訊息、5.VD-紅利訊息    *
******************************************************************************/

package cycm01;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Cycm0015 extends BaseEdit {
    String kk1MsgMonth = "",kk2AcctType="",kk3MsgCode="",kk4AprFlag="";
    Cycm0015Func func;
    CommString commString = new CommString();
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

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
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
        	strAction = "U";
        	updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
            deleteFunc();
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
        }else if (eqIgno(wp.buttonCode, "SC")) {
            /* 查詢card */
            strAction = "SC";
            selectCard();
        }else if (eqIgno(wp.buttonCode, "SG")) {
            /* 查詢group */
            strAction = "SG";
            selectGroup();
        }else if (eqIgno(wp.buttonCode, "SZ")) {
            /* 查詢zip */
            strAction = "SZ";
            selectZip();
        }else if (eqIgno(wp.buttonCode, "SCU")) {
            /* card存檔 */
            strAction = "SCU";
            updateCard();
        }else if (eqIgno(wp.buttonCode, "SGU")) {
            /* group存檔 */
            strAction = "SGU";
            updateGroup();
        }else if (eqIgno(wp.buttonCode, "SZU")) {
            /* zip存檔 */
            strAction = "SZU";
            updateZip();
        } else if (eqIgno(wp.buttonCode, "D2")) {
            /* 刪除功能 */
        	 strAction = "D2";
        	 saveFunc();
        } 

        dddwSelect();
        initButton();
    }
    
    @Override
    public void initPage(){
    	wp.colSet("checked", "checked");
    }
    
    @Override
    public void insertFunc() throws Exception {
    	saveFunc();
    	if (rc == 1 && userAction==false) {
    		//if (addRetrieve)
    		kk1MsgMonth = wp.itemStr2("msg_month");
    		kk2AcctType = wp.itemStr2("acct_type");
    		kk3MsgCode = wp.itemStr2("msg_code");
    			dataRead();
    		//else clearFunc();
    		wp.colSet("insertok", "A");
    	}
    }
    //for query use only
    private boolean getWhereStr() throws Exception {
        wp.whereStr =" where 1=1 ";
       
        if(empty(wp.itemStr2("ex_msg_month")) == false){
            wp.whereStr += " and  msg_month = :ex_msg_month ";
            setString("ex_msg_month", wp.itemStr2("ex_msg_month"));
        }
        
        if(empty(wp.itemStr2("ex_acct_type")) == false){
     		wp.whereStr += " and acct_type = :ex_acct_type ";
     		setString("ex_acct_type", wp.itemStr2("ex_acct_type"));
     	}
   
        if(empty(wp.itemStr2("ex_msg_type")) == false){
        	wp.whereStr += " and msg_type = :ex_msg_type ";
     		setString("ex_msg_type", wp.itemStr2("ex_msg_type"));
        }
        
        if(!wp.itemStr2("ex_apr_flag").equals("0")){
        	wp.whereStr += " and decode(apr_flag,'','N', apr_flag) = :ex_apr_flag ";
     		setString("ex_apr_flag", wp.itemStr2("ex_apr_flag"));
        }
        
        return true;
    }

    @Override
    public void queryFunc() throws Exception {

        //-page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        wp.selectSQL = " msg_month"
	              + ", acct_type"
	              + ", msg_type"
	              + ", msg_code"
	              + ", cycle_type "
	              + ", stmt_cycle_parm"
	              + ", apr_flag"
	              + ", crt_user"
	              + ", param1"
	              + ", param2"
	              + ", param3"
	              + ", param4"
	              + ", param5"
	              ;

        wp.daoTable = "ptr_billmsg";
        wp.whereOrder=" order by msg_month,acct_type,msg_type,msg_code ";
        getWhereStr();
        pageQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        wp.listCount[1] = wp.dataCnt;
        wp.setPageValue();
        listWkdata3();
    }

    @Override
    public void querySelect() throws Exception {
       
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {

        if (empty(kk1MsgMonth)){
        	kk1MsgMonth = wp.itemStr2("kk_msg_month");
        	if(empty(kk1MsgMonth)){
        		kk1MsgMonth = itemKk("data_k1");
        	}	
        }

        if (empty(kk2AcctType)){
        	kk2AcctType = wp.itemStr2("kk_acct_type");
        	if (empty(kk2AcctType)){
        		kk2AcctType = itemKk("data_k2");
        	}
        }
        
        if (empty(kk3MsgCode)){
        	kk3MsgCode = wp.itemStr2("kk_msg_code");
        	if (empty(kk3MsgCode)){
        		kk3MsgCode = itemKk("data_k3");
        	}
        }
        
      		String sqlSelect="select count(*) as ll_cnt "
    				+ " from ptr_billmsg "
    				+ " where msg_month = :msg_month "
    				+ " and acct_type = :acct_type"
    				+ " and msg_code = :msg_code "
    				+ " and apr_flag='N'";
    		setString("msg_month",kk1MsgMonth);
    		setString("acct_type",kk2AcctType);
    		setString("msg_code",kk3MsgCode);
    		sqlSelect(sqlSelect);
    		if(sqlNum("ll_cnt")>0){
    			 kk4AprFlag = "N";
    		}else{
    			 kk4AprFlag = itemKk("data_k4");
    		}
        String kk5MsgType = wp.itemStr2("kk_msg_type");
        
        wp.selectSQL = " hex(rowid) as rowid "
        		  + ", msg_month"
	              + ", acct_type"
	              + ", msg_type"
	              + ", msg_code"
	              + ", cycle_type "
	              + ", stmt_cycle_parm"
	              + ", apr_flag"
	              + ", crt_user"
	              + ", crt_date"
	              + ", apr_user"
	              + ", apr_date"
	              + ", param1"
	              + ", param2"
	              + ", param3"
	              + ", param4"
	              + ", param5"
	              + ", mod_seqno "
	              ;
        
        wp.daoTable = "ptr_billmsg";
        wp.whereStr = "where 1=1";
        if(!empty(kk5MsgType)){
        	 wp.whereStr += " and  msg_type = :msg_type ";
             setString("msg_type", kk5MsgType);
        }
        if(!empty(kk1MsgMonth)){
       	 wp.whereStr += " and  msg_month = :msg_month ";
            setString("msg_month", kk1MsgMonth);
        }
        if(!empty(kk2AcctType)){
          	 wp.whereStr += " and  acct_type = :acct_type ";
               setString("acct_type", kk2AcctType);
        }
        if(!empty(kk3MsgCode)){
         	 wp.whereStr += " and  msg_code = :msg_code ";
              setString("msg_code", kk3MsgCode);
        }
        if(!empty(kk4AprFlag)){
        	 wp.whereStr += " and  apr_flag = :apr_flag ";
             setString("apr_flag", kk4AprFlag);
        }
        pageSelect();
        
        if (sqlNotFind()) {
            alertErr("查無資料");
            return;
        }
        listWkdata();
    }

    @Override
    public void saveFunc() throws Exception {
    	func = new Cycm0015Func(wp);

    	if(strAction.equals("D2")){
    		func.dbDelete2();
    		wp.colSet("deleteOK", "Y");
    		return;
    	}
 
    	if(ofValidation()!=1){
    		return;
    	}
    	
        rc = func.dbSave(strAction);
        log(func.getMsg());
        if (rc != 1) {
            alertErr2(func.getMsg());
        }
        this.sqlCommit(rc);
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
            if (wp.respHtml.indexOf("_detl") > 0)
                wp.optionKey = wp.itemStr2("kk_acct_type");
            else
                wp.optionKey = wp.itemStr2("ex_acct_type");
          //this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 and chin_name<>'' order by acct_type ");
            if (strAction.equals("SG")) {
            	//wp.initOption = "--";
            	this.dddwList("dddw_group", "ptr_group_code", "group_code", "group_name",
					"where 1=1 order by group_code ");
            }
			
            if (strAction.equals("SZ")) {
					//wp.initOption = "--";
					this.dddwList("dddw_zip", "ptr_zipcode", "zip_code", "zip_city",
							"where 1=1 order by zip_code ");
			}
		}
        catch(Exception ex) {}
    }
    
    
    public void selectCard() throws Exception {
    	this.selectNoLimit();
    	strAction = "SC";
    	kk1MsgMonth = wp.itemStr2("kk1_msg_month");
    	if(empty(kk1MsgMonth)){
    		kk1MsgMonth = itemKk("mesg_month_kk");
    	}
    	kk2AcctType = wp.itemStr2("kk2_acct_type");
    	if(empty(kk2AcctType)){
    		 kk2AcctType = itemKk("acct_type_kk");
    	}
    	kk3MsgCode = wp.itemStr2("kk3_msg_code");
    	if(empty(kk3MsgCode)){
    		 kk3MsgCode = itemKk("mesg_code_kk");
    	}
    	kk4AprFlag = "N";
       
        wp.selectSQL ="hex(rowid) as rowid1 "
	              + ", mesg_type"
	              + ", set_data"
	              ;
        wp.daoTable = "ptr_msgset";
        wp.whereStr = " where 1=1 ";
        wp.whereStr  += " and  mesg_month = :kk1_msg_month "
        			+ " and acct_type = :kk2_acct_type"
        			+ " and mesg_type = '2'"
        			+ " and mesg_code = :kk3_msg_code"
        			+ " and decode(apr_flag,'','N', apr_flag) = :kk4_apr_flag ";
        setString("kk1_msg_month", kk1MsgMonth);
     	setString("kk2_acct_type", kk2AcctType);
     	setString("kk3_msg_code", kk3MsgCode);
     	setString("kk4_apr_flag", kk4AprFlag);
        pageQuery();
        wp.setListCount(1);
		wp.notFound = "";
		listWkdata2();
    }
    
    
    
    public void selectGroup() throws Exception {
    	this.selectNoLimit();
    	strAction = "SG";
    	kk1MsgMonth = wp.itemStr2("kk1_msg_month");
    	if(empty(kk1MsgMonth)){
    		kk1MsgMonth = itemKk("mesg_month_kk");
    	}
    	kk2AcctType = wp.itemStr2("kk2_acct_type");
    	if(empty(kk2AcctType)){
    		 kk2AcctType = itemKk("acct_type_kk");
    	}
    	kk3MsgCode = wp.itemStr2("kk3_msg_code");
    	if(empty(kk3MsgCode)){
    		 kk3MsgCode = itemKk("mesg_code_kk");
    	}
    	kk4AprFlag = "N";
       
        wp.selectSQL ="hex(rowid) as rowid2 "
	              + ", mesg_type"
	              + ", set_data"
	              ;
        wp.daoTable = "ptr_msgset";
        wp.whereStr = " where 1=1 ";
        wp.whereStr  += " and  mesg_month = :kk1_msg_month "
        			+ " and acct_type = :kk2_acct_type"
        			+ " and mesg_type = '3'"
        			+ " and mesg_code = :kk3_msg_code"
        			+ " and decode(apr_flag,'','N', apr_flag) = :kk4_apr_flag ";
        setString("kk1_msg_month", kk1MsgMonth);
     	setString("kk2_acct_type", kk2AcctType);
     	setString("kk3_msg_code", kk3MsgCode);
     	setString("kk4_apr_flag", kk4AprFlag);
        pageQuery();
        wp.setListCount(1);
		wp.notFound = "";
		listWkdata2();
    }
    
    void selectZip() throws Exception{
    	this.selectNoLimit();
    	strAction = "SZ";
    	kk1MsgMonth = wp.itemStr2("kk1_msg_month");
    	if(empty(kk1MsgMonth)){
    		kk1MsgMonth = itemKk("mesg_month_kk");
    	}
    	kk2AcctType = wp.itemStr2("kk2_acct_type");
    	if(empty(kk2AcctType)){
    		 kk2AcctType = itemKk("acct_type_kk");
    	}
    	kk3MsgCode = wp.itemStr2("kk3_msg_code");
    	if(empty(kk3MsgCode)){
    		 kk3MsgCode = itemKk("mesg_code_kk");
    	}
    	kk4AprFlag = "N";
       
        wp.selectSQL ="hex(rowid) as rowid3 "
	              + ", mesg_type"
	              + ", set_data"
	              ;
        wp.daoTable = "ptr_msgset";
        wp.whereStr = " where 1=1 ";
        wp.whereStr  += " and  mesg_month = :kk1_msg_month "
        			+ " and acct_type = :kk2_acct_type"
        			+ " and mesg_type = '4'"
        			+ " and mesg_code = :kk3_msg_code"
        			+ " and decode(apr_flag,'','N', apr_flag) = :kk4_apr_flag ";
        setString("kk1_msg_month", kk1MsgMonth);
     	setString("kk2_acct_type", kk2AcctType);
     	setString("kk3_msg_code", kk3MsgCode);
     	setString("kk4_apr_flag", kk4AprFlag);
        pageQuery();
        wp.setListCount(1);
		wp.notFound = "";
		listWkdata2();
    }
    
    public void updateCard() throws Exception {
    	 func =new Cycm0015Func(wp);
    	 int llErr = 0;
   	        String[] aaSetData = wp.itemBuff("set_data");
			String[] aaRowid1 = wp.itemBuff("rowid1");
			String[] aaOpt = wp.itemBuff("opt");
			wp.listCount[0] = aaSetData.length;
			//-insert-
			
			for (int ll = 0; ll < aaSetData.length; ll++) {
			func.varsSet("aa_set_data", aaSetData[ll]);
			func.varsSet("aa_rowid1", aaRowid1[ll]);
			func.varsSet("aa_mesg_type", "2");
		
				//-delete no-approve-
			if (!empty(aaRowid1[ll])) {
				if (func.dbDeleteCard() < 0) {
					alertErr(func.getMsg());
					llErr++;
				}
			}
			// -option-ON-
			if (checkBoxOptOn(ll, aaOpt)) {
				continue;
			}

			if (func.dbInsertCard() == 1) {
			} else {
				llErr++;
			}
		}
				if(llErr>0){
					sqlCommit(0);
					alertMsg("資料存檔處理失敗");
					return;
				}
				sqlCommit(1);
		        selectCard();
		        alertMsg("資料存檔處理完成");
    }
    
   
    
    public void updateGroup() throws Exception {
    	func =new Cycm0015Func(wp);
   	 	int llErr = 0;
  	        String[] aaSetData = wp.itemBuff("set_data");
			String[] aaRowid2 = wp.itemBuff("rowid2");
			String[] aaOpt = wp.itemBuff("opt");
			wp.listCount[0] = aaSetData.length;
			//-insert-
			
			for (int ll = 0; ll < aaSetData.length; ll++) {

			func.varsSet("aa_set_data", aaSetData[ll]);
			func.varsSet("aa_rowid2", aaRowid2[ll]);
			func.varsSet("aa_mesg_type", "3");
			
				//-delete no-approve-
			if (!empty(aaRowid2[ll])) {
				if (func.dbDeleteGroup() < 0) {
					alertErr(func.getMsg());
					llErr++;
				}
			}
			// -option-ON-
			if (checkBoxOptOn(ll, aaOpt)) {
				continue;
			}

			if (func.dbInsertGroup() == 1) {
			} else {
				llErr++;
			}
		}
			if(llErr>0){
				sqlCommit(0);
				alertMsg("資料存檔處理失敗");
				return;
			}
			sqlCommit(1);
			selectGroup();
	        alertMsg("資料存檔處理完成");       
    }
    void updateZip() throws Exception{
    	func =new Cycm0015Func(wp);
   	 	int llErr = 0;
  	        String[] aaSetData = wp.itemBuff("set_data");
			String[] aaRowid3 = wp.itemBuff("rowid3");
			String[] aaOpt = wp.itemBuff("opt");
			wp.listCount[0] = aaSetData.length;
			//-insert-
			
			for (int ll = 0; ll < aaSetData.length; ll++) {

			func.varsSet("aa_set_data", aaSetData[ll]);
			func.varsSet("aa_rowid3", aaRowid3[ll]);
			func.varsSet("aa_mesg_type", "4");
			
				//-delete no-approve-
			if (!empty(aaRowid3[ll])) {
				if (func.dbDeleteZip() < 0) {
					alertErr(func.getMsg());
					llErr++;
				}
			}
			// -option-ON-
			if (checkBoxOptOn(ll, aaOpt)) {
				continue;
			}

			if (func.dbInsertZip() == 1) {
			} else {
				llErr++;
			}
		}

			if(llErr>0){
				sqlCommit(0);
				alertMsg("資料存檔處理失敗");
				return;
			}
			sqlCommit(1);
			 selectZip();
	        alertMsg("資料存檔處理完成");  
		       
    }
    
    void listWkdata() throws Exception{
    	String ss="";
    	ss =wp.colStr("msg_type");
    //wp.colSet("tt_msg_type", commString.decode(ss, ",1,2,3,4", ",1.全體卡友,2.卡別,3.團體代號,4.郵遞區號"));
			if("1".equals(ss)) {
				wp.colSet("tt_msg_type", "1.Record04 (訊息項 FOR 簡訊前)");
			} else if("2".equals(ss)) {
				wp.colSet("tt_msg_type", "2.Record05 (訊息項 FOR 簡訊後)");
			} else if("5".equals(ss)) {
				wp.colSet("tt_msg_type", "5.Record04 (訊息項 FOR 簡訊-VD)");
			} 

    	ss =wp.colStr("acct_type");
    //String sqlSelect ="select acct_type||'_'||chin_name as tt_acct_type from ptr_acct_type where acct_type = :acct_type ";
    //setString("acct_type",ss);
    //sqlSelect(sqlSelect);
    //wp.colSet("tt_acct_type", sqlStr("tt_acct_type"));
			if("01".equals(ss)) {
				wp.colSet("tt_acct_type", "01_一般卡");
			} else if("03".equals(ss)) {
				wp.colSet("tt_acct_type", "03_商務卡");
			} else if("06".equals(ss)) {
				wp.colSet("tt_acct_type", "06_政府網路採購卡");
			} else if("90".equals(ss)) {
				wp.colSet("tt_acct_type", "90_Visa 金融卡");
			} 

    	ss = wp.colStr("stmt_cycle_parm");
    	String stmtCycleParm3 = strMid(ss,0,1);
    	String stmtCycleParm6 = strMid(ss,1,1);
    	String stmtCycleParm9 = strMid(ss,2,1);
    	String stmtCycleParm12 = strMid(ss,3,1);
    	String stmtCycleParm15 = strMid(ss,4,1);
    	String stmtCycleParm18 = strMid(ss,5,1);
    	String stmtCycleParm21 = strMid(ss,6,1);
    	String stmtCycleParm24 = strMid(ss,7,1);
    	String stmtCycleParm27 = strMid(ss,8,1);
  
/***
    	if(wp.colStr("cycle_type").equals("02")){
    		if(stmtCycleParm3.equals("Y")) stmtCycleParm3 = "N"; else stmtCycleParm3 = "Y";
    		if(stmtCycleParm6.equals("Y")) stmtCycleParm6 = "N"; else stmtCycleParm6 = "Y";
    		if(stmtCycleParm9.equals("Y")) stmtCycleParm9 = "N"; else stmtCycleParm9 = "Y";
    		if(stmtCycleParm12.equals("Y")) stmtCycleParm12 = "N"; else stmtCycleParm12 = "Y";
    		if(stmtCycleParm15.equals("Y")) stmtCycleParm15 = "N"; else stmtCycleParm15 = "Y";
    		if(stmtCycleParm18.equals("Y")) stmtCycleParm18 = "N"; else stmtCycleParm18 = "Y";
    		if(stmtCycleParm21.equals("Y")) stmtCycleParm21 = "N"; else stmtCycleParm21 = "Y";
    		if(stmtCycleParm24.equals("Y")) stmtCycleParm24 = "N"; else stmtCycleParm24 = "Y";
    		if(stmtCycleParm27.equals("Y")) stmtCycleParm27 = "N"; else stmtCycleParm27 = "Y";
    	}
    	wp.colSet("stmt_cycle_parm_3",stmtCycleParm3);
    	wp.colSet("stmt_cycle_parm_6",stmtCycleParm6);
    	wp.colSet("stmt_cycle_parm_9",stmtCycleParm9);
    	wp.colSet("stmt_cycle_parm_12",stmtCycleParm12);
    	wp.colSet("stmt_cycle_parm_15",stmtCycleParm15);
    	wp.colSet("stmt_cycle_parm_18",stmtCycleParm18);
    	wp.colSet("stmt_cycle_parm_21",stmtCycleParm21);
    	wp.colSet("stmt_cycle_parm_24",stmtCycleParm24);
    	wp.colSet("stmt_cycle_parm_27",stmtCycleParm27);
***/
    	
    	ss = wp.colStr("apr_flag");
    	wp.colSet("tt_apr_flag", commString.decode(ss, ",Y,N", ",已覆核,未覆核"));
    	if(!kk4AprFlag.equals("N")){
    		wp.colSet("rowid", "");
    	}
    }
    
    void listWkdata2() throws Exception{
    	String ss="",sqlSelect="";
    	wp.colSet("kk1_msg_month", kk1MsgMonth);
    	wp.colSet("kk2_acct_type", kk2AcctType);
    	wp.colSet("kk3_msg_code", kk3MsgCode);
    	wp.colSet("kk4_apr_flag", kk4AprFlag);
    	String kkMsgType = wp.itemStr2("msg_type");
    	if(empty(kkMsgType)){
    		kkMsgType = wp.itemStr2("kk_msg_type");
    	}
    	wp.colSet("kk_msg_type", kkMsgType);
    //wp.colSet("tt_kk_msg_type", commString.decode(kkMsgType, ",1,2,3,4", ",1.全體卡友,2.卡別,3.團體代號,4.郵遞區號"));
			if("1".equals(kkMsgType)) {
				wp.colSet("tt_kk_msg_type", "1.Record04 (訊息項 FOR 簡訊前)");
			} else if("2".equals(kkMsgType)) {
				wp.colSet("tt_kk_msg_type", "2.Record05 (訊息項 FOR 簡訊後)");
			} else if("4".equals(kkMsgType)) {
				wp.colSet("tt_kk_msg_type", "4.Record04 (訊息項 FOR VD-訊息)");
			} else if("5".equals(kkMsgType)) {
				wp.colSet("tt_kk_msg_type", "5.Record06 (訊息項 FOR VD-紅利訊息)");
			} 

			for (int ii = 0; ii < wp.selectCnt; ii++) {
				ss = wp.colStr(ii, "set_data");
				if(strAction.equals("SG")){
					sqlSelect = "select group_code||'_'||group_name as tt_set_data from ptr_group_code where group_code = :tt_set_data";
				}else if(strAction.equals("SZ")){
					sqlSelect = "select zip_code||'_'||zip_city as tt_set_data from ptr_zipcode where zip_code = :tt_set_data";
				}else if(strAction.equals("SC")){
					wp.colSet(ii,"tt_set_data", commString.decode(ss, ",C,G,P,S,I", ",C.普卡,G.金卡,P.白金卡,S.卓越卡,I.頂級卡"));
					continue;
				}
				setString("tt_set_data", ss);
				sqlSelect(sqlSelect);
				if (sqlRowNum > 0) {
					ss = sqlStr("tt_set_data");
				}
				wp.colSet(ii,"tt_set_data",ss );
				
			}
			wp.colSet("selectCnt",wp.selectCnt );
    }
    
    int ofValidation() throws Exception{
    	int err = 0;
    	if(strAction.equals("D")){
    		if(wp.itemStr2("apr_flag").equals("Y")){
    			alertErr("已覆核不可刪除");
    			return -1;
    		}
    	}
    	if(strAction.equals("A")){
    		String msg="";
    		String kkMsgMonth = wp.itemStr2("msg_month");
    	    if(empty(kkMsgMonth)){
    	      kkMsgMonth = wp.itemStr2("kk_msg_month");
    	    }
    	    String kkMsgCode = wp.itemStr2("msg_code");
            if(empty(kkMsgCode)){
            	kkMsgCode = wp.itemStr2("kk_msg_code");
            }
            String kkAcctType = wp.itemStr2("acct_type");
            if(empty(kkAcctType)){
            	kkAcctType = wp.itemStr2("kk_acct_type");
            }
            String kkMsgType = wp.itemStr2("msg_type");
            if(empty(kkMsgType)){
            	kkMsgType = wp.itemStr2("kk_msg_type");
            }

    		if(empty(kkMsgMonth)){
				msg +="帳單年月,";
    		}
    		if(empty(kkAcctType)){
				msg +="帳戶類別,";
    		}
    		if(empty(kkMsgType)){
				msg +="簡訊類別,";
    		}
    		if(empty(kkMsgCode)){
				msg +="列印順序,";
    		}
    		if(!empty(msg)){
    			alertErr(msg+"不可空白");
    			return -1;
    		}
    	}
    	
    	if(!strAction.equals("D")){  		
			if (empty(wp.itemStr2("param1")) && empty(wp.itemStr2("param2")) && empty(wp.itemStr2("param3"))
					&& empty(wp.itemStr2("param4")) && empty(wp.itemStr2("param5"))) {
				alertErr("簡訊內容不可空白");
				return -1;
			}
			String param1 = strMid(wp.itemStr2("param1"),0,20);
			String param2 = strMid(wp.itemStr2("param2"),0,20);
			String param3 = strMid(wp.itemStr2("param3"),0,20);
			String param4 = strMid(wp.itemStr2("param4"),0,20);
			String param5 = strMid(wp.itemStr2("param5"),0,20);
			if(!check(param1)){
    			err++;
    			wp.colSet("color1", "style='background-color:pink'");
			}else{
				wp.colSet("color1", "");
			}
			if(!check(param2)){
    			err++;
    			wp.colSet("color2", "style='background-color:pink'");
			}else{
				wp.colSet("color2", "");
			}
			if(!check(param3)){
    			err++;
    			wp.colSet("color3", "style='background-color:pink'");
			}else{
				wp.colSet("color3", "");
			}
			if(!check(param4)){
    			err++;
    			wp.colSet("color4", "style='background-color:pink'");
			}else{
				wp.colSet("color4", "");
			}
			if(!check(param5)){
    			err++;
    			wp.colSet("color5", "style='background-color:pink'");
			}else{
				wp.colSet("color5", "");
			}
			if(err>0){
				alertErr("簡訊內容,每行前20個字元不可有特殊符號");
				return -1;
			}
    	}
    	
    	if(strAction.equals("A")&&!wp.itemStr2("apr_flag").equals("Y")){
    		if(wp.itemNum("kk_msg_code")<=0){
    			alertErr("列印順序 須大於 0");
    			return -1;
    		}
    		String kkMsgCode = wp.itemStr2("kk_msg_code");
    		String sqlSelect="select count(*) as ll_cnt from ptr_billmsg where msg_month =:kk_msg_month and to_number(msg_code)= :kk_msg_code ";
    		setString("kk_msg_month",wp.itemStr2("kk_msg_month"));
    		setString("kk_msg_code",kkMsgCode);
    		sqlSelect(sqlSelect);
    		if(this.toNum(sqlStr("ll_cnt"))>0){
    			alertErr("當月-列印順序 不可重複");
    			return -1;
    		}

    	}
    	String stmtCycleParm3 = "",stmtCycleParm6="",stmtCycleParm9="",
    			stmtCycleParm12="",stmtCycleParm15="",stmtCycleParm18="",
    			stmtCycleParm21="",stmtCycleParm24="",stmtCycleParm27="";
/***
    	if(wp.itemStr2("cycle_type").equals("01")){
    		stmtCycleParm3 = wp.itemStr2("stmt_cycle_parm_3");
        	if(empty(stmtCycleParm3)){
        		stmtCycleParm3="N";
        	}
        	stmtCycleParm6 = wp.itemStr2("stmt_cycle_parm_6");
        	if(empty(stmtCycleParm6)){
        		stmtCycleParm6="N";
        	}
        	stmtCycleParm9 = wp.itemStr2("stmt_cycle_parm_9");
        	if(empty(stmtCycleParm9)){
        		stmtCycleParm9="N";
        	}
        	stmtCycleParm12 = wp.itemStr2("stmt_cycle_parm_12");
        	if(empty(stmtCycleParm12)){
        		stmtCycleParm12="N";
        	}
        	stmtCycleParm15 = wp.itemStr2("stmt_cycle_parm_15");
        	if(empty(stmtCycleParm15)){
        		stmtCycleParm15="N";
        	}
        	stmtCycleParm18 = wp.itemStr2("stmt_cycle_parm_18");
        	if(empty(stmtCycleParm18)){
        		stmtCycleParm18="N";
        	}
        	stmtCycleParm21 = wp.itemStr2("stmt_cycle_parm_21");
        	if(empty(stmtCycleParm21)){
        		stmtCycleParm21="N";
        	}
        	stmtCycleParm24 = wp.itemStr2("stmt_cycle_parm_24");
        	if(empty(stmtCycleParm24)){
        		stmtCycleParm24="N";
        	}
        	stmtCycleParm27 = wp.itemStr2("stmt_cycle_parm_27");
        	if(empty(stmtCycleParm27)){
        		stmtCycleParm27="N";
        	}
    	}
    	if(wp.itemStr2("cycle_type").equals("02")){
    		stmtCycleParm3 = wp.itemStr2("stmt_cycle_parm_3");
        	if(empty(stmtCycleParm3)){
        		stmtCycleParm3="Y";
        	}else{
        		stmtCycleParm3="N";
        	}
        	stmtCycleParm6 = wp.itemStr2("stmt_cycle_parm_6");
        	if(empty(stmtCycleParm6)){
        		stmtCycleParm6="Y";
        	}else{
        		stmtCycleParm6="N";
        	}
        	stmtCycleParm9 = wp.itemStr2("stmt_cycle_parm_9");
        	if(empty(stmtCycleParm9)){
        		stmtCycleParm9="Y";
        	}else{
        		stmtCycleParm9="N";
        	}
        	stmtCycleParm12 = wp.itemStr2("stmt_cycle_parm_12");
        	if(empty(stmtCycleParm12)){
        		stmtCycleParm12="Y";
        	}else{
        		stmtCycleParm12="N";
        	}
        	stmtCycleParm15 = wp.itemStr2("stmt_cycle_parm_15");
        	if(empty(stmtCycleParm15)){
        		stmtCycleParm15="Y";
        	}else{
        		stmtCycleParm15="N";
        	}
        	stmtCycleParm18 = wp.itemStr2("stmt_cycle_parm_18");
        	if(empty(stmtCycleParm18)){
        		stmtCycleParm18="Y";
        	}else{
        		stmtCycleParm18="N";
        	}
        	stmtCycleParm21 = wp.itemStr2("stmt_cycle_parm_21");
        	if(empty(stmtCycleParm21)){
        		stmtCycleParm21="Y";
        	}else{
        		stmtCycleParm21="N";
        	}
        	stmtCycleParm24 = wp.itemStr2("stmt_cycle_parm_24");
        	if(empty(stmtCycleParm24)){
        		stmtCycleParm24="Y";
        	}else{
        		stmtCycleParm24="N";
        	}
        	stmtCycleParm27 = wp.itemStr2("stmt_cycle_parm_27");
        	if(empty(stmtCycleParm27)){
        		stmtCycleParm27="Y";
        	}else{
        		stmtCycleParm27="N";
        	}
    	}
***/
    	
    	String stmtCycleParm = stmtCycleParm3+stmtCycleParm6+stmtCycleParm9+stmtCycleParm12
    			+stmtCycleParm15+stmtCycleParm18+stmtCycleParm21+stmtCycleParm24+stmtCycleParm27;
    	func.varsSet("stmt_cycle_parm", stmtCycleParm);
    	return 1;
    }
    
    void  listWkdata3(){
    	String ss = "",ttStmtCycleParm="";
    	for (int ii = 0; ii < wp.selectCnt; ii++) {
    		ttStmtCycleParm = "";
    		ss =wp.colStr(ii,"msg_type");
    	//wp.colSet(ii,"tt_msg_type", commString.decode(ss, ",1,2,3,4", ",1.全體卡友,2.卡別,3.團體代號,4.郵遞區號"));
	  		if("1".equals(ss)) {
		  		wp.colSet(ii,"tt_msg_type", "1.Record04 (訊息項 FOR 簡訊前)");
		  	} else if("2".equals(ss)) {
			  	wp.colSet(ii,"tt_msg_type", "2.Record05 (訊息項 FOR 簡訊後)");
			  } else if("5".equals(ss)) {
			  	wp.colSet(ii,"tt_msg_type", "5.Record04 (訊息項 FOR 簡訊-VD)");
			  } 

    		ss = wp.colStr(ii,"apr_flag");
    		wp.colSet(ii,"tt_apr_flag", commString.decode(ss, ",Y,N", ",Y.已覆核,N.未覆核"));
/***
    		ss = wp.colStr(ii,"cycle_type");
    		wp.colSet(ii,"tt_cycle_type", commString.decode(ss, ",01,02", ",指定,排除"));
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),0,1).equals("Y")) ttStmtCycleParm += "3"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),1,1).equals("Y")) ttStmtCycleParm += "6"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),2,1).equals("Y")) ttStmtCycleParm += "9"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),3,1).equals("Y")) ttStmtCycleParm += "12"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),4,1).equals("Y")) ttStmtCycleParm += "15"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),5,1).equals("Y")) ttStmtCycleParm += "18"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),6,1).equals("Y")) ttStmtCycleParm += "21"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),7,1).equals("Y")) ttStmtCycleParm += "24"+",";
    			if(strMid(wp.colStr(ii,"stmt_cycle_parm"),8,1).equals("Y")) ttStmtCycleParm += "27"+",";
    			wp.colSet(ii,"tt_stmt_cycle_parm", strMid(ttStmtCycleParm,0,ttStmtCycleParm.length()-1));
***/    	
    	}
    }
    
    public boolean check(String str){
		Pattern p = Pattern.compile("[/.*]");
		Matcher m = p.matcher(str);
		boolean match = m.find();
		if (match) {
			return false;
		}
		return true;
    }
}

