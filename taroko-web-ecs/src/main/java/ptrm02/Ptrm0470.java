/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-08-11   V1.00.02  JustinWu    add upload and ajax       
* 109-09-07  V1.00.03  JustinWu    commentary check id_no                    
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *                     *
******************************************************************************/
package ptrm02;
import java.util.ArrayList;
/** ptrm0470 帳戶 VIP 設定參數維護
 * 2019-1230:  Alex  add group_name 
 * 2019-1226:  JH    delete-approve
 * 2019-1212:  Alex  bug fix
 * 2018-0116:	JH		modify
 * 
 * */
import java.util.Arrays;

import busi.ecs.CommBusiCrd;
import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import ofcapp.EcsApprove;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Ptrm0470 extends BaseEdit {
  Ptrm0470Func func;
  String seqNo = "", aprFlag = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      initValue();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      detl2Read();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "U2")) {
      strAction = "U";
      detl2Save();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
    	uploadFile();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
    	checkIdnoAJAX();
    }

    dddwSelect();
    initButton();
  }

  void initValue() {
    wp.colSet("class_code_flag", "1");
    wp.colSet("pd_rate_mm", "0");
    wp.colSet("pd_rate_mm_flag", "1");
    wp.colSet("pd_rate_m1_flag", "1");
    wp.colSet("pay_rate_mm", "0");
    wp.colSet("pay_rate_mm_flag", "1");
    wp.colSet("pay_rate_m1_flag", "1");
    wp.colSet("card_since1", "0");
    wp.colSet("card_since2", "999999");
    wp.colSet("limit_amt1", "0");
    wp.colSet("limit_amt2", "99999999");
    wp.colSet("purch_amt_mm", "0");
    wp.colSet("purch_amt1", "0");
    wp.colSet("purch_amt2", "99999999");
    wp.colSet("purch_amt_num", "99999999");
    wp.colSet("bank_rela_flag", "N");
    wp.colSet("overdue_mm", "0");
    wp.colSet("overdue_times1", "0");
    wp.colSet("overdue_times2", "99999");
    wp.colSet("rc_use_mm", "0");
    wp.colSet("rc_use_mm_rate1", "0");
    wp.colSet("rc_use_mm_rate2", "9999");
    wp.colSet("rc_use_m1_rate1", "0");
    wp.colSet("rc_use_m1_rate2", "9999");
    wp.colSet("pre_cash_mm", "0");
    wp.colSet("pre_cash_times1", "0");
    wp.colSet("pre_cash_times2", "99999");
    wp.colSet("limit_use_mm", "0");
    wp.colSet("limit_use_mm_rate1", "0");
    wp.colSet("limit_use_mm_rate2", "9999");
    wp.colSet("limit_use_m1_rate1", "0");
    wp.colSet("limit_use_m1_rate2", "9999");
  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("建檔日期起迄：輸入錯誤");
      return;
    }

    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_vip_code"), "vip_code")
        + sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=");

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  void detl2Read() throws Exception {
    seqNo = wp.itemStr("seq_no");
    aprFlag = wp.itemStr("apr_flag");
    if (eqIgno(aprFlag, "Y")) {
      String sql1 = "select count(*) as db_1 from ptr_vip_code" + " where 1=1"
          + sqlCol(seqNo, "seq_no") + " and apr_flag <>'Y'";
      sqlSelect(sql1);
      if (this.sqlInt("db_1") > 0) {
        aprFlag = "N";
        wp.colSet("apr_flag", "N");
      }
    }

    String lsType = getDataType();
    if (lsType.indexOf("I") >= 0) {
      detl2ReadI99(lsType);
    } else if (lsType.indexOf("E") >= 0) {
      detl2ReadE99(lsType);
    }
  }

  void detl2ReadI99(String aType) throws Exception {
    wp.selectSQL = "data_code," + "hex(rowid) as rowid";
    if (eqIgno(aType, "I07"))
      wp.selectSQL +=
          ", (select group_name from ptr_group_code where group_code = data_code) as tt_data_code ";
    wp.daoTable = "ptr_vip_data";
    wp.whereStr =
        " where 1=1" + sqlCol(aType, "data_type") + sqlCol(seqNo, "seq_no") + sqlCol(aprFlag, "apr_flag");
    wp.whereOrder = " order by data_code";
    pageQuery();
    if (sqlRowNum == 0) {
      this.selectOK();
    }

    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);

  }

  void detl2ReadE99(String aType) throws Exception {

    wp.selectSQL = "data_code," + "hex(rowid) as rowid";
    wp.daoTable = "ptr_vip_data";
    wp.whereStr =
        " where 1=1" + sqlCol(seqNo, "seq_no") + sqlCol(aType, "data_type") + sqlCol(aprFlag, "apr_flag");
    wp.whereOrder = " order by data_code";
    pageQuery();
    if (sqlRowNum == 0) {
      this.selectOK();
    }

    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_I07") > 0) {
        wp.optionKey = wp.colStr("ex_data_code");
        dddwList("dddw_data_value", "ptr_group_code", "group_code", "group_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    // -action-code-
    if (wp.respHtml.indexOf("_E03") > 0) {
      wp.colSet("dddw_action", ecsfunc.DeCodeRsk.trialAction("", true));
    }

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "vip_code, " + "seq_no, " + "vip_desc, " + "crt_date, " + "crt_user, " + "apr_flag";
    wp.daoTable = "ptr_vip_code";
    wp.whereOrder = " order by seq_no Asc ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    seqNo = wp.itemStr("data_k1");
    aprFlag = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(seqNo)) {
      seqNo = wp.itemStr("seq_no");
    }
    if (empty(aprFlag)) {
      aprFlag = wp.itemStr("apr_flag");
    }
    wp.selectSQL = "A.*, hex(rowid) as rowid," + "substrb(incl_cond,1,1) as db_incl_class_code,"
        + "substrb(incl_cond,2,1) as db_incl_pd_rate,"
        + "substrb(incl_cond,3,1) as db_incl_pay_rate,"
        + "substrb(incl_cond,4,1) as db_incl_card_since,"
        + "substrb(incl_cond,5,1) as db_incl_limit_amt,"
        + "substrb(incl_cond,6,1) as db_incl_purch_amt,"
        + "substrb(incl_cond,7,1) as db_incl_bank_rela," + "substrb(incl_cond,8,1) as db_incl_list,"
        + "substrb(incl_cond,9,1) as db_incl_group_code,"
        + "substrb(incl_cond,10,1) as db_incl_reason_down,"
        + "substrb(excl_cond,1,1) as db_excl_block," + "substrb(excl_cond,2,1) as db_excl_hi_risk,"
        + "substrb(excl_cond,3,1) as db_excl_overdue," + "substrb(excl_cond,4,1) as db_excl_rc_use,"
        + "substrb(excl_cond,5,1) as db_excl_pre_cash,"
        + "substrb(excl_cond,6,1) as db_excl_limit_use,"
        + "substrb(excl_cond,7,1) as db_excl_action_code ,"
        + "substrb(excl_cond,8,1) as db_excl_list," + "substrb(excl_cond,9,1) as db_excl_exblock,"
        + "to_char(mod_time,'yyyymmdd') as mod_date" //
    ;
    wp.daoTable = "ptr_vip_code A";
    wp.whereStr = " where 1=1" + sqlCol(seqNo, "seq_no")
    // + sql_col(kk2, "apr_flag")
        + " order by decode(apr_flag,'Y',9,1)" + commSqlStr.rownum(1);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + seqNo);
      return;
    }
    if (wp.colEq("apr_flag", "N")) {
      wp.alertMesg("資料待覆核");
    }
    // --
    dataRead_After();
  }

  void dataRead_After() {
    // --
    String sql1 = "select sum(decode(data_type,'I01',1,0)) as cnt_I01,"
        + " sum(decode(data_type,'I02',1,0)) as cnt_I02,"
        + " sum(decode(data_type,'I03',1,0)) as cnt_I03,"
        + " sum(decode(data_type,'I04',1,0)) as cnt_I04,"
        + " sum(decode(data_type,'I05',1,0)) as cnt_I05,"
        + " sum(decode(data_type,'I06',1,0)) as cnt_I06,"
        + " sum(decode(data_type,'I07',1,0)) as cnt_I07,"
        + " sum(decode(data_type,'I08',1,0)) as cnt_I08,"
        + " sum(decode(data_type,'I09',1,0)) as cnt_I09,"
        + " sum(decode(data_type,'E01',1,0)) as cnt_E01,"
        + " sum(decode(data_type,'E02',1,0)) as cnt_E02,"
        + " sum(decode(data_type,'E03',1,0)) as cnt_E03,"
        + " sum(decode(data_type,'E04',1,0)) as cnt_E04,"
        + " sum(decode(data_type,'E05',1,0)) as cnt_E05," + " count(*) as db_cnt"
        + " from ptr_vip_data" + " where 1=1" + sqlCol(seqNo, "seq_no") + sqlCol(aprFlag, "apr_flag");
    this.sqlSelect(sql1);
    wp.colSet("cnt_I01", this.sqlInt("cnt_I01"));
    wp.colSet("cnt_I02", this.sqlInt("cnt_I02"));
    wp.colSet("cnt_I03", this.sqlInt("cnt_I03"));
    wp.colSet("cnt_I04", this.sqlInt("cnt_I04"));
    wp.colSet("cnt_I05", this.sqlInt("cnt_I05"));
    wp.colSet("cnt_I06", this.sqlInt("cnt_I06"));
    wp.colSet("cnt_I07", this.sqlInt("cnt_I07"));
    wp.colSet("cnt_I08", this.sqlInt("cnt_I08"));
    wp.colSet("cnt_I09", this.sqlInt("cnt_I09"));
    // --
    wp.colSet("cnt_E01", this.sqlInt("cnt_E01"));
    wp.colSet("cnt_E02", this.sqlInt("cnt_E02"));
    wp.colSet("cnt_E03", this.sqlInt("cnt_E03"));
    wp.colSet("cnt_E04", this.sqlInt("cnt_E04"));
    wp.colSet("cnt_E05", this.sqlInt("cnt_E05"));

  }

  @Override
  public void saveFunc() throws Exception {
    this.addRetrieve = true;
    this.updateRetrieve = true;

    if (wp.itemEq("apr_flag", "Y") && isDelete()) {
      // if(check_approve_zz()==false) return ;
      ofcapp.EcsApprove oo_apr = new EcsApprove(wp);
      if (oo_apr.checkAuthRun("ptrp0470", wp.itemStr2("approval_user"),
          wp.itemStr2("approval_passwd")) == false) {
        alertErr(oo_apr.getMesg());
        return;
      }
    }

    func = new ptrm02.Ptrm0470Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (rc == 1 && this.pos("|A|U", strAction) > 0) {
      userAction = true;
      dataRead();
    }

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // --
  String getDataType() {
    String lsType = "";
    if (wp.respHtml.indexOf("_I01") > 0)
      return "I01";
    else if (wp.respHtml.indexOf("_I02") > 0)
      return "I02";
    else if (wp.respHtml.indexOf("_I03") > 0)
      return "I03";
    else if (wp.respHtml.indexOf("_I04") > 0)
      return "I04";
    else if (wp.respHtml.indexOf("_I05") > 0)
      return "I05";
    else if (wp.respHtml.indexOf("_I06") > 0)
      return "I06";
    else if (wp.respHtml.indexOf("_I07") > 0)
      return "I07";
    else if (wp.respHtml.indexOf("_I08") > 0)
      return "I08";
    else if (wp.respHtml.indexOf("_I09") > 0)
      return "I09";
    else if (wp.respHtml.indexOf("_E01") > 0)
      return "E01";
    else if (wp.respHtml.indexOf("_E02") > 0)
      return "E02";
    else if (wp.respHtml.indexOf("_E03") > 0)
      return "E03";
    else if (wp.respHtml.indexOf("_E04") > 0)
      return "E04";
    else if (wp.respHtml.indexOf("_E05") > 0)
      return "E05";

    return "";
  }

  void detl2Save() throws Exception {
    int llOk = 0, llErr = 0;
    int ii = 0;
    // String ls_opt="";

    String lsType = wp.itemStr("data_type"); // get_dataType();
    if (empty(lsType)) {
      errmsg("無法取得資料類別[data_type]");
      return;
    }

    func = new ptrm02.Ptrm0470Func();
    func.setConn(wp);

    String[] aaCode = wp.itemBuff("data_code");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaCode.length;
    wp.colSet("IND_NUM", "" + aaCode.length);

    // -check duplication-
    ii = -1;
    for (String tmpStr : aaCode) {
      ii++;
      wp.colSet(ii, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ii, aaOpt)) {
        aaCode[ii] = "";
        continue;
      }

      if (ii != Arrays.asList(aaCode).indexOf(tmpStr)) {
        wp.colSet(ii, "ok_flag", "!");
        llErr++;
      }
      
    }
    
    if (llErr > 0) {
      alertErr("資料值重複: " + llErr);
      return;
    }

    // -copy Master-data-
    rc = func.copyMaster2unApr(lsType);
    if (rc == -1) {
      sqlCommit(-1);
      alertErr2(func.getMsg());
      return;
    }

    // -delete no-approve-
    if (func.dbDeleteDetl(lsType) < 0) {
      this.dbRollback();
      alertErr(func.getMsg());
      return;
    }

    for (int ll = 0; ll < aaCode.length; ll++) {
      wp.colSet(ll, "ok_flag", "");
      
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
    	  llOk++;
          continue;
      }
      
      if (empty(aaCode[ll])) {
    	 llOk++;
         continue;
      }

      func.varsSet("data_code", aaCode[ll]);
      if (func.dbInsertDetl(lsType) == 1) {
        llOk++;
      } else
        llErr++;
    }

    if (llOk > 0) {
      sqlCommit(1);
    }

    alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr);
    detl2Read();
  }
  
	private void checkIdnoAJAX() throws Exception {
//		// 2020-09-04 JustinWu: cancel check id_no
//		CommBusiCrd commBusiCrd = new CommBusiCrd();	
//		
//		 boolean isIdnoError = commBusiCrd.checkId(wp.itemStr("idnoAJAX"));
		 boolean isIdnoError = false; 
//
		 
		 wp.addJSON("isIdnoValid", isIdnoError ? "N" : "Y");

	}

	private void uploadFile() throws Exception {
		if (itemIsempty("zz_file_name")) {
		      alertErr2("上傳檔名: 不可空白");
		      return;
		    }
		
		if (wp.itemEq("data_type", "E04") || wp.itemEq("data_type", "I06")) {
			uploadIdno();
			detl2Read();
		}
		
	}

	private boolean uploadIdno() throws Exception {
		CommBusiCrd commBusiCrd = new CommBusiCrd();
		func = new ptrm02.Ptrm0470Func();
        func.setConn(wp);
		TarokoFileAccess tf = new TarokoFileAccess(wp);

	    String inputFile = wp.itemStr("zz_file_name");
	    int fi = tf.openInputText(inputFile, "MS950");

	    if (fi == -1)
	      return false;	
	    
	    ArrayList<String> idArr = new ArrayList<String>();
	    String idStr = "";
	    int cnt = 0, okCnt = 0, failCnt = 0, dupCnt = 0;
	    while (true) {
	    	idStr = tf.readTextFile(fi);
	    	
	        if (tf.endFile[fi].equals("Y"))
	          break;
	        
	        cnt++;
	        
			// 檢查是否重複
			if (idArr.contains(idStr)) {
				failCnt++;
				dupCnt++;
				continue;
			}
	        
	        // 檢查ID長度是否等於10
	        if (idStr.length() != 10) {
	        	failCnt++;
	        	continue;
	        }
	        
//	        // 2020-09-04 JustinWu: cancel check id_no 
//	        // 檢查ID是否符合格式   
//	        if ( commBusiCrd.checkId(idStr) ) {
//	        	failCnt++;
//	        	continue;
//	        }
	        
	        idArr.add(idStr);
	        
	    }
	    
		int size = idArr.size();
		if (size > 1) {
			// delete all not approved data
			rc = func.dbDeleteDetl(wp.itemStr("data_type"));
			if (rc == 1) {
				rc = func.dbInsertDetlByDataType(idArr, wp.itemStr("data_type") , wp.itemStr("seq_no"));
				if (rc != 1) {
					failCnt += size;
				} else {
					okCnt = size;
				}
			}else {
				failCnt += size;
			}
		}
		
		sqlCommit(rc);
		
		alertMsg(String.format("資料匯入處理筆數:%s, 成功(%s), 失敗(%s[重複(%s)])", cnt, okCnt, failCnt, dupCnt));
	    
	    return true;
		
	}
}
