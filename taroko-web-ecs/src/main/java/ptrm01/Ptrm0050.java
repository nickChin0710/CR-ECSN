/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-22  V1.00.00  Andy       program initial                            *
* 106-12-14            Andy		    update : ucStr==>zzStr                     *
* 107-02-06            Andy		    update : 刪除成本計算欄位                  *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
* 109-07-15  V1.00.03  tanwei     新增會計代號字段                           *
* 110-01-04  V1.00.04  yanghan    修改了變量名稱和方法名稱                   *
* 111-11-23  V1.00.05  Simon      新增分行移管作業                           *
******************************************************************************/

package ptrm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon; 

public class Ptrm0050 extends BaseEdit {
  String mExBranch = ""; // 變數--分行代碼

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      // wp.initFlag="Y";
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void queryFunc() throws Exception {
    // 設定queryRead() SQL條件

    // 判斷分行代碼是否為空值
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_branch")) == false) {
      wp.whereStr += " and  branch >= :branch ";
      setString("branch", wp.itemStr("ex_branch"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    // select columns 資料清單用
    wp.selectSQL =
        " branch" + " , curr_code" + " , full_chi_name" + " , brief_chi_name" 
            + " , merged_to_brn" + " , corp_no"
            + " , comp_addr" + " , comp_name" + " , user_code" + " , user_pass"+ " , branch_act_num"
            + " , uf_2ymd(mod_time) as mod_date" + " , mod_user ";
    // table name
    wp.daoTable = "gen_brn";
    // order column
    wp.whereOrder = " order by branch";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    list_wkdata();
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    mExBranch = wp.itemStr("branch");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExBranch = wp.itemStr("kk_branch");
    if (empty(mExBranch)) {
      mExBranch = itemKk("data_k1");
    }

    // select columns Detl用
    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + " , branch " + " , curr_code" + " , full_chi_name"
            + " , brief_chi_name" + " , full_eng_name" + " , brief_eng_name" 
            + " , merged_to_brn" + " , corp_no"
            + " , comp_addr" + " , comp_name" + " , user_code" + " , user_pass" + " , branch_act_num" +" , south_flag"
            + " , area_flag" + " , chi_addr_1" + " , chi_addr_2" + " , chi_addr_3" + " , chi_addr_4"
            + " , chi_addr_5" + " , eng_addr_1" + " , eng_addr_2" + " , eng_addr_3"
            + " , eng_addr_4" + " , brn_test" + " , connect_flag" + " , connect_date"
            + " , mail_fee_flag" + " , mail_cost" + " , uf_2ymd(mod_time) as mod_date"
            + " , mod_user ";
    wp.daoTable = "gen_brn";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  branch = :branch ";
    setString("branch", mExBranch);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, branch=" + mExBranch);
    }
    // dataRead();
	  wp.colSet("conf_flag", "");
    saveChkdata();
  }

  public void saveChkdata() {
	  wp.colSet("chk_curr_code", wp.colStr("curr_code"));
	  wp.colSet("chk_full_chi_name", wp.colStr("full_chi_name"));
	  wp.colSet("chk_brief_chi_name", wp.colStr("brief_chi_name"));
	  wp.colSet("chk_full_eng_name", wp.colStr("full_eng_name"));
	  wp.colSet("chk_brief_eng_name", wp.colStr("brief_eng_name"));
	  wp.colSet("chk_merged_to_brn", wp.colStr("merged_to_brn"));
	  wp.colSet("chk_corp_no", wp.colStr("corp_no"));
	  wp.colSet("chk_branch_act_num", wp.colStr("branch_act_num"));
	  wp.colSet("chk_comp_addr", wp.colStr("comp_addr"));
	  wp.colSet("chk_comp_name", wp.colStr("comp_name"));
	  wp.colSet("chk_user_code", wp.colStr("user_code"));
	  wp.colSet("chk_user_pass", wp.colStr("user_pass"));
	  wp.colSet("chk_south_flag", wp.colStr("south_flag"));
	  wp.colSet("chk_area_flag", wp.colStr("area_flag"));
	  wp.colSet("chk_chi_addr_1", wp.colStr("chi_addr_1"));
	  wp.colSet("chk_chi_addr_2", wp.colStr("chi_addr_2"));
	  wp.colSet("chk_chi_addr_3", wp.colStr("chi_addr_3"));
	  wp.colSet("chk_chi_addr_4", wp.colStr("chi_addr_4"));
	  wp.colSet("chk_chi_addr_5", wp.colStr("chi_addr_5"));
	  wp.colSet("chk_eng_addr_1", wp.colStr("eng_addr_1"));
	  wp.colSet("chk_eng_addr_2", wp.colStr("eng_addr_2"));
	  wp.colSet("chk_eng_addr_3", wp.colStr("eng_addr_3"));
	  wp.colSet("chk_eng_addr_4", wp.colStr("eng_addr_4"));
	  wp.colSet("chk_brn_test", wp.colStr("brn_test"));
	  wp.colSet("chk_connect_flag", wp.colStr("connect_flag"));
	  wp.colSet("chk_connect_date", wp.colStr("connect_date"));
	  wp.colSet("chk_mail_fee_flag", wp.colStr("mail_fee_flag"));
	  wp.colSet("chk_mail_cost", wp.colStr("mail_cost"));

  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    // if (!check_approve(wp.item_ss("apr_user"), wp.item_ss("apr_passwd")))
    // {
    // return;
    // }

		if (!funSaveCheck()) {
			rc = -1;
			return;
		}
		
    Ptrm0050Func func = new Ptrm0050Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

	private boolean funSaveCheck() throws Exception {

	//String ls_conf_flag=wp.itemStr("conf_flag");
		
		if(this.isDelete())	return true;
		
    if (this.isAdd()) {
      if (empty(wp.itemStr("kk_branch")) == true) {
 			  alertErr("請輸入分行代碼");
        return false;
      }
      if (empty(wp.itemStr("merged_to_brn")) == false) {
 			  alertErr("新增分行資料，不可輸入移管分行");
        return false;
      }
    } 

		//--是否異動--
		if(this.isUpdate())	{
		  if (   eqIgno(wp.itemStr2("curr_code"),wp.itemStr2("chk_curr_code"))
				  && eqIgno(wp.itemStr2("full_chi_name"),wp.itemStr2("chk_full_chi_name"))
				  && eqIgno(wp.itemStr2("brief_chi_name"),wp.itemStr2("chk_brief_chi_name"))
				  && eqIgno(wp.itemStr2("full_eng_name"),wp.itemStr2("chk_full_eng_name"))
				  && eqIgno(wp.itemStr2("brief_eng_name"),wp.itemStr2("chk_brief_eng_name"))
				  && eqIgno(wp.itemStr2("merged_to_brn"),wp.itemStr2("chk_merged_to_brn"))
				  && eqIgno(wp.itemStr2("corp_no"),wp.itemStr2("chk_corp_no"))
				  && eqIgno(wp.itemStr2("branch_act_num"),wp.itemStr2("chk_branch_act_num"))
				  && eqIgno(wp.itemStr2("comp_addr"),wp.itemStr2("chk_comp_addr"))
				  && eqIgno(wp.itemStr2("comp_name"),wp.itemStr2("chk_comp_name"))
				  && eqIgno(wp.itemStr2("user_code"),wp.itemStr2("chk_user_code"))
				  && eqIgno(wp.itemStr2("user_pass"),wp.itemStr2("chk_user_pass"))
				  && eqIgno(wp.itemStr2("south_flag"),wp.itemStr2("chk_south_flag"))
				  && eqIgno(wp.itemStr2("area_flag"),wp.itemStr2("chk_area_flag"))
				  && eqIgno(wp.itemStr2("chi_addr_1"),wp.itemStr2("chk_chi_addr_1"))
				  && eqIgno(wp.itemStr2("chi_addr_2"),wp.itemStr2("chk_chi_addr_2"))
				  && eqIgno(wp.itemStr2("chi_addr_3"),wp.itemStr2("chk_chi_addr_3"))
				  && eqIgno(wp.itemStr2("chi_addr_4"),wp.itemStr2("chk_chi_addr_4"))
				  && eqIgno(wp.itemStr2("chi_addr_5"),wp.itemStr2("chk_chi_addr_5"))
				  && eqIgno(wp.itemStr2("eng_addr_1"),wp.itemStr2("chk_eng_addr_1"))
				  && eqIgno(wp.itemStr2("eng_addr_2"),wp.itemStr2("chk_eng_addr_2"))
				  && eqIgno(wp.itemStr2("eng_addr_3"),wp.itemStr2("chk_eng_addr_3"))
				  && eqIgno(wp.itemStr2("eng_addr_4"),wp.itemStr2("chk_eng_addr_4"))
				  && eqIgno(wp.itemStr2("brn_test"),wp.itemStr2("chk_brn_test"))
				  && eqIgno(wp.itemStr2("connect_flag"),wp.itemStr2("chk_connect_flag"))
				  && eqIgno(wp.itemStr2("connect_date"),wp.itemStr2("chk_connect_date"))
				  && eqIgno(wp.itemStr2("mail_fee_flag"),wp.itemStr2("chk_mail_fee_flag"))
				  && wp.itemNum("mail_cost")==wp.itemNum("chk_mail_cost")
				//&& wp.itemNum("autopay_fix_amt")==wp.itemNum("chk_autopay_fix_amt")
				//&& wp.itemNum("autopay_rate")==wp.itemNum("chk_autopay_rate")                
         )
			{
			  alertErr("資料未異動, 不可存檔");
			  return false;
	 	  } 
		}
		
		if(this.isUpdate()) {
			if(eqIgno(wp.itemStr("merged_to_brn"),wp.itemStr("branch"))
		    )	
		  {
         alertErr("移管分行不可等於原分行");
  			 return false;
		  }

		  if(!wp.itemEmpty("chk_merged_to_brn") 
			   && !eqIgno(wp.itemStr("merged_to_brn"),wp.itemStr("chk_merged_to_brn"))
		    )	
		  {
         alertErr("已執行過分行移管作業，不可再更改移管分行");
  			 return false;
		  }
		  
		  if(!wp.itemEmpty("merged_to_brn") 
			   && !eqIgno(wp.itemStr("merged_to_brn"),wp.itemStr("chk_merged_to_brn"))
		    ) 
  		{
         String lsSql = "select count(*) as tot_cnt from gen_brn where branch = ?";
         Object[] param = new Object[] {wp.itemStr("merged_to_brn")};
         sqlSelect(lsSql, param);
         if (sqlInt("tot_cnt") <= 0) {
           alertErr("移管分行不存在，無法進行移管作業");
  			   return false;
         }
         if (itemEq("conf_flag", "Y") == false ) 
         {
           String lsMesg = "是否存檔並進行卡片檔受理行資料更新 ? ";
         	 wp.javascript("var resp =confirm('" + lsMesg + "');" + wp.newLine + "if (resp) {" + wp.newLine
         			+ "  document.dataForm.conf_flag.value='Y';" + wp.newLine + "  top.submitControl('U');" + wp.newLine
         			+ "}" + wp.newLine + "else {" + wp.newLine + "  alert('取消存檔!!!');" + wp.newLine + "}");
         	 wp.respCode = "02";
         	 return false;
         }
		  }
	  }
 
    if (wp.itemStr("connect_flag").equals("Y") && wp.itemEmpty("connect_date")) {
        alertErr("選取已加入連線，須輸入連線日期");
			  return false;
    }
		
    if (wp.itemStr("mail_fee_flag").equals("Y") && wp.itemNum("mail_cost") < 1) {
        alertErr("選取收取金融卡郵寄費用，郵寄成本須大於0");
			  return false;
    }
		
		return true;
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

      this.dddwList("dddw_branch", "gen_brn", "branch", "full_chi_name",
          "where 1=1 group by branch,full_chi_name order by branch");

      // 幣別
      wp.initOption = "--";
      wp.optionKey = wp.colStr("curr_code");
      dddwList("dddw_curr", "ptr_currcode", "curr_code_gl", "curr_chi_name",
          "where 1=1 group by curr_code_gl,curr_chi_name  order by curr_code_gl ");
    } catch (Exception ex) {
    }
  }

  // 改變欄位值顯示
  void list_wkdata() {
    String tmpStr = "", loatFlag = "";
    String[] cde = new String[] {"1", "2"};
    String[] txt = new String[] {"比率", "單價"};
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      tmpStr = wp.colStr(ii, "loat_flag");
      wp.colSet(ii, "loat_flag", commString.decode(tmpStr, cde, txt));
    }
  }

}
