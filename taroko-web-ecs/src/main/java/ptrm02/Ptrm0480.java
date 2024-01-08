/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-08-11  V1.00.02  JustinWu     add upload and ajax    
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
* *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *   
******************************************************************************/
package ptrm02;
/*VIP Code 取消參數維護 V.2018-0117
 * 2019-1203:  Alex  add initButton
 * 2018-0117:	JH		modify
 * */

import java.util.ArrayList;
import java.util.Arrays;

import busi.ecs.CommBusiCrd;
import ofcapp.BaseEdit;

import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Ptrm0480 extends BaseEdit {
  Ptrm0480Func func;
  String seqNo = "", aprFlag = "" ,dataKK3 = "", aprFlag1 = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
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
      /* 更新功能 */
      strAction = "U";
      detl2Save();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      strAction = "D";
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

	private void checkIdnoAJAX() throws Exception {
		CommBusiCrd commBusiCrd = new CommBusiCrd();
		
		 boolean isIdnoError = commBusiCrd.checkId(wp.itemStr("idnoAJAX"));

		 wp.addJSON("isIdnoValid", isIdnoError ? "N" : "Y");

	}

@Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_A01") > 0) {
        wp.optionKey = wp.colStr("ex_data_code");
        dddwList("dddw_vip_code", "ptr_vip_code", "vip_code", "vip_desc", "where 1=1");
      }
    } catch (Exception ex) {
    }

    // -action-code-
    if (wp.respHtml.indexOf("_A04") > 0) {
      wp.colSet("dddw_action", ecsfunc.DeCodeRsk.trialAction("", true));
    }

  }

  @Override
  public void initPage() {
    try {
      dataRead();
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  String getDatatype() {
    if (eqIgno(wp.respHtml, "ptrm0480_A01"))
      return "A01";
    else if (eqIgno(wp.respHtml, "ptrm0480_A02"))
      return "A02";
    else if (eqIgno(wp.respHtml, "ptrm0480_A03"))
      return "A03";
    else if (eqIgno(wp.respHtml, "ptrm0480_A04"))
      return "A04";
    else if (eqIgno(wp.respHtml, "ptrm0480_A05"))
      return "A05";

    return "";
  }

  void detl2Read() throws Exception {
	wp.colClear(0, "ex_data_code"); // clear ex_data_code for preventing from pass the value to other pages
	  
    seqNo = "PTR_VIP_CANCEL";
    aprFlag = "0";
    aprFlag1 = wp.itemStr("apr_flag");

    String lsType = getDatatype();
    if (empty(lsType)) {
      errmsg("無法取得資料類別[data_type]");
      return;
    }

    detl2Read(lsType);

  }

  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = "A.*, hex(rowid) as rowid," + "substrb(cancel_cond,1,1) as db_cond_vip, "
        + "substrb(cancel_cond,2,1) as db_cond_block, "
        + "substrb(cancel_cond,3,1) as db_cond_hirisk, "
        + "substrb(cancel_cond,4,1) as db_cond_overdue, "
        + "substrb(cancel_cond,6,1) as db_cond_precash, "
        + "substrb(cancel_cond,8,1) as db_cond_action_code, "
        + "substrb(cancel_cond,9,1) as db_cond_excllist, "
        + "to_char(mod_time,'yyyymmdd') as mod_date," + "'' as xxx";
    wp.daoTable = "ptr_vip_cancel A";
    wp.whereStr = " where 1=1"
        // + " and apr_flag in ('N','Y')"
        + " order by decode(apr_flag,'Y',9,1)" + commSqlStr.rownum(1);

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    if (wp.colEq("apr_flag", "N")) {
      wp.alertMesg("資料待覆核...");
    }
    dataReadAfter();
  }

  void dataReadAfter() {
    // --
    String sql1 = "select sum(decode(data_type,'A01',1,0)) as cnt_A01,"
        + " sum(decode(data_type,'A02',1,0)) as cnt_A02,"
        + " sum(decode(data_type,'A03',1,0)) as cnt_A03,"
        + " sum(decode(data_type,'A04',1,0)) as cnt_A04,"
        + " sum(decode(data_type,'A05',1,0)) as cnt_A05," + " count(*) as db_cnt"
        + " from ptr_vip_data" + " where 1=1" + sqlCol(wp.colNvl("apr_flag", "N"), "apr_flag");
    this.sqlSelect(sql1);
    wp.colSet("cnt_A01", this.sqlInt("cnt_A01"));
    wp.colSet("cnt_A02", this.sqlInt("cnt_A02"));
    wp.colSet("cnt_A03", this.sqlInt("cnt_A03"));
    wp.colSet("cnt_A04", this.sqlInt("cnt_A04"));
    wp.colSet("cnt_A05", this.sqlInt("cnt_A05"));
  }

  @Override
  public void saveFunc() throws Exception {
    this.addRetrieve = true;
    this.updateRetrieve = true;

    func = new ptrm02.Ptrm0480Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    if (rc == 1 && this.pos("|A|U|D", strAction) > 0) {
      userAction = true;
      dataRead();
    }

  }

  @Override
  public void initButton() {
    btnModeAud("xx");
    if (wp.colEq("apr_flag", "Y")) {
      this.btnDeleteOn(false);
    }

  }

  // --detl2_Read
  void detl2Read(String aType) throws Exception {
    seqNo = "0"; // wp.item_ss("seq_no");
    aprFlag = wp.itemStr("apr_flag");
    if (eqIgno(aprFlag, "Y")) {
      String sql1 = 
    		  " select count(*) as db_1 "
          + " from ptr_vip_cancel" 
          + " where 1=1" 
//          +sql_col(kk1,"seq_no")
          + " and apr_flag <>'Y' ";
      
      sqlSelect(sql1);
      if (this.sqlInt("db_1") > 0) {
        aprFlag = "N";
        wp.colSet("apr_flag", "N");
      }
    }

    wp.selectSQL = "data_code," 
                                + "hex(rowid) as rowid," 
    		                    + "apr_flag";
    wp.daoTable = "ptr_vip_data";
    wp.whereStr = " where 1=1" 
                              + " and table_name='PTR_VIP_CANCEL'" 
    	                      + " and seq_no='0'"
                              + sqlCol(aType, "data_type") 
                              + sqlCol(aprFlag, "apr_flag");
    wp.whereOrder = " order by data_code";
    pageQuery();
    if (sqlRowNum == 0) {
      this.selectOK();
    }

    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }

  // --detl2_save
  void detl2Save() throws Exception {
    int llOk = 0, llErr = 0, dupCnt = 0;
    int ii = 0;
    // String ls_opt="";

    String lsType = wp.itemStr("data_type");

    func = new ptrm02.Ptrm0480Func();
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
        dupCnt++;
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
      sqlCommit(-1);
      alertErr(func.getMsg());
      return;
    }

    for (int ll = 0; ll < aaCode.length; ll++) {
      wp.colSet(ll, "ok_flag", "");
      if (empty(aaCode[ll])) {
        continue;
      }
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
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

    alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr + " (重複=" + dupCnt + ")");
    detl2Read();
  }

	private void uploadFile() throws Exception {
		if (itemIsempty("zz_file_name")) {
		      alertErr2("上傳檔名: 不可空白");
		      return;
		    }
		
		if (wp.itemEq("data_type", "A05")) {
			uploadA05();
			detl2Read();
		}

		
	}

	private boolean uploadA05() throws Exception {
		CommBusiCrd commBusiCrd = new CommBusiCrd();
		func = new ptrm02.Ptrm0480Func();
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
	        
	        // 檢查ID是否符合格式   
	        if ( commBusiCrd.checkId(idStr) ) {
	        	failCnt++;
	        	continue;
	        }
	        
	        idArr.add(idStr);
	        
	    }
	    
		int size = idArr.size();
		if (size > 1) {
			// delete all not approved data
			rc = func.dbDeleteDetl("A05");
			if (rc == 1) {
				rc = func.dbInsertA05Detl(idArr);
				if (rc != 1) {
					failCnt += size;
				} else {
					okCnt = size;
				}
			}
		}
		
		sqlCommit(rc);
		
		alertMsg(String.format("資料匯入處理筆數:%s, 成功(%s), 失敗(%s[重複(%s)])", cnt, okCnt, dupCnt, failCnt));
	    
	    return true;
	}
}
