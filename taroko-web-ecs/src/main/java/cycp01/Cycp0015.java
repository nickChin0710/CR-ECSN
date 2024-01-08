/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-26  V1.00.00  yash       program initial                            *
* 109-07-30  V1.00.01  Amber      Update add query_After                     *
* 109-08-26  V1.00.02  Amber      Update : apr_user check2                   *
* 111/10/28  V1.00.03  Yang Bo        sync code from mega                  *
* 112-03-23  V1.00.04  Simon      1.新增帳戶類別 "90"                        *
*                                 2.訊息類別全體卡友、...等改為 "Record04..."、...等*
******************************************************************************/

package cycp01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Cycp0015 extends BaseProc {

    CommString commString = new CommString();

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

        wp.whereStr =" where 1=1 ";
        
        if(empty(wp.itemStr("ex_msg_month")) == false){
            wp.whereStr += " and  msg_month = :ex_msg_month ";
            setString("ex_msg_month", wp.itemStr("ex_msg_month"));
        }
        
        if(empty(wp.itemStr("ex_acct_type")) == false){
            wp.whereStr += " and  acct_type = :ex_acct_type ";
            setString("ex_acct_type", wp.itemStr("ex_acct_type"));
        }
        
        if(empty(wp.itemStr("ex_msg_type")) == false){
            wp.whereStr += " and  msg_type = :ex_msg_type ";
            setString("ex_msg_type", wp.itemStr("ex_msg_type"));
        }
        
        if(empty(wp.itemStr("ex_user")) == false){
            wp.whereStr += " and  crt_user = :ex_user ";
            setString("ex_user", wp.itemStr("ex_user"));
        }
        
        
        if(empty(wp.itemStr("ex_acct_date1")) == false){
            wp.whereStr += " and  crt_date >= :ex_acct_date1 ";
            setString("ex_acct_date1", wp.itemStr("ex_acct_date1"));
        }
        
        if(empty(wp.itemStr("ex_acct_date2")) == false){
            wp.whereStr += " and  crt_date <= :ex_acct_date2 ";
            setString("ex_acct_date2", wp.itemStr("ex_acct_date2"));
        }
        
   
        
        if(empty(wp.itemStr("ex_apr_flag")) == false){
            wp.whereStr += " and  apr_flag = :ex_apr_flag ";
            setString("ex_apr_flag", wp.itemStr("ex_apr_flag"));
        }
        
/***
        if(empty(wp.itemStr("ex_cycle_type")) == false){
        	
        	if(wp.itemStr("ex_cycle_type").equals("3")){
        		  
        		 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,1,1) = 'Y' ";
        	}else if(wp.itemStr("ex_cycle_type").equals("6")) {
        		 
        		 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,2,1) = 'Y' ";
			}else if(wp.itemStr("ex_cycle_type").equals("9")) {
				
				 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,3,1) = 'Y' ";
			}else if(wp.itemStr("ex_cycle_type").equals("12")) {
				 
				 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,4,1) = 'Y' ";
			}else if(wp.itemStr("ex_cycle_type").equals("15")) {
				 
				 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,5,1) = 'Y' ";
			}else if(wp.itemStr("ex_cycle_type").equals("18")) {
				 
				 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,6,1) = 'Y' ";
			}else if(wp.itemStr("ex_cycle_type").equals("21")) {
				
				 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,7,1) = 'Y' ";
			}else if(wp.itemStr("ex_cycle_type").equals("24")) {
				
				 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,8,1) = 'Y' ";
			}else if(wp.itemStr("ex_cycle_type").equals("27")) {
				
				 wp.whereStr += " and  SUBSTR(stmt_cycle_parm,9,1) = 'Y' ";
			}

        }
***/
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
        wp.pageControl();

        wp.selectSQL = " msg_month"
	              + ", apr_flag"
	              + ", acct_type"
	              + ", msg_type"
	              + ", msg_code"
	              + ", cycle_type"
	              + ", stmt_cycle_parm"
	              + ", apr_flag"
	              + ", param1 || param2 || param3 || param4 || param5 as wk_param"
	              + ", crt_date"
	              + ", crt_user"
	              + ", mod_user"
	              ;

        wp.daoTable = "ptr_billmsg";
        wp.whereOrder=" order by msg_month,acct_type,msg_code";
        getWhereStr();

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        wp.listCount[1] = wp.dataCnt;
        wp.setPageValue();
/***
        for (int ii = 0; ii < wp.selectCnt; ii++) {
        	String ttStmtCycleParm = "";
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
        }
***/
      //  appr_Disabled("mod_user");//20200826 add


        for (int ii = 0; ii < wp.selectCnt; ii++) {
        	String ss = "";
      		ss = wp.colStr(ii,"apr_flag");
      		wp.colSet(ii,"tt_apr_flag", commString.decode(ss, ",Y,N", ",Y.已覆核,N.未覆核"));
        }

        
    }

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {

    }

   
    @Override
	public void dataProcess() throws Exception {

		String[] aaMsgMonth = wp.itemBuff("msg_month");
		String[] aaAcctType = wp.itemBuff("acct_type");
		String[] aaMsgCode = wp.itemBuff("msg_code");
		String[] opt = wp.itemBuff("opt");

	
		wp.listCount[0] = aaMsgMonth.length;
		
		if(wp.itemStr("ex_apr_flag").equals("N")){
			//未覆核
	
		// check
		int rr = -1;
		int llOk = 0, llErr = 0;
        //save
			// -update-
			rr = -1;
			for (int ii = 0; ii < opt.length; ii++) {
				rr = optToIndex(opt[ii]);
			
				if (rr < 0) {
					return;
				}
				wp.colSet(rr, "ok_flag", "");
	
					//delete ptr_msgset
					String delSq =" delete ptr_msgset  "
						       +"where apr_flag='Y' "
						       + " and mesg_month = :mesg_month  "
						       + " and acct_type = :acct_type "
					           + " and mesg_code = :mesg_code ";
					setString("mesg_month",aaMsgMonth[rr]);
					setString("acct_type",aaAcctType[rr]);
					setString("mesg_code",aaMsgCode[rr]);
					sqlExec(delSq);

			
					//update ptr_msgset
					String usSq =" update ptr_msgset set "
                               +"  apr_flag ='Y' ,"
						       +"  mod_time =sysdate "
						       +" where apr_flag='N' "
						       + " and mesg_month = :mesg_month  "
						       + " and acct_type = :acct_type "
					           + " and mesg_code = :mesg_code ";
					setString("acct_type",aaAcctType[rr]);
					setString("mesg_month",aaMsgMonth[rr]);
					setString("acct_type",aaAcctType[rr]);
					setString("mesg_code",aaMsgCode[rr]);
					sqlExec(usSq);
//					if (sql_nrow <= 0) {
//						wp.colSet(rr, "ok_flag", "X");
//						wp.colSet(rr,"ls_errmsg", "update ptr_msgset err !");
//						ll_err++;
//						sql_commit(0);
//						continue;
//					}
					
					//delete ptr_billmsg
					String delSq2 =" delete ptr_billmsg  "
						       +"where apr_flag='Y' "
						       + " and 	msg_month = :msg_month  "
						       + " and acct_type =  :acct_type "
					           + " and msg_code =  :msg_code ";
					setString("	msg_month",aaMsgMonth[rr]);
					setString("acct_type",aaAcctType[rr]);
					setString("msg_code",aaMsgCode[rr]);
					sqlExec(delSq2);

					
					//update ptr_billmsg
					String usSq2 =" update ptr_billmsg set "
                               +"  apr_flag ='Y' ,"
                               +"  apr_user = :apr_user ,"
                               +"  apr_date = :apr_date ,"
						       +"  mod_time =sysdate "
						       +" where apr_flag='N' "
						       + " and msg_month = :msg_month  "
						       + " and acct_type = :acct_type "
					           + " and msg_code = :msg_code ";
					setString("apr_user",wp.loginUser);
					setString("apr_date",getSysDate());
					setString("	msg_month",aaMsgMonth[rr]);
					setString("acct_type",aaAcctType[rr]);
					setString("msg_code",aaMsgCode[rr]);
					sqlExec(usSq2);
					if (sqlRowNum <= 0) {
						wp.colSet(rr, "ok_flag", "X");
						wp.colSet(rr,"ls_errmsg", "update ptr_billmsg err !");
						llErr++;
						sqlCommit(0);
						continue;
					}else{
						wp.colSet(rr, "ok_flag", "V");
						llOk++;
						sqlCommit(1);
					}
		  }
			 alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
		}else{
			//解覆核
	
			// check
			int rr = -1;
			int llOk = 0, llErr = 0;
	        //save
				// -update-
				rr = -1;
				for (int ii = 0; ii < opt.length; ii++) {
					rr = (int) this.toNum(opt[ii]) - 1;
					if (rr < 0) {
						return;
					}
					wp.colSet(rr, "ok_flag", "");
		
						//delete ptr_msgset
						String delSq =" delete ptr_msgset  "
							       +"where apr_flag='N' "
							       + " and mesg_month = :mesg_month  "
							       + " and acct_type = :acct_type "
						           + " and mesg_code = :mesg_code ";
						setString("mesg_month",aaMsgMonth[rr]);
						setString("acct_type",aaAcctType[rr]);
						setString("mesg_code",aaMsgCode[rr]);
						sqlExec(delSq);

				
						//update ptr_msgset
						String usSq =" update ptr_msgset set "
	                               +"  apr_flag ='N' ,"
							       +"  mod_time =sysdate "
							       +" where apr_flag='Y' "
							       + " and mesg_month = :mesg_month  "
							       + " and acct_type = :acct_type "
						           + " and mesg_code = :mesg_code ";
						setString("mesg_month",aaMsgMonth[rr]);
						setString("acct_type",aaAcctType[rr]);
						setString("mesg_code",aaMsgCode[rr]);
						sqlExec(usSq);
//						if (sql_nrow <= 0) {
//							wp.colSet(rr, "ok_flag", "X");
//							ll_err++;
//							sql_commit(0);
//							continue;
//						}
						
						//delete ptr_billmsg
						String delSq2 =" delete ptr_billmsg  "
							       +"where apr_flag='N' "
							       + " and 	msg_month = :msg_month  "
							       + " and acct_type =  :acct_type "
						           + " and msg_code =  :msg_code ";
						setString("	msg_month",aaMsgMonth[rr]);
						setString("acct_type",aaAcctType[rr]);
						setString("msg_code",aaMsgCode[rr]);
						sqlExec(delSq2);

						
						//update ptr_billmsg
						String usSq2 =" update ptr_billmsg set "
	                               +"  apr_flag ='N' ,"
	                               +"  apr_user = '' ,"
	                               +"  apr_date = '' ,"
							       +"  mod_time =sysdate "
							       +" where apr_flag='Y' "
							       + " and msg_month = :msg_month  "
							       + " and acct_type = :acct_type "
						           + " and msg_code = :msg_code ";
						setString("	msg_month",aaMsgMonth[rr]);
						setString("acct_type",aaAcctType[rr]);
						setString("msg_code",aaMsgCode[rr]);
						sqlExec(usSq2);
						if (sqlRowNum <= 0) {
							wp.colSet(rr, "ok_flag", "X");
							llErr++;
							sqlCommit(0);
							continue;
						}else{
							wp.colSet(rr, "ok_flag", "V");
							llOk++;
							sqlCommit(1);
						}
			  }
				 alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
		}

	}

    @Override
    public void initButton() {
      if(empty(wp.itemStr("ex_apr_flag")) == true){
	    	wp.colSet("ex_apr_flag", "N");
      }

      if (wp.respHtml.indexOf("_detl") > 0) {
          this.btnModeAud();
      }
    }

    @Override
    public void dddwSelect() {
        try {
            
        	wp.initOption="--";
            wp.optionKey = wp.itemStr("ex_acct_type");
            this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1  order by acct_type");
            
            wp.initOption="--";
            wp.optionKey = wp.itemStr("ex_user");
            this.dddwList("dddw_sec_user", "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id");
            
        }
        catch(Exception ex) {}
    }

   
    
}
